# Protocol Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`)

**Status:** Living document — single source of truth for reverse-engineered protocol
knowledge, kept separate from implementation code so it can be versioned and corrected
independently. Every entry below must carry a **confidence level** (§2.1) and, where
possible, a reference to how it was verified (§6).

---

## 0. Document Metadata

| Field | Value |
|---|---|
| Last verified against firmware | `release_5.203` |
| Primary source | `qzed/pbpctrl` (Linux/Rust), commit/tag: _pin this_ |
| Secondary sources | Official App Screenshots (2026-07-30), Google Fast Pair Service (GFPS) specification (`developers.google.com/nearby/fast-pair`), community discussions, `pbtk`-extracted schemas |
| Maintainer verification method | Android `btsnoop_hci.log` comparison (§6) |
| Last updated | 2026-08-05 |

**Rule:** any change to this document that comes from your own testing (not from
`pbpctrl`) must be marked `[VERIFIED-LOCAL]` with a date, so it's clear which knowledge
came from the upstream project versus your own empirical confirmation. Facts sourced from
Google's own official Fast Pair specification are marked `[OFFICIAL-SPEC]` — these are
authoritative about the *mechanism*, but not automatically proof that the Buds Pro 2 use
that exact mechanism for a given feature until cross-checked against a real capture.

---

## 1. Overview

This document is the single source of truth for the reverse-engineered `libmaestro` and
`libgfps` protocols used by the Google Pixel Buds Pro 2. Knowledge here is derived
primarily from the `qzed/pbpctrl` project (Linux/Rust), community discussion, Google's own
public **Fast Pair** specification (which the Buds implement as their pairing/discovery
layer, and which turns out to document parts of the transport the Buds likely reuse), and
our own empirical verification via Android's Bluetooth HCI snoop log — adapted for the
Android Kotlin implementation described in `ARCHITECTURE.md`.

This document intentionally does **not** contain Android/Kotlin implementation details
(those live in `ARCHITECTURE.md` §5) — it contains only protocol facts: what bytes mean,
not how we structure the code that handles them.

---

## 2. RFCOMM Envelope (Framing Structure)

All communication over the `BluetoothSocket` (RFCOMM/SPP) is encapsulated in a framing
envelope. The raw byte stream must be parsed into discrete packets before the payload can
be handed to the Protobuf deserializer.

### 2.0 ⚠️ New: a second, officially-documented framing candidate

Google's Fast Pair specification publicly documents a generic RFCOMM message format
called the **Message Stream**, used for provider→seeker/seeker→provider events (device
info, actions like "ring", capability negotiation, ACK/NAK). `[OFFICIAL-SPEC]` structure:

```
+-----------------+----------------+---------------------------+------------------+
| Message Group   | Message Code   | Additional Data Length    | Additional Data  |
| (1 byte)        | (1 byte)       | (2 bytes, big-endian)     | (variable)       |
+-----------------+----------------+---------------------------+------------------+
```

No magic byte, no checksum — integrity here relies on the reliable, ordered nature of
RFCOMM itself rather than an application-level check. Confirmed example from the spec: an
ACK for a "ring" action (group `0x04`, code `0x01`) is encoded as
`0xFF 0x01 0x00 0x02 0x04 0x01` (event group `0xFF`=ACK, code `0x01`=ACK, length `0x0002`,
data = the group/code being acknowledged). A NAK example is also given in the spec with an
additional reason byte.

**This does not replace the existing magic-byte/length/channel-ID/checksum hypothesis
below — it sits alongside it as an open question:**

- **Open question:** is `libmaestro`'s control channel (ANC/EQ commands) the *same* RFCOMM
  channel as the Fast Pair Message Stream, just using a custom Message Group ID above
  Google's reserved ones? Or is it a **separate** RFCOMM channel/PSM entirely, with its
  own proprietary envelope (the magic-byte hypothesis from `pbpctrl`)?
- Both are plausible: Google explicitly allows partners to extend Message Stream with
  vendor-specific message groups, so it's architecturally reasonable for `libmaestro` to
  be "Fast Pair Message Stream, with Google's own private message groups" rather than a
  fully separate protocol. This would be a significant simplification if confirmed — it
  would mean the framing question in the general layout below is largely already solved
  for at least part of the traffic.
- **How to resolve empirically:** in your next `btsnoop_hci.log` capture, check whether
  ANC/EQ command frames on the RFCOMM data channel start with a plausible Message
  Group/Code pair followed by a 2-byte big-endian length that matches the rest of the
  frame — if so, that's strong evidence `libmaestro` rides on Message Stream framing
  rather than the separate magic-byte envelope below.

