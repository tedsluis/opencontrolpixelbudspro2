# 0064_FEATURE_PROMPT_2026_10_01.md — Keep the tab across a configuration change, make the balance precise, name a Bluetooth-off loss, close every profile proxy, and add licence / README / issues links to Settings → Info; write the `CAP-067` (Group BC) skeleton

**Number:** 0064
**Category:** FEATURE
**Date:** 2026-10-01
**Title:** Build the app changes the maintainer chose after `CAP-066` (`ai-sessions/0063` checkpoint "FEATURE", RESULT §F) plus licence, README and issue links on
the Info tab, with real `CAP-066` bytes and log lines as fixtures, a green test/lint gate with mutation checks, and a new `CAP-067` (Group BC) skeleton that
re-tests them and the steps `CAP-066` left open

---

## 0. How to use this prompt

You are an expert software architect, Android/Kotlin engineer, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session
in this repository. Run the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action, read **in full**, in this order:
`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md`, `DECISIONS.md` (**every** ADR, ADR-001 … ADR-049, with every dated
Update), `TODO.md`. These are the ground rules; do not audit or change anything before they are read. Then, per task, the sections it needs:

- `AI_SESSION_LOG_PROCEDURE.md` (in full) and `ai-sessions/INDEX.md`.
- **The sources of every item:** `ai-sessions/0063_CAPTURE_RESULT_2026_10_01.md` (in full — §A, §D the maintainer's answers verbatim, §F the outline this prompt
  expands) and `captures/CAP-066-2026-10-01_16-12-57_16-34-50-Group_BB/CAP-066-FINDINGS.md` §5 (balance), §6 (UI, rotation), §7 (session ends, T-1), §8
  (StrictMode, profile proxies), §12 (open questions), and `CAP-066-EVENT-NOTES.md` (the step mapping — what was not done).
- `DECISIONS.md` ADR-026 (balance range/polarity), ADR-044 (re-open), ADR-045 (field 17 write — unchanged by this session), ADR-048 (no ViewModel);
  `ARCHITECTURE.md` §2, §2.4, §6, §6.0b (loss causes), §7, §9, §12; `AGENTS.md` §1 (no network), §2 (`ACTION_STATE_CHANGED`), §3, §10, §12 (licence).
- `APP_TESTPLAN.md` (in full), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (§3, §9, the Group BA/BB sections and the Capture Index), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the
  Test-IDs the new skeleton names), `id_registry.csv` (last `CAP-066`, last ADR-049), the `CAP-066` skeleton (Appendix A of its EVENT-NOTES) as the layout example.
- **Every Kotlin file you change, in full, before changing it** — at least: `OpenControlNavHost.kt`, `SettingsMenu.kt`, `EqScreen.kt` (`BalanceSlider`),
  `BudsSettings.kt` (`snapBalance`, `BALANCE_RANGE`), `SessionDiagnostics.kt`, `SessionLoss.kt` (`classifySessionLoss`), `LinkEvaluation.kt`,
  `OsConnectionObserver.kt`, `BluetoothStateObserver.kt`, `BudsRepositoryImpl.kt` (`reclassifyLoss`, the loss path), `MainActivity.kt`,
  `AndroidManifest.xml` (`:app`), their tests (`SettingsMenuTest`, `EqScreenTest`, `SessionDiagnosticsTest`, the `OsConnectionObserver`/`LinkEvaluation` tests,
  `BudsRepositoryImplTest`), the fixture files, `android/app/build.gradle.kts`, `android/gradle/libs.versions.toml`, and the repository's `LICENSE` and `README.md`.

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
**ai-sessions/0064_FEATURE_RESULT_2026_10_01.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and update that block at the
end of every phase **and** after every item built: which items are done and tested, which files are touched but not yet tested, the last gate result, where
intermediate results live (the scratchpad directory; re-create it if gone). A resumed session reads this prompt, then the RESULT's Progress block, re-reads the
files the unfinished step touched, and continues from there. It never redoes a finished, recorded step and never assumes an unrecorded step was done.

