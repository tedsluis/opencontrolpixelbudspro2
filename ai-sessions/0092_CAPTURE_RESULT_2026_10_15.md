# 0092_CAPTURE_RESULT_2026_10_15.md — Full analysis of CAP-076 (Group BL: the 1.3.0 build `ff27ee1`, the settings stream on the app's own channel, in the Owner user with Google Play services and the secondary user without)

**Number:** 0092
**Category:** CAPTURE
**Date:** 2026-10-15
**Title:** Full analysis of `CAP-076` (Group BL): two screen recordings, four HCI snoop logs, two app debug exports, two app logcats, a system log, a full logcat and an adb shell log — part 1 (Owner, Play, 1.2.2 → 1.3.0) and part 2 (secondary user, no Play)
**Status:** awaiting maintainer sign-off (the analysis, the documents and the verdict done; the commit and push await the maintainer's yes)

## Summary (plain language first)

**What happened on the phone.** In the Owner user (with Google Play services) the maintainer started on 1.2.2, installed the tested 1.3.0 on film, and then
used the app for about 15 minutes: Disconnect/Connect, buds out of the case and into the ears during a podcast, both buds into the case and out again, 17 and
12 press-and-holds on the noise-control tab, four taps on a mode, the Quick Settings tile added and tapped nine times, the equalizer and in-ear detection
changed. In the secondary user (no Play) the film starts with a Bluetooth off/on; then Info, a Disconnect/Connect, a battery refresh, four taps, 12
press-and-holds on the noise-control tab, the equalizer, in-ear detection off, Ring Left and Stop. The installed APK is the tested build (SHA-256 checked by
`adb pull` after the run).

**Did every hold show?** On the noise-control tab, yes — **29 of 29 with Play services holding the shared channel, 12 of 12 without**, each on screen within a
tenth of a second of the Buds' push (measured frame by frame at 10 fps). Play received the Message Stream `Notify` of every hold in the Owner user; the app got
the same change on its own channel, as ADR-064/065 intended. Thirteen changes made on the Connection tab (holds and bud-in/out during the podcast) reached the app
too, but that tab shows no mode, so they cannot be "seen"; the maintainer's "the ANC mode in the app changed too" holds for every hold the screen can show.

**What the two new requests did on the wire.** One `SubscribeToSettingsChanges` and one `ReadSetting 4:13` in every MAESTRO session that got through its reads
(12 of 12; a 13th was closed by the Buds after six reads) — byte-identical to the official app's, in the planned place; a full session now sends seventeen
requests. With both buds out of the case the Buds push every change **twice** (channel 21 and 19) — harmless for the app, an open question for the protocol.

**What did not work as designed (minor).** (a) After a reconnect the "Changed since the app last saw it" line is overwritten 0.1–1.5 s later by the second,
equal read (12 of 12 sessions in that order) — it never shows; (b) one of 13 taps made while Play was on the channel (a tile tap) was cut right after the Buds'
answer and changed nothing without a message; (c) a log line shows the old cause for a same-mode push. Taps with Play present are still slower (8 of 12 claims
needed a second attempt) — 1.3.0's known issue, now measured. No crash, no wrong write, no Safe Mode.

**What the maintainer decided (chat 2026-10-15, verbatim in "Checkpoints"):** release 1.3.0 as built with the Known-issues lines (C4 minor issue); the ADR-065
Update and a `PROTOCOL.md` §4.1 note written; no FEATURE prompt now (P1–P6 in `TODO.md` §5); four analysis scripts committed; exports and films as is; the
folder renamed. **Next for the maintainer:** the commit/push answer, then `RELEASING.md` D1–D4 (merge, tag, publish) — never by this session.

## Progress

- **Started** 2026-10-15. Branch `feature/settings-stream` at `f41f970` (the 0092 prompt commit, after the build commit `ff27ee1`), level with
  `origin/feature/settings-stream`; `origin/main` at `c1d898b`; PR #56 open. `git status --short`: the fourteen untracked capture files and
  `android/domain/bin/` only.
- **Phase 0 step 2 done:** the folder listed fresh — all 14 files and their first eight SHA-256 hex digits equal the prompt's §2 tables; modes as stated
  (seven files 750, the rest 644). **Difference:** the empty file `test` in the repository root that §2 names is **not present** (`ls -la test`: "No such
  file or directory") — nothing to ask at Checkpoint 0 for it.
- **Scratch directory:** /tmp/claude-1000/cap076.0VWc (scripts, contact sheets, decoded tables).
- **Reading done** (Phase 0): AGENTS.md, PROJECT_RULES.md, PROJECT.md, DECISIONS.md (ADR-001 … ADR-065, every Update) and AI_SESSION_LOG_PROCEDURE.md in
  full; ARCHITECTURE.md §3.1, §3.2, §5a (the cause row), §6.0b in full; PROTOCOL.md §2.2a (the L-1 Updates), §4.1 (head, the 2026-10-04 … 2026-10-14
  Updates/Notes), §6 (the 2026-09-28/10-04/10-14 items), §8 (last rows); TODO.md §1, §5 and the CAP-076/Nearby lines; the 0091 RESULT in full; the CAP-076
  skeleton in full; CAP-075-FINDINGS.md in full. Still to read when a finding touches them: RELEASING.md C3/C4, the Kotlin sources (log strings), CAP-075
  EVENT-NOTES method, CAP-071 layouts, CAP-074 §8, CAP-051/056/038 sections.
- **Checkpoint 0 answered** (chat 2026-10-15, verbatim in "Checkpoints" below).
- **Phase 0 done.** Originals: the 14 files copied with `cp -a` to ~/opencontrol-capture-originals/CAP-076/ (sorted `sha256sum` lists of source and copy
  `diff` → identical). `chmod 644` on the seven 750 files (all 14 now 644). **Phone checks (07:52–07:53, read-only; output verbatim in the scratch file
  CAP-076-adb-checks-0092.log, to be committed into the folder):** `pm path --user 0` and `--user 10` give the same `base.apk` (exit 0 each); `versionCode=10300`,
  `versionName=1.3.0`, `lastUpdateTime=2026-10-15 06:36:57` (unchanged since the run — no install after it); the pulled APK's SHA-256 **= `03272c6f…a96c` = the
  tested build's** (`~/opencontrol-1.3.0-tested/….apk.sha256`). Play services `versionName=26.32.34 (260400-968093310)`; `User 0: installed=true`, `User 10:
  installed=false`; in user 0 `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `BLUETOOTH_ADVERTISE` (the *Nearby devices* group) `granted=true` — **the state at 07:53, an
  hour after part 1** (no grant change in between is shown; ⚪ that it was the same during the run).
- **Phase A, film 1 done** (scratch: f1/ 981 frames at 1 fps, diff1.txt, chg1.txt, sheets/f1_*.jpg, notes_film1.md): 307 seconds with a change below
  the status bar, all viewed on 39 labelled sheets; the other 674 pixel-identical (|Δ| ≤ 25 everywhere below the top 30 px). **Clock:** the status bar's minute
  flips at film t = 33.8, 93.7, 153.7, 453.7, 933.7 s (10-fps crops, flip.sh) ⇒ phone time = 06:35:26.3 + t (± 0.1 s), no drift; the film covers
  06:35:26.3–06:51:47.3; `creation_time` 06:52:11 is 24 s after the film's end (neither start nor end). Cross-check: the 1.3.0 relaunch on film s94 = 06:37:00.3,
  the export's "Permissions (start)" 06:37:00.476. **Film 2 has no audio stream** (`ffprobe -show_entries stream=codec_type` → video only; film 1: video +
  audio) — the prompt's "an audio track" for recording 2 is not so.
- **Phase A, film 2 done** (f2/, diff2.txt, sheets/f2_*.jpg, notes_film2.md): 168 changed seconds viewed on 21 sheets, 181 pixel-identical. **Clock:** flips
  at t = 28.5, 88.5, 148.5, 208.5 s ⇒ phone time = 07:02:31.5 + t (± 0.1 s); the film covers 07:02:31.5–07:08:20.4 — `creation_time` 07:08:20 is its end. The
  film starts 0.5 s before the Bluetooth panel's off (s8, 07:02:39.5 — the export's 07:02:39 OFF) — it does **not** cover the part-2 process start (07:01:03),
  the first session (07:01:07–07:01:08) or the second (07:01:13–07:02:39); the screen recorder was "Scherm opnemen" (screen only, no audio).
