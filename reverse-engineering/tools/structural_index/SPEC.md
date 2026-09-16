# SPEC.md — Structural Index (general androguard-based XREF/reference search)

Technical specification for a narrow, standalone tool that answers, mechanically, the single query
this project has repeatedly had to answer by hand with ad hoc `grep -rl "extends X"`/
`grep -rn "LFoo;->bar("`: **given a class, list every other class/method in the decompiled companion
APK that (a) constructs it, (b) calls one of its methods, or (c) holds it as a field type.** This is
`reverse-engineering/tools/BACKLOG.md`'s "general androguard-based structural code index" idea,
graduated out of that file per `ai-sessions/0025`'s own authorization (see that file's entry for the
citation). Built following `../lambda_dispatcher_resolver/SPEC.md` as the template, and reusing that
tool's own Layer 1 (`androguard_index.py`) directly, per `../BACKLOG.md`'s own sketch.

**Status: implemented 2026-09-16** (`src/`, `tests/`, `README.md`), against exactly this document's
own §5/§6/§10.

---

## 1. Problem statement

Three separate times in one `ai-sessions/0024` session alone (cross-referencing `esk`'s 20
constructor sites against `resolve-all`'s own discriminator column; checking whether any of `aie`'s
19 non-Bluetooth-relevant referenced classes were already catalogued; the `gwv` cross-reference
collision check), and across three separate sessions before that for `gjv.p()`'s own caller
(`ai-sessions/0023`), this project has answered the same underlying question by hand: *"who else in
this APK constructs, calls, or holds a reference to class `X`?"* Each time, that meant a manual
`grep -rn "new esk("` or `grep -rn "Lgiz;->p("` across the decompiled tree, followed by manually
reading and classifying every hit. This tool collapses that into one deterministic, repeatable
lookup, exactly as `lambda_dispatcher_resolver` already did for the "resolve one dispatcher branch"
question.

## 2. Scope

### 2.1 In scope (v1)

Per `../BACKLOG.md`'s own "sketch of a first, narrowest useful version" (2026-09-16 addition), v1 has
**one** capability: given a class name, return every other class/method in the APK that:

- **(a) constructs it** — a `new-instance <target>` instruction anywhere in the APK's bytecode
  (regardless of whether the matching `invoke-direct <target>-><init>...` is reachable from the same
  basic block — see §3's note on why `new-instance` alone is the detection signal, not the
  constructor-call pairing).
- **(b) calls one of its methods** — any `invoke-virtual`/`invoke-direct`/`invoke-static`/
  `invoke-super`/`invoke-interface` instruction whose referenced method's *declaring type* is the
  target class, optionally filtered to one specific method name (`--method`).
- **(c) holds it as a field type** — any field, on any class in the APK, whose declared type is
  exactly the target class descriptor.

This is a **shape-level reference search**, not a data-flow or call-graph tool — it answers "does a
reference exist, and where," never "what value flows through it" (that is `../BACKLOG.md`'s separate,
explicitly-higher-risk "limited dataflow analysis" idea, out of scope here).

### 2.2 Explicitly out of scope (v1)

Deferred to a later version, per the same "start narrow" discipline `lambda_dispatcher_resolver`
itself used and `../BACKLOG.md`'s own sketch names explicitly:

- An `implements`-query ("list every class implementing interface `X`") — a materially different
  lookup shape (interface table, not instruction/field scanning), needed for
  `MaestroEndpointService`'s Dagger-multibinding search (open question B, `ai-sessions/0024` Phase 2)
  but not built here.
- A resource/string-table search (`apktool-output/res/`) — a different data source entirely (XML,
  not DEX structure), needed for open question M but not built here.
- Any dataflow/value-tracking capability.
- Any relevance judgment — see §8.
- A persistent database/cache across runs — v1's output is files/stdout, same as
  `lambda_dispatcher_resolver`.

## 3. Target query, defined structurally

