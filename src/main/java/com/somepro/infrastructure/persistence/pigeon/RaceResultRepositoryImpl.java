package com.somepro.infrastructure.persistence.pigeon;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.somepro.domain.pigeon.model.RaceResult;
import com.somepro.domain.pigeon.model.RankRow;
import com.somepro.domain.pigeon.repository.RaceResultRepository;
import com.somepro.domain.shared.model.PageResult;
import com.somepro.infrastructure.persistence.pigeon.converter.RaceResultPoConverter;
import com.somepro.infrastructure.persistence.pigeon.po.RaceResultPO;
import com.somepro.infrastructure.persistence.pigeon.po.RankRowPO;
import com.somepro.infrastructure.persistence.pigeon.support.PigeonBlockingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 名次结果仓储适配器。
 *
 * 重算覆盖（{@link #replaceForRace}）的关键：
 * - 用 {@link TransactionTemplate} 把「物理删旧 + 插新」包成一个事务，
 *   中间任何一步失败整段回滚，库里不会出现半套成绩；
 * - 删除走 {@code physicalDeleteByRace} 的手写 DELETE（不走 @TableLogic），
 *   因为 uk_race_entry 不含 del_flag，逻辑删行会占住唯一键。
 */
@Repository
public class RaceResultRepositoryImpl extends PigeonBlockingRepository implements RaceResultRepository {

    private final RaceResultMapper raceResultMapper;
    private final TransactionTemplate transactionTemplate;

    public RaceResultRepositoryImpl(RaceResultMapper raceResultMapper,
                                    PlatformTransactionManager transactionManager) {
        this.raceResultMapper = raceResultMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public Mono<List<RaceResult>> replaceForRace(Long raceId, List<RaceResult> fresh) {
        return blocking(() -> transactionTemplate.execute(status -> {
            // 1. 物理清掉本场赛旧成绩（重算以最新一次为准，绝不留两套）
            raceResultMapper.physicalDeleteByRace(raceId);

            // 2. 逐条插入最新成绩（名次已在领域服务按分速排好、连号）
            List<RaceResult> inserted = fresh.stream()
                    .map(domain -> {
                        RaceResultPO po = RaceResultPoConverter.toPo(domain);
                        po.setId(IdUtil.getSnowflakeNextId());
                        raceResultMapper.insert(po);
                        return RaceResultPoConverter.toDomain(po);
                    })
                    .collect(Collectors.toList());
            return inserted;
        }));
    }

    @Override
    public Mono<List<RaceResult>> listByRace(Long raceId) {
        return blocking(() -> raceResultMapper.selectList(Wrappers.<RaceResultPO>lambdaQuery()
                        .eq(RaceResultPO::getRaceId, raceId)
                        .orderByAsc(RaceResultPO::getRankNo))
                .stream().map(RaceResultPoConverter::toDomain).collect(Collectors.toList()));
    }

    @Override
    public Mono<PageResult<RankRow>> pageRank(Long raceId, int pageNum, int pageSize) {
        return this.<PageResult<RankRow>>blocking(() -> {
            try {
                PageHelper.startPage(pageNum, pageSize);
                List<RankRowPO> rows = raceResultMapper.selectRankRows(raceId);
                long total = rows instanceof com.github.pagehelper.Page
                        ? ((com.github.pagehelper.Page<?>) rows).getTotal()
                        : rows.size();
                List<RankRow> content = rows.stream()
                        .map(RaceResultPoConverter::toRankRow)
                        .collect(Collectors.toList());
                return new PageResult<>(content, total, pageNum, pageSize);
            } finally {
                // PageHelper 靠 ThreadLocal 传参，boundedElastic 线程复用，必须清理
                PageHelper.clearPage();
            }
        });
    }
}
