# Findings: `CAP-015` (Group T — EQ command isolation), 2026-08-18 capture

Standardized, evidence-based extraction from `CAP-015-btsnoop_hci.log` + `CAP-015-recording.mp4`
(the `06-11-06` folder — **not** the earlier, incomplete `2026-08-15_15-02-31` `CAP-005` folder,
which is a separate, superseded capture and was not used as input here), staged here for later
promotion into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Modeled on
`captures/CAP-001-2026-08-09_08-51-00_08-52-20-Group_Z/CAP-001-FINDINGS.md`. Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number, reproducible via the
  stated command/script against the stated hex.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-015` (2026-08-18 session) · **Date:** 2026-08-18 · **Firmware:** `release_5.203`
· **Phone:** Pixel 7a, Android 17 (official app v1.0.955078536). **Log file:**
`CAP-015-btsnoop_hci.log` (3,728 frames, 641.5s). **Video:** `CAP-015-recording.mp4` (394.3s,
H.264 720×1280, burned-in wall-clock overlay, CEST/+0200, 06:11:06–06:17:40).
**Device:** Buds `04:00:6e:cf:6e:07` (matches `CAP-001`–`CAP-004`'s `Google_cf:6e:07`), single
classic ACL connection, handle `0x0004` — confirmed via `bthci_evt.code==0x03` (frame 1402,
status `0x00`, after an earlier Page Timeout at frame 1395).

**Stated goal of this session** (`CAP-015-EVENT-NOTES.md`): isolate EQ preset taps (`EQP-002`
family) and EQ slider drags (`EQS-004` family) to determine the wire format and, specifically, the
open question the earlier (2026-08-15) `CAP-005` capture left unresolved — **which protobuf field
in the 5-float quintet corresponds to which on-screen EQ band.** **Answer, per the evidence below:
fully resolved.** This session's maintainer, apparently independently, ran a far more thorough
procedure than the Group T minimum — 5 presets, then all 5 sliders each dragged to both extremes
and back near zero (3 passes) — which directly supplies the missing per-band isolation the earlier
capture called for.

---

## 1. Video verification

**Method:** `ffprobe` for container metadata, then `ffmpeg -ss <t> -frames:v 1` frame extraction
(1s resolution, refined to sub-second where needed) reading the video's own burned-in wall-clock
overlay (bottom-right corner, `DD mmm YYYY HH:MM:SS`). `t=0` reads `18 aug 2026 06:11:06`,
confirming `CAP-015-EVENT-NOTES.md`'s stated start time with a direct `t → wall-clock` offset of `+06:11:06`.

```
ffprobe -v quiet -print_format json -show_format -show_streams CAP-015-recording.mp4
ffmpeg -y -ss <t> -i CAP-015-recording.mp4 -frames:v 1 -q:v 3 "t<t>.jpg" -loglevel error
```

Full frame-by-frame review is in `CAP-015-EVENT-NOTES.md`'s Event Timeline. Key confirmations for
this findings file specifically:

| Video `t` (wall clock) | Observation | Confirms |
|---|---|---|
| 148s (06:13:34) | Clarity preset applied; all 5 sliders visible, top-to-bottom: **Upper treble, Treble, Mid, Bass, Low bass** | The UI's fixed slider order — needed for §5's mapping |
| 153s (06:13:39) | Finger directly on the **Upper treble** slider handle | Ties the field-5 (last quintet element) change to **Upper treble** — corroborated at t=168s (06:13:54) by a `-60` value tooltip over the same slider, right as the value settles at `-6.0` (frame 2586). (The dropdown is briefly reopened again at t=154s/06:13:40 with `Clarity` still checked before the wire-visible change appears at 06:13:40.998 — exact sequencing at 1fps is ambiguous, §6.) |
| 239s (06:15:05) | Finger directly over the **Mid** row label, Mid bar already at/near its right (max) extreme | Directly ties the field-3 change to **Mid** |
| 266s (06:15:32) | Finger on the **Bass** row mid-drag, `Save` button purple (active — unsaved change pending), Bass bar near far left (min) | Directly ties the field-2 change to **Bass** |
| 293s (06:15:59) | Finger at bottom-left near the **Low bass** row, Low bass bar at far left just as its drag begins | Directly ties the field-1 change to **Low bass** |

The remaining field (Treble, field 4) is confirmed by elimination (§5) rather than a single
unambiguous finger-on-slider frame — the fast 1s sampling combined with hand occlusion made a
clean shot harder to isolate for that one band specifically; see §5 for why the elimination is
still solid.

## 2. Methodology & filtering

**Target-device pre-filter (`AGENTS.md` §13):** one classic ACL connection for the session —
Connect Complete (frame 1402) resolves handle `0x0004` to `04:00:6e:cf:6e:07`. All RFCOMM frames
below are filtered by `bthci_acl.chandle==0x0004`.

```
tshark -r CAP-015-btsnoop_hci.log -Y "bthci_evt.code==0x03" \
  -T fields -e frame.number -e bthci_evt.status -e bthci_evt.bd_addr -e bthci_evt.connection_handle