Given one class descriptor `X` (accepted in the same three spellings `lambda_dispatcher_resolver`
already accepts — short name, `defpackage.`-qualified, or raw smali descriptor `LX;`), scan every
class in every `.dex` file of `base.apk` and, for every method with a body, inspect its decoded
instruction stream (via androguard's `EncodedMethod.get_code().get_bc().get_instructions()` — the
same per-method access `lambda_dispatcher_resolver`'s own smali/androguard layers already use, no
new dependency):

- Any `new-instance` instruction whose type operand is `X` → an **(a) construction site**, keyed by
  the enclosing class+method.
- Any `invoke-*` instruction whose method-reference operand's declaring class is `X` → a
  **(b) call site**, keyed by the enclosing class+method, the called method's name+descriptor, and
  the invoke kind (`invoke-virtual`/`invoke-direct`/`invoke-static`/`invoke-super`/
  `invoke-interface`) — this is deliberately kind-agnostic by default (an `invoke-virtual` on an
  abstract supertype reference and an `invoke-direct` both count; the `gjv.p()`/`Lgiz;->p(` worked
  example, §10 below, is exactly this: the call site's static receiver type is the abstract class,
  not the concrete subclass, and the search must still find it).
- Separately (not a bytecode scan — a class/field declaration scan), any field on any class whose
  declared type descriptor is `X` → a **(c) field-type holder**.

**Why `new-instance` alone, not `new-instance` + the paired `invoke-direct <init>`:** pairing the two
would require basic-block-local dataflow (associating a specific register across two instructions),
which is exactly the "no register-level model, no dataflow" boundary
`../lambda_dispatcher_resolver/SPEC.md` §4's own Layer 2 guardrail draws for a different reason. A bare
`new-instance X` is already unambiguous evidence that method constructs an `X` somewhere in its body,
which is all this tool's own worked examples (§10) ever needed — never leave the `X`-typed register
uninitialized in real code, so decoupling the two loses no accuracy for this tool's stated scope,
while keeping Layer 2/no-dataflow discipline intact.

## 4. Architecture — one layer, reusing `lambda_dispatcher_resolver`'s Layer 1 directly

```
┌───────────────────────────────────────────────────────────────────┐
│  lambda_dispatcher_resolver.androguard_index.load_apk(apk_root)   │
│  — imported directly (sys.path insert to the sibling tool's src/), │
│    not re-implemented — gives every class across every .dex file,  │
│    already keyed by class descriptor (SPEC.md's own LoadedApk).    │
└───────────────────────────────────────┬───────────────────────────┘
                                          │ LoadedApk (class_by_name, dexes, dex_names)
┌───────────────────────────────────────▼───────────────────────────┐
│  structural_index.xref_index                                       │
│  - normalizes the target class name (reuses the same three-spelling│
│    convention as lambda_dispatcher_resolver, duplicated locally    │
│    since it is an 8-line pure function, not worth a cross-tool      │
│    dependency for)                                                 │
│  - one pass over every class/method's decoded instructions          │
│    (construction + call sites)                                     │
│  - one pass over every class's field declarations (field-type       │
│    holders)                                                         │
└───────────────────────────────────────────────────────────────────┘
```

No Layer 2 (smali text) or Layer 3 (JADX correlation) — v1's output is instruction-level facts
(enclosing class, enclosing method name, dex file), not source line ranges. A citation into
`REVERSE_ENGINEERING.md` built from this tool's output still needs its own file+line lookup, the same
as any other candidate this project's existing `grep`-based method already required — this tool
answers *where to look*, not *what the line number is*, matching `../BACKLOG.md`'s own framing of the
idea as replacing the `grep` step specifically, not the read-the-file step after it.

**Reuse, stated precisely (per this task's own instruction to reuse Layer 1 rather than
re-implementing DEX loading):** `structural_index.xref_index` imports
`lambda_dispatcher_resolver.androguard_index.load_apk` and `_normalize_class_name` directly via a
`sys.path` insertion to the sibling tool's `src/` directory (both tools live under
`reverse-engineering/tools/`, so the relative path is stable) — no DEX-loading code is duplicated.
This mirrors how a Python monorepo would use a local path dependency, without adding packaging
machinery neither tool currently has.

