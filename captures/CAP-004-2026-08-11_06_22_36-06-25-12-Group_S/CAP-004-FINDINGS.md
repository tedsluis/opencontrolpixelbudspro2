# Findings: `CAP-004` (Group S — GMS disabled / no Pixel Buds app)

Standardized, evidence-based extraction from `CAP-004-btsnoop_hci.log` + `CAP-004-recording.mp4`, staged here
for later promotion into `PROTOCOL_NOTES.md` / `PROTOCOL.md` per `PROJECT_RULES.md` §2. Modeled
on `captures/CAP-001-2026-08-09_08-51-00_08-52-20-Group_Z/CAP-001-FINDINGS.md` (`CAP-001`). Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-004` · **Date:** 2026-08-11 · **Phone:** Pixel 7a, **Google Play Services
disabled, Pixel Buds app uninstalled**; connected via nRF Connect then paired via system
Bluetooth settings — no Pixel-Buds-specific app involved at any point. **Log file:**
`CAP-004-btsnoop_hci.log` (342.3s, 2,921 packets, 06:22:04.23–06:27:46.48 local/+0200). **Video:**
`CAP-004-recording.mp4` (155.4s, 06:22:36–06:25:12 local, on-screen wall-clock overlay). **Devices:**
phone `Google_7e:ca:81` (Pixel 7a, same phone as `CAP-001`–`CAP-003`), peer `Google_cf:6e:07`
(`04:00:6E:CF:6E:07`, the Buds/case — confirmed same physical device via frame 1891's BD_ADDR).

**Stated goal of this session:** determine whether the Fast Pair Message Stream traffic
identified in `CAP-002`'s `CAP-002-FINDINGS.md` §3 (the `[Group][Code][Length:2B][Value]`-framed
channel/DLCI carrying, among other fields, Group `0x03` Code `0x09` = `"Revision 6"`, Code
`0x01` = `da 2d b1`, and Group `0x07` Code `0x41` = `"in-use"`) is **Buds-initiated** (would
reappear identically) or **GMS/Nearby-driven** (would disappear or change) once Google Play
Services is disabled and no Pixel Buds app is present. **§4 gives the answer, with a nuance the
question didn't anticipate — see there.** A full pairing exchange was also expected as a side
effect and is documented in §2, per this session's own procedure step 3.

---

## 1. Log contains unrelated background traffic — Fitbit Charge 6 excluded (🟢 FACT)

Before analyzing anything else: the log's very first GATT activity (frames 216–327+, starting
06:22:05.17, including a full `Read By Group Type` primary-service discovery resolving
`GAP`/`GATT`/`Device Information`/**`Heart Rate`**) does **not** belong to the Buds. Checked
directly: frame 229's connection context shows `Source BD_ADDR: 62:6f:64:e2:1b:4a`, `Source
Device Name: Charge 6` — the maintainer's Fitbit, reconnecting in the background on the same
phone, same as the unrelated traffic already flagged and excluded in `CAP-002`'s `CAP-002-FINDINGS.md`
§7. This is recorded here so it isn't mistaken for Buds discovery traffic by a future reader —
all findings below are scoped to traffic confirmed against `Google_cf:6e:07`.

## 2. Classic pairing via Cross-Transport Key Derivation, not classic SSP (🟢 FACT — new mechanism, not seen in `CAP-002`/`CAP-003`)

Both earlier fresh-pairing captures (`CAP-002`, `CAP-003`) showed classic Secure Simple Pairing:
an `IO Capability Request/Response` exchange followed by `Simple Pairing Complete`. **This
session shows neither.** Instead:

| Step | Time | Frame(s) | Detail |
|---|---|---|---|
| `Delete Stored Link Key` | 06:24:24.668 | 1854–1855 | Confirms a deliberate fresh-bond flow, same intent as `CAP-002`/`CAP-003` |
| SMP `Pairing Request` (LE) | 06:24:24.672 | 1856 | `AuthReq: Bonding, MITM, SecureConnection`; **key distribution includes `Linkkey`** for both initiator and responder — this is the explicit request for **Cross-Transport Key Derivation (CTKD)** |
| SMP `Pairing Response`, Public Key exchange, `Pairing Confirm`/`Random` | 06:24:24.735–24.974 | 1859–1879 | LE Secure Connections (ECDH-based) pairing proceeds — not legacy SMP pairing |
| SMP `DHKey Check` (sent/received) | 06:24:29.606–29.684 | 1880, 1882 | Completes the LE Secure Connections pairing |
| `Identity Information`/`Identity Address Information` exchanged | 06:24:29.864–29.866 | 1886–1890 | Standard LE bonding key distribution |
| Classic `Create Connection` | 06:24:29.878 | 1891 | Sent immediately after the LE pairing completes |
| Classic `Connect Complete` | 06:24:30.380 (status `0x00`) | 1933 | |
| `Authentication Requested` → `Link Key Request` → **`Link Key Request Reply`** (not Negative) | 06:24:30.422–30.431 | 1966–1977 | The classic link key is **already available** — derived from the LE pairing above via CTKD, not obtained through a separate classic SSP exchange |
| `Authentication Complete` → `Encryption Change` | 06:24:30.442–30.795 | 1982, 2037 | |

**Why this differs from `CAP-002`/`CAP-003`:** in both earlier captures, classic pairing was the
*first* thing to happen, with no pre-existing BLE link. Here, nRF Connect had already established
a BLE connection to the Buds at 06:23:01 — over a minute before the classic pairing sequence
began — giving Android's stack an existing LE Secure Connections context to derive the classic
key from via CTKD instead of negotiating classic SSP from scratch. This is a genuinely new,
useful data point for `PROTOCOL.md` §5's connection-lifecycle section: **the classic bonding
mechanism used depends on whether an LE Secure Connections link already exists**, not just on
whether a classic bond exists.

**Answering this session's own procedural question** (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group S
step 3, "note whether the device was already unpaired or a fresh bond"): this **was** a fresh
bond (link key deleted, new key derived), but via CTKD from a fresh LE pairing rather than via
the classic-SSP mechanism seen before.

## 3. RFCOMM channel topology this session (🟢 FACT)

Channels opened: **0** (multiplexer control, frame 2052), **6** (labeled **"Hands-Free"** by
Wireshark, frame 2068 — HFP AT-command handshake confirmed present, 53 HFP-tagged packets),
**4** (frame 2178), **5** (frame 2225). **Channels 1 and 2 are never opened at all this
session** — a first among the four captures to date, and directly relevant to §4 below, since
`CAP-002`'s Fast Pair Message Stream Device Information content lived specifically on channel
2/DLCI 0x04.

## 4. Answering the core question: present-but-partial, not a clean present/absent (🟡 mixed outcome — see the two parts below)

The exact byte strings from `CAP-002` §3 were searched for directly across every RFCOMM data
frame in this capture. Result is genuinely mixed between two distinct sub-mechanisms that
`CAP-002` had described together as one channel's content — this capture shows they are
**not** the same thing, and behave differently under GMS-disabled conditions:

### 4a. The `[Group][Code][Length:2B][Value]` TLV content specific to `CAP-002` §3 — **ABSENT** (outcome 2: supports GMS/Nearby-driven)

A direct byte-string search for `"Revision 6"` (`5265766973696f6e2036`), the Model ID value
`da 2d b1`, and `"in-use"` (`696e2d757365`) across every RFCOMM data frame in this capture
returns **zero matches**. Consistent with that: **channel 2/DLCI 0x04 — the specific channel
`CAP-002` found this content on — is never opened in this session at all** (§3). This is a
clean, unambiguous absence for this specific sub-mechanism: the Model-ID/Firmware-version/
BLE-address-updated TLV exchange that `CAP-002` documented does not occur when GMS is disabled
and no Pixel Buds app is installed. **This supports the GMS/Nearby-driven hypothesis** for that
specific content — not proof (a negative result from one capture can't rule out a triggering
condition this session didn't happen to hit), but a real, checked absence.

### 4b. A related-but-distinct protobuf-shaped blob (channel 4/DLCI 0x08) — **PRESENT, essentially unchanged** (outcome 1: supports Buds-initiated)

`CAP-001`'s `CAP-001-FINDINGS.md` §2 separately documented a **different** piece of content — on
channel 4/DLCI 0x08, *not* channel 2 — containing the ASCII strings `google-pixel-buds-pro-v1`
and `Europe/Amsterdam`, protobuf-tag-framed (not the Group/Code/Length TLV shape). **This
content reappears in this capture, on the same channel-4/DLCI-0x08 pattern, essentially
unchanged:**

- `google-pixel-buds-pro-v1` + a capability-negotiation blob containing ASCII `all` — frames
  2311/2315 (06:24:31.104–31.105), byte-identical in shape to the equivalent frames in
  `CAP-001`/`CAP-002`/`CAP-003`.
- `Europe/Amsterdam`, protobuf tag+length-framed exactly as in `CAP-001` (`1a 10` + 16-byte
  string) — frame 2274 (06:24:31.076), value `08 9f 03 10 c3 b8 c2 f8 0e 1a 10` + `"Europe/
  Amsterdam"` — the leading random-looking bytes differ from `CAP-001`'s occurrence (expected,
  per `CAP-001`'s own note that this field appears session-specific/nonce-like), but the
  string, tag byte, and length byte are identical.

