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

public class Cube {

    static final int RED     = Argb32.fromRgb(255,   0,   0);
    static final int GREEN   = Argb32.fromRgb(  0, 255,   0);
    static final int BLUE    = Argb32.fromRgb(  0,  80, 255);
    static final int YELLOW  = Argb32.fromRgb(255, 255,   0);
    static final int MAGENTA = Argb32.fromRgb(255,   0, 255);
    static final int CYAN    = Argb32.fromRgb(  0, 255, 255);
    static final int EDGE    = Argb32.fromRgb( 20,  20,  20);

    static final int[] FACES = { RED, GREEN, BLUE, YELLOW, MAGENTA, CYAN };

    static final int FACE_SIZE = 5;
    static final int FRAME_MS = 60;

    public static void main(String[] args) throws InterruptedException {
        Context pi4j = Pi4J.newAutoContext();
        try (SenseHat hat = new SenseHat(pi4j)) {
            hat.setImuConfig(true, true, true);
            hat.clear();
            while (true) {
                double[] a = hat.getAccelerometerRaw();
                int face = dominantFace(a[0], a[1], a[2]);
                hat.setPixels(drawFace(FACES[face]));
                Thread.sleep(FRAME_MS);
            }
        } finally {
            pi4j.shutdown();
        }
    }

    private static int dominantFace(double x, double y, double z) {
        double ax = Math.abs(x), ay = Math.abs(y), az = Math.abs(z);
        if (ax >= ay && ax >= az) return x >= 0 ? 0 : 1;
        if (ay >= ax && ay >= az) return y >= 0 ? 2 : 3;
        return z >= 0 ? 4 : 5;
    }

    private static int[] drawFace(int color) {
        int[] frame = new int[64];
        int start = (8 - FACE_SIZE) / 2;
        int end = start + FACE_SIZE;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                boolean inside = x >= start && x < end && y >= start && y < end;
                boolean border = inside && (x == start || x == end - 1 || y == start || y == end - 1);
                frame[y * 8 + x] = inside ? (border ? EDGE : color) : Argb32.BLACK;
            }
        }
        return frame;
    }
}
