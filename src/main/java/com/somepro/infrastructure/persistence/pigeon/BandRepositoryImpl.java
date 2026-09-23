package com.somepro.infrastructure.persistence.pigeon;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.somepro.domain.pigeon.model.Band;
import com.somepro.domain.pigeon.repository.BandRepository;
import com.somepro.infrastructure.config.ReactiveOperatorContext;
import com.somepro.infrastructure.persistence.audit.AuditContextHolder;
import com.somepro.infrastructure.persistence.pigeon.converter.BandPoConverter;
import com.somepro.infrastructure.persistence.pigeon.po.BandPO;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Supplier;

/**
 * 足环档案仓储适配器（基础设施层）。本模块只读 t_band。
 * 阻塞 JDBC 一律经 {@link #blocking} 桥接，不在 event-loop 上直接调 Mapper。
 */
@Repository
public class BandRepositoryImpl implements BandRepository {

    private final BandMapper bandMapper;

    public BandRepositoryImpl(BandMapper bandMapper) {
        this.bandMapper = bandMapper;
    }

    @Override
    public Mono<Band> findByBandCode(String bandCode) {
        return blocking(() -> {
            // band_code 有唯一键 uk_band_code，selectOne 安全
            BandPO po = bandMapper.selectOne(Wrappers.<BandPO>lambdaQuery()
                    .eq(BandPO::getBandCode, bandCode));
            return po == null ? null : BandPoConverter.toDomain(po);
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
