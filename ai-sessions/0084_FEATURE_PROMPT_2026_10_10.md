# 0084_FEATURE_PROMPT_2026_10_10.md — Rebuild 1.2.0: hold the Message Stream claim while the noise-control tab is on screen (ADR-061), plus the other fixes `CAP-072` named — real capture bytes as fixtures, a green gate, the re-test skeleton and the release preparation

**Number:** 0084
**Category:** FEATURE
**Date:** 2026-10-10
**Title:** Rebuild OpenControl 1.2.0 (same version, versionCode 10200; nothing was published) with (1) `DECISIONS.md` ADR-061 — the app keeps its DLCI 0x04
claim open while the noise-control tab is on screen, so a press-and-hold on a bud reaches the app and shows "Changed by the Buds"; (2) the `HardwareInfoFixtures`
swap to OpenControl's own `GetHardwareInfo` exchange from `CAP-072`; (3) a finer balance slider (the maintainer's choice at the checkpoint); (4) the documentation
fixes `CAP-072` found (`CHANGELOG.md` / README / release-notes wording of "Changed by the Buds" and the worn-line limit, the full serials in `PROTOCOL.md`); and
(5) the skeleton of the re-test capture `CAP-073` (Group BI), built as the **minimal release run** plus the ADR-061 hold test and the film-2 items `CAP-072` did
not run. Real capture bytes as fixtures, a green test/lint gate with mutation checks, the documents that follow the code, the release preparation (no tag, no
publication).

---

## 0. How to use this prompt

You are an expert software architect, Android/Kotlin engineer, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in
this repository. Run the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action, read **in full**, in this order: `AGENTS.md`,
`PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md` (in particular §2.2a with its Update of 2026-10-10, §4.1 "Notify ANC state" and the Settable
bullet, §4.5.7 balance, §6), `DECISIONS.md` (**every** ADR, ADR-001 … ADR-061, with every dated Update — in particular ADR-009, ADR-021/022, **ADR-032**,
ADR-042, ADR-044, ADR-045, ADR-048, ADR-049, ADR-057, **ADR-058 and ADR-059 with their Updates of 2026-10-10**, and **ADR-061**), `TODO.md` (§2, §4, §5, §6).
Then: `AI_SESSION_LOG_PROCEDURE.md` (§4b, §9), `ai-sessions/INDEX.md`, `RELEASING.md` (the Release checklist, "Repository rules", §4–§8, §11, §13),
`APP_TESTPLAN.md` (§V and its `CAP-072` run note, the Summary), `CHANGELOG.md` (`[1.2.0] - not yet released`), `README.md` (status, known issues),
`id_registry.csv`, and:

- `ai-sessions/0083_CAPTURE_PROMPT_2026_10_09.md` and `ai-sessions/0083_CAPTURE_RESULT_2026_10_09.md` — the findings, the checkpoint answers and the
  testing-burden proposal this build follows;
- `captures/CAP-072-2026-10-09_17-27-26_18-23-39-Group_BH/CAP-072-FINDINGS.md` — §2 (the `GetHardwareInfo` exchange), §3 (the balance writes), §4 (the worn
  line), **§5 (the press-and-hold result and the hypotheses)**, §10 (the verdict), §12 (the improvements); and `CAP-072-EVENT-NOTES.md` (the timeline 17:44–17:52
  and 18:07:49–18:08:57, the step mapping, the skeleton appendix — the layout of the next skeleton);
