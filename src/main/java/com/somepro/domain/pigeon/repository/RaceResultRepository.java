package com.somepro.domain.pigeon.repository;

import com.somepro.domain.pigeon.model.LeaderboardRow;
import com.somepro.domain.pigeon.model.RaceResult;
import com.somepro.domain.shared.model.PageResult;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 名次结果的仓储端口。
 */
public interface RaceResultRepository {

    /**
     * 重算覆盖：清掉本场旧成绩后整批写入新成绩（同一事务），
     * 保证同一场赛重算不会算出两套，以最新一次为准。
     */
    Mono<Void> replaceForRace(Long raceId, List<RaceResult> results);

    /** 名次榜分页：数据来自已落库的成绩，榜上数与库里的成绩一致。 */
    Mono<PageResult<LeaderboardRow>> pageBoard(Long raceId, int pageNum, int pageSize);
}
