# 0088_CAPTURE_RESULT_2026_10_12.md — Full analysis of CAP-075 (Group BK: 1.2.1 build `31ee183`, the "Noise-control channel" switch in the user without and the user with Google Play services)

**Number:** 0088
**Category:** CAPTURE
**Date:** 2026-10-12
**Title:** Full analysis of `CAP-075` (Group BK): two screen recordings, four HCI snoop logs, two app debug exports, two app logcats, a system log and a full logcat — set 1 (no Play, 2026-10-12) and set 2 (Play, 2026-10-13) — which switch position works best in each user, and how the Play user can be made better
**Status:** complete

## Summary (plain language first)

**What the run showed.** Without Google Play (the secondary user, set 1, 2026-10-12) the new "Noise-control channel" switch changes nothing, as designed: no
other app ever touched the channel, all 41 press-and-holds made with the noise-control tab open were on screen within a second in both switch positions, no hint
was ever shown — the 1.2.0 behaviour of `CAP-073` is intact. With Google Play (the Owner user, set 2, 2026-10-13) Play's Fast Pair takes the channel back
5.3–5.8 s after it lost it and retries 2.1–2.8 s after a collision — the same timers as `CAP-074`. "Show a hint" did what ADR-062 says: the hint with the cut's
time, no re-claim; but the app's own claim lives only until Play's next connect (0.4–4.7 s), so none of the 9 holds made in that mode was shown as it happened.
"Take it back" ran three chains of exactly three re-claims (3 000 / 6 000 / 12 000 ms as scheduled); every re-claim collided with Play's retry (attempt 1
refused by the stack, attempt 2 connecting 0.9–1.5 s later by killing Play's socket) and every re-claimed hold was cut again 5.3–5.8 s after Play's loss: the app
held the channel about 43 % of each 44-second chain and then nothing until a tap; 8 of the 19 holds in the chains were on screen within a second, 6 never. The
6-s and 12-s steps buy nothing — Play's timers did not change over 15 cycles. The stack path of the cut that `CAP-074` left 🟡 is in the system log for 15 of
15 cuts. No crash, no write, no Safe Mode, no session lost by the app.

**Which works best per user (on evidence).** Without Play: the positions are indistinguishable; the default "Show a hint" is right (it never shows a hint there).
With Play: "Take it back" shows holds at all (8 of 19 vs 0 of 9) but fights Play and covers less than half of a 44-s chain; "Show a hint" keeps Android's own Buds
state consistent and shows essentially no hold. Neither is a good press-and-hold experience in the Play user.

**The maintainer's decisions (chat 2026-10-13, verbatim in "Checkpoints" below):** the folder and recordings renamed, modes 644, the system log committed as a
redacted extract, the films and exports committed as is, no APK pull; the verdict "No heavy issue: release 1.2.1 as built"; the ADR-062 Update approved; a 🟢
`PROTOCOL.md` §4.1 note on Play's client; the CHANGELOG Known-issues line replaced with the measured numbers; the next FEATURE prompt = P1 + P2 + P4 (a constant
3-s step with a longer chain, a "Take it back again" action on the hint, a cause label for a mode learned by a re-claim's read); the leftover worktree of the
previous session removed.

