# 0099_CAPTURE_PROMPT_2026_10_18.md — Full analysis of CAP-079 (Group BO: release candidate 1.4.0 and the experiment build 1.4.0-exp.1 — round 2, in the user without and the user with Google Play)

**Number:** 0099
**Category:** CAPTURE
**Date:** 2026-10-18
**Title:** Fully analyse `CAP-079`: **five camera recordings** (one of them unreadable as it stands) and **four Android screen recordings**, the HCI snoop logs
(eight files — successive pulls of one growing log, some nested), four app debug exports, four app logcats, three full `logcat -b all` dumps, two system logs
and one adb shell log, recorded on 2026-10-18 with **OpenControl 1.4.0** (release candidate, versionCode 10400, signed with the release key; `ai-sessions/0098`,
not published) and the debug-signed **experiment build 1.4.0-exp.1** (package `io.github.tedsluis.opencontrolpixelbuds.experiment`, never released) on a
Pixel 9a with GrapheneOS (Android 17) — parts 1 and 3 in the **secondary user 10 without Google Play**, parts 2 and 4 in the **Owner user 0 with Google Play**.
The task: establish from the films, the wire and the logs what happened; whether 1.4.0 behaves as `DECISIONS.md` ADR-067 … ADR-070 say (the mode written with
`WriteSetting 4:{13:n}`, Left/Right battery from runtime info, **no Message Stream (DLCI 0x04) claim except for Find**, the ring status and its ACK, Ring
both); what the Buds answered to the Dosimeter experiment F1–F4 and to A with a mode unticked; which skeleton steps BO-1 … BO-28 ran; whether anything on
the wire or in the logs was unexpected; record the findings; and prepare — as **proposals only** — the release verdict for 1.4.0, the FACT promotions and any
ADR drafts the maintainer needs next.
- Record the real events in the existing skeleton **CAP-079-EVENT-NOTES.md** (written by `ai-sessions/0098`; the timeline is added to it, the planned
  procedure stays as the appendix, as `CAP-078` did) and the analysis in a new CAP-079-FINDINGS.md.
- **No change to the app** in this session (fixes are **proposed**, with the evidence, for a FEATURE or fix session), **no new capture skeleton**, **no
  release step** (no `scripts/release.sh`, no tag, no `dist/` change, no publication), **no merge** of draft PR #69.

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this repository. Run the
phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Take the time to read the core files first, in full and in this order, to
understand the ground rules before analysing anything:
- `AGENTS.md` and `PROJECT_RULES.md`.
- `PROJECT.md` (scope, non-goals incl. the dated Ring-both note of 2026-10-17, the Definition of done; the status paragraph: 1.3.1 released, 1.4.0 prepared).
- `ARCHITECTURE.md`, in particular §3.1 (the settings stream and the 1.4.0 ANC path), §3.2 (the timing constants, incl. `RING_HOLD_MAX_MS`), §5a (incl. the
  round-2 note), §6.0b (one RFCOMM connection per channel across all apps; the 1.4.0 rule "the Message Stream is claimed only for Find" and its log lines), §7,
  §9, §12, §13.
- `PROTOCOL.md`, in particular §2.2a and §2.3 (the channels; channel 19 vs 21 and the ADR-034 addresses), §4.1 with every Update and the 2026-10-17
  implementation note, §4.3 Option F (runtime info 6.1/6.2/6.3), §4.4 (the ring group, `FIND-001` … `FIND-005`, Ring both), §4.5 (settings 12, 13, 21),
  §4.5.8a and §2.2a's Dosimeter notes, §6 (the open questions), §8.
- `DECISIONS.md`: **every** ADR, ADR-001 … ADR-070, with every dated Update, and the draft ADR-060 (the Dosimeter — not a decision). In particular ADR-067 … ADR-070
  (round 2, accepted 2026-10-17) with ADR-067's Note on field 12, ADR-064/065 (the settings stream), ADR-066 (what of a capture is committed), ADR-032/044 (the
  claim and the re-open), ADR-049/059 (Settable `00`, the worn line), ADR-006 (no scanning).
- `TODO.md`, §1, §2, §3 (`CAP-079`), §4, §5, §6.

These are the ground rules; do not analyse or change anything before they are read. Then, per task:

