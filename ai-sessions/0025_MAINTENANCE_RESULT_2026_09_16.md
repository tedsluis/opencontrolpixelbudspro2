# 0025_MAINTENANCE_RESULT_2026_09_16.md — Implement `ai-sessions/0024`'s top-4 `reverse-engineering/tools/BACKLOG.md` tools and workflow recommendations, continue APK reverse engineering with them, and cross-reference the results

**Number:** 0025
**Category:** MAINTENANCE
**Date:** 2026-09-16
**Title:** Implement the top 4 `reverse-engineering/tools/BACKLOG.md` items and the workflow amendments `ai-sessions/0024` proposed, use them to advance real APK reverse engineering, cross-reference the results against `CAP-NNN-FINDINGS.md`/`PROTOCOL.md`/`REVERSE_ENGINEERING.md`/`DESKRESEARCH_FINDINGS.md`/`TODO.md`, run project-wide consistency checks, and update every affected document
**Status:** complete. *(Updated 2026-09-17 by `ai-sessions/0026`, editing this header field in place
per `AI_SESSION_LOG_PROCEDURE.md` §4a — this file's own substantive content below is otherwise
unchanged. All of this file's tooling/workflow-process items and code-level findings were reviewed
and approved by the maintainer directly in chat; see `ai-sessions/0026_MAINTENANCE_RESULT_2026_09_17.md`
for the sign-off record and its explicit scope note — confidence tiers on any 🟡 HYPOTHESIS/🔴 OPEN
QUESTION finding are unchanged by this sign-off.)*

## Phase status table

