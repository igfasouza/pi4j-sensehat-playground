///usr/bin/env jbang "$0" "$@" ; exit $?

//JAVA 25

//REPOS mavencentral,mavensnapshot=https://central.sonatype.com/repository/maven-snapshots/

//DEPS com.pi4j:pi4j-core:5.0.0-SNAPSHOT
//DEPS com.pi4j:pi4j-plugin-ffm:5.0.0-SNAPSHOT
//DEPS com.pi4j:pi4j-drivers:1.1.0

import com.pi4j.Pi4J;
import com.pi4j.drivers.hat.raspberry.SenseHat;

import java.util.Random;

public class Fireworks {

    private static final Random RANDOM = new Random();

    private static final int[] COLORS = {
            0xFF0000,
            0x00FF00,
            0x0000FF,
            0xFFFF00,
            0xFF00FF,
            0x00FFFF,
            0xFFFFFF
    };

    public static void main(String[] args) throws Exception {

        var pi4j = Pi4J.newAutoContext();
        var sense = new SenseHat(pi4j);

        sense.clear();

        while (true) {

            int x = RANDOM.nextInt(8);
            int y = 2 + RANDOM.nextInt(4);
            int color = COLORS[RANDOM.nextInt(COLORS.length)];

            // Sobe como um foguete
            for (int rocketY = 7; rocketY >= y; rocketY--) {
                sense.clear();
                sense.setPixel(x, rocketY, 0xFFFFFF);
                Thread.sleep(70);
            }

            // Explosão
            explode(sense, x, y, color);

            Thread.sleep(300);
        }
    }

    private static void explode(SenseHat sense, int x, int y, int color)
            throws InterruptedException {

        for (int radius = 0; radius <= 3; radius++) {

            sense.clear();

            pixel(sense, x, y, color);

            if (radius > 0) {

                pixel(sense, x + radius, y, color);
                pixel(sense, x - radius, y, color);

                pixel(sense, x, y + radius, color);
                pixel(sense, x, y - radius, color);

                pixel(sense, x + radius, y + radius, color);
                pixel(sense, x - radius, y + radius, color);

                pixel(sense, x + radius, y - radius, color);
                pixel(sense, x - radius, y - radius, color);
            }

            Thread.sleep(120);
        }

        // Pequeno fade
        for (int i = 0; i < 2; i++) {
            sense.clear();
            Thread.sleep(80);
        }
    }

    private static void pixel(SenseHat sense, int x, int y, int color) {
        if (x >= 0 && x < 8 && y >= 0 && y < 8) {
            sense.setPixel(x, y, color);
        }
    }
}