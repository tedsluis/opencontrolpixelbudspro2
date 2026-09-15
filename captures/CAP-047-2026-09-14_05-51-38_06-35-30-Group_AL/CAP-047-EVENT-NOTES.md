# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AL, DLCI 0x0a burst trigger, purpose-built hypothesis test (`CAP-047`)

**Status:** ⚪ **Captured, video-analyzed (incl. a targeted dense re-check of Recording 2's tail), and wire-level analyzed.** See `CAP-047-FINDINGS.md` for the full log analysis and hypothesis-test conclusion.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AL, added 2026-09-09):** `CAP-021`'s 1123-frame
DLCI 0x0a burst (`PROTOCOL.md` §6) has recurred in exactly 1 of 16+ sessions checked — passively
waiting for it is not expected to work. `TODO.md`'s own "Recommended priority order" §5 asks for a
purpose-built hypothesis test bracketing candidate triggers **one at a time**, per
`PROJECT_RULES.md` §4's fixed template (hypothesis, setup, expected outcome, actual outcome,
conclusion).

**No existing Test-ID covers this specific bracketed-trigger question** — a new Test-ID would be a
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` addition, out of this session's scope; this is flagged here as a follow-up,
not silently assigned one. Use the existing DLCI-0x0a discussion in `PROTOCOL.md` §6 as the
evidence anchor for the findings file instead.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-047`                     |
|      Group(s)    |                         AL                          |
|       Date       |                     2026-09-14                      |
| Firmware version |                   `release_5.203`                   |
|   Test device    | Pixel 7a, official Pixel Buds Companion App (`1.0.955078536`), Android 17, Google Play services active |
| Video file       | `CAP-047-recording.mp4` (00:20:03.45) and `CAP-047-recording-2.mp4` (00:02:29.59) |
| Log file         | `TBD — pending separate log-analysis session` (Files: `CAP-047-btsnoop_hci.log`, `CAP-047-btsnoop_hci.log.last`, `CAP-047-btsnoop_hci-2.log`) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            `04:00:6e:cf:6e:07`             |

**Method choice:** Only Trigger candidate 3 (charge-state change) was run, across two recordings due to the mis-docking repeat, not as three separate bracketed sub-sessions. Trigger candidates 1 and 2 were **not attempted** this session.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AL)

1. **Trigger candidate 1 — app backgrounded/foregrounded**: Not attempted this session.
2. **Trigger candidate 2 — a scheduled sync window**: Not attempted this session.
3. **Trigger candidate 3 — a charge-state change**: dock one or both Buds into the case (charging begins) or remove them (charging stops) while logging, isolating this specific transition.

## Event Timeline

*Note: Timestamps are derived directly from the burned-in wall-clock overlays on the videos.*

### Recording 1 (`CAP-047-recording.mp4`)

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 05:51:38 | Recording starts. White case is closed, buds are outside resting on the gray surface. Phone screen is black (standby/idle). | User | — | `TBD — pending separate log-analysis session` |
| 06:09:56 | Phone screen is turned ON, showing home screen. Case is OPEN and empty. Buds are outside. | User | — | `TBD — pending separate log-analysis session` |
| 06:10:12 | Right hand picks up the Right bud from the gray surface. | User | — | `TBD — pending separate log-analysis session` |
| 06:10:15 | **Swapped-side placement attempt #1 (Part A)**: Right bud is placed into the Left slot (bottom slot in camera view). | User | — | `TBD — pending separate log-analysis session` |
| 06:10:18 | Right hand picks up the Left bud from the gray surface. | User | — | `TBD — pending separate log-analysis session` |
| 06:10:20 | **Swapped-side placement attempt #1 (Part B)**: Left bud is placed into the Right slot (top slot in camera view). | User | — | `TBD — pending separate log-analysis session` |
| 06:10:21 | Both buds are inside the case in swapped configuration. Case LED remains dark. Phone screen continues to show "Pixel Buds Pro 2 van Ted connected" notification with headphone badge (connection is active, did not disconnect). | Hardware | — | `TBD — pending separate log-analysis session` |
| 06:10:46 | Right hand removes the Left bud (top slot) from the case. | User | — | `TBD — pending separate log-analysis session` |
| 06:10:50 | Right hand removes the Right bud (bottom slot) from the case. Both buds are now resting outside on the gray surface. | User | — | `TBD — pending separate log-analysis session` |
| 06:11:41 | Recording 1 ends with both buds resting outside the open case. | User | — | `TBD — pending separate log-analysis session` |

