# 0072_CAPTURE_PROMPT_2026_10_05.md — Full analysis of CAP-053 (Group AO: EQ field 16 vs 18), CAP-054 (Group AP: the `0xFE2C` advertisement on case-open, connection-free) and CAP-058 (Group AT: `SDP-001` 3rd attempt with a process-liveness check)

**Number:** 0072
**Category:** CAPTURE
**Date:** 2026-10-05
**Title:** Fully analyse three captures recorded on 2026-10-05 on the Pixel 7a with the **official** Pixel Buds app 1.0.955078536 — `CAP-053` (Group AO),
`CAP-054` (Group AP) and `CAP-058` (Group AT, including the extra tests at the end of its film): films first, then the HCI logs with `tshark`, then the
correlation; record the real events in each **CAP-NNN-EVENT-NOTES.md** and the analysis in each **CAP-NNN-FINDINGS.md** — **no change to the app itself**,
**no new capture skeleton**

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this repository. Run the
phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded. Within a phase, handle the captures in the order
`CAP-053` → `CAP-054` → `CAP-058`.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action, take the time to read, in this order and in
full, to understand the ground rules before auditing anything: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md`, `DECISIONS.md`
(**every** ADR, ADR-001 … ADR-053, with every dated Update), `TODO.md`. These are the ground rules; do not audit or change anything before they are read.
Then, per task (each named section in full before anything in it is relied on or changed):

- `AI_SESSION_LOG_PROCEDURE.md` (in full, incl. §4b, the closing checklist), `ai-sessions/INDEX.md`, `id_registry.csv` (the `CAP-053`, `CAP-054`, `CAP-058` rows
  and every Test-ID named below).
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3, §5, §8, §9 (Capture Index), and the sections **Group AO**, **Group AP** and **Group AT**. **Group AT was rewritten on
  2026-10-05 in the chat that wrote this prompt** (the `pixelbuds` grep correction, the `pidof` check with a positive control, the `logcat -b events` process
  log, the outcome (a)/(b) rule) — that text was **not yet committed** when this prompt was written. Read **both** versions: the committed one
  (`git show HEAD:CAPTURE_BLUETOOTH_HCI_SNOOP.md`, or the commit that holds it) and the current one. The maintainer may have followed either; establish which
  from the film and the files present.
- `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` for every Test-ID these Groups name: `EQS-004`, `EQP-008` (AO); `BATT-007`, `BATT-002`, `BATT-003` (AP); `SDP-001`,
  `SDP-002` (AT); plus any Test-ID an extra action on film exercises (e.g. `PAIR-001`/`PAIR-002`/`PAIR-003` for the re-pair in CAP-058).
- The **skeletons**: each `CAP-NNN-EVENT-NOTES.md` as committed (`git show HEAD:<path>`) **and** the working tree. On 2026-10-05 the maintainer added to
  `CAP-053-EVENT-NOTES.md` and `CAP-054-EVENT-NOTES.md` a block "Google pixel buds app version" (`adb shell dumpsys package … | grep …` →
  `versionName=1.0.955078536`, `versionCode=10253511`). `CAP-058-EVENT-NOTES.md` is the unchanged skeleton of 2026-09-24. Keep the maintainer's additions.
- **The layout to follow, read in full:** `captures/CAP-049-2026-09-12_18-13-54_18-21-49-Group_AF/CAP-049-EVENT-NOTES.md` and `…/CAP-049-FINDINGS.md` (the
  maintainer names these as the examples).
- **Possibly relevant earlier analyses** (the maintainer's list; read the sections that concern Groups AO, AP and AT — the maintainer's list said "for the
  analysis of CAP-068", which the chat that wrote this prompt reads as a slip for these three captures; ask if in doubt):
  `ai-sessions/0016_CAPTURE_RESULT_2026_09_13.md`, `0017_MAINTENANCE_RESULT_2026_09_13.md` (where Groups AO and AP were added),
  `0031_MAINTENANCE_PROMPT_2026_09_18.md` and `0031_MAINTENANCE_RESULT_2026_09_18.md` (where Group AT was added), `0032_MAINTENANCE_RESULT_2026_09_18.md`,
  `0040_FEATURE_RESULT_2026_09_19.md`, `0043_FEATURE_PROMPT_2026_09_22.md`, `0044_AUDIT_RESULT_2026_09_23.md`, `0045_MAINTENANCE_RESULT_2026_09_24.md` (the
  CAP-058 skeleton), `0058_AUDIT_RESULT_2026_09_30.md`, `0059_MAINTENANCE_PROMPT_2026_09_30.md`, `0059_MAINTENANCE_RESULT_2026_09_30.md`,
  `0068_AUDIT_RESULT_2026_10_03.md` (incl. `A68-PROT-06`), `0069_MAINTENANCE_RESULT_2026_10_03.md` (lead L68-6, the Group AP redesign),
  `0071_CAPTURE_RESULT_2026_10_04.md`; `captures/CAP-043-2026-09-13_09-50-31_09-53-41-Group_Q/CAP-043-FINDINGS.md` (the connection-free, case-closed
  `0xFE2C` baseline for AP); `captures/CAP-068-2026-10-04_07-22-57_07-54-10-Group_BD/CAP-068-EVENT-NOTES.md`;
  `captures/CAP-069-2026-10-04_16-05-29_16-27-07-Group_BE/CAP-069-EVENT-NOTES.md` and `CAP-069-FINDINGS.md` (the most recent official-app run on this phone);
  `CHANGELOG.md`, `PROJECT.md`, `PROTOCOL.md`, `DECISIONS.md`, `REVERSE_ENGINEERING.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `TODO.md`.
