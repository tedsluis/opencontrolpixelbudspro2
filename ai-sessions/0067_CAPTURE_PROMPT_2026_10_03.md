# 0067_CAPTURE_PROMPT_2026_10_03.md — Full analysis of CAP-067 (Group BC: the 1.0.0 release APK in a GrapheneOS profile without Google Play services — the release test and the Definition-of-done evidence)

**Number:** 0067
**Category:** CAPTURE
**Date:** 2026-10-03
**Title:** Fully analyse `CAP-067` (video, **two** HCI snoop logs, **two** app debug exports, **one** app logcat, **no** system log), recorded with the
release-signed **1.0.0** APK in a GrapheneOS secondary user without Google Play while following the Group BC skeleton in `CAP-067-EVENT-NOTES.md`; record the
real events in **CAP-067-EVENT-NOTES.md** and the analysis in **CAP-067-FINDINGS.md**; establish what was actually done; give the release verdict for 1.0.0 and
the evidence for `PROJECT.md`'s Definition of done; **no change to the app itself**, **no new capture skeleton**, **no publishing step**

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this repository. Run the
phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action, take the time to read **in full**, in this order,
to understand the ground rules before auditing anything: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md` (incl. the Definition of done and its 2026-10-02 evidence
table), `ARCHITECTURE.md` (in particular §2.4, §3.1, §5a, §6.0b, §7, §8.1, §9, §12, §14 and every dated Update), `PROTOCOL.md` (in particular §2.2a with its
Updates, §2.3, §4.1, §4.3 Option B/F, §4.5, §5, §6), `DECISIONS.md` (**every** ADR, ADR-001 … ADR-051, with every dated Update — in particular ADR-004, ADR-008,
ADR-010, ADR-032, ADR-034, ADR-037, ADR-042, ADR-043, ADR-044, ADR-045, ADR-046, ADR-047, ADR-048, ADR-049, ADR-050 and its Update, ADR-051), `TODO.md`. These are
the ground rules; do not audit or change anything before they are read. Then, per task:

- `AI_SESSION_LOG_PROCEDURE.md` (in full), `ai-sessions/INDEX.md`, `id_registry.csv` (the `CAP-065`, `CAP-066`, `CAP-067` rows, ADR-050/051).
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3, §5, §8, §9 and the Group BB / Group BC sections; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` for every Test-ID the skeleton names
  (`AUDIO-003`, `INEAR-002`–`INEAR-004`, `PAIR-003`, `BATT-004`, `PAIR-001`); `APP_TESTPLAN.md` in full (the steps K1–K5, L3, A5, (E), B4, Z1 the skeleton uses).
- **What the build under test contains — read in full:** `ai-sessions/0064_FEATURE_PROMPT_2026_10_01.md` and `…_RESULT_…` (F-1 tab across a configuration
  change, F-3 Bluetooth-off loss cause, F-4 profile proxies, F-6 Info licence and links), `ai-sessions/0066_FEATURE_PROMPT_2026_10_02.md` and `…_RESULT_…` (balance
  slider without steps, no `LICENSE` link; §C.1 the route to a user without Play services), `ai-sessions/0065_MAINTENANCE_PROMPT_2026_10_01.md` and `…_RESULT_…`
  (release signing, version 1.0.0 / 10000, ADR-051 name and Info notice, §A.4 the Definition-of-done evidence table), `RELEASING.md` (the release flow and its
  rules), `README.md`, and the commits from `a0a95d3` to `8d8af4b` (`git log --stat a0a95d3^..8d8af4b`).
