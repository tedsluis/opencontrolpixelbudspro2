# 0080_CAPTURE_PROMPT_2026_10_08.md — Full analysis of CAP-071 (Group BG: the 1.1.1 release APK installed over 1.1.0 in the GrapheneOS profile without Google Play services — the regression run of the toolchain release)

**Number:** 0080
**Category:** CAPTURE
**Date:** 2026-10-08
**Title:** Fully analyse `CAP-071`: **one** film with sound, **four** HCI snoop logs, **two** app debug exports, **one** app logcat and **one** full system log
(`logcat -b all`). It was recorded while the release-signed **1.1.0** APK (build `0323849`) was updated **in place** to the release-signed **1.1.1** APK (build
`86a6fb3`), in a GrapheneOS secondary user without Google Play, following the Group BG skeleton in `CAP-071-EVENT-NOTES.md`.
- Record the real events in **CAP-071-EVENT-NOTES.md** and the analysis in **CAP-071-FINDINGS.md**.
- Establish what was actually done, and give the release verdict for 1.1.1 (`RELEASING.md`, Release checklist step C4).
- **No change to the app itself**, **no new capture skeleton**, **no publishing step**.

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this repository. Run the
phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action, read these in full and in this order, to understand
the ground rules before auditing anything:
- `AGENTS.md` and `PROJECT_RULES.md`.
- `PROJECT.md`, including the Definition of done with its evidence table and "Status after 1.0.x".
- `ARCHITECTURE.md`, in particular §2.4, §3.1 (the "one rule for current" bullet and the DLCI 0x02 settings row), §3.2 (timing constants), §5a, §6.0a, §6.0b, §7,
  §8.1, §9, §12 and §13.
- `PROTOCOL.md`, in particular:
  - §2.2a with all its Updates (the announced channel and lead L-1), §2.3;
  - §4.1, §4.2, §4.3 Option B/F;
  - §4.5's preamble, §4.5.2 Multipoint, §4.5.4 head gestures, §4.5.6 Volume EQ, §4.5.7 balance, §4.5.8 Case sounds;
  - §5 and §6.
- `DECISIONS.md`: **every** ADR, ADR-001 … ADR-057, with every dated Update. In particular ADR-010, ADR-029 (with its 2026-10-07 Update: compileSdk 37), ADR-034,
  ADR-036, ADR-037, ADR-042, ADR-044, ADR-045 with its Updates, ADR-046 … ADR-049, ADR-052 … ADR-057.
- `TODO.md`.

These are the ground rules; do not audit or change anything before they are read. Then, per task:

- **Procedure and registers:** `AI_SESSION_LOG_PROCEDURE.md` (in full, incl. §4b), `ai-sessions/INDEX.md`, and `id_registry.csv`: the `CAP-068` … `CAP-071` rows
  and every Test-ID the skeleton names.
- **What the build under test is — read in full:**
  - the 0078 and 0079 session files:
    - `ai-sessions/0078_MAINTENANCE_PROMPT_2026_10_07.md` and `ai-sessions/0078_MAINTENANCE_RESULT_2026_10_07.md` — the toolchain upgrade; the claim that the
      app's behaviour does not change, with its APK comparison, permissions and components, M1–M7;
    - `ai-sessions/0079_MAINTENANCE_PROMPT_2026_10_08.md` and `ai-sessions/0079_MAINTENANCE_RESULT_2026_10_08.md` — §A.3 the reference-byte table, §A.4 the
      `TODO.md` §2 items planned in this run, the checkpoint answers;
  - `CHANGELOG.md` `[1.1.1] - not yet released`, `README.md`;
  - `RELEASING.md`: the **Release checklist** A–E, "Repository rules", §4–§8 and §13;
  - `APP_TESTPLAN.md` in full. Section U (U1–U14) is the 1.1.1 build; C10, C12, O2, O3, S12 and T11 are taken into this run;
  - pull request #15 (`gh pr view 15`: the checklist, B1 = `86a6fb3`), and the commits of branch `release/1.1.1` (`git log --stat origin/main..origin/release/1.1.1`).
- **The skeleton:** `captures/CAP-071-2026-10-08_17-51-29_18-27-28-Group_BG/CAP-071-EVENT-NOTES.md` (the placeholder folder `CAP-071-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BG`, renamed in this session).
  - Read it **as committed** (`git show HEAD:<path>`): purposes, the reference-byte table, P0–P9 with the ★ items, BG-1 … BG-24 and BG-end1 in five parts, "Don'ts",
    "After the run", and the analysis checklist with its "Refuted if" column.
  - Read the **working-tree version** too. The maintainer added the P0/P1 command outputs after the run; record them as the maintainer's evidence (§2).
