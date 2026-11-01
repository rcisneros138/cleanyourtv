package com.gathin.tvhome.kuato;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** One file per key: first line = write time in ms, rest = text. Writes are temp file + rename. */
final class KuatoCache {
    private final File dir;

    KuatoCache(File dir) { this.dir = dir; }

    void put(String key, String text, long nowMs) {
        try {
            dir.mkdirs();
            File tmp = new File(dir, key + ".tmp");
            try (FileOutputStream o = new FileOutputStream(tmp)) {
                o.write((nowMs + "\n" + text).getBytes(StandardCharsets.UTF_8));
            }
            if (!tmp.renameTo(new File(dir, key))) tmp.delete();
        } catch (Exception ignored) {
            // cache is best effort
        }
    }

    String get(String key, long nowMs, long maxAgeMs) {
        String[] r = read(key);
        if (r == null) return null;
        long age = nowMs - Long.parseLong(r[0]);
        return age >= 0 && age <= maxAgeMs ? r[1] : null; // age < 0: clock went backwards
    }

    long ageMs(String key, long nowMs) {
        String[] r = read(key);
        if (r == null) return Long.MAX_VALUE;
        long age = nowMs - Long.parseLong(r[0]);
        return age < 0 ? Long.MAX_VALUE : age;
    }

    /** {writeTime, text} or null on any problem. */
    private String[] read(String key) {
        try {
            String s = new String(Files.readAllBytes(new File(dir, key).toPath()), StandardCharsets.UTF_8);
            int nl = s.indexOf('\n');
            if (nl < 0) return null;
            Long.parseLong(s.substring(0, nl));
            return new String[] {s.substring(0, nl), s.substring(nl + 1)};
        } catch (Exception e) {
            return null;
        }
    }
}
