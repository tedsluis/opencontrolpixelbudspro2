# 0061_CAPTURE_PROMPT_2026_10_01.md — Full analysis of CAP-065 (Group BA, the Pixel 9a / GrapheneOS robustness, UI and `ai-sessions/0059`-fixes run)

**Number:** 0061
**Category:** CAPTURE
**Date:** 2026-10-01
**Title:** Fully analyse `CAP-065` (video with an audio track, HCI snoop log, app debug export, app logcat, system log), recorded while following the
Group BA skeleton in `CAP-065-EVENT-NOTES.md`; record the real events in **CAP-065-EVENT-NOTES.md** and the analysis in **CAP-065-FINDINGS.md**; establish
what was actually done; adjust the planned `CAP-066` skeleton where the results call for it; **no change to the app itself**

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this repository. Run
the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action, take the time to read **in full**, in this
order, to understand the ground rules before auditing anything: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md` (in particular §2.4,
§3.1, §5, §6, §6.0b and every dated Update), `PROTOCOL.md` (in particular §2.2a with its 2026-09-30 L-1 Update, §2.3, §4.1 with its 2026-10-01 Update,
§4.3 Option B/F, §4.5, §6), `DECISIONS.md` (**every** ADR, ADR-001 … ADR-049, with every dated Update — in particular ADR-032, ADR-034, ADR-042, ADR-043,
ADR-044, ADR-045, ADR-046, ADR-047, ADR-048, ADR-049), `TODO.md`. Then, per task, the sections it needs:

- `AI_SESSION_LOG_PROCEDURE.md` (in full), `ai-sessions/INDEX.md`, `id_registry.csv` (the `CAP-064`, `CAP-065`, `CAP-066` rows).
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3, §5, §8, §9 and the Group BA / Group BB sections; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` for every Test-ID the skeleton
  names (`ANC-001`–`ANC-004`, `PAIR-001`, `PAIR-003`, `CASE-004`–`CASE-006`, `BATT-004`, `EQS-001`, `EQP-002`, `AUDIO-001`, `AUDIO-003`, `HOLD-001`,
  `INEAR-001`); `APP_TESTPLAN.md` sections A, B, F, K, L, O and P (the steps the skeleton references: F7, K1–K5, L2/L3, A5, (E), B4).
- What the build under test contains: `ai-sessions/0059_MAINTENANCE_RESULT_2026_09_30.md` (the app fixes — atomic transitions, the ANC tile's first value,
  undecodable reads, the write quarantine, EQ sliders disabled until read, `AppUiSession`; leads L-1 … L-5) and `ai-sessions/0057_FEATURE_RESULT_2026_09_28.md`
  (the Material 3 UI, pull to refresh, `refreshSettings()`).
- The **skeleton** `captures/CAP-065-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BA/CAP-065-EVENT-NOTES.md` **as committed** (`git show HEAD:<path>` — it is the
  procedure the maintainer followed: sections I–V, Z; steps S0–S2, BA-1 … BA-17, F7, K1–K5, L2/L3, A5, (E), B4, Z1/Z2).
