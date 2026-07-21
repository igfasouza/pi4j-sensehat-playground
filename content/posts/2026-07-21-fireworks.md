---
layout: post
bodyClass: post-page
title: Classic Fireworks on the Sense HAT
date: '2026-07-21'
slug: fireworks
tags:
- Java
- Pi4J
- Sense HAT
- Raspberry Pi
description: A small Java program that simulates fireworks on the Raspberry Pi Sense HAT 8x8 LED matrix using Pi4J and a simple geometric pattern.
---


`Fireworks` is a small [JBang](https://www.jbang.dev/) script that animates fireworks on the Raspberry Pi Sense HAT 8x8 LED matrix using [Pi4J](https://pi4j.com/), using a simple geometric explosion pattern.

Source code: [projects/Fireworks/Fireworks.java](https://github.com/igfasouza/Pi4J-Sense-HAT-Playground/blob/main/projects/Fireworks/Fireworks.java)

## How it works

The program runs an endless loop, and each iteration draws one firework in three stages:

1. **Launch** — a white pixel rises from the bottom of the matrix (`y = 7`) up to a random target height, giving the impression of a rocket climbing.
2. **Explosion** — from that target position, an expanding cross-and-diagonal pattern is drawn out to a radius of 3 pixels, painted with a random color picked from a fixed palette (red, green, blue, yellow, magenta, cyan, white).
3. **Fade** — the matrix is cleared a couple of times with short pauses to simulate the sparks dying out.

The `pixel()` helper clamps drawing to the 8x8 bounds so explosions near the edges don't throw errors.

For a physics-based version with individual particles and gravity, see the [particle fireworks](/Pi4J-Sense-HAT-Playground/posts/fireworks-particles/) post.

## Running it

This script depends on a specific branch of `pi4j-drivers` (my `igfasouza` branch), so you need to clone it and install it into your local Maven repository first:

```bash
git clone -b igfasouza https://github.com/igfasouza/pi4j-drivers.git
cd pi4j-drivers
mvn install
```

Then, with [JBang](https://www.jbang.dev/) installed on a Raspberry Pi with a Sense HAT attached, run:

```bash
jbang projects/Fireworks/Fireworks.java
```

The other dependencies (`pi4j-core`, `pi4j-plugin-ffm`) are declared inline in the script, so no extra setup is required.
