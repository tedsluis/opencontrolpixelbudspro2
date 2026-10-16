# 0095_CAPTURE_PROMPT_2026_10_16.md — Full analysis of CAP-077 (Group BM: the release-signed 1.3.1 build `7aa524b`, the fixes of `ai-sessions/0094`, in the secondary user without Google Play)

**Number:** 0095
**Category:** CAPTURE
**Date:** 2026-10-16
**Title:** Fully analyse `CAP-077`: **one** Android screen recording (no audio track), **two** HCI snoop logs, one app debug export, one app logcat, one
full `logcat -b all` and one adb shell log, recorded on 2026-10-16 with the release-signed **1.3.1 build `7aa524b`** (`ai-sessions/0094`, PR #61 merged as
`9bc09dd`; release branch `release/1.3.1`, PR #64; `scripts/release.sh 1.3.1` run by the maintainer on 2026-10-16, APK SHA-256
`9be3a89c94d3db7236416a244d2fea51e0524c6e5806d2e648faf4ca0611ee0a`, certificate `a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb`) on a
Pixel 9a with GrapheneOS (Android 17), in the **secondary user without Google Play**, the update 1.3.0 → 1.3.1 on film. The task: establish from the film,
the wire and the logs what happened; which of the 1.3.1 fixes (A93-APP-01 … 04, 07, 08) the run exercised and whether each behaved as built; whether every
press-and-hold reached the app through the settings stream; whether anything on the wire was unexpected; record the findings; propose the release verdict
(`RELEASING.md` C3/C4).
- Record the real events in the existing skeleton **CAP-077-EVENT-NOTES.md** (written by `ai-sessions/0094`; the timeline is added to it, the planned
  procedure stays as the appendix, as `CAP-076` did) and the analysis in a new CAP-077-FINDINGS.md.
- **No change to the app** in this session (improvements are **proposed**, with the evidence, for a later FEATURE session), **no new capture skeleton**,
  **no release step** (no tag, no `dist/` change, no merge): the analysis, its documentation and the verdict proposal for the maintainer.

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this repository. Run the
phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Take the time to read the core files first, in full and in this order, to
understand the ground rules before analysing anything:
- `AGENTS.md` and `PROJECT_RULES.md`.
- `PROJECT.md` (the Definition of done; the status paragraph: 1.3.0 released, 1.3.1 prepared).
- `ARCHITECTURE.md`, in particular §3.1 (the settings-stream bullet with its **"since 1.3.1"** sentences: the kept "changed since" cause, stream replies that
  never end a read, the `Get`'s answer that never ends a `Set`), §3.2 (the timing constants), §5a, §6.0b, §7, §9 (the "Use different Buds" paragraph with the
  shared `PairingChoiceMemory`), §12, §13, and the accessibility sentence of 1.3.1.
- `PROTOCOL.md`, in particular §2.2a and §2.3 (the channels; DLCI 0x04 **or 0x05**; channel 19 vs 21 and the ADR-034 addresses), §4.1 with its Updates
  (setting 13 = the mode; the Corrections of 2026-10-15), §4.5, §6 (incl. the CONV-002 Update of 2026-10-15 on DLCI 0x08), §8.
- `DECISIONS.md`: **every** ADR, ADR-001 … ADR-065, with every dated Update, and the draft ADR-060 (not a decision). In particular ADR-065 (items 1–6 and its
  Update of 2026-10-15 — `CAP-076`), ADR-064, ADR-032, ADR-044 (the re-open), ADR-049 (Settable `00`), ADR-057 (what is "current").
- `TODO.md`, §1 (1.3.1 → `CAP-077`, then `RELEASING.md`), §2, §5, §6.

These are the ground rules; do not analyse or change anything before they are read. Then, per task:

- **Procedure and registers:** `AI_SESSION_LOG_PROCEDURE.md` (in full, incl. §4b, §5 and §9), `ai-sessions/INDEX.md`, `id_registry.csv` (the `CAP-076`,
  `CAP-077`, ADR-065 rows and every Test-ID named in §3 item 11), `RELEASING.md` (the Release checklist, "Repository rules", §6, §11a, §13).
- **The build under test and its skeleton — read in full:** `ai-sessions/0094_MAINTENANCE_RESULT_2026_10_15.md` (the ledger rows A93-APP-01 … 08 with what was
  changed and how it was tested, the mutation table, the gate); the `ai-sessions/0093_AUDIT_RESULT_2026_10_15.md` findings A93-APP-01 … 08 (the defects as found);
  **CAP-077-EVENT-NOTES.md** (the skeleton: P0–P5, the reference-frame table, BM-1 … BM-9, the expected screen and wire per step); `CHANGELOG.md` `[1.3.1]`
  (Fixed / Changed / Known issues); `APP_TESTPLAN.md` §Z (Z1 … Z9).
