# 0070_CAPTURE_PROMPT_2026_10_04.md — Full analysis of CAP-068 (Group BD: the 1.0.1 release APK in a GrapheneOS profile without Google Play services — the hardware test of the 0069 hotfix)

**Number:** 0070
**Category:** CAPTURE
**Date:** 2026-10-04
**Title:** Fully analyse `CAP-068` (**two** films, **four** HCI snoop logs, **five** app debug exports, **one** app logcat, **no** system log), recorded with the
release-signed **1.0.1** APK in a GrapheneOS secondary user without Google Play while following the Group BD skeleton in `CAP-068-EVENT-NOTES.md`; record the
real events in **CAP-068-EVENT-NOTES.md** and the analysis in **CAP-068-FINDINGS.md**; establish what was actually done; give the release verdict for 1.0.1
(`RELEASING.md`, Release checklist step C4) and the capture evidence for `PROJECT.md`'s Definition of done criterion 2; **no change to the app itself**, **no
new capture skeleton**, **no publishing step**

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this repository. Run the
phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action, take the time to read **in full**, in this order,
to understand the ground rules before auditing anything: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md` (incl. the Definition of done, its evidence table with the
"Evidence type" column and the "Status after 1.0.x" section), `ARCHITECTURE.md` (in particular §2.4, §3.1 with the "one rule for current" bullet, §3.2 timing
constants, §5a, §6.0a, §6.0b, §7 with the 1.0.1 paragraph, §8.1, §9 with the permissions table, §9.0a item 8, §12, §13), `PROTOCOL.md` (in particular §2.2a with
its Updates, §2.3, §4.1, §4.2 with the 2026-10-03 "Flat" Update, §4.3 Option B/E/F, §4.4 Ring with its 2026-10-03 Correction, §4.5, §5, §6), `DECISIONS.md`
(**every** ADR, ADR-001 … ADR-051, with every dated Update — in particular ADR-006, ADR-008, ADR-010, ADR-020, ADR-027, ADR-032, ADR-034, ADR-037, ADR-042,
ADR-043, ADR-044, ADR-045, ADR-046, ADR-047, ADR-048, ADR-049, ADR-050, ADR-051), `TODO.md`. These are the ground rules; do not audit or change anything before
they are read. Then, per task:

- `AI_SESSION_LOG_PROCEDURE.md` (in full, incl. §4b, the closing checklist), `ai-sessions/INDEX.md`, `id_registry.csv` (the `CAP-066`, `CAP-067`, `CAP-068`,
  `CAP-069` rows and every Test-ID the skeleton names).
- **What the build under test contains — read in full:** `ai-sessions/0069_MAINTENANCE_PROMPT_2026_10_03.md` and `ai-sessions/0069_MAINTENANCE_RESULT_2026_10_03.md`
  (Summary, the per-item ledger for every `A68-APP-*` row, the Gate and mutation log, "Compliance", "`CAP-068` in one table"), `CHANGELOG.md` `[1.0.1]` and
  `[1.0.0]`, `README.md`, `RELEASING.md` (the **Release checklist** A–E, §4–§8, §11 hotfix path, §13 Release log), `APP_TESTPLAN.md` (in full — section S is the
  1.0.1 build; C5, H5, F5, J4, I4, L3, K1 are the "never run on hardware" steps), and the commits of branch `maintenance/0069` (`git log --stat cd30332..origin/maintenance/0069`;
  pull request #1 is open, not merged).
- The **skeleton** `captures/CAP-068-2026-10-04_07-22-57_07-54-10-Group_BD/CAP-068-EVENT-NOTES.md` (path updated after the folder was renamed in this session; the prompt named the placeholder folder) **as committed** (`git show HEAD:<path>` — purposes I–VII,
  P0–P7, BD-1 … BD-30, BD-end, "Don'ts", its analysis checklist and "Refuted if" column) **and** the working-tree version (the maintainer added the P0 output
  after the run — record it as the maintainer's evidence, see §2). (The maintainer's request names the group "BC" once; the folder and the skeleton are **Group
  BD** — `CAP-067` was BC.)
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3, §5, §8, §9 and the Group BD section; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` for every Test-ID the skeleton names (`ANC-001`…`ANC-004`,
  `ANC-006`, `FIND-001`, `FIND-005`, `PAIR-003`, `BATT-004`, `EQS-001`, `INEAR-004`).
- **The previous runs and their analyses — the layout to follow, read in full:** `captures/CAP-066-2026-10-01_16-12-57_16-34-50-Group_BB/CAP-066-EVENT-NOTES.md`
  and `…/CAP-066-FINDINGS.md` (clock-offset method, session-end and lid-open tables, the Play-services marker, the decoders in its header), and
  `captures/CAP-067-2026-10-03_07-57-35_08-18-29-Group_BC/CAP-067-EVENT-NOTES.md` and `…/CAP-067-FINDINGS.md` (the same set-up — 1.0.0 in the user without Play —
  and its release verdict).
