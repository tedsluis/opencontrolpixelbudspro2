# 0065_MAINTENANCE_RESULT_2026_10_01.md — Prepare the first public APK release (v1.0.0) on GitHub Releases

**Number:** 0065
**Category:** MAINTENANCE
**Date:** 2026-10-01 (run 2026-10-02)
**Title:** Validate and complete the maintainer's plan for the first public release of OpenControl for Pixel Buds Pro 2 — manual distribution outside Google
Play via GitHub Releases — and build what the maintainer approves
**Status:** complete — ADR-051 approved in chat; nothing published; not hardware-verified

---

## Summary (plain language) — what the maintainer does next, in order

Nothing was published and no real key was touched. The project is ready for you to make the first release:

1. **Create the signing key and back it up** — `RELEASING.md` §1 (you type the passwords; note the certificate fingerprint).
2. **Put the four values in `~/.gradle/gradle.properties`** (chmod 600) — `RELEASING.md` §2. Optionally set up tag signing (§3).
3. **Commit and push this session's changes** (I ask below), then **`scripts/release.sh 1.0.0`** — it builds in a clean worktree, verifies and writes
   `dist/1.0.0/` (APK, checksum, notices, release notes), and prints the publishing commands without running them.
4. **Run `CAP-067` on that exact APK, in a GrapheneOS secondary user without sandboxed Google Play** (your answers (a) and (f)). Only then tick
   `PROJECT.md`'s Definition of done 1–3.
5. **Publish:** signed tag, `git push origin v1.0.0`, draft release with the three files, check it, publish — `RELEASING.md` §7. Then date the `[1.0.0]`
   CHANGELOG block and update the README's "No release has been published yet" note.
6. **Once:** turn on private vulnerability reporting and fix the topic typo — `RELEASING.md` §9 (it is off today; `SECURITY.md` relies on it).

What changed in the app: the name is "OpenControl for Pixel Buds Pro 2" (launcher: "OpenControl"), the Info tab shows the "Not affiliated with Google" line,
the version is 1.0.0, and a release build is always signed with your key or not built at all.

## Progress

- **Phase 0: done.** Run in the same Claude Code session as `ai-sessions/0064` and `0066`, which read `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`,
  `ARCHITECTURE.md`, `PROTOCOL.md`, `DECISIONS.md` (ADR-001 … ADR-050) and `TODO.md` in full (0064 RESULT §O); the parts changed since (ADR-050 and its Update,
  the `TODO.md`/`ARCHITECTURE.md` edits of 0064/0066) were written in this session.
- Repo at start: `HEAD` = `275dc1d`; `git status --short` = `?? android/.kotlin/` and the nine untracked `images/opencontrol-for-buds-*` files; `HEAD..origin/main`
  empty; last commit under `android/` = `a0a95d3 Fri Oct 2 06:11:00 2026 +0200`.
- **Phases A–F: done** (§A–§F). Gate green after the changes (§D). Throwaway key deleted (§C.1).
- Scratchpad: `/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/5b9789ba-b996-4096-9dfc-a175ee79e0fe/scratchpad` (subfolder `r065/`: `b_gate.log`,
  `b_rel.log`, `vtile.png`).

## 0. Baseline (Phase 0)

`./gradlew --offline clean`, then `./gradlew --offline assembleDebug testDebugUnitTest test lint` → **BUILD SUCCESSFUL in 2 m 26 s, exit 0**. JUnit XML:
`:data` 1588, `:domain` 30, `:hardware` 56, `:ui` 35 (debug; release unit tests equal), 0 failures. Lint: 0 `<issue>` in `:app`/`:ui`/`:data`/`:hardware`.
Compiler warnings (`^w:` in the log): 0.

`./gradlew --offline assembleRelease` → **BUILD SUCCESSFUL in 36 s, exit 0**: `android/app/build/outputs/apk/release/app-release-unsigned.apk`, 21 249 268 bytes —
**unsigned**, because `android/app/build.gradle.kts` has no `signingConfigs` block; no `isMinifyEnabled` (grep for `minify|shrink` matched nothing ⇒ AGP default
off).

## Files read

