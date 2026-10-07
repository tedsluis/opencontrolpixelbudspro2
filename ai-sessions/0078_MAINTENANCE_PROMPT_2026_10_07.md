# 0078_MAINTENANCE_PROMPT_2026_10_07.md — Toolchain and dependency upgrade: Gradle, AGP, Kotlin, KSP, the Compose BOM, Hilt and the rest of the catalog, with proof that nothing changes

**Number:** 0078
**Category:** MAINTENANCE
**Date:** 2026-10-07
**Title:** Upgrade the build toolchain and every dependency of `android/gradle/libs.versions.toml` to current stable releases, each version pinned and
justified (`AGENTS.md` §10); re-check the Material 3 experimental opt-ins and `TabRow`/`PrimaryTabRow`; add `distributionSha256Sum` to the Gradle wrapper;
bring the CI workflow actions and the documentation in line; prove with the full gate, the same test counts, mutations and an APK comparison that the
app's behaviour does not change; no feature, no release

---

## 0. How to use this prompt

You are an expert Android/Kotlin build engineer and technical auditor working in this repository. Run the phases of §4 **strictly in order**; record each
phase before starting the next. This is the "its own session after 1.0.1" the maintainer chose for the upgrade (`TODO.md` §5).

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action read **in full**, in this order:
`AGENTS.md` (in particular §1 Zero-GMS, §3 coding standards, §10 dependency policy, §11 testing, §12 licensing), `PROJECT_RULES.md`, `PROJECT.md`,
`ARCHITECTURE.md` (in particular §2.4 UI, §9.0 build and §13 if present — find "libs.versions.toml", "material3", "Hilt", "Robolectric"), `PROTOCOL.md`
(§0–§2 only — this session changes no protocol behaviour; say so in the RESULT), `DECISIONS.md` (every ADR — in particular ADR-001, ADR-004, ADR-028 Hilt,
ADR-048, ADR-050, ADR-051), `TODO.md`. Then, per task:

- `android/gradle/libs.versions.toml`, `android/gradle/wrapper/gradle-wrapper.properties`, `android/settings.gradle.kts`, `android/build.gradle.kts`,
  every module's `build.gradle.kts` (`app`, `data`, `domain`, `hardware`, `ui`), `android/gradle.properties`, any `proguard-rules.pro` — **in full**.
- `.github/workflows/android.yml`, `.github/workflows/lint-docs.yml`, `.github/workflows/update-docs-sidebar.yml`, `.github/workflows/update-sitemap.yml` — in full (every `uses:` is pinned to a commit SHA with a
  version comment; keep that practice).
- `scripts/release.sh` and `scripts/third_party_notices.py` (they consume the build's output and the dependency list), `RELEASING.md` §1–§8,
  `README.md` "Building from source" (it names JDK 21 and `android-34`/build-tools `34.0.0`), `WORKSTATION_PREPARATIONS.md` (the JDK section),
  `CONTRIBUTING.md`, `THIRD_PARTY_NOTICES` handling in `ARCHITECTURE.md`.
- The UI files with the opt-ins: `android/ui/src/main/kotlin/io/github/tedsluis/opencontrolpixelbuds/ui/OpenControlNavHost.kt` (`@OptIn(ExperimentalMaterial3Api::class)`
  for `TopAppBar`, line ≈ 241), `PullRefresh.kt` (`PullToRefreshBox`, line ≈ 96), `SettingsMenu.kt` (`TabRow` vs `PrimaryTabRow`, line ≈ 94) — and their tests.
- The earlier dependency sessions for the method: `ai-sessions/0057` (the Material 3 overhaul — why `TopAppBar`/`PullToRefreshBox` were opted in, the checkpoint
  answer), `ai-sessions/0065` (third-party notices, `THIRD_PARTY_NOTICES.txt`), `ai-sessions/0074` RESULT §A.0/§H (the gate and mutation method),
  `ai-sessions/0076` RESULT (the gate table and `mutate.py` approach).

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5).** Create **ai-sessions/0078_MAINTENANCE_RESULT_2026_10_07.md** at the very
start with `**Status:** partial — resumed` and a **Progress** block; update it at the end of every phase and after every substantial step (each inventory
table, each upgrade step and its gate run, each mutation, each checkpoint answer): what is done, what is next, which files are touched but unverified,
where intermediate results live (the session scratchpad: baseline and after-upgrade APKs, dependency trees, gate logs, JUnit counts). A resumed session
reads this prompt, then the Progress block, re-reads the files the unfinished step touched, and continues; it never redoes a recorded step and never
assumes an unrecorded step was done.