- **The previous run on the same phone and the same build, and its analysis — read in full:** `captures/CAP-064-2026-10-01_10-04-14_10-29-28-Group_AZ/CAP-064-EVENT-NOTES.md`
  and `…/CAP-064-FINDINGS.md` (the layout to follow, the clock-offset method, the session-end and lid-open tables, the Settable table, the improvements of §9),
  and `ai-sessions/0060_CAPTURE_RESULT_2026_10_01.md` (its findings, the maintainer's decisions and **§H "Draft outline for the next FEATURE prompt"**). Also the
  layout examples `captures/CAP-063-2026-09-27_15-57-33_16-25-03-Group_AY/CAP-063-EVENT-NOTES.md` and `…/CAP-063-FINDINGS.md`.
- The planned skeleton `captures/CAP-066-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BB/CAP-066-EVENT-NOTES.md` (you may have to adjust it, §4 task 15).
- Every Kotlin source under `android/` that a finding touches, **in full**, before you write about it — at least `AncTileService.kt`, `EqScreen.kt` (slider
  enabled state), `ConnectionStateMachine.kt`, `BudsRepositoryImpl.kt` (connect, loss, re-open, `refreshSettings`, the write quarantine), `SessionReopener.kt`,
  `OsConnectionObserver.kt`, `MainActivity.kt`, `AppUiSession.kt`, the pairing code (`BudsCompanionPairing.kt`, `PairingLogic.kt`) and their tests.

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
**ai-sessions/0061_CAPTURE_RESULT_2026_10_01.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and update that block at
the end of every phase **and** after every substantial step within a phase (every 5 minutes of film reviewed, every channel decoded, every log file read,
every checkpoint answer): what is done, what is next, which files are touched but unverified, and where intermediate results live (the scratchpad directory —
scripts, contact sheets, decoded tables, extracted audio, notes; re-create them if the scratchpad is gone). A resumed session reads this prompt, then the
RESULT's Progress block, re-reads the files the unfinished step touched, and continues from there. It never redoes a finished, recorded step, and never
assumes an unrecorded step was done. The prompt keeps its number and date; the RESULT keeps growing in the same file.

**Nothing is taken on trust, including this prompt, the skeleton and the maintainer's statements.** Every claim — and every number measured below (§2) — is
re-derived from the film, the HCI log, the other logs, the code and official documentation.

---

## 1. What the maintainer asked for (chat, 2026-10-01, translated)

1. **Analyse `CAP-065` (Group BA) fully and record the findings.**
   - Analyse **CAP-065-recording.mp4** first and record every event and action with its time in **CAP-065-EVENT-NOTES.md**.
   - Analyse **CAP-065-btsnoop_hci.log** with `tshark`.
   - Then correlate the events of `CAP-065-EVENT-NOTES.md` with the HCI log **and the other log files**; `CAP-063-EVENT-NOTES.md` is the example.
   - Run an extensive analysis and record the findings in **CAP-065-FINDINGS.md**; `CAP-063-FINDINGS.md` is the example.
   - If needed, adjust the planned `CAP-066-EVENT-NOTES.md`.
   - `CAP-064-FINDINGS.md` and `ai-sessions/0060_CAPTURE_RESULT_2026_10_01.md` (incl. §H) may hold relevant findings.
2. **Not every step was done exactly as written in the plan** — among other things because the maintainer was filming their ears. So: **establish from the
   film and the logs what was actually done**, in the order it was done — never fill the timeline from the skeleton. Map each real action to the skeleton's
   step IDs and mark each step **done**, **done differently**, **repeated** (each repetition its own timeline row), **skipped** or **not identifiable**.
3. **Facts the maintainer gave (record them as the maintainer's statements; check each against the evidence where possible):**
   - **The app build is the latest build and the same build as `CAP-064`.** Check it from the logs (log wording, wire behaviour, the installer/process-start
     lines of the system log, an Info screen if one is filmed). Note: `CAP-064-FINDINGS.md` §0 showed that no log of that run carries the commit hash; say
     exactly what can and cannot be established, and whether anything contradicts "same build as `CAP-064`" (e.g. a reinstall or a data clear between the runs).
   - **The maintainer did not speak on the film.** The audio track carries no narration; what was heard inside the buds is not on it. The skeleton's "say
     aloud" instructions were therefore not followed — treat every such step as "not on the audio".
   - **Which bud is on film:** when an ear is visible, **the maintainer's head on the right of the frame = the Left bud; the head on the left of the frame = the
     Right bud.** Use this mapping for every wear step and say in each row which side of the frame the head was on. Cross-check it against the runtime-info
     stream's per-bud charging field (6.2.2 Left / 6.3.2 Right, `PROTOCOL.md` §4.3 Option F) at every dock change and report any contradiction (`CAP-064`: 0 in 5).
   - Known from `CAP-064`/`0060` (re-confirm, do not assume): Play services' *Nearby devices* permission is **allowed** (maintainer, chat 2026-10-01).
4. **Strict execution rules (the maintainer's own words, translated):**
   - **No assumptions.** Verify everything.
   - **Validate externally.** Use search tools to validate claims against official documentation (Android Developer docs / AOSP sources, the Bluetooth Core
     Specification, the Fast Pair specification) where needed; cite the URL and the exact sentence. (In `0060` developer.android.com reference pages returned only
     the navigation index to the fetch tool; the AOSP sources at `android.googlesource.com/platform/packages/modules/Bluetooth/+/refs/heads/main/framework/…?format=TEXT`
     worked.)
   - **No sampling.** No random spot checks. Review the relevant files 100 %, completely and exhaustively, checking every detail: every second of the video
     (and of its audio track), every packet of the HCI log that belongs to the Buds, every line of the debug export and the app logcat, and every relevant part
     of the system log.
   - **Best practices.** Industry-standard technical review methods: **evidence traceability** (every claim links back to a specific frame number, log line
     or video timestamp) and **structural integrity** checks (files, registry, links, footers).

---

## 2. The evidence

`captures/CAP-065-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BA/` — the skeleton folder; `CAP-065-EVENT-NOTES.md` is committed (the skeleton), the five capture files
are **untracked**. `id_registry.csv` has a `planned` row for `CAP-065`. Measured by the chat that wrote this prompt (2026-10-01) — **re-check each value**:

| File | Role / what to check first |
|---|---|
| **CAP-065-recording.mp4** | ≈ 1,252.98 s (≈ 20 min 53 s), ≈ 979 MB, H.264 + **an AAC audio track** (present; the maintainer did not speak — say what the track does and does not establish). In `CAP-064` the camera burned in an overlay `Oct 1, 2026 HH:MM:SS` and the phone's status bar was on film: check both here. Plan the frame extraction so every second is covered |
| **CAP-065-btsnoop_hci.log** | ≈ 12 k packets (get the exact count), 2026-10-01 11:07:36.017–11:30:07.973 (phone local time). Check encapsulation (`CAP-064`: `Bluetooth H4 with linux header`), "Packet size limit", `cap_len≠len`. Establish which part of the log the film covers |
| **CAP-065-opencontrol-debug-20261001-112907.txt** | the app's own debug export (phone local time), 738 lines, saved 11:29:07. The skeleton's L2/L3 planned **two** exports (Debug mode on, then off) named CAP-065-debug-export.log — only one file is present: establish which of L2/L3 it is, whether a second export was made (film, logcat), and whether the ring buffer covers the whole film window |
| **CAP-065-OpenControl-for-Pixel-Buds-log-14015f5bc461.txt** | Android logcat of the app's package, 606 lines, buffers printed one after another (sort by time). Timestamps were UTC in `CAP-064` (− 2 h 00 min 00.000 s): measure the offset again. Explain every crash, ANR, exception, `tombstoned` line from the evidence (in `CAP-064` it was the bug report's `dumpstate`) |
| **CAP-065-System-log-a52da8511ec6.txt** | Android system log, 64,665 lines, ≈ 10.2 MB. In `CAP-064` it had **no** lines of the Bluetooth process itself — check whether that holds here |
| **CAP-065-EVENT-NOTES.md** | the skeleton (the plan). Rewrite it into the record in the `CAP-064-EVENT-NOTES.md` layout; keep the planned procedure as an unchanged appendix |

File modes: the four files the phone produced are `-rwxr-----` — no executable bit on capture files (fix in Phase 0, with approval). No events file and no
`CAP-065-btsnoop_hci.log.last` are present — record that. `git check-attr filter` → `lfs` for all five capture files (check). A `zip/` sub-folder that was briefly
in this folder during `0060` is no longer present — if it reappears, ask before touching it.

**Known pitfalls (from `CAP-063`/`CAP-064` — check, do not assume):**
- With the `H4 with linux header` encapsulation `bluetooth.addr` is empty — pre-filter by the Buds' connection handle(s) from the HCI Connection Complete
  events and map them to the Buds (`AGENTS.md` §13.1); other devices share the log (`CAP-064`: an LE device `c8:cc:a8:e7:48:93`, handle `0x0041`, 🟡 a "Charge 6").
- **RFCOMM DLCIs are session-local:** when the Buds open the RFCOMM multiplexer (their `SABM` on DLCI 0x00) MAESTRO is **0x03** and the Message Stream **0x05**
  (`CAP-064` from 10:17:32). **`scripts/pwrpc_decode.py` reads DLCI 2 only** and keys its reassembly by `bluetooth.src` (empty here, so both directions share one
  buffer) — decode MAESTRO per direction on DLCI 2 **and** 3, with a CRC-32 check per frame (`CAP-064-FINDINGS.md` header describes the method). A single RFCOMM
  payload can hold several pw_hdlc frames and several Message Stream messages.
- The Message Stream (DLCI 0x04/0x05) is shared with Google Play services: separate the app's claims (the export's "RFCOMM channel 0x04 connected" lines) from
  Play services' (its `08 11` and the SASS `07 41 "in-use"`, `07 40`, `07 42` ACKs). `Notify` frames that arrive on Play services' claim are invisible to the app.
- HFP AT commands are on a session-local DLCI and may not be dissected (`bthfp` gave nothing in `CAP-064`): read them from the raw RFCOMM payloads. AVRCP is
  `btavctp or btavrcp` (not `avctp or avrcp`, which errors — `AGENTS.md` §13 step 8).
- `field 17` (balance) values are `sint32` (zigzag) — decode before interpreting.
- The app logs a loss cause up to three times within ≈ 0.2 s ("undetermined" → "the Buds closed …" → "link went down", `CAP-064-FINDINGS.md` §6) — the last one is final.

**Privacy (standing rule, ADR-037).** Check **every** frame of the recording **and the audio track** for personal data: the notification shade (`CAP-064` showed
WhatsApp group/contact names, a LinkedIn name and a bank debit notice), messages, contact names, e-mail addresses, Android's device-details screen, a burned-in
address overlay, faces or voices other than what the maintainer chose to film (the ears). This run includes A5/(E)/B4 (permission screens, Android's Bluetooth
settings, the pairing picker) — check those screens closely. Report what you find and **ask** (earlier choices for `CAP-064`/`CAP-063` do not carry over). If a
blur or an audio cut is wanted, propose the exact `ffmpeg` command, the ranges and a before/after sample. Never log or commit the Buds' full MAC outside what
ADR-010 allows.

---

## 3. Leads and context — verify each before relying on it

1. **Which build is this?** The maintainer says "the latest build, the same as `CAP-064`". The latest app commit on `main` is `b65085a` (re-check with
   `git log -- android/`). Compare with `CAP-064`: the system log's `installer_clear_app_data_caller`/`am_proc_start` lines, the app process id, the log wording.
   A reinstall or data clear between 10:30 and 11:07 would show there.
2. **Section I — the `0059` app fixes:** BA-1 the EQ sliders disabled until the EQ is read (film frame by frame against the `ReadSetting 4:16` answer), the
   presets usable; BA-2/BA-3 the ANC tile's subtitle right after "ready" and after a force-stop (A58-APP-01); BA-4 search the export for "answered with a value
   this app cannot read" and "Late WriteSetting answer dropped" / "Maestro request held" (watch-only — a negative needs the exact command and a positive control,
   `AGENTS.md` §13 step 8).