- **Phase A, audio done:** film 1 (a1.wav, sil1.txt, audio1_segments.txt, rms1.txt): 31 non-silent segments at −40 dB / 0.3 s (95 at −50 dB, 20.9 s in
  all); median RMS −69 dB per 100 ms. Ten voiced 0.5-s segments 06:40:48–06:41:53 (f0 200–420 Hz with harmonics, spectral flatness 0.15–0.27 — speech-like,
  words not intelligible to the analyst), four 06:42:11–06:42:18 (110–220 Hz), the rest short clicks (flatness ≥ 0.5). Film 2 has no audio stream.
- **Phase B, HCI logs (in progress):** scratch bin/hdlc.py (CRC-32 per pw_hdlc frame; positive control: the CAP-051 1177 reference passes, one flipped bit
  fails) → rpc_hci1.log.tsv 466 frames / rpc_hci2.log.last.tsv 39 / rpc_hci2.log.tsv 153, **0 CRC failures**, counts equal to scripts/pwrpc_decode.py
  --handle 0x000b (466/39/153). ctl_*.txt (RFCOMM control frames), bin/claims.py → claims_*.txt (DLCI 0x04: hci1.log 40 claims = 22 app + 18 Play; hci2.log.last
  1 app; hci2.log 10 app incl. 2 Ring/Stop). hci1.log.last: 0 frames on handle 0x000b, 0 RFCOMM (positive control: hci1.log 3,670 / 2,556).
  **Lead found:** with both buds out of the case every field-13 change arrives as two SERVER_STREAM packets, ch 21 and ch 19 (s13_*.txt), single while one
  bud is in the case — the export's paired lines; 🟡 to be stated with the runtime-info charging flags.
