# 0057_FEATURE_RESULT_2026_09_28.md — Material 3 UI overhaul: top app bar with a Debug action, five tabs, (i) info sheets, graphical battery, own Kotlin icons, pull to refresh / reconnect, within the maintainer's decisions of 2026-09-29

**Number:** 0057
**Category:** FEATURE
**Date:** 2026-09-28
**Title:** Assess the maintainer's Material 3 UI proposal against the current app, apply the decisions of 2026-09-29, settle the remaining points at a checkpoint, and build the presentation changes plus one user-triggered settings re-read
**Status:** complete

## Progress

- Phase 0: started 2026-09-29. Git at start: `4f9f189` (docs: prompt 0057), `git status --short`: only `?? android/.kotlin/` (not this session's);
  `git fetch` + `git log HEAD..origin/main`: empty (up to date).
- Scratchpad: `/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/6677b4f3-cfa4-4b87-9e1f-7d2265d9486b/scratchpad` —
  `parity_baseline.sha256` (every file under `android/{data,hardware,domain}/src`), the gate log (session-local).
- **Phase 0 done.** Baseline gate (`./gradlew --offline assembleDebug testDebugUnitTest test lint`, dependency cache present so `--offline` kept):
  green. Tests (JUnit XML): `:data` 1564 (debug) + 1564 (release), `:hardware` 51 + 51, `:domain` 25 — 0 failures. Lint: `:app` 0 errors, 1 warning
  (`DataExtractionRules`, `AndroidManifest.xml:37`, pre-existing), `:data`/`:hardware`/`:ui` no issues. Kotlin compiler warnings on a clean build: 6 lines, 3 distinct,
  all in `:hardware` (`BudsCompanionPairing.kt:263`, `:293` delicate API; `LinkEvaluation.kt:84` opt-in) — none in `:ui`/`:app`/`:data`.
  **Flake recorded:** the first clean run failed 2 `:hardware` **release** tests (`RfcommBudsTransportTest` "a failed write on an on-demand channel…",
  "a deliberate closeChannel…" — 10 s real-time `TimeoutCancellationException` while lint/tests ran in parallel); on the unchanged tree they passed in 2
  isolated reruns and in a full `--continue` rerun (session-local log). Not caused by this session (nothing was changed); noted for Phase F.
- Parity baseline: `parity_baseline.sha256`, 84 files under `android/{data,hardware,domain}/src`.
- **Phase A done** (§1–§5 below). No file other than this RESULT modified. Scratchpad also holds `sym/*.svg` (Material Symbols sources), `m3/`, `ug/`, `ic/` (javap work).
- **Phase B done** — answers in §6.
- **Phase C done:** new `OpenControlIcons.kt` (9 Material Symbols paths verified byte-equal to the fetched SVGs + the own Case icon), `OpenControlTheme.kt` (F-2),
  `Details.kt` ((i) button + dialog), `PullRefresh.kt` (`PullTab`, `PullAction`, `pullActionFor`, `PullToRefresh`); `OpenControlNavHost.kt` rewritten (top bar,
  5 tabs, Debug destination, pull per tab; `OpenControlActions.onPull`); `MainActivity.kt` (theme, pull mapping); `ARCHITECTURE.md` §2.4. **Deviation:** because
  `MainActivity` calls `refreshSettings()`, D-11's interface entry and 8-line implementation were added in the same step (tests in Phase D). Gate:
  `:app:assembleDebug` OK, 0 compiler warnings in `:ui`/`:app`; `:ui` lint 1 new `ModifierParameter` warning (`Details.kt:67`) — fixed (parameter order).
- **Phase D done:** 3 D-11 tests added to `BudsRepositoryImplTest.kt` (real `CAP-036` bytes); `:data` repository tests green.
- **Phase E done (all screens built, compiled, linted after each):** Connection (status `ElevatedCard` with `primaryContainer`/`errorContainer`, Safe Mode
  `errorContainer` card, graphical battery, firmware into (i)), ANC (2 × 2 mode buttons, (i)), Find (tonal buttons, (i)), Sound (two cards, (i), F-1 sliders;
  balance slider disabled while unread — U-1's rule applied to the one control it had not reached), Controls (three cards, (i)), Debug (headline removed).
  `ConnectionBanner.kt`: `MessageStreamHint` composable → `MESSAGE_STREAM_HINT_TEXT` (used in the (i)). Last gate: `:app:assembleDebug` OK, `:ui` lint no issues,
  `:app` lint the 1 pre-existing warning, 0 compiler warnings in `:ui`/`:app`.
- **UI tests done:** `libs.versions.toml` (`junit4` 4.13.2, `robolectric` 4.13, `compose-ui-test-junit4`, `compose-ui-test-manifest` from the BOM),
  `ui/build.gradle.kts` (`testImplementation` only — `ui-test-manifest` deliberately not `debugImplementation`, so the APK manifest is unchanged;
  `unitTests.isIncludeAndroidResources`); `:ui:dependencies` (test runtime): 242 artifacts, none network/analytics/ads/GMS. New `PullActionTest` (4) and
  `BatteryCardTest` (5, Robolectric SDK 34) — green. Robolectric's `android-all-instrumented` 14 is cached in `~/.m2` (first run online), so the gate stays `--offline`.
- **Phase F (partly):** `./gradlew --offline clean`, then the full gate with `--continue`: green. `:data` 1567 (= 1564 + 3 D-11) debug and release, `:hardware`
  51 + 51, `:domain` 25, `:ui` 9 + 9 (new, approved); lint `:app` 1 warning (pre-existing), `:data`/`:hardware`/`:ui` no issues; compiler warnings: the same 3
  `:hardware` lines as the baseline, none new. `@OptIn(ExperimentalMaterial3Api::class)`: `OpenControlNavHost.kt:211` (TopAppBar), `PullRefresh.kt:96`
  (PullToRefreshBox) — both approved. **Parity:** `sha256sum -c` of the baseline: only `BudsRepositoryImpl.kt` (+11), `BudsRepository.kt` (+8),
  `BudsRepositoryImplTest.kt` (+66) differ, additions only, no file added/removed, 0 removed test lines, `transport.send(` 8 → 8.
- **Interruption (2026-09-29):** the Claude Code process was OOM-killed during mutation check M3; the laptop was restarted and the scratchpad (`/tmp`) is gone.
  On resume M3's mutation (a second `readSettings()` after a failed pass) was still in `BudsRepositoryImpl.kt`; it was removed and the file re-checked
  against the intended 11-line D-11 diff. M1–M3 results were lost → re-run.
- **Mutation checks re-run after the restart** (script `mutate.py` in the new scratchpad, `--max-workers=2`, each restored with `cp` + `cmp`):
  M1 (no `Ready` check) → 1 test fails ("refreshSettings sends nothing while the session is not Ready"); M2 (fields 2 and 4 swapped in `SETTING_READ_ORDER`) →
  4 fail (the D-11 byte test, the D-11 unanswered-field test, the Connect-time byte test, the Connect-time unanswered test); M3 (a second pass after a failed
  one) → 1 fails ("an unanswered field in a re-read … is not retried"). 3/3 caught. Restored file = the 11-line D-11 diff; `:data` 1567, 0 failures.
- **Tasks 14–15 done** (§9, §10). **Phase G done:** `APP_TESTPLAN.md` (header note + section O, O1–O13), `CAP-064-EVENT-NOTES.md` section VIII (AZ-9…AZ-14)
  + `id_registry.csv` row, `ARCHITECTURE.md` §3.1 (D-7 marker wording, F-4 correction, D-11 in the settings row), `TODO.md`, `CHANGELOG.md`, `README.md`,
  `ai-sessions/INDEX.md`. `scripts/ensure_footers.py` exit 0; `scripts/lint_docs.py` exit 0 (its two new "dead reference" notes for scratchpad log names in this
  RESULT were reworded; the other 38 lines are pre-existing). `PROJECT.md` unchanged (no scope line changed).
- **Final state:** waiting only for the maintainer's commit/push decision (prompt task 19). Nothing committed.

## 0. Plain-language summary (what you will see differently)

1. **Top bar and five tabs.** "OpenControl" at the top with a bug icon for Debug (full screen, ← or back returns); the bottom bar has Connection, ANC, Sound,
   Controls, Find with the app's own icons. Swiping between tabs works as before.
2. **Battery as a picture.** Left | Case | Right, each with an icon, the percentage and a bar; a bolt on a charging bud; "Battery unavailable" without a bar.
3. **Times behind (i).** Every card has an (i); tapping it shows exactly the lines you used to see (with their times, "last seen", "last connection", "read …").
   When something on a card is not current, the (i) gets a dot and the value is dimmed (battery) or the control stays greyed out (settings).
4. **Pull down** on any tab: when connected it refreshes that tab (Connection/Find: battery, ANC: mode, Sound: EQ + settings, Controls: settings); when not
   connected it connects (or shows Android's own Bluetooth / permission / pairing prompt). The spinner lasts as long as the action.
5. **Dark mode and wallpaper colours**, and the EQ/balance knobs no longer stay at a value the Buds refused.
6. On the wire only one thing is new: the existing seven settings reads, sent again on a pull on Sound or Controls. Nothing is hardware-verified yet —
   `APP_TESTPLAN.md` section O and `CAP-064` section VIII.

## 1. Reading (prompt §0) — what was read in full and what only partly

- **In full:** `AGENTS.md` (in context), `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md` (all 862 lines), `DECISIONS.md` (all 2184 lines, ADR-001 … ADR-047 with
  every dated Update), `AI_SESSION_LOG_PROCEDURE.md`, this prompt.
- **Kotlin read in full before any change:** every file under `android/ui/src/main/` (`OpenControlNavHost.kt`, `ConnectionScreen.kt`, `ConnectionBanner.kt`,
  `AncScreen.kt`, `EqScreen.kt`, `ControlsScreen.kt`, `SettingsUi.kt`, `FindMyBudsScreen.kt`, `DebugScreen.kt`, `TimeFormat.kt`), `MainActivity.kt`, `BudsRepository.kt`,
  `BatteryStatus.kt`, `BudsSettings.kt`, `ui/build.gradle.kts`, `gradle/libs.versions.toml`.
- **Partly (disclosed deviation from "in full", taken to keep the context usable for a presentation-only session):** `BudsRepositoryImpl.kt` — lines 160–200, 580–680,
  765–1260 (`connect`/`openSession`, `disconnect`, `refreshEq`/`readEq`, the settings writes, `writeGate`, `refreshBattery`, `subscribeRuntimeInfo`,
  `withMessageStream`, `launchInitialSnapshot`, `launchInitialEqRead`, `readSettings`, `SETTING_READ_ORDER`, `SETTING_READ_TIMEOUT_MS`), plus a function outline of the
  rest; `BudsRepositoryImplTest.kt` — lines 1–157 (set-up), 355–475 (the settings-read tests), 1120–1147, and the test-name outline; `SettingsFixtures.kt` (`Settings036`);
  `BudsError.kt` and `SafeModeGate.kt` only through their uses; `PROTOCOL.md` — §0, §0.1, §1, §4.4, §4.5 intro (lines 1–150, 1292–1372) and the heading outline, **not**
  §2, §4.1, §4.3, §4.5.x, §6 in full (nothing in this session touches the wire beyond re-sending existing reads); `TODO.md`, `APP_TESTPLAN.md`,
  `SCREENSHOTS_PIXEL_BUDS_APP.md`, `CHANGELOG.md`, `ai-sessions/0052`/`0054`/`0056` RESULTs — not yet read at checkpoint time (read before Phase G / as needed);
  `CAP-064-EVENT-NOTES.md` — outline and section VII.

## 2. Phase A — assessment of the proposal (prompt §2), sentence by sentence

Legend: **built** = already in the app; **usable**; **usable + correction**; **decided otherwise** (D-row of prompt §3); **not possible now**.

| # | Proposal item | Verdict | Evidence / correction |
|---|---|---|---|
| P-1 | TopAppBar "OpenControl", Debug as `actions` icon, not a bottom tab | usable + correction (D-5, D-6) | Today Debug is the 6th bottom tab (`OpenControlNavHost.kt:95`), which **contradicts** `ARCHITECTURE.md` §2.4 ("never shown in the main bottom/side navigation") — D-6 restores the documented design, so no ADR is needed, only the §2.4 text. `Icons.Default.BugReport` is not in `material-icons-core` 1.7.0 (jar listing: 49 icons, `Info`, `Refresh`, `Settings`, `Build`, `Warning`, … no `BugReport`) → own icon (D-5). **`TopAppBar` is `@ExperimentalMaterial3Api` in the resolved `material3` 1.3.0** (`javap -v` of `AppBarKt.class`: the annotation on `TopAppBar-*`), although today's Compose docs show it without an opt-in — conflicts with prompt §3 "an `@OptIn` is acceptable only for [`PullToRefreshBox`]" → checkpoint question. |
| P-2 | Exactly 5 tabs: Connection, ANC, Sound, Controls, Find | usable (D-6) | Matches Android's guidance "three to five destinations of equal importance" (source S-1). |
| P-3 | Keep `HorizontalPager` in sync | built | `OpenControlNavHost.kt:239-252` (`ai-sessions/0043`); kept, 5 pages. |
| P-4 | Swipe down: refresh connection or information | usable + correction (D-10, D-11) | `PullToRefreshBox` is `@ExperimentalMaterial3Api` in 1.3.0 (`javap`, `PullToRefreshKt.class`). The actions are fire-and-forget today (`MainActivity.kt:427-461`, `applicationScope.launch {}`) — the indicator needs a completion signal: the pull callback returns the launched `Job` (design §4). ANC and Find are not scrollable (`AncScreen.kt:70-76`, `FindMyBudsScreen.kt:58-62`) — a pull needs scrollable content → `verticalScroll`. |
| P-5 | Timestamps hidden; `IconButton(Icons.Outlined.Info)` → `ModalBottomSheet`/`AlertDialog` with explanations + times | usable + correction (D-7) | `Icons.Outlined.Info` is not in core (only `Filled.Info`) → own (i) icon. `ModalBottomSheet` is `@ExperimentalMaterial3Api` in 1.3.0 (`javap`), `AlertDialog` is not → checkpoint. Non-text marker required for stale/unread (prompt §3 note). |
| P-6 | Connection status in `ElevatedCard`, `primaryContainer` "subtle green" when `Ready` | usable + correction (D-9) | `primaryContainer` is the theme's primary tone, not green; **the app has no theme at all today** — `MainActivity.kt:466` uses bare `MaterialTheme {}` (baseline light scheme, no dark theme, no dynamic colour) → further improvement F-2. Ready also gets a text/icon, not colour alone. |
| P-7 | 3-column battery (Left \| Case \| Right), icons, %, `LinearProgressIndicator` | usable + correction (D-5, D-8) | Unavailable ⇒ "Battery unavailable", **no** bar; `isCharging` is `Boolean?` (`BatteryStatus.kt:40`), per-bud charging is `leftCharging`/`rightCharging: ChargingReading?` (`BatteryStatus.kt:59-61`) incl. `fromEarlierSession`; `isStale` on L/R/Case (`BatteryStatus.kt:41`, `markedFromEarlierSession`, `:86-92`). |
| P-8 | Charging: `Icons.Default.Bolt`, `Color.Green` or tertiary bar | usable + correction (D-5, D-9) | Own bolt icon; `tertiary` role, never `Color.Green`; the bolt has a content description ("charging"). |
| P-9 | Case "Last seen": `alpha(0.5f)` + small "Last seen" label | usable + correction (D-7, D-8) | Applies to L/R too; the word moves into (i) per D-7 — the non-text marker is the checkpoint question. |
| P-10 | Errors as `FilledCard`/`OutlinedCard`, `errorContainer` | usable + correction (D-9) | No `FilledCard` in M3 (source S-5: `Card`, `ElevatedCard`, `OutlinedCard`); every specific text of `userMessage` (`ConnectionScreen.kt:318-344`) stays. |
| P-11 | ANC: 4 modes as prominent centred buttons | usable | `AncScreen.kt:94-98` today: 4 plain buttons labelled `mode.name`. |
| P-12 | ANC: keep `settableToggles == 0x00` check, **disable** buttons + warning | decided otherwise (`ai-sessions/0054` I-1) | Buttons stay enabled; a tap re-checks (`AncScreen.kt:52-54`, `:91-93`; `ARCHITECTURE.md` §3.1 ANC row). The line "ANC can only be changed while you wear the Buds (checked …)" is a state line → its time moves into (i), the sentence without the time stays visible (it is the reason a tap may not switch). |
| P-13 | Sound: 5 EQ sliders | built | `EqScreen.kt:105-109`. |
| P-14 | Presets 3 + 2 via `chunked(3)` | built | `EqScreen.kt:139-165` (`FlowRow` is `@ExperimentalLayoutApi` in foundation-layout 1.7.0, `ai-sessions/0051` §11). |
| P-15 | Balance −100…+100, +100 = L | built (D-4) | `EqScreen.kt:208-237`: slider position = −value, "L"/"R" labels, "Centre" snap ±3. |
| P-16 | Mono audio switch (Sound) | built (D-4) | `EqScreen.kt:118-120`. |
| P-17 | Controls: master switch "Use touch controls" | built | `ControlsScreen.kt:75`. |
| P-18 | Remove "Digital Assistant" | decided otherwise (D-2) | ADR-045 field 7; `ControlsScreen.kt:79-80`, `SettingsUi.kt:60-63`. |
| P-19 | Per-bud ANC checkboxes (L and R × 4) | decided otherwise (D-1) | ADR-046: one list, no side field on the wire (🟢); `ControlsScreen.kt:154-174`. |
| P-20 | Mono audio switch also on Controls | decided otherwise (D-4) | one setting, one place (Sound). |
| P-21 | In-ear detection as read-only row | decided otherwise (D-3) | ADR-047 switch, `ControlsScreen.kt:86-87`. |
| P-22 | Find: tonal buttons Ring Left / Ring Right / Stop; "Each action briefly claims…" behind (i) | usable | `FindMyBudsScreen.kt:67-70`; `MessageStreamHint` (`ConnectionBanner.kt:73-82`) moves into (i); the ringing notice (`FindMyBudsScreen.kt:81-92`) stays visible. |
| P-23 | Debug only via top bar; `UnidentifiedFrame` list + Export | built content, usable placement | `DebugScreen.kt:61-86` (also the Debug-mode switch, kept). |
| P-24 | Phase E "mock the write for field 12" | decided otherwise (D-1) | ADR-046: real `WriteSetting`, built `ai-sessions/0056`. |
| P-25 | Phase F: no XML, no Google icons, `assembleDebug lint` | usable | Plus the full gate of prompt task 2. |
| P-26 | Eval 1: UI never caches / never optimistic | usable — **one existing breach found** | F-1: `EqBandSlider` (`EqScreen.kt:252`, `remember(value)`) and `BalanceSlider` (`EqScreen.kt:211`, `remember(reading)` + `position = …` at `:227`) keep the **finger position after release**; when the write is refused or times out, the repository value does not change, so the `remember` key does not change and the knob stays at the unacknowledged value (the text of the balance shows the Buds' value, the EQ number shows the knob). `EqScreen.kt:204-205` claims "if the write is refused the slider snaps back" — it does not. Also `ARCHITECTURE.md` §3.1's bullet "A user-initiated write optimistically updates local state" is stale since 2026-09-24 (ANC applied only on the answer, §3 step 7). |
| P-27 | Eval 2: times never discarded; sheets show the exact same strings | usable (D-7) | Same helpers: `formatUpdatedAt` (`TimeFormat.kt:40`), `settingTime` (`SettingsUi.kt:47`), `budLine`/`caseLine`/`batteryText` (`ConnectionScreen.kt:393-437`), `ancNotAllowedLine` (`AncScreen.kt:110`). |
| P-28 | Eval 3: reads "2, 4, 7, 17, 19, 22" at Connect | usable + correction (D-12) | Stale: `SETTING_READ_ORDER` = 2, 4, 7, **12**, 17, 19, 22 (`BudsRepositoryImpl.kt:1235-1243`). Unchanged. |
| P-29 | Eval 4: every write through `writeGate`; Safe Mode read-only Sound/Controls; field 12 mocked | usable + correction (D-1) | Every settings write: `BudsRepositoryImpl.kt:900` (`writeGate`), EQ `:749`, ring `:946`; field 12 is a real, gated write (ADR-046). **Correction:** in Safe Mode the Sound/Controls controls are *enabled* today and each tap is refused by the gate with `UnsupportedFirmware` (`ControlsScreen.kt:65`, `EqScreen.kt:94`: `enabled = isReady()` only) — nothing is sent, but "read-only" is enforced by the gate, not by disabled controls. Keeping it that way is the as-built `0052` behaviour; disabling them while `safeMode != null` is further improvement F-3. |
| P-30 | Eval 5: tests green, no fixture/assert change, byte parity | usable | Phase F. |
| P-31 | Eval 6: no permission, dependency, GMS, timer, service, dispatcher | usable | Phase F. The pull uses the existing `applicationScope`. |

**Missed by the prompt author (found in Phase A):** P-1's and P-5's opt-in facts; P-6's missing theme (F-2); P-26's slider breach (F-1); P-29's Safe-Mode "enabled but refused" (F-3);
P-4's completion-signal and scrollability needs; the Connection screen's own title "OpenControl for Pixel Buds" (`ConnectionScreen.kt:101`) duplicates the new top bar;
`AncScreen.kt:80` shows the raw class name "Connection: Ready" (moves into the ANC (i) sheet unchanged).

## 3. Phase A — external validation (fetched 2026-09-29)

| # | Source (URL) | Exact sentence |
|---|---|---|
| S-1 | https://developer.android.com/develop/ui/compose/components/navigation-bar | "You should use navigation bars for: Three to five destinations of equal importance" |
| S-2 | https://developer.android.com/develop/ui/compose/components/app-bars | actions: "Icons that provide the user access to key actions. They appear on the right of the app bar." |
| S-3 | https://developer.android.com/develop/ui/compose/components/pull-to-refresh | "The pull to refresh component allows users to drag downwards at the beginning of an app's content to refresh the data." (`isRefreshing`: "A boolean value indicating whether the refresh action is in progress.") |
| S-4 | https://developer.android.com/develop/ui/compose/components/bottom-sheets | "If you want to implement a bottom sheet, you can use the `ModalBottomSheet` composable." |
| S-5 | https://developer.android.com/develop/ui/compose/components/card | "Cards typically present a single coherent piece of content." — variants `Card`, `ElevatedCard`, `OutlinedCard` (no `FilledCard`). |
| S-6 | https://developer.android.com/develop/ui/compose/components/progress | "A determinate indicator reflects exactly how complete an action is"; form `LinearProgressIndicator(progress = { … })`. |
| S-7 | https://developer.android.com/develop/ui/compose/designsystems/material3 | "Dynamic color is available on Android 12 and above." / "use `dynamicDarkColorScheme()` or `dynamicLightColorScheme()`"; `isSystemInDarkTheme()`; colour roles (`primaryContainer`/`onPrimaryContainer`) instead of hard-coded colours. |
| S-8 | https://developer.android.com/guide/topics/ui/accessibility/apps | "we recommend that each interactive UI element have a focusable area, or touch target size, of at least 48dpx48dp."; contrast "at least 4.5:1" (small text), "at least 3:1" (other). |
| S-9 | https://www.w3.org/WAI/WCAG21/Understanding/use-of-color.html | SC 1.4.1: "Color is not used as the only visual means of conveying information, indicating an action, prompting a response, or distinguishing a visual element." |
| S-10 | https://github.com/google/material-design-icons/blob/master/LICENSE | "Apache License, Version 2.0, January 2004" (the Material Symbols repository's licence). SVGs fetched from `raw.githubusercontent.com/google/material-design-icons/master/symbols/web/<name>/materialsymbolsoutlined/<name>_24px.svg`. |

m3.material.io pages render client-side and returned no text to the fetcher (tried `/components/navigation-bar/guidelines`); the developer.android.com Compose pages
above are Google's own Material 3 guidance for Compose and were used instead. **Opt-in status in the resolved version** (`./gradlew :ui:dependencies`:
`androidx.compose.material3:material3:1.3.0`, BOM `2024.09.00`; `material-icons-core` 1.7.0), from `javap -v` of the cached `material3-release` classes:
`TopAppBar` **experimental**, `PullToRefreshBox` **experimental**, `ModalBottomSheet` **experimental**; `Card`/`ElevatedCard`/`OutlinedCard`, `LinearProgressIndicator`,
`AlertDialog` **stable**. (The docs pages S-2…S-4 show no opt-in because they describe newer `material3` releases.) `ImageVector.Builder`, `addPath` and
`PathParser().parsePathString(d).toNodes()` are public in the resolved `ui-graphics` (`javap`); `materialIcon`/`materialPath` are public in `material-icons-core`
(`IconsKt.class`).

## 4. Phase A — design (per screen; binds only to existing `OpenControlUiState` fields / `OpenControlActions` callbacks unless noted)

**Scaffold.** `Scaffold(topBar = TopAppBar("OpenControl", actions = IconButton(OpenControlIcons.Debug, "Debug")), bottomBar = NavigationBar(5 tabs))`.
Debug is a `navController` destination outside the pager (the zero-size `NavHost` keeps being the back-stack source of truth); while it is current the
pager is replaced by `DebugScreen`, the top bar shows a back arrow and "Debug", system back pops it (→ the tab you came from). `TAB_DESTINATIONS` loses `DEBUG`.

**(i) icon and sheet.** `InfoButton(onClick, marked)`: an `IconButton` (48 dp touch target, S-8) with `OpenControlIcons.Info`, content description
"Details" / "Details — not current" when marked; the marker is a small dot badge drawn at the icon's top-right in `colorScheme.tertiary` **plus** the changed
content description (never colour alone, S-9). The sheet shows the exact current strings (list below), each line from the same helper as today.

**Pull.** `PullToRefreshBox` around each tab's scrollable content. `OpenControlActions.onPullToRefresh: (PullTab) -> Job?`: `:app` decides per D-10 from the
same state the screens show and returns the application-scope `Job` it launched (or `null` when it only opened a system prompt / did nothing); `:ui` shows
the indicator while that `Job` is active (`isRefreshing = job?.isActive`, cleared by `job.join()` in a `LaunchedEffect`) — no fixed-duration spinner.
Not connected + paired ⇒ `connect()`; Bluetooth off ⇒ `onRequestEnableBluetooth`; permission missing ⇒ `onRequestPermissions` (or app settings when
blocked); not paired ⇒ `onPair` — exactly what that state's own button does today (`ConnectionScreen.kt:104-116`, `:152-165`). Connecting ⇒ nothing.

| Screen | Main surface (after) | Moves into (i) (exact strings, file:line today) | Stale/unread marker | Pull while Ready |
|---|---|---|---|---|
| Connection — status | `ElevatedCard`; `Ready` ⇒ `primaryContainer` + check icon + "App control: ready"; Android line, session line, error text (`ErrorExplanation`, `ConnectionScreen.kt:239-244`) and Connect/Retry/Disconnect stay | — (no times on this card) | — | Refresh battery |
| Connection — Safe Mode | `Card` in `errorContainer`, same title + `safeModeText` (`:251-267`) | — | — | — |
| Connection — battery | `ElevatedCard` "Battery" + (i); a `Row` of 3 columns Left \| Case \| Right: icon (Earbud / Case), "97%" or "Battery unavailable" (no bar), `LinearProgressIndicator(progress = percent/100)` for a known value; `tertiary` bar + bolt icon ("charging") when that bud's `ChargingReading.charging` is true; no bolt when false or `null`. Error lines stay visible: `batteryRefreshError` (`:307-309`), Case unavailable reason (`:303-305`). "Refresh battery" button stays. Firmware line (`BudsInfoCard`, `:275-282`) moves into this card's (i) | the explanation (`:295-299`), `budLine(Left/Right)` (`:393-408`) and `caseLine` (`:414-424`) — **the full current lines**, `CASE_REFRESH_NOTE` (`:450`), firmware | a stale/unread/earlier-session value: column dimmed (alpha 0.6) **and** the (i) badge + "not current" description | — |
| ANC | four large mode buttons (2 × 2, 72 dp), labels as the Controls list names them (Noise cancellation, Off, Adaptive, Transparency — `ControlsScreen.kt:129-134`); the Buds' current mode = filled button with a check icon, others outlined; unknown ⇒ none filled + "ANC mode: unknown" (`AncScreen.kt:85`); `ANC_NOT_ALLOWED_TEXT` visible without the time; Refresh + "Add ANC Quick Settings tile" kept; `MessageStreamNotice` stays | "ANC mode: X (updated HH:MM:SS)" (`:84-88`), `ancNotAllowedLine(checkedAt)` (`:110-111`), "Connection: …" (`:80`), `MessageStreamHint` (`ConnectionBanner.kt:75-82`) | mode known but from an earlier claim is not flagged today (ADR-032: "last known" by design) — no marker added; unknown = no filled button | ANC Refresh |
| Sound | EQ card (sliders, presets 3 + 2), settings card (balance, mono, conversation detection); `EqStatusNotice`'s error / "Reading the Buds' current EQ…" texts stay visible (`EqScreen.kt:183-194`) | "EQ updated: HH:MM:SS" (`:196-198`); per setting `settingTime` (`SettingsUi.kt:47-51`) and the balance's "Left 40 · read …" (`EqScreen.kt:216`) — balance value text stays, only " · read …" moves | unread setting: control disabled (U-1, as today) + (i) badge | `refreshEq()` then `refreshSettings()` |
| Controls | touch-controls switch, press-and-hold chips per bud, the field-12 list (unchanged rules), in-ear switch; `DIGITAL_ASSISTANT_NOTE`, `ANC_MODE_LIST_MIN_TEXT`, `IN_EAR_DETECTION_OFF_NOTE` stay visible (they are rules, not times) | `settingTime` of each (`ControlsScreen.kt:107`, `:172`, `SettingsUi.kt:100`) | unread: disabled (as today) + (i) badge | `refreshSettings()` |
| Find | `FilledTonalButton` Ring Left / Ring Right, `OutlinedButton` Stop; `RingingNotice` visible (`FindMyBudsScreen.kt:81-84`); `MessageStreamNotice` visible | `MessageStreamHint` | — | checkpoint (recommend: nothing) |
| Debug | unchanged content (switch, Export, frames) | — | — | no pull |

**Icons (D-5)**, `OpenControlIcons` in `:ui`, 24 dp, one `ImageVector` each, filled with `SolidColor(Color.Black)` like the core icons (tinted by `Icon`'s
`LocalContentColor`, so they follow light and dark themes): Earbud ← Material Symbols `earbuds`; Case ← drawn from scratch (rounded case outline with a lid line;
no symbol exists); Charging ← `bolt`; Debug ← `bug_report`; Info ← `info`; tabs: Connection ← `bluetooth`, ANC ← `noise_control_on`, Sound ← `equalizer`,
Controls ← `touch_app`, Find ← `notifications_active` (the core `Notifications` bell is the nearest core icon; the "active" variant says "ringing"). Path data
is the SVG `d` string of the Outlined 24 px symbol (viewBox `0 -960 960 960`), parsed with `PathParser`; each icon names its symbol and "Apache-2.0" in a comment.

## 5. Phase A — D-11 specification

- `BudsRepository`: `suspend fun refreshSettings(): BudsResult<Unit>` — KDoc: "Re-reads the DLCI 0x02 settings on the user's request (a pull on Sound/Controls):
  the Connect-time `ReadSetting` pass once more — same fields and order ([SETTING_READ_ORDER] 2, 4, 7, 12, 17, 19, 22), sequential, each ≤ 2 s, never retried
  (ADR-036). Requires an open session: otherwise nothing is sent. A field that is not answered keeps its last value and its own time; the reason is in [settingsError]."
- `BudsRepositoryImpl`: `if (state !is Ready) return Failure(ConnectionLost)` (mirrors `refreshEq`, `:771-774` — nothing sent, nothing set); then
  `_settingsError.value = null` (mirrors Connect, `:617`), `readSettings()` (unchanged), return `Failure(settingsError.error)` if a read failed, else `Success`.
  Reads are never gated (ADR-042) — in Safe Mode it reads, as at Connect. Serialised by `readSettings`' own `eqMutex`.
- `OpenControlActions`: no separate callback — reached only through `onPullToRefresh` (Sound: `refreshEq()` then `refreshSettings()` in one launched `Job`;
  Controls: `refreshSettings()`).
- Tests (`BudsRepositoryImplTest`, real `CAP-036` bytes via `answerReadsLikeCap036`/`Settings036`, frame numbers in the comments): (T1) a refresh while Ready sends
  exactly the 7 requests `READ_2_REQ` (frame 1445) … `READ_22_REQ` (1538) in that order, nothing else, and fills the settings with the new time; (T2) not Ready
  ⇒ `ConnectionLost`, `transport.sent` empty; (T3) an unanswered field (17) ⇒ one pass, no retry, the other fields updated, the unanswered one keeps its earlier
  value and time, `Timeout` in `settingsError`.
- Files outside `:ui`: `BudsRepository.kt`, `BudsRepositoryImpl.kt`, `BudsRepositoryImplTest.kt`, `MainActivity.kt` — nothing else (no fake repository exists).

## 6. Phase B — checkpoint answers (chat, 2026-09-29, `AskUserQuestion`, verbatim)

Summary shown first in chat (baseline, what is built, decided otherwise, findings 1–7 of Phase A). Answers:

| Question (header) | Answer |
|---|---|
| Debug/back | "Full screen + back (Recommended)" — §2.4 text as offered; no ADR (§2.4 already bans Debug from the bottom bar). |
| (i) sheet | "Dialog per card + dot (Recommended)" — `AlertDialog` per card, 48 dp (i), dot + "Details — not current" description, stale value dimmed, unread setting disabled; the §3.1 wording as offered. |
| Battery | "As shown (Recommended)". |
| TopAppBar | "Allow @OptIn for it (Recommended)" — the second approved `@OptIn(ExperimentalMaterial3Api::class)` besides `PullToRefreshBox`. |
| Find pull | **"Refresh battery"** (not the recommended option) — a pull on Find while Ready runs *Refresh battery*. |
| ADR-036 | "No ADR, doc note (Recommended)" — `ARCHITECTURE.md` §3.1 settings row gains the pull; no `DECISIONS.md` change. |
| Extras | "F-1 slider honesty, F-2 app theme, F-4 §3.1 stale sentence" — **F-3 (Safe Mode greys out) not chosen**: Safe Mode stays enforced by the gate with its message. |
| HCI check | "CAP-064 section VIII (Recommended)". |
| UI tests | **"Add compose-ui-test"** — `androidx.compose.ui:ui-test-junit4` (BOM) + Robolectric, pinned in `libs.versions.toml`, justified per `AGENTS.md` §10, with a few tests. |

No `DECISIONS.md`/`PROTOCOL.md` change was approved or is needed. `ARCHITECTURE.md` §2.4 and §3.1 changes are approved as documentation of the chosen design.

## 7. What was built, per screen (Phases C–E)

| Item | Where | Notes |
|---|---|---|
| Icons (D-5) | `ui/…/OpenControlIcons.kt` | Earbud ← `earbuds`, Charging ← `bolt`, Debug ← `bug_report`, Info ← `info`, tabs Connection ← `bluetooth`, ANC ← `noise_control_on`, Sound ← `equalizer`, Controls ← `touch_app`, Find ← `notifications_active` — Material Symbols Outlined 24 px, © Google, **Apache-2.0** (named in a comment per icon); path strings verified equal to the fetched SVGs (script, 9/9). **Case** drawn for this project (rounded outline + lid seam, even-odd). No new dependency, no XML drawable. |
| Theme (F-2) | `ui/…/OpenControlTheme.kt`, `MainActivity.kt` | `dynamicLight/DarkColorScheme`, dark from `isSystemInDarkTheme()`. |
| (i) details (D-7) | `ui/…/Details.kt` | `InfoButton` (48 dp `IconButton`, dot in `tertiary` + description "…: Details — not current"), `DetailsDialog` (`AlertDialog`), `CardTitle`. |
| Pull (D-10) | `ui/…/PullRefresh.kt`, `OpenControlNavHost.kt`, `MainActivity.kt` | `pullActionFor` = D-10's table (Find → *Refresh battery*, the checkpoint choice); `PullToRefresh` shows the indicator while the returned job runs. |
| Scaffold / navigation (D-6) | `OpenControlNavHost.kt` | `TopAppBar` "OpenControl" + Debug action; 5 tabs; Debug a graph destination outside the pager with ← and system back. |
| Connection | `ConnectionScreen.kt` | status `ElevatedCard` (`primaryContainer` + check icon when ready, `errorContainer` when failed); Safe Mode `Card` in `errorContainer`; battery card with 3 columns (D-8); firmware line into the battery (i) (`firmwareLine`); screen title removed (top bar). |
| ANC | `AncScreen.kt` | 2 × 2 buttons, 72 dp, the current mode filled + check + `selected` semantics; "ANC mode: unknown" and the not-allowed sentence visible; times, "Connection: …" and the Message-Stream text in (i) (`ancDetailLines`). |
| Sound | `EqScreen.kt`, `SettingsUi.kt` | EQ card and "Balance and audio" card with (i) (`eqDetailLines`, `soundSettingsDetailLines`); F-1 sliders; balance disabled while unread. |
| Controls | `ControlsScreen.kt` | three cards (Touch controls, Press and hold incl. the field-12 list, In-ear detection) with (i) (`pressAndHoldDetailLines`, `settingTime`); all rules and notes unchanged. |
| Find | `FindMyBudsScreen.kt` | tonal Ring Left/Right, outlined Stop, ringing notice visible, `MESSAGE_STREAM_HINT_TEXT` in (i). |
| Debug | `DebugScreen.kt` | content unchanged; headline removed (title in the top bar). |
| D-11 | `BudsRepository.kt` (+8), `BudsRepositoryImpl.kt` (+11), `BudsRepositoryImplTest.kt` (+66) | as specified in §5. |
| UI tests | `ui/src/test/…/PullActionTest.kt` (4), `BatteryCardTest.kt` (5) | approved at the checkpoint. |

## 8. Gate, parity, mutations (Phase F)

| Check | Baseline | After |
|---|---|---|
| `:data` tests (debug / release) | 1564 / 1564 | 1567 / 1567 (+3 D-11) |
| `:hardware` | 51 / 51 | 51 / 51 |
| `:domain` | 25 | 25 |
| `:ui` | — | 9 / 9 (new, approved) |
| Lint | `:app` 1 warning (pre-existing) | same; `:data`/`:hardware`/`:ui` no issues |
| Compiler warnings | 3 distinct in `:hardware` | the same 3, none new |
| `@OptIn` | — | `OpenControlNavHost.kt:211` TopAppBar, `PullRefresh.kt:96` PullToRefreshBox (both approved) |
| Parity `android/{data,hardware,domain}/src` | 84 files | only the 3 D-11 files differ, additions only; 0 test lines removed; `transport.send(` 8 → 8; `SETTING_READ_ORDER`, timeouts, claim/subscription/re-open code untouched |
| Mutations M1 / M2 / M3 | — | caught 1 / 4 / 1 tests (see Progress) |

## 9. UI honesty review (task 14)

- **No UI-side copy of a Buds value.** Every `remember { mutableStateOf(…) }` in `:ui`: `Details.kt:68` (dialog open), `EqScreen.kt:229` / `:260` (the finger
  position **only while the finger is down** — F-1; `null` otherwise, so the knob shows the repository value), `PullRefresh.kt:99` (the running pull job). None
  holds a Buds value. The only new `LaunchedEffect` (`PullRefresh.kt:100`) waits for a job; it sends nothing.
- **Values change only on the Buds' report:** switches `checked = reading?.value` (`SettingsUi.kt`), chips/boxes from `settings` (`ControlsScreen.kt`), ANC filled
  button = `ancMode` (`AncScreen.kt`), sliders = repository value after release (F-1 — before `0057` a refused write left the knob at the finger value).
- **Every time and state word shown before `0057` is reachable in an (i), from the same helper:** battery — `budLine`, `caseLine`, the explanation,
  `CASE_REFRESH_NOTE`, firmware (`batteryDetailLines`, verified by `BatteryCardTest`); ANC — `ancModeLine` (the old line's code), `ancNotAllowedLine(checkedAt)`,
  "Connection: …"; EQ — "EQ updated: …"; settings — `settingTime` for every field, the balance with `balanceText`; Find/ANC — the Message-Stream text.
  **Kept visible on the main surface:** "Battery unavailable" (no bar), the Case "not reported / couldn't be requested" line, a failed Refresh, every error text
  (`userMessage`, `SettingsFailureNotice`, `MessageStreamNotice`, `NotConnectedBanner`), "ANC mode: unknown", the not-allowed sentence, "Reading the Buds'
  current EQ…", the ringing notice, the rule notes (Digital assistant, ≥ 2 modes, in-ear off).
- **Non-text marker:** a stale / earlier-session battery value → column dimmed + (i) dot + "not current" description (`BatteryCardTest`); an unread EQ or setting
  → (i) dot + control disabled (switches, field-12 boxes as before; the balance slider newly). Unread press-and-hold chips stay enabled as before (a write
  of an absolute value), none selected, (i) dot.
- **Safe Mode:** unchanged — reads continue; every write goes through the repository (`writeGate`, ADR-042) and is refused there with its message (F-3 not chosen).
- **Each pull calls exactly one D-10 action once** (`pullActionFor`, `PullActionTest`; `MainActivity` launches it once; a second pull is ignored while one runs).

## 10. Compliance (task 15)

No new permission and no manifest change (merged debug manifest: the same 5 `uses-permission`, activities `MainActivity` and the pre-existing tooling
`PreviewActivity`; `ui-test-manifest` is `testImplementation`, so it is not in it). `git diff --name-only -- '*.gradle.kts' '*.toml' '*AndroidManifest.xml'`:
`android/gradle/libs.versions.toml`, `android/ui/build.gradle.kts` — only the approved test dependencies (+ `isIncludeAndroidResources`). No
`com.google.android.gms`, no XML layout/drawable, no Google-owned artwork (Material Symbols are Apache-2.0 generic icons; the Case is own work). AGPL-3.0 header
on all 6 new Kotlin files. No new timer, polling loop, service or dispatcher (the pull uses the existing application scope). No logging added. Content
descriptions on every icon button (Debug, Back, (i), tab icons with their labels; decorative icons next to their text are `null`).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0057_FEATURE_RESULT_2026_09_28.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0057_FEATURE_RESULT_2026_09_28

## 11. Open items (for the maintainer on the phone)

- Everything in `APP_TESTPLAN.md` section O and `CAP-064` section VIII: gesture feel of pull vs. pager vs. sliders, the (i) times against the debug log, dark
  mode contrast, TalkBack reading of the dot ("Details — not current") and the bolt ("charging").
- The pull on Find runs *Refresh battery* (the maintainer's choice) — one DLCI 0x04 claim per pull, as the button.
- F-3 (grey out Sound/Controls in Safe Mode) was not chosen; `TODO.md` keeps it as an open note, with the re-check of the two `@OptIn`s on the next BOM bump.
- The pre-existing `:hardware` release-test flake (§ Progress, Phase 0) did not recur in the final gate; not investigated further (out of scope).
- Process note: this session was OOM-killed once during mutation check M3; the leftover mutation was found and removed on resume, and M1–M3 were re-run.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0057_FEATURE_RESULT_2026_09_28.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0057_FEATURE_RESULT_2026_09_28
