package com.somepro.infrastructure.persistence.pigeon.converter;

import com.somepro.domain.pigeon.model.Race;
import com.somepro.infrastructure.persistence.pigeon.po.RacePO;

/**
 * RacePO（表）→ Race（领域）转换器（基础设施层）。
 * 后半条线只读 t_race，故只有 toDomain 一个方向。
 */
public final class RacePoConverter {

    private RacePoConverter() {
    }

    public static Race toDomain(RacePO po) {
        Race domain = new Race();
        domain.setId(po.getId());
        domain.setRaceCode(po.getRaceCode());
        domain.setTitle(po.getTitle());
        domain.setReleaseSite(po.getReleaseSite());
        domain.setReleaseAt(po.getReleaseAt());
        domain.setCloseAt(po.getCloseAt());
        domain.setDistanceKm(po.getDistanceKm());
        domain.setStatus(po.getStatus());
        domain.setDelFlag(po.getDelFlag());
        domain.setCreateBy(po.getCreateBy());
        domain.setCreateTime(po.getCreateTime());
        domain.setUpdateBy(po.getUpdateBy());
        domain.setUpdateTime(po.getUpdateTime());
        return domain;
    }
}