### General Frame Layout (original `pbpctrl`-derived hypothesis)

```
+------------+-----------------+------------------+---------------------+------------------+
| Magic (?B) | Payload Length  | Channel / Msg ID | Protobuf Payload    | Checksum (opt.)  |
+------------+-----------------+------------------+---------------------+------------------+
```

| Field | Size | Notes | Confidence |
|---|---|---|---|
| Magic Bytes | TBD (commonly 1B, e.g. `0x5A`) | Marks start-of-frame; needed to resync a buffered stream after a partial/corrupt read | 🟡 Medium — confirm exact value(s) against `pbpctrl` source before implementing `FrameDecoder`; also test against the Message Stream hypothesis above first |
| Payload Length | 2 bytes (16-bit) | Size of the protobuf payload *only* — confirm whether it includes the Channel/Msg ID byte(s) or not | 🟡 Medium |
| Channel / Message ID | TBD size | Selects which `.proto` message handler decodes the payload | 🟡 Medium |
| Protobuf Payload | variable | Serialized `libmaestro` protobuf message | 🟢 High (protobuf itself is self-describing once you have the right `.proto`) |
| Checksum/CRC | optional, TBD | Integrity check; algorithm (CRC16? XOR? none for some channels?) not yet confirmed | 🔴 Low — must verify empirically. **Note:** the Message Stream format above has *no* checksum field at all, which is a point against this hypothesis if the two turn out to be the same channel. |

**Handling rule (unchanged from AGENTS.md/ARCHITECTURE.md):** any checksum mismatch, or
any frame that fails to parse against the magic/length invariants, is dropped silently and
surfaced internally as `BudsError.MalformedFrame` — never a crash, never a best-effort
guess at the payload.

### 2.1 Confidence Level Legend

- 🟢 **High** — directly confirmed in `pbpctrl` source code, directly stated in Google's
  official Fast Pair specification as a generic mechanism, or `[VERIFIED-LOCAL]` against a
  real device with an HCI snoop capture.
- 🟡 **Medium** — inferred from `pbpctrl` documentation/behavior but not yet confirmed
  against raw bytes ourselves, based on a related/older Pixel Buds generation, or an
  official spec describing a *generic* mechanism not yet confirmed as what the Buds Pro 2
  specifically use for a given feature.
- 🔴 **Low** — community speculation, undocumented, or extrapolated from a different
  device family. Treat as a hypothesis to test, not a fact to implement against blindly.

---

## 3. Protobuf (`.proto`) Definitions

The device communicates using serialized Protocol Buffers. Schemas are typically extracted
from the official companion app APK using tools like `pbtk`.

### 3.1 Known Schema Files

| File | Purpose | Confidence |
|---|---|---|
| `maestro_pw.proto` | Core control messages, routing, generic request/response envelope | 🟢 High |
| `anc_settings.proto` | ANC / Transparency / Adaptive mode enum | 🟢 High |
| `eq_settings.proto` | 5-band equalizer definitions, presets | 🟢 High |
| `hardware_status.proto` | Battery / hardware telemetry query-response | 🟡 Medium — see §4.3: this may turn out to be Fast Pair's generic Message Stream "Device Information" messages rather than a Buds-specific protobuf schema. Keep both possibilities open until verified. |

---

## 4. Feature Status & Commands

### 4.1 Confirmed Working (per upstream `pbpctrl` & UI captures)

| Feature | Detail | Confidence |
|---|---|---|
| ANC — Off | | 🟢 High |
| ANC — Active (Noise Cancelling) | | 🟢 High |
| ANC — Aware (Transparency) | | 🟢 High |
| ANC — Adaptive | Confirmed present in firmware `release_5.203` | 🟢 High |
| EQ — 5-Band Custom | Bands: Low Bass, Bass, Mid, Treble, Upper Treble | 🟢 High |
| EQ — Presets | Default, Heavy Bass, Light Bass, Balanced, Vocal Boost, Clarity, Last Saved | 🟢 High |

**Command opcode table** _(pending extraction from `btsnoop_hci.log`)_:

