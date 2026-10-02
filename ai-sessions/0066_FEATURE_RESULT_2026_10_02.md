# 0066_FEATURE_RESULT_2026_10_02.md — Two app changes before `CAP-067` (balance slider without the `‹`/`›` steps; no `LICENSE` link on Info) and answers to the maintainer's release-preparation questions

**Number:** 0066
**Category:** FEATURE
**Date:** 2026-10-02
**Title:** Remove the balance steps of `ai-sessions/0064` F-2 and the Info tab's "Licence on GitHub" link, keep everything aligned, and answer the
maintainer's questions on testing without Google Play services, clean release builds, video in the README, the app name and R8
**Status:** complete

---

## Summary (plain language)

1. **Sound tab:** the balance is the plain slider again (no `‹`/`›`), exactly as before `ai-sessions/0064` — one change per release of the finger, a release near
   the middle snaps to Centre. Note: reaching exactly Right 4 by dragging is as hard as in `CAP-066` (32 drags, never Right 4); `CAP-067` BC-3 now counts the drags.
2. **Settings → Info:** "Read the licence" (the full text, offline) stays; the "Licence on GitHub" link is gone. "README on GitHub" and "Report an issue on
   GitHub" stay. DECISIONS.md ADR-050 has a dated Update (approved in chat).
3. The answers to the questions are in §C (and were given in chat).

## A. What changed

- **Balance:** `EqScreen.kt`, `BudsSettings.kt`, `BalanceSnapTest.kt` restored from `d5fdf10` (the commit before `0064`; `git diff --stat d5fdf10 HEAD` showed only
  the `0064` F-2 lines in these files). Kept: the real-byte fixtures `Cap066Fixtures.kt` and the codec tests (`17:11`, `17:0`, `17:7` = `CAP-064` 6671), the
  `EqScreenTest` drag test (one write per release, none per drag frame). Changed: `BudsRepositoryImplTest` "four steps" → "a balance of Right 4 writes the captured
  `17:7`" (`setVolumeBalance(-4)` on channel 21 → `CAP-064` 6671 byte for byte, ACK 6673 applies it). Removed: the six `EqScreenTest` step tests and the
  `balanceStep` domain test.
- **Info:** `SettingsMenu.kt` — the "Licence on GitHub" button and `ProjectLinks.LICENSE_URL` removed; `SettingsMenuTest` asserts the two remaining URLs exactly and
  that "Licence on GitHub" does not exist.
- **ADR-050 Update** (maintainer, chat 2026-10-02, `AskUserQuestion` "ADR-050", option *"Update as shown (Recommended)"*): two URLs; everything else unchanged.
  `id_registry.csv` ADR-050 row extended.
