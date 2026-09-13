# 0010_CAPTURE_RESULT_2026_09_12.md — Full video+log re-analysis and FINDINGS for CAP-018, CAP-026, CAP-028, CAP-029, CAP-045, CAP-046, CAP-048, CAP-049

**Number:** 0010
**Category:** CAPTURE
**Date:** 2026-09-12
**Title:** Full video+log re-analysis and FINDINGS for CAP-018, CAP-026, CAP-028, CAP-029, CAP-045, CAP-046, CAP-048, CAP-049
**Status:** complete — all 3 originally-pending sign-off items resolved: item 1 (Volume Balance) via
`DECISIONS.md` ADR-026 (`ai-sessions/0013`); items 2 and 3 (the `CAP-037`/`CAP-048` anomaly and the
`DECISIONS.md` ADR-024 caveat) approved by the maintainer and applied via
`ai-sessions/0015_MAINTENANCE_RESULT_2026_09_13.md`

---

## Phase status table

| Phase | Scope | Status | Summary |
|---|---|---|---|
| 0 | Setup — mandatory reading, folder inventory, this file | done | Read `AGENTS.md`, `PROJECT.md`, `PROJECT_RULES.md`, `DECISIONS.md` (full, every ADR), `ARCHITECTURE.md`, `PROTOCOL.md` (full), `TODO.md`, `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Groups Y/L/O/P/AD/AF/AJ/AK + §9 Capture Index rows + column notes), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (full), `DESKRESEARCH_FINDINGS.md` (full), and the precondition `CAP-NNN-FINDINGS.md` files (`CAP-016` §11, `CAP-019`, `CAP-020`, `CAP-037`, `CAP-039`, `CAP-040`, `CAP-042`) plus all 8 skeleton `CAP-NNN-EVENT-NOTES.md` files. Confirmed via `ls` that all 8 capture folders contain their log/video/notes files as expected (CAP-048/CAP-049 also have `.log.last`; CAP-049's video is `CAP-049-recordings.mp4`, plural, confirming the naming issue flagged in the prompt). |
| 1 | `CAP-018` (Group Y, `GATT-002`) | done | Isolation confirmed (buds/case untouched, full 152s video). Significant finding: the `0x0044` burst rides the session's *only* LE connection, whose GATT profile (Heart Rate service present, no Fast Pair Service) is attributable to an unrelated nearby device, not the Buds — sharpens, doesn't close, `PROTOCOL.md` §6's open item. Bonus: ~64s classic-reconnect delay, new open question. Folder renamed to `CAP-018-2026-09-12_06-07-20_06-09-52-Group_Y`. `CAP-018-FINDINGS.md` written; `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` `GATT-002` row, `PROTOCOL.md` §6 (two items), `id_registry.csv` all updated. Footers fixed via `ensure_footers.py`. |
| 2 | `CAP-026` (Group L, `BATT-001`, `OBS-001`) | done | Draft timeline was largely accurate (unlike CAP-018). `BATT-001` wire-correlated for the first time: battery data lands at connect (~06:50:01), ~12-13s before the on-screen notification. `OBS-001` clean negative: force-close+reopen (connection never drops) produces zero DLCI traffic. Bonus: DLCI 0x08 Case reading (95) doesn't match on-screen (93%), a 4th data point for ADR-014's short-form-may-be-stale hypothesis. Folder renamed, FINDINGS written, cross-ref docs updated. |
| 3 | `CAP-028` (Group O, `HEAD-002`, `HEAD-003`) | done | Precondition (Head gestures enabled) confirmed. Camera never shows the user's head, so gestures aren't video-visible; wire shows a clean negative (zero DLCI traffic on Buds' own connection during the claimed gesture window) — inconclusive, since no call/notification was active for the gesture to act on. Bonus: a *third* occurrence (after CAP-032, CAP-018) of the unrelated Heart-Rate-device BLE signature, coincidentally overlapping this window. Folder renamed, FINDINGS written, cross-ref docs updated. |
| 4 | `CAP-029` (Group P, `CONV-002`, `CASE-007`, `CASE-008`, `PAIR-002`) | done | `CASE-007` (factory reset) was run; `PAIR-002` confirmed fresh SSP. `CONV-002` clean negative (media pauses on screen, zero wire signal). `CASE-008` not attempted. Major correction: draft's claimed final "forget" at 07:57:56 never happened — video/wire both show the device stays connected to the end. Folder renamed, FINDINGS written, cross-ref docs updated. |
| 5 | `CAP-045` (Group AJ, `HOLD-005`) | done | **Major finding: this session did not run Group AJ's own procedure.** Video shows physical press-and-hold ANC cycling on each earbud (TOUCH-007-style Notify-without-Set), never the rotation-checklist settings screen. Zero qhr-field-12 writes anywhere in the log. HOLD-005's Left/Right question remains open — a genuine re-run is still needed. Bonus: 10-sample confirmation that the Notify frame has no Left/Right field structurally. Folder renamed, FINDINGS written, cross-ref docs updated (flagging the gap, not promoting). |
| 6 | `CAP-046` (Group AK, `AUDIO-003`) | done | **Resolved Volume Balance's polarity — opposite of the prior assumption**: `field17=+100`=Left, `-100`=Right, range ±100 (3/3 video-confirmed). Only 2 of the draft's claimed 4 samples actually occurred; no intermediate-position data exists. Bonus: `field19` (Mono audio) fires in exact lockstep (8/8) with every Balance-extreme/center transition. Folder renamed, FINDINGS written, cross-ref docs + PROTOCOL.md §4.5.7 updated as a proposal (not self-promoted to FACT). |
| 7 | `CAP-048` (Group AD repeat, `OBS-004`) | done | `.log.last` resolved as leftover pre-session content (not used). **`CAP-037`'s flagged anomaly directly resolved**: video at the exact wire timestamp shows a bud being physically docked — consistent with ADR-016. 13/13 zero-miss ADR-022 replication. **Genuine counter-example to a simple ADR-024 reading found and reported plainly**: two fresh reconnects report "docked" while video shows the case empty. Draft's claimed 25-reconnect rapid-alternation table did not survive verification — actual cadence much slower. Bonus: unexplained connection-retry burst, plausibly tied to a closed case. Folder renamed, FINDINGS written, cross-ref docs + PROTOCOL.md §6 updated (proposals, not self-promoted). Noted: ~5 of 13 reconnects not individually video-re-verified (each falls inside an already-confirmed window) — disclosed as a scope note in EVENT-NOTES. |
| 8 | `CAP-049` (Group AF repeat, `OBS-006`) | done | Video renamed (`-recordings.mp4`→`-recording.mp4`). `.log.last` determined **genuine same-session rotation** (opposite of CAP-048) — safely combined per Step G's method into `CAP-049-btsnoop_hci-combined.log` (4,392 packets, verified). **Central finding: `CAP-039`'s disconnect cycling did NOT reproduce** — clean negative, classic connection stayed open/stable for the whole ~9-minute session. OBS-006 Set-vs-Get comparison confirmed consistent. Folder renamed, FINDINGS written, cross-ref docs updated. |
| 9 | Cross-capture synthesis and wrap-up | done | See full synthesis and maintainer summary below. `lint_docs.py` run — only pre-existing/expected "dead filename reference" noise (frame-citation convention already used by CAP-038/039/040 before this session, plus historical old-path references in earlier `ai-sessions/` files per the non-destructive-update convention) — nothing requiring a fix. `ensure_footers.py` run, all footers already up to date. `ai-sessions/INDEX.md` row updated. |

## Notes for resumption

- All 8 capture folders already contain their recorded log/video/EVENT-NOTES files (recorded
  2026-09-12 by the maintainer) — none need re-recording. This task is pure analysis.
- Every draft `CAP-NNN-EVENT-NOTES.md` Event Timeline is provisional per the prompt's own Context
  section — several already show signs of not matching the actual procedure (e.g. `CAP-037`-style
  "ran longer/differently than planned" patterns are expected here too), so each phase's Step D
  (full video review) and Step H (full log review) must independently re-derive the timeline rather
  than trust the draft.
- `CAP-048`/`CAP-049` each have a `.log.last`; per Guardrail/Step G, this must not be assumed to be
  rotation — apply the same evidentiary method `CAP-040-FINDINGS.md` §0 and `CAP-042-FINDINGS.md`
  §0 already used (compare `.log.last`'s own Connection-Complete timestamps against this session's
  own confirmed start time and against the neighboring earlier capture's own log content).

## Phase 9 — Cross-capture synthesis

**Agreement/conflict check against existing `PROTOCOL.md`/`DECISIONS.md` FACTs and prior captures:**

- **No genuine contradiction of any existing 🟢 FACT was found.** Every apparent tension resolves as
  a refinement of a scope already implicit in the FACT's own wording, or as a newly-flagged,
  explicitly-labeled exception:
  - `CAP-048`'s two stale `Settable-toggles` readings are a **real, reported-plainly exception** to a
    *simple* reading of `DECISIONS.md` ADR-024 — not a refutation of the FACT itself (four other
    readings in the same session are correct, and ADR-024's own wording never claimed the field
    updates instantaneously on every trigger type). Proposed as a documented caveat, not a reversal.
  - `CAP-048` also **directly resolves** `CAP-037-FINDINGS.md` §5's own flagged anomaly, in the
    *confirming* direction for `DECISIONS.md` ADR-016 (disconnect-on-both-docked).
  - `CAP-049`'s clean negative (no cycling) does not contradict `CAP-039`'s original observation —
    it demonstrates the phenomenon isn't reliably reproducible, which is itself informative.
  - `CAP-018`/`CAP-028` both independently corroborate `CAP-032-FINDINGS.md` §5's own prior finding
    that an unrelated nearby BLE device (Heart Rate service signature) can appear in this project's
    own captures — now a 3-session pattern, not a one-off.
- **`CAP-045` is the one capture whose own draft procedure diverged from its Group's actual purpose**
  — it captured `TOUCH-007` (already well-documented) instead of `HOLD-005` (the still-open
  question it was designed to close). This is flagged plainly, not glossed over: `HOLD-005`'s
  Left/Right question is exactly as open after this batch as before it.
- **Two proposed protocol corrections** (CAP-046's Volume Balance polarity, CAP-048's ADR-024
  caveat) revise or narrow existing documentation rather than merely adding to it — both are
  presented as proposals per the Guardrails, not self-promoted.
- **A recurring theme across this whole batch**: several draft `CAP-NNN-EVENT-NOTES.md` timelines
  (`CAP-018`'s connect timing, `CAP-029`'s final "forget," `CAP-045`'s entire procedure, `CAP-048`'s
  25-reconnect table) did not survive independent video/wire verification, exactly as the prompt's
  own Context section warned. `CAP-026` and `CAP-049` were the two exceptions, whose drafts were
  substantially accurate.

## Final summary for the maintainer

**What changed, per capture:**

| Capture | Headline result |
|---|---|
| `CAP-018` | `0x0044` BLE burst (from `CAP-016`) is attributable to an unrelated nearby device this session, not the Buds — `GATT-002`'s question sharpened, not closed |
| `CAP-026` | `BATT-001` wire-correlated for the first time; `OBS-001` clean negative; Case-field staleness (ADR-014) reproduced a 4th time |
| `CAP-028` | `HEAD-002`/`HEAD-003` inconclusive (no active call/notification existed); 3rd occurrence of the unrelated-BLE-device pattern |
| `CAP-029` | `CONV-002` clean negative (no wire signal despite visible media pause); `CASE-007`/`PAIR-002` confirmed; `CASE-008` not attempted; draft's claimed final "forget" corrected — never happened |
| `CAP-045` | **Did not run its own procedure** — captured `TOUCH-007` instead of `HOLD-005`; a genuine re-run is still needed |
| `CAP-046` | **Volume Balance polarity resolved, opposite of the prior assumption** (+100=Left, -100=Right, range ±100); bonus Mono-audio correlation found |
| `CAP-048` | `CAP-037`'s anomaly **directly resolved** (real-time docking, ADR-016-consistent); **genuine ADR-024 counter-example found and reported plainly**; draft's 25-reconnect table did not survive verification |
| `CAP-049` | `CAP-039`'s disconnect cycling **did not reproduce** (clean negative); `.log.last` genuinely rotated mid-session and was safely combined |

**Newly discovered anomalies/open items** (all added to `PROTOCOL.md` §6 as dated entries this
session): the recurring unrelated-BLE-device pattern (now 3 instances); `CAP-018`'s ~64s slow
classic reconnect; `CAP-029`'s wire-silent Conversation-Detection pause; `CAP-046`'s
Balance↔Mono-audio correlation; `CAP-048`'s ADR-024 counter-example and its unexplained
connection-retry burst.

**Every proposed 🟢 FACT promotion or `DECISIONS.md`-relevant change awaiting maintainer sign-off**
(per `AGENTS.md` §6/§15 — none of these were self-promoted; all are labeled PROPOSAL in the affected
documents):

1. ~~**`PROTOCOL.md` §4.5.7 (Volume Balance)** — promote the ±100 range and the
   `+100`=Left/`-100`=Right direction mapping to 🟢 FACT.~~ **Resolved 2026-09-13** — approved by
   the maintainer in the chat session that authored `ai-sessions/0013_FEATURE_PROMPT_2026_09_13.md`,
   recorded as `DECISIONS.md` ADR-026. `PROTOCOL.md` §4.5.7 updated accordingly.
2. **`DECISIONS.md` ADR-024** — record `CAP-048`'s two video-confirmed counter-examples (stale
   `Settable-toggles` readings on two fresh reconnects) as an explicit, documented exception —
   **not** proposed as a reversal of the existing FACT, proposed as a caveat needing the
   maintainer's own read on how to qualify it. **Still pending** — see
   `ai-sessions/0015_MAINTENANCE_RESULT_2026_09_13.md`.
3. **`CAP-037-FINDINGS.md` §5 / `PROTOCOL.md` §6** — mark this specific anomaly class as resolved
   (real-time dock-state change, ADR-016-consistent), citing `CAP-048`. **Still pending** — see
   `ai-sessions/0015_MAINTENANCE_RESULT_2026_09_13.md`.
4. **`HOLD-005` (`PROTOCOL.md` §4.5.3 / `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`)** — no promotion proposed;
   flagged instead that `CAP-045` did not close this question and a genuine re-run of Group AJ's own
   procedure (open the rotation-checklist screen, not physical long-presses) is still needed.
5. **`PROTOCOL.md` §6 new open items** (not promotions, just newly recorded, already added this
   session): the 3-instance unrelated-BLE-device pattern; the ~64s slow-reconnect question;
   Conversation Detection's wire-silent pause; the Balance↔Mono-audio timing correlation; the
   `CAP-048` connection-retry burst.

**Update (2026-09-13, `ai-sessions/0015_MAINTENANCE_RESULT_2026_09_13.md`):** item 1 resolved (see
above). Items 2 and 3 are this file's only remaining open sign-off items — surfaced again to the
maintainer in `0015`.

## Uncommitted work

Nothing committed yet this session (project convention: no auto-commits). Uncommitted changes as of
this file's completion include: all 8 renamed capture folders and their new/updated
`CAP-NNN-EVENT-NOTES.md`/`CAP-NNN-FINDINGS.md` files; `CAP-049-btsnoop_hci-combined.log` (new file,
produced per Step G); `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`,
`PROTOCOL.md`, and `id_registry.csv` (all updated per-capture); `ai-sessions/INDEX.md` (row updated);
and this file itself. The maintainer should review the 5 proposed items above before anything here
is promoted further.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0010_CAPTURE_RESULT_2026_09_12.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0010_CAPTURE_RESULT_2026_09_12
