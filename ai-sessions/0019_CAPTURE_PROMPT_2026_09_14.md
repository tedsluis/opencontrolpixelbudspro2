# 0019_CAPTURE_PROMPT_2026_09_14.md — Video-only analysis and CAP-050-EVENT-NOTES.md update for CAP-050 (Group AG repeat)

**Number:** 0019
**Category:** CAPTURE
**Date:** 2026-09-14
**Title:** Video-only analysis of `CAP-050-recording.mp4` and update of `CAP-050-EVENT-NOTES.md` — `.log`
analysis and `CAP-050-FINDINGS.md` are explicitly **out of scope** for this prompt

---

## Scope of this session — read this first

**This session does video analysis only.** It watches `CAP-050-recording.mp4` in full and updates
`CAP-050-EVENT-NOTES.md`'s Event Timeline and Log Metadata with the verified actions/events. It does
**not**:

- open, filter, or analyze `CAP-050-btsnoop_hci.log`;
- write `CAP-050-FINDINGS.md` (that file draws conclusions about whether the trigger used actually
  reopened DLCI 0x08 and about the seven unmapped `[Group][Code][00 00]`-shaped codes'
  correlation against the bracketed dock value — both require wire evidence this session
  deliberately does not collect);
- flip `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's `CAP-050` row from `planned` to `analyzed` (that status
  means the full video+log workflow is done, per `AGENTS.md` §13 — not true after a video-only pass);
- change `id_registry.csv`'s `CAP-050` status.

The log/btsnoop analysis and `CAP-050-FINDINGS.md` remain a **separate, future** task — do not start
them here even if they seem like a natural next step. If, while watching the video, something appears
that makes you want to check the log (e.g. to confirm a reconnect moment's exact wire effect), do
**not** open the log file — just note the open question in `CAP-050-EVENT-NOTES.md` for that future
session to pick up.

## How to (re)start this prompt — read this paragraph first, every time

This task is designed to be pasted verbatim into a **new** Gemini CLI chat, including on a resumption
(a single full-length recording may not fit in one pass depending on rate limits/context).

Before anything else: check whether `ai-sessions/0019_CAPTURE_RESULT_2026_09_14.md` already exists.
- **It does not exist** → this is the first run. Start at Phase 0.
- **It already exists** → read it in full. Its per-phase status table (created in Phase 0) says which
  phases are already `done`. Skip straight to the first phase not yet marked `done` and continue from
  there. Regardless of where you resume, you still owe the full "Mandatory reading order" below in
  this session — `AI_SESSION_LOG_PROCEDURE.md` §8 requires it for every session that acts on this
  repo, resumption or not, since a new chat has no memory of a previous one.

Every phase boundary is a safe stopping point: before ending a turn, update
`0019_CAPTURE_RESULT_2026_09_14.md`'s status table and `Status` header field (per
`AI_SESSION_LOG_PROCEDURE.md` §4/§5: `partial — resumed` while incomplete) so the next session picks
up cleanly.

## Mandatory reading order (do this first, in this order, in every session that works on this prompt)

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8, the floor for any session touching this
repo is `AGENTS.md`, `PROJECT.md`, `PROJECT_RULES.md`, and `DECISIONS.md` in full — read those first,
regardless of this task's narrower scope. Then, specifically for this task:

1. `AGENTS.md` (full) — especially §13's "Analyzing a Bluetooth capture" workflow item 1 (describe
   the user action performed during the capture, record in event notes) and rule item 6's
   "zero creativity, evidence-only" instruction (applies to video-derived claims too, not just hex
   parsing).
2. `PROJECT.md` (full).
3. `PROJECT_RULES.md` (full) — especially rule 1–4a (FACT/HYPOTHESIS/ASSUMPTION/OPEN QUESTION
   labeling and evidence traceability), rule 11 (hypothesis-test reproducibility metadata), rule 13/
   13a (AI session behavior and session logging), rule 14 (capture metadata).
4. `DECISIONS.md` — every ADR, ADR-001 through the most recent (context only; this session is not
   expected to touch any ADR).
5. `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s **Group AG** section (`#### Group AG — DLCI 0x08's unmapped
   Get-shaped codes vs. a known-changing value`) and its §9 Capture Index row for `CAP-050` — for
   context on *why* this capture exists (it is an explicit repeat of `CAP-040`, whose own trigger —
   the app's in-app Connect/Disconnect buttons — turned out to produce zero wire-visible signal,
   per `CAP-040-FINDINGS.md` §1/§3), even though this session does not attempt to answer the DLCI
   0x08 question itself (that needs the log, which is out of scope here).
