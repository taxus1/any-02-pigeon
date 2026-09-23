package com.somepro.domain.pigeon;

import com.somepro.common.exception.BizException;
import com.somepro.domain.pigeon.model.ClockSource;
import com.somepro.domain.pigeon.model.Clocking;
import com.somepro.domain.pigeon.model.Entry;
import com.somepro.domain.pigeon.model.Race;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 报到领域工厂：来源校验 + 时间窗校验（纯领域）。
 */
class ClockingRegisterTest {

    private final LocalDateTime releaseAt = LocalDateTime.parse("2026-09-23T07:00:00");

    private Race race() {
        Race race = new Race();
        race.setId(99L);
        race.setRaceCode("XF-2026-018");
        race.setReleaseAt(releaseAt);
        race.setCloseAt(releaseAt.plusHours(10));
        race.setDistanceKm(new BigDecimal("300.000"));
        return race;
    }

    private Entry entry() {
        Entry e = new Entry();
        e.setId(7L);
        e.setRaceId(99L);
        e.setBandId(3L);
        return e;
    }

    @Test
    void validScanRegisters() {
        Clocking c = Clocking.register(entry(), race(), releaseAt.plusHours(3), "SCAN");
        assertEquals("SCAN", c.getSource());
        assertEquals(7L, c.getEntryId());
        assertEquals(99L, c.getRaceId());
    }

    @Test
    void manualSourceAcceptedCaseInsensitive() {
        Clocking c = Clocking.register(entry(), race(), releaseAt.plusHours(3), "manual");
        assertEquals("MANUAL", c.getSource());
    }

    @Test
    void unknownSourceRejected() {
        BizException ex = assertThrows(BizException.class,
                () -> Clocking.register(entry(), race(), releaseAt.plusHours(3), "PHONE"));
        assertEquals(true, ex.getMessage().contains("来源非法"));
    }

    @Test
    void earlyClockRejectedAtDomainFactory() {
        BizException ex = assertThrows(BizException.class,
                () -> Clocking.register(entry(), race(), releaseAt.minusMinutes(1), "SCAN"));
        assertEquals(true, ex.getMessage().contains("开笼"));
    }

    @Test
    void lateAfterCloseRejectedAtDomainFactory() {
        BizException ex = assertThrows(BizException.class,
                () -> Clocking.register(entry(), race(), releaseAt.plusHours(11), "SCAN"));
        assertEquals(true, ex.getMessage().contains("关门"));
    }

    @Test
    void sourceEnumParser() {
        assertEquals(ClockSource.SCAN, ClockSource.of(" scan "));
        assertEquals(ClockSource.MANUAL, ClockSource.of("MANUAL"));
        assertEquals(null, ClockSource.of("nope"));
        assertEquals(null, ClockSource.of(null));
    }
}
