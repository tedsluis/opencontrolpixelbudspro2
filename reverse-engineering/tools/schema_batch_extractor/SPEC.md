# SPEC.md — Protobuf/`RawMessageInfo` Schema Batch-Extractor

Technical specification for a narrow, standalone tool that generalizes
`scripts/decode_rawmessageinfo.py` (which decodes one `GeneratedMessageLite` compact-schema class at
a time — this is how `qhr`/`qjc`/`qjb`/`nqx` were originally recovered, per `TODO.md`'s Phase 2 entry
and `REVERSE_ENGINEERING.md`'s "Tooling note" section) into a batch mode that scans the whole
decompiled JADX tree, decodes every matching class in one pass, and produces a full schema register.
This is `../BACKLOG.md`'s "protobuf/`RawMessageInfo` schema batch-extractor" idea, graduated out of
that file per `ai-sessions/0025`'s own resumption authorization (see that file's own citation). Built
following `../structural_index/SPEC.md` as the template, the same way that tool followed
`../lambda_dispatcher_resolver/SPEC.md`.

**Status: implemented 2026-09-16** (`src/`, `tests/`, `README.md`), against exactly this document's
own §5/§6/§10.

---

## 1. Problem statement

`scripts/decode_rawmessageinfo.py` is a correct, field-for-field port of protobuf-lite's own
`RawMessageInfo`/`MessageSchema.newSchemaForRawMessageInfo` decoding algorithm (see that script's own
header comment and `REVERSE_ENGINEERING.md`'s "Tooling note" section for the upstream-source
provenance) — but its CLI takes one or more explicit file paths and decodes only the `new naa(...)`
construction(s) it finds in each. Every class this project has recovered a real schema for so far
(`qhr`, `qjc`/`qja`, `qjb`, `nqx`, and the rest of §4 in `REVERSE_ENGINEERING.md`) was found by first
locating the class by some other means (a service-catalog string table, a call-graph trace, a
by-hand `grep`) and *then* running the script against that one file. `REVERSE_ENGINEERING.md`'s own
"Candidate rich schemas" section already shows the cost of *not* having a batch mode: a
one-off, not-committed "header-only variant" script had to be written just to rank all 807
`new naa(` constructions under `.../jadx-output/sources/defpackage/` by field count, and even that
one-off only decoded headers (field/oneof/map counts), not full field-level detail, for anything
outside the top ~12-60.

This tool removes the need for either the by-hand file-finding step or a fresh one-off script every
time: point it at the decompiled tree once, get every matching class's full field-level schema back,
in one register.

## 2. Scope

### 2.1 In scope (v1)

- **Scan** the whole decompiled JADX source tree (`jadx-output/sources/`, not just the
  `defpackage/` subdirectory — see §3's note on why the scan isn't hardcoded to one subdirectory even
  though, empirically, every match found so far lives there) for every class containing at least one
  `new naa(<default>, "<info-string>", <objectsArrayOrNull>)` construction — the exact same detection
  signature `scripts/decode_rawmessageinfo.py`'s own `find_naa_constructions` already uses,
  **reused directly, not reimplemented** (§4).
- **Decode** every match's full field-level schema (field number, wire type, oneof/map-ness, declared
  Java field name where recoverable, and — for the field shapes where the compact schema string
  itself actually carries it, see §3's scope note — the referenced message class) via
  `scripts/decode_rawmessageinfo.py`'s own `decode_info_string`, again reused directly.
- **Register**: aggregate every decoded class into one queryable set, keyed by class name, with a
  derived reverse index (`refs`, §5) of "which other classes' *own* compact schema string declares a
  message-typed reference to class X" — built purely from the forward `message_refs` this tool's own
  decode already recovers, no separate pass needed.
- Work directly against the on-disk decompiled output already present under
  `reverse-engineering/apk/<version>/jadx-output/` — no new decompilation step, no bytecode/androguard
  access at all (this tool's entire data source is JADX-decompiled Java *source text*, a different,
  cheaper data source than `structural_index`'s DEX-bytecode scan — see §4's note on why that
  distinction matters for one specific limitation, §3).

