# Lambda Dispatcher Resolver

See [`SPEC.md`](SPEC.md) for the design, the structural detection rules, and the governance
constraints this tool operates under (it never writes to `REVERSE_ENGINEERING.md`/`PROTOCOL.md`/
`DECISIONS.md`, and never judges a branch's relevance to the Bluetooth protocol — SPEC.md §8).

## Setup

Requires the maintainer's own locally-decompiled APK tree to already exist under
`reverse-engineering/apk/<version>/` (see `APK_REVERSE_ENGINEERING_PROCEDURE.md`) — this tool never
ships or commits any decompiled content of its own (SPEC.md §11).

```sh
cd reverse-engineering/tools/lambda_dispatcher_resolver
uv venv .venv --python 3.12          # androguard's own dependency set targets 3.10-3.13
uv pip install --python .venv/bin/python -e ".[dev]"
```

## Usage

```sh
# List every candidate lambda-dispatcher class in the APK (SPEC.md §3's structural shape check):
.venv/bin/python3 -m lambda_dispatcher_resolver.cli list \
  --apk-root ../../apk/v1.0.955078536-10253511

# Resolve one (class, discriminator) pair to its exact smali + JADX evidence:
.venv/bin/python3 -m lambda_dispatcher_resolver.cli resolve \
  --apk-root ../../apk/v1.0.955078536-10253511 \
  --class aie --discriminator 7

# Resolve EVERY defined case of one class in a single pass (plus its default
# branch) — the effective way to use this tool on a class you already know
# is protocol-relevant but have only ever spot-checked one discriminator of:
.venv/bin/python3 -m lambda_dispatcher_resolver.cli resolve-all \
  --apk-root ../../apk/v1.0.955078536-10253511 --class aie \
  --output-dir /tmp/aie_all      # omit --output-dir to get one JSON array on stdout instead
```

`--class` accepts the short form (`aie`), the default-package dotted form (`defpackage.aie`), or
the raw smali descriptor (`Laie;`) — all three resolve to the same class.

`resolve-all` never guesses a discriminator range to try — it resolves exactly the values the
class's own dispatch table actually defines (read from the `packed-switch`/`sparse-switch` data or
the `if`-chain's own checked values), plus exactly one extra entry for the default/"no case
matched" branch. With `--output-dir`, each case is written as its own
`<class>_<value>.json`/`<class>_default.json` file; without it, the whole set prints as one JSON
array to stdout.

## Tests

```sh
.venv/bin/python3 -m pytest tests/ -v
```

The suite reads directly from the same local `reverse-engineering/apk/v1.0.955078536-10253511/`
tree used above and is **skipped** (not failed) if that tree isn't present — see `tests/test_resolver.py`'s
own module docstring for why it deliberately does not ship committed fixture copies of decompiled
code.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/reverse-engineering/tools/lambda_dispatcher_resolver/README.md - https://tedsluis.github.io/opencontrolpixelbudspro2/reverse-engineering/tools/lambda_dispatcher_resolver/README
