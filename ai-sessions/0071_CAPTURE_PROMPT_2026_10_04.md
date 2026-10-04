# 0071_CAPTURE_PROMPT_2026_10_04.md — Full analysis of CAP-069 (Group BE: the official Pixel Buds app 1.0.990706425 on the Pixel 7a — head gestures, Multipoint, assistant hold, a tap on the current ANC mode, EQ "Default", wear states, Find)

**Number:** 0071
**Category:** CAPTURE
**Date:** 2026-10-04
**Title:** Fully analyse `CAP-069` (one film, one HCI snoop log, the skeleton notes), recorded with the **official** Pixel Buds app (new version
1.0.990706425, 2026-09-30) while following the Group BE skeleton in `CAP-069-EVENT-NOTES.md`; record the real events in **CAP-069-EVENT-NOTES.md** and the
analysis in **CAP-069-FINDINGS.md**; establish what was actually done (steps were skipped and repeated); correlate the official app's behaviour with every
protocol; list the improvements OpenControl could build from it — **no change to the app itself**, **no new capture skeleton**

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this repository. Run the
phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action, take the time to read, in this order, to
understand the ground rules before auditing anything: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md` (incl. the Definition of done and "Status after 1.0.x"),
`ARCHITECTURE.md` (in particular §3.1, §5a, §6.0b, §8.1), `PROTOCOL.md` (in particular §2.2a with its Updates, §2.3, §4.1 with its Settable-byte Updates and the
2026-10-04 `ANC-006` Update, §4.2 with the 2026-10-03 "Flat" Update, §4.3 Option E/F, §4.4 with its 2026-10-03 Correction and 2026-10-04 Update, §4.5.2
Multipoint, §4.5.3 press-and-hold, §4.5.4 Head gestures, §4.5.5 In-ear detection, §6), `DECISIONS.md` (**every** ADR, ADR-001 … ADR-051, with every dated Update —
in particular ADR-008, ADR-011, ADR-013, ADR-017, ADR-019, ADR-024/ADR-049, ADR-025, ADR-027, ADR-034, ADR-036, ADR-045, ADR-046, ADR-047), `TODO.md` (§1, §3,
§4 — head gestures and Multipoint are the next app features, each "only after its `PROTOCOL.md` entry is 🟢 FACT and its own ADR exists"). These are the ground
rules; do not audit or change anything before they are read. Then, per task:

- `AI_SESSION_LOG_PROCEDURE.md` (in full, incl. §4b, the closing checklist), `ai-sessions/INDEX.md`, `id_registry.csv` (the `CAP-069` row and every Test-ID
  the skeleton names).
- The **skeleton** `captures/CAP-069-2026-10-04_16-05-29_16-27-07-Group_BE/CAP-069-EVENT-NOTES.md` as committed (`git show HEAD:<path>` — purposes I–VII,
  P1–P7, BE-1 … BE-29, "Don'ts", its analysis checklist and "Refuted if" column) and the working tree (check whether the maintainer added anything).
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3, §5, §8, §9 and the Group BE section; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` for every Test-ID the skeleton names (`HEAD-001`,
  `MULTI-001`, `HOLD-003`, `HOLD-004`, `ANC-001`…`ANC-004`, `ANC-006`, `EQP-001`, `INEAR-002`…`INEAR-004`, `CASE-004`, `CASE-005`, `FIND-001`, `FIND-005`,
  `PAIR-003`, `BATT-004`).
- **The layout to follow, read in full:** `captures/CAP-056-2026-09-28_17-30-53_17-35-58-Group_AR/CAP-056-EVENT-NOTES.md` and `…/CAP-056-FINDINGS.md` (an
  official-app settings run on the Pixel 7a; the maintainer names these as the examples).
- **Possibly relevant earlier analyses, read the sections that concern CAP-069's items:** `ai-sessions/0069_MAINTENANCE_RESULT_2026_10_03.md` (the leads
  L68-1 … L68-7 and why CAP-069 was planned), `ai-sessions/0070_CAPTURE_RESULT_2026_10_04.md` and
  `captures/CAP-068-2026-10-04_07-22-57_07-54-10-Group_BD/CAP-068-EVENT-NOTES.md` / `CAP-068-FINDINGS.md` (§2 `ANC-006`, §6 the Ring by direction — OpenControl's
  side of the same questions), `captures/CAP-019-2026-08-21_07-35-50_07-39-30-Group_C/CAP-019-FINDINGS.md` (the one Multipoint OFF write, frame 2482),
  `DESKRESEARCH_FINDINGS.md` (the entry of 2026-10-03: DLCI 0x08 Code `0x03`/`0x05`, runtime-info field 2, the L68-5 table), `PROJECT.md`, `README.md`,
  `TODO.md`, and `CAP-020`/`CAP-021` FINDINGS where the skeleton cites their frames (head gestures 1935/2038; press-and-hold 1895/3619/4315/4976; the DLCI 0x0a
  waves).
