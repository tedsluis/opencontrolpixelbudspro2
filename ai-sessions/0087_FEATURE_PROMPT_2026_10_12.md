# 0087_FEATURE_PROMPT_2026_10_12.md — The Play-user hold fix: a Settings switch between "hint only, no re-claim" and "re-claim after a foreign close with a back-off" (CAP-074's finding), with tests, a hardware-run skeleton and the documents that follow

**Number:** 0087
**Category:** FEATURE
**Date:** 2026-10-12
**Title:** Build the maintainer's chosen fix for `CAP-074` (`ai-sessions/0086`): in a user with Google Play services the ADR-061 hold of DLCI 0x04 is cut by Play's
Fast Pair ≈ 5 s after every Connect and the app does not take the channel back, so only the first press-and-hold is shown. Add **a switch on the Settings tab**
that lets the user choose between **(1) "hint only, no re-claim"** — the noise-control card says that another app took the channel and that a tap re-reads the
mode (today's behaviour plus the hint) — and **(2) "re-claim after a foreign close while the tab is shown, with a back-off"**. Real capture bytes as fixtures, a
green gate with mutation checks, a skeleton for the hardware run in the Play user (`CAP-075`, Group BK), the documents, the release preparation (no tag, no
publication).

---

## 0. How to use this prompt

You are an expert software architect, Android/Kotlin engineer, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in
this repository. Run the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action, read **in full**, in this order: `AGENTS.md`,
`PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md` (in particular §2.4, §3.1's ANC row, §3.2 the timing constants, §5a's "Changed by the Buds" row, **§6.0b** with
its paragraphs of 2026-10-10 and 2026-10-12, §7, §12), `PROTOCOL.md` (§2.3, **§4.1** with its note of 2026-10-12, §8's last rows), `DECISIONS.md` (**every** ADR,
ADR-001 … ADR-061 with every dated Update — in particular **ADR-032 with its Update of 2026-10-12**, ADR-044, ADR-048, ADR-049, ADR-057, **ADR-061 with its Updates
of 2026-10-10 and 2026-10-12**; the draft ADR-060 is not a decision), `TODO.md` (§1, §2, §4, **§5's "the Play-user hold fix" item**, §6). Then:
`AI_SESSION_LOG_PROCEDURE.md` (§4b, §9), `ai-sessions/INDEX.md`, `RELEASING.md` (the Release checklist, "Repository rules", §4–§8, §11, §11a, §13), `APP_TESTPLAN.md`
(§W and the Summary), `CHANGELOG.md` (`[1.2.0]`, Known issues), `README.md` (status, known issues), `id_registry.csv`, and:

- **`ai-sessions/0086_CAPTURE_PROMPT_2026_10_12.md` and `ai-sessions/0086_CAPTURE_RESULT_2026_10_12.md`** — the analysis this build answers, the maintainer's
  choice (Checkpoint D item 3, verbatim), the reading the 0086 session did not do;
- **`captures/CAP-074-2026-10-12_06-40-16_06-45-31-Group_BJ/CAP-074-FINDINGS.md`** — §1 (the claim inventory of both clients), **§4 (the holds, the cuts and every
  `Notify`; items 3–5 are the numbers to design around)**, §7 (vs `CAP-073`), §8 (the root cause and the AOSP quotes), **§9 (the six options and the maintainer's
  choice)**, §10; and `CAP-074-EVENT-NOTES.md` (the timeline 06:40–06:45 — what the user saw);
- `captures/CAP-073-2026-10-10_08-23-35_08-33-24-Group_BI/CAP-073-FINDINGS.md` §4 (the hold's main path in the user without Play — the behaviour that must not
  change) and `CAP-073-EVENT-NOTES.md` (the skeleton layout, appendix);
- `ai-sessions/0084_FEATURE_PROMPT_2026_10_10.md` and `…/0084_FEATURE_RESULT_2026_10_10.md` — how ADR-061 was built (§A.2 the design, §B the checkpoint answers
  "no re-claim after a close" and "the re-open's snapshot kept", §E the gate and the mutation checks M1–M5);
- `ai-sessions/0039_FEATURE_RESULT_2026_09_19.md` §2.4 and §8 (the collision mechanism and the ping-pong warning), `ai-sessions/0040_FEATURE_RESULT_2026_09_19.md`
  (ADR-032's implementation; the 2.7–5.0 s recoveries of that day);
- **Every Kotlin source this build touches, in full, before changing it** — at least: `BudsRepositoryImpl.kt` (`reconcileHold`, `releaseOrHold`, `endHold`,
  `HoldEnd`, the `transport.channelClosed` collector ≈ `:638` with its "no re-claim by itself" comment, `withMessageStream` ≈ `:1412`, `scheduleRelease`,
  `cancelClaimJobs`, `launchInitialSnapshot`, the constants ≈ `:1639`), `BudsRepository.kt`, `BudsTransport.kt`, `RfcommBudsTransport.kt` (`openChannel`,
  `closeChannel`, `channelClosed`, the reader), `SessionReopener.kt`, `AncScreen.kt` (`ancDetailLines`, `ANC_HOLD_HINT_TEXT`), `ConnectionBanner.kt`
  (`MESSAGE_STREAM_HINT_TEXT`, `MessageStreamNotice`), `SettingsMenu.kt` (the Settings tab: Dark mode · Case sounds · Use different Buds), the DataStore files under
  `android/data/src/main/kotlin/io/github/tedsluis/opencontrolpixelbuds/data/settings/` (`DebugSettingsStore`, `DarkModeSettingsStore`, `SettingsDataStore` — the
  pattern for one more key), `OpenControlNavHost.kt` (`onAncTabShown`, `OpenControlActions`, `OpenControlUiState`), `MainActivity.kt` (the wiring of the settings
  stores and of `setAncTabShown`), `AncTileService.kt`, `BudsRepositoryImplTest.kt` (the ten hold tests of `ai-sessions/0084`), `FakeBudsTransport`,
  `AncScreenTest.kt`, `SettingsMenuTest.kt`, `android/app/build.gradle.kts`.

Say in the RESULT what was read in full and what in part.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5).** Create **ai-sessions/0087_FEATURE_RESULT_2026_10_12.md** at the very start with
`**Status:** partial — resumed` and a **Progress** block; update it at the end of every phase and after every substantial step (each file changed, each test class
green, each gate run). A resumed session reads this prompt, then the Progress block, re-reads the files the unfinished step touched, and continues; it never redoes a
recorded step and never assumes an unrecorded one.

**Memory and the machine.** 15 GB laptop. Never two Gradle builds at once; run mutation checks one at a time with `--max-workers=2` and check for leftover
mutations after a crash (the memory "Gradle OOM on mutation runs"). In `BudsRepositoryImplTest` use `runCurrent()`/a settle helper for `backgroundScope` collectors,
not `advanceUntilIdle()` alone (the memory "Repo tests: runCurrent"). Never `git worktree add` a commit (it checks out `captures/`, gigabytes).

**Nothing is taken on trust**, including this prompt, the 0086 RESULT and the ADR texts: re-derive every frame number, delay and code location you rely on from
`CAP-074`'s logs and the sources.

---

## 1. What the maintainer asked for (chat, 2026-10-12)

1. The finding (`CAP-074`, `ai-sessions/0086`, 🟢): in the GrapheneOS user **with** Google Play services, every entry of the noise-control tab starts the ADR-061 hold
   and every hold is cut 4.7–5.1 s after the app's `SABM` (12 of 12) — Play services' Fast Pair re-connects **5.2–5.7 s after losing DLCI 0x04** to the app's connect
   (20 of 20), the Android stack refuses the second connection and removes the app's port (`IOException … read return: -1`, the phone's `DISC`); Play **retries
   2.2–2.6 s after a recovery that collided**. The app does not re-claim (ADR-061, the maintainer's choice of 2026-10-10), so only the first (sometimes the second)
   press-and-hold after a Connect is shown; 35 later holds reached Play services. In the user without Play nothing contends (`CAP-073`: the hold lived 31 s, every
   hold shown).
2. The maintainer's choice at `ai-sessions/0086`'s Checkpoint D (verbatim, Dutch): *"Een switch opnemen in settings tabblad, die switcht tussen optie 1 (B + C: a visible
   hint + a known-issue line, no re-claim) en optie 2 (A: re-claim after a foreign close while the tab is shown, with a back-off), zodat de gebruiker zelf kan kiezen
   wat het beste werkt."* — a switch on the Settings tab between the two modes, so the user chooses what works best.
3. Not chosen then: a Known-issues line for 1.2.0 in `CHANGELOG.md`/`README.md` (the CAPTURE session wrote none) — this FEATURE session writes the texts of the
   **new** version.
4. "Commit and push only after I confirm" (standing practice); no tag, no release (`RELEASING.md`: publishing is the maintainer's own act).

---

## 2. The items (verify each against `CAP-074`, the ADRs and the code before building)

### Item 1 — The switch and its two modes (the main item)

**Mode (1) "hint only, no re-claim"** = today's ADR-061 behaviour plus a visible hint: when the hold ends with `HoldEnd.CHANNEL_CLOSED` (the `channelClosed`
collector), the noise-control card shows a line such as *"The noise-control channel was taken by another app at HH:MM:SS (for example Google Play services' Fast
Pair). A change made on a bud is not shown until you tap a mode, Refresh, or pull down."* — the wording is a checkpoint question; the time is the close's (the
export's "closed … not by this app" line); the line clears on the next claim of this tab. Nothing re-claims. The ANC (i)'s `ANC_HOLD_HINT_TEXT` sentence "Another
app that uses the channel (for example Google Play services' Fast Pair) cannot use it meanwhile" is **corrected** in both modes: Play takes the channel back ≈ 5 s
later and the app then holds nothing (mode 1) / claims it again after a delay (mode 2).

**Mode (2) "re-claim after a foreign close while the tab is shown, with a back-off"**: after `HoldEnd.CHANNEL_CLOSED` while `holdWanted` (the tab shown and the app
visible) and the session `Ready`, the app claims again (`reconcileHold(null)` — the M5 mutant of `ai-sessions/0084`) after a delay, with a back-off and a cap, never
in the background, never after a session loss, never when the tab or the app was left meanwhile. **Design the delay around `CAP-074`'s numbers and say the wire
consequence of each candidate at the checkpoint:**

- Play's recovery lands **5.2–5.7 s after Play lost the channel** (`CAP-074-FINDINGS.md` §4 item 3) and cuts the app; Play's **retry lands 2.2–2.6 s after a collided
  recovery** (item 5) and succeeds into a free channel; the app's own connect into Play's open claim fails once (56–363 ms) and succeeds on the 400-ms retry,
  killing Play's socket (§2 of that file).
- (a) **re-claim at once** (≈ 0.5 s): the channel is free until Play's retry at 2.2–2.6 s, which then cuts the app again (as `CAP-074` 06:41:06, cut after 0.38 s);
  with a back-off the cycle lengthens — but each cycle is a cut and a retry: a 2–3 s ping-pong while the tab is open.
- (b) **re-claim after ≈ 2.7 s** (after Play's retry has succeeded): the app's connect kills Play's socket (attempt 1 fails, attempt 2 connects), Play recovers
  5.2–5.7 s later and cuts the app — an ≈ 8 s cycle in which the app holds ≈ 5 s of every 8 (option F of `CAP-074-FINDINGS.md` §9).
- (c) **a back-off that stops** (e.g. 2.7, 5, 10 s, then stop until the next tap/pull/re-entry): fewer collisions, the symptom returns later in the same tab view.
- Every candidate rests on Play's timers of **this** Play version (`ai-sessions/0040` saw 2.7–5.0 s, `CAP-059` 1.7–3.1 s — 🟡 for other versions); say so in the (i)
  and in the docs; the cap keeps the worst case bounded (no loop, ADR-032/ADR-044's rule).
- Each re-claim is the existing claim code (`refreshAncMode` with the `Get` first, as entering the tab does — `reconcileHold`'s `claimNow` path); **nothing new on the
  wire** (`SABM`, `08 11 00 00`, the `DISC`); one always-on log line per re-claim with the attempt number and the delay (`AGENTS.md` §9, payload-free).

**The switch:** on the Settings tab (`SettingsMenu.kt` — propose its place and card: e.g. a "Noise-control channel" card under Dark mode: *"When another app takes the
channel: (•) Show a hint, do not take it back  ( ) Take it back while the noise-control tab is open (may fight that app)"*), persisted in DataStore like Dark mode
(`DarkModeSettingsStore`'s pattern, a new key in `SettingsDataStore` — plain preferences, non-sensitive), read by `BudsRepositoryImpl` as a `Flow<…>` the way
`debugModeEnabled` is injected, shown in `OpenControlUiState`. **The default is a checkpoint question** (mode 1 is today's behaviour and honest; mode 2 restores the
feature for the Play user at the cost of the ping-pong). No ViewModel (ADR-048); testable in `:data` with `FakeBudsTransport` and in `:ui` with Robolectric.

**The ADR:** mode (2) contradicts ADR-061's "the Buds (or another client) closing DLCI 0x04 ends it without a re-claim" (`ARCHITECTURE.md` §6.0b, the maintainer's
choice of 2026-10-10). Draft **ADR-062** — "A user-selectable re-claim of the noise-control channel after a foreign close, with a back-off" — superseding that clause
of ADR-061 for the switch's mode (2) only (ADR-061's hold itself stands; ADR-032's "no reconnect loop" is kept by the cap): Context (`CAP-074`), Options (the six of
`CAP-074-FINDINGS.md` §9 and the switch), Decision (the two modes, the default, the delay/back-off/cap values, the hint texts), Consequences (the ping-pong in mode 2,
the docs, the tests, the hardware run). **Show it at the checkpoint; write it only after the maintainer's answer in the chat** (`AGENTS.md` §6).

**Unit tests (real bytes, `AGENTS.md` §11; fixtures from `CAP-074` — re-derive with `tshark -r CAP-074-btsnoop_hci.log -Y "frame.number==N" -T fields -e data.data`):**
with `FakeBudsTransport` and virtual time —
- mode 1: the tab shown, the hold open (`CAP-074` B 15853's `Notify` `08 13 00 04 01 e8 e8 40` as the snapshot answer), an unprovoked `Notify` (B 15860 `… e8 e8 80`) →
  `CHANGED_BY_BUDS`; then the transport's `channelClosed` (the stack's close) → `endHold(CHANNEL_CLOSED)`, **no** `openChannel` within 60 s of virtual time, the hint
  state set with the close's time; a tap afterwards → one claim, the hint cleared;
- mode 2: the same, then `openChannel` after exactly the chosen delay (not before), the `Get` sent, the hold logged as started again; a second foreign close → the
  next back-off step; after the cap → no further claim and the hint state; the tab left or the app hidden during the delay → no claim; a session loss during the
  delay → no claim (the ADR-044 re-open's snapshot is kept as the hold, as today); a Disconnect → no claim;
- the switch: the DataStore key round-trips; the repository follows a change of the switch at once (a pending re-claim is cancelled when switched to mode 1);
- `:ui`: the switch's wording and state (Robolectric), the hint line's text pinned as a literal (`UserMessageTest`'s pattern), the corrected `ANC_HOLD_HINT_TEXT`.
The ten hold tests of `ai-sessions/0084` keep passing unchanged in meaning (mode 1 is their behaviour).

### Item 2 — The texts (checkpoint)

- The hint line (mode 1 and after the cap in mode 2), the switch's title, its two labels and its (i) explanation (what "another app" is, the ≈ 5 s, the ping-pong of
  mode 2, that the numbers were measured on 2026-10-12 and may differ with another Play version);
- `ANC_HOLD_HINT_TEXT` corrected (both modes);
- `CHANGELOG.md` for the **new version** (the maintainer decides at the checkpoint: **1.2.1** as a fix, or **1.3.0** — `versionCode` follows `ARCHITECTURE.md` §14's
  rule), Known issues (the Play-user behaviour in each mode), `README.md` (status, known issues), `scripts/release_notes.template`;
- `ARCHITECTURE.md` §2.4 (the Settings tab), §3.2 (the new constants), §5a ("Changed by the Buds" row), **§6.0b** (the re-claim paragraph), §7 if a new `BudsError`
  or state is added; `PROJECT.md`'s status line; `TODO.md` (§5's item done; anything deferred).

### Item 3 — The hardware-run skeleton `CAP-075` (Group BK)

Verify that `CAP-075` and Group BK are free (`id_registry.csv`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` — `CAP-074` is BJ). Write
captures/CAP-075-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BK/CAP-075-EVENT-NOTES.md in `CAP-073`'s skeleton layout (purposes, a reference-frame table re-derived from
`CAP-074`/`CAP-073`, P-steps, steps with "Expected on screen", "Expected on the wire", Test-ID and "Refuted if", Don'ts, After the run, the analysis checklist), a
`planned` registry row, a Group BK section and a *planned* Capture Index row. **Two parts, ≤ 20 minutes on film:**

1. **The Play user (the Owner, user 0)** — the screen recording with sound, the system log pulled **within one minute of the first cut** (so the Bluetooth process's
   `RFCOMM_CreateConnectionWithSecurity: already at opened state` / `cleanup_rfc_slot` / `RfcommEventStreamMedium` lines are retained — `CAP-074-FINDINGS.md` §10's
   first open question), `adb shell pm path` + `adb pull` + `sha256sum` of the installed APK, P0 with the Play-services *Nearby devices* permission state on film
   (`TODO.md` §3's standing item): mode 1 — Connect, the ANC tab, holds every ≈ 5 s for 30 s, the hint line on film, a tap, the hint gone; mode 2 — the same, the
   re-claims on the wire (`SABM` + `08 11` after the chosen delay), the holds shown or lost per the cycle, the back-off's steps and the cap; the switch toggled with the
   tab open.
2. **The user without Play (user 10)** — the regression of `CAP-073` §4: the hold lives until the tab is left, every hold shown, no re-claim ever (nothing contends), in
   both switch positions.

Test-IDs: `TOUCH-007`, `ANC-001`…`ANC-004`, `PAIR-003`, `BATT-004`, `FW-003`; `APP_TESTPLAN.md` gains a section **X** and its Summary row.

---

## 3. Decisions already taken, and what still needs the maintainer

Taken (do not reopen): the switch with the two modes (the maintainer, chat 2026-10-12); ADR-061's hold itself; ADR-032's claim model and "no reconnect loop"; the
hold's end reasons and log lines; no background claim; the CAPTURE session's findings (`CAP-074-FINDINGS.md` §8, 🟢/🟡 as labelled).

For the checkpoint (Phase B, one question per decision, options with pros and cons, "(Recommended)" on one, the exact text in the preview):
- the delay/back-off/cap of mode 2 (the candidates of Item 1 with their wire consequence) and the **default** mode;
- the switch's place, title, labels and (i) text; the hint line's wording; the corrected `ANC_HOLD_HINT_TEXT`;
- **ADR-062's draft** (nothing written without the answer);
- the version (1.2.1 or 1.3.0) and the `CHANGELOG.md`/`README.md`/release-notes texts;
- Item 3's skeleton scope (both users, or the Play user only).

---

## 4. Tasks

### Phase 0 — set-up

1. Create the RESULT (§0). Record `git log -1`, `git status --short`, `git fetch` and `git log --oneline HEAD..origin/main`. Work on a branch from an up-to-date
   `main`: `git switch main && git pull --ff-only && git switch -c feature/hold-reclaim-switch` (say the name you used). `android/domain/bin/` is build output: never
   stage it. `main` takes no direct push.
2. Run the gate once **before** any change (`cd android && ./gradlew assembleDebug testDebugUnitTest test lint --max-workers=2`, `python3 scripts/lint_docs.py`) and
   record the counts, so a later failure is attributable.

### Phase A — evidence and design check (no code change)

3. Re-derive from `CAP-074-btsnoop_hci.log` (handle `0x000b`) the delays of `CAP-074-FINDINGS.md` §4 items 3 and 5 for at least five cuts (the app's `SABM`, the
   `DISC`, Play's loss, Play's next `SABM`) and the fixture bytes; read the code paths Item 1 touches; write the design (the state, who calls what, the log lines, the
   tests, the constants) and the candidate delays with their consequences into the RESULT.
4. Item 2's text locations; Item 3's free IDs.

### Phase B — checkpoint (stop and ask, in this chat, via `AskUserQuestion`)

5. The questions of §3. Record the answers verbatim in the RESULT. Do not build an option that was not chosen. Write ADR-062 only after its answer.

### Phase C — Item 1

6. Build it in `:data` (+ `:domain` for any new state type, `:app`'s wiring, `:ui`'s switch and hint); the unit tests of §2 Item 1 first or alongside;
   `ARCHITECTURE.md` in the same commit.

### Phase D — gate, mutations, compliance

7. The full gate green. **Mutation checks** (one at a time, `--max-workers=2`): (M1) mode 1 re-claims after a foreign close → a test fails; (M2) mode 2 never
   re-claims → a test fails; (M3) mode 2 re-claims without the delay → a test fails; (M4) mode 2 keeps re-claiming after the cap → a test fails; (M5) mode 2
   re-claims with the tab left / the app hidden / the session lost → a test fails; (M6) the hint state not set on a foreign close → a test fails; (M7) the switch's
   DataStore key not persisted → a test fails. Revert each; `git diff` clean afterwards.
8. Compliance: no new permission, no `INTERNET`, no dependency (`./gradlew :app:dependencies --configuration releaseRuntimeClasspath` unchanged), every new Kotlin
   file with the AGPL header, no hidden API, no MAC or serial logged at INFO or above, the always-on log lines payload-free; no new wire request.

### Phase E — the skeleton and the test plan

9. Item 3 (`CAP-075` skeleton, registry row, Group BK section, *planned* index row) and `APP_TESTPLAN.md` §X.

### Phase F — documentation and release preparation

10. Item 2's approved texts; `DECISIONS.md` ADR-062 (approved) and a pointer Update on ADR-061 (approved); `id_registry.csv` (ADR-062); `TODO.md`; `PROJECT.md`;
    `README.md`; `CHANGELOG.md` (no date); `scripts/release_notes.template`; `android/app/build.gradle.kts` (`versionName`/`versionCode` of the chosen version).
11. `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and `scripts/lint_docs.py` (exit 0).

### Phase G — finish

12. The RESULT: plain-language summary first (what the app does differently in each mode, what the maintainer does next); the design; the tests and mutation results;
    the files read; "Deferred documentation" (each in `TODO.md`) and "Commits".
13. **The maintainer's next steps as instructions, never executed:** push and open the pull request (`gh pr create --base main --fill`, with the Release checklist);
    both CI workflows green; **B1** `scripts/release.sh <version>` on the branch tip; B2; **B3** `cp -a dist/<version> ~/opencontrol-<version>-tested`; C1–C2 the `CAP-075`
    run; then a CAPTURE session for C3/C4.
14. Show a short summary and **ask whether to commit and push**. Only after a yes: commits per concern (Conventional Commits: `feat`, `test`, `docs`), each with its
    *why* and the session's attribution line; `git fetch` and rebase; push without force; no file from a build directory, `dist/`, `.vscode/` or `__pycache__`. Opening
    the pull request is a separate question; never merge it.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Scope.** Only the items above. **No new wire request:** mode 2 re-uses the existing claim code (`SABM` DLCI 0x04, `08 11 00 00`, the `DISC`); it changes *when* a
  claim is made, nothing else. No background claim, no periodic claim, no unbounded loop (ADR-032, ADR-044): every re-claim chain has a cap and ends with the tab,
  the app's visibility or the session. No new permission, dependency or service. Nothing that touches Play services itself (`AGENTS.md` §1).
- **Approvals only in this chat** (`AGENTS.md` §6): no 🟢 FACT promotion, no new ADR or ADR Update without the maintainer's answer in this chat. ADR-060 stays a draft.
  ADR-061 is accepted — do not rewrite it; ADR-062 supersedes one clause of it for mode 2 only, after approval.
- **Evidence.** Fixtures are real bytes with file and frame in a comment (`AGENTS.md` §11); a hand-built array only as a labelled supplementary test. Every claim in
  the RESULT and the docs cites a frame, a log line or `file:line`. A negative needs its command, its exit status and a positive control. The delays are `CAP-074`'s
  measurements of one Play version — label them 🟡 for others in every text that names them.
- **Privacy.** No serial unredacted and no MAC in any committed text (first 4 + last 2 characters, ADR-010/ADR-058); the capture originals stay in
  `~/opencontrol-capture-originals/`.
- **Release.** No `scripts/release.sh`, nothing written into `dist/`, no tag, no GitHub release, no merge. The maintainer builds and publishes.
- **Files.** Before moving or deleting any file, diff a fresh listing; never wildcard deletes; ask before any delete.
- **Subagents** may only read and report; every write is done and verified in the main session.

---

## 6. Deliverables

- The app changes for Item 1 (the switch, the hint, the re-claim with back-off) with their tests, green gate and mutation results.
- `DECISIONS.md` ADR-062 and the ADR-061 pointer Update (as approved), `id_registry.csv`.
- CAP-075-EVENT-NOTES.md (skeleton, Group BK), registry and index rows, `APP_TESTPLAN.md` §X.
- The documentation of Item 2 (as approved), `TODO.md`, `PROJECT.md`, `README.md`, `CHANGELOG.md`, `scripts/release_notes.template`, `ARCHITECTURE.md`.
- ai-sessions/0087_FEATURE_RESULT_2026_10_12.md and the `ai-sessions/INDEX.md` row.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0087_FEATURE_PROMPT_2026_10_12.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0087_FEATURE_PROMPT_2026_10_12
