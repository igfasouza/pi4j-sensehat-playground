///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25
//DEPS com.pi4j:pi4j-core:4.0.1
//DEPS com.pi4j:pi4j-plugin-ffm:4.0.1
//DEPS com.pi4j:pi4j-drivers:1.1.1-SNAPSHOT
//REPOS mavencentral,mavenlocal,sonatype-snapshots=https://s01.oss.sonatype.org/content/repositories/snapshots/

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.drivers.display.graphics.Argb32;
import com.pi4j.drivers.hat.raspberry.SenseHat;
import com.pi4j.drivers.input.GameController;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

// Logs light + temperature + humidity + pressure once per second as CSV on stdout,
// shows a live "light meter" bar on the 8x8 LED matrix, and lets you tag key eclipse
// moments with the joystick — the tag appears in the "marker" column so you can align
// the curve during post-processing.
//
//   jbang Eclipse.java > eclipse.csv
//
// Joystick tags:
//   UP     -> C1  (first contact — Moon touches Sun's edge)
//   RIGHT  -> C2  (totality begins)
//   LEFT   -> C3  (totality ends)
//   DOWN   -> C4  (last contact)
//   CENTER -> MARK (free-form observation)
public class Eclipse {

    private static final long SAMPLE_INTERVAL_MS = 1000;
    private static final double CLEAR_FULL_SCALE = 65535.0;

    private static final Object PRINT_LOCK = new Object();
    private static final AtomicReference<Sample> LATEST = new AtomicReference<>();

    record Sample(Instant timestamp,
                  double clear, double red, double green, double blue,
                  double tempHts, double tempLps,
                  double humidity, double pressure) {}

    public static void main(String[] args) throws InterruptedException {
        Context pi4j = Pi4J.newAutoContext();
        try (SenseHat hat = new SenseHat(pi4j)) {
            if (hat.getVersion() != SenseHat.Version.V2) {
                System.err.println("This demo needs a Sense HAT v2 (TCS3400 colour sensor).");
                return;
            }

            hat.clear();
            hat.addJoystickListener(event -> {
                if (event.action() != SenseHat.Action.PRESSED) return;
                String tag = tagFor(event.key());
                if (tag == null) return;
                Sample s = LATEST.get();
                if (s != null) {
                    printRow(s, tag);
                } else {
                    printRow(new Sample(Instant.now(), 0, 0, 0, 0, 0, 0, 0, 0), tag);
                }
                System.err.println("tagged: " + tag);
            });

            synchronized (PRINT_LOCK) {
                System.out.println("timestamp,clear,red,green,blue,temp_hts,temp_lps,humidity,pressure,marker");
            }

            while (true) {
                double[] crgb = hat.getColour();
                Sample sample = new Sample(
                        Instant.now(),
                        crgb[0], crgb[1], crgb[2], crgb[3],
                        hat.getTemperatureFromHumidity(),
                        hat.getTemperatureFromPressure(),
                        hat.getHumidity(),
                        hat.getPressure());
                LATEST.set(sample);
                printRow(sample, "");
                drawBar(hat, sample.clear() / CLEAR_FULL_SCALE);
                Thread.sleep(SAMPLE_INTERVAL_MS);
            }
        } finally {
            pi4j.shutdown();
        }
    }

    private static String tagFor(GameController.Key key) {
        return switch (key) {
            case UP     -> "C1";
            case RIGHT  -> "C2";
            case LEFT   -> "C3";
            case DOWN   -> "C4";
            case CENTER -> "MARK";
            default     -> null;
        };
    }

    private static void printRow(Sample s, String marker) {
        synchronized (PRINT_LOCK) {
            System.out.printf("%s,%.0f,%.0f,%.0f,%.0f,%.2f,%.2f,%.2f,%.2f,%s%n",
                    s.timestamp(),
                    s.clear(), s.red(), s.green(), s.blue(),
                    s.tempHts(), s.tempLps(),
                    s.humidity(), s.pressure(),
                    marker);
        }
    }

    // Vertical bar (bottom-up): height = light level, colour fades yellow -> red as it darkens.
    private static void drawBar(SenseHat hat, double level) {
        level = Math.max(0.0, Math.min(1.0, level));
        int litRows = (int) Math.round(level * 8);
        if (litRows == 0 && level > 0) litRows = 1;

        int color = Argb32.fromRgb(255, (int) Math.round(255 * level), 0);

        hat.clear();
        for (int row = 0; row < litRows; row++) {
            int y = 7 - row;
            for (int x = 0; x < 8; x++) {
                hat.setPixel(x, y, color);
            }
        }
    }
}
