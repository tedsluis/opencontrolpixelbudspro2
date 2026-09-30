# 0059_MAINTENANCE_PROMPT_2026_09_30.md — Process every finding and lead of the 0058 audit into the project files and the app, within the maintainer's decisions of 2026-09-30, and prepare CAP-065 for what still needs a test

**Number:** 0059
**Category:** MAINTENANCE
**Date:** 2026-09-30
**Title:** Validate and process all 54 findings (A58-GOV/PROT/CAP/ARCH/DEC/HK/SES/APP) and the 5 protocol leads (L-1…L-5) of `ai-sessions/0058_AUDIT_RESULT_2026_09_30.md` — documentation, protocol, decisions, captures, test plans, session records and the Android app — asking the maintainer wherever `AGENTS.md` §6 or `PROJECT_RULES.md` require it, and write the `CAP-065` capture skeleton for everything that must be tested on hardware first (moving suitable steps out of the overlong `CAP-064`)

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer, Android/Kotlin engineer and technical auditor. This prompt is for a **fresh** Claude Code
session in this repository. This session **acts on** an audit that is already written; it does not start a new audit. When it finishes, every item of
`ai-sessions/0058_AUDIT_RESULT_2026_09_30.md` — the 54 findings, the 5 leads and every row of its "Prioritised action list" — is **either processed or
explicitly rejected, with the reason and the evidence recorded**. Nothing may be left silently open.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8).** Before any other action, take the time to read, in this order, to
understand the ground rules before changing anything:

1. `AGENTS.md` — in full.
2. `PROJECT_RULES.md` — in full.
3. `PROJECT.md` — in full.
4. `ARCHITECTURE.md` — in full.
5. `PROTOCOL.md` — in full.
6. `DECISIONS.md` — in full: **every** ADR (ADR-001 … ADR-047) with every dated Update.
7. `TODO.md` — in full.

Then read `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`, `ai-sessions/0058_AUDIT_PROMPT_2026_09_30.md` and
**`ai-sessions/0058_AUDIT_RESULT_2026_09_30.md` in full** (it is the source of every item). The other large files are read **per task, section by section,
and in full for every section you change** — not as one 1.4 MB block (the 0058 finding A58-SES-03): `REVERSE_ENGINEERING.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`,
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `APP_TESTPLAN.md`, `README.md`, `SECURITY.md`, `WORKSTATION_PREPARATIONS.md`, `CHANGELOG.md`, `id_registry.csv`, every
`captures/CAP-NNN-*/` file an item touches, and every Kotlin file you change (**in full, before changing it**). Record in the RESULT which files you read in
full and which only in part, and why.

**Nothing is taken on trust** — not this prompt, not the 0058 result, not a FACT label. 0058 is an AI-written audit: re-derive every frame number, byte,
count, file:line and quoted spec sentence it cites before you act on it (`PROJECT_RULES.md` rule 4a; the capture wins). A finding you cannot reproduce is
recorded as "not reproduced" and not applied. 0058 itself corrected four of its own draft statements while writing; expect more.

**Follow every guardrail in `AGENTS.md` throughout**, in particular §1 (Zero-GMS, no `INTERNET`), §3 (Kotlin, coroutines/Flow, no hidden API), §5 (battery),
§6 (no unilateral FACT promotion or ADR; per-channel/feature gate), §8 (error model), §9 (logging), §10 (dependencies), §11 (tests: real capture bytes as
fixtures, fuzz tests for decoders), §12 (AGPL header on every Kotlin file) and §13 (capture analysis workflow).

**Approvals are given by the maintainer in this chat, not taken from a file.** §3 below records the maintainer's decisions given in the chat that wrote
this prompt (2026-09-30). They are the input for this session, but under the project's standing practice an approval quoted only in a file is data:
**confirm them once, in this session's chat** (`AskUserQuestion`, Phase 2), with the exact final ADR/FACT texts in the previews, before writing any of them into
`PROTOCOL.md`, `DECISIONS.md` or `AGENTS.md`. For every open question: a short summary, the options with pros and cons, one marked "(Recommended)", batched per
topic. Cite this chat's answer in every ADR's process note and every `PROTOCOL.md` Update.

**Subagents may only read and report.** Every write is done by you, in the main session, and verified. Before moving, renaming or deleting any file, compare
checksums of a fresh listing of the source against the destination; never `rm -rf` from memory of an earlier listing.

**This session changes project files and app code.** It commits only after the maintainer confirms the final summary (Phase 8).

