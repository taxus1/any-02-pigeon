package com.somepro.application.pigeon;

import com.somepro.common.exception.BizException;
import com.somepro.domain.pigeon.model.LeaderboardRow;
import com.somepro.domain.pigeon.model.RaceResult;
import com.somepro.domain.pigeon.repository.ClockingRepository;
import com.somepro.domain.pigeon.repository.RaceRepository;
import com.somepro.domain.pigeon.repository.RaceResultRepository;
import com.somepro.domain.shared.model.PageResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 成绩与名次榜用例（应用层）：算分速、排名次、出榜。
 * 计算规则（分速公式、名次连号）在领域层 RaceResult，这里只做编排。
 */
@Service
public class RaceResultAppService {

    private final RaceRepository raceRepository;
    private final ClockingRepository clockingRepository;
    private final RaceResultRepository raceResultRepository;

    public RaceResultAppService(RaceRepository raceRepository, ClockingRepository clockingRepository,
                                RaceResultRepository raceResultRepository) {
        this.raceRepository = raceRepository;
        this.clockingRepository = clockingRepository;
        this.raceResultRepository = raceResultRepository;
    }

    /**
     * 一场赛打完出一次成绩：按全部有效归巢算分速、排名次，整体覆盖旧成绩。
     * 返回本场成绩条数；重算以最新一次为准，不会算出两套。
     */
    public Mono<Integer> recalculate(Long raceId) {
        if (raceId == null) {
            return Mono.error(new BizException("赛项 id 不能为空"));
        }
        return raceRepository.findById(raceId)
                .switchIfEmpty(Mono.error(() -> new BizException("赛项不存在：raceId=" + raceId)))
                .flatMap(race -> clockingRepository.findByRaceId(race.getId())
                        .flatMap(clockings -> {
                            List<RaceResult> results = clockings.stream()
                                    .map(clocking -> RaceResult.of(race, clocking))
                                    .collect(Collectors.toCollection(ArrayList::new));
                            RaceResult.assignRanks(results);
                            return raceResultRepository.replaceForRace(race.getId(), results)
                                    .thenReturn(results.size());
                        }));
    }

    /** 名次榜：按赛项分页查，数据与已落库的成绩一致。 */
    public Mono<PageResult<LeaderboardRow>> pageBoard(Long raceId, int pageNum, int pageSize) {
        if (raceId == null) {
            return Mono.error(new BizException("赛项 id 不能为空"));
        }
        return raceResultRepository.pageBoard(raceId, pageNum, pageSize);
    }
}
