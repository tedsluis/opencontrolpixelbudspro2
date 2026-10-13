# 0088_CAPTURE_PROMPT_2026_10_12.md — Full analysis of CAP-075 (Group BK: 1.2.1 build `31ee183`, the "Noise-control channel" switch in the user without and the user with Google Play services — which position works best in each, and how the Play-user case can be made better)

**Number:** 0088
**Category:** CAPTURE
**Date:** 2026-10-12 (prompt rewritten 2026-10-13 — see §1 item 0; the first version analysed only the first set of files and was stopped by the maintainer)
**Title:** Fully analyse `CAP-075`: **two** Android screen recordings (each with an audio track), **four** HCI snoop logs, **two** app debug exports, **two** app
logcats, a system log and a full `logcat -b all`, recorded with the release-signed **1.2.1 build `31ee183`** (`ai-sessions/0087`, built 2026-10-12 17:17) on a
Pixel 9a with GrapheneOS (Android 17) — **set 1** in the secondary user **without** Google Play services (2026-10-12, 17:22–17:31) and **set 2** in the Owner
user **with** Google Play services (2026-10-13, 06:18–06:36). The task: establish from the films, the wire and the logs what happened in each set, what each
position of the switch did under each condition, which position works best in each user **on evidence**, and — for the Play user — what can be made better;
compare with `CAP-073` and `CAP-074`; record the findings; propose the next step.
- Record the real events in the existing skeleton **CAP-075-EVENT-NOTES.md** (it exists — written by `ai-sessions/0087`, the build lines added by PR #41; the
  timeline is added to it, the planned procedure stays as the appendix, as `CAP-073` and `CAP-071` did) and the analysis in a new **CAP-075-FINDINGS.md**.
- **No change to the app itself** in this session (improvements are **proposed**, with the evidence and the design, for a later FEATURE session), **no new
  capture skeleton**, **no release step**: the analysis, its documentation and the release verdict proposal for the maintainer (`RELEASING.md` C3/C4).

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this repository. Run the
phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Take the time to read the core files first, in full and in this order, to
understand the ground rules before analysing anything:
- `AGENTS.md` and `PROJECT_RULES.md`.
- `PROJECT.md` (the Definition of done; the status paragraph with `CAP-074` and 1.2.1).
- `ARCHITECTURE.md`, in particular §2.4 (the Settings tab's "Noise-control channel" card and the hint line), §3.1 (the ANC row), §3.2 (`HOLD_RECLAIM_DELAYS_MS`,
  `MESSAGE_STREAM_LINGER_MS`), §5a (the "Changed by the Buds" row — 1.2.1 "not hardware-verified"), **§6.0b** (ADR-032's per-channel handling, the ADR-061 hold
  paragraph, the `CAP-074` observation and the **ADR-062 paragraph**), §7, §9, §12, §13.
- `PROTOCOL.md`, in particular §2.3 (the three-channel table; "DLCI and server channel" — the Message Stream is DLCI 0x04 **or 0x05**), **§4.1** (ANC
  `Get`/`Set`/`Notify`, the Settable byte, the note of 2026-10-12: `Notify` goes to the one client holding DLCI 0x04), §4.3 Option B/F, §8 (the 2026-10-10/12 rows).
- `DECISIONS.md`: **every** ADR, ADR-001 … ADR-062, with every dated Update, and the draft ADR-060 (not a decision). In particular **ADR-032** (and its
  2026-10-12 Update), ADR-044, ADR-049, ADR-057, **ADR-061** (its Updates of 2026-10-10 and 2026-10-12) and **ADR-062** (the switch: HINT_ONLY the default,
  RECLAIM 3/6/12 s then stop; "not yet hardware-verified — the run `CAP-075`").
