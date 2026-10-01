# 0063_CAPTURE_PROMPT_2026_10_01.md — Full analysis of CAP-066 (Group BB, the first hardware run of the `ai-sessions/0062` build: ANC `Get` before every `Set`, cut-off answers, the settings menu, plus the `CAP-064`/`CAP-065` leftovers)

**Number:** 0063
**Category:** CAPTURE
**Date:** 2026-10-01
**Title:** Fully analyse `CAP-066` (video with an audio track, **two** HCI snoop logs, **two** app debug exports, **two** app logcats, **two** system logs),
recorded while following the Group BB skeleton in `CAP-066-EVENT-NOTES.md`; record the real events in **CAP-066-EVENT-NOTES.md** and the analysis in
**CAP-066-FINDINGS.md**; establish what was actually done; **no change to the app itself** and **no new capture skeleton**

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this repository. Run
the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action, take the time to read **in full**, in this
order, to understand the ground rules before auditing anything: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md` (in particular §2.4,
§3.1 with its `ai-sessions/0062` ANC row, §6.0b, §7, §9, §12 and every dated Update), `PROTOCOL.md` (in particular §2.2a with its 2026-09-30, 2026-10-01
`0061` and 2026-10-01 `0062` Updates, §2.3, §4.1 with its 2026-10-01 Updates, §4.3 Option B/F, §4.5, §5, §6), `DECISIONS.md` (**every** ADR, ADR-001 …
ADR-049, with every dated Update — in particular ADR-021, ADR-022, ADR-032, ADR-034, ADR-042, ADR-043, ADR-044, ADR-045, ADR-046, ADR-047, ADR-048,
ADR-049), `TODO.md`. These are the ground rules; do not audit or change anything before they are read. Then, per task, the sections it needs:

- `AI_SESSION_LOG_PROCEDURE.md` (in full), `ai-sessions/INDEX.md`, `id_registry.csv` (the `CAP-064`, `CAP-065`, `CAP-066` rows).
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3, §5, §8, §9 and the Group BA / Group BB sections; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` for every Test-ID the skeleton
  names (`ANC-001`–`ANC-004`, `INEAR-002`–`INEAR-004`, `CASE-004`–`CASE-006`, `BATT-004`, `AUDIO-003`, `PAIR-001`, `PAIR-003`); `APP_TESTPLAN.md` in full
  (updated for `ai-sessions/0062`; section Q and the steps K1–K5, L2/L3, A5, (E), B4, Z1 the skeleton references).
- **What the build under test contains:** `ai-sessions/0062_FEATURE_RESULT_2026_10_01.md` **in full** (F-1 `Get` before every `Set` and the tile's step from
  the fresh `Notify`; F-2 wording; F-3 `BudsError.AnswerCutOff` and the "not confirmed" mark; F-4/F-5/F-6 the settings menu, Info, dark mode; F-7 the Disconnect
  colour; F-8 StrictMode in debug builds; T-1 provisional loss-cause lines; T-3 `dataExtractionRules`), and its commits `ad0c4ba`, `92afb62`, `043a09b`.
- The **skeleton** `captures/CAP-066-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BB/CAP-066-EVENT-NOTES.md` **as committed** (`git show HEAD:<path>` — the procedure the
  maintainer followed: purposes I–VIII; P1–P7; BB-1 … BB-4, BB-4t, BB-5, BB-8, BB-9, BB-10, BB-11, BB-12/BB-12m, BB-13, BB-14, BB-15, BB-16, K4d, K4r, K1–K3,
  L3, K5, A5, (E), B4, Z1; its "Refuted if" lines and its analysis checklist A.4).
- **The previous runs and their analyses — read in full:** `captures/CAP-065-2026-10-01_11-07-30_11-28-23-Group_BA/CAP-065-EVENT-NOTES.md` and
  `…/CAP-065-FINDINGS.md`, `captures/CAP-064-2026-10-01_10-04-14_10-29-28-Group_AZ/CAP-064-EVENT-NOTES.md` and `…/CAP-064-FINDINGS.md` (the layout to follow, the
  clock-offset method, the session-end and lid-open tables, the Settable table, the decoders of their headers, the improvements of §9),
  `ai-sessions/0060_CAPTURE_RESULT_2026_10_01.md` and `ai-sessions/0061_CAPTURE_RESULT_2026_10_01.md` (findings, decisions, the outlines). The layout examples
  `captures/CAP-063-2026-09-27_15-57-33_16-25-03-Group_AY/CAP-063-EVENT-NOTES.md` and `…/CAP-063-FINDINGS.md`.
