# UUID + BLE/GATT Context Reconstruction

See [`SPEC.md`](SPEC.md) for the design, the exact extraction/co-occurrence definition, and the
governance constraints this tool operates under (it never writes to
`REVERSE_ENGINEERING.md`/`PROTOCOL.md`/`DECISIONS.md`, and never judges a UUID's relevance to the
Bluetooth protocol — SPEC.md §8).

Reuses [`structural_index`](../structural_index/)'s own `find_refs`/`load_apk` directly for the
"usage location" half (SPEC.md §4) — `structural_index` itself reuses
[`lambda_dispatcher_resolver`](../lambda_dispatcher_resolver/)'s Layer 1 one hop further down.

## Setup

Requires the maintainer's own locally-decompiled APK tree to already exist under
`reverse-engineering/apk/<version>/` (see `APK_REVERSE_ENGINEERING_PROCEDURE.md`) — this tool never
ships or commits any decompiled content of its own (SPEC.md §11).

```sh
cd reverse-engineering/tools/uuid_ble_context
uv venv .venv --python 3.12
uv pip install --python .venv/bin/python -e ".[dev]"
```

## Usage

```sh
# Every UUID-shaped literal anywhere in the decompiled tree, with occurrences:
.venv/bin/python3 -m uuid_ble_context.cli extract \
  --apk-root ../../apk/v1.0.955078536-10253511

# One UUID's occurrences plus each owning class's structural_index usage graph:
.venv/bin/python3 -m uuid_ble_context.cli context \
  --apk-root ../../apk/v1.0.955078536-10253511 \
  --uuid 25e97ff7-24ce-4c4c-8951-f764a708f7b5
```

Add `--output-dir <dir>` to either command to write JSON to a file instead of stdout.

## Tests

```sh
.venv/bin/python3 -m pytest tests/ -v
```

The suite reads directly from the same local `reverse-engineering/apk/v1.0.955078536-10253511/`
tree used above and is **skipped** (not failed) if that tree isn't present — see
`tests/test_uuid_scan.py`'s own module docstring.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/reverse-engineering/tools/uuid_ble_context/README.md - https://tedsluis.github.io/opencontrolpixelbudspro2/reverse-engineering/tools/uuid_ble_context/README
