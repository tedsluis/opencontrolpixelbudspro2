# PROJECT.md

**Project name:** OpenControl for Pixel Buds Pro 2 — see `README.md`.

## Goal

Develop an open-source, self-contained Android app that lets users fully manage
the Google Pixel Buds Pro 2 without any dependency on the official Google Pixel
Buds app or Google Play Services.

## Approach (phases)

1. **Bluetooth analysis** — record btsnoop-hci captures of sessions with the
   official app, and analyze them in Wireshark. See
   `CAPTURE_BLUETOOTH_HCI_SNOOP.md`.
2. **APK reverse engineering** — decompile (JADX) and analyze (apktool) the
   official Pixel Buds app to identify BLE logic, UUIDs, and protocol
   implementation. Findings are recorded in `REVERSE_ENGINEERING.md`.
3. **Correlation & protocol reconstruction** — combine captures and code
   analysis into an evidence-based protocol specification in `PROTOCOL.md`,
   with working notes kept in `PROTOCOL_NOTES.md`.
4. **Design** — record the architecture of the app itself in `ARCHITECTURE.md`.
5. **Implementation** — build the Android app in Kotlin, based on the protocol
   specification, following the guardrails in `AGENTS.md` and `PROJECT_RULES.md`.
6. **Test & validation** — functional testing against real hardware, preventing
   regressions; see `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`.
7. **Documentation** — keep every conclusion traceable and reproducible for
   future contributors, using `EXPERIMENTS.md` (hypothesis → conclusion),
   `DECISIONS.md` (ADR-style design decisions), and `CHANGELOG.md` (changes per
   release).

## Functional scope (v1 — to be adjusted as protocol knowledge grows)

To be finalized based on what is actually found in the protocol. Candidate
features offered by the official app (it still needs to be verified which of
these run over local BLE/RFCOMM versus over the cloud/a Google account):

- [ ] Read battery status (case, left, right)
- [ ] Switch Active Noise Cancelling / Transparency / Adaptive mode
- [ ] Configure equalizer / sound profile (presets and custom bands)
- [ ] Configure touch controls and head gestures
- [ ] Read firmware version and serial numbers per component
- [ ] "Find my Buds" functionality (play sound on left/right/case)
- [ ] In-ear detection status
- [ ] Manage multipoint connections
- [ ] Case sound settings (earbuds replaced, other notifications)

> See `PROTOCOL.md` for which of these features actually run over a local
> BLE/RFCOMM command (in scope) versus require a Google cloud service or
> account (out of scope).

## Non-goals

- No circumvention of DRM or copy protection.
- No reproduction of Google's source code, assets, or trademarks (including the
  "Pixel Buds" wordmark/logo) in the app itself.
- No support for other Pixel Buds models unless the protocol is demonstrably
  identical — this must be separately verified, never assumed.
- No simultaneous multi-device support in v1 — the app targets exactly one
  paired Pixel Buds Pro 2 at a time (see `ARCHITECTURE.md` §15).
- No cloud functionality that requires a Google account — this is by definition
  out of scope for a project whose goal is independence from Google Play
  Services.
- No distribution of the original Google APK or any part of it.
- No telemetry, analytics, or crash reporting of any kind, and no `INTERNET`
  permission in the app (see `AGENTS.md` §1).
- No audio routing or codec implementation — this app sends control payloads
  (ANC, Transparency, EQ) and reads telemetry only; A2DP/LE Audio routing stays
  with the Android OS/Bluetooth stack (see `ARCHITECTURE.md` §6).

## Target platform

- Primary: GrapheneOS
- Secondary: standard Android (AOSP-based), with and without Google Play
  Services

## Definition of "done" (v1)

Without Google Play Services installed, the app can:

1. Connect to the Pixel Buds Pro 2 over Bluetooth (RFCOMM/BLE).
2. At minimum, read and change battery status and ANC/Transparency mode.
3. Remain stable across multiple connect/disconnect cycles.
4. Be documented and reproducible for other contributors, with every protocol
   claim traceable to a capture, a code reference, or a logged experiment.