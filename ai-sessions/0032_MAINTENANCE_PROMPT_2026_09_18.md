# 0032_MAINTENANCE_PROMPT_2026_09_18.md — Close `ai-sessions/0031`'s disclosed Phase 3 reading-scope gap and re-run its consistency-pass checklist properly

**Number:** 0032
**Category:** MAINTENANCE
**Date:** 2026-09-18
**Title:** `ai-sessions/0031_MAINTENANCE_RESULT_2026_09_18.md`'s own "Phase 0/3 scope trade-off" section
honestly disclosed that its Phase 3 document-wide consistency pass was narrower than the original
`0031` prompt asked for — several large documents were skimmed or grep-targeted rather than read
linearly. The maintainer explicitly asked, in the live chat session that ran `0031`, to close that
gap: finish the deferred linear reading, then re-run Phase 3's original checklist (staleness,
contradictions, broken cross-references, undocumented status drift, `id_registry.csv` consistency)
properly now that the reading is actually done.
**Status:** complete

---

## Mandatory reading order

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8. `AGENTS.md`, `PROJECT.md`,
`PROJECT_RULES.md`, `ARCHITECTURE.md`, `DECISIONS.md`, `PROTOCOL.md`, `TODO.md`, `ai-sessions/INDEX.md`
were already read in full by `ai-sessions/0031` earlier the same day, in the same conversation this
session continues — not re-read from scratch here. This session's own job was specifically the
documents `0031`'s own disclosure named as **not** linearly read:

1. `REVERSE_ENGINEERING.md`'s sections beyond what `0031`'s Phase 1 touched.
2. `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (full, all ~1852 lines).
3. `DESKRESEARCH_FINDINGS.md`, `CONTRIBUTING.md`, `WORKSTATION_PREPARATIONS.md`, `CHANGELOG.md`,
   `README.md`.
4. The 5 existing RE tools' own `README.md` files under `reverse-engineering/tools/`.
5. `APK_REVERSE_ENGINEERING_PROCEDURE.md`, `AI_SESSION_LOG_PROCEDURE.md`.
6. `ai-sessions/0027_MAINTENANCE_RESULT_2026_09_17.md` through `0030_MAINTENANCE_RESULT_2026_09_17.md`,
   full, start to finish.

## Scope

Re-run `ai-sessions/0031_MAINTENANCE_PROMPT_2026_09_18.md`'s own Phase 3 checklist (staleness,
contradictions, broken cross-references via `scripts/lint_docs.py`, undocumented status drift,
`id_registry.csv` consistency) across the newly-read documents above, cross-checked against the
already-read core documents. Fix what's safely fixable directly, with an auditable file+line record.
Flag, don't fix, anything that would constitute a new or re-characterized protocol/architecture claim,
a new 🟢 FACT promotion, or a `DECISIONS.md` ADR — per `AGENTS.md` §6/§15, none of those are this
session's call to make unilaterally.

## Guardrails

Same as `ai-sessions/0031`: no new Bluetooth capture, no decompiled APK content committed, no
unilateral FACT/ADR promotion, zero-creativity evidence discipline (`AGENTS.md` §13.6).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0032_MAINTENANCE_PROMPT_2026_09_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0032_MAINTENANCE_PROMPT_2026_09_18
