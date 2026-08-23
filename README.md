# OpenControl for Pixel Buds Pro 2

An independent, open-source Android app to fully control the **Google Pixel Buds
Pro 2** without the official Pixel Buds app or Google Play Services.

> **Status:** reverse-engineering phase. There is no working app yet. See `TODO.md`
> for the current state.

> ## ⚠️ Disclaimer: hardware risk
>
> This project sends undocumented, reverse-engineered commands to real Pixel Buds
> Pro 2 hardware over an unofficial channel. **This carries a real risk of putting
> your earbuds or case into a bad, potentially unrecoverable state** ("bricking")
> — malformed or unexpected commands are not something Google tests against or
> supports. Use this project's findings and any future app build **at your own
> risk**, against hardware you're prepared to lose.
>
> Mitigations this project takes seriously (see `ARCHITECTURE.md` §8.1's Startup
> Handshake / Safe Mode fallback for the app-level design, and
> `AGENTS.md`/`PROJECT_RULES.md` for the evidence-before-implementation
> discipline) reduce but do **not** eliminate this risk. If something does go
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
| `scripts/lint_docs.py` | Grep-based doc lint (dead filenames, unregistered IDs, stale project name) — run before committing a doc change |

## Target platform

- Compile/target SDK: API 34 (Android 14)
- Minimum supported Android API: **TBD** (see `ARCHITECTURE.md` §15)
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