- `reverse-engineering/APK_VERSIONS.md` (the analysed version is `1.0.955078536-10253511`; this run used **1.0.990706425** — a version never pulled or
  decompiled).

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
**ai-sessions/0071_CAPTURE_RESULT_2026_10_04.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and update that block at the
end of every phase **and** after every substantial step within a phase (every 5 minutes of film reviewed, the HCI log per channel decoded, every checkpoint
answer): what is done, what is next, which files are touched but unverified, and where intermediate results live (the session's scratchpad directory —
scripts, contact sheets, decoded tables, notes; re-create them if the scratchpad is gone). A resumed session reads this prompt, then the RESULT's Progress block,
re-reads the files the unfinished step touched, and continues from there. It never redoes a finished, recorded step and never assumes an unrecorded step was
done. The prompt keeps its number and date; the RESULT keeps growing in the same file.

**Nothing is taken on trust, including this prompt, the skeleton, earlier RESULTs and the maintainer's statements.** Every claim and every number below (§2) is
re-derived from the film, the HCI log, the code and official documentation.

---

## 1. What the maintainer asked for (chat, 2026-10-04, translated from Dutch)

1. **Analyse `CAP-069` fully and record the findings.**
   - Analyse the film (`CAP-069-recording.mp4`) first and record every event and action with its time in **CAP-069-EVENT-NOTES.md**.
   - Analyse `CAP-069-btsnoop_hci.log` with `tshark`.
   - Then correlate the events of `CAP-069-EVENT-NOTES.md` with the HCI log; `CAP-056-EVENT-NOTES.md` is the example.
   - Analyse what does not work well and what goes wrong. Which functionality works well, which does not?
   - Record all findings in **CAP-069-FINDINGS.md**; `CAP-056-FINDINGS.md` is the example.
   - Correlate the app's behaviour with the different protocols: what goes well, what goes wrong, what should be improved?
