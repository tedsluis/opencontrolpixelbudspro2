# 0086_CAPTURE_RESULT_2026_10_12.md — Full analysis of CAP-074 (Group BJ: the released 1.2.0 `5b4d5db` in the GrapheneOS user with Google Play services — why only the first press-and-hold after a Connect changes the ANC mode on screen)

**Number:** 0086
**Category:** CAPTURE
**Date:** 2026-10-12
**Title:** Fully analyse `CAP-074`: the screen recording with its audio track, two HCI snoop logs, the debug export, the app logcat and the system log; the root
cause of the first-hold-only pattern in the user with Play services; the comparison with `CAP-073`; the fix options
**Status:** complete (closed 2026-10-12: every checkpoint answered in chat, committed and pushed, pull request opened — see Commits; the fix is `ai-sessions/0087`)

## Summary (plain language)

**What happened on the phone** (`CAP-074`, 2026-10-12 06:40–06:45, the Owner user with Google Play services, the released 1.2.0):

1. **The app did exactly what the rebuilt 1.2.0 was built to do — and that is the problem in this user.** Every time you opened the noise-control tab the app
   claimed the Buds' noise-control channel and held it (ADR-061). But in this user Google Play services' Fast Pair wants that same channel: about **5.2–5.7 s after
   it loses the channel** (it loses it when the app connects) Play tries to take it back, the Android Bluetooth stack refuses the second connection and, on that
   refusal, **closes the app's socket**. The export logs it every time as "RFCOMM on-demand channel 0x04 closed (IOException … read return: −1) … hold ended: the
   channel was closed, not by this app" — twelve times in five minutes, 4.7–5.1 s after each claim. Play then re-opens the channel 2.2–2.6 s later and keeps it.
2. **The app does not take the channel back** — that was your choice for ADR-061 on 2026-10-10 ("no re-claim after a close"). So after each Connect there is a window
   of about 5 s in which a press-and-hold reaches the app and is shown; every later hold reaches Play services instead. On the wire: **47 mode changes you made on the
   buds, 9 reached the app (all 9 on screen), 35 reached Play services** (one of them is even logged by Play's `HearableControlManager`), 3 more were already in the
   mode Play read on taking the channel. The Buds behaved the same as in `CAP-073`; every request of the app was byte-identical to `CAP-073`; nothing crashed, no
   write failed.
3. **Why `CAP-073` worked:** in the user without Play services nothing contends for the channel, the hold lives until you leave the tab (31 s there), and every hold
   is shown.
4. Two side facts: no music played during the recording (the Bluetooth log reads Paused/Stopped until 06:45:35, after the film), and Android's media volume stepped
   down three times at holds (a swipe on the bud together with the hold, probably — out of scope).

**What you decided in this chat (2026-10-12):** Group BJ; folder renamed to the recording's times; all files 644 plus a checksum file; the recording committed as it
is, with your voice; the system log as a filtered extract (the full log and the original recording in `~/opencontrol-capture-originals/CAP-074/`); the root cause
labelled 🟢 for the contention and 🟡 for the stack's exact path in this run; both ADR Updates added (ADR-061, ADR-032); the registers, `TODO.md`, `ARCHITECTURE.md`
and a `PROTOCOL.md` §4.1 note updated; **the fix: a switch on the Settings tab between "hint only, no re-claim" and "re-claim after a foreign close with a
back-off"** — its own FEATURE prompt, **drafted on your request as `ai-sessions/0087_FEATURE_PROMPT_2026_10_12.md`** (the switch and its two modes, the back-off
designed around Play's 5.2–5.7 s / 2.2–2.6 s timers, a draft ADR-062 for mode 2 at its checkpoint, tests, mutation checks, the `CAP-075` (Group BK) skeleton for
both users, the texts of the new version). Not chosen: a Known-issues line for 1.2.0 in `CHANGELOG.md`/`README.md`.

**What you do next:** check the pull request's CI (opened on your answer to the commit question: "Commit + push + open the pull request"), merge it with a merge
commit when green; then, when you want the fix built: "voer prompt ai-sessions/0087_FEATURE_PROMPT_2026_10_12.md uit" in a fresh session.

