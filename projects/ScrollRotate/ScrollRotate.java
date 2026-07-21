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

public class ScrollRotate {

    public static void main(String[] args) {
        String message = "hello PI4J";
        int textColor = Argb32.fromRgb(255, 255, 0);
        int backColor = Argb32.fromRgb(0, 0, 0);
        long scrollDelayMillis = 80;

        Rotation[] rotations = {
            Rotation.ROTATE_0,
            Rotation.ROTATE_90,
            Rotation.ROTATE_180,
            Rotation.ROTATE_270
        };

        Context pi4j = Pi4J.newAutoContext();
        try (SenseHat hat = new SenseHat(pi4j)) {
            hat.clear();
            int rotationIndex = 0;
            while (true) {
                hat.setRotation(rotations[rotationIndex]);
                hat.showMessage(message, scrollDelayMillis, textColor, backColor, ScrollDirection.RIGHT_TO_LEFT);
                hat.showMessage(message, scrollDelayMillis, textColor, backColor, ScrollDirection.LEFT_TO_RIGHT);
                rotationIndex = (rotationIndex + 1) % rotations.length;
            }
        } finally {
            pi4j.shutdown();
        }
    }
}
