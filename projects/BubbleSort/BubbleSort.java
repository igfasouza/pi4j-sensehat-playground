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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

// Visualises the Bubble Sort algorithm on the Sense HAT 8x8 matrix.
// Each of the 8 columns is one element in the array: the column index
// is the position and the column height is the value (1..8). The two
// elements currently being compared flash yellow, a swap flashes red,
// and columns that are already in their final sorted position turn
// green. Once everything is green the array is shuffled and the sort
// starts again.
public class BubbleSort {

    private static final int SIZE = 8;

    private static final int BLUE   = Argb32.fromRgb(  0, 100, 255); // unsorted bar
    private static final int YELLOW = Argb32.fromRgb(255, 200,   0); // being compared
    private static final int RED    = Argb32.fromRgb(220,   0,   0); // swap in progress
    private static final int GREEN  = Argb32.fromRgb(  0, 180,   0); // sorted
    private static final int OFF    = Argb32.fromRgb(  0,   0,   0);

    private static final long COMPARE_MS = 350;
    private static final long SWAP_MS    = 350;
    private static final long PAUSE_MS   = 1500;

    public static void main(String[] args) throws InterruptedException {
        Context pi4j = Pi4J.newAutoContext();
        try (SenseHat hat = new SenseHat(pi4j)) {
            hat.clear();
            while (true) {
                int[] values = shuffled();
                boolean[] sorted = new boolean[SIZE];
                draw(hat, values, sorted, -1, -1, false);
                Thread.sleep(PAUSE_MS);

                bubbleSort(hat, values, sorted);

                // Everything green — hold the finished frame.
                Thread.sleep(PAUSE_MS * 2);
            }
        } finally {
            pi4j.shutdown();
        }
    }

    private static void bubbleSort(SenseHat hat, int[] a, boolean[] sorted)
            throws InterruptedException {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                // Highlight the pair being compared in yellow.
                draw(hat, a, sorted, j, j + 1, false);
                Thread.sleep(COMPARE_MS);

                if (a[j] > a[j + 1]) {
                    // Flash red before actually swapping.
                    draw(hat, a, sorted, j, j + 1, true);
                    Thread.sleep(SWAP_MS);

                    int tmp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = tmp;

                    draw(hat, a, sorted, j, j + 1, true);
                    Thread.sleep(SWAP_MS);
                }
            }
            // The largest unsorted element has bubbled to the right edge.
            sorted[n - 1 - i] = true;
            draw(hat, a, sorted, -1, -1, false);
        }
        // First column is sorted by elimination once the rest are done.
        sorted[0] = true;
        draw(hat, a, sorted, -1, -1, false);
    }

    // Renders the array as columns. compareLeft/compareRight are the two
    // indexes currently under the comparator (yellow), or turn red when
    // swapping is true. Columns marked in `sorted` are drawn green.
    private static void draw(SenseHat hat, int[] a, boolean[] sorted,
                             int compareLeft, int compareRight, boolean swapping) {
        int[] pixels = new int[64];
        Arrays.fill(pixels, OFF);
        for (int col = 0; col < SIZE; col++) {
            int color;
            if (sorted[col]) {
                color = GREEN;
            } else if (col == compareLeft || col == compareRight) {
                color = swapping ? RED : YELLOW;
            } else {
                color = BLUE;
            }
            int height = a[col];
            for (int k = 0; k < height; k++) {
                int row = 7 - k; // draw from the bottom row upwards
                pixels[row * 8 + col] = color;
            }
        }
        hat.setPixels(pixels);
    }

    private static int[] shuffled() {
        Integer[] boxed = new Integer[SIZE];
        for (int i = 0; i < SIZE; i++) boxed[i] = i + 1;
        List<Integer> list = Arrays.asList(boxed);
        Collections.shuffle(list, ThreadLocalRandom.current());
        int[] out = new int[SIZE];
        for (int i = 0; i < SIZE; i++) out[i] = boxed[i];
        return out;
    }
}