**Memory and the machine (OOM kills in `ai-sessions/0057` and `0074`).** The laptop has 15 GB. Gradle with `--max-workers=2`; **never two Gradle builds at
once**; mutation runs one at a time; after a crash check `git diff -- android` for a leftover mutation and `pgrep -f GradleDaemon` before anything else;
`./gradlew --stop` between the baseline and the upgraded toolchain. Never `git worktree add` a commit (it checks out `captures/`, gigabytes of LFS). Never
delete with a wildcard (`rm -f *`); temporary output goes into a fresh `mktemp -d` directory. Keep the baseline artefacts (APK, trees, counts) in the
scratchpad before upgrading — they are the "before" of every comparison.

**Network.** The upgrade downloads build tooling and libraries (Gradle distribution, Maven/Google Maven artefacts) — that is the developer machine, not the
app; `AGENTS.md` §1 concerns the app (no `INTERNET` permission, no telemetry SDK). The baseline gate runs `--offline` (the cache is present); the upgraded
build needs one online resolution, then `--offline` again. Versions are chosen from the **official** sources at run time — Gradle releases
(`services.gradle.org/versions/current`, the release notes), Google's Maven (`dl.google.com/dl/android/maven2/<group path>/group-index.xml` /
`maven-metadata.xml`), Maven Central (`repo1.maven.org/maven2/<group path>/maven-metadata.xml`), the AGP ↔ Gradle ↔ JDK compatibility table and the Kotlin ↔ KSP
↔ Compose-compiler compatibility notes (developer.android.com / kotlinlang.org / github.com/google/ksp releases), the Compose BOM mapping page. **Cite each
source with its URL and the exact line used** (memory/earlier sessions: developer.android.com pages may return only navigation to the fetch tool — read
the raw text, e.g. with `curl`, and check any summary against it). Never invent a version number; never use a `+`, `latest.release` or a pre-release
(alpha, beta, rc, dev, M) unless the maintainer approves it at the checkpoint.

**Nothing is taken on trust**, including this prompt: every current version, every compatibility claim and every count is re-derived.

---

## 1. What the maintainer asked for (chat, 2026-10-07, translated from Dutch)

> Make a prompt for the upgrade (the points below, from `TODO.md` §5), following the project's rules, in English, executable in a new session. Check which
> other things need an update and see whether they can be taken along at once.
> - Gradle, AGP, Kotlin, the Compose BOM and Hilt, each version pinned explicitly in `libs.versions.toml`.
> - On that bump, look again at the `@ExperimentalMaterial3Api` opt-ins (`TopAppBar`, `PullToRefreshBox`) and at `TabRow` vs `PrimaryTabRow` in
>   `SettingsMenu.kt`.
> - Add `distributionSha256Sum` to the Gradle wrapper.
> - Per `AGENTS.md` §10, justify each dependency, and check with `./gradlew app:dependencies` that nothing with network, analytics or ads comes in.
> - Prove that nothing breaks: the full gate before and after, the same test counts, mutations, and a comparison of the APK contents.

---

## 2. The state at the time of writing (re-check every value in Phase A)

