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

> **Status (2026-09-24; updated 2026-10-06 for 1.1.0):** a tick means *implemented in the app*; nothing is claimed hardware-verified unless `ARCHITECTURE.md` §5a
> says so. The authoritative per-feature state (and its unblocking ADR) is `ARCHITECTURE.md` §5a.

- [x] Read battery status (left, right: DLCI 0x04, ADR-033; case and per-bud "charging in the case": DLCI 0x02 `SubscribeRuntimeInfo`, ADR-043 and its Update)
- [x] Switch Active Noise Cancelling / Transparency / Adaptive mode (ADR-009)
- [x] Configure equalizer / sound profile (presets and custom bands) (ADR-020/034); Volume EQ on/off (field 15, read + write, ADR-055, `ai-sessions/0074`, 1.1.0)
- [x] Configure touch controls and head gestures — "Use touch controls" and press-and-hold per bud built (read + write, ADR-036/045, `ai-sessions/0052`); the press-and-hold ANC-mode list built (field 12, read + write, ADR-046, `ai-sessions/0056`); "Use head gestures" built (field 29, read + write, ADR-052, `ai-sessions/0074`, 1.1.0)
- [x] Volume balance, mono audio, conversation detection (read + write, ADR-036/045, `ai-sessions/0052`)
- [x] Read firmware version and serial numbers per component (firmware since `ai-sessions/0062`; serial numbers since 1.2.0 — one `GetHardwareInfo` per Connect, ADR-058, `ai-sessions/0082`; labelled Case / Right bud / Left bud by position, the official app's reading)
- [x] "Find my Buds" functionality — Left/Right only (ADR-011); Case/"both" out of scope (ADR-027)
- [x] In-ear detection status — the in-ear detection **setting** is read and written (field 2, ADR-047, `ai-sessions/0056`); whether a bud is worn is shown as **"probably worn"** since 1.2.0 (ADR-059, `ai-sessions/0082`: derived from the Settable byte, field 2 and the charging flags — a hypothesis, labelled as such; never per bud)
- [~] Manage multipoint connections — the Multipoint switch is built (field 11, read + write, ADR-053, `ai-sessions/0074`, 1.1.0); choosing or listing the connected devices is not
- [x] Case sound settings (earbuds replaced, other notifications) — fields 28 and 27, read + write, ADR-054, `ai-sessions/0074`, 1.1.0

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
> | Criterion | Without Google Play services | Evidence type |
> |---|---|---|
> | 1. Connect | ✅ `CAP-067` (1.0.0, `8d8af4b`, GrapheneOS secondary user without Play): Connect A161→A241, automatic re-opens A966, A1107, B372, B965, B1601; 0 Play-services claims (positive control `CAP-066`: 26) | capture frame |
> | 2. Battery and ANC | ✅ battery: `CAP-067` (`03 03` frames, Case via runtime info A324) and `CAP-068`; ANC change: ✅ `CAP-068` (1.0.1, `e1fc886`, GrapheneOS secondary user without Play): 12 `Set` → 12 ACK, 0 NAK, from the tab (e.g. A1039 → A1042) and the Quick Settings tile (A1215 → A1218 …), on film; 0 Play-services claims (positive control `CAP-066`) | capture frame + film |
> | 3. Stable over cycles | ✅ `CAP-067`: 5 session ends (Buds `DISC` ×2, Bluetooth off/on, permission revoked, forget + re-pair) and one failed first connect (page timeout, lid closed), each recovered | capture frame |
> | 4. Documented | ✅ | repository |
>
> Frame numbers: "A" = `CAP-067-btsnoop_hci.log.last`, "B" = `CAP-067-btsnoop_hci.log` (`CAP-067-FINDINGS.md`). The 2026-10-02 table (every earlier run had Play
> services present) is superseded by this one; `CAP-035` had Play services disabled, not absent.
> *Corrected 2026-10-03 (`ai-sessions/0069`, maintainer-approved in chat, `AskUserQuestion` "DoD ANC", option "Keep tick, reword + exception (Recommended)"):* the
> "Evidence type" column, the wording of row 2, and row 3's count (it said "6 session ends" and named five; the sixth row of `CAP-067-FINDINGS.md` §7 is a failed
> connect). From 1.0.1 on a criterion is ticked only with a capture frame or film (`RELEASING.md`).
> *Row 2 updated 2026-10-04 (`ai-sessions/0070`, maintainer-approved in chat, `AskUserQuestion` "DoD text", option *"Approve as shown (Recommended)"*):* the ANC change was
> "maintainer-attested, not captured" for 1.0.0; `CAP-068` (`CAP-068-FINDINGS.md` §2; "A" = `CAP-068-btsnoop_hci2.log.last`) replaces it with frames.

