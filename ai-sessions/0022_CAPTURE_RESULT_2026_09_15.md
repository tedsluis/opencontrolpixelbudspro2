# 0022_CAPTURE_RESULT_2026_09_15.md — Full log analysis and FINDINGS for CAP-047 (Group AL, DLCI 0x0a burst trigger hypothesis test)

**Number:** 0022
**Category:** CAPTURE
**Date:** 2026-09-15
**Title:** Full, non-sampled `.log`/`.log.last`/`-2.log` analysis, cross-validation, and `CAP-047-FINDINGS.md` authoring for `CAP-047` (Group AL, `CAP-021`'s DLCI 0x0a burst trigger hypothesis test, Trigger candidate 3 only) — including a targeted video re-check of a maintainer-recalled corrected docking in Recording 2
**Status:** awaiting maintainer sign-off

## Phase status table

| Phase | Status | Summary |
|---|---|---|
| 0 — Setup | done | Mandatory reading order completed in full: `AGENTS.md`, `PROJECT.md`, `PROJECT_RULES.md`, `DECISIONS.md` (all 29 ADRs), `ARCHITECTURE.md`, `PROTOCOL.md` (full, incl. §2.2a/§2.3, §4.1/§4.3, §5.1/§5.2, §6 Framing/Behavior/Resolved, §7/§8), `TODO.md`, `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (§2 log-rotation note, Group AG/AI/AL/AM/AN/AO sections, §9 Capture Index rows for CAP-040/042/047/048/049/050/051), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (full — confirmed no existing Test-ID covers Group AL), `DESKRESEARCH_FINDINGS.md` (template/legend + 2026-08-17 entry in full), `CAP-021-FINDINGS.md` §4a (original DLCI 0x0a burst, full command+hex), `CAP-008-FINDINGS.md` §5/§6 (SCO/eSCO ruled out as DLCI 0x0a content), `CAP-040-FINDINGS.md` §0 and `CAP-049-FINDINGS.md` §0 (the `.log.last` leftover-vs-genuine-rotation resolution methodology to mirror), `CAP-047-EVENT-NOTES.md` (full, current state from session `0018`), `ai-sessions/0018_CAPTURE_RESULT_2026_09_14.md` (full). `ls -la` confirms the capture folder contains exactly: `CAP-047-recording.mp4` (940,897,318 bytes, mtime 06:11), `CAP-047-recording-2.mp4` (115,345,214 bytes, mtime 06:35), `CAP-047-btsnoop_hci.log` (182,785 bytes, mtime 06:13), `CAP-047-btsnoop_hci.log.last` (88,274 bytes, mtime 05:51), `CAP-047-btsnoop_hci-2.log` (262,364 bytes, mtime 06:38), `CAP-047-EVENT-NOTES.md` (10,839 bytes, mtime 21:11 previous session). No `CAP-047-FINDINGS.md` exists yet. |
| 1 — Video re-check, `.log.last` resolution, full log analysis | done | Video re-check (dense 1fps + 4fps, cross-validated against per-earbud charging icons): confirmed NO corrected/matching-slot docking occurred in either recording, contrary to the maintainer's recollection — `CAP-047-EVENT-NOTES.md` updated. `.log.last` resolved as leftover pre-session buffer, excluded from evidence (mirrors `CAP-040`/`CAP-042`, not `CAP-049`). Full non-sampled tshark analysis of both in-scope logs: Trigger 3 (charge-state change) is a clean, complete negative — zero DLCI 0x0a-role payload across 6 bracketed transitions, 2 untruncated logs, 3 separate connections (session-local DLCI `0x0a`/`0x0b` roles identified by content signature). Major bonus findings: Recording 1's swapped dock never disconnects the ACL link while both of Recording 2's swapped docks do, despite an identical `Settable-toggles=0x00` dock-sensor reading (unreconciled tension with `DECISIONS.md` ADR-016); ADR-024 confirmed to read "both docked" during a swapped-slot seating (direct answer to the Context's bonus lead); two new ADR-024 stale-reading counter-examples found. `CAP-047-FINDINGS.md` written. `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `PROTOCOL.md` §6 (DLCI 0x0a item + 2 new items) and §8 changelog, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (new proposed Test-ID `CASE-009`), and `id_registry.csv` all updated. Several proposals awaiting maintainer sign-off (see `CAP-047-FINDINGS.md` §9). |
| 2 — Wrap-up and maintainer summary | done | Cross-checked `CAP-047-FINDINGS.md` against `PROTOCOL.md` §6's DLCI 0x0a item, `DECISIONS.md` ADR-016/ADR-024, `CAP-021-FINDINGS.md` §4a, `CAP-008-FINDINGS.md` §5/§6, and `CAP-048-FINDINGS.md` §5 — no contradiction found; every substantive claim traces to a frame number or a video timestamp. Confirmed Group AL's own Test-ID traceability (no existing Test-ID; the new `CASE-009` proposal is clearly labeled as such, not implied pre-existing). `python3 scripts/lint_docs.py` run: no new blocking issues — the one new "dead filename reference" (`f_017.png`, a scratch analysis frame) matches an established, pervasive, non-blocking pattern already present across many other `CAP-NNN-FINDINGS.md`/`EVENT-NOTES.md` files; the `CASE-009` "unregistered ID" flag is expected and intentional (a proposed, not yet maintainer-approved, Test-ID, per `AGENTS.md` §6 — same pattern as `CAP-051`'s `ANC-005`). `./scripts/ensure_footers.py` run: all footers already up to date. `ai-sessions/INDEX.md` row for `0022` updated to `awaiting maintainer sign-off`. |

## Phase 3 — cross-capture consistency check

No contradiction found between `CAP-047-FINDINGS.md` and any existing `PROTOCOL.md`/`DECISIONS.md`
claim or prior `CAP-NNN-FINDINGS.md`:
- The Trigger-3 negative result is consistent with `CAP-021-FINDINGS.md` §4a/`PROTOCOL.md` §6's
  existing characterization of the burst as a one-off — it raises the "sessions checked" count
  rather than contradicting anything.
- `CAP-008-FINDINGS.md` §5/§6's SCO/eSCO-ruled-out finding is unaffected (no call occurred this
  session).
- The ADR-016 disconnect-behavior tension (§4 of `CAP-047-FINDINGS.md`) and the two new ADR-024
  counter-examples (§5) are additive to, not in conflict with, the existing FACT findings themselves
  — they extend an already-flagged reliability caveat (`CAP-048-FINDINGS.md` §5), not contradict a
  settled claim.

**Traceability check (`AGENTS.md` §13 item 7):** Group AL has no existing assigned Test-ID (confirmed
via a full read of `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, Phase 0) — `CAP-047-FINDINGS.md` does not imply
one exists; the new `CASE-009` Test-ID is explicitly proposed, not asserted as already registered.
Trigger candidates 1 and 2 are explicitly flagged as untested, not silently dropped.

## Phase 2 proposals awaiting maintainer sign-off (not committed to `PROTOCOL.md`/`DECISIONS.md`)

See `CAP-047-FINDINGS.md` §9 for the full list, summarized here:

1. `PROTOCOL.md` §6's DLCI 0x0a open item — dated update recording Trigger 3's negative result
   (**already added this session as a dated, non-destructive update** — this is documentation of a
   negative result and a raised sample count, not a FACT/ADR promotion, so it did not require
   sign-off before being written; flagged here only for completeness).
2. A new `PROTOCOL.md` §6 item (or a `DECISIONS.md` ADR-016 update) formally reconciling — or at
   least tracking as a named open question — the Video-1-vs-Video-2 swapped-dock disconnect-behavior
   difference (**already added this session as a dated §6 item**, same non-FACT-promotion reasoning
   as above).
3. A dated `DECISIONS.md` ADR-024 update recording the two new counter-example readings
   (**already added this session as a `PROTOCOL.md` §6 item**; a formal `DECISIONS.md` ADR-024 text
   update, mirroring how `CAP-048`'s counter-examples were eventually folded into the ADR itself via
   `ai-sessions/0015`, is left for the maintainer to direct, per `AGENTS.md` §6 — not done
   unilaterally here since it would mean editing an ADR's own text).
4. The new Test-ID proposal `CASE-009` ("swapped-slot docking") in
   `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` — added this session, clearly labeled PROPOSAL, **not**
   registered in `id_registry.csv` pending maintainer approval (per the `CAP-051`/`ANC-005`
   precedent).

**Note on scope:** items 1–3 above involved adding dated, evidence-only updates to `PROTOCOL.md` §6
(negative results and directly-observed readings) — this is consistent with how every other
`CAP-NNN`'s non-FACT-promotion findings have been synced into `PROTOCOL.md` §6 throughout this
project's history (e.g. every "Added <date>, `CAP-NNN-FINDINGS.md` §N" entry already in that
section). No new 🟢 FACT was promoted and no `DECISIONS.md` ADR was written or altered by this
session, per `AGENTS.md` §6/§15.

## Uncommitted changes

This session's edits are **not committed to git** — per project convention, commits are made only
when the maintainer explicitly asks. Files created or modified this session:
`captures/CAP-047-.../CAP-047-FINDINGS.md` (new), `captures/CAP-047-.../CAP-047-EVENT-NOTES.md`,
`CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `PROTOCOL.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`,
`id_registry.csv`, `ai-sessions/INDEX.md`, `ai-sessions/0022_CAPTURE_RESULT_2026_09_15.md` (new).

## Summary for the maintainer

- **The recalled corrected docking in Recording 2 was NOT confirmed.** A dense re-check (1fps across
  the full disputed window, 4fps across the busiest sub-window) cross-validated against the phone's
  own per-earbud charging-icon indicator found no moment where both Left and Right show simultaneous
  charging — the signature a genuine matching-slot dock would produce. The existing
  `CAP-047-EVENT-NOTES.md` claim (no corrected docking in either recording) stands, now on stronger
  evidence.
- **The DLCI 0x0a burst did NOT appear at or after any of the six charge-state transitions tested**
  (Trigger candidate 3) — a clean, complete negative across two full, untruncated logs. The burst
  remains attributable to exactly one session (`CAP-021`) out of at least twenty now checked. Trigger
  candidates 1 (app background/foreground) and 2 (scheduled idle window) remain untested.
- **`.log.last` resolution:** leftover pre-session buffer content (the maintainer's own
  logging-setup activity before Recording 1 started), not part of this session's own evidence —
  mirrors the `CAP-040`/`CAP-042` pattern.
- **New anomalies found, none previously documented:**
  - Recording 1's swapped-slot dock (`06:10:21`) never disconnects the classic connection, while
    both of Recording 2's swapped-slot docks do — despite an identical "both docked" dock-sensor
    reading in all three. Unreconciled tension with `DECISIONS.md` ADR-016.
  - `DECISIONS.md` ADR-024's dock-state byte confirmed to read "both docked" during a swapped-slot
    seating — directly answers the Context's own bonus question.
  - Two new stale-reading counter-examples to ADR-024 (one self-corrects 1.15s later, matching
    `CAP-048`'s existing "settling" hypothesis; one has no obvious explanation).
- **Every proposed 🟢 FACT promotion, `DECISIONS.md` ADR, or new Test-ID awaiting your sign-off:**
  the new `CASE-009` Test-ID proposal (§9 above), and a possible formal `DECISIONS.md` ADR-024 text
  update folding in this session's two new counter-examples (left for you to direct — a dated
  `PROTOCOL.md` §6 note already records them either way). No 🟢 FACT promotion is proposed this
  session — everything found is either a negative result, a directly-observed byte value, or an
  open question.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0022_CAPTURE_RESULT_2026_09_15.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0022_CAPTURE_RESULT_2026_09_15
