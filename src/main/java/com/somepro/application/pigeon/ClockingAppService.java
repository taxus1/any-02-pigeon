package com.somepro.application.pigeon;

import com.somepro.common.exception.BizException;
import com.somepro.domain.pigeon.model.Band;
import com.somepro.domain.pigeon.model.Clocking;
import com.somepro.domain.pigeon.model.Entry;
import com.somepro.domain.pigeon.model.Race;
import com.somepro.domain.pigeon.repository.BandRepository;
import com.somepro.domain.pigeon.repository.ClockingRepository;
import com.somepro.domain.pigeon.repository.EntryRepository;
import com.somepro.domain.pigeon.repository.RaceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * 归巢报到用例（应用层）。
 *
 * 编排顺序，任何一关不过都抛 {@link BizException} 给出说法，绝不闷头入库：
 * 1. 赛项存在；
 * 2. 足环存在且在赛（ACTIVE）；
 * 3. 这羽鸽这场赛集过鸽；
 * 4. 这场赛这条集鸽还没报过到（一羽一场只认第一次）；
 * 5. 时间窗 + 来源合法性（领域工厂内校验）；
 * 6. 落库（uk_entry 在库侧并发兜底）。
 */
@Service
public class ClockingAppService {

    private final RaceRepository raceRepository;
    private final BandRepository bandRepository;
    private final EntryRepository entryRepository;
    private final ClockingRepository clockingRepository;

    public ClockingAppService(RaceRepository raceRepository,
                              BandRepository bandRepository,
                              EntryRepository entryRepository,
                              ClockingRepository clockingRepository) {
        this.raceRepository = raceRepository;
        this.bandRepository = bandRepository;
        this.entryRepository = entryRepository;
        this.clockingRepository = clockingRepository;
    }

    /**
     * 录一笔归巢报到。
     *
     * @param raceId   赛项 id
     * @param bandCode 足环号（集鸽登记时用的那枚）
     * @param clockAt  归巢时刻
     * @param source   SCAN / MANUAL
     */
    public Mono<Clocking> clockIn(Long raceId, String bandCode, LocalDateTime clockAt, String source) {
        if (raceId == null) {
            return Mono.error(new BizException("赛项 id 不能为空"));
        }
        if (bandCode == null || bandCode.isBlank()) {
            return Mono.error(new BizException("足环号不能为空"));
        }
        if (clockAt == null) {
            return Mono.error(new BizException("归巢时刻不能为空"));
        }

        return raceRepository.findById(raceId)
                .switchIfEmpty(Mono.error(new BizException("赛项不存在：" + raceId)))
                .flatMap(race ->
                        // 2. 足环存在且在赛
                        bandRepository.findByBandCode(bandCode.trim())
                                .switchIfEmpty(Mono.error(new BizException("足环不存在：" + bandCode)))
                                .flatMap(this::requireActiveBand)
                                // 3. 这羽鸽这场赛集过鸽
                                .flatMap(band -> entryRepository.findByRaceAndBand(raceId, band.getId())
                                        .switchIfEmpty(Mono.error(new BizException(
                                                "足环 " + bandCode + " 未参加赛项 " + race.getRaceCode()
                                                        + "，没有集鸽记录，不能报到"))))
                                // 4. 这条集鸽还没报过到（一羽一场只认第一次归巢）
                                .flatMap(entry -> clockingRepository.findByEntryId(entry.getId())
                                        .flatMap(existing -> Mono.<Entry>error(new BizException(
                                                "足环 " + bandCode + " 在本场赛已报过到（首次归巢时刻 "
                                                        + existing.getClockAt() + "），重复报到无效")))
                                        .switchIfEmpty(Mono.just(entry)))
                                // 5/6. 领域工厂校验时间窗与来源，落库（uk_entry 并发兜底）
                                .flatMap(entry -> {
                                    Clocking clocking = Clocking.register(entry, race, clockAt, source);
                                    return clockingRepository.save(clocking);
                                }));
    }

    private Mono<Band> requireActiveBand(Band band) {
        if (!band.isActive()) {
            return Mono.error(new BizException(
                    "足环 " + band.getBandCode() + " 当前状态为 " + band.getStatus() + "，不接受报到"));
        }
        return Mono.just(band);
    }
}
