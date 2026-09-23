package com.somepro.domain.pigeon.repository;

import com.somepro.domain.pigeon.model.Band;
import reactor.core.publisher.Mono;

/**
 * 足环档案仓储端口。本用例只读：按足环号查档案（鸽主、状态）。
 */
public interface BandRepository {

    Mono<Band> findByBandCode(String bandCode);
}