- **Logging and capture method:** `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3, §5, §8, §9 and the Group BG section.
  - Read `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` for every Test-ID the skeleton names: `ANC-001` … `ANC-004`, `AUDIO-001` … `AUDIO-003`, `BATT-004`, `CASE-001`,
    `CASE-002`, `CASE-004`, `CASE-005`, `CONV-001`, `EQP-001`, `EQS-001`, `FIND-001`, `FIND-002`, `HEAD-001`, `HOLD-005`, `INEAR-004`, `INEAR-005`, `MULTI-001`,
    `PAIR-003`. Re-derive this list from the skeleton yourself.
- **The layout to follow — read in full:**
  - `captures/CAP-049-2026-09-12_18-13-54_18-21-49-Group_AF/CAP-049-EVENT-NOTES.md` and `…/CAP-049-FINDINGS.md` — the maintainer's named examples;
  - `captures/CAP-070-2026-10-07_06-10-41_06-40-59-Group_BF/CAP-070-EVENT-NOTES.md` and `…/CAP-070-FINDINGS.md` — the previous release run in the same user;
    the comparison baseline for every screen and every frame. It also has the clock-offset method, the file-overlap proof, the session-end table, the
    Play-services marker and the release verdict;
  - `captures/CAP-068-2026-10-04_07-22-57_07-54-10-Group_BD/CAP-068-FINDINGS.md`.
- **The reference captures of the skeleton's byte table:** for the expected Buds behaviour, the FINDINGS of `CAP-024`, `CAP-041`, `CAP-058`, `CAP-059`, `CAP-062`,
  `CAP-064`, `CAP-065`, `CAP-066`, `CAP-069` and `CAP-070`, at the frames the table cites.
- **Every Kotlin source under `android/` that a finding touches, in full,** before you write about it. At least:
  - `SettingFrame.kt`, `Maestro.kt`, `CodecRouter.kt`, `PwRpc.kt`;
  - `BudsRepositoryImpl.kt`, `SessionReopener.kt`, `SessionDiagnostics.kt`, `RfcommBudsTransport.kt`;
  - `BudsSettings.kt`, `ValueCurrency.kt`;
  - `SettingsUi.kt`, `ControlsScreen.kt`, `EqScreen.kt`, `ConnectionScreen.kt`, `SettingsMenu.kt`, `OpenControlNavHost.kt`;
  - `MainActivity.kt`, `AppUiSession.kt`, `AncTileService.kt`;
  - `android/app/build.gradle.kts`.

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.**
- Create **ai-sessions/0080_CAPTURE_RESULT_2026_10_08.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block.
- Update that block at the end of every phase **and** after every substantial step within a phase: every 5 minutes of film reviewed, every HCI log and channel
  decoded, every log file read, every 20,000 lines of the system log, every checkpoint answer. Say:
  - what is done and what is next;
  - which files are touched but not yet verified;
  - where the intermediate results live: the scratchpad directory, with its scripts, contact sheets, decoded tables and notes. Re-create them if the scratchpad
    is gone.
- A resumed session reads this prompt, then the RESULT's Progress block, re-reads the files the unfinished step touched, and continues from there.
- It never redoes a finished, recorded step, and never assumes an unrecorded step was done.
- The prompt keeps its number and date; the RESULT keeps growing in the same file.

**Memory and the machine (two OOM kills so far, `ai-sessions/0057` and `0074`).** The laptop has 15 GB.
- Run `tshark`/`ffmpeg` work one job at a time. Read the 33 MB system log with `grep`/`sed`/`awk` in ranges, never whole into one tool output.
- Never run two Gradle builds at once; no build is needed in this session anyway.
- Never `git worktree add` a commit: a worktree checks out `captures/` (LFS, gigabytes).
- Write intermediate results to the scratchpad and update the Progress block **before** a long job.

**Nothing is taken on trust, including this prompt, the skeleton, earlier RESULTs and the maintainer's statements.** Every claim, and every number measured
below (§2), is re-derived from the film, the HCI logs, the other logs, the code and official documentation.

---

## 1. What the maintainer asked for (chat, 2026-10-08, translated from Dutch)

1. **Analyse `CAP-071` (Group BG) of the OpenControl app extensively and record the findings.** First read the core files (`AGENTS.md`, `PROJECT_RULES.md`,
   `PROTOCOL.md`, `ARCHITECTURE.md`, `DECISIONS.md`) to understand the ground rules before auditing the rest. Run everything in phases, strictly in order; when a
   token limit is hit, continue automatically later (§0).
   - Analyse the film `CAP-071-recording.mp4` first, and record every event and action with its time in **CAP-071-EVENT-NOTES.md**.
   - Analyse **all** `CAP-071-btsnoop_hci*` logs with `tshark`.
   - Analyse the `CAP-071-opencontrol-debug-20261008-*.txt` exports and the `CAP-071-OpenControl-for-Pixel-Buds-Pro-2-log-*.txt` logcat.
   - Then correlate the events of `CAP-071-EVENT-NOTES.md` with the HCI logs, CAP-071-logcat-all.txt (kept locally since this session; committed as the extract `CAP-071-logcat-extract.txt`), the exports and the app logcat;
     `CAP-049-EVENT-NOTES.md` is the example.
   - Run an extensive analysis and record the findings in **CAP-071-FINDINGS.md**; `CAP-049-FINDINGS.md` is the example.
   - Possibly relevant: `ai-sessions/0079_MAINTENANCE_RESULT_2026_10_08.md`, `ai-sessions/INDEX.md`, `APP_TESTPLAN.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`,
     `CAP-071-EVENT-NOTES.md`, `README.md`, `TODO.md`.
2. **Not every step was done exactly as in the test plan, and some extra things were shown on the film.** So **establish from the film and the logs what was
   actually done**, in the order it was done; never fill the timeline from the skeleton.
   - Map each real action to the skeleton's step IDs (P0–P9, BG-1 … BG-24, BG-end1), to `APP_TESTPLAN.md` section U (U1 … U14), and to the other test-plan IDs the
     skeleton names (C8, C9, C10, C12, F1–F4, G3, H2–H6, I1–I3, J4, K1, K2, K4, M2–M6, N2–N11, O2–O8, O13, Q2, R1–R4, R6, S6, S12, T1, T3–T8, T11).
   - Mark each step **done**, **done differently**, **repeated** (each repetition its own row), **skipped** or **not identifiable**.
   - List every extra action as its own row ("not in the plan") and say what it demonstrates.
3. **Facts the maintainer gave.** Record them as the maintainer's statements, and check each against the evidence:
   - **At the start of the film the app was 1.1.0, build `0323849`; during the film the maintainer upgraded it to 1.1.1, build `86a6fb3`.**
     - Read the Info tab on film before and after the update: version, `build <hash>[-dirty]`, commit date, "Control channel: N". "-dirty" must not be shown.
     - Check that `86a6fb3` is the tip of `origin/release/1.1.1` at the start of this session, and that no file under `android/` changed after it
       (`git diff 86a6fb3..origin/release/1.1.1 -- android` empty).
     - The app logcat's header says `package: io.github.tedsluis.opencontrolpixelbuds:10101` (re-check).
     - The system log holds the update itself; find and quote each line:
       - the `dex2oat` line with `app-version-name:1.1.1,app-version-code:10101` (≈ 17:53:02);
       - `Killing 26327 … killDueToPackageUpdate` (≈ 17:53:12);
       - `installation completed` (≈ 17:53:27).
     - Check `dist/1.1.1/` **read only**: `sha256sum -c`; expected APK `062f35b3b2d177ed7dbccd336742dffce59b194dccf6c4febcb7dc6def98f1d0`; `apksigner verify
       --print-certs`, expected certificate `a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb`. Never rebuild, never run `scripts/release.sh`,
       never write into `dist/`.
     - If the phone is attached and the maintainer agrees, `adb shell pm path` plus `adb pull` of the installed `base.apk` into the scratchpad and its SHA-256
       against `062f35b3…f1d0` proves the installed APK is the built one. Ask first; otherwise say it is not proved.
   - **The capture ran on a Pixel 9a with GrapheneOS, Android 17.**
     - Verify it from the logcat header (`osVersion: google/tegu/tegu:17/CP3A.260905.009/2026100201`, `userType: full.secondary`), from the system log, and from
       the P0 output in the notes.
     - Interpret the P0 output exactly: `grep -i -E "gms|vending"` matched only `app.grapheneos.gmscompat*`, exit 0 (GrapheneOS's own compatibility layer — check
       this against GrapheneOS documentation); no `com.google.android.gms` and no `com.android.vending`; the positive control `grep opencontrol` matched, exit 0.
     - Above all, use the HCI logs: there must be **no Message Stream claim whose first phone message is `03 08 00 02 01 25`** (the Play-services marker of
       `CAP-066-FINDINGS.md`). Give the command, its exit status, and a positive control in `CAP-066`.
   - **The maintainer did not speak during the film, but says everything worked well, and the ANC modes were right — also during playback.**
     - Record this as the maintainer's observation. The skeleton asked for spoken observations (BG-5, BG-11, BG-13, BG-14 and every bud change); there are none.
     - The film's audio track is the only other source. Say what it holds (music, the ANC change, the Buds' sounds, the ring of BG-16) and use it only for what it
       actually carries.
     - What the wire shows (each ANC `Set` with its ACK and the Buds' `Notify`) is the evidence; "it sounded right" is the maintainer's attestation.
4. **Strict execution rules (the maintainer's own words, translated):**
   - **No assumptions.** Verify everything.
   - **Validate externally.** Use search tools to validate claims against official documentation where needed: Android Developer docs / AOSP sources, the
     Bluetooth Core Specification, the Fast Pair specification, GrapheneOS documentation. Cite the URL and the exact sentence.
     - developer.android.com pages have returned only navigation to the fetch tool before.
     - AOSP sources at `android.googlesource.com/…?format=TEXT` read with `curl … | base64 -d`, and the raw Fast Pair pages via `curl`, worked.
     - Check any fetch-tool summary against the raw text before quoting it.
   - **No sampling.** No random spot checks: a 100 % complete and exhaustive review of the relevant files, checking every detail:
     - every second of the film;
     - every packet of all four HCI logs that belongs to the Buds;
     - every line of both exports and of the app logcat;
     - every line of the system log that belongs to the app, the Bluetooth stack, the package manager, the activity manager or the window manager in the film's
       time range, plus whatever a lead points to.
   - **Best practices.** Industry-standard technical review methods:
     - **evidence traceability:** every claim links back to a specific frame number with its HCI file, a log line with its file and line number, or a film
       timestamp;
     - **structural integrity:** check the files, the registry, the links and the footers.

---

## 2. The evidence

`captures/CAP-071-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BG/`:
- `CAP-071-EVENT-NOTES.md` is committed (the skeleton) and **modified** in the working tree (the maintainer's P0/P1 outputs).
- The other nine files are **untracked**.
- `id_registry.csv` has a `planned` row for `CAP-071`.

Measured by the chat that wrote this prompt (2026-10-08). **Re-check each value:**

| File | Measured / what to check first |
|---|---|
| **CAP-071-recording.mp4** | 2,158.67 s (≈ 35 min 59 s), 1,686,259,198 bytes, H.264 1280×720 at ≈ 29.87 fps, **AAC 44.1 kHz stereo** (an audio track exists, unlike `CAP-070`'s). `creation_time` 2026-10-08T16:27:28Z = 18:27:28 local, probably the film's *end*; measure the film's own clock. **Only one video file exists.** The skeleton asked for two camera films (BG-end1 ends film 1) plus Android's screen recording (P4). Establish whether this is a camera film, a screen recording, or a stitched file. Then find every missing part, and what covers BG-20 … BG-24 |
| **CAP-071-btsnoop_hci1.log.last** | 854 packets, 17:37:32.758 – 17:51:34.615. **Before the film?** It covers the 1.1.0 install at ≈ 17:40:13 and the 1.1.0 process started at 17:41:42 (PID 26327); that is, unfilmed 1.1.0 activity (P9: "no rehearsal") |
| **CAP-071-btsnoop_hci1.log** | 6,894 packets, 17:51:39.365 – 18:25:35.689 |
| **CAP-071-btsnoop_hci2.log.last** | 6,996 packets, **17:51:39.365** – 18:26:23.881. `cmp -n <size of hci1.log>` exits 0: **`CAP-071-btsnoop_hci1.log` is a byte-for-byte prefix of `CAP-071-btsnoop_hci2.log.last`** (the same snoop session pulled twice, as in `CAP-068`/`CAP-070`). Prove it packet by packet; count every frame once |
| **CAP-071-btsnoop_hci2.log** | 760 packets, 18:26:32.311 – 18:28:44.260 |
| All four HCI logs | `Bluetooth H4 with linux header`. There are two boundaries: ≈ 17:51:34 → 17:51:39 (P2's Bluetooth off/on?) and ≈ 18:26:23 → 18:26:32 (BG-19's or BG-23's Bluetooth off/on?). Establish what caused each, the gaps, and that no Buds traffic is lost or counted twice |
| **CAP-071-opencontrol-debug-20261008-182336.txt** | 881 lines, 17:53:29.631 – 18:23:28.787; 398 `DLCI 0x` lines |
| **…-182647.txt** | 1,012 lines, 17:53:29.631 – 18:26:39.862; 444 `DLCI 0x` lines |
| Both exports | **Only two exports.** The skeleton asked for one at BG-19, BG-end1, BG-22, BG-23 and BG-24. Map each export to a step. Both start at the same line, so they come from one process, but "Permissions (start)" / "Android link observer started" recur at 18:03:30, 18:03:38, 18:03:58, 18:04:05 … and at 18:26:39. Establish for each whether it is an Activity re-creation (rotation BG-8, a theme change BG-3/BG-9, the browser BG-10) or something else, from the system log's `wm_on_*` and `am_*` lines. Check the Debug-mode state per stretch (P1, BG-2). Check line by line which export contains which; explain the gap between each export's last line and its save time |
| **CAP-071-OpenControl-for-Pixel-Buds-Pro-2-log-bed3be1a5dc7.txt** | the app logcat: 671 lines, saved ≈ 18:27; header `userType: full.secondary`, `package: …:10101, targetSdk 34`; buffers `main,system,crash,events,kernel`. Its timestamps are `10-08 15:41:42.564 …` – `16:27:41.058`: **UTC, local − 2 h** (measure it). PIDs 26327 (the 1.1.0 process, from 17:41:42 local) and 28680 (1.1.1). Re-check for `FATAL`, `ANR in`, `tombstoned`, "Decoder fault" and "Unexpected error" (none found by `grep`). Re-check which PIDs appear at all; is there no process after a force-stop (BG-23)? |
| **CAP-071-logcat-all.txt** (new for this project: P6) | `adb logcat -b all -v threadtime`, 221,518 lines, 33.5 MB, **local time**, from `10-04 08:43:20` (kernel buffer) to `10-08 18:28:13.786`. Leads in it (verify each): `installer_clear_app_data_caller` for the app at 14:31:33, 17:40:06 and 17:53:02 — **does the update clear data or only caches?** Check against AOSP, and against BG-1/BG-2: were Dark mode On and Debug mode on kept? Installs completed at 17:40:13 and 17:53:27, with `Force stopping … user=10` / `user=0: pkg removed`. **The app runs in user 0 as well**: the `AncTileService` was bound there at 15:46:20 (PID 15436) and 17:43:33 (PID 26493, `u0a353`), and 26493 was killed by the 1.1.1 install. Which user's tile did the maintainer tap in BG-12, and which process answered? No `Force stopping … ` for `am force-stop` after 17:53:27 was found yet (BG-23) — re-check. Also the Bluetooth stack's own lines (`bt_btif`, `BluetoothAdapterService`, `bt_stack`) at every off/on and every ACL change; and what the `CAP-070` question asked: what sent the `SIGQUIT` / `tombstoned` there (look for the same here) |
| **CAP-071-EVENT-NOTES.md** | The skeleton, plus the maintainer's P0 output (user 10; three `app.grapheneos.gmscompat*` packages; exit 0; `grep opencontrol` exit 0) and P1 output before and after `adb install --user 10 -r dist/1.1.1/opencontrol-pixelbudspro2-1.1.1.apk` ("Success"). **`firstInstallTime=2026-10-08 17:40:13`** in user 10, both before and after the update. 1.1.0 was therefore installed into user 10 thirteen minutes before the update; establish from the system log what happened at 17:40 (a reinstall of 1.1.0? into which users?) and say what that means for U1's "installed over 1.1.0". The update kept the user-10 data inodes (`ceDataInode=343627 deDataInode=468510` before and after); check that this proves the data directory was kept. The second "User 10:" block (the package-visibility list) is shorter after the update; establish whether that matters. The install was from `dist/1.1.1/`, not from the kept copy: B3 (`~/opencontrol-1.1.1-tested`) **did not exist** on 2026-10-08 after the run (measured). Record it, and tell the maintainer B3 is still to do before D3. Rewrite the notes into the record; keep the planned procedure as an unchanged appendix |
| Absent | **No `uiautomator` dumps** (BG-23 asked for them: the screen-reader text, T11). **No second film**, and no separate screen recording (P4), unless the one file is one. **No spoken observations.** Record each absence, what other evidence covers it, and what stays open |

- **File modes:** six files are `-rw-r--r--`; four are `-rwxr-----` (both exports, the app logcat, the film). No capture file may have the executable bit (fix it
  in Phase 0, with approval).
- **LFS:** `git check-attr filter` gives `lfs` for the nine data files (including CAP-071-logcat-all.txt (kept locally since this session; committed as the extract `CAP-071-logcat-extract.txt`)) and `unspecified` for the notes (measured; re-check).

**Known pitfalls (from `CAP-063` … `CAP-070` — check them, do not assume):**
- With the `H4 with linux header` encapsulation `bluetooth.addr` is empty. Scope every filter by the Buds' connection handle(s), taken from the Connection Complete
  events **of each file** (`AGENTS.md` §13.1).
  - Handles restart after Bluetooth off/on.
  - Show each filter matching a known frame.
  - Other devices may share the log.
- **RFCOMM DLCIs are session-local:** MAESTRO is 0x02 or 0x03, the Message Stream 0x04 or 0x05.
  - `python3 scripts/pwrpc_decode.py <log>` (`--handle 0x…` per connection) reads DLCI 2 and 3 and reassembles packets split over RFCOMM frames.
  - Still check the CRC-32 of each pw_hdlc frame yourself where a claim depends on it (the method of `ai-sessions/0074` §A.1).
  - `scripts/message_stream_tally.py` tallies ANC `Set`/ACK/NAK.
- **The Message Stream claims:** the app starts its claim with `08 11`/`08 12`, or with its battery/Find sequence. Play services' claim starts with
  `03 08 00 02 01 25`; none is expected.
- A filter on `data.data` does not see frames a dissector has claimed (HFP, AVDTP). Use `frame contains` or the protocol's own field (`AGENTS.md` §13 step 8).
  - AVRCP is `btavctp or btavrcp`.
  - A `tshark` exit status ≠ 0 is an error, not "0 frames". Sets need commas: `frame.number in {a,b}`.
- `field 17` is `sint32` (zigzag); `field 16` holds five little-endian floats; **field 29 is 1 = off / 2 = on**, never 0/1 (ADR-052).
- The app logcat is in UTC, and the system log and the exports are in local time (CEST, UTC+2). Never mix them without the measured offset.

**Privacy (standing rule, ADR-037).** Check **every** frame of the film, and the audio track, for personal data:
- notifications, messages, contact names, e-mail addresses;
- Wi-Fi and Bluetooth device names; Android's settings and pairing screens;
- **a burned-in street-address overlay** (earlier camera films carried one);
- faces other than what the maintainer chose to film (head and ears);
- voices or conversation in the background;
- the browser pages of BG-10 (an account name, open tabs).

The system log also holds the whole phone's activity since 2026-10-04 (other apps, other users, possibly account names, Wi-Fi SSIDs, locations, addresses). It is
**untracked and goes to a public repository through LFS if committed**. Inventory what personal data it holds, and propose either committing it whole, committing
only an extract (the app's, Bluetooth's and the package/activity manager's lines in the film's range, with the exact command), or not committing it. **Ask**:
earlier choices do not carry over. If a blur or a cut of the film is wanted, propose the exact `ffmpeg` command, the ranges and a before/after sample. Never write
the Buds' full address or serial into a document (ADR-010).

---

## 3. Leads and context — verify each before relying on it

1. **The update (P1, BG-1, BG-2, U1, U2):**
   - 1.1.0 → 1.1.1 in place, with no uninstall, on film. Did 1.1.0 show "1.1.0, build 0323849" before the update, and 1.1.1 "build 86a6fb3" without "-dirty" after
     it?
   - Were Dark mode On and Debug mode on kept (the U1 test)?
   - Were the permissions and the Companion Device association kept? The first export line says "Permissions (start): Bluetooth NOT_REQUESTED -> GRANTED"; what
     does that mean (`MainActivity.kt`)?
   - Did the app open its session by itself (ADR-044)? Was anything opened between the install and BG-1 (P9; the 17:51:08 and 17:52:03 launcher `START u10`
     lines are **before** the update — what was filmed then)?
2. **No behaviour change from the toolchain (the 0078 claim, the core of this run):** compare the 1.1.1 app with 1.1.0 (`CAP-070`) for:
   - the Connect-time reads: per connection, every `ReadSetting` in the order 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29, each with its answer and latency;
   - the request bytes of every write against the skeleton's reference-byte table (byte-identical or not; real / derived / first capture);
   - the `GetSoftwareInfo` and `SubscribeRuntimeInfo` sequences, and the Message Stream claims;
   - the timing (ADR-044's guard, the reopen delays);
   - the screens on film (cards, texts, order, colours, the Material 3 widgets that 0078 touched: `TopAppBar`, `PullToRefresh`, `TabRow` and the menu's tab row,
     `LocalResources`).

   Any difference is a central finding: say whether the toolchain explains it.
3. **The screens (BG-3 … BG-10, U3 … U9, C10, O2, O3):**
   - "—" for ≈ 1 s after the start (U3, S6);
   - the swipe between tabs, pull to refresh with its spinner, and system back (the reworded comment in `OpenControlNavHost.kt` cites `CAP-065`/`CAP-067`/`CAP-070`
     — does this run confirm it?);
   - the (i) dialogs, the menu's tab row and its arrow;
   - rotation keeping the tab (K4);
   - Dark mode System, following Android's dark theme without a restart (BG-9);
   - the licence text and the two GitHub links. **The app has no `INTERNET` permission**: the browser opens, not the app. Verify there is no network call from the
     app's UID in the system log.
4. **Every write, worn (BG-11 … BG-16, U10 … U14):**
   - ANC four modes, the tile and the notification;
   - Balanced (restores the Buds, `TODO.md` §4), a band, *Read EQ again* (H5), balance, mono, conversation detection, Volume EQ, case sounds;
   - touch controls, press and hold, the mode list without Off (restores the Buds), in-ear detection, head gestures, Multipoint;
   - the read-back after Disconnect/Connect (BG-15);
   - Find: ring Left/Right and Stop.

   Per write: the request bytes, the empty `RESPONSE` or the ACK, the control moving only after it, and the (i) line. Did the Buds end in the state `TODO.md` §4
   asked for (Balanced, mode list without Off)?
5. **The ANC modes during playback** (the maintainer's statement): each `Set` with its ACK and the Buds' `Notify` while A2DP was streaming (AVDTP start/suspend,
   AVRCP play state). What the audio track shows.
6. **Robustness (BG-17 … BG-19, C8, C9, J4, K1, K2, R6):**
   - both buds into the case and out again: the cause text, and ready again by itself;
   - Home for 2 minutes;
   - the export, then Bluetooth off/on: "Bluetooth is disabled." and ready again.

   For each: the session-end table as in `CAP-070-FINDINGS.md`, the cause shown and logged, and whether ADR-044's rules held (one attempt per event, the 10-s
   guard).
7. **The channel choice (BG-20, lead L-1, `INEAR-005`):**
   - only the Left out ⇒ 19, only the Right out ⇒ 21 (`PROTOCOL.md` §2.2a 🟢, `CAP-065` 7/7). Bring more samples, or a counter-example;
   - with both worn on 19, the **Left** out of the ear ⇒ Buds `DISC` + 21 (🟡 predicted)?
   - the head side on film for every wear step (head on the **right** of the frame = **Left** bud), cross-checked with the runtime-info per-bud fields (6.2 Left /
     6.3 Right) and the Settable byte.
8. **A write during a re-open (BG-21, C12):** the text "The setting was not changed: The app's channel is being reopened — try again in a moment." on film, and
   **no `WriteSetting`** afterwards (nothing queued). This was not achieved in `CAP-070`.
9. **The export during rotation (BG-22, S12)** and **the screen-reader text (BG-23, T11):** with no `uiautomator` dump, say what is and is not established. It stays
   an open item, not a guess.
10. **The 1.1.1 known issues** in `CHANGELOG.md` and the release notes: does this run remove any, keep each, or add one?
11. **Battery and case:** every value shown on film against the wire value and its receive time (`03 03`, runtime-info 6.1/6.2/6.3), and the "last connection"
    marks.
12. **Anything new or wrong:**
    - NAKs, pw_rpc error statuses;
    - Safe Mode (expected off on `release_5.203`; re-check the firmware string);
    - "Late WriteSetting answer dropped" / "Maestro request held" lines (ADR-045 Update);
    - any app frame on DLCI 0x08/0x0a (there must be none);
    - any request outside the user's actions;
    - any `StrictMode`, `A resource failed to call close` (the app logcat's last line, PID 28680) or other warning from the app's UID. Find its source: app code,
      or a library of the new toolchain?
    - anything that behaves differently in a secondary user, or between the user-0 and user-10 installs.
13. **The extra things the maintainer showed on film:** identify each, and say what it demonstrates.

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename, mode change or `git add`)

1. Create the RESULT file (§0). Record:
   - `git log -1`, `git branch --show-current`, `git status --short`;
   - `git fetch && git log --oneline HEAD..origin/main` and `HEAD..origin/release/1.1.1`;
   - `gh pr view 15 --json state,headRefOid`.

   **Work on branch `release/1.1.1`** (`RELEASING.md`, Release checklist C3: the capture and its analysis are committed after the build commit on the release
   branch). Keep the maintainer's uncommitted change to the notes.

   Confirm the `planned` row of `CAP-071` in `id_registry.csv`, and the Group BG section and Capture Index row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`. Identify every
   file with `capinfos`, `ffprobe` and `sha256sum`, and re-check every value in §2.
