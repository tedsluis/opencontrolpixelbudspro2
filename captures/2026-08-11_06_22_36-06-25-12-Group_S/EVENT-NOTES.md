# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group S Capture (`CAP-004`)

**Status:** Reviewed against `recording_3.mp4` frame-by-frame (1s resolution, using the video's
burned-in wall-clock overlay) and cross-checked against `btsnoop_hci.log` via `tshark`/Wireshark.
Corrects and extends the original draft. See `FINDINGS.md` in this same folder for the
standardized, evidence-graded protocol findings extracted from this correlation — this file is
the *event timeline*, `FINDINGS.md` is *what it means for the protocol*.

## Log Metadata

|      Field       |                    Value                     |
|------------------|----------------------------------------------|
|    Capture ID    |                   `CAP-004`                   |
|      Group(s)    | S (forced GATT rediscovery, with play services disabled and without pixel buds app) |
|       Date       |                  2026-08-11                  |
| Firmware version | unknown — not visible in this capture, TBD |
|   Test device    | Pixel 7a —  |
| Video file       | `recording_3.mp4` —  |
| Log file         | `btsnoop_hci.log` |

## Event Timeline

*Timestamps based on video wall-clock overlay.*

| Time     | Action / Event | Initiator | Expected Transport / Protocol | Notes |
|----------|----------------|-----------|-------------------------------|-------|
| `06:22:36` | Start video recording. Android Bluetooth **"Saved devices"** screen shown | User (App) | UI state | Confirms Pixel Buds Pro 2 is **not** in the list, i.e., genuinely unpaired at session start. |
| `06:22:44` | nRF Connect app: "STOP SCANNING" button pressed / scan active | User (App) | BLE | Switch to nRF Connect, `SCANNER` tab. |
| `06:22:45` | Charging case: Opened | User (Hardware) | BLE Advertisement | Case opened. |
| `06:22:46` | Charging case: Pairing button pressed | User (Hardware) | BLE Advertisement | Initiates pairing mode. |
| `06:22:51` | nRF Connect app: "Pixel Buds Pro 2 van Ted" appears in scan list | App (Auto) | BLE | Device discovered. Address shown: `04:00:6E:CF:6E:07`. State: `NOT BONDED`. |
| `06:23:00` | nRF Connect app: User clicks **"CONNECT"** | User (App) | BLE | Initiates GATT connection. **Crucial starting point for connection analysis in log.** |
| `06:23:01` | nRF Connect app: Device tab opens, status transitions to `CONNECTED` | App (Auto) | BLE | Connection established. State still shows `NOT BONDED`. |
| `06:23:01` | nRF Connect app: **GATT Discovery complete (services visible)** | App (Auto) | GATT | Services populate the screen immediately after connection. |
| `06:23:01` | nRF Connect app: Service `Generic Attribute` (`0x1801`) listed | App (Auto) | UI State | Primary Service. |
| `06:23:01` | nRF Connect app: Service `Generic Access` (`0x1800`) listed | App (Auto) | UI State | Primary Service. |
| `06:23:01` | nRF Connect app: Service `Broadcast Audio Scan Service` (`0x184F`) listed | App (Auto) | UI State | Primary Service. |
| `06:23:01` | nRF Connect app: Service `Audio Stream Control Service` (`0x184E`) listed | App (Auto) | UI State | Primary Service. |
| `06:23:01` | nRF Connect app: Service `Published Audio Capabilities Service` (`0x1850`) listed | App (Auto) | UI State | Primary Service. |
| `06:23:01` | nRF Connect app: Service `Volume Control` (`0x1844`) listed | App (Auto) | UI State | Primary Service. |
| `06:23:01` | nRF Connect app: Service `Microphone Control` (`0x184D`) listed | App (Auto) | UI State | Primary Service. |
| `06:23:01` | nRF Connect app: Service `Audio Input Control` (`0x1843`) listed | App (Auto) | UI State | Secondary Service. |
| `06:23:01` | nRF Connect app: Service `Common Audio Service` (`0x1853`) listed | App (Auto) | UI State | Primary Service. |
| `06:23:01` | Android OS: System notification "Enable Google Play services" appears | OS (Auto) | UI State | "Google Play services must be enabled for..." - GFPS trying to intervene but failing. |
| `06:23:55` | nRF Connect app: User scrolls down the service list | User (App) | UI State | More services become visible. |
| `06:23:56` | nRF Connect app: Service `Common Audio Service` (`0x1853`) listed | App (Auto) | UI State | Primary Service. (Duplicated display due to scroll). |
| `06:23:56` | nRF Connect app: Service `Telephony and Media Audio Service` (`0x1855`) listed | App (Auto) | UI State | Primary Service. |
| `06:23:56` | nRF Connect app: Service `Google Fast Pair Service` (UUID: `0xFE2C`) listed | App (Auto) | UI State | Primary Service. |
| `06:23:56` | nRF Connect app: Service `Accessory Non-Owner Service` (UUID: `13190001-...`) listed | App (Auto) | UI State | Primary Service. Full UUID: `13190001-12f4-c226-88ed-2ac5579f2a85`. |
| `06:23:56` | nRF Connect app: Service `Device Information` (`0x180A`) listed | App (Auto) | UI State | Primary Service. |
| `06:23:56` | nRF Connect app: Service `Battery Service` (`0x180F`) listed | App (Auto) | UI State | Primary Service. |
| `06:23:56` | nRF Connect app: Service `Unknown Service` (UUID: `109b8b21-...`) listed | App (Auto) | UI State | Primary Service. Full UUID: `109b8b21-50e3-45cc-8ea1-ac62de4846d1`. |
| `06:24:01` | Android Bluetooth settings: Opened from notification shade | User (App) | UI State | Navigating to system Bluetooth settings. |
| `06:24:06` | Android Bluetooth settings: User selects "Pair new device" | User (App) | UI State | Initiates classic device discovery. |
| `06:24:07` | Android Bluetooth settings: "Pixel Buds Pro 2 van Ted" listed in available devices | App (Auto) | UI State | Device discovered via classic inquiry. |
| `06:24:24` | Android Bluetooth settings: User selects "Pixel Buds Pro 2 van Ted" | User (App) | Classic / SMP | Initiates pairing sequence. |
| `06:24:25` | Android OS: Pairing dialog appears | OS (Auto) | UI State | Pop-up: "Pair with Pixel Buds Pro 2 van Ted?". Confirms classic SSP pairing is triggered. |
| `06:24:29` | Android OS: User clicks "Pair" in the dialog | User (OS) | Classic / SMP | Confirms pairing. |
| `06:24:30` | Android OS: Switch back to nRF Connect app | User (App) | UI State | Observe connection state. |
| `06:24:59` | nRF Connect app: Device status changes to `CONNECTED`, `BONDED` | App (Auto) | UI State | Bonding confirmed successful. |
| `06:25:04` | Android Bluetooth settings: User opens device details for the Buds | User (App) | UI State | Verifying connection details in system settings. |
| `06:25:06` | End video recording | — | — | Session conclusion. |

```