---
layout: post
bodyClass: post-page
title: Scrolling and rotating text on the Sense HAT
date: '2026-07-21'
slug: scroll-rotate
tags:
- Java
- Pi4J
- Sense HAT
- Raspberry Pi
description: Scroll a message on the Sense HAT LED matrix in both directions and cycle through the four rotations of the display.
image: posts/scroll-rotate.jpg
---


`ScrollRotate` extends the basic `Hello` example by also demonstrating the two scroll directions and the four rotations supported by the Sense HAT LED matrix.

Source code: [projects/ScrollRotate/ScrollRotate.java](https://github.com/igfasouza/pi4j-sensehat-playground/blob/main/projects/ScrollRotate/ScrollRotate.java)

## How it works

The main loop:

1. Sets the current display rotation using `hat.setRotation(...)`, cycling through `ROTATE_0`, `ROTATE_90`, `ROTATE_180` and `ROTATE_270`.
2. Scrolls the string **hello PI4J** from right to left using `ScrollDirection.RIGHT_TO_LEFT`.
3. Then scrolls the same message from left to right with `ScrollDirection.LEFT_TO_RIGHT`.
4. Increments the rotation index and repeats forever.

This is a good example to see how `GraphicsDisplay.Rotation` and `GraphicsTextAnimator.ScrollDirection` interact — the text always reads the correct way for the current rotation.

## Running it

This script depends on a specific branch of `pi4j-drivers` (my `igfasouza` branch), so clone and install it locally first:

```bash
git clone -b igfasouza https://github.com/igfasouza/pi4j-drivers.git
cd pi4j-drivers
mvn install
```

Then run it with [JBang](https://www.jbang.dev/) on a Raspberry Pi with a Sense HAT attached:

```bash
jbang projects/ScrollRotate/ScrollRotate.java
```