- **Phase B, exports and app logcats read:** E1 1,061 lines (444 hex, 289 pw_rpc, 328 other — every "other" line read; the hex lines are the received
  bytes, matched to the wire by script), E2 461 (201/120/140, all "other" read). App logcat 1: PID 10952 (1.2.2) 26 lines 06:35:43–06:36:57, PID 11800
  (1.3.0) 578; app logcat 2: PID 4104 (1.2.2, user 10, 06:08:17–06:13:05) 20 lines, PID 18179 (1.3.0, user 10, from 07:01:02.56) 424. FATAL 0, ANR 0,
  StrictMode 0 in both (positive control: the tag OpenControlBuds 278 / 94); "A resource failed to call close." ×7 in logcat 1 (finalizer thread, 06:47:10,
  06:47:44), 0 in logcat 2. **The exports carry raw hex lines** (Debug mode on) — the prompt's "no raw hex lines" is not so; they hold the serials
  (GetHardwareInfo answers).
- **Phase B done.** Full logcat (part 1 window 06:35–06:53, win1.txt 94,164 lines): the update (`PackageManager` installation completed 06:36:58.015;
  `am_kill … killDueToPackageUpdate` of 4104 (user 10, 1.2.2) and 10952 (user 0) at 06:36:57.93/.94; `am_proc_start` 11800 06:36:59.954); 15 `already at
  opened state` (11 the app's refused attempt 1, 4 Play's connect while the app held the channel — the four on-demand closes); Play's timers recovery
  5.13–5.97 s (17), retry 2.37–2.50 s (3), 4.49 s once after "Retry times remaining 4". The extract CAP-076-logcat-extract.txt (scratch, 14,121 lines, 9
  GetHardwareInfo hex lines redacted; 0 e-mail, Wi-Fi name, real MAC or serial) is ready. Wire: all 12 subscriptions and 12 `ReadSetting 4:13` byte-identical
  to the references, after `SubscribeRuntimeInfo`, before `GetHardwareInfo`; every `ReadSetting` field one byte form per channel; 17 MAESTRO requests per
  full session (the skeleton's "fifteen" is the 1.2.x count + 2 miscounted); writes P1 3 / P2 2 (EQ, in-ear off/on; P2 ends with in-ear detection **off**),
  all OK; 0 `WriteSetting 4:{13:n}`. Holds vs film at 10 fps (btn.py over the button crops): P1 ANC tab 29/29 within ≤ 0.1 s, P2 12/12; P1 13 changes on the
  Connection tab (no mode shown there; the worn line's checked time moves); 3 wear-driven switches coincide with the Buds' AVRCP PAUSE/PLAY (2–7 ms). Own
  Sets 15/15 ACKed, the stream 55–246 ms after. **Pairs:** every change twice (ch 21 + ch 19) with both buds out (🟡 H1 per-core push; conflicts with the
  official app's single packets — open). **Code findings:** (a) `causeOfReadAnswer` returns READ for an equal read and so overwrites the snapshot Get's
  CHANGED_SINCE_LAST_READ 0.4–0.7 s later (S4 06:41:33, S7 06:43:05 — wire 4094→4135, 5991→6003); (b) the log prints the kept cause for a same-mode push
  (06:47:07/18 "set_by_app"); (c) a tile tap whose claim is cut right after the Get's answer sent no Set and showed nothing (06:46:08.96; 🟡 cause).
  Kotlin read in full: BudsRepositoryImpl.kt; in part: RfcommBudsTransport.kt (send), AncTileService.kt (onClick).
- **Phase C done:** CAP-076-EVENT-NOTES.md (the timeline of both parts, the steps as run, the Test-IDs; the skeleton as the appendix) and CAP-076-FINDINGS.md
  (§0–§13) written; numbers re-checked (the pairs 68 of 68, the read order 12 of 12, the release 1.507–1.516 s). External validation: no new specification
  claim needed a fetch — the user type, `0x13`/`0x16`, the one-connection-per-channel behaviour and the pw_rpc packet types are cited from the fetches recorded in
  `CAP-075-FINDINGS.md` §0, §5, §7 and `scripts/pwrpc_decode.py`.
- **Phase D checkpoint answered** (verbatim below).
- **Phase D documents done:** the folder renamed (`git mv`; the two full system logs removed from it after a hash check against the originals; the listing
  diffed before/after); CAP-076-logcat-extract.txt, CAP-076-adb-checks-0092.log, CAP-076-sha256sum.txt (13 data files) added; the four scripts written and
  checked to reproduce the scratch results byte for byte (tables, claims, 57 + 18 events, the button reads); `id_registry.csv` (CAP-076 analyzed),
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (the §9 row, the Group BL status, "seventeen"), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (BL on 14 Test-IDs), `APP_TESTPLAN.md` §Y
  (results, the Summary row 22 / 10 / 0 / 7 / 5, the run note), `TODO.md` §1/§3/§5, `ARCHITECTURE.md` §5a/§6.0b, `DECISIONS.md` ADR-065 Update,
  `PROTOCOL.md` §4.1 note + §8 row, `CHANGELOG.md` `[1.3.0]` Known issues, `PROJECT.md` status, `_sidebar.md`, `ai-sessions/INDEX.md` (0091 back-fill, 0092).
  The kept ~/opencontrol-1.3.0-tested/release-notes.md got the Known-issues paragraph (the APK's SHA-256 re-checked: OK).

## Checkpoints

**Checkpoint 0 (chat 2026-10-15, `AskUserQuestion`):**

| Question | Answer (verbatim) |
|---|---|
| Users | "Yes, as read (Recommended)" — part 1 the Owner (user 0) with Play, part 2 the secondary user (user 10) without Play |
| Folder name | "Rename after Phase A (Recommended)" — `git mv` to CAP-076-2026-10-15_HH-mm-ss_HH-mm-ss-Group_BL with the measured film times; the recordings keep their names |
| Modes+sha | "Both (Recommended)" — chmod 644 on the seven 750 files; CAP-076-sha256sum.txt |
| Films | "As is, audio kept (Recommended)" |
| System logs | "Extract committed, full local (Recommended)" — CAP-076-logcat-extract.txt (part 1's window, the app/Bluetooth/Play lines, redacted); the two full files moved out of the repository |
| Originals | "~/opencontrol-capture-originals/CAP-076/ (Recommended)" |
| Phone | "Yes, attach it now" — read-only adb checks: the installed APK's SHA-256 and Play's *Nearby devices* permission and version |

**Phase D checkpoint (chat 2026-10-15, `AskUserQuestion`):**

| Question | Answer (verbatim) |
|---|---|
| Verdict C4 | "Release as built + Known issues (Recommended)" — C4 minor issue; Known-issues lines for (a) the "changed since" line overwritten after a reconnect and (b) the reworded tap/collision line, in `CHANGELOG.md` `[1.3.0]` and the kept release notes |
| ADR-065 Upd | "Write it as drafted (Recommended)" |
| PROTOCOL | "Add the §4.1 note + §8 row (Recommended)" — no status change; §6's wear item not settled |
| Next step | "Not now" — P1–P6 recorded in `TODO.md` only |
| Scripts | "Commit 4 scripts (Recommended)" — scripts/pwhdlc_crc_table.py, scripts/message_stream_claims.py, scripts/stream_events.py, scripts/film_mode_buttons.py |
| Exports | "As is (Recommended)" |
| Notes file | "Yes, edit both (Recommended)" — CHANGELOG and ~/opencontrol-1.3.0-tested/release-notes.md |
| Rename | "Rename + update refs (Recommended)" — CAP-076-2026-10-15_06-35-26_07-08-20-Group_BL |

**Commit checkpoint (chat 2026-10-15, `AskUserQuestion`):**

| Question | Answer (verbatim) |
|---|---|
| Commit+push | "Yes, commit and push (Recommended)" — four commits on `feature/settings-stream`, no force, no merge |
| Kept notes | "Yes, apply it (Recommended)" — the "**Known issues in 1.3.0:** …" paragraph added to ~/opencontrol-1.3.0-tested/release-notes.md (the APK and its .sha256 untouched, re-checked) |
| PR #56 body | "Yes, after the push (Recommended)" — B3, C1, C2, C3 ticked, the C4 verdict line |

## Files read

- **In full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `DECISIONS.md` (ADR-001 … ADR-065 with every Update, the draft ADR-060), `AI_SESSION_LOG_PROCEDURE.md`,
  this prompt, `ai-sessions/0091_FEATURE_RESULT_2026_10_14.md`, the `CAP-076` skeleton, `CAP-075-FINDINGS.md`, `BudsRepositoryImpl.kt` (1–1795), both exports
  (every non-hex line; the hex lines matched to the wire by script), the adb shell log, the kept release notes.
- **In part:** `ARCHITECTURE.md` (§3.1, §3.2, the §5a row, §6.0b), `PROTOCOL.md` (§2.2a L-1 Updates, §4.1 head and its 2026-10-04 … 2026-10-14 Updates/Notes,
  §6's items of 2026-09-28/10-04/10-14, §8's last rows), `TODO.md` (§1, §3, §5, the CAP-076 lines), `RELEASING.md` (C1–C4, the release log rows),
  `CHANGELOG.md` (`[1.3.0]`), `APP_TESTPLAN.md` (§X, §Y, Summary), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BL, the §9 rows), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the
  exercised rows), `CAP-075-EVENT-NOTES.md` (its structure), `id_registry.csv` (the CAP-074 … CAP-076 rows), `RfcommBudsTransport.kt` (`send`),
  `AncTileService.kt` (the tap handler), `scripts/pwrpc_decode.py`, `scripts/anc_setting13_crosscheck.py` (its header). The two app logcats: every line of the old
  processes and every Bluetooth/System/crash line; the UI-event lines by count. The full logcat and the system log: by the stated filters over part 1's window.
- **Not read** (the prompt named them): `PROTOCOL.md` §4.5, `CAP-071-*` (the layout followed via `CAP-075`'s, which follows it), `CAP-074-FINDINGS.md` §8,
  `CAP-051`/`CAP-056`/`CAP-038` findings (only their stream packets re-checked with `pwrpc_decode.py`), `DESKRESEARCH_FINDINGS.md` 2026-10-14,
  `0088_CAPTURE_RESULT` (only its head), the Kotlin files `CodecRouter.kt`, `SettingFrame.kt`, `Maestro.kt`, `SessionReopener.kt`, `SessionDiagnostics.kt`,
  `AncScreen.kt`, `SettingsMenu.kt`, `MainActivity.kt`, `ValueCurrency.kt`, `AncModeCause.kt` — each finding of this run that touches code was traced in the files
  read in full or part; the log strings matched were all in `BudsRepositoryImpl.kt` and the transport/reopener/diagnostics lines quoted from the exports.

## Deferred documentation

- `TODO.md` §5: "After `CAP-076` … P1 keep "Changed since the app last saw it" across the reconnect's second, equal read …; P2 a tile tap whose claim closes
  after the `Get`'s answer and before its `Set` reports a failure …; P3 log a same-mode push as "unchanged" …; P4 the two packets per change …; P5 …; P6 a mode
  line on the Connection tab …" and the CloseGuard lines.
- `TODO.md` §1: "The Buds' state after `CAP-076` … in-ear detection off … restore in-ear detection if wanted (**M**)."
- `TODO.md` §3: the *Nearby devices* state "on film still open".
- `README.md` (status, Known issues) — E1 of `RELEASING.md` after the release, as `TODO.md` §1's "next: D1–D4, then E1–E4".

## Commits

Not committed — awaiting the maintainer's yes in chat (the RESULT's next session back-fills the hashes, `AI_SESSION_LOG_PROCEDURE.md` §4b).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0092_CAPTURE_RESULT_2026_10_15.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0092_CAPTURE_RESULT_2026_10_15
