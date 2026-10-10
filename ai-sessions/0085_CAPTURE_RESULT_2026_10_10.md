# 0085_CAPTURE_RESULT_2026_10_10.md — Full analysis of CAP-073 (Group BI: the rebuilt 1.2.0 release APK, build 5b4d5db, installed over the 1.2.0 build ec6d163 in the GrapheneOS user without Google Play services)

**Number:** 0085
**Category:** CAPTURE
**Date:** 2026-10-10
**Title:** Fully analyse `CAP-073`: the screen recording, two HCI snoop logs, the debug export, the app logcat, the system log and the shell log; the
release verdict for the rebuilt 1.2.0
**Status:** awaiting maintainer sign-off (every checkpoint answered in chat 2026-10-10; the commit question is open)


## Summary (plain language)

**What the rebuilt 1.2.0 (`5b4d5db`) did on the phone** (`CAP-073`, 2026-10-10, the user without Google Play; you tested only what changed):

1. **The update worked and kept your settings.** It went over `ec6d163` at 08:27:05; dark mode and Debug mode stayed on, and no permission dialog appeared. The
   APK on the phone is **byte-identical to the build you kept** (pulled after the run: SHA-256 `ac04415e…a67220`).
2. **The noise-control tab now keeps the channel open (ADR-061).** Opening the tab claimed the channel once and kept it for 31 s. Your four press-and-holds came
   in as the Buds' own reports, and the screen changed mode without a tap each time (Adaptive → Transparency → Noise cancellation → Adaptive). Your four taps used
   the open channel. Leaving the tab released it 1.509 s later. `CAP-072`'s gap is closed. Not seen: the (i) line "Changed by the Buds at …" — the (i) was not
   opened.
3. **The balance slider** shows the finger's value while dragging ("Right 4 — release to set"), writes once per release, and reached "Right 4" on the third
   release (`CAP-072`: about 30 writes). The Buds now stand on **Left 4**.
4. **Nothing else changed:** all 11 connections sent the same thirteen requests, byte for byte as `CAP-072`; 6 writes and 4 noise-control changes were answered;
   no crash, no ANR.
