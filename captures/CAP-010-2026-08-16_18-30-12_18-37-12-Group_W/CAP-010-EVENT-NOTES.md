# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group W Capture (`CAP-010`)

**Status:** Reviewed against `CAP-010-recording.mp4` frame-by-frame (1s resolution, using the
video's burned-in wall-clock overlay) and cross-checked against `CAP-010-btsnoop_hci.log` via
`tshark`/Wireshark. Corrects and extends the original draft. See `CAP-010-FINDINGS.md` in this
same folder for the standardized, evidence-graded protocol findings extracted from this
correlation — this file is the *event timeline*, `CAP-010-FINDINGS.md` is *what it means for the
protocol*.

## Log Metadata

|      Field       |                       Value                        |
|------------------|----------------------------------------------------|
|    Capture ID    |                      `CAP-010`                     |
|      Group(s)    | W (`GATT-001` — stronger GATT cache-busting |
|       Date       |                     2026-08-16                     |
| Firmware version |  `release_5.203` (confirmed on-the-wire this session too — `CAP-010-FINDINGS.md` §5) |
|   Test device    |    Pixel 7a, Android 17, Official Pixel Buds Companion App (version not visible on screen this capture) — same physical phone as `CAP-001`–`CAP-007`     |
| Video file       | `CAP-010-recording.mp4` — (wall clock, +0200) |
| Log file         | `CAP-010-btsnoop_hci.log` — (wall clock, +0200) |


## Event Timeline

Timestamps are the video's own on-screen wall-clock overlay (±1s, since frames were sampled at
1fps); `CAP-010-btsnoop_hci.log` uses the same wall clock (+0200), so times below are directly
comparable to log frame timestamps without offset correction.

| Time | Action / Event | Initiator | Test-ID | Evidence in `CAP-010-btsnoop_hci.log` |
|----------|---|---|---|---|
| 18:30:12 | Start video recording. Phone screen shows lock/"Bluetooth is off" screen; Buds case closed on the table | — | — | — |
| 18:30:16 | User turns Bluetooth on  | User (App) | — | — |
| 18:30:28 | User start nRF connect app | — | — | — |
| 18:30:57 | User pressed 'SCAN' in the nRF connect app. | — | — | — |
| 18:31:02 | User opens pixel buds pro 2 case | — | — | — |
| 18:31:03 | User presses pairing button on the pixel buds pro 2 case. | — | — | — |
| 18:31:39 | user selects 'connect' to the pixel buds pro 2 in scanner list in the nRF app. | — | — | — |
| 18:32:53 | user selects bound in nRF app | — | — | — |
| 18:32:58 | user selects 'pair with Buds Pro 2 van ted in nRF app. | — | — | — |
| 18:33:53 | user selects pixel buds pro 2 Device details | — | — | — |
| 18:37:12| End video recording | — | — | — |


