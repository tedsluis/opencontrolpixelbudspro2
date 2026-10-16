# 0094_MAINTENANCE_RESULT_2026_10_15.md — Process every finding, lead, document proposal and decision of the 0093 audit

**Number:** 0094
**Category:** MAINTENANCE
**Date:** 2026-10-15
**Prompt:** ai-sessions/0094_MAINTENANCE_PROMPT_2026_10_15.md
**Title:** Validate and process the 69 findings, 5 leads, 19 document proposals, 10 decisions and 12 action rows of `ai-sessions/0093_AUDIT_RESULT_2026_10_15.md` —
documentation and scripts first, the app last
**Status:** complete

## Summary

Every item of `ai-sessions/0093` has a verdict (ledger below — no row open): the documentation and scripts first, then the app. The maintainer's checkpoint
answers decided the 🟢 texts, the ADR texts, the project-law notes and the fix release; two project-law proposals (A93-GOV-05/06) were declined. The capture
notes were corrected from three read-only sub-agents' verified drafts: 330 items confirmed and 654 edits in 139 files, six items not reproduced and left as
they were. The app is **1.3.1** (versionCode 10301, not released): "Use different Buds" now acts on the pairing the repository connects with and asks first; a
re-open keeps "Changed since the app last saw it"; a tap's `Set` no longer takes the `Get`'s answer; stream pushes no longer end reads; the error-model gaps are
closed; the fuzz tests fail on a throwing decoder; TalkBack reads the switches and sliders; each mode has one name. Each app fix has a test that fails on the 1.3.0
code or under a mutation. Nothing new on the wire. Its hardware run is `CAP-077` (Group BM, planned, skeleton written). Commits wait for the maintainer.

## Progress

