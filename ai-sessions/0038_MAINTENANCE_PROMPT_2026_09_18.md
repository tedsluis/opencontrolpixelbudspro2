# 0038_MAINTENANCE_PROMPT_2026_09_18.md — Audit and close remaining unimplemented/too-basic v1 gaps

**Number:** 0038
**Category:** MAINTENANCE
**Date:** 2026-09-18
**Title:** Survey the v1 app for parts that are unimplemented, incomplete, or too basic and fix what can be fixed without new maintainer decisions
**Status:** prompt only — not yet run

---

## Context

After `ai-sessions/0037`'s real-Connect implementation and nav-bug fix, the maintainer asked directly
whether other parts of the app are still unimplemented, incomplete, or too basic, and asked for them to
be implemented or improved if so. Unlike prior sessions, this was not triggered by a specific crash
report or real-hardware test — it's a self-directed audit request.

## Task

1. Survey `TODO.md`'s open Phase 4/5 items and the Kotlin source for TODO/placeholder markers, then go
   further: audit `BudsRepository`'s full public API surface against what `:app`/`:ui` actually call,
   and read each Compose screen for correctness issues beyond "does it compile."
2. Fix what can be fixed without a new maintainer decision. Leave anything gated behind a
   `DECISIONS.md` ADR (per AGENTS.md §6, no independent FACT promotion or ADR authoring) exactly as
   gated, and say so explicitly rather than silently skip it.
3. Re-verify with the full build/test/lint suite, adding unit tests for anything newly testable
   without real hardware.
4. Update `TODO.md`, log this session, update `ai-sessions/INDEX.md`, record a `CHANGELOG.md` entry.
5. Commit and push once verified.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0038_MAINTENANCE_PROMPT_2026_09_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0038_MAINTENANCE_PROMPT_2026_09_18