3. **Section II — the Settable byte, ears visible (ADR-049 with its 2026-10-01 Update: 🟡 "`00` ⇒ no bud worn"; the converse refuted).** Every
   `08 13 00 04 01 e8 <Settable> <mode>` (the app's and Play services') against the film's wear and dock state, using the head-orientation mapping: BA-5 both on
   the table, BA-6 Left docked / Right loose, BA-7 lid closed with Right outside, BA-8 both worn. A `00` with a bud worn would contradict the 🟡; an `e8` with none
   worn adds to the `CAP-064` evidence. Note the time since the buds left the ears/case for every reading (the `CAP-064` §3 hypothesis: `00` follows a removal
   from the ear or a dock, not "not worn" as such).
4. **Section III — lead L-1:** the announced pw_rpc channel per Connect (`GetSoftwareInfo` `RESPONSE`, `call_id` 0xFFFFFFFF; export "Maestro channel announced by
   the Buds: N") against which bud(s) are out of the case (BA-10 only Left, BA-11 only Right) — the 🟡 of `PROTOCOL.md` §2.2a (19/21 = Left/Right core). In
   `CAP-064` the channel was 21 in 8 and 19 in 6 sessions — tabulate this run's sessions against which bud was out first/last too.
5. **Section IV — robustness (`APP_TESTPLAN.md`):** F7 (ANC tap then Home), K4 (rotation, dark mode), K1/K2 (Bluetooth off/on — `BluetoothAdapter.ACTION_STATE_CHANGED`,
   the "Bluetooth is disabled" state, `AGENTS.md` §2), K3 (out of range — ACL `Disconnection Complete` reason), L2/L3 (exports), K5 (GrapheneOS auto-off — the
   system log's `BluetoothAutoOff` lines), A5 (*Nearby devices* denied), (E) (Buds forgotten — bond state), B4 (double "Pair a device" — one picker,
   `PairingFailure.AlreadyInProgress`), Z1 (re-pairing: CDM association, bonding, SSP/CTKD per `PROTOCOL.md` §5.1/ADR-030 — no Account Linking analysis, ADR-008).
6. **Section V — pull to refresh (`ai-sessions/0057` D-10/D-11):** BA-12 … BA-17 against the skeleton's "Refuted if" list, **applied literally** (exactly seven
   reads in order on Controls; EQ read first on Sound; one claim per pull on Connection/Find/ANC; Connect on a pull while disconnected; nothing on the wire for
   Debug or an (i)).
