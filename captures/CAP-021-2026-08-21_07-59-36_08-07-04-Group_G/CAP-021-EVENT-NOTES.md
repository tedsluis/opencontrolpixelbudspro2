# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group G, Press-and-hold configuration (`CAP-021`)

**Status:** ✅ Captured and analyzed. Video reviewed via tiled contact sheets (full pass) plus
targeted sub-second single-frame extraction around each candidate action; the wire log was then
searched exhaustively for every DLCI 0x02 frame matching the settings-write envelope shape
(`CAP-020-FINDINGS.md` §5) across the **entire** session, and each match cross-checked against the
video at its exact timestamp — a log-driven-then-video-confirmed method, used here instead of a
purely video-driven one because this session's actual navigation path (multiple detours, a
dead-end into a "Touch controls" info screen, and back-and-forth between Left/Right) did not match
a simple linear "5 actions in sequence" read from the video alone.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group G):** main run-through group — attribute the
wire commands for per-earbud press-and-hold configuration (`HOLD-001`–`HOLD-004`) and the ANC-mode
rotation checklist (`HOLD-005`).

**Location note:** the press-and-hold assignment ("Left"/"Right" rows) lives at **Device details →
Controls and gestures → Customize press and hold**, tapping either row opens a **"Customize
left"/"Customize right"** screen with a 2-way segmented control ("Active noise control" /
"Digital assistant") plus, under "Active noise control", a 4-item checklist (Noise cancellation /
Off / Adaptive / Transparency) — this checklist **is** `HOLD-005`'s rotation list, reached from the
*same* screen as `HOLD-001`–`HOLD-004`, not a separate location. Tapping "Use touch controls"
(text, not the toggle) instead opens a read-only "Learn controls" info screen — a dead-end the
session briefly visited early on.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-021`                     |
|      Group(s)    |                         G                          |
|       Date       |                     2026-08-21                     |
| Firmware version |    not queried this session (⚪ ASSUMPTION `release_5.203`) |
|   Test device    | Pixel 7a, Android 17 (⚪ ASSUMPTION, not re-confirmed on screen), official Pixel Buds Companion App (version not visible on screen) |
| Video file       |            `CAP-021-recording.mp4` (448.3s, 07:59:36–08:07:04 local time, from the video's own burned-in overlay) |
| Log file         |             `CAP-021-btsnoop_hci.log` (07:59:30.485–08:09:01.819) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` (classic peer — same address documented in `CAP-016`/`CAP-019`/`CAP-020`) |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group G)

23. **Set 'Press and hold' Left → Toggle ANC** [`HOLD-001`].
24. **Set 'Press and hold' Left → Digital assistant** [`HOLD-002`].
25. **Set 'Press and hold' Right → Toggle ANC** [`HOLD-003`].
26. **Set 'Press and hold' Right → Digital assistant** [`HOLD-004`].
27. **Check/uncheck one ANC mode in the press-and-hold rotation list** [`HOLD-005`].

