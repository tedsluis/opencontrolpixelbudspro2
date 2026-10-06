# SPEC.md — Citation Checker

Technical specification for a small, standalone tool that finds every `file:line` citation the
project's markdown documents make into the locally decompiled companion APK tree and checks each
one against the files on disk. Built in `ai-sessions/0073` (2026-10-06) following
`../uuid_ble_context/SPEC.md`'s template; it is the "citation checker" of that session's prompt
(§4.4 item 1). Not one of `../BACKLOG.md`'s earlier ideas — it is recorded there as built.

**Status: implemented 2026-10-06** (`src/`, `tests/`, `README.md`), against this document's §5/§6/§10.

---

## 1. Problem statement

`REVERSE_ENGINEERING.md` alone carries more than 800 citations of the form `` `fyo.java:124-144` ``,
`` `cmi.smali:3499` `` or `` (`:278-311`) ``; `PROTOCOL.md`, `DECISIONS.md` and the capture FINDINGS
carry about 150 more. They were written by hand over five weeks, by many sessions. `ai-sessions/0068`
(`A68-RE-04`) found twelve of them off by 1–4 lines in a sample, and `REVERSE_ENGINEERING.md`'s own
"Known limitations" notes that a citation is only reproducible against the decompiler version that
produced it. Nothing checked them mechanically. This tool does: one run, every citation, one verdict
each.

## 2. Scope

### 2.1 In scope (v1)

- **Parse** three citation shapes (§3) from markdown text.
- **Resolve** the cited file name to a file under `jadx-output/sources/**`,
  `apktool-output*/smali*/**`, or the apktool `AndroidManifest.xml` / `res/{values,xml,layout,navigation}/*.xml`.
- **Check** that the cited line range lies inside the file.
- **Compare tokens**: strings the citing sentence itself puts in double quotes (a log string) or in
  backticks (a code fragment) are searched in the cited file; found inside the range (± a slack of 3
  lines) ⇒ `confirmed`, found only elsewhere ⇒ `drifted` with the nearest line.

### 2.2 Out of scope (v1)

- **Judging the claim.** Whether the sentence's *reading* of the code is right is not decided
  (§8). `in_file_no_token` means "the range exists, nothing quoted to compare" — a person or a
  review pass reads those.
