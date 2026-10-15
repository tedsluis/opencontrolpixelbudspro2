# 0092_CAPTURE_PROMPT_2026_10_15.md — Full analysis of CAP-076 (Group BL: the 1.3.0 build `ff27ee1`, the settings stream on the app's own channel — ADR-064/065 — in the Owner user with Google Play services and the secondary user without)

**Number:** 0092
**Category:** CAPTURE
**Date:** 2026-10-15
**Title:** Fully analyse `CAP-076`: **two** Android screen recordings, **four** HCI snoop logs, **two** app debug exports, **two** app logcats, one system log,
one full `logcat -b all` and one adb shell log, recorded on 2026-10-15 with the release-signed **1.3.0 build `ff27ee1`** (`ai-sessions/0091`, PR #56;
`scripts/release.sh 1.3.0` run by the maintainer on 2026-10-15, APK SHA-256 `03272c6f954316d76c3ef6d1d020abd8bdb97755d7f8e165c84d3f16a9c6a96c`, certificate
`a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb`) on a Pixel 9a with GrapheneOS (Android 17) — **part 1** in the Owner user **with** Google
Play services (06:35–06:52, the update 1.2.2 → 1.3.0 on film) and **part 2** in the secondary user **without** Google Play (07:01–07:08). The task: establish
from the films, the wire and the logs what happened in each part; whether the two new requests went out once per MAESTRO session and whether every
press-and-hold reached the app through the settings stream without a claim of DLCI 0x04; the own tap's stream packet; the re-opens and their reads; the
collisions with Play; compare with `CAP-075` (the same two users, 1.2.1); record the findings; propose the release verdict (`RELEASING.md` C3/C4).
- Record the real events in the existing skeleton **CAP-076-EVENT-NOTES.md** (rewritten for 1.3.0 by `ai-sessions/0091`; the timeline is added to it, the
  planned procedure stays as the appendix, as `CAP-073`/`CAP-075` did) and the analysis in a new **CAP-076-FINDINGS.md**.
- **No change to the app** in this session (improvements are **proposed**, with the evidence, for a later FEATURE session), **no new capture skeleton**,
  **no release step** (no tag, no `dist/` change, no merge): the analysis, its documentation and the verdict proposal for the maintainer.

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this repository. Run the
phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Take the time to read the core files first, in full and in this order, to
understand the ground rules before analysing anything:
- `AGENTS.md` and `PROJECT_RULES.md`.
- `PROJECT.md` (the Definition of done; the status paragraph ending with 1.3.0).
- `ARCHITECTURE.md`, in particular **§3.1** (the new bullet "The Buds push every change of a setting on the app's own channel": the fields, the cause rules,
  `UnreadSetting`, value 0), **§3.2** (`ANC_SET_WINDOW_MS`, `MESSAGE_STREAM_LINGER_MS`, `SETTING_READ_TIMEOUT_MS`, `LOSS_REOPEN_DELAY_MS`, `CHAIN_GUARD_MS`),
  §5a (the "Changed by the Buds" row — 1.3.0 "not hardware-verified — `CAP-076`"), **§6.0b** (the 1.2.0 … 1.2.2 history paragraph and the "Since 1.3.0"
  paragraph: the claim per tap, the foreign close that changes nothing, the known collision, the always-on log lines), §7, §9, §12, §13.
- `PROTOCOL.md`, in particular §2.2a and §2.3 (the three channels; DLCI 0x04 **or 0x05**; channel 19 vs 21 and the ADR-034 addresses), **§4.1 and its
  Update of 2026-10-14** (setting 13 = the mode, pushed on the settings stream for every reported change; `ReadSetting 4:13`; the 12-of-77 `DISC` case; the
  Notify goes to the one client holding DLCI 0x04; the 2026-10-13 note on Play's timers), §4.5, **§6** (the 🟡 items of 2026-10-14: the Buds' own switch on a
  wear change — the stored mode per wear state; one-bud / two-bud mode), §8 (the rows of 2026-10-12/13/14).
- `DECISIONS.md`: **every** ADR, ADR-001 … ADR-065, with every dated Update, and the draft ADR-060 (not a decision). In particular **ADR-064** and its
  2026-10-14 Update, **ADR-065** (items 1–6, the "as built" note, "Verification" — this run), ADR-032 (its three Updates), ADR-044 (the re-open), ADR-049
  (Settable `00`), ADR-057 (what is "current"), ADR-061/062/063 (superseded — what 1.2.x did, for the comparison with `CAP-075`).
- `TODO.md`, §1 (`CAP-076` = the 1.3.0 run), §2, §3 (the *Nearby devices* permission item), §4 (item 4 built; E-01 `WriteSetting 4:{13:n}` — never sent),
  §5 (the Play-user item: the per-tap collision).

These are the ground rules; do not analyse or change anything before they are read. Then, per task:

- **Procedure and registers:** `AI_SESSION_LOG_PROCEDURE.md` (in full, incl. §4b, §5 and §9), `ai-sessions/INDEX.md`, `id_registry.csv` (the `CAP-075`,
  `CAP-076`, ADR-064, ADR-065 rows and every Test-ID named in §3 item 12), `RELEASING.md` (the Release checklist, "Repository rules", §6, §11a, §13).
- **The build under test and its skeleton — read in full:** `ai-sessions/0091_FEATURE_PROMPT_2026_10_14.md` and `…/0091_FEATURE_RESULT_2026_10_14.md`
  (**§A.1 the reference frames re-derived** — every byte string the wire is compared against, §A.2–§A.4 the design, **§B the checkpoint answers Q1–Q11**, §C the
  build as made — the log strings, §D the gate and the mutations, §E the skeleton, §G the commits and PR #56); **`CAP-076-EVENT-NOTES.md`** (the skeleton: the
  two parts, P0–P10, the reference-frame table with the `tshark` lines, BL-1 … BL-25, the analysis checklist with the **counts for the verdict**);
  `CHANGELOG.md` `[1.3.0]` (the two Known issues); `APP_TESTPLAN.md` §Y (Y1 … Y22) and the Summary.
