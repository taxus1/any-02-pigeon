package com.somepro.infrastructure.persistence.pigeon;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.somepro.common.exception.BizException;
import com.somepro.domain.pigeon.model.Clocking;
import com.somepro.domain.pigeon.repository.ClockingRepository;
import com.somepro.infrastructure.config.ReactiveOperatorContext;
import com.somepro.infrastructure.persistence.audit.AuditContextHolder;
import com.somepro.infrastructure.persistence.pigeon.converter.ClockingPoConverter;
import com.somepro.infrastructure.persistence.pigeon.po.ClockingPO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 归巢报到仓储适配器（基础设施层）。
 * 阻塞 JDBC 一律经 {@link #blocking} 桥接，不在 event-loop 上直接调 Mapper。
 */
@Repository
public class ClockingRepositoryImpl implements ClockingRepository {

    private final ClockingMapper clockingMapper;

    public ClockingRepositoryImpl(ClockingMapper clockingMapper) {
        this.clockingMapper = clockingMapper;
    }

    @Override
    public Mono<Clocking> save(Clocking clocking) {
        return blocking(() -> {
            ClockingPO po = ClockingPoConverter.toPo(clocking);
            if (po.getId() == null) {
                po.setId(IdUtil.getSnowflakeNextId());
                try {
                    clockingMapper.insert(po);
                } catch (DuplicateKeyException e) {
                    // 并发报到时由唯一索引 uk_entry 兜底：一羽一场只认第一次归巢
                    throw new BizException("该鸽已报到，一羽一场只认第一次归巢");
                }
            } else {
                clockingMapper.updateById(po);
            }
            // insert 后框架会回填 id 与审计字段，转回领域对象一并返回
            return ClockingPoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<Clocking> findByEntryId(Long entryId) {
        return blocking(() -> {
            // entry_id 有唯一键 uk_entry，selectOne 安全
            ClockingPO po = clockingMapper.selectOne(Wrappers.<ClockingPO>lambdaQuery()
                    .eq(ClockingPO::getEntryId, entryId));
            return po == null ? null : ClockingPoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<List<Clocking>> findByRaceId(Long raceId) {
        return blocking(() -> clockingMapper.selectList(Wrappers.<ClockingPO>lambdaQuery()
                        .eq(ClockingPO::getRaceId, raceId))
                .stream()
                .map(ClockingPoConverter::toDomain)
                .collect(Collectors.toList()));
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
