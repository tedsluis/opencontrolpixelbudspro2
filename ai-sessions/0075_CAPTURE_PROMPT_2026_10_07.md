# 0075_CAPTURE_PROMPT_2026_10_07.md — Full analysis of CAP-070 (Group BF: the 1.1.0 release APK in a GrapheneOS profile without Google Play services — the hardware test of the five switches of 0074)

**Number:** 0075
**Category:** CAPTURE
**Date:** 2026-10-07
**Title:** Fully analyse `CAP-070` (**one** film, **four** HCI snoop logs, **four** app debug exports, **two** app logcats, **no** system log), recorded with the
release-signed **1.1.0** APK (build `0323849`) in a GrapheneOS secondary user without Google Play while following the Group BF skeleton in
`CAP-070-EVENT-NOTES.md`; record the real events in **CAP-070-EVENT-NOTES.md** and the analysis in **CAP-070-FINDINGS.md**; establish what was actually done;
give the release verdict for 1.1.0 (`RELEASING.md`, Release checklist step C4); **no change to the app itself**, **no new capture skeleton**, **no publishing step**

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this repository. Run the
phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action, take the time to read **in full**, in this order,
to understand the ground rules before auditing anything: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md` (incl. the Definition of done with its evidence table and
"Status after 1.0.x"), `ARCHITECTURE.md` (in particular §2.4, §3.1 with the "one rule for current" bullet and the DLCI 0x02 settings row, §3.2 timing constants,
§5a, §6.0a, §6.0b, §7, §8.1, §9, §12, §13), `PROTOCOL.md` (in particular §2.2a with all its Updates — the announced channel and lead L-1 —, §2.3, §4.1, §4.2, §4.3
Option B/F, §4.5 preamble, §4.5.2 Multipoint, §4.5.4 head gestures, §4.5.6 Volume EQ, §4.5.7 balance, §4.5.8 Case sounds, §5, §6), `DECISIONS.md` (**every** ADR,
ADR-001 … ADR-055, with every dated Update — in particular ADR-010, ADR-034, ADR-036, ADR-037, ADR-042, ADR-044, ADR-045 with its 2026-09-30 Update, ADR-046,
ADR-047, ADR-048, ADR-049, **ADR-052, ADR-053, ADR-054, ADR-055**), `TODO.md`. These are the ground rules; do not audit or change anything before they are read.
Then, per task:

- `AI_SESSION_LOG_PROCEDURE.md` (in full, incl. §4b), `ai-sessions/INDEX.md`, `id_registry.csv` (the `CAP-067` … `CAP-070` rows and every Test-ID the skeleton
  names, incl. the new `INEAR-005`).
- **What the build under test contains — read in full:** `ai-sessions/0074_FEATURE_PROMPT_2026_10_06.md` and `ai-sessions/0074_FEATURE_RESULT_2026_10_06.md` (the
  fixtures of §A.1 with their raw bytes, §A.4 read budget, §A.5 RUN pass, the checkpoint answers, §C–G per item, the gate and mutation tables, §I the run table,
  "Deferred documentation", "Commits"), `CHANGELOG.md` `[1.1.0] - not yet released`, `README.md`, `RELEASING.md` (the **Release checklist** A–E, §4–§8, §13),
  `APP_TESTPLAN.md` (in full — section T is the 1.1.0 build; M1, N1, O8 changed for it; H5, C10, C12, S6, S12 are taken into this run), the commits of branch
  `feature/0074-settings-switches` (`git log --stat fcd863e..origin/feature/0074-settings-switches`; pull request #7 is open, not merged).
- The **skeleton** `captures/CAP-070-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BF/` → `CAP-070-EVENT-NOTES.md` **as committed** (`git show HEAD:<path>` — purposes I–VI, the
  expected-frames table with its real/derived marks, P0–P7, BF-1 … BF-25, BF-end, "Don'ts", the analysis checklist and its "Refuted if" column) **and** the
  working-tree version (the maintainer added the P0 and P1 outputs after the run — record them as the maintainer's evidence, see §2).
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3, §5, §8, §9 and the Group BF section; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` for every Test-ID the skeleton names (`MULTI-001`,
  `HEAD-001`, `CASE-001`, `CASE-002`, `AUDIO-002`, `AUDIO-003`, `INEAR-002` … `INEAR-005`, `CASE-004`, `CASE-005`, `EQS-001`, `PAIR-003`, `BATT-004`).
