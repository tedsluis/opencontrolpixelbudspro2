# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group Q (repeat), Battery Notification BLE scan, connection-free (`CAP-043`)

**Status:** ✅ **Captured and analyzed 2026-09-13** — see `CAP-043-FINDINGS.md` for the full
write-up. This file's Event Timeline and Log Metadata below have been independently re-verified
against a full, non-sampled review of the video (tiled contact sheets every 5s, plus a denser 2fps
pass around one ambiguous thumbnail) and the complete wire log, per
`ai-sessions/0016_CAPTURE_PROMPT_2026_09_13.md`.

**Purpose (repeat of `CAP-011`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q item #18):**
`PROTOCOL.md` §4.3 Option A's Fast Pair Battery Notification mechanism is still 🟡 HYPOTHESIS for
the Buds Pro 2 specifically — `CAP-011` (2026-08-21) found the `0xFE2C` Fast Pair Service BLE
advertisement present, but the sampled payloads did not structurally match the documented byte
layout (no `0x33`/`0x34` Length&Type marker at any offset), and the session had a procedure
deviation: an active classic RFCOMM+GATT connection was present throughout (the official app was
left open on "Device details"), which this repeat must avoid. This session closes
`CAP-011-FINDINGS.md` §4's own open item either way (a structural match, or a second confirmed
non-match worth re-examining the hypothesis itself).

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-043`                     |
|      Group(s)    |                    Q (repeat)                      |
|       Date       |                     2026-09-13                     |
| Firmware version | `release_5.203` (⚪ ASSUMPTION, maintainer-supplied session context — not wire-verifiable this session, zero DLCI 0x08/classic traffic exists) |
|   Test device    | Pixel 7a, Android 17 (🟢 FACT, MP4 container tag); official Pixel Buds Companion App force-stopped; app version `1.0.955078536`, Google Play services active (⚪ ASSUMPTION, maintainer-supplied context) |
| Video file       |  `CAP-043-recording.mp4` — 189.897s, 09:50:31–09:53:41 local (burned-in overlay + `ffprobe`-cross-checked) |
| Log file         |          `CAP-043-btsnoop_hci.log` — 1,572 packets, 306.895s, 09:50:27.996–09:55:34.891 local |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | Not observed anywhere this session (see Isolation check below) |

**Isolation check (required — this is the whole point of the repeat) — 🟢 CONFIRMED CLEAN.**
`tshark -r CAP-043-btsnoop_hci.log -Y "bluetooth.addr == 04:00:6e:cf:6e:07"` and
`-Y "btrfcomm"`/`-Y "btsdp"` all return **zero** frames across the entire 306.9s log — no classic
connection, no RFCOMM, no SDP to the Buds' known classic address at any point. A raw-byte scan for
the address (both byte orders) confirms the same (one coincidental reversed-byte match inside an
unrelated vendor-command parameter blob, checked and dismissed — see `CAP-043-FINDINGS.md` §2). The
specific procedure deviation `CAP-011` had (an active classic RFCOMM+GATT connection present
throughout) does **not** reproduce here — this repeat achieves genuine isolation.

## Procedure (repeat of `CAP-011`, per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q item #18)

1. Ensure the official Pixel Buds Companion App is **not running** (force-stop it) and no other
   app holds an active classic Bluetooth connection or GATT session to the Buds for the entire
   capture. Confirm via Settings → Apps.
2. With the Buds already bonded (do not re-pair), start HCI snoop logging, then passively wait
   (case closed and idle, or worn but with no in-app interaction) for at least 60–90 seconds —
   long enough to observe multiple Battery Notification advertisement cycles per `PROTOCOL.md`
   §4.3 Option A.
3. Confirm via `bthci_evt`/`android.bluetooth` logs (or simply by not touching the phone) that no
   classic RFCOMM connection was ever established during the window.
4. Stop logging; extract per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3, preferring the raw
   `btsnoop_hci.log` path over the lossy `btsnooz.py`-from-bugreport fallback (per `TODO.md`'s own
   "Known technical debt" note on extraction-path truncation).

## Event Timeline

**Re-verified 2026-09-13 against a full, non-sampled video pass (5s-interval tiled contact sheets
covering the entire 189.9s video, plus a denser 2fps pass around t=104–120s) and the complete wire
log** — corrects the draft's timing/ordering per `ai-sessions/0016_CAPTURE_PROMPT_2026_09_13.md`
Step D/E.

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 09:50:27.996 | Log's only Bluetooth-adapter `Reset` (log t=0). Not necessarily the same moment as the video's own displayed toggle transition — see `CAP-043-FINDINGS.md` §2's timing note (unresolved, non-blocking, ~3s earlier than the video's own t=0). | System | — | Log frame 1 |
| 09:50:31 (video t=0) | Video starts. Both buds are in the closed case (visible on-camera, resting on the phone). Bluetooth settings screen shows "Bluetooth is off," toggle in off position. App confirmed force-stopped off-camera prior to recording. | User | `BATT-002`, `BATT-003` | Video frame at t=0 |
| ~09:50:29–09:50:36 (video t≈5s) | Bluetooth toggled ON; bonded "Pixel Buds Pro 2 van Ted" entry appears in the settings list showing a static `L: 100%, C: 84%, R: 100%` battery line (present already at first appearance — see `CAP-043-FINDINGS.md` §4 on this being a cached, not live, reading). First own-Buds Fast Pair (`0xFE2C`) BLE advertisement in the log at frame 133 (log t=1.06s, absolute 09:50:29.06). | User (OS) / Buds (auto) | `BATT-002`, `BATT-003` | Video ~t=5s frame; log frame 133 |
| 09:50:31–09:53:41 | Passive observation window. The case remains closed and untouched; the system Bluetooth settings panel stays open and static the entire time (screen never navigates away, no app opens); the "2 apps are active" indicator stays constant throughout (a single 5s-sampled thumbnail at t=115s misread as "3" on first pass — a denser 2fps re-check confirms "2" at every sample in that window, not a real transition). **Isolation confirmed:** zero classic RFCOMM/SDP/connection traffic to the Buds' address anywhere in the log (see Isolation check above). | — | `BATT-002`, `BATT-003` | Window duration: 189.9s (video); 60 own-Buds `0xFE2C` advertisements logged across the full 306.9s log, frames 133–1548, byte-identical payload throughout — see `CAP-043-FINDINGS.md` §3 |
| 09:53:41 | Video ends. | — | — | Video's final frame |
| 09:55:34.891 | Log ends (continues ~113s past the video's own end, with no further notable activity — logging simply stopped later than recording). | — | — | Log's last frame |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q item #18 / `CAP-011-FINDINGS.md` §4)

- [x] Confirm the isolation held: zero classic RFCOMM connection anywhere in the log. **Confirmed —
      genuinely clean this time**, see Isolation check above and `CAP-043-FINDINGS.md` §2/§3.
- [x] Filter for `0xFE2C` service-data BLE advertisements (`btle`, not `btrfcomm`). **320 frames, 2
      addresses; 60 from the own-Buds unit (RSSI -20..-23 dBm), 260 from an unrelated distant device
      (RSSI -84..-104 dBm)** — see `CAP-043-FINDINGS.md` §3.
- [x] Check every sampled payload against `PROTOCOL.md` §4.3 Option A's documented byte layout
      (Flags / Account Key Data / Battery-level-length-&-type byte [`0x33` show / `0x34` hide] /
      three battery-percentage octets). **No match** — first byte `0x10`, not `0x00`; no `0x33`/`0x34`
      byte at any offset.
- [x] Record the result explicitly either way. **Second confirmed non-match, now under genuinely
      clean isolation** — closes `CAP-011-FINDINGS.md` §4's open item (rules out "active connection
      suppresses the advertisement" as an explanation); see `CAP-043-FINDINGS.md` §4.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — `BATT-002`/`BATT-003` referenced throughout the timeline above.
- [x] Write `CAP-043-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [x] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `BATT-002`/`BATT-003` rows' Evidence column with a
      pointer to `CAP-043-FINDINGS.md` (result is a negative/still-HYPOTHESIS finding, not a
      `PROTOCOL.md` promotion — no promotion asserted here per `AGENTS.md` §6).
- [x] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to
      `CAP-043-2026-09-13_09-50-31_09-53-41-Group_Q` (video start/end times).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-043-2026-09-13_09-50-31_09-53-41-Group_Q/CAP-043-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-043-2026-09-13_09-50-31_09-53-41-Group_Q/CAP-043-EVENT-NOTES