- `ai-sessions/0082_FEATURE_PROMPT_2026_10_09.md` and `ai-sessions/0082_FEATURE_RESULT_2026_10_09.md` — the pattern of the last build (item 1 "Changed by the
  Buds", §A.1; the fixtures; the gate and mutations, §H; the skeleton, §I);
- `CAP-045-FINDINGS.md` (the unprovoked `Notify` frames 1583, 1755, 1818 — `Cap045Fixtures.kt`), `CAP-064-FINDINGS.md` §2–§3 (holds 9737–9793 with a client
  holding the channel), `CAP-071-FINDINGS.md` §3 and `CAP-070-FINDINGS.md` (balance `17:7` = "Right 4").
- **Every Kotlin source this build touches, in full, before changing it** — at least: `BudsRepositoryImpl.kt` (the claim code: `withMessageStream` ≈ `:1315`,
  `scheduleRelease` ≈ `:1359`, `launchInitialSnapshot`, `causeOfNotify` ≈ `:356`, `handleRoutedFrame`, the constants), `BudsRepository.kt`,
  `RfcommBudsTransport.kt` (`openChannel`/`closeChannel`, the channel-closed signal), `SessionReopener.kt`, `AncModeCause.kt`, `AncScreen.kt`,
  `OpenControlNavHost.kt` (`Routes.ANC`, the tab destinations), `MainActivity.kt` (`onAppVisible`, the pull actions), `AncTileService.kt`, `EqScreen.kt`
  (`BalanceSlider` ≈ `:260`), `BudsSettings.kt` (`BALANCE_RANGE`, `snapBalance`), `HardwareInfo.kt`, `HardwareInfoFixtures.kt`, `HardwareInfoCodecTest.kt`,
  `Cap045Fixtures.kt`, `BudsRepositoryImplTest.kt`, `FakeBudsTransport`, `android/app/build.gradle.kts`.

Say in the RESULT what was read in full and what in part (`ai-sessions/0083` fell short of its list — do not repeat that).

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5).** Create **ai-sessions/0084_FEATURE_RESULT_2026_10_10.md** at the very start with
`**Status:** partial — resumed` and a **Progress** block; update it at the end of every phase and after every substantial step (each file changed, each test
class green, each gate run). A resumed session reads this prompt, then the Progress block, re-reads the files the unfinished step touched, and continues; it
never redoes a recorded step and never assumes an unrecorded one.

**Memory and the machine.** 15 GB laptop. Never two Gradle builds at once; run mutation checks one at a time with `--max-workers=2` and check for leftover
mutations after a crash (the memory "Gradle OOM on mutation runs"). In `BudsRepositoryImplTest` use `runCurrent()`/a settle helper for `backgroundScope`
collectors, not `advanceUntilIdle()` alone (the memory "Repo tests: runCurrent").

**Nothing is taken on trust**, including this prompt, `ai-sessions/0083` and the ADR text: re-derive every frame number, byte string and code location you
rely on from the capture logs and the sources.

---

## 1. What the maintainer asked for (chat, 2026-10-10, translated from Dutch)

1. "Write the FEATURE prompt for the 1.2.0 rebuild with ADR-061, with the other fixes that are named in `CAP-072`."
2. The maintainer's decisions of 2026-10-10 (`ai-sessions/0083` RESULT, Phase D, verbatim there) that bind this build:
   - verdict **fix first**; ADR-061 **accepted** ("Accept as ADR-061 now");
   - film 2 of `CAP-072` folded into the **rebuild's run** ("Fold into the rebuild's run");
   - the serial strings in the Debug-mode hex dump are **accepted** per ADR-058 item 5 ("Accept per ADR-058 item 5") — no redaction in the app;
   - shorter runs: **both tiers + a helper script + a run card** ("Both tiers + helper + card") — the helper is **its own later session**; this build writes the
     re-test skeleton as the minimal tier plus the extended steps ADR-061 needs, and proposes the plan-file changes;
   - **no `CHANGELOG.md` change was wanted in the CAPTURE session** ("No CHANGELOG change now") — it belongs to this FEATURE session.
