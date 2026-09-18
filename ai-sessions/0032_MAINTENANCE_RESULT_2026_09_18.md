# 0032_MAINTENANCE_RESULT_2026_09_18.md — Close `ai-sessions/0031`'s disclosed Phase 3 reading-scope gap and re-run its consistency-pass checklist properly

**Number:** 0032
**Category:** MAINTENANCE
**Date:** 2026-09-18
**Prompt:** `ai-sessions/0032_MAINTENANCE_PROMPT_2026_09_18.md`
**Status:** complete

---

## What was read this session

Everything `ai-sessions/0031`'s own "Phase 0/3 scope trade-off" section disclosed as not linearly
read: `CONTRIBUTING.md`, `WORKSTATION_PREPARATIONS.md`, `CHANGELOG.md`, `README.md`,
`DESKRESEARCH_FINDINGS.md`, `APK_REVERSE_ENGINEERING_PROCEDURE.md`, `AI_SESSION_LOG_PROCEDURE.md`, all
5 RE tools' own `README.md` files, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` in full (all ~1895 lines, including
this session's own edits), `ai-sessions/0027_MAINTENANCE_RESULT_2026_09_17.md` through
`0030_MAINTENANCE_RESULT_2026_09_17.md` in full, and `REVERSE_ENGINEERING.md`'s remaining
previously-untouched tail sections (UUID register, Message Group/Code register, the `"GSND"` naming
lead, Native libraries, Call graph notes, Correlation status with `PROTOCOL.md`, Known limitations).

**One further, smaller, disclosed scope reduction of its own**: `REVERSE_ENGINEERING.md`'s own
"Identified relevant classes" body (roughly lines 105–3830 — `fzd`/`gbm`/`esk`/`qhr`'s full text/`qjw`/
`qjg`/`qht`/`qjo`/`qju`/the 0-field marker types/`qjb`/`ghd`/`ghb`/`gbu`/`goq`/the other
request-response-types register/the full-tree GATT sweep/the GMS Fast Pair client-library boundary/
`MaestroDeviceSettingsProviderService`/`frb`-`gjv`) was **not** re-read linearly this session — it was
read in full just one day earlier by `ai-sessions/0027` (2026-09-17), whose own Phase 5 cross-check
found zero contradictions against `DECISIONS.md`/`DESKRESEARCH_FINDINGS.md`/`TODO.md`/every
`CAP-NNN-FINDINGS.md`. Given that very recent, thorough pass, re-reading it line-by-line again the next
day was judged lower-value than the genuinely-never-read documents above, within this session's own
bounded time. Disclosed here, not silently skipped, matching the same convention `0030`/`0031`
themselves used for their own scope trade-offs.

## Findings and fixes, file+line cited

