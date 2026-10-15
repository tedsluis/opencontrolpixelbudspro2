# 0094_MAINTENANCE_PROMPT_2026_10_15.md — Process every finding, lead, document proposal and decision of the 0093 audit into the project files, the scripts and the app

**Number:** 0094
**Category:** MAINTENANCE
**Date:** 2026-10-15
**Title:** Validate and process all 69 findings (A93-GOV/PROT/CAP/ARCH/DEC/HK/SES/APP/RE), the 5 protocol leads (L93-1 … L93-5), the 19 document proposals (§6),
the 10 decisions (D1 … D10, §7), the 12 action rows (§8) and the grouped sub-review items (Appendix B) of `ai-sessions/0093_AUDIT_RESULT_2026_10_15.md` —
documentation and scripts first, the OpenControl app last — with the maintainer's approval in chat where `AGENTS.md` §6 requires it, and a capture skeleton for
what only hardware can settle.

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this repository. Your
task is to carry out the improvements that `ai-sessions/0093_AUDIT_RESULT_2026_10_15.md` reports. Run the phases of §5 **strictly in order**; do not start a
phase before the previous one is finished and recorded.

**Number check first.** `0094` is the next free number after `0093` (`ai-sessions/INDEX.md`, checked 2026-10-15). If another session took it, use the next free
number and rename this file and its RESULT (`AI_SESSION_LOG_PROCEDURE.md` §3).

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8).** Before any other action, take the time to read, **in full** and in this order:
`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md`, `DECISIONS.md` (every ADR, ADR-001 … ADR-065 with every dated Update, and the draft
ADR-060), `TODO.md`. Then `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`, `id_registry.csv`, `RELEASING.md`, `CHANGELOG.md`, `README.md`,
`APP_TESTPLAN.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (§4, the Group sections, §9), `REVERSE_ENGINEERING.md`,
`DESKRESEARCH_FINDINGS.md`, and then **`ai-sessions/0093_AUDIT_RESULT_2026_10_15.md` in full, including Appendix A (the main session's working notes) and
Appendix B (the six sub-review reports)**. Read the prompt of 0093 (`ai-sessions/0093_AUDIT_PROMPT_2026_10_15.md`) for its scope. These are large files
(`PROTOCOL.md` ≈ 3,800 lines, `REVERSE_ENGINEERING.md` ≈ 4,600, `DECISIONS.md` ≈ 2,900, the 0093 RESULT ≈ 1,900): read in chunks of ≈ 400 lines (the Read tool
truncates around 25,000 tokens; table-dense files need smaller chunks). Say in the RESULT what you read in full and what in part.

**Nothing is taken on trust** — not this prompt, not the 0093 result, not a FACT label, not a sub-review. 0093 is an AI-written audit. Its §4 marks each finding
M (verified by its main session) or S (found by a sub-review, re-checked on its quoted lines or command); Appendix B's raw items were **not** all re-checked.
Re-derive every frame, count, line number and quote before you act on it; line numbers in 0093 are as of commit `b9828ba` and may have moved.

**Follow every guardrail in `AGENTS.md` throughout**, in particular §1 (Zero-GMS, no `INTERNET`), §3 (Kotlin, coroutines/Flow, no hidden APIs or reflection),
§6 (no promotion to 🟢 FACT and no ADR or ADR Update without the maintainer's answer in this chat), §7 (no scanning; `CompanionDeviceManager` only), §8 (the
sealed error model), §9 (no MAC at INFO or above; payload only in debug mode), §11 (real capture bytes as fixtures), §12 (AGPL header; no code copied from the
official app or from `qzed/pbpctrl`), §13 (traceability, zero creativity on hex, a negative needs a positive control).

**Approvals are given by the maintainer in this chat, not taken from a file.** The chat that wrote this prompt recorded **no decisions** — 0093's §7 holds
advice only. Ask once, in one consolidated checkpoint (Phase 2), with your own recommendation per question; record the answers verbatim in the RESULT. Do not
ask again what is recorded there.

**Subagents may only read and report.** Every write is done by you, in the main session, and verified. A subagent writes its findings progressively to a scratch
file outside the repository (it may be cut off by a usage limit). Never two Gradle builds at once; never two `tshark` jobs at once; this laptop has 15 GB.
Before moving, renaming or deleting any file, list it fresh and diff the listing; never use wildcard deletes; ask before deleting anything not on an approved
list.

**This session changes project files, scripts and app code.** It commits only after the maintainer confirms (Phase 9). `main` takes changes **only by pull
request** (`RELEASING.md`, "Repository rules"): work on a branch. It does **not** merge, tag, run `scripts/release.sh` or publish anything.

---

## 1. Objective

1. **Process the review items.** Validate, check and assess every recommendation, new finding, improvement, proposal, decision and lead in
   `ai-sessions/0093_AUDIT_RESULT_2026_10_15.md`, item by item. Where it is right and worth doing, make the change in the files concerned; where it is wrong,
   only partly right, already fixed or not worth its cost, reject or narrow it **with the evidence**. Where the maintainer's approval is needed, give a clear
   summary and a recommendation, and ask. **At the end every item is either processed or rejected** — none is left silently open.
2. **Order of work: documentation and scripts first, the OpenControl app last.** No Kotlin file is changed before Phases 3–5 are finished and gated.
3. **Carry the findings into both the documentation and the app.** A code fix is also described wherever the documents describe that behaviour
   (`ARCHITECTURE.md`, `README.md`, `CHANGELOG.md`, `APP_TESTPLAN.md`, `SECURITY.md`); a corrected protocol statement is also corrected in every document, test name
   and code comment that repeats it (`grep` the whole repository).
4. **Test what existing evidence cannot settle** in a new capture skeleton (§5 Phase 4). Use the next free capture number and Group letter from `id_registry.csv`
   and the Capture Index (at the time of writing the highest are `CAP-076` and Group BL); register it *planned*. Only items that genuinely need hardware go in.

---

## 2. Strict execution rules

- **No assumptions.** Verify everything. Re-run the exact `tshark` / `python3 scripts/pwrpc_decode.py` / `python3 scripts/anc_setting13_crosscheck.py` / `grep` /
  `git` / `gh` (read-only) commands 0093 gives; where it gives none, run and record your own. Every changed claim links to a frame and capture, a file:line or a
  specification section.
- **Validate externally** where an item rests on external ground truth, and record the URL and the exact quoted sentence in the RESULT: the Fast Pair
  specification (the SASS extension page for A93-PROT-03/04; the Message Stream and Device Information pages), the **Bluetooth Core Specification** (the HCI Link
  Key Notification key types for A93-CAP-05 — P-192 versus P-256), the Android Developer / AOSP reference (`CompanionDeviceManager`, `TileService`, accessibility
  semantics in Compose for A93-APP-07, target-SDK behaviour changes for D7), the kotlinx.coroutines reference (`MutableSharedFlow`, `CoroutineStart.UNDISPATCHED`,
  dispatchers — A93-APP-03/04), GitHub Docs on `GITHUB_TOKEN` permissions (A93-APP-09), the Claude Code setup page and the vendors' install guidance
  (A93-GOV-10). Do not rely on a fetch that came back truncated or summarised; say so if a page could not be read in full.
- **No sampling.** Every item is handled, including every grouped item of Appendix B that a §4 finding points to (A93-CAP-07/08/09, A93-SES-06/07, A93-APP-08).
  Every file an item touches is read in full before you edit it. A corrected statement is corrected **everywhere it appears**: `grep` the whole repository first
  (0093 lists the copies it found — e.g. "thirteen requests" in six documents, "12 of 77" in five places — find the rest).
- **Best practice.** Evidence traceability (every claim → a frame/capture/file:line/spec section); structural integrity (headers, section numbers,
  cross-references, table columns vs rows, footers); consistency (one fact, one wording, everywhere).
- **Labels and rule 9a.** Every new or changed protocol statement carries 🟢 FACT / 🟡 HYPOTHESIS / ⚪ ASSUMPTION / 🔴 OPEN QUESTION. `CAP-NNN-FINDINGS.md` and
  `-EVENT-NOTES.md` state the current truth and are **rewritten in place**; `PROTOCOL.md`, `DECISIONS.md` and `REVERSE_ENGINEERING.md` get dated, non-destructive
  Updates or Corrections. `ARCHITECTURE.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` and the other guides are not under rule 9a's keep-history scope: rewrite them in place.
  **You never promote a finding to 🟢 FACT and never write or change an ADR without the maintainer's answer in this chat** (`AGENTS.md` §6).
- **A negative needs a positive control** (`AGENTS.md` §13 step 8): the command, its exit status, and the same filter matching a frame known to exist. Scope every
  filter to the Buds' connection handle first (`bluetooth.addr` matches nothing on several logs); a filter on `data.data` does not see frames a dissector claimed
  (HFP, AVDTP) — use `frame contains` or the protocol's own field.
- **Tests.** Real capture bytes as fixtures with file, frame number and command in a comment; a hand-built array only as a clearly labelled supplementary
  structural test next to real fixtures (`AGENTS.md` §11). In `BudsRepositoryImplTest`, `advanceUntilIdle()` does not run the repository's `backgroundScope`
  collectors — use `runCurrent()` (the existing `settle()` helper). Every code fix gets a regression test that fails without the fix.
- **Build gate.** `cd android && ./gradlew --offline --max-workers=2 assembleDebug testDebugUnitTest test lint` — green before the first change and at the end
  of every phase that touches code or a Gradle/CI file (drop `--offline` only if the dependency cache is empty, and say so). Record exact test counts per module
  from the JUnit XML. 0093's baseline (`b9828ba`): `:data` 1672 per variant, `:hardware` 64 per variant, `:domain` 42, `:ui` 92 per variant, `:app` none, 0
  failures; lint `:app` 6 warnings (3 `AndroidGradlePluginVersion`, 2 `NewerVersionAvailable`, 1 `OldTargetApi`), `:ui` 1 (`ModifierParameter`). No new lint
  suppression; no new compiler warning. **Mutation checks one at a time** with `--max-workers=2`; after a crash, check for a leftover mutation before anything
  else; restore and verify byte-identical (`sha256sum`/`cmp`).
- **Document gate.** `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py` → exit 0 at the end of every documentation phase. The lint rejects an ID that is
  not in `id_registry.csv` and a backticked file name that does not exist — register a new ID before you cite it, and write scratch file names without backticks.
  If a script rewrites `scripts/__pycache__`, restore it with `git checkout -- scripts/__pycache__`.
- **App behaviour rules that stay binding:** no background, periodic or automatic connecting and no timer loops (ADR-044's visible-only re-open is the one
  exception); DLCI 0x04 is claimed only by a user action (ADR-032); the settings stream on DLCI 0x02 stays read-only (ADR-064/065 — **`WriteSetting 4:{13:n}` is
  out of scope**); nothing is sent that no ADR unblocks; nothing on the two GSND channels; every write through the Safe-Mode gate (ADR-042); no new permission;
  no new dependency without `AGENTS.md` §10's justification and the maintainer's OK.
- **Scope** (`PROJECT.md`): nothing beyond the items. A new feature is a checkpoint question, never a silent addition.
- **Privacy:** no MAC addresses, serial numbers, names, Wi-Fi names or street addresses in new text; films may show them — do not quote them.

---

## 3. Items that need the maintainer (ask in Phase 2 — nothing here is decided yet)

For each: verify first, then ask with your own recommendation and the exact text in the question's preview. IDs are 0093's; D-numbers are its §7.

1. **FACT and ADR text corrections** (each a dated Update/Correction):
   - **D1 / A93-PROT-01, A93-DEC-02** — the setting-13 ratios in `PROTOCOL.md` §4.1 (the 2026-10-14 Update), §8's row, ADR-064's Context, the `[1.3.0]` known issue
     in `CHANGELOG.md`, the 0090 summary and `CAP-076-FINDINGS.md` §6.1. Re-run `python3 scripts/anc_setting13_crosscheck.py --census` over the same 54 logs first
     (0093: 73 + 2 + 2 + 12 = 89 Buds-side Notifies; 75 pushed; 12 lost to the Buds' `DISC`). The FACT itself is not in question.
   - **D2 / A93-CAP-05** — "P-256" → "P-192" for link-key types 0x04/0x05 in `PROTOCOL.md` (≈ 2188–2189) and `CAP-058` (validate against the Core Specification).
   - **A93-PROT-02** — "41 of 41 … while Play held the Message Stream" (`PROTOCOL.md` §4.1 note and §8 row of 2026-10-15) split into 29/29 with Play and 12/12 without.
   - **D3 / A93-PROT-03, -04, -07** — the "in-use" code (`0x41`, not `0x42`) in `PROTOCOL.md` §4.5.2; closing the §6 SASS item with the spec's names and layouts;
     closing the §6 ATT `0x0044` item with the 103-log scan (re-run it); and recording lead **L93-1** (`CAP-021`'s audio waves begin with the assistant-session
     opener) as 🟡 in `PROTOCOL.md` §6 and `DESKRESEARCH_FINDINGS.md`.
   - **A93-PROT-05, -06, -08, -09** — the Battery Notification labels, the two stale Status lines (§4.5.3, §4.5.4), the off-by-one frame (1509 → 1508) and the
     `[ ]` items of §6 that are answered.
   - **A93-DEC-01** — ADR-064's Status line ("not yet built" → built in 1.3.0); **A93-DEC-03** — one Status convention for superseded ADRs.
   - The corrections in the capture FINDINGS that change a 🟢 statement (A93-CAP-08, A93-CAP-09: `CAP-007`, `CAP-004`/`CAP-002`, `CAP-012`, `CAP-021`, `CAP-029`,
     `CAP-040`, `CAP-047`, `CAP-050`, `CAP-065` "7 of 7", `CAP-062` NAK count) — list each with its re-derived evidence.
2. **D4 — a fix release 1.3.1** for A93-APP-01 (one `BudsCompanionPairing` instance), A93-APP-02 (keep the cause on an equal read), A93-APP-03 (no `Notified` for a
   `Get`'s answer), A93-APP-04 (stream pushes do not end reads) and the small items of A93-APP-05/06/08 — version 1.3.1 / 10301, `CHANGELOG.md` text, and the
   reduced hardware run (the capture skeleton of Phase 4). Show each design before building it.
3. **D5 / A93-SES-08 — draft ADRs in `DECISIONS.md`**: allow a labelled "DRAFT — proposed" heading by a note in `AGENTS.md` §6, or change `lint_docs.py`.
4. **D6 / A93-DEC-04** — provenance lines for ADR-002, -004, -005, -007 (only the maintainer can say who decided them).
5. **D7** — `targetSdk` 34 (ADR-029): keep for 1.3.x, or plan the step (an ADR-029 Update) — no change without an answer.
6. **D8 / L93-4** — registering and decompiling the official app 1.0.990706425 (A93-HK-03): register it in `reverse-engineering/APK_VERSIONS.md` in this
   session (no decompile here); the decompile as a later CROSSCHECK session in `TODO.md`.
7. **D9** — the two large restructurings (§6 rows 6 and 13 of 0093: split `REVERSE_ENGINEERING.md`; "Current" blocks in `PROTOCOL.md`). Propose per document: in
   this session, or the corrections now and the restructure as a named later MAINTENANCE session in `TODO.md`. A restructure never changes a FACT/ADR text and never
   drops evidence — prove it with a before/after check (every frame number, ADR number and capture ID present before is present after).
8. **D10 / A93-SES-09** — the dated exception note for 1.2.2's release without its hardware run in `RELEASING.md`.
9. **Project law** — `AGENTS.md` §6 framing paragraph (A93-GOV-05), §10 ViewModel bullet (A93-GOV-06); `PROJECT_RULES.md` rule 13 (A93-GOV-04).
10. **App UX choices** in A93-APP-07/08 — a confirmation before "Use different Buds", one label for the noise-control mode, human words for states and presets.
11. **Session records** — the dated corrections to the RESULTs of 0069, 0073, 0079, 0081, 0082, 0089, 0090, 0091 (A93-SES-03/05/06/07) and the INDEX cells
    (A93-HK-05): ask once whether this session may add §4a status/correction lines to those files (they are history).

Anything 0093 raised that is not listed here is processed under the same rules; if it turns out to need a decision, ask.

---

## 4. Resumability (you must be able to continue automatically after a token or rate limit)

Follow `AI_SESSION_LOG_PROCEDURE.md` §5:

- Create **ai-sessions/0094_MAINTENANCE_RESULT_2026_10_15.md** at the very start, with `**Prompt:** ai-sessions/0094_MAINTENANCE_PROMPT_2026_10_15.md`,
  `**Status:** partial — resumed`, a **Progress** block and a **per-item ledger**: one row per 0093 item — the 69 findings (A93-SES-04 is a pointer), L93-1 … L93-5,
  the 19 document proposals (§6 rows 1–19), D1 … D10, the 12 action rows (§8) and one row per Appendix B item that a §4 finding groups — with its ID, verdict
  (processed / narrowed / rejected / not reproduced / already fixed / awaiting maintainer / moved to the capture skeleton / registered in `TODO.md`), the evidence,
  the files changed and the phase. An item with several parts gets one row **per part**.
- Update the Progress block and the ledger at the end of every phase **and** after every large file or file group: which items are done and tested, which files are
  touched but not yet gated, the last gate and lint results, the checkpoint answers verbatim, where intermediate results live (the scratchpad directory —
  re-create them if it is gone).
- **When resuming:** re-read this prompt, then the RESULT's Progress block and ledger, then `git status` / `git diff`; re-read the files the unfinished step touched;
  continue at the first unfinished item. Never redo a finished, recorded item; never assume an unrecorded one was done; never skip one silently. Checkpoint answers
  already recorded in the RESULT are not asked again.
- Phases run **strictly in sequence**. A phase is finished only when every item assigned to it has a final verdict, or is "awaiting maintainer" with the question
  actually asked.

---

## 5. Phases (strictly sequential)

### Phase 0 — set-up
1. Create the RESULT file (§4). Record `git log -1`, `git status --short`, `git fetch` and `git log --oneline HEAD..origin/main`. Create a branch from an
   up-to-date `main` (`git switch main && git pull --ff-only && git switch -c maintenance/0094-process-0093`; say the name used). `android/domain/bin/` is IDE build
   output — never stage it.
2. Baseline gate and `lint_docs.py` (§2) before any change; record the counts. If the gate is not green, stop and report.

### Phase 1 — triage and verification of every 0093 item (read-only)
3. For every item: re-run its evidence, give a verdict, and assign it to a phase (3–7), to the checkpoint, or to the capture skeleton. Check every S-marked item
   and every Appendix B item a §4 finding groups — they are the least certain. Note where cited line numbers have moved.
4. Collect every question for the maintainer (§3) with draft texts and your recommendation.

### Phase 2 — maintainer checkpoint (one consolidated round in this chat; follow-ups only if needed)
5. A short plain-language summary first (what 0093 found, what you verified, what did not reproduce, what you will change), then the questions (`AskUserQuestion`,
   one per decision, options with pros and cons, "(Recommended)" on one). Record the answers verbatim in the RESULT. Apply only what was approved.

### Phase 3 — governance, decisions, protocol, reverse-engineering record (documents)
6. The approved dated Updates in `DECISIONS.md` and `PROTOCOL.md` (D1–D3, A93-PROT-*, A93-DEC-*), with every copy of each corrected statement elsewhere (`CHANGELOG.md`,
   `DESKRESEARCH_FINDINGS.md`, `ARCHITECTURE.md`, `TODO.md`, the 0090 summary by a §4a line if approved; code comments and test names are listed for Phase 6/7).
7. The approved project-law changes (`AGENTS.md`, `PROJECT_RULES.md`); `PROJECT.md` (A93-GOV-01 … 03); `README.md` (A93-GOV-07/08).
8. `ARCHITECTURE.md` rewritten as built at 1.3.0 where 0093 found it stale (A93-ARCH-01 … 05) — the parts that depend on a Phase 6/7 change wait for Phase 8.
9. `REVERSE_ENGINEERING.md` corrections (A93-RE-01 … 06: restore the broken sentence from `git log -S`, the line self-references, the correlation table, the call
   graph, the UUID register row, the small items), and the restructure if approved (D9).

### Phase 4 — captures, test plans, capture plan (documents)
10. Rewrite in place every FINDINGS / EVENT-NOTES 0093 names (A93-CAP-06 … 09 and the Appendix B items they group), after re-deriving each frame; the Test-ID swaps
    against `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`; the Capture Index rows and `id_registry.csv` where they change.
11. `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (A93-CAP-01/02), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (A93-CAP-03: the stale lines; the Evidence cells to pointers if approved under
    D9), `APP_TESTPLAN.md` (A93-CAP-01, A93-CAP-04).
