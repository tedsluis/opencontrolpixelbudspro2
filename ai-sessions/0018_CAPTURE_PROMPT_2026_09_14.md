# 0018_CAPTURE_PROMPT_2026_09_14.md — Video-only analysis and CAP-047-EVENT-NOTES.md update for CAP-047 (Group AL, Trigger 3 only)

**Number:** 0018
**Category:** CAPTURE
**Date:** 2026-09-14
**Title:** Video-only analysis of `CAP-047`'s two recordings and update of `CAP-047-EVENT-NOTES.md` — `.log`/`.log.last`/`-2.log` analysis and `CAP-047-FINDINGS.md` are explicitly **out of scope** for this prompt

---

## Scope of this session — read this first

**This session does video analysis only.** It watches `CAP-047-recording.mp4` and
`CAP-047-recording-2.mp4` in full and updates `CAP-047-EVENT-NOTES.md`'s Event Timeline and Log
Metadata with the verified actions/events. It does **not**:

- open, filter, or analyze any of the three `.log`/`.log.last` files in the capture folder
  (`CAP-047-btsnoop_hci.log`, `CAP-047-btsnoop_hci.log.last`, `CAP-047-btsnoop_hci-2.log`);
- write `CAP-047-FINDINGS.md` (that file draws conclusions about the DLCI 0x0a burst trigger
  hypothesis, which requires wire evidence this session deliberately does not collect);
- flip `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's `CAP-047` row from `planned` to `analyzed` (that status
  means the full video+log workflow is done, per `AGENTS.md` §13 — not true after a video-only pass);
- change `id_registry.csv`'s `CAP-047` status.

The log/btsnoop analysis and `CAP-047-FINDINGS.md` remain a **separate, future** task — do not start
them here even if they seem like a natural next step. If, while watching the videos, something
appears that makes you want to check the log (e.g. to confirm a docking moment's exact effect), do
**not** open the log files — just note the open question in `CAP-047-EVENT-NOTES.md` for that future
session to pick up.

## How to (re)start this prompt — read this paragraph first, every time

This task is designed to be pasted verbatim into a **new** Gemini CLI chat, including on a
resumption (two full-length videos may not fit in one pass depending on rate limits/context).

Before anything else: check whether `ai-sessions/0018_CAPTURE_RESULT_2026_09_14.md` already exists.
- **It does not exist** → this is the first run. Start at Phase 0.
- **It already exists** → read it in full. Its per-phase status table (created in Phase 0) says
  which phases are already `done`. Skip straight to the first phase not yet marked `done` and
  continue from there. Regardless of where you resume, you still owe the full "Mandatory reading
  order" below in this session — `AI_SESSION_LOG_PROCEDURE.md` §8 requires it for every session that
  acts on this repo, resumption or not, since a new chat has no memory of a previous one.

Every phase boundary is a safe stopping point: before ending a turn, update
`0018_CAPTURE_RESULT_2026_09_14.md`'s status table and `Status` header field (per
`AI_SESSION_LOG_PROCEDURE.md` §4/§5: `partial — resumed` while incomplete) so the next session picks
up cleanly.

## Mandatory reading order (do this first, in this order, in every session that works on this prompt)

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8, the floor for any session touching this
repo is `AGENTS.md`, `PROJECT.md`, `PROJECT_RULES.md`, and `DECISIONS.md` in full — read those
first, regardless of this task's narrower scope. Then, specifically for this task:

1. `AGENTS.md` (full) — especially §13's "Analyzing a Bluetooth capture" workflow item 1 (describe
   the user action performed during the capture, record in event notes) and its "zero creativity,
   evidence-only" instruction (applies to video-derived claims too, not just hex parsing).
2. `PROJECT.md` (full).
3. `PROJECT_RULES.md` (full) — especially rule 1/4a (FACT/HYPOTHESIS/ASSUMPTION/OPEN QUESTION
   labeling), rule 11 (reproducibility metadata), rule 13/13a (AI session behavior), rule 14
   (capture metadata).
4. `DECISIONS.md` — every ADR, ADR-001 through the most recent (context only; this session is not
   expected to touch any ADR).
5. `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s **Group AL** section (`#### Group AL — DLCI 0x0a burst
   trigger, purpose-built hypothesis test`) — for context on *why* this capture exists, even though
   this session does not attempt to answer the DLCI 0x0a question itself (that needs the log, which
   is out of scope here).
6. `TODO.md`'s line noting `CAP-047`'s skeleton was created 2026-09-09 (context only).
7. `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md`.
8. The current (hand-filled, unverified skeleton) baseline:
   `captures/CAP-047-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AL/CAP-047-EVENT-NOTES.md` — note it is still
   a **skeleton** (`Status: 🔲 Not yet captured`), written before this session's videos were watched,
   and every `TBD` in it is unfilled. This prompt's Phase 1 replaces it with a verified account of
   what the videos show — nothing more.

Do **not** read `CAP-021-FINDINGS.md`, `CAP-049-FINDINGS.md`, `PROTOCOL.md` §6, or any other
log-evidence-heavy document as part of this task — they're only relevant once the (separate, future)
log-analysis session starts.

