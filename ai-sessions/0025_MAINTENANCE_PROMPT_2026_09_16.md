# 0025_MAINTENANCE_PROMPT_2026_09_16.md — Implement `ai-sessions/0024`'s top-4 `reverse-engineering/tools/BACKLOG.md` tools and workflow recommendations, continue APK reverse engineering with them, and cross-reference the results

**Number:** 0025
**Category:** MAINTENANCE
**Date:** 2026-09-16
**Title:** Implement the top 4 `reverse-engineering/tools/BACKLOG.md` items and the workflow amendments `ai-sessions/0024` proposed, use them to advance real APK reverse engineering, cross-reference the results against `CAP-NNN-FINDINGS.md`/`PROTOCOL.md`/`REVERSE_ENGINEERING.md`/`DESKRESEARCH_FINDINGS.md`/`TODO.md`, run project-wide consistency checks, and update every affected document

---

## How to (re)start this prompt — read this paragraph first, every time

This task is designed to be pasted verbatim into a **new Claude Code chat**, including on a
resumption, and is expected to legitimately span more than one session given its scope (building up
to four analysis tools, then using them for real APK-RE work). Before anything else: check whether
`ai-sessions/0025_MAINTENANCE_RESULT_2026_09_16.md` already exists.

- **It does not exist** → this is the first run. Start at Phase 0.
- **It already exists** → read it in full. Its per-phase status table (created in Phase 0) says
  which phases are already `done`. Skip straight to the first phase not yet marked `done` and
  continue from there — do not redo a `done` phase's work, and do not re-implement a tool this
  file's own record shows already built and tested. Regardless of where you resume, you still owe
  the full "Mandatory reading order" below in this session — `AI_SESSION_LOG_PROCEDURE.md` §8
  requires it for every session that acts on this repo, resumption or not, since a new chat has no
  memory of a previous one.

Every phase boundary, and every individual tool inside Phase 1, is a safe stopping point: before
ending a turn, update `0025_MAINTENANCE_RESULT_2026_09_16.md`'s status table and `Status` header
field (`partial — resumed` while incomplete) so the next session picks up cleanly. Given the size of
this task, expect to stop partway through and resume — that is the normal outcome here, not a
failure, per `AI_SESSION_LOG_PROCEDURE.md` §5's multi-part-result convention.

## Mandatory reading order (do this first, in this order, in every session that works on this prompt)

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8:

1. `AGENTS.md` (full) — especially §4/§6 (the mechanical-assistance boundary, `DECISIONS.md`
   ADR-017; the per-channel 🟢 FACT gate; the rule that an AI must never independently promote a
   FACT or write a `DECISIONS.md` ADR) and §13.6 (zero-creativity, evidence-only rule).
2. `PROJECT.md`, `PROJECT_RULES.md` (full), `ARCHITECTURE.md` (full).
3. `DECISIONS.md` — full ADR history. At minimum, read ADR-017 (AI-mechanical-assistance boundary),
   ADR-019 (the two field-tracing directions this project already uses, now a named workflow — see
   Phase 2 below), and ADR-025 (GMS reverse-engineering out of scope) in full.
4. `PROTOCOL.md` §6 in full (every open item), and skim the rest for context on anything Phase 3/4
   below touches.
5. `TODO.md` in full, especially Phase 2 (APK reverse engineering, including its 2026-09-16
   `ai-sessions/0024` update) and Phase 3.
6. `REVERSE_ENGINEERING.md` **in full, start to finish** — this project's entire accumulated APK
   knowledge. Pay special attention to the `qhr`/`fye` entry (including its `aie` addendum) and the
   new `esk` entry `ai-sessions/0024` added, the "Candidate rich schemas outside this pass's traced
   call graph" section, and the "Known limitations of this analysis" section.
7. `reverse-engineering/tools/lambda_dispatcher_resolver/SPEC.md` and `README.md` in full — the
   existing tool, and the template every new tool in Phase 1 below follows.
8. `reverse-engineering/tools/BACKLOG.md` in full, **including its 2026-09-16 `ai-sessions/0024`
   proposed-reordering blockquote and the structural-code-index sketch** — the direct input to
   Phase 1.
