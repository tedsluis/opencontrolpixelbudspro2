# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group A Capture (`CAP-002`)

**Status:** Reviewed against `CAP-002-recording.mp4` frame-by-frame (1s resolution, using the video's
burned-in wall-clock overlay) and cross-checked against `CAP-002-btsnoop_hci.log` via `tshark`/Wireshark.
Corrects and extends the original draft. See `CAP-002-FINDINGS.md` in this same folder for the
standardized, evidence-graded protocol findings extracted from this correlation — this file is
the *event timeline*, `CAP-002-FINDINGS.md` is *what it means for the protocol*.

## Log Metadata

|      Field       |                    Value                     |
|------------------|----------------------------------------------|
|    Capture ID    |                   `CAP-002`                   |
|      Group(s)    |      A (fresh pairing/bonding baseline) + first Pixel Buds app setup |
|       Date       |                  2026-08-09                  |
| Firmware version | unknown — not visible in this capture, TBD |
|   Test device    | Pixel 7a (Official Pixel Buds Companion App) |
| Video file       | `CAP-002-recording.mp4` — 114.2s, starts 08:04:53(=17:04:53 local), ends 17:06:46 (wall clock, +0200) |
| Log file         | `CAP-002-btsnoop_hci.log` — **note: this is a long-running, non-restarted snoop log spanning 08:50:32–17:10:58 (~8h20m), the same buffer used since `CAP-001`.** 50,468 packets total; this capture's actual window is only the ~150s slice 17:04:35–17:07:05 (1,877 packets), everything else is unrelated background Bluetooth activity accumulated over the day. |

**Scope note:** unlike `CAP-001` (a reconnection to an already-bonded device), this session is a
**genuine first-time pairing** — the app explicitly deletes any stored link key before
connecting, and a full Secure Simple Pairing (SSP) exchange occurs. It also captures the *entire*
first-run flow: OS-level Bluetooth pairing → Fast Pair "Save device" → Pixel Buds app's own
CompanionDeviceManager (CDM) permission flow → the app's "Device details" screen loading for the
first time.

**Process note for future captures:** because the HCI snoop buffer was never restarted between
`CAP-001` and this session, it now spans 8+ hours and will keep growing. Before the *next*
capture, restart Bluetooth (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §2 step 5) to get a fresh, short log
— the correlation work for this capture required first slicing the relevant ~150s window out of
a 50k-packet file with `editcap -A/-B`, which is avoidable overhead.

## Event Timeline

Timestamps are the video's own on-screen wall-clock overlay (1fps sampling, refined to
sub-second precision around each tap by extracting additional frames at the exact second); the
log uses the same wall clock (+0200), directly comparable.