**Nothing is taken on trust, including this prompt and `ai-sessions/0063`.** Re-derive every frame, byte and log line you use from the capture before it becomes
a fixture; re-check every `file:line` before relying on it (the line numbers below are from 2026-10-01 and may have moved). If anything here disagrees with a
capture or the code, the capture and the code win — record the correction in the RESULT.

---

## 1. What the maintainer asked for (chat 2026-10-01, translated)

1. "Write the prompt for the next FEATURE session. Include the new feature points from `ai-sessions/0063_CAPTURE_RESULT_2026_10_01.md`" — items F-1 … F-5 below
   (the `0063` checkpoint "FEATURE": *"Keep the tab on rotation (Recommended), Balance: steps or -/+ buttons (Recommended), Name a Bluetooth-off loss, Close
   unbound profile proxies"*, and the re-test list of `0063` §F item 5).
2. "And in addition, add to the app under Settings → Info: the licence plus a link to the LICENSE on my GitHub project; a link to the README.md; a link where
   you can report issues on GitHub." — item F-6.

---

## 2. The items (verify each against the captures and the code before building)

### F-1 — keep the tab across a configuration change (a `CAP-066` defect)

`CAP-066` (FINDINGS §6): rotating to landscape relaunched the activity (logcat `wm_on_stop … handleRelaunchActivity` / `wm_on_create` 14:31:47.259–.326 UTC,
after-force-stop logcat lines 372–374) and the screen went from **Sound** (film 16:31:46.5) to **Connection** (16:31:47.5); rotating back kept Connection.
🟡 Cause (code reading, `0063`): `OpenControlNavHost.kt` (~`:228–258`) — on the first composition after the relaunch `currentBackStackEntry` is still `null`,
`currentIndex` falls back to 0 (`coerceAtLeast(0)`), the `LaunchedEffect(currentIndex, inSettings)` scrolls the restored pager to page 0 and the settled-page
collector then navigates to Connection. **Confirm the cause with a failing test first**, then fix: do not sync pager ↔ back stack while the back stack is not
yet known (`currentBackStackEntry == null`), or derive the pager's page from the restored destination. Also check the settings menu (a relaunch while Settings
is open must keep Settings and its tab — `rememberSaveable` in `SettingsMenu.kt`) and the dark-theme relaunch.
Test: Robolectric `StateRestorationTester` in `:ui` — select Sound → `emulateSavedInstanceStateRestore()` → Sound is shown and selected; the same for each tab
and for Settings/Info. Mutation: re-introduce the unconditional sync → the test fails. Hardware: rotate on each of the five tabs and on Settings → Info.

### F-2 — balance precision (a `CAP-066` finding; behaviour chosen at the checkpoint)

`CAP-066` (FINDINGS §5): 32 drags of the balance slider wrote 32 values (`.log.last` A7723 … A7873, all `RESPONSE` OK) but never `17:7` (Right 4, the target);
the right side was reached only as Right 6 (`17:11`); the last write was Centre (`17:0`). `BalanceSlider` (`EqScreen.kt` ~`:232–257`) is a continuous
`-100f..100f` slider with one write per completed drag and the ±3 centre snap (`BudsSettings.snapBalance`). **Options for the checkpoint** (each with pros/cons;
show a mock-up in the preview): (a) `steps` so the slider snaps to multiples of 5; (b) multiples of 2; (c) −/+ buttons beside the slider (1 or 5 per tap),
the slider kept; (d) (c) without the slider. Invariants, whatever is chosen: one `WriteSetting 4:{17:…}` per completed gesture or tap (no write per drag
frame, no queue), ADR-045 unchanged (sint32 zigzag, ±100, +100 = Left), the label shows the Buds' value after their `RESPONSE` OK (ADR-045 / `0057` F-1), the
centre snap kept or changed only as chosen. Fixtures (real bytes, re-derive with `tshark -r CAP-066-btsnoop_hci.log.last -Y "bthci_acl.chandle==0x000b &&
btrfcomm.len>0 && frame.number==N" -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e data.data`): A7723 `7e 00 3b 03 10 13 1d ea 71 de
7d 5e 25 1d 9a 8c 9e 2a 05 22 03 88 01 0b c2 f4 d8 2c 7e` (`4:{17:11}` = Right 6) and A7873 `… 2a 05 22 03 88 01 00 4a 2d 0a bb 7e` (`4:{17:0}`) — and, if Right 4
is reachable now, the encoder's output for `17:7` checked against an existing real `17:7` capture if one exists (search the captures first; otherwise label the
expected bytes as derived, not captured). Test: `EqScreenTest` for the chosen control (a tap/gesture → exactly one `onChange` with the expected value; Right 4
reachable).

