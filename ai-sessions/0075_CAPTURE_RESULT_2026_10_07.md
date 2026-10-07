# 0075_CAPTURE_RESULT_2026_10_07.md — Full analysis of CAP-070 (Group BF: the 1.1.0 release APK in a GrapheneOS profile without Google Play services)

**Number:** 0075
**Category:** CAPTURE
**Date:** 2026-10-07
**Title:** Full analysis of CAP-070 (Group BF: the 1.1.0 release APK, build `0323849`, in a GrapheneOS secondary user without Google Play — the hardware test of the five switches of 0074)
**Status:** complete (checkpoint approved in chat 2026-10-07; committed and pushed — see Commits; 1.1.0 released 2026-10-07)

## Progress

- **Done:** Phase 0 (git state, file identification, privacy + migration approved and executed), Phase A (film: 101 two-second sheets, every
  transition narrowed; clock offsets; no audio), Phase B (four HCI logs, four exports, two logcats), Phase C (EVENT-NOTES, FINDINGS, verdict),
  Phase D (checkpoint answered; approved edits applied; `ensure_footers.py` and `lint_docs.py` run — see "Documentation").
- **Next:** nothing in this session; the maintainer's release steps below.
- **Touched, unverified:** none — every number in the documents was re-derived in this session (no subagents used).
- **Intermediate results:** session scratchpad (the film notes, `sheets/`, `z/` zoom strips, `dec/` decodes — `*.all.txt`, `*.ctl.txt`, `*.hdlc.txt`,
  `*.ms.txt`, `*.pw.txt`, the write table — and `bin/` scripts `inv.py`, `join.py`, `writes.py`, `reads.py`, `ph.sh`). Re-create with the commands in
  `CAP-070-FINDINGS.md` if gone.

## Plain-language answers

- **Release verdict (`RELEASING.md` C4): release the tested build `0323849` as 1.1.0** (maintainer-approved in chat). No heavy defect: no crash or ANR; all 89
  setting writes were confirmed by the Buds and read back on the next connection; every one matched its expected bytes; no request outside the user's
  taps and the Connect read; nothing on the Google channels; every session end recovered.
- **The five switches:** Multipoint, Use head gestures, Earbuds replaced, Other alerts and Volume EQ each worked off and on, on **both** control channels; the
  Buds kept the values across a Disconnect/Connect (BF-8) and a Bluetooth off/on. Head gestures are written as 1/2, never 0.
