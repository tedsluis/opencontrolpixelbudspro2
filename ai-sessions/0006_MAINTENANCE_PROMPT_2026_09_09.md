# 0006_MAINTENANCE_PROMPT_2026_09_09.md — Expand the CAP-018/026/028/029/030 placeholder skeletons into complete test/preparation/execution descriptions

**Number:** 0006
**Category:** MAINTENANCE
**Date:** 2026-09-09
**Title:** Expand the CAP-018/026/028/029/030 placeholder skeletons into complete test/preparation/execution descriptions

---

## Mandatory reading order (do this first, in this order)

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8, read, in order, before taking any other
action:

1. `AGENTS.md` (full)
2. `PROJECT.md` (full)
3. `PROJECT_RULES.md` (full)
4. `DECISIONS.md` (every ADR, ADR-001 through the most recent)
5. `ARCHITECTURE.md`
6. `PROTOCOL.md` (full, especially §0.1's firmware/version-compatibility matrix and its 2026-08-28
   "`XC-03`" note on the unreconciled `4.467`-vs-`release_5.203` version schemes; §4.4 Find My
   Buds; §4.5.1 Conversation Detection; §4.5.4 Head gestures; §4.5.9 "Not yet mapped" — Loud Noise
   Protection/Adaptive Audio; §6's open items for `LOUD-001`/`ADAPT-002`)
7. `TODO.md` (full, especially Phase 1's remaining-captures bullets for `CAP-018`/`CAP-026`/
   `CAP-028`–`CAP-030` and the `release_5.203`/`4.467` reconciliation caveat)
8. `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md`
9. `CAPTURE_BLUETOOTH_HCI_SNOOP.md` in full — specifically Groups L, O, P, and Q (the four Groups
   these five skeletons implement), §6 "Firmware/OS-level compatibility notes," and §9's Capture
   Index rows for `CAP-018`/`CAP-026`/`CAP-028`/`CAP-029`/`CAP-030`
10. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` in full — specifically the catalog rows for `BATT-001`,
    `OBS-001`, `GATT-002`, `HEAD-002`, `HEAD-003`, `CONV-002`, `CASE-007`, `CASE-008`, `PAIR-002`,
    `LOUD-001`, and `ADAPT-002`
11. All five existing skeleton files in full, to establish the current baseline before editing:
    - `captures/CAP-018-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_Y/CAP-018-EVENT-NOTES.md`
    - `captures/CAP-026-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_L/CAP-026-EVENT-NOTES.md`
    - `captures/CAP-028-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_O/CAP-028-EVENT-NOTES.md`
    - `captures/CAP-029-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_P/CAP-029-EVENT-NOTES.md`
    - `captures/CAP-030-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_Q/CAP-030-EVENT-NOTES.md`
12. For comparison — the 10 newer skeletons created by `ai-sessions/0005_MAINTENANCE_RESULT_2026_09_09.md`
    (`CAP-043` through `CAP-052`). These established a more consistent, explicit "required — this
    is the whole point of the session" callout box (e.g. `CAP-043`'s "Isolation check", `CAP-044`'s
    "Isolation check", `CAP-050`'s "Trigger check") directly under Log Metadata. The five older
    skeletons in scope for this prompt predate that pattern and apply it inconsistently (`CAP-018`
    has an "Isolation check"; `CAP-028` has a "Pre-condition check"; `CAP-026`, `CAP-029`, and
    `CAP-030` have none at all, even though `CAP-029`'s own Purpose paragraph states a precondition
    in prose) — use the newer style as the target format to converge on, not as content to copy
    (these are different Groups testing different things).

## Context (from the chat session that authored this prompt, not re-derived here)

The maintainer asked for a follow-up prompt covering the 5 **already-planned** captures that were
explicitly **out of scope** for `ai-sessions/0005` (`CAP-018`, `CAP-026`, `CAP-028`, `CAP-029`,
`CAP-030` — each already has a skeleton folder, created 2026-08-20, none yet executed). Unlike
`0005`, this prompt does **not** create new skeletons or assign new `CAP-NNN`/Group-letter IDs — it
improves the **existing** five in place so that, when the maintainer actually runs one of these
sessions, the skeleton itself already contains a clear, complete description of **what is being
tested, how to prepare for it, and how to execute it** — not just a Procedure list assuming the
reader already knows the surrounding context.

A pass over the current state of all five (done while authoring this prompt, not re-derived by the
executing session from scratch) found these five are inconsistent in how much preparation context
they actually spell out, and each has at least one concrete, specific gap — not just "could be more
detailed" in the abstract:

- **`CAP-018` (Group Y):** has a clear "Isolation check" callout and a 2-step procedure, but never
  states, as an explicit preparation step, that the Buds must already be bonded *and* Bluetooth
  must be off (or the BLE link not yet formed) *and* HCI snoop logging already enabled/running
  *before* the timed action (enabling Bluetooth) begins — this ordering is implied by
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Y's own text but not restated as a checklist a preparer
  can follow without cross-referencing that document.
- **`CAP-026` (Group L):** the Purpose paragraph is generic ("main run-through group, never yet
  captured") and gives no preparation checklist at all — it doesn't state that the Buds must
  already be connected and settled (app open) before Window 1 starts, nor what "settled" means
  operationally (a clean gap after the connect action, per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group
  L's own text, which the skeleton doesn't restate).
- **`CAP-028` (Group O):** has a "Pre-condition check" callout requiring Head gestures enabled via
  `CAP-020` first, but (a) gives no fallback instruction for what to do if `CAP-020` hasn't been run
  yet (toggle Head gestures on directly as this session's own first step, per Group F, and log that
  as its own timeline event), and (b) never mentions the firmware precondition
  (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §6: Head Gestures require firmware ≥4.467) at all — worth
  noting for reproducibility (`PROJECT_RULES.md` rule 11) even though `PROTOCOL.md` §0.1's `XC-03`
  note already confirms Head gestures work on this project's own test device since `CAP-020`, so
  this isn't a live blocker, just a documentation gap.
- **`CAP-029` (Group P):** already flags an open decision ("run items 15/17 only, or all three
  including the destructive factory reset — record the decision here: TBD") but gives no guidance
  for *how* to decide, and — unlike `CAP-018`/`CAP-028` — has no explicit "Pre-condition check"
  callout box even though its own Purpose paragraph states one in prose (Conversation Detection
  enabled via `CAP-019`) — this is exactly the formatting inconsistency across the five that this
  prompt should resolve.
- **`CAP-030` (Group Q items #19–20):** the Log Metadata table already notes the firmware ≥4.467
  requirement, but doesn't cross-reference `PROTOCOL.md` §0.1's `XC-03` open item (whether
  `release_5.203` numerically satisfies a `4.467` threshold is itself unreconciled) as something to
  check or note explicitly before/during this session. The Procedure also gives no practical detail
  for what counts as a safe, sufficiently "sudden loud sound" for `LOUD-001` or what a reasonably
  distinct pair of acoustic environments looks like for `ADAPT-002` beyond one example each, and
  doesn't state whether the official app needs to stay foregrounded during the observation windows
  (directly relevant to `ADAPT-001`'s own open sub-question in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`).