### F-3 — name a Bluetooth-off loss (T-1 follow-up, cosmetic)

`CAP-066` (FINDINGS §7): both Bluetooth-offs ended the session with a lasting provisional verdict — before-force-stop export (E1) lines 279–283 (16:13:00.971
link `UNKNOWN` … 16:13:01.390 "Session loss cause: undetermined (no reading of Android's link close to the loss) (provisional: …)") and after-force-stop export
(E2) lines 164–168 (16:32:17.010 … 16:32:17.642, the same). Proposed: when the adapter is off (`BluetoothAdapter.ACTION_STATE_CHANGED` `STATE_TURNING_OFF` /
`STATE_OFF` — **verify that `BluetoothStateObserver.kt` already observes it and how the repository sees it**; `AGENTS.md` §2) around the loss, the loss is
classified "Bluetooth was switched off" (final, not provisional) and the Connection screen keeps its existing "Bluetooth is disabled." state. Exact wording is a
checkpoint question. No new permission, no timer. Test: `SessionDiagnosticsTest` / the `classifySessionLoss` test with an adapter-off reading, built from the
E1/E2 sequences above (log lines as fixtures, with their file and line numbers in a comment); the existing provisional/link-down tests stay green.

### F-4 — close every profile proxy (StrictMode hygiene, 🟡)

`CAP-066` (FINDINGS §8): a `BluetoothLeAudio` proxy created at `OsConnectionObserver.kt:137` was finalized without `close()` (StrictMode 14:32:36.652 UTC). In
`OsConnectionObserver.observe` (~`:120–144`) `onServiceDisconnected` removes a proxy from the map, and `awaitClose` closes only those still in the map — so a
proxy unbound before the flow ends (e.g. at a Bluetooth-off: AOSP `BluetoothAdapter.onBluetoothOff` disconnects every proxy) is never `closeProfileProxy`'d.
Note (0063 §8, 🟡): in the AOSP module branch `android16-qpr2-release`, `BluetoothLeAudio` opens its `CloseGuard` and never closes it, so the warning may persist
even when the app closes everything — say so in the RESULT; the goal is that the app closes every proxy it obtained. Fix: keep every proxy object the listener
delivered (a separate list) and `closeProfileProxy` each in `awaitClose`, exactly once. Test: a fake adapter/listener (no real Bluetooth) that delivers a proxy,
unbinds it, then the flow ends → `closeProfileProxy` called for it once. Mutation: close only the map's members → the test fails.

### F-5 — the `CAP-067` (Group BC) skeleton (written by this session, after F-1 … F-6 are built)

