package com.somepro.domain.pigeon.model;

import com.somepro.common.exception.BizException;
import com.somepro.domain.shared.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 归巢报到聚合（领域层，赛鸽上下文）。
 *
 * 不变量（t_clocking 上 uk_entry 唯一约束兜底，业务侧也先判一次）：
 * - 一羽鸽子一场赛只认第一次有效归巢，重复报到打回；
 * - 归巢时刻必须落在赛项时间窗内（晚于开笼、不晚于关门）；
 * - 来源只认 SCAN / MANUAL。
 */
@Getter
@Setter
public class Clocking extends BaseEntity {

    private Long id;

    /** 集鸽登记 id（t_entry.id），唯一：一条集鸽对应至多一条报到。 */
    private Long entryId;

    private Long raceId;

    /** 归巢时刻。 */
    private LocalDateTime clockAt;

    /** SCAN / MANUAL。 */
    private String source;

    /**
     * 工厂方法：所有报到在领域层过一遍规则，非法输入直接抛 {@link BizException}（带中文原因），
     * 由全局异常收口成统一 Result，绝不闷头存进库。
     *
     * 「是否已集鸽 / 是否重复报到」属于跨聚合校验，由应用层先查再调本方法；
     * 本方法只负责单条报到自身与赛项时间窗的不变量。
     */
    public static Clocking register(Entry entry, Race race, LocalDateTime clockAt, String sourceCode) {
        ClockSource source = ClockSource.of(sourceCode);
        if (source == null) {
            throw new BizException("报到来源非法：" + sourceCode + "，只支持 SCAN（扫描）/ MANUAL（手工补录）");
        }
        String windowError = race.checkClockWindow(clockAt);
        if (windowError != null) {
            throw new BizException(windowError);
        }
        Clocking clocking = new Clocking();
        clocking.setEntryId(entry.getId());
        clocking.setRaceId(race.getId());
        clocking.setClockAt(clockAt);
        clocking.setSource(source.name());
        return clocking;
    }
}
