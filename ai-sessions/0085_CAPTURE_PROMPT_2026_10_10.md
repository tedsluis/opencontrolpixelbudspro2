# 0085_CAPTURE_PROMPT_2026_10_10.md — Full analysis of CAP-073 (Group BI: the rebuilt 1.2.0 release APK, build 5b4d5db, installed over the 1.2.0 build ec6d163 in the GrapheneOS user without Google Play services — the re-test of the ADR-061 hold and the finer balance slider)

**Number:** 0085
**Category:** CAPTURE
**Date:** 2026-10-10
**Title:** Fully analyse `CAP-073`: **one** Android screen recording (no audio track), **two** HCI snoop logs, **one** app debug export, **one** app logcat,
**one** full system log (`logcat -b all`) and the P0/P1 shell outputs. It was recorded while the release-signed **1.2.0 build `ec6d163`** (of 2026-10-09) was
updated **in place** to the release-signed **1.2.0 build `5b4d5db`** (the rebuild of `ai-sessions/0084`: `DECISIONS.md` ADR-061 — the Message Stream claim held
while the noise-control tab is on screen — and the finer balance slider), on a Pixel 9a with GrapheneOS (Android 17), in the secondary user without Google
Play. **The maintainer did not follow the skeleton**: only the changed functionality was tested.
- Record the real events in **CAP-073-EVENT-NOTES.md** and the analysis in **CAP-073-FINDINGS.md**.
- Establish exactly what was done and what each changed function did on screen, on the wire and in the logs; give the release verdict for the rebuilt 1.2.0
  (`RELEASING.md`, Release checklist C4).
- **No change to the app itself**, **no new capture skeleton**, **no publishing step**.

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this repository. Run the
phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Take the time to read the core files first, in full and in this order, to
understand the ground rules before auditing anything:
- `AGENTS.md` and `PROJECT_RULES.md`.
- `PROJECT.md` (the Definition of done with its evidence table; the 1.2.0 status paragraph).
- `ARCHITECTURE.md`, in particular §2.4, §3.1 (the ANC row, "one rule for current"), §3.2 (timing constants: `MESSAGE_STREAM_LINGER_MS`), §5a (the
  "Changed by the Buds" row), §6.0a, **§6.0b (the ADR-061 hold paragraph)**, §7, §8.1, §9, §12 and §13.
- `PROTOCOL.md`, in particular §2.2a with all its Updates (the announced channel, lead L-1), §2.3, **§4.1** (ANC `Get`/`Set`/`Notify`, the Settable byte),
  §4.3 Option B/F, §4.5's preamble, §4.5.7 (balance) and §8 (the 2026-10-10 rows).
- `DECISIONS.md`: **every** ADR, ADR-001 … ADR-061, with every dated Update, and the **draft** ADR-060 (proposed, not accepted — not a decision). In particular
  ADR-010, ADR-032 (and its 2026-10-10 pointer Update), ADR-042, ADR-044, ADR-045, ADR-048, ADR-049, ADR-057, ADR-058, ADR-059 (and its 2026-10-10 Update) and
  **ADR-061**.
- `TODO.md`, §2 (the items `CAP-073` was to settle) and §5 (the release steps of the rebuilt 1.2.0).

These are the ground rules; do not audit or change anything before they are read. Then, per task:

- **Procedure and registers:** `AI_SESSION_LOG_PROCEDURE.md` (in full, incl. §4b and §9), `ai-sessions/INDEX.md`, `id_registry.csv` (the `CAP-071` … `CAP-073`
  rows, ADR-061, every Test-ID the skeleton names).
- **What the build under test is — read in full:**
  - `ai-sessions/0084_FEATURE_PROMPT_2026_10_10.md` and `ai-sessions/0084_FEATURE_RESULT_2026_10_10.md` — §A.1 (the evidence for ADR-061), §A.2 (the design),
    §B (the maintainer's checkpoint answers: when the hold is open, the re-open's snapshot kept, no re-claim after a close, the ANC (i) text, the balance slider,
    the worn (i) text), §E (the gate and the mutations), §F (the skeleton), "After the session — the build" (build `5b4d5db`, APK SHA-256
    `ac04415eabf4369a49e9f88230aa83fc858a7e3ea0223d725c14a43ac5a67220`, certificate `a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb`);
  - `CHANGELOG.md` `[1.2.0] - not yet released`, `README.md` (Status), `scripts/release_notes.template`;
  - `RELEASING.md`: the Release checklist A–E, "Repository rules", §4–§8, **§11a** (the two tiers) and §13;
  - `APP_TESTPLAN.md`: section **W** (W1–W12), V, M3 and the Summary;
  - pull request #27 (`gh pr view 27 --json state,headRefOid,body`) and `git log --oneline a19e5e9..origin/release/1.2.0-rebuild`.