## Progress

- **Phase 0 (done).** Branch `capture/cap-074` from `main` `08de971` (= `origin/main`; `git fetch`, `HEAD..origin/main` empty). `android/domain/bin/` untracked build
  output, never staged. Fresh `ls -la` + `sha256sum` saved (scratchpad ls_initial.txt, sha_initial.txt): six files, sizes equal to the prompt's §2 table; modes 750
  on four files, 644 on the two HCI logs (now all 644). `capinfos`: the current log 18 k packets 2026-10-11 19:50:00.312 – 2026-10-12 06:52:09.044; `.last` 65 k, 2026-10-11
  09:20:50.730 – 19:50:00.311 (a rotation, `cmp -n 1100280` differs at byte 20; nothing of 2026-10-12 in it). `ffprobe`: 315.166 s, 2,559 video frames, AAC audio,
  `creation_time` 04:45:34Z. **Checkpoint 0 asked and answered (chat 2026-10-12, verbatim below); the folder renamed to
  `captures/CAP-074-2026-10-12_06-40-16_06-45-31-Group_BJ`** (a plain `mv`, the folder was untracked; the listing before/after equal), `chmod 644`, the originals of the
  recording and of the full system log copied to `~/opencontrol-capture-originals/CAP-074/` (`cmp` identical), the extract `CAP-074-logcat-extract.txt` written
  (2,370 lines; SSID values 0, address-shaped strings 0 — positive control on the full log 30 / 106), the full system log removed from the folder,
  `CAP-074-sha256sum.txt` written over the six committed files. The Group BJ section and the `CAP-074` registry row written in Phase D with the other updates.
- **Reading (done):** see "Files read".
- **Phase A (done):** 315 frames at 1 fps (scratchpad `frames/`), per-second diff below the status bar (`diffs.py`): 75 changed seconds + t=0 viewed on 13 labelled
  3 × 2 sheets (`sheets/`), 240 seconds proved unchanged. Clock: the status-bar minute changes at the full frame rate (`clock/`): 06:41:00 between film 44.106–44.324,
  06:42:00 103.987–104.336, 06:43:00 163.992–164.325, 06:45:00 283.995–284.311 ⇒ **phone time = film t + 06:40:15.8 (± 0.2 s)**; cross-check E "Disconnected ->
  Connecting" 06:40:50.304 = the Connect touch on film t=34. Audio: `silencedetect` −35 dB ≥ 0.3 s → 34 voiced segments (the first the recorder's click), 33 in the run
  of 0.4–0.9 s, peaks −22 … −37 dB, each 0.1–1.5 s after a `Notify`; RMS per 100 ms (`astats`): no sustained energy — no music.
- **Phase B (done):** the export (692 lines) and the app logcat (351) read line by line; HCI: the Buds' handle `0x000b` (Connection Complete 9121 06:30:18 — the Buds'
  request; Disconnection 10473 06:31:07 reason 0x16; Connection Complete 10528 06:31:12 after the phone's `Create Connection` — the run's ACL to the log's end); the
  claim inventory of DLCI 4/5 by first phone message (`ms_inventory.py` → ms_inventory_current.txt / ms_inventory_last.txt): **47 claims in the current log, 25 the
  app's, 22 Play services'**; every `08 13` attributed to its claim and to the app's `Get`/`Set` by the app's own `causeOfNotify` rule (`correlate.py` →
  correlation.txt); pw_hdlc CRC-32 OK 426/426 (current) and 62/62 (`.last`), positive control `CAP-073` B 381/381 (`hdlc_crc.py`); `scripts/pwrpc_decode.py` on the
  current log (426 packets); the Play marker `03 08 00 02 01 25` 22 (current) / 7 (`.last`), positive control `CAP-066` `.log.last` 22; `08 13 00 04 01` 107 / 20; "Battery
  updated" 151; NAKs 0; Sets 5 → ACKs 5. System log: 81,044 lines; `main` from 04:45:21.795 UTC only (line 63393), `system` from 10-11 19:36, `events` from line 25050;
  **no `bt_port_api` / `cleanup_rfc_slot` / `RfcommEventStreamMedium` lines** (0 each, exit 1; positive control `NearbyDiscovery` 232); the app's processes 12456 and
  13677 both `User=0`, uid 10354; `am_wtf` FGS-allowed ×13 at the app's Connects; StrictMode 65 lines all pid 10067 (`android.process.acore`); SIGQUITs 04:45:57–04:46:15
  = `bugreportz` (04:45:41); `volume_changed` ×3 at holds; the SSID in 165 lines, the Buds' address in 47, no e-mail, no serial, no phone number.
