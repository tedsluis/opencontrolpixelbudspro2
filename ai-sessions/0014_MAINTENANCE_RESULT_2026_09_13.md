# 0014_MAINTENANCE_RESULT_2026_09_13.md — Decide minimum Android API level, refresh README.md, commit and push

**Number:** 0014
**Category:** MAINTENANCE
**Date:** 2026-09-13
**Title:** Decide minimum Android API level, refresh README.md's project status, commit and push
**Status:** complete

---

## 1. Minimum Android API level: decided — API 34 (Android 14)

The maintainer decided directly, in conversation, that Android 14 (API 34) is the minimum supported
version — matching this project's already-fixed compile/target SDK exactly, with no separate lower
floor for broader AOSP-ROM compatibility.

Applied:
- **`DECISIONS.md` ADR-029** — new ADR recording the decision, its context (this closed
  `ARCHITECTURE.md` §15's last remaining open architecture question), and its consequences.
  Registered in `id_registry.csv`.
- **`ARCHITECTURE.md`** — §1's "Compile/target SDK: API 34... Minimum: TBD" line rewritten to
  "Compile/target/minimum SDK: API 34," citing ADR-029. §15: the "Minimum supported Android API
  level" open-question bullet removed; a corresponding line added to §15's "Already decided, not
  open" list.
- **`TODO.md`** — Phase 5's "Decide minimum supported Android API level" checkbox checked, with a
  pointer to ADR-029. The "Recommended priority order" section's matching bullet struck through as
  resolved.
- **`android/`'s Gradle project** — `minSdk` raised from `26` to `34` in the three modules that
  declare it (`:app`, `:hardware`, `:ui`); the `:hardware` module's own comment (previously
  justifying `26` for `CompanionDeviceManager`) rewritten to cite ADR-029 instead.
- **Rebuilt and re-tested end-to-end after the change** (`./gradlew clean assembleDebug test
  testDebugUnitTest`): `BUILD SUCCESSFUL`, a real debug APK produced, **zero test failures/errors**
  across every module's JUnit XML output (re-checked directly, not inferred from the build summary).

## 2. README.md refreshed

Updated to reflect the project's actual current state, which had drifted stale (last written
2026-09-08, before any Android code existed):

- Top status callout: replaced "no application code yet" with an accurate summary of what
  `android/` now contains (five Gradle modules, ANC codec implemented/tested, Hilt-wired
  composition root) and what it explicitly does not yet do (no UI for real device control, never
  run against real hardware).
- "Current state" section: capture count updated (42 → 52), decision count updated (25 → 29,
  naming the three decisions this chat session resolved: Hilt, minimum API, Find My Buds Case
  scope), a pointer to session `0012`'s full independent re-derivation of the prior capture review,
  and a new bullet describing the Android app's own current implementation state and its concrete
  gaps.
- "Target platform" section: "Minimum supported Android API: TBD" replaced with the decided value.
- "Project documentation" table: added a row for `android/` itself.

## 3. Commits and push

Per the maintainer's instruction, applied as three separate commits (one per prior "prompt," where
the files involved could be cleanly attributed) rather than one bundled commit, then a single push:

1. **`ai-sessions/0012`'s own files** — the `CROSSCHECK` prompt/result pair plus the one mechanical
   `CAP-002-FINDINGS.md` fix it applied. These existed, uncommitted, from before this chat session
   began.
2. **`ai-sessions/0013`'s own files** — every document `0013`'s phases touched (`PROTOCOL.md`,
   `DECISIONS.md` ADR-026/027/028, `PROJECT.md`, `ARCHITECTURE.md`, `TODO.md`,
   `REVERSE_ENGINEERING.md`, the `CAP-036`/`CAP-037` count fixes, `id_registry.csv`,
   `ai-sessions/INDEX.md`, the `0013` prompt/result pair itself) plus the entire new `android/`
   Gradle project as it stood at the end of that session (`minSdk = 26`, pre-this-prompt).
3. **This prompt's own files** — `DECISIONS.md` ADR-029, `id_registry.csv`, `ARCHITECTURE.md`,
   `TODO.md`, `README.md`, `android/`'s three `build.gradle.kts` `minSdk` edits, `ai-sessions/INDEX.md`,
   and this prompt/result pair.

This ordering means commit 2 captures `android/` at `minSdk = 26` and commit 3 captures the actual
`26 → 34` diff cleanly, rather than flattening both sessions' work into one indistinguishable change.

`git push` to `origin/main` follows the third commit.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0014_MAINTENANCE_RESULT_2026_09_13.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0014_MAINTENANCE_RESULT_2026_09_13
