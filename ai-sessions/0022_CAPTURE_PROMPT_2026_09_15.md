# 0022_CAPTURE_PROMPT_2026_09_15.md — Full log analysis and FINDINGS for CAP-047 (Group AL, DLCI 0x0a burst trigger hypothesis test)

**Number:** 0022
**Category:** CAPTURE
**Date:** 2026-09-15
**Title:** Full, non-sampled `.log`/`.log.last`/`-2.log` analysis, cross-validation, and `CAP-047-FINDINGS.md` authoring for `CAP-047` (Group AL, `CAP-021`'s DLCI 0x0a burst trigger hypothesis test, Trigger candidate 3 only) — including a targeted video re-check of a maintainer-recalled corrected docking in Recording 2 that the existing `CAP-047-EVENT-NOTES.md` currently says never happened

---

## How to (re)start this prompt — read this paragraph first, every time

This task is designed to be pasted verbatim into a **new Claude Code chat**, including on a
resumption (three wire-log files plus a targeted video re-check, analyzed completely and without
sampling, may not fit in one pass depending on rate limits/context).

Before anything else: check whether `ai-sessions/0022_CAPTURE_RESULT_2026_09_15.md` already exists.
- **It does not exist** → this is the first run. Start at Phase 0.
- **It already exists** → read it in full. Its per-phase status table (created in Phase 0) says
  which phases are already `done`. Skip straight to the first phase not yet marked `done` and
  continue from there — do not redo a `done` phase's work. Regardless of where you resume, you still
  owe the full "Mandatory reading order" below in this session — `AI_SESSION_LOG_PROCEDURE.md` §8
  requires it for every session that acts on this repo, resumption or not, since a new chat has no
  memory of a previous one.

Every phase boundary is a safe stopping point: before ending a turn (whether because the phase is
done, or because you sense a rate limit / context limit approaching), update
`0022_CAPTURE_RESULT_2026_09_15.md`'s status table and `Status` header field (per
`AI_SESSION_LOG_PROCEDURE.md` §4/§5: `partial — resumed` while incomplete) so the next session picks
up cleanly. Do not leave a phase half-done without a note in the result file describing exactly what
was and wasn't finished.

## Mandatory reading order (do this first, in this order, in every session that works on this prompt)

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8:

1. `AGENTS.md` (full) — especially §13's "Analyzing a Bluetooth capture" workflow (CLI hygiene:
   pre-filter by the Buds' address before layering protocol filters; the traceability check against
   `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`) and §13.6's zero-creativity, evidence-only rule for hex
   parsing.
2. `PROJECT.md` (full).
3. `PROJECT_RULES.md` (full) — especially rule 1/4a (FACT/HYPOTHESIS/ASSUMPTION/OPEN QUESTION
   labeling and the hex-and-script rule: every decode needs the exact command **and** the raw hex
   bytes), rule 3 (evidence traceability — a frame number or video timestamp for every claim), rule
   10–12 (the fixed hypothesis-test template this Group is explicitly built around: hypothesis,
   setup, expected outcome, actual outcome, conclusion; a negative/inconclusive result is still
   recorded), rule 13/13a (AI session behavior and session logging), rule 14 (capture metadata), rule
   19 (no MAC addresses/personal data logged in the clear, with `DECISIONS.md` ADR-010's own-capture
   exception).
4. `DECISIONS.md` — every ADR, ADR-001 through the most recent. Pay particular attention to
   **ADR-024** ("Notify ANC state" `Settable-toggles` byte confirmed as a dock-state indicator,
   `0x00`=both docked / `0xe8`=otherwise) and **ADR-016** (case lid opened/closed while both buds
   remain *outside* the case produces no wire-visible signal on any RFCOMM channel) — both directly
   bear on `CAP-047`'s repeated *swapped-slot* docking attempts (buds physically seated, but in the
   wrong Left/Right slot), which is a novel stress case neither ADR's own evidence base has covered
   before.
