# 0097_CAPTURE_PROMPT_2026_10_17.md — Full analysis of CAP-078 (Group BN: the experiment build 1.3.1-exp.1, round 1 — five untested requests, in the user without and the user with Google Play)

**Number:** 0097
**Category:** CAPTURE
**Date:** 2026-10-17
**Title:** Fully analyse `CAP-078`: **two** Android screen recordings (both **with** an audio track, the maintainer speaking), the HCI snoop logs (six files —
three of them successive pulls of one growing log), three app debug exports, three app logcats, one full `logcat -b all`, one system log and one adb shell
log, recorded on 2026-10-17 with the debug-signed **experiment build 1.3.1-exp.1** (`ai-sessions/0096`; build type `experiment`, package
`io.github.tedsluis.opencontrolpixelbuds.experiment`, never released; APK SHA-256 `4e5facfa3b6178f518750578f3e1ccc5de169103fd619120ac21740eceadd422`; the
maintainer reports the Info tab's build as **`61a59b9-dirty`**) beside the released 1.3.1 on a Pixel 9a with GrapheneOS (Android 17) — parts 1–2 in the
**secondary user 10 without Google Play**, part 3 in the **Owner user 0 with Google Play**. The task: establish from the films, the wire and the logs what
happened; what the Buds answered to each of the five experiments (A `WriteSetting 4:{13:n}`, B the ring status and the phone's ACK, C Ring both `0x03`,
D `SubscribeToOobeActions`, E `ReadSetting 4:8/9/10`); which steps of the skeleton ran and which did not; whether anything on the wire was unexpected; record
the findings; and prepare — as **proposals only** — the FACT promotions and ADR drafts the maintainer needs for round 2 (release 1.4.0).
- Record the real events in the existing skeleton **CAP-078-EVENT-NOTES.md** (written by `ai-sessions/0096`; the timeline is added to it, the planned
  procedure stays as the appendix, as `CAP-076`/`CAP-077` did) and the analysis in a new CAP-078-FINDINGS.md.
- **No change to the app** in this session (improvements are **proposed**, with the evidence, for the round-2 FEATURE session), **no new capture skeleton**,
  **no release step** (no tag, no `dist/` change), **no merge** of draft PR #68.

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this repository. Run the
phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Take the time to read the core files first, in full and in this order, to
understand the ground rules before analysing anything:
- `AGENTS.md` and `PROJECT_RULES.md`.
- `PROJECT.md` (scope, non-goals, the Definition of done; the status paragraph: 1.3.1 released 2026-10-16).
- `ARCHITECTURE.md`, in particular §3.1 (the settings stream), §5a (incl. the experiment-build note of `ai-sessions/0096`), §6.0b (one RFCOMM connection per
  channel across all apps), §7, §9, §12, §13.
- `PROTOCOL.md`, in particular §2.2a and §2.3 (the channels; DLCI 0x04 or 0x05; channel 19 vs 21 and the ADR-034 addresses), §4.1 with every Update (setting 13
  = the mode, pushed on `SubscribeToSettingsChanges`), §4.4 (Find My Buds / Ring action, the ring group `0x04`, `FIND-001` … `FIND-005`), §4.5.5 (in-ear detection), §6 (every open question
  the experiments address: settings 8/9/10, `SubscribeToOobeActions`, Ring both, the ring status/ACK, `WriteSetting 4:13`), §8.
- `DECISIONS.md`: **every** ADR, ADR-001 … ADR-066, with every dated Update, and the draft ADR-060 (not a decision). In particular ADR-064/065 (the settings
  stream), ADR-066 (what of a capture is committed), ADR-032/044 (the claim and the re-open), ADR-049 (Settable `00`), ADR-057, ADR-006 (no scanning).
- `TODO.md`, §1 (round 1 → `CAP-078` → round 2), §2, §4, §5, §6.

These are the ground rules; do not analyse or change anything before they are read. Then, per task:

- **Procedure and registers:** `AI_SESSION_LOG_PROCEDURE.md` (in full, incl. §4b, §5 and §9), `ai-sessions/INDEX.md`, `id_registry.csv` (the `CAP-077`,
  `CAP-078`, ADR-066 rows and every Test-ID of §3 item 12), `RELEASING.md` ("Repository rules"; no release step applies — the experiment build is never
  released).
- **The build under test and its skeleton — read in full:** `ai-sessions/0096_FEATURE_PROMPT_2026_10_16.md` and `ai-sessions/0096_FEATURE_RESULT_2026_10_16.md`
  (Checkpoint 1's approved sends, verbatim; the reference frames; the APK hash; the CI test fix); **CAP-078-EVENT-NOTES.md** (the skeleton: P0–P7, the
  reference-frame table, BN-1 … BN-19 with the expected screen and wire and "Refuted if", the run card); `APP_TESTPLAN.md` §AA; the new Test-IDs `ANC-007`,
  `ANC-008`, `INEAR-007` in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` and the Group BN block of `CAPTURE_BLUETOOTH_HCI_SNOOP.md`.
- **The experiment code, in full, before any export line is interpreted** (every "Experiment …" line is matched to its log string): under `android/data/src/experiment/`
  `ExperimentRunner.kt` and `ExperimentFrames.kt`; under `android/app/src/experiment/` the `ExperimentHook` card; `android/app/src/noExperiment/`; the
  `experiment` build type in `android/app/build.gradle.kts` and `android/data/build.gradle.kts`; `ExperimentFramesTest`; the "(experiment seam)" members of
  `BudsRepositoryImpl.kt` and `MaestroReply`. Then the 1.3.1 sources a finding touches (`BudsRepositoryImpl.kt`'s stream/read/claim paths and every log
  string, `codec/CodecRouter.kt`, `RfcommBudsTransport.kt`, `SessionReopener.kt`, `AncTileService.kt`, `BudsCompanionPairing.kt`).
- **The "before":** `captures/CAP-077-2026-10-16_06-56-14_07-10-38-Group_BM/CAP-077-FINDINGS.md` and its EVENT-NOTES (the 1.3.1 Connect sequence, the method,
  the logcat extract and its command, the open questions — the three stream packets after a write, the SIGQUIT); `ai-sessions/0095_CAPTURE_RESULT_2026_10_16.md`
  (the method and the scratch scripts — re-create them, they are not in the repository); the ring captures `CAP-025` (frames 2040, 2048, 2131) and every
  other capture `PROTOCOL.md` §4.4 cites for the ring group; for the Play user the claim analysis of `CAP-074`/`CAP-075` (Play's re-claim 5.2–5.7 s).
- **The maintainer's named examples:** `captures/CAP-071-2026-10-08_17-51-29_18-27-28-Group_BG/CAP-071-EVENT-NOTES.md` and `…/CAP-071-FINDINGS.md` (the
  layout: metadata table, P0/P1 outputs, pre-flight, review method and privacy, one timeline row per event, step mapping, Test-ID traceability).

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.**
- Create the RESULT file ai-sessions/0097_CAPTURE_RESULT_2026_10_17.md at the very start, with `**Status:** partial — resumed` and a **Progress** block.
- Update that block at the end of every phase **and** after every substantial step: every minute of each recording reviewed, every audio segment tabulated,
  every HCI log and channel decoded, every log file read, every experiment send traced, every checkpoint answer. Say what is done and what is next, which
  files are touched but not yet verified, and where the intermediate results live (the scratchpad: scripts, contact sheets, decoded tables, notes —
  re-create them if it is gone).
- A resumed session reads this prompt, then the RESULT's Progress block, re-reads the files the unfinished step touched, and continues from there. It never
  redoes a finished, recorded step and never assumes an unrecorded one was done. The prompt keeps its number and date; the RESULT grows in the same file.

**Memory and the machine.** The laptop has 15 GB and `/tmp` is a 7.7 GB tmpfs; Claude Code was OOM-killed during long sessions. Run `tshark`/`ffmpeg` one job
at a time; extract contact sheets and crops in ranges, never a whole film into memory (film 1: 434 MB, 2,077 s; film 2: 105 MB, 471 s); the full logcat
(25.0 MB, 158,238 lines) and the system log (11.5 MB, 78,700 lines) — `grep`/`awk` them, never read either whole into context. No Gradle build is needed
(if one is, `--max-workers=1`). Never `git worktree add` a commit without a sparse checkout that excludes `captures/` (gigabytes). Scratch files go into a fresh
`mktemp -d` under `/tmp/claude-1000/`; write their names in documents without backticks (the docs lint flags backticked names that do not exist).
`python3 scripts/pwrpc_decode.py <log>` decodes the pw_rpc frames.

**Nothing is taken on trust, including this prompt, the maintainer's statements, the 0096 RESULT and the memory notes.** Every claim and every number in §2
and §3 is re-derived from the recordings, the HCI logs, the other logs, the code and official documentation.

---

## 1. What the maintainer asked for (chat, 2026-10-17, translated from Dutch)

1. "Make a new prompt for Claude Code, in English, per the project's rules in `ai-sessions/`, that can be run in a new session. You are an expert software
   architect, reverse engineer and technical auditor. Your task is a comprehensive analysis of `CAP-078` (Group BN) of the OpenControl app, recording the
   findings. Take the time to read the core files first (`AGENTS.md`, `PROJECT_RULES.md`, `PROTOCOL.md`, `ARCHITECTURE.md`, `DECISIONS.md`) to understand the
   ground rules before auditing the rest. Run everything in phases, sequentially; when you hit a token limit you must be able to continue automatically
   later."
2. **The maintainer's file list and statements about the run (verify each; label each as the maintainer's statement until verified):**
   - The maintainer assigned CAP-078-btsnoop_hci1.log, …hci1.log.last, …hci2.log.last, the exports `-164310`/`-165715`, the app logcats `-8bfbd9598ce5`/
     `-4b3358e819e1`, CAP-078-logcat-all-1.txt and CAP-078-recording1.mp4 to **user 10 (parts 1 and 2)**, and CAP-078-btsnoop_hci2.log, …hci3.log.last, the
     export `-171718`, CAP-078-recording2.mp4, the app logcat `-02c37baa0fc4` and CAP-078-System-log-a5697a5cda0e.txt to **user 0 (part 3)**. *The measured
     times (§2) do not match this split for the HCI logs — and CAP-078-btsnoop_hci3.log is not in the list at all; settle the assignment from the files.*
   - "The OpenControl app is the newest version **1.3.1-exp.1 build 61a59b9-dirty**." — *the Info tab on film; the app logcats' `package:` header; the shell
     log's `dumpsys`; the APK on the phone against `4e5facfa…` if the phone can be attached (Checkpoint 0). "-dirty" means the build was made from a tree with
     uncommitted changes on top of `61a59b9` (the 0096 prompt commit): say whether the committed `0292e59` holds that tree — the export's experiment strings
     and frames against `0292e59`'s sources, and, only if the maintainer wants it at Checkpoint 0, a rebuild of `0292e59` compared class by class.*
   - "The capture was made on a Pixel 9a with GrapheneOS Android 17." — *the app logcats' and the system log's `osVersion` header.*
   - "During the videos I spoke now and then to say whether I put a bud in my left or right ear, or when I gave a long press on a bud." — *both films have an
     audio track (§2); see §3 item 4 for how speech can be used without a transcription tool.*
   - "**I do not have the putting-in of the buds on camera.**" — *in-ear and out-of-ear events come from the wire (the Message Stream in-ear/wear messages,
     runtime info, the Settable flags) and the speech, not the film; a bud event with no wire trace is an open question, not a finding.*
   - "**I did not always keep to the test plan.**" — *map every skeleton step BN-1 … BN-19 as done / not done / done differently; a step not done is "expected
     but not observed" in the Test-ID traceability, not a failure.*
3. **The analysis steps the maintainer named (§4 follows them):** first both recordings → CAP-078-EVENT-NOTES.md with every event and action and its time; all
   CAP-078-btsnoop_hci* logs with `tshark`; all app logcats (the maintainer wrote "CAP-075-OpenControl-for-Pixel-Buds-Pro-2-log-*.txt" — read as `CAP-078`'s,
   the only ones in the folder); all CAP-078-opencontrol-debug-*.txt exports; the system log; the full logcat; then correlate the timeline with every log
   (`CAP-071-EVENT-NOTES.md` as the example; the maintainer's "AP-075-…" is read as `CAP-078`'s app logcats); a comprehensive CAP-078-FINDINGS.md
   (`CAP-071-FINDINGS.md` as the example).
4. Strict rules (the maintainer's): no assumptions — verify everything; validate claims externally against official documentation where needed (Android
   Developer Docs, the Bluetooth Core Specification, the Fast Pair specification); **no sampling** — a 100 % complete review of the relevant files; evidence
   traceability (every claim links to a frame, a log line or a film time) and structural integrity.
5. Standing practice: no commit or push without the maintainer's "yes" in chat; no release step; `main` takes no direct push (`RELEASING.md`); no FACT
   promotion and no ADR without the maintainer's answer in this chat (`AGENTS.md` §6).

---

## 2. The evidence (measured 2026-10-17 by the session that wrote this prompt — re-derive every number)

**State of the repository:** branch **`feature/0096-experiment-build`** at **`1ddf791`**, pushed; **draft PR #68** ("do not merge before `CAP-078` is
analysed"; CI green); `main` has the 1.3.1 release and ADR-066 (`a14c157`). This session's commits go **on `feature/0096-experiment-build`** after `1ddf791`
(analysis and documentation only). Untracked: the thirteen capture files below that `.gitignore` does not block, and **`captures/test`** (0 bytes, created
2026-10-17 17:26 — not a capture file: ask at Checkpoint 0, never delete it unasked). `.gitignore` already blocks the two films, the full logcat and the system
log (ADR-066) — confirm with `git check-ignore -v`.

The folder `captures/CAP-078-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BN/` (the skeleton's placeholder name) holds, per `stat`, `sha256sum`, `ffprobe` and `tshark`
of 2026-10-17 (the first eight hex digits of each SHA-256; times local, CEST):

| File | Measured | Notes |
|---|---|---|
| CAP-078-EVENT-NOTES.md | 11,084 B, 644, `eb286e13…`, identical to the commit | the skeleton: add the timeline, keep the procedure as the appendix |
| CAP-078-adb-shell.log | 1,575 B, 644, `a8db1efa…` | **written 2026-10-16 10:20–10:31, not on the run day** — see below |
| CAP-078-recording1.mp4 | 434 MB, 740, `a0f41353…`; **2,077.08 s**, 1080 × 2424, **audio: AAC 44.1 kHz mono**; `creation_time` 14:59:15Z (= 16:59:15) | if the stamp is the end, the film covers ≈ 16:24:38 – 16:59:15 — **derive it from the status bar** |
| CAP-078-recording2.mp4 | 105 MB, 740, `bbc9d9a9…`; **470.78 s**, audio AAC mono; `creation_time` 15:17:49Z (= 17:17:49) | if the end, ≈ 17:09:58 – 17:17:49 — derive it |
| CAP-078-btsnoop_hci1.log.last | 14,915 B, **`37a4b7a8…`**, 220 packets, 16:21:42.52 – 16:24:05.62 | **byte-identical to …hci2.log.last** — one file pulled twice |
| CAP-078-btsnoop_hci2.log.last | 14,915 B, **`37a4b7a8…`**, 220 packets | duplicate of the above |
| CAP-078-btsnoop_hci1.log | 232,778 B, `cb203e0e…`, 5,415 packets, **16:24:08.94** – 16:42:52.88 | **a byte prefix of …hci2.log** (`cmp -n`) |
| CAP-078-btsnoop_hci2.log | 373,999 B, `328d9a14…`, 8,651 packets, **16:24:08.94** – 16:59:36.94 | **a byte prefix of …hci3.log.last** — so it is **user 10's** log (parts 1–2), not part 3's |
| CAP-078-btsnoop_hci3.log.last | 453,003 B, `57937899…`, 10,057 packets, **16:24:08.94** – 17:07:38.71 | the superset of hci1/hci2: the whole user-10 run up to the user switch |
| CAP-078-btsnoop_hci3.log | 526,312 B, `0ed4b237…`, 9,434 packets, **17:07:42.39** – 17:19:32.60 | **part 3 (user 0)** — not in the maintainer's list; the log restarted at the user switch (17:07:37.93, below) |
| CAP-078-opencontrol-debug-20261017-164310.txt | 663 lines, 740, `fa348849…`, 16:24:16.924 – 16:40:17.433 | **0 "Experiment" lines**; starts "Permissions: showing the system prompt" — which app wrote it? (§3 item 2) |
| CAP-078-opencontrol-debug-20261017-165715.txt | 688 lines, 740, `467256fe…`, 16:45:09.675 – 16:56:58.716 | Experiment lines: A 36, B1 10, B2 14, C 2, D 2, E 24 (`grep -o 'Experiment [A-E][0-9]*'`) |
| CAP-078-opencontrol-debug-20261017-171718.txt | 598 lines, 740, `940a0fa6…`, 17:09:35.169 – 17:17:06.162 | A 8, B1 7, B2 8, C 4, E 6, **D 0** |
| CAP-078-OpenControl-for-Pixel-Buds-Pro-2-log-8bfbd9598ce5.txt | 778 lines, 740, `b5c342a1…`; header `package: io.github.tedsluis.opencontrolpixelbuds.experiment:10301, targetSdk 34`, `userType: full.secondary`; **UTC** 14:19:54 – 14:43:23 | the experiment app's logcat (App info → View logs) |
| CAP-078-OpenControl-for-Pixel-Buds-Pro-2-log-4b3358e819e1.txt | 1,853 lines, 740, `af897104…`; the experiment package, `full.secondary`; UTC 14:19:54 – 14:57:28 | **overlaps the one above** (same first line) — a later pull of the same buffer? diff them |
| CAP-078-OpenControl-for-Pixel-Buds-Pro-2-log-02c37baa0fc4.txt | 430 lines, 740, `6ba3a3bb…`; the experiment package, **no `userType` line**; UTC 15:09:35 – 15:18:04 | part 3 (user 0) |
| CAP-078-logcat-all-1.txt | 25.0 MB, 644, `34352ce1…`, **158,238 lines**, `adb logcat -b all`, local time, 10-08 21:47 – **10-17 16:59:06** | **ends before part 3**; 2,552 lines name the app's packages; 5,333 match `gmail\|@\|ssid` — **never committed in full** (an extract only, ADR-066 item 3) |
| CAP-078-System-log-a5697a5cda0e.txt | 11.5 MB, 740, `8c8e72c8…`, **78,700 lines**, header `osVersion …CP3A.261005.005…`, buffers main/system/crash/events/kernel, **UTC**, to 15:18:24 (= 17:18:24) | the only phone log covering part 3; 2,949 match the privacy pattern — **never committed** (ADR-066 item 2), an extract if needed |

**Clocks:** the app logcats and the system log print **UTC**; the full logcat and the exports print **local time (UTC+2)** — shown by the same `am_proc_start`
line (PID 11426) at 16:19:41.132 in the full logcat and 14:19:41.132 in the system log. Convert every time to local phone time in the documents.

**The adb shell log, verbatim content to re-read:** written on **2026-10-16** (the install after `ai-sessions/0096`, not the run): `am get-current-user` → `0`
at 10:21:06; the release app `versionCode=10301`, `lastUpdateTime=2026-10-16 08:56:17` (the Obtainium update to 1.3.1, D4), two `firstInstallTime` lines;
`pm list packages --user 10 | grep -i -E "gms|vending"` → only the three `app.grapheneos.gmscompat*` packages, `exit=0` (**GrapheneOS's compatibility layer
matched — not a Play positive; no `com.google.android.gms`, no `com.android.vending`**); `--user 0` → the same three plus `com.android.vending` and
`com.google.android.gms`; the experiment package `versionName=1.3.1-exp.1`, `lastUpdateTime=2026-10-16 10:30:27`. **Not in the log:** the install command of
each user (P1 asked for `--user 10` and `--user 0`) and the APK's `sha256sum` — the full logcat's `PackageManager` lines of 2026-10-16 10:2x–10:3x decide in
which user(s) it was installed and when; the Info tab's build string on film is the fallback for the build.

**The process story from the phone logs (re-derive; `am_proc_start`/`am_proc_died`/`am_kill`/`am_switch_user`):**
- **The release app ran only as `bound-service` for `…app.AncTileService`** (its Quick Settings tile) in the window — 6 starts in the full logcat, e.g. user 10
  PID 13506 16:23:37.996 → died 16:43:10.966, PID 17584 16:43:25.648 → 16:57:18.059, PID 20076 16:57:30.952 → …; user 0 PIDs 23505 (17:07:38), 24998, 25359,
  27746 (system log). **No `top-activity` start of the release app** — did part 1 (BN-1 … BN-4, the release app's steps) run at all? And did the release
  app's tile service ever open a Bluetooth connection while the experiment app was in use (`ARCHITECTURE.md` §6.0b — the run card's "never both running")?
  Its logcat lines in the phone logs and any second RFCOMM connection on the wire decide.
- **The experiment app** (`next-top-activity`): user 10 PID 11684 16:19:53.700 (killed 16:22:11.731 from Settings, PID 11423), PID 13365 16:22:50.847
  (killed 16:23:16.471), **PID 13701 16:24:16.028** (export `-164310` starts 16:24:16.924; killed 16:44:53.741), **PID 18853 16:45:08.606** (export `-165715`);
  a brief `am_switch_user: 0` 16:20:57.013 → `am_switch_user: 10` 16:21:38.942 (user 0's experiment PID 10158 killed 16:21:11.329); **`am_switch_user` id=0 at
  17:07:37.928** (system log); user 0 **PID 25531 15:09:34.346 UTC = 17:09:34** (export `-171718`).
- Check: crashes, ANRs, `FATAL EXCEPTION` of either package in the window (`grep -c` with a positive control).

**First look, to be re-derived (leads, not findings):**
- The export `-165715`'s first experiment line: 16:46:20.058 "Experiment A: sending 7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 04 22 02 68 01
  f8 89 dc 6c 7e (channel 19)" — **byte-identical to the skeleton's reference frame A `4:{13:1}`** — then 16:46:20.170 "Experiment A: RESPONSE OK". Every A send
  of both exports: the frame, the status, the stream packet(s) `4:{13:n}` after it, the mode on screen, what was heard (speech), and any Message Stream
  `Notify` on DLCI 0x04 (BN-7: was the Message Stream told?).
- **B (the ring):** the exports show "the Buds' ring status 0x02 / 0x01 / 0x00", "ACK ff 01 00 02 04 01 sent", "the Buds reported 00 (stopped) — claim
  released" (16:55:20.901 B1, 16:55:54.319 and 16:56:21.005 B2) and, in user 0, only "no stop report within 30 s — claim released" (17:14:49.955, 17:15:28.859
  B1; 17:16:00.505 B2) with statuses 0x02/0x01 — **did the user-0 ring claims survive Play's re-claim** (`CAP-074`: Play re-claims 5.2–5.7 s after losing DLCI
  0x04)? Every ring frame, every Buds status message, every phone ACK, every `SABM`/`DISC` on DLCI 0x04 with its sender, and the Play markers
  (`03 08 00 02 01 25`) with their positive control (`CAP-076-btsnoop_hci1.log`: 18).
- **C (Ring both):** "Experiment C: ACK" at 16:56:37.864, 17:16:34.090, 17:16:55.054 — **ACK, not NAK**: but did both buds ring (the audio track: two
  sources? the speech)? The Buds' status messages after `04 01 00 01 03`; the Stop.
- **D (`SubscribeToOobeActions`):** 2 lines in `-165715`, **0 in user 0**; was anything streamed back (`SERVER_STREAM` on the subscription's call id) during
  the bud in/out events — or only an error status? Not refuted by silence alone (the skeleton's rule) — but a silence needs a positive control (another
  subscription's stream in the same session).
- **E (`ReadSetting 4:8/9/10`):** 24 + 6 lines; "sent (answers, if any, in the hex log)" — every answer decoded from the wire: a value (which field, which
  type) or an error status (`pw_rpc` status code); identical in one-bud / two-bud / in-ear-off conditions or not.
- **Subscriptions per export:** "Settings stream subscribed" 7 / 5 / 6 — one per MAESTRO session (Connect, every re-open)? Count the sessions on the wire.
- The export `-165715` ends 16:56:58.716 "Noise-control mode from the settings stream: no mode settable" — a value 0 after the ring tests (buds out).

---

## 3. Leads and context — verify each before relying on it

1. **The users and the logs:** settle which HCI file, export, app logcat and film belongs to which user and part — by time, the `am_switch_user` lines, the
   app logcat's `userType`, the Play markers (0 expected in user 10, some in user 0) with the positive control. Prove the prefix relation of
   hci1.log ⊂ hci2.log ⊂ hci3.log.last (`cmp -n`) and the identity of the two `.last` files; analyse the superset hci3.log.last (16:24:08 – 17:07:38) plus
   hci3.log (17:07:42 – 17:19:32) plus the 220-packet `.last` (16:21:42 – 16:24:05) **in full** and say that this covers every packet of all six files. The
   gap 17:07:38 → 17:07:42 at the user switch: what does the log say (a snoop restart, a Bluetooth restart)?
2. **The build and the apps:** which app wrote the export `-164310` (0 experiment lines — the experiment app before any experiment, or the release app?): its
   PID-matching app logcat (`-8bfbd9598ce5` starts with PID 11684/13701 lines of the experiment package), the "Permissions" first line against the
   `am_proc_start` at 16:24:16.028, the Info tab on film. The release app's steps of part 1 (BN-1 … BN-4: TalkBack, holds while disconnected, hold within 1 s
   of Connect, Use different Buds → Cancel and rotate) — run or not?
3. **The clock offsets:** each film's status bar (the minute changes at 10 fps) and a shared event with an export line or a frame; settle each film's start
   and end, with its uncertainty, before any correlation. Film 2 may start after the user-0 process (17:09:34).
4. **The speech:** **no speech-to-text tool is installed** (no `whisper`, `vosk`, `faster_whisper`; checked 2026-10-17). Without one, tabulate the audio
   (`ffmpeg … silencedetect`, RMS per 100 ms, voiced segments by pitch and spectral flatness — `ai-sessions/0092`'s method) and align each speech segment
   with the wire and the screen; the words themselves stay unknown unless the maintainer supplies them. **Checkpoint 0 asks** the maintainer to choose: (a) no
   transcription — the events come from the wire; speech segments are only timed; (b) the maintainer lists, per film time, what was said; (c) install a
   local, offline speech-to-text tool on the workstation (e.g. `whisper.cpp` with a downloaded model — a download, so the maintainer decides; nothing of
   the film leaves the machine) — never an online transcription service (`AGENTS.md` §1, §9; the films carry private surroundings). The ring tones and the
   ANC changes may be audible too (a ring = a tonal burst at a fixed pitch; say what the track shows, not what it "must" be).
5. **The bud events without a camera:** in-ear / out-of-ear / in-case per bud from the wire — the Message Stream wear and in-ear messages, the runtime-info
   in-ear and charging flags (7.x, `PROTOCOL.md` §4.5.5 and §6 — the 0090 cross-check), the Settable `00` (no bud worn, ADR-049), the read value 0 "no mode settable"; aligned with the
   speech segments. A bud event without a wire trace is an open question.
6. **Experiment A — the core question for round 2:** does `WriteSetting 4:{13:n}` switch the mode on the Buds with **no Message Stream claim**? Per send: the
   frame byte for byte against the reference table (channel 19 or 21; CRC-32), the `RESPONSE` status, the stream packet(s) `4:{13:n}` and their value (equal to
   n?), any DLCI 0x04 traffic in the same second, the mode on film, the heard change; with one bud worn, no bud worn (Settable `00` — BN-10: OK or an error,
   and does anything change?), in-ear detection off; in user 0 — did Play see the change (a Message Stream `Notify` to Play, a Play claim afterwards)? Count:
   sends, OK, stream packets, screen changes. Compare with `CAP-077`'s writes of fields 4/11/15/27/28 (three stream packets per write — the open question).
7. **Experiment B — the ring status and the ACK (`FIND-005`):** the Buds' status messages (group `0x04`, code?) with their payload per bud, the timing from
   the ring command, the phone's ACK `ff 01 00 02 04 01` (B2 only — byte for byte against `CAP-025` 2048), whether the Buds repeat a status when not ACKed
   (B1) vs ACKed (B2), what happens when the bud is touched (a status `00`?), the 30-s timeout lines; user 0: the claim's lifetime and who closed it.
8. **Experiment C — Ring both `0x03` (`FIND-004`):** the ACK/NAK on the wire, the statuses that follow (one per bud? a combined one?), the audio, the Stop.
9. **Experiment D — `SubscribeToOobeActions`:** the request against the reference frame (method hash `0x57f3becb`), the response or the stream packets, their
   decoding (the skeleton's "`qhp` 10–13?" — verify against `REVERSE_ENGINEERING.md`'s `SubscribeToOobeActions` (`qje`) entry and its 17 `qhp` constants), their timing against the bud events.
10. **Experiment E — `ReadSetting 4:8/9/10`:** every answer decoded; settings 8/9/10's meaning per `PROTOCOL.md` §6 and the APK notes — a value is reported
    as bytes and a type; a meaning only where the APK or a controlled change shows it (zero creativity, `AGENTS.md` §13 step 6).
11. **Unexpected on the wire (the maintainer's standing point):** anything on DLCI 0x08/0x0a (with a positive control, e.g. `CAP-029`'s 1592), any request not
    in the 1.3.1 Connect sequence or the experiment table, a write the user did not make, an `UnidentifiedFrame` in an export, LE traffic to the Buds, HFP
    AT commands, AVRCP pass-through, every disconnect reason, anything `scripts/pwrpc_decode.py` cannot classify, CRC failures, a second RFCOMM connection
    of the release app's tile service.
12. **Test-IDs** (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`; `AGENTS.md` §13 step 7): the skeleton's list — `PAIR-003`, `TOUCH-007`, `ANC-007`, `ANC-008`,
    `INEAR-001`, `INEAR-002`/`003`/`004`, `INEAR-007`, `FIND-001`, `FIND-002`, `FIND-004`, `FIND-005` — and any other the run actually exercised; "expected but
    not observed" for each that was not; the step mapping BN-1 … BN-19 (done / not done / done differently).
