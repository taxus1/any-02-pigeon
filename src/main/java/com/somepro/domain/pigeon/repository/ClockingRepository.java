package com.somepro.domain.pigeon.repository;

import com.somepro.domain.pigeon.model.Clocking;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 归巢报到的仓储端口。
 */
public interface ClockingRepository {

    Mono<Clocking> save(Clocking clocking);

    /** 按集鸽登记查报到（一羽一场只认第一次，查到即视为重复）。 */
    Mono<Clocking> findByEntryId(Long entryId);

    /** 一场赛的全部有效归巢，供算分速出名次。 */
    Mono<List<Clocking>> findByRaceId(Long raceId);
}
