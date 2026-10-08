# 0080_CAPTURE_RESULT_2026_10_08.md — Full analysis of CAP-071 (Group BG: the 1.1.1 release APK installed over 1.1.0 in the GrapheneOS profile without Google Play services) and the 1.1.1 release verdict

**Number:** 0080
**Category:** CAPTURE
**Date:** 2026-10-08
**Title:** Fully analyse `CAP-071` (one film with sound, four HCI snoop logs, two app debug exports, one app logcat, one full system log), record the real events and the findings, and give the release verdict for 1.1.1 (`RELEASING.md` C4); no app change, no new skeleton, no publishing
**Status:** complete (every checkpoint answer given in chat 2026-10-08: release `86a6fb3` as 1.1.1; committed and pushed — see Commits; 1.1.1 published 2026-10-08)

## Progress

- **Done:** all phases (0, A, B, C, D up to the commit question). Reading; Phase 0 (state, inventory, privacy, migration — executed as approved); Phase A
  (the film at 2 s and every change at 1 s, the audio, the clock offsets, the new EVENT-NOTES); Phase B (four HCI logs, both exports, the app logcat, the system
  log); Phase C (`CAP-071-FINDINGS.md`); Phase D checkpoint (answers below) and the approved documentation edits; footers added.
- **Next:** the maintainer's release steps D1–E4 (Summary). Commits back-filled by the next session.
- **Touched, unverified:** none — every edited file was re-read by `grep`/`git diff` after the edit.
- **Intermediate results:** the session scratchpad
  (`/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/736fc242-0154-4189-90fd-f6ac85f8e6aa/scratchpad/`): the decoder script ana (Python), the
  decoded HCI tables (folder hci), the film frames, sheets, zooms, notes and clock strips (folder film), the audio track, spectrograms and notes (folder audio), the
  pulled APK (folder pulled), the AOSP sources fetched (folder aosp), the checksum listings before and after the move, the system-log range and extract
  candidates, the Phase-B notes.

## Summary (plain language)

**The verdict:** release the tested build **`86a6fb3` as 1.1.1** — approved by the maintainer in chat (below). Nothing heavy went wrong.

**What works on 1.1.1:** everything that was tried. Installed over 1.1.0 it kept its data, dark mode and Debug mode, and started by itself after the update. The
APK on the phone is exactly the built one (pulled back: SHA-256 `062f35b3…f1d0`, equal to `dist/1.1.1/`). All 11 sessions read the twelve settings in the
right order; all 24 setting and EQ writes, the 9 ANC changes (from the tab and the tile, during music) and the 4 Ring/Stop commands were byte-identical to the
references and were confirmed by the Buds; after Disconnect/Connect every written value came back. Every session loss — the Buds closing the channel, both buds
into the case, the user's Disconnect, Bluetooth off/on — was explained and recovered; screens, tabs, swipes, pulls, dialogs, rotation, dark theme, licence and links
behaved as in 1.1.0.

**What did not happen:** the second film (lead L-1 with the head in view, the early tap C12, the export across a rotation S12, the screen-reader dumps T11), the
screen recording, *Read EQ again*, and spoken observations. The audio track recorded the Find ring, but no case chime either way.

**Did anything change compared with 1.1.0?** No. The same requests, bytes, answers, timings and screens; the only APK differences are metadata the toolchain
upgrade already recorded (compile SDK 37, two optional-library lines).

**What differs in a secondary user:** nothing in the app's behaviour. The app also lives in the Owner user (its tile process there), which the update stopped;
the tile used in the test was the user-10 one.

**What the app should change next:** nothing was found that needs a code change. The next app run should repeat film 2 with a screen recording, the lid open
before the app starts, no app use before the film, and Do Not Disturb on.

**Your next steps (`RELEASING.md` Release checklist — instructions only, nothing was executed):**
1. B3 is done: `~/opencontrol-1.1.1-tested` exists and equals `dist/1.1.1/` (`sha256sum` of both folders identical; checked 2026-10-08). Before D3 run
   `cd ~/opencontrol-1.1.1-tested && sha256sum -c opencontrol-pixelbudspro2-1.1.1.apk.sha256` once more.
