package com.somepro.domain.pigeon.repository;

import com.somepro.domain.pigeon.model.RaceResult;
import com.somepro.domain.pigeon.model.RankRow;
import com.somepro.domain.shared.model.PageResult;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 名次结果仓储端口。
 */
public interface RaceResultRepository {

    /**
     * 重算覆盖：在一个事务里物理清掉该赛项旧成绩、写入新成绩。
     * 「同一场赛重算不能算出两套」—— 必须先删后插、整段提交，
     * 不使用逻辑删除（t_result.uk_race_entry 不含 del_flag，软删行会占住唯一键）。
     *
     * @return 写入后的最新成绩（已带名次）
     */
    Mono<List<RaceResult>> replaceForRace(Long raceId, List<RaceResult> fresh);

    /** 某赛项当前有效成绩（名次顺序）。 */
    Mono<List<RaceResult>> listByRace(Long raceId);

    /**
     * 名次榜分页：联表取足环号 / 鸽主 / 归巢时刻，按名次升序。
     */
    Mono<PageResult<RankRow>> pageRank(Long raceId, int pageNum, int pageSize);
}
