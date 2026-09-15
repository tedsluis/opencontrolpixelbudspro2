# 0020_CAPTURE_PROMPT_2026_09_14.md — Video-only analysis and CAP-051-EVENT-NOTES.md update for CAP-051 (Group AM)

**Number:** 0020
**Category:** CAPTURE
**Date:** 2026-09-14
**Title:** Video-only analysis of `CAP-051-recording.mp4` and update of `CAP-051-EVENT-NOTES.md` — `.log`
analysis and `CAP-051-FINDINGS.md` are explicitly **out of scope** for this prompt

---

## Scope of this session — read this first

**This session does video analysis only.** It watches `CAP-051-recording.mp4` in full and updates
`CAP-051-EVENT-NOTES.md`'s Event Timeline and Log Metadata with the verified actions/events. It does
**not**:

- open, filter, or analyze `CAP-051-btsnoop_hci.log`;
- write `CAP-051-FINDINGS.md` (that file draws the actual conclusion — whether a
  `field5{field4{field13=N}}` write appears on DLCI 0x02 at the in-app tap and/or the physical
  press-and-hold gesture — which requires wire evidence this session deliberately does not collect);
- flip `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's `CAP-051` row from `planned` to `analyzed` (that status
  means the full video+log workflow is done, per `AGENTS.md` §13 — not true after a video-only pass);
- change `id_registry.csv`'s `CAP-051` status;
- add the new `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` Test-ID that `CAP-051-EVENT-NOTES.md`'s own Purpose
  section flags as a follow-up outside scope — leave that flagged, not actioned.

The log/btsnoop analysis and `CAP-051-FINDINGS.md` remain a **separate, future** task — do not start
them here even if they seem like a natural next step. If, while watching the video, something appears
that makes you want to check the log (e.g. to confirm whether a specific tap produced a DLCI 0x02
write), do **not** open the log file — just note the open question in `CAP-051-EVENT-NOTES.md` for
that future session to pick up.

## How to (re)start this prompt — read this paragraph first, every time

This task is designed to be pasted verbatim into a **new** Gemini CLI chat, including on a resumption
(a single full-length recording may not fit in one pass depending on rate limits/context).

Before anything else: check whether `ai-sessions/0020_CAPTURE_RESULT_2026_09_14.md` already exists.
- **It does not exist** → this is the first run. Start at Phase 0.
- **It already exists** → read it in full. Its per-phase status table (created in Phase 0) says which
  phases are already `done`. Skip straight to the first phase not yet marked `done` and continue from
  there. Regardless of where you resume, you still owe the full "Mandatory reading order" below in
  this session — `AI_SESSION_LOG_PROCEDURE.md` §8 requires it for every session that acts on this
  repo, resumption or not, since a new chat has no memory of a previous one.

Every phase boundary is a safe stopping point: before ending a turn, update
`0020_CAPTURE_RESULT_2026_09_14.md`'s status table and `Status` header field (per
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
5. `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s **Group AM** section (`#### Group AM — qhr field 13
   ANC-parallel-path wire confirmation`) and its §9 Capture Index row for `CAP-051` — for context on
   *why* this capture exists: `REVERSE_ENGINEERING.md`'s `qhr` entry traces ANC state (field 13) to
   two distinct code-side callers (an in-app `QuickActionsFragment` toggle tap, and a physical
   press-and-hold gesture via `gvi`/`gvj`), but neither path has ever been wire-confirmed against a
   DLCI 0x02 write — even though this session does not attempt to answer that wire-level question
   itself (that needs the log, which is out of scope here).
6. `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md`.
7. The current (hand-filled, unverified skeleton) baseline:
   `captures/CAP-051-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AM/CAP-051-EVENT-NOTES.md` — note it is still
   a **skeleton** (`Status: 🔲 Not yet captured`), written before this session's video was watched,
   and every `TBD` in it is unfilled. This prompt's Phase 1 replaces it with a verified account of
   what the video shows — nothing more.

Do **not** read `PROTOCOL.md` §4.1, `REVERSE_ENGINEERING.md`'s full `qhr` entry, or any other
log/code-evidence-heavy document beyond the one-paragraph context above as part of this task — they're
only relevant once the (separate, future) log-analysis session starts.

## Context (from the maintainer, not re-derived here)

The session was actually executed today (2026-09-14) by the maintainer, on a **Pixel 7a**, **Android
17**, Pixel Buds Pro 2 firmware **`release_5.203`**, official Pixel Buds Companion app version
**`1.0.955078536`**, with **Google Play services active** on the phone. Treat these five values as
confirmed (not `TBD`, not `⚪ assumed`) when filling in the Log Metadata table.

There is exactly **one** recording (`CAP-051-recording.mp4`) and **one** log file
(`CAP-051-btsnoop_hci.log`, out of scope here, do not open it).

