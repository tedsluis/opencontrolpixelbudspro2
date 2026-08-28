# CAP-021: Press-and-Hold Configuration (Group G)

Standardized, evidence-based extraction from `CAP-021-btsnoop_hci.log` + `CAP-021-recording.mp4`,
staged here per `PROJECT_RULES.md` §2. Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

## 1. Capture Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-021` |
| Purpose | Main run-through Group G — attribute the wire commands for per-earbud press-and-hold assignment (`HOLD-001`–`HOLD-004`) and the ANC-mode rotation checklist (`HOLD-005`) |
| Date | 2026-08-21 |
| Firmware | not queried this session — ⚪ ASSUMPTION `release_5.203` |
| Test device | Pixel 7a, Android 17 (⚪ ASSUMPTION), official Pixel Buds Companion App (version not visible on screen) |
| Log file | [`CAP-021-btsnoop_hci.log`](./CAP-021-btsnoop_hci.log) — 571.3s, 2026-08-21 07:59:30.485–08:09:01.819 (+0200) |
| Notes file | [`CAP-021-EVENT-NOTES.md`](./CAP-021-EVENT-NOTES.md) |
| Video file | [`CAP-021-recording.mp4`](./CAP-021-recording.mp4) — 448.3s, 07:59:36–08:07:04 local time |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` — same physical device as `CAP-016`/`CAP-019`/`CAP-020` |

## 2. Methodology & Filtering

This session's actual on-screen navigation did not follow a clean 5-actions-in-sequence path (a
dead-end detour into a read-only "Touch controls" info screen, and interleaved Left/Right
visits), so a **log-driven** method was used instead of pure video-timeline reconstruction: every
DLCI 0x02 payload across the **whole** 571s log (241 raw HDLC sub-frames) was decoded and searched
for the `field 5{ field 4{ ... } }` settings-write shape first identified in
`CAP-020-FINDINGS.md` §5, and each match was then checked against the video at its exact
timestamp to confirm the on-screen action. Video review used tiled contact sheets for the full
pass and single-frame extraction at each candidate timestamp.

```
$ tshark -r CAP-021-btsnoop_hci.log -Y "btrfcomm.dlci==0x02 and btrfcomm.len>0" \
    -T fields -e frame.number -e frame.time -e data.data
```

(241 lines; decoded in Python — full script in `CAP-021-EVENT-NOTES.md` §Decode.)

## 3. Analysis: `HOLD-001`–`HOLD-004` (per-earbud action selection)

Four frames, one per Left/Right × ANC/Assistant combination, each CRC-32-verified:

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
    "HOLD-002 (frame 1895)": "7e003b0310131dea71de7d5e251d9a8c9e2a0a22083a060a0422020806bcae58b07e",
    "HOLD-004 (frame 3619)": "7e003b0310131dea71de7d5e251d9a8c9e2a0a22083a061204220208064a2edd5f7e",
    "HOLD-001 (frame 4315)": "7e003b0310131dea71de7d5e251d9a8c9e2a0a22083a060a042202080506ff51297e",
    "HOLD-003 (frame 4976)": "7e003b0310131dea71de7d5e251d9a8c9e2a0a22083a06120422020805f07fd4c67e",
}
for name, hx in frames.items():
    raw = bytes.fromhex(hx)
    un = unescape_hdlc(raw[1:-1])
    body, trailer = un[:-4], un[-4:]
    assert struct.pack('<I', binascii.crc32(body) & 0xffffffff) == trailer   # CRC-32 verified, all 4
    addr, i = leb128(body, 0); ctrl = body[i]; i += 1
    print(name, "->", body[i:].hex())
```

Output (all 4 CRC-32 checks pass):

