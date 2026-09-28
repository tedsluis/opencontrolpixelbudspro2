# 0055_CAPTURE_PROMPT_2026_09_28.md — Full analysis of CAP-056 (Group AR: the ANC-mode checklist of press-and-hold, and in-ear detection off, with the official app on the Pixel 7a)

**Number:** 0055
**Category:** CAPTURE
**Date:** 2026-09-28
**Title:** Fully analyse `CAP-056` (video with audio, HCI snoop log), recorded while following the Group AR skeleton in `CAP-056-EVENT-NOTES.md`
(the `HOLD-005` ANC-mode checklist per bud, the `qht` bit order of `ai-sessions/0051` F-6, and the W-12b in-ear-detection-off steps W1–W5 added by
`ai-sessions/0054`); record the real events in **CAP-056-EVENT-NOTES.md** and the analysis in **CAP-056-FINDINGS.md**; verify the maintainer's
observation on in-ear detection; prepare — as proposals only — what field 12 and field 2 would need before the app may read or write them

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this
repository. Run the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8).** Before any other action, take the time to read **in full**, in
this order, to understand the ground rules before touching anything: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md`
(in particular §4.1, §4.5 with §4.5.3 "Touch & Hold" and §4.5.5 "In-ear detection" and all their dated Updates, §6), `DECISIONS.md` (**every** ADR,
ADR-001 … ADR-045, with every dated Update — in particular ADR-013, ADR-019 and its Updates, ADR-024, ADR-036, ADR-044, ADR-045), `TODO.md`. Then read
`AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group AR, Group AJ, §3, §5, §8, §9),
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (`HOLD-001` … `HOLD-005`, `INEAR-*`), `REVERSE_ENGINEERING.md` (the `qhr`, `qju`, `qht`/`hgj` and
`MaestroDeviceSettingsProviderService` entries), `id_registry.csv`, `ai-sessions/0051_FEATURE_RESULT_2026_09_26.md` (§15, §19, F-6),
`ai-sessions/0053_CAPTURE_RESULT_2026_09_27.md` (§6 — W-11, W-12a, W-12b and the draft "in-ear detection write" ADR),
`ai-sessions/0054_FEATURE_RESULT_2026_09_28.md` (§6, §12), the capture analyses this one builds on — `CAP-021-FINDINGS.md` (`HOLD-001` … `HOLD-005`),
`CAP-024-FINDINGS.md` (the in-ear-detection writes, frames 1850/1912), `CAP-045-FINDINGS.md` (why Group AJ failed), `CAP-063-FINDINGS.md` §3/§7/§8 —
the **skeleton** `CAP-056-EVENT-NOTES.md` in the folder `captures/CAP-056-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AR/` as committed [folder renamed by this session to `captures/CAP-056-2026-09-28_17-30-53_17-35-58-Group_AR/`] (`git show HEAD:<path>` — it is the
procedure the maintainer followed), and the worked example of the layout: `captures/CAP-050-2026-09-14_21-01-01_21-13-30-Group_AG/CAP-050-EVENT-NOTES.md`
and `…/CAP-050-FINDINGS.md`. Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
**ai-sessions/0055_CAPTURE_RESULT_2026_09_28.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and update that
block at the end of every phase **and** after every substantial step within a phase (every minute of film reviewed, every DLCI decoded, every
checkpoint answer): what is done, what is next, which files are touched but unverified, and where intermediate results live (the scratchpad
directory — scripts, contact sheets, decoded tables, extracted audio; re-create them if the scratchpad is gone). A resumed session reads this prompt,
then the RESULT's Progress block, re-reads the files the unfinished step touched, and continues from there. It never redoes a finished, recorded step,
and never assumes an unrecorded step was done. The prompt keeps its number and date; the RESULT keeps growing in the same file.

**Nothing is taken on trust, including this prompt, the skeleton and the maintainer's observation.** Every claim is re-derived from the film, the HCI
log, the APK sources where relevant and official documentation.

---

## 1. What the maintainer asked for (chat, 2026-09-28, translated)

1. **Analyse `CAP-056` (Group AR) fully and record the findings.**
   - Analyse **CAP-056-recording.mp4** first and record every action and event with its time in **CAP-056-EVENT-NOTES.md**.
   - Analyse **CAP-056-btsnoop_hci.log** with `tshark`.
   - Correlate the events of `CAP-056-EVENT-NOTES.md` with the HCI log (and any other log file present); `CAP-050-EVENT-NOTES.md` is the example.
   - Run an extensive analysis and record the findings in **CAP-056-FINDINGS.md**; `CAP-050-FINDINGS.md` is the example.
