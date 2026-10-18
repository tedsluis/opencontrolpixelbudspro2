# 0097_CAPTURE_RESULT_2026_10_17.md — Full analysis of CAP-078 (Group BN: the experiment build 1.3.1-exp.1, round 1 — five untested requests, in the user without and the user with Google Play)

**Number:** 0097
**Category:** CAPTURE
**Date:** 2026-10-17
**Title:** Full analysis of `CAP-078` (Group BN), per `ai-sessions/0097_CAPTURE_PROMPT_2026_10_17.md`
**Status:** complete

## Summary (plain language)

The maintainer ran round 1 with the experiment build on 2026-10-17: in the user without Google Play (16:24–16:58) and in the Owner user with it (17:09–17:17).
The release app was never opened, so part 1 of the plan did not run as written. Two of its steps were done in the experiment app instead, which has the same
1.3.1 code.

- **A — switching noise control through setting 13 works without the Message Stream.** 22 writes; 18 answered OK, and each time the Buds reported back exactly
  the mode written. Four were refused with `FAILED_PRECONDITION`, all four while no bud was worn. In the Play user, Play services (holding the Message Stream)
  was told of every change by the Buds. Whether each switch was audible is not measured (speech timed, not transcribed).
- **B — the Buds report when ringing starts and stops** (`04 01 00 01 xx`), once, to whichever app holds the Message Stream. Without Play the experiment app got
  all four stop reports; with Play, Play cut the app off about 4.5 s into each ring and received and acknowledged the stop reports itself.
- **C — "Ring both" (`0x03`) is accepted locally** and reported as status 3; a 19-s tone followed. This contradicts ADR-027's reasoning; the maintainer decides in
  round 2.
- **D — `SubscribeToOobeActions` answers** with "left on head", "right on head" at once, then ends with status 14 after 15.7 s; no events for the outs/ins.
- **E — settings 8, 9, 10 are not served** by this firmware (status 2, value 0, 15 of 15).
- **1.3.1 on hardware:** the kept "changed since" cause works (twice on film); Cancel in "Use different Buds" survives a rotation. Not exercised: TalkBack, a
  push during a read.
- **Unexpected:** the app classes its own setting-13 writes as "changed by the Buds"; the Bluetooth process was killed for a deadlock two minutes before the run.

**Next for the maintainer:** say whether to commit and push (below); then `ai-sessions/0098` (round 2, release 1.4.0) is to be written.

## Progress

- **Phase 0:** started 2026-10-17. Branch `feature/0096-experiment-build` at `3bb3bf9` (= origin; `HEAD..origin/main` empty); draft PR #68 open, head
  `3bb3bf9`. Untracked: the CAP-078 capture files and `captures/test` (0 bytes).
