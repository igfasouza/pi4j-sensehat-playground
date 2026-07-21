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

public class ApproxTemp {

    // Calibration offset in °C (fine-tune for your board)
    static final double CALIBRATION_OFFSET_C = 21.5;

    static double getCorrectedTempF(SenseHat hat) {
        double tempH = hat.getTemperatureFromHumidity();
        double tempP = hat.getTemperatureFromPressure();
        double tempC = (tempH + tempP) / 2.0 - CALIBRATION_OFFSET_C;
        double tempF = tempC * 9.0 / 5.0 + 32.0;
        return Math.round(tempF * 10.0) / 10.0;
    }

    public static void main(String[] args) throws InterruptedException {
        int textColor = Argb32.fromRgb(0, 255, 255);
        int backColor = Argb32.fromRgb(0, 0, 0);

        Context pi4j = Pi4J.newAutoContext();
        try (SenseHat hat = new SenseHat(pi4j)) {
            hat.clear();
            while (true) {
                double tempF = getCorrectedTempF(hat);
                String message = tempF + "F";
                hat.showMessage(message, 70, textColor, backColor);
                Thread.sleep(5000);
            }
        } finally {
            pi4j.shutdown();
        }
    }
}