- **The "before" — read in full:** `captures/CAP-076-2026-10-15_06-35-26_07-08-20-Group_BL/CAP-076-FINDINGS.md` (§2 the Connect's seventeen requests; §4 the holds;
  **§5.1–§5.3 the own taps and the silent tile tap; §6.2 the overwritten "changed since"**; §13 the proposals P1/P2) and its EVENT-NOTES (the method: the clock
  offset from the status bar, the claim-owner rule, the logcat extract and its command); the maintainer's named examples
  `captures/CAP-071-2026-10-08_17-51-29_18-27-28-Group_BG/CAP-071-EVENT-NOTES.md` and `…/CAP-071-FINDINGS.md` (the layout: metadata table, P0/P1 outputs,
  pre-flight, review method and privacy, one timeline row per event, step mapping, Test-ID traceability); `ai-sessions/0092_CAPTURE_RESULT_2026_10_15.md`
  (the method and the scratch scripts — re-create them, they are not in the repository).
- **Every Kotlin source a finding touches, in full,** before you write about it — at least: `BudsRepositoryImpl.kt` (`causeOfReadAnswer` with `readRound` and
  `changedSinceBy`, `causeOfNotify`, `causeOfStream`, `applyAncState` and its log line, `handleRoutedFrame`'s `Notify` branch — no `AncOutcome.Notified` for a
  `Get`'s answer — and its `Setting`/`Eq` branches with the `stream` flag, `readAncSetting`/`readEq`/`readSetting`'s accept predicates, `refreshAncMode` and
  `_ancModeFresh`, `ancSetOnClaim`, `sendAndAwaitOnClaim`, `launchInitialEqRead`, `openSession`, **every log string — each export line is matched against
  them**), `codec/CodecRouter.kt` (`routeMaestro`), `hardware/…/BudsCompanionPairing.kt` (`PairingChoiceMemory`, `forgetAssociations`, `lookupBonded`,
  `requestAssociation` and its `try/catch`), `RfcommBudsTransport.kt` (`attach`, `closeAll`, the reader's `read return: -1` path), `SessionReopener.kt`,
  `AncTileService.kt`, `ui/…/SettingsMenu.kt` (the confirmation dialog), `SettingsUi.kt`/`ControlsScreen.kt`/`DebugScreen.kt`/`EqScreen.kt` (the toggleable
  rows and slider semantics), `AncScreen.kt`, `domain/AncMode.kt` (`label`), `app/…/MainActivity.kt` and `app/…/di/RepositoryModule.kt`.

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.**
- Create the RESULT file ai-sessions/0095_CAPTURE_RESULT_2026_10_16.md at the very start, with `**Status:** partial — resumed` and a **Progress** block.
- Update that block at the end of every phase **and** after every substantial step: every minute of the recording reviewed, every HCI log and channel
  decoded, every log file read, every checkpoint answer. Say what is done and what is next, which files are touched but not yet verified, and where the
  intermediate results live (the scratchpad: scripts, contact sheets, decoded tables, notes — re-create them if it is gone).
- A resumed session reads this prompt, then the RESULT's Progress block, re-reads the files the unfinished step touched, and continues from there. It never
  redoes a finished, recorded step and never assumes an unrecorded one was done. The prompt keeps its number and date; the RESULT grows in the same file.

**Memory and the machine.** The laptop has 15 GB and `/tmp` is a 7.7 GB tmpfs; Claude Code was OOM-killed twice this week during long sessions. Run
`tshark`/`ffmpeg` one job at a time; extract contact sheets and crops in ranges, never the whole film into memory (188 MB, 864 s); the full logcat (25.6 MB,
197,535 lines) — `grep`/`awk` it, never read it whole into context. No Gradle build is needed (if one is, `--max-workers=1`). Never `git worktree add` a
commit (it checks out `captures/`, gigabytes). Scratch files go into a fresh `mktemp -d` under `/tmp/claude-1000/`; write their names in documents without
backticks (the docs lint flags backticked names that do not exist). `python3 scripts/pwrpc_decode.py <log>` decodes the pw_rpc frames.

**Nothing is taken on trust, including this prompt, the maintainer's statements, the 0094 RESULT and the memory notes.** Every claim and every number in §2
and §3 is re-derived from the recording, the HCI logs, the other logs, the code and official documentation.

---

## 1. What the maintainer asked for (chat, 2026-10-16, translated from Dutch)

1. "Make a new prompt for Claude Code, in English, per the project's rules in `ai-sessions/`, that can be run in a new session. You are an expert software
   architect, reverse engineer and technical auditor. Your task is a comprehensive analysis of `CAP-077` (Group BM) of the OpenControl app, recording the
   findings. Take the time to read the core files first (`AGENTS.md`, `PROJECT_RULES.md`, `PROTOCOL.md`, `ARCHITECTURE.md`, `DECISIONS.md`) to understand the
   ground rules before auditing the rest. Run everything in phases, sequentially; when you hit a token limit you must be able to continue automatically
   later."
2. **The maintainer's statements about the run (verify each; label each as the maintainer's statement until verified):**
   - "**I did not keep to the test plan.**" — *map every skeleton step BM-1 … BM-9 as done / not done / done differently; a step not done is "expected but
     not observed" in the Test-ID traceability, not a failure.*
   - "The app was version 1.3.0 (of yesterday) at the start of the video. During the video (CAP-077-recording.mp4) I did the upgrade to 1.3.1." — *the Info
     tab on film, the shell log's `dumpsys` lines, the full logcat's `am_kill … killDueToPackage` and `am_proc_start` lines and the export's first line decide.*
   - "The capture was made on a Pixel 9a with GrapheneOS Android 17 in a profile without Google Play services." — *the app logcat's header, the shell log's
     package list, the user switch in the full logcat, the Play-marker count with its positive control.*
   - "I did not speak during the video." — *the recording has no audio track (§2); say so with the `ffprobe` line.*
   - "Everything in the app seems to work well."
   - "**The ANC modes during playback were good! When I did a long press on a bud, the ANC mode in the app changed too!**" — *the core claim: count every
     press-and-hold (the stream packets `4:{13:n}` not caused by the app, the mode buttons on film) against the mode shown within a second; say how many were
     shown, how many not, and why for each miss; "during playback" with AVDTP/AVRCP evidence.*