### 2.2 Explicitly out of scope (v1)

Deferred, per the same "start narrow" discipline the other two tools already model:

- **Any bytecode/DEX-level analysis.** This tool never opens `base.apk`/`.dex` — it is a pure
  JADX-source-text scanner, exactly as `scripts/decode_rawmessageinfo.py` already is. Finding which
  other class holds a given class as a plain (non-oneof, non-repeated) Java field — the query
  `structural_index refs --class <X>`'s category (c) already answers — is explicitly **not**
  duplicated here; §3 explains precisely why the compact schema string cannot answer that query for
  every field shape, and why that gap is real, not an oversight.
- **Any relevance judgment** about what a decoded field or class *means* for the Bluetooth protocol —
  see §8, identical governance to the other two tools.
- **Semantic naming.** This tool never proposes what a field represents (e.g. "field 4 is the touch
  toggle") — that requires wire-capture correlation or a call-site trace, exactly as
  `REVERSE_ENGINEERING.md`'s own existing entries already distinguish (code-structure FACT vs.
  protocol-meaning HYPOTHESIS).
- A persistent database/cache across runs — v1's output is files/stdout, same as the other two tools.
- Re-decoding/modifying `scripts/decode_rawmessageinfo.py` itself — this tool imports and calls it
  directly (§4); the single-file script keeps working exactly as before for anyone who only needs one
  class.

## 3. Target scan, defined structurally — and one real, load-bearing limitation

