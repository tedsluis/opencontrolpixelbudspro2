# SPEC.md — pw_rpc Name Table

Technical specification for a small tool that names the pw_rpc service and method ids seen on the
wire of this project's captures, by hashing every string literal of the locally decompiled companion
APK. Built in `ai-sessions/0073` (2026-10-06), following `../uuid_ble_context/SPEC.md`'s template; it
is the "name-hash table" of that session's prompt (§4.4 item 2).

**Status: implemented 2026-10-06** (`src/`, `tests/`, `README.md`), against this document's §5/§6/§10.

---

## 1. Problem statement

A pw_rpc packet carries its service and method as 32-bit ids that are the 65599 hash of their names
(`PROTOCOL.md` §2.2a, `DECISIONS.md` ADR-034). `scripts/pwrpc_decode.py` holds a hand-kept table of
nine service and fourteen method names; an id outside it prints as hex. `CAP-069` showed two such
ids (`0xbf6c9399`, `0x8d99df93`) and the project planned to decompile a newer APK to name them
(`TODO.md` §4). The names are string literals of the app, so the question "is the name in the tree we
already have?" is mechanical: hash every literal, compare.

## 2. Scope

### 2.1 In scope (v1)

- **`census`** — every service id and (service, method) id on the wire of the given capture logs,
  with packet counts, directions, packet types, the number of logs and the first (log, frame).
- **`match`** — every Java string literal under `jadx-output/sources/` and every smali
  `const-string` under `apktool-output*/smali*/` is hashed once; a literal whose hash equals a wire
  id names it, with up to four places where the literal occurs.
- **`hash`** — the 65599 hash of names given on the command line (a positive control).

### 2.2 Out of scope (v1)

- **Meaning.** That `0x73d5d805` is `maestro_pw.Dosimeter` says nothing about what the service does
  (§8).
- **Pairing a method name with its service by code.** A method id is matched on its own hash; that
  the literal sits in the same service descriptor as the service name is for the reader to check at
  the reported `file:line`.
- Names built at run time (string concatenation), resources (`res/`), assets.
- Another APK version; a diff between versions (`../BACKLOG.md`, "APK version-diff tool").

## 3. Definitions

- **Wire id**: fields 3 (`service_id`) and 4 (`method_id`) of a pw_rpc `RpcPacket`, both fixed32,
  read by `scripts/pwrpc_decode.py`'s `packets()` (RFCOMM DLCI 2 and 3, per ACL handle and direction).
- **Literal**: the unescaped content of a Java `"…"` string in a `.java` file, or of a smali
  `const-string` / `const-string/jumbo` operand. Java and smali escapes (`\n`, `\"`, `\uXXXX`, …) are
  decoded before hashing.
- **Match**: `h65599(literal) == id` — an equality of 32-bit hashes.

## 4. Architecture

One module (`names.py`), standard library plus this repository's own `scripts/pwrpc_decode.py`,
imported through a `sys.path` entry (the pattern `../schema_batch_extractor/` uses for
`scripts/decode_rawmessageinfo.py`): `h65599` and `packets()` are **not** re-implemented, so the
tool and the decoder cannot disagree. `census` needs `tshark` on `PATH` (as the decoder does);
`match` needs only the tree.

## 5. Interface (CLI)

```
pwrpc-name-table census [--repo-root R] [--log <log-or-glob> …] [--output wire.json]
pwrpc-name-table match  --apk-root <reverse-engineering/apk/vX> --wire-ids wire.json [--output table.json]
pwrpc-name-table hash   <name> …
```

`census` without `--log` reads `captures/*/*btsno*_hci*.log*`.

## 6. Output format

`census`: `logs` (packets per log), `errors` (logs `tshark` could not read), `services` and
`methods` (key `0x…` or `0x…/0x…`; `count`, `dir`, `types`, `logs`, `first`).
`match`: the same two tables with `names` added (`name`, `found_in`: up to four `path:line`),
plus `literal_occurrences_scanned`, `distinct_literals_hashed`, `unnamed_services`,
`unnamed_methods`. **Only literals that match a wire id are written**; the sweep's literal list
exists in memory only (§11).

## 7. Explicit failure modes (never silent)

- An unreadable log is listed under `errors` with its exception; the run continues.
- No log, no tree, or an unreadable `--wire-ids` file: an error on stderr, exit 1.
- **A match is a hash equality, not a proof.** With about 24,000 distinct literals and two dozen ids
  the expected number of chance matches is about 10⁻⁴, but degenerate literals exist: the
  one-character literal `"\u0000"` hashes to `1`. A reported name that does not look like a dotted
  service name or a method identifier, or whose `found_in` is not a service descriptor, is to be
  doubted. Several names for one id are all listed.
- An id with no matching literal is listed under `unnamed_*` — together with
  `distinct_literals_hashed` this is the checked negative ("not a literal of this version").

## 8. Governance (binding, not optional)

`../BACKLOG.md`'s governance block applies: mechanical assistance only (`DECISIONS.md` ADR-017). The
tool lists; it writes into no document, and adding a name to `scripts/pwrpc_decode.py`'s table or to
`PROTOCOL.md` is a proposal for the maintainer (`AGENTS.md` §6).

## 9. Why the scope stops here

The method-to-service pairing and the request/response types are one read of the reported line away
(`fux.java` holds them side by side); automating that would mean parsing the descriptor builder, for
two dozen ids. A version diff belongs to the backlog's own tool.

## 10. Test plan / acceptance criteria

`tests/test_names.py`:

1. **Synthetic, always run** (invented literals in `tmp_path`): the hash values
   `scripts/pwrpc_decode.py` documents (`maestro_pw.Maestro` = `0x7ede71ea`, `WriteSetting` =
   `0x9e8c9a1d`); literals from Java and smali, with an escaped quote and `const-string/jumbo`;
   `match` names what is in the tree, lists what is not, and counts occurrences and distinct
   literals; the output holds only matched names; the CLI's error exits.
2. **Real tree, skipped without it**: the ids `PROTOCOL.md` §2.2a records as 🟢 FACT (Maestro with
   four methods, Dosimeter, `UpdateHelperService`) are all named from the tree; an id with no
   literal (`0xdeadbeef`) is reported unnamed.

Result on 2026-10-06 (tree present): 7 passed, 0 skipped. First real use the same day
(`ai-sessions/0073`): 80 logs, 21,692 packets, 8 service ids and 16 method ids; 75,538 literal
occurrences, 23,766 distinct; **0 unnamed** — including `0xbf6c9399` and `0x8d99df93`.

## 11. Directory layout and git-tracking boundary

```
pwrpc_name_table/
  SPEC.md  README.md  pyproject.toml
  src/pwrpc_name_table/{__init__,names,cli}.py
  tests/test_names.py
```

Tracked: the files above. Never tracked: `.venv/`, caches, and output JSON (write it outside the
repository). The literal sweep is never dumped: a string table copied wholesale out of the APK is
what `PROJECT_RULES.md` §8 rule 20 forbids; the names that match a wire id are protocol identifiers
the Buds expect byte for byte (rule 22).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/reverse-engineering/tools/pwrpc_name_table/SPEC.md - https://tedsluis.github.io/opencontrolpixelbudspro2/reverse-engineering/tools/pwrpc_name_table/SPEC