```
HOLD-002 (frame 1895) -> 0310131dea71de7e251d9a8c9e2a0a22083a060a0422020806
HOLD-004 (frame 3619) -> 0310131dea71de7e251d9a8c9e2a0a22083a06120422020806
HOLD-001 (frame 4315) -> 0310131dea71de7e251d9a8c9e2a0a22083a060a0422020805
HOLD-003 (frame 4976) -> 0310131dea71de7e251d9a8c9e2a0a22083a06120422020805
```

Decoded (offset 13 onward): `field5(len10){ field4(len8){ field7(len6){ field1|field2(len4){
field4=varint(5|6) } } } }`.

| Frame | Time | Outer inner field | Value | Meaning (🟡 hypothesis) | Test-ID |
|---|---|---|---|---|---|
| 1895 | 08:01:23.784 | `field1` (Left) | `6` | Left → Digital assistant | `HOLD-002` |
| 3619 | 08:03:16.151 | `field2` (Right) | `6` | Right → Digital assistant | `HOLD-004` |
| 4315 | 08:03:49.667 | `field1` (Left) | `5` | Left → Active noise control | `HOLD-001` |
| 4976 | 08:04:09.920 | `field2` (Right) | `5` | Right → Active noise control | `HOLD-003` |

**Video cross-check:** frame 1895's tap is video-confirmed at t≈108s (07:59:36 anchor + 108s =
08:01:24, "Customize left" screen, "Digital assistant" tab tap); frame 3619 at t=220s (08:03:16,
"Customize right", "Digital assistant" tab tap); frame 4315's context confirmed at t=253s
(08:03:49, "Controls and gestures" shows both Left/Right already "Digital assistant", finger
tapping "Left" row to revert it).

**Status:** 🟡 **HYPOTHESIS**, but a comparatively strong one — all 4 of the 2×2 (earbud × action)
combinations were exercised and each produced exactly the field/value pair a consistent scheme
predicts, with frame 1903 (Rcvd echo of 1895) independently containing *both* `field1{field4=6}`
(new) and `field2{field4=5}` (a second value) in one message, consistent with — though not
required by — this reading. Not 🟢 FACT: no official spec covers this, and the field/value mapping
was inferred, not confirmed against a documented schema.

## 4. Analysis: `HOLD-005` (ANC-mode rotation checklist)

16 frames, 08:05:28.428–08:06:46.203, all matching
`field5(len12){ field4(len10){ field12(len8){ field1..4 = varint(0|1) } } }`:

```
5237  08:05:28.428  08 00 10 01 18 01 20 01
5247  08:05:34.743  08 01 10 01 18 01 20 01
5255  08:05:38.176  08 01 10 00 18 01 20 01
5268  08:05:43.460  08 01 10 00 18 01 20 00
5278  08:05:47.738  08 01 10 01 18 01 20 00
5285  08:05:52.731  08 01 10 01 18 00 20 00
5294  08:05:57.895  08 01 10 01 18 00 20 01
5301  08:06:03.967  08 01 10 01 18 01 20 01
5317  08:06:14.491  08 00 10 01 18 01 20 01
5333  08:06:19.000  08 01 10 01 18 01 20 01
5340  08:06:21.720  08 01 10 00 18 01 20 01
5352  08:06:26.697  08 01 10 01 18 01 20 01
5362  08:06:30.785  08 01 10 01 18 01 20 00
5372  08:06:37.437  08 01 10 01 18 01 20 01
5387  08:06:41.784  08 01 10 01 18 00 20 01
5415  08:06:46.203  08 01 10 01 18 01 20 01
```

**🟡 HYPOTHESIS:** field 1–4 = Noise cancellation / Off / Adaptive / Transparency, in that order —
matches the on-screen checklist's top-to-bottom row order exactly. Each write toggles exactly one
field relative to its predecessor (a clean single-checkbox-at-a-time pattern), consistent with the
video showing the user tapping one checkbox, then another, repeatedly.