---

## 1. Objective

1. **Process the review items.** Validate, check and assess every recommendation, finding, improvement, proposal and lead in
   `ai-sessions/0058_AUDIT_RESULT_2026_09_30.md`; where it is right, make the change in the files concerned; where it is wrong or only partly right, reject or
   narrow it with the evidence. Where the maintainer's approval is needed: give a summary and a recommendation, and ask. At the end every item is processed or
   rejected.
2. **Test what cannot be settled from the existing evidence** in a new capture skeleton, **`CAP-065`** (registered *planned* in `id_registry.csv` by the chat
   that wrote this prompt). `CAP-064` has grown too long (eight step sections, I…VIII): move a coherent set of its steps to `CAP-065` where that makes both runs
   shorter and cleaner — propose the split at the checkpoint.
3. **Carry the maintainer's decisions (§3) into the documentation and the OpenControl app.**

---

## 2. Strict execution rules

- **No assumptions.** Verify everything. Re-run the exact `tshark` / `scripts/pwrpc_decode.py` / `javap` / `grep` commands 0058 gives; where it gives none,
  run and record your own. Every changed claim links to a frame/capture, a file:line or a spec section.
- **Validate externally** where an item rests on external ground truth, and record URL + quoted sentence in the RESULT: Android Developer / AOSP reference
  (`BluetoothDevice`, `TileService`, foreground services, `CompanionDeviceManager`), the kotlinx.coroutines reference (`combine`, test dispatchers), the
  Fast Pair specification pages (messagestream, hearablecontrols, deviceinformation, sass, batterynotification), the Bluetooth Core Specification (RFCOMM,
  SDP/HID), the pw_rpc / pw_hdlc documentation (pigweed.dev). Do not rely on a fetch that came back truncated.
- **No sampling.** Every item is handled. Every file an item touches is read in full before you edit it. Consistency fixes are applied everywhere the same
  statement appears: `grep` the whole repository first and fix every occurrence (the 0058 finding classes are systemic — A58-CAP-04, A58-DEC-01).
- **Best practice.** Evidence traceability (every claim → a frame/capture/file:line/spec section), structural integrity (headers, section numbers,
  cross-references, table columns vs rows, footers), consistency (one fact, one wording everywhere).
- **Labels and rule 9a.** Every new or changed protocol statement carries 🟢 FACT / 🟡 HYPOTHESIS / ⚪ ASSUMPTION / 🔴 OPEN QUESTION (`PROJECT_RULES.md` §1).
  `CAP-NNN-FINDINGS.md` / `-EVENT-NOTES.md` files state the current truth and are **rewritten in place** (fold dated addenda, A58-CAP-05); `PROTOCOL.md` and
  `DECISIONS.md` get dated, non-destructive Updates.
- **A negative needs a positive control** (the rule the maintainer adopted, §3 item 4): a "0 frames" result is written down only with the command, its exit
  status, and the same filter shown matching a frame known to exist.
- **Tests.** Real capture bytes as fixtures with frame number, time and command in a comment; a hand-built array only as a labelled supplementary structural
  test next to real fixtures (`AGENTS.md` §11). In `BudsRepositoryImplTest`, `advanceUntilIdle()` does not run the repository's `backgroundScope` collectors —
  use `runCurrent()` (the existing `settle()` helper). Every code fix gets a regression test.
- **Build gate.** `cd android && ./gradlew --offline --max-workers=2 assembleDebug testDebugUnitTest test lint` green at the end of every phase that touches
  code (drop `--offline` only if the dependency cache is empty, and say so); exact test counts per module from the JUnit XML; no new lint suppression; no new
  compiler warning. **Mutation checks one at a time** with `--max-workers=2` (earlier sessions were OOM-killed running them in parallel); after a crash, check
  for a leftover mutation before anything else; restore and verify byte-identical (`sha256sum`/`cmp`).
- **App behaviour rules that stay binding:** no background, periodic or automatic connecting and no timer loops (`ARCHITECTURE.md` §6; ADR-044's visible-only
  re-open is the one exception); DLCI 0x04 is claimed only by a user action (ADR-032); nothing is sent that no ADR unblocks; nothing on DLCI 0x08; every write
  through `writeGate` (ADR-042); no new permission; no new dependency without `AGENTS.md` §10's justification and the maintainer's OK.
