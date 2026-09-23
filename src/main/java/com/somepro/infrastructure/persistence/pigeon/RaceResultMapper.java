package com.somepro.infrastructure.persistence.pigeon;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.somepro.infrastructure.persistence.pigeon.po.RaceResultPO;
import com.somepro.infrastructure.persistence.pigeon.po.RankRowPO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 名次结果 Mapper。
 *
 * 除 BaseMapper 能力外两条手写 SQL：
 * - 物理删除某赛项全部成绩：重算「先删后插」用。不能用 @TableLogic 的逻辑删除 ——
 *   t_result.uk_race_entry 是 (race_id, entry_id)，不含 del_flag，软删行会占住唯一键导致重插撞键；
 * - 名次榜联表查询：每张表显式带 del_flag = 0（@TableLogic 不拦截手写 SQL）。
 */
@Mapper
public interface RaceResultMapper extends BaseMapper<RaceResultPO> {

    /**
     * 物理清空一场赛的成绩（仅在重算事务内调用）。
     */
    @Delete("DELETE FROM t_result WHERE race_id = #{raceId}")
    int physicalDeleteByRace(@Param("raceId") Long raceId);

    /**
     * 名次榜：按名次升序。PageHelper 会在外面套 LIMIT，并自动生成 count。
     * clock_at 取 t_clocking（一条 entry 至多一条报到，inner join 保证成绩必有报到）。
     */
    @Select("""
            SELECT r.entry_id        AS entryId,
                   b.id              AS bandId,
                   b.band_code       AS bandCode,
                   b.owner_name      AS ownerName,
                   c.clock_at        AS clockAt,
                   r.speed_mpm       AS speedMpm,
                   r.rank_no         AS rankNo
              FROM t_result r
              INNER JOIN t_clocking c
                      ON c.entry_id = r.entry_id AND c.del_flag = 0
              INNER JOIN t_entry e ON e.id = r.entry_id AND e.del_flag = 0
              INNER JOIN t_band b ON b.id = e.band_id AND b.del_flag = 0
             WHERE r.del_flag = 0
               AND r.race_id = #{raceId}
             ORDER BY r.rank_no ASC
            """)
    List<RankRowPO> selectRankRows(@Param("raceId") Long raceId);
}
