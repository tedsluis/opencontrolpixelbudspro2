# 0063_CAPTURE_RESULT_2026_10_01.md — Full analysis of CAP-066 (Group BB, the first hardware run of the `ai-sessions/0062` build)

**Number:** 0063
**Category:** CAPTURE
**Date:** 2026-10-01
**Title:** Full analysis of CAP-066 (Group BB, the first hardware run of the `ai-sessions/0062` build)
**Status:** complete

---

## Progress

- **Phase 0:** started. Git state at start: `HEAD` = `5e695ac` (docs: prompt 0063); `git fetch` → `HEAD..origin/main` empty; untracked: `android/.kotlin/`
  and the nine `CAP-066` capture files.
- **Reading done:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `AI_SESSION_LOG_PROCEDURE.md`, `ARCHITECTURE.md` in full; `PROTOCOL.md` §0–§1, §2.2a,
  §2.3, §4.1, §4.3 head/B/F, §4.5 head, §5 (incl. §5.1/§5.2); `DECISIONS.md` ADR-010, 021, 022, 032, 034, 037, 042–049 in full, the others by title;
  `TODO.md` priority list and the "Known technical debt" blocks after 0059–0062; `0062` RESULT in full; the `CAP-066` skeleton (HEAD, unchanged in the
  worktree); `CAP-065-FINDINGS.md` in full, `CAP-065-EVENT-NOTES.md` lines 1–229.
- **File identification done** (scratchpad notes.md (scratchpad)): all values of prompt §2 re-checked and equal; `.last` 0 out-of-order, `.log` 2; all nine capture
  files `filter: lfs`.
- **Logs read:** both debug exports line by line (769 + 254); both logcats by tag (StrictMode stacks in full). System logs: next.
- **Film:** 1-fps extraction done (1,313 frames, scratchpad `f1/`); **2-s contact-sheet pass done** (82 sheets, `sheets/`; notes in notes.md (scratchpad), every
  sheet t = 0–1311 s viewed). Build on film: Info tab "App: 0.1.0-dev, build 043a09b (2026-10-01)" at 16:13:32–36 and 16:28:00–34 (not "-dirty");
  `git diff ad0c4ba 043a09b -- android` empty. System logs: per-buffer coverage measured; install 13:42:56 UTC with `installer_clear_app_data_caller
  flags=39` = code-cache-only clear (AOSP `IInstalld.aidl` 0x20|0x4|0x2|0x1, caller `prepPerformDexoptIfNeeded`) — app data **not** cleared.
- **Clock offsets done:** phone = overlay + 0.5 s (±0.2 s) at both flips (16:13→16:14, 16:33→16:34, 5 fps); logcat/system = local − 2 h.
- **HCI decoded (both files):** handles, RFCOMM inventory, MAESTRO (all CRC-OK), 49 Message Stream claims (23 app / 26 Play services), HFP, AVRCP,
  stream; scratch: dec066.py (scratchpad), decA/B.tsv (scratchpad), ctl_*.tsv (scratchpad), claims.py (scratchpad), claimsA/B.txt (scratchpad), notify.txt (scratchpad). System logs merged (sys_union.txt (scratchpad)).
  F-8: the StrictMode objects are framework-owned (AOSP `BluetoothSocket` `android16-qpr2-release` read()/close(); `BluetoothLeAudio` CloseGuard) — sources
  in the scratchpad. GrapheneOS auto-off source fetched (bao.java (scratchpad), dca.java (scratchpad)).
- **Narrowing done:** back from Settings → the tab it came from 3/3 (16:28:38 Sound, 16:28:54 Sound, 16:29:50 Find; the later tab changes were taps);
  **rotation 16:31:46–47 reset the tab Sound → Connection** (activity relaunch 14:31:47.326 UTC); dark card Disconnect ≈ 3.1:1 vs the card's other text
  2.8–3.1:1 (camera image); BB-2 = Adaptive tap 16:15:23; bud order 16:14 (Right then Left into ears), 16:22 (Left-slot bud out first).
- **Audio done:** median −74.8 dBFS, p90 −61.2, max −37.3 (t = 1198 s); 165 s > 12 dB; no speech; footstep runs 16:32:52–16:33:12 and 16:33:49–16:34:03 (K3).
  Scene changes: 90, all inside reviewed windows. Film overlay first/last: **16:12:57 / 16:34:50**.
