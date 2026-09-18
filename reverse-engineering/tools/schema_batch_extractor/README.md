# Schema Batch-Extractor

See [`SPEC.md`](SPEC.md) for the design, the exact detection/decode definition, the one disclosed
data-source limitation (§3 — it cannot resolve a plain singular `MESSAGE` field's referenced class,
only oneof/list/map ones), and the governance constraints this tool operates under (it never writes
to `REVERSE_ENGINEERING.md`/`PROTOCOL.md`/`DECISIONS.md`, and never judges a field's relevance to the
Bluetooth protocol — SPEC.md §8).

Reuses [`scripts/decode_rawmessageinfo.py`](../../../scripts/decode_rawmessageinfo.py) directly (via
a `sys.path` insertion to the repo-root `scripts/` directory) for the actual `RawMessageInfo`
decoding — see SPEC.md §4. That single-file script is unmodified and still works standalone for a
one-off single-class lookup.

## Setup

Requires the maintainer's own locally-decompiled APK tree to already exist under
`reverse-engineering/apk/<version>/` (see `APK_REVERSE_ENGINEERING_PROCEDURE.md`) — this tool never
ships or commits any decompiled content of its own (SPEC.md §11).

```sh
cd reverse-engineering/tools/schema_batch_extractor
uv venv .venv --python 3.12
uv pip install --python .venv/bin/python -e ".[dev]"
```

## Usage

```sh
# Full register: every RawMessageInfo-shaped class under jadx-output/sources/, decoded:
.venv/bin/python3 -m schema_batch_extractor.cli scan \
  --apk-root ../../apk/v1.0.955078536-10253511

# Just the classes already known from REVERSE_ENGINEERING.md's own recovery work:
.venv/bin/python3 -m schema_batch_extractor.cli scan \
  --apk-root ../../apk/v1.0.955078536-10253511 --class qhr --class qjc --class qjb --class nqx

# The "candidate rich schemas" ranked-list view (REVERSE_ENGINEERING.md's own section),
# reproduced from a real, tested tool instead of a one-off, uncommitted header-only script:
.venv/bin/python3 -m schema_batch_extractor.cli scan \
  --apk-root ../../apk/v1.0.955078536-10253511 --min-field-count 20

# Which other classes' own schema declares a message-typed (oneof/list/map) reference to `qju`:
.venv/bin/python3 -m schema_batch_extractor.cli refs \
  --apk-root ../../apk/v1.0.955078536-10253511 --class qju
```

`--class` accepts the short form (`qhr`) or the `defpackage.`-qualified form (`defpackage.qhr`) —
this tool never touches DEX/smali data, so it does not accept the raw smali descriptor form the other
two tools do.

Add `--output-dir <dir>` to either command to write JSON to a file instead of stdout.

## Tests

```sh
.venv/bin/python3 -m pytest tests/ -v
```

The suite reads directly from the same local `reverse-engineering/apk/v1.0.955078536-10253511/`
tree used above and is **skipped** (not failed) if that tree isn't present — see
`tests/test_batch_extract.py`'s own module docstring.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/reverse-engineering/tools/schema_batch_extractor/README.md - https://tedsluis.github.io/opencontrolpixelbudspro2/reverse-engineering/tools/schema_batch_extractor/README