2. **Then analyse and produce a list of improvements that can be built into OpenControl. Change nothing in the OpenControl app itself.**
3. **Facts the maintainer gave (record them as the maintainer's statements; check each against the evidence where possible):**
   - **Not every step was done (correctly).** Several times one or more steps were skipped by accident, or repeated because something went wrong. So:
     **establish from the film and the log what was actually done**, in the order it was done — never fill the timeline from the skeleton. Map each real action
     to the skeleton's step IDs (P1–P7, BE-1 … BE-29) and mark it **done**, **done differently**, **repeated** (each repetition its own row), **skipped** or
     **not identifiable**; list every extra action as its own row ("not in the plan").
   - **The new official app (1.0.990706425, 30 September 2026) no longer has "Find device"**; that now apparently goes through the **Google Find Hub app**.
     Section VII (BE-28/BE-29) could therefore not be run as written. Establish from the film what was done instead (if anything) and what the wire shows;
     read the app version on film if it is shown. The maintainer's observation: "find device is no longer part of the official Google Pixel Buds app".

---

## 2. The evidence

> *Note (2026-10-04, by the executing session, after the maintainer approved the migration in chat — `AskUserQuestion` "Migration", option
> "Approve as shown (Recommended)"):* the folder was renamed from the placeholder `CAP-069-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BE` to
> `CAP-069-2026-10-04_16-05-29_16-27-07-Group_BE`; the paths in this prompt were updated to the new name (the committed skeleton is
> `git show 0378076:captures/CAP-069-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BE/CAP-069-EVENT-NOTES.md`). Nothing else in this prompt was changed.

`captures/CAP-069-2026-10-04_16-05-29_16-27-07-Group_BE/` — measured by the chat that wrote this prompt (2026-10-04); **re-check each value**:

| File | Measured / what to check first |
|---|---|
| **CAP-069-recording.mp4** | 1,297.98 s, 987,545,073 bytes, H.264 1280×720, rotation −90 (portrait), ≈ 29.9 fps; `creation_time` 2026-10-04T14:27:07Z (= 16:27:07 local — probably the film's **end**; measure the film's own overlay clock at both ends). An audio stream is declared with codec "unknown" — establish whether it holds any samples (`CAP-067`'s film had an empty audio track). Untracked; mode `-rwxr-----` |
| **CAP-069-btsnoop_hci.log** | 10,582 packets, `Bluetooth H4 with linux header`, 2026-10-04 16:05:56.025 – 16:30:06.677. **One** file (no `.log.last`) — check whether Bluetooth was toggled during the run (the skeleton's P2 toggles it before the run) and whether anything is missing at the start. Untracked |
| **CAP-069-EVENT-NOTES.md** | the skeleton (committed), unchanged in the working tree at the time of writing. Rewrite it into the record in the `CAP-056` / `CAP-068` layout; keep the planned procedure as an unchanged appendix |
| Not present | no system log, no app logcat, no bugreport, no `.log.last`, no events file — record each absence and what it means for the checks that needed it |

`git check-attr filter` → `lfs` for the film and the HCI log, `unspecified` for the notes (re-check). The film file once had a misspelt name
("recoding"); the maintainer renamed it to `CAP-069-recording.mp4` — check that only this name exists.

**Known pitfalls (from `CAP-056`, `CAP-063`–`CAP-068` — check, do not assume):**
- With `H4 with linux header` `bluetooth.addr` is empty — scope every filter by the Buds' connection handle(s) from the Connection Complete events
  (`AGENTS.md` §13.1, as changed 2026-10-03); handles restart after Bluetooth off/on; show each filter matching a known frame. The Pixel 7a's log may hold
  other devices (a Fitbit and other LE devices were seen in earlier Pixel 7a captures).
- **RFCOMM DLCIs depend on who opened the multiplexer** (`PROTOCOL.md` §2.3, 2026-10-03): MAESTRO is server channel 1 (DLCI 2 or 3), the Message Stream
  channel 2 (DLCI 4 or 5), GSND CONTROL channel 4 (DLCI 8 or 9), GSND AUDIO channel 5 (DLCI 0x0a or 0x0b). Count both DLCIs of a channel.
  `scripts/pwrpc_decode.py` reads DLCI 2 **and** 3 per handle, DLCI and direction (`--handle 0x…`); check the CRC-32 of each pw_hdlc frame yourself where a
  claim depends on it. `scripts/message_stream_tally.py` tallies ANC `Set`/ACK/NAK. One RFCOMM payload can hold several messages.
- **With Play services present, the Message Stream is held by Play services** (`ARCHITECTURE.md` §6.0b, ADR-032): its claims start with `03 08 00 02 01 25`
  (`CAP-066-FINDINGS.md`); the official app's ANC commands ride on it. DLCI 0x08/0x0a are opened by the Google app's assistant service (🟡,
  `CAP-061-FINDINGS.md` §2). Reverse-engineering Play services is out of scope (ADR-025); its *wire* behaviour may be described.
- `field 17` (balance) is `sint32` (zigzag); `field 16`/`18` hold five little-endian floats; field 29 = Head gestures (🟡 1 = off, 2 = on); field 11 =
  Multipoint (🟡 0 = off, 1 = on); field 12 = the press-and-hold ANC-mode list; field 7 = press-and-hold per bud.
- A filter on `data.data` does not see frames a dissector has claimed (HFP, AVDTP) — use `frame contains` or the protocol's own field (`AGENTS.md` §13 step 8).
  AVRCP is `btavctp or btavrcp`. A `tshark` exit status ≠ 0 is an error, not "0 frames".
- **DLCI 0x0a/0x0b content is audio** (the assistant's microphone stream): count and time its frames only — decode nothing and quote no payload
  (the skeleton's "Don'ts").

**Privacy (standing rule, ADR-037).** Check **every** frame of the film for personal data: notifications, messages, contact names, e-mail addresses, the Google
account shown in the official app or in Find Hub (account name, e-mail, **location map**), Android's device details, the Wi-Fi network name in Quick Settings
(`CAP-068`: an address-like SSID was readable — the maintainer chose "commit as is" for `CAP-068`; earlier choices do not carry over), a burned-in
street-address overlay, faces other than what the maintainer chose to film. **A Find Hub screen may show a map with the Buds' location — treat it as personal
data.** Report what you find and **ask** (`AskUserQuestion`). If a blur is wanted, propose the exact `ffmpeg` command, the ranges and a before/after sample.
Never write the Buds' full address or serial into a document (the "…DR3209" suffix form for the serial, ADR-010 for addresses).

---

## 3. Leads and context — verify each before relying on it

1. **Head gestures, field 29 (BE-1 … BE-5, lead L68-4, `HEAD-001`):** every `WriteSetting 4:{29:n}` with its tap on film and the settings-stream mirror; the
   DLCI 0x08 `04 16` value after each. Both directions, twice, each on film ⇒ a **🟢 FACT proposal** for `PROTOCOL.md` §4.5.4 and a **draft ADR** (no number)
   unblocking the read + write of field 29 — for the maintainer's decision at the checkpoint (`TODO.md` §4: the next feature).
2. **Multipoint, field 11 (BE-6 … BE-8, lead L68-3, `MULTI-001`):** every `4:{11:n}` and the SASS `07 11` flags byte after each; compare the changed bit with the
   Fast Pair SASS page's flag table (fetch the raw page, quote the sentence). FACT proposal + draft ADR as in lead 1 — only if the evidence holds.
3. **Assistant press-and-hold (BE-9 … BE-13, lead L68-1, `HOLD-004`/`HOLD-003`):** the field-7 write; per hold on film, the first DLCI 0x08 frame (🟡 `01 09 00
   03 0a 01 xx`) and the first DLCI 0x0a frame after it and the gap; BE-12 as the negative window (command, exit status, BE-10 as positive control).
4. **A tap on the current ANC mode (BE-14 … BE-16, lead L68-7, `ANC-006`):** count `08 12` in the BE-15/16 windows against BE-14's positive control. Compare
   with `CAP-068` (OpenControl sends a `Set`, the Buds ACK it — `PROTOCOL.md` §4.1 Update 2026-10-04). The maintainer decided "keep the `Set`; decide after
   CAP-069" — prepare that decision (options with pros and cons).
5. **EQ "Default" (BE-17/BE-18, `EQP-001`, `A68-APP-16`):** the five floats of the Default write against OpenControl's "Flat" (0.0 × 5, `CAP-015` frame 2111).
   If they differ, propose what OpenControl should do (rename, add a preset) — proposal only.
6. **Wear and placement states (BE-19 … BE-27, lead L68-5):** a table step → DLCI 0x08 `04 05 00 02 08 xx` → `04 03` → the latest `Notify` Settable byte → the
   battery charging bits → the runtime-info 6.x fields; which values repeat; additions to the `DESKRESEARCH_FINDINGS.md` table (2026-10-03 entry). Use the
   head-side mapping (head on the **right** of the frame = **Left** bud) only where the head is in view, and say so per row.
7. **Find (BE-28/BE-29, `FIND-001`/`FIND-005`, lead L68-2):** the app no longer offers Find device (the maintainer). Establish what was done on film (Find Hub?
   nothing?), and list by direction every `04 01` and `ff 01` frame on the Message Stream channel in the whole log. If a ring was started from Find Hub and
   stopped on the bud, is there a Buds `04 01 00 01 00` without a phone command before it, and a phone ACK `ff 01 00 02 04 01` — the open question of
   `PROTOCOL.md` §4.4 (🟡) and `TODO.md` §4. Note ADR-027 (Case/"both" ring through Google's network is out of scope) and ADR-008/ADR-025 (no account linking, no
   Play-services reverse engineering): describe the wire, nothing behind it. **Whether OpenControl's own Find feature is affected: no** (it sends its own
   command, `CAP-068` §6) — say so only with that evidence.
8. **The new official app version 1.0.990706425:** read it on film if shown (the skeleton's P6 asks for Settings → About phone and the firmware row). Anything
   in the wire behaviour that differs from the `1.0.955078536` captures (`CAP-036`, `CAP-041`, `CAP-056`) — connect burst, settings sweep, services called — is a
   finding. Pulling or decompiling the new APK is **not** part of this session; propose it as a follow-up if the wire shows changes (`ADR-017` boundary).
9. **Connection lifecycle and other protocols:** every ACL with its reason code; RFCOMM `SABM`/`UA`/`DISC`/`DM` per DLCI and side; HFP (`AT+BIEV`), AVRCP, A2DP,
   LE; the Buds-side `DISC`s at wear changes; the phone's re-paging (`PROTOCOL.md` §5 Notes). Firmware from the `GetSoftwareInfo` announcement.
10. **Anything new or wrong:** NAKs, pw_rpc error statuses, a `SERVER_ERROR`, unknown services or methods, settings fields never seen before, Message Stream
    groups not in `PROTOCOL.md`.

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename, mode change or `git add`)

1. Create the RESULT file (§0). Record `git log -1`, `git branch --show-current`, `git status --short`, `git fetch && git log --oneline HEAD..origin/main`.
   **Work on a new branch from `origin/main`** (e.g. `capture/0071-cap-069`), without losing the untracked capture files. Confirm the `planned` row of `CAP-069`
   in `id_registry.csv` and the Group BE section and Capture Index row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`. Identify every file with `capinfos`, `ffprobe` and
   `sha256sum` and re-check every value in §2.
2. Privacy check of the film (§2). Summarise the result.
3. Plan the migration (ADR-037, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9): rename the folder to `captures/CAP-069-YYYY-MM-DD_HH-mm-ss_HH-mm-ss-Group_BE/` from the
   film's first and last overlay times (say which clock); keep the file names; `chmod 644`; the registry row; the Capture Index row; Git LFS for the film and
   the log. **Ask the maintainer (`AskUserQuestion`)** about the privacy outcome and the migration plan together, before moving anything.
4. Execute only what was approved. Before moving anything, compare checksums of a fresh listing of the source against the destination; prefer a plain `mv` of
   the folder; never chain deletes; never `rm -rf` based on an earlier listing. Update every reference to the old folder name outside `ai-sessions/`
   (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TODO.md`, `id_registry.csv`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `README.md`); earlier `ai-sessions/` files are history —
   leave them as written. (This prompt names the placeholder path; update it here too once the folder is renamed, with a note, so `scripts/lint_docs.py` stays
   clean — `ai-sessions/0070` did the same.)

### Phase A — the film, in full

5. Scan the whole film (a fixed interval of ≤ 2 s, plus scene changes), then narrow every transition to ≤ 1 s, and to a single frame (or 4 fps) where a claim
   depends on it: every tap (a toggle, a mode, a preset, press-and-hold choices, Find Hub), every screen and tab change, every bud taken out of or put into the
   case or an ear (with the head-side mapping where the head is in view), every press-and-hold on a bud, the assistant overlay, every Bluetooth toggle, every
   Android settings screen, every toast and dialog, every action not in the plan. Read every visible on-screen text, including the app version and firmware rows.
6. The audio stream: establish whether it holds samples; if it does, use audible events only for timing, saying so.
7. Measure the film ↔ phone clock offset at the start **and** the end of the film (a status-bar minute flip against the overlay, at ≥ 4 fps — never a single
   point).
8. Rewrite **CAP-069-EVENT-NOTES.md** in the `CAP-056` / `CAP-068` layout: header status, Log Metadata (phone, Android build, the official app's version, Play
   services, firmware, other devices, film/log ranges, the clock offset, the wear mapping), capture-integrity pre-flight, video review method (incl. privacy), an
   Event Timeline (phone time, action, actor, step ID, Test-ID, evidence with frame numbers and film time), a step-mapping table, the analysis checklist, and the
   skeleton as an unchanged appendix. **Traceability (`AGENTS.md` §13.7):** every skeleton step and every Test-ID it names appears in the timeline or is
   explicitly "skipped" / "not identifiable"; every repetition and every extra action is its own row.

### Phase B — the HCI log, in full

9. Follow `AGENTS.md` §13. Pre-filter by the Buds' handle(s), take the full RFCOMM inventory (`SABM`/`UA`/`DISC`/`DM`, direction, per ACL and multiplexer side),
   then decode **every** Buds packet: MAESTRO on DLCI 2/3 (every `GetSoftwareInfo` with its channel and entries; every `ReadSetting`/`WriteSetting`/
   `SubscribeToSettingsChanges`/`SubscribeRuntimeInfo`/`SERVER_STREAM` with field, value and status; every other service and method; CRC-32 per frame); the
   Message Stream per claim (who opened it — the Play-services marker; `08 11`/`08 12`/`08 13`, ACK/NAK with reason; battery; SASS `07 xx`; Find with directions);
   DLCI 0x08/0x09 (every `04 03`, `04 05`, `04 16`, `0e 01`, `01 09` …) and DLCI 0x0a/0x0b (frame counts and timing only); HFP; AVRCP; A2DP; every ACL and LE
   event with its reason code. Every negative with its command, exit status and a positive control.
10. Put every relevant frame on the timeline (phone time after the measured offset). For every filmed action write the exact frames: what the app sent, on which
    channel, what the Buds answered. Where the film shows nothing for a frame (or the log nothing for a filmed action), say so.
11. Build the tables the findings need: per section I–VII the actions against the frames and the skeleton's "Refuted if" applied literally; every settings write
    and read with its field and value; every Message Stream claim with its first phone message; every ANC `Set`/ACK/NAK; the wear-state table (step → `04 05` →
    `04 03` → Settable → charging bits → runtime info); the session-end table (Buds `DISC`, ACL drops, re-opens); every Find frame by direction.

### Phase C — findings and improvements

12. For every lead of §3: the answer with evidence (frame numbers, film times, `file:line` where the app's code is cited), labelled 🟢 FACT / 🟡 HYPOTHESIS /
    ⚪ ASSUMPTION / 🔴 OPEN QUESTION, with the experiment that would settle any non-FACT.
13. Write **CAP-069-FINDINGS.md** in the `CAP-056-FINDINGS.md` layout, every conclusion labelled (`AGENTS.md` §15, `PROJECT_RULES.md` §1), every hex decoding with
    its command and raw bytes (rule 4a), at most one status banner (rule 9a). Include: what works and what does not in the official app (incl. the missing Find
    device), what goes wrong, per protocol (MAESTRO pw_rpc, Message Stream, SASS, DLCI 0x08/0x0a, HFP, AVRCP/A2DP, LE, Android's link state), and what differs
    from the earlier official-app captures. If a finding contradicts an existing 🟢 FACT or ADR, say so and draft the correction as a **proposal**; do not edit
    that FACT or ADR.
14. **Improvements for OpenControl (proposals only — change nothing under `android/`, `dist/` or `scripts/`):** for each, the evidence from this capture, the
    proposed change (file and function, the behaviour), the guardrails (no new permission, service, dependency or background work; no polling timer; ADR-044's
    one attempt per event; Safe Mode, ADR-042), what it needs first (a 🟢 FACT promotion, an ADR — draft it without a number), the unit test that would cover it
    (real-byte fixtures from this capture, `AGENTS.md` §11) and the hardware re-test step with its expected HCI bracket. At least: head gestures (field 29),
    Multipoint (field 11), the `ANC-006` decision, the EQ "Default" preset, the ring-status handling (`FIND-005`), any wear-state signal worth showing.

### Phase D — checkpoint, documentation, finish

15. **Checkpoint — stop and ask, in this chat, via `AskUserQuestion`.** One question per decision; each option with pros and cons, one marked "(Recommended)",
    the exact draft text of any FACT, ADR or `PROTOCOL.md` change in the preview (memory: "Approvals: confirm in chat"). Cover at least: the field-29 and
    field-11 FACT proposals and their draft ADRs (only where the evidence holds); `ANC-006` (keep OpenControl's `Set` or skip it); the EQ "Default" result; the
    Find / `FIND-005` result and `PROTOCOL.md` §4.4; the L68-5 samples; whether to pull and decompile the new official app version (a follow-up session, ADR-017);
    the order of the improvements. **No new capture skeleton is written in this session.** Record the answers verbatim in the RESULT.
16. Apply only what was approved, and only to documentation: `PROTOCOL.md`/`DECISIONS.md` (approved changes only, dated Updates or new ADRs with a process note
    citing this chat, numbers registered in `id_registry.csv`), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BE run note, Capture Index row), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
    (the evidence cells of the Test-IDs exercised — pointers only), `DESKRESEARCH_FINDINGS.md` (only if the L68-5 table grows — a dated addition),
    `reverse-engineering/APK_VERSIONS.md` (only a line that version 1.0.990706425 was used in a capture, not pulled — if the maintainer agrees), `id_registry.csv`
    (`CAP-069` → analyzed with the real times), `TODO.md` (done items removed, open items added), `ai-sessions/INDEX.md` (the 0071 row), `README.md` (capture
    counts in "Current state"). Run `python3 scripts/ensure_footers.py` and `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py`; it must exit 0 — report
    anything else. (`scripts/__pycache__/lint_docs.cpython-314.pyc` is tracked; `git checkout` it if it changed.)
17. Finish the RESULT: plain-language answers first (what works and what does not in the official app; head gestures; Multipoint; the assistant hold; `ANC-006`;
    EQ Default; wear states; Find; the improvements for OpenControl in order); then the step-mapping results, the Phase B tables, the external sources (URL plus
    quoted sentence). It must end with **"Deferred documentation"** (each item also added to `TODO.md`) and **"Commits"** (hashes, or "not committed — reason"),
    per `AI_SESSION_LOG_PROCEDURE.md` §9 and §4b. Set the Status per `AI_SESSION_LOG_PROCEDURE.md` §4.
18. Show the maintainer a short summary and **ask whether to commit, push and open a pull request**. Only after a yes: on the session branch, Conventional
    Commits, one commit per concern (capture/LFS, docs), each with a *why* and ending with the attribution line from the session's system reminder;
    `git check-attr filter` on every capture file (`lfs`); **no file under `android/`, `dist/` or `scripts/` in any commit**; nothing from a build directory,
    `android/.kotlin/`, `.vscode/` or `__pycache__` staged. A large LFS push (the film is ≈ 1 GB) can drop the SSH connection after the upload — check
    `git log origin/<branch> -1` and push again if needed.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Strict execution rules (the maintainer's own words, translated):**
  - **No assumptions.** Verify everything.
  - **Validate externally.** Use search tools to validate claims against official documentation (Android Developer docs / AOSP sources, the Bluetooth Core
    Specification, the Fast Pair specification — SASS, Hearable Controls, Device Action) where needed; cite the URL and the exact sentence.
    (developer.android.com pages have returned only navigation to the fetch tool before; AOSP sources at `android.googlesource.com/…?format=TEXT` read with
    `curl … | base64 -d`, and the raw Fast Pair pages via `curl`, worked — check any fetch-tool summary against the raw text before quoting it.)
  - **No sampling.** No random spot checks: a 100 % complete and exhaustive review of the relevant files, checking every detail — every second of the film and
    every packet of the HCI log that belongs to the Buds.
  - **Best practices.** Industry-standard technical review methods: **evidence traceability** (every claim links back to a specific frame number, a film
    timestamp, or a `file:line`) and **structural integrity** checks (files, registry, links, footers).
- **No app changes and no release actions.** Nothing under `android/`, `dist/` or `scripts/` is modified; no build is made; no tag or release.
- **Approvals only in this chat.** Never promote, demote or correct a 🟢 FACT and never write or update an ADR without the maintainer's approval given in this chat
  (`AGENTS.md` §6). Model agreement is not approval.
- **Evidence.** Zero creativity with hex (`AGENTS.md` §13.6). Every claim needs a frame number, a film timestamp or a `file:line` (`PROJECT_RULES.md` rule 4a). A
  negative needs its command, exit status and a positive control (`AGENTS.md` §13 step 8).
- **What was done, not what was planned.** The skeleton is the plan; the film and the log are the record. Skipped, repeated and extra steps are findings about
  the run. Never fill a timeline row from the skeleton's "Expected" column.
- **Privacy.** No full MAC address or serial in any committed text beyond what ADR-010 allows; no personal data from the film (account, location map).
- **Scope.** Stay within `PROJECT.md`. Fast Pair Account Linking / ownership and Google's Find network are out of scope (ADR-008, ADR-027); Play services is not
  reverse-engineered (ADR-025). Anything new for the app (a feature, a permission, a dependency, a background service, a new wire request) is a checkpoint
  question with a drafted ADR, not a silent addition.
- **Subagents.** A subagent may only read and report. Every write is done in the main session and verified; re-derive any number a subagent reports before it
  goes into a file.
- **Files.** Before deleting, moving or renaming any file, diff a fresh listing of the source against the copy (checksums). Never `rm -rf` from memory of an
  earlier listing; ask before any delete.
- **Commits.** Commit and push only after the maintainer confirms the final summary (task 18).
- **No new capture skeleton.** Do not create a new `CAP-NNN` folder, skeleton or `planned` registry row in this session; open steps go to the findings and
  `TODO.md`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0071_CAPTURE_PROMPT_2026_10_04.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0071_CAPTURE_PROMPT_2026_10_04
