package com.somepro.infrastructure.persistence.pigeon.converter;

import com.somepro.domain.pigeon.model.Clocking;
import com.somepro.domain.pigeon.model.ClockingSource;
import com.somepro.infrastructure.persistence.pigeon.po.ClockingPO;

/**
 * ClockingPO（表）↔ Clocking（领域）转换器（基础设施层）。
 * source 在库里是 VARCHAR、在领域是枚举，互转在这里做。
 */
public final class ClockingPoConverter {

    private ClockingPoConverter() {
    }

    public static ClockingPO toPo(Clocking domain) {
        ClockingPO po = new ClockingPO();
        po.setId(domain.getId());
        po.setEntryId(domain.getEntryId());
        po.setRaceId(domain.getRaceId());
        po.setClockAt(domain.getClockAt());
        po.setSource(domain.getSource() == null ? null : domain.getSource().name());
        po.setDelFlag(domain.getDelFlag());
        po.setCreateBy(domain.getCreateBy());
        po.setCreateTime(domain.getCreateTime());
        po.setUpdateBy(domain.getUpdateBy());
        po.setUpdateTime(domain.getUpdateTime());
        return po;
    }

    public static Clocking toDomain(ClockingPO po) {
        Clocking domain = new Clocking();
        domain.setId(po.getId());
        domain.setEntryId(po.getEntryId());
        domain.setRaceId(po.getRaceId());
        domain.setClockAt(po.getClockAt());
        domain.setSource(po.getSource() == null ? null : ClockingSource.valueOf(po.getSource()));
        domain.setDelFlag(po.getDelFlag());
        domain.setCreateBy(po.getCreateBy());
        domain.setCreateTime(po.getCreateTime());
        domain.setUpdateBy(po.getUpdateBy());
        domain.setUpdateTime(po.getUpdateTime());
        return domain;
    }
}
