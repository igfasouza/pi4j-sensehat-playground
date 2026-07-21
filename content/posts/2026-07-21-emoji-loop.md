---
layout: post
bodyClass: post-page
title: An emoji slideshow on the Sense HAT
date: '2026-07-21'
slug: emoji-loop
tags:
- Java
- Pi4J
- Sense HAT
- Raspberry Pi
description: Cycle through a set of 8x8 pixel-art icons (smiley, heart, skull, space invader, arrow) on the Sense HAT LED matrix using Pi4J.
image: posts/emoji-loop.png
---


`EmojiLoop` is a small pixel-art slideshow for the Sense HAT LED matrix. It cycles through five 8×8 icons — a smiley, a heart, a skull, a Space Invader-style alien and an up arrow — every 3 seconds.

Source code: [projects/EmojiLoop/EmojiLoop.java](https://github.com/igfasouza/Pi4J-Sense-HAT-Playground/blob/main/projects/EmojiLoop/EmojiLoop.java)

## How it works

Each icon is defined as a **flat `int[]` of length 64** (8 columns × 8 rows). To keep the source readable, the code uses short one-letter constants:

- `o` — off (transparent / background)
- `K` — black
- `Y` — yellow
- `R` — red
- `W` — white
- `G` — green
- `B` — blue

Every row of the array is written out with these constants, which makes each sprite look like ASCII art in the source file. That's a nice pattern to know: it makes it very easy to hand-draw new icons.

The main loop:

1. Opens the Sense HAT.
2. Iterates over the array of sprites and their names.
3. For each sprite, prints its name to the console and calls `hat.setPixels(sprite)` to draw all 64 pixels at once.
4. Sleeps 3 seconds, then moves to the next icon and eventually restarts from the beginning.

## Running it

This script depends on a specific branch of `pi4j-drivers` (my `igfasouza` branch), so clone and install it locally first:

```bash
git clone -b igfasouza https://github.com/igfasouza/pi4j-drivers.git
cd pi4j-drivers
mvn install
```

Then run it with [JBang](https://www.jbang.dev/) on a Raspberry Pi with a Sense HAT attached:

```bash
jbang projects/EmojiLoop/EmojiLoop.java
```