| Item | Now | Where |
|---|---|---|
| Gradle (wrapper) | 8.9, no `distributionSha256Sum` | `android/gradle/wrapper/gradle-wrapper.properties` |
| AGP | 8.6.0 | `libs.versions.toml` `agp` |
| Kotlin (+ Compose compiler plugin) | 2.0.20 | `kotlin` (also `kotlin-compose` plugin) |
| KSP | 2.0.20-1.0.25 | `ksp` (tied to Kotlin) |
| Hilt / Dagger | 2.52 | `hilt` (ADR-028) |
| Compose BOM | 2024.09.00 (resolves `material3` 1.3.0 — the opt-ins' comments say so) | `composeBom` |
| kotlinx.coroutines | 1.9.0 | `coroutines` |
| AndroidX lifecycle / activity-compose / core-ktx / navigation-compose / datastore-preferences | 2.8.6 / 1.9.2 / 1.13.1 / 2.8.0 / 1.1.1 | catalog |
| Tests: JUnit 5 / kotest / JUnit 4 / Robolectric / javax.inject | 5.11.0 / 5.9.1 / 4.13.2 / 4.13 / 1 | catalog |
| SDK levels | `compileSdk` 34, `minSdk` 34, `targetSdk` 34 (app) | each module's `build.gradle.kts` |
| JDK / toolchain | Java 21 (`jvmToolchain(21)`, `VERSION_21`); CI Temurin 21 | build files, `.github/workflows/android.yml` |
| CI actions | `.github/workflows/android.yml`: `actions/checkout` v7.0.1, `setup-java` v6.0.1, `gradle/actions/setup-gradle` v6.3.0; `.github/workflows/lint-docs.yml` and the two `update-*` workflows: `checkout` v5.1.0, `setup-python` v6.3.0 — **two different checkout versions** | `.github/workflows/` |
| Build warning | "Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0" (every run) | gate logs |
| Release minification | none configured (`isMinifyEnabled` absent) | `app/build.gradle.kts` |
| Docs that name versions | `README.md` "Building from source" (JDK 21, `android-34`, build-tools `34.0.0`); `WORKSTATION_PREPARATIONS.md` (JDK); `ARCHITECTURE.md` (catalog, material3 1.3.0 in comments) | — |

**Other things to check for an update, and take along only if they fit §3's rules:** the KSP plugin (it follows Kotlin), `compileSdk` (newer AndroidX
releases may require 35 or 36 — that is compile-time only), the Android build-tools the README names, Robolectric's supported SDK levels (it must support
the `compileSdk`/`targetSdk` the tests use), the CI actions (align `checkout` across the four workflows, newest pinned SHAs of each action, still with a
version comment), the Gradle 9 deprecations behind the warning above, `foojay` toolchain resolution if Gradle needs it, and whether
`scripts/third_party_notices.py` still runs against the new dependency tree. **`targetSdk` is different:** raising it changes the app's runtime behaviour
(Android 15 enforces edge-to-edge, Android 16 more) — it is **not** part of this upgrade unless the maintainer chooses it at the checkpoint; prepare the
question with the documented behaviour changes (developer.android.com "Behavior changes: apps targeting Android 15/16", quoted) and what it would mean
for each screen.

---

## 3. Rules (binding, in addition to `AGENTS.md`)

- **No behaviour change.** The app's bytes on the wire, its UI texts, its permissions and its features stay as they are. Code changes are allowed only
  where an upgrade forces them (a removed or renamed API, a new required parameter, a deprecation that became an error) or where the maintainer
  approves a listed improvement (the opt-ins, `PrimaryTabRow`); each such change gets its reason, its file:line and its test.
- **`AGENTS.md` §1 / ADR-004.** No new dependency that touches `com.google.android.gms.*`, Firebase, Crashlytics, any analytics, telemetry, crash reporting or ads
  SDK, and no `INTERNET` permission — check the **resolved runtime classpaths** (not only the catalog) before and after (§4 task 9). A new transitive
  library is listed and justified like a direct one.
- **`AGENTS.md` §10.** Every direct dependency: what it is for, why no lighter/native alternative, confirmation of no network/analytics/ads (transitively), its
  pinned version and the official source of that version. No dynamic versions. The catalog stays the single place of versions.
- **Pre-releases** (alpha, beta, rc, M, dev, snapshot): not used, unless the maintainer approves a specific one at the checkpoint with its reason (e.g. a stable
  AndroidX that requires it does not exist).
