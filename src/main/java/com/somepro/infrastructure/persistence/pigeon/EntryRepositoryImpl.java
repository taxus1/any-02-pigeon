package com.somepro.infrastructure.persistence.pigeon;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.somepro.domain.pigeon.model.Entry;
import com.somepro.domain.pigeon.repository.EntryRepository;
import com.somepro.infrastructure.config.ReactiveOperatorContext;
import com.somepro.infrastructure.persistence.audit.AuditContextHolder;
import com.somepro.infrastructure.persistence.pigeon.converter.EntryPoConverter;
import com.somepro.infrastructure.persistence.pigeon.po.EntryPO;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Supplier;

/**
 * 集鸽登记仓储适配器（基础设施层）。本模块只读 t_entry。
 * 阻塞 JDBC 一律经 {@link #blocking} 桥接，不在 event-loop 上直接调 Mapper。
 */
@Repository
public class EntryRepositoryImpl implements EntryRepository {

    private final EntryMapper entryMapper;

    public EntryRepositoryImpl(EntryMapper entryMapper) {
        this.entryMapper = entryMapper;
    }

    @Override
    public Mono<Entry> findByRaceIdAndBandId(Long raceId, Long bandId) {
        return blocking(() -> {
            // (race_id, band_id) 有唯一键 uk_race_band，selectOne 安全
            EntryPO po = entryMapper.selectOne(Wrappers.<EntryPO>lambdaQuery()
                    .eq(EntryPO::getRaceId, raceId)
                    .eq(EntryPO::getBandId, bandId));
            return po == null ? null : EntryPoConverter.toDomain(po);
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
