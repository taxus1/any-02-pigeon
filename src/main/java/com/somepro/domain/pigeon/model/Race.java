package com.somepro.domain.pigeon.model;

import com.somepro.common.exception.BizException;
import com.somepro.domain.shared.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 训放计划（一场训放/赛事）的领域对象（纯领域，无框架注解）。
 *
 * 后半条线只读它：报到校验要用开笼/关门时刻，算分速要用空距。
 * 表映射在基础设施层 RacePO，互转见 RacePoConverter。
 */
@Getter
@Setter
public class Race extends BaseEntity {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Long id;

    /** 赛项编号，唯一（如 XF-2026-018）。 */
    private String raceCode;

    private String title;

    private String releaseSite;

    /** 放飞时刻（开笼时间）。 */
    private LocalDateTime releaseAt;

    /** 关门时刻（报到截止；空 = 不限）。 */
    private LocalDateTime closeAt;

    /** 空距（公里）。 */
    private BigDecimal distanceKm;

    /** DRAFT 筹备 / SEALED 已集鸽 / RELEASED 已放飞 / CLOSED 已关棚。 */
    private String status;

    /**
     * 领域规则：归巢时刻必须不早于开笼时刻、不晚于关门时刻（关门时刻为空则不限）。
     * 卡不住的输入一律抛业务异常给出明确说法，不允许静默落库。
     */
    public void assertClockable(LocalDateTime clockAt) {
        if (clockAt == null) {
            throw new BizException("归巢时刻不能为空");
        }
        if (releaseAt != null && clockAt.isBefore(releaseAt)) {
            throw new BizException("归巢时刻早于开笼时间（" + releaseAt.format(TIME_FMT) + "），不能报到");
        }
        if (closeAt != null && clockAt.isAfter(closeAt)) {
            throw new BizException("归巢时刻晚于关门时刻（" + closeAt.format(TIME_FMT) + "），不能报到");
        }
    }
}
