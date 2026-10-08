# 0079_MAINTENANCE_PROMPT_2026_10_08.md — Prepare release 1.1.1 (the toolchain upgrade of `ai-sessions/0078`) and the skeleton of its hardware run (the next capture, Group BG)

**Number:** 0079
**Category:** MAINTENANCE
**Date:** 2026-10-08
**Title:** Prepare the next release — the toolchain and dependency upgrade of `ai-sessions/0078` shipped as a version of its own — up to the point
where the maintainer builds it with `scripts/release.sh`: version, `CHANGELOG.md`, `README.md`, release notes, the release checklist on a pull request;
write the skeleton of its hardware run (the next capture number, Group BG) — a regression run of the release-signed APK installed **over 1.1.0** in the GrapheneOS user
without Google Play, aimed at what the new toolchain could have changed, plus the open film items of `TODO.md` §2; no tag, no build by the agent, no
publication

---

## 0. How to use this prompt

You are an expert Android/Kotlin release engineer and technical auditor working in this repository. Run the phases of §4 **strictly in order**; record each
phase before starting the next. The analysis of the run itself is a **later CAPTURE session** (its own prompt, written after the run) — not this one.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Before any other action read **in full**, in this order:
`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md` (§0–§2 and the §4.5 preamble only — this session changes no protocol
behaviour; say so in the RESULT), `DECISIONS.md` (every ADR, in particular ADR-029 with its Update of 2026-10-07, ADR-042, ADR-044, ADR-048, ADR-051,
ADR-056, ADR-057), `TODO.md`. Then:

- `RELEASING.md` — **in full** (the release checklist, the flow and its rules, §4–§8, §11, §13 the Release log).
- `ai-sessions/0078_MAINTENANCE_RESULT_2026_10_07.md` — **in full**: what changed in the build, the dependency/manifest/APK audit, what it defers
  (`TODO.md` §5 "After the toolchain upgrade"), and its summary of what the next release run must look at.
- `ai-sessions/0074_FEATURE_PROMPT_2026_10_06.md` §4 Phases I–K and `ai-sessions/0074_FEATURE_RESULT_2026_10_06.md` §I/§J — how 1.1.0 was prepared and how
  its run skeleton was laid out; `ai-sessions/0075_CAPTURE_RESULT_2026_10_07.md` — how that run went (the release verdict, what was not done).
