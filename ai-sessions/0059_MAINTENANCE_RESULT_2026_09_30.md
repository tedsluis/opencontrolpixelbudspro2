# 0059_MAINTENANCE_RESULT_2026_09_30.md — Process every finding and lead of the 0058 audit into the project files and the app, and prepare CAP-065

**Number:** 0059
**Category:** MAINTENANCE
**Date:** 2026-09-30
**Title:** Validate and process all 54 findings and the 5 protocol leads of `ai-sessions/0058_AUDIT_RESULT_2026_09_30.md` — documentation, protocol, decisions, captures, test plans, session records and the Android app — within the maintainer's decisions, and write the `CAP-065` capture skeleton
**Status:** complete

---

## Progress

- **Session start:** 2026-09-30. `git log -1` = `6f8c59c docs: prompt 0058 — exhaustive end-to-end audit of the whole project`. `git status --short` at start:
  ` M ai-sessions/INDEX.md`, ` M id_registry.csv`, `?? ai-sessions/0058_AUDIT_RESULT_2026_09_30.md`, `?? ai-sessions/0059_MAINTENANCE_PROMPT_2026_09_30.md`,
  `?? android/.kotlin/`. `git fetch && git log --oneline HEAD..origin/main` → empty (nothing new on the remote).
- **Mandatory reading (done):** read in full — `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md` (878 lines), `PROTOCOL.md` (3,279), `DECISIONS.md`
  (2,185, ADR-001 … ADR-047 with every Update), `TODO.md` (900), `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/0058_AUDIT_RESULT_2026_09_30.md` (908). Read in part:
  `ai-sessions/INDEX.md` (rows 0001–0011 and 0030–0059), `ai-sessions/0058_AUDIT_PROMPT_2026_09_30.md` (§0–§2). Other large files: per task, see the reading ledger below.