- **The "before" — read in full:** `captures/CAP-075-2026-10-12_17-22-31_2026-10-13_06-34-32-Group_BK/CAP-075-FINDINGS.md` (**§0 the users' evidence, §1 the claim
  inventory of both clients and the Play-services marker `03 08 00 02 01 25`, §2 the Connect reads byte for byte, §4 the holds per position, §5 the cut's
  stack path, §9 the proposals P1–P6**) and `CAP-075-EVENT-NOTES.md` (the method: the clock offset from the status bar, the audio segments, the claim-owner
  rule, the system-log extract and its command); `captures/CAP-074-…-Group_BJ/CAP-074-FINDINGS.md` §8 (one RFCOMM connection per server channel — a second
  connect kills the incumbent); the official app's evidence for the stream: `captures/CAP-051-…/CAP-051-FINDINGS.md` §2–§3, `CAP-056-FINDINGS.md` §4,
  `CAP-038-FINDINGS.md` §3, `DESKRESEARCH_FINDINGS.md` entry 2026-10-14 (the census: 73/73, 39/39, the 12 `DISC` misses); the maintainer's named examples
  `captures/CAP-071-2026-10-08_17-51-29_18-27-28-Group_BG/CAP-071-EVENT-NOTES.md` and `…/CAP-071-FINDINGS.md` (the layout: metadata table, P0/P1 outputs,
  pre-flight, review method and privacy, one timeline row per event, step mapping, Test-ID traceability, the extract command for a system log);
  `ai-sessions/0088_CAPTURE_RESULT_2026_10_12.md` (the method and the scratch scripts — re-create them, they are not in the repository).
- **Every Kotlin source a finding touches, in full,** before you write about it — at least: `BudsRepositoryImpl.kt` (`launchInitialEqRead`,
  `subscribeToSettingsChanges`, `readAncSetting`, `applyAncState`, `causeOfStream`, `causeOfReadAnswer`, `causeOfNotify`, `applySetting`/`keepsOwnMark`,
  `handleRoutedFrame`'s `Setting`/`UnreadSetting` branches, `openSession` (the cause is no longer reset), `withMessageStream`/`scheduleRelease`, the
  `channelClosed` and `connectionLost` collectors, **every log string — each export line is matched against them**), `codec/CodecRouter.kt` (`routeMaestro`),
  `codec/SettingFrame.kt` (`SettingValue.AncState`, `fieldOf`), `codec/Maestro.kt`, `RfcommBudsTransport.kt` (the reader's `read return: -1` path, the
  session-loss vs on-demand-close reporting, the connect retry), `SessionReopener.kt` (`LOSS_REOPEN_DELAY_MS`, `CHAIN_GUARD_MS`, the skip line),
  `SessionDiagnostics.kt`, `AncScreen.kt` (`ANC_STREAM_HINT_TEXT`, `ancChangedByBudsLine`, `ancChangedSinceLastReadLine`, `ancNotAllowedLine`),
  `SettingsMenu.kt`, `MainActivity.kt`, `domain/ValueCurrency.kt` (`currentAncMode`), `domain/AncModeCause.kt`.

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.**
- Create **ai-sessions/0092_CAPTURE_RESULT_2026_10_15.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block.
- Update that block at the end of every phase **and** after every substantial step: every minute of each recording reviewed, every HCI log and channel
  decoded, every log file read, every checkpoint answer. Say what is done and what is next, which files are touched but not yet verified, and where the
  intermediate results live (the scratchpad: scripts, contact sheets, decoded tables, notes — re-create them if it is gone).
- A resumed session reads this prompt, then the RESULT's Progress block, re-reads the files the unfinished step touched, and continues from there. It never
  redoes a finished, recorded step and never assumes an unrecorded one was done. The prompt keeps its number and date; the RESULT grows in the same file.