9. `APK_REVERSE_ENGINEERING_PROCEDURE.md` in full, **including its §4a and its "Notes & Gotchas"
   additions from `ai-sessions/0024`** — the direct input to Phase 2/3.
10. `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md`.
11. `ai-sessions/0024_AUDIT_RESULT_2026_09_16.md` **in full** — the immediate predecessor this
    prompt executes on. Its Phase 2 (open-question inventory, items A–M), Phase 3 (per-item
    `reverse-engineering/tools/BACKLOG.md` assessment and priority proposal), and Phase 4 (workflow
    proposals) are this prompt's direct inputs; do not re-derive them from scratch.
12. `DESKRESEARCH_FINDINGS.md` in full — one of Phase 4's five cross-reference targets, not
    previously read by the `0024` session.
13. `id_registry.csv` and `captures/` — specifically, determine which `CAP-NNN` directories actually
    have a completed `CAP-NNN-FINDINGS.md` versus a placeholder/not-yet-captured skeleton (several
    exist as `CAP-0NN-yyyy-MM-dd_HH-mm-ss...` template directories for future sessions — e.g.
    `CAP-030`, `CAP-052` through `CAP-057` at the time this prompt was written). Phase 4 below only
    matches against captures that actually have findings recorded; do not treat an empty placeholder
    as a checked negative.

## Context (from the maintainer, not re-derived here)

The maintainer asked, in their own words (translated from Dutch, preserved in full below the
horizontal rule at the end of this prompt), to create a prompt in `ai-sessions/` that, working from
`ai-sessions/0024_AUDIT_RESULT_2026_09_16.md`:

1. First implements the top 4 recommendations regarding the `reverse-engineering/tools/BACKLOG.md`
   items.
2. Adopts the recommendations regarding workflows.
3. Further executes APK reverse engineering using the new scripts and workflows.
4. Tries to match the reverse-engineering findings against `CAP-NNN-FINDINGS.md`, `PROTOCOL.md`,
   `REVERSE_ENGINEERING.md`, `DESKRESEARCH_FINDINGS.md`, and `TODO.md`.
5. Runs cross-checks and consistency checks.
6. Updates all documents.

**What "top 4" means, concretely.** `ai-sessions/0024`'s Phase 3.2 proposed this order (applied
in-place to `reverse-engineering/tools/BACKLOG.md` as a proposal): **(1)** general
androguard-based structural/XREF code index, **(2)** protobuf/`RawMessageInfo` schema
batch-extractor, **(3)** UUID extraction + BLE/GATT context reconstruction, **(4)** limited
dataflow analysis — followed by 3 further, independently-useful, unordered items (wire-payload
decoder, APK version-diff tool, tshark/DLCI-reassignment helper) that are **not** part of this
prompt's "top 4" and are explicitly out of scope for Phase 1 below.

**On authorization to proceed.** This prompt's own existence — the maintainer's instruction quoted
above, asking to *implement* (not merely re-discuss) `ai-sessions/0024`'s tooling-priority and
workflow proposals — is the explicit maintainer decision `reverse-engineering/tools/BACKLOG.md` §0's
own header requires before an idea "graduates out of this file by getting its own spec document."
It is **also** the explicit sign-off event `AI_SESSION_LOG_PROCEDURE.md` §4a requires to move
`reverse-engineering/tools/BACKLOG.md`'s and `APK_REVERSE_ENGINEERING_PROCEDURE.md`'s
"awaiting maintainer sign-off" tooling/workflow-process proposals to adopted — cite *this prompt* as
that event when updating those two files in Phase 2. **This authorization is scoped to tooling
priority and workflow process only** (`AI_SESSION_LOG_PROCEDURE.md`'s own text: this class of
convention "does not go through the FACT/HYPOTHESIS/ADR sign-off gate"). It does **not** extend to
any specific protocol claim: promoting anything to 🟢 FACT in `PROTOCOL.md`, or writing/superseding
a `DECISIONS.md` ADR, still needs its own separate, explicit maintainer sign-off, per `AGENTS.md`
§6/§15 — nothing in this prompt changes that gate. Do not read "the maintainer authorized this
session" as blanket approval for anything beyond the tooling/workflow scope stated here.