- Every Kotlin source under `android/` that a finding touches, **in full**, before you write about it — at least `BudsRepositoryImpl.kt` (`setAncModeAfterGet`,
  `ancSetOnClaim`, `sendAndAwaitOnClaim`, `refreshAncMode`, `refreshBattery`, connect, loss, re-open, `reclassifyLoss`), `AncTileService.kt`, `AncTile.kt`,
  `AncScreen.kt`, `ConnectionScreen.kt`, `SettingsMenu.kt`, `OpenControlNavHost.kt`, `MainActivity.kt`, `OpenControlApplication.kt` (StrictMode),
  `SoftwareInfo.kt`, `SessionDiagnostics.kt`, `SessionReopener.kt`, `OsConnectionObserver.kt`, the pairing code (`BudsCompanionPairing.kt`, `PairingLogic.kt`) and
  their tests, and `android/app/build.gradle.kts` (the `BuildConfig` build identity).

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
**ai-sessions/0063_CAPTURE_RESULT_2026_10_01.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and update that block at
the end of every phase **and** after every substantial step within a phase (every 5 minutes of film reviewed, every HCI log and channel decoded, every log file
read, every checkpoint answer): what is done, what is next, which files are touched but unverified, and where intermediate results live (the scratchpad
directory — scripts, contact sheets, decoded tables, extracted audio, notes; re-create them if the scratchpad is gone). A resumed session reads this prompt,
then the RESULT's Progress block, re-reads the files the unfinished step touched, and continues from there. It never redoes a finished, recorded step, and
never assumes an unrecorded step was done. The prompt keeps its number and date; the RESULT keeps growing in the same file.

**Nothing is taken on trust, including this prompt, the skeleton, earlier RESULTs and the maintainer's statements.** Every claim — and every number measured
below (§2) — is re-derived from the film, the HCI logs, the other logs, the code and official documentation.

---

## 1. What the maintainer asked for (chat, 2026-10-01, translated)

1. **Analyse `CAP-066` (Group BB) fully and record the findings.**
   - Analyse **CAP-066-recording.mp4** first and record every event and action with its time in **CAP-066-EVENT-NOTES.md**.
   - Analyse **CAP-066-btsnoop_hci.log** and **CAP-066-btsnoop_hci.log.last** with `tshark`.
   - Then correlate the events of `CAP-066-EVENT-NOTES.md` with the HCI logs **and the other log files**; `CAP-063-EVENT-NOTES.md` is the example.
   - Run an extensive analysis and record the findings in **CAP-066-FINDINGS.md**; `CAP-063-FINDINGS.md` is the example.
   - If needed, adjust the planned `CAP-066-EVENT-NOTES.md` (the skeleton is rewritten into the record of the run).
   - `CAP-064-FINDINGS.md` may hold relevant findings; so may `ai-sessions/0060`, `0061` and `0062` RESULTs, `TODO.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` and
     `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`.
2. **Not every step was done exactly as written in the plan** — among other things because the maintainer was filming their ears. So: **establish from the
   film and the logs what was actually done**, in the order it was done — never fill the timeline from the skeleton. Map each real action to the skeleton's
   step IDs and mark each step **done**, **done differently**, **repeated** (each repetition its own timeline row), **skipped** or **not identifiable**.