In full: `README.md`, `SECURITY.md`, `CONTRIBUTING.md`, `MAINTAINING_DOCS_SITE.md`, `PROJECT.md`, `android/gradle.properties`, `.gitignore`, `android/.gitignore`,
this prompt, `ai-sessions/0066` RESULT/PROMPT, `ai-sessions/0064` RESULT (earlier in this session), every image in `images/` and 8 frames of the video. In part:
`CHANGELOG.md` (head and `[Unreleased]` structure), `index.html` (Docsify config lines), `generate_sidebar.sh` (the heredoc), `DECISIONS.md` ADR-004 and ADR-050,
`LICENSE` (head), the header/GMS/firmware lines of the FINDINGS of `CAP-035`, `CAP-059` … `CAP-066` (grep, output in §A.4), `android/app/build.gradle.kts`
(version/git/buildConfig lines), `SettingsMenu.kt` (the Info texts).

## A. Validation and design (Phase A)

### A.1 Official sources (fetched 2026-10-02)

| Claim | Source | Quoted text |
|---|---|---|
| Updates need the same key | developer.android.com/studio/publish/app-signing | *"When the system is installing an update to an app, it compares the certificate(s) in the new version with those in the existing version. The system allows the update if the certificates match. If you sign the new version with a different certificate, you must assign a different package name to the app"* |
| Losing the key | same | *"If you lose your app's signing key, you lose the ability to update your app."* · *"You cannot regenerate a previously generated key."* |
| Debug key per machine | same | *"the IDE automatically creates the debug keystore and certificate in `$HOME/.android/debug.keystore`"* |
| Validity | same | *"Your key should be valid for at least 25 years"* |
| Secrets out of build files | same | *"If you are working with a team or open-sourcing your code, you should move this sensitive information out of the build files"* (example: a `keystoreProperties` file read in `signingConfigs`) |
| Verify a signature | developer.android.com/tools/apksigner | `apksigner verify [options] app-name.apk`; *"`--print-certs` Show information about the APK's signing certificates."*; key rotation via `apksigner rotate … --old-signer … --new-signer` |
| versionCode | developer.android.com/studio/publish/versioning | *"The Android system uses the `versionCode` value to protect against downgrades by preventing users from installing an APK with a lower `versionCode` than the version currently installed"*; *"make sure that each successive release of your app uses a greater value"*; *"The `versionName` is the only value displayed to users."* |
| Sideloading | developer.android.com/distribute/marketing-tools/alternative-distribution | *"host the release-ready APK files on your website … To install an app distributed in this way, users must opt-in for installing unknown apps"*; *"On devices running Android 8.0 (API level 26) and higher, users must navigate to the Install unknown apps system settings screen to enable app installations from a particular location"* |
| Release assets | docs.github.com …/about-releases | *"Each file included in a release must be under 2 GiB."* · *"There is no limit on the total size of a release, nor bandwidth usage."* |
| `gh release create` | cli.github.com/manual/gh_release_create and local `gh 2.97.0 --help` | `--prerelease` *"Mark the release as a prerelease"*; `--verify-tag` *"Abort in case the git tag doesn't already exist in the remote repository"*; `--draft`; `--notes-file`; `--latest=false`; asset label `'file#label'` |
| Video in Markdown | docs.github.com …/attaching-files | *"10MB for videos uploaded to a repository owned by a user or organization on a free GitHub plan"*; *"we recommend using H.264 for greatest compatibility"*; *"For public repositories, uploaded files can be accessed without authentication."* |
| `--attach` | docs.github.com/en/github-cli/github-cli/attaching-files-with-github-cli | *"GitHub CLI uploads the file to GitHub and writes the resulting URL into the body of your issue, pull request, or comment"* — `gh issue/pr create/edit/comment` |
| Token least privilege | docs.github.com …/automatic-token-authentication | *"As a good security practice, you should grant the `GITHUB_TOKEN` the least required access."* |
| Apache-2.0 binaries | apache.org/licenses/LICENSE-2.0.txt §4 | *"(a) You must give any other recipients of the Work or Derivative Works a copy of this License"*; *"(d) If the Work includes a "NOTICE" text file as part of its distribution, then any Derivative Works that You distribute must include a readable copy of the attribution notices …"* |

**Corrections to the prompt / plan found here:**

