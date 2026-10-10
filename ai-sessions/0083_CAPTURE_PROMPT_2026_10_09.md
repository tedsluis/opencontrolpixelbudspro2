# 0083_CAPTURE_PROMPT_2026_10_09.md — Full analysis of CAP-072 (Group BH: the 1.2.0 release APK installed over 1.1.1 in the GrapheneOS user without Google Play services — the release run of 1.2.0; film 1 of two, BH-25/BH-26 pending)

**Number:** 0083
**Category:** CAPTURE
**Date:** 2026-10-09
**Title:** Fully analyse `CAP-072`: **one** camera film (no usable sound) and **one** Android screen recording (with sound), **four** HCI snoop logs, **four** app
debug exports, **one** app logcat, **one** full system log (`logcat -b all`) and the P0/P1 shell outputs. It was recorded while the release-signed **1.1.1** APK
(freshly reinstalled minutes before) was updated **in place** to the release-signed **1.2.0** APK (`dist/1.2.0/`, built from `release/1.2.0` at `ec6d163`), in a
GrapheneOS secondary user without Google Play, following the Group BH skeleton in `CAP-072-EVENT-NOTES.md` up to **BH-24**; BH-25 and BH-26 are filmed later.
- Record the real events in **CAP-072-EVENT-NOTES.md** and the analysis in **CAP-072-FINDINGS.md**.
- Establish what was actually done; investigate the maintainer's **case-sound** observation and the **press-and-hold** observation (the audible ANC change
  that the app never showed) thoroughly; give the release verdict for 1.2.0 (`RELEASING.md`, Release checklist C4) — conditional on film 2 if it is not
  there yet.
- Propose how the hardware runs can become **shorter and simpler** (a proposal, not a change).
- **No change to the app itself**, **no new capture skeleton**, **no publishing step**.

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this repository. Run the
phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action, read these in full and in this order, to understand
the ground rules before auditing anything:
- `AGENTS.md` and `PROJECT_RULES.md`.
- `PROJECT.md`, including the Definition of done with its evidence table and the 1.2.0 status paragraph.
- `ARCHITECTURE.md`, in particular §2.4, §3.1 (the "one rule for current" bullet and the DLCI 0x02 settings row), §3.2 (timing constants), §5a, §6.0a, §6.0b, §7,
  §8.1, §9, §12 and §13.
- `PROTOCOL.md`, in particular:
  - §2.2a with all its Updates (the announced channel and lead L-1), §2.3;
  - §4.1 (ANC: `Get`/`Set`/`Notify`, the Settable byte), §4.2, §4.3 Option B/F;
  - §4.5's preamble, §4.5.2 Multipoint, §4.5.4 head gestures, §4.5.6 Volume EQ, §4.5.7 balance, **§4.5.8 Case sounds** (field 28 "Bud return" /
    "Earbuds replaced", field 27 "Other alerts"), §4.5.8a;
  - §5, §6 (the 2026-10-09 entries: `GetHardwareInfo`, the serial labels by position) and §8 (the 2026-10-09 row).
- `DECISIONS.md`: **every** ADR, ADR-001 … ADR-059, with every dated Update, and the **draft** ADR-060 (proposed, not accepted — do not treat it as a decision).
  In particular ADR-010, ADR-019, ADR-029, ADR-034, ADR-036, ADR-037, ADR-042 … ADR-049, ADR-052 … ADR-059.
- `TODO.md`, §2 in particular (the items `CAP-072` was to settle) and §5 (the fixture swap after `CAP-072`).

These are the ground rules; do not audit or change anything before they are read. Then, per task:

- **Procedure and registers:** `AI_SESSION_LOG_PROCEDURE.md` (in full, incl. §4b and §9), `ai-sessions/INDEX.md`, and `id_registry.csv`: the `CAP-068` … `CAP-072`
  rows and every Test-ID the skeleton names.