5. `ARCHITECTURE.md`.
6. `PROTOCOL.md` (full) — especially:
   - §6's DLCI 0x0a open item (`CAP-021-FINDINGS.md` §4a, refined 2026-08-23) — this capture's whole
     purpose. Read the full characterization already on record: 100% Rcvd-direction, ~5–6 bursty
     waves with multi-second gaps, a repeating `6d b6 db`/`7e ee ed` byte-pattern body, structurally
     protobuf-tag-shaped (`0a d0 01` = field 1, length 208) but not decoded further, observed in
     exactly 1 of 16+ sessions checked (`CAP-021` only), and explicitly ruled out as the SCO/eSCO
     audio path (`CAP-008-FINDINGS.md` §5/§6).
   - The RFCOMM channel-open-order material (~lines 1560–1600) documenting DLCI 0x0a's own place in
     the normal per-session channel-opening sequence — the baseline "channel opens, stays silent"
     behavior this capture's burst would deviate from.
   - The case-lid/no-wire-signal finding around line 2620 (ADR-016's own evidence), relevant to
     checking whether video 1's swapped-slot dock at 06:10:21 (connection apparently did **not** drop
     per the on-screen "connected" label) is consistent with what the wire actually shows.
7. `TODO.md` (full) — the "Recommended priority order" §5 item this Group AL session was purpose-built
   to answer.
