package com.somepro.domain.pigeon;

import com.somepro.domain.pigeon.model.Clocking;
import com.somepro.domain.pigeon.model.Race;
import com.somepro.domain.pigeon.model.RaceResult;
import com.somepro.domain.pigeon.service.RaceResultDomainService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 分速计算与名次连号（纯领域服务，不起 Spring）。
 */
class RaceResultDomainServiceTest {

    private final RaceResultDomainService service = new RaceResultDomainService();
    private final LocalDateTime releaseAt = LocalDateTime.parse("2026-09-23T07:00:00");

    private Race race(String distance) {
        Race race = new Race();
        race.setId(99L);
        race.setRaceCode("XF-2026-018");
        race.setReleaseAt(releaseAt);
        race.setDistanceKm(new BigDecimal(distance));
        return race;
    }

    private Clocking clocking(long entryId, LocalDateTime clockAt) {
        Clocking c = new Clocking();
        c.setEntryId(entryId);
        c.setRaceId(99L);
        c.setClockAt(clockAt);
        c.setSource("SCAN");
        return c;
    }

    @Test
    void speedFormulaMatchesMetersPerMinuteRoundedHalfUp() {
        // 300km，飞了 300 分钟（5 小时）→ 300*1000/300 = 1000.00 米/分钟
        BigDecimal speed = RaceResult.calcSpeed(
                new BigDecimal("300.000"),
                releaseAt,
                releaseAt.plusHours(5));
        assertEquals(new BigDecimal("1000.00"), speed);
    }

    @Test
    void speedUsesExactSecondsNotWholeMinutes() {
        // 120km 飞 60 分钟 → 2000.00；飞 61 分钟 → 1967.21
        BigDecimal at60 = RaceResult.calcSpeed(new BigDecimal("120.000"), releaseAt,
                releaseAt.plusMinutes(60));
        assertEquals(new BigDecimal("2000.00"), at60);

        BigDecimal at61 = RaceResult.calcSpeed(new BigDecimal("120.000"), releaseAt,
                releaseAt.plusMinutes(61));
        assertEquals(new BigDecimal("1967.21"), at61);

        // 相差 30 秒也要在分速上体现（不抹整分钟）
        BigDecimal at6030 = RaceResult.calcSpeed(new BigDecimal("120.000"), releaseAt,
                releaseAt.plusMinutes(60).plusSeconds(30));
        assertEquals(0, at6030.compareTo(new BigDecimal("1983.47")));
    }

    @Test
    void ranksAreContiguousFromOneAndSortedBySpeedDesc() {
        Race race = race("300.000");
        // entry 1：5 小时；entry 2：4 小时（更快）；entry 3：6 小时
        List<Clocking> clockings = List.of(
                clocking(1L, releaseAt.plusHours(5)),
                clocking(2L, releaseAt.plusHours(4)),
                clocking(3L, releaseAt.plusHours(6)));

        List<RaceResult> results = service.compute(race, clockings);

        assertEquals(3, results.size());
        assertEquals(2L, results.get(0).getEntryId());
        assertEquals(1, results.get(0).getRankNo());
        assertEquals(1L, results.get(1).getEntryId());
        assertEquals(2, results.get(1).getRankNo());
        assertEquals(3L, results.get(2).getEntryId());
        assertEquals(3, results.get(2).getRankNo());
        // 最快者：300km / 240 分钟 = 1250.00
        assertEquals(new BigDecimal("1250.00"), results.get(0).getSpeedMpm());
    }

    @Test
    void emptyClockingsProduceEmptyResults() {
        List<RaceResult> results = service.compute(race("300.000"), List.of());
        assertEquals(0, results.size());
    }

    @Test
    void sameSpeedSameSecondOrderedByEntryId() {
        // 同空距、同归巢时刻（同一秒扫描到两羽）→ 完全同分速，entryId 小者靠前，名次仍连号
        LocalDateTime clockAt = releaseAt.plusHours(5);
        Race race = race("300.000");
        List<Clocking> clockings = List.of(
                clocking(20L, clockAt),
                clocking(10L, clockAt));

        List<RaceResult> results = service.compute(race, clockings);
        assertEquals(10L, results.get(0).getEntryId());
        assertEquals(1, results.get(0).getRankNo());
        assertEquals(20L, results.get(1).getEntryId());
        assertEquals(2, results.get(1).getRankNo());
        assertEquals(results.get(0).getSpeedMpm(), results.get(1).getSpeedMpm());
    }
}
