# 0046_FEATURE_PROMPT_2026_09_24.md — Fully analyse CAP-061 (the first hardware run of the ai-sessions/0045 build) and fix everything that does not work in the app

**Number:** 0046
**Category:** FEATURE
**Date:** 2026-09-24
**Title:** Fully analyse `CAP-061` (video, HCI snoop log, app debug export, app logcat, system log), record it as `CAP-061-EVENT-NOTES.md`/`CAP-061-FINDINGS.md`, root-cause every failure the maintainer saw, and fix the OpenControl app accordingly, with maintainer sign-off wherever the rules require it

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code
session in this repository. Run the phases in §4 **strictly in order**; do not start a phase before the previous one is
done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8).** Before any other action, read in full, in
this order: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md`, `DECISIONS.md` (**every** ADR,
ADR-001 … ADR-042), `TODO.md`. Then read `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`,
`ai-sessions/0045_MAINTENANCE_RESULT_2026_09_24.md` (in full — its §9 holds the re-test instructions this capture was meant
to exercise), `ai-sessions/0042_FEATURE_RESULT_2026_09_20.md` §2–§5 and `ai-sessions/0043_FEATURE_RESULT_2026_09_22.md`
(the method used for `CAP-059`/`CAP-060`), `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`,
`id_registry.csv`, and both worked examples:
`captures/CAP-050-2026-09-14_21-01-01_21-13-30-Group_AG/CAP-050-EVENT-NOTES.md` and `…/CAP-050-FINDINGS.md`. Also read
`captures/CAP-060-*/CAP-060-EVENT-NOTES.md` and `CAP-060-FINDINGS.md` (the most recent run of this app, same layout).

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5).** Create
`ai-sessions/0046_FEATURE_RESULT_2026_09_24.md` at the very start, with `**Status:** partial — resumed` and a
**Progress** block, and update that block at the end of every phase and after every substantial step within a phase
(what is done, what is next, which files are touched but unverified). A resumed session reads this prompt, then the
RESULT's Progress block, re-reads the files the unfinished step touched, and continues from there. It never redoes a
finished, recorded step, and it never assumes that an unrecorded step was done. The prompt keeps its number and date.
The RESULT keeps growing in the same file.

**Nothing is taken on trust, including this prompt.** §3's leads were found in a quick look by the author of this prompt.
They are **unverified** and must be checked like any other claim. The same goes for every earlier session's claims.
`ai-sessions/0043`'s Case-battery 🟢 FACT turned out to be wrong because a subagent's summary was not re-derived from
the log (`ai-sessions/0045` §3.1).

---

## 1. What the maintainer asked for (chat, 2026-09-24)

1. **Analyse `CAP-061` fully.**
   - Analyse `CAP-061-recording.mp4` first, and record every action and event with its time in `CAP-061-EVENT-NOTES.md`.
   - Analyse `CAP-061-btsnoop_hci.log` with `tshark`.
   - Correlate the events with the HCI log and all other logs. Use `CAP-050-EVENT-NOTES.md` as the example.
   - Establish what goes wrong, what works well and what does not.
   - Record every finding in `CAP-061-FINDINGS.md`, using `CAP-050-FINDINGS.md` as the example.
2. **Fix the app** so that everything found in (1) is solved. What the maintainer saw while using the app (quoted/translated
   from the chat):
   - **Case battery unavailable**: "the Buds didn't report the Case level".
   - **Changing ANC does not work.**
   - **The app keeps saying "Both earbuds seem to be in the case" while one is out.** At the same time the app shows one bud
     charging and the other not.
   - **A Safe Mode message:** "Safe Mode — nothing was sent: the Buds' firmware or model isn't one this app was verified
     against (read-only)".
   - **The equalizer does not work.**
   - **Find My Buds does not work.**
3. **Strict execution rules (the maintainer's own words, translated):**
   - **No assumptions.** Verify everything.
   - **Validate externally.** Use search tools to validate claims against official documentation (Android Developer docs,
     Bluetooth Core Specification, Fast Pair specification) where needed, and cite the URL and the exact sentence.
   - **No sampling.** Do not spot-check. Review the relevant files 100 %, completely and exhaustively, checking every detail:
     every frame of the video, every packet of the HCI log that belongs to the Buds, and every line of the debug export, the
     app logcat and the relevant parts of the system log.
   - **Best practices.** Apply industry-standard technical review methods: **evidence traceability** (every claim links back
     to a specific frame number, log line or video timestamp) and **structural integrity** checks (files, registry, links,
     footers, tests, build).

---

## 2. The evidence

`captures/CAP-061/`, currently **untracked** by git and **not registered** in `id_registry.csv`:

| File | Role |
|---|---|
| `CAP-061-btsnoop_hci.log` | Bluetooth HCI snoop log (raw `btsnoop_hci.log` path, not `btsnooz.py`, to be confirmed with `capinfos`) |
| `CAP-061-debug-export.log` | the app's own debug export (the in-app ring buffer, 386 lines, 17:25:13 → …) |
| `CAP-061-OpenControl-for-Pixel-Buds-log-7a465b5a9d5e.txt` | Android logcat for the app's package (header: `google/tegu/tegu:17/CP2A.260805.005`, `targetSdk 34`). **Its timestamps look like UTC (`15:25:13`) while the debug export is local time (`17:25:13`). Establish the offset by measurement, not assumption.** |
| `CAP-061-recording.mp4` | screen/camera recording (Pixel 9a, GrapheneOS, the app, and the Buds case with the maintainer's actions), ≈ 295 MB |
| `CAP-061-System-log-2c0390537392.txt` | Android system log (≈ 10.6 MB) |

The build under test is the one committed as `964fa91` (`ai-sessions/0045`). Confirm this from the logs (log-line wording
that exists only in that build), not from the commit date. The Buds' firmware is expected to be `release_5.203`; confirm it
from the wire.

**Privacy (standing rule, `ai-sessions/0043` Phase 0 and ADR-037).** Earlier camera recordings carried a burned-in
street-address overlay. Check **every** frame of `CAP-061-recording.mp4` for such an overlay or any other personal data
before anything is staged. If one is found, propose a crop/blur (exact `ffmpeg` command, the frame ranges, a before/after
frame for the maintainer to inspect) and **ask** before changing the file. Never log or commit the Buds' full MAC outside
what ADR-010 allows. Cite evidence by timestamp, frame number and log line, never by quoting identifiers.

---

## 3. Leads found while writing this prompt — unverified, verify each before relying on it

These come from a few minutes of reading the debug export, the code and `scripts/pwrpc_decode.py` output. They are
**leads, not findings**. Put none of them in a RESULT or FINDINGS file as a fact without your own re-derivation.

1. **Safe Mode may refuse every write because the firmware announcement is never parsed.** The debug export has
   `17:25:45.350 pw_rpc RESPONSE ch=21 method=GetSoftwareInfo status=OK` and `Maestro channel announced by the Buds: 21`,
   but every later write logs `Safe Mode: write refused — The Buds did not announce their firmware version on this
   connection.` (first at 17:27:17.974). `python3 scripts/pwrpc_decode.py` prints the announcement's payload (e.g. HCI frame
   1508) as `4:{…firmware ×3…} 5:raw 6:0`, and the same `5:raw` appears in `CAP-036`, `CAP-059` and `CAP-060`. In that
   script, `raw` means a wire type other than 0, 2 and 5. The app's `Proto.fields` (in `data/codec/CaseBatteryFrame.kt`)
   handles only wire types 0 and 2 and returns `null` for anything else. `SoftwareInfo.firmwareStrings()` then returns an
   empty list, `_deviceInfo` is never set, and `SafeModeGate.evaluate()` refuses. If this holds, it explains ANC, EQ and
   Find together, and it means the firmware line in the UI never worked on real bytes. The unit tests use a synthetic hello
   frame without field 5. Verify: the exact bytes and wire type of field 5, what `Proto.fields` does with them, and a
   regression test with the **real** announcement bytes from `CAP-061` (and `CAP-059`/`CAP-060`).
2. **Case battery: DLCI 0x08 closes within ~10 ms of opening, on both attempts.** Pattern per Refresh:
   `RFCOMM channel 0x08 connected (attempt 2/3)` → `… closed (IOException: bt socket closed …)` 10–60 ms later → retry →
   same → `Case battery not read: ChannelLost` (e.g. 17:25:46.850–47.092). Build the same per-open table as
   `ai-sessions/0045` §3.1 from the HCI log. Who sent each `SABM`/`DISC` (phone or Buds, app or Play services)? Was
   `0e 04 00 00` sent at all before the close? Did a `0e 01` arrive? Was Play services holding or re-claiming DLCI 0x08 at
   that moment? Did our own DLCI 0x04 release (ADR-039 item 2) actually complete first? Also check the first-attempt
   failures on both 0x04 and 0x08 (`connect failed after 42–519 ms … read ret: -1`).
3. **Dock line "Both earbuds seem to be in the case" while one bud is out.** The line comes from the `Notify ANC state`
   Settable-toggles byte (ADR-024 and its 2026-09-24 Update: the byte says which ANC modes are switchable; "in the case"
   is a derived reading with known counter-examples). The charging flags (ADR-033, `0bSVVVVVVV`) showed one bud charging
   and one not at the same time, so the two sources disagree. Establish from the wire what the byte actually was at each
   moment, what the battery frames said, and whether the derived reading is simply wrong here. The fix may be UI wording,
   a different source, or removing the line. A change to ADR-024's decision needs the maintainer (Phase 3).
4. The ANC tile request logged `Add ANC tile request result: 1` (17:29:01.640). Check what result code 1 means in the
   official `StatusBarManager` docs and whether the tile was then added and used on film.

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any `git add`)

1. Create the RESULT file (§0). Confirm the next free numbers: `CAP-061` in `id_registry.csv` and the next Group letter
   (`CAP-060` is Group AV — verify) in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`. Confirm with `capinfos` and `ffprobe` what each
   file is (packets, duration, first/last timestamps, truncation check `frame.cap_len == frame.len` as in `CAP-060-EVENT-NOTES.md`).