- **Scope** (`PROJECT.md`): nothing beyond the items; a new feature (e.g. a Dosimeter display, lead L-4) is a checkpoint question, never a silent addition.
- **Privacy:** no MAC addresses, names or street addresses in new text; camera films may carry an address overlay — do not quote it.

---

## 3. The maintainer's decisions (chat, 2026-09-30 — confirm once in Phase 2, then apply)

Numbers follow 0058's "Prioritised action list"; finding IDs in brackets.

1. **ViewModel vs as-built state hoisting** [GOV-01, ARCH-01] — **option C:** record the as-built design (no ViewModel; `MainActivity` collects the
   `BudsRepository` flows and passes `OpenControlUiState`/`OpenControlActions` to a stateless `:ui`; no use-case classes; user actions in `applicationScope`)
   in a **new ADR that supersedes only ADR-001's "MVVM in the UI layer" clause**, and move the three Activity-local pieces of A58-APP-08 out of the Activity
   (to the repository or an application-scoped holder): `pairingStateFlow`, the unidentified-frames list, and the bonding observation that now runs in
   `rememberCoroutineScope()`. Contents of the ADR (0058 GOV-01 draft, extended in chat): Status/process note; Context (0033 Phase 6; singleton repository
   survives rotation; tile and service read the repository directly; `BudsUiState` never became a type — `OpenControlUiState` fills the role); Decision (as
   above, plus "state that must survive a configuration change does not live in the Activity"); Consequences with the exact text changes to `AGENTS.md` §8 and
   §11, `ARCHITECTURE.md` §2 (module table L84–85, L99, L187, the "MVVM" heading) and §7 (L609–610), `README.md` Approach 4; "Reconsider when" (the UI mapping
   cannot be tested without a ViewModel, or a second screen/Activity needs the same state). Register the next free ADR number in `id_registry.csv`.
   `AGENTS.md` is project law: show the exact new §8/§11 sentences in the preview.
2. **`ACTION_BATTERY_LEVEL_CHANGED`** [GOV-02] — remove it from `AGENTS.md` §5's priority list and mark `PROTOCOL.md` §4.3 Option 0 as **not public API**
   (evidence: `javap -constants -cp <android-34 android.jar> android.bluetooth.BluetoothDevice | grep -i battery` → nothing, while public constants such as
   `ACTION_ACL_CONNECTED` are listed; `DESKRESEARCH_FINDINGS.md` 2026-09-19 Finding 3); align `ARCHITECTURE.md` §6/§15 and ADR-029 (dated Update). Re-run
   the `javap` yourself and try the official reference page once more.
