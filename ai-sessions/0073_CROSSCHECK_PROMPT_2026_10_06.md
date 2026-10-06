# 0073_CROSSCHECK_PROMPT_2026_10_06.md — Second, intensive reverse-engineering pass over the companion APK `v1.0.955078536-10253511`: verify every recorded APK finding, extend the registers, and find leads for every open question of the whole project, using and extending the tools in `reverse-engineering/tools/`

**Number:** 0073
**Category:** CROSSCHECK
**Date:** 2026-10-06
**Title:** Re-reverse-engineer the official Pixel Buds app `v1.0.955078536-10253511` (JADX + apktool output under `reverse-engineering/apk/`) exhaustively — (1) check every APK-derived claim the project has recorded, (2) complete the schema/field/service registers, (3) look for code-side leads for **every** 🔴 OPEN QUESTION and 🟡 HYPOTHESIS of the whole project — with the five analysis tools in `reverse-engineering/tools/` (run, repair, extend) and new tools where the work needs them; **mechanical assistance only (ADR-017), proposals only (`AGENTS.md` §6), no app change, no decompiled content committed**

---

## 0. How to use this prompt

You are an expert Android reverse engineer, software architect and technical auditor. This prompt is for a **fresh** Claude Code session in this
repository. Run the phases of §5 **strictly in order**; do not start a phase before the previous one is done and recorded. The work is large and is
expected to span more than one session (§0.2).

The category is `CROSSCHECK` (`AI_SESSION_LOG_PROCEDURE.md` §2: "cross-validating existing findings against captures/APK") because the first job is to
check what is already recorded; the register extensions and the open-question leads are the second and third jobs of the same pass.

### 0.1 Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9)