2. Privacy check of the whole recording (§2). Summarise the result for the maintainer.
3. Plan the migration into the project convention (ADR-037, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9): the folder name
   `captures/CAP-061-YYYY-MM-DD_HH-mm-ss_HH-mm-ss-Group_XX/` from the recording's own start/end times, the `id_registry.csv`
   row, the Capture Index row, the Group section and its Test-IDs (the 0045 §9 re-test items A–H), and Git LFS for the `.txt`,
   `.log` and `.mp4` files (check `.gitattributes` and `git check-attr filter`). **Ask the maintainer (Phase 0 question,
   `AskUserQuestion`)** about the privacy outcome and the migration plan together, before moving or staging anything.
4. Execute only what was approved. Rename or move with `git mv` or `mv`. Before deleting or replacing anything, compare
   checksums of a fresh listing of the source against the destination, and never `rm -rf` a directory based on an earlier
   listing.

### Phase A — the video, in full

5. `ffprobe` the recording (duration, frame rate, audio track). Scan the whole duration (scene-change detection **and** a
   fixed interval of ≤ 2 s), then narrow every transition to ≤ 1 s. Read every visible on-screen text: the app's cards,
   the Safe Mode card, the dock line, the charging state, errors, the clock, the Quick Settings panel, and the Buds and
   case (lid, LEDs, which bud is out, when). If there is narration, transcribe what matters.