| Time | Action / Event | Initiator | Test-ID | Evidence in `CAP-002-btsnoop_hci.log` |
|---|---|---|---|---|
| 17:04:53 | Start video recording. "Pair new device" screen already open, phone's own address shown (`E8:D5:2B:7E:CA:81`); list shows unrelated nearby devices (TV, DoorLocker) | User (App) | — | Ongoing `Extended Inquiry Result` events from before this window (inquiry already running) |
| 17:05:02 | Case opened, LED lit; more nearby devices appear in list over time (laptop, vuart:ktunnel) but not the Buds yet | User (Hardware) | `CASE-001` | — |
| 17:05:22 | "Pixel Buds Pro 2 van Ted" now visible in the device list | App (Auto) | — | `Extended Inquiry Result` at 17:05:21.79 (frame 526) — the Buds' inquiry response reached the phone shortly before this |
| 17:05:26 | Tap **"Pixel Buds Pro 2 van Ted"** in the list; row shows "Pairing..." | User (App) | `PAIR-001` | `Delete Stored Link Key` (17:05:26.717) → `Create Connection` (17:05:26.724) — confirms this is a deliberate fresh-pairing flow, not a reconnect |
| 17:05:27 | (log-only — happens between visible frames) | — | — | `Connect Complete` (17:05:27.146, status `0x00`) → `Link Key Request` → **`Link Key Request Negative Reply`** (no stored key) → `IO Capability Request/Reply/Response` — Secure Simple Pairing begins |
| 17:05:28–33 | **"Pair with Pixel Buds Pro 2 van Ted?"** dialog appears with a **"Also allow access to contacts and call history"** toggle (off by default) | App (Auto) | — | — |
| 17:05:30–32 | Tap the **contacts/call-history toggle** to turn it ON (not present in the original draft's notes) | User (App) | — | — |
| 17:05:33 | Tap **Pair** | User (App) | `PAIR-002` | `Simple Pairing Complete` (17:05:33.608) → `Link Key Notification` (new key stored, 17:05:33.622) → `Authentication Complete` → `Set Connection Encryption` → `Encryption Change` (17:05:33.721) |
| 17:05:34–48 | (not directly visible — app UI transitioning) | — | `PAIR-002` | Large SDP/L2CAP service-enumeration burst (Audio Sink, AVRCP, HFP, HID-Control, HID-Interrupt, generic RFCOMM — frames 726–1231); RFCOMM channels opened: **6 (labeled "Hands-Free" by Wireshark)**, 4, 5, 1, 2 (frames 908–1254); channel 2 closed then reopened (frames 1486–1543) |
| 17:05:53 | Notification banner: **"Pixel Buds Pro 2 van Ted connected — Left 100% Case 57% Right 100%"** | App (Auto) | — | Lags the actual technical connection (complete by ~17:05:48) by ~5–15s — UI/notification delay, not a protocol event |
| 17:05:51–17:06:03 | **"Save device to ted.sluis@gmail.com..."** dialog shown (Fast Pair account-linking prompt) | App (Auto) | `GFPS-001` | No further RFCOMM activity during this window — this step appears to be cloud/GMS-side, not locally observable |
| 17:06:04 | Tap **Save** | User (App) | `GFPS-002` | No corresponding local Bluetooth traffic — consistent with this being an account-linking (cloud) action |
| 17:06:06 | **"Set up device"** prompt appears | App (Auto) | — | — |
| 17:06:09 | Tap **Set up** | User (App) | `APP-001` | — |
| 17:06:10 | Transient **"Starting Setup..."** screen | App (Auto) | — | — |
| 17:06:11 | **"Allow a connection to your Pixel Buds"** screen (Pixel Buds app's own nearby-device permission request) | App (Auto) | — | — |
| 17:06:23 | Tap **Continue** | User (App) | `APP-002` | — |
| 17:06:24 | Green checkmark appears between the app/device icons | App (Auto) | — | — |
| 17:06:25–31 | System dialog: **"Allow the app Pixel Buds to access Pixel Buds Pro 2 van Ted?"** (CompanionDeviceManager association permission — confirms CDM is genuinely used, per `DECISIONS.md` ADR-005) | App (Auto) | — | — |
| 17:06:31 | Tap **Allow** | User (App) | — | — |
| 17:06:33–46 | **"Device details"** screen loaded: Sound, Hearing wellness, More settings, Audio switch, Phone calls/Media audio/**Input device** toggles (all on), device address shown (`04:00:6E:CF:6E:07`) | App (Auto) | — | **No RFCOMM data frames observed anywhere in this window** (17:05:48 through the end of the capture at 17:07:05) — see `CAP-002-FINDINGS.md` §4 |
| 17:06:46 | End video recording | — | — | — |

## Corrections vs. the original draft of this file

- Added the **contacts/call-history permission toggle** interaction (17:05:30–32) inside the
  pairing dialog — not mentioned in the original draft.
- Original draft said pairing completed with a single "Click 'Pair'" at 17:05:38; the actual tap
  is at **17:05:33**, immediately followed by `Simple Pairing Complete` in the log at the same
  second — a tight, well-correlated match. The draft's 17:05:38 was 5s late.
- The **"Allow a connection to your Pixel Buds"** screen actually appears at **17:06:11**, not
  17:06:14 as drafted, and remains on screen until the Continue tap at **17:06:23** (draft said
  "Click Continue" at 17:06:20, 3s early).
- Added the previously-missing **CompanionDeviceManager system permission dialog**
  (17:06:25–31, "Allow the app Pixel Buds to access...") — the draft's generic "Android OS:
  Permission prompt / Click 'Allow'" at 17:06:34 was really this CDM dialog, occurring earlier
  (tap at 17:06:31) and specifically evidencing CDM usage, which is architecturally significant
  (`DECISIONS.md` ADR-005).
- Flagged that the log evidence for `GFPS-001`/`GFPS-002` (Save-to-account) is **absent** —
  this is expected if that step is cloud-side, but it means this capture cannot confirm or refute
  anything about the Fast Pair account-linking protocol itself, only that the local Bluetooth link
  showed no activity during it.
- Flagged that **no RFCOMM data traffic occurs anywhere after ~17:05:48**, including during the
  entire app setup / CDM permission / "Device details" loading sequence (17:06:04–17:06:46) — see
  `CAP-002-FINDINGS.md` §4 for what this means and possible explanations.
