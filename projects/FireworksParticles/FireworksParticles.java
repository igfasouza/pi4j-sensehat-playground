///usr/bin/env jbang "$0" "$@" ; exit $?

//JAVA 25

//REPOS mavencentral,mavensnapshot=https://central.sonatype.com/repository/maven-snapshots/

//DEPS com.pi4j:pi4j-core:5.0.0-SNAPSHOT
//DEPS com.pi4j:pi4j-plugin-ffm:5.0.0-SNAPSHOT
//DEPS com.pi4j:pi4j-drivers-igfasouza:1.1.1-SNAPSHOT

import com.pi4j.Pi4J;
import com.pi4j.drivers.hat.raspberry.SenseHat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FireworksParticles {

    private static final Random RANDOM = new Random();

    private static final int[] COLORS = {
            0xFF3030,
            0x30FF30,
            0x3030FF,
            0xFFFF30,
            0xFF30FF,
            0x30FFFF,
            0xFF9030,
            0xFFFFFF
    };

    private static final double GRAVITY = 0.35;
    private static final int TRAIL_FRAMES = 18;
    private static final long FRAME_MS = 90;

    public static void main(String[] args) throws Exception {

        var pi4j = Pi4J.newAutoContext();
        var sense = new SenseHat(pi4j);

        sense.clear();

        while (true) {

            double launchX = RANDOM.nextInt(8);
            double targetY = 1 + RANDOM.nextInt(3);
            int color = COLORS[RANDOM.nextInt(COLORS.length)];

            for (double y = 7; y >= targetY; y -= 1) {
                sense.clear();
                pixel(sense, (int) launchX, (int) y, 0xFFFFFF);
                if (y - 1 >= 0) {
                    pixel(sense, (int) launchX, (int) (y - 1), 0x606060);
                }
                Thread.sleep(60);
            }

            List<Particle> particles = burst(launchX, targetY, color);

            for (int frame = 0; frame < TRAIL_FRAMES; frame++) {
                sense.clear();
                boolean anyAlive = false;
                for (Particle p : particles) {
                    p.step();
                    if (p.alive()) {
                        anyAlive = true;
                        pixel(sense, (int) Math.round(p.x), (int) Math.round(p.y), p.currentColor());
                    }
                }
                Thread.sleep(FRAME_MS);
                if (!anyAlive) {
                    break;
                }
            }

            sense.clear();
            Thread.sleep(200);
        }
    }

    private static List<Particle> burst(double x, double y, int color) {
        int count = 10 + RANDOM.nextInt(6);
        List<Particle> particles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count + RANDOM.nextDouble() * 0.3;
            double speed = 0.6 + RANDOM.nextDouble() * 0.6;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            int life = 8 + RANDOM.nextInt(TRAIL_FRAMES - 8);
            particles.add(new Particle(x, y, vx, vy, color, life));
        }
        return particles;
    }

    private static void pixel(SenseHat sense, int x, int y, int color) {
        if (x >= 0 && x < 8 && y >= 0 && y < 8) {
            sense.setPixel(x, y, color);
        }
    }

    private static final class Particle {
        double x, y, vx, vy;
        final int baseColor;
        int life;
        final int maxLife;

        Particle(double x, double y, double vx, double vy, int color, int life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.baseColor = color;
            this.life = life;
            this.maxLife = life;
        }

        void step() {
            x += vx;
            y += vy;
            vy += GRAVITY;
            life--;
        }

        boolean alive() {
            return life > 0 && x >= 0 && x < 8 && y >= 0 && y < 8;
        }

        int currentColor() {
            double fade = Math.max(0.1, (double) life / maxLife);
            int r = (int) (((baseColor >> 16) & 0xFF) * fade);
            int g = (int) (((baseColor >> 8) & 0xFF) * fade);
            int b = (int) ((baseColor & 0xFF) * fade);
            return (r << 16) | (g << 8) | b;
        }
    }
}