## Session bookkeeping & resumability

In Phase 0, create `ai-sessions/0025_MAINTENANCE_RESULT_2026_09_16.md` with the header block
required by `AI_SESSION_LOG_PROCEDURE.md` §4 (`Status: partial — resumed` to start) and a status
table with one row per phase (0–6, see below), plus a **sub-table inside Phase 1's own row** tracking
each of the 4 tools independently (not built / spec drafted / implemented / tested / used), since
Phase 1 alone is expected to be the majority of this task's total effort. This prompt does not
instruct automatic git commits — mention clearly, at the end of each session's final turn, what is
uncommitted.

## Phase 0 — Setup

Complete the mandatory reading order above. Create `ai-sessions/0025_MAINTENANCE_RESULT_2026_09_16.md`
with the required header and an empty phase-status table (including the Phase 1 tool sub-table).
Re-confirm the decompiled APK version on disk still matches `reverse-engineering/APK_VERSIONS.md`'s
row (`v1.0.955078536-10253511`, per `ai-sessions/0024`'s own Phase 0 finding — if a newer version has
since been pulled, flag it explicitly as a maintainer decision and do not re-decompile as part of
this prompt). Re-run `lambda_dispatcher_resolver`'s own test suite
(`.venv/bin/python3 -m pytest tests/ -v`, from `reverse-engineering/tools/lambda_dispatcher_resolver/`)
and confirm it is still green before building anything on top of it.

## Phase 1 — Implement the top 4 `reverse-engineering/tools/BACKLOG.md` items, in priority order