- **What the build under test is — read in full:**
  - `ai-sessions/0082_FEATURE_PROMPT_2026_10_09.md` and `ai-sessions/0082_FEATURE_RESULT_2026_10_09.md` — the five 1.2.0 items, §A.2/§D the reference bytes of
    `GetHardwareInfo` and its answer, §H the gate and the mutations, §I the skeleton table and the `TODO.md` §2 items taken into this run, the checkpoint
    answers (the maintainer's decisions of 2026-10-09);
  - `CHANGELOG.md` `[1.2.0] - not yet released` (Added / Changed / Studied, not built / Known issues), `README.md`;
  - `RELEASING.md`: the **Release checklist** A–E, "Repository rules", §4–§8 and §13 — and the deviation recorded in §2 below;
  - `APP_TESTPLAN.md` in full. Section V (V1–V19) is the 1.2.0 build; U1–U3, U10–U14, C8, C9, C12, F1–F4, G3, H2–H6, I1–I3, J4, K1, K2, M2–M6, N2–N12, R6,
    S6, S12, T3–T8 and T11 are taken into this run (re-derive the list from the skeleton);
  - pull request #21 (`gh pr view 21 --json state,mergeCommit,headRefOid,body`), and `git log --oneline 531c99d^..ec6d163` (the six 1.2.0 commits and the merge).
- **The skeleton:** captures/CAP-072-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BH/CAP-072-EVENT-NOTES.md (the placeholder folder, renamed in this session).
  - Read it **as committed** (`git show HEAD:<path>`): purposes I–V, the reference-frame table, P0–P9 with the ★ items, BH-1 … BH-26 and BH-end1 in five parts,
    "Don'ts", "After the run", and the analysis checklist with its "Refuted if" column. The working-tree copy is **unchanged** (measured 2026-10-09 22:00;
    re-check with `git status`): the maintainer's P0/P1 outputs are in CAP-072-adb-shell.log instead.
- **Logging and capture method:** `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3 (the snoop log rotates on every Bluetooth off/on; `adb bugreport` is how it is pulled), §5,
  §8, §9 and the Group BH section.
  - Read `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` for every Test-ID the skeleton names: `ANC-001` … `ANC-004`, `AUDIO-001` … `AUDIO-003`, `BATT-004`, `CASE-001`,
    `CASE-002`, `CASE-004`, `CASE-005`, `CONV-001`, `EQP-001`, `EQS-001`, `FIND-001`, `FIND-002`, `FW-003`, `HEAD-001`, `HOLD-005`, `INEAR-004`, `INEAR-005`,
    **`INEAR-006`** (new, registered by `ai-sessions/0082`), `MULTI-001`, `PAIR-003`. Re-derive this list from the skeleton yourself.
- **The layout to follow — read in full:**
  - `captures/CAP-071-2026-10-08_17-51-29_18-27-28-Group_BG/CAP-071-EVENT-NOTES.md` and `…/CAP-071-FINDINGS.md` — the maintainer's named examples, and the
    previous release run in the same user: the comparison baseline for every screen and every frame (1.1.1 is the "before" of 1.2.0). They also have the
    clock-offset method, the prefix proof of the HCI logs, the session-end table, the Play-services marker, the `bugreportz`/SIGQUIT explanation (§0), the
    system-log extract command (§0) and the release verdict (§10);
  - `captures/CAP-049-2026-09-12_18-13-54_18-21-49-Group_AF/CAP-049-EVENT-NOTES.md` and `…/CAP-049-FINDINGS.md`;
  - `captures/CAP-070-2026-10-07_06-10-41_06-40-59-Group_BF/CAP-070-FINDINGS.md` (1.1.0; the channel-19 references).
- **The reference captures of the skeleton's byte table:** the FINDINGS of `CAP-019`, `CAP-024`, `CAP-036`, `CAP-041`, `CAP-045`, `CAP-058`, `CAP-059`,
  `CAP-062`, `CAP-064`, `CAP-065`, `CAP-066`, `CAP-068`, `CAP-069`, `CAP-070` and `CAP-071`, at the frames the table cites. For the case-sound lead (§3 item 7)
  also `CAP-024-FINDINGS.md` §4–§5 and `CAP-058-FINDINGS.md` §6 in full, and `REVERSE_ENGINEERING.md`'s `qhr` entry (fields 27/28, `fyo.java`, `fxb.java`).
- **Every Kotlin source under `android/` that a finding touches, in full,** before you write about it. At least the sixteen files of the 1.2.0 commit
  (`git show --stat ca9debe -- android`):
  - `HardwareInfo.kt`, `Maestro.kt`, `CodecRouter.kt`, `SettingFrame.kt`, `PwRpc.kt`;
  - `BudsRepositoryImpl.kt`, `BudsRepository.kt`, `SessionReopener.kt`, `SessionDiagnostics.kt`, `RfcommBudsTransport.kt`;
  - `WornReading.kt`, `AncModeCause.kt`, `DeviceInfo.kt`, `BudsSettings.kt`, `ValueCurrency.kt`;
  - `AncScreen.kt`, `ConnectionScreen.kt`, `ControlsScreen.kt`, `EqScreen.kt`, `SettingsMenu.kt`, `SettingsUi.kt`, `OpenControlNavHost.kt`;
  - `MainActivity.kt`, `AppUiSession.kt`, `AncTileService.kt`;
  - the test fixtures `HardwareInfoFixtures.kt` (the redacted `GetHardwareInfo` answers) and `android/app/build.gradle.kts` (`versionCode = 10200`).

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.**
- Create **ai-sessions/0083_CAPTURE_RESULT_2026_10_09.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block.
- Update that block at the end of every phase **and** after every substantial step within a phase: every 5 minutes of film reviewed (there are two films of
  56 minutes each), every HCI log and channel decoded, every log file read, every 20,000 lines of the system log, every checkpoint answer. Say:
  - what is done and what is next;
  - which files are touched but not yet verified;
  - where the intermediate results live: the scratchpad directory, with its scripts, contact sheets, decoded tables, the audio transcript and notes. Re-create
    them if the scratchpad is gone.
- A resumed session reads this prompt, then the RESULT's Progress block, re-reads the files the unfinished step touched, and continues from there.
- It never redoes a finished, recorded step, and never assumes an unrecorded step was done.
- The prompt keeps its number and date; the RESULT keeps growing in the same file.

**Memory and the machine (two OOM kills so far, `ai-sessions/0057` and `0074`).** The laptop has 15 GB.
- Run `tshark`/`ffmpeg` work one job at a time. The camera film is 2.6 GB and the screen recording 414 MB: extract contact sheets and crops to the scratchpad in
  ranges, never decode a whole film into memory. Read the 49 MB system log with `grep`/`sed`/`awk` in ranges, never whole into one tool output.
- Never run two Gradle builds at once; no build is needed in this session anyway.
- Never `git worktree add` a commit: a worktree checks out `captures/` (LFS, gigabytes).
- Write intermediate results to the scratchpad and update the Progress block **before** a long job.

**Nothing is taken on trust, including this prompt, the skeleton, earlier RESULTs and the maintainer's statements.** Every claim, and every number measured
below (§2), is re-derived from the films, the HCI logs, the other logs, the code and official documentation.

---

## 1. What the maintainer asked for (chat, 2026-10-09, translated from Dutch)

1. **Analyse `CAP-072` (Group BH) of the OpenControl app extensively and record the findings.** First read the core files (`AGENTS.md`, `PROJECT_RULES.md`,
   `PROTOCOL.md`, `ARCHITECTURE.md`, `DECISIONS.md`) to understand the ground rules before auditing the rest. Run everything in phases, strictly in order; when a
   token limit is hit, continue automatically later (§0).
   - Analyse the films CAP-072-recording.mp4 and CAP-072-screen-20261009-182342-1791559646223.mp4 first, and record every event and action with its time in
     **CAP-072-EVENT-NOTES.md**.
   - Analyse **all** `CAP-072-btsnoop_hci*` logs with `tshark`.
   - Analyse the four `CAP-072-opencontrol-debug-20261009-*.txt` exports, the `CAP-072-OpenControl-for-Pixel-Buds-Pro-2-log-*.txt` logcat,
     CAP-072-logcat-all.txt (the system log) and CAP-072-adb-shell.log.
   - Then correlate the events of **CAP-072-EVENT-NOTES.md** with the HCI logs, the system log, the exports and the app logcat; `CAP-071-EVENT-NOTES.md` is the
     example.
   - Run an extensive analysis and record the findings in **CAP-072-FINDINGS.md**; `CAP-071-FINDINGS.md` is the example.
   - Possibly relevant: `ai-sessions/0082_FEATURE_PROMPT_2026_10_09.md`, `ai-sessions/0082_FEATURE_RESULT_2026_10_09.md`, `APP_TESTPLAN.md`,
     `ARCHITECTURE.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, the skeleton, `DECISIONS.md`, `PROJECT.md`, `README.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `TODO.md`.
2. **Not every step was done exactly as in the test plan, and some extra things were shown on the film. The run took an hour, and the maintainer got no further
   than BH-24; BH-25 and BH-26 are filmed "tomorrow" (2026-10-10).** So **establish from the films and the logs what was actually done**, in the order it was
   done; never fill the timeline from the skeleton.
   - Map each real action to the skeleton's step IDs (P0–P9, BH-1 … BH-24, BH-end1), to `APP_TESTPLAN.md` section V (V1 … V19), and to the other test-plan
     IDs the skeleton names (U1–U3, U10–U14, C8, C9, C12, F1–F4, G3, H2–H6, I1–I3, J4, K1, K2, M2–M6, N2–N12, R6, S6, S12, T3–T8, T11).
   - Mark each step **done**, **done differently**, **repeated** (each repetition its own row), **skipped**, **pending (film 2)** or **not identifiable**.
   - List every extra action as its own row ("not in the plan") and say what it demonstrates.
   - **Film 2 (BH-25, BH-26: the third export, Bluetooth off, the force-stop, the three `uiautomator` dumps, Bluetooth on, the last export).** Check at the
     start of the session, and again before the checkpoint, whether its files are in the folder (a second camera film, a second screen recording, three
     `uiautomator` XML dumps, further exports, further HCI logs, a longer or second system log). **If they are there, analyse them in this session with the same
     rigour** (Phases A–C apply to them as well; they are the third Bluetooth off/on and will have rotated the snoop log again). **If they are not, record every
     film-2 item as "pending (film 2)"**, give the release verdict conditional on them, and say exactly which files the maintainer must add and how they are
     analysed afterwards (a short continuation of this session's RESULT, not a new capture number — `CAP-072` stays one capture in one folder).
3. **Facts the maintainer gave.** Record them as the maintainer's statements, and check each against the evidence:
   - **At the start of the video the app was 1.1.1; during the video the maintainer upgraded it to 1.2.0.**
     - Read the Info tab on film before and after the update: version, `build <hash>[-dirty]`, commit date, "Control channel: N". "-dirty" must not be shown.
       The build hash is expected to be **`ec6d163`** — the merge commit of PR #21, the tip of `release/1.2.0` when `scripts/release.sh 1.2.0` ran (see the
       deviation in §2). If the film shows another hash, that is a central finding: find what was built.
     - Check that `ec6d163` is the tip of `origin/release/1.2.0` **and** of `origin/main` at the start of this session (`git fetch`; `git log --oneline -1`
       for both), and that no file under `android/` changed after it (`git diff ec6d163..origin/release/1.2.0 -- android` empty).
     - The app logcat's header says `package: io.github.tedsluis.opencontrolpixelbuds:10200, targetSdk 34` (re-check).
     - The system log holds the update itself; find and quote each line (measured line numbers in §2 — re-check): the `abb_exec:package` install request at
       17:29:12, the two `installer_clear_app_data_caller` lines at 17:29:13 (cache only, as `CAP-071` established — re-verify against AOSP
       `AppDataHelper.clearAppDataLeafLIF` / `prepPerformDexoptIfNeeded`), `Killing 11717 … killDueToPackageUpdate` at 17:29:24, the `am_proc_start` of
       PID 12137 at 17:29:25.
     - **The 1.1.1 base was not `CAP-071`'s install.** The system log shows `cmd package 'uninstall'` of the app at 17:24:06 (`Force stopping … user=-1:
       deletePackageX`, "pkg removed" in user 10 **and** user 0, PID 6994 killed), a new install at 17:24:40–17:24:52 (appid **10354** — a new UID; `CAP-071`'s
       was 10353), PID 11717 (1.1.1) from 17:25:16, the P0/P1 shell commands at 17:26:37–17:26:54, then the 1.2.0 install at 17:29:12. So: 1.2.0 **was**
       installed over 1.1.1 (U1/V1 hold for the update itself), but 1.1.1 had been reinstalled **five minutes earlier with its data deleted** — the Dark mode
       and Debug mode choices of P1 had to be made again in the fresh 1.1.1 (on film?), and the app is **no longer in user 0** (the `dumpsys` block with
       `firstInstallTime=1970-01-01` is a user where it is not installed — establish which). Say what this means for "the second update test", for the
       `CAP-071` comparison (user-0 tile processes existed there) and for the Companion Device association (kept or re-made? ADR-044; the first export line
       "Permissions (start): Bluetooth NOT_REQUESTED -> GRANTED").
     - Check `dist/1.2.0/` **read only**: `sha256sum -c`; expected APK `a67adad1214daf163981ca7b2b72aa28fee45e7f344a7669345ea6a3b7ca44c8`; `apksigner verify
       --print-certs`, expected certificate `a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb` (both measured 2026-10-09 22:00 with
       build-tools 36.0.0). Never rebuild, never run `scripts/release.sh`, never write into `dist/`.
     - If the phone is attached and the maintainer agrees, `adb shell pm path --user 10` plus `adb pull` of the installed `base.apk` into the scratchpad and its
       SHA-256 against `a67adad1…44c8` proves the installed APK is the built one. Ask first; otherwise say it is not proved.
   - **The capture ran on a Pixel 9a with GrapheneOS, Android 17.**
     - Verify it from the logcat header (`osVersion: google/tegu/tegu:17/CP3A.261005.005/2026100601:user/release-keys`, `userType: full.secondary`, `flags: dev
       options enabled`), from the system log, and from the P0 output. Note: `CAP-071` ran on `CP3A.260905.009` — **GrapheneOS was updated between the two
       runs**; say whether anything Bluetooth-related changed between the two builds (GrapheneOS release notes; cite the URL and sentence) and whether this run
       can tell the toolchain of 1.2.0 apart from the OS update for any difference found.
     - Interpret the P0 output exactly (CAP-072-adb-shell.log, block 2): `pm list packages --user 10 | grep -i -E "gms|vending"` matched only the three
       `app.grapheneos.gmscompat*` packages, `exit=0` (GrapheneOS's own compatibility layer — check it against GrapheneOS documentation); no
       `com.google.android.gms` and no `com.android.vending`. **The positive control (`grep opencontrol`, exit 0) and the `am get-current-user` output are
       not in the file** — the system log's adbd lines show which commands ran (17:26:53 `am get-current-user`, 17:26:54 `pm list packages --user 10`);
       record what the file lacks and what covers it.
     - Above all, use the HCI logs: there must be **no Message Stream claim whose first phone message is `03 08 00 02 01 25`** (the Play-services marker of
       `CAP-066-FINDINGS.md`). Give the command, its exit status, and a positive control in `CAP-066`.
   - **The maintainer spoke during the video, in Dutch, but not always.**
     - **The camera film carries no sound**: `ffprobe` shows an audio stream with `codec_name=unknown`, `codec_tag_string=[0][0][0][0]`, "Audio: none,
       0 channels" (measured; re-check, and establish whether the track is empty, undecodable or a container quirk — try `ffmpeg -i … -map 0:a -f null -`
       and report the error text). **The screen recording's AAC mono track is the only sound** (44.1 kHz; presumably the phone's microphone — establish what
       it holds: the maintainer's voice, the music, the Buds' and the case's sounds, the ring of BH-19, the incoming call).
     - **Transcribe every spoken observation** from the screen recording's audio, with its time, in Dutch and translated, into the notes (the Video review
       method section and the timeline). Use a speech-to-text tool only if one is available offline on this machine (say which, or that you listened in
       ranges with `ffmpeg` spectrograms/`silencedetect` to find speech and had no transcription tool — then record "speech at HH:MM:SS, not transcribed" and
       **ask the maintainer** for the content of the untranscribed stretches at the checkpoint). Nothing from the audio goes beyond what is needed (privacy,
       §2).
   - **"I find it very hard to run so many tests in a row and to think of so many details. Find out how the testing can be simpler and shorter from now on."**
     This is a deliverable of this session (§3 item 18, Phase C task 15, the checkpoint): a **proposal**, with numbers from this run (minutes per section,
     steps per section, which steps found something in `CAP-068` … `CAP-072` and which never did), not a change to any test plan without approval.
   - **"The ANC modes during playback were right."** Record it as the maintainer's observation; the wire (each `Set` with its ACK and the Buds' `Notify` while
     A2DP streams) is the evidence.
   - **"A press-and-hold on a bud changes the ANC mode audibly, but in none of the cases did the change become visible in the OpenControl app. Investigate the
     cause very thoroughly."** (Added by the maintainer in chat after the first version of this prompt.) This is the second main question (§3 item 5). It
     concerns BH-11 (V10) — the 1.2.0 item "Changed by the Buds" (`CHANGELOG.md` Added; `AncModeCause.kt`; `ai-sessions/0082` RESULT §A.1) — and
     possibly every press-and-hold of the run (BH-17's "Noise control" hold, and any hold shown as an extra). Establish on the wire and on both films, for
     **each** hold: the Buds' audible change (the audio track), whether the app's Message Stream channel (DLCI 0x04/0x05) was **open** at that moment, whether
     a `Notify` (`08 13 00 04 01 e8 e8 xx`) reached the phone at all, what the app logged, what the ANC card and its (i) showed then and at the **next**
     claim (a tap, a pull, the tile, a re-open). The leading hypothesis is in §3 item 5; test it, do not assume it.
   - **"Case sound → Earbuds replaced sometimes did not work right: sometimes I heard a sound when putting a bud back although it was not meant to, or no
     sound although it was meant to. I think it is because, when a bud goes out of the ear and back into the case, the connection between the phone and the
     case drops for a moment. Investigate this thoroughly."** This is the run's main question (§3 item 7). Treat the maintainer's explanation as a
     🟡 HYPOTHESIS among others, and test each against the evidence.
   - **"Nothing else about the app was found not to work."** Record it; the evidence decides.
   - **"During the test there was an incoming phone call."** The system log has it at 18:02:25.5 (`ImsPhoneCallTracker … newState=RINGING`, Telecom
     `setCallState NEW -> RINGING`, the `InCallService` of `com.android.bluetooth` bound for user 10). P7 asked for Do Not Disturb — it was evidently not on,
     or did not stop the call. Establish (§3 item 9): whether the call reached the Buds (HFP/SCO in the HCI log, the Buds' own call handling), what the app
     and its session did during the call, whether A2DP was suspended, and — privacy — whether the caller's number or name is on either film (the camera
     film's phone screen, the screen recording's incoming-call UI, the audio).
4. **Strict execution rules (the maintainer's own words, translated):**
   - **No assumptions.** Verify everything.
   - **Validate externally.** Use search tools to validate claims against official documentation where needed: Android Developer docs / AOSP sources, the
     Bluetooth Core Specification, the Fast Pair specification, GrapheneOS documentation. Cite the URL and the exact sentence.
     - developer.android.com pages have returned only navigation to the fetch tool before.
     - AOSP sources at `android.googlesource.com/…?format=TEXT` read with `curl … | base64 -d`, and the raw Fast Pair pages via `curl`, worked.
     - Check any fetch-tool summary against the raw text before quoting it.
   - **No sampling.** No random spot checks: a 100 % complete and exhaustive review of the relevant files, checking every detail:
     - every second of both films (and of film 2's files if present);
     - every packet of all four HCI logs that belongs to the Buds (and every packet of CAP-072-btsnoop_hci1.log.last that belongs to the Buds — it is the
       "before", 18 hours long);
     - every line of the four exports, the app logcat and the shell log;
     - every line of the system log that belongs to the app, the Bluetooth stack, the package manager, the activity manager, the window manager, Telecom/the
       phone process or `dumpstate` in the films' time range, plus whatever a lead points to.
   - **Best practices.** Industry-standard technical review methods:
     - **evidence traceability:** every claim links back to a specific frame number with its HCI file, a log line with its file and line number, or a film
       timestamp (and which film);
     - **structural integrity:** check the files, the registry, the links and the footers.

---

## 2. The evidence

captures/CAP-072-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BH/:
- CAP-072-EVENT-NOTES.md is committed (the skeleton) and **unchanged** in the working tree.
- The other **fourteen** files are **untracked**.
- `id_registry.csv` has a `planned` row for `CAP-072`; `CAPTURE_BLUETOOTH_HCI_SNOOP.md` has the Group BH section and a *planned* Capture Index row.

Measured by the chat that wrote this prompt (2026-10-09, 22:00). **Re-check each value:**

| File | Measured / what to check first |
|---|---|
| **CAP-072-recording.mp4** (the camera film) | 3,373.32 s (56 min 13 s), 2,638,791,929 bytes, H.264 1280×720 at ≈ 29.87 fps, **audio: none, 0 channels** (see §1 item 3). `creation_time` 2026-10-09T16:23:39Z = 18:23:39 local, mtime 18:23 — probably the film's *end*, which puts its start at ≈ 17:27:26, i.e. **after** the P0/P1 shell commands (17:26:37–17:26:54) and **before** the 1.2.0 install (17:29:12). Measure the film's own clock from the status bar (P5). Only one camera film: BH-end1 ("stop film 1") must be its end — or the film runs on through BH-23/BH-24 (the maintainer did those). Establish it |
| **CAP-072-screen-20261009-182342-1791559646223.mp4** (Android's screen recording, P4 ★) | 3,376.10 s, 414,388,023 bytes, H.264 1080×2424 at 59.94 fps, **AAC 44.1 kHz mono**. `creation_time` 2026-10-09T16:25:46Z = 18:25:46 local (the save); the name's 18:23:42 is probably the stop. Nearly the same length as the camera film: establish its start and end from the status bar, and the offset between the two films from a shared event (a tap seen on both). This is the first run with a screen recording: S6, C12, the (i) texts and every toast are read from it, not from the camera |
| **CAP-072-btsnoop_hci1.log.last** | 36,083 packets, **2026-10-08 21:47:46.610 – 2026-10-09 15:40:25.818** (≈ 17.9 h, 2,607,765 bytes). **Before the run**: the previous evening (after `CAP-071`'s release) and the whole day until the **user switch 0 → 10 at 15:40:24** (`am_switch_user: 10`) that closed the snoop session — establish that a user switch restarts the Bluetooth stack (the system log; AOSP), and that this is why the file ends there. Inventory what it holds: the Buds' connections and the 1.1.1 app's traffic (the app's tile process in user 10 started 15:40:36, PID 28386, `bound-service`), other devices (privacy). Analyse the Buds' packets as fully as the rest; nothing in it is on film |
| **CAP-072-btsnoop_hci1.log** | 21,727 packets, 15:40:29.398 – 18:17:59.310 (1,244,736 bytes). Pulled by **bugreport 1** (`bugreportz -v`/`-p` at 18:16:07, `dumpstate: done (id 1)` at 18:18:18) — **before** the Bluetooth off of BH-22, as the capture document asks |
| **CAP-072-btsnoop_hci2.log.last** | 21,861 packets, **15:40:29.398** – 18:18:33.356 (1,255,001 bytes). `cmp -n 1244736` exits 0: **CAP-072-btsnoop_hci1.log is a byte-for-byte prefix of CAP-072-btsnoop_hci2.log.last** (the same snoop session pulled twice; the session was closed by the Bluetooth off at 18:18:32–33). Prove it packet by packet; count every frame once |
| **CAP-072-btsnoop_hci2.log** | 1,158 packets, 18:18:45.641 – 18:23:12.322 (53,774 bytes): from the Bluetooth on (`STATE_CHANGED … OFF → TURNING_ON` 18:18:45.96, ON 18:18:46.24) to **bugreport 2** (`bugreportz` 18:21:31, `dumpstate: done (id 2)` 18:23:36). The last ≈ 11 minutes of the run (BH-22's on, BH-end1, BH-23, BH-24) are in this file only — unless the film ran on after 18:23:12: say what evidence covers 18:23:12–18:23:46 |
| All four HCI logs | `Bluetooth H4 with linux header`. The run itself (≈ 17:27–18:24) lies in hci1.log / hci2.log.last and hci2.log; the single boundary inside the run is 18:18:33 → 18:18:45 (BH-22). Establish the gap and that no Buds traffic is lost or counted twice. The P2 Bluetooth off/on "on film" at the start: was there one? If so it would have rotated the log — the files say there was **no** rotation between 15:40:29 and 18:18:33; reconcile this with the film |
| **CAP-072-opencontrol-debug-20261009-181532.txt** | 2,120 lines, 17:29:26.093 – 18:15:24.702; 924 `DLCI 0x` lines |
| **…-181901.txt** | 2,196 lines, – 18:18:49.377 ("RFCOMM on-demand channel 0x04 released"; 18:18:48.619 "Hardware info read (channel 19): 3 serial numbers" — the re-open after BH-22's Bluetooth on); 951 `DLCI 0x` lines |
| **…-182109.txt** | 2,334 lines, – 18:21:06.785; 1,001 `DLCI 0x` lines |
| **…-182335.txt** | 2,345 lines, – 18:23:05.380; 1,004 `DLCI 0x` lines |
| All four exports | **Four exports**, all starting with the same line at 17:29:26.093 ("Permissions (start): Bluetooth NOT_REQUESTED -> GRANTED, …"): one process (12137, the 1.2.0 process, alive from 17:29:25 to the end — no force-stop; BH-25's is film 2). The skeleton asked for an export at BH-22 (before Bluetooth off), BH-end1, BH-24 (across a rotation), BH-25 and BH-26. Map each of the four to a step from the films (18:15:32 is ≈ 3 min before the Bluetooth off of 18:18:32 — bugreport 1 lies in between; 18:19:01 is after the on; 18:21:09 and 18:23:35 are BH-23/BH-24 territory). Check the Debug-mode state per stretch, which export contains which, the **serial-number and MAC grep** (BH-22's "Refuted if"; `grep -c` each serial's first 4 + last 2 characters and the Buds' MAC in every export = 0, positive control: the export's own "Control channel" line), and explain the gap between each export's last line and its save time |
| **CAP-072-OpenControl-for-Pixel-Buds-Pro-2-log-c0b102db82fc.txt** | the app logcat: 722 lines; header `type: logcat`, `osVersion: google/tegu/tegu:17/CP3A.261005.005/2026100601:user/release-keys`, `userType: full.secondary`, `flags: dev options enabled`, `package: …:10200, targetSdk 34`, buffers `main,system,crash,events,kernel`, `level: verbose`. Timestamps `10-09 15:25:16.837` – `16:23:46.806`: **UTC, local − 2 h** (measure it against the system log). PIDs **11717** (19 lines, the 1.1.1 process 17:25:16–17:29:24 local — events only) and **12137** (513 lines, 1.2.0). `grep` found one `Wrote stack traces to tombstoned` (line 648, 16:21:56 UTC = bugreport 2's SIGQUIT) and no `FATAL`, `ANR in`, "Decoder fault", "Unexpected error", `StrictMode`, "failed to call close", "Late WriteSetting", "request held" — re-check each with its command and exit status |
| **CAP-072-logcat-all.txt** (P6) | `adb logcat -b all -v threadtime`, started 17:26:16.231 (adbd line 68458 — **before** the install, as P6 asked); 326,217 lines, 49.3 MB, **local time**, from `10-08 21:46:59` (ring buffer) to `10-09 18:24:23.574`. Leads (line numbers measured — re-check): the uninstall/reinstall of 1.1.1 (59352–64979), the 1.2.0 install (74502–75288), the incoming call (202401 ff.), bugreport 1 (271079–285031) and its SIGQUIT to 12137 (273502–273513), Bluetooth off/on (285872–289906), bugreport 2 (307502–318734) and its SIGQUIT (309478–309483). **The Bluetooth stack's own lines** (`bluetooth-a2dp`, `bt_btif`, `bt_stack`, `BluetoothAdapterService`, the HFP state machine) at every ACL change, every A2DP start/suspend, the call, and the off/on. **Privacy:** the file holds the whole phone's activity since the previous evening, in every user — Telecom lines name third-party phone accounts (a chat app, a messenger, a mail app), and there will be notifications, Wi-Fi, accounts. It is **untracked**; it goes to a public repository through LFS if committed whole. `CAP-071` committed an **extract** (its `CAP-071-logcat-extract.txt`: the films' range, the app's PIDs, the Bluetooth PIDs, every line naming the app or `com.android.bluetooth`; command in `CAP-071-FINDINGS.md` §0). Propose the same here (the range 17:20:00–18:25:00; PIDs 6994, 11717, 12137, the Bluetooth process 27925 and any other; add `dumpstate`, Telecom's **call-state** lines without numbers or names, and the package manager's lines for the app) — and **ask**: earlier choices do not carry over. The file name of the full log is deliberately not written in backticks anywhere (`scripts/lint_docs.py` flags a backticked file that is not committed) |
| **CAP-072-adb-shell.log** | 556 bytes, three dated blocks: **17:26:37** `dumpsys package` grep: `versionCode=10101`, `lastUpdateTime=2026-10-09 17:24:41`, two `firstInstallTime` lines (`1970-01-01 01:00:00` and `2026-10-09 17:24:41`); **17:26:53** P0: the three `app.grapheneos.gmscompat*` packages, `exit=0`; **17:29:54** `versionCode=10200`, `lastUpdateTime=2026-10-09 17:29:24`, the same two `firstInstallTime` lines. So: `firstInstallTime` unchanged across the update (U1/V1's test), `lastUpdateTime` later, `versionCode` 10200. Record the file as the maintainer's P0/P1 evidence (copied unchanged into the notes, as `CAP-071` did), with what it lacks (§1 item 3) |
| Absent (film 1) | **No `sha256sum *` file**, no copy of `dist/1.2.0/`'s `.sha256` or the script's certificate line (the skeleton's "After the run"): make the checksum file in this session (into the folder, with approval) and quote the two SHA-256 values from `dist/1.2.0/` read-only. **No spoken observations on the camera film** (its audio is empty) — the screen recording's audio is the only source |
| Absent (film 2, pending) | the three `uiautomator` dumps (BH-25, T11), the force-stop, the third Bluetooth off/on, exports 5 and 6, film 2 and its screen recording, and the HCI log after the third rotation. Record each as pending with what will cover it |

- **File modes:** seven files are `-rw-r--r--` (the notes, the shell log, the four HCI logs, the system log); **seven are `-rwxr-----`** (the four exports, the app
  logcat, both films). No capture file may have the executable bit (fix it in Phase 0, with approval: `chmod 644`).
- **LFS:** `git check-attr filter` gives `lfs` for all fourteen data files and `unspecified` for the notes (measured; re-check).
- **The release flow deviated from `RELEASING.md`'s checklist — record it, do not "repair" it:** pull request #21 (`feature/1.2.0` → `main`) was **merged
  before the build** (the maintainer's instruction in chat, 2026-10-09; merge commit `ec6d163`, after both CI checks passed), then `release/1.2.0` was branched
  from `ec6d163` and pushed, and `scripts/release.sh 1.2.0` built `dist/1.2.0/` from it (2026-10-09 08:32 — the maintainer's own act). So the **build commit
  is `ec6d163`, already on `main`**, and there is **no pull request for `release/1.2.0`** yet (`gh pr list --head release/1.2.0 --state all` is empty). The
  capture and this analysis are committed on `release/1.2.0` after the build commit (C3) and need their **own pull request to `main`**, merged with a merge
  commit (D1 for these commits only); the tag of D2 goes on `ec6d163`. **B3 is not done**: `~/opencontrol-1.2.0-tested` does not exist (measured) — tell
  the maintainer it is still to do, after `sha256sum -c` in `dist/1.2.0/`.

**Known pitfalls (from `CAP-063` … `CAP-071` — check them, do not assume):**
- With the `H4 with linux header` encapsulation `bluetooth.addr` is empty. Scope every filter by the Buds' connection handle(s), taken from the Connection Complete
  events **of each file** (`AGENTS.md` §13.1).
  - Handles restart after Bluetooth off/on (and after the user switch of 15:40).
  - Show each filter matching a known frame.
  - Other devices may share the log — especially CAP-072-btsnoop_hci1.log.last (18 hours).
- **RFCOMM DLCIs are session-local:** MAESTRO is 0x02 or 0x03, the Message Stream 0x04 or 0x05.
  - `python3 -I scripts/pwrpc_decode.py <log>` (`--handle 0x…` per connection) reads DLCI 2 and 3 and reassembles packets split over RFCOMM frames.
    **Check first that it decodes `GetHardwareInfo` and its `RESPONSE` field 7** (the official app's in `CAP-036` 1415/1423 as the positive control); if it
    does not, decode those frames by hand (the method of `ai-sessions/0082` RESULT §A.2) — do not change the script in this session.
  - Still check the CRC-32 of each pw_hdlc frame yourself where a claim depends on it (the method of `ai-sessions/0074` §A.1).
  - `scripts/message_stream_tally.py` tallies ANC `Set`/ACK/NAK.
- **The Message Stream claims:** the app starts its claim with `08 11`/`08 12`, or with its battery/Find sequence. Play services' claim starts with
  `03 08 00 02 01 25`; none is expected.
- A filter on `data.data` does not see frames a dissector has claimed (HFP, AVDTP). Use `frame contains` or the protocol's own field (`AGENTS.md` §13 step 8).
  - AVRCP is `btavctp or btavrcp`; HFP is `bthfp`/`btrfcomm` on its own DLCI; SCO/eSCO (the call) is `bthci_evt.code == 0x2c` (Synchronous Connection
    Complete) and `btsco` — check the field names against your `tshark` version.
  - A `tshark` exit status ≠ 0 is an error, not "0 frames". Sets need commas: `frame.number in {a,b}`.
- `field 17` is `sint32` (zigzag); `field 16` holds five little-endian floats; **field 29 is 1 = off / 2 = on**, never 0/1 (ADR-052); **field 28 is "Earbuds
  replaced" / "Bud return", field 27 "Other alerts"** (§4.5.8).
- The Settable byte is **not per bud** and is as old as the last `Notify` (ADR-049, ADR-059); the Worn line changes only after a claim (`08 11`) or a
  `Notify` — a pull on the Connection tab, not by itself.
- The app logcat is in UTC, and the system log, the shell log and the exports are in local time (CEST, UTC+2). Never mix them without the measured offset.

**Privacy (standing rule, ADR-037).** Check **every** frame of both films, and the screen recording's audio track, for personal data:
- notifications, messages, contact names, e-mail addresses; **the incoming call's caller** (18:02:25 — the number or the name on the screen recording's
  incoming-call UI and on the camera film);
- Wi-Fi and Bluetooth device names; Android's settings and pairing screens; the Quick Settings tiles;
- **a burned-in street-address overlay** (earlier camera films carried one);
- faces other than what the maintainer chose to film (head and ears);
- voices or conversation in the background, and what the maintainer said that is not an observation;
- the serial numbers: the Info tab shows all three in full on both films at BH-2 and BH-18 — **ADR-058/ADR-010: first 4 + last 2 characters in any text**;
  decide with the maintainer whether the films may be committed with the serials readable (they are the Buds' component serials, not personal data, but
  the maintainer decides), and never write them unredacted into any document.

Inventory what the system log holds (§2's row), and propose either committing it whole, committing only an extract (with the exact command), or not committing
it. **Ask**: earlier choices do not carry over. If a blur or a cut of a film is wanted, propose the exact `ffmpeg` command, the ranges and a before/after sample.
Never write the Buds' full address or serial into a document (ADR-010).

---

## 3. Leads and context — verify each before relying on it

1. **The update (P1, BH-1, BH-2; U1–U3, V1–V3):** 1.1.1 → 1.2.0 in place at 17:29, on film, on a 1.1.1 that was itself fresh (17:24). Did 1.1.1 show
   "1.1.1, build 86a6fb3" before, and 1.2.0 "build ec6d163" without "-dirty" after? Were Dark mode On and Debug mode on set in the fresh 1.1.1 on film, and
   kept by the update? Was the Companion Device association kept, or made again (the picker on film)? Did the app open its session by itself (ADR-044)? Was
   anything opened between the install and BH-1 (P9)? Does Controls show Conversation detection as its own card and **no** Case sounds card; does gear →
   Settings show the Case sounds card between Dark mode and "Use different Buds" with its (i) sentence?
2. **The thirteen requests and `GetHardwareInfo` (BH-1, BH-5 … BH-7, BH-18, BH-20, BH-22; V1, V2, V16; ADR-058):** per session (each announcement), every
   `ReadSetting` in the order `4:16`, then 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29, `SubscribeRuntimeInfo`, then **one** `GetHardwareInfo` **last**,
   byte-identical to `CAP-036` 1415 (channel 21) / `CAP-024` 801 (channel 19), answered with `RESPONSE` field 7 and its three length-14 strings; **no retry,
   no second `GetHardwareInfo` in a session, none outside Connect/re-open** (the filter's positive control: the official app's in `CAP-036`). The strings
   compared (redacted) with the Info tab on film in the order Case, Right bud, Left bud, with the "(from the Buds, HH:MM:SS)" time = the answer's time; the
   same three strings after every re-open; "Not read …" with the reason while Bluetooth is off. The latency of the answer and whether the request delays
   anything (the "—" of S6, the battery card). **The fixture swap:** `TODO.md` §5 asks, after `CAP-072`, to replace the redacted `HardwareInfoFixtures` by this
   capture's real answer bytes — that is a change under `android/`, so **propose** the exact frames (file, frame number, raw bytes with the serials X-ed out the
   same way) in the RESULT for a later FEATURE session; do not make it.
3. **The worn line (`INEAR-006`, ADR-059; BH-3, BH-5 … BH-10, BH-13; V4–V9, V12):** the `INEAR-006` table — one row per reading: time, the ears on film (head on
   the **right** of the frame = **Left** bud; cross-check with the runtime-info `6.2` Left / `6.3` Right charging flags and the Settable byte), the claim
   (`08 11`) and its `Notify` with the Settable byte, in-ear field 2, both charging flags, the Worn text on the screen recording with its "(checked HH:MM:SS)".
   Each of the six readings of `WornReading.kt` seen or not: "Both buds in the case" (BH-3), "Probably worn" (BH-5, BH-6, BH-10), "Not worn" (BH-8, BH-9's
   second reading), "Worn: unknown — in-ear detection is off" (BH-9), "—" (BH-18's gap, BH-20, BH-22), "not read"; the `CAP-064` ≈ 28 s case (BH-13: `e8` →
   `00` with the count said aloud). A "Probably worn" with both buds on the table beyond ≈ 30 s is a result for ADR-049 item 3 / ADR-059 (a **proposal**), not a
   fault of the app; "Not worn" with a bud in an ear, or "Probably worn" with in-ear detection off, is a defect.
4. **Lead L-1 (`INEAR-005`; BH-5, BH-7; V5, V6):** only the Left out ⇒ 19 (`PROTOCOL.md` §2.2a 🟢); with both worn on 19 the **Left** out of the ear ⇒ Buds `DISC`
   + 21 (🟡) — this time with the head in view. The `DISC` direction, the announcement, the thirteen requests on the new channel. A status proposal for
   §2.2a if the evidence holds; a counter-example is a central finding.
5. **"Changed by the Buds" and the press-and-hold observation — the second main question (`AncModeCause.kt`, `BudsRepositoryImpl.kt`; BH-10 … BH-12, BH-17;
   V9–V11; ADR-032, ADR-045, ADR-049).** The maintainer heard every press-and-hold change the mode, and never saw it in the app. Build the complete table of
   **every** press-and-hold of the run (both films; the audio track for the Buds' mode chime and what was said): time, bud, the mode before and after
   (audible / on the wire / on the screen), whether DLCI 0x04 was open at that second (the RFCOMM `SABM`/`UA`/`DISC` timeline of the Message Stream channel
   around it, and the export's "RFCOMM channel 0x04 connected" / "on-demand channel 0x04 released" lines), every `08 11`/`08 12`/`08 13` within ± 10 s, the
   app's log lines, the ANC card's mode and the (i) text on the screen recording at the hold and after the next claim. Then test each hypothesis, and label
   each:
   - (a) **the channel was closed — by design.** `DECISIONS.md` ADR-032: the app claims DLCI 0x04 only for its own action and **releases it ≈ 1.5 s later**
     (`MESSAGE_STREAM_LINGER_MS`, `BudsRepositoryImpl.kt` ≈ `:1307`/`:1361`); ADR-032 item 5: values from DLCI 0x04 are only as fresh as the last claim. A
     `Notify` the Buds send while no claim is open cannot reach the app — there is no socket. `CAP-045`'s unprovoked `Notify`s (1583, 1755, 1818 — the fixtures of
     the 1.2.0 feature) were captured with the **official app holding the channel**; `ai-sessions/0082` §A.1 designed and unit-tested the cause logic with an
     open channel and did not weigh ADR-032's release, and the skeleton's BH-11 expectation ("the mode changes without a tap") was therefore unachievable on
     this phone from the start. If this holds, say so plainly: it is a planning error of `ai-sessions/0082` and a design limit, not a defect of the codec or of
     the Buds — and `CHANGELOG.md`'s / the release notes' description ("when the Buds report a mode the app did not ask for") promises something that can only
     happen inside a claim window. Check whether the Buds send **anything** at a press-and-hold on a channel the app does hold (DLCI 0x02's runtime-info
     stream, a settings-stream frame) — that would be an alternative signal (`PROTOCOL.md` §4.1, §4.5; `CAP-021` §4's field-12 frames; `CAP-027` §4).
   - (b) the channel **was** open (a hold inside a claim's 1.5 s, or during the Connect snapshot) and a `Notify` arrived, but `causeOfNotify`
     (`BudsRepositoryImpl.kt` ≈ `:356`) classed it `READ` or `SET_BY_APP` (`getPending`/`setPendingMode` still set), so the mode changed with no line, or did
     not change on screen.
   - (c) a `Notify` arrived and `emitAncMode` ran, but the UI did not recompose (`AncScreen.kt`, `MainActivity.kt`'s collection) — the export and the app logcat
     vs the screen recording decide.
   - (d) the next claim's `Get` answered with the new mode: the app then showed the new mode with "read HH:MM:SS" (correct per §A.1: "the app cannot know when
     it happened") — did the maintainer look at the app only **between** claims, so that the last-known mode (ADR-032 item 5) stayed on screen? What did the
     BH-12 tap/pull/tile show?
   - (e) the Buds did not send a `Notify` to the phone at all for a hold (the firmware notifies only a connected Message Stream client; with none, nothing):
     then the HCI log holds no `08 13` near the hold even if the channel were open — distinguish (a) from (e) with a hold that fell inside an open window, if
     any; otherwise say that this run cannot tell them apart and what capture would (the official app's channel held, as in `CAP-045`, against this app's
     1.5-s window).
   - (f) the tile or the notification: which process answered, and whether a hold while the tile was claiming did anything.
   Give the answer with its evidence. **Then the options, for the checkpoint (proposals only, each a drafted ADR or ADR-032 Update, no number):** keep ADR-032
   and reword the feature honestly (the (i) line can only appear for a change inside a claim window; a known issue / a wording change in `CHANGELOG.md`, the
   release notes, `README.md`); or hold DLCI 0x04 **while the ANC tab is on screen** (foreground-only, released on leaving the tab or the app — ADR-032's
   "no background claim" kept, its "short claim" changed; contention with Play services returns on phones that have it, so say what the user without Play
   services and the user with it would each get); or hold it only when no Play-services client exists (how would the app know, without GMS APIs?); or a
   Refresh hint ("pull to see the Buds' current mode"). Give each option its pros and cons and the hardware check that would verify it. Do not implement any
   of them.
6. **Every write, byte for byte (BH-14 … BH-17; V13–V15; the reference table):** the two case-sound switches from gear → Settings (`4:{28:0}`/`{28:1}`,
   `4:{27:0}`/`{27:1}`), conversation detection from Controls (`4:{22:0}`/`{22:1}`), then Balanced, a band, *Read EQ again* (H5 ★), balance Right 4 / Centre,
   mono, Volume EQ, touch, press and hold, the mode list without Off, head gestures, Multipoint, in-ear detection (BH-9), and Find (BH-19). Per write: the
   request bytes against the reference (byte-identical / derived / first capture), the empty `RESPONSE` or the ACK, the control moving only after it, the (i)
   line. The read-back after Disconnect/Connect (BH-18). Did the Buds end in the state `TODO.md` §4 asks for (Balanced, the mode list without Off,
   conversation detection on, both case sounds on)?
7. **The case-sound observation — the maintainer's main question (BH-14; V13; `CASE-001`, `CASE-002`; `PROTOCOL.md` §4.5.8).** Build the complete table of
   **every** bud-into-the-case event of the run (both films, every section — not only BH-14): time, which bud, from where (ear or table), the lid, the state of
   "Earbuds replaced" at that moment (the last **ACKed** `4:{28:x}` before it, and the last `ReadSetting 4:28` answer), the session state (DISC/re-open under
   way? ADR-044; the Buds' `DISC` timing against the bud's entry), the runtime-info charging flag's arrival, what the audio track holds at that second (a
   chime? use `ffmpeg` `silencedetect`/`astats`/a spectrogram on the screen recording's track; the Find ring of BH-19 and the music are the positive controls
   for what the microphone picks up), and what the maintainer said. Then test each hypothesis against that table, and label each:
   - (a) the maintainer's: a brief loss of the phone ↔ case link when a bud goes from the ear into the case, so that the case does not have the setting the
     app just wrote — what would "the connection between the phone and the case" be on the wire (the ACL is to the Buds; the case has no link of its own —
     `PROTOCOL.md` §1/§4.3, `ARCHITECTURE.md`), and what does drop (the MAESTRO `DISC` of ADR-044's cause, the Message Stream, A2DP)?
   - (b) the write had not been acknowledged, or was sent on a channel that closed right after (the `OK` timing vs the bud's entry);
   - (c) the semantics of field 28 are not "a chime on every bud return" — the official app's own reading (`fxb.java` case 28 "received bud return sound
     setting value"; `CAP-024` §4–§5, `CAP-058` §6: was a chime ever recorded with the official app?), the Buds' firmware may chime only on a return from an
     ear, only with the lid open, only when both are in, or after a delay;
   - (d) the chime is the "Other alerts" family (field 27) or another sound (a low-battery or a charging sound), not "Earbuds replaced";
   - (e) the app showed a stale switch (the read-back after a re-open: `4:28`'s answer vs the switch on the screen recording);
   - (f) hearing/timing: the chime is quiet, the music played, the microphone is the phone's — the audio track decides what was audible.
   Give the answer with its evidence and the exact experiment that would settle what stays open (a **proposed** capture, no skeleton in this session: e.g. the
   official app's "Bud return" on/off with the same bud movements and a microphone at the case). If a defect in the app is found, Phase C task 14 applies.
8. **The ANC modes during playback (BH-10, BH-16; the maintainer's statement):** each `Set` with its ACK and the Buds' `Notify` while A2DP was streaming (AVDTP
   start/suspend, AVRCP play state); what the audio track shows.
9. **The incoming call (18:02:25; not in the plan):** the HFP state machine in the system log (`HeadsetStateMachine`, SCO), the SCO/eSCO connection in the HCI
   log, AVDTP suspend; what the Buds did (the call answered on a bud? declined?); what the app's session did (a `DISC`? a re-open? nothing?); whether any write
   or claim happened during the call; how long the call lasted; what is on each film (privacy). This also answers whether Do Not Disturb was on (P7 ★).
10. **Robustness (BH-20 … BH-22; C8, C9, J4, K1, K2, R6; V18):** both buds into the case and out: the cause text, ready again by itself; Home for 2 minutes;
    the export, then Bluetooth off/on at 18:18:32/18:18:45: "Bluetooth is disabled.", the serials "Not read …" while off, ready again and read again after.
    For each: the session-end table as in `CAP-071-FINDINGS.md` §6, the cause shown and logged, and whether ADR-044's rules held (one attempt per event, the
    10-s guard). The two `bugreportz` SIGQUITs discounted (P6 note), each matched to its `dumpstate`.
11. **C12 and S12, done in film 1 (BH-23, BH-24; V19 in part):** the early tap during a re-open — "The setting was not changed: The app's channel is being
    reopened — try again in a moment." on the screen recording and **no `WriteSetting`** afterwards (if the tap came after "ready", say so: C12 stays open);
    the export across a rotation — the "save as" dialog rotated on the screen recording, the toast, the saved file's content (which of the four exports it is).
    T11 (the three dumps) is film 2.
12. **No behaviour change outside the five 1.2.0 items (the regression against `CAP-071` / 1.1.1):** the Connect-time reads and their latencies, the request bytes
    of every write, the `GetSoftwareInfo` and `SubscribeRuntimeInfo` sequences, the Message Stream claims, the timing (ADR-044's guard, the re-open delays),
    the screens (cards, texts, order, colours). Any difference is a central finding: say whether 1.2.0's code, the OS update (`CP3A.260905.009` →
    `CP3A.261005.005`) or the fresh install explains it.
13. **Privacy** (§2): the caller, the serials, the audio, the system log.
14. **The 1.2.0 known issues** in `CHANGELOG.md` and `dist/1.2.0/release-notes.md`: does this run remove any, keep each, or add one?
15. **Battery and case:** every value shown on film against the wire value and its receive time (`03 03`, runtime-info 6.1/6.2/6.3), and the "last connection"
    marks; the Worn line next to them.
16. **Anything new or wrong:** NAKs, pw_rpc error statuses; Safe Mode (expected off on `release_5.203`; re-check the firmware string ×3); "Late WriteSetting
    answer dropped" / "Maestro request held" lines; any app frame on DLCI 0x08/0x0a (there must be none); any request outside the user's actions; any
    `StrictMode`, "A resource failed to call close" or other warning from the app's UID; anything that behaves differently in a secondary user without a
    user-0 install.
17. **The extra things the maintainer showed on film:** identify each, and say what it demonstrates.
18. **The testing burden — a proposal for shorter, simpler runs.** From this run and `CAP-068` … `CAP-071`: minutes and steps per section; which steps ever
    found something on hardware and which were only ever confirmed by the wire being byte-identical to a fixture the unit tests already check; which checks a
    `uiautomator` dump, an export grep or the debug log can make **without** the maintainer doing anything on film; what the maintainer had to remember (say
    the step aloud, pull after every wear change, the head in view, the exports, the bugreport before each Bluetooth off, Do Not Disturb, …) and which of those
    the procedure or the tooling could take over (a printed one-page run card; a `scripts/` helper that pulls the HCI log and the exports and names the files —
    a **proposal** with its own ADR-free scope; the app's own debug log already timestamps every write). Propose two tiers: a **minimal release run** (what a
    release *must* show on hardware; target ≤ 15 minutes) and an **extended run** only when the wire or a protocol-facing behaviour changes; and which open
    `TODO.md` §2 items should be dropped, folded into unit tests, or kept. Put it in the RESULT as a proposal and ask about it at the checkpoint; it changes
    `APP_TESTPLAN.md`/`RELEASING.md` only with approval.

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename, mode change or `git add`)

1. Create the RESULT file (§0). Record:
   - `git log -1`, `git branch --show-current`, `git status --short`;
   - `git fetch && git log --oneline HEAD..origin/main` and `HEAD..origin/release/1.2.0`;
   - `gh pr view 21 --json state,mergeCommit` and `gh pr list --head release/1.2.0 --state all`.

   **Work on branch `release/1.2.0`** (`RELEASING.md`, Release checklist C3: the capture and its analysis are committed after the build commit on the release
   branch; the deviation of §2 is recorded, not repaired). `android/domain/bin/` is an untracked build-output directory: never stage it.

   Confirm the `planned` row of `CAP-072` in `id_registry.csv`, and the Group BH section and Capture Index row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`. Identify every
   file with `capinfos`, `ffprobe` and `sha256sum`, re-check every value in §2, and **check whether film 2's files are present** (§1 item 2).
2. Privacy check of both films, the screen recording's audio and the system log (§2). Summarise the result.
3. Plan the migration (ADR-037, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9):
   - rename the folder to captures/CAP-072-YYYY-MM-DD_HH-mm-ss_HH-mm-ss-Group_BH/, from the first and last clock time on film (say which film and which clock;
     if film 2 is made on another day, propose how the folder is named — the convention names one capture's first and last time — and ask);
   - `chmod 644` on the seven executable files;
   - the `sha256sum *` file the skeleton asked for;
   - the registry row and the Capture Index row;
   - Git LFS for every capture file;
   - what to do with the system log (§2): whole, an extract (the command), or local only.

   **Ask the maintainer (`AskUserQuestion`)** about the privacy outcome and the migration plan together, before moving anything.
4. Execute only what was approved.
   - Before moving anything, compare checksums of a fresh listing of the source against the destination. Prefer a plain `mv`. Never chain deletes, and never
     `rm -rf` based on an earlier listing.
   - Update every reference to the placeholder folder name: `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TODO.md`, `APP_TESTPLAN.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`,
     `README.md`, `RELEASING.md`, `_sidebar.md`; `grep -rn "CAP-072-yyyy"` outside `ai-sessions/`.
   - A reference under `android/` (e.g. a KDoc) is recorded for a follow-up session, not edited.
   - Earlier `ai-sessions/` files are history: leave them as written. (`ai-sessions/0080`'s lesson: this prompt names the placeholder folder without
     backticks so `scripts/lint_docs.py` does not flag it after the rename — keep it that way.)

### Phase A — the films, in full

5. Scan **both** films at a fixed interval of ≤ 2 s plus scene changes. Then narrow every transition to ≤ 1 s, and to a single frame where a claim depends on it.
   The screen recording is the source for everything on the phone's screen; the camera film for the case, the buds, the ears and the phone's handling. Cover:
   - every lid opening and closing; every bud taken out of or put into the case or an ear, with the head-side mapping;
   - every tap: Connect, Disconnect, a switch, a pull, a slider, a preset, *Read EQ again*, the gear, Settings, Debug, Info, Export, the tile, the (i)s;
   - every tab change and swipe, every rotation, every theme change, every Bluetooth toggle, the incoming call;
   - every Android screen: settings, Quick Settings, the shade, the "save as" dialog, the installer, the launcher, the in-call screen;
   - every toast and dialog, every Home and return, every "—" and (i) dialog, every Worn-line text with its time;
   - every action not in the plan.

   Read every visible on-screen text: switch positions, (i) lines, error texts, "Control channel", the Info build line, the three serial lines (redacted in the
   notes). Crop and zoom the ear, case and card regions.
6. The **audio track** of the screen recording: establish what it holds (§1 item 3); transcribe the spoken observations with their times; mark the chimes, the
   ring, the music and the call. The camera film's empty audio stream: record the `ffprobe`/`ffmpeg` evidence.
7. Measure the film ↔ phone clock offset at the start **and** at the end of each film: a status-bar minute flip against the film's frame time, at ≥ 4 fps,
   never a single point; and the offset between the two films. Measure also the app-logcat (UTC) ↔ phone, the system-log ↔ phone and the export ↔ phone
   offsets, each from an event that is in both.
8. Rewrite **CAP-072-EVENT-NOTES.md** in the `CAP-071-EVENT-NOTES.md` layout:
   - header status;
   - Log Metadata: the phone, the GrapheneOS build (and that it is newer than `CAP-071`'s), the user and the absence of Google Play with the P0 output, the
     P1/shell outputs copied unchanged and their reading (the reinstall of 17:24), the OpenControl build before and after the update from the Info tab, the
     firmware, other devices, the film/log ranges per file, the clock offsets, and the wear mapping with its cross-check;
   - a capture-integrity pre-flight: all four HCI logs incl. the prefix proof and the user-switch rotation, the four exports, the app logcat and its two
     processes, the system log, the shell log, the two films and the empty audio stream, and the pending film-2 files;
   - the video review method, incl. privacy and the transcription;
   - an Event Timeline: phone time, action, actor, step ID, V-/test-plan ID, registry Test-ID, what was said, and evidence with its file;
   - a step-mapping table (incl. "pending (film 2)") and the analysis checklist;
   - the skeleton as an unchanged appendix.

   **Traceability (`AGENTS.md` §13.7):** every skeleton step and every Test-ID it names appears in the timeline, or is explicitly marked "skipped", "pending
   (film 2)" or "not identifiable". Every repetition and every extra action is its own row.

### Phase B — the HCI logs and the other logs, in full

9. Follow `AGENTS.md` §13 for **all four** HCI logs.
   - Establish the overlap first, and decide per packet range which file is the source.
   - Pre-filter by the Buds' handle(s), then take the full RFCOMM inventory: `SABM`/`UA`/`DISC`/`DM`, the direction, per ACL session and multiplexer side.
   - Then decode **every** Buds packet:
     - MAESTRO: every `GetSoftwareInfo` with its channel and entries; every `ReadSetting`/`WriteSetting`/`SubscribeRuntimeInfo`/`SERVER_STREAM` with field,
       value and status; **every `GetHardwareInfo` and its `RESPONSE`** (field 7's three strings, redacted); the CRC-32 per frame;
     - the Message Stream per claim (the app vs anything else; ANC `Get`/`Set`/`Notify` with the Settable byte, battery, Find, SASS);
     - DLCI 0x08/0x0a (who opens them, every message);
     - HFP incl. the call (SCO/eSCO), AVDTP, AVRCP;
     - every ACL and LE event with its reason code;
     - the Bluetooth off/on cycle and the user-switch boundary.
   - CAP-072-btsnoop_hci1.log.last (the 18 hours before the run) is analysed as fully as the rest for the Buds' traffic: it is the "before" (the 1.1.1 app of
     `CAP-071` still installed, the tile process of 15:40:36).
   - Every negative needs its command, exit status and a positive control.
10. Read the four exports, the app logcat and the shell log **line by line**, and the system log in the ranges of §1 item 4. Put each relevant line on the
    timeline, with the phone time after the measured offsets and the file named.
    - For every filmed action, write the exact frames and log lines: what the app sent, on which channel, what the Buds answered, what the app logged, what it
      showed, and what was said.
    - Where no log covers a stretch (18:18:33–18:18:45; after 18:23:12 in the HCI logs; after 18:23:05 in the exports), say which evidence does, or that none
      does.
11. Build the tables the findings need:
    - the thirteen requests per session, with `GetHardwareInfo`'s position, bytes, answer and latency, against `CAP-071` (twelve) and the references;
    - every write against the reference-byte table;
    - the `INEAR-006` table (lead 3) and the channel per announcement against the buds out/worn (lead 4);
    - the "Changed by the Buds" / press-and-hold table (lead 5): every hold, the channel state at that second, every `Notify`, provoked or not, and the mode
      and (i) text on screen after it and after the next claim;
    - **the bud-into-case table of lead 7**, with the audio evidence;
    - the call table (lead 9);
    - the session-end and process table (PIDs 6994, 11717, 12137 and any other; no user-0 process expected);
    - every battery value shown against the wire;
    - the Message Stream claims with their first phone message;
    - the four exports mapped to steps, with the serial/MAC grep;
    - the 1.1.1 → 1.2.0 comparison of lead 12.

### Phase C — findings, the case-sound answer, the release verdict and the proposals

12. For every lead of §3, give the answer with its evidence: frame numbers with their file, film times (which film), log lines, `file:line` in the app sources.
    Label each 🟢 FACT / 🟡 HYPOTHESIS / ⚪ ASSUMPTION / 🔴 OPEN QUESTION, with the experiment that would settle any non-FACT.
13. Write **CAP-072-FINDINGS.md** in the `CAP-071-FINDINGS.md` layout.
    - Label every conclusion (`AGENTS.md` §15, `PROJECT_RULES.md` §1). Give every hex decoding with its command and raw bytes (rule 4a). Use at most one status
      banner (rule 9a). No serial unredacted, no MAC.
    - Include:
      - the re-test verdicts, with the skeleton's "Refuted if" applied literally, and "pending (film 2)" where it applies;
      - the five 1.2.0 items, each: works / does not / partly, with the evidence;
      - **the case-sound section** (lead 7) with its table and the hypotheses weighed;
      - the call section (lead 9);
      - what works, what does not and what goes wrong, per protocol;
      - the 1.1.1 → 1.2.0 comparison;
      - what is different with the app in user 10 only.
    - If a finding contradicts an existing 🟢 FACT or ADR, say so and draft the correction as a **proposal**. Do not edit that FACT or ADR.
14. **The release verdict for 1.2.0** (`RELEASING.md`, Release checklist C4). Classify every defect, with the evidence:
    - **heavy:** a crash, a wrong or unacknowledged write, a write applied before its answer, Safe Mode failing on the verified firmware, a session lost without
      recovery, a setting lost by the update, a `GetHardwareInfo` retried or sent outside Connect, a serial or MAC in an export, a wrong Worn reading
      ("Not worn" with a bud in an ear; "Probably worn" with in-ear detection off), a "Changed by the Buds" line after the app's own tap, or anything risky for
      the Buds;
    - **minor:** a known issue for the release notes (e.g. the case-sound observation if it is the Buds' behaviour, not the app's; the ≈ 28 s case).
    - **The press-and-hold result is classified explicitly** (lead 5): if "Changed by the Buds" cannot fire on this phone by design (ADR-032), that is not a
      wrong write and not a crash — but 1.2.0's release notes and `CHANGELOG.md` announce it. Offer the maintainer the choice: *release with the feature
      reworded as a limit* (the wording drafted), or *fix first* (one of lead 5's options, with its drafted ADR and re-test); do not decide it alone. If
      instead a `Notify` did arrive and was dropped or mis-classed, it is a defect: **heavy** if the mode shown was wrong after a claim, **minor** if only the
      (i) line was missing.

    Then give one of:
    - *release the tested build as 1.2.0*, with the known issues worded for `CHANGELOG.md` and the release notes — **conditional on film 2** if BH-25/BH-26
      are still pending: say what film 2 must show and what would overturn the verdict (T11's "—" texts, the force-stop, the re-open after Bluetooth on);
    - *fix first*: what, and the targeted re-test. The same version may be rebuilt while nothing is published;
    - *re-run*: which steps, and why.
15. **Improvements (proposals only, no app change).** For every defect, give:
    - the cause;
    - the proposed change: the file and function, the behaviour, the guardrails;
    - the unit test that would have caught it, with real-byte fixtures from this capture (`AGENTS.md` §11);
    - the re-test step.

    Add **the fixture-swap proposal** (lead 2) and **the testing-burden proposal** (lead 18) as their own sections. Anything that changes an ADR's decision is a
    drafted ADR (no number).
16. **Do not modify any file under `android/`, `dist/` or `scripts/`.**

### Phase D — checkpoint, documentation, finish

17. **Checkpoint: stop and ask, in this chat, via `AskUserQuestion`.**
    - Ask one question per decision. Give each option its pros and cons, mark one "(Recommended)", and put the exact draft text of any FACT, ADR, `PROJECT.md`,
      `CHANGELOG.md` or `README.md` change in the preview (memory: "Approvals: confirm in chat").
    - Cover at least:
      - the release verdict (and its film-2 condition);
      - the known-issues wording;
      - the case-sound conclusion and the proposed experiment;
      - the press-and-hold conclusion: the cause, and which of lead 5's options (reword as a limit / hold the channel on the ANC tab / another) — each with
        its drafted ADR text in the preview;
      - lead L-1 (`PROTOCOL.md` §2.2a: a status proposal, if the evidence holds);
      - the `INEAR-006` result for ADR-049 item 3 / ADR-059 (an Update proposal, if the evidence holds) and ADR-058's Update (the serials on hardware);
      - the testing-burden proposal (which tier, which plan changes);
      - the open `TODO.md` §2 items this run did not settle, and the fixture-swap item;
      - the untranscribed speech, if any;
      - the system log's fate and the serials on the films (if not settled in Phase 0);
      - any other FACT or ADR change.
    - **No new capture skeleton is written in this session.**
    - Record the answers verbatim.
18. Apply only what was approved, and only to documentation:
    - `PROJECT.md` (the 1.2.0 status, the Definition-of-done evidence cells this run fills); `PROTOCOL.md`/`DECISIONS.md` (approved changes only, as dated
      Updates with a process note citing this chat; ADR-060 stays a draft); `ARCHITECTURE.md`, only where a hardware result corrects it;
    - `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (the Group BH run note, the Capture Index row — "analyzed" or "analyzed — film 2 pending");
    - `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the evidence cells of the Test-IDs exercised, incl. `INEAR-006`; pointers only);
    - `APP_TESTPLAN.md` (the Summary row for section V; a step that proved unworkable; the testing-burden changes only if approved);
    - `id_registry.csv` (`CAP-072` → analyzed, with the real times; "film 2 pending" if so);
    - `TODO.md` (done items removed, open items added; §2, §4 and §5 in particular);
    - `CHANGELOG.md`: the `[1.2.0]` block's Known issues if the verdict changes them, **not** its date;
    - `scripts/release_notes.template` and `dist/1.2.0/release-notes.md` are under `scripts/`/`dist/`: propose a wording change, do not make it;
    - `README.md` (status, capture counts; not "Latest release");
    - `ai-sessions/INDEX.md` (the 0083 row).

    Run `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py`; the lint must exit 0.
19. Finish the RESULT.
    - Put the plain-language answers first:
      - the release verdict and its condition;
      - what works and what does not on 1.2.0 (the five items, one line each);
      - the case-sound answer;
      - the press-and-hold answer (why the app did not show the Buds' change, and what to do about it);
      - what the incoming call did;
      - whether anything changed compared with 1.1.1;
      - how the runs can become shorter and simpler;
      - what the app should change next.
    - Then give the step-mapping results, the tables of Phase B, the transcript, and the external sources (URL plus quoted sentence).
    - If the verdict is "release" (or "release once film 2 confirms"), give the maintainer's next steps from `RELEASING.md`'s Release checklist, **as
      instructions, never executed**, adapted to the deviation of §2:
      - B3 first: `cp -a dist/1.2.0 ~/opencontrol-1.2.0-tested`, after checking `sha256sum -c` still passes;
      - film 2 (BH-25, BH-26) and its analysis, if pending;
      - a pull request from `release/1.2.0` to `main` for the capture and analysis commits (A5's `gh pr create --base main --fill`, with the Release checklist
        in its body and the B1 hash `ec6d163`), both CI workflows green, then D1: merge with a **merge commit**;
      - D2: tag **the build commit read from the Info tab** (`ec6d163`) — it is already on `main`;
      - D3: the files from the kept copy;
      - D4, and E1–E4.
    - End with **"Deferred documentation"** (each item also added to `TODO.md`) and **"Commits"** (hashes, or "not committed — reason"), per
      `AI_SESSION_LOG_PROCEDURE.md` §9 and §4b. Set the Status per §4 (`partial — resumed` if film 2 is pending and this RESULT is to be continued; say so).
20. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes:
    - commit on branch `release/1.2.0`, with Conventional Commits, one commit per concern (capture/LFS, docs, session files);
    - give each commit a *why*, and end it with the attribution line from the session's system reminder;
    - `git fetch` and rebase before pushing; never force;
    - run `git check-attr filter` on every capture file (`lfs`);
    - put **no file under `android/` in any commit**: `git diff ec6d163..HEAD -- android` must stay empty;
    - stage nothing from a build directory (`android/domain/bin/` included), `dist/`, `android/.kotlin/`, `.vscode/` or `__pycache__`.

    A large LFS push (the films are ≈ 3.1 GB together) can drop the SSH connection after the upload. Check `git log origin/release/1.2.0 -1`, and push again if
    needed. **Creating the pull request** for `release/1.2.0` is a separate question to the maintainer (show the body first: the Release checklist with B1 =
    `ec6d163`, B2 ticked, C1–C3 as far as the evidence goes, a link to this RESULT); never merge it.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **No app changes and no release actions.**
  - Nothing under `android/`, `dist/` or `scripts/` is modified, and no build is made.
  - `scripts/release.sh` is not run, and no pull request is merged.
  - No tag is created or pushed; no GitHub release is created or edited; no asset is uploaded.
  - Publishing is the maintainer's own act (`RELEASING.md`).
- **Approvals only in this chat.** Never promote, demote or correct a 🟢 FACT, and never write or update an ADR, without the maintainer's approval given in this chat
  (`AGENTS.md` §6). Model agreement is not approval. ADR-060 stays a draft.
- **Evidence.**
  - Zero creativity with hex (`AGENTS.md` §13.6).
  - Every claim needs a frame number (with its HCI file), a log line (with its file), a `file:line` or a film timestamp with its film (`PROJECT_RULES.md`
    rule 4a).
  - A negative needs its command, its exit status and a positive control (`AGENTS.md` §13 step 8).
  - What the maintainer heard or saw is their observation, unless the wire or the audio track shows it.
- **What was done, not what was planned.** The skeleton is the plan; the films and the logs are the record. Never fill a timeline row from the skeleton's "Expected"
  column.
- **Privacy.** No full MAC address or serial in any committed text beyond what ADR-010/ADR-058 allow (first 4 + last 2 characters), and no personal data from the
  films, the audio or the logs — the caller of 18:02 above all. The system log is committed only in the form the maintainer approves.
- **Scope.** Stay within `PROJECT.md`. Anything new (a feature, a permission, a dependency, a new wire request, a `scripts/` helper) is a checkpoint question with a
  drafted proposal, not a silent addition.
- **Subagents.** A subagent may only read and report. Every write is done in the main session and verified; re-derive any number a subagent reports.
- **Files.**
  - Before deleting, moving or renaming any file, diff a fresh listing of the source against the copy (checksums).
  - Never `rm -rf` from memory of an earlier listing, and never use wildcard deletes. Ask before any delete.
  - Do not run any tool that writes into `dist/`.
- **Commits.** Commit and push only after the maintainer confirms the final summary (task 20).
- **No new capture skeleton.** Do not create a new `CAP-NNN` folder, skeleton or `planned` registry row in this session. The proposed case-sound experiment and
  the open film-2 steps go to the findings and `TODO.md`; film 2's files, when they arrive, belong to `CAP-072`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0083_CAPTURE_PROMPT_2026_10_09.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0083_CAPTURE_PROMPT_2026_10_09
