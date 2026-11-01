package com.gathin.tvhome.kuato;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class WeatherTest {
    @Test public void parse() throws Exception {
        String json;
        try (InputStream in = getClass().getResourceAsStream("/kuato/open-meteo.json")) {
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        Weather w = Weather.parse(json);
        assertNotNull(w);
        assertEquals(57, w.tempF);
        assertEquals(62, w.highF);
        assertEquals(47, w.lowF);
        assertEquals(3, w.code);
    }

    @Test public void malformed() {
        assertNull(Weather.parse(null));
        assertNull(Weather.parse(""));
        assertNull(Weather.parse("not json"));
        assertNull(Weather.parse("{}"));
        assertNull(Weather.parse("{\"current\":{\"temperature_2m\":50},\"daily\":{}}"));
    }

    @Test public void url() {
        assertEquals("https://api.open-meteo.com/v1/forecast?latitude=43.04&longitude=-87.91"
                + "&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min"
                + "&temperature_unit=fahrenheit&timezone=auto&forecast_days=1", Weather.url(43.04, -87.91));
    }
}
