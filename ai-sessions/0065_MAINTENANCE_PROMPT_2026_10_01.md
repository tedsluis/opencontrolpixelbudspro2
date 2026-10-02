# 0065_MAINTENANCE_PROMPT_2026_10_01.md — Prepare the first public APK release (v1.0.0) on GitHub Releases: release signing, version, a release runbook and script, an optional tag-triggered CI release, a user-facing README, the v1 "Definition of done" and the CHANGELOG

**Number:** 0065
**Category:** MAINTENANCE
**Date:** 2026-10-01
**Title:** Validate and complete the maintainer's plan for the first public release of OpenControl for Pixel Buds Pro 2 — manual distribution outside Google
Play via GitHub Releases — and build what the maintainer approves: release signing kept out of git, version and build identity, a release runbook and a
local release script, optionally a tag-triggered CI release, a README written for end users with the app's own screenshots, the v1 "Definition of done"
checked against evidence, and a `[1.0.0]` CHANGELOG block

---

## 0. How to use this prompt

You are an expert Android release engineer, Kotlin/Gradle engineer, technical writer and auditor. This prompt is for a **fresh** Claude Code session in this
repository. Run the phases of §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action, read **in full**, in this order:
`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md`, `DECISIONS.md` (**every** ADR, ADR-001 … ADR-050, with every dated
Update), `TODO.md`. Then, per task, in full unless a section is named:

- `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md`; `ai-sessions/0064_FEATURE_RESULT_2026_10_01.md` (the state of the build to be released).
- `README.md`, `CHANGELOG.md`, `SECURITY.md`, `CONTRIBUTING.md`, `LICENSE` (head), `MAINTAINING_DOCS_SITE.md`, `index.html`, `_sidebar.md`, `generate_sidebar.sh`.
- `APP_TESTPLAN.md`; `captures/CAP-067-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BC/CAP-067-EVENT-NOTES.md` (the planned hardware run of the `0064` build).
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 (Capture Index: which runs had Google Play services, which phone, which build) and the "Phone"/"GMS" lines of the
  FINDINGS of `CAP-035`, `CAP-059` … `CAP-066` (evidence for the Definition of done).
- `DECISIONS.md` ADR-002 (licence), ADR-004 (no GMS, no network; "updates are distributed manually, e.g. via GitHub Releases"), ADR-029 (min API 34),
  ADR-042 (Safe Mode, the `release_5.203` allowlist), ADR-050 (Info links); `AGENTS.md` §1, §2, §10, §12 (no Google trademarks or assets), §15.
- `android/app/build.gradle.kts`, `android/build.gradle.kts`, `android/settings.gradle.kts`, `android/gradle.properties`, `android/gradle/libs.versions.toml`,
  `android/.gitignore`, the root `.gitignore`, `.gitattributes`, `android/app/src/main/AndroidManifest.xml`, `android/app/src/main/res/values/strings.xml`,
  `.github/workflows/android.yml`, `scripts/` (what exists; how scripts are written there).
- The images the maintainer wants in the README — **view every one** before using it: `images/opencontrol-for-buds-IMG_20261001_212401.jpg`, `…_212457.jpg`,
  `…_212622.jpg`, `…_212714.jpg`, `…_212805.jpg`, `…_212903.jpg`, `…_212954.jpg`, `…_214301.jpg` and the video
  `images/opencontrol-for-buds-screenrecording-20261001-202037.mp4` (60.8 s, 15.3 MB, Android 17; extract frames with `ffmpeg` to look at it).

Say in the RESULT which files you read in full and which only in part.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5).** Create **ai-sessions/0065_MAINTENANCE_RESULT_2026_10_01.md** at the very
start with `**Status:** partial — resumed` and a **Progress** block; update it after every phase and every item built (what is done and tested, what is
touched but not tested, the last gate result, where intermediate results live — the scratchpad directory; re-create it if gone). A resumed session reads this
prompt, then the Progress block, re-reads the files the unfinished step touched, and continues; it never redoes a recorded step and never assumes an unrecorded
one was done.