**Memory and the machine.** The laptop has 15 GB and `/tmp` is a 7.7 GB tmpfs. Run `tshark`/`ffmpeg` one job at a time; extract contact sheets and crops in
ranges, never a whole film into memory (recording 1 is 248 MB and 981 s); the system log (12 MB, 94,423 lines) and the full logcat (30 MB, 213,298 lines) —
`grep`/`awk` them, do not read them whole into context. No Gradle build is needed. Never `git worktree add` a commit (it checks out `captures/`, gigabytes).
Scratch files go into a fresh `mktemp -d` under `/tmp/claude-1000/`; write their names in documents without backticks (the docs lint flags backticked names
that do not exist). `python3 scripts/pwrpc_decode.py <log>` decodes the pw_rpc frames (run it with `-I` only when the log is untrusted input you did not
record — here the maintainer's own logs, so a plain run is fine).

**Nothing is taken on trust, including this prompt, the maintainer's statements, the 0091 RESULT and the memory notes.** Every claim and every number
measured in §2 and §3 is re-derived from the recordings, the HCI logs, the other logs, the code and official documentation.

---

## 1. What the maintainer asked for (chat, 2026-10-15, translated from Dutch)

1. "Make a new prompt for Claude Code, in English, per the project's rules in `ai-sessions/`, that can be run in a new session. You are an expert software
   architect, reverse engineer and technical auditor. Your task is a comprehensive analysis of `CAP-076` (Group BL) of the OpenControl app, recording the
   findings. Take the time to read the core files first (`AGENTS.md`, `PROJECT_RULES.md`, `PROTOCOL.md`, `ARCHITECTURE.md`, `DECISIONS.md`) to understand the
   ground rules before auditing the rest. Run everything in phases, sequentially; when you hit a token limit you must be able to continue automatically
   later."
2. **The maintainer's statements about the run (verify each; label each as the maintainer's statement until verified):**
   - "I ran `scripts/release.sh 1.3.0`" (its output: build commit `ff27ee1`, the APK and certificate SHA-256 above), "did `cp -a dist/1.3.0
     ~/opencontrol-1.3.0-tested` and installed the APK over 1.2.2. Then I ran `CAP-076` (in part)."
   - "**I did not keep to the test plan. I tested only the changed functionality.**" — *map every skeleton step BL-1 … BL-25 as done / not done / done
     differently; a step not done is "expected but not observed" in the Test-ID traceability, not a failure.*
   - "The app was version 1.2.2 (of yesterday) at the start of the video. During the first video (CAP-076-recording1.mp4) I did the upgrade to 1.3.0." —
     *the Info tab on film, the app logcats' `package: …:10300`, the `dumpsys` lines in the shell log (versionCode 10202 at 06:23:50 → 10300 at 06:38:17,
     `lastUpdateTime=2026-10-15 06:36:57`) and the full logcat's `am_proc_start` lines (06:35:42 PID 10952, 06:36:59 PID 11800) decide.*
   - "The capture was made on a Pixel 9a with GrapheneOS Android 17."
   - "I spoke only very little during the video."
   - "**The ANC modes during playback were good! When I did a long press on a bud, the ANC mode in the app changed too!**" — *the verdict's core claim:
     count every press-and-hold (the stream packets `4:{13:n}`, the audio, the ears if on camera, the mode buttons on film) against the mode shown within a
     second, in both users; say how many were shown, how many not, and why for each miss.*
3. **The analysis steps the maintainer named (§4 follows them):** first both recordings → `CAP-076-EVENT-NOTES.md` with every event and action and its time;
   all `CAP-076-btsnoop_hci*` logs with `tshark`; both debug exports; both app logcats; the full logcat; the adb shell log; the system log; then correlate the
   timeline with every log (`CAP-071-EVENT-NOTES.md` as the example); a comprehensive CAP-076-FINDINGS.md (`CAP-071-FINDINGS.md` as the example).
4. Strict rules (the maintainer's): no assumptions — verify everything; validate claims externally against official documentation where needed (Android
   Developer Docs, the Bluetooth Core Specification, the Fast Pair specification); **no sampling** — a 100 % complete review of the relevant files; evidence
   traceability (every claim links to a frame, a log line or a film time) and structural integrity.
5. Standing practice: no commit or push without the maintainer's "yes" in chat; no release step; `main` takes no direct push (`RELEASING.md`).

---

## 2. The evidence (measured 2026-10-15 07:20 by the session that wrote this prompt — re-derive every number)

**State of the repository:** branch `feature/settings-stream` at **`ff27ee1`** (= the build commit of B1; the four commits of 0091 and two docs commits),
pushed; **PR #56** open (A1–A6, B1, B2 ticked; B3, C1 done by the maintainer per chat — tick them in the PR body only after this session's checks); `main` at
`c1d898b`. Per `RELEASING.md` C3 this session's commits go **on the same branch, after the build commit** — they change no app file, so the tested APK stays
the build of B1. Untracked in the repository: the fourteen capture files below (the skeleton file is committed), `android/domain/bin/` (build output — never
staged) and an **empty file `test` in the repository root** (0 bytes, 07:17 — ask the maintainer at Checkpoint 0 whether it is an accident to delete).

The folder `captures/CAP-076-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BL/` (the skeleton's placeholder name) holds, per `ls -la` and `sha256sum` of 2026-10-15 07:20
(the files the maintainer listed, with the first eight hex digits of each SHA-256):

**Shared:**

| File | Measured | Notes |
|---|---|---|
| CAP-076-EVENT-NOTES.md | 29,329 B, committed (`b1669687…`), rewritten 2026-10-14 22:28 | **exists**: add the timeline to it, keep the planned procedure as the appendix |
| CAP-076-adb-shell.log | 1,473 B (`307f948a…`), 06:21–06:38, mode 644 | P0 for both users and P1's `dumpsys` before/after — see below |

**Part 1 — the Owner user (user 0) with Google Play (06:35–06:52):**

| File | Measured | Notes |
|---|---|---|
| CAP-076-recording1.mp4 | 248 MB, **980.97 s**, 1080 × 2424, 60 fps nominal, **an audio track**; `creation_time` 2026-10-15T04:52:11Z (= 06:52:11 local, likely the end); mode 750; `e008aa17…` | if the stamp is the end the film starts ≈ 06:35:50 — **derive it from the status bar**; it should cover the 1.2.2 → 1.3.0 update (install 06:36:57, the 1.3.0 process start 06:36:59) |
| CAP-076-btsnoop_hci1.log | 607,389 B, **10,0xx packets**, mode 644, `b171e3ca…` | Buds ACLs (Connection Complete for `04:00:6e:…`, handle `0x000b`): **672** 06:36:19.895 → Disconnection Complete **4558** 06:42:23.192 (reason `0x13`); **4680** 06:42:46.059 → open at the log's end (06:55). Count the packets and the time span exactly (`capinfos -c -a -e`) |
| CAP-076-btsnoop_hci1.log.last | 833,776 B, 7,684 packets, `489c44ce…` | ends with a Disconnection Complete of an LE handle `0x0041`, reason **`0x16`**, at 06:35:34.729 — a Bluetooth **off** before the run (P2's toggle?); no Buds Connection Complete found by the `bthci_evt.code == 0x03` filter — verify the span and whether any Buds frame is in it (the P2 "off and on on film") |
| CAP-076-opencontrol-debug-20261015-065105.txt | **1,061 lines**, 06:37:00.476 – 06:50:55.279 (local); mode 750; `e455b902…` | one process from "Permissions (start)" at 06:37:00 (the 1.3.0 process, PID 11800); **8 "Settings stream subscribed", 8 "Noise-control mode read (", 86 "from the settings stream (changed_by_buds)", 22 "(set_by_app)", 5 "from the mode read (read)", 4 "no mode settable", 0 "changed_since", 0 "not shown (not read"**, 7 "Automatic re-open" (1 LINK_BACK 06:37:00, 6 AFTER_LOSS), 1 "re-open skipped … 928 ms after", 1 "Session ended by the user's Disconnect tap" 06:39:41, **7 "Session lost: channel 0x02 closed"** (06:39:53, 06:41:31, 06:42:23, 06:42:53, 06:42:57, 06:47:30, 06:47:44) against **one** ACL disconnect (06:42:23) — the others are RFCOMM-level closes by the Buds (`DISC` of DLCI 0x02 — the wear-change re-homes? the film), **4 "RFCOMM on-demand channel 0x04 closed"** 06:45:58 – 06:46:28 (taps colliding with Play? BL-11); 0 "re-claim"/"hold" lines (positive control: `CAP-075`'s export 2); no raw hex lines |
| CAP-076-OpenControl-for-Pixel-Buds-Pro-2-log-a1c4370c8dcb.txt | 731 lines; header `osVersion: google/tegu/tegu:17/CP3A.261005.005/2026100601:user/release-keys`, **no `userType:` line** (the Owner, as `CAP-074`/`CAP-075` set 2), `package: …:10300, targetSdk 34`; UTC 04:35:43 – 04:51:15; mode 750; `6e7fcc66…` | the app logcat of part 1 (App info → View logs) |
| CAP-076-System-log-b5e49395adf6.txt | 12.2 MB, **94,423 lines**; header `type: logcat … buffers: main,system,crash,events,kernel`; spans 10-08 19:47 – **10-15 04:51:37 (UTC)**; 893 lines name the app, 1,482 name `com.google.android.gms`; **2,709 lines match a broad privacy pattern** (`gmail|@|ssid`); mode 750; `71b39d49…` | Android's log viewer, "System log" (all apps) — part 1 only |
| CAP-076-logcat-all-1.txt | 30.1 MB, **213,298 lines**; `adb logcat -b all -v threadtime` (no header); spans 10-08 21:47 – **10-15 06:56:48 (local)**; 985 lines name the app; `am_switch_user: 10` at 06:08:16 and `am_switch_user: 0` at 06:13:05 (before the run), `am_proc_start` of the app in user 10 at 06:08:16 (PID 4104, bound-service), in user 0 at **06:35:42** (PID 10952 — 1.2.2) and **06:36:59** (PID 11800 — 1.3.0); 10,011 lines match the privacy pattern; mode 644; `3f8e5ef7…` | the same phone log in local time, longer buffer — part 1 only (it ends before part 2); neither system file is committed in full (privacy): an extract, as `CAP-075`'s CAP-075-logcat-extract.txt |

**Part 2 — the secondary user (user 10) without Google Play (07:01–07:08):**

| File | Measured | Notes |
|---|---|---|
| CAP-076-recording2.mp4 | 111 MB, **348.94 s**, 1080 × 2424; an audio track; `creation_time` 2026-10-15T05:08:20Z (= 07:08:20 local); mode 750; `fd4e51ec…` | if the stamp is the end the film starts ≈ 07:02:31 — the export starts 07:01:03 and shows a Bluetooth off/on at 07:02:39/07:02:44: derive the offset from the status bar and say what the film does not cover |
| CAP-076-btsnoop_hci2.log | 133,422 B, 3,070 packets, `d3674b9f…` | Buds ACLs: **151** 07:02:45.890 → **2666** 07:08:33.441 (`0x13`); **2678** 07:10:20.388 → open at the end (07:11) — after the film |
| CAP-076-btsnoop_hci2.log.last | 71,981 B, 1,303 packets, `9c332bc7…` | holds the **start of part 2**: Buds ACLs **311** 07:01:07.655 → **489** 07:01:08.972 (`0x13`), **605** 07:01:13.785 → **1294** 07:02:39.649 (**`0x16`**, with an LE handle `0x0040` at 1295 — the Bluetooth **off** of BL-20); hci2.log starts 07:02:45 with Bluetooth on. One subscription, one read of 13, one `SABM` on DLCI 0x04 in it |
| CAP-076-opencontrol-debug-20261015-070741.txt | **461 lines**, 07:01:03.577 – 07:07:33.969; mode 750; `8b3ff719…` | "Permissions (start)" 07:01:03; Bluetooth adapter OFF → ON at 07:01:05 (!), LINK_BACK re-open 07:01:14; **4 subscribed, 4 read, 24 changed_by_buds, 8 set_by_app, 2 "from the settings stream (read)", 2 "from the mode read (read)", 2 "no mode settable", 0 changed_since**; Bluetooth ON → OFF 07:02:39 ("Session loss cause: Bluetooth was switched off on this phone"), OFF → ON 07:02:44, LINK_BACK 07:02:46; a session loss 07:03:00 + AFTER_LOSS 07:03:01; "Session ended by the user's Disconnect tap" 07:04:12 |
| CAP-076-OpenControl-for-Pixel-Buds-Pro-2-log-41fc9acad7b3.txt | 585 lines; the same header **with `userType: full.secondary`**, `package: …:10300`; UTC 04:08:17 – 05:07:57; mode 750; `66ade0a5…` | the app logcat of part 2 (its buffer reaches back to 06:08 local — the user-10 bound-service start) |

**The adb shell log (P0/P1), verbatim content to re-read:** `am get-current-user` → `0` (both times — the commands were run from the Owner, with
`--user 0` and `--user 10`); `pm list packages --user 0 | grep -i -E "gms|vending"` lists `app.grapheneos.gmscompat.config`, `…gmscompat.lib`,
`…gmscompat`, `com.android.vending`, **`com.google.android.gms`** (exit 0); `--user 10` lists only the three `app.grapheneos.gmscompat*` packages (exit 0 —
**the `grep` matched GrapheneOS's compatibility layer, so exit 0 is not a positive for Play in user 10; `com.google.android.gms` is absent there** — say it
with the lines); `dumpsys package` at 06:23:50: `versionCode=10202`, `lastUpdateTime=2026-10-14 18:35:00`, two `firstInstallTime` lines (2026-10-09 18:33:54
and 17:24:41 — two users); at 06:38:17: `versionCode=10300`, `lastUpdateTime=2026-10-15 06:36:57`, the same `firstInstallTime` lines (P1: an update, no
reinstall). **Not in the log:** Play's *Nearby devices* permission and version (P0 on film?), the APK pull and its `sha256sum` (the Info tab's hash on film is
the fallback), a `sha256sum` file, camera films (P3 — the ears; the audio and the stream packets must carry the hold timing instead).

**First look, to be re-derived (leads, not findings) — `tshark` on the Buds' handle `0x000b`, exit 0 each:**
- **The two new requests:** hci1.log **8** `SubscribeToSettingsChanges` REQUESTs sent (`frame contains 25:f5:ad:21:28:01:97:54:b0` or `…07:ee:8e:b2`) and **8**
  `ReadSetting 4:13` (`frame contains 2a:02:20:0d`), hci2.log **3 + 3**, hci2.log.last **1 + 1**, hci1.log.last 0 + 0 — against the exports' 8 and 4
  "subscribed" lines and the sessions (one Connect/re-open each): **one per MAESTRO session?** List every pair with its session and channel (19 or 21).
- **The stream:** **109** `4:{13:n}` SERVER_STREAM packets received in hci1.log (`frame contains 68:0n:08:07`), **34** in hci2.log; the exports' 86 + 22 and
  24 + 8 + 2 "from the settings stream" lines — reconcile packet by packet (a packet per line? **the `set_by_app` lines come in pairs ≈ 25–40 ms apart**
  (07:04:49.836/.861, 07:04:57.607/.646): two packets per own Set — the Buds echo the mode twice? one per bud? a `13` and another field? decode each).
- **Notifies and Sets:** hci1.log **110** `Notify` (`08 13 00 04 01`) and **11** `Set` (`08 12 00 14`); hci2.log 16 and 4. In user 0 the Notifies include
  Play's claims (Play receives them while it holds the channel): attribute each Notify to the claim it arrived on (`CAP-075` §1's owner rule).
- **Claims on DLCI 0x04/0x05:** hci1.log **40** phone-sent `SABM`s (the app's and Play's together — split them by the first phone message: `08 11` the app,
  `03 08 00 02 01 25` Play; **18 Play markers**), hci2.log 10 (0 Play markers — the positive control is hci1.log's 18). The verdict count: **the app's `SABM`s
  vs the taps/tile/pulls/Connects/re-opens** (the Connect's snapshot claim is expected once per session — ADR-065's Q7).
- **Sessions:** user 0: a Disconnect tap 06:39:41, then **seven session losses between 06:39:53 and 06:47:44 with only one ACL disconnect (06:42:23, `0x13`)** —
  the Buds' own `DISC` of DLCI 0x02 (the 12-of-77 case of `PROTOCOL.md` §4.1; a bud out/in — BL-9? the film), each followed by an AFTER_LOSS re-open 1.5 s
  later (one skipped by `CHAIN_GUARD_MS` at 06:42:57); the re-opens' reads answered **"no mode settable"** 4 times and "read" 5 times, **never
  "changed_since"** — was the mode ever different after a re-home, or always the same / 0? user 10: a Bluetooth off/on 07:02:39/44 (BL-20), a loss 07:03:00,
  a Disconnect 07:04:12.
- **The on-demand closes:** 4 "RFCOMM on-demand channel 0x04 closed" in user 0 at 06:45:58 – 06:46:28 — the app's own tap claims cut by Play's recovery
  (`CAP-075` §5; the known issue, BL-11): establish per tap whether the Set got its ACK before the cut and whether the stream packet still confirmed it.
- **User 10's first mode came from the stream** ("from the settings stream (read)" ×2): the first mode ever shown in that process arrived as a push before any
  read answered (the read answered 0 — no bud worn?) — check against `causeOfStream`.

---

## 3. Leads and context — verify each before relying on it

1. **Which user, which part** (🟡 until shown): part 1 = user 0 with Play (no `userType:` line in its app logcat; 18 Play markers; `am_proc_start … [0,11800,…]`;
   the shell log's `--user 0` list with `com.google.android.gms`); part 2 = user 10 without Play (`userType: full.secondary`; 0 Play markers; the `--user 10`
   list without `com.google.android.gms`). Confirm on film (the launcher, the wallpaper, the user icon in Quick Settings) and from the shell log; note that
   `am get-current-user` read 0 both times (run from the Owner before the switch) and that `logcat-all-1` ends 06:56 — part 2's user switch is not in it.
2. **The build on film:** part 1 starts on **1.2.2 build `bfd7891`** (the Info tab; the Settings tab **with** the "Noise control with Google Play services" card
   and its selected option — P1 asks for it on film) and updates to **1.3.0 build `ff27ee1`** at 06:36:57 (the shell log's `lastUpdateTime`; the full logcat's
   process starts; the Info tab afterwards, the card gone). Say which frames, claims and holds belong to 1.2.2 (the process of 06:35:42) and which to 1.3.0;
   only 1.3.0's count for the verdict. Part 2: 1.3.0 throughout? (its app logcat's `:10300` from 04:08 UTC — the bound-service start of 06:08 was already
   1.3.0? impossible — 1.3.0 was installed 06:36: re-read the header's meaning; `CAP-075` §0 explains the `package:` line as the current version at export time).
3. **The clock offsets:** both films' status bars (the minute changes at 10 fps) and a shared event with an export line (the update's restart at 06:36:59; the
   Bluetooth off at 07:02:39 — the Quick Settings tile on film); settle start/end for both `creation_time` stamps before any correlation.
4. **The audio tracks:** "I spoke only very little" — `silencedetect`/RMS per 100 ms, tabulate every non-silent segment with its film time, align each with a
   stream packet (a hold) or a tap; you cannot listen: label the words as the maintainer's statement, the timing as measured. Playback (AVDTP `START`/`SUSPEND`,
   AVRCP) — "during playback": say it with evidence.
5. **The press-and-hold evidence without a camera:** a hold is the Buds' change with no app `Set` before it: the stream packet `4:{13:n}` (direction 1) with no
   `08 12` in the 2 s before and no `Notify`-answered `08 11` of the app; the mode button on film within a second (the screen recording). Count per part:
   holds made (packets) vs shown (film) vs the mode list in force (`ReadSetting 12`'s answer at Connect: which modes a hold cycles through) and the cause line
   in the (i) when opened. The `80 ↔ 08` alternation of `CAP-075` lead 5 (a left vs right bud?) — record each packet with its time and the preceding mode;
   do not invent a rule.
6. **The own tap:** for each `Set` (11 + 4): `08 11` → `Notify` → `08 12` → ACK → the Buds' `Notify` → **the stream packet(s)** — the delay Set → stream
   (`CAP-051`: 269 ms), whether the export classed it `set_by_app` (the `ANC_SET_WINDOW_MS` rule), and the two-packets-per-Set lead of §2. A tap cut by Play
   (the four on-demand closes): the `AnswerCutOff` path and whether the stream packet still confirmed the mode (`ai-sessions/0091`'s test "after a cut-off
   answer").
7. **The re-homes:** for each "Session lost: channel 0x02 closed" without an ACL disconnect: the Buds' `DISC` of DLCI 0x02 on the wire (sender, time), what
   happened on the Buds (a bud out/in on film? a stream packet or a Notify just before it — `CAP-051` 1993 → 1996), the re-open 1.5 s later (`SABM`, the
   fifteen requests, the subscription and the read again — M1 on hardware), the read's value (0 = no bud worn? 1–4) and the (i) line if opened. The one
   `CHAIN_GUARD_MS` skip (06:42:57, 928 ms after a re-open) — what reconnected the session afterwards (the ACL at 06:42:46 … the next LINK_BACK?).
8. **Play's claims and the app's:** the claim inventory of DLCI 0x04/0x05 with both clients (`CAP-075` §1's method), Play's recovery/retry timers in this run
   vs `CAP-075`'s (5.3–5.8 s / 2.1–2.8 s), and — the point of 1.3.0 — **whether any hold was missed because of Play**: with the stream, none should be; if a
   hold is missing on screen, find its packet and the app's state at that moment (not visible? the session lost? the mode equal?).
9. **"No mode settable" at Connect/re-open** (4 + 2): the Settable `00` reading — both buds out of the ear at that moment (the film), the (i) "The Buds don't
   allow changing noise control right now …" line, a value 0 never shown as a mode (M2 on hardware), and the next 1–4 packet making it allowed again.
10. **Nothing held, nothing re-claimed:** 0 "hold"/"re-claim" lines in both exports (positive control: `CAP-075`'s export 2 has them); no `SABM` on DLCI 0x04
    from the app that is not a tap/tile/Refresh/pull/Connect snapshot (the verdict count); no `WriteSetting 4:{13:n}` anywhere (`frame contains 2a:…:68:`
    on a sent frame → 0, with a positive control on a received one).
11. **The other settings from the stream:** any stream packet for a field other than 13 (the official app on a second phone was not used — say so; the Buds
    may still push 16/12/2 after the app's own writes); any "not shown (not read by this app)" line (0 in both exports — positive control: the unit test).
12. **Test-IDs** (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`; `AGENTS.md` §13 step 7): `PAIR-003`, `BATT-004`, `FW-003`, `ANC-001`…`ANC-004`, `TOUCH-007` (the skeleton's
    list); `CASE-004`/`CASE-005`/`INEAR-002`/`INEAR-003` if buds went in/out on film. Re-derive from what the run actually did; "expected but not observed" for
    each that was not — and the skeleton's step mapping BL-1 … BL-25 (done / not done / done differently), part 1 and part 2 separately. The maintainer said
    the plan was not followed: expect many "not done".