- **The three forms never captured before** — Multipoint and head gestures on channel 19, Volume EQ "on" on channel 21 — and the channel-19 balance "Right 4" are
  now on the wire, **byte-identical** to the frames the unit tests derived, and acknowledged. The "three requests not yet seen" known issue is removed. The
  labelled test fixtures stay until a follow-up session swaps in these real bytes (the maintainer's choice: after the release).
- **"—":** seen on film in place of unread switches and EQ values (during a re-open and after the force-stop). What a **screen reader** says for it is still
  unchecked on a phone — there are no `uiautomator` dumps. New known-issue wording: "checked by the unit tests, not yet on a phone".
- **Lead L-1:** on channel 19 with both buds out of the case, taking the **Left** off made the Buds close the app's channel and come back on **21**, as
  predicted; the Right's five changes did nothing. Wear was not on film (head never in view), so the status stays 🟡 (dated Update, approved).
- **The install (P1):** the `dumpsys` output reads as a **first install of 1.1.0 in user 10**, not an update over 1.0.1 (🟡; AOSP prints one
  `firstInstallTime` per user in user-id order). The update path is still untested on hardware — a TODO.
- **Secondary user without Play:** no difference in the app's behaviour; the Message Stream was never contended (22 of 22 claims on the first try); 0 Play
  claims (positive control `CAP-066`).
- **What was not done in the run:** the early tap during a re-open (C12), *Read EQ again* (H5), the rotation during the save dialog (S12), the head in view, the
  screen recording, the `uiautomator` dumps; the film has **no sound**, so the case-sound and Volume-EQ observations are not recorded. An unfilmed rehearsal
  (05:44–05:59) is in the logs and was counted once.
- **What the app should change next:** nothing in its behaviour; the test fixtures (above). Everything else is test procedure (`TODO.md` §2).

## Checkpoint answers (chat 2026-10-07, `AskUserQuestion`, verbatim)

- Phase 0 "Privacy" → *"Commit as is (Recommended)"*; "Migration" → *"Proceed as planned (Recommended)"* — executed: folder
  `captures/CAP-070-2026-10-07_06-10-41_06-40-59-Group_BF/`, `CAP-070-recording.mp4`, mode 644, checksums equal before/after.
- "Verdict" → *"Release build 0323849 (Recommended)"*.
- "Known issues" → *"Approve draft (Recommended)"* — applied to `CHANGELOG.md` `[1.1.0]` and `README.md`; `scripts/release_notes.template` not edited (out of
  scope); the kept `release-notes.md` needs the maintainer's edit (below).
- "L-1" → *"Dated Update, no status change (Recommended)"* — applied: `PROTOCOL.md` §2.2a Update of 2026-10-07.
- "Docs" → *"Approve both (Recommended)"* — applied: `PROTOCOL.md` §4.5.2, §4.5.4, §4.5.6, §4.5.7 dated notes; `ARCHITECTURE.md` §5a (11, 15, 27, 28, 29
  hardware-verified in `CAP-070`).
- "Fixtures" → *"TODO for a FEATURE session after the release (Recommended)"* — `TODO.md` §4.
- "P1" → *"Record 🟡 + keep TODO (Recommended)"* — `CAP-070-FINDINGS.md` §0, `TODO.md` §2.

## Step mapping (summary; the full table is in `CAP-070-EVENT-NOTES.md`)

Done: P0, P1, P2, P5, P6, P7, BF-2, BF-3, BF-8, BF-12, BF-14, BF-15, BF-16, BF-17, BF-18, BF-20, BF-22, BF-25, BF-end. Done differently: BF-1, BF-4 … BF-7
(buds in the case), BF-9, BF-10 (channel 19), BF-11, BF-13 (tap after ready), BF-19, BF-21 (no *Read EQ again*), BF-23 (no rotation), BF-24 (no dumps).
Not done: P3, P4. Repeated: BF-1 (06:12:18), BF-14/15/16 (06:25:51–06:27:48), BF-23. `APP_TESTPLAN.md` T: ✅ T2, T3, T4, T6, T8, T9, T10; ⚠️ T1, T5, T7,
T11; C10 ✅; C12, H5, S12 not run.

## Phase B tables (pointers)

The twelve reads per connection: `CAP-070-FINDINGS.md` §2. Every write vs the expected-frames table: §3. The channel per announcement: §4. Session ends and
processes: §6. Battery vs the wire: §7. Message Stream claims and the Play-services negative: §1.

## External sources

- AOSP `services/core/java/com/android/server/pm/Settings.java` (refs/heads/main, fetched 2026-10-07 via `?format=TEXT | base64 -d`):
  `pw.print(prefix); pw.print("  lastUpdateTime=");` (once per package) and, inside `for (UserInfo user : users)`, `pw.print("  User "); pw.print(user.id);`
  … `pw.print("      firstInstallTime=");`; `UserManagerService.getUsersInternal` iterates `mUsers.valueAt(i)` (a `SparseArray`).
- grapheneos.org/usage (fetched 2026-10-07): *"GrapheneOS has a compatibility layer providing the option to install and use the official releases of Google
  Play in the standard app sandbox."*; *"Since the Google Play apps are simply regular apps on GrapheneOS, you install them within a specific user or work
  profile and they're only available within that profile."*
- github.com/GrapheneOS/platform_packages_apps_GmsCompat, branch `17` (raw files, fetched 2026-10-07): `AndroidManifest.xml` `package="app.grapheneos.gmscompat"`;
  `lib/AndroidManifest.xml` `package="app.grapheneos.gmscompat.lib"`; `config-holder/app/build.gradle.kts` `namespace = "app.grapheneos.gmscompat.config"`.

## Next steps for the maintainer (`RELEASING.md` D1–E4 — instructions, not executed)

1. Before publishing: edit the kept `~/opencontrol-1.1.0-tested/release-notes.md` (and `dist/1.1.0/release-notes.md` if you use it): remove "three of the new
   requests … had not been seen on the Bluetooth log when this release was prepared — they are built like the captured ones and the release test records them;"
   and add "the screen-reader text for "—" is checked by the unit tests, not yet on a phone." The APK and its SHA-256 are unchanged.
2. D1: `gh pr merge 7 --merge` (a merge commit — not squash, not rebase).
3. D2: `git tag -s v1.1.0 0323849 -m "OpenControl for Pixel Buds Pro 2 1.1.0" && git push origin v1.1.0`; check `git branch -r --contains v1.1.0` lists
   `origin/main`.
4. D3: `gh release create v1.1.0 --verify-tag --draft --title "OpenControl for Pixel Buds Pro 2 1.1.0" --notes-file ~/opencontrol-1.1.0-tested/release-notes.md
   ~/opencontrol-1.1.0-tested/*`; read the draft; `gh release edit v1.1.0 --draft=false`.
5. D4: download the APK on the phone, compare SHA-256 `0bc82f756d024667842f2de6cdacab585731481d1e4c428bf12fd4b438abc13b`, install over the tested one.
6. E1–E4 in a small pull request: the date in `CHANGELOG.md` `[1.1.0]` and an empty `[Unreleased]`; `README.md` "Latest release"; `RELEASING.md` §13 Release log
   row (build `0323849`, APK SHA-256 above, certificate `a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb`, capture `CAP-070`); this RESULT's and
   `ai-sessions/0074`'s Status; `scripts/release_notes.template` reset.

## Documentation

The prompt's one full path to the placeholder skeleton was split (folder → file name) so that `lint_docs.py` passes after the folder move; no other
change to the prompt.

`CAP-070-EVENT-NOTES.md` (rewritten, the skeleton as an unchanged appendix), `CAP-070-FINDINGS.md` (new); `PROTOCOL.md` (§2.2a Update, four §4.5 notes, history
row), `ARCHITECTURE.md` (§5a; §2.4's swipe sentence — the swipe is now on film), `PROJECT.md` (Status after 1.0.x), `CHANGELOG.md` (`[1.1.0]` intro and
Known issues; not its date), `README.md` (1.1.0 status, capture counts), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BF heading and run note, Capture Index row,
folder path), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (11 evidence pointers), `APP_TESTPLAN.md` (T results, Summary, run note), `id_registry.csv` (`CAP-070` analyzed),
`TODO.md` (§2 rewritten, §4 fixtures follow-up), `ai-sessions/INDEX.md` (0075 row), `ai-sessions/0074` RESULT (Commits back-filled: `0323849`). Nothing under
`android/`, `dist/` or `scripts/` changed.

