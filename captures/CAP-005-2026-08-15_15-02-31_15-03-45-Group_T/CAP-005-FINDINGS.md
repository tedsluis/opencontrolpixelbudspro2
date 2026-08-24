# Findings: `CAP-005` (Group T — EQ command isolation)

Standardized, evidence-based extraction from `CAP-005-btsnoop_hci.log` + `CAP-005-recording.mp4`,
staged here for later promotion into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Modeled on
`captures/CAP-004-2026-08-11_06-22-36_06-25-12-Group_S/CAP-004-FINDINGS.md`. Every claim below carries a status per
`PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number, and (per `PROJECT_RULES.md`
  §1 item 4a) reproducible via the stated command/script against the stated hex.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed (this is the
  *first* pass at decoding this content — nothing here is promoted past 🟡, per this session's own
  task instructions).
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-005` · **Date:** 2026-08-15 · **Firmware:** `release_5.203` · **Phone:** Pixel
7a, Android 17 (official app v1.0.955078536). **Log file:** `CAP-005-btsnoop_hci.log` (2,533 frames). **Video:**
`CAP-005-recording.mp4` (74.77s, H.264 720x1280, burned-in wall-clock overlay, CEST/+0200).
**Device:** Buds `04:00:6e:cf:6e:07` (matches `CAP-001`–`CAP-004`'s `Google_cf:6e:07`), phone
`e8:d5:2b:7e:ca:81`, single classic ACL connection, handle `0x0002` — confirmed via
`bthci_evt.code==0x03` (Connect Complete, frame 389).

**Stated goal of this session** (`CAP-005-EVENT-NOTES.md`): isolate the two Test-IDs `EQP-002`
(EQ preset tap) and `EQS-004` (EQ Bass slider drag) with a clean ≥10s gap around each, to determine
which RFCOMM channel/DLCI carries EQ commands and whether presets and sliders share the same wire
format. **Answer, per the evidence below: yes to both — DLCI 0x02, same envelope shape, differing
only in the outer field number and the one changed EQ-band value (§5).**

---

## 1. Video verification — independent re-check of `CAP-005-EVENT-NOTES.md`'s timeline

**Method:** `ffprobe` for container metadata, then `ffmpeg -ss <t> -frames:v 1` frame extraction at
1-second resolution around both claimed event times, reading the video's own burned-in wall-clock
overlay (bottom-right corner, `DD mmm YYYY HH:MM:SS`, updates once per second). Video-relative time
`t=0` reads `15 aug 2026 15:02:31`, confirming `CAP-005-EVENT-NOTES.md`'s stated start time and giving a
direct `t → wall-clock` offset of `+15:02:31`.

```
ffprobe -v quiet -print_format json -show_format -show_streams CAP-005-recording.mp4
for t in 0 40 41 42 43 44 51 52 53 54 55 56 57 58 73; do
  ffmpeg -y -ss $t -i CAP-005-recording.mp4 -frames:v 1 -q:v 2 "t${t}.jpg" -loglevel error
done
```

| Video `t` | On-screen clock | Observation |
|---|---|---|
| 0 | 15:02:31 | Recording start, Bluetooth-off settings sheet (matches notes) |
| 41 | 15:03:12 | Preset picker open, finger over the list; visible options: Default, **Hea[vy bass]**, **Ligh[t bass]**, Balanced, Vocal boost, Clarity |
| 42 | 15:03:13 | Preset now applied — dropdown reads **`Heavy bass`**, Bass/Low-bass sliders shifted right of center | Matches `EQP-002`'s claimed 15:03:13 exactly |
| 51 | 15:03:22 | Finger on/near the Bass slider, handle still at its post-preset position | |
| 52 | 15:03:23 | Bass slider handle has moved **left** (reduced) — drag already registered on-screen | |
| 53 | 15:03:24 | `Save` button switches from greyed-out to enabled (purple) — this is the moment `CAP-005-EVENT-NOTES.md` records as `EQS-004` | |
| 55–57 | 15:03:26–28 | Finger moves to and taps `Save` | |
| 58 | 15:03:29 | `"EQ saved"` toast appears, preset label reverts to `Last saved` | |

The video shows **three** distinct, wire-relevant moments in the `EQS-004` action, not one — (a)
the slider drag completing, on-screen between `t=51` and `t=52` (15:03:22–15:03:23), (b) the
`Save` button becoming enabled at 15:03:24, and (c) the user actually tapping `Save` at
approximately 15:03:27–28, confirmed saved at 15:03:29. **§3/§5 below show (a) and (c) correspond
to two separate wire bursts** — (b) is a pure UI-state change with no new wire traffic at that
exact moment. `CAP-005-EVENT-NOTES.md`'s own Event Timeline (and its "Corrections" section) already
reflects this three-moment breakdown directly.

**Not a discrepancy (checked and ruled out):** the on-screen preset name **`Heavy bass`** differs
textually from `CAP-005-EVENT-NOTES.md`'s `EQP-002` label `'Bass Boost'`, but `PROTOCOL.md` §4.2 already
lists `Bass Boost/Heavy Bass` as a known synonym pair for the same preset — so this is a naming
variant already on record, not a new correction.

## 2. Methodology & filtering

**Target-device pre-filter (`AGENTS.md` §13):** this session has exactly one classic ACL
connection for its whole duration — Connect Complete (frame 389) resolves handle `0x0002` to BD_ADDR
`04:00:6e:cf:6e:07` (the Buds), and every RFCOMM frame in the log carries that same
`bthci_acl.chandle`. Filtering by `bthci_acl.chandle == 0x0002` is therefore equivalent to filtering
by the Buds' `bluetooth.addr` for this capture (the direct `bluetooth.addr` filter itself returns
zero rows against `btrfcomm`, because Wireshark's BD_ADDR dissection is only populated on
name-resolution event frames, not on ordinary ACL data frames — confirmed empirically before
settling on the `chandle` filter):