2. Privacy check of the film, its audio and the system log (§2). Summarise the result.
3. Plan the migration (ADR-037, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9):
   - rename the folder to `captures/CAP-071-YYYY-MM-DD_HH-mm-ss_HH-mm-ss-Group_BG/`, from the film's first and last clock time (say which clock);
   - `chmod 644`;
   - the registry row and the Capture Index row;
   - Git LFS for every capture file;
   - what to do with the system log (§2).

   **Ask the maintainer (`AskUserQuestion`)** about the privacy outcome and the migration plan together, before moving anything.
4. Execute only what was approved.
   - Before moving anything, compare checksums of a fresh listing of the source against the destination. Prefer a plain `mv`. Never chain deletes, and never
     `rm -rf` based on an earlier listing.
   - Update every reference to the placeholder folder name: `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TODO.md`, `APP_TESTPLAN.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`,
     `README.md`, `RELEASING.md`, `_sidebar.md`; `grep -rn "CAP-071-yyyy"` outside `ai-sessions/`.
   - A reference under `android/` (e.g. a KDoc) is recorded for a follow-up session, not edited.
   - Earlier `ai-sessions/` files are history: leave them as written.

### Phase A — the film, in full

5. Scan the whole film at a fixed interval of ≤ 2 s plus scene changes. Then narrow every transition to ≤ 1 s, and to a single frame where a claim depends on it.
   Cover:
   - every lid opening and closing; every bud taken out of or put into the case or an ear, with the head-side mapping;
   - every tap: Connect, Disconnect, a switch, a pull, a slider, a preset, *Read EQ again*, the gear, Settings, Debug, Info, Export, the tile, the links;
   - every tab change and swipe, every rotation, every theme change, every Bluetooth toggle, every force-stop;
   - every Android screen: settings, Quick Settings, the shade, the "save as" dialog, the browser, the launcher;
   - every toast and dialog, every Home and return, every "—" and (i) dialog;
   - every action not in the plan.

   Read every visible on-screen text: switch positions, (i) lines, error texts, "Control channel", the Info build line. Crop and zoom the ear, case and card
   regions.
