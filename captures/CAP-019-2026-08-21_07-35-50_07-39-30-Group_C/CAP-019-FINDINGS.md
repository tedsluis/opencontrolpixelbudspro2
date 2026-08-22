# CAP-019: Conversation Detection & Multipoint Toggles (Group C)

Standardized, evidence-based extraction from `CAP-019-btsnoop_hci.log` + `CAP-019-recording.mp4`,
staged here per `PROJECT_RULES.md` §2. Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

## 1. Capture Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-019` |
| Purpose | Main run-through Group C — attribute the wire commands for the "Conversation detection" and "Multipoint" toggles |
| Date | 2026-08-21 |
| Firmware | not queried this session — ⚪ ASSUMPTION `release_5.203` |
| Test device | Pixel 7a, Android 17 (⚪ ASSUMPTION, not re-confirmed on screen), official Pixel Buds Companion App (version not visible on screen) |
| Log file | [`CAP-019-btsnoop_hci.log`](./CAP-019-btsnoop_hci.log) — 401.6s, 2026-08-21 07:34:41.723–07:41:23.329 (+0200) |
| Notes file | [`CAP-019-EVENT-NOTES.md`](./CAP-019-EVENT-NOTES.md) |
| Video file | [`CAP-019-recording.mp4`](./CAP-019-recording.mp4) — 220.5s, 07:35:50–07:39:30 local time |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` — same physical device as `CAP-016`/`CAP-020` |

## 2. Methodology & Filtering

Video reviewed frame-by-frame (1–2fps tiled contact sheets for the full pass; sub-second
single-frame extraction around both toggles) against its own burned-in wall-clock overlay; anchor
calibrated directly (`t=0s → 07:35:50`, `t=51s → 07:36:41`, `t=130s → 07:38:00` — consistent 1:1
linear mapping, no drift). RFCOMM traffic isolated with:

```
$ tshark -r CAP-019-btsnoop_hci.log -Y "btrfcomm.len>0 and frame.time_epoch>X and frame.time_epoch<Y" \
    -T fields -e frame.number -e frame.time -e btrfcomm.dlci -e btrfcomm.direction -e data.data
```

DLCI distribution for this session (`btrfcomm.dlci`, all frames): `0x00`×32, `0x02`×252, `0x04`×90,
`0x08`×76, `0x0a`×2, `0x0c`×47 — same DLCI set as `CAP-020`.

**🟢 FACT — DLCI 0x0c is the standard Hands-Free Profile (HFP) SLC setup, not custom app traffic.**
Hex-dumping this DLCI's frames:

```
$ tshark -r CAP-019-btsnoop_hci.log -Y "btrfcomm.dlci==0x0c" -x
...
0000  02 02 20 15 00 11 00 43 00 31 ff 19 03 41 54 2b   .. ....C.1...AT+
0010  42 52 53 46 3d 39 32 31 0d 89                     BRSF=921..
...
0000  02 02 20 16 00 12 00 43 00 31 ff 1b 02 41 54 2b   .. ....C.1...AT+
0010  42 41 43 3d 31 2c 32 2c 33 0d 89                  BAC=1,2,3..
...
0000  02 02 20 13 00 0f 00 43 00 31 ff 15 01 41 54 2b   .. ....C.1...AT+
0010  43 49 4e 44 3d 3f 0d 89                           CIND=?..
```

confirms standard ASCII AT commands (`AT+BRSF=921`, `AT+BAC=1,2,3`, `AT+CIND=?`, followed later by
`AT+CIND?`/`AT+CMER=3,0,0,1`/`AT+BIND=1,2`/`AT+BIND=?` and their `OK`/`+CIND:`/`+BIND:` responses,
not reproduced in full here) — this is HFP's standard Service Level Connection handshake, not a
custom settings-toggle channel. Rules this DLCI out as carrying app-specific behavior; not
investigated further.

## 3. Analysis: `CONV-001` (Conversation detection OFF→ON)

**Location correction against the planned-row assumption:** Conversation detection is not a
top-level Device-details item — it is under **Device details → Sound → Audio intelligence**.

Video: toggle OFF at t=44–50s, finger tap at t=51s (07:36:41), confirmed ON (purple, checkmark)
by t=52s (07:36:42).

One `Sent`-direction (ctrl `0x4b`) DLCI 0x02 frame lands in this exact window — frame **1808**,
`07:36:40.238522` (~0.8s before the video-visible tap, well within this project's established
timing-heuristic tolerance, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 step 3):

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

raw = bytes.fromhex("7e004b0310151dea71de7d5e251d9a8c9e2a052203b00101b46fc4c07e")
un = unescape_hdlc(raw[1:-1])
body, trailer = un[:-4], un[-4:]
assert struct.pack('<I', binascii.crc32(body) & 0xffffffff) == trailer   # CRC-32 verified
addr, i = leb128(body, 0); ctrl = body[i]; i += 1
payload = body[i:]
print(hex(addr), hex(ctrl), payload.hex())
# -> 0x0 0x4b 0310151dea71de7e251d9a8c9e2a052203b00101
```