3. **The analysis steps the maintainer named (§4 follows them):** first the recording → CAP-077-EVENT-NOTES.md with every event and action and its time; all
   CAP-077-btsnoop_hci* logs with `tshark`; the debug export; the app logcat; the full logcat; the adb shell log; **the System log (CAP-077-System-log-*.txt —
   no such file is in the folder, see §2)**; then correlate the timeline with every log (`CAP-071-EVENT-NOTES.md` as the example); a comprehensive
   CAP-077-FINDINGS.md (`CAP-071-FINDINGS.md` as the example); **look for what is not expected in the HCI logs**; get as much as possible out of the analysis.
4. Strict rules (the maintainer's): no assumptions — verify everything; validate claims externally against official documentation where needed (Android
   Developer Docs, the Bluetooth Core Specification, the Fast Pair specification); **no sampling** — a 100 % complete review of the relevant files; evidence
   traceability (every claim links to a frame, a log line or a film time) and structural integrity.
5. Standing practice: no commit or push without the maintainer's "yes" in chat; no release step; `main` takes no direct push (`RELEASING.md`).

---

## 2. The evidence (measured 2026-10-16 by the session that wrote this prompt — re-derive every number)

**State of the repository:** branch **`release/1.3.1`** at **`7aa524b`** (= the build commit of B1), pushed; **PR #64** open (A1–A6 and B1–B3 ticked; C1–C4
open; "do not merge yet"); `main` at `9bc09dd`. Per `RELEASING.md` C3 this session's commits go **on `release/1.3.1`, after the build commit** — they change no
app file, so the tested APK stays the build of B1. Untracked: the seven capture files below (the skeleton file is committed) and `android/domain/bin/` (build
output, ignored — never staged).

The folder `captures/CAP-077-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BM/` (the skeleton's placeholder name) holds, per `stat` and `sha256sum` of 2026-10-16 (size,
mode, the first eight hex digits of each SHA-256):

| File | Measured | Notes |
|---|---|---|
| CAP-077-EVENT-NOTES.md | 8,524 B, 644, `6f88e5ae…`, **identical to the commit** (`bbc45fe`) | the skeleton: add the timeline, keep the procedure as the appendix |
| CAP-077-recording.mp4 | 188 MB, 740, `0c6e0318…`; `ffprobe`: **863.54 s**, 1080 × 2424, **no audio stream**, `creation_time` 2026-10-16T05:10:38Z (= 07:10:38 local) | if the stamp is the end, the film starts ≈ 06:56:15 — **derive it from the status bar**; it should hold the update (the `am_kill` at 06:58:05.898) |
| CAP-077-btsnoop_hci.log.last | 73,076 B, 644, `5c61200f…`, **1,368 packets**, 06:48:05.214 – 06:56:37.570 | Buds ACL (Connection Complete for `…:cf:6e:07`, handle `0x000b`) **686** 06:56:27 → Disconnection Complete **1350** 06:56:37, reason **`0x16`**, with an LE handle `0x0040` at 1353 (`0x16`) — a Bluetooth **off**; one subscription (867, 06:56:28.463) and one read of 13; one `SABM` on DLCI 0x04 |
| CAP-077-btsnoop_hci.log | 410,108 B, 644, `989a9df0…`, **7,368 packets**, 06:56:41.715 – 07:12:00.360 | Buds ACLs: **151** 06:56:42 → **1589** 07:00:17 (reason **`0x13`**); **1678** 07:00:35 → open at the log's end |
| CAP-077-opencontrol-debug-20261016-071117.txt | **706 lines**, 06:58:07.853 – 07:11:14.281, mode 740, `b01f40d3…` | one process (the 1.3.1 one) from "Permissions (start)"; see the first look |
| CAP-077-OpenControl-for-Pixel-Buds-Pro-2-log-124d87c06bf0.txt | **701 lines**, mode 740, `9b9beef5…`; header `osVersion: google/tegu/tegu:17/CP3A.261005.005/2026100601:user/release-keys`, **`userType: full.secondary`**, `package: …:10301, targetSdk 34`; UTC 04:48:01 – 05:11:24 | the app logcat (App info → View logs) |
| CAP-077-logcat-all.txt | 25.6 MB, 644, `d3568e5a…`, **197,535 lines**, `adb logcat -b all` (no header), 10-08 21:47 – **10-16 07:10:32** (local); 1,017 lines name the app; **8,613 match a broad privacy pattern** (`gmail\|@\|ssid`) | the phone log; **never committed in full** — an extract only (as `CAP-076`) |
| CAP-077-adb-shell.log | 1,059 B, 644, `a580d38f…` | P0/P1 — see below |
| **CAP-077-System-log-*.txt** | **not in the folder** (`ls -la`, 2026-10-16) | the maintainer named it in the analysis steps: ask at Checkpoint 0 whether it exists elsewhere; otherwise record it as not provided — the full logcat's `system`/`main` buffers cover the window |