2. **Not every step was done as written in the plan** — among other things because the maintainer's ears had to be filmed. So: **establish from the
   film and the log what was actually done**, in the order it was done — never fill the timeline from the skeleton. Map each real action to the
   skeleton's step IDs (step 1–3 of the procedure: the Left/Right checklist toggles; (A) the `0051` F-6 bit-order steps; (B) W1–W5) and mark each
   step **done**, **done differently**, **repeated** (each repetition its own timeline row), **skipped** or **not identifiable**.
3. **The maintainer's observation — verify it with evidence (confirm, refute or qualify):** switching in-ear detection on or off worked. With in-ear
   detection **on**, the music stopped when a bud was taken out of an ear and resumed when it was put back. (Implied, check it: with it **off**, the
   music did not stop.)
4. **Strict execution rules (the maintainer's own words, translated):**
   - **No assumptions.** Verify everything.
   - **Validate externally.** Use search tools to validate claims against official documentation (Android Developer docs, Bluetooth Core
     Specification, AVRCP, Fast Pair specification) where needed; cite the URL and the exact sentence.
   - **No sampling.** Review the relevant files 100 %, completely and exhaustively: every second of the video (and of its audio track), every packet
     of the HCI log that belongs to the Buds.
   - **Best practices.** Industry-standard technical review methods: **evidence traceability** (every claim links back to a specific frame number,
     log line or video timestamp) and **structural integrity** checks (files, registry, links, footers).

---

## 2. The evidence

`captures/CAP-056-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AR/` — the skeleton folder; `CAP-056-EVENT-NOTES.md` is committed (the skeleton, incl. its
"Additions" (A) and (B) of `ai-sessions/0054`), the two capture files are **untracked**. `id_registry.csv` has a `planned` row for `CAP-056`.
Measured by the chat that wrote this prompt (2026-09-28) — re-check each value:

| File | Role / what to check first |
|---|---|
| **CAP-056-recording.mp4** | ≈ 306.0 s, H.264 1280×720 and **an AAC stereo audio track** (unlike `CAP-063`, whose track was empty). The camera microphone hears the room, not what plays inside the buds — say what the audio does and does not establish (speech, the Buds' tones, the phone's speaker), never more |
| **CAP-056-btsnoop_hci.log** | 5,376 packets, 2026-09-28 17:30:42.812–17:39:02.441, "Packet size limit: (not set)" (raw path), encapsulation `Bluetooth H4 with linux header`. The log covers ≈ 8.3 min, the film ≈ 5.1 min: establish which part of the log the film covers |
| **CAP-056-EVENT-NOTES.md** | the skeleton (the plan). Rewrite it into the record (layout of `CAP-050-EVENT-NOTES.md`); keep the planned procedure as an appendix, unchanged, as `CAP-063-EVENT-NOTES.md` did |

**No other logs** (no app debug export, no logcat, no system log) are present: the app under test is the **official Pixel Buds app on the Pixel 7a**
(skeleton, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AR). Confirm the phone, the app and its version from the film (the app's screens, "About", the
device details) and the log; confirm the Pixel 9a's Bluetooth was off (no second host in the Buds' traffic; multipoint) — or record that it was not.
Git LFS covers both files (`git check-attr filter` → `lfs`, 2026-09-28).

**Known pitfalls (check, do not assume):** with the `H4 with linux header` encapsulation `bluetooth.addr` is empty — pre-filter by the Buds'
connection handle(s) taken from the HCI Connection Complete / LE Connection Complete events and map them to `04:00:6e:cf:6e:07` (`AGENTS.md` §13.1);
other devices may share the log; RFCOMM DLCI numbers are session-local — identify each channel by its content; a single RFCOMM payload can hold
several pw_hdlc frames; HFP frames are consumed by the HFP dissector; AVRCP pass-through (PLAY/PAUSE) comes from the Buds and the phone answers.
`scripts/pwrpc_decode.py` prints `sint32` values (field 17) as **raw** zigzag varints (`ai-sessions/0054` §8) — decode before interpreting.

**Privacy (standing rule, ADR-037).** Check **every** frame of the recording **and the audio track** (voices, names, other people) for personal data:
the notification shade, messages, contact names, a burned-in address overlay, faces other than what the maintainer chose to film (the ears). Report
what you find and **ask** (earlier choices for `CAP-062`/`CAP-063` do not carry over). If a blur or an audio cut is wanted, propose the exact `ffmpeg`
command, the ranges and a before/after sample. Never log or commit the Buds' full MAC outside what ADR-010 allows.

---

## 3. Leads and context — verify each before relying on it