7. **The "no automatic connect when the case is opened" finding of `CAP-064` §1** (the maintainer chose "Nothing now"): for every lid-open and every moment the
   Buds' ACL comes up while the app is visible in this run, add rows to the same table (film time, tab, visibility, HCI event, link reading, re-open line, Connect
   tap). Confirm or contradict `CAP-064`'s conclusion (no ACL until a bud leaves the case; 🔴 why Android sometimes re-pages the docked Buds right after an ACL drop).
8. **Session ends and re-opens** — the full table as in `CAP-064-FINDINGS.md` §6 (Buds-side `DISC` with the ACL up, ACL drop with reason, Disconnect tap,
   Bluetooth off, range, permission revoked, unpaired), the cause the app displayed, and whether ADR-044's rules held (one attempt per event, only while visible,
   none after a Disconnect tap, none while the link is down, the 10 s chain guard).
9. **Battery and case** — every value shown on film against the wire value and its receive time (`03 03`, runtime-info 6.1/6.2/6.3).
10. **The logs themselves** — the debug export line by line, the app logcat (crashes, ANRs, exceptions, StrictMode, the activity lifecycle around K4/A5/(E)/B4),
    the system log (Bluetooth state changes, bond state, CDM, permission changes, GrapheneOS auto-off, the app's process lifecycle).
11. **Anything new:** NAKs, pw_rpc error statuses, Safe Mode (ADR-042), any app frame on DLCI 0x08/0x0a (must be none), any request outside the user's actions,
    other hosts or devices.
12. **Relevance for the next FEATURE session** (`ai-sessions/0060` RESULT §H: ANC `Get` before every `Set` with new wording; a settings menu behind a gear icon
    with Settings (dark mode On/Off/System), Debug and Info (Buds and Case firmware, app build number)): note every `CAP-065` finding that bears on it — e.g. K4's
    dark mode, the Debug screen's behaviour (BA-17, L2/L3), which `GetSoftwareInfo` entry is the Case, any further stale-availability ANC `Set`.

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename, mode change or `git add`)

