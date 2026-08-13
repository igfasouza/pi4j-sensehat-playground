///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25
//DEPS com.pi4j:pi4j-core:4.0.1
//DEPS com.pi4j:pi4j-plugin-ffm:4.0.1
//DEPS com.pi4j:pi4j-drivers-igfasouza:1.1.1-SNAPSHOT
//REPOS mavencentral,mavenlocal,sonatype-snapshots=https://s01.oss.sonatype.org/content/repositories/snapshots/

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.drivers.hat.raspberry.SenseHat;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

// Generic long-running sensor logger for the Sense HAT.
//
// Samples every enabled sensor on a fixed interval, writes CSV rows to a file, and
// stops after a total duration. Press the joystick to drop a MARK row into the log
// (handy for tagging events like "cloud cover", "shooting star", "door opened").
//
//   jbang Logger.java \
//       --label "Perseid Meteor Shower 2026" \
//       --interval 30s \
//       --duration 6h \
//       --output perseids.csv
//
// Flags (all optional, sensible defaults):
//   --label     free-form run label, written into a "# label: ..." header line
//   --interval  sample interval (e.g. 500ms, 30s, 2m, 1h). Default: 1s
//   --duration  total run time (same syntax). Default: run forever
//   --output    output CSV file. Default: stdout
public class Logger {

    private static final Object PRINT_LOCK = new Object();
    private static final AtomicReference<Sample> LATEST = new AtomicReference<>();

    record Sample(Instant timestamp,
                  Double tempHts, Double tempLps,
                  Double humidity, Double pressure,
                  double[] colour) {}

    record Config(String label, Duration interval, Duration duration, Path output) {}

    public static void main(String[] args) throws Exception {
        Config cfg = parseArgs(args);

        Context pi4j = Pi4J.newAutoContext();
        try (SenseHat hat = new SenseHat(pi4j);
             PrintWriter out = openOutput(cfg.output())) {

            boolean hasColour = hat.getVersion() == SenseHat.Version.V2;

            writeHeader(out, cfg, hasColour);

            hat.addJoystickListener(event -> {
                if (event.action() != SenseHat.Action.PRESSED) return;
                Sample s = LATEST.get();
                writeRow(out, s != null ? s : emptySample(hasColour), "MARK", hasColour);
                System.err.println("marked at " + Instant.now());
            });

            Instant start = Instant.now();
            Instant deadline = cfg.duration() == null ? null : start.plus(cfg.duration());
            long intervalMs = cfg.interval().toMillis();

            System.err.printf("logging \"%s\" every %s%s to %s%n",
                    cfg.label(),
                    human(cfg.interval()),
                    cfg.duration() == null ? "" : " for " + human(cfg.duration()),
                    cfg.output() == null ? "stdout" : cfg.output());

            while (deadline == null || Instant.now().isBefore(deadline)) {
                Sample sample = read(hat, hasColour);
                LATEST.set(sample);
                writeRow(out, sample, "", hasColour);
                Thread.sleep(intervalMs);
            }

            System.err.println("done after " + human(Duration.between(start, Instant.now())));
        } finally {
            pi4j.shutdown();
        }
    }

    private static Sample read(SenseHat hat, boolean hasColour) {
        return new Sample(
                Instant.now(),
                safe(hat::getTemperatureFromHumidity),
                safe(hat::getTemperatureFromPressure),
                safe(hat::getHumidity),
                safe(hat::getPressure),
                hasColour ? hat.getColour() : null);
    }

    private static Sample emptySample(boolean hasColour) {
        return new Sample(Instant.now(), null, null, null, null,
                hasColour ? new double[]{0, 0, 0, 0} : null);
    }

    private static Double safe(java.util.function.DoubleSupplier s) {
        try { return s.getAsDouble(); } catch (Exception e) { return null; }
    }

    private static void writeHeader(PrintWriter out, Config cfg, boolean hasColour) {
        synchronized (PRINT_LOCK) {
            out.println("# label: " + cfg.label());
            out.println("# interval: " + human(cfg.interval()));
            out.println("# started: " + Instant.now());
            StringBuilder h = new StringBuilder("timestamp,temp_hts,temp_lps,humidity,pressure");
            if (hasColour) h.append(",clear,red,green,blue");
            h.append(",marker");
            out.println(h);
            out.flush();
        }
    }