3. "Commit and push only after I confirm" (standing practice) — and no tag, no release (`RELEASING.md`: publishing is the maintainer's own act).

---

## 2. The items (verify each against the captures, the ADRs and the code before building)

### Item 1 — ADR-061: hold the Message Stream claim while the noise-control tab is on screen (the main item)

**Why.** `CAP-072-FINDINGS.md` §5: from 17:44:34.090 (the app's `DISC`, `CAP-072-btsnoop_hci2.log.last` frame 11947) to 17:48:46.460 (`SABM`, 12490) no DLCI 0x04
claim existed, the noise-control tab was on screen, and the maintainer's press-and-holds changed the mode audibly — the Buds' `Notify` had no recipient. The
1.2.0 cause logic worked inside a claim (unprovoked `Notify` TRN at 13519, 17:51:10, shown as "Changed by the Buds").

**ADR-061's decision (read it, do not paraphrase it into something else):** while the noise-control tab is visible the app keeps its DLCI 0x04 claim open
(opened on entering the tab, by the existing claim code); released 1.5 s after leaving the tab or the app, or on a session loss. No background or periodic claim;
ADR-032's other items unchanged. An unprovoked `Notify` during the hold is classed `AncModeCause.CHANGED_BY_BUDS` as today.

**Design questions to settle in Phase A (propose; the maintainer decides at the checkpoint where a choice is real):**
- **Where "the tab is visible" comes from:** `OpenControlNavHost`'s current destination (`Routes.ANC`) combined with the app's visibility (`onAppVisible`,
  `MainActivity.kt` ≈ `:412`), the Settings overlay (`inSettings`), a dialog (the (i)) and the screen off. One `BudsRepository` entry point (e.g.
  `setAncTabVisible(Boolean)` — a name to propose), called from `:app` (ADR-048: no ViewModel), testable without Compose.
- **How the hold coexists with the existing claim code** (`withMessageStream`, `claimMutex`, `releaseJob`, `snapshotJob`, `refreshBattery`'s fresh claim, the
  tile's claim from another process context, Find's Ring/Stop): a tap while the hold is open must reuse the open channel (no second `SABM`), still send `Get`
  first (F-1 rule) and still release only when the tab is left; `refreshBattery` (it needs a **fresh** open for the battery burst, `:1322`) — keep its
  release-and-reopen inside the hold, or skip it while held? Propose with the wire consequence.
- **When the channel dies under the hold** (the Buds' `DISC` of DLCI 0x04 — `CAP-072` B 827; a session loss; Play services taking it back on a phone that has
  it): no automatic re-claim loop (ADR-032 "no reconnect loop"); at most one re-claim on the next user action or on re-entering the tab. Say exactly what the
  app does and logs.
- **The ANC state while held:** `getPending`/`setPendingMode` keep their meaning; a `Notify` with nothing pending and a new mode → `CHANGED_BY_BUDS`; the same mode
  again → no change (`causeOfNotify` ≈ `:356`). The (i) text and `CHANGELOG.md` wording follow what the code can now promise.
- **ADR-032 contention:** on a phone **with** Play services the hold locks Play's Fast Pair out of DLCI 0x04 while the tab is open (ADR-061 Consequences).
  Nothing to build for it beyond the release on leaving; say it in the docs and the (i) if the maintainer wants.
- **The tile** (`AncTileService`) and the notification: unchanged (their own short claims), but a tile tap while the tab is held must not close the hold.
- **Logging (`AGENTS.md` §9):** one always-on line when the hold starts and one when it ends, with the reason (tab left, app hidden, session lost, channel closed
  by the Buds); no payload.

**Unit tests (real bytes, `AGENTS.md` §11):** with `FakeBudsTransport` — entering the tab opens one claim (one `SABM` equivalent, the `Get` sent); the `CAP-045`
1583 unprovoked `Notify` arriving during the hold with nothing pending → `ancModeCause == CHANGED_BY_BUDS` and `ancMode` updated; a tap during the hold reuses
the channel and the `Notify` of its `Set` → `SET_BY_APP`; leaving the tab → `closeChannel` after `MESSAGE_STREAM_LINGER_MS` (virtual time), not before;
leaving the app → the same; a session loss during the hold → no re-claim by itself; re-entering the tab → one new claim. Add `CAP-072` frames as fixtures where
they fit: the unprovoked `Notify` A 13519 (`08 13 00 04 01 e8 e8 80` — re-derive the bytes) after the answer A 13515, and B 825 (TRN → NC) — label each with its
file and frame.

### Item 2 — The `HardwareInfoFixtures` swap (`TODO.md` §5)

Replace the redacted official-app answers (`CAP-036` 1423 / `CAP-024` 832) by **OpenControl's own exchange** from `CAP-072-btsnoop_hci2.log.last`: request
A 7593 → answer A 7596 (channel 21, 17:29:27.052 → .087) and request A 8616 → answer A 8620 (channel 19). Re-derive the raw bytes with `tshark -r <log> -Y
"frame.number in {7593,7596,8616,8620}" -T fields -e frame.number -e data.data` and the scratch reassembly of `CAP-072-FINDINGS.md` (the answers span several
RFCOMM frames — reassemble before you redact); X-out the serial characters the same way the current fixtures do (first 4 + last 2 kept), recompute nothing that
the redaction does not touch, and say in the fixture's KDoc that the CRC no longer matches after redaction (or keep the CRC check out of that test, as today —
check what `HardwareInfoCodecTest` does). Keep the request fixtures byte-identical to the wire (they hold no serial). The tests must still pass unchanged in
meaning.

### Item 3 — A finer balance slider (UX; the maintainer chooses at the checkpoint)

`CAP-072` 18:07:49–18:08:57: 30 `WriteSetting 4:{17:…}` on one attempt to land on "Right 4" (labels seen: Right 6, Left 9, Right 53, Right 8 …; `17:7` was never
sent). The slider spans −100 … +100 over the card width (`EqScreen.kt` `BalanceSlider` ≈ `:260`, `BudsSettings.BALANCE_RANGE`, `snapBalance`). Propose options
with their pros and cons, e.g. (a) `Slider(steps = …)` in steps of 1 within ±10 and coarser beyond (non-linear mapping); (b) − / + buttons beside the slider
(one write per tap, the value shown); (c) a smaller range shown (±20) with a "more" toggle; (d) leave it. Every option still writes once on release (ADR-045),
never during the drag. The official app's own slider range and steps, if a capture or the APK shows them (`PROTOCOL.md` §4.5.7, ADR-026), are evidence for the
choice — check, do not assume. Build only the chosen option; its unit test (the value → zigzag request) uses `CAP-064` 6671 / `CAP-070` A3747 (`17:7`).

### Item 4 — Documentation fixes `CAP-072` named

- **`CHANGELOG.md` `[1.2.0] - not yet released`:** "Changed by the Buds" described as it now works (a change the Buds report while the noise-control tab is open,
  or within ≈ 1.5 s of another app action); the hold of ADR-061 under Changed; Known issues: the worn line can say "Probably worn" for 40 s or more with both buds on
  a table (`CAP-072` §4); the balance-slider change if built. **Not its date.**
- **`README.md`:** the status paragraph of 1.2.0 and the known issues (not "Latest release").
- **`scripts/release_notes.template`:** the 1.2.0 summary line and known issues to match `CHANGELOG.md` (a `scripts/` file — this session may edit it as part of
  the release preparation, A3; `dist/` is never touched).
- **`PROTOCOL.md`:** the three component serials written in full (§2.2a ≈ line 388, §6 ≈ lines 2791 and 3053 — re-locate them) → first 4 + last 2 characters
  (ADR-058 item 5). This changes text, not status: it still needs the maintainer's approval at the checkpoint (`AGENTS.md` §6 does not cover it, but
  `PROJECT_RULES.md` rule 9a/the convention of dated changes does — propose the exact wording, then apply).