**Nothing is taken on trust, including this prompt and the maintainer's plan below.** Every statement about GitHub, Gradle, Android signing or the Android
installer is checked against the official documentation (quote the sentence, with the URL and the fetch date) or against this repository; every statement
about the app against the code, the captures or a test. Where this prompt or the plan is wrong, say so in the RESULT and correct it.

---

## 1. What the maintainer asked for (chat 2026-10-01, translated from Dutch)

"I am about to publish the first APK version of the OpenControl for Buds app. I need help with: how do I publish my app on GitHub, and what is needed for
that? What must be improved (changed, added) in the README.md? What else should I arrange for this first release? I explicitly choose manual distribution
outside Google Play — is GitHub Releases indeed the right way? Below are ideas and considerations for my first release. Validate their usefulness for my
project, improve them and complete them. Propose further ideas and ask for approval before working them out and carrying them out."

The maintainer's plan, verbatim in substance (validate, correct and complete each point — §2 adds what this prompt's author already found):

1. **Publishing on GitHub.** GitHub is the distribution channel (no app store).
   - **Release keystore:** sign the release APK with an own keystore; with no keystore (or the debug key) users cannot update to a later version (v1.1)
     without data loss. How to store the keystore and its password locally and safely; it must **never** reach the git repository.
   - **Release build:** configure `build.gradle.kts` to build a `release` APK with that keystore, via environment variables or a local `local.properties`.
   - **GitHub Releases:** create a git tag (e.g. `v1.0.0`) locally and push it; create a release on GitHub from that tag; attach `app-release.apk` as a binary
     asset; release notes that say briefly what the app does, include the changes from `CHANGELOG.md`, and state the minimum Android 14 (API 34); how to
     automate all of this with a script and/or Claude Code.
   - **CI/CD:** extend the existing `.github/workflows/android.yml` (later) so that pushing a `v*` tag builds a release APK and attaches it to the release.
2. **README for end users.** A "Download & Install" section right below the introduction pointing to the Releases page, with one sentence on sideloading; a
   simple list of what the app does (ANC, EQ, battery, Find My Buds Left/Right) and does not do (ring the Case, firmware updates, multi-device); the app's own
   screenshots and the screen recording (the files above) next to the existing link to `SCREENSHOTS_PIXEL_BUDS_APP.md` (the official app); a less prominent
   hardware/bricking disclaimer that stresses that the app refuses writes on unknown firmware (Safe Mode, the `release_5.203` allowlist); in the introduction:
   fully offline, no network permission, no location tracking, made for privacy-focused operating systems.
3. **Project administration.** Mark v1 in `PROJECT.md`'s "Definition of done (v1)" as reached ("it is all achieved"); move the app's features from
   `[Unreleased]` in `CHANGELOG.md` into a formal `[1.0.0] - 2026-10-XX` block.

**Added 2026-10-02 (the maintainer, chat, before this prompt was run):** *no release is published yet* — this session prepares the project only; Phase E
gives the runbook and runs none of its publishing steps. Since `ai-sessions/0066` Info links only to the README and the issues (ADR-050 Update) and the balance
is the slider alone again. The maintainer's questions on video in the README, the app name and R8 were answered in chat (`ai-sessions/0066` RESULT) — use those
answers as input and verify them.

**Decided by the maintainer (chat 2026-10-02, `AskUserQuestion` "App name", option *"Keep \"…for Pixel Buds Pro 2\" + ADR"*, with this preview) — build it in
this session, no further question on the name itself:** app name **"OpenControl for Pixel Buds Pro 2"** (written exactly so); launcher label (under the icon)
**"OpenControl"**; in the README and on the Info tab: *"Works with Google Pixel Buds Pro 2. Not affiliated with or endorsed by Google. Pixel Buds is a trademark of
Google LLC."*; a new ADR (the next free number in `id_registry.csv`) and a dated note in `AGENTS.md` §12 and in `PROJECT.md`'s non-goals recording this as a
deliberate exception (the name as plain text only — no Google logo, wordmark image, icon or asset). Show the ADR text and both notes in chat and get a yes
before writing them (`AGENTS.md` §6). The `applicationId` stays `io.github.tedsluis.opencontrolpixelbuds`.

