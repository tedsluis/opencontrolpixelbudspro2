# Structural Index

See [`SPEC.md`](SPEC.md) for the design, the exact query definition, and the governance
constraints this tool operates under (it never writes to `REVERSE_ENGINEERING.md`/`PROTOCOL.md`/
`DECISIONS.md`, and never judges a reference's relevance to the Bluetooth protocol — SPEC.md §8).

Reuses [`lambda_dispatcher_resolver`](../lambda_dispatcher_resolver/)'s own Layer 1
(`androguard_index.py`) directly for DEX loading — see SPEC.md §4.

## Setup

Requires the maintainer's own locally-decompiled APK tree to already exist under
`reverse-engineering/apk/<version>/` (see `APK_REVERSE_ENGINEERING_PROCEDURE.md`) — this tool never
ships or commits any decompiled content of its own (SPEC.md §11).

```sh
cd reverse-engineering/tools/structural_index
uv venv .venv --python 3.12
uv pip install --python .venv/bin/python -e ".[dev]"
```

## Usage

```sh
# Every construct/call/field-type reference to a class, across the whole APK:
.venv/bin/python3 -m structural_index.cli refs \
  --apk-root ../../apk/v1.0.955078536-10253511 --class esk

# Restrict (b) call sites to one method name — e.g. the abstract-supertype
# receiver-type case that made gjv.p()'s caller invisible to a field-search:
.venv/bin/python3 -m structural_index.cli refs \
  --apk-root ../../apk/v1.0.955078536-10253511 --class giz --method p

# Check whether any of a list of classes has zero external references anywhere
# in the APK (constructs + calls + field-type holders combined):
.venv/bin/python3 -m structural_index.cli unreferenced \
  --apk-root ../../apk/v1.0.955078536-10253511 --class aly --class cvo --class gza

# v1.1 (2026-09-17): every class DIRECTLY implementing an interface (not a
# transitive-via-superclass implementor — see SPEC.md §5a):
.venv/bin/python3 -m structural_index.cli implements \
  --apk-root ../../apk/v1.0.955078536-10253511 --interface fya

# v1.1 (2026-09-17): every iput/sput site writing into one class's own named
# field — the complementary query to `refs`'s field-*type*-declaration search
# (SPEC.md §5b):
.venv/bin/python3 -m structural_index.cli field-writes \
  --apk-root ../../apk/v1.0.955078536-10253511 \
  --class com.google.android.apps.wearables.maestro.companion.phone.bluetoothpriority.BluetoothPriorityReceiver \
  --field c
```

`--class` accepts the short form (`esk`), the default-package dotted form (`defpackage.esk`), or the
raw smali descriptor (`Lesk;`) — same three-spelling convention as `lambda_dispatcher_resolver`.

Add `--output-dir <dir>` to either command to write JSON to a file instead of stdout.

## Tests

```sh
.venv/bin/python3 -m pytest tests/ -v
```

The suite reads directly from the same local `reverse-engineering/apk/v1.0.955078536-10253511/`
tree used above and is **skipped** (not failed) if that tree isn't present — see
`tests/test_xref_index.py`'s own module docstring.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/reverse-engineering/tools/structural_index/README.md - https://tedsluis.github.io/opencontrolpixelbudspro2/reverse-engineering/tools/structural_index/README
