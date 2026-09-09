# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AL (new), DLCI 0x0a burst trigger, purpose-built hypothesis test (`CAP-047`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-047-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AL` to the actual session
date/start-time/end-time, e.g. `CAP-047-2026-09-15_08-30-00_09-15-00-Group_AL`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AL, added 2026-09-09):** `CAP-021`'s 1123-frame
DLCI 0x0a burst (`PROTOCOL.md` §6) has recurred in exactly 1 of 16+ sessions checked — passively
waiting for it is not expected to work. `TODO.md`'s own "Recommended priority order" §5 asks for a
purpose-built hypothesis test bracketing candidate triggers **one at a time**, per
`PROJECT_RULES.md` §4's fixed template (hypothesis, setup, expected outcome, actual outcome,
conclusion).

**No existing Test-ID covers this specific bracketed-trigger question** — a new Test-ID would be a
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` addition, out of this skeleton-creation prompt's own scope (see
`ai-sessions/0005_MAINTENANCE_PROMPT_2026_09_09.md` Task 4); this is flagged here as a follow-up,
not silently assigned one. Use the existing DLCI-0x0a discussion in `PROTOCOL.md` §6 as the
evidence anchor for the findings file instead.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-047`                     |
|      Group(s)    |                     AL (new)                       |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, official Pixel Buds Companion App) |
| Video file       |    TBD — one recording per sub-session, or one continuous recording with clear boundary timestamps (record which was used, per the Procedure note below) |
| Log file         |             TBD — `CAP-047-btsnoop_hci.log` (or `CAP-047a`/`CAP-047b`/`CAP-047c` if run as 3 separate captures — record which) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

**Method choice (record which was used):** run as up to 3 separate bracketed sub-sessions, each
testing exactly one candidate trigger, logged either as 3 rows under this one Group/`CAP-047` ID or
as 3 short sequential capture windows in one continuous log with clear boundary timestamps — the
maintainer's own choice at execution time.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AL)

1. **Trigger candidate 1 — app backgrounded/foregrounded**: with the Buds connected and idle,
   background the official app for ≥2 minutes, then foreground it again; log throughout.
2. **Trigger candidate 2 — a scheduled sync window**: leave the Buds connected and the phone
   otherwise idle (screen off, app backgrounded) for an extended window (≥15 minutes, matching
   `CAP-042`'s own idle-bracket precedent) to see if the burst appears without any explicit action.
3. **Trigger candidate 3 — a charge-state change**: dock one or both Buds into the case (charging
   begins) or remove them (charging stops) while logging, isolating this specific transition.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Trigger 1 window start — app backgrounded | User (App) | — | Conn. state: TBD |
| TBD | Trigger 1 window end (≥2 min later, app foregrounded) | User (App) | — | TBD |
| TBD | Trigger 2 window start — phone idle, screen off, app backgrounded | — | — | TBD |
| TBD | Trigger 2 window end (≥15 min later) | — | — | TBD |
| TBD | Trigger 3 window start — bud(s) docked/undocked (charge-state change) | User (Hardware) | — | TBD |
| TBD | Trigger 3 window end | — | — | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AL, `PROJECT_RULES.md` §4's fixed template)

For each of the 3 bracketed triggers, record explicitly, per `PROJECT_RULES.md` §4 (hypothesis,
setup, expected outcome, actual outcome, conclusion):

- [ ] **Trigger 1 (app backgrounded/foregrounded):** did the 1123-frame (or any size) DLCI 0x0a
      burst appear in or shortly after this window?
- [ ] **Trigger 2 (scheduled sync window):** did the burst appear during or after this idle window?
- [ ] **Trigger 3 (charge-state change):** did the burst appear at or shortly after the dock/undock
      transition?
- [ ] A negative result for all three is itself a valuable, reportable outcome, not a failed
      session — record it as such rather than treating the session as inconclusive.

## Next steps after filling this in

- [ ] Cross-reference every candidate trigger tested above against `PROTOCOL.md` §6's own DLCI
      0x0a open item (`AGENTS.md` §13's traceability check).
- [ ] Write `CAP-047-FINDINGS.md` per `PROJECT_RULES.md` §2 and §4's fixed hypothesis-test
      template, using this file's timeline as the evidence source, following the hex & script rule
      (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log
      path(s).
- [ ] Flag, as a follow-up outside this prompt's own scope, whether a dedicated Test-ID for this
      question should be added to `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (see the Purpose section
      above).
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-047-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AL/CAP-047-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-047-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AL/CAP-047-EVENT-NOTES
