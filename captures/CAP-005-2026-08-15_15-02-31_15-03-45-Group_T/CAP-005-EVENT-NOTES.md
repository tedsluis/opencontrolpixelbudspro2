# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group T Capture (`CAP-005`)

**Status:** Reviewed against `CAP-005-recording.mp4` frame-by-frame (1s resolution, using the video's
burned-in wall-clock overlay) and cross-checked against `CAP-005-btsnoop_hci.log` via `tshark`/Wireshark.
Corrects and extends the original draft. See `CAP-005-FINDINGS.md` in this same folder for the
standardized, evidence-graded protocol findings extracted from this correlation — this file is
the *event timeline*, `CAP-005-FINDINGS.md` is *what it means for the protocol*.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-005`                     |
|      Group(s)    |               T (EQ command isolation)             |
|       Date       |                     2026-08-15                     |
| Firmware version |                   release_5.203                    |
|   Test device    |    Pixel 7a, Android 17 (Official Pixel Buds Companion App v1.0.955078536) |
| Video file       | `CAP-005-recording.mp4` — 74.8s, starts 15:02:31, ends ~15:03:45 (wall clock, +0200) |
| Log file         | `CAP-005-btsnoop_hci.log` — 270.1s, 15:02:24.56–15:06:54.63 (wall clock, +0200), 2,533 packets |

**Scope note (per the maintainer's test design):** this capture isolates EQ commands to determine
their specific DLCI channel and framing, and specifically tests whether EQ presets and EQ sliders
share the same command channel. The mandatory ≥10s isolation discipline between the two tested
actions (`EQP-002`, `EQS-004`) was strictly maintained — confirmed independently below and in
`CAP-005-FINDINGS.md` §5c (DLCI 0x02 is silent outside a handful of tightly-bounded bursts).

## Event Timeline

Timestamps are the video's own on-screen wall-clock overlay (±1s, 1fps sampling), CEST/+0200;
`CAP-005-btsnoop_hci.log` uses the same wall clock, so times below are directly comparable to log
frame timestamps without offset correction.

| Time | Action / Event | Initiator | Test-ID | Evidence in `CAP-005-btsnoop_hci.log` |
|---|---|---|---|---|
| 15:02:31 | Start video recording. Screen shows the system **Bluetooth** settings sheet, **"Bluetooth is off"**, `Use Bluetooth` toggled off | — | — | Log already running since 15:02:24.56 (~7s before recording starts) with ongoing LE advertising-report/vendor-command traffic despite the on-screen toggle reading "off" — see Corrections below |
| ~15:02:37 | Tap **Use Bluetooth** toggle on; device list shows "Pixel Buds Pro 2 van Ted — Connecting…" | User (Settings) | — | Classic `Create Connection Request` to `04:00:6e:cf:6e:07` already sent at 15:02:35.53 (frame 291) — ~1.5s *before* the toggle's on-screen effect is visible, i.e. the underlying connect attempt begins a beat ahead of the confirmed screen change |
| 15:02:37.85 (log) | Classic ACL connection completes | App (Auto) | — | `Connect Complete`, frame 389, 15:02:37.853420, status `0x00`, handle `0x0002`, BD_ADDR `04:00:6e:cf:6e:07` |
| ~15:02:39–40 | Device row updates to **"Active. L: 100%, R: 100% batt…"** | App (Auto) | — | Coincides with a burst of ~45 `Sent`-direction DLCI 0x02 frames, 15:02:39.32–15:02:42.34 — a one-time capability/setup handshake, structurally consistent with the pattern already documented for other DLCIs in `CAP-004-FINDINGS.md` §5a (see `CAP-005-FINDINGS.md` §5c) |
| 15:02:52–53 | User opens the Pixel Buds companion app (app-drawer icon) | User (App) | — | — |
| 15:02:53 | App's **Device details** screen visible (Left 100%, Case 21%, Right 100%, `Forget`/`Disconnect`) | App (Auto) | — | — |
| **15:03:12.02 (log) / 15:03:13 (UI)** | **User taps EQ preset "Heavy bass"** (on-screen label; `PROTOCOL.md` §4.2 already lists `Bass Boost`/`Heavy Bass` as the same preset under two names) — Bass/Low-bass sliders shift right of center | User (App) | `EQP-002` | DLCI 0x02, frame 1245 (`Sent`, 15:03:12.018963) + frames 1249/1250 (`Rcvd` ack) — see `CAP-005-FINDINGS.md` §3 |
| **15:03:22.31 (log) / 15:03:22–23 (UI)** | **User drags the "Bass" slider left** (reduces gain); handle visibly moves before `Save` even enables | User (App) | `EQS-004` (part 1 — live preview) | DLCI 0x02, frame 1321 (`Sent`, 15:03:22.313840) + frames 1325/1326 (`Rcvd` ack) — see `CAP-005-FINDINGS.md` §4/§5 |
| 15:03:24 | `Save` button switches from greyed-out to enabled | App (Auto) | — | No new DLCI 0x02/0x04/0x08 traffic at this exact moment — this is a pure UI-state change, not a separate wire event (see Corrections below) |
| **15:03:27.61 (log) / 15:03:27–28 (UI)** | **User taps `Save`** | User (App) | `EQS-004` (part 2 — explicit save) | DLCI 0x02, frame 1338 (`Sent`, 15:03:27.605271) + frames 1340/1341 (`Rcvd` ack) — carries the *same* Bass value as part 1, under a different outer field number — see `CAP-005-FINDINGS.md` §4/§5 |
| 15:03:29 | **"EQ saved"** toast appears; preset label reverts to `Last saved` | App (Auto) | — | — |
| 15:03:45 | End video recording | — | — | — |

## Corrections vs. the original draft of this file

- **`EQS-004` was one timestamp in the original draft (15:03:24); the wire evidence shows this is
  actually two distinct, structurally-related actions ~5.3s apart** — the slider drag itself (live
  preview, frame 1321, 15:03:22.31) and a separate explicit `Save`-button tap (frame 1338,
  15:03:27.61), each producing its own DLCI 0x02 burst with the same EQ value but a different outer
  protocol field. 15:03:24 (the original draft's single timestamp) turns out to be the moment
  `Save` becomes *enabled* on-screen — not a wire event at all; the actual drag landed ~1–2s
  *before* that, and the actual save tap ~3–4s *after* it. This is the main correction this pass
  makes, in the same spirit as `CAP-001-EVENT-NOTES.md`'s "not a single clean action" correction for
  its own pairing sequence.
- **Log predates the video by ~7s and already shows Bluetooth-adjacent radio activity** (LE
  advertising reports, vendor commands) while the video's first frame still shows the
  system "Bluetooth is off" sheet — the log was evidently already running (or the chip already
  active at a lower level) before the toggle tap. Not further explained by this capture; flagged so
  a future reader doesn't mistake the log's start time for the moment Bluetooth was actually
  enabled.
- **Buds-connected UI timestamp tightened:** the original draft's single "15:02:41 Pixel Buds Pro 2
  connected" is confirmed accurate to within the stated ±1s tolerance, but the device row is already
  visibly "Active" one second earlier, at 15:02:40 — noted for precision, not a substantive change.
- `EQP-002`'s on-screen preset label reads **"Heavy bass"**, not "Bass Boost" as in the original
  draft's action text — kept as `Bass Boost` in the Test-ID's own catalog name
  (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`), but the actual on-screen string is recorded here since
  `PROTOCOL.md` §4.2 already documents these as the same preset under two names, not a discrepancy.
- Added explicit `CAP-005-btsnoop_hci.log` frame references for every event with log-side evidence,
  per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5's correlation workflow (the original draft had none).
