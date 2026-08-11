# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group S Capture (`CAP-004`)

**Status:** Reviewed against `recording.mp4` frame-by-frame (1s resolution, using the video's
burned-in wall-clock overlay) and cross-checked against `btsnoop_hci.log` via `tshark`/Wireshark.
Corrects and extends the original draft. See `FINDINGS.md` in this same folder for the
standardized, evidence-graded protocol findings extracted from this correlation — this file is
the *event timeline*, `FINDINGS.md` is *what it means for the protocol*.

**⚠️ Procedure deviation from the Group S description (flagged, not silently corrected):** per
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group S, this session was meant to pair via system Bluetooth
settings only, with no app and no BLE tool. The video actually shows **nRF Connect** used first
(BLE scan, connect, GATT browsing) at 06:22:44–06:24:01, and only *then* a classic pairing via
system Bluetooth settings at 06:24:06–29. Both the Google Play Services disabled and Pixel Buds
app uninstalled conditions are confirmed (see below), so the core `GFPS-001` test condition
still held — but the capture is not a "system-settings-only" session as originally described.
This is documented here rather than silently reconciled, since it affects how §-references below
should be read.

## Log Metadata

|      Field       |                    Value                     |
|------------------|----------------------------------------------|
|    Capture ID    |                   `CAP-004`                   |
|      Group(s)    | S (`GFPS-001` — GMS disabled, no Pixel Buds app) + bonus classic pairing |
|       Date       |                  2026-08-11                  |
| Firmware version | unknown — not visible in this capture, TBD |
|   Test device    | Pixel 7a — **nRF Connect** (generic BLE tool) for the first phase, then Android system Bluetooth settings (no Pixel Buds app at any point) |
| Video file       | `recording.mp4` — 155.4s, starts 06:22:36, ends 06:25:12 (wall clock, +0200) |
| Log file         | `btsnoop_hci.log` — 342.3s, 06:22:04.23–06:27:46.48 (wall clock, +0200), 2,921 packets. Contains a mix of Buds traffic and **unrelated background traffic from a Fitbit Charge 6** (a different device on the same phone) — see the correction note in `FINDINGS.md` §1. |

## Event Timeline

Timestamps are the video's own on-screen wall-clock overlay; the log uses the same wall clock
(+0200), directly comparable. **UI note:** frames 06:22:36–06:24:01 show **nRF Connect** (blue
theme); 06:24:01 onward shows **Android system Bluetooth settings** (light theme, no
third-party app UI at all — confirming the Pixel Buds app truly never appears in this session).