| Command | Channel/Msg ID | Protobuf message | Direction | Confidence |
|---|---|---|---|---|
| **Set ANC mode** | Fast Pair Message Stream Group `0x08` ("Hearable Controls" `[OFFICIAL-SPEC]`), Code `0x12` "Set ANC state", on DLCI 0x04 (**not** `libmaestro`) | N/A — not protobuf; fixed layout `[Group][Code][Len][Ver][Settable][Enabled][NewMode][16B reserved]` | App → Buds | 🟢 High — **resolved 2026-08-12**, byte-for-byte spec match + 4/4 content+timing correlation against `CAP-001`'s own recorded ANC taps. See `PROTOCOL.md` §4.1, `CAP-001-FINDINGS.md` §5. |
| **ANC state notification** | Same Group `0x08`, Code `0x13` "Notify ANC state" | N/A — same layout, no "New mode"/reserved fields | Buds → App | 🟢 High — see above |
| Set EQ band values | TBD — no matching official Fast Pair extension found yet (2026-08-12 spec sweep covered Device Information/Action/Change-Capability/SASS/Hearable-Controls/Acknowledgement/Personalized-Name); `libmaestro` (DLCI 0x02, Pigweed `pw_hdlc`) remains the leading untested candidate | TBD | App → Buds | 🔴 Low |
| **Ring / Find My Buds action** | Likely Message Stream group `0x04`, code `0x01` per `[OFFICIAL-SPEC]` generic Fast Pair "Action" group | N/A (not protobuf — plain Message Stream data if this hypothesis holds) | App → Buds | 🟡 Medium — **new**, see note below |

**New note on ANC (2026-08-12):** Google's official
[Hearable Controls](https://developers.google.com/nearby/fast-pair/specifications/extensions/hearablecontrols)
extension page documents Message Group `0x08` (Get/Set/Notify ANC state, codes `0x11`/`0x12`/`0x13`)
with a one-hot ANC-mode bitmask (`0x80`=Transparent, `0x40`=Adaptive, `0x20`=Off, `0x08`=ANC) that
matches `CAP-001-btsnoop_hci.log` byte-for-byte, including a 4/4 content+timing match against that
capture's own six recorded ANC taps (frames 2039/2132/2159/2193, each followed by the documented
ACK). This is the project's first fully-resolved control-channel command — full write-up in
`PROTOCOL.md` §4.1 and `CAP-001-FINDINGS.md` §5's "Full resolution" block. Separately, DLCI 0x02
(channel 1) was confirmed the same day to be Pigweed `pw_hdlc` framing (flag `0x7E` + `0x7D`-escape
byte-stuffing + LEB128 address + control byte + CRC-32 FCS, 640/640 sub-frames verified across
`CAP-001`/`CAP-002`/`CAP-003`) — matching `qzed/pbpctrl`'s own notes that Maestro wraps its pw_rpc
messages in HDLC U-frames. This is the strongest candidate channel for EQ/other non-ANC settings,
but its payload content (a pw_rpc/protobuf service) is not yet decoded. See `PROTOCOL.md` §2.2a.

**New note on "Ring" / Find My Buds:** the Fast Pair Message Stream spec's own
worked ACK example explicitly references action group/code `0x04`/`0x01` for a **ring**
action. Combined with the "Speel geluid af" / "Find My Buds" actions identified in
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §1, this is a strong, concrete, low-risk first target to verify the Message
Stream hypothesis from §2.0 empirically — capture a "Play sound on Left earbud" action and
check whether the outbound frame matches `0x04 0x01 ...` framing.

### 4.2 Toggles & Secondary Features (Backlog)

Based on official UI analysis (firmware `release_5.203`), the following features exist and
require protobuf/message mapping:

