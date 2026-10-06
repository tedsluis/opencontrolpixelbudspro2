# pw_rpc Name Table

Names the pw_rpc service and method ids seen on the wire by hashing every string literal of the
locally decompiled companion APK. See [`SPEC.md`](SPEC.md) — in particular §7 (a match is a hash
equality) and §8 (it lists, it does not decide).

## Setup

Needs the decompiled tree under `reverse-engineering/apk/<version>/` and, for `census`, `tshark`.

```sh
cd reverse-engineering/tools/pwrpc_name_table
uv venv .venv --python 3.12
uv pip install --python .venv/bin/python -e ".[dev]"
```

## Usage

```sh
# 1. Which ids are on the wire, in every capture log (about a minute):
.venv/bin/python3 -m pwrpc_name_table.cli census --output /tmp/wire_ids.json

# 2. Name them from the tree (a few seconds); the summary goes to stderr:
.venv/bin/python3 -m pwrpc_name_table.cli match \
  --apk-root ../../apk/v1.0.955078536-10253511 --wire-ids /tmp/wire_ids.json --output /tmp/name_table.json

# Positive control — the hash of a name you already know:
.venv/bin/python3 -m pwrpc_name_table.cli hash maestro_pw.Maestro WriteSetting
```

`unnamed_services` / `unnamed_methods` in the output are the ids no literal of this version hashes
to; `distinct_literals_hashed` is the size of the search. Write the JSON outside the repository.

## Tests

```sh
.venv/bin/python3 -m pytest tests/ -v
```

The synthetic tests always run; the real-tree tests are **skipped** (not failed) without
`reverse-engineering/apk/v1.0.955078536-10253511/`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/reverse-engineering/tools/pwrpc_name_table/README.md - https://tedsluis.github.io/opencontrolpixelbudspro2/reverse-engineering/tools/pwrpc_name_table/README