6. The **audio track**: establish what it holds (§2). Use audible events for timing only where they are unambiguous, and say so.
7. Measure the film ↔ phone clock offset at the start **and** at the end: a status-bar minute flip against the film's frame time, at ≥ 4 fps, never a single
   point. Measure also the app-logcat (UTC) ↔ phone, the system-log ↔ phone and the export ↔ phone offsets, each from an event that is in both.
8. Rewrite **CAP-071-EVENT-NOTES.md** in the `CAP-049-EVENT-NOTES.md` / `CAP-070-EVENT-NOTES.md` layout:
   - header status;
   - Log Metadata: the phone, the GrapheneOS build, the user and the absence of Google Play with the P0 output, the P1 output and its reading, the OpenControl
     build before and after the update from the Info tab, the firmware, other devices, the film/log ranges per file, the clock offsets, and the wear mapping with
     its cross-check;
   - a capture-integrity pre-flight: all four HCI logs incl. the prefix proof, both exports, the app logcat and its processes, the system log, and the absent
     dumps, second film and screen recording;
   - the video review method, incl. privacy;
   - an Event Timeline: phone time, action, actor, step ID, U-/test-plan ID, registry Test-ID, and evidence with its file;
   - a step-mapping table and the analysis checklist;
   - the skeleton as an unchanged appendix.

   **Traceability (`AGENTS.md` §13.7):** every skeleton step and every Test-ID it names appears in the timeline, or is explicitly marked "skipped" or "not
   identifiable". Every repetition and every extra action is its own row.

