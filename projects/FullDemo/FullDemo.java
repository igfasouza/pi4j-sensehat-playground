///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25
//DEPS com.pi4j:pi4j-core:4.0.1
//DEPS com.pi4j:pi4j-plugin-ffm:4.0.1
//DEPS com.pi4j:pi4j-drivers-igfasouza:1.1.1-SNAPSHOT
//REPOS mavencentral,mavenlocal,sonatype-snapshots=https://s01.oss.sonatype.org/content/repositories/snapshots/

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.drivers.display.graphics.Argb32;
import com.pi4j.drivers.display.graphics.GraphicsDisplay.Rotation;
import com.pi4j.drivers.display.graphics.GraphicsTextAnimator.ScrollDirection;
import com.pi4j.drivers.hat.raspberry.SenseHat;
import com.pi4j.drivers.hat.raspberry.SenseHat.Event;
import com.pi4j.drivers.hat.raspberry.SenseHat.Version;
import com.pi4j.drivers.sensor.Sensor;

import java.util.List;

public class FullDemo {

    public static void main(String[] args) throws InterruptedException {
        Context pi4j = Pi4J.newAutoContext();
        try (SenseHat hat = new SenseHat(pi4j)) {
            section("Version detection");
            Version version = hat.getVersion();
            System.out.println("Detected Sense HAT version: " + version);

            section("LED matrix: clear + fill");
            hat.clear();
            Thread.sleep(500);
            hat.fill(64, 0, 0);
            Thread.sleep(500);
            hat.fill(Argb32.fromRgb(0, 64, 0));
            Thread.sleep(500);
            hat.clear();

            section("LED matrix: setPixel");
            for (int i = 0; i < 8; i++) {
                hat.setPixel(i, i, 255, 255, 255);
                hat.setPixel(7 - i, i, Argb32.fromRgb(0, 128, 255));
            }
            Thread.sleep(1000);
            System.out.println("Pixel (0,0) = " + Integer.toHexString(hat.getPixel(0, 0)));
            hat.clear();

            section("LED matrix: setPixels(int[])");
            int[] flat = new int[64];
            for (int i = 0; i < flat.length; i++) {
                flat[i] = Argb32.fromRgb((i * 4) & 0xff, 0, 255 - ((i * 4) & 0xff));
            }
            hat.setPixels(flat);
            Thread.sleep(1000);

            section("LED matrix: setPixels(int[][])");
            int[][] rgb = new int[64][3];
            for (int i = 0; i < 64; i++) {
                rgb[i] = new int[] { 0, (i * 4) & 0xff, 128 };
            }
            hat.setPixels(rgb);
            Thread.sleep(1000);

            section("LED matrix: getPixels snapshot");
            int[] snapshot = hat.getPixels();
            System.out.println("Snapshot length = " + snapshot.length);

            section("LED matrix: showLetter");
            hat.showLetter('A');
            Thread.sleep(600);
            hat.showLetter('B', Argb32.fromRgb(255, 0, 0));
            Thread.sleep(600);
            hat.showLetter('C', Argb32.fromRgb(255, 255, 0), Argb32.fromRgb(0, 0, 64));
            Thread.sleep(600);

            section("LED matrix: showMessage variants");
            hat.showMessage("Hi");
            hat.showMessage("Fast", 40);
            hat.showMessage("Color", 60, Argb32.fromRgb(0, 255, 128));
            hat.showMessage("Back", 60, Argb32.fromRgb(255, 255, 255), Argb32.fromRgb(0, 0, 64));
            hat.showMessage("Left>Right", 60, Argb32.fromRgb(255, 128, 0), Argb32.BLACK,
                            ScrollDirection.LEFT_TO_RIGHT);

            section("LED matrix: rotation + flips");
            for (Rotation r : new Rotation[] { Rotation.ROTATE_0, Rotation.ROTATE_90,
                                               Rotation.ROTATE_180, Rotation.ROTATE_270 }) {
                hat.setRotation(r);
                hat.showLetter('R', Argb32.fromRgb(0, 255, 0));
                System.out.println("Rotation " + r);
                Thread.sleep(500);
            }
            hat.setRotation(Rotation.ROTATE_0);
            hat.showLetter('F');
            hat.flipHorizontal();
            Thread.sleep(500);
            hat.flipVertical();
            Thread.sleep(500);
            hat.clear();

            section("Humidity sensor (HTS221)");
            System.out.printf("Humidity: %.2f %%RH%n", hat.getHumidity());
            System.out.printf("Temperature (humidity sensor): %.2f C%n", hat.getTemperatureFromHumidity());

            section("Pressure sensor (LPS25H)");
            System.out.printf("Pressure: %.2f hPa%n", hat.getPressure());
            System.out.printf("Temperature (pressure sensor): %.2f C%n", hat.getTemperatureFromPressure());

            section("Colour sensor (TCS3400)");
            if (version == Version.V2) {
                double[] crgb = hat.getColour();
                System.out.printf("Clear=%.0f R=%.0f G=%.0f B=%.0f%n", crgb[0], crgb[1], crgb[2], crgb[3]);
            } else {
                System.out.println("Skipped: only available on Sense HAT v2.");
            }

            section("IMU: accelerometer + gyroscope (LSM9DS1)");
            hat.setImuConfig(true, true, true);
            double[] accel = hat.getAccelerometerRaw();
            double[] gyro = hat.getGyroscopeRaw();
            double[] degrees = hat.getOrientationDegrees();
            double[] radians = hat.getOrientationRadians();
            System.out.printf("Accel [x,y,z] m/s^2: %.2f, %.2f, %.2f%n", accel[0], accel[1], accel[2]);
            System.out.printf("Gyro  [x,y,z] deg/s: %.2f, %.2f, %.2f%n", gyro[0], gyro[1], gyro[2]);
            System.out.printf("Orientation deg: %.2f, %.2f, %.2f%n", degrees[0], degrees[1], degrees[2]);
            System.out.printf("Orientation rad: %.2f, %.2f, %.2f%n", radians[0], radians[1], radians[2]);

            section("IMU: magnetometer / compass");
            System.out.printf("Compass heading: %.2f deg%n", hat.getCompass());
            double[] mag = hat.getCompassRaw();
            System.out.printf("Magnetic field [x,y,z] gauss: %.4f, %.4f, %.4f%n", mag[0], mag[1], mag[2]);

            section("All sensors");
            List<Sensor> sensors = hat.getAllSensors();
            for (Sensor s : sensors) {
                System.out.println(" - " + s.getClass().getSimpleName());
            }

            section("Joystick: polling with getEvents()");
            System.out.println("Move the joystick for 5 seconds...");
            long deadline = System.currentTimeMillis() + 5000;
            while (System.currentTimeMillis() < deadline) {
                for (Event e : hat.getEvents()) {
                    System.out.println("polled: " + e);
                }
                Thread.sleep(100);
            }

            section("Joystick: getController() state read");
            System.out.println("Center pressed = " + hat.getController().read(
                    com.pi4j.drivers.input.GameController.Key.CENTER));

            section("Joystick: listener");
            var listener = (java.util.function.Consumer<Event>) e ->
                    System.out.println("listener: " + e);
            hat.addJoystickListener(listener);
            System.out.println("Move the joystick for 5 more seconds...");
            Thread.sleep(5000);
            hat.removeJoystickListener(listener);

            section("Joystick: waitForEvent() (press to continue, 10s timeout)");
            Thread worker = Thread.currentThread();
            Thread timeout = new Thread(() -> {
                try {
                    Thread.sleep(10000);
                    worker.interrupt();
                } catch (InterruptedException ignored) {}
            });
            timeout.setDaemon(true);
            timeout.start();
            Event next = hat.waitForEvent();
            timeout.interrupt();
            System.out.println("received: " + next);

            section("Demo complete");
            hat.showMessage("Bye!", 60, Argb32.fromRgb(255, 0, 255));
        } finally {
            pi4j.shutdown();
        }
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }
}
