# 0035_FEATURE_RESULT_2026_09_18.md — Fix the v1 app's pairing crash and wire the CDM picker

**Number:** 0035
**Category:** FEATURE
**Date:** 2026-09-18
**Title:** Investigate and fix a crash-on-pair found after `ai-sessions/0034`'s launch-crash fix, and close the disclosed "Pair a device does nothing" gap while at it
**Status:** complete

---

## 1. Root cause

Tapping "Pair a device" calls `BudsCompanionPairing.requestAssociation()`, which calls
`CompanionDeviceManager.associate()`. That API throws `IllegalStateException: Must declare
uses-feature android.software.companion_device_setup in manifest to use this API` at **call time**
(not at manifest-parse time, not at install time) if the app's manifest never declares that
`<uses-feature>` — which this app's manifest never did. Every one of the five new logs shows this
same exception, always at the same call site (`BudsCompanionPairing.requestAssociation` →
`CompanionDeviceManager.associate`), triggered every time "Pair a device" was tapped.

**"The other functions don't work either" is fully explained by this one crash, not a separate
bug.** Cross-checking the app-specific log for any other exception or even a single line from this
app's own logging (`BleLogger`'s tag) found none — the app never once reached a state where ANC/EQ/
Find My Buds could meaningfully be exercised, because every attempt to pair killed the whole process
before the maintainer could navigate anywhere past the Connection screen's only available action (no
bonded device existed yet). This is not a second bug to chase.

The two earlier, `ai-sessions/0034`-era `ClassNotFoundException` crashes also present at the start of
the new logs are stale — timestamped before that fix was applied, not a regression of it (confirmed:
zero occurrences after the `0034` fix's install).

## 2. Fix

- **`android/hardware/src/main/AndroidManifest.xml`** and **`android/app/src/main/AndroidManifest.xml`**:
  added `<uses-feature android:name="android.software.companion_device_setup" android:required="true" />`
  to both (same redundant-but-harmless pattern already used for the `BLUETOOTH_CONNECT`/
  `BLUETOOTH_SCAN` permissions — declared in `:hardware` since that's where the gated API is
  actually called, and in `:app` since that's the final installed manifest; lint analyzes each
  library module against its own manifest, not the merged one).
- **Closed the resulting "does nothing" gap, not just the crash**: fixing the crash alone would leave
  tapping "Pair a device" silently do nothing (the `CompanionDeviceManager.Callback.onAssociationPending`
  callback fires with a real `IntentSender`, but nothing was launching it — a gap `ai-sessions/0033`'s
  own `TODO.md` entry already disclosed). Registered a `pairingLauncher` (`ActivityResultContracts.StartIntentSenderForResult`)
  as a `MainActivity` class property (required — `ComponentActivity` demands `ActivityResultLauncher`
  registration happen unconditionally before the Activity reaches `STARTED`, so it cannot live inside
  the Composable content lambda) and launch it from `onPending`. `TODO.md`'s matching item checked off.

## 3. Verification

```
cd android && ./gradlew assembleDebug testDebugUnitTest test lint
BUILD SUCCESSFUL — 279 actionable tasks
```

1232 tests, 0 failures; lint clean, no suppressions. A fresh debug APK was produced. This session has
no real device of its own — the maintainer's own re-test is the next real verification step.

## 4. Documentation

- `TODO.md`: the CDM `IntentSender`/`ActivityResultLauncher` item checked off, with a note on why it
  got closed alongside an unrelated crash fix.
- `CHANGELOG.md`: new `### Fixed` entry.
- `ai-sessions/INDEX.md`: `0035` row added.
- `MainActivity.kt`'s own class doc comment updated to reflect the picker now actually launching.

## Final summary

**Root cause**: a missing `<uses-feature android:name="android.software.companion_device_setup">`
declaration — `CompanionDeviceManager.associate()` enforces this at call time, and this project's
manifest never declared it. **Fixed**, and the adjacent, already-disclosed "picker never actually
launches" gap closed in the same pass so the maintainer doesn't hit an immediate follow-up report for
it. **What to expect on re-test**: "Pair a device" should now open the real Android device picker.
Selecting a device there is the next genuinely new edge this build will hit — `BudsCompanionPairing`'s
own `onCreated`/`onFailure` paths and the picker UI itself are still unverified against real hardware
by this session. `onConnect`/`onDisconnect` remain intentional placeholders (`RfcommBudsTransport.connect()`
needs a resolved `BluetoothDevice`, which only exists after a real pairing completes) — ANC/EQ/Find My
Buds screens are reachable and safe to explore now (they only read `FakeBudsTransport`-backed state),
but their buttons won't do anything visible against real hardware until that next increment lands.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0035_FEATURE_RESULT_2026_09_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0035_FEATURE_RESULT_2026_09_18