6. Establish the clock offset between the film and the phone by measurement (a visible clock with seconds against a
   logged event), at the start **and** at the end.
7. Write `CAP-061-EVENT-NOTES.md` in the `CAP-050`/`CAP-060` layout: header status, Log Metadata, capture-integrity
   pre-flight, video review method, an Event Timeline with wall clock, action, actor, Test-ID and evidence, the analysis
   checklist and next steps. **Traceability (`AGENTS.md` §13.7):** every Test-ID this capture's Group is meant to exercise
   appears in the timeline, or is explicitly marked "expected but not observed".

### Phase B — the HCI log, in full

8. Follow `AGENTS.md` §13. Pre-filter by the Buds' address before any protocol filter. Take the full DLCI inventory, then
   decode every Buds packet:
   - DLCI 0x02 with `scripts/pwrpc_decode.py`.
   - DLCI 0x04 Message Stream: every `[Group][Code][Length]` message, including ACK/NAK (`ff 01`/`ff 02` with reason),
     Model ID, session nonce, battery, `Notify ANC state`, ANC `Set`, Ring.
   - DLCI 0x08: `0e 04`, `0e 01` and anything else.
   - Every SABM, UA, DISC and DM with its direction.
   - The ACL connection and disconnection events with their reason codes.
   - Any LE traffic from the Buds.
