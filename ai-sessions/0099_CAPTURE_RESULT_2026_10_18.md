# 0099_CAPTURE_RESULT_2026_10_18.md — Full analysis of CAP-079 (Group BO: release candidate 1.4.0 and the experiment build 1.4.0-exp.1 — round 2, in the user without and the user with Google Play)

**Number:** 0099
**Category:** CAPTURE
**Date:** 2026-10-18
**Title:** Full analysis of `CAP-079` (Group BO), per `ai-sessions/0099_CAPTURE_PROMPT_2026_10_18.md`
**Status:** complete

## Summary (plain language)

The maintainer ran round 2 on 2026-10-18 in four parts: the release candidate 1.4.0 in the user without Google Play (07:01 – 07:36) and in the Owner user with it
(07:36 – 08:00), then the experiment build in both users (08:00 – 08:45). Nine films were made; one camera film was cut off and its picture was recovered from a
copy. Both installed APKs were checked on the phone against the built ones (equal hashes).

- **Noise control on the app's own channel works (ADR-067).** 26 mode changes: 22 confirmed by the Buds, 4 refused while no bud was worn — the app said so.
  The tile and the tab never chose a mode that was unticked for press and hold. With in-ear detection off the Buds allow a change even when nobody wears them.
- **The shared Fast Pair channel is used only for Find (ADR-068).** 8 of 8 app claims were rings or a stop; none at Connect, for the battery or for a mode change.
  Left/Right battery and *Refresh battery* come from the runtime-info stream (4 of 4 answered within 0.1 s).
- **Ring both works (ADR-070)** and **every ringing status was acknowledged (ADR-069)** — but **"Stopped on the bud" never appeared**: the app listens for at
  most 20 s and the Buds ring longer (about a minute untouched). In the user with Play, Play took the channel back after ≈ 4.8 s and received the stop itself,
  as predicted.
- **Experiment A:** the Buds accept and apply a mode that is not ticked for press and hold — the app's refusal is the app's own rule.
- **Dosimeter:** the Buds answered every request. The live value followed the phone's volume steps and stayed at 0 in silence; no sound-level meter was filmed, so
  `WELL-002` stays open. `SetWallclock` was answered (the app wrongly showed a time-out) and from then on the Buds report their clock. Writing setting 21 works on
  the session's channel and is refused on the other.
- **Not done:** TalkBack (BO-14); the Disconnect-during-Connect race (BO-16) was not hit.
- **Unexpected:** stream subscriptions survive the session, the connection and even a user switch; three screen texts still describe 1.3.x; a red error line can
  stay after the Buds allow a change again.

**Decided in chat:** release 1.4.0 with Known issues (written into `CHANGELOG.md`); five `PROTOCOL.md` notes and an ADR-060 Update (still a draft) written;
`ai-sessions/0100` will fix the ring hold, the error line, the texts and the experiment's `SetWallclock`. **Next for the maintainer:** say whether to commit
and push (below).

## Progress

