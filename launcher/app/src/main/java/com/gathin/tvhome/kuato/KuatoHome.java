package com.gathin.tvhome.kuato;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.gathin.tvhome.R;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The weather line under Home's date: "54° · high 61, low 45" with an icon. Refreshes every 30 min while Home is
 * resumed, cache first; hidden when there is nothing under 24 h old or the last fetch failed. Network and file work
 * run on one background thread; any RuntimeException turns the line off until the process restarts.
 */
public final class KuatoHome {
    private static final String TAG = "Kuato";
    private static final String KEY = "weather";
    private static final long INTERVAL_MS = 30 * 60_000L;
    private static final long MAX_AGE_MS = 24 * 3_600_000L;

    /** Static: a recreated Home stays off too, until the next process start. */
    private static volatile boolean disabled;

    private final Context ctx;
    private final View root;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ScheduledThreadPoolExecutor bg = new ScheduledThreadPoolExecutor(1, r -> {
        Thread t = new Thread(r, "kuato");
        t.setDaemon(true);
        return t;
    });
    private TextView line, date, lunarDate, stats;
    private KuatoCache cache;           // background thread only

    private boolean resumed;            // main thread only
    private volatile int gen;           // bumped on resume and pause; work from an older one is dropped
    private volatile ScheduledFuture<?> next;
    private boolean wanted;             // the last result says show the line
    private final int[] rootXY = new int[2], xy = new int[2];

    public KuatoHome(Activity a, View root) {
        this.ctx = a.getApplicationContext();
        this.root = root;
        bg.setRemoveOnCancelPolicy(true);
        bg.setKeepAliveTime(10, TimeUnit.SECONDS);   // an old Home's idle thread goes away after recreate()
        bg.allowCoreThreadTimeOut(true);
        guard(() -> {
            line = root.findViewById(R.id.kuatoWeather);
            date = root.findViewById(R.id.date);
            lunarDate = root.findViewById(R.id.lunarDate);
            stats = root.findViewById(R.id.stats);
            line.setFocusable(false);
            root.getViewTreeObserver().addOnGlobalLayoutListener(() -> guard(this::place));
        });
    }

    public void onResume() {
        guard(() -> {
            resumed = true;
            int g = ++gen;
            bg.execute(() -> guard(() -> tick(g)));
        });
    }

    public void onPause() {
        resumed = false;
        gen++;
        ScheduledFuture<?> f = next;
        if (f != null) f.cancel(false);
        main.removeCallbacksAndMessages(null);
    }

    // ---- background ------------------------------------------------------------------------------

    private void tick(int g) {
        if (g != gen || disabled) return;
        KuatoSettings s = KuatoSettings.load(ctx);
        if (!s.weather) {
            post(g, null, false);   // settings are read again on the next resume
            return;
        }
        if (cache == null) cache = new KuatoCache(new File(ctx.getCacheDir(), "kuato"));
        long now = System.currentTimeMillis();
        Weather w = Weather.parse(cache.get(KEY, now, MAX_AGE_MS));
        long delay = KuatoSchedule.nextDelayMs(cache.ageMs(KEY, now), INTERVAL_MS);
        if (delay > 0) {
            post(g, w, false);
            schedule(g, delay);
            return;
        }
        if (w != null) post(g, w, false);   // cache first, then the fresh copy
        boolean failed = false;
        try {
            String json = Http.get(Weather.url(s.lat, s.lon), Collections.emptyMap());
            Weather fresh = Weather.parse(json);
            if (fresh == null) throw new IOException("bad weather JSON");
            cache.put(KEY, json, System.currentTimeMillis());
            w = fresh;
        } catch (IOException e) {
            failed = true;
            Log.w(TAG, "weather fetch failed: " + e);
        }
        post(g, w, failed);
        schedule(g, INTERVAL_MS);
    }

    private void schedule(int g, long delayMs) {
        if (g == gen) next = bg.schedule(() -> guard(() -> tick(g)), delayMs, TimeUnit.MILLISECONDS);
    }

