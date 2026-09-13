# 0016_CAPTURE_PROMPT_2026_09_13.md — Full video+log re-analysis and FINDINGS for CAP-043, CAP-044

**Number:** 0016
**Category:** CAPTURE
**Date:** 2026-09-13
**Title:** Full video+log re-analysis and FINDINGS for CAP-043 (Group Q repeat, BATT-002/BATT-003) and CAP-044 (Group AA repeat, SDP-001/SDP-002)

---

## How to (re)start this prompt — read this paragraph first, every time

This is a multi-phase task that **may** span more than one chat session (rate limits, context
resets, or the maintainer simply stopping for the day). It is designed to be pasted verbatim into
a **new** Claude Code chat, including on a resumption.

Before anything else: check whether `ai-sessions/0016_CAPTURE_RESULT_2026_09_13.md` already exists.
- **It does not exist** → this is the first run. Start at Phase 0.
- **It already exists** → read it in full. Its per-phase status table (created in Phase 0) says
  which phases are already `done`. Skip straight to the first phase not yet marked `done` and
  continue from there — do not redo a `done` phase's work. Regardless of where you resume, you
  still owe the full "Mandatory reading order" below in this session — `AI_SESSION_LOG_PROCEDURE.md`
  §8 requires it for every session that acts on this repo, resumption or not, since a new chat has
  no memory of a previous one.

Every phase boundary is a safe stopping point: before ending a turn (whether because the phase is
done, or because you sense a rate limit / context limit approaching), update
`0016_CAPTURE_RESULT_2026_09_13.md`'s status table and `Status` header field (per
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
   **ADR-018** (the `gbm.a()` internal-RFCOMM-socket UUID branch — directly relevant to `CAP-044`).
5. `ARCHITECTURE.md`.
6. `PROTOCOL.md` (full) — especially §4.3 (battery mechanisms, Option A specifically) and §2
   (envelope/framing hypotheses, relevant to any RFCOMM traffic seen incidentally in either
   capture).
7. `TODO.md` (full).
8. `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md`.
9. `CAPTURE_BLUETOOTH_HCI_SNOOP.md` in full — specifically:
   - §2/§6's log-rotation note (why `.log`/`.log.last` pairs happen at all — check whether either
     capture folder actually has a `.log.last`; as of this prompt's drafting, neither does, but
     re-confirm via `ls` rather than trusting this note).
   - §4's Group **Q** section (items 18–20, the "three-way outcome" guidance for #18/19/20) and
     Group **AA** section (`SDP-001`/`SDP-002`, the `gbm.a()` UUID branch, and its "explicitly out
     of scope" note).
   - §9's Capture Index rows for `CAP-011` (original Group Q attempt) and `CAP-033` (original
     Group AA attempt), plus the `CAP-043`/`CAP-044` rows themselves.
10. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` — the catalog rows for `BATT-002`, `BATT-003`, `SDP-001`,
    `SDP-002`.
11. `DESKRESEARCH_FINDINGS.md` in full — its template, its status legend, and its existing entries
    that validate a wire finding against an external spec (this is the pattern to follow for this
    prompt's own "validate against internet sources" requirement, e.g. the Fast Pair Battery
    Notification byte layout for `CAP-043`).
12. The following existing `CAP-NNN-FINDINGS.md` files, read in full — this task builds directly
    on them:
    - `captures/CAP-011-2026-08-21_09-45-17_09-55-16-Group_Q/CAP-011-FINDINGS.md` §3/§4/§5/§6/§7
      — the original Battery Notification attempt `CAP-043` repeats, including its own procedure
      deviation (an active classic RFCOMM+GATT connection was present throughout) that `CAP-043`
      exists specifically to avoid, and its §7 DLCI 0x08 per-earbud battery push addendum.
    - `captures/CAP-033-2026-08-30_15-17-03_15-19-52-Group_AA/CAP-033-FINDINGS.md` §1/§3/§5/§6/§8
      — the original SDP UUID branch attempt `CAP-044` repeats, including its own procedure
      deviation (Forget preceded Force-stop by ~10s, and the app-open comparison step was never
      run) that `CAP-044` exists specifically to correct.
    - `REVERSE_ENGINEERING.md`'s `gbm`/`fzd` entries (the "pigweed" vs. "default internal rfcomm
      socket" UUID branch that `SDP-001`/`SDP-002` test).
13. Both existing skeleton files, in full, as the current (hand-filled, unverified) baseline:
    - `captures/CAP-043-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_Q/CAP-043-EVENT-NOTES.md`
    - `captures/CAP-044-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AA/CAP-044-EVENT-NOTES.md`

    Neither folder has been renamed from its `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder yet, even
    though both sessions have actually been run (video + log files already present) — that rename
    is part of this prompt's own work (Step F below).

