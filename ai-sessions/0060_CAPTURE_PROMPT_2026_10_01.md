# 0060_CAPTURE_PROMPT_2026_10_01.md — Full analysis of CAP-064 (Group AZ, the Pixel 9a / GrapheneOS wear-and-settings re-test of the 0054, 0056 and 0059 builds) and the "no automatic connect when the case is opened" observation

**Number:** 0060
**Category:** CAPTURE
**Date:** 2026-10-01
**Title:** Fully analyse `CAP-064` (video with an audio track, HCI snoop log, app debug export, app logcat, system log), recorded while following the
Group AZ skeleton in `CAP-064-EVENT-NOTES.md`; record the real events in **CAP-064-EVENT-NOTES.md** and the analysis in **CAP-064-FINDINGS.md**;
establish what was actually done; verify the maintainer's observation that OpenControl does **not** connect by itself when the case is opened, find its
cause in the code and the logs, and propose — as proposals only — what would fix it; **no change to the app itself**

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this
repository. Run the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action, take the time to read **in full**, in
this order, to understand the ground rules before auditing anything: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md` (in particular
§3.1, §5, §6, §6.0b and every dated Update), `PROTOCOL.md` (in particular §2.3, §4.1, §4.3, §4.5 with §4.5.3 "Touch & Hold" and §4.5.5 "In-ear
detection", §6), `DECISIONS.md` (**every** ADR, ADR-001 … ADR-049, with every dated Update — in particular ADR-016, ADR-024, ADR-032, ADR-036,
ADR-042, ADR-044, ADR-045, ADR-046, ADR-047, ADR-048, ADR-049), `TODO.md`. Then, per task, the sections it needs:

- `AI_SESSION_LOG_PROCEDURE.md` (in full), `ai-sessions/INDEX.md`, `id_registry.csv` (the `CAP-064` and `CAP-065` rows).
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3, §5, §8, §9 and the Group AZ / Group BA sections; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` for every Test-ID the
  skeleton's §A.7 names (`ANC-001`–`ANC-004`, `INEAR-001`–`INEAR-004`, `HOLD-001`–`HOLD-005`, `CASE-004`–`CASE-006`, `BATT-004`, `AUDIO-003`,
  `PAIR-003`); `APP_TESTPLAN.md` for any step the skeleton references.
- What the build under test contains: `ai-sessions/0054_FEATURE_RESULT_2026_09_28.md` (I-1 … I-5 and its re-test plan),
  `ai-sessions/0056_FEATURE_RESULT_2026_09_28.md` (section VII, AZ-1 … AZ-8), `ai-sessions/0057_FEATURE_RESULT_2026_09_28.md` (the Material 3 UI —
  tabs, top-bar Debug, pull to refresh), `ai-sessions/0059_MAINTENANCE_RESULT_2026_09_30.md` (the app fixes: atomic transitions, tile, undecodable
  reads, write quarantine, EQ sliders, `AppUiSession`; and the `CAP-064` → `CAP-065` split).
- The **skeleton** `captures/CAP-064-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AZ/CAP-064-EVENT-NOTES.md` **as committed** (`git show HEAD:<path>` — it is the
  procedure the maintainer followed; sections VI and VIII were moved to `CAP-065` and are not part of this run).
- The worked example of the previous run of this app on the same phone: `captures/CAP-063-2026-09-27_15-57-33_16-25-03-Group_AY/CAP-063-EVENT-NOTES.md`
  and `…/CAP-063-FINDINGS.md` (layout **and** content: the clock offsets, the session-end table, the Settable byte), and `CAP-056-FINDINGS.md` §4
  (in-ear detection off with the official app, the field-12 bit order).
