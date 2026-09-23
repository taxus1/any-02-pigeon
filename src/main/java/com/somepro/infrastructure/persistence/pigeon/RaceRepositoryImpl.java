package com.somepro.infrastructure.persistence.pigeon;

import com.somepro.domain.pigeon.model.Race;
import com.somepro.domain.pigeon.repository.RaceRepository;
import com.somepro.infrastructure.config.ReactiveOperatorContext;
import com.somepro.infrastructure.persistence.audit.AuditContextHolder;
import com.somepro.infrastructure.persistence.pigeon.converter.RacePoConverter;
import com.somepro.infrastructure.persistence.pigeon.po.RacePO;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Supplier;

/**
 * 训放计划仓储适配器（基础设施层）。本模块只读 t_race。
 * 阻塞 JDBC 一律经 {@link #blocking} 桥接，不在 event-loop 上直接调 Mapper。
 */
@Repository
public class RaceRepositoryImpl implements RaceRepository {

    private final RaceMapper raceMapper;

    public RaceRepositoryImpl(RaceMapper raceMapper) {
        this.raceMapper = raceMapper;
    }

    @Override
    public Mono<Race> findById(Long id) {
        return blocking(() -> {
            RacePO po = raceMapper.selectById(id);
            return po == null ? null : RacePoConverter.toDomain(po);
        });
    }

    /**
     * 阻塞 DB 调用 → 响应式链路的桥接器（与 DemoItemRepositoryImpl#blocking 同一约定）：
     * 先在响应式线程上从 Reactor Context 取操作人，再切 boundedElastic 执行 JDBC。
     */
    private <T> Mono<T> blocking(Supplier<T> supplier) {
        return Mono.deferContextual(ctx -> {
            String operator = ReactiveOperatorContext.getOperator(ctx);
            return Mono.fromCallable(() -> {
                AuditContextHolder.setOperator(operator);
                try {
                    return supplier.get();
                } finally {
                    AuditContextHolder.clear();
                }
            }).subscribeOn(Schedulers.boundedElastic());
        });
    }
}