## Files read

In full: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `AI_SESSION_LOG_PROCEDURE.md`, the prompt, the skeleton (committed and working tree),
`ai-sessions/0074` RESULT, `CAP-068-FINDINGS.md`, the four exports' non-hex lines (the hex lines through the decoders) and both logcats (substantive lines; the
repetitive `input_focus`/`InsetsController`/`ImeTracker`/`viewroot_draw_event` lines were tallied by PID, not read one by one), every Buds packet of the three
distinct HCI logs through the decoders. In part: `PROTOCOL.md` (§2.2a L-1 Updates, §4.3 Option F, §4.5 section heads and ends, the history table),
`ARCHITECTURE.md` (§2.4, §5a), `TODO.md` (§1–§4), `CHANGELOG.md` (`[1.1.0]`), `README.md` (status, current state), `RELEASING.md` (Release checklist),
`APP_TESTPLAN.md` (sections C, H, S, T, Summary), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BF, §9 rows 068–070), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the rows named),
`CAP-049-EVENT-NOTES.md` (header and timeline layout), `ai-sessions/INDEX.md` (tail), `id_registry.csv` (CAP rows), the Kotlin sources `MainActivity.kt`
(`:155–175`, `:232–250`, `:318–330`), `DeviceStatus.kt` (`:20–50`), `SessionReopener.kt` (`:80–140`), `RfcommBudsTransport.kt` (retry constants),
`SettingFrame.kt`/`SettingsCodecTest.kt`/`SettingsFixtures.kt` (the `CAP-070` references only). **Not read, against the prompt's reading list:** `DECISIONS.md` in
full (ADRs used through `PROTOCOL.md`/`ARCHITECTURE.md` citations), `PROTOCOL.md` and `ARCHITECTURE.md` in full, the `0074` PROMPT, `CAP-068-EVENT-NOTES.md`,
`CAP-049-FINDINGS.md`, the `CAP-069`/`058`/`024`/`022`/`065`/`066` FINDINGS sections, and the remaining Kotlin files of the list (`Maestro.kt`, `CodecRouter.kt`,
`PwRpc.kt`, `BudsRepositoryImpl.kt`, `SessionDiagnostics.kt`, `BudsSettings.kt`, `ValueCurrency.kt`, `SettingsUi.kt`, `ControlsScreen.kt`, `EqScreen.kt`,
`ConnectionScreen.kt`, `SettingsMenu.kt`, `AppUiSession.kt`, the test classes, `build.gradle.kts`) — no finding of this session depends on their internals
beyond what is cited; a later session that changes them reads them.

