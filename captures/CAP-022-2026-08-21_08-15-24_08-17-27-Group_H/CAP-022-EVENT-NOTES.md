# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group H, Audio & volume settings (`CAP-022`)

**Status:** ✅ Captured and analyzed. Video reviewed via tiled contact sheets (full pass) and
targeted single-frame extraction; the wire log was then searched for every DLCI 0x02
`field5{field4{...}}`-shaped write across the whole session (per the method established in
`CAP-021-FINDINGS.md` §2) and cross-checked against the video timestamps.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group H):** attribute the wire commands for Mono
audio, Volume EQ, and Volume balance.

**Location note:** all three settings live on **Device details → Sound**, except Volume EQ, which
is actually at the **bottom of the "Equalizer" sub-page** (reached by tapping "Equalizer" on the
Sound screen), not on the Sound page itself as the skeleton assumed.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-022`                     |
|      Group(s)    |                         H                          |
|       Date       |                     2026-08-21                     |
| Firmware version |    not queried this session (⚪ ASSUMPTION `release_5.203`) |
|   Test device    | Pixel 7a, Android 17 (⚪ ASSUMPTION), official Pixel Buds Companion App (version not visible on screen) |
| Video file       |            `CAP-022-recording.mp4` (123.1s, 08:15:24–08:17:27 local time) |
| Log file         |             `CAP-022-btsnoop_hci.log` (08:15:05.529–08:19:28.725) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` — same physical device as `CAP-016`/`CAP-019`–`CAP-021` |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group H)

28. **Toggle 'Mono audio' on/off** [`AUDIO-001`].
29. **Toggle 'Volume EQ' on/off** [`AUDIO-002`].
30. **Shift the 'Volume balance' slider** [`AUDIO-003`].

**Note:** "Conversation detection" is already ON at session start (screen incidentally visible on
the Sound page) — persisted state from `CAP-019`, confirming that toggle's effect survived across
sessions.

## Event Timeline

| Time (local) | Action | Initiator | Test-ID | Wire evidence |
|---|---|---|---|---|
| 08:15:24 | Video start | — | — | Video first frame |
| 08:15:2x–08:15:5x | Bluetooth reconnect; navigate Device details → Sound | User (App) | — | Connection handshake burst |
| **08:15:53.965** (video-confirmed tap ≈t=33s) | **Mono audio OFF→ON** | User (App) | `AUDIO-001` | Frame 1621 |
| **08:16:04.605** (video-confirmed toggle OFF by t=41s/08:16:05) | **Mono audio ON→OFF** | User (App) | `AUDIO-001` | Frame 1823 (a second, opposite-direction sample of the same Test-ID) |
| — | Navigate Sound → Equalizer (tap "Equalizer") | User (App) | — | No RFCOMM write (navigation only) |
| **08:16:15.825** (video-confirmed toggle OFF by t=53s) | **Volume EQ ON→OFF** | User (App) | `AUDIO-002` | Frame 1871 |
| **08:16:24.090** | **Volume EQ OFF→ON** | User (App) | `AUDIO-002` | Frame 1895 (second sample, opposite direction) |
| — | Navigate back to Sound | User (App) | — | — |
| **08:16:34.740–08:17:20.458** (video-confirmed: slider at center t=70s/08:16:34; dragged toward the right edge by t=78s/08:16:42; back toward center-right by t=86s/08:16:50) | **Drag the 'Volume balance' slider** (continuous drag, 7 wire updates) | User (App) | `AUDIO-003` | Frames 1922, 1944, 2019, 2039, 2056, 2073, 2099 — see §Decode |
| 08:17:27 | Video end | — | — | Video last frame |

## Decode (DLCI 0x02, `libmaestro` Pigweed `pw_hdlc` channel, `PROTOCOL.md` §2.2a)

All frames CRC-32 verified with the standard method (`CAP-020-FINDINGS.md` §3). Decoded from
payload offset 13 (`field=tag>>3`, `wiretype=tag&7`), same outer `field5{field4{...}}` wrapper as
every other capture in this batch:

```
AUDIO-001 ON  (frame 1621, 08:15:53.965): field5(len5){ field4(len3){ field19=1 } }
AUDIO-001 OFF (frame 1823, 08:16:04.605): field5(len5){ field4(len3){ field19=0 } }
AUDIO-002 a   (frame 1871, 08:16:15.825): field5(len4){ field4(len2){ field15=0 } }
AUDIO-002 b   (frame 1895, 08:16:24.090): field5(len4){ field4(len2){ field15=1 } }
AUDIO-003 #1  (frame 1922, 08:16:34.740): field5(len6){ field4(len4){ field17=199 } }
AUDIO-003 #2  (frame 1944, 08:16:42.770): field5(len5){ field4(len3){ field17=123 } }
AUDIO-003 #3  (frame 2019, 08:16:51.533): field17=49
AUDIO-003 #4  (frame 2039, 08:16:59.396): field17=30
AUDIO-003 #5  (frame 2056, 08:17:06.224): field17=150
AUDIO-003 #6  (frame 2073, 08:17:12.863): field17=200
AUDIO-003 #7  (frame 2099, 08:17:20.458): field17=10
```

**🟡 HYPOTHESIS:** `field 19` = Mono audio (clean two-sample ON/OFF match, tight timing correlation
with both video-observed taps), `field 15` = Volume EQ (same, two samples), `field 17` = Volume
balance (7 samples, all during the single continuous drag gesture the video shows, no other action
happening in this window).

**Not confirmed, flagged rather than guessed (`AGENTS.md` §13):** `field 17`'s numeric scale,
range, and which direction (Left/Right) increasing values represent. The 7 sampled values
(199, 123, 49, 30, 150, 200, 10) do not monotonically track the video's own coarse left-right
description of the drag (center → right-extreme → back toward center) in an obvious way at 1fps
video-sampling resolution — a continuous drag emits many wire updates per second (the video's 1s
sampling only captures 3 checkpoints across 7 wire values), so this is most likely just
under-sampling on the video side, not a contradiction, but it is **not resolved** which specific
wire value corresponds to which on-screen slider position. A future capture that pauses briefly at
each slider extreme (rather than dragging continuously) would let a clean min/max/center range be
read directly.

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [x] Identify which DLCI/channel carries each command frame. → DLCI 0x02 for all three.
- [x] For `AUDIO-003`: confirm the persistent-write claim (survives disconnect/reconnect)? →
      **Not tested this session** — no disconnect/reconnect cycle was captured. Flagged as an
      open follow-up, not assumed either way.
- [x] Compare structure against `PROTOCOL.md` §2's envelope hypotheses. → Matches the DLCI 0x02
      HDLC/CRC-32 `field5{field4{...}}` envelope exactly.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise — `AUDIO-001`–`AUDIO-003`
      all referenced above.
- [x] Write `CAP-022-FINDINGS.md` per `PROJECT_RULES.md` §2.
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index.
- [x] Rename this capture's folder to the actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-022-2026-08-21_08-15-24_08-17-27-Group_H/CAP-022-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/#/captures/CAP-022-2026-08-21_08-15-24_08-17-27-Group_H/CAP-022-EVENT-NOTES
