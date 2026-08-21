# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group J, In-ear detection & case sounds (`CAP-024`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-024-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_J` to the actual session
date/start-time/end-time, e.g. `CAP-024-2026-09-01_09-50-00_09-55-00-Group_J`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group J):** main run-through group, never yet
captured — attributes the wire commands for toggling In-ear detection and the two case-sound
settings ('Earbuds replaced', 'Other notifications').

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-024`                     |
|      Group(s)    |                         J                          |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    | TBD (Pixel 7a, Android version, official app version) |
| Video file       |               TBD — `CAP-024-recording.mp4`        |
| Log file         |             TBD — `CAP-024-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group J)

35. **Toggle 'In-ear detection' on/off** [`INEAR-001`]. Wait. Note time.
36. **Toggle case sound 'Earbuds replaced' on/off** [`CASE-001`]. Wait. Note time.
37. **Toggle case sound 'Other notifications' on/off** [`CASE-002`]. Wait. Note time.

Usual rhythm: wait ~5s → note the exact time → perform the action → wait ~5–10s → move to the
next action.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Toggle 'In-ear detection' on/off | User (App) | `INEAR-001` | TBD |
| TBD | Toggle case sound 'Earbuds replaced' on/off | User (App) | `CASE-001` | TBD |
| TBD | Toggle case sound 'Other notifications' on/off | User (App) | `CASE-002` | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [ ] Identify which DLCI/channel carries each toggle's command frame.
- [ ] Note per `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §1: case-sound settings are stored on the chip
      inside the case — check whether the write targets a case-specific address/field distinct
      from bud-targeted writes.
- [ ] Compare structure against `PROTOCOL.md` §2's envelope hypotheses per the usual process.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `INEAR-001`/`CASE-001`/`CASE-002` are clearly referenced above.
- [ ] Write `CAP-024-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.
