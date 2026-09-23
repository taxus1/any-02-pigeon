package com.somepro.domain.pigeon.model;

import com.somepro.common.exception.BizException;
import com.somepro.domain.shared.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 名次结果聚合（领域层，赛鸽上下文）：t_result 一行。
 *
 * 分速（米/分钟）= 空距公里数 × 1000 ÷ 飞行分钟数，保留两位小数（HALF_UP）。
 * 名次按分速从高到低，1 起连号；同分速按先到先得（归巢时刻更早者靠前）。
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

    private static final BigDecimal THOUSAND = new BigDecimal("1000");

    /**
     * 计算分速。
     *
     * 飞行分钟数不取整：用精确的秒级时长换算成分钟（Duration.toMillis()/60000），
     * 否则两羽鸽子相差几十秒会被抹成同一分钟，分速失真。
     *
     * @param distanceKm 赛项空距（公里）
     * @param releaseAt  开笼时刻
     * @param clockAt    归巢时刻（调用前已保证晚于开笼）
     * @return 保留两位小数的分速
     */
    public static BigDecimal calcSpeed(BigDecimal distanceKm, LocalDateTime releaseAt, LocalDateTime clockAt) {
        if (distanceKm == null || distanceKm.signum() <= 0) {
            throw new BizException("赛项空距缺失或非正数，无法计算分速");
        }
        long millis = Duration.between(releaseAt, clockAt).toMillis();
        if (millis <= 0) {
            throw new BizException("飞行时长为 0 或为负，无法计算分速");
        }
        BigDecimal flightMinutes = BigDecimal.valueOf(millis)
                .divide(BigDecimal.valueOf(60_000L), 10, RoundingMode.HALF_UP);
        return distanceKm.multiply(THOUSAND)
                .divide(flightMinutes, 2, RoundingMode.HALF_UP);
    }
}