- **`ARCHITECTURE.md`:** §6.0b / §3.1 / §3.2 (the claim model and the timing constants) gain the ADR-061 hold; §5a's row for ANC.
- **The (i) text of the ANC card** (`AncScreen.kt`) and the worn line's (i) mentioning `CAP-064` (`TODO.md` §5 item "reword without the capture id if the
  maintainer wants") — ask.

### Item 5 — The re-test skeleton `CAP-073` (Group BI) — the minimal release run plus the extended steps

Verify that `CAP-073` and Group BI are free (`id_registry.csv`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`). Write
captures/CAP-073-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BI/CAP-073-EVENT-NOTES.md in `CAP-072`'s skeleton layout (purposes, reference-frame table, P-steps with ★,
steps with "Expected on screen", "Expected on the wire", Test-ID and "Refuted if", Don'ts, After the run, analysis checklist), a `planned` registry row, a
Group BI section and a *planned* Capture Index row. **Keep it short** (the maintainer's request in `ai-sessions/0083`: "find out how testing can be simpler and
shorter"); target ≤ 20 minutes on film:

1. **Minimal release run (screen recording only, no camera):** P0/P1 as in `CAP-072` (the update **over the `ec6d163` 1.2.0** now installed — no uninstall);
   Info (build without "-dirty", firmware, serials); one Connect (the thirteen requests); one ANC tap; Bluetooth off/on; one export.
2. **ADR-061 (camera on the buds and the head for this part only):** the noise-control tab open → one claim stays open (no `DISC` while the tab is shown);
   **press and hold the Left bud, then the Right** → the unprovoked `08 13` arrives and the (i) shows "Changed by the Buds at HH:MM:SS"; a tap → "set …";
   leave the tab (Controls) → the phone's `DISC` of DLCI 0x04 within ≈ 1.5 s; Home with the tab open → `DISC`; return to the tab → a new claim; the tile while the
   tab is open → no second claim.
3. **The items `CAP-072` did not run:** T11 (Bluetooth off, `am force-stop`, three `uiautomator` dumps: Controls, Sound, gear → Settings), C12 (a tap during a
   re-open), S12 (rotate **during** Android's save dialog), H5 (*Read EQ again*), Volume EQ off/on, the worn line with in-ear detection **off** (with a pull), the
   balance "Right 4" with the new slider; P7 ★ Do Not Disturb **on** (`CAP-072` had a call); P9 ★ no app use before the film; the lid open (P8).
4. **Not in this run** (say so in the skeleton): the case-sound experiment (`TODO.md` §2 — its own capture with the official app and a microphone at the case),
   the wear sequences of `CAP-072` §4 (no app change touches them).

Reference bytes for every request the run makes: re-derive from `CAP-072` (channel 19 and 21 forms of the writes, the `GetHardwareInfo` exchange) and earlier
captures as `CAP-072`'s table did. Test-IDs: re-derive from the steps (`ANC-001`…`004`, `PAIR-003`, `FW-003`, `INEAR-006`, `EQS-001`, `AUDIO-002`,
`AUDIO-003`, …); `APP_TESTPLAN.md` gains a section **W** (the rebuild: W1 the hold, W2 the hold's release, W3 the fixture-independent checks, …) and the
Summary row.

### Item 6 — The shorter-run proposal into the plan files (proposal only)

`ai-sessions/0083` RESULT "Testing burden": write the **minimal release run** and the **extended run** as a **proposed** subsection for `APP_TESTPLAN.md` and
`RELEASING.md` (§11 or a new section) and a one-page **run card** (`docs`-level Markdown, printable) — show the exact text at the checkpoint; apply only what is
approved. The helper script (`scripts/`) is **not** written here (its own session; `TODO.md` §6 keeps it).

---

## 3. Decisions already taken, and what still needs the maintainer

Taken (do not reopen): ADR-061 as written; the version stays **1.2.0 / versionCode 10200** (nothing was published, `RELEASING.md` C4 "the same version again");
the serial hex in Debug-mode exports is accepted; the case-sound experiment is a separate capture; the helper script is a separate session.

For the checkpoint (Phase B, one question per decision, options with pros and cons, "(Recommended)" on one, exact text in the preview):
- Item 1's open design points (where the visibility comes from; `refreshBattery` during the hold; the behaviour when the Buds close 0x04 under the hold; the (i)
  wording);
- Item 3's option (or none);
- Item 4's texts (`CHANGELOG.md`, README, release notes, the `PROTOCOL.md` serial redaction, the (i) texts);
- Item 6's plan-file wording;
- whether `ARCHITECTURE.md` §6.0b's change needs an ADR-032 Update pointer (ADR-061 already records the decision — propose a one-line pointer Update to ADR-032
  only if the maintainer wants it; never write it without approval).

---

## 4. Tasks

### Phase 0 — set-up

1. Create the RESULT (§0). Record `git log -1`, `git status --short`, `git fetch` and `git log --oneline HEAD..origin/main`. Work on a branch from an up-to-date
   `main`: `git switch main && git pull --ff-only && git switch -c release/1.2.0-rebuild` — or reuse `release/1.2.0` if `git log main..origin/release/1.2.0` is
   empty (it was merged in #24): say which. `android/domain/bin/` is build output: never stage it.
2. Run the gate once **before** any change (`cd android && ./gradlew assembleDebug testDebugUnitTest test lint`, `python3 scripts/lint_docs.py`) and record the
   counts, so a later failure is attributable.

### Phase A — evidence and design check (no code change)

3. Re-derive Item 1's evidence from `CAP-072` (frames 11947, 12490, 13515/13519, B 825/827) and the code paths it touches; write the design (who calls what,
   which state, which log lines, which tests) into the RESULT.
4. Re-derive Item 2's bytes; Item 3's evidence (the official app's slider range if any); Item 4's text locations; Item 5's free IDs and reference bytes.

### Phase B — checkpoint (stop and ask, in this chat, via `AskUserQuestion`)

5. The questions of §3. Record the answers verbatim in the RESULT. Do not build an option that was not chosen.

### Phase C — Item 1 (ADR-061)

6. Build it in `:data` (+ the domain interface, `:app`'s wiring, `:ui`'s reporting of the tab's visibility); the unit tests of §2 Item 1 first or alongside;
   `ARCHITECTURE.md` in the same commit.

### Phase D — Item 2 (fixtures) and Item 3 (slider, if chosen)

7. The fixture swap with its tests; the slider with its test.

### Phase E — gate, mutations, compliance

8. The full gate green. **Mutation checks** (one at a time, `--max-workers=2`): (M1) never open the hold on entering the tab → a test fails; (M2) release the
   hold immediately instead of after the linger → a test fails; (M3) class an unprovoked `Notify` during the hold as `READ` → a test fails; (M4) open a second
   claim on a tap during the hold → a test fails; (M5) re-claim by itself after the Buds close 0x04 → a test fails; (M6) the fixture's request bytes off by one
   byte → a test fails; (M7) the slider writing during the drag → a test fails (if built). Revert each; `git diff` clean afterwards.
9. Compliance: no new permission, no `INTERNET`, no dependency (`./gradlew app:dependencies` unchanged), every new Kotlin file with the AGPL header, no hidden API,
   no MAC or serial logged at INFO or above, the always-on log lines payload-free.

### Phase F — the skeleton and the test plan

10. Item 5 (`CAP-073` skeleton, registry row, Group BI section, *planned* index row) and `APP_TESTPLAN.md` §W.

### Phase G — documentation and release preparation

11. Item 4's approved texts; Item 6's approved plan-file changes; `TODO.md` (done items removed: ADR-061 build, the fixture swap, the slider if built, the
    serial redaction; added: anything deferred); `PROJECT.md` (1.2.0 status: rebuilt, re-test `CAP-073` planned); `README.md`; `CHANGELOG.md` (no date);
    `scripts/release_notes.template`. `android/app/build.gradle.kts` stays `versionName = "1.2.0"`, `versionCode = 10200`.
12. `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and `scripts/lint_docs.py` (exit 0).

### Phase H — finish

13. The RESULT: plain-language summary first (what the rebuilt app does differently, what the maintainer does next); the design; the tests and mutation results;
    the files read; "Deferred documentation" (each in `TODO.md`) and "Commits".
14. **The maintainer's next steps as instructions, never executed:** push and open the pull request (`gh pr create --base main --fill`, with the Release
    checklist: A1–A6 for the rebuild, B1 to be filled by the maintainer); both CI workflows green; **B1** `scripts/release.sh 1.2.0` on the branch tip (it
    overwrites `dist/1.2.0` — the maintainer's own act; the old `ec6d163` build is superseded); B2; **B3** `cp -a dist/1.2.0 ~/opencontrol-1.2.0-tested`; C1–C2
    the `CAP-073` run; then a CAPTURE session for C3/C4.
15. Show a short summary and **ask whether to commit and push**. Only after a yes: commits per concern (Conventional Commits: `feat`, `test`, `docs`), each with
    its *why* and the session's attribution line; `git fetch` and rebase; push without force; no file from a build directory, `dist/`, `.vscode/` or
    `__pycache__`. Opening the pull request is a separate question; never merge it.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Scope.** Only the items above. No new wire request: ADR-061 changes *when* the existing claim (`SABM` DLCI 0x04, `08 11`) is open, nothing else. No
  background claim, no periodic re-claim, no reconnect loop (ADR-032, ADR-044). No new permission, dependency or service.
- **Approvals only in this chat** (`AGENTS.md` §6): no 🟢 FACT promotion, no new ADR or ADR Update without the maintainer's answer in this chat. ADR-060 stays a
  draft. ADR-061 is accepted — do not rewrite it; a pointer Update only if approved.
- **Evidence.** Fixtures are real bytes with file and frame in a comment (`AGENTS.md` §11); a hand-built array only as a labelled supplementary test. Every claim
  in the RESULT and the docs cites a frame, a log line or `file:line`. A negative needs its command, its exit status and a positive control.
- **Privacy.** No serial unredacted and no MAC in any committed text (first 4 + last 2 characters, ADR-010/ADR-058); the `CAP-072` system log's full copy is
  local only — use the committed extract.
- **Release.** No `scripts/release.sh`, nothing written into `dist/`, no tag, no GitHub release, no merge. The maintainer builds and publishes.
- **Files.** Before moving or deleting any file, diff a fresh listing; never wildcard deletes; ask before any delete.
- **Subagents** may only read and report; every write is done and verified in the main session.

---

## 6. Deliverables

- The app changes for Items 1–3 (as chosen) with their tests, green gate and mutation results.
- CAP-073-EVENT-NOTES.md (skeleton, Group BI), registry and index rows, `APP_TESTPLAN.md` §W.
- The documentation of Items 4 and 6 (as approved), `TODO.md`, `PROJECT.md`, `README.md`, `CHANGELOG.md`, `scripts/release_notes.template`.
- ai-sessions/0084_FEATURE_RESULT_2026_10_10.md and the `ai-sessions/INDEX.md` row.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0084_FEATURE_PROMPT_2026_10_10.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0084_FEATURE_PROMPT_2026_10_10
