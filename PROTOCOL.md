# PROTOCOL.md

Formal, evidence-based specification of the Pixel Buds Pro 2 communication
protocol (`libmaestro` / `libgfps` over Bluetooth Classic RFCOMM, plus relevant
BLE/GATT and Fast Pair mechanisms), as reconstructed from captures, official
Fast Pair specifications, and APK analysis. This is the **source of truth** for
the app's implementation — see `ARCHITECTURE.md` §5 for how the app's code
consumes it.

This document intentionally contains only protocol *facts, hypotheses, and
assumptions* — not implementation details (those live in `ARCHITECTURE.md`).
There is no separate working-notes buffer: new protocol knowledge is recorded
directly in the relevant capture's `CAP-NNN-FINDINGS.md` and promoted straight
into this document once confirmed (`PROJECT_RULES.md` §2) — `PROTOCOL_NOTES.md`
has been retired; its content was consolidated into this document (see §6,
§8's changelog).

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
| Promoted from | capture `CAP-NNN-FINDINGS.md` files directly (formerly via `PROTOCOL_NOTES.md`, retired 2026-08-15) |

### 0.1 Firmware / version compatibility matrix

Extend this table as additional firmware versions are tested — per `AGENTS.md` §4 and
`ARCHITECTURE.md` §8, protocol behavior is not assumed stable across firmware updates,
so each row should be added on its own evidence, not by assuming continuity with the row
above it.

| Firmware version | Known protocol-relevant differences | Source |
|---|---|---|
| `release_5.203` | `ADAPTIVE` ANC mode present; 5-band EQ; L/R/Case independent battery reporting (now understood to likely be the Fast Pair Battery Notification, §4.3 Option A) | `[VERIFIED-LOCAL]` (Screenshot UI analysis, 2026-07-30) |

> **Note (2026-08-14) — four different version-like strings are now documented across captures;
> not yet reconciled into a single confirmed firmware version.** Listed here explicitly, each with
> its source channel, pending a capture that also records the app's own "More settings" firmware
> display (never captured to date) to resolve which (if any) is the actual, user-visible firmware
> version:
> - **`"release_5.203"`** — this table's current baseline, `[VERIFIED-LOCAL]` 2026-07-30 (screenshot
>   UI). Also found on-the-wire, independently, in three sessions on **DLCI 0x08**'s private
>   envelope (Group `0x03` Code `0x02`, `CAP-004-FINDINGS.md` §5a Task 2; byte-for-byte identical
>   to `CAP-002` frame 49028) and in **DLCI 0x08**'s Group `0x02` Code `0x04` value
>   (`CAP-002-FINDINGS.md` §2a, a *third* independent sighting on the same DLCI).
> - **`"Revision 6"`** — found on **DLCI 0x04**, the official Fast Pair Message Stream's Device
>   Information group, Code `0x09` (spec-confirmed field identity = "Firmware version" string,
>   `CAP-002-FINDINGS.md` §3), and independently corroborated via a direct GATT characteristic read
>   (handle `0x0f2a`, `CAP-002-FINDINGS.md` §4). GMS-and/or-app-dependent (unresolved which) —
>   absent in `CAP-004` (`CAP-004-FINDINGS.md` §4a, where GMS was disabled **and** the official
>   app was uninstalled together — a confound not yet isolated), unlike the DLCI-0x08 strings
>   above.
> - **`"cape2_sm"`** (hardware/board codename) and **`"500m"`–`"500p"`** (config-variant
>   identifiers) — found alongside a third `"release_5.203"` sighting on DLCI 0x08's Group `0x02`
>   Code `0x04` (`CAP-002-FINDINGS.md` §2a) — plausibly per-preset/per-profile identifiers, not
>   mapped to any user-visible feature.
>
> These are not competing readings of one field — they are on-the-wire values from at least two
> structurally independent channels/mechanisms (§2.3's table), and `"Revision 6"` specifically
> looks more like a protocol/schema revision number than a firmware build string (`CAP-002-FINDINGS.md`
> §3). Left as 🔴 OPEN QUESTION which (if any) is what the app itself would call "the firmware
> version" until a capture also records that app screen.

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

### 2.2a Hypothesis B, resolved for one specific channel — DLCI 0x02 is Pigweed `pw_hdlc` framing

🟢 **FACT** (2026-08-12, deskresearch task, evidence below) that the RFCOMM
channel documented as "channel 1/DLCI 0x02" in `CAP-001-FINDINGS.md` §2 (and
its `CAP-002`/`CAP-003` counterparts) is **not** an unidentified proprietary
envelope, but a byte-for-byte match to **Pigweed's `pw_hdlc` wire format**
(https://pigweed.dev/pw_hdlc/), the exact transport `qzed/pbpctrl`'s own
project notes describe Maestro as using — see
`https://raw.githubusercontent.com/qzed/pbpctrl/main/docs/Notes.md`,
consulted per `AGENTS.md` §12/`DECISIONS.md` ADR-003 (protocol *knowledge*
only, no code reused): *"The protocol is implemented using the pigweed RPC
library... the RPC messages are wrapped in High-Level Data Link Control
(HDLC) U-frames."*

**Confirmed field-by-field, replacing §2.2's placeholder row for this channel:**

| §2.2 placeholder field | Confirmed value on DLCI 0x02 | Confidence |
|---|---|---|
| Magic bytes | Not a magic byte — standard HDLC flag `0x7E` delimits every frame (start **and** end), with `0x7D`-prefixed byte-stuffing (escaped byte `X` transmitted as `0x7D (X XOR 0x20)`) for any literal `0x7E`/`0x7D` in the frame body — this is why naive byte-splitting on `0x7E` alone previously looked structurally messy | 🟢 High — verified below |
| Payload length | No explicit length field — flag-delimited framing makes one unnecessary (length = distance to the next unescaped `0x7E`) | 🟢 High |
| Channel / Message ID | An HDLC **Address** field, LEB128-varint-encoded (1–3+ bytes) immediately after the opening flag, followed by a single **Control** byte. Two distinct address values observed: `0x00` (both directions) and `0xD180`/53632 (Buds→phone only) — plausibly two multiplexed pw_rpc channels, though Google's exact channel-ID scheme (not the pw_rpc default of `82`) is not otherwise documented | 🟢 High for the field's existence/position; 🟡 Medium for what the two specific values mean |
| Checksum/CRC | **Confirmed as CRC-32 (IEEE 802.3 / zlib polynomial, little-endian byte order)** over the unescaped Address+Control+Data — exactly matching Pigweed's documented use of `pw_checksum`'s CRC-32 frame check sequence | 🟢 High — see verification below |

**Verification method:** exported every RFCOMM payload on this DLCI across `CAP-001`, `CAP-002`,
`CAP-003` (`CAP-004` never opens this channel) via
`tshark -r CAP-NNN-btsnoop_hci.log -Y "btrfcomm.dlci==0x02 and btrfcomm.len > 0" -T fields -e frame.number -e frame.time_epoch -e data.data`,
split each RFCOMM payload on the `0x7E` flag byte, HDLC-unescaped each resulting
sub-frame (`0x7D <X>` → `X XOR 0x20`), then computed `zlib.crc32()` over
everything except the trailing 4 bytes and compared:

```python
def unescape_hdlc(data):
    out = bytearray(); i = 0
    while i < len(data):
        b = data[i]
        if b == 0x7d:
            i += 1
            out.append(data[i] ^ 0x20)
        else:
            out.append(b)
        i += 1
    return bytes(out)

body, trailer = unescape_hdlc(subframe)[:-4], unescape_hdlc(subframe)[-4:]
assert struct.pack('<I', binascii.crc32(body) & 0xffffffff) == trailer
```

**Result: 640/640 sub-frames matched (100%), across all three independent captures, zero
exceptions.** Example (`CAP-001` frame 1348, `00 4b 03 10 15 1d ea 71 de 7d 5e 25 e3 a5 ec 28
f9 67 61 b5`): unescaping the `7d 5e` sequence yields the true byte `0x7e` (`5e XOR 20`), after
which `crc32(00 4b 03 10 15 1d ea 71 de 7e 25 e3 a5 ec 28) = f9 67 61 b5` (little-endian) —
matches the trailing 4 bytes exactly. Before applying the unescape step, only sub-frames that
happened to contain no literal `0x7D`/`0x7E` bytes matched (a subset); after unescaping, the
match rate is unconditional. This is a reproducible, standard-algorithm match, not a coincidental
byte pattern — per `PROJECT_RULES.md` §1's promotion rule (byte-for-byte match to a documented
mechanism, replicated across ≥2 independent captures), **the framing mechanism itself (flag +
escape + LEB128 address + control + CRC-32) is promoted to 🟢 FACT.**

**What remains HYPOTHESIS, not promoted:** that this specific channel *is* `libmaestro`'s settings
channel specifically (as opposed to some other Pigweed-RPC-based Google service) — `pbpctrl`'s
notes describe Maestro's *transport* this way but do not give a DLCI/channel number, and no
Maestro-specific *content* (an ANC-mode-change command, an EQ write, or any decoded pw_rpc
service/method name) has been decoded from this channel's payload bytes yet — the "Rcvd"-direction
payloads decode as protobuf (device serial `"1779298694"` + firmware `"release_5.203"`, per
`CAP-001-FINDINGS.md` §2) and the "Sent"-direction payloads remain opaque 16-byte-ish blocks.
**🟡 HYPOTHESIS (strong):** DLCI 0x02 is `libmaestro`'s pw_rpc channel. Still requires either (a) a
pw_rpc/protobuf schema to decode the opaque "Sent" payloads and recognize an actual ANC/EQ
method call, or (b) a properly isolated capture (Group B, single ANC/EQ action per window)
correlating a specific "Sent" write here with a specific user action, before this can move to 🟢
FACT.

**Per `AGENTS.md` §6 / `ARCHITECTURE.md` §2.1's implementation gate:** this FACT-level framing
confirmation covers the *wire envelope* for one specific channel, not the full §2.3 question below
(which channel(s) carry `libmaestro`'s actual settings commands, and what their protobuf schema
is). `FrameEncoder`/`FrameDecoder` implementation still requires a `DECISIONS.md` ADR recording
this determination before any code is written against it — not added here, since that ADR is a
maintainer sign-off decision per `AGENTS.md` §6's own reasoning ("cheap insurance against an AI
agent... mis-promoting a hypothesis to FACT under implementation pressure"), flagged for the
maintainer rather than added unilaterally by this research pass.

**DLCI 0x08, by contrast, does not match this framing at all** (checked and ruled out, not
assumed): no `0x7E` flag bytes delimit its frames, no escaping, and its own
`[Group:1][Code:1][Length:2B-BE][Value]` envelope (`CAP-001-FINDINGS.md` §2, `CAP-004-FINDINGS.md`
§5a) has an explicit length field HDLC framing doesn't need. It is architecturally a **third**,
independent private protocol — neither the official Fast Pair Message Stream (DLCI 0x04, §2.1,
spec-verified) nor Maestro's Pigweed-HDLC channel (DLCI 0x02, above) — still 🔴 unidentified. See
§2.3 below for how this reframes the central open question.

### 2.3 Open question and resolution path

**Original framing (superseded by the finding above, kept for the record per `PROJECT_RULES.md`
§3's non-destructive-correction convention):** is `libmaestro`'s ANC/EQ control channel the *same*
RFCOMM channel as the Fast Pair Message Stream (§2.1), using a custom/vendor Message Group ID — or
a *separate* RFCOMM channel/PSM with its own proprietary envelope (§2.2)?

**Update (2026-08-12):** this is no longer a clean binary choice — three structurally distinct
RFCOMM sub-protocols are now evidenced across the four captures to date, all coexisting within the
same RFCOMM multiplexer session:

| DLCI | Framing | Status | Content |
|---|---|---|---|
| 0x04 | Official Fast Pair Message Stream (§2.1) | 🟢 FACT (spec-verified, `CAP-002-FINDINGS.md` §3) | Device Information (Group `0x03`), SASS (Group `0x07`), and the officially-documented **Hearable Controls extension (Group `0x08`)** — Get/Set/Notify ANC state, see §4.1 below; GMS-and/or-app-dependent, unresolved which (absent in `CAP-004`, §4a there — GMS disabled and the app uninstalled together, a confound) |
| 0x02 | Pigweed `pw_hdlc` (§2.2a) | 🟢 FACT for the framing; 🟡 HYPOTHESIS (strong) that this is specifically `libmaestro` | Opaque ~16-byte "Sent" blocks (phone→Buds, unresolved content) and protobuf-decodable "Rcvd" blocks (device serial + firmware) — not GMS-dependent (present regardless in every capture that opens it) |
| 0x08 | Private, undocumented `[Group][Code][Length][Value]` envelope, structurally resembling §2.1's shape but with its own Group/Code numbering (`CAP-004-FINDINGS.md` §5a) | 🟢 FACT that it's a real, decodable envelope; 🔴 OPEN QUESTION what protocol it belongs to | One-time capability/setup handshake + a low-rate periodic status ping; not GMS-dependent (`CAP-004-FINDINGS.md` §4b) |

**Resolved for ANC specifically (2026-08-12) — DLCI 0x04's Group `0x08` carries the actual ANC
set/get/notify commands, on the *official* Fast Pair Message Stream, not a proprietary envelope.**
See §4.1 below for the full byte-level evidence: Google's own "Hearable Controls" extension page
documents Group `0x08` Codes `0x11`/`0x12`/`0x13` (Get/Set/Notify ANC state) with an exact,
byte-for-byte match to frames already present in `CAP-001`, including a clean content-and-timing
correlation to 4 of that capture's 6 recorded ANC taps. This resolves this section's original
question for the ANC feature specifically — it does **not** ride on `libmaestro` (DLCI 0x02) or
the DLCI-0x08-private-envelope at all. **What remains open:** whether EQ, touch/head-gesture
config, and other non-ANC settings follow the same pattern (an as-yet-undiscovered official Fast
Pair extension) or genuinely require `libmaestro`'s separate channel (DLCI 0x02) — `libmaestro`'s
own opaque "Sent" payloads there remain undecoded, so this is not yet answered either way for
those features.

**Resolution path (updated):** for EQ/touch/other settings, first check whether any further
official Fast Pair extension pages (beyond Hearable Controls, Device Action, SASS, Device
Information) document a matching Group ID before assuming `libmaestro`/DLCI 0x02 involvement — the
ANC case shows the "proprietary envelope" assumption was wrong for at least one whole feature
area. Where no official extension covers it, decode DLCI 0x02's opaque "Sent" payloads (most
plausibly via a pw_rpc/protobuf service definition, once extracted per §3) or correlate one
against an isolated action (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group B, properly isolated this time).

**Addendum (2026-08-14), deskresearch task — DLCI 0x08 and DLCI 0x02 evaluated explicitly, side
by side, as `libmaestro` candidates against `pbpctrl`'s own transport description (not the
`pbpctrl` code — only its published protocol *knowledge*, per `AGENTS.md` §12/`DECISIONS.md`
ADR-003).** `pbpctrl`'s own project notes
(`https://raw.githubusercontent.com/qzed/pbpctrl/main/docs/Notes.md`, re-fetched and checked
specifically for transport/framing detail) state, verbatim: *"The protocol is implemented using
the pigweed RPC library... the RPC messages are wrapped in High-Level Data Link Control (HDLC)
U-frames."* The document gives **no** magic-byte, length-field, or channel-ID detail beyond that
(confirmed by a targeted re-read — those fields are simply not documented upstream), and the
project's own README gives no RFCOMM channel/UUID specifics either. This means the *only* concrete
transport signature `pbpctrl` actually publishes for Maestro is **"HDLC U-frames wrapping pw_rpc
protobuf messages"** — nothing else in `PROTOCOL.md` §2.2's placeholder field list (magic byte,
explicit length field, checksum) is independently confirmed by the upstream source; §2.2a already
derived the length/checksum mechanics empirically from the capture bytes themselves, not from
`pbpctrl`'s docs.

