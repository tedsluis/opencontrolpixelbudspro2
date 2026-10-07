# 0078_MAINTENANCE_RESULT_2026_10_07.md — Toolchain and dependency upgrade: Gradle, AGP, Kotlin, KSP, the Compose BOM, Hilt and the rest of the catalog, with proof that nothing changes

**Number:** 0078
**Category:** MAINTENANCE
**Date:** 2026-10-07
**Title:** Upgrade the build toolchain and every dependency of `android/gradle/libs.versions.toml` to current stable releases, each pinned and justified; re-check the Material 3 opt-ins and `TabRow`/`PrimaryTabRow`; add `distributionSha256Sum`; align the CI actions and the documents; prove with the gate, test counts, mutations and an APK comparison that the app's behaviour does not change
**Status:** complete (committed and pushed to `maintenance/0078-toolchain-upgrade`, pull request opened — see Commits)

## Progress

- **Done:** reading; Phase 0 (baseline gate, artefacts, mutations); Phase A (inventory); Phase B (checkpoint, two rounds); Phase C (steps 1–8, audit);
  Phase D (final gate, mutations, release path, compliance); Phase E (documents, `ensure_footers.py`, `lint_docs.py`).
- **Next:** CI on the pull request; the maintainer merges.
- **Touched, unverified:** none.
- **Intermediate results:** session scratchpad `…/scratchpad/0078/` (`base/`, `after/`, `meta/`, `logs/`, `mutate.py`, `counts.py`, `libs.py`).

## Summary (plain language)

The build tools and every library of the app are now current: Gradle 9.7.1 (its download is checked against the official SHA-256), the Android Gradle
plugin 9.3.3 — which now compiles Kotlin itself — Kotlin 2.4.20, Hilt 2.60.1, Compose BOM 2026.09.00 with Material 3 1.4.0, the AndroidX libraries,
coroutines, JUnit 6 and Robolectric 4.17. The newest Gradle (9.8.1, released today) and AGP (9.4.1) were not taken: Kotlin 2.4.20 is documented as fully
supporting Gradle up to 9.7.0 and AGP up to 9.3.1. The app is compiled against Android 17 (API 37) because four libraries require it; it still targets and
requires Android 14 (API 34), so it behaves as before (ADR-029 Update, approved). Raising the target is left for its own session with a phone test.

What changed in the app's code is small and does not change behaviour: one redundant cast, one `Uri.parse` written as its KTX equivalent, a lint note on
icon path data, the licence text read through `LocalResources`, the pull-to-refresh opt-in removed (no longer experimental), and the settings menu's `TabRow`
kept with its new deprecation warning suppressed so it looks the same. The `TopAppBar` opt-in had to stay: I first read it as stable, the compiler showed
that one of its parameter types is still experimental. In the tests: the Compose test rule moved to its v2 form, the unused Kotest library was removed,
and one test now pins the order of the settings-menu tabs — a gap the baseline mutations found.

Proof: the full build, tests and lint pass offline with the same test counts as before (plus that one new test), all seven deliberate faults are caught
(one more than before), the permissions and app components are identical (no `INTERNET`), and no network, analytics or ads library appears in any
dependency tree. The debug APK grew from 28 to 35 MB — the newer AndroidX/Compose libraries and 161 code-free stub classes that the build tool (D8) now
generates; the app's own code is the same size. **Not tested on the phone**: the upgrade ships with the next release, whose capture run should include a
quick pass over every tab, the settings menu (its tab colours/indicator unchanged), pull-to-refresh, the licence dialog and a connect/ANC/EQ round.
`PROTOCOL.md`: §0–§2 read; this session changes no protocol behaviour and no protocol text.

## Phase 0 — set-up and baseline (no change)

**Git and machine (task 1).** `git log -1`: `42660fd Merge pull request #10 from tedsluis/maintenance/0077-decisions` on `main`; `git status --short`: the two
0078 files untracked; `git fetch && git log --oneline HEAD..origin/main`: empty (exit 0). Branch `maintenance/0078-toolchain-upgrade` from `origin/main`.
`java -version`: Temurin 21.0.4+7. `./gradlew --version`: Gradle 8.9 (Kotlin DSL 1.9.23, daemon JVM 21.0.4). SDK (`$ANDROID_HOME` = `~/Android/Sdk`):
`platforms/android-34`, `build-tools/34.0.0`; `cmdline-tools/latest` has `apkanalyzer`. Memory: 15 GB; a VS Code Java-extension Gradle daemon (≈ 1 GB RSS,
not started by this session) was running throughout — noted, not touched.

**Baseline gate (task 2).** `cd android && ./gradlew --offline --max-workers=2 --warning-mode all clean assembleDebug testDebugUnitTest test lint` → `BUILD
SUCCESSFUL in 3m 43s`, exit 0 (scratchpad logs/base-gate.log). JUnit XML (`scratchpad/0078/counts.py`):

| Module | Task | Tests | Fail | Err | Skip |
|---|---|---|---|---|---|
| `:data` | `testDebugUnitTest` / `testReleaseUnitTest` | 1627 / 1627 | 0 | 0 | 0 |
| `:domain` | `test` | 37 | 0 | 0 | 0 |
| `:hardware` | `testDebugUnitTest` / `testReleaseUnitTest` | 64 / 64 | 0 | 0 | 0 |
| `:ui` | `testDebugUnitTest` / `testReleaseUnitTest` | 69 / 69 | 0 | 0 | 0 |

