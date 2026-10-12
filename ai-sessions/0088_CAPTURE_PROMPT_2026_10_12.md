# 0088_CAPTURE_PROMPT_2026_10_12.md — Full analysis of CAP-075 (Group BK: 1.2.1, the "Noise-control channel" switch — which setting works best, and in which user the run actually took place)

**Number:** 0088
**Category:** CAPTURE
**Date:** 2026-10-12
**Title:** Fully analyse `CAP-075`: **one** Android screen recording (with an audio track), **two** HCI snoop logs, **one** app debug export and **one** app
logcat, recorded with the release-signed **1.2.1 build `31ee183`** (`ai-sessions/0087`, built 2026-10-12 17:17) on a Pixel 9a with GrapheneOS (Android 17).
The maintainer's statement: the run was made in the user **with** Google Play services, press-and-holds worked in both positions of the new switch, and
**"Show a hint, do not take it back" seemed to work well — better than "Take it back"** — which puzzles them, because "hint only" is 1.2.0's behaviour plus a
line of text. The task: establish from the film, the wire and the logs what happened, **in which user** (see §2 — the logs say the secondary user *without*
Play), whether either switch position was actually exercised against contention, and what — on evidence — can and cannot be said about "which works best";
compare with `CAP-073` and `CAP-074`; record the findings; propose the next step (a re-run of part 1 in the Play user, if that is what the evidence shows).
- Record the real events in the existing skeleton **CAP-075-EVENT-NOTES.md** (it exists — written by `ai-sessions/0087`; the timeline is added to it, the planned
  procedure stays as the appendix, as `CAP-073` did) and the analysis in a new **CAP-075-FINDINGS.md**.
- **No change to the app itself**, **no new capture skeleton** (a re-run of part 1 reuses this skeleton — say so), **no release step**: the analysis, its
  documentation and the release verdict proposal for the maintainer (`RELEASING.md` C3/C4).

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
- `TODO.md`, §2 (the hold's untested branches; `CAP-075` takes the Play-user branch), §3 (the *Nearby devices* permission item — `CAP-075`'s P0 asks for it on
  film), §4 (🔴 "Why Play services stops re-claiming DLCI 0x04" — `CAP-059`/`061`/`062` vs `CAP-074`), §5 (the 1.2.1 release steps, **M**).

These are the ground rules; do not analyse or change anything before they are read. Then, per task:

- **Procedure and registers:** `AI_SESSION_LOG_PROCEDURE.md` (in full, incl. §4b and §9), `ai-sessions/INDEX.md`, `id_registry.csv` (the `CAP-073`, `CAP-074`,
  `CAP-075` rows, ADR-061, ADR-062, every Test-ID named in §3 item 10), `RELEASING.md` (the Release checklist, "Repository rules", §11a, §13).
- **The build under test and its skeleton — read in full:** `ai-sessions/0087_FEATURE_PROMPT_2026_10_12.md` and `…/0087_FEATURE_RESULT_2026_10_12.md` (§A.1 the
  `CAP-074` numbers re-derived, §A.3 the design, **§B the checkpoint answers**, §D the gate and the mutations, §E the skeleton, "After the session — the merge and the
  build": build commit `31ee183`, APK SHA-256 `a60a3cc9b2a80e9dc5fecb8810c9cb8e7cbb20fa9eb7a2a0ce663b89e964963e`, certificate
  `a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb`); **`CAP-075-EVENT-NOTES.md`** (the skeleton: the two parts, P0–P9, the reference-frame
  table from `CAP-074`/`CAP-073`, BK-1 … BK-21, the analysis checklist); `CHANGELOG.md` `[1.2.1] - not yet released`; `APP_TESTPLAN.md` §X and the Summary.