**Also asked (chat 2026-10-02): automate the README video.** Build (after the checkpoint) a local script under `scripts/` that turns a screen recording into
GitHub-ready media with `ffmpeg` only (no new dependency): H.264 MP4 without audio, metadata stripped (`-map_metadata -1`), scaled (e.g. 1280 px high, CRF
≈ 28, `+faststart`), checked to stay under GitHub's 10 MB video limit for attachments (`ai-sessions/0066` measured 0.55 MB for the 60 s recording), plus an
optional short GIF or animated WebP and a poster frame (PNG). Present at the checkpoint, with pros and cons: (a) commit the small MP4/GIF/poster and link them;
(b) upload the MP4 with `gh issue comment <n> --attach <file>` (GitHub Docs "Attaching files with GitHub CLI": `--attach` "uploads the file to GitHub and writes the
resulting URL into the body" — available on `gh issue`/`gh pr` create/edit/comment only) to get a `user-attachments` URL that plays in the README — an
outward-facing step (a public comment) needing the maintainer's yes each time; verify what happens to the file if that comment is deleted; (c) a GitHub Actions
job running the same script when a recording under `images/` changes (ffmpeg on the runner; it would commit the result — weigh bot commits against a local run).

---

## 2. Facts and corrections this prompt's author found (verify each; they are starting points, not conclusions)

- **GitHub Releases fits ADR-004** ("updates are distributed manually (e.g. via GitHub Releases)") and `TODO.md` Phase 5 ("Prepare the first public release (tag,
  `CHANGELOG.md` entry, GitHub Release per the manual-update-distribution decision in `AGENTS.md` §1)"). Users can also follow GitHub releases with
  Obtainium; IzzyOnDroid and F-Droid are further manual-distribution channels — proposals only (§4 task 6).
- **The signing key, precisely:** Android installs an update only if it is signed with the same key (or a key rotated to via APK Signature Scheme v3); a
  differently signed APK must be uninstalled first, which deletes the app's data. The debug key is per machine (`~/.android/debug.keystore`), so even two debug
  builds from different machines cannot update each other. Verify against developer.android.com ("Sign your app", `apksigner`) and quote it.
- **Where the secret lives:** `local.properties` is already gitignored (`android/.gitignore`, root `.gitignore`) but is meant for the SDK path; the usual
  alternatives are `~/.gradle/gradle.properties` (outside the repository) or environment variables. The keystore file itself must live **outside** the
  repository (and outside `android/`), with an encrypted offline backup — losing it means no update can ever be signed. Add `*.jks`, `*.keystore`, `*.p12`,
  `keystore.properties` to `.gitignore` regardless.
- **This session must never see the real secrets.** The maintainer creates the keystore and types the passwords himself (commands are given; the maintainer
  runs them, e.g. with the `!` prefix). The session tests the signing configuration only with a **throwaway** keystore in the scratchpad, generated with
  throwaway passwords, never committed, deleted at the end.
- **Version:** `android/app/build.gradle.kts` has `versionName = "0.1.0-dev"`, `versionCode = 1` and no `signingConfigs` block; `BuildConfig.GIT_COMMIT` gets
  "-dirty" when tracked files differ from `HEAD` (`ai-sessions/0062` F-5) — a release must be built from a clean tree at the tagged commit, and the Info tab
  must then show the tag's hash without "-dirty". A versionCode scheme is a checkpoint question.
- **The `0064` build is not hardware-verified.** `CAP-067` (Group BC) is planned, not run (`TODO.md` "Open after `ai-sessions/0064`"). Releasing an untested build
  as "1.0.0" is a decision for the maintainer: run `CAP-067` (or the core of `APP_TESTPLAN.md`) **on the release-signed APK** first, or publish a GitHub
  pre-release (e.g. `v1.0.0-rc.1`). Checkpoint question.
- **The Definition of done is not demonstrably met as written.** `PROJECT.md` says "*Without Google Play Services installed*, the app can: …". The OpenControl
  hardware runs `CAP-059`…`CAP-066` were on a Pixel 9a **with** Google Play services present (e.g. `CAP-066-FINDINGS.md` header: "Google Play services
  present"); `CAP-035` (GMS disabled) predates the app. Build an evidence table per criterion (capture + finding, or "not shown") and do **not** tick a
  criterion without evidence; the options (a test run without Play services, or a reworded criterion through the documented rules) are a checkpoint question.
- **Trademarks:** decided 2026-10-02 (§1, "Decided by the maintainer"): build it; the checkpoint only confirms the ADR and note texts.
- **Media:** `images/` is not under Git LFS (`.gitattributes` covers `captures/**` only); the screenshots and the video are untracked so far; the video is
  15.3 MB. GitHub's README renderer does not play a repository-relative `.mp4` inline (check GitHub's docs and say what it does); options: a short GIF or a
  poster frame linking to the video, the video as a release asset, or LFS. Check every image for personal data (status bar, notifications, account names,
  device names, EXIF — a first check found only 3 EXIF tags and no GPS); the maintainer's camera films have carried a street-address overlay before. The docs
  site (Docsify, `index.html`) renders the README too — check the images there.
- **Third-party licences:** the APK bundles Apache-2.0 libraries (AndroidX, Compose, Hilt/Dagger, Kotlin, Material icons). Check what their licences require
  when redistributing a binary (e.g. a NOTICE) and whether the app or the release must show them; propose, do not add a dependency for it.
- **CI release:** a workflow that signs on GitHub needs the keystore as a repository secret (the key leaves the maintainer's machine); alternatives — CI builds
  and attaches an unsigned APK plus checksum for the maintainer to compare, or CI only verifies that the tagged commit builds. Keep the existing pinning style
  (actions pinned to full commit SHAs) and least privilege (`contents: write` only in the release job).
- **What a careful release note carries:** what the app does, Android 14 (API 34) minimum, tested on Pixel 9a / GrapheneOS with firmware `release_5.203` only
  (other firmware ⇒ read-only Safe Mode, ADR-042), the APK's SHA-256, and the signing certificate's SHA-256 fingerprint (`apksigner verify --print-certs`) so
  users can verify the download (e.g. with AppVerifier on GrapheneOS) — verify these claims and propose.

---

## 3. Rules for this session

- **Evidence** (`PROJECT_RULES.md` rule 4a): every claim about the app links to a file:line, a capture, a test or a gate log; every external claim quotes the
  official text with URL and fetch date. A negative needs its command, its exit status and a positive control (`AGENTS.md` §13 step 8).
- **Labels and the `AGENTS.md` §6 gate:** nothing in `PROTOCOL.md` changes; no FACT is promoted; no ADR is written or updated, and no ADR number is registered,
  without the maintainer's approval given **in this chat**. A release process that changes a recorded decision (e.g. signing on GitHub, a renamed app) is
  drafted as an ADR proposal (the next free number in `id_registry.csv`) and decided at the checkpoint.
- **Guardrails of `AGENTS.md`:** no `INTERNET`, no new permission, no analytics or crash reporting, no update checker in the app (ADR-004: updates are manual),
  no Google trademarks or assets added, no new dependency without the §10 justification, AGPL headers on every new Kotlin file, Kotlin only.
- **Secrets:** never read, print, log, commit or ask for the real keystore, its passwords or any token. No secret in a command line that lands in shell history
  (`keytool` prompts for passwords — let it). `git status` and a `git diff --cached` check before every commit: no `*.jks`, `*.keystore`, `*.p12`, passwords.
- **Outward-facing actions need an explicit "yes" in chat, each time:** pushing a tag, creating or editing a GitHub release, uploading an asset, changing
  repository settings or secrets, pushing a workflow that publishes. A tag and a release are public at once and cannot be fully taken back. Prefer giving the
  maintainer the exact commands; run them only after that "yes".
- **Scope:** stay within §1 and §2; every further idea is a proposal at the checkpoint, built only after approval.

---

## 4. Tasks

### Phase 0 — set-up

1. Create the RESULT file (§0). Record `git log -1`, `git status --short`, `git fetch && git log --oneline HEAD..origin/main` (fast-forward if CI added a
   commit) and the last commit under `android/`.
2. Baseline gate from clean: `cd android && ./gradlew --offline clean` then `./gradlew --offline assembleDebug testDebugUnitTest test lint`, plus
   `./gradlew --offline assembleRelease` (today: an unsigned release APK, or a failure — record which and why). Test counts per module from the JUnit XML,
   lint issues, compiler warnings. If the gate is red, stop and report.

### Phase A — validation and design (no change to tracked files)

3. **Official sources** — fetch and quote: GitHub Docs (creating a release, release assets and their size limit, pre-releases, `gh release create`, tags and
   protected tags, Actions `GITHUB_TOKEN` permissions, encrypted secrets, how README renders images and video); developer.android.com (app signing, `keytool`
   key generation, `apksigner` sign/verify and `--print-certs`, signature schemes v2/v3 and key rotation, the update rule for a different key, versionCode
   rules, installing unknown apps / sideloading on Android 14); Gradle/AGP `signingConfigs` reading values from properties or environment.
4. **The app as it is:** `assembleRelease` output (signed or not, size, `minifyEnabled`), the release manifest (no `INTERNET`, the permissions, `debuggable` false),
   `BuildConfig` values in a release, StrictMode only in debug (`ai-sessions/0062` F-8), the Info tab text in a release.
5. **Evaluate every point of the maintainer's plan (§1)** — keep, correct, or replace, each with a reason — and write a **draft release runbook** (the exact
   commands in order, from creating the key to the published release, with the backup step and a verification step), a **draft release-notes text** and a
   **mock-up of the new README structure** (headings, the order of sections, the new texts).
6. **More ideas — propose, do not build.** At least consider: a GitHub pre-release first and the hardware run on the signed APK; SHA-256 and certificate
   fingerprint in the notes; signed git tags (`git tag -s`); a new runbook document (e.g. a root-level RELEASING file) or a section of an existing document; a `scripts/release.sh` that
   builds, verifies (`apksigner verify`, no `INTERNET` in the merged manifest, clean tree, versionName = tag), computes checksums and prints the
   `gh release create` command without running it; Obtainium instructions in the README; IzzyOnDroid / F-Droid later (what each requires: reproducible build,
   metadata, their signing); reproducible-build notes; issue templates (`.github/ISSUE_TEMPLATE/`: phone, Android/GrapheneOS version, app build from the Info tab,
   firmware, a debug export without the Buds' address); `SECURITY.md` pointing to GitHub's private vulnerability reporting; a short privacy statement in the README;
   repository "About" (description, topics, website = the docs site); third-party licence notices; R8/minify for release (size vs. risk with Hilt — likely
   "not now"); what the docs site needs. For each: what, why, cost, risk, and whether it touches a guardrail.

### Phase B — checkpoint (stop and ask, in this chat, via `AskUserQuestion`)

7. One question per decision; every option with pros and cons, one marked "(Recommended)", the exact text, command list or mock-up in the preview. At least:
   **(a)** release readiness — `CAP-067` / the core of `APP_TESTPLAN.md` on the release-signed APK first, or a pre-release, or 1.0.0 now; **(b)** version name and
   versionCode scheme; **(c)** where the signing values live (`~/.gradle/gradle.properties`, environment variables, a gitignored `keystore.properties`) and the
   key parameters (algorithm, size, validity); **(d)** the CI option (none now / build + checksum on a tag / sign on GitHub with secrets — with an ADR draft if
   the latter); **(e)** the README structure and texts (the mock-up) and the media (which images, how the video is shown — options (a)/(b)/(c) above, LFS or not); **(f)** the Definition of
   done (the evidence table; tick, test first, or reword); **(g)** the CHANGELOG `[1.0.0]` block (its text, and how the research history above it is kept);
   **(h)** the ADR and the `AGENTS.md`/`PROJECT.md` note texts for the decided app name; **(i)** the further ideas of task 6 — a multi-select, each as its own option. Record the answers verbatim in the
   RESULT. Build only what is approved.

### Phase C — build what is approved (no publishing)

8. `android/app/build.gradle.kts`: a `release` `signingConfig` that reads its four values from the approved source and **fails the release build with a clear
   message** when they are missing (never silently signing with the debug key); `versionName`/`versionCode` as approved. `.gitignore` entries for key files.
   Test with a throwaway scratchpad keystore: `assembleRelease` → `apksigner verify --verbose --print-certs` → the throwaway certificate; without the values →
   the clear failure. Delete the throwaway key afterwards and say so.
9. The approved scripts/workflow/docs (`scripts/release.sh`, a workflow change, a runbook document, issue templates, …). A new workflow or job keeps the pinned-SHA
   style and the no-`INTERNET` checks; a release job runs only on `v*` tags.
10. `README.md` per the approved mock-up; the approved images (and only those) added; links checked on GitHub's renderer and the docs site; `PROJECT.md`'s
    Definition of done as approved; `CHANGELOG.md` as approved; `generate_sidebar.sh`/`_sidebar.md` if a new root document was added.

### Phase D — gate and compliance

11. `./gradlew --offline clean`, then the full gate of task 2 plus `assembleRelease` with the throwaway key: all green, no new lint issue, suppression or warning.
    Compliance: no `INTERNET` in any manifest (source and merged, release variant included; positive control `BLUETOOTH_CONNECT`); `git diff` of the manifests
    shows only what was approved; no new dependency (or the approved one with its §10 justification); no key, password or token anywhere in the diff or the
    history of this session (`git diff --cached`, a grep for `storePassword`/`keyPassword` values); `python3 scripts/ensure_footers.py` and
    `python3 scripts/lint_docs.py` exit 0 (report the "historical" bucket separately).

### Phase E — the maintainer's release steps

12. Give the maintainer the final runbook in the RESULT and in chat, as a numbered list of commands to run himself: create the key (with backup), set the
    values, build from a clean tree at the release commit, verify (signature, certificate fingerprint, Info tab hash without "-dirty", no `INTERNET`), checksum,
    tag (and push), create the (pre-)release with the notes and the APK, check the release page and a fresh install on the phone. Run any of these only after
    an explicit "yes" for that step in chat.

### Phase F — finish

13. Documentation, only what changed: `TODO.md` (the Phase 5 release item; new items), `CHANGELOG.md`, `ai-sessions/INDEX.md` (the 0065 row), `ARCHITECTURE.md`
    §14 (build configuration: release signing) if the build changed, `CONTRIBUTING.md` if building/signing instructions for contributors changed.
14. Finish the RESULT: a plain-language summary first (what the maintainer has to do, in order), then the validation of each point of the plan, the sources
    (URL + quoted sentence + fetch date), what was built and where, the gate table, compliance, the runbook, open items. It must end with **"Deferred
    documentation"** (each item also in `TODO.md`) and **"Commits"** (hashes, or "not committed — reason"), per `AI_SESSION_LOG_PROCEDURE.md` §9. Set the Status
    per §4.
15. Show the maintainer a short summary and **ask whether to commit and push** the repository changes (separately from any release step). Only after a yes:
    Conventional Commits, one commit per concern (`build`/`ci` for the signing config and workflow, `docs` for README/PROJECT/CHANGELOG/runbook, `chore` for
    images), each with a *why* and the attribution line from the session's system reminder; `git fetch` and `git log origin/main..HEAD` first (rebase if CI added a
    commit, never force); nothing from a build directory, `android/.kotlin/`, a keystore or the scratchpad staged.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **No network from the app** and no update checker: a user learns about a new version from the Releases page (or a tool like Obtainium), never from the app.
- **Secrets never pass through this session** (§3). The throwaway test key exists only in the scratchpad and is deleted.
- **Publishing is the maintainer's act**, step by step, after an explicit "yes" in chat (§3).
- **Honest public text:** the README and the release notes claim only what the captures, the tests or `ARCHITECTURE.md` §5a support (hardware-verified vs. only
  unit-tested, the one verified firmware, the one tested phone); no Google logo or wordmark as an image; a "not affiliated with Google" line.
- **Approvals only in this chat** (`AGENTS.md` §6; memory "Approvals: confirm in chat").
- **Files.** Before deleting, moving or renaming a file, compare a fresh listing (checksums); never chain deletes; ask before any delete.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0065_MAINTENANCE_PROMPT_2026_10_01.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0065_MAINTENANCE_PROMPT_2026_10_01