## Context (from the maintainer, not re-derived here)

The session was actually executed today (2026-09-14) by the maintainer, on a **Pixel 7a**, **Android
17**, Pixel Buds Pro 2 firmware **`release_5.203`**, official Pixel Buds Companion app version
**`1.0.955078536`**, with **Google Play services active** on the phone. Treat these five values as
confirmed (not `TBD`, not `⚪ assumed`) when filling in the Log Metadata table.

**Only Trigger candidate 3 (charge-state change, dock/undock) was actually run this session** —
Trigger candidates 1 (app backgrounded/foregrounded) and 2 (scheduled sync window) were **not**
exercised. Do not write up this session as if it covered all three; the `CAP-047-EVENT-NOTES.md`
Analysis checklist items for Triggers 1 and 2 stay unchecked/open, explicitly noted as "not
attempted this session." The Trigger 3 checklist item itself (did the DLCI 0x0a burst appear?)
cannot be answered from video alone either — leave it explicitly open too, noted as "requires the
separate log-analysis session (out of scope here)," not silently checked or guessed at.

**The maintainer did not follow the planned procedure cleanly, and reports the deviation explicitly
— verify it frame-by-frame, do not just transcribe this paragraph into the notes:**

- **First recording (`CAP-047-recording.mp4`):** while docking the buds into the case, the
  maintainer placed the **Left** bud into the case's **Right** slot and the **Right** bud into the
  case's **Left** slot — i.e. swapped left/right — and, per the maintainer's own account, the buds
  did **not** register as "docked" in this configuration.
- **Second recording (`CAP-047-recording-2.mp4`):** the maintainer repeated the docking attempt, and
  **again** placed the buds in the swapped (wrong-side) configuration first — then, immediately
  afterward, in the same recording, corrected this by placing both buds in their proper (matching)
  slots.
- This means the actual event sequence to verify from video evidence is **not** a single clean "buds
  docked" transition — it is: (1) swapped-side placement attempt #1 (video 1), (2) swapped-side
  placement attempt #2 (video 2), (3) corrected placement (video 2, immediately after #2). Determine
  each of these three physical events' exact timestamp from each video's burned-in wall-clock
  overlay independently — do not assume the maintainer's own recollection of ordering/timing is
  precise, confirm it against the footage. If the footage shows something different (e.g. only one
  bud swapped, not both; a different order; an additional attempt), report what the video actually
  shows, not the maintainer's recollection.

Note in the Event Timeline (as a plain observation, not a conclusion) whether the buds visibly
appeared to charge/register in each configuration based on any on-screen indication (official app
UI, case LED if visible, etc.) — but do not draw any wire-level conclusion from this; that is exactly
the question the out-of-scope log analysis will answer later.

Beyond the docking sequence itself, note anything else visible in either video worth recording —
app screens shown, other actions taken, anything unexpected — as plain observations in the Event
Timeline.

## Session bookkeeping & resumability

In Phase 0, create `ai-sessions/0018_CAPTURE_RESULT_2026_09_14.md` with the header block required by
`AI_SESSION_LOG_PROCEDURE.md` §4 (`Status: partial — resumed` to start) and a status table with one
row per phase (0–2). After finishing each phase, update that row to `done` with a one- or two-line
summary, and re-save the file. Do **not** create a new numbered pair for a resumption — per §5, the
same `0018_CAPTURE_RESULT_2026_09_14.md` is progressively appended to.

This prompt does not instruct automatic git commits (project-wide convention: never commit without
being explicitly asked). Mention clearly, at the end of each session's final turn, what is
uncommitted so the maintainer can decide whether to commit before the next session starts.

## Phase 0 — Setup

Complete the Mandatory reading order above. Create `ai-sessions/0018_CAPTURE_RESULT_2026_09_14.md`
with the required header (`Status: partial — resumed`) and an empty phase-status table (Phases 0–2,
all `not started` except Phase 0 itself). Confirm via `ls -la` the actual current contents of
`captures/CAP-047-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AL/` — you should see (at least)
`CAP-047-recording.mp4`, `CAP-047-recording-2.mp4`, and `CAP-047-EVENT-NOTES.md`; the three log
files may also be present but are not to be opened. `ffprobe` both `.mp4` files and record their
exact durations.

## Phase 1 — Watch both videos in full and rewrite `CAP-047-EVENT-NOTES.md`