1. Create the RESULT file (§0). Record `git log -1`, `git status --short`, `git fetch && git log --oneline HEAD..origin/main`. Confirm the `planned` row of
   `CAP-065` in `id_registry.csv` and the Group BA section and Capture Index row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`. Identify every file with `capinfos`,
   `ffprobe` and `sha256sum` (packets, duration, first/last timestamps, frame rate, rotation, audio track, `frame.cap_len == frame.len`, file modes) and
   re-check every value in §2.
2. Privacy check of the whole recording, video **and audio** (§2). Summarise the result.
3. Plan the migration (ADR-037, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9): rename the folder to `captures/CAP-065-YYYY-MM-DD_HH-mm-ss_HH-mm-ss-Group_BA/` from the
   film's own first/last clock times (a burned-in overlay if there is one, else the status-bar clock — say which); keep or rename the debug export; `chmod 644`;
   the registry row; the Capture Index row; Git LFS. **Ask the maintainer (`AskUserQuestion`)** about the privacy outcome and the migration plan together, before
   moving anything.
4. Execute only what was approved. Before moving or replacing anything, compare checksums of a fresh listing of the source against the destination; never
   `rm -rf` based on an earlier listing. Update every reference to the old folder name (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TODO.md`, `_sidebar.md`,
   `id_registry.csv`, `CAP-066-EVENT-NOTES.md` if it names it); earlier `ai-sessions/` files are history — leave them as written and note it.