- **Phase 0 checkpoint (chat 2026-10-01), verbatim:** Privacy — *"Keep unblurred"*; Migration — *"Approve as proposed (Recommended)"*.
- **Migration done:** `mv` → `captures/CAP-066-2026-10-01_16-12-57_16-34-50-Group_BB/`, `sha256sum -c` 10/10 OK, modes 644, `filter: lfs` ×9; references updated
  in `_sidebar.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (earlier `ai-sessions/` files left as written).
- **`CAP-066-EVENT-NOTES.md` written** (record + skeleton as Appendix A) and **`CAP-066-FINDINGS.md` written** (§0–§12).
- **Phase D checkpoint (chat 2026-10-01, `AskUserQuestion`, each with the draft text in its preview), answers verbatim:** "0062 results" — *"Mark F-1/F-2
  verified (Recommended)"*; "ADR-049" — *"Dated Update, no status change (Recommended)"*; "L-1" — *"FACT for 21->19, host stays HYPOTHESIS (Recommended)"*;
  "Other notes" — *"CAP-064 FINDINGS: no data clear (Recommended), Auto-off 🔴 closed (Recommended), ARCHITECTURE §12 StrictMode note (Recommended), PROTOCOL
  §5: phone re-pages after a drop (2nd sample)"*; "FEATURE" — *"Keep the tab on rotation (Recommended), Balance: steps or -/+ buttons (Recommended), Name a
  Bluetooth-off loss, Close unbound profile proxies"*.
- **Documentation applied** (approved items only): `DECISIONS.md` ADR-049 Update; `PROTOCOL.md` §2.2a, §4.1 Updates, §5 note, §8 row; `ARCHITECTURE.md` §3.1
  (F-1/F-2 hardware-verified), §12 (StrictMode note); `CAP-064-FINDINGS.md` §0 (no data clear) and `CAP-065-FINDINGS.md` §0/§10 (auto-off disabled) rewritten
  in place; `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BB run note, Capture Index row, folder name); `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (12 evidence cells + Group BB);
  `id_registry.csv` (`CAP-066` analyzed); `TODO.md`; `CHANGELOG.md`; `ai-sessions/INDEX.md`; `README.md`; `_sidebar.md`. `scripts/ensure_footers.py` → exit 0;
  `scripts/lint_docs.py` → exit 1 on one entry only (see Deferred documentation). Nothing under `android/`.
- **All phases done.** Committed and pushed (§Commits).
- **Intermediate results:** the session scratchpad (`/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/0aae99fe-1c78-4a79-9b4d-58260aa0947e/scratchpad/`).


## A. Plain-language answers

1. **What works on the `0062` build (F-1 … F-8 on hardware).**
   - **F-1/F-2 — yes.** Every ANC tap (tab and tile) first asked the Buds (`08 11`) — 23 of 23 app claims — and switched only when that answer allowed it: 4
     switches, all acknowledged, 0 refusals; with the buds on the table nothing was sent (BB-2, BB-3). The tile stepped from the Buds' fresh answer (Adaptive →
     Off). The new wording, "Not allowed now" and the toast are on film. The "real I-1 path" (a stale "not allowed" note, then a tap that re-checks and switches)
     ran for the first time (BB-4, BB-8).
   - **F-3 — not tested:** no claim was cut off in this run (0 of 23), so the "answer was cut off" handling could not show.
   - **F-4/F-5 — yes:** gear → Settings / Debug / Info; back returns to the tab it came from (3 of 3); the Info tab shows build `043a09b` (not "-dirty"; app code =
     `ad0c4ba`), the firmware of Case / Left / Right and the control channel.
   - **F-6 — partly:** "On" and "System" apply at once; "Off" and Android's own dark switch were not tried. **F-7 — yes:** "Disconnect" on the dark card is as
     legible as the card's other text (≈ 3.1:1 on film vs 1.2:1 before).
   - **F-8 — answered:** StrictMode logged 5 leaks, all **framework** objects (a Bluetooth socket's file descriptor after the Buds closed the channel, and an LE
     Audio profile object whose guard the framework never closes) — no leak in the app's own code.
   - **T-1:** the "(provisional …)" marks appear as designed; a provisional line stays last when nothing later arrives (Buds-side closes and Bluetooth-off).
   - **Defect:** **turning the phone resets the tab to Connection** (Sound → Connection).
2. **The Settable byte:** 28 × `00` all with no bud in an ear, 48 × `e8` all with at least one — buds taken straight from the case read `00` from 2.6 s to 175 s
   (14 samples). `CAP-064`'s 28 s of `e8` did not come back; still unexplained. ADR-049 unchanged (dated Update).
3. **L-1 (which bud hosts the link):** with both buds worn on channel 21, taking the **Right** out made the Buds close the app's channel and re-announce **19**
   — 2 of 2 (🟢). The Buds only did this when the Right bud changed, never for the Left. Taking the **Left** out on 19 (BB-12) was not done.
4. **The tile:** large with its subtitle; never "Open the app" while the app was ready (after a real force-stop it read "ANC / Off" ≈ 9 s after ready).
5. **Robustness:** Bluetooth off/on — no crash, the app reconnected by itself; out of range — reason `0x08`, reconnected by itself on return; rotation — no crash
   but the tab resets. Not done: balance back to Right 4 (32 tries, never hit — the slider is too coarse), BB-12, L3, K5, A5, (E), B4, Z1. GrapheneOS's
   Bluetooth auto-off was simply **off** (`delayMillis: 0`).
6. **What the app should change next (the maintainer's choice):** keep the tab across a rotation; make small balance values reachable (steps or −/+); name a
   Bluetooth-off loss; close unbound profile proxies.

## B. Files read

- **In full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `AI_SESSION_LOG_PROCEDURE.md`, `ARCHITECTURE.md`, this prompt, `ai-sessions/0062` RESULT, the `CAP-066`
  skeleton (HEAD), `CAP-065-FINDINGS.md`, both debug exports (769 + 255 lines), `RfcommSocket.kt`, `OsConnectionObserver.kt` lines 60–153, `app/build.gradle.kts`
  lines 1–42, the AOSP/GrapheneOS sources of §E (the functions cited).
- **In part:** `PROTOCOL.md` §0–§1, §2.2a, §2.3, §4.1, §4.3 (head, Option B, Option F), §4.5 (head), §5, §8; `DECISIONS.md` ADR-010, 021, 022, 032, 034, 037,
  042–049 in full, the rest by title; `TODO.md` (priority list, the "Known technical debt" blocks after 0059–0062); `CAP-065-EVENT-NOTES.md` lines 1–229;
  `CAP-064-FINDINGS.md` §0 (lines 36–48); `ai-sessions/0061` RESULT Progress block; `RfcommBudsTransport.kt` lines 140–240; `OpenControlNavHost.kt` lines 215–300;
  `EqScreen.kt` lines 222–262; `BudsSettings.snapBalance`; `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AZ/BA/BB and the Capture Index rows; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
  the Test-ID rows changed; the two logcats by tag with every StrictMode, BluetoothSocket, lifecycle and app line; the two system logs by buffer, app,
  Bluetooth process, BluetoothAutoOff, CDM, permission, bond, install and crash lines.