8. `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md`.
9. `CAPTURE_BLUETOOTH_HCI_SNOOP.md` in full — specifically:
   - §2's log-rotation note (why a `.log.last` can appear — directly relevant here: this capture's
     folder has a `CAP-047-btsnoop_hci.log.last` file that has never been resolved).
   - The **Group AL** section (`CAP-021`'s DLCI 0x0a burst, three candidate triggers, this session
     tests only Trigger 3 — charge-state change).
   - §9's Capture Index row for `CAP-047` (currently `planned`, TBD columns) and the `CAP-040`/
     `CAP-042`/`CAP-049` rows, which each document the exact `.log.last`-resolution methodology
     (compare its own Connection Complete/timestamp content against the confirmed session start time
     to distinguish "leftover on-device buffer from an earlier session" from "genuine intra-session
     rotation") — apply the same method here rather than assuming either answer.
10. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` — confirm the Group AL purpose statement's own claim that no
    existing Test-ID covers this specific bracketed-trigger question still holds, and check for any
    `BATT-*`/`PAIR-*`/reconnect-family row incidentally exercised by the repeated dock/undock/swap
    cycles in either recording.
11. `DESKRESEARCH_FINDINGS.md` in full — its template, status legend, and existing entries validating
    a wire finding against an external spec. Follow this same pattern for anything in this session
    that maps to a public standard (e.g. protobuf varint/tag-byte structure for the `0a d0 01`
    tag-shape already noted in `PROTOCOL.md` §6, or Bluetooth RFCOMM channel semantics if relevant to
    explaining the burst's timing).
12. The following existing `CAP-NNN-FINDINGS.md` files, in full — this task builds directly on them:
    - `captures/CAP-021-2026-08-21_07-59-36_08-07-04-Group_G/CAP-021-FINDINGS.md` §4a — the original,
      only-ever-observed occurrence of the DLCI 0x0a burst; the baseline this session's result is
      compared against.
    - `captures/CAP-008-2026-08-26_09-38-44_09-41-36-Group_V/CAP-008-FINDINGS.md` §5/§6 — the prior
      ruled-out hypothesis (SCO/eSCO audio path), so this session doesn't re-test something already
      closed.
    - `captures/CAP-040-2026-09-06_07-28-00_07-57-05-Group_AG/CAP-040-FINDINGS.md`,
      `CAP-042-FINDINGS.md`, and `CAP-049-FINDINGS.md` — the established `.log.last`-resolution
      precedent (§9 above) to mirror exactly for `CAP-047-btsnoop_hci.log.last`.
13. `captures/CAP-047-2026-09-14_05-51-38_06-35-30-Group_AL/CAP-047-EVENT-NOTES.md` in full — this
    task's starting point, already fully video-verified once (`ai-sessions/0018`, frame-by-frame, both
    recordings watched in full). Read it alongside `ai-sessions/0018_CAPTURE_RESULT_2026_09_14.md` to
    understand exactly what that prior pass already confirmed — see Context below for the one specific
    point in it that now needs re-checking.

## Context (from the maintainer, not re-derived here)

The session was executed on 2026-09-14 by the maintainer, on a **Pixel 7a**, **Android 17**, Pixel
Buds Pro 2 firmware **`release_5.203`**, official Pixel Buds Companion app version
**`1.0.955078536`**, with **Google Play services active** on the phone. Treat these five values as
confirmed (not `TBD`, not `⚪ assumed`) when filling in the Log Metadata table and
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 row.

**Folder contents, already confirmed present (`ls -la
captures/CAP-047-2026-09-14_05-51-38_06-35-30-Group_AL/`):**
- `CAP-047-recording.mp4` (00:20:03.45, 05:51:38–06:11:41) and `CAP-047-recording-2.mp4`
  (00:02:29.59, 06:33:01–06:35:30) — both already watched frame-by-frame in `ai-sessions/0018`.
- `CAP-047-btsnoop_hci.log` (extracted ~06:13, i.e. shortly after Recording 1 ends) —
  `CAP-047-btsnoop_hci.log.last` (extracted ~05:51, i.e. at/around Recording 1's own start) —
  `CAP-047-btsnoop_hci-2.log` (extracted ~06:38, i.e. after Recording 2 ends). The `.log.last`
  file's own timestamp makes it plausible it is leftover buffer content predating this session (per
  the `CAP-040`/`CAP-042`/`CAP-049` precedent), but this must be verified from its own content, not
  assumed from the file mtime alone (§9/§12 above).

**Critical: a specific, already-documented conclusion in `CAP-047-EVENT-NOTES.md` now conflicts with
the maintainer's own recollection and must be re-checked, not taken as settled.** The existing
`CAP-047-EVENT-NOTES.md` (video-only pass, `ai-sessions/0018`) states explicitly, at 06:35:30:
*"Recording 2 ends with both buds resting outside the open, empty case. Note: Proper (matching) slot
corrected docking was never performed during either recording."* The maintainer's own account of this
session, given when commissioning this prompt, is different: in Recording 2, the buds were initially
placed in the case the wrong way round (swapped Left/Right slots) — **but immediately after that, the
buds were placed correctly (matching slots) into the case.** The maintainer's account does **not**
claim a corrected docking happened in Recording 1 (Recording 1's own "wrong way round" placement is
not disputed) — the discrepancy is specifically about Recording 2's tail.

Do not assume either account is right. Resolve this with a **targeted, dense
`ffmpeg -ss <t> -frames:v 1`** re-extraction across Recording 2's entire swapped-docking-and-after
sequence — from the first swapped placement (`~06:33:41`) through the recording's end (`~06:35:30`) —
at a higher time density than a normal pass, since the existing timeline already shows multiple bud
movements packed within seconds of each other in this window (e.g. `06:34:41`→`06:34:51`→`06:34:59`)
and a corrected-docking moment could plausibly have been missed at the review density
`ai-sessions/0018` used. This does **not** require re-watching Recording 1 in full, nor the earlier
majority of Recording 2 (`06:33:01`–`06:33:41`) already confirmed uncontested — only the swapped-
docking-and-after tail is in question. If the dense re-check confirms a corrected (matching-slot)
docking did occur, record the exact timestamp and update `CAP-047-EVENT-NOTES.md`'s Event Timeline,
its "Method choice" line, and its Analysis checklist's Trigger 3 note accordingly — this would make a
**genuine charge-state transition** (not just a swapped, possibly-non-charging seating) available as
the log-analysis phase's primary bracket window for the DLCI 0x0a burst check. If the re-check
confirms the existing note was correct (no corrected docking occurred, or it occurred but the
recording had already ended before capturing it — check whether 06:35:30's stated end time genuinely
matches the file's own 149.59s duration first), say so plainly and leave `CAP-047-EVENT-NOTES.md`
as-is for that point — a confirmed absence is an acceptable, reportable outcome, not a failed check.

**Beyond Group AL's own stated Trigger-3 question, actively look for anything else interesting** in
either log — new opcodes, unexpected reconnects, timing anomalies, anything that doesn't parse,
anything that contradicts an existing `PROTOCOL.md`/`DECISIONS.md` claim. This is an explicit part of
this task, not a nice-to-have (the maintainer's own instruction: "Probeer naast de doelstelling van de
CAP zoveel mogelijk andere bevindingen te vinden"). Two specific leads already visible from the
Event Timeline are worth checking on the wire, not just accepting from the on-screen label:
- Video 1's swapped-slot dock at `06:10:21` — the phone screen kept showing "connected" (no
  disconnect), unlike video 2's swapped-slot dock at `06:33:57`, which **did** show a disconnect.
  Confirm on the wire whether this is a genuine behavioral difference (and if so, whether it
  correlates with anything — e.g. one slot's charging contacts registering and the other's not) or
  an on-screen-label artifact that the wire log doesn't actually support.
- Whether ADR-024's `Settable-toggles` dock-state byte (DLCI 0x04) or any DLCI 0x08 dock-state field
  reads "both docked" during a *swapped*-slot seating (buds seated, but not in their assigned slot) —
  this is a case ADR-024's own original evidence base never tested, and a mismatch (or a match) is
  independently reportable regardless of the DLCI 0x0a question.

## Session bookkeeping & resumability

In Phase 0, create `ai-sessions/0022_CAPTURE_RESULT_2026_09_15.md` with the header block required by
`AI_SESSION_LOG_PROCEDURE.md` §4 (`Status: partial — resumed` to start) and a status table with one
row per phase (0–3). After finishing each phase, update that row to `done` with a one- or two-line
summary of what was found/changed, and re-save the file — this is the checkpoint a resumed session
reads to know where to continue. Do **not** create a new numbered pair for a resumption — per §5, the
same `0022_CAPTURE_RESULT_2026_09_15.md` is progressively appended to.

This prompt does not instruct automatic git commits (project-wide convention: never commit without
being explicitly asked). Mention clearly, at the end of each session's final turn, what is
uncommitted so the maintainer can decide whether to commit before the next session starts.

## Phase 0 — Setup

Complete the Mandatory reading order above. Create `ai-sessions/0022_CAPTURE_RESULT_2026_09_15.md`
with the required header (`Status: partial — resumed`) and an empty phase-status table (Phases 0–3,
all `not started` except Phase 0 itself). Run `ls -la` on the capture folder and record the actual
current contents verbatim in the result file (filenames, sizes, mtimes) — this is the evidence base
for the `.log.last` resolution in Phase 1.

## Phase 1 — Video re-check, `.log.last` resolution, and full log analysis

**Step A — Orient.** Re-read `CAP-047-EVENT-NOTES.md`, the Group AL section of
`CAPTURE_BLUETOOTH_HCI_SNOOP.md`, and `CAP-021-FINDINGS.md` §4a/`CAP-008-FINDINGS.md` §5–§6 (the
precondition/baseline for this whole session).

**Step B — Targeted video re-check (Context above).** Dense `ffmpeg -ss <t> -frames:v 1` extraction
across Recording 2's `~06:33:41`–`~06:35:30` window to resolve the corrected-docking discrepancy.
Update `CAP-047-EVENT-NOTES.md`'s Event Timeline, Method-choice line, and Analysis-checklist Trigger-3
note with whatever is actually confirmed (a corrected docking at a specific timestamp, or a confirmed
absence with a stated reason). Do not re-watch either recording in full — both were already fully
reviewed frame-by-frame in `ai-sessions/0018`; this is a targeted re-check of one specific window
only.

**Step C — `.log.last` resolution.** Apply the exact method `CAP-040-FINDINGS.md`/
`CAP-042-FINDINGS.md`/`CAP-049-FINDINGS.md` already established: compare `CAP-047-btsnoop_hci.log.last`'s
own Connection Complete events and internal timestamps against the confirmed session start time
(05:51:38, Recording 1's own start) to determine whether it is leftover on-device buffer content from
before this session (most likely, given its ~05:51 extraction time predates or coincides with
Recording 1's start) or genuine intra-session rotation. Do not assume either answer from the mtime
alone — verify from the log's own content. Record the conclusion explicitly, and state clearly which
log file(s) are actually in-scope evidence for this session as a result.

**Step D — Full, non-sampled log analysis.** Pre-filter by the Buds' own MAC address first (per
`AGENTS.md` §13's CLI-hygiene rule: `tshark -r CAP-047-btsnoop_hci.log -Y "bluetooth.addr ==
04:00:6e:cf:6e:07"` — full MAC from the capture's own metadata, before layering DLCI-specific
filters), then extract and review the **complete** relevant traffic across `CAP-047-btsnoop_hci.log`
(Recording 1's window) and `CAP-047-btsnoop_hci-2.log` (Recording 2's window) — and `.log.last` too,
if Step C finds it is genuinely in-scope. This is not a sample window or a subset of dock/undock
cycles — the maintainer's explicit instruction is "Doe geen steekproeven, maar doe analyses volledig."
Specifically:
- Search DLCI 0x0a (RFCOMM channel 5) across the **entire** duration of every in-scope log for any
  payload burst (any size, not just the 1123-frame `CAP-021` sample) beyond the normal
  channel-open/channel-control frames. Check in particular the windows immediately around each
  dock/undock/swap event in the (now-corrected) Event Timeline, including the corrected-docking moment
  from Step B if one was confirmed.