- Conversation Detection (Gespreksdetectie)
- Multipoint Bluetooth
- Touch & Hold customization (per bud: ANC cycle or Digital Assistant)
- In-ear detection (In-eardetectie)
- Volume EQ (Volume-equalizer)
- Volume Balance (L/R Balance slider)
- Case Sounds (Oordopjes terugplaatsen, Andere meldingen)
- Head Tracking (Hoofdbewegingen gebruiken)
- Loud Noise Protection (firmware 4.467+, likely on-device DSP only — see `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §4)
- Adaptive Audio dynamic adjustment (firmware 4.467+, likely on-device DSP only — see
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §4)

### 4.3 Battery (Left / Right / Case) — **major update: officially specified mechanisms found**

**Previous status:** treated as fully experimental/reverse-engineered, with the
`libmaestro` `HardwareStatus` query as an unconfirmed 🟡 Medium hypothesis.

**Updated status:** Google's official Fast Pair specification documents **two concrete,
byte-level mechanisms** for exactly this kind of multi-component battery reporting. Since
the Buds Pro 2 are a Fast Pair device, both are now the **primary** candidates — ahead of
a Buds-specific proprietary schema.

#### Option A — BLE Advertisement (Fast Pair "Battery Notification" extension) — 🟢 High (mechanism), 🟡 Medium (confirmed as what the Buds Pro 2 use)

`[OFFICIAL-SPEC]`, from `developers.google.com/nearby/fast-pair/specifications/extensions/batterynotification`:

```
Octet   Field                         Encoding
0       Flags                         0x00 (reserved)
1..s    Account Key Data              —
s+1     Battery level length & type   0bLLLLTTTT (L=3 values, T=0b0011 show / 0b0100 hide)
s+2     Left bud battery              0bSVVVVVVV (S=charging bit, V=0-100%, 0x7F=unknown)
s+3     Right bud battery             0bSVVVVVVV
s+4     Case battery                  0bSVVVVVVV
```

- **Trigger:** update sent when RFCOMM connects, or when a battery value changes —
  **event-driven, not periodic polling.** (Corrects the earlier unstated assumption of
  polling.)
- Shown for ≥8 seconds when using the "show" type; auto-hidden after 20s or via an
  explicit "hide" type frame.
- Optional to include when a single bud is inserted/removed from the case.
- **Advantage for our purposes:** this is visible on a passive BLE scan — no active
  connection required, useful for `ARCHITECTURE.md` §4 battery fallback logic even before
  RFCOMM is up.

#### Option B — RFCOMM via Fast Pair Message Stream "Device Information" — 🟢 High (mechanism exists), 🔴 Low (exact battery-specific message code not yet confirmed)

`[OFFICIAL-SPEC]`: the Message Stream (§2.0) has a documented "Device Information" message
group used for properties like firmware version (confirmed code `0x09` in the Find Hub
Network extension doc — "device information code 0x09" for firmware version string,
sent once per Message Stream establishment). Battery is highly likely to have its own code
in the same group, following the same event-driven ("send once per connection, then on
change") pattern as Option A, but the **specific code value for battery within this group
is not yet confirmed** from public documentation — needs empirical capture to pin down.
This is presumed to be the same channel as the `HardwareStatus` hypothesis in §3.1 — likely
**not** a Buds-specific protobuf schema at all, but generic Fast Pair Message Stream
traffic.

#### Option C — HFP AT commands (`AT+IPHONEACCEV`) — 🟡 Medium

Fallback if the above don't yield per-component data on a given Android/OEM combination.

#### Option D — BLE Battery Service (`0x180F`) — 🔴 Low

Standard GATT characteristic; likely only exposes a single aggregate value if present at
all.

**Revised priority order for implementation:** A (BLE advertisement, no connection
required, officially specified, exact byte layout known) → B (RFCOMM Message Stream, needs
one more capture to confirm the exact code) → C (HFP fallback) → D (GATT, last resort).
This is a change from the previous ordering, which had the (still-unconfirmed) proprietary
`libmaestro` query listed first.

---

## 5. Firmware / Version Compatibility Matrix

| Firmware version | Known protocol differences | Source |
|---|---|---|
| `release_5.203` | `ADAPTIVE` ANC mode present; 5-band EQ; L/R/Case independent battery reporting (now understood to likely be Fast Pair Battery Notification, §4.3 Option A) | `[VERIFIED-LOCAL]` (Screenshot UI Analysis, 2026-07-30) |

---

## 6. Verification Methodology (HCI Snoop Log)

See `CAPTURE_BLUETOOTH_HCI_SNOOP.md` for the full step-by-step capture procedure (Developer options setup,
mandatory test capture, `adb bugreport` extraction, Wireshark filtering, truncation
diagnostics).

**New, specific verification targets arising from this update:**

1. Confirm/refute the Message Stream framing hypothesis (§2.0) against a real ANC-toggle
   frame.
2. Confirm the "Ring" action group/code (`0x04`/`0x01` per spec) against a captured "Play
   sound on Left earbud" action (§4.1).
3. Identify the Device Information message code used for battery within the Message
   Stream (§4.3 Option B) by capturing the frames sent immediately after RFCOMM connects.
4. Passively capture a BLE scan (no connection) to confirm the Battery Notification
   advertisement (§4.3 Option A) byte-for-byte against the spec table.

---

## 7. Open Questions

Consolidated from inline notes scattered across this document. Check items off with a
date and a one-line pointer to the evidence when resolved; add new items here rather than
leaving them buried in a section's prose.

### Framing
- [ ] **Narrowed 2026-08-12:** is `libmaestro`'s ANC/EQ control channel the **same** RFCOMM
      channel as the Fast Pair Message Stream (§2.0), using a custom/vendor Message Group
      ID — or a **separate** RFCOMM channel/PSM with its own proprietary envelope (§2
      general layout)? For **ANC specifically**, resolved: it's neither — it's the
      *official* Message Stream's own "Hearable Controls" extension (Group `0x08`), see
      §4.1. Still open for EQ/other settings and for `libmaestro`'s own channel identity —
      see `PROTOCOL.md` §2.3's three-channel table for the current picture.
- [x] **If the magic-byte/length/checksum hypothesis (§2 general layout) is confirmed
      instead: exact magic byte value(s) and length-field endianness — resolved 2026-08-12
      for DLCI 0x02 (`PROTOCOL.md` §2.2a):** no magic byte (standard HDLC `0x7E` flag
      instead); no length field (flag-delimited).
- [x] **Checksum algorithm for that hypothesis — resolved 2026-08-12 for DLCI 0x02
      (`PROTOCOL.md` §2.2a):** CRC-32 (IEEE 802.3/zlib, little-endian), matching Pigweed
      `pw_checksum` exactly; 640/640 sub-frames verified across 3 captures.

### Commands & Schemas
- [ ] Real `.proto` file names/full contents, extracted via `pbtk` against the official
      companion app APK (§3.1) — current names are best-guess placeholders.
- [x] **Channel/Msg ID values for: Set ANC mode, ANC state notification — resolved
      2026-08-12**, see §4.1 above and `PROTOCOL.md` §4.1. Set EQ band values remains open.
- [ ] Confirm the "Ring" / Find My Buds action against the spec's worked example
      (`0x04`/`0x01`) — see the priority tip in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1 Group K.
- [ ] Whether `hardware_status.proto` (§3.1) exists as a genuine Buds-specific schema, or
      whether battery is purely generic Fast Pair Message Stream traffic with no
      Buds-specific protobuf involved at all (§4.3 Option B).
- [ ] The exact Device Information message code for battery within the Message Stream
      "Device Information" group (§4.3 Option B) — firmware version is confirmed as code
      `0x09`; battery's code is not yet confirmed from public docs.
- [ ] Protobuf/message mapping for the backlog toggle features listed in §4.2
      (Conversation Detection, Multipoint, Touch & Hold customization, In-ear detection,
      Volume EQ, Volume Balance, Case Sounds, Head Tracking) — none of these have a known
      opcode or schema yet.

### Behavior
- [ ] Whether Loud Noise Protection and/or Adaptive Audio (§4.2) generate any Bluetooth
      traffic toward the phone at all, or remain fully on-device DSP decisions with no
      wire-visible signal.
- [ ] Whether Adaptive Audio requires the official app to remain active/foregrounded to
      keep functioning, or is a one-time write to the buds — relevant to our own
      `ForegroundService` design (`ARCHITECTURE.md` §2/§6).
- [ ] Confirmed press duration for triggering pairing mode via the case button, distinct
      from the confirmed 30-second factory-reset hold (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §2).
- [ ] Whether captured RFCOMM payload bytes are ever link-layer encrypted in a way
      Wireshark can't automatically decrypt (see `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §7 FAQ) — if so, this is a
      separate problem from frame structure and needs its own resolution path.