## Status after 1.0.x (added 2026-10-03, `ai-sessions/0069`; 1.1.0 added 2026-10-06, `ai-sessions/0074`)

1.0.0 was published on 2026-10-03 (`RELEASING.md`, Release log); 1.0.1, a fix release prepared in `ai-sessions/0069`, was tested in `CAP-068` and released on 2026-10-04 (`ai-sessions/0070`). v1 contains: connection and Safe Mode,
noise control with a Quick Settings tile, the equalizer, battery (Left / Right / Case), Find My Buds (Left / Right), touch controls, press and hold and its mode
list, balance, mono audio, conversation detection and the in-ear detection setting. The next features the maintainer chose (chat 2026-10-03, "Features") reached
🟢 FACT and their ADRs in `CAP-069` (`ai-sessions/0071`): head gestures (`qhr` field 29, ADR-052) and Multipoint (field 11, ADR-053); the case sounds (27, 28,
ADR-054) and Volume EQ (15, ADR-055) followed on 2026-10-06. **1.1.0** (prepared in `ai-sessions/0074`, released 2026-10-07; 1.1.1 followed on 2026-10-08) builds all five switches and the
screen-reader text for "—"; its release run `CAP-070` (`ai-sessions/0075`, 2026-10-07) passed — every switch request on both control channels confirmed by the Buds, the three forms not seen before now on the wire; verdict: release build `0323849`. The Battery Notification on case-open was tested in `CAP-054`: no clear battery field
(ADR-006 Update of 2026-10-06).
**1.1.1** (`ai-sessions/0078`/`0079`: the same app built with the current toolchain) passed its release run `CAP-071` (`ai-sessions/0080`, 2026-10-08) in the
user without Play: installed over 1.1.0 it kept the data and settings, and every Connect read, write, ANC `Set` and Ring was byte-identical to 1.1.0's and answered;
verdict: release build `86a6fb3` (the maintainer, chat 2026-10-08).
**1.2.0** (prepared in `ai-sessions/0082`, 2026-10-09; not yet released): "Changed by the Buds" in the noise-control details, the component serial numbers on the
Info tab (ADR-058), the "probably worn" indicator on the battery card (ADR-059), the Case sounds switches moved to gear → Settings and Conversation detection
to Controls; volume-level notifications studied, not built (a draft ADR-060, a 1.3.0 candidate). Its hardware run `CAP-072` (`ai-sessions/0083`, 2026-10-09) found no heavy defect — every request answered and byte-identical, the serials and the worn line as specified — but showed that a press-and-hold is never shown (no claim open between actions, ADR-032); verdict **fix first** (the maintainer, chat 2026-10-10): ADR-061 holds the noise-control channel while that tab is on screen, then a short re-test. **Rebuilt** as the same version in `ai-sessions/0084` (2026-10-10): the hold built (`setAncTabShown`), the serial fixtures from OpenControl's own `CAP-072` exchange, a finer balance slider (the finger's value while dragging, steps of 1 near the centre); the re-test `CAP-073` (Group BI, `ai-sessions/0085`, 2026-10-10) found no heavy defect — the noise-control channel held on its tab, four press-and-holds shown without a tap, every request byte-identical to `CAP-072`; verdict **OK — ready to release** (the maintainer, chat 2026-10-10).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/PROJECT.md - https://tedsluis.github.io/opencontrolpixelbudspro2/PROJECT