**Continuation in chat (2026-10-13, after this session's commits):** the maintainer asked to merge #44 when CI was green (merged, `0596fc2`), then "Release 1.2.1 en werk alle documenten bij": the kept release notes' Known-issues paragraph replaced by the measured text (C4 minor issue), the maintainer's "Yes: tag, push, draft, publish" (`AskUserQuestion` "Publish"), `git tag -s v1.2.1 31ee183`, the tag pushed, the draft release created from the kept copy (three assets) and published 2026-10-13T06:42:36Z, the uploaded APK re-downloaded and its SHA-256 `a60a3cc9b2a80e9dc5fecb8810c9cb8e7cbb20fa9eb7a2a0ce663b89e964963e` confirmed; E1–E4 and the other documents on `release/1.2.1-after` by pull request. D4 done 2026-10-13: the maintainer installed the published 1.2.1 with Obtainium (chat: "De installatie van 1.2.1 op mijn telefoon met Obtainium is gelukt"). **Next:** the FEATURE prompt 0089.

## Phases

- **Phase 0 (reading, the folder, the scratch directory):** as the Progress block records; Checkpoint 0 deferred to the end of the analysis (the prompt allowed
  it) and asked together with Phase D.
- **Phase A (files and clocks):** the four HCI logs' boundaries and ACLs, the two films' clock offsets (17:22:31.85 and 06:29:04.15 ± 0.05 s), the exports'
  and app logcats' process and user lines; the integrity pre-flight in `CAP-075-EVENT-NOTES.md`.
- **Phase B (the analysis):** the claim inventories of DLCI 0x04 for both clients in all four logs (claims.py over `tshark` field dumps), every `Notify`
  attributed, every second of both films viewed or proved unchanged (1-fps sheets + 10-fps status-bar strips), both audio tracks tabulated, both exports and app
  logcats read line by line, the system log swept (the 15 cuts' stack lines, Play's Hearable Controls lines, the Google app's RFCOMM channels), the Connect
  reads compared byte for byte with `CAP-073`/`CAP-074`, 0 `Set` frames, Play's two timers measured (18 + 15 values), the external checks fetched.
- **Phase C (the documents):** `CAP-075-EVENT-NOTES.md` (the two timelines, the steps as run, Test-IDs, the checklist; the skeleton as the appendix) and
  `CAP-075-FINDINGS.md` §0–§12.
- **Phase D (checkpoints, registers, commits):** the questions below; then the file operations and every register edit; `ensure_footers.py` (up to date) and
  `lint_docs.py` → **exit 0, clean**.

## Checkpoints — the maintainer's answers (verbatim, `AskUserQuestion`, chat 2026-10-13)

| Question (header) | Answer |
|---|---|
| Users | "Yes, as read (Recommended)" — set 1 = the secondary user without Play, set 2 = the Owner with Play |
| Folder name | "Two dates (Recommended)" — CAP-075-2026-10-12_17-22-31_2026-10-13_06-34-32-Group_BK |
| Recordings | "recording1 / recording2 (Recommended)" |
| Modes + sums | "chmod 644 + sha256sum file (Recommended)" |
| Films | **"Commit as is"** (no blur of the Wi-Fi name, the serials or the e-mail fragment; audio kept) |
| System logs | "One extract from logcat-all (Recommended)" — CAP-075-logcat-extract.txt; both full files to ~/opencontrol-capture-originals/CAP-075/ |
| Exports | "Commit as is, as CAP-073/074 (Recommended)" |
| APK pull | "Not now — ⚪ stays (Recommended)" |
| Worktree | "Remove it (Recommended)" — .claude/worktrees/cap-075-prompt removed (`git worktree remove --force`, `git worktree prune`) |
| Verdict C4 | "No heavy issue: release 1.2.1 as built (Recommended)" |
| FEATURE | "P1 + P2 + P4 (Recommended)" — draft title in `TODO.md` §5 |
| ADR-062 Upd | "Approve the draft (Recommended)" — written to `DECISIONS.md` |
| PROTOCOL | "Add as 🟢 FACT in §4.1 (Recommended)" — written, with a §8 row |
| CHANGELOG | "Replace with the measured text (Recommended)" — done |
| Registers | "Do all (Recommended)" — done |
| Commits | "Yes, commit on capture/cap-075, ask before push (Recommended)" |

## What was done to the files (Checkpoint 0, in this order)

1. `git worktree remove --force .claude/worktrees/cap-075-prompt` + `git worktree prune` (its 215 modified files were footer URLs rewritten to the worktree
   path by an `ensure_footers.py` run inside it — nothing of value; the branch `capture/cap-075-prompt` is kept, merged as PR #43).
2. `git mv` of the folder to the two-date name; `mv` of the two recordings to CAP-075-recording1.mp4 / CAP-075-recording2.mp4; `chmod 644` on the eight 750 files.
3. The extract: `awk '$1=="10-13" && ($3==17838||$3==21872||$3==22714||$3==7277) && $2>="06:18:00" && $2<="06:36:30"'` over the log viewer's file, `grep -v -i -E
   'telenet|gmail'`, every MAC-shaped string reduced to its first and last octet (`sed -E`) → 20 711 lines; checks: the Wi-Fi name 0 (exit 1), `gmail` 0 (exit 1),
   an e-mail pattern 0 (exit 1), full MAC-shaped strings 0 (exit 1); kept: `already at opened state` 31, `cleanup_rfc_slot` 79, `Retry times remaining` 33. The two
   full system-log files moved to ~/opencontrol-capture-originals/CAP-075/.
4. CAP-075-sha256sum.txt over the eleven committed data files (the four HCI logs, two exports, two app logcats, the extract, the two recordings).
5. The placeholder path replaced by the new folder name in `APP_TESTPLAN.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `_sidebar.md` (+ the FINDINGS link, "planned"
   removed), the two CAP-075 documents, this RESULT and — **mechanically, in the maintainer's prompt** `ai-sessions/0088_CAPTURE_PROMPT_2026_10_12.md` — the
   placeholder path (1), the two recordings' Android names (2) and the backticks around the moved file CAP-075-logcat-all.txt (2), because the lint treats the
   newest session pair's dead references as errors. The 0087 files keep the placeholder (historical, informational in the lint).

## Register and document edits (all approved)

`DECISIONS.md` (the ADR-062 Update of 2026-10-13), `PROTOCOL.md` (§4.1 note 🟢 + §8 row 2026-10-13), `CHANGELOG.md` ([1.2.1] Known issues), `id_registry.csv`
(CAP-075 planned → analyzed), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (the Group BK heading and result paragraph, the §9 row), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
(Group BK on the eight Test-ID rows), `APP_TESTPLAN.md` (§X results X1–X12, the Summary row "X 1.2.1 | 12 | 6 | 0 | 5 | 1", a "Run CAP-075" paragraph),
`TODO.md` (§2 the branches done/not done, §3 the P0 film still open, §4 four open questions, §5 the FEATURE prompt 0089, §M C3/C4 done → D/E remain),
`ARCHITECTURE.md` (§5a's "Changed by the Buds" row, §6.0b's "Not hardware-verified" → the measured sentence), `ai-sessions/INDEX.md` (row 0088 complete).
No change under `android/`; no skeleton, no release step, no `dist/` change, no tag.

## Files read

- **In full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `AI_SESSION_LOG_PROCEDURE.md`, `DECISIONS.md` (ADR-001 … ADR-062 with every Update), `TODO.md`,
  `ai-sessions/0087_FEATURE_PROMPT_2026_10_12.md`, `ai-sessions/0087_FEATURE_RESULT_2026_10_12.md`, `ai-sessions/0088_CAPTURE_PROMPT_2026_10_12.md`, the
  CAP-075 skeleton, `ai-sessions/0086_CAPTURE_RESULT_2026_10_12.md`, `CAP-074-FINDINGS.md`, `CAP-074-EVENT-NOTES.md`, `CAP-073-FINDINGS.md`,
  `CAP-073-EVENT-NOTES.md`, `CAP-071-FINDINGS.md`, `CAP-071-EVENT-NOTES.md`; the Kotlin sources `BudsRepositoryImpl.kt`, `HoldReclaimMode.kt`,
  `HoldReclaimSettingsStore.kt`, `SessionReopener.kt`, `SessionDiagnostics.kt`, `LinkEvaluation.kt`, `RfcommBudsTransport.kt`, `AncScreen.kt`, `SettingsMenu.kt`,
  `MainActivity.kt`, `OpenControlNavHost.kt`, `OsConnectionObserver.kt`; both exports and both app logcats (line by line); both films (every second viewed or
  proved unchanged); the four HCI logs (every DLCI 0x04 frame on the Buds' handle, every DLCI 0x02 frame decoded, every ACL event).
- **In part:** `ARCHITECTURE.md` §2.4/§3/§3.1/§3.2/§5a/§6/§6.0a/§6.0b/§7/§9/§9.0a/§9.1/§12/§13; `PROTOCOL.md` §2.3, §4.1, §4.3 Options B and F, §8's last rows;
  `RELEASING.md` (Repository rules, the Release checklist, §11a, §13); `APP_TESTPLAN.md` §X and the Summary; `CHANGELOG.md` [1.2.1]; `ai-sessions/INDEX.md` rows
  0086–0088; `id_registry.csv` (the CAP-073/074/075, ADR-061/062 and Test-ID rows); `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (the Group BJ/BK sections, the §9 rows of
  CAP-074/075); `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the eight Test-ID rows); `scripts/lint_docs.py` (the exit logic); the two system-log files (swept with
  stated filters, not read line by line: 83 255 + 135 471 lines).
- **Not read:** `README.md`, `SECURITY.md`, `REVERSE_ENGINEERING.md`.

## External checks (§3 item 13; fetched 2026-10-13)

source.android.com *Multiple users* (last updated 2026-09-22 UTC: "Secondary user: any user added to the device other than the system user"); AOSP
`BluetoothSocket.java` (main: `throw new IOException("bt socket closed, read return: " + ret)`; "RFCOMM is a connection-oriented, streaming transport over
Bluetooth"; `connect()` "will block until a connection is made or the connection fails"); AOSP `hci_error_code.h` (`HCI_ERR_PEER_USER = 0x13`,
`HCI_ERR_CONN_CAUSE_LOCAL_HOST = 0x16`); Espressif's reason table ("Remote User Terminated Connection", "Connection Terminated By Local Host"); the Fast Pair
Message Stream page (last updated 2025-08-12: "the Seeker will attempt to connect the message stream", no timing statement); docs.fuchsia.dev `bt_rfcomm`
(RFCOMM §5.2: the non-initiating device's server applications on DLCIs 2, 4, …, 60; DLCIs 1 and 62–63 reserved). Not obtainable: the Device Information page
(404), the developer.android.com `BluetoothSocket` reference page (the fetch tool returns the site's navigation only — the AOSP source was used instead), the
RFCOMM specification HTML on bluetooth.com (404).

## Deferred documentation (each also in `TODO.md`)

- `TODO.md` §2: BK-11 … BK-13 and BK-19 … BK-21 (untested branches); §3: the P0 film (Play's *Nearby devices* permission and version), the APK pull (⚪).
- `TODO.md` §4: the Buds' own closes' trigger, Play's retry counter at 0, the Google app's DLCI 0x08/0x0a traffic, a mode field on DLCI 0x02 (the P5 lead).
- `TODO.md` §5: the FEATURE prompt 0089 (P1 + P2 + P4), later P3/P5; the ADR-062 Update 2 for the constant step comes with it.
- `CAP-074-FINDINGS.md` §10's first open question is answered by `CAP-075-FINDINGS.md` §5 (not edited there — a historical capture document; the
  `PROTOCOL.md` §4.1 note carries the closure).

## Notes on the session itself

- A `git stash -q` in a shell line stashed the modified skeleton for one step; `git stash pop` restored it unchanged and the stash was dropped (no other
  tracked file was touched; the later worktree note's warning about shared stashes is noted for future sessions).
- The image reader refuses more than ≈ 6 images per turn ("media removed: request limit"); the films were read at ≤ 6 sheets per turn.
- Scratch: a mktemp directory under /tmp/claude-1000/ with claims.py, the claim inventories, the `tshark` field dumps, the `pwrpc_decode` outputs, the sheets and
  strips, the audio tables and the edit scripts — not committed.

## Commits

- `be89684` docs(capture): CAP-075 (Group BK) — the folder (the two documents, the four HCI logs, two exports, two app logcats, the extract, the two recordings, the checksum file; Git LFS for the logs, texts and films).
- `23f5bc0` docs: the registers, the ADR-062 Update, the PROTOCOL §4.1 note + §8 row, CHANGELOG, TODO, ARCHITECTURE, the sidebar.
- the third commit (this RESULT, `ai-sessions/INDEX.md` row 0088, the mechanical path/name fixes in the 0088 PROMPT).
Branch `capture/cap-075` on `main` `9c52a4e` (`git fetch`: nothing behind at commit time); pushed 2026-10-13 after the maintainer's "Push and open the PR" (chat); pull request #44 opened against `main` (not merged).

## Progress (updated after every phase and every substantial step)

- **Session start:** 2026-10-13. Branch `capture/cap-075` rebased on `main` at `9c52a4e` (PR #43 merged — the rewritten prompt; the prompt's §2 named `0409a55`, since superseded by #43). `git fetch`: `HEAD..origin/main` empty. Open PRs: none. Untracked: `android/domain/bin/` (never staged) and the thirteen `CAP-075` files.
- **Scratch directory:** a fresh mktemp directory under /tmp/claude-1000/ (its path is written in the session scratchpad's scratchdir.txt; re-create it if gone — the prompt's §0 says the scripts are re-created, not committed).
- **Folder listing (Phase 0 step 2):** `ls -la` + `sha256sum` of 2026-10-13 match the prompt's §2 tables exactly (sizes and SHA-256 of all 13 files; modes 750 on the eight named files, 644 on the logs, the skeleton and logcat-all). No difference found.
- **Done:** reading `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `AI_SESSION_LOG_PROCEDURE.md`, `DECISIONS.md` (ADR-001 … ADR-062, every Update; in full), `ARCHITECTURE.md` §2.4/§3/§3.1/§3.2/§5a/§6/§6.0a/§6.0b/§7/§9/§9.0a/§9.1/§12/§13 (those sections in full), `PROTOCOL.md` §2.3 (incl. the DLCI/server-channel note), §4.1 (in full), §4.3 Option B and Option F, §8 rows of 2026-10-10/12, `TODO.md` (in full); the per-task list: `ai-sessions/0087_FEATURE_PROMPT_2026_10_12.md` and `…RESULT…` (in full), `CAP-075-EVENT-NOTES.md` (the skeleton, in full), `RELEASING.md` (Repository rules, the Release checklist, §11a, §13), `APP_TESTPLAN.md` §X and the Summary, `CHANGELOG.md` `[1.2.1]`, `ai-sessions/INDEX.md` rows 0086–0088, `id_registry.csv` (the CAP-073/074/075, ADR-061/062 and the nine Test-ID rows), `CAP-074-FINDINGS.md` and `CAP-074-EVENT-NOTES.md` (in full), `ai-sessions/0086_CAPTURE_RESULT_2026_10_12.md` (in full), `CAP-073-FINDINGS.md` and `CAP-073-EVENT-NOTES.md` (in full), `CAP-071-FINDINGS.md` and `CAP-071-EVENT-NOTES.md` (in full); Kotlin, in full: `BudsRepositoryImpl.kt`, `HoldReclaimMode.kt`, `HoldReclaimSettingsStore.kt`, `SessionReopener.kt`, `SessionDiagnostics.kt`, `LinkEvaluation.kt`, `RfcommBudsTransport.kt`, `AncScreen.kt`, `SettingsMenu.kt`, `MainActivity.kt`, `OpenControlNavHost.kt`, `OsConnectionObserver.kt`. **Reading complete.**
- **Phase A (the files, the clocks, the integrity) and Phase B (the analysis) done; Phase C (the documents) done:** `CAP-075-EVENT-NOTES.md` rewritten (status, metadata, pre-flight, review method, the two timelines, the steps as run, Test-IDs, the checklist; the skeleton kept as the appendix) and `CAP-075-FINDINGS.md` written (§0–§12). The external checks (§3 item 13) fetched 2026-10-13: source.android.com *Multiple users* (last updated 2026-09-22), AOSP `BluetoothSocket.java` (the `"bt socket closed, read return: "` IOException; `connect()` "will block until a connection is made or the connection fails"), AOSP `hci_error_code.h` (`HCI_ERR_PEER_USER = 0x13`, `HCI_ERR_CONN_CAUSE_LOCAL_HOST = 0x16`), Espressif's reason table (the Core names), the Fast Pair Message Stream page (last updated 2025-08-12: "the Seeker will attempt to connect the message stream" — no timing statement), docs.fuchsia.dev `bt_rfcomm` (RFCOMM §5.2's DLCI partition, DLCIs 1 and 62–63 reserved). The Device Information page returned 404; the developer.android.com `BluetoothSocket` reference page could not be read by the fetch tool (its text is the site's navigation) — the AOSP source was used instead.
- **Accident, repaired:** a `git stash -q` in a shell line stashed the modified (tracked) `CAP-075-EVENT-NOTES.md` for one step; `git stash pop` restored it unchanged (the stash was dropped). No other tracked file was touched.
- **Found:** a leftover git worktree `.claude/worktrees/cap-075-prompt` (branch `capture/cap-075-prompt`, PR #43's branch, from the previous session) — scripts/lint_docs.py walks it and lists its historical dead references twice; nothing of this session is in it. Not removed (Checkpoint 0 asks).
- **Next:** Checkpoint 0 and the Phase D checkpoint (`AskUserQuestion`), then the registers as approved, `ensure_footers.py` + `lint_docs.py`, the RESULT's summary and the INDEX row, then the commit question.
- **Files touched but not yet verified by the maintainer:** captures/CAP-075-2026-10-12_17-22-31_2026-10-13_06-34-32-Group_BK/CAP-075-EVENT-NOTES.md (modified), captures/CAP-075-2026-10-12_17-22-31_2026-10-13_06-34-32-Group_BK/CAP-075-FINDINGS.md (new), this RESULT.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0088_CAPTURE_RESULT_2026_10_12.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0088_CAPTURE_RESULT_2026_10_12
