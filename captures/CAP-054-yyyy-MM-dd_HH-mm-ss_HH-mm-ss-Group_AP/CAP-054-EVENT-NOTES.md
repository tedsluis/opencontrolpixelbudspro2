# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AP (new), Battery Notification right after the case is opened, connection-free (`CAP-054`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-054-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AP` to the actual session
date/start-time/end-time, e.g. `CAP-054-2026-09-15_08-30-00_08-40-00-Group_AP`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AP; redesigned 2026-10-03, `ai-sessions/0069`, lead L68-6):** the Fast Pair
`batterynotification` page names its own use case — show the notification "when the case has opened", hide it when the buds are removed or the
case is closed (`PROTOCOL.md` §4.3 Option A, Correction of 2026-10-03). No capture covers "case just opened, both buds inside, phone not
connected": `CAP-043` tested only the closed, idle case. This run opens the lid twice with the phone not connected, then brackets one bud
out/in as the earlier design did. Until 2026-10-03 this skeleton tested a single-bud insertion/removal only, on a sentence that is not on the
spec page (`A68-PROT-06`). Test-IDs: `BATT-007` (lid open), `BATT-002`, `BATT-003` (bud out/in).

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-054`                     |
|      Group(s)    |                     AP (new)                       |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, official Pixel Buds Companion App **force-stopped** for the entire session, matching `CAP-043`'s methodology) |
| Video file       |    TBD — must show the system Bluetooth settings panel and the exact bud removal/re-insertion moments |
| Log file         |             TBD — `CAP-054-btsnoop_hci.log` |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD (expected: not observed, per `CAP-043`'s own isolation — no classic connection this session)             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AP)

1. Phone: Bluetooth on, the Buds bonded but **not connected** — every other phone that knows the Buds has Bluetooth off; the official app
   force-stopped (as `CAP-043`). Film the system Bluetooth panel showing the Buds as not connected.
2. Start HCI snoop logging and the camera film (the phone's Bluetooth panel **and the case** in frame; the status bar across a minute change at
   the start and the end).
3. Both buds in the case, lid **closed**, ≥ 60 s (baseline).
4. **Open the lid** on film; touch nothing for ≥ 30 s; close it; wait 30 s. Repeat once. If the phone connects by itself, note the time.
5. Lid open: take **one** bud out on film, wait ≥ 15 s; put it back, wait ≥ 15 s.
6. Repeat step 5 with the other bud.
7. No app scans in this run — the log is the system's own (`AGENTS.md` §7).

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Session start, app confirmed force-stopped, no active connection | User | — | Conn. state: TBD |
| TBD | Lid closed, both buds inside, ≥ 60 s (baseline) | — | `BATT-002` | TBD |
| TBD | Lid **opened**, video-confirmed; ≥ 30 s untouched | User (Hardware) | `BATT-007`, `CASE-003` | TBD |
| TBD | Lid closed; 30 s | User (Hardware) | — | TBD |
| TBD | Lid opened a second time; ≥ 30 s untouched | User (Hardware) | `BATT-007`, `CASE-003` | TBD |
| TBD | Earbud 1 (Left/Right — specify) removed from case, video-confirmed | User (Hardware) | `CASE-004`/`CASE-005` | TBD |
| TBD | Wait ≥15s, nothing touched | — | — | TBD |
| TBD | Earbud 1 re-inserted into case, video-confirmed | User (Hardware) | — | TBD |
| TBD | Wait ≥15s, nothing touched | — | — | TBD |
| TBD | Earbud 2 (the other one) removed from case, video-confirmed | User (Hardware) | `CASE-004`/`CASE-005` | TBD |
| TBD | Wait ≥15s, nothing touched | — | — | TBD |
| TBD | Earbud 2 re-inserted into case, video-confirmed | User (Hardware) | — | TBD |
| TBD | Wait ≥15s, nothing touched | — | — | TBD |
| TBD | Session end | — | — | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AP)

- [ ] Isolation check (per `CAP-043`'s own method): confirm zero classic connection, zero RFCOMM,
      zero SDP to the Buds' known classic address anywhere in this log.
- [ ] For each of the 2 lid openings (`BATT-007`): the `0xFE2C` service-data advertisements in the 30 s after the lid opens — any field
      with type `0x3`/`0x4` and three value bytes? Command, exit status, and a positive control (the same filter on `CAP-043`).
- [ ] For each of the 4 bracketed events (2 removals + 2 insertions), does a `0xFE2C` service-data
      advertisement carrying the documented Battery Notification layout
      (`[Flags=0x00][Account Key Data][0x33/0x34 marker][L][R][Case]`) appear within a few seconds?
- [ ] If still a clean negative across the 2 lid openings and all 4 bud events, record that plainly — this strengthens the case
      for reframing `PROTOCOL.md` §4.3 Option A's status, per `CAP-043-FINDINGS.md` §7's own
      recommendation (a maintainer decision, not to be made unilaterally here).
- [ ] If a positive match is found, decode the payload per the documented layout and cross-reference
      against `AGENTS.md` §13.6's zero-creativity rule.

## Next steps after filling this in

- [ ] Cross-reference this session's own findings against `PROTOCOL.md` §4.3 Option A and
      `CAP-043-FINDINGS.md` (`AGENTS.md` §13's traceability check).
- [ ] Write `CAP-054-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-054-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AP/CAP-054-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-054-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AP/CAP-054-EVENT-NOTES