- `captures/CAP-070-*/CAP-070-EVENT-NOTES.md` — **in full** (the layout to follow: metadata, preparation P0–P7, steps with expected screen and HCI
  bracket, don'ts, after the run, analysis checklist) and `CAP-070-FINDINGS.md` §0–§5 (the request bytes of 1.1.0 — the reference for this run).
- `APP_TESTPLAN.md` — in full; `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 (the Capture Index) and the Group BF section; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §0
  and the Test-IDs the steps will name; `id_registry.csv` (the next free numbers); `CHANGELOG.md` (`[Unreleased]` and `[1.1.0]`); `README.md` (status,
  "Latest release", known issues, "Building from source"); `scripts/release.sh`, `scripts/release_notes.template`, `scripts/third_party_notices.py`.
- `android/app/build.gradle.kts` (versionName/versionCode) — in full before changing it.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5).** Create **ai-sessions/0079_MAINTENANCE_RESULT_2026_10_08.md** at the very start
with `**Status:** partial — resumed` and a **Progress** block; update it at the end of every phase and after every substantial step (what is done, what is
next, which files are touched but unverified, where intermediate results live — the session scratchpad). A resumed session reads this prompt, then the
Progress block, re-reads the files the unfinished step touched, and continues; it never redoes a recorded step and never assumes an unrecorded step was
done.

**Memory and the machine (`ai-sessions/0057`, `0074`, `0078`).** Gradle with `--max-workers=2`; never two Gradle builds at once; `./gradlew --stop` when
done. Never `git worktree add` (it checks out `captures/`, gigabytes of LFS — `scripts/release.sh` sets `GIT_LFS_SKIP_SMUDGE`, but the agent does not run
it). Never delete with a wildcard; temporary output goes into a fresh `mktemp -d` directory in the scratchpad.

**Nothing is taken on trust**, including this prompt and the 0078 RESULT: every version, count, byte and file name used here is re-derived.

---

## 1. What the maintainer asked for (chat, 2026-10-08, translated from Dutch)

> Make a prompt for the next release run.

Context from the same chat: `ai-sessions/0078` (Gradle 9.7.1, AGP 9.3.3 with built-in Kotlin, Kotlin 2.4.20, Hilt 2.60.1, Compose BOM 2026.09.00,
compileSdk 37, targetSdk/minSdk 34) was merged as PR #11 (`188d19e`) with CI green; its RESULT says it is **not hardware-tested** and "ships with the next
release, whose capture run should include a quick pass over every tab, the settings menu (its tab colours/indicator unchanged), pull-to-refresh, the
licence dialog and a connect/ANC/EQ round". The same chat checked GitHub's notice on `pull_request_target` (enforced 2026-11-02): no workflow of this
repository uses it (`grep -rn pull_request_target .github/` → exit 1; positive control `pull_request:` found in `.github/workflows/android.yml` and `.github/workflows/lint-docs.yml`) —
nothing to do; record it in the RESULT, no change.

---

## 2. What the release is, and what the run must prove (re-check every item in Phase A)

**The release.** Everything merged to `main` since `v1.1.0` (`0323849`): the 0076 test fixtures, the 0077 documents, the 0078 toolchain upgrade — no new
feature, no new byte on the wire, no permission or component change (0078 RESULT, tasks 11–12). By `RELEASING.md` §4 and semantic versioning that is a
patch: **1.1.1, versionCode 10101** — confirm at the checkpoint (a minor 1.2.0 = 10200 is the alternative if the maintainer counts the toolchain as more
than a fix). Not a hotfix in `RELEASING.md` §11's sense: the whole app is recompiled by a new compiler against new libraries, so the run is a **broad
regression run**, not the reduced hotfix set.

**What the new toolchain could have changed, and the step that checks it** (derive the list again from the 0078 RESULT's added/changed libraries):

| Risk (0078 change) | What to watch in the run |
|---|---|
| Update path: the 1.1.1 APK over 1.1.0 (same key); DataStore 1.1.1 → 1.2.1 reading the file 1.1.0 wrote | Before the update set **dark mode On** and **Debug mode on** in 1.1.0; after it both are still set; `dumpsys package` shows `lastUpdateTime` > `firstInstallTime` and `versionCode=10101` (also the `TODO.md` §2 update-path item) |
| Compose 1.7 → 1.12, material3 1.3 → 1.4 (rendering, the pager, `TopAppBar`, `NavigationBar`, `PullToRefreshBox`, dialogs) | every tab opened by tap **and** by swipe; a pull on every tab; each (i) dialog; the licence dialog; layout identical to `CAP-070`'s film (same screen, same texts) |
| `TabRow` kept with `@Suppress("DEPRECATION")` | the settings menu: Settings / Debug / Info, Primary label colour and a full-width indicator as in 1.1.0 |
| activity 1.13 / `navigationevent` 1.1 (back dispatch); targetSdk 34 (no predictive back) | system back from the settings menu returns to the tab it was opened from; back from a tab leaves the app as before |
| savedstate 1.5, lifecycle 2.11 (configuration changes) | a rotation and Android's dark switch keep the tab and the settings menu (`APP_TESTPLAN.md` R, F-1) |
| `LocalResources` for the licence; `toUri()` for the links | Info → Read the licence (full text), both links open the browser |
| Hilt 2.60.1, coroutines 1.11 (the tile service, the foreground service, the automatic re-open) | ANC from the Quick Settings tile; the notification while connected and gone after Disconnect; a Buds-side close re-opened by itself (ADR-044) |
| The codec is unchanged | every request on the wire **byte-identical** to the same request in `CAP-070` (Connect read of twelve fields, one write per switch kind, an EQ preset and a slider, balance, ANC `Get`/`Set`, Ring/Stop, `SubscribeRuntimeInfo`) — a difference is a finding |
| D8 global synthetics (161 code-free `android.*` stubs), two optional `<uses-library>` (androidx.window) | the app installs and starts on the Pixel 9a (Android 17); no crash in the logcat; the analysis compares the release APK's badging with 1.1.0's |

**Open film items of `TODO.md` §2 to fit in** (propose which; each with its expected screen or bytes): the screen-reader text with two `uiautomator`
dumps (T11); C12 (a tap during an automatic re-open — needs the screen recording); H5 with *Read EQ again*; S6 and S12 on the screen recording; lead L-1
(the Left out with both worn on channel 19, head in view, `INEAR-005`); the case-sound and Volume-EQ observations (a film with an audio track, or the
maintainer says what is heard); a **system log** kept for the `SIGQUIT` question. Not in this run unless chosen: B4 (needs a re-pair), K5/BC-12 (the
Owner user), F-4 (a debug build), a second device.

**Preparation item P7 (`TODO.md` §4, **M**):** restore the Buds first — Sound → BALANCED (`4:{16:[-3.5, 0.5, 1.0, -1.0, 2.5]}`), Controls → Press and
hold → untick Off (`4:{12:{1:1 2:0 3:1 4:0}}`), Disconnect + Connect, "read" shown.

---

## 3. Rules (binding, in addition to `AGENTS.md`)

- **No app change.** This session changes only `versionName`/`versionCode` in `android/app/build.gradle.kts`, documents, the run skeleton and the
  registries. Any app defect found while preparing is reported to the maintainer (a question), not fixed here. Exception, only if chosen at the
  checkpoint: the comment-only rewording of `OpenControlNavHost.kt`'s `// TODO(verify)` for swipe, pull and back (`TODO.md` §2, the maintainer's choice
  of 2026-10-07 "in the next session that touches `:ui`") — before the build commit, so the tested APK contains it.
