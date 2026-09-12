# 0010_CAPTURE_PROMPT_2026_09_12.md — Full video+log re-analysis and FINDINGS for CAP-018, CAP-026, CAP-028, CAP-029, CAP-045, CAP-046, CAP-048, CAP-049

**Number:** 0010
**Category:** CAPTURE
**Date:** 2026-09-12
**Title:** Full video+log re-analysis and FINDINGS for CAP-018, CAP-026, CAP-028, CAP-029, CAP-045, CAP-046, CAP-048, CAP-049

---

## How to (re)start this prompt — read this paragraph first, every time

This is a large, multi-phase task that **will** span more than one chat session (rate limits,
context resets, or the maintainer simply stopping for the day). It is designed to be pasted
verbatim into a **new** Claude Code chat, including on a resumption.

Before anything else: check whether `ai-sessions/0010_CAPTURE_RESULT_2026_09_12.md` already
exists.
- **It does not exist** → this is the first run. Start at Phase 0.
- **It already exists** → read it in full. Its per-capture status table (created in Phase 0) says
  which phases are already `done`. Skip straight to the first phase not yet marked `done` and
  continue from there — do not redo a `done` phase's work. Regardless of where you resume, you
  still owe the full "Mandatory reading order" below in this session — `AI_SESSION_LOG_PROCEDURE.md`
  §8 requires it for every session that acts on this repo, resumption or not, since a new chat has
  no memory of a previous one.

Every phase boundary is a safe stopping point: before ending a turn (whether because the phase is
done, or because you sense a rate limit / context limit approaching), update
`0010_CAPTURE_RESULT_2026_09_12.md`'s status table and `Status` header field (per
`AI_SESSION_LOG_PROCEDURE.md` §4/§5: `partial — resumed` while incomplete) so the next session
picks up cleanly. Do not leave a phase half-done without a note in the result file describing
exactly what was and wasn't finished.

## Mandatory reading order (do this first, in this order, in every session that works on this prompt)

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8:

1. `AGENTS.md` (full).
2. `PROJECT.md` (full).
3. `PROJECT_RULES.md` (full) — especially rule 1/4a (FACT/HYPOTHESIS/ASSUMPTION/OPEN QUESTION
   labeling and the hex-and-script rule), rule 11 (reproducibility metadata), rule 13/13a (AI
   session behavior), rule 14 (capture metadata), rule 19 (no MAC addresses/personal data logged
   in the clear).
4. `DECISIONS.md` — every ADR, ADR-001 through the most recent. Pay particular attention to
   **ADR-024** (the "Notify ANC state" `Settable-toggles` byte tracks dock state and is
   trigger-independent) — directly relevant to `CAP-048`/`CAP-049`, both repeats of sessions that
   fed that ADR.
5. `ARCHITECTURE.md`.
6. `PROTOCOL.md` (full) — especially §4.3 (battery mechanisms), §4.5.3 (ANC-mode rotation
   checklist / `HOLD-005`), §4.5.7 (Volume balance / field 17), and §6's open items for
   `GATT-002`, `BATT-001`, `OBS-001`, `HEAD-002`, `HEAD-003`, `CONV-002`, `CASE-007`, `CASE-008`,
   `PAIR-002`, `HOLD-005`, `AUDIO-003`, `OBS-004`, `OBS-006`.
7. `TODO.md` (full).
8. `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md`.
9. `CAPTURE_BLUETOOTH_HCI_SNOOP.md` in full — specifically:
   - §2/§6's log-rotation note (why `.log`/`.log.last` pairs happen at all).
   - §4's Group sections **Y**, **L**, **O**, **P**, **AD**, **AF**, **AJ**, **AK** (the last two
     added 2026-09-09 and reused verbatim for `CAP-045`/`CAP-046`; AD/AF are the *original*
     Group definitions that `CAP-037`/`CAP-039` ran and that `CAP-048`/`CAP-049` repeat the
     *anomaly*, not necessarily the original procedure, from — read both the original Group
     text and the specific repeat-purpose wording in the §9 table row).
   - §9's Capture Index rows for `CAP-016`, `CAP-018`, `CAP-019`, `CAP-020`, `CAP-026`, `CAP-028`,
     `CAP-029`, `CAP-037`, `CAP-039`, `CAP-040`, `CAP-041`, `CAP-042`, `CAP-045`, `CAP-046`,
     `CAP-048`, `CAP-049`, and the "Column notes" subsection right after the table.
10. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` — the catalog rows for `GATT-002`, `BATT-001`, `OBS-001`,
    `HEAD-002`, `HEAD-003`, `CONV-002`, `CASE-007`, `CASE-008`, `PAIR-002`, `HOLD-005`,
    `AUDIO-003`, `OBS-004`, `OBS-006`.
11. `DESKRESEARCH_FINDINGS.md` in full — its template, its status legend, and its existing
    entries that validate a wire finding against an external spec (this is the pattern to follow
    for this prompt's own "validate against internet sources" requirement).
12. The following existing `CAP-NNN-FINDINGS.md` files, read in full — this task builds directly
    on them:
    - `CAP-016-FINDINGS.md` §11 (the `0x0044` burst `CAP-018` isolates).
    - `CAP-019-FINDINGS.md` (Conversation Detection enable, `CAP-029`'s precondition).
    - `CAP-020-FINDINGS.md` (Head gestures enable, `CAP-028`'s precondition).
    - `CAP-037-FINDINGS.md` (the dock-state anomaly `CAP-048` repeats).
    - `CAP-039-FINDINGS.md` (the disconnect/reconnect cycling `CAP-049` repeats).
    - `CAP-040-FINDINGS.md` and `CAP-042-FINDINGS.md` — **read these closely.** Both independently
      concluded that a same-folder `.log.last` file was *not* this session's own rotated data, but
      leftover on-device buffer content from the *previous* capture plus the idle gap before the
      one being analyzed — and documented the reasoning that led to that conclusion. `CAP-048` and
      `CAP-049` (this prompt's scope) each have their own `.log.last`; do not assume either
      outcome (rotation vs. leftover) without repeating that same kind of evidence-based check —
      see Step G of the shared methodology below.
13. All 8 existing skeleton files, in full, as the current (admittedly imperfect, per the Context
    below) baseline:
    - `captures/CAP-018-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_Y/CAP-018-EVENT-NOTES.md`
    - `captures/CAP-026-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_L/CAP-026-EVENT-NOTES.md`
    - `captures/CAP-028-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_O/CAP-028-EVENT-NOTES.md`
    - `captures/CAP-029-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_P/CAP-029-EVENT-NOTES.md`
    - `captures/CAP-045-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AJ/CAP-045-EVENT-NOTES.md`
    - `captures/CAP-046-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AK/CAP-046-EVENT-NOTES.md`
    - `captures/CAP-048-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AD/CAP-048-EVENT-NOTES.md`
    - `captures/CAP-049-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AF/CAP-049-EVENT-NOTES.md`

    None of these 8 folders have been renamed from their `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder
    yet, even though the sessions have actually been run (video + log files already present)
    — that rename is part of this prompt's own work (Step F below).

## Context (from the maintainer, not re-derived here)

