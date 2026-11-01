package com.gathin.tvhome.kuato;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileWriter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class KuatoCacheTest {
    private static final long H = 3_600_000L;
    @Rule public TemporaryFolder tmp = new TemporaryFolder();

    @Test public void roundTrip() throws Exception {
        KuatoCache c = new KuatoCache(new File(tmp.getRoot(), "sub"));
        c.put("weather", "{\"a\":1}\nline2", 1000);
        assertEquals("{\"a\":1}\nline2", c.get("weather", 2000, 5000));
        assertEquals(1000, c.ageMs("weather", 2000));
    }

    @Test public void missingKey() {
        KuatoCache c = new KuatoCache(tmp.getRoot());
        assertNull(c.get("weather", 0, 5000));
        assertEquals(Long.MAX_VALUE, c.ageMs("weather", 0));
    }

    @Test public void tooOldIsNull() {
        KuatoCache c = new KuatoCache(tmp.getRoot());
        c.put("weather", "x", 0);
        assertNull(c.get("weather", 6000, 5000));
    }

    @Test public void twentyFourHourRule() {
        KuatoCache c = new KuatoCache(tmp.getRoot());
        c.put("weather", "x", 0);
        assertEquals("x", c.get("weather", 23 * H, 86_400_000L));
        assertNull(c.get("weather", 25 * H, 86_400_000L));
    }

    @Test public void corruptFileIsNull() throws Exception {
        KuatoCache c = new KuatoCache(tmp.getRoot());
        try (FileWriter w = new FileWriter(new File(tmp.getRoot(), "weather"))) { w.write("garbage"); }
        assertNull(c.get("weather", 0, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, c.ageMs("weather", 0));
    }

    @Test public void overwriteReplaces() {
        KuatoCache c = new KuatoCache(tmp.getRoot());
        c.put("weather", "old", 0);
        c.put("weather", "new", 10);
        assertEquals("new", c.get("weather", 10, 5000));
    }
}
