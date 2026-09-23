package com.somepro.domain.pigeon.repository;

import com.somepro.domain.pigeon.model.Band;
import reactor.core.publisher.Mono;

/**
 * 足环档案的仓储端口：报到时按足环号定位鸽子。
 */
public interface BandRepository {

    Mono<Band> findByBandCode(String bandCode);
}
