# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group W Retry Capture (`CAP-017`, 18:30)

**Status:** Reviewed against `CAP-017-recording.mp4` frame-by-frame (sub-second resolution near
key actions, using the video's burned-in wall-clock overlay) and cross-checked against
`CAP-017-btsnoop_hci.log` via `tshark`. This is a **second, independent `CAP-017` capture**,
distinct from `captures/CAP-010-2026-08-16_11-42-31_11-45-01-Group_W/` (the 11:42 attempt, which
used the stock system Settings flow and produced zero discovery traffic — see that folder's
`CAP-010-FINDINGS.md`). This 18:30 session instead drove **nRF Connect for Mobile** as a
third-party GATT client against the Buds. See `CAP-017-FINDINGS.md` in *this* folder for the
standardized, evidence-graded protocol findings — this file is the *event timeline*.

## Log Metadata

|      Field       |                       Value                        |
|------------------|----------------------------------------------------|
|    Capture ID    |                      `CAP-017`                     |
|      Group(s)    | W (`GATT-001` — GATT discovery forced via a third-party client, nRF Connect — **not** either of Group W's originally-defined candidate methods (`pm clear`/Pixel 9a); see `CAP-017-FINDINGS.md` §1 for that distinction) |
|       Date       |                     2026-08-16                     |
| Firmware version |  `release_5.203` (not re-confirmed on-the-wire this session — no DLCI 0x08 handshake occurred, since the official Companion App's Message-Stream channel was never opened; see `CAP-017-FINDINGS.md` §4) |
|   Test device    |    Pixel 7a, Android 17 — same physical phone as `CAP-001`–`CAP-007`/the 11:42 `CAP-010`. **Client app: nRF Connect for Mobile (Nordic Semiconductor)** throughout the GATT-discovery portion; the official Pixel Buds Companion App is only visible briefly at 18:33:52–18:33:59 (its "Device details" screen, reached via the system quick-settings tile, not used to drive discovery) |
| Video file       | `CAP-017-recording.mp4` — 419.6s, 18:30:12–18:37:12 (wall clock, +0200) |
| Log file         | `CAP-017-btsnoop_hci.log` — 559.2s, 1,747 packets, 18:31:32.72–18:40:51.93 (wall clock, +0200) — **log starts ~80s after the video** (Bluetooth-on and nRF Connect launch predate the log's own first captured frame) **and continues ~3.7 min after the video ends** |

**Capture-integrity note (see `CAP-017-FINDINGS.md` §2 for full detail):** `CAP-017-btsnoop_hci.log`
was recorded with a very short per-ACL-packet snapshot length — most GATT discovery
request/response frames are cut off at ~15 captured bytes regardless of their true on-the-wire
length (confirmed via `frame.cap_len` vs `frame.len` and Wireshark's own "Length too short/
Malformed" expert warning on e.g. frame 680). **This means the actual UUID bytes inside this
session's `Read By Group Type`/`Read By Type` responses are not recoverable from the log file
itself**, even though genuine live discovery traffic — completely absent from every prior `CAP-010`
attempt — does appear on the wire this time. The full GATT service list in §3 below and in
`CAP-017-FINDINGS.md` was recovered instead via direct frame-by-frame reading of nRF Connect's own
on-screen service list in the video, which reflects what nRF Connect's/Android's Bluetooth stack
actually resolved over the air, independent of what our own capture tooling managed to record.

## Event Timeline

Timestamps are the video's own on-screen wall-clock overlay; `CAP-017-btsnoop_hci.log` uses the
same wall clock (+0200), so times below are directly comparable to log frame timestamps without
offset correction. Sub-second frame timestamps from the log are given where a specific frame was
matched to a video-visible action.

| Time | Action / Event | Initiator | Test-ID | Evidence in `CAP-017-btsnoop_hci.log` |
|----------|---|---|---|---|
| 18:30:12 | Start video recording. Phone shows lock/quick-settings screen, Bluetooth **off**, battery 80%; Buds case sits closed on top of the phone | — | — | Log has not started yet (first frame is 18:31:32.72) |
| 18:30:16–17 | User opens the Bluetooth quick-settings dialog ("Bluetooth is off") and taps **Use Bluetooth** | User (Phone) | — | — |
| 18:30:28 | nRF Connect for Mobile app launches (Nordic Semiconductor splash screen, "Adding SIG adopted characteristics") | User (App) | — | — |
| 18:30:57 | nRF Connect's **Scanner** tab is active, `SCAN`/`STOP SCANNING` running, "No devices found" yet | User (App) | — | — |
| 18:31:02 | User opens the Pixel Buds Pro 2 case (held open in hand, both buds visible/seated) | User (Hardware) | `CASE-003`-equivalent | — |
| 18:31:03 | User presses the pairing button on the back of the case | User (Hardware) | — | — |
| 18:31:32.72 | *(not visible on screen — log-only)* First frame captured in `CAP-017-btsnoop_hci.log`: dense burst of LE Extended Advertising Reports from ~20 nearby BLE devices, incl. `04:00:6e:cf:6e:07` ("Pixel Buds Pro 2 van Ted") from frame 2 onward | — | — | Frames 1–651 |
| 18:31:39 | User taps **CONNECT** next to "Pixel Buds Pro 2 van Ted" in nRF Connect's scanner list | User (App) | `GATT-001` | `LE Enhanced Connection Complete`, status `0x00`, handle `0x0002` — frame 652, **18:31:39.512655** (video and log agree to well under 1s) |
| 18:31:39.58–41.94 | *(not directly visible — GATT client working in the background)* Exchange MTU (517), then a burst of `Read By Type`/`Read By Group Type`/`Find Information` requests spanning the full `0x0001–0xffff` handle range — **this is genuine live GATT discovery, absent from every prior `CAP-010` attempt** | App (nRF Connect, Auto) | `GATT-001` | Frames 668–908 (`Exchange MTU Request` 668 → last `Find Information Response` 908); payload truncated in the log itself, see capture-integrity note above and `CAP-017-FINDINGS.md` §2 |
| 18:31:41–53 | nRF Connect's **CLIENT** tab populates with the resolved GATT service list (first 9 of 15 services visible without scrolling: Generic Attribute, Generic Access, Broadcast Audio Scan, Audio Stream Control, Published Audio Capabilities, Volume Control, Microphone Control, Audio Input Control, Common Audio Service) | App (Auto) | `GATT-001` | Not present in the log payload (truncated, see above) — read directly from video frames; see `CAP-017-FINDINGS.md` §3 for the full list |
| 18:31:42 | Read Request/Response, handle `0x0f28` → value `0x31` (1 byte) — same value as the 11:42 `CAP-010` and `CAP-002`/`CAP-003` | App (Auto) | — | Frames 911→913, 18:31:41.958–41.990 |
| 18:32:52–53 | User opens nRF Connect's overflow menu (`Bond`, `Refresh`, `Parse known characteristics`, `Clone device's services`, …) | User (App) | — | — |
| 18:32:57 | System SSP dialog appears: **"Pair with Pixel Buds Pro 2 van Ted?"** (contacts/call-history toggle, Cancel button) | App (Auto) | `PAIR-001` | — |
| 18:32:58 | User confirms pairing (off-screen tap, dialog dismisses) | User (App) | `PAIR-001` | Classic BR/EDR `Connect Complete`, status `0x00`, handle `0x0003` — frame 994, **18:32:58.425331** |
| 18:32:58.42–58.72 | *(not visible on screen)* A second short ATT burst runs immediately after classic pairing: `Read By Type`/`Find By Type Value` against low handles, a `Write Request` to handle `0x0005` (value `07`), and a `Find Information Response` identifying handle `0x0003` as the **Service Changed** characteristic | App (Auto) | `GATT-001` | Frames 960–1090, 18:32:58.150–58.719 |
| 18:33:22–29 | User scrolls nRF Connect's **CLIENT** service list to the bottom, revealing the remaining 6 services: Telephony and Media Audio Service, **Google Fast Pair Service**, **Accessory Non-Owner Service** (128-bit UUID), Device Information, Battery Service, **Unknown Service** (128-bit UUID) — then scrolls back up | User (App) | `GATT-001` | Not present in the log payload (truncated) — read directly from video frames (18:33:22, 18:33:25, 18:33:29); see `CAP-017-FINDINGS.md` §3 |
| 18:33:33 | User backgrounds nRF Connect; phone quick-settings panel shows a **"Pixel Buds Pro · 100% battery"** tile (classic profile now connected) | User (Phone) | — | — |
| 18:33:39 | *(not visible on screen)* BLE link torn down | — | — | `Disconnect Complete`, handle `0x0002`, reason `0x16` (Terminated by Local Host) — frame 1690, **18:33:39.461955** |
| 18:33:52–59 | User opens the official Pixel Buds Companion App's **Device details** screen (Forget/Connect, Phone calls/Media audio/Input device toggles) via the quick-settings tile | User (App) | — | — |
| 18:34:52–18:36:52 | User returns to nRF Connect, browses the **Bonded/Advertiser** entry for the Buds — RSSI-vs-time history graph and raw advertising-data log, including Fast Pair TLV fields (`Fast Pair: Version 0, Flags: None`) | User (App) | — | — |
| 18:37:11 | User is back on nRF Connect's **Scanner** tab, inspecting raw advertising data of an unrelated nearby device | User (App) | — | — |
| 18:37:12 | End video recording | — | — | — |

## Corrections vs. the original draft of this file

- The original draft's timestamps (18:30:12 through 18:37:12) all check out against the video's
  own wall-clock overlay to within 1s — no time corrections were needed.
- Added Test-ID tags and specific `CAP-017-btsnoop_hci.log` frame references for every event where
  log evidence exists, per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5's correlation workflow. Six events
  now carry a specific frame number; several more (the MTU/discovery burst, the post-pairing GATT
  re-registration) are new rows not present in the original draft.
- **Most significant addition:** the original draft ended at "18:33:53 user selects pixel buds pro
  2 Device details" with no indication of what nRF Connect actually showed. The 18:31:41–53 and
  18:33:22–29 rows above — read directly from the video, not the log — document that this session
  **did achieve genuine live GATT discovery**, recovering a full 15-service profile including two
  previously-undocumented 128-bit UUIDs. See `CAP-017-FINDINGS.md` for the complete analysis.
- **Important caveat surfaced during this pass, not present in the original draft:**
  `CAP-017-btsnoop_hci.log` itself is critically truncated at the ACL-payload level for nearly
  every discovery-related ATT frame, so the "Evidence in log" column for the GATT-population rows
  above points to the video, not the log — the log confirms discovery *happened* (via the ATT
  opcodes and timing) but not *what was discovered*. See the capture-integrity note above.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-017-2026-08-16_18-30-12_18-37-12-Group_W/CAP-017-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-017-2026-08-16_18-30-12_18-37-12-Group_W/CAP-017-EVENT-NOTES
