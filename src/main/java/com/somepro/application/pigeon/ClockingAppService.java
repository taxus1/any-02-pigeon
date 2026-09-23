package com.somepro.application.pigeon;

import com.somepro.common.exception.BizException;
import com.somepro.domain.pigeon.model.Clocking;
import com.somepro.domain.pigeon.model.ClockingSource;
import com.somepro.domain.pigeon.repository.BandRepository;
import com.somepro.domain.pigeon.repository.ClockingRepository;
import com.somepro.domain.pigeon.repository.EntryRepository;
import com.somepro.domain.pigeon.repository.RaceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * 报到录入用例（应用层）：编排「查赛项 → 查足环 → 查集鸽 → 查重 → 落报到」。
 * 业务规则在领域层：时间窗由 Race.assertClockable 把关，一羽一场只认第一次由
 * 这里的查重 + t_clocking.uk_entry 唯一键共同把关。每一类打回都给出明确原因。
 */
@Service
public class ClockingAppService {

    private final RaceRepository raceRepository;
    private final BandRepository bandRepository;
    private final EntryRepository entryRepository;
    private final ClockingRepository clockingRepository;

    public ClockingAppService(RaceRepository raceRepository, BandRepository bandRepository,
                              EntryRepository entryRepository, ClockingRepository clockingRepository) {
        this.raceRepository = raceRepository;
        this.bandRepository = bandRepository;
        this.entryRepository = entryRepository;
        this.clockingRepository = clockingRepository;
    }

    /**
     * 报到录入：集过鸽的鸽子归巢后记一笔。
     * 打回情形（均抛 BizException 说明原因）：赛项/足环不存在、未集鸽、
     * 已报到过、归巢时刻早于开笼或晚于关门、来源非法。
     */
    public Mono<Clocking> register(Long raceId, String bandCode, LocalDateTime clockAt, String sourceRaw) {
        if (raceId == null) {
            return Mono.error(new BizException("赛项 id 不能为空"));
        }
        if (bandCode == null || bandCode.isBlank()) {
            return Mono.error(new BizException("足环号不能为空"));
        }
        // 来源先解析，非法来源不必再查库
        ClockingSource source = ClockingSource.from(sourceRaw);
        String code = bandCode.trim();
        return raceRepository.findById(raceId)
                .switchIfEmpty(Mono.error(() -> new BizException("赛项不存在：raceId=" + raceId)))
                .flatMap(race -> bandRepository.findByBandCode(code)
                        .switchIfEmpty(Mono.error(() -> new BizException("足环号不存在：" + code)))
                        .flatMap(band -> entryRepository.findByRaceIdAndBandId(race.getId(), band.getId())
                                .switchIfEmpty(Mono.error(() -> new BizException("足环 " + code + " 未在本场集鸽，不能报到")))
                                .flatMap(entry -> clockingRepository.findByEntryId(entry.getId())
                                        .flatMap(dup -> Mono.<Clocking>error(
                                                new BizException("该鸽已报到，一羽一场只认第一次归巢")))
                                        .switchIfEmpty(Mono.defer(() -> clockingRepository.save(
                                                Clocking.create(entry, race, clockAt, source)))))));
    }
}
