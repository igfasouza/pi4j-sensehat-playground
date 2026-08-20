///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25
//DEPS com.pi4j:pi4j-core:4.0.1
//DEPS com.pi4j:pi4j-plugin-ffm:4.0.1
//DEPS com.pi4j:pi4j-drivers-igfasouza:1.1.1-SNAPSHOT
//REPOS mavencentral,mavenlocal,sonatype-snapshots=https://s01.oss.sonatype.org/content/repositories/snapshots/

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.drivers.display.graphics.Argb32;
import com.pi4j.drivers.hat.raspberry.SenseHat;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

// Turns the Sense HAT 8x8 matrix into a network-controlled status light.
// A tiny embedded HTTP server accepts a colour name and floods the matrix
// with that colour. Meant as a simple "build passed / warning / broken"
// or "free / busy / do not disturb" indicator.
//
//   curl -X PUT http://<pi>:8080/state -d red
//   curl -X PUT http://<pi>:8080/state -d yellow
//   curl -X PUT http://<pi>:8080/state -d green
//   curl -X PUT http://<pi>:8080/state -d off
//   curl        http://<pi>:8080/state    # -> "green"
//
// Flags:
//   --port <n>   Port to bind. Default: 8080
public class Semaphore {

    private static final Map<String, int[]> COLOURS = Map.of(
            "red",    rgb(220,   0,   0),
            "yellow", rgb(220, 180,   0),
            "green",  rgb(  0, 180,   0),
            "off",    rgb(  0,   0,   0));

    private static final AtomicReference<String> STATE = new AtomicReference<>("off");

    public static void main(String[] args) throws IOException {
        int port = parsePort(args);

        Context pi4j = Pi4J.newAutoContext();
        try (SenseHat hat = new SenseHat(pi4j)) {
            hat.clear();

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/state", exchange -> {
                try (exchange) {
                    String method = exchange.getRequestMethod();
                    if ("GET".equalsIgnoreCase(method)) {
                        respond(exchange, 200, STATE.get() + "\n");
                        return;
                    }
                    if ("PUT".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method)) {
                        String body = new String(exchange.getRequestBody().readAllBytes(),
                                StandardCharsets.UTF_8).trim().toLowerCase(Locale.ROOT);
                        int[] colour = COLOURS.get(body);
                        if (colour == null) {
                            respond(exchange, 400,
                                    "unknown colour: " + body + " (use red, yellow, green, off)\n");
                            return;
                        }
                        fill(hat, colour);
                        STATE.set(body);
                        respond(exchange, 200, body + "\n");
                        return;
                    }
                    exchange.getResponseHeaders().add("Allow", "GET, PUT, POST");
                    respond(exchange, 405, "method not allowed\n");
                }
            });
            server.start();
            System.out.printf("semaphore listening on http://0.0.0.0:%d/state%n", port);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                server.stop(0);
                hat.clear();
                pi4j.shutdown();
            }));

            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void fill(SenseHat hat, int[] rgb) {
        int argb = Argb32.fromRgb(rgb[0], rgb[1], rgb[2]);
        int[] pixels = new int[64];
        for (int i = 0; i < pixels.length; i++) pixels[i] = argb;
        hat.setPixels(pixels);
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, int status, String body)
            throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static int[] rgb(int r, int g, int b) { return new int[]{r, g, b}; }

    private static int parsePort(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("--port".equals(args[i]) && i + 1 < args.length) {
                try {
                    return Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.err.println("bad port: " + args[i + 1]);
                    System.exit(2);
                }
            }
            if ("-h".equals(args[i]) || "--help".equals(args[i])) {
                System.err.println("Usage: jbang Semaphore.java [--port <n>]");
                System.exit(0);
            }
        }
        return 8080;
    }
}
