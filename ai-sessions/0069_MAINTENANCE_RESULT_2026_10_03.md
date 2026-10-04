# 0069_MAINTENANCE_RESULT_2026_10_03.md — Process every finding, lead and document proposal of the 0068 audit into the project files and the app, and prepare CAP-068 for what must be tested first

**Number:** 0069
**Category:** MAINTENANCE
**Date:** 2026-10-03
**Title:** Validate and process all 85 findings, the 10 protocol leads, the 18 document improvement proposals and every row of the prioritised action list of `ai-sessions/0068_AUDIT_RESULT_2026_10_03.md`, and write the `CAP-068` capture skeleton
**Status:** awaiting maintainer sign-off — the work is done and gated; nothing is committed (the prompt asks for the maintainer's word first), 1.0.1 is not built for release, tagged or published, and `TODO.md` §1/§4 list the decisions that are the maintainer's (resumed once after a usage limit, during Phase 4)

---

## Summary

**What was asked:** validate and process every item of the `ai-sessions/0068` audit — 85 findings, 10 leads, 18 document proposals and the action
list — documents and scripts first, the app last, and prepare the hardware run for what cannot be settled at the desk.

**Result:** all 173 ledger rows are closed — 134 processed, 26 narrowed (the reason is in each row), 7 registered in `TODO.md`, 5 moved to a planned
capture, 1 rejected. Nothing is committed, tagged or published.

- **The one critical finding (`A68-APP-01`) is fixed:** a length field near the top of its integer range no longer throws or loops in the two protobuf
  readers, and the router decodes each frame inside a guard. Five new tests failed on the old code and pass now.
- **The app is 1.0.1 (versionCode 10001)** with, besides that fix: one rule for "is this value current" on every tab, the tile and the notification;
  no silent choice between several paired Buds and a "Use different Buds" action; the reworded messages; "—" for unread values; an equalizer preset
  "Flat"; exceptions around socket calls converted into errors. No new message is sent to the Buds, no permission and no dependency was added.
- **Documents:** the wrong statements the audit found are corrected in place (capture FINDINGS and notes) or by dated Updates (`PROTOCOL.md`,
  `DECISIONS.md`, `REVERSE_ENGINEERING.md`), each within the answers the maintainer gave at the checkpoint. `TODO.md` now holds open items only.
- **Scripts and CI:** `lint_docs.py` has three new checks (they found two table rows in `ARCHITECTURE.md` that had been broken across lines since
  before this session, and 59 short rows in five capture notes); `pwrpc_decode.py` reads DLCI 2 and 3 per direction; `release.sh` refuses unsafe
  release candidates; CI also compiles and lints the release variant.
- **Hardware:** `CAP-068` (Group BD, the 1.0.1 release build) and `CAP-069` (Group BE, the official app) are written as skeletons; `CAP-054` is
  redesigned for the case-open advertisement.

**Not reproduced or contradicted (the audit's sub-review claims):** `CAP-036`'s app version "…535" is what the notes cite from the screen, not a typo;
the third `Europe/Amsterdam` frame in `CAP-035` (frame 1306) is not found by `frame contains` (1180 and 1775 are); the proposed rule "`04 05` = `03` ⇔
Settable `00`" does not hold as an equivalence over all captures (table in `DESKRESEARCH_FINDINGS.md`); "`CAP-016`'s BLE link is the heart-rate device
of `CAP-018`" is narrowed to "a heart-rate device" (both use random addresses).

**Found on the way, not in the audit:** `PROTOCOL.md` §4.2's all-zero "Last saved" write (`CAP-015` frame 2111) is a real fixture for the "Flat"
preset, so that test needs no hand-built bytes; in `CAP-016` the first docking is on the wire as the charging bit at 06:33:35.057, 3–5 s before the
film's time for it; runtime-info field 2 is absent in the OpenControl captures, where no `SetWallclock` is sent.

**For the maintainer** (also `TODO.md` §1 and §4): whether to commit and push; building, testing (`CAP-068`) and publishing 1.0.1; whether the two
behaviours built on chat answers get an ADR each; the three protocol proposals of the desk entry of 2026-10-03.

## Progress

> A resumed session reads this block first (prompt §4), then the ledger, then `git status` / `git diff`.

| Phase | State |
|---|---|
| 0. Set-up, reading, baseline gate | done |
| 1. Triage and verification (read-only) | done for every M item and the S items listed below; the remaining S parts of `A68-CAP-15/16/23`, `A68-RE-03…06`, `A68-APP-08…15` are checked when their file is opened (Phases 3–7) |
| 2. Maintainer checkpoint | done (2026-10-03) |
| 3. Governance, decisions, protocol, RE record | done — `lint_docs.py` exit 0 (2026-10-03); no Gradle/CI file touched |
| 4. Captures, test plans, `CAP-068` | done — `lint_docs.py` exit 0 (2026-10-03); `CAP-068` (Group BD), `CAP-069` (Group BE) skeletons written, `CAP-054` redesigned; section IV of `CAP-068` gets its exact texts in Phase 8 |
| 5. Scripts, CI, release machinery, remaining documents | done — `lint_docs.py` exit 0 with its three new checks; the new CI step run locally (`:app:compileReleaseKotlin :app:processReleaseMainManifest lintRelease`, no signing value): BUILD SUCCESSFUL; `scripts/release.sh` dry-checked (version logic only, nothing built) |
| 6. App: `A68-APP-01`, `-02`, `-03` | done — gate green (`:data` 1596, `:hardware` 56, `:ui` 46, `:domain` 33); mutations M1–M8 each caught, sources restored |
| 7. App: remaining items | done — gate green (`:data` 1601, `:hardware` 64, `:ui` 58, `:domain` 37); mutations M9–M16 each caught; `transport.send(` sites identical to `HEAD` (8) |
| 8. Documents that follow the code | done — `APP_TESTPLAN.md` section S, `CAP-068` section IV with the built texts, `ARCHITECTURE.md`, `PROTOCOL.md` §4.2 pointer, `CHANGELOG.md` `[1.0.1]`, `README.md`, `SECURITY.md`, `TODO.md`, release notes template; `lint_docs.py` exit 0 |
| 9. Finish | done except the commit: final gate after `clean` green; the maintainer is asked whether to commit and push |

### Phase 0 — recorded facts

- `git log -1`: `cd3033284051cf7ab18768a4eaeb6bb6252f2464` — `docs: add the 0068 audit (prompt, result) and its INDEX row`. (The prompt names `29a975d` as the
  0068 commit; no such commit is on this branch — the 0068 pair is in `cd30332`.)
- `git status --short` at start: ` M ai-sessions/INDEX.md` (the maintainer's own 0069 row), `?? ai-sessions/0069_MAINTENANCE_PROMPT_2026_10_03.md`,
  `?? android/.kotlin/`.
- `git fetch && git log --oneline HEAD..origin/main`: empty (the remote is not ahead).
- `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py` before any change: exit 0 (informational sections only: historical session logs; `CAP-068` not yet
  registered, named by the 0069 prompt and its INDEX row).
- Baseline gate (before any change): `cd android && ./gradlew --offline --max-workers=2 assembleDebug testDebugUnitTest test lint` → BUILD SUCCESSFUL in 44 s, exit 0
  (329 tasks, 33 executed). JUnit XML: `:data` 1588, `:hardware` 56, `:ui` 35, `:domain` 30, `:app` none; 0 failures, 0 errors. Lint: "No issues found".
- **Read in full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md` (3,444 lines), `DECISIONS.md` (ADR-001…051, every Update),
  `TODO.md`, `AI_SESSION_LOG_PROCEDURE.md`, `RELEASING.md`, `ai-sessions/0068_AUDIT_RESULT_2026_10_03.md` (sections 1–8 and Appendix A), this prompt, and of the
  code `BudsRepositoryImpl.kt` and every file of `data/…/codec/` named below. **In part:** `ai-sessions/0068_AUDIT_PROMPT_2026_10_03.md` (§0–§2; it instructs the
  audit, not this session), `ai-sessions/INDEX.md` (rows 0059–0069 and the rows whose Status changes). Other files: per task, recorded at the task.

### Phase 1 — verification summary (re-derived in this session; commands in the scratchpad; the tally script is committed as `scripts/message_stream_tally.py`)

- **Reproduced (wire):** `A68-PROT-01` (19 Gets in 11 files; 14 × `e8`, 5 × `00`), `-02` (`CAP-019` 2482 `4:{11:0}`, 2487 `… 98 00`), `-03` (`CAP-020` 1183/1935/2038),
  `-04` (`CAP-025` 2040 Sent / 2044 Rcvd / 2045 Rcvd / 2048 Sent), `-10` (`CAP-036` 1423 "…DR3209"; sweep to 1587), `A68-CAP-01` (100 vs 23), `-02` (three 21 → 19
  switches), `-03` (71 frames), `-04` (4936), `-06` (six `0x13`), `-08` (7 + 5), `-09`, `-11`, `-12` (29 = 9 + 20), `-17` (56 `btavdtp`), `-18`, `-19`, `-20`, `-21`,
  `-22`, `A68-GOV-01` (0 × `08 12` in both `CAP-067` logs; control 4).
- **Reproduced (files):** `A68-GOV-02/03/04/06/07`, `A68-DEC-01` (the lost line was `## ADR-035 — DLCI 0x08 is claimed on demand for the Case battery: …; receive-only`,
  `git show 0f89ad9^:DECISIONS.md` line 1805), `A68-DEC-02/03`, `A68-HK-01…05`, `A68-ARCH-01/03`, `A68-RE-01/02/03/04`, `A68-APP-01` (both sites read),
  `A68-APP-02/03/04/07` (lines read).
- **Narrowed:** `A68-SES-01` — of the six commits, `8d8af4b`, `1752667` (0067 RESULT) and `275dc1d` (0065 RESULT) are named; `adc8c1f`, `9e2a475`, `d25edb8` and the
  publication are not. `A68-PROT-05` — full tally by message-level parse of server channel 2 (DLCI 4/5) over every log: 70 `Set`s, 59 ACKed, 11 NAKed reason `0x02`
  (`CAP-062` ×10, `CAP-064` ×1), each NAK after a wire `Notify` reading `00`; 57 ACKs after a non-zero `Notify`, 2 after an older `00` (`CAP-059` 2768, `CAP-060` 3174);
  captures with Sets: `CAP-001`, `006`, `039`, `040`, `049`, `051`, `059`, `060`, `062`…`066` (13).
- **New from the desk (A68-APP-12):** in `CAP-059`, `CAP-062`, `CAP-063` the Buds follow each of the app's Ring/Stop with their ACK `ff 01 00 03 04 01 00` and then
  their own `04 01 00 01 xx`; no phone-side `ff 01 00 02 04 01` follows (in `CAP-025` the official stack sent it). The app drops the Buds' message
  (`BudsRepositoryImpl` `RoutedFrame.Ring -> Unit`).
- **External (curl, raw text, 2026-10-03):** the three sentences 0068 quotes are on the pages as quoted; the SASS page: *"Bit 2: multipoint current state 1, if multipoint
  is on 0, otherwise"* and *"Bit 4: on-head detection current state 1, if on-head detection is turned on"* (MSB-first: `b8` → `98` clears bit 2).
- Intermediate results: the session scratchpad (the gate logs, the tally and environment scripts). Re-create them if the directory is gone.

### Checkpoint answers (verbatim)

All given by the maintainer in this session's chat on 2026-10-03 (`AskUserQuestion`; the option text is quoted as chosen; the full preview texts are applied verbatim in
the files named). Six rounds, 24 questions.

| # | Header | Answer (verbatim option) | Covers |
|---|---|---|---|
| 1 | "Hotfix 1.0.1" | *"1.0.1 with all of 0069 (Recommended)"* | `A68-APP-01`; versionName 1.0.1, versionCode 10001; reduced hardware set = `CAP-068` on the release APK |
| 2 | "DoD ANC" | *"Keep tick, reword + exception (Recommended)"* | `A68-GOV-01`: `PROJECT.md` Evidence row + "Evidence type", `RELEASING.md` exception note and rule from 1.0.1 |
| 3 | "ADR-035" | *"Restore as shown (Recommended)"* | `A68-DEC-01` + lint check |
| 4 | "Safe Mode" | *"Runbook + README, no override (Recommended)"* | `A68-DEC-03` |
| 5 | "Counts" | *"Approve all five (Recommended)"* | ADR-022 Update, ADR-024 Update pointer, `PROTOCOL.md` §4.1 tally, §2.2a "3 of 3", §6 serial suffix + sweep |
| 6 | "MP / gestures" | *"Dated Updates, statuses as drafted (Recommended)"* | `PROTOCOL.md` §4.5.2 and §4.5.4 Updates (`A68-PROT-02/03`) |
| 7 | "Ring" | *"Approve as drafted (Recommended)"* | `PROTOCOL.md` §4.4 Correction, §6 item ticked (`A68-PROT-04`) |
| 8 | "Other texts" | *"Approve all six (Recommended)"* | §4.3 A + ADR-006, ADR-030, ADR-033 Status, ADR-043 pointer, §2.3 DLCI sentence, §8 rows + README sentence |
| 9 | "AGENTS.md" | *"Four edits, no shortening (Recommended)"* | §10 DI, §13 CLI hygiene + step 8, `pbtk` wording §4/§13, ADR-048 Update for §13 step 2; §5/§6 shortening → `TODO.md` |
| 10 | "Rules 9a/4a" | *"Allow one banner + tighten 4a (Recommended)"* | `PROJECT_RULES.md` 9a and 4a |
| 11 | "Rule 20" | *"Clarify: all three allowed, bounded (Recommended)"* | rule 20 (a) quotations, (b) screenshots, (c) icons; `.gitignore` comment and the tool SPEC sentence aligned |
| 12 | "rc codes" | *"Script refuses unsafe candidates (Recommended)"* | `scripts/release.sh`, `RELEASING.md` §4 |
| 13 | "Stale values" | *"Keep the value, mark it (Recommended)"* | `A68-APP-02/03`: one not-current rule, "last connection", `ancMode` as a `StateFlow` |
| 14 | "Device choice" | *"No silent pick + 'Use different Buds' (Recommended)"* | `A68-APP-04/05` |
| 15 | "Wording" | *"Approve as drafted (Recommended)"* | `A68-APP-07`, `A68-GOV-03` |
| 16 | "Not read" | *"Visual '—' only"* | `A68-APP-08`: the visible text only; accessibility semantics → `TODO.md` |
| 17 | "Ring status" | *"Nothing new on the wire; test first (Recommended)"* | `A68-APP-12`: comment fix, `CAP-068` step |
| 18 | "EQ flat" | *"Add 'Flat' (0.0 × 5) now (Recommended)"* | `A68-APP-16` |
| 19 | "Dependencies" | *"Own session after 1.0.1 (Recommended)"* | `A68-APP-17` → `TODO.md` |
| 20 | "App polish" | *"Fix what a test proves (Recommended)"* | `A68-APP-06/09/10/11/13/14/15` |
| 21 | "Restructures" | *"Corrections now; TODO.md + CHANGELOG now; rest later (Recommended)"* | 0068 §6 |
| 22 | "Session log" | *"Close the three + Release log in RELEASING.md (Recommended)"* | `A68-SES-01/02` |
| 23 | "Leads" | *"CAP-068 = release build; CAP-069 = official app (Recommended)"* | L68-1…10; `CAP-054` redesigned |
| 24 | "Features" | *"Yes to both, after CAP-069 (Recommended)"* | head gestures and Multipoint as next features, each after FACT + ADR |

(24 answers in six `AskUserQuestion` calls.)

---


## Per-item ledger

Verdicts: processed / narrowed / rejected / not reproduced / awaiting maintainer / moved to `CAP-068` / registered in `TODO.md`.

| 0068 item | Verdict | Evidence (re-derived in this session) | Files changed | Phase |
|---|---|---|---|---|
| A68-GOV-01 | processed (documents); capture evidence moved to CAP-068 | checkpoint 2 | PROJECT.md; RELEASING.md rules; CAP-068 I | 3/5 |
| A68-GOV-02 | processed | `AGENTS.md` §10 bullet read; ADR-028 (2026-09-13); checkpoint 9 | `AGENTS.md` | 3 |
| A68-GOV-03 | processed | 'Unexpected error: …'; BudsError.SettingNotReadable replaces Unknown("EQ field not readable") | domain BudsError.kt; BudsRepositoryImpl.kt; ConnectionScreen.kt | 7 |
| A68-GOV-04 | processed | `git log -S` → `203cc43`; checkpoint 9 | `DECISIONS.md` (ADR-048 Update) | 3 |
| A68-GOV-05 | processed | recount: 1 fenced block, 56 inline Java spans (≈ 2,800 characters) match the decompiled tree; checkpoint 11 | `PROJECT_RULES.md` rule 20, `.gitignore`, the resolver tool's SPEC; icon attribution → Phase 5 | 3 |
| A68-GOV-06 | processed (rule) — the banners/fold per file in Phase 4 | 33 of 60 FINDINGS carry a banner; 9 named files have none; checkpoint 10 | `PROJECT_RULES.md` rule 9a | 3 |
| A68-GOV-07 | processed | ADR-009; §8 rows pointed in Phase 3 | README.md; PROTOCOL.md §8 | 3/5 |
| A68-PROT-01 | processed | 19 Gets in 11 files (frames in ADR-022 Update); 14 × `e8`, 5 × `00`; checkpoint 5 | `DECISIONS.md` ADR-022 Update, `PROTOCOL.md` §4.1; other copies in Phases 4–5 | 3 |
| A68-PROT-02 | processed | `CAP-019` 2482/2486/2489, 2296 `b8`, 2487 `98`; film 07:39:11–16 (switch ON → finger → OFF); SASS page "Bit 2"; checkpoint 6 | `PROTOCOL.md` §4.5.2 Update; `CAP-019-FINDINGS.md` in Phase 4 | 3 |
| A68-PROT-03 | processed | `CAP-020` 1183/1935 (07:47:00.005)/2038 (07:47:35.122); film 07:47:33–37; checkpoint 6 | `PROTOCOL.md` §4.5.4 Update; `CAP-020-FINDINGS.md` in Phase 4 | 3 |
| A68-PROT-04 | processed | `CAP-025` 2040 S / 2044 R / 2045 R / 2048 S (4 of 4; 2202 → 2204 ACK only); `CAP-059`/`062`/`063` no phone ACK; Device Action page; checkpoint 7 | `PROTOCOL.md` §4.4 Correction, §6 item ticked | 3 |
| A68-PROT-05 | narrowed and processed | tally 70 Sets / 59 ACK / 11 NAK over 13 captures (`scripts/message_stream_tally.py`); checkpoint 5 | `PROTOCOL.md` §4.1 Update; `scripts/message_stream_tally.py` | 3 |
| A68-PROT-06 | processed | raw page text (curl 2026-10-03): the "One common use case …" sentence; checkpoint 8 | `PROTOCOL.md` §4.3 Option A, `DECISIONS.md` ADR-006 Update; Group AP / `BATT-002` in Phase 4 | 3 |
| A68-PROT-07 | processed | rows 3437/3438 had 2 cells (awk cell count) | `PROTOCOL.md` §8 | 3 |
| A68-PROT-08 (§0 metadata) | processed | text read | `PROTOCOL.md` §0 | 3 |
| A68-PROT-08 (§1 transports table) | processed | §4.3 Option D 🟢 (`CAP-034`) | `PROTOCOL.md` §1 | 3 |
| A68-PROT-08 (§2 intro) | processed | text read | `PROTOCOL.md` §2 | 3 |
| A68-PROT-08 (§3 opening) | processed | ADR-041 | `PROTOCOL.md` §3 | 3 |
| A68-PROT-08 (§2.2a frame 1327 pointer) | processed | ADR-018 Update 2026-09-30 | `PROTOCOL.md` §2.2a | 3 |
| A68-PROT-08 (Option C BIEV count) | processed | `bthfp` 7 + raw 5 (3794, 4149, 4171, 5207, 5212, DLCI 0x09) | `PROTOCOL.md` §4.3 Option C | 3 |
| A68-PROT-08 (Option E 04 03) | processed (🟡 pointer, no status change) | tabulation over all logs: field 3 = lower bud level in 106 of 118 unequal samples, never the higher | PROTOCOL.md §4.3 Option E; DESKRESEARCH_FINDINGS.md 2026-10-03 | 4 |
| A68-PROT-08 (7-event burst) | processed | `CAP-048` 7705…7753 status `0x04`, 7781 `0x00` | `PROTOCOL.md` §6 | 3 |
| A68-PROT-08 (2 of 2) | processed | `CAP-066` `.log.last` announcements 820/2547/3267/5268/6441/6850, DISCs 2502/5223/6278/6803; checkpoint 5 | `PROTOCOL.md` §2.2a | 3 |
| A68-PROT-08 (22 Notify) | processed | 29 = 9 × `00` + 20 × `e8` | `PROTOCOL.md` §4.1, ADR-024 Update | 3 |
| A68-PROT-08 (CAP-066 command) | processed | `.log.last` 28 `00` / 41 `e8`; `.log` 7 `e8` | `PROTOCOL.md` §4.1 pointer | 3 |
| A68-PROT-09 | processed | each item's answering text located; HFP item: 3 SLC handshakes in `CAP-002` after frame 2663 | `PROTOCOL.md` §6 (six items ticked with pointers) | 3 |
| A68-PROT-10 | processed | `CAP-036` 1423 strings; sweep requests 1412…1585, last answer 1587; checkpoint 5 | `PROTOCOL.md` §6 (two places) | 3 |
| A68-CAP-01 | processed | `frame contains "AT+"` → 100 vs 23; handshake 48954–49079 (17:05:34.541–.759, DLCI 0x0c); 3 SLC handshakes after frame 2663 | `CAP-002-FINDINGS.md` §2 row, §5, §8, §9; `CAP-008-FINDINGS.md` §3; `CAP-012-FINDINGS.md` §6; `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group V + Index row; `PROTOCOL.md` §6 | 4 |
| A68-CAP-02 | processed | announcements A820 (21), A2547 (19), A3267 (21), A5268 (19), A6441 (21), A6850 (19); Buds `DISC` A2502, A5223, A6278, A6803 | `CAP-066-FINDINGS.md` §4 row 4 + conclusion, `CAP-066-EVENT-NOTES.md`; `PROTOCOL.md` §2.2a | 4 |
| A68-CAP-03 | processed | 71 frames on DLCI 0x08 (`SABM` 1147, `UA` 1153, 58 with payload); `frame.cap_len` ≤ 15 | `CAP-017-FINDINGS.md`, `CAP-017-EVENT-NOTES.md`, Index row | 4 |
| A68-CAP-04 | processed | Buds `DISC` 2188, 2189, 3651, 4936 (17:59:15.106); debug export 17:59:15.114–.126 | `CAP-060-FINDINGS.md` §1 (row 3b, row 4), `CAP-060-EVENT-NOTES.md` | 4 |
| A68-CAP-05 | processed | 14 = 12 `handleRelaunchActivity` (06:01:11…06:03:07 UTC) + 2 `MainActivity` creations at 06:10:44 / 06:11:28; 7 zeros among the 17 decoded balance writes; §7 table: 5 session ends + 1 failed connect | `CAP-067-FINDINGS.md` §5/§6/§10, `ARCHITECTURE.md` §2.4, `CHANGELOG.md`, `TODO.md`, `id_registry.csv`, Capture Index, `PROJECT.md`; the 0067 RESULT is left as written (session record) | 3–4 |
| A68-CAP-06 | processed | six `0x13` (1498, 3646, 4653, 7094, 8613, 9683), one LE `0x08` (12282) | `CAP-065-FINDINGS.md`, `CAP-065-EVENT-NOTES.md` | 4 |
| A68-CAP-07 | processed | see `A68-PROT-04`; 18 frames 2040…2204 with directions | `CAP-025-FINDINGS.md` §3–§6, §9; `CAP-025-EVENT-NOTES.md`; ADR-011 pointer | 4 |
| A68-CAP-08 | processed | `CAP-059`: 7 `bthfp` + 5 raw (3794, 4149, 4171, 5207, 5212) = 12; `CAP-062`: 65 frames with `AT+BIEV` (15 + 50) | `CAP-059-FINDINGS.md` §7, `CAP-062-FINDINGS.md`, `CAP-062-EVENT-NOTES.md`, `ARCHITECTURE.md` §3.1/§5a, `PROTOCOL.md` Option C; ADR-040's Context ("seven times") left unchanged — an ADR text, registered in `TODO.md` | 3–4 |
| A68-CAP-09 | processed | `CAP-003` 1621/1689/1750, no `btsmp`; `CAP-004` 1856, `CAP-014` 2365; checkpoint 8 | `CAP-012-FINDINGS.md` §2, ADR-030 Update | 3–4 |
| A68-CAP-10 | processed | 28 "TBD" replaced by pointers; `CAP-064` notes: P2 held (`flags=39`), 9 + 5 announcements, "Charge 6"; `CAP-040` :250 (frame 980 = `03 01` time zone), `CAP-027` :23 (845, 862, 875 + 904), `CAP-019` :47 (2326 is an ANC `Notify`) | `CAP-047-`, `CAP-064-`, `CAP-040-`, `CAP-027-`, `CAP-019-EVENT-NOTES.md`; `CAP-064-FINDINGS.md` | 4 |
| A68-CAP-11 | processed | six `0x04` + 7781 `0x00` | `CAP-048-FINDINGS.md` §1/§6; `PROTOCOL.md` §6 | 3–4 |
| A68-CAP-12 | processed | 29 `08 13 00 04` frames (9 × `00`, 20 × `e8`); checkpoint 5 | `CAP-063-FINDINGS.md` §3, ADR-024 Update, `PROTOCOL.md` §4.1 | 3–4 |
| A68-CAP-13 | processed | 12-cell CAP-067 row, CAP-064…066 cells, Android '17 ⚪' for CAP-036…041, legend 'withdrawn', stale phrases; registry rows corrected. CAP-036 app version '…535' is cited from the screen in its notes: not reproduced as a typo | CAPTURE_BLUETOOTH_HCI_SNOOP.md §9; id_registry.csv | 4 |
| A68-CAP-14 | processed | before/after comparison on CAP-036/050/062/066/067: no line lost; DLCI 3 adds 892/8/19/25 packets | scripts/pwrpc_decode.py | 5 |
| A68-CAP-15 | processed | grep of each Test-ID in the notes (0 hits for CAP-001 BATT-004/APP-001/APP-002/GFPS-002, CAP-003 GATT-001, CAP-013 GFPS-002; PAIR-003 metadata only in CAP-040/041; CAP-004 GFPS-001 is referenced: not a gap); CAP-059: 8 Sets 4250…4581 and 12 EQ writes re-derived | EVENT-NOTES of CAP-001/002/003/013/040/041/059/062 | 4 |
| A68-CAP-16 (CAP-014 frame 3353) | processed | tshark -x: 0b + 24-byte name; btatt.handle prints 0x0034,0x7869 (dissector artefact) | CAP-014-FINDINGS.md | 4 |
| A68-CAP-16 (CAP-016 §3/§6) | processed | DISC 2770/2772 Rcvd; 03 03 … e4 64 ff at 3173 (06:33:35.057) | CAP-016-FINDINGS.md, CAP-016-EVENT-NOTES.md | 4 |
| A68-CAP-16 (CAP-016 BLE link) | narrowed | Heart Rate service 0x180D on handle 0x0002 (frame 760) = 🟢 not the Buds; 'same device as CAP-018' stays 🟡 (random addresses differ) | CAP-016-FINDINGS.md §2/§11; PROTOCOL.md §6 (two Updates) | 4 |
| A68-CAP-16 (CAP-007 §3.3) | processed | 1350 at .436, DISC 1351 at .438; 04 05 06→05 at 717/1345 | CAP-007-FINDINGS.md | 4 |
| A68-CAP-16 (CAP-015 notes) | processed | 3487 4:{15:0}, 3505 4:{15:1} | CAP-015-EVENT-NOTES.md; PROTOCOL.md §4.5.6 Update | 4 |
| A68-CAP-16 (CAP-024 frame 1988) | processed | read 1096 28:1, write 1988 28:0 | CAP-024-FINDINGS.md, EVENT-NOTES; PROTOCOL.md §4.5 and §6 Updates | 4 |
| A68-CAP-16 (invalid filters 007/009/028) | processed | each quoted filter exits non-zero; valid filters with positive controls (btl2cap.psm: 1994 frames; btavctp/btavrcp: 6) | CAP-007/009/028-FINDINGS.md | 4 |
| A68-CAP-16 (arithmetic slips) | narrowed | CAP-009 (68 pushes, 5 points) done; the slips in CAP-001, 004, 008, 010, 012, 015, 047, 049, 050, 060 are registered in TODO.md | CAP-009-FINDINGS.md; PROTOCOL.md §4.3; TODO.md | 4/5 |
| A68-CAP-16 (CAP-002 tail) | processed | 39 frames on handle 0x000b | CAP-002-FINDINGS.md | 4 |
| A68-CAP-17 | processed | `-Y "avdtp"` exit 4; `btavdtp` 56; `SetConfiguration` 967/2932, `Open` 977/2943; `bta2dp` 0 | `CAP-038-FINDINGS.md` §6, §9; `CAP-038-EVENT-NOTES.md` | 4 |
| A68-CAP-18 | processed | `ReadSetting` answers per window (fields 4, 7, 15, 16, 17, 19, 29); `SetWallclock` 783 = 1788707520935 ms = the frame's capture time | `CAP-041-FINDINGS.md` banner, §3, §6, §7, §8; `PROTOCOL.md` §6 | 4 |
| A68-CAP-19 | processed for `CAP-043`; the address-scoped commands of `CAP-033`/`CAP-044`/`CAP-035` in the batch below | frame 91 `01 57 fd 0a 02 00 04 07 6e cf 6e 00 04 02` (= `CAP-032` frame 91); `btrfcomm or btsdp or bthci_evt.code==0x03` → 0 (control `CAP-044`: 323) | `CAP-043-FINDINGS.md` §1/§2/§3, `CAP-043-EVENT-NOTES.md` | 4 |
| A68-CAP-20 | processed | 4518/4528/7627 Dosimeter stream; 38 + 8 + 6 frames | `CAP-042-FINDINGS.md` banner + §5 | 4 |
| A68-CAP-21 | processed | frame 653: event `0x03`, status `0x04`, link type `0x01` | `CAP-039-FINDINGS.md` §1 | 4 |
| A68-CAP-22 | processed | `CAP-038` handle 0x0001: DLCI 0x03/ch 1, 0x05/ch 2, 0x09/ch 4, 0x0b/ch 5 (direction 1), 0x08/ch 4 (direction 0); 1074 `Rcvd SABM Channel=0` | `CAP-038-FINDINGS.md` §3, §9; `CAP-038-EVENT-NOTES.md`; `PROTOCOL.md` §2.3 | 4 |
| A68-CAP-23 (CAP-032) | processed | opcode 0xFD57 ×69 before frame 768 in four bursts (23/30/10/6) | CAP-032-FINDINGS.md | 4 |
| A68-CAP-23 (CAP-033 EIR) | processed | frame 1072 = Extended Inquiry Result of the Buds with five 128-bit UUIDs | CAP-033-FINDINGS.md | 4 |
| A68-CAP-23 (CAP-034) | processed | the notes (primary record) say the Pixel 9a had connected before; findings and index aligned | CAP-034-FINDINGS.md; CAPTURE_BLUETOOTH_HCI_SNOOP.md | 4 |
| A68-CAP-23 (Pixel 7a Android version) | processed (⚪, unreconciled) | no bugreport in those folders to read the version from | 12 files of CAP-036…041; CAP-043-FINDINGS.md; index | 4 |
| A68-CAP-23 (CAP-036 §12.6) | processed | frame 2048: field 2 = 1, 2, 2 | CAP-036-FINDINGS.md | 4 |
| A68-CAP-23 (notes vs findings 036/040/037) | processed | 45 frames; frame 980; 6.4–283.0 ms; 12008 at 06:18:24.27 | CAP-036/037/040-EVENT-NOTES.md | 4 |
| A68-CAP-23 (CAP-037 §5/§8) | processed | 12065 Disconnect Complete 06:18:28.089; CAP-048 §4 | CAP-037-FINDINGS.md | 4 |
| A68-CAP-23 (CAP-035) | narrowed | last frame 1945; ~3 s gap; the third 'Europe/Amsterdam' frame (1306) not reproduced (frame contains → 1180, 1775) | CAP-035-FINDINGS.md, EVENT-NOTES | 4 |
| A68-CAP-23 (CAP-042) | processed | 38+8+6; 04 12 at 700/751 (and 4522/4606/7569/7671); 7569 at 18:05:03.907 | CAP-042-FINDINGS.md, EVENT-NOTES | 4 |
| A68-CAP-23 (CAP-040) | processed | 0e 01: 14 / 1 / 7; offsets by arithmetic from the table's own first row; banner added | CAP-040-FINDINGS.md | 4 |
| A68-CAP-23 (CAP-039) | processed | 47 frames; 14 Notifies (7+7, all e8) | CAP-039-FINDINGS.md | 4 |
| A68-CAP-23 (slips 031–034/041/043/044) | narrowed | corrected: CAP-031 :106, CAP-032 :12, CAP-033 §-references, CAP-034 times and 0x0400, CAP-041 notes :55, CAP-044 2m12s (CAP-043 already done); the rest registered in TODO.md | those files; TODO.md | 4/5 |
| A68-CAP-23 (folder names) | registered in TODO.md | a rename breaks every link to the folder; discrepancy of 2–8 s recorded | TODO.md | 5 |
| A68-CAP-23 (EVENT-NOTES templates) | registered in TODO.md | — | TODO.md | 5 |
| A68-CAP-23 (PAIR-003) | processed | flagged in the notes of CAP-040/041 | CAP-040/041-EVENT-NOTES.md | 4 |
| A68-CAP-23 (rule-9a layers) | registered in TODO.md | rule 9a now allows one status banner (checkpoint 10); the remaining layers are a later rewrite | PROJECT_RULES.md; TODO.md | 3/5 |
| A68-ARCH-01 | processed | grep uses-permission: four permissions, no BLUETOOTH_SCAN | ARCHITECTURE.md §9 (bullet + permissions table) | 5 |
| A68-ARCH-02 | processed (pattern documented); fallback text follows the Phase 7 change | BudsCompanionPairing.kt name pattern | ARCHITECTURE.md §9.0a | 5/8 |
| A68-ARCH-03 | narrowed | dockStateUpdatedAt → ancAvailabilityUpdatedAt; '13 of 13' → 14 done in Phase 4; '11 00 answers' → 12 is a sub-review count not re-derived: registered in TODO.md | ARCHITECTURE.md; TODO.md | 5 |
| A68-ARCH-04 (§1 :137/:172) | processed | OpenControlNavHost.kt: Debug is a tab of the settings menu; every tab reachable | ARCHITECTURE.md §2.4 | 5 |
| A68-ARCH-04 (ACTION_STATE_CHANGED) | processed | BluetoothStateObserver.kt:39 | ARCHITECTURE.md §6 | 5 |
| A68-ARCH-04 (notification) | processed | OpenControlApplication.notificationText: state and ANC mode only | ARCHITECTURE.md §6.0a | 5 |
| A68-ARCH-04 (GATT in module list) | processed | no GATT client in :hardware | ARCHITECTURE.md §1/§2 | 5 |
| A68-ARCH-04 (§5 protobuf/CodecRouter) | processed | ADR-041; CodecRouter has feed() only | ARCHITECTURE.md §5 | 5 |
| A68-ARCH-04 (§6.0b (a)) | processed | ADR-044 | ARCHITECTURE.md §6.0b | 5 |
| A68-ARCH-04 (§15) | processed | PROJECT.md: non-goal | ARCHITECTURE.md §15 | 5 |
| A68-ARCH-04 (§3.1 vs §5a) | processed | CAP-063 AY-14: 8 of 8 | ARCHITECTURE.md §5a | 5 |
| A68-ARCH-04 (§2.4 TODO(verify)) | processed | CAP-065 pull, CAP-067 back, swipe in no capture | ARCHITECTURE.md §2.4 | 5 |
| A68-ARCH-05 | processed | constants read from the code (grep const val .*_MS) | ARCHITECTURE.md §3.2 (new table); ADR-043 Update points to it | 3/5 |
| A68-DEC-01 | processed | `git show 0f89ad9^:DECISIONS.md` line 1805; now `^## ADR-035` at line 1881, 51 headings; checkpoint 3 | `DECISIONS.md`; lint check in Phase 5 | 3 |
| A68-DEC-02 | processed | Status line read; checkpoint 8 | `DECISIONS.md` ADR-033 | 3 |
| A68-DEC-03 | processed | checkpoint 4: runbook + README, no override | RELEASING.md §12; README.md | 5 |
| A68-DEC-04 | processed | see `A68-PROT-01`, `-CAP-12`, `-CAP-09`, `-ARCH-05`; ADR-049's tally: `PROTOCOL.md` only (the maintainer chose "Approve all five", not the ADR-049 option) | `DECISIONS.md` ADR-022/024/030/043 Updates | 3 |
| A68-HK-01 | processed | dry check: 1.0.1 → 10001; 1.0.1-rc.1, 1.1.0-rc.1, rc.100, 1.100.0 refused; 1.0.0-rc.1 → 9901 | scripts/release.sh; RELEASING.md §4 | 5 |
| A68-HK-02 | processed | gh release view v1.0.0: Known issue line | CHANGELOG.md [1.0.0] Known issues; README.md; RELEASING.md §4 (template step) | 5 |
| A68-HK-03 | processed | fingerprint from the published notes = CAP-067-FINDINGS.md | README.md; SECURITY.md; RELEASING.md §13 | 5 |
| A68-HK-04 | processed | gh api: {"enabled":true}; topics checked | TODO.md (restructured); RELEASING.md §9 | 5 |
| A68-HK-05 | processed | local run of the new CI step without a signing value: BUILD SUCCESSFUL; no minify in the build, so no R8 step; distributionSha256Sum registered in TODO.md | .github/workflows/android.yml; android/.gitignore; scripts/release.sh | 5 |
| A68-HK-06 | processed | README lines read | README.md | 5 |
| A68-HK-07 | processed | three entries moved into date order; superseded sentence marked | CHANGELOG.md | 5 |
| A68-HK-08 | processed | the checks found 68 short rows, two table rows broken across lines in ARCHITECTURE.md, and the Status lines of 0022/0023/0040 | scripts/lint_docs.py; 8 files with table fixes | 5 |
| A68-SES-01 | processed (narrowed in Phase 1) | git log | RELEASING.md §13 Release log | 5 |
| A68-SES-02 | processed | headers of 0022/0023/0040 | the three RESULT files; ai-sessions/INDEX.md | 5 |
| A68-SES-03 | registered in TODO.md | not re-read in this session | TODO.md §6 | 5 |
| A68-SES-04 | moved to CAP-068 | — | CAP-068 V; TODO.md §2 | 4/5 |
| A68-SES-05 | rejected (historical; the audit asks no action) | — | — | 5 |
| A68-APP-01 | processed | five new tests failed on the unfixed code (IllegalArgumentException ×3, a 60 s timeout, the guard stub); both length checks now compare without adding; the router decodes each frame inside decodeGuarded; mutations M1–M3 caught. Other length-driven read sites checked: the fixed-offset readers (AncFrameDecoder, ModelIdFrame, MessageStreamReply, Battery/Ring decoders, Hdlc.decode) each test the size first; Varint.decode yields a negative Int for an overlong value, which both checks refuse | android/data …/codec/CaseBatteryFrame.kt, PwRpc.kt, CodecRouter.kt, BudsRepositoryImpl.kt (fault log line), OversizedLengthTest.kt | 6 |
| A68-APP-02 | processed | checkpoint 13; one rule (isCurrent) in :domain used by the ANC tab, the tile and the notification; Sound/Controls marked while not connected; battery values marked at Disconnect and at a loss; Case line '(last connection)'; mutations M5–M8 caught | domain/ValueCurrency.kt, BudsRepository.kt, AncTile.kt; BudsRepositoryImpl.kt; ui AncScreen.kt, EqScreen.kt, ControlsScreen.kt, ConnectionScreen.kt, SettingsUi.kt, Details.kt, OpenControlNavHost.kt; app MainActivity.kt, OpenControlApplication.kt, AncTileService.kt; tests in :domain, :data, :ui | 6 |
| A68-APP-03 | processed | ancMode is a StateFlow<AncMode?>; test with a busy collector and three real Notifies; mutation M4 caught (9 tests fail) | BudsRepositoryImpl.kt; BudsRepositoryImplTest.kt | 6 |
| A68-APP-04 | processed | checkpoint 14; chooseBondedOrAsk returns Several for two named candidates without an association; new status SeveralBudsPaired with the approved sentence; mutations M9, M16 caught | hardware PairingLogic.kt, BudsCompanionPairing.kt; domain DeviceStatus.kt; ui ConnectionScreen.kt, PullRefresh.kt; app MainActivity.kt; PairingLogicTest, DeviceStatusTest, PullActionTest | 7 |
| A68-APP-05 | processed | checkpoint 14; 'Use different Buds' on the Settings tab: Disconnect, disassociate this app's own associations, name fallback off until a new association (in memory); mutation M10 caught; the CDM calls themselves are not unit-testable → CAP-068 BD-17 | BudsCompanionPairing.forgetAssociations; ui SettingsMenu.kt; app MainActivity.kt; SettingsMenuTest | 7 |
| A68-APP-06 | narrowed | added: UserMessageTest (every error type, literal), ControlsScreenTest, FindMyBudsScreenTest, split-frame reassembly at every cut, the hex gate, real NAK (CAP-064 3440) in CodecRouterTest, labels on the hand-built fixtures (CaseBatteryCodecTest, helloFrame), literal lines in BatteryCardTest/AncScreenTest. Not done: tests for :app (no test set-up in that module; adding one is a build change), Disconnect during Connect and openSession success (need a BluetoothDevice) — TODO.md. RfcommBudsTransportTest 310–331 and ConnectionStateMachineTest 122–141 left: the first states its own limit, the second tests the test double | tests in :data, :hardware, :ui | 7 |
| A68-APP-07 | processed | checkpoint 15, texts as approved; mutation M12 caught | ui ConnectionScreen.kt; UserMessageTest | 7 |
| A68-APP-08 | processed (visible text only, checkpoint 16); semantics registered in TODO.md | an unread EQ band and an unread balance show '—'; mutation M13 caught | ui EqScreen.kt; EqScreenTest | 7 |
| A68-APP-09 | narrowed | tile: every failure has a toast (test); the permission flag and the pending export moved to AppUiSession (Activity fields are reset by a rotation — by reading; no test set-up in :app). The pull guard and action-button guards: not changed — the repository serialises each action (connectMutex, eqMutex, the Message Stream claim); registered in TODO.md | app AncTileService.kt, AppUiSession.kt, MainActivity.kt; ui ConnectionScreen.kt | 7 |
| A68-APP-10 | narrowed | four new transport tests failed first (SecurityException and IllegalStateException from write, a RuntimeException from read, a close that throws); fixed; mutation M11 caught. Not tested: the non-atomic channel insert and the foreground-service start/stop window (CAP-068 BD-29); bonded lookups on the main thread — TODO.md | hardware RfcommBudsTransport.kt; RfcommBudsTransportTest | 7 |
| A68-APP-11 | processed | EqBandGains.clampedOrNull; repository refuses before sending; mutation M14 caught | domain EqBandGains.kt; BudsRepositoryImpl.kt; tests | 7 |
| A68-APP-12 | processed (comment); behaviour moved to CAP-068 / CAP-069 | checkpoint 17; CAP-059 2312/2494/2507/2604 | BudsRepositoryImpl.kt (comment); CAP-068 VI, CAP-069 VII | 7 |
| A68-APP-13 | narrowed | (a) the Notify-before-NAK order: a labelled structural test records today's result (Success, the Buds' own mode shown) — by design (a Notify is an answer) and not seen in any capture; not changed. (b)–(f) need a forced interleaving that the current test set-up cannot produce: TODO.md | BudsRepositoryImplTest; TODO.md | 7 |
| A68-APP-14 | processed | six comments corrected; two unused state fields and the DLCI 0x08 label removed | listed files | 7 |
| A68-APP-15 | processed (documented) | 'English only' note in README and ARCHITECTURE; string resources in TODO.md | README.md; ARCHITECTURE.md; TODO.md | 8 |
| A68-APP-16 | processed | checkpoint 18; the all-zero write is on the wire (CAP-015 frame 2111) and is the fixture; mutation M15 caught; the official 'Default' → CAP-069 | domain EqBandGains.kt; tests | 7 |
| A68-APP-17 | registered in TODO.md | checkpoint 19: its own session after 1.0.1 | TODO.md §5 | 5 |
| A68-RE-01 | processed | `cmi.smali:3499` in `:pswitch_e` (`:3102`) = case 5 of `cmi.b()`; `hhn.java:33` (`new cmi(…, 5)`), `key_head_gestures_toggle` (`public.xml:5762`); recorded, not promoted | `REVERSE_ENGINEERING.md` (row 29, Update, Method step 4a, correlation row) | 3 |
| A68-RE-02 | processed | `fut.java:130-131` | `REVERSE_ENGINEERING.md` (retitle note) | 3 |
| A68-RE-03 | processed | lines 123–125, header, Method step 4, UUID register, seven entry-level statements, correlation table (10 rows added) | `REVERSE_ENGINEERING.md` | 3 |
| A68-RE-04 | narrowed | the two retired report files (`find` → nothing) and the paragraph at `:1154` fixed; the citation drift (12 places) and the seven dangling "see above" references are left for the restructure session | `REVERSE_ENGINEERING.md`; `TODO.md` in Phase 5 | 3 |
| A68-RE-05 | registered in TODO.md | restructure deferred (checkpoint 21) | TODO.md §6 | 5 |
| A68-RE-06 | narrowed | decoder: DLCI 2+3, per-direction streams, nine service names, trailing escape guarded; decode_qhr_settings.py and the tool backlog statements registered in TODO.md | scripts/pwrpc_decode.py; TODO.md | 5 |
| L68-1 | moved to CAP-069 | film stills at the four wave times: buds out of frame, trigger not on film | DESKRESEARCH_FINDINGS.md 2026-10-03; CAP-069 skeleton III | 4 |
| L68-2 | moved to CAP-068 / CAP-069 | directions corrected in PROTOCOL.md §4.4 (checkpoint 7); stop-on-bud untested | CAP-068 VI, CAP-069 VII; FIND-005 | 3/4 |
| L68-3 | processed as dated Update (🟡); moved to CAP-069 | CAP-019 2482/2487; SASS page | PROTOCOL.md §4.5.2; CAP-069 II | 3/4 |
| L68-4 | processed as dated Update (🟡); moved to CAP-069 | CAP-020 1183/1935/2038 | PROTOCOL.md §4.5.4; CAP-069 I | 3/4 |
| L68-5 | narrowed | 04 03 = lower bud level (106/118); 04 05 ⇔ Settable does not hold as an equivalence (table) | DESKRESEARCH_FINDINGS.md 2026-10-03; CAP-069 VI | 4 |
| L68-6 | moved to CAP-054 (Group AP redesigned) | no capture has the condition | CAPTURE_BLUETOOTH_HCI_SNOOP.md Group AP; CAP-054 skeleton; BATT-007 | 4 |
| L68-7 | moved to CAP-068 / CAP-069 | — | ANC-006; CAP-068 II, CAP-069 IV | 4 |
| L68-8 | processed (🟡) | 7 samples within 52–548 ms of capture time; absent in CAP-062/067 where no SetWallclock is sent | DESKRESEARCH_FINDINGS.md 2026-10-03 | 4 |
| L68-9 | registered in TODO.md | not re-derived | TODO.md §4 | 5 |
| L68-10 | narrowed | see Phase 4; the rest in TODO.md §4 | TODO.md | 4/5 |
| §6.1 `ARCHITECTURE.md` | narrowed | A68-ARCH-01…05 corrected, timing and permission tables added; the as-built rewrite deferred | ARCHITECTURE.md; TODO.md §6 | 5 |
| §6.2 `DESKRESEARCH_FINDINGS.md` | narrowed | tally corrected, entry of 2026-10-03 added; restructure deferred (checkpoint 21) | DESKRESEARCH_FINDINGS.md; TODO.md | 4/5 |
| §6.3 `AGENTS.md` | narrowed | four edits made in Phase 3; §5/§6 shortening deferred (checkpoint 9) | AGENTS.md; TODO.md §6 | 3/5 |
| §6.4 `CAPTURE_BLUETOOTH_HCI_SNOOP.md` | narrowed | corrections, Groups AP/BD/BE, index rows; restructure deferred (checkpoint 21) | CAPTURE_BLUETOOTH_HCI_SNOOP.md; TODO.md | 4/5 |
| §6.5 `MAINTAINING_DOCS_SITE.md` | processed | — | MAINTAINING_DOCS_SITE.md | 5 |
| §6.6 `REVERSE_ENGINEERING.md` | narrowed | corrections in Phase 3; split deferred | REVERSE_ENGINEERING.md; TODO.md §6 | 3/5 |
| §6.7 `AI_SESSION_LOG_PROCEDURE.md` | processed | closing checklist §4b; Status lint | AI_SESSION_LOG_PROCEDURE.md; scripts/lint_docs.py | 5 |
| §6.8 `PROJECT.md` | processed | Phase 3 | PROJECT.md | 3 |
| §6.9 `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` | narrowed | corrections and new Test-IDs; restructure deferred | TESTPLAN_BLUETOOTH_HCI_SNOOP.md; TODO.md | 4/5 |
| §6.10 `CHANGELOG.md` | narrowed | Known issues, date order; the 'not yet released' refusal is not built: the heading gets its date only after publishing (RELEASING.md §8), so the script cannot require it before the build | CHANGELOG.md | 5 |
| §6.11 `PROJECT_RULES.md` | processed | Phase 3 (checkpoints 10, 11) | PROJECT_RULES.md | 3 |
| §6.12 `TODO.md` | processed | before/after: 12 open checkbox items and the open prose items of the debt section carried or closed (the five capture ids not carried are finished captures) | TODO.md | 5 |
| §6.13 `PROTOCOL.md` | narrowed | corrections as dated Updates; restructure deferred | PROTOCOL.md; TODO.md §6 | 3/5 |
| §6.14 `WORKSTATION_PREPARATIONS.md` | processed | tools checked on this machine (java 21 Temurin, android-34, build-tools 34.0.0, tshark, ffmpeg, git-lfs, adb) | WORKSTATION_PREPARATIONS.md; README.md (factory reset) | 5 |
| §6.15 `APK_REVERSE_ENGINEERING_PROCEDURE.md` | processed | ADR-041; scripts/decode_rawmessageinfo.py exists | APK_REVERSE_ENGINEERING_PROCEDURE.md §3, §6, §7 | 5 |
| §6.16 `CONTRIBUTING.md` | processed | — | CONTRIBUTING.md | 5 |
| §6.17 `RELEASING.md` | processed | — | RELEASING.md | 5 |
| §6.18 `SECURITY.md` | narrowed | supported versions, fingerprint, reporting link now; the fuzz sentence follows the Phase 6 fix | SECURITY.md | 5/8 |
| §7 row 1 | processed (code); the release is the maintainer's step | see A68-APP-01 | — | 6 |
| §7 row 2 | processed (documents) / moved to CAP-068 | see A68-GOV-01, A68-SES-04 | PROJECT.md; RELEASING.md; CAP-068 | 3–5 |
| §7 row 3 | processed | see A68-DEC-01, A68-HK-08 | DECISIONS.md; scripts/lint_docs.py | 3/5 |
| §7 row 4 | processed | see A68-APP-02/03 | — | 6 |
| §7 row 5 | processed | see A68-APP-04/05 | — | 7 |
| §7 row 6 | processed | see A68-PROT-01…04, A68-CAP-02, A68-PROT-10 | PROTOCOL.md; DECISIONS.md | 3 |
| §7 row 7 | processed | see A68-CAP-01, -03, -04, -17, -18, -19 | capture FINDINGS | 4 |
| §7 row 8 | processed | see A68-DEC-03 | RELEASING.md §12; README.md | 5 |
| §7 row 9 | processed | see A68-HK-01 | scripts/release.sh; RELEASING.md §4 | 5 |
| §7 row 10 | narrowed | see A68-APP-06 | — | 7 |
| §7 row 11 | processed | see A68-RE-01 | REVERSE_ENGINEERING.md row 29 | 3 |
| §7 row 12 | processed | see the items named | — | 3/5 |
| §7 row 13 | processed / narrowed | see A68-CAP-05…16, -20…23, A68-CAP-14 | — | 4/5 |
| §7 row 14 | processed | checkpoints 10 and 11 | PROJECT_RULES.md | 3 |
| §7 row 15 | processed / narrowed | see A68-APP-07…15 | — | 7 |
| §7 row 16 | narrowed | TODO.md restructured now; PROTOCOL.md, REVERSE_ENGINEERING.md, the Capture Index deferred (checkpoint 21) | TODO.md §6 | 5 |
| §7 row 17 | processed | see A68-HK-08, A68-HK-05 | scripts/lint_docs.py; .github/workflows/android.yml | 5 |
| §7 Leads to plan | processed | see L68-1…10 | — | 4 |
| §7 Nit | narrowed | see A68-SES-05, A68-RE-06, A68-CAP-16/23 | — | 5 |