Checked against that one confirmed signature, the two candidates diverge sharply:

| Field (per §2.2's Hypothesis B placeholder / `pbpctrl`'s stated mechanism) | DLCI 0x02 (§2.2a) | DLCI 0x08 (`CAP-001-FINDINGS.md` §2, `CAP-004-FINDINGS.md` §5a) |
|---|---|---|
| Framing mechanism vs. `pbpctrl`'s stated "HDLC U-frames" | **Match** — `0x7E`-flag-delimited, `0x7D`-escaped, exactly HDLC framing | **No match** — no `0x7E` flag bytes anywhere, no escaping; framed instead by an explicit `[Group:1][Code:1][Length:2B-BE][Value]` header, structurally the *Message Stream* shape (§2.1), not HDLC |
| "Magic bytes" (Hypothesis B placeholder) | No fixed magic value — the HDLC flag `0x7E` itself is shared start/end framing, not a distinguishing sync byte | No magic byte either — but for a different reason: framing is length-delimited (TLV), which doesn't need one, same as the official Message Stream |
| "Payload length" field | Not explicit — implicit via flag-delimiting (HDLC's own mechanism, matching `pbpctrl`'s description) | Explicit 2-byte big-endian length — this is *not* what `pbpctrl` describes for Maestro at all; it is exactly Message Stream §2.1's "Additional Data Length" field shape |
| "Channel / Message ID" | HDLC Address (LEB128 varint) + Control byte — consistent with pw_rpc's own channel/service addressing scheme | Group (1B) + Code (1B) — consistent with Message Stream's Group/Code addressing, not pw_rpc's |
| Checksum | CRC-32 (IEEE 802.3/zlib), confirmed 640/640 — matches Pigweed's own documented `pw_checksum` FCS convention exactly | None found (`CAP-004-FINDINGS.md` §5a's reassembling parser closes cleanly on every session with 0 leftover bytes and no checksum-shaped trailer ever isolated) |