3. **ADR-024** [PROT-01, CAP-09] — supersede its "Settable-toggles byte = dock state" 🟢 with the 🟡 "`0x00` = no bud worn" reading (0058 PROT-01 draft) via a
   new ADR (or a superseding Status + Update, whichever `DECISIONS.md`'s preamble requires — decide and say why); correct `PROTOCOL.md` §4.1's heading and
   every code comment that still says "dock state" (`AncFrame.kt:86`, …). Add to **`CAP-065`** the three `e8`-while-not-worn samples to re-check on film:
   `CAP-042` frame 602, `CAP-045` frame 612, `CAP-048` frame 11939 (re-derive their bytes and the notes' wear state first).
4. **False FACT and false negatives in the captures** [CAP-01, CAP-02, HK-04] — correct the false 🟢 in `CAP-002-FINDINGS.md` L96–100 (the "different,
   unattributed device" on handle 0x0001 is the Buds: Connect Complete 17233, Model ID in 17610); rewrite the false "clean negatives" with the corrected
   command and result — `CAP-029` AVRCP (the documented filter `avctp or avrcp` errors; `btavctp or btavrcp` → 26 frames, pause at 1593), `CAP-044` "zero
   DISC" (frame 4795), `CAP-047` "Option E never fires", `CAP-023` "Revision 6 absent" (frame 730), `CAP-019` Group 0x07 / Code 0x34; **re-derive `CONV-002`**'s
   verdict in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (a status change is the maintainer's — ask); add the **"negative needs a positive control"** rule to the
   capture-analysis procedure (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §6 or the analysis checklist, and `AGENTS.md` §13 if the maintainer agrees — ask).
5. **App fixes** [APP-01, APP-03, APP-05, APP-04, APP-02] — (a) the ANC tile's `combine` gets `repository.ancMode.onStart<AncMode?> { emit(null) }` (as
   `OpenControlApplication.kt:60`) plus a test of the tile's state mapping; (b) an answered but undecodable `ReadSetting` gets its own error instead of a 2 s
   `Timeout` (and is logged under Debug mode), with a labelled supplementary test built from a real answer; (c) `ConnectionStateMachine` transitions become
   atomic (`_state.update { … }` / `compareAndSet`); (d) a write-timeout quarantine for late `WriteSetting` results (design it, show it at the checkpoint),
   documented in ADR-045's Consequences (dated Update — ask); (e) **EQ sliders while the EQ is unread: still to decide** — ask at the checkpoint (disable the
   sliders until read and keep the presets, vs keep the `ai-sessions/0039` behaviour and name the exception in `ARCHITECTURE.md` L170). Also A58-APP-06
   (item 11) and A58-APP-07/08 (items 6/12).
6. **One documentation sweep** [CAP-03…06, ARCH-02…05, DEC-01…04, HK-01…06, APP-07/08, PROT-03…08] — capture status banners (write a script that lists every
   🟡/🔴 line in `captures/` whose subject has a 🟢 row in `PROTOCOL.md`; review its output, do not apply it blindly), every frame/hex/count correction of
   A58-CAP-03, rule-9a folding (A58-CAP-05), dangling references and missing files (A58-CAP-06), `ARCHITECTURE.md` §5a/§6.1/§7, ADR pointer Updates (ADR-031,
   ADR-040, ADR-044, ADR-030, and the DEC-04 set), `README.md` "Current state", `TODO.md`, `WORKSTATION_PREPARATIONS.md` (factory reset = `CAP-029` §3),
   `SECURITY.md` (the gate covers every write; Model ID per channel), stale code comments and settled `TODO(verify)` markers.
7. **Test plans and capture plan** [CAP-07, CAP-08, SES-04] — `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` back-links for `CAP-056`, `CAP-062`, `CAP-063` and every later
   Group (Group column and evidence cells); fix `CAP-062`'s FIND-001/002 tags and `CAP-002`'s CASE-001 tag (and the Index row); **add Group AZ (`CAP-064`) to
   `CAPTURE_BLUETOOTH_HCI_SNOOP.md`** (Group section + §9 row) — or, if you find a reason not to, record it and ask; add Groups AU/AV sections; withdraw or
   re-scope `CAP-052` (its question is answered by Option F — ask); correct `CAP-054`'s premise and `PROTOCOL.md` L868 (`CAP-043` is connection-free — a status
   sentence, ask); `id_registry.csv` CAP-056 → `analyzed`.
8. **Protocol leads** [L-1…L-5, PROT-07] — name the "unnamed" services in `PROTOCOL.md` (hash + APK literal + wire, L-4) and relabel "1779298694" as the
   `UpdateHelperService.GetRunningVersion` answer (L-5; `PROTOCOL.md` L301, L2427, `CAP-001-FINDINGS.md` L115) — each a FACT/label change: show the exact text
   and ask; **ask whether the Dosimeter stream is in scope** (a `PROJECT.md` scope question; if yes, only a plan and a CAP-065 experiment, no code); plan the
   **L-1 one-bud capture** and the **L-2 HID report-descriptor check** — first try L-2 on the existing `CAP-033` SDP frames 1274/1355 (attribute 0x0206) before
   planning a capture; put what still needs hardware into `CAP-065`. PROT-07 (`REVERSE_ENGINEERING.md` `gbm` lead refuted by `CAP-033` SDP) in the same pass.
9. **Process** [SES-01…05] — a prompt template (propose where it lives, e.g. a section in `AI_SESSION_LOG_PROCEDURE.md`) with the §8 reading block and per-task
   reading sections instead of "read 1.4 MB in full"; every RESULT lists deferred documentation explicitly (and `TODO.md` carries it); set `0033`'s RESULT and
   INDEX row to `complete` (citing ADR-045/046/047, `AI_SESSION_LOG_PROCEDURE.md` §4a — ask); drop the "Status: prompt only — not yet run" lines from the
   PROMPT files (0031, 0033–0040, 0043, 0044, 0045, 0049, 0058) — PROMPTs carry no Status field (§4); set `ai-sessions/INDEX.md`'s 0058 row to `complete`.
10. **Governance housekeeping** [GOV-03…07] — LFS for the five `.log.last` files (`CAP-040/042/047/048/049`): migrating the *current* files is fine; **a
    history rewrite is the maintainer's call — ask, and never force-push without an explicit yes**; ADR-009's sign-off provenance (ask the maintainer where it
    was given; record "not recorded" if unknown — never invent one); `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 step 4 "raise confidence to 🟢" (reword per
    `AGENTS.md` §6); `REVERSE_ENGINEERING.md`'s rewrite-in-place claim vs its 64 dated addenda (fold or reword — propose); the ~64 open "PROPOSAL — pending
    maintainer approval" markers: list them all, group them, and ask per group (approve / reject / still open).
11. **`RfcommBudsTransportTest`** [APP-06] — make it deterministic (virtual time / an injected dispatcher) or find why a run can block 10 s; report which.
12. **Tooling and small nits** [GOV-08/09, SES-05, APP-08, DEC-04, HK-06, PROT-08] — including the `ensure_footers.py` docstring, the unpinned actions in
    `.github/workflows/lint-docs.yml`, the CI manifest check that inspects one variant only, the duplicate footer in the 0057 RESULT, the stale "waiting for commit" lines of the
    0054–0057 RESULTs.

Anything not listed here that 0058 raised is processed under the same rules.

---

## 4. Resumability (you must be able to continue automatically after a token or rate limit)

Follow `AI_SESSION_LOG_PROCEDURE.md` §5:

- Create **ai-sessions/0059_MAINTENANCE_RESULT_2026_09_30.md** at the very start, with `**Status:** partial — resumed`, a **Progress** block and a
  **per-item ledger**: one row per 0058 item (54 findings, L-1…L-5, the 12 action rows) with its ID, verdict (processed / narrowed / rejected / not reproduced /
  awaiting maintainer), the evidence, the files changed and the phase.
- Update the Progress block and the ledger at the end of every phase **and** after every large file or file group (which items are done and tested, which
  files are touched but not yet tested, the last gate result, where intermediate results live — the scratchpad directory; re-create them if the scratchpad is
  gone).
- **When resuming:** re-read this prompt, then the RESULT's Progress block and ledger, then `git status` / `git diff`; re-read the files the unfinished step
  touched; continue at the first unfinished item. Never redo a finished, recorded item; never assume an unrecorded one was done; never skip one silently.
- Phases run **strictly in sequence**. A phase is finished only when every item assigned to it has a final verdict, or is "awaiting maintainer" with the
  question actually asked.

---

## 5. Phases (strictly sequential)

### Phase 0 — set-up
1. Create the RESULT file (§4). Record `git log -1`, `git status --short` and `git fetch && git log --oneline HEAD..origin/main`. The 0058 RESULT may still
   be **untracked**: if so, ask whether to commit it first on its own (`docs: record ai-sessions/0058 audit result`), before any change of this session.
2. Baseline gate (§2) before any change; record the per-module test counts, lint and compiler warnings (0058 baseline: `:data` 1567, `:hardware` 51,
   `:domain` 25, `:ui` 9 per variant; lint 1 warning; 3 Kotlin warnings). If it is not green, stop and report.

### Phase 1 — triage and verification of every 0058 item (read-only)
3. For every item: re-run its evidence, give a verdict, and assign it to a phase (3–7) or to the checkpoint. Note where 0058's cited line numbers have moved.
4. Collect every question for the maintainer (§3's confirmations, 5(e), the ask-items of 3/4/7/8/9/10, the CAP-064/CAP-065 split) with draft texts.

### Phase 2 — maintainer checkpoint (one consolidated round in this chat; follow-ups only if needed)
5. Short plain-language summary first (what 0058 found, what you verified, what you will change), then the questions (`AskUserQuestion`, one per decision, pros
   and cons, one "(Recommended)", exact texts in the previews). Record the answers verbatim in the RESULT. Apply only what was approved.

### Phase 3 — governance, decisions, protocol (items 1, 2, 3, 8, 10)
6. The ADRs and dated Updates as approved (register new numbers in `id_registry.csv`); `AGENTS.md` §5/§8/§11 as approved; `PROTOCOL.md` status changes as
   approved; `REVERSE_ENGINEERING.md`, the PROPOSAL markers, ADR-009, the LFS migration.

### Phase 4 — captures, test plans, capture plan (items 4, 6-capture part, 7)
7. The capture corrections (A58-CAP-01…09), the banner script and its reviewed output, rule-9a folding, the positive-control rule, `TESTPLAN` back-links and tags,
   Group AZ/AU/AV sections, `CAP-052`/`CAP-054`, `id_registry.csv`.
8. **`CAP-065` skeleton** — `captures/CAP-065-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_<next free letter after AZ, check §4 of CAPTURE_BLUETOOTH_HCI_SNOOP.md>/CAP-065-EVENT-NOTES.md`,
   in the layout of `CAP-064-EVENT-NOTES.md` (A.0 build, A.1 metadata, A.2 preparation with the `CAP-063`/`CAP-064` lessons — minute change filmed at start and
   end, commit hash said aloud, Do Not Disturb, ears visible for wear steps, an events file, the empty audio track — A.3 start, A.4 steps with expected screen
   and expected HCI bracket, A.5 don'ts, A.6 collection, A.7 analysis checklist with Test-IDs and "Refuted if"). Contents: the three Settable samples (item 3), the
   app fixes of item 5 on hardware, what remains of L-1/L-2/L-4 (item 8), and the `CAP-064` steps the maintainer agreed to move. Update `CAP-064`, both registry
   rows and the capture plan (Group section + §9 row) accordingly. Every step carries its Test-ID; check the traceability both ways (`AGENTS.md` §13 step 7).

### Phase 5 — the OpenControl app (items 1, 5, 11, 12-code part)
9. Build per the checkpoint, each item with its tests: APP-08's three pieces out of the Activity (item 1); APP-01, APP-03, APP-05, APP-04 and 5(e) (item 5);
   APP-06 (item 11); APP-07/08 comments and nits. List every `transport.send(` call site before and after (0058: 8) — expected: unchanged.
10. Gate after each item; the final gate after `./gradlew --offline clean`. Mutation checks, one at a time, at least: the tile without `onStart` (must fail
    the new test); the undecodable read back to `Timeout`; a non-atomic transition (if testable); the quarantine removed; the 5(e) choice reversed. Each must
    fail ≥ 1 test, then restore and verify byte-identical.
11. Compliance: no `INTERNET`, no new permission, no manifest change; `git diff --name-only -- '*.gradle.kts' '*.toml' '*AndroidManifest.xml'` empty (or only
    what the maintainer approved, with the §10 justification and `./gradlew app:dependencies` checked); AGPL headers on new files; nothing on DLCI 0x08.

### Phase 6 — documentation sweep and process (items 6, 9, 12)
12. `ARCHITECTURE.md`, `README.md`, `TODO.md`, `WORKSTATION_PREPARATIONS.md`, `SECURITY.md`, `CHANGELOG.md`, `APP_TESTPLAN.md` (new steps for the app fixes;
    L1's "Debug tab"), the prompt template, the session-record fixes, `ai-sessions/INDEX.md` (0058 and 0033 rows, the 0059 row).

### Phase 7 — verification
13. Re-grep every corrected statement across the repository (nothing left in another file); re-run every command whose result you changed in a document;
    `python3 scripts/ensure_footers.py` and `python3 scripts/lint_docs.py` → exit 0.

### Phase 8 — finish
14. Finish the RESULT: a plain-language summary first (what changed for the maintainer and the app user), the per-item ledger (every 0058 item with its
    verdict), the checkpoint answers verbatim, the gate table, the mutation table, compliance, the `CAP-065` summary (step · action · expected screen · expected
    HCI bracket) and the `CAP-064` split, external sources (URL + quoted sentence), deferred items. Status per `AI_SESSION_LOG_PROCEDURE.md` §4.
15. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes: Conventional Commits, one commit per concern, each built and
    tested on its own (`PROJECT_RULES.md` rule 16) — e.g. `docs` (decisions/protocol), `docs` (captures/test plans), `fix(app)` / `refactor(app)` / `test`,
    `chore` (LFS) — each with a *why* and ending with the attribution line from the session's system reminder; `git fetch` and `git log origin/main..HEAD`
    before pushing (CI adds sitemap/sidebar commits — rebase, never force unless the maintainer explicitly approved the LFS history rewrite); nothing from a
    build directory, `android/.kotlin/`, `.vscode/` or `__pycache__` staged.

---

## 6. Deliverables

- **ai-sessions/0059_MAINTENANCE_RESULT_2026_09_30.md** — Progress block, per-item ledger (all 54 findings, 5 leads, 12 action rows closed), checkpoint answers,
  gate/mutation/compliance tables, external sources, footer.
- The changed project files and app code, and the new `CAP-065` skeleton — each change traceable to a 0058 item and to its evidence.
- Nothing left silently open: every item processed, narrowed, rejected or "awaiting maintainer" with the question asked.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0059_MAINTENANCE_PROMPT_2026_09_30.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0059_MAINTENANCE_PROMPT_2026_09_30
