# 0035_FEATURE_PROMPT_2026_09_18.md — Fix the v1 app's pairing crash and wire the CDM picker

**Number:** 0035
**Category:** FEATURE
**Date:** 2026-09-18
**Title:** Investigate and fix a crash-on-pair found after `ai-sessions/0034`'s launch-crash fix, and close the disclosed "Pair a device does nothing" gap while at it
**Status:** prompt only — not yet run

---

## Context

The maintainer rebuilt and reinstalled the debug APK after `ai-sessions/0034`'s fix — the app now
launches successfully. Tapping "Pair a device" on the Connection screen crashes the app, and the
maintainer reports the other screens/functions don't work either. Five new log files were supplied
(two app-specific logcat exports, one crash-only export, two full system logs) — as with
`ai-sessions/0034`, the maintainer's instruction stands: information may be taken from these logs
into other project files, but no committed file may ever reference or point back at the log files
themselves. `android/logs/` is already gitignored from the previous session.

## Task

1. Read the new logs and determine the pairing crash's root cause from first principles.
2. Confirm or rule out whether "the other functions don't work either" is a separate bug, or a
   consequence of the pairing crash killing the whole app before those screens could be reached.
3. Fix the root cause, re-verify with the full build/test/lint suite.
4. If fixing the crash alone would leave a known, already-disclosed gap (the CDM picker's
   `IntentSender` not being launched) making "Pair a device" appear to still do nothing, close that
   gap too rather than leaving the maintainer to file a follow-up report for it.
5. Update `TODO.md` for whatever this closes, log this session per `AI_SESSION_LOG_PROCEDURE.md`,
   update `ai-sessions/INDEX.md`, record a `CHANGELOG.md` entry.
6. Commit and push once verified.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0035_FEATURE_PROMPT_2026_09_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0035_FEATURE_PROMPT_2026_09_18