### Phase B — the HCI logs and the other logs, in full

9. Follow `AGENTS.md` §13 for **all four** HCI logs.
   - Establish the overlap first, and decide per packet range which file is the source.
   - Pre-filter by the Buds' handle(s), then take the full RFCOMM inventory: `SABM`/`UA`/`DISC`/`DM`, the direction, per ACL session and multiplexer side.
   - Then decode **every** Buds packet:
     - MAESTRO: every `GetSoftwareInfo` with its channel and entries; every `ReadSetting`/`WriteSetting`/`SubscribeRuntimeInfo`/`SERVER_STREAM` with field, value
       and status; the CRC-32 per frame;
     - the Message Stream per claim (the app vs anything else; ANC, battery, Find, SASS);
     - DLCI 0x08/0x0a (who opens them, every message);
     - HFP; AVDTP; AVRCP;
     - every ACL and LE event with its reason code;
     - the Bluetooth off/on cycles.
   - `hci1.log.last` (the unfilmed 1.1.0 stretch and its 17:40 reinstall) is analysed as fully as the rest: it is the "before" of the update.
   - Every negative needs its command, exit status and a positive control.
10. Read both exports and the app logcat **line by line**, and the system log in the ranges of §1 item 4. Put each relevant line on the timeline, with the phone
    time after the measured offsets and the file named.
    - For every filmed action, write the exact frames and log lines: what the app sent, on which channel, what the Buds answered, what the app logged and what it
      showed.
    - Where no log covers a stretch (the HCI boundaries, the stretch after the last export), say which evidence does, or that none does.