- **Phase 0 done.** Fresh listing = the prompt's §2 table (sizes, modes, hashes, all 18 files). Checkpoint 0 answered (below, verbatim). Done on the answers:
  `captures/test` removed (0 bytes, checked first); `chmod 644` on the nine 740 files; films, full logcat and system log copied to
  ~/opencontrol-capture-originals/CAP-078/ (`sha256sum -c` 4 × OK). Read-only adb checks → CAP-078-adb-checks-0097.log: the experiment APK on the phone
  (one path, both users) = **`4e5facfa…eadd422`** (= 0096's); installed **user 10 2026-10-16 10:30:10, user 0 10:30:27**; the release app 1.3.1 in both users,
  `lastUpdateTime 2026-10-16 14:56:46` (the shell log said 08:56:17 — a later update; find it in the full logcat). "-dirty": 15 of 17 string literals of
  `0292e59`'s `ExperimentRunner.kt` found verbatim in the APK's 17 DEX files; the other two are a KDoc comment (not compiled) and "setting 13 takes 1…4"
  (found up to the non-ASCII "…", which `strings` splits); `git diff 0292e59 HEAD -- android/` = the test fix only. Rename: after the clock (Phase A).
- **Phase A done (films and audio).** Film 1: 2,077 frames at 1 fps (405 px), 441 changed seconds (> 0.03 % of pixels) viewed on 56 sheets, 1,636
  pixel-identical or below the threshold; film 2: 471 frames, 107 changed seconds on 14 sheets. Notes: notes_film1.md, notes_film2.md (scratchpad).
  **Clock** (status-bar minute flips at 10 fps, scripts bin/flips1fps.py + bin/flip.sh): film 1 flips at t = 69.0, 189.0, 609.0, 1209.0, 1989.0 ⇒ phone =
  **16:23:51.0 + t** (± 0.1 s), covers 16:23:51.0 – 16:58:28.1 (`creation_time` 16:59:15 is 47 s after the end); film 2 flips at t = 9.1, 249.1, 429.1 ⇒
  **17:09:50.9 + t**, covers 17:09:50.9 – 17:17:41.7 (`creation_time` 7 s after). Cross-check: film 1 s1162 toast "Debug log saved (664 lines)" = 16:43:13;
  app logcat "Debug log exported (664 lines)" 14:43:12.832 UTC. The REC chip counter = t + 1 s. Folder renamed (Checkpoint 0) to
  `captures/CAP-078-2026-10-17_16-23-51_17-17-41-Group_BN` (`git mv`, 19 files). **Audio:** a1.wav/a2.wav (16 kHz mono); segments ≥ −40 dB tabulated
  (audio1_segments.txt 51, audio2_segments.txt 17; median −85.8 dB per 100 ms); speech-like segments (f0 ≈ 120–130 Hz) e.g. 16:33:58, 16:34:47–16:35:54,
  16:37:05–16:37:31, 16:53:31–16:53:54, 17:12:04–17:12:32, 17:14:50, 17:15:24, 17:15:44 — words not transcribed (Checkpoint 0). **A 3.14 kHz tone**
  (bin/tone.py: band 3,080–3,200 Hz ≥ 15 dB over its neighbours, per 100 ms) in 1.1–1.5-s pulses about 3 s apart: film 1 16:54:37–16:54:46,
  16:55:11–16:55:21, 16:55:43–16:55:53, 16:56:12–16:56:19, 16:56:46–16:57:05; film 2 17:14:35–17:14:42, 17:15:08–17:15:15, 17:15:39–17:15:44,
  17:16:11–17:16:18, 17:16:43–17:17:05 (tone1.txt, tone2.txt) — the ring windows of B1/B2/C (to be matched to the wire in Phase B).
  Deviation (stated): the EVENT-NOTES timeline is written once, in Phase C, from these notes plus the wire, instead of a film-only draft first.
- **Phase B done.** Scratch notes_wire.md holds every result with frame numbers. Buds ACL handle `0x000b` (Connection Complete by address `…:cf:6e:07`):
  user 10 170 16:25:12.652 → 10039 17:07:38.487 (`0x16`); user 0 192 17:07:45.656 → 1408 17:09:23.436 (`0x13`), 2229 17:10:44.868 → open; the 220-packet
  `.last` has no ACL. pw_hdlc on DLCI 0x02: 667 + 277 frames, CRC-32 944/944 (bin/hdlc.py; positive control: the reference frame passes, a flipped bit fails).
  DLCI 0x04 claims (bin/claims.py): user 10 22 claims, 0 Play markers; user 0 29 claims, 15 Play (positive control `CAP-076-btsnoop_hci1.log` 18).
  Exports: line templates counted over all 1,949 lines (`cut -c14- | sed` → 84 templates) and every non-hex template read with its lines; app logcats: the
  release app's tile-service processes logged 0 app-tag lines (experiment PIDs 13701/18853: 729). Phone logs: the Bluetooth process was killed for a main-thread
  deadlock at 16:19:34 (`am_wtf`, before the run); one Bluetooth process per user switch; the 2026-10-16 install lines are no longer in the ring buffer (the
  dates exist: 15,309 lines of 10-16); Play's NearbyFastPair lines are format strings without values. Extracts written: CAP-078-logcat-extract.txt (16,360
  lines, 2.8 MB: 10-17 16:19:00–16:59:07, the apps' and Bluetooth PIDs, CDM, user switches) and CAP-078-system-log-extract.txt (3,452 lines, 0.6 MB: 15:07–15:19
  UTC, likewise plus Play's Fast Pair PID 11869); full MACs masked to `XX:XX:XX:…` (0 left); 0 e-mail, 0 Wi-Fi name, 0 serials (the "ssid" hits are `mRssiDelta`).
- **Phase C done.** CAP-078-FINDINGS.md (§0–§15) and CAP-078-EVENT-NOTES.md (status, metadata, pre-flight, review method, the timeline, step mapping, Test-IDs;
  the skeleton kept as the appendix) written; CAP-078-sha256sums.txt (20 files). External validation fetched 2026-10-17 (the Device Action and Acknowledgement
  pages of the Fast Pair specification; pigweed.dev pw_status). Checked against the committed scripts: `scripts/pwhdlc_crc_table.py` 667/277 frames, 0 bad;
  `scripts/message_stream_claims.py` 22/29 claims. `lint_docs.py` exit 0.
- **Phase D done (except the commit).** Checkpoint D answered (below). Written: `PROTOCOL.md` §4.1 Update, §4.4 Update, §6 Note + new item, §8 row (all
  2026-10-17, approved); `id_registry.csv` `CAP-078` analyzed; `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group BN status and the §9 row; `APP_TESTPLAN.md` §AA results,
  summary row, run note; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` — no change (every exercised Test-ID already carries BN); `TODO.md` §1 (the Buds' state, round 2) and
  §4 (the open items), finished items removed; `ARCHITECTURE.md` §5a run note; `ai-sessions/INDEX.md` 0097 row and the 0096 back-fill; scripts
  `scripts/ring_tone_windows.py` and `scripts/rpc_request_answers.py` (rule 4a; checked against the scratch results). `ensure_footers.py`; `lint_docs.py` exit 0.
- **Done.** Committed and pushed (see Commits).
- **Scratchpad:** /tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/aa9a8659-d019-41cc-a793-56b16454fad0/scratchpad/c078 (scripts, sheets, tables).

## Checkpoint 0 (chat 2026-10-17, `AskUserQuestion`, verbatim answers)

- "Spraak" → *"Alleen timen (Recommended)"* — speech segments are timed and classed; the bud events come from the wire.
- "Bestanden" → *"Alles zoals voorgesteld (Recommended)"* — rename after the clock; `chmod 644`; CAP-078-sha256sums.txt; originals to
  ~/opencontrol-capture-originals/CAP-078/; both identical `.last` files kept.
- "APK-check" → *"Ja, alleen-lezen (Recommended)"*.
- "captures/test" → *"Verwijderen"*.
- "-dirty" → *"Strings en frames (Recommended)"* — no rebuild.
- "Uittreksels" → *"Beide uittreksels (Recommended)"* — CAP-078-logcat-extract.txt and CAP-078-system-log-extract.txt.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0097_CAPTURE_RESULT_2026_10_17.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0097_CAPTURE_RESULT_2026_10_17

## Checkpoint D (chat 2026-10-17, `AskUserQuestion`, verbatim answers)

- "A → PROTOCOL" → *"🟢 FACT-notitie (Recommended)"*.
- "B/C → §4.4" → *"🟢 FACT B + C (Recommended)"*.
- "D/E → §6" → *"🟢 wire-notities (Recommended)"*.
- "ADR-027" → *"Besluit in ronde 2 (Recommended)"* — no ADR text now; round 2 brings a draft superseding ADR-027 for "both".
- "Ronde 2" → *"Ja, prompt 0098 (Recommended)"*.
- "Commit" → *"Ja, alles + scripts (Recommended)"* — the small capture files, the documents and the analysis scripts on `feature/0096-experiment-build`; the push
  after the maintainer's "yes" at the end.
- "PR #68" → *"Draft tot ronde 2 (Recommended)"*.

## Files read

- **In full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md` (the sections from Goal to the Definition of done), the prompt, the CAP-078 skeleton, `ai-sessions/0096`'s
  RESULT (earlier in this conversation), `ExperimentRunner.kt` literals at `0292e59`, `scripts/pwrpc_decode.py`'s tables and frame parser, the adb shell log,
  every changed second of both films.
- **In part:** `PROTOCOL.md` §4.1 (end), §4.4 (whole section), §6 (the items on settings 8/9/10, the three pushes per write), §8 (table end); `DECISIONS.md` ADR-066
  (whole) and ADR-027 through `PROTOCOL.md`/`PROJECT.md`; `ARCHITECTURE.md` §3.1 (the cause rule) and §5a (the experiment note); `REVERSE_ENGINEERING.md` (the
  `qhp` entry); `TODO.md` §1 and the round-1 items; `AI_SESSION_LOG_PROCEDURE.md` §3, §4, §9; `CAP-077-FINDINGS.md` (layout and §0); the exports by template
  with every non-hex template read; the app logcats by PID/tag; the full logcat and the system log by stated filters only.
- **Not read in full, against the prompt's reading list:** `ARCHITECTURE.md`, `PROTOCOL.md` and `DECISIONS.md` as wholes (1.1 MB together) — the sections
  above were read before they were used or changed.

## Deferred documentation

- The open items of `CAP-078-FINDINGS.md` §14 — in `TODO.md` §4 ("From `CAP-078` … open").
- ADR-027 and the ANC-through-setting-13 ADR — round 2 (`TODO.md` §1, `ai-sessions/0098`).

## Commits

On `feature/0096-experiment-build` after `3bb3bf9` (the prompt), the maintainer's "yes" in chat 2026-10-17 (*"Ja, commit en push (Recommended)"*):
`b2b641b` docs(capture) the CAP-078 folder; `2b7eeea` docs the protocol and registers; `fdadd2c` docs the RESULT, INDEX and scripts; this back-fill. Pushed;
draft PR #68 not merged.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0097_CAPTURE_RESULT_2026_10_17.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0097_CAPTURE_RESULT_2026_10_17
