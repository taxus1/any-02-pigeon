package com.somepro.domain.pigeon.model;

import com.somepro.domain.shared.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 归巢报到（聚合根）：一羽赛鸽一场赛只计第一次有效归巢。
 *
 * 不变量：归巢时刻必须落在赛项的时间窗内（不早于开笼、不晚于关门），
 * 由工厂方法委托 Race 校验，任何入口都绕不开。
 * 「一羽一场只认第一次」由应用层先查重、数据库 uk_entry 唯一键兜底。
 */
@Getter
@Setter
public class Clocking extends BaseEntity {

    private Long id;

    /** 集鸽登记 id（t_entry.id）。 */
    private Long entryId;

    private Long raceId;

    /** 归巢时刻（扫描/补录）。 */
    private LocalDateTime clockAt;

    /** 来源：SCAN 扫描 / MANUAL 手工补录。 */
    private ClockingSource source;

    /** 工厂方法：构造一次有效报到，并保证归巢时刻满足赛项时间窗。 */
    public static Clocking create(Entry entry, Race race, LocalDateTime clockAt, ClockingSource source) {
        race.assertClockable(clockAt);
        Clocking clocking = new Clocking();
        clocking.setEntryId(entry.getId());
        clocking.setRaceId(race.getId());
        clocking.setClockAt(clockAt);
        clocking.setSource(source);
        return clocking;
    }
}