## Gate and mutation log

Gate: `cd android && ./gradlew --offline --max-workers=2 assembleDebug testDebugUnitTest test lint`. Counts are the debug unit tests per module
(`:domain` is JVM-only), read from the JUnit XML.

| When | Result | `:data` | `:hardware` | `:ui` | `:domain` |
|---|---|---|---|---|---|
| Phase 0 baseline (HEAD `cd30332`) | green | 1588 | 56 | 35 | 30 |
| Phase 5 (new CI step, run locally: `:app:compileReleaseKotlin :app:processReleaseMainManifest lintRelease`, no signing value) | BUILD SUCCESSFUL | — | — | — | — |
| Phase 6 after `A68-APP-01` | green | 1593 | 56 | 35 | 30 |
| Phase 6 end (`A68-APP-02`, `-03`) | green | 1596 | 56 | 46 | 33 |

Mutation checks, one Gradle run at a time; after each the source was restored (`git status` shows no leftover):

| # | Mutation | Tests that failed |
|---|---|---|
| M1 | `Proto.fields`: back to `next + len > data.size` | `OversizedLengthTest` — the direct reader test (1 of 5); the router test still passed, which shows the guard catching it |
| M2 | `PwRpc.decode`: back to `next + len > bytes.size` | 3 of 5 (direct, router, structured fuzz — the wrapping length loops, which no catch can stop) |
| M3 | router guard removed, together with M1 | 4 of 5 (guard test, direct, router, fuzz) |
| M4 | the ANC mode keeps its first value (the old replay-1 behaviour under a busy collector) | 9 `BudsRepositoryImplTest` tests, among them the new A68-APP-03 test |
| M5 | `sessionSince` not set at Connect | "the ANC mode survives a new Connect but is not current …" |
| M6 | no marking at session end | "Disconnect and a lost session mark the battery values …" |
| M7 | ANC tab: `fromLastConnection` always false | 2 `AncScreenTest` tests |
| M8 | `isCurrent` without `ready` | 2 `ValueCurrencyTest` tests |