Decoded from offset 13: `field=5 wt=LD len=5 { field=4 wt=LD len=3 { field=22 wt=varint value=1 } }`.

Rcvd echo: frames 1812 (07:36:40.322594), 1813 (07:36:40.323574) — same shape as every other
DLCI 0x02 write's echo in this project (`CAP-020-FINDINGS.md` §3/§4).

**Status:** 🟡 **HYPOTHESIS** — frame 1808 is the `CONV-001` command, on tight timing correlation
and a verified CRC-32/protobuf-tag decode. Single capture, no official spec coverage.

## 4. Analysis: `MULTI-001` (Multipoint OFF→ON)

Video: toggle OFF through t=128s, finger taps the toggle row starting ~t=129s, on-screen transition
visible at t≈132s, confirmed ON (purple, checkmark) by t=133s (07:38:03).

The actual `Sent`-direction (ctrl `0x4b`) DLCI 0x02 write is frame **2293**, `07:38:01.609130`
(07:38:01.6 — this refines the video-only estimate of "~129–130s/07:37:59–08:00" to the precise
wire timestamp, still within the same tap-to-confirm window):

```
raw = "7e004b0310151dea71de7d5e251d9a8c9e2a04220258013b536cdb7e"
# -> addr=0x0 ctrl=0x4b payload=0310151dea71de7e251d9a8c9e2a0422025801
# [off 13] field=5 wt=LD len=4 { [off 15] field=4 wt=LD len=2 { [off 17] field=11 wt=varint value=1 } }
```

CRC-32 verified. Rcvd echo: frame 2295 (07:38:01.640548).

**New this capture — Multipoint additionally triggers a Fast Pair Message Stream burst on DLCI
0x04, Group `0x07`** (identified in `PROTOCOL.md` §2.3's table as SASS, previously never correlated
to a specific user action): frames 2296–2319, `07:38:01.641`–`07:38:02.047`, immediately following
the DLCI 0x02 write:

```
2296  07:38:01.641616  07 11 00 04 01 02 b8 00
2297  07:38:01.644327  07 34 00 0c 01 e7 9f 73 c7 a8 65 3e 05 10 d5 58
2301  07:38:01.678562  07 21 00 00
2302  07:38:01.686762  07 41 00 16 69 6e 2d 75 73 65 7e 8a 13 d5 15 28 03 10 dd 34 ae b2 93 14 5f ea
2303  07:38:01.687522  07 40 00 11 00 64 f7 92 56 19 25 bd 33 db df 1e e3 e4 dd 86 22
2304  07:38:01.692811  07 42 00 11 00 26 da d8 67 95 fb 82 58 ab c0 8e 15 71 00 e4 fc
2309  07:38:01.827519  ff 01 00 08 07 41 69 6e 2d 75 73 65     # ACK for Code 0x41
2315  07:38:01.955553  ff 01 00 03 07 40 00                    # ACK for Code 0x40
2316  07:38:01.956370  ff 01 00 03 07 42 00                    # ACK for Code 0x42
2319  07:38:02.046941  07 34 00 0c 01 0c 34 07 bd ba 3c f7 9a 7d 42 9b
```

