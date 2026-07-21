---
layout: post
bodyClass: post-page
title: Particle Fireworks on the Sense HAT
date: '2026-07-21'
slug: fireworks-particles
tags:
- Java
- Pi4J
- Sense HAT
- Raspberry Pi
description: A physics-based fireworks simulation for the Raspberry Pi Sense HAT 8x8 LED matrix using Pi4J, with individual particles, gravity and color fade.
---


`FireworksParticles` is a more elaborate take on the [classic fireworks](/Pi4J-Sense-HAT-Playground/posts/fireworks/) demo. Instead of drawing a fixed geometric shape at each explosion, this version runs a small particle system with per-frame physics.

Source code: [projects/FireworksParticles/FireworksParticles.java](https://github.com/igfasouza/Pi4J-Sense-HAT-Playground/blob/main/projects/FireworksParticles/FireworksParticles.java)

## How it works

Each iteration of the main loop produces one firework in four steps:

1. **Launch** — a white pixel climbs from `y = 7` to a random target height and leaves a dim gray trail behind it, giving the rocket a bit more weight than the classic version.
2. **Burst** — at the target position, 10 to 15 `Particle` objects are created. Their angles are distributed around a full circle (with some jitter) and each gets its own random speed, so every burst looks different.
3. **Physics** — every frame each particle advances by its velocity, then a small constant is added to its `vy` to simulate **gravity**. The sparks curve downward and eventually fall off the matrix.
4. **Fade** — the color of each particle is scaled by its remaining life, so it dims out gradually instead of disappearing abruptly.

The animation loop stops as soon as every particle is dead or off-screen, so the duration of each firework varies with the burst.

## Running it

This script depends on a specific branch of `pi4j-drivers` (my `igfasouza` branch), so you need to clone it and install it into your local Maven repository first:

```bash
git clone -b igfasouza https://github.com/igfasouza/pi4j-drivers.git
cd pi4j-drivers
mvn install
```

Then, with [JBang](https://www.jbang.dev/) installed on a Raspberry Pi with a Sense HAT attached, run:

```bash
jbang projects/FireworksParticles/FireworksParticles.java
```

The other dependencies (`pi4j-core`, `pi4j-plugin-ffm`) are declared inline in the script, so no extra setup is required.
