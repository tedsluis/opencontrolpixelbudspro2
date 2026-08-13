# PROTOCOL.md

Formal, evidence-based specification of the Pixel Buds Pro 2 communication
protocol (`libmaestro` / `libgfps` over Bluetooth Classic RFCOMM, plus relevant
BLE/GATT and Fast Pair mechanisms), as reconstructed from captures, official
Fast Pair specifications, and APK analysis. This is the **source of truth** for
the app's implementation — see `ARCHITECTURE.md` §5 for how the app's code
consumes it.

This document intentionally contains only protocol *facts, hypotheses, and
assumptions* — not implementation details (those live in `ARCHITECTURE.md`) and
not day-to-day working notes (those live in `PROTOCOL_NOTES.md`, which is the
rawer, living document this specification is periodically promoted from — see
`PROJECT_RULES.md` §2).

**Rule:** every entry below carries a status. Nothing is implemented on the
basis of an ASSUMPTION without explicitly accepting that as a risk, recorded in
`DECISIONS.md`.

Status legend:

- 🟢 **FACT** — observed and repeatedly confirmed (multiple captures/experiments,
  or directly stated in Google's official Fast Pair specification as the
  documented mechanism *and* confirmed as what the Buds Pro 2 use).
- 🟡 **HYPOTHESIS** — observed or plausible, not yet independently confirmed
  against our own capture.
- ⚪ **ASSUMPTION** — not yet tested, assumed based on comparable/official
  protocols or an older Pixel Buds generation.

---

## 0. Document metadata

