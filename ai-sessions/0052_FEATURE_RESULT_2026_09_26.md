# 0052_FEATURE_RESULT_2026_09_26.md — Record D-1/D-2/D-3 of `ai-sessions/0051`, then build the EQ preset layout, the Refresh fix and the settings

**Number:** 0052
**Category:** FEATURE
**Date:** 2026-09-26
**Title:** Record the three proposals the maintainer approved in `ai-sessions/0051` (D-1, D-2, D-3), then build the EQ presets in two rows (3 + 2), the *Refresh battery* fix, the Case re-subscription on Refresh, read-only settings (ADR-036) and the settings writes the new ADR unblocks
**Status:** partial — resumed

## Progress

- Phase 0: done. Start commit `0f36bd5` (docs: add prompt 0052 …); `git status --short` clean before this file was created. Baseline gate green
  (`:data` 1509, `:hardware` 49, `:domain` 17, 0 failures; lint `:app` 0 errors / 1 warning, others clean).
- Evidence re-derived (fixtures, film frames, FHN page, `qht`/`hgj` code) — §3.
- Phase A: done — D-1 (a)–(e) in `PROTOCOL.md` (dated Updates, §8 row) and rewritten in place in `CAP-019`/`CAP-020`/`CAP-022` EVENT-NOTES/FINDINGS;
  D-2 = ADR-043 Update; D-3 = ADR-045 (registered in `id_registry.csv`).
- Phase B: done — EQ presets 3 + 2 (`EqScreen.kt`); gate green, counts unchanged (1509/49/17).
- Phase C: done — Refresh = fresh claim, `NoNewBatteryReading`, Case note; +3 repository tests (CAP-062 7098/7106/7110); gate green (1512/49/17).
- Phase D: done in `:data`/`:domain` — `BudsSettings.kt` (domain), `SettingFrame.kt` (`SettingsCodec`, `SettingValue`), `Maestro.READABLE_FIELDS`
  = {2, 4, 7, 16, 17, 18, 19, 22}, `EqFrameDecoder` limited to `Maestro.EQ_FIELDS`, `RoutedFrame.Setting`, Connect sequence EQ → settings reads →
  `SubscribeRuntimeInfo`, one re-subscription per Refresh (ADR-043 Update). Tests: `SettingsCodecTest` (14, real frames in `SettingsFixtures.kt`,
  fuzz), repository tests updated/added. **Settings UI not built yet.**
- Phase E: done (checkpoint answers in §4).
- Phase F: repository writes done (`setVolumeBalance`, `setMonoAudio`, `setConversationDetection`, `setTouchControls`, `setPressAndHold`) with 6 tests;
  `:data` unit tests all green at the last run (full gate not yet re-run).
- **Next (resume here):** (1) UI — rename the EQ tab "Sound" and add balance slider (display: +100 = Left, slider left end = Left), mono and
  conversation-detection switches below the presets; a new "Controls" tab (`ControlsScreen.kt`: touch controls, hold Left/Right, in-ear detection
  setting read-only) in `OpenControlNavHost.kt`; wire `settings`/`settingsError` and the five writes in `MainActivity.kt`; texts exactly as in §4.
  (2) full gate after `clean`; mutation checks (zigzag removed, wrong field number, write applied without ACK, Refresh reuses the open claim,
  re-subscription sent twice) with `cmp` restore. (3) compliance list of new `transport.send` sites (readSettings, writeSetting, Refresh
  re-subscription). (4) Phase G docs: `ARCHITECTURE.md` §2.4 ("5 sliders + 6 presets" → 5 presets; Sound/Controls tabs), §3.1, §5a; `APP_TESTPLAN.md`,
  `TODO.md`, `CHANGELOG.md`, `README.md`, `PROJECT.md`, `ai-sessions/INDEX.md` 0052 row; Group AY re-test table; `ensure_footers.py`, `lint_docs.py`.
  (5) ask the maintainer about commit/push. Not updated (outside the prompt's file list): `CAP-034-FINDINGS.md`/EVENT-NOTES still say `FE2C1238`
  is unnamed.
- Intermediate results: scratchpad `/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/0cc61406-83d2-4a02-a9d4-db7bfec27e89/scratchpad`.

## 4. Maintainer decisions (this chat, 2026-09-26, `AskUserQuestion`, previews carried the exact texts)

- **D-1** (multi-select): **"(a) conv. detection, (b) touch controls OFF, (c) balance persists, (d)+(e) FHN + qht"** — all five.
- **ADRs** (multi-select): **"D-2 Refresh re-subscribes, D-3 ADR-045 writes"** — both.
- **Checkpoint (Phase E)** — asked right after the Phase D codec/repository work and **before** building any settings UI, so the read-only rows and
  the writes were built once in their final place (a deliberate reorder of D's "read-only UI rows"):
  - **Placement**: **"New 'Controls' tab"** (preview: *Tabs: Connection ANC Sound Controls Find Debug — Sound: sliders, presets, Balance, Mono,
    Conversation detection — Controls: Use touch controls, Hold Left, Hold Right, In-ear detection (setting)*).
  - **Texts**: **"As in the preview (Recommended)"** — *Balance "Left 40 · read 14:32:07" (ends 'L'/'R'; 0 = 'Centre'); "Mono audio — same sound in
    both ears"; "Conversation detection — switch from noise cancellation to transparency when you talk"; "Use touch controls"; "Press and hold — Left:
    Noise control | Digital assistant" (same for Right); "In-ear detection (setting): on — read-only; not whether a bud is worn"; not read: "Not read
    from the Buds yet"; time "read HH:MM:SS" / "changed HH:MM:SS"; read error "Couldn't read the Buds' settings: <reason>"; write error "The setting was
    not changed: <reason>"; Refresh "No new battery reading from the Buds — try again." + "The Buds report the Case level only while a bud is in the case."*