- **Phase C (done):** `CAP-074-EVENT-NOTES.md` (metadata, pre-flight, method and privacy, the timeline before / during / after the recording, the steps BJ-1 … BJ-28,
  the Test-IDs, the checklist) and `CAP-074-FINDINGS.md` (§0 build/user/logs/privacy, §1 ACLs and the claim inventory of both clients, §2 the Connect reads byte for byte
  against `CAP-073`, §3 every write, §4 the holds, the cuts and every `Notify`, §5 playback, §6 session ends, §7 `CAP-074` vs `CAP-073`, §8 the root cause with its
  labels and the external checks, §9 six fix options, §10 open questions). External checks fetched 2026-10-12: the Fast Pair Hearable Controls, Message Stream and Device
  Information pages; AOSP `system/stack/rfcomm/port_api.cc` (`PORT_ALREADY_OPENED`, the incumbent's handle returned) and `system/bta/jv/bta_jv_act.cc`
  (`RFCOMM_RemoveConnection(handle)` on failure); the Android `BluetoothSocket` reference page could not be read through the fetch tool (its text came back as the
  navigation sidebar in three segments) — said so in §8.
- **Phase D (done except the commit):** the checkpoint asked and answered (verbatim below); `DECISIONS.md` ADR-032 and ADR-061 Updates; `PROTOCOL.md` §4.1 note +
  §8 row; `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group BJ section + the §9 `CAP-074` row (12 columns, analyzed); `id_registry.csv` `CAP-074` → analyzed; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
  Group column BJ on `PAIR-003`, `ANC-001`…`ANC-004`, `TOUCH-007`, `BATT-004`, `FW-003`; `TODO.md` §1 (the Buds' state changed again), §2 (the hold's "another client
  closes 0x04" branch ran twelve times), §4 (the Play re-claim 🔴 with `CAP-074`'s numbers), §5 (the fix, **M**); `ARCHITECTURE.md` §5a ("Changed by the Buds" row) and
  §6.0b (the observed contention); `ai-sessions/INDEX.md` row 0086; the two capture files updated with the checkpoint's results. `PYTHONDONTWRITEBYTECODE=1 python3
  scripts/ensure_footers.py` and `python3 scripts/lint_docs.py` → **exit 0** (the only fatal findings during the session were backticked scratch names, unquoted). Not
  changed: `CHANGELOG.md`, `README.md`, `PROJECT.md`, `APP_TESTPLAN.md` (not chosen), nothing under `android/`, no `dist/`, no release step, no FEATURE prompt.
- Scratchpad: `/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/b6892625-e45c-48de-9f95-8502dc97ed7c/scratchpad/0086/` (`ms_inventory.py`, `correlate.py`,
  `hdlc_crc.py`, `diffs.py`, the frames, sheets, clock crops, audio, the inventories and the correlation). Re-create by re-running the scripts on the folder if gone.

## Checkpoint 0 (chat 2026-10-12, `AskUserQuestion`, answers verbatim)

1. "Rename" → *"Group BJ, rename to the film's times (Recommended)"* — done: `captures/CAP-074-2026-10-12_06-40-16_06-45-31-Group_BJ`.
2. "Modes + sha" → *"chmod 644 all six + write CAP-074-sha256sum.txt (Recommended)"* — done (the checksum file lists the six committed files: two HCI logs, the export,
   the app logcat, the recording, the extract).
3. "Recording" → *"Commit as is, audio kept (Recommended)"* — done (the original also in `~/opencontrol-capture-originals/CAP-074/`, `cmp` identical).
4. "System log" → *"Filtered extract committed, full log kept locally (Recommended)"* — done (the command and its checks in `CAP-074-FINDINGS.md` §0).
   Not asked separately (settled from the logs): the Play-services user is **user 0, the Owner** (`am_proc_start … User=0`); the APK pull was not made (⚪ in §0).

## Checkpoint D (chat 2026-10-12, `AskUserQuestion`, answers verbatim)

1. "Root cause" → *"FACT for the contention; HYPOTHESIS for the stack path in this run (Recommended)"* — as written in `CAP-074-FINDINGS.md` §8.
2. "ADR Updates" → *"Add both Updates (Recommended)"* — the two texts of the preview are in `DECISIONS.md` (ADR-061, ADR-032; decisions unchanged).
3. "Fix option" → the maintainer's own words: *"Een switch opnemen in settings tabblad, die switcht tussen optie 1 (B + C: a visible hint + a known-issue line, no
   re-claim) en optie 2 (A: re-claim after a foreign close while the tab is shown, with a back-off), zodat de gebruiker zelf kan kiezen wat het beste werkt."* ("Put a
   switch on the Settings tab that switches between option 1 … and option 2 …, so that the user can choose what works best.") — recorded in `TODO.md` §5 (**M**, a
   FEATURE prompt of its own), `CAP-074-FINDINGS.md` §9, ADR-061's Update, the Group BJ section and the registry row. The FEATURE prompt was drafted after the
   maintainer's answer to the second question of the commit checkpoint ("Yes, draft 0087 now (Recommended)"): `ai-sessions/0087_FEATURE_PROMPT_2026_10_12.md`, with
   its planned row in `ai-sessions/INDEX.md`.
4. "Docs" → *"Registers + TODO + ARCHITECTURE (Recommended), PROTOCOL.md §4.1 note (no status change)"* — done; the CHANGELOG Known-issues line and the README
   sentence were not selected and are not written.

## Files read

- **In full:** `AGENTS.md` (system context), `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `DECISIONS.md` (ADR-001 … ADR-061 with every Update, the draft ADR-060),
  `TODO.md`, `AI_SESSION_LOG_PROCEDURE.md`, the prompt, `CAP-073-EVENT-NOTES.md`, `CAP-073-FINDINGS.md`, `ai-sessions/0084_FEATURE_RESULT_2026_10_10.md`,
  `ai-sessions/0085_CAPTURE_RESULT_2026_10_10.md`, `PROTOCOL.md` §2.3 and §4.1, the Group BI section of `CAPTURE_BLUETOOTH_HCI_SNOOP.md`; Kotlin: `BudsRepositoryImpl.kt`,
  `RfcommBudsTransport.kt`, `SessionReopener.kt`, `AncScreen.kt`, `BleLogger.kt`; the debug export, the app logcat, every RFCOMM frame of both HCI logs on handle
  `0x000b` through the inventory, every second of the film (viewed or proved unchanged).
- **In part:** `ai-sessions/0039_FEATURE_RESULT_2026_09_19.md` (§2.4–§2.6, §8), `OpenControlNavHost.kt` (lines 200–452), `MainActivity.kt` (330–449 and the `onAppVisible`/
  `setAncTabShown` call sites by `grep`), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the eight Test-ID rows), `CHANGELOG.md` (`[1.2.0]` Known issues), `RELEASING.md` (the
  headings, C4), `ai-sessions/INDEX.md` (the last rows), `CAP-072-FINDINGS.md` §0 (the extract command), `PROTOCOL.md` §8 (the 2026-10-10 rows); the system log in the
  run's and the pre-run's windows by the stated, positive-controlled filters (per-minute counts, the app's pids, uid 1002, Nearby/Fast Pair, `am_wtf`, StrictMode,
  SIGQUIT, `volume_changed`, the privacy patterns), not line by line.
