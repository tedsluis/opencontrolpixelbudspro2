# SPEC.md — UUID Extraction + BLE/GATT Context Reconstruction

Technical specification for a narrow, standalone tool that finds every UUID-shaped string literal
across the whole decompiled companion APK, and — for each one — reconstructs its usage context: the
class(es) that declare it, whether that same source file textually co-occurs with any
Bluetooth-GATT/RFCOMM-relevant API name, and (built directly on top of `../structural_index/`, not
re-derived) who else in the APK references the owning class. This is `../BACKLOG.md`'s "UUID
extraction + BLE/GATT context reconstruction" idea, graduated out of that file per `ai-sessions/0025`'s
own resumption authorization. Built following `../schema_batch_extractor/SPEC.md`'s own template, the
same way that tool followed `../structural_index/SPEC.md`.

**Status: implemented 2026-09-16** (`src/`, `tests/`, `README.md`), against exactly this document's
own §5/§6/§10.

---

## 1. Problem statement

`REVERSE_ENGINEERING.md`'s own "UUID register" section (§UUID register) and "Full-tree GATT/BLE
reference sweep" entry were both built by hand: a targeted `grep` for the two RFCOMM UUIDs already
known from `fzd.java`'s own selection logic (`ADR-018`), a separate targeted `grep -rli "fe2c"` for
the Fast Pair GATT service UUID, and a separate `grep -ril` pass for two specific GATT handle numbers
from a capture. Each of these searches was seeded by a UUID or keyword a researcher already had in
mind — none of them was a genuinely blind, exhaustive "find every UUID-shaped literal anywhere in
this APK, whatever it is" sweep. This tool runs that blind sweep once, mechanically, and — for every
UUID it finds — answers the two follow-up questions a researcher would otherwise ask by hand for
each one: "does this UUID's own file look Bluetooth-related at all?" and "who else in the APK
references the class that declares it?"