# 1395  0x04 (Page Timeout)  04:00:6e:cf:6e:07  0x0004
# 1402  0x00 (Success)       04:00:6e:cf:6e:07  0x0004
```

**Protocol hierarchy overview:**

```
tshark -r CAP-015-btsnoop_hci.log -q -z io,phs
```

`btrfcomm` DLCIs seen this session: `0x00` (mux control), `0x02` (`libmaestro`'s Pigweed `pw_hdlc`
channel, `PROTOCOL.md` §2.2a), `0x04` (official Fast Pair Message Stream), `0x08` (private
envelope), `0x0c` (carries `bthfp` — HFP AT commands, a different DLCI number than `CAP-001`'s
0x09, consistent with `PROTOCOL.md` §2.3's "channel numbers are not stable per-profile labels"
rule). During the EQ interaction window (06:12:13–06:17:09), DLCI 0x04 and 0x08 each carry 21
frames and DLCI 0x0c carries 14 — none silent, but (per a spot check) shaped like the same
low-rate periodic housekeeping/battery traffic documented in prior captures, not correlated with
individual EQ taps. **All EQ-attributable content in this capture is on DLCI 0x02.**

**Extraction command used for everything below:**

```
tshark -r CAP-015-btsnoop_hci.log \
  -Y "bthci_acl.chandle==0x0004 and btrfcomm.dlci==0x02 and btrfcomm.len>5" \
  -T fields -E separator='|' -e frame.number -e frame.time_epoch -e frame.p2p_dir -e data.data
