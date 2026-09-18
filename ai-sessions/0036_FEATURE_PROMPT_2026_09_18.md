# 0036_FEATURE_PROMPT_2026_09_18.md — Fix the v1 app's pairing flow (unfiltered picker, no actual bonding, no status feedback)

**Number:** 0036
**Category:** FEATURE
**Date:** 2026-09-18
**Title:** Investigate and fix why pairing offers arbitrary devices and never completes after `ai-sessions/0035`'s crash fix, add pairing status feedback, and make the app notice a device paired outside it
**Status:** prompt only — not yet run

---

## Context

The maintainer rebuilt and reinstalled the app after `ai-sessions/0035`'s crash-on-pair fix. No
crash this time, but:

1. Tapping "Pair a device" offers an arbitrary nearby Bluetooth device (not a list to choose from,
   and not filtered to Pixel Buds) — the maintainer expected a device chooser.
2. After several cancel-and-retry cycles, selecting the actual Pixel Buds Pro 2 and granting the
   permission dialog does not result in a paired device, and the app shows no error or explanation.
3. The maintainer asked explicitly for: a filtered device list (Pixel Buds only), and more on-screen
   status/error information about the pairing attempt.
4. The maintainer asked whether pairing via the app and via Android's own Bluetooth settings are the
   same, noting that pairing via Android Settings is invisible to the app.

Three screenshots and two new log files were supplied — same standing instruction as prior sessions:
information may be taken from them into other project files, but no committed file may ever
reference the log/screenshot files themselves. `android/logs/` is already fully gitignored.

## Task

1. Read the screenshots and logs; determine the root cause(s) from first principles.
2. Fix the device-filter gap and confirm whether CDM's own "success" callback actually performs
   Bluetooth bonding, or whether a separate step was always required and simply never implemented.
3. Add on-screen pairing status/error feedback.
4. Make the app detect a device paired via Android's own Settings app while the app is running.
5. Re-verify with the full build/test/lint suite.
6. Update `TODO.md`, log this session, update `ai-sessions/INDEX.md`, record a `CHANGELOG.md` entry.
7. Commit and push once verified.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0036_FEATURE_PROMPT_2026_09_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0036_FEATURE_PROMPT_2026_09_18