| Phase 7 after `A68-APP-04/05` | green | 1596 | 59 | 47 | 34 |
| Phase 7 after the wording, "Flat", "—", NaN | green | 1598 | 59 | 55 | 37 |
| Phase 7 end (`A68-APP-06`, `-10`, `-13`) | green | 1601 | 64 | 58 | 37 |
| Phase 9, after `./gradlew clean` | green | 1601 | 64 | 58 | 37 |

(The four rows above continue the gate table; the release-variant step was run again at the end: BUILD SUCCESSFUL, and none of the four merged
manifests under `app/build/intermediates` contains `android.permission.INTERNET`.)

| # | Mutation | Tests that failed |
|---|---|---|
| M9 | `chooseBondedOrAsk`: two candidates → the first one | `PairingLogicTest`, "two bonded devices … the app picks none" |
| M10 | the name fallback is not switched off after "Use different Buds" | `PairingLogicTest`, "a single named candidate is used as before, unless …" |
| M11 | `send`: a `SecurityException` is rethrown | `RfcommBudsTransportTest`, "a write refused with a SecurityException …" |
| M12 | the connect-failure text names another app again | `UserMessageTest`, "each error type reads as its own sentence" |
| M13 | an unread EQ band shows its number | `EqScreenTest`, "an unread EQ shows a dash …" |
| M14 | `clampedOrNull` accepts a NaN | `BudsRepositoryImplTest`, "a gain that is not a number is refused …" |
| M15 | Flat = `[0, 0, 0, 0, 0.5]` | `BudsRepositoryImplTest`, "the Flat preset writes CAP-015 frame 2111 byte for byte" |
| M16 | a pull in the several-Buds state does nothing | `PullActionTest` |

