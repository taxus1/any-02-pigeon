package com.somepro.domain.pigeon.repository;

import com.somepro.domain.pigeon.model.Entry;
import reactor.core.publisher.Mono;

/**
 * 集鸽登记的仓储端口：报到前确认「这羽鸽子集过本场鸽」。
 */
public interface EntryRepository {

    Mono<Entry> findByRaceIdAndBandId(Long raceId, Long bandId);
}
