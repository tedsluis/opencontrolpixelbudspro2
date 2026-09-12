# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group L, Passive/automatic observation windows (`CAP-026`)

**Status:** ✅ **Captured and analyzed 2026-09-12.** Full video (176.57s) and full log (2,179 packets,
untruncated) reviewed — see `CAP-026-FINDINGS.md`. The hand-filled draft timeline below was largely
accurate (within a few seconds of the video/wire evidence); corrected precisely against direct
video-frame extraction and wire timestamps rather than left as-is.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group L):** main run-through group, never yet
captured — these are **waiting periods, not taps**, meant to catch background/automatic app
traffic. Window 1 (`BATT-001`) targets the officially-confirmed claim that "each time you
connect... a notification will appear showing you where battery life stands"
(`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` `BATT-001`) — this session is what would promote that from a
🔵 confirmed-to-exist UI behavior to a `[VERIFIED-LOCAL]` wire finding. Window 2 (`OBS-001`)
targets whether the app issues any status query specifically on launch, distinct from `OBS-004`'s
already-FACT reconnect-time "Get ANC state" query (`DECISIONS.md` ADR-021/ADR-022) — `OBS-001` has
never itself been wire-checked. **Log explicit boundaries for each window** (observation start,
any event of interest, observation end, Bluetooth connection state, app foreground/background
state) — not just a single timestamp, so settling traffic from whatever preceded the window isn't
confused with genuinely spontaneous traffic during it.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-026`                     |
|      Group(s)    |                         L                          |
|       Date       |                     2026-09-12                     |
| Firmware version | ⚪ ASSUMPTION `release_5.203` (carried over) |
|   Test device    | Pixel 7a, Android 17, Pixel Buds Companion App `1.0.955078536`, Google Play services active |
| Video file       | `CAP-026-recording.mp4` — 176.57s, overlay `06:49:53`–`06:52:49` |
| Log file         | `CAP-026-btsnoop_hci.log` — 2,179 packets, 326.64s, `06:49:20.832`–`06:54:47.467`, untruncated |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |         `04:00:6e:...:07`         |

**Preparation (required before starting):**
- Confirm HCI snoop logging (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §2) is already enabled and running
  before the Buds are connected for Window 1.
- Connect the Buds and the official companion app **before** Window 1 starts. "Settled," per
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group L's own text, means: leave a clean ~10s gap after the
  connect action visibly finishes (app shows "Connected") before treating the window as started —
  do not start the clock at the moment of connecting itself.
- Note the app's foreground/background state and the Bluetooth connection state explicitly at the
  start of *each* window (both are separate table columns below) — this is what distinguishes
  genuinely spontaneous traffic from settling traffic left over from the preceding action.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group L)

42. **Idle wait with the app open** [`BATT-001`], ~60s right after connecting, without touching
    anything — intended to catch the "battery status notification on every reconnect" behavior.
    Leave a clean ~10s gap after the preceding connect action before this window starts (per the
    Preparation note above).
43. **Force-close and reopen the app** [`OBS-001`] — intended to catch any status query the app
    sends on launch. Note the exact time of reopening as the window start; window ends ~30–60s
    after reopening, or once traffic visibly settles (i.e. no new DLCI 0x02/0x04/0x08 activity for
    at least ~10s).

## Event Timeline (corrected against full video review + full log analysis, 2026-09-12)

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 06:49:53 | Video starts. Bluetooth Settings sheet open, "Bluetooth is off." Buds/case sit on top of the phone. | User | — | Video `t=0`. |
| ~06:49:58 | Bluetooth toggled ON; row shows "Connecting…", battery notification already showing "Left 100% Case 93% Right 100%" at the bottom (cached) | User (Hardware) | — | Video `t=8` (overlay `06:50:01`). |
| 06:49:59.9–06:50:00.9 | Classic BR/EDR `Create Connection`→`Connect Complete` (stored key, no SSP) | System | — | Log frame 413, `06:50:00.878`. |
| 06:50:01.30 | DLCI 0x04 `Group 0x03 Code 0x03` (Option B candidate): `L=0x64(100) R=0x64(100)` | System (auto) | — | Log frame 702 — matches on-screen L/R. |
| 06:50:01.36–.40 | DLCI 0x04 `Get ANC state`(`08110000`)/`Notify`(`Settable=0x00`=docked,`Current=0x20`=Off) | System (auto) | — | Log frames 731/739 — ADR-021/022 replication. |
| 06:50:01.65 | DLCI 0x08 `Group 0x0e Code 0x01` (Option E battery triple): `Left=100(flag=1,idx=1), Right=100(flag=1,idx=2), Case=95(idx=3, no-flag/short form)` | System (auto) | — | Log frame 871 — **Case value (95) does not match the on-screen 93% shown throughout this video** (see Findings §3 — a further instance of ADR-014's "short/no-flag form = possibly stale" reading). |
| ~06:50:07 | Device row shows "Active. 100% battery." (purple) | System (UI) | — | Video `t=14`. |
| ~06:50:13 | Notification shade opened, shows "Devices • now, Pixel Buds Pro 2 van Ted, L:100% C:93% R:100%" | User | `BATT-001` | Video `t=20` — the battery-status-on-reconnect notification; all underlying wire data already delivered at `06:50:00–01`, ~12–13s earlier. |
| ~06:50:19 | "Device details" (Pixel Buds app) screen opened; **idle observation window starts** | User (App) | `BATT-001` | Video `t=26`. |
| 06:50:19–06:51:19 | Idle wait, nothing touched, screen stays on "Device details" | — | `BATT-001` | Video spot-checked at `t=40,50,60,70,80` — unchanged. **Zero DLCI 0x02/0x04/0x08 traffic anywhere in this window** (log query, `06:51:00`→end, returns 0 rows) — no spontaneous re-query during the idle wait. |
| ~06:51:19 | Recent-apps (multitasking) screen opened, showing the Settings→Bluetooth debugging page (used earlier to enable HCI snoop) as a background task — **idle window ends** | User | `BATT-001` | Video `t=86`. |
| ~06:51:22–06:51:29 | Returns to home screen, opens app drawer | User | — | Video `t=96`. |
| ~06:51:31 | Pixel Buds app tapped — "Device details" reloads (progress bar visible); **second observation window starts** | User (App) | `OBS-001` | Video `t=98`. |
| 06:51:31–06:52:49 | App stays open on "Device details," nothing touched | — | `OBS-001` | Video spot-checked at `t=110,130,150,170,175` — unchanged, "Active," same battery values throughout. **Zero DLCI 0x02/0x04/0x08 traffic anywhere from `06:51:25` through the end of the log** — the force-close+reopen produces no wire-visible query. |
| ~06:52:49 | Video ends (176.57s) | System | `OBS-001` | Video `t=176`, overlay `06:52:49`. Classic ACL connection never drops for the entire session (single `Connect Complete`, zero `Disconnection Complete` events in the whole 326.64s log). |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [x] Confirm/refute the "battery status notification on every reconnect" claim
      (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` `BATT-001`). → Confirmed at the wire-correlation level: the
      underlying battery data (DLCI 0x04 Group 0x03 Code 0x03, DLCI 0x08 Option E) is delivered in the
      connect-time burst, ~12s before the on-screen notification became visible in the shade.
- [x] Identify any status query frame the app sends on launch (`OBS-001`). → None found — zero
      DLCI 0x02/0x04/0x08 traffic anywhere in the force-close/reopen window; a clean negative result.
- [x] Compare structure against `PROTOCOL.md` §2's envelope hypotheses per the usual process. → Both
      events decode against already-documented envelopes (Message Stream §2.1, DLCI 0x08 private
      envelope §2.2a's sibling framing) — no new structure.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `BATT-001`/`OBS-001` are clearly referenced above.
- [x] Write `CAP-026-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [x] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-026-2026-09-12_06-49-53_06-52-49-Group_L/CAP-026-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-026-2026-09-12_06-49-53_06-52-49-Group_L/CAP-026-EVENT-NOTES
