# 0077_MAINTENANCE_PROMPT_2026_10_07.md — The maintainer's pending decisions: open proposals, two ADR candidates from 1.0.1, the tracked .pyc, and the Buds' state before the next run

**Number:** 0077
**Category:** MAINTENANCE
**Date:** 2026-10-07
**Title:** Put every decision that waits only for the maintainer in front of them — the desk-research proposals of 2026-10-03, the older proposals from
capture FINDINGS, whether the two 1.0.1 behaviours (device choice, the "current value" rule) get an ADR each, untracking
`scripts/__pycache__/lint_docs.cpython-314.pyc`, and restoring the Buds to Balanced with Off unticked — each with a full description, the options with
pros and cons, and a recommendation; apply only what is approved; no app change

---

## 0. How to use this prompt

You are an expert technical auditor and documentation maintainer working in this repository. Run the phases of §4 **strictly in order**; record each phase
before starting the next. **This session's product is decisions, well prepared.** The maintainer asked explicitly: *for every choice an extensive and clear
description, the possible options, and an advice.* A question the maintainer cannot answer from the question itself (because it assumes context they
must look up) is a defect of this session.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action read **in full**, in this order:
`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md`, `DECISIONS.md` (every ADR with its Updates), `TODO.md`. Where a file is too
large to hold at once, read it section by section and say in the RESULT what was read in full and what in part. Then, per task:

- `TODO.md` §1, §3 ("Record Play services' …", the Buds-state item), §4 ("Proposals awaiting the maintainer"), §6 (the `__pycache__` item).
- `DESKRESEARCH_FINDINGS.md`, the entry **"2026-10-03 — Three cross-capture checks …"** in full (its items 1–4 and "Proposals awaiting the maintainer"), and the
  entries of 2026-10-04 and 2026-10-06 where they touch the same codes.
- `PROTOCOL.md` §2.3 (the DLCI 0x08 table), §4.3 Option E (incl. the dated pointer of 2026-10-03), §4.3 Option F, §4.1 (Settable), §4.5.3 (field 12),
  §6; `REVERSE_ENGINEERING.md` — the entries for the runtime-info and software-info messages (find them with `grep -n "SubscribeRuntimeInfo\|GetSoftwareInfo"`).