| Field | Value |
|---|---|
| Last verified against firmware | `release_5.203` |
| Primary source | [`qzed/pbpctrl`](https://github.com/qzed/pbpctrl) (Linux/Rust) — protocol *knowledge* only, no code reused (see `AGENTS.md` §12) |
| Secondary sources | Official app/web screenshots (`SCREENSHOTS_PIXEL_BUDS_APP.md`, `SCREENSHOTS_PIXEL_BUDS_WEB_APP.md`), Google Fast Pair Service (GFPS) specification (`developers.google.com/nearby/fast-pair`), `pbtk`-extracted schemas |
| Verification method | Android `CAP-nnn-btsnoop_hci.log` capture and analysis (see `CAPTURE_BLUETOOTH_HCI_SNOOP.md`) |
| Promoted from | `PROTOCOL_NOTES.md` (working notes) |

### 0.1 Firmware / version compatibility matrix

Extend this table as additional firmware versions are tested — per `AGENTS.md` §4 and
`ARCHITECTURE.md` §8, protocol behavior is not assumed stable across firmware updates,
so each row should be added on its own evidence, not by assuming continuity with the row
above it.

| Firmware version | Known protocol-relevant differences | Source |
|---|---|---|
| `release_5.203` | `ADAPTIVE` ANC mode present; 5-band EQ; L/R/Case independent battery reporting (now understood to likely be the Fast Pair Battery Notification, §4.3 Option A) | `[VERIFIED-LOCAL]` (Screenshot UI analysis, 2026-07-30) |

## 1. Transports overview

| Transport | Used for | Status |
|---|---|---|
| Bluetooth Classic RFCOMM (`BluetoothSocket`, SPP-style) | `libmaestro` control channel (ANC, EQ, touch/head-gesture config) and, possibly, Fast Pair Message Stream events (see §2) | 🟡 HYPOTHESIS that these two use the *same* RFCOMM channel — see §2 and §6 open questions |
| BLE advertisement | Fast Pair "Battery Notification" extension — passive battery status broadcast, no active connection required | 🟢 FACT (mechanism, official spec); 🟡 HYPOTHESIS (confirmed as what the Buds Pro 2 send) |
| BLE GATT | Possible standard Battery Service (`0x180F`) for the case; otherwise not confirmed to be used for control | ⚪ ASSUMPTION |

## 2. RFCOMM framing (two competing hypotheses)

All application data over `BluetoothSocket` must be parsed into discrete frames
before being handed to the protobuf deserializer, since RFCOMM is a byte stream
and is not message-delimited at the socket level.

### 2.1 Hypothesis A — Fast Pair Message Stream framing (official, generic)

🟢 FACT (mechanism exists, officially documented) / 🟡 HYPOTHESIS (that
`libmaestro` control traffic rides on it):

| Field | Size | Notes |
|---|---|---|
| Message Group | 1 byte | Selects the category of message (e.g. `0x04` = Action group; `0xFF` = ACK) |
| Message Code | 1 byte | Selects the specific message within the group (e.g. `0x01` = Ring, within the Action group) |
| Additional Data Length | 2 bytes, big-endian | Length of the Additional Data field |
| Additional Data | variable | Message-specific payload |

No magic byte, no checksum — integrity relies on RFCOMM's own reliable, ordered
delivery. Confirmed worked example from the spec: an ACK for a "ring" action
(group `0x04`, code `0x01`) is encoded as `0xFF 0x01 0x00 0x02 0x04 0x01`.

Google's Fast Pair specification explicitly allows partners to extend the
Message Stream with vendor-specific message groups — so it is architecturally
plausible that `libmaestro` is "Message Stream, with Google's own private
message group(s)" rather than a fully separate protocol.

### 2.2 Hypothesis B — proprietary envelope (`pbpctrl`-derived)

🟡 HYPOTHESIS:

| Field | Size | Notes | Confidence |
|---|---|---|---|
| Magic bytes | TBD (commonly cited as 1 byte, e.g. `0x5A`) | Start-of-frame marker, needed to resync a buffered stream after a partial/corrupt read | 🟡 Medium |
| Payload length | 2 bytes (16-bit) | Size of the protobuf payload; whether it includes the Channel/Msg ID byte(s) is unconfirmed | 🟡 Medium |
| Channel / Message ID | TBD size | Selects which `.proto` message handler decodes the payload | 🟡 Medium |
| Protobuf payload | variable | Serialized `libmaestro` protobuf message | 🟢 High (protobuf itself is self-describing once the correct `.proto` is known) |
| Checksum/CRC | optional, TBD | Algorithm not confirmed (CRC16? XOR? absent on some channels?) | 🔴 Low |

A point against this hypothesis: the officially documented Message Stream
format (§2.1) has **no** checksum field at all, which is inconsistent with this
hypothesis if the two channels turn out to be the same one.

### 2.3 Open question and resolution path

**This is the single highest-value open protocol question right now:** is
`libmaestro`'s ANC/EQ control channel the *same* RFCOMM channel as the Fast
Pair Message Stream (§2.1), using a custom/vendor Message Group ID — or a
*separate* RFCOMM channel/PSM with its own proprietary envelope (§2.2)?

**Resolution path:** in a capture, check whether ANC/EQ command frames on the
RFCOMM data channel start with a plausible Message Group/Code pair followed by
a 2-byte big-endian length matching the rest of the frame. If so, that is
strong evidence for §2.1. See `CAPTURE_BLUETOOTH_HCI_SNOOP.md` and open
question tracking in §6 below.

**Handling rule (unchanged regardless of which hypothesis is confirmed, per
`AGENTS.md` §6 and `ARCHITECTURE.md` §5):** any checksum mismatch, or any frame
that fails to parse against the relevant invariants, is dropped silently and
surfaced internally as `BudsError.MalformedFrame` — never a crash, never a
best-effort guess at the payload.

## 3. Protobuf (`.proto`) definitions

The `libmaestro` control channel communicates using serialized Protocol
Buffers. Schemas are extracted from the official companion app APK using tools
such as `pbtk` — extraction is performed by the maintainer, not guessed by an
AI assistant (see `AGENTS.md` §4/§6, `DECISIONS.md` ADR-003).

| File | Purpose | Status |
|---|---|---|
| `maestro_pw.proto` | Core control messages, routing, generic request/response envelope | 🟡 HYPOTHESIS |
| `anc_settings.proto` | ANC / Transparency / Adaptive mode enum | 🟡 HYPOTHESIS |
| `eq_settings.proto` | 5-band equalizer definitions, presets | 🟡 HYPOTHESIS |
| `hardware_status.proto` | Battery / hardware telemetry query-response | 🟡 HYPOTHESIS — may turn out to be Fast Pair's generic Message Stream "Device Information" messages rather than a Buds-specific schema (see §4.3) |

> File names above are best-guess placeholders pending real extraction — see §6
> open questions.

## 4. Command / response patterns

### 4.1 ANC mode

- **Feature states confirmed present in the UI** (firmware `release_5.203`):
  Off, Active (Noise Cancellation), Aware (Transparency), Adaptive. Status: 🟢
  FACT (UI presence, via screenshots).
- **Opcode/payload structure**: not yet extracted — pending §2.3 resolution and
  `.proto` extraction (§3).
- **Sent to**: RFCOMM control channel (exact channel/framing pending §2.3).
- **Expected response**: an ANC state notification frame; exact structure
  unconfirmed.
- **Status**: 🔴 unconfirmed at the byte level (channel/Msg ID, payload layout).
- **Evidence**: UI presence only (`SCREENSHOTS_PIXEL_BUDS_APP.md`,
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §1). No capture evidence yet.
- **Verified with experiment**: none yet — see `EXPERIMENTS.md`.

### 4.2 Equalizer (EQ)

- **Feature confirmed present**: 5-band custom EQ (Low Bass, Bass, Mid, Treble,
  Upper Treble) and presets (Standard/Default, Bass Boost/Heavy Bass, Bass
  Reduction/Light Bass, Balanced, Vocal Boost, Clarity, Last Saved). Status: 🟢
  FACT (UI presence).
- **Opcode/payload structure**: not yet extracted.
- **Sent to / expected response**: same open questions as §4.1.
- **Status**: 🔴 unconfirmed at the byte level.
- **Evidence**: `SCREENSHOTS_PIXEL_BUDS_APP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §1.
- **Verified with experiment**: none yet.

### 4.3 Battery status (Left / Right / Case)

Five candidate mechanisms, in priority order for implementation:

#### Option 0 — Generic OS battery broadcast (supplementary, cheapest to check)

- **Status**: ⚪ ASSUMPTION — mechanism exists on Android (API 31+), not
  confirmed whether it actually fires for this device.
- `BroadcastReceiver` on `BluetoothDevice.ACTION_BATTERY_LEVEL_CHANGED`
  (API 31+) or the legacy `android.bluetooth.device.action.BATTERY_LEVEL_CHANGED`
  intent — Android's own generic, profile-agnostic battery signal for a
  connected Bluetooth device, independent of which underlying mechanism
  (Options A–D below) the OS derived it from.
- **Advantage**: costs nothing to implement — no scan permission, no RFCOMM
  connection, just a receiver registration. If it reliably fires for the
  Buds Pro 2, it can short-circuit needing to implement any of Options A–D
  directly.
- **Caveat**: whether the OS actually populates this for a Fast-Pair-based
  device like the Buds Pro 2 is unconfirmed — treat as a supplement to,
  never a replacement for, confirming at least one of Options A–D.
- **Evidence**: none yet — cheap to test early in a capture/implementation
  session precisely because confirming or ruling it out costs almost nothing.

#### Option A — BLE advertisement (Fast Pair "Battery Notification" extension)

- **Status**: 🟢 FACT (mechanism, official spec) / 🟡 HYPOTHESIS (confirmed as
  used by the Buds Pro 2 specifically).
- **Payload structure** (`[OFFICIAL-SPEC]`,
  `developers.google.com/nearby/fast-pair/specifications/extensions/batterynotification`):

  | Octet | Field | Encoding |
  |---|---|---|
  | 0 | Flags | `0x00` (reserved) |
  | 1..s | Account Key Data | — |
  | s+1 | Battery level length & type | `0bLLLLTTTT` (L = 3 values; T = `0b0011` show / `0b0100` hide) |
  | s+2 | Left bud battery | `0bSVVVVVVV` (S = charging bit, V = 0–100%, `0x7F` = unknown) |
  | s+3 | Right bud battery | `0bSVVVVVVV` |
  | s+4 | Case battery | `0bSVVVVVVV` |

- **Trigger**: sent when RFCOMM connects, or when a battery value changes —
  event-driven, **not** periodic polling (this corrects an earlier, unstated
  assumption of fixed-interval polling).
- Shown ≥8 seconds when using the "show" type; auto-hidden after 20s or via an
  explicit "hide" type frame. Optional when a single bud is inserted/removed.
- **Advantage**: visible on a passive BLE scan — no active connection required,
  useful for the battery fallback logic in `ARCHITECTURE.md` §4.
- **Evidence**: official Fast Pair spec. Not yet `[VERIFIED-LOCAL]` against a
  real capture.

#### Option B — RFCOMM via Fast Pair Message Stream "Device Information"

- **Status**: 🟢 FACT (mechanism exists) / 🔴 unconfirmed (exact battery
  message code).
- The Message Stream (§2.1) has a documented "Device Information" message
  group. Firmware version is confirmed at code `0x09` (per the Find Hub
  Network extension doc), sent once per Message Stream establishment. Battery
  is expected to have its own code in the same group, following the same
  event-driven pattern, but the **specific code value is not yet confirmed**
  from public documentation.
- This is presumed to be the same underlying channel as the
  `hardware_status.proto` hypothesis in §3 — i.e. likely **not** a
  Buds-specific protobuf schema at all, but generic Fast Pair Message Stream
  traffic.
- **Evidence**: official Fast Pair Message Stream / Find Hub Network extension
  docs. Battery-specific code not yet captured.

#### Option C — HFP AT commands (`AT+IPHONEACCEV` / `AT+XAPL`)

- **Status**: ⚪ ASSUMPTION — documented Android-side fallback mechanism (see
  `ARCHITECTURE.md` §4), not yet confirmed as active for this device.

#### Option D — BLE Battery Service (`0x180F`)

- **Status**: 🔴 low confidence — standard GATT characteristic; likely only
  exposes a single aggregate value if present at all.

**Implementation priority**: 0 (cheap to rule in/out) → A → B → C → D (see
`ARCHITECTURE.md` §4; A–D's order reflects official-spec confidence and
connection-cost, not raw confidence alone since A requires no active
connection).

### 4.4 Find My Buds / Ring action

- **Status**: 🟡 HYPOTHESIS — concrete and testable.
- **Hypothesis**: this maps to the Fast Pair Message Stream's documented
  Action group (`0x04`), Ring code (`0x01`), per the spec's own worked ACK
  example (`0xFF 0x01 0x00 0x02 0x04 0x01`).
- **Sent to**: RFCOMM Message Stream channel, per §2.1.
- **Expected response**: an ACK frame (`0xFF 0x01 ...`) per the spec's worked
  example, or a NAK with a reason byte on failure.
- **Evidence**: official Fast Pair Message Stream spec worked example;
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §1 confirms the "Play sound on Left/Right
  earbud/Case" actions exist in the official app UI.
- **Verified with experiment**: none yet — this is the recommended **first**
  capture target to empirically confirm/refute the Message Stream framing
  hypothesis (§2.3), since the expected byte pattern is fully specified and
  low-risk to trigger repeatedly.

### 4.5 Other toggles and secondary features (not yet mapped)

The following features are confirmed present in the official app UI (🟢 FACT
for UI presence) but have no known opcode, channel, or schema yet (🔴
unconfirmed at the protocol level):

- Conversation Detection
- Multipoint Bluetooth
- Touch & Hold customization (per bud: ANC cycle or Digital Assistant)
- In-ear detection
- Volume EQ
- Volume Balance (L/R slider)
- Case sounds (earbuds replaced, other notifications)
- Head gestures (nod/shake)
- Loud Noise Protection (firmware 4.467+; likely on-device DSP only, no
  wire-visible command expected — see §6)
- Adaptive Audio dynamic adjustment (firmware 4.467+; likely on-device DSP
  only — see §6)

> Duplicate §4.1–§4.4's structure per command once each is captured and
> confirmed (opcode/payload structure, target channel, expected response,
> status, evidence, verifying experiment).

## 5. Connection lifecycle

Full step-by-step sequence not yet captured end-to-end. Expected shape, pending
confirmation (advertising → scan/CDM pairing → RFCOMM connect → Message
Stream/`libmaestro` handshake → first battery notification → first
writes/reads):

```
1. Case opens / device becomes discoverable → BLE advertisement observed
   (Fast Pair pairing notification + Battery Notification, §4.3 Option A)
2. User selects device via CompanionDeviceManager (first pairing) or OS
   auto-reconnects to a bonded device
3. RFCOMM (BluetoothSocket) connection established
4. Fast Pair Message Stream and/or libmaestro handshake (exact sequence TBD)
5. Battery status received (event-driven, per §4.3) — "notification with
   battery status on every reconnect" confirmed at the UI/behavior level
   (TESTPLAN_BLUETOOTH_HCI_SNOOP.md §3)
6. App-driven reads (firmware version, serial numbers) and/or writes
   (ANC mode, EQ) as triggered by the user
```

**Status**: ⚪ ASSUMPTION for the overall shape; 🟢 FACT for step 5's
behavioral outcome (battery notification on reconnect), per
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §3.
**Evidence**: to be filled in with capture-ID + frame range once a full
connection sequence is captured (see `CAPTURE_BLUETOOTH_HCI_SNOOP.md`).

## 6. Open questions

Consolidated from `PROTOCOL_NOTES.md` §7 — check items off with a date and a
one-line pointer to the evidence when resolved; add new items here rather than
leaving them buried in prose elsewhere.

### Framing

- [ ] Is `libmaestro`'s ANC/EQ control channel the same RFCOMM channel as the
      Fast Pair Message Stream (§2.1), using a custom/vendor Message Group ID —
      or a separate RFCOMM channel/PSM with its own proprietary envelope
      (§2.2)? Highest-value open question; determines which framing hypothesis
      `FrameDecoder` is implemented against.
- [ ] If §2.2 is confirmed instead: exact magic byte value(s) and length-field
      endianness.
- [ ] Checksum algorithm for §2.2, if confirmed (CRC16? XOR? absent on some
      channels?).

### Commands & schemas

- [ ] Real `.proto` file names and full contents, extracted via `pbtk` against
      the official companion app APK (§3) — current names are placeholders.
- [ ] Channel/Msg ID values for: Set ANC mode, ANC state notification, Set EQ
      band values (§4.1, §4.2).
- [ ] Confirm the Ring / Find My Buds action against the spec's worked example
      (§4.4).
- [ ] Whether `hardware_status.proto` exists as a genuine Buds-specific schema,
      or whether battery is purely generic Fast Pair Message Stream traffic
      with no Buds-specific protobuf involved (§4.3 Option B).
- [ ] Exact Device Information message code for battery within the Message
      Stream group (§4.3 Option B) — firmware version is confirmed as code
      `0x09`; battery's code is not yet confirmed.
- [ ] Protobuf/message mapping for all features listed in §4.5.

### Behavior

- [ ] Whether Loud Noise Protection and/or Adaptive Audio generate any
      Bluetooth traffic toward the phone at all, or remain fully on-device DSP
      decisions with no wire-visible signal.
- [ ] Whether Adaptive Audio requires the official app to remain active to
      keep functioning, or is a one-time write to the buds — relevant to the
      `ForegroundService` design in `ARCHITECTURE.md` §2/§6.
- [ ] Confirmed press duration for triggering pairing mode via the case
      button, distinct from the confirmed 30-second factory-reset hold.
- [ ] Whether captured RFCOMM payload bytes are ever link-layer encrypted in a
      way that requires extra Wireshark configuration to decode.

### Resolved

- [x] Firmware version baseline for the test device — `release_5.203`,
      confirmed via official app screenshot, 2026-07-30.

## 7. Error handling / edge cases

| Scenario | Observed behavior | Status | Evidence |
|---|---|---|---|
| Malformed/unparseable frame (bad magic/length, checksum failure) | Dropped silently, surfaced internally as `BudsError.MalformedFrame`, never a crash | Design rule (not yet capture-verified) | `AGENTS.md` §6, `ARCHITECTURE.md` §5/§7 |
| Connection lost during write | `ConnectionState` moves to `DISCONNECTED`; in-flight polling coroutines cancelled | Design rule (not yet capture-verified) | `ARCHITECTURE.md` §6 |
| Buds out of range | Expected: `IOException` → `ConnectionLost`, per architecture | ⚪ ASSUMPTION | — |
| Case closed during connection | Terminates the active Bluetooth Classic connection | 🔵 confirmed via official support documentation (not yet capture-verified) | `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §2 |
| Inbound frame matching no known schema version | Returns `UnsupportedFirmware` rather than a best-effort parse | Design rule (not yet capture-verified) | `ARCHITECTURE.md` §8 |

## 8. Changelog of this specification

| Date | Change | Author (human/AI model) |
|---|---|---|
| 2026-08-07 | Initial formal specification promoted from `PROTOCOL_NOTES.md`; includes both RFCOMM framing hypotheses, battery mechanism options A–D, Find My Buds/Ring hypothesis, and consolidated open questions | Claude (AI), reviewed by maintainer |