| Phase | Status | Summary |
|---|---|---|
| 0 | done | Mandatory reading order completed in full (see below). APK version on disk matches `reverse-engineering/APK_VERSIONS.md`'s only row (`v1.0.955078536-10253511`) — no drift, no re-decompilation. `lambda_dispatcher_resolver`'s own `pytest tests/ -v` re-run before building anything on top of it: 14/14 passed. This RESULT file created in Phase 0 and updated progressively since. |
| 1 | done | See the tool-by-tool sub-table below. **All 4 tools are now fully done: spec drafted, implemented, tested, and used for real** — the top-4 `reverse-engineering/tools/BACKLOG.md` priority list this prompt names is complete. Tool 1 (structural/XREF code index): 7/7 pytest, all 3 of `reverse-engineering/tools/structural_index/SPEC.md` §10's acceptance criteria passing against the real APK, used extensively in Phase 3. Tool 2 (protobuf/`RawMessageInfo` schema batch-extractor): 12/12 pytest, all 6 of `reverse-engineering/tools/schema_batch_extractor/SPEC.md` §10's acceptance criteria passing, used for real on all 12 of item H's "candidate rich schemas." Tool 3 (UUID/BLE-context reconstruction): 9/9 pytest, all 6 of `reverse-engineering/tools/uuid_ble_context/SPEC.md` §10's acceptance criteria passing, used for real to find 6 UUID-shaped literals (1 genuinely new). Tool 4 (limited dataflow analysis, built after this resumption's own mid-task rate-limit interruption and continuation): 10/10 pytest, covering every one of `reverse-engineering/tools/limited_dataflow/SPEC.md` §10's acceptance criteria (two of which were found mis-described against the real APK and corrected in place), used for real to confirm the already-known `esk` discriminator-19 `WriteSetting` chain and to trace a genuinely new candidate, `esk` discriminator 18. See this file's own Phase 1/Phase 3 continuation sections below for tools 2-4's full write-ups. |
| 2 | done | `reverse-engineering/tools/BACKLOG.md`'s 2026-09-16 proposed-reordering blockquote updated from "proposal, awaiting sign-off" to "approved," citing this prompt (`ai-sessions/0025`) as the event, per `AI_SESSION_LOG_PROCEDURE.md` §4a. `APK_REVERSE_ENGINEERING_PROCEDURE.md`'s §4a and its Notes & Gotchas retry-strategy bullet checked — neither ever carried an explicit "proposal" tag in their own text (only `reverse-engineering/tools/BACKLOG.md`'s separate blockquote did), so a short adoption-confirmation note was added to each instead of a substantive rewrite, honestly reflecting that there was no textual flag to flip. `ai-sessions/0024_AUDIT_RESULT_2026_09_16.md`'s own `Status` field updated in place: tooling/workflow proposals (Phase 3/Phase 4 of that file) now recorded as adopted via this prompt; its Phase 1 code-level leads (`esk` discriminator 18/default-branch entries) explicitly carved out as still unreviewed by the maintainer, not swept into the same status. |
| 3 | done (bounded) | Ran the zero-cost "free win" (`lambda_dispatcher_resolver resolve-all --class gag`, all 21 cases read, no sampling) and used the new structural-index tool on open items B, F, G, H, J from `ai-sessions/0024`'s Phase 2 inventory, plus a full manifest re-review (item L). See the findings summary below — item F and item J are resolved; item C is corrected (a prior misattribution walked back); items B, G, H are narrowed with new, concretely-cited leads; item L surfaced one genuinely new component (`BluetoothPriorityReceiver`). Item M (resource/string-table sweep) was **not** attempted this pass — out of room. Items A/D/E/K were correctly left alone (capture-only/live-check-only, per this prompt's own guardrails and `ai-sessions/0024`'s own classification). |
| 4 | done (for everything Phase 3 produced) | Every Phase 3 finding, plus the two `ai-sessions/0024`-originated findings the prompt specifically named (the `esk` discriminator-18 "Feature A" write site and the default-branch `gcp`/`gcn` device_info DAO lead), checked against all five named documents. Results: `PROTOCOL.md` — 3 items updated with cross-reference pointers (Feature A's now-concrete write site; `qhr` field 6's now-found caller; the `gjv.p()`/`gag` OTA-trigger chain confirmed from the construction side, with the prior 7-day-staleness misattribution corrected); `MaestroEndpointService`'s open item updated with the `ofd`/`ofh`/`ofi`/`ofj` checked negative; a new open item added for `BluetoothPriorityReceiver`. `REVERSE_ENGINEERING.md` — internal self-consistency checks were the primary mechanism *producing* several Phase 3 findings themselves (F, J), not a separate afterward step; no contradiction found anywhere, only extensions/corrections of existing entries. `DESKRESEARCH_FINDINGS.md` — checked, no existing mention of any Phase 3 finding's subject matter (all code-level, no wire correlation exists yet to conflict with or corroborate). `CAP-NNN-FINDINGS.md` files that exist — checked via targeted `grep` for "Feature A"/`BluetoothPriority`/connection-priority terms across `captures/`; zero hits, confirming (not contradicting) `PROTOCOL.md`'s own existing note that no capture has wire-correlated "Feature A" yet. `TODO.md` — its Phase 2 `esk` entry updated to record item F's resolution and item B's narrowed status. **No contradiction found anywhere** — every cross-check was a match, an extension, or a genuine "no existing mention," never a conflict needing to be flagged and left unresolved. |
| 5 | done | `python3 scripts/lint_docs.py` and `./scripts/ensure_footers.py` re-run against every file this session touched; every issue this session's own edits introduced (a `reverse-engineering/tools/structural_index/SPEC.md`/`TODO.md`/`REVERSE_ENGINEERING.md` bare-filename reference, two missing tool-doc footers) was fixed; pre-existing noise this session didn't touch (the 3 bare `reverse-engineering/tools/structural_index/SPEC.md` refs in `TODO.md`'s `lambda_dispatcher_resolver` entry, already flagged and left as-is by `ai-sessions/0024`; the prompt file's own forward-references to not-yet-built tools 2-4) was correctly left alone. Git-tracking boundary for `reverse-engineering/tools/structural_index/` confirmed via `git add -n` + `git status --ignored`: only `reverse-engineering/tools/structural_index/SPEC.md`/`README.md`/`pyproject.toml`/`src/`/`tests/` would be tracked; `.venv/`/`__pycache__/`/`.pytest_cache/` are ignored by the existing (lightly generalized) `.gitignore` glob. `id_registry.csv` confirmed out of scope for this session (no new ADR/CAP/Test-ID was created — only a new tool directory and `ai-sessions/` entries, neither of which that registry covers, per `AI_SESSION_LOG_PROCEDURE.md` §3). `ai-sessions/INDEX.md` cross-checked against both `RESULT` files' own header `Status` fields and updated to match for both `0024` and `0025`. |
| 6 | done (see below) | Wrap-up and maintainer summary below. |

### Phase 1 tool-by-tool status

| Tool (priority order) | Spec drafted | Implemented | Tested | Used for real | What it found when used |
|---|---|---|---|---|---|
| 1. General structural/XREF code index | ✅ `reverse-engineering/tools/structural_index/SPEC.md` | ✅ `reverse-engineering/tools/structural_index/src/structural_index/{cli,xref_index,models}.py`, reusing `lambda_dispatcher_resolver`'s own Layer 1 (`androguard_index.load_apk`) directly via a `sys.path` insertion, not re-implemented | ✅ 7/7 pytest (`tests/test_xref_index.py`), all 3 of `reverse-engineering/tools/structural_index/SPEC.md` §10's named acceptance criteria passing against the real, locally-decompiled APK: `esk`'s 21 construction sites, `giz.p()`'s sole caller (`ftw.a`, reproducing `ai-sessions/0023`'s finding from a structured query), and the `aie`-referenced-class zero-reference check | ✅ extensively — see Phase 3 below | Resolved item F (`gcp`/`gcn` = the same `device_info` DAO layer as `gcl`/`gck`/`eht`, not a distinct accessor) and item J (`gbb`/`gbc` have exactly one construction site each — nothing to generalize). Narrowed item B (`ofd`'s 3 implementations confirmed complete; 3 new field-holders `ofh`/`ofi`/`ofj` found and ruled out as the multibinding assembly site — a checked negative, not the answer). Found `qhr` field 6's own caller (item G) via the same abstract-interface-indirection technique that found `gjv.p()`'s caller. Confirmed all 12 of item H's candidate rich schemas have zero external construction sites, and found a nesting relationship among them (`nef` contains `ndi`/`nca`; `nhm` contains `nfh`) plus an unattributed field-holder naming cluster. |
| 2. Protobuf/`RawMessageInfo` schema batch-extractor | ✅ `reverse-engineering/tools/schema_batch_extractor/SPEC.md` | ✅ `src/schema_batch_extractor/{cli,batch_extract,models}.py`, reusing `scripts/decode_rawmessageinfo.py` directly via a `sys.path` insertion, not re-implemented or modified | ✅ 12/12 pytest (`tests/test_batch_extract.py`), all 6 of `SPEC.md` §10's named acceptance criteria passing against the real, locally-decompiled APK: `qhr`/`qjc`/`qja`/`nqx`/`qjb` reproduced exactly; whole-tree count = 807 candidates, 807 decoded, 0 unparsable (matching `REVERSE_ENGINEERING.md`'s independently-obtained count); the disclosed plain-`MESSAGE`-field limitation confirmed as a regression | ✅ — see this file's Phase 3 continuation below | Recovered full field-level schemas for all 12 of item H's "candidate rich schemas" (previously only header counts were known). New: `mtn`'s own field 12 (oneof) *is* `msw` — not two independent roots. New, previously-uncatalogued: `mtn` is itself held as a repeated field of 3 new classes (`mqm`/`mra`/`mqk`, the latter also referencing a 4th new class `mtg`); `qaj` is held by a new class `qak` and itself references a new class `qaz` (`qak` also references a 5th new class `qam`). `qar`'s one map field's default-entry descriptor is `qaq.a`. 7 genuinely new class leads surfaced, none traced further. |
| 3. UUID extraction + BLE/GATT context reconstruction | ✅ `reverse-engineering/tools/uuid_ble_context/SPEC.md` | ✅ `src/uuid_ble_context/{cli,uuid_scan,models}.py`, reusing `structural_index`'s own `find_refs`/`load_apk` directly via a `sys.path` insertion, not re-implemented | ✅ 9/9 pytest (`tests/test_uuid_scan.py`), all 6 of `reverse-engineering/tools/uuid_ble_context/SPEC.md` §10's named acceptance criteria passing against the real, locally-decompiled APK: the 5 already-known register UUID literal forms reproduced exactly; whole-tree count = 6; the new non-Bluetooth find confirmed as a checked negative (2 occurrences, both `bt_api_cooccurrence: false`); `fzd`'s usage graph (`calls: 11`, `field_type_holders: 6`) matching a direct `structural_index refs --class fzd` run; the `fzd`/`gbm` (true) vs. `fqg` (false) co-occurrence discriminating pair; unknown-UUID hard error | ✅ — see this file's Phase 3 continuation below | Found exactly 6 distinct UUID-shaped literals in this APK version: the 5 already-registered forms (reconfirmed byte-for-byte), plus one genuinely new, non-Bluetooth find — an AndroidX WorkManager `Data`-serialization sentinel string, `95ed6082-b8e9-46e8-a73f-ff56f00f5d9d`, in `defpackage/ehs.java` (2 occurrences, both a checked negative). Also demonstrated a real-data limitation of the tool's own textual co-occurrence heuristic: `fqg.java` is genuinely Bluetooth-adjacent (it wires the RFCOMM UUID constant through) but shows `bt_api_cooccurrence: false`, since it never names a §3 BT API directly in its own file text — a disclosed, not silent, precision limit. |
| 4. Limited dataflow analysis | ✅ `reverse-engineering/tools/limited_dataflow/SPEC.md` | ✅ `src/limited_dataflow/{cli,dataflow,models}.py`, reusing `lambda_dispatcher_resolver`'s own `analyze_class`/`smali_reader` directly via a `sys.path` insertion, not re-implemented | ✅ 10/10 pytest (`tests/test_dataflow.py`: 6 against the real APK + 4 synthetic fixtures that always run), covering every one of `reverse-engineering/tools/limited_dataflow/SPEC.md` §10's named acceptance criteria — two of which were found to be mis-described against the real APK and corrected in `reverse-engineering/tools/limited_dataflow/SPEC.md` itself in the process (see this file's Phase 1 continuation below) | ✅ — see this file's Phase 1 continuation below | Confirmed the primary regression fixture (`esk` discriminator 19's already-known `WriteSetting` chain: `cast`-to-`qjc` then `sink_use` into `Lfys;-><init>`, correctly never reaching `nqo.e(...)`) and the adversarial basic-block-boundary fixture (`v1` surviving to the next `:pswitch_1` label). Used on one genuinely new candidate, `esk` discriminator 18 (the "Feature A" write site): confirmed, mechanically, that the device-id-carrying register never moves before the branch's own `if-eqz` — the conditional write this document's `esk` entry already describes in prose is now confirmed at the bytecode level, one basic block away from where the value is first read. |

### Resumption continued, 2026-09-16 (same date, later session) — tools 2-4

This RESULT file's own header `Status` stayed `partial — resumed` after the first pass (tool 1 only).
Per that pass's own "Where the next session should pick up" note (superseded by this continuation's
own note at the end of this file), this resumption picked up at **Phase 1, tool 2** exactly as
instructed, following `reverse-engineering/tools/structural_index/SPEC.md` as the template the same
way it followed `reverse-engineering/tools/lambda_dispatcher_resolver/SPEC.md`.

**Sub-continuation, 2026-09-17 — tool 4, after a mid-task Claude usage rate-limit interruption.**
This same resumption reached tool 2 (built and used for real, below) and tool 3 (built and used for
real, below) before being killed mid-task by a Claude usage rate limit while about to write tool 4's
own `tests/` directory — `reverse-engineering/tools/limited_dataflow/SPEC.md`/`README.md`/`src/` already existed and looked substantial, but
there was no `tests/` directory at all, so per this project's own tool-building discipline
(`reverse-engineering/tools/BACKLOG.md`'s governance section: an untested tool is not trusted or used
for real) tool 4 was not yet usable. A separate orchestrating session confirmed this exact on-disk
state (tools 2/3 fully done including this file's own write-up for tool 2, only tool 3's own
write-up below still missing, tool 4's tests missing) before continuing here. This sub-continuation
writes tool 3's missing narrative (below), builds and validates tool 4's tests, uses tool 4 for real,
and closes out this file's own remaining bookkeeping.

**Phase 0 re-confirmation (per this resumption's own instructions):** APK version on disk still
matches `reverse-engineering/APK_VERSIONS.md`'s only row (`v1.0.955078536-10253511`) — no drift.
`lambda_dispatcher_resolver`'s own suite: 14/14 passed. `structural_index`'s own suite: 7/7 passed.
Both confirmed green before building tool 2 on top of them (tool 2 does not actually depend on
either — it reuses `scripts/decode_rawmessageinfo.py` instead — but both were re-run anyway per this
resumption's own explicit Phase 0 instruction).

**Tool 2 — protobuf/`RawMessageInfo` schema batch-extractor.** Built per
`reverse-engineering/tools/schema_batch_extractor/SPEC.md` (template:
`reverse-engineering/tools/structural_index/SPEC.md`). Generalizes
`scripts/decode_rawmessageinfo.py` (imported directly via a `sys.path` insertion to the repo-root
`scripts/` directory, not modified or reimplemented) into a `scan`/`refs` CLI: `scan` walks the whole
`jadx-output/sources/` tree (12,545 files, a cheap `"new naa(" in text` substring pre-filter before
the real per-file parse — full scan runs in well under a second), decodes every matching class's
full field-level schema, and builds a register; `refs` derives, from the register's own forward
`message_refs`, which other classes' schemas reference a given class via a oneof/repeated-list/map
field — a schema-string-level reference query, genuinely complementary to (not a duplicate of)
`structural_index`'s own bytecode field-descriptor query, with one disclosed, tested limitation:
protobuf-lite's own compact schema string carries no class reference at all for a *plain* singular
`MESSAGE`/`GROUP` field (only for oneof/list/map shapes) — so `refs` cannot recover, e.g., that `nef`
holds `ndi` as a plain field (that finding still requires `structural_index refs --class ndi`, as
`ai-sessions/0025`'s first pass already established). This limitation is disclosed in `SPEC.md` §3/§9
and covered by its own regression test (`TestDisclosedLimitation`) rather than being a silent gap.

**Test results**: `.venv/bin/python3 -m pytest tests/ -v` → 12 passed, 0 failed, on the very first
run against the real APK — no test needed adjustment after seeing real data, since every expected
value was independently derived first by running `scripts/decode_rawmessageinfo.py` directly against
`qhr.java`/`qjc.java`/`qja.java`/`nqx.java`/`qjb.java` before any test was written (the exact byte-for-byte
outputs are quoted in `SPEC.md` §10). All 6 of `SPEC.md` §10's acceptance criteria pass:

1. `qhr`: 38 fields, 1 oneof, range [1,38], `message_refs` = `[qju, qht, qjw, qhq, qiq, qjf, qis]`.
2. `qjc`/`qja`: both 5 fields, 1 oneof, `message_refs` = `[qhx, qjn, qjt, qhr, qjv]`.
3. `nqx`: 7 fields, 0 oneofs, no message refs at all — `refs --class nqx` returns empty.
4. `qjb`: 4 fields, 1 oneof, sparse range [3,6].
5. Whole-tree: 807 candidates, 807 decoded, 0 unparsable — matches
   `REVERSE_ENGINEERING.md`'s own independently-obtained header-only-sweep count exactly.
6. Disclosed-limitation regression: `refs --class ndi` returns `[]` despite `nef` genuinely holding
   `ndi` as a plain field — confirmed as the documented, correct behavior, not a bug.

**Governance**: mechanical assistance only — confirmed by reading `batch_extract.py`/`cli.py` in
full: no write path to `REVERSE_ENGINEERING.md`/`PROTOCOL.md`/`DECISIONS.md`/any
`CAP-NNN-FINDINGS.md` exists anywhere in the tool. `git add -n reverse-engineering/tools/schema_batch_extractor/`
confirms only `SPEC.md`/`README.md`/`pyproject.toml`/`src/`/`tests/` would be staged — `.venv/`/caches
are ignored by the existing `.gitignore` glob (`reverse-engineering/tools/*/.venv/`) without any
change needed. `TODO.md`'s Phase 2 section and `reverse-engineering/tools/BACKLOG.md`'s own entry
(plus its 2026-09-16 re-ordering blockquote) both updated to record the tool as implemented.

**Used for real, on Phase 3 item H (the 12 candidate rich schemas)**:
`schema-batch-extractor scan --class nhm --class nef --class qaa --class ndi --class mtn --class nca
--class gdw --class nfh --class msw --class qaj --class qbu --class qar` recovered full field-level
schemas for all 12 (previously only header counts — field/oneof/map counts — were known, from a
one-off, uncommitted header-only script). Running `refs` against each of the 12 in turn found:

- **`msw`'s sole incoming schema reference is `mtn`, field 12, context `oneof`** — i.e. `mtn`'s own
  field 12 *is* the `msw` alternative. This narrows the 12-candidate "independent roots" count by one
  more (after the already-known `nef`⊃`ndi`/`nca` and `nhm`⊃`nfh` nestings from `structural_index`'s
  first pass), via a different, complementary data source (the schema string's own oneof encoding,
  not a bytecode field-descriptor scan).
- **`mtn` is itself held as a repeated (`MESSAGE_LIST`) field of three small, previously-uncatalogued
  protobuf message classes**: `mqm` (2 fields, its field 5), `mra` (1 field, its field 1), `mqk`
  (4 fields, its field 7). `mqk` additionally references a fourth new class, `mtg`, on one of its own
  fields — not traced further. None of `mqm`/`mra`/`mqk` has any incoming schema reference of its own
  (`refs` on each returns empty) — the chain terminates here, via this data source.
- **`qaj` is itself held as a repeated field of a new class, `qak`** (2 fields, its field 1); `qaj`'s
  own field 29 (oneof) references a fifth new class, `qaz`; `qak` also references a sixth new class,
  `qam`. Neither `qak`/`qaz`/`qam` traced further.
- **`qar`'s field 16 is a `MAP` field whose default-entry descriptor is `qaq.a`** (a nested class) —
  the only one of the 12 with a map field, matching the original header-only sweep's own note.
- **`nhm`/`nef`/`qaa`/`ndi`/`nca`/`gdw`/`nfh`/`qbu` show zero incoming schema-level references** —
  consistent with (not contradicting) `structural_index`'s own bytecode-level "zero construction
  sites" finding for the same 12, since these are two different query shapes over two different data
  sources (this one cannot see plain-field references at all, per its own disclosed limitation).

7 genuinely new class leads surfaced (`mqm`/`mra`/`mqk`/`mtg`/`qak`/`qaz`/`qam`) — none catalogued
anywhere in `REVERSE_ENGINEERING.md` before this pass (`grep -n` for each returned zero hits before
this session's own edit), none traced further this pass, per this prompt's own "don't chase every
tangent to full depth" instruction. Recorded in `REVERSE_ENGINEERING.md`'s "Candidate rich schemas"
section as a new dated update, 🟢 FACT for code existence/structure (mechanical, tested decode),
explicitly not any protocol-behavior claim.

**Tool 3 — UUID extraction + BLE/GATT context reconstruction.** Built per
`reverse-engineering/tools/uuid_ble_context/SPEC.md` (template:
`reverse-engineering/tools/schema_batch_extractor/SPEC.md`, the way that tool followed
`reverse-engineering/tools/structural_index/SPEC.md`). Runs a genuinely unseeded, blind regex sweep
of the whole `jadx-output/sources/` tree for every UUID-shaped literal (`extract`), tags each
occurrence's own source file with a textual BLE/GATT/RFCOMM-API co-occurrence signal (a fixed name
list — `android.bluetooth.*`/`BluetoothGatt`/`BluetoothDevice`/`UUID.fromString`/etc., never a call-
graph trace), and reuses `structural_index`'s own `find_refs`/`load_apk` directly (a two-hop reuse
chain down to `lambda_dispatcher_resolver`'s Layer 1, imported via a `sys.path` insertion, not
re-implemented) for the "usage location" half (`context`).

**Test results**: `.venv/bin/python3 -m pytest tests/ -v` → 9 passed, 0 failed, against the real APK.
All 6 of `reverse-engineering/tools/uuid_ble_context/SPEC.md` §10's acceptance criteria pass:

1. `extract` reproduces the 5 already-known register UUIDs' literal forms exactly
   (`25e97ff7-24ce-4c4c-8951-f764a708f7b5`, `099775cb-7e0d-3465-5576-d2246d6f043a`,
   `3a046f6d-24d2-7655-6534-0d7ecb759709`, `b5f708a7-64f7-5189-4c4c-ce24f77fe925`,
   `00001124-0000-1000-8000-00805f9b34fb`).
2. Whole-tree count: `total_uuids_found: 6` — the 5 above plus the one genuinely new find.
3. The new, non-Bluetooth find is a confirmed checked negative: `context --uuid
   95ed6082-b8e9-46e8-a73f-ff56f00f5d9d` returns exactly two occurrences, both in
   `defpackage/ehs.java`, both `bt_api_cooccurrence: false`.
4. `fzd`'s own usage graph matches a direct `structural_index refs --class fzd` run exactly
   (`calls: 11`, `field_type_holders: 6`).
5. The real discriminating co-occurrence pair: `fzd.java`/`gbm.java` both show
   `bt_api_cooccurrence: true`; `fqg.java` — genuinely Bluetooth-adjacent (it wires the same RFCOMM
   UUID constant through) but never naming a BT API directly in its own file text — shows `false`,
   demonstrating the heuristic's own disclosed textual-not-semantic precision limit.
6. Unknown UUID in `context` is a hard, non-zero-exit error.

**Governance**: mechanical assistance only — confirmed by reading `uuid_scan.py`/`cli.py` in full:
no write path to `REVERSE_ENGINEERING.md`/`PROTOCOL.md`/`DECISIONS.md`/any `CAP-NNN-FINDINGS.md`
exists anywhere in the tool. `git add -n reverse-engineering/tools/uuid_ble_context/` confirms only
`reverse-engineering/tools/uuid_ble_context/SPEC.md`/`README.md`/`pyproject.toml`/`src/`/`tests/` would be staged — `.venv/`/caches are ignored
by the existing `.gitignore` glob. `TODO.md`'s Phase 2 section and
`reverse-engineering/tools/BACKLOG.md`'s own entry both updated to record the tool as implemented.

**Used for real, on the whole-tree UUID sweep**: `uuid-ble-context extract --apk-root ...` found
exactly 6 distinct UUID-shaped literals in this APK version — the 5 already-registered forms
(reconfirmed byte-for-byte against `REVERSE_ENGINEERING.md`'s own UUID register), plus one
genuinely new, non-Bluetooth find: `95ed6082-b8e9-46e8-a73f-ff56f00f5d9d`, an AndroidX WorkManager
`Data`-serialization internal sentinel string, appearing exactly twice (a read-side and a write-side
use) in `defpackage/ehs.java` — a checked negative, genuinely examined and confirmed not
Bluetooth-relevant, not silently skipped. `context --uuid 25e97ff7-...` additionally surfaced a
real, disclosed limitation of the co-occurrence heuristic itself: `fqg.java` (the already-known
`gbb`/`gbc` construction-wiring class) is genuinely Bluetooth-adjacent but shows
`bt_api_cooccurrence: false`, since it wires the UUID constant through without ever naming a BT API
directly in its own file text — a real-data demonstration of a textual (not semantic) check's own
disclosed boundary, not a bug. Recorded in `REVERSE_ENGINEERING.md`'s UUID register as a new dated
update, 🟢 FACT for code existence/structure, explicitly not any protocol-behavior claim.

**Tool 4 — limited dataflow analysis.** Built per `reverse-engineering/tools/limited_dataflow/SPEC.md`
(template: `reverse-engineering/tools/uuid_ble_context/SPEC.md`, the way that tool followed
`reverse-engineering/tools/schema_batch_extractor/SPEC.md`), exactly the risk-bounded scope
`reverse-engineering/tools/BACKLOG.md`'s own text for this idea already prescribed ("scope the first
version to straight-line, single-basic-block flows only"). Traces one register forward, instruction
by instruction, through a resolved dispatcher branch (`trace-branch`) or a plain method body
(`trace-method`) — reusing `lambda_dispatcher_resolver`'s own `analyze_class`/`smali_reader`
directly via a `sys.path` insertion, not re-implemented — recognizing exactly four shapes: alias
(`move-object`), cast (`check-cast`), sink use (appearing as an `invoke-*` argument), and a
basic-block boundary (label/`goto`/`if-*`/`packed-switch`/`sparse-switch`). Anything else that names
the traced register is an explicit stop (`ambiguous_redefinition`), never a guess.

**`reverse-engineering/tools/limited_dataflow/SPEC.md`'s own tests/ directory did not exist yet when this sub-continuation began** — `reverse-engineering/tools/limited_dataflow/SPEC.md`
(282 lines) and `README.md` (66 lines) were already substantial, and `src/limited_dataflow/{cli.py,
dataflow.py, models.py}` were already fully implemented, but the prior pass was interrupted (Claude
usage rate limit) before writing `tests/test_dataflow.py`. This sub-continuation wrote that test
file, running the tool against the real APK first to independently derive every expected value
before writing the matching assertion — the same discipline `schema_batch_extractor`'s own test
file used.

**Two of `reverse-engineering/tools/limited_dataflow/SPEC.md` §10's own acceptance-criteria *descriptions* were found wrong against the real
APK while doing this, and corrected in `reverse-engineering/tools/limited_dataflow/SPEC.md` itself rather than weakened into a test that just
asserts whatever the code happens to output:**

1. Item 1 (the primary `esk` discriminator-19 fixture) claimed `final_status:
   "reached_end_of_block"` for the `v0` trace. The real tool, run against the real APK, produces
   `"ambiguous_redefinition"` instead: after the documented `cast`-to-`qjc` step (line 333) and
   `sink_use` into `Lfys;-><init>` (line 337), the *same* `v0` register slot is legitimately reused
   later in the same branch by `const-wide/16 v0, 0x5` (line 386 — the RPC send's own 5-second
   timeout setup) — a real, unrelated redefinition, correctly caught by `reverse-engineering/tools/limited_dataflow/SPEC.md` §3's own row "-"
   guardrail. The tool's behavior is correct per its own rules; only the SPEC's *stated expected
   outcome* was wrong. Corrected in `reverse-engineering/tools/limited_dataflow/SPEC.md` §6/§10 with the real line numbers and outcome.
2. Item 2 (the adversarial basic-block-boundary fixture) named `p1` as the register to trace. Against
   the real APK, `p1` never reaches a label/branch boundary at all — it is reassigned by
   `move-result-object p1` (line 347, well before any label), itself a correct but *different* stop
   (`ambiguous_redefinition`), not the `left_basic_block_scope` outcome this item means to exercise.
   `v1` — defined by the branch's own `new-instance v1, Lfys;` at line 329, the very wrapper object
   item 1's `sink_use` step constructs — survives unredefined all the way to the next `:pswitch_1`
   label at line 442. Corrected in `reverse-engineering/tools/limited_dataflow/SPEC.md` §10 to name `v1`, with the reasoning spelled out.

**Test results**: `.venv/bin/python3 -m pytest tests/ -v` → **10 passed, 0 failed**, against the real
APK, on the first run after the two corrections above (every expected value was independently
verified by running the CLI directly against the real APK before being written into a test
assertion). 6 cases exercise the real APK (skip gracefully if absent); 4 are synthetic, hand-written
smali-line fixtures (per `reverse-engineering/tools/limited_dataflow/SPEC.md` §10 item 3's own framing — not decompiled APK content) that
always run regardless:

1. `trace-branch --class esk --discriminator 19`, `v0` from line 325: `cast`→`qjc` (333), `sink_use`
   into `Lfys;-><init>` (337, `arg_position: 1`), never reaching `nqo`/`e`, then
   `ambiguous_redefinition` at line 386 (see correction 1 above).
2. `trace-method --class esk --method a`, `v1` from line 329: three `sink_use` steps (337, 342, 394),
   `left_basic_block_scope` at line 442's `:pswitch_1` (see correction 2 above).
3. Out-of-range `--start-line` and a start-line/register mismatch both raise `ValueError` with the
   exact expected message, at both the `dataflow` function level and the `cli` exit-code level.
4. Synthetic fixtures: an `ambiguous_redefinition` with zero prior steps; the three alias-preserving
   shapes chained together reaching `reached_end_of_block`; `left_basic_block_scope` on both a label
   and a `goto`.

**Governance**: mechanical assistance only — confirmed by reading `dataflow.py`/`cli.py` in full: no
write path to `REVERSE_ENGINEERING.md`/`PROTOCOL.md`/`DECISIONS.md`/any `CAP-NNN-FINDINGS.md` exists
anywhere in the tool; every sink use is recorded as a bare mechanical fact (register, line, called
class/method), never a relevance judgment. `git add -n reverse-engineering/tools/limited_dataflow/`
confirms only `reverse-engineering/tools/limited_dataflow/SPEC.md`/`README.md`/`pyproject.toml`/`src/`/`tests/` would be staged — `.venv/`/caches
are ignored by the existing `.gitignore` glob. `TODO.md`'s Phase 2 section and
`reverse-engineering/tools/BACKLOG.md`'s own entry both updated to record the tool as implemented.

**Used for real, on both the regression fixture and one genuinely new candidate**: beyond the two
test fixtures above (which are themselves real, "used for real" confirmations against the real
APK, not synthetic), `trace-branch --class esk --discriminator 18 --start-line 459
--start-register p0` was run against `esk`'s discriminator 18 (the "Feature A" write site,
`REVERSE_ENGINEERING.md`'s `esk` entry, a lead `ai-sessions/0024` surfaced but did not dataflow-trace)
— result: `steps: []`, `final_status: "left_basic_block_scope"`, `stop_line: 463`, `stop_text:
"if-eqz p1, :cond_0"`. This mechanically confirms, at the bytecode level, what this document's own
prose already said in words: the device-id-carrying value (`esk.b`) never moves before the branch's
own boolean-flag check, one basic block away from where it is first read — the conditional "Feature
A" write happens (if at all) strictly inside that `if`-branch, out of this v1 tool's own disclosed
single-basic-block scope. This narrows nothing new about *what* discriminator 18 does (already
documented) — it is a mechanical cross-check using a new tool, recorded as such in
`REVERSE_ENGINEERING.md`'s `esk` entry, not a new lead.

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
built in this first pass — that was the clean tool-boundary stopping point the prompt itself names as
acceptable, chosen deliberately over starting tool 2 and leaving it half-finished, in favor of
spending the remaining room using tool 1 for real work (Phase 3). **Superseded, 2026-09-17**: all
three were subsequently built across this file's own "Resumption continued" section below (tool 2)
and its "Sub-continuation, 2026-09-17" section (tools 3-4, after an intervening rate-limit
interruption) — see the Phase status table and Phase 1 tool sub-table at the top of this file for the
current, accurate state, and the "Final summary for the maintainer" section below for the up-to-date
conclusion. This paragraph is left in place, corrected rather than deleted, as the historical record
of the first pass's own reasoning, per `PROJECT_RULES.md` §3 rule 9a's non-destructive-update
convention.

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

**1. Which of the top-4 tools were implemented, and to what state?** **Updated, 2026-09-17 (this
point was stale from the first pass — corrected here rather than left standing, per `PROJECT_RULES.md`
§3 rule 9a): all 4 tools are now fully implemented, tested against the real APK, and used for real.**
Tool 1 (structural/XREF code index): 7/7 pytest, all 3 named acceptance criteria. Tool 2 (protobuf
schema batch-extractor): 12/12 pytest, all 6 named acceptance criteria, used on all 12 of item H's
candidate rich schemas. Tool 3 (UUID/BLE-context reconstruction): 9/9 pytest, all 6 named acceptance
criteria, used for real — found 6 UUID-shaped literals (the 5 already-registered, reconfirmed, plus
one genuinely new non-Bluetooth find, an AndroidX WorkManager sentinel string in `ehs.java`). Tool 4
(limited dataflow analysis): 10/10 pytest, used to reproduce the hand-traced `esk` discriminator 19 →
`WriteSetting` chain as its regression fixture, then on discriminator 18 (the "Feature A" write),
mechanically confirming its conditional write sits one basic block past the tool's own disclosed
single-basic-block scope. See this file's own "Phase 1 tool-by-tool status" table and each tool's
narrative section (the original Phase 1 section above, the "Resumption continued" section, and the
"Sub-continuation, 2026-09-17" section) for full detail.

**2. Were the workflow recommendations adopted?** Yes, for the tooling-priority and workflow-process
scope this prompt's own authorization covers: `reverse-engineering/tools/BACKLOG.md`'s re-ordering
blockquote now reads "approved," citing this prompt; `APK_REVERSE_ENGINEERING_PROCEDURE.md`'s §4a
and its retry-strategy bullet got a short adoption-confirmation note each (neither needed a
substantive rewrite, since neither ever carried an explicit "proposal" tag to begin with — an honest
finding, not an assumed one); `ai-sessions/0024`'s own `Status` field updated to reflect this,
explicitly not extending to its still-open Phase 1 code-level leads.

**3. What did the tools find when used for real APK-RE work?** **Updated, 2026-09-17 — the tool-1
findings below are unchanged from the first pass; tools 2-4's own findings are added.** Substantial
findings across 7 of `ai-sessions/0024`'s 13 open items (see Phase 3 above for the full detail): item
F and item J are **resolved** (the `device_info` DAO relationship; `gbb`/`gbc`'s single construction
path); item C's prior "~7-day staleness gate" reading is **corrected** (it belonged to an unrelated
feature, not the `GetSoftwareInfo` trigger chain, whose real construction site — `gjy.java:38` — is
now found and confirms an OTA-apply-completion trigger); items B, G, and H are **narrowed** with new,
concretely cited leads (a checked-negative gRPC-transport-builder dead end; `qhr` field 6's caller;
the 12 candidate schemas' nesting relationships and an unattributed naming cluster); item L surfaced
one genuinely new component, `BluetoothPriorityReceiver`. Item M was not attempted (out of room).
**Tool 2** additionally recovered full field-level schemas for all 12 of item H's candidate rich
schemas and surfaced 7 genuinely new, previously-uncatalogued class leads (`mqm`/`mra`/`mqk`/`mtg`/
`qak`/`qaz`/`qam`), none traced further. **Tool 3** found one genuinely new UUID-shaped literal (an
AndroidX WorkManager sentinel, non-Bluetooth) alongside reconfirming the 5 already-known ones, and
demonstrated a disclosed real-data limitation of its own co-occurrence heuristic (a false negative on
`fqg.java`). **Tool 4** mechanically confirmed the `esk` discriminator-19 chain matches the existing
hand-trace, and traced discriminator 18 (the "Feature A" write) to show its conditional write sits one
basic block past the tool's own disclosed scope. Every finding is labeled 🟢 FACT (mechanical code
facts) or 🟡 HYPOTHESIS/🔴 OPEN QUESTION (anything interpretive), per `PROJECT_RULES.md` §1 — nothing
here is a protocol-behavior claim, so none of it needed or received a `PROTOCOL.md` 🟢 FACT promotion
or a `DECISIONS.md` ADR.

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

**6. Full list of documents updated, and what changed in each:** **Updated, 2026-09-17 — the tool-1
entries below are unchanged from the first pass; tools 2-4's own new/untracked directories and the
further doc edits from the resumption and sub-continuation are added.**

- `reverse-engineering/tools/structural_index/` (new) — own SPEC.md, README.md, pyproject.toml,
  `src/structural_index/{__init__,cli,xref_index,models}.py`, `tests/{__init__,test_xref_index}.py`.
  Tool 1 (first pass).
- `reverse-engineering/tools/schema_batch_extractor/` (new) — own SPEC.md, README.md,
  pyproject.toml, `src/schema_batch_extractor/{__init__,cli,batch_extract,models}.py`,
  `tests/{__init__,test_batch_extract}.py`. Tool 2 (resumption).
- `reverse-engineering/tools/uuid_ble_context/` (new) — own SPEC.md, README.md, pyproject.toml,
  `src/uuid_ble_context/{__init__,cli,uuid_scan,models}.py`, `tests/{__init__,test_uuid_scan}.py`.
  Tool 3 (sub-continuation).
- `reverse-engineering/tools/limited_dataflow/` (new) — own SPEC.md (corrected in place: two of §10's
  own named acceptance criteria were mis-described against the real APK and fixed, with dated
  correction notes), README.md, pyproject.toml,
  `src/limited_dataflow/{__init__,cli,dataflow,models}.py`, `tests/{__init__,test_dataflow}.py`. Tool
  4 (sub-continuation, after the mid-task rate-limit interruption).
- `.gitignore` — generalized the tool-glob comment (already covered every new tool by wildcard; only
  the comment text changed).
- `reverse-engineering/tools/BACKLOG.md` — all 4 tool ideas marked "Implemented," each pointing at its
  own SPEC.md/README.md; the 2026-09-16 re-ordering blockquote's framing changed from proposed to
  approved, citing this prompt; a further dated note added once tools 2/3 (resumption) and tool 4
  (sub-continuation) were built.
- `APK_REVERSE_ENGINEERING_PROCEDURE.md` — §4a's heading and the Notes & Gotchas retry-strategy
  bullet each got a short "adoption confirmed" note citing this session's own real re-use of both.
- `ai-sessions/0024_AUDIT_RESULT_2026_09_16.md` — `Status` field updated in place (tooling/workflow
  proposals adopted via this prompt; Phase 1 code-level leads explicitly carved out as still
  unreviewed).
- `ai-sessions/INDEX.md` — rows for `0024` and `0025` kept in sync with each `RESULT` file's own
  `Status` field across every resumption.
- `REVERSE_ENGINEERING.md` — the 7 entries the first pass added/updated (`gbb`/`gbc` resolved, item J;
  the `esk` entry's `gcp`/`gcn` resolution, item F; the `gag`/`gjv` OTA-trigger correction, item C;
  `MaestroEndpointService`'s checked negative, item B; `qhr` field 6's caller, item G; the "Candidate
  rich schemas" nesting, item H; the new `BluetoothPriorityReceiver` entry, item L), **plus**: the
  "Candidate rich schemas" section's own 12 full field-level schemas and 7 new class leads (tool 2);
  the UUID register's 2026-09-16 update (tool 3's new find); the `esk` entry's dated update recording
  both dataflow-trace confirmations, labeled 🟢 FACT (mechanical), explicitly no `PROTOCOL.md` claim
  (tool 4).
- `PROTOCOL.md` — §6 updated in 4 places by the first pass (Feature A's write-site citation; the
  field-6 register note; the `gjv.p()`/connect-time-burst trigger correction; `MaestroEndpointService`'s
  checked negative; the new `BluetoothPriorityReceiver` open item) — unchanged by tools 2-4's own
  findings, which stayed code-level-only with no corresponding `PROTOCOL.md` entry to update.
- `TODO.md` — Phase 2's `esk`/`lambda_dispatcher_resolver` entry updated (item F resolution, item B
  narrowed status) by the first pass; a bullet added for each of tools 1-4 as it was built, mirroring
  the `lambda_dispatcher_resolver` entry's own pattern.
- `ai-sessions/0025_MAINTENANCE_RESULT_2026_09_16.md` (this file) — created by the first pass,
  progressively appended to by the resumption (tool 2) and the sub-continuation (tools 3-4), and
  corrected in place on 2026-09-17 (this edit) to fold this section and the original Phase 1 section's
  closing paragraph back in line with the rest of the file, per `PROJECT_RULES.md` §3 rule 9a.

## What remains uncommitted

Everything listed above is uncommitted — no `git commit` or `git push` was run at any point this
session (including the 2026-09-17 sub-continuation), per this prompt's own explicit instruction that
session bookkeeping is manual. `git status` at the end of the sub-continuation shows every file in
the list above as modified, plus the new `reverse-engineering/tools/structural_index/`,
`reverse-engineering/tools/schema_batch_extractor/`, `reverse-engineering/tools/uuid_ble_context/`,
and `reverse-engineering/tools/limited_dataflow/` directories as untracked (source and tests only —
each tool's own `.venv/`/caches are gitignored and were never staged), plus this file and
`REVERSE_ENGINEERING.md`/`TODO.md`/`reverse-engineering/tools/BACKLOG.md`/`ai-sessions/INDEX.md`
modified for tool 4's own write-up and findings.

## Where the next session should pick up

**Superseded by the 2026-09-17 sub-continuation above — this note originally said "resume at Phase
1, tool 2." That is stale: all 4 of the top-4 `reverse-engineering/tools/BACKLOG.md` tools (structural
code index, schema batch-extractor, UUID/BLE-context reconstruction, limited dataflow) are now
built, tested against the real APK, and used for real — see the Phase 1 tool-by-tool sub-table and
each tool's own narrative section above.** No further tool-building is needed unless the maintainer
wants one of the three remaining, independently-useful `reverse-engineering/tools/BACKLOG.md` items
(none of which any of tools 1-4 depend on): the wire-payload-vs-schema auto-decoder, the APK
version-diff tool, or the tshark/DLCI-reassignment helper library.

Real-APK-RE work still open, unrelated to tool-building, carried over unchanged from the first
pass's own Phase 3 (not touched by this sub-continuation, which only closed out tools 3-4's own
bookkeeping and used tool 4 on one new candidate): item M (resource/string-table sweep, cheap, not
yet attempted); item B's actual multibinding-assembly site (would benefit from tool 1's own deferred
`implements`-query capability); item H's unattributed field-holder naming cluster
(`kii`/`koq`/`pzr`/`jau`/`msc`/`jaj`/`jjn`/`jjx`/`jkl`/`jsg`/`kip`/`kiq`/`kob`/`kol`/`qan`/`fwe`/
`nbm`) — tracing even one of these classes' own purpose would be a natural next step; the 7 new
class leads tools 2/4 surfaced (`mqm`/`mra`/`mqk`/`mtg`/`qak`/`qaz`/`qam`, and `esk` discriminator
18's own `Lffd;->f(String,Z)Lmbo;` callee, still untraced past this session's own single-basic-block
boundary — a v2 cross-method dataflow tool, not attempted here, would be the natural way to go
further); and `BluetoothPriorityReceiver`'s own trigger/effect (item L's finding), which is
capture-territory, not further static analysis, if pursued.

This file's own `Status` is `awaiting maintainer sign-off`, not `complete` — per `AGENTS.md`
§6/§15, no AI session ever self-promotes its own findings to settled without maintainer review, and
that applies here exactly as it did to tools 1-3's own findings.

---

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0025_MAINTENANCE_RESULT_2026_09_16.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0025_MAINTENANCE_RESULT_2026_09_16
