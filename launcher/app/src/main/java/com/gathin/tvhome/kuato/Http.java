package com.gathin.tvhome.kuato;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

final class Http {
    private Http() {}

    /** GET as UTF-8 text. Any non-2xx status throws IOException. */
    static String get(String url, Map<String, String> headers) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        try {
            c.setConnectTimeout(3000);
            c.setReadTimeout(5000);
            for (Map.Entry<String, String> h : headers.entrySet()) c.setRequestProperty(h.getKey(), h.getValue());
            int code = c.getResponseCode();
            if (code < 200 || code > 299) throw new IOException("HTTP " + code);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (InputStream in = c.getInputStream()) {
                byte[] buf = new byte[4096];
                for (int n; (n = in.read(buf)) > 0; ) out.write(buf, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } finally {
            c.disconnect();
        }
    }
}