13. **Privacy:** the recordings' audio carries the maintainer's voice; the screens may show the Wi-Fi name in Quick Settings and the serial numbers on the Info
    tab (allowed on film, never unredacted in text — first 4 + last 2); the logcats print `XX:XX:XX:XX:6E:07`-style addresses; the system log and the full
    logcat match the privacy pattern thousands of times — **commit an extract only** (the `awk` of `CAP-071`/`CAP-075`: the time window of part 1, the app's
    and the Bluetooth process's PIDs, every line naming the app, `com.android.bluetooth` or `NearbyDiscovery`/`gms`), keep the full files local
    (`~/opencontrol-capture-originals/CAP-076/`), name the extract CAP-076-logcat-extract.txt.
14. **External validation** where a claim rests on a specification or platform behaviour (fetch, quote with the date): Android's user types (`UserManager`,
    "full.secondary"), `BluetoothSocket`'s one-connection-per-channel behaviour (AOSP), the Bluetooth Core Specification for the `0x13`/`0x16` reasons and
    RFCOMM's `DISC` semantics, the Fast Pair Message Stream page, Pigweed's pw_rpc packet types (`SERVER_STREAM` = 7) — re-fetch only if a new claim needs them.

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename, mode change, delete or `git add`)

1. Create the RESULT (§0). Record `git log -1`, `git status --short`, `git fetch`, `git log --oneline HEAD..origin/main`, the open PR #56. Work on
   **`feature/settings-stream`** (`git switch feature/settings-stream && git pull --ff-only`); never on `main`; never rebase or amend the build commit `ff27ee1`.