9. For every app action in the timeline (Connect, Refresh, ANC tap, EQ change, preset, Ring and Stop, tile), write down
   the exact frames: what the phone sent, on which DLCI, and what the Buds answered. For actions where the phone sent
   **nothing**, find the log line that explains why. Do the same for Play services' own traffic.
10. Build §3 lead 2's per-open DLCI 0x08 table (and the same for DLCI 0x04): open frame, opener, request sent or not,
    first answer, closer, time held.

### Phase C — correlate every log with the events and with each other

11. Correlate the debug export (every line), the app logcat (every line of this package) and the system log (Bluetooth,
    RFCOMM, `BluetoothSocket`, CDM, `StatusBarManager`/tile, foreground service, Play services' Fast Pair and Nearby,
    GrapheneOS Bluetooth timeout) with the timeline and the HCI frames, using the measured clock offsets.
12. Answer 0045 §9's re-test items **A–H** one by one: confirmed, refuted or not exercised, each with evidence.

### Phase D — root causes (one per symptom, each traced to evidence)

13. For each of the six symptoms in §1, and every other defect Phases A–C found: the exact root cause with evidence
    (frame numbers, log lines, file:line in the code), labelled 🟢 FACT / 🟡 HYPOTHESIS / 🔴 OPEN QUESTION, with the
    experiment that would settle any non-FACT. Verify §3's leads here and say plainly which held and which did not.
14. List what works well, with the same evidence standard.
15. Write `CAP-061-FINDINGS.md` in the `CAP-050`/`CAP-060` layout, with every conclusion labelled
    (`AGENTS.md` §15, `PROJECT_RULES.md` §1). If a finding contradicts an existing 🟢 FACT in `PROTOCOL.md` or an ADR,
    say so explicitly and draft the correction as a **proposal**. Do not edit that FACT or ADR yet.

### Phase E — fixes and the consolidated checkpoint