- Per Group, also: **AO** — `PROTOCOL.md` §4.2 (the "Outer field 16 vs. 18" item and every Update), `REVERSE_ENGINEERING.md`'s `qjw` entry (`fyd.d`/`fyd.e`,
  `hod.java:36` "Navigate away, save EQ"), `CAP-015-FINDINGS.md` (the 15 field-18 frames). **AP** — `PROTOCOL.md` §4.3 Option A (incl. the Correction of
  2026-10-03), `DECISIONS.md` ADR-006, `DESKRESEARCH_FINDINGS.md` where it covers the Battery Notification. **AT** — `CAP-033-FINDINGS.md` and
  `CAP-044-FINDINGS.md` (both in full: §1–§5), `REVERSE_ENGINEERING.md`'s `gbm`/`fzd` entries, `DECISIONS.md` ADR-018.
- `reverse-engineering/APK_VERSIONS.md` (the analysed version is `1.0.955078536-10253511` — the version used in all three captures; see §1).

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
**ai-sessions/0072_CAPTURE_RESULT_2026_10_05.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and update that block at the
end of every phase **and** after every substantial step within a phase (every film reviewed per minute block, every HCI log per channel decoded, every
checkpoint answer): what is done, what is next, which files are touched but unverified, and where intermediate results live (the session's scratchpad
directory — scripts, contact sheets, decoded tables, notes; re-create them if the scratchpad is gone). A resumed session reads this prompt, then the RESULT's
Progress block, re-reads the files the unfinished step touched, and continues from there. It never redoes a finished, recorded step and never assumes an
unrecorded step was done. The prompt keeps its number and date; the RESULT keeps growing in the same file.

**Nothing is taken on trust, including this prompt, the skeletons, earlier RESULTs and the maintainer's statements.** Every claim and every number below (§2)
is re-derived from the films, the HCI logs, the other files and official documentation.

---

## 1. What the maintainer asked for (chat, 2026-10-05, translated from Dutch)

1. **Analyse `CAP-053` (Group AO), `CAP-054` (Group AP) and `CAP-058` (Group AT) fully and record the findings.** Work in phases, strictly sequentially; be
   able to resume automatically after a token limit.
   - Analyse the films first (`CAP-053-recording.mp4`, `CAP-054-recording.mp4`, `CAP-058-recording.mp4`) and record every event and action with its time in
     `CAP-053-EVENT-NOTES.md`, `CAP-054-EVENT-NOTES.md` and `CAP-058-EVENT-NOTES.md` respectively.
   - Analyse **all** `CAP-*-btsnoop_hci` log files of these captures with `tshark`.
   - Then correlate the events of each EVENT-NOTES with its HCI log(s) and any other log file; `CAP-049-EVENT-NOTES.md` is the example.
   - Do an extensive analysis and record the findings in **CAP-053-FINDINGS.md**, **CAP-054-FINDINGS.md** and **CAP-058-FINDINGS.md**; `CAP-049-FINDINGS.md` is the
     example.