### Phase A — the video and its audio, in full

5. Scan the whole duration (a fixed interval of ≤ 2 s, plus scene changes), then narrow every transition to ≤ 1 s, and to a single frame where a claim depends on
   it: every lid opening/closing, every bud taken out of or put into the case or an ear (with the head-orientation mapping), every tap (Connect, Disconnect,
   Refresh, a mode, a tile, a pull, a checkbox, a switch, Export), every tab change, rotation, dark-mode change, Bluetooth toggle, every Android settings
   screen (permissions, Bluetooth, pairing picker), every Home / return to the app. Read every visible on-screen text (status lines with their times, Quick
   Settings and the tile's subtitle, toasts, (i) dialogs, Android's screens). Crop and zoom the ear, case and slider regions.
6. The **audio track**: confirm it decodes, extract it (`ffmpeg -i CAP-065-recording.mp4 -vn -ac 1 …`), inspect it completely (a per-second level plot and a
   full-length spectrogram) and list what can be heard with times. Use an audible event only to time events, and say so.
7. Measure the film ↔ phone clock offset at the start **and** the end (a status-bar minute flip against the overlay, at ≥ 4 fps — never a single point), and the
   logcat/system log ↔ phone offset (a logged event that appears in both).
8. Rewrite **CAP-065-EVENT-NOTES.md** in the `CAP-064-EVENT-NOTES.md` layout: header status, Log Metadata (phone and GrapheneOS build, OpenControl build — what
   the evidence establishes, firmware, Play services *Nearby devices*, other devices, film/log ranges, clock offsets, the wear mapping and its cross-check),
   capture-integrity pre-flight, video/audio review method (incl. privacy), an Event Timeline (phone time, action, actor, step ID, registry Test-ID, evidence),
   a step-mapping table, the analysis checklist, and the skeleton as an unchanged appendix. **Traceability (`AGENTS.md` §13.7):** every skeleton step and every
   Test-ID the skeleton names appears in the timeline or is explicitly "skipped" / "not identifiable"; every repetition is its own row.

### Phase B — the HCI log and the other logs, in full