    private void post(int g, Weather w, boolean failed) {
        main.post(() -> guard(() -> {
            if (resumed && g == gen) show(w, failed);
        }));
    }

    // ---- main thread -----------------------------------------------------------------------------

    private void show(Weather w, boolean failed) {
        wanted = showWeather(w, failed);
        if (wanted) {
            // The date's look, read now: Home may have recoloured or resized it (header_white, shamsi_date).
            float px = date.getTextSize();
            line.setTextSize(TypedValue.COMPLEX_UNIT_PX, px);
            line.setTextColor(date.getTextColors());
            line.setTypeface(date.getTypeface());
            line.setCompoundDrawableTintList(date.getTextColors());
            Drawable icon = ctx.getDrawable(icon(condition(w.code))).mutate();
            int size = Math.round(px * 1.15f);
            icon.setBounds(0, 0, size, size);
            line.setCompoundDrawables(icon, null, null, null);
            line.setCompoundDrawablePadding(Math.round(px * 0.45f));
            line.setText(text(w));
            line.setContentDescription(description(w));
        }
        place();
    }

    /** Under the date (or the lunar date under it), same left edge; under the stats line when that is shown. */
    private void place() {
        if (disabled || line == null) return;
        boolean visible = wanted && date.isShown();
        if (visible) {
            root.getLocationInWindow(rootXY);
            View anchor = lunarDate != null && lunarDate.getVisibility() == View.VISIBLE ? lunarDate : date;
            anchor.getLocationInWindow(xy);
            float gap = 4 * root.getResources().getDisplayMetrics().density;
            float y = xy[1] - rootXY[1] + anchor.getHeight() + gap;
            if (stats != null && stats.getVisibility() == View.VISIBLE) {
                // The stats line sits just below the clock block and is long enough to reach under the date.
                stats.getLocationInWindow(xy);
                y = Math.max(y, xy[1] - rootXY[1] + stats.getHeight() + gap);
            }
            date.getLocationInWindow(xy);
            float x = xy[0] - rootXY[0] + date.getPaddingLeft();
            if (line.getX() != x) line.setX(x);
            if (line.getY() != y) line.setY(y);
        }
        int v = visible ? View.VISIBLE : View.GONE;
        if (line.getVisibility() != v) line.setVisibility(v);
    }

    /** Runs r; a RuntimeException turns the weather line off until the process restarts. One log line. */
    private void guard(Runnable r) {
        if (disabled) return;
        try {
            r.run();
        } catch (RuntimeException e) {
            disabled = true;
            Log.w(TAG, "weather line off until restart: " + e);
            main.post(() -> {
                try {
                    if (line != null) line.setVisibility(View.GONE);
                } catch (RuntimeException ignored) {
                    // already off
                }
            });
        }
    }

    // ---- pure helpers (JVM tests) ----------------------------------------------------------------

    static boolean showWeather(Weather w, boolean failed) {
        return w != null && !failed;
    }

    /** WMO weather code to one of: clear, cloudy, rain, snow, storm. Unknown codes are cloudy. */
    static String condition(int code) {
        if (code == 0 || code == 1) return "clear";
        if ((code >= 51 && code <= 67) || (code >= 80 && code <= 82)) return "rain";
        if ((code >= 71 && code <= 77) || code == 85 || code == 86) return "snow";
        if (code >= 95 && code <= 99) return "storm";
        return "cloudy";   // 2, 3, 45 and 48 (fog), anything else
    }

    static String text(Weather w) {
        return w.tempF + "° · high " + w.highF + ", low " + w.lowF;
    }

    static String description(Weather w) {
        return w.tempF + " degrees, " + condition(w.code) + ", high " + w.highF + ", low " + w.lowF;
    }

    private static int icon(String condition) {
        switch (condition) {
            case "clear": return R.drawable.kuato_wx_clear;
            case "rain": return R.drawable.kuato_wx_rain;
            case "snow": return R.drawable.kuato_wx_snow;
            case "storm": return R.drawable.kuato_wx_storm;
            default: return R.drawable.kuato_wx_cloudy;
        }
    }
}
