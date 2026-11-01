package com.gathin.tvhome.kuato;

final class KuatoSchedule {
    private KuatoSchedule() {}

    /** How long until the next refresh: 0 when the copy is at least one interval old, else what is left of it. */
    static long nextDelayMs(long ageMs, long intervalMs) {
        return ageMs >= intervalMs ? 0 : intervalMs - ageMs;
    }
}
