# 0034_FEATURE_RESULT_2026_09_18.md — Fix the v1 app's launch crash on real hardware

**Number:** 0034
**Category:** FEATURE
**Date:** 2026-09-18
**Title:** Investigate and fix a crash-on-launch found when installing `ai-sessions/0033`'s debug APK on the maintainer's own real hardware, and gitignore the maintainer's local device/app logs
**Status:** complete

---

## 1. `.gitignore` — done first, before reading the logs

Added `android/logs/` to `.gitignore`. Confirmed via `git status` that the two log files were
untracked and stayed untracked after the change — nothing was ever at risk of being committed. Per
the maintainer's explicit instruction, no file in this project (including this one) names the log
files or their path; only the information extracted from them is used below.

## 2. Root cause

`:app`'s `AndroidManifest.xml` declared its `Application`/`Activity` components with the usual
Android shorthand, `android:name=".OpenControlApplication"` / `android:name=".MainActivity"`. This
shorthand resolves relative to the module's manifest `namespace`
(`io.github.tedsluis.opencontrolpixelbuds`, `app/build.gradle.kts`) — **not** relative to the
class's own actual package. `OpenControlApplication.kt`/`MainActivity.kt` are declared
`package io.github.tedsluis.opencontrolpixelbuds.app` (matching their source directory,
`app/src/main/kotlin/io/github/tedsluis/opencontrolpixelbuds/app/`), one segment deeper than the
namespace. So `.OpenControlApplication` was silently resolving to the fully-qualified name
`io.github.tedsluis.opencontrolpixelbuds.OpenControlApplication` — a class that has never existed —
instead of the real `io.github.tedsluis.opencontrolpixelbuds.app.OpenControlApplication`. Android
only discovers this at process start, when it tries to instantiate the (nonexistent) resolved class
and gets `ClassNotFoundException`, crashing before a single line of app code runs.

This is a pre-existing bug in the app skeleton `ai-sessions/0013` first created (2026-09-13) — this
namespace/package mismatch has been present since that session, just never previously exercised,
since no session before this one had run a real build on a real device.

**This session's own earlier work made it worse, not better.** `ai-sessions/0033`'s Phase 8 build
verification actually hit Android lint's `MissingClass` check flagging exactly this
(`OpenControlApplication`/`MainActivity` "not found in the project or the libraries"), investigated
it, and concluded — wrongly — that it was a known AGP+Hilt tooling false positive, because the
compiled `.class` files existed on disk at the paths checked. That check was real, not a false
positive; verifying "the class file exists somewhere in `build/`" is not the same thing as verifying
"the manifest's fully-resolved class reference actually matches that file's real package," which is
exactly the distinction that mattered here. The lint check was then disabled
(`app/build.gradle.kts`, `lint { disable += "MissingClass" }`), which suppressed the one automated
signal that would have caught this before it ever reached the maintainer's hardware.

Every other module was checked for the same class of bug (manifest `android:name="."` shorthand vs.
that module's actual namespace/package): `:hardware`'s manifest declares `.BudsForegroundService`,
and `:hardware`'s namespace (`io.github.tedsluis.opencontrolpixelbuds.hardware`) does match
`BudsForegroundService.kt`'s real package exactly — confirmed correct, no bug there. `:ui`/`:data`
declare no manifest components at all. `:app` was the only broken one.

## 3. Fix

- **`android/app/src/main/AndroidManifest.xml`**: `OpenControlApplication`/`MainActivity` now
  declared with their fully-qualified class names
  (`io.github.tedsluis.opencontrolpixelbuds.app.OpenControlApplication` /
  `io.github.tedsluis.opencontrolpixelbuds.app.MainActivity`) instead of the relative shorthand —
  a completely standard, valid manifest pattern for a class living in a sub-package of the module's
  namespace, with an inline comment explaining exactly why the shorthand was wrong here (so a future
  reader doesn't "simplify" it back).
- **`android/app/build.gradle.kts`**: removed the incorrect `lint { disable += "MissingClass" }`
  block, replaced with a comment recording the correction (the check was right, the earlier
  reasoning for suppressing it was wrong) — the underlying bug is fixed at the source, so no
  suppression is needed at all.

## 4. Verification

```
cd android && ./gradlew assembleDebug testDebugUnitTest test lint
BUILD SUCCESSFUL in 15s — 279 actionable tasks
```

- **1232 tests, 0 failures** (unchanged from `ai-sessions/0033` — this was a manifest bug, not a
  logic bug, so no test behavior changed).
- **Lint clean, with no `MissingClass` (or any other) suppression** — confirms the check now passes
  because the bug is actually fixed, not because the check was silenced.
- Read the final **merged** manifest directly
  (`app/build/intermediates/merged_manifest/debug/processDebugMainManifest/AndroidManifest.xml`) and
  confirmed both components carry the correct fully-qualified names.
- A fresh debug APK was produced. This session has no real device of its own to install it on — the
  maintainer's own re-test against their Pixel 7a/GrapheneOS hardware is the next real verification
  step, not something this session can complete itself.

## 5. Documentation

- `CHANGELOG.md`: new `### Fixed` entry under `[Unreleased]`.
- `ai-sessions/INDEX.md`: `0034` row added.
- No `ARCHITECTURE.md`/`DECISIONS.md` change — this is a build-configuration bug fix, not an
  architecture or protocol decision.

## Final summary

**Root cause**: `:app`'s `AndroidManifest.xml` used relative class-name shorthand that silently
resolved to a nonexistent class, because the module's `namespace` and its Kotlin source package
differ by one segment (`...opencontrolpixelbuds` vs. `...opencontrolpixelbuds.app`) — a bug present
since the app skeleton was first created, exposed only now that a real APK was installed on real
hardware for the first time. **Compounding factor**: `ai-sessions/0033` had already surfaced this
exact issue via Android lint and incorrectly dismissed it as tooling noise, disabling the one check
that would have caught it pre-emptively.

**Fixed**: `AndroidManifest.xml` now uses fully-qualified class names; the incorrect lint suppression
is removed. Full build/test/lint suite passes clean with no suppressions. Please reinstall the
rebuilt debug APK (`./gradlew assembleDebug` then `adb install -r
app/build/outputs/apk/debug/app-debug.apk`) and confirm the app now launches — if it does, the next
real edge this build will hit is the `CompanionDeviceManager` pairing flow, which remains genuinely
unverified against hardware (`ai-sessions/0033`'s own capability table).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0034_FEATURE_RESULT_2026_09_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0034_FEATURE_RESULT_2026_09_18
