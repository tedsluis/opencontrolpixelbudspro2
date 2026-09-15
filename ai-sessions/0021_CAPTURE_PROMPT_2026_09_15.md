# 0021_CAPTURE_PROMPT_2026_09_15.md — Full log analysis and FINDINGS for CAP-050 (Group AG repeat, PRIV-001) and CAP-051 (Group AM, qhr field 13 ANC-parallel-path)

**Number:** 0021
**Category:** CAPTURE
**Date:** 2026-09-15
**Title:** Full, non-sampled `.log` analysis, cross-validation, and `CAP-NNN-FINDINGS.md` authoring for CAP-050 (Group AG repeat, `PRIV-001`) and CAP-051 (Group AM, `qhr` field 13 ANC-parallel-path, `TOUCH-007`/`ANC`-family)

---

## How to (re)start this prompt — read this paragraph first, every time

This task is designed to be pasted verbatim into a **new Gemini CLI chat**, including on a
resumption (two full-length recordings plus two wire logs across both captures, analyzed completely
and without sampling, may not fit in one pass depending on rate limits/context).

Before anything else: check whether `ai-sessions/0021_CAPTURE_RESULT_2026_09_15.md` already exists.
- **It does not exist** → this is the first run. Start at Phase 0.
- **It already exists** → read it in full. Its per-phase status table (created in Phase 0) says
  which phases are already `done`. Skip straight to the first phase not yet marked `done` and
  continue from there — do not redo a `done` phase's work. Regardless of where you resume, you
  still owe the full "Mandatory reading order" below in this session — `AI_SESSION_LOG_PROCEDURE.md`
  §8 requires it for every session that acts on this repo, resumption or not, since a new chat has
  no memory of a previous one.

Every phase boundary is a safe stopping point: before ending a turn (whether because the phase is
done, or because you sense a rate limit / context limit approaching), update
`0021_CAPTURE_RESULT_2026_09_15.md`'s status table and `Status` header field (per
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
   11 (hypothesis-test reproducibility metadata), rule 13/13a (AI session behavior and session
   logging), rule 14 (capture metadata), rule 19 (no MAC addresses/personal data logged in the
   clear).
