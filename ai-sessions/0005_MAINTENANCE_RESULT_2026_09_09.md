# 0005_MAINTENANCE_RESULT_2026_09_09.md — Create capture-session skeletons for the 10 outstanding non-destructive captures that are new or need re-execution

**Number:** 0005
**Category:** MAINTENANCE
**Date:** 2026-09-09
**Title:** Create capture-session skeletons for the 10 outstanding non-destructive captures that are new or need re-execution
**Status:** complete

---

## Mandatory reading order

Read in full, in order, before taking any other action: `AGENTS.md`, `PROJECT.md`,
`PROJECT_RULES.md`, `DECISIONS.md` (every ADR), `ARCHITECTURE.md` (structure/section headings),
`PROTOCOL.md` (§4.3 Option A, §4.5.3, §4.5.7, §5.2, §6, plus structure), `TODO.md` (full),
`AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (full),
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (full), `id_registry.csv` (full), and the existing
`CAP-018-EVENT-NOTES.md`/`CAP-026-EVENT-NOTES.md` placeholder skeletons for structure/tone
comparison.

## Task 1 — Next-free `CAP-NNN` and Group letter, re-verified

`id_registry.csv` and `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's Capture Index both confirmed `CAP-042`
as the last-assigned capture ID and Group `AI` as the last-assigned Group letter — matching this
prompt's own as-of-writing estimate. **Next free `CAP-NNN` = `CAP-043`; next free Group letter =
`AJ`.** No intervening work had landed since the prompt was authored.

## Final `CAP-NNN` ↔ session ↔ Group-letter mapping

| Session | `CAP-NNN` | Group | Test-ID(s) |
|---|---|---|---|
| A — repeat of `CAP-011` (Battery Notification BLE scan) | `CAP-043` | Q (repeat) | `BATT-002`, `BATT-003` |
| B — repeat of `CAP-033`/`SDP-001` (SDP UUID branch isolation) | `CAP-044` | AA (repeat, 2nd attempt) | `SDP-001`, `SDP-002` |
| C — `HOLD-005` Left/Right ANC-rotation-checklist split | `CAP-045` | AJ (new) | `HOLD-005` |
| D — Volume balance (`field 17`) scale/direction | `CAP-046` | AK (new) | `AUDIO-003` |
| E — DLCI 0x0a burst trigger, purpose-built hypothesis test | `CAP-047` | AL (new) | none existing — flagged as a `TESTPLAN` follow-up |
| F — repeat of `CAP-037`'s dock-state anomaly with an open ACL | `CAP-048` | AD (repeat) | `OBS-004` |
| G — repeat of `CAP-039`'s unexplained disconnect/reconnect cycling | `CAP-049` | AF (repeat) | `OBS-006` |
| H — repeat of `CAP-040` with a genuine trigger | `CAP-050` | AG (repeat) | `PRIV-001` |
| I — `qhr` field 13 ANC-parallel-path wire confirmation | `CAP-051` | AM (new) | none existing — flagged as a `TESTPLAN` follow-up |
| J — `CAP-041` Case%-change bracket | `CAP-052` | AN (new) | none existing — flagged as a `TESTPLAN` follow-up |

This matches the prompt's own as-of-writing estimate exactly (no drift found at execution time).

## Task 2 — 10 capture-session skeleton directories/files created

All 10 directories created under `captures/`, each with a single `CAP-NNN-EVENT-NOTES.md` file
matching `CAP-018-EVENT-NOTES.md`'s exact structure (status banner, Purpose paragraph, Log Metadata
table, Procedure, Event Timeline template, Analysis checklist, "Next steps after filling this in"
checklist, repo footer):

- `captures/CAP-043-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_Q/CAP-043-EVENT-NOTES.md`
- `captures/CAP-044-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AA/CAP-044-EVENT-NOTES.md`
- `captures/CAP-045-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AJ/CAP-045-EVENT-NOTES.md`
- `captures/CAP-046-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AK/CAP-046-EVENT-NOTES.md`
- `captures/CAP-047-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AL/CAP-047-EVENT-NOTES.md`
- `captures/CAP-048-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AD/CAP-048-EVENT-NOTES.md`
- `captures/CAP-049-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AF/CAP-049-EVENT-NOTES.md`
- `captures/CAP-050-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AG/CAP-050-EVENT-NOTES.md`
- `captures/CAP-051-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AM/CAP-051-EVENT-NOTES.md`
- `captures/CAP-052-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AN/CAP-052-EVENT-NOTES.md`

For Sessions E, I, and J (no existing Test-ID), the skeleton's own Purpose section explicitly notes
that a dedicated Test-ID would be a `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` addition out of this prompt's
scope, and flags it as a follow-up rather than silently assigning one — per Task 4's own
instruction. No `TBD` field was filled with a guessed value; no capture was performed.

## Task 3 — 5 new Group definitions added to `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4

Added `#### Group AJ`, `AK`, `AL`, `AM`, `AN` (Sessions C, D, E, I, J respectively) directly after
Group AI's own content, before "### 4.2 Pixel 9a" — matching the existing Group Y/AA entries'
structure (Purpose paragraph + numbered procedure + Analysis guidance), content based directly on
the Purpose/Procedure text from the prompt. Sessions A, B, F, G, H reuse their original Group's
existing §4 definition unchanged (Q, AA, AD, AF, AG) — no new Group-definition text was added for
those five, per the prompt's own instruction.