    private static void writeRow(PrintWriter out, Sample s, String marker, boolean hasColour) {
        synchronized (PRINT_LOCK) {
            StringBuilder row = new StringBuilder();
            row.append(s.timestamp())
               .append(',').append(fmt(s.tempHts(), 2))
               .append(',').append(fmt(s.tempLps(), 2))
               .append(',').append(fmt(s.humidity(), 2))
               .append(',').append(fmt(s.pressure(), 2));
            if (hasColour) {
                double[] c = s.colour();
                row.append(',').append(fmt(c[0], 0))
                   .append(',').append(fmt(c[1], 0))
                   .append(',').append(fmt(c[2], 0))
                   .append(',').append(fmt(c[3], 0));
            }
            row.append(',').append(marker);
            out.println(row);
            out.flush();
        }
    }

    private static String fmt(Double v, int decimals) {
        return v == null ? "" : String.format("%." + decimals + "f", v);
    }

    private static String fmt(double v, int decimals) {
        return String.format("%." + decimals + "f", v);
    }

    private static PrintWriter openOutput(Path path) throws IOException {
        if (path == null) return new PrintWriter(System.out, true);
        return new PrintWriter(Files.newBufferedWriter(path), true);
    }

    private static Config parseArgs(String[] args) {
        String label = "sensor log";
        Duration interval = Duration.ofSeconds(1);
        Duration duration = null;
        Path output = null;

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            String v = i + 1 < args.length ? args[i + 1] : null;
            switch (a) {
                case "--label"    -> { label = require(a, v);          i++; }
                case "--interval" -> { interval = parseDuration(require(a, v)); i++; }
                case "--duration" -> { duration = parseDuration(require(a, v)); i++; }
                case "--output"   -> { output = Path.of(require(a, v)); i++; }
                case "-h", "--help" -> { printUsage(); System.exit(0); }
                default -> {
                    System.err.println("unknown argument: " + a);
                    printUsage();
                    System.exit(2);
                }
            }
        }
        return new Config(label, interval, duration, output);
    }

    private static String require(String flag, String value) {
        if (value == null) {
            System.err.println("missing value for " + flag);
            System.exit(2);
        }
        return value;
    }

    // Accepts "500ms", "30s", "2m", "6h" or ISO-8601 like "PT6H".
    private static Duration parseDuration(String s) {
        String t = s.trim().toLowerCase();
        try {
            if (t.endsWith("ms")) return Duration.ofMillis(Long.parseLong(t.substring(0, t.length() - 2)));
            if (t.endsWith("s"))  return Duration.ofSeconds(Long.parseLong(t.substring(0, t.length() - 1)));
            if (t.endsWith("m"))  return Duration.ofMinutes(Long.parseLong(t.substring(0, t.length() - 1)));
            if (t.endsWith("h"))  return Duration.ofHours(Long.parseLong(t.substring(0, t.length() - 1)));
            return Duration.parse(t);
        } catch (Exception e) {
            throw new IllegalArgumentException("bad duration: " + s + " (use 500ms, 30s, 2m, 6h)");
        }
    }

    private static String human(Duration d) {
        long ms = d.toMillis();
        if (ms % 3_600_000 == 0) return d.toHours() + "h";
        if (ms % 60_000 == 0)    return d.toMinutes() + "m";
        if (ms % 1_000 == 0)     return d.toSeconds() + "s";
        return ms + "ms";
    }

    private static void printUsage() {
        System.err.println("""
            Usage: jbang Logger.java [options]

              --label     <text>      Run label written into the CSV header
              --interval  <duration>  Sample interval (e.g. 500ms, 30s, 2m, 6h). Default: 1s
              --duration  <duration>  Total run time. Default: forever
              --output    <path>      CSV output file. Default: stdout

            Press the joystick to drop a MARK row into the log.
            """);
    }
}
