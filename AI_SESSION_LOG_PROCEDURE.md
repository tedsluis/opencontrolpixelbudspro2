# AI_SESSION_LOG_PROCEDURE.md — AI Session Prompt/Result Logging

**Purpose:** every substantive prompt given to an AI agent working on this project (Claude Code, or
another tool per `WORKSTATION_PREPARATIONS.md`'s cross-validation workflow), and that agent's
resulting output/report, is saved permanently into `ai-sessions/` under the fixed naming scheme
below — instead of living only in a chat transcript that disappears when the session ends or is
cleared. This document is the *how*; `ai-sessions/INDEX.md` is the *lookup table* this procedure's
numbering discipline depends on. See `PROJECT_RULES.md` §5 for the binding rule this procedure
implements.

This is a documentation/process convention, not a protocol claim — it does not go through the
FACT/HYPOTHESIS/ADR sign-off gate in `AGENTS.md` §6/§15 (that gate governs protocol and architecture
claims, not workflow bookkeeping).

---

## 1. Naming scheme

Each logged session produces a **paired** set of two files, sharing the same number and category:

- `NNNN_CATEGORY_PROMPT_YYYY_MM_DD.md` — the prompt given to the agent.
- `NNNN_CATEGORY_RESULT_YYYY_MM_DD.md` — the agent's resulting output/report.

Both live directly in `ai-sessions/` (no subdirectories).

- **`NNNN`** — a single, global, zero-padded 4-digit sequence number, shared across **all**
  categories (e.g. `0001`, `0002`, `0003`, …). There is no separate per-category counter — a
  `CROSSCHECK` entry and the next `REVIEW` entry share the same increasing sequence.
- **`CATEGORY`** — one of the fixed values in §2 below.
- **`YYYY_MM_DD`** — the date the prompt was given (for the `PROMPT` file) — the `RESULT` file uses
  the same date as its paired `PROMPT` file even if the result itself was finished or last updated
  on a later date (the header's `status` field, §4, is where later-date context belongs for a
  multi-part result, per §5).

**Example pair:** `ai-sessions/0001_CROSSCHECK_PROMPT_2026_09_07.md` /
`ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md`.

## 2. Fixed category vocabulary

A closed list — no ad hoc category names:

| Category | Use for |
|---|---|
| `FEATURE` | Implementing or designing a specific protocol/app feature |
| `CROSSCHECK` | Cross-validating existing findings against captures/APK/another AI model |
| `REVIEW` | Reviewing existing documentation, code, or findings for correctness/consistency |
| `AUDIT` | Broad, structured audits of project state, scope, or compliance with `AGENTS.md`/`PROJECT_RULES.md` |
| `SETUP` | Environment, tooling, or workstation setup tasks |
| `CAPTURE` | Planning or analyzing a Bluetooth HCI capture session |
| `MAINTENANCE` | Repository/documentation housekeeping not covered by the categories above |

If a new task genuinely doesn't fit any of these, that is a reason to **deliberately extend this
list** (add a new row here, in a dedicated documentation change) — never to invent a one-off
category name for a single session's files.

## 3. Numbering discipline

Before assigning `NNNN` to a new session, **check `ai-sessions/INDEX.md` for the next free number**
— this mirrors the same "check the registry before assigning" discipline `id_registry.csv` already
uses for `CAP-NNN`/`ADR-NNN`/Test-IDs (see `README.md`'s documentation table). `ai-sessions/INDEX.md`
is this convention's own, separate registry — it is **not** merged into `id_registry.csv`, which
keeps its existing scope (captures, ADRs, Test-IDs) unchanged.

## 4. Required header block

Every prompt and result file starts with a header block immediately after the title:

```markdown
# NNNN_CATEGORY_PROMPT_YYYY_MM_DD.md — <one-line title>

**Number:** NNNN
**Category:** CATEGORY
**Date:** YYYY-MM-DD
**Title:** <one-line title>
```

`RESULT` files carry one additional required field, **Status**, with one of these three values:

- `complete` — the task finished in one pass, nothing pending.
- `partial — resumed` — the task was interrupted (e.g. a rate limit) and picked up again; the file
  documents how far it got.
- `awaiting maintainer sign-off` — the task itself is finished, but its conclusions are proposals
  that need maintainer review before anything is promoted into `PROTOCOL.md`/`DECISIONS.md`/etc.
  (per `AGENTS.md` §6).

```markdown
**Status:** complete | partial — resumed | awaiting maintainer sign-off
```

### 4a. Updating a `Status` field later (added 2026-09-08, prompt `0002`)

A `RESULT` file's `Status` field is not frozen at the value it was given when first written — a
**later** logged session (a new `NNNN_CATEGORY_PROMPT_YYYY_MM_DD.md`/`NNNN_CATEGORY_RESULT_YYYY_MM_DD.md`
pair) may update an earlier entry's `Status` field in place, when and only when the specific condition
that value represents has actually been met in that later session:

- `awaiting maintainer sign-off` → `complete` (or a status reflecting whatever new work followed) once
  the maintainer has actually reviewed and signed off on the pending proposals — the later session's
  own `RESULT` file must cite where/how that sign-off happened (e.g. "approved by the maintainer in
  the chat session that authored prompt NNNN," or a specific `DECISIONS.md` ADR number it produced).
- `partial — resumed` → `complete` (or a further `partial — resumed`) once the interrupted task
  actually finishes or is picked up again.

This is an *update to the earlier file's header field*, not a new duplicate entry — the earlier
`NNNN_CATEGORY_RESULT_YYYY_MM_DD.md` file itself is edited in place, and `ai-sessions/INDEX.md`'s row
for that same `NNNN` is updated to match. The later session's own new pair (its own `NNNN`) is what
records *that the update happened and why*; it does not re-narrate the earlier file's substantive
content, matching the spirit of `PROJECT_RULES.md` rule 9a (a reference document is not an
accumulating changelog).

### 4b. Closing checklist (added 2026-10-03, `ai-sessions/0069`, from `ai-sessions/0068` `A68-SES-01`…`03`)

Before a RESULT file's `Status` is set to `complete`:

1. **Deferred items reach `TODO.md`.** Every item the session defers has a line in `TODO.md`; the RESULT lists them under "Deferred" with the
   `TODO.md` wording, so the two can be compared by `grep`.
2. **Commits are back-filled.** A RESULT is usually written before its last commit exists. Its "Commits" section names the commits made so far; the
   **next** session's first step completes it (hash and subject of each later commit of that session). A release adds its row to `RELEASING.md`'s
   Release log.
3. **The `Status` value is one of the three above** — `scripts/lint_docs.py` checks the line of every RESULT file. Text after the value is free
   (`complete (closed 2026-10-03 by …)`).
4. **In an audit that uses sub-reviews,** each finding says whether the main session re-derived it or took it from a sub-review (the M/S marks of
   `ai-sessions/0068`). A sub-review claim that was not re-derived is an open question with its check, not a fact.

## 5. Multi-part results

A long-running or rate-limit-interrupted task does not get a new number each time it resumes.
Instead, the **same** `RESULT` file is progressively appended to across resumptions, and its
header's `Status` field is updated to reflect the current state (`partial — resumed` while still in
progress, `complete` or `awaiting maintainer sign-off` once it finishes). The `PROMPT` file's number
and date stay fixed to the session's original start — it is not rewritten on each resumption.

## 6. Scope — what gets logged

This applies to **substantive** task prompts: the kind of multi-phase, governance-aware prompts this
project already produces for capture analysis, APK reverse-engineering passes, and audits — not
one-off trivial commands (a single file read, a quick grep, a one-line typo fix). Use judgment, but
**default to logging when in doubt** — an unnecessary log entry costs little; a missing one loses a
record of "how this AI-agent output was arrived at" that no chat transcript preserves once the
session ends.

## 7. Version control

These files are **git-tracked**, not gitignored — they are permanent project record, on the same
footing as `CAP-NNN-FINDINGS.md` or `DECISIONS.md`.

## 8. Mandatory reading order (added 2026-09-08, prompt `0002`)

Every `NNNN_CATEGORY_PROMPT_YYYY_MM_DD.md` file's body must itself state, explicitly, `AGENTS.md`
§0.1's reading order (or a superset of it) — binding on the **prompt author**, not optional
boilerplate an author may skip on the assumption the executing session already knows this.

Regardless of what a specific prompt's task-specific instructions say, and regardless of whether the
prompt itself restates this requirement, the **executing AI session** must, at minimum, read
`AGENTS.md`, `PROJECT.md`, `PROJECT_RULES.md`, and `DECISIONS.md` in full before taking any other
action. This is a floor, not a replacement for `AGENTS.md` §0.1's own full reading order (which also
covers `ARCHITECTURE.md`, `PROTOCOL.md`, and `TODO.md`) or `PROJECT_RULES.md` rule 13/13a's existing
requirement that an AI model read `AGENTS.md`, `PROJECT_RULES.md`, `ARCHITECTURE.md`, and the
relevant sections of `PROTOCOL.md` at the start of a session. If a prompt's author forgot to restate
the reading order, the executing session still does the reading — the absence of the reminder in a
specific prompt file is a defect in that file, not license for the session to skip it.

## 9. Prompt template (added 2026-09-30, `ai-sessions/0059`)

Approved by the maintainer in chat on 2026-09-30 (`AskUserQuestion` "Process", *"Approve all (Recommended)"*), after `ai-sessions/0058`
A58-SES-01/03/04/05. A prompt (`NNNN_CATEGORY_PROMPT_YYYY_MM_DD.md`) has these parts, in this order:

1. **Header block** (§4) — with no Status line: a PROMPT carries no Status field; the RESULT does.
2. **Reading block:** `AGENTS.md` §0.1's seven files in full (§8); then the files this task touches, section by section, each section in full
   before it is changed. The RESULT says what was read in full and what in part.
3. **Per-task reading list:** name the sections (e.g. `PROTOCOL.md` §4.1, `CAP-062-FINDINGS.md` §2) instead of "read <1.4 MB> in full".
4. **Rules block:** evidence (`PROJECT_RULES.md` rule 4a; a negative needs a positive control, `AGENTS.md` §13 step 8), labels
   (FACT/HYPOTHESIS/ASSUMPTION), the `AGENTS.md` §6 gate (no FACT promotion or ADR without the maintainer).
5. **Resumability** (§5) and the **checkpoint**: questions in chat, one per decision.
6. **The RESULT must end with** "Deferred documentation" (each item also added to `TODO.md`) and "Commits" (hashes, or "not committed —
   reason").

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/AI_SESSION_LOG_PROCEDURE.md - https://tedsluis.github.io/opencontrolpixelbudspro2/AI_SESSION_LOG_PROCEDURE
