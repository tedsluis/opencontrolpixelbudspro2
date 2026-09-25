# OpenControl for Pixel Buds Pro 2

An independent, open-source Android app to fully control the **Google Pixel Buds
Pro 2** without the official Pixel Buds app or Google Play Services.

> **Status (2026-09-24):** protocol reconstruction is mature and a v1 Android app exists end to end. ANC mode switching,
> Find My Buds (Left/Right), the equalizer (read and write), battery (Left/Right with charging, and the Case), a Quick
> Settings ANC tile and a read-only **Safe Mode** for unverified firmware are implemented and unit-tested (1533 tests,
> `ai-sessions/0045`). Two hardware runs exist (`CAP-059`, `CAP-060`): pairing, Connect, ANC, EQ read/write, Find and
> Left/Right battery worked; the Case battery did not (fixed on paper by `DECISIONS.md` ADR-039, not yet re-tested). Battery
> via HFP was removed — it is not deliverable to an app (ADR-040). **Everything changed after `CAP-060` is unverified on
> hardware** — see the re-test list in `ai-sessions/0045_MAINTENANCE_RESULT_2026_09_24.md` before trusting it with your earbuds.

> ## ⚠️ Disclaimer: hardware risk
>
> This project sends undocumented, reverse-engineered commands to real Pixel Buds
> Pro 2 hardware over an unofficial channel. **This carries a real risk of putting
> your earbuds or case into a bad, potentially unrecoverable state** ("bricking")
> — malformed or unexpected commands are not something Google tests against or
> supports. Use this project's findings and any future app build **at your own
> risk**, against hardware you're prepared to lose.
>
> Mitigations this project takes seriously — the app's read-only Safe Mode, which refuses every write unless
> the Buds announce a firmware version this app was verified against (`release_5.203`) and the Pixel Buds Pro 2's
> Fast Pair Model ID (`ARCHITECTURE.md` §8.1, `DECISIONS.md` ADR-042; implemented 2026-09-24, not yet exercised on
> hardware), and the evidence-before-implementation discipline in `AGENTS.md`/`PROJECT_RULES.md` — reduce but do
> **not** eliminate this risk. If something does go
> wrong, see `WORKSTATION_PREPARATIONS.md`'s Disaster Recovery section for the
> hardware-level factory-reset procedure.

## Why

The official Pixel Buds app requires Google Play Services. This project
reconstructs the BLE/RFCOMM communication protocol between the official app and
the Buds based on the maintainer's own, legally obtained Bluetooth captures and
APK analysis of software the maintainer has installed themselves — with the goal
of a free, privacy-friendly implementation that also works on GrapheneOS and other
Google-free Android variants.

## Project goal

Build an open, self-contained Android app that lets you fully manage the Pixel
Buds Pro 2 (ANC modes, EQ, touch controls, battery, case sounds, etc.) with no
dependency on the official Pixel Buds app or Google Play Services (GMS).

To get there, the communication protocol between the official Pixel Buds app and
the Pixel Buds Pro 2 first has to be reconstructed through Bluetooth traffic
analysis and reverse engineering of the Android APK. That knowledge is then used
to design, implement, test, and document a native Android app.

## Current state (2026-09-25)

- **Captures:** 61 registered sessions (`CAP-001`–`CAP-061`): 53 analyzed, 7 planned, 1 withdrawn (`CAP-057`) — see
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 and `id_registry.csv`. `CAP-059`–`CAP-061` are captures of this project's own app; `CAP-061` found
  why the 0045 build stayed in Safe Mode on the verified firmware (fixed in `ai-sessions/0046`, not yet re-tested on hardware).
- **APK analysis:** one companion-app version fully pulled, decompiled, and analyzed (`v1.0.955078536-10253511`) — see
  `reverse-engineering/APK_VERSIONS.md`. DLCI 0x04/0x08's transport code is not in it (ADR-025): both channels are implemented
  independently, from wire-capture evidence (and, for DLCI 0x04, the public Fast Pair spec).
- **Decisions:** 43 ADRs (`DECISIONS.md`); every 🟢 FACT in `PROTOCOL.md` has an explicit maintainer sign-off.
- **Implemented in the app:** ANC/Transparency/Adaptive (DLCI 0x04, ADR-009), Find My Buds Left/Right (ADR-011), EQ read and write
  (DLCI 0x02 pw_rpc, ADR-020/034), battery Left/Right with charging (ADR-033) and the Case (DLCI 0x02 `SubscribeRuntimeInfo`, ADR-043),
  firmware line, ANC Quick Settings tile, Safe Mode (ADR-042).
- **Protocol-known but not built:** read-only display of the other settings (touch & hold, multipoint, mono audio, volume EQ, volume
  balance, case sounds, in-ear detection — reads unblocked by ADR-036, writes gated per field); the BLE battery advertisement (never
  matched on the wire).
