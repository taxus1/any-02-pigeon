package com.somepro.domain.pigeon.model;

import com.somepro.domain.shared.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 训放计划 / 赛项（领域层，赛鸽上下文）。
 *
 * 纯领域对象，不带任何持久化注解。报到、算分速的时间窗口都以本聚合的
 * releaseAt（开笼）/ closeAt（关门，空=不限）/ distanceKm（空距）为准。
 */
@Getter
@Setter
public class Race extends BaseEntity {

    private Long id;

    /** 赛项编号，唯一（如 XF-2026-018）。 */
    private String raceCode;

    private String title;

    /** 放飞地。 */
    private String releaseSite;

    /** 放飞时刻（开笼时间）：归巢时刻必须晚于它。 */
    private LocalDateTime releaseAt;

    /** 关门时刻（报到截止；空=不限）：过点归来不记成绩。 */
    private LocalDateTime closeAt;

    /** 空距（公里），分速计算的距离基准。 */
    private BigDecimal distanceKm;

    /** DRAFT 筹备 / SEALED 已集鸽 / RELEASED 已放飞 / CLOSED 已关棚。 */
    private String status;

    /**
     * 判断归巢时刻是否落在本赛项允许报到的时间窗内。
     *
     * 规则（题面）：
     * - 不早于开笼：clockAt &lt;= releaseAt 一律视为错误（早于开笼、与开笼同一时刻都不可能真归巢）；
     * - 关门时刻为空表示不限；非空时晚于关门（clockAt &gt; closeAt）不记成绩。
     *
     * @return null 表示通过；否则返回给调用方的中文拒绝原因
     */
    public String checkClockWindow(LocalDateTime clockAt) {
        if (clockAt == null) {
            return "归巢时刻不能为空";
        }
        if (!clockAt.isAfter(releaseAt)) {
            return "归巢时刻早于或等于开笼时间（" + releaseAt + "），报到无效";
        }
        if (closeAt != null && clockAt.isAfter(closeAt)) {
            return "归巢时刻晚于关门时间（" + closeAt + "），报到无效";
        }
        return null;
    }
}