13. **Privacy:** the films may show the Wi-Fi name, notifications and the serial numbers (allowed on film, never unredacted in text — first 4 + last 2); the
    audio may carry other voices or private speech — quote only what bears on the run; the logcats print `XX:XX:XX:XX:6E:07`-style addresses; the full logcat
    and the system log match the privacy pattern thousands of times — **commit an extract only** (the `awk` of `CAP-077`: the run's time window, the apps'
    and the Bluetooth process's PIDs, every line naming either package, `com.android.bluetooth`, `CompanionDevice`, `PackageManager`/`installd` for the
    2026-10-16 install), named CAP-078-logcat-extract.txt (and, if the maintainer wants part 3's, a CAP-078-system-log-extract.txt — `.gitignore`'s
    `*System-log*` pattern must not match its name); scan every committed file for e-mail, Wi-Fi name and full serials before staging.
14. **External validation** where a claim rests on a specification or platform behaviour (fetch, quote with the date): the Fast Pair Message Stream page (the
    ring group `0x04`, its codes, the ACK `0xFF 0x01`, Ring both); Pigweed's pw_rpc packet types and status codes; the Bluetooth Core Specification for the
    disconnect reasons and RFCOMM `DISC`/`DM`; Android's `CompanionDeviceManager` only if an association changes.

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename, mode change, delete or `git add`)

