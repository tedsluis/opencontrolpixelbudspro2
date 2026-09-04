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

Status legend (this is the project-wide canonical legend — every other document's
legend, e.g. `PROJECT_RULES.md` §1 rule 1, `REVERSE_ENGINEERING.md`,
`DESKRESEARCH_FINDINGS.md`, and every `CAP-NNN-FINDINGS.md`, must be consistent
with this one, not the reverse):

- 🟢 **FACT** — observed and repeatedly confirmed (multiple captures/experiments,
  or directly stated in Google's official Fast Pair specification as the
  documented mechanism *and* confirmed as what the Buds Pro 2 use).
- 🟡 **HYPOTHESIS** — observed or plausible, not yet independently confirmed
  against our own capture.
- ⚪ **ASSUMPTION** — not yet tested, assumed based on comparable/official
  protocols or an older Pixel Buds generation.
- 🔴 **OPEN QUESTION** — genuinely unresolved: no specific hypothesis or working
  assumption exists yet, only an identified gap. **Formally documented
  2026-08-20** — this tier was already in heavy, load-bearing use throughout
  this document's body (16 occurrences) and in every `CAP-NNN-FINDINGS.md`
  file's own legend before it was added here; this entry reconciles the
  written rule with the practice that already existed, per `PROJECT_RULES.md`
  §1 rule 1's matching update.

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
>
> **Documentation-gap fix (2026-09-04, `CAP-036-FINDINGS.md` §12.3) — mechanical, no new sign-off
> needed:** DLCI 0x04's own Device Information group (Group `0x03`, distinct from DLCI 0x08's
> private numbering above) has two further codes that reached 🟢 FACT in `CAP-002-FINDINGS.md` §3
> back on 2026-08-10 (direct fetch of Google's official `deviceinformation` spec page, worked-example
> byte match) but were never copied into this document — fixed now: **Code `0x01` = "Model ID"**,
> value `da 2d b1`, constant across every session checked to date (`CAP-001`, `CAP-002`, `CAP-010`,
> `CAP-036`). **Code `0x02` = "BLE address updated"**, a 6-byte value that rotates every session
> (`77:96:2c:96:68:1c`, `53:0c:b4:c8:06:3d`, `75:51:27:4f:ae:59`, `51:70:22:b8:72:2f`,
> `5e:6a:14:ce:17:9f`, `44:d6:94:50:f0:4e` — six distinct sessions, six distinct values) —
> consistent with a rotating private/resolvable BLE address, exactly as its name states. `CAP-036`
> additionally cross-checked its own session's value against live BLE advertising traffic in the
> same log and found it broadcasting a Fast Pair (`0xFE2C`) advertisement 407 times — see §4.3
> Option A's device-attribution note below; this is 🟡 HYPOTHESIS (one session), not itself part of
> this documentation-gap fix.
>
> **Update (2026-08-21), `CAP-023` — the capture this note asked for now exists.** Device details →
> More settings → Firmware update shows **"Device firmware version": Left earbud `release_5.203`,
> Right earbud `release_5.203`, Case `release_5.203`** — video-confirmed on-screen, same session,
> at 08:24:17. The DLCI 0x08 private-envelope string documented above (Group `0x03` Code `0x02`)
> was independently present in this same session's connection-time handshake (frame 849,
> 08:23:46.038, **before** the screen was even opened) with the byte-identical value
> `"release_5.203"`. This is the first same-session, on-screen-confirmed match this project has
> recorded — 🟢 **FACT, promoted 2026-08-23** (maintainer sign-off obtained per `AGENTS.md` §6;
> see `DECISIONS.md` ADR-012): `"release_5.203"` is what the app calls "the firmware version," and
> `"Revision 6"` is not surfaced anywhere in the app's own UI. **Still open:** what `"Revision 6"`
> itself represents, if not the user-facing firmware version. Also established as a **clean
> negative finding**: tapping the manual "Up to date" check and opening this screen produce **zero**
> RFCOMM traffic — the display is read from already-cached connection-time data, not queried live.
> See `CAP-023-FINDINGS.md` §3–§4.

> **Note (2026-08-28), 2026-08-28 project-wide audit finding `XC-03` — an unrelated version
> identifier has never been reconciled against the baseline above.** `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
> §6 states that `ANC-003` (Adaptive)/`HEAD-*`/`LOUD-001` were "added in firmware 4.467," using a
> different-looking version scheme from this project's own confirmed `"release_5.203"` baseline
> above. ⚪ **ASSUMPTION, not yet checked either way:** whether `release_5.203` numerically
> satisfies a `4.467` threshold, or whether the two are even comparable version schemes at all — no
> capture or external source consulted by this project states the relationship. **Not currently
> blocking anything**: all three gated features are confirmed present and working on this
> project's test device regardless (`ANC-003`/Adaptive since `CAP-001`; `HEAD-*` since `CAP-020`) —
> this is a latent documentation gap, flagged so a future firmware-compatibility check
> (`ARCHITECTURE.md` §8) doesn't silently assume a relationship that was never verified.

## 1. Transports overview

| Transport | Used for | Status |
|---|---|---|
| Bluetooth Classic RFCOMM (`BluetoothSocket`, SPP-style) | Three coexisting, independently-framed DLCIs (see §2.3's table): the official Fast Pair Message Stream (DLCI 0x04, 🟢 FACT, carries the confirmed ANC command), `libmaestro`'s candidate Pigweed `pw_hdlc` channel (DLCI 0x02, 🟡 HYPOTHESIS that this is specifically `libmaestro`), and a third, still-unidentified private envelope (DLCI 0x08, 🔴 open identity) | 🟢 FACT that these are three separate channels, not one shared channel — see §2.3 |
| BLE advertisement | Fast Pair "Battery Notification" extension — passive battery status broadcast, no active connection required | 🟢 FACT (mechanism, official spec); 🟡 HYPOTHESIS (confirmed as what the Buds Pro 2 send) |
| BLE GATT | Possible standard Battery Service (`0x180F`) for the case; otherwise not confirmed to be used for control | ⚪ ASSUMPTION |

## 2. RFCOMM framing (three structurally distinct sub-protocols: DLCI 0x02 / 0x04 / 0x08)

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

> **Correction (2026-08-23):** the byte sequence above does **not** match the official spec's
> own worked example — caught during an external audit pass that fetched
> `developers.google.com/nearby/fast-pair/specifications/extensions/acknowledgement` directly
> rather than relying on this document's prior (miscited) restatement. The spec's actual worked
> example for acknowledging a received ring action (`0x04010002013C`) is an ACK of
> **`0xFF 0x01 0x00 0x04 0x04 0x01 0x01 0x3C`** — length `0x0004`, **four** data bytes (echoed
> group `0x04`, echoed code `0x01`, plus two additional bytes `0x01 0x3C`), not the six-byte,
> two-data-byte value stated above. The framing structure itself (Group/Code/Length/Data) is
> unaffected — only this specific worked-example citation was wrong. See §4.4 below and
> `CHANGELOG.md`'s 2026-08-22/23 entry for the full correction and its effect on the Find My Buds
> ACK-variant comparison.

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
| Channel / Message ID | An HDLC **Address** field, LEB128-varint-encoded (1–3+ bytes) immediately after the opening flag, followed by a single **Control** byte. In `CAP-001`–`CAP-003`/`CAP-006`/the 11:42 `CAP-010` session, exactly two distinct address values are observed: `0x00` (both directions) and `0xD180`/53632 (Buds→phone only). **Not a fixed/exhaustive set, per a 2026-08-17 deskresearch pass (§6, `DESKRESEARCH_FINDINGS.md`):** `CAP-005`/`CAP-007` additionally show `0x1e80`/`0x2680` (Sent) answered by `0xe980` (Rcvd), appearing specifically around connection-(re)open events and carrying the same content as the `0x00`/`0xD180` pair — HYPOTHESIS that this field is a per-connection-negotiated pw_rpc handle, not a small fixed set | 🟢 High for the field's existence/position; 🟡 Medium for what any specific value means, and now also for whether the address *set itself* is fixed |
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

**Channel ownership — promoted to 🟢 FACT (2026-08-30, maintainer sign-off, `DECISIONS.md`
ADR-018, Option 2 — a narrow promotion, see that ADR's "What this new evidence is, precisely — and
what it is not"):** this channel is the Pixel Buds companion app's **own internal RFCOMM socket**,
not some other/generic Pigweed-RPC-based Google service. Evidence, from an AI-run APK
keyword-search pass (`DECISIONS.md` ADR-017's mechanical-assistance boundary;
`REVERSE_ENGINEERING.md`'s `fzd`/`gbm`/`gau`/`gbd`/`fxm`/`fsz`/`fut`/`fux`/`ghd`/`goq` entries,
`v1.0.955078536-10253511`) cross-checked against captures already in `captures/`: the app's own
decompiled code (`gbm.java:35-43`) selects between two internal RFCOMM sockets by checking which of
two 128-bit SDP UUIDs (`fzd.java:9`) is present in the discovered service set, logging `"Provide
pigweed internal rfcomm socket"` for UUID `25e97ff7-24ce-4c4c-8951-f764a708f7b5`. That exact UUID's
SDP Service Search Attribute Response resolves to **RFCOMM server channel 1** in three independent
captures (`CAP-001` frame 1327 @ 42.534s, `CAP-002` frame 1327 @ 42.534s, `CAP-032` frame 1632 @
104.943s), and `tshark`'s own `btrfcomm.dlci` field reads **`0x02`** for every frame once that
channel opens in each of them (`CAP-001` frame 1334 @ 42.545s; `CAP-032` frame 1645 @ 105.173s) — a
direct wire reading, not the `2×channel` arithmetic applied blind. **Independently corroborated a
fourth time (2026-08-30 audit finding, `CAP-033-FINDINGS.md` §3):** `CAP-033`'s own SDP browse (frame
1279, within the 15:18:09.417–15:18:18.822 window) returns a full named-service table naming this
same UUID's service **"MAESTRO APP"** on RFCOMM channel 1 — the first time this correlation is
confirmed from a human-readable SDP service-name string rather than only the raw UUID/channel-number
match above. The same code path is where the
app's own `maestro_pw.*` pw_rpc services are dispatched (`Maestro`, `HeadGesture`, `EartipFitTest`,
`Dosimeter`, `JitterBuffer`, `Multipoint`, `DynamicServerConfigService`), including a `WriteSetting`
call (`fsz.java:75`) confirmed via a surviving Kotlin function-reference metadata string
(`fsz.java:223`) to route through `dev.pigweed.pw_rpc.MethodClient` against the app's own
`com.google.android.apps.wearables.maestro.companion.pw.hdlc.RouteProto$Route`.

**What remains HYPOTHESIS, not promoted by ADR-018:** that this channel's *opaque payload content*
specifically carries `libmaestro`'s ANC/EQ/settings-write commands (as opposed to, say, only
diagnostic/telemetry RPCs riding the same socket) — no Maestro-specific *content* (an
ANC-mode-change command, an EQ write, or any decoded pw_rpc service/method name applied to a real
value) has been decoded from this channel's payload bytes yet — the "Rcvd"-direction payloads
decode as protobuf (device serial `"1779298694"` + firmware `"release_5.203"`, per
`CAP-001-FINDINGS.md` §2) and the "Sent"-direction payloads remain opaque 16-byte-ish blocks.
**🟡 HYPOTHESIS (strong):** DLCI 0x02's Sent-direction payloads specifically carry `libmaestro`'s
settings-write commands. Still requires either (a) a pw_rpc/protobuf schema to decode the opaque
"Sent" payloads and recognize an actual ANC/EQ method call, or (b) a properly isolated capture
(Group B, single ANC/EQ action per window) correlating a specific "Sent" write here with a specific
user action, before this can move to 🟢 FACT.

**Per `AGENTS.md` §6 / `ARCHITECTURE.md` §2.1's implementation gate:** the FACT-level framing
confirmation above, together with the channel-ownership FACT confirmed by ADR-018, still does not
cover the full §2.3 question below (what protobuf schema `libmaestro`'s actual settings commands
use, and which Sent-direction bytes correspond to which command). `FrameEncoder`/`FrameDecoder`
implementation for the *settings-command content* still requires its own `DECISIONS.md` ADR
recording that determination before any code is written against it — ADR-018 only settles channel
ownership, not payload semantics, per that ADR's own explicit scope note.

**Update (2026-08-30) — the protobuf schema question above is now answered, for the specific fields
tested: it is `libmaestro`'s own `WriteSetting` request type (Java class `qhr`), recovered by APK
static analysis and byte-confirmed against 2 sampled wire fields.** 🟢 **FACT, promoted 2026-08-30**
(maintainer sign-off, `DECISIONS.md` ADR-019): re-decoding `CAP-020` frames 1741/1935 (already
identified as `field5{field4{...}}` per `ADR-013`) one level deeper than originally notated shows the
"..." is exactly `qhr`'s own protobuf oneof, addressed via standard wire-format tags — confirmed for
`qhr` field 4 (frame 1741, value 1) and field 29 (frame 1935, value 2), both matching the
independently-recovered app schema with no discrepancy. See §4.5.3 below for two further fields
(7 and 12) confirmed the same way. **Scope, precisely**: this closes path (a) above (a pw_rpc/protobuf
schema to decode the opaque payloads) for the specific fields sampled — it does not itself claim every
one of `qhr`'s 38 fields has been wire-verified, and does not by itself promote the broader "Sent"
HYPOTHESIS above to FACT for fields not yet sampled; it substantially strengthens that HYPOTHESIS
without fully closing it. See `DECISIONS.md` ADR-019 for the complete scope note.

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
| 0x02 | Pigweed `pw_hdlc` (§2.2a) | 🟢 FACT for the framing and for channel ownership (this is the companion app's own internal RFCOMM socket, ADR-018); 🟡 HYPOTHESIS (strong) that its Sent-direction payload content specifically carries `libmaestro`'s settings-write commands | Opaque ~16-byte "Sent" blocks (phone→Buds, unresolved content) and protobuf-decodable "Rcvd" blocks (device serial + firmware) — not GMS-dependent (present regardless in every capture that opens it) |
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
rather than left ambiguous. **Status, per the evidence rules (updated 2026-08-30, `DECISIONS.md`
ADR-018):** DLCI 0x02 as the companion app's own internal RFCOMM channel is now 🟢 FACT (see
§2.2a's "Channel ownership" finding — an SDP-record/APK-code correlation, not framing-mechanism
match alone). What remains 🟡 HYPOTHESIS (strong), unchanged in strength, is narrower than the
original "DLCI 0x02 = `libmaestro`" framing above: whether this channel's Sent-direction payload
*content* specifically carries `libmaestro`'s settings-write commands, since no Maestro-specific
content (an ANC/EQ method call) is decoded yet. DLCI 0x08's *identity* remains 🔴 OPEN QUESTION as before, but is now narrowed by a
checked negative: **not** `libmaestro` (mechanism mismatch against the one concrete signature
`pbpctrl` publishes), leaving "a lower-level Nearby/CDM companion-device negotiation independent
of both Fast Pair and Maestro" (`CAP-004-FINDINGS.md` §5a's existing framing) as the leading
remaining candidate for it. §2.3's three-channel table above is not restructured into a binary
choice — it already correctly shows three coexisting channels; this addendum only sharpens which
one the *libmaestro* hypothesis should now concentrate on.

**Update (2026-08-30, audit finding, `CAP-033-FINDINGS.md` §3) — a new lead, not a resolution.**
`CAP-033`'s SDP browse names DLCI 0x08's service **"GSND CONTROL"** (UUID
`f8d1fbe4-7966-4334-8024-ff96c9330e15`, RFCOMM channel 4) on the wire — the first time this channel
has had anything beyond a raw DLCI number to refer to it by. This is a concrete new search target for
a future APK keyword pass (a `grep -ri "gsnd"` sweep of `jadx-output/` for `v1.0.955078536-10253511`
found no match as of this update), not a resolution of the channel's identity — knowing it is *named*
"GSND CONTROL" does not by itself reveal its Group/Code semantics or confirm/deny the Nearby/CDM
candidate above. 🟡 HYPOTHESIS awaiting maintainer review, per `AGENTS.md` §6 — not committed as a
promotion. The same browse also named DLCI 0x0a "GSND AUDIO" (`CAP-021-FINDINGS.md` §4a's
still-unattributed 1123-frame burst channel), DLCI 0x06 "DEBUG APP", and DLCI 0x12 "BTIS" — none
previously documented anywhere in this project.

**Handling rule (unchanged regardless of which hypothesis is confirmed, per
`AGENTS.md` §6 and `ARCHITECTURE.md` §5):** any checksum mismatch, or any frame
that fails to parse against the relevant invariants, is dropped silently and
surfaced internally as `BudsError.MalformedFrame` — never a crash, never a
best-effort guess at the payload.

## 3. Protobuf (`.proto`) definitions