## Context (from the maintainer, not re-derived here)

Both sessions were actually executed today (2026-09-13) by the maintainer, on a **Pixel 7a**,
**Android 17**, Pixel Buds Pro 2 firmware **`release_5.203`**, official Pixel Buds app version
**`1.0.955078536`**, with **Google Play services active** on the phone. Treat these five values as
confirmed (not `TBD`, not `⚪ assumed`) when filling in every Log Metadata table and
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 row in this prompt's scope.

Each `CAP-NNN-EVENT-NOTES.md` in scope was filled in **by hand** today and may not accurately
reflect what was actually done — the maintainer did not always follow the planned procedure. **Every
video must be watched in full and every log analyzed in full** before any timeline or finding is
finalized — no sampling, no skipping sections, no trusting the draft timeline's stated order/times
without independent video/log confirmation.

**Specific known deviation to verify, not assume (`CAP-044`):** the planned `SDP-001` procedure
called for "Forget" to be performed via **system Bluetooth settings only**, with the companion app
staying force-stopped throughout, so the app "cannot react to the pairing at all" (per
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA step 1). The maintainer reports that on a Pixel phone,
Android's own Bluetooth settings for an already-bonded Pixel Buds device route directly into the
companion app's own device-management screen — there is no way to "Forget" a Pixel Buds Pro 2
purely from system Bluetooth settings without the app surfacing, because the OS-level integration
is that tight. Concretely, the maintainer force-stopped the app, then performed "Forget" **from
within the reopened companion app itself** (not system settings), because no other path existed.
The draft `CAP-044-EVENT-NOTES.md` timeline already shows this (13:05:55 "User navigates back into
the Pixel Buds app," 13:05:59 "'Forget device' tapped within the Pixel Buds app," both flagged
"Deviation" in the draft) — but the draft was hand-typed and must still be independently confirmed
frame-by-frame per Step D below, not taken on faith. This has two consequences to reason through in
Phase 2, not just note in passing:
1. It may mean `SDP-001`'s originally-envisioned "force-stop, so the app cannot react to the
   pairing at all" isolation is not achievable at all on this hardware/OS combination — worth
   determining explicitly and, if confirmed, worth flagging as a **procedure-feasibility finding**
   for `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA / `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` `SDP-001` (a
   proposal, not a unilateral edit — see Guardrails).
2. It does not necessarily invalidate the whole session — determine from the actual timeline
   whether there was *any* window (e.g. between the app-triggered Forget and the app resurfacing
   its UI at 13:08:10) where an SDP browse happened without the app actively driving it, and
   whether step 3's app-open comparison browse (needed either way) was actually captured cleanly.
   Reason from the evidence in the log/video, not from what the plan assumed would happen.

Beyond each capture's own stated Test-ID goal, actively look for **anything else** interesting in
the video or the wire log — new opcodes, unexpected reconnects, timing anomalies, anything that
doesn't parse, anything that contradicts an existing `PROTOCOL.md`/`DECISIONS.md` claim. That is an
explicit goal of this task, not a nice-to-have.

## Session bookkeeping & resumability

In Phase 0, create `ai-sessions/0016_CAPTURE_RESULT_2026_09_13.md` with the header block required
by `AI_SESSION_LOG_PROCEDURE.md` §4 (`Status: partial — resumed` to start) and a status table with
one row per phase (0–3). After finishing each phase, update that row to `done` with a one- or
two-line summary of what was found/changed, and re-save the file — this is the checkpoint a resumed
session reads to know where to continue. Do **not** create a new numbered pair for a resumption —
per §5, the same `0016_CAPTURE_RESULT_2026_09_13.md` is progressively appended to.

This prompt does not instruct automatic git commits (project-wide convention: never commit without
being explicitly asked). Since all work happens in the same working directory, on-disk progress
survives a session break on its own; mention clearly, at the end of each session's final turn, what
is uncommitted so the maintainer can decide whether to commit before the next session starts.

## Shared per-capture methodology (apply this to each of Phases 1–2)

**Step A — Orient.** Re-read this capture's skeleton `CAP-NNN-EVENT-NOTES.md`, its
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group section and §9 row, its `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
Test-ID row(s), and the precondition/baseline `CAP-NNN-FINDINGS.md` named in this phase's own
section below.

