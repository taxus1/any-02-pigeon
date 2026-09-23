package com.somepro.domain.pigeon.repository;

import com.somepro.domain.pigeon.model.Race;
import reactor.core.publisher.Mono;

/**
 * 训放赛项仓储端口。本用例只读：算分速需要开笼时间与空距，报到需要时间窗。
 */
public interface RaceRepository {

    Mono<Race> findById(Long id);
}
