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
   analysis into an evidence-based protocol specification directly in
   `PROTOCOL.md`, with per-capture working notes kept in each capture's
   `CAP-NNN-FINDINGS.md`.
4. **Design** — record the architecture of the app itself in `ARCHITECTURE.md`.
5. **Implementation** — build the Android app in Kotlin, based on the protocol
   specification, following the guardrails in `AGENTS.md` and `PROJECT_RULES.md`.
6. **Test & validation** — functional testing against real hardware, preventing
   regressions; see `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`.
7. **Documentation** — keep every conclusion traceable and reproducible for
   future contributors, using each capture's `CAP-NNN-FINDINGS.md` (hypothesis
   → conclusion, next to the evidence it tests), `DECISIONS.md` (ADR-style
   design decisions), and `CHANGELOG.md` (changes per release).

## Functional scope (v1 — to be adjusted as protocol knowledge grows)

To be finalized based on what is actually found in the protocol. Candidate
features offered by the official app (it still needs to be verified which of
these run over local BLE/RFCOMM versus over the cloud/a Google account):

> **Status (2026-09-24):** a tick means *implemented in the app*; nothing is claimed hardware-verified unless `ARCHITECTURE.md` §5a
> says so. The authoritative per-feature state (and its unblocking ADR) is `ARCHITECTURE.md` §5a.

- [x] Read battery status (left, right: DLCI 0x04, ADR-033; case and per-bud "charging in the case": DLCI 0x02 `SubscribeRuntimeInfo`, ADR-043 and its Update)
- [x] Switch Active Noise Cancelling / Transparency / Adaptive mode (ADR-009)
- [x] Configure equalizer / sound profile (presets and custom bands) (ADR-020/034)
- [~] Configure touch controls and head gestures — "Use touch controls" and press-and-hold per bud built (read + write, ADR-036/045, `ai-sessions/0052`); the press-and-hold ANC-mode list built (field 12, read + write, ADR-046, `ai-sessions/0056`); head gestures not built
- [x] Volume balance, mono audio, conversation detection (read + write, ADR-036/045, `ai-sessions/0052`)
- [~] Read firmware version and serial numbers per component (firmware shown; serial numbers not read)
- [x] "Find my Buds" functionality — Left/Right only (ADR-011); Case/"both" out of scope (ADR-027)
- [ ] In-ear detection status — the in-ear detection **setting** is read and written (field 2, ADR-047, `ai-sessions/0056`); whether a bud is worn is not shown
- [ ] Manage multipoint connections
- [ ] Case sound settings (earbuds replaced, other notifications)

> See `PROTOCOL.md` for which of these features actually run over a local
> BLE/RFCOMM command (in scope) versus require a Google cloud service or
> account (out of scope).

## Non-goals

- No circumvention of DRM or copy protection.
- No reproduction of Google's source code, assets, or trademarks (including the
  "Pixel Buds" wordmark/logo) in the app itself. *Note (2026-10-02, `DECISIONS.md` ADR-051):* the product name
  as plain text in the app's name ("OpenControl for Pixel Buds Pro 2") and the compatibility
  line is a deliberate exception; logos, wordmark images and other assets stay excluded.
- No support for other Pixel Buds models unless the protocol is demonstrably
  identical — this must be separately verified, never assumed.
- No simultaneous multi-device support in v1 — the app targets exactly one
  paired Pixel Buds Pro 2 at a time (see `ARCHITECTURE.md` §15).
- No cloud functionality that requires a Google account — this is by definition
  out of scope for a project whose goal is independence from Google Play
  Services. Explicitly includes Fast Pair **Account Linking**, **Ownership
  Transfer**, and the **Accessory Non-Owner Service** — investigating or
  implementing these is out of scope (see `DECISIONS.md` ADR-008).