- `TODO.md`, §2 (the hold's untested branches — `CAP-075` takes both), §3 (the *Nearby devices* permission item), §4 (🔴 "Why Play services stops re-claiming
  DLCI 0x04" — `CAP-059`/`061`/`062` vs `CAP-074`), §5 (the 1.2.1 release steps, **M**).

These are the ground rules; do not analyse or change anything before they are read. Then, per task:

- **Procedure and registers:** `AI_SESSION_LOG_PROCEDURE.md` (in full, incl. §4b and §9), `ai-sessions/INDEX.md`, `id_registry.csv` (the `CAP-073`, `CAP-074`,
  `CAP-075` rows, ADR-061, ADR-062, every Test-ID named in §3 item 12), `RELEASING.md` (the Release checklist, "Repository rules", §11a, §13).
- **The build under test and its skeleton — read in full:** `ai-sessions/0087_FEATURE_PROMPT_2026_10_12.md` and `…/0087_FEATURE_RESULT_2026_10_12.md` (§A.1 the
  `CAP-074` numbers re-derived, §A.3 the design — the predicted outcome per position under contention, **§B the checkpoint answers**, §D the gate and the
  mutations, §E the skeleton, "After the session — the merge and the build": build commit `31ee183`, APK SHA-256
  `a60a3cc9b2a80e9dc5fecb8810c9cb8e7cbb20fa9eb7a2a0ce663b89e964963e`, certificate `a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb`);
  **`CAP-075-EVENT-NOTES.md`** (the skeleton: the two parts, P0–P9, the reference-frame table from `CAP-074`/`CAP-073`, BK-1 … BK-21, the analysis checklist);
  `CHANGELOG.md` `[1.2.1] - not yet released`; `APP_TESTPLAN.md` §X and the Summary.
- **The "before" — read in full:** `captures/CAP-074-2026-10-12_06-40-16_06-45-31-Group_BJ/CAP-074-FINDINGS.md` (**§1 the claim inventory of both clients and the
  Play-services marker `03 08 00 02 01 25`, §4 the holds, the cuts and every `Notify`, §7 vs `CAP-073`, §8 the root cause, §9 the options, §10 open questions**) and
  `CAP-074-EVENT-NOTES.md` (the method: the clock offset from the status bar, the audio's voiced segments, the claim-owner rule, the system-log extract and its
  command); `captures/CAP-073-…-Group_BI/CAP-073-FINDINGS.md` §0 (P0: the user without Play), §1 (0 Play-services claims, the positive control), §4 (the hold's
  main path: 31 s, every hold shown) and `CAP-073-EVENT-NOTES.md` (the layout: metadata, pre-flight, review method, timeline, step mapping, Test-IDs, checklist,
  the skeleton as appendix); `ai-sessions/0086_CAPTURE_RESULT_2026_10_12.md` (the method and the scratch scripts `ms_inventory.py`/`correlate.py`/`hdlc_crc.py`/
  `diffs.py` — re-create them, they are not in the repository); the maintainer's named examples
  `captures/CAP-071-2026-10-08_17-51-29_18-27-28-Group_BG/CAP-071-EVENT-NOTES.md` and `…/CAP-071-FINDINGS.md` (two films' worth of layout: metadata table, P0/P1
  outputs, pre-flight, review method and privacy, one timeline row per event, step mapping, Test-ID traceability, the extract command for a system log).
- **Every Kotlin source a finding touches, in full,** before you write about it — at least: `BudsRepositoryImpl.kt` (`reconcileHold`, `releaseOrHold`, `endHold`,
  the `transport.channelClosed` collector with **`onHeldClaimTaken`**, `startReclaimChain`/`cancelReclaim`, `HOLD_RECLAIM_DELAYS_MS`, the log strings — every
  export line is matched against them), `HoldReclaimMode.kt` (`ChannelTaken`), `HoldReclaimSettingsStore.kt`, `AncScreen.kt` (`ancChannelTakenLine`,
  `ANC_HOLD_HINT_TEXT`), `SettingsMenu.kt` (`HoldReclaimCard`, the labels), `RfcommBudsTransport.kt` (the reader's `read return: -1` path, `reportChannelClosed`,
  `closeChannel`'s "released" line, the connect retry after a fast failure), `SessionReopener.kt`, `MainActivity.kt` (`onAncTabShown`, `onHoldReclaimModeChanged`),
  `OpenControlNavHost.kt`.

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.**
- Create **ai-sessions/0088_CAPTURE_RESULT_2026_10_12.md** at the very start (a fresh file — the first run's partial RESULT was removed with this rewrite), with
  `**Status:** partial — resumed` and a **Progress** block.
- Update that block at the end of every phase **and** after every substantial step: every minute of each recording reviewed, every HCI log and channel
  decoded, every log file read, every checkpoint answer. Say what is done and what is next, which files are touched but not yet verified, and where the
  intermediate results live (the scratchpad: scripts, contact sheets, decoded tables, notes — re-create them if it is gone).
- A resumed session reads this prompt, then the RESULT's Progress block, re-reads the files the unfinished step touched, and continues from there. It never
  redoes a finished, recorded step and never assumes an unrecorded one was done. The prompt keeps its number and date; the RESULT grows in the same file.

**Memory and the machine.** The laptop has 15 GB and `/tmp` is a 7.7 GB tmpfs. Run `tshark`/`ffmpeg` one job at a time; extract contact sheets and crops in
ranges, never a whole film into memory; the two full system-log files are 21 MB and 12 MB — `grep`/`awk` them, do not read them whole into context. No Gradle
build is needed. Never `git worktree add` a commit (it checks out `captures/`, gigabytes). Scratch files go into a fresh `mktemp -d` under `/tmp/claude-1000/`;
write their names in documents without backticks (the docs lint flags backticked names that do not exist).

**Nothing is taken on trust, including this prompt, the maintainer's statements, the 0087 RESULT and the memory notes.** Every claim and every number measured
in §2 and §3 is re-derived from the recordings, the HCI logs, the other logs, the code and official documentation.

---

## 1. What the maintainer asked for (chat, 2026-10-12 and 2026-10-13, translated from Dutch)

0. **The correction (2026-10-13):** "I made a mistake that I want to put right. The earlier prompt for `CAP-075` was wrong and incomplete: I had stated in it
   that the capture's files were from a user with Google Play services, but that was a capture **without** Google Play services. I stopped that prompt and added
   to the `CAP-075` folder the files of a capture **with** Google Play services. Adapt the prompt so that I can run it again; the RESULT must be made completely
   anew." — *The first run's partial findings on set 1 are in §3 as leads (re-derive them); its RESULT stub was removed.*
1. "Make a new prompt for Claude Code … a comprehensive analysis of `CAP-075` (Group BK) of the OpenControl app, record the findings. Take the time to read
   the core files first. Run everything in phases, sequentially; when you hit a token limit, you must be able to continue automatically later."
2. **The goal, in the maintainer's words:** "In `CAP-073` I tested 1.2.0 build `5b4d5db` in a user without Google Play services on GrapheneOS; there, changing
   the ANC mode with a long press always works — the app shows every change, also in later tests. The same build does **not** work well in a user **with**
   Google Play services: after a Disconnect and a Connect, of several long presses only the first changes the ANC mode in the app; the next one I hear in my
   ears but do not see in the app. `CAP-074` shows that clearly. The goal was to find out why. In **1.2.1 build 31ee183** a fix was built with two options: the
   'Noise-control channel' switch (on the Settings tab) chooses between 'Show a hint, do not take it back' and 'Take it back while the noise-control tab is
   open (may fight that app)'. **`CAP-075` must establish what works best. That is why `CAP-075` consists of a capture in a user without Google Play services
   and a capture in a user with Google Play services, so that we can establish what works well and what goes wrong.**"
3. **The maintainer's statements about the runs (verify each; label each as the maintainer's statement until verified):**
   - "The app is the newest version 1.2.1 build `31ee183`." — *the Info tab on film and the logcats' `package: …:10201` decide; §3 item 2: set 1's film
     starts on **1.2.0 build `5b4d5db`** and shows an update to 1.2.1 on film.*
   - "The capture was made on a Pixel 9a with GrapheneOS Android 17."
   - "During the video I spoke only to say when I had done a **left or right** long press."
   - "**In the user without Google Play services**, switching ANC modes with a long press works well: the app's ANC-mode display always followed. My
     observation is that the switch's 'Show a hint, do not take it back' works best, and it also seems to work better than 'Take it back while the noise-control
     tab is open (may fight that app)'. **Find out what works best in a user without Google Play services.**" — *in a user without Play both positions are the same
     by design (ADR-062 items 2/3: the re-claim only follows a foreign close); if the evidence shows no foreign close in set 1, say so plainly and explain why the
     two positions could not differ there.*
   - "**In the user with Google Play services**, switching ANC modes with a long press did not work well by far: the app's ANC-mode display very often did not
     follow. My observation is that 'Take it back while the noise-control tab is open (may fight that app)' worked best, and that 'Show a hint, do not take it
     back' only worked the first time. **Find out what works best in a user with Google Play services and examine how it can be made better.**"
4. **The analysis steps the maintainer named (§4 follows them):** first both recordings → `CAP-075-EVENT-NOTES.md` with every event and action and its time;
   all `CAP-075-btsnoop_hci*` logs with `tshark`; both app logcats; both debug exports; the system log and the full logcat; then correlate the timeline with
   every log; a comprehensive CAP-075-FINDINGS.md (`CAP-071`'s files as the examples); the differences between the capture with and without Google Play services
   in combination with the two switch positions; **compare `CAP-073`, `CAP-074` and both captures of `CAP-075`** and establish which position works best for the
   user with and for the user without Google Play services.
5. Strict rules (the maintainer's): no assumptions — verify everything; validate claims externally against official documentation where needed (Android
   Developer Docs, the Bluetooth Core Specification, the Fast Pair specification); **no sampling** — a 100 % complete review of the relevant files; evidence
   traceability (every claim links to a frame, a log line or a film time) and structural integrity.
6. Standing practice: no commit or push without the maintainer's "yes" in chat; no release step; `main` takes no direct push (`RELEASING.md`).

---

## 2. The evidence (measured 2026-10-12/13 by the session that wrote this prompt — re-derive every number)

The folder `captures/CAP-075-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BK/` (the skeleton's placeholder name; the skeleton file is committed, the **thirteen** other
files are **untracked**) holds, per `sha256sum` and `ls -la` of 2026-10-13 06:42:

**Set 1 — the user without Google Play (2026-10-12; the first run of this prompt analysed these):**

| File | Measured | Notes |
|---|---|---|
| CAP-075-EVENT-NOTES.md | the committed skeleton (27,190 B, SHA-256 `7f42e6c5…acbde`) | **exists**: add the timeline to it, keep the planned procedure as the appendix |
| CAP-075-screen-20261012-172842-1791818551858.mp4 | 76.5 MB, 370.8 s, H.264 1080 × 2424 at 60 fps nominal (4,203 video frames); **an AAC audio track**; `creation_time` 2026-10-12T15:28:54Z; mode 750; SHA-256 `d351739d…42b1b3` | the status bar reads **17:22** at the first frame and flips to **17:23** between film seconds 28 and 29, **17:28** at the last frame ⇒ the name stamp 17:28:42 is the **end**; the film covers ≈ 17:22:31 – 17:28:42 (measure the offset to ± 0.1 s at 10 fps) |
| CAP-075-btsnoop_hci1.log | 81,868 B; 1,832 packets, 2026-10-12 17:23:08.048 – 17:30:33.747; SHA-256 `96e70475…6411` | renamed by the maintainer from the earlier name CAP-075-btsnoop_hci.log; one Buds ACL: page frame 145, Connection Complete 151 (17:23:09.631, handle `0x000b`, `04:00:6e:…:07`) → Disconnection Complete 1814 (17:28:47.531, reason `0x13`); the 18 packets after it are vendor/LE housekeeping, no Buds frame |
| CAP-075-btsnoop_hci1.log.last | 55,100 B; 1,110 packets, 17:21:54.524 – 17:23:04.307; SHA-256 `493357a2…7c550` | not a prefix of hci1.log; a Buds ACL from frame 501 (17:22:45.301) to 1097 (17:23:04.162, reason **`0x16`**, together with an LE handle `0x0040` at 1096 — a Bluetooth **off** at 17:23:04; the current log starts 17:23:08 with Bluetooth **on**) |
| CAP-075-opencontrol-debug-20261012-173158.txt | 228 lines, 17:25:20.122 – 17:31:53.825 (local time); mode 750; SHA-256 `c66a4ced…369b92` | one export, one process (PID 14361); starts with "Permissions (start)" and a LINK_BACK re-open at 17:25:20 |
| CAP-075-OpenControl-for-Pixel-Buds-Pro-2-log-97b3b88fc408.txt | 272 lines; header `osVersion: google/tegu/tegu:17/CP3A.261005.005/2026100601:user/release-keys`, **`userType: full.secondary`**, `package: …:10201, targetSdk 34`; UTC; PIDs **11917** (15:22:00–15:25:17 UTC) and **14361** (from 15:25:20 UTC); 124 `OpenControlBuds` lines; mode 750; SHA-256 `d02bcd81…14a37` | the app logcat (App info → View logs); `full.secondary` = the secondary user (`CAP-073`'s header had the same line for user 10; `CAP-074`'s Owner header had no `userType:` line) |

**Set 2 — the user with Google Play (2026-10-13):**

| File | Measured | Notes |
|---|---|---|
| CAP-075-screen-20261013-063431-1791865743758.mp4 | 65.4 MB, 327.9 s, 1080 × 2424, an audio track; `creation_time` 2026-10-13T04:34:36Z; mode 750; SHA-256 `39e3433c…e21ea6` | if the name stamp is the end (as set 1): ≈ 06:29:03 – 06:34:31 — **derive it from the status bar**; the export's last hold is 06:35:30 and the export was saved 06:35:33 (the logcat's "Debug log exported (513 lines)" 04:35:38 UTC), so the film cannot start at 06:34:31 |
| CAP-075-btsnoop_hci2.log | 476,769 B; 7,291 packets, 2026-10-13 06:29:18.537 – 06:36:53.161; SHA-256 `6b55526f…1250f1` | one Buds ACL (page 163, Connection Complete 469 at 06:29:21.136, `0x000b`), open at the log's end; DLCIs on `0x000b`: 0x00, 0x02, 0x04 (945 frames), **0x08 (59)**, 0x09, 0x0a, 0x0c |
| CAP-075-btsnoop_hci2.log.last | 1,395,929 B; 21,824 packets, **2026-10-12 18:13:13.002 – 2026-10-13 06:29:14.324** (one rotation spanning the night); SHA-256 `e9241db8…9b3c` | Buds ACLs: 18:13:17 → 18:14:11 (`0x13`), 18:14:11 → 18:14:12 (`0x13`), and **06:19:00.784 → 06:29:13.490 (`0x16`, with LE handle `0x0041` — a Bluetooth off at 06:29:13**; hci2.log starts 06:29:18 with Bluetooth on); 905 Buds frames on 10-12 18:xx (before the run — another app session? the Owner's 1.2.1 install test?) and 592 on 10-13 06:xx |
| CAP-075-opencontrol-debug-20261013-063533.txt | 512 lines, 06:18:58.764 – 06:35:31.729 (local); mode 750; SHA-256 `23ea855b…6f990` | one process (PID 17838, started 06:18:58 — `am_proc_start: [User=0,PID=17838,UID=10354,…,next-top-activity]` in the system log); **16 holds, 15 ended "the channel was closed, not by this app"**, 1 "the tab was left"; 17 "connect failed after N ms (attempt 1/3)"; 16 "on-demand channel 0x04 closed … session channels untouched"; **three complete re-claim chains 1/3 → 2/3 → 3/3 → "stopped after 3 re-claims"** (06:30:22–06:30:58, 06:32:48–06:33:25, 06:33:46–06:34:22) and one "re-claim cancelled" (06:19:04); **two "taken by another client: the hint is shown, no re-claim (hint only)"** (06:31:33, 06:32:02); 3 "Session ended by the user's Disconnect tap"; 5 Connect sequences (channel 21 once at 06:19, channel **19** four times); a session loss at 06:28:49 (Bluetooth off) and the LINK_BACK re-open at 06:29:27 |
| CAP-075-OpenControl-for-Pixel-Buds-Pro-2-log-b6d40f91b2ca.txt | 315 lines; the same header **without a `userType:` line** (the Owner, as `CAP-074`); UTC; one PID 17838 (04:18:58–04:35:52 UTC); mode 750; SHA-256 `1cdf4c26…180ed` | the app logcat of set 2 |
| CAP-075-System-log-571517c9248e.txt | 11.9 MB, 83,255 lines; header `type: logcat … buffers: main,system,crash,events,kernel`; spans 10-10 03:35 – **10-13 04:36:05 (UTC)**; 356 lines name the app, 758 name `com.google.android.gms`; **14 lines match the account/Wi-Fi privacy patterns**; mode 750; SHA-256 `466edda0…869e73` | Android's log viewer, "System log" (all apps; the app's uid is `u0a354`/10354 — note: `CAP-074`'s was 10353 in a different install) |
| CAP-075-logcat-all.txt | 21.2 MB, 135,471 lines; `adb logcat -b all -v threadtime` (no header); spans 10-10 05:35 – **10-13 06:34:54 (local)**; 265 lines name the app; 4,215 match `nearby`; **284 lines match the privacy patterns**; mode 644; SHA-256 `943c1e48…938eea` | the same phone log in local time and a longer buffer; the two system files overlap — **say which one you use for each claim**; neither is committed in full (privacy, as `CAP-071`/`CAP-074`): an extract |

Not present in either set: a shell log (P0/P1: `am get-current-user`, `pm list users`, `pm list packages --user <id> | grep gms`, the `dumpsys` before/after, the
APK pull and its `sha256sum`), a camera film, a `sha256sum` file. Record each as "not made" and what it leaves unverified; propose the APK pull
(`adb shell pm path --user <id>`, `adb pull`, `sha256sum` against `~/opencontrol-1.2.1-tested` — the kept copy exists) if the maintainer attaches the phone
(Checkpoint 0).

**First look, to be re-derived (leads, not findings):**
- **Set 1 (no Play): 0 Play-services markers** in both logs (`tshark -r <log> -Y 'btrfcomm && frame contains 03:08:00:02:01:25' | wc -l` → 0 and 0, exit 0;
  positive control: `CAP-074-btsnoop_hci.log` → 22, `CAP-074-btsnoop_hci.log.last` → 7). **10 Message Stream claims in hci1.log, all the app's** (`08 11 00 00` the
  first phone message of every `SABM`), every one closed by the **phone's `DISC` after an app "released" line** (SABM/DISC frames 460/546, 615/640, 782/845, 868/954,
  1111/1159, 1192/1268, 1289/1389, 1442/1551, 1572/1670, 1724/1802); 1 claim in hci1.log.last (617/841). **0 `Set`** (`08 12`) in both logs; 51 `Notify` frames
  in hci1.log of which **41 unprovoked** (no `08 11` in the 2 s before), on five holds: 4 (claim 1192, 17:26:11–17:26:18), 9 (1289, 17:26:26–17:26:55),
  12 (1442, 17:27:09–17:27:37), 12 (1572, 17:27:43–17:28:14), 4 (1724, 17:28:24–17:28:34). 109 pw_hdlc frames, CRC-32 OK on all (77 + 32); 26 + 13 `ReadSetting`,
  every `RESPONSE` OK; 3 + 1 `SubscribeRuntimeInfo`. The export's six holds (lines 60, 98, 114, 145, 178, 207) ended "the noise-control tab was left" ×5 and "the
  app is not visible" ×1 (17:28:43.653); **no "not by this app", no re-claim, no hint line** (`grep -c` → 0 each). Every claim lasted from its `SABM` to a `DISC`
  1.5 s after the hold's end (`MESSAGE_STREAM_LINGER_MS`).
- **Set 2 (Play): 18 + 6 Play-services markers**; **38 `SABM`s on DLCI 0x04 in hci2.log, all phone → Buds, all followed by the app's `08 11`**, 37 `DISC`s all
  phone → Buds, 0 `DM`; 12/11 (+1 Buds `DISC`) in hci2.log.last; 60 + 18 `Notify`; **0 `Set`** in both. The app's channel was closed 16 times "not by this app" —
  by the **phone's own stack**, not by a Buds `DISC`: establish from the wire who sent each `DISC` and what Play did in the same second (its `SABM` on which DLCI —
  0x04? 0x05? the 59 frames on **DLCI 0x08**?). This is `CAP-074` §8's mechanism (one RFCOMM connection per server channel; a second connect kills the incumbent)
  — verify it here frame by frame, do not assume it.