**No sampling — watch each video in its entirety.** Using `ffmpeg -ss <t> -frames:v 1` (this
project's established method, see `DESKRESEARCH_FINDINGS.md`), extract frames across each video's
**full** duration at an interval dense enough that no on-screen action or physical action (bud
picked up, bud placed in a slot, case lid opened/closed, app screen change) could be missed between
two consecutive extracted frames — tighten to sub-second spacing around each of the three docking
attempts described in the Context section above, since telling "swapped slot" from "correct slot"
apart, and pinning the exact moment each attempt happens, is the entire point of this phase. Save
frames to your scratchpad directory. Read each one and build an independent, complete, time-stamped
action list from what's actually on screen (and, for the buds/case, actually in view of the camera)
for **each** recording separately.

For each recording, determine:
- Its exact start and end wall-clock time from the burned-in overlay (this project's primary time
  source for the Event Timeline, per `README.md`).
- Every physical docking-relevant action: which bud (Left/Right, identify from video — e.g. any
  visible marking, the hand/position used, or the maintainer's own narration if audible) went into
  which case slot, and whether the case lid was open/closed at each moment.
- Whether the maintainer's own account (Context section above) matches what the footage actually
  shows — confirm or correct it, do not just transcribe it.
- Any on-screen indication (official app UI, if visible) of connection/dock/battery/charging state
  at each relevant moment — record as a plain observation only, no wire-level interpretation.

Rewrite `CAP-047-EVENT-NOTES.md`'s Event Timeline table to the verified real sequence across both
recordings (mark clearly which row belongs to which recording; leave the "Wire evidence / Notes"
column explicitly `TBD — pending separate log-analysis session (out of scope for this prompt)` for
every row rather than guessing). Fill in the Log Metadata table: Date `2026-09-14`, Test device
`Pixel 7a`, Android `17`, Firmware `release_5.203`, App version `1.0.955078536`, Google Play
services active — plus both video filenames and their durations. Leave the Log file field as
`TBD — pending separate log-analysis session`. Update the "Method choice" line to state plainly that
only Trigger candidate 3 was run, across two recordings due to the mis-docking repeat, not as three
separate bracketed sub-sessions per the original skeleton's framing. Leave every Analysis checklist
item unchecked with an explicit note per the Context section above (Triggers 1/2: not attempted;
Trigger 3: video confirms the physical sequence, wire-level burst question deferred to the
log-analysis session).

Rename the capture folder via `git mv`, from
`captures/CAP-047-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AL` to
`captures/CAP-047-2026-09-14_HH-MM-SS_HH-MM-SS-Group_AL`, using the earliest recording's start time
and the latest recording's end time (both from the video overlays only — this rename does not depend
on the log files). Update the header line and any internal path references inside
`CAP-047-EVENT-NOTES.md` to match the renamed folder.

## Phase 2 — Wrap-up

- Run `python3 scripts/lint_docs.py`; fix any newly introduced dead-filename-reference finding (the
  folder rename is the most likely source — grep for the old placeholder folder name across the
  repo before finishing).
- Run `./scripts/ensure_footers.py` if `CAP-047-EVENT-NOTES.md` is missing its footer after editing.
- Update `ai-sessions/INDEX.md`'s row for `0018` to match this session's final `Status` and this
  narrower title.
- Do **not** touch `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's `CAP-047` status field or `id_registry.csv`
  — both stay `planned` until the separate log-analysis session runs.
- Finalize `0018_CAPTURE_RESULT_2026_09_14.md`: `Status: complete` once both videos are fully
  reviewed and `CAP-047-EVENT-NOTES.md` is updated and the folder is renamed; `partial — resumed` if
  genuine work remains.
- Write a short final summary for the maintainer: the verified docking sequence across both
  recordings, whether it matched the maintainer's own account, and an explicit reminder that
  `.log`/`.log.last`/`-2.log` analysis and `CAP-047-FINDINGS.md` are still pending as a separate
  future task.

## Guardrails

- Never invent a timestamp or on-screen detail not actually observed in the video — an unclear frame
  is an explicit open question, not a filled-in guess (`PROJECT_RULES.md` rule 1;
  `AGENTS.md` §13's "zero creativity, evidence-only" instruction).
- Never log the Buds' MAC address at INFO level or above (not expected to come up in a video-only
  pass, but stated for completeness per `AGENTS.md` §9).
- Use `git mv` for the folder rename — never a plain `mv` — so git history (and Git LFS-tracked
  binaries, `PROJECT_RULES.md` rule 18) survive the rename.
- No sampling: both videos are watched in full — a partial pass is not an acceptable substitute
  (per the maintainer's explicit instruction, "Doe geen steekproeven, maar doe de analyse volledig").
- Do not decide, after the fact, whether the mis-docking deviation was "acceptable" — it already
  happened; this task's job is only to document what the footage shows, not to judge the
  maintainer's on-the-day execution or to draw any wire-level conclusion from it.
- **Do not open, filter, `tshark`, or otherwise analyze any `.log`/`.log.last` file in this task,
  and do not write `CAP-047-FINDINGS.md`** — both are explicitly out of scope per the maintainer's
  instruction for this session; leave the corresponding `CAP-047-EVENT-NOTES.md` fields as `TBD`
  pending that separate future session.
- This is a pure video-review/documentation pass — no new Bluetooth action against the hardware is
  performed as part of this prompt.

## Output

At the end of each phase, a short note (in the RESULT file, and to the maintainer if the chat is
still live) of what was found/changed. At the end of Phase 2, the full summary described there.
Every claim in the Event Timeline must cite its evidence (a video timestamp) per
`PROJECT_RULES.md` rule 3.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0018_CAPTURE_PROMPT_2026_09_14.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0018_CAPTURE_PROMPT_2026_09_14