12. **Desk analysis** of what existing logs settle: L93-1 (the assistant-session opener in every capture with a GSND AUDIO burst, not only `CAP-021` — tabulate),
    L93-2/3 (re-run), and any Appendix B claim that needs a re-count. Results go into the captures' FINDINGS and, as labelled proposals, to the maintainer — not into
    `PROTOCOL.md` as FACT without approval.
13. **The capture skeleton** for the hardware run of the approved app fixes (D4) — next free capture number and Group letter, `captures/CAP-NNN-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_<letter>/`
    with its EVENT-NOTES in the layout of `CAP-076-EVENT-NOTES.md` (build, metadata, preparation, steps with expected screen and expected HCI bracket, "Refuted if",
    don'ts, collection, analysis checklist with Test-IDs). Candidate steps, each only if still needed: "Use different Buds" then Connect with two paired Pixel Buds
    or the same pair (A93-APP-01); a reconnect after a hold made while disconnected — the "changed since" line (A93-APP-02); ten tile taps with Google Play services
    present (A93-APP-03); a Refresh and a read right after the subscription (A93-APP-04); TalkBack on the switches and EQ sliders (A93-APP-07). Keep it short; add the
    Group section and the §9 row; register the capture *planned*.

### Phase 5 — scripts, CI, release machinery and the remaining documents
14. `scripts/lint_docs.py`: the checks 0093 proposes — INDEX status cells (A93-HK-05), no `[x]` in `TODO.md` (A93-HK-01), the README ADR count against the registry
    (A93-GOV-07), the first word of an ADR Status against the registry (A93-DEC-03), "(this commit)" left unfilled (A93-SES-07) — run each against the repository
    and fix what it finds; `.github/workflows/lint-docs.yml` `permissions: contents: read` (A93-APP-09); `.gitignore` for `android/**/bin/` (A93-HK-07).
15. `TODO.md` (A93-HK-01/02), `RELEASING.md` (E1 list, D10), `MAINTAINING_DOCS_SITE.md` (A93-HK-04), `CHANGELOG.md` (A93-HK-06 and the other slips),
    `reverse-engineering/APK_VERSIONS.md` (A93-HK-03, D8), `reverse-engineering/tools/BACKLOG.md` and the leftover `scratch_0024/` (A93-HK-08 — ask before deleting),
    `CONTRIBUTING.md` (A93-GOV-09), `WORKSTATION_PREPARATIONS.md` (A93-GOV-10/11), `APK_REVERSE_ENGINEERING_PROCEDURE.md`, `DESKRESEARCH_FINDINGS.md`,
    `AI_SESSION_LOG_PROCEDURE.md` (0093 §6 row 7), `SECURITY.md` (0093 §6 row 18), the session records as approved (§3 item 11), and every other §6 proposal approved.
16. Gate (if a Gradle/CI file changed) and `lint_docs.py`. **Documentation and scripts are now finished; only now does app work start.**

### Phase 6 — the OpenControl app: the user-facing defect first
17. **A93-APP-01.** A failing test first (two lookups through the repository's provider after "Use different Buds" on the Activity's path); inject the singleton
    `BudsCompanionPairing` into `MainActivity` (or move the flag to one shared holder) without breaking the `IntentSender` launch; then the fix; regression test green.
18. **A93-APP-02 / A93-APP-03 / A93-APP-04** as approved: fix `BudsRepositoryImplTest`'s re-open test to answer the `Get` and the `4:13` read with the **same** mode
    (CAP-076 H1 4094 → 4135) so it fails today; then return `null` for an equal read; a unit test that a Notify answering a `Get` emits no `AncOutcome.Notified`;
    carry `stream` in the replies so only a RESPONSE ends a read's wait, and let `_ancModeFresh` come only from the Message Stream Notify.
19. Gate. Mutation checks, one at a time: each fix reverted must fail ≥ 1 test; restore and verify byte-identical.

### Phase 7 — the OpenControl app: the remaining items
20. As approved: A93-APP-05 (catch around `CompanionDeviceManager.associate`; the transport's check-and-put under its lock; a scope exception handler; `.catch` on the
    preference flows), A93-APP-06 (the real `CAP-051` 1527 bytes or delete the fake fixture; the fuzz tests assert the decoder-fault count is 0; the stale test names),
    A93-APP-07 (accessibility), A93-APP-08 (labels, the KDoc of removed 1.2.x parameters, `ModifierParameter`, the stale `TODO(verify)`, the small code items), and
    every stale code comment that repeats a statement corrected in Phases 3–4.
21. List every `transport.send(` call site before and after; any difference is explained (expect none — no new wire traffic).
22. Gate after each item; the final gate after `./gradlew --offline clean`. Compliance: no `INTERNET`, no new permission, no manifest change except what was approved;
    `git diff --name-only -- '*.gradle.kts' '*.toml' '*AndroidManifest.xml'` empty or only what was approved; AGPL header on new files; nothing sent on a channel or
    with a command no ADR unblocks; version 1.3.1 / 10301 only if D4 was approved.

### Phase 8 — documents that follow the code, and verification
23. `ARCHITECTURE.md`, `README.md`, `SECURITY.md`, `CHANGELOG.md` (`[1.3.1]` as approved, with known issues), `APP_TESTPLAN.md` (steps for every app change; add them to
    the capture skeleton), `TODO.md` (every deferred item, every lead not closed, every approved later session), `ai-sessions/INDEX.md` (the 0094 row; rows whose
    status changed).
24. Re-grep every corrected statement across the repository (nothing left in another file, test name or code comment); re-run every command whose result you wrote
    into a document; `python3 scripts/lint_docs.py` → exit 0; final gate.

### Phase 9 — finish
25. Finish the RESULT: a plain-language summary first (what changed for the maintainer and for the app user), the per-item ledger (every 0093 item with its verdict —
    none open), the items that did not reproduce, the checkpoint answers verbatim, the gate table, the mutation table, compliance, the capture-skeleton summary, external
    sources (URL + quoted sentence), what was read in full and in part, and the closing sections "Deferred documentation" (each item also in `TODO.md`) and "Commits".
    Status per `AI_SESSION_LOG_PROCEDURE.md` §4.
26. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes: Conventional Commits, one commit per concern, each building and
    passing on its own (`PROJECT_RULES.md` rule 16) — e.g. `docs` (decisions/protocol), `docs` (captures/test plans), `chore` (scripts/CI), `fix(app)` for
    A93-APP-01 on its own, further `fix`/`test` commits — each with a *why* and the attribution line from the session's system reminder; `git fetch` and rebase before
    pushing, never force; nothing from a build directory, `android/domain/bin/`, `dist/`, `.vscode/` or `__pycache__` staged. Opening the pull request is a separate
    question; never merge it; no tag and no release.

---

## 6. Deliverables

- **ai-sessions/0094_MAINTENANCE_RESULT_2026_10_15.md** — Progress block, per-item ledger (all 69 findings, 5 leads, 19 document proposals, 10 decisions, 12 action rows
  and the grouped Appendix B items closed), checkpoint answers, gate/mutation/compliance tables, external sources, "Deferred documentation", "Commits", footer.
- The changed project files, scripts and app code, and the new capture skeleton — each change traceable to a 0093 item and to its evidence.
- Nothing left silently open: every item processed, narrowed, rejected, not reproduced, already fixed, moved to the capture skeleton, registered in `TODO.md` for a
  named later session, or "awaiting maintainer" with the question asked.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0094_MAINTENANCE_PROMPT_2026_10_15.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0094_MAINTENANCE_PROMPT_2026_10_15