2. **Facts the maintainer gave (record them as the maintainer's statements; check each against the evidence where possible):**
   - **Not every step was done exactly as in the test plan.** The maintainer also showed a number of extra things on film. So: **establish from the film and the
     log what was actually done**, in the order it was done — never fill a timeline from the skeleton. Map each real action to the Group's numbered steps and
     mark it **done**, **done differently**, **repeated** (each repetition its own row), **skipped** or **not identifiable**; list every extra action as its own
     row ("not in the plan").
   - **The maintainer does not speak on the films.** Establish whether the audio tracks hold anything usable (they are declared AAC); use audible events
     only for timing, and say so.
   - **The official Pixel Buds app was version 1.0.955078536 in all three captures.** (Context: on 2026-10-05 the maintainer downgraded the app from
     1.0.990706425 to 1.0.955078536 by uninstalling and installing the pulled APK set from `reverse-engineering/apk/v1.0.955078536-10253511/`; the app data was
     wiped by the uninstall. 1.0.990706425 was pulled to `reverse-engineering/apk/v1.0.990706425-10260911/` — not decompiled, `TODO.md` §4.) The maintainer
     also observed that after the downgrade **1.0.955078536 shows no "Find device" either** (`TODO.md` §4, Ring status, 🟡). Read the app version on film where
     it is shown.
   - **The captures were made on a Pixel 7a with Android 17.** Read the build on film where it is shown.
   - **`CAP-058` has a number of extra tests at the end of its film. Record their findings too** — each with its own rows in the timeline and its own section
     in **CAP-058-FINDINGS.md**.
   - The maintainer expects the two `.log.last` files to be superfluous ("probably outside the window of the video"). **Check this; do not assume it** (see §2).
3. **Strict execution rules** — see §5.

---

## 2. The evidence

Measured by the chat that wrote this prompt (2026-10-05); **re-check each value** (`capinfos`, `ffprobe`, `sha256sum`, the films' own overlay clocks).

| Capture | File | Measured / what to check first |
|---|---|---|
| `CAP-053` | **CAP-053-recording.mp4** | 184.54 s, 142,302,330 bytes, H.264 1280×720, rotation −90 (portrait), 5,502 video frames (≈ 29.8 fps), AAC audio (7,913 frames); `creation_time` 2026-10-05T15:49:39Z (= 17:49:39 local — probably the film's **end**, so a start near 17:46:35; measure the overlay / status-bar clock at both ends). Mode `-rwxr-----`; untracked |
| | **CAP-053-btsnoop_hci.log** | 2,924 packets, `bluetooth-h4-linux`, 2026-10-05 17:46:25.846 – 17:53:48.054. Untracked |
| | **CAP-053-btsnoop_hci.log.last** | 3,081 packets, 17:39:52.118 – 17:46:23.518 — ends ≈ 2 s before `.log` starts. Probably before the film, but establish the film's start from its own clock and check whether anything in `.last`'s last minutes belongs to the run (e.g. the EQ screen being opened). A Bluetooth off/on at ≈ 17:46:24 is the likely cause of the split — check |
| | **CAP-053-EVENT-NOTES.md** | the skeleton (tracked) + the maintainer's app-version block (uncommitted) |
| `CAP-054` | **CAP-054-recording.mp4** | 356.02 s, 274,942,258 bytes, H.264 1280×720, rotation −90, 10,654 video frames (≈ 29.9 fps), AAC audio; `creation_time` 2026-10-05T16:08:29Z (= 18:08:29 local, probably the end → start near 18:02:33). Mode `-rwxr-----`; untracked |
| | **CAP-054-btsnoop_hci.log** | 4,807 packets, 18:02:45.507 – 18:10:34.930. **No `.log.last`** — check what the film shows in the ≈ 12 s before the log starts. The group requires the Buds **not connected** — check for any classic ACL to the Buds |
| | **CAP-054-EVENT-NOTES.md** | the skeleton (tracked) + the maintainer's app-version block (uncommitted) |
| `CAP-058` | **CAP-058-recording.mp4** | 391.01 s, 302,393,835 bytes, H.264 1280×720, rotation −90, 11,695 video frames (≈ 29.9 fps), AAC audio; `creation_time` 2026-10-05T19:46:22Z (= 21:46:22 local, probably the end → start near 21:39:51). Mode `-rwxr-----`; untracked |
| | **CAP-058-btsnoop_hci.log** | 6,093 packets, 21:40:06.636 – 21:49:13.292 — starts ≈ 15 s after the film's probable start and runs ≈ 3 min past its probable end (the extra tests? check) |
| | **CAP-058-btsnoop_hci.log.last** | 13,250 packets, **18:02:45.507** – 21:40:04.504. **Not superfluous by default:** its first timestamp is identical to `CAP-054-btsnoop_hci.log`'s first, so it probably contains CAP-054's whole log plus ≈ 3.5 h after it — including the preparation of CAP-058 (the force-stop, the Forget, the observation below at 21:31:03). Establish whether `CAP-054-btsnoop_hci.log` is a byte-prefix of it (compare packets, not only times), what lies between 18:10:35 and 21:40:04 that belongs to the Buds, and whether the Forget / un-bond and any SDP happened there, before `.log` starts |
| | **CAP-058-adb-shell-dumpsys-activity-processes.txt** | 17,501 bytes; **six** runs of `date ; adb shell dumpsys activity processes \| grep -i com.google.android.apps.wearables.maestro.companion` (the laptop's clock, CEST): **21:40:22** → process present, PID 6219 (`MaestroCompanionDeviceService c:android` bound; also a `com.google.android.gms/.auth.api.proxy.AuthService` record with the app as client); **21:40:35** → **no output** (process absent — the 21:40:22 run with the same filter is the positive control); **21:40:51** → present again with a **new PID 7893**; **21:41:00, 21:41:21, 21:42:14** → PID 7893. So the process was restarted between 21:40:35 and 21:40:51. Re-derive all of this from the file. Note: the laptop clock (`date`) and the phone clock (HCI log, status bar) are different clocks — measure their offset where both are visible, or say that it is unmeasured |
| | **CAP-058-EVENT-NOTES.md** | the unchanged skeleton of 2026-09-24 (`ai-sessions/0045`) — its step 2 still names `grep -i pixelbuds` |
| | **Not present** | no CAP-058-proc-events.txt (`logcat -b events`, the revised Group AT's process log), no `pidof` output, no system log, no bugreport — record each absence and what it means for outcome (a)/(b) |

All three folders still carry the placeholder name `CAP-NNN-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_XX`. `git check-attr filter` → `lfs` for the films, the HCI logs
**and** the `.txt` file; `unspecified` for the notes (re-check). `git status` at the time of writing: on **`main`** (up to date with `origin/main`, `771f400`);
uncommitted changes from the chat of 2026-10-05 in `TODO.md` (§4: decompile 1.0.990706425; Ring status: the downgrade observation) and
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group AT rewrite), plus the maintainer's two EVENT-NOTES additions and this prompt file. Carry these over to the session
branch; do not lose or rewrite them.

**Observations by the chat that wrote this prompt (2026-10-05, Pixel 7a over adb — the maintainer's statements by proxy; not in any capture file):**
- ≈ 21:3x (phone clock): `adb shell pidof com.google.android.apps.wearables.maestro.companion` → `5056`, `exit=0`.
- `adb logcat -b events -d` held
  `10-05 21:31:03.366 I/am_proc_start( 1768): [0,5056,10278,com.google.android.apps.wearables.maestro.companion,bound-service,{com.google.android.apps.wearables.maestro.companion/com.google.android.apps.wearables.maestro.companion.companiondevice.MaestroCompanionDeviceService}]`
  — the app's process started by a bind to its CompanionDeviceManager service, ≈ 9 min before the CAP-058 film. `am_proc_start`, `am_proc_died` and `am_kill`
  occurred in the events buffer; `am_force_stop` did not (no force-stop had been done then).
- Earlier the same evening the maintainer's own `dumpsys activity processes` showed PID 22553 with `com.android.settings` bound to
  `MaestroDeviceSettingsProviderService` and `android` bound to `MaestroCompanionDeviceService`.

These are context for the 🟡 HYPOTHESIS in the revised Group AT text (a force-stop does not keep the process dead because Settings / CompanionDeviceManager
bind its services). CAP-058's own dumpsys file is the evidence for this run; use the observations above only as context, labelled as such.

**Known pitfalls (from `CAP-049`, `CAP-056`, `CAP-063`–`CAP-069` — check, do not assume):**
- With `H4 with linux header` `bluetooth.addr` is often empty — scope every filter by the Buds' connection handle(s) from the Connection Complete events
  (`AGENTS.md` §13.1); handles restart after Bluetooth off/on and after a re-pair; show each filter matching a known frame. The Pixel 7a's logs hold other
  devices (a Fitbit and other LE devices were seen before).
- **LE advertisements (CAP-054):** the Buds advertise from a resolvable private address that changes — scope by the advertiser's content
  (`btcommon.eir_ad.entry.uuid_16 == 0xfe2c`), then per advertisement the service data's field headers; say per advertisement why it is attributed to the
  Buds. Check whether the phone's controller was scanning at all in each window (the HCI log only holds advertising reports the host asked for) — a missing
  report is not a missing advertisement; this is the positive-control question for every "no `0x3`/`0x4` field" result. The app under test scans nothing
  (`AGENTS.md` §7).
- **RFCOMM DLCIs depend on who opened the multiplexer** (`PROTOCOL.md` §2.3): MAESTRO is server channel 1 (DLCI 2 or 3), the Message Stream channel 2 (DLCI
  4 or 5), GSND CONTROL channel 4 (DLCI 8 or 9), GSND AUDIO channel 5 (DLCI 0x0a or 0x0b). `scripts/pwrpc_decode.py` reads DLCI 2 **and** 3 per handle
  (`--handle 0x…`); check the CRC-32 of each pw_hdlc frame yourself where a claim depends on it. One RFCOMM payload can hold several messages.
- **EQ (CAP-053):** field 16 and field 18 each hold five little-endian floats; quote every write with its outer field, the five values and its frame, and
  the settings-stream mirror that follows.
- **SDP (CAP-058):** the "default internal rfcomm socket" UUID `3a046f6d-24d2-7655-6534-0d7ecb759709` and the "pigweed" / "MAESTRO APP" UUID `25e97ff7-…`
  (`REVERSE_ENGINEERING.md`) — search **both byte orders** in the raw bytes, every SDP transaction, both directions; show the search matching the known UUID
  first (positive control).
- A filter on `data.data` does not see frames a dissector has claimed (HFP, AVDTP, SDP) — use `frame contains` or the protocol's own field (`AGENTS.md` §13
  step 8). AVRCP is `btavctp or btavrcp`. A `tshark` exit status ≠ 0 is an error, not "0 frames".
- DLCI 0x0a/0x0b content is audio: count and time its frames only, quote no payload.

**Privacy (standing rule, ADR-037).** Check **every** frame of all three films for personal data: notifications, messages, contact names, e-mail addresses,
the Google account shown in the app or in Settings, Android's device details, the Wi-Fi network name, a burned-in **street-address overlay** (earlier
camera films carried one — memory "Films: privacy + clock"), the laptop screen (CAP-058 may show the terminal with `adb` output — paths and host name are fine,
anything personal is not), faces. Report what you find and **ask** (`AskUserQuestion`). If a blur is wanted, propose the exact `ffmpeg` command, the ranges and
a before/after sample. Never write the Buds' full address or serial into a document (the "…DR3209" suffix form for the serial, ADR-010 for addresses).

---

## 3. Leads and context — verify each before relying on it

1. **CAP-053 / Group AO — which action writes outer field 18 (`EQS-004`, `EQP-008`, `PROTOCOL.md` §4.2):** for each of the three isolated conditions —
   release-only (sliders 1 and 2, ≥ 10 s, no Save, no navigation), release + **Save** (slider 3), release + **navigate away** (slider 4) — every
   `WriteSetting` on DLCI 2/3 within the window with its outer field (16 or 18), its five floats, the gap to the filmed action, and the settings-stream mirror.
   Apply the Group's analysis literally: does field 18 appear on release alone (as `CAP-015`'s timing suggested), only on Save, only on navigate-away, or on
   more than one? The code trace (`qjw` entry: Save handler and `hod.java:36`, no release path) is the prediction to test — say per condition whether the wire
   agrees, with `file:line` from `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/` (the same version as the run). A "no field-18 write" window
   needs its command, exit status and a positive control (a field-18 write found by the same filter elsewhere in the log). If the evidence closes the open
   item, draft the `PROTOCOL.md` §4.2 change as a **proposal** — for the checkpoint.
2. **CAP-054 / Group AP — the `0xFE2C` advertisement on case-open, connection-free (`BATT-007`, `BATT-002`, `BATT-003`, lead L68-6, ADR-006):** per filmed
   step (lid closed ≥ 60 s baseline, lid open ×2, one bud out/in, the other bud out/in) every `0xFE2C` advertisement from the Buds with its time, its service
   data and its field headers. Does a field of type `0x3` (show UI) or `0x4` (hide UI) with three battery bytes appear, and when (lid open, bud out)? Compare
   with `CAP-043`'s closed-case baseline. Quote the Fast Pair `batterynotification` page (raw fetch, the exact sentence) for the layout. Whether the phone
   connected by itself when the lid opened (a classic ACL or an LE connection to the Buds) — if so, its time, and only the advertisements before it are the
   sample. Whether the official app was force-stopped as the Group asks (step 1). If a battery layout is found, decode it byte by byte with the spec's own field
   definitions (charging bit, 7-bit level, Left/Right/Case order) and compare with the levels shown on film, if any. Result → `PROTOCOL.md` §4.3 Option A
   proposal and the ADR-006 consequences (the only Case level without a connection) — proposals only.
3. **CAP-058 / Group AT — `SDP-001`, 3rd attempt:**
   - Establish from the film, the dumpsys file and the logs the exact order of force-stop, Forget, the liveness checks, the "Pair" tap, the SDP browse(s) and
     the app open; mark which version of the Group AT procedure (committed / revised) was followed.
   - Every SDP transaction on the wire (both `.log` and `.log.last`): its time, handle, direction, the service-search pattern, and every UUID in the response
     (both byte orders); the service names (`MAESTRO APP`, `GSND CONTROL`, `GSND AUDIO`, … — `CAP-033` frame 1279).
   - The process state at the SDP browse: from the dumpsys runs (present 21:40:22, absent 21:40:35, back with a new PID 21:40:51) and the film. What started
     the process again between 21:40:35 and 21:40:51 — which filmed action (opening Bluetooth settings, the device page, the pairing screen, the Fast Pair
     half-sheet, the Forget itself)? Without the `logcat -b events` file the cause is not on record — say so, and say what can still be concluded.
   - Apply the revised Group AT's usability rule literally: outcome (a) a clean process-dead window up to and including the SDP browse, or (b) a restart with
     its cause, which would show "app never running" unreachable through this path; or neither (not decidable from this run) — and why.
   - The `SDP-001` question itself: does `3a046f6d-…` ever appear? With the earlier 5 negatives (`CAP-033`, `CAP-044` and the files `REVERSE_ENGINEERING.md`
     lists), what does this run add? Draft any status change of `SDP-001` / the `gbm`/`fzd` entries / ADR-018 as a **proposal**.
   - **The extra tests at the end of the film** (the maintainer's statement): identify each from the film, give each its own timeline rows, Test-ID(s) where one
     fits (or "none"), its wire evidence and its own findings section.
4. **The official app 1.0.955078536 after the downgrade:** anything the wire or the film shows that differs from the earlier 1.0.955078536 captures
   (`CAP-036`, `CAP-041`, `CAP-056`) or from 1.0.990706425 (`CAP-069`) — the connect burst, the settings sweep, services called, the missing Find device
   (the maintainer: absent in 1.0.955078536 too now) — is a finding. Fresh app data (wiped by the uninstall) may change the first-run behaviour; say where it
   does. Decompiling 1.0.990706425 is **not** part of this session (`TODO.md` §4).
5. **Connection lifecycle and other protocols, per capture:** every ACL with its reason code; RFCOMM `SABM`/`UA`/`DISC`/`DM` per DLCI and side; the
   Message Stream claims (the Play-services marker `03 08 00 02 01 25`, ADR-032); HFP (`AT+BIEV`), AVRCP, A2DP, LE; firmware from the `GetSoftwareInfo`
   announcement; for CAP-058 the bonding itself (SSP, link-key, the Fast Pair key-based pairing if any — describe the wire, ADR-008/ADR-025).
6. **Anything new or wrong:** NAKs, pw_rpc error statuses, unknown services or methods, settings fields never seen before, Message Stream groups not in
   `PROTOCOL.md`.

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename, mode change or `git add`)

1. Create the RESULT file (§0). Record `git log -1`, `git branch --show-current`, `git status --short`, `git fetch && git log --oneline HEAD..origin/main`.
   **Work on a new branch from `origin/main`** (e.g. `capture/0072-cap-053-054-058`), carrying over every uncommitted change listed in §2 and the untracked
   capture files without losing any. Confirm the `planned` rows of `CAP-053`, `CAP-054`, `CAP-058` in `id_registry.csv` and the Group AO/AP/AT sections and
   Capture Index rows in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`. Identify every file with `capinfos`, `ffprobe` and `sha256sum` and re-check every value in §2.
2. Privacy check of all three films (§2). Summarise the result per film.
3. Plan the migration (ADR-037, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9) per capture: rename each folder to `captures/CAP-NNN-YYYY-MM-DD_HH-mm-ss_HH-mm-ss-Group_XX/`
   from the film's first and last overlay times (say which clock); keep the file names; `chmod 644`; the registry rows; the Capture Index rows; Git LFS for
   the films, the logs and the `.txt`. Propose what to do with each `.log.last` (keep, with the reason from the evidence, or leave out). **Ask the maintainer
   (`AskUserQuestion`)** about the privacy outcome, the `.log.last` files and the migration plan together, before moving anything.
4. Execute only what was approved. Before moving anything, compare checksums of a fresh listing of the source against the destination; prefer a plain `mv` of
   each folder; never chain deletes; never `rm -rf` based on an earlier listing (memory "File migration care"). Update every reference to the old folder names
   outside `ai-sessions/` (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TODO.md`, `id_registry.csv`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `README.md`); earlier
   `ai-sessions/` files are history — leave them as written. Update the paths in this prompt too once the folders are renamed, with a note, so
   `scripts/lint_docs.py` stays clean (as `ai-sessions/0071` did).

### Phase A — the films, in full (CAP-053, then CAP-054, then CAP-058)

5. Per film: scan the whole film (a fixed interval of ≤ 2 s, plus scene changes), then narrow every transition to ≤ 1 s, and to a single frame (or 4 fps)
   where a claim depends on it: every tap and slider drag/release (CAP-053: the `Save` button never touched in the release-only windows, the screen never
   changed — the Group requires both on film), every screen change, every lid open/close and bud out/in (CAP-054), every force-stop, Forget, terminal command
   and its visible output, the "Pair" tap, the Fast Pair half-sheet, the app open (CAP-058), every Bluetooth toggle, every toast and dialog, every action not
   in the plan. Read every visible on-screen text, including app version, Android build and firmware rows.
6. The audio streams: establish whether they hold anything; if they do, use audible events only for timing, saying so.
7. Measure the film ↔ phone clock offset at the start **and** the end of each film (a status-bar minute flip against the overlay, at ≥ 4 fps — never a single
   point). For CAP-058 also the laptop clock (the dumpsys `date` lines) against the phone clock, if the terminal is on film.
8. Rewrite each **CAP-NNN-EVENT-NOTES.md** in the `CAP-049` layout: header status, Log Metadata (phone, Android build, the official app's version, Play
   services, firmware, other devices, film/log ranges, the clock offsets), capture-integrity pre-flight, video review method (incl. privacy), an Event Timeline
   (phone time, action, actor, step, Test-ID, evidence with frame numbers and film time), a step-mapping table, the analysis checklist, and the skeleton as an
   unchanged appendix (incl. the maintainer's app-version block). **Traceability (`AGENTS.md` §13.7):** every Group step and every Test-ID it names appears in
   the timeline or is explicitly "skipped" / "not identifiable"; every repetition and every extra action is its own row.

### Phase B — the HCI logs, in full (all five files)

9. Follow `AGENTS.md` §13. Per file: pre-filter by the Buds' handle(s) (CAP-054: by the `0xFE2C` advertiser content), take the full RFCOMM inventory, then
   decode **every** Buds packet: MAESTRO on DLCI 2/3 (every `GetSoftwareInfo`, every `ReadSetting`/`WriteSetting`/`SubscribeToSettingsChanges`/
   `SubscribeRuntimeInfo` with field, value and status; CRC-32 per frame where it matters), the Message Stream per claim, DLCI 0x08/0x09, DLCI 0x0a/0x0b (counts
   and timing only), SDP (every transaction), SMP/SSP and the link key events (CAP-058), HFP, AVRCP, A2DP, every LE advertising report from the Buds
   (CAP-054), every ACL and LE connection with its reason code. Every negative with its command, exit status and a positive control. For each `.log.last`:
   what it covers, how it relates to `.log` and to the other captures' logs, and whether any of it falls inside a film.
10. Put every relevant frame on the timeline (phone time after the measured offset). For every filmed action write the exact frames: what the phone sent, on
    which channel, what the Buds answered. Where the film shows nothing for a frame (or the log nothing for a filmed action), say so.
11. Build the tables the findings need: per Group the actions against the frames and the Group's analysis question applied literally; CAP-053: every EQ write
    (outer field, five floats, trigger); CAP-054: every `0xFE2C` advertisement per step with its field headers; CAP-058: the process-state timeline (dumpsys
    runs, film, frames) against every SDP transaction and its UUIDs, and the extra tests.

### Phase C — findings

12. For every lead of §3: the answer with evidence (frame numbers, film times, `file:line`), labelled 🟢 FACT / 🟡 HYPOTHESIS / ⚪ ASSUMPTION / 🔴 OPEN
    QUESTION, with the experiment that would settle any non-FACT.
13. Write **CAP-053-FINDINGS.md**, **CAP-054-FINDINGS.md** and **CAP-058-FINDINGS.md** in the `CAP-049-FINDINGS.md` layout, every conclusion labelled
    (`AGENTS.md` §15, `PROJECT_RULES.md` §1), every hex decoding with its command and raw bytes (rule 4a), at most one status banner (rule 9a). Include per
    capture: what was actually done against the plan, the answer to the Group's question, what went wrong in the run and what that limits, per protocol what
    was seen, what differs from earlier captures, and — for CAP-058 — the extra tests. If a finding contradicts an existing 🟢 FACT or ADR, say so and draft the
    correction as a **proposal**; do not edit that FACT or ADR. Where a Group's question stays open, write the procedure change a next attempt needs (as a
    proposal in FINDINGS and a `TODO.md` line — not a new skeleton).
14. Consequences for OpenControl (proposals only — change nothing under `android/`, `dist/` or `scripts/`): e.g. the EQ write OpenControl sends (field 16 vs
    18), a Case level from the advertisement (ADR-006 bounds), anything in CAP-058 that matters for connecting. For each: the evidence, the proposed change, the
    guardrails, what it needs first (a 🟢 FACT promotion, an ADR — drafted without a number), the unit test (real-byte fixtures from these captures,
    `AGENTS.md` §11) and the hardware re-test.

### Phase D — checkpoint, documentation, finish

15. **Checkpoint — stop and ask, in this chat, via `AskUserQuestion`.** One question per decision; each option with pros and cons, one marked "(Recommended)",
    the exact draft text of any FACT, ADR or `PROTOCOL.md` change in the preview (memory "Approvals: confirm in chat"). Cover at least: the field-16/18
    result and `PROTOCOL.md` §4.2; the `0xFE2C` result, `PROTOCOL.md` §4.3 Option A and ADR-006; the `SDP-001` result and its status, ADR-018, and the Group AT
    procedure for any 4th attempt; the findings of CAP-058's extra tests; the `.log.last` files if not settled in Phase 0. **No new capture skeleton is written
    in this session.** Record the answers verbatim in the RESULT.
16. Apply only what was approved, and only to documentation: `PROTOCOL.md`/`DECISIONS.md` (approved changes only, dated Updates or new ADRs with a process note
    citing this chat, numbers registered in `id_registry.csv`), `REVERSE_ENGINEERING.md` (approved changes only), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group
    AO/AP/AT run notes, Capture Index rows), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the evidence cells of the Test-IDs exercised — pointers only), `id_registry.csv`
    (`CAP-053`, `CAP-054`, `CAP-058` → analyzed with the real times), `TODO.md` (§2: remove the rows of the three captures once analysed; open items added),
    `ai-sessions/INDEX.md` (the 0072 row), `README.md` (capture counts in "Current state"). Run `python3 scripts/ensure_footers.py` and
    `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py`; it must exit 0 — report anything else. (`scripts/__pycache__/lint_docs.cpython-314.pyc` is tracked;
    `git checkout` it if it changed.)