2. After this session's commits are pushed: **D1** `gh pr merge 15 --merge` (a merge commit, not squash/rebase); merge a bot pull request the same way if one appears.
3. **D2** `git tag -s v1.1.1 86a6fb3 -m "OpenControl for Pixel Buds Pro 2 1.1.1"`, `git push origin v1.1.1`, then `git branch -r --contains v1.1.1` must list
   `origin/main`.
4. **D3** `gh release create v1.1.1 --verify-tag --draft --title "OpenControl for Pixel Buds Pro 2 1.1.1" --notes-file ~/opencontrol-1.1.1-tested/release-notes.md
   ~/opencontrol-1.1.1-tested/opencontrol-pixelbudspro2-1.1.1.apk ~/opencontrol-1.1.1-tested/opencontrol-pixelbudspro2-1.1.1.apk.sha256
   ~/opencontrol-1.1.1-tested/THIRD_PARTY_NOTICES.txt`; read the draft; `gh release edit v1.1.1 --draft=false`.
5. **D4** install the APK from the release page over the tested one and compare its SHA-256.
6. **E1–E4** in a small pull request: the `CHANGELOG.md` date and a new `[Unreleased]`; `README.md` "Latest release" and status; the `RELEASING.md` Release log row
   (build `86a6fb3`, APK `062f35b3b2d177ed7dbccd336742dffce59b194dccf6c4febcb7dc6def98f1d0`, certificate `a7530f5c…c79d8dcb`, capture `CAP-071`); this RESULT's
   Commits and Status; `scripts/release_notes.template` without the 1.1.1 lines.

## Phase 0 — session start (2026-10-08)

