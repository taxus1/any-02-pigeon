package com.somepro.infrastructure.persistence.pigeon.converter;

import com.somepro.domain.pigeon.model.RaceResult;
import com.somepro.infrastructure.persistence.pigeon.po.RaceResultPO;

/**
 * RaceResultPO（表）↔ RaceResult（领域）转换器（基础设施层）。
 */
public final class RaceResultPoConverter {

    private RaceResultPoConverter() {
    }

    public static RaceResultPO toPo(RaceResult domain) {
        RaceResultPO po = new RaceResultPO();
        po.setId(domain.getId());
        po.setRaceId(domain.getRaceId());
        po.setEntryId(domain.getEntryId());
        po.setSpeedMpm(domain.getSpeedMpm());
        po.setRankNo(domain.getRankNo());
        po.setDelFlag(domain.getDelFlag());
        po.setCreateBy(domain.getCreateBy());
        po.setCreateTime(domain.getCreateTime());
        po.setUpdateBy(domain.getUpdateBy());
        po.setUpdateTime(domain.getUpdateTime());
        return po;
    }

    public static RaceResult toDomain(RaceResultPO po) {
        RaceResult domain = new RaceResult();
        domain.setId(po.getId());
        domain.setRaceId(po.getRaceId());
        domain.setEntryId(po.getEntryId());
        domain.setSpeedMpm(po.getSpeedMpm());
        domain.setRankNo(po.getRankNo());
        domain.setDelFlag(po.getDelFlag());
        domain.setCreateBy(po.getCreateBy());
        domain.setCreateTime(po.getCreateTime());
        domain.setUpdateBy(po.getUpdateBy());
        domain.setUpdateTime(po.getUpdateTime());
        return domain;
    }
}
