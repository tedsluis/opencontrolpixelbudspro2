# 0031_MAINTENANCE_PROMPT_2026_09_18.md — Continue the `0030` worklist, start `TODO.md` Phase 3 (protocol reconstruction), full document consistency pass, compile every pending maintainer sign-off, and produce a v1-readiness overview

**Number:** 0031
**Category:** MAINTENANCE
**Date:** 2026-09-18
**Title:** Resume `ai-sessions/0030`'s static-analysis-tractable worklist; advance `TODO.md`'s Phase 3 (Protocol reconstruction) open items; run a full cross-check/consistency pass across every project document and bring them all up to date; systematically compile every finding still awaiting maintainer sign-off and present each one, individually summarized, for the maintainer's approval; finish with a clear, prioritized overview of everything that remains before v1 app development can begin in earnest
**Status:** prompt only — not yet run

---

## How to (re)start this prompt — read this paragraph first, every time

This task is designed to be pasted verbatim into a **new Claude Code chat**, including on a
resumption. Before anything else: check whether `ai-sessions/0031_MAINTENANCE_RESULT_2026_09_18.md`
already exists.

- **It does not exist** → this is the first run. Start at Phase 0.
- **It already exists** → read it in full. Its per-phase status table (created in Phase 0) says what's
  already `done`. Skip straight to the first item not yet marked `done` and continue from there.

Every phase (or, within Phase 1/2, every worklist-item) boundary is a safe stopping point: before
ending a turn, update `0031_MAINTENANCE_RESULT_2026_09_18.md`'s status table and `Status` header field
(`partial — resumed` while incomplete) so the next session picks up cleanly. This is an intentionally
large, five-part task — per the maintainer's own instruction below, work through as much of it as time
allows in a single sitting; do not force everything into one turn, and do not skip Phase 4's interactive
sign-off step just because it's slower than the static-analysis phases.

## Mandatory reading order (do this first, in this order, in every session that works on this prompt)

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8:

1. `AGENTS.md` (full) — especially §4/§6 (mechanical-assistance boundary; never independently promote a
   FACT or write a `DECISIONS.md` ADR) and §13.6 (zero-creativity, evidence-only rule).
2. `PROJECT.md`, `PROJECT_RULES.md` (full), `ARCHITECTURE.md` (full, including §15's open architecture
   questions).
3. `DECISIONS.md` — full ADR history (ADR-001 through the highest-numbered entry present).
4. `PROTOCOL.md` **in full, start to finish** — not just §6 this time (Phase 3 of this prompt touches
   the connection-lifecycle sections in §5, and Phase 3's consistency pass needs the whole document).
5. `TODO.md` in full — especially the "Recommended priority order" section (top, now corrected by
   `ai-sessions/0030`) and Phase 3 ("Protocol reconstruction").
6. `REVERSE_ENGINEERING.md` **in full, start to finish** — `ai-sessions/0030`'s own prompt file
   disclosed that its own execution (`ai-sessions/0030_MAINTENANCE_RESULT_2026_09_17.md`'s
   "Mandatory reading order" section) deliberately did **not** read this file linearly, as a scope
   trade-off — this session should close that gap rather than repeat it, since Phase 3's consistency
   pass below depends on actually knowing the whole document.
7. `reverse-engineering/tools/BACKLOG.md`, all 5 existing tools' own SPEC/README files (under
   `reverse-engineering/tools/`), `APK_REVERSE_ENGINEERING_PROCEDURE.md`, `AI_SESSION_LOG_PROCEDURE.md`
   — `ai-sessions/0030` also deliberately skipped a linear read of these; read them properly this time.
8. `ai-sessions/INDEX.md` in full.
9. `ai-sessions/0027_MAINTENANCE_RESULT_2026_09_17.md` through `ai-sessions/0030_MAINTENANCE_RESULT_2026_09_17.md`
   in full (the immediately preceding sessions this one continues from).
10. `CHANGELOG.md`, `README.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`,
    `DESKRESEARCH_FINDINGS.md`, `CONTRIBUTING.md`, `WORKSTATION_PREPARATIONS.md`, and `id_registry.csv`
    — needed for Phase 3's own cross-document consistency pass; skim rather than deep-read where a
    document is large and not directly implicated by anything found so far, but do not skip any of them
    entirely.

## Context (from the maintainer, not re-derived here)