- **Not read by the main session** (contrary to the prompt's list): `ai-sessions/0084_FEATURE_PROMPT_2026_10_10.md`, `ai-sessions/0085_CAPTURE_PROMPT_2026_10_10.md`,
  `CAP-071-EVENT-NOTES.md`/`-FINDINGS.md` (the `CAP-073` files, which follow them, served as the layout), `CAP-072-FINDINGS.md` beyond §0, `CHANGELOG.md` `[1.2.0]` beyond
  Known issues, `RELEASING.md` §11a/§13 bodies, `PROTOCOL.md` §2.2a's Updates and §4.3 Options B/F, `AncTileService.kt`, `BudsRepository.kt`, `BudsTransport.kt`.
  **A read-only subagent** read `ai-sessions/0040_FEATURE_RESULT_2026_09_19.md`, `CAP-059`/`CAP-061`/`CAP-065`/`CAP-066`/`CAP-067` FINDINGS and reported the Play-services
  claim evidence (its report is in the chat); every number of it used here was re-derived from `CAP-074`'s own logs, and the earlier captures' figures (2.7–5.0 s,
  1.7–3.1 s, "stopped re-claiming") are cited as those files' statements, not re-derived (an **S** mark in the sense of `AI_SESSION_LOG_PROCEDURE.md` §4b item 4).

## Deferred documentation (each also in `TODO.md`)

- `TODO.md` §5: "**M** — the Play-user hold fix (`CAP-074`, `ai-sessions/0086`; the maintainer's choice in chat 2026-10-12): a switch on the Settings tab … Its own FEATURE
  prompt (`ai-sessions/0087`) …" (new).
- `TODO.md` §1: "**`CAP-074` … changed that state again:** the Buds now hold a custom EQ … and **all four** press-and-hold modes … — restore if wanted (**M**)." (new).
- `TODO.md` §2: the hold item's addition "… **`CAP-074` … ran the 'another client closes DLCI 0x04 while the tab is shown' branch twelve times** … Still not run: the tile,
  Home, a session loss / ADR-044 re-open with the tab shown, the (i) line." (changed).
- `TODO.md` §4: "🔴 Why Play services stops re-claiming DLCI 0x04 … — it did **not** stop in `CAP-074` … 20 of 20 recoveries 5.2–5.7 s …" (changed); the standing item
  "Record Play services' *Nearby devices* permission state and the phone's Android version … in every capture" (§3) applies to this run too — not recorded again.
- Open from this session, in `CAP-074-FINDINGS.md` §10 (not separately in `TODO.md`): the stack's lines for the cuts (a repeat with the system log pulled within a
  minute), the installed APK's hash, the 06:31:07 local-host ACL end, the volume steps at holds.