- The older proposals, each in full at its source: `CAP-008-FINDINGS.md` §4 and §5 (eSCO/mSBC, `CALL-001`); `CAP-026-FINDINGS.md` item 3 (the short Case
  form); `CAP-029-FINDINGS.md` item 3 (`CASE-008`); `CAP-037-FINDINGS.md` item 3 (Settable ↔ Current co-occurrence); `CAP-047-FINDINGS.md` items 4 and 5;
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` row `CASE-007` and `ai-sessions/0068` `A68-SES-03` (the proposal 🔵 → 🟢); `ai-sessions/0059` RESULT (where these were
  listed as open). Locate each with `grep -n -i "proposal"`; if an item number does not exist or says something else, report it — do not guess.
- The 1.0.1 behaviours: `ARCHITECTURE.md` §3.1 bullet "One rule for 'current'" and §8.x item 8 "Which bonded device"; `ai-sessions/0068` `A68-APP-02`,
  `A68-APP-04/05`; `ai-sessions/0069` RESULT (the maintainer's choices of 2026-10-03, "Keep the value, mark it"); `ai-sessions/0070` RESULT (the "Not now"
  of 2026-10-04); `CAP-068-FINDINGS.md` §7 (S1, S2, S4, S7, S8 on hardware); `PROJECT_RULES.md` rules 8 and 9; `AGENTS.md` §6 and §15.
- The `.pyc`: `.gitignore` (the `**/__pycache__/` line), `git ls-files | grep pyc`, `git log --oneline -- scripts/__pycache__/` (when and by which commit it
  entered), `.github/workflows/*` (does CI rely on it — it should not).
- The Buds' state: `TODO.md` §4's item ("left with the EQ on a custom 'Last saved' curve and the press-and-hold mode list with Off ticked after `CAP-069`
  (P7 was Balanced, Off unticked)"); `CAP-070-FINDINGS.md` §2 (the values the Buds reported on 2026-10-07: EQ `4:{16:[−1, 0, 4, 2, 3.72]}` after the BF-21
  drag, field 12 = `{1:1 2:1 3:1 4:0}`); `PROTOCOL.md` §4.2 (the Balanced preset `[-3.5, 0.5, 1.0, -1.0, 2.5]`, band order) and §4.5.3 (field 12's bit order);
  `android/domain/src/main/kotlin/io/github/tedsluis/opencontrolpixelbuds/domain/EqBandGains.kt` (`BALANCED`), the app's Sound and Controls tabs
  (`APP_TESTPLAN.md` H, N).

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5).** Create **ai-sessions/0077_MAINTENANCE_RESULT_2026_10_07.md** at the very
start with `**Status:** partial — resumed` and a **Progress** block; update it at the end of every phase and after every substantial step (each decision
prepared, each question answered, each edit applied): what is done, what is next, which files are touched but unverified, where intermediate results live
(the session scratchpad). A resumed session reads this prompt, then the Progress block, re-reads the files the unfinished step touched, and continues; it
never redoes a recorded step and never assumes an unrecorded step was done.

**The machine.** No Gradle build is needed. Never `git worktree add` a commit (it checks out `captures/`, gigabytes of LFS). Never delete with a wildcard;
temporary output goes into a fresh `mktemp -d` directory. `git rm --cached` is a deletion from the repository — it is done only after the maintainer's yes.

**Nothing is taken on trust**, including this prompt and earlier RESULTs: every count, frame number and quotation in a question is re-derived or quoted
from its source with file and line.

---

## 1. What the maintainer asked for (chat, 2026-10-07, translated from Dutch)

> Make a prompt session for my decisions (MAINTENANCE). A few points wait for me: the open proposals from `DESKRESEARCH_FINDINGS.md`, and the older proposals
> from the capture FINDINGS; whether the two behaviours of 1.0.1 (the device choice and the rule for the "current value") get their own ADR; taking
> `lint_docs.cpython-314.pyc` out of git (`git rm --cached`); setting your Buds back to Balanced, with Off unticked, before the next run. For every choice give
> an extensive and clear description with the possible options and an advice.

Note on the count: the earlier chat summary said "four" desk-research proposals; the 2026-10-03 entry lists **three** — (a) Option E wording, (b) DLCI 0x08
Codes `0x03`/`0x05` in §2.3, (c) field 2 "wall clock" in `REVERSE_ENGINEERING.md`. Re-count from the source and say which number is right; if a fourth exists
elsewhere (e.g. the 2026-10-04 or 2026-10-06 entries), include it.

---

## 2. The decisions to prepare

For **each** decision below, the RESULT gets a section with: **What it is** (plain language first, then the technical facts with file:line and, for a
protocol claim, frame numbers and commands — `PROJECT_RULES.md` rule 4a), **Why it waits for the maintainer** (which rule: `AGENTS.md` §6 FACT/ADR gate,
project law, a deletion, a hardware action), **What changes if yes / if no** (the exact files and the exact text — a draft, not applied), **Options** (2–4,
each with pros and cons), **Advice** (one option, with the reason), and **Risk of waiting**.

### D1 — Desk-research proposal (a): §4.3 Option E wording
"cross-confirms the Right value" → "equals the lower of the two bud levels (🟡)". Evidence: the 2026-10-03 entry item 1 (106 of 118, never the higher; 12
"neither" carry the previous triple's lower value). Re-run the count only if its command is reproducible from the entry; otherwise quote it and say so.

### D2 — Desk-research proposal (b): DLCI 0x08 Codes `0x03` and `0x05` in `PROTOCOL.md` §2.3
Add both to the DLCI 0x08 table with their 🟡 readings (field 3 = lower bud level; Code `0x05` = placement/wear state with values 1, 3, 4, 5, 6 — 3 ≈ both
in the case, 6 ≈ both worn). Include what `CAP-069` (2026-10-04 entry, Group BE) added since, and that the app never opens DLCI 0x08 (ADR-039 Update) — the
consequence for the app is none.

### D3 — Desk-research proposal (c): field 2 = "wall clock, ms (🟡)" in `REVERSE_ENGINEERING.md`
For the runtime-info and software-info messages. Evidence: item 3 of the entry (52–548 ms before capture time in three official-app captures; absent in
OpenControl's, which sends no `SetWallclock`); `ai-sessions/0073` §4.6 (top-level fields 2 and 3 are not in app 1.0.955078536's schema). Also say whether the
`CAP-070` stream (OpenControl 1.1.0) again has field 2 absent — one `grep` of the `pwrpc_decode.py` output, with a positive control.

### D4 — The older proposals from capture FINDINGS (one sub-decision each)
`CAP-008` §4/§5 (eSCO/mSBC, `CALL-001`) · `CAP-026` item 3 (the short Case form) · `CAP-029` item 3 (`CASE-008`) · `CAP-037` item 3 (Settable ↔ Current
co-occurrence) · `CAP-047` items 4 and 5 · `CASE-007` 🔵 → 🟢 (`A68-SES-03`). For each: what the proposal says (quoted), whether later captures confirmed,
contradicted or superseded it (search `PROTOCOL.md` and the later FINDINGS), and the options *apply as drafted* / *apply reworded* / *close as superseded*
/ *keep open with a named experiment*. Several of these may be best **closed as superseded** — say so where the evidence shows it. Put them in as few
questions as stays clear (`AskUserQuestion` allows 4 questions per call; use several calls; each option's preview holds the exact text).

### D5 — An ADR for each of the two 1.0.1 behaviours
(a) **Device choice** — the device is the one of this app's CDM association; without one, a single bonded "Pixel Buds" device; with two or more, none is
picked (`SeveralBudsPaired`) and the user chooses in Android's picker; "Use different Buds" removes the app's associations (`ARCHITECTURE.md` item 8). (b) **The
"current value" rule** — a value is current only while `Ready` and reported on this connection; otherwise it stays visible, dimmed, "from the last
connection"; never read → "—" (`ARCHITECTURE.md` §3.1). Both were built in 1.0.1 on the maintainer's chat answers of 2026-10-03, both are on hardware in
`CAP-068` (S1, S2, S4, S7, S8) and in 1.1.0; no ADR was written (`AGENTS.md` §6: an agent may not write one without approval); the maintainer chose "Not now"
on 2026-10-03 and 2026-10-04. Prepare for each: why `PROJECT_RULES.md` rule 8 ("significant architecture choices are recorded in `DECISIONS.md`") applies or
not; the **full draft ADR** (number = next free per `id_registry.csv`, Status, Context with evidence, Options considered, Decision, Consequences) in the
question's preview; the options *ADR now as drafted* / *ADR later* / *no ADR — `ARCHITECTURE.md` is the record* / *one combined ADR for both*; and the
consequence of each for `TODO.md` §1 (the item is closed only by "ADR now" or "no ADR").

### D6 — Untrack `scripts/__pycache__/lint_docs.cpython-314.pyc`
Facts to establish: since when it is tracked (commit), that `.gitignore` already ignores `**/__pycache__/`, that it changes whenever `lint_docs.py` runs
without `PYTHONDONTWRITEBYTECODE=1` (show `git status` after a normal run in a throw-away copy, or explain from the timestamps — do not leave the
working tree dirty), that no workflow or script reads it. Options: *untrack now* (`git rm --cached scripts/__pycache__/lint_docs.cpython-314.pyc`, the file
stays on disk, one commit), *keep tracked*, *also add a `lint_docs.py` header note*. Say what each later session's instructions (the "`git checkout` it if
it changed" lines in prompts) become.

### D7 — Restore the Buds: EQ preset Balanced, press-and-hold modes with Off unticked
This is a **hardware action for the maintainer**, not something the session can do (no phone access). Establish the current state from `CAP-070` (last
known; `CAP-070-FINDINGS.md` §2: EQ `[−1, 0, 4, 2, 3.72]`, field 12 `{1:1 2:1 3:1 4:0}` = Noise cancellation, Off and Transparency ticked, Adaptive not — check
the bit order against `PROTOCOL.md` §4.5.3), what "the P7 state" of the skeletons means (Balanced, Off unticked), and write the **step list** for doing it
with OpenControl 1.1.0 itself (Sound → preset **Balanced**; Controls → Press and hold → untick **Off** — the app refuses fewer than two ticked modes), with
the expected wire writes (`4:{16:[-3.5, 0.5, 1.0, -1.0, 2.5]}` — the exact frame if a capture holds it, otherwise "derived" and labelled; the field-12 write
`{1:1 2:0 3:1 4:0}`) and how to confirm (the (i) lines "changed HH:MM:SS"; after a reconnect "read …"). Options: *do it now, before the next capture* /
*make it P7 of the next capture's skeleton* / *leave the Buds as they are and update P7*. Advice with reason (e.g. reproducibility of the next run versus
the value of testing from a non-default state).

---

## 3. Rules (binding, in addition to `AGENTS.md`)

- **Evidence.** Every claim with file:line, frame number and command (`PROJECT_RULES.md` rule 4a); a negative with its command, exit status and a positive
  control (`AGENTS.md` §13 step 8); zero creativity with hex (`AGENTS.md` §13.6).
- **The `AGENTS.md` §6 gate.** No 🟢 promotion, no ADR and no ADR Update without the maintainer's approval **in this chat** — model agreement is not approval
  (memory: "Approvals: confirm in chat"). Drafts go into the question previews, exactly as they would be written.
- **How to ask** (the maintainer's request): one decision per question; each option with pros **and** cons; one option marked "(Recommended)"; the preview
  holds the exact text that would be written; the question text is understandable without opening another file. Before the first question, give the
  maintainer a short plain-language overview of all decisions (what, why now, the advice) in the chat. Record every answer verbatim.
- **Non-destructive conventions.** `DECISIONS.md`, `PROTOCOL.md`, `REVERSE_ENGINEERING.md`: dated Updates, nothing overwritten (`PROJECT_RULES.md` rule 9a);
  `CAP-NNN-FINDINGS.md`: rewritten in place or the one status banner — never a dated addendum.
- **Scope.** Exactly the decisions of §2. No app change, no file under `android/`, `dist/` or `scripts/` edited (D6 untracks a file; it does not edit one).
  Anything else found goes to `TODO.md` or the RESULT, not into the diff.
- **Files.** Look before overwriting; never delete with a wildcard; no `git worktree add`.
- **Commits.** Only after the maintainer confirms the final summary.

---

## 4. Tasks

### Phase 0 — set-up
1. Create the RESULT (§0). Record `git log -1`, `git branch --show-current`, `git status --short`, `git fetch && git log --oneline HEAD..origin/main`. Branch
   `maintenance/0077-decisions` from an up-to-date `origin/main`.

### Phase A — prepare (no change)
2. For D1–D7, gather and re-derive the facts (§0's reading list); write each decision's section into the RESULT (§2's template). Where a proposal's source
   is missing, ambiguous or already superseded, say so in its section.
3. Check `id_registry.csv` for the next free ADR numbers (for D5's drafts) and any Test-ID a proposal touches.

### Phase B — the overview and the questions
4. Post the plain-language overview in the chat (§3 "How to ask").
5. Ask with `AskUserQuestion`, in as many calls as needed (≤ 4 questions each), D1–D7 in that order; record the answers verbatim in the RESULT as they come.

### Phase C — apply what was approved
6. Apply only approved items, each with the dated "maintainer-approved in chat 2026-10-07 (or the actual date), `AskUserQuestion` "<header>", option
   *"<label>"*, `ai-sessions/0077`" note: `PROTOCOL.md` (§2.3, §4.3 Option E), `REVERSE_ENGINEERING.md`, the capture FINDINGS (rewritten in place per rule 9a),
   `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (`CASE-007` and any other status cell), `DECISIONS.md` (new ADRs exactly as approved), `ARCHITECTURE.md` (a pointer to a new
   ADR), `id_registry.csv` (new ADR rows), `DESKRESEARCH_FINDINGS.md` (each proposal marked applied/declined with the date — a dated line, not a rewrite),
   `TODO.md` (closed items removed; D7 as a maintainer step or a skeleton note per the answer), `ai-sessions/INDEX.md` (the 0077 row).