17. Finish the RESULT: plain-language answers first (per capture: what was done, the answer to its question, what it means for OpenControl; the extra tests of
    CAP-058); then the step-mapping results, the Phase B tables, the external sources (URL plus quoted sentence). It must end with **"Deferred documentation"**
    (each item also added to `TODO.md`) and **"Commits"** (hashes, or "not committed — reason"), per `AI_SESSION_LOG_PROCEDURE.md` §9 and §4b. Set the Status
    per `AI_SESSION_LOG_PROCEDURE.md` §4.
18. Show the maintainer a short summary and **ask whether to commit, push and open a pull request**. Only after a yes: on the session branch, Conventional
    Commits, one commit per concern (the chat's carried-over edits of 2026-10-05; capture files/LFS per capture; docs), each with a *why* and ending with the
    attribution line from the session's system reminder; `git check-attr filter` on every capture file (`lfs`); **no file under `android/`, `dist/` or
    `scripts/` in any commit**; nothing from a build directory, `android/.kotlin/`, `.vscode/` or `__pycache__` staged. A large LFS push can drop the SSH
    connection after the upload — check `git log origin/<branch> -1` and push again if needed.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Strict execution rules (the maintainer's own words, translated):**
  - **No assumptions.** Verify everything.
  - **Validate externally.** Use search tools to validate claims against official documentation (Android Developer docs / AOSP sources — e.g. for
    `dumpsys activity processes`, `am_proc_start`, service binding of a stopped app, `CompanionDeviceManager`; the Bluetooth Core Specification — SDP, SSP,
    LE advertising reports; the Fast Pair specification — `batterynotification`, the Message Stream) where needed; cite the URL and the exact sentence.
    (developer.android.com pages have returned only navigation to the fetch tool before; AOSP sources at `android.googlesource.com/…?format=TEXT` read with
    `curl … | base64 -d`, and the raw Fast Pair pages via `curl`, worked — check any fetch-tool summary against the raw text before quoting it.)
  - **No sampling.** No random spot checks: a 100 % complete and exhaustive review of the relevant files, checking every detail — every second of the three
    films and every packet of the five HCI logs that belongs to the Buds.
  - **Best practices.** Industry-standard technical review methods: **evidence traceability** (every claim links back to a specific frame number, a film
    timestamp, or a `file:line`) and **structural integrity** checks (files, registry, links, footers).
- **No app changes and no release actions.** Nothing under `android/`, `dist/` or `scripts/` is modified; no build is made; no tag or release.
- **Approvals only in this chat.** Never promote, demote or correct a 🟢 FACT and never write or update an ADR without the maintainer's approval given in this
  chat (`AGENTS.md` §6). Model agreement is not approval.
- **Evidence.** Zero creativity with hex (`AGENTS.md` §13.6). Every claim needs a frame number, a film timestamp or a `file:line` (`PROJECT_RULES.md` rule
  4a). A negative needs its command, exit status and a positive control (`AGENTS.md` §13 step 8).
- **What was done, not what was planned.** The skeletons are the plan; the films and the logs are the record. Skipped, repeated and extra steps are findings
  about the run. Never fill a timeline row from a skeleton's "Expected" column.
- **Privacy.** No full MAC address or serial in any committed text beyond what ADR-010 allows; no personal data from the films.
- **Scope.** Stay within `PROJECT.md`. Fast Pair Account Linking / ownership and Google's Find network are out of scope (ADR-008, ADR-027); Play services is
  not reverse-engineered (ADR-025). Anything new for the app (a feature, a permission, a dependency, a background service, a scan, a new wire request) is a
  checkpoint question with a drafted ADR, not a silent addition.
- **Subagents.** A subagent may only read and report. Every write is done in the main session and verified; re-derive any number a subagent reports before it
  goes into a file.
- **Files.** Before deleting, moving or renaming any file, diff a fresh listing of the source against the copy (checksums). Never `rm -rf` from memory of an
  earlier listing; ask before any delete.
- **Commits.** Commit and push only after the maintainer confirms the final summary (task 18).
- **No new capture skeleton.** Do not create a new `CAP-NNN` folder, skeleton or `planned` registry row in this session; open steps go to the findings and
  `TODO.md`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0072_CAPTURE_PROMPT_2026_10_05.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0072_CAPTURE_PROMPT_2026_10_05
