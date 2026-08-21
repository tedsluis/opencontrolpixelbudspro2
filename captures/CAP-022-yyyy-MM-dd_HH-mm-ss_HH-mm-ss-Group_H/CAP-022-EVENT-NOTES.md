# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group H, Audio & volume settings (`CAP-022`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-022-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_H` to the actual session
date/start-time/end-time, e.g. `CAP-022-2026-09-01_09-30-00_09-35-00-Group_H`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group H):** main run-through group, never yet
captured — attributes the wire commands for Mono audio, Volume EQ, and Volume balance. Volume
balance is noted by `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §1 as stored **locally on the earbuds
themselves** (persistent write, works across devices) — a good candidate for a confirmable
persistent write.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-022`                     |
|      Group(s)    |                         H                          |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    | TBD (Pixel 7a, Android version, official app version) |
| Video file       |               TBD — `CAP-022-recording.mp4`        |
| Log file         |             TBD — `CAP-022-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group H)

28. **Toggle 'Mono audio' on/off** [`AUDIO-001`]. Wait. Note time.
29. **Toggle 'Volume EQ' on/off** [`AUDIO-002`]. Wait. Note time.
30. **Shift the 'Volume balance' slider** [`AUDIO-003`]. Wait. Note time.

Usual rhythm: wait ~5s → note the exact time → perform the action → wait ~5–10s → move to the
next action.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Toggle 'Mono audio' on/off | User (App) | `AUDIO-001` | TBD |
| TBD | Toggle 'Volume EQ' on/off | User (App) | `AUDIO-002` | TBD |
| TBD | Shift the 'Volume balance' slider | User (App) | `AUDIO-003` | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [ ] Identify which DLCI/channel carries each command frame.
- [ ] For `AUDIO-003` specifically: confirm the persistent-write claim (does the same value
      survive a disconnect/reconnect, per the app's own "works across devices" claim)?
- [ ] Compare structure against `PROTOCOL.md` §2's envelope hypotheses per the usual process.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `AUDIO-001`–`AUDIO-003` are clearly referenced above.
- [ ] Write `CAP-022-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.