Before any other action, read, in this order and **in full**: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md`,
`DECISIONS.md` (**every** ADR, ADR-001 … the highest number, with every dated Update — ADR-003, ADR-017, ADR-018, ADR-019, ADR-025, ADR-034, ADR-041 are
the ones this session acts under), `TODO.md`. These are the ground rules; do not search, change or build anything before they are read.

Then, per task, each file **in full** before anything in it is relied on:

- `AI_SESSION_LOG_PROCEDURE.md` (incl. §4b, the closing checklist), `ai-sessions/INDEX.md`, `id_registry.csv`.
- **`REVERSE_ENGINEERING.md` in full, start to finish** (≈ 4,400 lines) — the whole accumulated APK knowledge, every dated Update, the `qhr` register,
  the "Candidate rich schemas" section, the UUID register, the Message Group/Code register, the "GSND" naming lead, the resource/string-table sweep, the
  native-libraries table, the call-graph notes, the "Correlation status with PROTOCOL.md" table and "Known limitations". Every entry of this file is
  checked in Phase 2 — read it with that in mind.
- `APK_REVERSE_ENGINEERING_PROCEDURE.md` in full (§4 keyword search, §4a lambda dispatchers, §4.1 exclusion list, §5, §6 gotchas, §7 checklist).
- `reverse-engineering/APK_VERSIONS.md` (the analysed version, SHA-256s, tool versions; note that `1.0.990706425` is pulled but **not** decompiled and
  **not** part of this session — `TODO.md` §4, the maintainer's *"Voer het nog niet uit. Dat kan later."*).
- `reverse-engineering/tools/BACKLOG.md` in full (governance block, priority order, the three not-yet-built ideas) and, for each of the five tools,
  `SPEC.md` and `README.md` in full: `lambda_dispatcher_resolver/`, `structural_index/`, `schema_batch_extractor/`, `uuid_ble_context/`,
  `limited_dataflow/`. Also their `src/` and `tests/` (you will run and may change them).
- `scripts/decode_rawmessageinfo.py`, `scripts/pwrpc_decode.py` (its `h65599` name hash and the service/method name tables), `scripts/decode_qhr_settings.py`
  (superseded — `TODO.md` §6, `A68-RE-06`), `scripts/lint_docs.py` (what it checks), `scripts/ensure_footers.py`.
- `DESKRESEARCH_FINDINGS.md` in full (every entry carries open items; the 2026-10-03 entry has three proposals awaiting the maintainer).
- The earlier APK-RE sessions, **in full**: `ai-sessions/0024_AUDIT_RESULT_2026_09_16.md` (the open-question inventory A–M and the tool priorities),
  `0025_MAINTENANCE_RESULT_2026_09_16.md` (the four tools built, what each found), `0027_MAINTENANCE_RESULT_2026_09_17.md` (item M string sweep,
  `implements` query), `0007_CROSSCHECK_RESULT_2026_09_11.md` (the independent review of the RE catalogue), `0001_CROSSCHECK_RESULT_2026_09_07.md`,
  `0003_MAINTENANCE_RESULT_2026_09_08.md` (Phase 3/4), `0023_CROSSCHECK_RESULT_2026_09_15.md`; the RE findings of the last audit,
  `0068_AUDIT_RESULT_2026_10_03.md` (every `A68-RE-*` item and §6.6) and how `0069_MAINTENANCE_RESULT_2026_10_03.md` processed them.
- The capture findings whose open questions point at the code (read their "Open questions"/"Proposals" sections; the rest as needed):
  `CAP-069-FINDINGS.md` (§9: the unnamed service `0xbf6c9399`, `JitterBuffer` `0x8d99df93`), `CAP-058-FINDINGS.md` (§1 the process restart, §4 Find
  device, §6 field 21), `CAP-054-FINDINGS.md` (§2, §9), `CAP-053-FINDINGS.md` (§3 the `hod` navigate-away path, §4), `CAP-061-FINDINGS.md` §2 (the
  "Bisto" lead for DLCI 0x08/0x0a), `CAP-036-FINDINGS.md`, `CAP-033-FINDINGS.md` §3 (the SDP service names), `CAP-016-FINDINGS.md` §10 (HID).
- Every other `captures/CAP-NNN-*/CAP-NNN-FINDINGS.md` (65 files): at least its open-questions section — Phase 1 builds the project-wide inventory
  from them; say in the RESULT which were read in full and which only partly.

Say in the RESULT which files you read in full and which only partly, and why.

### 0.2 Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically

Create **ai-sessions/0073_CROSSCHECK_RESULT_2026_10_06.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and
update that block at the end of every phase **and** after every substantial step within a phase (every register section verified, every tool's test run,
every tool change, every lead worked, every checkpoint answer): what is done, what is next, which files are touched but unverified, and where
intermediate results live (the session's scratchpad directory — tool outputs as JSON, the hash table, the inventory CSV; re-create them with the
recorded commands if the scratchpad is gone). A resumed session reads this prompt, then the RESULT's Progress block, re-reads the files the unfinished
step touched, and continues from there. It never redoes a finished, recorded step and never assumes an unrecorded step was done. The prompt keeps its
number and date; the RESULT keeps growing in the same file.

**Nothing is taken on trust, including this prompt, `REVERSE_ENGINEERING.md`'s own entries, earlier RESULTs and the maintainer's statements.** Every
claim below (§2) is re-derived from the decompiled tree, the logs and the documents.

---

## 1. What the maintainer asked for (chat, 2026-10-06, translated from Dutch; the original wording is at the end of this file)

Create a prompt that, in a new session, **reverse-engineers the APK in `reverse-engineering/apk/v1.0.955078536-10253511/` once more and intensively**, in
order to **check** the recorded findings, **supplement** them, and **find further leads for all open questions of the entire project**. **Use the scripts
in `reverse-engineering/tools/`, and improve and/or extend those scripts where needed.**

Read as four deliverables, in this order of priority:

1. **Verification** — every APK-derived statement in `REVERSE_ENGINEERING.md` (and every `file:line` citation that `PROTOCOL.md`, `DECISIONS.md`,
   `DESKRESEARCH_FINDINGS.md` and the capture FINDINGS make into the decompiled tree) is re-checked against the tree on disk: does the file exist, does the
   cited line range still hold the cited identifier/log string/schema, is the stated reading right? Each entry gets a verdict — **confirmed**, **drifted**
   (right content, wrong line), **wrong** (with the correct reading), or **not checkable** (why).
2. **Completion** — the registers that are known to be partial are completed from the tree: the `qhr` field register (every one of the 38 fields with
   its write site, read site, UI label where traceable, and wire samples per capture), the pw_rpc service/method table (every id seen on any wire,
   named or not), the response/stream schemas (`SubscribeRuntimeInfo`, `GetHardwareInfo`, `Dosimeter`, `DynamicServerConfigService`, …), the UUID
   register, the Message Group/Code register, the "Candidate rich schemas".
3. **Leads** — for **every** 🔴 OPEN QUESTION and 🟡 HYPOTHESIS of the whole project (Phase 1 builds the inventory), say what the companion app's own code
   can and cannot contribute, and work every item the code can contribute to.
4. **Tooling** — run, repair and extend the five tools so that 1–3 are done mechanically and reproducibly; build a new tool only where the work needs
   it (candidates in §4), each following the existing `SPEC.md` template and `BACKLOG.md`'s governance block.

---

## 2. Context — verify each item before relying on it

Measured by the chat that wrote this prompt (2026-10-06):

- The decompiled tree: `reverse-engineering/apk/v1.0.955078536-10253511/` holds `base.apk`, `split_config.arm64_v8a.apk`, `split_config.xxhdpi.apk`,
  `jadx-output/` (`sources/` ≈ 64 MB), `apktool-output/` (≈ 215 MB; `smali/`, `smali_classes2/`, `smali_classes3/`, `res/`, `assets/`), `apktool-output-arm64_v8a/`
  and `pbtk-output/` (empty by root cause, ADR-041). All of `reverse-engineering/apk/` is gitignored; only `reverse-engineering/APK_VERSIONS.md` is tracked. Record the
  SHA-256 of the three APKs against `reverse-engineering/APK_VERSIONS.md` before starting (the tree must be the registered version).
- Tools on the workstation: `jadx` (`~/tools/jadx/bin/jadx`), `apktool` (`~/tools/apktool/apktool`), `uv`; system Python is **3.14** — the five tools pin
  **3.12** in their own `.venv` (`uv venv .venv --python 3.12`, each README). The `.venv` directories exist from 2026-09-16/17; re-create one if its
  interpreter is gone. Every tool's test suite reads the local tree and **skips** without it; a skip is not a pass — record pass/fail/skip counts per tool.
- The five tools and their known gaps (each `SPEC.md` §2.2/§9): `structural_index` v1.1 has `refs`, `unreferenced` and `implements` but **no
  resource/string-table search**; `schema_batch_extractor` cannot resolve a **plain singular `MESSAGE` field's class** (only oneof/list/map); `uuid_ble_context`
  has no byte-reversed-alias detection and no GATT-semantic mapping; `limited_dataflow` is single-basic-block only; `lambda_dispatcher_resolver` handles
  `packed-switch`/`if`-chains on one discriminator field. `BACKLOG.md` lists three ideas not built: wire-payload-vs-schema decoder, APK version-diff,
  tshark/DLCI-reassignment helper.
- Known stale statements to fix or propose (`TODO.md` §6, `A68-RE-06`): `scripts/decode_qhr_settings.py` is superseded by `scripts/pwrpc_decode.py` and
  describes the pw_hdlc address format wrongly (it is a one-terminated LSB varint, `PROTOCOL.md` §2.2a Correction of 2026-09-30); four stale statements in
  `reverse-engineering/tools/` backlog/spec files (find them; `0068` names them).
- What the wire already says (inputs for the code search, all re-derivable with `python3 scripts/pwrpc_decode.py <log>` over `captures/*/*btsnoop_hci*.log*`):
  the official app's connect-time `ReadSetting` sweep asks for `qhr` fields 13, 1–5, 7, 11–13, 15–19, 21–32, 34–38 and never 6, 8, 9, 10, 14, 20, 33; the
  Buds answer `UNKNOWN` for 1, 25, 34–38; services named so far: `maestro_pw.Maestro`, `Dosimeter`, `Multipoint`, `DynamicServerConfigService`,
  `pw.software_update.BundledUpdate`, `hr.core.software_update.UpdateHelperService`; unnamed on the wire of `CAP-069` (1.0.990706425): service
  `0xbf6c9399`, `JitterBuffer` method `0x8d99df93`; `GetHardwareInfo` answers `1:6 2:17 5:9 6:7 7:{serials}`; `SetWallclock 1:<ms>`; `SetConfig 1:1 2:0 3:0`;
  the runtime-info stream `2:<ms> 3:0 6:{1:{Case} 2:{Left} 3:{Right}} 7:{…}`; the settings stream carries field 13 (`qhs` ANC state); `CAP-058` showed `qhr`
  field 21 written by "Volume level notifications" (`PROTOCOL.md` §4.5.8a) and field 27 by "Other alerts"; `CAP-053` showed field 18 written by Save only.
- Governance that binds every line of this session: **ADR-017** (an AI session may search, list, extract, correlate and explain; it does not decide what
  is relevant and does not record a HYPOTHESIS/FACT by itself — every addition to `REVERSE_ENGINEERING.md` is a **proposal** until the maintainer approves
  it in chat), **`AGENTS.md` §6/§15** (no 🟢 FACT promotion, no ADR, without the maintainer), **`PROJECT_RULES.md` §8 rule 20** (short quotations with file
  and line only; whole methods or files never; nothing decompiled enters the repository — not as a fixture, not as a JSON dump, not as a table of string
  literals copied wholesale), **ADR-025** (Google Play services is not reverse-engineered — the companion app only; where a question lives in GMS, say so
  and stop), **ADR-008** (Fast Pair account linking / Find My Device network out of scope).

**Known pitfalls:** JADX "Method dump skipped" means grep the smali before recording "not found" (`A68-RE-01`); field *names* from getters are not wire
field *numbers* (`REVERSE_ENGINEERING.md` "Known limitations"); R8 merges unrelated lambdas into one class — resolve with `lambda_dispatcher_resolver`, not
by reading a `case` number as meaning (§4a of the procedure); a class present in the APK is not a class exercised by the Buds (🟡 until a capture shows it);
`PROTOCOL.md`/`REVERSE_ENGINEERING.md` keep history (dated Updates, rule 9a) — a correction is a dated Update, never a silent rewrite; line citations drift
with every edit of `REVERSE_ENGINEERING.md` — cite by entry name plus `file:line` of the **decompiled tree**, not by line of the markdown.

---

## 3. The inventory this session must build first (Phase 1)

A project-wide register of **every** 🔴 OPEN QUESTION and 🟡 HYPOTHESIS, one row each, in a CSV in the scratchpad and as a table in the RESULT:

| Column | Content |
|---|---|
| id | `Q-001` … |
| source | file + section (`PROTOCOL.md §6 Behavior`, `TODO.md §4`, `REVERSE_ENGINEERING.md <entry>`, `DESKRESEARCH_FINDINGS.md <date>`, `CAP-NNN-FINDINGS.md §N`, `ARCHITECTURE.md §15`, `DECISIONS.md ADR-NNN Update`) |
| statement | the question, quoted or tightly paraphrased |
| label | 🔴 / 🟡 as recorded |
| class | **A** the companion app's code can answer or narrow it (a schema, a field name, a write/read site, a log string, a flag, a UI condition); **B** needs a capture or a hardware test (the code can only say what to look for); **C** lives outside the app (GMS/Play services, the Buds' firmware, Android itself) — out of this session's reach (ADR-025) |
| lead | for A: the concrete search/trace to run; for B: what in the code would design the test; for C: one line why |
| status after this session | answered / narrowed / unchanged, with the pointer |

Sources to sweep, all of them: `PROTOCOL.md` §6 (every unticked item, ≈ 45), `PROTOCOL.md` §4.x "still open"/🔴/🟡 lines inside the entries, `TODO.md` §4
(every 🔴/🟡 line) and §6's RE items, `REVERSE_ENGINEERING.md` (every 🔴/🟡 and every "not found"/"not traced"/"unattributed"/"undetermined"),
`DESKRESEARCH_FINDINGS.md` (every open item), every `CAP-NNN-FINDINGS.md` open-questions section, `ARCHITECTURE.md` §15, every ADR Update that leaves
something open (e.g. ADR-034 item 4, ADR-049's 🟡, ADR-018's `3a046f6d` question). **No sampling:** the inventory is complete or the RESULT says which
sources were not swept and why. Duplicates across sources are one row with all sources listed.

---

## 4. Leads to work (seed list — Phase 1's inventory is the authority; verify each item, add what the inventory finds, drop what is class C)

### 4.1 Verification of the recorded APK findings (deliverable 1)

1. **Every `REVERSE_ENGINEERING.md` entry** — re-open the cited file at the cited lines; confirm the identifier, the log string, the schema string, the
   `RawMessageInfo` decode (`scripts/decode_rawmessageinfo.py` and `schema_batch_extractor scan --class …` for `qhr`, `qjc`/`qja`, `qjb`, `nqx`, `qjw`, `qht`,
   `qjg`, `qjo`, `qju`, `qhq`…, `qjn`/`qjt`/`qhx`/`qjv`, `qiv`, `qie`/`qid`, the Dosimeter/Multipoint/… shapes of the "decoded shapes register"), the
   call-graph arrows (`gbm`→`fzd`→`gau`→`gbd`; `fye`/`fsz`→`nqo.e`→`npy`→`npw`→`npv`→`fut.f`→`ffd.j`; `fxm.i`; `frb`/`fuh`/`glk`/`gjv`; `gaa.d`→`gdm`→`OtaFragment`
   with the smali tables `hfb`/`gyg`/`hff`), the `MaestroDeviceSettingsProviderService` case IDs, `MaestroEndpointService`, `BluetoothPriorityReceiver`, the
   HID/`fxm` UUID check, the native-library table (`find … -iname '*.so'`), the resource/string-table sweep's counts. Use `structural_index refs` /
   `implements`, `lambda_dispatcher_resolver resolve`/`resolve-all`, `limited_dataflow` where the entry's claim is a dataflow claim.
2. **Every `file:line` citation into the decompiled tree in `PROTOCOL.md`, `DECISIONS.md`, `DESKRESEARCH_FINDINGS.md`, the capture FINDINGS and
   `ARCHITECTURE.md`** (e.g. `hju.java:207–214`, `hod.java:32–39`, `fux.java:90–103`, `fut.java:178–202`, `hey.java:165–190`, `hgj.java:216–331`, `qht.java:31`,
   `hlv.java:2125–2134`, `cmi.smali:3499`, `hfb.smali :1422–1442`) — same verdicts. Build the citation checker of §4.4 item 1 for this and run it over all
   documents; the RESULT lists every citation with its verdict.
3. The "Correlation status with PROTOCOL.md" table — every row's pointer still resolves to the stated `PROTOCOL.md` section and ADR.

### 4.2 Completion of the registers (deliverable 2)

4. **`qhr`, all 38 fields:** for each field — type (from the decoded schema), write site(s), read-side `case`, the UI control or service case that reaches
   it (the two directions of ADR-019: forward from a UI fragment/log string, backward from `fxb.java`/`fyo.java`), its log strings, and every wire sample
   (`pwrpc_decode.py` over every log: first `ReadSetting` answer and every `WriteSetting`, per capture). Resolve in particular: 1, 3, 5, 6 (`fyo.m`, caller
   unfound), 8–10, 14 (write-only), 20, 23 (`qhq` marker), 24, 25, 26, 30, 31 (a 3-field message: `4:{31:{1:f32 2:f32 3:5}}` in `CAP-053` 1058 — which
   message, which feature?), 32 (`fyo.r`, Settings case 2116), 33, 34–38 (`UNKNOWN` on this firmware — are they gated by a firmware/feature flag in the
   code?). Explain from the code **why the app never reads 6, 8, 9, 10, 14, 20, 33** and in which order and from which class the sweep is issued.
5. **The pw_rpc name table:** compute `h65599` (the function in `scripts/pwrpc_decode.py`) of **every string literal in the decompiled tree** (JADX sources
   and smali `const-string`, de-duplicated) and match against **every service id and method id seen on any wire** (collect them with `pwrpc_decode.py` over
   all logs, including the unresolved ids it prints as hex). Name `0xbf6c9399` and `0x8d99df93` if their names exist as literals in this version (they may
   not — then a checked negative with the count of literals hashed); name every method of `Dosimeter`, `Multipoint`, `HeadGesture`, `EartipFitTest`,
   `JitterBuffer`, `DynamicServerConfigService`, `BundledUpdate`, `UpdateHelperService` from `fux.java`/`gnb.java`/`fwr.java` and wherever else the literals
   live. The hash→name table (names and hashes only — not the literal sweep itself) is a candidate for `scripts/pwrpc_decode.py`'s table or a tracked data
   file; propose at the checkpoint.
6. **Response and stream schemas** with their field names from the handlers' code: `SubscribeRuntimeInfo`'s packet (top-level 2 = wall clock? 3 = ?, entries
   6.1/6.2/6.3 fields 1/2, 7.x — `PROTOCOL.md` §4.3 Option F's 🔴), `GetHardwareInfo` (`qiv`: fields 1, 2, 5, 6, 7), `GetSoftwareInfo` (`qjb`/`qie`/`qid`: fields
   5 fixed64 and 6 — ADR-034's 🔴), `Dosimeter.FetchDailySummaries`/`SubscribeToLiveDb` (the daily-summary record: `1:462 2:{1:228 6:f32}`… — field names,
   units if named; display out of scope), `DynamicServerConfigService.SetConfig` (`1:1 2:0 3:0` — the three config fields), `SetWallclock`,
   `Multipoint.SubscribeToQuietModeStatus` (`1:0`), the `CLIENT_ERROR` cancellations the official app sends and when.
7. **The settings stream (`SubscribeToSettingsChanges`)**: the handler that receives `4:{13:n}` and its enum (`qhs`: 1 Off, 2 NC, 3 Transparency, 4
   Adaptive, 0 — `CAP-069` §); every other field the handler accepts.
8. **The "Candidate rich schemas"** (`nhm`, `nef`, `qaa`, `ndi`, `mtn`, `nca`, `gdw`, `nfh`, `msw`, `qaj`, `qbu`, `qar`) and the holder cluster (`kii`, `koq`, `pzr`,
   `jau`, `msc`, `jaj`, `jjn`, `jjx`, `jkl`, `jsg`, `kip`, `kiq`, `kob`, `kol`, `qan`): attribute each to a service/method or to a non-protocol library (Clearcut/KPI
   logging, Phenotype, GMS client stubs …) — the name-hash table of item 5 and `structural_index implements`/`refs` are the tools; close or narrow the 🔴.
9. **The UUID register:** rerun `uuid_ble_context extract` (6 literals expected) and add byte-reversed-alias detection (§4.4); then the checked negatives for
   the Buds' five custom 128-bit UUIDs of the Extended Inquiry Result (`CAP-033` 1072; the SDP browse of `CAP-058` names `25e97ff7-…f7b4`, `81c2e72a-…`,
   `df21fe2c-…`, `e7ab2241-…`, `f8d1fbe4-…` GSND CONTROL) and `CAP-034`'s eight GATT UUIDs — in the tree or not, both byte orders.
10. **`MaestroEndpointService`:** resolve the Dagger multibinding (`structural_index implements` on the gRPC service base type; the `@IntoSet` module) to
    name the registered services and the caller UID check (`ofd`) — `REVERSE_ENGINEERING.md`'s "open questions only" entry.

### 4.3 Leads for the open questions (deliverable 3 — seed, class A unless noted)

11. **Field 18 / EQ (`CAP-053` §3, §8):** what fires `hod.a(ebo)` — identify the navigation library behind `ebe`/`ebo`/`edd` (back-stack changed listener?),
    so the Back test can be designed; what sets `hpp.b` (the unsaved flag) and what `UserEqFragment` does with slider values 10 s after a drag (the
    thumb redraw of `CAP-053` §4 — a `crm`/LiveData observer re-applying the last read value?).
12. **Find device (`CAP-058` §4, `TODO.md` §4 Ring status):** the code that decides whether the "Find device" row/screen is offered — connection state,
    a Phenotype/feature flag (`com.google.android.gms/.phenotype.provider.ConfigurationProvider` is read by the app, `dumpsys` of `CAP-058`), an
    account condition? List the flag names (literals only) and the condition, with `file:line`. 1.0.990706425 is **not** decompiled here; say what the
    diff pass should look at.
13. **The process restart after a force stop (`CAP-058` §1, Group AT's 4th attempt):** the exported components and their binders —
    `MaestroCompanionDeviceService` (CompanionDeviceManager), `MaestroDeviceSettingsProviderService` (Settings), `MaestroSliceProvider` (system/GMS),
    `DumpContentProvider`, `BluetoothPriorityReceiver`, `ClassicBTReceiver` — from `AndroidManifest.xml` (`apktool-output/`) with `exported`/permission;
    which of them Settings' device pages touch; what `pm disable-user` would and would not stop. Output: the procedure text for the 4th attempt.
14. **DLCI 0x08 "GSND CONTROL" / 0x0a "GSND AUDIO" identity (`PROTOCOL.md` §2.3, §6; `CAP-061` Bisto lead; `CAP-069` codes `0x05` wear, `0x16` head
    gestures, `0x14` hold; `CAP-058` X2 — closed by the "Media audio" switch):** re-run the negative searches for `GSND`/`gsound`/`f8d1fbe4`/
    `e7ab2241`/the Group/Code bytes with the tools (sources, smali, `res/`, `assets/`), record the counts; then the positive side: which classes in the
    companion app read the A2DP/"media audio" profile state or the HFP state, and whether any companion-app code could own those channels (expected
    negative — then the entry says so with the commands, and the channel stays GMS/Google-app territory, class C).
15. **The announcement routing (`PROTOCOL.md` §2.2a Update 2026-10-01, 🟡 `gaa.d`)** and **the channel ↔ address derivation and "the announced channel
    names the hosting bud" (ADR-034 Update, `fux.java:90–103`, `fut.java:178`, `goq`)** — trace with `limited_dataflow`/`lambda_dispatcher_resolver` how
    the unsolicited `GetSoftwareInfo` (`call_id 0xFFFFFFFF`) is dispatched and how the app picks the channel for its own requests after an announcement
    changes (19 ↔ 21 after a Buds `DISC`).
16. **Loud Noise Protection and Adaptive Audio (`PROTOCOL.md` §4.5.9, §6 Behavior):** find their UI toggles and write sites — a `qhr` field, another
    service, or no wire at all (on-device DSP) — a class-A answer to a long-open question.
17. **Hearing wellness / Dosimeter (`WELL-001`, display out of scope):** the response record's field names and units; `HearingWellnessFragment`'s other
    writes (field 21 done; any others?).
18. **Runtime-info entry field 2 ("in the case" vs "charging", `PROTOCOL.md` §4.3 Option F 🔴):** the handler's name for it (`gaa`/`gdm` family?).
19. **The Hearable Controls MAC (`PROTOCOL.md` §4.1 🟡), the SASS flags, the Battery Notification advertisement (`CAP-054` §2):** companion app does not
    build Message Stream frames nor parse advertisements (ADR-025) — confirm with one search each (`08 12`, `0xFE2C`, "batterynotification",
    `AES/HMAC` imports) and classify C with the commands; do not go further.
20. **`fxm.i()`'s HID UUID check and `#AndroidHeadTracker#` (`SPATIAL-001`):** does the app do anything with the HID profile beyond the UUID check?
21. **`qjn`/`qjt`/`qhx`/`qjv` — `qjc`'s other oneof alternatives** (memory: possibly another Buds model's schema): which product/feature gates select them
    (`fux`-style catalogue, model-ID or capability checks) — settle whether they are Pro 2 fields or not.
22. **Every remaining class-A row of the Phase 1 inventory not listed above.**

### 4.4 Tooling (deliverable 4)

Order: run and record first, repair second, extend third, build new last — and only what §4.1–§4.3 need.

1. **Citation checker (new, small):** parses every `` `<name>.(java|smali):N[-M]` `` and `` `<Class>.java` `` citation in the markdown documents, resolves it
   under `jadx-output/sources/**` or `apktool-output*/smali*/**`, and reports exists / line range in file / an optional expected token found in the range
   (taken from the citation's own sentence where the sentence quotes a log string or identifier in backticks). Output JSON per citation; never writes to a
   document. Lives in `reverse-engineering/tools/<name>/` with `SPEC.md`, `README.md`, tests that skip without the tree. Rerunnable by every future session
   (`APK_REVERSE_ENGINEERING_PROCEDURE.md` §7 may point at it — propose).
2. **Name-hash table (new, small, or an extension of `scripts/pwrpc_decode.py`):** the `h65599` sweep of item 5 of §4.2 — input: the tree; output: a
   table of (hash, name, where found) for every literal that matches an id seen on the wire, plus the list of wire ids with no match. Only the matched
   names are ever recorded; the literal sweep itself stays in the scratchpad.
3. **`structural_index`:** the deferred resource/string-table search (`apktool-output/res/values/strings.xml`, `public.xml`, `arrays.xml`, `assets/`) with
   the key→id→usage chain, needed by §4.3 items 12, 16, 17; keep `refs`/`implements`/`unreferenced` as they are and their tests green.
4. **`schema_batch_extractor`:** close the plain-`MESSAGE`-field gap by cross-referencing the bytecode field types (`structural_index`'s field-type-holder
   query), as its `SPEC.md` §9 proposes; needed by §4.2 items 6 and 8.
5. **`uuid_ble_context`:** byte-reversed-alias detection (`SPEC.md` §2.2), needed by §4.2 item 9.
6. **`lambda_dispatcher_resolver` / `limited_dataflow`:** only what §4.3 item 15's traces need (e.g. `sparse-switch`, a second discriminator field) — not a v2.
7. **Stale statements (`A68-RE-06`):** the four in `reverse-engineering/tools/` backlog/spec files — fix them (dated, in place, since those files are
   tool documentation, not the history-keeping documents of rule 9a); `scripts/decode_qhr_settings.py` — propose at the checkpoint: delete (superseded) or
   correct its address description; do not change it before the answer.
8. **`BACKLOG.md`:** mark what this session built, what it found not worth building, and anything new it would add (the APK version-diff tool is the
   obvious candidate for the pending 1.0.990706425 pass — design notes only, do not build it against a version that is not decompiled).

Every tool change: tests green (`.venv/bin/python3 -m pytest tests/ -v`, counts recorded), `SPEC.md` updated with a dated note, the acceptance criteria
reproduced against already-confirmed findings before the tool is trusted on anything new (`BACKLOG.md` governance). No decompiled content in `tests/`
(synthetic smali lines only, as `limited_dataflow` does).

---

## 5. Tasks (phases, strictly in order)

### Phase 0 — set-up and registration

1. Create the RESULT (§0.2). Record `git log -1`, `git branch --show-current`, `git status --short`, `git fetch && git log --oneline HEAD..origin/main`.
   **Work on a new branch from `origin/main`** (e.g. `crosscheck/0073-apk-re-pass-2`). Verify the APK tree (SHA-256 of the three APKs vs
   `reverse-engineering/APK_VERSIONS.md`; `jadx --version`, `apktool --version`, the Python of each `.venv`). Run every tool's test suite as found; record pass/fail/skip per
   tool. Nothing is changed in Phase 0.

### Phase 1 — the inventory (§3)

2. Build the project-wide open-question register (CSV in the scratchpad, table in the RESULT). Classify every row A/B/C with its lead. This is the work
   list for Phases 3–4; the RESULT's Progress block tracks it row by row.

### Phase 2 — verification (§4.1)

3. Build and run the citation checker (§4.4 item 1) over all documents; then verify every `REVERSE_ENGINEERING.md` entry by hand against the tree with the
   tools (§4.1 items 1–3). Record every verdict in a table (entry/citation → confirmed / drifted / wrong / not checkable, with the evidence). A "wrong"
   verdict is a **proposed** correction (dated Update text drafted, not applied) — `AGENTS.md` §6.

### Phase 3 — tooling needed by Phases 4–5 (§4.4 items 2–6)

4. Build/extend only what the inventory's class-A leads need, in the order of §4.4; tests green after each change; SPEC/README updated. Record in the RESULT
   per tool: what changed, why (which lead), the test counts before and after, the acceptance reproduction.

### Phase 4 — registers (§4.2) and leads (§4.3)

5. Work every class-A row of the inventory and every §4.2 register, with the tools. For each: the search/trace run (command, exit status, counts), the
   `file:line` evidence with a short quotation (rule 20 (a)), the reading with its label (🟢 only for "this code exists and does X" statements that the
   quoted lines show directly; everything about what the Buds do with it is 🟡 until a capture shows it), what it changes for the question, and — for class
   B rows — the test the code suggests. A negative needs its command, exit status and a positive control (`AGENTS.md` §13 step 8: e.g. the same search
   finding `25e97ff7` before reporting `3a046f6d` absent).
6. **Cross-reference with the wire** where a code finding predicts bytes: run `scripts/pwrpc_decode.py` (and `tshark` where needed) over the existing logs
   to find or refute the predicted request/response (e.g. a newly named method's id in any capture; a field's `ReadSetting` answer; the Dosimeter record
   shape). Frames and commands in the RESULT.

### Phase 5 — checkpoint, documentation, finish

7. **Checkpoint — stop and ask, in this chat, via `AskUserQuestion`.** One question per decision; options with pros and cons, one marked "(Recommended)";
   the exact draft text of every proposed `REVERSE_ENGINEERING.md` entry/Update, every `PROTOCOL.md` note, every `DESKRESEARCH_FINDINGS.md` entry and
   every `TODO.md` change in the preview (memory "Approvals: confirm in chat" — approvals quoted only in a file do not count). Group the questions:
   (a) the corrections of Phase 2 (wrong/drifted entries); (b) the register completions (per register); (c) the leads answered/narrowed (per question or
   per theme); (d) the tooling (what to keep tracked, `decode_qhr_settings.py`, `pwrpc_decode.py`'s name table, `BACKLOG.md`); (e) anything that would need
   an ADR (draft it without a number — e.g. a new read/write the app could offer, which this session does **not** build). Record every answer verbatim in
   the RESULT.
8. Apply only what was approved, and only to documentation and tooling: `REVERSE_ENGINEERING.md` (dated Updates, rule 9a — never a silent rewrite; a
   register row may be extended in place when the Update says so), `PROTOCOL.md` (approved notes only; no status change without the maintainer's
   explicit word), `DECISIONS.md` (never, unless an ADR was approved in chat — then a process note citing this chat), `DESKRESEARCH_FINDINGS.md` (the
   cross-capture checks of Phase 4 step 6, with commands), `APK_REVERSE_ENGINEERING_PROCEDURE.md` (the citation checker in §7, if approved),
   `reverse-engineering/tools/*` and `BACKLOG.md`, `TODO.md` (§4 rows answered/narrowed; new rows), `ai-sessions/INDEX.md` (the 0073 row), `README.md` only if a
   count it states changed. Run `python3 scripts/ensure_footers.py` and `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py`; it must exit 0 — report
   anything else. (`scripts/__pycache__/lint_docs.cpython-314.pyc` is tracked; `git checkout` it if it changed.)
9. Finish the RESULT: plain-language answers first (what was verified and with what result; what the registers now hold; which open questions were
   answered, narrowed, or shown to be unanswerable from this APK; what the tools can now do), then the Phase 2 verdict table, the Phase 1 inventory with its
   after-status column, the Phase 4 evidence per lead, the tool change log, the external sources (URL + quoted sentence, if any were consulted — AOSP/
   Android developer pages, Pigweed docs, protobuf-lite source — raw fetches, not fetch-tool summaries). It must end with **"Deferred documentation"**
   (each item also added to `TODO.md`) and **"Commits"** (hashes, or "not committed — reason"), per `AI_SESSION_LOG_PROCEDURE.md` §9 and §4b. Set the Status
   per §4.
10. Show the maintainer a short summary and **ask whether to commit, push and open a pull request**. Only after a yes: on the session branch, Conventional
    Commits, one commit per concern (tooling per tool; documentation; the RESULT), each with a *why* and ending with the attribution line from the
    session's system reminder; **nothing under `android/` or `dist/` in any commit**; nothing from `reverse-engineering/apk/`, a tool's `.venv/`,
    `.pytest_cache/`, `__pycache__/`, or any JSON/CSV dump that contains decompiled text; `git diff --cached --stat` checked for decompiled content before
    every commit.

---

## 6. Guardrails (binding, in addition to `AGENTS.md`)

- **ADR-017 boundary, every step:** search, list, extract, correlate, explain. Never decide relevance; never record a finding as settled. Every new
  statement for `REVERSE_ENGINEERING.md`, `PROTOCOL.md` or `DESKRESEARCH_FINDINGS.md` is a proposal with its draft text, approved in this chat before it is
  written. Model agreement (a sub-agent, a second model) is not approval.
- **No 🟢 FACT promotion, no ADR** without the maintainer (`AGENTS.md` §6/§15). A code reading that is certain is 🟢 **for the code** ("this method writes
  field 6"); what the Buds do with it stays 🟡 until a capture — say which label applies to which half.
- **No decompiled content in the repository** (`PROJECT_RULES.md` §8 rule 20; `BACKLOG.md` governance; every tool `SPEC.md` §11): quotations of a few lines
  with `file:line` in the documents are allowed; whole methods, string tables, JSON dumps, fixtures copied from the tree are not. The tools read the local
  tree and skip without it. Check `.gitignore` before `git add`.
- **Scope:** the companion app `v1.0.955078536-10253511` only. Not Play services (ADR-025), not Find My Device / account linking (ADR-008), not the
  Buds' firmware, not `1.0.990706425` (pulled, not decompiled; `TODO.md` §4 — a diff pass is a later session; this session may only write the design
  notes for it). No change under `android/` or `dist/`; no build; no capture skeleton; no new `CAP-NNN`/Test-ID/ADR number in `id_registry.csv` unless
  approved in chat.
- **Evidence:** every claim carries `file:line` of the decompiled tree (plus the quoted token) or a frame number; every decode carries its command and raw
  bytes (`PROJECT_RULES.md` rule 4a); a negative carries its command, exit status and positive control (`AGENTS.md` §13 step 8); zero creativity with hex and
  with obfuscated names (`AGENTS.md` §13.6) — an unnamed class stays unnamed, with a reading-alias clearly marked as the session's own label.
- **No sampling.** Every `REVERSE_ENGINEERING.md` entry verified; every citation checked; every open question inventoried and classified; every class-A
  lead worked or explicitly deferred with the reason. Where the tree is too large for a step (the literal sweep is ≈ 64 MB of sources), the step runs as
  a script, not as a read — and the script's counts are the evidence.
- **Non-destructive documents:** `REVERSE_ENGINEERING.md`, `PROTOCOL.md`, `DECISIONS.md` keep history (rule 9a) — corrections are dated Updates; tool
  SPEC/README/BACKLOG are maintained in place with a dated note.
- **Subagents** may only read and report; every number a subagent reports is re-derived in the main session before it enters a file.
- **Resumability** (§0.2) is kept at every step; **commits only after the maintainer confirms** (task 10).

---

## 7. The maintainer's original request (Dutch, verbatim, chat 2026-10-06)

> Maak een prompt in het engels, in ai-sessions/, volgens de regels van het project, die in een nieuwe sessie kan draaien, die: de apk in
> reverse-engineering/apk/v1.0.955078536-10253511/ nogmaals intensief reverse engineert, om bevindingen te controleren, aan te vullen en verdere
> aanknopingspunten te vinden voor alle openstaande vragen van het gehele project. Maak gebruik van de scripts in reverse-engineering/tools/ en verbeter
> en/of bereid deze scripts verder uit indien nodig.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0073_CROSSCHECK_PROMPT_2026_10_06.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0073_CROSSCHECK_PROMPT_2026_10_06