| Time | Action / Event | Initiator | Test-ID | Evidence in `btsnoop_hci.log` |
|---|---|---|---|---|
| 06:22:36 | Start video recording. Android Bluetooth **"Saved devices"** screen shown — confirms Pixel Buds Pro 2 is **not** in the list, i.e. genuinely unpaired at session start | User (App) | — | — |
| 06:22:44 | Switch to **nRF Connect**, `SCANNER` tab, scan running | User (App) | — | — |
| ~06:22:45 | Charging case opened (buds visible, case LED lit in subsequent frames) | User (Hardware) | `CASE-003` | — |
| 06:22:51 | nRF Connect scan list shows **"Pixel Buds Pro 2 van Ted"**, `04:00:6E:CF:6E:07`, `NOT BONDED`, `-30 dBm` | App (Auto) | — | — |
| ~06:23:00 | Tap **CONNECT** on the Buds entry | User (App) | — | `LE Extended Create Connection` at 06:23:01.558 (frame 1497) |
| 06:23:01 | nRF Connect shows `CONNECTED` / `NOT BONDED`; `CLIENT` GATT tab lists 9 primary services (Generic Attribute, Generic Access, Broadcast Audio Scan Service, Audio Stream Control Service, Published Audio Capabilities Service, Volume Control, Microphone Control, Audio Input Control, Common Audio Service) — **not from live discovery, see `FINDINGS.md` §1** | App (Auto) | `PAIR-001`(BLE) | `Sent Read By Type Request, Database Hash` (frame 1524) — cached-database check, not fresh discovery |
| 06:23:01 | System notification: **"Enable Google Play services — Google Play services must be enabled for..."** persists on screen from here through the end of the video | OS (Auto) | — | Confirms GMS is genuinely disabled/non-functional for this flow — matches the test setup |
| 06:23:55–56 | User scrolls the service list further: `Telephony and Media Audio Service`, **`Google Fast Pair Service (0xFE2C)`**, **`Accessory Non-Owner Service (13190001-12f4-c226-88ed-2ac5579f2a85)`**, `Device Information (0x180A)`, `Battery Service (0x180F)`, and an unnamed **`Unknown Service (109b8b21-50e3-45cc-8ea1-ac62de4846d1)`** | User (App) | — | Same caveat as above — this list is UI evidence of the cached GATT database, not of live discovery in this session |
| 06:24:01 | Open Android Bluetooth settings from the notification shade | User (App) | — | — |
| 06:24:06 | System settings → **"Pair new device"**, classic inquiry starts | User (App) | — | — |
| ~06:24:07 | "Pixel Buds Pro 2 van Ted" appears in the classic device list | App (Auto) | — | — |
| 06:24:24 | Tap **"Pixel Buds Pro 2 van Ted"** in the list | User (App) | `PAIR-002` | `Delete Stored Link Key` (frame 1854, 06:24:24.668) → SMP `Pairing Request` (frame 1856, 06:24:24.672, LE Secure Connections, requesting `Linkkey` key distribution — Cross-Transport Key Derivation) — see `FINDINGS.md` §2 |
| 06:24:25 | System **"Pair with Pixel Buds Pro 2 van Ted?"** dialog appears | App (Auto) | — | Coincides with the SMP Public-Key/Confirm/Random exchange (frames 1869–1879) |
| 06:24:29 | Tap **Pair** | User (App) | — | SMP `DHKey Check` completes (frames 1880/1882) immediately before; classic `Create Connection` sent at 06:24:29.878 (frame 1891) |
| 06:24:30 | Switch back to nRF Connect | User (App) | — | Classic `Connect Complete` (frame 1933), `Link Key Request` → **`Reply`** (not Negative — frames 1969–1977, using the CTKD-derived key), `Authentication Complete`, `Encryption Change` (frame 2037) — RFCOMM channels 0/6/4/5 open through 06:24:31.04 |
| 06:24:59 | nRF Connect shows the device as `CONNECTED`, `BONDED` | App (Auto) | — | — |
| 06:25:04 | Open the Buds' device-details page in **system Bluetooth settings** (not nRF Connect, not a Pixel Buds app screen) | User (App) | — | — |
| 06:25:11 | System settings device page: **"Pixel Buds Pro 2 van Ted — Active. 100% battery."**, `Forget`/`Disconnect`, `Phone calls`/`Media audio`/`Input device` toggles (all on), "Allow access to contacts..." toggle, no ANC/EQ/Sound section at all (unlike the Pixel Buds app's own Device details screen in `CAP-002`/`CAP-003`) | App (Auto) | — | Confirms this is the generic Android per-device Bluetooth settings page, not any Buds-specific app UI — consistent with no Pixel Buds app being installed |
| 06:25:12 | End video recording | — | — | — |

## Corrections vs. the original draft of this file

- Fixed the video filename reference (draft said `recording_3.mp4`; the actual file in this
  folder is `recording.mp4`).
- Added the explicit procedure-deviation note above (nRF Connect *and* system settings were both
  used, not system-settings-only as Group S's written procedure describes).
- The draft's claim that "GATT Discovery complete (services visible)" happened live at 06:23:01
  is corrected — per `FINDINGS.md` §1, the wire log shows only a cached-database-hash check at
  that moment, not a live `Read By Group Type` discovery. The service names are real (matching
  the device's actual GATT layout) but sourced from Android's cache, not from traffic in this
  capture.
- Added the classic pairing's log-level detail (SMP/LE Secure Connections + Cross-Transport Key
  Derivation, not classic SSP) — the original draft only noted the on-screen dialog, not the
  underlying mechanism, which turns out to be different from `CAP-002`'s/`CAP-003`'s classic-SSP
  pattern. See `FINDINGS.md` §2.
- The draft's timestamps were otherwise accurate to within 1–2s of the refined values above.