- **One step at a time.** Upgrade in the order of §4 Phase B, each step followed by its own gate run; a failing step is fixed or rolled back before the next.
  Record every step, its command and its result. A step that cannot be completed is reported with the error, not hidden.
- **The `AGENTS.md` §6 gate.** No ADR or ADR Update without the maintainer's approval in this chat. If a change touches an ADR's content (e.g. ADR-028 names
  Hilt), draft a dated Update for the checkpoint.
- **Scope.** Exactly the build, its dependencies, the CI workflows' action versions and the documents that name versions. No feature, no `versionName`/
  `versionCode` change, no `scripts/release.sh` run, no tag, no release. `scripts/` are edited only if the new toolchain forces it (e.g. a renamed Gradle
  task in `third_party_notices.py`) — then it is a checkpoint question with the diff.
- **Evidence.** Every claim with its command, exit status and output; a negative with a positive control (`AGENTS.md` §13 step 8); external facts with URL and
  quoted line.
- **Files.** Look before overwriting; never delete with a wildcard; no `git worktree add`.
- **Commits.** Only after the maintainer confirms the final summary; never force-push.

---

## 4. Tasks

### Phase 0 — set-up and baseline (no change)
1. Create the RESULT (§0). Record `git log -1`, `git branch --show-current`, `git status --short`, `git fetch && git log --oneline HEAD..origin/main`;
   `java -version`, `./gradlew --version`, the Android SDK platforms and build-tools installed (`ls $ANDROID_HOME/platforms $ANDROID_HOME/build-tools`). Branch
   `maintenance/0078-toolchain-upgrade` from an up-to-date `origin/main`.
2. **Baseline gate:** `cd android && ./gradlew --offline --max-workers=2 clean assembleDebug testDebugUnitTest test lint` → exit code, per-module test counts
   from the JUnit XML (`*/build/test-results/*/*.xml`: tests, failures, errors, skipped per task), lint issues per module (`*/build/reports/lint-results-debug.xml`),
   `w:` lines, and the deprecation warning text (`--warning-mode all` once, into the scratchpad).
3. **Baseline artefacts** (scratchpad): the debug APK; `aapt2 dump badging` and `aapt2 dump xmltree --file AndroidManifest.xml` of it; `unzip -l` (file list
   and sizes); the dex classes (`dexdump` or `apkanalyzer dex packages`, whichever the SDK has — say which); the resolved trees
   `./gradlew --offline :app:dependencies --configuration releaseRuntimeClasspath` and `debugRuntimeClasspath`, and the same for `:data`, `:hardware`, `:ui`,
   `:domain` (`runtimeClasspath`); `./gradlew :app:processReleaseMainManifest` → the merged release manifest.
4. **Baseline mutations:** run the mutation set of task 13 on the baseline once, so "killed before / killed after" can be compared.

### Phase A — inventory and plan (no change)
5. For every entry of §2 and every catalog version: the current stable release from the official source (URL, quoted line, release date), the
   compatibility constraints (AGP ↔ Gradle ↔ JDK; Kotlin ↔ KSP; Kotlin ↔ Compose compiler plugin; Hilt ↔ KSP/AGP; AndroidX ↔ `compileSdk`; Robolectric ↔ SDK),
   breaking changes and migration notes that touch this code base (quote them). One table: item · now · proposed · source · constraint · risk · code impact.
6. The opt-ins and `PrimaryTabRow`: in the `material3` version the proposed BOM resolves, is `TopAppBar` still `@ExperimentalMaterial3Api`? `PullToRefreshBox`?
   Is `PrimaryTabRow` stable, and is `TabRow` deprecated? Evidence: the library's source or its public API file (the public API listing — current dot txt — under compose/material3/material3/api of that release on
   `android.googlesource.com` / `androidx` GitHub mirror, read raw) — quote the annotations. Plan the change only where the answer allows it.