Lint (`lint-results-debug.xml`): `:app` 0, `:data` 0, `:hardware` 0, `:ui` 1 (`ModifierParameter`, Warning — pre-existing). `w:` lines: 0 (`grep -c "^w: "`).
Gradle deprecation (the only one, printed at `:data:testDebugUnitTest`): *"The automatic loading of test framework implementation dependencies has been
deprecated. This is scheduled to be removed in Gradle 9.0. Declare the desired test framework directly on the test suite or explicitly declare the test
framework implementation dependencies on the test's runtime classpath."* Other build warning: `WARNING: The option setting
'android.experimental.enableTestFixturesKotlinSupport=true' is experimental.`

**Baseline artefacts (task 3)**, all in `scratchpad/0078/base/`: `app-debug.apk` (28,259,172 bytes, SHA-256 `f61e5817…`); `aapt2 dump badging` and `dump
xmltree --file AndroidManifest.xml` (build-tools 34.0.0); `unzip -l`; the dex class list with **`apkanalyzer dex packages --defined-only`** (cmdline-tools
latest; `dexdump` not used); the resolved trees `:app` `releaseRuntimeClasspath` / `debugRuntimeClasspath`, `:data`/`:hardware`/`:ui` `releaseRuntimeClasspath`,
`:domain` `runtimeClasspath`; the merged release manifest (`:app:processReleaseMainManifest`). Badging: `versionCode='10100' versionName='1.1.0'
compileSdkVersion='34'`, `sdkVersion:'34'`, `targetSdkVersion:'34'`, permissions `BLUETOOTH_CONNECT`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`,
`FOREGROUND_SERVICE_CONNECTED_DEVICE`, `…DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. Merged manifest components: 1 `<activity>`, 2 `<service>`, 1 `<provider>`,
1 `<receiver>`.

**Baseline mutations (task 4)** — `scratchpad/0078/mutate.py` (one textual mutation, `./gradlew --offline --max-workers=2 <task>`, failing tests from the JUnit
XML, the file written back and compared by SHA-256; one at a time):

| # | Mutation | File | Baseline | Killed by (first named) |
|---|---|---|---|---|
| M1 | field 11 written with `!on` | `BudsRepositoryImpl.kt` | killed (2) | "MP, channel 19: … CAP-070 A3668 …" |
| M2 | field 29 written as 0/1 | `SettingFrame.kt` | killed (6) | "head gestures on channel 19: CAP-070 A3614 / A3685 …" |
| M3 | channel 19's request address = 21's | `Maestro.kt` | killed (28) | "HG, channel 19 …", "F12 (a)/(b)/(e), channel 19 …" |
| M4 | field 15 written with `!on` | `BudsRepositoryImpl.kt` | killed (3) | "VEQ, channel 21: … CAP-070 A4800 …" |
| M5 | balance zigzag of −v | `SettingFrame.kt` | killed (5) | "balance: Right 4 on channel 19 = CAP-070 A3747 …" |
| M6 | the screen-reader description of "—" removed | `SettingsUi.kt` | killed (5) | `ControlsScreenTest` "connected and nothing read …" |
| M7 | settings-menu tab order swapped | `SettingsMenu.kt` | **survived** — `SettingsMenuTest` and (M7b) the whole `:ui` suite | — |

Every file restored byte-identical (SHA-256 before = after). M7 is a gap in the existing tests (tabs are found by label, their order is asserted nowhere), not
something the upgrade causes — checkpoint question.

## Phase A — inventory and plan (no change)

Sources (fetched 2026-10-07, raw text kept in `scratchpad/0078/meta/`): Gradle `https://services.gradle.org/versions/current` and `/versions/all`; Google Maven
`https://dl.google.com/android/maven2/<group path>/<artifact>/maven-metadata.xml`; Maven Central `https://repo1.maven.org/maven2/<group path>/<artifact>/maven-metadata.xml`;
release dates = the `Last-Modified` header of the version's POM. Stable = no alpha/beta/rc/M/dev/snapshot/Beta/RC in the version string.

### A.1 Versions (task 5)