**Step B — Inventory the folder's files** (`ls` the capture directory) and confirm the actual
filenames match what's expected (`CAP-NNN-btsnoop_hci.log`, `CAP-NNN-recording.mp4`,
`CAP-NNN-EVENT-NOTES.md`, and check whether a `.log.last` exists — it did not as of this prompt's
drafting, but re-confirm). Fix any naming inconsistency via `git mv`, noting the fix explicitly in
the result file.

**Step C — Get exact video duration/metadata:** `ffprobe` the recording. Recordings in this project
carry a burned-in wall-clock overlay (per `README.md`) — that overlay is your primary time source
for the Event Timeline, not the file's own internal timestamp.

**Step D — Watch the entire video, not a sample.** Using `ffmpeg -ss <t> -frames:v 1` (this
project's established method, see `DESKRESEARCH_FINDINGS.md`), extract frames across the video's
**full** duration at an interval dense enough that no on-screen action or state change could be
missed between two consecutive extracted frames (a rough guide: every 1–2s for these short
sessions, tightened to sub-second spacing around any moment where the draft `CAP-NNN-EVENT-NOTES.md`,
the log, or a previous frame suggests something is about to happen — every tap, prompt, and screen
transition in `CAP-044` in particular). Save frames to your scratchpad directory. Read each one and
build an independent, complete, time-stamped action list from what's actually on screen — then
reconcile it against the existing draft timeline, correcting order and timing rather than trusting
the draft.

**Step E — Rewrite the Event Timeline and Log Metadata.** Update `CAP-NNN-EVENT-NOTES.md`'s Event
Timeline table to the verified real sequence (add missed events, fix ordering/timestamps, remove
anything the video disproves). Fill in Log Metadata: Date `2026-09-13`, Test device `Pixel 7a`,
Android `17`, Firmware `release_5.203`, App version `1.0.955078536`, and note Google Play services
active. Determine this video's exact start and end wall-clock time from the burned-in overlay.

**Step F — Rename the capture folder.** Use `git mv` to rename
`captures/CAP-NNN-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_X` to
`captures/CAP-NNN-2026-09-13_HH-MM-SS_HH-MM-SS-Group_X` using the start/end times from Step E (keep
the `-Group_X` suffix unchanged — `Group_Q` for `CAP-043`, `Group_AA` for `CAP-044`). Update the
header line and any internal path references inside `CAP-NNN-EVENT-NOTES.md` to match the new
folder name.

**Step G — `.log`/`.log.last` resolution, only if a `.log.last` file is actually present** (re-check
in Step B; neither capture is currently known to have one). If one turns out to exist, apply the
same evidence-based method `CAP-040-FINDINGS.md`/`CAP-042-FINDINGS.md` established (compare
`.log.last`'s own Connection Complete/timestamp content against this session's confirmed start time
and any neighboring capture) before concluding rotation vs. leftover — do not assume either
answer. Skip this step entirely if no `.log.last` exists.

**Step H — Full, non-sampled log analysis.** Pre-filter by the Buds' own MAC address first (per
`AGENTS.md` §13's CLI-hygiene rule), then extract and review the **complete** relevant traffic for
this capture's scope (`btle`/GATT for `CAP-043`'s Battery Notification question; `btsdp` plus
`btrfcomm` DLCI activity for `CAP-044`'s SDP-browse and re-pairing sequence) across the entire log
(not a sample window). Correlate every event in the corrected Event Timeline to a specific frame
number. Separately, inventory anything unexplained or unclassified even outside this capture's own
stated Test-ID scope (per this prompt's Context above and `AGENTS.md` §6's `UnidentifiedFrame`
philosophy).

**Step I — Cross-checks and consistency checks.** Check internal consistency (video vs. log vs.
corrected Event Timeline) and correlate against `PROTOCOL.md`'s existing entries, `DECISIONS.md`
ADRs (especially ADR-018 for `CAP-044`), and the specific prior `CAP-NNN-FINDINGS.md` files named in
this phase's own section below. Flag, don't silently resolve, any contradiction with an existing
FACT-level claim.

**Step J — External validation.** For any wire finding that maps to a known open standard (Fast
Pair Battery Notification structure for `CAP-043`; SDP Service Search Attribute Response / Bluetooth
SDP protocol UUIDs for `CAP-044`), verify against publicly available specification documentation
(WebSearch/WebFetch) and cite the source, following `DESKRESEARCH_FINDINGS.md`'s own established
pattern for this. Label every conclusion FACT/HYPOTHESIS/ASSUMPTION/OPEN QUESTION per
`PROJECT_RULES.md` rule 1 — an agreeing external source raises confidence toward HYPOTHESIS, it does
not itself authorize a 🟢 FACT promotion (see Guardrails).

**Step K — Write `CAP-NNN-FINDINGS.md`.** Follow the structure already established by
`CAP-011-FINDINGS.md` and `CAP-033-FINDINGS.md` (the sessions being repeated/closed out here):
scope/goal, methodology & any procedure deviation actually observed, wire evidence per finding (hex
+ command, per rule 4a), explicit answer to this capture's own stated Test-ID question(s), any
bonus/incidental findings, open items, and an explicit "does this close `CAP-011-FINDINGS.md` §4 /
`CAP-033-FINDINGS.md` §5/§8's open item, or not" verdict. Apply the hex-and-script rule to every
decoded burst.

**Step L — Update the cross-reference documents.**
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9: flip this row's Status from `planned` to `analyzed`, fill in
  Android/Firmware/App version, the (now-renamed) log path(s), and append a short outcome note in
  the same terse style as the `CAP-038`/`CAP-040`/`CAP-042` rows.
- `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`: update this capture's Test-ID row(s)' Evidence column with a
  pointer to the new `CAP-NNN-FINDINGS.md` section.
- `id_registry.csv`: update the `CAP-043`/`CAP-044` rows' status field from `planned` to reflect the
  actual outcome (mirroring how other `analyzed` captures are recorded there).
- Update `0016_CAPTURE_RESULT_2026_09_13.md`'s status table for this phase (see "Session
  bookkeeping" above) before moving on.

## Phase 0 — Setup

Complete the Mandatory reading order above. Create `ai-sessions/0016_CAPTURE_RESULT_2026_09_13.md`
with the required header (`Status: partial — resumed`) and an empty phase-status table (Phases 0–3,
all `not started` except Phase 0 itself). Confirm via `ls` the actual current contents of both
capture folders (already done once while drafting this prompt — re-confirm nothing changed).

## Phase 1 — `CAP-043` (Group Q repeat, `BATT-002`, `BATT-003`)

Purpose (§9, repeat of `CAP-011`): determine, with the companion app force-stopped and zero active
classic RFCOMM+GATT connection for the entire capture, whether the Fast Pair Battery Notification
BLE advertisement (`PROTOCOL.md` §4.3 Option A) is present and whether its payload structurally
matches the documented layout (Flags / Account Key Data / Length&Type byte `0x33`/`0x34` / three
battery-percentage octets) — `CAP-011` found the `0xFE2C` service present but non-matching, with a
connection-present procedure deviation this repeat must avoid.

Apply the shared methodology (Steps A–F, H–L; skip G unless Step B finds a `.log.last`). During
Step A/I, treat the **isolation check** as the primary thing to verify, not assume: confirm from the
video and from the log itself (zero Connection Complete / zero RFCOMM frames to the Buds' address
for the entire window) that the app truly stayed force-stopped and no classic connection ever formed
— this is the entire point of the repeat, per the skeleton's own "Isolation check (required)" note.
The draft timeline's ~3-minute observation window (09:50:35–09:53:40) must be independently
re-verified against both the video overlay and the log's own frame timestamps, not copied as-is.

During Step H, filter for `0xFE2C` service-data BLE advertisements (`btle`, not `btrfcomm`/`btatt`)
across the **entire** log and check every sampled payload against `PROTOCOL.md` §4.3 Option A's
byte layout. Record the result explicitly either way per the skeleton's analysis checklist: a
structural match promotes Option A toward FACT (propose only, per Guardrails); a second confirmed
non-match (this being the second connection-free-or-not attempt) is itself a reportable finding
worth re-examining the hypothesis, not a failed session. Precondition/baseline source:
`CAP-011-FINDINGS.md` §3/§4/§6/§7.

## Phase 2 — `CAP-044` (Group AA repeat, 2nd attempt, `SDP-001`, `SDP-002` opportunistic)

Purpose (§9, repeat of `CAP-033`): isolate which SDP UUID set (`25e97ff7-...` "pigweed" vs.
`3a046f6d-...` "default internal rfcomm socket", `REVERSE_ENGINEERING.md`'s `gbm`/`fzd` entries,
`DECISIONS.md` ADR-018) shows up when SDP is queried via the OS's own pairing flow, compared against
the companion app's own `fetchUuidsWithSdp()` re-fetch, in one session with force-stop strictly
preceding forget and the app-open comparison browse (step 3) actually executed — the two gaps that
capped `CAP-033` at 🟡 HYPOTHESIS.

Apply the shared methodology (Steps A–F, H–L; skip G unless Step B finds a `.log.last`). Step D is
unusually important here — every tap, prompt, and screen transition in the draft timeline
(13:05:51 through 13:11:07) needs frame-level confirmation, not just the ones already flagged
"Deviation" in the draft, since the draft itself says the plan wasn't followed exactly.

During Step A/I, work through the **specific deviation described in this prompt's Context section
above** (Force-stop was done first, but "Forget" then had to be performed from *within* the
reopened companion app, not system Bluetooth settings, because Android routes an already-bonded
Pixel Buds device's system-settings entry into the app itself) as a concrete question to answer from
the evidence, not a foregone conclusion:
- Does the log show any SDP Service Search Attribute Response frames *before* the app resurfaces its
  own UI (13:08:10 in the draft, to be re-verified)? If so, what UUID set do they contain?
- Is there a clean, separate app-driven SDP browse later in the same log (the step-3 comparison)
  that can be directly diffed against the earlier one?
- Does the "default" UUID (`3a046f6d-...`, either byte order) appear anywhere in this log at all —
  continuing the zero-occurrence streak `REVERSE_ENGINEERING.md`'s `gbm` entry already documents
  across 26 prior files, or breaking it?
- Does the "MAESTRO APP"/"GSND CONTROL"/"GSND AUDIO" service naming (`CAP-033-FINDINGS.md` §3)
  reproduce identically here?

Use the exact `tshark` filter already validated in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Group AA
Analysis section (pre-filter by address per §13, then `btsdp`) — reproduced there and in
`REVERSE_ENGINEERING.md`'s `gbm` entry. In `CAP-NNN-FINDINGS.md` (Step K), state plainly whether
this session actually closes `SDP-001` (clean result either way) or whether the forget-via-app
deviation means the originally-envisioned isolation is infeasible on this hardware/OS and the
Test-ID's question needs reframing — if the latter, write this up as an explicit **proposed**
procedure-feasibility note for `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA and
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `SDP-001` row, clearly labeled as a proposal awaiting maintainer
sign-off (never edit those rows' substantive conclusions unilaterally beyond the mechanical §L
updates). `SDP-002` stays out of scope (opportunistic, requires a pending firmware OTA — note its
status as still "not attempted" unless the video/log show otherwise). Precondition/baseline source:
`CAP-033-FINDINGS.md` §1/§3/§5/§6/§8.

## Phase 3 — Cross-capture synthesis and wrap-up

- Re-read both newly-written `CAP-NNN-FINDINGS.md` files together. Check for agreement/conflict
  between them and with the sessions they build on (does `CAP-043` actually resolve
  `CAP-011-FINDINGS.md` §4's open item? Does `CAP-044` actually resolve `CAP-033-FINDINGS.md` §5/§8's
  open item, or does the forget-via-app deviation mean a third attempt is still needed?).
- Run `python3 scripts/lint_docs.py`; fix any newly introduced unregistered-ID or dead-filename-
  reference finding (folder renames from Step F are the most likely source of a stale reference —
  grep for the old placeholder folder names across the repo before finishing).
- Run `./scripts/ensure_footers.py` if any new or renamed `.md` file is missing its footer.
- Update `ai-sessions/INDEX.md`'s row for `0016` to match this session's final `Status`.
- Finalize `0016_CAPTURE_RESULT_2026_09_13.md`: `Status: complete` only if every phase's work is
  actually done and no proposed FACT-promotion/ADR/procedure-change is still pending maintainer
  review; otherwise `Status: awaiting maintainer sign-off` (if the only thing left is maintainer
  review of proposed promotions/proposals) or `partial — resumed` (if genuine work remains).
- Write a final summary for the maintainer: what changed per capture, any newly discovered
  anomalies, and an explicit list of every proposed 🟢 FACT promotion, `DECISIONS.md` ADR, or
  procedure-documentation change (clearly labeled as proposals, per Guardrails) awaiting sign-off.

## Guardrails

- **Never independently promote a finding to 🟢 FACT in `PROTOCOL.md`, and never write or alter a
  `DECISIONS.md` ADR, without explicit maintainer approval** (`AGENTS.md` §6/§15). Propose and label
  clearly as a proposal in the relevant `CAP-NNN-FINDINGS.md` and in the Phase 3 summary instead —
  this applies even where an external source (Step J) agrees with your reading, and even for the
  Group AA procedure-feasibility question raised in Phase 2.
- Never invent a timestamp, opcode meaning, or value not actually observed in the video/log — an
  unrecoverable byte's meaning is an explicit `PROTOCOL.md` §6-style open question, not a filled-in
  guess (`PROJECT_RULES.md` rule 1, `AGENTS.md` §13's "zero creativity" instruction for hex
  parsing).
- Apply the hex-and-script rule (`PROJECT_RULES.md` rule 4a) to every decoded burst: the exact
  command **and** the raw hex bytes, not just the interpretation.
- Never fabricate/interpolate a battery percentage (`AGENTS.md` §5); never log the Buds' MAC address
  at INFO level or above, and never dump raw payload bytes outside this project's existing
  debug-mode gating conventions (`AGENTS.md` §9) in anything you write.
- Use `git mv` for every folder/file rename in this task (Steps F, B) — never a plain `mv` — so git
  history (and Git LFS-tracked binaries, `PROJECT_RULES.md` rule 18) survive the rename.
- No sampling: every video is watched in full (Step D), every log analyzed in full (Step H) — a
  partial pass is not an acceptable substitute anywhere in this task, per the maintainer's explicit
  instruction.
- Do not decide, after the fact, whether `CAP-044`'s forget-via-app deviation was "acceptable" or
  not — it already happened; Phase 2's job is to determine and document exactly what it does and
  doesn't prove about `SDP-001`, not to second-guess the maintainer's on-the-day choice (which, per
  the Context section, was not optional given the OS's own behavior).
- This is a pure documentation/analysis pass on already-executed capture sessions — no new
  Bluetooth action against the hardware is performed as part of this prompt.

## Output

At the end of each phase, a short note (in the RESULT file, and to the maintainer if the chat is
still live) of what was found/changed for that capture. At the end of Phase 3, the full summary
described there. Every substantive claim must cite its evidence (frame number, video timestamp, or
file+line) per `PROJECT_RULES.md` rule 3.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0016_CAPTURE_PROMPT_2026_09_13.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0016_CAPTURE_PROMPT_2026_09_13
