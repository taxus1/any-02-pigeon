package com.somepro.infrastructure.persistence.pigeon.converter;

import com.somepro.domain.pigeon.model.Band;
import com.somepro.infrastructure.persistence.pigeon.po.BandPO;

/**
 * BandPO（t_band）↔ Band（领域）。
 */
public final class BandPoConverter {

    private BandPoConverter() {
    }

    public static Band toDomain(BandPO po) {
        Band d = new Band();
        d.setId(po.getId());
        d.setBandCode(po.getBandCode());
        d.setBandYear(po.getBandYear());
        d.setOwnerName(po.getOwnerName());
        d.setLoftCity(po.getLoftCity());
        d.setStatus(po.getStatus());
        d.setDelFlag(po.getDelFlag());
        d.setCreateBy(po.getCreateBy());
        d.setCreateTime(po.getCreateTime());
        d.setUpdateBy(po.getUpdateBy());
        d.setUpdateTime(po.getUpdateTime());
        return d;
    }
}