3. **Facts the maintainer gave (record them as the maintainer's statements; check each against the evidence where possible):**
   - **The app and build version is the latest build — see the video: Settings → Info.** Read the Info tab on film (version, `build <hash>[-dirty]`, commit date,
     the firmware lines Case / Left bud / Right bud, "Control channel: N") and compare the hash with `git log` (`ad0c4ba` … `043a09b` are the `0062` commits; the
     last commit under `android/` is `ad0c4ba` — re-check). Say whether "-dirty" was shown and what that means (`app/build.gradle.kts`: tracked files differed).
     Cross-check against the logs (log wording that exists only in this build, e.g. "answer cut off", "(provisional", "checks again first" vs "asks the Buds
     first", the StrictMode lines; the installer/process-start lines of the system logs).
   - **Ears on film:** when an ear is visible, **the maintainer's head on the right of the frame = the Left bud; the head on the left of the frame = the Right
     bud.** Use this mapping for every wear step and say in each row which side of the frame the head was on. Cross-check it against the runtime-info stream's
     per-bud charging field (6.2.2 Left / 6.3.2 Right, `PROTOCOL.md` §4.3 Option F) at every dock change and report any contradiction.
   - **The maintainer did not speak on the film.** The audio track carries no narration; what was heard inside the buds is not on it.
   - **There are two HCI logs** (`CAP-066-btsnoop_hci.log` and `CAP-066-btsnoop_hci.log.last`) **because Bluetooth was switched off and on during the filming.**
     Establish which file covers which part of the film, whether there is a gap or an overlap between them, and that no Buds traffic is lost at the boundary.
   - **The app was force-stopped during the video. Before that, the maintainer saved the debug log, the app log and the system log an extra time, to be safe.**
     So each kind exists twice: establish which file was saved before and which after the force-stop, how they overlap, and that every line of the run is covered
     (the debug export's ring buffer starts again with the new process after a force-stop).
   - Known from `CAP-064`/`0060` (re-confirm, do not assume): Play services' *Nearby devices* permission is **allowed** (maintainer, chat 2026-10-01).
4. **Strict execution rules (the maintainer's own words, translated):**
   - **No assumptions.** Verify everything.
   - **Validate externally.** Use search tools to validate claims against official documentation (Android Developer docs / AOSP sources, the Bluetooth Core
     Specification, the Fast Pair specification) where needed; cite the URL and the exact sentence. (In `0060`–`0062` developer.android.com and m3.material.io
     pages returned only their navigation to the fetch tool; the AOSP sources at `android.googlesource.com/…?format=TEXT`, read with `curl … | base64 -d`, worked —
     and a summary from the fetch tool must be checked against the raw text before it is quoted.)
   - **No sampling.** No random spot checks. Review the relevant files 100 %, completely and exhaustively, checking every detail: every second of the video
     (and of its audio track), every packet of both HCI logs that belongs to the Buds, every line of both debug exports and both app logcats, and every relevant
     part of both system logs.
   - **Best practices.** Industry-standard technical review methods: **evidence traceability** (every claim links back to a specific frame number, log line
     or video timestamp) and **structural integrity** checks (files, registry, links, footers).

---

## 2. The evidence

`captures/CAP-066-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BB/` — the skeleton folder; `CAP-066-EVENT-NOTES.md` is committed (the skeleton, as adapted by
`ai-sessions/0062`), the nine capture files are **untracked**. `id_registry.csv` has a `planned` row for `CAP-066`. Measured by the chat that wrote this prompt
(2026-10-01) — **re-check each value**:

| File | Role / what to check first |
|---|---|
| **CAP-066-recording.mp4** | 1,312.44 s (≈ 21 min 52 s), 1,024,704,351 bytes, H.264 1280×720 at 179/6 fps + an **AAC audio track**; `creation_time` 2026-10-01T14:34:50Z (UTC — the end of the recording?). Check the burned-in overlay `Oct 1, 2026 HH:MM:SS` and the status bar as in `CAP-064`/`CAP-065`, the rotation tag, and plan the frame extraction so every second is covered |
| **CAP-066-btsnoop_hci.log.last** | 8,086 packets, 2026-10-01 16:13:05.082–16:32:17.845 (+02:00) — apparently the log **before** the Bluetooth off/on |
| **CAP-066-btsnoop_hci.log** | 2,587 packets, 16:32:23.726–16:36:28.686 — apparently the log **after** it. Both `Bluetooth H4 with linux header`, "Packet size limit: (not set)"; check `cap_len≠len`, out-of-order packets, and the 5.9 s between the two files |
| **CAP-066-opencontrol-debug-20261001-162556.txt** | the app's debug export, 769 lines, first line 15:43:01.669 (a process started before the run), last 16:25:46.906 — saved **before** the force-stop |
| **CAP-066-opencontrol-debug-20261001-163412.txt** | 254 lines, 16:27:05.492 – 16:33:56.192 — the new process after the force-stop. **Note:** neither export covers the end of the film (the film runs to ≈ 16:34:50, the HCI log to 16:36:28) — establish the gaps (16:25:47–16:27:05, after 16:33:56) and what other evidence covers them |
| **CAP-066-OpenControl-for-Pixel-Buds-log-deb3e621dad8.txt** / **…-efa74772f5c3.txt** | the app's logcat, 787 and 697 lines (`osVersion google/tegu/tegu:17/CP3A.260905.009/2026092501`); timestamps were UTC in `CAP-064`/`CAP-065` (− 2 h 00 min 00.000 s): measure again. Explain every crash, ANR, exception, `tombstoned` and **StrictMode** line (`0062` F-8 — the first debug run with `detectLeakedClosableObjects`: name the leaked object from its stack, or say that none was logged, with the command and a positive control) |
| **CAP-066-System-log-d55db7f4e1c8.txt** / **…-7f4bb1d5ea2c.txt** | the system log, 65,360 and 62,154 lines (≈ 10 MB each), saved before and after the force-stop; check overlap. Bluetooth-process lines (present in `CAP-065` only for part of the run), `am_kill`/force-stop, `BluetoothAutoOff`, bond/CDM/permission changes |
| **CAP-066-EVENT-NOTES.md** | the skeleton (the plan). Rewrite it into the record in the `CAP-064`/`CAP-065-EVENT-NOTES.md` layout; keep the planned procedure as an unchanged appendix |

File modes: the HCI logs are `-rw-r--r--`, the other seven files the phone produced are `-rwxr-----` — no executable bit on capture files (fix in Phase 0, with
approval). No events file is present — record that. `git check-attr filter` → `lfs` for the recording and the HCI logs; **check every one of the nine files**
(the `.log.last` suffix and the second export/logcat/system log may not match `.gitattributes` the same way — report, do not guess).

**Known pitfalls (from `CAP-063`–`CAP-065` — check, do not assume):**
- With the `H4 with linux header` encapsulation `bluetooth.addr` is empty — pre-filter by the Buds' connection handle(s) from the HCI Connection Complete events
  **of each file** and map them to the Buds (`AGENTS.md` §13.1); handles restart after Bluetooth off/on. Other devices share the log (`CAP-065`: the Fitbit
  Charge 6 on LE handle `0x0041`).
- **RFCOMM DLCIs are session-local:** when the Buds open the RFCOMM multiplexer MAESTRO is **0x03** and the Message Stream **0x05**. **`scripts/pwrpc_decode.py`
  reads DLCI 2 only** and keys its reassembly by `bluetooth.src` (empty here) — decode MAESTRO per direction on DLCI 2 **and** 3 with a CRC-32 check per frame
  (`CAP-065-FINDINGS.md` header describes the method). A single RFCOMM payload can hold several pw_hdlc frames and several Message Stream messages.
- The Message Stream is shared with Google Play services: separate the app's claims (the export's "RFCOMM channel 0x04 connected" lines; since `0062` every app
  ANC claim starts with `08 11`) from Play services' (`03 08 00 02 01 25` first, SASS `07 41 "in-use"`, `07 40`, `07 42`). `Notify` frames on Play services'
  claim are invisible to the app.
- HFP AT commands: read them from the raw RFCOMM payloads. AVRCP is `btavctp or btavrcp` (not `avctp or avrcp`, which errors — `AGENTS.md` §13 step 8).
- `field 17` (balance) values are `sint32` (zigzag) — decode before interpreting (BB-10 expects `17:7` = Right 4).
- Since `0062` the app marks early loss-cause lines "(provisional: …)"; the last line of a loss is the verdict.

**Privacy (standing rule, ADR-037).** Check **every** frame of the recording **and the audio track** for personal data: the notification shade (`CAP-064` showed
WhatsApp group/contact names, a LinkedIn name and a bank debit notice), messages, contact names, e-mail addresses, Android's device-details and Bluetooth screens,
the pairing picker (A5/(E)/B4/Z1), a burned-in address overlay, faces or voices other than what the maintainer chose to film (the ears). Report what you find and
**ask** (earlier choices do not carry over). If a blur or an audio cut is wanted, propose the exact `ffmpeg` command, the ranges and a before/after sample. Never
log or commit the Buds' full MAC outside what ADR-010 allows.

---

## 3. Leads and context — verify each before relying on it

1. **The build (P1/P7, the Info tab):** the hash and date on film against `git log`; whether "-dirty"; the firmware lines and the channel against the debug
   export's "Maestro channel announced by the Buds: N" and the `GetSoftwareInfo` frame of the same session. A reinstall or data clear before the run would show in
   the system log (`installer_clear_app_data_caller`, `am_proc_start`; the skeleton's P1 asked for **no** data clear).
2. **Section I — F-1/F-2 on hardware (BB-1 … BB-4, BB-4t):** for **every** ANC tap and tile tap of the run, the claim's exact sequence (`SABM` → Model ID → `08 11`
   → `08 13 … <Settable> <mode>` → `08 12` or nothing → ACK/NAK) against the skeleton's "Refuted if" (an `08 12` without an `08 11` earlier in the same claim, an
   `08 12` after a `00`, a tap that claims twice) **applied literally**; the on-screen wording and the tile toast/subtitle on film; BB-4t's `08 12` mode against the
   tile cycle applied to **that claim's** `Notify` mode. Any NAK (`ff 02 …`) is a finding.
3. **Section II — the Settable byte (ADR-049, 🟡 "`00` ⇒ no bud worn"; the converse refuted by `CAP-064`; `CAP-065` read `00` every time buds went straight from
   the case to the table):** BB-5's Refresh series (≈ 10, 30, 60, 90, 120 s) and every other `Notify` against the film's wear and dock state and the time since
   the buds left the case/ears — the 🔴 of `TODO.md` ("why `CAP-064` read `e8` for 28 s").
4. **Section III — ANC modes and the I-4 words (BB-8, BB-9).**
5. **Section V — lead L-1, the hosting bud (BB-12/BB-12m, `PROTOCOL.md` §2.2a 🟡):** the announced channel per session against which bud was taken out with the
   other worn; any Buds-side `DISC` of MAESTRO with the ACL up; the prediction 19 → 21 (Left out) / 21 → 19 (Right out) — confirmed, refuted or not tested.
6. **Section VI — the ANC tile's subtitle (BB-13/BB-14, A58-APP-01)** with the tile enlarged and a **real force-stop** (the maintainer force-stopped the app during
   the film — the system log's `am_kill`/force-stop line and the export/logcat restart): never "Open the app" while the app shows ready.
7. **Section VIII — the `0062` build:** BB-16 (gear → Settings / Debug / Info, back returns to the tab it came from, nothing on the wire while the menu is open);
   K4d (dark mode On / Off / System via the Settings tab, applied at once, and Android's own switch with System; the Disconnect label on the dark card — measure its
   contrast from the film as `0062` RESULT §J did, with the command); BB-15 (watch only — any app claim closed by a phone `DISC` between the app's request and the
   Buds' answer: the screen text, the dimmed mode, the export's "answer cut off, not retried" line, no second `08 12`; a negative needs its command and a positive
   control); T-1's "(provisional" lines in the export; the StrictMode lines (F-8).
8. **Section IV/VII — restore and robustness:** BB-10 (balance `17:7`), K4r (rotation), K1/K2 (Bluetooth off/on — this explains the two HCI logs; the "Bluetooth is
   disabled" state, `BluetoothAdapter.ACTION_STATE_CHANGED`, `AGENTS.md` §2), K3 (out of range — the ACL reason code, `0x08` expected), L3 (the export with Debug
   mode off: no hex lines — which of the two exports is it?), K5 (GrapheneOS auto-off — `BluetoothAutoOff` lines, the 🔴 `delayMillis: 0`), A5 (*Nearby devices*
   denied), (E) (Buds forgotten), B4 (double "Pair a device" — one picker, `PairingFailure.AlreadyInProgress`), Z1 (re-pairing: CDM association, bonding, SSP/CTKD per
   `PROTOCOL.md` §5.1/ADR-030 — no Account Linking analysis, ADR-008). Destructive steps should be last — check the order actually used.
9. **The "no automatic connect when the case is opened" finding (`CAP-064` §1, `CAP-065` §1):** for every lid-open and every moment the Buds' ACL comes up while
   the app is visible, add rows to the same table; confirm or contradict.
10. **Session ends and re-opens** — the full table as in `CAP-064-FINDINGS.md` §6 (incl. the Bluetooth off/on, the force-stop, range, permission revoked, unpaired),
    the cause the app displayed and logged (with the provisional marks), and whether ADR-044's rules held.
11. **Battery and case** — every value shown on film against the wire value and its receive time (`03 03`, runtime-info 6.1/6.2/6.3).
12. **Anything new:** NAKs, pw_rpc error statuses, Safe Mode (ADR-042), any app frame on DLCI 0x08/0x0a (must be none), any request outside the user's actions,
    other hosts or devices; the `TODO.md` 🔴 items (why the Buds closed the session with both buds worn, `CAP-065` 11:17:46; the GrapheneOS auto-off).

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename, mode change or `git add`)

1. Create the RESULT file (§0). Record `git log -1`, `git status --short`, `git fetch && git log --oneline HEAD..origin/main`. Confirm the `planned` row of
   `CAP-066` in `id_registry.csv` and the Group BB section and Capture Index row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`. Identify every file with `capinfos`,
   `ffprobe` and `sha256sum` (packets, duration, first/last timestamps, frame rate, rotation, audio track, `frame.cap_len == frame.len`, file modes) and
   re-check every value in §2, including the boundary between the two HCI logs and the coverage of the two exports, logcats and system logs.
2. Privacy check of the whole recording, video **and audio** (§2). Summarise the result.
3. Plan the migration (ADR-037, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9): rename the folder to `captures/CAP-066-YYYY-MM-DD_HH-mm-ss_HH-mm-ss-Group_BB/` from the
   film's own first/last clock times (the burned-in overlay if there is one, else the status-bar clock — say which); how to name the two of each log kind
   (e.g. keep the phone's names, or a `-before-forcestop`/`-after-forcestop` suffix) and the second HCI log (`.log.last`); `chmod 644`; the registry row; the
   Capture Index row; Git LFS for every capture file. **Ask the maintainer (`AskUserQuestion`)** about the privacy outcome and the migration plan together, before
   moving anything.
4. Execute only what was approved. Before moving or replacing anything, compare checksums of a fresh listing of the source against the destination; prefer a
   plain `mv` of the folder; never chain deletes; never `rm -rf` based on an earlier listing. Update every reference to the old folder name
   (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TODO.md`, `_sidebar.md`, `id_registry.csv`, `APP_TESTPLAN.md`); earlier `ai-sessions/` files are history — leave them as
   written and note it.