- `captures/CAP-025-2026-08-21_08-40-52_08-45-26-Group_K/CAP-025-FINDINGS.md` (Find My Buds on the wire: directions, the Buds' own ring-status message — the
  reference for BD-26 … BD-28 / `FIND-005`), and `DESKRESEARCH_FINDINGS.md`, entry of 2026-10-03 (DLCI 0x08 `04 03`/`04 05`, runtime-info field 2).
- Every Kotlin source under `android/` that a finding touches, **in full**, before you write about it — at least `BudsRepositoryImpl.kt`, `CodecRouter.kt`,
  `PwRpc.kt`, `CaseBatteryFrame.kt`, `SessionDiagnostics.kt`, `SessionReopener.kt`, `RfcommBudsTransport.kt`, `BudsCompanionPairing.kt`, `PairingLogic.kt`,
  `ValueCurrency.kt`, `DeviceStatus.kt`, `AncTile.kt`, `EqBandGains.kt`, `ConnectionScreen.kt`, `AncScreen.kt`, `EqScreen.kt`, `ControlsScreen.kt`,
  `SettingsMenu.kt`, `MainActivity.kt`, `AppUiSession.kt`, `OpenControlApplication.kt`, `AncTileService.kt`, their tests, and `android/app/build.gradle.kts`.

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
**ai-sessions/0070_CAPTURE_RESULT_2026_10_04.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and update that block at the
end of every phase **and** after every substantial step within a phase (every 5 minutes of film reviewed, every HCI log and channel decoded, every log file read,
every checkpoint answer): what is done, what is next, which files are touched but unverified, and where intermediate results live (the scratchpad directory —
scripts, contact sheets, decoded tables, notes; re-create them if the scratchpad is gone). A resumed session reads this prompt, then the RESULT's Progress block,
re-reads the files the unfinished step touched, and continues from there. It never redoes a finished, recorded step, and never assumes an unrecorded step was
done. The prompt keeps its number and date; the RESULT keeps growing in the same file.

**Nothing is taken on trust, including this prompt, the skeleton, earlier RESULTs and the maintainer's statements.** Every claim — and every number measured
below (§2) — is re-derived from the films, the HCI logs, the other logs, the code and official documentation.

---

## 1. What the maintainer asked for (chat, 2026-10-04, translated from Dutch)

1. **Analyse `CAP-068` (Group BD) fully and record the findings.**
   - Analyse **both** films (`CAP-068-recording1.mp4`, `CAP-068-recording2.mp4`) first and record every event and action with its time in
     **CAP-068-EVENT-NOTES.md**.
   - Analyse **all four** HCI snoop logs with `tshark`.
   - Then correlate the events of `CAP-068-EVENT-NOTES.md` with the HCI logs **and the other log files**; `CAP-066-EVENT-NOTES.md` is the example.
   - Run an extensive analysis and record the findings in **CAP-068-FINDINGS.md**; `CAP-066-FINDINGS.md` is the example.
   - Possibly relevant: `ai-sessions/0069_MAINTENANCE_RESULT_2026_10_03.md`, `APP_TESTPLAN.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `CAP-025-FINDINGS.md`,
     `CHANGELOG.md`, `PROJECT.md`, `PROTOCOL.md`, `README.md`, `RELEASING.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `TODO.md`.
