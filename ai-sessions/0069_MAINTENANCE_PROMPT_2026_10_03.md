# 0069_MAINTENANCE_PROMPT_2026_10_03.md — Process every finding, lead and document proposal of the 0068 audit into the project files and the app, and prepare CAP-068 for what must be tested first

**Number:** 0069
**Category:** MAINTENANCE
**Date:** 2026-10-03
**Title:** Validate and process all 85 findings (A68-GOV/PROT/CAP/ARCH/DEC/HK/SES/APP/RE), the 10 protocol leads (L68-1…L68-10), the 18 document improvement proposals and every row of the prioritised action list of `ai-sessions/0068_AUDIT_RESULT_2026_10_03.md` — documentation and scripts first, then the OpenControl app — asking the maintainer wherever `AGENTS.md` §6 or `PROJECT_RULES.md` require it, and write the `CAP-068` capture skeleton for everything that must be tested on hardware first

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer, Android/Kotlin engineer and technical auditor. This prompt is for a **fresh** Claude Code
session in this repository. This session **acts on** an audit that is already written; it does not start a new audit. When it finishes, every item of
`ai-sessions/0068_AUDIT_RESULT_2026_10_03.md` — the 85 findings, the 10 leads, the 18 document proposals (its §6) and every row of its "Prioritised
action list" (its §7) — is **either processed or explicitly rejected, with the reason and the evidence recorded**. Nothing may be left silently open.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8).** Before any other action, take the time to read, in this order, to
understand the ground rules before changing anything:

1. `AGENTS.md` — in full.
2. `PROJECT_RULES.md` — in full.
3. `PROJECT.md` — in full.
4. `ARCHITECTURE.md` — in full.
5. `PROTOCOL.md` — in full.
6. `DECISIONS.md` — in full: **every** ADR (ADR-001 … ADR-051) with every dated Update. Note before you start: the heading of ADR-035 is missing
   (finding A68-DEC-01) — its body follows ADR-034's 2026-09-30 Update.
7. `TODO.md` — in full.