### Recording 2 (`CAP-047-recording-2.mp4`)

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 06:33:01 | Recording 2 starts. Phone screen is on, showing "Bluetooth" settings showing "Pixel Buds Pro 2 van Ted" as "Connecting...". Case is closed, buds are outside. | User | — | `TBD — pending separate log-analysis session` |
| 06:33:21 | Bluetooth connection establishes. Phone screen shows "Device details" screen as "Active" with Left: 100%, Case: 78%, Right: 100%. | Hardware | — | `TBD — pending separate log-analysis session` |
| 06:33:31 | Case lid is opened (case is empty). | User | — | `TBD — pending separate log-analysis session` |
| 06:33:41 | **Swapped-side placement attempt #2**: Right bud is placed into bottom slot (Left slot), Left bud is placed into top slot (Right slot). | User | — | `TBD — pending separate log-analysis session` |
| 06:33:51 | Both buds sit in the swapped configuration. Screen button still says "Disconnect" (still connected). | Hardware | — | `TBD — pending separate log-analysis session` |
| 06:33:57 | Connection drops. Phone screen button changes to "+ Connect". Green charging bolts are visible on screen before/during disconnect. Case LED remains dark. | Hardware | — | `TBD — pending separate log-analysis session` |
| 06:34:13 | Right hand removes top bud (Left bud) from the case. | User | — | `TBD — pending separate log-analysis session` |
| 06:34:15 | Right hand removes bottom bud (Right bud) from the case. Both buds are now outside. | User | — | `TBD — pending separate log-analysis session` |
| 06:34:16 | Connection immediately re-establishes as soon as buds are removed. Screen button changes back to "Disconnect". | Hardware | — | `TBD — pending separate log-analysis session` |
| 06:34:37 | Case lid is closed. Case LED flashes white. | User | — | `TBD — pending separate log-analysis session` |
| 06:34:41 | Case lid is opened again. | User | — | `TBD — pending separate log-analysis session` |
| 06:34:41 | **Swapped-side placement attempt #3**: Right hand places Left bud into top slot (Right slot) only. The Right bud remains lying outside on the gray surface. | User | — | `TBD — pending separate log-analysis session` |
| 06:34:46 | Left bud sits in top slot (Right slot). Right bud remains outside. | Hardware | — | `TBD — pending separate log-analysis session` |
| 06:34:51 | Right hand places Right bud into bottom slot (Left slot). Both buds are inside in swapped configuration again. Screen immediately disconnects and shows "+ Connect". | User / HW | — | `TBD — pending separate log-analysis session` |
| 06:34:59 | Right hand removes bottom bud (Right bud) from bottom slot. Top bud (Left bud) remains inside top slot. | User | — | `TBD — pending separate log-analysis session` |
| 06:35:01 | Left bud remains in top slot (Right slot) and charges (green charging bolt visible for Left: 100% on phone screen). Right bud is outside and re-establishes connection (screen button says "Disconnect", active status). | HW / User | — | `TBD — pending separate log-analysis session` |
| 06:35:12 | Right hand removes Left bud from the top slot (Right slot). Both slots are empty and both buds rest outside. | User | — | `TBD — pending separate log-analysis session` |
| 06:35:30 | Recording 2 ends with both buds resting outside the open, empty case. *Note: Proper (matching) slot corrected docking was never performed during either recording — re-confirmed 2026-09-15 (prompt `0022`) via a targeted, dense re-check (1fps across the full 06:33:41–06:35:30 window, 4fps across the densest 06:34:47–06:35:13 sub-window), cross-validated against the phone screen's own per-earbud charging-icon indicator (Device details' Left/Case/Right circles). No frame anywhere in this window shows both Left and Right simultaneously displaying the charging-bolt icon — the signature a genuine matching-slot docking would be expected to produce — and no case-visual frame shows a bud configuration inconsistent with the swap timeline already documented above. The maintainer's recollection of a corrected docking in Recording 2 is not supported by this denser re-check; see `CAP-047-FINDINGS.md` §1 for the full evidence and the corresponding hypothesis-test writeup.* | User | — | See `CAP-047-FINDINGS.md` §1 |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AL, `PROJECT_RULES.md` §4's fixed template)

For each of the 3 bracketed triggers, record explicitly, per `PROJECT_RULES.md` §4 (hypothesis,
setup, expected outcome, actual outcome, conclusion):

- [ ] **Trigger 1 (app backgrounded/foregrounded):** did the 1123-frame (or any size) DLCI 0x0a
      burst appear in or shortly after this window?
      *Note: Not attempted this session.*
- [ ] **Trigger 2 (scheduled sync window):** did the burst appear during or after this idle window?
      *Note: Not attempted this session.*
- [x] **Trigger 3 (charge-state change):** did the burst appear at or shortly after the dock/undock
      transition?
      *Note: Video analysis (including a targeted dense re-check, see the 06:35:30 row above) confirms the physical sequence of three swapped-side docking attempts across two recordings (one in video 1, two in video 2), and that a corrected (matching-slot) docking attempt was never performed in either recording. See `CAP-047-FINDINGS.md` for the full DLCI 0x0a wire-log result against each of these bracketed charge-state transitions.*
- [x] A negative result for all three is itself a valuable, reportable outcome, not a failed
      session — record it as such rather than treating the session as inconclusive. *See `CAP-047-FINDINGS.md` for the actual result.*

## Next steps after filling this in

- [ ] Cross-reference every candidate trigger tested above against `PROTOCOL.md` §6's own DLCI
      0x0a open item (`AGENTS.md` §13's traceability check).
- [ ] Write `CAP-047-FINDINGS.md` per `PROJECT_RULES.md` §2 and §4's fixed hypothesis-test
      template, using this file's timeline as the evidence source, following the hex & script rule
      (§1 rule 4a). (Pending separate log-analysis session).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log
      path(s). (Pending separate log-analysis session).
- [ ] Flag, as a follow-up outside this prompt's own scope, whether a dedicated Test-ID for this
      question should be added to `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (see the Purpose section
      above).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-047-2026-09-14_05-51-38_06-35-30-Group_AL/CAP-047-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-047-2026-09-14_05-51-38_06-35-30-Group_AL/CAP-047-EVENT-NOTES
