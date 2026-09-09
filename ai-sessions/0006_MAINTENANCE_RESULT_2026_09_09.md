# 0006_MAINTENANCE_RESULT_2026_09_09.md — Expand the CAP-018/026/028/029/030 placeholder skeletons into complete test/preparation/execution descriptions

**Number:** 0006
**Category:** MAINTENANCE
**Date:** 2026-09-09
**Title:** Expand the CAP-018/026/028/029/030 placeholder skeletons into complete test/preparation/execution descriptions
**Status:** complete

---

## Mandatory reading order

Read/confirmed in full before editing (carried over from this same chat session, which authored
the prompt itself after already reading these documents in full): `AGENTS.md`, `PROJECT.md`,
`PROJECT_RULES.md`, `DECISIONS.md` (every ADR), `ARCHITECTURE.md`, `PROTOCOL.md` (§0.1's `XC-03`
note, §4.4, §4.5.1, §4.5.4, §4.5.9, §6), `TODO.md`, `AI_SESSION_LOG_PROCEDURE.md`,
`ai-sessions/INDEX.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (full, Groups L/O/P/Q, §6, §9),
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (full), and all five existing skeleton files in full, prior to
editing any of them.

## Task 1 — Preparation callouts added to all five skeletons

Each of the five files now has a clearly labeled **Preparation** section directly under Log
Metadata:

- **`CAP-018`:** states the bonded-but-Bluetooth-off starting state and that HCI snoop logging
  must already be running before Bluetooth is re-enabled, keeping the existing "Isolation check"
  callout alongside it.
- **`CAP-026`:** states that the Buds/app must be connected and "settled" (a concrete ~10s gap
  after the connect action, not the moment of connecting itself) before Window 1, and that
  connection/app state must be logged at the start of each window.
- **`CAP-028`:** states the Head-gestures precondition plus a fallback instruction (enable it as
  this session's own first step if `CAP-020` wasn't run separately), and adds the firmware ≥4.467
  documentation note (cross-referencing `PROTOCOL.md` §0.1's `XC-03` item, explicitly framed as
  non-blocking since Head gestures are already confirmed working since `CAP-020`).
- **`CAP-029`:** states the Conversation Detection precondition plus the same kind of fallback
  instruction, and adds a **Decision guidance** blockquote for the item-16 factory-reset question
  (guidance only — the decision itself is left to the maintainer, per this prompt's own
  guardrails).
- **`CAP-030`:** adds the same firmware-note pattern as `CAP-028` (cross-referencing `XC-03`,
  non-blocking since `ANC-003`/Adaptive is confirmed working since `CAP-001`), a requirement to
  decide and record the app's foreground/background state for both windows (tied to
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` `ADAPT-001`'s own open sub-question), and concrete
  practical/safety notes for what counts as a suitable `LOUD-001` trigger and `ADAPT-002`
  environment pair.

## Task 2 — Procedure/Execution detail expanded

- **`CAP-018`:** added Procedure step 0 (confirm logging running, then bonded/Bluetooth-off state)
  before step 1.
- **`CAP-026`:** Procedure items 42/43 now cross-reference the Preparation section's concrete
  definition of "settled" and "once traffic visibly settles" (≥10s with no new DLCI activity).
- **`CAP-028`:** added Procedure step 0 (conditional Head-gestures-enable step) before steps 13–14.
- **`CAP-029`:** added Procedure step 0 (conditional Conversation-Detection-enable step) before
  step 15; item 16 now cross-references the Decision guidance blockquote.
- **`CAP-030`:** items 19/20 now cross-reference the Preparation section's practical/safety notes
  (safe trigger description for `LOUD-001`, concrete environment-pair examples and hold-time for
  `ADAPT-002`).

## Task 3 — Event Timeline rows added to match

Each file's Event Timeline table gained rows for the newly-added preparation/execution steps:

- `CAP-018`: 2 new rows (logging confirmed running; bonded/Bluetooth-off state confirmed).
- `CAP-026`: 2 new rows (logging confirmed running; connect-and-settle confirmation).
- `CAP-028`: 2 new rows (logging/connected confirmation; conditional Head-gestures-enable row).
- `CAP-029`: 3 new rows (logging/connected confirmation; conditional Conversation-Detection-enable
  row; item-16 decision-recorded row).
- `CAP-030`: 2 new rows (logging/connected/worn confirmation; app foreground/background state
  decision row).

## Task 4 — Existing content otherwise left untouched

No folder was renamed (all five remain `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholders — real session
timestamps are still unknown). No `CAP-NNN`, Group letter, or Test-ID was changed. No `TBD` field
was filled with a guessed value — every addition is preparation/execution *guidance*, not a
simulated result. **No factual error was found** in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's existing
Capture Index rows for these five (`CAP-018`/`CAP-026`/`CAP-028`/`CAP-029`/`CAP-030`) — their
Test-ID lists match what each skeleton actually references, so no correction was made there. No new
capture skeleton, `CAP-NNN`, or Group letter was created — out of this prompt's scope.

## Guardrails observed

- No capture was performed; this is a documentation-quality pass on five already-existing
  placeholder skeletons.
- No protocol claim was touched; no `PROTOCOL.md`/`DECISIONS.md` promotion or ADR was written.
- Every added sentence traces back to `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`,
  `PROTOCOL.md`, or this prompt's own Context section — no new precondition, safety claim, or test
  content was invented.
- `CAP-029`'s item-16 factory-reset question was **not** decided on the maintainer's behalf — only
  decision-support guidance was added, and the file's own "Decide before capturing... Record the
  decision here: TBD" line was left as `TBD`.
- `python3 scripts/lint_docs.py` run after all edits: no new unregistered-ID or dead-filename-
  reference findings introduced by this session's changes (the one dead-reference finding for this
  file's own path, in the paired `PROMPT` file, is expected and resolved by this file's own
  existence; the pre-existing unrelated dead references in `CAP-036`/`CAP-038`/`CAP-039`/`CAP-040`
  predate this session and were not touched).

## Files changed

- `captures/CAP-018-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_Y/CAP-018-EVENT-NOTES.md`
- `captures/CAP-026-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_L/CAP-026-EVENT-NOTES.md`
- `captures/CAP-028-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_O/CAP-028-EVENT-NOTES.md`
- `captures/CAP-029-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_P/CAP-029-EVENT-NOTES.md`
- `captures/CAP-030-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_Q/CAP-030-EVENT-NOTES.md`
- `ai-sessions/INDEX.md` — this session's row `Status` updated from "pending — prompt only, not yet
  run" to "complete"

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0006_MAINTENANCE_RESULT_2026_09_09.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0006_MAINTENANCE_RESULT_2026_09_09
