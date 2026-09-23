package com.somepro.infrastructure.persistence.pigeon.converter;

import com.somepro.domain.pigeon.model.Clocking;
import com.somepro.infrastructure.persistence.pigeon.po.ClockingPO;

/**
 * ClockingPO（t_clocking）↔ Clocking（领域）。
 */
public final class ClockingPoConverter {

    private ClockingPoConverter() {
    }

    public static ClockingPO toPo(Clocking d) {
        ClockingPO po = new ClockingPO();
        po.setId(d.getId());
        po.setEntryId(d.getEntryId());
        po.setRaceId(d.getRaceId());
        po.setClockAt(d.getClockAt());
        po.setSource(d.getSource());
        po.setDelFlag(d.getDelFlag());
        po.setCreateBy(d.getCreateBy());
        po.setCreateTime(d.getCreateTime());
        po.setUpdateBy(d.getUpdateBy());
        po.setUpdateTime(d.getUpdateTime());
        return po;
    }

    public static Clocking toDomain(ClockingPO po) {
        Clocking d = new Clocking();
        d.setId(po.getId());
        d.setEntryId(po.getEntryId());
        d.setRaceId(po.getRaceId());
        d.setClockAt(po.getClockAt());
        d.setSource(po.getSource());
        d.setDelFlag(po.getDelFlag());
        d.setCreateBy(po.getCreateBy());
        d.setCreateTime(po.getCreateTime());
        d.setUpdateBy(po.getUpdateBy());
        d.setUpdateTime(po.getUpdateTime());
        return d;
    }
}
