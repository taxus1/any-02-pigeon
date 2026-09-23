package com.somepro.domain.pigeon.model;

import com.somepro.common.exception.BizException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 报到时间窗与来源解析的领域规则测试（纯领域，不起 Spring）。
 */
class ClockingTest {

    private static final LocalDateTime RELEASE = LocalDateTime.of(2026, 9, 20, 7, 0, 0);
    private static final LocalDateTime CLOSE = LocalDateTime.of(2026, 9, 20, 18, 0, 0);

    private Race newRace() {
        Race race = new Race();
        race.setId(1001L);
        race.setReleaseAt(RELEASE);
        race.setCloseAt(CLOSE);
        race.setDistanceKm(new BigDecimal("500.000"));
        return race;
    }

    private Entry newEntry(long id) {
        Entry entry = new Entry();
        entry.setId(id);
        return entry;
    }

    @Test
    void clockAtBeforeReleaseIsRejected() {
        Race race = newRace();
        Entry entry = newEntry(1L);
        assertThrows(BizException.class,
                () -> Clocking.create(entry, race, RELEASE.minusSeconds(1), ClockingSource.SCAN));
    }

    @Test
    void clockAtAfterCloseIsRejected() {
        Race race = newRace();
        Entry entry = newEntry(1L);
        assertThrows(BizException.class,
                () -> Clocking.create(entry, race, CLOSE.plusSeconds(1), ClockingSource.SCAN));
    }

    @Test
    void clockAtWithinWindowIsAccepted() {
        Clocking clocking = Clocking.create(newEntry(1L), newRace(), RELEASE.plusHours(3), ClockingSource.MANUAL);
        assertEquals(Long.valueOf(1L), clocking.getEntryId());
        assertEquals(Long.valueOf(1001L), clocking.getRaceId());
        assertEquals(ClockingSource.MANUAL, clocking.getSource());
    }

    @Test
    void nullCloseAtMeansNoDeadline() {
        Race race = newRace();
        race.setCloseAt(null);
        Clocking clocking = Clocking.create(newEntry(1L), race, RELEASE.plusDays(2), ClockingSource.SCAN);
        assertEquals(RELEASE.plusDays(2), clocking.getClockAt());
    }

    @Test
    void sourceParsingIsLenientOnCaseAndBlank() {
        assertEquals(ClockingSource.SCAN, ClockingSource.from("scan"));
        assertEquals(ClockingSource.MANUAL, ClockingSource.from(" MANUAL "));
    }

    @Test
    void unknownOrBlankSourceIsRejected() {
        assertThrows(BizException.class, () -> ClockingSource.from("PIGEON"));
        assertThrows(BizException.class, () -> ClockingSource.from(null));
        assertThrows(BizException.class, () -> ClockingSource.from("  "));
    }
}
