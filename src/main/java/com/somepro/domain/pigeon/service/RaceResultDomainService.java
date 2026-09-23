package com.somepro.domain.pigeon.service;

import com.somepro.domain.pigeon.model.Clocking;
import com.somepro.domain.pigeon.model.Race;
import com.somepro.domain.pigeon.model.RaceResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 成绩计算领域服务（领域层，无框架依赖）：
 * 一场赛打完出一次成绩 —— 为每笔有效归巢报到算分速，再按分速降序定名次（1 起连号）。
 *
 * 不负责落库：只产出当次最新成绩列表，覆盖语义由仓储端口
 * {@code RaceResultRepository.replaceForRace} 在一个事务里保证。
 */
@Component
public class RaceResultDomainService {

    /**
     * 排序口径：
     * 1）分速高者在前；
     * 2）同分速先到先得（归巢时刻更早者在前）；
     * 3）再相同用 entryId 兜底，保证排序稳定、名次唯一可复现。
     */
    private static final Comparator<Scored> RANK_ORDER = Comparator
            .comparing(Scored::speed, Comparator.reverseOrder())
            .thenComparing(Scored::clockAt)
            .thenComparing(Scored::entryId);

    private record Scored(Long entryId, java.time.LocalDateTime clockAt,
                          java.math.BigDecimal speed, RaceResult result) {
    }

    /**
     * @param race      赛项（取开笼时刻、空距）
     * @param clockings 该赛项全部有效归巢报到
     * @return 最新成绩，rankNo 已按 1 起连号填好
     */
    public List<RaceResult> compute(Race race, List<Clocking> clockings) {
        List<Scored> scoredList = new ArrayList<>();
        if (clockings != null) {
            for (Clocking clocking : clockings) {
                RaceResult rr = new RaceResult();
                rr.setRaceId(race.getId());
                rr.setEntryId(clocking.getEntryId());
                java.math.BigDecimal speed = RaceResult.calcSpeed(
                        race.getDistanceKm(), race.getReleaseAt(), clocking.getClockAt());
                rr.setSpeedMpm(speed);
                scoredList.add(new Scored(clocking.getEntryId(), clocking.getClockAt(), speed, rr));
            }
        }
        scoredList.sort(RANK_ORDER);

        List<RaceResult> results = new ArrayList<>(scoredList.size());
        int rank = 1;
        for (Scored scored : scoredList) {
            RaceResult rr = scored.result();
            rr.setRankNo(rank++);
            results.add(rr);
        }
        return results;
    }
}
