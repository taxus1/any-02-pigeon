package com.somepro.domain.pigeon.model;

import com.somepro.common.exception.BizException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 分速公式与名次排序的领域规则测试（纯领域，不起 Spring）。
 */
class RaceResultTest {

    private static final LocalDateTime RELEASE = LocalDateTime.of(2026, 9, 20, 7, 0, 0);

    private Race newRace() {
        Race race = new Race();
        race.setId(1001L);
        race.setReleaseAt(RELEASE);
        race.setDistanceKm(new BigDecimal("500.000"));
        return race;
    }

    private Clocking newClocking(long entryId, LocalDateTime clockAt) {
        Entry entry = new Entry();
        entry.setId(entryId);
        return Clocking.create(entry, newRace(), clockAt, ClockingSource.SCAN);
    }

    @Test
    void speedIsMetersPerMinuteRoundedToTwoDecimals() {
        // 500km / 300min = 1666.666... → 四舍五入 1666.67
        RaceResult result = RaceResult.of(newRace(), newClocking(1L, RELEASE.plusHours(5)));
        assertEquals(new BigDecimal("1666.67"), result.getSpeedMpm());
    }

    @Test
    void speedUsesSecondPrecision() {
        // 开笼后 30 秒归巢：500km × 60000 ÷ 30s = 1,000,000.00 米/分钟
        RaceResult result = RaceResult.of(newRace(), newClocking(1L, RELEASE.plusSeconds(30)));
        assertEquals(new BigDecimal("1000000.00"), result.getSpeedMpm());
    }

    @Test
    void zeroFlightTimeIsRejected() {
        Race race = newRace();
        Clocking clocking = newClocking(1L, RELEASE);
        assertThrows(BizException.class, () -> RaceResult.of(race, clocking));
    }

    @Test
    void ranksAreAssignedBySpeedDescStartingFromOne() {
        Race race = newRace();
        RaceResult slow = RaceResult.of(race, newClocking(1L, RELEASE.plusHours(6)));   // 1388.89
        RaceResult fast = RaceResult.of(race, newClocking(2L, RELEASE.plusHours(4)));   // 2083.33
        RaceResult mid = RaceResult.of(race, newClocking(3L, RELEASE.plusHours(5)));    // 1666.67
        List<RaceResult> results = new ArrayList<>(Arrays.asList(slow, fast, mid));
        RaceResult.assignRanks(results);
        assertEquals(Integer.valueOf(1), fast.getRankNo());
        assertEquals(Integer.valueOf(2), mid.getRankNo());
        assertEquals(Integer.valueOf(3), slow.getRankNo());
        // 名次 1 起连号，且排序后榜序即名次序
        assertEquals(Arrays.asList(1, 2, 3),
                results.stream().map(RaceResult::getRankNo).collect(java.util.stream.Collectors.toList()));
    }

    @Test
    void tiedSpeedsAreBrokenByEntryIdForDeterminism() {
        Race race = newRace();
        RaceResult later = RaceResult.of(race, newClocking(9L, RELEASE.plusHours(5)));
        RaceResult earlier = RaceResult.of(race, newClocking(3L, RELEASE.plusHours(5)));
        List<RaceResult> results = new ArrayList<>(Arrays.asList(later, earlier));
        RaceResult.assignRanks(results);
        assertEquals(Integer.valueOf(1), earlier.getRankNo());
        assertEquals(Integer.valueOf(2), later.getRankNo());
    }
}