11. Build the tables the findings need:
    - the twelve reads per connection, against `CAP-070`;
    - every write against the reference-byte table;
    - the channel per announcement against the buds out/worn;
    - section C12;
    - the session-end and process table (PIDs 26327, 28680, and any other; the user-0 tile processes);
    - every battery value shown against the wire;
    - the Message Stream claims with their first phone message;
    - the 1.1.0 → 1.1.1 comparison of lead 2.

### Phase C — findings and the release verdict

12. For every lead of §3, give the answer with its evidence: frame numbers with their file, film times, log lines, `file:line` in the app sources. Label each
    🟢 FACT / 🟡 HYPOTHESIS / ⚪ ASSUMPTION / 🔴 OPEN QUESTION, with the experiment that would settle any non-FACT.
13. Write **CAP-071-FINDINGS.md** in the `CAP-049-FINDINGS.md` / `CAP-070-FINDINGS.md` layout.
    - Label every conclusion (`AGENTS.md` §15, `PROJECT_RULES.md` §1). Give every hex decoding with its command and raw bytes (rule 4a). Use at most one status
      banner (rule 9a).
    - Include:
      - the re-test verdicts, with the skeleton's "Refuted if" applied literally;
      - what works, what does not and what goes wrong, per protocol;
      - the 1.1.0 → 1.1.1 comparison;
      - what is different in a secondary user, and with the app also in user 0.
    - If a finding contradicts an existing 🟢 FACT or ADR, say so and draft the correction as a **proposal**. Do not edit that FACT or ADR.