- **Release discipline (`RELEASING.md`).** On a branch (`release/1.1.1`, or the name chosen), a pull request with the checklist; the agent prepares A1–A6
  and C/E texts, and **never** runs `scripts/release.sh`, creates or pushes a tag, creates a GitHub release, uploads a file or merges — those are the
  maintainer's own acts (B, C1–C2, D). It prints the commands of §5–§7 for the maintainer and runs none.
- **The `AGENTS.md` §6 gate.** No FACT promotion, ADR or ADR Update without the maintainer's approval in this chat; none is expected.
- **Evidence.** Every claim with its command, exit status and output; a negative with a positive control (`AGENTS.md` §13 step 8); external facts with
  URL and quoted line; zero creativity with hex — the expected bytes in the skeleton are copied from `CAP-070` frames or the test fixtures, each with its
  source frame.
- **Registries.** The capture is registered *planned* in `id_registry.csv` with the next free numbers (check before assigning; `CAP-NNN` and Group BG are
  expected — confirm), its Group section and Capture Index row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, and new Test-IDs only where a step has none
  (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, registered).
- **Scope.** §1–§2 and `RELEASING.md` A/E. Anything else is a RESULT note or a `TODO.md` line, never a silent addition.
- **Files.** Look before overwriting; never delete with a wildcard; no `git worktree add`.
- **Commits.** Only after the maintainer confirms the final summary.

---

## 4. Tasks

### Phase 0 — set-up
1. Create the RESULT (§0). Record `git log -1`, `git branch --show-current`, `git status --short`, `git fetch && git log --oneline HEAD..origin/main`,
   `git log --oneline v1.1.0..origin/main` (what the release contains). Branch (name per §3) from an up-to-date `origin/main`.
