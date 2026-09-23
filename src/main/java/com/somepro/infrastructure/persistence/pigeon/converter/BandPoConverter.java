package com.somepro.infrastructure.persistence.pigeon.converter;

import com.somepro.domain.pigeon.model.Band;
import com.somepro.infrastructure.persistence.pigeon.po.BandPO;

/**
 * BandPO（表）→ Band（领域）转换器（基础设施层）。
 * 后半条线只读 t_band，故只有 toDomain 一个方向。
 */
public final class BandPoConverter {

    private BandPoConverter() {
    }

    public static Band toDomain(BandPO po) {
        Band domain = new Band();
        domain.setId(po.getId());
        domain.setBandCode(po.getBandCode());
        domain.setBandYear(po.getBandYear());
        domain.setOwnerName(po.getOwnerName());
        domain.setLoftCity(po.getLoftCity());
        domain.setStatus(po.getStatus());
        domain.setDelFlag(po.getDelFlag());
        domain.setCreateBy(po.getCreateBy());
        domain.setCreateTime(po.getCreateTime());
        domain.setUpdateBy(po.getUpdateBy());
        domain.setUpdateTime(po.getUpdateTime());
        return domain;
    }
}
