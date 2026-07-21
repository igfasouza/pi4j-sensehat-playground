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

public class Compass {

    static final int CENTER_X = 3;
    static final int CENTER_Y = 3;
    static final int ARROW_LENGTH = 3;

    static final int BG_COLOR = Argb32.fromRgb(0, 0, 0);
    static final int CENTER_COLOR = Argb32.fromRgb(0, 0, 255);
    static final int MID_COLOR = Argb32.fromRgb(100, 0, 0);
    static final int TIP_COLOR = Argb32.fromRgb(255, 0, 0);

    static double smoothedHeading(SenseHat hat, int samples) throws InterruptedException {
        double sum = 0.0;
        for (int i = 0; i < samples; i++) {
            sum += hat.getCompass();
            Thread.sleep(10);
        }
        return sum / samples;
    }

    static void drawArrowToNorth(SenseHat hat, double headingDeg) {
        hat.fill(BG_COLOR);
        double angleRad = Math.toRadians(-headingDeg);

        for (int i = 1; i <= ARROW_LENGTH; i++) {
            int dx = (int) Math.round(Math.cos(angleRad) * i);
            int dy = (int) Math.round(Math.sin(angleRad) * i);

            int x = CENTER_X + dx;
            int y = CENTER_Y + dy;

            if (x >= 0 && x <= 7 && y >= 0 && y <= 7) {
                int color = (i == ARROW_LENGTH) ? TIP_COLOR : MID_COLOR;
                hat.setPixel(x, y, color);
            }
        }

        hat.setPixel(CENTER_X, CENTER_Y, CENTER_COLOR);
    }

    public static void main(String[] args) throws InterruptedException {
        Context pi4j = Pi4J.newAutoContext();
        try (SenseHat hat = new SenseHat(pi4j)) {
            hat.clear();
            while (true) {
                double heading = smoothedHeading(hat, 5);
                drawArrowToNorth(hat, heading);
            }
        } finally {
            pi4j.shutdown();
        }
    }
}