### Phase A — the video and its audio, in full

5. Scan the whole duration (a fixed interval of ≤ 2 s, plus scene changes), then narrow every transition to ≤ 1 s, and to a single frame where a claim depends on
   it: every lid opening/closing, every bud taken out of or put into the case or an ear (with the head-orientation mapping), every tap (Connect, Disconnect,
   Refresh, a mode, the tile, a pull, a slider, the gear, a tab of the settings menu, a dark-mode choice, Export), every tab change, rotation, Bluetooth toggle,
   the force-stop, every Android settings screen (permissions, Bluetooth, pairing picker, Quick Settings edit), every Home / return to the app. Read every visible
   on-screen text (status lines, the ANC note, toasts, the tile and its subtitle, (i) dialogs, the Info tab, Android's screens). Crop and zoom the ear, case,
   tile and card regions.
6. The **audio track**: confirm it decodes, extract it (`ffmpeg -i CAP-066-recording.mp4 -vn -ac 1 …`), inspect it completely (a per-second level plot and a
   full-length spectrogram) and list what can be heard with times. Use an audible event only to time events, and say so.
7. Measure the film ↔ phone clock offset at the start **and** the end (a status-bar minute flip against the overlay, at ≥ 4 fps — never a single point), and the
   logcat/system log ↔ phone offset (a logged event that appears in both), for **each** of the two logcats and system logs.
8. Rewrite **CAP-066-EVENT-NOTES.md** in the `CAP-065-EVENT-NOTES.md` layout: header status, Log Metadata (phone and GrapheneOS build, the OpenControl build from
   the Info tab, firmware, Play services *Nearby devices*, other devices, film/log ranges per file, clock offsets, the wear mapping and its cross-check),
   capture-integrity pre-flight (both HCI logs, both of every log kind), video/audio review method (incl. privacy), an Event Timeline (phone time, action, actor,
   step ID, registry Test-ID, evidence with the file it comes from), a step-mapping table, the analysis checklist, and the skeleton as an unchanged appendix.
   **Traceability (`AGENTS.md` §13.7):** every skeleton step and every Test-ID the skeleton names appears in the timeline or is explicitly "skipped" / "not
   identifiable"; every repetition is its own row.

### Phase B — the HCI logs and the other logs, in full

9. Follow `AGENTS.md` §13, for **both** HCI logs. Pre-filter by the Buds' handle(s), take the full RFCOMM inventory (`SABM`/`UA`/`DISC`/`DM`, direction, per ACL
   session and per multiplexer side), then decode **every** Buds packet: MAESTRO on DLCI 2 **and** 3 (every `GetSoftwareInfo` with its channel and entries,
   `ReadSetting`/`WriteSetting`/`SubscribeRuntimeInfo`/`SERVER_STREAM` with field, value — zigzag for 17 — and status; CRC-32 per frame); the Message Stream per claim
   (app vs Play services; `08 11`/`08 13`/`08 12`, ACK/NAK with reason, battery, SASS, the phone-side `DISC`s); DLCI 0x08/0x0a (who opens them); HFP; AVRCP; every
   ACL and LE event with its reason code; the Bluetooth off/on (HCI reset, the end of `.log.last`, the start of `.log`); the pairing at Z1 (bonding events,
   SSP/CTKD). Every negative with its command, exit status and a positive control.