7. The CI actions: for each `uses:` the newest release and its commit SHA (`gh api repos/<owner>/<repo>/releases/latest`, `gh api repos/<owner>/<repo>/git/ref/tags/<tag>`),
   breaking notes; plan to align `actions/checkout` across the four workflows.
8. The documents to update afterwards (README, WORKSTATION_PREPARATIONS, ARCHITECTURE comments/sections, RELEASING if a tool changes, the opt-in comments).

### Phase B — the checkpoint before changing anything (stop and ask, in this chat, `AskUserQuestion`)
Before the first question, post a short plain-language overview (what would change, why, the risk). Then ask, one decision per question, each option with
pros and cons and one "(Recommended)", the exact versions or text in the preview:
- **(a) The version set** — the proposed table (or tiers: "stable newest compatible" / "minimal bump to remove the Gradle 9 deprecations" / "keep").
- **(b) `compileSdk`** — keep 34 or raise it (only if a proposed library requires it; compile-time only).
- **(c) `targetSdk`** — keep 34 (recommended for this session) or raise it, with the quoted behaviour changes.
- **(d) The opt-ins and `PrimaryTabRow`** — remove the opt-ins that became stable; switch `TabRow` → `PrimaryTabRow` (a visual change: the indicator style — say
  what changes on screen) or keep.
- **(e) The CI actions** — align and update, or leave.
- **(f) Anything else found** (a pre-release that cannot be avoided, a script change, an ADR Update draft, `distributionSha256Sum` source).
Record the answers verbatim.

### Phase C — upgrade, step by step (each step: change → gate → record)
9. In this order, each followed by `./gradlew --max-workers=2 clean assembleDebug testDebugUnitTest test lint` (online once where needed, then `--offline`):
   (1) Gradle wrapper — `./gradlew wrapper --gradle-version <v> --distribution-type bin --gradle-distribution-sha256-sum <sha>` with the SHA-256 from the official
   checksum file (`services.gradle.org/distributions/gradle-<v>-bin.zip.sha256`; quote it), check `gradle-wrapper.properties` has `distributionSha256Sum`, and the
   wrapper JAR's checksum against `services.gradle.org/distributions/gradle-<v>-wrapper.jar.sha256`; (2) AGP (+ `compileSdk` if approved); (3) Kotlin + the
   Compose compiler plugin + KSP; (4) Hilt; (5) the Compose BOM and the opt-in/`PrimaryTabRow` changes approved in (d); (6) AndroidX libraries and coroutines;
   (7) test libraries (JUnit 5, kotest, JUnit 4, Robolectric); (8) the CI workflows. Fix the Gradle 9 deprecations the new versions still report, where the fix
   is in this repository.
10. **Dependency audit (`AGENTS.md` §10):** the after-trees of task 3; `diff` before/after per configuration; every **added** group/artifact named, with what
    pulled it in (`dependencyInsight`); a scan of the resolved release runtime classpath and of the APK for network/analytics/ads/telemetry —
    `grep -i -E "firebase|crashlytics|analytics|play-services|gms|datatransport|okhttp|retrofit|sentry|mixpanel|amplitude|appsflyer|adjust|ads|measurement|ktor-client"`
    over the trees and over the dex class list; positive control: the same `grep` finds a known library line (e.g. `androidx.compose`) — and a planted string
    in a scratch file is found. The justification table: dependency · purpose · why not lighter · network/analytics/ads: none (evidence) · version · source.
11. **No new permission or component:** the merged release manifest and the APK's `badging`/`xmltree` before vs after — permissions identical (no `INTERNET`),
    no new `<service>`, `<receiver>`, `<provider>` or `<activity>` (a library may add e.g. `androidx.startup`/`ProfileInstaller` — list and judge each).
12. **APK contents:** file list diff (added/removed/size-changed entries, grouped), dex class/package diff (added/removed packages), `minSdk`/`targetSdk`/`compileSdk`
    in `badging`, the APK size before/after. Explain every non-trivial difference; any new top-level package that is not AndroidX/Kotlin/Dagger/the app is a
    finding.

