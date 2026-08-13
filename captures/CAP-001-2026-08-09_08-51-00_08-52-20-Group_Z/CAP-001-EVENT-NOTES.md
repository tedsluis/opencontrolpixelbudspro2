# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group Z Capture (`CAP-001`)

**Status:** Reviewed against `CAP-001-recording.mp4` frame-by-frame (1s resolution, using the video's
burned-in wall-clock overlay) and cross-checked against `CAP-001-btsnoop_hci.log` via `tshark`/Wireshark.
Corrects and extends the original draft. See `CAP-001-FINDINGS.md` in this same folder for the
standardized, evidence-graded protocol findings extracted from this correlation — this file is
the *event timeline*, `CAP-001-FINDINGS.md` is *what it means for the protocol*.

## Log Metadata

|      Field       |                       Value                        |
|------------------|----------------------------------------------------|
|    Capture ID    |                      `CAP-001`                     |
|      Group(s)    | Z (pipeline validation) + A (pairing) + B (all 4 ANC modes) + M (case/in-ear) — see note below |
|       Date       |                     2026-08-09                     |
| Firmware version |                   unknown — not visible in this capture, TBD |
|   Test device    |    Pixel 7a (Official Pixel Buds Companion App)    |
| Video file       | `CAP-001-recording.mp4` — 83.4s, starts 08:50:57, ends 08:52:20 (wall clock, +0200) |
| Log file         | `CAP-001-btsnoop_hci.log` — 233.9s, 08:50:32.67–08:54:26.57 (wall clock, +0200), 2,663 packets |

**Scope note:** this session was intended as the Group Z pipeline-validation capture (one
trivial action) but in practice also exercised the pairing baseline (Group A) and all four ANC
modes multiple times (Group B), plus case/bud in-ear transitions (Group M), all within one
continuous ~80s window without isolation pauses between actions. This is useful for confirming
the whole tooling pipeline works end-to-end (it does), but is **not** a substitute for a properly
isolated capture when attributing specific commands to specific RFCOMM frames — see
`CAP-001-FINDINGS.md` §5 for what this cost us.

## Event Timeline

Timestamps are the video's own on-screen wall-clock overlay (±1s, since frames were sampled at
1fps); `CAP-001-btsnoop_hci.log` uses the same wall clock (+0200), so times below are directly
comparable to log frame timestamps without offset correction.