For **each** of the 4 tools below, follow the same discipline `lambda_dispatcher_resolver` itself
used (per its own `reverse-engineering/tools/lambda_dispatcher_resolver/SPEC.md`) and the governance
`reverse-engineering/tools/BACKLOG.md`'s own
"Governance that applies to every idea below, unconditionally" section fixes for all of them:
mechanical assistance only (never decides Bluetooth-protocol relevance, never writes into
`REVERSE_ENGINEERING.md`/`PROTOCOL.md`/`DECISIONS.md`/any `CAP-NNN-FINDINGS.md`); no decompiled APK
content committed anywhere, ever, including as test fixtures (tests read from the maintainer's
local, gitignored `reverse-engineering/apk/<version>/` tree and skip themselves when it's absent);
reuse this project's FACT/HYPOTHESIS/ASSUMPTION/OPEN QUESTION vocabulary for anything a tool's output
implies, never a parallel taxonomy; test against already-confirmed findings as regression fixtures
before trusting a tool on something new.

Work through the 4 tools **in this order**, stopping at a clean tool boundary (spec drafted →
implemented → tested → used once for real) if a session runs out of room, and resuming the next tool
in a later session rather than starting several in parallel and leaving all of them half-finished:

1. **General structural/XREF code index.** Write its own `reverse-engineering/tools/structural_index/SPEC.md`
   (following `reverse-engineering/tools/lambda_dispatcher_resolver/SPEC.md`'s template and
   structure) inside a new `reverse-engineering/tools/structural_index/` directory, starting from
   `reverse-engineering/tools/BACKLOG.md`'s own "Sketch of a first, narrowest useful version" text
   (2026-09-16 addition) as the v1 scope: given a class name, list every other class/method that (a)
   constructs it, (b) calls one of its methods, or (c) holds it as a field type. Reuse
   `lambda_dispatcher_resolver`'s own Layer 1 (`androguard_index.py`) rather than re-implementing DEX
   loading from scratch. Implement, add a pytest suite (skipping gracefully when the local APK tree
   is absent, matching `reverse-engineering/tools/lambda_dispatcher_resolver/tests/`'s own
   convention), and verify against its
   own acceptance criterion (`reverse-engineering/tools/BACKLOG.md`'s sketch names three: `esk`'s 20
   constructor sites, `gjv.p()`'s sole `Lgiz;->p(` caller, and a "which of `aie`'s never-catalogued
   referenced classes have zero external references" check) — each of these three has already been
   worked out by hand in a prior session (`ai-sessions/0023`, `ai-sessions/0024`), so this is a
   genuine regression-test-against-known-truth, not a guess at what the right answer should be.
2. **Protobuf/`RawMessageInfo` schema batch-extractor.** Generalize
   `scripts/decode_rawmessageinfo.py` (which currently recovers one `GeneratedMessageLite` class's
   schema at a time — this is how `qhr`/`qjc`/`qjb` were originally recovered, per `TODO.md`'s Phase
   2 entry) into a batch mode that scans the whole decompiled tree for every class matching the same
   generated-message shape and produces a full schema register. Write its own
   `reverse-engineering/tools/schema_batch_extractor/SPEC.md`. Verify it reproduces the already-known
   `qhr`/`qjc`/`qjb`/`nqx` schemas exactly (same field count, same field-type/reference info) as its
   own acceptance test before trusting it on anything new.
3. **UUID extraction + BLE/GATT context reconstruction.** Per `reverse-engineering/tools/BACKLOG.md`'s
   own text, this is a larger-scope idea that depends on tool 1 above for its "usage location" half —
   design its own `reverse-engineering/tools/uuid_ble_context/SPEC.md` accordingly (a
   UUID/characteristic string → usage-location graph, built on top of the structural index rather than
   re-deriving XREFs itself). Given `REVERSE_ENGINEERING.md`'s own "Full-tree GATT/BLE reference
   sweep" finding (zero `BluetoothGatt` references anywhere in this APK version), this tool's first
   real run may legitimately return "no `BluetoothGatt`/UUID content found in this APK version" — a
   valid, recordable checked-negative result confirming that section's prior finding with a
   standing tool instead of only a one-off manual sweep, not a failure of the tool itself.
4. **Limited dataflow analysis.** Per `reverse-engineering/tools/BACKLOG.md`'s own explicit
   risk-scoping, build only the narrowest version: tracking a byte-array's construction
   (`new-array`/`fill-array-data`/`aput-byte`, or an equivalent protobuf-builder call chain) forward
   to a `writeCharacteristic`/`WriteSetting` call site, **scoped to straight-line, single-basic-block
   flows only** — do not attempt cross-branch or cross-loop dataflow in this first version. Write its
   own `reverse-engineering/tools/limited_dataflow/SPEC.md`, and require the same
   worked-example test discipline as the other three tools before using it on anything not already
   manually verified (the already-hand-traced `esk` discriminator 19 → `WriteSetting` chain,
   `REVERSE_ENGINEERING.md`'s `qhr`/`fye` entry, is a natural first regression fixture, per
   `ai-sessions/0024`'s own Phase 3.1 finding that this exact chain needed zero dataflow tooling to
   trace by hand — use it to confirm the tool reaches the same conclusion, not to discover something
   new).

Record, for each tool, its own entry in `TODO.md`'s Phase 2 section once built and tested (mirroring
how the `lambda_dispatcher_resolver` entry documents itself), and mark it as implemented (with a
pointer to its own SPEC.md file, at the path given above, per tool) in
`reverse-engineering/tools/BACKLOG.md`,
replacing that idea's "Idea:" heading text — but leave `reverse-engineering/tools/BACKLOG.md` itself
in place as the registry of which idea graduated when, rather than deleting the entry.

## Phase 2 — Adopt the workflow recommendations

Per the "On authorization to proceed" note in Context above, this prompt itself is the sign-off event
for the tooling-priority and workflow-process proposals `ai-sessions/0024` recorded (not for any
protocol claim). Concretely:

1. In `reverse-engineering/tools/BACKLOG.md`, update the 2026-09-16 `ai-sessions/0024`
   proposed-reordering blockquote: change its framing from "a proposal, not applied as a decision by
   this edit alone" / "a proposal for the maintainer to approve or reject" to record that it **was**
   approved, citing this prompt (`ai-sessions/0025`) as the event, per
   `AI_SESSION_LOG_PROCEDURE.md` §4a's own citation requirement. Do the same for the structural-index
   sketch's status.
2. In `APK_REVERSE_ENGINEERING_PROCEDURE.md`, update §4a's and the "Notes & Gotchas" retry-strategy
   bullet's framing the same way — these were already applied in-place as text by `ai-sessions/0024`,
   so this step is a status/citation update (record adoption), not a rewrite of the substance.
3. Update `ai-sessions/0024_AUDIT_RESULT_2026_09_16.md`'s own `Status` field from
   `awaiting maintainer sign-off` to reflect that its tooling/workflow proposals (not its protocol-
   level open questions, which remain genuinely open) have been acted on — per
   `AI_SESSION_LOG_PROCEDURE.md` §4a, this is an edit to the earlier file's header field, citing this
   prompt as where the update happened, not a new duplicate narration of its content. If any part of
   `ai-sessions/0024`'s output still has something pending beyond the tooling/workflow scope (e.g. its
   Phase 1 code-level leads, which are protocol-adjacent, not workflow), the `Status` field should
   reflect that nuance honestly rather than flipping to a blanket `complete`.