- **Phase 0:** DONE. 0058 RESULT committed and pushed on its own (`38d06a6`, maintainer's answer). Baseline gate `./gradlew --offline --max-workers=2
  assembleDebug testDebugUnitTest test lint` → BUILD SUCCESSFUL; JUnit XML: `:data` 1567, `:hardware` 51, `:ui` 9 (per variant), `:domain` 25, 0 failures;
  lint: `:app` 1 warning (`DataExtractionRules`); Kotlin warnings after `--rerun-tasks` compile: 3 (`BudsCompanionPairing.kt:263`, `:293`, `LinkEvaluation.kt:84`)
  — identical to 0058's baseline.
- **Phase 1:** DONE — every item re-derived (ledger below); corrections of 0058 itself: TESTPLAN's icon column is the existence source (HK-01/HK-04),
  CAP-019 `07 34` ×15 not ×20, CAP-027 count wording, L-2 settled from existing bytes, L-5 wider.
- **Phase 2:** DONE — answers below.
- **Phase 3:** DONE (docs only, no code): `DECISIONS.md` (ADR-048, ADR-049 new; Status of ADR-001/024; dated Updates on ADR-001, 009 (process note), 012, 013,
  018, 019, 020, 024, 025, 029, 030, 031, 034, 040, 044, 045), `id_registry.csv` (ADR-048/049 registered, ADR-001/024 rows), `AGENTS.md` (§5 note and list,
  §8, §11, §13 step 8), `PROJECT_RULES.md` rule 9a scope, `REVERSE_ENGINEERING.md` (convention paragraph, LEB128 correction, `gbm` refutation),
  `PROTOCOL.md` (§2.2a/§2.3/§3/§4.1/§4.2/§4.3/§6/§7/§8 dated Updates and pointers), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 step 4 and §6 rule. The LFS
  migration of the five `.log.last` files is done as its own `chore` commit in Phase 8 (staging it now would mix with the doc commits). One count in the
  approved ADR-049 preview was corrected on re-derivation: 41/41 → **40/40** ACKed (CAP-002's four Sets are CAP-001's frames).
- **Phase 4:** DONE (docs only). CAP-01 (CAP-002 false FACT rewritten), CAP-02 (CAP-029/044/047/023/019/001/004 negatives rewritten with commands and
  positive controls), CAP-03 (every bullet corrected in place), CAP-04 (`scripts/stale_capture_status.py` written; its 76-line output reviewed (scratchpad,
  not committed — rerun `python3 scripts/stale_capture_status.py`); status banners on 33 FINDINGS files), CAP-05 (three concrete rule-9a cases folded: CAP-010, CAP-015, CAP-021; the remaining dated addenda are now
  read under each file's banner — a full fold is deferred, `TODO.md`), CAP-06 (dangling refs fixed with `scratchpad/secref.py` = 0 unresolved; missing scratch
  files replaced by their commands; duplicate footers removed), CAP-07 (TESTPLAN Group back-links AU…BA, CAP-062/063 evidence, FIND-001/002 and CASE-001 tags),
  CAP-08 (Groups AU/AV/AZ/BA sections, §9 rows CAP-064/065, CAP-052 withdrawn, CAP-054 premise, registry), GOV-07 groups 1–4 resolved, CAP-064 split,
  `CAP-065` skeleton written (`captures/CAP-065-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BA/CAP-065-EVENT-NOTES.md`).
- **Phase 5:** DONE. APP-01…08 (ledger). Gate `./gradlew --offline --max-workers=2 assembleDebug testDebugUnitTest test lint --rerun-tasks`: exit 0;
  per variant `:data` 1570 (+3), `:hardware` 52 (+1), `:ui` 11 (+2); `:domain` 27 (+2) — JUnit XML; lint 1 warning (`:app` `DataExtractionRules`, carried — needs a manifest
  change); Kotlin warnings 0 (was 3). Mutations M1–M6 caught, each restored byte-identical (`sha256sum -c`). `transport.send(` call sites in
  `BudsRepositoryImpl.kt`: 8, unchanged. No gradle/manifest/version-catalog diff; no new permission or dependency; nothing on DLCI 0x08; every new write path
  passes `writeGate`. AGPL header block of the 4 new Kotlin files identical to `MainActivity.kt`'s (`cmp`).
- **Phase 6:** DONE — documentation sweep (ledger "Files changed"); `ARCHITECTURE.md`, `README.md`, `TODO.md`, `WORKSTATION_PREPARATIONS.md`, `SECURITY.md`,
  `CHANGELOG.md`, `APP_TESTPLAN.md` §L/§P, `AI_SESSION_LOG_PROCEDURE.md` §9, workflows, `ensure_footers.py`, session records, `INDEX.md`.
- **Phase 7:** DONE — re-grep of every corrected statement ("LEB128" as address encoding: 4 more places corrected; "dock state" in current texts and code
  comments: corrected; "MVVM"/`BudsUiState` in binding texts: only history and ADR-048's own text remain); `ensure_footers.py` and `lint_docs.py` exit 0
  (the first `lint_docs` run failed on five bare file names in this RESULT — fixed). One self-inflicted ledger breakage (escaped `\|` split) repaired from the
  session transcript.
- **Phase 8:** DONE — final gate green (below); commit/push approved in chat; commits below.
- **Scratchpad:** `/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/e5039337-3e7c-415a-9825-62b1f6a12d25/scratchpad`

## Per-item ledger

Verdicts: processed / narrowed / rejected / not reproduced / awaiting maintainer. Filled per phase.

| Item | Verdict | Evidence (re-derived in this session) | Files changed | Phase |
|---|---|---|---|---|
| A58-GOV-01 | processed (ADR-048, maintainer) | `grep -rn "BudsUiState\|ViewModel\|UseCase" android --include=*.kt` → only `MainActivity.kt:98`, `AncScreen.kt:56` ("no ViewModel") | `DECISIONS.md` (ADR-048, ADR-001 Status/Update), `AGENTS.md` §8/§11/§13 step 2, `ARCHITECTURE.md` §1/§2/§7, `README.md`, `id_registry.csv`; code: `AppUiSession.kt` | 3, 5, 6 |
| A58-GOV-02 | processed (maintainer) | `javap -constants -cp ~/Android/Sdk/platforms/android-34/android.jar android.bluetooth.BluetoothDevice \| `AGENTS.md` §5 note, `PROTOCOL.md` §4.3 Option 0 (🟢 not public API) + L847 pointer, `DECISIONS.md` ADR-029 Update, `ARCHITECTURE.md` §4/§15 |  | 3 |
| A58-GOV-03 | processed — current files only (maintainer: no history rewrite, no force-push) | `comm -23 <(git ls-files captures \| grep -v .md$ \| sort) <(git lfs ls-files -n \| sort)` → the five `.log.last` of CAP-040/042/047/048/049 | the five `.log.last` files re-added through LFS in their own `chore` commit (Phase 8) | 3 |
| A58-GOV-04 | processed (maintainer: "Not recorded") | `awk '/^## ADR-009/,/^## ADR-010/' DECISIONS.md \| grep -ci "maintainer\|approved\|sign-off"` → 1, and that 1 is the ADR-010 heading line: ADR-009 itself names no approval | `DECISIONS.md` ADR-009 process note | 3 |
| A58-GOV-05 | processed | `CAPTURE_BLUETOOTH_HCI_SNOOP.md` L1680–1681 "mark the entry `[VERIFIED-LOCAL]` … and raise its confidence to 🟢" | `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 step 4 | 3 |
| A58-GOV-06 | processed (maintainer: "Reword the claim") | `REVERSE_ENGINEERING.md` L25–32 rewrite-in-place claim; `grep -c "Update (20\|\*\*Update\|update (20"` = 64 | `REVERSE_ENGINEERING.md` convention paragraph, `PROJECT_RULES.md` rule 9a scope | 3 |
| A58-GOV-07 | processed (groups 1–4, maintainer); narrowed | scratchpad list of `PROPOSAL` markers (`grep -rn PROPOSAL --include=*.md`, not committed): 65 hits outside `ai-sessions/` (0058: 64); 5 are not proposals (the status vocabulary in `AI_SESSION_LOG_PROCEDURE.md` ×4, `DECISIONS.md` L697 history) — grouped at the checkpoint | groups 1–3 markers resolved in place; group 4: status lines in 19 capture FINDINGS; the remaining hits are history (changelog rows, capture proposals now registered as planned captures) — open items in `TODO.md` | 3 |
| A58-GOV-08 | narrowed | all four merged manifests (debug/release × two dirs) list `…DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` besides the four documented ones; no INTERNET | `README.md` permission paragraph (protectionLevel `signature` checked in the merged manifest); the manifest comment needs a manifest change → `TODO.md` | 6 |
| A58-GOV-09 | processed | `scripts/ensure_footers.py:8` has `#/`; tag-pinned actions in `.github/workflows/lint-docs.yml` **and** `.github/workflows/update-docs-sidebar.yml`, `.github/workflows/update-sitemap.yml` (0058 named only lint-docs); `.github/workflows/android.yml:53` `… \| head -1` | `scripts/ensure_footers.py` docstring; `.github/workflows/lint-docs.yml`, `.github/workflows/update-sitemap.yml`, `.github/workflows/update-docs-sidebar.yml` pinned to SHAs looked up with `gh api` (checkout v5.1.0, setup-python v6.3.0); `.github/workflows/android.yml` loops over every merged manifest (dry run on 4 manifests + positive control) | 6 |
| A58-PROT-01 | processed (ADR-049, maintainer) | ADR-024 Decision vs its 2026-09-25 Update read; `CAP-062` 5998 = `ff020003020812` (06:45:20.529, DLCI 0x04, Rcvd) | `DECISIONS.md` ADR-049 + ADR-024 Status; `PROTOCOL.md` §4.1; `AncFrame.kt`, `BudsError.kt`, `BudsRepositoryImpl.kt` comments; `AncCodecTest.kt` name; `TESTPLAN` OBS-006 | 3 |
| A58-PROT-02 | processed | md5 of `-T fields -e frame.time_epoch -e frame.len` over all 2,663 CAP-001 frames = md5 of CAP-002's first 2,663 = `1c68774f38316ad9c275a975c7fcbc16`; CAP-002 own window: `pwrpc_decode.py` 49136 `6:{1:{1:57 …}}` | `CAP-002-FINDINGS.md`, `CAP-002-EVENT-NOTES.md` (+47984), `DECISIONS.md` ADR-018 Update, ADR-049 count 40 (not 41) | 3 |
| A58-PROT-03 | processed | `fut.java:178–202`: groups written `i3 + i3`, last byte `\| 1` (one-terminated); "LEB128" at `PROTOCOL.md` L229/L265/L448, `DECISIONS.md` L775, `REVERSE_ENGINEERING.md` L784/L787/L815/L4276 (`Varint.kt:22` and RE L685 are protobuf varints — correct, untouched) | `PROTOCOL.md` §2.2a/§2.3, `DECISIONS.md` ADR-018, `REVERSE_ENGINEERING.md` (nqx, call graph), `CAP-001`/`CAP-005`/`CAP-015` FINDINGS, `DESKRESEARCH_FINDINGS.md` | 3 |
| A58-PROT-04 | processed | `PROTOCOL.md` L1280 "B and E are implemented" vs L804–809 / ADR-043 | `PROTOCOL.md` §4.3 ("B and F implemented", "seven options", "A–D") | 3 |
| A58-PROT-05 | processed | each quoted line read in this session's full read of `PROTOCOL.md` | `PROTOCOL.md` §4.2 pointers, §4.5.2 SASS, §6 answered items ticked | 3 |
| A58-PROT-06 | processed | §7 last row vs `ARCHITECTURE.md` §8 L643–645 | `PROTOCOL.md` §7 (two rows) | 3 |
| A58-PROT-07 | processed | `tshark -r CAP-033… -Y frame.number==1279 -V`: "GSND CONTROL" record = UUID `f8d1fbe4…`, RFCOMM channel 4; `grep f8d1fbe4 REVERSE_ENGINEERING.md` → none; `fut.java:131` "Unsupported legacy communication style." | `REVERSE_ENGINEERING.md` gbm entry (refuted: CAP-033 1279), `DECISIONS.md` ADR-025 Update | 3 |
| A58-PROT-08 | processed | lines read (§4.3 intro, §3 table, Option B, §0.1 row, §2.2a L343–345) | `PROTOCOL.md` §6 (CAP-018 count, 0x0c0c 40 B, CAP-036 bytes, CONV-002), §2.2a "1779298694" relabel | 3 |
| A58-CAP-01 | processed | `tshark -r CAP-002… -Y "frame.number==17233\|\|frame.number==17610"` → 17233 Connect Complete handle 0x0001 BD_ADDR = the Buds; 17610 DLCI 0x05 `030a0008… 03010003da2db1 … 0309000a5265766973696f6e2036` | `CAP-002-FINDINGS.md` (DLCI 0x03/0x05 = an earlier Buds session, 17233/17610) | 4 |
| A58-CAP-02 | processed (6/6 rewritten with commands and positive controls); one count narrowed | CAP-029 `avctp or avrcp` → tshark error, exit 4; `btavctp or btavrcp` → 26; 1593 Sent `PlaybackStatusChanged` Paused; `btavrcp.opcode==0x7c` → 0. CAP-044 4795 `Sent DISC Channel=2`. CAP-047 `0e:01` on DLCI 0x08: log 1 = 11 frames, log 2 = 5. CAP-023 730 "Revision 6". CAP-019: message-level parse (scratchpad `ms.py`) → `07 34` ×**15** (0058: 20 — not reproduced; the claim's substance holds), 0x11/0x40/0x41/0x42 at 786–807 and 2487–2496; 2326 = ANC Notify. CAP-001 `contains e8:e8` → 19 in CAP-001, 26 in CAP-002. CAP-004 has 2,921 frames; 49251 is CAP-002's | `CAP-029`, `CAP-044`, `CAP-047`, `CAP-023`, `CAP-019`, `CAP-001`, `CAP-004` FINDINGS (and EN where they repeat the claim) | 4 |
| A58-CAP-03 | processed; CAP-027 narrowed | each frame re-run (commands in §Phase 1); CAP-027: 3 frames on DLCI 0x08 (845/862/875) + the DLCI 0x02 announcement 904 carrying the string 3× — "6 occurrences" is right as a string count, wrong as "on DLCI 0x08" | FINDINGS of CAP-005/006/007/008/013/014/016/018/020/027/028/032/036/039/040/045/056/059/063 | 4 |
| A58-CAP-04 | processed | see Phase 4 banner-script output | `scripts/stale_capture_status.py` (new); "Status as of 2026-09-30" banners on 33 FINDINGS | 4 |
| A58-CAP-05 | processed in part; full fold deferred |  | folded: `CAP-010`, `CAP-015` §7, `CAP-021` §3; the rest under the banners → `TODO.md` | 4 |
| A58-CAP-06 | processed | `git ls-files` / `find` for the four TSV/merged-log names → none; duplicate footers: `grep -c "^https://github.com/tedsluis"` = 2 in CAP-059/060 EN+FINDINGS and the 0057 RESULT | section refs in CAP-014/016/018/028/030/009/011/060; scratch TSVs → commands (CAP-034/036/040); duplicate footers (CAP-059/060, 0057 RESULT) | 4 |
| A58-CAP-07 | processed | `grep -c CAP-063 TESTPLAN…` = 0; TESTPLAN L151–152 FIND-001 = Left; CAP-062 EN tags read | `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` Group back-links (AO…BA), CAP-062/063 evidence | 4 |
| A58-CAP-08 | processed (maintainer: "Withdraw + correct") | no "Group AZ"/`CAP-064` in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`; `tshark -r CAP-043… -Y "btrfcomm or btsdp" \| wc -l` = 0 (of 1,572 frames) | `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Groups AU/AV/AZ/BA, §9 rows CAP-064/065, CAP-052 withdrawn, CAP-054 premise), `id_registry.csv`, `CAP-052`/`CAP-054` EN | 4 |
| A58-CAP-09 | processed — open samples recorded; test planned | `CAP-042` 602 `0813000401e8e808`, `CAP-045` 612 `…e8e840`, `CAP-048` 11939 `…e8e880`; notes: CAP-042 "both earbuds resting loose beside the open, empty case", CAP-045 "Left bud is in the case, right bud is out (loose)", CAP-048 EN L86 "both buds loose" | ADR-049 (3 open samples: CAP-042 602, CAP-045 612, CAP-048 11939); `CAP-065` section II (BA-5…8) | 3/4 |
| A58-ARCH-01 | processed | same grep as GOV-01; ARCHITECTURE L84–85, L99, L187, L609–610 read | `ARCHITECTURE.md` §1 diagram, §2 heading/table/UI note/dependency sentence, §7 | 3/6 |
| A58-ARCH-02 | processed | `grep -rn "WakeLock\|PowerManager" android --include=*.kt` → none | `ARCHITECTURE.md` §6.1 ("As built: no wakelock"; §1 → §6.0a) | 6 |
| A58-ARCH-03 | processed | `BudsError.kt` 76/83/89/95/101 not in ARCHITECTURE §7 | `ARCHITECTURE.md` §7 `BudsError` block (+5 members, + `UnreadableAnswer`) | 6 |
| A58-ARCH-04 | processed | ARCHITECTURE L391 heading, L403/L404 cells | `ARCHITECTURE.md` §5a heading (through ADR-049), Find row (CAP-062), EQ row (CAP-063), ANC row (ADR-049) | 6 |
| A58-ARCH-05 | processed | L170, L441, L57, L641; `find android -name "*.proto" -not -path "*/build/*"` → none | `ARCHITECTURE.md` L170 (disabled-until-read incl. EQ sliders; `HoldRow` exception named), L441, L57, §8 `.proto` | 6 |
| A58-DEC-01 | processed | ADR-031 Update L1639, ADR-040 L1991 | `DECISIONS.md` ADR-031, ADR-040 Updates | 3 |
| A58-DEC-02 | processed | ADR-044 L2091 "Not yet implemented." | `DECISIONS.md` ADR-044 Update; `TODO.md` ADR-044 debt line | 3 |
| A58-DEC-03 | processed | `ls captures`: CAP-015 = 2026-08-18 Group T (EQ) | `DECISIONS.md` ADR-030 Update (CAP-014, not CAP-015) | 3 |
| A58-DEC-04 | processed | `grep -rn minSdk android --include=*.kts` → data/app/ui/hardware | `DECISIONS.md` ADR-012/013/019/020/034 Updates | 3 |
| A58-HK-01 | processed; narrowed (CASE-007 status is the maintainer's) | WORKSTATION L321–322; `grep -il factory` on CAP-001/002 → none; CAP-029 §3. **Correction of 0058:** TESTPLAN's 🟢🔵🟡🔴 column is the *existence source* (TESTPLAN L63/L69: "🔵 Official — confirmed via support.google.com"), not a test status — `CASE-007` 🔵 is correct and needs no change; only its evidence cell | `WORKSTATION_PREPARATIONS.md` (CAP-029 §3); `TESTPLAN` CASE-007 evidence | 4 |
| A58-HK-02 | processed | README L61, L63, L67, L71, L74, L115, L133 | `README.md` "Current state (2026-09-30)" (counts from `id_registry.csv`), re-test pointer, Approach 4 | 6 |
| A58-HK-03 | processed | TODO L59–76, L634–635, L703, L876 read | `TODO.md` queue (CAP-054/056/057), L634, L703, the test-setup line, ADR-044 line; new "Open after 0059" block | 6 |
| A58-HK-04 | processed; narrowed (icon column = existence source, not test status) | rows printed; same existence-column correction as HK-01: CONV-002's 🟢 is "screenshot", its "clean negative" is Notes text (fixable mechanically); `CALL-001` evidence = `CAP-008-FINDINGS.md` §4/§5 | `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` AUDIO-001, CALL-001, CASE-007, CONV-002, PAIR-004, BATT-003/004, OBS-004, EQP-001, FW-003 | 4 |
| A58-HK-05 | processed | `SECURITY.md` L18–20; `writeGate(` at `BudsRepositoryImpl.kt` 704 (ANC, true), 749 (EQ, false), 911 (settings, false), 957 (Ring, true) | `SECURITY.md` (every write; Model-ID rule per channel — matches the 4 `writeGate` call sites) | 6 |
| A58-HK-06 | processed | CHANGELOG order, APP_TESTPLAN L1, registry CAP-056 | `CHANGELOG.md` (0056 before 0057; 0058/0059 entries; intro), `APP_TESTPLAN.md` L1, `id_registry.csv` CAP-056 | 6 |
| A58-SES-01 | processed (template for future prompts; history not rewritten) | `grep -qw` of the four files in 0034–0038 PROMPTs → none / AGENTS.md only | `AI_SESSION_LOG_PROCEDURE.md` §9 (approved text) | 6 |
| A58-SES-02 | processed (maintainer) | `grep -l "Status:\*\* prompt only"` → 14 PROMPTs: 0031, 0033–0040, 0043, 0044, 0045, 0049, 0058; 0033 RESULT L7 "awaiting maintainer sign-off" | `0033` RESULT Status → complete (ADR-036/045/046/047, §4a); Status line dropped from 14 PROMPTs; `INDEX.md` rows 0033/0058/0059 | 6 |
| A58-SES-03 | processed (template); this session's own reads are disclosed in §Reading | `wc -c` | `AI_SESSION_LOG_PROCEDURE.md` §9 items 2–3 | 6 |
| A58-SES-04 | processed |  | deferred items now in `TODO.md` ("Open after 0059"); Group AZ/BA added to the capture plan; §9 item 6 makes the list mandatory | 4/6 |
| A58-SES-05 | processed | 0054/0056 L16, 0057 L56 "waiting only for the maintainer's commit/push decision"; `git log` shows their commits | annotations in the 0054/0055/0056/0057 RESULTs (commits checked on `origin/main`); 0057 duplicate footer removed | 6 |
| A58-APP-01 | processed | `AncTileService.kt:90` combine without `onStart`; `BudsRepositoryImpl.kt:214` `MutableSharedFlow<AncMode>(replay = 1)` no initial value; kotlinx `combine` reference (Sources). Fix: `domain/AncTile.kt` `ancTileStates` with `ancMode.onStart { emit(null) }`; test `AncTileTest` (2); mutation M3 caught | `domain/…/AncTile.kt` (new), `domain/…/AncTileTest.kt` (new), `app/…/AncTileService.kt` | 5 |
| A58-APP-02 | processed (5e: "Disable until read") | `EqScreen.kt:94–112`; `ControlsScreen.kt:123`. Fix: `slidersEnabled = enabled && gains != null`, presets stay; `EqScreenTest` (2, Robolectric); mutation M5 caught | `ui/…/EqScreen.kt`, `ui/…/EqScreenTest.kt` (new) | 2, 5 |
| A58-APP-03 | processed | `CodecRouter.kt:257–277`; `BudsRepositoryImpl` `readSettings`/`readEq` accepted only a non-OK result → 2 s `Timeout`. Fix: an OK result without a decodable value → new `BudsError.UnreadableAnswer` at once; test with CAP-036 frame 1462 (supplementary, re-encoded field); mutation M2 caught | `domain/…/BudsError.kt`, `data/…/BudsRepositoryImpl.kt`, `ui/…/ConnectionScreen.kt`, `BudsRepositoryImplTest.kt` | 5 |
| A58-APP-04 | processed (🟡 race, mitigated) | pw_rpc answers carry no call id (13,582 `call_id=None`, 175 `4294967295`; 56 logs). Fix: a `WriteSetting` timeout starts a quarantine of `EQ_WRITE_ACK_TIMEOUT_MS`; every Maestro request waits it out; a late answer inside it is logged and dropped; test (ACK_CH21_1731 late); mutation M4 caught. Nothing is retried or queued | `data/…/BudsRepositoryImpl.kt`, `BudsRepositoryImplTest.kt` | 5 |
| A58-APP-05 | processed | `transition()` is a `compareAndSet` loop; `onDisconnected`/`onError` through it; test "a loss racing onReady always ends Disconnected" (3000 rounds); mutation M1 caught (twice) | `hardware/…/ConnectionStateMachine.kt`, `ConnectionStateMachineTest.kt` | 5 |
| A58-APP-06 | not reproduced; diagnostics added | Probe (temporary `@AfterEach`, restored byte-identical): 0 reader threads left after any of the 20 tests, ≤ 5 dispatcher workers — IO starvation refuted; positive control: the same probe sees 1 blocked reader before `disconnect()`. 5 runs × Debug+Release × 20 tests = 200 executions under 16 busy loops on 8 cores: 0 failures, ≤ 3.4 s per class. Virtual time does not fit (readers block in `read()` on real threads); `blocking {}` now prints every thread's stack on its 10 s timeout (positive control: a temporary 11 s test printed 212 stack lines; removed) | `hardware/…/RfcommBudsTransportTest.kt` | 5 |
| A58-APP-07 | processed (4 TODO(verify) stay open) | Rewritten: `AncFrame.kt` (ADR-049), `Hdlc.kt` (§5a per setting), `ConnectionScreen.kt` 0x08 label, `OpenControlNavHost.kt` (`CreateDocument`), `Maestro.kt` (derivable 🟡, table kept), `AncTileService.kt`/`BudsForegroundService.kt`/`BudsRepositoryImpl.kt` settled TODO(verify)s; L-5 "serial" → version number in `SoftwareInfo.kt`, `DeviceInfo.kt` and 4 test files (comments only). Still open, kept: `BudsCompanionPairing.kt:50/111` (CDM on hardware ⚪), `EqFrame.kt:39`, `CaseBatteryFrame.kt:53`, `SoftwareInfo.kt:29`, `OsConnectionObserver.kt:67`, `OpenControlNavHost.kt:208` | see Verified-by list | 5 |
| A58-APP-08 | processed; 1 narrowed | Two clocks → `clock()` everywhere (default `System::currentTimeMillis`, production unchanged); `setEqGains` during a re-open → `SessionOpening` on `eqError` (test; mutation M6 caught); bonding wait + Debug frames → `AppUiSession` (ADR-048 item 2, application scope); `BluetoothStateObserver.current()` as the initial value; the 3 Kotlin warnings → explicit `@OptIn` (0 warnings). **Narrowed:** the Activity-context `BudsCompanionPairing` stays for the system picker and the bonded lookup; only the bond wait moved. **Not done:** lint `DataExtractionRules` needs a manifest change (prompt: no manifest diff) | `app/…/AppUiSession.kt` (new), `MainActivity.kt`, `hardware/…/BluetoothStateObserver.kt`, `BudsCompanionPairing.kt`, `LinkEvaluation.kt`, `data/…/BudsRepositoryImpl.kt`, `BudsRepositoryImplTest.kt` | 5 |
| L-1 | processed — 🟡 recorded (maintainer), test planned | `fux.java:90–103` channels 18–27; `goq.java` LEFT_BT_CORE 3 / RIGHT_BT_CORE 4 / MAESTRO_A 10 / MAESTRO_B 13; `fut.java:178` formula → (10,3) `003b`, (10,4) `004b`, (13,3) `803d`, (13,4) `804d` = the four observed request addresses | `PROTOCOL.md` §2.2a Update; `Maestro.kt` comment; `CAP-065` section III (BA-9…11) | 3, 4 (CAP-065) |
| L-2 | processed — 🟢 (maintainer: "Promote to FACT") | `CAP-033` frame 1355 SDP HID record "Android Gamepad", attribute 0x0206 Report descriptor (170 bytes) parsed: Usage Page 0x20, Usage 0xE1, Feature 0x308 (23 B) and 0x302 (16 B), Input 0x544/0x545/0x546 — the AOSP head-tracker HID protocol exactly (Sources). No capture needed | `PROTOCOL.md` §6 (HID head tracker, descriptor bytes CAP-033 1355) | 3 |
| L-3 | processed — counts recorded (🟡 meaning) | `CLIENT_ERROR` present (271 in my 56-log set; 0058: 274 in 55), `SERVER_ERROR` 0 in both; per-call split matches in kind | `PROTOCOL.md` §2.2a Update (271 CLIENT_ERROR in 56 logs; SERVER_ERROR 0) | 3 |
| L-4 | processed — 🟢 names (maintainer) | `scripts/pwrpc_decode.py:h65599` on the names → `0x73d5d805` Dosimeter, `0x4e4abee7` Multipoint, `0x1c256c5d` DynamicServerConfigService, `0xaf3a7737` BundledUpdate, `0x755ffe65` UpdateHelperService (+ methods); literals in `fux.java`, `gnb.java`, `fwr.java`, … | `PROTOCOL.md` §2.2a Update; Dosimeter display out of scope (maintainer) → `TODO.md` note | 3 |
| L-5 | processed — 🟢 relabel (maintainer) | `CAP-041` 782 `UpdateHelperService.GetRunningVersion` → `1:1779298694`; the same digits are field 1 of each entry of the `GetSoftwareInfo` announcement (`0x31373739323938363934`, CAP-001 1346, CAP-041 758) — so the "serial" in the announcement is this version number; also named "serial" in `SoftwareInfo.kt:26`, `DeviceInfo.kt:48`, test comments, `CAP-032-FINDINGS.md` L203, `DESKRESEARCH_FINDINGS.md` L129 | `PROTOCOL.md` §2.2a; `SoftwareInfo.kt`, `DeviceInfo.kt` and 4 test-file comments | 3 |

## The 0058 action list (rows 1–12)

| # | Verdict | Where |
|---|---|---|
| 1 | processed | ADR-048 (maintainer); `AGENTS.md` §8/§11/§13 step 2; `ARCHITECTURE.md` §1/§2/§7; `README.md`; `AppUiSession` |
| 2 | processed | `AGENTS.md` §5 note; `PROTOCOL.md` §4.3 Option 0 🟢 not public API; ADR-029 Update |
| 3 | processed | ADR-049 (supersedes ADR-024's Decision); the three `e8` samples are in ADR-049 and in `CAP-065` section II (the maintainer chose `CAP-065` over `CAP-064` AY-3) |
| 4 | processed | `CAP-002` false 🟢 rewritten; CAP-029/044/047/023/019 negatives rewritten with commands and positive controls; `CONV-002` re-derived; the rule in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §6 and `AGENTS.md` §13 step 8 |
| 5 | processed | APP-01/03/04/05 built with tests and mutations; APP-02 "Disable until read" |
| 6 | processed; full rule-9a fold deferred (`TODO.md`) | banners (script), corrections, 3 folds, refs, `ARCHITECTURE.md`, ADR Updates, `README.md`, `TODO.md`, `WORKSTATION_PREPARATIONS.md`, `SECURITY.md`, code comments |
| 7 | processed | `TESTPLAN` back-links (CAP-056/062/063, Groups AO…BA); FIND-001/002 evidence; CAP-002 tag `CASE-003`; Groups AU/AV/AZ/BA in the capture plan |
| 8 | processed | L-4/L-5 🟢, L-2 🟢, L-1 🟡 + `CAP-065` III, Dosimeter "not now" (all maintainer); PROT-07 reconciled |
| 9 | processed | `AI_SESSION_LOG_PROCEDURE.md` §9; 0033 → complete; 14 PROMPT Status lines dropped; INDEX rows |
| 10 | processed | LFS for the current five files (Phase 8 `chore` commit, no rewrite); ADR-009 "not recorded"; capture procedure; RE convention; PROPOSAL groups 1–4 |
| 11 | not reproduced; diagnostics added | A58-APP-06 row |
| 12 | processed; GOV-08 narrowed (manifest comment deferred) | GOV-08/09, SES-05, APP-08, DEC-04, HK-06 rows |

## Checkpoint answers (Phase 2, chat 2026-09-30, `AskUserQuestion`) — verbatim option labels

| # | Question (short) | Answer |
|---|---|---|
| Pre | Commit the 0058 RESULT first, on its own? | **"Commit and push now"** → `38d06a6 docs: record ai-sessions/0058 audit result`, pushed |
| 1 | ADR-048 (no ViewModel), AGENTS.md §8/§11 texts | **"Approve as shown (Recommended)"** (preview text = ADR-048 as written in `DECISIONS.md`) |
| 2 | `ACTION_BATTERY_LEVEL_CHANGED` out of AGENTS.md §5; Option 0 relabel; ADR-029 Update | **"Approve as shown (Recommended)"** |
| 3 | ADR-024 superseded by a new ADR-049 | **"New ADR-049 (Recommended)"** |
| 5(e) | EQ sliders while the EQ is unread | **"Disable until read (Recommended)"** |
| 5(a–d), APP-08 | tile `onStart`, `UnreadableAnswer`, atomic transitions, write quarantine + ADR-045 Update, `AppUiSession` | **"Approve all (Recommended)"** |
| 4 | Negative-needs-a-positive-control rule | **"Capture procedure + AGENTS §13 (Recommended)"** |
| 7/8 | CAP-064/CAP-065 split, CAP-065 = Group BA, Groups AZ/BA/AU/AV into the capture plan | **"Split as proposed (Recommended)"** |
| 7 | CAP-052 withdrawn; CAP-054 premise and `PROTOCOL.md` L868 corrected | **"Withdraw + correct (Recommended)"** |
| 8 | L-4 service names, L-5 relabel of "1779298694" | **"Approve as shown (Recommended)"** |
| 8 | L-2 HID = head-tracker sensor → 🟢 | **"Promote to FACT (Recommended)"** |
| 8 | L-1 address derivable → 🟡 + CAP-065 test | **"Record 🟡 + test in CAP-065 (Recommended)"** |
| 8 | Dosimeter in scope? | **"Not now — note only (Recommended)"** |
| 9 | 0033 → complete, drop PROMPT Status lines, prompt template §9 | **"Approve all (Recommended)"** |
| 10 | LFS for the five `.log.last` | **"Current files only, no rewrite (Recommended)"** — no force-push |
| 10 | ADR-009 provenance | **"Not recorded (Recommended)"** |
| 10 | `REVERSE_ENGINEERING.md` convention | **"Reword the claim (Recommended)"** (dated Updates kept; file added to rule 9a's non-destructive list) |
| 10 | PROPOSAL group 1 (Group-A repeat / PAIR-004) | **"Approve as recorded (Recommended)"** |
| 10 | PROPOSAL group 2 (extraction path) | **"Approve (Recommended)"** |
| 10 | PROPOSAL group 3 (superseded/stale markers) | **"Mark resolved/superseded (Recommended)"** |
| 10 | PROPOSAL group 4 (per-capture conclusions) | **"Pointer per item (Recommended)"** |


## Reading (what was read in full, what in part)

- In full: the files of the Progress block's "Mandatory reading", `ai-sessions/0058_AUDIT_RESULT_2026_09_30.md`, and every section changed in this session
  before it was changed (`AI_SESSION_LOG_PROCEDURE.md` §9 item 2). In part: `REVERSE_ENGINEERING.md` (the entries named in the ledger: convention
  paragraph, `nqx`, the call graph, `gbm`), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (§3, §5, §6, the Group sections touched, §9), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
  (the rows touched and §0.3), the capture FINDINGS/EN files named in the ledger (the paragraphs cited), `CHANGELOG.md` (L1–30, L150–215), `APP_TESTPLAN.md`
  (§L, §O, the end), the session records 0033/0054–0057 (header and Progress blocks).

## Tests, mutations, gate

| Mutation | What was broken | Caught by | Restored |
|---|---|---|---|
| M1 | `ConnectionStateMachine.transition()` back to read-then-write (with and without a widened window) | "a loss racing onReady always ends Disconnected" | `sha256sum -c` OK |
| M2 | an OK `ReadSetting` with an undecodable value back to waiting for `Timeout` | "an answered but undecodable settings read is UnreadableAnswer at once…" | OK |
| M3 | the tile's `combine` without `onStart { emit(null) }` | `AncTileTest` | OK |
| M4 | the write quarantine removed | "a late answer to a timed-out write is not taken by the next write" | OK |
| M5 | EQ sliders enabled while the EQ is unread | `EqScreenTest` | OK |
| M6 | `setEqGains` during a re-open back to `ConnectionLost` | "an EQ write during a re-open says so and sends nothing" | OK |

Fixtures: the new repository tests use real capture bytes (`CAP-036` 1462 for the settings read — one byte re-encoded and labelled as a supplementary
structural variant, `AGENTS.md` §11; the `CAP-062`/`CAP-015` ACK frames already in the fixtures for the quarantine); `AncTileTest`/`EqScreenTest` test pure
mappings/UI states, no wire bytes.

**Final gate** (after `./gradlew --offline clean`): `./gradlew --offline --max-workers=2 assembleDebug testDebugUnitTest test lint` → exit 0, BUILD SUCCESSFUL.
JUnit XML, per variant: `:data` 1570 (baseline 1567), `:hardware` 52 (51), `:ui` 11 (9); `:domain` 27 (25); 0 failures, 0 skipped. Lint: `:app` 1 warning
(`DataExtractionRules`, carried; needs a manifest change), the other modules none. Kotlin warnings: 0 (baseline 3). `aapt dump permissions` on the debug APK:
`BLUETOOTH_CONNECT`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_CONNECTED_DEVICE` and the app-private
`DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` — no `INTERNET`. `python3 scripts/ensure_footers.py` and `python3 scripts/lint_docs.py`: exit 0.

**Compliance:** no diff in any `*.gradle.kts`, `libs.versions.toml` or `AndroidManifest.xml`; no new permission or dependency; nothing sent on DLCI 0x08
(the app does not open it); `transport.send(` call sites in `BudsRepositoryImpl.kt`: 8 before and after; every write path passes `writeGate` (4 call
sites: ANC, EQ, settings, Ring) — `setEqGains`' new early return sends nothing; AGPL header block of the 4 new Kotlin files identical to `MainActivity.kt`'s;
Kotlin only. Privacy: the added text holds no MAC address, name or street address (scan of `git diff` + new files; the two added lines that showed
the Buds' address were redacted).

## `CAP-065` (Group BA) — the hardware re-test

Skeleton `captures/CAP-065-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BA/CAP-065-EVENT-NOTES.md`: I the 0059 fixes (BA-1…4), II the Settable byte with the ears in
view (BA-5…8, ADR-049's 🟡 and its three open samples), III lead L-1 — which channel the Buds announce with one bud out (BA-9…11), IV the robustness steps
moved from `CAP-064` VI, V the Material 3 / pull-to-refresh steps moved from `CAP-064` VIII (BA-12…17); restore, Test-ID mapping and refuted-if list.
`APP_TESTPLAN.md` §P mirrors section I. Registered in `id_registry.csv` and `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BA, §9).

## External sources (fetched 2026-09-30/10-01; quotes as returned by the fetch)

- AOSP `BluetoothDevice.java` (`android.googlesource.com/platform/packages/modules/Bluetooth/+/refs/heads/main/framework/java/android/bluetooth/BluetoothDevice.java`):
  `@SystemApi … public static final String ACTION_BATTERY_LEVEL_CHANGED = "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED";` — with the local
  `javap` check (no battery constant in the public `android.jar`, API 34). The `developer.android.com` reference page came back truncated and was not used.
- kotlinx.coroutines `combine` (`kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/combine.html`): "Returns a Flow whose
  values are generated with transform function by combining the most recently emitted values by each flow." — no value until every flow has emitted (A58-APP-01).
- Fast Pair Hearable Controls (`developers.google.com/nearby/fast-pair/specifications/extensions/hearablecontrols`), Notify ANC state, Settable toggles: "Any or
  all of the UI toggle bits above may also be set here, to indicate which are currently enabled." (ADR-049).
- Android head tracker HID protocol (`source.android.com/docs/core/interaction/sensors/head-tracker-hid-protocol`): "At the top level, the head tracker device
  is an app collection with the `Sensors` page (`0x20`) and the `Other: Custom` usage (`0xE1`)."; Sensor Description `0x0308` "#AndroidHeadTracker#1.0";
  Persistent Unique ID `0x0302` "a read-only array of 16 elements, 8 bit each"; Custom Values `0x0544`/`0x0545`/`0x0546` (L-2).
- GitHub API (`gh api repos/actions/{checkout,setup-python}/git/ref/tags/…` and `…/tags`): `actions/checkout` v5 → `fbc6f3992d24b796d5a048ff273f7fcc4a7b6c09`
  (= v5.1.0); `actions/setup-python` v6 → `ece7cb06caefa5fff74198d8649806c4678c61a1` (= v6.3.0) (A58-GOV-09).

## Deferred documentation (also in `TODO.md`, "Open after `ai-sessions/0059`")

- The full rule-9a fold of the remaining dated addenda in capture FINDINGS (three folded here).
- Open capture-FINDINGS proposals: `CAP-008` §5/§4, `CAP-026` 3, `CAP-029` 3 (`CASE-008`), `CAP-037` 3, `CAP-047` 4–5 — each needs the maintainer.
- A manifest change for lint `DataExtractionRules` and a justification comment for `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` (no manifest change was allowed).
- Run `CAP-065`. `TESTPLAN` `CASE-007`'s status (🔵 → 🟢?) is the maintainer's.
- `AGENTS.md` §13 step 2 was aligned with ADR-048 ("MVVM" removed, dated) — ADR-048's Consequences named only §8/§11; flagged here for the maintainer.

## Commits

The maintainer's answer in chat (2026-10-01, `AskUserQuestion` "Commit/push"): **"Commit and push (Recommended)"**. On top of `38d06a6`, no force-push,
no history rewrite:

| Commit | Subject |
|---|---|
| `8bf7901` | chore(capture): store the five .log.last files in Git LFS (current files only) — LFS oids = the files' sha256 |
| `203cc43` | docs(governance): no-ViewModel wording, battery API note, positive-control rule, prompt template |
| `0f89ad9` | docs(decisions): ADR-048 (no ViewModel), ADR-049 (Settable byte) and dated ADR Updates |
| `9b9c498` | docs(protocol): leads L-1…L-5, one-terminated pw_hdlc address, battery Option 0, Settable byte |
| `247ad81` | docs(captures): corrected negatives and FACTs, status banners, test-plan links, CAP-065 skeleton |
| `b65085a` | fix(app): process the 0058 app findings — atomic transitions, tile, undecodable reads, write quarantine, EQ sliders, AppUiSession |
| `b21146b` | ci: pin docs-workflow actions to commit SHAs and check every merged manifest |
| `a9cf404` | docs: align ARCHITECTURE, README, TODO, SECURITY and the test plans with the as-built app |
| (this file's commit) | docs: record ai-sessions/0059 — RESULT, INDEX, session-record status fixes |

Not staged: `android/.kotlin/`, build directories, `.vscode/`, `__pycache__`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0059_MAINTENANCE_RESULT_2026_09_30.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0059_MAINTENANCE_RESULT_2026_09_30