6. `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md`.
7. The current (hand-filled, unverified skeleton) baseline:
   `captures/CAP-050-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AG/CAP-050-EVENT-NOTES.md` — note it is still
   a **skeleton** (`Status: 🔲 Not yet captured`), written before this session's video was watched,
   and every `TBD` in it is unfilled. This prompt's Phase 1 replaces it with a verified account of
   what the video shows — nothing more.

Do **not** read `CAP-040-FINDINGS.md` in more depth than needed for the one-paragraph context above,
`PROTOCOL.md` §6, or any other log-evidence-heavy document as part of this task — they're only
relevant once the (separate, future) log-analysis session starts.

## Context (from the maintainer, not re-derived here)

The session was actually executed today (2026-09-14) by the maintainer, on a **Pixel 7a**, **Android
17**, Pixel Buds Pro 2 firmware **`release_5.203`**, official Pixel Buds Companion app version
**`1.0.955078536`**, with **Google Play services active** on the phone. Treat these five values as
confirmed (not `TBD`, not `⚪ assumed`) when filling in the Log Metadata table.

There is exactly **one** recording (`CAP-050-recording.mp4`) and **one** log file
(`CAP-050-btsnoop_hci.log`, out of scope here, do not open it) — unlike some other captures in this
project, there is no `.log.last` or `-2.log`/`-recording-2.mp4` pair to reconcile.

**The maintainer did not follow the planned procedure cleanly, and reports the deviation explicitly —
verify it frame-by-frame, do not just transcribe this paragraph into the notes:** the skeleton's
planned procedure (`CAP-050-EVENT-NOTES.md`'s own "Procedure" section, mirrored from
`CAP-040-EVENT-NOTES.md`) calls for ~10–15 clean reconnect cycles, each one bracketing a specific,
identifiable dock-state value (which of Left/Right/both/neither is docked) using a trigger confirmed
to reopen DLCI 0x08 (the OS-level Bluetooth toggle, or physical case/bud docking/undocking) — **not**
the app's own in-app Connect/Disconnect buttons. The maintainer reports that, during preparation and
execution, they did **not** always follow this exact procedure: the **order** in which the Left and
Right buds were placed (back in the case, or removed) and the **order** relative to closing the case
lid sometimes varied from what a clean, single-variable-at-a-time protocol would prescribe — e.g. a
bud might have been placed after the lid was already being closed, or the two buds placed in a
different order than planned, on some cycles. Do not assume the planned procedure was followed as
written; determine, from the video alone, what actually happened at each cycle: which physical action
occurred, in what order, at what timestamp, and whether the case lid was open or closed at each
moment. If a given cycle's bracketed dock-state value is genuinely ambiguous from the video (e.g. it
isn't clear which component was docked at the moment of reconnect because of the order deviation),
report that ambiguity explicitly as an open question for that row rather than guessing which value
was intended.

