# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group C, Conversation Detection & Multipoint (`CAP-019`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-019-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_C` to the actual session
date/start-time/end-time, e.g. `CAP-019-2026-09-01_09-00-00_09-05-00-Group_C`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group C):** main run-through group, never yet
captured — attributes the wire commands for toggling Conversation Detection and Multipoint.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-019`                     |
|      Group(s)    |                         C                          |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    | TBD (Pixel 7a, Android version, official app version) |
| Video file       |               TBD — `CAP-019-recording.mp4`        |
| Log file         |             TBD — `CAP-019-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group C)

6. **Toggle 'Conversation Detection' on/off** [`CONV-001`]. Wait. Note time.
7. **Toggle 'Multipoint' on/off** [`MULTI-001`]. Wait. Note time.

Usual rhythm: wait ~5s → note the exact time → perform the action → wait ~5–10s → move to the
next action. Buds must already be connected and active.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Toggle 'Conversation Detection' on/off | User (App) | `CONV-001` | TBD |
| TBD | Toggle 'Multipoint' on/off | User (App) | `MULTI-001` | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [ ] Identify which DLCI/channel carries each toggle's command frame.
- [ ] Note per `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s own text: Multipoint may trigger an
      SDP/connection update, not just an RFCOMM command — check for that too.
- [ ] Compare structure against `PROTOCOL.md` §2's envelope hypotheses per the usual process.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `CONV-001`/`MULTI-001` are clearly referenced above.
- [ ] Write `CAP-019-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.
