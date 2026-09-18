# 0037_FEATURE_PROMPT_2026_09_18.md — "Connect" does nothing, tab navigation shows the wrong screen

**Number:** 0037
**Category:** FEATURE
**Date:** 2026-09-18
**Title:** Investigate and fix "Connect" doing nothing with no status feedback, and a bottom-nav bug landing unexpectedly on Debug; extensive up-front analysis requested before any fix
**Status:** prompt only — not yet run

---

## Context

The maintainer rebuilt and reinstalled the app after `ai-sessions/0036`'s pairing-flow fix, and this
time successfully paired the real Pixel Buds Pro 2 via Android's own Bluetooth settings. Two new
reports:

1. Tapping "Connect" does nothing — no status, no error, no visible change — even though a bonded
   device now exists. The other feature screens (ANC, EQ, Find) also don't work, which the maintainer
   suspected might be a consequence of "Connect" never actually connecting.
2. Switching between the bottom-nav tabs (Connection, ANC, EQ, Find, Debug) sometimes shows the wrong
   tab — Debug appears unexpectedly in place of the tab actually being navigated to.

A screenshot and log files were supplied — same standing instruction as prior sessions: information
may be taken from them into other project files, but no committed file may ever reference the
log/screenshot files themselves. `android/logs/` is already fully gitignored.

The maintainer explicitly asked for an extensive analysis of everything not okay before any fix, and
asked directly whether this is the point to go back to the drawing board for a much better app design,
or whether these issues can be fixed as-is. After the analysis, the maintainer approved implementing a
real `Connect` now (rather than only fixing navigation and leaving Connect labeled
not-yet-implemented).

## Task

1. Read the screenshot/logs and the current code; produce an extensive root-cause analysis of both
   reports, and give an honest assessment of whether the pattern across sessions 0034-0036 (a new
   isolated bug surfacing each time real hardware is used) indicates a need for architectural redesign.
2. Fix the bottom-nav bug.
3. Implement real `Connect`/`Disconnect`: wire `MainActivity`'s actions to an actual
   `RfcommBudsTransport.connect()`/`disconnect()` call via the domain/data layers, extending whatever
   interfaces are needed.
4. Re-verify with the full build/test/lint suite, including fixing any test file broken by the
   `BudsRepositoryImpl` constructor change this requires.
5. Update `TODO.md`, log this session, update `ai-sessions/INDEX.md`, record a `CHANGELOG.md` entry.
6. Commit and push once verified.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0037_FEATURE_PROMPT_2026_09_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0037_FEATURE_PROMPT_2026_09_18