9. Follow `AGENTS.md` §13. Pre-filter by the Buds' handle(s), take the full RFCOMM inventory (`SABM`/`UA`/`DISC`/`DM`, direction, per ACL session and per
   multiplexer side), then decode **every** Buds packet: MAESTRO on DLCI 2 **and** 3 (every `GetSoftwareInfo` with its channel, `ReadSetting`/`WriteSetting`/
   `SubscribeRuntimeInfo`/`SERVER_STREAM` with field, value — zigzag for 17 — and status; CRC-32 per frame); the Message Stream per claim (app vs Play services;
   ACK/NAK with reason, `Notify ANC state` with its Settable byte and mode, battery, SASS); DLCI 0x08/0x0a (who opens them); HFP; AVRCP; every ACL and LE event
   with its reason code; the pairing at Z1 (bonding events, SSP/CTKD). Every negative with its command, exit status and a positive control.
10. Read the debug export, the app logcat and the relevant parts of the system log **line by line** and put each relevant line on the timeline (phone time, after
    the measured offsets). For every filmed action write the exact frames and log lines: what the app sent, on which channel, what the Buds answered, what the
    app logged and showed.
11. Build the tables the findings need: section I (slider state vs EQ read, tile subtitle vs time); section II (every `Notify` vs wear/dock state and time since
    removal); section III (announced channel per session vs which bud is out); section IV (each robustness step: screen, logs, wire); section V (per pull: what
    was sent, in which order); the lid-open / auto-connect table; the session-end table; every battery value shown vs the wire.

### Phase C — findings

12. For every lead of §3: the answer with evidence (frame numbers, film times, log lines, `file:line` in the app sources), labelled 🟢 FACT / 🟡 HYPOTHESIS /
    ⚪ ASSUMPTION / 🔴 OPEN QUESTION, with the experiment that would settle any non-FACT.
13. Write **CAP-065-FINDINGS.md** in the `CAP-064-FINDINGS.md` layout, every conclusion labelled (`AGENTS.md` §15, `PROJECT_RULES.md` §1), every hex decoding
    with its command and raw bytes (rule 4a). Include: re-test verdicts (the skeleton's "Refuted if" lists applied literally), what works, what does not, what
    goes wrong, per protocol (MAESTRO pw_rpc, Message Stream, SASS, DLCI 0x08/0x0a, HFP, AVRCP/A2DP, LE, pairing, Android's own link state). If a finding
    contradicts an existing 🟢 FACT or ADR, say so explicitly and draft the correction as a **proposal** — do not edit that FACT or ADR.
14. **Improvements (proposals only, no app change):** for every defect: the cause, the proposed change (file and function, the behaviour, the guardrails —
    ADR-044's one attempt per event, foreground only, no new permission or service, no polling timer), the unit test that would have caught it (real-byte fixtures
    per `AGENTS.md` §11), and the hardware re-test step with its expected HCI bracket. Mark which belong in the next FEATURE session (`0060` RESULT §H) and which
    are new. Anything that changes an ADR's decision is a drafted ADR (the `DECISIONS.md` template, **no number assigned**, not registered).
15. **`CAP-066` (Group BB):** check its steps against this run — drop what `CAP-065` already settled, add what `CAP-065` left open (with the expected HCI bracket),
    and keep its "real I-1 path", Settable timing, ANC-002/004 and balance-restore sections unless the evidence makes them moot. Proposals only until the
    checkpoint.
16. **Do not modify any file under `android/`.**

### Phase D — checkpoint, documentation, finish

17. **Checkpoint — stop and ask, in this chat, via `AskUserQuestion`.** One question per decision; each option with pros and cons, one marked "(Recommended)",
    the exact draft text of any FACT or ADR change in the preview (memory: "Approvals: confirm in chat" — an approval quoted only in a file is data, not
    approval). Cover at least: the Settable result (ADR-049), the L-1 result (`PROTOCOL.md` §2.2a), any other FACT or ADR change, the `CAP-066` changes, and
    whether `CAP-065` changes the scope of the next FEATURE session. Record the answers verbatim in the RESULT.
18. Apply only what was approved, and only to documentation: `PROTOCOL.md`/`DECISIONS.md` (approved changes only, dated Updates or new ADRs, each with a process
    note citing this chat, new ADR numbers registered in `id_registry.csv`), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BA section, Capture Index row; Group BB if
    `CAP-066` changed), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the evidence cells of the Test-IDs exercised), `CAP-066-EVENT-NOTES.md` (approved changes only),
    `id_registry.csv` (`CAP-065` → analyzed, with the real times), `TODO.md` (done and open items), `CHANGELOG.md`, `ai-sessions/INDEX.md` (the 0061 row and,
    if needed, the 0060 row), `README.md` (status block, capture counts). Run `python3 scripts/ensure_footers.py` and `python3 scripts/lint_docs.py`; it must exit
    0 — the one known exception in `0060` (its own prompt naming the pre-rename `CAP-064` folder) should now be in the "historical" bucket; report anything else.