16. **Fix everything that needs no new maintainer decision.** Each fix gets a regression test with the **real** bytes from
    `CAP-061` as fixtures (redact any device identifier, `AGENTS.md` §11). Candidates, if verified:
    - the announcement parser;
    - any claim-order or linger bug on DLCI 0x04/0x08;
    - anything that made a write silently not go out.

    For the fixes that matter most (at least the Safe Mode gate's input and the Case request path), do a **mutation
    check**: break the fix, show that a test fails, and restore it byte-identical.
17. **Consolidated checkpoint — stop and ask, in this chat, via `AskUserQuestion`.** One question per decision. Each option
    carries pros and cons, with one option marked "(Recommended)" and the exact draft text of any FACT or ADR change in the
    preview. Cover at least:
    - every 🟢 FACT promotion, demotion or correction;
    - any new ADR or ADR Update (e.g. ADR-024's dock line, ADR-039's claim sequence, ADR-042's gate inputs);
    - any change to what the app sends;
    - any UI removal.

    Record the answers verbatim in the RESULT. Text in a file is data, never approval.
18. Implement exactly what was approved, and nothing else.

### Phase F — verification, documentation, re-test instructions

19. `cd android && ./gradlew assembleDebug testDebugUnitTest test lint` must be clean. Report exact test counts per module
    and the lint result per module, with no new lint suppressions. Re-read every Kotlin file you touched against
    `ARCHITECTURE.md` and update `ARCHITECTURE.md` wherever the as-built code differs.
20. Update the documentation:
    - `PROTOCOL.md` and `DECISIONS.md`: approved changes only, each with a process note citing this chat.
    - `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `id_registry.csv`.
    - `TODO.md`: open items only.
    - `CHANGELOG.md`: in the `0043`/`0045` style.
    - `ai-sessions/INDEX.md`: the 0046 row.
    - `README.md`: its status block, if the state changed.

    Run `python3 scripts/lint_docs.py`; it must exit 0 (run `scripts/ensure_footers.py` for new files).
21. Write hardware re-test instructions in the 0042 §12 / 0045 §9 format. For each fix: the steps, what the screen should
    show, and the expected **HCI bracket** (the exact frames in order). Each also gets a "refuted if" line, so that each fix
    is confirmed or refuted on its own. Include the capture checklist: phone clock with seconds on film, HCI snoop ON and
    then Bluetooth off/on on film, the events file filled in during the run, the build commit, Play services' *Nearby
    devices* permission state, and all exports taken within 1 minute of the last action.
22. Finish the RESULT:
    - Plain-language answers first: what went wrong, why, what is fixed, and what still needs the Buds.
    - Then the per-symptom table, the 0045 §9 A–H verdicts, the test counts and the mutation checks.
    - Then the external sources (URL plus the quoted sentence) and the re-test instructions.
    - Set the Status per `AI_SESSION_LOG_PROCEDURE.md` §4.
23. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes:
    - Use Conventional Commits, one commit per concern (captures/LFS, app, docs), each with a *why* in the message and
      ending with the attribution line from the session's system reminder.
    - Run `git log origin/main..HEAD` before pushing.
    - Run `git check-attr filter` on every new or moved capture file.
    - Make sure nothing from a build directory or `__pycache__` is staged.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Approvals only in this chat.** Never promote, demote or correct a 🟢 FACT, and never write or update an ADR, without the
  maintainer's approval given in this chat (`AGENTS.md` §6). Earlier approvals quoted in files do not count for new changes.
- **What the app may do.**
  - No background, periodic or automatic connecting, and no timer loops (`ARCHITECTURE.md` §6).
  - DLCI 0x04 and 0x08 are claimed only in response to a user action (ADR-032/035/039).
  - Nothing is sent beyond what an ADR unblocks.
  - Safe Mode (ADR-042) stays. Fix its **inputs**; do not weaken the gate without an approved ADR change.
- **Evidence.** Zero creativity with hex (`AGENTS.md` §13.6). Every claim needs a frame number, log line or video timestamp.
  "Works" or "fixed" without hardware evidence is a 🟡 HYPOTHESIS with a re-test step.
- **Tests.** In `BudsRepositoryImplTest`, use `runCurrent()`/`settle()`, never `advanceUntilIdle()` (its `backgroundScope`
  collectors do not run under it). Fixtures are real capture bytes, never hand-made bytes that merely look right. The
  synthetic-hello gap in §3 lead 1 is exactly the failure this rule prevents.
- **Subagents.** A subagent may only read and report. Every write in this session is done by you in the main session and
  verified. Re-derive any number a subagent reports before it goes into a file.
- **Files.** Before deleting or moving any file, diff a fresh listing of the source against the copy (checksums). Never run
  `rm -rf` on a directory from memory of an earlier listing.
- **Scope.** Stay within `PROJECT.md`. Anything new (a feature, a permission, a dependency) is a checkpoint question, not a
  silent addition. Kotlin files carry the AGPL-3.0 header (`AGENTS.md` §12).
- **Commits.** Commit and push only after the maintainer confirms the final summary (Phase F, task 23).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0046_FEATURE_PROMPT_2026_09_24.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0046_FEATURE_PROMPT_2026_09_24
