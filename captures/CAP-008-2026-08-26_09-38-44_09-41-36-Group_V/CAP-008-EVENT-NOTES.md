# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group V, In-call HFP/SCO audio behavior (`CAP-008`)

**Status:** ✅ **Captured and analyzed** — see `CAP-008-FINDINGS.md` in this folder for the full
wire/video correlation. Every timestamp below was independently re-verified against each
recording's burned-in camera clock (1s resolution) and matches to within 1 second — no
corrections were needed. Folder already renamed to its actual session date/start/end time.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group V):** `CAP-002-FINDINGS.md` §5 found zero
`AT+` traffic anywhere outside `CAP-001`'s own pairing-time handshake across a full 8+ hour log,
and `CAP-001-FINDINGS.md` §6 Task 6 ruled out any SCO/eSCO HCI event in all four captures to
date — both findings converge on the same missing scenario: **none of the captures so far ever
contains an actual phone call**, the one trigger that would exercise HFP's Service Level
Connection setup and channel 5/DLCI 0x0a's audio path.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-008`                     |
|      Group(s)    |                         V                          |
|       Date       |                     2026-08-26                     |
| Firmware version |     `release_5.203` (🟢 wire-confirmed, DLCI 0x08 frame 1116 — see `CAP-008-FINDINGS.md`) |
|   Test device    | Pixel 7a, Android 17 (⚪ assumed, same device as `CAP-001`–`CAP-025`), official Pixel Buds Companion App (⚪ assumed v1.0.955078536, not shown on screen this session) |
| Video file       |               `CAP-008-recording1.mp4`, `CAP-008-recording2.mp4`, `CAP-008-recording3.mp4` and `CAP-008-recording4.mp4`        |
| Log file         |             `CAP-008-btsnoop_hci.log` (309.3s, 3,668 packets)        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            `04:00:6e:cf:6e:07` — same physical unit as every prior capture             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group V)

1. **Place or receive an actual phone call** while connected to the Buds [`CALL-001`]. Note the
   exact start and end time of the call.
2. Optionally, during the call, trigger a deliberate audio-routing action (e.g. switch the audio
   output device) as a bonus data point — note its time separately.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|----------|---|---|---|---|
| 09:38:44 | start of video CAP-008-recording1.mp4. | - | - | - |
| 09:38:49 | user turns on bluetooth. | User (Hardware) | - | `Create Connection` sent 09:38:47.919 (frame 230); stored link key reused, no fresh pairing |
| 09:38:52 | connected to pixel buds pro 2. | - | - | `Connect Complete` 09:38:50.335 (frame 587); full HFP SLC handshake completes by 09:38:50.96 (frames 776–1132) — see `CAP-008-FINDINGS.md` §1/§3 |
| 09:38:59 | user pressed play in spotify (podcast started playing). | User (App) | - | `AVDTP Start` (A2DP resume) 09:38:58.646 (frame 1463) — Δ≈0.35s |
| 09:39:08 | user start pixel buds app. | User (App) | - | not wire-visible (local UI navigation) |
| 09:39:19 | end of video CAP-008-recording1.mp4. | - | - | Call 1 alerting actually begins at 09:39:19.689 (frame 1639, `+CIEV:2,1`), right at this boundary — see `CAP-008-FINDINGS.md` §4 gap note |
| 09:39:29 | start of video CAP-008-recording2.mp4, incomming call. | Buds/Case (Auto) | `CALL-001` | 3× `RING` already sent (09:39:19.823–29.827, frames 1658/1730/1767); eSCO connection (mSBC) set up 09:39:19.697–.821 (frames 1646/1656) |
| 09:39:34 | user pressed 'answer' incomming call, (podcast paused playing). | User (App) | `CALL-001` | `+CIEV:1,1`+`+CIEV:2,0` 09:39:33.861 (frames 1842/1843), Δ≈0.14s; `AVDTP Suspend` already sent 09:39:19.666 (frame 1618) |
| 09:39:51 | user ends incomming call, (podcast resumed playing). | User (App) | `CALL-001` | `+CIEV:1,0` 09:39:51.694 (frame 2022), Δ≈0s; eSCO handle 0x0005 `Disconnect Complete` same instant (frame 2020); `AVDTP Start` 09:39:51.904 (frame 2034) |
| 09:39:59 | end of video CAP-008-recording2.mp4. | - | - | - |
| 09:40:02 | start of video CAP-008-recording3.mp4. | - | - | - |
| 09:40:12 | (not video-visible, notification shade open) | Buds/Case (Auto) | - | isolated unsolicited `+CIEV:6,4` (`battchg`) push, 09:40:12.143 (frame 2161) — see `CAP-008-FINDINGS.md` §9, open question |
| 09:40:14 | user pressed pause in spotify (podcast stopped playing). | User (App) | - | pause tap matches exactly on video; not separately isolated on the wire in this pass |
| 09:40:22 | end of video CAP-008-recording3.mp4. | - | - | - |
| 09:40:56 | start of video CAP-008-recording4.mp4, incomming call. | Buds/Case (Auto) | `CALL-001` | Call 2 alerting began 09:40:23.468 (frame 2273, `+CIEV:2,1`), well before this video starts; 8× `RING` 09:40:23.732–58.734 |
| 09:41:00 | user pressed 'answer' incomming call. | User (App) | `CALL-001` | `+CIEV:1,1`+`+CIEV:2,0` 09:40:59.340 (frames 2632/2633), Δ≈0.7s |
| 09:41:18 | user ends incomming call. | User (App) | `CALL-001` | `+CIEV:1,0` 09:41:18.344 (frame 2797), Δ≈0s; eSCO handle 0x0006 `Disconnect Complete` same instant (frame 2794); `AVDTP Start` 09:41:18.631 (frame 2807) |
| 09:41:36 | end of video CAP-008-recording4.mp4. | - | - | - |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group V)

- [x] Does a full HFP AT-command SLC handshake reappear, matching `CAP-001`'s shape? **Yes** —
      structurally identical (`BRSF`/`BAC`/`CIND`/`CMER`/`BIND`/`BIEV`/`VGM`/`VGS`/`NREC`/`COPS`/
      `CMEE`), frames 776–1132, on RFCOMM channel 6/DLCI 0x0c this session (not channel 4 as in
      `CAP-001` — RFCOMM channel numbers are session-local, see `CAP-008-FINDINGS.md` §2). See
      `CAP-008-FINDINGS.md` §3.
- [x] Does channel 5/DLCI 0x0a carry any payload this time? **No** — `SABM`/`UA` only (frames
      1042/1049), zero payload through both calls; the 14th consecutive silent capture, but now
      with "the call's SCO/eSCO audio path" specifically ruled out as its purpose. See
      `CAP-008-FINDINGS.md` §6.
- [x] Does an HCI-level `Setup Synchronous Connection` (`0x0428`) / `Enhanced Setup Synchronous
      Connection` (`0x043D`) / `Synchronous Connection Complete` (`0x2C`) event appear at all? **Yes,
      twice** — one `Enhanced Setup Synchronous Connection`/`Synchronous Connection Complete` pair
      per call (mSBC codec, eSCO link), the first such event in any capture in this project. See
      `CAP-008-FINDINGS.md` §5.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — `CALL-001` is exercised twice, both video- and wire-confirmed; no gap.
      See `CAP-008-FINDINGS.md` §11.
- [x] Write `CAP-008-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a). Done.
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [x] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time — already done prior to this analysis pass.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-008-2026-08-26_09-38-44_09-41-36-Group_V/CAP-008-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-008-2026-08-26_09-38-44_09-41-36-Group_V/CAP-008-EVENT-NOTES