## Task 4 — 10 new Capture Index rows added to `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9

One row per session, `Status` = `planned`, Phone/Android/Buds FW/App version columns = `TBD`,
Bugreport file/Extracted log columns = `—`, matching the existing `CAP-018`-style placeholder row
format. Existing Test-IDs used where already catalogued (`BATT-002`/`BATT-003`, `SDP-001`/`SDP-002`,
`HOLD-005`, `AUDIO-003`, `OBS-004`, `OBS-006`, `PRIV-001`); Sessions E/I/J's Test(s) column
explicitly states "none existing — flagged as a follow-up" rather than inventing one.

## Task 5 — 10 new `CAP-NNN` entries registered in `id_registry.csv`

`id_registry.csv` tracks only `ADR-NNN`/`CAP-NNN`/Test-ID rows — it has no separate Group-letter
registry (confirmed by reading the file's existing structure in full before assuming otherwise, per
the prompt's own caution). One row added per new `CAP-NNN` (`CAP-043` through `CAP-052`), `status
= planned`, matching the existing `capture,planned,"session date/status: *planned* — ..."` format
used for `CAP-018`/`CAP-026`/`CAP-028`/`CAP-029`/`CAP-030`. No Group-letter rows were added, since
the registry's own established practice doesn't track those separately.

## Task 6 — `TODO.md` cross-references made concrete

Updated the specific bullets that previously described these items only vaguely, now naming the
actual assigned `CAP-NNN`:

- **"Recommended priority order" §4** (line ~51): the "clean connection-free repeat of the Battery
  Notification BLE scan" and "proper isolation-clean repeat of `SDP-001`" bullets now name
  `CAP-043` and `CAP-044` respectively.
- **"Recommended priority order" §5** (lines ~64-80): the `HOLD-005` Left/Right split, Volume
  balance `field 17` scale/direction, and `CAP-021` DLCI 0x0a burst-trigger bullets now name
  `CAP-045`, `CAP-046`, and `CAP-047` respectively.
- **Phase 1** (the "Passively capture a BLE scan..." bullet, line ~255): "Still open: a genuinely
  clean, connection-free repeat is needed" now names `CAP-043`.
- **Phase 3** ("Bring the first command to full FACT status", line ~431): the battery-Option-A
  capture-blocked note now names `CAP-043`.

**Sessions F, G, H, I, and J have no matching vague bullet in `TODO.md`** — their open questions
(the `CAP-037` dock-state anomaly, `CAP-039`'s disconnect/reconnect cycling, `CAP-040`'s unmapped
DLCI 0x08 codes, `qhr` field 13's parallel path, and `CAP-041`'s Case%-change question) are tracked
exclusively in `PROTOCOL.md` §6, per `TODO.md`'s own "Open questions" section policy ("tracked at
their source only"). No `TODO.md` edit was made for these five, since none was needed — confirmed
by grepping `TODO.md` for every relevant term before concluding this, not assumed. No `PROTOCOL.md`
edit was made either, matching the guardrail that this is preparatory/administrative work only.

The priority ordering itself was not changed in any of the above — only the existing bullets were
made concrete.

## Guardrails observed

- No capture was performed; no `TBD` field was filled with a guessed or invented value.
- No protocol claim was touched; no `PROTOCOL.md` promotion or `DECISIONS.md` ADR was written or
  proposed.
- The destructive, optional factory-reset re-pair item (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1
  Group P #16) was not touched.
- No skeleton was created for any of the 5 already-planned captures (`CAP-018`/`CAP-026`/
  `CAP-028`/`CAP-029`/`CAP-030`).
- `python3 scripts/lint_docs.py` run after all edits: no new unregistered-ID or dead-filename-
  reference findings introduced by this session's changes (the one dead-reference finding for this
  file's own path, in `TODO.md` and the paired `PROMPT` file, is expected and resolved by this
  file's own existence; a handful of unrelated pre-existing dead references in `CAP-038`/`CAP-039`/
  `CAP-040`'s own findings files predate this session and were not touched).

## Every file created/changed

**Created (11 files, 10 directories):**
- 10× `captures/CAP-0NN-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_<letters>/CAP-0NN-EVENT-NOTES.md`
  (`CAP-043`–`CAP-052`, listed in full under Task 2 above)
- `ai-sessions/0005_MAINTENANCE_RESULT_2026_09_09.md` (this file)

**Changed:**
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md` — 5 new Group definitions (§4, Groups AJ/AK/AL/AM/AN) + 10 new
  Capture Index rows (§9, `CAP-043`–`CAP-052`)
- `id_registry.csv` — 10 new `CAP-NNN` rows (`CAP-043`–`CAP-052`)
- `TODO.md` — 5 bullets made concrete with actual `CAP-NNN` references (Sessions A–E only; F–J have
  no matching bullet, see Task 6)
- `ai-sessions/INDEX.md` — this session's row `Status` updated from "pending — prompt only, not yet
  run" to "complete"

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0005_MAINTENANCE_RESULT_2026_09_09.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0005_MAINTENANCE_RESULT_2026_09_09
