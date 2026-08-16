# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group S Capture (`CAP-007`)

**Status:** Reviewed against `CAP-007-recording.mp4` frame-by-frame (1s resolution, using the video's
burned-in wall-clock overlay) and cross-checked against `CAP-007-btsnoop_hci.log` via `tshark`/Wireshark.
Corrects and extends the original draft. See `CAP-007-FINDINGS.md` in this same folder for the
standardized, evidence-graded protocol findings extracted from this correlation — this file is
the *event timeline*, `CAP-007-FINDINGS.md` is *what it means for the protocol*.


## Log Metadata

|      Field       |                    Value                     |
|------------------|----------------------------------------------|
|    Capture ID    |                   `CAP-007`                   |
|      Group(s)    | S (`GFPS-001` — GMS disabled, no Pixel Buds app) + bonus classic pairing |
|       Date       |                  2026-08-11                  |
| Firmware version | release_5.203 |
|   Test device    | Pixel 7a, Android 17 — **nRF Connect** (generic BLE tool) for the first phase, then Android system Bluetooth settings (no Pixel Buds app at any point — app version not applicable, uninstalled) |
| Video file       | `CAP-007-recording.mp4` — 155.4s, starts 06:22:36, ends 06:25:12 (wall clock, +0200) |
| Log file         | `CAP-007-btsnoop_hci.log` — 342.3s, 06:22:04.23–06:27:46.48 (wall clock, +0200), 2,921 packets. Contains a mix of Buds traffic and **unrelated background traffic from a Fitbit Charge 6** (a different device on the same phone) — see the correction note in `CAP-007-FINDINGS.md` §1. |

## Event Timeline

| Time | Action / Event | Initiator | Test-ID | Evidence in `CAP-007-btsnoop_hci.log` |
|----------|---|---|---|---|
| 09:14:10 | Start video recording. Screen shows the system **Bluetooth** settings sheet, **"Bluetooth is off"**, `Use Bluetooth` toggled off | — | — | — |
| 09:14: | Tap **Use Bluetooth** toggle on | User (Settings) |  |  |
| 09:14:14 | User turns on Bleutooth | — | — | — |
| 09:14:18 | Pixel Buds pro 2 connects to phone| — | — | — |
| 09:14:36 | User removes pixel bud from left ear.| — | — | — |
| 09:16:44 | User closes case | — | — | — |
| 09:17:57 | End video recording  | — | — | — |