| Phase | State | Notes |
|---|---|---|
| 0 Set-up | done | branch `maintenance/0094-process-0093`; baseline gate green (below); `lint_docs.py` exit 0 |
| 1 Triage | done | every item assigned (ledger column Phase); M-verified items of 0093 stand (same commit, no data change); S items re-checked as noted |
| 2 Checkpoint | done | three rounds, answers below |
| 3 Governance/decisions/protocol/RE documents | done | `PROTOCOL.md`, `DECISIONS.md`, `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `README.md`, `ARCHITECTURE.md`, `REVERSE_ENGINEERING.md`, `DESKRESEARCH_FINDINGS.md`, `CHANGELOG.md` (three dated notes); lint exit 0 |
| 4 Captures and test plans | done | the CAPTURE guide, `APP_TESTPLAN.md`, the TESTPLAN (P3/P4 items); the capture files from three read-only sub-agents' verified drafts, applied by the main session (Appendix A.4); the `CAP-077` skeleton (Group BM) |
| 5 Scripts, CI, remaining documents | done | `lint_docs.py` `check_governance()` (h)–(l) and the retired `promoted` status; `.github/workflows/lint-docs.yml` permissions; `.gitignore`; `RELEASING.md`, `CONTRIBUTING.md`, `WORKSTATION_PREPARATIONS.md`, `MAINTAINING_DOCS_SITE.md`, `SECURITY.md`, session records — **deviation:** started while the Phase 4 sub-agents were still running (no shared file; their drafts were applied afterwards) |
| 6 App: user-facing defect first | done | A93-APP-01 … 04, each test first (failing on 1.3.0) — **deviation:** started before the Phase 4 drafts were applied (app code and capture notes share no file) |
| 7 App: remaining items | done | A93-APP-05 … 08; version 1.3.1 / 10301; the `transport.send` call sites unchanged (8 MAESTRO, 3 Message Stream, before and after) |
| 8 Documents that follow the code | done | `ARCHITECTURE.md` (cause rule, stream replies, pairing memory, accessibility), `CHANGELOG.md` [1.3.1], `README.md`, `PROJECT.md`, `SECURITY.md` (fuzz), `APP_TESTPLAN.md` §Z, `TODO.md` §1/§6, the CONV-002 note |
| 9 Finish | done | this RESULT; commits await the maintainer |

### Phase 0 — recorded facts

- `git log -1`: `cb41e7d` 2026-10-15 20:29:44 +0200 "Merge pull request #60 from tedsluis/audit/0093-full-audit"; `git fetch` → nothing new (`HEAD..origin/main` empty).
- `git status --short` (before the branch): ` M ai-sessions/INDEX.md` (the planned 0094 row), `?? ai-sessions/0094_MAINTENANCE_PROMPT_2026_10_15.md`, `?? android/domain/bin/`.
- Branch: `maintenance/0094-process-0093` (from `main` at `cb41e7d`; the two uncommitted prompt-writing changes carried along).
- **Reading.** This session runs in the same chat as 0093, which read every mandatory document in full at `b9828ba` (0093 RESULT §3). `git diff --name-only
  b9828ba..HEAD` lists only `ai-sessions/0093_AUDIT_PROMPT_2026_10_15.md`, `ai-sessions/0093_AUDIT_RESULT_2026_10_15.md` and `ai-sessions/INDEX.md` — no other file
  changed since that reading, so it is not repeated; every file is re-read (the part concerned, or in full when it is rewritten) before it is edited.

## Ledger (one row per 0093 item; verdicts: processed / narrowed / rejected / not reproduced / already fixed / awaiting maintainer / capture skeleton / `TODO.md`)

Phase key: CP = maintainer checkpoint (Phase 2), P1 … P8 = the phase that changes the files.

| Item | Title (0093) | Phase | Verdict | Evidence / files changed | Notes |
|---|---|---|---|---|---|
| A93-GOV-01 | `PROJECT.md` says 1.3.0 is "not released". | P3 | processed | `PROJECT.md` §Status rewritten for 1.3.0 (released) |  |
| A93-GOV-02 | `PROJECT.md`'s status stamp and status paragraph lag the releases. | P3 | processed | `PROJECT.md:39` stamp; status narrative → a short current-release box + pointer to `CHANGELOG.md` |  |
| A93-GOV-03 | `PROJECT.md` phase 6 names only the protocol test plan. | P3 | processed | `PROJECT.md:27` names `APP_TESTPLAN.md` |  |
| A93-GOV-04 | `PROJECT_RULES.md` rule 13's reading list differs from `AGENTS.md` §0.1. | CP | processed | `PROJECT_RULES.md` rule 13 → `AGENTS.md` §0.1 (approved, "Projectwet") |  |
| A93-GOV-05 | `AGENTS.md` §6's framing paragraph still opens "Framing is not yet settled". | CP | rejected (maintainer) | not chosen at the checkpoint ("Projectwet") — `AGENTS.md` §6 framing paragraph unchanged |  |
| A93-GOV-06 | `AGENTS.md` §10's preferred stack names ViewModel-Compose; ADR-048 decided "no ViewModel". | CP | rejected (maintainer) | not chosen at the checkpoint — `AGENTS.md` §10 unchanged |  |
| A93-GOV-07 | `README.md` "Current state" and contributor section are out of date in four places. | P3 | processed | `README.md` Current state: date 2026-10-15, "58 accepted, 6 superseded, one draft", ADR-060 "a later release", newest run `CAP-076` |  |
| A93-GOV-08 | `README.md`'s Status section repeats nine release blocks. | P3 | processed | `README.md` §Status: the current release + its known issues; older releases → `CHANGELOG.md` |  |
| A93-GOV-09 | `CONTRIBUTING.md` describes a pre-release project and omits the repository rules. | P5 | processed | `CONTRIBUTING.md`: framing + "How a change gets in" |  |
| A93-GOV-10 | `WORKSTATION_PREPARATIONS.md` installs tools against their vendors' current guidance. | P5 | processed (narrowed) | `WORKSTATION_PREPARATIONS.md`: dated recommendation notes beside the recorded transcripts (npm docs, Claude Code setup, Antigravity release page — its RPM repo is legacy and unsigned by the vendor) |  |
| A93-GOV-11 | `WORKSTATION_PREPARATIONS.md` small defects. | P5 | processed | `warn` → an inline echo; `capinfos`, `uv` added to the check list |  |
| A93-PROT-01 | The 2026-10-14 FACT on setting 13 counts a class against itself ("73 of 73") and uses a wrong denominator ("12 | CP/P3 | processed | `PROTOCOL.md` §4.1 Correction 2026-10-15 (75/89, 12/89, 2/89; census numbers re-used — no log changed since the 2026-10-15 re-run); §8 row; `CHANGELOG.md` [1.3.0] note |  |
| A93-PROT-02 | "41 of 41 holds … while Play held the Message Stream" conflates the two users. | CP/P3 | processed | `PROTOCOL.md` §4.1 Correction 2026-10-15 item (2): 29/29 with Play, 12/12 without |  |
| A93-PROT-03 | The SASS burst's "in-use" string is attributed to Code `0x42`; it is in `0x41`. | CP/P3 | processed | `PROTOCOL.md` §4.5.2 Correction: "in-use" in 0x41 (`CAP-019` 804/806/810; SASS page) |  |
| A93-PROT-04 | The open §6 item on SASS Codes `0x11`/`0x21`/`0x40`/`0x42`/`0x34` is answerable from the official page. | CP/P3 | processed | `PROTOCOL.md` §6 SASS item ticked with the spec's names/lengths (🟢), encrypted contents 🔴 |  |
| A93-PROT-05 | The BLE Battery Notification is still labelled "confirmed as what the Buds Pro 2 send". | CP/P3 | processed | `PROTOCOL.md` §1 row and Option A Status relabelled (🔴 not observed from the Pro 2) |  |
| A93-PROT-06 | Two 🟢 promotions are not reflected in their entries' own Status lines. | CP/P3 | processed | `PROTOCOL.md` §4.5.3/§4.5.4 Status pointers |  |
| A93-PROT-07 | The ATT `0x0044` notification burst item can be closed. | CP/P3 | processed | `PROTOCOL.md` §6 ATT `0x0044` item closed with the 103-log scan |  |
| A93-PROT-08 | One frame number off by one. | CP/P3 | processed | `PROTOCOL.md:112` frame 1508 note |  |
| A93-PROT-09 | §6's open items lag findings made elsewhere in the same document. | CP/P3 | processed (narrowed) | §6: field-13 mirror item ticked; pointers on the `CAP-038` two-Notify item (stays open — trigger unfilmed), the DLCI 0x08 Get-code item and the GSND assistant item; DLCI 0x0a item updated (L93-1) |  |
| A93-CAP-01 | "Thirteen MAESTRO requests at Connect" (1.2.x) is a miscount in six documents. | P4 | processed | "thirteen" → fifteen in `APP_TESTPLAN.md` (8) and `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (5) with a dated note; 1.3.0 "fifteen" → seventeen; `CHANGELOG.md` [1.2.0] note; capture files via the sub-review edits |  |
| A93-CAP-02 | `CAPTURE_BLUETOOTH_HCI_SNOOP.md` stale statements. | P4 | processed | capture guide: Group AH sweep range, Group BK status, "opcode table" wording (3), Group column note, `promoted` retired, §4.1 superseded tip layers replaced by a current note |  |
| A93-CAP-03 | `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` breaks its own Evidence-column rule and has stale lines. | P4 | processed (narrowed) | `TESTPLAN`: groups "AA–BL", §9 `SWITCH-001`/`WELL-001`, `ANC-001` "Planned" wording; Evidence cells → pointers deferred to the restructure session (D9) |  |
| A93-CAP-04 | `APP_TESTPLAN.md` section Y has its columns shifted. | P4 | processed | `APP_TESTPLAN.md` §Y: the 22 rows' "Expected on the wire" restored from `8a72572` (counts → seventeen), results moved to "Result"; note under §Y |  |
| A93-CAP-05 | `PROTOCOL.md` and `CAP-058` name the link-key type wrongly: P-192, not P-256. | CP | processed | PROTOCOL.md key-type Correction (P-192; kernel commit 11015c79; tshark CAP-058 2235); CAP-058-FINDINGS.md :89 and :93 corrected (tshark 2235, 3804, .log.last 10333/11982 all "Combination Key, P-192"); the EVENT-NOTES name no P-256 (not reproduced there) |  |
| A93-CAP-06 | Test-IDs swapped or misused in five EVENT-NOTES (traceability, `AGENTS.md` §13 step 7). | P4 | processed | Test-ID tags re-checked against the TESTPLAN titles and corrected in the EVENT-NOTES (sub-agent items SC1/SC2/SC3, e.g. SC3-14/40/45/47/56/60/64; GATT-001 "attempted, no discovery" for CAP-003/010); Appendix A.4 |  |
| A93-CAP-07 | Counts in the 1.x capture FINDINGS that do not add up. | P4 | processed | every count re-derived (tshark/pwrpc_decode, one process at a time) and corrected in place with the 🟢 correction marker; differences from the sub-reviews' numbers recorded (SC3-30/31/54/67/68, SC2-117/119, SC1-20/106); Appendix A.4 |  |
| A93-CAP-08 | Wrong or stale statements in `CAP-026` … `CAP-051` files. | CP/P4 | processed | CAP-026…050: 118 of 122 items confirmed and applied (226 edits); SC2-05, SC2-50, SC2-97 not reproduced (left as is, reasons in Appendix A.4); SC2-116 withdrawn by its sub-review; SC2-18 extended (DLCI 0x08 frames 1592/1711 — PROTOCOL.md §6 CONV-002 Update and TESTPLAN CONV-002 row, re-checked with tshark) |  |
| A93-CAP-09 | Wrong or stale statements in `CAP-001` … `CAP-025`. | CP/P4 | processed | CAP-001…025: 142 of 143 items confirmed and applied (270 edits); SC1-133 not reproduced (no exact correction derivable — TODO.md §6); CAP-051…075: 70 of 72 + 3 extras applied (158 edits; SC3-07, SC3-38 not reproduced); 654 edits in 139 files, each OLD exactly once, lint exit 0; further slips found → TODO.md §6 |  |
| A93-ARCH-01 | §3.1's ANC reconciliation row describes 1.2.x behaviour as current. | P3 | processed | `ARCHITECTURE.md` §3.1 ANC row rewritten as built at 1.3.0 (sources, causes, no reset at Connect, hold as history); re-checked in Phase 8 after the app fixes |  |
| A93-ARCH-02 | §2.4's navigation tree and §9's persistence line still list removed 1.2.x settings. | P3 | processed | §2.4 tree and §9 persistence: the 1.2.x card/key as history |  |
| A93-ARCH-03 | `BluetoothGatt` is named as owned by the hardware layer; no GATT client exists. | P3 | processed | §2 Hardware layer and §2.1 `BudsTransport`: no `BluetoothGatt` as built |  |
| A93-ARCH-04 | §8.1 keeps the 2026-08 design text below the as-built note. | P3 | processed | §8.1 design paragraph → the as-built gate (announcement firmware + Model ID, ADR-042) |  |
| A93-ARCH-05 | §15 says no architecture question is open; §6.0b keeps one open. | P3 | processed | §15 lists §6.0b (b) as open |  |
| A93-DEC-01 | ADR-064's Status still says "not yet built". | CP | processed | ADR-064 Status pointer (built in 1.3.0, CAP-076, released) |  |
| A93-DEC-02 | ADR-064's Context repeats the circular "73 of 73". | CP | processed | ADR-064 Note 2026-10-15 (75 of 89; decision unchanged) |  |
| A93-DEC-03 | Superseded ADRs use two Status conventions. | CP | processed | ADR-061/062/063 Status begin "Superseded by ADR-065 (2026-10-14) — was: …" |  |
| A93-DEC-04 | Four early ADRs record no decision-maker. | CP | processed | ADR-002/004/005/007: provenance line "decided by the maintainer at project start" (maintainer's statement, "ADR-herkomst") |  |
| A93-HK-01 | `TODO.md` keeps finished items although its own rule and `RELEASING.md` E3 say to delete them. | P5 | processed | five `[x]` items deleted from `TODO.md`; lint (i) now fails on `[x]` |  |
| A93-HK-02 | Three open `TODO.md` items describe a world before 1.3.0. | P5 | processed | `TODO.md` stored-choice sentence and "1.3.0 candidate" relabelled |  |
| A93-HK-03 | `reverse-engineering/APK_VERSIONS.md` says the second APK was "Not pulled". | P5 | processed | `reverse-engineering/APK_VERSIONS.md`: three rows (SHA-256, `aapt2 dump badging`), dated note; `TODO.md` §4 updated |  |
| A93-HK-04 | `MAINTAINING_DOCS_SITE.md` still describes pushing to `main` and bots that commit to it. | P5 | processed | `MAINTAINING_DOCS_SITE.md`: PR flow and the bot branches |  |
| A93-HK-05 | `ai-sessions/INDEX.md` status cells drift from the RESULT headers; nothing checks them. | P5 | processed | INDEX cells 0069, 0079, 0089, 0091 aligned; lint (h) checks every cell against its RESULT |  |
| A93-HK-06 | `CHANGELOG.md` `[1.1.1]` contradicts itself on hardware testing. | P5 | processed | `CHANGELOG.md` [1.1.1] dated note (CAP-071 passed) |  |
| A93-HK-07 | IDE build output in `android/domain/bin/` is not ignored. | P5 | processed | `.gitignore` `android/**/bin/`; `android/domain/bin/` deleted (25 files, all byte-identical to `src/*/kotlin`, checked with `cmp`) |  |
| A93-HK-08 | Small leftovers. | P5 | processed | empty `scratch_0024/` removed; `reverse-engineering/tools/BACKLOG.md` doubled rule removed |  |
| A93-SES-01 | This audit's own prompt omits two releases. | P1 | rejected (no action) | the 0093 prompt's own context line; noted in 0093, nothing to change |  |
| A93-SES-02 | `0091` did not finish the `ARCHITECTURE.md` update its prompt required, and its RESULT says it did. | P3 | processed | the `ARCHITECTURE.md` gaps it describes are fixed (ARCH-01/02) |  |
| A93-SES-03 | `0091`'s compliance record lists a permission the app does not declare, and a wrong request count. | CP | processed | 0091 RESULT Corrections block (`BLUETOOTH_SCAN`, 17 requests, next-steps) |  |
| A93-SES-04 | `0090`'s headline ratios | P1 | processed with PROT-01 | a pointer |  |
| A93-SES-05 | `0090`/`0091`/`0092` small record defects. | CP | processed | 0090 RESULT Corrections (ratios, PR placeholder); `CAP-076` overwrite-delay wording via the capture edits |  |
| A93-SES-06 | Session records that disagree with the repository (sub-review of `0069` … `0089`, each re-verified). | CP | processed | Corrections blocks/status updates in 0069, 0073, 0078, 0079, 0081, 0082, 0089; status updates 0076, 0077, 0078, 0079, 0081, 0082, 0090 |  |
| A93-SES-07 | `AI_SESSION_LOG_PROCEDURE.md` §4b is not followed consistently. | CP/P5 | processed | "(this commit)" back-filled in 0073, 0075, 0076, 0077, 0078, 0079, 0086; missing commits listed in 0069, 0070, 0071, 0072, 0087, 0088; lint (l); `AI_SESSION_LOG_PROCEDURE.md` §4b items 5–6 |  |
| A93-SES-08 | ADR-060 entered `DECISIONS.md` as a "Proposed" heading, against the prompt; the lint forced it. | CP | processed | = D5 |  |
| A93-SES-09 | 1.2.2 was released without its hardware run and without the exception note `RELEASING.md` uses for 1.0.0. | CP | processed | = D10, `RELEASING.md` exception note |  |
| A93-APP-01 | "Use different Buds" acts on a second `BudsCompanionPairing`; the repository keeps connecting to the old Buds | CP/P6 | processed | PairingChoiceMemory shared by the Activity instance and the DI singleton (BudsCompanionPairing.kt, RepositoryModule.kt, MainActivity.kt); CompanionPairingChoiceTest 2 tests; mutation (per-instance memory) 2 of 2 fail |  |
| A93-APP-02 | A re-open's "Changed since the app last saw it" is overwritten by the equal `4:13` answer (CAP-076 defect (a)) | CP/P6 | processed | causeOfReadAnswer keeps CHANGED_SINCE_LAST_READ for the other read source of the same read round (readRound in launchInitialEqRead); test with real CAP-076 H1 4094 → 4135 (Cap076Fixtures.kt) fails on 1.3.0, passes now; two mutations killed. The audit's note that the re-open test answers Get and read differently is inexact: that test has no Get (it reads only); left as is |  |
| A93-APP-03 | A tap's `Set` can take the `Get`'s own Notify as its answer (CAP-076 defect (b), the silent tile tap). | CP/P6 | processed | no AncOutcome.Notified for a Notify that answers a Get; test with the caller on UnconfinedTestDispatcher reproduces the interleaving (1.3.0: Success with no answer; now Timeout) |  |
| A93-APP-04 | Settings-stream pushes can end a pending read's wait. | CP/P6 | processed | stream flag carried in RoutedFrame.Eq and MaestroReply.Value/Setting; the reads accept only RESPONSEs; applyAncState no longer feeds _ancModeFresh; four tests (4:13 read, EQ read, settings read, Refresh) — three fail on 1.3.0, the fourth by mutation |  |
| A93-APP-05 | Narrow error-model gaps (`AGENTS.md` §3/§8). | CP/P7 | processed | associate() in try/catch with the in-flight flag reset; RfcommBudsTransport attach() under the connection's lock with closeAll (by construction — no deterministic test of the window); application scope CoroutineExceptionHandler that logs; DataStore readableData() .catch(IOException) → defaults, test with a corrupt file (mutation killed) |  |
| A93-APP-06 | Tests that do not test what they say. | P7 | processed | SET_TRANSPARENT_CAP051_1527 = the real frame (tshark CAP-051 1527, the official app's Set with its 16 trailing bytes); fuzz tests in SettingsStreamCodecTest, OversizedLengthTest, HardwareInfoCodecTest, CodecRouterTest count onDecoderFault and assert 0 (mutation: a throwing router fails the fuzz test); three stale test names renamed |  |
| A93-APP-07 | Accessibility. | CP/P7 | processed | toggleable rows (Role.Switch / Role.Checkbox) in SettingsUi, ControlsScreen, DebugScreen; EQ band sliders with contentDescription + stateDescription (dB / not read); tab icons contentDescription = null; the UI tests' finders follow the merged rows (5 failed on the 1.3.0 layout); two new slider tests |  |
| A93-APP-08 | UI wording and small code issues. | CP/P7 | processed | AncMode.label used by buttons, (i), tile, notification; connectionStateText; presetLabel; Use different Buds confirmation dialog (test: Cancel 0, confirm 1); KDoc of removed parameters and MESSAGE_STREAM_LINGER_MS; modifier first optional parameter; log line "unchanged"; Toast on applicationContext; null-safe launch intent; stale TODO(verify) in BudsCompanionPairing replaced by the hardware confirmation |  |
| A93-APP-09 | `.github/workflows/lint-docs.yml` runs with the default token permissions. | P5 | processed | `.github/workflows/lint-docs.yml` `permissions: contents: read` (GitHub Docs "Automatic token authentication": "grant the GITHUB_TOKEN the least required access") |  |
| A93-RE-01 | Self-references by line number have drifted. | P3 | processed | four line self-references replaced by entry references |  |
| A93-RE-02 | A broken edit and a duplicated discovery in the `frb`/`fuh`/`glk`/`gjv` entry. | P3 | processed | broken sentence restored from `08fc3ad` (marked); correction on 0023's "both prior passes"; "seven classes" |  |
| A93-RE-03 | The "Correlation status with `PROTOCOL.md`" table stops on 2026-10-06. | P3 | processed | correlation table: four rows added, empty template row removed |  |
| A93-RE-04 | The call-graph block still shows four `GetSoftwareInfo` calls. | P3 | processed | call-graph block: "opens 4 pending calls — no request sent" |  |
| A93-RE-05 | The UUID register keeps a superseded 🟡. | P3 | processed | UUID register row: 🟢 pointer (ADR-019/034) |  |
| A93-RE-06 | Smaller document defects. | P3 | processed | mid-sentence pointer moved; Message Group intro, pbtk mention, "static findings 🟡" clarified; register rows 5/14 and the provider-service open questions pointed |  |
| L93-1 | lead | CP/P4 | processed | `PROTOCOL.md` §6 DLCI 0x0a Update (🟡 strong), `DESKRESEARCH_FINDINGS.md` 2026-10-03 item 4 Update; the census over every GSND burst in Phase 4 |  |
| L93-2 | lead | CP/P4 | processed | = A93-PROT-03/04 |  |
| L93-3 | lead | CP/P4 | processed | = A93-PROT-07 |  |
| L93-4 | lead | CP/P4 | TODO.md | `TODO.md` §4 decompile item (settings 23/31/39 named there) |  |
| L93-5 | lead | CP/P4 | TODO.md | recorded in the same `TODO.md` §4 item (the newer APK is where the next pass starts) |  |
| DOC-01 | 0093 §6 row 1 | P3–P5 | processed | = A93-ARCH-01…05 |  |
| DOC-02 | 0093 §6 row 2 | P3–P5 | processed (narrowed) | the two slips fixed; the template relaxation is part of the restructure sessions (`TODO.md` §6) |  |
| DOC-03 | 0093 §6 row 3 | P3–P5 | rejected (maintainer) | `AGENTS.md` §5/§6 shortening not chosen; only the D5 note added |  |
| DOC-04 | 0093 §6 row 4 | P3–P5 | processed (narrowed) | the stale lines fixed; the index-as-one-line restructure stays in `TODO.md` §6 (D9) |  |
| DOC-05 | 0093 §6 row 5 | P3–P5 | processed | = A93-HK-04 |  |
| DOC-06 | 0093 §6 row 6 | P3–P5 | TODO.md | the split stays a later session (D9); the corrections were made (RE-01…06) |  |
| DOC-07 | 0093 §6 row 7 | P3–P5 | processed | `AI_SESSION_LOG_PROCEDURE.md` §4b items 5–6 + lint (h)/(l) |  |
| DOC-08 | 0093 §6 row 8 | P3–P5 | processed | = A93-GOV-01…03 |  |
| DOC-09 | 0093 §6 row 9 | P3–P5 | TODO.md | Evidence cells → pointers: later session (D9) |  |
| DOC-10 | 0093 §6 row 10 | P3–P5 | processed | three dated corrections in `CHANGELOG.md` |  |
| DOC-11 | 0093 §6 row 11 | P3–P5 | processed | = A93-GOV-04 |  |
| DOC-12 | 0093 §6 row 12 | P3–P5 | processed | = A93-HK-01 + lint (i) |  |
| DOC-13 | 0093 §6 row 13 | P3–P5 | TODO.md | "Current" blocks: later session (D9); the corrections were made |  |
| DOC-14 | 0093 §6 row 14 | P3–P5 | processed | = A93-GOV-10/11 |  |
| DOC-15 | 0093 §6 row 15 | P3–P5 | processed | `APK_REVERSE_ENGINEERING_PROCEDURE.md` intro and §7 step 1 |  |
| DOC-16 | 0093 §6 row 16 | P3–P5 | processed | = A93-GOV-09 |  |
| DOC-17 | 0093 §6 row 17 | P3–P5 | processed | `RELEASING.md` E1 list |  |
| DOC-18 | 0093 §6 row 18 | P3–P5 | processed | `SECURITY.md` settings-stream paragraph (the fuzz-assert sentence follows APP-06 in Phase 8) |  |
| DOC-19 | 0093 §6 row 19 | P3–P5 | processed | = A93-CAP-01/04 |  |
| D1 | 0093 §7 decision | CP | processed | approved ("FACT-teksten"); applied in `PROTOCOL.md`, ADR-064, `CHANGELOG.md` |  |
| D2 | 0093 §7 decision | CP | processed | approved; `PROTOCOL.md` (CAP-058 files in Phase 4) |  |
| D3 | 0093 §7 decision | CP | processed | approved ("§6 en lead") |  |
| D4 | 0093 §7 decision | CP | processed | maintainer: "1.3.1 met APP-01…06 (Recommended)" — version 1.3.1 / 10301, CHANGELOG [1.3.1] without date, CAP-077 (Group BM) planned with skeleton, APP_TESTPLAN §Z |  |
| D5 | 0093 §7 decision | CP | processed | approved; `AGENTS.md` §6 note |  |
| D6 | 0093 §7 decision | CP | processed | approved ("Ik, bij de projectstart"); four provenance lines |  |
| D7 | 0093 §7 decision | CP | TODO.md | approved "34 houden" — a planned step in `TODO.md` §5 (Phase 5) |  |
| D8 | 0093 §7 decision | CP | processed | APK registered (HK-03); decompile stays a CROSSCHECK session in `TODO.md` §4 |  |
| D9 | 0093 §7 decision | CP | TODO.md | approved "Later" — three named MAINTENANCE sessions in `TODO.md` §6 (Phase 5) |  |
| D10 | 0093 §7 decision | CP | processed | `RELEASING.md` exception for 1.2.2 |  |
| ACT-01 | 0093 §8 action row | P1 | processed | A93-APP-01 … 04 fixed with tests and real bytes; the short capture is CAP-077 (planned skeleton) |  |
| ACT-02 | 0093 §8 action row | P1 | processed | Phase 3 |  |
| ACT-03 | 0093 §8 action row | P1 | processed | Phase 3 |  |
| ACT-04 | 0093 §8 action row | P1 | processed | A93-CAP-06/08/09 — the sub-agents' verified edits applied (Appendix A.4) |  |
| ACT-05 | 0093 §8 action row | P1 | processed | Phase 4 |  |
| ACT-06 | 0093 §8 action row | P1 | processed | Phases 3–5 + lint |  |
| ACT-07 | 0093 §8 action row | P1 | processed | A93-APP-06 — fuzz fault assertions and the real 1527 bytes |  |
| ACT-08 | 0093 §8 action row | P1 | processed | README, PROJECT, TODO, RELEASING E1 |  |
| ACT-09 | 0093 §8 action row | P1 | processed (narrowed) | registered; decompile in `TODO.md` |  |
| ACT-10 | 0093 §8 action row | P1 | processed (narrowed) | corrections done; split in `TODO.md` |  |
| ACT-11 | 0093 §8 action row | P1 | processed | A93-APP-07, A93-APP-08 |  |
| ACT-12 | 0093 §8 action row | P1 | processed | HK-04/07/08, GOV-10/11, APP-09 |  |

## Phase 1 — triage notes

- The 0093 items marked M were verified by the 0093 main session in this same chat at `b9828ba`; no capture, document or source file changed since
  (`git diff --name-only b9828ba..HEAD` → only the 0093 files), so their evidence stands and is re-read at the edit.
- S items that feed a `PROTOCOL.md` change, re-checked now (M): `CAP-062` NAKs — `tshark -r CAP-062-btsnoop_hci.log -Y 'btrfcomm.len>0 && (btrfcomm.dlci==4||btrfcomm.dlci==5)
  && frame.p2p_dir==1 && frame contains ff:02' -T fields -e data.data | grep -o 'ff02[0-9a-f]\{10\}' | sort | uniq -c` → `10 ff020003020812` (positive control: the same
  filter finds 10 — `PROTOCOL.md:810` already says ×10; `CAP-062-FINDINGS.md` says 11); `CAP-065` "7 of 7" — its own table (§4 rows 1–4, 6–9) has **8** rows with one bud
  (or one bud first) out, each fitting, so "8 of 8" (repeated at `PROTOCOL.md:411/3754`).
- External (A93-CAP-05): the Linux kernel commit 11015c79 (Marcel Holtmann, "Bluetooth: Add support for P-256 authenticated/unauthenticated link keys" — renames the
  older types with a P-192 suffix) and the Zephyr/Pigweed link-key definitions: 0x04 unauthenticated P-192, 0x05 authenticated P-192, 0x07 unauthenticated P-256,
  0x08 authenticated P-256 (the Core Specification text itself was not retrieved; Wireshark prints the same names for `CAP-058` 2235).
- A93-SES-01 (the 0093 prompt's release list) and A93-SES-04 (a pointer to A93-PROT-01): no change needed — verdict "rejected (no action)" / "processed with PROT-01".

## Checkpoint answers (chat 2026-10-15, `AskUserQuestion`, three rounds — the maintainer's choices verbatim)

| Question (short) | Answer |
|---|---|
| FACT texts in `PROTOCOL.md` (A93-PROT-01/02/05/06/08, A93-CAP-05, the `CAP-062`/`CAP-065` counts) | "Alles zoals voorgesteld (Recommended)" — the preview's texts |
| §6 closures and the lead (A93-PROT-03/04/07/09, L93-1) | "Alles zoals voorgesteld (Recommended)" |
| Capture notes (A93-CAP-06 … 09, Appendix B) | "Alles, na hercontrole (Recommended)" |
| ADR texts (A93-DEC-01/02/03) | "Alle drie (Recommended)" |
| Fix release (D4) | "1.3.1 met APP-01…06 (Recommended)" |
| UX (A93-APP-07/08) | "Toegankelijkheid (Recommended), Eén label voor de modus, Bevestiging bij 'Use different Buds', Opruimen kleine code-punten" |
| Restructures (D9) | "Later, als eigen sessies in TODO.md (Recommended)" |
| targetSdk (D7) | "34 houden, stap plannen in TODO.md (Recommended)" |
| Project law | "AGENTS.md §6: concept-ADR toegestaan (D5) (Recommended), PROJECT_RULES.md regel 13 (GOV-04)" — GOV-05 (§6 framing paragraph) and GOV-06 (§10 ViewModel) **not chosen** → declined |
| Session records (A93-SES-03/05/06/07, A93-HK-05) | "Ja, gedateerde regels + INDEX (Recommended)" |
| Provenance of ADR-002/004/005/007 (D6) | "Ik, bij de projectstart" |
| Smaller decisions | "RELEASING.md: uitzondering 1.2.2 (D10) (Recommended), APK 1.0.990706425 registreren (D8) (Recommended), scratch_0024/ en android/domain/bin/ opruimen (Recommended), Lint-controles uitbreiden (Recommended)" |

## Gate and lint

| When | Command | Result |
|---|---|---|
| Phase 0 baseline | `cd android && ./gradlew --offline --max-workers=2 assembleDebug testDebugUnitTest test lint` | BUILD SUCCESSFUL in 48 s, exit 0; JUnit `:data` 1672 per variant, `:hardware` 64 per variant, `:domain` 42, `:ui` 92 per variant, `:app` none, 0 failures (= 0093's baseline) |
| Phase 0 | `python3 scripts/lint_docs.py` | exit 0 |
| Phase 9 final (after an OOM kill of the first attempt; resumed in a new chat, `--max-workers=1`) | `cd android && ./gradlew --max-workers=1 clean build` | BUILD SUCCESSFUL in 5 m 42 s, exit 0; JUnit `:data` 1679 per variant, `:hardware` 66 per variant, `:domain` 42, `:ui` 95 per variant, 0 failures; Android lint 0 errors (`:ui` 0 warnings — the `ModifierParameter` warning is gone; `:app` 6 warnings) |
| Phase 9 final | `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py` | exit 0 |
| Phase 9 privacy | the MAC-like strings on the diff's added lines: 4 addresses, each with the same count in `HEAD` and the working tree (`git grep -o`) | rewritten lines only — nothing new |

## Mutation checks (one at a time, `--max-workers=2`; every file restored and compared with `diff -q`)

| Fix | Mutation | Result |
|---|---|---|
| A93-APP-01 | each `BudsCompanionPairing` gets its own `PairingChoiceMemory` (the 1.3.0 behaviour) | `CompanionPairingChoiceTest` 2 of 2 fail |
| A93-APP-02 | the other source's equal read in the same round returns `READ` (the 1.3.0 rule) | 1 of 172 fail ("the equal read after a differing Get keeps changed since") |
| A93-APP-02 | no `readRound++` in `launchInitialEqRead` | the same test fails at "another session's read" |
| A93-APP-03 | the 1.3.0 line (`Notified` emitted for the `Get`'s answer) | the race test fails: `Success` with no answer instead of `Timeout` |
| A93-APP-04 | the 1.3.0 code, per read | the 4:13 read (1 hardware request instead of 0), the EQ read (16 Maestro frames instead of 1), the Refresh (TRANSPARENT instead of ACTIVE) fail |
| A93-APP-04 | the settings read accepts a stream packet | "a stream push does not end a settings read" fails |
| A93-APP-05 | `readableData()` without its `catch` | "a corrupt settings file reads as the defaults" fails |
| A93-APP-06 | `routeMaestro` throws for a 5-byte payload | the settings fuzz test fails on "no decoder threw" (+3 other tests) |
| A93-APP-07 | the 1.3.0 layout (label beside the switch) | the UI tests' merged-row finders: 5 tests fail |

Not testable on the JVM: the transport's attach window (A93-APP-05 — closed by construction, under the connection's lock with `closeAll`) and the application
scope's exception handler (`:app` has no unit tests).

## Compliance (`AGENTS.md`)

- No `INTERNET` permission, no new permission, no new dependency (`git diff` of the manifests and `libs.versions.toml`: none). No GMS reference added.
- No new wire request: the `transport.send` call sites are the same before and after (8 MAESTRO, 3 Message Stream); no `WriteSetting 4:{13:n}`.
- No background or periodic connect; DLCI 0x04 is still claimed only for a tap, the tile, Refresh, a pull and the Connect's snapshot.
- No MAC address or serial in any new text (scan of the 654 drafted texts: 0 matches); every new Kotlin file carries the AGPL header.
- Fixtures are real bytes (`CAP-076` H1 4094/4135, `CAP-051` 1527), each with its command; negatives carry their command and a positive control.
- No 🟢 promotion and no ADR without the maintainer's answer in chat (the checkpoint above); the CONV-002 note is 🔴 open.

## External sources

- Fast Pair, "Smart Audio Source Switching" (developers.google.com/nearby/fast-pair/specifications/extensions/sass) — the SASS codes (A93-PROT-04).
- Linux kernel commit 11015c79 ("Bluetooth: Add support for P-256 authenticated/unauthenticated link keys") — the link-key type names (A93-CAP-05).
- Claude Code setup (code.claude.com/docs) — the native installer (WORKSTATION).
- npm Docs, "Resolving EACCES permissions errors when installing packages globally" (docs.npmjs.com) — WORKSTATION.
- antigravity.google/releases — the RPM repository note (WORKSTATION).
- GitHub Docs, "Automatic token authentication — Modifying the permissions for the GITHUB_TOKEN" — `.github/workflows/lint-docs.yml` (A93-APP-09).

## Deferred documentation

- The restructures of `TODO.md` §6 (the maintainer's choice D9: later, each its own session).
- The slips the sub-agents found outside the 0093 list (`TODO.md` §6, item "Found while `ai-sessions/0094` re-checked …").
- `CHANGELOG.md` [1.3.1] gets its date at the release (`RELEASING.md`).

## Commits

The maintainer approved in chat 2026-10-15 ("Ja, commit en push het"), on branch `maintenance/0094-process-0093`, pushed; no PR, no merge.

| Commit | Content |
|---|---|
| `f483b98` | documents, scripts and CI of Phases 3–5 |
| `bbc45fe` | the capture corrections, the test plans and the `CAP-077` skeleton |
| `9999d28` | the app 1.3.1 with its tests |
| `cf986f4` | `ARCHITECTURE.md`, `CHANGELOG.md`, `README.md`, `PROJECT.md`, `SECURITY.md`, `TODO.md`, this prompt and RESULT, the INDEX row |

## Appendix A.4 — the capture sub-agents

Three read-only sub-agents re-checked the 0093 Appendix B items per capture range, one `tshark` process at a time, and wrote verified OLD/NEW drafts; the main
session applied only CONFIRMED items, each OLD occurring exactly once at its turn (a dry run first: 0 failures), then ran `lint_docs.py` (exit 0).

| Range | Confirmed | Not reproduced / withdrawn | Edits |
|---|---|---|---|
| `CAP-001` … `CAP-025` | 142 | SC1-133 (the `0x20NN` count cannot be re-derived) | 270 |
| `CAP-026` … `CAP-050` | 118 | SC2-05 (the procedure's own numbering), SC2-50 (no film frame shows the version), SC2-97 (the film fits `CAP-044`); SC2-116 withdrawn | 226 |
| `CAP-051` … `CAP-075` | 70 + 3 extras | SC3-07 (all three numbers match the decode), SC3-38 (each line cites what it names) | 158 |

Where the right fix differed from the sub-review (re-derived numbers, recorded in the edits): SC1-20, SC1-35, SC1-38, SC1-106, SC1-107; SC2-18 (DLCI 0x08 frames
1592/1711 — also `PROTOCOL.md` §6 and the TESTPLAN `CONV-002` row, re-checked by the main session with `tshark`, exit 0), SC2-31, SC2-75, SC2-104/105/108,
SC2-117, SC2-119; SC3-30, SC3-31, SC3-54, SC3-67, SC3-68. The drafts are scratch files of this session (not kept in the repository).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0094_MAINTENANCE_RESULT_2026_10_15.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0094_MAINTENANCE_RESULT_2026_10_15
