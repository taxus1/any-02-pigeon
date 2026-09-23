package com.somepro.infrastructure.persistence.pigeon;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.somepro.domain.pigeon.model.Band;
import com.somepro.domain.pigeon.repository.BandRepository;
import com.somepro.infrastructure.persistence.pigeon.converter.BandPoConverter;
import com.somepro.infrastructure.persistence.pigeon.po.BandPO;
import com.somepro.infrastructure.persistence.pigeon.support.PigeonBlockingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * 足环档案仓储适配器。
 */
@Repository
public class BandRepositoryImpl extends PigeonBlockingRepository implements BandRepository {

    private final BandMapper bandMapper;

    public BandRepositoryImpl(BandMapper bandMapper) {
        this.bandMapper = bandMapper;
    }

    @Override
    public Mono<Band> findByBandCode(String bandCode) {
        return blocking(() -> {
            BandPO po = bandMapper.selectOne(Wrappers.<BandPO>lambdaQuery()
                    .eq(BandPO::getBandCode, bandCode)
                    .last("LIMIT 1"));
            return po == null ? null : BandPoConverter.toDomain(po);
        });
    }
}
