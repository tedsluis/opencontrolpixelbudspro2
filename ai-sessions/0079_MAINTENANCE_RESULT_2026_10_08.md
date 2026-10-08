# 0079_MAINTENANCE_RESULT_2026_10_08.md — Prepare release 1.1.1 (the toolchain upgrade of `ai-sessions/0078`) and the skeleton of its hardware run (the next capture, Group BG)

**Number:** 0079
**Category:** MAINTENANCE
**Date:** 2026-10-08
**Title:** Prepare the next release — the toolchain and dependency upgrade of `ai-sessions/0078` shipped as a version of its own — up to the maintainer's `scripts/release.sh`; the skeleton of its hardware run; `RELEASING.md` brought in line with the repository rules; no tag, no build by the agent, no publication
**Status:** complete (committed and pushed to `release/1.1.1`, pull request open, not merged — see Commits)

## Progress

- **Done:** reading; Phase 0 (branch `release/1.1.1` from `7101d9c`, gate equal to 0078's final gate); Phase A (inventory, release machinery, reference
  bytes, `TODO.md` §2 fit, the repository rules re-derived).
- **Done (Phases B–E):** checkpoint (two rounds, all approved); version 1.1.1 / 10101; the `// TODO(verify)` reworded; `CHANGELOG.md`, `README.md`, the
  release-notes template; `RELEASING.md` "Repository rules" (task 8a); gate again (equal); `CAP-071` skeleton, its registry and index rows, the Group BG
  section, `APP_TESTPLAN.md` U; `TODO.md`, `ai-sessions/INDEX.md`; `ensure_footers.py` and `lint_docs.py` exit 0.
- **Next:** `RELEASING.md` B1–C2 (the maintainer), then a CAPTURE session for `CAP-071`.
- **Touched, unverified:** none.
- **Intermediate results:** session scratchpad `…/scratchpad/0079/`.

## Summary (plain language)

**What the release is.** 1.1.1 is the same app as 1.1.0, built with the new tools and libraries of `ai-sessions/0078` (Gradle 9.7.1, the Android Gradle
plugin 9.3.3, Kotlin 2.4.20, Jetpack Compose 2026.09) — nothing changes for a user. Since `v1.1.0` the app's code changed only where 0078 made code-neutral
fixes; this session added nothing to the app but its version (1.1.1, versionCode 10101) and one reworded comment (the `// TODO(verify)` that is now
backed by three captures). The tests, lint and warnings are exactly as after 0078.

**What is prepared** on the branch `release/1.1.1`: the version; `CHANGELOG.md`'s `[1.1.1] - not yet released` block; the README's status line; the
release-notes template; `RELEASING.md` now describes the repository rules set on 2026-10-08 (pull requests only on `main`, release tags that cannot be moved,
the bot pull requests for the sitemap/sidebar) — and one gap found while checking them: the repository still allows squash and rebase merges (a
proposal in `TODO.md`, nothing changed). The hardware run is written out as `CAP-071` (Group BG): 24 steps in two films — the update over 1.1.0 with your
dark-mode and Debug-mode choices kept, every screen, every write once compared byte for byte with the 1.1.0 build's own requests, the case and Bluetooth
off/on, and then every open film item (lead L-1 with the head in view, the early tap C12, the export across a rotation, the screen-reader dumps). Two of
its writes (preset Balanced, the mode list without Off) also put the Buds back as `TODO.md` §4 asks.

**What you do next, step by step** (`RELEASING.md`): (1) merge nothing yet — check CI on the pull request (A6); (2) B1 `scripts/release.sh 1.1.1` on the
pushed branch tip and note the build commit; (3) B2 the certificate fingerprint; (4) B3 keep `dist/1.1.1`; (5) C1–C2 install it over 1.1.0 in the test user
and film `CAP-071` with its skeleton; (6) a CAPTURE session analyses it and gives the verdict; then D (merge with a merge commit, tag, release) and E.

## Phase 0 — set-up

`git log -1`: `7101d9c Merge pull request #14 from tedsluis/prompt/0079-releasing-rules` on `main`; `git status --short`: only this RESULT (untracked);
`git fetch && git log --oneline HEAD..origin/main`: empty (exit 0). Branch **`release/1.1.1`** from `origin/main` (the name recommended by `RELEASING.md` A1;
checkpoint (b) decides). `git log --oneline v1.1.0..origin/main`: 38 commits (31 without merges) — §A.1.

