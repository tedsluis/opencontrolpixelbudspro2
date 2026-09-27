# 0053_CAPTURE_PROMPT_2026_09_27.md — Full analysis of CAP-063 (Group AY, the hardware re-test of the 0048 and 0052 builds) and a list of app improvements, without changing the app

**Number:** 0053
**Category:** CAPTURE
**Date:** 2026-09-27
**Title:** Fully analyse `CAP-063` (video, HCI snoop log, app debug export, app logcat, system log), recorded while following the Group AY skeleton in
`CAP-063-EVENT-NOTES.md`; record the real events in **CAP-063-EVENT-NOTES.md** and the analysis in **CAP-063-FINDINGS.md**; correlate the app's
behaviour with each protocol; verify the maintainer's observations with evidence; and produce a prioritised list of app improvements — **no change to
the app itself**

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this
repository. Run the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8).** Before any other action, read **in full**, in this order:
`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md`, `DECISIONS.md` (**every** ADR, ADR-001 … ADR-045, with every dated
Update), `TODO.md`. Then read `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`, `ai-sessions/0048_FEATURE_RESULT_2026_09_25.md` (§8–§10) and
`ai-sessions/0052_FEATURE_RESULT_2026_09_26.md` (in full — what the build under test contains, its §9 re-test plan and the "refuted if" criteria),
`ai-sessions/0051_FEATURE_RESULT_2026_09_26.md` §5–§15 and §19, `APP_TESTPLAN.md` (in full), `CAPTURE_BLUETOOTH_HCI_SNOOP.md`,
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `id_registry.csv`, the **skeleton** `captures/CAP-063-*/CAP-063-EVENT-NOTES.md` as committed (`git show
HEAD:<path>` — it is the procedure the maintainer followed), and the worked example of the previous run of this app:
`captures/CAP-062-2026-09-25_06-38-36_06-57-10-Group_AX/CAP-062-EVENT-NOTES.md` and `…/CAP-062-FINDINGS.md`. Read every Kotlin source under
`android/` that a finding touches **in full** before you write about it. Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
**ai-sessions/0053_CAPTURE_RESULT_2026_09_27.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and update that
block at the end of every phase **and** after every substantial step within a phase (e.g. every 5 minutes of film reviewed, every DLCI decoded):
what is done, what is next, which files are touched but unverified, and where intermediate results live (the scratchpad directory — scripts,
contact sheets, decoded tables; re-create them if the scratchpad is gone). A resumed session reads this prompt, then the RESULT's Progress block,
re-reads the files the unfinished step touched, and continues from there. It never redoes a finished, recorded step, and never assumes an
unrecorded step was done. The prompt keeps its number and date; the RESULT keeps growing in the same file.

**Nothing is taken on trust, including this prompt and the maintainer's observations.** Every observation (§1.4) and every earlier session's claim
is verified against the capture, the code and official documentation. Earlier sessions went wrong exactly where a summary was not re-derived from
the log (`ai-sessions/0043`'s Case "FACT"; `ai-sessions/0046`'s Safe Mode bug found only by decoding the real announcement bytes).

---

## 1. What the maintainer asked for (chat, 2026-09-27, translated)

1. **Analyse `CAP-063` fully.**
   - Analyse **CAP-063-recording.mp4** first and record every action and event with its time in **CAP-063-EVENT-NOTES.md**.
   - Analyse **CAP-063-btsnoop_hci.log** with `tshark`.
   - Correlate the events of `CAP-063-EVENT-NOTES.md` with the HCI log and all other log files (`CAP-062-EVENT-NOTES.md` is the example).
   - Establish what does not work and what goes wrong; which functionality works well and which does not.
   - Record every finding in **CAP-063-FINDINGS.md** (`CAP-062-FINDINGS.md` is the example).
   - Correlate the app's behaviour with the different protocols (DLCI 0x02 pw_rpc, DLCI 0x04 Message Stream, DLCI 0x08, HFP, AVRCP/A2DP where
     relevant, LE/GATT, Android's own Bluetooth state): what goes well, what goes wrong, what must be improved.