1. **`HOLD-005` — the ANC-mode checklist, per bud (the Group AR purpose).** `PROTOCOL.md` §4.5.3: the checklist is `qhr` field 12 (`qht`, four
   booleans; 🟢 field-number identity, ADR-019); 🟡 which item each boolean is; 🔴 whether Left and Right have one shared list or one each (the
   write carries no Left/Right field in `CAP-021`). Decode **every** `WriteSetting 4:{12:{…}}` of the official app and every mirrored
   `SubscribeToSettingsChanges` push, and correlate each with the film (which screen — "Customize left" / "Customize right" — and which checkbox the
   finger changed). **The anti-repeat safeguard applies:** the session counts only if the checklist screen itself is visible on film before the first
   toggle (`CAP-045`'s failure). State plainly whether the Left and Right lists are distinguishable on the wire, or only by the film.
2. **The `qht` bit order (`0051` F-6, `PROTOCOL.md` §4.5.3 2026-09-26 Update, 🔴).** The code (`qht.java:31`, `hgj.java:216/245/274/303`) maps
   1 = Noise cancellation, 2 = Off, 3 = Transparency, 4 = Adaptive; the 🟡 on-screen reading swaps 3 and 4. The decisive steps are (A): untick **only
   Adaptive**, then **only Transparency** — which boolean clears each time? Re-check the code references yourself (the JADX sources under
   `reverse-engineering/`, if present locally) before writing anything; say which reading the capture supports and with which frames.
3. **One list or two?** Step (A)2: after unticking on "Customize left", does "Customize right" show the change on film? Does a toggle on one screen
   produce a push that the other screen then shows? A 🟢 answer needs both the film and the wire.
4. **In-ear detection (field 2), W-12b (B) W1–W5.** `PROTOCOL.md` §4.5.5: field 2 = "CATEGORY_OHD", 🟢 at category level only (ADR-019 Update
   2026-09-08); the UI label "In-ear detection" is 🟡; `CAP-024` 1850/1912 are the official app's writes in both directions. For each toggle on film:
   the `WriteSetting 4:{2:0|1}` frame, the empty `RESPONSE` status OK, the mirrored push — and whether the film shows the **switch labelled "In-ear
   detection"** and the finger on it at that moment (the evidence a label promotion needs, as D-1(a) of `0051` used for field 22).
5. **The maintainer's observation (§1.3).** With in-ear detection on: find the AVRCP PAUSE/PLAY pass-through from the Buds (and the phone's
   PlaybackStatusChanged) at each bud removal and re-insertion on film; with it off: their absence. Also check what else changes with it off:
   (a) does the Buds' `DISC` of DLCI 0x02 on wear changes still happen (ADR-044's re-open in OpenControl depends on it; in `CAP-062`/`CAP-063` the Buds
   closed the MAESTRO channel on some wear changes); (b) the `Notify ANC state` Settable byte (`00`/`e8`) on wear changes with it on vs off (ADR-024
   Update, 🟡 "`0x00` = no bud worn") — note that the official app's Message Stream may be held by Play services; (c) any other push (the runtime-info
   stream fields 3/7.3, which were constant 0 in `CAP-063`). If the ears are visible on film, this capture may also bear on AY-3 ("one bud worn") —
   say so only with film evidence.
6. **Touch-and-hold behaviour with the checklist changed.** If the maintainer long-pressed a bud after changing the checklist, the `Notify ANC state`
   sequence shows which modes the cycle visits — a second, independent check of the bit order (lead 2). Only with film evidence of the long press.
7. **What the official app sends besides.** Every other official-app request in the window (`ReadSetting` sweeps, `SubscribeToSettingsChanges`,
   unnamed services) — listed, not interpreted beyond the evidence.
8. **Anything new:** NAKs, pw_rpc error statuses, ACL/LE disconnections and their reasons, Play services or the Google app opening DLCI 0x04/0x08/0x0a,
   a second host.

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename or `git add`)