- **Aligned:** `ARCHITECTURE.md` §1/§2.4/§9, `APP_TESTPLAN.md` (header note, M3, R1, R3, R5), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BC, Capture Index row),
  `CAP-067-EVENT-NOTES.md` (purpose, P7, section II = the slider: BC-3/BC-5, BC-R; A.4), `README.md` status, `CHANGELOG.md`, `TODO.md` ("Open after
  `ai-sessions/0066`"), the `ai-sessions/0065` prompt (a dated note: nothing is published yet; Info has two links; the slider is back).

## B. Gate and checks

`./gradlew --offline clean` then `./gradlew --offline assembleDebug testDebugUnitTest test lint` → BUILD SUCCESSFUL (2 m 30 s). Tests (debug; release equal):
`:data` 1588, `:domain` 30, `:hardware` 56, `:ui` 35 — 0 failures. Lint 0 issues in `:app`/`:ui`/`:data`/`:hardware`; compiler warnings 0. Mutation: the
"Licence on GitHub" button put back → `SettingsMenuTest` fails ("… no LICENSE link"); file restored byte-identical (`sha256sum`). No manifest, dependency or
permission change.

## C. Answers to the questions (sources fetched 2026-10-02)

1. **Testing without Google Play services, without removing it.** GrapheneOS installs sandboxed Google Play **per user profile**; a new secondary user has none.
   GrapheneOS features page: *"GrapheneOS enables the standard install available apps feature … to allow the Owner user to install packages that are available in
   other users. This allows installing an app in a secondary user that's already installed in the Owner user without needing to download it again."* Route:
   Settings → System → Users → add a user; in the Owner: that user → "Install available apps" → OpenControl (or `adb install --user <id> app-debug.apk`); switch to
   the user; the Buds' bond is device-wide, but the app's companion (CDM) association is per user, so in that user tap **Pair a device** once and pick the Buds (no new
   bonding). HCI snoop stays a device-wide developer setting (set in the Owner). 🟡 HYPOTHESIS to verify in the capture: while the secondary user is in the
   foreground the Owner's Play services cannot use Bluetooth (AOSP restricts Bluetooth to the foreground user; sandboxed Play has no privileged exemption) —
   the evidence is the HCI log: no Play-services claim on the Message Stream (first message `03 08 00 02 01 25`, the marker used in `CAP-066-FINDINGS.md`). If such a
   claim appears, that run is not "without Play services". Alternative: disable Google Play services temporarily in the Owner (reversible, nothing reinstalled) —
   less clean, as in `CAP-035`.
2. **A release without "-dirty".** "-dirty" means *tracked* files differ from the commit (`git status --porcelain --untracked-files=no` non-empty,
   `app/build.gradle.kts`); untracked files such as `android/.kotlin/` do not count. So: commit (or stash) everything, check `git status`, then build. Safest: a
   separate worktree at the release commit — `git worktree add ../ocp-release <commit-or-tag>`, `cd ../ocp-release/android && ./gradlew clean assembleRelease` —
   always clean, your working copy untouched; afterwards `git worktree remove ../ocp-release`. The Info tab then shows "build <hash> (<date>)" of that commit.
   (A signed release needs the signing set-up of the `0065` prompt; today `assembleRelease` gives `app-release-unsigned.apk`.)
3. **Video in the README.** GitHub Docs (*Attaching files*): *"10MB for videos uploaded to a repository owned by a user or organization on a free GitHub plan"*,
   formats `.mp4`, `.mov`, `.webm`, and *"we recommend using H.264 for greatest compatibility"*; an uploaded file gets an anonymized `user-attachments` URL that
   GitHub renders as a player. A repository-relative `.mp4` link is shown as a link, not played (to verify on the page); Docsify (the docs site) can play an HTML
   `<video>` tag. Measured on the recording (scratchpad, nothing committed): re-encoded H.264 without audio at 1280 px height, CRF 28 → **0.55 MB**; at 960 px,
   CRF 30 → **0.35 MB**; a GIF of the whole 60 s at 320 px, 8 fps → **1.5 MB**; a 12 s GIF at 320 px → **0.17 MB**. So: compress first (the 15.3 MB comes from
   1080 × 2424 at a 90 kHz timebase; the screen hardly moves). Options: (a) the compressed MP4 uploaded via the GitHub editor → a player in the README on github.com;
   (b) a short GIF (or animated WebP) inline — plays everywhere incl. the docs site, but low quality and no controls; (c) a screenshot linking to the MP4 (in the
   repository or as a release asset). Do not put README media in Git LFS: GitHub Pages does not serve LFS files.
4. **The name "Pixel Buds".** The app label is "OpenControl for Pixel Buds" (`app/src/main/res/values/strings.xml`); `AGENTS.md` §12 bans the "Pixel Buds"
   wordmark/logo in app resources. Options: (a) rename the label to a neutral name (e.g. "OpenControl") and say "works with Google Pixel Buds Pro 2" in the README —
   fully within §12; (b) keep a descriptive "… for Pixel Buds" as plain text with "not affiliated with or endorsed by Google; Pixel Buds is a trademark of Google LLC" —
   needs your explicit decision and an `AGENTS.md`/ADR change (it conflicts with §12's text), and is a legal judgement, not mine; (c) a new own name. Keep the
   `applicationId` `io.github.tedsluis.opencontrolpixelbuds` either way: it is not shown to users, and changing it after the first release breaks updates. Decide
   before the first release (the `0065` checkpoint).
5. **R8.** The Android build tool that, for a release build with `isMinifyEnabled = true` (and `isShrinkResources = true`), removes unused code and resources,
   optimizes, and renames classes (obfuscation). Today's unsigned release APK without it: **21.2 MB** (`app-release-unsigned.apk`); with R8 a Compose/Hilt app
   usually shrinks to a few MB (to be measured). Why not now: a minified build is a different program from the one every unit test and hardware run checked
   (they run unminified), so a missing keep-rule can crash only the release build; obfuscated stack traces in a debug export need the mapping file; and it
   complicates reproducible builds. Sensible later, with its own hardware run on the minified APK.

## Deferred documentation

Also in `TODO.md` ("Open after `ai-sessions/0066`"): balance precision is open again; the "without Play services" test in a secondary user; the pre-release
questions (name, R8, video) for the `0065` session.

## Commits

Committed and pushed after the maintainer's yes in chat (2026-10-02, "ja, commit en push"):

- `a0a95d3` — fix(app): balance slider without steps, no LICENSE link on Info (0066).
- `0a90d27` — docs: ADR-050 Update and the 0066 alignment (CAP-067, test plan, architecture).
- `5ee334e` — docs: prompt 0065 (release preparation) and session 0066 files; plus the follow-up commit that records these hashes.

Not staged: `android/.kotlin/`, the untracked screenshots and video in `images/` (left for the README session).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0066_FEATURE_RESULT_2026_10_02.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0066_FEATURE_RESULT_2026_10_02