2. **Then analyse and produce a list of improvements** that can be built into the app. **Do not change the OpenControl app itself.**
3. **Not every step was done as written.** The maintainer followed the skeleton's steps, but a number of times skipped one or more steps by mistake,
   or repeated one or more steps because something went wrong. So: **establish from the film and the logs what was actually done**, in the order it
   was done — never fill the timeline from the skeleton's plan. Map each real action to the skeleton's step IDs (S0 … Z2, AY-0 … AY-14, B1 … B3,
   C4/C5/C8/C9, D1 … D13, H2 … H5, F5 … F7, J4, K1 … K5, L2/L3, A5, B4, (E)) and mark each step **done**, **done differently**, **repeated** (with
   every repetition as its own timeline row), **skipped** or **not identifiable**.
4. **The maintainer's observations — verify each with evidence (confirm, refute or qualify), and for each wish say exactly what would be needed:**
   1. Find My Buds works: the buds can be heard ringing.
   2. The EQ works (treble, bass, mid, etc.), and so do the presets; the difference is clearly audible.
   3. Volume balance and mono on/off work.
   4. Conversation detection works well: the music stops when the maintainer talks and resumes after they stop.
   5. As soon as a bud is taken out of the case, the app connects by itself.
   6. The Case and bud battery percentages are always shown correctly.
   7. Buds in or out of the case are shown correctly. **Not shown: whether the case lid is closed, and whether the buds are in the ears.** (wish)
   8. Connecting and disconnecting work well.
   9. ANC works well, both from the app and via a long press on the buds.
   10. Controls: "Use touch controls" on/off works; with it off, the touch controls on the buds do not work.
   11. Controls: switching between Noise control and Digital assistant works — with Digital assistant a different sound is heard when touching a
       bud than with Noise control. **Missing for Noise control: choosing which ANC modes the press-and-hold cycles through — Noise Cancelling, Off,
       Adaptive and/or Transparent — with checkboxes.** (wish) Digital assistant does nothing further: **GrapheneOS has no digital assistant**
       (the maintainer confirmed in chat 2026-09-27), so the "Digital assistant" choice currently looks superfluous — say what the app should do
       with it (keep, hide, explain) as an improvement question.
   12. Controls: **in-ear detection cannot be changed** (it is read-only in the app), and the battery card does **not show when a bud is in an ear**.
       (wish)