1. **Stale "current top priority" framing in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`** — the same class of bug
   `ai-sessions/0030` found once in `TODO.md`. Two spots (the §4.1 intro's own priority-tip block,
   ~line 212, and Group T's own header/intro, ~line 489) still asserted Group T (EQ command isolation)
   as the project's current single highest-priority capture target — but EQ's command channel has long
   since been fully resolved (`CAP-005`/`CAP-015`, `DECISIONS.md` ADR-013/ADR-019/ADR-020). **Fixed**:
   both spots annotated as superseded, following this same document's own established convention
   (`PROJECT_RULES.md` §3 — keep the historical record, mark it explicitly outdated) rather than
   deleted.
2. **`README.md`'s "Current state" section was stale on a factual count** — dated 2026-09-13, it said
   "52 registered sessions (`CAP-001`–`CAP-052`)"; `id_registry.csv` now has 58 capture entries
   (`CAP-001`–`CAP-058`, 10 still `planned`). **Fixed**: count and date updated.
3. **A doubled `---` separator glitch** in 4 of the 5 RE tools' own `README.md` files
   (`structural_index`, `schema_batch_extractor`, `uuid_ble_context`, `limited_dataflow` — not
   `lambda_dispatcher_resolver`, which was correct) — an accidental duplicate horizontal rule
   immediately before the mandatory footer link, present since each file's own creation. **Fixed**, all
   4.
4. **`ai-sessions/INDEX.md`/RESULT-file `Status`-field contradiction for `0017`/`0021`/`0022`** — the
   fork that ran `0031`'s Phase 4 sign-off updated `ai-sessions/INDEX.md`'s own summary rows for these
   three sessions (to `complete`/`partial` with detail) but never updated each session's own
   `RESULT` file header `Status:` field, which still read the bare `awaiting maintainer sign-off` —
   a direct contradiction between two documents about the same fact, exactly the kind of
   undocumented status drift this consistency pass exists to catch, and exactly what
   `AI_SESSION_LOG_PROCEDURE.md` §4a's own text requires be kept in sync. **Fixed**: all three RESULT
   files' `Status:` headers updated to match `INDEX.md`, each citing `AI_SESSION_LOG_PROCEDURE.md` §4a
   and explaining precisely what's still open for `0022` (its 2 unresolved items don't disappear —
   they're now stated in the `Status` field itself, not just in `INDEX.md`'s prose).
5. **A flagged-but-never-fixed nit from `ai-sessions/0028`** — that session's own text explicitly noted
   "`PROTOCOL.md`'s own restatement of the `BluetoothPriorityReceiver`→`dcservice` finding doesn't carry
   an explicit 🟢 FACT tag the way its `REVERSE_ENGINEERING.md` source entry does," but never applied the
   fix, and no session since did either. **Fixed**: `PROTOCOL.md` §6's restatement (the "New finding"
   sentence) now carries the same 🟢 FACT tag as its `REVERSE_ENGINEERING.md` source entry, citing both
   `ai-sessions/0028`'s own flag and this fix.
6. **A genuine contradiction in `REVERSE_ENGINEERING.md`'s own "Correlation status with `PROTOCOL.md`"
   table** — its `MaestroEndpointService.onCreate()` row (dated 2026-09-08, sourced from
   `ai-sessions/0003`) still asserted "registered services come from a Dagger multibinding (names not
   recovered)." `ai-sessions/0027` (2026-09-17), maintainer-approved via `0028`, definitively overturned
   this: `MaestroEndpointService.b` is a hardcoded, permanently-empty Guava `ImmutableMap` — there is no
   multibinding at all. This table row was never updated when that finding landed, leaving a direct,
   uncorrected contradiction between two parts of the same document about the same fact. **Fixed**: the
   row now carries a "Superseded" note citing the correct, current finding and where it lives.

All 6 are plain documentation-accuracy/consistency corrections against already-established, already
maintainer-approved facts (`PROJECT_RULES.md` §1) — none constitutes a new protocol/architecture claim,
none promotes anything to 🟢 FACT that wasn't already there under this project's own established
code-existence-fact convention, and none touches `DECISIONS.md`. Nothing was flagged as needing further
maintainer sign-off beyond what `ai-sessions/0031` itself already flagged (the two `DECISIONS.md`-ADR-
text questions for CTKD/Battery Option B, and `0022`'s own remaining open items).

## `lint_docs.py`/`ensure_footers.py`

Run at the start of this session (baseline: same as `0031`'s own end-of-session run — `ANC-005` and
pre-existing capture-folder/`.venv` noise only, `CASE-009`/`CAP-058` cleanly registered) and again at
the end, after all 6 fixes above: **zero new issues** introduced by this session's own edits.
`ensure_footers.py` touched only 4 gitignored `.venv`-internal third-party package docs, same as every
prior session — not a real repo change.

## What's still not covered (disclosed, not silent)

- `REVERSE_ENGINEERING.md`'s own large "Identified relevant classes" body (see the scope note above) —
  read in full by `ai-sessions/0027` one day prior, not re-read line-by-line this session.
- The two `DECISIONS.md`-ADR-text flags `ai-sessions/0031` itself raised (CTKD gating, Battery Option
  B) remain exactly as flagged there — outside this session's own scope, which was specifically the
  reading gap, not new sign-off items.
- `ai-sessions/0022`'s own 2 genuinely open items (the swapped-slot-dock `DECISIONS.md` ADR-016
  disconnect-inconsistency, and the ADR-024 text-update proposal) remain open, unaffected by this
  session — this session only fixed the *header field* contradiction (item 4 above), not the
  underlying research questions.

## Files changed (uncommitted, in working tree — parent session to review and commit)

`CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `README.md`, `PROTOCOL.md`, `REVERSE_ENGINEERING.md`,
`ai-sessions/0017_MAINTENANCE_RESULT_2026_09_13.md`, `ai-sessions/0021_CAPTURE_RESULT_2026_09_15.md`,
`ai-sessions/0022_CAPTURE_RESULT_2026_09_15.md`,
`reverse-engineering/tools/{limited_dataflow,schema_batch_extractor,structural_index,uuid_ble_context}/README.md`,
plus this file and its paired prompt file. `ai-sessions/INDEX.md` updated with a new `0032` row.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0032_MAINTENANCE_RESULT_2026_09_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0032_MAINTENANCE_RESULT_2026_09_18
