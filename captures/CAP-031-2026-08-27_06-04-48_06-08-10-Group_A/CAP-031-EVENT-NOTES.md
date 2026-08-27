# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group A repeat, logging started before any prior association (`CAP-031`)



## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-031`                     |
|      Group(s)    |                    A (repeat)                      |
|       Date       |                     2026-08-26                     |
| Firmware version | release_5.203 (screen-confirmed, see timeline) |
|   Test device    | Pixel 7a, Android 17 (⚪ assumed — build number not screen-confirmed this session), system Bluetooth settings + Pixel Buds companion app for setup steps only |
| Video file       | `CAP-031-recording.mp4` —  |
| Log file         | `CAP-031-btsnooz_hci.log` —  |

## Event Timeline

Timestamps are the video's own on-screen wall-clock overlay (bottom-right, seconds-resolution); the log
uses the same wall clock (+0200), independently confirmed (`date`/`TZ` on the analysis machine and the
log's own `+0200` frame timestamps both agree, matching the video overlay convention). Screenshots
validated via `ffmpeg -y -ss <offset> -i CAP-031-recording.mp4 -frames:v 1 <out>.png` at 1s precision
around every timestamp below (video start = 06:04:48, so offset = wall-clock time − 06:04:48).

| Time     | Action | Initiator | Test-ID | Wire evidence / Notes |
|----------|---|---|---|---|
| 06:04:48 | start of video `CAP-031-recording.mp4`. | - | - | - |
| 06:04:55 | user enabled Bluetooth. | User | `PAIR-001`/`PAIR-004` | - |
| 06:05:07 | user opens "Device details" for "Pixel Buds Pro 2 van Ted" (Bluetooth quick-settings panel). Screenshot-confirmed. | User | `PAIR-001`/`PAIR-004` | - |
| 06:05:15 | user opens "Firmware update" screen: shows `release_5.203` for Left/Right/Case. Screenshot-confirmed, screen-confirms firmware baseline. | User | - | - |
| 06:05:23 | "Device details" screen shown (Left 100%/Case 68%/Right 100%), with per-device trash-can **"Forget"** button (not a phone-wide reset). Screenshot-confirmed. | User | `PAIR-004` | Confirms this session used the narrow, per-device Forget path, unlike `CAP-013` |
| 06:05:27 | user taps **"Forget"** — "Forget device? / Your phone will no longer be paired with Pixel Buds Pro 2 van Ted" confirmation dialog appears. Screenshot-confirmed. | User | `PAIR-004` | - |
| 06:05:31 | user taps **"Forget device"** (confirmation), finger visible on button in screenshot. **This is the primary-question clearing action.** | User | `PAIR-004` | Log's first frame (06:06:37.16) is 66s *after* this — not captured, see §0 of `CAP-031-FINDINGS.md` |
| 06:05:33 | "Connected devices" screen shown; "Saved devices" section present but empty of the Buds entry (Forget succeeded). Screenshot-confirmed. | - | `PAIR-004` | - |
| 06:05:38 | user selects "Saved devices" → "See all" — confirmed empty list. | User | `PAIR-004` | - |
| 06:06:01 | user opens the Buds case (screenshot-confirmed: case visible open, phone still on "Connected devices"). | User | `PAIR-001` | - |
| 06:06:02 | user presses the pair button on the case. | User | `PAIR-001` | - |
| 06:06:14 | user checks "Saved devices" again. | User | - | - |
| 06:06:20 | user selects "Pair new device" (1st attempt). | User | `PAIR-001` | - |
| 06:06:21 | "Pair new device" screen shows phone's Bluetooth address `E8:D5:2B:7E:CA:81`. | - | - | - |
| 06:06:23 | Buds' case LED stops blinking. | - | - | - |
| 06:06:37 | **Log's first frame** (06:06:37.159791). Screenshot-confirmed: phone already on "Pair new device" → "Available devices" screen, listing `[TV] Samsung 5 Series`, `vuart:ktunnel`, `DoorLocker` — Buds not yet listed. | - | - | Log begins mid-way through the 1st scan attempt, not at/before the Forget tap — see `CAP-031-FINDINGS.md` §0 |
| 06:06:54 | "Pair new device" screen — Buds still not shown in "Available devices" list (1st attempt fails). Screenshot-confirmed. | - | `PAIR-001` | - |
| 06:07:03 | user re-selects "Pair new device" (2nd attempt). | User | `PAIR-001` | - |
| 06:07:04 | "Pair new device" screen again shows phone's Bluetooth address `E8:D5:2B:7E:CA:81`. | - | - | - |
| 06:07:07 | user presses the pair button on the case a second time. | User | `PAIR-001` | LE Extended Advertising Reports from the Buds' public address (`04:00:6e:cf:6e:07`) begin at 06:07:09.59 (frame 423), ~2.5s later |
| 06:07:10 | "Pixel Buds Pro 2 van Ted" appears in "Available devices". | - | `PAIR-001` | - |
| 06:07:11 | Pop-up appears: "Pair with Pixel Buds Pro 2". | - | - | - |
| 06:07:15 | user taps **"Connect"** on the pairing pop-up. | User | `PAIR-001`/`PAIR-004` | Matches `Sent Delete Stored Link Key` at 06:07:15.111 (frame 598) to within 1s — full fresh-SSP sequence follows, frames 598–689, see `CAP-031-FINDINGS.md` §2 |
| 06:07:25 | "Ready to use" pop-up appears. | - | - | - |
| 06:07:27 | user taps "Start using the device" — Buds now paired. | User | `PAIR-001` | - |
| 06:07:28 | "Set up device" pop-up appears. | - | - | - |
| 06:07:30 | user taps "Set up" — "Allow a connection to your Pixel Buds" permission screen appears. | User | `APP-001`/`APP-002` | - |
| 06:07:41 | user taps "Continue" — "Allow your Pixel to find..." pop-up appears. | User | `APP-001`/`APP-002` | DLCI 0x02 already opened 22s earlier (06:07:19.2) — this session's DLCI-0x02 open time is not gated on this permission step, see `CAP-031-FINDINGS.md` §5 |
| 06:07:44 | user taps "Allow" — "Allow the Pixel Buds app to access the Pixel Buds Pro 2 Bud van Ted" pop-up appears. | User | `APP-001`/`APP-002` | - |
| 06:07:58 | Firmware screen re-shown: `release_5.203` confirmed again, status bar now shows Bluetooth/headset icons (paired). Screenshot-confirmed. | - | - | - |
| 06:08:10 | end of video `CAP-031-recording.mp4` (video duration 202.4s from 06:04:48 start, matches). | - | - | - |

## Corrections vs. the original draft of this file

- The original draft's `ffmpeg` note had a typo'd video filename (missing a leading zero in the
  capture ID) — corrected to `CAP-031-recording.mp4` above.
- Every row's "Wire evidence / Notes" column was empty in the original draft (`- | - | -`) — filled
  in above from screenshot verification (`ffmpeg` frame extraction at each timestamp) and
  `tshark`/`capinfos` log correlation, per this session's Step 0/Step 2/Step 3.
- The original draft did not distinguish that the Forget action was the narrow, per-device
  "Forget" (not a broader reset) — this is now explicitly screenshot-confirmed at 06:05:23/27/29/31,
  which matters directly for `PAIR-004` (see `CAP-031-FINDINGS.md` §0).
- Added the Step 0 finding that the log's own first frame (06:06:37.159791) lands 66s after the
  06:05:31 Forget tap, and screenshot-confirmed what is already on screen at that exact second
  (mid-way through the 1st "Pair new device" scan attempt) — this was not noted in the original
  draft at all.
- Added `Test-ID` attributions (`PAIR-001`, `PAIR-004`, incidental `APP-001`/`APP-002`) per the
  traceability check below; the original draft's `Test-ID` column was entirely empty.

## Traceability check (`AGENTS.md` §13 point 7, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s Group-A Test-IDs)

Group A's Test-IDs per `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §5 and `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s
Group-A-repeat note: `PAIR-001`, `PAIR-004`, incidental `BATT-004`.

- **`PAIR-001`** (pairing/bonding handshake, forget-and-re-pair baseline) — ✅ exercised and
  captured in full: the Forget tap (06:05:31, on-screen only, not logged), case-open/pair-button
  sequence (06:06:01–06:07:07), and the successful classic pairing handshake (06:07:15–16.45,
  logged in full, `CAP-031-FINDINGS.md` §2). Note the Forget action *itself* is on-screen evidence
  only, per §0's logging-gap finding — it is not wire-confirmed as an isolated event.
- **`PAIR-004`** (does Forget clear a pre-existing BLE association/link-key completely, even one
  not established via the Pixel Buds app?) — ⚠️ **partially exercised**: the primary claim (state
  *before* the Forget tap) is **not answered** — the log starts 66s after the tap (§0). The
  secondary claim (does the subsequent re-pairing show a fresh SSP handshake?) **is** exercised and
  answered: 🟢 CONFIRMED fresh SSP, no key reuse (`CAP-031-FINDINGS.md` §2/§7 Test B) — a sixth
  confirming instance, consistent with `CAP-013`'s result for the same secondary question.
- **`BATT-004`** (battery data via RFCOMM after connecting) — ⚠️ **incidentally touched, not
  usefully exercised**: `Group 0x0e Code 0x01`/`0x02` battery-push traffic is present at connection
  time on DLCI `0x08` (frames 992/994, `CAP-031-FINDINGS.md` §4), but the Length/Value bytes are
  truncated by this log's ~15-byte capture cap (§1) — presence only, no decodable battery value
  this session, same gap as `CAP-013`.

No Test-ID expected for this Group was left entirely unreferenced in the timeline above.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-031-2026-08-27_06-04-48_06-08-10-Group_A/CAP-031-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-031-2026-08-27_06-04-48_06-08-10-Group_A/CAP-031-EVENT-NOTES
