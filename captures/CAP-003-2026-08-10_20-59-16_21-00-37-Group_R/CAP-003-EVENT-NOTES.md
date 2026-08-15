# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group R Capture (`CAP-003`)

**Status:** Reviewed against `CAP-003-recording.mp4` frame-by-frame (1s resolution, using the video's
burned-in wall-clock overlay) and cross-checked against `CAP-003-btsnoop_hci.log` via `tshark`/Wireshark.
Corrects and extends the original draft. See `CAP-003-FINDINGS.md` in this same folder for the
standardized, evidence-graded protocol findings extracted from this correlation — this file is
the *event timeline*, `CAP-003-FINDINGS.md` is *what it means for the protocol*.

## Log Metadata

|      Field       |                    Value                     |
|------------------|----------------------------------------------|
|    Capture ID    |                   `CAP-003`                   |
|      Group(s)    | R (forced GATT rediscovery, via nRF Connect) — see `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group R |
|       Date       |                  2026-08-10                  |
| Firmware version | release_5.203 |
|   Test device    | Pixel 7a, Android 17 — **nRF Connect** (generic BLE scanner/GATT browser app), with the official Pixel Buds app (v1.0.955078536) taking over partway through, not the primary tool this time |
| Video file       | `CAP-003-recording.mp4` — 81.1s, starts 20:59:16, ends 21:00:37 (wall clock, +0200) |
| Log file         | `CAP-003-btsnoop_hci.log` — 302.2s, 20:58:57.10–21:03:59.33 (wall clock, +0200), 2,863 packets — a short, freshly-restarted log (unlike `CAP-002`'s shared 8h20m log), so no slicing-out-unrelated-devices problem this time |

**Scope note (per the maintainer's test design):** the goal of this session was **not** command
attribution — it was to force a fresh GATT service/characteristic discovery by removing the
existing pairing beforehand (via system Bluetooth settings) and connecting with **nRF Connect**
(a generic BLE tool) instead of the official app, specifically to resolve two UUID gaps left open
by `CAP-002`'s `CAP-002-FINDINGS.md` §4/§7: handle `0x0f2a` (already known to return `"Revision 6"`) and
the `0x0c0X` handle cluster (the Key-based-Pairing-shaped write/notify bursts). **Whether that
goal was actually achieved is a `CAP-003-FINDINGS.md` question, not an event-log question — see
`CAP-003-FINDINGS.md` §1 for the result.** A full classic Secure Simple Pairing exchange was expected as
a side effect of clearing the pairing (both the classic and BLE bond were removed) and is treated
as a welcome bonus `PAIR-001`/`PAIR-002` data point, not the main subject of this session.

## Event Timeline

Timestamps are the video's own on-screen wall-clock overlay (1fps sampling, refined to
sub-second precision around each tap by extracting additional frames at the exact second); the
log uses the same wall clock (+0200), directly comparable. **UI note:** most of this session's
screenshots show **nRF Connect's** UI (blue theme, "Devices / SCANNER / BONDED / ADVERTISER"
tabs, a "CLIENT" GATT browser tab) — visually unrelated to the Pixel Buds app's own UI seen in
`CAP-002`. The official Pixel Buds app only appears from ~21:00:04 onward, once its own setup
flow is triggered.

| Time | Action / Event | Initiator | Test-ID | Evidence in `CAP-003-btsnoop_hci.log` |
|---|---|---|---|---|
| 20:59:16 | Start video recording. Android Bluetooth **"Saved devices"** screen shown (system settings, not nRF Connect) — confirms Pixel Buds Pro 2 is **not** in the list, i.e. genuinely unpaired at session start | User (App) | — | — |
| 20:59:23 | Switch to **nRF Connect**, `SCANNER` tab, "Devices / STOP SCANNING" — only an unrelated `HUAWEI Band 6` visible so far | User (App) | — | — |
| ~20:59:24 | Charging case opened (per original draft note; buds visible with case LED lit in later frames) | User (Hardware) | `CASE-003` | — |
| 20:59:29 | System-level **Fast Pair "half-sheet" card** appears over nRF Connect's scanner list: **"Pixel Buds Pro 2 will appear on devices linked with ted.sluis@gmail.com"**, with `Close`/`Connect` buttons — this is Android's own Fast Pair UI, not part of nRF Connect | App (Auto) | — | A BLE `LE Extended Create Connection` follows shortly after (20:59:38.31, frame 1612) — the ~9s gap between the card appearing and the BLE connect is consistent with the user reading the card before acting (exact tap moment not captured in 1fps sampling) |
| 20:59:38 | (log-only) Fresh BLE connection + classic pairing sequence begins | — | `PAIR-001` | See `CAP-003-FINDINGS.md` §1 for the full pairing sequence |
| 20:59:42 | nRF Connect now shows the device as **`CONNECTED` / `BONDED`**, GATT `CLIENT` tab listing `Generic Attribute (0x1801)`, `Generic Access (0x1800)`, `Broadcast Audio Scan Service (0x184F)` as primary services (only 3 visible on screen, not scrolled further) | App (Auto) | `PAIR-002` | RFCOMM/SDP/ATT activity in progress — see `CAP-003-FINDINGS.md` §2 |
| 20:59:43–54 | System dialog **"Ready to use — Pixel Buds Pro 2 is connected to someone else's account. That person can locate it in Find Hub..."** with options `Ask owner to share device` / `Remove previous owner` / `Start using the device` | App (Auto) | — | Fast-Pair ownership-transfer flow — the device was previously linked to a Google account from earlier test sessions; this is expected, not a new finding |
| 20:59:45 | Notification banner: **"Pixel Buds Pro 2 — Left 100% Case 38% Right 100%"** appears on top of the "Ready to use" dialog | App (Auto) | — | — |
| ~20:59:54 | User taps **"Start using the device"** | User (App) | — | — |
| 20:59:56–21:00:04 | **"Set up device — Your device is ready to be set up"** card shown (Android system Fast Pair card, same as seen in `CAP-002`), still layered over nRF Connect's unchanged 3-service GATT list | App (Auto) | — | — |
| 21:00:04 | Tap **Set up** — this hands off from nRF Connect/system UI to the **official Pixel Buds app**, which takes over the screen from here on | User (App) | `APP-001` | — |
| 21:00:05–14 | Pixel Buds app's own **"Allow a connection to your Pixel Buds"** permission screen | App (Auto) | — | — |
| 21:00:14 | Tap **Continue** | User (App) | `APP-002` | — |
| ~21:00:20 | System **CompanionDeviceManager** dialog: "Allow the app Pixel Buds to access Pixel Buds Pro 2 van Ted?" → tapped **Allow** (per original draft; consistent with the identical `CAP-002` flow) | User (App) | — | — |
| 21:00:37 | **"Device details"** screen loaded in the official app: Left 100% / Case 38% / Right 100%, `Forget`/`Disconnect`, Digital assistant / Find device / Controls and gestures / Sound / Hearing wellness menu | App (Auto) | — | — |
| 21:00:37 | End video recording | — | — | — |

## Corrections vs. the original draft of this file

- The original draft attributed several early steps to "Pixel Buds app" (e.g. "20:59:29 Pixel
  Buds app: request to connect", "20:59:38 Pixel Buds app: user confirms to connect") — video
  review shows these were actually **nRF Connect and Android system UI** (the Fast Pair
  half-sheet card and nRF Connect's own scanner/connect flow), not the Pixel Buds app. The Pixel
  Buds app does not visibly appear until **21:00:04** (the "Set up" tap hands off to it). This
  matters for correlation: anything logged before 21:00:04 should not be attributed to the
  official app's own connection logic.
- Filled in the log-only pairing sequence (20:59:38–39) that the draft only summarized as "user
  comfirms to connect" — see `CAP-003-FINDINGS.md` §1 for the frame-level detail.
- Corrected minor typos from the draft ("comfirms" → "confirms") and added the missing
  intermediate steps: the "Ready to use / connected to someone else's account" ownership-transfer
  dialog (20:59:43–54) and nRF Connect's own service list (20:59:42) were not in the original
  timeline at all.
- The draft's timestamps were otherwise accurate to within 1–2s of the refined values above.
