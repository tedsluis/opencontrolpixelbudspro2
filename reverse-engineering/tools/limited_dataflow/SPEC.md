# SPEC.md — Limited Dataflow Analysis (single-basic-block only)

Technical specification for a narrow, standalone tool that traces one register's value forward,
line by line, through a smali method body or a resolved lambda-dispatcher branch — **strictly
within one basic block: it stops the moment it would have to follow a branch, loop, or another
method's own body.** This is `../BACKLOG.md`'s "limited dataflow analysis" idea, graduated out of
that file per `ai-sessions/0025`'s own resumption authorization, at exactly the risk-bounded scope
that file's own text already prescribed: "scope the first version to straight-line, single-basic-block
flows only." Built following `../uuid_ble_context/SPEC.md`'s own template, the same way that tool
followed `../schema_batch_extractor/SPEC.md`.

**Status: implemented 2026-09-16** (`src/`, `tests/`, `README.md`), against exactly this document's
own §5/§6/§10.

---

## 1. Problem statement

This project has repeatedly had to answer, by hand, "where does this value actually end up?" —
most visibly for `esk`'s discriminator-19 branch (`REVERSE_ENGINEERING.md`'s `esk` entry), whose own
prose summarizes the branch as "casts `obj`→`nqo` ... and calls `nqoVar.e(qjcVar)`," a summary that
telescopes across an intermediate wrapper object (`fys`) without saying so explicitly. Reading the
raw smali directly (§10's worked example) shows the branch never actually calls `nqo.e(...)` itself
— it constructs a `new fys(qjc, nqo)` and hands that wrapper to a deferred-task builder; the real
`nqo.e(qjc)` invocation, if it happens at all, is inside `fys`'s own method body, a different class
entirely. Every one of this project's existing static-analysis tools stops at exactly the boundary
where this kind of cross-method truth lives — `structural_index` finds *that* a call/construction
exists, never *what value* flows into it; `lambda_dispatcher_resolver` finds *which branch* a
discriminator resolves to, never what that branch's own registers actually carry through it. This
tool closes that one specific, narrow gap: given a register defined at a known point, what happens
to its value for the rest of that same basic block?

## 2. Scope

### 2.1 In scope (v1)

- **Trace one register, forward, within one basic block only.** Given a method body (or a resolved
  dispatcher branch's own raw text — reusing `lambda_dispatcher_resolver` directly, §4) and a
  starting line + register that the caller has already identified as interesting (typically a
  `new-array`/`iget-object`/`move-result-object` that captures a byte array or a pre-built protobuf
  message — `../BACKLOG.md`'s own two named source shapes), walk forward instruction by instruction
  and recognize exactly four things, and nothing else (§3):
  1. the register being renamed by a plain `move-object` (its value is unchanged, only its register
     slot moves — tracing continues under the new name);
  2. the register being narrowed/asserted by a `check-cast` (its value is unchanged; tracing
     continues under the same name);
  3. the register appearing as an argument in an `invoke-*` instruction — recorded as a **sink use**
     (this is the "forward to a ... call site" `../BACKLOG.md`'s own text asks for); tracing
     continues past it (an object reference survives being passed as an argument, per Dalvik's
     call-by-reference-for-objects semantics — see §3's own note on why this is not "the trace
     ends here").
  4. a basic-block boundary (a label, `if-*`, `goto`, `packed-switch`/`sparse-switch`) — tracing
     stops immediately, reported as `left_basic_block_scope`, never followed.
- **Anything else that touches the traced register is an explicit stop, not a guess** (§3's
  guardrail, directly modeled on `lambda_dispatcher_resolver/SPEC.md` §4's own "moment a feature
  request would require more than that, it belongs in a later, separately-scoped tool" discipline).

### 2.2 Explicitly out of scope (v1)

- **Cross-method/cross-block tracing of any kind.** Following a sink-use invocation *into* the
  callee's own body, or a branch to see what happens on the other side, is dataflow *across* basic
  blocks — exactly what `../BACKLOG.md`'s own text calls "the highest-risk item on this list" and
  explicitly defers. §10's own worked example demonstrates this boundary directly: it does **not**
  reach `nqo.e(...)`, because that call lives inside a different method's body.
  Constant-propagation (what value a register actually holds — Should a `new-array` size or a
  `const` register's own numeric value be resolved), array-content tracking
  (`fill-array-data`/`aput-*`), and multi-register (e.g. wide/`long`) tracking are all deferred —
  v1 tracks exactly one named register's *identity* (which object it refers to), never its content.
- **Any relevance judgment** about what a sink use *means* for the Bluetooth protocol — see §8,
  identical governance to the other three tools.
- **Automatic starting-point discovery.** This tool does not search for "interesting" byte-array/
  protobuf-builder constructions on its own — the caller supplies the exact starting line+register,
  typically obtained by first reading a `lambda_dispatcher_resolver resolve`/`structural_index refs`
  result, per this project's own existing "propose, human/AI reads it, decides where to look next"
  division of labor (§9).

## 3. The four recognized instruction shapes, and why a 5th case is always a stop

Per smali's own instruction shapes, as actually emitted by `apktool`'s baksmali for this APK (same
verbatim-regex discipline as `lambda_dispatcher_resolver/SPEC.md` §4's own):

| # | Pattern | Effect on the trace |
|---|---|---|
| 1 | `move-object[/from16] <dst>, <src>` where `<src>` == the currently-traced register | Register renamed to `<dst>`; value unchanged; continue. |
| 2 | `check-cast <reg>, L<type>;` where `<reg>` == the currently-traced register | Value/name unchanged; recorded as a "cast to `<type>`" step; continue. |
| 3 | `invoke-<kind>[/range] {<args>}, L<cls>;-><method>(<params>)<ret>` where the traced register appears in `<args>` | Recorded as a **sink use** (`caller class/method`, `called class/method`, `invoke_kind`, `arg_position`); continue (see below — this is not a stop). |
| 4 | A label (`:foo`), `if-*`, `goto[/16|/32]`, `packed-switch`, `sparse-switch` | Stop: `left_basic_block_scope`. |
| — (anything else that names the traced register at all, in any operand position) | Stop: `ambiguous_redefinition` — the instruction is not one of the three alias-preserving shapes above, so per `AGENTS.md` §13.6 the tool does not guess whether it overwrites, reads-only, or is unrelated; it reports exactly which line and stops. |

**Why a sink use (row 3) does not end the trace:** Dalvik objects are passed to `invoke-*` by
reference — the calling method's own register still holds the same object after the call returns
(unless something later explicitly reassigns that register, which row "—" above would then catch).
Stopping at the first sink use would silently miss a register used as an argument to two different
calls in sequence (a real, observed shape — see §10's own worked example, which finds exactly this:
`check-cast` then a sink use, then the trace correctly continues to the end of the block finding no
second use, rather than stopping early and leaving that an open question).

## 4. Architecture — reusing `lambda_dispatcher_resolver` directly for method/branch text

```
┌───────────────────────────────────────────────────────────────────┐
│  lambda_dispatcher_resolver.cli.analyze_class / smali_reader        │
│  — imported directly (sys.path insert to the sibling tool's src/),  │
│    not re-implemented — gives a resolved dispatcher branch's own    │
│    MethodBody + DispatchInfo, and `smali_reader.resolve_branch`     │
│    for the exact line range + raw text of one discriminator value.  │
│    `smali_reader.find_method` alone (no dispatch analysis) covers   │
│    the plain-method entry point (§5's `trace-method`).              │
└───────────────────────────────────────┬───────────────────────────┘
                                          │ MethodBody.lines + a (start_line, end_line) range
┌───────────────────────────────────────▼───────────────────────────┐
│  limited_dataflow.dataflow                                          │
│  - this tool's own code: the §3 four-shape recognizer, applied      │
│    forward from a given (line, register) starting point, never      │
│    past the given range's own end or a §3-row-4 boundary            │
└───────────────────────────────────────────────────────────────────┘
```

**Reuse, stated precisely:** `limited_dataflow.dataflow` imports
`lambda_dispatcher_resolver.cli.analyze_class`, `.smali_reader.resolve_branch`, and
`.smali_reader.find_method` directly via a `sys.path` insertion to the sibling tool's `src/`
directory — no smali-file-location or method/branch-boundary logic is duplicated; this tool's own
code is exactly the §3 recognizer, nothing else.

## 5. Interface (CLI)

Two entry points, matching the two ways this project has needed a starting point so far:

```
limited-dataflow trace-branch \
  --apk-root reverse-engineering/apk/v1.0.955078536-10253511 \
  --class <dispatcher-class> \
  --discriminator <int> \
  --start-line <int> \                # 1-indexed, absolute line in the smali file, the instruction
                                       #   that DEFINES --start-register (must fall inside the
                                       #   resolved branch's own line range -- SPEC.md §7)
  --start-register <vN|pN> \
  [--output-dir <dir>]

limited-dataflow trace-method \
  --apk-root reverse-engineering/apk/v1.0.955078536-10253511 \
  --class <class> \
  --method <name> \
  --start-line <int> \
  --start-register <vN|pN> \
  [--output-dir <dir>]
```

- `trace-branch` resolves the branch exactly as `lambda_dispatcher_resolver resolve` would (reused
  directly), then runs the §3 trace starting the line *after* `--start-line`, bounded to that
  branch's own line range.
- `trace-method` runs the same trace over a plain method's full body (no dispatcher resolution),
  bounded to that method's own line range — still single-basic-block only: if the method itself
  branches before the trace reaches the caller-supplied starting point's own basic block end, the
  trace still stops at the first boundary it hits (§3 row 4), exactly as `trace-branch` would.

## 6. Output format

Plain JSON, same governance as the other three tools (§8).

```json
{
  "class": "defpackage.esk",
  "discriminator": 19,
  "start_line": 325,
  "start_register": "v0",
  "steps": [
    {"line": 333, "kind": "cast", "to_type": "qjc", "register": "v0"},
    {"line": 337, "kind": "sink_use", "register": "v0", "arg_position": 1,
     "invoke_kind": "invoke-direct", "called_class": "fys", "called_method": "<init>"}
  ],
  "final_status": "ambiguous_redefinition",
  "final_register": "v0",
  "stop_line": 386,
  "stop_text": "const-wide/16 v0, 0x5"
}
```

(Real output against the real APK, `v1.0.955078536-10253511` — line numbers and the
`ambiguous_redefinition` outcome corrected 2026-09-17 per §10 item 1's own correction note; the
illustrative line numbers this example originally used (897/903/909) did not correspond to any
real line in `esk.smali` and have been replaced with the actual ones.)

`final_status` is one of `reached_end_of_block`, `left_basic_block_scope`, or
`ambiguous_redefinition` — never a silent, unstated stop (§7). This is a **candidate-evidence
bundle**, not a research finding (§8) — the same framing every other tool in this project's
`reverse-engineering/tools/` uses.

## 7. Explicit failure modes (never silent)

| Situation | Required behavior |
|---|---|
| `--start-line` falls outside the resolved branch's/method's own line range | Non-zero exit, clear error stating the valid range. |
| The line at `--start-line` does not actually mention `--start-register` at all | Non-zero exit — the caller mis-identified the starting point; this tool does not guess a nearby line. |
| The trace hits a basic-block boundary (row 4) | Not an error — `final_status: "left_basic_block_scope"`, every step recorded up to that point still returned in full. |
| The trace hits an unrecognized instruction touching the traced register (the "—" row) | Not an error — `final_status: "ambiguous_redefinition"`, with the exact offending line included in the result so a human can read it directly, never silently dropped. |
| The trace reaches the end of the given range with the register never redefined ambiguously | `final_status: "reached_end_of_block"` — a real, legitimate, complete result, not a partial one. |

## 8. Governance (binding, not optional)

Identical boundary to the other three tools:

- Never writes to `REVERSE_ENGINEERING.md`, `PROTOCOL.md`, `DECISIONS.md`, or any
  `CAP-NNN-FINDINGS.md`.
- Never labels a sink use's relevance to the Bluetooth protocol — a "sink use" is a mechanical fact
  (this register was passed to this call, at this line), never a FACT/HYPOTHESIS/ASSUMPTION/OPEN
  QUESTION judgment about what that call does.
- No modification to anything under `reverse-engineering/apk/` — read-only.
- Fully reproducible: the same `(apk version, class, discriminator/method, start line, start
  register)` always produces the same output.
- No decompiled APK content is ever committed, in source, tests, or fixtures (§11).

## 9. Why the scope stops here

Same reasoning as the other three tools' own §9, and `../BACKLOG.md`'s own explicit risk-scoping for
this specific idea ("the highest-risk item on this list ... scope the first version to
straight-line, single-basic-block flows only"). Cross-method tracing (following a sink use into its
callee, exactly what would be needed to actually reach `nqo.e(...)` from `esk`'s discriminator-19
branch, §10) requires a real interprocedural call graph and a register-to-parameter mapping
convention — a materially larger undertaking this project has not yet attempted, and this backlog
item was accepted into v1 specifically *without* that capability, per its own risk-scoping text. If
pursued later, it is a new, separately-specified v2, not a quiet expansion of this tool's own scope.

## 10. Test plan / acceptance criteria

**Primary fixture, per this session's own resumption instructions: the already-hand-traced `esk`
discriminator 19 → `WriteSetting` chain.** Confirming the tool reaches the same conclusion a human
reading the raw smali directly would reach — including the one place that reading and this
project's own prose summary (`REVERSE_ENGINEERING.md`'s `esk` entry) diverge (§1).

1. **`trace-branch --class esk --discriminator 19`, starting at the `iget-object v0, p0,
   Lesk;->b:Ljava/lang/Object;` line (the real APK's line 325), register `v0`:** must produce, in
   order: a `cast` step to type `qjc` (the `check-cast v0, Lqjc;` immediately after, line 333),
   then a `sink_use` step at the `invoke-direct {v1, v0, p1}, Lfys;-><init>(Lqjc;Lnqo;)V` line (337,
   `arg_position: 1`, `called_class: "fys"`, `called_method: "<init>"`) — **not** `nqo`/`e`,
   confirming §1/§9's own disclosed boundary directly rather than by assertion alone: this tool's
   own real output, run against the real APK, does not reach the `WriteSetting` RPC invoke itself,
   because that call is inside `fys`'s own body, out of single-basic-block scope.
   **Corrected 2026-09-17 (tool-4 build/test session, `ai-sessions/0025`'s resumption):** this
   item's `final_status` is **not** `"reached_end_of_block"` as originally written here (and in
   §6's worked example above) — running the real tool against the real APK shows `v0`'s own
   register slot is legitimately reused later in the *same* branch by `const-wide/16 v0, 0x5`
   (line 386, the RPC send's own 5-second timeout setup — see `REVERSE_ENGINEERING.md`'s `esk`
   entry), which correctly trips §3's row "-" guardrail. The true, verified `final_status` is
   `"ambiguous_redefinition"`, `stop_line: 386`, `stop_text: "const-wide/16 v0, 0x5"`. The
   `cast`/`sink_use` steps and the disclosed non-reach of `nqo`/`e` are exactly as originally
   written and remain the tool's headline confirmation; only the *stated* final outcome was wrong.
   See `reverse-engineering/tools/limited_dataflow/tests/test_dataflow.py`'s own module docstring
   and `ai-sessions/0025_MAINTENANCE_RESULT_2026_09_16.md` for the full account.
2. **Basic-block-boundary stop, adversarial fixture:** tracing a register that survives, unaliased
   and unredefined, from inside the branch all the way to the next `:pswitch_*`/switch-data
   boundary, over the *whole* method (`trace-method`, not `trace-branch`, so the trace is not
   pre-bounded to one branch) — must reach `left_basic_block_scope` at that boundary when the scan
   range is deliberately extended past the branch's own end, confirming row 4 fires and does not
   silently wander into a sibling branch's code.
   **Corrected 2026-09-17:** the register originally named here, `p1` (the `nqo`-typed parameter),
   does **not** exercise this — against the real APK it is reassigned by `move-result-object p1`
   (line 347, well before any label), which is itself a correct, but different, stop
   (`ambiguous_redefinition`), not the boundary this item means to exercise. `v1` — defined by the
   branch's own `new-instance v1, Lfys;` at line 329, the very wrapper object item 1's `sink_use`
   step constructs — survives unredefined all the way to the `:pswitch_1` label at line 442, and is
   the register this item's test actually uses.
3. **Ambiguous-redefinition fixture:** a register overwritten by a `move-result-object` from an
   unrelated call before any of rows 1-3 apply must produce `final_status:
   "ambiguous_redefinition"` with the exact line quoted — confirmed against a second, deliberately
   simple hand-written smali snippet fixture (not from the live APK) kept inline in the test file
   itself (a handful of literal smali lines is not "decompiled APK content" in the sense
   `PROJECT_RULES.md` §8 rule 20 bans — it is a synthetic, tool-authored test input, the same
   category as `lambda_dispatcher_resolver`'s own fixed byte-array unit-test inputs elsewhere in
   this project's testing conventions — not an excerpt of the actual companion app's copyrighted
   source).
4. **Out-of-range `--start-line` is a hard error**, per §7.

No regression test may be marked passing by inspection alone — each asserts the exact expected step
sequence, per this project's own `AGENTS.md` §11 fixture discipline.

## 11. Directory layout and git-tracking boundary

```
reverse-engineering/
└── tools/
    └── limited_dataflow/
        ├── SPEC.md              (this file)
        ├── README.md
        ├── pyproject.toml
        ├── .venv/               (gitignored)
        ├── src/
        │   └── limited_dataflow/
        │       ├── __init__.py
        │       ├── cli.py
        │       ├── dataflow.py
        │       └── models.py
        └── tests/
            └── test_dataflow.py
```

Source code (everything under `src/`, `pyproject.toml`, this `SPEC.md`, `README.md`) is git-tracked
normally. `tests/test_dataflow.py`'s APK-dependent cases read directly from the maintainer's own
locally-decompiled `reverse-engineering/apk/v1.0.955078536-10253511/` tree (already gitignored in
full) and are **skipped** (not failed) when that tree isn't present, exactly the pattern the other
three tools already established; its synthetic-fixture cases (§10 item 3) do not depend on the APK
tree at all and always run. `.venv/` is gitignored for the same reason the others' own venvs are.

---

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/reverse-engineering/tools/limited_dataflow/SPEC.md - https://tedsluis.github.io/opencontrolpixelbudspro2/reverse-engineering/tools/limited_dataflow/SPEC