```

**HDLC decode method** (identical to `PROTOCOL.md` §2.2a and the earlier `CAP-005`'s method — flag
`0x7E`, unescape `0x7D <X>` → `X^0x20`, LEB128 address, 1-byte control, trailing 4-byte
CRC-32/IEEE-802.3/zlib, little-endian). **Every quintet-shaped `Sent`-direction DLCI 0x02 frame
between frame 2111 (page-open) and frame 3468 (final save) — 56 frames, the full EQ interaction
window — passes the CRC check (56/56, 100%)**, checked exhaustively, not sampled — reproduction
script in §4 below. (One further frame, 1883 at 06:11:58 during the initial connection-setup
burst well before the EQ window starts, superficially matches the same byte prefix but is actually
three back-to-back HDLC frames concatenated by the extraction filter, not a real 5th quintet
frame — excluded here as out of scope for the EQ analysis, not counted as a CRC failure.)

## 3. Analysis: EQ presets (`EQP-002` family)

Six presets tapped in sequence, dropdown-list order (Default/`Last saved` baseline first, then
Heavy bass → Light bass → Balanced → Vocal boost → Clarity), each an isolated action with the app
settling before the next tap (`CAP-015-EVENT-NOTES.md`'s timeline). All six are `Sent`, HDLC
address `0x0000`, control `0x3b`, outer field 16 (see §4 for what that field means):

| Frame | Time | On-screen preset | Decoded quintet `[Low bass, Bass, Mid, Treble, Upper treble]` |
|---|---|---|---|
| 2111 | 06:12:13.279 | `Last saved` (baseline, page just opened) | `[0.0, 0.0, 0.0, 0.0, 0.0]` |
| 2165 | 06:12:26.370 | **Heavy bass** | `[5.0, 3.0, 0.0, 0.0, 0.0]` |
| 2227 | 06:12:41.340 | **Light bass** | `[-5.0, -1.5, 0.0, 0.0, 0.0]` |
| 2303 | 06:12:57.324 | **Balanced** | `[-3.5, 0.5, 1.0, -1.0, 2.5]` |
| 2351 | 06:13:11.028 | **Vocal boost** | `[-1.0, 0.0, 4.0, 2.0, 0.0]` |
| 2400 | 06:13:26.064 | **Clarity** | `[-2.0, 0.0, 2.0, 3.0, 5.0]` |

Raw hex for the first three (representative — full set in the reproduction script's `frames` dict,
§4):

```
2111  7e003b0310131dea71de7d5e251d9a8c9e2a1e221c8201190d0000000015000000001d0000000025000000002d00000000881667fe7e
2165  7e003b0310131dea71de7d5e251d9a8c9e2a1e221c8201190d0000a04015000040401d0000000025000000002d000000007b1bccbe7e
2227  7e003b0310131dea71de7d5e251d9a8c9e2a1e221c8201190d0000a0c0150000c0bf1d0000000025000000002d00000000e774823b7e
```

**🟢 FACT, cross-capture replicated:** frame 2165's `Heavy bass` quintet `[5.0, 3.0, 0.0, 0.0, 0.0]`
is **byte-for-byte identical** to the "Heavy bass"/"Bass Boost" preset decoded independently in the
earlier (2026-08-15) `CAP-005-FINDINGS.md` §3 (`Low bass=5.0, Bass=3.0`, rest 0.0), from a
completely separate session five days apart. This is a second, independent capture confirming the
same preset produces the same wire values — satisfies `PROJECT_RULES.md` §1's cross-capture
replication bar for *this specific preset's encoding*.

**Framing structure** (nested tag/length groups, same shape as the earlier capture's §5a, and
re-verified here — see §4's script for the executable assertions): outer envelope is
`[0x03 0x10 <10 opaque bytes> 0x2a <len> [0x1e/0x1f/0x22 <len> [tag(LEB128) <len> [5×(tag:1
fixed32:4)]]]]`; every length byte matches the actual remaining span in all 6 preset frames, no
exceptions.

## 4. Analysis: EQ sliders (`EQS-004` family) — all 5 bands, full down/up/reset cycle

Unlike the earlier `CAP-005` (one band, one direction, one sample), this session drags **every one
of the 5 sliders**, three times each (to its negative extreme, its positive extreme, and back to
near-zero), always in the same order: **Upper treble → Treble → Mid → Bass → Low bass**. This
produces 56 `Sent` frames total (6 presets + 50 slider-related), of which **15 carry outer field 18
("save" shape) and the rest carry outer field 16 ("preview" shape)** — exactly 3 field-18 frames
per band, matching the 3 drag-cycles per band.

**Per-band summary** (field index within the quintet, 0-based; values are the *final* value of
each of the 3 cycles, all confirmed field-18/"save"-shaped):

| Quintet index | Band (confirmed §5) | Cycle 1 final (frame, time) | Cycle 2 final (frame, time) | Cycle 3 final (frame, time) |
|---|---|---|---|---|
| 4 (last) | Upper treble | `-6.0` (2586, 06:13:54.638) | `6.0` (2653, 06:14:07.672) | `0.2` (3285, 06:16:24.832) |
| 3 | Treble | `5.9` (2819, 06:14:46.551) | `-6.0` (2863, 06:14:54.261) | `0.2` (3328, 06:16:34.846) |
| 2 | Mid | `6.0` (2937, 06:15:06.755) | `-6.0` (3009, 06:15:18.509) | `0.3` (3382, 06:16:46.563) |
| 1 | Bass | `-6.0` (3083, 06:15:33.008) | `6.0` (3142, 06:15:46.176) | `0.0` (3428, 06:16:57.794) |
| 0 (first) | Low bass | `-6.0` (3187, 06:16:00.104) | `5.8` (3228, 06:16:12.770) | `0.1` (3468, 06:17:09.029) |

**🟢 FACT — the ±6.0 clamp:** every "extreme" cycle above lands at exactly `±6.0` except two
(Treble's `5.9` and Low bass's `5.8`, both just short of +6.0 — consistent with the user's drag
gesture not quite reaching the physical slider's right edge before lifting, per the video). This is
consistent, repeatable, and matches the video's own slider track visually bottoming out at the same
physical position each time — the app's EQ band gain range is **±6.0 dB** (or dB-like units; unit
not independently confirmed, only the numeric range).

**Full per-frame table (all 56 `Sent` frames, `Rcvd` ACKs omitted for brevity — every `Sent` frame
here has a matching `Rcvd` echo/ACK pair, same shape as `CAP-001-FINDINGS.md` §2's documented ACK
convention, not separately tabulated):**

```
frame   time          field  [Low bass, Bass, Mid, Treble, Upper treble]
2111  06:12:13.279    16    [ 0.0,  0.0,  0.0,  0.0,  0.0]   <- Last saved baseline
2165  06:12:26.370    16    [ 5.0,  3.0,  0.0,  0.0,  0.0]   <- preset Heavy bass
2227  06:12:41.340    16    [-5.0, -1.5,  0.0,  0.0,  0.0]   <- preset Light bass
2303  06:12:57.324    16    [-3.5,  0.5,  1.0, -1.0,  2.5]   <- preset Balanced
2351  06:13:11.028    16    [-1.0,  0.0,  4.0,  2.0,  0.0]   <- preset Vocal boost
2400  06:13:26.064    16    [-2.0,  0.0,  2.0,  3.0,  5.0]   <- preset Clarity
2470  06:13:40.998    16    [ 4.5, -4.9,  4.5,  3.8,  4.1]   <- baseline jump, see §6 open question
2527  06:13:52.550    16    [ 4.5, -4.9,  4.5,  3.8,  3.9]
2533  06:13:52.705    16    [ 4.5, -4.9,  4.5,  3.8,  1.6]
2541  06:13:52.857    16    [ 4.5, -4.9,  4.5,  3.8, -1.1]
2555  06:13:53.007    16    [ 4.5, -4.9,  4.5,  3.8, -3.5]
2560  06:13:53.160    16    [ 4.5, -4.9,  4.5,  3.8, -5.3]
2573  06:13:53.311    16    [ 4.5, -4.9,  4.5,  3.8, -6.0]
2586  06:13:54.639    18    [ 4.5, -4.9,  4.5,  3.8, -6.0]   SAVE (Upper treble cycle 1)
2616  06:14:05.917    16    [ 4.5, -4.9,  4.5,  3.8, -5.8]
2619  06:14:06.071    16    [ 4.5, -4.9,  4.5,  3.8, -2.4]
2621  06:14:06.225    16    [ 4.5, -4.9,  4.5,  3.8,  1.2]
2631  06:14:06.378    16    [ 4.5, -4.9,  4.5,  3.8,  4.3]
2642  06:14:06.528    16    [ 4.5, -4.9,  4.5,  3.8,  6.0]
2653  06:14:07.672    18    [ 4.5, -4.9,  4.5,  3.8,  6.0]   SAVE (Upper treble cycle 2)
2701  06:14:22.684    16    [ 4.5, -4.9,  4.5,  3.5,  6.0]
2706  06:14:22.837    16    [ 4.5, -4.9,  4.5, -1.1,  6.0]
2708  06:14:22.991    16    [ 4.5, -4.9,  4.5, -5.3,  6.0]
2715  06:14:23.141    16    [ 4.5, -4.9,  4.5, -6.0,  6.0]
2785  06:14:37.662    16    [ 4.5, -4.9,  4.5,  5.9,  6.0]
2819  06:14:46.551    18    [ 4.5, -4.9,  4.5,  5.9,  6.0]   SAVE (Treble cycle 1)
2848  06:14:52.407    16    [ 4.5, -4.9,  4.5, -6.0,  6.0]
2863  06:14:54.261    18    [ 4.5, -4.9,  4.5, -6.0,  6.0]   SAVE (Treble cycle 2)
2917  06:15:04.959    16    [ 4.5, -4.9,  5.0, -6.0,  6.0]
2920  06:15:05.110    16    [ 4.5, -4.9,  6.0, -6.0,  6.0]
2937  06:15:06.755    18    [ 4.5, -4.9,  6.0, -6.0,  6.0]   SAVE (Mid cycle 1)
2969  06:15:16.879    16    [ 4.5, -4.9,  5.7, -6.0,  6.0]
2972  06:15:17.029    16    [ 4.5, -4.9,  2.1, -6.0,  6.0]
2974  06:15:17.182    16    [ 4.5, -4.9, -4.0, -6.0,  6.0]
2979  06:15:17.335    16    [ 4.5, -4.9, -6.0, -6.0,  6.0]
3009  06:15:18.509    18    [ 4.5, -4.9, -6.0, -6.0,  6.0]   SAVE (Mid cycle 2)
3064  06:15:31.073    16    [ 4.5, -5.2, -6.0, -6.0,  6.0]
3068  06:15:31.226    16    [ 4.5, -6.0, -6.0, -6.0,  6.0]
3083  06:15:33.008    18    [ 4.5, -6.0, -6.0, -6.0,  6.0]   SAVE (Bass cycle 1)
3130  06:15:44.756    16    [ 4.5,  6.0, -6.0, -6.0,  6.0]
3142  06:15:46.176    18    [ 4.5,  6.0, -6.0, -6.0,  6.0]   SAVE (Bass cycle 2)
3178  06:15:58.201    16    [-6.0,  6.0, -6.0, -6.0,  6.0]
3187  06:16:00.104    18    [-6.0,  6.0, -6.0, -6.0,  6.0]   SAVE (Low bass cycle 1)
3215  06:16:10.682    16    [ 5.8,  6.0, -6.0, -6.0,  6.0]
3228  06:16:12.770    18    [ 5.8,  6.0, -6.0, -6.0,  6.0]   SAVE (Low bass cycle 2)
3271  06:16:23.313    16    [ 5.8,  6.0, -6.0, -6.0,  0.2]
3285  06:16:24.832    18    [ 5.8,  6.0, -6.0, -6.0,  0.2]   SAVE (Upper treble cycle 3)
3318  06:16:33.690    16    [ 5.8,  6.0, -6.0,  0.2,  0.2]
3328  06:16:34.846    18    [ 5.8,  6.0, -6.0,  0.2,  0.2]   SAVE (Treble cycle 3)
3373  06:16:45.354    16    [ 5.8,  6.0,  0.3,  0.2,  0.2]
3382  06:16:46.563    18    [ 5.8,  6.0,  0.3,  0.2,  0.2]   SAVE (Mid cycle 3)
3403  06:16:56.484    16    [ 5.8,  0.0,  0.3,  0.2,  0.2]
3428  06:16:57.794    18    [ 5.8,  0.0,  0.3,  0.2,  0.2]   SAVE (Bass cycle 3)
3451  06:17:07.602    16    [ 0.2,  0.0,  0.3,  0.2,  0.2]
3458  06:17:07.750    16    [ 0.1,  0.0,  0.3,  0.2,  0.2]
3468  06:17:09.029    18    [ 0.1,  0.0,  0.3,  0.2,  0.2]   SAVE (Low bass cycle 3)
```

**🟢 FACT — exactly one quintet element changes between any two consecutive frames, for all 50
slider-related frames.** Checked exhaustively (not sampled) — this is the strongest form of the
isolation this project's methodology asks for (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4): every single
wire transition in this ~4-minute stretch is attributable to exactly one on-screen slider moving,
with zero ambiguous multi-field transitions.

**Reproduction script** (HDLC decode, CRC check, tag/length assertions, full quintet extraction —
self-contained, run against the raw hex list saved alongside this analysis):

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

def decode(hx):
    raw = bytes.fromhex(hx)
    un = unescape_hdlc(raw[1:-1])
    body, trailer = un[:-4], un[-4:]
    assert struct.pack('<I', binascii.crc32(body) & 0xffffffff) == trailer, "CRC mismatch"
    addr, i = leb128(body, 0); ctrl = body[i]; i += 1
    payload = body[i:]
    assert payload[14] == len(payload) - 15
    assert payload[16] == len(payload) - 17
    outer_tag, oi = leb128(payload, 17)
    bands = []
    off = 20
    for _ in range(5):
        bands.append(struct.unpack('<f', payload[off+1:off+5])[0])
        off += 5
    return addr, ctrl, outer_tag >> 3, bands

# Example: verify the "Heavy bass" preset (frame 2165) against the earlier CAP-005's independent decode
addr, ctrl, field, bands = decode("7e003b0310131dea71de7d5e251d9a8c9e2a1e221c8201190d0000a04015000040401d0000000025000000002d000000007b1bccbe7e")
assert bands == [5.0, 3.0, 0.0, 0.0, 0.0]
print("OK:", addr, hex(ctrl), field, bands)
```