5. **Strict execution rules (the maintainer's own words, translated):**
   - **No assumptions.** Verify everything.
   - **Validate externally.** Use search tools to validate claims against official documentation (Android Developer docs, Bluetooth Core
     Specification, Fast Pair specification) where needed, and cite the URL and the exact sentence.
   - **No sampling.** Review the relevant files 100 %, completely and exhaustively, checking every detail: every second of the video, every packet
     of the HCI log that belongs to the Buds, every line of the debug export and the app logcat, and every relevant part of the system log.
   - **Best practices.** Industry-standard technical review methods: **evidence traceability** (every claim links back to a specific frame number,
     log line or video timestamp) and **structural integrity** checks (files, registry, links, footers).

---

## 2. The evidence

`captures/CAP-063-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AY/` — the skeleton folder (committed in `cf1f0c9`); the capture files in it are **untracked**.
`id_registry.csv` has a `planned` row for `CAP-063` — set it to `analyzed` with the real times when done.

| File | Role / what to check first |
|---|---|
| **CAP-063-recording.mp4** | the film (≈ 1.27 GB — long; plan the extraction so every second is covered and every transition is narrowed to ≤ 1 s). ⚠️ **Its audio track is empty** (`ffprobe`: stream 0 "Audio: none, 0 channels", no decodable codec — found 2026-09-27); what the maintainer heard is not on the film and stays their observation |
| **CAP-063-btsnoop_hci.log** | the HCI snoop log of the session (`capinfos`: "Packet size limit: (not set)" = raw path; `frame.cap_len == frame.len` for every packet; first/last time) |
| **CAP-063-btsnoop_hci.log.last** | the previous snoop log file (Android rotates the log). `capinfos` (2026-09-27): 961 packets, 15:56:10.981–15:57:16.332 — it ends 18 s **before** the film's first frame (15:57:34): the pre-session state before the Bluetooth off/on rotated the log. Confirm, then use it only as context. `.gitattributes` now covers `captures/**/*.log.last` (LFS) |
| **CAP-063-debug-export.log** | the app's own debug export (phone local time). ⚠️ It is **exactly 65,536 bytes and its last line is cut mid-frame** (746 lines, 15:57:55.706–16:18:29.012): the share-sheet `EXTRA_TEXT` hand-off of the build under test truncated it, so the last ≈ 6.5 minutes of the session (16:18:29–16:25) are **not** in it — use the HCI log and logcat for that part. **Already fixed in the app** (the 2026-09-27 app commit after `cf1f0c9`: export saved as a file via Android's "save as" dialog, ring buffer 20,000 lines) — do not propose it again; do verify that the 1,000-line buffer did **not** wrap (746 lines) |
| **CAP-063-OpenControl-for-Pixel-Buds-log-a5f9783708f6.txt** | Android logcat of the app's package (UTC — measure the offset; `CAP-062` found −2 h 00 min 00.0 s) |
| **CAP-063-System-log-6cf0a8a3bd50.txt** | Android system log (≈ 14 MB) |
| **CAP-063-EVENT-NOTES.md** | **Already has a first video-based timeline** (written 2026-09-27 in the chat that authored this prompt, at 1 fps with zoomed frames for every quoted screen text, in **film overlay** time; the planned procedure moved to Appendix A), a step-mapping table and a list of ⚠ open points. It has **no** HCI/log evidence yet and its clock offset is provisional (≈ +1 s). Treat it as a draft to **verify**, not as evidence: re-check every row against the film, narrow transitions, add the evidence columns, convert to phone time, and correct anything wrong (record corrections in the RESULT) |

**The build under test** should be `94e4fb1` or later (the `ai-sessions/0052` settings UI — tabs "Sound" and "Controls"). The maintainer may not
have written the hash (skeleton P1). **Confirm it from the logs**, not from dates: log wording that exists only from `0052` on — e.g. "Settings read
(channel …)", "Setting N written (channel …)", "Runtime info re-requested on Refresh", "Message Stream still claimed from an earlier action:
released for a fresh claim", "Battery refresh: NoNewBatteryReading" — and wire behaviour (six `ReadSetting`s at Connect, `WriteSetting 4:{17|19|22|4|7:…}`).
Also look for anything that contradicts it (the `0048` loss wording, the old EQ tab title).

**Known pitfalls (check, do not assume):** with the `H4 with linux header` encapsulation `bluetooth.addr` is empty — pre-filter by the Buds'
connection handle(s) taken from the HCI Connection Complete / LE Connection Complete events (`AGENTS.md` §13.1; `CAP-062` used
`bthci_acl.chandle==0x000b`); another LE device may share the log (`CAP-062`: handle `0x0041`); HFP frames are consumed by the HFP dissector
(`data.data` empty — use the dissector's own fields); RFCOMM DLCI numbers are session-local — identify each channel by its content (`CAP-062`:
MAESTRO on DLCI 0x03 and Message Stream on 0x05 on a Buds-initiated connection); a single RFCOMM payload can hold several pw_hdlc frames and several
Message Stream messages.

**Privacy (standing rule, ADR-037).** Check **every** frame of the recording for a burned-in address overlay, the notification shade, messages,
contact names or any other personal data before anything is staged. The maintainer kept `CAP-062` unblurred; that choice does **not** carry over —
report what you find in `CAP-063` and **ask**. If a blur is wanted, propose the exact `ffmpeg` command, the frame ranges and a before/after frame.
Never log or commit the Buds' full MAC outside what ADR-010 allows.

---

## 3. Leads and context — verify each before relying on it

