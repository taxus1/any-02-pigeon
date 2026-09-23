package com.somepro.infrastructure.persistence.pigeon.support;

import com.somepro.infrastructure.config.ReactiveOperatorContext;
import com.somepro.infrastructure.persistence.audit.AuditContextHolder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Supplier;

/**
 * 赛鸽上下文仓储适配器共享的「响应式 × 阻塞 JDBC」桥接基类。
 *
 * 约定与 DemoItemRepositoryImpl#blocking 完全一致（见 README「唯一正确写法」）：
 * 先 deferContextual 在响应式线程上取操作人，再 subscribeOn(boundedElastic) 切线程，
 * 把操作人塞进 AuditContextHolder 供审计填充，绝不在 Netty event-loop 上跑 JDBC。
 */
public abstract class PigeonBlockingRepository {

    protected <T> Mono<T> blocking(Supplier<T> supplier) {
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