**Actual session shape (differs from the listed order — both starting values are already "Active
noise control", so the true chronological order of *changes* was DA→DA→ANC→ANC→checklist, not the
procedure's listed 1-2-3-4-5):**

## Event Timeline

| Time (local) | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 07:59:36 | Video start | — | — | Video first frame |
| 07:59:4x–08:00:5x | Bluetooth reconnect; navigate Device details → Controls and gestures; brief dead-end into "Touch controls" info screen (tap "Use touch controls" text, not the toggle) and back | User (App) | — | Connection handshake burst (DLCI 0x02/0x04/0x08); a `2001`→`2026` sequentially-incrementing DLCI 0x02 burst (38 frames, 07:59:42.9–07:59:44.5) matches the connection-time state-sync pattern, not a user action |
| ~08:00:10 | Incidental re-send of the `TOUCH-001`-shaped write (`field4{field4=1}`) while navigating in/out of "Touch controls"/"Controls and gestures" | User (App, incidental) | — | Frame 1626, `08:00:09.891252` — not a new toggle (touch controls was already ON, per `CAP-020`); a re-affirm, not attributed to any Group G Test-ID |
| **08:01:23.784** (video-confirmed: finger taps "Digital assistant" tab on "Customize left" at t≈108s/08:01:24) | **Set Left → Digital assistant** | User (App) | `HOLD-002` | **Frame 1895** — see §Decode. Rcvd echo: frame 1903 (08:01:24.230), which uniquely also echoes the *prior* value (`5` = Active noise control) alongside the new one (`6` = Digital assistant) |
| **08:03:16.151** (video-confirmed: finger taps "Digital assistant" tab on "Customize right" at t=220s) | **Set Right → Digital assistant** | User (App) | `HOLD-004` | **Frame 3619** — see §Decode |
| **08:03:49.667** (video context: at t=253s, "Controls and gestures" shows Left/Right both "Digital assistant"; finger taps "Left" row, about to revert it) | **Set Left → Active noise control** (i.e. `HOLD-001`, executed *after* `HOLD-002` in this session, not before) | User (App) | `HOLD-001` | **Frame 4315** — see §Decode |
| **08:04:09.920** | **Set Right → Active noise control** (`HOLD-003`, executed after `HOLD-004`) | User (App) | `HOLD-003` | **Frame 4976** — see §Decode |
| 08:05:28.428–08:06:46.203 (video-confirmed: "Customize left" open at t=352s/08:05:28; "Customize right" open at t=430s/08:06:46 — the burst spans **both** screens) | **Repeated check/uncheck of individual ANC-mode-rotation checkboxes** (Noise cancellation / Off / Adaptive / Transparency), one at a time, multiple cycles, on both Left's and Right's rotation lists | User (App) | `HOLD-005` | **16 frames**, all matching a 4-flag checklist shape — see §Decode. **Not resolved:** which specific frames belong to Left vs. Right — the wire payload for this checklist does not carry a Left/Right-distinguishing field the way the ANC/Digital-assistant writes above do (see §Decode) |
| 08:07:04 | Video end | — | — | Video last frame |

## Decode (DLCI 0x02, `libmaestro` Pigweed `pw_hdlc` channel, `PROTOCOL.md` §2.2a)

All frames below verified via the standard CRC-32 method (`PROTOCOL.md` §2.2a); the search covered
the **entire** session's DLCI 0x02 traffic (241 raw HDLC sub-frames), not just windows around a
video-estimated tap time, using:

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

def find_field5(payload):
    for start in range(6, min(25, len(payload))):
        try:
            tag, i = leb128(payload, start)
            field, wt = tag >> 3, tag & 7
            if field == 5 and wt == 2:
                ln, i2 = leb128(payload, i)
                if 0 <= ln and i2 + ln <= len(payload):
                    return start, ln, payload[i2:i2 + ln]
        except Exception:
            continue
    return None

# raw = each btrfcomm.dlci==0x02 payload's hex, split on b'\x7e' (byte-level, not text substring —
# splitting the raw hex STRING on '7e' is unsafe, it can match mid-byte)
```

Full list of every payload matching the `field 5{ ... }` outer shape (`find_field5`), with frame
number, time, and the field-5 sub-message hex:

```
1895  08:01:23.784150  22083a060a0422020806   (Left → Digital assistant, HOLD-002)
3619  08:03:16.151018  22083a06120422020806   (Right → Digital assistant, HOLD-004)
4315  08:03:49.667129  22083a060a0422020805   (Left → Active noise control, HOLD-001)
4976  08:04:09.919880  22083a06120422020805   (Right → Active noise control, HOLD-003)
5237  08:05:28.428038  220a62080800100118012001
5247  08:05:34.742543  220a62080801100118012001
5255  08:05:38.176290  220a62080801100018012001
5268  08:05:43.460275  220a62080801100018012000
5278  08:05:47.738301  220a62080801100118012000
5285  08:05:52.730866  220a62080801100118002000
5294  08:05:57.894683  220a62080801100118002001
5301  08:06:03.966534  220a62080801100118012001
5317  08:06:14.490965  220a62080800100118012001
5333  08:06:18.999697  220a62080801100118012001
5340  08:06:21.719653  220a62080801100018012001
5352  08:06:26.696716  220a62080801100118012001
5362  08:06:30.785490  220a62080801100118012000
5372  08:06:37.437194  220a62080801100118012001
5387  08:06:41.783951  220a62080801100118002001
5415  08:06:46.202614  220a62080801100118012001    (HOLD-005, 16 frames total)
```

### HOLD-001–HOLD-004: per-earbud press-and-hold action selection

Decoding `field 5{ field 4{ ... } }`'s inner content mechanically (`field=tag>>3`,
`wiretype=tag&7`):

```
1895: field7 wt=LD len=6 { field1 wt=LD len=4 { field4 wt=varint value=6 } }
3619: field7 wt=LD len=6 { field2 wt=LD len=4 { field4 wt=varint value=6 } }
4315: field7 wt=LD len=6 { field1 wt=LD len=4 { field4 wt=varint value=5 } }
4976: field7 wt=LD len=6 { field2 wt=LD len=4 { field4 wt=varint value=5 } }
```

**🟡 HYPOTHESIS, well-supported by a clean 2×2 cross-check:** `field 1` = Left, `field 2` = Right
(within the outer `field 7`), and the innermost `field 4` varint value encodes the selected action:
`5` = Active noise control, `6` = Digital assistant. This is not a single-sample guess — all 4
combinations (Left/Right × ANC/Assistant) were exercised this session and each produced exactly the
predicted field/value pair, with no exceptions. **Additional corroboration:** frame 1903 (the Rcvd
echo for 1895, `08:01:24.230`) contains **both** `field1{field4=6}` (the new value) **and**
`field2{field4=5}` (a second, distinct sub-block) in the same message — read as an old/new or
Left/Right status pair, consistent with this reading without requiring it.

### HOLD-005: ANC-mode rotation checklist

`field 4{ field 12{ field1=<0|1> field2=<0|1> field3=<0|1> field4=<0|1> } }` — four boolean flags,
one per checklist row. **🟡 HYPOTHESIS:** field order matches the on-screen top-to-bottom checkbox
order exactly (`field1`=Noise cancellation, `field2`=Off, `field3`=Adaptive, `field4`=Transparency)
— inferred from the video showing the same 4 rows in the same order, not independently confirmed by
toggling only one field in isolation (the session toggles each checkbox off-then-back-on
individually, in sequence, but adjacent writes are close enough in time that attributing each
specific frame to a specific checkbox tap by video timing alone was not attempted here — the field
values themselves are the primary evidence). **Not resolved:** this envelope carries no Left/Right
distinguishing field (unlike `HOLD-001`–`HOLD-004`'s `field7{field1|field2{...}}` wrapper), so which
of the 16 frames belong to Left's list vs. Right's list cannot be determined from the wire content
alone — the video confirms the burst spans both screens (`Customize left` open at 08:05:28,
`Customize right` open at 08:06:46) but a frame-by-frame Left/Right split was not completed this
session.

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [x] Identify which DLCI/channel carries each configuration write's command frame. → DLCI 0x02
      for all 5 Test-IDs.
- [x] Check whether Left/Right are distinguished in the payload or via separate opcodes. → A single
      shared envelope, distinguished by inner `field1`(Left)/`field2`(Right) for
      `HOLD-001`–`HOLD-004`; **not** distinguished for `HOLD-005`'s checklist (open question).
- [x] Compare structure against `PROTOCOL.md` §2's envelope hypotheses. → Matches the DLCI 0x02
      HDLC/CRC-32 envelope and the `field5{field4{...}}` outer wrapper first seen in `CAP-020`.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise — `HOLD-001`–`HOLD-005` all
      referenced above.
- [x] Write `CAP-021-FINDINGS.md` per `PROJECT_RULES.md` §2.
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index.
- [x] Rename this capture's folder to the actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-021-2026-08-21_07-59-36_08-07-04-Group_G/CAP-021-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-021-2026-08-21_07-59-36_08-07-04-Group_G/CAP-021-EVENT-NOTES