Output: `OK: 0 0x3b 16 [5.0, 3.0, 0.0, 0.0, 0.0]`

## 5. Field-to-band mapping — 🟢 FACT (promoted from the earlier capture's 🟡 HYPOTHESIS)

The earlier (2026-08-15) `CAP-005-FINDINGS.md` §5b/§6 left this as an explicit open question:
*"Field order (1↔Low-bass, 2↔Bass, 3↔Mid, 4↔Treble, 5↔Upper-treble) is inferred purely from which
single value changed correlating with which single slider the user touched — not independently
confirmed by decoding a second field's change, since only Bass was moved this session."*

**This capture directly closes that gap.** §4's table shows the quintet's 5 elements change in a
strict, repeating order — index 4, then 3, then 2, then 1, then 0 — across three full passes, and
§1's video review directly ties 4 of the 5 indices to a specific on-screen slider by finger
position and (for index 4) an on-screen value tooltip:

| Quintet index | Band | Evidence |
|---|---|---|
| 4 (last field, tag `0x2d`) | **Upper treble** | 🟢 Direct: video shows finger on the Upper treble slider with a `-60` tooltip exactly as this field drops to `-6.0` (frame 2573→2586) |
| 3 (tag `0x25`) | **Treble** | 🟢 By elimination + structural consistency: always the 2nd field to change in every one of the 3 passes, matching Treble's position (2nd from top) in the fixed on-screen slider order confirmed in §1; a supporting (not fully clean) video frame at 06:16:35 shows Treble's bar already at its just-changed value while Bass's is still untouched |
| 2 (tag `0x1d`) | **Mid** | 🟢 Direct: video (t=239s/06:15:05) shows the finger directly over the "Mid" row exactly as this field starts changing |
| 1 (tag `0x15`) | **Bass** | 🟢 Direct: video (t=266s/06:15:32) shows the finger on the "Bass" row mid-drag, `Save` button active, shortly before this field's cycle-1 save (frame 3083, 06:15:33.008) |
| 0 (first field, tag `0x0d`) | **Low bass** | 🟢 Direct: video (t=293s/06:15:59) shows the finger at the "Low bass" row exactly as this field starts changing |

