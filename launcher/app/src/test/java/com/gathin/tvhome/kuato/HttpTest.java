package com.gathin.tvhome.kuato;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.junit.Test;

/** Tiny one-shot HTTP server on a ServerSocket (com.sun.net.httpserver is hidden by the Android compile classpath). */
public class HttpTest {
    private static ServerSocket serve(String status, String body) throws IOException {
        ServerSocket ss = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
        Thread t = new Thread(() -> {
            try (Socket s = ss.accept()) {
                s.getInputStream().read(new byte[4096]);
                byte[] b = body.getBytes(StandardCharsets.UTF_8);
                OutputStream o = s.getOutputStream();
                o.write(("HTTP/1.1 " + status + "\r\nContent-Length: " + b.length + "\r\nConnection: close\r\n\r\n")
                        .getBytes(StandardCharsets.US_ASCII));
                o.write(b);
                o.flush();
            } catch (IOException ignored) {
            }
        });
        t.setDaemon(true);
        t.start();
        return ss;
    }

    @Test public void ok() throws Exception {
        try (ServerSocket ss = serve("200 OK", "héllo")) {
            assertEquals("héllo", Http.get("http://127.0.0.1:" + ss.getLocalPort() + "/", Collections.emptyMap()));
        }
    }

    @Test public void non2xxIsFailure() throws Exception {
        try (ServerSocket ss = serve("401 Unauthorized", "no")) {
            Http.get("http://127.0.0.1:" + ss.getLocalPort() + "/", Collections.emptyMap());
            fail("expected IOException");
        } catch (IOException expected) {
        }
    }
}
