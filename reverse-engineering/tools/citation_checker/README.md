# Citation Checker

Checks every `file:line` citation that the project's documents make into the locally decompiled
companion APK. See [`SPEC.md`](SPEC.md) for the citation shapes, the verdicts and the governance
(it never writes to a document and never judges a claim — SPEC.md §8).

## Setup

Needs the maintainer's own decompiled tree under `reverse-engineering/apk/<version>/`
(`APK_REVERSE_ENGINEERING_PROCEDURE.md`). Standard library only.

```sh
cd reverse-engineering/tools/citation_checker
uv venv .venv --python 3.12
uv pip install --python .venv/bin/python -e ".[dev]"
```

## Usage

```sh
# All default documents (REVERSE_ENGINEERING.md, PROTOCOL.md, DECISIONS.md, DESKRESEARCH_FINDINGS.md,
# ARCHITECTURE.md, TODO.md, APK_REVERSE_ENGINEERING_PROCEDURE.md, every CAP-NNN-FINDINGS.md):
.venv/bin/python3 -m citation_checker.cli check \
  --apk-root ../../apk/v1.0.955078536-10253511 --output /tmp/citations.json   # prints the summary

# One document, only the citations worth a look:
.venv/bin/python3 -m citation_checker.cli check \
  --apk-root ../../apk/v1.0.955078536-10253511 --doc REVERSE_ENGINEERING.md \
  --verdict drifted --verdict range_out_of_file --verdict missing_file
```

Verdicts: `confirmed`, `drifted`, `in_file_no_token`, `exists`, `range_out_of_file`, `missing_file`
(SPEC.md §6). Read `drifted` as a lead (the quoted string may belong to the next sentence) and
`missing_file` with the document open (an AOSP or protobuf file name is not a citation into the
tree). A bare `:N` whose file is named in no cell, column header or paragraph is not listed.

Write the JSON outside the repository; it holds line numbers and the documents' own quoted strings,
never decompiled text.

## Tests

```sh
.venv/bin/python3 -m pytest tests/ -v
```

The synthetic tests always run; the real-tree tests are **skipped** (not failed) when
`reverse-engineering/apk/v1.0.955078536-10253511/` is absent.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/reverse-engineering/tools/citation_checker/README.md - https://tedsluis.github.io/opencontrolpixelbudspro2/reverse-engineering/tools/citation_checker/README