- **The skeleton:** captures/CAP-073-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BI/CAP-073-EVENT-NOTES.md (the placeholder folder, renamed by this session), read **as committed** (`git show HEAD:<path>`): purposes I–III,
  the reference-frame table, P0–P9, BI-1 … BI-20 with "Refuted if", Don'ts, the analysis checklist. Check with `git status` that the working-tree copy is unchanged.
- **The previous run and the layout to follow — read in full:** `captures/CAP-072-2026-10-09_17-27-26_18-23-39-Group_BH/CAP-072-EVENT-NOTES.md` and
  `…/CAP-072-FINDINGS.md` (the "before": the press-and-hold gap of §5, the claim inventory of §1, the session ends of §7, the privacy method of §0); and the
  maintainer's named examples `captures/CAP-071-2026-10-08_17-51-29_18-27-28-Group_BG/CAP-071-EVENT-NOTES.md` and `…/CAP-071-FINDINGS.md` (clock offsets, the
  HCI prefix proof, the session-end table, the Play-services marker, the `bugreportz`/SIGQUIT explanation, the system-log extract, the release verdict).
- **Logging and capture method:** `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3, §5, §8, §9 and the Group BI section; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` for every Test-ID
  the skeleton names (`PAIR-003`, `BATT-004`, `FW-003`, `ANC-001` … `ANC-004`, `TOUCH-007`, `CASE-004`, `CASE-005`, `AUDIO-002`, `AUDIO-003`, `INEAR-006`,
  `INEAR-004` — re-derive the list from the skeleton).
- **Every Kotlin source under `android/` that a finding touches, in full,** before you write about it — at least the files of the rebuild
  (`git diff --stat a19e5e9..5b4d5db -- android`): `BudsRepositoryImpl.kt` (`reconcileHold`, `releaseOrHold`, `endHold`, `withMessageStream`,
  `scheduleRelease`, `causeOfNotify`), `BudsRepository.kt`, `OpenControlNavHost.kt`, `MainActivity.kt`, `AncScreen.kt`, `EqScreen.kt` (`BalanceSlider`,
  `balanceFromPosition`), `BudsSettings.kt`, `ConnectionScreen.kt`, `SessionReopener.kt`, `RfcommBudsTransport.kt`, `AncTileService.kt`.

Say in the RESULT which files you read in full and which only partly (`ai-sessions/0083` and `0084` each fell short of their lists — do not repeat that).

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.**
- Create **ai-sessions/0085_CAPTURE_RESULT_2026_10_10.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block.
- Update that block at the end of every phase **and** after every substantial step: every 2 minutes of the screen recording reviewed, every HCI log and channel
  decoded, every log file read, every 20,000 lines of the system log, every checkpoint answer. Say what is done and what is next, which files are touched but
  not yet verified, and where the intermediate results live (the scratchpad: scripts, contact sheets, decoded tables, notes — re-create them if it is gone).
- A resumed session reads this prompt, then the RESULT's Progress block, re-reads the files the unfinished step touched, and continues from there. It never
  redoes a finished, recorded step and never assumes an unrecorded one was done. The prompt keeps its number and date; the RESULT grows in the same file.

**Memory and the machine.** The laptop has 15 GB and `/tmp` is a 7.7 GB tmpfs.
- Run `tshark`/`ffmpeg` one job at a time; extract contact sheets and crops in ranges, never a whole film into memory. Read the 18 MB system log with
  `grep`/`sed`/`awk` in ranges.
- Never `git worktree add` a commit (it checks out `captures/`, gigabytes — `ai-sessions/0084` filled `/tmp` that way). No Gradle build is needed.

**Nothing is taken on trust, including this prompt, the skeleton, earlier RESULTs and the maintainer's statements.** Every claim and every number measured in §2
is re-derived from the recording, the HCI logs, the other logs, the code and official documentation.