## Phase 3 — Use the new tools and adopted workflows to continue real APK reverse engineering

Using `ai-sessions/0024`'s own Phase 2 inventory (items A–M) as the worklist, and the classification
that inventory already assigned each item (tractable via a `lambda_dispatcher_resolver`-adjacent tool
vs. needing a different tool vs. capture-only):

1. **Run the zero-cost free win first, before any new tool is even needed**: `gag` is itself another
   R8-merged dispatcher (`implements Lorz;`), per `ai-sessions/0024`'s own Phase 2.3 point 4 — run
   `lambda_dispatcher_resolver resolve-all --class gag` and read every case, exactly as Phase 1 of
   `ai-sessions/0024` did for `aie`/`esk` (item C, closing it or narrowing it).
2. Once Phase 1's structural/XREF index (tool 1 above) is built, use it directly on items **B**
   (`MaestroEndpointService`'s Dagger-multibinding assembly site — find every class implementing/
   constructing its key type), **F** (whether `esk`'s default-branch `gcp`/`gcn` is the same DAO as
   the already-known `gcl`/`gck`/`eht` device_info sink, or a distinct accessor — `REVERSE_ENGINEERING.md`'s
   `esk` entry's own open question), **G** (the remaining never-traced `qhr` fields' call sites),
   **H** (the 12 never-attributed "candidate rich schemas" — find every external reference to each),
   and **J** (whether `gbb`/`gbc`'s second `gbd`-construction path generalizes).
3. Once Phase 1's schema batch-extractor (tool 2) is built, use it on item **H**'s 12 candidate rich
   schemas (`nhm`, `nef`, `qaa`, `ndi`, `mtn`, `nca`, `gdw`, `nfh`, `msw`, `qaj`, `qbu`, `qar`) to
   recover their field-level structure, complementing (not replacing) the XREF work in step 2 above.
4. Items **A**, **D**, **E**, **K** are already classified by `ai-sessions/0024` as needing a fresh
   capture, a live check, or being wire-payload-only — **do not** perform a new Bluetooth capture as
   part of this prompt (same guardrail as `ai-sessions/0024`); note their static-analysis ceiling has
   already been reached and move on. Item **L** (a full `AndroidManifest.xml` re-review) and item
   **M** (a resource/string-table sweep) are cheap, mechanical, and don't strictly need a new tool —
   do them directly if time permits, using Phase 1's structural index for M's "already referenced
   elsewhere" half if it's ready by then.
5. Every new finding follows `PROJECT_RULES.md` §1 in full: FACT/HYPOTHESIS/ASSUMPTION/OPEN QUESTION
   labeled explicitly, code-level findings from static analysis alone capped at "this code exists and
   looks like X" (never a protocol-behavior FACT without capture correlation, per
   `APK_REVERSE_ENGINEERING_PROCEDURE.md` §5 rule 2). Do not chase every tangent to full depth —
   flag what's promising, trace one level further, and record an open question for a future session
   rather than blowing this phase's own scope, exactly as `ai-sessions/0024`'s own Phase 1 guidance
   already modeled.

