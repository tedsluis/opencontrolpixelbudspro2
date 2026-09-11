# ai-sessions/INDEX.md — AI Session Prompt/Result Registry

Lookup table for every logged AI-agent prompt/result pair under `ai-sessions/`, per
`AI_SESSION_LOG_PROCEDURE.md`. **Check this table for the next free `Number` before assigning one to
a new session** — the same "check before assigning" discipline `id_registry.csv` uses for
`CAP-NNN`/`ADR-NNN`/Test-IDs, applied here as its own, separate registry.

| Number | Category | Date | Title | Status |
|---|---|---|---|---|
| 0001 | CROSSCHECK | 2026-09-07 | Deep V1-without-GMS iterative cross-check (2nd pass) | complete |
| 0002 | MAINTENANCE | 2026-09-08 | Session logging rule additions, 0001 sign-off implementation, and V1 protocol-readiness gap scan | complete |
| 0003 | MAINTENANCE | 2026-09-08 | Execute 0002's Task 4 gap scan (APK RE, cross-checks, consistency, external validation, documentation) | complete |
| 0004 | MAINTENANCE | 2026-09-09 | Clean up TODO.md's stale Phase 3 status; advance non-capture-dependent Phase 3 work (UUID register, connection-lifecycle analysis on existing captures) | complete |
| 0005 | MAINTENANCE | 2026-09-09 | Create capture-session skeletons for the 10 outstanding non-destructive captures that are new or need re-execution | complete |
| 0006 | MAINTENANCE | 2026-09-09 | Expand the CAP-018/026/028/029/030 placeholder skeletons into complete test/preparation/execution descriptions | complete |
| 0007 | CROSSCHECK | 2026-09-11 | Independent Gemini CLI review of the APK reverse-engineering catalog and the 0001 deep cross-check pass | complete |
| 0008 | CROSSCHECK | 2026-09-11 | Full, non-sampled Claude Code validation of Gemini's 0007 cross-check result, with direct application of verified corrections | complete |
| 0009 | MAINTENANCE | 2026-09-11 | Apply maintainer decisions from 0008's validation: field-19 dual-write-path clarification, 0007/0008 status closeout | complete |

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/INDEX.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/INDEX