4. `DECISIONS.md` — every ADR, ADR-001 through the most recent. Pay particular attention to
   **ADR-022** ("Get ANC state" trigger-reliability, DLCI 0x04) and **ADR-024** ("Notify ANC state"
   `Settable-toggles` byte as dock-state indicator, `0x00`=both docked / `0xe8`=otherwise) — both
   directly bear on CAP-050's dock/undock reconnect cycles; also **ADR-018** (DLCI 0x02 confirmed as
   the app's own internal RFCOMM channel; `field5{field4{...}}` write-envelope shape) and **ADR-019**
   (`qhr`'s oneof structure inside that envelope), both directly relevant to CAP-051's `field13`
   correlation question.
5. `ARCHITECTURE.md`.
6. `PROTOCOL.md` (full) — especially §2 (envelope/framing hypotheses per DLCI — CAP-050 concerns
   DLCI 0x08, CAP-051 concerns DLCI 0x02 vs. the already-confirmed DLCI 0x04 ANC path), §4.1 (ANC
   state, DLCI 0x04, the path CAP-051 checks for a DLCI 0x02 parallel write), §4.2/§4.5 (settings
   write shapes, relevant to decoding any DLCI 0x08 field in CAP-050), and §6 (existing open
   questions — check whether either capture's findings close one).
7. `TODO.md` (full).
8. `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md`.
9. `CAPTURE_BLUETOOTH_HCI_SNOOP.md` in full — specifically:
   - §2/§6's log-rotation note (why a `.log.last` can appear alongside `.log` — relevant if Phase 1's
     file inventory turns up one for CAP-050; see Context below).
   - The **Group AG** section (`PRIV-001`, DLCI 0x08's 7 unmapped zero-length
     `[Group][Code][00 00]`-shaped codes: `05 0c`, `04 02`, `04 04`, `04 11`, `04 13`, `04 15`,
     `0e 04` — correlate each against a known-changing value, per `AGENTS.md` §13.6).
   - The **Group AM** section (`qhr` field 13 ANC-parallel-path: check whether a
     `field5{field4{field13=N}}` write appears on DLCI 0x02 at the in-app tap and/or either physical
     press-and-hold gesture).
   - §9's Capture Index rows for `CAP-040` (original Group AG attempt), `CAP-050`, `CAP-051`, and
     the neighboring `CAP-036`–`CAP-042`/`CAP-048`/`CAP-049` rows (DLCI 0x08/dock-state lineage).
10. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` — the catalog rows for `PRIV-001`, `TOUCH-007`, the `ANC-*`
    family, and incidental `PAIR-003`/`BATT-*` rows that may appear in either log.
11. `DESKRESEARCH_FINDINGS.md` in full — its template, status legend, and existing entries that
    validate a wire finding against an external spec. This is the pattern to follow for this
    prompt's own "validate against internet sources" requirement (e.g. Pigweed `pw_hdlc`, Bluetooth
    RFCOMM/SDP, protobuf zigzag-decoding references, or the official Fast Pair spec, whichever is
    actually relevant to what's found).
12. The following existing `CAP-NNN-FINDINGS.md` files, in full — this task builds directly on them:
    - `captures/CAP-040-2026-09-06_07-28-00_07-57-05-Group_AG/CAP-040-FINDINGS.md` — the original
      Group AG attempt CAP-050 repeats; its §1/§3/§5 (in-app Connect/Disconnect buttons produce zero
      wire signal, invalidating that attempt's bracket) is exactly what CAP-050's genuine
      OS-toggle/physical-dock trigger is meant to fix.
    - `captures/CAP-036-2026-09-04_06-35-58_06-41-18-Group_AC/CAP-036-FINDINGS.md` §5 (where the 7
      unmapped codes were first found).
    - `CAP-037-FINDINGS.md`, `CAP-038-FINDINGS.md`, `CAP-039-FINDINGS.md`, `CAP-041-FINDINGS.md`,
      `CAP-042-FINDINGS.md`, `CAP-048-FINDINGS.md`, `CAP-049-FINDINGS.md` — the fuller DLCI
      0x08/dock-state/reconnect lineage CAP-050 sits in (ADR-022/ADR-024's own evidence base);
      CAP-050's own reconnect-heavy session should be cross-checked against all of these for
      consistency, not just CAP-040.
    - `CAP-006-FINDINGS.md` (the DLCI-0x04 ANC-confirmation methodology CAP-051's own Analysis
      checklist explicitly says to mirror) and `captures/CAP-038-2026-09-06_06-49-50_06-53-57-Group_AE/CAP-038-FINDINGS.md`
      §5 (the two DLCI-0x04 ANC Notify frames with no preceding Get/Set — the otherwise-unexplained
      pattern CAP-051 exists to try to correlate against a DLCI 0x02 write).
    - `REVERSE_ENGINEERING.md`'s `qhr` entry (field 13's two code-side callers:
      `QuickActionsFragment`'s in-app toggle tap, and the physical press-and-hold gesture via
      `gvi`/`gvj`).
13. Both existing, already video-verified `CAP-NNN-EVENT-NOTES.md` files, in full — this task's
    starting point:
    - `captures/CAP-050-2026-09-14_21-01-01_21-13-30-Group_AG/CAP-050-EVENT-NOTES.md` (video-only
      pass, `ai-sessions/0019`) — confirmed one video and one log (see Context below for a physical
      mis-docking event within this single recording worth checking for).
    - `captures/CAP-051-2026-09-14_21-42-55_21-44-09-Group_AM/CAP-051-EVENT-NOTES.md` (video-only
      pass, `ai-sessions/0020`).

## Context (from the maintainer, not re-derived here)

Both sessions were executed on 2026-09-14 by the maintainer, on a **Pixel 7a**, **Android 17**,
Pixel Buds Pro 2 firmware **`release_5.203`**, official Pixel Buds Companion app version
**`1.0.955078536`**, with **Google Play services active** on the phone. Treat these five values as
confirmed (not `TBD`, not `⚪ assumed`) when filling in every Log Metadata table and
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 row in this prompt's scope.

**CAP-050 — confirmed single video and single log; check for a physical mis-docking event within
it, do not assume it occurred at any specific moment.** The maintainer has confirmed CAP-050 consists
of exactly **one** recording (`CAP-050-recording.mp4`) and **one** wire log
(`CAP-050-btsnoop_hci.log`) — matching what `ls -la` on
`captures/CAP-050-2026-09-14_21-01-01_21-13-30-Group_AG/` already shows, and what the existing
`CAP-050-EVENT-NOTES.md` (`ai-sessions/0019`, video-only pass) already documents. There is **no**
second recording or second log for this capture (unlike `CAP-047`, which genuinely does have a
`-recording-2.mp4`/`-btsnoop_hci-2.log` pair — do not import that pattern here).

Separately, the maintainer recalls that at some point during this session's extensive dock/undock
handling, one or both earbuds were placed the wrong way round in the case (and, per the maintainer's
recollection, self-corrected shortly after) — a physical mis-docking event within this single
recording, not a separate recording attempt. `CAP-050-EVENT-NOTES.md` does not currently document any
such event. Verify from evidence whether and when this happened — do not assume a timestamp or
assume it happened at all just because it's recalled; a targeted re-check of the video (dense
`ffmpeg -ss <t> -frames:v 1` extraction, per the maintainer's instruction to (re)watch video only as
much as needed to confirm what's already recorded, or correct/supplement it, is accurate) around the
case-handling moments should be enough, rather than a full frame-by-frame re-watch of the whole
748.8s video (already done once in `ai-sessions/0019`). This is worth checking specifically because
it may explain one or more of the already-flagged ambiguous/lower-confidence dock-state reads in
`CAP-050-EVENT-NOTES.md`'s Event Timeline (`~21:02:37–39`, `~21:02:58–00`) or something inside the
large unattributed `~00:04:00`–`00:11:41` window — a wrong-orientation docking could plausibly fail
to register a normal dock event, which is directly relevant to `PRIV-001`'s "known-changing value"
bracket and ADR-024's dock-state reading. If the video/log evidence does not clearly show such an
event (or shows it at a moment that doesn't explain anything already flagged), say so plainly rather
than forcing a connection — a confirmed absence is also a reportable result here, not a failed
check. Update `CAP-050-EVENT-NOTES.md`'s Event Timeline only with what is actually verified.

**CAP-051 — already fully video-verified, no discrepancy, spot-check only if needed:** the
maintainer separately reports the video shows the ANC mode changing on the Pixel 7a's screen and the
right earbud being physically press-and-held for a long press twice ("2x lang ingedrukt"). This
matches what `ai-sessions/0020` already investigated and corrected: the existing
`CAP-051-EVENT-NOTES.md` (full, non-sampled video review already completed) found **three** physical
press-and-hold gestures, not two, plus one in-app tap — a discrepancy already resolved by that prior
session, not something this session needs to re-derive from scratch. Do **not** re-watch
`CAP-051-recording.mp4` in full again — only re-check specific frames if something encountered during
the log analysis (Phase 2) makes a specific timestamp's on-screen state worth spot-confirming (per
the maintainer's own instruction: watch the video only as much as needed to confirm actions/events
were captured correctly, and correct `CAP-051-EVENT-NOTES.md` only if such a spot-check finds an
actual error).

**Beyond each capture's own stated Test-ID goal, actively look for anything else interesting** in
either log — new opcodes, unexpected reconnects, timing anomalies, anything that doesn't parse,
anything that contradicts an existing `PROTOCOL.md`/`DECISIONS.md` claim. This is an explicit goal of
this task, not a nice-to-have, per the maintainer's own instruction ("Probeer naast de doelstellingen
van de beide CAP's zoveel mogelijk andere bevindingen te vinden").

## Session bookkeeping & resumability

In Phase 0, create `ai-sessions/0021_CAPTURE_RESULT_2026_09_15.md` with the header block required by
`AI_SESSION_LOG_PROCEDURE.md` §4 (`Status: partial — resumed` to start) and a status table with one
row per phase (0–3). After finishing each phase, update that row to `done` with a one- or two-line
summary of what was found/changed, and re-save the file — this is the checkpoint a resumed session
reads to know where to continue. Do **not** create a new numbered pair for a resumption — per §5, the
same `0021_CAPTURE_RESULT_2026_09_15.md` is progressively appended to.

This prompt does not instruct automatic git commits (project-wide convention: never commit without
being explicitly asked). Mention clearly, at the end of each session's final turn, what is
uncommitted so the maintainer can decide whether to commit before the next session starts.

## Shared per-capture methodology (apply to each of Phases 1–2, adjusted per phase's own notes)

**Step A — Orient.** Re-read this capture's `CAP-NNN-EVENT-NOTES.md`, its
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group section and §9 row, its `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
Test-ID row(s), and the precondition/baseline `CAP-NNN-FINDINGS.md` files named in this phase's own
section below.

**Step B — Inventory the folder's files** (`ls -la` the capture directory) and confirm the actual
filenames present — one video and one log expected for each of CAP-050 and CAP-051 (see Context
above), plus check whether a `.log.last` happens to exist for either. Fix any naming inconsistency
via `git mv`, noting the fix explicitly in the result file.

**Step C — Get exact video duration/metadata** for any video not yet `ffprobe`'d in a prior session.

**Step D — Video verification, scoped per capture (see each Phase's own instructions below) —** not
a blanket full re-watch requirement for a video a prior session has already reviewed in full; see
Context above for exactly what's owed for CAP-050 (a targeted re-check for the recalled mis-docking
event) vs. CAP-051 (spot-check only, if needed).

**Step E — Update the Event Timeline and Log Metadata** with anything Step D or the log analysis
(Step H) reveals that the existing `CAP-NNN-EVENT-NOTES.md` gets wrong or omits.

**Step F — Rename folders only if needed.** Neither capture is expected to need a folder rename in
this session (both are already correctly named from their prior video-only passes) — only use
`git mv` if Step B finds an actual naming inconsistency.

**Step G — `.log.last` resolution, only if one is actually found in Step B.** If one turns out to
exist for either capture, apply the same evidence-based method `CAP-040-FINDINGS.md`/
`CAP-042-FINDINGS.md`/`CAP-049-FINDINGS.md` §0 already established (compare its own Connection
Complete/timestamp content against the confirmed session start time) before concluding rotation vs.
leftover-buffer content — never assume. Skip this step entirely if no `.log.last` exists.

**Step H — Full, non-sampled log analysis.** Pre-filter by the Buds' own MAC address first (per
`AGENTS.md` §13's CLI-hygiene rule: `tshark -r CAP-NNN-btsnoop_hci.log -Y "bluetooth.addr == <MAC>"`
before layering DLCI-specific filters), then extract and review the **complete** relevant traffic for
this capture's scope across the entire log (not a sample window, not a subset of reconnects — the
maintainer's explicit instruction is "Doe geen steekproeven, maar doe analyses volledig"). Correlate
every event in the (corrected) Event Timeline to a specific frame number. Separately, inventory
anything unexplained or unclassified even outside this capture's own stated Test-ID scope (per
`AGENTS.md` §6's `UnidentifiedFrame` philosophy).

