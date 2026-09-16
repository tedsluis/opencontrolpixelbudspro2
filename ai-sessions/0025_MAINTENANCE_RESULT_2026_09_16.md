# 0025_MAINTENANCE_RESULT_2026_09_16.md — Implement `ai-sessions/0024`'s top-4 `reverse-engineering/tools/BACKLOG.md` tools and workflow recommendations, continue APK reverse engineering with them, and cross-reference the results

**Number:** 0025
**Category:** MAINTENANCE
**Date:** 2026-09-16
**Title:** Implement the top 4 `reverse-engineering/tools/BACKLOG.md` items and the workflow amendments `ai-sessions/0024` proposed, use them to advance real APK reverse engineering, cross-reference the results against `CAP-NNN-FINDINGS.md`/`PROTOCOL.md`/`REVERSE_ENGINEERING.md`/`DESKRESEARCH_FINDINGS.md`/`TODO.md`, run project-wide consistency checks, and update every affected document
**Status:** partial — resumed

## Phase status table

| Phase | Status | Summary |
|---|---|---|
| 0 | done | Mandatory reading order completed in full (see below). APK version on disk matches `reverse-engineering/APK_VERSIONS.md`'s only row (`v1.0.955078536-10253511`) — no drift, no re-decompilation. `lambda_dispatcher_resolver`'s own `pytest tests/ -v` re-run before building anything on top of it: 14/14 passed. This RESULT file created in Phase 0 and updated progressively since. |
| 1 | partial | See the tool-by-tool sub-table below. Tool 1 (structural/XREF code index) is fully done: spec drafted, implemented, tested (7/7 pytest, all 3 of `reverse-engineering/tools/structural_index/SPEC.md` §10's acceptance criteria passing against the real APK), and used for real — extensively, in Phase 3. Tools 2-4 (schema batch-extractor, UUID/BLE-context reconstruction, limited dataflow) are **not built** — this session stopped at a clean tool-1 boundary per the prompt's own instruction, then spent its remaining room on real APK-RE work with tool 1 (Phase 3) rather than starting tool 2 half-finished. |
| 2 | done | `reverse-engineering/tools/BACKLOG.md`'s 2026-09-16 proposed-reordering blockquote updated from "proposal, awaiting sign-off" to "approved," citing this prompt (`ai-sessions/0025`) as the event, per `AI_SESSION_LOG_PROCEDURE.md` §4a. `APK_REVERSE_ENGINEERING_PROCEDURE.md`'s §4a and its Notes & Gotchas retry-strategy bullet checked — neither ever carried an explicit "proposal" tag in their own text (only `reverse-engineering/tools/BACKLOG.md`'s separate blockquote did), so a short adoption-confirmation note was added to each instead of a substantive rewrite, honestly reflecting that there was no textual flag to flip. `ai-sessions/0024_AUDIT_RESULT_2026_09_16.md`'s own `Status` field updated in place: tooling/workflow proposals (Phase 3/Phase 4 of that file) now recorded as adopted via this prompt; its Phase 1 code-level leads (`esk` discriminator 18/default-branch entries) explicitly carved out as still unreviewed by the maintainer, not swept into the same status. |
| 3 | done (bounded) | Ran the zero-cost "free win" (`lambda_dispatcher_resolver resolve-all --class gag`, all 21 cases read, no sampling) and used the new structural-index tool on open items B, F, G, H, J from `ai-sessions/0024`'s Phase 2 inventory, plus a full manifest re-review (item L). See the findings summary below — item F and item J are resolved; item C is corrected (a prior misattribution walked back); items B, G, H are narrowed with new, concretely-cited leads; item L surfaced one genuinely new component (`BluetoothPriorityReceiver`). Item M (resource/string-table sweep) was **not** attempted this pass — out of room. Items A/D/E/K were correctly left alone (capture-only/live-check-only, per this prompt's own guardrails and `ai-sessions/0024`'s own classification). |
| 4 | done (for everything Phase 3 produced) | Every Phase 3 finding, plus the two `ai-sessions/0024`-originated findings the prompt specifically named (the `esk` discriminator-18 "Feature A" write site and the default-branch `gcp`/`gcn` device_info DAO lead), checked against all five named documents. Results: `PROTOCOL.md` — 3 items updated with cross-reference pointers (Feature A's now-concrete write site; `qhr` field 6's now-found caller; the `gjv.p()`/`gag` OTA-trigger chain confirmed from the construction side, with the prior 7-day-staleness misattribution corrected); `MaestroEndpointService`'s open item updated with the `ofd`/`ofh`/`ofi`/`ofj` checked negative; a new open item added for `BluetoothPriorityReceiver`. `REVERSE_ENGINEERING.md` — internal self-consistency checks were the primary mechanism *producing* several Phase 3 findings themselves (F, J), not a separate afterward step; no contradiction found anywhere, only extensions/corrections of existing entries. `DESKRESEARCH_FINDINGS.md` — checked, no existing mention of any Phase 3 finding's subject matter (all code-level, no wire correlation exists yet to conflict with or corroborate). `CAP-NNN-FINDINGS.md` files that exist — checked via targeted `grep` for "Feature A"/`BluetoothPriority`/connection-priority terms across `captures/`; zero hits, confirming (not contradicting) `PROTOCOL.md`'s own existing note that no capture has wire-correlated "Feature A" yet. `TODO.md` — its Phase 2 `esk` entry updated to record item F's resolution and item B's narrowed status. **No contradiction found anywhere** — every cross-check was a match, an extension, or a genuine "no existing mention," never a conflict needing to be flagged and left unresolved. |
| 5 | done | `python3 scripts/lint_docs.py` and `./scripts/ensure_footers.py` re-run against every file this session touched; every issue this session's own edits introduced (a `reverse-engineering/tools/structural_index/SPEC.md`/`TODO.md`/`REVERSE_ENGINEERING.md` bare-filename reference, two missing tool-doc footers) was fixed; pre-existing noise this session didn't touch (the 3 bare `reverse-engineering/tools/structural_index/SPEC.md` refs in `TODO.md`'s `lambda_dispatcher_resolver` entry, already flagged and left as-is by `ai-sessions/0024`; the prompt file's own forward-references to not-yet-built tools 2-4) was correctly left alone. Git-tracking boundary for `reverse-engineering/tools/structural_index/` confirmed via `git add -n` + `git status --ignored`: only `reverse-engineering/tools/structural_index/SPEC.md`/`README.md`/`pyproject.toml`/`src/`/`tests/` would be tracked; `.venv/`/`__pycache__/`/`.pytest_cache/` are ignored by the existing (lightly generalized) `.gitignore` glob. `id_registry.csv` confirmed out of scope for this session (no new ADR/CAP/Test-ID was created — only a new tool directory and `ai-sessions/` entries, neither of which that registry covers, per `AI_SESSION_LOG_PROCEDURE.md` §3). `ai-sessions/INDEX.md` cross-checked against both `RESULT` files' own header `Status` fields and updated to match for both `0024` and `0025`. |
| 6 | done (see below) | Wrap-up and maintainer summary below. |

### Phase 1 tool-by-tool status

| Tool (priority order) | Spec drafted | Implemented | Tested | Used for real | What it found when used |
|---|---|---|---|---|---|
| 1. General structural/XREF code index | ✅ `reverse-engineering/tools/structural_index/SPEC.md` | ✅ `reverse-engineering/tools/structural_index/src/structural_index/{cli,xref_index,models}.py`, reusing `lambda_dispatcher_resolver`'s own Layer 1 (`androguard_index.load_apk`) directly via a `sys.path` insertion, not re-implemented | ✅ 7/7 pytest (`tests/test_xref_index.py`), all 3 of `reverse-engineering/tools/structural_index/SPEC.md` §10's named acceptance criteria passing against the real, locally-decompiled APK: `esk`'s 21 construction sites, `giz.p()`'s sole caller (`ftw.a`, reproducing `ai-sessions/0023`'s finding from a structured query), and the `aie`-referenced-class zero-reference check | ✅ extensively — see Phase 3 below | Resolved item F (`gcp`/`gcn` = the same `device_info` DAO layer as `gcl`/`gck`/`eht`, not a distinct accessor) and item J (`gbb`/`gbc` have exactly one construction site each — nothing to generalize). Narrowed item B (`ofd`'s 3 implementations confirmed complete; 3 new field-holders `ofh`/`ofi`/`ofj` found and ruled out as the multibinding assembly site — a checked negative, not the answer). Found `qhr` field 6's own caller (item G) via the same abstract-interface-indirection technique that found `gjv.p()`'s caller. Confirmed all 12 of item H's candidate rich schemas have zero external construction sites, and found a nesting relationship among them (`nef` contains `ndi`/`nca`; `nhm` contains `nfh`) plus an unattributed field-holder naming cluster. |
| 2. Protobuf/`RawMessageInfo` schema batch-extractor | ❌ not built | ❌ | ❌ | ❌ | — |
| 3. UUID extraction + BLE/GATT context reconstruction | ❌ not built | ❌ | ❌ | ❌ | — |
| 4. Limited dataflow analysis | ❌ not built | ❌ | ❌ | ❌ | — |

## Mandatory reading order — completed this session

Per this prompt's own "Mandatory reading order" section and `AI_SESSION_LOG_PROCEDURE.md` §8:
`AGENTS.md` (full, including §0's 2026-08-30 correction, §4, §6, §13.6), `PROJECT.md` (full),
`PROJECT_RULES.md` (full), `ARCHITECTURE.md` (full), `DECISIONS.md` (full ADR history, ADR-001
through ADR-029; ADR-017, ADR-019, and ADR-025 read in full as specifically called out, plus
ADR-018/ADR-020-024/ADR-026-029 read for context), `PROTOCOL.md` §6 in full (Framing/Commands &
schemas/Behavior/Resolved subsections), `TODO.md` (full), `REVERSE_ENGINEERING.md` (full
section-header map obtained via `grep`; the `qhr`/`fye` entry read in full including every dated
update through 2026-09-16; the `esk` entry, `frb`/`fuh`/`glk`/`gjv` entry,
`MaestroDeviceSettingsProviderService` entry, "Candidate rich schemas," "Full-tree GATT/BLE
reference sweep," "Correlation status with `PROTOCOL.md`," "Known limitations of this analysis,"
UUID register, Message Group/Code register, and the "GSND" naming-lead entry all read in full),
`reverse-engineering/tools/lambda_dispatcher_resolver/SPEC.md` and `README.md` (full),
`reverse-engineering/tools/BACKLOG.md` (full, including its 2026-09-16 proposed-reordering
blockquote and structural-index sketch), `APK_REVERSE_ENGINEERING_PROCEDURE.md` (full, including §4a
and its Notes & Gotchas additions), `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md` (both
full), `ai-sessions/0024_AUDIT_RESULT_2026_09_16.md` (full — this prompt's direct predecessor and
primary input), `DESKRESEARCH_FINDINGS.md` (full), and `captures/` (directory listing, to identify
which `CAP-NNN` directories are placeholder/not-yet-captured skeletons — `CAP-030` and
`CAP-052`-`CAP-057` all still carry a literal `yyyy-MM-dd` placeholder in their directory name and
have no real content; every other `CAP-NNN` directory has a real date and a completed
`CAP-NNN-FINDINGS.md`).

**Honest flag, same in kind as `ai-sessions/0024`'s own:** `PROTOCOL.md` §0/§1-§5 (roughly the first
1650 lines) were not read start-to-finish word-for-word this pass — §6 (Open questions, ~1100 lines)
was read in full since that is where every APK-code-adjacent open item actually lives, and §0/§4.1
were skimmed for the ANC/EQ/DLCI-framing context needed to interpret §6's own cross-references
correctly. This does not affect Phase 3's own work (code-level, not a `PROTOCOL.md` claim) but is
recorded honestly rather than silently claiming a full line-by-line read that didn't happen.

## Phase 0 — Setup

- **APK version check**: `reverse-engineering/apk/v1.0.955078536-10253511/` on disk matches
  `reverse-engineering/APK_VERSIONS.md`'s only registered row exactly — no drift, no
  re-decompilation performed or needed.
- **`lambda_dispatcher_resolver` regression check**: `.venv/bin/python3 -m pytest tests/ -v` from
  `reverse-engineering/tools/lambda_dispatcher_resolver/` → **14 passed, 0 failed**, confirming the
  foundation this session's new tool reuses (`androguard_index.py`) is sound before building on it.
- This RESULT file created with the required header block and phase-status table (including the
  Phase 1 tool sub-table), `Status: partial — resumed` from the start, updated progressively as work
  proceeded rather than written once at the end.

## Phase 1 — Structural/XREF code index (tool 1 of 4)

Built per `reverse-engineering/tools/structural_index/SPEC.md` (its own template:
`reverse-engineering/tools/lambda_dispatcher_resolver/SPEC.md`). V1 delivers exactly the single
capability `reverse-engineering/tools/BACKLOG.md`'s own 2026-09-16 sketch named: given a class, list
every other class/method that (a) constructs it, (b) calls one of its methods, or (c) holds it as a
field type, via a `refs`/`unreferenced` CLI reusing `lambda_dispatcher_resolver`'s own Layer 1
(`androguard_index.load_apk`) directly — imported via a `sys.path` insertion to the sibling tool's
`src/` directory, not re-implemented.

**Test results**: `.venv/bin/python3 -m pytest tests/ -v` → 7 passed, 0 failed. All 3 of `reverse-engineering/tools/structural_index/SPEC.md`
§10's named acceptance criteria pass against the real, locally-decompiled APK:

1. `esk`'s constructor sites (`refs --class esk`) include `fyv.a` (discriminator 19) and `gcp.g`
   (the default-branch discriminator-20 site) among 21 total constructs — matching
   `ai-sessions/0024`'s own by-hand `grep` count exactly.
2. `giz.p()`'s sole caller (`refs --class giz --method p`) returns exactly one entry,
   `defpackage.ftw`, method `a`, `invoke-virtual` — reproducing `ai-sessions/0023`'s
   `Lgiz;->p(` smali-grep finding from one structured query.
3. The `aie` zero-external-reference check (`unreferenced --class aly --class cvo --class gza`)
   correctly reports all three as externally referenced (`referenced: true`), matching the expected
   answer for these three widely-reused, non-Bluetooth `aie`-referenced classes.

**Governance**: mechanical assistance only — the tool never decides Bluetooth relevance and never
writes into `REVERSE_ENGINEERING.md`/`PROTOCOL.md`/`DECISIONS.md`/any `CAP-NNN-FINDINGS.md` itself
(confirmed: `xref_index.py`/`cli.py` contain no such write path). No decompiled APK content committed
anywhere — `git add -n`/`git status --ignored` confirm only `reverse-engineering/tools/structural_index/SPEC.md`/`README.md`/`pyproject.toml`/
`src/`/`tests/` are tracked; `.venv/`/caches are ignored by the existing (lightly generalized)
`.gitignore` tool-glob. `TODO.md`'s Phase 2 section and `reverse-engineering/tools/BACKLOG.md`'s own
entry both updated to record the tool as implemented, with pointers to its `reverse-engineering/tools/structural_index/SPEC.md`/`README.md`.

Tools 2-4 (schema batch-extractor, UUID/BLE-context reconstruction, limited dataflow) were **not**
built this session — this is the clean tool-boundary stopping point the prompt itself names as
acceptable, chosen deliberately over starting tool 2 and leaving it half-finished, in favor of
spending the remaining room using tool 1 for real work (Phase 3).

## Phase 2 — Workflow adoption

Per this prompt's own scoped authorization (tooling-priority/workflow-process decisions only, never
protocol claims):

1. **`reverse-engineering/tools/BACKLOG.md`**: the 2026-09-16 proposed-reordering blockquote's
   framing changed from "a proposal... for the maintainer to approve or reject" to "approved,"
   citing `ai-sessions/0025` (this prompt) as the sign-off event, per `AI_SESSION_LOG_PROCEDURE.md`
   §4a. The structural-index idea's own status line updated to record it as
   "Implemented 2026-09-16," pointing at its `reverse-engineering/tools/structural_index/SPEC.md`/`README.md`, while explicitly noting the
   deferred `implements`-query/resource-search capabilities remain not built (the idea stays open
   for whoever picks those up, not marked fully closed).
2. **`APK_REVERSE_ENGINEERING_PROCEDURE.md`**: checked §4a and the Notes & Gotchas retry-strategy
   bullet directly against the prompt's own claim that they "were already applied in-place as
   text... so this step is a status/citation update, not a rewrite." Found, honestly: neither
   passage ever carried an explicit "PROPOSAL"/"awaiting sign-off" tag in its own text in the first
   place (`grep -n "PROPOSAL\|awaiting maintainer"` against the file returns nothing) — unlike
   `reverse-engineering/tools/BACKLOG.md`'s own explicit blockquote, `ai-sessions/0024` applied these two passages directly as
   normative procedure text from the start. Added a short adoption-confirmation note to each,
   citing this session's own real re-use of both (running `resolve-all --class gag`; finding `qhr`
   field 6's caller via the interface-indirection retry technique) as evidence the guidance holds up
   in practice, rather than rewriting substance that was never flagged as provisional.
3. **`ai-sessions/0024_AUDIT_RESULT_2026_09_16.md`**: `Status` field updated in place from
   `awaiting maintainer sign-off` to record that its tooling/workflow proposals (Phase 3's
   `reverse-engineering/tools/BACKLOG.md` re-ordering, Phase 4's procedure amendment) are now adopted via this prompt — while
   explicitly carving out that its Phase 1 code-level leads (the `esk` discriminator-18/default-branch
   entries) remain unreviewed by the maintainer and are not covered by this update, per this
   prompt's own scoping. `ai-sessions/INDEX.md`'s row for `0024` updated to match.

## Phase 3 — Real APK reverse engineering with the new tool

Worklist: `ai-sessions/0024`'s own Phase 2 inventory (items A-M).

### Item C (the zero-cost free win) — `gag` `resolve-all`, all 21 cases read, no sampling

**Corrects a prior static-analysis misattribution.** The 2026-09-13 finding that a `604800000`ms
(~7-day) staleness check gates `gjv.p()`/`fxm.i()`'s `GetSoftwareInfo` fetch turns out to be wrong:
`resolve-all`'s own per-case line-range isolation shows that literal belongs to `gag`'s
**discriminator 10** (`HearingWellnessNotificationWorker`'s own, unrelated once-per-7-days
notification throttle), not discriminator 15 (the branch that actually feeds `ftw(_,9)`/`gjv.p()`).
Discriminator 15's own branch contains no staleness check of any kind. Its real construction site
is now found instead: `gjy.java:38`, inside a method operating on a variable literally named
`otaApplyWorker2` — confirming, from the trigger side, that this whole chain is OTA-apply-lifecycle
scoped, consistent with (and now doubly confirming) `ai-sessions/0023`'s own finding from the
response side. The other 19 real discriminators were also read in full: mostly generic
utility/transform helpers and OTA-progress-tracking machinery, one already-documented RFCOMM
socket-creation call site (discriminator 4), and one genuinely new but not-Bluetooth-relevant
`SharedPreferences` key reference (`"key_lea_ever_enabled"`, discriminator 9). No tool bug found.

### Item B — `MaestroEndpointService`'s Dagger-multibinding assembly site

Narrowed, not closed. `structural-index refs --class ofd` confirms its 3 known implementations
(`mie`/`oex`/`ofb`) are the only ones, and surfaces 3 new field-holders (`ofh`/`ofi`/`ofj`) —
reading them directly shows they are generic `io.grpc`-shaped transport-factory/server-builder
plumbing, unrelated to `MaestroEndpointService`'s own service-registration map. A checked negative,
not the assembly site. The actual injector class (whatever sets `MaestroEndpointService.b`, the
`Map` field) has no findable literal name and would need either a future `implements`-query
capability (deferred from `structural_index` v1) or a field-*write* search (not built) to locate.

### Item F — `gcp`/`gcn` vs. `gcl`/`gck`/`eht` — resolved

They are the same `device_info` data pathway at different layers, not two independent accessors.
`structural-index refs --class gcp` shows its sole construction site is `DeviceInfoRoomDatabase_Impl`
(Room's own generated code); `refs --class gcn` shows `gck` (the already-known sink) itself holds a
`gcn`-typed field. Reading `gcn.java` directly confirms it: `gcn.f(String, gcm)` — the exact method
`esk`'s default branch calls — constructs `new gcl(...)` inside its own body before writing. `gck`
(repository) → `gcn`/`gcp` (DAO) → `gcl` (domain value object) is one continuous pathway.

### Item G — remaining never-traced `qhr` fields — field 6's caller found

`structural-index refs --class fyo --method m` returns zero calls (matching the prior session's own
dead end) — but `fyo` implements interface `fya`, and re-querying `--class fya --method m` finds
exactly two calls, both from `fmp` (itself another R8-merged lambda dispatcher). `resolve-all
--class fmp` finds discriminators 11/19 call `fya.m()`; their construction sites are `fyc.java:44`
and `guy.java:43`. Reading `guy.java` shows a lifecycle observer logging `"Enable OOBE mode"`/
`"Disable OOBE mode"` — proposed reading: field 6 is a transient "OOBE mode active" flag, distinct
from field 3's completion flag. 🟡 HYPOTHESIS, code-level only.

### Item H — the 12 candidate rich schemas

All 12 confirmed to have zero external construction sites (only their own `<clinit>`/factory
method) — extends the original "top 3 checked by hand" finding to the full set. New: `nef` contains
`ndi`/`nca` as nested fields, `nhm` contains `nfh` — narrowing the count of independent "roots."
A previously-uncatalogued field-holder naming cluster (`kii`/`koq`/`pzr`/`jau`/`msc`/`jaj`/`jjn`/
`jjx`/`jkl`/`jsg`/`kip`/`kiq`/`kob`/`kol`/`qan`/`fwe`/`nbm`) surfaced but not attributed — plausibly
(not confirmed) an unrelated bundled feedback/diagnostics subsystem given the field-count/naming
density.

### Item J — `gbb`/`gbc`'s "second `gbd`-construction path" — resolved

`gbc` has exactly one construction site (`fqg.K`); `gbb` has exactly one (inside `gbc.b`). There is
no second path to generalize from — the question's own premise doesn't apply.

### Item L — full manifest re-review

Done via `grep -B3 'android:exported="true"'` against the apktool-decompiled manifest. Confirmed
most exported components are generic AndroidX/Firebase/GMS/notifications plumbing (already
out-of-scope per `DECISIONS.md` ADR-008/ADR-025). One genuinely new finding:
**`BluetoothPriorityReceiver`** (`phone.bluetoothpriority` package, exported, handling a custom
`ACTION_TRIGGER_CLASSIC_CONNECTION_PRIORITY` intent with `EXTRA_BD_ADDR`/`EXTRA_PRIORITY`/
`EXTRA_DATA_DIRECTION` extras, holding a direct `fzd`-typed field) — not previously catalogued
anywhere in this project. 🔴 OPEN QUESTION, no capture correlation attempted.

### Item M — resource/string-table sweep

**Not attempted this pass** — out of room after items B/C/F/G/H/J/L. Remains open for a future
session; `structural_index`'s own v1 doesn't cover this data source anyway (deferred per its own
`reverse-engineering/tools/structural_index/SPEC.md` §2.2, would need a follow-up pass reading `apktool-output/res/`).

### Items A/D/E/K

Correctly left alone, per their own existing classification (`ai-sessions/0024`'s Phase 2): A and E
need a fresh capture or an exhausted-static-analysis-ceiling acceptance; D needs either a deeper
Dagger-graph trace or a live check; K is wire-payload content the APK's own source cannot resolve.
No new Bluetooth capture was performed, per this prompt's own guardrails.

## Phase 4 — Cross-referencing against the five documents

Every Phase 3 finding, plus the two `ai-sessions/0024`-originated findings this prompt specifically
named (the `esk` discriminator-18 "Feature A" write site, the default-branch `gcp`/`gcn` device_info
DAO lead), checked against all five documents:

1. **`CAP-NNN-FINDINGS.md` files that exist**: `grep -rln "Feature A\|BluetoothPriority\|classic
   connection priority\|CLASSIC_CONNECTION_PRIORITY" captures/` → **zero hits**. No existing
   capture's wire evidence touches the "Feature A" mechanism or the `device_info` table by name —
   this is a confirmation of (not a contradiction to) `PROTOCOL.md` §6's own existing note that
   "Feature A" has never been wire-correlated. No match, no contradiction.
2. **`PROTOCOL.md`** (full text checked, not just §6): 3 existing open items updated with
   cross-reference pointers to this session's own new evidence — the "Feature A" case-2104/2115
   item (now has a concrete file+line write-site citation), `qhr` field 6's register note (caller
   now found), and the `gjv.p()`/connect-time-burst item (confirmed from the trigger side, with the
   prior 7-day-staleness misattribution corrected and withdrawn). `MaestroEndpointService`'s open
   item updated with the `ofd`/`ofh`/`ofi`/`ofj` checked negative. A new open item added for
   `BluetoothPriorityReceiver`. All matches/extensions — no contradiction found.
3. **`REVERSE_ENGINEERING.md`**: internal self-consistency checking was the mechanism that
   *produced* items F and J's own resolutions (comparing `gcp`/`gcn`'s references against
   `gcl`/`gck`/`eht`'s; checking whether `gbb`/`gbc` had more than one construction site) — not a
   separate afterward step. No duplicate/contradicting entry under a different obfuscated name was
   found for anything this session touched.
4. **`DESKRESEARCH_FINDINGS.md`**: checked via targeted `grep` for every new finding's key terms —
   zero existing mentions of any of them. No corroboration or conflict to record; this file's own
   scope (capture-derived pattern analysis) doesn't overlap with this session's code-level-only
   findings, so a "no existing mention" result here is expected, not a gap.
5. **`TODO.md`**: Phase 2's `esk`/`lambda_dispatcher_resolver` entry updated to record item F's
   resolution and item B's narrowed status; the structural-index tool's own entry added.

**No contradiction found anywhere in this cross-referencing pass** — every check landed as a match,
an extension of existing text, or a genuine "no existing mention," never a conflict between two
documents' existing claims needing to be flagged and left for the maintainer.

## Phase 5 — Consistency checks

1. `python3 scripts/lint_docs.py` and `./scripts/ensure_footers.py` re-run against every file this
   session touched. Issues this session's own edits introduced, all fixed: two unqualified
   sibling-doc filenames inside the new `structural_index` tool's own spec document (fixed by
   prefixing each with the correct relative path, `../`); two similarly unqualified filenames
   inside `TODO.md`'s own new bullet (each fixed by qualifying to its full
   `reverse-engineering/tools/...` path); one unqualified filename inside `REVERSE_ENGINEERING.md`
   (fixed by adding the missing `reverse-engineering/tools/` prefix); missing footers on the new
   tool's own two doc files (added by `ensure_footers.py`). Pre-existing noise this session did not
   touch (3 already-unqualified references inside `TODO.md`'s *original*
   `lambda_dispatcher_resolver` entry, already flagged and left as-is by `ai-sessions/0024`; the
   prompt file `ai-sessions/0025_MAINTENANCE_PROMPT_2026_09_16.md`'s own forward-references to
   tools 2-4's not-yet-existing spec files) was correctly left alone, per this prompt's own Phase 5
   instruction.
2. Git-tracking boundary for `reverse-engineering/tools/structural_index/`: `git add -n` confirms
   only `reverse-engineering/tools/structural_index/SPEC.md`/`README.md`/`pyproject.toml`/`src/`/`tests/` would be staged; `git status
   --porcelain --ignored` confirms `.venv/`/`__pycache__/`/`.pytest_cache/` are all ignored, via the
   existing (generalized this session, see below) `.gitignore` glob pattern
   `reverse-engineering/tools/*/.venv/`.
3. `id_registry.csv` checked and confirmed out of scope: no new `ADR-NNN`/`CAP-NNN`/Test-ID was
   introduced this session (only a new tool directory and `ai-sessions/` entries, neither covered by
   that registry per `AI_SESSION_LOG_PROCEDURE.md` §3) — the assumption was confirmed, not skipped.
4. `ai-sessions/INDEX.md` cross-checked against both `RESULT` files' own header `Status` fields —
   `0024`'s row updated to summarize its new, longer `Status` field text; `0025`'s row set to
   `partial — resumed`, matching this file's own header.

## Phase 6 — Wrap-up and update every affected document

- `.gitignore`'s tool-glob comment generalized from naming only `lambda_dispatcher_resolver`
  specifically to describing the pattern for any tool under `reverse-engineering/tools/<name>/`
  (the wildcard glob itself already covered `structural_index` without any change — only the
  comment was updated, for accuracy, since it previously read as if written for one specific tool).
- Every substantive new claim in the files this session modified cites a file+line (e.g.
  `gjy.java:38`, `gcn.java`'s `f(String, gcm)` method body, `guy.java:43`) or an existing
  document's own section/entry, per `PROJECT_RULES.md` rule 3.

### Final summary for the maintainer

**1. Which of the top-4 tools were implemented, and to what state?** Only tool 1 (general
structural/XREF code index) — fully implemented, tested (7/7 pytest, all 3 named acceptance
criteria passing against the real APK), and used extensively for real work this same session. Tools
2-4 (schema batch-extractor, UUID/BLE-context reconstruction, limited dataflow) are **not built** —
a deliberate stopping point at a clean tool boundary, per this prompt's own instruction, in favor of
spending the remaining session on real findings with tool 1 rather than a half-built tool 2.

**2. Were the workflow recommendations adopted?** Yes, for the tooling-priority and workflow-process
scope this prompt's own authorization covers: `reverse-engineering/tools/BACKLOG.md`'s re-ordering
blockquote now reads "approved," citing this prompt; `APK_REVERSE_ENGINEERING_PROCEDURE.md`'s §4a
and its retry-strategy bullet got a short adoption-confirmation note each (neither needed a
substantive rewrite, since neither ever carried an explicit "proposal" tag to begin with — an honest
finding, not an assumed one); `ai-sessions/0024`'s own `Status` field updated to reflect this,
explicitly not extending to its still-open Phase 1 code-level leads.

**3. What did the tools find when used for real APK-RE work?** Substantial findings across 7 of
`ai-sessions/0024`'s 13 open items (see Phase 3 above for the full detail): item F and item J are
**resolved** (the `device_info` DAO relationship; `gbb`/`gbc`'s single construction path); item C's
prior "~7-day staleness gate" reading is **corrected** (it belonged to an unrelated feature, not the
`GetSoftwareInfo` trigger chain, whose real construction site — `gjy.java:38` — is now found and
confirms an OTA-apply-completion trigger); items B, G, and H are **narrowed** with new, concretely
cited leads (a checked-negative gRPC-transport-builder dead end; `qhr` field 6's caller; the 12
candidate schemas' nesting relationships and an unattributed naming cluster); item L surfaced one
genuinely new component, `BluetoothPriorityReceiver`. Item M was not attempted (out of room). Every
finding is labeled 🟢 FACT (mechanical code facts) or 🟡 HYPOTHESIS/🔴 OPEN QUESTION (anything
interpretive), per `PROJECT_RULES.md` §1 — nothing here is a protocol-behavior claim, so none of it
needed or received a `PROTOCOL.md` 🟢 FACT promotion or a `DECISIONS.md` ADR.

**4. What did cross-referencing against the five documents find?** No contradictions anywhere.
`PROTOCOL.md` gained 3 updated open items and 1 new one (all cross-reference pointers, not FACT
promotions); `REVERSE_ENGINEERING.md`'s own internal self-consistency checking was the mechanism
that resolved items F and J in the first place; `DESKRESEARCH_FINDINGS.md` had no existing mention
of anything this session found (expected, given this session's findings are all code-level);
`captures/` had zero wire-level hits for "Feature A"/`BluetoothPriority` (a confirming, not
contradicting, negative); `TODO.md`'s own `esk` entry updated to record item F's resolution.

**5. What did the consistency checks find?** `lint_docs.py`/`ensure_footers.py` caught, and this
session fixed, several bare-filename/missing-footer issues its own new files introduced; nothing
pre-existing was touched. The new tool's git-tracking boundary is correct (source tracked,
`.venv`/caches ignored). `id_registry.csv` needed no new entries (confirmed, not assumed).
`ai-sessions/INDEX.md` now matches both `RESULT` files' own `Status` fields.

**6. Full list of documents updated, and what changed in each:**

- `reverse-engineering/tools/structural_index/` (new) — `reverse-engineering/tools/structural_index/SPEC.md`, `README.md`, `pyproject.toml`,
  `src/structural_index/{__init__,cli,xref_index,models}.py`, `tests/{__init__,test_xref_index}.py`.
  The new tool (Phase 1).
- `.gitignore` — generalized the tool-glob comment (already covered the new tool by wildcard; only
  the comment text changed).
- `reverse-engineering/tools/BACKLOG.md` — the structural-index idea marked "Implemented
  2026-09-16," pointing at its `reverse-engineering/tools/structural_index/SPEC.md`/`README.md`; the 2026-09-16 re-ordering blockquote's
  framing changed from proposed to approved, citing this prompt.
- `APK_REVERSE_ENGINEERING_PROCEDURE.md` — §4a's heading and the Notes & Gotchas retry-strategy
  bullet each got a short "adoption confirmed" note citing this session's own real re-use of both.
- `ai-sessions/0024_AUDIT_RESULT_2026_09_16.md` — `Status` field updated in place (tooling/workflow
  proposals adopted via this prompt; Phase 1 code-level leads explicitly carved out as still
  unreviewed).
- `ai-sessions/INDEX.md` — rows for `0024` (new status summary) and `0025` (this file, `partial —
  resumed`) updated.
- `REVERSE_ENGINEERING.md` — 6 new/updated entries: the `gbb`/`gbc` "Open questions (remaining)"
  item (resolved, item J); the `esk` entry (`gcp`/`gcn` vs. `gcl`/`gck`/`eht` resolved, item F); the
  `frb`/`fuh`/`glk`/`gjv` entry (the `gag` `resolve-all` pass, correcting the 7-day-staleness
  misattribution and confirming the OTA trigger from the construction side, item C); the
  `MaestroEndpointService` entry (`ofd`/`ofh`/`ofi`/`ofj` checked negative, item B); the `qhr` entry
  (field 6's caller found, item G); the "Candidate rich schemas" section (all 12 confirmed
  zero-external-reference, nesting relationships found, item H); a new `BluetoothPriorityReceiver`
  entry (item L).
- `PROTOCOL.md` — §6 updated in 4 places: the "Feature A" case-2104/2115 item (concrete write-site
  citation added); the field-6 register note (caller found); the `gjv.p()`/connect-time-burst item
  (confirmed from the trigger side, prior misattribution corrected); the `MaestroEndpointService`
  open item (checked negative added); one new open item added (`BluetoothPriorityReceiver`).
- `TODO.md` — Phase 2's `esk`/`lambda_dispatcher_resolver` entry updated (item F resolution, item B
  narrowed status); a new entry added recording the structural-index tool itself.
- `ai-sessions/0025_MAINTENANCE_RESULT_2026_09_16.md` (this file) — created and progressively
  updated throughout the session.

## What remains uncommitted

Everything listed above is uncommitted — no `git commit` or `git push` was run at any point this
session, per this prompt's own explicit instruction that session bookkeeping is manual. `git status`
at the end of this session shows every file in the list above as modified or untracked, plus the
new `reverse-engineering/tools/structural_index/` directory (source only — its own `.venv/`/caches
are gitignored and were never staged).

## Where the next session should pick up

Resume at **Phase 1, tool 2** (protobuf/`RawMessageInfo` schema batch-extractor) — the next item in
priority order, following `reverse-engineering/tools/structural_index/SPEC.md` as the template the
same way it followed `reverse-engineering/tools/lambda_dispatcher_resolver/SPEC.md`. Once tools 2-4 exist (or a decision is
made to stop building tools and just keep using tool 1), the remaining real-APK-RE work still open
from this session's own Phase 3 is: item M (resource/string-table sweep, cheap, not yet attempted);
item B's actual multibinding-assembly site (would benefit from tool 1's own deferred
`implements`-query capability); item H's unattributed field-holder naming cluster
(`kii`/`koq`/`pzr`/`jau`/`msc`/`jaj`/`jjn`/`jjx`/`jkl`/`jsg`/`kip`/`kiq`/`kob`/`kol`/`qan`/`fwe`/
`nbm`) — tracing even one of these classes' own purpose would be a natural next step; and
`BluetoothPriorityReceiver`'s own trigger/effect (item L's new finding), which is capture-territory,
not further static analysis, if pursued.

---

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0025_MAINTENANCE_RESULT_2026_09_16.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0025_MAINTENANCE_RESULT_2026_09_16