- `git log -1`: `63a56a952da47363a663e95324976573a664046b` "docs(session): ai-sessions/0080 prompt — …" (2026-10-08 19:02:26 +0200).
- `git branch --show-current`: `release/1.1.1`.
- `git status --short`: ` M` the notes; `??` the nine data files of `CAP-071` (as the prompt's §2 says).
- `git fetch`; `git log --oneline HEAD..origin/main` → empty; `HEAD..origin/release/1.1.1` → empty.
- `gh pr view 15 --json state,headRefOid` → `OPEN`, `63a56a952da47363a663e95324976573a664046b`.
- **`86a6fb3` is not the tip** of `origin/release/1.1.1`: one later commit, `63a56a9` (this session's prompt, documentation only). `git diff --stat
  86a6fb3..origin/release/1.1.1 -- android` prints nothing (exit 0): no file under `android/` changed after the build commit.
- Registry: `CAP-071,capture,planned,…` (line 129); `CAPTURE_BLUETOOTH_HCI_SNOOP.md` the Group BG section (line 1756) and the Capture Index row (*planned*).
- **Re-checked values of the prompt's §2:** film 2,158.67 s, 1,686,259,198 bytes, H.264 1280×720 (portrait), AAC 44.1 kHz stereo, `creation_time`
  16:27:28Z = the end — confirmed; it is **a camera film**, not a screen recording (the phone, the case and hands in view). HCI packet counts and times as stated;
  `cmp -n 319218` exit 0 and the Python prefix test `True`. Exports 882/1,013 lines (`wc -l` says 881/1,012: the last line has no newline; the toasts say 882 and
  1013). App logcat 671 lines, UTC = phone − 2 h exactly. System log 221,518 lines. File modes: four `-rwxr-----` (fixed to 644). LFS: `git check-attr filter` → `lfs`
  for every data file incl. the new extract, `unspecified` for the notes. **`~/opencontrol-1.1.1-tested` (B3) exists** (the prompt measured it absent; its files
  carry the build's mtimes, 17:20, `cp -a`) and equals `dist/1.1.1/`.

### Phase 0 decisions (chat 2026-10-08, `AskUserQuestion`, answers verbatim)

| Question | Answer |
|---|---|
| The film shows a third party's phone number and records the call (18:13:36–18:14:13). What should happen to the film before it is committed? | "Commit as is" |
| What should be committed of the system log … ? | "Extract only (Recommended)" (with the `awk` command in the preview) |
| Migration plan: rename the folder to `CAP-071-2026-10-08_17-51-29_18-27-28-Group_BG` …, chmod 644 …, OK? | "Yes, as proposed (Recommended)" |
| May I read the installed APK from user 10 … to prove its SHA-256 equals dist/1.1.1? | "Yes, pull it (Recommended)" |

I had advised against "Commit as is" (the number of a third party and a call on the audio track); the decision is the maintainer's. The number is in no document.

**Executed:** checksums of a fresh listing before and after `mv` identical (and the data unchanged since the first inventory); `chmod 644`; the extract made
(47,628 lines, SHA-256 `2b56846491b20730a0f31ca91897d2d7fd7dda27f461ba2c714f7b04d971a2f2`; checked: no account address, group id, Wi-Fi name or the caller's
number); the full system log moved out of the repository to `~/opencontrol-local/CAP-071/` (`sha256sum -c` OK there); the two references to the placeholder folder
outside `ai-sessions/` updated (`APP_TESTPLAN.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`); no reference under `android/`.

## Phase A — the film

- 2-s scan: 108 contact sheets; every change narrowed at 1 s (2,159 frames with the film time burned in; a frame-difference map marks every movement; the two quiet
  stretches confirmed by it); zooms for the Info lines, the tab row, the toast, the case slots, the "—" screens; a 5-fps strip of the first ready; 8-fps strips of
  the two status-bar minute flips.
- **Clock:** phone = overlay + 1.25 s (± 0.1) at both ends; cross-checked at the first ready (film 17:57:29.51 overlay = phone 17:57:30.76; E2 `17:57:30.765 …
  Discovering -> Ready`).
- **Audio:** what it holds is in `CAP-071-FINDINGS.md` §0 (ringtone and voice-like bursts during the call, the Find ring, no music, no case chime, nothing said).
- **Privacy:** no address overlay; head partly at 18:06:56–58; Wi-Fi name, carrier, Bluetooth names; public web pages; **the phone call** (decision above).
- `CAP-071-EVENT-NOTES.md` rewritten: status, Log Metadata, the maintainer's P0/P1 outputs unchanged, the integrity pre-flight, the review method and privacy, an
  Event Timeline of 88 rows (phone time, actor, step, test-plan ID, registry Test-ID, evidence), the step mapping, the Test-ID traceability with "expected but not
  observed", the analysis checklist, and the skeleton as an unchanged appendix.

## Phase B — the logs (tables)

All tables are in `CAP-071-FINDINGS.md`: §1 the ACLs and the 27 Message Stream claims (all the app's; the Play-services marker: 0 in all four logs, exit 0; positive
control `CAP-066` 22), §2 the twelve reads per session against `CAP-070`, §3 every write against the reference bytes (24 + 9 + 4, all equal, all answered), §4 the
channel per announcement against the buds in/out, §5 C12/S12/T11 (not run), §6 the session-end table (9 ends + the two failed Connects; the processes), §7 the
battery values shown against the wire, §9 the 1.1.0 → 1.1.1 comparison. Command lines are in the FINDINGS (rule 4a).

**Leads of the prompt's §3, answered:** (1) the update — 🟢 in place, data and settings kept, 1.1.0 had been added to user 10 at 17:40 (FINDINGS §0); (2) no
behaviour change — 🟢 (§9); (3) the screens — 🟢 (§5), S6/U3 not measurable without a screen recording; (4) every write — 🟢 (§3); (5) ANC during playback — 🟢
on the wire (A2DP streaming A2462–A3091 across all 9 `Set`s), "it sounded right" is the maintainer's statement; (6) robustness — 🟢 (§6); (7) channel choice — 🟢 two
more samples, 🟡 one 19 → 21 on the Right going back to the head, L-1 itself 🔴 not run (§4); (8) C12 — not run; (9) S12, T11 — not run; (10) known issues —
unchanged; (11) battery — 🟢 every shown value = the wire (§7); (12) nothing new or wrong — 0 NAK, 0 error status, firmware `release_5.203` (no Safe Mode), 0 late
answers, nothing on DLCI 0x08/0x0a, no request outside the user's actions, the "A resource failed to call close" lines are framework objects present in 1.1.0 too
(🟡), the `SIGQUIT`s are the computer's `bugreportz`; (13) the extra actions — in the timeline (the tiles added, the call, the radio, the extra case cycles).

## Phase D — checkpoint (chat 2026-10-08, `AskUserQuestion`, answers verbatim)

| Question | Answer |
|---|---|
| Release verdict for 1.1.1 (RELEASING.md C4) … Which verdict? | "Release 86a6fb3 as 1.1.1 (Recommended)" (preview: verdict, known issues unchanged, the open items) |
| Known issues for 1.1.1 (CHANGELOG.md and release-notes): change the wording? | "Keep unchanged (Recommended)" |
| PROTOCOL.md notes (dated, no status change) — which may I add? | "§2.2a L-1 note (Recommended)", "§5 page-after-drop note" (the texts in the previews) |
| TODO.md §2 after this run: remove the settled items … and keep the rest open …? | "Yes, as described (Recommended)" |

**Applied (documentation only):** `PROTOCOL.md` §2.2a note and §5 note (dated 2026-10-08, the approved texts); `CAP-071-FINDINGS.md` §10 records the verdict;
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` the Group BG run note (with the `bugreportz` note and the next-run procedure) and the Capture Index row (*analyzed*);
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` a `CAP-071` pointer in the 23 Test-IDs' evidence cells (and BG in their Group column where exercised); `APP_TESTPLAN.md` section U
results (11 ✅, 3 ⚠️) and its Summary row; `id_registry.csv` `CAP-071` → analyzed with the film times; `TODO.md` §2 (two items removed, five updated, the next-run
procedure), §4 (the restore item: only the Upper treble band left), §5 (a); `README.md` status and capture counts; `PROJECT.md` "Status after 1.0.x" one sentence;
`ai-sessions/INDEX.md` the 0080 row. **Not changed:** `DECISIONS.md` (no ADR), any FACT status, `CHANGELOG.md` (known issues unchanged; the date is E1),
`ARCHITECTURE.md`, anything under `android/`, `dist/`, `scripts/`.

**`PROJECT.md`/`README.md`:** the exact texts were shown in chat afterwards and approved ("PROJECT/README": *"Approve as shown (Recommended)"*); commit: *"Commit,
push, update PR (Recommended)"*.

**Proposed, not made:** `scripts/release_notes.template` needs no wording change (the known issues are unchanged).

## External sources (URL and quoted text)

- AOSP `frameworks/native/+/refs/heads/main/cmds/installd/binder/android/os/IInstalld.aidl` (fetched with `?format=TEXT | base64 -d`, 2026-10-08), lines 150–156:
  `const int FLAG_STORAGE_DE = 0x1;` `const int FLAG_STORAGE_CE = 0x2;` `const int FLAG_STORAGE_EXTERNAL = 0x4;` … `const int FLAG_CLEAR_CODE_CACHE_ONLY = 0x20;`
- AOSP `frameworks/base/+/refs/heads/main/services/core/java/com/android/server/pm/InstallPackageHelper.java` (same method): `if (request.isClearCodeCache()) {
  mAppDataHelper.clearAppDataLIF(ps.getPkg(), UserHandle.USER_ALL, FLAG_STORAGE_DE | FLAG_STORAGE_CE | FLAG_STORAGE_EXTERNAL | Installer.FLAG_CLEAR_CODE_CACHE_ONLY); }`
- AOSP `…/pm/Installer.java`: `EventLog.writeEvent(EventLogTags.INSTALLER_CLEAR_APP_DATA_CALLER, pid, uid, packageName, flags);` — the logged number is the flags.
- grapheneos.org/usage (quoted in `CAP-070-FINDINGS.md` §0, not re-fetched): *"GrapheneOS has a compatibility layer providing the option to install and use the
  official releases of Google Play in the standard app sandbox."*

## Files read

**In full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `TODO.md`, `AI_SESSION_LOG_PROCEDURE.md`, the 0080 prompt, the committed skeleton
and its working-tree additions, `CAP-070-FINDINGS.md`, the 0079 RESULT (from its §Summary), both exports (every non-hex line; hex lines checked against the HCI
decode), the app logcat's 1.1.0 lines and every warning line, `SessionReopener.kt` lines 60–140. **In part:** `PROTOCOL.md` (§0, §2.2a, §2.3, §4.1, §4.2, §4.3
Option B/F, §4.5 preamble, §4.5.2, §4.5.4, §4.5.6–§4.5.8, §5 start); `DECISIONS.md` (ADR-010, 029, 034, 036, 037, 042, 044–057 in full; the other ADRs by heading
only); `CAP-070-EVENT-NOTES.md` (metadata, pre-flight, method, timeline); `RELEASING.md` (checklist, §4–§13); `APP_TESTPLAN.md` (sections C, H, K, O, S, T, U and the
Summary rows); `CHANGELOG.md` `[1.1.1]`; `README.md` status lines; `CAPTURE_BLUETOOTH_HCI_SNOOP.md` the Group BG section and index rows; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
the 23 Test-ID rows; `ai-sessions/INDEX.md` tail; the system log in the ranges named (package/activity/window/Bluetooth/adb/dumpstate lines, 17:35–18:29, plus
14:31 and 17:40); `scripts/pwrpc_decode.py`, `scripts/lint_docs.py` (the session-log rule). **Not read:** `CAP-049-EVENT-NOTES.md`/`FINDINGS.md` and
`CAP-068-FINDINGS.md` (the layout followed `CAP-070`'s instead), the FINDINGS of the other reference captures (their bytes were matched on the wire instead),
the Kotlin sources the prompt lists other than those named above (no finding depends on code behaviour that 1.1.0's analysis had not already traced).

## Deferred documentation

Each is in `TODO.md` (§2, §4, §6):

- §2 — T11, C12, H5/S6/S12, lead L-1 (`INEAR-005`), the sound observations: not done in `CAP-071` (no film 2, no screen recording, nothing said).
- §2 — the next app run: lid open before the app starts, no app use before the film, Do Not Disturb, the screen recording, observations aloud.
- §4 — one tap on Sound → BALANCED (the Upper treble is at +6.0).
- `scripts/lint_docs.py`: it flagged this session's own committed PROMPT (the placeholder folder path and the full system log's file name, both removed
  by the approved migration). The maintainer chose (chat 2026-10-08, "Lint": *"Edit the two names in the prompt (Recommended)"*): the path now names the
  renamed folder and the log's name is no longer a backticked file reference; `lint_docs.py` → exit 0, `ensure_footers.py` → nothing to add.
- `CAP-070-FINDINGS.md` §0/§12: a pointer that its two `SIGQUIT`s were most likely `bugreportz` (🟡) — not made (no approval asked); in `TODO.md` §6.

## Commits

On `release/1.1.1` (the maintainer answered *"Commit, push, update PR (Recommended)"* in chat 2026-10-08); the first push dropped the SSH connection after the LFS
upload, the second pushed the refs (`63a56a9..ac1d1e3`). Pull request #15's checklist B2–C4 ticked.

| Hash | Subject |
|---|---|
| `a84be2d` | docs(capture): CAP-071 (Group BG) — the 1.1.1 release run, analysed |
| `4611885` | docs: record CAP-071 — registry, index, test plans, TODO, two PROTOCOL notes |
| `ac1d1e3` | docs(session): ai-sessions/0080 result, INDEX row, prompt file names |

**After the session (the maintainer's release steps, chat 2026-10-08):** PR #15 merged with a merge commit `f359aa2` (the maintainer); signed tag `v1.1.1` on
`86a6fb3` created and pushed at the maintainer's request (`git branch -r --contains v1.1.1` lists `origin/main`); release published 2026-10-08 19:41 UTC by the
maintainer (the downloaded APK's SHA-256 = `062f35b3…f1d0`); D4: updated with Obtainium, works. E1–E4 in the pull request of branch
`docs/1.1.1-after-publish` (CHANGELOG date, README, the Release log row, this back-fill, the template).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0080_CAPTURE_RESULT_2026_10_08.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0080_CAPTURE_RESULT_2026_10_08
