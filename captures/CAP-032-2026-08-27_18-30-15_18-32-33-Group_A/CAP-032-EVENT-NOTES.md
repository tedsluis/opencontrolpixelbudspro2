# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group A repeat, logging started before any prior association (`CAP-032`)



## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-032`                     |
|      Group(s)    |                    A (repeat)                      |
|       Date       |                     2026-08-26                     |
| Firmware version | release_5.203 (screen-confirmed, see timeline) |
|   Test device    | Pixel 7a, Android 17, system Bluetooth settings + Pixel Buds companion app for setup steps only |
| Video file       | `CAP-032-recording.mp4` —  |
| Log file         | `CAP-032-btsnooz_hci.log` —  |

## Event Timeline

Timestamps are the video's own on-screen wall-clock overlay (bottom-right, seconds-resolution); the log
uses the same wall clock (+0200), independently confirmed (`date`/`TZ` on the analysis machine and the
log's own `+0200` frame timestamps both agree, matching the video overlay convention). Screenshots
validated via `ffmpeg -y -ss <offset> -i CAP-032-recording.mp4 -frames:v 1 <out>.png` at 1s precision
around every timestamp below (video start = 06:04:48, so offset = wall-clock time − 06:04:48).

| Time     | Action | Initiator | Test-ID | Wire evidence / Notes |
|----------|---|---|---|---|
| 18:30:15 | start of video `CAP-032-recording.mp4`. | - | - | - |
| 18:30:21 | user enabled Bluetooth. | User | `PAIR-001`/`PAIR-004` | - |
| 18:30:30 | user opens "Device details" for "Pixel Buds Pro 2 van Ted" (Bluetooth quick-settings panel).  | User | `PAIR-001`/`PAIR-004` | - |
| 18:30:31 | "Device details" screen shown (Left 100%/Case 57%/Right 100%), with per-device trash-can **"Forget"** button (not a phone-wide reset).  | User | `PAIR-004` |  |
| 18:30:37 | user taps **"Forget"** — "Forget device? / Your phone will no longer be paired with Pixel Buds Pro 2 van Ted" confirmation dialog appears.  | User | `PAIR-004` | - |
| 18:30:43 | user taps **"Forget device"** (confirmation), finger visible on button in screenshot. **This is the primary-question clearing action.** | User | `PAIR-004` |  |
| 18:30:44 | "Connected devices" screen shown | - | - | - | 
| 18:30:51 | User selets "See All", "Saved devices" section is present but empty of the Buds entry.| - | `PAIR-004` | - |
| 18:31:08 | user opens the Buds case, case visible open, phone still on "Connected devices". | User | `PAIR-001` | - |
| 18:31:11 | user presses the pair button on the case. | User | `PAIR-001` | - |
| 18:31:14 | Pop-up appears: "Pair with Pixel Buds Pro 2". | - | - | - |
| 18:31:24 | user taps **"Connect"** on the pairing pop-up. | User | `PAIR-001`/`PAIR-004` | | - | - | - |
| 18:31:36 | user taps "Start using the device" — Buds now paired. | User | `PAIR-001` | - |
| 18:31:37 | "Set up device" pop-up appears. | - | - | - |
| 18:31:46 | user taps "Set up" — "Allow a connection to your Pixel Buds" permission screen appears. | User | `APP-001`/`APP-002` | - |
| 18:31:59 | user taps "Continue" — "Allow your Pixel to find..." pop-up appears. | User | `APP-001`/`APP-002` | |
| 18:32:04 | user taps "Allow" — "Allow the Pixel Buds app to access the Pixel Buds Pro 2 Bud van Ted" pop-up appears. | User | `APP-001`/`APP-002` | - |
| 18:32:15 | Firmware screen re-shown: `release_5.203`  status bar now shows Bluetooth/headset icons (paired).  | - | - | - |
| 18:32:33 | end of video `CAP-032-recording.mp4` (video duration 202.4s from 06:04:48 start, matches). | - | - | - |

## Corrections vs. the original draft of this file
