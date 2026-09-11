# 0009_MAINTENANCE_PROMPT_2026_09_11.md — Apply maintainer decisions from 0008's validation

**Number:** 0009
**Category:** MAINTENANCE
**Date:** 2026-09-11
**Title:** Apply maintainer decisions from 0008's validation: field-19 dual-write-path clarification, 0007/0008 status closeout

---

> **How to use this file:** this entire document (everything below this line) is the prompt given
> to the Claude Code session that produced `ai-sessions/0009_MAINTENANCE_RESULT_2026_09_11.md`,
> reconstructed verbatim from the chat session, per `AI_SESSION_LOG_PROCEDURE.md`'s requirement that
> every substantive prompt be logged even when handed directly rather than as a file.

---

You are an AI assistant working on the OpenControl for Pixel Buds Pro 2 project. The maintainer has
reviewed `ai-sessions/0008_CROSSCHECK_RESULT_2026_09_11.md`'s validation of
`ai-sessions/0007_CROSSCHECK_RESULT_2026_09_11.md` and made explicit decisions, directly in
conversation. Apply them.

## Mandatory reading order

`AGENTS.md`, `PROJECT.md`, `PROJECT_RULES.md` (full, especially §7 rule 17), `DECISIONS.md`,
`AI_SESSION_LOG_PROCEDURE.md` (full, especially §4a — this task is exactly the scenario it
describes), `ai-sessions/INDEX.md`, `REVERSE_ENGINEERING.md`'s `qhr` field 19 entry, and both
`ai-sessions/0007_CROSSCHECK_RESULT_2026_09_11.md` and
`ai-sessions/0008_CROSSCHECK_RESULT_2026_09_11.md` in full.

## Maintainer decisions to apply (already made, apply directly — no further sign-off needed for these)

1. Agreed: treat `0007` as unreliable for any claim not independently reconfirmed elsewhere; the
   existing "Mono audio" 🟢 FACT for `qhr` field 19 stands unchanged.
2. Agreed: add the optional clarification to `REVERSE_ENGINEERING.md` that field 19 has two
   independent write paths.
3. Agreed: update `0007`'s and `0008`'s own `Status` header fields, and commit everything to git.

## Task 1 — Resolve the git-status discrepancy first

Before anything else: `ai-sessions/0008_CROSSCHECK_RESULT_2026_09_11.md` exists on disk but did not
appear in a prior `git status` run (unlike `0007`'s prompt/result and `0008`'s own prompt, which did
appear as untracked). Run `git status` and `git check-ignore -v ai-sessions/0008_CROSSCHECK_RESULT_2026_09_11.md`
and report what's actually going on. If something is incorrectly ignoring this file, fix that first
(e.g. an overly broad `.gitignore` pattern) — don't proceed to commit until this is understood and
resolved.

## Task 2 — Add the field-19 dual-write-path clarification to `REVERSE_ENGINEERING.md`

Update the `qhr` field 19 entry: state explicitly that field 19 has **two independent write paths** —
(a) the dedicated Mono-audio toggle (the field's established, primary identity — unchanged), and
(b) `fxf.java`'s volume-balance-extreme-value side effect (surfaced by `0007`, independently
re-verified in `0008-CROSSCHECK_RESULT_2026_09_11.md` §1.18) — a genuine secondary write to the same
field, not a replacement identity. Do not remove or weaken the existing "Mono audio" identity
statement; add this as a documented, coexisting dual-write note, per rule 9a's rewrite-in-place
convention for this document.

## Task 3 — Update `0007` and `0008`'s own `Status` header fields (per `AI_SESSION_LOG_PROCEDURE.md` §4a)

1. Edit `ai-sessions/0007_CROSSCHECK_RESULT_2026_09_11.md`'s header in place: `Status: awaiting
   maintainer sign-off` → `Status: complete` — cite that the maintainer reviewed this session's
   findings (via `0008`'s validation) and accepted the conclusion that most of its citations were
   unreliable, in the chat session authoring this prompt (this session's own assigned number —
   determine it per Task 4).
2. Edit `ai-sessions/0008_CROSSCHECK_RESULT_2026_09_11.md`'s header the same way: `Status: awaiting
   maintainer sign-off` → `Status: complete` — cite that the maintainer explicitly accepted all four
   of its §3 recommendations as stated, in this same session.
3. Update `ai-sessions/INDEX.md`'s rows for `0007` and `0008` to match the new `complete` status.

## Task 4 — Log this task itself

Check `ai-sessions/INDEX.md` for the next free number (category `MAINTENANCE`) and save this
prompt/result pair accordingly, per `AI_SESSION_LOG_PROCEDURE.md`. Add the row to
`ai-sessions/INDEX.md`.

## Task 5 — Commit

Stage and commit, using Conventional Commits format (`PROJECT_RULES.md` §7 rule 17) and explaining
*why*, not just what (rule 7). Split into sensible, logically separate commits rather than one
bundle — e.g. a `re:` commit for the `REVERSE_ENGINEERING.md` field-19 clarification, and a
`docs:`/`chore:` commit for the `ai-sessions/` status updates and this session's own new log entry.

## Guardrails

- Everything in this prompt is pre-approved by the maintainer directly — apply it, don't stop to
  re-draft-and-ask for these specific items.
- Still don't touch anything beyond what's listed here — no other `PROTOCOL.md`/`DECISIONS.md`
  changes, no other `REVERSE_ENGINEERING.md` edits.
- Four-tier status legend and hex-&-script rule apply to Task 2's addition as they would to any
  other `REVERSE_ENGINEERING.md` content.

## Output

Confirm the git-status resolution (Task 1), the exact text added to `REVERSE_ENGINEERING.md`
(Task 2), both updated `Status` headers and the `INDEX.md` rows (Task 3), this session's own
assigned number (Task 4), and the commit(s) made with their messages (Task 5).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0009_MAINTENANCE_PROMPT_2026_09_11.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0009_MAINTENANCE_PROMPT_2026_09_11
