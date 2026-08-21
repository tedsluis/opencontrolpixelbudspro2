# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group F, Touch & head gesture toggles (`CAP-020`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-020-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_F` to the actual session
date/start-time/end-time, e.g. `CAP-020-2026-09-01_09-10-00_09-15-00-Group_F`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group F):** main run-through group, never yet
captured — attributes the wire commands for the top-level Touch controls / Head gestures on-off
toggles. **Note:** Group O (Head gestures physical actions) requires 'Head gestures' enabled
here first — run this Group before Group O (`CAP-028`).

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-020`                     |
|      Group(s)    |                         F                          |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    | TBD (Pixel 7a, Android version, official app version) |
| Video file       |               TBD — `CAP-020-recording.mp4`        |
| Log file         |             TBD — `CAP-020-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group F)

21. **Toggle 'Touch controls' fully on/off** [`TOUCH-001`]. Wait. Note time.
22. **Toggle 'Head gestures' fully on/off** [`HEAD-001`]. Wait. Note time.

Usual rhythm: wait ~5s → note the exact time → perform the action → wait ~5–10s → move to the
next action. **End this session with Head gestures left ON** if Group O will follow.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Toggle 'Touch controls' fully on/off | User (App) | `TOUCH-001` | TBD |
| TBD | Toggle 'Head gestures' fully on/off | User (App) | `HEAD-001` | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [ ] Identify which DLCI/channel carries each toggle's command frame.
- [ ] Compare structure against `PROTOCOL.md` §2's envelope hypotheses per the usual process.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `TOUCH-001`/`HEAD-001` are clearly referenced above.
- [ ] Write `CAP-020-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.