The re-test list of `0063` §F item 5 plus this build: BB-12 (the **Left** out with both worn on channel 19 — predicted Buds `DISC` + announcement 21, the open
half of L-1, `PROTOCOL.md` §2.2a 2026-10-01 `0063` Update), BB-15 watch (an answer cut off by the claim's close, F-3 of `0062`), BB-10 balance to **Right 4**
with the new control (`WriteSetting 4:{17:7}` → OK), K4d dark mode **Off** and Android's own dark switch with **System**, L3 (an export with Debug mode **off**:
no hex lines), K5 (GrapheneOS Bluetooth auto-off set to a short value on film, then restored), and destructive last A5 (*Nearby devices* denied), (E) (Buds
forgotten), B4 (Pair a device twice quickly), Z1 (re-pairing; `PAIR-001`); plus this build: rotation on every tab and on Settings → Info (F-1), a Bluetooth-off
loss line in the export (F-3), StrictMode lines after a Bluetooth off/on (F-4), the Info tab's licence and links (F-6 — opening each link on film only if the
checkpoint approves links). Folder `captures/CAP-067-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BC/CAP-067-EVENT-NOTES.md` in the `CAP-066` skeleton's layout (purposes,
A.0 with the Info tab filmed first, A.1 preparation, steps with "Expected on screen" / "Expected on the wire" and "Refuted if", A.3, A.4 checklist with the
registry Test-IDs); `id_registry.csv` row `CAP-067,capture,planned,…`; a Group BC section and a Capture Index row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`;
`_sidebar.md`. Check `id_registry.csv` for the next free number before using `CAP-067` / Group BC.

### F-6 — licence, README and issues on Settings → Info (new, the maintainer's request)

Add to the Info tab (`SettingsMenu.kt`, `InfoTab`): (1) the licence — "GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)" (verify against
`LICENSE` and the SPDX headers, `AGENTS.md` §12) — with a link to `https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/LICENSE`; (2) a link to
`https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/README.md`; (3) a link to report issues, `https://github.com/tedsluis/opencontrolpixelbudspro2/issues`
(verify each URL against `git remote get-url origin` and the files' footers). **This touches `AGENTS.md` §1 ("must function 100% offline", no `INTERNET`):** the
app itself must not fetch anything. Present at the checkpoint, with pros/cons: (a) tappable links that hand the URL to the user's browser with an explicit
`Intent.ACTION_VIEW` started by a tap (the app makes no network request and needs no permission; handle `ActivityNotFoundException` with a message; verify
whether Android 11+ package visibility needs a `<queries>` entry for this — check the developer.android.com / AOSP text and quote it; a manifest change is a
checkpoint item); (b) the URLs shown as selectable/copyable text only, no intent; (c) (a) plus the full licence text bundled offline (an asset or a raw
resource, no dependency) so the licence is readable without a network. Ask whether this needs an ADR (it interprets `AGENTS.md` §1); if the maintainer says
yes, draft it (the `DECISIONS.md` template, next free number — ADR-050 if still free — registered in `id_registry.csv` only after approval). No Google-owned
assets or trademarks (`AGENTS.md` §12). Test: `SettingsMenuTest` — the licence line and the three links are shown; tapping a link calls the injected URL opener
with the exact URL (no real intent in a JVM test); with option (b) the text is selectable. Mutation: a wrong URL → the test fails.

---

## 3. Rules for this session

- **Evidence** (`PROJECT_RULES.md` rule 4a): every fixture with its frame number or log line, file, time and the command that extracts it, in a comment; every
  claim in the RESULT links to a frame, log line, film time or `file:line`. Zero creativity with hex (`AGENTS.md` §13.6). A negative needs its command, exit
  status and a positive control (`AGENTS.md` §13 step 8).
- **Fixtures are real bytes and real log lines** (`AGENTS.md` §11, TST-01); a hand-built value only as a labelled supplementary test next to real ones.
- **Labels and the `AGENTS.md` §6 gate:** every protocol statement FACT / HYPOTHESIS / ASSUMPTION / OPEN QUESTION; never promote, demote or correct a 🟢 FACT,
  and never write or update an ADR, without the maintainer's approval given **in this chat**.
- **Honest state** (`AGENTS.md` §5, `ARCHITECTURE.md` §3.1): a value changes on screen only after the Buds' answer; every value keeps its own receive time.
- **Guardrails of `AGENTS.md`:** Kotlin only, Compose + Material 3, coroutines/`StateFlow`, **no `INTERNET`**, no new permission, no new dependency, no hidden
  API, no reflection, AGPL headers on every new Kotlin file, no MAC above DEBUG in logs, no raw bytes outside Debug mode, no ViewModel (ADR-048).
- **No protocol change:** no new message type, no new claim, nothing on DLCI 0x08; the only wire effect of this session is that F-2 may make different
  (still allowed) field-17 values reachable.

---

## 4. Tasks

### Phase 0 — set-up

1. Create the RESULT file (§0). Record `git log -1`, `git status --short`, `git fetch && git log --oneline HEAD..origin/main` (fast-forward if CI added a
   commit), and the last commit under `android/` (`git log -1 --format='%h %ad' -- android`).
2. Baseline gate from clean: `cd android && ./gradlew --offline clean` then `./gradlew --offline assembleDebug testDebugUnitTest test lint` — record the test
   counts per module (from the JUnit XML), lint issues per module and compiler warnings. If the gate is red before any change, stop and report.

### Phase A — evidence and design (no code change)

3. Re-derive the fixtures of F-2 (A7723, A7873, and a captured `17:7` if one exists in any capture: `scripts/pwrpc_decode.py` / the `CAP-065` scratch decoder over
   `captures/*/*btsnoop_hci*` — record the command and the result, positive or negative with a positive control) and the log-line fixtures of F-3 (E1 279–283,
   E2 164–168 — the exact lines). For F-1, reproduce the cause with a failing Robolectric test before designing the fix.
4. For each item, write in the RESULT: today's behaviour (`file:line`), the planned change, the tests, the mutation that each test must catch.
5. F-6: check how the Info tab is built today; the exact URLs; the licence text in `LICENSE` and the SPDX identifier; and the external rules for opening a URL
   from an app on Android 14+ (package visibility, `ACTION_VIEW`, `ActivityNotFoundException`) — quote the official text (developer.android.com pages may return
   only navigation to the fetch tool; then the AOSP source with `curl … ?format=TEXT | base64 -d`; check any summary against the raw text before quoting).

### Phase B — checkpoint (stop and ask, in this chat, via `AskUserQuestion`)

6. One question per decision, each option with pros and cons, one marked "(Recommended)", the exact user-visible text or mock-up in the preview: **F-1** the fix
   approach; **F-2** the control (steps of 5 / 2, −/+ of 1 or 5, slider kept or not, the centre snap); **F-3** the wording and whether the loss is final at once;
   **F-4** confirm; **F-6** (a)/(b)/(c), the exact Info texts, a manifest change if any, and whether an ADR is needed (with the draft); **F-5** the skeleton's
   scope and order (destructive steps last). Record the answers verbatim in the RESULT. Build only what is approved.

### Phase C — build F-1 and F-2

7. F-1 with its `StateRestorationTester` tests (every tab, Settings/Info), then F-2 per the checkpoint with `EqScreenTest` (and a repository/codec test if the
   encoder path changes). Update the RESULT's Progress block after each.

### Phase D — build F-3 and F-4

8. F-3 in `SessionDiagnostics`/`classifySessionLoss` (and the repository's loss path), tests with the `CAP-066` log sequences; F-4 in `OsConnectionObserver` with
   the fake-adapter test.

### Phase E — build F-6

9. F-6 per the checkpoint: the Info tab's licence and links (an injected URL opener so `:ui` stays testable and Android-free where it is today), the
   `ActivityNotFoundException` path, the manifest change only if approved; `SettingsMenuTest`.

### Phase F — gate, mutations, compliance, the `CAP-067` skeleton, documentation

10. `./gradlew --offline clean`, then the full gate of task 2: all green, no new lint issue, no new suppression, no new compiler warning; record the counts.
11. Mutation checks (apply with a script, run the module's tests with `--max-workers=2`, restore, verify byte-identical with `sha256sum`), one at a time, at
    least: M1 the unconditional pager sync (F-1); M2 a balance write per drag frame / the old continuous slider (F-2); M3 an adapter-off loss classified as
    "undetermined" (F-3); M4 `awaitClose` closes only the map's proxies (F-4); M5 a wrong Info URL (F-6). Each must fail at least one test.
12. Compliance: no `INTERNET` in any manifest, source or merged (`grep`, positive control `BLUETOOTH_CONNECT`); `git diff -- '*AndroidManifest.xml'` shows only
    what the checkpoint allowed; no new dependency (`git diff -- android/gradle/libs.versions.toml` empty); no new `transport.send` call site; every write still
    passes `writeGate` (ADR-042); AGPL headers on new files; nothing under `android/` reads or writes the network.
13. Write the **`CAP-067` (Group BC) skeleton** of F-5 and register it (§2 F-5).
14. Documentation, only what changed: `ARCHITECTURE.md` (§2.4 navigation/Info, §6.0b loss causes, §9/§12 as needed, and §1/§9 if links were approved),
    `APP_TESTPLAN.md` (a section for this build; header note "updated for `ai-sessions/0064`"), `TODO.md` (the "Open after `ai-sessions/0063`" items struck or
    kept, new ones added), `CHANGELOG.md`, `README.md` (status block), `ai-sessions/INDEX.md` (the 0064 row). **No** `PROTOCOL.md`/`DECISIONS.md` change unless
    approved in this chat (then a dated Update or a new ADR with a process note, registered in `id_registry.csv`). Run `python3 scripts/ensure_footers.py` and
    `python3 scripts/lint_docs.py`; it must exit 0 except for entries in the "historical" bucket (the `0063` prompt's pre-rename `CAP-066` folder should now be
    historical) — report anything else.

### Phase G — finish

15. Finish the RESULT: a plain-language summary first (what the user will see differently), then per item what was built and where, the gate table, the
    mutation table, compliance, the fixtures with their bytes/lines, the `CAP-067` skeleton, the external sources (URL + quoted sentence), open items. It must
    end with **"Deferred documentation"** (each item also in `TODO.md`) and **"Commits"** (hashes, or "not committed — reason"), per
    `AI_SESSION_LOG_PROCEDURE.md` §9. Set the Status per `AI_SESSION_LOG_PROCEDURE.md` §4.
16. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes: Conventional Commits, one commit per concern (`feat(app)`/`fix(app)`
    for code and tests, `docs` for documentation, the skeleton and the session files), each with a *why* and ending with the attribution line from the
    session's system reminder; `git fetch` and `git log origin/main..HEAD` before pushing (rebase if CI added a commit, never force); nothing from a build
    directory, `android/.kotlin/`, `.vscode/` or `__pycache__` staged.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **No network from the app** (`AGENTS.md` §1): no `INTERNET`, no fetch, no web view, no analytics; F-6's links may only hand a URL to another app on the user's
  tap, and only if the checkpoint approves it.
- **Approvals only in this chat** (`AGENTS.md` §6; memory "Approvals: confirm in chat"). Behaviour choices and every user-visible text are confirmed at the
  Phase B checkpoint.
- **Honest state.** A balance value changes only after the Buds' `RESPONSE` OK; a loss cause is never guessed — "Bluetooth was switched off" only with an
  adapter-off reading.
- **No new permission, no new dependency, no background work, no polling timer** (`AGENTS.md` §2, §10; `ARCHITECTURE.md` §6).
- **Scope.** Stay within `PROJECT.md` and the items of §2; anything else is a checkpoint question or a RESULT note.
- **Subagents.** A subagent may only read and report; every write is done in the main session and verified.
- **Files.** Before deleting, moving or renaming any file, compare a fresh listing (checksums); never chain deletes; ask before any delete.
- **Commits.** Only after the maintainer confirms the final summary (task 16).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0064_FEATURE_PROMPT_2026_10_01.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0064_FEATURE_PROMPT_2026_10_01
