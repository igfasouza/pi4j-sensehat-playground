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
import com.pi4j.drivers.hat.raspberry.SenseHat.Action;
import com.pi4j.drivers.hat.raspberry.SenseHat.Event;
import com.pi4j.drivers.input.GameController.Key;

public class Joystick {

    static int x = 3;
    static int y = 3;
    static final int COLOR_GREEN = Argb32.fromRgb(0, 255, 0);
    static final int COLOR_RED = Argb32.fromRgb(255, 0, 0);
    static int currentColor = COLOR_GREEN;

    static Key heldDirection = null;

    static void draw(SenseHat hat) {
        hat.clear();
        hat.setPixel(x, y, currentColor);
    }

    static int[] delta(Key key) {
        return switch (key) {
            case UP    -> new int[] { 0, -1 };
            case DOWN  -> new int[] { 0,  1 };
            case LEFT  -> new int[] {-1,  0 };
            case RIGHT -> new int[] { 1,  0 };
            default    -> null;
        };
    }

    public static void main(String[] args) throws InterruptedException {
        Context pi4j = Pi4J.newAutoContext();
        try (SenseHat hat = new SenseHat(pi4j)) {
            draw(hat);
            while (true) {
                for (Event event : hat.getEvents()) {
                    Key key = event.key();
                    Action action = event.action();

                    if (key == Key.CENTER) {
                        if (action == Action.PRESSED) {
                            currentColor = (currentColor == COLOR_GREEN) ? COLOR_RED : COLOR_GREEN;
                            draw(hat);
                        }
                    } else if (delta(key) != null) {
                        if (action == Action.PRESSED) {
                            heldDirection = key;
                        } else if (action == Action.RELEASED && key == heldDirection) {
                            heldDirection = null;
                        }
                    }
                }

                if (heldDirection != null) {
                    int[] d = delta(heldDirection);
                    x = Math.floorMod(x + d[0], 8);
                    y = Math.floorMod(y + d[1], 8);
                    draw(hat);
                }

                Thread.sleep(150);
            }
        } finally {
            pi4j.shutdown();
        }
    }
}