**This supports the Buds-initiated hypothesis** for *this* content specifically — it appears
unchanged with GMS disabled and no app installed, meaning the Buds (or the phone's own Bluetooth
stack, independent of GMS) send this regardless.

### Conclusion: the honest answer is "it depends on which sub-mechanism"

`CAP-002` §3 treated the channel-2 TLV content and general "device info exchange" as one
finding. This capture shows that framing was too coarse: there are **at least two distinct
device-info-flavored exchanges** happening over RFCOMM at connection time, on different
channels, with different framing, and — now shown here — **different dependencies on GMS**.
Recommend `CAP-002`'s `CAP-002-FINDINGS.md` §3 be given a similar non-destructive correction note (per
the pattern already used there for the channel-1/AVRCP correction) clarifying that its content
is GMS-dependent, distinct from the channel-4/DLCI-0x08 content documented in `CAP-001`, which
is not.

## 5. Spec research on Groups `0x04`/`0x05`/`0x09`, and a re-decode of frame 2305 (updated 2026-08-11)

**Method correction first:** the byte table in the original version of this section was read by
treating each individual RFCOMM data frame as one complete `[Group][Code][Length:2B][Value]`
message. Re-parsing precisely (Python, exact byte offsets) shows this was wrong for at least two
of the frames — **a single logical message can be split across consecutive RFCOMM packets**,
header in one frame, value in the next (mirroring how `CAP-002`'s single 47-byte RFCOMM frame
packed several complete messages together — the reverse fragmentation pattern). Frames 2272
(`03 01 00 1b`, header only, declared value length 27) and 2274 (27 bytes) are one message, not
two; frame 2305 is a single, complete 67-byte frame. The corrected per-frame decode:

| Frame(s) | Group | Code | Declared len | Value |
|---|---|---|---|---|
| 2246 | `0x05` | `0x0c` | 0 | — |
| 2256, 2258, 2259, 2261, 2263 | `0x04` | `0x02`, `0x04`, `0x11`, `0x13`, `0x15` | 0 each | — |
| 2264 | `0x05` | `0x0a` | 13 | `0a 07 "713f855" 10 40 18 00` (protobuf-shaped) |
| 2272+2274 (one message) | `0x03` | `0x01` | 27 | `08 9f 03 10 <4 bytes> 0e 1a 10 "Europe/Amsterdam"` |
| 2275 | `0x04` | `0x03` | 4 | `00 10 05 18 64` (truncated in source dump — see raw hex) |
| 2277 | `0x09` | `0x03` | 0 | — |
| 2280 | `0x04` | `0x05` | 2 | `08 03` |
| 2289 | `0x04` | `0x12` | 4 | `08 02 10 01` |
| 2292 | `0x04` | `0x14` | 2 | `08 01` |
| 2295 | `0x04` | `0x16` | 2 | `08 02` |
| **2305** | **`0x03`** | **`0x02`** | **63** | protobuf-shaped, contains `"release_5.203"` and `"713f855"` — see below |
| 2310 | `0x09` | `0x02` | 2 | `08 00` |