- Every Kotlin source under `android/` that a finding touches, **in full**, before you write about it — at least `SessionReopener.kt`,
  `BudsRepositoryImpl.kt` (connect, loss and re-open paths), the `:app` visibility / `OsConnectionObserver` / `AppUiSession` code, and their tests
  (`SessionReopenerTest.kt`, `BudsRepositoryImplTest.kt`).

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
**ai-sessions/0060_CAPTURE_RESULT_2026_10_01.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and update that
block at the end of every phase **and** after every substantial step within a phase (every 5 minutes of film reviewed, every DLCI decoded, every
log file read, every checkpoint answer): what is done, what is next, which files are touched but unverified, and where intermediate results live (the
scratchpad directory — scripts, contact sheets, decoded tables, extracted audio; re-create them if the scratchpad is gone). A resumed session reads
this prompt, then the RESULT's Progress block, re-reads the files the unfinished step touched, and continues from there. It never redoes a finished,
recorded step, and never assumes an unrecorded step was done. The prompt keeps its number and date; the RESULT keeps growing in the same file.

**Nothing is taken on trust, including this prompt, the skeleton and the maintainer's observation.** Every claim — and every number measured below
(§2) — is re-derived from the film, the HCI log, the other logs, the code and official documentation.

---

## 1. What the maintainer asked for (chat, 2026-10-01, translated)

1. **Analyse `CAP-064` (Group AZ) fully and record the findings.**
   - Analyse **CAP-064-recording.mp4** first and record every event and action with its time in **CAP-064-EVENT-NOTES.md**.
   - Analyse **CAP-064-btsnoop_hci.log** with `tshark`.
   - Then correlate the events of `CAP-064-EVENT-NOTES.md` with the HCI log **and the other log files** (debug export, app logcat, system log);
     `CAP-063-EVENT-NOTES.md` is the example.
   - Run an extensive analysis and record the findings in **CAP-064-FINDINGS.md**; `CAP-063-FINDINGS.md` is the example.
2. **Not every step was done exactly as written in the plan** — among other things because the maintainer was filming their ears. So: **establish
   from the film and the logs what was actually done**, in the order it was done — never fill the timeline from the skeleton. Map each real action to
   the skeleton's step IDs (S0–S2, I-0, I-1a–d, AY-3a–c, I-2a/b, I-3a–d, I-4a/b, I-5, AZ-1 … AZ-8, Z1/Z2) and mark each step **done**, **done
   differently**, **repeated** (each repetition its own timeline row), **skipped** or **not identifiable**. Note any `CAP-065` step that was done
   anyway (it is recorded, not analysed as `CAP-065`).
