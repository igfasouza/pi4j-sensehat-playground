///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25
//DEPS com.pi4j:pi4j-core:4.0.1
//DEPS com.pi4j:pi4j-plugin-ffm:4.0.1
//DEPS com.pi4j:pi4j-drivers-igfasouza:1.1.1-SNAPSHOT
//REPOS mavencentral,mavenlocal,sonatype-snapshots=https://s01.oss.sonatype.org/content/repositories/snapshots/

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.drivers.display.graphics.Argb32;
import com.pi4j.drivers.display.graphics.GraphicsTextAnimator.ScrollDirection;
import com.pi4j.drivers.hat.raspberry.SenseHat;

// Turns the Sense HAT into a tiny wall projector. A LEGO magnifier lens
// held a few centimetres above the LED matrix focuses the 8x8 image onto
// a nearby wall — but a lens flips the image left-to-right, so every
// frame is pre-mirrored on the X axis before being sent to the display.
// That is the entire trick: draw the frame, apply Mirror.X, push.
//
// Sequence:
//   heart (red)     -> 2s
//   happy face (Y)  -> 2s
//   sad face (blue) -> 2s
//   "Hello PI4J"    -> fast scroll
//   arrow up (grn)  -> 2s
//   arrow down (M)  -> 2s
public class Projector {

    static final int o = Argb32.BLACK;
    static final int R = Argb32.fromRgb(220,   0,   0); // heart
    static final int Y = Argb32.fromRgb(255, 220,   0); // happy
    static final int B = Argb32.fromRgb(  0, 120, 255); // sad
    static final int G = Argb32.fromRgb(  0, 200,   0); // arrow up
    static final int M = Argb32.fromRgb(220,   0, 200); // arrow down
    static final int T = Argb32.fromRgb(  0, 220, 220); // scrolling text

    static final long FRAME_MS  = 2000;
    static final long SCROLL_MS = 40;

    static final int[] HEART = {
        o, R, R, o, o, R, R, o,
        R, R, R, R, R, R, R, R,
        R, R, R, R, R, R, R, R,
        R, R, R, R, R, R, R, R,
        o, R, R, R, R, R, R, o,
        o, o, R, R, R, R, o, o,
        o, o, o, R, R, o, o, o,
        o, o, o, o, o, o, o, o,
    };

    static final int[] HAPPY = {
        o, o, Y, Y, Y, Y, o, o,
        o, Y, Y, Y, Y, Y, Y, o,
        Y, Y, o, Y, Y, o, Y, Y,
        Y, Y, o, Y, Y, o, Y, Y,
        Y, Y, Y, Y, Y, Y, Y, Y,
        Y, o, Y, Y, Y, Y, o, Y,
        o, Y, o, o, o, o, Y, o,
        o, o, Y, Y, Y, Y, o, o,
    };

    static final int[] SAD = {
        o, o, B, B, B, B, o, o,
        o, B, B, B, B, B, B, o,
        B, B, o, B, B, o, B, B,
        B, B, o, B, B, o, B, B,
        B, B, B, B, B, B, B, B,
        B, B, B, o, o, B, B, B,
        o, B, o, o, o, o, B, o,
        o, o, B, B, B, B, o, o,
    };

    static final int[] ARROW_UP = {
        o, o, o, G, G, o, o, o,
        o, o, G, G, G, G, o, o,
        o, G, G, G, G, G, G, o,
        G, G, G, G, G, G, G, G,
        o, o, G, G, G, G, o, o,
        o, o, G, G, G, G, o, o,
        o, o, G, G, G, G, o, o,
        o, o, G, G, G, G, o, o,
    };

    static final int[] ARROW_DOWN = {
        o, o, M, M, M, M, o, o,
        o, o, M, M, M, M, o, o,
        o, o, M, M, M, M, o, o,
        o, o, M, M, M, M, o, o,
        M, M, M, M, M, M, M, M,
        o, M, M, M, M, M, M, o,
        o, o, M, M, M, M, o, o,
        o, o, o, M, M, o, o, o,
    };

    public static void main(String[] args) throws InterruptedException {
        Context pi4j = Pi4J.newAutoContext();
        try (SenseHat hat = new SenseHat(pi4j)) {
            hat.clear();
            while (true) {
                showFrame(hat, HEART);
                showFrame(hat, HAPPY);
                showFrame(hat, SAD);

                // Scroll direction is reversed on purpose: the lens flips
                // the image on the X axis, so LEFT_TO_RIGHT scrolling on
                // the panel becomes a natural right-to-left read on the
                // wall.
                hat.showMessage("Hello PI4J", SCROLL_MS, T, o, ScrollDirection.LEFT_TO_RIGHT);

                showFrame(hat, ARROW_UP);
                showFrame(hat, ARROW_DOWN);
            }
        } finally {
            pi4j.shutdown();
        }
    }

    // Push a static frame, apply Mirror.X (so the lens un-flips it into
    // the correct orientation on the wall) and hold for FRAME_MS.
    private static void showFrame(SenseHat hat, int[] frame) throws InterruptedException {
        hat.setPixels(mirrorX(frame));
        Thread.sleep(FRAME_MS);
    }

    // Reverse each row of the 8x8 buffer — this is exactly what the
    // GraphicsDisplay.Mirror.X option does inside the driver, expressed
    // here in-line so the trick is visible.
    private static int[] mirrorX(int[] frame) {
        int[] out = new int[64];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                out[row * 8 + col] = frame[row * 8 + (7 - col)];
            }
        }
        return out;
    }
}