This also matches, exactly, the earlier `CAP-005-FINDINGS.md` §5b's inferred mapping (field
1↔Low bass … field 5↔Upper treble) — an independent capture, five days apart, landing on the
identical field order. Combined with this capture's own internal repetition (the same order,
independently, 3 times) and the direct video ties for 4/5 fields, this clears
`PROJECT_RULES.md` §1's promotion bar (byte-for-byte reproducible decode, cross-capture agreement,
plus multiple independent within-capture confirmations) — **promoted to 🟢 FACT.**

**Note on UI-to-wire ordering:** the wire quintet is in the *reverse* of the on-screen top-to-bottom
slider order (UI shows Upper treble first/top; wire puts it last/field 5). This is worth keeping in
mind for `FrameEncoder`/`FrameDecoder` implementation — do not assume the two orders match without
an explicit re-index.

## 6. Open Questions

- 🔴 **Untraced value jump before the first slider drag.** Between the Clarity preset tap (frame
  2400, 06:13:26.064, clean quintet `[-2.0, 0.0, 2.0, 3.0, 5.0]`) and the first Upper-treble drag
  frame (2470, 06:13:40.998, quintet `[4.5, -4.9, 4.5, 3.8, 4.1]`), **fields 0–3 all changed** with
  no intervening `Sent` frame on DLCI 0x02 to explain it (checked exhaustively — only one `Rcvd`
  frame, 2456 at 06:13:36.700, appears in that window, and it has a structurally different payload
  shape that doesn't match the standard quintet envelope at all — see raw hex below, not decoded
  further here). The video shows the dropdown briefly reopened at 06:13:40 with `Clarity` still
  checked, then the user grabs the Upper treble slider directly — consistent with the user tapping
  **`Last saved`** in the dropdown (recalling an existing custom profile from *before this session*,
  e.g. a leftover state from an earlier test day) immediately before starting the drag, fast enough
  that the two actions landed in the same outgoing wire frame — but this is not confirmed; no
  isolated "tap Last saved with nothing else touched" sample exists in this capture to test it
  directly. **Frame 2456 raw hex** (flagged, not decoded):
  `7e80a3032a1f1090c4cc9881341800320c1204086410011a04086410013a06080010001800080710131dea71de7d5e2590821ee6e9e6be187e`
  — Rcvd direction, structurally distinct from every other frame in this capture (different tag
  byte after the `2a`, nested small-int fields resembling `08 64 10 01`/`08 64 10 01` repeated,
  possibly a preset-list or capability response, not a quintet write).
