#!/usr/bin/env python3
"""The windows in which a film's audio track carries the Buds' ring tone (a narrow band around 3.14 kHz), per 100 ms.

PROJECT_RULES.md rule 4a: the tool behind a count is committed. Written in ai-sessions/0097 (CAP-078-FINDINGS.md §4–§5), where the Find tone of the
Pixel Buds Pro 2 showed as 1.1–1.5-s pulses about 3 s apart with their energy at 3,080–3,200 Hz.

A 100-ms block counts as tone when the mean power in 3,080–3,200 Hz is at least 15 dB above the mean power in 2,600–3,000 Hz and 3,300–3,700 Hz; blocks
closer than 1 s are joined into one window. The input is a 16-kHz mono WAV made from the film (ffmpeg); the offset maps the film's second 0 to phone time.

Usage (needs numpy):
  ffmpeg -i CAP-NNN-recording1.mp4 -vn -ac 1 -ar 16000 /tmp/a1.wav
  python3 scripts/ring_tone_windows.py /tmp/a1.wav 16:23:51.0
"""
import sys
import wave

import numpy as np


def main(path, offset):
    h, m, s = offset.split(":")
    off = int(h) * 3600 + int(m) * 60 + float(s)
    w = wave.open(path)
    sr = w.getframerate()
    x = np.frombuffer(w.readframes(w.getnframes()), dtype=np.int16).astype(np.float32) / 32768
    n = sr // 10
    f = np.fft.rfftfreq(n, 1 / sr)
    tone = (f >= 3080) & (f <= 3200)
    near = ((f >= 2600) & (f < 3000)) | ((f > 3300) & (f <= 3700))
    hits = []
    for i in range(len(x) // n):
        p = np.abs(np.fft.rfft(x[i * n:(i + 1) * n] * np.hanning(n))) ** 2
        if 10 * np.log10((p[tone].mean() + 1e-15) / (p[near].mean() + 1e-15)) > 15:
            hits.append(i / 10)
    windows = []
    for t in hits:
        if windows and t - windows[-1][1] <= 1.0:
            windows[-1][1] = t
        else:
            windows.append([t, t])

    def clock(t):
        t += off
        hh = int(t // 3600)
        mm = int(t % 3600 // 60)
        return f"{hh:02d}:{mm:02d}:{t % 60:04.1f}"

    for a, b in windows:
        k = sum(1 for t in hits if a <= t <= b)
        print(f"{clock(a)}\t{clock(b)}\tfilm {a:.1f}-{b:.1f} s\t{k} blocks")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
