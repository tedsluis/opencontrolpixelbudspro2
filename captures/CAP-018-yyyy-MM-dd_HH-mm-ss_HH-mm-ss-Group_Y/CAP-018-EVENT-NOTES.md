# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group Y, BLE-only isolation of the `0x0044` notification burst (`CAP-018`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-018-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_Y` to the actual session
date/start-time/end-time, e.g. `CAP-018-2026-09-01_08-30-00_08-32-00-Group_Y`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Y, added 2026-08-20):** `CAP-016-FINDINGS.md`
§11 found a 73-frame `Handle Value Notification` burst on BLE ATT handle `0x0044` (connection
handle `0x0002`, 23 of the 73 frames containing a recurring `0xfea9` byte-pair marker), confined
to a ~29s window right after the BLE link forms and before the classic link exists. Not yet
isolated from a physical trigger — every capture that shows this burst to date also has a bud
removal/insertion happening nearby in the same session. This Group isolates whether the burst is
triggered by the BLE connection forming alone, independent of any bud/case action.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-018`                     |
|      Group(s)    |                         Y                          |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |         TBD (either phone — Pixel 7a or 9a)        |
| Video file       |          TBD — optional (nothing to visually confirm beyond "buds/case were not touched") |
| Log file         |             TBD — `CAP-018-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

**Isolation check (required — this is the whole point of the session):** confirm and record
explicitly that the buds/case were **not** touched at any point before, during, or for at least
60s after the BLE link formed — the opposite of Group M's procedure, which deliberately triggers
bud/case events.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Y)

1. **Enable Bluetooth and let the phone's BLE link to the already-paired Buds form on its own**
   [`GATT-002`] — do not touch the buds or the case at any point before, during, or for at least
   60s after the link forms. Note the exact time Bluetooth was (re-)enabled or the BLE link began
   forming.
2. Keep the observation window open and logging for at least 60s past that point, per the usual
   observation-window discipline (note observation start, any event of interest, observation end).

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Bluetooth (re-)enabled | User (Hardware) | `GATT-002` | TBD |
| TBD | BLE link forms | Buds/Case (Auto) | `GATT-002` | TBD |
| TBD | Observation window end (≥60s after link forms) | — | `GATT-002` | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Y)

- [ ] Filter for `btatt.opcode==0x1b and btatt.handle==0x0044`.
- [ ] If the burst appears despite no bud/case action anywhere in/near the window: narrows the
      trigger to "BLE link establishment alone" (`PROTOCOL.md` §6).
- [ ] If it does not appear: points back toward a bud/case physical action as the real trigger —
      an equally useful negative result.
- [ ] Either outcome closes this open question — don't leave it ambiguous.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `GATT-002` is clearly referenced above.
- [ ] Write `CAP-018-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `GATT-002` row's Evidence column with a pointer
      once promoted into `PROTOCOL.md`.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-018-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_Y/CAP-018-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/#/captures/CAP-018-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_Y/CAP-018-EVENT-NOTES