### Phase D — proof that nothing breaks
13. **Final gate** (`clean`, then the full command): exit 0; per-module test counts **equal** to the baseline (explain any difference — a test renamed by a
    library change is not a lost test); 0 failures/errors/skipped; lint: no new issue (a new lint check firing is listed and either fixed in a code-neutral way
    or recorded as a question); `w:` lines: none new.
14. **Mutations** (scratch `mutate.py`, one at a time, `--max-workers=2`, restore byte-identically with a SHA-256 check): at least the five of `ai-sessions/0076`
    (field 11 `!on`; field 29 as 0/1; channel 19's address = 21's; field 15 `!on`; balance zigzag of −v) plus two UI ones: the screen-reader description of "—"
    removed (`SettingsUi.kt`, `ai-sessions/0074` M9) and the settings-menu tab order swapped (`SettingsMenu.kt`). Each killed after the upgrade as before; name
    the killing test.
15. **The release build path** (no signing, no `release.sh`): `./gradlew :app:compileReleaseKotlin :app:processReleaseMainManifest lintRelease` exit 0 (the CI's
    release check); `python3 scripts/third_party_notices.py <the debug APK> <scratch output>` (or `--help` if it needs the release APK — say which) still runs and
    lists every library of the new tree with a licence (none "not found").
16. Compliance: `git diff --name-only origin/main` lists only build files, the catalog, the wrapper, the workflows, the approved UI files and documents;
    `grep -rn uses-permission android/*/src/main/AndroidManifest.xml` unchanged; no `transport.send` added or removed; AGPL headers intact on every changed
    Kotlin file.

### Phase E — documents and finish
17. Update what names versions or tools: `README.md` "Building from source", `WORKSTATION_PREPARATIONS.md` (if the JDK changes), `ARCHITECTURE.md` (the catalog
    section; the opt-in notes), `RELEASING.md` (only if a tool or command changed), the code comments that name `material3 1.3.0`, `TODO.md` §5 (the upgrade
    item removed; anything deferred added), `CHANGELOG.md` `[Unreleased]` ("Changed — build: …", user-invisible), `ai-sessions/INDEX.md` (the 0078 row), any
    approved ADR Update. `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and `… scripts/lint_docs.py` → exit 0.
18. Finish the RESULT: plain-language summary first (what was upgraded, what was not and why, what changed in the APK, what the next hardware run must
    look at — the upgrade ships with the next release and is hardware-tested in its run); the version table with sources; the gate, dependency, manifest,
    APK and mutation tables; the checkpoint answers verbatim; "Files read"; **"Deferred documentation"** (each also in `TODO.md`); **"Commits"**. Status per
    `AI_SESSION_LOG_PROCEDURE.md` §4/§4b.
19. Show the maintainer a short summary and **ask whether to commit, push and open a pull request**. Only after a yes: Conventional Commits, one per concern
    (`build: …` per upgrade step or group, `ci: …` for the workflows, `refactor(ui): …` for approved code changes, `docs: …`, `docs(session): …`), each with a
    *why* and the attribution line from the session's system reminder; `git fetch` and rebase before pushing, never force; nothing from `build/`, `dist/`,
    `android/.kotlin/`, `.vscode/` or `__pycache__` staged. CI must be green on the pull request before the maintainer merges.

---

## 5. Guardrails (summary)

- The app's behaviour, bytes on the wire, permissions and texts do not change; the proof is the gate with equal counts, the mutations and the APK diff.
- Every version pinned, from an official source, stable, justified; no network/analytics/ads/GMS in any resolved classpath or in the APK.
- `targetSdk`, pre-releases, script changes, visual changes and ADR texts only with the maintainer's approval in this chat.
- One upgrade step at a time, one Gradle build at a time, `--max-workers=2`; no worktrees; no wildcard deletes.
- No release, no tag, no `versionName` change; commit and push only after the maintainer confirms.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0078_MAINTENANCE_PROMPT_2026_10_07.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0078_MAINTENANCE_PROMPT_2026_10_07