Per `scripts/decode_rawmessageinfo.py`'s own already-verified detection: a class is a match iff its
decompiled `.java` source contains at least one `new naa(<default_expr>, "<info_string>",
<objects_expr>)` construction where `<info_string>` is a string literal and `<objects_expr>` is
either the literal `null` or a parseable `new Object[]{...}` literal (§7 covers what happens when it
isn't parseable). This is the identical shape `scripts/decode_rawmessageinfo.py`'s existing
`find_naa_constructions`/`parse_objects_arg` already recognize — not a new or looser pattern.

**Why the scan walks the whole `jadx-output/sources/` tree, not just `defpackage/`:** every match
found in this APK version happens to live under `defpackage/` (JADX's default-package folder for
R8-obfuscated classes with no real package name — confirmed empirically, 807/807 matches, zero
elsewhere in a full-tree check performed while building this tool). A future APK version, or a class
that keeps a real package name, could put a matching class anywhere else in the tree; hardcoding the
scan to one subdirectory would silently miss it. The cost of scanning the full ~12,500-file tree
instead of one subdirectory is negligible (a cheap `"new naa(" in text` substring pre-filter before
the more expensive regex/brace-matching parse — see §4) — sub-second for the full tree.

**Real limitation, not a bug, disclosed up front rather than discovered by a future session:** the
compact `RawMessageInfo` schema string does **not** encode the referenced message class for a plain
singular `MESSAGE`/`GROUP`-typed field (protobuf-lite's own `newSchemaForRawMessageInfo` algorithm
consumes *zero* extra `objects[]` entries for that specific field shape — see
`scripts/decode_rawmessageinfo.py`'s own comment at the `field_type in (9, 17): pass` branch, ported
verbatim from the upstream algorithm, not this tool's own choice). It **does** encode the referenced
class for three other shapes this tool still recovers correctly: a `oneof` alternative typed
`MESSAGE`/`GROUP` (e.g. `qjc` field 4 → `qhr`), a repeated `MESSAGE_LIST`/`GROUP_LIST` field, and a
`MAP` field's default-entry descriptor. Concretely, this means `schema_batch_extractor refs
--class ndi` (§5) correctly returns **empty**, even though `nef` genuinely holds `ndi` as a plain
`MESSAGE`-typed field (`nef.v`) — the compact schema string this tool decodes simply does not carry
that fact for a plain field; only a bytecode/field-descriptor scan (`structural_index refs --class
ndi`'s category (c), already used for exactly this in `ai-sessions/0025`'s first pass) can. §9
explains why this tool does not also implement that separate lookup shape, and §5's own worked
example (item H) demonstrates and records this exact gap rather than silently under-reporting.

## 4. Architecture — one thin batch layer, reusing `scripts/decode_rawmessageinfo.py` directly

```
┌───────────────────────────────────────────────────────────────────┐
│  scripts/decode_rawmessageinfo.py                                   │
│  — imported directly (sys.path insert to the repo-root scripts/     │
│    directory), not re-implemented, not modified — gives per-file    │
│    `find_naa_constructions`/`decode_file`/`decode_info_string`,     │
│    already validated against this project's own trivial-marker-     │
│    type fixtures (`nia`/`qib`) before this tool ever trusted it.    │
└───────────────────────────────────────┬───────────────────────────┘
                                          │ one decoded schema dict per `new naa(...)` found
┌───────────────────────────────────────▼───────────────────────────┐
│  schema_batch_extractor.batch_extract                               │
│  - walks jadx-output/sources/**/*.java, cheap substring pre-filter  │
│    ("new naa(" in text) before the real parse, per file             │
│  - derives each match's class name from its path (JADX's own       │
│    package-to-directory convention; "defpackage.X" for the          │
│    default-package folder, dotted-package form otherwise)           │
│  - normalizes each decoded field's message-class reference (where   │
│    the info string actually carries one, §3) by stripping the       │
│    ".class" source-text suffix                                      │
│  - builds the register + the reverse "who references X" index from  │
│    the forward references already recovered — one extra pass, no    │
│    new parsing                                                      │
└───────────────────────────────────────────────────────────────────┘
```

**Reuse, stated precisely:** `schema_batch_extractor.batch_extract` imports
`decode_rawmessageinfo.decode_file` and `.decode_info_string` directly via a `sys.path` insertion to
the repo-root `scripts/` directory — the single-file script is not copied, forked, or modified.
Anyone who only needs one class can keep using
`python3 scripts/decode_rawmessageinfo.py <file.java>` exactly as before; this tool is a batch
wrapper around the same, unchanged decoding logic, mirroring how `structural_index` wraps
`lambda_dispatcher_resolver`'s Layer 1 without touching it.

## 5. Interface (CLI)

Two commands:

```
schema-batch-extractor scan \
  --apk-root reverse-engineering/apk/v1.0.955078536-10253511 \
  [--class <name1> [--class <name2> ...]]   # optional: filter the register to just these classes
  [--min-field-count N]                      # optional: only classes with >= N fields (the "rich
                                              #   schema" ranked-list use case, REVERSE_ENGINEERING.md's
                                              #   own "Candidate rich schemas" section)
  [--output-dir <dir>]                       # omit for JSON on stdout

schema-batch-extractor refs \
  --apk-root reverse-engineering/apk/v1.0.955078536-10253511 \
  --class <name> \
  [--output-dir <dir>]