**Gate** (`cd android && ./gradlew --offline --max-workers=2 --warning-mode all clean assembleDebug testDebugUnitTest test lint`, scratchpad 0079/logs/gate0.log):
exit 0, `BUILD SUCCESSFUL in 3m 59s`. Tests (JUnit XML): `:data` 1627/1627, `:domain` 37, `:hardware` 64/64, `:ui` 70/70; 0 failures/errors/skipped. Lint:
`:app` 1 (`OldTargetApi`), `:ui` 1 (`ModifierParameter`), `:data`/`:hardware` 0. Compiler warnings: 0 ("Problem found" 0, `^w: ` 0). Gradle deprecations: the
one inside AGP ("Using a Project object as a dependency notation"). **Identical to `ai-sessions/0078`'s final gate.**

## Phase A — inventory (no change)

### A.1 What the release contains (task 3)

`git log --oneline --no-merges v1.1.0..origin/main` (31 commits): the `CAP-070` analysis and the 1.1.0 after-publish documents (`ai-sessions/0075`, `4bcc409`…`4076ed0`);
`ai-sessions/0076` (`cfc8537` the `CAP-070` frames as test fixtures, `ba804c5` ADR-055 Update); `ai-sessions/0077` (documents, ADR-056/057); `ai-sessions/0078`
(`c6633c8`…`462b829`, the toolchain); the 0079 prompt and the CI bot change (`8b4a095`); sitemap/sidebar bot commits. **App code since `v1.1.0`:**
`git diff --name-only v1.1.0 origin/main -- 'android/*/src/main'` → eight files: `SettingFrame.kt` (comment only — `ai-sessions/0076` removed ADR-055's
`// TODO(verify)`), and the seven of 0078's code-neutral commit (`MainActivity.kt` `toUri()`, `BudsRepositoryImpl.kt` the redundant cast, `EqScreen.kt`/
`OpenControlIcons.kt` comments, `OpenControlNavHost.kt`/`PullRefresh.kt` opt-ins, `SettingsMenu.kt` `TabRow` suppression and `LocalResources`). **User-visible
effect: none** (0078 RESULT tasks 11–13). `CHANGELOG.md` `[Unreleased]` names the 0078 build and CI changes, nothing else — it matches (0076/0077 changed no
app behaviour).

### A.2 The release machinery with the new toolchain (task 4, read-only)

- `scripts/release.sh` picks `ls -d $sdk/build-tools/* | sort -V | tail -1` → `~/Android/Sdk/build-tools/36.0.0` (installed by AGP in 0078); it holds `apksigner`,
  `aapt2` and `dexdump` (`ls … | grep -E "^(apksigner|aapt2|dexdump)$"` → all three).
