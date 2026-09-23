package com.somepro.infrastructure.persistence.pigeon.converter;

import com.somepro.domain.pigeon.model.RaceResult;
import com.somepro.domain.pigeon.model.RankRow;
import com.somepro.infrastructure.persistence.pigeon.po.RaceResultPO;
import com.somepro.infrastructure.persistence.pigeon.po.RankRowPO;

/**
 * 成绩 / 名次榜投影的 PO ↔ 领域转换。
 */
public final class RaceResultPoConverter {

    private RaceResultPoConverter() {
    }

    public static RaceResultPO toPo(RaceResult d) {
        RaceResultPO po = new RaceResultPO();
        po.setId(d.getId());
        po.setRaceId(d.getRaceId());
        po.setEntryId(d.getEntryId());
        po.setSpeedMpm(d.getSpeedMpm());
        po.setRankNo(d.getRankNo());
        po.setDelFlag(d.getDelFlag());
        po.setCreateBy(d.getCreateBy());
        po.setCreateTime(d.getCreateTime());
        po.setUpdateBy(d.getUpdateBy());
        po.setUpdateTime(d.getUpdateTime());
        return po;
    }

    public static RaceResult toDomain(RaceResultPO po) {
        RaceResult d = new RaceResult();
        d.setId(po.getId());
        d.setRaceId(po.getRaceId());
        d.setEntryId(po.getEntryId());
        d.setSpeedMpm(po.getSpeedMpm());
        d.setRankNo(po.getRankNo());
        d.setDelFlag(po.getDelFlag());
        d.setCreateBy(po.getCreateBy());
        d.setCreateTime(po.getCreateTime());
        d.setUpdateBy(po.getUpdateBy());
        d.setUpdateTime(po.getUpdateTime());
        return d;
    }

    public static RankRow toRankRow(RankRowPO po) {
        return new RankRow(
                po.getEntryId(),
                po.getBandId(),
                po.getBandCode(),
                po.getOwnerName(),
                po.getClockAt(),
                po.getSpeedMpm(),
                po.getRankNo());
    }
}
