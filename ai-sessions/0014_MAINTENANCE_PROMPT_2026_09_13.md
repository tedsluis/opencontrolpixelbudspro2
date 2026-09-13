# 0014_MAINTENANCE_PROMPT_2026_09_13.md — Decide minimum Android API level, refresh README.md, commit and push

**Number:** 0014
**Category:** MAINTENANCE
**Date:** 2026-09-13
**Title:** Decide minimum Android API level, refresh README.md's project status, commit and push

---

## Mandatory reading order

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8: `AGENTS.md`, `PROJECT.md`,
`PROJECT_RULES.md`, and `DECISIONS.md` in full at minimum before any other action, plus
`ARCHITECTURE.md` (§1/§15 specifically, for the open minimum-API question this prompt resolves),
`TODO.md` (Phase 5's matching checklist item), and `README.md` (the file being updated) — all
already read in full earlier in this same conversation, still current.

## Task (verbatim, as given by the maintainer in chat, translated from Dutch)

> 1) I decide that Android 14 is the minimum version. Process this decision in the relevant files.
> 2) Update the current state of the project in README.md.
> 3) Commit and push the files. If possible, make separate commits per prompt.

This is a direct continuation of the same chat session that authored and resolved
`ai-sessions/0013_FEATURE_PROMPT_2026_09_13.md`/`ai-sessions/0013_FEATURE_RESULT_2026_09_13.md` —
that session's own Phase 5 (`TODO.md`'s "Decide minimum supported Android API level" item,
`ARCHITECTURE.md` §15) is what this prompt closes.

## Scope

1. Record the minimum-Android-API-level decision (Android 14 / API 34, matching this project's
   already-fixed compile/target SDK) as a `DECISIONS.md` ADR, per `AGENTS.md` §6/§15 — this session
   has the maintainer's direct, explicit decision in-conversation, so no proposal/sign-off gate
   applies here (unlike `0013`'s Phase 4/5/6 items, which were AI-drafted proposals). Apply the
   decision to every relevant document (`ARCHITECTURE.md`, `TODO.md`) and to the actual Android
   project (`android/`'s `:app`/`:hardware`/`:ui` `minSdk`), then rebuild/re-test to confirm nothing
   regresses.
2. Update `README.md`'s "Current state" section (and any other stale summary text, e.g. the top
   status callout and the "Target platform" section) to reflect the project's actual current state —
   captures, decisions, protocol readiness, and, new since the last README update, the Android app
   scaffolding itself (five Gradle modules, ANC codec implemented/tested, Hilt wired, minimum API
   decided).
3. Commit and push. Per the maintainer's own instruction, make separate commits per distinct prior
   "prompt" where practical, rather than one bundled commit — applied here as: session `0012`'s own
   files, session `0013`'s own files (including the three decisions approved earlier this same chat
   turn and the new `android/` project), and this prompt's own files (minSdk decision + README
   refresh), as three separate commits, in that order, followed by a single push.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0014_MAINTENANCE_PROMPT_2026_09_13.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0014_MAINTENANCE_PROMPT_2026_09_13
