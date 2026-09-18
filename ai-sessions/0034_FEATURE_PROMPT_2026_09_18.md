# 0034_FEATURE_PROMPT_2026_09_18.md — Fix the v1 app's launch crash on real hardware

**Number:** 0034
**Category:** FEATURE
**Date:** 2026-09-18
**Title:** Investigate and fix a crash-on-launch found when installing `ai-sessions/0033`'s debug APK on the maintainer's own real hardware, and gitignore the maintainer's local device/app logs
**Status:** prompt only — not yet run

---

## Context

The maintainer built and installed `ai-sessions/0033`'s debug APK on their own Pixel 7a running
GrapheneOS (`./gradlew assembleDebug` then `adb install -r app/build/outputs/apk/debug/app-debug.apk`).
The app crashed immediately on launch with:

```
java.lang.RuntimeException: Unable to instantiate application
io.github.tedsluis.opencontrolpixelbuds.OpenControlApplication
Caused by: java.lang.ClassNotFoundException: Didn't find class
"io.github.tedsluis.opencontrolpixelbuds.OpenControlApplication" on path: ...
```

The maintainer supplied a logcat export and a full system log pulled from the device, and asked
that both be added to `.gitignore` (device/app logs, distinct from this project's own committed
`captures/` research data) — **the maintainer's own instruction: information may be taken from these
logs into other project files, but no committed file may ever reference or point back at the log
files themselves.**

## Task

1. Add the log files/directory to `.gitignore` before anything else, so they can never be
   accidentally committed.
2. Read both logs and determine the crash's root cause from first principles — do not guess.
3. Fix it, and re-verify with the full build/test/lint suite (`ai-sessions/0033`'s own Phase 8
   already established this project builds and tests cleanly; re-run it after the fix, not before).
4. Check whether the same class of bug exists anywhere else in the project (every module's manifest
   vs. its actual Kotlin package structure), not just the one symptom reported.
5. If this session's own earlier work (`ai-sessions/0033`) contributed to why this wasn't caught
   before reaching the maintainer's hardware, say so plainly and fix that too, not just the symptom.
6. Log this session per `AI_SESSION_LOG_PROCEDURE.md`, update `ai-sessions/INDEX.md`, and record a
   `CHANGELOG.md` entry.
7. Commit and push once verified.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0034_FEATURE_PROMPT_2026_09_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0034_FEATURE_PROMPT_2026_09_18