The maintainer asked, in their own words (translated from Dutch, preserved verbatim below the
horizontal rule at the end of this prompt), for a single new prompt that bundles five distinct pieces of
follow-on work after `ai-sessions/0030`:

1. **Continue `ai-sessions/0030`'s own worklist** into this (or a later) session, rather than starting a
   separate, disconjoined thread for it.
2. **Start on `TODO.md`'s Phase 3 ("Protocol reconstruction")** — its own still-open items specifically
   (most of that section is already closed; see Phase 2 below for what's actually left).
3. **Run a cross-check and consistency pass across every project document**, and bring all of them fully
   up to date — not just the documents `0030` happened to touch.
4. **Let the maintainer give sign-off, per item, on every finding that still needs one** — each
   presented with its own short summary, so the maintainer can approve (or decline) individually rather
   than as an undifferentiated batch.
5. **Produce a clear overview of everything that still needs attention before starting v1 app
   development in earnest** — the maintainer's own explicit framing is that this overview is a
   pre-condition for starting the build phase, not a nice-to-have alongside it.

This is a genuinely large, five-part ask — larger in scope than any single `NNNN_MAINTENANCE` prompt
this project has run so far. The phase structure below sequences the five parts deliberately: worklist
continuation and Phase 3 protocol reconstruction (parts 1–2) can surface **new** findings that
themselves need sign-off, so they run *before* Phase 4's sign-off compilation (part 4) — compiling that
list first and then finding more items afterward would mean re-running it. The consistency pass (part 3)
runs after parts 1–2 for the same reason (new findings need to be reflected consistently before a
document-wide pass, not patched in afterward) but before Phase 4 (an inconsistent document makes a
sign-off summary harder to state accurately). The v1-readiness overview (part 5) runs last, once
everything above is both current and (as far as this session gets) signed off, since its whole purpose
is to summarize the state *after* the rest of this prompt's own work.

## Phase 0 — Setup

Complete the mandatory reading order above **in full this time** — do not repeat `ai-sessions/0030`'s
own disclosed scope reduction (it skipped a linear `REVERSE_ENGINEERING.md` read and the tool
SPEC/README/procedure documents to preserve turn budget for its own Phase 3 work; this prompt's own
Phase 3 below specifically depends on having read everything, so that trade-off doesn't apply here).
Create `ai-sessions/0031_MAINTENANCE_RESULT_2026_09_18.md` with the required header (`Status: partial —
resumed` to start) and an empty status table (one row per phase below). Re-confirm the decompiled APK
version on disk still matches `reverse-engineering/APK_VERSIONS.md`'s row and flag (do not
re-decompile) if it has changed. Re-run all 5 existing tools' test suites
(`.venv/bin/python3 -m pytest tests/ -v`, from each tool's own directory under
`reverse-engineering/tools/`) and confirm every one is still green before relying on or extending any of
them.

## Phase 1 — Resume `ai-sessions/0030`'s worklist

Read `ai-sessions/0030_MAINTENANCE_RESULT_2026_09_17.md`'s own "Phase 2 — Worklist" section and "What a
resumption should do next" list in full — do not rebuild the worklist from scratch, continue the one
already recorded there. In the priority order that file itself already gives, work through:

1. **The `fwe` 18-class caller search** (not just each class's own constructing method, which `0027`
   already exhausted — check each of the 18 unattributed classes' own *callers*, via
   `structural_index refs`, and/or read `fwe.b()`/`fwk`'s surrounding call sites more broadly for any
   label these classes might pick up secondhand).
2. **The `qjn`/`qjt`/`qhx`/`qjv` field-matching pass** — match the 60 actually-present fields across
   these 4 oneof groups against `PROTOCOL.md` §4.5.1–§4.5.8's already-HYPOTHESIS individual-settings
   list, per `REVERSE_ENGINEERING.md`'s own `qjn`/`qjt` entry's explicit note that this was "a natural
   next step... not attempted." Remember this entry's own standing HYPOTHESIS that these 4 groups
   belong to a *different* product, not the Buds Pro 2 — do not silently treat a field match here as
   confirming a Buds-Pro-2 feature without flagging that tension.
3. **`qhr` field 5's write-side call site** (`0030` only checked the read side, `fxb.java` case 5, and
   found it dispatches to non-self-describing generic `gea` interface methods — the write side, in
   `fyo.java`, was explicitly not checked; a different angle that might still resolve this field).