1. **The re-test items this run was meant to settle** — give each a verdict (confirmed / refuted / not exercised / not identifiable) with frames
   and log lines: `0048` §9 AY-0 … AY-12 (automatic re-open ADR-044, ANC only while worn, loss wording, per-bud "charging in the case", last-seen
   Case, ring notice); `0051` §19 AY-13 (two Refreshes within 1 s → two `SABM`s on DLCI 0x04, each followed by `03 03`) and AY-14 (docked, idle
   2 min, Refresh → is the re-sent `SubscribeRuntimeInfo` answered? — ADR-043 Update, 🔴); `0052` §9 (every settings read at Connect, every
   `WriteSetting` byte-compared with `android/data/src/test/…/codec/SettingsFixtures.kt` for the same channel, the empty `RESPONSE`, the read-back
   after a reconnect, Safe Mode not triggered). Apply the skeleton's §7 "Refuted if" list literally.
2. **Settable byte = worn? (ADR-024 Update 2026-09-25, 🟡).** The skeleton's AY-3a–c (one bud in an ear, the other on the table) is the test. If it
   was done, decode every `08 13 00 04 01 e8 <settable> <mode>` around those steps and compare with the film. This also bears on wish 12 ("show
   when a bud is in an ear"): is there **any** per-bud wear signal on any channel? (`ai-sessions/0051` §5 found none in `CAP-062` — re-check with this
   capture, including the runtime-info stream's fields 3 and 7.3, which were constant 0 in 438/438 packets, and any `SubscribeToSettingsChanges`-like
   push.)
3. **Wish 7 (case lid closed):** ADR-016 item 6 is a 🟢 FACT that opening/closing the lid with both buds **outside** the case produces no wire signal;
   with buds **inside**, the Buds drop the ACL (ADR-016 item 5; `0051` §10). Check what this capture shows around every lid change on the film —
   any frame on any channel/handle, LE advertising changes, Android broadcasts in the system log — and say whether any signal exists that the app
   could use without new permissions or scanning (ADR-005/006).
4. **Wish 11 (ANC-mode checkboxes for press-and-hold):** that is `qhr` field 12 (`qht`), **read- and write-gated** (ADR-036 does not list it; ADR-045
   excludes it) because its bit order is disputed (`PROTOCOL.md` §4.5.3 2026-09-26 Update: code says 1 NC, 2 Off, 3 Transparency, 4 Adaptive; the 🟡
   on-screen reading swaps 3 and 4) and whether it is one shared list or one per bud is 🟡. The settling capture is Group AR (`CAP-056`, Pixel 7a,
   official app, with the additions in `CAP-063-EVENT-NOTES.md` §8). Check whether `CAP-063` contains any field-12 traffic (it must not come from the
   app; the Buds might push it); then state exactly what is needed (capture, promotion, ADR, app work) — do not propose sending field 12 now.
5. **Wish 12 (in-ear detection writable):** field 2's identity is 🟢 only at category level ("CATEGORY_OHD", ADR-019 Update 2026-09-08); the UI label
   "In-ear detection" is 🟡; `CAP-024` frames 1850/1912 hold the official app's writes in both directions (video-confirmed). State what a write ADR
   would need (a label promotion like D-1(a) of `0051`, the byte-identical fixtures) and what the Buds do when it is off (the `0048` automatic
   re-open and the "charging in the case" lines depend on wear/dock pushes — could turning it off change them? verify against captures, do not guess).
6. **Digital assistant on GrapheneOS (observation 11):** establish from the logs what happens on a press-and-hold with "Digital assistant" (an AVRCP
   / HFP / `ACTION_VOICE_COMMAND`-style event, a system-log line, or nothing) and cite the relevant Android documentation for how a Bluetooth headset
   triggers the assistant — without proposing any GMS dependency (`AGENTS.md` §1).