**Conclusion, stated per `PROJECT_RULES.md` §1's promotion rules:** DLCI 0x02 is now a
**direct, mechanism-level match** to the one concrete thing `pbpctrl` actually documents about
Maestro's transport (HDLC U-frames) — this was already known (§2.2a) but had not previously been
stated as a comparison *against a rejected alternative*. DLCI 0x08, by contrast, structurally
matches the *official* Fast Pair Message Stream's TLV shape (§2.1) applied to a private Group
namespace — it does not use HDLC framing at all, and therefore **does not match `pbpctrl`'s own
description of Maestro's transport mechanism**. This is a genuine negative result for DLCI 0x08
as a `libmaestro` candidate specifically, not merely "still unresolved" — recorded here as such
rather than left ambiguous. **Status, per the evidence rules:** DLCI 0x02 = `libmaestro` remains
🟡 HYPOTHESIS (strong) — unchanged in strength from §2.2a, since no Maestro-specific *content*
(an ANC/EQ method call) is decoded yet, so this cannot cross to 🟢 FACT on framing-mechanism
match alone. DLCI 0x08's *identity* remains 🔴 OPEN QUESTION as before, but is now narrowed by a
checked negative: **not** `libmaestro` (mechanism mismatch against the one concrete signature
`pbpctrl` publishes), leaving "a lower-level Nearby/CDM companion-device negotiation independent
of both Fast Pair and Maestro" (`CAP-004-FINDINGS.md` §5a's existing framing) as the leading
remaining candidate for it. §2.3's three-channel table above is not restructured into a binary
choice — it already correctly shows three coexisting channels; this addendum only sharpens which
one the *libmaestro* hypothesis should now concentrate on.

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
- **Opcode/payload structure — 🟢 FACT, resolved 2026-08-12.** Google's official
  Fast Pair ["Hearable Controls"](https://developers.google.com/nearby/fast-pair/specifications/extensions/hearablecontrols)
  extension (`[OFFICIAL-SPEC]`) documents Message Group `0x08` with three codes:

  | Code | Name | Direction | MAC | ACK |
  |---|---|---|---|---|
  | `0x11` | Get ANC state | Seeker → Provider | N | N |
  | `0x12` | Set ANC state | Seeker → Provider | Y | Y |
  | `0x13` | Notify ANC state | Provider → Seeker | N | N |

  **"Set ANC state" (`0x12`) layout** (`[Group:1][Code:1][Len:2BE][Seeker version:1][ANC
  settable modes:1][ANC enabled modes:1][New ANC mode index:1][Reserved:16, present iff
  Len=`0x14`]`), and the mode-index byte uses a one-hot bitmask. **Bit-mapping table (rewritten
  2026-08-14 for clarity — same underlying mapping as before, no conclusion changed):**

  | Spec's own bit name (`hearablecontrols` page, MSB-first numbering) | Standard bit position | Observed hex value | ANC mode |
  |---|---|---|---|
  | `Bit 0` | bit 7 (MSB) | `0x80` | Transparent / Aware |
  | `Bit 1` | bit 6 | `0x40` | Adaptive |
  | `Bit 2` | bit 5 | `0x20` | Off |
  | `Bit 3` | bit 4 | — (not observed) | Reserved |
  | `Bit 4` | bit 3 | `0x08` | ANC / Active Noise Cancelling |
- **Byte-level match against `CAP-001`, exact, no discrepancy:** four `08 12 00 14 01 e8 e8 XX
  <16 reserved bytes>` frames exist in `CAP-001-btsnoop_hci.log` (frames 2039, 2132, 2159, 2193;
  DLCI 0x04). Decoded: `ver=0x01, settable=0xe8, enabled=0xe8, new_mode=`:

  | Frame | Time | `new_mode` byte | Decoded ANC mode | Nearest video-observed tap (`CAP-001-EVENT-NOTES.md`) |
  |---|---|---|---|---|
  | 2039 | 08:51:41.77 | `0x40` (bit6) | Adaptive | "Tap Adaptive" @ 08:51:43 |
  | 2132 | 08:51:48.14 | `0x80` (bit7) | Transparent/Aware | "Tap Transparency (2nd)" @ 08:51:49 |
  | 2159 | 08:51:53.39 | `0x08` (bit3 — note: spec's `Bit 4`) | ANC/Active Noise Cancelling | "Tap Noise Cancellation" @ 08:51:54 |
  | 2193 | 08:51:59.20 | `0x20` (bit5) | Off | "Tap Off (2nd)" @ 08:52:00 |

  All four decoded modes match their nearest tap **by content**, in the correct **sequence**, each
  within ~1–1.5s of the video-observed tap (well inside the ±1s 1fps-video-sampling uncertainty
  already documented for this capture) — this is not a spec-shape resemblance, it is a confirmed
  identification with an internal cross-check the spec page itself does not provide. Each `0x12`
  frame is immediately followed (~60–110ms later) by `ff 01 00 06 08 12 01 e8 e8 XX` — the
  documented ACK shape (§2.1), consistent with the spec's `ACK: Y` column. This satisfies
  `PROJECT_RULES.md` §1's promotion bar on two independent grounds at once (official spec
  byte-match **and** internal content/timing cross-check within one capture).
- **Open sub-question, not resolved:** `CAP-001`'s first two ANC taps (Transparency @ 08:51:32,
  Off @ 08:51:39) have **no** corresponding `0x12` frame anywhere in the log — possibly UI-state
  realization rather than genuine user-initiated sets (the ANC row was still greyed out until
  shortly before, per `CAP-001-EVENT-NOTES.md`), not yet confirmed.