---

## 1. What the maintainer asked for (chat, 2026-10-10, translated from Dutch)

1. "Make a new prompt for Claude Code … to carry out a comprehensive analysis of CAP-073 (Group BI) of the OpenControl app and record the findings. Take the
   time to read the core files first."
2. "Run everything in phases. All phases must be run sequentially. When you hit a token limit, you must be able to continue automatically later."
3. **Analysis:**
   - first analyse the screen recording CAP-073-screen-*.mp4 and record the events and actions with their time in CAP-073-EVENT-NOTES.md;
   - analyse all CAP-073-btsnoop_hci* logs with `tshark`;
   - analyse CAP-073-opencontrol-debug-20261010-*.txt, CAP-073-OpenControl-for-Pixel-Buds-Pro-2-log-*.txt, CAP-073-logcat-all.txt and CAP-073-adb-shell.log;
   - then correlate the events of CAP-073-EVENT-NOTES.md with the HCI logs, the system log, the debug export and the app logcat (`CAP-071-EVENT-NOTES.md` as the
     example);
   - carry out an extensive analysis and record the findings in CAP-073-FINDINGS.md (`CAP-071-FINDINGS.md` as the example).
4. **Strict rules:** no assumptions — verify everything; validate claims externally with search tools against official documentation (Android Developer Docs,
   the Bluetooth Core Specification, the Fast Pair specification) where needed; **no sampling** — a 100 % complete, exhaustive review of the relevant files,
   every detail checked; industry-standard review methodology: traceability of evidence (every claim links to a specific frame, log line or film time) and
   structural integrity.
5. **The maintainer's statements (verify each, label each as the maintainer's statement until verified):**
   - "I did not follow the test plan. I only tested the changed functionality."
   - "At the start of the video the OpenControl app was version 1.2.0 build `ec6d163` (from yesterday). During the video I did the upgrade to the new version
     1.2.0 build `5b4d5db`."
   - "The capture was made on a Pixel 9a with GrapheneOS, Android 17."
   - "I did not speak during the video."
   - "The ANC modes during playback were right! When I did a long press on a bud, the ANC modes in the app changed too!"

---

## 2. The evidence (measured 2026-10-10 by the session that wrote this prompt — re-derive every number)

The folder `captures/CAP-073-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BI/` holds the committed skeleton CAP-073-EVENT-NOTES.md and eight new, **untracked** files:

| File | Measured | Notes |
|---|---|---|
| CAP-073-screen-20261010-083324-1791613415361.mp4 | 80.1 MB, 589.3 s, H.264 1080×2424, ≈ 7.8 fps average (`103072500/13259647`); **no audio stream** (`ffprobe -show_entries stream=codec_type` lists video only) | the file name's 08:33:24 is presumably the end — derive the clock offset from the status bar and from shared events; mode 750 |
| CAP-073-btsnoop_hci.log | 7,064 packets, 2026-10-10 08:22:51.771 – 08:35:07.913 (`capinfos -c -a -e`) | the current log |
| **CAP-0731**-btsnoop_hci.log.last | 28,116 packets, 2026-10-09 18:32:14.701 – 2026-10-10 08:22:48.796 | **the name has a stray "1"** (presumably CAP-073-btsnoop_hci.log.last) — a rename needs the maintainer's yes (Phase 0). Mostly before the run; check what of the run (if anything) is in it and whether one log is a prefix of the other (`cmp -n`) |
| CAP-073-logcat-all.txt | 114,867 lines, 18 MB; first line "beginning of kernel", last 10-10 08:34:07.085 | the **full system log** — privacy as in `CAP-072-FINDINGS.md` §0 (an extract is committed, the full log stays local) |
| CAP-073-opencontrol-debug-20261010-083250.txt | 680 lines, 08:27:07.086 – 08:32:20.679 | one debug export, one process; mode 750 |
| CAP-073-OpenControl-for-Pixel-Buds-Pro-2-log-774c13a575c8.txt | 471 lines; header `package: io.github.tedsluis.opencontrolpixelbuds:10200`, `osVersion: google/tegu/tegu:17/CP3A.261005.005/…`, `userType: full.secondary` | the app logcat (UTC in `CAP-072`; check); mode 750 |
| CAP-073-adb-shell.log | 15 lines: `dumpsys` at 08:24:10 (versionCode 10200, `lastUpdateTime=2026-10-09 18:33:54`, `firstInstallTime` 2026-10-09 18:33:54 and 2026-10-09 17:24:41), the `pm list packages` P0 at 08:24:30 (`gmscompat` ×3, `exit=0`), `dumpsys` at 08:39:19 (`lastUpdateTime=2026-10-10 08:27:05`) | the update at 08:27:05; **a (re)install of `ec6d163` at 2026-10-09 18:33:54** — after `CAP-072` ended (18:23:39); find out what that was (the system log may not reach back; say so) |

