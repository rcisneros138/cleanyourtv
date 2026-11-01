package com.gathin.tvhome.kuato;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class KuatoHomeTest {
    private static Weather w(double t, double hi, double lo, int code) {
        return Weather.parse("{\"current\":{\"temperature_2m\":" + t + ",\"weather_code\":" + code + "},"
                + "\"daily\":{\"temperature_2m_max\":[" + hi + "],\"temperature_2m_min\":[" + lo + "]}}");
    }

    @Test public void visibility() {
        assertTrue(KuatoHome.showWeather(w(54, 61, 45, 3), false));
        assertFalse(KuatoHome.showWeather(w(54, 61, 45, 3), true));
        assertFalse(KuatoHome.showWeather(null, false));
        assertFalse(KuatoHome.showWeather(null, true));
    }

    @Test public void conditions() {
        int[][] cases = {{0, 1}, {2, 3, 45, 48}, {51, 55, 57, 61, 67, 80, 82}, {71, 77, 85, 86}, {95, 96, 99}};
        String[] names = {"clear", "cloudy", "rain", "snow", "storm"};
        for (int i = 0; i < cases.length; i++) {
            for (int code : cases[i]) assertEquals("code " + code, names[i], KuatoHome.condition(code));
        }
        assertEquals("cloudy", KuatoHome.condition(-1)); // unknown code: neutral icon
        assertEquals("cloudy", KuatoHome.condition(100));
    }

    @Test public void text() {
        Weather x = w(53.6, 61.2, 44.5, 3);
        assertEquals("54° · high 61, low 45", KuatoHome.text(x));
        assertEquals("54 degrees, cloudy, high 61, low 45", KuatoHome.description(x));
        assertEquals("-3° · high 2, low -9", KuatoHome.text(w(-3, 2, -9, 71)));
    }
}
