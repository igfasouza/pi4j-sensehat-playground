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

public class Pressure {

    public static void main(String[] args) throws InterruptedException {
        int textColor = Argb32.fromRgb(255, 255, 0);
        int backColor = Argb32.fromRgb(0, 0, 0);

        Context pi4j = Pi4J.newAutoContext();
        try (SenseHat hat = new SenseHat(pi4j)) {
            hat.clear();
            while (true) {
                double pressure = Math.round(hat.getPressure() * 10.0) / 10.0;
                String message = pressure + " hPa";
                hat.showMessage(message, 70, textColor, backColor);
                Thread.sleep(5000);
            }
        } finally {
            pi4j.shutdown();
        }
    }
}