2. List the folder fresh (`ls -la`, `sha256sum`), diff it against §2's tables and say every difference.
3. **Checkpoint 0 (`AskUserQuestion`, one question per decision, options with pros and cons, "(Recommended)" on one, the exact commands in the preview):**
   the users of part 1 and part 2 (the logs' reading); the folder rename to the recordings' first/last phone times (one day: `CAP-076-2026-10-15_HH-mm-ss_HH-mm-ss-Group_BL`)
   and whether the recordings keep their names; `chmod 644` on the files with mode 750; a `sha256sum` file; the privacy treatment of the recordings (blur
   ranges or as is; the audio stays) and of the two system-log files (an extract committed, the full files local); the empty `test` file in the repository
   root; whether the phone can be attached for the APK pull (`adb shell pm path --user <id> …`, `adb pull`, `sha256sum` against `~/opencontrol-1.3.0-tested`)
   and for Play's *Nearby devices* permission state (`TODO.md` §3); whether the originals go to `~/opencontrol-capture-originals/CAP-076/`.
4. Update the `CAP-076` row of `id_registry.csv` (it exists with status `planned`; `analyzed` in Phase D) and prepare the Group BL status line of
   `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (written in Phase D).

### Phase A — both screen recordings, in full

5. Decode each recording at 1 frame per second into the scratchpad in ranges (part 1: 981 frames; part 2: 349); compare each second with the previous one; view
   **every** second with a change on labelled contact sheets; narrow transitions at 10 fps where a claim depends on them (the mode buttons after a hold, the
   (i) dialogs, the connection state and the loss line, the "Updaten…" screen, the Settings tab before and after the update, the Quick Settings Bluetooth
   tile). No sampling: every second is either viewed or proved unchanged.
6. The audio tracks: extract each (`ffmpeg -vn`), derive the non-silent segments with `silencedetect`/RMS per 100 ms and tabulate every segment with its film
   time; align them with the stream packets and the taps. Detect music playback the same way and with AVDTP/AVRCP.
7. **Measure both clock offsets** (status-bar minute changes at 10 fps; a shared event with an export line) with their uncertainty, and settle start/end for
   both `creation_time` stamps before any correlation.
8. Write the timeline into the existing CAP-076-EVENT-NOTES.md: phone time · what happened · step BL-n / Test-ID · evidence — in `CAP-071`/`CAP-075`'s layout
   (the status line becomes "captured 2026-10-15 … and analyzed …", Log Metadata filled in for both parts, the capture-integrity pre-flight incl. the
   `.last`/current boundaries, the review method and privacy, the Event Timeline — part 1 then part 2, each incl. "before the recording" and "after the
   recording" — the step mapping BL-1 … BL-25 done / not done / done differently, Test-IDs per `AGENTS.md` §13 step 7 with "expected but not observed", the
   analysis checklist ticked; the planned procedure kept below as the appendix).

### Phase B — the HCI logs and the other logs, in full

9. All four HCI logs: Connection Complete events and the Buds' handle (by address; `bluetooth.addr` may match nothing — show the filter matching a known
   frame), the boundaries between `.last` and current, the RFCOMM inventory per DLCI and direction, **every pw_hdlc frame CRC-32-checked** (`scripts/pwrpc_decode.py`,
   with its positive control; a payload holding several HDLC frames split on `7e`), every Message Stream message decoded, **the full claim inventory of DLCI
   0x04/0x05 with the sender of every `SABM`/`UA`/`DISC`/`DM` and the owner of every claim** (the app's vs Play's), the Play marker counts with the positive
   control, **every `4:{13:n}` stream packet with its time, value, channel and what preceded it** (a `Set`, a `Notify`, nothing), every `ReadSetting 4:13`
   REQUEST/RESPONSE pair per session, the fifteen Connect requests byte for byte against `ai-sessions/0091` RESULT §A.1 / `CAP-075` §2 (order, channel, once
   per session), every write (expected: the Connect reads, the subscriptions, the Sets — nothing else), AVDTP/AVRCP state. Commands and exit statuses in
   FINDINGS (`PROJECT_RULES.md` rule 4a; `AGENTS.md` §13 step 8 for every negative).
10. Both exports and both app logcats: every line read (1,061 + 461 + 731 + 585) — every export line matched to the code's log string (`BudsRepositoryImpl.kt`,
    `RfcommBudsTransport.kt`, `SessionReopener.kt`, `SessionDiagnostics.kt`, `OsConnectionObserver.kt`, `MainActivity.kt`): the subscriptions and reads per
    session, every "from the settings stream (<cause>)" line against its packet and the cause rule, the "no mode settable" lines, the session losses and
    re-opens with their delays against `LOSS_REOPEN_DELAY_MS`/`CHAIN_GUARD_MS`, the on-demand closes, the claim lifetimes (`SABM` → `DISC`) against
    `MESSAGE_STREAM_LINGER_MS`, the connect failures and retries; the logcats' `events` lines (process start/stop, `wm_*`), the `BluetoothSocket` close
    lines, crashes/ANRs/StrictMode (`grep -c` with a positive control).
11. The adb shell log (every line; P0/P1's meaning as in §2), the system log and the full logcat (part 1 only): `grep`/`awk` by time window and PID — the
    app's process starts and user, the update at 06:36:57 (`PackageManager`/`installd` lines), Play services' `NearbyDiscovery`/Fast Pair lines around every
    on-demand close and every app `SABM` (the collision's stack path — `CAP-075` §5's lines), the Buds' `DISC`s of DLCI 0x02 (the stack's socket close lines),
    any crash. Write the extract command and its line count; commit the extract only (Checkpoint 0).

### Phase C — correlation, the findings, the comparison

12. Correlate every timeline row with its frames and log lines. Write CAP-076-FINDINGS.md in `CAP-071`/`CAP-075`'s structure: §0 build (1.2.2 → 1.3.0 on
    film; the shell log's `dumpsys`; the Info tab's hash = `ff27ee1`), install, **users** (the evidence per part side by side), logs, privacy; §1 ACLs and the
    claim inventory per part (both clients in part 1); **§2 the Connect sequence byte for byte: the fifteen requests, the two new ones once per MAESTRO
    session (Connects and re-opens), their position, channel 19/21**; §3 every write (the Sets; no `WriteSetting 4:13`); **§4 the holds per part: every
    stream packet, the mode on screen within a second (yes/no, the film time), the (i) line, the mode list in force, the misses and their cause** — the
    maintainer's core claim verified or refuted; **§5 the own taps: Set → ACK → Notify → stream packet(s), the delay, the cause classed, the four taps cut by
    Play**; **§6 the re-homes and re-opens: each Buds-side `DISC`, the re-open's subscription and read (M1 on hardware), the read's value and the (i) line, the
    `CHAIN_GUARD_MS` skip**; §7 Bluetooth off/on in part 2 (BL-20); §8 playback; **§9 `CAP-075` (1.2.1, the same users) vs `CAP-076` (1.3.0)** — a table per
    aspect (holds made / shown / missed per user, the app's `SABM`s per user, Play's claims and timers, the first-hold-only pattern gone?, the session losses);
    **§10 the counts for the verdict** (the skeleton's analysis checklist: the app's `SABM`s vs the user's actions; holds shown vs made; stream packets vs
    Notifies; the first read's value after every Connect/re-open); §11 the skeleton's "Refuted if" applied literally per step run; **§12 the verdict table
    (`RELEASING.md` C4)** — what the run verifies for 1.3.0 (the stream in both users; the removal), what it does not (the steps not done: the EQ/switch from a
    second phone, the tile?, the camera), heavy/minor/OK, and the analysing session's view — the decision is the maintainer's; §13 open questions and
    proposals (the two-packets-per-Set lead; `PROTOCOL.md` §6's wear-change item: does this run settle the 🟡 "stored mode per wear state"? — a promotion
    needs the maintainer's approval; the per-tap collision). FACT / HYPOTHESIS / ASSUMPTION / OPEN QUESTION on every conclusion (`PROJECT_RULES.md` §1); every
    claim with a frame, a log line or a film time.
13. Validate externally (§3 item 14): fetch, quote with the date.

### Phase D — checkpoint, documentation, finish

14. **Checkpoint (`AskUserQuestion`):** the users' final reading (if not settled at Checkpoint 0); **the verdict** (with the pros and cons: `RELEASING.md` C4,
    `PROJECT.md`'s Definition of done, `CHANGELOG.md`'s Known-issues lines — keep, reword or add); **the ADR-065 Update** (the numbers of both parts,
    "hardware-verified: …" — a draft in the preview; `AGENTS.md` §6: nothing written without the maintainer's answer in this chat) and any `PROTOCOL.md` note
    (§4.1: the stream in OpenControl's own logs; §6: the wear-change item — promotion or not); whether a FEATURE prompt follows (the two-packets lead; the
    per-tap collision — a draft title in the preview); the privacy treatment; whether the folder's untracked files are committed now (Git LFS,
    `.gitattributes` covers `captures/**` `.log`, `.log.last`, `.mp4`, `.txt`) on `feature/settings-stream` after the build commit. Record the answers
    verbatim in the RESULT.
15. With the answers: `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 (the `CAP-076` row: the real times, both users, analyzed) and the Group BL status line;
    `id_registry.csv`; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the Group column: BL on the Test-IDs exercised); `APP_TESTPLAN.md` §Y (the Result column per row,
    the Summary row, a run note like §X's); `TODO.md` §1/§2/§3/§4/§5; `ARCHITECTURE.md` §5a (the "Changed by the Buds" row: what `CAP-076` verified) and
    §6.0b (one sentence, as approved); `DECISIONS.md` ADR-065 Update (as approved); `PROJECT.md`/`README.md`/`CHANGELOG.md` only as approved; the PR #56
    body's C2/C3 lines (`gh pr edit`) as approved. `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and `python3 scripts/lint_docs.py` (exit 0).
16. The RESULT: plain-language summary first (what happened on the phone in each user, whether every hold showed and why not where it did not, what the two
    requests did on the wire, what the maintainer decides next), the phases, the files read in full / in part, "Deferred documentation" (each also in
    `TODO.md`), "Commits"; the `ai-sessions/INDEX.md` row (0092; the 0091 row's Status gets the back-fill "hardware run analysed by 0092").
17. Show a short summary and **ask whether to commit and push**. Only after a yes: commits per concern (Conventional Commits: `docs(capture)`, `docs`), each
    with its *why* and the attribution line; Git LFS for the capture files; on `feature/settings-stream` after `ff27ee1`; `git fetch`; push without force;
    never merge; the release steps D1–D4 are the maintainer's.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Scope:** analysis and documentation only. No change under `android/` (improvements are proposals), no new skeleton, no release step, no `dist/` change, no
  `scripts/release.sh`, no tag, no merge, no rebase of the build commit.
- **Approvals only in this chat** (`AGENTS.md` §6): no 🟢 FACT promotion, no new ADR or ADR Update, no verdict recorded as the maintainer's without their
  answer here; ADR-060 stays a draft. "Every hold showed" is answered per user with the evidence of this run — never by the maintainer's impression.
- **Evidence:** every claim cites a frame (log and number), a log line or a film time; a negative needs its command, its exit status and a positive control
  (the Play marker's positive control is hci1.log's 18; the "re-claim"/"hold" lines' positive control is `CAP-075`'s export 2); a hypothesis names its
  settling experiment. The maintainer's statements are statements until verified — including which user each part was made in, "1.2.2 at the start" and
  "the ANC modes were good".
- **No sampling:** every second of both recordings, every packet of all four HCI logs on the Buds' handle, every line of both exports, both app logcats and the
  shell log — reviewed or covered by a stated, positive-controlled filter; the system log and the full logcat by stated filters over the full time window of
  part 1.
- **Privacy:** no serial unredacted, no MAC, no phone number, no account name or e-mail, no Wi-Fi name in any committed text; the system-log files are
  committed as an extract only; the recordings' treatment and the originals' place are Checkpoint 0's.
- **Files:** before moving, renaming or deleting anything, diff a fresh listing; never wildcard deletes; ask first. `android/domain/bin/` is never staged.
- **Subagents** may only read and report; every write is done and verified in the main session.

## 6. Deliverables

- CAP-076-EVENT-NOTES.md (the timeline of both parts added to the skeleton) and CAP-076-FINDINGS.md (the users settled, the two requests per session, every
  hold against the screen, the own taps, the re-homes and re-opens, the `CAP-075` comparison, the counts, the verdict table), with the folder and files as
  approved in Checkpoint 0 (incl. the system-log extract).
- The registry, index, test-plan, `TODO.md`, architecture, ADR-065 Update, PR-body and status updates as approved; a FEATURE-prompt proposal if one follows.
- ai-sessions/0092_CAPTURE_RESULT_2026_10_15.md and the `ai-sessions/INDEX.md` row.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0092_CAPTURE_PROMPT_2026_10_15.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0092_CAPTURE_PROMPT_2026_10_15