**The maintainer did not follow the planned procedure exactly as written, and reports this
explicitly — verify it frame-by-frame, do not just transcribe this paragraph into the notes:** the
skeleton's planned procedure (`CAP-051-EVENT-NOTES.md`'s own "Procedure" section) calls for exactly
**two** isolated actions, each performed once, each followed by a ≥10s pause: (1) a single in-app
ANC-mode tap via the `QuickActionsFragment` toggle group on the phone, and (2) a single physical
press-and-hold gesture on one earbud. The maintainer instead reports: the video shows the ANC mode
changing on-screen on the Pixel 7a (plural "modes" — potentially more than one mode transition, not
necessarily a single tap), **and** the maintainer physically pressed and held the **right** earbud,
in the right ear, **twice** ("2x lang ingedrukt") — i.e. two physical press-and-hold gestures, not
the single isolated gesture the skeleton's procedure describes. Do not assume the planned
one-tap/one-gesture structure was followed; determine, from the video alone:

- Exactly how many discrete in-app ANC-mode changes occurred, at what timestamps, and what the
  on-screen ANC-mode transition was for each (e.g. Off → Active Noise Cancellation → Transparency,
  or whichever states the app actually shows — read the exact on-screen label at each step, don't
  infer from memory of the app's usual mode names).
- Exactly how many discrete physical press-and-hold gestures occurred on the right bud, at what
  timestamps, and — as best determinable from the video — the approximate hold duration of each
  (a "long press" gesture; report what's visible, not an assumed fixed duration).
- The actual chronological order of all these actions relative to each other, and whether the ≥10s
  isolation pause the planned procedure calls for was actually observed between each discrete action
  — if two actions occurred closer together than that, flag this explicitly as reducing the future
  log-analysis session's ability to cleanly attribute a DLCI 0x02 write to one specific action. This
  is exactly the kind of ambiguity that matters for Group AM's correlation purpose, so get the count,
  order, and spacing right rather than approximating.
- Confirm from the video that it is genuinely the **right** earbud being pressed (per the
  maintainer's account) — note any visual identifier used (marking, hand/ear position) to support
  this, or flag it as unconfirmed if the video doesn't make the left/right identity clear.

Note in the Event Timeline (as a plain observation, not a conclusion) the exact on-screen ANC-mode
label shown after each in-app tap and after each physical gesture (if visible on the phone screen
during/after the physical gesture too) — but do not draw any wire-level conclusion from this (e.g. do
not conclude whether DLCI 0x02 carries a parallel write); that is exactly the question the
out-of-scope log analysis will answer later.

Beyond the ANC-mode actions themselves, note anything else visible in the video worth recording — app
screens shown, other actions taken, anything unexpected — as plain observations in the Event
Timeline.

## Session bookkeeping & resumability

In Phase 0, create `ai-sessions/0020_CAPTURE_RESULT_2026_09_14.md` with the header block required by
`AI_SESSION_LOG_PROCEDURE.md` §4 (`Status: partial — resumed` to start) and a status table with one
row per phase (0–2). After finishing each phase, update that row to `done` with a one- or two-line
summary, and re-save the file. Do **not** create a new numbered pair for a resumption — per §5, the
same `0020_CAPTURE_RESULT_2026_09_14.md` is progressively appended to.

This prompt does not instruct automatic git commits (project-wide convention: never commit without
being explicitly asked). Mention clearly, at the end of each session's final turn, what is
uncommitted so the maintainer can decide whether to commit before the next session starts.

## Phase 0 — Setup

Complete the Mandatory reading order above. Create `ai-sessions/0020_CAPTURE_RESULT_2026_09_14.md`
with the required header (`Status: partial — resumed`) and an empty phase-status table (Phases 0–2,
all `not started` except Phase 0 itself). Confirm via `ls -la` the actual current contents of
`captures/CAP-051-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AM/` — you should see (at least)
`CAP-051-recording.mp4` and `CAP-051-EVENT-NOTES.md`; the log file may also be present but is not to
be opened. `ffprobe` the `.mp4` file and record its exact duration.

## Phase 1 — Watch the video in full and rewrite `CAP-051-EVENT-NOTES.md`

**No sampling — watch the video in its entirety.** Using `ffmpeg -ss <t> -frames:v 1` (this project's
established method, see `DESKRESEARCH_FINDINGS.md`), extract frames across the video's **full**
duration at an interval dense enough that no on-screen action or physical action (phone screen ANC
label change, finger touching/pressing the right bud, releasing it) could be missed between two
consecutive extracted frames — tighten to sub-second spacing around every in-app tap and every
physical press-and-hold gesture, since getting the exact count, order, timestamps, and hold-duration
of each right, per the Context section above, is the entire point of this phase. Save frames to your
scratchpad directory. Read each one and build an independent, complete, time-stamped action list of
what's actually on screen (phone) and actually visible of the earbud/hand in frame.

Determine:
- The video's exact start and end wall-clock time from the burned-in overlay (this project's primary
  time source for the Event Timeline, per `README.md`).
- Every in-app ANC-mode-change action: timestamp, and the exact before/after on-screen ANC-mode label
  (read literally from the screen, not inferred).
- Every physical press-and-hold gesture on the right bud: timestamp of press start, timestamp of
  release, approximate hold duration, and (if visible) the resulting on-screen ANC-mode label
  afterward.
- The actual chronological order and inter-action spacing of all of the above, flagging any pair of
  consecutive actions spaced less than ~10s apart as reducing future correlation confidence (per the
  Context section above).
- Whether the maintainer's own account (Context section above — ANC modes changed via the app, right
  bud pressed-and-held twice) matches what the footage actually shows — confirm or correct it, do not
  just transcribe it. If the footage shows a different count or order (e.g. only one physical gesture,
  or an in-app action interleaved between the two physical gestures), report what the video actually
  shows.

Rewrite `CAP-051-EVENT-NOTES.md`'s Event Timeline table to the verified real sequence (one row per
distinguishable action; leave the "Wire evidence / Notes" column explicitly
`TBD — pending separate log-analysis session (out of scope for this prompt)` for every row rather than
guessing). Fill in the Log Metadata table: Date `2026-09-14`, Firmware version `release_5.203`, Test
device `Pixel 7a, official Pixel Buds Companion App; full DLCI 0x02 traffic retained, raw-path
extraction` (note explicitly that whether raw-path/non-`btsnooz.py` logging was actually used is a
log-file property this video-only session cannot confirm — leave that specific sub-claim as an open
item for the log-analysis session, do not mark it confirmed just because the skeleton's Test device
cell mentions it), Video file (filename + duration), Log file stays
`TBD — pending separate log-analysis session — CAP-051-btsnoop_hci.log (raw path, not btsnooz.py)`,
Buds MAC field stays `TBD` (per `AGENTS.md` §7/§9, do not attempt to read a MAC address off-screen or
from the log). Update the "Procedure" section's framing if the actual session diverged from the
two-single-isolated-actions structure it describes (per the Context section above) — state plainly
what was actually done instead, without removing the originally planned procedure (keep both, clearly
distinguished, e.g. "Planned procedure (skeleton, unchanged below) vs. what the video actually shows
happened"). Leave every Analysis checklist item unchecked, each with an explicit note that it requires
the separate, out-of-scope log-analysis session.

Rename the capture folder via `git mv`, from
`captures/CAP-051-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AM` to
`captures/CAP-051-2026-09-14_HH-MM-SS_HH-MM-SS-Group_AM`, using the video's actual start and end time
(from the video overlay only — this rename does not depend on the log file). Update the header line
and any internal path references inside `CAP-051-EVENT-NOTES.md` to match the renamed folder.

## Phase 2 — Wrap-up

- Run `python3 scripts/lint_docs.py`; fix any newly introduced dead-filename-reference finding (the
  folder rename is the most likely source — grep for the old placeholder folder name across the repo
  before finishing).
- Run `./scripts/ensure_footers.py` if `CAP-051-EVENT-NOTES.md` is missing its footer after editing.
- Update `ai-sessions/INDEX.md`'s row for `0020` to match this session's final `Status` and this
  title.
- Do **not** touch `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's `CAP-051` status field or `id_registry.csv`
  — both stay `planned` until the separate log-analysis session runs. Do **not** add a new Test-ID to
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` — leave that flagged as a follow-up, per the skeleton's own
  Purpose section.
- Finalize `0020_CAPTURE_RESULT_2026_09_14.md`: `Status: complete` once the video is fully reviewed
  and `CAP-051-EVENT-NOTES.md` is updated and the folder is renamed; `partial — resumed` if genuine
  work remains.
- Write a short final summary for the maintainer: the verified action sequence (count and order of
  in-app taps vs. physical press-and-hold gestures), whether it matched the maintainer's own account,
  any actions spaced closer than the planned ≥10s isolation window, and an explicit reminder that
  `.log` analysis and `CAP-051-FINDINGS.md` are still pending as a separate future task.

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
- Do not decide, after the fact, whether the deviation from the planned single-tap/single-gesture
  procedure invalidates the session or requires a repeat — that judgment is the maintainer's to make
  once the log analysis is done; this task's job is only to document what the footage shows.
- **Do not open, filter, `tshark`, or otherwise analyze `CAP-051-btsnoop_hci.log`, and do not write
  `CAP-051-FINDINGS.md`** — both are explicitly out of scope per the maintainer's instruction for this
  session; leave the corresponding `CAP-051-EVENT-NOTES.md` fields as `TBD` pending that separate
  future session.
- This is a pure video-review/documentation pass — no new Bluetooth action against the hardware is
  performed as part of this prompt.

## Output

At the end of each phase, a short note (in the RESULT file, and to the maintainer if the chat is
still live) of what was found/changed. At the end of Phase 2, the full summary described there. Every
claim in the Event Timeline must cite its evidence (a video timestamp) per `PROJECT_RULES.md` rule 3.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0020_CAPTURE_PROMPT_2026_09_14.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0020_CAPTURE_PROMPT_2026_09_14