| Time | Action / Event | Initiator | Test-ID | Evidence in `CAP-001-btsnoop_hci.log` |
|---|---|---|---|---|
| 08:50:57 | Start video recording. Screen already shows buds recognized (Left 100%, Case 63%, Right 100%), `Connect`/`Forget` both enabled | — | — | A BLE link to the peer already exists from 08:50:36.27 (frame 290, `LE Enhanced Connection Complete`) — i.e. some association predates this recording |
| 08:51:02–03 | Tap **Forget** | User (App) | — | No immediately visible log effect; a link key was still available and reused at 08:51:12 (frame 911) — see `CAP-001-FINDINGS.md` §6 open question |
| 08:51:06–11 | Case lid opened; buds visible, case LED lit | User (Hardware) | `CASE-003` | 1st `Create Connection` (08:51:01.98, frame 732) fails with **Page Timeout** (frame 737, status `0x04`) — consistent with the peer not yet reachable until the lid was fully open |
| 08:51:11 | System notification card appears: "Pixel Buds Pro 2 van Ted / Left 100% Case 62% Right 100%" | App (Auto) | — | — |
| 08:51:07–09 | 2nd `Create Connection` attempt | — | — | Succeeds (frame 775, 08:51:09.33, status `0x00`) then is torn down ~0.24s later (`Disconnect Complete`, frame 831, 08:51:09.56) |
| 08:51:14–15 | App briefly shows **"Problem connecting. Turn device off & back on"** | App (Auto) | — | Coincides exactly with the connect-then-disconnect cycle above |
| 08:51:09–12 | 3rd `Create Connection` attempt — this one persists | — | `PAIR-001` | Succeeds (frame 855, 08:51:12.10, status `0x00`); Link Key Request/Reply → Authentication Complete → Encryption Change all complete by 08:51:12.21 (frames 911–917), using a **stored** link key (no fresh pairing dialog) |
| 08:51:12–15 | (not directly visible on screen — app UI mid-transition) | — | `PAIR-001` | RFCOMM channels 0, 1, 2, 4, 5 opened (frames 984–1070, 1334); SDP queries for Audio Sink/AVRCP/HFP/A2DP (frames 902–1231); full HFP AT-command handshake incl. `AT+BIEV=2,100` battery report (frames 1236–1310) — see `CAP-001-FINDINGS.md` §3 |
| 08:51:16 | Hand touches left bud in case | User (Hardware) | — | — |
| 08:51:17 | App shows **Disconnect** button, ANC row still greyed out, "Active" label under Case | App (Auto) | — | — |
| 08:51:20–21 | Left earbud removed from case (held in hand) | User (Hardware) | `CASE-004` | — |
| 08:51:23 | Left ring shows not-charging state ("0" instead of charging bolt) | App (Auto) | `INEAR-001`(partial) | — |
| 08:51:32 | Tap **Transparency** (1st) — ANC row now fully active | User (App) | `ANC-004` | No confidently attributable command frame identified — see `CAP-001-FINDINGS.md` §5 |
| 08:51:36 | Right earbud removed from case | User (Hardware) | `CASE-005` | — |
| 08:51:39 | Tap **Off** | User (App) | `ANC-001` | See `CAP-001-FINDINGS.md` §5 |
| 08:51:43 | Tap **Adaptive** | User (App) | `ANC-003` | See `CAP-001-FINDINGS.md` §5 |
| 08:51:49 | Tap **Transparency** (2nd) | User (App) | `ANC-004` | See `CAP-001-FINDINGS.md` §5 |
| 08:51:54 | Tap **Noise Cancellation** | User (App) | `ANC-002` | See `CAP-001-FINDINGS.md` §5 |
| 08:52:00 | Tap **Off** (2nd) | User (App) | `ANC-001` | See `CAP-001-FINDINGS.md` §5 |
| 08:52:04 | Right earbud placed back in case | User (Hardware) | `CASE-006`(part) | — |
| 08:52:09–10 | Left earbud placed back in case; app screen reverts to default (Disconnect → greyed `Connect`) | User (Hardware) / App (Auto) | `CASE-006`(part) | — |
| 08:52:12–13 | Case lid closed | User (Hardware) | `CASE-006`(end) | `Disconnect Complete` at 08:52:08.34 (frame 2302) — note: log-side disconnect precedes the visible lid-close by several seconds, consistent with the classic link dropping as soon as both buds are docked, before the lid mechanically shuts |
| 08:52:20 | End video recording | — | — | — |

## Corrections vs. the original draft of this file

- Video actually starts at **08:50:57**, not 08:51:00 (minor — off by 3s in the original).
- The pairing sequence was **not a single clean connect** — it took three `Create Connection`
  attempts, including one Page Timeout failure and one connect-then-immediately-disconnect cycle,
  with a corresponding transient "Problem connecting" warning shown in the app around
  08:51:14–15. The original draft only listed "08:51:17 Connected" with no indication of this.
- The right-bud-removal and left-bud-removal timestamps were adjusted slightly (08:51:20/36
  instead of 08:51:24/36) based on the actual video frames showing hands physically removing
  each bud, rather than the resulting battery-icon state change (which lags the physical action
  by 1–3s).
- Added explicit `CAP-001-btsnoop_hci.log` frame references for every event where log evidence exists,
  per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5's correlation workflow.
- The six ANC-mode taps' *exact click times* were re-verified against 1s-resolution video frames
  and match the original draft closely (all within ±1s) — those were already accurate.