All 8 sessions were actually executed today (2026-09-12) by the maintainer, on a **Pixel 7a**,
**Android 17**, Pixel Buds Pro 2 firmware **`release_5.203`**, official Pixel Buds app version
**`1.0.955078536`**, with **Google Play services active** on the phone. Treat these five values as
confirmed (not `TBD`, not `⚪ assumed`) when filling in every Log Metadata table and
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 row in this prompt's scope. Note GMS being active on the test
phone is a fact about the test environment, not a violation of `AGENTS.md` §1 (that rule governs
the app being *built*, not the phone it's tested against) — but it is analytically relevant: GMS's
own Fast Pair/Nearby stack can generate BLE scan/advertisement traffic that has nothing to do with
`libmaestro`, so don't misattribute GMS-originated frames to the app under reverse-engineering
without checking.

Each `CAP-NNN-EVENT-NOTES.md` in scope was filled in **by hand** today and may not accurately
reflect what was actually done: the maintainer did not always follow the planned step order,
and in several cases deliberately performed **extra** actions beyond the original plan to capture
more events in one session — this makes the captures richer, not unusable, but it means the
existing Event Timeline tables cannot be trusted as-is. **Every video must be watched in full and
every log analyzed in full** before any timeline or finding is finalized — no sampling, no
skipping sections, no trusting the draft timeline's stated order/times without independent
video/log confirmation.

Two of the eight (`CAP-048`, `CAP-049`) have both a `CAP-NNN-btsnoop_hci.log` and a
`CAP-NNN-btsnoop_hci.log.last` because the maintainer isn't sure whether the on-device log
rotated mid-session or Bluetooth was toggled off/on during the session. **Do not assume either
answer and do not blindly concatenate them** — see Step G below; `CAP-040`/`CAP-041`/`CAP-042`
already hit this exact question twice and both times the `.log.last` turned out to be leftover
data from the *previous* capture, not this session's own rotated data. `CAP-049`'s video file is
also named `CAP-049-recordings.mp4` (plural) unlike every other capture's singular
`CAP-NNN-recording.mp4` — fix this naming inconsistency via `git mv` while working on `CAP-049`
(same precedent as `CAP-005-recoding.mp4` → `CAP-005-recording.mp4`, see `CHANGELOG.md`).

Beyond each capture's own stated Test-ID goal, actively look for **anything else** interesting in
the video or the wire log — new opcodes, unexpected reconnects, timing anomalies, anything that
doesn't parse, anything that contradicts an existing `PROTOCOL.md`/`DECISIONS.md` claim. That is
an explicit goal of this task, not a nice-to-have.

## Session bookkeeping & resumability

In Phase 0, create `ai-sessions/0010_CAPTURE_RESULT_2026_09_12.md` with the header block required
by `AI_SESSION_LOG_PROCEDURE.md` §4 (`Status: partial — resumed` to start) and a status table with
one row per phase (0–9). After finishing each phase, update that row to `done` with a one- or
two-line summary of what was found/changed, and re-save the file — this is the checkpoint a
resumed session reads to know where to continue. Do **not** create a new numbered pair for a
resumption — per §5, the same `0010_CAPTURE_RESULT_2026_09_12.md` is progressively appended to.

This prompt does not instruct automatic git commits (project-wide convention: never commit without
being explicitly asked). Since all work happens in the same working directory, on-disk progress
survives a session break on its own; mention clearly, at the end of each session's final turn,
what is uncommitted so the maintainer can decide whether to commit before the next session starts.

## Shared per-capture methodology (apply this to each of Phases 1–8)

**Step A — Orient.** Re-read this capture's skeleton `CAP-NNN-EVENT-NOTES.md`, its
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group section and §9 row, its `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
Test-ID row(s), and any precondition-source `CAP-NNN-FINDINGS.md` named in this phase's own
section below.

**Step B — Inventory the folder's files** (`ls` the capture directory) and confirm the actual
filenames match what's expected; fix any naming inconsistency via `git mv` (e.g. `CAP-049`'s
`CAP-049-recordings.mp4` → `CAP-049-recording.mp4`), noting the fix explicitly in the result file.

**Step C — Get exact video duration/metadata:** `ffprobe` the recording. Recordings in this
project carry a burned-in wall-clock overlay (per `README.md`) — that overlay is your primary
time source for the Event Timeline, not the file's own internal timestamp.

**Step D — Watch the entire video, not a sample.** Using `ffmpeg -ss <t> -frames:v 1` (this
project's established method, see `DESKRESEARCH_FINDINGS.md`), extract frames across the video's
**full** duration at an interval dense enough that no on-screen action or state change could be
missed between two consecutive extracted frames (a rough guide: every 1–2s for shorter/busier
sessions, tightened to sub-second spacing around any moment where the draft `CAP-NNN-EVENT-NOTES.md`,
the log, or a previous frame suggests something is about to happen; loosened only across long,
provably static stretches you've already confirmed are static by checking frames on both ends).
Save frames to your scratchpad directory. Read each one and build an independent, complete,
time-stamped action list from what's actually on screen — then reconcile it against the existing
draft timeline, correcting order and timing rather than trusting the draft.

**Step E — Rewrite the Event Timeline and Log Metadata.** Update `CAP-NNN-EVENT-NOTES.md`'s Event
Timeline table to the verified real sequence (add missed events, fix ordering/timestamps, remove
anything the video disproves). Fill in Log Metadata: Date `2026-09-12`, Test device `Pixel 7a`,
Android `17`, Firmware `release_5.203`, App version `1.0.955078536`, and note Google Play services
active. Determine this video's exact start and end wall-clock time from the burned-in overlay.

**Step F — Rename the capture folder.** Use `git mv` to rename
`captures/CAP-NNN-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_X` to
`captures/CAP-NNN-2026-09-12_HH-MM-SS_HH-MM-SS-Group_X` using the start/end times from Step E
(keep the `-Group_X` suffix unchanged). Update the header line and any internal path references
inside `CAP-NNN-EVENT-NOTES.md` to match the new folder name.

**Step G — Resolve `.log`/`.log.last` (CAP-048, CAP-049 only).** Before touching either file,
determine — using the same kind of evidence `CAP-040-FINDINGS.md`/`CAP-042-FINDINGS.md` used —
whether `.log.last` is genuine same-session rotation (the on-device size cap was hit mid-session)
or leftover buffer content from a prior/idle period before this session's own start time (from
Step E). Concretely: compare `.log.last`'s own Connection Complete / timestamp content against (a)
this session's own confirmed start time and (b) the neighboring earlier capture's own log content
— if `.log.last`'s frames predate this session's start or duplicate the previous capture's own
events, it is leftover, not a rotation. Document the determination explicitly (mirroring
`CAP-040`/`CAP-042`'s "Integrity correction" language) either way — do not silently assume.
If, and only if, the evidence supports genuine same-session rotation, produce a new, separate
combined file (e.g. `CAP-NNN-btsnoop_hci-combined.log`) rather than overwriting either original —
**a raw `cat` will corrupt the result**, since the btsnoop file format has a single global file
header (magic string + version + datalink type) followed by packet records; a valid concatenation
must keep the first file's header and append only the second file's packet records (strip its
header). State the exact method/command used. If the evidence says "leftover," treat the main
`.log` as this session's sole evidence, same as `CAP-040`/`CAP-042` did, and say so plainly.

**Step H — Full, non-sampled log analysis.** Pre-filter by the Buds' own MAC address first (per
`AGENTS.md` §13's CLI-hygiene rule), then extract and review the **complete** relevant traffic —
RFCOMM DLCI 0x02/0x04/0x08, GATT, HFP AT commands — across the entire log (not a sample window).
Correlate every event in the corrected Event Timeline to a specific frame number. Separately,
inventory anything unexplained or unclassified even outside this capture's own stated Test-ID
scope (per this prompt's Context above and `AGENTS.md` §6's `UnidentifiedFrame` philosophy).

**Step I — Cross-checks and consistency checks.** Check internal consistency (video vs. log vs.
corrected Event Timeline) and correlate against `PROTOCOL.md`'s existing entries, `DECISIONS.md`
ADRs (especially ADR-024 for `CAP-048`/`CAP-049`), and the specific prior `CAP-NNN-FINDINGS.md`
files named in this phase's own section below. Flag, don't silently resolve, any contradiction
with an existing FACT-level claim.

**Step J — External validation.** For any wire finding that maps to a known open standard (Fast
Pair, HFP AT commands, Pigweed `pw_hdlc`, GATT Battery Service, AVRCP, etc.), verify against
publicly available specification documentation (WebSearch/WebFetch) and cite the source, following
`DESKRESEARCH_FINDINGS.md`'s own established pattern for this. Label every conclusion
FACT/HYPOTHESIS/ASSUMPTION/OPEN QUESTION per `PROJECT_RULES.md` rule 1 — an agreeing external
source raises confidence toward HYPOTHESIS, it does not itself authorize a 🟢 FACT promotion (see
Guardrails).

**Step K — Write `CAP-NNN-FINDINGS.md`.** Follow the structure already established by the most
recent comparable findings files (e.g. `CAP-042-FINDINGS.md`, `CAP-040-FINDINGS.md`): scope/goal,
what was verified against the video, wire evidence per finding (hex + command, per rule 4a),
explicit answer to this capture's own stated Test-ID question(s), any bonus/incidental findings,
open items. Apply the hex-and-script rule to every decoded burst.

**Step L — Update the cross-reference documents.**
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9: flip this row's Status from `planned` to `analyzed`, fill
  in Android/Firmware/App version, the (now-renamed) log path(s), and append a short outcome note
  in the same terse style as the `CAP-038`/`CAP-040`/`CAP-042` rows.
- `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`: update this capture's Test-ID row(s)' Evidence column with a
  pointer to the new `CAP-NNN-FINDINGS.md` section.
- Update `0010_CAPTURE_RESULT_2026_09_12.md`'s status table for this phase (see "Session
  bookkeeping" above) before moving on.

## Phase 0 — Setup

Complete the Mandatory reading order above. Create `ai-sessions/0010_CAPTURE_RESULT_2026_09_12.md`
with the required header (`Status: partial — resumed`) and an empty phase-status table (Phases
0–9, all `not started` except Phase 0 itself). Confirm via `ls` the actual current contents of all
8 capture folders (already done once while drafting this prompt — re-confirm nothing changed).

## Phase 1 — `CAP-018` (Group Y, `GATT-002`)

Purpose (§9): isolate whether `CAP-016-FINDINGS.md` §11's `0x0044` notification burst is triggered
by BLE link establishment alone, with the buds/case completely untouched. Note the skeleton's
"Isolation check" callout — the video/log review must explicitly confirm nothing touched the
buds/case at any point, since that's the entire point of the session. Apply the shared methodology
(Steps A–L; skip G, not applicable — no `.log.last` here). Precondition source: `CAP-016-FINDINGS.md`
§11.

## Phase 2 — `CAP-026` (Group L, `BATT-001`, `OBS-001`)

Purpose (§9): main run-through — passive observation windows (idle-wait-for-reconnect-notification,
force-close/reopen the app). Apply Steps A–F, H–L (no `.log.last`). Pay particular attention during
Step D/H to reconnect-notification timing and to the app force-close/reopen boundary, since these
are exactly the kind of easy-to-mis-time events the maintainer's caveat above warns about.

## Phase 3 — `CAP-028` (Group O, `HEAD-002`, `HEAD-003`)

Purpose (§9): physical head-gesture actions; requires Head gestures already enabled (`CAP-020`,
Group F). Apply Steps A–F, H–L (no `.log.last`). During Step A/I, confirm from the video whether
Head gestures were already on at session start or were turned on as this session's own first step
— `CAP-020-FINDINGS.md` is the precondition source either way.

## Phase 4 — `CAP-029` (Group P, `CONV-002`, `CASE-007` optional/destructive, `CASE-008`, `PAIR-002` if `CASE-007` run)

Purpose (§9): Conversation Detection voice trigger; optionally a factory reset (`CASE-007`, resets
the Find My Device link) and the shorter-press pairing-mode question (`CASE-008`). Apply Steps
A–F, H–L (no `.log.last`). During Step A, determine from the video/EVENT-NOTES whether the
factory reset was actually performed this session (the skeleton left this an open "TBD" decision)
— if it was, `PAIR-002` evidence is in scope too and the post-reset re-pairing sequence needs the
same full-video/full-log treatment as everything else. Precondition source: `CAP-019-FINDINGS.md`
(Conversation Detection enable).

## Phase 5 — `CAP-045` (Group AJ, `HOLD-005`)

Purpose (§4 Group AJ / §9): isolate whether Left-earbud vs. Right-earbud ANC-rotation-checklist
writes (4 checklist items each, toggled one at a time with a video-visible pause between each) are
wire-distinguishable. Apply Steps A–F, H–L (no `.log.last`). Step D is unusually important here —
each of the 8 individual toggles (4 per earbud) must be pinned to its own frame(s) via tight
video/log timing correlation, since that 1:1 correlation *is* the analysis this Group exists to
produce (per §4 Group AJ's own "Analysis" note).

## Phase 6 — `CAP-046` (Group AK, `AUDIO-003`)

Purpose (§4 Group AK / §9): resolve Volume balance (`qhr` field 17) numeric scale and Left/Right
sign, using isolated discrete extreme-position samples (not a continuous drag) with tight video
correlation to the on-screen slider position/label. Apply Steps A–F, H–L (no `.log.last`). Step H
must zigzag-decode (`(n>>1) ^ -(n&1)`) every isolated sample's field-17 value and match it against
the video-confirmed slider position at that exact moment, per §4 Group AK's own "Analysis" note.

## Phase 7 — `CAP-048` (Group AD repeat, `OBS-004`)

Purpose (§9): repeat of `CAP-037`'s dock-state anomaly with an open ACL — 20+ minutes of isolated
reconnects alternating docked/undocked, this time with continuous physical dock-state video for
sub-second correlation. Apply the **full** shared methodology including Step G (`.log`/`.log.last`
resolution) and the `CAP-049-recordings.mp4` naming note does not apply here, but re-run Step B
anyway. Cross-check every dock-state transition against ADR-024's trigger-independence conclusion
and against `CAP-037-FINDINGS.md`'s own open questions — this session exists specifically to close
those, so an inconclusive result here should be explicitly labeled as such (`PROJECT_RULES.md`
rule 12: a failed/inconclusive test is still recorded).

## Phase 8 — `CAP-049` (Group AF repeat, `OBS-006`)

Purpose (§9): repeat of `CAP-039`'s unexplained disconnect/reconnect cycling (5 cycles, no clear
camera-visible trigger), this time with continuous phone-screen recording throughout to catch any
background system event. Apply the full shared methodology including Step B's filename fix
(`CAP-049-recordings.mp4` → `CAP-049-recording.mp4` via `git mv`) and Step G
(`.log`/`.log.last` resolution). Since this session adds phone-screen recording specifically to
catch a background trigger `CAP-039` couldn't see, Step D's frame-by-frame review is the whole
point — look hard at notification-shade activity, OS Bluetooth-settings state, and any other
system UI visible at each reconnect boundary, not just the Buds app.

## Phase 9 — Cross-capture synthesis and wrap-up

- Re-read all 8 newly-written `CAP-NNN-FINDINGS.md` files together. Check for agreement/conflict
  between them and with the sessions they build on (e.g., does `CAP-048`'s repeat agree with
  `CAP-037`'s original dock-state finding and with ADR-024? Does `CAP-049`'s repeat corroborate or
  contradict `CAP-039`'s cycling observation?).
- Run `python3 scripts/lint_docs.py`; fix any newly introduced unregistered-ID or
  dead-filename-reference finding (folder renames from Step F are the most likely source of a
  stale reference — grep for the old placeholder folder names across the repo before finishing).
- Run `./scripts/ensure_footers.py` if any new or renamed `.md` file is missing its footer.
- Update `ai-sessions/INDEX.md`'s row for `0010` to match this session's final `Status`.
- Finalize `0010_CAPTURE_RESULT_2026_09_12.md`: `Status: complete` only if every phase's work is
  actually done and no proposed FACT-promotion/ADR is still pending maintainer review; otherwise
  `Status: awaiting maintainer sign-off` (if the only thing left is maintainer review of proposed
  promotions) or `partial — resumed` (if genuine work remains).
- Write a final summary for the maintainer: what changed per capture, any newly discovered
  anomalies, and an explicit list of every proposed 🟢 FACT promotion or `DECISIONS.md` ADR
  (clearly labeled as proposals, per Guardrails) awaiting sign-off.

## Guardrails

- **Never independently promote a finding to 🟢 FACT in `PROTOCOL.md`, and never write or alter a
  `DECISIONS.md` ADR, without explicit maintainer approval** (`AGENTS.md` §6/§15). Propose and
  label clearly as a proposal in the relevant `CAP-NNN-FINDINGS.md` and in the Phase 9 summary
  instead — this applies even where an external source (Step J) agrees with your reading.
- Never invent a timestamp, opcode meaning, or value not actually observed in the video/log — an
  unrecoverable byte's meaning is an explicit `PROTOCOL.md` §6-style open question, not a filled-in
  guess (`PROJECT_RULES.md` rule 1, `AGENTS.md` §13's "zero creativity" instruction for hex
  parsing).
- Apply the hex-and-script rule (`PROJECT_RULES.md` rule 4a) to every decoded burst: the exact
  command **and** the raw hex bytes, not just the interpretation.
- Never fabricate/interpolate a battery percentage (`AGENTS.md` §5); never log the Buds' MAC
  address at INFO level or above, and never dump raw payload bytes outside this project's existing
  debug-mode gating conventions (`AGENTS.md` §9) in anything you write.
- Use `git mv` for every folder/file rename in this task (Steps F, B) — never a plain `mv` — so
  git history (and Git LFS-tracked binaries, `PROJECT_RULES.md` rule 18) survive the rename.
- No sampling: every video is watched in full (Step D), every log analyzed in full (Step H) — a
  partial pass is not an acceptable substitute anywhere in this task, per the maintainer's explicit
  instruction.
- Do not decide `CAP-029`'s factory-reset question after the fact — it already happened or didn't;
  Phase 4's job is to determine and document which, not to second-guess it.
- This is a pure documentation/analysis pass on already-executed capture sessions — no new
  Bluetooth action against the hardware is performed as part of this prompt.

## Output

At the end of each phase, a short note (in the RESULT file, and to the maintainer if the chat is
still live) of what was found/changed for that capture. At the end of Phase 9, the full summary
described there. Every substantive claim must cite its evidence (frame number, video timestamp, or
file+line) per `PROJECT_RULES.md` rule 3.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0010_CAPTURE_PROMPT_2026_09_12.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0010_CAPTURE_PROMPT_2026_09_12