- **Citations into anything else**: this repository's own files (`scripts/pwrpc_decode.py:25`,
  `android/…/*.kt`), capture frame numbers, AOSP or protobuf sources named in the text. A `.java`
  name that is not in the tree is reported `missing_file`; the reader decides whether it was meant
  as a citation into the tree at all (`BluetoothDevice.java` is AOSP's).
- **Fixing** a document. The tool never writes to a document.
- A table whose header names two files in one column, or a continuation whose file is named only
  in an earlier paragraph: not attributed (§7).

## 3. What counts as a citation

| Kind | Shape | Example |
|---|---|---|
| `explicit` | `<path/>Name.(java\|smali\|xml):N` or `:N-M` (hyphen or en dash; a space before the colon is accepted) | `` `fyo.java:124-144` ``, `` `apktool-output/smali_classes2/cmi.smali:3499` ``, `hfb.smali :1422–1442` |
| `continuation` | a bare `:N` / `:N-M` — the file is the one named last **in the same table cell**, else the single file the table's **header row** names in that column, else (prose) the file named last in the same paragraph | `` case 13 (`:278-311`) `` under a column headed ``Read case (`fxb.java`)`` |
| `bare` | `` `Name.java` `` / `` `Name.smali` `` in backticks, no line | `` `UserEqFragment.java` `` |

A path may contain `…` or `...` segments (`com/google/.../OtaFragment.java`); the literal segments
must occur in order in the resolved path. With several files of the same base name the default
package wins (`defpackage/` in JADX, the top of a smali root), and `apktool-output/` wins over a
split's `apktool-output-<abi>/`; `candidates` in the output says how many there were.

**Expected tokens** are taken from the text between the previous and the next citation, at most 320
characters each side, inside the paragraph: every double-quoted string of ≥ 6 characters, and every
backticked fragment of ≥ 8 characters that is not a file name and contains a space, `.`, `(` or `>`
(a bare class name occurs everywhere and proves nothing). A token that does not occur in the cited
file at all is ignored — the sentence quotes it about something else.

## 4. Architecture

One module, standard library only (`check.py`): `TreeIndex` (one `os.walk` over the tree, base name →
paths, files read lazily and cached), `parse_citations`, `check_document`, `summarize`; `cli.py` wraps
them. No androguard: line numbers are a property of the decompiled *text*, which is what the
documents cite.

## 5. Interface (CLI)

```
citation-checker check --apk-root <reverse-engineering/apk/vX> [--repo-root <repo>]
                       [--doc <file-or-glob> …] [--slack N] [--verdict V …] [--output out.json]
```

Without `--doc` the documents are: `REVERSE_ENGINEERING.md`, `PROTOCOL.md`, `DECISIONS.md`,
`DESKRESEARCH_FINDINGS.md`, `ARCHITECTURE.md`, `TODO.md`, `APK_REVERSE_ENGINEERING_PROCEDURE.md` and
`captures/*/CAP-*-FINDINGS.md`. `ai-sessions/` is left out on purpose (session records are history,
not maintained). `--verdict` limits the listed citations (the summary always counts all).

## 6. Output format

JSON: `summary` (`total_citations`, `by_verdict`, `by_document`) and `citations`, each with `doc`,
`doc_line`, `raw`, `kind`, `name`, `start`, `end`, `resolved` (path relative to the APK root),
`candidates`, `file_lines`, `verdict`, `nearest_token_line`, and `tokens` (`token`, `in_range`,
`found_lines` — up to five line numbers). **No line of decompiled text is ever written**: the tokens
are the document's own words (§11).

| Verdict | Meaning |
|---|---|
| `confirmed` | a token the sentence quotes is inside the cited range (± slack) |
| `drifted` | quoted tokens occur in the file, none inside the range; `nearest_token_line` says where |
| `in_file_no_token` | the range lies inside the file; no quoted token occurs in the file |
| `exists` | a bare citation whose file exists |
| `range_out_of_file` | the file is shorter than the cited range |
| `missing_file` | no file of that name in the tree |

`drifted` is a lead, not a finding: the token may belong to a neighbouring sentence. `confirmed` says
the quoted string is where the document says — not that the document's reading of it is right.

## 7. Explicit failure modes (never silent)

- No `jadx-output/sources/` under `--apk-root`, or a `--doc` pattern that matches nothing: an error
  on stderr, exit 1.
- A continuation with no file named in its cell, column header or paragraph is **not listed** (it
  cannot be attributed); the README says so. Clock times (`08:24:17`) are not continuations.
- A file that cannot be decoded as UTF-8 is read with replacement characters, not skipped.

## 8. Governance (binding, not optional)

`../BACKLOG.md`'s governance block applies unchanged: mechanical assistance only (`DECISIONS.md`
ADR-017) — the tool lists and compares, it never decides that a citation or a claim is right or
relevant and never writes into `REVERSE_ENGINEERING.md`, `PROTOCOL.md`, `DECISIONS.md` or a
`CAP-NNN-FINDINGS.md`. A correction of a document is a dated Update proposed to the maintainer
(`AGENTS.md` §6, `PROJECT_RULES.md` rule 9a).

## 9. Why the scope stops here

A checker that tried to judge claims would need to understand them; that is the review a person or
a reading pass does with this tool's list in hand (`ai-sessions/0073` did exactly that for the 448
citations without a comparable token). Storing a structural signature next to each citation, so that
it survives a decompiler upgrade (`REVERSE_ENGINEERING.md`, "Known limitations"), is a possible v2;
the `tokens` this tool already extracts are a first approximation of such a signature.

## 10. Test plan / acceptance criteria

`tests/test_check.py`, two groups:

1. **Synthetic, always run** (an invented mini tree under pytest's `tmp_path`, no decompiled
   content): the three citation shapes, the en dash and the space before the colon, the `…` path, a
   continuation that must not cross a blank line, the table-header column rule, a clock time that is
   not a continuation; every verdict; base-name ambiguity; the apktool-split preference; the CLI's
   JSON, summary and error exits; and that the JSON carries no file content.
2. **Real tree, skipped without it** — already-confirmed citations as regression fixtures:
   `gbm.java:36` with its log string (`confirmed`), `cmi.smali:3499` with its two smali tokens
   (`confirmed`, `ai-sessions/0069`), `a.java` resolving to `defpackage/a.java` among several, the
   same log string cited at a wrong line (`drifted`, nearest line 36), and the whole default
   document set (more than 900 citations, none `range_out_of_file`).

Result on 2026-10-06 (tree present): 20 passed, 0 skipped (the preference-XML case was added the same day, when a citation of
`res/xml/settings_preferences.xml:7` did not resolve).

## 11. Directory layout and git-tracking boundary

```
citation_checker/
  SPEC.md  README.md  pyproject.toml
  src/citation_checker/{__init__,check,cli}.py
  tests/test_check.py
```

Tracked: the files above. Never tracked: `.venv/`, caches, and any output JSON written into the
repository (write it to a scratch directory). The tests contain invented file content only; the
real-tree tests name citations by file and line and quote only strings the tracked documents already
quote (`PROJECT_RULES.md` §8 rule 20 (a)).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/reverse-engineering/tools/citation_checker/SPEC.md - https://tedsluis.github.io/opencontrolpixelbudspro2/reverse-engineering/tools/citation_checker/SPEC