**A genuinely new finding surfaced while designing this tool, before a single line of it was
written** (an unavoidable side effect of prototyping the extraction regex against the real tree to
validate the approach, not a separate ad hoc search): the exhaustive sweep finds **6** distinct
UUID-shaped literals in this APK version, not the 3 already registered in `REVERSE_ENGINEERING.md`'s
UUID register (the two RFCOMM UUIDs' own byte-reversed alias forms already double-count as "the same
UUID" for register purposes, so 3 registered UUIDs = 5 of the 6 literal strings). The 6th,
`95ed6082-b8e9-46e8-a73f-ff56f00f5d9d`, is **not** Bluetooth-related — see §10's worked example for
the full trace — but its existence is itself the point: nothing before this tool had run a genuinely
unseeded sweep, so this specific string was simply never looked at.

## 2. Scope

### 2.1 In scope (v1)

- **Extract**: scan every `.java` file under `jadx-output/sources/` for every string-literal
  substring matching the canonical 128-bit UUID textual form
  (`[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}`, case-insensitive),
  record every occurrence (file, line number, the class the file belongs to, and the raw source
  line), and deduplicate case-insensitively into one canonical (lower-case) register entry per UUID.
- **Tag textual BLE/GATT/RFCOMM co-occurrence**: for each UUID's owning file(s), a cheap substring
  check against a fixed, named list of Bluetooth-relevant API/class names (§3) — a **textual
  co-occurrence signal**, not a call-graph trace, stated plainly as such (§8).
- **Usage-location graph, reusing `../structural_index/` directly**: for each class that declares a
  UUID (derived from the file path, same convention as `../schema_batch_extractor/`), call
  `structural_index.xref_index.find_refs` (imported directly, not re-implemented — §4) to report
  every other class/method that constructs it, calls one of its methods, or holds it as a field
  type — the exact "usage location" half `../BACKLOG.md`'s own text says this tool should not
  re-derive.
- Work directly against the on-disk decompiled output already present under
  `reverse-engineering/apk/<version>/jadx-output/` — no new decompilation step, no bytecode access of
  its own beyond what `structural_index` already provides.

### 2.2 Explicitly out of scope (v1)

- **Byte-reversed-alias detection.** `fzd.java`'s own two RFCOMM UUIDs each have a byte-reversed
  form present as a *separate* string literal in the source (both forms are written out by hand in
  the decompiled code, confirmed by direct inspection — `fzd.java:9`) — this tool finds both as two
  distinct register entries, exactly as it finds any other UUID-shaped literal, and does **not**
  attempt to detect or fold together a byte-reversed pair automatically. That pairing is a
  Bluetooth-protocol-domain judgment (the RFCOMM UUID big-endian/little-endian convention question,
  `PROTOCOL.md`'s own territory), not a mechanical string-matching one — see §8.
- **Any relevance judgment.** The tool never decides whether a found UUID is "Bluetooth-relevant" —
  it reports the textual co-occurrence signal (§3) as a fact about the surrounding file, and leaves
  the actual judgment (as §1's own worked example already models) to whoever reads the output.
- **GATT characteristic/service semantic mapping** (e.g. "this UUID is the Battery Level
  characteristic") — that is a wire-capture-correlation task (`PROTOCOL.md`'s own domain), never
  derivable from APK source alone, and this tool makes no attempt at it.
- **16-bit/32-bit "short form" Bluetooth SIG UUID detection** (e.g. a bare `0x180F` int literal with
  no surrounding 128-bit UUID text) — `REVERSE_ENGINEERING.md`'s own UUID register shows every UUID
  found in this APK so far is written out in full 128-bit textual form even for officially-short
  UUIDs (`00001124-0000-1000-8000-00805f9b34fb`, the Bluetooth-SIG-assigned-base-UUID expansion of
  short form `0x1124`) — so v1 does not add a separate short-form detector; deferred if a future APK
  version is found to use bare short-form int literals instead.
- A persistent database/cache across runs — v1's output is files/stdout, matching the other tools.

## 3. The fixed BLE/GATT/RFCOMM API-name list (textual co-occurrence signal)

A UUID's owning file is tagged `bt_api_cooccurrence: true` iff the same file's raw text contains at
least one of these literal substrings (the exact same class/method names
`REVERSE_ENGINEERING.md`'s own "Full-tree GATT/BLE reference sweep" entry and `AGENTS.md`
§13's "Reverse engineering the APK" workflow already name as the standing keyword list for this kind
of search — not a new vocabulary invented for this tool):

```
BluetoothGatt, BluetoothGattCallback, BluetoothGattCharacteristic, BluetoothGattService,
BluetoothSocket, BluetoothDevice, BluetoothAdapter, BluetoothManager, BluetoothHeadset,
BluetoothServerSocket, fetchUuidsWithSdp, createRfcommSocketToServiceRecord,
createInsecureRfcommSocketToServiceRecord, UUID.fromString
```

This is a **plain substring check on the file's own text**, run once per file already read for the
extraction pass (§4) — not a second file read, not a call-graph trace. A file matching zero of these
strings is tagged `bt_api_cooccurrence: false`, which is itself informative (§1's worked example: the
file holding the 6th, WorkManager-internal UUID matches none of them).

## 4. Architecture — one thin layer, reusing `structural_index` for the usage-location half

```
┌───────────────────────────────────────────────────────────────────┐
│  Pass 1 — regex extraction over jadx-output/sources/**/*.java      │
│  (this tool's own code, not reused from elsewhere — a single       │
│  compiled regex + a per-file line scan, comparable in cost to      │
│  schema_batch_extractor's own "new naa(" substring pre-filter)     │
└───────────────────────────────────────┬───────────────────────────┘
                                          │ UUID -> [(file, line, owning class, raw line)]
┌───────────────────────────────────────▼───────────────────────────┐
│  Pass 2 — §3's fixed-list substring co-occurrence check, per file  │
│  already read in Pass 1 (no second read)                           │
└───────────────────────────────────────┬───────────────────────────┘
                                          │ + bt_api_cooccurrence: bool, per file
┌───────────────────────────────────────▼───────────────────────────┐
│  Pass 3 — structural_index.xref_index.find_refs, one call per       │
│  distinct owning class (deduplicated across all of a UUID's own    │
│  occurrences) — imported directly via a sys.path insertion to the  │
│  sibling tool's src/ directory, exactly as structural_index itself │
│  already imports lambda_dispatcher_resolver's Layer 1              │
└───────────────────────────────────────────────────────────────────┘
```

**Reuse, stated precisely:** `uuid_ble_context.uuid_scan` imports
`structural_index.xref_index.find_refs` and `structural_index.xref_index.load_apk` directly via a
`sys.path` insertion to the sibling tool's `src/` directory — the same pattern `structural_index`
itself already uses one level down for `lambda_dispatcher_resolver`. This is a two-hop reuse chain
(`uuid_ble_context` → `structural_index` → `lambda_dispatcher_resolver`), each hop importing its
immediate neighbor's already-published module directly, never re-implementing DEX loading or
XREF search a third time.

## 5. Interface (CLI)

Two commands:

```
uuid-ble-context extract \
  --apk-root reverse-engineering/apk/v1.0.955078536-10253511 \
  [--output-dir <dir>]           # omit for JSON on stdout

uuid-ble-context context \
  --apk-root reverse-engineering/apk/v1.0.955078536-10253511 \
  --uuid <uuid-string> \
  [--output-dir <dir>]
```

- `extract` runs the full §4 Pass 1/Pass 2 sweep (never Pass 3 — that would mean one
  `structural_index` XREF call per UUID even for callers who only want the raw register) and returns
  every distinct UUID found, its occurrences, and each occurrence's `bt_api_cooccurrence` flag.
- `context` takes one UUID (any case) and runs the full 3-pass pipeline for it alone: occurrences,
  co-occurrence flags, and — for every distinct class among its occurrences — the `structural_index`
  usage-location graph (§6).

## 6. Output format

Plain JSON, same governance as the other tools (§8) — no database in v1.

```json
{
  "apk_root": "reverse-engineering/apk/v1.0.955078536-10253511",
  "total_java_files_scanned": 12545,
  "total_uuids_found": 6,
  "uuids": [
    {
      "uuid": "25e97ff7-24ce-4c4c-8951-f764a708f7b5",
      "occurrences": [
        {"file": ".../defpackage/fzd.java", "line": 9, "class": "defpackage.fzd", "bt_api_cooccurrence": false},
        {"file": ".../defpackage/gbm.java", "line": 35, "class": "defpackage.gbm", "bt_api_cooccurrence": true}
      ]
    }
  ]
}
```

`context --uuid <X>` additionally nests each distinct owning class's `structural_index` usage graph:

```json
{
  "uuid": "25e97ff7-24ce-4c4c-8951-f764a708f7b5",
  "occurrences": [ ... ],
  "usage_by_class": {
    "defpackage.fzd": { "constructs": [...], "calls": [...], "field_type_holders": [...], "counts": {...} }
  }
}
```

This is a **candidate-evidence bundle**, not a research finding — same framing as the other three
tools' own §8/§6. `bt_api_cooccurrence: true` is a textual fact about a file, never itself a
FACT/HYPOTHESIS/ASSUMPTION/OPEN QUESTION judgment about the protocol.

## 7. Explicit failure modes (never silent)

| Situation | Required behavior |
|---|---|
| `context --uuid <X>` where X was never found by `extract`'s own sweep | Non-zero exit, clear error naming the UUID and APK root searched. |
| `extract` finds zero UUIDs anywhere in the tree | Not an error — a real, empty-but-valid `total_uuids_found: 0` result (would happen for an APK version with none, or if BLE/GATT UUID usage genuinely moved elsewhere — a legitimate finding in its own right, per this session's own instructions on what a "no `BluetoothGatt` content" result means). |
| A UUID's owning class cannot be resolved by `structural_index` (e.g. it's declared as a bare local variable, not a class-level static field, so there's no meaningful "owning class" to query) | `usage_by_class` for that class is omitted with a note, not a crash — never silently drops the UUID's own occurrence record, only the usage-graph nesting for that one class. |
| A `.java` file cannot be read | Skipped with a warning to stderr, scan continues for every other file. |

## 8. Governance (binding, not optional)

Identical boundary to the other three tools:

- Never writes to `REVERSE_ENGINEERING.md`, `PROTOCOL.md`, `DECISIONS.md`, or any
  `CAP-NNN-FINDINGS.md`.
- Never labels a UUID's relevance to the Bluetooth protocol, and never proposes what a UUID
  represents — `bt_api_cooccurrence` is a textual fact about a file, not a judgment (§6). §1's own
  worked example (the WorkManager sentinel string) is deliberately reported by this document's prose,
  not by the tool's own output, precisely to keep that distinction real rather than nominal.
- No modification to anything under `reverse-engineering/apk/` — read-only.
- Fully reproducible: the same `(apk version, uuid)` always produces the same output.
- No decompiled APK content is ever committed, in source, tests, or fixtures (§11).

## 9. Why the scope stops here

Same reasoning as the other three tools' own §9: this tool answers one question (where does a UUID
literal live, and what else in the APK touches its owning class) by composing two already-proven
layers (a cheap regex sweep of its own, plus `structural_index`'s existing XREF search) rather than
building a third, independent code-analysis engine. Byte-reversed-alias pairing and GATT-semantic
mapping are both real, plausible follow-ups, but each is a *protocol*-domain judgment
(`PROTOCOL.md`'s own territory, requiring capture correlation for the latter), not a mechanical
extraction this tool could safely automate without crossing into the relevance-judgment line §8
draws.

## 10. Test plan / acceptance criteria

1. **Reproduces the 3 already-known register UUIDs' literal forms.** `extract` must return
   `25e97ff7-24ce-4c4c-8951-f764a708f7b5`, `099775cb-7e0d-3465-5576-d2246d6f043a`,
   `3a046f6d-24d2-7655-6534-0d7ecb759709`, `b5f708a7-64f7-5189-4c4c-ce24f77fe925`, and
   `00001124-0000-1000-8000-00805f9b34fb` — matching `REVERSE_ENGINEERING.md`'s own UUID register
   (5 literal strings for 3 registered UUIDs, since the two RFCOMM UUIDs each have a separately-listed
   byte-reversed alias).
2. **Whole-tree count.** `extract` must return exactly `total_uuids_found: 6` — the 5 above plus the
   one genuinely new find (below) — confirming, with a real committed tool instead of a one-off
   prototype regex, the exact count this document's own §1 reports.
3. **The new, non-Bluetooth find, confirmed as a checked negative.** `context --uuid
   95ed6082-b8e9-46e8-a73f-ff56f00f5d9d` must return exactly two occurrences (a read-side and a
   write-side use of the same literal sentinel), both in `defpackage/ehs.java`, both with
   `bt_api_cooccurrence: false` — confirming §1's own worked example (an `androidx.work.Data`
   internal sentinel string, unrelated to Bluetooth) reproducibly, not as a one-off manual check.
4. **`fzd`'s own usage graph, reproducing `ai-sessions/0025`'s real hand-run** (this session's own
   §4/§10 worked example): `context --uuid 25e97ff7-24ce-4c4c-8951-f764a708f7b5`'s
   `usage_by_class["defpackage.fzd"]` must show `counts.calls == 11` and `counts.field_type_holders ==
   6`, matching `structural_index refs --class fzd` run directly.
5. **Textual co-occurrence, both directions on the same UUID — verified against the real tree, not
   assumed.** For `25e97ff7-24ce-4c4c-8951-f764a708f7b5`: `fzd.java` and `gbm.java` both show
   `bt_api_cooccurrence: true` (both files literally contain `UUID.fromString(`/import
   `android.bluetooth.BluetoothDevice`), **but `fqg.java`** — the already-known
   `REVERSE_ENGINEERING.md` `gbb`/`gbc` construction-wiring class, which merely passes the same UUID
   constant through without ever naming a §3 API directly in its own file text — shows
   `bt_api_cooccurrence: false`. This is the actual discriminating pair this tool's real run found
   (an earlier draft of this criterion assumed `fzd`/`gbm` would differ from each other; they do not
   — both are genuinely BT-API-adjacent; `fqg` is the real, non-tautological negative), and is itself
   informative about the heuristic's own precision limits (§3: a genuinely Bluetooth-adjacent file
   can still show `false` if it never names a BT API directly — a textual, not semantic, check).
6. **Unknown UUID in `context` is a hard error**, per §7.

No regression test may be marked passing by inspection alone — each asserts the exact expected
result set, per this project's own `AGENTS.md` §11 fixture discipline.

## 11. Directory layout and git-tracking boundary

```
reverse-engineering/
└── tools/
    └── uuid_ble_context/
        ├── SPEC.md              (this file)
        ├── README.md
        ├── pyproject.toml
        ├── .venv/               (gitignored)
        ├── src/
        │   └── uuid_ble_context/
        │       ├── __init__.py
        │       ├── cli.py
        │       ├── uuid_scan.py
        │       └── models.py
        └── tests/
            └── test_uuid_scan.py
```

Source code (everything under `src/`, `pyproject.toml`, this `SPEC.md`, `README.md`) is git-tracked
normally — original tooling code, not derived APK content, so `PROJECT_RULES.md` §8 rule 20 does not
apply to it. `tests/test_uuid_scan.py` reads directly from the maintainer's own locally-decompiled
`reverse-engineering/apk/v1.0.955078536-10253511/` tree (already gitignored in full) and the entire
suite is skipped — not failed — when that tree isn't present, exactly the pattern the other three
tools already established. `.venv/` is gitignored for the same reason the others' own venvs are.

---

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/reverse-engineering/tools/uuid_ble_context/SPEC.md - https://tedsluis.github.io/opencontrolpixelbudspro2/reverse-engineering/tools/uuid_ble_context/SPEC