```

- `scan` always runs a full tree scan (§3/§4) — `--class`/`--min-field-count` filter the *output*,
  never a shortcut that skips decoding everything else, so the register stays internally consistent
  run to run.
- `refs` runs `scan`'s full decode once, then returns every class in the register whose own decoded
  schema declares a message-typed reference (oneof, list, or map — §3) to the target class. Per §3's
  disclosed limitation, an empty result does **not** mean "nothing in the APK holds this class" — it
  means "no *oneof/list/map* schema field encodes this class as its type"; a plain singular
  `MESSAGE` field pointing at the same class would not appear here (use `structural_index refs` for
  that).

## 6. Output format

Plain JSON, same governance as the other two tools (§8) — no database in v1.

```json
{
  "apk_root": "reverse-engineering/apk/v1.0.955078536-10253511",
  "total_java_files_scanned": 12545,
  "total_candidates": 807,
  "total_decoded": 807,
  "unparsable": [],
  "classes": [
    {
      "class": "defpackage.qhr",
      "file": "reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/qhr.java",
      "field_count": 38,
      "oneof_count": 1,
      "map_field_count": 0,
      "min_field_number": 1,
      "max_field_number": 38,
      "fields": [
        {"field_number": 1, "type_name": "BOOL", "is_oneof": true, "oneof_index": 0, "message_ref": null, "java_field": null, "has_presence": false},
        {"field_number": 7, "type_name": "MESSAGE", "is_oneof": true, "oneof_index": 0, "message_ref": "qju", "java_field": null, "has_presence": false}
      ],
      "message_refs": ["qju", "qht", "qjw", "qhq", "qiq", "qjf", "qis"]
    }
  ]
}
```

`refs` returns:

```json
{
  "class": "defpackage.qju",
  "referenced_by": [
    {"class": "defpackage.qhr", "field_number": 7, "context": "oneof"}
  ]
}
```

This is a **candidate-evidence bundle**, not a research finding — same framing as the other two
tools' §8. A decoded field list is a mechanical fact about the compiled code; what any field number
*means* for the protocol is a separate, later judgment requiring wire correlation, exactly as
`REVERSE_ENGINEERING.md`'s own existing `qhr` entry already models.

## 7. Explicit failure modes (never silent)

| Situation | Required behavior |
|---|---|
| `--class` given to `scan`/`refs` but that class isn't in the register (no `new naa(...)` match found for it anywhere in the tree) | Non-zero exit, clear error naming the class and APK root searched. |
| A `new naa(` construction is found in a file, but its 3rd argument (`objects`) is not `null` and not a parseable `new Object[]{...}` literal | Not silently dropped: recorded in the top-level `unparsable` list (file path + a byte offset), and excluded from `classes`/`total_decoded` — `total_candidates` still counts it, so `total_candidates != total_decoded` is a visible, honest signal something was skipped, never silently absorbed into a lower total with no explanation. |
| `refs --class X` where X exists in the register but nothing else references it via oneof/list/map | Not an error — a real, empty-but-valid `referenced_by: []` result, exactly the same "legitimate zero" framing `structural_index`'s own §7 already uses for its `unreferenced` command. |
| A `.java` file cannot be read (encoding error, permission) | Skipped with a warning to stderr (file path), scan continues for every other file — never aborts the whole run for one bad file. |

## 8. Governance (binding, not optional)

Identical boundary to the other two tools' own §8/§8-equivalent sections and `../BACKLOG.md`'s
"Governance that applies to every idea below, unconditionally" section:

- Never writes to `REVERSE_ENGINEERING.md`, `PROTOCOL.md`, `DECISIONS.md`, or any
  `CAP-NNN-FINDINGS.md`.
- Never labels a decoded field's relevance or meaning — output is a candidate-evidence bundle (this
  class's compiled schema shape, here's its fields), never a FACT/HYPOTHESIS/ASSUMPTION/OPEN
  QUESTION judgment about protocol behavior.
- No modification to anything under `reverse-engineering/apk/` or to `scripts/decode_rawmessageinfo.py`
  itself — read-only.
- Fully reproducible: the same `(apk version, class filter)` always produces the same output.
- No decompiled APK content is ever committed, in source, tests, or fixtures (§11).

## 9. Why the scope stops here

Same reasoning as the other two tools' own §9: this tool is one data source (JADX-decompiled source
text, decoded via the already-proven `scripts/decode_rawmessageinfo.py` algorithm) turned into a
standing batch query, nothing more. The bytecode/field-descriptor query that would close §3's
disclosed limitation (plain-`MESSAGE`-field type resolution) already exists, in a different tool,
built on a different data source (`structural_index refs`'s category (c)) — duplicating that logic
here, on top of a data source that fundamentally cannot answer it for every field shape, would not
close the gap, only obscure that a gap exists. The right fix, if ever pursued, is a small follow-up
that cross-references this tool's own `classes` register against `structural_index`'s field-holder
query for exactly the plain-`MESSAGE` fields this tool cannot resolve on its own — a natural, but
explicitly deferred, v2 idea, not built here.

## 10. Test plan / acceptance criteria

Per this session's own resumption instructions, the primary acceptance bar is reproducing the
already-known `qhr`/`qjc`/`qjb`/`nqx` schemas **exactly** (same field count, same field-type/reference
info) before this tool is trusted on anything new:

1. **`qhr`**: `scan --class qhr` must return `field_count: 38`, `oneof_count: 1`,
   `min_field_number: 1`, `max_field_number: 38`, and `message_refs` containing exactly
   `qju`, `qht`, `qjw` (twice, fields 16 and 18), `qhq`, `qiq`, `qjf`, `qis` — matching
   `REVERSE_ENGINEERING.md`'s own `qhr` entry's field register and this session's own direct
   `python3 scripts/decode_rawmessageinfo.py qhr.java` re-run, byte-for-byte.
2. **`qjc`/`qja`**: both must decode to `field_count: 5`, `oneof_count: 1`, with `message_refs`
   `["qhx", "qjn", "qjt", "qhr", "qjv"]` in field-number order — matching
   `REVERSE_ENGINEERING.md`'s own `qjc`/`qja` entry.
3. **`nqx`**: `field_count: 7`, `oneof_count: 0`, **no** oneof/message-typed fields (its 7 fields are
   `ENUM`/`UINT32`/`FIXED32`×2/`BYTES`/`UINT32`×2, per `RpcPacket`'s own confirmed shape) — `refs
   --class nqx` from any other class in this APK's register must return `referenced_by: []` (`nqx`
   is populated by hand-written code, per `REVERSE_ENGINEERING.md`'s `npy` entry, not referenced as a
   nested message field of another `RawMessageInfo`-shaped class).
4. **`qjb`**: `field_count: 4`, `oneof_count: 1`, `min_field_number: 3`, `max_field_number: 6`
   (a genuinely sparse field-number range, unlike the other three) — matching this session's own
   direct script re-run and `REVERSE_ENGINEERING.md`'s `qjb` entry.
5. **Whole-tree count check**: `scan` with no filter must return `total_candidates: 807`,
   `total_decoded: 807`, `unparsable: []` — matching `REVERSE_ENGINEERING.md`'s own "Candidate rich
   schemas" section's independently-obtained 807-construction count from its one-off header-only
   sweep script, now reproduced by a real, committed, tested tool instead.
6. **Disclosed-limitation regression** (§3): `refs --class ndi` must return `referenced_by: []`
   even though `ndi` is genuinely held as a plain field of `nef` (confirmed independently via
   `structural_index refs --class ndi`, `ai-sessions/0025`'s first pass) — this is the expected,
   documented behavior, not a bug, and a test asserting it stays that way protects against a future
   change accidentally "fixing" it into a false positive that claims a message-ref this data source
   cannot actually support.

No regression test may be marked passing by inspection alone — each asserts the exact expected
result set, per this project's own `AGENTS.md` §11 fixture discipline.

## 11. Directory layout and git-tracking boundary

```
reverse-engineering/
└── tools/
    └── schema_batch_extractor/
        ├── SPEC.md              (this file)
        ├── README.md
        ├── pyproject.toml
        ├── .venv/               (gitignored)
        ├── src/
        │   └── schema_batch_extractor/
        │       ├── __init__.py
        │       ├── cli.py
        │       ├── batch_extract.py
        │       └── models.py
        └── tests/
            └── test_batch_extract.py
```

Source code (everything under `src/`, `pyproject.toml`, this `SPEC.md`, `README.md`) is git-tracked
normally — original tooling code, not derived APK content, so `PROJECT_RULES.md` §8 rule 20 does not
apply to it. `tests/test_batch_extract.py` reads directly from the maintainer's own
locally-decompiled `reverse-engineering/apk/v1.0.955078536-10253511/` tree (already gitignored in
full) and the entire suite is skipped — not failed — when that tree isn't present, exactly the
pattern `lambda_dispatcher_resolver/tests/test_resolver.py` and `structural_index/tests/test_xref_index.py`
already established. `.venv/` is gitignored for the same reason the other two tools' own venvs are.

---

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/reverse-engineering/tools/schema_batch_extractor/SPEC.md - https://tedsluis.github.io/opencontrolpixelbudspro2/reverse-engineering/tools/schema_batch_extractor/SPEC