5. **About "during playback":** the Bluetooth log shows no music playing during the recording (AVRCP Stopped/Paused, the phone's volume set to 0 %), and no audio
   stream during the four holds. The mode changes themselves are on the wire and on screen, as you said.

**Verdict (yours, chat 2026-10-10): OK — ready to release.** Not tested on hardware: the hold with the Quick Settings tile, with Home, after a reconnect, and the
(i) line — now items in `TODO.md` §2.

**What you do next:** answer the commit question. Then `RELEASING.md` D1–D4: merge PR #27 with **"Create a merge commit"**, tag **`5b4d5db`** (`git tag -s v1.2.0
5b4d5db -m "OpenControl for Pixel Buds Pro 2 1.2.0"`), publish the draft release from `~/opencontrol-1.2.0-tested`, and check the download on the phone. After
that, E1–E4 (the CHANGELOG date and README in a small pull request, the Release log row, closing this RESULT and `TODO.md`, the release-notes template).

## Progress

- **Phase 0 — in progress.** Started 2026-10-10 in the same chat that wrote the prompt (the maintainer: "voer prompt ai-sessions/0085_CAPTURE_PROMPT_2026_10_10.md
  uit. Met de resultaten van CAP-072 moet dit genoeg zijn voor een release." — "Run the prompt. With the results of CAP-072 this should be enough for a
  release."). `git log -1`: `f46404a docs(ai-sessions): 0085 prompt — full analysis of CAP-073 (Group BI)`. `git fetch`; `HEAD..origin/main` empty;
  `origin/release/1.2.0-rebuild..HEAD` empty (34dbd7a and f46404a pushed). `git status --short`: `android/domain/bin/` (build output) and the eight CAP-073
  files, untracked.
- Scratchpad: `/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/4a71de74-9f20-415c-b034-27f2468f2216/scratchpad/0085/`.
- **Reading (done, before any analysis):** in full — `AGENTS.md` (system context), `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `AI_SESSION_LOG_PROCEDURE.md`,
  the committed `CAP-073-EVENT-NOTES.md` skeleton (`git diff HEAD` on it: empty), `CAP-072-FINDINGS.md`, `RELEASING.md` checklist + §11/§11a, `APP_TESTPLAN.md`
  §W. In part — `PROTOCOL.md` (§0–§2.2a start, §4.1 start, §4.5.7, §8's last rows; the rest not read — 417 kB), `DECISIONS.md` (ADR-032, 044, 049, 058, 059, 060, 061
  in full with every Update; the other ADRs by heading only). Not yet: `CAP-072-EVENT-NOTES.md`, `CAP-071-*`, the Kotlin sources.
- **Phase 0 step 2 (listing):** `ls -la` + `sha256sum` saved (sha_initial.txt in the scratchpad); modes now: three files 750 (export, app logcat, screen recording), the others 644.
- **Phase B (started early, read-only):** the debug export E (681 lines, 08:27:07.086–08:32:20.679, read in full) and the app logcat L (471 lines, UTC, read in
  full; its 260 OpenControlBuds lines from 08:31:38.986 equal E's lines except a 1-ms rounding on 14; 7 lines after E's end: the export at 08:32:54.338 "681 lines").
  The system log reaches 2026-10-09 only with kernel `trusty` lines (no 18:33:54 install record).
- **Checkpoint 0 (chat 2026-10-10, `AskUserQuestion`, answers verbatim):** "Renames" → *"Rename as listed (Recommended)"*; "Film privacy" → *"Blur
  Wi-Fi only (Recommended)"*; "System log" → *"Filtered extract (Recommended)"*; "APK pull" → *"I attach the phone now"*.
- **Phase A (film) — viewed in full:** 589 frames at 1 fps; 447 seconds proved unchanged below the status bar (pixel diff > 40 only in the timer rows; scratch
  `diffs3.py`), the other 141 + the first viewed on 36 labelled sheets; clock offset phone = 08:23:35.34 + film t (minute change 08:28:00 between film 264.650 and
  264.667; 08:24:00, 08:27:00, 08:31:00 consistent within 0.36 s); the hold end checked frame by frame (Sound tab content at film 294.029 = 08:28:29.37 vs the log's
  08:28:29.381).
- **Phase B — HCI:** inventory of both logs on handle 0x000b (`scripts/inventory.py` in the scratchpad), CRC-32 OK 381/381 (current) and 448/448 (.last); every
  phone MAESTRO request of the run (171 frames, 36 distinct) compared with CAP-072/070/058; Play-services marker 0 (run) / 14 (.last, Owner user) / 22 (CAP-066).
- **Phase 0 done:** the installed APK pulled from user 10 (`adb shell pm path --user 10` → one `base.apk`, the same path for user 0): SHA-256
  `ac04415e…a67220` = `~/opencontrol-1.2.0-tested` (B1). `dumpsys`: user 0 `firstInstallTime=2026-10-09 18:33:54`, user 10 `17:24:41`. Folder `git mv` to
  `CAP-073-2026-10-10_08-23-35_08-33-24-Group_BI`; `CAP-0731-…` → `CAP-073-btsnoop_hci.log.last`; the film → `CAP-073-screen.mp4`, committed blurred (top 820 px,
  569.5–575.5 s and 586.5–589.4 s); the original film and the full system log copied to `~/opencontrol-capture-originals/CAP-073/` (`cmp` identical), the full
  log removed from the folder; `CAP-073-logcat-extract.txt` (1,360 lines; e-mail, SSID, serial hex, MAC: 0 each). References in `APP_TESTPLAN.md` and
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` updated.
- **Next:** write CAP-073-EVENT-NOTES.md (skeleton as appendix) and CAP-073-FINDINGS.md; Phase D checkpoint.
- **Phase C done:** CAP-073-EVENT-NOTES.md (352 lines; the skeleton as appendix, headings demoted, text unchanged) and CAP-073-FINDINGS.md (§0–§10) written.
  External check: the Fast Pair Hearable Controls page (fetched 2026-10-10): "If the user changes the setting via headset gesture or companion application … the
  Provider should also send notification to all connected Seekers." The RFCOMM `DM` after the phone's `DISC`: no source names the case (search 2026-10-10) — kept 🟡.
- **Phase D — checkpoint next.**
- **Checkpoint D (chat 2026-10-10, `AskUserQuestion`, answers verbatim):** "Verdict" → *"OK — ready to release (Recommended)"*; "ADR-061" → *"Add the Update
  (Recommended)"* (the text of the preview, now in `DECISIONS.md`); "Docs" → *"All listed (Recommended)"*.

- **Phase D done:** `DECISIONS.md` ADR-061 Update; `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (the Group BI status, the §9 row: analyzed); `id_registry.csv` (CAP-073 analyzed,
  ADR-061 row); `APP_TESTPLAN.md` §W results (✅ 3, ⚠️ 6, not run 3) and the Summary; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (BI added to the Group column of the 12
  Test-IDs exercised); `TODO.md` §2 (the CAP-073 note, the hold's untested branches), §4 (restore: balance Left 4, EQ, Adaptive), §5 (the release steps left);
  `PROJECT.md` and `README.md` status lines; `ARCHITECTURE.md` §5a ("Changed by the Buds": main path hardware-verified); PR #27 C1–C4 ticked; `ai-sessions/0084`
  RESULT Commits back-filled (`34dbd7a`, `f46404a`) and its Status closed (§4a); `ai-sessions/INDEX.md` rows 0084/0085. `ensure_footers` (3 files) and
  `lint_docs.py` exit 0 — after fixing four dead references (the checksum file created; two scratch names unquoted; in the 0085 PROMPT the placeholder skeleton path
  unquoted with "(the placeholder folder, renamed by this session)").

## Files read

- **In full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `AI_SESSION_LOG_PROCEDURE.md`, the committed CAP-073 skeleton, `CAP-072-FINDINGS.md`,
  the debug export E (681 lines), the app logcat L (471 lines; the hex lines checked by `diff` against E), the shell log, every second of the film (§A), both HCI
  logs through the inventory (every RFCOMM frame on handle `0x000b`), `BudsRepositoryImpl.kt` lines 340–500 (`causeOfNotify`, the hold) and the constants.
- **In part:** `PROTOCOL.md` (§0–§2.2a, §4.1's start, §4.5.7, §8's rows; not the rest), `DECISIONS.md` (ADR-032, 044, 049, 058, 059, 060, 061 in full; the others
  by heading), `RELEASING.md` (checklist, §11, §11a), `APP_TESTPLAN.md` (§W, Summary), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BI, §9 rows), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
  (the 12 Test-ID rows), `TODO.md` (§2, §4's restore item, §5), `CAP-072-EVENT-NOTES.md` (header, method, the first timeline rows), the system log (the run window
  by the stated `grep`/`awk` filters with a positive control, not line by line; before 08:22 only the app's and the user-switch lines).
- **Not read** (contrary to the prompt's list): `CAP-071-EVENT-NOTES.md`/`-FINDINGS.md` (the `CAP-072` files, which follow them, served as the layout), the 0084
  PROMPT, the Kotlin files other than `BudsRepositoryImpl.kt` (`EqScreen.kt` cited from `ai-sessions/0084`, not re-read).

## Deferred documentation

- `TODO.md` §2: "The ADR-061 hold's other branches on hardware … the tile … Home and return … a session loss and the ADR-044 re-open while the tab is shown … and the
  noise-control (i) after a press-and-hold" (new); T11, C12, S12, H5, Volume EQ, the worn line with in-ear detection off (kept).
- `TODO.md` §4: "Restore the Buds … balance Left 4 …" (rewritten).
- `TODO.md` §5: "release the rebuilt 1.2.0: … Left: D1–D4 … and E1–E4" (rewritten).

## Commits

Not committed yet — the question is in the chat (`AI_SESSION_LOG_PROCEDURE.md` §4b item 2: the next session back-fills the hashes).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0085_CAPTURE_RESULT_2026_10_10.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0085_CAPTURE_RESULT_2026_10_10
