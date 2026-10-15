#!/usr/bin/env python3
"""Which of OpenControl's four noise-control mode buttons is filled, frame by frame — the screen side of a hold-vs-push count.

PROJECT_RULES.md rule 4a: the tool behind a count is committed. Written in ai-sessions/0092 (CAP-076-FINDINGS.md §4.1–§4.2).

Input: a directory of PNG frames f0001.png … cut from a 1080-px-wide screen recording with the noise-control tab on screen (the card
without the "not allowed" line, the 1.3.0 layout), e.g.
  ffmpeg -ss <t0> -t <seconds> -i <recording.mp4> -vf "fps=10,crop=1080:400:0:540,pad=1080:1000:0:540" <dir>/f%04d.png
For each frame the mean grey value of a 40 × 40 patch inside each button (Noise cancellation, Off, Adaptive, Transparency) is measured; a
filled (selected) button reads < 170, an empty one ≈ 240. A frame with no or several dark patches (a dialog, a volume panel, the dimmed
card) prints "-" or "?<names>". Only changes are printed, with the film time (t0 + frame/10) and the phone time (base + film time).

Usage:
  python3 scripts/film_mode_buttons.py <dir> <t0 seconds> <phone time of film t=0, in seconds of the day>
  (CAP-076: film 1 base 23726.3 = 06:35:26.3, film 2 base 25351.5 = 07:02:31.5 — from the status bar's minute flips)
"""
import glob
import sys

import numpy as np
from PIL import Image

PATCHES = {"NC": (470, 640, 510, 680), "OFF": (940, 640, 980, 680), "ADA": (470, 860, 510, 900), "TRA": (940, 860, 980, 900)}
FILLED = 170


def main(d, t0, base):
    prev = None
    for i, f in enumerate(sorted(glob.glob(d + "/f*.png"))):
        a = np.asarray(Image.open(f).convert("L"), dtype=float)
        means = {k: a[y0:y1, x0:x1].mean() for k, (x0, y0, x1, y1) in PATCHES.items()}
        dark = [k for k, v in means.items() if v < FILLED]
        sel = dark[0] if len(dark) == 1 else ("?" + "".join(dark) if dark else "-")
        t = t0 + i / 10
        ph = base + t
        if sel != prev:
            print("%.1f %02d:%02d:%04.1f %s %s" % (t, ph // 3600, (ph % 3600) // 60, ph % 60, sel,
                                                   " ".join("%s=%.0f" % (k, v) for k, v in means.items())))
            prev = sel


if __name__ == "__main__":
    if len(sys.argv) != 4:
        sys.exit(__doc__)
    main(sys.argv[1], float(sys.argv[2]), float(sys.argv[3]))