## 5. Interface (CLI)

Two commands:

```
structural-index refs \
  --apk-root reverse-engineering/apk/v1.0.955078536-10253511 \
  --class <fully-qualified-class-name-or-short-name> \
  [--method <name>]              # optional: restrict (b) call sites to one method name
  [--output-dir <dir>]           # omit for JSON on stdout

structural-index unreferenced \
  --apk-root reverse-engineering/apk/v1.0.955078536-10253511 \
  --class <name1> [--class <name2> ...]
```

- `refs` runs the §3 query for one class and returns all three categories (a)/(b)/(c) together —
  never partial by default; `--method` narrows (b) only, (a)/(c) are unaffected by it.
- `unreferenced` takes one or more classes and reports, for each, whether *any* external
  reference exists across categories (a)/(b)/(c) combined (i.e. a class with a construction site, a
  call site, or a field-type holder anywhere else in the APK is "referenced"; a class with none is
  "zero external references") — this is exactly the check `ai-sessions/0024`'s own acceptance
  criterion (§10 below) names for `aie`'s never-catalogued referenced classes, generalized to accept
  any list of classes rather than being specific to that one worked example.

Exact flag spellings are negotiable during future extension; the two command shapes and the
no-side-effects contract in §8 are not.

## 6. Output format

Plain JSON, same governance as `lambda_dispatcher_resolver` (§8) — no database in v1.

```json
{
  "class": "defpackage.esk",
  "constructs": [
    {"caller_class": "defpackage.gcp", "caller_method": "g", "dex_file": "classes2.dex"},
    {"caller_class": "defpackage.fyv", "caller_method": "c", "dex_file": "classes2.dex"}
  ],
  "calls": [
    {"caller_class": "defpackage.fyv", "caller_method": "c", "called_method": "a", "invoke_kind": "invoke-virtual", "dex_file": "classes2.dex"}
  ],
  "field_type_holders": [
    {"holder_class": "defpackage.krb", "field_name": "a", "dex_file": "classes2.dex"}
  ],
  "counts": {"constructs": 12, "calls": 1, "field_type_holders": 0}
}
```

`unreferenced` returns one object per queried class:

```json
{"class": "defpackage.laly", "total_references": 0, "referenced": false}
```

## 7. Explicit failure modes (never silent)

Mirrors `../lambda_dispatcher_resolver/SPEC.md` §7's own discipline:

| Situation | Required behavior |
|---|---|
| Class not found anywhere in the APK's dex set | Non-zero exit, clear error naming the class and APK root searched. |
| Class found, but zero references in every one of (a)/(b)/(c) | Not an error — a real, empty-but-valid result (`counts` all zero, empty lists); this is a legitimate "zero external references" finding, not a tool failure. |
| `--method` given but that method name never appears as a call target for the class | Not an error — `calls` comes back empty; `constructs`/`field_type_holders` are unaffected by the filter. |
| A method's bytecode fails to decode (corrupt/unsupported instruction) | That one method is skipped with a warning printed to stderr (class+method name), scan continues for every other method — never aborts the whole run for one bad method. |

## 8. Governance (binding, not optional)

Identical boundary to `../lambda_dispatcher_resolver/SPEC.md` §8 and `../BACKLOG.md`'s own "Governance that
applies to every idea below, unconditionally" section:

- Never writes to `REVERSE_ENGINEERING.md`, `PROTOCOL.md`, `DECISIONS.md`, or any
  `CAP-NNN-FINDINGS.md`.