- **Set 2 timing per hold (export):** hold start → "closed, not by this app" after 0.4–4.6 s (06:30:14.98 → 06:30:19.32; 06:31:32.67 → 06:31:33.10; 06:32:01.07 →
  06:32:02.28 …); re-claim 1/3 after 3,000 ms, 2/3 after 6,000 ms, 3/3 after 12,000 ms, each re-claim's attempt 1 failing after 261–1,053 ms and attempt 2
  succeeding ≈ 0.9 s later; the Buds' first `Notify` after each re-claim. Count per chain how many of the maintainer's press-and-holds fell inside a held claim
  (shown) and how many outside (not shown) — the maintainer's "very often did not follow".
- **The switch position in force per period** (set 2): the export's lines name it — re-claim chains at 06:30:22–06:30:58, 06:32:48–06:33:25, 06:33:46–06:34:22 =
  RECLAIM; "hint is shown, no re-claim" at 06:31:33 and 06:32:02 = HINT_ONLY; "re-claim cancelled" at 06:19:04 (HINT_ONLY or the tab left? — the code decides);
  the three Disconnect taps (06:31:26, 06:31:51, 06:32:35) separate the conditions. Confirm each switch on film (the Settings card's selected row).
- **`CAP-075-logcat-all.txt` contains no `am_switch_user`**, one `am_proc_start` for the app (user 0, 06:18:58.248), no `installation completed`; the System-log
  file has the same `am_proc_start` with `User=0`. **Set 2 is the Owner user** by the system log — verify and say it with the line numbers.

State of the repository at the time of writing: `main` at `0409a55` (PRs #39–#42 merged: the feature, the bot PRs, the build identity, this prompt's first
version). A branch `capture/cap-075` exists locally with no commits (created by the first run; reuse it or recreate it from `main`). `android/domain/bin/`
untracked (build output — never stage it). The 1.2.1 APK: `dist/1.2.1` (gitignored, `.sha256` = `a60a3cc9…963e`) and `~/opencontrol-1.2.1-tested` (B3 done).

---

## 3. Leads and context — verify each before relying on it

1. **Which user, which set** (🟡 until shown): set 1 = the secondary user without Play (`userType: full.secondary`, 0 Play markers, no attempt-1 failure, every
   hold ended by the tab or the app); set 2 = the Owner with Play (`am_proc_start … User=0`, 18 + 6 markers, 17 attempt-1 failures, 15 foreign closes). Confirm
   on film (the launcher, the wallpaper, the user icon in Quick Settings, the Bluetooth dialog's other devices) and from the maintainer at Checkpoint 0 (the P0
   commands with their exit statuses — not made). The skeleton's **part 1 = set 2** (BK-1 … BK-14, Play) and **part 2 = set 1** (BK-15 … BK-18): map each step as
   done / not done / done differently.
2. **The build on film — set 1 starts on 1.2.0:** the first run's contact sheets show the Info tab "App: 1.2.0, build 5b4d5db (2026-10-10)" from the first frame to
   ≈ film second 151 (≈ 17:25:02) and the Settings tab **without** the "Noise-control channel" card; at ≈ film seconds 167–169 (≈ 17:25:18–17:25:20) Android's
   "Updaten…" screen; from ≈ second 170 the app is back (PID 14361, `wm_on_create_called` 15:25:20.128 UTC; the old PID 11917's `wm_on_top_resumed_lost_called`
   15:25:17.843 UTC; the phone's `DISC` on DLCI 0x02 at 17:25:17.885 = the dying process's socket), and the Info tab reads **"App: 1.2.1, build 31ee183
   (2026-10-12)"** from ≈ second 188 (≈ 17:25:39), the Settings card present (≈ second 182). So **set 1 is an update 1.2.0 → 1.2.1 on film (P1), with 1.2.0 used
   for ≈ 2.5 min before it** (a Connect at 17:22:45 in hci1.log.last, a Bluetooth off/on at 17:23:04/08, a LINK_BACK session at 17:23:09 with two claims 17:23:10 and
   17:23:23 — 1.2.0's, and the earlier process). Re-derive the frames; say which claims and holds belong to 1.2.0 and which to 1.2.1; only 1.2.1's count for the
   verdict. Set 2's film: the Info tab reading and the card — 1.2.1 throughout?
3. **The clock offsets:** set 1: phone ≈ film + 17:22:31 (the 17:23 flip between film seconds 28 and 29; the 17:28 at the end) — pin it at 10 fps and
   cross-check with an export line visible on film (the "Updaten…" restart at 17:25:20, a hold's start when the ANC tab opens); set 2: derive the same way. Both
   file-name stamps appear to be the **end** (set 1: 17:28:42 ≈ 17:22:31 + 371 s; set 2: `creation_time` 06:34:36 = the end + ≈ 5 s as `CAP-074`).
4. **The audio tracks:** set 1 is silent (mean −82 dBFS) but for the recorder's start chime (film 0.3–2.0 s) and five short bursts at film 220.3–221.1, 234.6–235.0,
   239.1–239.5, 251.1–251.5 and 276.8–277.9 s (−31 … −39 dBFS peaks) ≈ phone 17:26:11, 17:26:25, 17:26:30, 17:26:42, 17:27:08 — each within a second of an unprovoked
   `Notify` (17:26:11.35 `08`, 17:26:26.38 `20`, 17:26:30.60 `40`, 17:26:42.11 `20`, 17:27:09.20 `20`): the maintainer's cue words. Set 2: `silencedetect` finds 33
   non-silent segments — tabulate them all and align each with a `Notify`. You cannot listen: label the words as the maintainer's statement, the timing as measured.
5. **The press-and-hold cycle (set 1, 1.2.1):** the mode list is `{1:1 2:1 3:1 4:1}` (all four; `ReadSetting 12`) and the sequences read `08 → 20 → 40 → 80 → 08`
   (Active → Off → Adaptive → Transparent → Active) but also runs of `80 ↔ 08` (17:27:15–17:27:31: `80 08 80 08 80 08 80`). Do not invent a rule: record each
   `Notify` with its time and the preceding mode; mark the `80 ↔ 08` alternation as 🔴 open (a right-bud hold vs a left-bud hold? the maintainer said "left or
   right"; the cue words on the audio may separate them) with a settling experiment.
6. **Set 2 — the Play user's mechanism, frame by frame:** for every "closed, not by this app": the `DISC` on the wire (sender, DLCI), what Play's client did in the
   same second (its `SABM`, the DLCI it used, its first message, the `03 08 00 02 01 25` marker), the Buds' answers, whether the app's socket got `read return: -1`
   before or after Play's `UA`. Compare with `CAP-074` §1/§8 (Play's recovery 5.2–5.7 s after losing the channel, its retry 2.2–2.6 s) — here the cut came 0.4–4.6 s
   after the hold's start: measure Play's reaction time to **each** of the app's claims (claim → Play's `SABM`) and whether it depends on anything (the ANC tab
   just opened? a `Notify` just delivered? a Disconnect/Connect before?). The 59 frames on **DLCI 0x08** and the 4 on 0x0a in hci2.log: whose, and what?
7. **Set 2 — what each position achieved (the maintainer's question):** per hold, the holds shown on screen (film) vs the `Notify` frames that reached the app
   vs those that reached Play (on Play's claim) vs those that reached nobody; the first-hold-only pattern under HINT_ONLY (ADR-062's prediction ≈ 1 of N); under
   RECLAIM the three chains: how many seconds of each 30-s hold series the app actually held the channel (sum of SABM→DISC lifetimes), which holds fell in them,
   and what the fight looked like on the wire (Play's claims between the app's). Then **"how can it be made better"** — proposals only, each with the evidence it
   rests on and the design it would need (e.g. a longer or endless chain while the tab is shown; a shorter first delay; reacting to Play's `DISC` immediately; a
   different trigger; the Buds' own behaviour that cannot be changed), and what each would cost (the fight with Play; `CAP-074` §9's options; `TODO.md` §4's open
   "why Play stops re-claiming" — did Play stop in set 2 at any point?). No code in this session.
8. **Set 1 — why both positions are the same there:** no foreign close ⇒ `onHeldClaimTaken` never ran ⇒ HINT_ONLY and RECLAIM take the same path (ADR-061's hold,
   1.2.0's code). State it with the code lines and the export's absence of the ADR-062 strings; the maintainer's "hint only seems better" cannot rest on a
   difference that did not exist — say what the maintainer may have seen instead (the hold's end by the tab, a hold series spanning a release, 1.2.0's claims
   before the update).
9. **The session ends and re-opens:** set 1: the `0x13` at 17:28:47.531 (the Buds — both into the case? the film), the export's loss-cause lines, no re-open (the
   app not visible; "on return Android's link was down" at 17:31:53); the `.last`/current boundary (Bluetooth off/on 17:23:04/08 — the Bluetooth dialog on film
   at ≈ 17:23:03–17:23:10). Set 2: the loss at 06:28:49 (Bluetooth off at 06:29:13 on the wire? — the export says the loss came first: `channel 0x02 closed … last
   inbound frame 8,496 ms earlier`; reconcile the clocks), the LINK_BACK re-open at 06:29:27 (attempt 1 fails after 433 ms, attempt 2 OK — the same collision as
   `CAP-071` #7?), the three Disconnect/Connect pairs, channel 19 vs 21 (`PROTOCOL.md` §2.2a: which bud hosts the link; the film shows which buds were worn).
10. **"During playback":** AVDTP `START`/`SUSPEND` pairs (set 1: 17:23:11–17:28:42, many short ones — system sounds or music?), AVRCP play status, the audio
    track's energy. Say it with evidence.
11. **Privacy:** both recordings' audio carries the maintainer's voice; the screens show the Wi-Fi name in Quick Settings (set 1 at ≈ 17:22:54–17:23:12), other
    device names ("Niro"), the serial numbers on the Info tab (allowed on film, never unredacted in text — first 4 + last 2, ADR-058 item 5); the logcats print
    `XX:XX:XX:XX:6E:07`-style addresses; the two system-log files hold the account address and the Wi-Fi name (14 and 284 matching lines) — **commit an extract
    only** (the `awk` of `CAP-071` §0: the time window, the app's and the Bluetooth process's PIDs, every line naming the app, `com.android.bluetooth` or
    `NearbyDiscovery`/`gms`), keep the full files local (`~/opencontrol-capture-originals/CAP-075/`), name the extract CAP-075-logcat-extract.txt.
12. **Test-IDs** (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`; `AGENTS.md` §13 step 7): `PAIR-003`, `BATT-004`, `FW-003`, `ANC-001`…`ANC-004`, `TOUCH-007` (the skeleton's
    list); `AUDIO-002` if Volume EQ was touched; `CASE-004`/`CASE-005` if buds went in/out of the case. Re-derive from what the runs actually did; "expected but
    not observed" for each that was not — and the skeleton's step mapping BK-1 … BK-21 (done / not done / done differently), part 1 (set 2) and part 2 (set 1)
    separately.
13. **External validation** where a claim rests on a specification or platform behaviour (fetch, quote with the date): Android's user types (`UserManager`,
    "full.secondary"), `BluetoothSocket`'s one-connection-per-channel behaviour and `IOException: read failed, socket might closed or timeout, read ret: -1`
    (AOSP `BluetoothSocket.java`), the Fast Pair Message Stream and Hearable Controls pages (already quoted in `CAP-074-FINDINGS.md` §8 — re-fetch only if a new
    claim needs them), the Bluetooth Core Specification for the `0x13`/`0x16` reasons and RFCOMM's DLCI rules.

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename, mode change or `git add`)

1. Create the RESULT (§0). Record `git log -1`, `git status --short`, `git fetch`, `git log --oneline HEAD..origin/main`, open PRs. Use the branch
   `capture/cap-075` from an up-to-date `main` (`git switch main && git pull --ff-only`, then `git switch capture/cap-075 && git rebase main`, or recreate it).
   `main` takes no direct push.
2. List the folder fresh (`ls -la`, `sha256sum`), diff it against §2's tables and say every difference.
3. **Checkpoint 0 (`AskUserQuestion`, one question per decision, options with pros and cons, "(Recommended)" on one, the exact commands in the preview):**
   the users of set 1 and set 2 (the logs' reading; what the maintainer can still check on the phone: `adb shell am get-current-user`, `pm list users`, `pm list
   packages --user <id> | grep gms` with exit statuses); the folder rename to the recordings' first/last phone times (set 1's first frame to set 2's last frame,
   `CAP-075-2026-10-12_HH-mm-ss_2026-10-13_HH-mm-ss-Group_BK`? — the index convention is one day; propose the form and let the maintainer choose) and the
   recordings' renames (CAP-075-recording1.mp4 / CAP-075-recording2.mp4, or the skeleton's single name — the skeleton planned two parts); `chmod 644` on the
   eight files with mode 750; a `sha256sum` file; the privacy treatment of the recordings (blur ranges; the audio stays — the cue words) and of the two
   system-log files (an extract committed, the full files local); whether the phone can be attached for the APK pull and `am get-current-user`; whether the
   originals go to `~/opencontrol-capture-originals/CAP-075/`.
4. Update the `CAP-075` row of `id_registry.csv` (it exists with status `planned`; `analyzed` in Phase D) and prepare the Group BK section's status line of
   `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (written in Phase D).

### Phase A — both screen recordings, in full

5. Decode each recording at 1 frame per second into the scratchpad in ranges (set 1: 371 frames; set 2: 328); compare each second with the previous one; view
   **every** second with a change on labelled contact sheets; narrow transitions at 10 fps where a claim depends on them (the mode buttons, the hint line, the
   Settings card's selected row, the (i) dialogs, the connection state, the "Updaten…" screen). No sampling: every second is either viewed or proved unchanged.
6. The audio tracks: extract each (`ffmpeg -vn`), derive the voiced segments with `silencedetect`/RMS per 100 ms and tabulate every segment with its film time;
   align them with the maintainer's statement ("left or right long press") and with the wire's unprovoked `Notify` frames. Detect music playback the same way.
7. **Measure both clock offsets** (status-bar minute changes at 10 fps; a shared event with an export line) with their uncertainty, and settle start/end for both
   file stamps before any correlation.
8. Write the timeline into the existing CAP-075-EVENT-NOTES.md: phone time · what happened · step BK-n / Test-ID · evidence — in `CAP-071`/`CAP-073`'s layout (the
   status line becomes "captured … and analyzed …", Log Metadata filled in for both sets, the capture-integrity pre-flight incl. the hci1/hci2 boundaries and the
   night-spanning hci2.log.last, the review method and privacy, the Event Timeline — set 1 then set 2, each incl. "before the recording" and "after the
   recording" — the step mapping BK-1 … BK-21 done / not done / done differently, Test-IDs per `AGENTS.md` §13 step 7 with "expected but not observed", the
   analysis checklist ticked; the planned procedure kept below as the appendix).

### Phase B — the HCI logs and the other logs, in full

9. All four HCI logs: Connection Complete events and the Buds' handle (by address; `bluetooth.addr` may match nothing — show the filter matching a known frame),
   the boundaries between `.last` and current, the RFCOMM inventory per DLCI and direction, every pw_hdlc frame CRC-32-checked (`scripts/pwrpc_decode.py`, with its
   positive control), every Message Stream message decoded (`[Group][Code][Len][Value]`), **the full claim inventory of DLCI 0x04/0x05 (and whatever DLCI Play
   used) with the sender of every `SABM`/`UA`/`DISC`/`DM` and the owner of every claim** (`CAP-074` §1's rule), the Play marker counts with the positive control,
   every `Notify` with the claim it arrived on and the client that held it, AVDTP/AVRCP state, the Connect reads byte for byte against `CAP-074` §2 / `CAP-073`
   §2, every write (expected: none but the Connect reads). Commands and exit statuses in FINDINGS (`PROJECT_RULES.md` rule 4a; `AGENTS.md` §13 step 8 for every
   negative).
10. Both exports and both app logcats: every line read (228 + 512 + 272 + 315) — every export line matched to the code's log string (`BudsRepositoryImpl.kt`,
    `RfcommBudsTransport.kt`, `SessionReopener.kt`, `SessionDiagnostics.kt`, `OsConnectionObserver.kt`, `LinkEvaluation.kt`, `MainActivity.kt`): the hold
    starts/ends with their reasons, the releases and foreign closes, the claim lifetimes (SABM → DISC) against `MESSAGE_STREAM_LINGER_MS`, every ADR-062 line
    with its delay against `HOLD_RECLAIM_DELAYS_MS`, the connect failures and retries against the transport's retry delay, the session-loss lines; the logcats'
    `events` lines (process start/stop, `wm_*`), the `BluetoothSocket` close lines, crashes/ANRs/StrictMode (`grep -c` with a positive control).
11. The system log and the full logcat (set 2 only): `grep`/`awk` by time window and PID — the app's process start and user, Play services' `NearbyDiscovery`/
    Fast Pair lines around every cut (the 🔴 of `CAP-074` §10: what the stack logs when the second connect kills the incumbent — `BluetoothSocketManagerBinder`,
    `bt_stack`, `BluetoothSocket`), the Bluetooth off/on at 06:29, any `am_switch_user`, crashes. Write the extract command and its line count; commit the extract
    only (Checkpoint 0).

### Phase C — correlation, the findings, the comparison

12. Correlate every timeline row with its frames and log lines. Write CAP-075-FINDINGS.md in `CAP-071`/`CAP-074`'s structure: §0 build (the 1.2.0 → 1.2.1
    update on film), install, **users** (the evidence per set side by side), logs, privacy; §1 ACLs and the claim inventory per set (both clients in set 2);
    §2 the Connect reads byte for byte; §3 every write; **§4 the holds, per set and per switch position: each hold's start/end reason, each press-and-hold's
    `Notify` (shown / reached Play / reached nobody), the screen, the ADR-062 paths exercised** (set 1: none — why; set 2: the chains, the cancel, the hint);
    **§5 the Play user's mechanism frame by frame** (lead 6) and Play's reaction time per claim; §6 playback; §7 session ends and re-opens; **§8 `CAP-073` vs
    `CAP-074` vs `CAP-075` set 1 vs set 2** — a table per aspect (user, build, Play marker, claim owner over time, hold lifetime and end reason, holds shown /
    reached Play / reached nobody, the switch position, the first-hold-only pattern) **and the answer per user to "which works best"** with the evidence of
    each run and its conditions; **§9 "how can it be made better"** — proposals ranked, each with its evidence, design, cost and the test that would verify it;
    §10 the skeleton's "Refuted if" applied literally per step run; **§11 the verdict table (`RELEASING.md` C4)** — what the runs verify for 1.2.1 (part 2's
    regression; part 1's two positions), what they do not, heavy/minor/OK, and the analysing session's view (release 1.2.1 as is with the Known-issues line
    updated, or build the improvement first) — the decision is the maintainer's; §12 open questions. FACT / HYPOTHESIS / ASSUMPTION / OPEN QUESTION on every
    conclusion (`PROJECT_RULES.md` §1); every claim with a frame, a log line or a film time.
13. Validate externally (§3 item 13): fetch, quote with the date.

### Phase D — checkpoint, documentation, finish

14. **Checkpoint (`AskUserQuestion`):** the users' final reading (if not settled at Checkpoint 0); **the verdict** (with the pros and cons: `RELEASING.md` C4,
    `PROJECT.md`'s Definition of done is the user without Play, the `CHANGELOG.md` Known-issues line); **which improvement proposal, if any, goes to a FEATURE
    prompt** (a draft title in the preview); every proposed ADR-062 Update (the numbers of both sets, "hardware-verified: …" — a draft in the preview; `AGENTS.md`
    §6: nothing written without the maintainer's answer in this chat) and any `PROTOCOL.md` note (Play's DLCI, its reaction time); whether `CHANGELOG.md`'s
    Known-issues line changes; the privacy treatment; whether the folder's untracked files are committed now (Git LFS, `.gitattributes` covers `captures/**`
    `.log`, `.log.last`, `.mp4`, `.txt`) and on which branch. Record the answers verbatim in the RESULT.
15. With the answers: `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 (the `CAP-075` row: the real times, both users, analyzed) and the Group BK status line; `id_registry.csv`;
    `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the Group column: BK on the Test-IDs exercised); `APP_TESTPLAN.md` §X (the Result column per row, the Summary row, a run
    note like §W's); `TODO.md` §2/§3/§4/§5 (the hold's branches now tested; the improvement; the 1.2.1 release steps); `ARCHITECTURE.md` §5a (the "Changed by the
    Buds" row: what `CAP-075` verified) and §6.0b (one sentence, as approved); `DECISIONS.md` ADR-062 Update (as approved); `PROJECT.md`/`README.md` only as
    approved. `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and `python3 scripts/lint_docs.py` (exit 0).
16. The RESULT: plain-language summary first (what happened on the phone in each user, what each switch position did, which works best where and why, what
    could make the Play user better, what the maintainer decides next), the phases, the files read in full / in part, "Deferred documentation" (each also in
    `TODO.md`), "Commits"; the `ai-sessions/INDEX.md` row (0088; the 0087 row's Status gets the back-fill of this session's number if it refers to "the next
    session").
17. Show a short summary and **ask whether to commit and push**. Only after a yes: commits per concern (Conventional Commits: `docs(capture)`, `docs`), each with
    its *why* and the attribution line; Git LFS for the capture files; `git fetch` and rebase; push without force; open the pull request only when asked, and
    never merge it yourself.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Scope:** analysis and documentation only. No change under `android/` (improvements are proposals), no new skeleton, no release step, no `dist/` change, no
  `scripts/release.sh`, no tag.
- **Approvals only in this chat** (`AGENTS.md` §6): no 🟢 FACT promotion, no new ADR or ADR Update, no verdict recorded as the maintainer's without their answer
  here; ADR-060 stays a draft. "Which works best" is answered per user with the evidence of each run and its conditions — never by preference.
- **Evidence:** every claim cites a frame (log and number), a log line or a film time; a negative needs its command, its exit status and a positive control
  (the Play marker's positive control is `CAP-074`'s log: 22); a hypothesis names its settling experiment. The maintainer's statements are statements until
  verified — including which user each set was made in, "1.2.1 throughout" and what was said aloud.
- **No sampling:** every second of both recordings, every packet of all four HCI logs on the Buds' handle, every line of both exports and both app logcats —
  reviewed or covered by a stated, positive-controlled filter; the two system-log files by stated filters over the full time window of set 2.
- **Privacy:** no serial unredacted, no MAC, no phone number, no account name or e-mail, no Wi-Fi name in any committed text; the system-log files are committed
  as an extract only; the recordings' blur ranges and the originals' place are Checkpoint 0's.
- **Files:** before moving, renaming or deleting anything, diff a fresh listing; never wildcard deletes; ask first. `android/domain/bin/` is never staged.
- **Subagents** may only read and report; every write is done and verified in the main session.

## 6. Deliverables

- CAP-075-EVENT-NOTES.md (the timeline of both sets added to the skeleton) and CAP-075-FINDINGS.md (the users settled, the holds per set and per position, the
  Play user's mechanism frame by frame, the four-way comparison, the answer per user, the improvement proposals, the verdict table), with the folder and files
  as approved in Checkpoint 0 (incl. the system-log extract).
- The registry, index, test-plan, `TODO.md`, architecture, ADR-062 Update and status updates as approved; the FEATURE-prompt proposal for the chosen improvement,
  if any.
- ai-sessions/0088_CAPTURE_RESULT_2026_10_12.md (fresh) and the `ai-sessions/INDEX.md` row.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0088_CAPTURE_PROMPT_2026_10_12.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0088_CAPTURE_PROMPT_2026_10_12