```
tshark -r CAP-005-btsnoop_hci.log -Y "bthci_evt.code==0x03" \
  -T fields -e frame.number -e bthci_evt.bd_addr -e bthci_evt.connection_handle
# -> 389  04:00:6e:cf:6e:07  0x0002
```

**Protocol hierarchy overview** (confirms which DLCIs carry traffic at all this session):

```
tshark -r CAP-005-btsnoop_hci.log -q -z io,phs
```
`btrfcomm` DLCIs seen this session: `0x00` (mux control), `0x02`, `0x04`, `0x08`, `0x0a`, `0x0c`.

## 3. Analysis: `EQP-002` (Preset: "Heavy bass" / "Bass Boost")

Window queried: 15:03:10–15:03:16 (±3s around the verified 15:03:13 on-screen change), all DLCIs,
`chandle==0x0002`:

```
tshark -r CAP-005-btsnoop_hci.log \
  -Y "bthci_acl.chandle==0x0002 and btrfcomm.len>0 and frame.time>=\"2026-08-15 15:03:10\" and frame.time<=\"2026-08-15 15:03:16\"" \
  -T fields -E separator='|' -e frame.number -e frame.time -e btrfcomm.dlci -e btrfcomm.len -e data.data
```

**Result: traffic appears on DLCI 0x02 only** — DLCI 0x04 (official Fast Pair Message Stream) and
DLCI 0x08 (private envelope) are completely silent in this window (checked explicitly, zero rows —
see §4's identical check reused for both windows). Three frames, one Sent + two Rcvd:

| Frame | Time | Dir | DLCI | Raw hex |
|---|---|---|---|---|
| 1245 | 15:03:12.018963 | Sent (phone→Buds) | 0x02 | `7e003b0310131dea71de7d5e251d9a8c9e2a1e221c8201190d0000a04015000040401d0000000025000000002d000000007b1bccbe7e` |
| 1249 | 15:03:12.074566 | Rcvd (Buds→phone) | 0x02 | `7e80a3032a1e221c8201190d0000a04015000040401d0000000025000000002d00000000080710131dea71de7d5e25f5ad21284adbcc867e` |
| 1250 | 15:03:12.075254 | Rcvd (Buds→phone) | 0x02 | `7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e` |

**Timing:** frame 1245 (Sent) lands at 15:03:12.02 — **0.98s before** the on-screen preset change
becomes visible at 15:03:13 (§1), consistent with the app sending the command as soon as the tap
registers, with the UI updating on the following screen redraw.

**HDLC decode** (per `PROTOCOL.md` §2.2a's established method — flag `0x7E`, unescape `0x7D <X>` →
`X^0x20`, LEB128 address, 1-byte control, trailing 4-byte CRC-32/IEEE-802.3/zlib, little-endian):

```python
def unescape_hdlc(data):
    out = bytearray(); i = 0
    while i < len(data):
        b = data[i]
        if b == 0x7d:
            i += 1; out.append(data[i] ^ 0x20)
        else:
            out.append(b)
        i += 1
    return bytes(out)
# body, trailer = unescape_hdlc(raw[1:-1])[:-4], unescape_hdlc(raw[1:-1])[-4:]
# assert struct.pack('<I', binascii.crc32(body) & 0xffffffff) == trailer
```

| Frame | Address | Control | CRC check | Payload (post address+control) |
|---|---|---|---|---|
| 1245 | `0x0000` | `0x3b` | 🟢 match (`7b1bccbe`) | `0310131dea71de7e251d9a8c9e2a1e221c8201190d0000a04015000040401d0000000025000000002d00000000` (45B) |
| 1249 | `0xd180` | `0x2a` | 🟢 match (`4adbcc86`) | `1e221c8201190d0000a04015000040401d0000000025000000002d00000000080710131dea71de7e25f5ad2128` (45B) |
| 1250 | `0xd180` | `0x08` | 🟢 match (`e6d97e`†) | `0110131dea71de7e251d9a8c9e` (13B) |

†CRC verified programmatically against the full trailer; truncated in this table for width. All
three CRCs verified via the same `crc32`-over-unescaped-body method as `PROTOCOL.md` §2.2a — 🟢
FACT (reproducible, script above + raw hex above).

**Address `0x0000`** = phone→Buds direction, **`0xd180`/53632** = Buds→phone — both already
documented in `PROTOCOL.md` §2.2a, reconfirmed here.

## 4. Analysis: `EQS-004` (Slider: Bass)

Window queried: 15:03:20–15:03:30 (covers both the drag, per §1's corrected timing, and the
subsequent `Save` tap):

```
tshark -r CAP-005-btsnoop_hci.log \
  -Y "bthci_acl.chandle==0x0002 and btrfcomm.len>0 and frame.time>=\"2026-08-15 15:03:20\" and frame.time<=\"2026-08-15 15:03:30\"" \
  -T fields -E separator='|' -e frame.number -e frame.time -e btrfcomm.dlci -e btrfcomm.len -e data.data
```

Again, **DLCI 0x02 only** — explicitly re-checked for DLCI 0x04 and 0x08 in this exact window:

```
tshark -r CAP-005-btsnoop_hci.log -Y "bthci_acl.chandle==0x0002 and btrfcomm.dlci==0x04 and btrfcomm.len>0 and frame.time>=\"2026-08-15 15:03:20\" and frame.time<=\"2026-08-15 15:03:30\"" -T fields -e frame.number   # 0 rows
tshark -r CAP-005-btsnoop_hci.log -Y "bthci_acl.chandle==0x0002 and btrfcomm.dlci==0x08 and btrfcomm.len>0 and frame.time>=\"2026-08-15 15:03:20\" and frame.time<=\"2026-08-15 15:03:30\"" -T fields -e frame.number   # 0 rows
```

**Result: two separate, structurally-identical-shaped bursts, not one** — this is the direct wire
counterpart of §1's video correction:

| Frame | Time | Dir | Raw hex |
|---|---|---|---|
| 1321 | 15:03:22.313840 | Sent | `7e003b0310131dea71de7d5e251d9a8c9e2a1e221c8201190d0000a04015333383c01d0000000025000000002d00000000eaf192597e` |
| 1325 | 15:03:22.614288 | Rcvd | `7e80a3032a1e221c8201190d0000a04015333383c01d0000000025000000002d00000000080710131dea71de7d5e25f5ad2128eb4f80b17e` |
| 1326 | 15:03:22.616624 | Rcvd | `7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e` |
| **1338** | **15:03:27.605271** | Sent | `7e003b0310131dea71de7d5e251d9a8c9e2a1e221c9201190d0000a04015333383c01d0000000025000000002d000000003a20cd427e` |
| 1340 | 15:03:27.651085 | Rcvd | `7e80a3032a1e221c9201190d0000a04015333383c01d0000000025000000002d00000000080710131dea71de7d5e25f5ad21288526b6a27e` |
| 1341 | 15:03:27.651706 | Rcvd | `7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e` |

Burst 1 (frame 1321, 15:03:22.31) lands **inside** the drag window identified in §1 (between
`t=51`/15:03:22 and `t=52`/15:03:23, i.e. before the on-screen `Save` button even enables at
15:03:24) — this is the **live preview** write. Burst 2 (frame 1338, 15:03:27.61) lands during the
finger's approach to/tap on `Save` (§1, `t=56`–`57`) — this is the **explicit save** write. Same
HDLC decode method as §3:

| Frame | Address | Control | CRC | Payload |
|---|---|---|---|---|
| 1321 | `0x0000` | `0x3b` | 🟢 (`eaf19259`) | `0310131dea71de7e251d9a8c9e2a1e221c8201190d0000a04015333383c01d0000000025000000002d00000000` |
| 1338 | `0x0000` | `0x3b` | 🟢 (`3a20cd42`) | `0310131dea71de7e251d9a8c9e2a1e221c**92**01190d0000a04015333383c01d0000000025000000002d00000000` |

(`**92**` marks the one header byte that differs between the two bursts — see §5.)

## 5. Cross-command correlation & hypothesis (🟡 all HYPOTHESIS — first pass on this channel's content)

All four `Sent` payloads (1245, 1321, 1338, and 1321≡1338's shared value) line up byte-for-byte
except in two narrow regions. Byte offsets below are relative to the 45-byte payload (i.e. *after*
the HDLC address+control, *before* the 4-byte CRC trailer):

```
off:  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44
1245: 03 10 13 1d ea 71 de 7e 25 1d 9a 8c 9e 2a 1e 22 1c 82 01 19 0d 00 00 a0 40 15 00 00 40 40 1d 00 00 00 00 25 00 00 00 00 2d 00 00 00 00
1321: 03 10 13 1d ea 71 de 7e 25 1d 9a 8c 9e 2a 1e 22 1c 82 01 19 0d 00 00 a0 40 15 33 33 83 c0 1d 00 00 00 00 25 00 00 00 00 2d 00 00 00 00
1338: 03 10 13 1d ea 71 de 7e 25 1d 9a 8c 9e 2a 1e 22 1c 92 01 19 0d 00 00 a0 40 15 33 33 83 c0 1d 00 00 00 00 25 00 00 00 00 2d 00 00 00 00
                                                          ^^                      ^^ ^^ ^^ ^^
                                                       offset 17                    offset 26-29
```

**Only two byte regions ever change; everything else — including a full request/response
correlation echo back from the Buds (§3/§4 `Rcvd` frames) — is byte-identical across all three
captured actions.**

### 5a. Nested length-prefixed structure (verified, not guessed — see script below)

Reading the payload as nested protobuf-style `[tag varint][len varint][sub-message]` /
`[tag varint][fixed32 value]` groups, entirely mechanically (standard wire-format tag decoding:
`field = tag>>3`, `wiretype = tag&7`; wiretype `2`=length-delimited, `5`=32-bit fixed), every tag
and every length byte checks out exactly against the actual byte distances in the payload:

```python
p = bytes.fromhex("0310131dea71de7e251d9a8c9e2a1e221c8201190d0000a04015000040401d0000000025000000002d00000000")
assert p[14] == len(p) - 15 == 30   # tag 0x2a (field 5, LD) @13, len=30, spans 15..44
assert p[16] == len(p) - 17 == 28   # tag 0x22 (field 4, LD) @15, len=28, spans 17..44
assert p[19] == len(p) - 20 == 25   # tag(2B LEB128) @17-18, len=25, spans 20..44
```
Run: `python3 decode_cap005.py` (reproduced in full at the end of this section) — all three
assertions pass for every one of the three `Sent` payloads.

Outer tag at offset 17–18 is a 2-byte LEB128 varint, decoding to a different value in the two
`EQS-004` bursts:

```python
def leb128(b):
    v=0; s=0; i=0
    while True:
        v |= (b[i]&0x7f)<<s; i+=1
        if not (b[i-1]&0x80): break
        s+=7
    return v
leb128(bytes.fromhex("8201"))  # -> 130  -> field 16, wiretype 2 (LD)   [1245, 1321]
leb128(bytes.fromhex("9201"))  # -> 146  -> field 18, wiretype 2 (LD)   [1338]
```

**🟡 HYPOTHESIS:** field **16** = a "preview/apply" write (used for both the preset tap and the
live slider drag — i.e. every value the Buds should render immediately), field **18** = the
explicit "save" write triggered by the `Save` button tap — matching the app's own on-screen copy,
*"Moving sliders creates a custom tuning. Settings are saved after leaving this page."* Consistent
with, but not proven by, this single capture: burst 1 (field 16, 15:03:22.31, mid-drag) and burst 2
(field 18, 15:03:27.61, at Save) carry the **identical** band value (`33 33 83 c0`, §5b) — i.e. Save
does not introduce a new value, only a new outer field/wrapper, exactly as this hypothesis predicts.

### 5b. The 5-band EQ quintet (innermost 25-byte sub-message, offsets 20–44)

Within the length-25 sub-message, five consecutive `[tag: fixed32][value: 4 bytes LE]` fields
appear back-to-back with zero gap or leftover byte:

| Tag offset | Tag byte | Field # | Value offset | Value bytes (1245 / 1321,1338) | `struct.unpack('<f', …)` |
|---|---|---|---|---|---|
| 20 | `0x0d` | 1 | 21–24 | `00 00 a0 40` (constant, all 3 frames) | **5.0** |
| 25 | `0x15` | 2 | 26–29 | `00 00 40 40` → `33 33 83 c0` | **3.0 → −4.1** |
| 30 | `0x1d` | 3 | 31–34 | `00 00 00 00` (constant) | 0.0 |
| 35 | `0x25` | 4 | 36–39 | `00 00 00 00` (constant) | 0.0 |
| 40 | `0x2d` | 5 | 41–44 | `00 00 00 00` (constant) | 0.0 |

```python
import struct
struct.unpack('<f', bytes.fromhex('0000a040'))[0]   # 5.0
struct.unpack('<f', bytes.fromhex('00004040'))[0]   # 3.0
struct.unpack('<f', bytes.fromhex('333383c0'))[0]   # -4.099999904632568
```

**🟡 HYPOTHESIS (strong, but single-capture):** these are the app's 5 EQ band gains, one `float32`
each, sent as a fixed-order quintet regardless of which single slider the user actually touched
(the other 4 always retransmitted at their current value). Field **2** is the only one that ever
changes across this capture's two actions — the only slider the user actually touched was **Bass**
(§1) — making field 2 = **Bass**. `EQP-002`'s "Heavy bass" preset sets it to **+3.0**; `EQS-004`'s
drag pulls it down to **−4.1**. Field **1** = 5.0 and constant across every frame in this
capture — plausibly **Low bass** (visibly pushed to near-max in the "Heavy bass" preset screenshot,
§1, and never touched by the user this session, consistent with never changing on the wire).
Fields 3/4/5 = 0.0 and constant — plausibly **Mid/Treble/Upper treble**, all shown centered
on-screen throughout (§1) and consistent with 0.0 = unity/no-change. **Field order (1↔Low-bass,
2↔Bass, 3↔Mid, 4↔Treble, 5↔Upper-treble) is inferred purely from which single value changed
correlating with which single slider the user touched — not independently confirmed by decoding a
second field's change, since only Bass was moved this session.** A future capture isolating a
*different* single slider (e.g. Treble only) would directly confirm or refute this specific
field-to-band mapping — flagged in §6.

**Not decoded, flagged rather than guessed:** the 8 bytes at payload offset 1–8 (`10 13 1d ea 71 de
7e 25`) and offset 8–12 (`25 1d 9a 8c 9e` — note offset 8 is shared as both the tail of one read and
the tag byte of the next, see the raw hex) are constant across all three captured `Sent` frames in
this capture, and the same 8-byte sequence at offset 1–8 reappears verbatim in the tail of every
`Rcvd` response (frames 1249/1325/1340, e.g. `...080710131dea71de7e25f5ad2128`) — consistent with a
request/response correlation ID (`call_id`) that the Buds echo back, per typical `pw_rpc` request/
response semantics, but **not decoded as a specific field** — attempting to read offset 8's `0x25`
as *both* a fixed32 tag (as in §5b's field 4) *and* the tail of a preceding value would be
internally inconsistent, and nothing in this single-session, single-value-changing capture
disambiguates it further. 🔴 left as-is rather than force-fit.

**Full reproduction script** (all of §3–§5's decodes, self-contained):

```python
import struct, binascii

def unescape_hdlc(data):
    out = bytearray(); i = 0
    while i < len(data):
        b = data[i]
        if b == 0x7d:
            i += 1; out.append(data[i] ^ 0x20)
        else:
            out.append(b)
        i += 1
    return bytes(out)

def leb128(data, i=0):
    val = 0; shift = 0
    while True:
        b = data[i]; val |= (b & 0x7f) << shift; i += 1
        if not (b & 0x80): break
        shift += 7
    return val, i

frames = {
    1245: "7e003b0310131dea71de7d5e251d9a8c9e2a1e221c8201190d0000a04015000040401d0000000025000000002d000000007b1bccbe7e",
    1321: "7e003b0310131dea71de7d5e251d9a8c9e2a1e221c8201190d0000a04015333383c01d0000000025000000002d00000000eaf192597e",
    1338: "7e003b0310131dea71de7d5e251d9a8c9e2a1e221c9201190d0000a04015333383c01d0000000025000000002d000000003a20cd427e",
}
for fno, hx in frames.items():
    raw = bytes.fromhex(hx)
    un = unescape_hdlc(raw[1:-1])
    body, trailer = un[:-4], un[-4:]
    assert struct.pack('<I', binascii.crc32(body) & 0xffffffff) == trailer
    addr, i = leb128(body, 0); ctrl = body[i]; i += 1
    payload = body[i:]
    assert payload[14] == len(payload) - 15
    assert payload[16] == len(payload) - 17
    assert payload[19] == len(payload) - 20
    outer_tag, _ = leb128(payload, 17)
    band2 = struct.unpack('<f', payload[26:30])[0]
    print(fno, hex(addr), hex(ctrl), "outer_field", outer_tag >> 3, "band2_value", band2)
```

Output:
```
1245 0x0 0x3b outer_field 16 band2_value 3.0
1321 0x0 0x3b outer_field 16 band2_value -4.099999904632568
1338 0x0 0x3b outer_field 18 band2_value -4.099999904632568
```

### 5c. Isolation quality — directly supports attributing this content to the EQ actions

A full-session scan of all `Sent`-direction, non-trivial (`btrfcomm.len>5`) DLCI 0x02 frames shows
this channel is essentially silent outside of (a) the initial connection-setup burst
(15:02:39.32–15:02:42.34, ~45 frames, matching the "one-time capability handshake" pattern already
documented for other DLCIs in `CAP-004-FINDINGS.md` §5a) and (b) three 54-byte frames — **exactly**
frames 1245, 1321, 1338 — with nothing else of this size anywhere in the session. The next
DLCI-0x02 activity after 1338 is a single 42-byte frame at 15:04:05, well after this capture's
`EQP-002`/`EQS-004` window closes (15:03:45, per `CAP-005-EVENT-NOTES.md`).

```
tshark -r CAP-005-btsnoop_hci.log \
  -Y "bthci_acl.chandle==0x0002 and btrfcomm.dlci==0x02 and btrfcomm.len>5 and frame.p2p_dir==0" \
  -T fields -e frame.number -e frame.time -e btrfcomm.len
```

## 6. Open Questions

- 🔴 **Field-to-band mapping (§5b) is inferred from a single changed field, not confirmed.** Needs a
  follow-up capture isolating one *different* single slider (e.g. Treble or Upper treble alone) to
  confirm field 3/4/5's assignment, and ideally one isolating Low bass alone to confirm field 1.
- 🔴 **Outer field 16 vs. 18 (§5a) as "preview" vs. "save" is a plausible but unconfirmed
  interpretation.** An isolated capture of a slider drag *without* ever tapping Save (drag, wait
  >10s, navigate away without saving) would show whether field 16 alone appears with no field-18
  follow-up, supporting this hypothesis directly.
- 🔴 **Payload offset 1–12 (the apparent `call_id`/correlation bytes, §5b) not decoded** — constant
  in this capture (only one "session" of RPC calls observed), so nothing here distinguishes a fixed
  session identifier from a per-call sequence number that simply hadn't incremented yet within this
  74-second window.
- 🔴 **The HDLC `Control` byte's three observed values (`0x3b` Sent, `0x2a`/`0x08` Rcvd) are recorded
  but not interpreted** — `PROTOCOL.md` §2.2a documents the Address/Control fields' existence but
  not Control's specific bit meaning; out of scope for this EQ-focused pass.
- ✅ **Checked 2026-08-17 (deskresearch pass, `DESKRESEARCH_FINDINGS.md`): this capture's own
  field-16/18 envelope shape does not appear anywhere in `CAP-001`, `CAP-002`, `CAP-003`, `CAP-006`
  (four cleanly isolated single ANC taps — the exact "differently-isolated ANC-only capture" this
  item called for, already on disk), `CAP-007`, or the 11:42 `CAP-010` session — a clean negative
  result across every capture with DLCI 0x02 traffic. 🟡 HYPOTHESIS (strengthened): the field-16/18
  pair is EQ-specific, not a general-purpose settings-apply/save pair shared with ANC. See
  `DESKRESEARCH_FINDINGS.md`'s 2026-08-17 entry for the full method and per-capture results.

(Per this session's task instructions, these are also being copied into `PROTOCOL.md` §6 — see that
file's own changelog for the corresponding addition.)

## 7. Promotion readiness

**Ready to note in `PROTOCOL.md` (as 🟡 HYPOTHESIS only, not FACT — per `PROJECT_RULES.md` §1):**
- DLCI 0x02 (`libmaestro`'s Pigweed `pw_hdlc` channel, `PROTOCOL.md` §2.2a) is the **first channel
  with EQ-attributable content decoded at all** — previously described there as "opaque ~16-byte
  Sent blocks, unresolved content." This capture's isolation (§5c) directly ties two specific
  54-byte `Sent` bursts to the exact on-screen timing of an EQ preset tap and an EQ slider drag,
  with no concurrent activity on DLCI 0x04 or 0x08 in either window (§3/§4).
- A structural, protobuf-tag-consistent nested envelope is decoded three layers deep (§5a), with
  three independent length-prefix self-checks all passing exactly (§5a's assertions) — stronger
  than a single coincidental byte match, but still only one capture, one band, one direction of
  change (increase-then-decrease of one value) — not yet cross-capture-replicated per
  `PROJECT_RULES.md` §1's promotion bar for 🟢 FACT.
- This is the first concrete candidate wire format for `PROTOCOL.md` §4.2 ("Opcode/payload
  structure: not yet extracted" → a specific, byte-verified 🟡 HYPOTHESIS now exists).

**Not ready:** the field-to-band mapping, the field-16/18 preview/save interpretation, and any
generalization to ANC or other `libmaestro` commands — all explicitly flagged 🔴 in §6, needing
further isolated captures before any promotion to FACT.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-005-2026-08-15_15-02-31_15-03-45-Group_T/CAP-005-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/#/captures/CAP-005-2026-08-15_15-02-31_15-03-45-Group_T/CAP-005-FINDINGS