**The adb shell log, verbatim content to re-read:** at 06:53:39 `pm list packages --user 10 | grep -i -E "gms|vending"` lists only the three
`app.grapheneos.gmscompat*` packages (`exit=0` — **the `grep` matched GrapheneOS's compatibility layer, so exit 0 is not a positive for Play; no
`com.google.android.gms`, no `com.android.vending`** — say it with the lines); `am get-current-user` was run but **its output is not in the log** (it went to
the terminal); `dumpsys package` at 06:54:22: `versionCode=10300`, **`lastUpdateTime=2026-10-15 09:11:11`** (not `CAP-076`'s 06:36:57 — something updated the
app again on 2026-10-15 at 09:11: find it in the full logcat's `PackageManager`/`installd` lines and say what was installed), two `firstInstallTime` lines; at
06:58:37: `versionCode=10301`, `lastUpdateTime=2026-10-16 06:58:05` (P1: an update, no reinstall). **Not in the log:** Debug mode on before the update (P1 —
the film?), the APK pull and its `sha256sum` (the Info tab's hash on film is the fallback).

**The process story from the full logcat (re-derive):** `am_proc_start` of the app in user 0 at 06:34:08 (PID 22144, bound-service; died 06:47:01) and
06:47:53 (PID 25390; died 06:48:02); **`am_switch_user: 10` at 06:47:59.689**; user 10's process **PID 25520** at 06:47:59.739 (bound-service — 1.3.0);
**`am_kill … killDueToPackage` at 06:58:05.898**; **PID 28176** at 06:58:07.419 (top-activity — 1.3.1). Check: are there crashes of the app in the window?
(`FATAL EXCEPTION` occurs 4× in the log, all `in.sunilpaulmathew.izzyondroid` on 10-12 … 10-15 — not the app; re-derive with `ANR in` and the window.)

**First look, to be re-derived (leads, not findings) — `tshark` on the Buds' handle `0x000b`, exit 0 each:**
- **Sessions on the wire** (DLCI 0x02 `SABM` → `SubscribeToSettingsChanges`): .last 763/867 (06:56:27/28 — 1.3.0, before the Bluetooth off); hci.log 399/525
  (06:56:42/43 — 1.3.0 after Bluetooth on), **1010/1108** (06:58:08/09 — 1.3.1's LINK_BACK re-open), **1755/1859** (07:00:35/36 — the Connect after the new
  association), **2201/2306** (07:00:45/47 — the AFTER_LOSS re-open, channel 19), **4359/4452** (07:04:36/37 — the Connect after the second Disconnect).
  Requests: 1 + 5 `SubscribeToSettingsChanges` (`frame contains 25:f5:ad:21:28:01:97:54:b0` or `…07:ee:8e:b2`, sent) and 1 + 5 `ReadSetting 4:13`
  (`frame contains 2a:02:20:0d`); the export has **4** "Settings stream subscribed" (it starts at 06:58:07 — the 1.3.0 sessions are before it). One pair per
  MAESTRO session? The seventeen requests byte for byte per session (`CAP-076` §2).
- **The stream:** **39** `4:{13:n}` SERVER_STREAM packets received in hci.log, 0 in .last (`frame contains 68:0n:08:07`, `n` = 0…4, received); the export's
  **9 "(changed_by_buds)"**, **22 "(set_by_app)"**, **8 "(unchanged)"** (1.3.1's new log word for a packet that changes nothing — A93-APP-08), 0 "(read)" from
  the stream — reconcile packet by packet (two packets per own `Set` — channels 21 and 19, `CAP-076` §4 — 11 Sets × 2 = 22?).
- **The own taps:** **11 `Set`s** (`08 12 00 14`): **6 between 07:01:53 and 07:02:16** — the export's **6 "ANC tile tapped"** lines (07:01:51.969 … 07:02:15.532),
  each followed by a `Set` 1.0–1.3 s later (**BM-5, A93-APP-03: no silent tile tap?**) — and 5 more 07:02:27 – 07:02:47 (the app's buttons?). 18 `Get`s
  (`08 11 00 00`), 20 phone-sent `SABM`s on DLCI 0x04/0x05, 40 `Notify` frames; **0 Play markers** (`03 08 00 02 01 25`, sent) in both logs — positive control:
  `CAP-076-btsnoop_hci1.log` → **18**. Attribute every claim (the app's: tile, buttons, Refresh, pulls, the Connect's snapshot) and say why there are 20 `SABM`s
  for 11 `Set`s.
- **"Use different Buds" (BM-7/BM-8, A93-APP-01):** export 07:00:05.202 "Session ended by the user's Disconnect tap", **07:00:05.206 "Pairing: use different
  Buds — removed 0 association(s) of this app"** (0 — why? did user 10's app ever have a CDM association, or was it always the name fallback? the 0093/0094
  analysis assumed one), the ACL's end **07:00:17** (`0x13`, 1589 — who ended it, and why 12 s later?), 07:00:18.280 "association requested (CDM picker)",
  07:00:24.052 "association created", "already bonded — no createBond()", the ACL **07:00:35** and the Connect's `SABM` 07:00:35.744. **Was any Connect tapped
  between 07:00:05 and 07:00:18, and did the app refuse to connect by name?** (the film; the wire: no DLCI 0x02 `SABM` in that window). Was the Cancel path
  (BM-7) shown? the dialog's texts on film; a rotation?
- **"Changed since the app last saw it" (BM-4, A93-APP-02):** **0 "changed_since"** in the export; the reads answered **"no mode settable"** twice (06:58:09,
  07:00:36 — no bud worn?) and **"(read)"** twice (07:00:47, 07:04:37). Was a mode ever changed while disconnected (07:04:34 Disconnect → 07:04:37 Connect is
  3 s)? If not, the fix was **not exercised** — say so; do not count a missing line as a pass or a fail.
- **The session loss:** 07:00:43.373 "Session lost: channel 0x02 closed … read return: -1", 07:00:44.877 AFTER_LOSS re-open (1.5 s — `LOSS_REOPEN_DELAY_MS`),
  channel **19** after channel 21 — the Buds' own `DISC` (a bud in/out? the film)?
- **BM-6 (A93-APP-04):** a press-and-hold within ≈ 1 s of a Connect — is there a stream packet between a session's `ReadSetting 4:13` REQUEST and its
  RESPONSE? If yes, did the read end with the RESPONSE (the export's "Noise-control mode read (channel N)" after the RESPONSE's time)? If not, not exercised.
- **The labels and TalkBack (BM-2/BM-3, A93-APP-07/08):** the film only — "Noise cancellation"/"Off"/"Adaptive"/"Transparency" on the buttons, the (i)
  ("ANC mode: …", "Connection: connected"), the notification ("Connected — noise control: …"), the tile's subtitle, "Heavy bass"; TalkBack's green focus
  frame and its caption (if the film shows TalkBack at all).
- **The export's last lines:** 07:11:14.271 "Android link observer started" — a second observer start in the same process 13 minutes after the first
  (06:58:07.994): an Activity re-creation? match it to the full logcat (`wm_*`) and the app logcat (05:11:24 UTC "CONNECTED -> PENDING").

---

## 3. Leads and context — verify each before relying on it

1. **The user** (🟡 until shown): the app logcat's `userType: full.secondary`; `am_switch_user: 10` at 06:47:59; the user-10 process PIDs; 0 Play markers vs the
   positive control; the shell log's `--user 10` list without `com.google.android.gms`. Confirm on film (the launcher, the user icon in Quick Settings).
2. **The build on film:** the film starts on **1.3.0** (which build? `lastUpdateTime 2026-10-15 09:11:11` — the Info tab's hash before the update; is it
   `ff27ee1`, the tested 1.3.0, or something else?) and updates to **1.3.1 build `7aa524b`** at 06:58:05 (the `am_kill`, the `dumpsys`, the export's start; the
   Info tab afterwards: "App: 1.3.1, build 7aa524b", no "-dirty"). Which frames, claims and holds belong to 1.3.0 (PID 25520) and which to 1.3.1 (PID 28176);
   **only 1.3.1's count for the verdict.**
3. **The clock offset:** the film's status bar (the minute changes at 10 fps) and a shared event with an export line or a frame (the update's restart at
   06:58:07; a tile tap at 07:01:51.969; the Bluetooth off at 06:56:37 if on film); settle the film's start and end before any correlation.
4. **No audio:** the maintainer did not speak and the recording has no audio stream — the timing of holds comes from the stream packets and the screen only.
   Playback: AVDTP `START`/`SUSPEND` and AVRCP `PlaybackStatusChanged` on the wire; the media notification on film.
5. **The press-and-hold evidence:** a hold is a stream packet `4:{13:n}` with no app `Set` in the 2 s before it (the `ANC_SET_WINDOW_MS` rule) and no
   `Notify`-answered `08 11` of the app; the mode on film within a second; the (i) line "Changed by the Buds at …" when opened; the mode list in force (the
   Connect's `ReadSetting 12` answer). Count per process (1.3.0 / 1.3.1): holds made vs shown, and each miss with its cause.
6. **The own taps** (11): `08 11` → `Notify` → `08 12` → ACK → the Buds' `Notify` → the stream packet(s); the delay; the cause classed (`set_by_app`); the
   tile's six (BM-5 asked for ten "within ≈ 2 s of Ready" — were they? the session at 07:00:47 vs the first tap at 07:01:51); any tap that ended without a
   `Set` or without an ACK and its message on film (A93-APP-03).
7. **The claims on DLCI 0x04/0x05:** every phone-sent `SABM`/`UA`/`DISC`/`DM` with its sender and owner; the claim lifetimes against
   `MESSAGE_STREAM_LINGER_MS` (1.5 s after the answer); the Connect snapshot once per session; no claim that is not a tap/tile/Refresh/pull/Connect snapshot.
8. **Unexpected on the wire (the maintainer's point):** anything on DLCI 0x08/0x0a (0 data frames counted on DLCI 0x08 in both logs — verify with a positive
   control, e.g. `CAP-029`'s 1592), any request not in `CAP-076` §2 or the skeleton's table, a second `GetHardwareInfo` in a session, a `WriteSetting` of any
   field (none expected — the user changed no setting? the film), any `WriteSetting 4:{13:n}` (never), an `UnidentifiedFrame` in the export, LE traffic to
   the Buds (GATT, the Fast Pair advertisement), HFP AT commands, AVRCP pass-through from the Buds, the reasons of every disconnect (`0x13`, `0x16`), anything
   from the Buds the decoder could not classify (`scripts/pwrpc_decode.py`'s unknown lines; the CRC check).
9. **The re-open and the reads:** the AFTER_LOSS re-open at 07:00:44.877 (the subscription and the read again — M1), the read values (0 = no mode settable,
   1–4), the next 1–4 packet making the mode allowed again; a value 0 never shown as a mode.
10. **Nothing held, nothing re-claimed:** 0 "re-claim"/"hold" lines in the export (positive control: `CAP-075`'s export 2); no app `SABM` on DLCI 0x04 that
    is not a user action or a Connect snapshot.
11. **Test-IDs** (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`; `AGENTS.md` §13 step 7): `PAIR-003`, `FW-003`, `ANC-001`…`ANC-004`, `TOUCH-007` (the skeleton's list);
    `PAIR-001`/`PAIR-002` for the new association if it applies; `CASE-004`/`CASE-005`/`INEAR-002`/`INEAR-003` if buds went in/out on film. Re-derive from what
    the run actually did; "expected but not observed" for each that was not — and the step mapping BM-1 … BM-9 (done / not done / done differently).
12. **Privacy:** the screen may show the Wi-Fi name in Quick Settings and the serial numbers on the Info tab (allowed on film, never unredacted in text — first
    4 + last 2); the logcats print `XX:XX:XX:XX:6E:07`-style addresses; the full logcat matches the privacy pattern thousands of times — **commit an extract
    only** (the `awk` of `CAP-076`: the run's time window, the app's and the Bluetooth process's PIDs, every line naming the app, `com.android.bluetooth`,
    `CompanionDevice`/`cdm`, `PackageManager`/`installd` for the update), keep the full file local (`~/opencontrol-capture-originals/CAP-077/`), name the
    extract CAP-077-logcat-extract.txt.
13. **External validation** where a claim rests on a specification or platform behaviour (fetch, quote with the date): `CompanionDeviceManager` (`associate`,
    `disassociate`, `getMyAssociations`) on developer.android.com — for the "removed 0 association(s)" reading; Compose `Modifier.toggleable` /
    `Role.Switch` semantics for TalkBack; the Bluetooth Core Specification for the `0x13`/`0x16` reasons and RFCOMM's `DISC`; the Fast Pair Message Stream page;
    Pigweed's pw_rpc packet types (`SERVER_STREAM` = 7) — re-fetch only if a new claim needs them.

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename, mode change, delete or `git add`)

1. Create the RESULT (§0). Record `git log -1`, `git status --short`, `git fetch`, `git log --oneline HEAD..origin/main`, the open PR #64. Work on
   **`release/1.3.1`** (`git switch release/1.3.1 && git pull --ff-only`); never on `main`; never rebase or amend the build commit `7aa524b`.
2. List the folder fresh (`ls -la`, `sha256sum`), diff it against §2's table and say every difference (incl. the missing System log).
3. **Checkpoint 0 (`AskUserQuestion`, one question per decision, options with pros and cons, "(Recommended)" on one, the exact commands in the preview):**
   the folder rename to the recording's first/last phone times (`CAP-077-2026-10-16_HH-mm-ss_HH-mm-ss-Group_BM`) and whether the recording keeps its name;
   `chmod 644` on the three files with mode 740; a `sha256sum` file; the privacy treatment of the recording (as is, or blur ranges) and of the full logcat
   (an extract committed, the full file local); **the System log** (does a CAP-077-System-log-*.txt exist elsewhere — the maintainer adds it, or the analysis
   records it as not provided); whether the phone can be attached for the APK pull (`adb shell pm path --user 10 …`, `adb pull`, `sha256sum` against
   `~/opencontrol-1.3.1-tested`); whether the originals go to `~/opencontrol-capture-originals/CAP-077/`.
4. Update the `CAP-077` row of `id_registry.csv` (status `planned` now; `analyzed` in Phase D) and prepare the Group BM status line of
   `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (written in Phase D).

### Phase A — the screen recording, in full

5. Decode the recording at 1 frame per second into the scratchpad in ranges (864 frames); compare each second with the previous one; view **every** second
   with a change on labelled contact sheets; narrow transitions at 10 fps where a claim depends on them (the mode buttons after a hold, the (i) dialogs, the
   tile and its subtitle, the notification shade, the "Use different Buds" dialog and Android's picker, the update screen, the Info tab before and after, a
   rotation, TalkBack's focus frame). No sampling: every second is either viewed or proved unchanged.
6. **Measure the clock offset** (status-bar minute changes at 10 fps; a shared event with an export line) with its uncertainty, and settle the film's start and
   end before any correlation. Confirm with `ffprobe` that there is no audio stream.
7. Write the timeline into the existing CAP-077-EVENT-NOTES.md: phone time · what happened · step BM-n / Test-ID · evidence — in `CAP-071`/`CAP-076`'s layout (the
   status line becomes "captured 2026-10-16 … and analyzed …", Log Metadata filled in, the capture-integrity pre-flight incl. the `.last`/current boundary at
   06:56:37/06:56:41, the review method and privacy, the Event Timeline incl. "before the recording" and "after the recording", the step mapping BM-1 … BM-9
   done / not done / done differently, Test-IDs per `AGENTS.md` §13 step 7 with "expected but not observed"; the planned procedure kept below as the appendix).

### Phase B — the HCI logs and the other logs, in full

8. Both HCI logs: Connection Complete events and the Buds' handle (by address; `bluetooth.addr` may match nothing — show the filter matching a known frame),
   the `.last`/current boundary, the RFCOMM inventory per DLCI and direction, **every pw_hdlc frame CRC-32-checked** (`scripts/pwrpc_decode.py`, with its
   positive control; a payload holding several HDLC frames split on `7e`), every Message Stream message decoded, **the full claim inventory of DLCI 0x04/0x05**
   with the sender of every `SABM`/`UA`/`DISC`/`DM` and the owner of every claim, the Play-marker count with its positive control, **every `4:{13:n}` stream
   packet with its time, value, channel and what preceded it** (a `Set`, a `Notify`, nothing), every `ReadSetting 4:13` REQUEST/RESPONSE pair per session and
   any stream packet between them, the seventeen Connect requests byte for byte against `CAP-076` §2 (order, channel, once per session), every write
   (expected: the Connect reads, the subscriptions, the Sets — nothing else), AVDTP/AVRCP state, LE traffic to the Buds, every disconnect reason; **and a
   deliberate search for the unexpected** (§3 item 8). Commands and exit statuses in FINDINGS (`PROJECT_RULES.md` rule 4a; `AGENTS.md` §13 step 8 for every
   negative).
9. The export and the app logcat: every line read (706 + 701) — every export line matched to the code's log string: the subscriptions and reads per session,
   every "from the settings stream (<cause>)" line against its packet and the cause rule (incl. the new "(unchanged)"), the "no mode settable" lines, the session
   loss and re-open with their delays against `LOSS_REOPEN_DELAY_MS`/`CHAIN_GUARD_MS`, the tile lines against their `Set`s, the "Pairing:" lines against
   `BudsCompanionPairing.kt`, the claim lifetimes against `MESSAGE_STREAM_LINGER_MS`, connect failures and retries; the app logcat's `events` lines (process
   start/stop, `wm_*`), crashes/ANRs/StrictMode (`grep -c` with a positive control).
10. The adb shell log (every line; P0/P1's meaning as in §2) and the full logcat: `grep`/`awk` by time window and PID — the user switch, the app's processes,
    **the update at 06:58:05** and **the 1.3.0 update of 2026-10-15 09:11:11** (`PackageManager`/`installd` lines), `CompanionDeviceManager` lines around
    07:00:05 – 07:00:24 (the disassociation of 0 and the new association), the Bluetooth process's lines around every disconnect and the session loss at
    07:00:43, any crash. Write the extract command and its line count; commit the extract only (Checkpoint 0). The System log: as decided at Checkpoint 0.

### Phase C — correlation, the findings, the comparison

11. Correlate every timeline row with its frames and log lines. Write CAP-077-FINDINGS.md in `CAP-071`/`CAP-076`'s structure: §0 build (1.3.0 → 1.3.1 on film;
    the shell log's `dumpsys`; the Info tab's hash = `7aa524b`), install, the user, logs, privacy; §1 ACLs and the claim inventory; §2 the Connect sequence
    byte for byte, the two requests once per MAESTRO session; §3 every write; **§4 the holds: every stream packet, the mode on screen within a second, the (i)
    line, the misses and their cause** — the maintainer's core claim verified or refuted; **§5 the 1.3.1 fixes, one subsection each — A93-APP-01 ("Use different
    Buds": the dialog, no connection by name, the new association, the "removed 0" reading), A93-APP-02 (the kept "changed since" — exercised or not),
    A93-APP-03 (the tile taps: every tap a `Set` and an ACK, or a message), A93-APP-04 (a stream packet during a read — exercised or not), A93-APP-07 (TalkBack —
    on film or not), A93-APP-08 (the labels, "(unchanged)", the dialog)** — each "verified on hardware", "not exercised" or "refuted", with the evidence; §6 the
    own taps (Set → ACK → Notify → stream packets, the delays, the cause classed); §7 the sessions, the loss and the re-open; §8 playback; **§9 unexpected
    findings on the wire and in the logs**; §10 `CAP-076` (1.3.0, part 2 — the same user) vs `CAP-077` (1.3.1) — a table per aspect; §11 the counts for the
    verdict (the app's `SABM`s vs the user's actions; holds shown vs made; stream packets vs export lines; the first read after every Connect/re-open); §12 the
    skeleton's expectations applied literally per step run; **§13 the verdict table (`RELEASING.md` C4)** — what the run verifies for 1.3.1, what it does not
    (the steps not done), heavy/minor/OK, and the analysing session's view — the decision is the maintainer's; §14 open questions and proposals.
    FACT / HYPOTHESIS / ASSUMPTION / OPEN QUESTION on every conclusion (`PROJECT_RULES.md` §1); every claim with a frame, a log line or a film time.
12. Validate externally (§3 item 13): fetch, quote with the date.

### Phase D — checkpoint, documentation, finish

13. **Checkpoint (`AskUserQuestion`):** **the verdict** (with the pros and cons: `RELEASING.md` C4, `PROJECT.md`'s Definition of done, `CHANGELOG.md` `[1.3.1]`'s
    Known issues — keep, reword or add; the fixes not exercised — release anyway, or a short re-run of the missing steps first); any `PROTOCOL.md` note or
    ADR-065 Update (a draft in the preview; `AGENTS.md` §6: nothing written without the maintainer's answer in this chat); whether a FEATURE prompt follows (a
    draft title in the preview); the privacy treatment; whether the folder's untracked files are committed now (Git LFS — `.gitattributes` covers `captures/**`
    `.log`, `.log.last`, `.mp4`, `.txt`) on `release/1.3.1` after the build commit. Record the answers verbatim in the RESULT.
14. With the answers: `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 (the `CAP-077` row: the real times, analyzed) and the Group BM status line; `id_registry.csv`;
    `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the Group column: BM only on the Test-IDs exercised — remove it where a Test-ID was not); `APP_TESTPLAN.md` §Z (the
    Result column per row, the Summary row, a run note); `TODO.md` §1/§2/§5; `ARCHITECTURE.md` §5a where the run verified a feature; `CHANGELOG.md` `[1.3.1]`
    only as approved; the PR #64 body's C1/C2/C3 lines (`gh pr edit`) as approved. `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and
    `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py` (exit 0).
15. The RESULT: plain-language summary first (what happened on the phone, which fixes the run verified and which it did not exercise, whether every hold showed
    and why not where it did not, what was unexpected, what the maintainer decides next), the phases, the files read in full / in part, "Deferred
    documentation" (each also in `TODO.md`), "Commits"; the `ai-sessions/INDEX.md` row (0095; the 0094 row's Status gets the back-fill "hardware run analysed
    by 0095", and its stale "no PR yet" becomes "PR #61, merged `9bc09dd`" as a dated note).
16. Show a short summary and **ask whether to commit and push**. Only after a yes: commits per concern (Conventional Commits: `docs(capture)`, `docs`), each
    with its *why* and the attribution line; Git LFS for the capture files; on `release/1.3.1` after `7aa524b`; `git fetch`; push without force; never merge;
    the release steps D1–D4 are the maintainer's.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Scope:** analysis and documentation only. No change under `android/` (improvements are proposals), no new skeleton, no release step, no `dist/` change, no
  `scripts/release.sh`, no tag, no merge, no rebase of the build commit.
- **Approvals only in this chat** (`AGENTS.md` §6): no 🟢 FACT promotion, no new ADR or ADR Update, no verdict recorded as the maintainer's without their
  answer here; ADR-060 stays a draft. "Every hold showed" and "everything works" are answered with the evidence of this run — never by the maintainer's
  impression; a fix the run did not exercise is "not exercised", not "verified".
- **Evidence:** every claim cites a frame (log and number), a log line or a film time; a negative needs its command, its exit status and a positive control
  (the Play marker's positive control is `CAP-076-btsnoop_hci1.log`'s 18; the "re-claim"/"hold" lines' positive control is `CAP-075`'s export 2); a hypothesis
  names its settling experiment. The maintainer's statements are statements until verified.
- **No sampling:** every second of the recording, every packet of both HCI logs on the Buds' handle, every line of the export, the app logcat and the shell
  log — reviewed or covered by a stated, positive-controlled filter; the full logcat by stated filters over the run's full time window.
- **Privacy:** no serial unredacted, no MAC, no phone number, no account name or e-mail, no Wi-Fi name in any committed text; the full logcat is committed as
  an extract only; the recording's treatment and the originals' place are Checkpoint 0's.
- **Files:** before moving, renaming or deleting anything, diff a fresh listing; never wildcard deletes; ask first. `android/domain/bin/` is never staged.
- **Subagents** may only read and report; every write is done and verified in the main session; never two `tshark`/`ffmpeg` jobs at once.

## 6. Deliverables

- CAP-077-EVENT-NOTES.md (the timeline added to the skeleton) and CAP-077-FINDINGS.md (the user and the builds settled, the two requests per session, every hold
  against the screen, each 1.3.1 fix verified / not exercised / refuted, the own taps, the sessions, the unexpected, the `CAP-076` comparison, the counts, the
  verdict table), with the folder and files as approved in Checkpoint 0 (incl. the logcat extract).
- The registry, index, test-plan, `TODO.md`, architecture, PR-body and status updates as approved; a FEATURE-prompt proposal if one follows.
- ai-sessions/0095_CAPTURE_RESULT_2026_10_16.md and the `ai-sessions/INDEX.md` row.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0095_CAPTURE_PROMPT_2026_10_16.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0095_CAPTURE_PROMPT_2026_10_16
