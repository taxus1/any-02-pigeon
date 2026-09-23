package com.somepro.domain.pigeon.repository;

import com.somepro.domain.pigeon.model.Entry;
import reactor.core.publisher.Mono;

/**
 * 集鸽登记仓储端口。本用例只读：报到时确认「集过鸽」。
 */
public interface EntryRepository {

    /**
     * 按赛项 + 足环查集鸽记录；没集过鸽返回空信号。
     */
    Mono<Entry> findByRaceAndBand(Long raceId, Long bandId);

    Mono<Entry> findById(Long id);
}