## Commits

The maintainer's answer (chat 2026-10-12, `AskUserQuestion` "Commit"): *"Commit + push + open the pull request"*. Per concern, on `capture/cap-074`
(Git LFS covers the capture files: `.gitattributes` `captures/**` `.log`, `.log.last`, `.mp4`, `.txt`):

1. `31ee819` `docs(capture): CAP-074 (Group BJ) — the released 1.2.0 in the user with Play services, analysed` — the capture folder (two notes, two HCI logs, the export, the
   app logcat, the extract, the recording, the checksum file).
2. `1343e97` `docs: record CAP-074 — ADR-061/ADR-032 Updates, PROTOCOL §4.1 note, registers, test plan, TODO, architecture` — `DECISIONS.md`, `PROTOCOL.md`,
   `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `id_registry.csv`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `TODO.md`, `ARCHITECTURE.md`.
3. `docs(ai-sessions): 0086 CAPTURE result, 0087 FEATURE prompt, INDEX rows` — this file, `ai-sessions/0087_FEATURE_PROMPT_2026_10_12.md` and `ai-sessions/INDEX.md`.

Then `git fetch` + rebase on `origin/main`, push without force, the pull request opened with `gh pr create --base main`; never merged by this session. *(The hashes
and the PR number are back-filled below once they exist; the next session completes the list, `AI_SESSION_LOG_PROCEDURE.md` §4b item 2.)*

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0086_CAPTURE_RESULT_2026_10_12.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0086_CAPTURE_RESULT_2026_10_12