## Task 1 — Add a consistent "Preparation" callout to all five skeletons

For each of the five files, add (or reformat an existing ad hoc note into) a clearly labeled
**Preparation** section directly under Log Metadata — matching the "required — this is the whole
point of the session" callout style already used in `CAP-018`'s "Isolation check" and the newer
`CAP-043`–`CAP-052` skeletons (see reading-order item 12) — that states, as an explicit, checkable
list:

- Any prior capture/setting-state precondition (e.g. `CAP-028` needs Head gestures already on,
  `CAP-029` needs Conversation Detection already on), **plus a fallback instruction** for what to
  do in this same session if that precondition was never separately captured (toggle the setting on
  directly as this session's own first, logged step, rather than assuming it's already true).
- Any firmware-version precondition, cross-referencing `PROTOCOL.md` §0.1's `XC-03` open item where
  relevant (`CAP-028`, `CAP-030`) — state plainly that this is a documentation/reproducibility note,
  not a currently-blocking gate, per that note's own existing conclusion.
- The required connection/app state before the timed action begins (e.g. Buds bonded but Bluetooth
  off for `CAP-018`; Buds connected and settled, app open, for `CAP-026`; app foregrounded state
  explicitly decided for `CAP-030`'s two observation windows, given `ADAPT-001`'s own open
  sub-question about this).
- HCI snoop logging already enabled and confirmed running before any timed action starts, per
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §2/§6's own "a Bluetooth restart is required" caution.