- **The layout to follow — read in full:** `captures/CAP-049-2026-09-12_18-13-54_18-21-49-Group_AF/CAP-049-EVENT-NOTES.md` and `…/CAP-049-FINDINGS.md` (the
  maintainer's named examples), and — the same set-up as this run (a release build in the user without Play) — `captures/CAP-068-2026-10-04_07-22-57_07-54-10-Group_BD/CAP-068-EVENT-NOTES.md`
  and `…/CAP-068-FINDINGS.md` (clock-offset method, file-overlap proof, session-end table, the Play-services marker, the release verdict).
- The capture FINDINGS behind the five switches, for the expected Buds behaviour: `CAP-069-FINDINGS.md` §1–§2 (field 29 1/2, field 11 and SASS bit 2, GSND
  Code `0x16`), `CAP-058-FINDINGS.md` §6 (case sounds), `CAP-024-FINDINGS.md` §4–§5, `CAP-022-FINDINGS.md` §4, `CAP-065-FINDINGS.md` §4 and `CAP-066-FINDINGS.md` §4
  (one bud out ⇒ channel 19/21; lead L-1).
- Every Kotlin source under `android/` that a finding touches, **in full**, before you write about it — at least `SettingFrame.kt`, `Maestro.kt`, `CodecRouter.kt`,
  `PwRpc.kt`, `BudsRepositoryImpl.kt`, `SessionReopener.kt`, `SessionDiagnostics.kt`, `RfcommBudsTransport.kt`, `BudsSettings.kt`, `ValueCurrency.kt`,
  `SettingsUi.kt`, `ControlsScreen.kt`, `EqScreen.kt`, `ConnectionScreen.kt`, `SettingsMenu.kt`, `MainActivity.kt`, `AppUiSession.kt`, their tests
  (`SettingsCodecTest.kt`, `SettingsFixtures.kt` with `Settings074`, `BudsRepositoryImplTest.kt`, `ControlsScreenTest.kt`, `EqScreenTest.kt`), and
  `android/app/build.gradle.kts`.

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
**ai-sessions/0075_CAPTURE_RESULT_2026_10_07.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and update that block at the
end of every phase **and** after every substantial step within a phase (every 5 minutes of film reviewed, every HCI log and channel decoded, every log file read,
every checkpoint answer): what is done, what is next, which files are touched but unverified, and where intermediate results live (the scratchpad directory —
scripts, contact sheets, decoded tables, notes; re-create them if the scratchpad is gone). A resumed session reads this prompt, then the RESULT's Progress block,
re-reads the files the unfinished step touched, and continues from there. It never redoes a finished, recorded step, and never assumes an unrecorded step was
done. The prompt keeps its number and date; the RESULT keeps growing in the same file.

**Memory and the machine (two OOM kills so far, `ai-sessions/0057` and `0074`).** The laptop has 15 GB. Run `tshark`/`ffmpeg` work one job at a time; never run
two Gradle builds at once (no build is needed in this session anyway); never `git worktree add` a commit — a worktree checks out `captures/` (LFS, gigabytes);
write intermediate results to the scratchpad and update the Progress block **before** a long job.

**Nothing is taken on trust, including this prompt, the skeleton, earlier RESULTs and the maintainer's statements.** Every claim — and every number measured
below (§2) — is re-derived from the film, the HCI logs, the other logs, the code and official documentation.

---

## 1. What the maintainer asked for (chat, 2026-10-07, translated from Dutch)

1. **Analyse `CAP-070` (Group BF) of the OpenControl app fully and record the findings.** First read the core files (`AGENTS.md`, `PROJECT_RULES.md`,
   `PROTOCOL.md`, `ARCHITECTURE.md`, `DECISIONS.md`) to understand the ground rules before auditing the rest.
   - Analyse the film first and record every event and action with its time in **CAP-070-EVENT-NOTES.md**.
   - Analyse **all** `CAP-070-btsnoop_hci*` logs with `tshark`.
   - Analyse the `CAP-070-opencontrol-debug-20261007-*.txt` exports and the `CAP-070-OpenControl-for-Pixel-Buds-Pro-2-log-*.txt` logcats.
   - Then correlate the events of `CAP-070-EVENT-NOTES.md` with the HCI logs, the exports and the logcats; `CAP-049-EVENT-NOTES.md` is the example.
   - Run an extensive analysis and record the findings in **CAP-070-FINDINGS.md**; `CAP-049-FINDINGS.md` is the example.
   - Possibly relevant: `ai-sessions/0074_FEATURE_RESULT_2026_10_06.md`, `APP_TESTPLAN.md`, `ARCHITECTURE.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`,
     `CAP-070-EVENT-NOTES.md`, `PROJECT.md`, `README.md`, `TODO.md`.
2. **Not every step was done exactly as in the test plan, and some extra things were shown on the film.** So: **establish from the film and the logs what was
   actually done**, in the order it was done — never fill the timeline from the skeleton. Map each real action to the skeleton's step IDs (P0–P7, BF-1 … BF-25,
   BF-end) and to `APP_TESTPLAN.md` section T (T1 … T11) and the other test-plan IDs the skeleton names (H5, C10, C12, S6, S12), and mark each step **done**,
   **done differently**, **repeated** (each repetition its own row), **skipped** or **not identifiable**; list every extra action as its own row ("not in the plan").