Note in the Event Timeline (as a plain observation, not a conclusion) whether the on-screen official
app UI (Device details screen, per the skeleton's Test device field) showed Left/Right/Case
battery/connection values changing at each relevant moment — but do not draw any wire-level
conclusion from this (e.g. do not conclude DLCI 0x08 did or didn't reopen); that is exactly the
question the out-of-scope log analysis will answer later.

Beyond the reconnect cycles themselves, note anything else visible in the video worth recording — app
screens shown, other actions taken, anything unexpected — as plain observations in the Event
Timeline.

## Session bookkeeping & resumability

In Phase 0, create `ai-sessions/0019_CAPTURE_RESULT_2026_09_14.md` with the header block required by
`AI_SESSION_LOG_PROCEDURE.md` §4 (`Status: partial — resumed` to start) and a status table with one
row per phase (0–2). After finishing each phase, update that row to `done` with a one- or two-line
summary, and re-save the file. Do **not** create a new numbered pair for a resumption — per §5, the
same `0019_CAPTURE_RESULT_2026_09_14.md` is progressively appended to.

This prompt does not instruct automatic git commits (project-wide convention: never commit without
being explicitly asked). Mention clearly, at the end of each session's final turn, what is
uncommitted so the maintainer can decide whether to commit before the next session starts.

## Phase 0 — Setup

Complete the Mandatory reading order above. Create `ai-sessions/0019_CAPTURE_RESULT_2026_09_14.md`
with the required header (`Status: partial — resumed`) and an empty phase-status table (Phases 0–2,
all `not started` except Phase 0 itself). Confirm via `ls -la` the actual current contents of
`captures/CAP-050-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AG/` — you should see (at least)
`CAP-050-recording.mp4` and `CAP-050-EVENT-NOTES.md`; the log file may also be present but is not to
be opened. `ffprobe` the `.mp4` file and record its exact duration.

## Phase 1 — Watch the video in full and rewrite `CAP-050-EVENT-NOTES.md`

**No sampling — watch the video in its entirety.** Using `ffmpeg -ss <t> -frames:v 1` (this project's
established method, see `DESKRESEARCH_FINDINGS.md`), extract frames across the video's **full**
duration at an interval dense enough that no on-screen action or physical action (bud picked up, bud
placed in a slot, case lid opened/closed, app screen change) could be missed between two consecutive
extracted frames — tighten to sub-second spacing around every reconnect cycle, since telling exactly
which bud went where, in what order, relative to the case lid, is the entire point of this phase
(deviations from the planned procedure specifically hinge on getting this order right). Save frames
to your scratchpad directory. Read each one and build an independent, complete, time-stamped action
list of what's actually on screen (and, for the buds/case, actually in view of the camera).

Determine:
- The video's exact start and end wall-clock time from the burned-in overlay (this project's primary
  time source for the Event Timeline, per `README.md`).
- Every reconnect-relevant physical action: which bud (Left/Right, identify from video — e.g. any
  visible marking, the hand/position used, or the maintainer's own narration if audible) was
  placed/removed, in the case or out, and whether the case lid was open/closed at each moment, in the
  **actual** order the video shows.
- For each distinguishable reconnect cycle, what dock-state value (Left only / Right only / both /
  neither docked) was actually in effect at the moment of reconnect — and, per the Context section
  above, flag as an explicit open question any cycle where the order deviation makes this genuinely
  ambiguous from video alone, rather than picking the value that "should" have been intended.
- Whether the maintainer's own account (Context section above — order deviations occurred) matches
  what the footage actually shows — confirm or correct it, do not just transcribe it. If the footage
  shows the planned procedure was followed cleanly for some or all cycles despite the maintainer's
  general recollection of deviating, report that too.
- Any on-screen indication (official app UI, if visible) of connection/dock/battery/case values at
  each relevant moment — record as a plain observation only, no wire-level interpretation.

Rewrite `CAP-050-EVENT-NOTES.md`'s Event Timeline table to the verified real sequence (one row per
distinguishable action/cycle; leave the "Wire evidence / Notes" column explicitly
`TBD — pending separate log-analysis session (out of scope for this prompt)` for every row rather than
guessing). Fill in the Log Metadata table: Date `2026-09-14`, Firmware version `release_5.203`, Test
device `Pixel 7a, official Pixel Buds Companion App + GMS enabled` (confirm from video whether the app
was actually open on Device details throughout, per the skeleton's expectation, and note any
deviation), Video file (filename + duration), Log file stays
`TBD — pending separate log-analysis session — CAP-050-btsnoop_hci.log`, Buds MAC field stays `TBD`
(per `AGENTS.md` §7/§9, do not attempt to read a MAC address off-screen or from the log). Fill in the
"Trigger check" paragraph with what the video actually shows was used as the trigger at each cycle
(OS Bluetooth toggle vs. physical dock/undock) — note explicitly that confirming this trigger actually
reopened DLCI 0x08 on the wire is deferred to the log-analysis session; this session can only report
what physical/UI action was performed. Leave every Analysis checklist item unchecked, each with an
explicit note that it requires the separate, out-of-scope log-analysis session.

Rename the capture folder via `git mv`, from
`captures/CAP-050-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AG` to
`captures/CAP-050-2026-09-14_HH-MM-SS_HH-MM-SS-Group_AG`, using the video's actual start and end time
(from the video overlay only — this rename does not depend on the log file). Update the header line
and any internal path references inside `CAP-050-EVENT-NOTES.md` to match the renamed folder.

## Phase 2 — Wrap-up

- Run `python3 scripts/lint_docs.py`; fix any newly introduced dead-filename-reference finding (the
  folder rename is the most likely source — grep for the old placeholder folder name across the repo
  before finishing).
- Run `./scripts/ensure_footers.py` if `CAP-050-EVENT-NOTES.md` is missing its footer after editing.
- Update `ai-sessions/INDEX.md`'s row for `0019` to match this session's final `Status` and this
  title.
- Do **not** touch `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's `CAP-050` status field or `id_registry.csv`
  — both stay `planned` until the separate log-analysis session runs.
- Finalize `0019_CAPTURE_RESULT_2026_09_14.md`: `Status: complete` once the video is fully reviewed
  and `CAP-050-EVENT-NOTES.md` is updated and the folder is renamed; `partial — resumed` if genuine
  work remains.
- Write a short final summary for the maintainer: the verified reconnect-cycle sequence, whether it
  matched the maintainer's own account of the order deviations, any cycle left ambiguous, and an
  explicit reminder that `.log` analysis and `CAP-050-FINDINGS.md` are still pending as a separate
  future task.

## Guardrails

- Never invent a timestamp or on-screen detail not actually observed in the video — an unclear frame
  is an explicit open question, not a filled-in guess (`PROJECT_RULES.md` rule 1; `AGENTS.md` §13's
  "zero creativity, evidence-only" instruction).
- Never log the Buds' MAC address at INFO level or above (not expected to come up in a video-only
  pass, but stated for completeness per `AGENTS.md` §9).
- Use `git mv` for the folder rename — never a plain `mv` — so git history (and Git LFS-tracked
  binaries, `PROJECT_RULES.md` rule 18) survive the rename.
- No sampling: the video is watched in full — a partial pass is not an acceptable substitute (per the
  maintainer's explicit instruction, "Doe geen steekproeven, maar doe de analyse volledig").
- Do not decide, after the fact, whether the order deviations were "acceptable" or invalidate the
  session — that judgment (and whether a repeat capture is needed) is the maintainer's to make once
  the log analysis is done; this task's job is only to document what the footage shows.
- **Do not open, filter, `tshark`, or otherwise analyze `CAP-050-btsnoop_hci.log`, and do not write
  `CAP-050-FINDINGS.md`** — both are explicitly out of scope per the maintainer's instruction for this
  session; leave the corresponding `CAP-050-EVENT-NOTES.md` fields as `TBD` pending that separate
  future session.
- This is a pure video-review/documentation pass — no new Bluetooth action against the hardware is
  performed as part of this prompt.

## Output

At the end of each phase, a short note (in the RESULT file, and to the maintainer if the chat is
still live) of what was found/changed. At the end of Phase 2, the full summary described there. Every
claim in the Event Timeline must cite its evidence (a video timestamp) per `PROJECT_RULES.md` rule 3.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0019_CAPTURE_PROMPT_2026_09_14.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0019_CAPTURE_PROMPT_2026_09_14
