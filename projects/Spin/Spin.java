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

public class Spin {

    static final double SPIN_THRESHOLD = 1.0;

    public static void main(String[] args) throws InterruptedException {
        int rightColor = Argb32.fromRgb(0, 255, 0);
        int leftColor = Argb32.fromRgb(255, 0, 0);
        int idleColor = Argb32.fromRgb(50, 50, 50);

        Context pi4j = Pi4J.newAutoContext();
        try (SenseHat hat = new SenseHat(pi4j)) {
            hat.clear();
            while (true) {
                double[] gyro = hat.getGyroscopeRaw();
                double z = gyro[2];

                hat.clear();

                if (z > SPIN_THRESHOLD) {
                    hat.showLetter('R', rightColor);
                } else if (z < -SPIN_THRESHOLD) {
                    hat.showLetter('L', leftColor);
                } else {
                    hat.showLetter('-', idleColor);
                }

                Thread.sleep(100);
            }
        } finally {
            pi4j.shutdown();
        }
    }
}
