package com.somepro.infrastructure.persistence.pigeon.converter;

import com.somepro.domain.pigeon.model.Entry;
import com.somepro.infrastructure.persistence.pigeon.po.EntryPO;

/**
 * EntryPO（表）→ Entry（领域）转换器（基础设施层）。
 * 后半条线只读 t_entry，故只有 toDomain 一个方向。
 */
public final class EntryPoConverter {

    private EntryPoConverter() {
    }

    public static Entry toDomain(EntryPO po) {
        Entry domain = new Entry();
        domain.setId(po.getId());
        domain.setRaceId(po.getRaceId());
        domain.setBandId(po.getBandId());
        domain.setBasketNo(po.getBasketNo());
        domain.setEntryTime(po.getEntryTime());
        domain.setDelFlag(po.getDelFlag());
        domain.setCreateBy(po.getCreateBy());
        domain.setCreateTime(po.getCreateTime());
        domain.setUpdateBy(po.getUpdateBy());
        domain.setUpdateTime(po.getUpdateTime());
        return domain;
    }
}
