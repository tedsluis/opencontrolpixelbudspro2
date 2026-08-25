# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group W Capture (`CAP-010`)

**Status:** Reviewed against `CAP-010-recording.mp4` frame-by-frame (1s resolution, using the
video's burned-in wall-clock overlay) and cross-checked against `CAP-010-btsnoop_hci.log` via
`tshark`/Wireshark. Corrects and extends the original draft. See `CAP-010-FINDINGS.md` in this
same folder for the standardized, evidence-graded protocol findings extracted from this
correlation — this file is the *event timeline*, `CAP-010-FINDINGS.md` is *what it means for the
protocol*.

**⚠️ Procedure note (see `CAP-010-FINDINGS.md` §1 for the full evidenced writeup):** this session
is filed under Group W, but the video and log both show a standard "Forget Buds, re-pair via the
official app" flow on the **same** Pixel 7a used for `CAP-001`–`CAP-007` — not Group W's actual
defined cache-busting method (`adb shell pm clear com.android.bluetooth`, or a first-ever
connection from the project's Pixel 9a). This does not make the capture useless — it independently
reproduces several prior findings — but it does **not** achieve Group W's stated goal of forcing a
live GATT discovery.

## Log Metadata

|      Field       |                       Value                        |
|------------------|----------------------------------------------------|
|    Capture ID    |                      `CAP-010`                     |
|      Group(s)    | W (`GATT-001` — stronger GATT cache-busting, **attempted but not executed as designed**, see procedure note above) + incidental `PAIR-001` (fresh forget-and-re-pair) |
|       Date       |                     2026-08-16                     |
| Firmware version |  `release_5.203` (confirmed on-the-wire this session too — `CAP-010-FINDINGS.md` §5) |
|   Test device    |    Pixel 7a, Android 17, Official Pixel Buds Companion App (version not visible on screen this capture) — same physical phone as `CAP-001`–`CAP-007` (BD_ADDR `E8:D5:2B:7E:CA:81`, visible on-screen at 11:42:58/11:43:03)    |
| Video file       | `CAP-010-recording.mp4` — 149.9s, starts 11:42:31, ends ~11:45:01 (wall clock, +0200) |
| Log file         | `CAP-010-btsnoop_hci.log` — 350.7s, 11:41:45.67–11:47:36.40 (wall clock, +0200), 2,903 packets — extends well past the video's end; see the final timeline row and `CAP-010-FINDINGS.md` §5 |

**Scope note:** this session's on-screen action is a single, continuous "forget-and-re-pair via
system Bluetooth settings, then complete the official app's setup flow" sequence — no ANC/EQ
actions, no GATT tool (nRF Connect/WebBluetooth), no `pm clear`. The peer device confirmed as the
same physical Buds/case used throughout this project (BD_ADDR `04:00:6E:CF:6E:07`,
`Google_cf:6e:07`, first EIR name match frame 652).

## Event Timeline

Timestamps are the video's own on-screen wall-clock overlay (±1s, since frames were sampled at
1fps); `CAP-010-btsnoop_hci.log` uses the same wall clock (+0200), so times below are directly
comparable to log frame timestamps without offset correction.

| Time | Action / Event | Initiator | Test-ID | Evidence in `CAP-010-btsnoop_hci.log` |
|---|---|---|---|---|
| 11:42:31 | Start video recording. Phone screen shows lock/"Bluetooth is off" screen; Buds case closed on the table | — | — | — |
| 11:42:37–38 | User turns Bluetooth on (quick-settings panel shown: `Use Bluetooth` on, saved devices `Charge 6`, `Light-HD`, `OpenMeet by Shokz` listed, `Pair new device` option visible) | User (App) | — | — |
| 11:42:51–52 | User taps **Pair new device** | User (App) | — | — |
| 11:42:57–58 | User opens the Pixel Buds Pro 2 case; screen still shows the empty "Pair new device" scan list at this point | User (Hardware) | `CASE-003` | First `LE Extended Advertising Report`s from this device begin shortly after (log continuously receives LE adverts from ~11:41 onward from many nearby devices; the Buds' own classic EIR name is not visible until frame 652 below) |
| 11:43:02–03 | **Pixel Buds Pro 2 van Ted** appears in the "Available devices" list | App (Auto) | — | Classic inquiry/EIR carries the name `Pixel Buds Pro 2 van Ted` from frame 652 (11:43:02.19) onward, repeating through frame 727 |
| 11:43:28 | User selects **Pixel Buds Pro 2 van Ted** in the list | User (App) | `PAIR-001` | `Delete Stored Link Key` (frame 1236, 11:43:28.377) → `Create Connection` (frame 1238) → `Connect Complete`, status `0x00` (frame 1242, 11:43:28.649) |
| 11:43:29 | System popup: **"Pair with Pixel Buds Pro 2 van Ted?"** (Cancel / Pair) | App (Auto) | `PAIR-001` | `IO Capability Request/Response` (frames 1270–1276, 11:43:28.68–28.72); `User Confirmation Request` received 11:43:28.962 (frame 1290) — the dialog is waiting on this |
| 11:43:34 | User taps **Pair** | User (App) | `PAIR-001` | `User Confirmation Request Reply` sent 11:43:34.925 (frame 1303) → `Simple Pairing Complete` 11:43:35.141 (frame 1305) → `Link Key Notification`/`Authentication Complete` (frames 1306–1307) → `Encryption Change` 11:43:35.251 (frame 1312) |
| 11:43:35–41 | (not directly visible on screen) SDP service discovery, RFCOMM channel setup, HFP AT-command handshake | App (Auto) | `PAIR-001` | SDP `Service Search Attribute Request/Response` for PnP/L2CAP/Phonebook/Audio Sink/HID/Hands-Free/AVRCP (frames 1320–1844); RFCOMM channels 0, 6 (Hands-Free), 4, 5, 1, 2 opened (frames 1483–1853); `AT+BRSF`…`AT+CMEE` handshake incl. `AT+BIEV=2,100` battery report (frames 1515–1680) |
| 11:43:40–44 | (not directly visible on screen) BLE GATT exchange — Fast Pair Key-based-Pairing-shaped write/notify cluster on handles `0x0c04`/`0x0c05`/`0x0c0a`/`0x0c0c`/`0x0c0d`/`0x0c13`/`0x0c14`, plus reads of `0x0f28` (`0x31`) and `0x0f2a` (`"Revision 6"`) — **no primary-service or characteristic discovery request anywhere** | App (Auto) / Buds (Auto) | `GATT-001` (attempted) | `LE Enhanced Connection Complete` 11:43:40.061 (frame 1965); full ATT exchange frames 1985–2089; see `CAP-010-FINDINGS.md` §2–§3 for the complete decode |
| 11:43:42 | Popup: **"Pixel Buds Pro 2 — Save device to [account] to connect more quickly to your other devices"** (Cancel / Save) | App (Auto) | — | — |
| 11:43:47 | Notification banner appears: **"Pixel Buds Pro 2: Left 100% Case 42% Right 100%"** (while the Save popup is still showing) | App (Auto) | — | `AT+BIEV=2,100` (frame 1671, 11:43:36.247) reports the same 100% aggregate battery indicator |
| ~11:43:57 | User taps **Save** | User (App) | — | — |
| ~11:43:58 | Popup: **"Allow a connection to your Pixel Buds — Continue"** (setup intro screen) | App (Auto) | — | — |
| 11:44:07 | User taps **Set up** (from a Pixel Buds app card, distinct from the Continue screen above) | User (App) | — | — |
| ~11:44:24 | User taps **Continue** on the "Allow a connection to your Pixel Buds" screen | User (App) | — | — |
| ~11:44:25–29 | System permission dialog: **"Allow the app Pixel Buds to access Pixel Buds Pro 2 van Ted?"** (Allow / Don't allow) | App (Auto) | — | — |
| ~11:44:33 | User taps **Allow** | User (App) | — | — |
| 11:44:34 | **Device details** screen shown: `Pixel Buds Pro 2 van Ted`, Left 100% / Case 42% / Right 100%, `Active`; Forget/Disconnect buttons; Digital assistant / Controls and gestures / Sound / Hearing wellness / More settings rows — setup complete | App (Auto) | — | — |
| ~11:45:00 | Device details screen still shown, unchanged (Left 100%, Case 42%, Right 100%) | — | — | — |
| 11:45:01 | End video recording | — | — | — |
| *(after video ends)* | `LE Disconnect Complete` at 11:43:49.236 (frame 2195, inside the video window but not a visible on-screen event) and a full RFCOMM channel teardown/reopen cycle at ~11:46:58–11:47:04 (frames 2604–2837, well after recording stopped) | — | — | See `CAP-010-FINDINGS.md` §5 |

## Corrections vs. the original draft of this file

- **Header/metadata block was internally inconsistent** — the original draft's title said "Group
  S Capture," the metadata table's `Group(s)` field started "S (`GFPS-001`" (both leftover from a
  different capture's template) while the folder itself is `Group_W`. Corrected to `W` throughout,
  matching the folder name and `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1's own Group W definition.
- **Battery percentage corrected: Case was 42%, not 47%.** The original draft's "Pixel Buds Pro 2:
  Lef 100%, Case 47% Right 100%" notification line does not match the video — the on-screen
  notification banner (11:43:47) and the final Device details screen (11:44:34 onward) both read
  **Case: 42%**, confirmed from two independent video frames.
- **Procedure gap flagged (new, not in the original draft):** the original draft did not note that
  this session's actual on-screen actions (system Settings "Pair new device," no `pm clear`, same
  phone as every prior capture) do not match Group W's own defined cache-busting procedure — see
  the callout at the top of this file and `CAP-010-FINDINGS.md` §1 for the full evidence.
- Added exact `CAP-010-btsnoop_hci.log` frame references for every event where log evidence
  exists, per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5's correlation workflow — the original draft had
  none.
- Split the original single "user selects 'set up'" / "user select continue" / "user selects Allow
  the app..." rows into their correct, distinct on-screen steps (a setup card tap, a permission
  intro screen with its own Continue button, and a system permission dialog with its own Allow
  button are three different UI moments, confirmed via 1s-frame sampling at 11:44:07, ~11:44:24,
  and ~11:44:33 respectively) — the original draft's times for these were already close (within
  the ±1s sampling tolerance) but the action descriptions were merged/imprecise.
- Added the BLE GATT exchange (`GATT-001`, 11:43:40–44) and the post-video log activity
  (11:43:49 disconnect, 11:46:58–11:47:04 reconnect cycle) — neither appeared in the original
  draft at all, despite being the entire stated purpose of a Group W capture.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-010-2026-08-16_11-42-31_11-45-01-Group_W/CAP-010-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-010-2026-08-16_11-42-31_11-45-01-Group_W/CAP-010-EVENT-NOTES
