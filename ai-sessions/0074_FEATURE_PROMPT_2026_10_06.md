# 0074_FEATURE_PROMPT_2026_10_06.md — Build five settings switches (Multipoint ADR-053, Head gestures ADR-052, Case sounds ADR-054, Volume EQ ADR-055) with real capture bytes as fixtures, the screen-reader text for unread values, the hardware-run skeleton, and the preparation of release 1.1.0

**Number:** 0074
**Category:** FEATURE
**Date:** 2026-10-06
**Title:** Implement the switches Multipoint (`qhr` field 11), Head gestures (field 29), Case sounds "Other alerts" and "Earbuds replaced" (fields 27
and 28) and Volume EQ (field 15) in the OpenControl app — reads at Connect and one write per tap, real capture bytes as test fixtures, a green
test/lint gate with mutation checks — plus the screen-reader text for a value that was not read, the documents that follow the code, the skeleton of
the hardware run (with the open film items of `TODO.md` §2), and the release preparation for 1.1.0 (no tag, no publication)

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer, Android/Kotlin engineer and technical auditor. This prompt is for a **fresh** Claude Code
session in this repository. Run the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8).** Before any other action, read **in full**, in this order:
`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md` (in particular §2.2a, the §4.5 preamble, §4.5.2 Multipoint, §4.5.4
head gestures, §4.5.6 Volume EQ and §4.5.8 Case sounds with **all** their dated Updates, and §6), `DECISIONS.md` (**every** ADR, ADR-001 … ADR-055,
with every dated Update — in particular ADR-019, ADR-034, ADR-036, ADR-042, ADR-044, ADR-045 with its 2026-09-30 Update, ADR-047, ADR-048, **ADR-052,
ADR-053, ADR-054 and ADR-055**), `TODO.md`. Then read `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`, `RELEASING.md`, `APP_TESTPLAN.md`,
`CHANGELOG.md`, `id_registry.csv`, and:

- `ai-sessions/0056_FEATURE_RESULT_2026_09_28.md` — how the field-2 switch (ADR-047) and the field-12 list were built: the pattern to follow;
- `ai-sessions/0052_FEATURE_RESULT_2026_09_26.md` — how the ADR-045 settings reads and writes were built;
- `ai-sessions/0069_MAINTENANCE_RESULT_2026_10_03.md` — the 1.0.1 build: the "current value" rule, "—" for a value that was not read, and how a
  release build got its hardware-run skeleton;
- `ai-sessions/0071_CAPTURE_RESULT_2026_10_04.md` and `CAP-069-FINDINGS.md` §1, §2 and §13 — the evidence behind ADR-052 and ADR-053;
- `CAP-024-FINDINGS.md` §4–§5, `CAP-058-FINDINGS.md` §6, `CAP-022-FINDINGS.md` §4 — the evidence behind ADR-054 and ADR-055;
- `ai-sessions/0073_CROSSCHECK_RESULT_2026_10_06.md` §4.2 (the settings register) and §4.5 (the announcement and the channel);
- `CAP-068-EVENT-NOTES.md` and `CAP-068-FINDINGS.md` — the layout of the last release-build run and what it left open.