2. **Not every step was done exactly as in the test plan** — among other things because the maintainer was filming their ears — and **some extra things were
   shown on the film.** So: **establish from the films and the logs what was actually done**, in the order it was done — never fill the timeline from the
   skeleton. Map each real action to the skeleton's step IDs (BD-1 … BD-30, P0–P7) and to `APP_TESTPLAN.md` section S (S1 … S13), and mark each step **done**,
   **done differently**, **repeated** (each repetition its own row), **skipped** or **not identifiable**; list every extra action as its own row ("not in the
   plan").
3. **Facts the maintainer gave (record them as the maintainer's statements; check each against the evidence where possible):**
   - **The app and build version is the latest build, 1.0.1 — see the video: Settings → Info.** Read the Info tab on film (version, `build <hash>[-dirty]`,
     commit date, the ADR-051 notice, the firmware lines, "Control channel: N"). Check that the hash is a commit of `origin/maintenance/0069` (the release
     checklist's build commit B1) and that "-dirty" is **not** shown. `dist/1.0.1/` exists in the working tree: check the APK's SHA-256 and its certificate
     (`apksigner verify --print-certs`, read only — never rebuild, never run `scripts/release.sh`, never write into `dist/`); the certificate must be
     `a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb`. The logcat header says `package: …opencontrolpixelbuds:10001` (re-check). Cross-check
     against wording that exists only in 1.0.1 (`APP_TESTPLAN.md` section S: "— from the last connection", "Use different Buds", the FLAT chip, "—" for unread
     values, the new connect-failure and cut-off sentences, "Unexpected error: …").
   - **The capture ran on GrapheneOS (Android 17) in a profile without Google Play services.** Verify it from the logs: the logcat header (`userType`), the P0
     output the maintainer added to the notes, and above all the HCI logs — **no Message Stream claim whose first phone message is `03 08 00 02 01 25`** (the
     Play-services marker of `CAP-066-FINDINGS.md`), with the command, its exit status and a positive control (the same filter finding Play-services claims in
     `CAP-066`'s logs). Interpret the P0 output exactly: its `grep -i -E "gms|vending"` matched `app.grapheneos.gmscompat*` packages — say what those are
     (GrapheneOS's own compatibility layer; check against GrapheneOS documentation) and whether `com.google.android.gms` / `com.android.vending` are absent; the
     exit status was not recorded.
   - **Ears on film:** head on the **right** of the frame = the **Left** bud; head on the **left** of the frame = the **Right** bud. Use this mapping for every
     wear step, say in each row which side the head was on, and cross-check it against the runtime-info per-bud fields (6.2.x Left / 6.3.x Right, `PROTOCOL.md`
     §4.3 Option F) and the Settable byte at every wear change; report any contradiction.
   - **The maintainer did not speak on the film.**
   - **There are four HCI logs** (`CAP-068-btsnoop_hci.log`, `….log.last`, `CAP-068-btsnoop_hci2.log`, `…hci2.log.last`) **because Bluetooth was stopped and
     started during the filming.** Establish which file covers which part, whether files overlap or duplicate each other, the gaps between them, how many
     Bluetooth off/on cycles there were, and that no Buds traffic is lost or counted twice at a boundary.
   - **There are five debug exports, made at different moments, some with Debug mode off.** Establish for each which stretch it covers, whether Debug mode was on
     (hex lines), and which later export contains which earlier one.
   - **There are two films because the recording stopped by itself at an unexpected moment (during the pairing).** Establish the gap between them and what
     the logs show in it; nothing in the gap is taken from the plan.
   - **There is no system log, because it was not available in the profile without Google Play services.** Record this; say which skeleton checks depended on a
     system log (adapter `STATE_CHANGED`, force-stop lines, the BD-29 `ForegroundServiceDidNotStartInTimeException` check) and what other evidence (the logcat's
     events buffer, the exports, the films, the HCI logs) covers each — or that it stays unverified.
   - **The app was force-stopped during the video. Before that, the maintainer saved the debug log, the app log and the system log an extra time, to be safe.**
     Establish from the files present which were saved before and which after the force-stop, and whether every stretch of the run is covered (the export's ring
     buffer restarts with a new process).
4. **Strict execution rules (the maintainer's own words, translated):**
   - **No assumptions.** Verify everything.
   - **Validate externally.** Use search tools to validate claims against official documentation (Android Developer docs / AOSP sources, the Bluetooth Core
     Specification, the Fast Pair specification, GrapheneOS documentation) where needed; cite the URL and the exact sentence. (developer.android.com pages have
     returned only navigation to the fetch tool before; AOSP sources at `android.googlesource.com/…?format=TEXT`, read with `curl … | base64 -d`, and the raw
     Fast Pair pages via `curl` worked — check any fetch-tool summary against the raw text before quoting it.)
   - **No sampling.** No random spot checks: a 100 % complete and exhaustive review of the relevant files, checking every detail — every second of both films,
     every packet of all four HCI logs that belongs to the Buds, every line of the five exports and of the app logcat.
   - **Best practices.** Industry-standard technical review methods: **evidence traceability** (every claim links back to a specific frame number with its HCI
     file, a log line with its file, or a film timestamp with its film) and **structural integrity** checks (files, registry, links, footers).

---

## 2. The evidence

`captures/CAP-068-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BD/` — `CAP-068-EVENT-NOTES.md` is committed (the skeleton) and **modified** in the working tree (the
maintainer's P0 output); the other twelve files are **untracked**. `id_registry.csv` has a `planned` row for `CAP-068`. Measured by the chat that wrote this
prompt (2026-10-04) — **re-check each value**:

| File | Measured / what to check first |
|---|---|
| **CAP-068-recording1.mp4** | 1,069.67 s, 834,251,747 bytes, H.264 1280×720 at ≈ 29.8 fps, **AAC audio** (the maintainer did not speak — establish what the audio holds and use audible events only for timing, saying so); `creation_time` 2026-10-04T05:40:47Z (= 07:40:47 local — the film's *end*? measure the film's own clock) |
| **CAP-068-recording2.mp4** | 798.98 s, 623,542,033 bytes, same format; `creation_time` 2026-10-04T05:54:11Z. The gap between the films (the pairing) has no film |
| **CAP-068-btsnoop_hci.log.last** | 1,413 packets, 07:11:25.132 – 07:23:02.020 |
| **CAP-068-btsnoop_hci.log** | 2,219 packets, 07:23:07.514 – 07:33:48.745 |
| **CAP-068-btsnoop_hci2.log.last** | 2,314 packets, **07:23:07.514** – 07:34:58.303 — starts at the **same** instant as `CAP-068-btsnoop_hci.log`: probably the same snoop session pulled later (a superset?). Prove or refute packet by packet before counting anything; never count a frame twice |
| **CAP-068-btsnoop_hci2.log** | 3,563 packets, 07:35:44.243 – 07:56:06.297 |
| All four HCI logs | `Bluetooth H4 with linux header`. Two boundaries: ≈ 07:23:02 → 07:23:07 and ≈ 07:34:58 → 07:35:44. The exports log the adapter OFF/ON at 07:34:57–07:35:45 (`…-075355.txt` lines 429–441) — **what caused the 07:23 boundary?** (a second Bluetooth toggle, the snoop-log setting, a log rotation — establish it) |
| **CAP-068-opencontrol-debug-20261004-073517.txt** | 434 lines (209 hex lines ⇒ Debug mode on for part of it), 07:11:42.250 – 07:34:58.165 |
| **…-073601.txt** | 480 lines (225 hex), 07:11:42.250 – 07:35:51.250 |
| **…-074922.txt** | 861 lines (387 hex), 07:11:42.250 – 07:49:11.125 |
| **…-074950.txt** | 868 lines (387 hex), 07:11:42.250 – 07:49:45.704 |
| **…-075355.txt** | 941 lines (420 hex), 07:11:42.250 – 07:53:44.526 |
| All five exports | **All start at 07:11:42.250** (one process, ring buffer not wrapped) — each later export appears to contain the earlier ones. Check line by line; find the Debug-off stretch(es) (BD-25 / L3: no hex line after the "Debug mode off" line); explain the ≈ 19 s, 44 s and 28 s gaps between export times and their last lines |
| **CAP-068-OpenControl-for-Pixel-Buds-Pro-2-log-b098b04ac4a7.txt** | the app logcat, 425 lines, header `osVersion google/tegu/tegu:17/CP3A.260905.009/2026092501`, **`userType: full.secondary`**, `package: io.github.tedsluis.opencontrolpixelbuds:10001`; timestamps 10-04 05:11:42.183 – 05:54:17.956 (UTC — measure the offset, expected + 2 h); **only one PID (16124) in the whole file** — so where is the force-stop? (after 07:54:18, after the last export, or not in this process's log?) Explain every crash, ANR, exception, `tombstoned` line; StrictMode is off in a release build |
| **CAP-068-EVENT-NOTES.md** | the skeleton (the plan) plus the maintainer's P0 output (user 10; `app.grapheneos.gmscompat.config`, `…gmscompat.lib`, `…gmscompat`). Rewrite it into the record in the `CAP-066-EVENT-NOTES.md` layout; keep the planned procedure as an unchanged appendix |
| `dist/1.0.1/` (not a capture file) | APK SHA-256 `f9dce033d3a42d93892b90ebe7b77347385c93602bdec099f59d232963b15e0f` (from its `.sha256`, re-compute); check the certificate (read only) |

File modes: five files `-rw-r--r--`, eight `-rwxr-----` — no executable bit on capture files (fix in Phase 0, with approval). `git check-attr filter` → `lfs`
for all twelve capture files, `unspecified` for the notes (measured; re-check). No events file and no system log is present — record each absence.

**Known pitfalls (from `CAP-063`–`CAP-067` — check, do not assume):**
- With the `H4 with linux header` encapsulation `bluetooth.addr` is empty — scope every filter by the Buds' connection handle(s) from the Connection Complete
  events **of each file** (`AGENTS.md` §13.1, as changed 2026-10-03); handles restart after Bluetooth off/on. Show each filter matching a known frame. Other
  devices may share the log.
- **RFCOMM DLCIs are session-local** (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05). `scripts/pwrpc_decode.py` now reads DLCI 2 **and** 3 per ACL handle,
  DLCI and direction (`--handle 0x…` scopes one connection) and names the services; still check the CRC-32 of each pw_hdlc frame yourself where a claim depends on
  it. One RFCOMM payload can hold several pw_hdlc frames and several Message Stream messages. `scripts/message_stream_tally.py` tallies ANC `Set`/ACK/NAK.
- **The Message Stream claims:** the app's start with `08 11`/`08 12` (ANC) or its battery/Find sequence; Play services' with `03 08 00 02 01 25`. In this run **no**
  Play-services claim is expected — any one is a central finding.
- A filter on `data.data` does not see frames a dissector has claimed (HFP, AVDTP) — use `frame contains` or the protocol's own field (`AGENTS.md` §13 step 8).
  AVRCP is `btavctp or btavrcp`. A `tshark` exit status ≠ 0 is an error, not "0 frames".
- `field 17` (balance) is `sint32` (zigzag); `field 16` holds five little-endian floats.
- 1.0.1 logs a reader that throws as "Decoder fault on channel N: …" (A68-APP-01) — any such line is a central finding.

**Privacy (standing rule, ADR-037).** Check **every** frame of both recordings for personal data: notifications, messages, contact names, e-mail addresses,
Android's device-details, Bluetooth and pairing screens (Android's picker shows the Buds' name), **a burned-in street-address overlay** (the maintainer's camera
films have carried one before), faces other than what the maintainer chose to film (the ears). Report what you find and **ask** (earlier choices do not carry
over). If a blur is wanted, propose the exact `ffmpeg` command, the ranges and a before/after sample. Never write the Buds' full address or serial into a
document (use the "…DR3209" suffix form for the serial, ADR-010 for addresses).

---

## 3. Leads and context — verify each before relying on it

1. **The build (P1, P7, BD-19, S13):** 1.0.1 / 10001, a hash on `maintenance/0069` without "-dirty", installed over 1.0.0 (P1: "an update, no uninstall" —
   did Android accept it, were the app's data kept?). Which commit of the branch is it — and is it the **tip at build time** (the release checklist's B1)?
   Commits made on the branch after the build change no file under `android/`? (`git diff <hash>..origin/maintenance/0069 -- android` must be empty for the
   tested APK to be the release.)
2. **The Definition of done without Google Play services:** criterion 2's ANC change by a release build **with frames** (BD-1 … BD-5: `08 11` → Notify →
   `08 12` → ACK → Notify per mode, from the tab and from the tile). This is the evidence that replaces "maintainer-attested, not captured" in `PROJECT.md` and
   `RELEASING.md` — only with the maintainer's approval at the checkpoint. Criteria 1 and 3 with frames too; the negative "no Play-services claim" over all four
   logs (command, exit status, positive control).
3. **Section II — a tap on the current mode (BD-7, BD-8, `ANC-006`, lead L68-7):** does 1.0.1 send a `Set` for the mode already shown (its claim's `Get`
   answers first — `BudsRepositoryImpl.setAncModeAfterGet`), and what do the Buds answer (ACK, or NAK and its reason byte `0x04` "redundant device action")?
4. **Section III — Bluetooth off/on with exports (BD-9 … BD-12, S12):** the export lines "Bluetooth adapter: …", the loss cause "Bluetooth was switched off on
   this phone" without "(provisional", the re-open; both boundaries of §2; S12 (export across a rotation while the "save as" dialog is open — 1.0.1 moved
   `pendingLogExport` to `AppUiSession`).
5. **Section IV — the 1.0.1 changes (BD-13 … BD-19, S1 … S13):** literal on-screen texts against `APP_TESTPLAN.md` section S and `UserMessageTest`:
   values "— from the last connection" after Disconnect, dimmed with the (i) dot (ANC, Sound, Controls); the tile "Open the app"; the Case line
   "(last connection)"; FLAT → `WriteSetting 4:{16:{0.0 × 5}}` (the payload of `CAP-015` frame 2111) → OK; "—" for unread EQ bands and balance in the first
   second after ready; **Use different Buds** → Disconnect, "Pairing: use different Buds — removed N association(s) …", then *Pair a device* opens Android's
   picker (the film stopped during the pairing — what do the logs show?); S9 (several Buds) "not testable" with one pair unless the film shows otherwise; S11
   (a toast for every failed tile tap).
6. **Section V — the never-run steps (BD-20 … BD-25):** C5 (Connect twice quickly: one `SABM` on MAESTRO), H5 (Read EQ again after a write: the same five floats),
   F5 (mode changed by a press-and-hold on a bud: a Buds `Notify` without a phone `Set`, then Refresh), J4 (two minutes in the background), I4 (Ring, Disconnect,
   Connect, Stop — the words), L3 (an export with Debug mode off: no hex line). Mark each done / not done in `APP_TESTPLAN.md`'s Summary terms.
7. **Section VI — Ring stopped on the bud (BD-26 … BD-28, `FIND-005`, lead L68-2, `A68-APP-12`):** by direction, every `04 01 00 01 xx` and `ff 01 …` frame: the
   app's command, the Buds' ACK `ff 01 00 03 04 01 00`, the Buds' own `04 01 00 01 xx` — after a stop on the bud, is there a Buds `04 01 00 01 00` with no phone
   command before it, and no phone ACK `ff 01 00 02 04 01` (no Play services)? What the app showed (1.0.1 reads nothing from that message). This settles the
   🟡 status-sync reading of `PROTOCOL.md` §4.4 — a FACT proposal for the maintainer, not a change.
8. **Section VII — the connect that fails (BD-29, BD-30, S10):** the HCI status of the page attempt (`0x04` page timeout), no RFCOMM, and the exact text on
   film — 1.0.1 must not name another app (the published 1.0.0 known issue). Whether the notification flashed; without a system log the
   `ForegroundServiceDidNotStartInTimeException` check rests on the logcat's crash buffer — say so.
9. **The wear states against DLCI 0x08 (lead L68-5):** every `04 05 00 02 08 xx` and `04 03 … 18 xx` in the logs against the ears on film and the runtime-info
   per-bud fields — more samples for the table in `DESKRESEARCH_FINDINGS.md` (2026-10-03). Runtime-info field 2: absent here too (no `SetWallclock` from the app)?
10. **Session ends and re-opens** — the full table as in `CAP-066-FINDINGS.md` (Bluetooth off/on ×?, Disconnect, the force-stop, "Use different Buds", the
    pairing, Buds-side closes), the cause shown and logged, whether ADR-044's rules held.
11. **The force-stop:** when (films, logcat PIDs, export coverage), what the app showed on return, the tile; why the logcat holds one PID only.
12. **Battery and case** — every value shown on film against the wire value and its receive time (`03 03`, runtime-info 6.1/6.2/6.3), and the "last connection"
    marks of 1.0.1.
13. **Anything new or wrong:** NAKs, pw_rpc error statuses, Safe Mode (ADR-042 — expected off on `release_5.203`), any "Decoder fault" or "Unexpected error" line,
    any app frame on DLCI 0x08/0x0a (must be none), any request outside the user's actions; anything that behaves differently in a secondary user.
14. **The extra things the maintainer showed on film** — identify each and say what it demonstrates.

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename, mode change or `git add`)

1. Create the RESULT file (§0). Record `git log -1`, `git branch --show-current`, `git status --short`, `git fetch && git log --oneline HEAD..origin/main` and
   `HEAD..origin/maintenance/0069`, and `gh pr view 1 --json state,headRefOid`. **Work on branch `maintenance/0069`** (`RELEASING.md`, Release checklist C3: the
   capture and its analysis are committed after the build commit on the release branch); switch to it if needed, without losing the maintainer's uncommitted
   change to the notes. Confirm the `planned` row of `CAP-068` in `id_registry.csv` and the Group BD section and Capture Index row in
   `CAPTURE_BLUETOOTH_HCI_SNOOP.md`. Identify every file with `capinfos`, `ffprobe` and `sha256sum` and re-check every value in §2.
2. Privacy check of both recordings (§2). Summarise the result.
3. Plan the migration (ADR-037, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9): rename the folder to `captures/CAP-068-YYYY-MM-DD_HH-mm-ss_HH-mm-ss-Group_BD/` from the
   first film's first and the second film's last clock time (say which clock); keep the file names (they already carry `CAP-068-`) unless one is misleading; `chmod
   644`; the registry row; the Capture Index row; Git LFS for every capture file. **Ask the maintainer (`AskUserQuestion`)** about the privacy outcome and the
   migration plan together, before moving anything.
4. Execute only what was approved. Before moving anything, compare checksums of a fresh listing of the source against the destination; prefer a plain `mv` of the
   folder; never chain deletes; never `rm -rf` based on an earlier listing. Update every reference to the old folder name (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`,
   `TODO.md`, `id_registry.csv`, `APP_TESTPLAN.md`, `PROJECT.md`, `RELEASING.md`, `README.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `_sidebar.md`); earlier
   `ai-sessions/` files are history — leave them as written.

### Phase A — the films, in full

5. Scan the whole duration of **both** films (a fixed interval of ≤ 2 s, plus scene changes), then narrow every transition to ≤ 1 s, and to a single frame
   where a claim depends on it: every lid opening/closing, every bud taken out of or put into the case or an ear (with the head-side mapping), every tap
   (Connect, Disconnect, Refresh, a mode, the tile, a pull, a slider, a preset, the gear, a settings tab, Use different Buds, Pair a device, Export, Ring, Stop),
   every tab change, rotation, Bluetooth toggle, the force-stop, every Android settings screen (users/profiles, apps, permissions, Bluetooth, the pairing picker,
   Quick Settings), every toast, every Home / return to the app, and every action not in the plan. Read every visible on-screen text. Crop and zoom the ear, case,
   tile and card regions.
6. The **audio streams**: inspect them completely (level plot, spectrogram); use audible events (ring tones of BD-26, clicks) only for timing, saying so.
7. Measure the film ↔ phone clock offset at the start **and** the end of **each** film (a status-bar minute flip against the overlay, at ≥ 4 fps — never a single
   point), and the logcat ↔ phone and export ↔ phone offsets (an event in both).
8. Rewrite **CAP-068-EVENT-NOTES.md** in the `CAP-066-EVENT-NOTES.md` layout: header status, Log Metadata (phone, GrapheneOS build, **the user/profile and the
   absence of Google Play** with the maintainer's P0 output, the OpenControl build from the Info tab, firmware, other devices, film/log ranges per file, clock
   offsets, the wear mapping and its cross-check), capture-integrity pre-flight (all four HCI logs incl. completeness and overlap, the five exports, the logcat, the
   absent system log, the gap between the films), video review method (incl. privacy), an Event Timeline (phone time, action, actor, step ID, S-ID, registry
   Test-ID, evidence with its file), a step-mapping table, the analysis checklist, and the skeleton as an unchanged appendix. **Traceability (`AGENTS.md` §13.7):**
   every skeleton step and every Test-ID it names appears in the timeline or is explicitly "skipped" / "not identifiable"; every repetition and every extra action
   is its own row.

### Phase B — the HCI logs and the other logs, in full

9. Follow `AGENTS.md` §13, for **all four** HCI logs. Establish the overlap first (§2) and decide, per packet range, which file is the source. Pre-filter by the
   Buds' handle(s), take the full RFCOMM inventory (`SABM`/`UA`/`DISC`/`DM`, direction, per ACL session and per multiplexer side), then decode **every** Buds packet:
   MAESTRO on DLCI 2 **and** 3 (every `GetSoftwareInfo` with its channel and entries, `ReadSetting`/`WriteSetting`/`SubscribeRuntimeInfo`/`SERVER_STREAM` with
   field, value — zigzag for 17 — and status; CRC-32 per frame); the Message Stream per claim (app vs anything else; `08 11`/`08 13`/`08 12`, ACK/NAK with reason,
   battery, Find with directions, SASS, phone-side `DISC`s); DLCI 0x08/0x0a (who opens them, every `04 03`/`04 05`/`04 16`/`0e 01`); HFP; AVRCP; every ACL and LE
   event with its reason code; the Bluetooth off/on and the pairing after "Use different Buds" (CDM, bonding events, SSP/CTKD — no Account Linking analysis,
   ADR-008). Every negative with its command, exit status and a positive control.
10. Read the five exports and the app logcat **line by line** and put each relevant line on the timeline (phone time after the measured offsets; the file named).
    For every filmed action write the exact frames and log lines: what the app sent, on which channel, what the Buds answered, what the app logged and showed. Where
    no log covers a stretch (the HCI boundaries, between the films, no system log), say which evidence does, or that none does.
11. Build the tables the findings need: the Definition-of-done table (criterion → frames/lines → verdict); every Message Stream claim with its first phone message;
    every ANC claim against "Refuted if"; section II (a tap on the current mode); section III (screen, exports, wire per off/on); section IV (each S-step: film text
    vs the expected literal text, wire); section V (each never-run step); section VI (every Ring frame by direction); section VII (the failed connect); the lid-open /
    auto-connect table; the session-end table; every battery value shown vs the wire; the DLCI 0x08 `04 05`/`04 03` samples vs the wear state.

### Phase C — findings and the release verdict

12. For every lead of §3: the answer with evidence (frame numbers with their file, film times, log lines, `file:line` in the app sources), labelled 🟢 FACT /
    🟡 HYPOTHESIS / ⚪ ASSUMPTION / 🔴 OPEN QUESTION, with the experiment that would settle any non-FACT.
13. Write **CAP-068-FINDINGS.md** in the `CAP-066-FINDINGS.md` layout, every conclusion labelled (`AGENTS.md` §15, `PROJECT_RULES.md` §1), every hex decoding with
    its command and raw bytes (rule 4a), a one-line status banner at most (rule 9a). Include: re-test verdicts (the skeleton's "Refuted if" lists applied
    literally), what works, what does not, what goes wrong, per protocol (MAESTRO pw_rpc, Message Stream, SASS, DLCI 0x08/0x0a, HFP, AVRCP/A2DP, LE, pairing,
    Android's own link state), and what is different in a secondary user. If a finding contradicts an existing 🟢 FACT or ADR, say so and draft the correction as a
    **proposal**; do not edit that FACT or ADR.
14. **The release verdict for 1.0.1** (`RELEASING.md`, Release checklist C4): classify every defect as **heavy** (a crash, a wrong or unacknowledged write, Safe
    Mode failing on the verified firmware, a session lost without recovery, a Definition-of-done criterion not met, anything risky for the Buds) or **minor** (a
    known issue for the release notes), with the evidence. Give one of: *release the tested build as 1.0.1* (with any known issues worded for
    `dist/1.0.1/release-notes.md` and `CHANGELOG.md`), *fix first* (what, and the targeted re-test — the same version may be rebuilt while nothing is published),
    or *re-run*.
15. **Improvements (proposals only, no app change):** for every defect: the cause, the proposed change (file and function, the behaviour, the guardrails — ADR-044's
    one attempt per event, foreground only, no new permission or service, no polling timer, no new message type), the unit test that would have caught it (real-byte
    fixtures per `AGENTS.md` §11 — from this capture), and the hardware re-test step with its expected HCI bracket. Anything that changes an ADR's decision is a
    drafted ADR (no number).
16. **Do not modify any file under `android/`, `dist/` or `scripts/`.**

### Phase D — checkpoint, documentation, finish

17. **Checkpoint — stop and ask, in this chat, via `AskUserQuestion`.** One question per decision; each option with pros and cons, one marked "(Recommended)", the
    exact draft text of any FACT, ADR or `PROJECT.md`/`RELEASING.md` change in the preview (memory: "Approvals: confirm in chat"). Cover at least: the release
    verdict (task 14); **replacing "maintainer-attested, not captured" for Definition-of-done criterion 2** in `PROJECT.md` and the 1.0.0 exception note in
    `RELEASING.md` with this capture's frames (the exact new text, only if the evidence holds); the known-issues wording, if any; the `ANC-006` result and whether
    the app should skip a `Set` for the mode already shown; the Ring status-sync (`PROTOCOL.md` §4.4, FACT proposal) and whether the app should read it (a new ADR —
    `TODO.md` §4); the L68-5 samples; any other FACT or ADR change; the two ADR questions of `TODO.md` §1 (device choice, the "current value" rule) if the capture
    bears on them. **No new capture skeleton is written in this session.** Record the answers verbatim in the RESULT.
18. Apply only what was approved, and only to documentation: `PROJECT.md`, `RELEASING.md` (the exception note; **not** the Release log row — that follows
    publication, checklist E2), `PROTOCOL.md`/`DECISIONS.md` (approved changes only, dated Updates or new ADRs with a process note citing this chat, numbers
    registered in `id_registry.csv`), `ARCHITECTURE.md` (only where a hardware result corrects it, e.g. §5a hardware-verified marks), `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
    (Group BD run note, Capture Index row), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the evidence cells of the Test-IDs exercised — pointers only), `APP_TESTPLAN.md` (the
    Summary for this run; a step that proved unworkable), `DESKRESEARCH_FINDINGS.md` (only if the L68-5 table grows — a dated addition to the 2026-10-03 entry),
    `id_registry.csv` (`CAP-068` → analyzed with the real times), `TODO.md` (done items removed, open items added — §1 and §2 in particular), `CHANGELOG.md` (the
    `[1.0.1]` block's "Known limits" if the verdict adds any; **not** its date), `ai-sessions/INDEX.md` (the 0070 row), `README.md` (status, capture counts — not
    "Latest release", which changes only after publication). Run `python3 scripts/ensure_footers.py` and `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py`;
    it must exit 0 (its checks include table cell counts and status values since `0069`) — report anything else.
19. Finish the RESULT: plain-language answers first (the release verdict; the Definition of done; what works and what does not on 1.0.1 — each `A68-APP-*` fix on
    hardware —; `ANC-006`; `FIND-005`; what differs in a secondary user; what the app should change next); then the step-mapping results, the tables of Phase B,
    the external sources (URL plus quoted sentence), and, if the verdict is "release", the maintainer's next steps from `RELEASING.md`'s Release checklist D1–E4
    (merge PR #1 with a **merge commit**, tag **the build commit read from the Info tab**, the files from the kept copy of `dist/1.0.1/`) — **as instructions, never
    executed**. It must end with **"Deferred documentation"** (each item also added to `TODO.md`) and **"Commits"** (hashes, or "not committed — reason"), per
    `AI_SESSION_LOG_PROCEDURE.md` §9 and §4b. Set the Status per `AI_SESSION_LOG_PROCEDURE.md` §4.
20. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes: on branch `maintenance/0069`, Conventional Commits, one commit per
    concern (capture/LFS, docs), each with a *why* and ending with the attribution line from the session's system reminder; `git fetch` and
    `git log origin/maintenance/0069..HEAD` before pushing (rebase, never force); `git check-attr filter` on every capture file (`lfs`); **no file under `android/`
    in any commit** (the tested APK must stay the build commit's — check `git diff <build commit>..HEAD -- android` is empty); nothing from a build directory,
    `dist/`, `android/.kotlin/`, `.vscode/` or `__pycache__` staged (`scripts/__pycache__/lint_docs.cpython-314.pyc` is tracked — run the scripts with
    `PYTHONDONTWRITEBYTECODE=1`, and `git checkout` that file if it changed). A large LFS push (the films are ≈ 1.5 GB together) can drop the SSH connection after
    the upload — check `git log origin/maintenance/0069 -1` and push again if needed.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **No app changes and no release actions.** Nothing under `android/`, `dist/` or `scripts/` is modified; no build is made; `scripts/release.sh` is not run; the
  pull request is not merged; no tag is created or pushed, no GitHub release created or edited, no asset uploaded, no repository setting changed. Publishing is the
  maintainer's own act (`RELEASING.md`).
- **Approvals only in this chat.** Never promote, demote or correct a 🟢 FACT, never write or update an ADR, and never change the Definition of done evidence,
  without the maintainer's approval given in this chat (`AGENTS.md` §6). Model agreement is not approval.
- **Evidence.** Zero creativity with hex (`AGENTS.md` §13.6). Every claim needs a frame number (with its HCI file), log line (with its file), `file:line` or film
  timestamp (with its film) (`PROJECT_RULES.md` rule 4a). A negative needs its command, exit status and a positive control (`AGENTS.md` §13 step 8). What the
  maintainer heard inside the buds is their observation unless the wire shows the command that caused it.
- **What was done, not what was planned.** The skeleton is the plan; the films and the logs are the record. Skipped, repeated and extra steps are findings about
  the run, not errors to smooth over. Never fill a timeline row from the skeleton's "Expected" column, and nothing in the gap between the films from the plan.
- **Privacy.** No full MAC address or serial in any committed text beyond what ADR-010 allows; no personal data from the films or the logs.
- **Scope.** Stay within `PROJECT.md`. Anything new (a feature, a permission, a dependency, a background service, a new wire request — e.g. acknowledging the
  ring-status message) is a checkpoint question with a drafted ADR, not a silent addition. Fast Pair Account Linking / ownership at re-pairing is out of scope
  (ADR-008).
- **Subagents.** A subagent may only read and report. Every write is done in the main session and verified; re-derive any number a subagent reports before it goes
  into a file.
- **Files.** Before deleting, moving or renaming any file, diff a fresh listing of the source against the copy (checksums). Never `rm -rf` from memory of an earlier
  listing; ask before any delete. The doc tools skip `dist/`; do not run any tool that writes into `dist/`.
- **Commits.** Commit and push only after the maintainer confirms the final summary (task 20).
- **No new capture skeleton.** Do not create a new `CAP-NNN` folder, skeleton or `planned` registry row in this session; open steps go to the findings and `TODO.md`
  (`CAP-069`, Group BE, is already planned for the official-app questions).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0070_CAPTURE_PROMPT_2026_10_04.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0070_CAPTURE_PROMPT_2026_10_04
