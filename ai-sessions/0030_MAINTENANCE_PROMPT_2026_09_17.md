# 0030_MAINTENANCE_PROMPT_2026_09_17.md — Verify/fix `TODO.md`'s stale priority-order claim, then advance as many open static-analysis-tractable leads as possible

**Number:** 0030
**Category:** MAINTENANCE
**Date:** 2026-09-17
**Title:** Verify whether `TODO.md`'s "Recommended priority order" section's claimed next step (the `MaestroDeviceSettingsProviderService` 6-case-ID→`qhr`-field trace) is still actually open — it appears, from this prompt's own authoring-session research, to already be fully closed since 2026-09-08 — correct `TODO.md` if so, then build and work through a worklist of every other genuinely still-open, non-capture-dependent lead across `TODO.md`/`PROTOCOL.md`/`REVERSE_ENGINEERING.md`/`DECISIONS.md`

---

## How to (re)start this prompt — read this paragraph first, every time

This task is designed to be pasted verbatim into a **new Claude Code chat**, including on a
resumption. Before anything else: check whether `ai-sessions/0030_MAINTENANCE_RESULT_2026_09_17.md`
already exists.

- **It does not exist** → this is the first run. Start at Phase 0.
- **It already exists** → read it in full. Its per-phase/per-worklist-item status table (created in
  Phase 0/2) says what's already `done`. Skip straight to the first item not yet marked `done` and
  continue from there.

Every phase or worklist-item boundary is a safe stopping point: before ending a turn, update
`0030_MAINTENANCE_RESULT_2026_09_17.md`'s status table and `Status` header field
(`partial — resumed` while incomplete) so the next session picks up cleanly. This is an intentionally
open-ended, resumable task — per the maintainer's own instruction below, work through as many worklist
items as time allows in a single sitting; do not force everything into one turn.

## Mandatory reading order (do this first, in this order, in every session that works on this prompt)

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8:

1. `AGENTS.md` (full) — especially §4/§6 (mechanical-assistance boundary; never independently promote a
   FACT or write a `DECISIONS.md` ADR) and §13.6 (zero-creativity, evidence-only rule).
2. `PROJECT.md`, `PROJECT_RULES.md` (full), `ARCHITECTURE.md` (full, including §15's open architecture
   questions).
3. `DECISIONS.md` — full ADR history.
4. `PROTOCOL.md` **§6 in full, start to finish** — this is the authoritative, single-source-of-truth
   open-questions list per `TODO.md`'s own "Open questions" section (which explicitly says protocol
   -level open questions live here, not duplicated in `TODO.md`).
5. `TODO.md` in full — especially the "Recommended priority order" section (top) and the "Targeted
   research follow-ups"/Phase 2 sections, which is where this prompt's own Phase 1 verification starts.
6. `REVERSE_ENGINEERING.md` **in full, start to finish** — pay specific attention to: the "Candidate
   rich schemas outside this pass's traced call graph" section's newest dated update (18 of `fwe`'s 24
   sub-message classes plus `ncs`/`ndm`/`ncx`/`nch`'s own field content, explicitly left open); the
   `MaestroEndpointService` entry's newest dated update (what happens when `ofm.a()`'s dispatch finds no
   registered service, explicitly left open); and the `qjn`/`qjt`/`qhx`/`qjv` entry (a standing,
   never-fully-settled HYPOTHESIS that this schema belongs to a different product, not the Buds Pro 2).
7. All 5 existing tools' own SPEC and README files in full, under `reverse-engineering/tools/`:
   `lambda_dispatcher_resolver/`, `structural_index/` (including its own `implements`/`field-writes`
   v1.1 additions), `schema_batch_extractor/`, `uuid_ble_context/`, `limited_dataflow/`.