**Group `0x04` identified — 🟢 FACT.** A targeted search (not a generic query — searched directly
for "Fast Pair Message Stream Action event group 0x04") found Google's own
[Device action](https://developers.google.com/nearby/fast-pair/specifications/extensions/deviceaction)
spec page, which states explicitly: *"Device action event"* = `0x04`, and defines code `0x01` as
**Ring** (already known to this project via `PROTOCOL.md` §4.4's Ring/Find-My-Buds hypothesis).
The page's visible content only documents code `0x01` in detail; codes `0x02`, `0x03`, `0x04`,
`0x05`, `0x11`, `0x12`, `0x13`, `0x14`, `0x15`, `0x16` observed here are **not** covered by the
fetched excerpt — group identity confirmed, individual code meanings within it remain 🔴 open.

**Bonus, directly relevant to `CAP-002` §3's separate open item:** the same search pass also
found Google's [SASS (Smart Audio Source Switching)](https://developers.google.com/nearby/fast-pair/specifications/extensions/sass)
extension page, which states it uses **Message Group `0x07`** and lists a full code table
including **`0x41` = "Indicate in use account key."** This is an exact match to `CAP-002`
`CAP-002-FINDINGS.md` §3's Group `0x07` Code `0x41` `"in-use"` finding — previously 🟡 on the strength of
an *English-phrase* match against a different (GATT-level) spec page. This SASS page gives the
*same transport* (Message Stream) and the *exact* group/code pair, which is materially stronger
evidence. Recommend `CAP-002` §3 be updated to 🟢 FACT for that specific bullet (not done in this
file — out of this session's scope, flagged for a maintainer/future pass, consistent with how
`CAP-002` §7 item 4 already deferred a similar cross-file update).

**Groups `0x05` and `0x09` — still 🔴 genuinely unidentified.** Unlike Group `0x04`, no spec page
was found defining a standalone "Group `0x05`" or "Group `0x09`". Search results for these codes
kept surfacing **Device Information (Group `0x03`) codes `0x05`/`0x06`** ("Active components
request/response") and **code `0x09`** ("Firmware version") instead — i.e. `0x05` and `0x09` are
documented as *codes within Group `0x03`*, not as separate top-level groups. This raises a real
possibility not resolved in this pass: frames 2246/2264 (`Group 0x05`) and 2277/2310
(`Group 0x09`) might not be freestanding messages with an undocumented group byte, but
**fragments of a still-larger multi-message frame this analysis hasn't fully reassembled** (per
the fragmentation behavior just discovered above) — or they are genuinely undocumented groups.
Left as 🔴 OPEN QUESTION rather than guessed at.

**Frame 2305 decoded — a `Group 0x03`/`Code 0x02` message containing the project's real,
confirmed firmware string, precisely re-parsed (not the "12-character string" originally
guessed — corrected below):**

```
Group 0x03, Code 0x02, Length 63
Value (protobuf-shaped):
  08 06                      → field 1 (varint) = 6
  10 01                      → field 2 (varint) = 1
  22 0d "release_5.203"      → field 4 (string, len 13) = "release_5.203"
  2a 00                      → field 5 (string, len 0) = ""
  30 e6 01                   → field 6 (varint) = 230
  38 00                      → field 7 (varint) = 0
  4a 07 "713f855"            → field 9 (string, len 7) = "713f855"   [7 chars, not 12]
  50 00                      → field 10 (varint) = 0
  60 b1 db e8 06             → field 12 (varint, multi-byte)
  70 02                      → field 14 (varint) = 2
  78 01                      → field 15 (varint) = 1
  ... (remaining bytes: further small varint fields)
```

**This confirms `"release_5.203"` — `PROTOCOL.md` §0.1's actual, already-verified firmware
baseline — genuinely appears on the wire**, inside this protobuf-shaped value. This is a
different string from `CAP-002`'s `"Revision 6"` (found under the *documented* Group `0x03` Code
`0x09` = Firmware version field). **New open question raised, not resolved, by this precise
decode:** this value sits under Code `0x02`, which `CAP-002` §3 spec-confirmed as **"BLE address
updated"** — a documented 6-byte MAC address field, categorically incompatible with this 63-byte
protobuf structure. Either Code `0x02` is being reused here for something unrelated to
`CAP-002`'s clean 6-byte-MAC match there, or — more likely, given the fragmentation bug already
found in this same burst — **this frame's leading `03 02 00 3f` is not actually a Message Stream
header at all**, and is coincidentally similar-looking header bytes belonging to a different,
not-yet-identified structure. **Not resolved in this pass — flagged explicitly rather than
force-fit into either the "BLE address" or "firmware version" slot.** This means `CAP-002` §3's
open question ("does `'Revision 6'` or something else represent the real firmware version?")
remains open: `"release_5.203"` is now a documented, on-the-wire-confirmed candidate, but the
framing that would explain *which* field it officially belongs to is not yet established.

## 5a. 2026-08-12 follow-up: fragmentation re-check, frame 2305 resolved, groups 0x04/0x05/0x09 not actually new to this capture

Deskresearch pass (Python + `tshark -T fields`/`data.data`, no Wireshark UI) re-analyzing this
capture's `CAP-002-btsnoop_hci.log` plus `CAP-003-btsnoop_hci.log`/`CAP-004-btsnoop_hci.log` for cross-validation.
Scripts used are reproduced at the end of this addendum.

**Reassembly note first, since it changes how everything below must be read:** a single-frame
`parse_tlvs()` pass under-counts fragmentation. A stream-level reassembler (buffers bytes per
DLCI in time order, parses `[Group][Code][Len:2B-BE][Value]` messages as soon as enough bytes are
available, and attributes each message to every frame whose bytes it spans) finds **three**
cross-frame splits in this session's DLCI 0x08 burst, not the one already documented in §5 above:
frames **[2272, 2274]** (Group `0x03` Code `0x01`, already known), plus **[2360, 2361]** and
**[2365, 2366]** (both Group `0x02` Code `0x05`, 2-byte value each) — none of these two new ones
were caught by the original per-frame reading. All three involve a 4-byte header arriving in one
RFCOMM write and the value in the very next one, ~1 ms later; in every case `bthci_acl.pb_flag`
shows no `continuation_to`/`reassembled_in` on either frame — i.e. **not** L2CAP/ACL-level
segmentation, but a message genuinely split across two complete, independent RFCOMM I-frames on
the same DLCI. Confirms and extends this file's own §5 finding: any parser for this channel must
reassemble across RFCOMM frame boundaries, and this is not a one-off.

**Task 1 (also covering `CAP-002` §3 and this file's §4b) — byte-offset/fragmentation check, both
directions answered explicitly:**

- `CAP-002` §3's TLV content (Model ID `da 2d b1`, BLE-address-updated, `"Revision 6"`) — **NOT
  fragmented, in either of the two occurrences found in this session's own log lineage.** Searching
  `CAP-002`'s underlying `CAP-002-btsnoop_hci.log` (which — per that file's own header note — is the same
  shared, non-restarted buffer used since `CAP-001`, 50,468 packets over ~8h20m, not just the
  documented ~150s slice) for the Model ID byte string `da:2d:b1` returns **seven** occurrences
  across the whole day (frames 1004, 1826, 17610, 18895, 21195, 49251, 49538 in that file's own
  numbering — frame 49251 is the one `CAP-002` §3 calls "frame 1267" under the sliced-file
  numbering it used for analysis). Every single occurrence is a single, complete, 47-byte RFCOMM
  UIH frame carrying all five TLV messages in the burst together (`03 0a 00 08 <8B>` + `03 01 00 03
  <3B>` + `03 02 00 06 <6B>` + `03 09 00 0a "Revision 6"` + `07 10 00 00` = 12+7+10+14+4 = 47,
  matching `btrfcomm.len` exactly). `bthci_acl.pb_flag` for frame 49251's underlying ACL packet is
  `2` (complete PDU) with no `continuation_to`/`reassembled_in` — confirmed via
  `tshark -r CAP-004-btsnoop_hci.log -Y "frame.number==49251" -e bthci_acl.pb_flag -e bthci_acl.continuation_to -e bthci_acl.reassembled_in`.
  **Negative result, itself a finding per the project's evidence rules:** this specific burst does
  not fragment across packets in any of the 7 independent occurrences checked.
- This file's own §4b content (`google-pixel-buds-pro-v1`, `Europe/Amsterdam`, on channel
  4/DLCI 0x08) — **mixed.** The `google-pixel-buds-pro-v1` + capability-negotiation (`"all"`)
  messages are never fragmented in any capture checked (`CAP-001`, `CAP-002`, `CAP-003`, `CAP-004`
  all carry them as single complete frames). The `Europe/Amsterdam` message specifically **is**
  fragmented in this capture (frames 2272+2274, per §5 above) but is **not** fragmented in either
  of its two occurrences in `CAP-001`'s own log (frame 1113: single 31-byte frame containing header
  `03 01 00 1b` + the full 27-byte value together; frame 1673: single 35-byte frame containing an
  empty `09 03 00 00` message immediately followed by the same header+value, again packed whole) —
  confirmed via `tshark -r CAP-004-btsnoop_hci.log -Y 'data.data contains "Europe/Amsterdam"' -e bthci_acl.pb_flag -e bthci_acl.continuation_to`
  against `CAP-001`'s log, no continuation flags set on either frame. **So this exact
  logical message is demonstrably NOT always packet-sized in a fixed way — it is sometimes sent as
  one write (`CAP-001`, twice) and sometimes split header/value across two writes (`CAP-004`,
  once)** — real, non-deterministic fragmentation behavior a `FrameDecoder` must handle, not an
  artifact of one capture's read.

