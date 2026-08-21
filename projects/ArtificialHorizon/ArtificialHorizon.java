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

public class ArtificialHorizon {

    static final int SKY = Argb32.fromRgb(0, 80, 255);
    static final int GROUND = Argb32.fromRgb(139, 69, 19);

    static final double PITCH_PIXELS_PER_DEGREE = 0.15;
    static final double MAX_SLOPE = 6.0;
    static final int FRAME_MS = 50;

    public static void main(String[] args) throws InterruptedException {
        Context pi4j = Pi4J.newAutoContext();
        try (SenseHat hat = new SenseHat(pi4j)) {
            hat.setImuConfig(true, true, true);
            hat.clear();
            while (true) {
                double[] deg = hat.getOrientationDegrees();
                double pitch = signed(deg[0]);
                double roll = signed(deg[1]);

                double slope = clamp(Math.tan(Math.toRadians(roll)), -MAX_SLOPE, MAX_SLOPE);
                double pitchOffset = pitch * PITCH_PIXELS_PER_DEGREE;

                int[] frame = new int[64];
                for (int x = 0; x < 8; x++) {
                    double horizon = 3.5 + pitchOffset + slope * (x - 3.5);
                    for (int y = 0; y < 8; y++) {
                        frame[y * 8 + x] = (y < horizon) ? SKY : GROUND;
                    }
                }
                hat.setPixels(frame);
                Thread.sleep(FRAME_MS);
            }
        } finally {
            pi4j.shutdown();
        }
    }

    private static double signed(double deg) {
        double d = deg % 360.0;
        if (d > 180.0) d -= 360.0;
        if (d < -180.0) d += 360.0;
        return d;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