- **Procedure and registers:** `AI_SESSION_LOG_PROCEDURE.md` (in full, incl. §4b, §5 and §9), `ai-sessions/INDEX.md`, `id_registry.csv` (the `CAP-078`,
  `CAP-079`, ADR-066 … ADR-070 rows and every Test-ID of §3 item 13), `RELEASING.md` (in full: the verdict after a hardware run decides steps B–D, which are
  the maintainer's).
- **The builds under test and the skeleton — read in full:** `ai-sessions/0098_FEATURE_PROMPT_2026_10_17.md` and `ai-sessions/0098_FEATURE_RESULT_2026_10_17.md`
  (Checkpoint 1's approved decisions and sends, verbatim; the log strings; the mutation table; the gate; the APK hashes of the gate build); **CAP-079-EVENT-NOTES.md**
  (the skeleton: P0–P7, the reference-frame table, the log lines to look for, BO-1 … BO-28 with the expected screen and wire and "Refuted if", the run card);
  `APP_TESTPLAN.md` §AB; `CHANGELOG.md` `[1.4.0]` (its Known issues are predictions to test); the Group BO block of `CAPTURE_BLUETOOTH_HCI_SNOOP.md`; `WELL-001` …
  `WELL-003` and the other Test-IDs of §3 item 13 in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`.
- **The code, in full, before any export line is interpreted** (every export line is matched to its log string): `BudsRepositoryImpl.kt` (`writeAncMode`,
  `stepAncMode`, `readAncSetting`, the runtime-info branch, `refreshBattery`, `ringBud`/`stopRinging`/`startRingHold`/`onRingStatus`, `openSession`'s generation
  check, `awaitWriteOrQuarantine`, every `logConnectionEvent`); `codec/SettingFrame.kt`, `codec/RuntimeInfo.kt`, `codec/RingFrame*.kt`, `codec/CodecRouter.kt`,
  `codec/PwRpc.kt`; `AncTileService.kt`; the `:ui` screens a finding touches (`AncScreen`, `FindMyBudsScreen`, `ConnectionScreen`); `ProfileProxies.kt`; under
  `android/data/src/experiment/` `ExperimentRunner.kt` (`ExperimentFrames`, `dosimeterLine`, `describeFields`, the listener's per-minute count) and under
  `android/app/src/experiment/` the `ExperimentHook` card; `ExperimentFramesTest`, `Cap078Fixtures.kt`.
- **The "before":** `captures/CAP-078-2026-10-17_16-23-51_17-17-41-Group_BN/CAP-078-FINDINGS.md` and its EVENT-NOTES (the method, the clocks, the HCI nesting,
  the extracts and their commands, experiment A's 22 sends, the ring status, Play's cut 4.5–4.9 s); `ai-sessions/0097_CAPTURE_RESULT_2026_10_17.md` (the method
  and the scratch scripts — re-create them, they are not in the repository; `scripts/pwhdlc_crc_table.py`, `scripts/rpc_request_answers.py` and
  `scripts/pwrpc_decode.py` are); the Dosimeter captures `CAP-027` (frames 898–923, 1107–1229) and `CAP-058` (5736–5753, `CAP-058-FINDINGS.md` §6),
  `CAP-041` 783 (`SetWallclock`), and `REVERSE_ENGINEERING.md`'s Dosimeter section of 2026-10-09 (`qir`/`qia` field numbers, `hiw.java:32–37`).
- **The maintainer's named examples:** `captures/CAP-071-2026-10-08_17-51-29_18-27-28-Group_BG/CAP-071-EVENT-NOTES.md` and `…/CAP-071-FINDINGS.md` (the
  layout: metadata table, P0/P1 outputs, pre-flight, review method and privacy, one timeline row per event, step mapping, Test-ID traceability).

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.**
- Create the RESULT file ai-sessions/0099_CAPTURE_RESULT_2026_10_18.md at the very start, with `**Status:** partial — resumed` and a **Progress** block.
- Update that block at the end of every phase **and** after every substantial step: every minute of each film reviewed, every audio segment tabulated, every
  HCI log and channel decoded, every log file read, every experiment send traced, every checkpoint answer. Say what is done and what is next, which files are
  touched but not yet verified, and where the intermediate results live (the scratchpad: scripts, contact sheets, decoded tables, notes — re-create them if
  it is gone).
- A resumed session reads this prompt, then the RESULT's Progress block, re-reads the files the unfinished step touched, and continues from there. It never
  redoes a finished, recorded step and never assumes an unrecorded one was done. The prompt keeps its number and date; the RESULT grows in the same file.

**Memory and the machine.** The laptop has 15 GB (≈ 5 GB available when this prompt was written) and `/tmp` is a 7.7 GB tmpfs; Claude Code was OOM-killed
during long sessions. Run `tshark`/`ffmpeg` **one job at a time**; extract frames and crops in ranges, never a whole film into memory (the largest camera film
is 873 MB, 1,111 s); the three full logcats (24.6–26.8 MB, 188,680–200,857 lines each) and the two system logs (12.2–12.5 MB, ≈ 100,000 lines each) —
`grep`/`awk` them, never read one whole into context. No Gradle build is needed (if one is, `--max-workers=1`). Never `git worktree add` a commit without a
sparse checkout that excludes `captures/` (gigabytes). Scratch files go into a fresh `mktemp -d` under `/tmp/claude-1000/`; write their names in documents
without backticks (the docs lint flags backticked names that do not exist). Tools present (checked 2026-10-18): `tshark`, `ffmpeg`, `ffprobe`; **absent**:
`tesseract` (OCR), `untrunc`, `MP4Box`, `mediainfo`, any speech-to-text — installing one is a download, so the maintainer decides (Checkpoint 0).

**Nothing is taken on trust, including this prompt, the maintainer's statements, the 0098 RESULT and the memory notes.** Every claim and every number in §2
and §3 is re-derived from the recordings, the HCI logs, the other logs, the code and official documentation.

---

## 1. What the maintainer asked for (chat, 2026-10-18, translated from Dutch)

1. "Make a new prompt for Claude Code, in English, per the project's rules in `ai-sessions/`, that can be run in a new session. You are an expert software
   architect, reverse engineer and technical auditor. Your task is a comprehensive analysis of `CAP-079` (Group BO) of the OpenControl app, recording the
   findings. Take the time to read the core files first (`AGENTS.md`, `PROJECT_RULES.md`, `PROTOCOL.md`, `ARCHITECTURE.md`, `DECISIONS.md`) to understand the
   ground rules before auditing the rest. Run everything in phases, sequentially; when you hit a token limit you must be able to continue automatically
   later."
2. **The maintainer's file list and statements about the run (verify each; label each as the maintainer's statement until verified):**
   - **Part 1, user 10 (no Google Play):** CAP-079-btsnoop_hci1.log.last, CAP-079-btsnoop_hci1.log, CAP-079-recording_20261018_070731.mp4,
     CAP-079-recording_20261018_070945.mp4, CAP-079-screen-20261018-072815-1792300074640.mp4, CAP-079-opencontrol-debug-20261018-072742.txt,
     CAP-079-OpenControl-for-Pixel-Buds-Pro-2-log-e6d5930222ee.txt, CAP-079-logcat-all-1.txt.
   - **Part 2, user 0 (Google Play):** CAP-079-btsnoop_hci2.log.last, CAP-079-btsnoop_hci2.log, CAP-079-recording_20261018_074156.mp4,
     CAP-079-recording_20261018_074254.mp4, CAP-079-screen-20261018-074935-1792302086542.mp4, CAP-079-opencontrol-debug-20261018-074943.txt,
     CAP-079-OpenControl-for-Pixel-Buds-Pro-2-log-6e1f3e6952b9.txt, CAP-079-System-log-180018fac7f6.txt, CAP-079-logcat-all-2.txt.
   - **Part 3, user 10:** CAP-079-btsnoop_hci3.log.last, CAP-079-btsnoop_hci3.log, CAP-079-recording_20261018_080913.mp4,
     CAP-079-screen-20261018-082214-1792303752702.mp4, CAP-079-opencontrol-debug-20261018-082157.txt, CAP-079-OpenControl-for-Pixel-Buds-Pro-2-log-aa5039cbef3d.txt.
   - **Part 4, user 0:** CAP-079-btsnoop_hci4.log.last, CAP-079-btsnoop_hci4.log, CAP-079-screen-20261018-084232-1792305231948.mp4,
     CAP-079-opencontrol-debug-20261018-084224.txt, CAP-079-OpenControl-for-Pixel-Buds-Pro-2-log-44863b57e090.txt, CAP-079-System-log-fbd720889bab.txt,
     CAP-079-logcat-all-3.txt.
   - Also: CAP-079-EVENT-NOTES.md (the skeleton) and CAP-079-adb-shell.log.
   - *The measured times (§2) do not match this split everywhere: several `.last` files are earlier pulls that contain the previous part (or `CAP-078`), and
     part 4 has no camera film. Settle the assignment from the files.*
   - "The OpenControl apps are the newest versions **1.4.0 and 1.4.0-exp.1**." — *the app logcats' `package:` headers, the shell log's `dumpsys` lines, the
     Info tab on film (build `e2b1c06`, not "-dirty", expected).*
   - "The capture was made on a Pixel 9a with GrapheneOS Android 17." — *the `osVersion` headers.*
   - "**During the videos I did not speak.** On the video you can see when I put a bud in my ear or take it out: if my head is on the **left** of the frame it
     is my **right** ear; if my head is on the **right** of the frame it is my **left** ear." — *the bud events come from the camera films (with this rule,
     stated by the maintainer — record it as such) and the wire; the audio tracks still carry the rings, the ANC changes and the sound levels of the Dosimeter
     part — tabulate them, attribute nothing to speech.*
   - "**I did not always keep to the test plan.**" — *map every skeleton step BO-1 … BO-28 as done / not done / done differently; a step not done is "expected
     but not observed" in the Test-ID traceability, not a failure.*
3. **The analysis steps the maintainer named (§4 follows them):** first the recordings — the maintainer wrote "both CAP-079-recording*.mp4 and CAP-079-screen-*.mp4
   videos": read as **all nine films** (five camera, four screen) → CAP-079-EVENT-NOTES.md with every event and action and its time; all CAP-079-btsnoop_hci*
   logs with `tshark`; all app logcats (the maintainer wrote "CAP-075-OpenControl-for-Pixel-Buds-Pro-2-log-*.txt" and, later, "AP-075-…" — read as `CAP-079`'s, the
   only ones in the folder); all CAP-079-opencontrol-debug-*.txt exports; the system logs (the maintainer wrote "the" system log — there are two); the full
   logcats (the maintainer wrote "CAP-079-logcat-all.txt" — there are three); then correlate the timeline with every log (`CAP-071-EVENT-NOTES.md` as the
   example); a comprehensive CAP-079-FINDINGS.md (`CAP-071-FINDINGS.md` as the example); "try to get as many findings as possible out of the captures, also
   things you are not looking for or do not expect".
4. Strict rules (the maintainer's): no assumptions — verify everything; validate claims externally against official documentation where needed (Android
   Developer Docs, the Bluetooth Core Specification, the Fast Pair specification); **no sampling** — a 100 % complete review of the relevant files; evidence
   traceability (every claim links to a frame, a log line or a film time) and structural integrity.
5. Standing practice: no commit or push without the maintainer's "yes" in chat; no release step; `main` takes no direct push (`RELEASING.md`); no FACT
   promotion and no ADR without the maintainer's answer in this chat (`AGENTS.md` §6).

---

## 2. The evidence (measured 2026-10-18 by the session that wrote this prompt — re-derive every number)

**State of the repository:** branch **`feature/0098-round-2`** at **`e2b1c06`**, pushed; **draft PR #69** ("do not merge before `CAP-079` is analysed"; CI
green, 2 of 2); draft PR #68 closed (superseded by #69). This session's commits go **on `feature/0098-round-2`** after `e2b1c06` (analysis and documentation
only). Untracked: the capture files `.gitignore` does not block, and **`test`** at the repository root (0 bytes — not a capture file, not this project's: ask
at Checkpoint 0, never delete it unasked). `.gitignore` blocks the nine films (`captures/**/*.mp4`), the three full logcats (`*logcat-all*`) and the two system
logs (`*System-log*`) — ADR-066; confirmed with `git check-ignore -v` (lines 77–79).

**The APKs built for the run (`ai-sessions/0098` continuation, chat 2026-10-18, from the clean tree at `e2b1c06`; not committed):** 1.4.0 SHA-256
`2f75ad86273ac04d27ab57f2b0a8c95d56d4a1af8e17b8136108fc20cd511172` (the release key, certificate SHA-256 `a7530f5c…d8dcb` = `README.md`'s), 1.4.0-exp.1
`5280e7b37210181170007e1c45ce47779d122c3847cde5d52ce85465071ab11e` (the debug key) — both under /tmp/claude-1000/apk140/ if the tmpfs still holds them; the 0098
RESULT's hashes are of an earlier gate build from a modified tree, not of these. `BuildConfig.GIT_COMMIT` = `e2b1c06` in both.

The folder `captures/CAP-079-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BO/` (the skeleton's placeholder name) holds, per `stat`, `sha256sum`, `ffprobe`, `capinfos` and
`grep` of 2026-10-18 (the first eight hex digits of each SHA-256; times local, CEST = UTC+2, unless marked UTC):

| File | Measured | Notes |
|---|---|---|
| CAP-079-EVENT-NOTES.md | 14,965 B, `8736016b…`, identical to the commit | the skeleton: add the timeline, keep the procedure as the appendix |
| CAP-079-adb-shell.log | 2,355 B, `43a8e023…`, 56 lines | 06:26–06:46: the versions before (1.3.1-exp.1, 10301), P0's `pm list packages`, the four `adb install --user 10/0 -r` "Success", the versions after (10400, 1.4.0-exp.1, `lastUpdateTime` 06:42:10 / 06:42:28) — see below |
| CAP-079-btsnoop_hci1.log.last | 2,369,292 B, `be4728e1…`, 37,707 packets, **2026-10-17 17:07:42.39 – 2026-10-18 07:01:08.48** | **`CAP-078`'s user-0 log CAP-078-btsnoop_hci3.log (526,312 B) is a byte prefix of it** (`cmp -n`): the rest is the night (27,368 packets 17:20 → 06:30) and the morning up to 07:01 |
| CAP-079-btsnoop_hci1.log | 276,665 B, `6e39b135…`, 6,112 packets, 07:01:12.96 – 07:30:21.78 | part 1 — not a prefix of anything here |
| CAP-079-btsnoop_hci2.log.last | 80,332 B, `eb520d32…`, 1,071 packets, 07:36:38.80 – 07:41:36.47 | after a restart at the user switch? |
| CAP-079-btsnoop_hci2.log | 429,255 B, `dab17fca…`, 6,614 packets, **07:41:38.77** – 07:52:25.22 | **a byte prefix of …hci3.log.last** |
| CAP-079-btsnoop_hci3.log.last | 495,659 B, `c6487f0d…`, 7,618 packets, **07:41:38.77** – 08:00:20.10 | the superset of hci2.log: part 2 up to the switch to user 10 (08:00:17.895, below) |
| CAP-079-btsnoop_hci3.log | 225,918 B, `0d9c425b…`, 4,899 packets, **08:00:22.82** – 08:24:44.88 | part 3; **a byte prefix of …hci4.log.last** |
| CAP-079-btsnoop_hci4.log.last | 239,730 B, `ac02d47f…`, 5,126 packets, **08:00:22.82** – 08:30:45.38 | the superset of hci3.log: part 3 up to the switch to user 0 (08:30:44.705) |
| CAP-079-btsnoop_hci4.log | 495,653 B, `874e2b83…`, 8,455 packets, 08:30:49.65 – 08:45:00.52 | part 4 |
| CAP-079-recording_20261018_070731.mp4 | 96 MB, `c8169e41…` | **`ffprobe`: "moov atom not found — Invalid data"** — unreadable as it stands (a truncated recording?); by its name 07:07:31 → before 07:09:45 |
| CAP-079-recording_20261018_070945.mp4 | 873 MB, `f70a0470…`, **1,111.32 s**, audio AAC; `creation_time` 05:28:17Z | if the stamp is the end: ≈ 07:09:46 – 07:28:17 (the name says 07:09:45 — derive it from the status bar) |
| CAP-079-recording_20261018_074156.mp4 | 41 MB, `14c16bf3…`, 53.03 s, AAC; 05:42:49Z | ≈ 07:41:56 – 07:42:49 |
| CAP-079-recording_20261018_074254.mp4 | 310 MB, `ab0469f1…`, 396.05 s, AAC; 05:49:30Z | ≈ 07:42:54 – 07:49:30 |
| CAP-079-recording_20261018_080913.mp4 | 617 MB, `c4a62a53…`, 788.75 s; 06:22:22Z; **its audio stream has codec "unknown" (tag `[0][0][0][0]`)** | ≈ 08:09:13 – 08:22:22 — the camera film of the Dosimeter part (BO-22 … BO-27): can the audio be decoded at all? |
| CAP-079-screen-20261018-072815-1792300074640.mp4 | 124 MB, `530733cd…`, 1,220.44 s, **no audio stream**; 05:28:15Z | the Pixel 9a's screen recording, ≈ 07:07:55 – 07:28:15 |
| CAP-079-screen-20261018-074935-1792302086542.mp4 | 100 MB, `617145d8…`, 488.43 s, AAC; 05:49:40Z | ≈ 07:41:32 – 07:49:40 |
| CAP-079-screen-20261018-082214-1792303752702.mp4 | 181 MB, `4ada1895…`, 782.22 s, AAC; 06:22:29Z | ≈ 08:09:27 – 08:22:29 |
| CAP-079-screen-20261018-084232-1792305231948.mp4 | 181 MB, `fb7df376…`, 520.78 s, AAC; 06:42:41Z | ≈ 08:34:00 – 08:42:41 — **part 4 has no camera film**: BO-28's sound-level meter is not on film |
| CAP-079-opencontrol-debug-20261018-072742.txt | 1,302 lines, `6c0d3023…`, 07:02:59.798 – 07:27:28.316 | first line "Permissions (start): … GRANTED"; 0 "Experiment" lines; "Noise-control mode written" 20, "not written" 4, "Ring status from the Buds" 5, "acknowledged" 5, "hold ended" 3, "request failed" 5, "EQ written" 0 |
| CAP-079-opencontrol-debug-20261018-074943.txt | 448 lines, `fdbbb916…`, 07:42:05.104 – 07:49:39.663 | written 2, ring status 4, acknowledged 4, hold ended 3, failed 1 |
| CAP-079-opencontrol-debug-20261018-082157.txt | 2,174 lines, `f73b4168…`, 08:04:14.930 – 08:21:57.045 | starts with an ADR-044 re-open ("trigger: LINK_BACK"); Experiment lines: A 12, F 626, F1 3, F2 32, F3 8, F4 8; "live-stream packets in the last 60 s": 13, 11, 6, … |
| CAP-079-opencontrol-debug-20261018-084224.txt | 2,948 lines, `6905f3d6…`, 08:31:18.091 – 08:42:24.214 | F 737, F1 3, F2 26, F3 8, F4 8, **A 0**; the per-minute count **120, 120, 120, …** — against part 3's 6–13: why? |
| CAP-079-OpenControl-for-Pixel-Buds-Pro-2-log-e6d5930222ee.txt | 854 lines, `72bd1625…`; `package: io.github.tedsluis.opencontrolpixelbuds:10400`, `userType: full.secondary`; **UTC** 05:01:09 – 05:27:51 | part 1 (release app) |
| CAP-079-OpenControl-for-Pixel-Buds-Pro-2-log-6e1f3e6952b9.txt | 422 lines, `a9053681…`; the release package, no `userType` line; UTC 05:36:36 – 05:49:50 | part 2 |
| CAP-079-OpenControl-for-Pixel-Buds-Pro-2-log-aa5039cbef3d.txt | 1,664 lines, `551848eb…`; `….experiment:10400`, `full.secondary`; UTC 06:04:14 – 06:22:35 | part 3 |
| CAP-079-OpenControl-for-Pixel-Buds-Pro-2-log-44863b57e090.txt | 1,484 lines, `0f460149…`; the experiment package, no `userType`; UTC 06:31:17 – 06:42:48 | part 4 |
| CAP-079-logcat-all-1.txt | 24.6 MB, `c250b3fb…`, 200,857 lines, local time, last line 10-18 07:28:35 | part 1 — **never committed in full** (an extract only, ADR-066) |
| CAP-079-logcat-all-2.txt | 25.4 MB, `12cbb147…`, 195,049 lines, last 07:50:26 | part 2; the three dumps overlap (each starts 10-08 21:47) — define the window each one covers |
| CAP-079-logcat-all-3.txt | 26.8 MB, `613b86f8…`, 188,680 lines, last 08:46:23 | parts 3–4; `am_switch_user: 10` at **08:00:17.895**, `am_switch_user: 0` at **08:30:44.705** |
| CAP-079-System-log-180018fac7f6.txt | 12.5 MB, `ce209f8d…`, 101,298 lines, **UTC**, last 05:50:15 (= 07:50:15) | part 2 — never committed (ADR-066), an extract if needed |
| CAP-079-System-log-fbd720889bab.txt | 12.2 MB, `c131e1b8…`, 96,443 lines, UTC, last 06:42:56 (= 08:42:56) | part 4 |

**Clocks:** the app logcats and the system logs print **UTC**; the full logcats and the exports print **local time (UTC+2)** — re-derive with one shared line (as
`CAP-078` did with an `am_proc_start`). The films' `creation_time` looks like the **end** of each film (070945: 07:09:45 + 1,111 s = 07:28:16 vs 05:28:17Z) —
prove it with the status bar per film. Convert every time to local phone time in the documents.

**The adb shell log (re-read every line):** 06:26:18 the experiment package was still 1.3.1-exp.1 (`lastUpdateTime` 2026-10-16 10:30:27); 06:27:47 the
release app 10301; P0 at 06:28:27/06:28:49 used `grep -i -E "gms|vending"` (not the skeleton's anchored pattern): user 10 → the three
`app.grapheneos.gmscompat*` packages only, `exit=0` (**GrapheneOS's compatibility layer matched — not a Play positive**); user 0 → those three plus
`com.android.vending` and `com.google.android.gms`. 06:42:03 → the four installs, each "Success" (so the experiment build's debug key matched the one of
1.3.1-exp.1); 06:46:11 `versionCode=10400`, `lastUpdateTime=2026-10-18 06:42:10`; 06:46:17 `versionName=1.4.0-exp.1`, 06:42:28. **Not in the log:** the
`sha256sum -c` of P1 and the per-user `dumpsys` (the `dumpsys` output is not per user) — the full logcat's `PackageManager` lines 06:42–06:43 decide in which
users each APK was installed.

**User switches and Play markers (leads):** `am_switch_user: 10` 08:00:17.895 and `: 0` 08:30:44.705 (logcat-all-3); the part 1 → part 2 switch (≈ 07:36?) is
in logcat-all-2 / the system log — find it. Frames containing Play's Message Stream marker `03 08 00 02 01 25` (`tshark -Y 'frame contains 03:08:00:02:01:25'`):
hci1.log 0, hci2.log 7, hci4.log.last 0, hci4.log 3 — re-run with a positive control (`CAP-076-btsnoop_hci1.log`: 18) and per user.

**First look, to be re-derived (leads, not findings):**
- Part 1: 20 "Noise-control mode written", 4 "not written" (BO-5, no bud worn?), 5 ring statuses all acknowledged, 3 holds ended, 5 "request failed" — which
  failures (the hold list BO-8? a Find claim?), and **no "EQ written"** (P1 was not exercised — or EQ was not touched).
- Part 2 (Play): 2 writes, 4 ring statuses acknowledged — **did the app hear the stop status in user 0** (the CHANGELOG's Known issue predicts Play cuts the hold
  ≈ 4.5–5 s in)?
- Part 3: the export starts with an automatic re-open, not a Connect tap; A 12 lines (BO-21); F2 sent on both channels; live counts 6–13/min.
- Part 4: **120 live-stream packets per minute** (2 per second) against part 3's 6–13 — two subscriptions (19 + 21) both streaming, a Play-side
  subscription (does Play's companion code subscribe too?), or a different Buds state? The `SERVER_STREAM` packets per channel and call on the wire decide.
- The release decoder's own pw_rpc summary lines name the Dosimeter as `method=0x73d5d805/0x4d93b6e2` (no name in `PwRpc`'s table) — expected; the
  "Experiment F:" lines name them.

---

## 3. Leads and context — verify each before relying on it

1. **The parts, the users and the logs:** settle which HCI file, export, app logcat, system log, full logcat and film belongs to which user and part — by time,
   the `am_switch_user` lines, the `userType` header, the Play markers with a positive control. Prove the nesting (hci2.log ⊂ hci3.log.last, hci3.log ⊂
   hci4.log.last, CAP-078-btsnoop_hci3.log ⊂ hci1.log.last — `cmp -n`); analyse the non-redundant set **in full** (hci1.log.last's part after `CAP-078`'s
   prefix, hci1.log, hci2.log.last, hci3.log.last, hci4.log.last, hci4.log) and say that it covers every packet of all eight files; the overnight part of
   hci1.log.last (17:20 → 06:30): anything to or from the Buds (their handle, or LE to their address — `AGENTS.md` §13's CLI hygiene), or only other devices'
   advertisements (then: counted, not analysed, not quoted — privacy). The gaps at each switch (07:30:21 → 07:36:38, 07:41:36 → 07:41:38, 08:00:20 → 08:00:22,
   08:30:45 → 08:30:49): a snoop restart, a Bluetooth restart, a user switch?
2. **The builds and the apps:** which app wrote each export (the package in the matching app logcat, the PIDs, the first lines); the Info tab's build string on
   film (`e2b1c06`); in which users each APK was installed (the shell log, `PackageManager`). Whether the release app and the experiment app ever ran at the
   same time (P6, "one app at a time"; `ARCHITECTURE.md` §6.0b) — `am_proc_start`/`am_proc_died`/`am_kill` of both packages, the tile service's bound starts,
   any second RFCOMM connection on the wire.
3. **The clock offsets:** each film's status bar (the minute changes at 10 fps) and a shared event with an export line or a frame; settle each film's start and
   end with its uncertainty before any correlation. The camera films and the screen films overlap — align them with each other too.
4. **The unreadable film recording_20261018_070731.mp4:** "moov atom not found". **Checkpoint 0 asks** the maintainer: (a) leave it — the 2 minutes before
   07:09:45 come from the screen film and the logs; (b) attempt a recovery on a **copy** in the scratchpad — `untrunc` with recording_20261018_070945.mp4 (same
   camera, same settings) as the reference (a download/build of an open-source tool, so the maintainer decides; nothing of the film leaves the machine); (c) the
   maintainer re-copies it from the camera if the original is intact. Never modify the original.
5. **The audio:** the maintainer did **not** speak. Tabulate each track anyway (`ffmpeg … silencedetect`, RMS per 100 ms, tonal vs broadband per segment —
   `ai-sessions/0092`'s method): the rings (a tonal burst at a fixed pitch — onset and end against the ring status `…00` on the wire, BO-10 … BO-13, BO-19), the
   ANC changes if audible, the Dosimeter levels (silence, speech, music low/high — BO-24) against the meter on film. recording_20261018_080913.mp4's audio stream
   has an unknown codec: say whether `ffmpeg` decodes it; if not, the levels come from the meter on film only. screen-072815 has no audio stream.
6. **The bud events:** from the camera films with the maintainer's rule (head on the left of the frame = the right ear; head on the right = the left ear) —
   in, out, into the case, on the table, per bud, with film time; and from the wire (runtime info 6.2/6.3 charging/in-case flags, setting 13 = 0 "no mode
   settable", the Buds' own session closes and re-opens, `PROTOCOL.md` §2.2a). A bud event with no wire trace, or a wire trace with no film event, is an open
   question.
7. **ADR-067 — the mode through setting 13 (BO-3 … BO-9, BO-18):** per write: the frame byte for byte against the reference table (channel 19 or 21, CRC-32),
   the `RESPONSE` status, the push(es) `4:{13:n}` (value = n?), any DLCI 0x04 traffic in the same second (**expected none**), the mode on screen, the line
   "Noise-control mode written (channel N): <mode>"; with both / one / no bud worn (status 9 → the "don't allow" text, BO-5), in-ear detection off (BO-6), the
   tile (BO-7: which modes it stepped through; P2 — any "unavailable" frame between two states, at 10 fps on the screen film), the hold list (BO-8: Off
   unticked — the field-12 write, the disabled button and the hint on film, **no `4:{13:1}` on the wire** while Off was unticked), the holds (BO-9: "Changed by
   the Buds"), user 0 (BO-18: who gets the `Notify` — Play?). Count: writes, OK, status 9, other statuses, timeouts, pushes, screen changes.
8. **ADR-068 — the battery and "no DLCI 0x04 except for Find" (BO-1, BO-2, BO-18):** the full claim inventory of DLCI 0x04 (and 0x05) with the sender of every
   `SABM`/`UA`/`DISC`/`DM`, per session and per user — **every app claim must be a Find action** (Ring/Stop); any other app claim refutes ADR-068's
   consequence. The Connect sequence per session against 1.3.1's (`CAP-077`) minus the snapshot claim; `SubscribeRuntimeInfo` at Connect and per Refresh
   battery (BO-2: one request, one packet within 2 s?); the battery card on film against 6.2/6.3 field 1 per packet; an entry absent from a packet ("last seen").
9. **ADR-069/070 — the ring (BO-10 … BO-13, BO-19):** every Ring/Stop frame, the Buds' ACK, every Buds status `04 01 00 01 xx` and the phone's ACK
   `ff 01 00 02 04 01` after each (only while DLCI 0x04 is open), the hold's start and end ("Ring: claim held …", "Ring: hold ended — <why>") against the wire
   (the `DISC` after the linger), "Stopped on the bud at …" on film only after a stop the app did not send (BO-10, BO-13) and never after its own Stop (BO-11);
   Ring both (BO-12): the ACK, the status `03`, both buds heard; the Buds' own ring time-out (BO-13: `CAP-078` saw 14.8–17.1 s); user 0 (BO-19): Play's cut
   (the time from the Ring to Play's `SABM`/the app's loss), "the claim was closed by another client", whether Play ACKed the stop, and the screen afterwards
   (the Known issue: "Ringing …" until Stop).
10. **The 1.3.1 steps never exercised before:** BO-14 TalkBack (A93-APP-07: each switch read with its label, the Bass slider in dB, the tabs once, Find's
    buttons — the screen film and its audio; TalkBack speaks — **the screen film of part 1 has no audio**: then the TalkBack focus rectangle and caption on film
    only); BO-15 a hold within ≈ 1 s of a Connect (A93-APP-04: a `4:{13:n}` push between a `ReadSetting 4:13` and its `RESPONSE` — find every such interleave
    on the wire); BO-16 a Disconnect during a Connect (A68-APP-13: "Connect finished after a Disconnect tap: its channels are closed again" and the `DISC`s).
11. **The Dosimeter experiment (F1–F4, BO-21 … BO-28; draft ADR-060, `WELL-001` … `WELL-003`):** every request byte for byte against the reference table
    (F1's time against the phone's clock on film and the log line "the phone's clock N ms"), every answer and every `SERVER_STREAM` packet decoded
    (`scripts/pwrpc_decode.py`; the "Experiment F:" lines against the wire, packet for packet); the live stream's field 2 per packet as a time series per channel,
    aligned with the meter readings on the part-3 camera film (OCR is not installed — read the meter's display frame by frame where the stream changes, and at
    a fixed interval; state the interval and the uncertainty), the four levels (silence, speech, music low/high) and the buds worn; whether `round(10·log10(v))`
    (`hiw.java:32–37`, the APK — not this project's claim) tracks the meter: a correlation reported as numbers, never as a health or dB claim beyond what the
    data shows; F3's day entries before/after (`qia` field 1/6) and whether the current day's entry moved; F1's effect on them (`WELL-003`); F4's answers and
    any push of field 21 (and channel 19's never-captured form — its answer); the per-minute counts (part 3 vs part 4); the cost (packets per minute, bytes).
    **No unit and no health wording in any committed text** (the 0098 guardrail).
12. **A with a mode unticked (BO-21, ADR-067's Note):** the field-12 state at the time (the last read/write of field 12 on the wire), the `WriteSetting 4:{13:4}`
    and the Buds' answer (OK and switched? an error status?) — this answers what the app's refusal guards against.
13. **Test-IDs** (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`; `AGENTS.md` §13 step 7): the skeleton's list — `ANC-001` … `ANC-004`, `ANC-007`, `INEAR-001`, `INEAR-004`,
    `BATT-004`, `FIND-001`, `FIND-002`, `FIND-004`, `FIND-005`, `TOUCH-007`, `PAIR-003`, `WELL-001`, `WELL-002`, `WELL-003` — and any other the run actually
    exercised (e.g. `INEAR-002`/`003` with the film's ear rule); "expected but not observed" for each that was not; the step mapping BO-1 … BO-28 (done / not
    done / done differently).
14. **Unexpected on the wire and in the logs (the maintainer's standing point — look for it deliberately):** anything on DLCI 0x08/0x0a (positive control
    `CAP-029` 1592), any request not in the 1.4.0 Connect sequence or the experiment table, a write the user did not make, a write of settings 3 or 6 (**must be
    0**), an `UnidentifiedFrame` in an export, CRC failures, LE traffic to the Buds, HFP AT commands, AVRCP pass-through, every disconnect reason, the Buds'
    own session closes, anything `scripts/pwrpc_decode.py` cannot classify, crashes/ANRs/`FATAL EXCEPTION`/StrictMode of either package (`grep -c` with a
    positive control), `CloseGuard` lines (the late-proxy fix), the SIGQUIT of `CAP-077`, Play services' Fast Pair lines in user 0 around each ring.
15. **Privacy:** the camera films show the room, the maintainer and possibly a screen; the screen films may show the Wi-Fi name, notifications and the serial
    numbers (allowed on film, never unredacted in text — first 4 + last 2); the audio may carry other voices — quote nothing private; the logcats print
    `XX:XX:XX:XX:…`-style addresses; the full logcats and the system logs match e-mail/SSID patterns thousands of times — **commit extracts only**
    (CAP-079-logcat-extract.txt, and a CAP-079-system-log-extract.txt only if wanted — `.gitignore`'s `*System-log*` must not match its name), each with its
    command and line count; scan every committed file for e-mail, Wi-Fi name, MAC and full serials before staging.
16. **External validation** where a claim rests on a specification or platform behaviour (fetch, quote with the date): the Fast Pair Message Stream / Device
    Action page (the ring group, its ACK, ring both, ringing-status sync); Pigweed's pw_rpc packet types and status codes (`FAILED_PRECONDITION` = 9); the
    Bluetooth Core Specification for disconnect reasons and RFCOMM `DISC`/`DM`; Android's TalkBack/accessibility semantics only where BO-14's result needs it;
    the MP4 container's `moov` atom (ISO/IEC 14496-12) if the film's recovery is discussed.

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename, mode change, delete or `git add`)

1. Create the RESULT (§0). Record `git log -1`, `git status --short`, `git fetch`, `git log --oneline HEAD..origin/main`, the state of draft PR #69. Work on
   **`feature/0098-round-2`** (`git switch feature/0098-round-2 && git pull --ff-only`); never on `main`; never rebase or amend.
2. List the folder fresh (`ls -la`, `sha256sum`), diff it against §2's table and say every difference.
3. **Checkpoint 0 (`AskUserQuestion`, one question per decision, options with pros and cons, "(Recommended)" on one, the exact commands in the preview):** the
   folder rename to the first/last phone times of the run (`CAP-079-2026-10-18_HH-mm-ss_HH-mm-ss-Group_BO`, and every reference to the placeholder path in the
   docs); `chmod 644` on the files with mode 740; the CAP-079-sha256sums.txt (every file incl. the local-only ones); the originals to
   ~/opencontrol-capture-originals/CAP-079/ (the films, the full logcats, the system logs — ADR-066); the extracts; **the unreadable film (§3 item 4: a / b / c)**;
   whether a tool may be installed for the meter readings (OCR) — or frame-by-frame reading by eye only; whether the phone can be attached for read-only checks
   (`pm path --user N`, `adb pull`, `sha256sum` against §2's hashes); **`test`** at the repository root (leave, or remove on the maintainer's word).
4. Update the `CAP-079` row of `id_registry.csv` (status `planned` now; `analyzed` in Phase D) and prepare the Group BO status line of
   `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (written in Phase D).

### Phase A — the films and their audio, in full

5. Decode each film at 1 frame per second into the scratchpad in ranges (≈ 1,111 + 53 + 396 + 789 camera seconds; 1,220 + 488 + 782 + 521 screen seconds, plus
   whatever a recovered 070731 yields); compare each second with the previous one; view **every** second with a change on labelled contact sheets; narrow
   transitions at 10 fps where a claim depends on them (the mode buttons and the hint line after each write, the tile in the shade, the battery card, the ring
   notice and "Stopped on the bud at …", the (i) dialogs, the Info tab's build string, the Debug card and its confirmation dialogs, TalkBack's focus, the
   meter's display, the user switch, the "Debug log saved"). No sampling: every second is either viewed or proved unchanged.
6. **Measure each film's clock offset** (status-bar minute changes at 10 fps; a shared event with an export line) with its uncertainty; align the camera and the
   screen films of the same part. Confirm the audio streams with `ffprobe`.
7. **The audio, in full:** for each film with a decodable track, `silencedetect` and RMS per 100 ms over the whole track; every non-silent segment tabulated with its
   phone time, duration and class (tonal — a ring?, music, speech from a speaker, click); align each with the screen and the wire.
8. Write the timeline into the existing CAP-079-EVENT-NOTES.md: phone time · what happened · step BO-n / Test-ID · evidence — in `CAP-071`/`CAP-078`'s layout
   (the status line becomes "captured 2026-10-18 … and analyzed …", Log Metadata filled in, the capture-integrity pre-flight incl. the HCI files' nesting, the
   switches and the unreadable film, the review method and privacy, the Event Timeline incl. "before the recording" and "after the recording", the step mapping
   BO-1 … BO-28 done / not done / done differently, Test-IDs per `AGENTS.md` §13 step 7 with "expected but not observed"; the planned procedure kept below as the
   appendix).

### Phase B — the HCI logs and the other logs, in full

9. The HCI logs (§3 item 1's non-redundant set, which covers all eight): Connection Complete events and the Buds' handle (by address; show the filter matching a
   known frame), the RFCOMM inventory per DLCI and direction, **every pw_hdlc frame CRC-32-checked** (with its positive control; a payload holding several HDLC
   frames split on `7e`), every Message Stream message decoded, **the full claim inventory of DLCI 0x04/0x05** with the sender and the owner of every claim, the
   Play-marker count per user with its positive control, **every 1.4.0 request (the mode writes, the reads, the runtime-info subscriptions, the field-12 writes,
   the ring frames and ACKs) and every experiment request (A, F1–F4) byte for byte against the reference table with its answer and everything that followed it
   within 30 s**, every `4:{13:n}` push with what preceded it, the Connect sequence per session against `CAP-077`'s, every other write, AVDTP/AVRCP state, LE
   traffic to the Buds, every disconnect reason; **and a deliberate search for the unexpected** (§3 item 14). Commands and exit statuses in FINDINGS
   (`PROJECT_RULES.md` rule 4a; `AGENTS.md` §13 step 8 for every negative).
10. The four exports and the four app logcats: every line read (1,302 + 448 + 2,174 + 2,948; 854 + 422 + 1,664 + 1,484) — every export line matched to the
    code's log string (the "Experiment …" lines to `ExperimentRunner.kt`; the 1.4.0 lines to `BudsRepositoryImpl.kt`/`AncTileService.kt`), the sessions,
    subscriptions and reads per session, every "from the settings stream (<cause>)" line against its packet, the claim lifetimes, the session losses and
    re-opens, the connect failures, the five "request failed" lines of part 1; the app logcats' `events` lines; crashes/ANRs/StrictMode (`grep -c` with a positive
    control).
11. The adb shell log (every line), the three full logcats and the two system logs: `grep`/`awk` by time window and PID — the 06:42 installs (in which users),
    the user switches, both apps' processes and the tile service, the Bluetooth process around every disconnect, every ring and every experiment, Play
    services' Fast Pair lines in user 0, TalkBack's service during BO-14, `CloseGuard`, any crash. Write each extract command and its line count; commit
    extracts only (Checkpoint 0).

### Phase C — correlation, the findings, the proposals

12. Correlate every timeline row with its frames and log lines. Write CAP-079-FINDINGS.md in `CAP-071`/`CAP-078`'s structure: §0 the builds (both APKs, the build
    string, the installs), the users, the parts, the logs and their nesting, the unreadable film, privacy; §1 ACLs and **the claim inventory (ADR-068's "only
    Find")**; §2 the Connect sequence per session; **§3 the mode through setting 13 (ADR-067): every write and its answer per condition, the tile, the hold
    list, the holds, user 0**; **§4 the battery (ADR-068): runtime info against the card, Refresh battery**; **§5 the ring (ADR-069/070): every status and ACK,
    the holds, "Stopped on the bud", Ring both, the time-out, user 0 against Play**; §6 the mode line (P6), P1, P2; **§7 TalkBack (BO-14)**; **§8 the hold during a
    Connect (BO-15)**; **§9 the Disconnect during a Connect (BO-16)**; **§10 experiment A with a mode unticked**; **§11 the Dosimeter F1–F4 (requests, answers,
    the stream against the meter, the day entries, the rates of parts 3 and 4)**; §12 sessions, losses and re-opens; §13 playback and audio; **§14 unexpected
    findings**; §15 the skeleton's expectations and "Refuted if" applied literally per step run; §16 the counts; **§17 the verdict proposal for 1.4.0** (release
    as built / release with Known issues — the CHANGELOG's predicted ones confirmed or not — / fix first; each item with its evidence) and the proposals for
    `PROTOCOL.md` (FACT promotions or 🟡 notes, texts marked "proposal — not accepted"), for ADR-060 (the Dosimeter: what the run settles of its 🟡 items — a
    draft rewrite only as a proposal), and for the next session; §18 open questions. FACT / HYPOTHESIS / ASSUMPTION / OPEN QUESTION on every conclusion
    (`PROJECT_RULES.md` §1); every claim with a frame, a log line or a film time.
13. Validate externally (§3 item 16): fetch, quote with the date.

### Phase D — checkpoint, documentation, finish

14. **Checkpoint (`AskUserQuestion`, one question per decision):** the 1.4.0 verdict (release as built / with Known issues / fix first — the evidence in the
    preview); per finding, the proposed `PROTOCOL.md` update (a 🟢 FACT promotion or a 🟡 note — the draft text in the preview; `AGENTS.md` §6: nothing written
    without the maintainer's answer in this chat); ADR-060 (unchanged, an Update with this run's numbers, or a rewritten draft — still not accepted unless the
    maintainer says so); whether a fix or FEATURE prompt follows (title and scope in the preview); the privacy treatment; whether the small capture files are
    committed now (ordinary git files, ADR-066) on `feature/0098-round-2`; what happens to draft PR #69 (stays a draft until the release steps, or is marked
    ready for review). Record the answers verbatim in the RESULT.
15. With the answers: `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 (the `CAP-079` row: the real times, analyzed) and the Group BO status line; `id_registry.csv`;
    `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the Group column: BO only on the Test-IDs exercised); `APP_TESTPLAN.md` §AB (the Result column per row, the Summary row,
    a run note); `CHANGELOG.md` `[1.4.0]` (the hardware run and the Known issues as measured — the block stays "not yet released"); `README.md`'s 1.4.0 paragraph;
    `PROTOCOL.md` and `DECISIONS.md` only as approved; `TODO.md` §1/§2/§3/§4/§5; `ARCHITECTURE.md` §5a where the run settled something. Then
    `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py` (exit 0).
16. The RESULT: plain-language summary first (what happened on the phone in each part, what 1.4.0 did against each ADR, what the Dosimeter showed, which steps did
    not run, what was unexpected, what the maintainer decides next), the phases, the files read in full / in part, "Deferred documentation" (each also in
    `TODO.md`), "Commits"; the `ai-sessions/INDEX.md` row (0099; the 0098 row's Status gets the back-fill "hardware run `CAP-079` analysed by 0099").
17. Show a short summary and **ask whether to commit and push**. Only after a yes: commits per concern (Conventional Commits: `docs(capture)`, `docs`), each with
    its *why* and the attribution line; ordinary git files for the small capture files (ADR-066), never the films, the full logcats or the system logs; on
    `feature/0098-round-2`; `git fetch`; push without force; never merge.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Scope:** analysis and documentation only. No change under `android/` (fixes are proposals), no new skeleton, no release step (`RELEASING.md` B–D are the
  maintainer's), no `dist/` change, no tag, no merge, no rebase.
- **Approvals only in this chat** (`AGENTS.md` §6): no 🟢 FACT promotion, no new ADR or ADR Update, no draft ADR in `DECISIONS.md` without the maintainer's
  consent here; ADR-060 stays a draft. A behaviour is reported from this run's evidence — never from the maintainer's impression, the 0098 expectations or the
  unit tests; a step that did not run is "not exercised", not "passed".
- **No new wire traffic:** this session sends nothing to the Buds. If the phone is attached (Checkpoint 0), only read-only `adb` commands (`pm path`, `pull`,
  `dumpsys`, `logcat -d`) — never an install, an `am start` of either app, or a Bluetooth action.
- **Evidence:** every claim cites a frame (log and number), a log line or a film time; a negative needs its command, its exit status and a positive control; a
  hypothesis names its settling experiment. The maintainer's statements (incl. the ear rule) are statements until verified.
- **No sampling:** every second of every readable film and audio track, every packet of the non-redundant HCI set on the Buds' handle (with the nesting proof),
  every line of the four exports, the four app logcats and the shell log — reviewed or covered by a stated, positive-controlled filter; the full logcats and the
  system logs by stated filters over the run's full time window.
- **The Dosimeter:** raw values and correlations only — no unit, no dose, no health wording in any committed text; nothing about the maintainer's hearing.
- **Privacy:** no serial unredacted, no MAC, no phone number, no account name or e-mail, no Wi-Fi name, nothing private from the films or the audio in any
  committed text; the films, the full logcats and the system logs are never committed (ADR-066); extracts only.
- **Files:** before moving, renaming or deleting anything, diff a fresh listing; never wildcard deletes; ask first; never modify an original film (recovery only
  on a copy). `android/domain/bin/` is never staged.
- **Subagents** may only read and report; every write is done and verified in the main session; never two `tshark`/`ffmpeg` jobs at once.

## 6. Deliverables

- CAP-079-EVENT-NOTES.md (the timeline added to the skeleton) and CAP-079-FINDINGS.md (the parts, builds and logs settled; ADR-067 … ADR-070 checked on the wire
  and on film; TalkBack, the hold and the Disconnect during a Connect; experiment A and the Dosimeter F1–F4 with their evidence and labels; the unexpected; the
  step mapping; the counts; the 1.4.0 verdict proposal), with the folder and files as approved in Checkpoint 0 (incl. the extracts and the hash list).
- The registry, index, test-plan, `CHANGELOG.md`, `README.md`, `TODO.md`, protocol and decision updates as approved; a fix or FEATURE-prompt proposal if one
  follows.
- ai-sessions/0099_CAPTURE_RESULT_2026_10_18.md and the `ai-sessions/INDEX.md` row.

---

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0099_CAPTURE_PROMPT_2026_10_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0099_CAPTURE_PROMPT_2026_10_18