- **Still open (protocol):** head-gestures field 29, the ANC-rotation checklist's Left/Right split, EQ field 16-vs-18 save semantics,
  DLCI 0x08's own identity, why the Buds sometimes close the RFCOMM channels — see `PROTOCOL.md` §6.
- **App code:** [`android/`](./android) — five Gradle modules (`:app`, `:ui`, `:domain`, `:data`, `:hardware`), Hilt; every codec is
  unit-tested against real capture bytes and fuzzed; CI builds, tests and lints every change and asserts no `INTERNET` permission
  (`.github/workflows/android.yml`).

## Building and installing the debug APK

Requirements: JDK 21, Android SDK with `android-34`/build-tools `34.0.0` installed (the Gradle
wrapper handles the rest). No Android Studio installation is required — the commands below use the
wrapper directly.

```bash
cd android
./gradlew assembleDebug
```

The APK lands at `android/app/build/outputs/apk/debug/app-debug.apk`. Install it on a device with
[ADB](https://developer.android.com/tools/adb) (USB debugging enabled) or by copying the file to the
device and opening it (Android will prompt to allow installing from that source):

```bash
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
```

**Minimum Android version: 14 (API 34)** — `DECISIONS.md` ADR-029; the app will not install on an
older OS version. **Permissions the app requests, and why** (AGENTS.md §2, each declared with its
own justification comment in `android/app/src/main/AndroidManifest.xml` and
`android/hardware/src/main/AndroidManifest.xml`): `BLUETOOTH_CONNECT` (RFCOMM socket I/O against the
paired Buds), `POST_NOTIFICATIONS`/`FOREGROUND_SERVICE`/`FOREGROUND_SERVICE_CONNECTED_DEVICE` (the
persistent connection-status notification while connected). `BLUETOOTH_SCAN` is not requested: nothing scans (it would
return, flagged `neverForLocation`, only with the bounded battery-advertisement scan of ADR-006). No `INTERNET` permission, ever
(AGENTS.md §1) — verify this yourself with `aapt dump permissions android/app/build/outputs/apk/debug/app-debug.apk`
if you want to check before installing.

To also run the full test suite and static analysis (what CI runs on every change):

```bash
./gradlew assembleDebug testDebugUnitTest test lint
```

**Before testing against real hardware**, read the re-test list in the latest `ai-sessions/` RESULT
(`ai-sessions/0045_MAINTENANCE_RESULT_2026_09_24.md`) — it says, item by item, what was seen working on hardware and what is only
unit-tested. Given this project's own hardware-risk disclaimer above, do not assume "the tests pass" means "safe against your
earbuds" — it means the wire bytes match known-good captures, nothing more.

## Approach

1. **Capture** — record Bluetooth HCI snoop logs while triggering known actions
   in the official app and on the hardware (see
   [`CAPTURE_BLUETOOTH_HCI_SNOOP.md`](./CAPTURE_BLUETOOTH_HCI_SNOOP.md) and
   [`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`](./TESTPLAN_BLUETOOTH_HCI_SNOOP.md)).
2. **Reverse engineer** — analyze the official Pixel Buds APK (JADX, apktool) to
   understand the internal Bluetooth logic and protocol implementation.
3. **Correlate** — match APK findings against capture data to reconstruct the
   `libmaestro` / `libgfps` wire protocol, with per-capture working notes kept
   in each capture's `CAP-NNN-FINDINGS.md` and the resulting specification in
   `PROTOCOL.md`.
4. **Design & implement** — build a native Android app (Kotlin, Jetpack Compose,
   MVVM/Clean Architecture) around that protocol knowledge, targeting GrapheneOS
   as the primary reference OS with compatibility for stock AOSP-based ROMs. See
   [`ARCHITECTURE.md`](./ARCHITECTURE.md).
5. **Validate & document** — test against real hardware, document findings and
   decisions, and keep protocol/architecture knowledge versioned and evidence-based.

## Core principles

- **Zero-GMS:** the app must function 100% offline, with no telemetry, analytics,
  crash reporting, or `INTERNET` permission whatsoever.
- **GrapheneOS-first:** minimal permissions, no location permissions for BLE
  scanning, no continuous background scanning, and graceful handling of
  GrapheneOS's aggressive Bluetooth/battery policies.
- **Evidence-based reverse engineering:** every protocol claim is backed by a
  capture, a code reference, or an experiment, and is explicitly labeled as fact,
  assumption, or hypothesis — never silently guessed.
- **Independent implementation:** built from reverse-engineering of the
  official Pixel Buds app — the maintainer's own Bluetooth captures plus
  JADX/apktool analysis of the APK the maintainer has installed — and informed
  by the public reverse-engineering findings of
  [`qzed/pbpctrl`](https://github.com/qzed/pbpctrl) (Linux/Rust, MIT-licensed)
  for protocol *knowledge* only. No code is copied from either source; only
  the observed *behavior* (the protocol) is reconstructed, never the
  implementation. No code from BlueZ/D-Bus/UPower is applicable, since this
  app talks directly to Android's native Bluetooth stack (Fluoride/Babel)
  instead.

## Project documentation

This project uses its documentation as the primary knowledge source for both
humans and AI coding assistants working on it:

| File | Purpose |
|---|---|
| `AGENTS.md` | Binding instructions and guardrails for AI coding agents |
| `PROJECT_RULES.md` | Binding project rules (evidence, documentation, scope) |
| `PROJECT.md` | Project goal, scope, and non-goals |
| `ARCHITECTURE.md` | Software architecture of the Android app |
| `REVERSE_ENGINEERING.md` | Findings from APK analysis |
| `APK_REVERSE_ENGINEERING_PROCEDURE.md` | APK pull/decompile/extract/search procedure (prerequisites → steps → analysis approach → gotchas) |
| `reverse-engineering/APK_VERSIONS.md` | Git-tracked index of every analyzed APK version (SHA-256, versionName/versionCode, pull date, provenance, tool versions) — the actual APK/decompiled output never leave the maintainer's own machine |
| `PROTOCOL.md` | Reconstructed protocol specification |
| `DESKRESEARCH_FINDINGS.md` | Offline, script-based pattern analyses across existing captures (no new capture involved) |
| `DECISIONS.md` | Architecture and design decisions (ADR-style) |
| `SECURITY.md` | Security scope and vulnerability reporting |
| `CONTRIBUTING.md` | Guidelines for third-party contributors |
| `CAPTURE_BLUETOOTH_HCI_SNOOP.md` | Bluetooth HCI capture procedure and log |
| `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` | Action/behavior catalog (Test-IDs), linked to capture scenarios and protocol evidence |
| `captures/CAP-NNN-.../CAP-NNN-FINDINGS.md` | Per-capture findings and hypothesis tests (hypothesis → conclusion), promoted directly into `PROTOCOL.md` when confirmed |
| `captures/CAP-NNN-.../CAP-NNN-EVENT-NOTES.md` | Per-capture event timeline (action → timestamp → wire evidence), the raw material `CAP-NNN-FINDINGS.md` is written from |
| `captures/CAP-NNN-.../CAP-NNN-btsnoop_hci.log` | Per-capture raw Bluetooth HCI snoop log, extracted per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3 |
| `captures/CAP-NNN-.../CAP-NNN-recording.mp4` | Per-capture screen recording with burned-in wall-clock overlay, used to correlate on-screen actions with log timestamps |
| `captures/CAP-NNN-.../` (additional artifacts) | Some capture folders include extra supporting material beyond the four standard files above — e.g. `CAP-017-nRF.txt` (an nRF Connect export) and several PNG screenshots. Not every capture has these; check the specific folder. |
| `SCREENSHOTS_PIXEL_BUDS_APP.md` | Reference screenshots of the official Android app |
| `SCREENSHOTS_PIXEL_BUDS_WEB_APP.md` | Reference screenshots of the official web companion app |
| `WORKSTATION_PREPARATIONS.md` | Fedora development workstation setup |
| `TODO.md` | Open tasks and current project status |
| `CHANGELOG.md` | Changes per release |
| `id_registry.csv` | Machine-readable registry of every `CAP-NNN`/`ADR-NNN`/Test-ID — check before assigning a new one |
| `AI_SESSION_LOG_PROCEDURE.md` | Naming scheme, category vocabulary, and numbering discipline for logging AI-agent prompts/results into `ai-sessions/` |
| `ai-sessions/INDEX.md` | Registry of every logged AI-agent prompt/result pair under `ai-sessions/` — check before assigning the next number |
| `scripts/lint_docs.py` | Grep-based doc lint (dead filenames, unregistered IDs, stale project name) — run before committing a doc change |
| `android/` | The Android Studio project itself (five Gradle modules: `:app`, `:ui`, `:domain`, `:data`, `:hardware`) — see the "Current state" section above for what's implemented so far |

## Target platform

- Compile/target/minimum SDK: **API 34 (Android 14)** — `DECISIONS.md` ADR-029
- Primary reference OS: GrapheneOS, with compatibility maintained for stock
  AOSP-based ROMs

## Attribution

Protocol structure knowledge is informed by the public reverse-engineering work of
the [`qzed/pbpctrl`](https://github.com/qzed/pbpctrl) project (Linux/Rust). No
source code from that project is reused directly; only documented protocol/frame
knowledge informs this Android-native implementation.

## License

GNU Affero General Public License v3.0 (AGPL-3.0) — see [`LICENSE`](./LICENSE)
and `DECISIONS.md` ADR-002.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/README.md - https://tedsluis.github.io/opencontrolpixelbudspro2/README