- **"Notify ANC state" (`0x13`)**: `Provider → Seeker`, layout `[Group:1][Code:1][Len:2BE=0004]
  [Version:1][UI toggles:1][Settable toggles:1][Current state:1]`, same one-hot bit layout for
  the "Current state" byte. Matches the `08 13 00 04 01 e8 e8 XX` frames independently documented
  in `CAP-001-FINDINGS.md` §5 (26 occurrences across the day this capture's log spans) — a
  periodic/on-change status report, not itself a command.
- **Sent to**: RFCOMM Fast Pair Message Stream, DLCI 0x04 (§2.1/§2.3) — **not** `libmaestro`'s
  Pigweed-HDLC channel (DLCI 0x02, §2.2a) and **not** the private DLCI-0x08 envelope; both were
  live candidates before this resolution.
- **Expected response**: ACK (`0xFF 0x01 0x00 0x06 <echoed group/code/data>`), 🟢 FACT, see above.
- **Status**: 🟢 FACT (opcode, payload layout, and the "set" direction's semantics are all
  confirmed against official documentation and cross-validated within `CAP-001`); recorded in
  `DECISIONS.md` ADR-009. **`FrameEncoder` implementation for this command is blocked pending
  `CAP-006`** (ADR-009) — the FACT status above does not by itself establish that every ANC tap
  reliably produces a command frame; see the open sub-question below and `CAP-001-FINDINGS.md`
  §5's risk flag.
- **Evidence**: UI presence (`SCREENSHOTS_PIXEL_BUDS_APP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
  §1); official spec (`developers.google.com/nearby/fast-pair/specifications/extensions/hearablecontrols`,
  consulted 2026-08-12); `CAP-001` frames 2039/2132/2159/2193 (`Set`) and 2041/2134/2162/2195
  (ACK), cross-referenced against `CAP-001-EVENT-NOTES.md`'s tap timeline.
- **Verified with experiment**: none formally logged in a `CAP-NNN-FINDINGS.md` yet — this is a
  deskresearch correlation against an existing capture, not a fresh, purpose-built experiment;
  recommended as a cheap confirmation step (repeat with isolated single taps, per
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group B) before treating the mode-index bit mapping as final for
  implementation.

### 4.2 Equalizer (EQ)

- **Feature confirmed present**: 5-band custom EQ (Low Bass, Bass, Mid, Treble,
  Upper Treble) and presets (Standard/Default, Bass Boost/Heavy Bass, Bass
  Reduction/Light Bass, Balanced, Vocal Boost, Clarity, Last Saved). Status: 🟢
  FACT (UI presence).
- **Opcode/payload structure**: not yet extracted from an official spec, but **Added 2026-08-15**:
  `CAP-005-FINDINGS.md` (Group T, isolated `EQP-002`/`EQS-004` capture) proposes a first candidate
  wire format on DLCI 0x02 (`libmaestro`'s Pigweed `pw_hdlc` channel, §2.2a) — an HDLC frame whose
  payload nests down to a 5×`float32` band-gain quintet, with only the touched band's value
  changing between a preset tap and a slider drag. **🟡 HYPOTHESIS only** (single capture, field-
  to-band mapping inferred from one changed field) — see that file's §5–§6 for the full decode and
  open questions, also tracked in §6 below.
- **Sent to / expected response**: same open questions as §4.1.
- **Status**: 🔴 unconfirmed at the byte level (candidate 🟡 HYPOTHESIS now exists, not yet FACT).
- **Evidence**: `SCREENSHOTS_PIXEL_BUDS_APP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §1,
  `captures/CAP-005-2026-08-15_15-02-31_15-03-45-Group_T/CAP-005-FINDINGS.md`.
- **Verified with experiment**: `CAP-005` (Group T) — see `CAP-005-FINDINGS.md`.

### 4.3 Battery status (Left / Right / Case)

Five candidate mechanisms, in priority order for implementation. **These do
not all share one update model** — Options A/B (the Fast Pair mechanisms, on
the official Message Stream, DLCI 0x04) are event-driven (sent on connect or
on value change, per the official spec); Option C (HFP, on DLCI 0x09) instead
**pushes periodically** regardless of whether the value changed —
`CAP-001-FINDINGS.md` §3 observed `AT+BIEV=2,100` repeating on a roughly
6–7 second cadence throughout the session, not just on change. An
implementation that treats all four mechanisms as equally event-driven would
either miss HFP's periodic updates (if it only listens for state transitions)
or busy-poll unnecessarily on the Fast Pair mechanisms (if it treats their
event-driven pushes as periodic) — see `ARCHITECTURE.md` §6 on
event-observation coroutines.

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

#### Option C — HFP AT commands (`AT+BIEV` HF Indicator #2, `AT+CIND` `battchg`)

- **Status**: 🟢 FACT — confirmed active for this device (`CAP-001-FINDINGS.md`
  §3), on channel 4 / **DLCI 0x09**. Two simultaneously-active HFP mechanisms
  observed: `AT+BIEV=2,<0-100>` (HF Indicator #2, Bluetooth-spec-assigned as
  Battery Level) and the older `AT+CIND?` `battchg` indicator (0–5 scale) —
  these **disagreed** in `CAP-001` (`battchg=3` ≈60% vs. `AT+BIEV=2,100` =
  100%, at the same moment) and it's still 🔴 open which one (if either)
  tracks real changes accurately (`PROTOCOL.md` §6, `BATT-006`).
- **Update model — periodic, not event-driven**: `AT+BIEV=2,100` repeated on
  a roughly 6–7 second cadence throughout `CAP-001`'s session regardless of
  whether the value changed (frames 1236–2269, timestamps 08:51:14.106
  through 08:51:52.148) — unlike Options A/B above, this is not a
  connect-or-on-change push. An app relying on this mechanism should expect
  (and can rely on) a steady stream of updates, not just change notifications.
- Neither HFP indicator distinguishes Left/Right/Case — both report a single
  aggregate value, unlike Option A's separate L/R/Case fields.

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

Full step-by-step sequence not yet captured end-to-end for the RFCOMM
profile/Message-Stream/battery/command portions (steps 3–6 below remain ⚪
ASSUMPTION). The classic BR/EDR link establishment portion (steps 1–2's
link-layer mechanics), however, is now 🟢 FACT — see §5.1. Expected overall
shape, pending confirmation (advertising → scan/CDM pairing → RFCOMM connect →
Message Stream/`libmaestro` handshake → first battery notification → first
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

### 5.1 Classic BR/EDR link establishment — 🟢 FACT, promoted 2026-08-15

Three independent captures now agree on this state machine's shape —
`CAP-001` (reconnect to an already-bonded device, stored link key), `CAP-002`
(fresh pairing via the official app, first attempt succeeds), `CAP-003`
(fresh pairing via nRF Connect, first attempt succeeds). Two converging paths
depending on whether a stored link key exists:

**Fresh pairing (no stored key) — `CAP-002`/`CAP-003`:**

```
Delete stored link key (if any) → Create Connection → Connect Complete
  (status 0x00) → Link Key Request → Negative Reply (no prior bonding
  material) → IO Capability Request/Reply/Response (SSP negotiation) →
  Simple Pairing Complete → Link Key Notification (new key stored) →
  Authentication Complete → Set Connection Encryption → Encryption Change
```

- **`CAP-002`** (official app): frames 653–734, 17:05:26.717–33.721. The
  ~6.4s gap between IO Capability Response and Simple Pairing Complete
  matches the on-screen permission dialog being shown/confirmed, not a
  passkey step — no passkey digits are ever shown to the user; SSP here is a
  silent cryptographic exchange behind a permission-confirmation UI.
- **`CAP-003`** (nRF Connect): frames 1621, 1687–1756, 20:59:38.320–39.876.
  Same shape, but only a ~0.7s IO-Capability-to-Complete gap instead of
  `CAP-002`'s ~6.4s, since nRF Connect shows no confirmation dialog to wait
  on — this cross-check confirms the 6.4s gap above is UI dwell time, not
  part of the protocol's own timing. `CAP-003` also shows the BLE (LE) link
  established ~0.4s *before* the classic connection attempt, consistent with
  Fast Pair's BLE-first design (classic pairing triggered from the BLE side).

**Reconnect (stored key exists) — `CAP-001`:**

```
Create Connection (may require multiple attempts — CAP-001 needed 3; attempt
  1 failed with Page Timeout before the case was fully open on camera) →
  Connect Complete (status 0x00) → Link Key Request → Link Key Request Reply
  (stored key, no SSP negotiation) → Authentication Complete → Set Connection
  Encryption → Encryption Change
```

- **`CAP-001`**: frames 732–917, 08:51:01.981–12.208. No IO Capability/SSP
  exchange at all — the stored key from a prior pairing is reused directly.
  A BLE (LE) link to the same peer was independently already established
  earlier (frame 290, 08:50:36.27), before the case was even open on camera
  — see `CAP-001-FINDINGS.md` §6 for the still-open question this raises
  about exactly when that BLE association was formed relative to the
  on-screen "Forget" tap (tracked as planned capture `CAP-013`).

**Common tail, both paths:** `Authentication Complete` → `Set Connection
Encryption` → `Encryption Change`, converging to the same encrypted classic
link regardless of which path reached it.

**Not covered by this promotion — still ⚪ ASSUMPTION:** the RFCOMM
channel-opening sequence, the Message Stream/`libmaestro` handshake ordering,
and exactly when the first battery notification/app command arrives relative
to the classic link completing (steps 3–6 in the diagram above). Only the
classic BR/EDR link-establishment mechanics (steps 1–2) are promoted here.

**Status**: 🟢 FACT for classic BR/EDR link establishment (§5.1, three
independent captures); ⚪ ASSUMPTION for the RFCOMM/Message-Stream/battery/
command portions (steps 3–6); 🟢 FACT for step 5's specific behavioral outcome
(battery notification on reconnect), per `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §3.
**Evidence**: §5.1 above for the classic-link portion (`CAP-001` frames
732–917, `CAP-002` frames 653–734, `CAP-003` frames 1621/1687–1756); steps 3–6
still need a full connection sequence captured end-to-end (see
`CAPTURE_BLUETOOTH_HCI_SNOOP.md`).

## 6. Open questions

Consolidated from `PROTOCOL_NOTES.md` §7 — check items off with a date and a
one-line pointer to the evidence when resolved; add new items here rather than
leaving them buried in prose elsewhere.

### Framing

- [ ] **Narrowed 2026-08-12, not fully resolved:** is `libmaestro`'s ANC/EQ
      control channel the same RFCOMM channel as the Fast Pair Message Stream
      (§2.1), using a custom/vendor Message Group ID — or a separate RFCOMM
      channel/PSM with its own proprietary envelope (§2.2)? Three coexisting
      sub-protocols are now evidenced (§2.3's table: DLCI 0x04 official
      Message Stream, DLCI 0x02 Pigweed `pw_hdlc`, DLCI 0x08 a third private
      envelope) — none yet confirmed to carry the actual ANC/EQ *command*.
      `FrameDecoder` still cannot be implemented until one of these is
      confirmed as the command channel and a `DECISIONS.md` ADR records it
      (`AGENTS.md` §6).
- [x] **If §2.2 is confirmed instead: exact magic byte value(s) and
      length-field endianness — resolved 2026-08-12 for DLCI 0x02 specifically
      (§2.2a):** no magic byte — standard HDLC `0x7E` flag delimits frames; no
      explicit length field — flag-delimited instead. Does not apply to DLCI
      0x08, which uses a genuine 2-byte big-endian length field of its own
      (§2.2a).
- [x] **Checksum algorithm for §2.2, if confirmed — resolved 2026-08-12 for
      DLCI 0x02 specifically (§2.2a):** CRC-32 (IEEE 802.3/zlib polynomial,
      little-endian), matching Pigweed's `pw_checksum` module exactly;
      verified at 640/640 (100%) sub-frames across three independent
      captures. DLCI 0x08 still has no confirmed checksum (unchanged, 🔴).
- [x] **Added 2026-08-14:** DLCI 0x08 checked directly against `pbpctrl`'s
      published Maestro transport description (HDLC U-frames) and found to
      *not* match (no `0x7E` flag framing at all) — a checked negative
      result narrowing, not resolving, its identity. See §2.3's 2026-08-14
      addendum. DLCI 0x02 remains the sole candidate that matches
      `pbpctrl`'s stated mechanism.
- [ ] **Added 2026-08-15: wire-baseline firmware version, distinct from the
      confirmed UI-baseline.** §0.1 documents `release_5.203` as the
      `[VERIFIED-LOCAL]` UI-baseline (official app screenshot, 2026-07-30),
      but four different version-like strings exist on the wire and are not
      yet reconciled: `"release_5.203"` itself (found on DLCI 0x08's
      still-unidentified private envelope — its semantic meaning there isn't
      confirmed, only the string match), `"Revision 6"` (DLCI 0x04's
      *official, spec-confirmed* "Firmware version" field — a completely
      different string format), and `"cape2_sm"`/`"500m"`–`"500p"`
      (board codename / config variant, not version candidates). **Do not
      treat `release_5.203` as confirmed on the wire in the same sense it's
      confirmed in the UI** — see §0.1 for the full breakdown. Needs a
      capture that also records the app's firmware-display screen at the
      same moment as the capture to resolve which wire value (if any)
      corresponds to it.

### Commands & schemas

- [ ] Real `.proto` file names and full contents, extracted via `pbtk` against
      the official companion app APK (§3) — current names are placeholders.
- [x] **Channel/Msg ID values for: Set ANC mode, ANC state notification —
      resolved 2026-08-12, see §4.1.** Fast Pair Message Stream Group `0x08`
      ("Hearable Controls" extension), Codes `0x11`/`0x12`/`0x13`
      (Get/Set/Notify), spec- and capture-confirmed. Set EQ band values
      remains open (§4.2) — no matching official extension found yet.
- [ ] Confirm the Ring / Find My Buds action against the spec's worked example
      (§4.4).
- [ ] Whether `hardware_status.proto` exists as a genuine Buds-specific schema,
      or whether battery is purely generic Fast Pair Message Stream traffic
      with no Buds-specific protobuf involved (§4.3 Option B).
- [ ] Exact Device Information message code for battery within the Message
      Stream group (§4.3 Option B) — firmware version is confirmed as code
      `0x09`; battery's code is not yet confirmed.
- [ ] Protobuf/message mapping for all features listed in §4.5.
- [ ] Added 2026-08-14: Groups `0x01`/`0x02`/`0x05`/`0x09` on DLCI 0x08's private envelope are
      structurally confirmed as genuine, self-contained, standalone Message-Stream-shaped groups
      (not a reassembly artifact — `CAP-004-FINDINGS.md` §5a Task 3, checked across all four
      captures' full DLCI-0x08 byte streams with zero parse errors/leftover bytes). Their
      *semantic* meaning remains 🔴 open — no official Fast Pair extension page documents these
      group numbers under DLCI 0x08's private numbering (`CAP-002-FINDINGS.md` §2a Task 2).
- [ ] Added 2026-08-14: DLCI 0x08's own purpose/ownership as a whole channel is still 🔴 open.
      Ruled out as `libmaestro` specifically (§2.3's 2026-08-14 addendum — no HDLC framing, unlike
      the one concrete transport signature `pbpctrl` documents for Maestro). Leading remaining
      candidate: a lower-level Nearby/CDM companion-device negotiation, independent of both Fast
      Pair (DLCI 0x04) and `libmaestro` (DLCI 0x02) — not confirmed.
- [ ] Added 2026-08-14: EQ's opcode/channel is explicitly **not** assumed to sit alongside ANC's
      (DLCI 0x04 Group `0x08`) — that assumption held only while ANC's own channel was unresolved.
      See `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group T (new top-priority capture target) and §4.2
      above.
- [ ] **Added 2026-08-15: possible application-layer AES-128 encryption of DLCI 0x02's opaque
      "Sent" blocks.** §2.2a describes the phone→Buds "Sent" blocks on this channel as
      "opaque ~16-byte blocks, unresolved content" — 16 bytes is exactly the AES block size, and
      Fast Pair's own Key-based Pairing procedure already uses AES-128 (ECB) elsewhere in the
      broader Fast Pair ecosystem, making an AES-128-encrypted application-layer payload here a
      plausible explanation for why these blocks have resisted structural decoding (encrypted
      data is indistinguishable from opaque/random bytes). **Not established either way** — this
      is distinct from Bluetooth link-layer encryption, which per
      `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §7's FAQ is unlikely to still be present at the HCI-snoop
      capture boundary; an application-layer scheme on top of the already-plaintext HCI capture
      is a different, live possibility. If confirmed, this would materially affect
      `FrameDecoder`'s design for this channel (decryption step required before payload parsing)
      — see `ARCHITECTURE.md` §5.
- [ ] **Added 2026-08-15, from `CAP-005-FINDINGS.md` (Group T, EQ isolation) §6 — carried over per
      this session's task instructions.** A properly isolated capture (`EQP-002` preset tap,
      `EQS-004` Bass slider drag, ≥10s gaps) found DLCI 0x02's `Sent` direction is silent all
      session except for exactly three 45-byte payloads landing precisely at the two EQ actions
      (plus a later `Save`-button tap) — the first EQ-attributable, structurally-decoded content on
      any channel (`CAP-005-FINDINGS.md` §5, all 🟡 HYPOTHESIS, not promoted to FACT). Distinct from
      the "~16-byte opaque Sent blocks" the AES-128 item above describes — this is a separate,
      larger (45-byte), now partially-structured payload from the connection-setup handshake
      blocks, not a resolution of that item either way. Specific open items this raised, not yet
      resolved:
      - Which of the 5 decoded `float32` fields maps to which of the 5 UI sliders is inferred from
        only one slider (Bass) having been moved this session — needs a capture isolating a
        *different* single slider to confirm.
      - Whether the outer field number (`16` during preset-tap/drag, `18` at the `Save` tap) means
        "preview" vs. "save", or something else — needs a capture that drags a slider and
        deliberately never taps `Save`.
      - ~13 bytes of apparent `call_id`/correlation data (payload offset 1–12, echoed back
        verbatim by the Buds) are present but undecoded.
      - Whether DLCI 0x02's field-16/18 pair is EQ-specific or a general-purpose
        `libmaestro` "apply/save" pair also used by ANC/other settings — needs a differently
        isolated capture (e.g. ANC-only) to check.

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
- [ ] Added 2026-08-14: why HFP AT-command traffic never recurs after `CAP-001`'s own handshake —
      confirmed as a genuine negative (zero `AT+` traffic anywhere else across a full 8+ hour
      shared log spanning multiple reconnects, `CAP-002-FINDINGS.md` §5), but the underlying
      *reason* (per-pairing SLC setup once only? requires an actual call to re-trigger?) is still
      open. `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group V (new) targets this directly.
- [ ] Added 2026-08-14: DLCI 0x08 Group `0x04` Code `0x12`'s alternating value — event-driven
      (breaks/skips on a real physical event) or a free-running liveness/sequence-parity counter?
      🟡 HYPOTHESIS (free-running) per `CAP-004-FINDINGS.md` §5a Task 5's irregular-interval,
      near-perfect-alternation observation; not yet tested against a bracketed physical event.
      `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group U (new) targets this directly.
- [ ] Added 2026-08-14: live GATT primary-service discovery requires stronger cache-busting than
      bond removal — confirmed as a genuine requirement, not an assumption: three independent
      captures (`CAP-002`, `CAP-003`, `CAP-004`) all failed to trigger a live `Read By Group Type`
      response against the Buds despite bond removal beforehand in two of them
      (`CAP-003-FINDINGS.md` §1, `CAP-004-FINDINGS.md` §6). `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
      Group W (new) proposes two untried, stronger candidates.

### Resolved

- [x] **UI-baseline firmware version** for the test device — `release_5.203`,
      confirmed via official app screenshot, 2026-07-30. This is what the
      app's About/settings screen displays — **not** the same thing as
      confirming what appears on the wire (see the "wire-baseline" item under
      Framing, added 2026-08-15).

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
| 2026-08-12 | Added §2.2a: DLCI 0x02's framing confirmed as Pigweed `pw_hdlc` (flag/escape/LEB128-address/control/CRC-32), matching `pbpctrl`'s own Maestro-transport notes; promoted to 🟢 FACT for the framing mechanism (640/640 sub-frames verified across 3 captures). Restructured §2.3's binary framing question into a three-channel table (DLCI 0x04/0x02/0x08). **§4.1 ANC mode promoted to 🟢 FACT**: Google's official "Hearable Controls" Fast Pair extension (Message Group `0x08`, Codes `0x11`/`0x12`/`0x13`) matches `CAP-001` byte-for-byte, including a 4/4 content+timing correlation against that capture's own recorded ANC taps — resolves the project's original highest-priority open command question, on the *official* Message Stream (DLCI 0x04), not `libmaestro`. Updated §6 Framing and Commands checklists accordingly. `libmaestro` (DLCI 0x02) and the private DLCI-0x08 envelope's command content, and EQ/other settings, remain unconfirmed — `FrameEncoder`/`FrameDecoder` implementation gate (`AGENTS.md` §6) remains closed pending a `DECISIONS.md` ADR | Claude (AI), deskresearch task, not yet reviewed by maintainer |