1. Create the RESULT file (§0). Record `git log -1`, `git status --short`, `git fetch && git log --oneline HEAD..origin/main`. Confirm the `planned`
   row of `CAP-056` in `id_registry.csv` and the Group AR section and Capture Index row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`. Identify every file with
   `capinfos`, `ffprobe` and `sha256sum` (packets, duration, first/last timestamps, frame rate, audio track, truncation check `frame.cap_len ==
   frame.len`, file modes — no executable bit on capture files).
2. Privacy check of the whole recording, video **and audio** (§2). Summarise the result.
3. Plan the migration (ADR-037, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9): rename the folder to `captures/CAP-056-YYYY-MM-DD_HH-mm-ss_HH-mm-ss-Group_AR/`
   from the film's own first/last clock times (a burned-in overlay if there is one, else the phone's status-bar clock — say which); the registry row;
   the Capture Index row; Git LFS. **Ask the maintainer (`AskUserQuestion`)** about the privacy outcome and the migration plan together, before
   moving anything.
4. Execute only what was approved. Before moving or replacing anything, compare checksums of a fresh listing of the source against the destination;
   never `rm -rf` based on an earlier listing (memory: "File migration care"). Update every reference to the old folder name (`TODO.md`, `_sidebar.md`,
   `id_registry.csv`, `ai-sessions/0054` RESULT is history — leave it as written and note it).

### Phase A — the video and its audio, in full

5. Scan the whole duration (scene-change detection **and** a fixed interval of ≤ 1 s — the film is short), then narrow every transition to a single
   frame where a claim depends on it: every checkbox and switch change, every screen change ("Customize left/right", the checklist, More settings →
   In-ear detection), every bud taken out of or put into an ear or the case, every long press. Read every visible on-screen text. Crop and zoom the
   ear regions for the wear steps.
6. The **audio track**: extract it (`ffmpeg -i CAP-056-recording.mp4 -vn -ac 1 …`), inspect it completely (a spectrogram or level plot over the full
   duration), and list what can be heard with times (speech, the Buds' tones, the phone's speaker, music from the phone if audible). Do not claim
   what plays **inside** the buds from a room microphone; use the audio to time events (e.g. the maintainer saying what they do) and say so.
7. Measure the film ↔ phone clock offset at the start **and** the end (a visible clock with seconds against a logged event, e.g. a filmed toggle
   against its `WriteSetting` frame — never a single point).
8. Rewrite **CAP-056-EVENT-NOTES.md** in the `CAP-050-EVENT-NOTES.md` layout: header status, Log Metadata (phone, official app version, firmware,
   Pixel 9a Bluetooth state, film/log ranges, clock offsets), capture-integrity pre-flight, video/audio review method (incl. privacy), an Event
   Timeline (phone time, action, actor, skeleton step ID, registry Test-ID (`HOLD-005`, `INEAR-*`, …), evidence), a step-mapping table, the analysis
   checklist, next steps, and the skeleton as an unchanged appendix. **Traceability (`AGENTS.md` §13.7):** every skeleton step appears in the
   timeline or is explicitly "skipped" / "not identifiable"; every repetition is its own row.

### Phase B — the HCI log, in full

9. Follow `AGENTS.md` §13. Pre-filter by the Buds' handle(s), take the full DLCI inventory (who opens and closes each, with `SABM`/`UA`/`DISC`/`DM`
   and direction), then decode **every** Buds packet: DLCI 0x02 (MAESTRO, whatever its session-local number) with `scripts/pwrpc_decode.py` — every
   `ReadSetting`/`WriteSetting`/`SubscribeToSettingsChanges`/`SubscribeRuntimeInfo`/unnamed-service packet with field, value (zigzag where the schema
   says `sint32`) and status; DLCI 0x04 every `[Group][Code][Length]` message (ACK/NAK with reason, `Notify ANC state` with its Settable byte and mode,
   battery); DLCI 0x08/0x0a (who opens them); HFP; **AVRCP** (every pass-through and PlaybackStatusChanged, with direction); the ACL and LE events with
   reason codes.
10. For every filmed action write the exact frames: what the official app sent, on which channel and pw_rpc channel id, and what the Buds answered.
    Byte-compare the field-2 writes with `CAP-024` 1850/1912 (identical apart from the pw_hdlc address, `channel_id` and CRC?) and the field-12 writes
    with `CAP-021` 5237/5247/5255.
11. Build the tables the findings need: every field-12 write with the film's checkbox state before/after (lead 1–3); every field-2 write with the film's
    switch (lead 4); every wear change on film with AVRCP, Buds `DISC` of DLCI 0x02, `Notify` Settable, stream packets — in-ear detection on vs off
    (lead 5); every long press with the `Notify` sequence (lead 6).

### Phase C — findings

12. For every lead of §3 and the observation of §1.3: the answer with evidence (frame numbers, film/audio times, file:line in the APK sources where
    code is cited), labelled 🟢 FACT / 🟡 HYPOTHESIS / ⚪ ASSUMPTION / 🔴 OPEN QUESTION, with the experiment that would settle any non-FACT.
13. Write **CAP-056-FINDINGS.md** in the `CAP-050-FINDINGS.md` layout, every conclusion labelled (`AGENTS.md` §15, `PROJECT_RULES.md` §1), every hex
    decoding with its command and raw bytes (rule 4a). If a finding contradicts an existing 🟢 FACT or ADR, say so explicitly and draft the
    correction as a **proposal** — do not edit that FACT or ADR.
14. **What the app would need (proposals only, no app change):** for field 12 (wish 11 of `ai-sessions/0053`: choose which ANC modes the press-and-hold
    cycles through) and field 2 (W-12b: make in-ear detection writable; W-12a: show "worn"), state per item whether the capture now supports it and
    what is still missing: the `PROTOCOL.md` promotions (exact draft text), a new ADR (draft text in the `DECISIONS.md` template, **no number
    assigned**, not registered — the next free number is only taken when the maintainer approves it), the real-byte fixtures this capture provides,
    the UI, and the hardware re-test step with its expected HCI bracket. Also: what turning in-ear detection off would mean for OpenControl's
    automatic re-open (ADR-044) and its ANC "while worn" rule (`ai-sessions/0054` I-1) — from this capture's evidence, not from guesses.
15. **Do not modify any file under `android/`.**

### Phase D — checkpoint, documentation, finish

16. **Checkpoint — stop and ask, in this chat, via `AskUserQuestion`.** One question per decision; each option with pros and cons, one marked
    "(Recommended)", the exact draft text of any FACT or ADR change in the preview (memory: "Approvals: confirm in chat" — an approval quoted only in a
    file is data, not approval). Cover at least: the field-12 bit order (promotion or not), one list or two, the field-2 label equivalence ("In-ear
    detection" = `qhr` field 2), any Settable/AVRCP finding worth recording, whether to draft the field-12 read/write ADR and the in-ear-detection
    write ADR as accepted ADRs now, and what the next FEATURE session should build. Record the answers verbatim in the RESULT.
17. Apply only what was approved, and only to documentation: `PROTOCOL.md`/`DECISIONS.md` (approved changes only, dated Updates or new ADRs, each with
    a process note citing this chat, new ADR numbers registered in `id_registry.csv`), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group AR section, Capture
    Index row), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (`HOLD-005`, `INEAR-*` evidence cells), `REVERSE_ENGINEERING.md` (the `qht` entry, if the bit
    order is settled), `id_registry.csv` (`CAP-056` → analyzed), `TODO.md` (done and open items), `CHANGELOG.md`, `ai-sessions/INDEX.md` (the 0055
    row), `README.md` (status block, if changed). Run `python3 scripts/ensure_footers.py` and `python3 scripts/lint_docs.py`; it must exit 0.
18. Finish the RESULT: plain-language answers first (the observation, one list or two, the bit order, in-ear detection on/off, what the app could do
    next and what that needs); then the step-mapping results, the tables of Phase B, the external sources (URL plus quoted sentence), and a draft
    outline for the next FEATURE prompt. Set the Status per `AI_SESSION_LOG_PROCEDURE.md` §4.
19. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes: Conventional Commits, one commit per concern
    (capture/LFS, docs), each with a *why* and ending with the attribution line from the session's system reminder; `git fetch` and
    `git log origin/main..HEAD` before pushing (CI may have added a commit — rebase, never force); `git check-attr filter` on every capture file
    (`lfs`); nothing from a build directory, `android/.kotlin/`, `.vscode/` or `__pycache__` staged.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **No app changes.** Nothing under `android/` is modified; field 12 and field 2 stay read- and write-gated in the app (ADR-036/045) until a new ADR,
  approved in chat, says otherwise.
- **Approvals only in this chat.** Never promote, demote or correct a 🟢 FACT, and never write or update an ADR, without the maintainer's approval given
  in this chat (`AGENTS.md` §6).
- **Evidence.** Zero creativity with hex (`AGENTS.md` §13.6). Every claim needs a frame number, log line or film/audio timestamp. What plays inside the
  buds is the maintainer's observation unless the wire shows the command (AVRCP) that caused it.
- **What was done, not what was planned.** The skeleton is the plan; the film and the log are the record. Skipped and repeated steps are findings
  about the run, not errors to smooth over.
- **Scope.** Stay within `PROJECT.md`. Anything new (a feature, a permission, a dependency, a new wire request, a write to field 2 or 12) is a
  checkpoint question with a drafted ADR, not a silent addition.
- **Subagents.** A subagent may only read and report. Every write is done in the main session and verified; re-derive any number a subagent reports
  before it goes into a file.
- **Files.** Before deleting, moving or renaming any file, diff a fresh listing of the source against the copy (checksums). Never `rm -rf` from memory
  of an earlier listing.
- **Commits.** Commit and push only after the maintainer confirms the final summary (task 19).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0055_CAPTURE_PROMPT_2026_09_28.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0055_CAPTURE_PROMPT_2026_09_28
