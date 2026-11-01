package com.gathin.tvhome.kuato;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class KuatoSettingsTest {
    @Test public void fullConfig() {
        KuatoSettings s = KuatoSettings.parse(
                "{\"plex_url\":\"http://192.168.86.48:32400/\",\"plex_token\":\"t\",\"lat\":41.88,\"lon\":-87.63}");
        assertEquals("http://192.168.86.48:32400", s.plexUrl);
        assertEquals("t", s.plexToken);
        assertTrue(s.streams && s.recent && s.weather);
        assertTrue(s.plexReady());
        assertEquals(41.88, s.lat, 0.0001);
        assertEquals(-87.63, s.lon, 0.0001);
    }

    @Test public void badOrEmptyIsOff() {
        for (String j : new String[] {"not json", "{}", null}) {
            KuatoSettings s = KuatoSettings.parse(j);
            assertFalse(s.plexReady());
            assertFalse(s.weather);
        }
    }

    @Test public void featureFlagHonoured() {
        KuatoSettings s = KuatoSettings.parse(
                "{\"plex_url\":\"u\",\"plex_token\":\"t\",\"lat\":1,\"lon\":2,\"streams\":false}");
        assertFalse(s.streams);
        assertTrue(s.recent);
    }
}