Do not invent a precondition that isn't already established somewhere in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`,
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, or `PROTOCOL.md` — every added line must trace back to one of
those documents (or to this prompt's own Context section above, which already cites its sources).

## Task 2 — Expand each skeleton's Procedure/Execution detail

For each of the five files, expand the Procedure section (or add a short "Execution notes"
paragraph immediately after it, if the Group's own numbered steps from `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
should stay verbatim) so that a preparer who has never run this specific Group before can execute it
without cross-referencing `CAPTURE_BLUETOOTH_HCI_SNOOP.md` mid-session:

- **`CAP-018`:** state explicitly, as step 0, the Bluetooth-off/Buds-bonded/logging-already-running
  starting state from Task 1, before step 1's "enable Bluetooth."
- **`CAP-026`:** state explicitly what "the preceding connect action" and "once traffic visibly
  settles" mean operationally for a first-time preparer (a concrete wait/observation discipline,
  consistent with `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4's own "wait ~5s → note the exact time →
  perform the action → wait ~5–10s" rhythm).
- **`CAP-028`:** add the Head-gestures-enable step as its own numbered/logged step if not already
  done in a prior session, before steps 13–14.
- **`CAP-029`:** add brief decision guidance for the item-16 factory-reset question (e.g., "run
  items 15/17 only unless a from-true-factory-state `PAIR-002` baseline is specifically wanted this
  session — see `WORKSTATION_PREPARATIONS.md`'s Disaster Recovery section before choosing yes").
  Do not decide it — record it as guidance for the maintainer's own decision, per this prompt's own
  guardrails below.
- **`CAP-030`:** add brief practical notes for what a safe, sufficiently sudden loud sound (`LOUD-001`)
  and a clearly distinct pair of acoustic environments (`ADAPT-002`) look like in practice, and
  state explicitly whether the app should stay foregrounded through both observation windows (and
  why, citing `ADAPT-001`'s own open sub-question).

## Task 3 — Verify Event Timeline rows still match the (possibly expanded) Procedure

After Tasks 1–2, re-check each file's Event Timeline table against its own (now-expanded) Procedure
section — add any missing timeline row for a newly-added preparation/execution step (e.g. a
Head-gestures-enable row for `CAP-028`, an explicit "HCI snoop logging confirmed running" row for
`CAP-018`) so the timeline still fully covers every step a preparer would actually log.

## Task 4 — Leave everything else untouched

Do not rename any of the five folders (their real session date/time is still unknown — they remain
placeholders). Do not change any `CAP-NNN` ID, Group letter, or Test-ID. Do not touch
`CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s §9 Capture Index rows for these five (they already correctly say
`planned`) unless a factual error is found in the process (e.g. a wrong Test-ID reference) — if so,
fix it and note the fix explicitly in the result file, don't silently correct it. Do not create any
new capture skeleton, `CAP-NNN`, or Group letter — that is explicitly out of this prompt's scope.

---

## Guardrails

- This is a documentation-quality/completeness pass on five already-existing placeholder skeletons
  — it does not touch any protocol claim and does not go through the FACT/ADR sign-off gate
  (`AGENTS.md` §6/§15).
- Do not perform any of the five captures yourself — this prompt only improves the paperwork the
  maintainer will use to actually run each session with their own hardware.
- Do not fill in any `TBD` field with a guessed or invented value — Task 1/2's additions are
  preparation *guidance* (what to check, what to decide, what "ready" means), not simulated results.
- Every added sentence must trace back to `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`,
  `PROTOCOL.md`, or this prompt's own Context section — do not invent new preconditions, safety
  claims, or test content beyond what's already documented (`PROJECT_RULES.md` §1).
- Do not decide `CAP-029`'s open item-16 (factory reset) question on the maintainer's behalf —
  provide decision-support guidance only, per Task 2.
- Run `python3 scripts/lint_docs.py` after editing and confirm no new unregistered-ID or
  dead-filename-reference findings were introduced.
- Log this session per `AI_SESSION_LOG_PROCEDURE.md`: write
  `ai-sessions/0006_MAINTENANCE_RESULT_2026_09_09.md` with `Status: complete` if all 4 tasks finish,
  `partial — resumed` if not.

## Output

For each of the five files, a short before/after summary of what was added (Preparation callout
content, Procedure/Execution expansion, any Event Timeline rows added) — not a full diff, but
specific enough that the maintainer can see exactly what changed without re-reading the whole file.
List any factual correction made under Task 4, if any.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0006_MAINTENANCE_PROMPT_2026_09_09.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0006_MAINTENANCE_PROMPT_2026_09_09
