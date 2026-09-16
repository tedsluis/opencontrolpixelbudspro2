# SPEC.md — Lambda Dispatcher Resolver

Technical specification for a narrow, standalone tool that resolves R8-merged synthetic
lambda-dispatcher classes in the decompiled companion APK to their exact source evidence (smali
line range + JADX `case` block, where decompilable). This is deliberately the *first and only*
piece of a possible future, broader "smali/UUID analysis pipeline" — see §9 for why the scope is
kept this narrow.

**Status: implemented 2026-09-16** (`src/`, `tests/`, `README.md`), against exactly this document's
own §5/§6/§10. This document was written as the design step called for before implementation began
(architecture + acceptance criteria first, per the maintainer's own instruction) and is kept as the
design record — §11 carries one correction made during implementation, everything else describes
what was actually built.

---

## 1. Problem statement

R8 (the Android app-shrinker/optimizer used to build this companion app) merges many unrelated
lambda call sites that share the same functional-interface shape into a single synthetic class,
distinguishing the original call sites by an integer discriminator passed into the constructor. A
`switch` (or, for a small number of cases, an `if`-chain) on that discriminator, inside the class's
one non-trivial method, dispatches to the code that was originally a distinct lambda at each call
site.

This project's own APK reverse-engineering work has already hit this pattern repeatedly and
resolved it **by hand**, at real cost each time — three cases from a single 2026-09-15 session
(`ai-sessions/0023`) are used as this spec's own worked examples and later become its test
fixtures:

- `krb` (implements `java.util.function.BiFunction`, 2-way `if`-chain, discriminator field `b`) —
  resolving `fyv.c()`'s `Map.compute()` remapping function required manually reading
  `krb.java`/`krb.smali` to find that discriminator `1` selects `this.a.a(obj, obj2)`.
- `aie` (implements the app's own single-method interface `pkk`, a 20-case `packed-switch`,
  discriminator field `c`) — `aie.a()` is **JADX-undecompilable** ("Method dump skipped, instructions
  count: 1926"); resolving discriminator `7` required a manual `apktool` smali read
  (`apktool-output/smali_classes2/aie.smali:2544-2610`, packed-switch entry index 7 → label
  `:pswitch_c`).
- `ftw` (implements the app's own single-method interface `orr`, an 18-case `packed-switch`,
  discriminator field `b`) — resolving discriminator `9` (`OtaApplyWorker`'s completion callback,
  which turned out to hold the long-sought caller of `gjv.p()`) required the same manual
  smali-then-JADX cross-read (`apktool-output/smali_classes2/ftw.smali:337`, JADX
  `ftw.java:61-73`).

Each of these took a human/AI researcher several manual steps: find the class, find its
constructor call site(s) to learn the discriminator value in question, open the smali file, locate
the `packed-switch`/`if`-chain, count entries to the right label, read the branch, then separately
open the JADX `.java` file and find the matching `case N:` text. The goal of this tool is to
collapse that into one deterministic, repeatable lookup.

## 2. Scope

### 2.1 In scope (v1)

- **Detect** candidate lambda-dispatcher classes across the whole decompiled APK (a `list` command).
- **Resolve** a given `(class, discriminator value)` pair to:
  - the exact smali source range implementing that branch (file path relative to the repo,
    starting/ending line number, raw text),
  - which dispatch mechanism was used (`packed-switch`, `sparse-switch`, or an `if`-chain),
  - the corresponding JADX `case N:` block in the `.java`/`.kt` source, when JADX successfully
    decompiled the method (and an explicit "not decompilable" result, not silence, when it did not
    — see §7).
- Work directly against the on-disk decompiled output already present under
  `reverse-engineering/apk/<version>/` (`jadx-output/`, `apktool-output/`, `apktool-output-arm64_v8a/`)
  — no new decompilation step.

### 2.2 Explicitly out of scope (v1)

Everything the earlier, broader "smali/UUID analysis pipeline" proposal described beyond this one
capability is deferred, not rejected — see §9:

- UUID extraction/normalization.
- BLE/GATT API call-graph reconstruction (`writeCharacteristic`, `setCharacteristicNotification`, ...).
- Byte-array/dataflow tracking.
- Protobuf/`RawMessageInfo` schema auto-extraction (already separately served by
  `scripts/decode_rawmessageinfo.py` for one class at a time).
- APK-version diffing.
- A SQLite/relational store. v1's output is files, per §6.
- Anything that decides a branch's *semantic* relevance to the Bluetooth protocol, or that writes
  into `REVERSE_ENGINEERING.md`/`PROTOCOL.md`/`DECISIONS.md` — see §8.

## 3. Target pattern, defined structurally (not by interface name)

R8 reuses whichever functional interface happens to be at each merge site — both
`java.util.function.*` (`BiFunction`, `Callable`, ...) and this app's own numerous single-abstract-
method interfaces (`pkk`, `pkg`, `orr`, and others not yet catalogued). **Detection must not assume
a fixed interface name.** A class is a candidate lambda-dispatcher iff, read from the DEX (via
androguard, see §4):

1. It is marked `synthetic` (R8's own marker for compiler-generated classes) and `final`.
2. It implements **exactly one** interface, and that interface declares **exactly one** abstract
   method (ignoring default/static methods — `krb`'s `andThen()` override on `BiFunction` is a
   default-method passthrough, not the dispatch method).
3. It declares an `int` field marked `synthetic` (name varies — `b` in `krb`/`ftw`, `c` in `aie`;
   never assume a fixed field name) that is:
   - assigned unconditionally in **every** constructor, and
   - read at the start of the interface's sole abstract-method implementation, immediately used as
     the operand of either a `packed-switch`/`sparse-switch` instruction, or the first of a chain of
     `if-eq`/`if-ne` comparisons against integer literals.
4. It declares zero or more additional `Object`-typed (or narrower) `synthetic` fields (the captured
   lambda variables — `krb` has one, `esk` has two, `ftw` has one).
5. It has one or more constructors, each assigning the discriminator field from one constructor
   parameter and each `Object`-typed field from another — multiple constructor overloads (`esk` has
   12+) are normal and reflect merging of call sites with different captured-variable *types*, not a
   different dispatcher.

This is a shape check, not a semantic one — it says nothing about what any branch does, only that
the class has this mechanical structure.

## 4. Architecture — three layers

Per the discussion this spec follows from: androguard is the right foundation for DEX-level
structure, but it does not by itself give the exact `apktool`-formatted smali text and line numbers
this project's own evidence trail already cites (e.g. `aie.smali:337`, `REVERSE_ENGINEERING.md`'s
own citation style throughout). Building that mapping is this tool's actual job — using androguard
for everything it already solves well, and a narrow, purpose-built smali reader for exactly what it
doesn't.

```
┌─────────────────────────────────────────────────────────────┐
│  Layer 1 — androguard (DEX structure)                        │
│  - enumerate classes/methods/fields across all .dex files     │
│  - identify candidate classes per §3's structural shape check │
│  - which physical .dex file a class lives in (→ layer 3)      │
└───────────────────────────────┬───────────────────────────────┘
                                 │ class descriptor (e.g. "Laie;") + dex index
┌───────────────────────────────▼───────────────────────────────┐
│  Layer 2 — smali source reader (NOT a general smali parser)   │
│  - locates the exact apktool smali file for a class (§4.1)    │
│  - reads ONLY what's needed: method boundaries, the           │
│    packed-switch/sparse-switch data block, or an if-chain     │
│  - never attempts general instruction semantics beyond this   │
└───────────────────────────────┬───────────────────────────────┘
                                 │ resolved branch: file + line range + raw text
┌───────────────────────────────▼───────────────────────────────┐
│  Layer 3 — JADX correlation                                   │
│  - same class name → jadx-output/sources/<package>/<Cls>.java │
│  - text-search for the matching `case N:` (or the literal-cast│
│    form, e.g. `case UrlRequest.Status.SENDING_REQUEST /* 12 */:`)│
│  - extract that case's block up to the next case/default/break│
│  - explicit "not decompilable" result if JADX shows a         │
│    "Method dump skipped" stub instead of real code             │
└─────────────────────────────────────────────────────────────┘
```

**Guardrail against scope creep (this is the single biggest risk called out in review of the
original proposal):** Layer 2's job is bounded to exactly three things — find a method's line range
inside a class's smali file, recognize a `packed-switch`/`sparse-switch` data block and its label
table, and recognize a short `if-eq`/`if-ne` chain against integer literals. It does **not** parse
general smali instruction semantics, build a register-level model, or attempt anything resembling
dataflow. The moment a feature request would require more than that, it belongs in a later,
separately-scoped tool (§9), not bolted onto this one.

### 4.1 Locating a class's smali file across split dex groups

`apktool` splits smali output across `smali/`, `smali_classes2/`, `smali_classes3/` by physical
`.dex` file, and which directory a given class lands in is not derivable from the class name alone
— it must be looked up. androguard already knows which `.dex` a class came from (layer 1); layer 2
uses that to search only the corresponding `smali*/` root (falling back to searching all three, in
declared order, if the direct mapping is ambiguous — e.g. differing dex-numbering conventions
between androguard and `apktool`), rather than grepping the whole tree on every lookup. This
class→file mapping is built once per run and cached for the session (not persisted across runs in
v1 — see §6).

## 5. Interface (CLI)

Three commands, matching the three things this tool does — `resolve-all` added 2026-09-16 after the
maintainer asked how to use the tool effectively at scale, once `resolve`'s single-value form
proved the mechanism on the three worked examples:

```
lambda-resolver list \
  --apk-root reverse-engineering/apk/v1.0.955078536-10253511 \
  [--interface-hint <FQN>]        # optional filter, does not gate detection (§3)

lambda-resolver resolve \
  --apk-root reverse-engineering/apk/v1.0.955078536-10253511 \
  --class <fully-qualified-class-name-or-short-name> \
  --discriminator <int>

lambda-resolver resolve-all \
  --apk-root reverse-engineering/apk/v1.0.955078536-10253511 \
  --class <fully-qualified-class-name-or-short-name> \
  [--output-dir <dir>]            # one file per case; omit for one JSON array on stdout
```

- `list` scans the whole APK per §3's structural check and prints one row per candidate class:
  class name, dex file, discriminator field name, dispatch mechanism (`packed-switch` /
  `sparse-switch` / `if-chain`), and case count. This is the mechanical equivalent of what a
  researcher currently does by eyeballing a class and noticing "this looks like an R8 lambda
  dispatcher" — it does not claim any of the listed classes are relevant to anything.
- `resolve` takes one class + one discriminator value and prints the full evidence bundle (§6). If
  the discriminator falls outside the enumerated cases, it reports the `default` branch explicitly
  (never an error swallowed into "not found").
- `resolve-all` takes one class only, and resolves **every** case its own dispatch table actually
  defines, plus exactly one extra entry for the default branch — never an arbitrary guessed range
  (§7's "never guess" rule extends here too). This is the effective way to use this tool on a class
  already known to be protocol-relevant (e.g. `aie`/`esk`/`ftw`, each of which has had only one of
  its 18-20 real cases manually investigated so far) — one call surfaces every branch's evidence at
  once, rather than requiring the caller to already know which discriminator values exist.

Exact flag names are negotiable during implementation; the command shapes and the
no-side-effects contract in §8 are not.

## 6. Output format

Plain JSON to stdout (one object per `resolve` call; a JSON array of summary objects for `list`) —
no database in v1 (§2.2). Every `resolve` result includes:

```json
{
  "class": "defpackage.aie",
  "dex_file": "classes2.dex",
  "interface_implemented": "defpackage.pkk",
  "discriminator_field": "c",
  "discriminator_value": 7,
  "dispatch_mechanism": "packed-switch",
  "case_count": 20,
  "smali": {
    "path": "reverse-engineering/apk/v1.0.955078536-10253511/apktool-output/smali_classes2/aie.smali",
    "method_line_range": [37, 3990],
    "branch_label": "pswitch_c",
    "branch_line_range": [2544, 2610],
    "raw_text": "    :pswitch_c\n    move-object/from16 v1, p1\n    ..."
  },
  "jadx": {
    "path": "reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/aie.java",
    "decompilable": false,
    "note": "Method dump skipped, instructions count: 1926 — smali is the only available evidence for this branch."
  },
  "resolution_status": "resolved"
}
```

`resolution_status` is one of `resolved`, `resolved-default-branch`, or `ambiguous` (e.g. the
smali/JADX case counts disagree, or the class matched §3's shape check only partially) — never
silently "best guess," per `AGENTS.md` §13.6.

This is a **candidate-evidence bundle**, not a research finding. It contains no FACT/HYPOTHESIS
label of its own (§8) — that judgment happens after a human or AI researcher reads the bundle and
decides what, if anything, it means for the protocol.

## 7. Explicit failure modes (never silent)

| Situation | Required behavior |
|---|---|
| Class not found | Non-zero exit, clear error naming the class and APK root searched. |
| Class found but fails §3's structural check | Non-zero exit, explains which check failed (e.g. "implements 2 interfaces, not 1"). |
| Discriminator outside the enumerated case range | `resolution_status: "resolved-default-branch"`, resolves to the `default`/final `else` branch, not an error. |
| JADX shows "Method dump skipped" for the target method | `jadx.decompilable: false` with the note field populated — the smali evidence is still returned in full. |
| Smali file for the class not found in any `smali*/` root | Non-zero exit — this should not happen if layer 1's dex mapping is correct, and is itself worth surfacing as a tool bug, not papered over. |
| Dispatch mechanism is neither `packed-switch`/`sparse-switch` nor a short `if`-chain (e.g. a `sparse-switch` with non-contiguous keys spanning a wide range, or nested dispatch) | `resolution_status: "ambiguous"`, raw method text returned as-is for manual reading — not force-fit into the wrong mechanism. |

## 8. Governance (binding, not optional)

Per `AGENTS.md` §6 and `DECISIONS.md` ADR-017's existing AI-mechanical-assistance boundary, which
this tool operates squarely inside:

- The tool **never** writes to `REVERSE_ENGINEERING.md`, `PROTOCOL.md`, `DECISIONS.md`, or any
  `CAP-NNN-FINDINGS.md` — its only output is the JSON described in §6, to stdout or a file the
  invoking researcher/session chooses.
- The tool **never** labels a branch's semantic relevance to the Bluetooth protocol. Its
  `resolution_status` field describes only whether the *mechanical lookup* succeeded — it is not a
  FACT/HYPOTHESIS/ASSUMPTION/OPEN QUESTION label in `PROJECT_RULES.md` §1's sense, and must never be
  presented as one.
- The tool makes **no modification** to any file under `reverse-engineering/apk/` — read-only
  access to the existing decompiled output, always.
- Every `resolve` result is fully reproducible: running it again against the same APK version and
  the same `(class, discriminator)` pair produces byte-identical output.
- A researcher (human or AI session) reading a `resolve` result is still bound by every existing
  evidence rule when writing that result up — a citation into `REVERSE_ENGINEERING.md` still needs
  its own file+line reference (this tool supplies exactly that), and a promotion to 🟢 FACT in
  `PROTOCOL.md` or a new `DECISIONS.md` ADR still needs explicit maintainer sign-off, unaffected by
  this tool's existence.

## 9. Why the scope stops here

The lambda-dispatcher-resolver is being built first, alone, because it is the one piece of the
originally-proposed broader pipeline that (a) has three already-worked, hand-verified examples to
test against today, (b) has a narrow, precisely statable structural definition (§3), and (c)
directly speeds up work this project is already doing by hand, repeatedly. Everything else from the
original proposal — UUID extraction, BLE-context reconstruction, dataflow, schema/version tooling —
remains a reasonable future direction, but is deliberately not designed here. If this tool proves
useful, the natural next step is an androguard-based structural code index (class/method/field/XREF
search) as its own, separately-specified follow-up — not an expansion of this tool's own scope.

## 10. Test plan / acceptance criteria

v1 is accepted once `resolve` reproduces all three worked examples from §1 exactly, byte-for-byte
against the raw text already quoted in `REVERSE_ENGINEERING.md`'s `qhr`/`fye` entry and `frb`-`gjv`
entry:

| # | Class | Discriminator | Expected `dispatch_mechanism` | Expected branch (smali label / evidence) | Expected `jadx.decompilable` |
|---|---|---|---|---|---|
| 1 | `defpackage.aie` | `7` | `packed-switch` | `:pswitch_c`, `apktool-output/smali_classes2/aie.smali:2544-2610` | `false` ("Method dump skipped, instructions count: 1926") |
| 2 | `defpackage.ftw` | `9` | `packed-switch` | `apktool-output/smali_classes2/ftw.smali:337` region; JADX `case 9:` (`ftw.java:61-73`, the `OtaApplyWorker` branch) | `true` |
| 3 | `defpackage.krb` | `1` | `if-chain` | the `this.a.a(obj, obj2)` branch (`krb.java`'s `i != 1 ? ... : this.a.a(obj, obj2)`) | `true` |

A fourth, deliberately-adversarial fixture should be added before v1 is considered done: a
discriminator value **outside** the enumerated range for one of the classes above, confirming
`resolution_status: "resolved-default-branch"` fires correctly rather than erroring or silently
matching the wrong case.

`list` is accepted once it independently re-discovers all of `krb`, `aie`, `esk`, `ftw` (this
session's four known instances) from a full-APK scan, with zero manual seeding of class names.

**`resolve-all`** (added 2026-09-16) is accepted once, for both a packed-switch class (`ftw`) and
an if-chain class (`krb`): it returns exactly `case_count + 1` results (every real case plus one
default), every real-case value is `"resolved"` with the same evidence `resolve` would give it
individually, and exactly one `"resolved-default-branch"` entry exists whose `discriminator_value`
is not among the real cases.

No regression test may be marked passing by inspection alone — each must assert the exact expected
line range/text, per this project's own `AGENTS.md` §11 fixture discipline.

## 11. Directory layout, and a correction made during implementation

```
reverse-engineering/
└── tools/
    └── lambda_dispatcher_resolver/
        ├── SPEC.md              (this file)
        ├── pyproject.toml
        ├── .venv/               (gitignored — see repo-root .gitignore)
        ├── src/
        │   └── lambda_dispatcher_resolver/
        │       ├── __init__.py
        │       ├── cli.py
        │       ├── androguard_index.py   (layer 1)
        │       ├── smali_reader.py       (layer 2)
        │       ├── jadx_correlate.py     (layer 3)
        │       └── models.py             (the §6 output schema)
        └── tests/
            └── test_resolver.py   (the §10 worked examples — see the note below on where they
                                     actually read from)
```

**Resolved during implementation, correcting this section's original text.** This tool's own
Python source (everything under `src/`, plus `pyproject.toml` and this `SPEC.md`) is git-tracked
normally — it is original tooling code, not derived APK content, so `PROJECT_RULES.md` §8 rule 20
does not apply to it.

This section's *original* text proposed committing `tests/fixtures/` as "byte-exact smali/JADX
excerpts." That was wrong, and was caught only once the fixtures were actually created and checked
against this repo's own `.gitignore` comment (which quotes rule 20 verbatim: decompiled Java/smali
is "a reproduction of Google's copyrighted code, banned from this project's codebase even as
research output — this holds even though it never leaves the maintainer's own machine, since
'codebase' here means this git history"). A committed excerpt of `aie.smali`/`ftw.java`/etc. is
exactly what that rule bans, regardless of how small the excerpt is or that it only exists to
support a test. **Fixed**: `tests/test_resolver.py` reads directly from the maintainer's own
locally-decompiled `reverse-engineering/apk/v1.0.955078536-10253511/` tree (already gitignored in
full) and the entire suite is skipped — not failed — when that tree isn't present on the machine
running it, exactly the same pattern every other script in this project that depends on a local
decompile already uses. No decompiled content of any kind ships inside this tool's own directory.
This is also why the venv (`.venv/`) is gitignored: it is not decompiled APK content, but there is
no reason for it to be committed either (`androguard` and its own dependency tree are pinned in
`pyproject.toml` and reproducibly reinstallable).

---

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/reverse-engineering/tools/lambda_dispatcher_resolver/SPEC.md - https://tedsluis.github.io/opencontrolpixelbudspro2/reverse-engineering/tools/lambda_dispatcher_resolver/SPEC
