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

public class Accelerometer {

    public static void main(String[] args) throws InterruptedException {
        int pixelColor = Argb32.fromRgb(0, 255, 0);

        int x = 3;
        int y = 3;

        Context pi4j = Pi4J.newAutoContext();
        try (SenseHat hat = new SenseHat(pi4j)) {
            hat.clear();
            while (true) {
                double[] accel = hat.getAccelerometerRaw();
                double xAccel = accel[0];
                double yAccel = accel[1];

                hat.clear();

                if (xAccel < -0.2) {
                    x = Math.floorMod(x - 1, 8);
                } else if (xAccel > 0.2) {
                    x = Math.floorMod(x + 1, 8);
                }

                if (yAccel < -0.2) {
                    y = Math.floorMod(y - 1, 8);
                } else if (yAccel > 0.2) {
                    y = Math.floorMod(y + 1, 8);
                }

                hat.setPixel(x, y, pixelColor);
                Thread.sleep(200);
            }
        } finally {
            pi4j.shutdown();
        }
    }
}