- **`gh … --attach` is not in the installed GitHub CLI.** `gh issue comment --help`, `gh issue create --help`, `gh issue edit --help`, `gh pr comment --help` on
  `gh version 2.97.0 (2026-07-31)`: `grep -i -E "attach|upload|file"` matched only `--body-file` (exit 0 — the positive control), never `--attach`. The docs page
  describes it, so it needs a newer `gh`; option (b) of the video question therefore needs a `gh` upgrade first (or the web editor's drag-and-drop).
- **Deleting the comment:** no official GitHub page found that says what happens to a `user-attachments` file when its comment is deleted (one docs page and a
  web search, 2026-10-02). 🔴 OPEN — treat an upload as public and permanent.
- **No source for "AppVerifier"** on grapheneos.org/usage (fetched: only OS-update sideloading is mentioned); the GitHub API returned 404 for
  `GrapheneOS/AppVerifier`. The release notes will therefore name only `apksigner verify --print-certs` and the SHA-256 checksum, not a specific app.
- The GitHub REST page did not give the release permission directly (the fetch returned a "Workflows" note only); `contents: write` for creating a release is
  ⚪ ASSUMPTION until a CI job is approved and tested.

### A.2 The app as it is (release variant)

- `aapt2 dump badging app-release-unsigned.apk`: `package … versionCode='1' versionName='0.1.0-dev'`, `sdkVersion:'34'`, permissions `BLUETOOTH_CONNECT`,
  `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_CONNECTED_DEVICE` and AndroidX's `…DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`; label
  `'OpenControl for Pixel Buds'`.
- `aapt2 dump xmltree --file AndroidManifest.xml … | grep -i -E "debuggable|INTERNET"` → **exit 1, no match** (positive control: `BLUETOOTH_CONNECT` printed by
  `badging` above). No `debuggable` ⇒ false in the release variant.
- StrictMode only when `BuildConfig.DEBUG` (`OpenControlApplication.kt:63`); the Info tab shows `BuildConfig.VERSION_NAME`, `GIT_COMMIT`, `GIT_COMMIT_DATE`
  (`MainActivity.kt:536`); `GIT_COMMIT` gets `-dirty` when `git status --porcelain --untracked-files=no` is non-empty (`app/build.gradle.kts:24-25`).
- Third-party notices: the APK holds one `META-INF/NOTICE.md` (Jakarta Dependency Injection, Apache-2.0, via Hilt); no licence list of the bundled libraries is
  shown in the app or shipped beside the APK. Apache-2.0 §4(a)/(d) ask for a copy of the licence and the NOTICE attribution — proposal I-12 below.

### A.3 Media (privacy and currency)

- The eight JPGs: app UI only, no status bar, no notification, no account, device name or address; EXIF 3 tags, no GPS (0066). The video: the status bar area is
  blank grey; the Debug tab frame shows "Unidentified frames" lines (DLCI/Group/Code/length — no address); container tags `creation_time 2026-10-01T18:20:39Z`
  and `com.android.version=17` (stripped by `-map_metadata -1` in the planned script).
- **Out of date:** `…_212805.jpg` (Balance and audio) **and the video** show the balance slider with `‹`/`›` — the `0064` build, reverted in `0066`
  (`EqScreen.kt` restored from `d5fdf10`). They do not show the app being released. Options at the checkpoint: re-take that screenshot and the recording on the
  release build, or leave them out.
- Also stale in `README.md`: "the newest planned app capture (`CAP-065-EVENT-NOTES.md`)" (it is `CAP-067`); "Decisions: 49 ADRs" (50).

### A.4 Definition of done — evidence table

`PROJECT.md`: *"Without Google Play Services installed, the app can: …"*

| Criterion | Evidence | Without Play services? |
|---|---|---|
| 1 Connect (RFCOMM) | `CAP-059` … `CAP-066`: pairing, Connect, re-open (e.g. `CAP-064-FINDINGS.md` row G) | ❌ not shown — every run header: "Google Play services present" (`CAP-060`:15, `CAP-061`:15, `CAP-062`:15, `CAP-063`:15, `CAP-064`:13, `CAP-065`:13, `CAP-066`:13); `CAP-059` §4: Play services contended for DLCI 0x04 |
| 2 Battery + ANC | `CAP-062` R1 (writes sent), `CAP-063`:253 (ANC `Set` ACKed 3/3), battery in every run | ❌ not shown (same runs) |
| 3 Stable over connect/disconnect cycles | `CAP-063`/`CAP-064` several sessions, re-opens, losses explained | ❌ not shown (same runs) |
| 4 Documented and reproducible | `PROTOCOL.md` FACTs with sign-off, FINDINGS with frame numbers | ✅ (not GMS-dependent) |

`CAP-035` had Google Play services **disabled**, not absent, and predates the app (no OpenControl build). **No criterion 1–3 may be ticked as written.** Options:
(i) a run in a GrapheneOS secondary user without sandboxed Play (route in `0066` RESULT §C.1; evidence = no `03 08 00 02 01 25` claim in the HCI log) — the
cleanest, and it can be the same run as `CAP-067` on the signed APK; (ii) reword the criterion (a `PROJECT.md` change the maintainer decides, ideally with an
ADR); (iii) release as a pre-release and leave the DoD open.

### A.5 The maintainer's plan, point by point

| Point | Verdict | Why |
|---|---|---|
| GitHub Releases as the channel | **Keep** | ADR-004 (*"updates are distributed manually (e.g. via GitHub Releases)"*); no store; assets up to 2 GiB, no bandwidth limit. |
| Own release keystore | **Keep, sharpen** | Same-certificate rule (A.1). The key lives **outside** the repo with an encrypted offline backup; one key for the app's whole life (25+ years). |
| Signing values via env vars or `local.properties` | **Correct** | `local.properties` is the SDK-path file AGP writes; use `~/.gradle/gradle.properties` (outside the repo, read by Gradle as project properties) or environment variables; the build **fails** with a clear message when missing — never falls back to the debug key. |
| Tag, release, `app-release.apk`, notes | **Keep, add** | Tag the commit that is built (worktree build, no `-dirty`); name the asset `opencontrol-pixelbudspro2-<version>.apk`; add SHA-256 and the certificate's SHA-256 to the notes; create as **draft** first, check, then publish. |
| Automate with a script / Claude Code | **Keep (local)** | `scripts/release.sh` builds, verifies and prints the `gh` commands; publishing stays the maintainer's typed act. |
| CI on `v*` tag | **Replace for now** | Signing on GitHub means the key leaves the maintainer's machine (a repository secret). Lighter: CI builds the tag unsigned and posts its SHA-256 for comparison, or only verifies the tag builds. Later decision. |
| "Download & Install" below the intro | **Keep** | Plus the sideloading sentence ("Install unknown apps" for your browser/file manager) and how to check the checksum. |
| Feature list does / does not | **Keep, correct** | Does: ANC/Adaptive/Transparency, EQ + presets, battery L/R + Case, Find Left/Right, touch/press-and-hold, balance/mono/conversation detection, in-ear detection setting, ANC tile, dark mode. Does not: ring the Case or both, firmware updates, multipoint management, head gestures, any account/cloud feature. |
| Own screenshots + video next to the official-app link | **Keep, with care** | Two items are out of date (A.3); keep the README light (a few screenshots, the rest in a gallery page). |
| Less prominent disclaimer stressing Safe Mode | **Keep, correct** | Shorter, not hidden: the risk sentence stays; the Safe Mode text must say what is shown — Safe Mode was **hardware-verified** in `CAP-062`/`CAP-063` (README still says "not yet exercised on hardware"). |
| Intro: offline, no network permission, no location | **Keep** | True: no `INTERNET` (A.2), no location permission, no `BLUETOOTH_SCAN`. "Made for privacy-focused OSs" → "made and tested on GrapheneOS". |
| Tick the Definition of done | **Do not, as written** | A.4. |
| CHANGELOG `[1.0.0]` | **Keep, shape** | A short user-facing block (features, requirements, known limits) above `[Unreleased]`; the research history stays below as "History before 1.0.0". |
| Release the `0064`/`0066` build as 1.0.0 | **Not yet** | Not hardware-tested (`CAP-067` planned). Run `CAP-067` on the **signed** APK (it also proves the signing/update path), ideally in a user without Play services; or publish `v1.0.0-rc.1` as a pre-release. |

### A.6 Draft runbook (the maintainer runs every step; nothing is run by the session)

1. **Key, once:** `mkdir -p ~/keys && chmod 700 ~/keys` ·
   `keytool -genkeypair -v -keystore ~/keys/opencontrol-release.jks -storetype PKCS12 -keyalg RSA -keysize 4096 -validity 10000 -alias opencontrol` (keytool
   prompts for the passwords and the name — answer with a name you are happy to be public; it appears in the certificate) · **backup:** copy the `.jks` to
   two offline media, encrypted (e.g. `gpg -c`), with the passwords in a password manager.
2. **Values, once:** in `~/.gradle/gradle.properties` (`chmod 600`): `OPENCONTROL_STORE_FILE=/home/<you>/keys/opencontrol-release.jks`,
   `OPENCONTROL_KEY_ALIAS=opencontrol`, `OPENCONTROL_STORE_PASSWORD=…`, `OPENCONTROL_KEY_PASSWORD=…` (typed in an editor, never on a command line).
3. **Release commit:** version bumped, CHANGELOG/README done, committed and pushed; `git status` clean.
4. **Build clean:** `git worktree add ../ocp-release <commit>` · `cd ../ocp-release/android && ./gradlew clean assembleRelease` (or `scripts/release.sh`).
5. **Verify:** `apksigner verify --verbose --print-certs app/build/outputs/apk/release/app-release.apk` (v2/v3 "true", the certificate SHA-256 equals the one
   noted at step 1) · `aapt2 dump badging …` (versionName, no `INTERNET`) · install on the phone, Info tab: version, hash **without** `-dirty`.
6. **Checksum:** `cp … opencontrol-pixelbudspro2-<v>.apk` · `sha256sum opencontrol-pixelbudspro2-<v>.apk > ….sha256`.
7. **Hardware run** on this exact APK (`CAP-067`, in a user without Play services if approved).
8. **Tag:** `git tag -a v<v> <commit> -m "OpenControl for Pixel Buds Pro 2 <v>"` (or `-s` signed) · `git push origin v<v>` ⇐ outward, needs a yes.
9. **Release (draft first):** `gh release create v<v> --verify-tag --draft [--prerelease] --title "OpenControl for Pixel Buds Pro 2 <v>" --notes-file notes.md
   opencontrol-pixelbudspro2-<v>.apk opencontrol-pixelbudspro2-<v>.apk.sha256` ⇐ outward · check the draft on github.com · `gh release edit v<v> --draft=false`.
10. **Check:** download from the release page on the phone, compare SHA-256, install over the previous version (update keeps data) · `git worktree remove ../ocp-release`.

### A.7 Draft release notes

> **OpenControl for Pixel Buds Pro 2 — <v>** (first public release)
>
> An independent, open-source Android app to control Google Pixel Buds Pro 2 — fully offline, no Google Play services needed, no `INTERNET` permission, no
> location permission, no account.
>
> **Features:** noise control (Noise cancellation, Adaptive, Transparency, Off) and a Quick Settings tile · equalizer with presets · battery Left/Right (with
> charging) and the Case · Find My Buds (ring Left / Right) · touch controls, press-and-hold per bud and its noise-control modes · balance, mono audio,
> conversation detection, in-ear detection · dark mode.
>
> **Not included:** ringing the Case or both buds, firmware updates, multipoint management, head gestures, anything needing a Google account.
>
> **Requirements:** Android 14 (API 34) or newer. Tested on a Pixel 9a with GrapheneOS (Android 17) and Buds firmware `release_5.203` only. On any other firmware
> the app opens in read-only **Safe Mode** and sends no setting changes.
>
> **Install:** download the APK below, allow "Install unknown apps" for your browser or file manager, open the file. Updates: install a newer APK from this page
> over the old one (same signing key, your settings stay).
>
> **Verify:** SHA-256 of the APK: `<sha256>` · signing certificate SHA-256: `<fingerprint>` (`apksigner verify --print-certs`).
>
> **Risk:** this app sends reverse-engineered commands; use at your own risk. Works with Google Pixel Buds Pro 2. Not affiliated with or endorsed by Google.
> Pixel Buds is a trademark of Google LLC. Licence: AGPL-3.0-or-later.

### A.8 README mock-up (order of sections)

1. `# OpenControl for Pixel Buds Pro 2` + two-sentence intro (what it is; offline, no `INTERNET`, no location, no Play services; made and tested on GrapheneOS)
   + the decided notice line ("Works with Google Pixel Buds Pro 2. Not affiliated with or endorsed by Google. Pixel Buds is a trademark of Google LLC.").
2. `## Download & install` — the Releases link, Android 14+, the "Install unknown apps" sentence, checksum/certificate check, updates are manual (no update
   checker in the app; watch the releases, optionally with a tool such as Obtainium).
3. `## What it does` / `## What it does not do` (lists of A.5).
4. `## Screenshots` — 3–4 current screenshots side by side (width-limited `<img>`), the video per the approved option, a link to the official app's screenshots
   (`SCREENSHOTS_PIXEL_BUDS_APP.md`) for comparison.
5. `## Privacy` — no network, no analytics, no location, data stays on the phone, backups excluded; links open only on a tap, in your browser.
6. `## Safety and Safe Mode` — the shortened disclaimer (risk, verified firmware, Safe Mode hardware-verified in `CAP-062`/`CAP-063`, recovery link).
7. `## Status` — a short status (v-number, tested phone/firmware); the long dated history moves to `CHANGELOG.md`.
8. Then, for contributors, unchanged in substance: Why · Project goal · Current state · Building from source (debug + release) · Approach · Core principles ·
   Project documentation · Target platform · Attribution · License (corrected to "AGPL-3.0-or-later").

### A.9 Further ideas (proposals; none built)

| ID | Idea | Why | Cost | Risk / guardrail |
|---|---|---|---|---|
| I-1 | Pre-release `v1.0.0-rc.1` first | ship to testers before the hardware run is done | low | none |
| I-2 | SHA-256 + certificate fingerprint in every release | users can check the download | low | none |
| I-3 | Signed tags (`git tag -s`) | provenance | low (a GPG/SSH signing key) | none |
| I-4 | `RELEASING.md` runbook document (root, in the sidebar) | the steps survive this session | low | none |
| I-5 | `scripts/release.sh` (clean-tree, tag = versionName, build in worktree, `apksigner verify`, no `INTERNET`, checksum, print `gh` commands — never run them) | repeatable, fewer mistakes | medium | none |
| I-6 | `scripts/readme_media.sh` (the ffmpeg video script, already asked) | GitHub-ready MP4/GIF/poster | low | ffmpeg is a dev tool, not an app dependency |
| I-7 | Obtainium hint in the README | update notifications without an in-app checker | low | ADR-004-conform (outside the app) |
| I-8 | IzzyOnDroid / F-Droid later | discoverability | high (reproducible build, metadata; F-Droid signs itself unless reproducible) | later |
| I-9 | Issue templates (phone, OS, Info-tab build, firmware, debug export without the Buds' address) | useful reports, privacy | low | none |
| I-10 | `SECURITY.md`: enable GitHub private vulnerability reporting (a repository setting — outward, the maintainer's act) | the policy already points to it | low | setting change needs a yes |
| I-11 | Repository "About": description, topics, website = docs site | findability | low | the maintainer's act |
| I-12 | Third-party licence notices (a generated list of bundled libraries + licences, shipped as text beside the APK or bundled like `license.txt`, no new dependency) | Apache-2.0 §4(a)/(d) | medium | no dependency; a script reading Gradle's dependency list |
| I-13 | R8/minify | smaller APK | medium | **not now** (0066 §C.5) |
| I-14 | Docs site: README images use relative paths (Docsify `relativePath: true`) — check after the push | site parity | low | none |

## B. Checkpoint (Phase B) — the maintainer's answers, verbatim (chat 2026-10-02, `AskUserQuestion`)

- (a) Readiness: **"CAP-067 on signed APK first (Recommended)"**.
- (b) Version: **"1.0.0 / code from semver (Recommended)"** — preview: `versionName = "1.0.0"`, `versionCode = 10000 // 1.0.0`, `// 1.0.1 -> 10001, 1.1.0 ->
  10100`, `// rc before 1.0.0: "1.0.0-rc.1" -> 9901`.
- (c) Signing: **"~/.gradle/gradle.properties (Recommended)"** — `OPENCONTROL_STORE_FILE`, `OPENCONTROL_KEY_ALIAS`, `OPENCONTROL_STORE_PASSWORD`,
  `OPENCONTROL_KEY_PASSWORD` (chmod 600), environment variables of the same name as fallback; `keytool -genkeypair -v -storetype PKCS12 -keystore
  ~/keys/opencontrol-release.jks -keyalg RSA -keysize 4096 -validity 10000 -alias opencontrol`.
- (d) CI: **"None now (Recommended)"**.
- (e1) README: **"Mock-up as shown (Recommended)"** (A.8).
- (e2) Media: **"All 8 + old video now"** — all eight screenshots and the existing recording, compressed and committed. Because `…_212805.jpg` and the video show
  the `0064` balance control (A.3), the README says so in a caption (honest public text, prompt §5).
- (f) Definition of done: **"Test in a user without Play (Recommended)"** — `CAP-067` on the signed APK in a secondary user without sandboxed Play; now only
  the evidence table, criterion 4 ticked.
- (g) CHANGELOG: **"Short user block + history (Recommended)"**.
- (h) ADR-051 and the `AGENTS.md` §12 / `PROJECT.md` notes: **"Yes, as shown (Recommended)"** (texts shown in chat before the question).
- (i-1) **"I-5 scripts/release.sh, I-4 RELEASING.md runbook, I-2 checksum + fingerprint, I-3 signed tags"**.
- (i-2) **"I-9 issue templates, I-7 Obtainium hint, I-12 third-party notices, I-10/I-11 repo settings commands"**.
- Built anyway (asked in chat 2026-10-02, prompt §1): `scripts/readme_media.sh`.

## C. What was built (Phase C)

1. **Release signing and version** — `android/app/build.gradle.kts`: `signingConfigs.release` from Gradle properties or the environment
   (`OPENCONTROL_STORE_FILE`, `OPENCONTROL_KEY_ALIAS`, `OPENCONTROL_STORE_PASSWORD`, `OPENCONTROL_KEY_PASSWORD`); `packageRelease` fails without them;
   `versionName "1.0.0"`, `versionCode 10000`. `.gitignore`: `*.jks`, `*.keystore`, `*.p12`, `keystore.properties`, `dist/`, and the two generated names.
   **Tested:** without values → exit 1, *"Release signing is not configured: missing OPENCONTROL_STORE_FILE, OPENCONTROL_KEY_ALIAS, OPENCONTROL_STORE_PASSWORD,
   OPENCONTROL_KEY_PASSWORD. …"* (`c_nokey.log`), no APK; with a throwaway key (scratchpad, `CN=Throwaway 0065 test`, RSA 2048, 1 day) → exit 0,
   `app-release.apk`, `apksigner verify --verbose --print-certs`: v2 true, 1 signer, DN `CN=Throwaway 0065 test`, SHA-256 `9404a117…318c` (= `keytool -list -v`).
   `aapt2 dump badging`: `versionCode='10000' versionName='1.0.0'`, `application-label:'OpenControl for Pixel Buds Pro 2'`, launchable activity
   `label='OpenControl'`. **The throwaway key, the scratch clone and the throwaway-signed APK were deleted** (`find / -name throwaway.jks` → nothing).
   Note: AGP signs this minSdk-34 APK with v2 only (v3 false); v3 is needed only for a later key rotation.
2. **ADR-051** (`DECISIONS.md`), `id_registry.csv` row, the dated notes in `AGENTS.md` §12 and `PROJECT.md` non-goals — the texts shown in chat, approved (§B (h)).
   `strings.xml` `app_name` + `launcher_label`; `AndroidManifest.xml` MainActivity `android:label`; the notification title (`BudsForegroundService.kt:61`);
   `TRADEMARK_NOTICE` on the Info tab (`SettingsMenu.kt`) asserted in `SettingsMenuTest` (mutation below).
3. **`scripts/readme_media.sh`** — ffmpeg only; run on the recording: `images/opencontrol-for-buds-demo.mp4` 430 037 bytes (H.264 570×1280, no audio,
   tags `creation_time`/`com.android.version` gone — `ffprobe`), `…-demo-preview.gif` 118 410 bytes (12 s), `…-demo-poster.png` 95 652 bytes (not used in
   the README; left untracked). Usage error → exit 2.
4. **`scripts/third_party_notices.py`** + **`LICENSES/Apache-2.0.txt`** (apache.org, SHA-256 `cfc7749b…3d30`): 118 libraries of `releaseRuntimeClasspath`, all
   Apache-2.0 per their POMs, plus `META-INF/NOTICE.md` from the APK and the full Apache text. Gradle's cache held no POM for 20 of them — `--fetch` downloads
   only those (Google Maven, else Maven Central) to `~/.cache/opencontrol-poms/`; `javax.inject:1`'s POM has no Maven namespace (handled). Without a licence →
   exit 1.
5. **`scripts/release.sh`** + **`scripts/release_notes.template`** — tested end to end in a scratch clone with its own bare "origin" (the real repository
   and GitHub untouched): wrong version → `FAIL: versionName … is not "1.0.1"`; correct → exit 0, `dist/1.0.0/` with APK, `.sha256` (`sha256sum -c` OK),
   notices, notes with both checksums; the dex holds `63f1582` without `-dirty`; no worktree left. **Found and fixed in the test:** `git worktree add`
   ran the LFS smudge for every capture (failed in the clone; in the real repository it would copy every capture log) → `GIT_LFS_SKIP_SMUDGE=1`.
6. **`RELEASING.md`** (runbook, in `generate_sidebar.sh` / `_sidebar.md`), **issue templates** (`.github/ISSUE_TEMPLATE/bug_report.yml`, `config.yml`;
   YAML parsed), **`README.md`** per the mock-up (user part first; all eight screenshots and the GIF → MP4, with a caption that they show the `0064` balance
   steps; stale lines fixed: `CAP-067`, 51 ADRs, 67 captures, Safe Mode on hardware, AGPL-3.0-or-later), **`PROJECT.md`** Definition-of-done evidence table
   (criterion 4 ✅, 1–3 not yet), **`CHANGELOG.md`** (`[Unreleased]` empty, `[1.0.0] - not yet released` for users, the old entries under "History before
   1.0.0", a `0065` entry).

## D. Gate and compliance (Phase D)

`./gradlew --offline clean`, then `./gradlew --offline assembleDebug testDebugUnitTest test lint` → BUILD SUCCESSFUL in 2 m 24 s; `assembleRelease` with the
throwaway key → BUILD SUCCESSFUL in 33 s.

| Module | Tests (debug = release) | Failures |
|---|---|---|
| `:data` | 1588 | 0 |
| `:domain` | 30 | 0 |
| `:hardware` | 56 | 0 |
| `:ui` | 35 (the notice assertion is in an existing test) | 0 |

Lint: 0 issues in `:app`/`:ui`/`:data`/`:hardware`; compiler warnings 0 (gate and release logs). **Mutation:** the `Text(TRADEMARK_NOTICE …)` line removed →
`SettingsMenuTest` "Info shows the licence line and the two links …" FAILED (9 tests, 1 failed); file restored, `sha256sum -c` OK.

Compliance:
- `INTERNET` = 0 in every source manifest and every merged manifest, debug and release (`app`, `hardware`, `ui`, `data`); positive control
  `BLUETOOTH_CONNECT` = 1 in `app`/`hardware` source and merged manifests.
- Manifest diff: only `android:label="@string/launcher_label"` on MainActivity. No permission, no dependency added (`libs.versions.toml` untouched).
- Secrets: `git diff | grep -i -E "throwaway0065|storePassword *=|keyPassword *="` → only the two `getValue("OPENCONTROL_…")` lines (no value);
  `grep -rn throwaway0065` over the tree (without `.git`/build) → exit 1; no `*.jks`/`*.keystore`/`*.p12`/`keystore.properties` in `git status --ignored`.
  The real key and passwords were never created, read or asked for.
- `python3 scripts/ensure_footers.py` → "all footers already up to date", exit 0. `python3 scripts/lint_docs.py` → exit 0, "clean"; the historical bucket
  lists only older session logs and this RESULT's scratchpad names (informational).

## E. The maintainer's runbook (Phase E)

`RELEASING.md` is the final runbook (§1 key + backup, §2 values, §3 signed tags, §4 version/commit, §5 `scripts/release.sh`, §6 `CAP-067` on the signed APK,
§7 tag/draft/publish, §8 after publishing, §9 repository settings, §10 media). None of its steps was run in this session.

## Deferred documentation

All also in `TODO.md` ("Open after `ai-sessions/0065`"):

- The maintainer's release steps (key, values, `scripts/release.sh`, `CAP-067` on the signed APK in a user without Play services, tag, release), then the
  Definition of done 1–3, the `[1.0.0]` date and the README's release note.
- Repository settings: private vulnerability reporting (off on 2026-10-02) and the topic "graphenos".
- Re-take `…_212805.jpg` and the recording on the release build.
- 🔴 `gh … --attach` missing in `gh 2.97.0`; deletion behaviour of `user-attachments` undocumented.
- Third-party notices on the Info tab (later, optional).

## Commits

Committed and pushed after the maintainer's yes in chat (2026-10-02, "commit and push this"):

- `2d0b6d3` — build(app): release signing, version 1.0.0, app name and Info notice (0065).
- `23eb0af` — chore(scripts): release, third-party notices and README media scripts (0065).
- `d306ce7` — chore(images): app screenshots and a compressed screen recording (0065).
- the `docs` commit that carries this file — ADR-051 and notes, README, RELEASING, PROJECT, CHANGELOG, TODO, ARCHITECTURE, sidebar, issue templates,
  `ai-sessions/` files.

Not staged: `android/.kotlin/`, the 15.3 MB original recording, `images/opencontrol-for-buds-demo-poster.png` (unused), build output, the scratchpad.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0065_MAINTENANCE_RESULT_2026_10_01.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0065_MAINTENANCE_RESULT_2026_10_01