Read every Kotlin file you change **in full** before changing it — at least `SettingFrame.kt` (`SettingValue`, `SettingsCodec`, `FLAG_FIELDS`,
`WRITABLE_FLAG_FIELDS`, `flagRequest`, `decode`), `Maestro.kt` (`READABLE_FIELDS`, `readSettingRequest`), `BudsSettings.kt`, `BudsRepository.kt`,
`BudsRepositoryImpl.kt` (`readSettings`, `refreshSettings`, `writeFlag`, `writeSetting`, the ADR-045 Update's quarantine), `SafeModeGate.kt`,
`BudsRepositoryImplTest.kt`, `SettingsCodecTest.kt`, `SettingsFixtures.kt`, `FakeBudsTransport.kt`, `ControlsScreen.kt`, `EqScreen.kt` (the "Sound"
tab, `NOT_READ_VALUE`), `Details.kt`, `OpenControlNavHost.kt`, `MainActivity.kt` (the state holder, ADR-048) and their tests. Say in the RESULT which
files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
**ai-sessions/0074_FEATURE_RESULT_2026_10_06.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and update that
block at the end of every phase **and** after every item built (which items are done and tested, which files are touched but not yet tested, the
last gate result, where intermediate results live — the scratchpad directory: decoded frames, the mutation script; re-create them if the scratchpad
is gone). A resumed session reads this prompt, then the RESULT's Progress block, re-reads the files the unfinished step touched, and continues from
there. It never redoes a finished, recorded step, and never assumes an unrecorded step was done.

**Nothing is taken on trust, including this prompt, the ADRs' frame numbers and the earlier RESULTs.** Re-derive every frame and byte you use from
its capture log (rule 4a) before it becomes a fixture; if anything here disagrees with a capture or with the code, the capture wins — record the
correction in the RESULT and, where it touches an ADR's text, bring it to the checkpoint.

---

## 1. What the maintainer asked for (chat, 2026-10-06, translated)

1. One build session for **all five switches**: Multipoint, Head gestures, the two Case sounds switches and Volume EQ. ADR-052 and ADR-053 were
   accepted on 2026-10-04; ADR-054 and ADR-055 on 2026-10-06 (both chats recorded in the ADRs). The order Multipoint first, then Head gestures, is the
   maintainer's earlier choice (chat 2026-10-04, "Order").
2. In the same session: the **screen-reader text** for a setting that was not read ("—"; `TODO.md` §5 "Accessibility"), the update of
   **`ARCHITECTURE.md` §5a and `PROJECT.md`** (they still describe head gestures and Multipoint as waiting for `CAP-069`), and the **open film items
   of `TODO.md` §2** in the hardware run of this build.
3. The maintainer expects **two sessions**: this one builds, documents and writes the test script; the maintainer then films the run on the phone;
   a later session analyses the capture and gives the release verdict. This session publishes nothing.
4. Deliberately **left out** (the maintainer agreed, chat 2026-10-06): the Gradle/Kotlin/Compose upgrade, moving texts to `strings.xml`,
   instrumented tests, the concurrency items of `ai-sessions/0068`, the balance precision, and decompiling app 1.0.990706425.
5. **Placement (the maintainer's decision, chat 2026-10-06, translated: "place the switches for Multipoint, Case sounds and Head gestures on
   Controls"):** the **Multipoint** switch, the two **Case sounds** switches and the **Head gestures** switch go on the **"Controls"** tab. Volume EQ
   was not part of that sentence; ADR-055 names the "Sound" tab for it.
6. **Strict execution rules (standing, the maintainer's own words in earlier sessions):** no assumptions — verify everything; no sampling — read the
   relevant files completely; validate external claims against the official documentation and cite the URL and the exact sentence; evidence
   traceability (every claim links to a capture frame, a log line or a `file:line`) and structural integrity (files, registry, links, footers).

---

## 2. The items (verify each against the captures, the ADRs and the code before building)

All five are `maestro_pw.Maestro` requests on DLCI 0x02, payload `4:{N:v}`, on the channel the Buds announced with its ADR-034 address.
"Fixtures" lists every frame known on 2026-10-06 — re-derive each; the channel is in brackets.

| # | What | Rules | Write fixtures | Read fixtures (`ReadSetting` `RESPONSE`) |
|---|---|---|---|---|
| **MP** | "Multipoint" switch: read field 11 at Connect, `WriteSetting 4:{11:0\|1}` on a tap | ADR-053 (write), ADR-036 (read — unblocked, not built) | `CAP-069` 3161, 3245 (`0`), 3212, 3275 (`1`) [21]; `CAP-019` 2482 (`0`), 2293 (`1`) [21] — **no channel-19 write captured** | `CAP-069` 1267 [21], 7272 [19]; `CAP-024` 1014 [19] — all `4:{11:1}` |
| **HG** | "Head gestures" switch: read field 29 at Connect, `WriteSetting 4:{29:1\|2}` on a tap — **1 = off, 2 = on, not 0/1** | ADR-052 (read + write) | `CAP-069` 2492, 2737, 2923 (`1`), 2564, 2831, 3025 (`2`) [21]; `CAP-020` 2038 (`1`), 1935 (`2`) [21]; `CAP-041` 2268 (`1`) [21] — **no channel-19 write captured** | `CAP-069` 1226 [21], 7209 [19] — `4:{29:2}`; `CAP-024` 1100 [19] — `4:{29:1}` |
| **CS-27** | Case sounds "Other alerts" switch: read field 27, `WriteSetting 4:{27:0\|1}` | ADR-054, ADR-036 | `CAP-024` 2053 (`0`), 2084 (`1`) [19]; `CAP-058` 5680 (`0`), 5697 (`1`) [21] | `CAP-024` 1092 [19], `CAP-058` 4514 [21] — `4:{27:1}` |
| **CS-28** | Case sounds "Earbuds replaced" switch: read field 28, `WriteSetting 4:{28:0\|1}` | ADR-054, ADR-036 | `CAP-024` 1988 (`0`), 2023 (`1`) [19]; `CAP-058` 5623 (`0`), 5643 (`1`) [21] | `CAP-024` 1096 [19], `CAP-058` 4517 [21] — `4:{28:1}` |
| **VEQ** | "Volume EQ" switch: read field 15, `WriteSetting 4:{15:0\|1}` | ADR-055, ADR-036 | `CAP-022` 1871 (`0`), 1895 (`1`) [19]; `CAP-015` 3487 (`0`), 3505 (`1`) [19]; `CAP-041` 2461 (`0`) [21] — **no channel-21 "on" captured** | `CAP-024` 1038 [19], `CAP-058` 2930 [21] — `4:{15:1}`; `CAP-041` 3239 [21] — `4:{15:0}` |
| **A11Y** | A setting that was not read shows "—" (`NOT_READ_VALUE`); give it a content description for screen readers ("not read" — exact text at the checkpoint), for the existing settings and the five new ones | `TODO.md` §5 "Accessibility"; the maintainer chose the visual form on 2026-10-03 and deferred this half | — | — |
| **DOC** | `ARCHITECTURE.md` §5a and §2.4, `PROJECT.md` ("Status after 1.0.x", the feature list), `APP_TESTPLAN.md`, `CHANGELOG.md`, `README.md`, `TODO.md` | rule 9a for the history-keeping documents | — | — |
| **RUN** | The skeleton of the hardware run of this build: the next free capture number and Group letter (check `id_registry.csv` and `CAPTURE_BLUETOOTH_HCI_SNOOP.md`; register it *planned*), in the layout of `CAP-068-EVENT-NOTES.md`, including every open film item of `TODO.md` §2 that fits this run | `AGENTS.md` §13 step 7 (Test-IDs both ways) | — | — |
| **REL** | Release preparation for **1.1.0** per `RELEASING.md` §4 only (version, `CHANGELOG.md`, `README.md`, `scripts/release_notes.template`) — if the maintainer confirms the version at the checkpoint | `RELEASING.md`; no tag, no `scripts/release.sh`, no publication by the agent | — | — |

`CAP-042`'s `.log.last` holds the same packets as the `CAP-041` log (`DESKRESEARCH_FINDINGS.md`, correction of 2026-10-06) — use the `CAP-041` log
and never count its frames twice.

Check in Phase A and handle in the design:

- **Field 29 is not a 0/1 flag.** `SettingsCodec.flagRequest` writes 0/1 and `decode` reads 0/1 for `FLAG_FIELDS`; head gestures need 1/2 on the wire
  in both directions. Do not push it through the 0/1 path with a hidden offset that a later reader can miss — give it its own, named representation.
  A read value other than 1 or 2 is shown as "—" (ADR-052). Fields 11, 15, 27 and 28 are plain 0/1 and may join the existing flag path.
- **Three channel forms have never been captured:** a write of field 11 and of field 29 on channel 19, and `4:{15:1}` on channel 21. ADR-052/053
  allow "the channel-19 form otherwise"; ADR-055 asks for a `// TODO(verify)` and a labelled structural test for its missing frame. For each of the
  three: build it with the same codec, test it with a **clearly labelled supplementary structural** byte array next to the real fixtures
  (`AGENTS.md` §11), and put its capture into the RUN skeleton as a named step whose bytes become the fixture afterwards. Which channel a session uses
  is the Buds' choice (ADR-034; `ai-sessions/0073` §4.5: the bud that announces); read how earlier runs came to channel 19
  (`CAP-066-FINDINGS.md`, `CAP-068-FINDINGS.md`, `TODO.md` §2 "both worn on channel 19") before promising a step that forces one.
- **The Connect-time read grows by five requests** (11, 15, 27, 28, 29). Find the time box and the sequencing rule the code and ADR-036 set for
  `readSettings`, measure from the fixtures' captures how long a read takes, and show that the longer sequence still fits. Do not raise a timeout
  silently — a change of a timing constant (`ARCHITECTURE.md` §3.2) is a checkpoint question.
- **The known limit of ADR-045's 2026-09-30 Update** (a `WriteSetting` answer is matched by its method only; the 1.5 s quarantine) applies to the new
  writes too: show that each goes through the same write path and lock, and add no second path.
- **One write per tap, applied only on the empty `RESPONSE` status OK;** otherwise the previous state stays and the reason is shown. No retry, no
  queue, no `CLIENT_ERROR`, no `SubscribeToSettingsChanges`. The pull-to-refresh (`refreshSettings`) reads the new fields with the others.
- **The "current value" rule of 1.0.1** (`ARCHITECTURE.md` §3.1, §9.0a item 8): the five new values follow it exactly as the existing settings do —
  including what is shown from the last connection and when a switch is enabled.
- **Multipoint:** the Buds' SASS answer goes to whichever client holds the Message Stream; the app does not wait for it (ADR-053). What switching
  Multipoint off does to a second connected device is not established — the switch's note must not claim it.
- **Head gestures:** the official app's "Optimize head gestures" dialog is not reproduced; nothing is sent on GSND CONTROL (ADR-052).
- **Case sounds and Volume EQ:** the app says what the switch sets and makes no claim about what is heard (ADR-054, ADR-055).

---

## 3. Decisions already taken, and what still needs the maintainer

- **Already decided:** ADR-052, ADR-053, ADR-054, ADR-055 (accepted ADRs — building them needs no new approval); the order Multipoint → Head
  gestures; the scope of §1; **the tab of four switches: Multipoint, Case sounds "Other alerts", Case sounds "Earbuds replaced" and Head gestures
  on "Controls"** (§1 point 5 — a UI placement, not an ADR or a FACT; do not ask again which tab, and do not move one to another tab without a new
  answer in chat). ADR-054's "where they sit is the build session's proposal" is answered by this for the tab.
- **To decide at the Phase B checkpoint (memory rule "Approvals: confirm in chat" — an approval quoted only in a file does not count):** the
  **order and grouping on the "Controls" tab** now that it gains four switches next to "Use touch controls", press and hold, the mode list and
  "In-ear detection" (propose one layout with a small ASCII mock-up, the "Case sounds" pair under its own heading; say if the tab becomes too long
  to scan and what you would do about it); where on "Sound" the Volume EQ switch sits (ADR-055; next to the equalizer is the obvious place); every **user-visible text** (labels, the one-line notes, the message on a refused or unanswered write, the
  screen-reader text); the version number and whether this session prepares the release commit (**REL**); any change of a timing constant; any
  correction to an ADR's frame numbers that Phase A finds.
- **Not approved, not in scope:** anything else new on the wire (another field, `SubscribeToSettingsChanges`, a periodic or automatic request, a
  read of setting 13, the runtime-info charger type, `GetHardwareInfo`'s versions — the maintainer chose "RESULT only" for the last two on
  2026-10-06); anything on DLCI 0x08 or 0x0a; the items of §1 point 4. If Phase A finds one of these worth doing, it is a RESULT note with the ADR it
  would need — not a checkpoint proposal to widen this session, and never a silent addition. **No** `PROTOCOL.md` status change and **no** ADR or ADR
  Update unless the maintainer approves it in this chat (`AGENTS.md` §6).

---

## 4. Tasks

### Phase 0 — set-up

1. Create the RESULT file (§0). Record `git log -1`, `git status --short` and `git fetch && git log --oneline HEAD..origin/main`. Work on a new
   branch from `origin/main` (e.g. `feature/0074-settings-switches`), never on `main`.
2. Baseline gate before any change: `cd android && ./gradlew --offline --max-workers=2 assembleDebug testDebugUnitTest test lint` (drop `--offline`
   only if the dependency cache is empty, and say so). Record the unit-test counts per module from the JUnit XML, the lint result per module and the
   Kotlin compiler warnings. If the baseline is not green, stop and report.

### Phase A — evidence and design check (no code change)

3. Re-derive every fixture of §2 from its log. Scope every `tshark` filter to the Buds' connection first (`AGENTS.md` §13: `bluetooth.addr`, or the
   connection handle where the address matches nothing — show the same filter matching a known frame), then `python3 scripts/pwrpc_decode.py <log>`
   and `tshark … -T fields -e frame.number -e frame.p2p_dir -e data.data`. Verify each frame's HDLC CRC-32. Write the raw bytes, the commands and the
   capture times into the RESULT. For each write also record its `RESPONSE` and the mirrored stream packet. Say per item which channel forms exist as
   real bytes and which do not.
4. Read the code paths in full and write, per item, the exact current behaviour (`file:line`) and the planned change (types, functions, state, tests):
   how a flag setting is decoded, sequenced into the Connect read, gated (ADR-042), sent, matched to its `RESPONSE` and applied; how `BudsSettings`
   carries a reading with its time; how the "Controls" and "Sound" tabs render a switch, its note, its time and "—"; where the 1.0.1 "current value"
   rule lives.
5. List every `transport.send` call site the design adds (expected: **none** — the five reads join the existing read sequence, the five writes use
   the existing write path). If a new call site is unavoidable, name the ADR that covers it or stop and ask.
6. The read budget (§2, third bullet): the numbers, with frames.
7. **RUN, first pass:** go through every item of `TODO.md` §2 and say for each whether it fits a release-build run of this version in the GrapheneOS
   secondary user without Google Play (as `CAP-067`/`CAP-068`), with the reason when it does not (e.g. needs two pairs of Buds, needs a debug build,
   exists only in the Owner user).

### Phase B — checkpoint (stop and ask, in this chat, via `AskUserQuestion`)

8. One question per decision; each option with pros and cons, one marked "(Recommended)"; the exact texts and an ASCII mock-up of the tab in the
   preview: (a) the order and grouping of the four new switches on "Controls" (the tab itself is decided, §3) and the place of Volume EQ on "Sound"; (b) labels and notes, per switch; (c) the failure and refusal messages if
   they differ from the existing ones; (d) the screen-reader text; (e) version 1.1.0 and the release preparation in this session; (f) anything Phase A
   found that needs the maintainer (a frame-number correction, a timing constant). Record the answers verbatim in the RESULT. Build only what was
   chosen.

### Phase C — Multipoint (ADR-053)

9. `:data`: field 11 joins the readable and the writable 0/1 fields; `:domain`: the reading in `BudsSettings` and `setMultipoint(on)` in
   `BudsRepository`; repository: the Connect read includes 11, the write goes through the existing flag write; `:ui`: the switch on "Controls" with its note.
10. Tests (JVM, `FakeBudsTransport`, real bytes with frame, time and command in a comment): decode the three read answers; encode `0` and `1` on
    channel 21 byte-identical to `CAP-069` 3161 and 3212 (and to `CAP-019` 2482/2293), including the frame check; the labelled structural test for
    channel 19; OK `RESPONSE` → the new value with its receive time; no answer or an error status → the old value stays with the reason; the Safe-Mode
    gate blocks the write on an unverified firmware; the existing tests stay green. In `BudsRepositoryImplTest` use `runCurrent()`/the suite's
    `settle()` — `advanceUntilIdle()` does not run its `backgroundScope` collectors.

### Phase D — Head gestures (ADR-052)

11. As Phase C for field 29, with its own value representation (1 = off, 2 = on): `setHeadGestures(on)`; decode `1` → off, `2` → on, anything else →
    not interpreted ("—"). Tests: `CAP-069` 2492 (`1`) and 2564 (`2`) byte-identical on channel 21 (and `CAP-020` 2038/1935); reads 1226, 7209,
    `CAP-024` 1100; a read of `0` and of `3` → not interpreted; the labelled structural test for channel 19; the rest as task 10.

### Phase E — Case sounds (ADR-054)

12. Fields 27 and 28: `setCaseSoundOtherAlerts(on)` / `setCaseSoundEarbudsReplaced(on)` (or the names that fit the code's style), the two switches
    on "Controls" under a "Case sounds" heading. Tests: all eight write frames byte-identical on their channel (both channels are real here); reads `CAP-024` 1092/1096,
    `CAP-058` 4514/4517; the rest as task 10.

### Phase F — Volume EQ (ADR-055)

13. Field 15: `setVolumeEq(on)`, the switch on "Sound". Tests: `CAP-022` 1871/1895 on channel 19, `CAP-041` 2461 on channel 21; reads `CAP-024` 1038,
    `CAP-058` 2930, `CAP-041` 3239; the channel-21 "on" frame as a labelled structural test with the `// TODO(verify)` ADR-055 asks for (pointing to
    the RUN step that will record it); the rest as task 10.

### Phase G — the screen-reader text

14. The chosen content description wherever `NOT_READ_VALUE` is shown (existing settings, EQ bands, the five new switches); a Robolectric/Compose
    test per place that asserts the semantics, not only the visible text.

### Phase H — gate, mutations, compliance

15. `./gradlew --offline --max-workers=2 clean`, then the full gate of task 2: all green, no new lint issue, no new suppression, no new compiler
    warning; record the new test counts per module.
16. Mutation checks — apply each with a script, run the relevant test task, restore, verify the file byte-identical with `sha256sum`. **Run them one
    at a time with `--max-workers=2`** (parallel runs have exhausted memory before) and, after any crash, check for a leftover mutation before
    continuing. At least: **M1** field 29 written as 0/1 instead of 1/2; **M2** field 29 decoded with on/off swapped; **M3** field 27 and 28 swapped in
    the encoder; **M4** the same swap in the decoder; **M5** field 11 written with the inverted value; **M6** field 15 dropped from the Connect read;
    **M7** a new write applied before its `RESPONSE`; **M8** a new field allowed past the Safe-Mode gate on an unverified firmware; **M9** the content
    description removed. Each must fail at least one test; record which.
17. Compliance: no `INTERNET`, no new permission, no manifest or version-catalog change, no new dependency
    (`git diff --name-only -- '*.toml' '*AndroidManifest.xml'` empty; the only Gradle change is the version of **REL**, if chosen); every
    `transport.send` call site in the diff listed (expected: none new); nothing on DLCI 0x08/0x0a; only fields 11, 15, 27, 28, 29 added to the codec;
    every write passes the ADR-042 gate; AGPL headers on any new Kotlin file; no MAC address at `INFO` or above and no raw payload outside debug mode
    (`AGENTS.md` §9).

### Phase I — the hardware-run skeleton (RUN) and the test plan

18. Create the capture folder and its `…-EVENT-NOTES.md` skeleton in the layout of `CAP-068-EVENT-NOTES.md` (build and metadata, preparation with
    the lessons of `CAP-063`…`CAP-068`, start, steps with expected screen and expected HCI bracket, don'ts, collection, analysis checklist with
    Test-IDs and "Refuted if" lines). Register the capture *planned* in `id_registry.csv`, add its Group section and Capture Index row
    (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`), and new Test-IDs where a step has none (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, registered). Contents:
    - per switch: the value shown at Connect (the read), OFF → the expected `WriteSetting` bytes → `RESPONSE` OK, ON → the same, then a reconnect
      that reads the last value back; for Multipoint also the SASS flags if the app holds the Message Stream (`07 11 00 04 01 02 98 00` off / `b8` on,
      ADR-053); for Case sounds where the Buds and the case are, and — as an observation — whether the case still sounds when a bud is put back with
      "Earbuds replaced" off; for Volume EQ and Head gestures what the maintainer hears or sees, as an observation;
    - the three uncaptured channel forms of §2 as named steps (and, if no step can force the channel, say so and record whichever channel occurs);
    - a write attempted in Safe Mode or while the session is closed, if a step for that exists in the earlier skeletons;
    - the screen-reader text, checked with TalkBack on film or by a screenshot of the semantics — say which;
    - every `TODO.md` §2 item that fits (Phase A task 7), each with its expected bytes or screen;
    - P1 with `dumpsys package … | grep -E "firstInstallTime|lastUpdateTime"`: this run is an **update over 1.0.1**, so record both times.
    Keep the run to one sitting; if it does not fit, propose a split at the end of the session (a question, not a silent cut).
19. `APP_TESTPLAN.md`: a new section for this build (after section S), one step per switch and for the screen-reader text; correct step A5's wording
    as `TODO.md` §2 asks.

### Phase J — documentation and release preparation

20. Documentation, only what changed: `ARCHITECTURE.md` (§5a rows for fields 11, 15, 27, 28, 29: built; §2.4 for the tabs; §3.1/§3.2 only if a rule
    or a constant changed — as dated current text, rule 9a), `PROJECT.md` (the feature list and "Status after 1.0.x"), `CHANGELOG.md`
    (`[Unreleased]`, or the 1.1.0 block if **REL** was chosen), `README.md` (status and feature list), `TODO.md` (finished items deleted, open ones
    kept, new ones added), `ai-sessions/INDEX.md` (the 0074 row). `PROTOCOL.md` and `DECISIONS.md`: nothing, unless approved in this chat.
21. **REL, only if chosen at the checkpoint:** `RELEASING.md` §4 steps 1–2 — `versionName` 1.1.0 and `versionCode` 10100 in
    `android/app/build.gradle.kts`, the `CHANGELOG.md` block marked "not yet released" with its Known issues (among them: the three channel forms not
    yet seen on the wire, until the run records them), `README.md`, `scripts/release_notes.template`. Do **not** run `scripts/release.sh`, create a
    tag, or publish; print the commands of `RELEASING.md` §5–§6 for the maintainer and run none.
22. Run `python3 scripts/ensure_footers.py` and `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py` — it must exit 0
    (`scripts/__pycache__/lint_docs.cpython-314.pyc` is tracked; `git checkout` it if it changed).

### Phase K — finish

23. Finish the RESULT: a plain-language summary first (what the app user will see differently; what the maintainer has to do next, step by step),
    then per item what was built and where, the fixtures with their bytes and commands, the checkpoint answers verbatim, the gate table, the
    mutation table, compliance, the run skeleton as a table (step · action · expected screen · expected HCI bracket · Test-ID), the `TODO.md` §2 items
    taken and not taken, external sources (URL + quoted sentence), what was read in full and in part, and the two closing sections **"Deferred
    documentation"** (each item also in `TODO.md`, in the same words) and **"Commits"**. Status per `AI_SESSION_LOG_PROCEDURE.md` §4 and §4b.
24. Show the maintainer a short summary and **ask whether to commit, push and open a pull request**. Only after a yes: Conventional Commits, one
    commit per concern, each building and passing on its own (`PROJECT_RULES.md` rule 16) — e.g. one `feat(app)` per switch with its tests, one for
    the screen-reader text, `docs` for the documents, `docs` for the run skeleton and the registry, `chore(release)` for **REL**, `docs` for the
    session files — each with a *why* and ending with the attribution line from the session's system reminder; `git fetch` and rebase before
    pushing, never force; nothing from a build directory, `android/.kotlin/`, `dist/`, `.vscode/` or `__pycache__` staged. The pull request is not
    merged by the agent unless the maintainer says so.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Wire discipline.** Exactly ten additions: `ReadSetting 4:N` for N ∈ {11, 15, 27, 28, 29} inside the existing Connect read and pull-to-refresh, and
  `WriteSetting 4:{N:v}` for the same five after a user tap — byte-identical to the official app's captured request for the same value and channel
  where one exists, on the announced channel with its ADR-034 address, through the ADR-042 gate. No other field, no subscription, no retry loop, no
  queue, nothing on another DLCI.
- **Honest state (`AGENTS.md` §5, `ARCHITECTURE.md` §3.1).** A value changes on screen only after the Buds' `RESPONSE` OK; every value keeps its own
  receive time; a failed write leaves the previous value with the reason; a value that was not read, or not understood, is "—" — never a guess. No text
  claims an audible or behavioural effect that no capture or observation supports.
- **Approvals only in this chat.** Never promote, demote or correct a 🟢 FACT, and never write or update an ADR, without the maintainer's approval
  given in this chat (`AGENTS.md` §6). The four ADRs cover the build and §3 fixes the tab of four switches; texts, the layout within the tab, the version and anything Phase A turns up are confirmed at the
  checkpoint.
- **Tests with real bytes (`AGENTS.md` §11).** Fixtures come from the logs with frame number, time and command in a comment; a hand-built frame only
  as a labelled supplementary structural test next to real fixtures (the three uncaptured channel forms). Redact device identifiers.
- **Evidence.** Zero creativity with hex (`AGENTS.md` §13 step 6); every claim in the RESULT links to a frame, a log line or a `file:line`; a
  negative needs its command, exit status and a positive control (step 8); external claims cite the URL and the exact sentence.
- **Scope.** `PROJECT.md`, the four ADRs and §1. Everything else is a RESULT note, never a silent addition. No dependency, Gradle-plugin or
  toolchain change.
- **Subagents.** A subagent may only read and report; every write is done in the main session and verified; re-derive any number a subagent
  reports, and say in the RESULT what was taken over without re-derivation.
- **Files.** Before deleting, moving or renaming any file, look at the target and diff a fresh listing; never `rm -rf` from memory.
- **Release.** No tag, no `scripts/release.sh`, no GitHub release, nothing public. The release verdict belongs to the session that analyses the
  hardware run.
- **Commits.** Only after the maintainer confirms the final summary (task 24).

---

## 6. Deliverables

- **ai-sessions/0074_FEATURE_RESULT_2026_10_06.md** — Progress block, plain-language summary, per-item record with fixtures, checkpoint answers,
  gate/mutation/compliance tables, the run skeleton's table, "Deferred documentation", "Commits", footer.
- The app with five new switches and the screen-reader text; their tests; a green gate.
- The hardware-run skeleton, registered *planned*, with the open `TODO.md` §2 items it takes and the three channel forms it must record.
- The documents that follow the code; the release preparation for 1.1.0 if chosen.
- Nothing left silently open: every item built, moved to the run, registered in `TODO.md` for a named later session, or "awaiting maintainer" with
  the question asked.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0074_FEATURE_PROMPT_2026_10_06.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0074_FEATURE_PROMPT_2026_10_06