**Task 2 — frame 2305 is a genuine TLV header, not coincidental protobuf bytes, but it belongs to
a *different, private* Group/Code namespace than `CAP-002` §3's:**

The hypothesis in this file's §5 ("frame 2305's `03 02 00 3f` might not be a Message Stream header
at all") does not survive a precise check, for two independent reasons:

1. **A pure-protobuf reading of byte 0 fails.** `0x03 = 0b00000011` as a protobuf tag decodes to
   field number `0` (`0x03 >> 3`), wire type 3 (deprecated `START_GROUP`) — field number 0 is
   illegal in protobuf (field numbers start at 1), so frame 2305 cannot be read as a raw protobuf
   stream starting at byte 0. The TLV reading (`Group=0x03, Code=0x02, Len=0x3f=63`) is the only
   one under which the frame parses cleanly at all, and `4 + 63 = 67` matches the frame's actual
   length exactly.
2. **The exact same 67-byte frame — header and all 63 value bytes, byte-for-byte — reappears in
   `CAP-002`'s log**, at frame 49028 (17:05:34.599755, well *before* frame 49251's Group-0x03-TLV
   burst above, and on the *same* DLCI 0x08 that carries `CAP-001`'s `google-pixel-buds-pro-v1`
   content, not `CAP-002`'s own DLCI-0x04 channel):
   ```
   CAP-004 frame 2305: 0302003f08061001220d72656c656173655f352e3230332a0030e60138004a0737313366383535500060b1dbe80670027801a80101b00101ba01020102c00101c80101
   CAP-002 frame 49028: 0302003f08061001220d72656c656173655f352e3230332a0030e60138004a0737313366383535500060b1dbe80670027801a80101b00101ba01020102c00101c80101
   ```
   Identical, across two independent capture sessions taken two days apart (2026-08-09 vs.
   2026-08-11), one with GMS enabled and one with GMS disabled. A coincidental protobuf-byte
   collision cannot reproduce this; a real, stable envelope can. Per this project's own promotion
   rule (structural match across ≥2 independent captures ⇒ 🟢 FACT), **frame 2305's leading 4 bytes
   are a genuine `[Group][Code][Len]` header — 🟢 FACT** — and its value decodes as a well-formed
   protobuf message: `field1=6, field2=1, field4="release_5.203", field6=230, field9="713f855",
   field12=14298545, field14=2, field15=1, field21=1, field22=1, field23=[01 02], field24=1,
   field25=1` (fields 5/7/10 empty/zero).

   **What this means for the open question it raised:** Group `0x03` Code `0x02` on **DLCI 0x08**
   is *not* the same message as Group `0x03` Code `0x02` on **DLCI 0x04** (`CAP-002` §3's
   spec-verified "BLE address updated", a fixed 6-byte MAC). DLCI 0x08 runs its **own private
   Group/Code/Length envelope** — structurally identical in shape to the official Fast Pair
   Message Stream (matching `PROTOCOL.md` §2.1 Hypothesis A's general form), reusing the same
   numeric Group/Code labels as the GMS-driven official channel purely coincidentally (or by lazy
   convention — e.g. both start numbering their "device info" group at `0x03`) — not because
   they're the same logical message. This is now well evidenced (below) as a *whole private
   protocol on DLCI 0x08*, not a one-off anomaly in frame 2305 specifically. `CAP-002` §3's
   original open question ("is `'Revision 6'` the real firmware version, or is `'release_5.203'`
   from this frame?") is now understood to be comparing two unrelated protocols' fields, not two
   candidate readings of the same field — both strings are real, on-the-wire values, from two
   different channels/mechanisms.

**Task 9/10 (also resolves `CAP-002` CAP-002-FINDINGS.md §2's own 🔴 "not decoded in this pass" for its
channel-4/DLCI-0x08 burst) — full decode of the DLCI 0x08 private envelope, and correction of the
"new in `CAP-004`" framing:**

`CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Capture Index describes Message Stream groups `0x04`, `0x05`,
`0x09` as newly discovered *in `CAP-004`*. A full-session, reassembling decode of DLCI 0x08 in
**all three** captures that have this channel (`CAP-001`, `CAP-002`, `CAP-004` — `CAP-003` also has
it, 87 messages) shows this framing is **not new to `CAP-004` at all** — it is present,
byte-for-byte structurally identical, from the very first capture (`CAP-001`, 2026-08-09 08:51) —
simply never decoded before because both `CAP-001` §2 and `CAP-002` §2 explicitly deferred this
channel as "not decoded in this pass." Corrected finding: **groups `0x01`, `0x02`, `0x03`, `0x04`,
`0x05`, `0x09`, `0x0e` all appear on DLCI 0x08 in `CAP-001`, `CAP-002`, and `CAP-004` alike** (`0x01`
and `0x02` additionally only surface in `CAP-002`'s richer, genuinely-fresh-pairing session — see
below).

Unique Code/Value shapes for groups `0x04`/`0x05`/`0x09` (this file's `CAP-004-btsnoop_hci.log`, reassembled):

| Group | Code | Len | Value | Notes |
|---|---|---|---|---|
| `0x04` | `0x02`,`0x04`,`0x11`,`0x13`,`0x15` | 0 | — | empty "ping"-shaped messages |
| `0x04` | `0x03` | 4 | `10 05 18 64` (pb: field2=5, field3=100) | |
| `0x04` | `0x05` | 2 | `08 03` (pb: field1=3) | also seen as `08 04`/`08 05`/`08 06` in `CAP-001`/`CAP-002` |
| `0x04` | `0x12` | 4 | `08 02 10 01` / `08 03 10 01` (pb: field1∈{2,3}, field2=1) | **recurs every few seconds for the rest of the session** (frames 2352, 2443, 2454, 2468, 2483, 2533, ... alternating field1 2↔3) — looks like a periodic 2-state heartbeat/status ping, not a one-time handshake value |
| `0x04` | `0x14` | 2 | `08 01` | |
| `0x04` | `0x16` | 2 | `08 02` | |
| `0x05` | `0x0a` | 13 | `0a 07 "713f855" 10 40 18 00` (pb string = build ID) | only readable ASCII string among these three groups |
| `0x05` | `0x0b` | 34–35 | mostly zero varints + 2 larger fields (`CAP-001`/`CAP-002` only, not seen in this file's window) | |
| `0x05` | `0x0c` | 0 | — | |
| `0x09` | `0x02` | 2 | `08 00` | |
| `0x09` | `0x03` | 0 | — | |

No spec page defines groups `0x05`/`0x09` as freestanding (this file's original §5 finding stands
— search results keep redirecting to Device Information's own `0x05`/`0x06`/`0x09` *codes*, not a
same-numbered *group*).

> **Task 3 (2026-08-12): are Groups `0x05`/`0x09` genuine standalone groups, or is the group byte
> actually part of the preceding message (an alternative packing this section already flagged as
> possible)? Tested directly — genuine standalone groups, confirmed, not a reassembly artifact.**
> The stream-reassembling parser from §5a (below) was run over DLCI 0x08's **entire** byte stream —
> not just the initial burst — for all four captures. Result: **every single one of 99 (`CAP-001`),
> 273 (`CAP-002`), 27 (`CAP-003`), and 66 (`CAP-004`) reassembled TLV messages parses cleanly, with
> exactly 0 leftover/unaccounted bytes at the end of each session's stream, and zero parse errors
> (no `INCOMPLETE`, no invalid-protobuf-field-0 markers) anywhere.** Groups `0x05` and `0x09`
> specifically occur 5–13 and 6–13 times respectively across the four sessions, every occurrence
> cleanly bounded and, where non-empty, decoding as well-formed protobuf (e.g. Group `0x05` Code
> `0x0a`'s value consistently decodes to the same build-ID string shape, `field1="713f855"`,
> across all four captures). **This rules out the "group byte belongs to the previous message"
> alternative decisively, not just weakly:** if the true message boundaries were different from
> what a naive `[Group][Code][Len][Value]` read produces, the parser would desync somewhere across
> hundreds of independently-timed messages spanning four separate capture sessions (one of them,
> `CAP-002`, effectively covering many hours) — it never does, not once. **Conclusion: Groups
> `0x05` and `0x09` are genuine, self-contained, standalone Message-Stream-shaped groups on DLCI
> 0x08's private envelope — confirmed 🟢 FACT for "these are real groups, not a parsing artifact."**
> Their *semantic identity* (what a private Group `0x05`/`0x09` on this specific, non-Fast-Pair-spec
> channel actually represents) remains 🔴 OPEN QUESTION, unchanged — this task only closes the
> structural question, not the meaning.

> **Task 5 (2026-08-12): correlating Group `0x04`'s individual codes (`0x02`–`0x16`) against each
> capture's own `CAP-00n-EVENT-NOTES.md` timeline.** Two clearly different behavior classes emerge:
>
> - **Codes `0x02`, `0x04`, `0x05`, `0x11`, `0x13`, `0x14`, `0x15`, `0x16`** — in **every** capture
>   that opens DLCI 0x08 (`CAP-001`, `CAP-002`, `CAP-003`, `CAP-004`), every occurrence of these
>   codes falls within ~1 second of the channel opening, before any subsequent user/hardware event
>   in that session's own timeline. **Correlation: 100% with "DLCI 0x08 has just opened" (connection
>   setup), 0% with any later, discrete hardware/app event** — consistent with, and reinforcing,
>   this section's existing "one-time setup handshake" characterization. No further event-level
>   meaning is extractable from existing captures; these codes never recur, so there is nothing
>   later to correlate against.
> - **Code `0x12`** — the sole recurring code, already flagged as a "periodic ping." Precise timing
>   across all occurrences (this capture and `CAP-002`, the two sessions with enough post-setup
>   duration to check) shows the *interval* is **not fixed** (gaps of 4–30s, no consistent period),
>   but the *value* alternates near-perfectly between `field1=2` and `field1=3` on almost every
>   occurrence (23 consecutive toggles in this capture's own tail, one exception where two
>   consecutive `field1=3` reads land 79ms apart — frames 2766/2770). An irregular-interval,
>   strictly-alternating pattern is a better fit for a **toggling liveness/sequence-parity bit**
>   (marking successive keep-alive pings) than for a real physical state (which would not toggle
>   with such regularity independent of what's actually happening physically) — 🟡 HYPOTHESIS, not
>   claimed as FACT.
>
> **Per this task's own instruction, flagged for Fase 2 Taak 8 (needs a new, isolated capture, not
> resolvable from existing data):** Code `0x12`'s exact trigger. A capture that brackets a specific,
> known physical event (bud removed from ear, case closed, a deliberate multi-minute idle wait)
> while recording this code's occurrences would show whether the toggle sequence ever *breaks* or
> *skips* at that moment (supporting an event-driven reading) or continues its regular alternation
> unperturbed (supporting a free-running liveness counter). None of the codes in the first bullet
> above need a new capture to resolve further — they are fully explained as connection-setup
> handshake content by the data already in hand.

**Answering this task's literal question:** with the sole exception of Code
`0x0a`'s embedded build-ID string, none of groups `0x04`/`0x05`/`0x09`'s values are ASCII —
they're small, fixed-length (0/2/4/13 bytes) varint-encoded protobuf fragments, consistent with a
capability/feature-flag negotiation rather than data transfer. Combined with (a) every one of these
groups' *first* occurrence landing within ~1 second of DLCI 0x08 opening in all three captures, and
(b) Group `0x04` Code `0x12` being the only code that keeps recurring afterward — this reads as a
**one-time capability/setup handshake for whatever runs on DLCI 0x08** (plausibly `libmaestro`
itself, or a lower-level companion-device negotiation independent of Fast Pair — it survives GMS
being disabled per this file's own §4b), followed by a low-rate periodic status ping (Group `0x04`
Code `0x12`) for the rest of the connection. Group identity itself (what "0x04"/"0x05"/"0x09" *mean*
as private groups on this DLCI) remains 🔴 open — no spec covers a private/vendor numbering here,
consistent with `PROTOCOL.md` §2.1's own note that Google permits vendor-private Message Stream
groups.

**Reproduction — Python (stream reassembler + generic protobuf-varint decoder), run against
`data.data`-exported `tshark` fields:**
```python
def parse_tlvs_stream(rows, target_dlci):
    """rows: list of (frame_no, time, dlci, payload_bytes), time-ordered.
    Buffers bytes per DLCI so a [Group][Code][Len:2B-BE][Value] message split
    across consecutive RFCOMM frames is still parsed as one logical message,
    attributed to every frame number whose bytes it spans."""
    buf = bytearray(); spans = []; base = 0; out = []
    for fno, t, dlci, data in rows:
        if dlci != target_dlci: continue
        start = base + len(buf); buf += data
        spans.append((start, start + len(data), fno, t))
        i = 0
        while i + 4 <= len(buf):
            group, code = buf[i], buf[i+1]
            length = (buf[i+2] << 8) | buf[i+3]
            if i + 4 + length > len(buf): break
            value = bytes(buf[i+4:i+4+length])
            lo, hi = base + i, base + i + 4 + length
            frames = sorted({s[2] for s in spans if not (s[1] <= lo or s[0] >= hi)})
            out.append((group, code, length, value, frames))
            i += 4 + length
        if i: del buf[:i]; base += i
    return out
```
Extraction command used for the raw per-frame input:
```
tshark -r CAP-004-btsnoop_hci.log -Y "btrfcomm.len > 0" -T fields -E separator='|' \
  -e frame.number -e frame.time_epoch -e btrfcomm.dlci -e data.data
```

## 6. GATT service list from nRF Connect's UI — cross-checked against `CAP-002`/`CAP-003`'s open UUID questions (🟡 HYPOTHESIS, UI-sourced not wire-confirmed)

As established in §1's method and `CAP-003`'s own findings, nRF Connect's displayed service list
comes from Android's **cached** GATT database (confirmed here too — the only GATT traffic at
connect time, frame 1524, is a `Database Hash` check, not live discovery). The list itself is
still genuine, real data (Android didn't fabricate it), just not re-verifiable against wire
traffic in this specific session. Full list observed on screen (`CAP-004-EVENT-NOTES.md`):

`Generic Attribute (0x1801)`, `Generic Access (0x1800)`, `Broadcast Audio Scan Service (0x184F)`,
`Audio Stream Control Service (0x184E)`, `Published Audio Capabilities Service (0x1850)`,
`Volume Control (0x1844)`, `Microphone Control (0x184D)`, `Audio Input Control (0x1843)`,
`Common Audio Service (0x1853)`, `Telephony and Media Audio Service (0x1855)`, **`Google Fast
Pair Service (0xFE2C)`**, **`Accessory Non-Owner Service (13190001-12f4-c226-88ed-
2ac5579f2a85)`**, `Device Information (0x180A)`, `Battery Service (0x180F)`, and an unnamed
**`Unknown Service (109b8b21-50e3-45cc-8ea1-ac62de4846d1)`**.

This is the first time this project has a candidate **named** service list for the Buds at all.
Combined with `CAP-002`/`CAP-003`'s handle numbers, a structural hypothesis (not wire-confirmed):

- Handle `0x0f2a` (returns `"Revision 6"`) and `0x0f28` (returns `0x31`) sit in a **high** handle
  range, consistent with falling inside **`Device Information (0x180A)`**, which per `CAP-002`'s
  spec research (Manufacturer/Model/Serial, then Hardware/Firmware/Software Revision String,
  consecutively) is exactly the kind of service that would place a Firmware/Software Revision
  characteristic near a Hardware Revision characteristic two handles apart.
- The `0x0c0X` handle cluster (encrypted-looking write/notify bursts, CCCD-enable pattern) sits
  in a **lower-middle** handle range — a plausible fit for **`Google Fast Pair Service
  (0xFE2C)`**, whose documented Key-based Pairing / Passkey / Account Key characteristics are
  specified to use exactly this encrypted-write/notify shape.
- **`Accessory Non-Owner Service (13190001-12f4-c226-88ed-2ac5579f2a85)`** is a non-standard
  128-bit UUID (not a Bluetooth SIG base UUID) — the name and structure strongly suggest this is
  Google's **Find My Device Network (FMDN)** accessory service already referenced in
  `PROTOCOL.md`/`DECISIONS.md` in the Fast Pair context, not yet linked to a concrete UUID
  anywhere in this project's documents until now.

None of this is a confirmed handle→UUID mapping (still requires the live-discovery capture
`CAP-003` §7 already recommended) — it is a plausible, multi-source-consistent HYPOTHESIS,
recorded so the eventual discovery capture has concrete predictions to check itself against.

**Update 2026-08-12 (deskresearch task): confirmed this capture's own wire traffic still cannot
resolve any handle→UUID mapping either — same negative result as `CAP-003`, now checked
exhaustively rather than assumed.** Filtered this file's `CAP-004-btsnoop_hci.log` for every ATT `Read By
Group Type Response` (`btatt.opcode==0x11`) and `Read By Type Response` (`btatt.opcode==0x09`):

```
tshark -r CAP-004-btsnoop_hci.log -Y "btatt.opcode==0x11" -T fields -e frame.number -e bthci_acl.src.bd_addr -e bthci_acl.dst.bd_addr -e btatt.uuid128 -e btatt.uuid16
```

All 8 `Read By Group Type Response` frames in this log (229, 234, 239, 244, 249, 254, 259, 264)
involve BD_ADDR `62:6f:64:e2:1b:4a` — the maintainer's Fitbit Charge 6, already flagged and
excluded as unrelated background traffic in §1 above, **not** the Buds. Filtering explicitly to
frames involving the Buds' own address (`04:00:6e:cf:6e:07`) returns **zero** `Read By Group Type`
frames and only four `Read By Type Response` frames, all resolving low, standard GAP/GATT-service
handles (`0x0002`–`0x0009`: `0x2a00` Device Name, `0x2a05` Service Changed, `0x2b29` Client
Supported Features, `0x2b2a` Database Hash, `0x2b3a` Server Supported Features) — nowhere near the
`0x0f2a`/`0x0f28`/`0x0c0X` range this task asked about. **Cross-checked against `CAP-002`'s own
full, non-restarted 8h20m log for the same opcode/address combination — also zero results, across
the entire day, not just the ~150s slice `CAP-002`'s own §7 already checked.** Combined with
`CAP-003` §1's identical finding, this makes it **three for three**: no capture taken so far has
ever triggered a live GATT primary-service discovery against the Buds — Android's cached GATT
database has survived every bond-removal/reconnect attempted to date. §6's handle→UUID hypotheses
above remain HYPOTHESIS only; `CAP-003` §7 item 1's stronger cache-busting recommendation (clear
the Bluetooth system app's storage/cache directly, or discover from a phone that has genuinely
never connected to this device) is the only path left untried.

## 7. Other observations

- **The "Enable Google Play services" notification persists for the entire session (🟢 FACT,
  video evidence, 06:23:01 onward)** — direct on-screen confirmation the GMS-disabled condition
  held throughout, not just at setup-check time.
- **No Fast Pair "half-sheet" or "Save device" dialog appears anywhere in this video** — consistent
  with `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group S's own manually-validated setup note that this
  GMS-driven UI does not appear when GMS is disabled.
- **The end-of-session device page is the generic Android Bluetooth settings page, not any
  Buds-specific UI (🟢 FACT, video evidence, 06:25:11)** — no Sound/ANC/EQ/Hearing wellness
  section at all, unlike `CAP-002`/`CAP-003`'s Pixel-Buds-app screens — direct confirmation that
  ANC/EQ control simply isn't reachable at all without the official app, reinforcing why
  `libmaestro` command capture (the project's ultimate goal) requires the app to be present, per
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s own guidance to do the primary/app-based capture first.

## 8. Recommended next steps

1. ~~Determine the meaning of Message Stream Groups `0x04`, `0x05`, and `0x09` (§5)~~ — **partially
   done 2026-08-12, see §5a:** the *group identity* (which private protocol these belong to) is
   still 🔴 open, but their framing is now fully decoded, cross-capture-confirmed, and shown to
   predate this capture (already present in `CAP-001`/`CAP-002`, just undecoded until now).
2. ~~Add a non-destructive correction note to `CAP-002`'s `CAP-002-FINDINGS.md` §3~~ — **done 2026-08-12**,
   see `CAP-002` `CAP-002-FINDINGS.md` §3's own 2026-08-12 addendum.
3. The live-GATT-discovery capture already recommended in `CAP-003`'s `CAP-003-FINDINGS.md` §7 item 1
   remains the only way to confirm §6's handle→UUID hypotheses — **re-confirmed still necessary,
   2026-08-12: this capture's own wire traffic was checked exhaustively (§6 update) and, like
   `CAP-002`/`CAP-003`, never contains a live discovery response.** This capture adds concrete
   named candidates (Fast Pair Service, Accessory Non-Owner Service, Device Information) worth
   checking against once that capture exists.
4. A repeat of this same GMS-disabled test *without* nRF Connect in the mix (pure system-settings
   pairing, per the original Group S design) would cleanly isolate whether nRF Connect's presence
   affected any of this session's results (e.g. the CTKD pairing mechanism in §2 might be an
   artifact of nRF Connect's early BLE connection, not of GMS being disabled) — worth doing once,
   even though this capture's core `GFPS-001` answer (§4) is not expected to change.

## 9. `GFPS-001` outcome and `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` status

Per the three possible outcomes defined for this test: the result is **outcome 3 (present but
different)** — more precisely, "present for one sub-mechanism, absent for another," which
`CAP-002` had not yet distinguished as two separate mechanisms. This is recorded as 🟡 in
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (not 🟢), because:
- The *absence* of the channel-2 TLV content (§4a) is a clean, checked negative result, but one
  capture cannot rule out that some other trigger (not exercised in this session) would still
  produce it under GMS-disabled conditions.
- The channel-4 content's *presence* (§4b) is solid, but was already established as
  Buds-initiated-shaped in `CAP-001` before this test — this capture confirms it survives
  GMS-disabled conditions, which is the new information, not a first discovery.
- The newly-found Group `0x04`/`0x05`/`0x09` content (§5) is a genuine unknown this test
  surfaced, not resolved by it.

## 10. Promotion readiness — what's ready for `PROTOCOL_NOTES.md` (updated 2026-08-11)

**Ready to promote now (🟢 FACT):**
- Cross-Transport Key Derivation as an alternate classic-bonding path when an LE Secure
  Connections link already exists (§2) — new addition to `PROTOCOL.md` §5's connection-lifecycle
  material.
- The channel-2/DLCI-0x04 TLV content from `CAP-002` §3 does not appear when GMS is disabled
  and no Pixel Buds app is present, in a session that otherwise fully connects and bonds (§4a) —
  now also cross-referenced as a non-destructive correction in `CAP-002`'s `CAP-002-FINDINGS.md` §3.
- The channel-4/DLCI-0x08 content from `CAP-001` (`google-pixel-buds-pro-v1`, `Europe/
  Amsterdam`, capability blob) reappears unchanged under the same GMS-disabled conditions (§4b).
- RFCOMM channel numbers continue to vary session-to-session (channels 1/2 never opened this
  time at all) — a fourth confirming data point for the reusable note in `CAP-001`'s
  `CAP-001-FINDINGS.md` §2.
- **Message Stream Group `0x04` = "Device action"**, spec-confirmed directly against Google's
  own `deviceaction` page (§5) — corroborates `PROTOCOL.md` §4.4's existing Ring/Find-My-Buds
  hypothesis (code `0x01` = Ring) with an authoritative source, and gives the group itself a
  confirmed identity even though most individual codes within it (`0x02`–`0x16` observed here)
  remain open.
- **Message Stream Group `0x07` Code `0x41` = "Indicate in use account key,"** spec-confirmed
  via the SASS extension page (§5), which names the exact group **and** code together over the
  same transport — materially stronger than `CAP-002`'s original phrase-match evidence. `CAP-002`
  §3's corresponding bullet is flagged there for upgrade to 🟢 (not edited in that file this
  round, per this session's scope).
- Fragmentation behavior discovered: a single Message Stream message can split its header and
  value across two consecutive RFCOMM packets (§5's frame 2272+2274 correction) — a
  methodological finding relevant to how *all* prior and future captures' RFCOMM data should be
  reassembled before TLV-parsing, not specific to this capture's content. **Extended 2026-08-12
  (§5a): two more, previously-uncaught instances of the same fragmentation pattern found via a
  full-session automated reassembler (frames [2360,2361] and [2365,2366]) — this is a recurring
  behavior, not a single anomaly, and `CAP-001`'s two occurrences of the same logical message were
  each sent whole (not fragmented) — i.e. fragmentation for a given message type is
  non-deterministic and any decoder must handle both cases.**
- **2026-08-12: DLCI 0x08's private Group/Code/Length envelope (groups `0x01`,`0x02`,`0x03`,
  `0x04`,`0x05`,`0x09`,`0x0e`) is confirmed present, byte-for-byte structurally identical, in
  `CAP-001` and `CAP-002` as well as this capture — not new to `CAP-004`** (§5a). Frame 2305
  (Group `0x03` Code `0x02`, 63-byte protobuf value containing `"release_5.203"`) reproduces
  byte-for-byte in `CAP-002` at frame 49028, ruling out a coincidental/non-TLV reading and
  confirming this is a real, stable, private envelope distinct from `CAP-002` §3's official-spec
  DLCI-0x04 channel.

**Not ready yet:**
- Groups `0x05`/`0x09`'s identity (§5) — search results kept redirecting to Device Information
  (Group `0x03`) codes `0x05`/`0x06`/`0x09` instead of confirming standalone groups; **not a
  reassembly artifact (ruled out 2026-08-12, §5a — reassembly is confirmed correct and stable
  across three independent captures)** — these are genuinely undocumented private group numbers.
- ~~Frame 2305's precise field mapping (§5)~~ — **resolved 2026-08-12, see §5a: not a genuine
  Message Stream header collision.** The header IS a real `[Group][Code][Len]` TLV envelope
  (ruled out pure-protobuf byte-0 reading; confirmed byte-for-byte identical to `CAP-002` frame
  49028), but it belongs to DLCI 0x08's own private Group/Code namespace, not DLCI 0x04's
  official-spec namespace `CAP-002` §3 verified — the two `Group 0x03 Code 0x02`s are unrelated
  messages that happen to share numeric labels. `CAP-002` §3's open question about `"Revision 6"`
  vs. `"release_5.203"` is now understood as comparing two different channels' fields, not two
  readings of one field — no longer an unresolved ambiguity, just two independently real values.
- Individual codes within Group `0x04` beyond Ring (§5) — codes `0x02`–`0x16` now byte-decoded
  (§5a), several fixed-shape and one (`0x12`) confirmed as a recurring periodic ping, but their
  semantic *meaning* remains open.
- Any handle→UUID mapping (§6) — hypotheses only, still needs a real discovery capture;
  **re-confirmed 2026-08-12 that this capture's own wire traffic cannot supply it either (§6
  update) — three independent captures (`CAP-002`, `CAP-003`, `CAP-004`) have now all failed to
  trigger live discovery.**
- Whether nRF Connect's presence (vs. a pure system-settings flow) influenced any result here
  (§8 item 4).
- The `libmaestro`/ANC-EQ control channel identity — **still completely unaddressed by any of
  the four captures to date.**
