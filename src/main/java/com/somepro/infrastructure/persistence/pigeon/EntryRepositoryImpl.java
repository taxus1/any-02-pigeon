package com.somepro.infrastructure.persistence.pigeon;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.somepro.domain.pigeon.model.Entry;
import com.somepro.domain.pigeon.repository.EntryRepository;
import com.somepro.infrastructure.persistence.pigeon.converter.EntryPoConverter;
import com.somepro.infrastructure.persistence.pigeon.po.EntryPO;
import com.somepro.infrastructure.persistence.pigeon.support.PigeonBlockingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * 集鸽登记仓储适配器。
 */
@Repository
public class EntryRepositoryImpl extends PigeonBlockingRepository implements EntryRepository {

    private final EntryMapper entryMapper;

    public EntryRepositoryImpl(EntryMapper entryMapper) {
        this.entryMapper = entryMapper;
    }

    @Override
    public Mono<Entry> findByRaceAndBand(Long raceId, Long bandId) {
        return blocking(() -> {
            EntryPO po = entryMapper.selectOne(Wrappers.<EntryPO>lambdaQuery()
                    .eq(EntryPO::getRaceId, raceId)
                    .eq(EntryPO::getBandId, bandId)
                    .last("LIMIT 1"));
            return po == null ? null : EntryPoConverter.toDomain(po);
        });
    }

    @Override
    public Mono<Entry> findById(Long id) {
        return blocking(() -> {
            EntryPO po = entryMapper.selectById(id);
            return po == null ? null : EntryPoConverter.toDomain(po);
        });
    }
}