1. Create the RESULT (§0). Record `git log -1`, `git status --short`, `git fetch`, `git log --oneline HEAD..origin/main`, the state of draft PR #68. Work on
   **`feature/0096-experiment-build`** (`git switch feature/0096-experiment-build && git pull --ff-only`); never on `main`; never rebase or amend.
2. List the folder fresh (`ls -la`, `sha256sum`), diff it against §2's table and say every difference.
3. **Checkpoint 0 (`AskUserQuestion`, one question per decision, options with pros and cons, "(Recommended)" on one, the exact commands in the preview):** the
   folder rename to the first/last phone times of the run (`CAP-078-2026-10-17_HH-mm-ss_HH-mm-ss-Group_BN`); the duplicate `.last` file (keep both, or
   keep one and record the identical hash); `chmod 644` on the files with mode 740; the CAP-078-sha256sums.txt (every file incl. the local-only ones);
   the originals to ~/opencontrol-capture-originals/CAP-078/ (the two films, the full logcat, the system log — ADR-066); the logcat/system-log extracts;
   **the speech (§3 item 4: a / b / c)**; whether the phone can be attached for the APK pull (`adb shell pm path --user 10 …experiment`, `adb pull`,
   `sha256sum` against `4e5facfa…`) and the install lines; whether a rebuild of `0292e59` is wanted for the "-dirty" question; **`captures/test`** (what it
   is; leave, or remove on the maintainer's word).
4. Update the `CAP-078` row of `id_registry.csv` (status `planned` now; `analyzed` in Phase D) and prepare the Group BN status line of
   `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (written in Phase D).

### Phase A — the screen recordings and their audio, in full

5. Decode each recording at 1 frame per second into the scratchpad in ranges (≈ 2,077 + 471 frames); compare each second with the previous one; view
   **every** second with a change on labelled contact sheets; narrow transitions at 10 fps where a claim depends on them (the Debug card and its confirmation
   dialogs, the mode buttons after each A send, the (i) dialogs, the Info tab's build string, the notification shade, the tile, the user switch, the export's
   "Debug log saved"). No sampling: every second is either viewed or proved unchanged.
6. **Measure each film's clock offset** (status-bar minute changes at 10 fps; a shared event with an export line) with its uncertainty; settle each film's start
   and end before any correlation. Confirm the audio streams with `ffprobe`.
7. **The audio, in full:** for each film, `silencedetect` and RMS per 100 ms over the whole track; every non-silent segment tabulated with its phone time,
   duration and class (speech-like, tonal — a ring?, click); then, per Checkpoint 0's answer, the words. Align each segment with the screen and the wire.
8. Write the timeline into the existing CAP-078-EVENT-NOTES.md: phone time · what happened · step BN-n / Test-ID · evidence — in `CAP-071`/`CAP-077`'s layout
   (the status line becomes "captured 2026-10-17 … and analyzed …", Log Metadata filled in, the capture-integrity pre-flight incl. the HCI files' prefix
   relations and the user switch, the review method and privacy, the Event Timeline incl. "before the recording" and "after the recording", the step mapping
   BN-1 … BN-19 done / not done / done differently, Test-IDs per `AGENTS.md` §13 step 7 with "expected but not observed"; the planned procedure kept below as
   the appendix).

### Phase B — the HCI logs and the other logs, in full

9. The HCI logs (§3 item 1's three files, which cover all six): Connection Complete events and the Buds' handle (by address; show the filter matching a known
   frame), the RFCOMM inventory per DLCI and direction, **every pw_hdlc frame CRC-32-checked** (`scripts/pwrpc_decode.py`, with its positive control; a payload
   holding several HDLC frames split on `7e`), every Message Stream message decoded, **the full claim inventory of DLCI 0x04/0x05** with the sender of every
   `SABM`/`UA`/`DISC`/`DM` and the owner of every claim, the Play-marker count per user with its positive control, **every experiment request (A–E) byte for byte
   against the reference table with its answer and everything that followed it within 30 s**, every `4:{13:n}` stream packet with what preceded it, the
   Connect sequence per session against `CAP-077`'s, every write, AVDTP/AVRCP state, LE traffic to the Buds, every disconnect reason; **and a deliberate search
   for the unexpected** (§3 item 11). Commands and exit statuses in FINDINGS (`PROJECT_RULES.md` rule 4a; `AGENTS.md` §13 step 8 for every negative).
10. The three exports and the three app logcats: every line read (663 + 688 + 598; 778 + 1,853 + 430 — diff the two overlapping logcats first and read the
    union once) — every export line matched to the code's log string (the "Experiment …" lines to `ExperimentRunner.kt`), the subscriptions and reads per
    session, every "from the settings stream (<cause>)" line against its packet, the claim lifetimes, the session losses and re-opens, connect failures; the
    app logcats' `events` lines; crashes/ANRs/StrictMode (`grep -c` with a positive control).
11. The adb shell log (every line), the full logcat and the system log: `grep`/`awk` by time window and PID — the 2026-10-16 install of the experiment APK (in
    which users), the user switches, both apps' processes, the release app's tile service, the Bluetooth process's lines around every disconnect and every
    experiment, Play services' Fast Pair lines in user 0 around the ring claims (B1/C in part 3), any crash. Write each extract command and its line count;
    commit extracts only (Checkpoint 0).

### Phase C — correlation, the findings, the proposals

12. Correlate every timeline row with its frames and log lines. Write CAP-078-FINDINGS.md in `CAP-071`/`CAP-077`'s structure: §0 build (the experiment APK, the
    "-dirty" question, the release app beside it), install, the users, logs, privacy; §1 ACLs and the claim inventory; §2 the Connect sequence per session;
    **§3 experiment A (every send, its answer, the mode on the Buds and on screen, per condition: both / one / no bud worn, in-ear detection off, user 0)**;
    **§4 experiment B (B1 and B2: every status, every ACK, the stop by touch, the 30-s timeouts; user 0's claims against Play)**; **§5 experiment C**;
    **§6 experiment D**; **§7 experiment E**; §8 the press-and-holds and the settings stream (every packet against the screen); §9 the release app (part 1 run or
    not; its tile service); §10 the sessions, losses and re-opens; §11 playback and audio; **§12 unexpected findings on the wire and in the logs**; §13 the
    skeleton's expectations and "Refuted if" applied literally per step run; §14 the counts; **§15 per experiment: what the run shows, its label
    (FACT / HYPOTHESIS / OPEN QUESTION with frame numbers), and the round-2 proposal** — what the app could build on it (e.g. ANC through `WriteSetting 4:13`
    without the Message Stream; the ring-status ACK; Ring both), what still needs a capture, and which `PROTOCOL.md` entries could be proposed for 🟢 FACT and
    which ADR drafts round 2 needs (texts in the FINDINGS, marked "proposal — not accepted"); §16 open questions. FACT / HYPOTHESIS / ASSUMPTION / OPEN
    QUESTION on every conclusion (`PROJECT_RULES.md` §1); every claim with a frame, a log line or a film time.
13. Validate externally (§3 item 14): fetch, quote with the date.

### Phase D — checkpoint, documentation, finish

14. **Checkpoint (`AskUserQuestion`, one question per decision):** per experiment A–E, the proposed `PROTOCOL.md` update (a 🟢 FACT promotion or a 🟡 note —
    the draft text in the preview; `AGENTS.md` §6: nothing written without the maintainer's answer in this chat); whether ADR drafts for round 2 are written
    now (a draft headed "(DRAFT — proposed, not accepted)" and registered `proposed`, per the `AGENTS.md` §6 note of 2026-10-15) or left to the FEATURE
    session; whether a round-2 FEATURE prompt follows (a draft title and scope in the preview: the passed experiments, the runtime-info L/R battery,
    `TODO.md` items 2.1/2.2/2.4/2.5, P1/P2); whether part 1's release-app steps need a short re-run; the privacy treatment; whether the small capture files
    are committed now (ordinary git files, ADR-066) on `feature/0096-experiment-build`; what happens to draft PR #68 (stays a draft — the experiment build is
    never merged as a release feature — or is closed after round 2). Record the answers verbatim in the RESULT.
15. With the answers: `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 (the `CAP-078` row: the real times, analyzed) and the Group BN status line; `id_registry.csv`;
    `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the Group column: BN only on the Test-IDs exercised); `APP_TESTPLAN.md` §AA (the Result column per row, a run note);
    `PROTOCOL.md` and `DECISIONS.md` only as approved; `TODO.md` §1/§2/§4/§5; `ARCHITECTURE.md` §5a where the run settled something. Then
    `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py` (exit 0).
16. The RESULT: plain-language summary first (what happened on the phone, what each experiment showed, which steps did not run, what was unexpected, what the
    maintainer decides next), the phases, the files read in full / in part, "Deferred documentation" (each also in `TODO.md`), "Commits"; the
    `ai-sessions/INDEX.md` row (0097; the 0096 row's Status gets the back-fill "hardware run `CAP-078` analysed by 0097").
17. Show a short summary and **ask whether to commit and push**. Only after a yes: commits per concern (Conventional Commits: `docs(capture)`, `docs`), each
    with its *why* and the attribution line; ordinary git files for the small capture files (ADR-066), never the films, the full logcat or the system log; on
    `feature/0096-experiment-build`; `git fetch`; push without force; never merge.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Scope:** analysis and documentation only. No change under `android/` (improvements are proposals for round 2), no new skeleton, no release step, no
  `dist/` change, no tag, no merge, no rebase.
- **Approvals only in this chat** (`AGENTS.md` §6): no 🟢 FACT promotion, no new ADR or ADR Update, no draft ADR in `DECISIONS.md` without the maintainer's
  consent here; ADR-060 stays a draft. An experiment's result is reported from this run's evidence — never from the maintainer's impression or the 0096
  expectations; an experiment step that did not run is "not exercised", not "passed".
- **No new wire traffic:** this session sends nothing to the Buds. If the phone is attached (Checkpoint 0), only read-only `adb` commands (`pm path`, `pull`,
  `dumpsys`, `logcat -d`) — never an install, an `am start` of either app, or a Bluetooth action.
- **Evidence:** every claim cites a frame (log and number), a log line or a film time; a negative needs its command, its exit status and a positive control; a
  hypothesis names its settling experiment. The maintainer's statements are statements until verified.
- **No sampling:** every second of both recordings and both audio tracks, every packet of the HCI files on the Buds' handle (the superset files, with the
  prefix proof), every line of the three exports, the app logcats and the shell log — reviewed or covered by a stated, positive-controlled filter; the full
  logcat and the system log by stated filters over the run's full time window.
- **Privacy:** no serial unredacted, no MAC, no phone number, no account name or e-mail, no Wi-Fi name, no private speech in any committed text; the films, the
  full logcat and the system log are never committed (ADR-066); extracts only.
- **Files:** before moving, renaming or deleting anything, diff a fresh listing; never wildcard deletes; ask first. `android/domain/bin/` is never staged.
- **Subagents** may only read and report; every write is done and verified in the main session; never two `tshark`/`ffmpeg` jobs at once.

## 6. Deliverables

- CAP-078-EVENT-NOTES.md (the timeline added to the skeleton) and CAP-078-FINDINGS.md (the users, the builds and the logs settled, each experiment A–E with
  its evidence and label, the release app's part, the sessions, the unexpected, the step mapping, the counts, the round-2 proposals), with the folder and files
  as approved in Checkpoint 0 (incl. the extracts and the hash list).
- The registry, index, test-plan, `TODO.md`, protocol and decision updates as approved; a round-2 FEATURE-prompt proposal if one follows.
- ai-sessions/0097_CAPTURE_RESULT_2026_10_17.md and the `ai-sessions/INDEX.md` row.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0097_CAPTURE_PROMPT_2026_10_17.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0097_CAPTURE_PROMPT_2026_10_17
