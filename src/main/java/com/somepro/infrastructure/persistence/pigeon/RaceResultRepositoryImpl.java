package com.somepro.infrastructure.persistence.pigeon;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.somepro.common.exception.BizException;
import com.somepro.domain.pigeon.model.LeaderboardRow;
import com.somepro.domain.pigeon.model.RaceResult;
import com.somepro.domain.pigeon.repository.RaceResultRepository;
import com.somepro.domain.shared.model.PageResult;
import com.somepro.infrastructure.config.ReactiveOperatorContext;
import com.somepro.infrastructure.persistence.audit.AuditContextHolder;
import com.somepro.infrastructure.persistence.pigeon.converter.RaceResultPoConverter;
import com.somepro.infrastructure.persistence.pigeon.po.BandPO;
import com.somepro.infrastructure.persistence.pigeon.po.ClockingPO;
import com.somepro.infrastructure.persistence.pigeon.po.EntryPO;
import com.somepro.infrastructure.persistence.pigeon.po.RaceResultPO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 名次结果仓储适配器（基础设施层）。
 * 阻塞 JDBC 一律经 {@link #blocking} 桥接，不在 event-loop 上直接调 Mapper。
 */
@Repository
public class RaceResultRepositoryImpl implements RaceResultRepository {

    private final RaceResultMapper raceResultMapper;
    private final EntryMapper entryMapper;
    private final BandMapper bandMapper;
    private final ClockingMapper clockingMapper;
    private final TransactionTemplate transactionTemplate;

    public RaceResultRepositoryImpl(RaceResultMapper raceResultMapper,
                                    EntryMapper entryMapper,
                                    BandMapper bandMapper,
                                    ClockingMapper clockingMapper,
                                    PlatformTransactionManager transactionManager) {
        this.raceResultMapper = raceResultMapper;
        this.entryMapper = entryMapper;
        this.bandMapper = bandMapper;
        this.clockingMapper = clockingMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * 重算覆盖：同一事务里先物理清掉本场旧成绩，再整批写入新成绩，以最新一次为准。
     *
     * 用编程式事务（TransactionTemplate）而不是 @Transactional 注解：本方法返回 Mono，
     * 真正的 JDBC 在 boundedElastic 线程上执行，注解代理只会在调用线程上开/关事务，
     * 管不到执行线程；TransactionTemplate 把事务边界划在执行线程内部才有效。
     */
    @Override
    public Mono<Void> replaceForRace(Long raceId, List<RaceResult> results) {
        return this.<Void>blocking(() -> {
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    raceResultMapper.physicalDeleteByRaceId(raceId);
                    for (RaceResult result : results) {
                        RaceResultPO po = RaceResultPoConverter.toPo(result);
                        po.setId(IdUtil.getSnowflakeNextId());
                        raceResultMapper.insert(po);
                    }
                });
            } catch (DuplicateKeyException e) {
                // 并发重算同一场赛时由 uk_race_entry 兜底，事务已回滚，提示重试
                throw new BizException("本场成绩正在重算，请稍后重试");
            }
            return null;
        }).then();
    }

    @Override
    public Mono<PageResult<LeaderboardRow>> pageBoard(Long raceId, int pageNum, int pageSize) {
        return this.<PageResult<LeaderboardRow>>blocking(() -> {
            try {
                PageHelper.startPage(pageNum, pageSize);
                List<RaceResultPO> rows = raceResultMapper.selectList(Wrappers.<RaceResultPO>lambdaQuery()
                        .eq(RaceResultPO::getRaceId, raceId)
                        .orderByAsc(RaceResultPO::getRankNo));
                // 命中分页插件时返回的是 com.github.pagehelper.Page，可直接取总数
                long total = rows instanceof com.github.pagehelper.Page
                        ? ((com.github.pagehelper.Page<?>) rows).getTotal()
                        : rows.size();
                return new PageResult<>(assemble(rows), total, pageNum, pageSize);
            } finally {
                // 分页插件靠 ThreadLocal 传递分页参数，必须清理，否则污染线程池里的下一次调用
                PageHelper.clearPage();
            }
        });
    }

    /**
     * 把一页成绩行与集鸽/足环/报到三张表在内存里装配成名次榜行。
     * 不写自定义 join SQL（本项目约定 MyBatis-Plus 单表查询），按 id 批量取回后内存关联。
     */
    private List<LeaderboardRow> assemble(List<RaceResultPO> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Long> entryIds = rows.stream().map(RaceResultPO::getEntryId).collect(Collectors.toList());
        Map<Long, EntryPO> entries = entryMapper.selectBatchIds(entryIds).stream()
                .collect(Collectors.toMap(EntryPO::getId, Function.identity()));
        List<Long> bandIds = entries.values().stream().map(EntryPO::getBandId).collect(Collectors.toList());
        Map<Long, BandPO> bands = bandIds.isEmpty()
                ? Map.of()
                : bandMapper.selectBatchIds(bandIds).stream()
                        .collect(Collectors.toMap(BandPO::getId, Function.identity()));
        Map<Long, ClockingPO> clockings = clockingMapper.selectList(Wrappers.<ClockingPO>lambdaQuery()
                        .in(ClockingPO::getEntryId, entryIds)).stream()
                .collect(Collectors.toMap(ClockingPO::getEntryId, Function.identity()));
        return rows.stream().map(row -> {
            EntryPO entry = entries.get(row.getEntryId());
            BandPO band = entry == null ? null : bands.get(entry.getBandId());
            ClockingPO clocking = clockings.get(row.getEntryId());
            return new LeaderboardRow(
                    row.getRankNo(),
                    band == null ? null : band.getBandCode(),
                    band == null ? null : band.getOwnerName(),
                    clocking == null ? null : clocking.getClockAt(),
                    row.getSpeedMpm());
        }).collect(Collectors.toList());
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