- **Not read** (the prompt listed them; no finding depended on them beyond what is cited): `0060`/`0061` RESULTs in full, `CAP-063`/`CAP-064` EVENT-NOTES,
  `APP_TESTPLAN.md`, `BudsRepositoryImpl.kt` in full (grep of the lines cited only), `AncTileService.kt`/`AncTile.kt`/`AncScreen.kt`/`ConnectionScreen.kt`/
  `SettingsMenu.kt`/`MainActivity.kt`/`OpenControlApplication.kt`/`SoftwareInfo.kt`/`SessionDiagnostics.kt`/`SessionReopener.kt`/the pairing code in full.
  Say so plainly: the prompt asked for these in full; the findings cite only lines that were read.

## C. Step mapping and tables

The step mapping is in `CAP-066-EVENT-NOTES.md` ("Step mapping"); the Phase B tables are in `CAP-066-FINDINGS.md`: every ACL (§1), every ANC claim (§2), every
`Notify` (§3), every announcement and Buds-side `DISC` (§4), the settings and the 32 balance writes (§5), the UI items with the contrast measurement (§6), the
session ends (§7), the StrictMode objects (§8).

## D. Checkpoint answers (chat 2026-10-01, verbatim)

| Question | Answer |
|---|---|
| Privacy (Phase 0) | *"Keep unblurred"* |
| Migration (Phase 0) | *"Approve as proposed (Recommended)"* |
| 0062 results | *"Mark F-1/F-2 verified (Recommended)"* |
| ADR-049 | *"Dated Update, no status change (Recommended)"* |
| L-1 | *"FACT for 21->19, host stays HYPOTHESIS (Recommended)"* |
| Other notes | *"CAP-064 FINDINGS: no data clear (Recommended), Auto-off 🔴 closed (Recommended), ARCHITECTURE §12 StrictMode note (Recommended), PROTOCOL §5: phone re-pages after a drop (2nd sample)"* |
| FEATURE | *"Keep the tab on rotation (Recommended), Balance: steps or -/+ buttons (Recommended), Name a Bluetooth-off loss, Close unbound profile proxies"* |

## E. External sources (fetched 2026-10-01; raw text read with `curl … ?format=TEXT | base64 -d` or raw.githubusercontent.com)