### Resolved
- [x] Firmware version baseline for the test device — `release_5.203`, confirmed via
      official app screenshot (§5), 2026-07-30.

---

## 8. Next Steps for Implementation

1. Prioritize resolving the **framing open question** above (Message Stream vs. separate
   proprietary envelope) before writing `FrameDecoder`/`FrameEncoder` — implementing
   against the wrong hypothesis risks code that "works" by accident on some frames and
   silently mishandles others.
2. Once the real `.proto` files are extracted (§7), compile them into Kotlin Lite classes
   via the Gradle Protobuf plugin (`build/generated/source/proto/...`, per
   `ARCHITECTURE.md` §4/§11 — never commit generated code).
3. Implement `FrameDecoder`/`FrameEncoder` in `:data` strictly against whichever framing
   hypothesis reaches 🟢 confidence — not against placeholders.
4. **New architectural consideration:** if the framing question resolves to "both
   channels exist" (i.e. `libmaestro` control commands use a separate envelope from
   generic Fast Pair Message Stream traffic like Ring/battery/device info), `:data` will
   likely need **two separate codecs** rather than one shared `FrameDecoder` — flag this
   for `ARCHITECTURE.md` §5 if/when confirmed, since it changes the Data layer's internal
   structure (though not its external interface to `:domain`).
5. Implement `BudsViewModel` (`:domain`) to map UI intents to the now-confirmed protobuf
   messages, per the pipeline in `ARCHITECTURE.md` §3.
6. Feed any behavioral open questions resolved during implementation (§7 "Behavior")
   back into `ARCHITECTURE.md` where they affect design (e.g. `ForegroundService`
   lifecycle, battery fallback ordering).