| Item | Now | Newest stable (source line) | Proposed (set A) | Constraint (source, quoted) | Risk / code impact |
|---|---|---|---|---|---|
| Gradle | 8.9 | 9.8.1 (`versions/current`: `"version" : "9.8.1"`, built 2026-10-07 16:57 UTC) | **9.7.1** (built 2026-08-19) | KGP 2.4.20 "fully supported" Gradle `7.6.3`–`9.7.0` (`kotlin-web-site/docs/v.list`: `minGradleVersion` 7.6.3, `maxGradleVersion` 9.7.0); AGP 9.3 minimum Gradle 9.5.0 | Gradle 9: the test-launcher deprecation becomes an error → fix in the build files |
| AGP | 8.6.0 | 9.4.1 (2026-09-18) | **9.3.3** (2026-09-17) | KGP 2.4.20 AGP `8.5.2`–`9.3.1` (`v.list`); AGP 9.3: "maximum API level … 37", Gradle 9.5.0, Build Tools 36.0.0, JDK 17 (developer.android.com, AGP 9.3.0 release notes, Compatibility table) | **AGP 9 is a major:** built-in Kotlin ("you no longer have to apply the `org.jetbrains.kotlin.android` … plugin"), new DSL default, `android.onlyEnableUnitTestForTheTestedBuildType` false → true ("Only creates unit test components for the tested build type") — the release unit tests would disappear |
| Kotlin + Compose compiler plugin | 2.0.20 | 2.4.20 (Central `maven-metadata.xml`, newest without Beta/RC; 2026-09-07) | **2.4.20** | the Compose compiler plugin has the Kotlin version; KGP 2.0.20 itself is "fully supported" only to Gradle 8.8 / AGP 8.5 (the gradle-configure-project page's table) — **today's build is already outside that range** | new compiler warnings possible (`w:` checked) |
| KSP | 2.0.20-1.0.25 | 2.3.12 (2026-09-09) | **2.3.12** | KSP 2.3.0 release: "KSP version is no longer tied to the Kotlin compiler version"; 2.3.10: "Fix R-class resolution in KSP when AGP 9 built-in Kotlin is enabled", "so KSP works with Kotlin 2.4.0" | `ksp(…)` deprecation applies to KMP only (README table: "Deprecated in KMP") |
| Hilt / Dagger | 2.52 | 2.60.1 (2026-07-06) | **2.60.1** | dagger-2.59: "AGP 9 is now a requirement" for the Hilt Gradle plugin; 2.59.1: "minimum required AGP version to 9.0.0"; 2.60: "minSDK for Hilt is now 23" | ADR-028 unchanged (Hilt stays) |
| Compose BOM | 2024.09.00 (material3 1.3.0, ui 1.7.0) | 2026.09.00 (2026-09-09) | **2026.09.00** → material3 **1.4.0**, ui/foundation **1.12.1**, material-icons-core 1.7.8 (BOM POM) | `ui-android` 1.12.1 AAR metadata `minCompileSdk=37 minAndroidGradlePluginVersion=9.1.0` | opt-ins and `TabRow` (A.2) |
| kotlinx.coroutines | 1.9.0 | 1.11.0 (2026-05-07) | **1.11.0** | release notes: "Kotlin was updated to 2.2.20"; "Advanced the deprecation levels on …" | compile shows any deprecated call |
| lifecycle (runtime-ktx, runtime-compose, viewmodel-compose) | 2.8.6 | 2.11.0 (2026-06-17) | **2.11.0** | `lifecycle-runtime-compose-android` 2.11.0: `minCompileSdk=37`, AGP ≥ 9.1.0 | — |
| activity-compose | 1.9.2 | 1.13.0 (2026-03-11) | **1.13.0** | `minCompileSdk=36`, AGP ≥ 8.9.1 | — |
| core-ktx | 1.13.1 | 1.19.1 (2026-09-23) | **1.19.1** | `core` 1.19.1: `minCompileSdk=37`, AGP ≥ 9.1.0 | — |
| navigation-compose | 2.8.0 | 2.10.2 (2026-09-23) | **2.10.2** | `navigation-compose-android` 2.10.2: `minCompileSdk=37`, AGP ≥ 9.1.0 | — |
| datastore-preferences | 1.1.1 | 1.2.1 (2026-03-11) | **1.2.1** | `minCompileSdk=34` | — |
| JUnit 5 → JUnit 6 | 5.11.0 | 6.1.3 (2026-08-07) | **6.1.3** | JUnit 6.0.0 notes: "Minimum required Java version is now 17. Minimum required Kotlin version is now 2.2." | major: removed APIs not used here (`MethodOrderer.Alphanumeric`, `@CsvFileSource(lineSeparator)`) |
| kotest-assertions-core | 5.9.1 | 6.2.5 (2026-09-10) | **remove** (checkpoint) | — | **no test imports `io.kotest`** (`grep -rn "io.kotest" --include=*.kt` → 0 lines; positive control: the same grep for `org.junit.jupiter.api.Test` → 30 imports) |
| JUnit 4 | 4.13.2 | 4.13.2 | 4.13.2 | — | unchanged |
| Robolectric | 4.13 | 4.17 (2026-09-10) | **4.17** | release: "Robolectric 4.17 supports SDK 37"; the `:ui` tests pin `@Config(sdk = [34])` (8 classes) | it fetches its own `android-all` jar for SDK 34 once |
| javax.inject | 1 | 1 (`maven-metadata.xml` lists only `1`) | 1 | — | unchanged |
| compileSdk | 34 | — | **37** (forced by core 1.19.1, ui 1.12.1, lifecycle-runtime-compose 2.11.0, navigation-compose 2.10.2) | AGP 9.3 max API 37; SDK repository `repository2-3.xml`: `platforms;android-37.0` rev 2, channel `stable` | compile-time only; ADR-029 says compile = 34 → ADR-029 Update draft |
| targetSdk | 34 | — | **34** (unchanged unless chosen) | A.4 | — |
| Build-tools (README) | 34.0.0 | 37.0.0 (`build-tools;37.0.0`, stable) | ≥ 36.0.0 (AGP 9.3 minimum/default 36.0.0) | — | `release.sh` takes the highest installed folder |
| JDK | 21 | — | 21 (unchanged) | AGP 9.3 JDK minimum 17; JUnit 6 minimum 17 | none |

**Alternatives considered:** *set B, the last AGP 8 line* — AGP 8.13.2 (max API 36.1, Gradle ≥ 8.13, build tools 35.0.0), Gradle 8.14.5, Kotlin 2.3.21 (AGP
8.2.2–9.0.0, Gradle 7.6.3–9.3.0), KSP 2.3.12, Hilt 2.58 (the last release before AGP 9 became a requirement), compileSdk 36, BOM 2026.06.01 (ui 1.11.4
`minCompileSdk=35`, material3 1.4.0), core 1.18.0 (36), lifecycle 2.10.0 (35), activity 1.13.0 (36), navigation 2.9.8 (35), datastore 1.2.1 — no built-in-Kotlin
migration now, but AGP 9 remains to be done. *Set A′, the absolute newest* — Gradle 9.8.1 (built today) and AGP 9.4.1 — both above KGP 2.4.20's "fully
supported" maximum (9.7.0 / 9.3.1).

### A.2 The opt-ins and `TabRow` in material3 1.4.0 (task 6)

Evidence: the release's own sources, `https://dl.google.com/android/maven2/androidx/compose/material3/material3-android/1.4.0/material3-android-1.4.0-sources.jar`
(the androidx repository's `api/` files are not published per release on GitHub: api/1.4.0.txt → 404, and current.txt is the development version).

- **`TopAppBar`** (`commonMain/…/material3/AppBar.kt:214–216`): the overload this app calls carries `@OptIn(ExperimentalMaterial3Api::class)
  @Composable fun TopAppBar(…, expandedHeight: Dp = …)` — **no** `@ExperimentalMaterial3Api` marker; only the old overload without `expandedHeight` is
  `@Deprecated(level = HIDDEN) @ExperimentalMaterial3Api` (`:154–160`). I read this as "stable"; **that was wrong** — the parameter type
  `TopAppBarScrollBehavior` is still `@ExperimentalMaterial3Api @Stable interface TopAppBarScrollBehavior` (`:1401–1403`), so a call still needs the opt-in:
  without it the compiler reports `OPT_IN_USAGE_ERROR` "This material API is experimental …" at `OpenControlNavHost.kt:287` (Phase C step 5,
  scratchpad logs/step5-gate.log). The opt-in stays (corrected after the checkpoint answer; see Phase C).
- **`PullToRefreshBox`** (`pulltorefresh/PullToRefresh.kt:119–120`): `@Composable fun PullToRefreshBox(` — no marker; `rememberPullToRefreshState()` (`:584–586`)
  also none. ⇒ stable; the opt-in in `PullRefresh.kt:96` can go.
- **`PrimaryTabRow`** (`TabRow.kt:149–150`): `@Composable fun PrimaryTabRow(` — stable. **`TabRow`** (`:1324–1334`): `@Deprecated(level =
  DeprecationLevel.WARNING, message = "Replaced with PrimaryTabRow and SecondaryTabRow.", replaceWith = ReplaceWith("SecondaryTabRow(…)"))` — keeping it would
  add a deprecation warning (`w:`) to the build.
- **What changes on screen** (same file, `tokens/*.kt`): `TabRow` today = Primary-coloured selected label (`primaryContentColor` =
  `PrimaryNavigationTabTokens.ActiveLabelTextColor` = `Primary`) + a full-tab-width indicator (`SecondaryIndicator`). `SecondaryTabRow` (the `ReplaceWith`) =
  same full-width indicator, but the selected label in `OnSurface` (`SecondaryNavigationTabTokens.ActiveLabelTextColor`). `PrimaryTabRow` = Primary label
  (as now) + an indicator only as wide as the label text, 3 dp with rounded corners (`PrimaryIndicator`, `matchContentSize = true`).

### A.3 CI actions (task 7)

`gh api repos/<owner>/<repo>/releases/latest` and `…/git/ref/tags/<tag>` (an annotated tag dereferenced with `…/git/tags/<sha>`):

| Action | In use | Latest | Commit SHA of the latest | Note |
|---|---|---|---|---|
| `actions/checkout` | v7.0.1 (`.github/workflows/android.yml`), **v5.1.0** (`.github/workflows/lint-docs.yml`, `.github/workflows/update-docs-sidebar.yml`, `.github/workflows/update-sitemap.yml`) | v7.0.1 (2026-07-20) | `3d3c42e5aac5ba805825da76410c181273ba90b1` | both `runs: using: node24` (the action.yml at each tag) |
| `actions/setup-java` | v6.0.1 | v6.0.1 (2026-09-09) | `de7274f081f381c8f8158605e0321c36c376e2e6` | unchanged |
| `gradle/actions/setup-gradle` | v6.3.0 | v6.4.0 (2026-09-28) | `3f5f9adaf7d9fecd50b5935e54106014257a94e6` (tag object `b9bee63e…`) | v6.4.0 reports Gradle versions that are out of date / end-of-life as job annotations |
| `actions/setup-python` | v6.3.0 | v7.0.0 (2026-07-20) | `5fda3b95a4ea91299a34e894583c3862153e4b97` | major: "Remove the pip-install input" (not used here), ESM; `node24` in both |

### A.4 `targetSdk` (prompt §2) — the documented behaviour changes, quoted

- Android 15 (`developer.android.com/about/versions/15/behavior-changes-15`): *"Apps are edge-to-edge by default on devices running Android 15 if the app is
  targeting Android 15 (API level 35)."* *"If your app is not already edge-to-edge, portions of your app may be obscured and you must handle insets."*
- Android 16 (`…/16/behavior-changes-16`): *"For apps targeting Android 16 (API level 36), R.attr#windowOptOutEdgeToEdgeEnforcement is deprecated and disabled"*;
  *"the predictive back system animations … are enabled by default. Additionally, onBackPressed is not called and KeyEvent.KEYCODE_BACK is not dispatched
  anymore."*
- Android 17 (`…/17/behavior-changes-17`): *"For apps targeting Android 17 (API level 37), the read() method of the InputStream obtained from an RFCOMM-based
  BluetoothSocket now returns -1 when the socket is closed or the connection is dropped."* — this app's transport is RFCOMM.

What it would mean per screen: every tab and the settings menu sit in a Material 3 `Scaffold` with a `TopAppBar` and a bottom `NavigationBar`, which apply
insets themselves; the pager content, the dialogs and the Debug log would need checking on the phone; system back (settings menu → tab) would get the
predictive animation; at 37 the RFCOMM read loop's end-of-stream path changes. None of this can be verified without the phone — a hardware run.

### A.5 Documents to update afterwards (task 8)

`README.md` "Building from source" (build-tools, `android-34`), "Current state"/SDK line (§ "Compile/target/minimum SDK"); `WORKSTATION_PREPARATIONS.md` (the
SDK platform/build-tools section; JDK unchanged); `ARCHITECTURE.md` §1 ("Compile/target/minimum SDK: API 34"), §13 ("JUnit 5 + Kotest assertions"), §14
(catalog note), §15 (ADR-029 line); `DECISIONS.md` ADR-029 (Update, if approved); the opt-in comments (`OpenControlNavHost.kt`, `PullRefresh.kt`,
`SettingsMenu.kt`, `EqScreen.kt`'s `foundation-layout 1.7.0` remark — `FlowRow` still to be checked in 1.12.1); the `ui/build.gradle.kts` comment on
lifecycle 2.6.2/2.8.6; `TODO.md` §5; `CHANGELOG.md` `[Unreleased]`; `ai-sessions/INDEX.md`. `RELEASING.md` only if a command changes (none expected).

## Phase B — checkpoint (chat 2026-10-07, `AskUserQuestion`, answers verbatim)

Overview posted before the questions (summary): everything is two years old; set A = the newest stable set inside Kotlin 2.4.20's "fully supported" range;
AGP 9 is the big step (built-in Kotlin, Hilt ≥ 2.59 requires it, release unit tests off by default); `compileSdk` 37 is forced by four libraries; material3
1.4.0 makes the two opt-ins unnecessary and deprecates `TabRow`; Kotest is unused; M7 survives in the baseline; the SDK needs `android-37` and build-tools ≥ 36.

| Question | Answer (selected option, verbatim) |
|---|---|
| (a) Welke versieset wil je voor deze upgrade? | "Set A: nieuwste getest (Recommended)" — Gradle 9.7.1, AGP 9.3.3, Kotlin 2.4.20 (+compose plugin), KSP 2.3.12, Hilt 2.60.1, Compose BOM 2026.09.00 (material3 1.4.0, ui 1.12.1), coroutines 1.11.0, lifecycle 2.11.0, activity 1.13.0, core-ktx 1.19.1, navigation 2.10.2, datastore 1.2.1, JUnit 6.1.3, JUnit4 4.13.2, Robolectric 4.17, javax.inject 1, compileSdk 37, build-tools ≥ 36, JDK 21 |
| (b) compileSdk … Akkoord met verhogen + deze ADR-029 Update? | "37 + ADR-029 Update (Recommended)" — with the Update text shown in the preview |
| (c) targetSdk: houden op 34 of verhogen? | "Houden op 34 (Recommended)" |
| (d) TabRow is deprecated … Wat in SettingsMenu.kt? | "TabRow + @Suppress (Recommended)" |
| (d2) … De twee @OptIn's verwijderen? | "Beide verwijderen (Recommended)" |
| (e) De CI-actions: bijwerken en checkout gelijktrekken …? | "Gelijktrekken + bijwerken (Recommended)" — checkout v7.0.1 `3d3c42e5…`, setup-java v6.0.1 `de7274f0…`, setup-gradle v6.4.0 `3f5f9ada…`, setup-python v7.0.0 `5fda3b95…` |
| (f1) AGP 9 maakt standaard alleen nog unittests voor de debug-build … | "Release-tests behouden (Recommended)" — `android.onlyEnableUnitTestForTheTestedBuildType=false` with a comment |
| (f2) kotest-assertions-core … geen enkele test importeert io.kotest | "Verwijderen (Recommended)" |
| (f3) Mutatie M7 … Een test toevoegen die de volgorde vastpint? | "Test toevoegen (Recommended)" |
| (f4) … android-37 en build-tools ≥36 … Hoe installeren? | "AGP laat downloaden (Recommended)" |

The ADR-029 Update is approved in this chat (question (b), the text in its preview), per `AGENTS.md` §6.

**Second round (after Phase C step 7, the same chat).** Before it I reported that the `TopAppBar` opt-in had to stay (the compiler, step 5); then:

| Question | Answer (verbatim) |
|---|---|
| (g1) ui-test 1.12.1 markeert createComposeRule() als deprecated (14 compilerwaarschuwingen …). Wat te doen? | "Nu migreren naar v2" |
| (g2) Nieuwe lint-meldingen die volgen uit eerdere keuzes: OldTargetApi … en AndroidGradlePluginVersion … Wat te doen? | "Laten staan + vastleggen (Recommended)" |
| (g3) Lint LocalContextResourcesRead (nieuw) in SettingsMenu.kt:243 … | "LocalResources.current gebruiken (Recommended)" |

## Phase C — the upgrade, step by step (task 9)

Every gate: `./gradlew --max-workers=2 --warning-mode all clean assembleDebug testDebugUnitTest test lint` (online where a new artefact was needed, the final
one `--offline`); logs in `scratchpad/0078/logs/`. Counts are tests per task from the JUnit XML (`:data` debug / release, `:domain`, `:hardware` d / r,
`:ui` d / r); 0 failures, errors and skipped in every green run.

| Step | Change | Command result | Counts | Notes |
|---|---|---|---|---|
| 1a | Gradle wrapper 8.9 → 9.7.1 + `distributionSha256Sum` (`./gradlew wrapper --gradle-version 9.7.1 --distribution-type bin --gradle-distribution-sha256-sum acd53f1e…`, run twice so the 9.7.1 wrapper writes its own files) | `--offline` gate: exit 1 — "No cached version of org.jetbrains.kotlin:kotlin-stdlib:2.0.20 available for offline mode" (Gradle 9 keeps its own metadata cache); online: exit 1 — `:hardware:testDebugUnitTest` "Failed to load JUnit Platform. Please ensure that all JUnit Platform dependencies are available on the test's runtime classpath, including the JUnit Platform launcher." | — | the Gradle 8.9 deprecation became an error, as announced |
| 1b | `testRuntimeOnly(libs.junit.platform.launcher)` in `:data`, `:domain`, `:hardware` (catalog: `junit-platform-launcher` 1.11.0, = Jupiter 5.11.0's platform) | exit 0 (4m 34s) | 1627/1627, 37, 64/64, 69/69 | lint `:app` 30 (`GradleDependency` 24, `AndroidGradlePluginVersion` 6 — online version look-ups) |
| 2 | AGP 8.6.0 → 9.3.3; `compileSdk` 34 → 37 (4 modules, comment → ADR-029 Update); `kotlin-android` plugin removed (built-in Kotlin: root, 4 modules, catalog); `android.onlyEnableUnitTestForTheTestedBuildType=false` (f1) | exit 1 — "An exception occurred applying plugin request [id: 'com.google.dagger.hilt.android', version: '2.52'] > Android BaseExtension not found." | — | **steps 2 and 4 are coupled**: Hilt ≥ 2.59 requires AGP 9 (dagger-2.59 notes), Hilt 2.52 cannot apply on AGP 9 |
| 2+4 | + Hilt 2.52 → 2.60.1 | exit 1 — "Using kotlin.sourceSets DSL to add Kotlin sources is not allowed with built-in Kotlin. Kotlin source set 'debug' contains: […]/generated/ksp/debug/kotlin …" | — | **step 3 is coupled too**: AGP lifted the old KSP to 2.2.10-2.0.2 (AGP 9.0 notes), which still uses `kotlin.sourceSets`; KSP 2.3.10 "Fix R-class resolution in KSP when AGP 9 built-in Kotlin is enabled" |
| 2+3+4 | + Kotlin 2.0.20 → 2.4.20 (+ Compose compiler plugin), KSP → 2.3.12 | exit 0 (6m 1s); AGP downloaded `platforms;android-37.0` and `build-tools;36.0.0` (f4) | equal | new: Kotlin `USELESS_CAST` at `BudsRepositoryImpl.kt:1266`; lint `UseKtx` (`MainActivity.kt:316`), `TextConcatSpace` (`OpenControlIcons.kt:61`), `OldTargetApi` (`app/build.gradle.kts:51`); `ModifierParameter` (`:ui`) not reported in this run |
| 2+3+4 fix | code-neutral: the redundant cast removed (Kotlin 2.4 smart-casts `result`); `Uri.parse(url)` → `url.toUri()` (core 1.19.1 `androidx/core/net/Uri.kt:30`: `public inline fun String.toUri(): Uri = Uri.parse(this)`); `//noinspection TextConcatSpace` with its reason (path data needs no space between `Z` and `M`) | — | — | each with a comment naming this session |
| 5 | Compose BOM 2024.09.00 → 2026.09.00; `PullToRefreshBox` opt-in removed; `TabRow` kept with `@Suppress("DEPRECATION")` and the reason (d); the `TopAppBar` opt-in removed (d2) | exit 1 — `:ui:compileDebugKotlin` "OPT_IN_USAGE_ERROR … This material API is experimental …" at `OpenControlNavHost.kt:287` | — | my A.2 reading of `TopAppBar` was wrong (A.2 corrected): its parameter type `TopAppBarScrollBehavior` is still experimental |
| 5b | `TopAppBar` opt-in restored, comment corrected | exit 0 | equal | new: 14 × Kotlin `DEPRECATION` "`createComposeRule(…)` is deprecated. Use `androidx.compose.ui.test.junit4.v2.createComposeRule` instead." (7 `:ui` test classes × 2 variants); lint `LocalContextResourcesRead` (`SettingsMenu.kt:243`) |
| 6 | coroutines 1.11.0, lifecycle 2.11.0, activity-compose 1.13.0, core-ktx 1.19.1, navigation-compose 2.10.2, datastore-preferences 1.2.1 | exit 0 | equal | nothing new in production code |
| 7 | JUnit 5.11.0 → 6.1.3 (Jupiter and the launcher share the version; catalog `junit5` → `junit`), Robolectric 4.13 → 4.17, Kotest removed (f2) | exit 0 | equal | — |
| 7b | `android.experimental.enableTestFixturesKotlinSupport` removed: with built-in Kotlin `compileDebugTestFixturesKotlin` builds `FakeBudsTransport` without it (`hardware/build/intermediates/built_in_kotlinc/debugTestFixtures/…/FakeBudsTransport.class`; `BudsRepositoryImplTest` 145/145 on it) | targeted run exit 0 | — | removes the last `WARNING:` of the build |
| 8 | CI: `checkout` v7.0.1 in all four workflows, `setup-python` v7.0.0, `setup-gradle` v6.4.0, `setup-java` v6.0.1 unchanged (e) | — (runs on the pull request) | — | SHAs checked with `gh api repos/<r>/commits/<sha>` |
| g | second question round: v2 compose test rule (g1, import only — all 70 `:ui` tests pass without a timing change), `LocalResources.current` (g3), the M7 test (f3) | targeted runs exit 0 | `:ui` 70 | — |

**Final gate** (`--offline`, `clean`, the full command, scratchpad logs/final-gate.log): exit 0, `BUILD SUCCESSFUL in 4m 9s`.

| Module | Task | Baseline | After | Difference |
|---|---|---|---|---|
| `:data` | debug / release | 1627 / 1627 | 1627 / 1627 | — |
| `:domain` | `test` | 37 | 37 | — |
| `:hardware` | debug / release | 64 / 64 | 64 / 64 | — |
| `:ui` | debug / release | 69 / 69 | 70 / 70 | +1: the approved tab-order test (`SettingsMenuTest`, f3); no test renamed or lost (the other test-source changes are the `createComposeRule` import line) |

Lint: `:app` 1 (`OldTargetApi` — new, accepted and recorded, g2), `:data` 0, `:hardware` 0, `:ui` 1 (`ModifierParameter`, pre-existing). (`AndroidGradlePluginVersion`
appears only online — lint looks newer versions up; recorded, g2.) Compiler warnings: 0 (Kotlin 2.4 prints them as "Problem found: Kotlin compiler: …", not
`w:` — both counted: `grep -c "^w: "` 0, `grep -c "Problem found"` 0). Gradle deprecations: one left, *"Using a Project object as a dependency notation has been
deprecated. This will fail with an error in Gradle 10."* — raised inside AGP (`--stacktrace`: `com.android.build.gradle.internal.testFixtures.TestFixturesUtil
.getTestFixturesCapabilityForProject(TestFixturesUtil.kt:38)`), not fixable in this repository. The Gradle 9 deprecation of the baseline is gone (step 1b).

### Dependency audit (task 10, `AGENTS.md` §10)

Trees after the upgrade: `scratchpad/0078/after/tree-*.txt` (same six configurations as the baseline). `:app` `releaseRuntimeClasspath`: **118 → 135** libraries
(`scratchpad/0078/libs.py`, constraints and unresolved nodes left out). **Added (25)**, with the direct requesters from `./gradlew --offline -q
:app:dependencyInsight --configuration releaseRuntimeClasspath --dependency <d>` (`after/insight-*.txt`):

| Added | Pulled in by | What it is |
|---|---|---|
| `androidx.compose.runtime:runtime-annotation(-android)`, `runtime-retain(-android)` 1.12.1 | Compose runtime / `ui-android` 1.12.1 | Compose runtime split-outs |
| `androidx.core:core-viewtree` 1.0.0 | `core` 1.19.1, `activity` 1.13.0, lifecycle 2.11.0, `savedstate` 1.5.0 | view-tree owner helpers |
| `androidx.datastore:datastore-preferences-core-android`, `-preferences-proto`, `-preferences-external-protobuf` 1.2.1 | `datastore-preferences` 1.2.1 | DataStore's own preferences format (its repackaged protobuf; not a `.proto` build input of this project, ADR-041) |
| `androidx.lifecycle:lifecycle-viewmodel-savedstate-android` 2.11.0 | `activity` 1.13.0, `navigation-compose` 2.10.2, `hilt-android` 2.60.1 | lifecycle split-out |
| `androidx.navigation:navigation-common-android`, `-compose-android`, `-runtime-android` 2.10.2 | `navigation-compose` 2.10.2 | the Android variants of the (now multiplatform) navigation artifacts |
| `androidx.navigationevent:navigationevent(-android)`, `-compose(-android)` 1.1.2 | `activity` 1.13.0 (requests 1.0.0), `navigation-compose` 2.10.2 | back-event dispatch |
| `androidx.savedstate:savedstate-android`, `savedstate-compose(-android)` 1.5.0 | lifecycle 2.11.0, `ui-android` 1.12.1, `navigation-compose` 2.10.2 | saved-state split-outs |
| `androidx.window:window`, `window-core(-android)` 1.5.0 | `androidx.compose.ui:ui-android` 1.12.1 | window-size / fold information (adds two optional `<uses-library>`, task 11) |
| `org.jetbrains.kotlinx:kotlinx-serialization-json(-jvm)` 1.7.3 | `androidx.datastore:datastore-core-okio-jvm` 1.2.1 | JSON serialisation library (JetBrains); no I/O of its own |
| `org.jspecify:jspecify` 1.0.0 | `core` 1.19.1, lifecycle 2.11.0, `ui-android`, `window`, `dagger` 2.60.1 | nullness annotations only |

**Removed (8):** `material-icons-core` 1.7.0 (now only as `material-icons-core-android` 1.7.8 — the multiplatform split), `datastore-preferences-core-jvm` 1.1.1,
`navigation-common-ktx`/`navigation-runtime-ktx` 2.8.0, `kotlin-android-extensions-runtime`/`kotlin-parcelize-runtime` 1.9.22, `kotlin-stdlib-jdk7`/`-jdk8` 1.8.0.
92 libraries changed version (`libs.py … -v`).

**Network / analytics / ads scan.** `grep -i -E "firebase|crashlytics|analytics|play-services|gms|datatransport|okhttp|retrofit|sentry|mixpanel|amplitude|appsflyer|adjust|ads|measurement|ktor-client"`
over all six trees, before and after: **0 lines**. Over the dex class list (`apkanalyzer dex packages --defined-only`): before 0; after only
`android.adservices`, `android.adservices.adselection`, `.common`, `.ondevicepersonalization`, `.signals` (26 classes). Positive controls: the same `grep -i
"androidx.compose"` finds 202 lines in the after tree; a planted line `com.google.firebase:firebase-analytics:1.0` in a scratch file is found (exit 0).
**What the `android.*` classes are:** the after APK defines 161 classes in `android.*` outside `android.support.*` (adservices, `android.app.appfunctions`,
`android.crypto.hpke`, `android.health.connect`, …); `comm` against `dexdump app/build/intermediates/global_synthetics_dex/debug/generateDebugGlobalSynthetics/classes.dex`
shows **all 161 are in that file** — they are produced by AGP's own D8 global-synthetics task, not by a library (no tree contains such a group). Each is
`Access flags 0x1001 (PUBLIC SYNTHETIC)` with only a synthetic `<clinit>` and no code (`dexdump -d`, e.g. `android.adservices.ondevicepersonalization.IsolatedService`,
superclass `android.app.Service`), and no instruction in the APK references any of them (`dexdump -d` of all 16 dex files, `Landroid/adservices/` outside its own
classes: 0). 🟢 FACT: generated by D8, code-free, unreferenced; 🟡 HYPOTHESIS: D8 stubs framework classes newer than `minSdk` 34 because the build now compiles
against API 37 (none existed with `compileSdk` 34 = `minSdk`). Not a network, analytics or ads SDK.

**Direct dependencies — justification table (versions from the catalog; source = A.1):**

| Dependency | Purpose | Why no lighter / native alternative | Network / analytics / ads | Version |
|---|---|---|---|---|
| `kotlinx-coroutines-core` / `-android` | Coroutines + Flow (`AGENTS.md` §3) | the project's mandated async model | none (scan above) | 1.11.0 |
| `androidx.core:core-ktx` | `ContextCompat`, `toUri` | AndroidX baseline | none | 1.19.1 |
| `androidx.lifecycle:lifecycle-runtime-ktx`, `-runtime-compose`, `-viewmodel-compose` | lifecycle scopes, `collectAsStateWithLifecycle`; the last only aligns navigation's request (2.10.0 → 2.11.0) | AndroidX | none | 2.11.0 |
| `androidx.activity:activity-compose` | `setContent`, result launchers | AndroidX | none | 1.13.0 |
| `androidx.compose:compose-bom` → `ui`, `ui-graphics`, `ui-tooling-preview`, `material3`, `material-icons-core` (`ui-tooling` debug only) | the UI (`AGENTS.md` §3) | the mandated UI stack | none | 2026.09.00 |
| `androidx.navigation:navigation-compose` | `ARCHITECTURE.md` §2.4 | as justified there | none | 2.10.2 |
| `androidx.datastore:datastore-preferences` | Debug-mode and dark-mode switches | the decided persistence (`ARCHITECTURE.md` §2) | none | 1.2.1 |
| `com.google.dagger:hilt-android` (+ `hilt-compiler` via KSP, build time) | DI (ADR-028) | ADR-028 | none | 2.60.1 |
| `javax.inject:javax.inject` | `@Inject` in `:hardware` without Hilt | one annotation jar | none | 1 |
| Test only: `junit-jupiter-api/-params/-engine`, `junit-platform-launcher` | unit tests | — | not in the APK | 6.1.3 |
| Test only: `kotlinx-coroutines-test`, Compose `ui-test-junit4` / `ui-test-manifest`, `junit` 4, `robolectric` | coroutine and Compose UI tests on the JVM | — | not in the APK | 1.11.0 / BOM / 4.13.2 / 4.17 |
| Build: AGP, Kotlin (`kotlin-jvm`, Compose compiler plugin), KSP, Hilt Gradle plugin, Gradle | the build | — | developer machine only | 9.3.3 / 2.4.20 / 2.3.12 / 2.60.1 / 9.7.1 |
| **Removed:** `io.kotest:kotest-assertions-core` | — (never imported) | — | — | (was 5.9.1) |

### Manifest (task 11)

`diff` of the merged release manifest (`:app:processReleaseMainManifest`) and of `aapt2 dump xmltree`: permissions **identical** (`BLUETOOTH_CONNECT`,
`POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_CONNECTED_DEVICE`, the app's own `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`; no `INTERNET`);
**no** new `<activity>`, `<service>`, `<receiver>` or `<provider>` (still 1 / 2 / 1 / 1). Added: `<uses-library android:name="androidx.window.extensions"
android:required="false"/>` and `… "androidx.window.sidecar" … required="false"` — from `androidx.window` 1.5.0 (Compose ui 1.12.1): optional device
libraries for window/fold information, used only if the phone has them; not a component and not a permission. Badging: `compileSdkVersion='37'`,
`platformBuildVersionCode='37'`; `sdkVersion:'34'`, `targetSdkVersion:'34'` unchanged; `versionCode='10100' versionName='1.1.0'` unchanged.

### APK contents (task 12)

Debug APK 28,259,172 → 35,354,234 bytes; dex 27,620,884 → 34,647,772 bytes in 14 → 16 files (debug builds are not minified — no R8, `ARCHITECTURE.md` §14).
Per `apkanalyzer dex packages` (columns: defined methods, referenced methods, bytes): `androidx` 80,098 / 9,046,829 B → 100,749 / 10,956,714 B (of which
`androidx.compose` 6,767,060 → 7,899,094 B and `androidx.datastore` 544,227 → 776,662 B), `kotlin` 955,978 → 1,034,211 B, `kotlinx` 610,273 → 758,663 B, `dagger`
37,746 → 40,524 B, `android` (the D8 stubs) 82 → 238 defined methods; **the app's own code** (`io.github`) 4,424 → 4,272 defined methods, 503,733 → 503,959 B —
the same sources compiled by Kotlin 2.4 with AGP's built-in Kotlin (the code changes of this session are three lines). The growth is the libraries, not the app. Files: +31 / −50 / 52 size-changed (non-dex): the added ones are `META-INF/*.version` and licence files of the new artefacts, one
`kotlin/concurrent/atomics/atomics.kotlin_builtins`, and `res/` entries; the removed ones are `res/` — androidx.core 1.19's call-notification icons
(`ic_call_*`) and notification layouts now ship once (`res/drawable/`, `res/layout/`) instead of per density and `-v21` copies, which is why badging lists
fewer `densities` and drops `application-icon-120/480/640` (the launcher icon itself is unchanged, `res/mipmap-anydpi-v21/ic_launcher.xml`). New top-level
packages in the dex: only `android.*` (the D8 stubs) — no package outside AndroidX / Kotlin / Dagger / the app's own / the D8 stubs.

## Phase D — proof that nothing breaks

**Final gate (task 13):** the Phase C table's last row and scratchpad logs/final-gate.log; repeated after the last comment edits (scratchpad logs/final-gate2.log, see
"Final check" below). Counts equal to the baseline except `:ui` +1 per variant (the approved test); 0 failures/errors/skipped; lint: one new issue
(`OldTargetApi`, accepted, g2); compiler warnings: none.

**Mutations (task 14)** — the same `mutate.py`, after the upgrade (scratchpad mutations-after.txt):

| # | Mutation | Baseline | After | Killed by (after) |
|---|---|---|---|---|
| M1 | field 11 written with `!on` | killed (2) | killed (2) | "MP, channel 19: read CAP-069 7272 = on; OFF = CAP-070 A3668, ON = A3679 …" |
| M2 | field 29 written as 0/1 | killed (6) | killed (6) | "head gestures on channel 19: CAP-070 A3614 … / A3685 …" |
| M3 | channel 19's request address = 21's | killed (28) | killed (28) | "HG, channel 19 …", "F12 (a)/(b)/(e), channel 19 …" |
| M4 | field 15 written with `!on` | killed (3) | killed (3) | "VEQ, channel 21: … CAP-070 A4800 …" |
| M5 | balance zigzag of −v | killed (5) | killed (5) | "balance: Right 4 on channel 19 = CAP-070 A3747 …" |
| M6 | screen-reader description of "—" removed | killed (5) | killed (5) | `ControlsScreenTest` "connected and nothing read …" |
| M7 | settings-menu tab order swapped | **survived** | killed (1) | `SettingsMenuTest` "the tabs are Settings, Debug, Info from left to right, and Settings is selected when the menu opens" (new, f3) |

Every file restored byte-identical (SHA-256 before = after; `git diff --stat -- android/data/src/main` afterwards shows only this session's cast fix).

**Release build path (task 15):** `./gradlew --offline --max-workers=2 :app:compileReleaseKotlin :app:processReleaseMainManifest lintRelease` → exit 0
(scratchpad logs/release-path.log); release lint: `:app` 1 (`OldTargetApi`), `:ui` 1 (`ModifierParameter`), others 0. `python3 -I scripts/third_party_notices.py
<after/app-debug.apk> <scratch>/THIRD_PARTY_NOTICES.txt` (the debug APK — the script reads the release runtime classpath from Gradle and only the NOTICE files
from the APK) → exit 0, "136 libraries, licence texts ['Apache-2.0.txt']", **0 × "licence not found"** (the new `jspecify`, `kotlinx-serialization-json`,
`androidx.window` included, each Apache-2.0). No script change was needed.

**Compliance (task 16):** `git diff --name-only origin/main` → the four workflows, the build files, the catalog, the wrapper (`gradle-wrapper.jar`,
`.properties`, `gradlew`, `gradlew.bat`), `gradle.properties`, three production Kotlin files for the code-neutral fixes (`MainActivity.kt`,
`BudsRepositoryImpl.kt`, `OpenControlIcons.kt`), the approved UI files (`OpenControlNavHost.kt`, `PullRefresh.kt`, `SettingsMenu.kt`, `EqScreen.kt` comment),
seven `:ui` test files (the v2 import; `SettingsMenuTest.kt` also the new test), and the documents of Phase E. `grep -rn uses-permission
android/*/src/main/AndroidManifest.xml`: unchanged (`git diff origin/main -- 'android/*/src/main/AndroidManifest.xml'` → 0 lines). `transport.send`
added or removed: 0 (`git diff -U0 origin/main -- android | grep -E '^[+-]' | grep -c transport.send`; positive control: the same name occurs 8 times in
`android/data/src/main`). Every changed Kotlin file keeps `SPDX-License-Identifier: AGPL-3.0-or-later` (13 of 13). No `versionName`/`versionCode` change, no
`scripts/` change, no `release.sh` run, no tag.

## Phase E — documents

`README.md` ("Building from source": platform `android-37.0`, build-tools `36.0.0`, the toolchain; "Target platform": target/minimum 34, compile 37);
`WORKSTATION_PREPARATIONS.md` (the SDK section and its checks; JDK unchanged); `ARCHITECTURE.md` §1, §13 (JUnit Jupiter 6, Kotest removed), §14 (a toolchain
bullet), §15; `DECISIONS.md` ADR-029 Update (approved, question (b)); `TODO.md` §5 (the upgrade item replaced by what is deferred); `CHANGELOG.md`
`[Unreleased]`; `ai-sessions/INDEX.md`; code comments (`PullRefresh.kt`, `OpenControlNavHost.kt`, `SettingsMenu.kt`, `EqScreen.kt`, `ui/build.gradle.kts`, the
four `compileSdk` comments). `RELEASING.md`: no change (no command changed; `release.sh` takes the highest installed build-tools folder, now 36.0.0).
Not changed on purpose: the `javap … android-34/android.jar` commands in ADR-029's 2026-09-30 Update and `PROTOCOL.md` (evidence of that date; the
platform is still installed).

## Final check

The full gate repeated on the finished sources after the last comment edits (scratchpad logs/final-gate2.log): `./gradlew --offline --max-workers=2
--warning-mode all clean assembleDebug testDebugUnitTest test lint` → exit 0, `BUILD SUCCESSFUL in 3m 1s`; counts `:data` 1627/1627, `:domain` 37, `:hardware`
64/64, `:ui` 70/70, 0 failures/errors/skipped; lint `:app` 1 (`OldTargetApi`), `:ui` 1 (`ModifierParameter`), others 0; compiler warnings 0.

## Files read

In full: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `AI_SESSION_LOG_PROCEDURE.md`, `ARCHITECTURE.md`, `DECISIONS.md` (ADR-001 … ADR-057), `TODO.md`,
`android/gradle/libs.versions.toml`, `gradle-wrapper.properties`, `settings.gradle.kts`, the root and the five module `build.gradle.kts`, `gradle.properties`,
the four workflows, `scripts/third_party_notices.py`, `scripts/release.sh`. In part: `PROTOCOL.md` §0–§2 (as the prompt says); `RELEASING.md` §1–§8;
`README.md` "Building from source" and "Target platform"; `WORKSTATION_PREPARATIONS.md` (JDK and SDK sections); `CONTRIBUTING.md` (grep for build/test
lines); `CHANGELOG.md` (head); `ai-sessions/INDEX.md` (tail); `ai-sessions/0057` RESULT (the opt-in rows), `0074` RESULT §A.0/§H, `0076` RESULT (Phase 0, Phase
C, Commits); `OpenControlNavHost.kt`, `PullRefresh.kt`, `SettingsMenu.kt`, `SettingsUi.kt`, `EqScreen.kt`, `OpenControlIcons.kt`, `MainActivity.kt`,
`BudsRepositoryImpl.kt`, `SettingFrame.kt`, `Maestro.kt` and `SettingsMenuTest.kt` around the lines changed or mutated. `ai-sessions/0065` was not re-read
(the notices script itself was). External: the sources listed in Phase A (raw text in `scratchpad/0078/meta/`), the material3 1.4.0, ui 1.12.1,
ui-test-junit4 1.12.1, foundation-layout 1.12.1 and core 1.19.1 sources jars.

## Deferred documentation

Each is in `TODO.md` §5 ("After the toolchain upgrade of `ai-sessions/0078`"):

- (a) the upgrade is not hardware-tested — the next release's capture run covers it.
- (b) **M** — raising `targetSdk` (lint `OldTargetApi`) is its own session with a hardware run.
- (c) **M** — `TabRow` is deprecated in material3 1.4.0 and kept with `@Suppress("DEPRECATION")`; `PrimaryTabRow` / `SecondaryTabRow` is a visual choice.
- (d) re-check the `TopAppBar` opt-in on the next BOM bump (`TopAppBarScrollBehavior` is experimental).
- (e) Gradle 9.8 / AGP 9.4 once Kotlin's "fully supported" range includes them.
- (f) one Gradle-10 deprecation remains inside AGP (`TestFixturesUtil.kt:38`).

Not deferred and not done (outside this prompt's scope): `TODO.md` §2's rewording of `OpenControlNavHost.kt:238`'s `// TODO(verify)` "in the next session
that touches `:ui`" — this session touched `:ui` only for the build; the item stays in `TODO.md` §2 for the maintainer to assign.

## Commits

On `maintenance/0078-toolchain-upgrade` (from `origin/main` = `42660fd`); the maintainer answered *"Commit, push en PR (Recommended)"* in chat
2026-10-07. The four `build:` commits were assembled from the per-step contents (scratchpad `commit_stages.py`); the working tree ended byte-identical
to the gated state.

| Hash | Subject |
|---|---|
| `c6633c8` | build: Gradle 9.7.1 wrapper with distributionSha256Sum; declare the JUnit Platform launcher |
| `faf41b6` | build: AGP 9.3.3 with built-in Kotlin, Kotlin 2.4.20, KSP 2.3.12, Hilt 2.60.1, compileSdk 37 |
| `03b4954` | build: Compose BOM 2026.09.00, AndroidX and coroutines to current stable |
| `280b74e` | build: JUnit 6.1.3, Robolectric 4.17; remove the unused Kotest |
| `e6ebafe` | refactor: code-neutral fixes for the new compiler and lint checks; Material 3 1.4.0 opt-ins and TabRow |
| `890c6fb` | test(ui): v2 Compose test rule; pin the settings-menu tab order |
| `6f17d1f` | ci: actions/checkout v7.0.1 in all workflows; setup-python v7.0.0; setup-gradle v6.4.0 |
| `462b829` | docs: toolchain versions, ADR-029 Update (compileSdk 37), TODO and CHANGELOG |
| (this commit) | docs(session): ai-sessions/0078 prompt and result, INDEX row |

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0078_MAINTENANCE_RESULT_2026_10_07.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0078_MAINTENANCE_RESULT_2026_10_07