- `./gradlew --offline … assembleRelease` in the script's worktree needs: the Gradle 9.7.1 distribution (in `~/.gradle/wrapper/dists`, used by this session's
  gate), `platforms;android-37.0` (installed), every dependency (the offline gate and 0078's offline `:app:compileReleaseKotlin :app:processReleaseMainManifest
  lintRelease` resolved them; packaging adds no dependency) and the signing values (the maintainer's). Not run here — it needs the key.
- `scripts/third_party_notices.py`: 0078 ran it offline on the new tree (136 libraries, exit 0, 0 "licence not found"); the script reads the release runtime
  classpath, unchanged since.
- The script uses `git worktree add` with `GIT_LFS_SKIP_SMUDGE=1` (no LFS download) — run by the maintainer, not this session.

### A.3 The reference bytes for the run (task 5)

Re-derived from the logs (commands: `python3 -I scripts/pwrpc_decode.py --handle 0x000b <log>` → scratchpad 0079/pwrpc-A.txt, exit 0, 540 packets; then `tshark -r
<log> -Y "bthci_acl.chandle==0x000b && frame.number in {…}" -T fields -e frame.number -e btrfcomm.dlci -e data.data`, exit 0, 46 rows — scratchpad
0079/refbytes-A.tsv (scratchpad). An earlier attempt with a space-separated set failed with a filter syntax error, exit 4 — not an empty result). "A" =
`captures/CAP-070-2026-10-07_06-10-41_06-40-59-Group_BF/CAP-070-btsnoop_hci2.log.last` (1.1.0, the reference build); every request carries the prefix
`7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25` (channel 21) or `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25` (channel 19), then method id, payload, CRC, `7e`.

| Request | Channel 21 (frame: bytes after the prefix) | Channel 19 (frame: bytes after the prefix) |
|---|---|---|
| `ReadSetting 4:16` | A271 `51aed0ae 2a022010 47eeadcf 7e` | A966 `51aed0ae 2a022010 fdae3f24 7e` |
| `ReadSetting 4:N`, N = 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29 | A296, A299, A302, A305, A308, A311, A314, A317, A320, A323, A326, A329 (`…2a0220<N>` + CRC, `refbytes-A.tsv`) | A979, A1005, A1008, A1011, A1014, A1017, A1020, A1023, A1026, A1029, A1032, A1035 |
| `SubscribeRuntimeInfo` | A332 `90821ee6 6654bfab 7e` | A1038 `90821ee6 602d65a9 7e` |
| `WriteSetting 4:{11:0}` / `{11:1}` | A752 `…2a0422025800ad636bac7e` / `CAP-069` 3212 `…2a04220258013b536cdb7e` | A3668 `…2a04220258009d8f9dc47e` / A1989, A3679 `…2a04220258010bbf9ab37e` |
| `WriteSetting 4:{29:1}` / `{29:2}` | A762 `…2a052203e80101bc106ba27e` / `CAP-069` 2564 `…2a052203e801020641623b7e` | A3614 `…2a052203e80101fcd6da847e` / A1998, A3685 `…2a052203e801024687d31d7e` |
| `WriteSetting 4:{28:0}` / `{28:1}` | A770 `…2a052203e0010092717fdb7e` / `CAP-058` 5643 `…2a052203e00101044178ac7e` | `CAP-024` 1988 `…2a052203e00100d2b7cefd7e` / A1992 `…2a052203e001014487c98a7e` |
| `WriteSetting 4:{27:0}` / `{27:1}` | A777 `…2a052203d80100bac507f17e` / `CAP-058` 5697 `…2a052203d801012cf500867e` | `CAP-024` 2053 `…2a052203d80100fa03b6d77e` / A1995 `…2a052203d801016c33b1a07e` |
| `WriteSetting 4:{15:0}` / `{15:1}` | A785, A4770 `…2a04220278000f47ef397e` / A4800 `…2a04220278019977e84e7e` | A3072 `…2a04220278003fab19517e` / A1953 `…2a0422027801a99b1e267e` |
| `WriteSetting 4:{17:7}` (Right 4) | `CAP-064` 6671 `…2a052203880107a97d5edf037e` | A3747 `…2a052203880107e9b86e257e` |
| `WriteSetting 4:{16:…}` (EQ) | A4829 (`5:-4.26`); BALANCED = `CAP-059` 2188 `…2a1e221c8201190d000060c0150000003f1d0000803f25000080bf2d0000204008f7fa577e` (P7) | — |
| `WriteSetting 4:{12:{1:1 2:0 3:1 4:0}}` (P7) | `CAP-041` 2198 `…2a0c220a62080801100018012000fc57b66a7e` | — |
| Message Stream: ANC `Get` | A (11 claims): `08 11 00 00` — `tshark -r A -Y 'bthci_acl.chandle==0x000b && btrfcomm.len>0 && (btrfcomm.dlci==4\|\|btrfcomm.dlci==5) && frame.p2p_dir==0' … \| uniq -c` → `11 08110000` | |
| ANC `Set` (Transparent / Off / Active / Adaptive) | `CAP-068-btsnoop_hci.log` 1039 `0812001401e8e880` + 16 × `00`, 1090 `…e8e820…`, 1146 `…e8e808…`, 1266 `…e8e840…` (1.0.1, the same encoder) | |
| Ring Left / Stop / Right | `CAP-068-btsnoop_hci2.log` 2142 `0401000102`, 2416 `0401000100`; `CAP-062-btsnoop_hci.log` 9660 `0401000101` | |

The frames from other captures were re-derived the same way (`tshark -r <log> -Y "frame.number in {…}" -T fields -e frame.number -e
bthci_acl.chandle -e btrfcomm.dlci -e data.data`, exit 0 each: `CAP-069-btsnoop_hci.log` 2564/3212, `CAP-058-btsnoop_hci.log` 5643/5697, `CAP-024-btsnoop_hci.log`
1988/2053, `CAP-064-btsnoop_hci.log` 6671, `CAP-059-btsnoop_hci.log` 2188, `CAP-041-btsnoop_hci.log` 2198, `CAP-068-btsnoop_hci*.log`, `CAP-062-btsnoop_hci.log` 9660); every one
equals what `CAP-070-EVENT-NOTES.md`'s table states. The analysing session compares every request of the run with this table; a request with no row is compared with the app's unit-test fixture of the same
request (`android/data/src/test`).

### A.4 `TODO.md` §2 items against this run (task 6)

| Item | Fits? | Needs | Expected |
|---|---|---|---|
| Update path (install over the previous release, `dumpsys` "User 10:" block) | **yes — the core of this run** | 1.1.0 still installed in user 10 (it is: `CAP-070`) | `lastUpdateTime` > user 10's `firstInstallTime`, `versionCode=10101` |
| T11 screen-reader text (`uiautomator dump` ×2) | yes | `adb` in the test user; Bluetooth off; ≈ 2 min | every `text="—"` node has `content-desc="Not read from the Buds yet"` |
| H5 *Read EQ again* | yes | nothing | the read equals the write just made |
| S12 export across a rotation | yes | auto-rotate on | "Debug log saved (N lines)", file non-empty |
| S6 on a screen recording | yes, with (e) | Android's screen recorder | "—" for ≈ 1 s after ready, then values |
| C12 a tap during an automatic re-open | possible, hard to time (`CAP-070` missed it) | screen recording; a bud out of the case, a tap within ≈ 2 s | "The app's channel is being reopened — try again in a moment.", no `WriteSetting` |
| L-1: the Left out with both worn on 19, head in view | possible | head and both ears in view (`CAP-070` P3 not done) | Buds `DISC` + announcement 21 |
| Case-sound / Volume-EQ observations | possible | a film with an audio track, or say it | an observation only |
| `SIGQUIT` source | yes, with (e) | the system log (GrapheneOS log viewer / `logcat -b all`) | what sent signal 3 |
| B4 double tap | no | a re-pair | — |
| K5/BC-12, F-4, a second device | no | the Owner user / a debug build / another phone | — |
| `OpenControlNavHost.kt:238` `// TODO(verify)` | (f) | a comment-only change before the build commit | — |

### A.5 The repository rules (task 8a's first half, re-derived)

- `gh api repos/tedsluis/opencontrolpixelbudspro2/branches/main/protection` (exit 0): pull request required, `required_approving_review_count` 0,
  `enforce_admins` **true**, `required_status_checks` **null**, force pushes and deletion not allowed, no linear history, no signature requirement.
- `gh api …/rulesets` → one ruleset, `24697736 Protect Release Tags tag active`; `…/rulesets/24697736`: `refs/tags/v*`, rules `deletion`, `non_fast_forward`;
  bypass `RepositoryRole` 5 (admin), `always`. Creating a `v*` tag is not restricted.
- `gh api …/actions/permissions/workflow` → `{"default_workflow_permissions":"read","can_approve_pull_request_reviews":true}`.
- `.github/workflows/update-sitemap.yml` / `.github/workflows/update-docs-sidebar.yml`: `pull-requests: write`; a changed file goes to `bot/sitemap` / `bot/docs-sidebar`
  (`git push --force` of that branch only) and one pull request is opened unless one is open.
- **Found:** `gh api repos/tedsluis/opencontrolpixelbudspro2 --jq '{allow_merge_commit, allow_squash_merge, allow_rebase_merge, allow_auto_merge,
  delete_branch_on_merge}'` → merge commit, squash **and** rebase all allowed — `RELEASING.md` D1 requires a merge commit, but the repository does not enforce
  it. Proposal for `TODO.md` (**M**), not changed here.
- Other documents with a direct-to-`main` instruction (`grep -n -i -E "on main|to main|push.*main|…"` over `AI_SESSION_LOG_PROCEDURE.md`, `CONTRIBUTING.md`,
  `README.md`, `AGENTS.md`, `PROJECT_RULES.md`): **none**; positive control: the same command lists `RELEASING.md` lines 98, 107 (the flowchart) and 19, 25,
  48, 50. In `RELEASING.md`: the flowchart's two "on main" boxes (98, 107), §6's "Commits made on `main` while you test" (229–230), §11 step 1 "Fix on `main`".

## Phase B — checkpoint (chat 2026-10-08, `AskUserQuestion`, answers verbatim)

Overview posted before the questions (summary): the release is 1.1.1 — the same app as 1.1.0 built with 0078's toolchain, no visible change; the release
machinery is ready (build-tools 36.0.0, everything offline but the key); the reference bytes are re-derived; the repository rules re-derived, with one finding
(squash and rebase merges are allowed although `RELEASING.md` D1 requires a merge commit) — a `TODO.md` proposal, no setting changed.

| Question | Answer (verbatim) |
|---|---|
| (a) Welk versienummer krijgt deze release? | "1.1.1 / 10101 (Recommended)" |
| (b) Welke branch voor de release? | "release/1.1.1 (Recommended)" |
| (c) Hoe breed wordt de hardware-run? | "Brede regressierun (Recommended)" |
| (e) Wat wordt er opgenomen tijdens de run? | "Film met geluid + schermopname + systeemlog (Recommended)" |
| (d) Welke open filmpunten van TODO.md §2 gaan mee in de run? (multi-select) | "T11 + H5 + S12 + S6", "C12 (tik tijdens heropenen)", "L-1 met hoofd in beeld", "Observaties (geluid)" — all four |
| (f) De `// TODO(verify)` in OpenControlNavHost.kt … nu of later? | "Nu meenemen (Recommended)" |
| (g) De teksten voor 1.1.1 (CHANGELOG, README-status, releasenotes) — akkoord? | "Akkoord zoals getoond (Recommended)" — with the texts in the preview |
| (h) De wijzigingen in RELEASING.md (taak 8a) — akkoord? | "Akkoord zoals getoond (Recommended)" — with the section and rewordings in the preview |

Because every `TODO.md` §2 item fits in, the run is split as the prompt allows: **film 1** (the regression, sections I–V) and **film 2** (the `TODO.md` §2
items, section VI), in the same folder, one sitting if it fits.

## Phase C — release preparation (tasks 7–9, 8a)

- **Task 7** `android/app/build.gradle.kts`: `versionCode = 10101`, `versionName = "1.1.1"`; the comment's list gains "1.1.1 → 10101" and the line naming
  `ai-sessions/0078`/`0079`.
- **(f)** `OpenControlNavHost.kt`: the `// TODO(verify)` for swipe, pull and back reworded to the evidence — pull `CAP-065`, system back `CAP-067`, the swipe
  `CAP-070` (`ARCHITECTURE.md` §2.4 states the same three); comment only, before the build commit, so the tested APK contains it.
- **Task 8** `CHANGELOG.md`: `[1.1.1] - not yet released` with the approved intro, the existing "Changed" lines (Build, CI) and the three Known issues of
  1.1.0 that still hold; a new empty `[Unreleased]`. `README.md`: "1.1.1 is being prepared …" in the Status section; "Current state": 71 registered captures, 3
  planned (registry count: 66 analyzed, 3 planned, 2 withdrawn). `scripts/release_notes.template`: "New in 1.1.1" and "Known issues in 1.1.1" (as for 1.1.0;
  E4 removes them after publishing).
- **Task 8a** `RELEASING.md`: a section **"Repository rules"** after the introduction (the four rules of §A.5 and what they mean for a release); A6 (CI green
  is your own check — no required status check), D1 (all three merge methods are allowed — choose the merge commit; a bot pull request may follow), E1 (on a
  branch, by pull request); the note above the flowchart; the flowchart's "Commit capture + analysis on main" → "on the release branch, merge the PR with a
  merge commit", "CHANGELOG date + README on main" → "in a small pull request (E)"; §6 "Pull requests merged to `main` while you test"; §11 step 1 "Fix in a
  normal session on its own branch, merged to `main` by pull request". Other documents: no direct-to-`main` instruction found (§A.5).
- **Task 9** gate (`clean`, scratchpad 0079/logs/gate1.log): exit 0; `:data` 1627/1627, `:domain` 37, `:hardware` 64/64, `:ui` 70/70; lint `:app` 1
  (`OldTargetApi`), `:ui` 1 (`ModifierParameter`); 0 compiler warnings — equal to Phase 0. `aapt2 dump badging` (build-tools 36.0.0) of the debug APK:
  `versionCode='10101' versionName='1.1.1' … compileSdkVersion='37'`.

## Phase D — the hardware-run skeleton and the test plan (tasks 10–12)

- `captures/CAP-071-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BG/CAP-071-EVENT-NOTES.md`: status, purpose, log metadata, preparation P0–P9 (★ = what `CAP-070`
  missed: P0's exit statuses, P1's update and the 1.1.0 choices set before it, the head in view, a film **with sound**, the screen recording, the system log
  from the start, no unfilmed rehearsal), the reference-byte table (§A.3), steps BG-1 … BG-24 in five sections (I update and start, II screens, III every
  write, IV robustness — film 1; V `TODO.md` §2 items — film 2), don'ts, after the run, and an analysis checklist (incl. the release APK's badging against
  the kept 1.1.0 copy, the system log's `SIGQUIT` sender, traceability per Test-ID).
- `id_registry.csv`: `CAP-071,capture,planned,…`. `CAPTURE_BLUETOOTH_HCI_SNOOP.md`: the Group BG section (after Group BF) and the Capture Index row
  (*planned*). No new Test-ID: every ID the steps name is registered (`grep -c "^<ID>," id_registry.csv` → 1 for each of the 26 checked).
- `APP_TESTPLAN.md`: the header line for the 1.1.1 build; **section U** (U1–U14, one row per risk of the 0078 change, each pointing at its BG step); the
  Summary row "U 1.1.1 build | 14".

## Phase E — documents

`TODO.md`: §2 — a "Planned in `CAP-071`" line and a "planned in `CAP-071` (BG-…)" note on the seven items it takes; the `OpenControlNavHost.kt` item deleted
(done, (f)); §4's restore item points at BG-13/BG-14; §5 "After the toolchain upgrade" (a) points at `CAP-071`; a new **M** item with the two repository
proposals (a required status check — after giving the workflows a job that always reports; merge commits only). `ai-sessions/INDEX.md`: the 0079 row.
`PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` (the skeleton's footer added) and `… scripts/lint_docs.py` → exit 0.

**Not changed:** `PROTOCOL.md`, `DECISIONS.md` (nothing to approve), any repository setting, any app code but the version and the comment.

## `pull_request_target` (the chat of 2026-10-08, before this session)

`grep -rn pull_request_target .github/` → no line, exit 1; positive control: `grep -rn "pull_request:" .github/workflows/` finds `.github/workflows/android.yml` and
`.github/workflows/lint-docs.yml`. GitHub's restriction from 2026-11-02 does not touch this repository; no change.

## Files read

In full in this conversation (`ai-sessions/0078`, 2026-10-07, the same session): `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `DECISIONS.md`
(ADR-001 … ADR-057), `TODO.md`, `RELEASING.md`; since then they changed only by 0078's own edits (`git diff --stat 42660fd origin/main` over the seven: 22 lines
in `ARCHITECTURE.md`, `DECISIONS.md`, `TODO.md`), re-read here. Read in this session: `PROTOCOL.md` §0–§2 (in 0078) and the §4.5 preamble; the 0079 prompt;
`captures/CAP-070-…/CAP-070-EVENT-NOTES.md` in full; `CAP-070-FINDINGS.md` §0–§5; `APP_TESTPLAN.md` in full; `CAPTURE_BLUETOOTH_HCI_SNOOP.md` the Group BF section
and the Capture Index row of `CAP-070`; `id_registry.csv` (capture rows, the Test-IDs used); `CHANGELOG.md` `[Unreleased]` and `[1.1.0]`; `README.md` Status and
"Current state"; `scripts/release.sh`, `scripts/release_notes.template` (and its 1.1.0 form, `git show 0323849:…`), `scripts/third_party_notices.py`;
`ai-sessions/0074` PROMPT Phases I–K and RESULT §J; `ai-sessions/0075` RESULT (summary). Not read in full: `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (only the IDs
checked in the registry), `ai-sessions/0074` RESULT §I, `ai-sessions/0078` RESULT (written in this conversation).

## Deferred documentation

Each is in `TODO.md`:

- §5 **M** — repository settings: a required status check (after an always-reporting job) and merge commits only (`ai-sessions/0079` §A.5).
- §2 — the seven items planned in `CAP-071`, removed by the session that analyses it; §4 — the Buds restored in BG-13/BG-14.
- §5 (a) — the toolchain upgrade's hardware test is `CAP-071`.

## Commits

On `release/1.1.1` (from `origin/main` = `7101d9c`); the maintainer answered *"Commit, push en PR (Recommended)"* in chat 2026-10-08. Not merged
(D1 comes after `CAP-071` and its analysis).

| Hash | Subject |
|---|---|
| `c106f4e` | refactor(ui): the swipe/pull/back TODO(verify) reworded to its evidence |
| `5dbde55` | chore(release): prepare 1.1.1 (versionCode 10101), not yet released |
| `e9740df` | docs: RELEASING.md — the repository rules of 2026-10-08 |
| `68d214d` | docs(capture): CAP-071 skeleton (Group BG) for the 1.1.1 release run, registered planned |
| `ab8c64b` | docs: TODO.md — items planned in CAP-071, repository proposals |
| (this commit) | docs(session): ai-sessions/0079 result, INDEX row |

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0079_MAINTENANCE_RESULT_2026_10_08.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0079_MAINTENANCE_RESULT_2026_10_08
