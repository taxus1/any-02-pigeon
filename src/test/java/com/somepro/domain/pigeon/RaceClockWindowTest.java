package com.somepro.domain.pigeon;

import com.somepro.domain.pigeon.model.Race;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 赛项报到时间窗校验（纯领域，不起 Spring）。
 */
class RaceClockWindowTest {

    private Race race(LocalDateTime releaseAt, LocalDateTime closeAt) {
        Race race = new Race();
        race.setId(1L);
        race.setRaceCode("XF-2026-018");
        race.setReleaseAt(releaseAt);
        race.setCloseAt(closeAt);
        race.setDistanceKm(new BigDecimal("300.000"));
        return race;
    }

    @Test
    void normalClockWithinWindowPasses() {
        Race r = race(LocalDateTime.parse("2026-09-23T07:00:00"),
                LocalDateTime.parse("2026-09-23T18:00:00"));
        assertNull(r.checkClockWindow(LocalDateTime.parse("2026-09-23T12:00:00")));
    }

    @Test
    void clockBeforeReleaseRejected() {
        Race r = race(LocalDateTime.parse("2026-09-23T07:00:00"), null);
        String err = r.checkClockWindow(LocalDateTime.parse("2026-09-23T06:59:59"));
        assertTrue(err.contains("开笼"));
    }

    @Test
    void clockExactlyAtReleaseRejected() {
        Race r = race(LocalDateTime.parse("2026-09-23T07:00:00"), null);
        assertTrue(r.checkClockWindow(LocalDateTime.parse("2026-09-23T07:00:00")).contains("开笼"));
    }

    @Test
    void clockExactlyAtCloseIsAcceptedAfterCloseRejected() {
        LocalDateTime close = LocalDateTime.parse("2026-09-23T18:00:00");
        Race r = race(LocalDateTime.parse("2026-09-23T07:00:00"), close);
        // 踩点关门算有效
        assertNull(r.checkClockWindow(close));
        // 过一秒不认
        assertTrue(r.checkClockWindow(close.plusSeconds(1)).contains("关门"));
    }

    @Test
    void nullClockRejected() {
        Race r = race(LocalDateTime.parse("2026-09-23T07:00:00"), null);
        assertEquals("归巢时刻不能为空", r.checkClockWindow(null));
    }

    @Test
    void noCloseMeansNoUpperLimit() {
        Race r = race(LocalDateTime.parse("2026-09-23T07:00:00"), null);
        assertNull(r.checkClockWindow(LocalDateTime.parse("2026-09-30T08:00:00")));
    }
}