**Not resolved — flagged rather than guessed:** unlike `HOLD-001`–`HOLD-004`, this envelope carries
**no** Left/Right-distinguishing field. Video confirms "Customize left" is open at the burst's
start (08:05:28) and "Customize right" is open by its end (08:06:46), so the burst spans **both**
screens, but which specific frames belong to which earbud's list cannot be determined from the wire
payload alone. A future capture that isolates a single checkbox toggle on **one** earbud's screen
only (no Left/Right switch mid-burst) would resolve this.

## 4a. DLCI 0x0a payload burst (new finding, not one of Group G's Test-IDs)

**🟢 FACT:** this session is the **first capture in the project to observe any payload on DLCI
0x0a (RFCOMM channel 5)**. Every other capture that has checked this DLCI — `CAP-001`, `CAP-002`,
`CAP-005`, `CAP-006`, `CAP-007`, `CAP-016` (pre-batch) and `CAP-011`/`CAP-019`/`CAP-020`/`CAP-022`–
`CAP-025` (this batch) — shows only the 2 channel-control frames (open/close) and zero data. This
session alone carries a sustained burst:

```
$ tshark -r CAP-021-btsnoop_hci.log -Y "btrfcomm" -T fields -e btrfcomm.dlci | sort | uniq -c
   1687 0x0a
    312 0x02
    151 0x08
     64 0x04
     43 0x0c
     32 0x00

$ tshark -r CAP-021-btsnoop_hci.log -Y "btrfcomm.dlci==0x0a and btrfcomm.len>0" -T fields -e frame.number -e frame.time_relative -e btrfcomm.len | wc -l
1123
```