- Correlate every event in the corrected Event Timeline to a specific frame number, in both logs.
- Investigate the two bonus leads flagged in Context above (the video-1-vs-video-2 disconnect-behavior
  difference on swapped docking; ADR-024's dock-state byte's behavior during a swapped-slot seating)
  on the wire.
- Separately, inventory anything else unexplained or unclassified even outside DLCI 0x0a (per
  `AGENTS.md` §6's `UnidentifiedFrame` philosophy) — this is an explicit goal of this session, not
  just the Group AL question.

**Step E — Cross-checks and consistency checks.** Check internal consistency (video vs. log vs.
corrected Event Timeline) and correlate against `PROTOCOL.md` §6's DLCI 0x0a entry, ADR-016/ADR-024,
`CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, and the specific prior
`CAP-NNN-FINDINGS.md` files named in the reading order above. Flag, don't silently resolve, any
contradiction with an existing FACT-level claim.

**Step F — External validation.** For the already-noted protobuf tag-shape (`0a d0 01` = field 1,
length 208) and for anything else in this session that maps to a known open standard or public
mechanism (Bluetooth RFCOMM/SDP specifics, protobuf varint/tag encoding, or anything else that turns
out relevant), verify against publicly available documentation (WebSearch/WebFetch) and cite the
source, following `DESKRESEARCH_FINDINGS.md`'s own established pattern. Label every conclusion
FACT/HYPOTHESIS/ASSUMPTION/OPEN QUESTION per `PROJECT_RULES.md` rule 1 — an agreeing external source
raises confidence toward HYPOTHESIS, it does not itself authorize a 🟢 FACT promotion (see
Guardrails).

**Step G — Write `CAP-047-FINDINGS.md`.** Follow `PROJECT_RULES.md` rule 10's fixed hypothesis-test
template (hypothesis, setup, expected outcome, actual outcome, conclusion) explicitly for Trigger
candidate 3 (charge-state change) — the only trigger this session tested; state plainly that Triggers
1 and 2 (app backgrounded/foregrounded; scheduled sync window) remain untested and are open follow-up
work, not silently dropped. Apply the hex-and-script rule (`PROJECT_RULES.md` rule 4a) to every
decoded burst: exact command **and** raw hex bytes. Include the `.log.last` resolution (Step C), the
bonus findings (Step D), and an explicit answer to whether this session's result is consistent with
or contradicts `CAP-021-FINDINGS.md` §4a's characterization of the burst. A negative result (burst did
not appear at/after the charge-state transition(s) tested) is itself a valuable, reportable outcome
per Group AL's own purpose statement — do not treat it as an inconclusive or failed session.

**Step H — Update the cross-reference documents.**
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9: flip the `CAP-047` row's Status from `planned` to `analyzed`,
  fill in the Android/Firmware/App-version columns (values confirmed in Context above), the log
  path(s), and append a short outcome note in the same terse style as the `CAP-040`/`CAP-042`/
  `CAP-049` rows (including the `.log.last` resolution, mirroring how those rows phrase it).
- `PROTOCOL.md` §6: add a dated update to the existing DLCI 0x0a open item recording this session's
  result (negative/positive, and any new characterization) — do **not** close or promote the item
  without maintainer sign-off; append as a dated note, per existing precedent in that section.
- `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`: if this session's result still leaves no existing Test-ID
  covering this bracketed-trigger question (per Group AL's own purpose statement), propose — do not
  unilaterally add — a new Test-ID, clearly labeled as a proposal awaiting maintainer sign-off.
- `id_registry.csv`: update the `CAP-047` row's status field from `planned` to reflect the actual
  outcome (mirroring how other `analyzed` captures are recorded there).
- Update `0022_CAPTURE_RESULT_2026_09_15.md`'s status table for this phase before moving on.

## Phase 2 — Wrap-up and maintainer summary

- Re-read `CAP-047-FINDINGS.md` once complete; confirm every substantive claim cites a frame number,
  video timestamp, or file+line (`PROJECT_RULES.md` rule 3).
- Confirm Group AL's own Test-ID traceability: since Group AL has no existing Test-ID assigned
  (flagged as a follow-up, not silently assigned one — see `CAP-047-EVENT-NOTES.md`'s own Purpose
  section), confirm this session's write-up doesn't imply one exists that doesn't (`AGENTS.md` §13
  item 7's traceability-gap discipline, applied in spirit even without a formal Test-ID here).
- Run `python3 scripts/lint_docs.py`; fix any newly introduced unregistered-ID or dead-filename-
  reference finding.
- Run `./scripts/ensure_footers.py` if any new or edited `.md` file is missing its footer.
- Update `ai-sessions/INDEX.md`'s row for `0022` to match this session's final `Status`.
- Finalize `0022_CAPTURE_RESULT_2026_09_15.md`: `Status: complete` only if every phase's work is
  actually done and no proposed FACT-promotion/ADR/new-Test-ID is still pending maintainer review;
  otherwise `Status: awaiting maintainer sign-off` (if the only thing left is maintainer review of
  proposed promotions/proposals) or `partial — resumed` (if genuine work remains).
- Write a final summary for the maintainer: whether the recalled corrected-docking event in Recording
  2 was confirmed (and at what timestamp, or why it was not), whether the DLCI 0x0a burst appeared at
  or after any dock/undock/charge-state transition tested in this session, the `.log.last` resolution,
  any newly discovered anomalies (including the two bonus leads from Context), and an explicit list of
  every proposed 🟢 FACT promotion, `DECISIONS.md` ADR, or new `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
  Test-ID (clearly labeled as proposals, per Guardrails) awaiting sign-off.

## Guardrails

- **Never independently promote a finding to 🟢 FACT in `PROTOCOL.md`, and never write or alter a
  `DECISIONS.md` ADR, without explicit maintainer approval** (`AGENTS.md` §6/§15). Propose and label
  clearly as a proposal in `CAP-047-FINDINGS.md` and in the Phase 2 summary instead — this applies
  even where an external source (Step F) agrees with your reading.
- Never invent a timestamp, opcode meaning, dock state, or value not actually observed in the
  video/log — an unrecoverable byte's meaning, or an ambiguous moment the log genuinely cannot
  resolve, is an explicit open question, not a filled-in guess (`PROJECT_RULES.md` rule 1; `AGENTS.md`
  §13.6's zero-creativity instruction).
- Apply the hex-and-script rule (`PROJECT_RULES.md` rule 4a) to every decoded burst: the exact command
  **and** the raw hex bytes, not just the interpretation.
- Never log the Buds' full MAC address at INFO level or above in anything committed outside this
  capture's own already-consented evidence (`DECISIONS.md` ADR-010's own-capture exception covers the
  existing `CAP-047-EVENT-NOTES.md`/`CAP-047-FINDINGS.md` files themselves, per `PROJECT_RULES.md`
  rule 19 — this does not extend to any other document you touch in this session).
- Use `git mv` for any file rename in this task — never a plain `mv` — so git history (and Git
  LFS-tracked binaries, `PROJECT_RULES.md` rule 18) survive the rename. No rename is currently
  expected (the folder is already correctly named from the prior video-only pass); only act if Phase 0
  actually finds a naming inconsistency.
- No sampling: the targeted video re-check (Step B) covers the disputed window densely, and every log
  analyzed in Step D is analyzed in full across its entire duration — a partial pass is not an
  acceptable substitute anywhere in this task, per the maintainer's explicit instruction ("Doe geen
  steekproeven, maar doe analyses volledig").
- Do not assume the recalled corrected-docking event happened, or happened at a particular moment,
  without video evidence — a confirmed absence is an acceptable, reportable outcome (see Context
  above).
- This is a pure documentation/analysis pass on an already-executed capture session — no new
  Bluetooth action against the hardware is performed as part of this prompt.

## Output

At the end of Phase 1, a short note (in the RESULT file, and to the maintainer if the chat is still
live) of what was found/changed. At the end of Phase 2, the full summary described there. Every
substantive claim in `CAP-047-FINDINGS.md` must cite its evidence (frame number, video timestamp, or
file+line) per `PROJECT_RULES.md` rule 3.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0022_CAPTURE_PROMPT_2026_09_15.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0022_CAPTURE_PROMPT_2026_09_15
