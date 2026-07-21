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

public class EmojiLoop {

    static final int o = Argb32.BLACK;
    static final int Y = Argb32.fromRgb(255, 220,   0);
    static final int K = Argb32.fromRgb(  0,   0,   0);
    static final int R = Argb32.fromRgb(220,   0,   0);
    static final int W = Argb32.fromRgb(255, 255, 255);
    static final int G = Argb32.fromRgb(  0, 200,   0);
    static final int B = Argb32.fromRgb(  0, 120, 255);

    static final int[] SMILEY = {
        o, o, Y, Y, Y, Y, o, o,
        o, Y, Y, Y, Y, Y, Y, o,
        Y, Y, K, Y, Y, K, Y, Y,
        Y, Y, K, Y, Y, K, Y, Y,
        Y, Y, Y, Y, Y, Y, Y, Y,
        Y, K, Y, Y, Y, Y, K, Y,
        o, Y, K, K, K, K, Y, o,
        o, o, Y, Y, Y, Y, o, o,
    };

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

    static final int[] SKULL = {
        o, o, W, W, W, W, o, o,
        o, W, W, W, W, W, W, o,
        W, W, K, W, W, K, W, W,
        W, W, K, W, W, K, W, W,
        W, W, W, K, K, W, W, W,
        o, W, W, W, W, W, W, o,
        o, W, K, W, K, W, K, o,
        o, o, W, W, W, W, o, o,
    };

    static final int[] SPACE_INVADER = {
        o, o, G, o, o, o, G, o,
        o, o, o, G, o, G, o, o,
        o, o, G, G, G, G, G, o,
        o, G, G, K, G, K, G, G,
        G, G, G, G, G, G, G, G,
        G, o, G, G, G, G, G, o,
        G, o, G, o, o, o, G, o,
        o, o, o, G, G, o, o, o,
    };

    static final int[] ARROW_UP = {
        o, o, o, B, B, o, o, o,
        o, o, B, B, B, B, o, o,
        o, B, B, B, B, B, B, o,
        B, B, B, B, B, B, B, B,
        o, o, B, B, B, B, o, o,
        o, o, B, B, B, B, o, o,
        o, o, B, B, B, B, o, o,
        o, o, B, B, B, B, o, o,
    };

    public static void main(String[] args) throws InterruptedException {
        int[][] emojis = { SMILEY, HEART, SKULL, SPACE_INVADER, ARROW_UP };
        String[] names = { "smiley", "heart", "skull", "invader", "arrow" };

        Context pi4j = Pi4J.newAutoContext();
        try (SenseHat hat = new SenseHat(pi4j)) {
            while (true) {
                for (int i = 0; i < emojis.length; i++) {
                    System.out.println("showing: " + names[i]);
                    hat.setPixels(emojis[i]);
                    Thread.sleep(3000);
                }
            }
        } finally {
            pi4j.shutdown();
        }
    }
}
