# 0036_FEATURE_RESULT_2026_09_18.md — Fix the v1 app's pairing flow (unfiltered picker, no actual bonding, no status feedback)

**Number:** 0036
**Category:** FEATURE
**Date:** 2026-09-18
**Title:** Investigate and fix why pairing offers arbitrary devices and never completes after `ai-sessions/0035`'s crash fix, add pairing status feedback, and make the app notice a device paired outside it
**Status:** complete

---

## 1. Root causes — two distinct bugs, not one

**Bug A — no device filter.** `BudsCompanionPairing.requestAssociation()` built its
`BluetoothDeviceFilter` with `Builder().build()` — no name, address, or service constraint at all.
`CompanionDeviceManager` with an unfiltered request offers whatever nearby classic Bluetooth device
it finds, one confirmation dialog at a time, rather than a browsable list — matching exactly what the
maintainer saw and had to repeatedly cancel through.

**Bug B — `CompanionDeviceManager.associate()`'s success callback is not Bluetooth pairing.**
`onAssociationCreated` firing (confirmed by the maintainer successfully reaching and accepting the
consent dialog for the real Pixel Buds Pro 2) only grants this app permission to see/communicate
with the selected device — it does **not** perform classic Bluetooth bonding. `ARCHITECTURE.md`
§9.0a's own documented design already specified the correct next step ("the app calls
`BluetoothDevice.createBond()` and observes `BluetoothDevice.ACTION_BOND_STATE_CHANGED`") — that step
had simply never been implemented in the code calling `BudsCompanionPairing`. So accepting the
consent dialog silently did nothing further: no bonding was ever attempted, `bondedDevices` never
included the Buds, and no error existed to show, because nothing had actually failed from the code's
own (incomplete) perspective — it just never tried the step that matters.

**Bug C — no resume-time re-check.** `hasBondedDevice` was computed once at Activity creation and
only ever updated from the app's own CDM callback. Pairing via Android's own Bluetooth settings
(bypassing CDM and this app entirely) is a completely valid, simpler path that also results in a
bonded device — but the running app had no way to notice it happened. This directly answers the
maintainer's own question: pairing via the app and via Android Settings are **not** the same
mechanism (CDM association+explicit bonding vs. classic Settings-driven bonding directly), but as of
this fix both correctly converge to "a bonded device exists," and the app now notices either path.

Re-confirmed via the newest logs that no crash recurred (only the two already-fixed, pre-`0035`
crash signatures still appear, all with timestamps predating that fix's install) — this session is a
follow-up to a working, crash-free build, not a regression of the previous two fixes.

## 2. Fixes

- **`BudsCompanionPairing.kt`** (`:hardware`):
  - `BluetoothDeviceFilter.Builder().setNamePattern(...)` — a case-insensitive `Pixel\s*Buds` regex,
    confirmed against the maintainer's own real device (whose classic Bluetooth name starts "Pixel
    Buds Pro 2").
  - New `PairingState` sealed type (`Bonding` / `Bonded` / `Failed`) and `observeBonding(device)`,
    which calls `createBond()` and reports progress via a `ACTION_BOND_STATE_CHANGED` receiver —
    implementing `ARCHITECTURE.md` §9.0a's already-correct design for the first time.
  - `deviceForAssociation(AssociationInfo)` resolves CDM's association back to a real
    `BluetoothDevice` (via `AssociationInfo.deviceMacAddress` + `BluetoothAdapter.getRemoteDevice()`)
    so `observeBonding` has something to call `createBond()` on.
- **`MainActivity.kt`** (`:app`): `onPair`'s `onCreated` callback now resolves the device and
  collects `observeBonding`, updating pairing status and `hasBondedDevice` on `Bonded`; `onFailure`
  surfaces the CDM-reported reason. A `LifecycleEventObserver` re-checks `bondedDevice()` on every
  `ON_RESUME`, closing the Settings-app-pairing blind spot.
- **`OpenControlUiState`/`ConnectionScreen`** (`:ui`): new `pairingStatusText: String?` field,
  rendered under the "Pair a device" button — closes the "no message why it didn't work" gap
  directly.

## 3. Verification

```
cd android && ./gradlew assembleDebug testDebugUnitTest test lint
BUILD SUCCESSFUL — 279 actionable tasks
```

1232 tests, 0 failures; lint clean after two further real fixes it caught along the way (a
`BLUETOOTH_CONNECT`-gated `device.name` read inside the bond-state receiver, and an unguarded
`ACTION_REQUEST_ENABLE` `startActivity` call — both wrapped for `SecurityException`, matching
`AGENTS.md` §3's error-conversion rule) and a `LocalLifecycleOwner` import moved to its
non-deprecated package. A fresh debug APK was produced. This session has no real device of its own —
the maintainer's own re-test is the next real verification step, specifically: does the picker now
show only Pixel-Buds-named devices, and does accepting it actually reach `Bonded`.

## 4. Documentation

- `TODO.md`: new entry recording both fixes and what remains unverified.
- `CHANGELOG.md`: new `### Fixed` entry.
- `ai-sessions/INDEX.md`: `0036` row added.
- `ARCHITECTURE.md` needed **no change** — §9.0a's design was already correct; only the
  implementation had never caught up to it. Worth recording precisely, since it means this bug was
  an implementation gap, not a design gap.

## Final summary

**Two real bugs**, both in `BudsCompanionPairing`: no device filter (fixed with a name-pattern
match), and CDM's association success never triggering actual Bluetooth bonding (fixed by
implementing the `createBond()`/`ACTION_BOND_STATE_CHANGED` step `ARCHITECTURE.md` §9.0a had already
specified but nothing had ever called). A third, related gap — the app never noticing a device paired
via Android's own Settings — is also fixed via a resume-time re-check. On-screen pairing status text
now exists so a future failure has a visible reason instead of silence.

**Answering the maintainer's specific question**: pairing via the app (CompanionDeviceManager) and
via Android Settings are two different underlying mechanisms — CDM grants an app-level association
and (as of this fix) triggers bonding explicitly; Settings triggers bonding directly with no CDM
association step at all. Both now converge to the same end state (a bonded device in
`getBondedDevices()`), and the app now detects either path.

**What to expect on re-test**: "Pair a device" should now show only Pixel-Buds-named devices in the
system picker, and accepting the consent dialog should trigger a real classic-Bluetooth pairing
request (the Buds' own pairing UI/LED behavior, if any, is outside this app's control) with a status
line updating from "Pairing…" to either "Paired with …" or an explicit failure reason. This is the
first pairing attempt this project has ever made against real hardware — if bonding itself fails for
a reason specific to the Buds Pro 2 (rather than this app's own wiring, which is what the previous
three sessions' bugs were), that would be new, genuinely hardware-specific information this session
cannot anticipate further.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0036_FEATURE_RESULT_2026_09_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0036_FEATURE_RESULT_2026_09_18
