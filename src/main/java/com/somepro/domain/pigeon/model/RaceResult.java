package com.somepro.domain.pigeon.model;

import com.somepro.common.exception.BizException;
import com.somepro.domain.shared.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;

/**
 * 名次结果（聚合根）：一场赛的每条有效归巢算一条成绩，按分速降序排名次。
 * 重算覆盖不重插：同一场赛重算以最新一次为准（见 RaceResultRepository#replaceForRace）。
 */
@Getter
@Setter
public class RaceResult extends BaseEntity {

    private Long id;

    private Long raceId;

    private Long entryId;

    /** 分速（米/分钟，保留 2 位）。 */
    private BigDecimal speedMpm;

    /** 名次（1 起，按分速降序）。 */
    private Integer rankNo;

    /**
     * 由一条归巢报到算出分速：空距公里 × 1000 ÷ 飞行分钟，保留两位小数（四舍五入）。
     * 飞行分钟按秒级精度折算（飞行秒 ÷ 60），与计时设备的秒级读数对齐。
     * 数据撑不起计算时（缺开笼时间/空距、飞行时长为 0）抛业务异常给出说法，不算出脏成绩。
     */
    public static RaceResult of(Race race, Clocking clocking) {
        if (race.getReleaseAt() == null) {
            throw new BizException("赛项缺少开笼时间，无法计算分速");
        }
        if (race.getDistanceKm() == null) {
            throw new BizException("赛项缺少空距，无法计算分速");
        }
        long flightSeconds = Duration.between(race.getReleaseAt(), clocking.getClockAt()).getSeconds();
        if (flightSeconds <= 0) {
            throw new BizException("归巢时刻不晚于开笼时间，飞行时长为 0，无法计算分速（entryId="
                    + clocking.getEntryId() + "）");
        }
        // 分速(米/分钟) = 空距km × 1000 ÷ (飞行秒 ÷ 60) = 空距km × 60000 ÷ 飞行秒
        BigDecimal speed = race.getDistanceKm()
                .multiply(BigDecimal.valueOf(60_000L))
                .divide(BigDecimal.valueOf(flightSeconds), 2, RoundingMode.HALF_UP);
        RaceResult result = new RaceResult();
        result.setRaceId(race.getId());
        result.setEntryId(clocking.getEntryId());
        result.setSpeedMpm(speed);
        return result;
    }

    /** 领域规则：按分速从高到低排名次，1 起连号；分速相同按 entryId 升序，保证重算结果稳定。 */
    public static void assignRanks(List<RaceResult> results) {
        results.sort(Comparator.comparing(RaceResult::getSpeedMpm).reversed()
                .thenComparing(RaceResult::getEntryId));
        for (int i = 0; i < results.size(); i++) {
            results.get(i).setRankNo(i + 1);
        }
    }
}
