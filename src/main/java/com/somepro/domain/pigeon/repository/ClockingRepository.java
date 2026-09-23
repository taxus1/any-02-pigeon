package com.somepro.domain.pigeon.repository;

import com.somepro.domain.pigeon.model.Clocking;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 归巢报到仓储端口。
 */
public interface ClockingRepository {

    /**
     * 保存一条报到。t_clocking.uk_entry 在库侧兜底「一条集鸽只认一次归巢」，
     * 并发下撞唯一键由适配器翻译成业务异常。
     */
    Mono<Clocking> save(Clocking clocking);

    /** 按集鸽 id 查已有报到（去重判断）；没有返回空信号。 */
    Mono<Clocking> findByEntryId(Long entryId);

    /** 查某场赛全部有效报到（算分速用）。 */
    Mono<List<Clocking>> listByRace(Long raceId);
}
