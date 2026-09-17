# Limited Dataflow Analysis (single-basic-block only)

See [`SPEC.md`](SPEC.md) for the design, the exact four recognized instruction shapes (§3), and the
governance constraints this tool operates under (it never writes to
`REVERSE_ENGINEERING.md`/`PROTOCOL.md`/`DECISIONS.md`, and never judges a sink use's relevance to the
Bluetooth protocol — SPEC.md §8). **This tool never follows a branch, a loop, or a call into its own
callee** — SPEC.md §1's own worked example shows exactly where that boundary falls on a real,
already-documented example (`esk`'s discriminator-19 branch).

Reuses [`lambda_dispatcher_resolver`](../lambda_dispatcher_resolver/) directly (via a `sys.path`
insertion to the sibling tool's `src/` directory) for method/branch location — see SPEC.md §4.

## Setup

Requires the maintainer's own locally-decompiled APK tree to already exist under
`reverse-engineering/apk/<version>/` (see `APK_REVERSE_ENGINEERING_PROCEDURE.md`) — this tool never
ships or commits any decompiled content of its own (SPEC.md §11).

```sh
cd reverse-engineering/tools/limited_dataflow
uv venv .venv --python 3.12
uv pip install --python .venv/bin/python -e ".[dev]"
```

## Usage

```sh
# The primary worked example (SPEC.md §1/§10): trace qjc's own register (v0) forward
# through esk's discriminator-19 branch, starting right after the `iget-object v0, p0,
# Lesk;->b:Ljava/lang/Object;` line that captures it:
.venv/bin/python3 -m limited_dataflow.cli trace-branch \
  --apk-root ../../apk/v1.0.955078536-10253511 \
  --class esk --discriminator 19 \
  --start-line <the exact line number from lambda-resolver resolve's own smali.branch_line_range> \
  --start-register v0

# Any plain method's own body, no dispatcher resolution:
.venv/bin/python3 -m limited_dataflow.cli trace-method \
  --apk-root ../../apk/v1.0.955078536-10253511 \
  --class <class> --method <name> \
  --start-line <int> --start-register <vN|pN>
```

`--start-line` must fall inside the resolved branch's/method's own line range and must actually
mention `--start-register` — the caller supplies the exact starting point (typically obtained by
first reading a `lambda_dispatcher_resolver resolve`/`structural_index refs` result), this tool does
not search for one on its own (SPEC.md §2.2).

Add `--output-dir <dir>` to either command to write JSON to a file instead of stdout.

## Tests

```sh
.venv/bin/python3 -m pytest tests/ -v
```

The suite's APK-dependent cases read directly from the same local
`reverse-engineering/apk/v1.0.955078536-10253511/` tree used above and are **skipped** (not failed)
if that tree isn't present; its synthetic-fixture cases (a handful of hand-written smali lines, not
an excerpt of the companion app's own source) always run — see `tests/test_dataflow.py`'s own module
docstring.

---

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/reverse-engineering/tools/limited_dataflow/README.md - https://tedsluis.github.io/opencontrolpixelbudspro2/reverse-engineering/tools/limited_dataflow/README
