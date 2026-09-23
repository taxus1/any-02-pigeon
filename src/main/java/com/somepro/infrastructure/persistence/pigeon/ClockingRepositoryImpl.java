package com.somepro.infrastructure.persistence.pigeon;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.somepro.common.exception.BizException;
import com.somepro.domain.pigeon.model.Clocking;
import com.somepro.domain.pigeon.repository.ClockingRepository;
import com.somepro.infrastructure.persistence.pigeon.converter.ClockingPoConverter;
import com.somepro.infrastructure.persistence.pigeon.po.ClockingPO;
import com.somepro.infrastructure.persistence.pigeon.support.PigeonBlockingRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 归巢报到仓储适配器。
 */
@Repository
public class ClockingRepositoryImpl extends PigeonBlockingRepository implements ClockingRepository {

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
            }
            try {
                clockingMapper.insert(po);
            } catch (DuplicateKeyException e) {
                // 并发报到：应用层的「先查后插」之间被另一笔抢先，uk_entry 兜底
                throw new BizException("该羽赛鸽本场赛的报到已存在（首次归巢为准），重复报到无效");
            }
            return ClockingPoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<Clocking> findByEntryId(Long entryId) {
        return blocking(() -> {
            ClockingPO po = clockingMapper.selectOne(Wrappers.<ClockingPO>lambdaQuery()
                    .eq(ClockingPO::getEntryId, entryId)
                    .last("LIMIT 1"));
            return po == null ? null : ClockingPoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<List<Clocking>> listByRace(Long raceId) {
        return blocking(() -> clockingMapper.selectList(Wrappers.<ClockingPO>lambdaQuery()
                        .eq(ClockingPO::getRaceId, raceId)
                        .orderByAsc(ClockingPO::getClockAt))
                .stream().map(ClockingPoConverter::toDomain).collect(Collectors.toList()));
    }
}
