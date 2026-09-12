# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group L, Passive/automatic observation windows (`CAP-026`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-026-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_L` to the actual session
date/start-time/end-time, e.g. `CAP-026-2026-09-01_10-10-00_10-14-00-Group_L`.

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
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    | TBD (Pixel 7a, Android version, official app version) |
| Video file       |               TBD — `CAP-026-recording.mp4`        |
| Log file         |             TBD — `CAP-026-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

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

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|----------|---|---|---|---|
| 06:49:54 | start of video- Bluetooth is disabled on the Pixel 7a. | — |
| 06:49:58 | User enables Bluetooth. | — |
| 06:50:00 | Pixel Buds Pro 2 shows status "Connecting...". | — |
| 06:50:03 | Pixel Buds Pro 2 are connected ("Active. L: 100%, R: 100%"). | — |
| 06:50:08 | User navigates back to homescreen. Device connection is gesetteld. | — |
| 06:50:11 | Notification screen is opened and shows pop-up battery status (L: 100%, Case: 93%, R: 100%). | `BATT-001`<br> |
| 06:50:13 | Bluetooth "Device details" screen is opened. Start idle observation window (app open, ~60s wait). | `BATT-001`<br> |
| 06:51:19 | End of idle wait. Users opens multitasking screen. | `BATT-001`<br> |
| 06:51:22 | User return to home screen. | — |
| 06:51:32 | Pixel Buds app (Device details) are opened. Start of second observation window after reopening. | `OBS-001`<br> |
| 06:52:51 | End of video. App was keept open for the rest of the video for passieve observation. | `OBS-001`<br> |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [ ] Confirm/refute the "battery status notification on every reconnect" claim
      (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` `BATT-001` — currently 🔵 confirmed at the
      UI/behavior level per official docs, not yet `[VERIFIED-LOCAL]` on the wire).
- [ ] Identify any status query frame the app sends on launch (`OBS-001`).
- [ ] Compare structure against `PROTOCOL.md` §2's envelope hypotheses per the usual process.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `BATT-001`/`OBS-001` are clearly referenced above.
- [ ] Write `CAP-026-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-026-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_L/CAP-026-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-026-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_L/CAP-026-EVENT-NOTES
