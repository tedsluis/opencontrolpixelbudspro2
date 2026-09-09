# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AN (new), `CAP-041` Case%-change bracket (`CAP-052`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-052-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AN` to the actual session
date/start-time/end-time, e.g. `CAP-052-2026-09-15_08-30-00_09-00-00-Group_AN`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AN, added 2026-09-09):** `CAP-041-FINDINGS.md` §4
and `CAP-036-FINDINGS.md` §12.6 both found a recurring 2-field sub-message inside DLCI 0x02's
connect-time/periodic burst holding a constant value that happens to match the on-screen Case
battery percentage throughout the session — but because the value never changed in either session,
this is consistent with, but does not confirm, the field tracking Case battery (it could equally be
any other session-constant value that happens to match).

**No existing Test-ID covers this specific Case%-change-bracket question** — a new Test-ID would be
a `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` addition, out of this skeleton-creation prompt's own scope (see
`ai-sessions/0005_MAINTENANCE_PROMPT_2026_09_09.md` Task 4); this is flagged here as a follow-up,
not silently assigned one. `OBS-007` (the existing Test-ID for Group AH's connect-time-burst
question) covers a *different* variable (EQ/touch-controls settings state), not Case battery — do
not conflate the two.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-052`                     |
|      Group(s)    |                     AN (new)                       |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, official Pixel Buds Companion App, app open on Device details for on-screen Case% checks) |
| Video file       |    TBD — must periodically show the on-screen Case battery percentage across the session |
| Log file         |             TBD — `CAP-052-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AN)

1. Start a session with the Case at a known, video-confirmed battery percentage (check via the
   official app's Device details screen before starting).
2. Over an extended session (≥30 minutes, allowing genuine charge/discharge to occur — e.g. leave
   the Case charging via USB for part of the window, or simply let it discharge naturally if a
   bud is docked), periodically re-check and video-confirm the on-screen Case percentage.
3. Ensure the DLCI 0x02 periodic push (already confirmed to recur every few minutes when the app is
   foregrounded, per `CAP-036-FINDINGS.md` §12.5) is captured throughout.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Session start, initial Case% video-confirmed | User | — | Conn. state: TBD |
| TBD | Periodic Case% re-check #1, video-confirmed | User | — | TBD |
| TBD | Periodic Case% re-check #2, video-confirmed | User | — | TBD |
| TBD | (continue periodic re-checks for ≥30 minutes — add rows as needed) | User | — | TBD |
| TBD | Session end, final Case% video-confirmed | User | — | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AN)

- [ ] Extract this session's own recurring 2-field sub-message inside the DLCI 0x02 burst, using
      the same method as `CAP-036-FINDINGS.md` §4/`CAP-041-FINDINGS.md` §4.
- [ ] Does the recurring 2-field sub-message's value actually change in step with the
      video-confirmed Case percentage changes across the session?
- [ ] A positive correlation across a genuine change would promote this from "consistent with" to
      real evidence; a mismatch would be an equally useful, reportable negative result.

## Next steps after filling this in

- [ ] Cross-reference this session's own findings against `CAP-036-FINDINGS.md` §12.6 and
      `CAP-041-FINDINGS.md` §4 (`AGENTS.md` §13's traceability check).
- [ ] Write `CAP-052-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Flag, as a follow-up outside this prompt's own scope, whether a dedicated Test-ID for this
      question should be added to `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (see the Purpose section
      above).
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-052-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AN/CAP-052-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-052-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AN/CAP-052-EVENT-NOTES
