package com.somepro.domain.pigeon.repository;

import com.somepro.domain.pigeon.model.Race;
import reactor.core.publisher.Mono;

/**
 * 训放计划的仓储端口：后半条线只按 id 读（报到校验、算分速都要赛项的时间窗与空距）。
 */
public interface RaceRepository {

    Mono<Race> findById(Long id);
}