7. **Conversation detection (observation 4):** is the pause/resume visible on the wire (AVRCP pause/play from the Buds, an A2DP suspend, a DLCI 0x04
   `Notify` ANC change) or purely inside the Buds? (`CAP-029-FINDINGS.md` §2 says the media pause had no wire effect in the official-app era —
   re-check here.)
8. **Battery (observation 6)** — compare every value the app showed (film) with the wire value and its time (DLCI 0x04 `03 03`, the runtime-info
   stream's 6.1/6.2/6.3) and with Android's own Bluetooth screen if filmed. A "correct" percentage without its own receive time is not enough
   (ARCHITECTURE.md §3.1).
9. **Session ends (observations 5 and 8):** build the table of every session end and re-open as in `CAP-062` §3 / `0048` §4: who closed what
   (Buds-side `DISC` with the ACL up, ACL `Disconnection Complete` with reason, the user's Disconnect, Android's panel, Bluetooth off, range), the
   cause the app displayed, and whether ADR-044's rules held (one attempt per event, only while visible, none after a Disconnect tap, 10 s guard).
10. **Anything new:** NAKs, pw_rpc error statuses, `MalformedFrame`/`UnidentifiedFrame` counts in the debug export, crashes/ANRs/exceptions in the
    logs, Play services contending for DLCI 0x04, any app frame on DLCI 0x08 (must be none).

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename or `git add`)

1. Create the RESULT file (§0). Record `git log -1` and `git status --short`. Confirm `CAP-063`'s `planned` row in `id_registry.csv` and whether
   Group AY exists in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (it does not yet — `0052` did not add it). Identify every file with `capinfos`, `ffprobe` and
   `sha256sum` (packets, duration, first/last timestamps, frame rate, audio track, truncation check, the 65,536-byte export, the `.last` log's range).
2. Privacy check of the whole recording (§2). Summarise the result.
3. Plan the migration into the project convention (ADR-037, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9): rename the folder to
   `captures/CAP-063-YYYY-MM-DD_HH-mm-ss_HH-mm-ss-Group_AY/` from the film's own first/last overlay times; the `id_registry.csv` row; the Capture
   Index row; a short Group AY section (what the skeleton planned, what was run); Git LFS for `.txt`/`.log`/`.mp4`/`.last` (check `.gitattributes`
   and `git check-attr filter` — `CAP-063-btsnoop_hci.log.last` does **not** match the existing `captures/**/*.log` pattern); no executable bit on capture files (several are `-rwxr-----`);
   what to do with `CAP-063-btsnoop_hci.log.last`. **Ask the maintainer (`AskUserQuestion`)** about the privacy outcome and the migration plan
   together, before moving anything.
4. Execute only what was approved. Before moving or replacing anything, compare checksums of a fresh listing of the source against the
   destination; never `rm -rf` based on an earlier listing (memory: "File migration care").

### Phase A — the video, in full

5. `ffprobe` the recording. Scan the whole duration (scene-change detection **and** a fixed interval of ≤ 2 s), then narrow every transition to
   ≤ 1 s (single frames where a claim depends on it — the moment a bud is seated, taken out, put in an ear; a lid opening/closing; a switch
   flipping). Read every visible on-screen text: every app screen and message (Connection, ANC, Sound, Controls, Find, Debug), notifications,
   Quick Settings, Android's Bluetooth screens, the clock, the buds and the case (lid, LED, which bud is where, when). Establish which slot holds
   which bud from the wire (the charging bit), as `CAP-062` did.
6. The **audio track** is empty (§2) — confirm it (e.g. `ffmpeg -i … -map 0:a -f null -`), record it in the notes, and treat every sound claim as
   the maintainer's observation, backed where possible by the wire (the command sent and acknowledged).
7. Measure the film ↔ phone clock offset at the start **and** the end (the skeleton's S0/Z2 minute changes, or any visible clock with seconds
   against a logged event).
8. Complete **CAP-063-EVENT-NOTES.md** (the draft timeline of §2) in the `CAP-062` layout: header status, Log Metadata, capture-integrity pre-flight, video review
   method (incl. privacy), an Event Timeline (phone time, action, actor, skeleton step ID and `APP_TESTPLAN.md` test ID and registry Test-ID where
   applicable, evidence), the analysis checklist and next steps. **Traceability (`AGENTS.md` §13.7):** every skeleton step and every `APP_TESTPLAN.md`
   test the skeleton names appears in the timeline or is explicitly marked "skipped" / "not identifiable"; every repetition is its own row.

### Phase B — the HCI log, in full

9. Follow `AGENTS.md` §13. Pre-filter by the Buds' handle(s), take the full DLCI inventory, then decode **every** Buds packet: DLCI 0x02 (MAESTRO,
   whatever its session-local number) with `scripts/pwrpc_decode.py`; DLCI 0x04 every `[Group][Code][Length]` message (ACK/NAK with reason, Model ID,
   session nonce, battery, `Notify ANC state`, `Set`, Ring); DLCI 0x08/0x0a (who opens them — the app must not); every `SABM`/`UA`/`DISC`/`DM` with its
   direction; HFP (incl. `AT+BIEV`); AVRCP (play/pause around conversation detection and touch controls); the ACL and LE connection and
   disconnection events with their reason codes; any LE/GATT traffic of the Buds.