7. D6 if approved: `git rm --cached scripts/__pycache__/lint_docs.cpython-314.pyc` — as its own commit; confirm `git ls-files | grep pyc` → nothing and the
   file still exists on disk.
8. `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py` → exit 0.

### Phase D — finish
9. Finish the RESULT: a plain-language summary first (what was decided, what changed, what the maintainer still has to do on the phone), the decisions with
   their answers, "Files read", **"Deferred documentation"** (each item also in `TODO.md`) and **"Commits"**; Status per `AI_SESSION_LOG_PROCEDURE.md` §4/§4b.
10. Show the maintainer a short summary and **ask whether to commit, push and open a pull request**. Only after a yes: Conventional Commits, one per concern
    (`docs: …` for protocol/decision texts, `chore: untrack …` for D6, `docs(session): …` for the prompt/RESULT/INDEX), each with a *why* and the attribution
    line from the session's system reminder; `git fetch` and rebase before pushing, never force. Merging is the maintainer's.

---

## 5. Guardrails (summary)

- Decisions are the maintainer's: prepare them fully, ask clearly, apply only what is approved, record the answers verbatim.
- No FACT promotion, ADR or ADR Update without approval in this chat.
- No app change; nothing under `android/`, `dist/` or `scripts/` edited; the only removal is D6's untracking, after a yes.
- No wildcard deletes, no worktrees; commit and push only after the maintainer confirms.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0077_MAINTENANCE_PROMPT_2026_10_07.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0077_MAINTENANCE_PROMPT_2026_10_07