## Phase 4 — Match the reverse-engineering findings against the project's other records

For **every** finding this session produced in Phase 3 (and, while at it, the two findings
`ai-sessions/0024` itself already recorded but had not yet cross-matched beyond `PROTOCOL.md`/
`DECISIONS.md` — the `esk` discriminator 18 "Feature A" write site and the default-branch `gcp`/`gcn`
device_info DAO lead), check each of the following five documents explicitly and record the result
(match / contradiction / no existing mention) rather than assuming silence means agreement:

1. **`CAP-NNN-FINDINGS.md` files that actually exist** (per Phase 0's reading-order step 13 — skip
   placeholder/not-yet-captured directories). Specifically worth checking: does any existing capture's
   wire evidence already touch the "Feature A" mechanism (`REVERSE_ENGINEERING.md`'s own cases `2104`/
   `2115`/`2116` already link it to `MaestroDeviceSettingsProviderService`, but no capture has wire-
   correlated it yet, per `PROTOCOL.md` §6) or the `device_info` table (`gcl`/`gck`/`eht`'s existing
   correlation entries)?
2. **`PROTOCOL.md`** — full text, not just §6, in case a finding relates to a section this prompt's
   own reading order didn't call out by name.
3. **`REVERSE_ENGINEERING.md`** — internal self-consistency: does a new finding from Phase 3
   contradict or duplicate an existing entry under a different obfuscated name (the same risk
   `ai-sessions/0024`'s own Phase 1 flagged for `gcp`/`gcn` vs. `gcl`/`gck`/`eht`)?
4. **`DESKRESEARCH_FINDINGS.md`** — this project's external/spec-research findings; check whether any
   Phase 3 code-level lead corroborates or conflicts with something already researched there (e.g. if
   `qaj`/`qbu`/`qar`'s recovered schema shape from tool 2 resembles a publicly-documented Fast Pair or
   Pigweed message type already noted in that file).
5. **`TODO.md`** — does a Phase 3 finding close out an existing open item (e.g. one of the "Targeted
   research follow-ups"), or does it introduce a new one that needs its own bullet?

**Flag every contradiction found — do not silently resolve one.** Per `PROJECT_RULES.md` §1 rule 2
and `ai-sessions/0024`'s own Phase 5 precedent, a genuine conflict between two documents' existing
claims is maintainer-decision material, not something an AI session resolves unilaterally by picking
whichever claim looks newer or more detailed.

## Phase 5 — Cross-checks and consistency checks

1. Re-run `python3 scripts/lint_docs.py` and `./scripts/ensure_footers.py` against every file this
   session touches; fix anything this session's own edits introduce (a pre-existing, already-noisy
   finding elsewhere — e.g. `TODO.md`'s existing bare (unqualified) SPEC.md filename references
   `ai-sessions/0024`
   already left as-is — is not this session's responsibility to clean up unless it's editing that
   exact line anyway).
2. Confirm every new tool directory under `reverse-engineering/tools/` follows the same
   git-tracking boundary `reverse-engineering/tools/BACKLOG.md`'s governance section requires: source
   code tracked, nothing derived from or copied out of the decompiled APK tracked (check
   `.gitignore` covers each new tool's own test-fixture directory if it caches anything locally).
3. Check `id_registry.csv` for whether any new construct this session introduces needs its own
   registered ID (it shouldn't — tool names and `ai-sessions/` numbers aren't in that registry's
   scope per `AI_SESSION_LOG_PROCEDURE.md` §3 — but confirm this assumption rather than skipping the
   check).
4. Cross-check `ai-sessions/INDEX.md` against the actual current `Status` of every row this session
   touches (0024, 0025) and every `RESULT` file's own header field — they must agree.

## Phase 6 — Wrap-up and update every affected document

- Update `REVERSE_ENGINEERING.md`, `TODO.md`, `PROTOCOL.md` (only where a finding is genuinely
  wire-correlated and maintainer-approved — otherwise leave it at HYPOTHESIS/OPEN QUESTION, per the
  FACT/ADR gate that stays in force throughout this entire prompt), `reverse-engineering/tools/BACKLOG.md`,
  `APK_REVERSE_ENGINEERING_PROCEDURE.md`, `DESKRESEARCH_FINDINGS.md` (if Phase 4 found anything to
  add there), and `ai-sessions/INDEX.md` (rows for `0024` and `0025`).
- Re-read every file this session modified; confirm every substantive claim cites a file+line or
  frame reference (`PROJECT_RULES.md` rule 3).
- Finalize `0025_MAINTENANCE_RESULT_2026_09_16.md`: `Status: complete` only if every phase, including
  all 4 tools in Phase 1, is actually done; `partial — resumed` is the expected outcome if Phase 1's
  full tool set isn't finished in one pass — say plainly which tools are done and which aren't, per
  the Phase 1 sub-table.
- Write a final summary for the maintainer, answering the six points from the Context section
  directly: (1) which of the top-4 tools were implemented (and to what state, if not finished), (2)
  confirmation the workflow recommendations were adopted (with the citation trail per Phase 2), (3)
  what the tools found when used for real APK-RE work (Phase 3), (4) what Phase 4's cross-referencing
  against the five documents found — matches, new leads, and any flagged contradiction, (5) the
  outcome of Phase 5's consistency checks, (6) the full list of documents updated and what changed in
  each.

## Guardrails

- **Scoped to the companion app's own decompiled output only** (`reverse-engineering/apk/v1.0.955078536-10253511/`,
  or whatever version is actually on disk per Phase 0) — never Google Play Services, per
  `DECISIONS.md` ADR-025.
- **Never independently promote a finding to 🟢 FACT in `PROTOCOL.md`, and never write or alter a
  `DECISIONS.md` ADR, without explicit maintainer approval** (`AGENTS.md` §6/§15) — this prompt's own
  authorization (see Context) covers tooling priority and workflow process only, explicitly not this.
- **No sampling** when running any tool's `resolve-all`/batch output — every case is read, per
  `AGENTS.md` §13.6.
- This is a research/tooling/documentation task — no new Bluetooth capture or hardware action is
  performed as part of this prompt (no new `CAP-NNN` session is created), per items A/D/E/K's own
  classification in Phase 3.
- **No decompiled APK content is ever committed**, in any tool's source, tests, or fixtures — see
  Phase 1's own governance recap and `reverse-engineering/tools/BACKLOG.md` §0's fuller text on the
  specific mistake this rule prevents.
- Every proposed *protocol* finding is labeled awaiting maintainer sign-off; every *tooling/workflow*
  decision this prompt itself authorizes may be recorded as adopted, with this prompt cited as the
  event, per Phase 2's own scoped authorization.

## Output

At the end of each phase (and each tool inside Phase 1), a short note (in the RESULT file, and to the
maintainer if the chat is still live) of what was found/changed/built. At the end of Phase 6, the
full summary described there.

---

**Original maintainer instruction (Dutch, preserved verbatim for exact phrasing):**

> Maak een prompt in ai-sessions/ in het engels volgens de regels van dit project, met betrekking tot
> ai-sessions/0024_AUDIT_RESULT_2026_09_16.md, die: 1) eerst de belangrijkste 4 adviezen met
> betrekking tot de items op de BACKLOG.md implementeerd 2) de aanbevelingen met betrekking tot work
> flows over neemt. 3) Het verder uitvoeren van het reverse engineeren van de APK middels de nieuwe
> scripts en work flows. 4) De bevindingen van het reserve engineeren proberen te matchen met de
> CAP-0nn-FINDINGS.md, PROTOCOL.md, REVERSE_ENGINEERING.md, DESKRESEARCH_FINDINGS.md en TODO.md 5)
> cross checks, consistency checks 6) alle documenten actualiseren.

---

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0025_MAINTENANCE_PROMPT_2026_09_16.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0025_MAINTENANCE_PROMPT_2026_09_16