- The **skeleton** `captures/CAP-067-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BC/CAP-067-EVENT-NOTES.md` **as committed** (`git show HEAD:<path>` — the procedure the
  maintainer followed, as adapted on 2026-10-02 in `8d8af4b`: purposes A.0/P7, I–X; P0–P8; BC-1 … BC-12, BC-R, BC-end, A5, (E), B4, Z1; its "Refuted if" lines and
  its analysis checklist A.4). (The maintainer's request names it once with `Group_BB` — the folder is `…-Group_BC`.)
- **The previous run and its analysis — the layout to follow, read in full:** `captures/CAP-066-2026-10-01_16-12-57_16-34-50-Group_BB/CAP-066-EVENT-NOTES.md` and
  `…/CAP-066-FINDINGS.md` (clock-offset method, session-end and lid-open tables, the Play-services marker, the decoders of its header), and
  `ai-sessions/0063_CAPTURE_PROMPT_2026_10_01.md` / `…_RESULT_…` (the method).
- Every Kotlin source under `android/` that a finding touches, **in full**, before you write about it — at least `BudsRepositoryImpl.kt`, `SessionDiagnostics.kt`,
  `SessionReopener.kt`, `OsConnectionObserver.kt`, `ProfileProxies.kt`, `OpenControlNavHost.kt`, `MainActivity.kt`, `OpenControlApplication.kt`, `SettingsMenu.kt`,
  `EqScreen.kt`, `AncTileService.kt`, `BudsForegroundService.kt`, the pairing code (`BudsCompanionPairing.kt`, `PairingLogic.kt`), their tests, and
  `android/app/build.gradle.kts` (version, `BuildConfig` build identity, release signing).

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
**ai-sessions/0067_CAPTURE_RESULT_2026_10_03.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and update that block at the
end of every phase **and** after every substantial step within a phase (every 5 minutes of film reviewed, every HCI log and channel decoded, every log file read,
every checkpoint answer): what is done, what is next, which files are touched but unverified, and where intermediate results live (the scratchpad directory —
scripts, contact sheets, decoded tables, notes; re-create them if the scratchpad is gone). A resumed session reads this prompt, then the RESULT's Progress block,
re-reads the files the unfinished step touched, and continues from there. It never redoes a finished, recorded step, and never assumes an unrecorded step was
done. The prompt keeps its number and date; the RESULT keeps growing in the same file.

**Nothing is taken on trust, including this prompt, the skeleton, earlier RESULTs and the maintainer's statements.** Every claim — and every number measured
below (§2) — is re-derived from the film, the HCI logs, the other logs, the code and official documentation.

---

## 1. What the maintainer asked for (chat, 2026-10-03, translated from Dutch)

1. **Analyse `CAP-067` (Group BC) fully and record the findings.**
   - Analyse **CAP-067-recording.mp4** first and record every event and action with its time in **CAP-067-EVENT-NOTES.md**.
   - Analyse **CAP-067-btsnoop_hci.log** and **CAP-067-btsnoop_hci.log.last** with `tshark`.
   - Then correlate the events of `CAP-067-EVENT-NOTES.md` with the HCI logs **and the other log files**; `CAP-066-EVENT-NOTES.md` is the example.
   - Run an extensive analysis and record the findings in **CAP-067-FINDINGS.md**; `CAP-066-FINDINGS.md` is the example.
   - If needed, adjust the planned `CAP-067-EVENT-NOTES.md` (the skeleton is rewritten into the record of the run).
   - Possibly relevant: the `ai-sessions/0064`, `0065`, `0066` prompts and RESULTs, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `PROJECT.md`, `README.md`, `RELEASING.md`,
     `TODO.md`.
2. **Not every step was done exactly as in the test plan** — among other things because the maintainer was filming their ears — and **some extra things were
   shown on the film.** So: **establish from the film and the logs what was actually done**, in the order it was done — never fill the timeline from the
   skeleton. Map each real action to the skeleton's step IDs and mark each step **done**, **done differently**, **repeated** (each repetition its own row),
   **skipped** or **not identifiable**; list every extra action as its own row ("not in the plan").
3. **Facts the maintainer gave (record them as the maintainer's statements; check each against the evidence where possible):**
   - **The app and build version is the latest build, 1.0.0 — see the video: Settings → Info.** Read the Info tab on film (version, `build <hash>[-dirty]`, commit
     date, the ADR-051 notice, the firmware lines Case / Left bud / Right bud, "Control channel: N"). Expected from the chat of 2026-10-02 (re-check, do not
     assume): `scripts/release.sh 1.0.0` built it from commit **`8d8af4b`**, versionCode 10000, APK SHA-256 `107d49b609a3509364f92e2911931e9ff51ae1dd62f96405026c27464f3467a2`,
     signing-certificate SHA-256 `a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb`. If `dist/1.0.0/` exists in the working tree, check the APK's
     SHA-256 and certificate (read only — never rebuild, never run `scripts/release.sh`, never touch `dist/`). Say whether "-dirty" was shown. Cross-check against
     wording that exists only in this build (the "Works with Google Pixel Buds Pro 2. Not affiliated …" line, the launcher label "OpenControl", the notification
     title "OpenControl for Pixel Buds Pro 2", "Bluetooth was switched off on this phone", the adapter-state lines).
   - **The capture ran on GrapheneOS (Android 17) in a profile without Google Play services.** This is the Definition-of-done evidence (`PROJECT.md`, `0065`
     §A.4, skeleton P0/X). Verify it from the logs: the logcat header (`userType`), any package list the maintainer captured (P0), and above all the HCI log —
     **no Message Stream claim whose first phone message is `03 08 00 02 01 25`** (the Play-services marker of `CAP-066-FINDINGS.md`), with the command, its exit
     status and a positive control (the same filter finding Play-services claims in `CAP-066`'s logs).
   - **Ears on film:** head on the **right** of the frame = the **Left** bud; head on the **left** of the frame = the **Right** bud. Use this mapping for every
     wear step, say in each row which side the head was on, and cross-check it against the runtime-info per-bud fields (6.2.x Left / 6.3.x Right, `PROTOCOL.md`
     §4.3 Option F) at every dock change; report any contradiction.
   - **The maintainer did not speak on the film.**
   - **There are two HCI logs** (`CAP-067-btsnoop_hci.log` and `CAP-067-btsnoop_hci.log.last`) **because Bluetooth was switched off and on during the filming.**
     Establish which file covers which part, the gap between them, and that no Buds traffic is lost at the boundary.
   - **There are two debug exports, one with Debug mode on and one with Debug mode off** (L3 / BC-9). Establish which is which from their content (hex lines).
   - **There is no system log this time, because it was not available in the profile without Google Play services.** Record this; say which skeleton checks
     depended on the system log (P7's `ACTION_VIEW` lines, BC-10's `BluetoothAutoOff`/`STATE_CHANGED`, BC-12's auto-off alarm, force-stop lines) and what other
     evidence (the app logcat's events buffer, the export, the film, the HCI log) covers each — or that it stays unverified.
   - **The app was force-stopped during the video. Before that, the maintainer saved the debug log, the app log and the system log an extra time, to be safe.**
     Establish from the files present which were saved before and which after the force-stop, and whether every stretch of the run is covered (the export's ring
     buffer restarts with the new process).
4. **Strict execution rules (the maintainer's own words, translated):**
   - **No assumptions.** Verify everything.
   - **Validate externally.** Use search tools to validate claims against official documentation (Android Developer docs / AOSP sources, the Bluetooth Core
     Specification, the Fast Pair specification) where needed; cite the URL and the exact sentence. (In earlier sessions developer.android.com pages sometimes
     returned only navigation to the fetch tool; AOSP sources at `android.googlesource.com/…?format=TEXT`, read with `curl … | base64 -d`, worked — check any
     fetch-tool summary against the raw text before quoting it.)
   - **No sampling.** No random spot checks: a 100 % complete and exhaustive review of the relevant files, checking every detail — every second of the video,
     every packet of both HCI logs that belongs to the Buds, every line of both debug exports and of the app logcat.
   - **Best practices.** Industry-standard technical review methods: **evidence traceability** (every claim links back to a specific frame number, log line or
     video timestamp) and **structural integrity** checks (files, registry, links, footers).

---

## 2. The evidence

`captures/CAP-067-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BC/` — `CAP-067-EVENT-NOTES.md` is committed (the skeleton, `8d8af4b`), the six capture files are
**untracked**. `id_registry.csv` has a `planned` row for `CAP-067`. Measured by the chat that wrote this prompt (2026-10-03) — **re-check each value**:

| File | Measured / what to check first |
|---|---|
| **CAP-067-recording.mp4** | 1,254.32 s (≈ 20 min 54 s), 967,913,555 bytes, H.264 1280×720 at 179/6 fps; an **audio stream with `codec_name=unknown`, tag `[0][0][0][0]`** — establish whether it decodes at all (`ffmpeg -i … -vn -f null -`), and say so; `creation_time` 2026-10-03T06:18:29Z (= 08:18:29 local — inside the run, not obviously its start or end: measure the film's own clock from the burned-in overlay / status bar) |
| **CAP-067-btsnoop_hci.log.last** | 1,563 packets, 2026-10-03 07:57:42.429 – 08:12:24.089 (local) — apparently **before** the Bluetooth off/on |
| **CAP-067-btsnoop_hci.log** | 1,810 packets, 08:12:38.178 – 08:20:05.132 — apparently **after** it. Both `Bluetooth H4 with linux header`, no size limit. **Both files are far smaller than `CAP-066`'s (155 KB vs 568 KB) for a run of similar length:** check the snoop-log mode (full vs filtered/headers-only: are RFCOMM payloads present and complete, `frame.cap_len == frame.len`?), out-of-order packets, the 14 s gap, and that **the HCI log ends at 08:20:05 while the second export runs to 08:23:06** — what the film shows after 08:20:05 has no wire evidence |
| **CAP-067-opencontrol-debug-20261003-081146.txt** | 340 lines, 07:12:34.868 – 08:11:28.901 (a process that started before the run); saved at 08:11:46 per its name. **Repeated "Permissions (start)" lines every ≈ 10 s at 07:12–07:13 and 08:01–08:02** — explain them (activity recreations at the rotation steps BC-1/BC-2? check against the logcat's `wm_on_create_called`) |
| **CAP-067-opencontrol-debug-20261003-082306.txt** | 124 lines, 08:15:42.399 – 08:23:01.435 — the new process after the force-stop; its first lines read **"Bluetooth NOT_REQUESTED -> NOT_REQUESTED … showing the system prompt … BLUETOOTH_CONNECT=true"** — establish why the permission had to be granted again (A5 *Nearby devices* denied? the force-stop does not reset permissions) |
| **CAP-067-OpenControl-for-Pixel-Buds-Pro-2-log-0d4af245fcc4.txt** | the app logcat, 671 lines, header `osVersion google/tegu/tegu:17/CP3A.260905.009/2026092501`, **`userType: full.secondary`**; timestamps 05:03:17.975 – 06:23:20.880 (UTC? then − 2 h 00 min — measure the offset). Contains the events buffer (`wm_on_create_called`). Two PIDs (12287, 20326) = before and after the force-stop? **Only one logcat file exists**, although the maintainer saved the app log twice — record what is present. Explain every crash, ANR, exception, `tombstoned` line; StrictMode is **off** in a release build (`OpenControlApplication.kt:63`) |
| **CAP-067-EVENT-NOTES.md** | the skeleton (the plan). Rewrite it into the record in the `CAP-066-EVENT-NOTES.md` layout; keep the planned procedure as an unchanged appendix |

File modes: the HCI logs are `-rw-r--r--`, the four files the phone produced are `-rwxr-----` — no executable bit on capture files (fix in Phase 0, with
approval). `git check-attr filter` → `lfs` for all six capture files (measured; re-check). No events file, no system log, no package-list output of P0 is present
— record each absence and what it means for the checks that needed it.

**Known pitfalls (from `CAP-063`–`CAP-066` — check, do not assume):**
- With the `H4 with linux header` encapsulation `bluetooth.addr` is empty — pre-filter by the Buds' connection handle(s) from the HCI Connection Complete events
  **of each file** and map them to the Buds (`AGENTS.md` §13.1); handles restart after Bluetooth off/on. Other devices may share the log.
- **RFCOMM DLCIs are session-local** (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05). `scripts/pwrpc_decode.py` reads DLCI 2 only and keys its reassembly by
  `bluetooth.src` (empty here) — decode MAESTRO per direction on DLCI 2 **and** 3 with a CRC-32 check per frame (`CAP-065-FINDINGS.md` header). One RFCOMM payload
  can hold several pw_hdlc frames and several Message Stream messages.
- **The Message Stream claims:** the app's start with `08 11`/`08 12` (ANC) or its battery/Find sequence; Play services' with `03 08 00 02 01 25`. In this run
  **no** Play-services claim is expected — any one is a central finding (it would refute "without Play services" for this run, not by itself the app).
- HFP AT commands: read them from the raw RFCOMM payloads. AVRCP is `btavctp or btavrcp` (not `avctp or avrcp`, which errors — `AGENTS.md` §13 step 8).
- `field 17` (balance) values are `sint32` (zigzag) — decode before interpreting (BC-3 aims at `17:7` = Right 4; the derived channel-19 frame is in the skeleton).
- The app marks early loss-cause lines "(provisional: …)"; the last line of a loss is the verdict. F-3 expects "Bluetooth was switched off on this phone" without it.

**Privacy (standing rule, ADR-037).** Check **every** frame of the recording for personal data: notifications, messages, contact names, e-mail addresses,
Android's device-details and Bluetooth screens, the pairing picker (A5/(E)/B4/Z1), **a burned-in street-address overlay** (the maintainer's camera films have
carried one before), faces other than what the maintainer chose to film (the ears). Report what you find and **ask** (earlier choices do not carry over). If a
blur is wanted, propose the exact `ffmpeg` command, the ranges and a before/after sample. Never log or commit the Buds' full MAC outside what ADR-010 allows.

---

## 3. Leads and context — verify each before relying on it

1. **The build and the release identity (A.0, P1, P7, P8):** version 1.0.0, hash `8d8af4b` without "-dirty", the ADR-051 notice, launcher label, notification
   title; whether the APK on the phone is the `dist/1.0.0` APK (no reinstall from another build — logcat install/process lines); the firmware lines and channel
   against the export's "Maestro channel announced by the Buds: N" and the `GetSoftwareInfo` frame of the same session.
2. **X — the Definition of done without Google Play services** (`PROJECT.md`): criterion 1 (connect), 2 (battery and ANC), 3 (several connect/disconnect cycles —
   BC-6, BC-10/BC-11, the force-stop, Z1) — each with frame numbers; and the negative "no Play-services claim" over **both** HCI logs (command, exit status, positive
   control). 🟡 HYPOTHESIS to test (`0066` RESULT §C.1): the Owner's sandboxed Play services cannot use Bluetooth while the secondary user is in the foreground.
   Also look for any other app or host on the Buds' channels (DLCI 0x08/0x0a openers, SASS).
3. **P7/F-6 — Info and links:** the Info text on film; "Read the licence"; each link opens the browser — without a system log the `ACTION_VIEW` start is only on
   film and possibly in the logcat's events buffer (`wm_*`/`am_*` lines); no app RFCOMM frame while the menu is open.
4. **I — F-1, the tab across a configuration change (BC-1, BC-2, BC-3r, BC-8):** per rotation and dark-mode switch, the tab before and after on film against
   `wm_on_create_called`; refuted if any tab other than Connection comes back as Connection.
5. **II — the balance slider (BC-3, BC-5, BC-R):** every `WriteSetting 4:{17:n}` (zigzag) with its `RESPONSE`, exactly one per release; the number of drags to reach
   `17:7`; the channel-19 bytes of `17:7` against the derived frame in the skeleton (a first capture of it, if it happened on 19).
6. **III — BB-12 / lead L-1 (BC-6a, BC-6):** the announced channel per session against which bud was taken out with the other worn (head-side mapping); any
   Buds-side `DISC` of MAESTRO with the ACL up; the prediction 19 → 21 (Left out) — confirmed, refuted or not tested.
7. **IV — BC-7:** any app claim closed between request and answer (expected none without Play services); the "cut off" text on film (expected never).
8. **V, VI — K4d (BC-8) and L3 (BC-9):** dark mode Off / System with Android's switch; the export with Debug mode off has no `DLCI 0x..:` hex line after the
   switch-off line.
9. **VII — F-3 (BC-10, BC-11):** the export's "Bluetooth adapter: ON -> TURNING_OFF", "-> OFF", the loss cause "Bluetooth was switched off on this phone" without
   "(provisional", no later "undetermined"; the re-open after Bluetooth on (ADR-044, `PAIR-003`); the HCI log boundary (this toggle explains the two files).
   **BC-11s (F-4, StrictMode) is not testable on the release APK** — record it so, not as "no violation".
10. **VIII — K5 (BC-12):** whether the auto-off setting was reachable in the secondary user (the skeleton allowed skipping it); the adapter lines.
11. **IX — A5, (E), B4, Z1:** *Nearby devices* denied and re-granted (see §2: the second export's permission prompt); the Buds forgotten (device-wide — the Owner
    is unpaired too); one picker on a double tap (`PairingFailure.AlreadyInProgress`); re-pairing: CDM association, bonding (SSP or CTKD, `PROTOCOL.md` §5.1/
    ADR-030; no Account Linking analysis, ADR-008). Check the order actually used (destructive last).
12. **The force-stop:** when (film, logcat PIDs, export gap), what the app showed on return, the tile.
13. **The "no automatic connect when the case is opened" finding (`CAP-064` §1 … `CAP-066`)** — add rows for every lid-open and ACL-up while the app is visible.
14. **Session ends and re-opens** — the full table as in `CAP-066-FINDINGS.md` (Bluetooth off/on, force-stop, permission revoked, unpaired, Buds-side closes),
    the cause shown and logged, whether ADR-044's rules held.
15. **Battery and case** — every value shown on film against the wire value and its receive time (`03 03`, runtime-info 6.1/6.2/6.3).
16. **Anything new:** NAKs, pw_rpc error statuses, Safe Mode (ADR-042 — expected off on `release_5.203`), any app frame on DLCI 0x08/0x0a (must be none), any request
    outside the user's actions; anything that behaves differently in a secondary user (companion association, notifications, the tile, the foreground service).
17. **The extra things the maintainer showed on film** — identify each and say what it demonstrates.

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename, mode change or `git add`)

1. Create the RESULT file (§0). Record `git log -1`, `git status --short`, `git fetch && git log --oneline HEAD..origin/main`. Confirm the `planned` row of
   `CAP-067` in `id_registry.csv` and the Group BC section and Capture Index row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`. Identify every file with `capinfos`,
   `ffprobe` and `sha256sum` and re-check every value in §2 (incl. the snoop-log completeness, the boundary between the two HCI logs, the coverage of the exports
   and the logcat, the audio stream).
2. Privacy check of the whole recording (§2). Summarise the result.
3. Plan the migration (ADR-037, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9): rename the folder to `captures/CAP-067-YYYY-MM-DD_HH-mm-ss_HH-mm-ss-Group_BC/` from the film's
   own first/last clock times (say which clock); the names of the two exports and of the second HCI log; `chmod 644`; the registry row; the Capture Index row; Git
   LFS for every capture file. **Ask the maintainer (`AskUserQuestion`)** about the privacy outcome and the migration plan together, before moving anything.
4. Execute only what was approved. Before moving anything, compare checksums of a fresh listing of the source against the destination; prefer a plain `mv` of the
   folder; never chain deletes; never `rm -rf` based on an earlier listing. Update every reference to the old folder name (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`,
   `TODO.md`, `_sidebar.md`, `id_registry.csv`, `APP_TESTPLAN.md`, `PROJECT.md`, `RELEASING.md`); earlier `ai-sessions/` files are history — leave them as written.

### Phase A — the video, in full

5. Scan the whole duration (a fixed interval of ≤ 2 s, plus scene changes), then narrow every transition to ≤ 1 s, and to a single frame where a claim depends on
   it: every lid opening/closing, every bud taken out of or put into the case or an ear (with the head-side mapping), every tap (Connect, Disconnect, Refresh, a mode,
   the tile, a pull, a slider, the gear, a settings tab, a dark-mode choice, Export, a link, Read the licence), every tab change, rotation, Bluetooth toggle, the
   force-stop, every Android settings screen (users/profiles, permissions, Bluetooth, pairing picker, Quick Settings, auto-off), every Home / return to the app, and
   every action not in the plan. Read every visible on-screen text. Crop and zoom the ear, case, tile and card regions.
6. The **audio stream**: establish whether it decodes; if it does, inspect it completely (level plot, spectrogram) and use audible events only for timing, saying so.
7. Measure the film ↔ phone clock offset at the start **and** the end (a status-bar minute flip against the overlay, at ≥ 4 fps — never a single point), and the
   logcat ↔ phone offset (an event in both).
8. Rewrite **CAP-067-EVENT-NOTES.md** in the `CAP-066-EVENT-NOTES.md` layout: header status, Log Metadata (phone, GrapheneOS build, **the user/profile and the
   absence of Google Play**, the OpenControl build from the Info tab, firmware, other devices, film/log ranges per file, clock offsets, the wear mapping and its
   cross-check), capture-integrity pre-flight (both HCI logs incl. completeness, both exports, the logcat, the absent system log), video review method (incl.
   privacy), an Event Timeline (phone time, action, actor, step ID, registry Test-ID, evidence with its file), a step-mapping table, the analysis checklist, and the
   skeleton as an unchanged appendix. **Traceability (`AGENTS.md` §13.7):** every skeleton step and every Test-ID it names appears in the timeline or is explicitly
   "skipped" / "not identifiable"; every repetition and every extra action is its own row.

### Phase B — the HCI logs and the other logs, in full

9. Follow `AGENTS.md` §13, for **both** HCI logs. Pre-filter by the Buds' handle(s), take the full RFCOMM inventory (`SABM`/`UA`/`DISC`/`DM`, direction, per ACL
   session and per multiplexer side), then decode **every** Buds packet: MAESTRO on DLCI 2 **and** 3 (every `GetSoftwareInfo` with its channel and entries,
   `ReadSetting`/`WriteSetting`/`SubscribeRuntimeInfo`/`SERVER_STREAM` with field, value — zigzag for 17 — and status; CRC-32 per frame); the Message Stream per claim
   (app vs anything else; `08 11`/`08 13`/`08 12`, ACK/NAK with reason, battery, Find, SASS, phone-side `DISC`s); DLCI 0x08/0x0a (who opens them); HFP; AVRCP; every
   ACL and LE event with its reason code; the Bluetooth off/on (the end of `.log.last`, the start of `.log`); the pairing at Z1 (bonding events, SSP/CTKD). Every
   negative with its command, exit status and a positive control.
10. Read both debug exports and the app logcat **line by line** and put each relevant line on the timeline (phone time after the measured offsets; the file named).
    For every filmed action write the exact frames and log lines: what the app sent, on which channel, what the Buds answered, what the app logged and showed. Where
    no log covers a stretch (§2: after 08:20:05 no HCI; between the exports; no system log), say which evidence does, or that none does.
11. Build the tables the findings need: the Definition-of-done table (criterion → frames/lines → verdict); every Message Stream claim with its first phone message;
    every ANC claim against "Refuted if"; section I (tab per configuration change); section II (every balance write); section III (announced channel per session vs
    the bud taken out); section VII (screen, logs, wire per step); the lid-open / auto-connect table; the session-end table; every battery value shown vs the wire.

### Phase C — findings and the release verdict

12. For every lead of §3: the answer with evidence (frame numbers with their file, film times, log lines, `file:line` in the app sources), labelled 🟢 FACT /
    🟡 HYPOTHESIS / ⚪ ASSUMPTION / 🔴 OPEN QUESTION, with the experiment that would settle any non-FACT.
13. Write **CAP-067-FINDINGS.md** in the `CAP-066-FINDINGS.md` layout, every conclusion labelled (`AGENTS.md` §15, `PROJECT_RULES.md` §1), every hex decoding with its
    command and raw bytes (rule 4a). Include: re-test verdicts (the skeleton's "Refuted if" lists applied literally), what works, what does not, what goes wrong, per
    protocol (MAESTRO pw_rpc, Message Stream, SASS, DLCI 0x08/0x0a, HFP, AVRCP/A2DP, LE, pairing, Android's own link state), and what is different in a secondary user.
    If a finding contradicts an existing 🟢 FACT or ADR, say so and draft the correction as a **proposal**; do not edit that FACT or ADR.
14. **The release verdict for 1.0.0** (`RELEASING.md`, the flow's "Result?" step): classify every defect as **heavy** (a crash, a wrong or unacknowledged write, Safe
    Mode failing, a session lost without recovery, a Definition-of-done criterion not met, anything risky for the Buds) or **minor** (a known issue for the release
    notes), with the evidence. Separately: a Play-services claim in the HCI log means "Definition of done not proven by this run", not "app broken". Give one of:
    *release `8d8af4b` as 1.0.0* (with any known issues worded for `dist/1.0.0/release-notes.md`), *fix first* (what, and the targeted re-test for the next capture —
    `RELEASING.md`: the same version may be rebuilt while nothing is published), or *re-run for the Definition of done*.
15. **Improvements (proposals only, no app change):** for every defect: the cause, the proposed change (file and function, the behaviour, the guardrails — ADR-044's
    one attempt per event, foreground only, no new permission or service, no polling timer, no new message type), the unit test that would have caught it (real-byte
    fixtures per `AGENTS.md` §11), and the hardware re-test step with its expected HCI bracket. Anything that changes an ADR's decision is a drafted ADR (no number).
16. **Do not modify any file under `android/`, `dist/` or `scripts/`.**

### Phase D — checkpoint, documentation, finish

17. **Checkpoint — stop and ask, in this chat, via `AskUserQuestion`.** One question per decision; each option with pros and cons, one marked "(Recommended)", the
    exact draft text of any FACT, ADR or `PROJECT.md` change in the preview (memory: "Approvals: confirm in chat"). Cover at least: the release verdict (§4 task 14);
    **ticking the Definition of done 1–3 in `PROJECT.md`** (the exact new text of the evidence table, only if the evidence holds); the known-issues wording for the
    release notes, if any; the L-1 hosting-bud result (`PROTOCOL.md` §2.2a); any other FACT or ADR change; what the next FEATURE session should build. **No new
    capture skeleton is written in this session.** Record the answers verbatim in the RESULT.
18. Apply only what was approved, and only to documentation: `PROJECT.md` (the Definition of done, if approved), `PROTOCOL.md`/`DECISIONS.md` (approved changes only,
    dated Updates or new ADRs with a process note citing this chat, numbers registered in `id_registry.csv`), `ARCHITECTURE.md` (only where a hardware result corrects
    it, e.g. §5a hardware-verified marks), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BC run note, Capture Index row), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the evidence cells
    of the Test-IDs exercised), `APP_TESTPLAN.md` (if a step proved unworkable in a secondary user), `id_registry.csv` (`CAP-067` → analyzed with the real times),
    `TODO.md` (done and open items — "Open after `ai-sessions/0064`/`0065`/`0066`": the hardware verification, F-4 still open, the release steps), `CHANGELOG.md`
    (the history entry; **not** the `[1.0.0]` date — that follows publication, `RELEASING.md` §8), `ai-sessions/INDEX.md` (the 0067 row), `README.md` (status, capture
    counts — not the "No release has been published yet" note, which changes only after publication). Run `python3 scripts/ensure_footers.py` and
    `python3 scripts/lint_docs.py`; it must exit 0 — the prompts naming the pre-rename `CAP-067` folder should then be in the "historical" bucket; report anything
    else.
19. Finish the RESULT: plain-language answers first (the release verdict; the Definition of done; what works and what does not on 1.0.0 — F-1 … F-6 on hardware —;
    the L-1 result; what differs in a secondary user; what the app should change next); then the step-mapping results, the tables of Phase B, the external sources
    (URL plus quoted sentence), and, if the verdict is "release", the next steps from `RELEASING.md` §7 for the maintainer (tag **`8d8af4b`**, the files from the kept
    copy of `dist/1.0.0/`) — **as instructions, never executed**. It must end with **"Deferred documentation"** (each item also added to `TODO.md`) and **"Commits"**
    (hashes, or "not committed — reason"), per `AI_SESSION_LOG_PROCEDURE.md` §9. Set the Status per `AI_SESSION_LOG_PROCEDURE.md` §4.
20. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes: Conventional Commits, one commit per concern (capture/LFS, docs),
    each with a *why* and ending with the attribution line from the session's system reminder; `git fetch` and `git log origin/main..HEAD` before pushing (CI may
    have added a commit — rebase, never force); `git check-attr filter` on every capture file (`lfs`); nothing from a build directory, `dist/`, `android/.kotlin/`,
    `.vscode/` or `__pycache__` staged. A large LFS push (the film is ≈ 1 GB) can drop the SSH connection after the upload — check `git log origin/main -1` and push
    again if needed.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **No app changes and no release actions.** Nothing under `android/`, `dist/` or `scripts/` is modified; no build is made; `scripts/release.sh` is not run; no
  tag is created or pushed, no GitHub release created or edited, no asset uploaded, no repository setting changed. Publishing is the maintainer's own act
  (`RELEASING.md`).
- **Approvals only in this chat.** Never promote, demote or correct a 🟢 FACT, never write or update an ADR, and never tick the Definition of done, without the
  maintainer's approval given in this chat (`AGENTS.md` §6). Model agreement is not approval.
- **Evidence.** Zero creativity with hex (`AGENTS.md` §13.6). Every claim needs a frame number (with its HCI file), log line (with its file), `file:line` or film
  timestamp (`PROJECT_RULES.md` rule 4a). A negative needs its command, exit status and a positive control (`AGENTS.md` §13 step 8). What the maintainer heard
  inside the buds is their observation unless the wire shows the command that caused it.
- **What was done, not what was planned.** The skeleton is the plan; the film and the logs are the record. Skipped, repeated and extra steps are findings about the
  run, not errors to smooth over. Never fill a timeline row from the skeleton's "Expected" column.
- **Privacy.** No full MAC address at INFO level or in any committed text beyond what ADR-010 allows; no personal data from the film or the logs.
- **Scope.** Stay within `PROJECT.md`. Anything new (a feature, a permission, a dependency, a background service, a new wire request) is a checkpoint question with a
  drafted ADR, not a silent addition. Fast Pair Account Linking / ownership at re-pairing is out of scope (ADR-008).
- **Subagents.** A subagent may only read and report. Every write is done in the main session and verified; re-derive any number a subagent reports before it goes
  into a file.
- **Files.** Before deleting, moving or renaming any file, diff a fresh listing of the source against the copy (checksums). Never `rm -rf` from memory of an earlier
  listing; ask before any delete. The doc tools skip `dist/` (`scripts/lint_docs.py`, `9e2a475`); do not run any tool that writes into `dist/`.
- **Commits.** Commit and push only after the maintainer confirms the final summary (task 20).
- **No new capture skeleton.** Do not create a new `CAP-NNN` folder, skeleton or `planned` registry row in this session; open steps go to the findings and `TODO.md`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0067_CAPTURE_PROMPT_2026_10_03.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0067_CAPTURE_PROMPT_2026_10_03
