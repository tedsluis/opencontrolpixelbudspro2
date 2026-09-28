# 0054_FEATURE_PROMPT_2026_09_28.md — Build the `CAP-063` improvements I-1, I-2, I-3, I-5 and I-4 (ANC re-check on a disabled tap, the background loss wording, balance "Centre", the Digital-assistant note, stale per-bud lines) with real `CAP-063` bytes as fixtures, and write the Group AZ re-test skeleton

**Number:** 0054
**Category:** FEATURE
**Date:** 2026-09-28
**Title:** Implement the improvements the maintainer chose in `ai-sessions/0053` (RESULT §6, §9: I-1, I-2, I-3, I-5, I-4, in that order), based on `CAP-063-FINDINGS.md` §3/§6/§7 — nothing new on the wire except I-1's ordinary claim, real `CAP-063` bytes as test fixtures, a green test/lint gate with mutation checks, and a Group AZ capture skeleton (`CAP-064`) that re-tests them and adds AY-3

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer, Android/Kotlin engineer and technical auditor. This prompt is for a **fresh** Claude Code
session in this repository. Run the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8).** Before any other action, take the time to read **in full**, in
this order, to understand the ground rules before touching the project: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`,
`PROTOCOL.md`, `DECISIONS.md` (**every** ADR, ADR-001 … ADR-045, with every dated Update — including the 2026-09-28 Updates of ADR-024 and ADR-043),
`TODO.md`. Then read `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`, **`ai-sessions/0053_CAPTURE_RESULT_2026_09_27.md` in full** (the source of
every item below — especially §1, §3, §6, §7 and §9), **`captures/CAP-063-2026-09-27_15-57-33_16-25-03-Group_AY/CAP-063-FINDINGS.md` in full**
(especially §3, §4, §6, §7), `CAP-063-EVENT-NOTES.md` (timeline and step mapping), `ai-sessions/0048_FEATURE_RESULT_2026_09_25.md` §4, §7–§9 (the I-3
and I-7 designs this session changes), `ai-sessions/0052_FEATURE_RESULT_2026_09_26.md` §4–§9, `APP_TESTPLAN.md` and `id_registry.csv`.

Read every Kotlin file you change **in full** before changing it — at least `BudsRepository.kt`, `BudsRepositoryImpl.kt`, `BudsRepositoryImplTest.kt`,
`SessionLoss.kt` (`:domain`) and its tests, `SessionReopener.kt`, `SessionDiagnostics.kt`, `OsConnectionObserver.kt`, `DeviceInfo.kt`
(`AncAvailability`), `BatteryStatus.kt`, `BudsError.kt`, `AncScreen.kt`, `AncTileService.kt`, `ConnectionScreen.kt`, `EqScreen.kt` (`BalanceSlider`),
`SettingsUi.kt`, `ControlsScreen.kt`, `MainActivity.kt`, `FakeBudsTransport.kt`, and the fixture files (`Cap061Fixtures.kt`, `Cap062Fixtures.kt`,
`SettingsFixtures.kt`). Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
**ai-sessions/0054_FEATURE_RESULT_2026_09_28.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and update that
block at the end of every phase **and** after every item built (which items are done and tested, which files are touched but not yet tested, the
last gate result, where intermediate results live — the scratchpad directory: decoded frames, mutation script; re-create them if the scratchpad is
gone). A resumed session reads this prompt, then the RESULT's Progress block, re-reads the files the unfinished step touched, and continues from
there. It never redoes a finished, recorded step, and never assumes an unrecorded step was done. The prompt keeps its number and date; the RESULT
keeps growing in the same file.

**Nothing is taken on trust, including this prompt and `ai-sessions/0053`.** Re-derive every frame and byte you use from the capture with the
commands of `CAP-063-FINDINGS.md` (rule 4a) before it becomes a fixture; if anything here disagrees with the capture, the capture wins — record the
correction in the RESULT.

---

## 1. What the maintainer asked for (chat, 2026-09-28, translated)

1. Implement the improvements named in `ai-sessions/0053_CAPTURE_RESULT_2026_09_27.md` §6, which are based on the findings of `CAP-063-FINDINGS.md`.
   The maintainer's choice (0053 §9, `AskUserQuestion` "Volgende"): **I-1, I-2, I-3, I-5, I-4 — in that order.**
2. Read the core files first (§0) to understand the ground rules before auditing or changing anything.
3. Run everything in phases, strictly sequentially; be able to resume automatically after a token limit (§0).
4. Use §7 "Draft outline for the next FEATURE prompt" of the 0053 RESULT as the outline — this prompt is that outline, worked out.

---

## 2. The items (from `ai-sessions/0053` §6 — verify each against the capture and the code before building)

| # | Problem (evidence) | Change | Where | Wire |
|---|---|---|---|---|
| **I-1** | ANC stays disabled after the buds are worn again, until Refresh or a reconnect: the `Notify` 4774 (16:06:11.245, Settable `00`, buds on the table) set `AncAvailability.NOT_ALLOWED`; the buds were worn again from ≈ 16:06:48 but no claim followed until 16:12:33 (`CAP-063-FINDINGS.md` §3; `BudsRepositoryImpl.setAncMode` returns `AncNotAllowed` before any claim) | A mode tap (ANC screen **and** tile) while `NOT_ALLOWED` does a **normal claim** with `Get` (`08 11`); if that claim's `Notify` reads Settable non-zero (`e8`), the `Set` is sent **in the same claim** and counted as done only on the ACK (unchanged rules); if it reads `00`, nothing more is sent and the existing message is shown. The buttons stay tappable while `NOT_ALLOWED`, with a wording that says a tap re-checks (texts: checkpoint) | `BudsRepositoryImpl` (`setAncMode`, the claim/`Get` path), `AncScreen.kt`, `AncTileService.kt`, `ARCHITECTURE.md` §3.1 ANC row | the existing claim content only: `SABM` 0x04, `08 11`, then `08 12 …` only after an `e8` `Notify` — no new message type |
| **I-2** | A session loss while the app is not visible keeps the "undetermined" sentence "The Maestro channel (equalizer) was closed. Tap Connect to reconnect." although Android's link was down: loss 16:15:22.008 (export line 589), observer stopped 16:15:21.408 (586), first reading `NOT_CONNECTED` 16:15:25.062 (593) — 3.05 s after the loss, outside `classifySessionLoss`'s window (`CAP-063-FINDINGS.md` §6 #8) | When the cause is still undetermined, the **first link reading after the app becomes visible again** (or after the loss, whichever is later) decides the wording: `NOT_CONNECTED` → the "Android no longer shows the Buds connected …" text; `CONNECTED` → the "Buds closed …" text; both marked as read **now** ("… (checked when you returned to the app)" — texts: checkpoint). Readings older than the loss are never used (unchanged `0048` I-7 rule) | `SessionLoss.kt` (`:domain`), `BudsRepositoryImpl.onAndroidLink`, `ConnectionScreen.channelLostMessage`, `ARCHITECTURE.md` §6.0b loss-cause bullet | none |
| **I-3** | "Centre" practically unreachable: 12 releases near the centre gave `17:21, 19, 7, 1, 1, 3, 22, 14, 11, 6, 7, 1` (frames 5180 … 5245), never 0; the slider has 201 positions (`EqScreen.kt` `BalanceSlider`) | On release, snap to 0 within ±N (N = 3 proposed; checkpoint), or a small "Centre" control — the maintainer chooses at the checkpoint. "Centre" is still shown for 0 only | `EqScreen.kt` (`BalanceSlider`), `SettingsUi.kt` | `WriteSetting 4:{17:0}` — a value inside ADR-045's −100 … +100 |
| **I-5** | "Digital assistant" does nothing on this phone: no `AT+BVRA`, no AVRCP, and the GSND channels (DLCI 0x08/0x0a) were closed during the whole hold test (`CAP-063-FINDINGS.md` §7) | Keep the option; add a one-line note under "Press and hold" (proposed: "Digital assistant works only with an assistant app that supports headphones (for example the Google app); otherwise the Buds just play a tone." — text: checkpoint). No detection of an assistant app (reading the assistant role needs a privileged permission) | `ControlsScreen.kt` | none |
| **I-4** | For ≈ 2 s after Connect the previous session's per-bud lines stay ("charging in the case (16:00:17)" on film 16:01:02–03) until the new claim (battery 3046, 16:01:03.872) and stream (3059, 16:01:04.208) arrive | At Connect mark the per-bud charging lines "from the last connection" (like the Case's "last seen") until the first new report of either source | `BudsRepositoryImpl` (Connect reset), `BatteryStatus.kt`, `ConnectionScreen.kt` (`budLine`) | none |

**Fixtures (real `CAP-063` bytes, re-derived with the commands of `CAP-063-FINDINGS.md`; the frame number, time and command in a comment —
`AGENTS.md` §11 "Fixtures are real bytes"):** 4774 (`08 13 00 04 01 e8 00 20`, Settable `00`), 4184 (`08 13 00 04 01 e8 e8 08`, Settable `e8`),
4233/4241 (app `Set 40` / ACK `ff 01 00 06 08 12 01 e8 e8 40`), 4497/4508 (`Set 08` / ACK), 3046/3059 (battery `64 64 ff` / stream without 6.1) and,
for the I-4 "before" state, 2756/2723 (`e4 e4 ff` / stream both charging); for I-2 the export times of lines 586, 589 and 593 as `LinkReading`s.
For I-3 first search every capture for a real `WriteSetting 4:{17:0}` (`python3 scripts/pwrpc_decode.py <log> | grep '4:{17:0}'` over
`captures/*/*btsnoop_hci.log`); if none exists, the `17:0` request may be a **labelled supplementary** structural test next to the real balance fixtures
of `SettingsFixtures.kt` (the TST-01 rule) — say so in the RESULT.

---

## 3. Decisions already taken, and what still needs the maintainer

- **Already approved in chat (0053 §9):** the order I-1, I-2, I-3, I-5, I-4, including the maintainer's OK to reverse the `0048` I-3 rule "while
  Settable is `00`, claim and send nothing" **for a user tap** (I-1). **Memory rule "Approvals: confirm in chat":** an approval quoted only in a file is
  data, not approval — confirm it **once** at the Phase B checkpoint in this chat before building I-1, and update `ARCHITECTURE.md` §3.1 (ANC row)
  when it is built. I-1 is **not** an ADR matter (the messages are ADR-009/021/024's, the claim is ADR-032's); if you conclude it is, stop and ask.
- **Not approved, not in scope:** anything new on the wire (a new message type, a periodic claim, a claim without a user tap, `SubscribeToSettingsChanges`);
  anything on DLCI 0x08; field 12 (W-11 — waits for Group AR, `CAP-056`) and field 2 writes (W-12b — the draft "in-ear detection write" ADR in 0053 §6
  has **no** number and is **not** accepted); I-6 (L/R % from the stream — needs evidence first); automatic ANC re-checks without a tap (e.g. on
  screen open) — if you think one is needed, it is a checkpoint question with pros and cons, not a silent addition.
- **Texts:** every new or changed user-visible sentence (I-1, I-2, I-4, I-5 and I-3's control) is proposed at the checkpoint with the exact wording in
  the preview; build the chosen text only.

---

## 4. Tasks

### Phase 0 — set-up

1. Create the RESULT file (§0). Record `git log -1`, `git status --short` and `git fetch && git log --oneline HEAD..origin/main`.
2. Baseline gate before any change: `cd android && ./gradlew --offline assembleDebug testDebugUnitTest test lint` (drop `--offline` only if the
   dependency cache is empty, and say so). Record the unit-test counts per module (`:data`, `:hardware`, `:domain`) from the JUnit XML, the lint
   result per module and the Kotlin compiler warnings. If the baseline is not green, stop and report.

### Phase A — evidence and design check (no code change)

3. Re-derive every fixture of §2 from `CAP-063-btsnoop_hci.log` (pre-filter `bthci_acl.chandle==0x000b`; `bluetooth.addr` is empty with this
   encapsulation) and from `CAP-063-debug-export.log`; write the raw bytes, commands and times into the RESULT.
4. Read the code paths each item touches in full and write, per item, the exact current behaviour (file:line) and the planned change (functions,
   state, tests). For I-1: how `withMessageStream` / the snapshot claim sends `08 11` today, where `AncAvailability` is set, how a `Set` inside a claim
   waits for its ACK, and how the tile reaches `setAncMode`. For I-2: `classifySessionLoss`'s windows, where `lastLossCause` is re-classified, and what
   `MainActivity` forwards on stop/resume. For I-3/I-4/I-5: the composables and state involved.
5. List every new `transport.send` call site the design would add (expected: **none**; I-1 reuses the claim/`Get`/`Set` paths). If a new call site
   is unavoidable, name the ADR that covers it or stop and ask.

### Phase B — checkpoint (stop and ask, in this chat, via `AskUserQuestion`)

6. One question per decision; each option with pros and cons, one marked "(Recommended)", the exact texts in the preview:
   - **I-1 confirmation** — reverse `0048` I-3 for a user tap (the 0053 §9 approval, confirmed here in chat); the ANC-screen and tile wording while
     `NOT_ALLOWED`; what a refused re-check shows.
   - **I-2 wording** for the re-classified cause (link down / Buds closed, "checked when you returned").
   - **I-3** snap-to-centre (±N, N proposed 3) vs a "Centre" control.
   - **I-5 and I-4 texts.**
   Record the answers verbatim in the RESULT. Build only what was chosen.

### Phase C — build I-1 (ANC re-check on a disabled tap)

7. Implement per the checkpoint. Tests (JVM, `FakeBudsTransport`, real `CAP-063` bytes): (a) `NOT_ALLOWED` from 4774, tap ADAPTIVE, the claim's
   `Notify` = 4184 (`e8`) → exactly one `Set` byte-identical to 4233, applied on the ACK 4241; (b) the claim's `Notify` = 4774 (`00`) → **no** `Set`,
   the `AncNotAllowed` result, availability stays `NOT_ALLOWED`; (c) a claim that fails to open → its error, no `Set`; (d) the tile path behaves the
   same; (e) with availability `ALLOWED`/`UNKNOWN` nothing changes from today (existing tests stay green). Update `ARCHITECTURE.md` §3.1's ANC row.

### Phase D — build I-2 (loss cause after returning to the app)

8. Implement per the checkpoint in `:domain` first (pure function over `LinkReading`s + visibility), then the repository wiring and the text. Tests:
   the `CAP-063` export times (observer stopped 16:15:21.408, loss 16:15:22.008, `NOT_CONNECTED` 16:15:25.062 after resume → "link down"); a
   `CONNECTED` first reading after resume → "Buds closed"; a reading older than the loss is never used; the existing `CAP-062`-based `SessionLoss`
   tests stay green. Update `ARCHITECTURE.md` §6.0b's loss-cause bullet.

### Phase E — build I-3, I-5, I-4

9. I-3 in `BalanceSlider` (+ the repository/codec test for `17:0`, §2); I-5 the note in `ControlsScreen.kt`; I-4 the stale marking at Connect with a
   repository test (Connect → per-bud lines stale until the `CAP-063` 3046/3059 reports; a Connect whose first report is the stream still clears it).

### Phase F — gate, mutations, compliance

10. `./gradlew --offline clean`, then the full gate of task 2: all green, no new lint issue, no new suppression, no new compiler warning; record the
    new test counts.
11. Mutation checks (apply with a script, run `:data:testDebugUnitTest` / `:domain:test`, restore, verify byte-identical with `sha256sum`), at least:
    M1 I-1 sends the `Set` without waiting for an `e8` `Notify`; M2 I-1 sends the `Set` after a `00` `Notify`; M3 I-2 uses a reading older than the loss;
    M4 I-2 ignores the post-resume reading (stays undetermined); M5 I-4 never clears the stale mark. Each must fail at least one test.
12. Compliance: no `INTERNET`, no new permission, no manifest/Gradle/version-catalog change
    (`git diff --name-only -- '*.gradle.kts' '*.toml' '*AndroidManifest.xml'` empty); list every `transport.send` call site in the diff (expected: none
    new); nothing on DLCI 0x08; no field 12 or field 2 write; every write still passes `writeGate` (ADR-042); AGPL headers on any new Kotlin file.

### Phase G — re-test skeleton (Group AZ) and documentation

13. Write the capture skeleton **captures/CAP-064-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AZ/CAP-064-EVENT-NOTES.md** (to be created) in the layout of
    `CAP-063-EVENT-NOTES.md` Appendix A (preparation P1–P14 with the lessons of `CAP-063`: film a minute change at start **and** end, say the commit hash
    and Play services' *Nearby devices* state aloud, **Do Not Disturb on**, keep the ears **visible on film** for the wear steps, an events file), with
    steps, expected screen and expected HCI bracket for: I-1 (buds on the table → ANC disabled; put them in → tap ADAPTIVE → one claim `08 11` →
    `08 13 … e8` → `08 12 … 40` → ACK); I-2 (Home, both buds into the case with the lid open so the ACL drops while the app is in the background, wait
    10 s, return → the link-down text); I-3 (balance centre → `4:{17:0}` → OK, "Centre"); I-4 (Disconnect, change a bud's dock state, Connect → stale
    lines until the new report); I-5 (read the note; hold = Digital assistant once with the Google app's service running and once after it stopped, if
    the maintainer wants the GSND check — `CAP-063-FINDINGS.md` §7); **AY-3** (one bud visibly in an ear, the other visibly on the table, ANC Refresh
    each time, then swap — the test of "Settable `0x00` = no bud worn", ADR-024 Update); and the steps `CAP-063` skipped (F7, K1–K5, L3, A5, B4, (E)).
    **W-12b's OHD-off steps only if the maintainer chooses them at the Phase B checkpoint** — the app cannot write field 2, so they need the official
    app on the Pixel 7a (then they belong in Group AR's capture, `CAP-056`) — say so. Add the "Refuted if" list. `CAP-064` is already registered as *planned* in
    `id_registry.csv` (by the chat that wrote this prompt); update its row text if the skeleton's scope differs. Do not add Group AZ to `CAPTURE_BLUETOOTH_HCI_SNOOP.md` unless asked.
14. Documentation, only what changed: `ARCHITECTURE.md` (§3.1 ANC row, §6.0b loss-cause bullet, any UI text description), `APP_TESTPLAN.md` (F8, G3,
    C8/E-section and M3 as the new behaviour requires; a header note "updated for `ai-sessions/0054`"), `TODO.md` (done items; open items stay),
    `CHANGELOG.md`, `README.md` (status block), `ai-sessions/INDEX.md` (the 0054 row). **No** `PROTOCOL.md`/`DECISIONS.md` change unless the maintainer
    approves one in this chat. Run `python3 scripts/ensure_footers.py` and `python3 scripts/lint_docs.py` — it must exit 0.
15. Finish the RESULT: a plain-language summary first (what the user will see differently), then per item what was built and where, the gate
    table, the mutation table, compliance, the fixtures with their bytes, the Group AZ re-test summary, open items. Set the Status per
    `AI_SESSION_LOG_PROCEDURE.md` §4.
16. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes: Conventional Commits, one commit per concern
    (`feat(app)` for the code and tests; `docs` for the documentation, the skeleton and the session files), each with a *why* and ending with the
    attribution line from the session's system reminder; `git fetch` and `git log origin/main..HEAD` before pushing (rebase if CI added a commit, never
    force); nothing from a build directory, `android/.kotlin/`, `.vscode/` or `__pycache__` staged.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Wire discipline.** No new message type, no new automatic claim, nothing on DLCI 0x08, no field 12 or field 2 write, no `SubscribeToSettingsChanges`.
  I-1 sends only what an ANC tap's claim already sends (ADR-009/021/032), and only after the user's tap.
- **Honest state (`AGENTS.md` §5, `ARCHITECTURE.md` §3.1).** A value changes on screen only after the Buds' answer; every value keeps its own receive
  time; a stale value is marked stale, never presented as new.
- **Approvals only in this chat.** Never promote, demote or correct a 🟢 FACT, and never write or update an ADR, without the maintainer's approval
  given in this chat (`AGENTS.md` §6). Behaviour choices (I-1's reversal, all texts) are confirmed at the Phase B checkpoint.
- **Tests with real bytes** (`AGENTS.md` §11): fixtures from `CAP-063` (or earlier captures) with frame number, time and command in a comment; a
  hand-built frame only as a labelled supplementary structural test next to real fixtures.
- **Evidence.** Zero creativity with hex (`AGENTS.md` §13.6); every claim in the RESULT links to a frame, log line, film time or file:line.
- **Scope.** Stay within `PROJECT.md` and the five items; anything else (I-6, W-7, W-11, W-12a/b, an automatic re-check) is a checkpoint question or a
  RESULT note, never a silent addition.
- **Subagents.** A subagent may only read and report; every write is done in the main session and verified.
- **Files.** Before deleting, moving or renaming any file, diff a fresh listing of the source against the copy (checksums); never `rm -rf` from memory.
- **Commits.** Only after the maintainer confirms the final summary (task 16).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0054_FEATURE_PROMPT_2026_09_28.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0054_FEATURE_PROMPT_2026_09_28