Not present: a camera film (the skeleton's part II asked for one), `uiautomator` dumps (BI-14), a second debug export, the P0 positive control and
`am get-current-user` output — record each as "not made" and what that leaves unverified.

State of the repository at the time of writing: branch `release/1.2.0-rebuild`, local tip `34dbd7a` ("docs: record the rebuilt 1.2.0 build (5b4d5db) for
CAP-073"), **not pushed** — `origin/release/1.2.0-rebuild` is at `5b4d5db`, the build commit. PR #27 is open with A1–A6 and B1–B3 ticked. Re-check with
`git status`, `git log --oneline origin/release/1.2.0-rebuild..HEAD` and `gh pr view 27`.

---

## 3. Leads and context — verify each before relying on it

1. **The installed APK.** The maintainer's statement and the shell log say 1.2.0 → 1.2.0 (both versionCode 10200). Verify from the screen recording's Info tab
   (the build hash before and after, no "-dirty"), the system log (`PACKAGE_REPLACED`/`installPackageLI`, dex2oat `app-version-name`, the process kill
   `killDueToPackageUpdate`, the new PID) and, if the maintainer can attach the phone, `adb shell pm path` + `adb pull` + `sha256sum` against
   `~/opencontrol-1.2.0-tested` (`CAP-071` did this; `CAP-072` could not — an open item there). Without the pull, say "not proved".
2. **ADR-061 — the hold.** For every DLCI 0x04/0x05 `SABM`, `UA`, `DISC` and UIH of the run (both directions), build the claim inventory (`CAP-072-FINDINGS.md` §1's
   method) and set each against the tab shown on the screen recording and the app's log lines "Message Stream hold started …" / "Message Stream hold ended: …".
   Check, exhaustively: a claim opened on entering the noise-control tab (one `SABM` + `08 11 00 00`); **no `DISC` while the tab is shown** (the old build:
   `DISC` ≈ 1.5 s after every action); the `DISC` ≈ 1.5 s after leaving the tab or the app (`MESSAGE_STREAM_LINGER_MS`); a tap, a pull and the tile on the
   held channel without a second `SABM`; a re-open's snapshot claim kept while the tab is shown; nothing re-claimed by itself after a Buds-side close. **Before
   the update (`ec6d163`)** the old behaviour must show — the comparison inside one run.
3. **"Changed by the Buds" — the maintainer's observation** ("a long press on a bud changed the mode in the app too"). For every unprovoked `08 13 00 04 01 …`
   (no `08 11` in the 2 s before it): its time, mode and Settable byte; the mode list read at Connect (`ReadSetting 4:12`, the cycle it allows); what the screen
   showed and when (the mode buttons, the (i) line "Changed by the Buds at HH:MM:SS", its time against the frame's); whether any hold fell outside a claim
   (and what the app then showed). The holds are not on film (no camera): derive them only from the wire and the screen, and label what is inferred.
4. **The balance slider** (if used): the live label "… — release to set" during a drag (screen recording), one `WriteSetting 4:{17:…}` per release and none during
   a drag, the zigzag values against the labels, how many attempts a target took (`CAP-072`: 30 writes for "Right 4").
5. **The texts of the rebuild:** the ANC (i)'s last line (ADR-061 sentence), the worn (i) ("… 40 seconds or more …", no capture id) — on screen if opened.
6. **Everything else the run touched** (sessions, the thirteen requests per Connect, `GetHardwareInfo` once per session, writes, ANC `Set`s, session ends and
   re-opens, Bluetooth off/on, the export) against `CAP-072`'s references — the rebuild changed no request; any difference is a finding.
7. **The "during playback" remark:** music playing (AVDTP streaming, AVRCP) and its effect on the claims and on the modes.
8. **Play services:** 0 claims expected in this user (`frame contains 03:08:00:02:01:25`, positive control `CAP-066`).
9. **Privacy:** the screen recording may show the Wi-Fi name, phone numbers, notifications, other device names, the serial numbers (allowed on the Info tab,
   never written unredacted in any text — first 4 + last 2, ADR-058 item 5). The system log carries the account e-mail and the SSID (`CAP-072` §0's extract
   command removes them).

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename, mode change or `git add`)

1. Create the RESULT (§0). Record `git log -1`, `git status --short`, `git fetch`, `git log --oneline HEAD..origin/main`,
   `git log --oneline origin/release/1.2.0-rebuild..HEAD`. Work on `release/1.2.0-rebuild` (the capture and its analysis go on the release branch after the build
   commit, `RELEASING.md` C3). `android/domain/bin/` is build output: never stage it.
2. List the folder fresh (`ls -la`, `sha256sum`), diff it against §2's table and say every difference.
3. **Checkpoint 0 (`AskUserQuestion`, one question per decision, options with pros and cons, "(Recommended)" on one, the exact commands in the preview):** the
   folder rename to the screen recording's first/last phone times (`CAP-073-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BI`), the rename of CAP-0731-btsnoop_hci.log.last,
   `chmod 644` on the files with mode 750, a `sha256sum` file; the privacy treatment of the screen recording (blur ranges, after Phase A finds them) and of the
   system log (the filtered extract as in `CAP-072-FINDINGS.md` §0, the full log kept off the repository); whether the phone can be attached for the APK pull.
   Update every reference to the placeholder folder outside `ai-sessions/` only after the yes.

### Phase A — the screen recording, in full

4. Decode the recording at 1 frame per second (589 frames) into the scratchpad in ranges; compare each second with the previous one; view **every** second with
   a change on labelled contact sheets; narrow transitions at 10 fps where a claim depends on them. No sampling: every second is either viewed or proved
   unchanged.
5. Measure the clock offset (status-bar minute changes, the update dialog, a tap with a matching log line) and write it with its uncertainty.
6. Write the timeline into CAP-073-EVENT-NOTES.md: phone time · what happened · step / W / Test-ID · evidence — in `CAP-071`/`CAP-072`'s layout (Log Metadata,
   the P0/P1 outputs copied, the capture-integrity pre-flight, the review method and privacy, the Event Timeline, the step mapping BI-1 … BI-20 as done /
   not done / done differently, "Not in the plan", Test-IDs per `AGENTS.md` §13 step 7 with "expected but not observed", the analysis checklist). Keep the
   committed skeleton as the appendix, unchanged (as `CAP-072-EVENT-NOTES.md` does).

### Phase B — the HCI logs and the other logs, in full

7. Both HCI logs: Connection Complete events and the Buds' handle (by address; `bluetooth.addr` may match nothing — show the filter matching a known frame),
   the prefix check between the two logs, the RFCOMM inventory per DLCI and direction, every pw_hdlc frame CRC-32-checked (`scripts/pwrpc_decode.py`'s
   `parse`/`describe`, with its positive control on `CAP-036`), every Message Stream message decoded (`[Group][Code][Len][Value]`). Commands and exit statuses
   in FINDINGS (`PROJECT_RULES.md` rule 4a; `AGENTS.md` §13 step 8 for every negative).
8. The debug export, the app logcat, the shell log: every line read (680 + 471 + 15). The system log: every line in the run's window read or filtered with a
   stated, positive-controlled command; crashes, ANRs, `StrictMode`, `am_wtf`, the install/update events, the Bluetooth stack's RFCOMM lines, the SIGQUITs.

### Phase C — correlation, findings and the release verdict

9. Correlate every timeline row with its frames and log lines. Write CAP-073-FINDINGS.md in `CAP-071`/`CAP-072`'s structure: §0 build, install, user, logs,
   privacy; §1 ACLs and the Message Stream claims (the inventory of §3 item 2); §2 the Connect read; §3 every write byte for byte; §4 "Changed by the Buds" and
   the holds (§3 item 3); §5 the balance slider; §6 session ends and re-opens; §7 the 1.2.0 `ec6d163` → `5b4d5db` comparison inside the run; §8 the skeleton's
   "Refuted if" applied literally per step that was run; §9 the verdict table (heavy / minor / not tested); §10 open questions. FACT / HYPOTHESIS / ASSUMPTION /
   OPEN QUESTION on every conclusion (`PROJECT_RULES.md` §1); every claim with a frame, a log line or a film time.
10. Validate externally where a claim rests on a specification or platform behaviour (Android developer documentation, the Bluetooth Core Specification for
    RFCOMM `DISC`/`UA`, the Fast Pair Hearable Controls page for `Notify ANC state`): fetch, quote with the date.
11. The release verdict (`RELEASING.md` C4): heavy issue ⇒ fix on the branch (a FEATURE session); minor ⇒ known issue; OK ⇒ ready to merge and publish. Say what
    the skipped steps (no camera, no dumps, …) leave unproved, and whether that blocks the release in your view — the decision is the maintainer's.

### Phase D — checkpoint, documentation, finish

12. **Checkpoint (`AskUserQuestion`):** the verdict; every proposed `PROTOCOL.md` note or status change and every ADR Update (drafts in the preview — `AGENTS.md`
    §6: nothing promoted or written without the maintainer's answer in this chat); what to do with the items not run (`TODO.md`); the privacy treatment.
    Record the answers verbatim in the RESULT.
13. With the answers: `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 (the `CAP-073` row: analyzed) and the Group BI status, `id_registry.csv`, `APP_TESTPLAN.md` §W results
    and the Summary, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the Group column for the Test-IDs exercised), `TODO.md` §2/§5, `PROJECT.md` and `README.md` status
    lines, `ARCHITECTURE.md` §5a ("hardware-verified" only with this capture's evidence), the PR #27 checklist (C1–C4) — **no `CHANGELOG.md` date, no tag, no
    release**. `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and `python3 scripts/lint_docs.py` (exit 0).
14. The RESULT: plain-language summary first (what the rebuilt 1.2.0 did, the verdict, what the maintainer does next), the phases, the files read in full / in
    part, "Deferred documentation" (each also in `TODO.md`), "Commits"; the `ai-sessions/INDEX.md` row; back-fill `ai-sessions/0084`'s Commits (`34dbd7a` and any
    later commit of that session) per `AI_SESSION_LOG_PROCEDURE.md` §4b item 2.
15. Show a short summary and **ask whether to commit and push**. Only after a yes: commits per concern (Conventional Commits: `docs(capture)`, `docs`), each with
    its *why* and the attribution line; Git LFS for the large files (`.gitattributes`); `git fetch` and rebase; push without force. Never merge, tag or release;
    the maintainer's next steps (D1–D4, E1–E4 of `RELEASING.md`) as instructions only.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Scope:** analysis and documentation only. No change under `android/`, no new skeleton, no publishing step, no `dist/` change.
- **Approvals only in this chat** (`AGENTS.md` §6): no 🟢 FACT promotion, no new ADR or ADR Update without the maintainer's answer here; ADR-060 stays a draft.
- **Evidence:** every claim cites a frame (log and number), a log line or a film time; a negative needs its command, its exit status and a positive control;
  a hypothesis names its settling experiment.
- **No sampling:** every second of the recording, every packet of both HCI logs, every line of the export, the app logcat and the shell log, and the system log
  in the run's window — reviewed or covered by a stated, positive-controlled filter.
- **Privacy:** no serial unredacted, no MAC, no phone number, no account e-mail, no Wi-Fi name in any committed text; the full system log stays local.
- **Files:** before moving, renaming or deleting anything, diff a fresh listing; never wildcard deletes; ask first.
- **Subagents** may only read and report; every write is done and verified in the main session.

## 6. Deliverables

- CAP-073-EVENT-NOTES.md (the real run, the skeleton as appendix) and CAP-073-FINDINGS.md, with the folder and files as approved in Checkpoint 0.
- The registry, index, test-plan, `TODO.md`, status and PR-checklist updates as approved; the verdict.
- ai-sessions/0085_CAPTURE_RESULT_2026_10_10.md and the `ai-sessions/INDEX.md` row.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0085_CAPTURE_PROMPT_2026_10_10.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0085_CAPTURE_PROMPT_2026_10_10