Then read `AI_SESSION_LOG_PROCEDURE.md`, `RELEASING.md`, `ai-sessions/INDEX.md`, `ai-sessions/0068_AUDIT_PROMPT_2026_10_03.md` and
**`ai-sessions/0068_AUDIT_RESULT_2026_10_03.md` in full, including Appendix A** (sections 1–8 are the verified report; Appendix A holds the saved
sub-review reports with the commands behind every item marked "sub-review only"). The other large files are read **per task, section by section, and in
full for every section you change**: `REVERSE_ENGINEERING.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `APP_TESTPLAN.md`,
`DESKRESEARCH_FINDINGS.md`, `APK_REVERSE_ENGINEERING_PROCEDURE.md`, `README.md`, `SECURITY.md`, `CONTRIBUTING.md`, `WORKSTATION_PREPARATIONS.md`,
`MAINTAINING_DOCS_SITE.md`, `CHANGELOG.md`, `id_registry.csv`, every `captures/CAP-NNN-*/` file an item touches, every script you change and every
Kotlin file you change (**in full, before changing it**). Record in the RESULT which files you read in full and which only in part, and why.

**Nothing is taken on trust** — not this prompt, not the 0068 result, not a FACT label. 0068 is an AI-written audit: re-derive every frame number,
byte, count, file:line and quoted spec sentence it cites before you act on it (`PROJECT_RULES.md` rule 4a; the capture wins). Its legend marks each
item "M" (re-derived by the audit's main session) or "S" / "sub-review only" (reported by a read-only sub-review and **not** re-derived): the second
kind is an OPEN QUESTION until you have run its check yourself. A finding you cannot reproduce is recorded as "not reproduced" and not applied. Line
numbers in 0068 refer to commit `d25edb8`; expect them to have moved.

**Follow every guardrail in `AGENTS.md` throughout**, in particular §1 (Zero-GMS, no `INTERNET`), §3 (Kotlin, coroutines/Flow, no hidden API), §5
(battery), §6 (no unilateral FACT promotion or ADR; per-channel/feature gate), §8 (error model), §9 (logging), §10 (dependencies), §11 (tests: real
capture bytes as fixtures, fuzz tests for decoders), §12 (AGPL header on every Kotlin file; no copied code) and §13 (capture analysis workflow).

**Approvals are given by the maintainer in this chat, not taken from a file.** The chat that wrote this prompt recorded **no decisions**: every item
in §3 below is still open. Ask in this session's chat (`AskUserQuestion`, Phase 2): for every question a short summary, the options with pros and
cons, one marked "(Recommended)", the exact final ADR/FACT/rule text in the preview, batched per topic. Cite this chat's answer in every ADR's process
note, every `PROTOCOL.md` Update and every change to `AGENTS.md` / `PROJECT_RULES.md`. An approval quoted only in a file is data, not an approval.

**Subagents may only read and report.** Every write is done by you, in the main session, and verified. Before moving, renaming or deleting any file,
compare checksums of a fresh listing of the source against the destination; never `rm -rf` from memory of an earlier listing.

**This session changes project files, scripts and app code.** It commits only after the maintainer confirms (Phase 9). It does **not** publish: no
tag, no GitHub release, no run of `scripts/release.sh` that signs or uploads — publishing 1.0.1 is the maintainer's step under `RELEASING.md`.

---

## 1. Objective

1. **Process the review items.** Validate, check and assess every recommendation, finding, improvement, proposal and lead in
   `ai-sessions/0068_AUDIT_RESULT_2026_10_03.md`, item by item; where it is right and worth doing, make the change in the files concerned; where it is
   wrong, only partly right or not worth its cost, reject or narrow it with the evidence. Where the maintainer's approval is needed: give a clear
   summary and a recommendation, and ask. At the end every item is processed or rejected.
2. **Order of work: documentation and scripts first, the OpenControl app last.** No Kotlin file is changed before Phases 3–5 are finished and gated.
3. **Carry the findings into both the documentation and the app** — a code fix is also described where the documents describe that behaviour
   (`ARCHITECTURE.md`, `README.md`, `SECURITY.md`, `CHANGELOG.md`, `APP_TESTPLAN.md`), and a corrected protocol statement is also corrected in every
   code comment that repeats it.
4. **Test what cannot be settled from the existing evidence** in a new capture skeleton, **`CAP-068`**. Check `id_registry.csv` and the Capture Index
   for the next free capture number and Group letter first (expected: `CAP-068`, Group BD); register it *planned*. Only items that genuinely need
   hardware go in; what an existing log or film answers is answered from that log or film.

---

## 2. Strict execution rules

- **No assumptions.** Verify everything. Re-run the exact `tshark` / `scripts/pwrpc_decode.py` / `grep` / `git` / `gh` (read-only) commands 0068
  gives; where it gives none, run and record your own. Every changed claim links to a frame/capture, a file:line or a spec section.
- **Validate externally** where an item rests on external ground truth, and record URL + the exact quoted sentence in the RESULT: the Fast Pair
  specification pages (batterynotification, deviceaction, acknowledgement, sass, messagestream), the Android Developer / AOSP reference (foreground
  services and `startForegroundService` timing, `CompanionDeviceManager`, `TileService`, version codes), the kotlinx.coroutines reference
  (`MutableSharedFlow`, `MutableStateFlow`), the Bluetooth Core Specification / RFCOMM (DLCI = server channel and direction bit), pigweed.dev
  (pw_rpc, pw_hdlc), and the release pages of the pinned build tools (A68-APP-17). Do not rely on a fetch that came back truncated.
- **No sampling.** Every item is handled. Every file an item touches is read in full before you edit it. A corrected statement is corrected
  **everywhere it appears**: `grep` the whole repository first and fix every occurrence (0068 lists the copies it found; find the rest).
- **Best practice.** Evidence traceability (every claim → a frame/capture/file:line/spec section), structural integrity (headers, section numbers,
  cross-references, table columns vs rows, footers), consistency (one fact, one wording everywhere).
- **Labels and rule 9a.** Every new or changed protocol statement carries 🟢 FACT / 🟡 HYPOTHESIS / ⚪ ASSUMPTION / 🔴 OPEN QUESTION. `CAP-NNN-FINDINGS.md`
  and `-EVENT-NOTES.md` state the current truth and are **rewritten in place**; `PROTOCOL.md` and `DECISIONS.md` get dated, non-destructive Updates.
  **You never promote a finding to 🟢 FACT and never write or change an ADR without the maintainer's answer in this chat** (`AGENTS.md` §6).
- **A negative needs a positive control** (`AGENTS.md` §13 step 8): the command, its exit status, and the same filter matching a frame known to
  exist. Two traps 0068 found: a filter on `data.data` does not see frames a dissector has claimed (HFP, AVDTP — use `frame contains` or the
  protocol's own field), and `bluetooth.addr == <address>` returns nothing on several logs (scope by connection handle instead).
- **DLCI versus channel.** DLCI = 2 × server channel + direction bit. In captures where the Buds opened the multiplexer the same services appear on
  odd DLCIs; where a `.log.last` exists, counts need both logs. Say which logs and which DLCIs every count covers.
- **Tests.** Real capture bytes as fixtures with frame number, time and command in a comment; a hand-built array only as a clearly labelled
  supplementary structural test next to real fixtures (`AGENTS.md` §11). In `BudsRepositoryImplTest`, `advanceUntilIdle()` does not run the
  repository's `backgroundScope` collectors — use `runCurrent()` (the existing `settle()` helper). Every code fix gets a regression test.
- **Build gate.** `cd android && ./gradlew --offline --max-workers=2 assembleDebug testDebugUnitTest test lint` green before the first change and at
  the end of every phase that touches code or a Gradle/CI file (drop `--offline` only if the dependency cache is empty, and say so); exact test counts
  per module from the JUnit XML (0068 baseline: `:data` 1588, `:hardware` 56, `:ui` 35, `:domain` 30, `:app` none; lint "No issues found"); no new
  lint suppression; no new compiler warning. **Mutation checks one at a time** with `--max-workers=2`; after a crash, check for a leftover mutation
  before anything else; restore and verify byte-identical (`sha256sum`/`cmp`).
- **Document gate.** `python3 scripts/lint_docs.py` → exit 0 at the end of every documentation phase (run with `PYTHONDONTWRITEBYTECODE=1`).
- **App behaviour rules that stay binding:** no background, periodic or automatic connecting and no timer loops (ADR-044's visible-only re-open is the
  one exception); DLCI 0x04 is claimed only by a user action (ADR-032); nothing is sent that no ADR unblocks; nothing on the two GSND channels; every
  write through the Safe-Mode gate (ADR-042); no new permission; no new dependency without `AGENTS.md` §10's justification and the maintainer's OK.
- **Scope** (`PROJECT.md`): nothing beyond the items. A new feature (head gestures, Multipoint, an EQ "Default" preset, a device chooser, "use
  different Buds", a Safe-Mode override) is a checkpoint question, never a silent addition; a protocol feature is built only after its `PROTOCOL.md`
  entry is 🟢 and its ADR exists.
- **Privacy:** no MAC addresses, serial numbers, names or street addresses in new text; camera films may carry an address overlay — do not quote it.

---

## 3. Items that need the maintainer (ask in Phase 2 — nothing here is decided yet)

For each: verify first, then ask with your own recommendation. IDs are 0068's.

1. **Hotfix 1.0.1** [A68-APP-01] — fix now and prepare 1.0.1 (the maintainer publishes), and with which reduced hardware test set; version code.
2. **Definition of done, ANC** [A68-GOV-01, A68-SES-04] — keep the tick as "maintainer-attested" with a reworded evidence row and a dated exception
   note in `RELEASING.md`, or untick until `CAP-068` shows an ANC `Set` by the release build; which evidence types the rule accepts from now on.
3. **ADR-035 heading** [A68-DEC-01] — restore the lost heading (a repair, not a new ADR): show the exact diff.
4. **FACT and ADR text corrections** — each with the exact dated Update text in the preview: ADR-022 count [A68-PROT-01]; Multipoint OFF and the SASS
   correction [A68-PROT-02]; head gestures [A68-PROT-03]; Ring directions and the §6 open item [A68-PROT-04]; the ACK tally [A68-PROT-05]; the battery
   notification page wording in `PROTOCOL.md` and ADR-006 [A68-PROT-06]; `CAP-066` "3 of 3" [A68-CAP-02]; ADR-024 Update "22" [A68-CAP-12]; ADR-030
   Context [A68-CAP-09]; ADR-033 Status [A68-DEC-02]; ADR-043 Update "1 s" [A68-ARCH-05]; the component serial and sweep range [A68-PROT-10]; the DLCI
   = channel + direction sentence for `PROTOCOL.md` §2.3 [A68-CAP-22]; the six `PROTOCOL.md` §8 rows "not yet reviewed" and README's sign-off sentence
   [A68-GOV-07].
5. **Project law** — `AGENTS.md` §10 dependency-injection bullet [A68-GOV-02]; the unrecorded approval of §13 step 2 [A68-GOV-04]; the §13 CLI-hygiene
   example and the two filter traps; the `pbtk` wording in §4/§13; shortening §5/§6 (0068 §6.3). `PROJECT_RULES.md`: rule 9a versus the status-banner
   practice [A68-GOV-06]; rule 4a "the quoted command reproduces the quoted result; tools are committed"; rule 20's scope — quoted decompiled lines in
   `REVERSE_ENGINEERING.md` (count them again yourself), screenshots of the official app, Apache-licensed generic icons [A68-GOV-05]. Until rule 20 is
   decided, do not restructure `REVERSE_ENGINEERING.md` and add no new quotation.
6. **Firmware allowlist** [A68-DEC-03] — the "new Buds firmware" runbook and the README sentence; whether Safe Mode gets a user override (default
   recommendation to weigh: no override).
7. **Release-candidate version codes** [A68-HK-01] — a new scheme (1.0.0 is published as 10000) or a script check that forbids the unsafe cases.
8. **App behaviour and UX** — per-session state reset and one staleness rule [A68-APP-02]; device chooser [A68-APP-04]; "use different Buds"
   [A68-APP-05]; the wording of inferred causes [A68-APP-07]; the "not read" presentation [A68-APP-08]; how the app treats the Buds' own ring-status
   message [A68-APP-12, after you have checked what the app does today]; EQ Default [A68-APP-16]; a dependency upgrade [A68-APP-17, after the external
   check]. Show each design before building it.
9. **Document restructures** (0068 §6: `PROTOCOL.md`, `REVERSE_ENGINEERING.md`, `TODO.md`, the Capture Index, `ARCHITECTURE.md`, `CHANGELOG.md`,
   `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`) — these are large. Propose per document: do it in this session, or do the corrections now and register the
   restructure as its own later MAINTENANCE session in `TODO.md`. A restructure never changes a FACT/ADR text and never drops evidence; prove it with
   a before/after check (every frame number, ADR number and capture ID present before is present after).
10. **Session records** — close the Status of 0022, 0023 and 0040 [A68-SES-02] (`AI_SESSION_LOG_PROCEDURE.md` §4a — say what closes each); where the
    1.0.0 publication and its six unrecorded commits are logged [A68-SES-01].
11. **Leads** [L68-1…L68-10] — which become `CAP-068` steps, which a desk analysis of existing logs in this session, which a `TODO.md` item only; and
    whether head gestures and Multipoint are wanted as features (scope, `PROJECT.md`).

Anything 0068 raised that is not listed here is processed under the same rules; if it turns out to need a decision, ask.

---

## 4. Resumability (you must be able to continue automatically after a token or rate limit)

Follow `AI_SESSION_LOG_PROCEDURE.md` §5:

- Create **ai-sessions/0069_MAINTENANCE_RESULT_2026_10_03.md** at the very start, with `**Status:** partial — resumed`, a **Progress** block and a
  **per-item ledger**: one row per 0068 item — 85 findings, L68-1…L68-10, the 18 document proposals (6.1…6.18), the action-list rows — with its ID,
  verdict (processed / narrowed / rejected / not reproduced / awaiting maintainer / moved to `CAP-068` / registered in `TODO.md`), the evidence, the
  files changed and the phase. An item with several parts (e.g. A68-CAP-16, A68-CAP-23, A68-PROT-08, A68-ARCH-04) gets one row **per part**.
- Update the Progress block and the ledger at the end of every phase **and** after every large file or file group: which items are done and tested,
  which files are touched but not yet gated, the last gate and lint results, the checkpoint answers verbatim, where intermediate results live (the
  scratchpad directory — re-create them if it is gone).
- **When resuming:** re-read this prompt, then the RESULT's Progress block and ledger, then `git status` / `git diff`; re-read the files the
  unfinished step touched; continue at the first unfinished item. Never redo a finished, recorded item; never assume an unrecorded one was done;
  never skip one silently. Checkpoint answers already recorded in the RESULT are not asked again.
- Phases run **strictly in sequence**. A phase is finished only when every item assigned to it has a final verdict, or is "awaiting maintainer" with
  the question actually asked.

---

## 5. Phases (strictly sequential)

### Phase 0 — set-up
1. Create the RESULT file (§4). Record `git log -1`, `git status --short` and `git fetch && git log --oneline HEAD..origin/main` (the 0068 pair was
   committed as `29a975d`; the remote was one generated commit ahead — rebase before any push, never force).
2. Baseline gate and `lint_docs.py` (§2) before any change; record the counts. If the gate is not green, stop and report.

### Phase 1 — triage and verification of every 0068 item (read-only)
3. For every item: re-run its evidence, give a verdict, and assign it to a phase (3–7), to the checkpoint, or to `CAP-068`. Run the check of every
   "sub-review only" item (0068 A68-CAP-15/16/23, A68-ARCH-04/05, A68-SES-01/03/04/05, A68-APP-08…15, A68-RE-03…06, and the S-marked parts of other
   items) — these are the least certain. Note where cited line numbers have moved.
4. Collect every question for the maintainer (§3) with draft texts and your recommendation.

### Phase 2 — maintainer checkpoint (one consolidated round in this chat; follow-ups only if needed)
5. A short plain-language summary first (what 0068 found, what you verified, what did not reproduce, what you will change), then the questions
   (`AskUserQuestion`, one per decision). Record the answers verbatim in the RESULT. Apply only what was approved.

### Phase 3 — governance, decisions, protocol, reverse-engineering record (documents)
6. The ADR-035 repair; the approved dated Updates in `DECISIONS.md` and `PROTOCOL.md` (every copy of each corrected number: `TESTPLAN`,
   `id_registry.csv`, `DESKRESEARCH_FINDINGS.md`, `ARCHITECTURE.md`, `CHANGELOG.md`, `TODO.md`, code comments are listed for Phase 6); the approved
   `AGENTS.md` / `PROJECT_RULES.md` changes; `PROTOCOL.md` structure and stale items [A68-PROT-07/08/09]; `REVERSE_ENGINEERING.md` corrections
   [A68-RE-01…04] — for A68-RE-01 resolve which dispatcher case reaches the field-29 write site and record it (labelled; no promotion); `PROJECT.md`'s
   evidence row as approved.

### Phase 4 — captures, test plans, capture plan (documents)
7. Rewrite in place every FINDINGS / EVENT-NOTES 0068 names, after re-deriving each frame [A68-CAP-01…13, A68-CAP-15…23]; status banners versus
   rule 9a as decided; the Capture Index and `id_registry.csv` rows; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the corrected counts, the missing Test-IDs —
   register new IDs in `id_registry.csv`).
8. **Desk analysis of the leads that existing logs can settle** (as decided at the checkpoint): e.g. L68-5 (tabulate every DLCI-8 `04 05` and `04 03`
   against wear state and bud levels over all captures), L68-8 (field 2 against capture time in every capture), L68-3 (the SASS flags byte against the
   Fast Pair SASS page), L68-1 (the `CAP-021` film at the four wave times), A68-APP-12 (what follows the app's Ring in `CAP-059`/`CAP-063`, by
   direction). Results go into the capture's FINDINGS and, as labelled proposals, to the maintainer — not into `PROTOCOL.md` as FACT.
9. **`CAP-068` skeleton** — `captures/CAP-068-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_<letter>/CAP-068-EVENT-NOTES.md` in the layout of
   `CAP-067-EVENT-NOTES.md` (build, metadata, preparation with the lessons of `CAP-063`…`067`, start, steps with expected screen and expected HCI
   bracket, don'ts, collection, analysis checklist with Test-IDs and "Refuted if"). Candidate contents, each only if it still needs hardware after
   step 8: an ANC `Set` from the tab and from the tile by the release build, Bluetooth off/on, and the never-run `APP_TESTPLAN.md` steps [A68-GOV-01,
   A68-SES-04]; the hardware checks of the Phase 7 fixes; Ring stopped on the bud, with and without Play services [L68-2]; case just opened, both
   buds inside, phone not connected [L68-6, within ADR-006's bounds — a system-level HCI log only, the app scans nothing]; head gestures off/on and
   Multipoint off/on with the official app [L68-3/4]; one assistant press-and-hold [L68-1]; a tap on the current ANC mode [L68-7]; the official app's
   EQ "Default" [A68-APP-16]; the forced connect failure of A68-APP-10. Keep the run short: if it exceeds one sitting, propose a split. Every step
   carries its Test-ID; check traceability both ways (`AGENTS.md` §13 step 7). Add the Group section and the §9 row; register the capture *planned*.

### Phase 5 — scripts, CI, release machinery and the remaining documents
10. `scripts/lint_docs.py`: the three new checks [A68-HK-08] — run them against the repository and fix what they find; `scripts/pwrpc_decode.py`
    and the DLCI/channel-aware decoder [A68-CAP-14, A68-RE-06] (a decoder change is verified by reproducing counts already documented, with the
    command); `scripts/release.sh` [A68-HK-01, A68-HK-05] — dry checks only, no signing, no upload; CI release build and `.gitignore` [A68-HK-05].
11. `README.md`, `CHANGELOG.md`, `RELEASING.md` (release log, hotfix path, firmware runbook as approved), `SECURITY.md`, `CONTRIBUTING.md`,
    `WORKSTATION_PREPARATIONS.md`, `APK_REVERSE_ENGINEERING_PROCEDURE.md`, `DESKRESEARCH_FINDINGS.md`, `MAINTAINING_DOCS_SITE.md`,
    `AI_SESSION_LOG_PROCEDURE.md`, `ARCHITECTURE.md` [A68-ARCH-01…05 — the parts that describe today's code; parts that depend on a Phase 7 change
    wait for Phase 8], `TODO.md` [A68-HK-04, 0068 §6.12], the session records [A68-SES-01/02/03], and the approved restructures (§3 item 9).
    Also give `ai-sessions/0068_AUDIT_RESULT_2026_10_03.md` the two closing sections it lacks (`AI_SESSION_LOG_PROCEDURE.md` §9 item 6): "Deferred
    documentation" (none of its own — everything is carried by this session) and "Commits" (`29a975d`).
12. Gate (if a Gradle/CI file changed) and `lint_docs.py`. **Documentation and scripts are now finished; only now does app work start.**

### Phase 6 — the OpenControl app: the critical fix first
13. **A68-APP-01.** Reproduce both failures with a failing test first (the four byte sequences of 0068, labelled synthetic, next to real fixtures);
    fix both length checks without overflow; make the router turn any decoder exception into `BudsError.MalformedFrame` without ending the
    collector; add a structured fuzz test that mutates fields inside CRC-valid frames (length varints at and around every boundary, including the
    maximum) for every decoder behind `CodecRouter`, with a time limit that catches an endless loop. Search every other hand-written reader for the
    same pattern (`next + len`, `toInt()` on an untrusted length) — list each site and its verdict.
14. **A68-APP-03** and the approved part of **A68-APP-02** (state reset, one staleness rule for all tabs), each with tests.
15. Gate. Mutation checks, one at a time, at least: each overflow guard reverted; the router's catch removed; `tryEmit` restored; the reset removed.
    Each must fail ≥ 1 test, then restore and verify byte-identical.

### Phase 7 — the OpenControl app: the remaining items
16. As approved: A68-APP-04/05 (device selection), A68-APP-06 (tests: `:app`, the untested screens, `userMessage()` for all 19 errors, real-byte
    fixtures replacing unlabelled synthetic ones, tests that cannot fail), A68-GOV-03 and A68-APP-07 (texts), A68-APP-08 (unread state and
    accessibility), A68-APP-09/10/11/13 (each hypothesis first proven or refuted by a test that forces the case), A68-APP-12, A68-APP-14/15,
    A68-APP-16/17 (only if approved), and every stale code comment that repeats a statement corrected in Phases 3–4.
17. List every `transport.send(` call site before and after (0068: eight — four gated writes, four ungated reads); any difference is explained.
18. Gate after each item; the final gate after `./gradlew --offline clean`. Compliance: no `INTERNET`, no new permission, no manifest change;
    `git diff --name-only -- '*.gradle.kts' '*.toml' '*AndroidManifest.xml'` empty or only what was approved (with `AGENTS.md` §10's justification);
    AGPL header on new files; nothing sent on a channel or with a command no ADR unblocks.

### Phase 8 — documents that follow the code, and verification
19. `ARCHITECTURE.md`, `README.md`, `SECURITY.md`, `CHANGELOG.md` (`[Unreleased]` / the 1.0.1 block as approved, with known issues),
    `APP_TESTPLAN.md` (new steps for every app change; add them to `CAP-068`), `TODO.md` (every deferred item, every lead not closed, every approved
    later session), `ai-sessions/INDEX.md` (the 0069 row; rows whose Status changed).
20. Re-grep every corrected statement across the repository (nothing left in another file or code comment); re-run every command whose result you
    wrote into a document; `python3 scripts/lint_docs.py` → exit 0; final gate.

### Phase 9 — finish
21. Finish the RESULT: a plain-language summary first (what changed for the maintainer and for the app user), the per-item ledger (every 0068 item
    with its verdict — none open), the items that did not reproduce, the checkpoint answers verbatim, the gate table, the mutation table,
    compliance, the `CAP-068` summary (step · action · expected screen · expected HCI bracket · Test-ID), external sources (URL + quoted sentence),
    what was read in full and in part, and the two closing sections "Deferred documentation" (each item also in `TODO.md`) and "Commits". Status
    per `AI_SESSION_LOG_PROCEDURE.md` §4.
22. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes: Conventional Commits, one commit per concern,
    each building and passing on its own (`PROJECT_RULES.md` rule 16) — e.g. `docs` (decisions/protocol), `docs` (captures/test plans), `chore`
    (scripts/CI), `fix(app)` for A68-APP-01 on its own, further `fix`/`feat`/`test` commits — each with a *why* and ending with the attribution line
    from the session's system reminder; `git fetch` and rebase before pushing, never force; nothing from a build directory, `android/.kotlin/`,
    `dist/`, `.vscode/` or `__pycache__` staged. No tag and no release.

---

## 6. Deliverables

- **ai-sessions/0069_MAINTENANCE_RESULT_2026_10_03.md** — Progress block, per-item ledger (all 85 findings, 10 leads, 18 document proposals and the
  action-list rows closed), checkpoint answers, gate/mutation/compliance tables, external sources, "Deferred documentation", "Commits", footer.
- The changed project files, scripts and app code, and the new `CAP-068` skeleton — each change traceable to a 0068 item and to its evidence.
- Nothing left silently open: every item processed, narrowed, rejected, not reproduced, moved to `CAP-068`, registered in `TODO.md` for a named later
  session, or "awaiting maintainer" with the question asked.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0069_MAINTENANCE_PROMPT_2026_10_03.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0069_MAINTENANCE_PROMPT_2026_10_03