14. **The release verdict for 1.1.1** (`RELEASING.md`, Release checklist C4). Classify every defect, with the evidence:
    - **heavy:** a crash, a wrong or unacknowledged write, a write applied before its answer, Safe Mode failing on the verified firmware, a session lost without
      recovery, a setting lost by the update, a behaviour change caused by the toolchain, or anything risky for the Buds;
    - **minor:** a known issue for the release notes.

    Then give one of:
    - *release the tested build as 1.1.1*, with the known issues worded for `CHANGELOG.md` and the release notes;
    - *fix first*: what, and the targeted re-test. The same version may be rebuilt while nothing is published;
    - *re-run*: which steps, and why.

    The steps that were skipped (BG-20 … BG-24 if not filmed, T11) are weighed explicitly: are they needed for **this** release (the claim is "no behaviour
    change"), or do they stay open in `TODO.md` §2?
15. **Improvements (proposals only, no app change).** For every defect, give:
    - the cause;
    - the proposed change: the file and function, the behaviour, the guardrails;
    - the unit test that would have caught it, with real-byte fixtures from this capture (`AGENTS.md` §11);
    - the re-test step.

    Anything that changes an ADR's decision is a drafted ADR (no number).
16. **Do not modify any file under `android/`, `dist/` or `scripts/`.**

### Phase D — checkpoint, documentation, finish

17. **Checkpoint: stop and ask, in this chat, via `AskUserQuestion`.**
    - Ask one question per decision. Give each option its pros and cons, mark one "(Recommended)", and put the exact draft text of any FACT, ADR, `PROJECT.md`,
      `CHANGELOG.md` or `README.md` change in the preview (memory: "Approvals: confirm in chat").
    - Cover at least:
      - the release verdict;
      - the known-issues wording;
      - lead L-1 (`PROTOCOL.md` §2.2a: a status proposal, if the evidence holds);
      - the open `TODO.md` §2 items this run did not settle;
      - the system log's fate (if not settled in Phase 0);
      - any other FACT or ADR change.
    - **No new capture skeleton is written in this session.**
    - Record the answers verbatim.
18. Apply only what was approved, and only to documentation:
    - `PROJECT.md`; `PROTOCOL.md`/`DECISIONS.md` (approved changes only, as dated Updates, with a process note citing this chat); `ARCHITECTURE.md`, only where a
      hardware result corrects it;
    - `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (the Group BG run note, the Capture Index row);
    - `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the evidence cells of the Test-IDs exercised; pointers only);
    - `APP_TESTPLAN.md` (the Summary for this run; a step that proved unworkable);
    - `id_registry.csv` (`CAP-071` → analyzed, with the real times);
    - `TODO.md` (done items removed, open items added; §2 in particular);
    - `CHANGELOG.md`: the `[1.1.1]` block's Known issues if the verdict changes them, **not** its date;
    - `scripts/release_notes.template` is under `scripts/`: propose a wording change, do not make it;
    - `README.md` (status, capture counts; not "Latest release");
    - `ai-sessions/INDEX.md` (the 0080 row).

    Run `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py`; the lint must exit 0.