Read as the official Message Stream envelope (`PROTOCOL.md` §2.1: `[Group:1][Code:1][Len:2BE][Value]`),
Code `0x41`'s value starts with ASCII `69 6e 2d 75 73 65` = **`"in-use"`**. Code `0x34` (len 12,
opaque) recurs a 3rd time at 07:38:06.191 (frame 2326) outside any action window this session,
alongside the same DLCI 0x08 one-time-capability-shaped burst seen at connection open
(`CAP-004-FINDINGS.md` §5a) — 🟡 HYPOTHESIS: Code `0x34` is a periodic/keepalive SASS code, not
Multipoint-specific, while Codes `0x11`/`0x21`/`0x40`/`0x41`/`0x42` appear **only** in this one
Multipoint-triggered burst and nowhere else in the session — 🟡 HYPOTHESIS (stronger): these five
codes are genuinely Multipoint-triggered SASS negotiation, not coincidental.

**Directly confirms `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group C's own hint** ("Multipoint may trigger
an SDP/connection update, not just an RFCOMM command") — this is the first capture to correlate
DLCI 0x04 Group `0x07` (SASS) content with a specific triggering user action.

**Checked and ruled out:** DLCI 0x04's `08 13 00 04 01 e8 e8 80` (frame 2277, 07:38:00.916, ~0.7s
before the Multipoint write) is the already-documented periodic "Notify ANC state" report
(`PROTOCOL.md` §4.1, Code `0x13`) — coincidental timing, not Multipoint-specific.

**Status:** 🟡 **HYPOTHESIS** for both the DLCI 0x02 write (frame 2293) and the DLCI 0x04 Group
`0x07` correlation (frames 2296–2319) — single capture, no official SASS extension page consulted
this session to confirm Code semantics.

## 5. Cross-command structural comparison

Extends `CAP-020-FINDINGS.md` §5's table with two more settings on the same DLCI 0x02
`field 5{ field 4{ <field>=<value> } }` outer wrapper:

| Setting | Inner field # | Value | Capture |
|---|---|---|---|
| Touch controls ON | 4 | 1 | `CAP-020` |
| Head gestures ON | 29 | 2 | `CAP-020` |
| Conversation detection ON | 22 | 1 | `CAP-019` |
| Multipoint ON | 11 | 1 | `CAP-019` |

Four different settings, four different inner field numbers, three sharing value `1` and one using
value `2` — 🟡 HYPOTHESIS (strengthened by a 4th and 5th data point, still not 🟢 FACT): the outer
wrapper is a general-purpose `libmaestro` settings-write envelope, with each setting owning its own
inner field number. **Not confirmed:** whether the field number is a stable per-setting identifier
across firmware versions/sessions, or whether value `2` for head gestures specifically means
something beyond "enabled" (e.g. a sub-mode). No second OFF cycle was captured for any of these
four settings yet.

## 6. Conclusions & Next Steps

- Both `CONV-001` and `MULTI-001` isolate cleanly to a single DLCI 0x02 `Sent` frame, verified
  CRC-32, within ~1s of the video-confirmed action — 🟡 HYPOTHESIS level.
- **New, higher-confidence finding:** Multipoint's DLCI 0x02 write is immediately followed by a
  DLCI 0x04 Group `0x07` (SASS) negotiation burst containing an ASCII `"in-use"` string — the first
  content-level, action-correlated data for this previously only structurally-identified Message
  Stream group.
- **Recommended next step:** toggling Multipoint back OFF in a future capture would confirm whether
  the SASS burst reverses/differs, and whether DLCI 0x02's `field 11` value flips to `0`.

## 7. Open Questions

- 🔴 What do DLCI 0x02 inner field numbers 4/11/22/29 (and the `1`/`2` values) actually encode —
  stable per-setting IDs, or something else? → copied to `PROTOCOL.md` §6.
- 🔴 What do SASS (DLCI 0x04 Group `0x07`) Codes `0x11`/`0x21`/`0x40`/`0x42` encode beyond their
  raw bytes — no official spec page consulted this session? → copied to `PROTOCOL.md` §6.
- 🔴 Is Group `0x07` Code `0x34` a periodic/keepalive SASS code unrelated to Multipoint, or does it
  have its own trigger not yet identified? → copied to `PROTOCOL.md` §6.