10. For every app action in the timeline write down the exact frames: what the phone sent, on which channel, and what the Buds answered; for every
    `WriteSetting`/`ReadSetting` the field, value and status; byte-compare the app's requests with the fixtures of `SettingsFixtures.kt` /
    `Cap062Fixtures.kt` for the same channel. For actions where nothing was sent, find the log line that explains why.
11. Build per-open tables for DLCI 0x04 (opener, request, battery burst yes/no, first answer, closer, time held — this settles AY-13) and the table
    of every session end and re-open (§3 lead 9).

### Phase C — correlate every log with the events and with each other

12. Correlate the debug export (every line), the app logcat (every line of this package) and the relevant system-log parts (Bluetooth, RFCOMM,
    CDM, profile connection states, foreground service, tile, Play services' Fast Pair/Nearby, GrapheneOS Bluetooth timeout, media session /
    assistant intents, crashes/ANRs) with the timeline and the HCI frames, using the measured offsets.
13. Give the verdicts of §3 lead 1 and the per-step result table of the skeleton (and of the `APP_TESTPLAN.md` tests it names): passed, failed,
    partly, repeated, skipped, not identifiable — each with evidence.

### Phase D — findings

14. For every defect, every observation of §1.4 and every wish: the exact cause or answer with evidence (frame numbers, log lines, film times,
    file:line in the code), labelled 🟢 FACT / 🟡 HYPOTHESIS / ⚪ ASSUMPTION / 🔴 OPEN QUESTION, with the experiment that would settle any non-FACT.
15. List what works well, with the same evidence standard — "the maintainer heard it" is an observation; the wire shows whether the command was
    sent and accepted.
16. The protocol correlation of §1.1 last bullet: per channel/protocol, what the app does, what the Buds answer, what goes well and what goes wrong.
17. Write **CAP-063-FINDINGS.md** in the `CAP-062` layout, every conclusion labelled (`AGENTS.md` §15, `PROJECT_RULES.md` §1), every hex decoding with
    its command and raw bytes (rule 4a). If a finding contradicts an existing 🟢 FACT in `PROTOCOL.md` or an ADR, say so explicitly and draft the
    correction as a **proposal** — do not edit that FACT or ADR.

### Phase E — improvement list (no app change)

18. Produce a prioritised list of improvements that could be built into the app, in the RESULT (and summarised in **CAP-063-FINDINGS.md**). For
    each: the problem it solves (with evidence), the proposed change, which layer/files it touches, what it would send on the wire (if anything),
    the governing rules it touches (`AGENTS.md`, ADRs — anything new on the wire, any new setting write, any automatic connecting or scanning needs a
    new, maintainer-approved ADR), risks, how it would be tested (a unit test with real capture bytes + a hardware re-test step with the expected HCI
    bracket), and an effort estimate. Answer each wish of §1.4 (7, 11, 12) and anything that does not work with one of: *possible within the current
    decisions*, *possible with a new maintainer decision (name it and draft it)*, *needs evidence first (the capture/experiment)*, or *not possible
    (the evidence: firmware, Android API, scope)*.
