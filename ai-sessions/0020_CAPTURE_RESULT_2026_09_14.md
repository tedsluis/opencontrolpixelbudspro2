# 0020_CAPTURE_RESULT_2026_09_14.md — Video-only analysis and CAP-051-EVENT-NOTES.md update for CAP-051 (Group AM)

**Number:** 0020
**Category:** CAPTURE
**Date:** 2026-09-14
**Title:** Video-only analysis of `CAP-051-recording.mp4` and update of `CAP-051-EVENT-NOTES.md` — `.log`
analysis and `CAP-051-FINDINGS.md` are explicitly **out of scope** for this prompt
**Status:** complete

## Phase status

| Phase | Status | Summary |
|---|---|---|
| 0 — Setup | done | Mandatory reading order completed (AGENTS.md, PROJECT.md, PROJECT_RULES.md, DECISIONS.md in full, plus CAPTURE_BLUETOOTH_HCI_SNOOP.md Group AM, AI_SESSION_LOG_PROCEDURE.md, ai-sessions/INDEX.md, CAP-051-EVENT-NOTES.md skeleton). Confirmed folder contents via `ls -la`: `CAP-051-btsnoop_hci.log`, `CAP-051-EVENT-NOTES.md`, `CAP-051-recording.mp4` present. `ffprobe`: duration 73.758489s, 1280x720, 30fps. |
| 1 — Watch video, rewrite EVENT-NOTES | done | Full video watched via dense `ffmpeg` frame extraction (1s spacing full duration, 0.5s spacing around each action). Found: 1 in-app ANC tap (Adaptive→Transparency, ~21:43:26) + **3** physical press-and-hold gestures on the same earbud (Transparency→NC ~21:43:38-42; NC→Adaptive ~21:43:51-55; Adaptive→Transparency ~21:44:02-04) — not 2 as the maintainer recalled. Two of the three inter-action gaps (~9-10s, ~7s) fall short of the planned ≥10s isolation window. Left/Right earbud identity not independently confirmable from video. `CAP-051-EVENT-NOTES.md` rewritten; folder renamed via `git mv` to `CAP-051-2026-09-14_21-42-55_21-44-09-Group_AM`. |
| 2 — Wrap-up | done | See below. |

## Notes

- First run of this prompt (no prior `0020_CAPTURE_RESULT_2026_09_14.md` existed).
- Video duration ~73.76s — dense frame extraction across full duration, tightened to sub-second spacing around each in-app tap and physical gesture.
- `python3 scripts/lint_docs.py` run — no new dead-filename-reference finding traced to the folder rename (grepped for the old `CAP-051-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AM` placeholder; the only remaining hits are inside `ai-sessions/0005_MAINTENANCE_RESULT_2026_09_09.md` and this session's own `ai-sessions/0020_CAPTURE_PROMPT_2026_09_14.md` — both permanent, non-destructively-preserved historical records per `AI_SESSION_LOG_PROCEDURE.md`, not edited). Remaining lint output is pre-existing noise unrelated to this session (other captures' scratch-frame filenames, other sessions' dead refs, and `CAP-051-FINDINGS.md` references to a file this prompt deliberately does not create yet — the same pattern already present for `CAP-050`).
- `./scripts/ensure_footers.py` run — added the missing footer to this file and to `0019_CAPTURE_RESULT_2026_09_14.md` (the latter belongs to the separate, parallel CAP-050 session; noted here only because the same command touched both).
- `ai-sessions/INDEX.md`'s row for `0020` updated to `complete`.
- **Uncommitted at end of session:** `captures/CAP-051-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AM/` → `captures/CAP-051-2026-09-14_21-42-55_21-44-09-Group_AM/` (renamed via `git mv`, contents unchanged except `CAP-051-EVENT-NOTES.md`), `ai-sessions/0020_CAPTURE_RESULT_2026_09_14.md` (new), `ai-sessions/INDEX.md` (row update). Not committed — per project convention, commits happen only when the maintainer explicitly asks.

## Final summary

**Verified action sequence:** 1 in-app ANC-mode tap (Adaptive→Transparency, `QuickActionsFragment` toggle group, ~21:43:26–27) followed by **3** physical press-and-hold gestures on the same earbud — not the 2 the maintainer recalled beforehand ("2x lang ingedrukt"): Transparency→Noise cancellation (~21:43:38–42), Noise cancellation→Adaptive (~21:43:51/52–55), Adaptive→Transparency (~21:44:02–04). The video ends at 21:44:09 with Transparency still selected and no further action.

**Match against the maintainer's account:** partially — the maintainer's account of "ANC modes changing on-screen, potentially more than one transition" is confirmed and even understated (4 total mode changes: 1 app tap + 3 gestures). The "2x lang ingedrukt" physical-gesture count is corrected to **3** gestures, all on the same visible earbud.

**Isolation-window gaps:** the in-app tap → gesture #1 gap (~11s) meets the planned ≥10s window. Gesture #1 → gesture #2 (~9–10s) and gesture #2 → gesture #3 (~7s) both fall **short** of it — flagged explicitly in `CAP-051-EVENT-NOTES.md` as reducing the future log-analysis session's ability to cleanly attribute a DLCI 0x02 write to one specific gesture for those two pairs.

**Left/Right identity:** not independently confirmable from the video — only one ear/earbud is ever in frame, with no marking or view of the other side to establish which physical bud it is. Recorded as an open item, consistent with (not proof of) the maintainer's "right earbud" account.

**Reminder:** `.log`/`btsnoop` analysis and `CAP-051-FINDINGS.md` remain a separate, future task — neither was opened or written in this session, per the prompt's explicit scope boundary. `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's `CAP-051` row and `id_registry.csv` were left untouched (still `planned`), and no new `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` Test-ID was added.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0020_CAPTURE_RESULT_2026_09_14.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0020_CAPTURE_RESULT_2026_09_14
