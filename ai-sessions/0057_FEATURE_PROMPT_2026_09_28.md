# 0057_FEATURE_PROMPT_2026_09_28.md — Material 3 UI overhaul: rebuild the screens' presentation (top app bar with a Debug action, five tabs, (i) info sheets, graphical battery, own Kotlin icons, pull to refresh / reconnect) within the maintainer's decisions of 2026-09-29, keeping ADR-045/046/047 as built

**Number:** 0057
**Category:** FEATURE
**Date:** 2026-09-28
**Title:** Assess the maintainer's "Material 3 UI Overhaul, Information Hierarchy, and Tab Refactoring" proposal (§2, verbatim) against the current app
(`ai-sessions/0052`, `0054`, `0056` builds), apply the maintainer's decisions already taken in chat (§3), settle the few remaining points at a checkpoint, then
build the presentation changes in `:ui`/`:app` plus one narrow, user-triggered settings re-read for pull to refresh — nothing else changes on the wire, and the
field-12 list, "Digital assistant" and the in-ear detection switch stay as built

---

## 0. How to use this prompt

You are an expert Android software architect, reverse engineer, Android UX designer (Material 3 / Jetpack Compose) and technical auditor. This prompt is for a
**fresh** Claude Code session in this repository. Run the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8).** Before any other action, take the time to read **in full**, in this order, to
understand the ground rules before touching the project: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md` (in particular §2 and §2.4 UI
Navigation Structure, §3.1 State Reconciliation / honest state, §5a, §8.1 Safe Mode, §9), `PROTOCOL.md` (at least §0–§2, §4.1, §4.3, §4.5 with its dated Updates,
§6), `DECISIONS.md` (**every** ADR, ADR-001 … ADR-047, with every dated Update — in particular ADR-006, ADR-024, ADR-032, ADR-034, ADR-036, ADR-042, ADR-043,
ADR-044, ADR-045, **ADR-046** and **ADR-047**), `TODO.md`. Then `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`, `ai-sessions/0052_FEATURE_PROMPT_2026_09_26.md`
and its RESULT, `ai-sessions/0054_FEATURE_RESULT_2026_09_28.md`, **`ai-sessions/0056_FEATURE_RESULT_2026_09_28.md` in full** (the current state of the Sound and
Controls tabs), `APP_TESTPLAN.md`, `SCREENSHOTS_PIXEL_BUDS_APP.md` (the official app's layout, for reference only — `AGENTS.md` §12: no Google assets) and
`CHANGELOG.md`'s last entries.

Read **every** Kotlin file under `android/ui/src/main/` **in full** before changing any of them (`OpenControlNavHost.kt`, `ConnectionScreen.kt`, `ConnectionBanner.kt`,
`AncScreen.kt`, `EqScreen.kt`, `ControlsScreen.kt`, `SettingsUi.kt`, `FindMyBudsScreen.kt`, `DebugScreen.kt`, `TimeFormat.kt`), plus `MainActivity.kt` (there is **no
ViewModel** and **no `BudsState` type**: `MainActivity` collects the repository's flows into `OpenControlUiState` and passes `OpenControlActions`), `BudsRepository.kt`,
`BudsRepositoryImpl.kt` (at least `connect`, `refreshEq`/`readEq`, `launchInitialEqRead`, `readSettings`, `SETTING_READ_ORDER`, `SETTING_READ_TIMEOUT_MS`, the
battery refresh and the ANC refresh), `BudsRepositoryImplTest.kt` (the settings-read tests), `BatteryStatus.kt`, `BudsSettings.kt`, `BudsError.kt`, `SafeModeGate.kt`,
the `ui`/`app` `build.gradle.kts` and `gradle/libs.versions.toml`. Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
**ai-sessions/0057_FEATURE_RESULT_2026_09_28.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and update that block at
the end of every phase **and** after every screen built (which items are done and verified, which files are touched but not yet built, the last gate result,
the checkpoint answers, where intermediate results live — the scratchpad directory: baseline logs, the checksum baseline; re-create them if the scratchpad is
gone). A resumed session reads this prompt, then the RESULT's Progress block, re-reads the files the unfinished step touched, and continues from there. It never
redoes a finished, recorded step, and never assumes an unrecorded step was done. The prompt keeps its number and date; the RESULT keeps growing in the same file.

**Nothing is taken on trust — not this prompt, not the proposal in §2, not an earlier RESULT.** Verify every file:line cited here before using it. If the code,
an ADR or a capture disagrees with this prompt, the code/ADR/capture wins — record the correction in the RESULT. The maintainer's decisions in §3 are binding;
if one turns out to be impossible or to conflict with a rule not named here, stop and ask — do not work around it.

---

## 1. What the maintainer asked for (chat, 2026-09-28 and 2026-09-29, translated)

1. You are an expert Android software architect, reverse engineer, Android UX designer and technical auditor. Implement improvements to the **UI** of the
   OpenControl app. First take the time to read the core files (`AGENTS.md`, `PROJECT_RULES.md`, `PROTOCOL.md`, `ARCHITECTURE.md`, `DECISIONS.md`, …) to
   understand the ground rules before starting.
2. Run everything in phases, strictly sequentially. When a token limit is hit, continue automatically later (§0).
3. §2 is a proposal for an improved UI. **Validate and assess it on all relevant aspects; use what is usable and improve what is needed.** The prompt author's
   assessment was reviewed by the maintainer in chat on 2026-09-29; the resulting decisions are in §3 and are binding.
4. **Ask the maintainer to decide where still needed: give a summary and a recommendation, so the maintainer can approve** (Phase B checkpoint).
5. The proposal's last line — "Begin with Phase A and output your plan. Wait for confirmation before modifying files." — is honoured by the Phase B checkpoint:
   no file other than the RESULT is modified before the maintainer has answered there.

---

## 2. The maintainer's proposal (verbatim — the object of the assessment; §3 overrides it where they differ)

> **Title:** Material 3 UI Overhaul, Information Hierarchy, and Tab Refactoring
> **Context:** The maintainer has approved a major visual overhaul of the OpenControl app to improve the UX using Material 3 patterns. The core philosophy
> ("Hardware is the absolute truth", Zero-GMS) remains strictly intact. However, technical explanations and raw timestamps will be hidden behind "Info" dialogs
> to clean up the primary UI.
>
> **Mandatory Reading:** Read `ARCHITECTURE.md` §2.4, `AGENTS.md`, and `0052_FEATURE_PROMPT_2026_09_26.md`.
> *Note:* This prompt focuses on UI/UX presentation. Do not alter the underlying Bluetooth transport, parsing logic, or the `StateFlow` structures in `:domain`
> and `:data`.
>
> **1. Global UI & Navigation Rules**
> 1. **TopAppBar:** Implement a `TopAppBar` with the title "OpenControl". Move the "Debug" screen access here as an `actions` icon (e.g.,
>    `Icons.Default.BugReport` or `Settings`). It must no longer be a bottom navigation tab.
> 2. **Bottom Navigation (`NavigationBar`):** Exactly 5 tabs: **Connection, ANC, Sound, Controls, Find**.
> 3. **Swipeable Pager:** Retain the `HorizontalPager` keeping the tabs in sync with the swipe gestures.
> 4. **Swipe down:** refresh connection or information
> 5. **Information Hierarchy (Timestamp Hiding):** Do not display raw `(updated HH:MM:SS)` text on the main UI surfaces by default. Instead, place an
>    `IconButton(Icons.Outlined.Info)` in the corner of relevant `ElevatedCard` or `OutlinedCard` components. Tapping this icon opens a `ModalBottomSheet` or
>    `AlertDialog` containing the verbose, honest explanations (e.g., "Left/Right %: read from the Buds when...") and the exact timestamps.
>
> **2. Screen Specifications**
>
> *Tab 1: Connection Screen* — **Status Indicator:** wrap the primary connection status in an `ElevatedCard`. If `ConnectionState` is `Ready`, use a subtle
> green container color (`MaterialTheme.colorScheme.primaryContainer`) for immediate visual feedback. **Graphical Battery Layout:** replace the text-heavy battery
> display with a 3-column `Row` (Left Bud | Case | Right Bud). Use generic Material icons (`Icons.Rounded.Earbuds`, `Icons.Rounded.Inbox`). *No Google proprietary
> assets.* Below each icon, show the percentage as text, and add a `LinearProgressIndicator`. If `isCharging` is true, display a small `Icons.Default.Bolt` next to
> the percentage and use a distinct color (e.g., `Color.Green` or `tertiary`) for the progress bar. If the Case is "Last seen", apply `Modifier.alpha(0.5f)` to
> the Case column to indicate it is not a live reading, and show a small "Last seen" label. **Safe Mode / Errors:** display `UnsupportedFirmware` or specific
> `BudsError` states as prominent `FilledCard` or `OutlinedCard` elements with clear warning colors (`errorContainer`).
>
> *Tab 2: ANC Screen* — display the 4 modes (Transparent, Adaptive, Off, Active) as prominent, centered buttons. Retain the `settableToggles == 0x00` check:
> disable the buttons and show the warning text if the buds are not actively worn.
>
> *Tab 3: Sound Screen (Formerly 'EQ')* — **Equalizer Sliders:** the 5 vertical (or horizontal) sliders for the EQ bands (Upper Treble, Treble, Mild, Bass,
> Low Bass). **Presets Layout:** display the 5 presets in exactly two rows: `3 + 2` layout using `chunked(3)` in a `Row` or a `FlowRow` (if permitted by
> compose-layout version). Row 1: `[HEAVY BASS] [LIGHT BASS] [BALANCED]`; Row 2: `[VOCAL BOOST] [CLARITY]`. **Volume Balance:** horizontal slider (-100 to +100).
> Visually indicate that `+100` = L (Left) and `-100` = R (Right). **Toggles:** add `Switch` component for **Mono audio**.
>
> *Tab 4: Controls Screen (NEW)* — **Touch Controls:** a master `Switch` for "Use touch controls". **Per-Bud ANC Checkboxes:** *Design Requirement:* remove the
> "Digital Assistant" option. The user explicitly requested to configure ANC modes per bud using checkboxes. *UI Layout:* create a section for "Left Bud" and
> "Right Bud". Under each, place 4 checkboxes: `Noise Cancelling`, `Off`, `Adaptive`, `Transparent`. *Action:* build the UI components for these checkboxes exactly
> as requested. **Toggles:** add `Switch` components for **Mono audio** and **In-ear detection**. **In-ear detection:** display the current state as a read-only
> text/status row.
>
> *Tab 5: Find My Buds Screen* — clean `FilledTonalButton` or `Button` elements for "Ring Left", "Ring Right", and "Stop". Hide the "Each action briefly
> claims..." warning text behind an Info icon/dialog.
>
> *TopAppBar Action: Debug Screen* — accessible only via the TopAppBar icon. Contains the `UnidentifiedFrame` list and the "Export debug log" button.
>
> **3. Execution Plan for Claude Code** — Phase A (Preparation): analyze `EqScreen.kt`, `ConnectionScreen.kt`, `OpenControlNavHost.kt`, and `MainActivity.kt`;
> ensure you understand how the `settings` state is currently hoisted. Phase B (Navigation & Scaffold): implement the `TopAppBar`, move the Debug screen out of the
> `HorizontalPager`, and rename the EQ tab to Sound; add the Controls tab. Phase C (Connection & Battery): build the 3-column graphical battery UI; extract
> timestamps into an Info dialog state. Phase D (Sound Tab): implement the 3+2 preset layout and add the Balance, Mono, and Conversation Detection UI elements.
> Phase E (Controls Tab): build the Switches and the Left/Right ANC checkboxes (remember the strict architectural blocker: mock the write action for Field 12).
> Phase F (Review & Lint): ensure no XML is used, no Google proprietary icons are imported, and run the baseline gate (`./gradlew assembleDebug lint`).
>
> **4. Evaluation Points for Functional Integrity (Non-Negotiable)** — before committing or claiming "done", evaluate and verify the changes against these
> points; if any point is breached, the implementation must be rejected:
> 1. **Unbroken Reactive State Flow (No UI State-Caching):** the UI remains 100% passive and reactive, listening purely to `StateFlow` (e.g., `BudsState`,
>    `ConnectionState`) from the repository/ViewModel. The UI NEVER caches state or makes "optimistic" changes (e.g., toggling a switch visually before receiving
>    the hardware's verified acknowledge/response frame). If a write fails or takes time, the UI must wait for the actual repository status.
> 2. **Strict Hardware-is-Truth Timing (Zero Discarded Data):** although timestamps like `(updated HH:MM:SS)` are visually hidden from primary views, they are
>    NEVER discarded from the domain model or underlying logic. The Info sheets display the EXACT same parsed timestamps, raw statuses, and last-seen statuses
>    without mutation or truncating.
> 3. **No Changes to Message Claims, Subscriptions or Timing Behavior:** the *Refresh battery* fix, DLCI claim-release sequence (to prevent the 1.5s linger
>    overlap), and runtime subscription behavior must not be altered, rescheduled, or throttled differently. Settings read/write sequences (sequential Reads
>    `2, 4, 7, 17, 19, 22` under 3s) during Connection happen in the exact sequence as current. NO looping or infinite retry logic may be added.
> 4. **Safety & Fallback Integrity (Write-Gate Parity):** all user actions, particularly writes like Volume Balance or touch controls, are still piped through
>    the exact `writeGate` (Safe Mode, `BudsError`, unknown firmware restriction). Safe Mode must still render the Sound/EQ and Controls screen read-only as
>    required by `0052`. The disputed `field 12` (ANC configuration) is strictly Gated and blocked, with the UI only triggering a mockup warning and NO
>    hardware-level RFCOMM write.
> 5. **Test and Mock Integrity:** the entire test suite in `:data`, `:hardware`, and `:domain` (and their `FakeBudsTransport` integrations) must remain green
>    with zero changes to assert statements or byte fixtures. Run a strict baseline check before and after to verify that the byte-by-byte encoding of RFCOMM
>    frames (`SettingsCodec`, write logic) for the new tabs is 100% byte-identical to the captures.
> 6. **Compliance and Privacy Audit (Zero Feature Bloat):** no new Android permissions (e.g. `INTERNET`), no new SDK dependencies, and no Google Mobile Services
>    (GMS/Play Services) references were added. The code must not inject polling, timers, background services, or async dispatchers that didn't exist before.
>    Everything remains strictly localized and responsive to the user's action or standard Bluetooth callback events.

---


## 3. The maintainer's decisions (chat, 2026-09-29) — binding; they override §2 where they differ

Each row names the proposal item, the decision, and what the session must verify before building. File:line references are from `631523a` — re-check them.

| # | Topic | Decision | Verify / notes |
|---|---|---|---|
| D-1 | **Field-12 list** (proposal: per-bud checkboxes, write "mocked") | **Keep as built** (`ai-sessions/0056`, ADR-046): **one** "Modes for press and hold (both buds)" list, real `WriteSetting`, ≥ 2 selected, shown only while a bud's press-and-hold is Noise control. **No** Left/Right lists — the write carries no side (🟢) and there is one list for both buds. Only the look changes. | `ControlsScreen.kt`; ADR-046 |
| D-2 | **"Digital assistant"** (proposal: remove) | **Keep.** It is the field-7 press-and-hold action (ADR-045), a real, working Buds setting. Only the look changes. | ADR-045; 0056 §6/§7 |
| D-3 | **In-ear detection** (proposal: read-only row) | **Keep the switch** built in `0056` (ADR-047), with its subtitle and its always-visible note. A live "is a bud worn" status is not built (W-12a, class C). | ADR-047; 0056 §7 |
| D-4 | **Scope of the Sound / Controls tabs** | **Keep everything as it is functionally; change only the presentation.** Mono audio and conversation detection stay on **Sound** (as today, `ai-sessions/0052`) — the proposal's second mono switch on Controls is **not** built (one setting, one place). Presets 3 + 2 via `chunked(3)` (`EqScreen.kt:136-142`) and the balance slider (Left at the left end, `+100` = Left on the wire, "Centre" snap, `EqScreen.kt:203-226`) stay. | `EqScreen.kt`, `ControlsScreen.kt` |
| D-5 | **Icons** (proposal: `Icons.Rounded.Earbuds`, `Inbox`, `Default.Bolt`, `BugReport`) | **Own Kotlin icons** (chosen via `AskUserQuestion`, 2026-09-29): an `OpenControlIcons` object in `:ui` with `ImageVector`s built in Kotlin (`ImageVector.Builder`/`materialPath`) — at least **Earbud**, **Case**, **Charging** (bolt), **Debug** (bug), **Info** (the (i) of D-7), plus tab icons where the core set has no fitting one. Path data may be taken from **Material Symbols (Apache-2.0)** — name the source symbol and the licence in a comment per icon and in the RESULT; or drawn from scratch. **No new dependency** (`material-icons-extended` rejected: very large; the app has no `isMinifyEnabled`, so every icon would ship), no XML drawable, no Google-owned artwork or "Pixel Buds" branding (`AGENTS.md` §12). | Confirm `material-icons-core` 1.7.0 lacks these (the jar lists ~50 icons: `Info`, `Refresh`, `Settings`, `Build`, `Warning`, …); fetch the Material Symbols licence page and quote it |
| D-6 | **Top app bar and Debug** | `TopAppBar` "OpenControl"; Debug leaves the bottom bar and the pager and becomes a top-bar action (the D-5 Debug icon, with a content description). Exactly **five** tabs: **Connection, ANC, Sound, Controls, Find**; the `HorizontalPager` and its two-way sync (`ai-sessions/0043`) stay. | This changes `ARCHITECTURE.md` §2.4 → draft the text and, if the section says so, an ADR — approval at the checkpoint |
| D-7 | **Times and state words behind (i)** | The exact times **and** the state words — "updated / read / changed HH:MM:SS", "last seen", "not read from the Buds yet", "from the last connection", "charging in the case (…)", and the explanations (e.g. "Left/Right %: read from the Buds when …") — move **off the main surface** into an info sheet/dialog opened by a **new, well-designed (i) icon** (`OpenControlIcons.Info`, D-5) on each relevant card. To see them the user taps (i). The texts and times are **the same strings, from the same helpers** (`TimeFormat.kt`, `settingTime`), not re-worded, truncated or re-computed. | See the honest-state note below the table |
| D-8 | **Battery** | Graphical 3-column layout (Left \| Case \| Right) with the D-5 icons, the percentage and a `LinearProgressIndicator`, **with these corrections to the proposal**: `BatteryLevel.Unavailable` shows **"Battery unavailable"** and **no** bar (never an empty/0 % bar); `isCharging` is `Boolean?` — `null` = **unknown**, shown as neither "charging" nor "not charging"; "last seen" (`isStale`) applies to **Left and Right as well as the Case**; per-bud charging comes from `leftCharging`/`rightCharging` (`ChargingReading`, incl. `fromEarlierSession`); `caseBatteryError`/`batteryRefreshError` stay reachable. | `BatteryStatus.kt` |
| D-9 | **Colours and components** | Colour-scheme roles only — `primaryContainer` is the theme's primary tone (dynamic colour), **not green**; no `Color.Green` (fails in dark theme and contrast). Material 3 has `Card` (filled), `ElevatedCard`, `OutlinedCard` — **no `FilledCard`**. Errors and Safe Mode use `errorContainer` with every specific `BudsError` text kept (`ConnectionScreen.kt` `userMessage`, `AGENTS.md` §8 — no generic catch-all). Never colour alone to carry a state. | m3.material.io colour roles |
| D-10 | **Pull (swipe down) to refresh / reconnect** | Per tab — see the table below. Each pull calls **one existing user action**, once; no automatic refresh, no timer, no retry loop. | Table below; the one new repository function is D-11 |
| D-11 | **Settings re-read** (needed for Sound and Controls) | A new `BudsRepository.refreshSettings()` that runs the **existing** `readSettings()` once — the same order **2, 4, 7, 12, 17, 19, 22**, the same per-read timeout (`SETTING_READ_TIMEOUT_MS`, verify), sequential under the same mutex, never retried — only on a user pull while `Ready`. **This is the only change allowed in `:domain`/`:data`.** | ADR-036 says reads are "sequential, wait ≤ 3 s and are never retried in a loop" and does not tie them to Connect — verify the full text and all Updates, and state at the checkpoint whether an ADR-036 Update (drafted) is needed |
| D-12 | **Connect-time sequence** | Unchanged: the read order at Connect is and stays **2, 4, 7, 12, 17, 19, 22** (the proposal's "2, 4, 7, 17, 19, 22" is stale). No change to claims, subscriptions, the Refresh-battery fix, the ADR-044 re-open, or timeouts. | `SETTING_READ_ORDER` |

**Pull-to-refresh behaviour (D-10):**

| Tab | While `Ready` | While not connected (Disconnected / a lost session / an error) | While connecting / discovering |
|---|---|---|---|
| Connection | *Refresh battery* (the existing action, `ConnectionScreen.kt:310`; a DLCI 0x04 claim-on-tap, ADR-032) | **Connect** (the existing Connect action = connect or retry) | nothing (the indicator ends at once) |
| ANC | the existing ANC **Refresh** (`AncScreen.kt:99`) | **Connect** | nothing |
| Sound | the existing **Read EQ again** (`refreshEq`) **then** `refreshSettings()` (D-11), sequentially | **Connect** | nothing |
| Controls | `refreshSettings()` (D-11) | **Connect** | nothing |
| Find | **decide at the checkpoint** — there is no ring-state read on the wire (verify); options: nothing new on the wire (the indicator just ends) *(recommend)*, or the Connection tab's *Refresh battery* | **Connect** | nothing |

Where Connect cannot start (Bluetooth off, permission missing, not paired), a pull does what the Connect button does today in that state (the existing
enable-Bluetooth / permission / pair prompt) — never a new dialog, never a silent no-op without a message. The pull indicator shows while the action runs and
ends when the repository reports the result; it is not a fake spinner with a fixed duration. `PullToRefreshBox` may be `@ExperimentalMaterial3Api` in the resolved
Material 3 version (BOM `2024.09.00`) — check; an `@OptIn` is acceptable only for that API and must be listed in the RESULT. Check gesture conflicts with the
`HorizontalPager` and the EQ/balance sliders.

**Honest-state note on D-7 (the maintainer's decision, to be implemented, not re-litigated).** `ARCHITECTURE.md` §3.1 says "Locally cached values are marked
provisional/stale until reconciled" and describes the visible "updated/last seen HH:MM:SS" strings; `AGENTS.md` §5 forbids fabricating a value. Moving the words
into the (i) sheet is allowed by the maintainer's decision; to keep a stale or unread value from **looking current**, the main surface keeps a **non-text marker**
for those states (e.g. the value dimmed and a small dot/badge on the (i) icon; for an unread setting the control stays disabled as today, U-1 of `0056`). Propose
the exact marker at the checkpoint (with a mock-up) and draft the matching `ARCHITECTURE.md` §3.1 wording change; "Battery unavailable" (D-8) and every error text
(D-9) stay visible on the main surface.

**Not approved, not in scope:** per-bud field-12 lists, removing Digital assistant, a read-only in-ear row, any other new request on the wire, a ViewModel, a
new Gradle dependency, Compose UI test dependencies (unless chosen at the checkpoint), anything on DLCI 0x08. **No** `PROTOCOL.md`/`DECISIONS.md` change unless
the maintainer approves it in this chat (memory rule "Approvals: confirm in chat").

---

## 4. Tasks

### Phase 0 — set-up

1. Create the RESULT file (§0). Record `git log -1`, `git status --short` and `git fetch && git log --oneline HEAD..origin/main`.
2. Baseline gate before any change: `cd android && ./gradlew --offline assembleDebug testDebugUnitTest test lint` (drop `--offline` only if the dependency cache
   is empty, and say so). Record the unit-test counts per module from the JUnit XML, the lint result per module and the Kotlin compiler warnings. Save
   `sha256sum` of every file under `android/{data,hardware,domain}/src` into the scratchpad (the parity baseline for Phase F). If the baseline is not green,
   stop and report.

### Phase A — assessment and design (no code change)

3. Go through **every** sentence of §2 and record, with file:line: *already built*, *usable as is*, *usable with a correction* (which), *decided otherwise in §3*
   (which D-row), or *not possible now* (why). Add anything the prompt author missed.
4. **External validation** (URL + exact sentence, fetched this session): Material 3 guidance for top app bars, navigation bars (number of destinations), bottom
   sheets vs. dialogs, cards, pull to refresh, progress indicators and colour roles (m3.material.io); the Compose Material 3 API reference for `TopAppBar`,
   `ModalBottomSheet`, `PullToRefreshBox`, `ElevatedCard`/`OutlinedCard`, `LinearProgressIndicator`, and whether each is experimental in the **resolved**
   `androidx.compose.material3` version (`./gradlew :ui:dependencies`); `ImageVector.Builder`; the Material Symbols licence (Apache-2.0) and the source symbols for
   D-5; Android accessibility guidance on contrast, touch-target size (the (i) button ≥ 48 dp) and not using colour alone.
5. Design, per screen, a concrete target: the component list, what stays on the main surface, what moves into each (i) sheet (the exact current strings, file:line),
   the non-text stale/unread marker, the pull behaviour, and which `OpenControlUiState` field / `OpenControlActions` callback each element binds to. Design the
   D-5 icons (source symbol or own drawing, size 24 dp, how they look in light and dark theme).
6. Specify D-11 exactly: the `BudsRepository` signature and KDoc, the implementation (reuse `readSettings()`; what it returns/sets on `Timeout`/not `Ready`/Safe
   Mode — mirror the existing reads), the `OpenControlActions` callback, and the tests (real bytes from the existing settings-read fixtures with frame/time in the
   comment — `AGENTS.md` §11; the same 7 requests in the same order as at Connect; no retry; nothing sent when not `Ready`). List every file outside `:ui` the
   design touches; anything beyond `MainActivity`, `BudsRepository`, `BudsRepositoryImpl`, the repository test and `FakeBudsRepository`-style fakes → stop and ask.

### Phase B — checkpoint (stop and ask, in this chat, via `AskUserQuestion`)

7. First show the maintainer **a plain-language summary**: what of §2 is already built, what will be built per D-row, anything Phase A found that conflicts with §3
   or with a rule, and your overall recommendation. Then one question per **remaining** decision; each option with pros and cons, one marked "(Recommended)", and
   a small ASCII mock-up in the preview:
   - **Debug and back** (D-6): how system back leaves Debug; the `ARCHITECTURE.md` §2.4 text and, if needed, the ADR draft;
   - **(i) sheet** (D-7): `ModalBottomSheet` or `AlertDialog`; one per card or one per screen; the (i) icon design; the non-text stale/unread marker and the
     §3.1 wording change;
   - **Battery mock-up** (D-8) with the states charging, unknown charging, last seen (L/R/Case), unavailable, Case error;
   - **Find pull** (D-10) while `Ready`;
   - **D-11**: whether an ADR-036 Update is needed (draft text in the preview);
   - **Compose UI tests**: none, or add (with the dependency and its §10 justification);
   - any further class-A improvement Phase A found (class B only with its draft).
   Record the answers verbatim in the RESULT. Build only what was chosen. Record an approved ADR/Update or `ARCHITECTURE.md` rule change (with the chat date)
   before the code that depends on it.

### Phase C — icons, scaffold and navigation

8. `OpenControlIcons` (D-5) with licence comments; `Scaffold` with the `TopAppBar` and the Debug action; five `NavigationBar` tabs; the pager on five pages with
   the `0043` sync; Debug as a separate destination outside the pager with the chosen back behaviour; edge-to-edge insets correct. Update `ARCHITECTURE.md` §2.4
   and the `TAB_DESTINATIONS` doc comment. Build and lint.

### Phase D — D-11 and pull to refresh

9. Build `refreshSettings()` with its tests (Phase A task 6), then a shared pull-to-refresh wrapper in `:ui` that takes the tab's "Ready" and "not connected"
   actions and the connection state (D-10). Build, run `:data:testDebugUnitTest` and lint.

### Phase E — screens

10. One sub-step per screen — Connection (status card, D-8 battery, error/Safe-Mode cards, (i) sheet), ANC (four prominent mode buttons with the `0054` I-1
    re-check kept — a tap while "not allowed" re-checks and switches only if the Buds now allow it; Refresh and the Quick Settings tile button kept), Sound,
    Controls (D-1…D-4, look only), Find (tonal buttons; the ringing notices of `FindMyBudsScreen.kt:89-91` stay visible, only the claim explanation moves into
    (i)), Debug (unchanged content: the `UnidentifiedFrame` list, debug-mode switch, "Export debug log"). Each: the (i) sheet, the stale/unread marker, the pull
    behaviour, Safe Mode read-only, no optimistic state. Build and lint after each screen; update the Progress block after each.

### Phase F — gate, parity, honesty, compliance

11. `./gradlew --offline clean`, then the full gate of task 2: all green, no new lint issue, no new suppression (list every `@OptIn`), no new compiler warning.
    Test counts: the baseline **plus only** the new D-11 tests.
12. **Parity:** `sha256sum` of every file under `android/{data,hardware,domain}/src` equals the baseline except the D-11 files (list them; show the diff is only the
    new function, its interface entry and its tests); no existing test assertion or byte fixture changed (`git diff` of the test sources shows additions only);
    `transport.send(` call sites unchanged in number and place; `SETTING_READ_ORDER`, timeouts, the ADR-032/043/044 claim, subscription and re-open code untouched.
13. **Mutation checks** for D-11 (script, restore, `sha256sum`): **M1** `refreshSettings()` sends while not `Ready`; **M2** it reads in a different order; **M3** it
    retries a timed-out read. Each must fail at least one test.
14. **UI honesty review** (the proposal's points 1, 2 and 4, corrected): for every control show at file:line that it renders the repository's value only (no
    `remember { mutableStateOf(…) }` holding a Buds value — a slider's position while the finger is down is the one allowed exception, as today; verify how
    `EqScreen`/`BalanceSlider` handle it); every time and state word shown before `0057` is reachable in an (i) sheet, from the same helper; every stale/unread value
    carries the non-text marker; "Battery unavailable" and every error stay visible; Safe Mode still makes Sound and Controls read-only; every write still goes
    through the repository (→ `writeGate`, ADR-042); each pull calls exactly the D-10 action once.
15. Compliance: no `INTERNET`, no new permission, no manifest change; `git diff --name-only -- '*.gradle.kts' '*.toml' '*AndroidManifest.xml'` empty (or only a
    checkpoint-approved test dependency); no `com.google.android.gms`; no XML layout or drawable; no Google-owned artwork (`AGENTS.md` §12); AGPL-3.0 headers on
    every new Kotlin file; no new timer, polling loop, service or dispatcher; no MAC address at `INFO` or above; content descriptions on every icon button.

### Phase G — test plan and documentation

16. `APP_TESTPLAN.md`: a header note "updated for `ai-sessions/0057`" and steps for the maintainer on the Pixel 9a: navigation (tabs, swipe, Debug and back),
    pull on each tab in each connection state, each battery state, each (i) sheet (same times as the log), the stale/unread marker, Safe Mode read-only, a failed
    write keeping the old value, light and dark theme. D-11 puts a known request on the wire again: add a short HCI check (the 7 reads after a pull on Controls,
    in order, nothing else) to the next capture skeleton that tests the app — check `id_registry.csv`/`CAP-064-EVENT-NOTES.md` and propose where at the checkpoint.
17. Documentation, only what changed: `ARCHITECTURE.md` §2.4 and §3.1 (as approved), `PROJECT.md` only if a scope line changes, `TODO.md`, `CHANGELOG.md`,
    `README.md` (status block), `ai-sessions/INDEX.md` (update the 0057 row). Run `python3 scripts/ensure_footers.py` and `python3 scripts/lint_docs.py` — exit 0.
18. Finish the RESULT: a plain-language summary first (what the user will see differently), the §2 assessment table, the checkpoint answers, per screen what was
    built and where, the icons with their sources and licence, the gate/parity/mutation tables, the honesty review, compliance, the external sources (URL + quoted
    sentence), open items (what the maintainer must check on the phone). Set the Status per `AI_SESSION_LOG_PROCEDURE.md` §4.
19. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes: Conventional Commits, one commit per concern (`feat(ui)` for the
    UI; `feat(data)` for D-11 and its tests; `docs` for the documentation and the session files; a separate `docs(adr)` if an ADR/Update was approved), each with a
    *why* and ending with the attribution line from the session's system reminder; `git fetch` and `git log origin/main..HEAD` before pushing (rebase if CI added a
    commit, never force); nothing from a build directory, `android/.kotlin/`, `.vscode/` or `__pycache__` staged.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Presentation only, plus D-11.** No change to `:data`, `:hardware` or `:domain` except `refreshSettings()` and its tests; no change to the Connect sequence,
  claims, subscriptions, timeouts or the re-open logic. A UI wish that needs more is a checkpoint question — never a silent addition.
- **Keep what is built** (D-1…D-4): one field-12 list, Digital assistant, the in-ear detection switch, mono and conversation detection on Sound.
- **Honest state** (`AGENTS.md` §5, `ARCHITECTURE.md` §3.1 as amended at the checkpoint): a value changes on screen only after the Buds report it; a stale,
  unread or earlier-session value carries the non-text marker; "Battery unavailable" instead of a number or an empty bar; unknown charging is not "not charging".
- **No optimistic UI, no UI-side cache** of Buds values; no `LaunchedEffect`/timer that sends anything; a pull calls one D-10 action once.
- **ADRs and FACTs only with approval in this chat** (`AGENTS.md` §6, memory rule "Approvals: confirm in chat").
- **Dependencies** (`AGENTS.md` §10): none new (D-5); a test dependency only if approved at the checkpoint, pinned in `libs.versions.toml`, with its justification.
- **Compose / Material 3 only**, Kotlin only, no XML layouts or drawables, no hidden APIs, colour-scheme roles only, text or a shape alongside every colour cue,
  content descriptions on every icon and icon button, touch targets ≥ 48 dp.
- **No Google branding** (`AGENTS.md` §12): generic iconography; Material Symbols path data only with the Apache-2.0 licence named per icon.
- **Evidence.** Every claim in the RESULT links to a file:line, an ADR sentence or a fetched source (URL + exact sentence); D-11 tests use real capture bytes.
- **Subagents.** A subagent may only read and report; every write is done in the main session and verified.
- **Files.** Before deleting, moving or renaming any file, diff a fresh listing of the source against the copy (checksums); never `rm -rf` from memory.
- **Commits.** Only after the maintainer confirms the final summary (task 19).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0057_FEATURE_PROMPT_2026_09_28.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0057_FEATURE_PROMPT_2026_09_28