4. **The "Feature A"/`dcservice` same-key question** — whether `BluetoothPriorityReceiver`'s forwarded
   `SetFeatureState` call and `esk` discriminator 18's `GetFeatureState` call use the *same* feature-state
   key within `dcservice.BluetoothApiService`.

For each: trace it per `AGENTS.md` §13.6's zero-creativity discipline, one level further than `0030`
left it. Record the resolution (or an honest narrowing/negative, exactly as `0030`/`0027` already
modeled) in `REVERSE_ENGINEERING.md`/`PROTOCOL.md` §6 as appropriate, each labeled `awaiting maintainer
sign-off` if it touches a protocol-behavior claim. This phase is bounded in depth per item (trace one
level, then move to the next), not required to fully close every item.

## Phase 2 — `TODO.md` Phase 3 (Protocol reconstruction)

Read `TODO.md`'s own "Phase 3 — Protocol reconstruction" section in full and confirm, independently
(per `PROJECT_RULES.md` §1 — do not take this prompt's own characterization on faith), which items are
actually still open. As of `ai-sessions/0030`'s own reading, most of this section is already closed;
the two genuinely open threads appear to be:

1. **Connection-lifecycle steps 4/6** (`PROTOCOL.md` §5.2's own "still open" note) — the Message
   Stream/`libmaestro` handshake's own internal content ordering beyond channel-open timing (step 4),
   and user-triggered-command timing (step 6). Check whether either is actually resolvable from
   **existing** capture data already on disk (a re-analysis pass, per this prompt's own
   static-analysis/existing-evidence scope — no new capture) before concluding it needs a fresh
   capture.
2. **Battery Option A** (the Fast Pair BLE Battery Notification) reaching full 🟢 FACT status — already
   documented as capture-blocked (needs `CAP-054`, not yet run) — confirm this is still accurate and,
   if so, classify it as out of this prompt's own scope (no new capture) rather than attempting it.

If this independent check finds `TODO.md`'s own Phase 3 section is *itself* stale in some other way
(mirroring what `ai-sessions/0030` found for "Recommended priority order"), fix it directly, the same
way `0030` did — a plain factual correction against already-recorded evidence, not a new finding
requiring sign-off (`PROJECT_RULES.md` §1).

## Phase 3 — Full document cross-check and consistency pass

Go through every project document (the full list is the Phase 0 reading order above, plus anything else
under the repo root that isn't a per-capture file) and check for:

- **Staleness** — a claim, status marker, or "current next step"-style sentence that's been overtaken by
  later work elsewhere in the repo (exactly the class of bug `ai-sessions/0030` found and fixed once
  already in `TODO.md` — check whether any *other* document has the same kind of stale forward-pointer).
- **Contradictions** — two documents (or two sections of the same document) making incompatible claims
  about the same fact. Per `PROJECT_RULES.md` §1/§3: flag, do not silently resolve, any contradiction
  that touches a FACT-level claim or a `DECISIONS.md` ADR — a documentation-only inconsistency (e.g. a
  broken cross-reference, a stale section-number pointer) can be fixed directly.
- **Broken/unqualified cross-references** — run `python3 scripts/lint_docs.py` early in this phase (not
  only at the end) to get a current baseline of dead filename references, unregistered ID references,
  and footer issues; distinguish pre-existing noise (already flagged by `0030` as "not this session's
  responsibility" — e.g. capture-folder dead image references, `.venv`-internal package docs) from
  anything this session's own Phase 1/2 edits introduce.
- **Undocumented status drift** — anything marked `[ ]`/open in `TODO.md` that's actually done elsewhere
  (and vice versa: anything marked done that a closer read shows is only partially done, mirroring the
  EQ field-16/18 "closed by omission" pattern `ai-sessions/0027` itself caught and corrected once).
- **`id_registry.csv` consistency** — cross-check `lint_docs.py`'s "Unregistered ID references" output
  (`ANC-005`, `CASE-009` were already flagged before this session started) against
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md`/`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s own Test-ID tables — register any
  genuinely-new, correctly-used ID; flag (don't silently guess) anything that looks like an actual typo
  or reused number.

Fix what's safe to fix directly (documentation-accuracy corrections, broken cross-references, stale
forward-pointers to already-closed items). Flag, list, and leave for Phase 4 anything that constitutes a
new or re-characterized protocol/architecture claim rather than a plain correction. Record every fix
made in this phase in the RESULT file's own Phase 3 section, file+line cited, so it's auditable — do not
silently touch a document without a record of what changed and why.

Run `python3 scripts/lint_docs.py` and `./scripts/ensure_footers.py` again at the **end** of this phase
too (not only once) — confirm this phase's own edits didn't introduce anything new.

## Phase 4 — Compile and collect every pending maintainer sign-off, presented individually

This is the phase the maintainer specifically asked for an interactive, per-item structure on — do not
compress it into a single bundled summary.

1. **Build the list systematically, don't rely on memory of what's pending.** Sweep
   `PROTOCOL.md`, `REVERSE_ENGINEERING.md`, `TODO.md`, and every `ai-sessions/*_RESULT_*.md` file for
   markers like `awaiting maintainer sign-off`, `PROPOSAL —`, `pending maintainer review`, `not yet
   reviewed by maintainer`, `proposed for maintainer`, `proposal awaiting sign-off` (a starting grep:
   `grep -rn "awaiting maintainer sign-off\|PROPOSAL —\|pending maintainer review\|not yet reviewed by
   maintainer\|proposed for maintainer\|proposal awaiting sign-off" PROTOCOL.md REVERSE_ENGINEERING.md
   TODO.md DECISIONS.md ai-sessions/` — 25 raw hits as of 2026-09-17, before de-duplication and before
   this prompt's own Phase 1/2/3 additions; treat that count as a floor, not a target). De-duplicate:
   several hits will describe the *same* underlying finding restated in more than one document. Also
   check `ai-sessions/INDEX.md` for any session whose `Status` column still reads `awaiting maintainer
   sign-off` (as of 2026-09-17: at least `0017`, `0021`, `0022` — verify this list is still accurate,
   since some may have been quietly closed by a later session's own sign-off sweep without updating
   their own `Status` field, exactly the kind of status-drift Phase 3 above is meant to catch).
2. **Include this session's own new findings** — anything Phase 1/2/3 above produced or re-characterized
   at HYPOTHESIS level or above belongs on this same list, not a separate one.
3. **For each distinct item**, write a short (2–4 sentence), plain-language summary: what the finding
   claims, what evidence supports it, and where it lives (file + section, or `REVERSE_ENGINEERING.md`
   entry name). Follow the format `ai-sessions/0026`/`0028`/`0029` already established: split into
   clearly labeled groups where that helps (e.g. "code-level findings" vs. "HYPOTHESIS/OPEN QUESTION
   items being left as-is, not asked to promote" — `0028`'s own list A/B split is a good template), and
   present the **whole list** to the maintainer in this chat session, in a form they can respond to
   per item (approve/decline/ask a question) — do not ask for one omnibus "approve everything" decision
   unless the maintainer themselves chooses to respond that way.
4. **Record the maintainer's actual response, per item**, in the RESULT file — which were approved
   (and folded into `PROTOCOL.md`/`DECISIONS.md` as appropriate, per `AGENTS.md` §6's requirement that
   the agent still does the mechanical promotion after sign-off, not before), which were declined (and
   why, if given), and which are still pending because the maintainer didn't reach them in this session
   (a legitimate, expected outcome for a list this size — do not pressure a single-session resolution).
5. **Only promote to 🟢 FACT, or write/alter a `DECISIONS.md` ADR, for items the maintainer explicitly
   approved in this session** — per `AGENTS.md` §6/§15, unchanged by this prompt.

## Phase 5 — v1-readiness overview

Once Phases 1–4 have run (even if Phase 4 didn't reach every item — work with whatever state exists at
this point), produce a single, clearly organized overview answering the maintainer's own framing
question: **what still needs attention before starting v1 app development in earnest?** Structure it
as:

- **Blocking, needs a maintainer decision** — e.g. any remaining open architecture question
  (`ARCHITECTURE.md` §15), any Phase 4 item left pending that itself blocks an implementation gate.
- **Blocking, needs a new Bluetooth capture** — cross-reference `TODO.md`'s own capture queue
  (`CAP-053`–`CAP-057` and anything Phase 2/3 above surfaced as capture-dependent) against
  `ARCHITECTURE.md` §5's per-channel/per-command implementation gates, specifically calling out which
  gaps actually block a v1-scoped feature (`PROJECT.md`'s "Definition of done") versus which are
  research-interesting but not implementation-blocking.
- **Not blocking, but worth the maintainer's awareness** — genuinely open research threads (the
  DLCI 0x08 identity question, the various still-open Behavior-section items in `PROTOCOL.md` §6) that
  don't block any v1 feature per `PROJECT.md`'s own scope, so they can be consciously deferred rather
  than silently forgotten.
- **Already clear to build against** — the features/commands already 🟢 FACT and
  implementation-unblocked (ANC, Find My Buds Left/Right, EQ, Battery via HFP, per `DECISIONS.md`
  ADR-009/ADR-011/ADR-020/ADR-015/ADR-023) — stated plainly so the maintainer has the positive list, not
  only the outstanding-items list.

This overview is the deliverable Phase 5 exists to produce — write it both into the RESULT file and as
a direct message to the maintainer in this chat session, not buried inside a phase-status table.

## Phase N — Wrap-up

- Update `REVERSE_ENGINEERING.md`, `PROTOCOL.md` §6, `TODO.md`, `DECISIONS.md` (only for
  maintainer-approved promotions), and `ai-sessions/INDEX.md` to reflect everything this session
  actually resolved or changed.
- Run `python3 scripts/lint_docs.py` and `./scripts/ensure_footers.py` one final time; fix anything this
  session's own edits introduced (pre-existing noise elsewhere remains not this session's
  responsibility, per established convention).
- Finalize `0031_MAINTENANCE_RESULT_2026_09_18.md`: `Status: complete` only if every phase (including
  Phase 4's full sign-off list) was fully worked through; `awaiting maintainer sign-off` if Phase 4 items
  remain genuinely pending the maintainer's own response; `partial — resumed` if any phase's own work is
  still queued.
- Write a final summary for the maintainer covering all five parts: what Phase 1/2 advanced, what
  Phase 3 fixed, the full Phase 4 sign-off outcome (approved/declined/pending, per item), and Phase 5's
  own v1-readiness overview restated concisely.

## Guardrails

- **Scoped to the companion app's own decompiled output only**
  (`reverse-engineering/apk/v1.0.955078536-10253511/`, or whatever version is actually on disk per
  Phase 0), never Google Play Services, per `DECISIONS.md` ADR-025.
- **Never independently promote a protocol-behavior finding to 🟢 FACT in `PROTOCOL.md`, and never
  write or alter a `DECISIONS.md` ADR, without explicit maintainer approval** (`AGENTS.md` §6/§15) —
  Phase 4 exists specifically to obtain that approval item-by-item; do not shortcut it by bundling.
- **No sampling, no assumptions.** A search that comes back empty is recorded as a checked negative
  with what was actually searched, not silently dropped or filled in with a guess (`AGENTS.md` §13.6).
- **No new Bluetooth capture or hardware action is performed as part of this prompt.** Anything that
  needs one is classified as out of scope in the relevant phase and left for the maintainer's own
  capture-planning decision.
- **No decompiled APK content is ever committed** anywhere in this repository.
- Phase 3's document fixes must each be individually auditable (file+line, what changed, why) in the
  RESULT file — a silent bulk edit defeats the purpose of a consistency pass.
- Phase 4's sign-off presentation must be genuinely per-item, not a single "approve all" ask, unless the
  maintainer themselves chooses to respond that way once they see the list.

## Output

At each phase boundary, a short note (in the RESULT file, and to the maintainer if the chat is still
live) of what was found/changed. Phase 4's own output is the structured, per-item sign-off list itself,
presented directly in chat. Phase 5's own output is the v1-readiness overview, presented directly in
chat. At Phase N, the full wrap-up summary described there.

---

**Original maintainer instruction (Dutch, preserved verbatim for exact phrasing):**

> maak een nieuwe prompt in ai-sessions/ volgens de project standaard die: 1) continue the worklist next
> sessions 2) aan de gang gaat met Phase 3 uit de TODO.md — Protocol reconstruction. 3) een cross check
> en consistency check doet op alle documenten. En die verder zorgt dat alle documenten weer helemaal
> actueel zijn. 4) die mij een sign-off laat doen van alle punten die nog een sign-off nodig hebben,
> waar ik voor elk punt een samenvatting krijg, zodat ik akkoord kan geven. 5) een duidelijk overzicht
> geeft van alle punten waar ik mee verder moet, voordat ik klaar ben om aan de bouw van een v1 app te
> beginnen.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0031_MAINTENANCE_PROMPT_2026_09_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0031_MAINTENANCE_PROMPT_2026_09_18
