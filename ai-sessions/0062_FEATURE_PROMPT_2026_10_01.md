# 0062_FEATURE_PROMPT_2026_10_01.md — Build ANC `Get`-before-`Set` with the new wording, honest handling of an answer cut off by a closed Message Stream claim, and a settings menu behind a gear icon (Settings with dark mode, Debug, Info with firmware and build number); adapt the `CAP-066` skeleton to the new build

**Number:** 0062
**Category:** FEATURE
**Date:** 2026-10-01
**Title:** Implement the app changes the maintainer chose after `CAP-064` (`ai-sessions/0060` §D/§H) and `CAP-065` (`ai-sessions/0061` checkpoint "FEATURE"), with
real `CAP-064`/`CAP-065` bytes as fixtures, a green test/lint gate with mutation checks, and an updated `CAP-066` (Group BB) skeleton that re-tests them

---

## 0. How to use this prompt

You are an expert software architect, Android/Kotlin engineer, reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session
in this repository. Run the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action, read **in full**, in this order:
`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md`, `DECISIONS.md` (**every** ADR, ADR-001 … ADR-049, with every dated
Update), `TODO.md`. These are the ground rules; do not audit or change anything before they are read. Then, per task, the sections it needs:

- `AI_SESSION_LOG_PROCEDURE.md` (in full) and `ai-sessions/INDEX.md`.
- **The sources of every item:** `ai-sessions/0060_CAPTURE_RESULT_2026_10_01.md` (in full — §D the maintainer's answers verbatim, §H the outline),
  `ai-sessions/0061_CAPTURE_RESULT_2026_10_01.md` (in full — the Progress block's checkpoint answers, §A, §E the outline),
  `captures/CAP-064-2026-10-01_10-04-14_10-29-28-Group_AZ/CAP-064-FINDINGS.md` §2, §3, §6, §9 and
  `captures/CAP-065-2026-10-01_11-07-30_11-28-23-Group_BA/CAP-065-FINDINGS.md` §0, §2, §3, §9 (the commands, frames and raw bytes the fixtures come from).
- `PROTOCOL.md` §2.2a (incl. the 2026-09-30 and 2026-10-01 Updates — the announcement's structure, L-1), §4.1 (ANC, the Settable byte, ADR-049), §4.3
  Option B/F; `DECISIONS.md` ADR-021/022 (`Get ANC state`), ADR-032 (the on-demand claim — note its item 5: "each claim re-queries ANC (`Get`)"), ADR-042
  (Safe Mode), ADR-044 (re-open), ADR-048 (no ViewModel), ADR-049 (Settable); `ARCHITECTURE.md` §2, §2.4, §3.1 (the ANC row), §6.0b, §7, §9, §12.
- `REVERSE_ENGINEERING.md` — everything about `GetSoftwareInfo`, `qjb`, `fxm` ("Start fetch SoftwareInfo"), `fux`/`fut` (the channel → component map) — for
  task 5 (which announcement entry is the Case).
- `APP_TESTPLAN.md` (in full — the steps this build changes), the `CAP-066` skeleton
  `captures/CAP-066-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BB/CAP-066-EVENT-NOTES.md` (in full), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (the Group BB section and the
  Capture Index row), `id_registry.csv` (the `CAP-066` row).
- **Every Kotlin file you change, in full, before changing it** — at least: `BudsRepository.kt`, `BudsRepositoryImpl.kt` (the claim, `setAncMode`,
  `ancSetOnClaim`, `sendAncGetAndAwait`, the availability state, the Connect reset), `BudsRepositoryImplTest.kt`, `FakeBudsTransport.kt` (does it model
  `channelClosed` for an on-demand channel?), `BudsTransport.kt`, `RfcommBudsTransport.kt` (how an on-demand channel close is reported), `BudsError.kt`,
  `DeviceInfo.kt` (`AncAvailability`, the announcement), `AncTile.kt` and `AncTileTest`, `AncTileService.kt`, `AncScreen.kt`, `ConnectionScreen.kt` (the
  error texts, `ANC_NOT_ALLOWED_TEXT`), `DebugScreen.kt`, `OpenControlNavHost.kt`, `OpenControlTheme.kt`, `OpenControlIcons.kt`, `Details.kt`,
  `SoftwareInfo.kt` and its tests, `CodecRouter.kt` (`MaestroHello`), `MainActivity.kt`, `AppUiSession.kt`, `OpenControlApplication.kt`,
  `DebugSettingsStore.kt` (`:data/settings`), `SessionDiagnostics.kt`, the `:ui` Robolectric tests, the fixture files (`Cap061Fixtures.kt` …
  `Cap063Fixtures.kt`, `SettingsFixtures.kt`), and `android/app/build.gradle.kts`.

Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
**ai-sessions/0062_FEATURE_RESULT_2026_10_01.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and update that block
at the end of every phase **and** after every item built: which items are done and tested, which files are touched but not yet tested, the last gate
result, where intermediate results live (the scratchpad directory — decoded frames, the mutation script; re-create them if the scratchpad is gone). A
resumed session reads this prompt, then the RESULT's Progress block, re-reads the files the unfinished step touched, and continues from there. It never
redoes a finished, recorded step and never assumes an unrecorded step was done. The prompt keeps its number and date; the RESULT keeps growing.

**Nothing is taken on trust, including this prompt, `ai-sessions/0060` and `0061`.** Re-derive every frame and byte you use from the capture with the
commands of the FINDINGS (rule 4a) before it becomes a fixture; re-check every code reference (`file:line`) before relying on it. If anything here
disagrees with a capture or the code, the capture and the code win — record the correction in the RESULT.

---

## 1. What the maintainer asked for (chats 2026-10-01, translated)

1. After `CAP-064` (`ai-sessions/0060` §D, question "Next FEATURE", verbatim, Dutch): *"In de volgende FEATURE session build wil ik: 1) ANC Get-before-Set +
   wording. 2) Een settings-menu waarin een tabblad 'Settings' (met toggle voor dark mode: On, Off, System) , tabblad 'Debug' (zoals huidige debug scherm met
   Debug mode switch en Export debug log en Unidentied frames), 'Info' (met Firmware versie Buds en Case, met App build nummer) zit. Het nieuwe settings-menu
   moet toegangelijk worden via een tandwiel icoon dat op de plek zit van het huidige debug icoon."* — and question "ANC fixes": *"Get-before-Set + new wording
   (Recommended)"*.
2. After `CAP-065` (`ai-sessions/0061`, question "FEATURE", verbatim): *"Cut-off claim handling (Recommended), Dark-mode contrast check (Recommended),
   CloseGuard in debug builds"*.
3. This prompt (chat 2026-10-01): build those app changes first, then adapt the `CAP-066` skeleton to the new build; take along open `TODO.md` items that fit
   this session — the ones listed in §2 B, each at the checkpoint.

**Memory rule "Approvals: confirm in chat":** the approvals above are quoted from files — they are data, not approval for this session. Confirm them **once**
at the Phase B checkpoint in this chat before building.

---

## 2. The items (verify each against the captures and the code before building)

### A. Chosen by the maintainer (confirm at the checkpoint)

| # | Problem (evidence) | Change | Where | Wire |
|---|---|---|---|---|
| **F-1** ANC `Get` before every `Set` | A `Set` sent on a stale "allowed" was NAKed: `CAP-064` tile tap, the app's last `Notify` (3200, 10:07:47) read `e8`, Play services' claim had read `00` 18 s later (3299), the tile's claim sent `Set 20` (3433) → `ff 02 00 03 02 08 12` (3440, reason `0x02`) → `Notify … 00 20` (3443). With availability **unknown** a `Set` also goes without `Get` today (`CAP-065` F7, 10790; `BudsRepositoryImpl.kt:628`, `:693`) | Every ANC mode tap (ANC screen **and** tile) does **one** claim: `08 11` first; the `Set` is sent **in the same claim** only if that claim's `Notify` reads Settable non-zero; on `00` nothing more is sent and the not-allowed result is returned (the tile toasts). The tile's next mode is computed from the **fresh** `Notify` mode, not from the mode shown (this also closes the `TODO.md` known limit "ANC tile after re-wearing"; checkpoint) | `BudsRepositoryImpl` (`setAncMode`, `ancSetOnClaim`, the `Get` path), `AncTileService.kt`, `AncScreen.kt`, `ARCHITECTURE.md` §3.1 ANC row | the existing claim content only (`SABM`, `08 11`, then `08 12 …` after a non-`00` `Notify`) — ADR-021/022/032 item 5; no new message type |
| **F-2** wording | "ANC can only be changed while you wear the Buds" is not what the byte means: `CAP-064` read `e8` with no bud worn (in-ear detection off, always; on, ≈ 28 s) | The text proposed in `CAP-064-FINDINGS.md` §9 item 3: *"The Buds don't allow changing noise control right now (usually because no bud is in an ear). Tapping a mode checks again first."* and a matching tile toast and tile subtitle (`AncTile.kt` "Only while worn"); exact texts at the checkpoint | `ConnectionScreen.kt` (`ANC_NOT_ALLOWED_TEXT`), `AncScreen.kt`, `AncTileService.kt`, `AncTile.kt` | none |
| **F-3** an answer cut off by a closed claim | The stack closes the app's Message Stream socket when Play services connects to the same channel (system log `CAP-065` 09:27:10.450 UTC, `RFCOMM_CreateConnectionWithSecurity: already at opened state`); an answer arriving after that is lost: `CAP-065` tile `Set 08` (2640) → phone `DISC` (2649) → the Buds' ACK (2651) and `Notify … 08` after the close → the app kept OFF; snapshot `Get` (10321) → close (10344) → `Notify` (10356) → the app showed "The Buds didn't respond in time." (`BudsError.Timeout`, `ConnectionScreen.kt:399`) | When the claim's channel closes **after** a `Set` or `Get` was written and **before** its answer, return a distinct result (proposed: `BudsError.ChannelLost(0x04, …)` or a new `AnswerCutOff`; checkpoint) with the wording proposed in `CAP-065-FINDINGS.md` §9 item 1 (*"The answer was cut off — another app took the Buds' channel. Tap Refresh to see the current mode."*); after a cut-off `Set` the shown mode is marked **not confirmed** (dimmed, the (i) dot, `ARCHITECTURE.md` §3.1) until the next `Notify`. A timeout with the channel still open stays `Timeout` | `BudsRepositoryImpl` (claim, `ancSetOnClaim`, `sendAncGetAndAwait`), `BudsError.kt`, `ConnectionScreen.kt`/`AncScreen.kt`, `ARCHITECTURE.md` §7 and §6.0b | none — no automatic re-claim or retry beyond ADR-032's bounded attempts **within the same tap** |
| **F-4** settings menu | The maintainer's design (§1 item 1) | A **gear icon** in the top app bar in place of today's bug icon opens a full-screen **Settings** destination with three tabs: **Settings** (dark mode On / Off / System), **Debug** (today's Debug screen unchanged: Debug mode switch, Export debug log, Unidentified frames), **Info** (F-5). Back returns to the tab it was opened from (as Debug today). Not part of the five bottom tabs | `OpenControlNavHost.kt`, `DebugScreen.kt`, new `:ui` composables, `MainActivity.kt`, `ARCHITECTURE.md` §2.4 | none |
| **F-5** Info tab | No capture records the build (`CAP-064` §0, `CAP-065` §0: `versionCode` 1, no hash in any log) | Show the app's **build identity** — `versionName`, the git commit (short hash, "-dirty" if the tree was dirty) and the build time — from a Gradle `BuildConfig` field computed **locally** at build time (`git` via `providers.exec`; "unknown" when git is not available); and the **firmware** from this connection's `GetSoftwareInfo` announcement (task 5 decides whether entries can be labelled Left/Right/Case — **never** labelled without a 🟢 FACT approved in this chat); "not connected yet" when no announcement exists; the announced Maestro channel (19/21) as a plain value | `app/build.gradle.kts` (`buildFeatures.buildConfig`), `:app` → `:ui` state, `SoftwareInfo.kt` (if entries are kept per index), `DeviceInfo.kt` | none (the announcement is already received) |
| **F-6** dark mode setting | Today the theme follows the system only (`OpenControlTheme.kt:35`) | On / Off / System, default System, persisted in the **existing** AndroidX DataStore (`DebugSettingsStore`'s file or a sibling store — no new dependency), applied without restarting the app; `ARCHITECTURE.md` §2/§9 ("only the Debug-mode switch is stored") updated | `:data/settings`, `OpenControlTheme.kt`, `MainActivity.kt` | none |
| **F-7** dark-mode contrast | `CAP-065` film 11:23:46–11:24:00: the Connection card's "Disconnect" label had low contrast on the pink card in the dark scheme | Check the dark colour roles of the card and its button; fix if below WCAG AA (4.5:1 for text); a Robolectric screenshot or a colour-contrast unit test over the light and dark schemes | `OpenControlTheme.kt`, `ConnectionScreen.kt` | none |
| **F-8** leak check | Three `W System: A resource failed to call close.` in `CAP-065`'s logcat (09:24:04.606, 09:26:39.149, 09:27:21.562 UTC) | **Debug builds only:** `StrictMode.setVmPolicy(VmPolicy.Builder().detectLeakedClosableObjects().penaltyLog().build())` in `OpenControlApplication`, guarded by `BuildConfig.DEBUG`; no behaviour change in release builds. If the leak is found by reading the code (e.g. a `BluetoothSocket` or `ParcelFileDescriptor` of a failed connect attempt not closed), propose the fix at the checkpoint | `OpenControlApplication.kt` | none |

### B. Open `TODO.md` items that fit this session (each a checkpoint question — build only what is chosen)

| # | Item (source) | Proposal |
|---|---|---|
| **T-1** | One loss-cause log line instead of three (`CAP-064-FINDINGS.md` §6, §9 item 5; seen again in `CAP-065` §6) | Log the cause only once it is final (after the +1 s window), or mark the earlier lines "provisional"; log-only, no UI change |
| **T-2** | Notification flash on a failed connect (`TODO.md` Known technical debt: the foreground service is started at `Connecting` and stopped on `Failed`) | Start the service only once the session is `Ready` (or after a short bounded delay), keeping `ARCHITECTURE.md` §6.0a's rule that a user-initiated connect is a foreground start; or leave as is |
| **T-3** | Manifest points left by `ai-sessions/0059` (`TODO.md` "Needs a manifest change"): the `:app` lint warning `DataExtractionRules`; a justification comment for AndroidX Core's app-private `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | Add `android:dataExtractionRules` (excluding everything, consistent with "nothing leaves the device") and the comment; **no new permission**; only if the maintainer allows a manifest change in this session |

**Not in scope (a RESULT note, never a silent addition):** anything new on the wire (a new message type, a claim without a user tap, a periodic `Get`,
`SubscribeToSettingsChanges`, anything on DLCI 0x08, the Dosimeter); auto-connect on lid-open (the maintainer chose "Nothing now", 0060); deriving the
request address from the channel in code (ADR-034 item 3 stays; L-1's "hosting bud" is 🟡 until `CAP-066`); Safe-Mode greying of Sound/Controls (`0057`
F-3, not chosen); a new dependency of any kind.

---

## 3. Rules for this session

- **Evidence** (`PROJECT_RULES.md` rule 4a): every fixture with its frame number, time and the command that extracts it, in a comment; every claim in the
  RESULT links to a frame, log line, film time or `file:line`. Zero creativity with hex (`AGENTS.md` §13.6). A negative needs its command, exit status and a
  positive control (`AGENTS.md` §13 step 8).
- **Fixtures are real bytes** (`AGENTS.md` §11, TST-01): `CAP-064` 3200, 3433/3440/3443, 4091/4103; `CAP-065` 5453/5465 (`Get` → `00`), 6322/6334
  (`Get` → `e8`), 10790/10799 (`Set 08` → ACK), 2640/2649/2651 (`Set` → close → late ACK) and 10321/10344/10356 (`Get` → close → late `Notify`). Re-derive them:
  `tshark -r <log> -Y "bthci_acl.chandle==0x000b && btrfcomm.len>0 && frame.number==N" -T fields -e frame.number -e frame.time -e frame.p2p_dir -e
  btrfcomm.dlci -e data.data` (`bluetooth.addr` is empty with these logs' encapsulation); the close is the `DISC` frame (`-e btrfcomm.frame_type` = `0x43`).
  A hand-built frame only as a labelled supplementary structural test next to real ones.
- **Labels and the `AGENTS.md` §6 gate:** every protocol statement FACT / HYPOTHESIS / ASSUMPTION / OPEN QUESTION; never promote, demote or correct a 🟢 FACT,
  and never write or update an ADR, without the maintainer's approval given **in this chat**. F-5's component labels need such a FACT.
- **Honest state** (`AGENTS.md` §5, `ARCHITECTURE.md` §3.1): a value changes on screen only after the Buds' answer; every value keeps its own receive time;
  an unconfirmed value is marked as such, never shown as current.
- **Guardrails of `AGENTS.md`:** Kotlin only, Compose + Material 3, coroutines/`StateFlow`, no `INTERNET`, no new permission, no new dependency, no hidden
  API, no reflection, AGPL headers on every new Kotlin file, no MAC above DEBUG in logs, no raw bytes outside Debug mode.

---

## 4. Tasks

### Phase 0 — set-up

1. Create the RESULT file (§0). Record `git log -1`, `git status --short`, `git fetch && git log --oneline HEAD..origin/main`, and the last commit under
   `android/` (`git log -1 --format='%h %ad' -- android`).
2. Baseline gate before any change: `cd android && ./gradlew --offline assembleDebug testDebugUnitTest test lint` (drop `--offline` only if the dependency
   cache is empty, and say so). Record the unit-test counts per module from the JUnit XML, the lint result per module and the Kotlin compiler warnings. If the
   baseline is not green, stop and report. (Memory: run mutation checks one at a time with `--max-workers=2`; in `BudsRepositoryImplTest` use
   `runCurrent()`/`settle()`, not `advanceUntilIdle()`, for background collectors.)

### Phase A — evidence and design (no code change)

3. Re-derive every fixture of §3 from `CAP-064-btsnoop_hci.log` and `CAP-065-btsnoop_hci.log`; write the raw bytes, times and commands into the RESULT.
4. Read the code paths of each item in full and write, per item, the current behaviour (`file:line`) and the planned change (functions, state, tests). For
   F-1: how an ANC tap's claim is built today (with and without `Get`), where `AncAvailability` is set and reset, how the tile computes its next mode. For F-3:
   how `RfcommBudsTransport`/`BudsTransport` report the on-demand channel's close (`channelClosed`), what the repository's waits do on it today, and whether
   `FakeBudsTransport` can script "write, then close, then a frame that never arrives". For F-4/F-6: the navigation and theme code. For F-5: how the announcement
   is parsed (`SoftwareInfo.firmwareStrings` returns **distinct** strings — the per-entry structure is lost).
5. **F-5, the Case question.** Establish from evidence which entry of the announcement (`4:{1:{…} 2:{…} 3:{…}}`) is which component: `REVERSE_ENGINEERING.md`
   (the `qjb`/`fxm`/`fux` entries), the JADX output under `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/` (local, gitignored — if it is absent, say so and rely on
   `REVERSE_ENGINEERING.md`; mechanical search only, ADR-017's boundary: the maintainer
   decides relevance), and the captures (`python3 scripts/pwrpc_decode.py <log> | grep GetSoftwareInfo` over every capture: do the entries' field 1/field 2
   values ever differ, e.g. after a firmware update or with one bud missing?). Write the result as a labelled finding in the RESULT and, if it supports a
   promotion, a **draft** `PROTOCOL.md` §2.2a Update for the checkpoint. Without an approved FACT the Info tab shows the firmware **unlabelled**.
6. List every `transport.send` call site the design would add or change (expected: **none new** — F-1 reuses the claim's existing `Get` and `Set`). If a new
   call site is unavoidable, name the ADR that covers it or stop and ask. Confirm that F-1 is covered by ADR-021/022/032 item 5 and is **not** an ADR matter;
   if you conclude it is, draft one for the checkpoint.

### Phase B — checkpoint (stop and ask, in this chat, via `AskUserQuestion`)

7. One question per decision; each option with pros and cons, one marked "(Recommended)", the exact texts and any draft FACT/ADR text in the preview:
   - **F-1/F-2 confirmation** (the 0060 approval, confirmed here) and the exact wordings: ANC tab note, tile toast, tile subtitle; whether the tile's next mode
     comes from the fresh `Notify`.
   - **F-3:** the error shape (`ChannelLost(0x04)` vs a new `AnswerCutOff`), the wording, and how "not confirmed" is shown.
   - **F-4/F-6:** the menu's title and tab names, the dark-mode labels (On / Off / System) and the default.
   - **F-5:** the build-identity fields and format; the firmware display — labelled (only with an approved FACT, draft text in the preview) or unlabelled.
   - **F-7/F-8:** confirm; any leak fix found by reading the code.
   - **T-1, T-2, T-3:** each build or leave (T-3 also: is a manifest change allowed in this session?).
   - **`CAP-066` changes** (Phase F, task 13): the outline.
   Record the answers verbatim in the RESULT. Build only what was chosen.

### Phase C — build F-1, F-2 and F-3 (the ANC claim)

8. Implement per the checkpoint. Tests (JVM, `FakeBudsTransport`, real bytes): (a) availability `ALLOWED`, a tap → `08 11` first, `Notify` 6334 (`e8`) →
   exactly one `Set`, byte-identical to the app's earlier `Set` frames for that mode (e.g. `CAP-065` 10790 for `08`), applied on the ACK 10799; (b) the claim's
   `Notify` = 5465 (`00`) → **no** `Set`, the not-allowed result, the tile's toast path; (c) the `CAP-064` sequence: a stale `e8` (3200) then a tap → `08 11`
   first → `Notify … 00` (3443's bytes) → no `Set` (today's NAK 3440 cannot happen); (d) availability `UNKNOWN` (after a failed snapshot) → `Get` first too;
   (e) F-3: `Set` written, then the channel closes (2649), the ACK (2651) is never delivered → the cut-off result, mode unchanged and marked not confirmed; a
   later `Notify` clears the mark; (f) F-3: `Get` written, close, no `Notify` → cut-off, **not** `Timeout`; (g) a plain timeout with the channel open stays
   `Timeout`; (h) the tile's next mode from the fresh `Notify`. Update `ARCHITECTURE.md` §3.1 (ANC row), §7 (the error) and §6.0b (the 2026-10-01 note's
   proposal → built).

### Phase D — build F-4, F-5, F-6 (settings menu, Info, dark mode)

9. Navigation and screens per the checkpoint; the Debug tab is today's Debug screen unchanged. Info: the `BuildConfig` fields (`./gradlew` must still build
   offline and in CI; "unknown" without git), the firmware per the checkpoint (keep per-entry structure in the parser if needed, with real `CAP-065`/`CAP-061`
   announcement bytes as fixtures — `CAP-061` 1508 is already in `Cap061Fixtures.kt`), the announced channel. Dark mode: DataStore key, applied at once,
   default System. Robolectric tests: the gear opens the menu, the three tabs render, back returns to the previous tab, the Info fields show their values and
   "unknown"/"not connected yet", dark mode On/Off/System changes the scheme. Update `ARCHITECTURE.md` §2, §2.4, §9, §12 (where Debug lives now).

### Phase E — build F-7, F-8 and the chosen T-items

10. F-7 with its contrast test over both schemes; F-8 debug-only (a unit or Robolectric check that release config does not enable it, if testable); T-1/T-2/T-3
    as chosen, each with a test where testable.

### Phase F — gate, mutations, compliance, the `CAP-066` skeleton, documentation

11. `./gradlew --offline clean`, then the full gate of task 2: all green, no new lint issue, no new suppression, no new compiler warning; record the new test
    counts.
12. Mutation checks (apply with a script, run the module's tests, restore, verify byte-identical with `sha256sum`), one at a time, at least: M1 the `Set` is
    sent before the `Notify`; M2 the `Set` is sent after a `00` `Notify`; M3 `UNKNOWN` skips the `Get`; M4 a cut-off is reported as `Timeout`; M5 a cut-off
    `Set` applies the requested mode; M6 the tile uses the shown mode instead of the fresh one; M7 the dark-mode setting is ignored; M8 Info shows a component
    label without the approved FACT (if labels were chosen: a wrong index). Each must fail at least one test.
13. **Adapt the `CAP-066` skeleton** (`captures/CAP-066-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BB/CAP-066-EVENT-NOTES.md`) to this build, per the checkpoint:
    - A.0/P1: install this build **without** clearing the app's data; **film the Info tab** at the start (build hash, firmware) — this replaces "write the
      hash"; the settings menu replaces the bug icon in every step (S2, L3, X1).
    - Section I (BB-1 … BB-4): every ANC tap now starts with `08 11`; BB-2/BB-3 expect `08 11` → `08 13 … 00 …` → **no** `08 12` and the new wording/toast;
      BB-4 expects `08 11` → `e8` → `08 12` → ACK; add a "Refuted if": a `Set` without a preceding `Get` in the same claim.
    - Add a watch-only F-3 step (the cut-off cannot be provoked; note how it shows if it happens: the new wording, mode "not confirmed").
    - K4 dark mode: use the app's own Settings tab (On, Off, System) as well as the system setting; check the Connection card's contrast on film.
    - Keep sections II (BB-5), III (BB-8/BB-9), IV (BB-10), V (BB-12/BB-12m), VI (BB-13/BB-14) and VII (the robustness steps, destructive last) — update
      their wording only where the UI changed; keep the "expected on the wire" columns exact.
    - Update the Group BB section and the Capture Index row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` and the `CAP-066` row of `id_registry.csv` if the scope text
      changes.
14. Compliance: no `INTERNET`, no new permission (`git diff -- '*AndroidManifest.xml'` shows only what T-3 allowed), no new dependency (`git diff --
    android/gradle/libs.versions.toml` empty; `build.gradle.kts` only the `BuildConfig` change), every `transport.send` call site in the diff listed (expected:
    none new), nothing on DLCI 0x08, every write still passes `writeGate` (ADR-042), AGPL headers on new files.
15. Documentation, only what changed: `ARCHITECTURE.md` (§2, §2.4, §3.1, §6.0b, §7, §9, §12), `APP_TESTPLAN.md` (the ANC steps, the menu, Info, dark mode; a
    header note "updated for `ai-sessions/0062`"), `TODO.md` (done items struck, open items kept, new ones added), `CHANGELOG.md`, `README.md` (status block),
    `ai-sessions/INDEX.md` (the 0062 row). **No** `PROTOCOL.md`/`DECISIONS.md` change unless approved in this chat (then a dated Update with a process note,
    new ADR numbers registered in `id_registry.csv`). Run `python3 scripts/ensure_footers.py` and `python3 scripts/lint_docs.py`; it must exit 0 except for
    entries in the "historical" bucket — report anything else.

### Phase G — finish

16. Finish the RESULT: a plain-language summary first (what the user will see differently), then per item what was built and where, the gate table, the
    mutation table, compliance, the fixtures with their bytes, the `CAP-066` changes, the external sources consulted (URL + quoted sentence, e.g. Android's
    `StrictMode.VmPolicy.Builder.detectLeakedClosableObjects` and Material 3 colour-role guidance), open items. It must end with **"Deferred
    documentation"** (each item also in `TODO.md`) and **"Commits"** (hashes, or "not committed — reason"), per `AI_SESSION_LOG_PROCEDURE.md` §9. Set the
    Status per `AI_SESSION_LOG_PROCEDURE.md` §4.
17. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes: Conventional Commits, one commit per concern (`feat(app)` for
    code and tests, `build` for the `BuildConfig` change if separate, `docs` for documentation, the skeleton and the session files), each with a *why* and
    ending with the attribution line from the session's system reminder; `git fetch` and `git log origin/main..HEAD` before pushing (rebase if CI added a
    commit, never force); nothing from a build directory, `android/.kotlin/`, `.vscode/` or `__pycache__` staged.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Wire discipline.** No new message type, no claim without a user tap, no periodic `Get`, no `SubscribeToSettingsChanges`, nothing on DLCI 0x08; F-1 sends
  only what an ANC tap's claim may already send (ADR-021/022/032), F-3 adds no retry beyond ADR-032's bounded attempts within the same tap.
- **Approvals only in this chat** (`AGENTS.md` §6; memory "Approvals: confirm in chat"). Behaviour choices and every user-visible text are confirmed at the
  Phase B checkpoint; a component label in Info needs an approved 🟢 FACT.
- **Honest state.** A value changes only after the Buds' answer; a cut-off answer never becomes an applied or a "didn't respond" state; "unknown" is shown as
  unknown.
- **No network, no new permission, no new dependency, no background work, no polling timer** (`AGENTS.md` §1, §2, §10; `ARCHITECTURE.md` §6). The build
  identity comes from the local git checkout at build time, never from the network.
- **Scope.** Stay within `PROJECT.md` and the items of §2; anything else is a checkpoint question or a RESULT note.
- **Subagents.** A subagent may only read and report; every write is done in the main session and verified.
- **Files.** Before deleting, moving or renaming any file, compare a fresh listing (checksums); prefer a plain rename over copy-then-delete; never chain deletes
  into one long command; ask before any delete.
- **Commits.** Only after the maintainer confirms the final summary (task 17).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0062_FEATURE_PROMPT_2026_10_01.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0062_FEATURE_PROMPT_2026_10_01
