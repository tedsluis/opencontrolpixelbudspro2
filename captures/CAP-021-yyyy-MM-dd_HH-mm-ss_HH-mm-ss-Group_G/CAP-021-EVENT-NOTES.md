# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group G, Press-and-hold configuration (`CAP-021`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-021-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_G` to the actual session
date/start-time/end-time, e.g. `CAP-021-2026-09-01_09-20-00_09-28-00-Group_G`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group G):** main run-through group, never yet
captured — attributes the wire commands for configuring the per-earbud press-and-hold action and
the ANC-mode rotation list. **Note:** the mode set here (Toggle ANC vs. Digital assistant) is
relevant context for Group N's `TOUCH-007` (press-and-hold physical action, `CAP-027`).

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-021`                     |
|      Group(s)    |                         G                          |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    | TBD (Pixel 7a, Android version, official app version) |
| Video file       |               TBD — `CAP-021-recording.mp4`        |
| Log file         |             TBD — `CAP-021-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group G)

23. **Set 'Press and hold' Left → Toggle ANC** [`HOLD-001`]. Wait. Note time.
24. **Set 'Press and hold' Left → Digital assistant** [`HOLD-002`]. Wait. Note time.
25. **Set 'Press and hold' Right → Toggle ANC** [`HOLD-003`]. Wait. Note time.
26. **Set 'Press and hold' Right → Digital assistant** [`HOLD-004`]. Wait. Note time.
27. **Check/uncheck one ANC mode in the press-and-hold rotation list** [`HOLD-005`] (e.g. remove
    'Off' from the cycle). Wait. Note time.

Usual rhythm: wait ~5s → note the exact time → perform the action → wait ~5–10s → move to the
next action.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Set Press-and-hold Left → Toggle ANC | User (App) | `HOLD-001` | TBD |
| TBD | Set Press-and-hold Left → Digital assistant | User (App) | `HOLD-002` | TBD |
| TBD | Set Press-and-hold Right → Toggle ANC | User (App) | `HOLD-003` | TBD |
| TBD | Set Press-and-hold Right → Digital assistant | User (App) | `HOLD-004` | TBD |
| TBD | Check/uncheck one ANC mode in the rotation list | User (App) | `HOLD-005` | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [ ] Identify which DLCI/channel carries each configuration write's command frame.
- [ ] Check whether Left/Right are distinguished in the payload (a per-earbud field) or via
      separate opcodes/commands entirely.
- [ ] Compare structure against `PROTOCOL.md` §2's envelope hypotheses per the usual process.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `HOLD-001`–`HOLD-005` are clearly referenced above.
- [ ] Write `CAP-021-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.
