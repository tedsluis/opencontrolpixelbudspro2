# 0086_CAPTURE_PROMPT_2026_10_12.md — Full analysis of CAP-074 (Group BJ: the released 1.2.0, build 5b4d5db, in the GrapheneOS user **with** Google Play services — why only the first press-and-hold after a Connect changes the ANC mode on screen)

**Number:** 0086
**Category:** CAPTURE
**Date:** 2026-10-12
**Title:** Fully analyse `CAP-074`: **one** Android screen recording (with an audio track: the phone's audio and the microphone), **two** HCI snoop logs, **one**
app debug export, **one** app logcat and **one** system log. It was recorded with the published, release-signed **1.2.0 build `5b4d5db`** (`ai-sessions/0084`,
`0085`; tag `v1.2.0`), on a Pixel 9a with GrapheneOS (Android 17), **in the user with Google Play services** — the user `CAP-073` did **not** test. The
maintainer did a Disconnect and a Connect and then pressed and held a bud several times: the ANC mode changed audibly every time, but **the app's mode
changed only after the first hold**. In the user without Play services (`CAP-073` and later, untaped tests) every hold changes the app's mode.
- Record the real events in **CAP-074-EVENT-NOTES.md** and the analysis in **CAP-074-FINDINGS.md**.
- Establish, from the recording, the wire, the logs and the code, **why** the second and later holds do not reach the app in the user with Play services
  and do in the user without; compare `CAP-074` with `CAP-073`; propose the fix options for the maintainer's decision.
- **No change to the app itself**, **no new capture skeleton**, **no release step**: the analysis and its documentation only. A fix, if the maintainer wants
  one, gets its own FEATURE prompt (as `ai-sessions/0083` → `0084` did).

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this repository. Run the
phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Take the time to read the core files first, in full and in this order, to
understand the ground rules before auditing anything:
- `AGENTS.md` and `PROJECT_RULES.md`.
- `PROJECT.md` (the Definition of done with its evidence table; the 1.2.0 status paragraph).
- `ARCHITECTURE.md`, in particular §2.4, §3.1 (the ANC row, "one rule for current"), §3.2 (`MESSAGE_STREAM_LINGER_MS`, `DEFAULT_CONNECT_ATTEMPTS`), §5a (the
  "Changed by the Buds" row), §6.0a, **§6.0b** (the per-channel handling of ADR-032 and the ADR-061 hold paragraph: "the Buds (or another client) closing DLCI
  0x04 ends it **without a re-claim**"), §7, §8.1, §9, §12 and §13.
- `PROTOCOL.md`, in particular §2.2a with all its Updates, §2.3 (the three-channel table), **§4.1** (ANC `Get`/`Set`/`Notify`, the Settable byte), §4.3
  Option B/F and §8 (the 2026-10-10 rows).
- `DECISIONS.md`: **every** ADR, ADR-001 … ADR-061, with every dated Update, and the **draft** ADR-060 (proposed, not accepted — not a decision). In particular
  **ADR-032** (on-demand claiming, its (i) on Play services' recovery of its socket in 2.7–5.0 s, and every Update), ADR-042, ADR-044, ADR-045, ADR-048,
  ADR-049, ADR-057, ADR-058, ADR-059 and **ADR-061** with its 2026-10-10 Update (its Consequences foresaw Play's Fast Pair being locked out while the tab is
  open — `CAP-074` may show the reverse).
- `TODO.md`, §1 (nothing open after 1.2.0), §2 (the hold's untested branches: "a loss or re-open while the tab is shown"; the 🔴 "Why Play services stops
  re-claiming DLCI 0x04 (`CAP-059`, `CAP-061`)"; the Nearby-devices permission item) and §5.

These are the ground rules; do not audit or change anything before they are read. Then, per task:

- **Procedure and registers:** `AI_SESSION_LOG_PROCEDURE.md` (in full, incl. §4b and §9), `ai-sessions/INDEX.md`, `id_registry.csv` (the `CAP-059`, `CAP-061`,
  `CAP-065`, `CAP-071` … `CAP-073` rows, ADR-032, ADR-061, every Test-ID named in §3 item 9).
- **The build under test — read in full:** `ai-sessions/0084_FEATURE_PROMPT_2026_10_10.md` and `…/0084_FEATURE_RESULT_2026_10_10.md` — §A.1, §A.2 (the design
  of the hold), **§B (the maintainer's checkpoint answers: "no re-claim after a close", the re-open's snapshot kept)**, §E, "After the session — the build"
  (build `5b4d5db`, APK SHA-256 `ac04415eabf4369a49e9f88230aa83fc858a7e3ea0223d725c14a43ac5a67220`, certificate
  `a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb`); `ai-sessions/0085_CAPTURE_PROMPT_2026_10_10.md` and `…/0085_CAPTURE_RESULT_2026_10_10.md`
  (the method and the verdict of `CAP-073`); `CHANGELOG.md` `[1.2.0] - 2026-10-10`; `RELEASING.md` §11a, §13 (1.2.0 published 2026-10-10 08:19:35 UTC).
- **The "before" and the layout to follow — read in full:** `captures/CAP-073-2026-10-10_08-23-35_08-33-24-Group_BI/CAP-073-EVENT-NOTES.md` and
  `…/CAP-073-FINDINGS.md` (the same build in the user **without** Play: §1 the claim inventory, **§4 the hold and the four unprovoked `Notify`**, §6 the
  session ends, §9 the verdict, §10 the open questions); `captures/CAP-072-…-Group_BH/CAP-072-FINDINGS.md` §0 (the system-log extract method) and §5 (the
  press-and-hold gap that led to ADR-061); and the maintainer's named examples `captures/CAP-071-2026-10-08_17-51-29_18-27-28-Group_BG/CAP-071-EVENT-NOTES.md`
  and `…/CAP-071-FINDINGS.md` (clock offsets, the HCI prefix proof, the session-end table, the Play-services marker, the `bugreportz`/SIGQUIT explanation, the
  system-log extract).
- **The earlier evidence on Play services and DLCI 0x04 — read the named sections in full:** `ai-sessions/0039_FEATURE_RESULT_2026_09_19.md` §8 (the
  stack's `RFCOMM_CreateConnectionWithSecurity: already at opened state` and the incumbent's port closed with it; the maintainer's design decisions),
  `ai-sessions/0040_FEATURE_RESULT_2026_09_19.md` (ADR-032's implementation), `CAP-059-FINDINGS.md`, `CAP-061-FINDINGS.md` and `CAP-065-FINDINGS.md` (the
  sections on the Message Stream claims and Play services' re-claims), `CAP-066` (the Play-services marker's positive control, 22 frames).
- **Logging and capture method:** `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3, §5, §8, §9 and the Group BG–BI sections (Group BJ does not exist yet — this session
  writes it, see Phase 0); `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` for every Test-ID in §3 item 9.
- **Every Kotlin source under `android/` that a finding touches, in full,** before you write about it — at least: `BudsRepositoryImpl.kt` (`reconcileHold`,
  `releaseOrHold`, `endHold`, `HoldEnd`, the `transport.channelClosed` collector with its ADR-061 "no re-claim by itself" comment, `withMessageStream`,
  `scheduleRelease`, `causeOfNotify`, `refreshAncMode`, `handleRoutedFrame`), `BudsRepository.kt`, `RfcommBudsTransport.kt` (`openChannel`, `closeChannel`,
  `channelClosed`, the reader's `IOException` path, the connect retries), `SessionReopener.kt`, `OpenControlNavHost.kt`, `MainActivity.kt`, `AncScreen.kt`
  (the (i) texts), `AncTileService.kt`, `BleLogger.kt`.

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.**
- Create **ai-sessions/0086_CAPTURE_RESULT_2026_10_12.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block.
- Update that block at the end of every phase **and** after every substantial step: every minute of the screen recording reviewed, every HCI log and channel
  decoded, every log file read, every 20,000 lines of the system log, every checkpoint answer. Say what is done and what is next, which files are touched but
  not yet verified, and where the intermediate results live (the scratchpad: scripts, contact sheets, decoded tables, notes — re-create them if it is gone).
- A resumed session reads this prompt, then the RESULT's Progress block, re-reads the files the unfinished step touched, and continues from there. It never
  redoes a finished, recorded step and never assumes an unrecorded one was done. The prompt keeps its number and date; the RESULT grows in the same file.

**Memory and the machine.** The laptop has 15 GB and `/tmp` is a 7.7 GB tmpfs.
- Run `tshark`/`ffmpeg` one job at a time; extract contact sheets and crops in ranges, never a whole film into memory. Read the 10.8 MB system log and the
  4.3 MB `.last` HCI log with `grep`/`sed`/`awk`/`tshark -Y` in ranges.
- Never `git worktree add` a commit (it checks out `captures/`, gigabytes). No Gradle build is needed.

**Nothing is taken on trust, including this prompt, earlier RESULTs, the memory notes and the maintainer's statements.** Every claim and every number measured
in §2 and §3 is re-derived from the recording, the HCI logs, the other logs, the code and official documentation.

---

## 1. What the maintainer asked for (chat, 2026-10-12, translated from Dutch)

1. "Make a new prompt for Claude Code … to carry out a comprehensive analysis of `CAP-074` of the OpenControl app and record the findings. Take the time to
   read the core files first."
2. "Run everything in phases. All phases must be run sequentially. When you hit a token limit, you must be able to continue automatically later."
3. **The goal:** "In `CAP-073` I tested 1.2.0 build `5b4d5db` in a user without Google Play services on GrapheneOS. There, changing the ANC mode with a
   press-and-hold always works: I always see the ANC mode change in the app — also in later tests. I found, however, that the same build does **not** work
   well in a user **with** Google Play services on GrapheneOS: after a Disconnect and a Connect, several press-and-holds change the ANC mode in the app only
   the **first** time. On the next hold I hear the mode change in my ears, but the app's mode does not change. `CAP-074` shows this clearly. The goal is to
   find out why this happens in the user with Play services and not in the user without."
4. **Analysis:**
   - first analyse CAP-074-recording.mp4 and record the events and actions with their time in CAP-074-EVENT-NOTES.md (to be created);
   - analyse all `CAP-074-btsnoop_hci*` logs with `tshark`;
   - analyse CAP-074-opencontrol-debug-20261012-070305.txt, CAP-074-OpenControl-for-Pixel-Buds-Pro-2-log-5e00d0eabad2.txt and
     CAP-074-System-log-3d4f8ff69fde.txt;
   - then correlate the events of CAP-074-EVENT-NOTES.md with the HCI logs, the system log, the debug export and the app logcat (`CAP-071-EVENT-NOTES.md` as
     the example);
   - carry out an extensive analysis and record the findings in CAP-074-FINDINGS.md (`CAP-071-FINDINGS.md` as the example);
   - **compare `CAP-074` with `CAP-073`**: which findings from that comparison point to the cause of the ANC mode not updating in the app?
5. **Strict rules:** no assumptions — verify everything; validate claims externally with search tools against official documentation (Android Developer Docs,
   the Bluetooth Core Specification, the Fast Pair specification) where needed; **no sampling** — a 100 % complete, exhaustive review of the relevant files,
   every detail checked; industry-standard review methodology: traceability of evidence (every claim links to a specific frame, log line or film time) and
   structural integrity.
6. **The maintainer's statements (verify each; label each as the maintainer's statement until verified):**
   - "The app is the newest version 1.2.0 build `5b4d5db`."
   - "The capture was made on a Pixel 9a with GrapheneOS Android 17, in a user with Google Play services."
   - "During the video I spoke only to say when I had done a left or a right press-and-hold."
   - "Switching ANC modes with a press-and-hold during playback worked; the app's ANC mode followed only when I had just done a Disconnect and a Connect. A
     second hold on a bud changed the mode audibly, but not visibly in the app."
   - The maintainer's chat text says "Group BI"; the folder says `Group_BJ`. **Group BI is `CAP-073`.** `CAP-074` is a new group, **BJ** — confirm this in
     Checkpoint 0 (§4 item 3) and use BJ unless the maintainer says otherwise.

---

## 2. The evidence (measured 2026-10-12 by the session that wrote this prompt — re-derive every number)

The folder `captures/CAP-074-2026-dd-mm_hh-mm-ss_hh-mm-ss-Group_BJ/` (a placeholder name the maintainer typed; **no committed skeleton** exists for this run,
and nothing in the repository mentions `CAP-074` or Group BJ except the `id_registry.csv` row and the `INDEX.md` row written with this prompt) holds six
**untracked** files:

| File | Measured | Notes |
|---|---|---|
| CAP-074-recording.mp4 | 38.8 MB, 315.2 s, H.264 1080×2424, 2,559 video frames (≈ 8.1 fps average); **an AAC audio track** (13,571 frames); `creation_time` 2026-10-12T04:45:34Z (= 06:45:34 +02:00) | presumably the **end** (`CAP-073`'s file name carried the end time too) ⇒ start ≈ 06:40:19 — derive the clock offset from the status bar and from shared events; touch indicators on; mode 750 |
| CAP-074-btsnoop_hci.log | 18 k packets, 2026-10-11 19:50:00.312 – 2026-10-12 06:52:09.044 (`capinfos -a -e`; the timestamps print in +02:00) | the current log — covers the run and a 06:30–06:31 window before it (see §3 item 6) |
| CAP-074-btsnoop_hci.log.last | 65 k packets, 2026-10-11 09:20:50.730 – 2026-10-11 19:50:00.311 | ends 1 ms before the current log starts — the previous rotation, **not** a prefix (`cmp -n 1100280` differs at byte 20). Check what, if anything, of the run is in it (expected: nothing) |
| CAP-074-opencontrol-debug-20261012-070305.txt | 692 lines, 06:38:10.182 – 07:03:04.708 (local time) | one debug export, one process; it runs **18 minutes past the recording's end** — the lines after 06:45:34 are activity after the film; mode 750 |
| CAP-074-OpenControl-for-Pixel-Buds-Pro-2-log-5e00d0eabad2.txt | 351 lines; header `osVersion: google/tegu/tegu:17/CP3A.261005.005/2026100601:user/release-keys`, `package: io.github.tedsluis.opencontrolpixelbuds:10200, targetSdk 34`, buffers main,system,crash,events,kernel; lines 10-12 04:30:20.102 – 04:46:12.407 (**UTC** in `CAP-072`/`CAP-073` — check) | the app logcat; **no `userType:` line** (`CAP-073`'s header had `userType: full.secondary`) — which user ran the app is to be established from the system log (uid/user id) or the maintainer; two app PIDs appear (12456 at 04:30:20, 13677 at 04:46:12 with a SIGQUIT = the log exporter's own trace dump, as in `CAP-071`); mode 750 |
| CAP-074-System-log-3d4f8ff69fde.txt | 81,044 lines, 10.8 MB; the same header; buffers kernel (from 10-08 19:46:59), crash, events, system, main; last line 10-12 04:46:36.780 | the **full system log** — privacy as in `CAP-072-FINDINGS.md` §0 (an extract is committed, the full log stays local); mode 750 |

Not present: a camera film, a shell log (`dumpsys`, `pm list packages`, `am get-current-user`), a `sha256sum` file, the installed APK's hash — record each as
"not made" and what that leaves unverified (the build identity rests on the Info tab on film, the system log's `dex2oat`/install lines and, if the maintainer
attaches the phone, `adb shell pm path` + `adb pull` + `sha256sum` against `~/opencontrol-1.2.0-tested`, as `CAP-073` did).

State of the repository at the time of writing: branch `main` at `a31fcc9` (level with `origin/main`), no other branch; `android/domain/bin/` untracked
(build output — never stage it). Re-check with `git status`, `git fetch`, `git log --oneline HEAD..origin/main`.

**First look, to be re-derived (this is the lead, not the finding):** the debug export logs, after every Connect of the Disconnect/Connect series
(06:40:50 … 06:44:52, nine Connects in 4 minutes), "Message Stream hold started: the noise-control tab is on screen (ADR-061)" and **1–5 s later**
"RFCOMM on-demand channel 0x04 closed (IOException: bt socket closed, read return: -1) — session channels untouched" followed by "Message Stream hold ended:
**the channel was closed, not by this app**" — **eleven times** between 06:40:55 and 06:44:58. In `CAP-073` (the user without Play) the only hold end in the
run was "the noise-control tab was left". The current HCI log carries the Play-services marker `frame contains 03:08:00:02:01:25` **22 times** (`CAP-073`: 0;
positive control `CAP-066`: 22) and 107 frames containing `08 13 00 04 01` (`CAP-073`'s run: a handful) — count the Buds' unprovoked `Notify`s and say
which client each one reached.

---

## 3. Leads and context — verify each before relying on it

1. **The working hypothesis (🟡 until the wire and the system log show it):** in the user with Play services, Google Play services' Fast Pair re-claims
   DLCI 0x04 within seconds of the app's hold (ADR-032 (i): 2.7–5.0 s recoveries; `ai-sessions/0039` §8: the stack refuses a second connection to an open
   (device, channel) with `RFCOMM_CreateConnectionWithSecurity: already at opened state` **and its failure path closes the incumbent's port**). The app's
   hold then ends "not by this app" and, by ADR-061's design (the maintainer's 2026-10-10 choice: no re-claim by itself), nothing claims again until the next
   tap, pull or tab re-entry. The **first** hold after a Connect lands while the snapshot claim/hold is still open (`CAP-073` §4's path) and is shown; every
   later `Notify` has no recipient in this app. Establish, for every hold in the run: the time of the `SABM`/`UA` of the app's claim, who sent the `DISC` (or
   whether the stack closed the socket without a `DISC` — read the `IOException` text against the HCI frames), Play services' own `SABM` and its claim's
   lifetime, each unprovoked `08 13 00 04 01 …` with its mode and Settable byte and **which RFCOMM connection carried it** (the app's or Play's — by time
   against the claim inventory), and what the screen showed. Use `CAP-072-FINDINGS.md` §1's claim-inventory method on both directions of DLCI 0x04/0x05.
2. **The system log is the second witness.** Grep the run's window (10-12 04:40–04:46 UTC, and the 04:30–04:32 pre-run window) for the Bluetooth stack's
   RFCOMM lines (`bt_stack`, `RFCOMM_CreateConnectionWithSecurity`, `already at opened state`, `PORT_`, `rfc_`, `bta_jv`, the uid/app of each connect —
   `com.google.android.gms` vs `io.github.tedsluis.opencontrolpixelbuds`), the app's own `BleLogger` lines, Play services' Fast Pair lines (`FastPair`,
   `Nearby`, `gmscompat`), and any `StrictMode`/`am_wtf`/crash/ANR. State every command with its exit status and a positive control (`AGENTS.md` §13 step 8).
   Remember the stack lines may be kept only for the last minutes — say what the log does and does not reach back to.
3. **The code's path, line by line.** `RfcommBudsTransport`'s reader on DLCI 0x04 gets `read return: -1` → `channelClosed` → `BudsRepositoryImpl`'s collector
   → `codecRouter.reset`, `_modelIdThisClaim = null`, `endHold(CHANNEL_CLOSED)` and **no re-claim**. Trace what the UI shows after that (the mode buttons keep
   the last value; is there any hint that the channel is gone? the ANC (i) text), what the next tap does (a fresh `SABM` + `08 11 00 00`), and what the
   `SessionReopener` does on the maintainer's Disconnect/Connect (ADR-044's snapshot claim kept as the hold while the tab is shown — the path that makes the
   **first** hold work). Quote the log lines of the export against the code's log strings.
4. **Why the user without Play differs:** in `CAP-073` nobody contended for DLCI 0x04 (0 marker frames); the hold lived 31 s and ended by the tab. If
   `CAP-074`'s holds all end by a close, the difference is contention, not the Buds. Confirm that the Buds' behaviour is the same in both users (the
   `Notify`s are sent either way — show them on the wire in `CAP-074` even when the app had no claim) and that the app's code path is the same build (the
   Info tab, the system log's install lines).
5. **"During playback":** the maintainer says music was playing. Check AVDTP streaming/AVRCP on the wire, the phone's audio in the recording's audio track,
   and whether playback has any effect on the claims (it should not; say so with evidence or flag it).
6. **The 06:30–06:31 window before the recording:** the current HCI log shows DLCI 0x04 `SABM`/`DISC` pairs from 06:30:20 to 06:31:52 and the app logcat a
   `MainActivity` create at 04:30:20 UTC — an earlier app session in this user, before the film. Inventory it the same way (it may show the same close
   pattern) and keep it apart from the filmed run in the timeline ("before the recording").
7. **The export's tail (06:45:34 – 07:03:04):** after the film the maintainer kept using the app (holds started/ended until 07:03:03). Inventory those lines
   too, mark them "after the recording", and use them as extra samples of the same pattern — without a film they are wire + log only.
8. **Play services' recovery timing against `MESSAGE_STREAM_LINGER_MS` and the hold:** measure every Play re-claim's delay after the app's `DISC` or after the
   app's `SABM` (the contention case), and compare with ADR-032 (i)'s 2.7–5.0 s and with `CAP-059`/`CAP-061`/`CAP-065`'s numbers. This is the number a fix
   would be designed around.
9. **Test-IDs** (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`; `AGENTS.md` §13 step 7): `PAIR-003` (Disconnect/Connect), `ANC-001` … `ANC-004` (Off / NC / Adaptive /
   TRN — the mapping of `CAP-073-EVENT-NOTES.md`), `TOUCH-007` (press-and-hold), `BATT-004`, `FW-003`, `AUDIO-002` (playback) — re-derive the list from what
   the run actually did; "expected but not observed" for each that was not.
10. **Privacy:** the recording's audio track carries the maintainer's voice (the cue words) and the phone's audio; the screen may show the Wi-Fi name, phone
    numbers, notifications, other device names, the serial numbers (allowed on the Info tab, never written unredacted in any text — first 4 + last 2,
    ADR-058 item 5). The system log carries the account e-mail and the SSID (`CAP-072` §0's extract command removes them). The user with Play services may
    expose more identifiers (Google account names, Play's device names) — grep for them before anything is committed.
11. **External validation** where a claim rests on a specification or platform behaviour: the Android `BluetoothSocket`/`BluetoothAdapter.listenUsingRfcomm…`
    documentation (one RFCOMM connection per channel), the AOSP Bluetooth stack's RFCOMM port code for the "already at opened state" path (quote the file and
    the line, with the date read), the Fast Pair Message Stream page (one RFCOMM connection per Seeker; `Notify ANC state`), the Bluetooth Core Specification
    for RFCOMM `DISC`/`UA`/`DM`. Fetch, quote with the date.

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename, mode change or `git add`)

1. Create the RESULT (§0). Record `git log -1`, `git status --short`, `git fetch`, `git log --oneline HEAD..origin/main`. `main` takes no direct push: create
   the branch `capture/cap-074` from `main` and work there. `android/domain/bin/` is build output: never stage it.
2. List the folder fresh (`ls -la`, `sha256sum`), diff it against §2's table and say every difference.
3. **Checkpoint 0 (`AskUserQuestion`, one question per decision, options with pros and cons, "(Recommended)" on one, the exact commands in the preview):**
   the Group letter (BJ, see §1 item 6); the folder rename to the recording's first/last phone times (`CAP-074-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BJ`, after
   Phase A's clock offset); `chmod 644` on the files with mode 750; a `sha256sum` file; the privacy treatment of the recording (blur ranges and whether the
   audio track stays — it carries only the maintainer's cue words, by their statement) and of the system log (the filtered extract as in `CAP-072-FINDINGS.md`
   §0, the full log kept off the repository, the originals to `~/opencontrol-capture-originals/CAP-074/`); whether the phone can be attached for the APK
   pull and `am get-current-user`; which user id the Play-services user is.
4. Write the **Group BJ section** of `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (after Group BI: purpose, the maintainer's ad-hoc steps as run — there was no skeleton
   — and the files) and update the `CAP-074` row of `id_registry.csv` (it exists with status `planned`, written with this prompt; `analyzed` in Phase D).

### Phase A — the screen recording, in full

5. Decode the recording at 1 frame per second (316 frames) into the scratchpad in ranges; compare each second with the previous one; view **every** second
   with a change on labelled contact sheets; narrow transitions at 10 fps where a claim depends on them (the mode buttons, the (i) line, the connection
   state, the touch indicator). No sampling: every second is either viewed or proved unchanged.
6. The audio track: you cannot listen. Extract it (`ffmpeg -vn`), derive the speech segments with `silencedetect`/`astats` (RMS per 100 ms) and tabulate
   every voiced segment with its film time; align them with the maintainer's statement ("I spoke only to say left/right press-and-hold") and with the wire's
   `Notify`s. Label the words as the maintainer's statement, the timing as measured. Detect music playback the same way (continuous energy) — label it.
7. Measure the clock offset (status-bar minute changes, a Connect with a matching export line) and write it with its uncertainty.
8. Write the timeline into CAP-074-EVENT-NOTES.md: phone time · what happened · Test-ID · evidence — in `CAP-071`/`CAP-073`'s layout (Log Metadata, the
   capture-integrity pre-flight, the review method and privacy, the Event Timeline incl. "before the recording" and "after the recording", the maintainer's
   steps as a numbered list BJ-1 … BJ-n derived from the film, Test-IDs per `AGENTS.md` §13 step 7 with "expected but not observed", the analysis checklist).
   There is no skeleton to append.

### Phase B — the HCI logs and the other logs, in full

9. Both HCI logs: Connection Complete events and the Buds' handle (by address; `bluetooth.addr` may match nothing — show the filter matching a known frame),
   the boundary between the two logs, the RFCOMM inventory per DLCI and direction, every pw_hdlc frame CRC-32-checked (`scripts/pwrpc_decode.py`'s
   `parse`/`describe`, with its positive control on `CAP-036`), every Message Stream message decoded (`[Group][Code][Len][Value]`), **the full claim
   inventory of DLCI 0x04/0x05 with the sender of every `SABM`/`UA`/`DISC`/`DM`** (§3 item 1), the Play-services marker frames, AVDTP/AVRCP state. Commands
   and exit statuses in FINDINGS (`PROJECT_RULES.md` rule 4a; `AGENTS.md` §13 step 8 for every negative).
10. The debug export and the app logcat: every line read (692 + 351). The system log: every line in the run's and the pre-run's window read or filtered with
    a stated, positive-controlled command (§3 item 2); crashes, ANRs, `StrictMode`, `am_wtf`, the Bluetooth stack's RFCOMM lines with the uid of each
    connect, Play services' Fast Pair lines, the SIGQUITs, the user id of the app's processes.

### Phase C — correlation, the root cause, the comparison with CAP-073

11. Correlate every timeline row with its frames and log lines. Write CAP-074-FINDINGS.md in `CAP-071`/`CAP-073`'s structure: §0 build, install, user,
    logs, privacy; §1 ACLs and the Message Stream claims — the inventory with **both** clients; §2 the Connect reads (the thirteen requests against
    `CAP-073`, byte for byte); §3 every write; **§4 the holds, the closes and every `Notify` — which client received it and what the screen showed**;
    §5 playback; §6 session ends and re-opens (the Disconnect/Connect series); **§7 `CAP-074` versus `CAP-073` — a table per aspect (user, Play marker, claim
    owner over time, hold lifetime and end reason, `Notify`s received by the app / by Play / by nobody, modes shown on screen, the first-hold-only pattern)
    and the conclusion it supports**; §8 the root cause with its FACT/HYPOTHESIS label and the settling experiment for whatever stays 🟡; §9 the fix options
    for the maintainer (each with pros, cons, the wire cost, what ADR-032/ADR-061 would have to say — e.g. re-claim after a foreign close while the tab is
    shown with a back-off; a "channel taken by Play services — tap to refresh" hint; a re-claim on the next `Notify`-less interval; accept as a documented
    limit of the Play user; a one-shot re-claim per close) — **proposals, not decisions**; §10 open questions. FACT / HYPOTHESIS / ASSUMPTION / OPEN QUESTION on
    every conclusion (`PROJECT_RULES.md` §1); every claim with a frame, a log line or a film time.
12. Validate externally (§3 item 11): fetch, quote with the date.

### Phase D — checkpoint, documentation, finish

13. **Checkpoint (`AskUserQuestion`):** the root-cause label (FACT or HYPOTHESIS with the experiment); every proposed `PROTOCOL.md` note or status change,
    every proposed ADR-032/ADR-061 Update (drafts in the preview — `AGENTS.md` §6: nothing promoted or written without the maintainer's answer in this chat);
    the fix option the maintainer wants pursued (⇒ a FEATURE prompt, drafted by this session as `ai-sessions/0087_FEATURE_PROMPT_…` only if asked) or the
    limit accepted; whether 1.2.0 gets a known-issue line (`CHANGELOG.md`, `README.md`, the release notes — the maintainer's call, `RELEASING.md` C4's "minor
    ⇒ known issue"); the privacy treatment. Record the answers verbatim in the RESULT.
14. With the answers: `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 (the `CAP-074` row: analyzed) and the Group BJ status, `id_registry.csv`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
    (the Group column for the Test-IDs exercised), `TODO.md` §2 (the hold's "a loss … while the tab is shown" item becomes this finding; the 🔴 on Play's
    re-claims gets `CAP-074`'s numbers; "Next app run"), `ARCHITECTURE.md` §5a (the "Changed by the Buds" row: say in which user it is verified and what
    `CAP-074` showed) and §6.0b (a sentence on the observed contention, as approved), `APP_TESTPLAN.md` (a note that §W ran in the user without Play, if
    approved), `PROJECT.md`/`README.md` only as approved. `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and `python3 scripts/lint_docs.py`
    (exit 0).
15. The RESULT: plain-language summary first (what happened on the phone, why, what the maintainer decides next), the phases, the files read in full / in
    part, "Deferred documentation" (each also in `TODO.md`), "Commits"; the `ai-sessions/INDEX.md` row.
16. Show a short summary and **ask whether to commit and push**. Only after a yes: commits per concern (Conventional Commits: `docs(capture)`, `docs`), each
    with its *why* and the attribution line; Git LFS for the capture files (`.gitattributes` covers `captures/**` `.log`, `.log.last`, `.mp4`, `.txt`);
    `git fetch` and rebase; push without force; open the pull request only when asked, and never merge it yourself.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Scope:** analysis and documentation only. No change under `android/`, no new skeleton, no release step, no `dist/` change, no FEATURE work in this session.
- **Approvals only in this chat** (`AGENTS.md` §6): no 🟢 FACT promotion, no new ADR or ADR Update without the maintainer's answer here; ADR-060 stays a draft.
  Fix options are proposals with pros and cons; the choice is the maintainer's.
- **Evidence:** every claim cites a frame (log and number), a log line or a film time; a negative needs its command, its exit status and a positive control;
  a hypothesis names its settling experiment. Play services' side is read from the wire and the system log only — never from assumptions about its code.
- **No sampling:** every second of the recording, every packet of the current HCI log (and the `.last` log's boundary), every line of the export and the app
  logcat, and the system log in the run's and the pre-run's window — reviewed or covered by a stated, positive-controlled filter.
- **Privacy:** no serial unredacted, no MAC, no phone number, no account name or e-mail, no Wi-Fi name in any committed text; the full system log stays local.
- **Files:** before moving, renaming or deleting anything, diff a fresh listing; never wildcard deletes; ask first.
- **Subagents** may only read and report; every write is done and verified in the main session.

## 6. Deliverables

- CAP-074-EVENT-NOTES.md and CAP-074-FINDINGS.md (with the `CAP-073` comparison and the fix options), with the folder and files as approved in Checkpoint 0.
- The Group BJ section, the registry, index, test-plan, `TODO.md`, architecture and status updates as approved.
- ai-sessions/0086_CAPTURE_RESULT_2026_10_12.md and the `ai-sessions/INDEX.md` row.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0086_CAPTURE_PROMPT_2026_10_12.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0086_CAPTURE_PROMPT_2026_10_12