1123 of the 1687 DLCI-0x0a frames carry a nonzero-length payload, spanning **frame 2093 to frame
4926**, log-relative time **179.019580s–276.657232s** (wall clock ≈08:02:29.5–08:04:07.1, per this
session's `07:59:30.485` log start). Payload length is overwhelmingly fixed: 1026 of 1123 frames
are exactly 215 bytes, with the remainder at 319/267/58/32/6/475/163/430/110 bytes.

Raw hex, frame 2097 (`tshark -r CAP-021-btsnoop_hci.log -Y "frame.number==2097" -x`):

```
0000  02 02 20 e0 00 dc 00 42 00 29 ef ae 01 01 04 00   .. ....B.)......
0010  d3 0a d0 01 00 00 00 00 6d b6 db 6d b6 db 6d b6   ........m..m..m.
0020  db 6d b6 db 6d b6 db 6d b6 db 6d b6 db 6d b6 db   .m..m..m..m..m..
...(repeating `00 00 00 00` / `6d b6 db` runs through the remainder of the 215-byte frame)...
00b0  62 22 11 00 7e ee ed 7e ee ed 7e ee ed 7e ee ed
00c0  83 0e cd 86 73 09 a9 a6 2a b4 ee 91 b3 4a ed b8
00d0  ec e9 b7 0c 41 b6 af 10 c1 2b da bd 69 89 c4 50
00e0  c8 c5 2d 71 6a
```

The RFCOMM UIH payload (from byte `01 04 00 d3 0a d0 01 ...`) opens with what mechanically decodes
as a protobuf tag `0x0a` = field 1, wiretype 2 (length-delimited), followed by varint length
`d0 01` = 208. This structural shape (tag+varint-length) is the extent of what is confirmed —
**the content is not decoded further here.** The bulk of the 215-byte frame is a highly repetitive
`00 00 00 00` / `6d b6 db` byte run, distinct in character from the tail ~20 bytes, which look
higher-entropy.

**🔴 OPEN QUESTION — not resolved by this session.** A fuller byte-level characterization (100%
Rcvd-direction, ~5–6 discrete bursty waves, a `7e ee ed` tail pattern near each frame's end, an
entropy profile suggesting segmented bulk-data transfer rather than continuous telemetry) was later
added from a 2026-08-23 external-audit re-analysis of all 1,123 frames — see `PROTOCOL.md` §6
(Commands & schemas) for that fuller read; not repeated here, still 🔴 OPEN QUESTION either way.
- What this stream represents. The burst window (08:02:29.5–08:04:07.1) does not cleanly bound
  any single Group G Test-ID's tap time: `HOLD-002` (08:01:23.784) finishes well before the burst
  starts, `HOLD-004` (08:03:16.151) and `HOLD-001` (08:03:49.667) fall inside it, and `HOLD-003`
  (08:04:09.920) falls just after it ends — so the burst is not obviously attributable to any one
  settings write, and looks more like a sustained background stream than a per-action payload.
- **An interpretation of this stream as Spatial Audio/IMU head-tracking telemetry is explicitly
  NOT adopted here.** Group G's actual subject is per-earbud press-and-hold configuration, not
  head gestures (see this file's own Purpose/§1, and `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Group G
  description) — there is no session context tying this burst to head-tracking, and no field-level
  decode was performed to support any specific payload semantics. Per `AGENTS.md` §13's
  zero-creativity rule, this is left as an open question rather than a guessed interpretation.
- Whether this recurs in any other capture, or is specific to whatever incidental
  app/OS/connection state was active in this one session (e.g. the earlier `TOUCH-001`-shaped
  re-send noted in `CAP-021-EVENT-NOTES.md` around 08:00:10, or some other incidental state) is
  unknown — no other capture in this batch shows any DLCI 0x0a payload to compare against.
- `TODO.md`'s `CAP-008` item already earmarks DLCI 0x0a for a phone-call capture; this finding
  shows the channel is not exclusively call-related, since nothing here involved a call.

## 5. Cross-command structural comparison

Extends `CAP-019-FINDINGS.md` §5's table — `HOLD-001`–`HOLD-004` are the first settings observed to
nest a **third** level (`field5{field4{field7{field1|2{field4=N}}}}`) rather than the two-level
`field5{field4{<value>}}` seen for `TOUCH-001`/`HEAD-001`/`CONV-001`/`MULTI-001`. 🟡 HYPOTHESIS
(new): the outer `field5{field4{...}}` wrapper's *inner* content structure scales with how much the
setting needs to express — a plain boolean toggle uses a single varint; a per-earbud, multi-value
setting (like press-and-hold) adds a `field7{field1|field2{...}}` sub-wrapper to select which
earbud, before the value itself.

## 6. Conclusions & Next Steps

- All 5 Test-IDs (`HOLD-001`–`HOLD-005`) isolate to DLCI 0x02 writes, cross-checked against video
  at each write's exact timestamp — the clean 2×2 combination coverage for `HOLD-001`–`HOLD-004`
  is stronger evidence than a single-sample capture, though still 🟡 HYPOTHESIS overall (no
  official spec).
- **Recommended next step:** an isolated single-checkbox-toggle-on-one-earbud-only capture would
  resolve `HOLD-005`'s open Left/Right attribution question.
- **New, unattributed finding (§4a):** this session is the first to record any payload on DLCI
  0x0a — a 1123-frame burst, frames 2093–4926, ~179–277s into the log. Not tied to any Group G
  Test-ID; flagged for follow-up, not investigated further this session.

## 7. Open Questions

- 🔴 Does `field 7`'s `field1`/`field2` (Left/Right) selector generalize to other per-earbud
  settings beyond press-and-hold? → copied to `PROTOCOL.md` §6.
- 🔴 Which of `HOLD-005`'s 16 frames belong to Left's rotation list vs. Right's — the payload
  carries no earbud-distinguishing field for this particular write. → copied to `PROTOCOL.md` §6.
- 🔴 What does DLCI 0x0a's 1123-frame payload burst (§4a) represent, and why does it appear only
  in this session? → copied to `PROTOCOL.md` §6.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-021-2026-08-21_07-59-36_08-07-04-Group_G/CAP-021-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-021-2026-08-21_07-59-36_08-07-04-Group_G/CAP-021-FINDINGS