## Compliance (`AGENTS.md`)

| Rule | Check | Result |
|---|---|---|
| §1 no `INTERNET`, no GMS | `grep` of the added lines under `android/`; the merged manifests of all four variants | none |
| §2 permissions | `grep uses-permission android/*/src/main/AndroidManifest.xml` | unchanged: `BLUETOOTH_CONNECT`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_CONNECTED_DEVICE` |
| §6 no new send, no FACT/ADR without the maintainer | `transport.send(` sites against `HEAD`; `DECISIONS.md`/`PROTOCOL.md` diff | 8 sites, identical; dated Updates only, each citing the chat answer; no new ADR, no promotion |
| §7/§9 identifiers | added lines: MAC-like strings, the device name | only dummy addresses (`AA:00:…`) in `PairingLogicTest`; five existing notes rows that carry the device name were re-written by the table padding, not added |
| §10 dependencies | `git diff` of the Gradle files | `app/build.gradle.kts`: version only; `.gitignore`: `.kotlin/` |
| §11 fixtures | new tests | real bytes with frame numbers (`CAP-015`, `036`, `059`, `061`…`064`); the oversized-length sequences, the hand-ordered ANC sequence and one NAK are labelled supplementary structural tests |
| §12 licence header | the seven new Kotlin files | each carries the AGPL-3.0-or-later header |

## `CAP-068` in one table

| Step | Action | Expected on screen | Expected HCI bracket | Test-ID |
|---|---|---|---|---|
| BD-1…4 | ANC tab: Transparent, Adaptive, Off, Active (buds worn) | the mode with "updated HH:MM:SS" | `08 11` → Notify; `08 12 … <mode>` → ACK → Notify | `ANC-001`…`004` |
| BD-5, BD-6 | the tile four times; once with the buds on the table | the label follows; not worn: a toast with the reason | four `08 12`; Settable `00` ⇒ no `Set` | `ANC-001`…`004`, `INEAR-004` |
| BD-7, BD-8 | a tap on the mode already shown | unchanged | a `Set` or none; its answer | `ANC-006` |
| BD-9…12 | Bluetooth off, export, on, export | "Bluetooth is disabled."; ready again | adapter lines and the loss cause in the export | `PAIR-003` |
| BD-13, BD-14 | Disconnect; Connect | values dimmed, "from the last connection"; current again | session close; announcement, Notify | `PAIR-003`, `BATT-004` |
| BD-15, BD-16 | preset FLAT; the earlier preset | sliders at 0.0 | `WriteSetting 4:{16:{0.0 × 5}}` → OK | `EQS-001` |
| BD-17 | Use different Buds; Pair a device | the picker; paired again | export line; CDM association | `PAIR-003` |
| BD-18, BD-19 | the first second after ready; Info | "—" until read; "App: 1.0.1 …" | `ReadSetting` answers | — |
| BD-20…25 | never-run steps C5, H5, F5, J4, I4, L3 | per `APP_TESTPLAN.md` | per step | `PAIR-003`, `EQS-001`, `FIND-001` |
| BD-26…28 | Ring Left; stop on the bud; Stop | the notice | `04 01 00 01 02`; 🟡 Buds `04 01 00 01 00` | `FIND-001`, `FIND-005` |
| BD-29, BD-30 | Connect with the case closed; then open | the out-of-reach text | a page timeout, no RFCOMM | `PAIR-003` |

## External sources (fetched 2026-10-03 with `curl`, raw text)

- developers.google.com/nearby/fast-pair/specifications/extensions/batterynotification — *"One common use case for this is to use 0b0011 when the case
  has opened and 0b0100 when buds have been removed from the case or it has been closed again."*
- …/extensions/deviceaction — *"Syncing ringing status back to Seekers — Providers may want to notify a Seeker when it changes the ringing status, for
  example if a gesture causes the ringing to stop. … The Provider should follow the same message format as defined in the example above."*
- …/extensions/sass — *"Bit 2: multipoint current state 1, if multipoint is on 0, otherwise"*; *"Bit 4: on-head detection current state 1, if on-head
  detection is turned on"*.
- …/extensions/acknowledgement and …/extensions/messagestream — read for the ACK/NAK layout and the group/code table; no sentence is quoted from them
  in a project file.
- The published release: `gh release view v1.0.0` (the notes with both SHA-256 values and the "Known issue" line; published 2026-10-03T08:30:07Z);
  `gh api repos/…/private-vulnerability-reporting` → `{"enabled":true}`.
- Current tool versions (looked up for `A68-APP-17`, not acted on): Gradle 9.8.0, Android Gradle Plugin 9.4.1, Compose BOM 2026.09.00, Hilt 2.60.1.

## What was read

In full: the prompt; `ai-sessions/0068_AUDIT_RESULT_2026_10_03.md` with its appendix; `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `README.md`,
`RELEASING.md`, `SECURITY.md`, `CONTRIBUTING.md`, `TODO.md` (before the restructure), `scripts/lint_docs.py`, `scripts/pwrpc_decode.py`,
`scripts/release.sh`, `scripts/third_party_notices.py`, the CI workflow; of the app: every file under `data/…/codec`, `BudsRepositoryImpl.kt`,
`PairingLogic.kt`, `BudsCompanionPairing.kt`, `RfcommBudsTransport.kt`, and the `:ui` screens. In the parts an item named: `PROTOCOL.md`,
`DECISIONS.md`, `REVERSE_ENGINEERING.md`, `ARCHITECTURE.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `CHANGELOG.md`,
`APP_TESTPLAN.md`, the procedure documents and each capture's FINDINGS and notes that an item touched. Logs: every capture log an item cites, through
`tshark` and the scripts; films: `CAP-019`, `CAP-020`, `CAP-021` stills. **Not read:** the capture documents no item named; `MainActivity.kt` outside the
parts changed; the decompiled APK beyond the files `A68-RE-01` names.

## Deferred documentation

Every item is in `TODO.md` under the section named:

- §1: the commit decision; building, `CAP-068`, publishing 1.0.1; replacing "maintainer-attested" by the capture; the release-notes line; whether the
  device choice and the "current" rule get an ADR each.
- §2: the hardware verification debt (the `CAP-068` list; the Left bud out on channel 19; `17:7`; B4; K5; the tab swipe; F-4 on a debug build; a second
  device).
- §3: the planned captures (`CAP-068`, `069`, `054`, `053`, `055`, `058`, `030`).
- §4: head gestures and Multipoint as the next features; the three proposals of the desk entry; the open proposals of earlier FINDINGS; the open
  protocol questions; the inventory of L68-9; the UUID register.
- §5: the dependency upgrade session; accessibility semantics; balance precision; string resources; instrumented and `:app` tests; the `A68-APP-13`,
  `-10` and `-09` remainders.
- §6: the deferred restructures (`ARCHITECTURE.md`, `PROTOCOL.md`, `REVERSE_ENGINEERING.md`, the test plan, the Capture Index, the desk-research file,
  `AGENTS.md` §5/§6); the corrections not yet made (slips in ten FINDINGS, folder names, note templates, rule-9a layers, the `CAP-066` count, ADR-040's
  Context, the superseded script, the unread deferred items of 0061/0063, the README media).

## Commits

None. The prompt reserves the commit for the maintainer's answer: at the end of this session the working tree holds 153 modified and 12 new paths
on top of `cd30332`, uncommitted. This section is completed by the session that commits them (`AI_SESSION_LOG_PROCEDURE.md` §4b).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0069_MAINTENANCE_RESULT_2026_10_03.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0069_MAINTENANCE_RESULT_2026_10_03