2. **Gate on the branch tip before any change:** `cd android && ./gradlew --offline --max-workers=2 clean assembleDebug testDebugUnitTest test lint` →
   exit code; per-module counts from the JUnit XML (expected: equal to the 0078 final gate — `:data` 1627/1627, `:domain` 37, `:hardware` 64/64, `:ui`
   70/70); lint per module (expected: `:app` `OldTargetApi`, `:ui` `ModifierParameter`); compiler warnings (Kotlin 2.4 prints them as "Problem found:
   Kotlin compiler: …" — count those and `w:` lines). A difference from 0078's numbers is reported before anything else.

### Phase A — inventory (no change)
3. What the release contains: `git log --oneline v1.1.0..origin/main` grouped by session; the user-visible effect of each (expected: none); the
   `[Unreleased]` block of `CHANGELOG.md` against it.
4. The release machinery with the new toolchain, read-only: `scripts/release.sh` picks `ls -d $sdk/build-tools/* | sort -V | tail -1` — which folder is
   that now and does it hold `apksigner` and `aapt2` (expected `36.0.0`)? Does `./gradlew --offline clean assembleRelease` need anything the machine lacks
   (the platform `android-37.0`, the Gradle 9.7.1 distribution, the Robolectric jar are already cached)? Does `third_party_notices.py --fetch` still parse
   the tree (0078: 136 libraries, exit 0)? Say what was checked and how — **without** running the release build itself (it needs the signing key).
5. The run's reference bytes: for every request kind of §2's codec row, the `CAP-070` frame (file, frame number, hex) or the test fixture
   (`Settings070`, `Cap0xxFixtures`) it must equal — one table. Re-derive each from the log with its `tshark` / `scripts/pwrpc_decode.py` command.
6. The `TODO.md` §2 items: per item, whether it fits this run (one sitting, ≈ 30–40 min of steps like `CAP-070`), its expected screen or bytes, and what
   it needs (screen recording, audio track, system log, the Owner user, a re-pair).

### Phase B — checkpoint (stop and ask, in this chat, `AskUserQuestion`)
Before the first question post a short plain-language overview. One decision per question, each option with pros and cons, one "(Recommended)", the
exact text or version in the preview:
- **(a) The version:** 1.1.1 / 10101 (Recommended — no user-visible change) or 1.2.0 / 10200.
- **(b) The branch:** `release/1.1.1` (Recommended, `RELEASING.md` A1) or this session's own name.
- **(c) The run's scope:** the full regression set of §2 (Recommended) or the reduced hotfix set of `RELEASING.md` §11 plus the §2 risk table.
- **(d) Which `TODO.md` §2 items go in** (multi-select), with what each needs.
- **(e) Recording:** camera film with an audio track + Android's screen recording + a system log (Recommended) — or which of them.
- **(f) The `// TODO(verify)` rewording** in `OpenControlNavHost.kt` now (comment only, before the build commit) or later.
- **(g) The texts:** the `CHANGELOG.md` `[1.1.1]` block, its Known issues, and the `README.md` status line — shown in full in the preview.
Record the answers verbatim.

### Phase C — release preparation (`RELEASING.md` A1–A5)
7. `android/app/build.gradle.kts`: `versionName` and `versionCode` as chosen (the comment's version list extended). Nothing else in the app.
8. `CHANGELOG.md`: `[Unreleased]` → the `[X.Y.Z] - not yet released` block in the users' words (what changes for them: nothing visible; why the release:
   current build tools and libraries; what was proved: tests, mutations, APK audit — and that the hardware run follows), with **Known issues** carried from
   1.1.0 that still hold; a new empty `[Unreleased]`. `README.md`: status line and known issues (not "Latest release" — that is E1, after publishing).
   `scripts/release_notes.template`: the summary line for this version if the template carries one (`RELEASING.md` A3/E4 — check how 1.1.0 did it).
9. Gate again (task 2's command, `clean`): equal counts, nothing new; the debug APK's badging shows the new `versionCode`/`versionName`;
   `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py` and `… scripts/ensure_footers.py` exit 0.

### Phase D — the hardware-run skeleton `CAP-NNN` (Group BG) and the test plan
10. Create captures/CAP-NNN-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BG/CAP-NNN-EVENT-NOTES.md (the folder is renamed with the real times after the run, as
    before) in `CAP-070`'s planned-procedure layout: log metadata (build commit = "the hash `scripts/release.sh` prints", versionCode, phone, user),
    preparation P0–P7 with the lessons of `CAP-063`…`CAP-070` (P1 = the update over 1.1.0 with dark mode On and Debug mode on set **before** it; P7 = the
    Buds restored, §2), steps grouped by the §2 risk table, each with its action, the expected screen and the expected HCI bracket (with the reference
    frame of task 5), the chosen `TODO.md` §2 items, don'ts, "after the run" (what to pull, `sha256sum *`), and an analysis checklist with Test-IDs and
    "Refuted if" lines — including: zero Play-services claims with its positive control; every request byte-identical to its `CAP-070` reference; the
    release APK's `aapt2 dump badging` against the kept 1.1.0 copy (`~/opencontrol-1.1.0-tested`: permissions, components, `targetSdkVersion` 34, the two
    `uses-library-not-required`); the logcat checked for crashes and `StrictMode` (none expected in a release build).
11. Register `CAP-NNN` *planned* (`id_registry.csv`), the Group BG section and Capture Index row (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`), new Test-IDs if any
    (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, registered).
12. `APP_TESTPLAN.md`: a section **U. The 1.1.1 build (the toolchain upgrade of `ai-sessions/0078`)** after T — one line per §2 risk, each pointing at its
    `CAP-NNN` step.

### Phase E — the pull request and the hand-over
13. `TODO.md`: §2 items taken into `CAP-NNN` marked "planned in `CAP-NNN`" (removed only by the analysing session); §5's "(a) it is not hardware-tested"
    pointed at `CAP-NNN`; anything deferred added. `ai-sessions/INDEX.md` (the 0079 row).
14. Finish the RESULT: plain-language summary first (what the release is, what the maintainer does next, step by step: B1–B3, C1–C2 with the skeleton,
    then a CAPTURE session for the analysis); the inventory, the reference-byte table, the checkpoint answers verbatim, the gate table, the
    `pull_request_target` check of §1, "Files read", **"Deferred documentation"** (each also in `TODO.md`) and **"Commits"**. Status per
    `AI_SESSION_LOG_PROCEDURE.md` §4/§4b.
15. Show the maintainer a short summary and **ask whether to commit, push and open the pull request**. Only after a yes: Conventional Commits, one per
    concern (`chore(release): 1.1.1 version` with `CHANGELOG`/`README`/template, `docs: CAP-NNN skeleton, registry, test plan`, `docs(session): …`), each
    with a *why* and the attribution line from the session's system reminder; `git fetch` and rebase before pushing, never force; nothing from `build/`,
    `dist/`, `android/.kotlin/`, `.vscode/` or `__pycache__` staged. The pull request's description is `RELEASING.md`'s checklist with A1–A6 ticked as
    done and B–E open, and a link to this RESULT. **Do not merge** — the merge (D1) comes after the run and its analysis. Then print, for the maintainer,
    the exact commands of B1–B3 and C1 (`scripts/release.sh X.Y.Z`, the fingerprint check, `cp -a dist/X.Y.Z ~/opencontrol-X.Y.Z-tested`,
    `adb install --user <id> -r …`), and run none of them.

---

## 5. Guardrails (summary)

- The app's code does not change in this session (only its version; the optional comment of (f)). No byte on the wire changes.
- No `scripts/release.sh`, no tag, no GitHub release, no merge, no upload — the maintainer's acts. The release verdict belongs to the session that
  analyses `CAP-NNN`.
- Every expected byte in the skeleton comes from a named `CAP-070` frame or a fixture; every negative has its positive control.
- Approvals, texts and the version only from this chat; one Gradle build at a time, `--max-workers=2`; no worktrees; no wildcard deletes.

---

## 6. Deliverables

- **ai-sessions/0079_MAINTENANCE_RESULT_2026_10_08.md** — Progress block, summary, inventory, reference-byte table, checkpoint answers, gate tables,
  "Files read", "Deferred documentation", "Commits".
- The release branch with the version change, `CHANGELOG.md`/`README.md`/template, the `CAP-NNN` skeleton and its registrations, `APP_TESTPLAN.md` U —
  pushed, with an open pull request carrying the release checklist.
- The maintainer's next commands, printed, not run.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0079_MAINTENANCE_PROMPT_2026_10_08.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0079_MAINTENANCE_PROMPT_2026_10_08
