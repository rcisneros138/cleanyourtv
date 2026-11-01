package com.gathin.tvhome.kuato;

import android.content.Context;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Settings from kuato.json next to config.json. Missing file or bad JSON means everything is off. */
public final class KuatoSettings {
    public final String plexUrl;
    public final String plexToken;
    public final double lat, lon;
    public final boolean streams, recent, weather;

    private KuatoSettings(String url, String token, double lat, double lon,
                          boolean streams, boolean recent, boolean weather) {
        this.plexUrl = url;
        this.plexToken = token;
        this.lat = lat;
        this.lon = lon;
        this.streams = streams;
        this.recent = recent;
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
        String url = o.optString("plex_url", "");
        while (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        boolean hasLoc = o.has("lat") && o.has("lon");
        return new KuatoSettings(url, o.optString("plex_token", ""),
                o.optDouble("lat", 0), o.optDouble("lon", 0),
                o.optBoolean("streams", true), o.optBoolean("recent", true),
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

    public boolean plexReady() {
        return !plexUrl.isEmpty() && !plexToken.isEmpty();
    }
}
