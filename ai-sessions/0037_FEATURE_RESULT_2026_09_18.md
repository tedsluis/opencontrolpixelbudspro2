# 0037_FEATURE_RESULT_2026_09_18.md — "Connect" does nothing, tab navigation shows the wrong screen

**Number:** 0037
**Category:** FEATURE
**Date:** 2026-09-18
**Title:** Investigate and fix "Connect" doing nothing with no status feedback, and a bottom-nav bug landing unexpectedly on Debug; extensive up-front analysis requested before any fix
**Status:** complete

---

## 1. Root causes — two distinct, unrelated bugs

**Bug A — "Connect" does nothing.** Not a bug in the usual sense: `MainActivity`'s `onConnect`/
`onDisconnect` actions were still the exact placeholders `ai-sessions/0033` left in place when it
first wired the UI (`TODO.md`'s own item for this, added 2026-09-18, had never been closed). More
fundamentally, `BudsRepository` (the domain interface) had no `connect()`/`disconnect()` method at
all — there was nothing for `MainActivity` to call even if it had tried. This directly explains why
ANC/EQ/Find also "don't work": every one of those actions sends a frame over `BudsTransport`, and no
transport connection has ever been opened, regardless of how successful pairing itself is. Pairing
(CDM bonding, `ai-sessions/0036`) and connecting (opening RFCOMM sockets against the bonded device)
are two separate steps; only the first had ever been implemented.

**Bug B — bottom-nav lands on Debug unexpectedly.** `OpenControlNavHost` special-cased the Debug
`NavigationBarItem` with a plain `navController.navigate(Routes.DEBUG)` call, while the other four tabs
used `popUpTo(start){saveState} + launchSingleTop + restoreState`. That asymmetry let Debug accumulate
back-stack entries the other four's `saveState`/`restoreState` handling never touched or cleaned up,
so a later back-navigation (the maintainer's report correlates with back-gesture use, also visible as
repeated "OnBackInvokedCallback is not enabled" warnings in the same log window) could resurface a
stale Debug entry instead of the tab actually being switched to.

## 2. Is this the point to redesign, or keep fixing?

Assessed directly, as asked: **no redesign needed.** Each of the four real-hardware bugs found across
`ai-sessions/0034`-`0037` (manifest namespace mismatch, missing CDM `uses-feature`, no device
filter/no actual bonding, and now no real Connect + a nav back-stack asymmetry) has a distinct,
narrow, already-identified root cause, each fixed in the layer it belongs to without touching
unrelated code. This is the expected shape of "a large batch of new code, never previously run on real
hardware, meeting real hardware for the first time" — not a symptom of an unsound architecture. The
Clean Architecture/MVVM layering (`:ui → :domain ← :data → :hardware`) held up throughout: every fix
so far has been addable within its existing layer boundaries (a manifest fix, a hardware-layer filter,
a domain-interface extension) rather than requiring a structural rework to accommodate it.

## 3. Fixes

- **`android/hardware/.../BudsTransport.kt`**: added `connect(device, channels)`/`disconnect()` to the
  interface.
- **`android/hardware/.../RfcommBudsTransport.kt`**: implements the above — one `BluetoothSocket` per
  DLCI, opened via an injectable `socketFactory`, closing any partially-opened sockets on failure.
  Found and fixed a latent bug in the same pass: `disconnect()` used to cancel a single
  object-lifetime `CoroutineScope`, which would have permanently broken any later reconnect attempt —
  now a fresh `connectionScope` is created per `connect()` call.
- **`android/hardware/.../FakeBudsTransport.kt`**: matching fake implementation plus
  `connectShouldFail` for tests.
- **`android/domain/.../BudsRepository.kt`**: added `connect()`/`disconnect()` to the domain
  interface.
- **`android/data/.../BudsRepositoryImpl.kt`**: constructor now takes the real `ConnectionStateMachine`
  object (not just its read-only `Flow`, since `connect()`/`disconnect()` need to drive its
  transitions) and a `bondedDeviceProvider: () -> BluetoothDevice?` (resolved lazily at connect time,
  never cached at construction, since pairing can happen well after this singleton is built).
  `connect()` fails with `BudsError.PermissionDenied` if no bonded device exists yet.
- **`android/app/.../di/TransportModule.kt`**: now provides the real `RfcommBudsTransport` (default
  socket factory: `BluetoothDevice.createRfcommSocketToServiceRecord()`) instead of
  `FakeBudsTransport`.
- **`android/app/.../di/RepositoryModule.kt`**: updated to supply `ConnectionStateMachine` and
  `companionPairing::bondedDevice` to `BudsRepositoryImpl`.
- **`android/app/.../MainActivity.kt`**: `onConnect`/`onDisconnect` now call
  `budsRepository.connect()`/`disconnect()` for real.
- **`android/ui/.../OpenControlNavHost.kt`**: merged all five destinations (including Debug) into one
  `TAB_DESTINATIONS` list using identical navigation mechanics; Debug stays visually distinct only in
  never showing as `selected`. Also fixed `iconFor()`, which had no `Routes.DEBUG` branch and silently
  fell through to the generic Settings icon (same as Connection/ANC) — now maps explicitly to
  `Icons.Filled.Info`.
- **`android/app/src/main/AndroidManifest.xml`**: added `android:enableOnBackInvokedCallback="true"`,
  closing the recurring "OnBackInvokedCallback is not enabled" warning and adopting predictive-back
  support properly instead of the legacy fallback.
- **`android/app/.../di/TransportModule.kt`** (lint fix): the default `socketFactory` lambda's
  `device.createRfcommSocketToServiceRecord(uuid)` call requires `BLUETOOTH_CONNECT`, and is already
  safely handled — `RfcommBudsTransport.connect()` wraps every `socketFactory(...)` invocation in a
  `catch (e: SecurityException)` that converts it to `BudsError.PermissionDenied` — but lint's
  `MissingPermission` check cannot see across that lambda boundary into a different file's try/catch.
  Added a local catch-and-rethrow in the lambda itself so the permission-requiring call is
  structurally wrapped where lint can see it, without changing behavior (the exception still
  propagates to `RfcommBudsTransport`'s own handling).

## 4. Verification

```
cd android && ./gradlew assembleDebug testDebugUnitTest test lint
BUILD SUCCESSFUL
```

1233 tests, 0 failures (1219 in `:data`, 14 in `:hardware`), 0 lint errors across all four modules
(`:app` has 32 pre-existing warnings, both traced to `AndroidManifest.xml` lines unrelated to this
session's changes — `DataExtractionRules`/`MissingApplicationIcon`). `BudsRepositoryImplTest.kt`
updated to match the new constructor: a `buildConnectionStateMachine(driveToReady = true)` helper
replays the real `Disconnected -> Connecting -> Discovering -> Ready` sequence instead of setting a
raw `MutableStateFlow.value`; the EQ-reset test now drives the same real transitions instead of
jumping the old flow directly to `Ready`; a new test confirms `connect()` fails with
`BudsError.PermissionDenied` when no bonded device exists yet. A fresh debug APK was produced. This
session has no real device of its own — the maintainer's own re-test is the next real verification
step, specifically whether `connect()` actually completes a socket-level handshake against the Buds.

## 5. Documentation

- `TODO.md`: the `ai-sessions/0033`-added "wire onConnect/onDisconnect" item checked off with the full
  fix description; a new "Known technical debt" entry for the nav bug.
- `CHANGELOG.md`: new `### Fixed` entry.
- `ai-sessions/INDEX.md`: `0037` row added.
- `ARCHITECTURE.md`/`PROTOCOL.md`/`DECISIONS.md` needed **no change** — this session extended existing
  interfaces along already-documented lines (`ARCHITECTURE.md` §9.0a's connection design), it did not
  change or newly settle any protocol/architecture question.

## Final summary

**Two unrelated bugs**, both real: `Connect` had never been implemented past its `ai-sessions/0033`
placeholder (no `connect()`/`disconnect()` existed anywhere below `MainActivity`, which is also why
ANC/EQ/Find don't work — they all depend on a transport connection that was never opened), and the
Debug tab's back-stack asymmetry could resurface it in place of the tab actually being switched to.
Both fixed. **No architectural redesign is warranted** — every fix across this session and the three
before it stayed within its existing layer, which is itself evidence the Clean Architecture layering is
holding up under real use, not evidence against it.

**What to expect on re-test**: tapping "Connect" should now attempt a real RFCOMM socket connection
against the bonded Buds and reflect `Connecting`/`Ready`/a specific `BudsError` on the Connection
screen (via `ConnectionState`); tab switching, including after using the system back gesture, should no
longer land on Debug unexpectedly. This is the first real socket-open attempt this project has made —
if the handshake itself fails for a reason specific to the Buds Pro 2 (as opposed to this app's own
wiring, which is what every previous session's bug was), that would be new, genuinely hardware-specific
information this session cannot anticipate further.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0037_FEATURE_RESULT_2026_09_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0037_FEATURE_RESULT_2026_09_18