- **The "before" — read in full:** `captures/CAP-074-2026-10-12_06-40-16_06-45-31-Group_BJ/CAP-074-FINDINGS.md` (**§1 the claim inventory of both clients and the
  Play-services marker `03 08 00 02 01 25`, §4 the holds, the cuts and every `Notify`, §7 vs `CAP-073`, §8 the root cause, §9 the options, §10 open questions**) and
  `CAP-074-EVENT-NOTES.md` (the method: the clock offset from the status bar, the audio's voiced segments, the claim-owner rule); `captures/CAP-073-…-Group_BI/CAP-073-FINDINGS.md`
  §0 (P0: the user without Play), §1 (0 Play-services claims, the positive control), §4 (the hold's main path: 31 s, every hold shown) and `CAP-073-EVENT-NOTES.md`
  (the layout: metadata, pre-flight, review method, timeline, step mapping, Test-IDs, checklist, the skeleton as appendix); `ai-sessions/0086_CAPTURE_RESULT_2026_10_12.md`
  (the method and the scratch scripts `ms_inventory.py`/`correlate.py`/`hdlc_crc.py`/`diffs.py` — re-create them, they are not in the repository); the maintainer's
  named examples `captures/CAP-071-2026-10-08_17-51-29_18-27-28-Group_BG/CAP-071-EVENT-NOTES.md` and `…/CAP-071-FINDINGS.md`.
- **Every Kotlin source a finding touches, in full,** before you write about it — at least: `BudsRepositoryImpl.kt` (`reconcileHold`, `releaseOrHold`, `endHold`,
  the `transport.channelClosed` collector with **`onHeldClaimTaken`**, `startReclaimChain`/`cancelReclaim`, `HOLD_RECLAIM_DELAYS_MS`, the log strings — every
  export line is matched against them), `HoldReclaimMode.kt` (`ChannelTaken`), `HoldReclaimSettingsStore.kt`, `AncScreen.kt` (`ancChannelTakenLine`,
  `ANC_HOLD_HINT_TEXT`), `SettingsMenu.kt` (`HoldReclaimCard`, the labels), `RfcommBudsTransport.kt` (the reader's `read return: -1` path, `reportChannelClosed`,
  `closeChannel`'s "released" line), `SessionReopener.kt`, `MainActivity.kt` (`onAncTabShown`, `onHoldReclaimModeChanged`), `OpenControlNavHost.kt`.

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.**
- Create **ai-sessions/0088_CAPTURE_RESULT_2026_10_12.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block.
- Update that block at the end of every phase **and** after every substantial step: every minute of the screen recording reviewed, every HCI log and channel
  decoded, every log file read, every checkpoint answer. Say what is done and what is next, which files are touched but not yet verified, and where the
  intermediate results live (the scratchpad: scripts, contact sheets, decoded tables, notes — re-create them if it is gone).
- A resumed session reads this prompt, then the RESULT's Progress block, re-reads the files the unfinished step touched, and continues from there. It never
  redoes a finished, recorded step and never assumes an unrecorded one was done. The prompt keeps its number and date; the RESULT grows in the same file.

**Memory and the machine.** The laptop has 15 GB and `/tmp` is a 7.7 GB tmpfs. Run `tshark`/`ffmpeg` one job at a time; extract contact sheets and crops in
ranges, never a whole film into memory. No Gradle build is needed. Never `git worktree add` a commit (it checks out `captures/`, gigabytes).

**Nothing is taken on trust, including this prompt, the maintainer's statements, the 0087 RESULT and the memory notes.** Every claim and every number measured
in §2 and §3 is re-derived from the recording, the HCI logs, the other logs, the code and official documentation.

---

## 1. What the maintainer asked for (chat, 2026-10-12, translated from Dutch)

1. "Make a new prompt for Claude Code … a comprehensive analysis of `CAP-075` (Group BK) of the OpenControl app, record the findings. Take the time to read
   the core files first. Run everything in phases, sequentially; when you hit a token limit, you must be able to continue automatically later."
2. **The goal, in the maintainer's words:** "In `CAP-073` I tested 1.2.0 build `5b4d5db` in a user without Google Play services on GrapheneOS; there, changing
   the ANC mode with a long press always works — the app shows every change, also in later tests. The same build does **not** work well in a user **with**
   Google Play services: after a Disconnect and a Connect, of several long presses only the first changes the ANC mode in the app; the next one I hear in my
   ears but do not see in the app. `CAP-074` shows that clearly. The goal was to find out why. In **1.2.1 build 31ee183** a fix was built with two options: the
   'Noise-control channel' switch (on the Settings tab) chooses between 'Show a hint, do not take it back' and 'Take it back while the noise-control tab is
   open (may fight that app)'. **`CAP-075` must establish what works best.**"
3. **The maintainer's statements about the run (verify each; label each as the maintainer's statement until verified):**
   - "The app is the newest version 1.2.1 build `5b4d5db`." — *the 1.2.1 build is `31ee183` (`ai-sessions/0087`, "After the session"); `5b4d5db` is 1.2.0's. The
     Info tab on film and the logcat's `package: …:10201` decide.*
   - "The capture was made on a Pixel 9a with GrapheneOS Android 17 **in a user with Google Play services**." — *see §2: the app logcat's header reads
     `userType: full.secondary` and the HCI log holds **0** Play-services claims — the user without Play, by the logs. This is the first thing to settle.*
   - "During the video I spoke only to say when I had done a **left** long press."
   - "Switching ANC modes with a long press during playback worked well, and the app's ANC mode changed with it."
   - "My observation: in GrapheneOS with Google Play services the switch's 'Show a hint, do not take it back' works well — which I find odd, since it should be
     no change from 1.2.0 (right?) — and it also seems to work better than 'Take it back while the noise-control tab is open'. Find out what works best."
     *The maintainer's "right?" is right: in HINT_ONLY the hold and its end are 1.2.0's code paths (ADR-061 unchanged); only the hint line and the log line
     are new. If both positions "worked well", the likely explanation is that nothing contended for the channel in this run — which is what a user without
     Play looks like (`CAP-073`). Establish it; do not assume it.*
4. "Compare `CAP-073`, `CAP-074` and `CAP-075` and determine which position of the switch works best." — *the maintainer wrote "`CAP-073`, `CAP-074` with
   `CAP-073`"; read it as `CAP-075`.*
5. Strict rules (the maintainer's): no assumptions — verify everything; validate claims externally against official documentation where needed; **no
   sampling** — a 100 % complete review of the relevant files; evidence traceability (every claim links to a frame, a log line or a film time) and structural
   integrity.
6. Standing practice: no commit or push without the maintainer's "yes" in chat; no release step; `main` takes no direct push (`RELEASING.md`).

---

## 2. The evidence (measured 2026-10-12 by the session that wrote this prompt — re-derive every number)

The folder `captures/CAP-075-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BK/` (the skeleton's placeholder name; the skeleton file is committed, the five other files are
**untracked**) holds:

| File | Measured | Notes |
|---|---|---|
| CAP-075-EVENT-NOTES.md | the committed skeleton (`ai-sessions/0087`; the build identity added in PR #41) | **exists** — the maintainer's text says "to be created": add the timeline to it, keep the planned procedure as the appendix |
| CAP-075-screen-20261012-172842-1791818551858.mp4 | 76.5 MB, 370.8 s, H.264 1080 × 2424, 4,203 video frames (≈ 11.3 fps average); **an AAC audio track** (15,962 frames); `creation_time` 2026-10-12T15:28:54Z (= 17:28:54 +02:00); file name stamp 17:28:42; mode 750 | Android's screen recorder; whether the name/`creation_time` is the start or the end is to be derived from the status bar (`CAP-074`: `creation_time` = the end + 3 s; `CAP-073`: the name = the start) — **it matters: see the time windows below** |
| CAP-075-btsnoop_hci.log | 1,832 packets, 2026-10-12 17:23:08.048 – 17:30:33.747 (`capinfos -a -e`; +02:00) | the current log — one Buds ACL: Connection Complete frame 151 (17:23:09.631, handle `0x000b`, the Buds' address) → Disconnection Complete frame 1814 (17:28:47.531, reason `0x13`); **nothing after 17:28:47 on the Buds' handle?** — check what the last 18 packets are |
| CAP-075-btsnoop_hci.log.last | 1,110 packets, 17:21:54.524 – 17:23:04.307 | the previous rotation (`cmp` differs at byte 37: not a prefix); an ACL on `0x000b` from frame 501 (17:22:45.301) to 1097 (17:23:04.162), and an LE handle `0x0040` ending at 1096 — the minutes before the run |
| CAP-075-opencontrol-debug-20261012-173158.txt | 228 lines, 17:25:20.122 – 17:31:53.825 (local time); mode 750 | one export, one process; it starts with "Permissions (start)" and an automatic re-open (LINK_BACK) at 17:25:20 — **the app was (re)started at 17:25:20**, two minutes after the ACL; it runs 3 minutes past the HCI log's end |
| CAP-075-OpenControl-for-Pixel-Buds-Pro-2-log-97b3b88fc408.txt | 272 lines; header `osVersion: google/tegu/tegu:17/CP3A.261005.005/2026100601:user/release-keys`, **`userType: full.secondary`**, `package: io.github.tedsluis.opencontrolpixelbuds:10201, targetSdk 34`; the last line 10-12 15:29:58.402 (**UTC**); 124 `OpenControlBuds` lines; mode 750 | the app logcat (App info → View logs) — `full.secondary` is the **secondary user** (`CAP-073`'s header had the same line for user 10; `CAP-074`'s had no `userType:` line in the Owner user) |

Not present: a **system log** (P6 — the stack's lines of a cut, `CAP-074` §10's open question; without it, if there was no cut, nothing is lost), a **shell log**
(P0/P1: `am get-current-user`, `pm list packages … gms`, the `dumpsys` before/after, the APK pull and its `sha256sum`), a camera film, a `sha256sum` file. Record
each as "not made" and what it leaves unverified; propose the APK pull now (`adb shell pm path --user <id>`, `adb pull`, `sha256sum` against
`~/opencontrol-1.2.1-tested` — the kept copy exists) if the maintainer attaches the phone (Checkpoint 0).

**First look, to be re-derived (this is the lead, not the finding):**
- **0 Play-services claims** in both HCI logs: `tshark -r <log> -Y 'btrfcomm && frame contains 03:08:00:02:01:25' | wc -l` → 0 and 0 (positive control:
  `CAP-074-btsnoop_hci.log` → 22, `CAP-066-btsnoop_hci.log.last` → 22). `CAP-073` (user 10) had 0; `CAP-074` (user 0 with Play) had 22.
- **10 Message Stream claims in the current log, all the app's** (`08 11 00 00` ×10 — the first phone message of every `SABM`), every one closed by the **phone's
  `DISC` after an app "released" line** — frames 460/546, 615/640, 782/845, 868/954, 1111/1159, 1192/1268, 1289/1389, 1442/1551, 1572/1670, 1724/1802 — and
  **no attempt-1 failure** in the export (`grep -c "connect failed after"` → 0; in `CAP-074` it was 14 of 16). The export's holds (lines 60, 98, 114, 145, 178, 207)
  ended "the noise-control tab was left" (five times, after 3.8–34 s) and "the app is not visible" (once, 17:28:43.653) — **never "the channel was closed, not by
  this app"**; no "re-claim", no "hint is shown" line (`grep -c` → 0 each; positive control: `CAP-074`'s export has 12 "not by this app" lines).
- 51 `Notify` frames (`frame contains 08:13:00:04:01`) in the current log — count how many are unprovoked, on which claim, and what the screen showed.
- **The time windows do not obviously overlap:** the export's holds lie between 17:25:24 and 17:28:45; the HCI log ends 17:30:33 with the ACL gone at 17:28:47
  (`0x13`); the film's name/`creation_time` say 17:28:42/17:28:54 — if that is the **start**, the film (371 s, to ≈ 17:34:5x) begins where the holds end and the
  Buds drop the link; if it is the **end**, the film covers 17:22:4x–17:28:5x, the whole run. Derive the offset from the status bar's minute changes and from a
  shared event (a Connect, the hold's end at 17:28:43.653 by "the app is not visible" — the recorder's own start/stop?) before anything else is correlated.
- The export's last lines (17:28:47 → 17:31:53) are a session loss and its cause lines ("the loss happened while the app was not visible…"?) — read them: what
  ended the session at 17:28:47 (`0x13` = the Buds; both buds into the case?), and whether the app tried to re-open.
- **Nothing in the logs says "user 0" or "Play present"**: no `03 08` marker, no `NearbyDiscovery` lines (no system log), `userType: full.secondary`. If the
  session confirms user 10, the Play part of `CAP-075` (BK-1 … BK-14) **has not been run** and the maintainer's "which works best" cannot be answered from
  this run — in a user without Play both positions are the same by design (ADR-062 item 2/3: the re-claim only follows a *foreign* close). Say that plainly,
  with the evidence; do not soften it.

State of the repository at the time of writing: `main` at `935a37a` (PR #39 and the bot PR #38 merged); open: PR #40 (`bot/sitemap`, `sitemap.xml`) and **PR #41**
(`docs/1.2.1-build`: the build identity in the skeleton and the 0087 RESULT — merge it first, or branch from it, so the skeleton you append to carries the build
lines). `android/domain/bin/` untracked (build output — never stage it). The 1.2.1 APK: `dist/1.2.1` (gitignored) and `~/opencontrol-1.2.1-tested` (B3 done).

---

## 3. Leads and context — verify each before relying on it

1. **Which user?** (🟡 user 10 until shown) The app logcat's `userType: full.secondary`; the HCI logs' 0 Play markers; no attempt-1 failures; every hold ended by the
   tab or the app, none by a close. Confirm on film (the launcher, the wallpaper, the user icon in Quick Settings, Settings → Apps for "Google Play services",
   anything that names the user) and from the maintainer at Checkpoint 0 (`adb shell am get-current-user` / `pm list users` and `pm list packages --user <id> |
   grep gms` with the exit statuses — the P0 that was not made). If the user was 10: the run is **part 2** of the skeleton (BK-15 … BK-18) and the Play part is
   open; if the maintainer insists it was user 0: then Play services made no claim in 7 minutes — itself a finding against `CAP-074` (22 claims in 5 minutes) that
   needs its own explanation (Play's *Nearby devices* permission revoked? Play disabled? the 🔴 "why Play stops re-claiming" of `TODO.md` §4) — and the evidence
   for either reading must be laid out side by side.
2. **The build:** "1.2.1 build 31ee183 (2026-10-12)" on the Info tab (film), `:10201` in the logcat; the Settings card on film ("Noise-control channel", which row
   selected); the ADR-062 strings in the export (none expected unless a foreign close happened).
3. **The switch positions actually used:** the export has no "re-claim"/"hint" lines, so neither position left a trace on the wire or in the log — only the film
   (the Settings tab) shows when the maintainer switched; the holds before/after a switch are the two "conditions". Map every hold (the export's "hold started"
   06 lines) to the position in force. The claim/release pattern is identical in both positions without contention — say so with the frames.
4. **Every press-and-hold:** the unprovoked `Notify` frames (no `08 11`/`08 12` in the 2 s before, nothing from the app after — `CAP-073` §4's criteria) on the
   app's claims, each with the mode, the time and the screen (the mode buttons at that film time) and the maintainer's cue word in the audio track (the
   `silencedetect` method of `CAP-074-EVENT-NOTES.md`; the maintainer said "left" only). Which holds fell outside a hold (between claims) and were therefore not
   shown — if any — and whether the Buds then sent a `Notify` to nobody (`CAP-072` §5: no claim ⇒ no frame).
5. **"During playback":** AVRCP play status / AVDTP streaming on the wire during the holds; the audio track's energy. Say it with evidence (`CAP-073`/`CAP-074`:
   "no media" both times despite the statement).
6. **The session ends:** the ACL `0x13` at 17:28:47.531 (the Buds), what preceded it on film (both buds into the case?), the export's loss-cause lines, and why the
   HCI log holds nothing of a re-open afterwards (the app not visible? Bluetooth off? the log pulled at 17:30:33?). The `.last` log's own ACL (17:22:45 → 17:23:04)
   and the gap to the current log's ACL at 17:23:09 — a Bluetooth off/on, a user switch (an `am_switch_user` would be in a system log — not available), or the
   buds in and out of the case?
7. **The app start at 17:25:20** (export line 1 "Permissions (start)"), 2 minutes after the ACL: a first install in this user? an update over 1.2.0 (P1 says no
   uninstall)? The logcat's `wm_on_create_called`/`am_proc_start` lines (if its `events` buffer reaches back) and the first lines of the export ("Permissions
   (start): … NOT_REQUESTED -> GRANTED" = a process start, not necessarily an install).
8. **The comparison (§4 item 11):** `CAP-073` (user 10, 1.2.0, no Play: the hold 31 s, 4 of 4 holds shown), `CAP-074` (user 0, 1.2.0, Play: every hold cut ≈ 5 s,
   9 of 44 shown), `CAP-075` (this run: the user per lead 1, 1.2.1, the positions per lead 3: N holds, M shown). The honest answer to "which works best" is
   **conditional on contention**: without Play both are the same; with Play, ADR-062's design (`ai-sessions/0087` §A.3) predicts ≈ 1 of N for HINT_ONLY and
   ≈ 60 % for RECLAIM during the chain — **untested until part 1 is run in the Play user**. If this run was user 10, say: "`CAP-075` verifies part 2 (the
   regression in both positions); part 1 is not run; the question is open" — and propose the re-run (the same skeleton, BK-1 … BK-14, P0 with Play's
   *Nearby devices* permission on film, the system log within a minute of the first cut).
9. **Privacy:** the recording's audio carries the maintainer's voice; the screen may show the Wi-Fi name (Quick Settings), notifications, other device names, the
   serial numbers on the Info tab (allowed on film, never unredacted in text — first 4 + last 2, ADR-058 item 5); the logcat prints `XX:XX:XX:XX:6E:07`-style
   addresses (as `CAP-074`); the export's Debug-mode hex lines may carry the `GetHardwareInfo` answer (ADR-058 item 5 allows it). No system log this time.
10. **Test-IDs** (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`; `AGENTS.md` §13 step 7): `PAIR-003`, `BATT-004`, `FW-003`, `ANC-001`…`ANC-004`, `TOUCH-007` (the skeleton's
    list); `AUDIO-002` if Volume EQ was touched; `CASE-004`/`CASE-005` if buds went in/out of the case. Re-derive from what the run actually did; "expected but
    not observed" for each that was not — and the skeleton's step mapping BK-1 … BK-21 (done / not done / done differently), part 1 and part 2 separately.
11. **External validation** where a claim rests on a specification or platform behaviour (fetch, quote with the date): Android's user types (`UserManager`,
    "full.secondary" — the user-type names of `UserManager` / the `UserInfo` flags), the Fast Pair Message Stream and Hearable Controls pages (already quoted in
    `CAP-074-FINDINGS.md` §8 — re-fetch only if a new claim needs them), the Bluetooth Core Specification for the `0x13` reason ("Remote User Terminated
    Connection").

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename, mode change or `git add`)

1. Create the RESULT (§0). Record `git log -1`, `git status --short`, `git fetch`, `git log --oneline HEAD..origin/main`, and the state of PR #40/#41. Create the
   branch `capture/cap-075` from an up-to-date `main` (`git switch main && git pull --ff-only && git switch -c capture/cap-075`); if PR #41 is still open, say so
   and base the branch on `origin/docs/1.2.1-build` instead (or cherry-pick its one commit) so the skeleton has the build lines. `main` takes no direct push.
2. List the folder fresh (`ls -la`, `sha256sum`), diff it against §2's table and say every difference.
3. **Checkpoint 0 (`AskUserQuestion`, one question per decision, options with pros and cons, "(Recommended)" on one, the exact commands in the preview):**
   **which user the run was made in** (the logs' reading vs the maintainer's statement; what the maintainer can still check on the phone: `adb shell am
   get-current-user`, `pm list users`, `pm list packages --user <id> | grep gms`); the folder rename to the recording's first/last phone times
   (`CAP-075-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BK` → the real times, after Phase A's clock offset) and the recording's rename to CAP-075-recording.mp4 (the
   skeleton says so); `chmod 644` on the three files with mode 750; a `sha256sum` file; the privacy treatment of the recording (blur ranges; the audio track stays —
   the maintainer's cue words); whether the phone can be attached for the APK pull (`pm path --user <id>`, `adb pull`, `sha256sum` vs `~/opencontrol-1.2.1-tested`)
   and `am get-current-user`; whether the originals go to `~/opencontrol-capture-originals/CAP-075/`.
4. Update the `CAP-075` row of `id_registry.csv` (it exists with status `planned`; `analyzed` in Phase D) and prepare the Group BK section's status line of
   `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (written in Phase D).

### Phase A — the screen recording, in full

5. Decode the recording at 1 frame per second (371 frames) into the scratchpad in ranges; compare each second with the previous one; view **every** second with a
   change on labelled contact sheets; narrow transitions at 10 fps where a claim depends on them (the mode buttons, the hint line, the Settings card's selected
   row, the (i) dialogs, the connection state). No sampling: every second is either viewed or proved unchanged.
6. The audio track: you cannot listen. Extract it (`ffmpeg -vn`), derive the voiced segments with `silencedetect`/`astats` (RMS per 100 ms) and tabulate every
   segment with its film time; align them with the maintainer's statement ("left long press" only) and with the wire's unprovoked `Notify` frames. Label the
   words as the maintainer's statement, the timing as measured. Detect music playback the same way — label it.
7. **Measure the clock offset** (status-bar minute changes; a Connect, a hold's start/end or the recorder's own start that matches an export line) and write it with
   its uncertainty — and resolve §2's time-window question (is the file stamp the start or the end?) before any correlation.
8. Write the timeline into the existing CAP-075-EVENT-NOTES.md: phone time · what happened · step BK-n / Test-ID · evidence — in `CAP-073`'s layout (the status line
   becomes "captured … and analyzed …", Log Metadata filled in, the capture-integrity pre-flight, the review method and privacy, the Event Timeline incl. "before
   the recording" and "after the recording", the step mapping BK-1 … BK-21 done / not done / done differently — part 1 and part 2 separately — Test-IDs per
   `AGENTS.md` §13 step 7 with "expected but not observed", the analysis checklist ticked; the planned procedure kept below as the appendix).

### Phase B — the HCI logs and the other logs, in full

9. Both HCI logs: Connection Complete events and the Buds' handle (by address; `bluetooth.addr` may match nothing — show the filter matching a known frame), the
   boundary between the two logs (and what the `.last` log's ACL was), the RFCOMM inventory per DLCI and direction, every pw_hdlc frame CRC-32-checked
   (`scripts/pwrpc_decode.py`'s `parse`/`describe`, with its positive control on `CAP-036`), every Message Stream message decoded (`[Group][Code][Len][Value]`),
   **the full claim inventory of DLCI 0x04/0x05 with the sender of every `SABM`/`UA`/`DISC`/`DM` and the owner of every claim** (`CAP-074` §1's rule), the Play
   marker count with its positive control, every `Notify` with the claim it arrived on, AVDTP/AVRCP state, the Connect reads byte for byte against `CAP-074` §2 /
   `CAP-073` §2 (the thirteen requests and `GetHardwareInfo` per session), every write. Commands and exit statuses in FINDINGS (`PROJECT_RULES.md` rule 4a;
   `AGENTS.md` §13 step 8 for every negative).
10. The debug export and the app logcat: every line read (228 + 272) — every export line matched to the code's log string (`BudsRepositoryImpl.kt`,
    `RfcommBudsTransport.kt`, `SessionReopener.kt`, `BleLogger`): the hold starts/ends with their reasons, the releases, the claim lifetimes (SABM → DISC) against
    `MESSAGE_STREAM_LINGER_MS`, the absence of every ADR-062 line, the session-loss lines; the logcat's `events` lines (process start, `am_proc_start`, user id if
    present), the `BluetoothSocket` close lines, crashes/ANRs/StrictMode.

### Phase C — correlation, the findings, the comparison

11. Correlate every timeline row with its frames and log lines. Write CAP-075-FINDINGS.md in `CAP-073`/`CAP-074`'s structure: §0 build, install, **user** (the
    evidence for which user, side by side), logs, privacy; §1 ACLs and the claim inventory (both clients — here presumably one); §2 the Connect reads byte for
    byte; §3 every write; **§4 the holds: each hold's start/end reason, each press-and-hold's `Notify`, the screen, the switch position in force, and the
    ADR-062 paths that were and were not exercised** (no foreign close ⇒ `onHeldClaimTaken` never ran — say which test covers it instead); §5 playback; §6
    session ends and re-opens (the `0x13`, the `.last`/current boundary, the 17:25:20 start); **§7 `CAP-073` vs `CAP-074` vs `CAP-075`** — a table per aspect
    (user, build, Play marker, claim owner over time, hold lifetime and end reason, holds shown / reached Play / reached nobody, the switch position, the
    first-hold-only pattern) **and the conditional answer to "which works best"** (§3 item 8: with the evidence of each run, what is verified, what ADR-062
    predicts, what is untested); §8 the skeleton's "Refuted if" applied literally per step that was run; **§9 the verdict table (`RELEASING.md` C4)** — what this
    run verifies for 1.2.1 (part 2's regression if user 10; no heavy defect?), what it does not (part 1), and the analysing session's view: *release 1.2.1 on
    part 2 alone, or run part 1 first* — the decision is the maintainer's; §10 open questions. FACT / HYPOTHESIS / ASSUMPTION / OPEN QUESTION on every
    conclusion (`PROJECT_RULES.md` §1); every claim with a frame, a log line or a film time.
12. Validate externally (§3 item 11): fetch, quote with the date.

### Phase D — checkpoint, documentation, finish

13. **Checkpoint (`AskUserQuestion`):** the user question's final reading (if not settled at Checkpoint 0); **the verdict** (release 1.2.1 on this run, or run part 1
    in the Play user first — with the pros and cons: `RELEASING.md` C4 "OK / minor / heavy", `PROJECT.md`'s Definition of done is the user without Play, the
    `CHANGELOG.md` Known-issues line already says "not yet checked on a phone"); every proposed ADR-062 Update (the numbers of this run, "hardware-verified for
    part 2 / not for part 1" — a draft in the preview; `AGENTS.md` §6: nothing written without the maintainer's answer in this chat) and any `PROTOCOL.md` note;
    whether `CHANGELOG.md`'s Known-issues line changes; the privacy treatment; whether the folder's untracked files are committed now (Git LFS, `.gitattributes`
    covers `captures/**` `.log`, `.log.last`, `.mp4`, `.txt`) and on which branch. Record the answers verbatim in the RESULT.
14. With the answers: `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 (the `CAP-075` row: the real times, the user, analyzed — or "part 2 analyzed, part 1 planned") and the
    Group BK status line; `id_registry.csv`; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the Group column: BK on the Test-IDs exercised); `APP_TESTPLAN.md` §X (the Result
    column per row, the Summary row, a run note like §W's); `TODO.md` §2/§3/§5 (part 1 open if so; the 1.2.1 release steps; the Nearby-devices item);
    `ARCHITECTURE.md` §5a (the "Changed by the Buds" row: what `CAP-075` verified) and §6.0b (one sentence, as approved); `DECISIONS.md` ADR-062 Update (as
    approved); `PROJECT.md`/`README.md` only as approved. `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and `python3 scripts/lint_docs.py` (exit 0;
    write scratch file names without backticks — the lint flags backticked names that do not exist).
15. The RESULT: plain-language summary first (what happened on the phone, in which user, what each switch position did, what can and cannot be said about
    "which works best", what the maintainer decides next), the phases, the files read in full / in part, "Deferred documentation" (each also in `TODO.md`),
    "Commits"; the `ai-sessions/INDEX.md` row (0088; the 0087 row's Status gets the back-fill of this session's number if it refers to "the next session").
16. Show a short summary and **ask whether to commit and push**. Only after a yes: commits per concern (Conventional Commits: `docs(capture)`, `docs`), each with
    its *why* and the attribution line; Git LFS for the capture files; `git fetch` and rebase; push without force; open the pull request only when asked, and
    never merge it yourself.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Scope:** analysis and documentation only. No change under `android/`, no new skeleton (a re-run of part 1 reuses `CAP-075`'s skeleton — propose it as text),
  no release step, no `dist/` change, no `scripts/release.sh`, no tag.
- **Approvals only in this chat** (`AGENTS.md` §6): no 🟢 FACT promotion, no new ADR or ADR Update, no verdict recorded as the maintainer's without their answer
  here; ADR-060 stays a draft. "Which works best" is answered with the evidence of each run and its conditions — never by preference.
- **Evidence:** every claim cites a frame (log and number), a log line or a film time; a negative needs its command, its exit status and a positive control
  (the Play marker's positive control is `CAP-074`'s log: 22); a hypothesis names its settling experiment. The maintainer's statements are statements until
  verified — including "with Google Play services" and "build 5b4d5db".
- **No sampling:** every second of the recording, every packet of both HCI logs on the Buds' handle, every line of the export and the app logcat — reviewed or
  covered by a stated, positive-controlled filter.
- **Privacy:** no serial unredacted, no MAC, no phone number, no account name or e-mail, no Wi-Fi name in any committed text; the recording's blur ranges and the
  originals' place are Checkpoint 0's.
- **Files:** before moving, renaming or deleting anything, diff a fresh listing; never wildcard deletes; ask first. `android/domain/bin/` is never staged.
- **Subagents** may only read and report; every write is done and verified in the main session.

## 6. Deliverables

- CAP-075-EVENT-NOTES.md (the timeline added to the skeleton) and CAP-075-FINDINGS.md (with the user question settled, the three-way comparison, the conditional
  "which works best", the verdict table), with the folder and files as approved in Checkpoint 0.
- The registry, index, test-plan, `TODO.md`, architecture, ADR-062 Update and status updates as approved; the proposal for the part-1 re-run if the evidence
  shows it was not run.
- ai-sessions/0088_CAPTURE_RESULT_2026_10_12.md and the `ai-sessions/INDEX.md` row.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0088_CAPTURE_PROMPT_2026_10_12.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0088_CAPTURE_PROMPT_2026_10_12