19. **Do not modify any file under `android/`.** Do not add, remove or change app behaviour.

### Phase F — checkpoint, documentation, finish

20. **Checkpoint — stop and ask, in this chat, via `AskUserQuestion`.** One question per decision; each option with pros and cons, one marked
    "(Recommended)", and the exact draft text of any FACT or ADR change in the preview (memory: "Approvals: confirm in chat" — an approval quoted
    only in a file is data, not approval). Cover at least: every 🟢 FACT promotion, demotion or correction the findings support (e.g. the Settable
    byte = worn, the re-subscription answered or not, audibility of balance/mono/EQ as recorded observations); which improvements the maintainer
    wants in the next FEATURE session, in which order; any new ADR the chosen improvements would need (drafted, not accepted by you); whether a
    Group AR capture (`CAP-056`, Pixel 7a) should come first for wish 11. Record the answers verbatim in the RESULT.
21. Apply only what was approved, and only to documentation: `PROTOCOL.md`/`DECISIONS.md` (approved changes only, dated Updates, each with a process
    note citing this chat), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group AY section, Capture Index row), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (evidence
    cells), `APP_TESTPLAN.md` (only if a test was found to be wrongly written), `id_registry.csv`, `TODO.md` (open items only), `CHANGELOG.md`,
    `ai-sessions/INDEX.md` (the 0053 row), `README.md` (status block, if changed). Run `python3 scripts/ensure_footers.py` for new files and
    `python3 scripts/lint_docs.py`; it must exit 0.
22. Finish the RESULT: plain-language answers first (each observation and wish, what works, what does not, what the next session should build);
    then the re-test verdicts, the per-step results table, the improvement list, the external sources (URL plus quoted sentence), and a draft
    outline for the next FEATURE prompt. Set the Status per `AI_SESSION_LOG_PROCEDURE.md` §4.
23. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes: Conventional Commits, one commit per concern
    (capture/LFS, docs), each with a *why* and ending with the attribution line from the session's system reminder; `git fetch` and
    `git log origin/main..HEAD` before pushing (CI may have added a commit — rebase, never force); `git check-attr filter` on every capture file;
    nothing from a build directory, `android/.kotlin/`, `.vscode/` or `__pycache__` staged.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **No app changes.** Nothing under `android/` is modified in this session; improvements are proposals only.
- **Approvals only in this chat.** Never promote, demote or correct a 🟢 FACT, and never write or update an ADR, without the maintainer's approval
  given in this chat (`AGENTS.md` §6).
- **Evidence.** Zero creativity with hex (`AGENTS.md` §13.6). Every claim needs a frame number, log line or video timestamp. "Works" without wire
  evidence is a 🟡 HYPOTHESIS; a sound the maintainer reports is their observation, recorded as such.
- **What was done, not what was planned.** The skeleton is the plan; the film and the logs are the record. Skipped and repeated steps are findings
  about the run, not errors to smooth over.
- **Scope.** Stay within `PROJECT.md`. Anything new (a feature, a permission, a dependency, automatic connecting, a background service, scanning, a
  new wire request, a write to field 2 or 12) is a checkpoint question with a drafted ADR, not a silent addition. Case ringing / "ring both" stay out
  of scope (ADR-027).
- **Subagents.** A subagent may only read and report. Every write is done in the main session and verified; re-derive any number a subagent
  reports before it goes into a file.
- **Files.** Before deleting, moving or renaming any file, diff a fresh listing of the source against the copy (checksums). Never `rm -rf` from
  memory of an earlier listing.
- **Commits.** Commit and push only after the maintainer confirms the final summary (task 23).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0053_CAPTURE_PROMPT_2026_09_27.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0053_CAPTURE_PROMPT_2026_09_27