## Deferred documentation

Each item is also in `TODO.md`, in the same words where it is a task:

- Replace the labelled structural fixtures with the real `CAP-070` frames and remove ADR-055's `// TODO(verify)` — a FEATURE session after the release. (`TODO.md` §4)
- The screen-reader text on the phone (two `uiautomator` dumps); C12; H5, S6 on a screen recording, S12; the update path with `grep -A12 "User 10:"`; L-1
  with the head in view; the case-sound and Volume-EQ observations with audio; the cause of the two `SIGQUIT`s. (`TODO.md` §2)
- `CAP-068-FINDINGS.md` §0: the "Permissions (start)" line is no evidence of a fresh install (`CAP-070-FINDINGS.md` §0) — rewrite that sentence when the
  findings file is next revised. (This RESULT only — no task in `TODO.md`.)
- Read in full, against the prompt's list: see "Files read". (This RESULT only.)

## Commits

On `feature/0074-settings-switches` (pull request #7), after the build commit `0323849`; the maintainer answered *"Commit + push, 3 commits
(Recommended)"* in chat 2026-10-07. `git diff 0323849..HEAD -- android dist scripts` is empty.

| Hash | Subject |
|---|---|
| `4bcc409` | docs(capture): CAP-070 (Group BF) — the 1.1.0 release run, analysed |
| `0f142e8` | docs: follow CAP-070 — known issues, L-1 update, hardware-verified switches |
| `714694b` | docs(session): ai-sessions/0075 prompt and result, INDEX row, 0074 back-fill |
| (this commit) | docs(session): ai-sessions/0075 commits back-filled, status complete |

The first push uploaded all LFS objects (1.4 GB) but the SSH connection closed before the refs; the second push moved the branch `0323849..714694b`.

## Release (after the session's commits, the maintainer's request in chat 2026-10-07: *"Voer alle commandos uit"*)

- D1: `gh pr merge 7 --merge` → merge commit `b1e4db6` (CI `build` and `lint-docs` green on `8256c74` first).
- D2: `git tag -s v1.1.0 0323849` (SSH signature), pushed; `git branch -r --contains v1.1.0` lists `origin/main`.
- B3 had been skipped: `~/opencontrol-1.1.0-tested` was made from `dist/1.1.0` (`cp -a`) and its `release-notes.md` updated (Known issues); the maintainer
  edited it once more (16:58). D3: the draft created with `gh release create … --draft`; checked in a fresh `mktemp -d` directory (APK `sha256sum -c` OK,
  notes byte-equal to the kept copy). Publishing through `gh release edit --draft=false` and `gh api -X PATCH … -F draft=false` failed with HTTP 500 four
  times (last request id `D0BC:3AB80A:2518F24:24EC1C4:6AC660D7`; githubstatus.com "All Systems Operational"); the maintainer published it on github.com —
  2026-10-07 15:20:14 UTC, "Latest"; the published assets re-checked (APK SHA-256 OK, notes and notices equal to the kept copy).
- E1–E4: branch `release/1.1.0-after-publish` (CHANGELOG date and link, README latest release and status, RELEASING §13 row, template reset, these
  Status lines). D4: the maintainer installed it from the release page, SHA-256 checked, works (chat 2026-10-07).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0075_CAPTURE_RESULT_2026_10_07.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0075_CAPTURE_RESULT_2026_10_07