- **No "Find My Buds" ring support for the Case, or "ring both simultaneously"** — in the official
  app, both route exclusively through Google's Find My Device Network (an account/cloud-mediated
  path; `PROTOCOL.md` §4.4 confirms zero local wire traffic while that flow is active, and a
  code-level trace found no local ring-trigger anywhere in the companion app's own decompiled
  source). Left/Right ring is unaffected — it is local, 🟢 FACT, and already implemented (see
  `DECISIONS.md` ADR-011). This is a deliberate, permanent v1 scope decision, not a placeholder for
  future work — see `DECISIONS.md` ADR-027; revisit only if a future capture or protocol change
  finds a genuine local mechanism.
- No reverse-engineering of Google Play Services' own Fast Pair/Nearby module. DLCI 0x04's Fast
  Pair Message Stream and DLCI 0x08's private envelope appear to be implemented entirely inside
  GMS rather than the companion app itself (no trace of either transport was found anywhere in the
  companion app's decompiled source) — this project implements both channels independently, from
  wire-capture evidence (and, for DLCI 0x04, the public Fast Pair specification) alone, the same
  method already used for every confirmed command (see `DECISIONS.md` ADR-025 and its 2026-09-24
  wording Update — not "clean-room", which `AGENTS.md` §12 rules out for this project).
- No distribution of the original Google APK or any part of it.
- No telemetry, analytics, or crash reporting of any kind, and no `INTERNET`
  permission in the app (see `AGENTS.md` §1).
- No audio routing or codec implementation — this app sends control payloads
  (ANC, Transparency, EQ) and reads telemetry only; A2DP/LE Audio routing stays
  with the Android OS/Bluetooth stack (see `ARCHITECTURE.md` §6). This extends
  to the reverse-engineering effort itself: identifying precise audio codec
  parameters (sample rates, bit-depths, bitrates) in captured values is out of
  scope research — the app never needs this information regardless of what
  those values turn out to mean, since the OS/Bluetooth stack owns codec
  negotiation. Where such values appear incidentally in a capture (e.g.
  `CAP-002-FINDINGS.md` §2a's numeric-field comparison), note them as
  out-of-scope-to-pursue-further rather than continuing to narrow their
  meaning.

## Target platform

- Primary: GrapheneOS
- Secondary: standard Android (AOSP-based), with and without Google Play
  Services

## Definition of "done" (v1)

Without Google Play Services installed, the app can:

- [x] 1. Connect to the Pixel Buds Pro 2 over Bluetooth (RFCOMM/BLE).
- [x] 2. At minimum, read battery status and change the ANC/Transparency mode.
- [x] 3. Remain stable across multiple connect/disconnect cycles.
- [x] 4. Be documented and reproducible for other contributors, with every protocol
   claim traceable to a capture, a code reference, or a logged experiment.

> **Evidence (2026-10-03, `ai-sessions/0067`; maintainer-approved in chat 2026-10-03, `AskUserQuestion` "DoD text", *"Approve as shown (Recommended)"*):**
>
> | Criterion | Without Google Play services |
> |---|---|
> | 1. Connect | ✅ `CAP-067` (1.0.0, `8d8af4b`, GrapheneOS secondary user without Play): Connect A161→A241, automatic re-opens A966, A1107, B372, B965, B1601; 0 Play-services claims (positive control `CAP-066`: 26) |
> | 2. Battery and ANC | ✅ battery: `CAP-067` (`03 03` frames, Case via runtime info A324); ANC change: the maintainer's own test of 1.0.0 without Play services (chat 2026-10-03) — `CAP-067` itself carries no ANC `Set` (0 × `08 12`) |
> | 3. Stable over cycles | ✅ `CAP-067`: 6 session ends (Buds `DISC` ×2, Bluetooth off/on, permission revoked, forget + re-pair), each recovered |
> | 4. Documented | ✅ |
>
> Frame numbers: "A" = `CAP-067-btsnoop_hci.log.last`, "B" = `CAP-067-btsnoop_hci.log` (`CAP-067-FINDINGS.md`). The 2026-10-02 table (every earlier run had Play
> services present) is superseded by this one; `CAP-035` had Play services disabled, not absent.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/PROJECT.md - https://tedsluis.github.io/opencontrolpixelbudspro2/PROJECT
