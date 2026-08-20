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

import java.util.concurrent.ThreadLocalRandom;

// Shake the Sense HAT to roll a dice.
// The accelerometer magnitude is watched for a spike; when it crosses
// SHAKE_THRESHOLD a random number 1..6 is drawn on the 8x8 matrix as
// classic dice pips. The face stays on for DISPLAY_MS and then the
// matrix clears, ready for the next roll.
public class Dice {

    private static final double SHAKE_THRESHOLD = 18.0;
    private static final long DISPLAY_MS = 5000;

    // Pip slots in a 3x3 grid, each stored as the (row, col) top-left
    // corner of a 2x2 block inside the 8x8 matrix.
    private static final int[][] PIP = {
            {1, 1}, {1, 3}, {1, 5},
            {3, 1}, {3, 3}, {3, 5},
            {5, 1}, {5, 3}, {5, 5},
    };

    // Which pip indexes are lit for each face 1..6.
    private static final int[][] FACES = {
            {},
            {4},                 // 1: center
            {0, 8},              // 2: top-left, bottom-right
            {0, 4, 8},           // 3: top-left, center, bottom-right
            {0, 2, 6, 8},        // 4: four corners
            {0, 2, 4, 6, 8},     // 5: four corners + center
            {0, 2, 3, 5, 6, 8},  // 6: two columns of three
    };

    public static void main(String[] args) throws InterruptedException {
        int pipColor = Argb32.fromRgb(255, 255, 255);

        Context pi4j = Pi4J.newAutoContext();
        try (SenseHat hat = new SenseHat(pi4j)) {
            hat.clear();
            System.out.println("Shake the Sense HAT to roll the dice.");
            while (true) {
                if (isShaken(hat)) {
                    int face = ThreadLocalRandom.current().nextInt(1, 7);
                    System.out.println("Rolled: " + face);
                    drawFace(hat, face, pipColor);
                    Thread.sleep(DISPLAY_MS);
                    hat.clear();
                }
                Thread.sleep(50);
            }
        } finally {
            pi4j.shutdown();
        }
    }

    private static boolean isShaken(SenseHat hat) {
        double[] a = hat.getAccelerometerRaw();
        double magnitude = Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]);
        return magnitude > SHAKE_THRESHOLD;
    }

    private static void drawFace(SenseHat hat, int face, int color) {
        hat.clear();
        for (int index : FACES[face]) {
            int row = PIP[index][0];
            int col = PIP[index][1];
            hat.setPixel(col,     row,     color);
            hat.setPixel(col + 1, row,     color);
            hat.setPixel(col,     row + 1, color);
            hat.setPixel(col + 1, row + 1, color);
        }
    }
}