3. **Facts the maintainer gave (record them as the maintainer's statements; check each against the evidence where possible):**
   - **The app version is the latest build** — confirm from the logs which commit/build that is (§3 lead 1); the skeleton's P1 hash may not have been
     written or said aloud.
   - **The maintainer did not speak on the film.** The audio track exists, but it carries no narration; what was heard inside the buds is not on it.
   - **Which bud is on film:** when an ear is visible, **the maintainer's head on the right of the frame = the Left bud; the head on the left of the
     frame = the Right bud.** Use this mapping for every wear step (I-1, AY-3, AZ-6b) and say in each row which side of the frame the head was on.
     Where the wire carries a per-bud signal at that moment (e.g. a per-bud charging/dock bit), cross-check the mapping against it and report any
     contradiction.
4. **The maintainer's observation — verify it with evidence (confirm, refute or qualify):** *when the OpenControl app is open and the Buds case is
   opened, the app does **not** connect by itself; the maintainer has to tap Connect. This holds on every tab (Connection, ANC, Sound, Controls and
   Find).* This contradicts what ADR-044 item 1(b) specifies ("when Android's link comes back", while the app is visible) and what `CAP-063`
   observed (the maintainer's observation 5 there: "as soon as a bud is taken out of the case, the app connects by itself") — so it may be a
   regression in a later build (`0054`/`0056`/`0057`/`0059`), a condition the re-open does not cover (e.g. the ACL coming up while a different
   UI state is shown), or a difference in what "opening the case" did on this run. Establish which, with evidence (§3 lead 2).
5. **Strict execution rules (the maintainer's own words, translated):**
   - **No assumptions.** Verify everything.
   - **Validate externally.** Use search tools to validate claims against official documentation (Android Developer docs, Bluetooth Core
     Specification, Fast Pair specification) where needed; cite the URL and the exact sentence.
   - **No sampling.** No random spot checks. Review the relevant files 100 %, completely and exhaustively, checking every detail: every second of the
     video (and of its audio track), every packet of the HCI log that belongs to the Buds, every line of the debug export and the app logcat, and every
     relevant part of the system log.
   - **Best practices.** Industry-standard technical review methods: **evidence traceability** (every claim links back to a specific frame number,
     log line or video timestamp) and **structural integrity** checks (files, registry, links, footers).

---

## 2. The evidence

`captures/CAP-064-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AZ/` — the skeleton folder; `CAP-064-EVENT-NOTES.md` is committed (the skeleton), the five capture
files are **untracked**. `id_registry.csv` has a `planned` row for `CAP-064`. `git check-attr filter` → `lfs` for all five capture files
(2026-10-01). Measured by the chat that wrote this prompt (2026-10-01) — **re-check each value**:

| File | Role / what to check first |
|---|---|
| **CAP-064-recording.mp4** | ≈ 1,514.8 s (≈ 25 min 15 s), ≈ 1.18 GB, H.264 1280×720 at ≈ 29.87 fps (`r_frame_rate` 90000/3013), **an AAC stereo audio track** — present, but the maintainer did not speak (§1.3): say what the track does and does not establish (room sounds, the phone's speaker, tones), never more. Plan the frame extraction so every second is covered |
| **CAP-064-btsnoop_hci.log** | ≈ 11 k packets (get the exact count), 2026-10-01 10:04:23.294–10:31:21.841 (phone local time), "Packet size limit: (not set)" (raw path), encapsulation `Bluetooth H4 with linux header`. The log is ≈ 27 min, the film ≈ 25 min: establish which part of the log the film covers |
| **CAP-064-opencontrol-debug-20261001-103023.txt** | the app's own debug export (phone local time), 1,130 lines, first line 08:53:29.939 ("Permissions (start) …" — the app process started ≈ 1 h before the HCI log), last line 10:30:19.222 ("Android link: PENDING -> CONNECTED (trigger: profile 2 bound) via profiles [2]"). Note: its name differs from the one the skeleton planned (A.1, "App debug export"). It was saved at 10:30:23 — the HCI log runs one more minute (to 10:31:21); that tail is not in the export. Check that the ring buffer did not wrap and that the whole film window is inside it |
| **CAP-064-OpenControl-for-Pixel-Buds-log-49ab12ce3f7f.txt** | Android logcat of the app's package, 401 lines, header `osVersion: google/tegu/tegu:17/CP3A.260905.009/2026092501`, `package: io.github.tedsluis.opencontrolpixelbuds:1, targetSdk 34`, buffers `main,system,crash,events,kernel` printed **one after another** (sort by time before correlating). Timestamps look like **UTC** (06:53:29 for the 08:53:29 local process start) — measure the offset (`CAP-062`/`CAP-063` found −2 h 00 min 00.0 s). Its last line, `10-01 08:29:56.385 … I ontrolpixelbuds: Wrote stack traces to tombstoned`, needs an explanation from the evidence (an ANR? a stack dump requested by the log viewer at collection time?) — do not guess |
| **CAP-064-System-log-11c30e3704a6.txt** | Android system log, 61,308 lines, ≈ 9.5 MB, same header, same buffer layout |
| **CAP-064-EVENT-NOTES.md** | the skeleton (the plan). Rewrite it into the record in the `CAP-063-EVENT-NOTES.md` layout; keep the planned procedure as an unchanged appendix, as `CAP-063` did |

File modes: the four files the phone produced (`.txt`, `.mp4`) are `-rwxr-----` — no executable bit on capture files (fix in Phase 0, with approval).
No events file (skeleton P6) and no `CAP-064-btsnoop_hci.log.last` are present — record that.

**Known pitfalls (check, do not assume):** with the `H4 with linux header` encapsulation `bluetooth.addr` is empty — pre-filter by the Buds'
connection handle(s) taken from the HCI Connection Complete / LE Connection Complete events and map them to the Buds (`AGENTS.md` §13.1); other
devices may share the log (`CAP-063`: a "Charge 6" speaker); RFCOMM DLCI numbers are session-local — identify each channel by its content (MAESTRO
pw_hdlc, Message Stream `[Group][Code][Length]`); a single RFCOMM payload can hold several pw_hdlc frames and several Message Stream messages; HFP
frames are consumed by the HFP dissector (`data.data` empty); AVRCP is `btavctp or btavrcp` (not `avctp or avrcp`, which errors — `AGENTS.md` §13
step 8); `scripts/pwrpc_decode.py` prints `sint32` values (field 17) as **raw** zigzag varints — decode before interpreting.

**Privacy (standing rule, ADR-037).** Check **every** frame of the recording **and the audio track** for personal data: the notification shade,
messages, contact names, e-mail addresses (`CAP-063` had WhatsApp/LinkedIn names and an e-mail address in the shade), a burned-in address overlay,
faces or voices other than what the maintainer chose to film (the ears). Report what you find and **ask** (earlier choices for `CAP-062`/`CAP-063`/
`CAP-056` do not carry over). If a blur or an audio cut is wanted, propose the exact `ffmpeg` command, the ranges and a before/after sample. Never log
or commit the Buds' full MAC outside what ADR-010 allows.

---

## 3. Leads and context — verify each before relying on it

1. **Which build is this?** The maintainer says "the latest build" (§1.3). The latest app commit on `main` is `b65085a` ("fix(app): process the 0058
   app findings …", `ai-sessions/0059`); later commits are docs/CI only — re-check with `git log -- android/`. Confirm the build from the logs, not
   from dates: log wording or wire behaviour that exists only from `0059` on (e.g. the `AppUiSession`, write-quarantine and undecodable-read lines;
   compare the strings in the `0059` sources with the export), from `0057` on (no Debug tab — Debug in the top bar), and from `0056` on (`ReadSetting 4:12`
   and `4:2` in the Connect burst). Report anything that contradicts it.
2. **The observation (§1.4) — no automatic connect when the case is opened.** For **every** lid-open on film (and every moment the Buds' ACL comes up
   while the app is visible), build a table: film time (phone time), which tab was shown, the app's visibility (resumed/paused — logcat
   `wm_on_resume_called`/`wm_on_paused_called`, the export's visibility lines), Android's link state as the export logs it ("Android link: … ->
   CONNECTED (trigger: …)"), the HCI `Connection Complete` (handle, status) and the profiles that came up (A2DP/AVRCP/HFP, the Buds' own `SABM`s),
   whether the app sent `SABM` on DLCI 0x02 (and when), any re-open line in the export, and when and on which tab the maintainer tapped Connect.
   Then:
   - trace the path in the code that ADR-044 1(b) depends on (`SessionReopener`, `BudsRepositoryImpl`, `OsConnectionObserver`, `AppUiSession` and
     whatever feeds the "Android link" state) and state, with `file:line`, why no re-open was attempted (or why it was attempted and failed) in each
     case — the condition that was false, the event that never arrived, or the state that suppressed it (e.g. a preceding Disconnect tap, ADR-044
     item 2; a "PENDING" link state; a guard added in `0059`);
   - **positive control** (`AGENTS.md` §13 step 8): find at least one case in this capture (or, if none, in `CAP-063`) where the automatic re-open
     **did** fire, and show what was different;
   - check whether the tests (`SessionReopenerTest`, `BudsRepositoryImplTest`) cover the case that failed; if they pass while the hardware fails,
     say exactly which assumption of the test the hardware breaks;
   - check against the Android documentation what the app may rely on when the ACL comes up (e.g. `BluetoothDevice.ACTION_ACL_CONNECTED`,
     `BluetoothProfile.ServiceListener`, `BluetoothA2dp`/`BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED`) and with which permissions — cite the
     page and the sentence; no hidden APIs (`AGENTS.md` §3);
   - verdict: confirmed / refuted / qualified, per tab and per lid-open, and the root cause labelled 🟢 / 🟡 / 🔴.
3. **The re-test items this run was meant to settle** — a verdict per skeleton step (confirmed / refuted / not exercised / not identifiable), with
   frames and log lines, applying the skeleton's "Refuted if" lists **literally**:
   - **I-0 / I-1a–d** (ADR-044 ready by itself; a tap on a disabled ANC mode checks again with **one** claim; `08 11` → `08 13 … 00 …` → no `08 12`;
     with buds worn `Get` and `Set` in the same claim; the Quick Settings tile "Only while worn" + toast).
   - **AY-3a–c** — the test of "Settable `0x00` = no bud worn" (ADR-049, 🟡): every `08 13 00 04 01 e8 <Settable> <mode>` against the film, using the
     head-orientation mapping of §1.3 for which bud is visibly worn. A counter-example either way is the result. Also check whether `CAP-065`'s
     docked/loose variant was (accidentally) exercised.
   - **I-2a/b** (the loss wording after returning to the app; the export's "Session loss cause" lines against the film and the HCI `Disconnection
     Complete` reason; no app `SABM` 0x02 while the app was not visible).
   - **I-3a–d** (balance: `4:{17:0}` after each release near the centre, answered OK; "Centre" shown only for 0; zigzag decoded).
   - **I-4a/b** (the first 3 s after each Connect: previous per-bud lines marked "last seen" / "last connection" until the new report arrives).
   - **I-5** (the Digital-assistant note's text on film).
   - **AZ-1 … AZ-8** (ADR-046/047): every `ReadSetting 4:12` / `4:2` and every `WriteSetting 4:{12:{…}}` / `4:{2:…}` — one per tap, answered by an empty
     `RESPONSE` OK, the screen changing only after it; the field-12 writes byte-compared with `CAP-056` 1815/1725 for the same channel; AZ-3 — every
     `Notify` mode after each long press with Adaptive unticked (**never `40`**); AZ-4 — nothing on the wire for the NC attempt; AZ-6a/d — the SASS
     `07 11 … b0 00` / `… b8 00` if DLCI 0x04 was open; AZ-6b — **no** AVRCP `PlaybackStatusChanged`/PAUSE with in-ear detection off, and whether the
     Buds still `DISC` DLCI 0x02 on the wear change (ADR-044 depends on it); AZ-6c — the Settable byte with in-ear detection off and no bud worn
     (the 🔴 of `CAP-056-FINDINGS.md` §4); AZ-7 — the read-back after a reconnect; AZ-8 — the list hides/reappears and no field-12 write.
   - **S0/S2/Z1/Z2** — the minute changes filmed (clock offset), Debug mode on, the restore of balance, in-ear detection and the ANC-mode list.
4. **Session ends and re-opens** — the full table as in `CAP-063-FINDINGS.md`: every DLCI 0x02 session (open, who closed it: Buds-side `DISC` with the
   ACL up, ACL `Disconnection Complete` with reason, the user's Disconnect, Bluetooth off / GrapheneOS auto-off, range), the cause the app displayed,
   and whether ADR-044's rules held (one attempt per event, only while visible, none after a Disconnect tap). This table is also the evidence base for
   lead 2.
5. **Battery and case** — every value the app showed on film against the wire value and its receive time (DLCI 0x04 `03 03`, the runtime-info
   stream's 6.1/6.2/6.3, ADR-043); "charging in the case" per bud; the Case "last seen". A correct percentage without its own receive time is not
   enough (`ARCHITECTURE.md` §3.1).
6. **The logs themselves** — the debug export line by line (every warning, every `MalformedFrame`/`UnidentifiedFrame` count, every loss cause, every
   settings read/write line), the app logcat (crashes, ANRs, exceptions, StrictMode, the "Wrote stack traces to tombstoned" line and what preceded it),
   the system log (Bluetooth stack lines for the Buds' address — truncated per `AGENTS.md` §9 in anything you write —, GrapheneOS Bluetooth auto-off,
   Play services / Fast Pair contending for DLCI 0x04, `CompanionDeviceManager`, the app's process lifecycle).
7. **Anything new:** NAKs, pw_rpc error statuses, Safe Mode (ADR-042), any app frame on DLCI 0x08 (must be none), any field-12/field-2 request outside
   section VII's taps and the Connect reads, a second host (Pixel 7a Bluetooth should be off), other devices connecting.

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename, mode change or `git add`)

1. Create the RESULT file (§0). Record `git log -1`, `git status --short`, `git fetch && git log --oneline HEAD..origin/main`. Confirm the `planned`
   row of `CAP-064` in `id_registry.csv` and the Group AZ section and Capture Index row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`. Identify every file with
   `capinfos`, `ffprobe` and `sha256sum` (packets, duration, first/last timestamps, frame rate, audio track, truncation check `frame.cap_len ==
   frame.len`, file modes) and re-check every value in §2.
2. Privacy check of the whole recording, video **and audio** (§2). Summarise the result.
3. Plan the migration (ADR-037, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9): rename the folder to `captures/CAP-064-YYYY-MM-DD_HH-mm-ss_HH-mm-ss-Group_AZ/`
   from the film's own first/last clock times (a burned-in overlay if there is one, else the phone's status-bar clock — say which); whether to rename
   the debug export to the skeleton's name; `chmod 644` on the capture files; the registry row; the Capture Index row; Git LFS. **Ask the maintainer
   (`AskUserQuestion`)** about the privacy outcome and the migration plan together, before moving anything.
4. Execute only what was approved. Before moving or replacing anything, compare checksums of a fresh listing of the source against the destination;
   never `rm -rf` based on an earlier listing (memory: "File migration care"). Update every reference to the old folder name (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`,
   `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `TODO.md`, `_sidebar.md`, `id_registry.csv`, the `CAP-065` skeleton); earlier `ai-sessions/` files are history —
   leave them as written and note it.

### Phase A — the video and its audio, in full

5. Scan the whole duration (scene-change detection **and** a fixed interval of ≤ 2 s), then narrow every transition to ≤ 1 s, and to a single frame
   where a claim depends on it: every lid opening/closing, every bud taken out of or put into the case or an ear (with the head-orientation mapping of
   §1.3), every tap (Connect, Disconnect, Refresh, a mode, a checkbox, a switch, the balance slider, the ANC tile), every tab change, every long press,
   every Home / return to the app. Read every visible on-screen text (every tab, every status line with its time, Quick Settings, toasts, Android's
   Bluetooth screens). Crop and zoom the ear and case regions for the wear and lid steps.
6. The **audio track**: confirm it decodes, extract it (`ffmpeg -i CAP-064-recording.mp4 -vn -ac 1 …`), inspect it completely (a level plot or
   spectrogram over the full duration) and list what can be heard with times. No narration is expected (§1.3); use any audible event (a tap, the
   phone's speaker, a tone) only to time events, and say so.
7. Measure the film ↔ phone clock offset at the start **and** the end (S0/Z2 minute changes if filmed; else a visible clock with seconds against a
   logged event — a filmed tap against its frame — never a single point), and the logcat ↔ phone offset.
8. Rewrite **CAP-064-EVENT-NOTES.md** in the `CAP-063-EVENT-NOTES.md` layout: header status, Log Metadata (phone and GrapheneOS build, OpenControl
   build/commit, firmware, Play services *Nearby devices* if visible, the Pixel 7a's Bluetooth state, other devices, film/log ranges, clock offsets),
   capture-integrity pre-flight, video/audio review method (incl. privacy and the head-orientation mapping), an Event Timeline (phone time, film
   time, action, actor, tab, skeleton step ID, registry Test-ID, evidence), a step-mapping table, the analysis checklist, next steps, and the skeleton
   as an unchanged appendix. **Traceability (`AGENTS.md` §13.7):** every skeleton step and every Test-ID the skeleton names appears in the timeline or
   is explicitly "skipped" / "not identifiable"; every repetition is its own row; every lid-open is its own row with the Connect tap that followed.

### Phase B — the HCI log and the other logs, in full

9. Follow `AGENTS.md` §13. Pre-filter by the Buds' handle(s), take the full DLCI inventory (who opens and closes each, `SABM`/`UA`/`DISC`/`DM` and
   direction, per ACL session), then decode **every** Buds packet: MAESTRO (whatever its session-local DLCI) with `scripts/pwrpc_decode.py` — every
   `ReadSetting`/`WriteSetting`/`SubscribeRuntimeInfo`/announcement with field, value (zigzag where the schema says `sint32`) and status; the Message
   Stream — every `[Group][Code][Length]` message (ACK/NAK with reason, `Notify ANC state` with its Settable byte and mode, battery, SASS); DLCI
   0x08/0x0a (who opens them); HFP; AVRCP (`btavctp or btavrcp`, every pass-through and `PlaybackStatusChanged`, with direction); every ACL and LE event
   with its reason code. Every negative with its command, exit status and a positive control (`AGENTS.md` §13 step 8).
10. Read the debug export, the app logcat and the relevant parts of the system log **line by line** and put each relevant line on the timeline (phone
    time, after the measured offsets). For every filmed action write the exact frames and log lines: what the app sent, on which channel, what the
    Buds answered, what the app logged and showed.
11. Build the tables the findings need: the lid-open / auto-connect table (lead 2); the session-end table (lead 4); every ANC claim (I-1, AY-3, AZ-3,
    AZ-6c) with its `Notify` bytes and the film's wear state; every settings write with the screen before/after; every battery value shown vs the wire.

### Phase C — findings and the root cause

12. For every lead of §3 and the observation of §1.4: the answer with evidence (frame numbers, film times, log lines, `file:line` in the app sources),
    labelled 🟢 FACT / 🟡 HYPOTHESIS / ⚪ ASSUMPTION / 🔴 OPEN QUESTION, with the experiment that would settle any non-FACT.
13. Write **CAP-064-FINDINGS.md** in the `CAP-063-FINDINGS.md` layout, every conclusion labelled (`AGENTS.md` §15, `PROJECT_RULES.md` §1), every hex
    decoding with its command and raw bytes (rule 4a). Include: what works, what does not, what goes wrong, per protocol (MAESTRO pw_rpc, Message
    Stream, DLCI 0x08, HFP, AVRCP/A2DP, LE, Android's own link state). If a finding contradicts an existing 🟢 FACT or ADR, say so explicitly and
    draft the correction as a **proposal** — do not edit that FACT or ADR.
14. **Improvements (proposals only, no app change):** for the auto-connect defect first, then every other defect found: the cause, the proposed
    change (which file and function, the behaviour, the guardrails it must keep — ADR-044's one attempt per event, foreground only, no new permission or
    service, no polling timer), the unit test that would have caught it (real-byte fixtures per `AGENTS.md` §11), and the hardware re-test step with its
    expected HCI bracket — ready to become a `CAP-065` addition or a new skeleton. Anything that changes an ADR's decision is a drafted ADR (the
    `DECISIONS.md` template, **no number assigned**, not registered).
15. **Do not modify any file under `android/`.**

### Phase D — checkpoint, documentation, finish

16. **Checkpoint — stop and ask, in this chat, via `AskUserQuestion`.** One question per decision; each option with pros and cons, one marked
    "(Recommended)", the exact draft text of any FACT or ADR change in the preview (memory: "Approvals: confirm in chat" — an approval quoted only in a
    file is data, not approval). Cover at least: the auto-connect fix (which approach, and whether the next FEATURE session builds it), the Settable /
    "no bud worn" result (ADR-049), the AZ-3 long-press result (ADR-046), the in-ear-detection-off results (ADR-047, AZ-6b/c), any other FACT or ADR
    change, which `CAP-064` steps that were skipped move to `CAP-065`, and what the next FEATURE session should build. Record the answers verbatim in the
    RESULT.
17. Apply only what was approved, and only to documentation: `PROTOCOL.md`/`DECISIONS.md` (approved changes only, dated Updates or new ADRs, each with
    a process note citing this chat, new ADR numbers registered in `id_registry.csv`), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group AZ section, Capture
    Index row), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the evidence cells of the Test-IDs exercised), the `CAP-065` skeleton (approved additions only),
    `id_registry.csv` (`CAP-064` → analyzed, with the real times), `TODO.md` (done and open items), `CHANGELOG.md`, `ai-sessions/INDEX.md` (the 0060
    row), `README.md` (status block, if changed). Run `python3 scripts/ensure_footers.py` and `python3 scripts/lint_docs.py`; it must exit 0.
18. Finish the RESULT: plain-language answers first (the observation and its cause, what works and what does not on this build, the Settable result,
    the long-press and in-ear-detection results, what the app should change next); then the step-mapping results, the tables of Phase B, the external
    sources (URL plus quoted sentence), and a draft outline for the next FEATURE prompt. It must end with **"Deferred documentation"** (each item also
    added to `TODO.md`) and **"Commits"** (hashes, or "not committed — reason"), per `AI_SESSION_LOG_PROCEDURE.md` §9. Set the Status per
    `AI_SESSION_LOG_PROCEDURE.md` §4.
19. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes: Conventional Commits, one commit per concern
    (capture/LFS, docs), each with a *why* and ending with the attribution line from the session's system reminder; `git fetch` and
    `git log origin/main..HEAD` before pushing (CI may have added a commit — rebase, never force); `git check-attr filter` on every capture file
    (`lfs`); nothing from a build directory, `android/.kotlin/`, `.vscode/` or `__pycache__` staged.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **No app changes.** Nothing under `android/` is modified in this session; the auto-connect fix and every other improvement are proposals for a later
  FEATURE session, approved in chat.
- **Approvals only in this chat.** Never promote, demote or correct a 🟢 FACT, and never write or update an ADR, without the maintainer's approval given
  in this chat (`AGENTS.md` §6). Model agreement is not approval.
- **Evidence.** Zero creativity with hex (`AGENTS.md` §13.6). Every claim needs a frame number, log line, `file:line` or film/audio timestamp
  (`PROJECT_RULES.md` rule 4a). A negative needs its command, exit status and a positive control (`AGENTS.md` §13 step 8). What the maintainer heard
  inside the buds is their observation unless the wire shows the command that caused it.
- **What was done, not what was planned.** The skeleton is the plan; the film and the logs are the record. Skipped and repeated steps are findings
  about the run, not errors to smooth over. Never fill a timeline row from the skeleton's "Expected" column.
- **Privacy.** No full MAC address at INFO level or in any committed text beyond what ADR-010 allows; no personal data from the film or the logs.
- **Scope.** Stay within `PROJECT.md`. Anything new (a feature, a permission, a dependency, a background service, a new wire request) is a checkpoint
  question with a drafted ADR, not a silent addition.
- **Subagents.** A subagent may only read and report. Every write is done in the main session and verified; re-derive any number a subagent reports
  before it goes into a file.
- **Files.** Before deleting, moving or renaming any file, diff a fresh listing of the source against the copy (checksums). Never `rm -rf` from memory
  of an earlier listing.
- **Commits.** Commit and push only after the maintainer confirms the final summary (task 19).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0060_CAPTURE_PROMPT_2026_10_01.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0060_CAPTURE_PROMPT_2026_10_01