3. **Facts the maintainer gave (record them as the maintainer's statements; check each against the evidence):**
   - **The OpenControl app was 1.1.0, build `0323849`, in all captures.** Read the Info tab on film (version, `build <hash>[-dirty]`, commit date, "Control
     channel: N"); check that `0323849` is the tip of `origin/feature/0074-settings-switches` (the release checklist's build commit B1) and that no file under
     `android/` changed after it (`git diff 0323849..origin/feature/0074-settings-switches -- android` empty); "-dirty" must not be shown. Both logcat headers say
     `package: io.github.tedsluis.opencontrolpixelbuds:10100` (re-check). If `dist/1.1.0/` exists, check the APK's SHA-256 and certificate **read only**
     (`apksigner verify --print-certs`; expected certificate `a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb`); never rebuild, never run
     `scripts/release.sh`, never write into `dist/`. Cross-check against wording that exists only in 1.1.0 (the cards "Head gestures", "Multipoint", "Case sounds",
     the "Volume EQ" switch, "—" in place of an unread switch, the (i) lines "<Label>: read/changed HH:MM:SS").
   - **The capture ran on a Pixel 9a with GrapheneOS, Android 17.** Verify it: the logcat header (`osVersion …tegu:17/CP3A.260905.009/2026100201`, `userType:
     full.secondary`), the P0 output the maintainer added to the notes, and above all the HCI logs — **no Message Stream claim whose first phone message is
     `03 08 00 02 01 25`** (the Play-services marker of `CAP-066-FINDINGS.md`), with the command, its exit status and a positive control in `CAP-066`. Interpret
     the P0 output exactly: `grep -i -E "gms|vending"` matched only `app.grapheneos.gmscompat*` (GrapheneOS's own compatibility layer — check against GrapheneOS
     documentation); no `com.google.android.gms` / `com.android.vending`; its exit status was not recorded.
4. **Strict execution rules (the maintainer's own words, translated):**
   - **No assumptions.** Verify everything.
   - **Validate externally.** Use search tools to validate claims against official documentation (Android Developer docs / AOSP sources, the Bluetooth Core
     Specification, the Fast Pair specification, GrapheneOS documentation) where needed; cite the URL and the exact sentence. (developer.android.com pages have
     returned only navigation to the fetch tool before; AOSP sources at `android.googlesource.com/…?format=TEXT` read with `curl … | base64 -d`, and the raw Fast
     Pair pages via `curl` worked — check any fetch-tool summary against the raw text before quoting it.)
   - **No sampling.** No random spot checks: a 100 % complete and exhaustive review of the relevant files, checking every detail — every second of the film, every
     packet of all four HCI logs that belongs to the Buds, every line of the four exports and of both logcats.
   - **Best practices.** Industry-standard technical review methods: **evidence traceability** (every claim links back to a specific frame number with its HCI
     file, a log line with its file, or a film timestamp) and **structural integrity** checks (files, registry, links, footers).

---

## 2. The evidence

`captures/CAP-070-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BF/` — `CAP-070-EVENT-NOTES.md` is committed (the skeleton) and **modified** in the working tree (the
maintainer's P0/P1 outputs); the other eleven files are **untracked**. `id_registry.csv` has a `planned` row for `CAP-070`. Measured by the chat that wrote this
prompt (2026-10-07) — **re-check each value**:

| File | Measured / what to check first |
|---|---|
| **recording.mp4** | 1,817.27 s (≈ 30 min 17 s), 1,399,626,054 bytes, H.264 1280×720 at ≈ 29.85 fps; `creation_time` 2026-10-07T04:40:59Z (= 06:40:59 local — the film's *end*? measure the film's own clock); **an audio stream that `ffprobe` reports as `codec_name=unknown`, `sample_rate=0`** — establish whether it holds sound at all; the skeleton asked the maintainer to *say* observations in BF-9, BF-11, BF-12 (case sounds, Volume EQ) — if there is no usable audio, those observations are only what is visible or what the maintainer states. The file is not named `CAP-070-…` (rename at the migration, §4 task 3) |
| **CAP-070-btsnoop_hci1.log.last** | 4,664 packets, 05:44:02.520 – 06:10:46.252 (before the film?) |
| **CAP-070-btsnoop_hci1.log** | 5,013 packets, 06:10:52.690 – 06:36:08.491 |
| **CAP-070-btsnoop_hci2.log.last** | 5,285 packets, **06:10:52.690** – 06:37:28.403 — `cmp -n <size of hci1.log>` shows **`CAP-070-btsnoop_hci1.log` is a byte-for-byte prefix of `CAP-070-btsnoop_hci2.log.last`** (the same snoop session pulled twice, as `CAP-068`). Prove it packet by packet; count every frame once |
| **CAP-070-btsnoop_hci2.log** | 1,430 packets, 06:39:36.576 – 06:42:06.705 |
| All four HCI logs | `Bluetooth H4 with linux header`. Boundaries ≈ 06:10:46 → 06:10:52 (P2's Bluetooth off/on?) and ≈ 06:37:28 → 06:39:36 (BF-24's Bluetooth off, BF-25's on?). Establish what caused each, the gaps, and that no Buds traffic is lost or counted twice |
| **CAP-070-opencontrol-debug-20261007-063244.txt** | 957 lines, 06:09:28.868 – 06:32:11.411 |
| **…-063339.txt** | 962 lines, 06:09:28.868 – 06:32:49.210 |
| **…-063647.txt** | 973 lines, 06:09:28.868 – 06:35:41.621 (the export before BF-24's force-stop?) |
| **…-063952.txt** | 55 lines, **06:37:57.663** – 06:39:43.230 — a **new process** (the ring buffer starts again; "Permissions (start)" line) |
| All exports | ≈ 380 hex lines in each of the first three, 24 in the fourth (check the Debug-mode state per stretch, P6). Check line by line which export contains which; explain the gaps between each export's last line and its save time |
| **CAP-070-OpenControl-for-Pixel-Buds-Pro-2-log-e106d62edae5.txt** | logcat, 583 lines (saved ≈ 06:37), header `userType: full.secondary`, `package: …:10100`, buffers `main,system,crash,events,kernel`; timestamps `10-07 03:26:45.772 …` (**local − 2 h?** — measure); PIDs 23551, 23764, 28098 |
| **CAP-070-OpenControl-for-Pixel-Buds-Pro-2-log-807f70280a2a.txt** | logcat, 617 lines (saved ≈ 06:40), same header; PIDs 23551, 23764, 28098 **and 525** |
| Both logcats | **Four app processes** — establish every start and end (force-stop, Android killing it, a crash) and what was on screen; **two `Wrote stack traces to tombstoned` lines** (e106d… line 510, `04:34:48.516`, PID 28098; 807f… line 599, `04:40:32.203`, PID 525) — find what caused each (an ANR, a SIGQUIT from a bug report or a logcat/stack dump by the maintainer's tool, …) and whether anything was lost; no `FATAL`, `ANR in`, "Decoder fault" or "Unexpected error" line was found by `grep` (re-check) |
| **CAP-070-EVENT-NOTES.md** | the skeleton plus the maintainer's P0 output (user 10; `app.grapheneos.gmscompat.config`, `…gmscompat.lib`, `…gmscompat`) and P1 output: `versionCode=10100`, `lastUpdateTime=2026-10-07 05:26:38`, **two** `firstInstallTime` lines (`2026-10-04 15:22:42` and `2026-10-07 05:26:38`) — establish which line belongs to which user (`dumpsys package` prints one per user; check against AOSP `PackageManagerService`/`dumpsys` output format) and therefore whether **1.1.0 was installed over 1.0.1 in user 10 or freshly** (the `TODO.md` §2 question P1 was meant to settle). Rewrite the notes into the record; keep the planned procedure as an unchanged appendix |
| Absent | **No `uiautomator` dumps** (BF-24 asked for two) — record the absence and what other evidence (the film) covers the screen-reader check; **no system log**; **no screen recording** (P4 — only the camera film is present; check). Record each absence |

File modes: five files `-rw-r--r--`, seven `-rwxr-----` — no executable bit on capture files (fix in Phase 0, with approval). `git check-attr filter` → `lfs` for
eleven files, `unspecified` for the notes (measured; re-check).

**Known pitfalls (from `CAP-063` … `CAP-068` — check, do not assume):**
- With the `H4 with linux header` encapsulation `bluetooth.addr` is empty — scope every filter by the Buds' connection handle(s) from the Connection Complete events
  **of each file** (`AGENTS.md` §13.1); handles restart after Bluetooth off/on. Show each filter matching a known frame. Other devices may share the log.
- **RFCOMM DLCIs are session-local** (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05). `python3 scripts/pwrpc_decode.py <log>` (`--handle 0x…` per
  connection) reads DLCI 2 and 3 and reassembles packets split over RFCOMM frames; still check the CRC-32 of each pw_hdlc frame yourself where a claim depends on
  it (the method of `ai-sessions/0074` §A.1). `scripts/message_stream_tally.py` tallies ANC `Set`/ACK/NAK.
- **The Message Stream claims:** the app's start with `08 11`/`08 12` or its battery/Find sequence; Play services' with `03 08 00 02 01 25` — none expected.
- A filter on `data.data` does not see frames a dissector has claimed (HFP, AVDTP) — use `frame contains` or the protocol's own field (`AGENTS.md` §13 step 8);
  AVRCP is `btavctp or btavrcp`; a `tshark` exit status ≠ 0 is an error, not "0 frames".
- `field 17` is `sint32` (zigzag); `field 16` five little-endian floats; **field 29 is 1 = off / 2 = on**, never 0/1 (ADR-052).

**Privacy (standing rule, ADR-037).** Check **every** frame of the film for personal data: notifications, messages, contact names, e-mail addresses, Wi-Fi and
Bluetooth device names, Android's settings and pairing screens, **a burned-in street-address overlay** (earlier camera films carried one), faces other than what
the maintainer chose to film (head and ears). Report what you find and **ask** (earlier choices do not carry over). If a blur is wanted, propose the exact `ffmpeg`
command, the ranges and a before/after sample. Never write the Buds' full address or serial into a document (ADR-010).

---

## 3. Leads and context — verify each before relying on it

1. **The build and the install (P1, BF-2, T1):** 1.1.0 / 10100 / `0323849` without "-dirty"; installed over 1.0.1 in user 10 or not (§2, P1 output); were the
   app's permissions and its CDM association kept (an update keeps both; the first export starts with "Permissions (start): … NOT_REQUESTED -> GRANTED" — what
   does that say)?
2. **The twelve Connect-time reads (BF-1, BF-8, BF-25, T1, T8, O8):** per connection, every `ReadSetting` in the order 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29,
   each with its answer and latency, against `ai-sessions/0074` §A.4 (21–241 ms in the official app's sweeps); any read for another field (13, 21, 23 … 39) or on
   DLCI 0x08/0x0a is a central finding; the same sequence on a pull.
3. **The five switches (BF-3 … BF-12, T2 … T8, ADR-052 … ADR-055):** per write, the request bytes against the skeleton's expected-frames table, the empty
   `RESPONSE`, the switch on film moving only after it, the (i) line; the read-back after the reconnect (BF-8); head gestures 1/2 on the wire, no dialog, nothing
   from the phone on DLCI 0x08; for Multipoint the SASS `07 11 00 04 01 02 98/b8 00` only if a Message Stream claim was open (who held DLCI 0x04 then?); GSND
   Code `0x16` after each head-gesture write if the Google app's channel was open (`CAP-069-FINDINGS.md` §1; in this user it is likely not).
4. **The three request forms never captured before (BF-14, BF-15, BF-20; the 0074 "Deferred documentation"):** fields 11 and 29 on **channel 19**
   (`…2a04220258009d8f9dc47e`, `…2a04220258010bbf9ab37e`, `…2a052203e80101fcd6da847e`, `…2a052203e801024687d31d7e`) and `4:{15:1}` on **channel 21**
   (`…2a04220278019977e84e7e`), and the channel-19 balance `4:{17:7}` (`…2a052203880107e9b86e257e`, BF-16). For each: did it occur, on which channel (the
   announcement before it), byte-identical to the derived frame or not, the Buds' answer. These bytes decide whether the labelled structural fixtures
   (`Settings074`) and ADR-055's `// TODO(verify)` can be replaced — **that replacement is an app (test) change: propose it for a later session, do not make it
   here** (the release branch must not get an `android/` change after the build commit).
5. **The channel choice (BF-13, BF-17 … BF-19, lead L-1, `INEAR-005`):** only the Left bud out ⇒ 19, only the Right out ⇒ 21 (`PROTOCOL.md` §2.2a 🟢, `CAP-065`
   7/7) — more samples or a counter-example; BF-18: both worn on 19, the **Left** out of the ear ⇒ Buds `DISC` + 21 (🟡 predicted)? The head side on film for every
   wear step (head on the **right** of the frame = **Left** bud), cross-checked with the runtime-info per-bud fields (6.2 Left / 6.3 Right) and the Settable byte.
6. **A write during a re-open (BF-13, C12):** the text "The setting was not changed: The app's channel is being reopened — try again in a moment." on film, and **no
   `WriteSetting`** afterwards (nothing queued).
7. **"—" and the screen-reader text (BF-1/S6, BF-24, T11):** "—" in place of each unread switch and for the EQ bands/balance on film; the first second after
   "ready"; the screen-reader content description cannot be seen on film — with no `uiautomator` dump present, say what is and is not established (an open item for
   the next run, not a guess).
8. **The film items of `TODO.md` §2 (BF-21 H5, BF-22 the swipe, BF-23 S12, P1):** done, done differently or not done, with the evidence.
9. **Case sounds and Volume EQ observations (BF-9, BF-11, BF-12):** what the film shows and what the maintainer says or shows; nothing is claimed about what was
   heard without evidence (`AGENTS.md` §5, ADR-054/055).
10. **Session ends, re-opens and processes:** the full table as in `CAP-068-FINDINGS.md` (Bluetooth off/on, Disconnect, the force-stop of BF-24, Buds-side closes on
    wear changes, the four PIDs), the cause shown and logged, whether ADR-044's rules held (one attempt per event, the 10-s guard).
11. **Battery and case** — every value shown on film against the wire value and its receive time (`03 03`, runtime-info 6.1/6.2/6.3), and the "last connection"
    marks.
12. **Anything new or wrong:** NAKs, pw_rpc error statuses, Safe Mode (expected off on `release_5.203`), "Late WriteSetting answer dropped" / "Maestro request held"
    lines (ADR-045 Update), any app frame on DLCI 0x08/0x0a (must be none), any request outside the user's actions, the two `tombstoned` lines, anything that
    behaves differently in a secondary user.
13. **The extra things the maintainer showed on film** — identify each and say what it demonstrates.

---

## 4. Tasks

### Phase 0 — set-up, privacy and registration (ask before any move, rename, mode change or `git add`)

1. Create the RESULT file (§0). Record `git log -1`, `git branch --show-current`, `git status --short`, `git fetch && git log --oneline HEAD..origin/main` and
   `HEAD..origin/feature/0074-settings-switches`, and `gh pr view 7 --json state,headRefOid`. **Work on branch `feature/0074-settings-switches`** (`RELEASING.md`,
   Release checklist C3: the capture and its analysis are committed after the build commit on the release branch); switch to it if needed without losing the
   maintainer's uncommitted change to the notes. Confirm the `planned` row of `CAP-070` in `id_registry.csv` and the Group BF section and Capture Index row in
   `CAPTURE_BLUETOOTH_HCI_SNOOP.md`. Identify every file with `capinfos`, `ffprobe` and `sha256sum` and re-check every value in §2.
2. Privacy check of the film (§2). Summarise the result.
3. Plan the migration (ADR-037, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9): rename the folder to `captures/CAP-070-YYYY-MM-DD_HH-mm-ss_HH-mm-ss-Group_BF/` from the
   film's first and last clock time (say which clock); rename recording.mp4 to CAP-070-recording.mp4; `chmod 644`; the registry row; the Capture Index row; Git
   LFS for every capture file. **Ask the maintainer (`AskUserQuestion`)** about the privacy outcome and the migration plan together, before moving anything.
4. Execute only what was approved. Before moving anything, compare checksums of a fresh listing of the source against the destination; prefer a plain `mv`; never
   chain deletes; never `rm -rf` based on an earlier listing. Update every reference to the placeholder folder name (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TODO.md`,
   `APP_TESTPLAN.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `README.md`, `_sidebar.md`). The KDoc of `SettingsCodec.FIELD_VOLUME_EQ` also names the placeholder, but it
   is under `android/` — record it for the follow-up session, do not edit it. Earlier `ai-sessions/` files are history — leave them as written.

### Phase A — the film, in full

5. Scan the whole film at a fixed interval of ≤ 2 s plus scene changes, then narrow every transition to ≤ 1 s, and to a single frame where a claim depends on it:
   every lid opening/closing, every bud taken out of or put into the case or an ear (with the head-side mapping), every tap (Connect, Disconnect, a switch, a pull,
   a slider, a preset, *Read EQ again*, the gear, Info, Debug, Export, the tile), every tab change and swipe, rotation, Bluetooth toggle, force-stop, every Android
   settings screen, every toast and dialog, every Home / return, every "—" and (i) dialog, and every action not in the plan. Read every visible on-screen text
   (switch positions, (i) lines, error texts, "Control channel"). Crop and zoom the ear, case and card regions.
6. The **audio stream**: establish what it is (§2); use audible events only for timing, saying so.
7. Measure the film ↔ phone clock offset at the start **and** the end (a status-bar minute flip against the overlay, at ≥ 4 fps — never a single point), and the
   logcat ↔ phone and export ↔ phone offsets (an event in both).
8. Rewrite **CAP-070-EVENT-NOTES.md** in the `CAP-049-EVENT-NOTES.md` / `CAP-068-EVENT-NOTES.md` layout: header status, Log Metadata (phone, GrapheneOS build, the
   user and the absence of Google Play with the maintainer's P0 output, P1's install times and their reading, the OpenControl build from the Info tab, firmware,
   other devices, film/log ranges per file, clock offsets, the wear mapping and its cross-check), capture-integrity pre-flight (all four HCI logs incl. the prefix
   proof, the four exports, the two logcats and their processes, the absent system log, dumps and screen recording), video review method (incl. privacy), an Event
   Timeline (phone time, action, actor, step ID, T-/test-plan ID, registry Test-ID, evidence with its file), a step-mapping table, the analysis checklist, and the
   skeleton as an unchanged appendix. **Traceability (`AGENTS.md` §13.7):** every skeleton step and every Test-ID it names appears in the timeline or is explicitly
   "skipped" / "not identifiable"; every repetition and every extra action is its own row.

### Phase B — the HCI logs and the other logs, in full

9. Follow `AGENTS.md` §13 for **all four** HCI logs. Establish the overlap first and decide, per packet range, which file is the source. Pre-filter by the Buds'
   handle(s), take the full RFCOMM inventory (`SABM`/`UA`/`DISC`/`DM`, direction, per ACL session and multiplexer side), then decode **every** Buds packet: MAESTRO
   (every `GetSoftwareInfo` with its channel and entries; every `ReadSetting`/`WriteSetting`/`SubscribeRuntimeInfo`/`SERVER_STREAM` with field, value and status;
   CRC-32 per frame); the Message Stream per claim (app vs anything else; ANC, battery, SASS); DLCI 0x08/0x0a (who opens them, every message); HFP; AVRCP; every
   ACL and LE event with its reason code; the Bluetooth off/on cycles. Every negative with its command, exit status and a positive control.
10. Read the four exports and both logcats **line by line** and put each relevant line on the timeline (phone time after the measured offsets; the file named). For
    every filmed action write the exact frames and log lines: what the app sent, on which channel, what the Buds answered, what the app logged and showed. Where no
    log covers a stretch (the HCI boundaries, the force-stop, no system log), say which evidence does, or that none does.
11. Build the tables the findings need: the twelve reads per connection; every write against the expected-frames table (real / derived / first capture); the three
    new forms; the channel per announcement against the buds out/worn; section C12; the session-end and process table; every battery value shown vs the wire; the
    Message Stream claims with their first phone message.

### Phase C — findings and the release verdict

12. For every lead of §3: the answer with evidence (frame numbers with their file, film times, log lines, `file:line` in the app sources), labelled 🟢 FACT / 🟡
    HYPOTHESIS / ⚪ ASSUMPTION / 🔴 OPEN QUESTION, with the experiment that would settle any non-FACT.
13. Write **CAP-070-FINDINGS.md** in the `CAP-049-FINDINGS.md` / `CAP-068-FINDINGS.md` layout, every conclusion labelled (`AGENTS.md` §15, `PROJECT_RULES.md` §1),
    every hex decoding with its command and raw bytes (rule 4a), at most one status banner (rule 9a). Include: the re-test verdicts (the skeleton's "Refuted if"
    applied literally), what works, what does not, what goes wrong, per protocol, and what is different in a secondary user. If a finding contradicts an existing 🟢
    FACT or ADR, say so and draft the correction as a **proposal**; do not edit that FACT or ADR.
14. **The release verdict for 1.1.0** (`RELEASING.md`, Release checklist C4): classify every defect as **heavy** (a crash, a wrong or unacknowledged write, a write
    applied before its answer, Safe Mode failing on the verified firmware, a session lost without recovery, anything risky for the Buds) or **minor** (a known issue
    for the release notes), with the evidence. Give one of: *release the tested build as 1.1.0* (with the known issues worded for `CHANGELOG.md` and the release
    notes — the "three forms not yet seen" issue removed or kept according to lead 4), *fix first* (what, and the targeted re-test — the same version may be rebuilt
    while nothing is published), or *re-run*.
15. **Improvements (proposals only, no app change):** for every defect: the cause, the proposed change (file and function, the behaviour, the guardrails), the unit
    test that would have caught it (real-byte fixtures from this capture, `AGENTS.md` §11), and the re-test step. The fixture replacement of lead 4 goes here as a
    proposal for a follow-up session. Anything that changes an ADR's decision is a drafted ADR (no number).
16. **Do not modify any file under `android/`, `dist/` or `scripts/`.**

### Phase D — checkpoint, documentation, finish

17. **Checkpoint — stop and ask, in this chat, via `AskUserQuestion`.** One question per decision; each option with pros and cons, one marked "(Recommended)", the
    exact draft text of any FACT, ADR, `PROJECT.md`, `CHANGELOG.md` or `README.md` change in the preview (memory: "Approvals: confirm in chat"). Cover at least: the
    release verdict; the known-issues wording; the three new forms (lead 4) and the follow-up for the fixtures; lead L-1 (`PROTOCOL.md` §2.2a, a status proposal if
    the evidence holds); the P1 install question; any other FACT or ADR change. **No new capture skeleton is written in this session.** Record the answers verbatim.
18. Apply only what was approved, and only to documentation: `PROJECT.md`, `PROTOCOL.md`/`DECISIONS.md` (approved changes only, dated Updates, process note citing
    this chat), `ARCHITECTURE.md` (only where a hardware result corrects it, e.g. §5a hardware-verified marks for fields 11, 15, 27, 28, 29), `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
    (Group BF run note, Capture Index row), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (evidence cells of the Test-IDs exercised — pointers only), `APP_TESTPLAN.md` (the
    Summary for this run; a step that proved unworkable), `id_registry.csv` (`CAP-070` → analyzed with the real times), `TODO.md` (done items removed, open items
    added — §2 in particular), `CHANGELOG.md` (the `[1.1.0]` block's Known issues if the verdict changes them; **not** its date), `README.md` (status, capture counts —
    not "Latest release"), `ai-sessions/INDEX.md` (the 0075 row). Run `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and
    `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py`; it must exit 0.
19. Finish the RESULT: plain-language answers first (the release verdict; what works and what does not on 1.1.0 — each of the five switches, "—", the three new
    forms, L-1; what differs in a secondary user; what the app should change next); then the step-mapping results, the tables of Phase B, the external sources (URL
    plus quoted sentence), and, if the verdict is "release", the maintainer's next steps from `RELEASING.md`'s Release checklist D1–E4 (merge PR #7 with a **merge
    commit**, tag **the build commit read from the Info tab**, the files from the kept copy of `dist/1.1.0/`) — **as instructions, never executed**. It must end with
    **"Deferred documentation"** (each item also added to `TODO.md`) and **"Commits"** (hashes, or "not committed — reason"), per `AI_SESSION_LOG_PROCEDURE.md` §9 and
    §4b; also back-fill the "Commits" of `ai-sessions/0074` (the hash of its session commit, `0323849`). Set the Status per §4.
20. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes: on branch `feature/0074-settings-switches`, Conventional Commits,
    one commit per concern (capture/LFS, docs, session files), each with a *why* and ending with the attribution line from the session's system reminder; `git fetch`
    and rebase before pushing, never force; `git check-attr filter` on every capture file (`lfs`); **no file under `android/` in any commit** (`git diff
    0323849..HEAD -- android` must stay empty); nothing from a build directory, `dist/`, `android/.kotlin/`, `.vscode/` or `__pycache__` staged
    (`scripts/__pycache__/lint_docs.cpython-314.pyc` is tracked — `git checkout` it if it changed). A large LFS push (the film is ≈ 1.4 GB) can drop the SSH
    connection after the upload — check `git log origin/feature/0074-settings-switches -1` and push again if needed.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **No app changes and no release actions.** Nothing under `android/`, `dist/` or `scripts/` is modified; no build is made; `scripts/release.sh` is not run; the pull
  request is not merged; no tag is created or pushed, no GitHub release created or edited, no asset uploaded. Publishing is the maintainer's own act (`RELEASING.md`).
- **Approvals only in this chat.** Never promote, demote or correct a 🟢 FACT and never write or update an ADR without the maintainer's approval given in this chat
  (`AGENTS.md` §6). Model agreement is not approval.
- **Evidence.** Zero creativity with hex (`AGENTS.md` §13.6). Every claim needs a frame number (with its HCI file), log line (with its file), `file:line` or film
  timestamp (`PROJECT_RULES.md` rule 4a). A negative needs its command, exit status and a positive control (`AGENTS.md` §13 step 8). What the maintainer heard is
  their observation unless the wire shows the command that caused it.
- **What was done, not what was planned.** The skeleton is the plan; the film and the logs are the record. Never fill a timeline row from the skeleton's "Expected"
  column.
- **Privacy.** No full MAC address or serial in any committed text beyond what ADR-010 allows; no personal data from the film or the logs.
- **Scope.** Stay within `PROJECT.md`. Anything new (a feature, a permission, a dependency, a new wire request) is a checkpoint question with a drafted ADR, not a
  silent addition.
- **Subagents.** A subagent may only read and report. Every write is done in the main session and verified; re-derive any number a subagent reports.
- **Files.** Before deleting, moving or renaming any file, diff a fresh listing of the source against the copy (checksums). Never `rm -rf` from memory of an earlier
  listing; ask before any delete. Do not run any tool that writes into `dist/`.
- **Commits.** Commit and push only after the maintainer confirms the final summary (task 20).
- **No new capture skeleton.** Do not create a new `CAP-NNN` folder, skeleton or `planned` registry row in this session; open steps go to the findings and `TODO.md`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0075_CAPTURE_PROMPT_2026_10_07.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0075_CAPTURE_PROMPT_2026_10_07
