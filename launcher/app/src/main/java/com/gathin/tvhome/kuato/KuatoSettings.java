package com.gathin.tvhome.kuato;

import android.content.Context;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Settings from kuato.json next to config.json. Missing file or bad JSON means everything is off.
 * A future Plex token must live in internal storage, never in kuato.json (external storage is readable by other apps).
 */
public final class KuatoSettings {
    public final double lat, lon;
    public final boolean weather;

    private KuatoSettings(double lat, double lon, boolean weather) {
        this.lat = lat;
        this.lon = lon;
        this.weather = weather;
    }

    /** No Android types here so it runs in plain JVM tests. */
    public static KuatoSettings parse(String json) {
        JSONObject o;
        try {
            o = new JSONObject(json);
        } catch (Exception e) {
            o = new JSONObject();
        }
        boolean hasLoc = o.has("lat") && o.has("lon");
        return new KuatoSettings(o.optDouble("lat", 0), o.optDouble("lon", 0),
                hasLoc && o.optBoolean("weather", true));
    }

    /** Reads the file; call off the main thread. Never throws. */
    public static KuatoSettings load(Context ctx) {
        try (InputStream in = new FileInputStream(new File(ctx.getExternalFilesDir(null), "kuato.json"))) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            for (int n; (n = in.read(buf)) > 0; ) out.write(buf, 0, n);
            return parse(out.toString(StandardCharsets.UTF_8.name()));
        } catch (Exception e) {
            return parse("{}");
        }
    }
}