The `libmaestro` control channel communicates using serialized Protocol
Buffers. Schemas are extracted from the official companion app APK using tools
such as `pbtk` — an AI session may run `pbtk` itself and search/explain its
output mechanically, but never hand-reconstructs or guesses a schema, and
never decides which extracted finding is relevant (see `AGENTS.md` §4/§6,
`DECISIONS.md` ADR-017, superseding ADR-003).

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
- **"Get ANC state" (`0x11`) — opcode identity 🟢 FACT, promoted 2026-09-04 (maintainer sign-off,
  `DECISIONS.md` ADR-021):** documented above by spec but never previously observed on the wire
  until `CAP-036` (2026-09-04), frame 1169 (`08 11 00 00`, Sent, DLCI 0x04) — `Group=0x08,
  Code=0x11, Len=0x0000`, exact structural match to the spec's `Get ANC state` (Seeker→Provider, no
  payload). Fired 34ms after DLCI 0x04's channel opens during a *reconnect* — ~9s before the app's
  own "Active" UI confirmation renders — and answered ~10.7ms later by frame 1182 (`08 13 00 04
  01 e8 00 20`, Rcvd — Notify, current state=`0x20`=Off), matching the on-screen ANC state
  confirmed later in the same session (an internal content cross-check, on the same pattern as
  `0x12`'s own promotion above). **Scope of this promotion, narrower than it might look:** only the
  *opcode's identity and existence on the wire* is FACT (Group/Code values, direction, zero-length
  structure, and that the Notify response's decoded value matches on-screen ground truth). **Not
  promoted — remains 🟡 HYPOTHESIS pending replication:** whether this query reliably fires on
  *every* reconnect (n=1, one session, one sample) — a second independent capture reproducing the
  same query/response pair on a fresh reconnect would be needed before that broader trigger-reliability
  claim can be promoted. Directly relevant to `ARCHITECTURE.md` §3.1 (State Reconciliation) —
  confirms the official app performs a comparable read-on-reconnect for ANC state specifically, at
  least once. The same session found **no** query of any kind (this opcode or otherwise) when a
  settings screen is opened with nothing touched, across five clean windows (EQ, Controls and
  gestures, Touch controls, More settings, Multipoint) — that negative result likewise stays 🟡
  HYPOTHESIS (single session), not promoted by this ADR. See `CAP-036-FINDINGS.md` §3–§7 for the
  full decode, and an unreconciled discrepancy this same frame raises (`CAP-036-FINDINGS.md` §3, §6
  below).
- **Sent to**: RFCOMM Fast Pair Message Stream, DLCI 0x04 (§2.1/§2.3) — **not** `libmaestro`'s
  Pigweed-HDLC channel (DLCI 0x02, §2.2a) and **not** the private DLCI-0x08 envelope; both were
  live candidates before this resolution.
- **Expected response**: ACK (`0xFF 0x01 0x00 0x06 <echoed group/code/data>`), 🟢 FACT, see above.
- **Status**: 🟢 FACT (opcode, payload layout, and the "set" direction's semantics are all
  confirmed against official documentation and cross-validated within `CAP-001`); recorded in
  `DECISIONS.md` ADR-009. **`FrameEncoder` implementation for this command is blocked pending
  `CAP-006`** (ADR-009) — the FACT status above does not by itself establish that every ANC tap
  reliably produces a command frame; see the open sub-question below and `CAP-001-FINDINGS.md`
  §5's risk flag. **`0x11`'s opcode identity independently 🟢 FACT as of 2026-09-04** (`CAP-036`,
  `DECISIONS.md` ADR-021) — its trigger-reliability ("fires on every reconnect") remains 🟡
  HYPOTHESIS, unaffected by ADR-009's scope.
- **Evidence**: UI presence (`SCREENSHOTS_PIXEL_BUDS_APP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
  §1); official spec (`developers.google.com/nearby/fast-pair/specifications/extensions/hearablecontrols`,
  consulted 2026-08-12, re-fetched 2026-08-30 with no drift — Message Group `0x08`, Codes
  `0x11`/`0x12`/`0x13` unchanged; the page still contains no mention of touch-control/gesture
  configuration, EQ, or a "GSND"-named service, corroborating §2.3's conclusion that EQ/touch
  settings need `libmaestro`'s own channel, not a further official extension); `CAP-001` frames
  2039/2132/2159/2193 (`Set`) and 2041/2134/2162/2195
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
- **Opcode/payload structure**: not yet extracted from an official spec, but **Added 2026-08-15,
  refined 2026-08-18**: `CAP-005-FINDINGS.md` (Group T, isolated `EQP-002`/`EQS-004` capture)
  identified the wire format on DLCI 0x02 (`libmaestro`'s Pigweed `pw_hdlc` channel, §2.2a) — an
  HDLC frame whose payload nests down to a 5×`float32` band-gain quintet, one `Sent` frame per
  changed value. 🟢 **FACT** for the envelope/quintet shape itself (byte-for-byte reproducible,
  cross-capture replicated — see below), and 🟢 FACT that this channel is the companion app's own
  internal RFCOMM socket (§2.2a's "Channel ownership" finding, `DECISIONS.md` ADR-018); 🟡
  HYPOTHESIS (strong) that this specific EQ quintet write is `libmaestro`'s own settings-write
  content (as opposed to, e.g., a coincidentally-similar payload from another RPC on the same
  socket) — unchanged from §2.2a's narrower remaining caveat.
- **Field-to-band mapping — 🟢 FACT, promoted 2026-08-18, maintainer sign-off obtained 2026-08-28
  (`DECISIONS.md` ADR-016)** (was 🟡 HYPOTHESIS as of 2026-08-15,
  inferred from only one slider ever having moved in that first capture). The 2026-08-18 session
  (`captures/CAP-015-2026-08-18_06-11-06_06-17-40-Group_T/CAP-015-FINDINGS.md` §5) drags **all
  five** sliders individually, three passes each, and directly video-confirms 4 of the 5 fields via
  finger-on-slider position (plus an on-screen `-6.0` tooltip for one); the 5th is confirmed by
  elimination against a perfectly repeating field-change order across all three passes. The mapping
  matches the earlier (2026-08-15) capture's single-band inference exactly, five days apart,
  independently:

  | Quintet field (wire order) | 1 (first) | 2 | 3 | 4 | 5 (last) |
  |---|---|---|---|---|---|
  | On-screen band | Low bass | Bass | Mid | Treble | Upper treble |

  **Note:** wire field order is the *reverse* of the on-screen top-to-bottom slider order (UI shows
  Upper treble first/top; the wire quintet puts it last/field 5) — `FrameEncoder`/`FrameDecoder`
  must not assume the two orders match without an explicit re-index.
- **Band-gain range — 🟢 FACT (2026-08-18, maintainer sign-off obtained 2026-08-28, `DECISIONS.md`
  ADR-016)**: every slider, dragged to its physical UI extreme,
  clamps at **±6.0** (`CAP-015-FINDINGS.md` [2026-08-18] §4 — 8 of 10 extreme-drag samples land at
  exactly `±6.0`, the remaining 2 at `5.8`/`5.9`, consistent with the drag gesture not quite
  reaching the slider's physical edge before release, not a different clamp value). 🔴 units not
  independently confirmed (plausibly dB, not tested against any external reference).
- **Confirmed preset quintets — 🟢 FACT (2026-08-18, maintainer sign-off obtained 2026-08-28,
  `DECISIONS.md` ADR-016), `[Low bass, Bass, Mid, Treble, Upper treble]`**:

  | Preset | Quintet |
  |---|---|
  | `Last saved` (this session's baseline) | `[0.0, 0.0, 0.0, 0.0, 0.0]` |
  | Heavy bass | `[5.0, 3.0, 0.0, 0.0, 0.0]` — byte-for-byte identical to the independent 2026-08-15 capture's decode |
  | Light bass | `[-5.0, -1.5, 0.0, 0.0, 0.0]` |
  | Balanced | `[-3.5, 0.5, 1.0, -1.0, 2.5]` |
  | Vocal boost | `[-1.0, 0.0, 4.0, 2.0, 0.0]` |
  | Clarity | `[-2.0, 0.0, 2.0, 3.0, 5.0]` |

  `Last saved`'s quintet is per-account/session state, not a fixed constant — the value above is
  this specific session's starting point, not a universal default.
- **Outer field 16 vs. 18 ("preview" vs. "save") — still 🟡 HYPOTHESIS, reading revised
  2026-08-18.** The 2026-08-15 capture guessed field 18 = an explicit `Save`-button tap, ~5s after
  the matching field-16 write. The 2026-08-18 capture's 15 field-18 frames each fire only
  0.05–1.9s after the preceding field-16 write, with no video-visible `Save`-button tap in between
  for any of them — **revised hypothesis: field 18 fires on slider-release (finger lift), not on a
  separate `Save`-button tap**; the visible `Save` button may instead persist the whole profile as
  the account's `Last saved` preset, a separate/higher-level action. Neither reading is confirmed;
  would need a capture that drags-and-releases without ever tapping `Save`.
- **Sent to / expected response**: same open questions as §4.1.
- **Status**: 🟢 FACT for the wire envelope, the field-to-band mapping, and the ±6.0 range; 🟡
  HYPOTHESIS (strong) that DLCI 0x02 is specifically `libmaestro`; 🟡 HYPOTHESIS for the
  preview/save field semantics; 🔴 unconfirmed for the gain units, the Control byte, and the
  ~13-byte correlation-ID region (§6). **`FrameEncoder`/`FrameDecoder` implementation for EQ is
  explicitly unblocked, 2026-09-03 (`DECISIONS.md` ADR-020)** — the FACT-level elements above
  (envelope, field-to-band mapping, ±6.0 clamp, preset quintets) are sufficient on their own; the
  field-16-vs-18 and gain-unit open items above are unaffected and should be resolved before a
  "Save as preset" UI affordance ships, per that ADR's own scope note.
- **Evidence**: `SCREENSHOTS_PIXEL_BUDS_APP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §1,
  `captures/CAP-005-2026-08-15_15-02-31_15-03-45-Group_T/CAP-005-FINDINGS.md` (first candidate
  format, single-band sample), `captures/CAP-015-2026-08-18_06-11-06_06-17-40-Group_T/CAP-015-FINDINGS.md`
  (all-5-bands confirmation, range, preset table).
- **Verified with experiment**: Group T, two independent sessions — `CAP-005` (2026-08-15,
  single Bass slider) and `CAP-015` (2026-08-18, 5 presets + all 5 sliders, 3 passes each) — see
  both FINDINGS.md files above.

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
  **Re-check flagged 2026-09-03**: two direct re-fetches of the official
  `batterynotification` extension page found no sentence anywhere on it stating this
  8s/20s timing — the byte-layout table above was re-confirmed exactly, but this specific
  timing sub-claim's citation could not be re-verified against the currently-live page text.
  Not retracted (the fetch tool's page-to-text conversion is not a guaranteed-complete read,
  and this detail may live on a different Fast Pair spec page, e.g. the base Message Stream
  spec, not checked this pass) — downgraded from unqualified `[OFFICIAL-SPEC]` to 🟡
  HYPOTHESIS pending a maintainer or future session reading the actual page directly.
- **Advantage**: visible on a passive BLE scan — no active connection required,
  useful for the battery fallback logic in `ARCHITECTURE.md` §4.
- **Attempted 2026-08-21, `CAP-011` — inconclusive, not `[VERIFIED-LOCAL]`.** A dedicated capture
  found the Fast Pair Service (`0xFE2C`) BLE advertisement 🟢 FACT present (634 frames, 5 rotating
  addresses, strong/close-range RSSI), but the sampled service-data payloads do **not** structurally
  match this section's documented layout — no sampled byte equals the expected Length&Type marker
  (`0x33` show / `0x34` hide) at any offset. **Two un-isolated confounds, not resolved either way:**
  (1) the capture's own procedure deviated from a clean passive scan — an active classic RFCOMM
  connection was present throughout (the official app was left open on "Device details"), a
  confound this section's guidance doesn't yet cover; (2) the observed payloads structurally
  resemble a plain Account Key Filter/rotating-salt advertisement rather than the Battery
  Notification extension specifically. See `CAP-011-FINDINGS.md` §4 for the full byte-level check.
  **Not force-fit, per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5's explicit instruction** — recorded as
  inconclusive rather than a false negative or false positive. A clean repeat (Bluetooth scanning
  only, no app open, no active connection) is still needed.
- **Timing correlation, 🟡 HYPOTHESIS (`CAP-009`, 2026-08-23, maintainer-approved for recording
  here per `AGENTS.md` §6) — a candidate trigger for on-screen updates when HFP/Option E are
  both closed.** After `CAP-009`'s case+USB reconnect (20:02:27.21), the on-screen Right-earbud
  percentage updates (20:02:28.68–29.01) with **no** HFP or DLCI-0x08 traffic present at all
  (both channels stay closed post-reconnect, `CAP-009-FINDINGS.md` §4) — so neither Option C nor
  Option E can explain it. Immediately after the reconnect, a `LE Set Extended Scan Enable`
  command (20:02:27.683) is followed by a stream of `LE Extended Advertising Report` events
  (starting 20:02:27.742, continuing through and past the on-screen update), one of which
  (frame 29174) decodes to a Fast Pair Service (`0xFE2C`) advertisement — matching the
  foreground/reconnect-triggered, time-boxed scan pattern `AGENTS.md` §7's bounded exception
  describes. **Not confirmed**: the advertising `BD_ADDR` (`17:6e:d1:d9:e3:dd`, a random/rotating
  address) was not traced back to this specific Buds unit (no Account Key Filter decode
  attempted), so this is a timing correlation, not a payload-level confirmation — it neither
  proves nor disproves whether the payload itself matches this section's documented layout (still
  open per the `CAP-011` result above). Proposed verifying experiment: capture the BLE side at
  full detail and decode the Account Key Filter to confirm device attribution.
- **Device-attribution advance, 🟡 HYPOTHESIS (`CAP-036-FINDINGS.md` §12.4, 2026-09-04) — a
  different sub-question than the payload-layout one above, not a resolution of it.** DLCI 0x04's
  "BLE address updated" field (Device Information Group `0x03` Code `0x02`, §0.1) gave this
  session's own value, `44:d6:94:50:f0:4e` — cross-checked directly against the same log's BLE
  advertising traffic, this address broadcasts a Fast Pair (`0xFE2C`) + `0x1853` advertisement
  **407 times**, stable payload, throughout the ~7-minute session. This is the first time a
  live-observed advertising address has been tied back to a classic-channel field describing the
  Buds themselves (rather than only a timing correlation, as the entry below records) — but the
  payload's first byte is `0x10`, not the `0x00` "Flags" byte this section's table requires,
  structurally still an Account Key Filter-shaped frame, not a Battery Notification match — the
  payload-layout question above remains exactly as open as `CAP-011` left it.
- **Evidence**: official Fast Pair spec; `CAP-011-FINDINGS.md` (2026-08-21, inconclusive
  payload-layout result); `CAP-009-FINDINGS.md` §4 (2026-08-23, timing-only correlation);
  `CAP-036-FINDINGS.md` §12.4 (2026-09-04, device-attribution advance).

#### Option B — RFCOMM via Fast Pair Message Stream "Device Information"

- **Status**: 🟢 FACT (mechanism exists) / 🟡 HYPOTHESIS (candidate battery
  message code identified, `CAP-009`, 2026-08-23 — not yet a confirmed match).
- The Message Stream (§2.1) has a documented "Device Information" message
  group. Firmware version is confirmed at code `0x09` (per the Find Hub
  Network extension doc), sent once per Message Stream establishment. Battery
  is expected to have its own code in the same group, following the same
  event-driven pattern; **PROPOSAL, added 2026-08-28 (2026-08-28 project-wide audit finding
  `EXT-01`), pending maintainer review — the specific code value now has external spec support:**
  Google's official Fast Pair Device Information extension spec
  (`developers.google.com/nearby/fast-pair/specifications/extensions/deviceinformation`, fetched
  2026-08-28) documents Message Group `0x03` Code `0x03` = **"Battery updated"** — an exact match
  to the candidate below, independently derived from `CAP-009`'s own wire behavior. This
  strengthens, but does not by itself promote, the HYPOTHESIS below (`AGENTS.md` §6 still requires
  explicit maintainer sign-off, and ideally the fresh independent-session reproduction already
  proposed there, before promotion).
- This is presumed to be the same underlying channel as the
  `hardware_status.proto` hypothesis in §3 — i.e. likely **not** a
  Buds-specific protobuf schema at all, but generic Fast Pair Message Stream
  traffic.
- **Candidate battery code, 🟡 HYPOTHESIS (`CAP-009-FINDINGS.md` §7, maintainer-approved
  2026-08-2x for recording here per `AGENTS.md` §6):** DLCI `0x04`'s `Group 0x03 Code 0x03`
  message (`03 03 00 03 <b1> <b2> ff`) is a strong structural and behavioral candidate. Across
  208 occurrences in a 101-minute natural-discharge session, `b2` matched the Right earbud's
  percentage at all 7 of its transitions, and `b1` matched Left's percentage at both of its
  transitions **while not charging** — both fields updating within single-digit milliseconds of
  the already-established `AT+BIEV` (Option C) and DLCI-0x08 Option E pushes for the same
  underlying change. Once the Left earbud starts charging, `b1` stops behaving like a percentage
  (jumps to 221 and climbs ~1/sample instead of following L's known 93→100 charging curve) —
  read as a regime change (the field switches to reporting something else while charging, not yet
  identified) rather than a counter-example against the mapping while discharging. **Not yet
  confirmed**: whether this `Group`/`Code` numbering is stable across sessions the way DLCI `0x08`'s
  Option E numbering has proven to be, or is itself session-dynamic; the charging-state field
  switch is unexplained. Proposed verifying experiment: reproduce in an independent session and
  check the `Group 0x03 Code 0x03` numbering holds.
- **Cross-channel timing synchronization extended to DLCI 0x02, 🟡 HYPOTHESIS (`CAP-036-FINDINGS.md`
  §12.5, 2026-09-04):** the near-lockstep pattern above (Option B/C/E firing within single-digit
  milliseconds of each other) is joined, in this session, by a periodic DLCI 0x02 (`libmaestro`)
  push firing within 7–18ms of Option E's DLCI 0x08 push at all 7 checked occurrences — suggesting
  a single shared underlying trigger across at least 4 mechanisms, not four independently-timed
  loops. The DLCI 0x02 push's own payload does not clearly match Option E's `[value, flag, index]`
  battery-triple shape, though (§6's new open question below) — this extends the *timing*
  observation only, not a claim that DLCI 0x02 carries confirmed battery content.
- **Evidence**: official Fast Pair Message Stream / Find Hub Network extension
  docs (mechanism). `CAP-009-FINDINGS.md` §7, `[VERIFIED-LOCAL]` 2026-08-23 (candidate code).

#### Option C — HFP AT commands (`AT+BIEV` HF Indicator #2, `AT+CIND` `battchg`)

- **Status**: 🟢 FACT — confirmed active for this device (`CAP-001-FINDINGS.md`
  §3), on channel 4 / **DLCI 0x09**. **Note (2026-08-23):** this "channel 4" is a *different*
  logical channel from DLCI 0x08's "channel 4" (the private envelope, §2.3) — both DLCIs share
  one RFCOMM multiplexer session (same ACL handle/L2CAP CID, independently confirmed via
  `tshark`), but each side of the connection independently assigned its own service to server
  channel 4, disambiguated by RFCOMM's direction bit (phone-initiated → DLCI 0x08, Buds-initiated
  → DLCI 0x09). This is standard, spec-correct RFCOMM behavior, not a numbering conflict — see
  `CAP-001-FINDINGS.md` §2 for the full phone-init/Buds-init disambiguation. Two
  simultaneously-active HFP mechanisms
  observed: `AT+BIEV=2,<0-100>` (HF Indicator #2, Bluetooth-spec-assigned as
  Battery Level) and the older `AT+CIND?` `battchg` indicator (0–5 scale) —
  these **disagreed** in `CAP-001` (`battchg=3` ≈60% vs. `AT+BIEV=2,100` =
  100%, at the same moment).
- **`BATT-006` resolved 2026-08-23 (`CAP-009`, `ADR-015`, maintainer sign-off obtained):**
  `AT+CIND?`'s `battchg` is 🟢 **FACT** a single, non-repeating snapshot queried once at HFP
  Service Level Connection setup and never refreshed again — confirmed across a 101-minute,
  natural-discharge session (`CAP-009-FINDINGS.md` §1) where the peer's Right earbud genuinely
  changed by ~13 percentage points while `battchg` stayed silent the whole time. `AT+BIEV=2` does
  track real changes — but **per-earbud, not as a single aggregate** (this project's earlier
  working assumption — see the next bullet).
- **Update model — periodic, but not a fixed cadence.** `AT+BIEV=2,100` repeated on
  a roughly 6–7 second cadence immediately after SLC setup in both `CAP-001` (frames 1236–2269,
  08:51:14.106–08:51:52.148) and `CAP-009` — but `CAP-009`'s much longer session (101 minutes vs.
  `CAP-001`'s ~80 seconds) shows this tight spacing is a **settling-burst behavior right after
  connection, not a sustained fixed cadence**: gaps widen to a median of 20.5s and as much as
  ~14.6 minutes during idle stretches later in the same session (69 pushes over 86 minutes,
  `CAP-009-FINDINGS.md` §2, 🟡 HYPOTHESIS — `CAP-009` cannot distinguish "less frequent
  push-on-change" from "polling that only slows down while idle"). **Revises** the previous
  "regardless of whether the value changed... expect a steady stream" guidance below, which held
  for `CAP-001`'s short, connection-adjacent window but does not describe extended idle behavior.
  An app relying on this mechanism should expect a push shortly after connecting, plus further
  pushes whenever the value changes — but **not** a steady drip throughout an idle session.
- **Per-earbud tracking, not a single aggregate (revises this project's earlier working
  assumption that both HFP indicators report one aggregate value) — 🟢 FACT for `CAP-009`,
  🟡 HYPOTHESIS as a general rule.** `CAP-009`'s 101-minute session shows `AT+BIEV=2`
  tracking the **Right** earbud specifically: all 5 of its distinct values (93→92→90→89→88) match
  R's on-screen percentage and only R's, at every transition, while Left (96→95→94→100) and Case
  (72→71→68→75) never appear in the `AT+BIEV` sequence at all (`CAP-009-FINDINGS.md` §3). Whether
  `AT+BIEV` always reports physical-Right, or whichever earbud is currently HFP-primary (R
  happened to be primary this session), is not yet distinguished — a session with a confirmed-L
  primary earbud would resolve this. `AT+CIND?`'s `battchg` was only ever observed once per
  session (see above), so whether *it* is aggregate or per-earbud remains untested either way.
- **Second data point, added 2026-08-26 (`CAP-008`, Group V — proposal awaiting sign-off):**
  `CAP-008`'s `AT+BIEV=2` tracked the **Left** earbud instead — its one value transition
  (98%→97%) matches only the on-screen Left percentage (cross-checked against 3 separate video
  frames), while Right (100%) and Case (43%) stayed constant throughout
  (`CAP-008-FINDINGS.md` §8). Two independent sessions now each track a *different* physical
  earbud (`CAP-009`: Right; `CAP-008`: Left) — evidence in favor of the "whichever earbud is
  HFP-primary" reading over a fixed-Right rule, still 🟡 HYPOTHESIS since neither session
  deliberately swapped the primary earbud mid-session to confirm the mechanism directly.
- **New open question, added 2026-08-26 (`CAP-008-FINDINGS.md` §9):** `CAP-008` observed a
  single unsolicited `+CIEV: 6,<battchg>` push (index 6 = `BATTCHG`) mid-session, not tied to any
  video-visible action — the first time this project has seen the `battchg` *indicator* itself
  update outside the initial SLC-setup snapshot, even though the `AT+CIND?` *query* remains
  observed only once per session in every capture to date (consistent with `ADR-015`, not a
  contradiction of it). 🔴 OPEN QUESTION: is this a general mechanism (battchg can push on
  change, just rarely) or a one-off artifact? A single occurrence is not enough to resolve this.
- **GMS/app-independence confirmed for Option C specifically, 🟢 FACT (`DESKRESEARCH_FINDINGS.md`
  2026-09-04 entry):** `AT+BIEV=2,<value>` fires normally with Google Play Services **disabled
  and** the official app **uninstalled** (`CAP-004`) and, independently, with the app
  **force-stopped** for the entire session while GMS is untouched (`CAP-033`) — HFP battery
  reporting is OS/Bluetooth-stack-level, not GMS- or app-driven. Extends `CAP-035-FINDINGS.md`'s
  existing GMS-independence result (which only checked DLCI 0x08/0x0a/0x06/0x12) to Option C.

#### Option D — BLE Battery Service (`0x180F`, Battery Level characteristic `0x2A19`)

- **Status**: 🟡 HYPOTHESIS — the service's *existence* is now confirmed
  (`CAP-017`, 18:30 session — live GATT discovery via a fresh nRF Connect
  client, `[VERIFIED-LOCAL]` 2026-08-16, see
  `captures/CAP-017-2026-08-16_18-30-12_18-37-12-Group_W/CAP-017-FINDINGS.md`
  §3, service #14 of 15 in the recovered profile). **Not yet confirmed:**
  the characteristic's actual value, whether it reports a single aggregate
  figure or per-component (L/R/Case), and whether it is actually read by
  either the official app or any capture to date — no `Read`/`Notify`
  traffic against this service's handles has been observed in any capture
  (the handle range itself is still unresolved, same open question as the
  `0x0c0X`/`0x0f2X` cluster in §2.3). Raised from 🔴 (service presence was
  previously unconfirmed) to 🟡 (presence confirmed, use/content still
  open) — not promoted further, since existence alone does not establish
  this is how the app or this project's own implementation should read
  battery. If pursued, the value itself would be read from the standard Battery Level
  characteristic, `0x2A19` (Bluetooth SIG-assigned, externally confirmed 2026-08-23) — the
  service UUID `0x180F` alone only identifies that the service exists, not which characteristic
  handle to read/subscribe to.
  The Battery Service/Battery Level pairing was independently reconfirmed a second time via nRF
  Connect's on-screen characteristic list (`Properties: NOTIFY, READ`, `CAP-014-FINDINGS.md` §3).
  **Handle range resolved 2026-09-01 (`CAP-034`, maintainer sign-off obtained per `AGENTS.md` §6,
  🟢 FACT):** the Battery Service occupies handles `0x0f30`–`0x0f33`, with the Battery Level
  characteristic at value handle `0x0f32` (CCCD `0x0f33`) — resolved by the same discovery burst
  that closed the `0x0c0X`/`0x0f2X` open item below; see that item and
  `CAP-034-FINDINGS.md` §4.5 for the full command+hex evidence. This also explains the
  previously-unresolved `0x0f32`=`0x64` (100%) value first seen in `CAP-017`/`CAP-014`: it is an
  ordinary Battery Level reading, not a proprietary field — it only ever appeared via nRF Connect
  because the official app reads battery through the Fast Pair/HFP mechanisms above, not this BLE
  characteristic. **Still not resolved:** no capture to date has observed a `Read`/`Notify` of this
  characteristic actually returning a value while the official app is in use, so whether the app (or
  this project's own future implementation) should read battery via this path remains open — status
  stays 🟡 for that narrower question.

#### Option E — DLCI 0x08 private envelope, per-earbud+case push (`Group 0x0e Code 0x01` / `Group 0x04 Code 0x03`)

- **Status**: 🟢 **FACT, promoted 2026-08-23** (maintainer sign-off obtained per `AGENTS.md` §6;
  see `DECISIONS.md` ADR-014) for the **index=1/2/3 → Left/Right/Case mapping**, based on the
  3-independent-session cross-check below. `CAP-011`'s own stale idx=3 reading and the burst's
  trigger stay open, unaffected by this promotion (see below).
- **Discovered while re-analyzing `CAP-011`** for an unrelated request (locating the exact moment a
  1%-battery UI change occurred): a message on DLCI 0x08 (the still-🔴-unidentified private
  envelope, §2.3), `Group 0x0e Code 0x01`, decodes to a nested structure carrying **3 repeated
  entries** `[value, flag, index]` plus 2 trailing scalars.
- **Cross-capture confirmation (2026-08-23): entries index=1/2/3 = Left/Right/Case, confirmed in 3
  independent sessions spanning 12 days.** `CAP-011` (2026-08-21) alone: entries idx=1/idx=2 match
  the on-screen Left/Right percentages across **4** independent occurrences in one ~17.5-minute
  log, including a video-confirmed UI *change* (92/87, ~0.86s before the screen visibly updates)
  and two further off-camera recurrences where Right keeps declining (88→87→86) — not a
  coincidence. Extending the same check to `CAP-001` and `CAP-002` (2026-08-09, both `Group 0x0e
  Code 0x01` frames picked near an independently-recorded on-screen notification) found a clean
  **3-for-3 match, including idx=3=Case**: `CAP-001` frame 1114 = `[100,100,62]` against on-screen
  "Left 100% Case 62% Right 100%"; `CAP-002` frame 49024 = `[100,100,57]` against on-screen "Left
  100% Case 57% Right 100%". An independent, single-value message on the same DLCI (`Group 0x04
  Code 0x03`) cross-confirms the Right value at all 4 of `CAP-011`'s occurrences.
- **Not a new packet type — a new semantic decode of an already-known shape:** this exact
  `field1="all"` + "3 varint-triple entries" structure was already documented, structurally only,
  in `CAP-002-FINDINGS.md` §2a (2026-08-12) — that pass did not attempt to interpret the numbers.
  This entry is the first to propose (and cross-check) what they mean.
- **One specific, unresolved anomaly — not glossed over:** in `CAP-011` alone, entry idx=3 (Case)
  reads a **stale, non-matching** value (92) against the on-screen Case reading (89%, constant
  throughout that session), unlike `CAP-001`/`CAP-002` where it matched live. `CAP-011`'s idx=3
  also lacks the `flag` field (`field2`) that every other confirmed-fresh entry carries (present
  and `=1` in all of `CAP-001`/`CAP-002`'s entries and `CAP-011`'s idx=1/2) — plausibly, not
  confirmed, a "fresh/valid" bit, absent specifically when a value is stale. A plausible (not
  confirmed) explanation for the staleness itself: `CAP-011`'s case sat open and empty for the
  whole session (a documented procedure deviation, §4.3's intro to this capture) — if the case's
  own reporting requires it closed/holding a bud to refresh, idx=3 could be carrying a
  last-known value predating this session's log.
- **Also unresolved:** the burst's own trigger — it recurs at irregular intervals in `CAP-011`
  (4:02, 2:56, 8:21 apart), and checking it against that session's near-continuous BLE reconnect
  churn found no correlation (the churn is far more frequent than this burst, ruling that out as
  the trigger).
- **Sent to**: DLCI 0x08's private envelope (§2.3) — not DLCI 0x04's official Message Stream, not
  DLCI 0x02.
- **Evidence**: `CAP-011-FINDINGS.md` §7 (`[VERIFIED-LOCAL]`, 2026-08-23) — full command + raw hex
  for all 4 occurrences of both message types in `CAP-011`, the frame-by-frame video re-derivation
  of the exact UI-change timestamp, and the `CAP-001`/`CAP-002` cross-check frames (§7c).
- **Verified with experiment**: `CAP-009` (2026-08-23) delivered the "dedicated repeat" this entry
  called for — a fresh, purpose-built, 101-minute natural-discharge bracket, not a deskresearch
  correlation against a capture recorded for another purpose. It is now the **4th independent
  confirming session** (after `CAP-001`/`CAP-002`/`CAP-011`) and by far the longest/densest: 75
  occurrences of `Group 0x0e Code 0x01`, Left and Right matching the on-screen value at every one
  of 14 transitions across the whole session (`CAP-009-FINDINGS.md` §6). **5th confirming session,
  2026-09-04 (`CAP-036-FINDINGS.md` §12.2):** `100/100/100`, matching on-screen exactly.
  **Completeness addendum, not a new claim:** `Group 0x04 Code 0x03`'s own payload has a constant
  leading field (`field2=5`) preceding the Right-battery value in **every** session checked
  (`CAP-001`, `CAP-002`, `CAP-011`, `CAP-036`) — not previously called out at this byte-level
  precision.
- **Two addenda, maintainer-approved 2026-08-2x (`AGENTS.md` §6) — `CAP-009-FINDINGS.md` §6:**
  - **A live charge cycle, observed for the first time on this mechanism.** After the Left earbud
    is placed in the case (~19:52:15), its `Group 0x0e Code 0x01` value climbs monotonically
    93→94→95→96→97→98→100 over the following ~6 minutes — the first confirming session to capture
    charging rather than only discharging.
  - **The Case field's "unknown" placeholder has two distinct wire encodings, not one.** For the
    ~78 minutes before any bud touches the case, every occurrence's Case entry is the shorter,
    **no-`flag`-field** form (`08 ff 01 18 03`, matching `CAP-011`'s stale-reading encoding).
    Right as a bud is about to make contact (3 occurrences, ~1.3s before a real value arrives),
    the Case entry briefly switches to a **longer form that does carry `flag=1`** despite still
    reporting the same "unknown" (255) value (`08 ff 01 10 01 18 03`). 🟡 **HYPOTHESIS:** this is
    the first direct evidence for the mechanism `CAP-011-FINDINGS.md` §7c could only speculate
    about (that capture never observed an empty→populated case transition) — the case's reading
    needs the case closed/holding a bud to be considered fresh; the no-`flag` form is a long-lived
    cached placeholder, while the `flag=1` form marks an actively-attempted-but-not-yet-successful
    fresh read, immediately before a real value lands. Not confirmed beyond this one transition.
    **Complication, added 2026-09-04 (`DESKRESEARCH_FINDINGS.md` entry, `CAP-007`):** the same
    short, no-`flag` form (`0a 04 08 2d 18 03`) is observed carrying a plausible **real** value
    (`0x2d`=45, not the `0xff`/255 sentinel) throughout an entire ~6-minute session, with no
    case-contact transition anywhere in that window. This doesn't fit "no-`flag` form = unknown
    placeholder specifically" as a universal rule — 🔴 **narrowed to an open question**: either the
    short form can carry any value under some other, not-yet-identified condition (not exclusively
    the unknown/255 case), or `CAP-007`'s own session differs from `CAP-009`'s in some relevant way
    not yet isolated (e.g. `CAP-007`'s Group U condition — buds removed from case, case lid
    variously open/closed — versus `CAP-009`'s natural-discharge idle session).
  - **The Case field itself dips sharply right as charging begins, then declines further, more
    slowly.** 71%→69% in ~21 seconds (far faster than this session's other observed discharge
    rates), holds ~6 minutes, then 69%→68%. 🟡 **HYPOTHESIS:** may reflect an
    instantaneous/voltage-based reading that dips under a sudden charging-current load rather than
    a smoothed charge-level percentage — the on-screen UI only shows 68% by ~20:00:01, later than
    either wire step, consistent with the UI smoothing or delaying this relative to the raw value.
    Not verified further; proposed as a concrete follow-up (a case-insertion bracket with tighter
    video sampling).

**Implementation priority**: 0 (cheap to rule in/out) → A → B → C → D (see
`ARCHITECTURE.md` §4; A–D's order reflects official-spec confidence and
connection-cost, not raw confidence alone since A requires no active
connection). **Option E is not placed in this ordering**, even though promoted to FACT
2026-08-23 (`ADR-014`) — unlike A–D it is not an officially-documented Fast Pair mechanism at all
(it's DLCI 0x08's still-unidentified private envelope), so it doesn't fit the "official-spec
confidence" ranking rationale above. Practically: its own trigger is unconfirmed and irregular
(observed gaps of several minutes), and one session (`CAP-011`) showed its Case field reading
stale — not yet a reliable enough *update cadence* to slot ahead of the already-periodic HFP
option (C), even though its per-earbud content is now FACT-confirmed.

### 4.4 Find My Buds / Ring action

- **Status**: 🟢 **FACT, for Left/Right specifically — promoted 2026-08-23 (maintainer sign-off
  obtained per `AGENTS.md` §6; see `DECISIONS.md` ADR-011).** Tested 2026-08-21, `CAP-025`:
  video-correlated, cross-validated against ANC's already-🟢-FACT command on the same channel
  (§4.1) per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1's Group K discipline. Case/"both simultaneously"
  are a **separate, unresolved mechanism** — see below; not covered by this promotion.
- **Confirmed opcode/payload** (`[Group:1][Code:1][Len:2BE][Value:1]`, `PROTOCOL.md` §2.1's Message
  Stream envelope): `Group=0x04` (Action), `Code=0x01` (Ring) — exact match to the spec's own
  worked example. `Value` byte: `0x01` = start ringing **Right**, `0x02` = start ringing **Left**,
  `0x00` = stop/mute (shared, not per-earbud). Every `Sent` frame is retransmitted once
  (byte-identical), and answered by two ACK variants: `0xFF 0x01 0x00 0x02 0x04 0x01` and
  `0xFF 0x01 0x00 0x03 0x04 0x01 0x00` (one byte longer).

  > **Correction (2026-08-23):** the first variant was previously described as a "byte-for-byte
  > match to the spec's worked example." It is not — per §2.1's 2026-08-23 correction, the spec's
  > real worked ACK example is `0xFF 0x01 0x00 0x04 0x04 0x01 0x01 0x3C` (4 data bytes), which
  > matches **neither** of the two variants actually observed on the wire (2 and 3 data bytes
  > respectively). Both remain genuine, confirmed wire observations from `CAP-025` — that part is
  > unaffected — but neither can be described as matching the spec's own example. See §6's open
  > item on the second variant's extra byte, reopened against the corrected 4-byte spec tail.
- **Sent to**: RFCOMM Message Stream channel, DLCI 0x04, per §2.1.
- **Expected response**: confirmed — see the two ACK variants above.
- **Major structural finding — Case/"both" use a different mechanism entirely**: the official app
  splits this feature across two screens. "Device details → Find device" has only **"Ring
  Left"/"Ring Right"** buttons (the mechanism above). Case and "both simultaneously" are only
  reachable via "Find device → Most recent location", which opens a **Find Hub / Find My Device
  map view** with its own "Play sound" flow — video-confirmed showing a "Connecting…" state and the
  on-screen copy *"If you have another device linked with your Google Account, it may try to play
  sound on Pixel Buds Pro 2."* Across a ~2.5-minute window with this flow active, **zero**
  `Group 0x04 Code 0x01` frames appear on the wire — 🟢 FACT (checked explicitly, not assumed) that
  Case/"both" do **not** use the confirmed local Ring mechanism. 🟡 HYPOTHESIS: this instead routes
  through Google's Find My Device Network (account/cloud-mediated) — **potentially a hard
  Zero-GMS limit** (`AGENTS.md` §1) on offline Case-ring support, not just an open research
  question; flagged for maintainer awareness.
- **Evidence**: official Fast Pair Message Stream spec worked example; `CAP-025-FINDINGS.md` §3–§7
  (`[VERIFIED-LOCAL]`, 2026-08-21) — video-confirmed taps, 4 action/response pairs (2 starts, 2
  stops), cross-validated against ANC's confirmed envelope.
- **Verified with experiment**: `CAP-025` (2026-08-21) — see `captures/CAP-025-2026-08-21_08-40-52_08-45-26-Group_K/CAP-025-FINDINGS.md`.
  Recommended follow-up: none required for Left/Right; a targeted capture of the Find Hub network
  path (if in-scope at all, per the Zero-GMS question above) would be needed for Case/"both".

### 4.5 Other toggles and secondary features

**General-purpose settings-write envelope, discovered 2026-08-21 (`CAP-019`–`CAP-024`) — shared
infrastructure underlying every subsection below.** All the settings confirmed in this section ride
DLCI 0x02 (`libmaestro`'s Pigweed `pw_hdlc` channel, §2.2a), inside a common two-level outer
wrapper: `field 5 { field 4 { <setting-specific content> } }` (standard protobuf wire-format tags,
`field=tag>>3`, `wiretype=tag&7`). Payload offsets 0–12, preceding this wrapper, are a constant,
**cross-session-stable** prefix (`03 10 XX 1d ea 71 de 7e 25 1d 9a 8c 9e` or a closely related
variant — confirmed identical across `CAP-005`, `CAP-019`, `CAP-020`, `CAP-021` on different days),
consistent with `CAP-005-FINDINGS.md` §5a's "request/response correlation ID" reading, now
confirmed stable across sessions, not just within one. Each individual setting supplies its own
inner field number (and, for the per-earbud settings in §4.5.3, a further `field 7{field1|2{...}}`
sub-wrapper to select Left/Right). **The outer envelope shape/pattern itself is 🟢 FACT, promoted
2026-08-23** (maintainer sign-off obtained, `DECISIONS.md` ADR-013): a general-purpose
`libmaestro` settings-apply envelope, evidenced by 9+ distinct settings across 6 independent
captures all sharing the identical outer nesting with no counter-example found — comparable
cross-capture replication to how DLCI 0x02's own HDLC framing was promoted in §2.2a. See
`CAP-020-FINDINGS.md` §5 for the envelope's first identification.

**Scope of this promotion — narrower than it might look:** only the *outer wrapper's existence and
shape* is FACT. Each subsection's *specific* field-number-to-setting mapping below remains its own,
separately-labeled 🟡 HYPOTHESIS (strong for the ones with 2+ independent samples — In-ear
detection, Volume EQ, press-and-hold's 4/4 combinations; weaker for the single-sample ones —
Conversation Detection, Multipoint, the top-level Touch/Head-gesture toggles, Case sounds) — none
of those individual mappings are promoted by this entry, per the maintainer's explicit 2026-08-23
decision to promote the envelope pattern only, not blanket-promote every field. `FrameEncoder`
implementation of the *generic write path* (building the two-level wrapper itself) is unblocked;
implementing what any specific field number *means* is not, per `ARCHITECTURE.md` §5's per-command
implementation gate.

#### 4.5.1 Conversation Detection

- **Feature confirmed present**: toggle at Device details → Sound → Audio intelligence →
  Conversation detection. 🟢 FACT (UI presence).
- **Opcode/payload — field-number/type identity 🟢 FACT, promoted 2026-09-03 (maintainer sign-off,
  `DECISIONS.md` ADR-019)**: `field5(len5){ field4(len3){ field22 = 0|1 } }` (varint), `field 22` =
  `libmaestro`'s own `qhr` schema field 22, independently confirmed by APK static analysis: write
  site `hnz.java:29-49` (`a`, logging `"Set Speech Detection"`), read side `fxb.java` case 22 — a
  self-describing app-code match confirming this is a real, distinctly-numbered `qhr` field. **Not
  promoted**: whether the code's own internal name for this field, "Speech Detection," is the *same*
  feature as this section's "Conversation Detection" UI label — the two could describe the same
  feature seen from two angles (an internal/engineering name vs. the UI's own label), or something
  narrower/broader; this has not been reconciled, and the maintainer explicitly declined to promote
  that equivalence at this time. It remains 🟡 HYPOTHESIS: ON (`1`) confirmed video-correlated; OFF
  direction not captured this session.
- **Sent to**: DLCI 0x02 (`libmaestro`, §2.2a).
- **Expected response**: `Rcvd`-direction echo of the same field/prefix shape (no distinct ACK
  opcode observed).
- **Status**: 🟢 FACT for the field-number/type identity (`qhr` field 22 = the app's own "Speech
  Detection"); 🟡 HYPOTHESIS for the equivalence to this UI's "Conversation Detection" label, and for
  the OFF-direction wire value.
- **Evidence**: `CAP-019-FINDINGS.md` §3 (`[VERIFIED-LOCAL]`, 2026-08-21, frame 1808);
  `REVERSE_ENGINEERING.md`'s `qhr` entry; `DECISIONS.md` ADR-019.
- **Verified with experiment**: `CAP-019` (2026-08-21), single OFF→ON sample.

#### 4.5.2 Multipoint Bluetooth

- **Feature confirmed present**: toggle at Device details → More settings → Multipoint. 🟢 FACT.
- **Opcode/payload**: `field5(len4){ field4(len2){ field11 = 0|1 } }`. 🟡 HYPOTHESIS — `field 11` =
  Multipoint, ON confirmed video-correlated.
- **Additional finding**: enabling Multipoint also triggers a **Fast Pair Message Stream SASS
  burst** on DLCI 0x04, Group `0x07` (Codes `0x11`/`0x21`/`0x34`/`0x40`/`0x41`/`0x42`, the last
  containing the ASCII string `"in-use"`) — the first time this project has correlated SASS content
  (§2.3's table) with a specific triggering action. Directly confirms
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group C's own hint that Multipoint "may trigger an
  SDP/connection update, not just an RFCOMM command."
- **Sent to**: DLCI 0x02 (setting write) **and** DLCI 0x04 Group `0x07` (SASS negotiation).
- **Status**: 🟡 HYPOTHESIS (both the DLCI 0x02 write and the SASS correlation).
- **Evidence**: `CAP-019-FINDINGS.md` §4 (`[VERIFIED-LOCAL]`, 2026-08-21, frame 2293 + frames
  2296–2319).
- **Verified with experiment**: `CAP-019` (2026-08-21), single OFF→ON sample.

#### 4.5.3 Touch & Hold customization

- **Feature confirmed present**: "Use touch controls" top-level toggle (Device details → Controls
  and gestures), plus per-earbud "Press and hold" assignment (Toggle ANC / Digital assistant) and
  an ANC-mode rotation checklist. 🟢 FACT.
- **Top-level toggle opcode — 🟢 FACT, promoted 2026-08-30 (maintainer sign-off, `DECISIONS.md`
  ADR-019)**: `field5(len4){ field4(len2){ field4 = 1 } }` (`CAP-020`, `[VERIFIED-LOCAL]`
  2026-08-21, frame 1741) — the inner `field 4` is `libmaestro`'s own `WriteSetting` schema field
  4 (Java class `qhr`), independently confirmed by a 2026-08-30 APK static-analysis pass: write site
  `fyo.java:124-144`, read side logging `"Log Gestures Enable setting"` — a self-describing app-code
  match, not a naming inference, cross-validated against this wire evidence by the maintainer. 🔴 Not
  yet tested in the OFF direction, one session only.
- **Press-and-hold action selection opcode — 🟢 FACT, promoted 2026-08-30 (maintainer sign-off,
  `DECISIONS.md` ADR-019)** (`HOLD-001`–`HOLD-004`, `CAP-021`, `[VERIFIED-LOCAL]` 2026-08-21):
  `field5(len10){ field4(len8){ field7(len6){ field1|field2(len4){ field4(len2){ field1 = 5|6 } } } } }`
  — `field 1` (inside the `field 7` sub-wrapper) = Left, `field 2` = Right; the value sits one level
  deeper than originally notated (a `qik`→`qho` nested pair, not a bare varint directly under
  `field1`/`field2` — corrected 2026-08-30, see `CAP-021-FINDINGS.md`'s addendum): `5` = Active noise
  control, `6` = Digital assistant. All 4 combinations (Left/Right × ANC/Assistant) exercised, each
  producing exactly the predicted field/value pair with no exceptions; frame 1903 (Rcvd echo)
  independently contains both the new and a second value in one message. The wire-level field 7 is
  independently confirmed as `libmaestro`'s own `qju` schema type by the same 2026-08-30 static pass:
  write site `fyo.java:300-374` (`t(gdx)`), read side logging the self-describing
  `"Log Gestures Customization for touch and hold setting, left: %s, right: %s"` — naming this exact
  feature by name, not merely matching its shape.
- **ANC-mode rotation checklist opcode** (`HOLD-005`, `CAP-021`): `field5(len12){ field4(len10){
  field12(len8){ field1..4 = 0|1 } } }` — four boolean flags, 🟡 HYPOTHESIS field order = on-screen
  top-to-bottom order (Noise cancellation / Off / Adaptive / Transparency). **Not resolved**: this
  envelope carries no Left/Right-distinguishing field, so which frames belong to which earbud's
  list is unconfirmed (§6). **Field-number identity only — 🟢 FACT, promoted 2026-08-30 (`DECISIONS.md`
  ADR-019)**: wire field 12 is confirmed to be `libmaestro`'s own `qht` schema type (a 4-boolean
  message, write site `hgj.java:216-331`, read side logging `"Log ANC gesture loop to Clearcut"`).
  **Explicitly not promoted**: whether the app's own internal name for this field, "ANC gesture
  loop," is the *same* feature as this bullet's "ANC-mode rotation checklist" reading — the two could
  describe the same setting from two angles, or two different settings sharing a 4-boolean shape;
  the maintainer reviewed this specifically and declined to promote that equivalence — it remains 🟡
  HYPOTHESIS.
- **Sent to**: DLCI 0x02 for all three sub-features.
- **Status**: 🟢 FACT for the top-level toggle and press-and-hold-action opcodes (field numbers,
  values, and — independently — the app's own schema/field identity for both); 🟢 FACT for the
  rotation-checklist's field-number identity only; 🟡 HYPOTHESIS for the rotation-checklist's own
  field-order/on-screen-mapping reading and for whether it's the same feature as `qht`'s "ANC gesture
  loop."
- **Evidence**: `CAP-020-FINDINGS.md` §3, §8 (2026-08-30 addendum); `CAP-021-FINDINGS.md` §3–§4, §8
  (2026-08-30 addendum); `REVERSE_ENGINEERING.md`'s `qhr`/`qjo`/`qju`/`qjg`/`qht` entries (2026-08-30
  updates); `DECISIONS.md` ADR-019.
- **Verified with experiment**: `CAP-020` (top-level toggle), `CAP-021` (press-and-hold + checklist),
  both 2026-08-21.

#### 4.5.4 Head gestures

- **Feature confirmed present**: "Use head gestures" toggle (Device details → Controls and
  gestures), gated behind the touch-controls screen; tapping it the first time also shows a
  one-time "Optimize head gestures" explainer dialog (client-side only, no separate wire event tied
  to its dismissal). 🟢 FACT.
- **Opcode/payload**: `field5(len5){ field4(len3){ field29 = 2 } }`. 🟡 HYPOTHESIS — `field 29` =
  Head gestures; the wire write fires on the tap itself, ~1s before the explainer dialog even
  renders.
- **Sent to**: DLCI 0x02.
- **Status**: 🟡 HYPOTHESIS.
- **Evidence**: `CAP-020-FINDINGS.md` §4 (`[VERIFIED-LOCAL]`, 2026-08-21, frame 1935).
- **Verified with experiment**: `CAP-020` (2026-08-21), single OFF→ON sample.

#### 4.5.5 In-ear detection

- **Feature confirmed present**: toggle at Device details → More settings → In-ear detection.
  🟢 FACT.
- **Opcode/payload**: `field5(len4){ field4(len2){ field2 = 0|1 } }`. 🟡 HYPOTHESIS — `field 2` =
  In-ear detection, both ON and OFF video-confirmed.
- **Sent to**: DLCI 0x02.
- **Status**: 🟡 HYPOTHESIS.
- **Evidence**: `CAP-024-FINDINGS.md` §3 (`[VERIFIED-LOCAL]`, 2026-08-21, frames 1850/1912).
- **Verified with experiment**: `CAP-024` (2026-08-21), both directions sampled.

#### 4.5.5a Mono audio

- **Feature confirmed present**: "Mono audio" toggle at Device details → Sound. 🟢 FACT (UI
  presence).
- **Opcode/payload — full identity 🟢 FACT, promoted 2026-09-03 (maintainer sign-off, `DECISIONS.md`
  ADR-019)**: `field5(len5){ field4(len3){ field19 = 0|1 } }` (varint), `field 19` = `libmaestro`'s
  own `qhr` schema field 19, independently confirmed by APK static analysis: write site
  `fyo.java:278-298` (`s`), read side logging `"received mono setting value"` (`fxb.java` case 19)
  — a self-describing app-code match to this section's own "Mono audio" reading, not a naming
  inference. Both directions (ON/OFF) video-confirmed on the wire.
- **Sent to**: DLCI 0x02.
- **Status**: 🟢 FACT for the field-number identity and semantic name ("Mono audio").
- **Evidence**: `CAP-022-FINDINGS.md` §3 (`[VERIFIED-LOCAL]`, 2026-08-21, frames 1621/1823);
  `REVERSE_ENGINEERING.md`'s `qhr` entry; `DECISIONS.md` ADR-019.
- **Verified with experiment**: `CAP-022` (2026-08-21), both directions sampled.

#### 4.5.6 Volume EQ

- **Feature confirmed present**: toggle at the bottom of Device details → Sound → Equalizer (not
  the top-level Sound page). 🟢 FACT.
- **Opcode/payload**: `field5(len4){ field4(len2){ field15 = 0|1 } }`. 🟡 HYPOTHESIS — `field 15` =
  Volume EQ, both directions video-confirmed.
- **Sent to**: DLCI 0x02.
- **Status**: 🟡 HYPOTHESIS.
- **Evidence**: `CAP-022-FINDINGS.md` §4 (`[VERIFIED-LOCAL]`, 2026-08-21, frames 1871/1895).
- **Verified with experiment**: `CAP-022` (2026-08-21), both directions sampled.

#### 4.5.7 Volume Balance (L/R slider)

- **Feature confirmed present**: "Balance" slider at Device details → Sound. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
  §1 claims this is stored locally on the earbuds (persistent, works across devices) — **not tested
  this batch** (no disconnect/reconnect cycle captured after setting it).
- **Opcode/payload — 🟢 FACT, promoted 2026-09-03 (maintainer sign-off, `DECISIONS.md` ADR-019)**:
  `field5{ field4{ field17 = N } }`, `field 17` = `libmaestro`'s own `qhr` schema field 17,
  independently confirmed by APK static analysis: write site `fxf.java:82-133` (case 16 of that
  dispatcher), read side logging `"received last saved volume balance setting value"` (`fxb.java`
  case 17) — a self-describing app-code match to this section's own "Volume balance" reading, not a
  naming inference. **Correction accompanying this promotion**: `qhr`'s own schema types field 17 as
  `SINT32` (`REVERSE_ENGINEERING.md` line 856), so its wire values must be zigzag-decoded, not read
  as raw unsigned varints as this section previously did. The 7 samples from one continuous drag
  gesture (`CAP-022` frames 1922/1944/2019/2039/2056/2073/2099), correctly zigzag-decoded
  (`(n>>1) ^ -(n&1)`), are `-100, -62, -25, 15, 75, 100, 5` (previously misread as the raw unsigned
  varints `199, 123, 49, 30, 150, 200, 10`).
- **Sent to**: DLCI 0x02.
- **Status**: 🟢 FACT for the field-number identity and semantic name ("Volume balance"). 🔴 still
  open: the scale/range beyond these 7 samples, which direction (Left/Right) corresponds to negative
  vs. positive values, and persistence across a disconnect/reconnect — the zigzag correction narrows
  but does not resolve any of these; none is derivable from the corrected values alone, and would
  need isolated extreme-position samples with video correlation.
- **Evidence**: `CAP-022-FINDINGS.md` §5 (`[VERIFIED-LOCAL]`, 2026-08-21, frames 1922–2099, raw hex
  and corrected zigzag decode backfilled 2026-09-03); `REVERSE_ENGINEERING.md`'s `qhr` entry;
  `DECISIONS.md` ADR-019.
- **Verified with experiment**: `CAP-022` (2026-08-21) — a single continuous drag, not isolated
  extreme-position samples.

#### 4.5.8 Case sounds

- **Feature confirmed present**: Device details → More settings → Case sounds, with two toggles
  labeled **"Bud return"** (app's own settings-list wording: "Earbuds replaced") and **"Other
  alerts"** ("Other notifications"). 🟢 FACT.
- **Opcode/payload**: `"Bud return"` = `field5(len5){ field4(len3){ field28 = 0|1 } }`; `"Other
  alerts"` = `field5(len5){ field4(len3){ field27 = 0|1 } }`.
  - **`field 28` ("Bud return") — full identity 🟢 FACT, promoted 2026-09-03 (maintainer sign-off,
    `DECISIONS.md` ADR-019)**: `libmaestro`'s own `qhr` schema field 28, independently confirmed by
    APK static analysis: write site `fyo.java:58-78` (`d`), read side logging `"received bud return
    sound setting value"` (`fxb.java` case 28) — a self-describing app-code match to this section's
    own "Bud return"/"Earbuds replaced" reading, not a naming inference.
  - **`field 27` ("Other alerts") — category-level identity only 🟢 FACT, promoted 2026-09-03
    (maintainer sign-off, `DECISIONS.md` ADR-019)**: `libmaestro`'s own `qhr` schema field 27,
    independently confirmed by APK static analysis: write site `fyo.java:80-100` (`e`), read side
    logging `"received case earcon setting value"` (`fxb.java` case 27) — a self-describing app-code
    match confirming this is a real, code-confirmed case-sound-family boolean. **Not promoted**: the
    code's own log message is generic ("case earcon setting"), not specific to which case sound — it
    does not itself distinguish "Other alerts" from any other case-sound toggle, so the specific
    "Other alerts"/"Other notifications" label remains 🟡 HYPOTHESIS, based only on this section's own
    wire/video correlation (`CASE-002`), not reconciled with the generic code-side name.
- **Sent to**: DLCI 0x02. No case-specific vs. bud-specific channel/address distinction was found —
  both use the same shared DLCI 0x02 envelope as every bud-targeted setting.
- **Status**: 🟢 FACT for `field 28`'s full identity ("Bud return"). 🟢 FACT for `field 27`'s
  category-level identity (a real case-sound-family boolean); 🟡 HYPOTHESIS for `field 27`'s specific
  "Other alerts"/"Other notifications" label. The `"Bud return"` OFF sample (frame 1988) is not
  cleanly disambiguated between a genuine tap and a screen-open state sync — the ON sample and both
  `"Other alerts"` samples are unambiguous; this does not affect the field-identity promotions above.
- **Evidence**: `CAP-024-FINDINGS.md` §4–§5 (`[VERIFIED-LOCAL]`, 2026-08-21, raw hex backfilled
  2026-09-03); `REVERSE_ENGINEERING.md`'s `qhr` entry; `DECISIONS.md` ADR-019.
- **Verified with experiment**: `CAP-024` (2026-08-21), both toggles, both directions.

#### 4.5.9 Not yet mapped

The following remain 🔴 unconfirmed at the protocol level — no capture has targeted them yet:

- Loud Noise Protection (firmware 4.467+; likely on-device DSP only, no wire-visible command
  expected — see §6).
- Adaptive Audio dynamic adjustment (firmware 4.467+; likely on-device DSP only — see §6).

> Duplicate §4.1–§4.4's structure per command once each is captured and confirmed (opcode/payload
> structure, target channel, expected response, status, evidence, verifying experiment) — this is
> now done for every setting captured through `CAP-024`; extend §4.5.1–§4.5.8 or add a new
> subsection as further settings are confirmed, rather than reverting to a bare bullet list.

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
- **`CAP-013`** (2026-08-26, official app, following a phone-wide "Reset Bluetooth & Wi-Fi"):
  frames 117–270, 17:11:46.737–48.843. Same shape again, a ~1.2s IO-Capability-to-Complete gap. A
  fifth independent confirming instance of this path — see `CAP-013-FINDINGS.md` §2/§7 for the
  full frame table (this capture's own purpose was `PAIR-004`, not this path itself, which was
  already 🟢 FACT before this session).
- **`CAP-031`** (2026-08-27, official app, following a genuine narrow per-device "Forget"): frames
  598–689, 06:07:15.111–16.451. Same shape again, a ~0.4s IO-Capability-to-Complete gap. A sixth
  independent confirming instance of this path — see `CAP-031-FINDINGS.md` §2/§7 (again, this
  capture's own purpose was `PAIR-004`'s still-open primary question, not this path itself).
- **`CAP-032`** (2026-08-27, official app, following a genuine narrow per-device "Forget"): frames
  1090–1153, 18:31:26.776–28.019. Same shape again, a ~0.6s IO-Capability-to-Complete gap. A
  seventh independent confirming instance of this path — see `CAP-032-FINDINGS.md` §2/§7 (this
  capture's own purpose was, for the first time, actually resolving `PAIR-004`'s primary question —
  see §6's "Behavior" entry below).

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

**Reconnect, Buds-initiated variant — 🟢 FACT, added 2026-08-18, maintainer sign-off obtained
2026-08-28 (`DECISIONS.md` ADR-016) (`CAP-016-FINDINGS.md` §1):**
where `CAP-001`'s reconnect is a phone-side `Create Connection` (needing 3 attempts), `CAP-016`
shows the *Buds* paging the phone instead — a single `Rcvd Connect Request` → `Sent Accept
Connection Request` → `Rcvd Connect Complete` sequence, landing within 0.5s of the on-camera
earbud removal from the case (frames 1213–1217, 06:32:02.531–749). Same stored-key tail as
`CAP-001`'s path (no IO Capability/SSP exchange visible in this window). Not yet reconciled with
*why* one session pages and the other is paged — plausibly which side (phone vs. Buds) detects
the case-open/bud-removal event first, not yet tested directly.

**Common tail, both paths:** `Authentication Complete` → `Set Connection
Encryption` → `Encryption Change`, converging to the same encrypted classic
link regardless of which path reached it.

**Third path — Cross-Transport Key Derivation (CTKD), gated on a pre-existing LE link — 🟡
HYPOTHESIS (strengthened 2026-08-26), PROPOSAL awaiting maintainer sign-off for promotion to 🟢
FACT.** `CAP-004-FINDINGS.md` §2 first observed a third bonding path when a BLE (LE Secure
Connections) link to the Buds already existed before classic pairing began (nRF Connect,
`CAP-004`): `Delete Stored Link Key` → SMP `Pairing Request` (requesting `Linkkey` key
distribution) → Public Key/Confirm/Random → `DHKey Check` → classic `Create Connection` →
`Link Key Request Reply` (not Negative) — i.e. the classic link key is derived from the LE pairing
rather than negotiated via classic SSP. `CAP-004-FINDINGS.md` §10 explicitly withheld this from
promotion, since it rested on one capture with a specific confound: nRF Connect's early BLE
connection might itself be *why* CTKD occurred, not the GMS-disabled/no-app condition that
session was actually testing. **`CAP-012` (2026-08-26) directly tested this as a controlled
hypothesis test** (`CAP-012-FINDINGS.md` §2/§10): repeating the same GMS-disabled/no-app
condition with no BLE tool at any point, and independently confirming zero BLE connection to the
Buds anywhere in that session's log, produced classic SSP instead — not CTKD. Combined with
`CAP-002`/`CAP-003` (classic SSP, official app / nRF Connect but classic-only pairing path — no
early BLE link either), the pattern across all captures to date is a clean split: **classic SSP in
every session with no pre-existing LE link to the Buds (`CAP-002`, `CAP-003`, `CAP-012`); CTKD in
every session that had one (`CAP-004`, and now `CAP-014` — 2026-08-27, `CAP-014-FINDINGS.md` §5 — a
second, independently confirming CTKD instance: SMP `Pairing Request` with `Linkkey` distribution →
Public Key/Confirm/DHKey Check → classic `Create Connection` → `Link Key Request Reply`, again
initiated by a BLE tool, nRF Connect, connecting first).** This is a direct causal isolation from a
purpose-built repeat, not merely a repeated negative — but per `AGENTS.md` §6, promoting "an LE
Secure Connections link already existing gates CTKD vs. classic SSP" into this section's own 🟢 FACT
connection-lifecycle diagram is left to the maintainer rather than done unilaterally here.

**Not covered by this promotion — still ⚪ ASSUMPTION:** the RFCOMM
channel-opening sequence, the Message Stream/`libmaestro` handshake ordering,
and exactly when the first battery notification/app command arrives relative
to the classic link completing (steps 3–6 in the diagram above). Only the
classic BR/EDR link-establishment mechanics (steps 1–2) are promoted here.

**Status**: 🟢 FACT for classic BR/EDR link establishment (§5.1, seven
independent captures); ⚪ ASSUMPTION for the RFCOMM/Message-Stream/battery/
command portions (steps 3–6); 🟢 FACT for step 5's specific behavioral outcome
(battery notification on reconnect), per `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §3.
**Evidence**: §5.1 above for the classic-link portion (`CAP-001` frames
732–917, `CAP-002` frames 653–734, `CAP-003` frames 1621/1687–1756, `CAP-016`
frames 1213–1217, `CAP-013` frames 117–270, `CAP-031` frames 598–689, `CAP-032`
frames 1090–1153); steps 3–6 still need a full connection sequence captured
end-to-end (see `CAPTURE_BLUETOOTH_HCI_SNOOP.md`).

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
- [x] **Added 2026-08-15, resolved 2026-08-21 (`CAP-023`): wire-baseline firmware version, distinct
      from the confirmed UI-baseline.** §0.1 documents `release_5.203` as the `[VERIFIED-LOCAL]`
      UI-baseline (official app screenshot, 2026-07-30); the requested capture that also records
      the app's firmware-display screen now exists (`CAP-023`, 2026-08-21) — the on-screen
      "Device firmware version" (Left/Right/Case, all `release_5.203`) matches DLCI 0x08's private
      envelope string byte-for-byte, same session. 🟢 **FACT, promoted 2026-08-23** (maintainer
      sign-off obtained, `DECISIONS.md` ADR-012): `"release_5.203"` is what the app calls "the
      firmware version."
      `"Revision 6"` (DLCI 0x04's official field) and `"cape2_sm"`/`"500m"`–`"500p"` remain
      un-surfaced by the app's own UI — see §0.1's 2026-08-21 update for the full detail.

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
      **Partially advanced 2026-08-23 for a related, previously-unlisted group:** `Group 0x0e`
      (also on DLCI 0x08, structurally known since `CAP-002-FINDINGS.md` §2a but not in this
      item's original group list) — `Code 0x01`'s "3 varint-triple entries" shape now has a
      proposed semantic reading (per-earbud battery, §4.3 Option E), 🟡 HYPOTHESIS pending
      sign-off; `Code 0x02`'s value was already known (`"google-pixel-buds-pro-v1"`). Groups
      `0x01`/`0x02`/`0x05`/`0x09` themselves remain fully open, unaffected by this.
- [x] **Added 2026-08-23, resolved same day (`CAP-011-FINDINGS.md` §7c):** DLCI 0x08's
      `Group 0x0e Code 0x01` battery message's 3rd entry (index=3) is **Case** — confirmed via a
      clean 3-for-3 match (Left/Right/Case) against on-screen values in 2 independent captures
      (`CAP-001` frame 1114, `CAP-002` frame 49024). **Not fully closed:** in `CAP-011` specifically,
      idx=3 reads a stale, non-matching value (92 vs. on-screen 89%) and lacks the `flag` field
      every fresh entry carries elsewhere — a plausible but unconfirmed explanation (that session's
      case sat open/empty, possibly preventing a fresh case-battery read) is offered in
      `PROTOCOL.md` §4.3 Option E, not resolved further.
- [ ] **Added 2026-08-23, `CAP-011-FINDINGS.md` §7:** what triggers DLCI 0x08's `Group 0x0e`/
      `Group 0x04 Code 0x03` battery-push burst? Recurs at irregular intervals (4:02, 2:56, 8:21
      apart in one session) — checked against that session's own near-continuous BLE
      connect/disconnect churn and found no correlation (the churn is far more frequent than this
      burst). Not yet checked against any other candidate trigger.
- [ ] Added 2026-08-14: DLCI 0x08's own purpose/ownership as a whole channel is still 🔴 open.
      Ruled out as `libmaestro` specifically (§2.3's 2026-08-14 addendum — no HDLC framing, unlike
      the one concrete transport signature `pbpctrl` documents for Maestro). Leading remaining
      candidate: a lower-level Nearby/CDM companion-device negotiation, independent of both Fast
      Pair (DLCI 0x04) and `libmaestro` (DLCI 0x02) — not confirmed.
      **Update (2026-08-30, `CAP-033-FINDINGS.md` §3, audit finding):** the channel now has an
      on-the-wire SDP service name, **"GSND CONTROL"** (UUID `f8d1fbe4-7966-4334-8024-ff96c9330e15`,
      RFCOMM channel 4) — a new search lead (see §2.3's 2026-08-30 update), not a resolution; no
      match for "GSND" found in a first APK keyword pass. Still 🔴 open.
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
      - ~~Which of the 5 decoded `float32` fields maps to which of the 5 UI sliders is inferred
        from only one slider (Bass) having been moved this session — needs a capture isolating a
        *different* single slider to confirm.~~ **Resolved 2026-08-18**, per a fresh, separate
        `CAP-015` session (`captures/CAP-015-2026-08-18_06-11-06_06-17-40-Group_T/CAP-015-FINDINGS.md`
        §5) that drags all 5 sliders individually, 3 passes each — field 1↔Low bass, 2↔Bass,
        3↔Mid, 4↔Treble, 5↔Upper treble (wire order reversed from the on-screen top-to-bottom
        order), matching this capture's own single-band inference exactly. Promoted to 🟢 FACT,
        `PROTOCOL.md` §4.2.
      - Whether the outer field number (`16` during preset-tap/drag, `18` at the `Save` tap) means
        "preview" vs. "save", or something else — needs a capture that drags a slider and
        deliberately never taps `Save`. **Still open** as of 2026-08-18 — that capture's 15
        field-18 frames all fire within ~2s of the preceding field-16 write with no video-visible
        `Save`-button tap in between, revising (not confirming) the reading to "field 18 = slider
        release" — see `PROTOCOL.md` §4.2 and that capture's own §6.
      - ~13 bytes of apparent `call_id`/correlation data (payload offset 1–12, echoed back
        verbatim by the Buds) are present but undecoded.
      - **Checked 2026-08-17 (deskresearch pass, `DESKRESEARCH_FINDINGS.md`): whether DLCI 0x02's
        field-16/18 pair is EQ-specific or a general-purpose `libmaestro` "apply/save" pair also
        used by ANC/other settings.** CAP-005's own exact structural check (three independent
        nested-length self-consistency assertions) was re-run against every Sent-direction DLCI
        0x02 payload in `CAP-001`, `CAP-002`, `CAP-003`, `CAP-006`, `CAP-007`, and the 11:42
        `CAP-010` session — including `CAP-006`'s four cleanly isolated single ANC taps, the best
        available test case for this exact question. **Clean negative result:** zero matches
        anywhere outside `CAP-005`'s own three already-known frames — no ANC tap, in any capture,
        produces a payload matching this envelope shape on DLCI 0x02, under any outer field
        number. 🟡 HYPOTHESIS (strengthened, not yet 🟢 FACT — still only one capture ever produced
        this shape at all): the field-16/18 envelope is EQ-specific, not a general-purpose
        settings-apply/save pair. See `DESKRESEARCH_FINDINGS.md`'s 2026-08-17 entry for the full
        method and per-frame results.
- [ ] **Added 2026-08-17, deskresearch pass (`DESKRESEARCH_FINDINGS.md`):** DLCI 0x02's HDLC
      **Address** field appears to be renegotiated per connection/reconnect, not a small fixed set
      of protocol-level constants as `PROTOCOL.md` §2.2a's "two multiplexed pw_rpc channels"
      framing implied. Two new address pairs — `0x1e80`/`0x2680` (Sent, phone→Buds) answered by
      `0xe980` (Rcvd, Buds→phone) — appear in `CAP-005` and `CAP-007`, always in a burst
      immediately following a connection/channel-reopen event (not the *first* connection
      handshake), and carry the **same** "device serial + firmware string, repeated 3×" content
      already documented on the `0x0000`→`0xD180` pair since `CAP-001` — including in `CAP-007`,
      which never used the official app at all (system Bluetooth Settings only), yet still
      produced this exact response, on the new address pair instead of the original one. 🟡
      HYPOTHESIS: the Address field is a per-connection-negotiated pw_rpc client/channel handle
      rather than a fixed value, extending the already-established "RFCOMM server channel numbers
      are session-local, not profile-fixed" methodological note (`CAP-001-FINDINGS.md` §2) one
      layer deeper, into DLCI 0x02's own internal addressing. Not confirmed: why the request is
      apparently duplicated on two addresses at once (`0x18` vs. `0x1a` as an inner field-2 value,
      correlating 1:1 with which address carries it) — genuinely unresolved, not guessed at. See
      `DESKRESEARCH_FINDINGS.md`'s 2026-08-17 entry.
- [x] **Added 2026-08-18, from `CAP-010-FINDINGS.md` §3 (11:42 session) — byte-level detail for two
      GATT handles already known to be part of the `0x0c0X` Key-based-Pairing-shaped cluster
      (§4.3 Option D context), not yet spec-identified. Resolved 2026-09-01, see the `CAP-034`
      update below.** Originally 🟡 HYPOTHESIS, tracked as open — **not**
      promoted as resolved facts at the time:
      - `0x0c0c`: `Notification`, 40 bytes (frame 2020) — handle already known to be in the
        cluster (`CAP-003-FINDINGS.md` §4), payload length not previously characterized.
      - `0x0c13`/`0x0c14`: `0x0c13` carries 9-byte (`Read`), 10-byte (`Write`), and 32-byte
        (`Notification`) payloads; `0x0c14` carries 2-byte CCCD enable/disable writes. These
        lengths do **not** cleanly fit the 16-byte AES-block pattern seen on `0x0c04`/`0x0c05`/
        `0x0c0a` — possibly a structurally distinct characteristic from the Key-based-Pairing
        pair (a leading `0x01` byte precedes the payload on all three `0x0c13` values, not
        decoded further). Not independently confirmed against any spec.
      **PROPOSAL, pending maintainer approval, added 2026-08-27 from `CAP-014-FINDINGS.md` §4c —
      all of the above byte-length/leading-byte characterizations reproduce exactly a 3rd/4th time**
      (`0x0c0c` 41B notify, `0x0c13` 9B-Read/10B-Write/32B-Notify each with a leading `0x01`,
      `0x0c14` 2B CCCD) in an independent session 11 days later, on the same physical device —
      strengthens confidence these are stable characteristic shapes, not session artifacts, but
      did **not**, at the time, change their status: still 🟡 HYPOTHESIS, still not resolved to real
      UUIDs. The **handle↔UUID mapping question itself remained 🔴 OPEN QUESTION** after this 3rd
      Group-W attempt (`CAP-010`, `CAP-017`, `CAP-014`) — `CAP-014` confirmed its own wire log was
      not truncated (unlike `CAP-017`), but found a **different** blocking cause: the session reused
      an already-bonded phone with a cached GATT client, so Android served this cluster from its
      cached database instead of re-declaring it live on the wire (only the GATT service itself,
      handles `0x0001`–`0x0009`, was genuinely re-discovered). Neither of Group W's own candidate
      cache-busting methods (`pm clear com.android.bluetooth`, or a phone that has never connected
      to this Buds unit before) had been tried in any of the 3 sessions to that point.

      **🟢 FACT, resolved 2026-09-01 (`CAP-034`, maintainer sign-off obtained per `AGENTS.md` §6) —
      the handle↔UUID mapping for this entire cluster, and the full 15-primary-service GATT profile,
      is now known.** `CAP-034` (4th Group W attempt) combined, for the first time, an unlimited HCI
      snoop snaplen with a genuine full-database GATT cache miss (`pm clear com.android.bluetooth`
      run on a Pixel 9a that had never before connected to this Buds unit, plus a fresh nRF Connect
      install) — the resulting 06:47:42.147–45.490 discovery burst (frames 3264–3469) is a complete,
      untruncated `Read By Group Type`/`Read By Type`/`Find Information` walk of the entire
      `0x0001`–`0xffff` handle space, decoded byte-for-byte and independently corroborated by nRF
      Connect's own on-screen UUID-name rendering (a second, client-side decoding path agreeing with
      the wire-hex decode on every field checked). See `captures/CAP-034-2026-09-01_06-46-31_06-52-45-Group_W/CAP-034-FINDINGS.md`
      §4 for the full command+hex evidence per handle. Resolved mapping:

      | Handle range | UUID | Service |
      |---|---|---|
      | `0x0c00`–`0x0c14` | `0xFE2C` | **Google Fast Pair Service** |
      | `0x0f20`–`0x0f2a` | `0x180A` | Device Information |
      | `0x0f30`–`0x0f33` | `0x180F` | Battery Service |
      | `0x0f37`–`0x0f3e` | `109b862f-50e3-45cc-8ea1-ac62de4846d1` | "Unknown Service" (name still unidentified — see below) |
      | `0x0c15`–`0x0c18` | `15190001-12f4-c226-88ed-2ac5579f2a85` | Accessory Non-Owner Service (out of scope, `DECISIONS.md` ADR-008) |

      Within the Fast Pair Service, every characteristic in the `0x0c0X` cluster this project has
      tracked by byte-shape alone since `CAP-002` now has a spec-verified name (live-checked against
      `developers.google.com/nearby/fast-pair`, not recalled from training data):

      | Value handle | CCCD | UUID | Name | Properties |
      |---|---|---|---|---|
      | `0x0c02` | — | `FE2C1233…` | Model ID | Read |
      | `0x0c04` | `0x0c05` | `FE2C1234…` | **Key-based Pairing** | Notify, Write |
      | `0x0c07` | `0x0c08` | `FE2C1235…` | Passkey | Notify, Write |
      | `0x0c0a` | — | `FE2C1236…` | Account Key | Write |
      | `0x0c0c` | `0x0c0d` | `FE2C1237…` | Additional Data | Notify, Write |
      | `0x0c0f` | — | `0x2A26` | Firmware Revision String (standard, 2nd copy) | Read |
      | `0x0c11` | — | `FE2C1239…` | Message Stream PSM Characteristic | Read |
      | `0x0c13` | `0x0c14` | `FE2C1238…` | **still unnamed** — checked against the base spec, the Message Stream extension, and the Personalized Name extension; not found on any of the three | Notify, Write, Read |

      This directly confirms, as an exact UUID/name match rather than only a byte-shape match, the
      FORM already hypothesized since `CAP-003-FINDINGS.md` §4 for the 80-byte-first-write/16-byte
      pattern on `0x0c04` (Key-based Pairing) and resolves `0x0f28`="1"/`0x0f2a`="Revision 6"
      (confirmed across `CAP-002`/`CAP-003`/`CAP-010`/`CAP-017`) as the Device Information Service's
      ordinary Serial Number String and Firmware Revision String — not a Fast-Pair-proprietary value.
      It also **corrects** a `CAP-017-FINDINGS.md` §6 hypothesis: "Unknown Service"
      (`109b862f-…`) is **not** the `0x0c0X` cluster's container — it occupies a separate handle
      range (`0x0f37`–`0x0f3e`) and its own purpose remains unidentified. **Not resolved by this
      capture:** `FE2C1238…`'s official name, and "Unknown Service"'s own purpose — both remain
      🔴 OPEN QUESTION, tracked in `CAP-034-FINDINGS.md` §8.
- [ ] **Added 2026-08-21, `CAP-019`–`CAP-024`:** what do DLCI 0x02's confirmed inner field numbers
      (§4.5's `field4`=touch controls, `field11`=Multipoint, `field15`=Volume EQ, `field17`=Volume
      balance, `field19`=Mono audio, `field22`=Conversation Detection, `field27`/`field28`=Case
      sounds, `field29`=Head gestures, plus §4.5.3's `field7{field1|2{field4=5|6}}` for
      press-and-hold) actually represent — stable per-setting/per-field schema IDs from a real
      `.proto` definition, or something else? No official spec or extracted schema confirms this;
      inferred purely from timing correlation across 9+ settings in 6 captures.
      **Partially resolved 2026-08-30, maintainer sign-off (`DECISIONS.md` ADR-019):** for `field 4`,
      `field 7`, and `field 12` specifically, the answer is now known — a 2026-08-30 APK
      static-analysis pass recovered `libmaestro`'s real `WriteSetting` request schema (Java class
      `qhr`) field-for-field, and re-decoding existing `CAP-020`/`CAP-021` frames confirmed these 3
      field numbers (plus, at wire-level only, field 29) match that recovered schema exactly, with two
      of them (7 and 12) additionally matching a self-describing log message in the app's own code.
      **Still open**: whether the *remaining* confirmed field numbers listed above (11, 15, 17, 19, 22,
      27, 28) also correspond 1:1 to this same recovered `qhr` schema's own field numbers — plausible
      given the pattern, but not individually checked against the recovered schema yet. See
      `PROTOCOL.md` §2.2a's 2026-08-30 update and `REVERSE_ENGINEERING.md`'s `qhr` entry.
      **Further resolved 2026-09-03, maintainer sign-off (`DECISIONS.md` ADR-019 Update):** `field
      17`, `field 19`, `field 22`, `field 27`, and `field 28` are now individually checked against the
      recovered `qhr` schema and confirmed to match it (§4.5.1, §4.5.5a, §4.5.7, §4.5.8) — `field 17`
      (full identity, plus a zigzag-decoding correction), `field 19` (full identity), and `field 28`
      (full identity) promoted in full; `field 22` and `field 27` promoted for field-number/type or
      category-level identity only, with their specific semantic-label equivalence left 🟡 HYPOTHESIS
      (see those sections for the reasoning). **Still open**: `field 11` (Multipoint) and `field 15`
      (Volume EQ) remain unchecked against the recovered schema — not part of this update.
- [ ] **Added 2026-08-21:** does DLCI 0x02's general-purpose `field5{field4{...}}` settings-write
      envelope (§4.5's shared preamble) generalize to *every* remaining `libmaestro` setting, or
      only to the ones captured so far? Does the `field7{field1|field2{...}}` Left/Right selector
      (§4.5.3) generalize to other per-earbud settings beyond press-and-hold?
- [ ] **Added 2026-08-21, `CAP-021-FINDINGS.md` §4:** which of `HOLD-005`'s 16 ANC-mode-rotation
      checklist frames belong to Left's list vs. Right's — the envelope carries no
      earbud-distinguishing field for this specific write, unlike `HOLD-001`–`HOLD-004`.
- [ ] **Added 2026-08-21, `CAP-022-FINDINGS.md` §5:** `field 17`'s (Volume balance) numeric
      scale/range and which direction (L/R) increasing values represent — a single continuous drag
      gesture (7 wire values) wasn't enough to resolve this at 1fps video-sampling resolution.
- [ ] **Added 2026-08-21, `CAP-019-FINDINGS.md` §4:** what do Fast Pair SASS (DLCI 0x04 Group
      `0x07`) Codes `0x11`/`0x21`/`0x40`/`0x42` encode beyond their raw bytes? Is Code `0x34`
      (which also fires with no Multipoint action nearby) a periodic/keepalive SASS code unrelated
      to Multipoint specifically?
- [ ] **Added 2026-08-21, `CAP-011-FINDINGS.md` §4:** why do the Fast Pair Service (`0xFE2C`) BLE
      advertisement payloads sampled in `CAP-011` not match `PROTOCOL.md` §4.3 Option A's
      documented Battery Notification byte layout (no sampled byte equals the expected `0x33`/`0x34`
      Length&Type marker at any offset)? Do the 5 rotating BLE addresses observed in that capture
      all belong to the same physical Buds/Case unit (RSSI proximity is supporting, not conclusive,
      evidence)?
- [ ] **Added 2026-08-21, `CAP-025-FINDINGS.md` §7; reopened 2026-08-23 against a corrected spec
      citation (§2.1's 2026-08-23 correction):** what do the Ring action's two observed ACK
      variants (`0xFF 0x01 0x00 0x02 0x04 0x01` and `0xFF 0x01 0x00 0x03 0x04 0x01 0x00`)
      represent, now that neither matches the spec's *actual* worked example
      (`0xFF 0x01 0x00 0x04 0x04 0x01 0x01 0x3C`, 4 data bytes)? The spec's own extra two bytes
      (`01 3C`) are a plausible status/result-code candidate for what the shorter observed
      variants are missing or encoding differently — not yet checked against a fresh capture.
- [ ] **Added 2026-08-21, `CAP-022-FINDINGS.md` §8:** does Volume balance (`field 17`) actually
      persist locally on the earbuds across a disconnect/reconnect, as
      `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §1's `AUDIO-003` row claims from the app's own on-screen
      text? Not tested — `CAP-022` only captured the write itself, no reconnect cycle.
- [ ] **Added 2026-08-21, `CAP-021-FINDINGS.md` §4a:** DLCI 0x0a (RFCOMM channel 5) — silent
      (channel-control frames only, zero payload) in every capture that has checked it before or
      since (`CAP-001`/`CAP-002`/`CAP-005`/`CAP-006`/`CAP-007`/`CAP-016`,
      `CAP-011`/`CAP-019`/`CAP-020`/`CAP-022`–`CAP-025`, and now `CAP-008`) — carried a 1123-frame
      payload burst in `CAP-021` alone (frames 2093–4926, ~179–277s into that session's log,
      dominant frame size 215 bytes). Structurally protobuf-tag-shaped (`0a d0 01` = field 1,
      length 208) but not decoded further, and not attributable to any single Group G Test-ID's
      tap time. What triggers this, and why it appears in exactly one session out of fifteen
      checked, is unresolved. **`CAP-008` (Group V, a real phone call — proposal awaiting
      sign-off) specifically tests and rules out one candidate: this is not the call's SCO/eSCO
      audio path** — `CAP-008-FINDINGS.md` §5/§6 shows the actual audio connection is a separate
      HCI-level synchronous connection (its own connection handle) that never touches RFCOMM/L2CAP
      framing at all, while DLCI 0x0a stayed open-but-empty through two full calls in that same
      session.
      **Refined characterization, added 2026-08-23 (external audit pass, independent re-analysis
      of all 1123 frames — offered as a research direction, not a conclusion):** the burst is
      **100% Rcvd-direction** (Buds→phone only; the phone never requests it) and arrives in ~5–6
      discrete bursty waves separated by multi-second gaps (8.4s/28.5s/22.5s between waves, vs.
      1–100ms within a wave) rather than as continuous streaming; the payload body is a long run
      of a repeating 3-byte pattern (`6d b6 db`) with a different repeating pattern (`7e ee ed`)
      near each frame's tail and ~20 bytes of higher-entropy data before that. This timing/entropy
      profile is more consistent with a **segmented bulk-data or capability/diagnostic dump**
      than continuous real-time sensor/audio telemetry, but this is weaker evidence than a spec
      match or cross-capture replication and stays 🔴 OPEN QUESTION — explicitly **not** promoted
      to a firmer hypothesis, consistent with this project's 2026-08-22 decision to decline a
      prior, less-supported "IMU/telemetry" guess about this same burst. A capture bracketing
      whatever background condition preceded ~08:02:29 in that session (app backgrounded?
      scheduled sync? battery/charge-state change?) would be needed to attribute a trigger.
- [ ] **Added 2026-08-18, `CAP-016-FINDINGS.md` §11:** a 73-frame `Handle Value Notification`
      burst on BLE ATT handle `0x0044` (connection handle `0x0002`), confined to a ~29s window
      right after the BLE link forms and before the classic link exists; 23 of the 73 contain a
      recurring `0xfea9` byte-pair marker. Not decoded — payloads don't obviously match any
      already-documented envelope shape, and the handle's own UUID was not resolved this session.
- [ ] **Added 2026-08-18, `CAP-016-FINDINGS.md` §10:** Bluetooth HID (PSM `0x0011`) Feature Report
      Id `0x01`'s response (frame 1983) is only 3 bytes (`a3 00 00`) — too short to carry any
      content comparable to Report Id `0x02`'s decoded `AndroidHeadTracker` string (same section,
      🟡 HYPOTHESIS, not yet promoted here); reported as short/near-empty, not decoded further, no
      content guessed.
- [ ] **Added 2026-09-04, `CAP-036-FINDINGS.md` §5:** DLCI 0x08's connect-time burst contains
      several `Sent` frames matching the identical `[Group:1][Code:1][Len(2BE)=0000]` zero-length
      shape as DLCI 0x04's confirmed "Get" pattern (§4.1): `05 0c 00 00`, `04 02 00 00`,
      `04 04 00 00`, `04 11 00 00`, `04 13 00 00`, `04 15 00 00`, and `0e 04 00 00`. None of these
      Group/Code pairs is mapped to any known setting — genuinely open, not claimed as a settings
      query given DLCI 0x08's semantics remain largely unresolved (see the `Group 0x01/0x02/0x05/
      0x09` item above).
- [ ] **Added 2026-09-04, `CAP-036-FINDINGS.md` §4:** a dense, RPC-shaped burst occurs on DLCI 0x02
      immediately after channel establishment (`CAP-036` frames 1404–1591, ~3.1s), containing three
      ASCII `"release_5.203"` firmware-version strings and many small request/response pairs
      sharing a partial match to §4.5's documented correlation-ID prefix (`03 10 XX 1d ea 71 de
      7e 25...`). Not decoded further — whether it carries any settings-state read-back via
      `libmaestro`'s own channel (as opposed to the DLCI 0x04 "Get ANC state" mechanism confirmed
      the same session, §4.1) is unresolved.
- [ ] **Added 2026-09-04, `CAP-036-FINDINGS.md` §3; strengthened same day by
      `DESKRESEARCH_FINDINGS.md`'s cross-check:** the "Notify ANC state" frame observed in
      `CAP-036` (`08 13 00 04 01 e8 00 20`, Settable=`0x00`) fires as the very first Notify right
      after a fresh RFCOMM connection — structurally the same situation as `CAP-016-FINDINGS.md`
      §4's own frame 1521 (`08 13 00 04 01 e8 00 20`, byte-identical), which that capture's own
      🟡 HYPOTHESIS already reads as "the Buds have not yet reported which ANC modes are currently
      selectable," specifically observed right after connect. **Two independent sessions now show
      the same connect-time `Settable=0x00` pattern** — this "discrepancy" against `CAP-001`'s
      `0xe8` samples (all taken well after connection had settled) looks increasingly like an
      instance of `CAP-016`'s already-documented pattern rather than a new, separate anomaly. Still
      🟡 HYPOTHESIS (2 sessions, not maintainer-reviewed for promotion) — not reconciled at FACT
      strength.
- [ ] **Added 2026-09-04, `CAP-036-FINDINGS.md` §12.6 (bonus battery/firmware analysis):** DLCI
      0x02's periodic push (§4.3 Option E's timing-correlation entry) decodes, HDLC-unescaped and
      CRC-32-verified, to a repeated triple pattern (`0a 04 08 64 10 01`, `12 04 08 64 10 01`,
      `1a 04 08 64 10 02`) that resembles but does not field-for-field match Option E's confirmed
      `[value, flag, index]` battery-triple shape (only 2 fields per entry here, not 3, and the
      trailing numbers are `01, 01, 02` rather than a clean `1, 2, 3` index). Not decoded further
      per `AGENTS.md` §13.6's zero-creativity rule — genuinely open whether `libmaestro`'s own
      channel carries a differently-shaped battery-adjacent message here, or something unrelated
      that happens to repeat the value `100`.

### Behavior

- [ ] **Added 2026-08-21, `CAP-025-FINDINGS.md` §7/§8 — directly relevant to this project's
      Zero-GMS goal (`AGENTS.md` §1).** Does "Find My Buds" for the Case and "both simultaneously"
      (`FIND-003`/`FIND-004`) genuinely require Google's Find My Device Network (an
      account/cloud-mediated path, video-confirmed showing a "Connecting…" state and copy
      referencing "another device linked with your Google Account"), with **no local-only
      fallback**? If so, this may be a hard limit on offline Case-ring support for this project's
      own implementation, not just an open research question — flagged for maintainer awareness.
      Related: what triggers the three repeated classic-RFCOMM-connection-reopen bursts
      (~40s apart) observed while this Find Hub flow was active in `CAP-025`?
- [ ] **Added 2026-08-21, `CAP-011-FINDINGS.md` §4/§5:** does an active classic RFCOMM connection
      suppress or alter the Buds' Fast Pair Battery Notification BLE advertisement? `CAP-011`'s own
      attempted passive-scan capture had an active connection present throughout (a procedure
      deviation, not the intended design), leaving this un-isolated from the capture's other
      finding (the sampled payload not matching the documented Battery Notification byte layout at
      all — see the Commands & schemas entry above).
- [ ] **Added 2026-08-21, `CAP-024-FINDINGS.md` §4:** does Case sounds' `"Bud return"` setting
      (`CASE-001`) require an explicit tap to register a write even when the value doesn't change,
      or does opening the "Case sounds" screen itself trigger a state-sync write on DLCI 0x02?
      One sampled frame (1988) isn't disambiguated between these two readings from video alone.
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
- [ ] **Added 2026-08-14; narrowed 2026-08-26 (`CAP-008-FINDINGS.md` §3, Group V, proposal awaiting
      sign-off):** why HFP AT-command traffic never recurs after `CAP-001`'s own handshake —
      confirmed as a genuine negative in `CAP-002` (zero `AT+` traffic anywhere else across a full
      8+ hour shared log spanning multiple reconnects, `CAP-002-FINDINGS.md` §5). `CAP-008`
      (Bluetooth radio switched off then back on, a fresh classic-link connection) shows the full
      SLC handshake **does** reoccur on that kind of reconnect, structurally identical to
      `CAP-001`'s — narrowing the question to "which reconnections retrigger it," not "does it ever
      recur." Still open: whether a reconnect that does *not* involve a full radio power-cycle
      (e.g. `PAIR-003`, disconnect/reconnect to an already-bonded device without toggling
      Bluetooth off) also retriggers it, or whether `CAP-002`'s negative result specifically
      reflects that its ACL connection was simply never torn down within that log's window — not
      tested by either capture to date.
      **Further narrowed 2026-08-26 (`CAP-012-FINDINGS.md` §6, Group S repeat, incidental —
      proposal awaiting sign-off):** this exact untested case now has a data point. A manual
      disconnect + reconnect via system Bluetooth settings (no radio toggle — `PAIR-003`,
      `CAP-012`'s Sequence 2) **does** retrigger the full HFP AT-command SLC handshake on DLCI
      0x0c, the same shape as `CAP-008`'s radio-power-cycle case. Two independently-triggered
      reconnect types now both show recurrence and none show `CAP-002`'s original silence —
      `CAP-002`'s negative result increasingly looks attributable to "that session's ACL
      connection was simply never torn down" rather than to which reconnect mechanism is used,
      but this is two data points, not yet a settled rule. (`CAP-012`'s own log was severely
      ACL-truncated, so only the handshake's *recurrence*, not its exact AT-command content, is
      confirmed here.)
- [ ] Added 2026-08-14: live GATT primary-service discovery requires stronger cache-busting than
      bond removal — confirmed as a genuine requirement, not an assumption: three independent
      captures (`CAP-002`, `CAP-003`, `CAP-004`) all failed to trigger a live `Read By Group Type`
      response against the Buds despite bond removal beforehand in two of them
      (`CAP-003-FINDINGS.md` §1, `CAP-004-FINDINGS.md` §6). `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
      Group W (new) proposes two untried, stronger candidates.
- [ ] **Added 2026-08-18, `CAP-016-FINDINGS.md` §3/§9:** what triggers the RFCOMM multiplexer
      channel-bounce class (all 4 DLCIs `DISC`+reopened in sequence, ACL link itself undisturbed)?
      Confirmed **not** solely tied to bud removal — `CAP-016` reproduces the same bounce shape
      with no camera-visible cause anywhere nearby, while `CAP-007`(old) shows one coincident with
      a bud removal. No positive mechanism identified in either capture.
- [ ] **Added 2026-08-18, `CAP-016-FINDINGS.md` §3/§9:** why does the ANC Notify's
      settable-toggles byte revert from `0xe8` to `0x00` a second time (frame 3054, 06:33:23.456),
      after already having re-announced `0xe8`/Transparency twice during the same channel bounce
      (frames 2768, 3012)? Not resolved by any video-visible action in that capture.
- [ ] **Added 2026-08-18, `CAP-016-FINDINGS.md` §6:** docking a bud produces no distinct "docked"
      wire event either — the ±3s window around each of the two bud-redocking actions this
      session shows only routine periodic traffic (or unrelated background BLE scan noise), no
      RFCOMM data frame, ANC re-notify, or DLCI 0x08 Code `0x12` push tied to the docking action
      itself; only the eventual `Disconnection Complete` once *both* buds are back (§5's promoted
      FACT).
- [ ] **Added 2026-08-18, `CAP-016-FINDINGS.md` §2/§9:** does the second, distinct BLE address
      (`4f:25:00:85:9a:b1`, connected 06:31:40.983) actually belong to the same physical Buds unit
      as classic peer `04:00:6e:cf:6e:07`? Time-coincident only — not content-verified in that
      pass; a GATT-level read of that handle's advertised service data would settle it.
      **Second occurrence, 2026-08-26 (`CAP-013-FINDINGS.md` §6):** a further second BLE link,
      to yet another random/resolvable address (`43:8a:82:03:4b:f2`), forms shortly after classic
      pairing/RFCOMM-channel-open in that session — same open question (not content-verified as the
      Buds' own address), not yet the same address as `CAP-016`'s either, so this doesn't confirm a
      stable secondary identity, only that the pattern (an unattributed second BLE link appearing
      around connection time) recurs.
      **Tested and not reproduced, 2026-08-27 (`CAP-031-FINDINGS.md` §6), PROPOSAL — pending maintainer approval:** a third capture (`CAP-031`) checked its full log for any
      `LE Enhanced Connection Complete` beyond the Buds' own link — found exactly one, resolving to
      the Buds' own public address (`04:00:6e:cf:6e:07`), with zero occurrences of either
      `43:8a:82:03:4b:f2` or `4f:25:00:85:9a:b1`. This is a clean negative data point (the
      phenomenon is not universal, plausibly session-specific noise or a nearby unrelated device
      rather than a stable Buds-side secondary identity) but does **not** itself resolve what either
      prior address actually was — both remain 🔴 OPEN independently.
- [ ] **Re-raised 2026-08-26, still unresolved from `CAP-001-FINDINGS.md` §6 (primary question
      `CAP-013` was meant to answer, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `PAIR-004`):** did a BLE
      link and/or a still-valid classic link key already exist for this peer *before* the on-screen
      clearing action (`CAP-001`'s "Forget" tap at 08:51:02–03, pre-dated by a BLE link at
      08:50:36)? `CAP-013` (2026-08-26) attempted the planned repeat — start HCI snoop logging
      before any association exists at all — but logging in that session did not actually begin
      until 2m21s *after* its own clearing action ("Reset Bluetooth & Wi-Fi", not a single-device
      "Forget") and after the entire subsequent case-open/pair-button/device-selection sequence
      (`CAP-013-FINDINGS.md` §0). **Still 🔴 OPEN QUESTION, not narrowed by `CAP-013`** — a genuine
      repeat, with logging verified to start before the clearing action itself, is still needed.
      What `CAP-013` *did* confirm: the classic-link re-pairing that followed its own clearing
      action used a fresh SSP handshake, not a reused key (`CAP-013-FINDINGS.md` §2/§7) — another
      instance of `PROTOCOL.md` §5.1's already-FACT "fresh pairing" path, not a new finding in
      itself.
      **Second attempt, 2026-08-27 (`CAP-031-FINDINGS.md` §0), PROPOSAL — pending maintainer approval:** `CAP-031` retried the same repeat, this time with a genuine narrow per-device
      "Forget" (screenshot-confirmed, unlike `CAP-013`'s broader reset) and a live snoop-log
      file-size-polling check during recording specifically meant to avoid `CAP-013`'s failure —
      but the log's first frame still starts 66s *after* the Forget tap, and after the
      case-open/pair-button/first-scan-attempt sequence too. **Still 🔴 OPEN QUESTION, untested a
      third time** — a fourth attempt is needed, this time verifying the snoop log's own *content*
      freshness (last-frame timestamp against a live wall clock), not just its file size, before
      the Forget tap (`CAP-031-FINDINGS.md` §8). What `CAP-031` *did* confirm: a sixth instance of
      the fresh-SSP path (`CAP-031-FINDINGS.md` §2/§7), and two negative results against `CAP-013`'s
      own bonus findings — DLCI 0x02's ~61s-delayed open and the unattributed second BLE link both
      failed to reproduce this session (`CAP-031-FINDINGS.md` §5/§6), suggesting those were
      single-session artifacts rather than recurring behavior.
      **Third/fourth attempt, 2026-08-27 (`CAP-032-FINDINGS.md` §0), PROPOSAL — pending maintainer approval — succeeded.** Extracted via the raw BTSnoop file path instead of the `btsnooz.py`
      fallback `CAP-012`/`CAP-013`/`CAP-031` all used — the resulting log is genuinely untruncated
      and its first frame (18:29:45.72) lands ~58s *before* the on-screen Forget tap (18:30:42), and
      ~30s before the video itself starts, finally covering the pre-clearing-action window. Across
      that entire covered window: zero classic BR/EDR connection events of any kind, exactly one LE
      connection (resolving to an unrelated random-address device exposing a Heart Rate GATT
      service, not the Buds), and the `Delete Stored Link Key` command issued at the Forget tap's
      own moment reports `Num_Keys_Deleted = 0` (`CAP-032-FINDINGS.md` §0.3/§1, byte-level HCI
      evidence). **For this session specifically: no BLE link and no valid classic link key existed
      for the Buds anywhere before the Forget tap.** This is a clean counter-example to `CAP-001`'s
      original finding, not a reproduction or a refutation of it — `CAP-001`'s own session-specific
      puzzle (why *that* session had a BLE link and a valid key present before its clearing action)
      remains independently 🔴 OPEN, and this section's status is left as OPEN QUESTION rather than
      moved to "Resolved" below, pending maintainer review of whether a single clean session settles
      the general claim or only demonstrates it is non-universal. `CAP-032` also reconfirmed the
      fresh-SSP path a seventh time (`CAP-032-FINDINGS.md` §2/§7 Test B) and, as a genuinely new
      finding not previously documented, found a vendor-specific HCI command (`0xFD57`/`0x0157`,
      frame 91, 105ms into the log) whose payload embeds the Buds' address as part of an apparent
      bulk bonded-device-list provisioning at Bluetooth-stack bring-up — recorded 🔴 OPEN QUESTION on
      its own terms (unconfirmed vendor semantics), not bearing on this section's primary question
      (`CAP-032-FINDINGS.md` §5).

### Resolved

- [x] **UI-baseline firmware version** for the test device — `release_5.203`,
      confirmed via official app screenshot, 2026-07-30. This is what the
      app's About/settings screen displays — **not** the same thing as
      confirming what appears on the wire (see the "wire-baseline" item under
      Framing, added 2026-08-15).
- [x] **DLCI 0x08 Group `0x04` Code `0x12`'s alternating value — resolved 2026-08-18, 🟢 FACT,
      maintainer sign-off obtained 2026-08-28 (`DECISIONS.md` ADR-016):**
      neither purely reactive nor purely free-running — it fires in step with DLCI-0x08
      channel-(re)open events, **and** continues firing autonomously during otherwise-idle
      stretches after a gap with no channel churn. First characterized this way in
      `CAP-004-FINDINGS.md` §5a Task 5 (irregular-interval, near-perfect alternation) and
      `CAP-007-FINDINGS.md`(old) §3.2/§5 (fires with channel-(re)opens, and independently during
      idle periods); independently reconfirmed by a second, distinct session,
      `CAP-016-FINDINGS.md` §7 (8 pushes, cycling `0x02`/`0x03`, 2 in step with channel-(re)opens,
      4 during idle stretches with no churn) — same characterization holds across two independent
      captures, clearing `PROJECT_RULES.md` §1's promotion bar for the *behavior* (event-driven
      **and** autonomous). **What the value itself encodes remains 🔴 open** — not resolved by
      either capture.

## 7. Error handling / edge cases

| Scenario | Observed behavior | Status | Evidence |
|---|---|---|---|
| Malformed/unparseable frame (bad magic/length, checksum failure) | Dropped silently, surfaced internally as `BudsError.MalformedFrame`, never a crash | Design rule (not yet capture-verified) | `AGENTS.md` §6, `ARCHITECTURE.md` §5/§7 |
| Connection lost during write | `ConnectionState` moves to `DISCONNECTED`; in-flight polling coroutines cancelled | Design rule (not yet capture-verified) | `ARCHITECTURE.md` §6 |
| Buds out of range | Expected: `IOException` → `ConnectionLost`, per architecture | ⚪ ASSUMPTION | — |
| Case closed during connection | Terminates the active Bluetooth Classic connection — capture-verified 2026-08-18: the trigger is specifically **both buds being docked** (ACL `Disconnection Complete` fires the instant the second bud is placed in the case, reason `0x13`, Buds-initiated), not the lid closing itself — closing/reopening the lid alone, with no bud docked, is wire-silent (see row below) | 🟢 FACT (maintainer sign-off 2026-08-28, `DECISIONS.md` ADR-016) | `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §2, `CAP-016-FINDINGS.md` §1 |
| Case lid opened/closed while both buds remain outside the case | No wire-visible signal on any RFCOMM channel (`0x02`/`0x04`/`0x08`/`0x0a`) — whatever senses the lid position, if anything, does not report it to the phone while no bud is docked | 🟢 FACT — 2 independent captures (maintainer sign-off 2026-08-28, `DECISIONS.md` ADR-016) | `CAP-007-FINDINGS.md` §3.4, `CAP-016-FINDINGS.md` §5 |
| Inbound frame matching no known schema version | Returns `UnsupportedFirmware` rather than a best-effort parse | Design rule (not yet capture-verified) | `ARCHITECTURE.md` §8 |

## 8. Changelog of this specification

| Date | Change | Author (human/AI model) |
|---|---|---|
| 2026-08-07 | Initial formal specification promoted from `PROTOCOL_NOTES.md`; includes both RFCOMM framing hypotheses, battery mechanism options A–D, Find My Buds/Ring hypothesis, and consolidated open questions | Claude (AI), reviewed by maintainer |
| 2026-08-12 | Added §2.2a: DLCI 0x02's framing confirmed as Pigweed `pw_hdlc` (flag/escape/LEB128-address/control/CRC-32), matching `pbpctrl`'s own Maestro-transport notes; promoted to 🟢 FACT for the framing mechanism (640/640 sub-frames verified across 3 captures). Restructured §2.3's binary framing question into a three-channel table (DLCI 0x04/0x02/0x08). **§4.1 ANC mode promoted to 🟢 FACT**: Google's official "Hearable Controls" Fast Pair extension (Message Group `0x08`, Codes `0x11`/`0x12`/`0x13`) matches `CAP-001` byte-for-byte, including a 4/4 content+timing correlation against that capture's own recorded ANC taps — resolves the project's original highest-priority open command question, on the *official* Message Stream (DLCI 0x04), not `libmaestro`. Updated §6 Framing and Commands checklists accordingly. `libmaestro` (DLCI 0x02) and the private DLCI-0x08 envelope's command content, and EQ/other settings, remain unconfirmed — `FrameEncoder`/`FrameDecoder` implementation gate (`AGENTS.md` §6) remains closed pending a `DECISIONS.md` ADR | Claude (AI), deskresearch task, not yet reviewed by maintainer |
| 2026-08-17 | §4.3 Option D (BLE Battery Service `0x180F`) raised from 🔴 to 🟡 HYPOTHESIS: service *existence* confirmed via `CAP-017`'s (18:30) session's live GATT discovery (`CAP-017-FINDINGS.md` §3) — content/usage still unconfirmed, handle range still unresolved. Cross-check pass across all 9 capture sessions' documents; also updated `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `GATT-001` row to include this session (it previously only referenced the earlier, unsuccessful 11:42 `CAP-010` attempt) | Claude (AI), deskresearch task (HYPOTHESIS-level only — no FACT promotion, no sign-off needed) |
| 2026-08-17 | §6 Commands & schemas: DLCI 0x02 deskresearch pass across all captures with DLCI-0x02 traffic (`CAP-001`–`CAP-003`, `CAP-006`, `CAP-007`, 11:42 `CAP-010`). Answered the "is field-16/18 EQ-specific?" open item with a clean negative result (zero matches outside `CAP-005`, including `CAP-006`'s clean isolated ANC taps). Surfaced a new open item: two previously-undocumented HDLC addresses (`0x1e80`/`0x2680` Sent, `0xe980` Rcvd) recur at connection-reopen events in `CAP-005`/`CAP-007`, carrying the same already-documented serial+firmware content as the `0x0000`/`0xD180` pair — HYPOTHESIS that DLCI 0x02's Address field is per-connection-negotiated, not fixed. Full method in `DESKRESEARCH_FINDINGS.md` | Claude (AI), deskresearch task (HYPOTHESIS-level only — no FACT promotion, no sign-off needed) |
| 2026-08-18 | §4.2 EQ updated from a fresh, independent `CAP-015` session (`captures/CAP-015-2026-08-18_06-11-06_06-17-40-Group_T/CAP-015-FINDINGS.md`) that drags all 5 EQ sliders individually (3 passes each) and taps 5 presets, resolving the 2026-08-15 capture's field-to-band open question: **field-to-band mapping promoted to 🟢 FACT** (field 1↔Low bass, 2↔Bass, 3↔Mid, 4↔Treble, 5↔Upper treble, wire order reversed from on-screen order), matching the earlier single-band inference exactly. Also added: the ±6.0 band-gain clamp (🟢 FACT, units unconfirmed), a confirmed preset-quintet reference table, and a revised (still 🟡) reading of outer field 16/18 as preview/slider-release rather than preview/explicit-Save-tap. Updated the corresponding §6 open-question entry non-destructively | Claude (AI), capture-analysis task; retroactive maintainer sign-off obtained 2026-08-28, `DECISIONS.md` ADR-016 |
| 2026-08-18 | Synced with `CAP-016` (Group U re-run, `captures/CAP-016-2026-08-18_06-31-31_06-33-58-Group_U/CAP-016-FINDINGS.md`): §5.1 added the Buds-initiated reconnect-on-removal variant (🟢 FACT, frames 1213–1217); §7 added the case-lid-closed/re-docked disconnect row (🟢 FACT, `Disconnection Complete` reason `0x13` fires the instant the second bud is docked, not on lid-close alone) and the case-lid-open/close-while-buds-are-out zero-signal row (🟢 FACT, 2-capture-confirmed with `CAP-007`); §6 "Resolved" added the DLCI 0x08 Group `0x04` Code `0x12` behavior characterization (🟢 FACT for the event-driven-and-autonomous behavior, value's meaning still 🔴 open) and several new open items (RFCOMM channel-bounce trigger, ANC settable-toggles byte, the `0x0044` BLE notification burst, the `AndroidHeadTracker` HID Feature report) | Claude (AI), capture-analysis task; retroactive maintainer sign-off obtained 2026-08-28, `DECISIONS.md` ADR-016 |
| 2026-08-21 | Synced with 8 new captures (`CAP-011`, `CAP-019`–`CAP-025`): **§4.4 Find My Buds/Ring** — Left/Right confirmed 🟡 HYPOTHESIS (strong), video-correlated, proposed for 🟢 FACT pending maintainer sign-off (`CAP-025`); Case/"both" found to route through a separate, likely GMS-mediated Find Hub mechanism producing no local wire command — flagged as a possible Zero-GMS hard limit. **§4.5 rewritten** from a bare unmapped-feature bullet list into per-command subsections (§4.5.1–§4.5.8), each with a confirmed DLCI 0x02 opcode, following §4.1–§4.4's structure, plus a new shared preamble describing the general-purpose `field5{field4{...}}` settings-write envelope discovered this batch (9+ settings, 6 captures, no counter-example). **§4.3 Option A** — `CAP-011` attempted a passive BLE scan; result recorded as inconclusive (Fast Pair Service traffic present but not structurally matching the documented Battery Notification layout), not force-fit; procedure deviation (active connection present) flagged. **§0.1** — wire-baseline-vs-UI-baseline firmware version resolved (`CAP-023`): on-screen `release_5.203` matches DLCI 0x08's already-documented string, same session. §6 updated with ~10 new open items across Commands & schemas and Behavior, including a newly-raised Zero-GMS-relevant question about Find Hub's Case/"both" ring mechanism | Claude (AI), capture-analysis task, not yet reviewed by maintainer |
| 2026-08-23 | Remediation from an external audit pass (maintainer-approved fixes, see `CHANGELOG.md`'s 2026-08-22/23 entry for the full report summary): **§2.1/§4.4 corrected** — the cited "spec worked ACK example" for the Ring action did not match Google's actual Fast Pair acknowledgement spec (verified by direct fetch); corrected via non-destructive dated notes per `PROJECT_RULES.md` §3, and the "byte-for-byte match to spec" claim for one observed ACK variant retracted (neither observed variant actually matches the corrected spec example). **§6 reopened** the Ring ACK extra-byte open item against the corrected spec tail, and added a refined characterization (timing/direction/entropy profile) of `CAP-021`'s still-unexplained DLCI 0x0a burst. **§4.3 Option C** annotated to explain DLCI 0x08 vs. 0x09 both being called "channel 4" (same RFCOMM multiplexer session, disambiguated by direction bit — not a numbering error). **§4.3 Option D** added the Battery Level characteristic UUID (`0x2A19`) alongside the already-documented service UUID (`0x180F`) | Claude (AI), audit-remediation task, maintainer-directed |
| 2026-08-23 | **Three pending FACT promotions reviewed and explicitly approved by the maintainer** (`AGENTS.md` §6), each recorded with its own `DECISIONS.md` ADR: **§4.4 Find My Buds Left/Right** promoted to 🟢 FACT (`ADR-011`) — Case/"both" remains a separate, unresolved mechanism, not covered. **§0.1 wire-baseline firmware version** (`"release_5.203"` on DLCI 0x08) promoted to 🟢 FACT (`ADR-012`) — `"Revision 6"`'s meaning remains open, not covered. **§4.5's shared preamble, general-purpose DLCI 0x02 settings-write envelope shape** promoted to 🟢 FACT (`ADR-013`) — narrower than it may look: only the outer `field5{field4{...}}}` wrapper's existence/shape is FACT; every individual setting's specific field-number mapping in §4.5.1–§4.5.8 remains its own, separately-labeled 🟡 HYPOTHESIS, per the maintainer's explicit decision not to blanket-promote | Claude (AI), maintainer-directed sign-off session |
| 2026-08-23 | **§4.3 Option E added** — re-analysis of `CAP-011` (prompted by the maintainer spotting a 1% battery drop in the recording) pinpointed the exact UI-change timestamp (09:52:25.8, correcting an initial ~09:45:47 estimate) and found a DLCI 0x08 message (`Group 0x0e Code 0x01`) whose entries track on-screen battery values. **Cross-capture check same day found a clean 3-for-3 match (Left/Right/Case) in 2 further independent sessions (`CAP-001`, `CAP-002`, both 2026-08-09)** — upgrading this from a single-session (`CAP-011`, 4 internal recurrences) finding to a 3-session, 12-day-spanning one; `CAP-011`'s Case entry specifically reads stale/non-matching, flagged as its own open item, not treated as contradicting the mapping. 🟡 HYPOTHESIS (strong), proposed for FACT pending maintainer sign-off — not yet reviewed. Refines an already-known-but-undecoded message shape from `CAP-002-FINDINGS.md` §2a (2026-08-12), not a newly-found packet type. §6's item on the message's 3rd entry resolved (index=3=Case); a new item added for `CAP-011`'s specific staleness anomaly; the burst's irregular, BLE-churn-uncorrelated trigger interval remains open; one pre-existing item partially advanced (`Group 0x0e`, previously outside its listed group set) | Claude (AI), maintainer-requested capture re-analysis |
| 2026-08-2x | **`CAP-009` (`BATT-006`), independently re-analyzed, then 5 findings reviewed and explicitly approved by the maintainer** (`AGENTS.md` §6): **§4.3 Option C** — `battchg` confirmed 🟢 FACT a stale single snapshot; `AT+BIEV` confirmed 🟢 FACT per-earbud (Right, this session) rather than a fixed aggregate, revising the project's earlier aggregate assumption; push cadence corrected from "fixed ~6–7s" to "settling burst, then irregular" (also updates `AGENTS.md` §5's implementation guidance) — all recorded in `ADR-015`; `BATT-006` closed. **§4.3 Option E** — two addenda added at 🟡 HYPOTHESIS (a live charge-cycle observation; the Case field's two distinct "unknown"-placeholder wire encodings) as part of the "fully purpose-built confirmation" Option E's own entry had called for. **§4.3 Option B** — DLCI `0x04`'s `Group 0x03 Code 0x03` added as a 🟡 HYPOTHESIS candidate for the still-unconfirmed battery code (208 occurrences, Left/Right in near-lockstep with `AT+BIEV`/Option E outside the charging period). **§4.3 Option A** — a BLE Fast Pair scan added as a 🟡 HYPOTHESIS timing correlation for on-screen updates after HFP/Option E both close post-reconnect; device attribution not yet confirmed. See `CAP-009-FINDINGS.md` and `CAP-009-EVENT-NOTES.md` for the full independent re-analysis (its own video timeline, MAC re-derivation, filter-sanity/DLCI-inventory checks) behind all of the above | Claude (AI), maintainer-directed sign-off session |
| 2026-08-27 | **PROPOSAL, pending maintainer approval.** `CAP-014` (Group W repeat, snaplen-fixed) analyzed: **§4.3 Option D and the `0x0c0X`/`0x0f2X` open item annotated, no status change** — the handle↔UUID mapping remains 🔴 OPEN after a 3rd Group-W-labeled attempt, but the blocking cause is now precisely identified as GATT-cache reuse on an already-bonded phone (not a snaplen issue this time, which this session's own check confirmed fixed) — see `CAP-014-FINDINGS.md` §4/§8 for the full analysis and the recommended next capture (genuinely combining a fixed snaplen with one of Group W's own untried cache-busting methods, `pm clear com.android.bluetooth` or the Pixel 9a). Byte-length/leading-byte shapes for `0x0c0c`/`0x0c13`/`0x0c14` and content for `0x0f2a` ("Revision 6")/`0x0f32` (`0x64`) reproduce exactly across independent sessions, strengthening confidence without changing any status | Claude (AI), capture-analysis task, not yet reviewed by maintainer |
| 2026-08-30 | **Four pending FACT promotions from a combined Tier 0 (capture re-decode) / Tier 2 (APK static-analysis) session reviewed and explicitly approved by the maintainer, per-point** (`AGENTS.md` §6), recorded in `DECISIONS.md` ADR-019: **§2.2a** — the "..." inside DLCI 0x02's `field5{field4{...}}` wrapper confirmed 🟢 FACT to be `libmaestro`'s own recovered `WriteSetting` schema (`qhr`), for 2 sampled fields (4, 29), via independent APK static analysis. **§4.5.3** — the top-level "Use touch controls" toggle opcode (`field 4`) and the press-and-hold action-selection opcode (`field 7`/`qju`, plus a corrected, one-level-deeper `qik`→`qho` nesting) both promoted to 🟢 FACT, each now backed by both wire+video correlation and a self-describing log message in the app's own code. The ANC-mode rotation-checklist opcode's **field number** (`field 12`/`qht`) promoted to 🟢 FACT; its equivalence to the app's own "ANC gesture loop" name explicitly **not** promoted — the maintainer reviewed this specific point and kept it at 🟡 HYPOTHESIS. See `REVERSE_ENGINEERING.md`'s `qjc`/`qja`/`qhr`/`qjo`/`qju`/`qjg`/`qht` entries (2026-08-30 updates) and `CAP-020-FINDINGS.md`/`CAP-021-FINDINGS.md`'s 2026-08-30 addenda for the full byte-level and code-level evidence | Claude (AI), maintainer-directed per-point sign-off session |
| 2026-08-30 | Remediation from a 2026-08-30 project-wide documentation audit (maintainer-directed fixes, no new FACT promotion or ADR): **§2.2a** — added `CAP-033` as a fourth independent, SDP-service-name-level corroboration of DLCI 0x02's "MAESTRO APP" channel-ownership finding. **§2.3** — added a 2026-08-30 update recording `CAP-033`'s SDP-browse naming of DLCI 0x08 ("GSND CONTROL"), DLCI 0x0a ("GSND AUDIO"), DLCI 0x06 ("DEBUG APP"), and DLCI 0x12 ("BTIS") — new leads, 🟡 HYPOTHESIS, explicitly not a resolution of DLCI 0x08's identity. **§6** — added a matching dated update to the DLCI-0x08-ownership open item | Claude (AI), audit-remediation task, maintainer-directed |
| 2026-09-03 | **§4.2 EQ** — `FrameEncoder`/`FrameDecoder` implementation explicitly unblocked (`DECISIONS.md` ADR-020, maintainer-directed, closing a gap a 2026-09-02 documentation audit found: EQ's protocol knowledge was already fully FACT per `ADR-016`, but no ADR had ever explicitly cleared `ARCHITECTURE.md` §5's implementation gate for it, unlike ANC/`ADR-009` and Find My Buds/`ADR-011`). No new protocol knowledge; field-16-vs-18 and gain-unit questions remain open | Claude (AI), maintainer-directed sign-off session |
| 2026-09-03 | Remediation from a 2026-09-02 documentation audit (mechanical fixes, no new FACT promotion or ADR beyond ADR-020 above): **§4.3 Option A** — the "shown ≥8s, auto-hidden after 20s" Battery Notification visibility-timing claim downgraded from unqualified `[OFFICIAL-SPEC]` to 🟡 HYPOTHESIS after two direct re-fetches of the official `batterynotification` extension page found no matching text; the byte-layout table in the same section was re-confirmed exactly and is unaffected | Claude (AI), audit-remediation task, maintainer-directed |

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/PROTOCOL.md - https://tedsluis.github.io/opencontrolpixelbudspro2/PROTOCOL