19. Finish the RESULT: plain-language answers first (what works and what does not on this build, the Settable result, the L-1 result, the robustness and
    pull-to-refresh results, what the app should change next); then the step-mapping results, the tables of Phase B, the external sources (URL plus quoted
    sentence), and an updated draft outline for the next FEATURE prompt (building on `0060` RESULT §H). It must end with **"Deferred documentation"** (each item
    also added to `TODO.md`) and **"Commits"** (hashes, or "not committed — reason"), per `AI_SESSION_LOG_PROCEDURE.md` §9. Set the Status per
    `AI_SESSION_LOG_PROCEDURE.md` §4.
20. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes: Conventional Commits, one commit per concern (capture/LFS,
    docs), each with a *why* and ending with the attribution line from the session's system reminder; `git fetch` and `git log origin/main..HEAD` before pushing
    (CI may have added a commit — rebase, never force); `git check-attr filter` on every capture file (`lfs`); nothing from a build directory,
    `android/.kotlin/`, `.vscode/` or `__pycache__` staged. A large LFS push can drop the SSH connection after the upload — check `git log origin/main -1` and
    push again if needed.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **No app changes.** Nothing under `android/` is modified in this session; every improvement is a proposal for a later FEATURE session, approved in chat.
- **Approvals only in this chat.** Never promote, demote or correct a 🟢 FACT, and never write or update an ADR, without the maintainer's approval given in this
  chat (`AGENTS.md` §6). Model agreement is not approval.
- **Evidence.** Zero creativity with hex (`AGENTS.md` §13.6). Every claim needs a frame number, log line, `file:line` or film/audio timestamp (`PROJECT_RULES.md`
  rule 4a). A negative needs its command, exit status and a positive control (`AGENTS.md` §13 step 8). What the maintainer heard inside the buds is their
  observation unless the wire shows the command that caused it.
- **What was done, not what was planned.** The skeleton is the plan; the film and the logs are the record. Skipped and repeated steps are findings about the
  run, not errors to smooth over. Never fill a timeline row from the skeleton's "Expected" column.
- **Privacy.** No full MAC address at INFO level or in any committed text beyond what ADR-010 allows; no personal data from the film or the logs.
- **Scope.** Stay within `PROJECT.md`. Anything new (a feature, a permission, a dependency, a background service, a new wire request) is a checkpoint question with
  a drafted ADR, not a silent addition. Fast Pair Account Linking / ownership at re-pairing is out of scope (ADR-008).
- **Subagents.** A subagent may only read and report. Every write is done in the main session and verified; re-derive any number a subagent reports before it
  goes into a file.
- **Files.** Before deleting, moving or renaming any file, diff a fresh listing of the source against the copy (checksums). Never `rm -rf` from memory of an
  earlier listing.
- **Commits.** Commit and push only after the maintainer confirms the final summary (task 20).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0061_CAPTURE_PROMPT_2026_10_01.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0061_CAPTURE_PROMPT_2026_10_01