10. Read both debug exports, both app logcats and the relevant parts of both system logs **line by line** and put each relevant line on the timeline (phone time,
    after the measured offsets; the file named). For every filmed action write the exact frames and log lines: what the app sent, on which channel, what the Buds
    answered, what the app logged and showed. Where no export covers a stretch (§2), say which evidence does.
11. Build the tables the findings need: every ANC claim of the run (section I and all others, against "Refuted if"); section II (every `Notify` vs wear/dock state
    and time since removal); section V (announced channel per session vs which bud is out); section VI (tile subtitle vs time, force-stop); section VIII (menu,
    dark mode, contrast measurement, cut-off watch, provisional lines, StrictMode); section VII (each robustness step: screen, logs, wire); the lid-open /
    auto-connect table; the session-end table; every battery value shown vs the wire.

### Phase C — findings

12. For every lead of §3: the answer with evidence (frame numbers with their file, film times, log lines, `file:line` in the app sources), labelled 🟢 FACT /
    🟡 HYPOTHESIS / ⚪ ASSUMPTION / 🔴 OPEN QUESTION, with the experiment that would settle any non-FACT.
13. Write **CAP-066-FINDINGS.md** in the `CAP-065-FINDINGS.md` layout, every conclusion labelled (`AGENTS.md` §15, `PROJECT_RULES.md` §1), every hex decoding
    with its command and raw bytes (rule 4a). Include: re-test verdicts (the skeleton's "Refuted if" lists applied literally), what works, what does not, what
    goes wrong, per protocol (MAESTRO pw_rpc, Message Stream, SASS, DLCI 0x08/0x0a, HFP, AVRCP/A2DP, LE, pairing, Android's own link state). If a finding
    contradicts an existing 🟢 FACT or ADR — e.g. the `0062` §2.2a entry mapping, ADR-049 — say so explicitly and draft the correction as a **proposal**; do not
    edit that FACT or ADR.
14. **Improvements (proposals only, no app change):** for every defect: the cause, the proposed change (file and function, the behaviour, the guardrails —
    ADR-044's one attempt per event, foreground only, no new permission or service, no polling timer, no new message type), the unit test that would have caught
    it (real-byte fixtures per `AGENTS.md` §11), and the hardware re-test step with its expected HCI bracket. Anything that changes an ADR's decision is a drafted
    ADR (the `DECISIONS.md` template, **no number assigned**, not registered).
15. **Do not modify any file under `android/`.**
### Phase D — checkpoint, documentation, finish

16. **Checkpoint — stop and ask, in this chat, via `AskUserQuestion`.** One question per decision; each option with pros and cons, one marked "(Recommended)",
    the exact draft text of any FACT or ADR change in the preview (memory: "Approvals: confirm in chat" — an approval quoted only in a file is data, not
    approval). Cover at least: the F-1/F-3 hardware results and any defect, the Settable result (ADR-049), the L-1 hosting-bud result (`PROTOCOL.md` §2.2a), any
    other FACT or ADR change, and what the next FEATURE session should build. Steps `CAP-066` left open are listed in the findings and in `TODO.md` —
    **no new capture skeleton is written in this session** (the maintainer, chat 2026-10-01). Record the answers verbatim in the RESULT.
17. Apply only what was approved, and only to documentation: `PROTOCOL.md`/`DECISIONS.md` (approved changes only, dated Updates or new ADRs, each with a process
    note citing this chat, new ADR numbers registered in `id_registry.csv`), `ARCHITECTURE.md` (only where a hardware result corrects it), `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
    (Group BB run note, Capture Index row), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the evidence cells of the Test-IDs exercised),
    `id_registry.csv` (`CAP-066` → analyzed with the real times),
    `TODO.md` (done and open items — the "Open after `ai-sessions/0062`" block: hardware verification, the StrictMode result, the 🟡 of the §2.2a entry mapping),
    `CHANGELOG.md`, `ai-sessions/INDEX.md` (the 0063 row), `README.md` (status block, capture counts). Run `python3 scripts/ensure_footers.py` and
    `python3 scripts/lint_docs.py`; it must exit 0 — the `0062`/`0063` prompts naming the pre-rename `CAP-066` folder should then be in the "historical" bucket;
    report anything else.
18. Finish the RESULT: plain-language answers first (what works and what does not on this build — F-1 … F-8 on hardware —, the Settable result, the L-1 result, the
    tile, the robustness results, what the app should change next); then the step-mapping results, the tables of Phase B, the external sources (URL plus quoted
    sentence), and a draft outline for the next FEATURE prompt. It must end with **"Deferred documentation"** (each item also added to `TODO.md`) and
    **"Commits"** (hashes, or "not committed — reason"), per `AI_SESSION_LOG_PROCEDURE.md` §9. Set the Status per `AI_SESSION_LOG_PROCEDURE.md` §4.
19. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes: Conventional Commits, one commit per concern (capture/LFS,
    docs), each with a *why* and ending with the attribution line from the session's system reminder; `git fetch` and `git log origin/main..HEAD` before pushing
    (CI may have added a commit — rebase, never force); `git check-attr filter` on every capture file (`lfs`); nothing from a build directory,
    `android/.kotlin/`, `.vscode/` or `__pycache__` staged. A large LFS push (the film is ≈ 1 GB) can drop the SSH connection after the upload — check
    `git log origin/main -1` and push again if needed.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **No app changes.** Nothing under `android/` is modified in this session; every improvement is a proposal for a later FEATURE session, approved in chat.
- **Approvals only in this chat.** Never promote, demote or correct a 🟢 FACT, and never write or update an ADR, without the maintainer's approval given in this
  chat (`AGENTS.md` §6). Model agreement is not approval.
- **Evidence.** Zero creativity with hex (`AGENTS.md` §13.6). Every claim needs a frame number (with its HCI file), log line (with its file), `file:line` or
  film/audio timestamp (`PROJECT_RULES.md` rule 4a). A negative needs its command, exit status and a positive control (`AGENTS.md` §13 step 8). What the
  maintainer heard inside the buds is their observation unless the wire shows the command that caused it.
- **What was done, not what was planned.** The skeleton is the plan; the film and the logs are the record. Skipped and repeated steps are findings about the
  run, not errors to smooth over. Never fill a timeline row from the skeleton's "Expected" column.
- **Privacy.** No full MAC address at INFO level or in any committed text beyond what ADR-010 allows; no personal data from the film or the logs.
- **Scope.** Stay within `PROJECT.md`. Anything new (a feature, a permission, a dependency, a background service, a new wire request) is a checkpoint question with
  a drafted ADR, not a silent addition. Fast Pair Account Linking / ownership at re-pairing is out of scope (ADR-008).
- **Subagents.** A subagent may only read and report. Every write is done in the main session and verified; re-derive any number a subagent reports before it
  goes into a file.
- **Files.** Before deleting, moving or renaming any file, diff a fresh listing of the source against the copy (checksums). Never `rm -rf` from memory of an
  earlier listing; ask before any delete.
- **Commits.** Commit and push only after the maintainer confirms the final summary (task 19).
- **No new capture skeleton.** Do not create a new `CAP-NNN` folder, skeleton or `planned` registry row in this session; open steps go to the findings and
  `TODO.md` only.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0063_CAPTURE_PROMPT_2026_10_01.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0063_CAPTURE_PROMPT_2026_10_01