8. `reverse-engineering/tools/BACKLOG.md` in full.
9. `APK_REVERSE_ENGINEERING_PROCEDURE.md` in full.
10. `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md`.
11. `ai-sessions/0027_MAINTENANCE_RESULT_2026_09_17.md` and `ai-sessions/0028_MAINTENANCE_RESULT_2026_09_17.md`
    in full — the immediately preceding sessions this one continues from. Also check whether
    `ai-sessions/0029_MAINTENANCE_RESULT_2026_09_17.md` exists and, if so, read it too (it records
    sign-off on three of this session's own input findings from item H/B above).

## Context (from the maintainer, not re-derived here)

The maintainer asked, in their own words (translated from Dutch, preserved verbatim below the
horizontal rule at the end of this prompt), for a prompt that picks up the next item from `TODO.md`'s
own "Recommended priority order" section — named there as the `MaestroDeviceSettingsProviderService`
6-case-ID→`qhr`-field forward trace — and, beyond that one item specifically, tries to advance as many
open items from across the project's documents as possible.

**A note from this prompt's own authoring session, to be verified (not assumed) by whoever executes
this prompt:** while drafting this prompt, a check of `TODO.md`'s own "Targeted research follow-ups"
section and Phase 2 checklist found that the `MaestroDeviceSettingsProviderService` 6-case-ID trace
**appears to already be fully closed**, and has been since **2026-09-08**
(`ai-sessions/0003_MAINTENANCE_RESULT_2026_09_08.md` Phase 3 item 1; `DECISIONS.md` ADR-019 Update;
`REVERSE_ENGINEERING.md`'s own `MaestroDeviceSettingsProviderService` entry) — all 6 case IDs
(`2102`→`qhr` field 2, `2103`→field 27, `2104`→field 11, `2113`→field 5, `2115`→no `qhr` field/a
separate "Feature A" toggle, `2116`→field 32) are traced and checked off (`[x]`) in `TODO.md`'s own
Phase 2 section. The *other two* open APK-RE leads that same "Recommended priority order" paragraph
names as needing "an untried smali read or a fresh capture first" — `MaestroEndpointService`'s smali
fallback read, and `gjv.p()`'s caller trace — are **also** both already closed (the former as of
2026-09-08, deepened further on 2026-09-17 by `ai-sessions/0027`'s own item-B continuations; the latter
as of 2026-09-15, `ai-sessions/0023`). **The entire "Recommended priority order" item 3 paragraph in
`TODO.md` appears to be stale**, written once on 2026-08-23/updated 2026-09-08 and never revisited after
all three of the leads it names were closed on or shortly after that same date. Phase 1 below is this
prompt's own instruction to *verify* this independently (do not simply trust this prompt's own
authoring-session claim — re-derive it from `TODO.md`'s current text and the cited evidence) before
acting on it, per `AGENTS.md` §13.6's zero-creativity rule; if confirmed, correct `TODO.md`'s stale text
rather than spend a full phase re-doing already-finished work.

## Phase 0 — Setup

Complete the mandatory reading order above. Create `ai-sessions/0030_MAINTENANCE_RESULT_2026_09_17.md`
with the required header (`Status: partial — resumed` to start) and an empty status table (one row per
phase below, plus one row per worklist item once Phase 2 builds it). Re-confirm the decompiled APK
version on disk still matches `reverse-engineering/APK_VERSIONS.md`'s row
(`v1.0.955078536-10253511`) — flag and do not re-decompile if it has changed. Re-run all 5 existing
tools' test suites (`.venv/bin/python3 -m pytest tests/ -v`, from each tool's own directory under
`reverse-engineering/tools/`) and confirm every one is still green before relying on or extending any
of them.

## Phase 1 — Verify (or refute) the staleness claim above, and fix `TODO.md` if confirmed

1. Independently re-check: is the `MaestroDeviceSettingsProviderService` 6-case-ID trace, and the two
   other leads `TODO.md`'s "Recommended priority order" item 3 names, actually still open, or already
   closed? Cite the specific `TODO.md`/`REVERSE_ENGINEERING.md`/`DECISIONS.md` lines that settle this
   either way — do not take this prompt's own claim on faith.
2. **If confirmed closed** (as this prompt's authoring session found): edit `TODO.md`'s "Recommended
   priority order" section's item 3 paragraph to reflect current reality — either remove the
   now-resolved "current highest-leverage single next step" sentence entirely, or replace it with
   whatever the actual next-cheapest open APK-RE lead is once Phase 2's worklist exists (may require
   writing Phase 2 first and coming back to this edit — order these however makes sense, but do not
   leave stale, already-contradicted text in place either way). This is a documentation-accuracy fix,
   not a finding requiring maintainer sign-off (`PROJECT_RULES.md` §1 — a plain factual correction
   against already-recorded, already-approved evidence is not a new claim).
