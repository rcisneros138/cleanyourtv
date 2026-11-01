package com.gathin.tvhome.kuato;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class KuatoScheduleTest {
    @Test public void freshWaitsTheRest() {
        assertEquals(60_000, KuatoSchedule.nextDelayMs(0, 60_000));
        assertEquals(15_000, KuatoSchedule.nextDelayMs(45_000, 60_000));
    }

    @Test public void dueNow() {
        assertEquals(0, KuatoSchedule.nextDelayMs(61_000, 60_000));
        assertEquals(0, KuatoSchedule.nextDelayMs(60_000, 60_000));
        assertEquals(0, KuatoSchedule.nextDelayMs(Long.MAX_VALUE, 60_000)); // missing or future-stamped cache
    }
}