- 🔴 **"Save" (outer field 18) trigger mechanism refined, not fully resolved.** The earlier capture
  hypothesized field 18 = an explicit tap of the on-screen `Save` button, roughly 5s after the
  matching field-16 preview. In **this** capture, field 18 fires **0.05–1.9s** after the last
  field-16 preview in every one of the 15 save cycles (§4's table) — far too fast for a deliberate,
  separate tap on a UI element in a different screen region, and no video frame shows a distinct
  `Save`-button tap between drag and save for any of the 15 cycles. **Revised 🟡 HYPOTHESIS:**
  field 18 fires on **slider-release** (finger lift), not on a separate `Save` button tap — the
  `Save` button's own purpose may be to persist the *whole custom profile* as the account's
  `Last saved` preset for future recall, a separate/higher-level action from the per-band field-18
  writes. Not confirmed either way — would need a capture that drags a slider, releases, and
  explicitly does **not** ever tap `Save`, to see whether field-18 frames still appear.
- 🔴 **Units of the ±6.0 range not independently confirmed** — could be dB, could be an arbitrary
  UI-defined scale that happens to display as dB-like slider positions. Not testable from wire
  data alone.
- 🔴 **HDLC Control byte (`0x3b` for every `Sent` frame in this capture) still uninterpreted** —
  same open item as the earlier capture and `PROTOCOL.md` §2.2a; out of scope here.
- ✅ **Resolved 2026-08-18 (this capture):** the earlier `CAP-005-FINDINGS.md` §6's top open item —
  "field-to-band mapping is inferred from a single changed field, not confirmed" — is now closed,
  see §5 above.

## 7. Promotion readiness

**Ready for `PROTOCOL.md` §4.2 (recommended next step, not applied in this pass — out of this
session's stated scope):**
- The field-to-band mapping (§5) should be promoted from 🟡 HYPOTHESIS to 🟢 FACT — Low bass↔field
  1, Bass↔field 2, Mid↔field 3, Treble↔field 4, Upper treble↔field 5, wire order reversed from the
  on-screen top-to-bottom order.
- The ±6.0 band-gain range (§4) is new information not previously in `PROTOCOL.md` — worth adding
  as 🟢 FACT (range) / 🔴 open (units).
- The 6 presets' exact quintets (§3) are a reusable reference table for anyone implementing preset
  recall.
- Outer field 16 ("preview") vs. 18 ("save") — keep as 🟡 HYPOTHESIS, but note the revised
  "save = slider release" reading (§6) supersedes the earlier "save = explicit Save-button tap"
  reading; neither is fully confirmed.

**Not ready:** the exact meaning of the Control byte, the untraced baseline-jump anomaly (§6), and
frame 2456's distinct payload shape — all still 🔴.