- **Phase 0 started** 2026-10-18. Branch `feature/0098-round-2` at `e2b1c06` (= origin after `git fetch`; `HEAD..origin/main` empty, 0 commits);
  draft PR #69 open, draft, head `e2b1c06`, checks 2 × SUCCESS. Untracked: the prompt, the CAP-079 capture files `.gitignore` does not block, and `test` at
  the repository root (0 bytes, 2026-10-18 08:50). `ai-sessions/INDEX.md` modified (the 0099 row, "planned", written by the prompt's author).
- **Fresh listing = the prompt's §2 table:** 32 files; the first eight hex digits of every SHA-256, every line count, every `ffprobe` duration/codec/
  `creation_time` and every `capinfos` packet count agree. Differences: none in content. Modes: the four exports, four app logcats, nine films and two system
  logs are 740 (`-rwxr-----`); the rest 644. The 1.4.0 APKs under /tmp/claude-1000/apk140/ still exist with the prompt's two SHA-256.
- **Checkpoint 0 answered** (below, verbatim) and applied: `test` removed (0 bytes, not tracked — `git ls-files --error-unmatch` failed — checked
  first); `chmod 644` on the 19 files with mode 740 (all 32 now 644); the nine films, three full logcats and two system logs copied to
  ~/opencontrol-capture-originals/CAP-079/ (`cp -p`; `sha256sum -c` 14 × OK). Rename: after the clock (Phase A). Read-only adb (scratch adb-checks.log):
  one `codePath` per package, the same in user 0 and user 10 (`pm path --user 0/10`, exit 0 × 4); release 10400 / 1.4.0, `lastUpdateTime` 2026-10-18 06:42:10,
  installed in users 0 and 10; experiment 10400 / 1.4.0-exp.1, 06:42:28, installed in users 0 and 10 (user 10 `stopped=true` at the time of the check); the
  pulled base.apk files hash to **`2f75ad86…511172`** (release) and **`5280e7b3…071ab11e`** (experiment) = the prompt's two built APKs.
- **Reading block done** (list under "Files read" at the end of the session): AGENTS, PROJECT_RULES, PROJECT, ARCHITECTURE in full; PROTOCOL §0–§2.3, §4.1,
  §4.3 F, §4.4, §4.5 (intro, 4.5.3 field-12 lines, 4.5.8a, 4.5.9), §6 by keyword, §8 tail; DECISIONS ADR-032, 044, 046, 049, 059, 060 … 070 in full, the
  rest by heading; the 0098 RESULT, the CAP-079 skeleton, CAP-078 EVENT-NOTES (layout), `BudsRepositoryImpl.kt` in full (1,911 lines),
  `ExperimentRunner.kt` in full; `REVERSE_ENGINEERING.md` Dosimeter section.
- **Phase A in progress.** Film 070731 recovered on a copy (scratch bin/mdat2h264.py: AVC NAL walk of `mdat`, SPS/PPS from 070945's avcC; positive
  control on 070945's first 96,461,054 bytes: 3,458 of the 3,459 frames that start before the cut, 0 decoder errors; 070731: 3,468 frames 1280×720, 0
  decoder errors; no audio recovered — raw AAC without lengths). All nine films at 1 fps in scratch fr/ (camera 640 px, screen 540 px). **Clocks (screen
  films, status-bar minute flips at 10 fps, bin/flip.sh):** s072815 flips t = 4.8 / 64.9 / 1205.4 ⇒ 07:08:00 at 4.8, 07:28:00 at 1205.4 (start 07:07:55.2,
  end 07:28:15.6); s074935 33.2 / 453.2 ⇒ start 07:41:26.8, end 07:49:35.2; s082214 47.3 / 767.5 ⇒ start 08:09:12.7, end 08:22:15.0; s084232 7.9 / 487.9
  ⇒ start 08:33:52.1, end 08:42:33.0. (`creation_time` is not the end for 074935.) The REC chip = t + 1 s. Changed seconds (> 0.03 % of pixels below the
  status bar by > 40 levels, bin/changes.py): screen 213 / 112 / 190 / 131, camera 105 / 737 / 52 / 343 / 504. **Screen film 1 reviewed in full**
  (18 sheets, notes_s1.md). **Screen films 2–4 reviewed in full** (10 + 16 + 11 sheets; notes_s2.md … notes_s4.md). Camera sheets built (6 × 4,
  nominal clock = name / `creation_time` − duration; 5 + 31 + 3 + 15 + 22 sheets, sh_c*). The camera burns its own clock into the picture (bottom
  right); in 070945 it equals the nominal clock and the screen events within ≈ 1 s (battery (i) 07:10:39 vs 07:10:40). **Camera 070731 (recovered,
  5 sheets) and 070945 (31 sheets) reviewed in full** (notes_cam.md); **074156 (3) and 074254 (15) reviewed in full**; **080913 reviewed in full** (sheets
  0–4 at 6 × 4, the rest at 8 × 5, sh_c080913b) — **no sound-level meter is in frame at any changed second** (P7 not done). **Audio done** (notes_audio.md):
  six tracks decodable; 080913's audio has no sample description (`ffmpeg -map 0:a` exit 234); ring-tone windows and all segments ≥ −40 dB tabulated.
  **Phase A complete except the EVENT-NOTES timeline, which is written once in Phase C from these notes plus the wire (as 0097 did — stated deviation).**
- **Phase B done** (scratch notes_wire.md, wire/H*.tsv/.claims/.compact/.live; bin/msgs.py, bin/compact.py). Nesting proven (`cmp -n`); handle
  0x000b; CRC-32 716/226/890/1,214 frames, 0 bad; claims: user 10 4 (all Ring/Stop), user 0 4 app (Ring ×3, Stop) + Play; every request inventoried
  and equal to the reference bytes; exports read by template and every event line; app logcats; phone logs by filters (switches, installs, BT process,
  crashes, CloseGuard, TalkBack, Play). Headline results in notes_wire.md. Next: Phase C — extracts, hash list, folder rename, EVENT-NOTES, FINDINGS.
- **Phase C in progress.** Extracts written (scratch bin/extract.py, bin/extract_sys.py): CAP-079-logcat-extract.txt 25,526 lines, 4.2 MB (L-1/2/3 in
  disjoint windows 06:41:00 – 07:28:35.269 – 07:50:26.301 – 08:46:24; the apps' and Bluetooth PIDs from `am_proc_start` + keyword lines; a2dp/quality/
  input-focus noise and the app's hex dumps dropped), CAP-079-system-log-extract.txt 3,919 lines (Y2 05:36:00 – 05:50:16, Y4 06:30:30 – 06:42:57 UTC);
  MACs masked to XX:XX:XX:XX:…; 0 MAC, 0 e-mail, 0 SSID left; `git check-ignore` exit 1 for the system-log extract. CAP-079-adb-checks-0099.log (serial
  shortened). **Folder renamed** to captures/CAP-079-2026-10-18_07-07-31_08-42-33-Group_BO (plain `mv`, 34 files; references updated in `APP_TESTPLAN.md`,
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` and the EVENT-NOTES; the session records 0098/0099 keep the placeholder as history, as 0096/0097 did). **EVENT-NOTES
  written** (295 lines). **FINDINGS written** (§0–§18). External validation fetched 2026-10-18: the Fast Pair Device Action page ("Last updated
  2025-08-12 UTC": the timeout byte in seconds, no default; the ringing-status sync and the Seeker's ACK), pigweed.dev pw_status reference
  (FAILED_PRECONDITION = 9, "the system is not in a state required for the operation's execution"), Android `hci_error_code.h` (0x04 PAGE_TIMEOUT, 0x08
  CONNECTION_TOUT, 0x13 PEER_USER, 0x16 CONN_CAUSE_LOCAL_HOST, 0x22 LMP_RESPONSE_TIMEOUT). CAP-079-sha256sums.txt (36 files; regenerated after the last edit).
  `lint_docs.py` exit 0. **Phase C done.** Next: Checkpoint D.
- **Phase D done (except the commit).** Checkpoint D answered (below). Written: `PROTOCOL.md` five notes (§2.2a, §4.1, §4.3 Option F, §4.4, §4.5.8a) + §8 row;
  `DECISIONS.md` ADR-060 Update (draft stays Proposed); `id_registry.csv` `CAP-079` analyzed; `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group BO status and §9 row;
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` BO on `INEAR-002`, `INEAR-003`, `INEAR-006`; `APP_TESTPLAN.md` §AB results, summary row, run note; `CHANGELOG.md` [1.4.0] (the run,
  four Known issues); `README.md` 1.4.0 paragraph; `TODO.md` §1/§3/§4/§5; `ARCHITECTURE.md` §5a run note; `ai-sessions/INDEX.md` 0099 row and the 0098 back-fill.
- **Scratchpad:** /tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/c71f8691-3b62-4526-9929-49d1b46db330/scratchpad/c079

## Checkpoint 0 (chat 2026-10-18, `AskUserQuestion`, verbatim answers)

- "Bestanden" → *"Alles zoals voorgesteld (Recommended)"* — rename after the clock; `chmod 644`; CAP-079-sha256sums.txt; originals to
  ~/opencontrol-capture-originals/CAP-079/.
- "Film 070731" → *"Eigen script op kopie (Recommended)"* — a self-written script on a copy in the scratchpad (no download) extracts the H.264 from `mdat`
  with the parameter sets of film 070945; the original untouched; if it fails, option (a) (leave it).
- "OCR" → *"Alleen met het oog (Recommended)"* — no tool installed.
- "Uittreksels" → *"Beide uittreksels (Recommended)"* — CAP-079-logcat-extract.txt and CAP-079-system-log-extract.txt.
- "APK-check" → *"Ja, alleen-lezen (Recommended)"*.
- "test" → *"Verwijderen"*.

## Checkpoint D (chat 2026-10-18, `AskUserQuestion`, verbatim answers)

- "1.4.0" → *"Release met Known issues"* (against the recommendation "Eerst fixen").
- "PROTOCOL" → *"Alle vijf zoals voorgesteld (Recommended)"*.
- "ADR-060" → *"Update met deze cijfers (Recommended)"*.
- "Volgende" → *"Ja, 0100 fixes + korte run (Recommended)"*.
- "Bestanden" → *"Ja, zoals ADR-066 (Recommended)"*.
- "PR #69" → *"Draft tot 0100 klaar is (Recommended)"*.
- "Privacy" → *"Akkoord (Recommended)"*.

## Files read

- **In full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `AI_SESSION_LOG_PROCEDURE.md`, the prompt, the 0098 RESULT, the CAP-079 skeleton,
  `BudsRepositoryImpl.kt` (1,911 lines), `ExperimentRunner.kt`, `CodecRouter.kt` `routeMaestro`, the adb shell log, every changed second of the nine films, every
  event line of the four exports (hex and stream lines by template).
- **In part:** `PROTOCOL.md` (§0–§2.3, §4.1, §4.3 Option F, §4.4, §4.5 intro/§4.5.3 field 12/§4.5.8a/§4.5.9, §6 by keyword, §8 tail); `DECISIONS.md` (ADR-032, 044,
  046, 049, 059, 060 … 070 in full, the rest by heading); `REVERSE_ENGINEERING.md` (the Dosimeter section); `TODO.md` (§1, §3, §4, §5 items touched); `CHANGELOG.md`
  [1.4.0]; `APP_TESTPLAN.md` §AB and the summary; `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group BO and §9; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the Test-ID rows);
  `CAP-078-FINDINGS.md` §4 and its EVENT-NOTES (layout); the UI files of the three texts; the app logcats by tag/PID; the full logcats and system logs by filters.
- **Not read in full, against the prompt's list:** `PROTOCOL.md`, `DECISIONS.md` as wholes (the sections above before use); `RELEASING.md`; `CAP-071`'s documents
  (the layout taken from `CAP-078`'s, which follows them); `AncScreen`/`FindMyBudsScreen`/`ConnectionScreen` beyond the texts; `ProfileProxies.kt`; the test files.

## Deferred documentation (each also in `TODO.md`)

- `ai-sessions/0100` FEATURE: the ring hold (ADR-069 Update), the stale error line, the three texts, the experiment's `SetWallclock` routing (`TODO.md` §4).
- A short hardware run after 0100: BO-10/13, TalkBack, the Disconnect race, `WELL-002` with a meter (`TODO.md` §3).
- The open questions of `CAP-079-FINDINGS.md` §18 (`TODO.md` §4).
- `RELEASING.md` B–D for 1.4.0 (the maintainer's).

## Commits

On `feature/0098-round-2` after `e2b1c06`, the maintainer's "ga verder" in chat 2026-10-18: `b065a25` docs(capture) the CAP-079 folder; `4e5b17c` docs the
PROTOCOL notes, ADR-060 Update, registers and 1.4.0 known issues; `46d0815` docs the prompt, RESULT and INDEX; this back-fill. Pushed; draft PR #69 not merged.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0099_CAPTURE_RESULT_2026_10_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0099_CAPTURE_RESULT_2026_10_18