19. Finish the RESULT.
    - Put the plain-language answers first:
      - the release verdict;
      - what works and what does not on 1.1.1;
      - whether anything changed compared with 1.1.0;
      - what differs in a secondary user;
      - what the app should change next.
    - Then give the step-mapping results, the tables of Phase B, and the external sources (URL plus quoted sentence).
    - If the verdict is "release", give the maintainer's next steps from `RELEASING.md`'s Release checklist, **as instructions, never executed**:
      - B3 first, if still not done: `cp -a dist/1.1.1 ~/opencontrol-1.1.1-tested`, after checking `sha256sum -c` still passes;
      - D1: merge PR #15 with a **merge commit**;
      - D2: tag **the build commit read from the Info tab** (`86a6fb3`);
      - D3: the files from the kept copy;
      - D4, and E1–E4.
    - End with **"Deferred documentation"** (each item also added to `TODO.md`) and **"Commits"** (hashes, or "not committed — reason"), per
      `AI_SESSION_LOG_PROCEDURE.md` §9 and §4b. Set the Status per §4.
20. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes:
    - commit on branch `release/1.1.1`, with Conventional Commits, one commit per concern (capture/LFS, docs, session files);
    - give each commit a *why*, and end it with the attribution line from the session's system reminder;
    - `git fetch` and rebase before pushing; never force;
    - run `git check-attr filter` on every capture file (`lfs`);
    - put **no file under `android/` in any commit**: `git diff 86a6fb3..HEAD -- android` must stay empty;
    - stage nothing from a build directory, `dist/`, `android/.kotlin/`, `.vscode/` or `__pycache__`.

    A large LFS push (the film is ≈ 1.7 GB) can drop the SSH connection after the upload. Check `git log origin/release/1.1.1 -1`, and push again if needed.
    Then update PR #15's checklist (C1–C4 ticked as far as the evidence goes) with `gh pr edit`, after showing the new body.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **No app changes and no release actions.**
  - Nothing under `android/`, `dist/` or `scripts/` is modified, and no build is made.
  - `scripts/release.sh` is not run, and the pull request is not merged.
  - No tag is created or pushed; no GitHub release is created or edited; no asset is uploaded.
  - Publishing is the maintainer's own act (`RELEASING.md`).
- **Approvals only in this chat.** Never promote, demote or correct a 🟢 FACT, and never write or update an ADR, without the maintainer's approval given in this chat
  (`AGENTS.md` §6). Model agreement is not approval.
- **Evidence.**
  - Zero creativity with hex (`AGENTS.md` §13.6).
  - Every claim needs a frame number (with its HCI file), a log line (with its file), a `file:line` or a film timestamp (`PROJECT_RULES.md` rule 4a).
  - A negative needs its command, its exit status and a positive control (`AGENTS.md` §13 step 8).
  - What the maintainer heard or saw is their observation, unless the wire shows the command that caused it.
- **What was done, not what was planned.** The skeleton is the plan; the film and the logs are the record. Never fill a timeline row from the skeleton's "Expected"
  column.
- **Privacy.** No full MAC address or serial in any committed text beyond what ADR-010 allows, and no personal data from the film, its audio or the logs. The system
  log is committed only in the form the maintainer approves.
- **Scope.** Stay within `PROJECT.md`. Anything new (a feature, a permission, a dependency, a new wire request) is a checkpoint question with a drafted ADR, not a
  silent addition.
- **Subagents.** A subagent may only read and report. Every write is done in the main session and verified; re-derive any number a subagent reports.
- **Files.**
  - Before deleting, moving or renaming any file, diff a fresh listing of the source against the copy (checksums).
  - Never `rm -rf` from memory of an earlier listing, and never use wildcard deletes. Ask before any delete.
  - Do not run any tool that writes into `dist/`.
- **Commits.** Commit and push only after the maintainer confirms the final summary (task 20).
- **No new capture skeleton.** Do not create a new `CAP-NNN` folder, skeleton or `planned` registry row in this session. Open steps go to the findings and
  `TODO.md`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0080_CAPTURE_PROMPT_2026_10_08.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0080_CAPTURE_PROMPT_2026_10_08
