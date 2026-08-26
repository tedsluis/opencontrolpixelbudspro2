# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group V, In-call HFP/SCO audio behavior (`CAP-008`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-008-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_V` to the actual session
date/start-time/end-time, e.g. `CAP-008-2026-09-01_10-15-00_10-22-30-Group_V`.

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
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    | TBD (Pixel 7a/9a, Android version, official app version if used) |
| Video file       |               TBD — `CAP-008-recording1.mp4`, `CAP-008-recording2.mp4`, `CAP-008-recording3.mp4` and `CAP-008-recording4.mp4`        |
| Log file         |             TBD — `CAP-008-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group V)

1. **Place or receive an actual phone call** while connected to the Buds [`CALL-001`]. Note the
   exact start and end time of the call.
2. Optionally, during the call, trigger a deliberate audio-routing action (e.g. switch the audio
   output device) as a bonus data point — note its time separately.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|----------|---|---|---|---|
| 09:38:44 | start of video CAP-008-recording1.mp4. | - | - |
| 09:38:49 | user turns on bluetooth. | - | - |
| 09:38:52 | connected to pixel buds pro 2. | - | - |
| 09:38:59 | user pressed play in spotify (podcast started playing). | - | - |
| 09:39:08 | user start pixel buds app. | - | - |
| 09:39:19 | end of video CAP-008-recording1.mp4. | - | - |
| 09:39:29 | start of video CAP-008-recording2.mp4, incomming call. | - | - |
| 09:39:34 | user pressed 'answer' incomming call, (podcast paused playing). | - | - |
| 09:39:51 | user ends incomming call, (podcast resumed playing). | - | - |
| 09:39:59 | end of video CAP-008-recording2.mp4. | - | - |
| 09:40:02 | start of video CAP-008-recording3.mp4. | - | - |
| 09:40:14 | user pressed pause in spotify (podcast stopped playing). | - | - |
| 09:40:22 | end of video CAP-008-recording3.mp4. | - | - |
| 09:40:56 | start of video CAP-008-recording4.mp4, incomming call. | - | - |
| 09:41:00 | user pressed 'answer' incomming call. | - | - |
| 09:41:18 | user ends incomming call. | - | - |
| 09:41:36 | end of video CAP-008-recording4.mp4. | - | - |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group V)

- [ ] Does a full HFP AT-command SLC handshake reappear, matching `CAP-001`'s shape?
- [ ] Does channel 5/DLCI 0x0a carry any payload this time?
- [ ] Does an HCI-level `Setup Synchronous Connection` (`0x0428`) / `Enhanced Setup Synchronous
      Connection` (`0x043D`) / `Synchronous Connection Complete` (`0x2C`) event appear at all
      (none has, in any capture to date — `CAP-001-FINDINGS.md` §6 Task 6)?

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `CALL-001` is clearly referenced above, not silently missing.
- [ ] Write `CAP-008-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-008-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_V/CAP-008-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-008-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_V/CAP-008-EVENT-NOTES