- Never labels a reference's relevance to the Bluetooth protocol — output is a candidate-evidence
  bundle (a reference exists, here's where), not a FACT/HYPOTHESIS/ASSUMPTION/OPEN QUESTION judgment.
- No modification to anything under `reverse-engineering/apk/` — read-only.
- Fully reproducible: the same `(apk version, class, method filter)` always produces the same output.
- No decompiled APK content is ever committed, in source, tests, or fixtures (§11).

## 9. Why the scope stops here

Same reasoning as `../lambda_dispatcher_resolver/SPEC.md` §9, one level up the stack: this tool is one
query shape (reference search) built directly on Layer 1's already-proven DEX-loading foundation.
`implements`-queries and resource/string-table search are both real, named needs (open questions B
and M respectively, `ai-sessions/0024` Phase 2) but are a different lookup shape/data source each —
deferred to their own follow-up passes of this same tool (or a new one), not folded into v1's scope,
per the same "don't bolt on a materially different capability" discipline `lambda_dispatcher_resolver`
already models.

## 10. Test plan / acceptance criteria

Per `ai-sessions/0024_AUDIT_RESULT_2026_09_16.md`'s own Phase 3.3 sketch, all three already
hand-derived in a prior session (`ai-sessions/0023`, `ai-sessions/0024`) — this is a genuine
regression-test-against-known-truth, not a guess at the right answer:

1. **`esk`'s constructor sites**: `refs --class esk` must return exactly the same construction-site
   set `ai-sessions/0024`'s own by-hand `grep -rn "new esk(" jadx-output/sources/defpackage/` found —
   at minimum, `gcp.java` (the discriminator-20 default-branch site) and `fyv.java` (discriminator
   19) must both appear in `constructs`, matching `REVERSE_ENGINEERING.md`'s `esk` entry's own
   citations.
2. **`gjv.p()`'s sole caller**: `refs --class giz --method p` must return exactly one entry in
   `calls`, with `caller_class` = `defpackage.ftw` — matching `ai-sessions/0023`'s own
   `Lgiz;->p(` smali-grep finding (`REVERSE_ENGINEERING.md`'s `frb`/`fuh`/`glk`/`gjv` entry's
   2026-09-15 update) exactly, from one structured query instead of a raw smali grep.
3. **Zero-external-reference check**: `unreferenced` run against a small, deliberately-picked list of
   `aie`'s own non-catalogued referenced classes (e.g. `Laly`, `Lcvo`, `Lgza`) must correctly report,
   for each, whether any construction/call/field-type reference exists elsewhere in the APK — the
   exact check `ai-sessions/0024`'s own Phase 3.3 named as this tool's third acceptance criterion.

No regression test may be marked passing by inspection alone — each asserts the exact expected
result set, per this project's own `AGENTS.md` §11 fixture discipline.

## 11. Directory layout and git-tracking boundary

```
reverse-engineering/
└── tools/
    └── structural_index/
        ├── SPEC.md              (this file)
        ├── README.md
        ├── pyproject.toml
        ├── .venv/               (gitignored)
        ├── src/
        │   └── structural_index/
        │       ├── __init__.py
        │       ├── cli.py
        │       ├── xref_index.py
        │       └── models.py
        └── tests/
            └── test_xref_index.py
```

Source code (everything under `src/`, `pyproject.toml`, this `SPEC.md`, `README.md`) is git-tracked
normally — original tooling code, not derived APK content, so `PROJECT_RULES.md` §8 rule 20 does not
apply to it. `tests/test_xref_index.py` reads directly from the maintainer's own locally-decompiled
`reverse-engineering/apk/v1.0.955078536-10253511/` tree (already gitignored in full) and the entire
suite is skipped — not failed — when that tree isn't present, exactly the pattern
`lambda_dispatcher_resolver/tests/test_resolver.py` already established. `.venv/` is gitignored for
the same reason `lambda_dispatcher_resolver`'s own venv is (reproducibly reinstallable, not decompiled
content, no reason to commit it either way).

---

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/reverse-engineering/tools/structural_index/SPEC.md - https://tedsluis.github.io/opencontrolpixelbudspro2/reverse-engineering/tools/structural_index/SPEC