**Step I — Cross-checks and consistency checks.** Check internal consistency (video vs. log vs.
corrected Event Timeline) and correlate against `PROTOCOL.md`'s existing entries, `DECISIONS.md`
ADRs (§ above), `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, and the specific
prior `CAP-NNN-FINDINGS.md` files named in this phase's own section below. Flag, don't silently
resolve, any contradiction with an existing FACT-level claim.

**Step J — External validation.** For any wire finding that maps to a known open standard or public
mechanism (Pigweed `pw_hdlc` framing, Bluetooth RFCOMM/SDP specifics, protobuf varint/zigzag
decoding, or anything else that turns out to be relevant), verify against publicly available
documentation (WebSearch/WebFetch) and cite the source, following `DESKRESEARCH_FINDINGS.md`'s own
established pattern. Label every conclusion FACT/HYPOTHESIS/ASSUMPTION/OPEN QUESTION per
`PROJECT_RULES.md` rule 1 — an agreeing external source raises confidence toward HYPOTHESIS, it does
not itself authorize a 🟢 FACT promotion (see Guardrails).

**Step K — Write `CAP-NNN-FINDINGS.md`.** Follow the structure already established by
`CAP-040-FINDINGS.md` (Group AG's own prior attempt) and `CAP-038-FINDINGS.md`/`CAP-006-FINDINGS.md`
(Group AM's methodological precedent): scope/goal, methodology & any procedure deviation actually
observed, wire evidence per finding (hex + command, per rule 4a), explicit answer to this capture's
own stated Test-ID question(s), any bonus/incidental findings, open items, and an explicit verdict on
whether this session closes the relevant prior open item (`CAP-040-FINDINGS.md` §1/§3/§5 for CAP-050;
`CAP-038-FINDINGS.md` §5's uncorrelated Notify pattern for CAP-051) or not. Apply the hex-and-script
rule to every decoded burst.

**Step L — Update the cross-reference documents.**
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9: flip this row's Status from `planned` to `analyzed`, fill in
  Android/Firmware/App version, the (now-renamed/updated) log path(s), and append a short outcome
  note in the same terse style as the `CAP-040`/`CAP-048`/`CAP-049` rows.
- `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`: update `PRIV-001`'s row (CAP-050) and any `TOUCH-007`/`ANC-*`
  row touched incidentally by CAP-051 — Evidence column gets a pointer to the new
  `CAP-NNN-FINDINGS.md` section. If Phase 2 confirms `Group AM`'s own flagged gap (no existing
  Test-ID covers the DLCI-0x02/DLCI-0x04 correlation question), propose — do not unilaterally add —
  a new Test-ID, clearly labeled as a proposal awaiting maintainer sign-off.
- `id_registry.csv`: update the `CAP-050`/`CAP-051` rows' status field from `planned` to reflect the
  actual outcome (mirroring how other `analyzed` captures are recorded there).
- Update `0021_CAPTURE_RESULT_2026_09_15.md`'s status table for this phase before moving on.

## Phase 0 — Setup

Complete the Mandatory reading order above. Create `ai-sessions/0021_CAPTURE_RESULT_2026_09_15.md`
with the required header (`Status: partial — resumed`) and an empty phase-status table (Phases 0–3,
all `not started` except Phase 0 itself). Run `ls -la` on both capture folders and record the actual
current contents verbatim in the result file — this is the evidence base for CAP-050's
file-completeness branch decision in Phase 1 (Context above).

## Phase 1 — `CAP-050` (Group AG repeat, `PRIV-001`)

**Purpose (§9, repeat of `CAP-040`):** decode DLCI 0x08's 7 unmapped zero-length
`[Group][Code][00 00]`-shaped `Sent` frames (`05 0c`, `04 02`, `04 04`, `04 11`, `04 13`, `04 15`,
`0e 04`) via correlation against a known-changing value (dock state: Left/Right/both/neither), using
a trigger confirmed to actually reopen DLCI 0x08 (OS Bluetooth toggle or physical case/bud
docking/undocking) — `CAP-040` failed because the app's own in-app Connect/Disconnect buttons produce
zero wire-visible signal at all, leaving every code with only one sample.

Apply the shared methodology (Steps A–L). Specifically:

- **Step B/D (mis-docking check):** confirm via `ls -la` that only one video/one log exist (per
  Context above), then do the targeted video re-check for the recalled wrong-orientation docking
  event described in Context — dense `ffmpeg -ss <t> -frames:v 1` extraction around the case-handling
  windows already in `CAP-050-EVENT-NOTES.md` (especially the ambiguous-dock-state moments and the
  large `~00:04:00`–`00:11:41` window), not a full re-watch of the whole video. Update
  `CAP-050-EVENT-NOTES.md` with whatever is actually confirmed (an event found at a specific
  timestamp, or a confirmed absence) before moving to the log analysis below.
- **Step H (log analysis):** for each of the 7 unmapped codes, find its `Sent` Get frame and the
  immediately-following `Rcvd` response(s) on the same Group, across **every** reconnect in the
  entire session (well over 20 by the existing `CAP-050-EVENT-NOTES.md`'s own in-app-label count —
  confirm the actual DLCI 0x08 (re)open count directly from the log, not from that video-only
  estimate). Decode any numeric fields and check whether any field tracks the bracketed dock-state
  value (Left/Right/both/neither docked) repeat after repeat. A field that stays constant is not a
  match; only report a semantic reading for a field that visibly tracks the known value. This
  includes **resolving, from the log's own timestamps against the video overlay, the specific open
  questions `CAP-050-EVENT-NOTES.md` already flags** — the ambiguous-dock-state disconnects at
  `~21:02:37–39` and `~21:02:58–00`, and the large `~00:04:00`–`00:11:41` window that the prior
  video-only pass could not attribute to individual sub-cycles at its review density. The wire log's
  own DLCI 0x08 (re)open timestamps, cross-referenced against the video's burned-in overlay, can
  likely resolve at least some of these — do not leave them as unresolved open questions in
  `CAP-050-FINDINGS.md` if the log evidence actually settles them; only leave genuinely unresolvable
  ones open.
- **Step I:** cross-check this session's DLCI 0x08 (re)open behavior and any `Settable-toggles`-style
  dock-state field against ADR-024's existing model and the full `CAP-036`–`CAP-042`/`CAP-048`/
  `CAP-049` lineage (Step 12 of the reading order) for consistency — flag, don't silently resolve, any
  session that appears to contradict this one or vice versa.
- **Step K:** in `CAP-050-FINDINGS.md`, state plainly whether this repeat actually resolves
  `PRIV-001`'s open question (a clean three-way outcome per code: tracks the value / constant,
  therefore not a match / still inconclusive) or whether some further repeat is still needed.
  Precondition/baseline source: `CAP-040-FINDINGS.md` §1/§3/§5.

## Phase 2 — `CAP-051` (Group AM, new)

**Purpose (§9):** `REVERSE_ENGINEERING.md`'s `qhr` entry traces ANC state (field 13) to two
code-side callers — the in-app `QuickActionsFragment` toggle tap and the physical press-and-hold
gesture (`gvi`/`gvj`) — but neither has ever been wire-confirmed against a DLCI 0x02
`field5{field4{field13=N}}` write, alongside DLCI 0x04's already-confirmed ANC path. This session's
video (already fully reviewed, `ai-sessions/0020`) shows one in-app tap
(`Adaptive`→`Transparency`, ~21:43:26–27) followed by three physical press-and-hold gestures on the
same earbud (`Transparency`→`Noise cancellation` ~21:43:38–42; `Noise cancellation`→`Adaptive`
~21:43:51/52–55; `Adaptive`→`Transparency` ~21:44:02–04) — two of the three inter-gesture gaps fall
short of the planned ≥10s isolation window (flagged already in `CAP-051-EVENT-NOTES.md`).

Apply the shared methodology (Steps A, B, C [already done], D [spot-check only, see Context above],
E–L). Specifically:

- **Step H (log analysis):** pre-filter by the Buds' MAC, then extract DLCI 0x02's **complete**
  traffic across the whole log (raw-path extraction is claimed for this session — Step B/Step A
  should independently confirm this from the log itself, e.g. absence of `btsnooz.py`-style
  truncation, rather than trusting the skeleton's inherited claim, per `CAP-051-EVENT-NOTES.md`'s own
  explicit note that this wasn't confirmable from video alone). For **each** of the four actions (the
  tap, and all three gestures — not just two, correcting the originally-planned single-tap/
  single-gesture design), check whether a `field5{field4{field13=N}}` write appears on DLCI 0x02 at
  that moment, and whether the value of `N` matches the resulting on-screen ANC mode for each of the
  4 transitions logged in the Event Timeline. Also check DLCI 0x04 for its own already-confirmed ANC
  Notify at each of the same 4 moments, so the two channels' behavior can be directly compared
  side-by-side per action.
- **Step I:** because two of the three inter-gesture gaps are shorter than the planned ≥10s isolation
  window (per `CAP-051-EVENT-NOTES.md`'s own spacing summary), explicitly account for this when
  attributing any DLCI 0x02 write to a specific gesture — if a write's timestamp could plausibly
  belong to either of two closely-spaced gestures, say so rather than picking one. Cross-reference
  against `CAP-038-FINDINGS.md` §5's two previously-unexplained DLCI-0x04-Notify-with-no-preceding-
  Get/Set frames — does this session's own physical-gesture mechanism reproduce that same "Notify
  without Set" pattern on DLCI 0x04, and does DLCI 0x02 do anything parallel at those same moments?
- **Step K:** in `CAP-051-FINDINGS.md`, answer the Group AM question plainly for all 4 actions
  individually (a positive match, a confirmed absence, or genuinely inconclusive due to the isolation-
  window overlap) — a positive match on any action is strong evidence DLCI 0x02 also carries ANC
  state in parallel to DLCI 0x04's confirmed path; record the result either way, per the Analysis
  checklist already in `CAP-051-EVENT-NOTES.md`. Precondition/baseline source: `CAP-006-FINDINGS.md`
  (DLCI-0x04 methodology) and `CAP-038-FINDINGS.md` §5.

## Phase 3 — Cross-capture synthesis and wrap-up

- Re-read both newly-written `CAP-NNN-FINDINGS.md` files together. Check for agreement/conflict
  between them and with every prior `CAP-NNN-FINDINGS.md` file named in the reading order above.
- Confirm every Test-ID either capture's Group is supposed to exercise (`PRIV-001`; `TOUCH-007`/
  `ANC-*` incidentally) is actually referenced in that capture's own Event Timeline — a traceability
  gap (`AGENTS.md` §13 item 7) is a finding to flag explicitly, not silently ignore.
- Run `python3 scripts/lint_docs.py`; fix any newly introduced unregistered-ID or dead-filename-
  reference finding (a CAP-050 folder change from Phase 1 is the most likely source — grep for any
  old placeholder path across the repo before finishing).
- Run `./scripts/ensure_footers.py` if any new or edited `.md` file is missing its footer.
- Update `ai-sessions/INDEX.md`'s row for `0021` to match this session's final `Status`.
- Finalize `0021_CAPTURE_RESULT_2026_09_15.md`: `Status: complete` only if every phase's work is
  actually done and no proposed FACT-promotion/ADR/new-Test-ID is still pending maintainer review;
  otherwise `Status: awaiting maintainer sign-off` (if the only thing left is maintainer review of
  proposed promotions/proposals) or `partial — resumed` (if genuine work remains).
- Write a final summary for the maintainer: what was found for each capture (including whether the
  recalled CAP-050 mis-docking event was confirmed, at what timestamp, and whether it explains any
  previously-flagged ambiguous dock-state moment), any newly discovered anomalies, and an explicit
  list of every proposed 🟢 FACT promotion,
  `DECISIONS.md` ADR, or new `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` Test-ID (clearly labeled as proposals,
  per Guardrails) awaiting sign-off.

## Guardrails

- **Never independently promote a finding to 🟢 FACT in `PROTOCOL.md`, and never write or alter a
  `DECISIONS.md` ADR, without explicit maintainer approval** (`AGENTS.md` §6/§15). Propose and label
  clearly as a proposal in the relevant `CAP-NNN-FINDINGS.md` and in the Phase 3 summary instead —
  this applies even where an external source (Step J) agrees with your reading, and even where two
  independent models/sessions would agree.
- Never invent a timestamp, opcode meaning, dock state, or value not actually observed in the
  video/log — an unrecoverable byte's meaning, or an ambiguous dock-state moment the log genuinely
  cannot resolve, is an explicit open question, not a filled-in guess (`PROJECT_RULES.md` rule 1;
  `AGENTS.md` §13.6's zero-creativity instruction).
- Apply the hex-and-script rule (`PROJECT_RULES.md` rule 4a) to every decoded burst: the exact
  command **and** the raw hex bytes, not just the interpretation.
- Never fabricate/interpolate a battery percentage (`AGENTS.md` §5); never log the Buds' MAC address
  at INFO level or above, and never dump raw payload bytes outside this project's existing
  debug-mode gating conventions (`AGENTS.md` §9) in anything you write.
- Use `git mv` for every folder/file rename in this task — never a plain `mv` — so git history (and
  Git LFS-tracked binaries, `PROJECT_RULES.md` rule 18) survive the rename.
- No sampling: every video watched in this session is watched in full (Step D, where applicable),
  every log analyzed in full across its **entire** duration (Step H) — a partial pass is not an
  acceptable substitute anywhere in this task, per the maintainer's explicit instruction ("Doe geen
  steekproeven, maar doe analyses volledig").
- Do not assume the recalled CAP-050 mis-docking event happened, or happened at a particular moment,
  without video/log evidence — a confirmed absence is an acceptable, reportable outcome (see
  Context above).
- This is a pure documentation/analysis pass on already-executed capture sessions — no new Bluetooth
  action against the hardware is performed as part of this prompt.

## Output

At the end of each phase, a short note (in the RESULT file, and to the maintainer if the chat is
still live) of what was found/changed for that capture. At the end of Phase 3, the full summary
described there. Every substantive claim in `CAP-050-FINDINGS.md`/`CAP-051-FINDINGS.md` must cite its
evidence (frame number, video timestamp, or file+line) per `PROJECT_RULES.md` rule 3.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0021_CAPTURE_PROMPT_2026_09_15.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0021_CAPTURE_PROMPT_2026_09_15
