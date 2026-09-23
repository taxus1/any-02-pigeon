package com.somepro.infrastructure.persistence.pigeon;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.somepro.infrastructure.persistence.pigeon.po.RaceResultPO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * t_result 的 MyBatis-Plus Mapper（基础设施层）。
 * 阻塞 JDBC API，只能在仓储适配器的 blocking(...) 里调用。
 */
@Mapper
public interface RaceResultMapper extends BaseMapper<RaceResultPO> {

    /**
     * 物理删除一场赛的全部成绩（成绩重算「覆盖不重插」专用）。
     * 必须物理删而不是逻辑删：uk_race_entry 唯一键不含 del_flag，
     * 逻辑删除的旧行仍占着唯一键，重算后的新成绩会插不进去。
     */
    @Delete("DELETE FROM t_result WHERE race_id = #{raceId}")
    int physicalDeleteByRaceId(@Param("raceId") Long raceId);
}