3. **If genuinely still open** (this prompt's own claim was wrong): say so plainly, explain what this
   prompt's authoring session missed, and proceed to trace it properly as this session's own next step
   before moving to Phase 2.

## Phase 2 — Build a worklist of genuinely open, static-analysis-tractable leads

Enumerate every currently-open (`[ ]` or otherwise explicitly unresolved) item across:

- `PROTOCOL.md` §6 (the authoritative open-questions list per `TODO.md`'s own pointer) — full sweep,
  not a sample.
- `TODO.md`'s Phase 2 (APK reverse engineering) section and "Targeted research follow-ups" subsection.
- `ARCHITECTURE.md` §15 (open architecture questions).
- `REVERSE_ENGINEERING.md`'s own explicitly-flagged-still-open threads, at minimum these four already
  identified by this prompt's authoring session (seed the worklist with these, then look for more):
  1. **Item H's 4th pool residue** — 18 of `fwe.java`'s 24 private sub-message parser classes
     (`nec`/`nee`/`ncu`/`nby`/`ncg`/`nck`/`ncl`/`ncm`/`ncv`/`ndb`/`nde`/`ndn`/`ndo`/`ndq`/`ndu`/`ndw`/
     `ndy`/`neb`) plus `ncs`/`ndm`/`ncx`/`nch`'s own field-level content carry no self-describing
     exception/log text in their own constructing method (already checked once) — a further pass could
     try each one's own *callers* (not just its constructing method) via `structural_index refs`, or
     read the surrounding `fwe.b()`/`fwk` call sites more broadly for any label these 18 classes might
     pick up secondhand.
  2. **Item B's own deeper residue** — what happens when an authorized Binder call reaches `ofm.a()`'s
     own dispatch logic looking for a matching registered service, given the service map is confirmed
     empty (`REVERSE_ENGINEERING.md`'s `MaestroEndpointService` entry, newest update). This needs
     reading further into `ofm.a(int, Parcel)`'s own body past what has already been read.
  3. **The `qjn`/`qjt`/`qhx`/`qjv` "different product" hypothesis** — re-examine this standing,
     never-fully-settled HYPOTHESIS in light of today's `presto_mr1`/`fpz` findings (does the new
     "device type" evidence sharpen or weaken the "different product" reading? See
     `REVERSE_ENGINEERING.md`'s own cross-reference note on this entry for where the two threads
     already touch).
  4. Any `PROTOCOL.md` §6 item whose own text is resolvable by reading the companion app's decompiled
     source further (as opposed to needing a new Bluetooth capture, a maintainer product decision, or
     external spec research) — e.g. "Protobuf/message mapping for all features listed in §4.5" and
     similar broad, not-yet-fully-swept items. Read §6 in full per the mandatory reading order above and
     identify these directly; do not assume this list is exhaustive.

For each candidate, classify it into exactly one of:

- **Static-analysis-tractable** (this prompt's own scope — decompiled-APK-only, no hardware needed).
- **Needs a new Bluetooth capture or hardware action** — out of scope for this prompt; note it and move
  on, per this project's own standing rule that new captures need a maintainer decision, not an
  AI-agent's own initiative.
- **Needs a maintainer product/scope decision** — out of scope for this prompt for the same reason.

Record the full worklist (with its own classification) in
`ai-sessions/0030_MAINTENANCE_RESULT_2026_09_17.md`'s status table before starting Phase 3, so a
resumption has a clear, complete picture of what's queued.

## Phase 3+ — Work through the worklist

Work through the static-analysis-tractable items from Phase 2, cheapest/highest-confidence-of-progress
first, using the existing tool suite (`structural_index`, `lambda_dispatcher_resolver`,
`schema_batch_extractor`, `uuid_ble_context`, `limited_dataflow`) wherever applicable rather than manual
`grep`-only passes, per this project's own established preference (`ai-sessions/0025`'s adoption).

For each item: trace it, per `AGENTS.md` §13.6's zero-creativity discipline (byte offsets/field
meanings/class attributions come only from actual bytes, code, or a documented spec/APK reference —
never a plausible-sounding guess), one level further than its current state. If it resolves: record the
resolution, file+line cited, in the document where that open item is tracked (`REVERSE_ENGINEERING.md`
for code-level findings, `PROTOCOL.md` §6 for protocol-level ones — proposed and labeled `awaiting
maintainer sign-off` if it would touch a protocol-behavior claim, not silently promoted). If it doesn't
resolve, or only partially narrows: record that honestly too, exactly as `ai-sessions/0027`'s own
continuations already modeled repeatedly (a checked negative, or an honestly-narrowed-but-still-open
result, is a real, useful, recordable outcome — not a failure to hide).

This phase is intentionally open-ended in scope but bounded in depth per item (trace one level, then
move to the next worklist item rather than exhausting one item across an entire session) — the
maintainer's own instruction is to advance **as many** items as reasonably possible, not to fully close
any single one at all costs. Update the status table after each item, so a mid-session interruption
(rate limit, turn boundary) leaves a clean resumption point.

## Phase N — Cross-checks and wrap-up

- Cross-reference every finding from Phase 3+ against `PROTOCOL.md`'s full text, `DECISIONS.md`'s full
  ADR history, `DESKRESEARCH_FINDINGS.md`, and any `CAP-NNN-FINDINGS.md` files that actually exist — flag,
  do not silently resolve, any contradiction found.
- Update `REVERSE_ENGINEERING.md`, `PROTOCOL.md` §6, `TODO.md` (closing out anything this session
  actually resolved, in addition to Phase 1's own stale-text fix), and `ai-sessions/INDEX.md`.
- Run `python3 scripts/lint_docs.py` and `./scripts/ensure_footers.py`; fix anything this session's own
  edits introduce (pre-existing noise elsewhere is not this session's responsibility).
- Finalize `0030_MAINTENANCE_RESULT_2026_09_17.md`: `Status: complete` only if the worklist is fully
  worked through and nothing is pending review; `awaiting maintainer sign-off` if any protocol-level
  proposal is pending; `partial — resumed` if worklist items remain queued.
- Write a final summary for the maintainer: what `TODO.md`'s stale-text check found, the full worklist
  with each item's outcome (resolved/narrowed/still open/out of scope), and what remains for a future
  session.

## Guardrails

- **Scoped to the companion app's own decompiled output only**
  (`reverse-engineering/apk/v1.0.955078536-10253511/`, or whatever version is actually on disk per
  Phase 0) — never Google Play Services, per `DECISIONS.md` ADR-025.
- **Never independently promote a protocol-behavior finding to 🟢 FACT in `PROTOCOL.md`, and never
  write or alter a `DECISIONS.md` ADR, without explicit maintainer approval** (`AGENTS.md` §6/§15).
- **No sampling, no assumptions.** A search that comes back empty is recorded as a checked negative
  with what was actually searched, not silently dropped or filled in with a guess (`AGENTS.md` §13.6).
- **No new Bluetooth capture or hardware action is performed as part of this prompt.** Any worklist item
  that needs one is classified as out of scope in Phase 2 and left for the maintainer's own
  capture-planning decision, not attempted or worked around.
- **No decompiled APK content is ever committed** anywhere in this repository — any new/extended tool
  code, tests, or fixtures read from the maintainer's local, gitignored `reverse-engineering/apk/<version>/`
  tree and skip themselves when it's absent.
- Every proposed protocol-level finding is labeled `awaiting maintainer sign-off`; mechanical code-facts
  may be recorded as 🟢 FACT on their own terms, per this project's existing practice.

## Output

At each worklist-item boundary, a short note (in the RESULT file, and to the maintainer if the chat is
still live) of what was found/changed. At Phase N, the full wrap-up summary described there.

---

**Original maintainer instruction (Dutch, preserved verbatim for exact phrasing):**

> 2) Verder werk oppakken — volgens TODO.md's prioriteitsvolgorde is de goedkoopste, hoogste-hefboom
> volgende stap de MaestroDeviceSettingsProviderService 6-case-ID→qhr-field trace (nog open APK-RE
> lead). probeer zoveel mogelijk openstaande punten uit de verschillende documenten verder te
> onderzoeken.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0030_MAINTENANCE_PROMPT_2026_09_17.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0030_MAINTENANCE_PROMPT_2026_09_17
