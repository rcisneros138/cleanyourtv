package com.gathin.tvhome.kuato;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class KuatoSettingsTest {
    @Test public void locationEnablesWeather() {
        KuatoSettings s = KuatoSettings.parse("{\"lat\":41.88,\"lon\":-87.63}");
        assertTrue(s.weather);
        assertEquals(41.88, s.lat, 0.0001);
        assertEquals(-87.63, s.lon, 0.0001);
    }

    @Test public void badOrEmptyIsOff() {
        for (String j : new String[] {"not json", "{}", null}) {
            assertFalse(KuatoSettings.parse(j).weather);
        }
    }
}