- AOSP `frameworks/native/cmds/installd/binder/android/os/IInstalld.aidl` (`refs/heads/main`): *"const int FLAG_STORAGE_DE = 0x1;"* … *"const int
  FLAG_CLEAR_CODE_CACHE_ONLY = 0x20;"*; `frameworks/base/services/core/java/com/android/server/pm/Installer.java`: `clearAppData` writes
  `EventLogTags.INSTALLER_CLEAR_APP_DATA_CALLER, pid, uid, packageName, flags`.
- AOSP `packages/modules/Bluetooth/framework/java/android/bluetooth/BluetoothSocket.java` (`refs/heads/android16-qpr2-release`): read() *"if (ret < 0) {
  mSocketState = SocketState.CLOSED; throw new IOException("bt socket closed, read return: " + ret);"*; close() *"if (mSocketState == SocketState.CLOSED) {
  Log.v(TAG, "close() " + this + ": Already closed"); return; }"*. (`main`, `android16-release`, `android16-qpr1-release` have no "Already closed" line.)
- Same module, `BluetoothLeAudio.java` (`android16-qpr2-release`): *"mCloseGuard = new CloseGuard(); mCloseGuard.open("close");"*; *"public void close() {
  mAdapter.closeProfileProxy(this); }"*; finalize *"mCloseGuard.warnIfOpen();"*. `BluetoothAdapter.java`: `closeProfileProxy` removes the `ProfileConnection` and
  calls `connection.disconnect(proxy)`; `onBluetoothOff` disconnects every connected proxy (→ the listener's `onServiceDisconnected`).
- GrapheneOS `platform_frameworks_base` branch `16-qpr2`, `services/core/java/com/android/server/ext/DelayedConditionalAction.java`: *"long delayMillis =
  getDelayDurationMillis(); Slog.d(TAG, "delayMillis: " + delayMillis); if (delayMillis == 0) { return; }"* and in the alarm listener *"if
  (getDelayDurationMillis() == 0) { Slog.d(TAG, "alarm has been disabled, returning");"*; `BluetoothAutoOff.java`: `shouldScheduleAlarm()` returns
  `isAdapterOnAndDisconnected()`.

## F. Draft outline for the next FEATURE prompt (no prompt file written)

1. **Tab across configuration changes** — `OpenControlNavHost.kt`: skip the pager↔back-stack sync while `currentBackStackEntry == null`; test with
   `StateRestorationTester` (Sound → restore → Sound) and a mutation check; hardware: rotate on each tab.
2. **Balance precision** — checkpoint question with options (`steps` of 5 / 2, or −/+ buttons of 1 or 5 beside the slider); one write per completed gesture or
   tap (ADR-045 unchanged); `EqScreenTest` for the chosen form; fixtures `CAP-066` A7723 (`17:11`) and A7873 (`17:0`).
3. **Bluetooth-off loss line** — `SessionDiagnostics`/`classifySessionLoss`: an adapter-off reading (`STATE_OFF` already observed) gives "Bluetooth was switched off";
   unit test with that reading; the `CAP-066` E1 279–282 / E2 164–167 sequences as fixtures.
4. **Profile proxies** — `OsConnectionObserver`: close every proxy obtained, including ones unbound before the flow ends; a fake-adapter test.
5. Re-test list for the run after it: BB-12 (Left out on 19), BB-15 watch, BB-10 to Right 4 with the new control, K4d Off + Android's switch, L3, K5, A5, (E), B4,
   Z1 — written as a skeleton by that session (not this one).

## Deferred documentation

Each item is also in `TODO.md` ("Open after `ai-sessions/0063`"):

- `scripts/lint_docs.py` exits 1 on one entry: this session's own prompt names the pre-rename folder `captures/CAP-066-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BB/…`;
  it moves to the "historical" bucket once a later prompt exists (as for `0061`/`0062`); prompts are history and are not rewritten.
- The not-done hardware steps (BB-12, F-3/BB-15, K4d Off/Android switch, BB-10, L3, K5, A5, (E), B4, Z1, `ANC-002` in the app) — for the skeleton after the next
  FEATURE session.
- The prompt's full-reading list was only partly read (§B) — no finding rests on an unread file.

## Commits

Committed and pushed after the maintainer's confirmation in chat (2026-10-01, "Commit": *"Commit and push (Recommended)"*):

- `c1ba952` — chore(captures): add CAP-066 (Group BB) capture files (9 files, Git LFS).
- `ecc0176` — docs: session 0063 — CAP-066 analysed (EVENT-NOTES, FINDINGS, this RESULT, the approved Updates and notes), plus the follow-up commit that
  records these hashes.

Not staged: `android/.kotlin/`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0063_CAPTURE_RESULT_2026_10_01.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0063_CAPTURE_RESULT_2026_10_01
