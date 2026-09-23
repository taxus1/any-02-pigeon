package com.somepro.infrastructure.persistence.pigeon.converter;

import com.somepro.domain.pigeon.model.Race;
import com.somepro.infrastructure.persistence.pigeon.po.RacePO;

/**
 * RacePO（t_race）↔ Race（领域）。
 */
public final class RacePoConverter {

    private RacePoConverter() {
    }

    public static Race toDomain(RacePO po) {
        Race d = new Race();
        d.setId(po.getId());
        d.setRaceCode(po.getRaceCode());
        d.setTitle(po.getTitle());
        d.setReleaseSite(po.getReleaseSite());
        d.setReleaseAt(po.getReleaseAt());
        d.setCloseAt(po.getCloseAt());
        d.setDistanceKm(po.getDistanceKm());
        d.setStatus(po.getStatus());
        d.setDelFlag(po.getDelFlag());
        d.setCreateBy(po.getCreateBy());
        d.setCreateTime(po.getCreateTime());
        d.setUpdateBy(po.getUpdateBy());
        d.setUpdateTime(po.getUpdateTime());
        return d;
    }
}
