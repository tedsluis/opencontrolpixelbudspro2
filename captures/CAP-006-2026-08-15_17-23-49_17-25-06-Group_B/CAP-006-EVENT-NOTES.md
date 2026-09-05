# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group B Repeat Capture (`CAP-006`)

**Status:** Reviewed against `CAP-006-recording.mp4` frame-by-frame (~1s resolution, using the
video's burned-in wall-clock overlay) and cross-checked against `CAP-006-btsnoop_hci.log` via
`tshark`/Wireshark. Corrects and extends the original draft. See `CAP-006-FINDINGS.md` in this
same folder for the standardized, evidence-graded protocol findings extracted from this
correlation — this file is the *event timeline*, `CAP-006-FINDINGS.md` is *what it means for the
protocol*.

**Video recovery note (resolved 2026-09-05):** the original `CAP-006-recording.mp4`'s `moov` atom
was internally inconsistent for the video track (`stsc` claims 2,342 samples, `stsz` claims 2,353;
the final sample offset points ~3.8 KB past the actual end of the `mdat` box) — `ffmpeg`/`ffprobe`
(8.1.2) refused to open the file at all (`stream 1, contradictionary STSC and STCO`). VLC's (3.0.23)
more lenient MP4 demuxer decoded the file up to the truncation point before disabling the video
track (`Failed to read 6025 bytes sample at 20208791`); at the time, the video was recovered via
VLC's `scene` video filter (`cvlc --intf dummy --vout dummy --aout dummy --video-filter=scene
--scene-format=png --scene-ratio=30 --scene-prefix=frame --scene-path=<dir> --play-and-exit
CAP-006-recording.mp4 vlc://quit`), producing one PNG roughly every native frame interval up to
~17:25:05 — this recovered every action in this session (all four ANC taps occur before 17:24:52);
only the last ~2s (17:25:05–17:25:07) was unrecovered. **The maintainer has since re-pulled the
file from the phone and replaced it** — the current `CAP-006-recording.mp4` (79.49s, 1280×720,
17:23:49–~17:25:08) opens cleanly in `ffmpeg`/`ffprobe`, no VLC workaround needed going forward.
Note this file still does not extend to 17:26:55 (this session's third ANC-Notify occurrence,
`CAP-006-FINDINGS.md`/`DECISIONS.md` ADR-024) — the session's log runs to 17:27:30, well past
either video file's coverage, so that specific moment remains unverified on screen.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-006`                     |
|      Group(s)    |          B (Active Noise Control, single-tap repeat) |
|       Date       |                     2026-08-15                     |
| Firmware version |                   release_5.203                    |
|   Test device    |    Pixel 7a, Android 17 (Official Pixel Buds Companion App v1.0.955078536) |
| Video file       | `CAP-006-recording.mp4` — 17:23:49–~17:25:08 (wall clock, +0200) per burned-in overlay; re-pulled from the phone 2026-09-05, opens cleanly (see video recovery note above) |
| Log file         | `CAP-006-btsnoop_hci.log` — 233.16s, 2026-08-15 17:23:37.30–17:27:30.45 (wall clock, +0200), 3,441 packets |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `...cf:6e:07` (same physical device as `CAP-001`/`CAP-002`/`CAP-004`) |

**Scope note (per the maintainer's test design):** unlike `CAP-001` (six ANC taps bundled into one
continuous, unpaused ~30s window alongside pairing and case/bud housekeeping), this session was
run as a clean repeat of Group B per `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s 2026-08-14 "Repeat
recommendation": Bluetooth was enabled and the connection allowed to settle *before* the Pixel
Buds app was opened, the ANC row was confirmed fully active/enabled on screen (not greyed out)
before any tap, and each of the four ANC modes was tapped exactly once, individually, with several
seconds of separation between taps and no other action interleaved. This directly targets the open
question from `CAP-001-FINDINGS.md` §5 / `DECISIONS.md` ADR-009: whether *every* genuine ANC tap
reliably produces a `0x12` "Set ANC state" frame, given that 2 of `CAP-001`'s 6 taps did not.

## Event Timeline

Timestamps are the video's own on-screen wall-clock overlay (±1s, per-frame reading — see the video
recovery note above), CEST/+0200; `CAP-006-btsnoop_hci.log` uses the same wall clock, so times below
are directly comparable to log frame timestamps without offset correction.

| Time | Action / Event | Initiator | Test-ID | Evidence in `CAP-006-btsnoop_hci.log` |
|---|---|---|---|---|
| 17:23:49 | Start video recording. Screen shows the system **Bluetooth** settings sheet, **"Bluetooth is off"**, `Use Bluetooth` toggled off | — | — | — |
| 17:23:53–54 | Tap **Use Bluetooth** toggle on | User (Settings) | — | `Create Connection` sent 17:23:52.970 (frame 323) → Connection Complete, status `0x00` (frame 361, 17:23:53.238) — succeeds on the **first** attempt, unlike `CAP-001`'s three-attempt sequence; Encryption Change (frame 423, 17:23:53.316); RFCOMM multiplexer SABM (channel 0, frame 550, 17:23:53.622) |
| 17:23:55 | Device list shows "Pixel Buds Pro 2 van Ted — Connecting…" | App (Auto) | — | RFCOMM channels/DLCIs 0x02/0x04/0x08/0x0c opening (frames 704–845); DLCI 0x04 Message Stream traffic begins (frame 719, 17:23:54.337) |
| 17:23:56 | Device list shows "Pixel Buds Pro 2 van Ted — Active. L: 100%, R: 100% batt…" (connected) | App (Auto) | `PAIR-003` (reconnect to an already-bonded device — this capture's own bonus data point for the open Pixel-7a-specific gap noted in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §9) | Device Information / capability frames on DLCI 0x04 (frames 727–822, through 17:23:54.86); no fresh pairing/link-key exchange (stored link key reused, consistent with an already-bonded reconnect, not `PAIR-001`) |
| 17:24:04–06 | User navigates from the notification shade / app drawer into the **Pixel Buds** app, landing on the "Device details" screen (ANC row not yet rendered) | User (App) | — | No new RFCOMM traffic in this window beyond periodic housekeeping already covered above |
| 17:24:08 | Pixel Buds app: "Device details" screen fully loaded, **Active noise control** row visible with all four mode buttons already enabled (not greyed out) | App (Auto) | — | — |
| 17:24:12 | Tap **Noise cancellation** | User (App) | `ANC-002` | `Set ANC state` (Group `0x08` Code `0x12`), `new_mode=0x08` (ANC/Active Noise Cancelling) — frame 1393, 17:24:13.296; ACK frame 1398, 17:24:13.422; Notify (Group `0x08` Code `0x13`) frames 1399/1402, 17:24:13.423/13.458 |
| 17:24:26 | Tap **Off** | User (App) | `ANC-001` | `Set ANC state`, `new_mode=0x20` (Off) — frame 1627, 17:24:26.321; ACK frame 1630, 17:24:26.371; Notify frames 1631/1633, 17:24:26.372/26.374 |
| 17:24:38 | Tap **Adaptive** | User (App) | `ANC-003` | `Set ANC state`, `new_mode=0x40` (Adaptive) — frame 1731, 17:24:38.666; ACK frame 1735, 17:24:38.708; Notify frames 1736/1738, 17:24:38.709/38.711 |
| 17:24:51 | Tap **Transparency** | User (App) | `ANC-004` | `Set ANC state`, `new_mode=0x80` (Transparent/Aware) — frame 1862, 17:24:50.825 (0.2s *before* the observed on-screen tap, within this capture's ±1s video-sampling tolerance); ACK frame 1864, 17:24:50.862; Notify frames 1865/1867, 17:24:50.863/50.864 |
| 17:24:52–55 | UI shows a brief transitional/all-muted button state, then **Transparency** fully selected/active | App (Auto) | — | Matches the Notify frames above; no further `0x12` frames follow |
| ~17:24:56–17:25:02 | (not directly visible on screen — video decode is truncated in this window, see video recovery note) RFCOMM channels 1 and 2 are cleanly torn down (`DISC`/`UA`) and a fresh SDP query + full channel/multiplexer renegotiation runs on DLCIs 0x02/0x04/0x08/0x0a, without any preceding HCI `Disconnect` or `Create Connection` event — the underlying ACL link is never dropped | — | — | `DISC` Channel=1/2 (frames 1888/1890, 17:24:56.689–.690); fresh SDP Service Search (frame 1908, 17:24:58.398); RFCOMM SABM on DLCI 0x0a/0x08/0x02/0x04 (frames 1920/1949/2030/2091, 17:24:58.450–17:25:01.828); a repeat Device-Information/capability burst and a fresh ANC Notify (`e8e880`, still Transparent — frame 2115, 17:25:02.084) follow, consistent with a full profile/channel re-sync rather than a new user action — see `CAP-006-FINDINGS.md` §6 |
| ~17:25:07 | End video recording (nominal, per file naming/original draft — last independently recovered frame is 17:25:05) | — | — | — |

## Corrections vs. the original draft of this file

- Group corrected from "T (EQ command isolation)" to **B (Active Noise Control, single-tap
  repeat)** — the original draft's header carried over Group T's label from the previous session
  (`CAP-005`) by mistake; this capture's actual content (four isolated ANC taps) and its own
  directory name (`..._Group_B`) are both Group B.
- Video filename corrected from `CAP-006-recoding.mp4` (typo) to `CAP-006-recording.mp4` (actual
  file on disk).
- All four ANC-mode tap timestamps re-verified frame-by-frame against the video's burned-in
  wall-clock overlay (see video recovery note) and refined to the frame where the finger first
  contacts the target button, rather than the frame where the UI shows the mode as fully selected
  (which lags the physical tap by ~1–3s in several cases, e.g. Transparency: tap visible at
  17:24:51, UI fully settled only by 17:24:55). `ANC-002` (Noise Cancellation)'s original draft
  time (17:24:14) was the UI-settled time, not the tap time (17:24:12) — corrected here since the
  tap time is what the log's `0x12` frame should be compared against.
- Added explicit Test-ID tags (`ANC-001`–`ANC-004`, plus `PAIR-003` for the incidental reconnect)
  and `CAP-006-btsnoop_hci.log` frame references for every event where log evidence exists, per
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5's correlation workflow and `AGENTS.md` §13's traceability
  check.
- Added the post-Transparency RFCOMM channel renegotiation window (~17:24:56–17:25:02), which the
  original draft did not mention at all — flagged as not independently attributable to an on-screen
  action since the video decode is truncated in that exact window.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-006-2026-08-15_17-23-49_17-25-06-Group_B/CAP-006-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-006-2026-08-15_17-23-49_17-25-06-Group_B/CAP-006-EVENT-NOTES
