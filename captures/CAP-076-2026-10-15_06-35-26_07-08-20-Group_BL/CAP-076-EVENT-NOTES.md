# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, the Owner user **with** Google Play services and the secondary user without) — Group BL, 1.3.0: the settings stream on the app's own channel (`CAP-076`)

**Status:** ✅ **captured 2026-10-15** (part 1 in the Owner user with Google Play services, film 06:35:26.3–06:51:47.3; part 2 in the secondary user without
Google Play, film 07:02:31.5–07:08:20.4) **and analyzed 2026-10-15 by `ai-sessions/0092`** (CAP-076-FINDINGS.md). The release-signed **1.3.0 build `ff27ee1`**
(versionCode 10300) was installed over 1.2.2 `bfd7891` **on film 1** (Android's "Updaten…" at 06:36:58–59). The maintainer's statement: "I did not keep to the
test plan. I tested only the changed functionality." — the steps as run are mapped to BL-1 … BL-25 below ("The maintainer's steps, as run"); the skeleton
of `ai-sessions/0091` is kept as written as the appendix. **After the run (Checkpoint 0 of `ai-sessions/0092`, chat 2026-10-15):** the seven mode-750 files
set to 644; the two phone-wide logs (CAP-076-System-log-b5e49395adf6.txt, CAP-076-logcat-all-1.txt) kept local in ~/opencontrol-capture-originals/CAP-076/ and
committed as the redacted extract CAP-076-logcat-extract.txt; the films committed as is (audio kept); the read-only adb checks of 07:52–07:53 saved as
CAP-076-adb-checks-0092.log; `sha256sum` of the folder's 13 data files (not the two notes, which later sessions may edit) in CAP-076-sha256sum.txt; the folder renamed to the films' first and last phone times (CAP-076-2026-10-15_06-35-26_07-08-20-Group_BL).

Labels per `PROJECT_RULES.md` §1: 🟢 FACT, 🟡 HYPOTHESIS, ⚪ ASSUMPTION, ❓ OPEN QUESTION. Frame numbers are per log: **H1** = CAP-076-btsnoop_hci1.log, **H1L** =
…hci1.log.last, **H2** = …hci2.log, **H2L** = …hci2.log.last; **E1** / **E2** = the two debug exports; **A1** / **A2** = the two app logcats (UTC); **L** = the full
logcat (local time; the committed extract carries the same lines); **s** = a film second (film 1: phone time = 06:35:26.3 + s; film 2: 07:02:31.5 + s). Every
`tshark` filter was run on the Buds' ACL handle `0x000b` (the Connection Complete for the Buds' address with that handle: H1 672 and 4680, H2L 311 and 605,
H2 151 and 2678; `bluetooth.addr == 04:00:6e:cf:6e:07` matches 0 frames in all four logs, `bthci_evt.bd_addr == 04:00:6e:cf:6e:07` matches each of those
Connection Completes). Modes: Message Stream byte `20` Off, `08` Noise cancellation (ACTIVE), `80` Transparency, `40` Adaptive; setting 13: 1 Off, 2 Noise
cancellation, 3 Transparency, 4 Adaptive, 0 no mode settable.

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-076` |
| Group(s) | BL |
| Date | 2026-10-15 — part 1 06:35–06:52, part 2 07:01–07:08 (local, CEST) |
| Phone | Pixel 9a "tegu", GrapheneOS, Android 17 — `osVersion: google/tegu/tegu:17/CP3A.261005.005/2026100601:user/release-keys` (A1/A2 line 2); "17 (CP3A.261005.005)" in Quick Settings (film 1 s595) |
| Users | **part 1 = the Owner (user 0) with Google Play services**: A1 has no `userType:` line; L `am_proc_start: [0,11800,10354,io.github.tedsluis.opencontrolpixelbuds,…]` 06:36:59.954; the shell log's `pm list packages --user 0` lists `com.google.android.gms` (exit 0); 18 Play claims in H1. **Part 2 = the secondary user (user 10) without Google Play**: A2 line 3 `userType: full.secondary`; the `--user 10` list holds only `app.grapheneos.gmscompat*` (the `grep` matched GrapheneOS's compatibility layer — exit 0 is not a positive for Play); `dumpsys package com.google.android.gms` 07:53: `User 10: … installed=false`; 0 Play markers in H2L/H2 (positive control H1: 18); "Alle 20 apps zien" without a Sandboxed Google Play entry on film 2 (s332) against film 1's Apps screen with "Sandboxed Google Play" (s956). Confirmed by the maintainer at Checkpoint 0 |
| App under test | OpenControl 1.3.0 (10300), build `ff27ee1` — on film "App: 1.3.0, build ff27ee1 (2026-10-14)", no "-dirty" (film 1 s101–107, film 2 s81–86, s321); **the installed APK pulled at 07:52 has SHA-256 `03272c6f954316d76c3ef6d1d020abd8bdb97755d7f8e165c84d3f16a9c6a96c` = the tested build's** (`~/opencontrol-1.3.0-tested`, CAP-076-adb-checks-0092.log); `lastUpdateTime=2026-10-15 06:36:57` unchanged at 07:52 (no install after the run) |
| Previous version | 1.2.2 `bfd7891` (10202) — film 1 s62–73 "App: 1.2.2, build bfd7891 (2026-10-13)"; the shell log 06:23:50 `versionCode=10202`, `lastUpdateTime=2026-10-14 18:35:00`; after the update 06:38:17 `versionCode=10300`, `lastUpdateTime=2026-10-15 06:36:57`, the two `firstInstallTime` lines unchanged (an update, no reinstall) |
| Google Play services (part 1) | present: `versionName=26.32.34 (260400-968093310)`; `User 0: installed=true`; *Nearby devices* group `BLUETOOTH_SCAN`/`BLUETOOTH_CONNECT`/`BLUETOOTH_ADVERTISE` `granted=true` — **read at 07:53, an hour after part 1** (⚪ the same during the run); Play's Fast Pair client = uid 10205, the persistent process 18871 (L) |
| Official Pixel Buds app | not used; no second phone (BL-13/BL-25 not run) |
| Buds | Pixel Buds Pro 2, `release_5.203` ×3 (Info on film; the `GetSoftwareInfo` announcements, e.g. H2 428) — no Safe Mode |
| Video files | CAP-076-recording1.mp4 (980.97 s, 1080 × 2424, video + **AAC audio**, `creation_time` 04:52:11Z = 24 s after the film's end), CAP-076-recording2.mp4 (348.94 s, **video only — no audio stream**, "Scherm opnemen", `creation_time` 05:08:20Z = the film's end); no camera films |
| Log files | four HCI snoop logs (H1L, H1, H2L, H2), two exports (E1 1,062 lines, E2 462 — the files end without a newline, `wc -l` gives 1,061/461; the toasts said "1062" / "462" lines), two app logcats (A1 731 lines, A2 585), the GrapheneOS "System log" (part 1, 94,423 lines), `adb logcat -b all` (part 1, 213,298 lines), CAP-076-adb-shell.log (P0/P1) |

## Capture-integrity pre-flight

| File | Measured (2026-10-15 07:20 / 07:45) | Covers |
|---|---|---|
| H1L | 7,684 packets, 06:13:09.488 – 06:35:34.956 (`capinfos -c -a -e`); **0 frames on handle `0x000b`, 0 RFCOMM frames** (positive control H1: 3,670 / 2,556); ends with LE handle `0x0041` Disconnection Complete reason `0x16` at 06:35:34.729 (7647) | before film 1 and its first 8 s: the Buds were not connected (film 1 s4–7: "Opgeslagen"); the Bluetooth **off** of film 1 s8 rotated the log |
| H1 | 10,617 packets, 06:36:11.035 – 06:54:29.229 | part 1 from the Bluetooth on (film s44, 06:36:10.3): ACL 672 06:36:19.895 → Disconnection Complete 4558 06:42:23.192 (`0x13`); ACL 4680 06:42:46.059 → open at the end |
| H2L | 1,303 packets, 07:01:04.767 – 07:02:39.805 | part 2 before film 2: ACL 311 07:01:07.655 → 489 07:01:08.972 (`0x13`, 1.3 s, no RFCOMM); ACL 605 07:01:13.785 → 1294 07:02:39.649 (`0x16`, the Bluetooth off; LE `0x0040` at 1295) |
| H2 | 3,070 packets, 07:02:44.098 – 07:11:10.545 | part 2 from the Bluetooth on: ACL 151 07:02:45.890 → 2666 07:08:33.441 (`0x13`, after the film); ACL 2678 07:10:20.388 → open (after the film) |
| E1 | 06:37:00.476 – 06:50:55.279; one process (A1's 11800) | the 1.3.0 process of part 1 from its first line |
| E2 | 07:01:03.577 – 07:07:33.969; process 18179 (A2) | part 2 from the process start |
| A1 / A2 | UTC 04:35:43 – 04:51:15 / 04:08:17 – 05:07:57 | A1: PID 10952 (1.2.2, 26 lines, 06:35:43–06:36:57) and 11800 (1.3.0, 578); A2: PID 4104 (1.2.2 in user 10, 20 lines, 06:08:17–06:13:05) and 18179 (1.3.0, 424). The header's `package: …:10300` is the version at export time, not that of the old processes |
| L | 10-08 21:47 – 10-15 06:56:48 (local) | part 1 (it ends before part 2: the user switch to 10 is not in it) |
| Shell log | 06:21:43 – 06:38:17 | P0 for both users (run from the Owner: `am get-current-user` 0 both times) and P1's `dumpsys` before/after; **the second `dumpsys` (06:38:17) falls inside film 1** (s171, in the unchanged stretch s159–254 — run from the laptop while the app sat on the Connection tab) |

pw_hdlc frames on DLCI 0x02/0x03 of handle `0x000b`, reassembled per (DLCI, direction), split on `7e`, CRC-32 checked (`scripts/pwhdlc_crc_table.py`; positive
control: `CAP-051` 1177's bytes pass, the same bytes with one bit flipped fail): **H1 466, H2L 39, H2 153 — 0 CRC failures**; the same counts as
`python3 scripts/pwrpc_decode.py --handle 0x000b <log>` (466 / 39 / 153). No `DM` on DLCI 0x02 from the phone; one `DM` from the Buds (H1 5621, after their
`DISC` 5616).

## Review method and privacy

- **Films:** each decoded at 1 fps (film 1 981 frames, film 2 349, scaled to 360 px); every second compared with the one before (pixel |Δ| > 25 below the
  top 30 px): **film 1 307 changed seconds, film 2 168 — all viewed** on labelled contact sheets of eight; the other 674 / 181 seconds are pixel-identical
  below the status bar. Transitions narrowed at 10 fps where a claim depends on them: the status-bar minute flips (the clock), and the four mode buttons of the
  noise-control card over every hold window (an automated read of which button is filled, `scripts/film_mode_buttons.py`, crops at full resolution — film 1 s460–530, s560–610, s792–852;
  film 2 s132–252).
- **Clocks:** film 1's minute flips at s 33.8, 93.7, 153.7, 453.7, 933.7 (first 10-fps frame with the new minute) ⇒ phone time = 06:35:26.3 + s ± 0.1 s, no
  drift; cross-check: the 1.3.0 relaunch on film (s94 = 06:37:00.3) against E1's first line 06:37:00.476. Film 2's flips at s 28.5, 88.5, 148.5, 208.5 ⇒
  07:02:31.5 + s ± 0.1 s; cross-check: the Bluetooth panel's off on film (s8 = 07:02:39.5) against E2's `ON -> TURNING_OFF` 07:02:39.124 and H2L 1294
  07:02:39.649.
- **Audio (film 1 only; film 2 has no audio stream):** `ffmpeg -vn` → 16 kHz mono; `silencedetect=noise=-40dB:d=0.3` gives **31 non-silent segments**
  (95 at −50 dB, 20.9 s in all; median RMS −69 dB per 100 ms) — the maintainer's "I spoke only very little" (a statement; the words are not intelligible to the
  analyst). Ten 0.5-s voiced segments 06:40:48.15, 52.63, 06:41:00.81, 09.30, 17.96, 27.57, 37.11, 44.18, 49.40, 52.56 (f0 200–420 Hz with harmonics, spectral
  flatness 0.15–0.27 — speech-like), four lower ones 06:42:11.04–06:42:18.60 (110–220 Hz), the rest short clicks (≤ 0.34 s, flatness ≥ 0.47). None during the
  holds on the ANC tab (06:43:14–06:44:11, 06:48:44–06:49:33).
- **Privacy:** film 1 shows the three serial numbers on Info (s62–73, s101–107) and the podcast's title in Quick Settings (s593–616, s951–952); film 2 shows
  **the Wi-Fi network's name** in the Quick Settings tile (s0–4, s15, s327–328) and the serials (s81–86, s321–323). The audio carries the maintainer's voice.
  No serial, Wi-Fi name, MAC, phone number or account name is written unredacted in this folder's text; the exports and app logcats print the Buds' address as
  `XX:XX:XX:XX:6E:07`; **the exports' Debug-mode hex lines include the `GetHardwareInfo` answers, i.e. the serials** (E1 8 lines, E2 4; A1 2 more, truncated) — committed as is (as
  `CAP-073`…`CAP-075`, the Phase-D answer); the extract has them redacted.

## Event Timeline

One row per event: phone time · film second · what happened · step / Test-ID · evidence. "Claim #n" = the n-th phone `SABM` on DLCI 0x04 in that log (`scripts/message_stream_claims.py`; owner by the first phone message: `08 11 00 00` the app, `03 08 00 02 01 25` Play); "S0 … S13" = the MAESTRO sessions.

### Part 1 — before the recording

| Time | What | Step / Test-ID | Evidence |
|---|---|---|---|
| 06:08:16 | the phone switched to user 10 and back to user 0 at 06:13:05; 1.2.2 ran in user 10 as PID 4104 (bound service of the tile), until the update killed it | — | L `am_switch_user: 10` 06:08:16.009, `am_proc_start: [10,4104,1010354,…,bound-service,{…AncTileService}]` 06:08:16.049, `am_switch_user: 0` 06:13:05.730; A2 4104 lines 04:08:17–04:13:05 UTC |
| 06:21:43–06:23:50 | P0 (both users' package lists) and P1's first `dumpsys` (10202) | P0, P1 | CAP-076-adb-shell.log |

### Part 1 — film 1 (06:35:26.3 – 06:51:47.3), the Owner user with Google Play

| Time | s | What happened | Step / Test-ID | Evidence |
|---|---|---|---|---|
| 06:35:26 | 0–7 | shade and the Bluetooth panel: the Buds "Opgeslagen" (not connected) | — | film |
| 06:35:34 | 8–10 | **Bluetooth off** ("Bluetooth staat uit") | P2 | H1L 7647 06:35:34.729 LE `0x0041` reason `0x16`; H1L ends 06:35:34.956 |
| 06:35:43 | 17–18 | 1.2.2 starts (PID 10952): "Bluetooth is disabled. [Enable Bluetooth]" | — | L `am_proc_start: [0,10952,…,next-top-activity,…]` 06:35:42.993 |
| 06:36:10 | 44–47 | **Bluetooth on**; the Bluetooth process restarts (PID 11130) | P2 | film; L `am_proc_start: [0,11130,1002,com.android.bluetooth,…]` 06:36:10.147; H1 starts 06:36:11.035 |
| 06:36:16–21 | 50–57 | 1.2.2: "Paired – not connected" → Connect → ready; L/R 100 % charging, Case 44 %, "Both buds in the case" | — | H1 672 Connection Complete 06:36:19.895; S0 (1.2.2, ch 21): `SABM` 760, `ReadSetting` 4:16 … 4:29 788–863, `SubscribeRuntimeInfo` 867, `GetHardwareInfo` 868 — **no subscription, no `4:13`** (1.2.2); claim #1 805–1013 (app, `Notify` 832 `e8 00 20`) |
| 06:36:23 | — | Play's first claim of the ACL (#2, 37.5 s) | — | H1 1121 `03 08 00 02 01 25` |
| 06:36:25–39 | 59–73 | 1.2.2 Settings: the card "Noise control with Google Play services", "Google Play services: found on this phone", the option "This phone has Google Play services" selected; Info "App: 1.2.2, build bfd7891 (2026-10-13)", firmware `release_5.203` ×3, control channel 21 | P1 | film |
| 06:36:57–59 | 92–93 | **the update to 1.3.0** — Android's "Updaten…" | P1 | L `am_kill: [10,4104,…,stop … due to killDueToPackageUpdate,…]` 06:36:57.930 and `[0,10952,…]` 06:36:57.941; H1 1586 phone `DISC` DLCI 0x02 06:36:57.987; L `PackageManager: installation completed for package:io.github.tedsluis.opencontrolpixelbuds` 06:36:58.015 (code path `~~Xm8mP1I_…` = the path pulled at 07:52) |
| 06:36:59–37:03 | 94–95 | **1.3.0 starts** (PID 11800) and re-opens the session by itself (LINK_BACK): S1 ch 21, the **seventeen** requests — `SubscribeToSettingsChanges` 1745 and `ReadSetting 4:13` 1746 after `SubscribeRuntimeInfo` 1744, `GetHardwareInfo` 1751 last; the read answers `4:{13:0}` (1750) — "no mode settable" (both buds in the case); the snapshot claim: attempt 1 refused (Play on the channel), attempt 2 = claim #3, released 06:37:03.04 | BL-1 (done differently: the update on film), `PAIR-003`, `BATT-004` | L `am_proc_start: [0,11800,…]` 06:36:59.954; E1 06:37:00.476 "Permissions (start)", 06:37:00.656 "Automatic re-open … LINK_BACK", 06:37:01.594 "Settings stream subscribed (channel 21)", 06:37:01.646 "… from the mode read: no mode settable", 06:37:00.821 "connect failed after 48 ms (attempt 1/3)"; L 06:37:00.818 `already at opened state` |
| 06:37:06 | — | Play's claim #4 (158.9 s) | — | H1 1789 |
| 06:37:26–52 | 100–135 | 1.3.0 Settings: Dark mode, Case sounds, Use different Buds — **no Play card, no Play line**; Info "App: 1.3.0, build ff27ee1 (2026-10-14)"; Debug: Debug mode on, "Unidentified frames (5)"; Battery (i); the ANC tab: "The Buds don't allow changing noise control right now …", Off; its (i): "ANC mode: OFF (updated 06:37:01)", "Connection: Ready", and the new last sentence "The Buds report every change of the mode on the app's own channel. … — that part is a hypothesis." | BL-2, `FW-003`; BL-6 (the stream sentence) | film |
| 06:37:43 | — | the Google app (uid 10302) closes its DLCI 0x08/0x0a | — | H1 2063/2064; L `cleanup_rfc_slot … scn: 4/5, app_uid: 10302` |
| 06:39:41 | 255 | **Disconnect** tap | BL-3 | E1 06:39:41.764 "Session ended by the user's Disconnect tap"; H1 2691 phone `DISC` DLCI 0x02 |
| 06:39:44–47 | 256–259 | **Connect**: S2 ch 21, the seventeen requests (subscription 2838, `4:13` 2839 → 2843 `4:{13:0}`); snapshot attempt 1 refused, #5 | BL-3, `PAIR-003` | E1 06:39:45.678 "Settings stream subscribed (channel 21)" |
| 06:39:53.97 | 268 | **the Buds close DLCI 0x02** (the Left bud out of the case: runtime info 06:39:57.99 Left not charging, Right charging); the loss line, "Android still showed the Buds connected … the Buds closed the channel" | BL-9 (a wear-change re-home), `CASE-004` | H1 2985 Buds `DISC`; E1 06:39:53.977 |
| 06:39:55.48–58.46 | 269–273 | **AFTER_LOSS re-open 1.50 s later**: S3 on **ch 19** — subscription 3120, `4:13` 3120 → 3124 `4:{13:1}` (Off, "(read)"); snapshot #7 (`Notify` 3090 `e8 e8 20`); "Probably worn (checked 06:39:56/58)" | BL-9, `PAIR-003` | E1 06:39:55.481 "… trigger: AFTER_LOSS", 06:39:57.890 "Settings stream subscribed (channel 19)", 06:39:58.031 "from the mode read (read)" |
| 06:39:59.6 | — | Play's claim #8 (91.7 s) — receives the `Notify` of every change until 06:41:31 | — | H1 3156; its `Notify` 3194 … 3971 |
| 06:40:02.27 | 276 | a change to Adaptive, **one stream packet on ch 19** (only the Left bud out of the case); the worn line's time moves | (BL-9: a Buds' own switch?) | H1 3233 `4:{13:4}`; E1 06:40:02.272 "(changed_by_buds)" |
| 06:40:10 | 284 | the Right bud out of the case (Battery (i): "not charging (06:40:10)") | `CASE-005` | H1 3280 runtime info both not charging |
| 06:40:46.8–06:41:44.8 | 320–378 | **the podcast plays on the Buds**: the Buds send AVRCP PLAY / PAUSE eleven times (in-ear detection resuming/pausing as buds go in and out of the ears) | `INEAR-002`/`INEAR-003` (which ear: not identifiable) | H1 3522 PLAY 06:40:46.806, 3603 PAUSE 06:40:53.995, 3626 PLAY 55.970, 3735 PAUSE 06:41:10.617, 3794 PLAY 14.434, 3852 PAUSE 19.327, 3919 PLAY 25.043, 3974 PAUSE 29.130, 4000 PLAY 31.589, 4272 PAUSE 41.315; AVDTP START 3569 06:40:51.362 … SUSPEND 4316 06:41:44.750; AVRCP "Playing" 3553 06:40:51.002 |
| 06:40:47.57–06:41:51.88 | 321–386 | **13 mode changes on the Connection tab** (that tab shows no mode): 3531 13:3, 3573 13:2, 3600 13:3, 3624 13:2, 3668 13:1, 3723 13:4, 3838 13:3, 3953 13:2, 3972 13:3, — the re-home — 4165 13:1, 4299 13:4, 4368 13:3, 4390 13:2 — **each as two stream packets, ch 19 and ch 21, 1–100 ms apart**; three of them within 2–7 ms of the Buds' AVRCP PAUSE/PLAY (3600↔3603, 3624↔3626, 3972↔3974: wear-driven switches); the worn line's "checked" time follows each (06:40:47, :51, :54, :56, :59, 06:41:08, :17, :26, :29, :36, :43, :51); ten short voiced audio segments 0.6–1.4 s after ten of them | BL-4 (done differently: holds and wear changes on the Connection tab), `TOUCH-007` | E1 26 lines "(changed_by_buds)" 06:40:47.575 … 06:41:51.946; claim #8 / #10 `Notify` 3530 … 4389 |
| 06:41:31.28 | 365 | **the Buds close DLCI 0x02** (a bud into an ear: AVRCP PLAY 0.31 s later) | BL-9 | H1 3994; E1 06:41:31.295 |
| 06:41:32.80–34.93 | 367–373 | AFTER_LOSS re-open 1.51 s later, S4 on **ch 21**; the snapshot's `Notify` 4094 `e8 e8 08` (06:41:33.418: Noise cancellation, the shown mode was Transparency) and `4:13` 4130 → 4135 `4:{13:2}` (06:41:34.165) → E1 "from the mode read (**read**)" | BL-9, `PAIR-003` | E1 06:41:32.803, 06:41:34.170; FINDINGS §6 (the "changed since" cause overwritten) |
| 06:41:36.6 | — | Play's claim #10 (to the ACL drop) | — | H1 4187 |
| 06:42:11.27/.64 | 405 | a change to Off (one packet, ch 21) and **`4:{13:0}` — no mode settable**: both buds into the case; "Not worn (checked 06:42:11)" | BL-23 (in part 1), `CASE-*` (into the case) | H1 4492, 4502; E1 06:42:11.277, 06:42:11.647 "… from the settings stream: no mode settable" |
| 06:42:23.19 | 417–419 | the ACL ends (`0x13`, the Buds); "Android no longer shows the Buds connected …" → "Paired – not connected" | BL-23 | H1 4558; E1 06:42:23.204, 06:42:23.616 "Android's link to the Buds went down around the loss" |
| 06:42:45.70–48.06 | 439–441 | **Connect** tap → the ACL (4680, 06:42:46.059) → S5 ch 21, subscription 4858, `4:13` 4859 → 4863 `4:{13:0}` ("no mode settable"); both buds in the case, then the Right out (runtime info 06:42:51) | BL-23, `PAIR-003`, `CASE-005` | E1 06:42:45.703, 06:42:47.354 |
| 06:42:48 | 442 | the ANC tab: the "don't allow changing noise control right now" line, Off kept — **value 0 never shown as a mode** | BL-23 (M2 on hardware) | film |
| 06:42:53.38 | 447 | the Buds close DLCI 0x02 | BL-9 | H1 5357; E1 06:42:53.387 |
| 06:42:54.89–57.11 | 449–451 | AFTER_LOSS re-open 1.50 s later, S6 ch 19 — **closed by the Buds again at 06:42:57.106 after 4:12** (no subscription, no `4:13`); "Automatic re-open skipped: the session was lost 928 ms after an automatic re-open" (`CHAIN_GUARD_MS`); "Still connecting…" / "Not connected to the Buds …" | BL-9 | H1 5528, 5564–5602, 5616 (+ `DM` 5621); E1 06:42:57.113 |
| 06:43:04.88–08.76 | 458–462 | **Connect** tap (nothing re-opened the session for 7.8 s) → S7 ch 21; the snapshot's `Notify` 5991 `e8 e8 08` (06:43:07.250; the shown mode was Off) and `4:13` → 6003 `4:{13:2}` (06:43:07.440) → "(read)"; the ANC tab: Noise cancellation | `PAIR-003` | E1 06:43:04.879, 06:43:07.444 |
| 06:43:11.6 | — | Play's claim #16 (84.4 s) — receives the `Notify` of the 17 holds | — | H1 6042; `Notify` 6107 … 6548 |
| 06:43:14.73–06:44:11.13 | 468–525 | **17 press-and-holds on the ANC tab**: Off, Adaptive, Transparency, NC, Off, Adaptive, Transparency, NC, Off, Adaptive, Transparency, NC, Off, Adaptive, Transparency, NC, Off — each two stream packets (6108/6117 … 6549/6553); **17 of 17 on screen within 0.1 s** of the first packet (10-fps button read: 06:43:14.8, 17.6, 19.8, 22.4, 24.6, 27.1, 29.1, 31.7, 38.7, 44.1, 48.8, 53.4, 57.4, 06:44:02.3, 05.8, 08.2, 11.2); no AVRCP, no audio, **no app `SABM`** | **BL-5**, `TOUCH-007` | E1 34 lines "(changed_by_buds)" 06:43:14.738 … 06:44:11.203 |
| 06:44:22 | 536 | the ANC (i): "ANC mode: OFF (updated 06:44:11) / **Changed by the Buds at 06:44:11: the Buds reported a new mode — a press-and-hold on a bud, or a change they made themselves.** / Connection: Ready / The Buds report every change …" | **BL-6**, `TOUCH-007` | film |
| 06:44:35.96–39.06 | 551–553 | **tap Adaptive**: attempt 1 refused (Play on the channel), attempt 2 0.99 s later (claim #17); `Get`, `Notify` `e8 e8 20`, **`Set` 6724 `40`** 06:44:37.456 → ACK 6726 +91 ms → `Notify` 6727 → **stream 6731/6732 +177/+246 ms** → "(set_by_app)" ×2; the (i): "ADAPTIVE (updated 06:44:37)", no cause line; released 1.513 s after the ACK (`DISC` 6737) | **BL-7**, BL-11, `ANC-003` | E1 06:44:35.963, 06:44:37.055, 06:44:37.636/.708; L 06:44:35.961 `already at opened state` |
| 06:44:48–52 | 562–566 | **tap Transparency**: refused, attempt 2 (#19); `Set` 6956 `80` 06:44:50.476 → ACK +104 ms → stream +237/+238 ms; on screen at 06:44:50.6 (the ACK); (i) no cause line | BL-7, BL-11, `ANC-004` | E1 06:44:50.716/.717 |
| 06:44:58–45:02 | 572–576 | **tap Noise cancellation twice**: refused, attempt 2 (#21); `Get` 7124, `Set` 7137 `08` 06:45:00.538 → ACK +131 → stream +154/+155; the second tap on the open claim: `Get` 7143, `Set` 7151 `08` → ACK 7154, no stream (no change); (i) no cause line | BL-7, BL-11, `ANC-002` | E1 06:45:00.694/.695 |
| 06:45:19–42 | 593–616 | Quick Settings (the podcast card paused): **the ANC tile added** (edit tiles) | BL-8 | film |
| 06:45:44.90–48.70 | 618–621 | **tile** (shown Active): refused, attempt 2 (#23); `Set` 7498 `80` → ACK +83 → stream +160/+201; the tile "Transparent" | **BL-8**, BL-11, `ANC-004` | E1 06:45:44.902 "ANC tile tapped …" |
| 06:45:51.40–55.46 | 628 | tile (Transparent): refused, attempt 2 (#25); `Set` 7665 `40` → ACK +100 → stream +246/+248; "Adaptive" | BL-8, BL-11, `ANC-003` | E1 06:45:51.402 |
| 06:45:57.07–58.38 | 632–634 | tile (Adaptive): attempt 1 connects (#26); **Play's connect 06:45:58.170 is refused and the stack closes both sockets** — the `Get` unanswered: "ANC Get: … answer cut off, not retried"; toast "The channel was closed before the Buds' answer arrived (possibly by another app …)" (06:45:58–06:46:00) | **BL-11** (a collided tap) | E1 06:45:58.254 "RFCOMM on-demand channel 0x04 closed", 06:45:58.383; L 06:45:58.170 `already at opened state …`, `cleanup_rfc_slot … app_uid: 10205` / 06:45:58.247 `… app_uid: 10354`, NearbyDiscovery "Retry times remaining 5" |
| 06:46:03.40–06.72 | 639 | tile (Adaptive): refused, attempt 2 (#28); `Set` 7930 `20` → ACK +95 → stream +198/+199; "Off" | BL-8, BL-11, `ANC-001` | E1 06:46:03.395 |
| 06:46:08.96–09.95 | 640–646 | tile (Off): attempt 1 connects (#29); `Get` 8016 → `Notify` 8029 `e8 e8 20` 06:46:09.878 → **Play's connect refused at 06:46:09.879, the stack's `DISC` 8030 at .880** — **no `Set` sent, no failure line, no toast** (s640–646 pixel-identical) | **BL-11** (a collided tap, silent) | E1 06:46:09.952 "… closed"; L 06:46:09.879/.880/.950; FINDINGS §5 |
| 06:46:13.89–17.53 | 647–650 | tile (Off) again: refused, attempt 2 (#31); `Set` 8208 `08` → ACK +129 → stream +205/+242; "Active" | BL-8, BL-11, `ANC-002` | E1 06:46:13.886 |
| 06:46:19.56–23.79 | 653–656 | tile (Active): attempt 1 refused after 1,071 ms (Play's claim #32 06:46:20.045, killed by it), attempt 2 (#33); `Set` 8377 `80` → ACK +127 → stream +233/+300; "Transparent" | BL-8, BL-11 | E1 06:46:19.563, 06:46:20.636 |
| 06:46:23.87–26.25 | 657–659 | tile (Transparent): attempt 1 connects (#34); `Set` 8445 `40` → ACK +100 → stream +176/+218; then Play's connect (06:46:26.112) closes the claim in its linger; "Adaptive" | BL-8, BL-11 | E1 06:46:26.250 "… closed" |
| 06:46:26.98–28.55 | 660–662 | tile (Adaptive): attempt 1 connects (#35); `Set` 8527 `20` → ACK +129 → stream +195/+260; Play's retry 2.37 s after its refusal closes the claim (06:46:28.479; "Retry times remaining 4"); "Off" | BL-8, BL-11 | E1 06:46:28.549; L 06:46:28.483 |
| 06:46:33.0 | — | Play's claim #36 (57.5 s) | — | H1 8590 |
| 06:47:00–02 | 693–696 | Home and back to the app; the ANC tab: Off | — | E1 06:47:00.722 "observer stopped" / 06:47:01.681 "started" |
| 06:47:07.11, 06:47:18.66 | 701, 712 | two pushes of the **same** mode (Off, one packet each, ch 21) — E1 logs "(set_by_app)" (the kept cause, FINDINGS §4); the (i) "OFF (updated 06:47:07)" / "(updated 06:47:18)", no cause line | (BL-9?) | H1 8739, 8774; claim #36 `Notify` 8738, 8773 |
| 06:47:30.43–35.20 | 724–734 | the Buds close DLCI 0x02 (and Play's DLCI 0x04); AFTER_LOSS 1.50 s later, S8 ch 19: subscription 8959, `4:13` 8960 → 8968 `4:{13:1}` (read, the same); (i) "OFF (updated 06:47:35)" | BL-9, `PAIR-003` | H1 8827/8828; E1 06:47:30.447, 06:47:31.951, 06:47:35.152 |
| 06:47:44.72–49.51 | 738–748 | the Buds close DLCI 0x02 again; AFTER_LOSS 1.50 s later, S9 ch 21: subscription 9213, `4:13` 9214 → 9220 `4:{13:1}`; (i) "OFF (updated 06:47:49)" | BL-9, `PAIR-003` | H1 9077/9080; E1 06:47:44.722, 06:47:46.226 |
| 06:47:50.2 | — | Play's claim #40 (open to the log's end) — receives the 12 holds' `Notify` | — | H1 9250 |
| 06:48:44.23–06:49:33.02 | 798–847 | **12 press-and-holds on the ANC tab** (Adaptive, Transparency, NC, Off ×3), two packets each (9492/9493 … 9717/9720); **12 of 12 on screen within 0.1 s** (06:48:44.3, 46.6, 52.0, 57.1, 06:49:01.8, 06.5, 11.0, 14.6 under a volume panel, 21.2, 25.2, 29.6, 33.1); no app `SABM` | **BL-12** (12 instead of 5), `TOUCH-007` | E1 24 lines 06:48:44.234 … 06:49:33.091 |
| 06:49:44–50:11 | 858–885 | Sound: Upper treble 0 → 6.0; `WriteSetting 4:{16:…}` 9854 → RESPONSE 9860 OK; the stream's mirror 9856 (field 16); (i) "EQ updated: 06:50:04" | (BL-13 not run: no second phone) | E1 (no "Setting written" line for the EQ — it is the EQ path) |
| 06:50:21.80 | 895–905 | Controls: **In-ear detection off**: `4:{2:0}` 9925 → 9934 OK; mirror 9932; (i) "In-ear detection: changed 06:50:22" | `INEAR-001` | E1 06:50:22.351 "Setting 2 written (channel 21)" |
| 06:50:54.86 | 928–929 | In-ear detection on: `4:{2:1}` 10037 → 10042 OK; mirror 10040 | `INEAR-001` | H1 |
| 06:51:03–14 | 937–948 | Debug ("Unidentified frames (119)") → Export: "Debug log saved (1062 lines)." | BL-end1 | film |
| 06:51:19–42 | 953–976 | App info ("Rechten: Apparaten in de buurt, Meldingen en Sensors") → View logs → Save (A1); Show system log → Save (the System log) | BL-end1, P6 | film |
| 06:51:45–47 | 979–980 | the notification "Connected — ANC: OFF"; the film ends | — | film |

### Part 1 — after the recording

| Time | What | Evidence |
|---|---|---|
| 06:52:14.9 | AVRCP "Playing", AVDTP START (playback after the film) | H1 10255, 10257 |
| 06:54:29 | H1 ends with the ACL open; L ends 06:56:48 | `capinfos` |

### Part 2 — before the recording (the secondary user without Google Play)

| Time | What | Step / Test-ID | Evidence |
|---|---|---|---|
| 07:01:02.6–05.4 | 1.3.0 starts in user 10 (PID 18179); Bluetooth was off in this user and turns on | — | A2 `service_manager_slow` 05:01:02.560 UTC; E2 07:01:03.577 "Permissions (start)", 07:01:05.074 "Bluetooth adapter: OFF -> TURNING_ON", 07:01:05.355 "-> ON"; H2L starts 07:01:04.767 |
| 07:01:07.7–09.0 | a 1.3-s ACL without RFCOMM | — | H2L 311 → 489 (`0x13`) |
| 07:01:11.0–13.8 | the CDM pairing picker opened and cancelled (not on film) | — | E2 07:01:11.036 "Pairing: association requested", 07:01:13.845 "association failed (canceled)" |
| 07:01:13.8–15.9 | the ACL (605); LINK_BACK re-open: S10 ch 19 — subscription 848, `4:13` 849 → 853 `4:{13:0}` ("no mode settable", both buds in the case) | BL-14 (part), `PAIR-003` | E2 07:01:14.279, 07:01:15.114, 07:01:15.156 |
| 07:02:39.1–39.6 | **Bluetooth off** (the film's s8); "Session loss cause: Bluetooth was switched off on this phone" | **BL-20** | E2 07:02:39.124, 07:02:39.442; H2L 1294 (`0x16`) |

### Part 2 — film 2 (07:02:31.5 – 07:08:20.4), the secondary user without Google Play

| Time | s | What happened | Step / Test-ID | Evidence |
|---|---|---|---|---|
| 07:02:31.5 | 0–7 | shade, Quick Settings, the Bluetooth panel (the Buds "Actief. Batterijniveau 100%") | — | film |
| 07:02:39.5–44.5 | 8–13 | **Bluetooth off, then on** | **BL-20** | E2 07:02:44.391 "OFF -> TURNING_ON"; H2 starts 07:02:44.098 |
| 07:02:45.9–47.9 | 14–19 | the ACL (151); **LINK_BACK re-open 0.27 s after Android's link**: S11 ch 19 — subscription 540, `4:13` 541 → 546 `4:{13:0}` (no mode settable) — **the subscription again after the Bluetooth off (M1 on hardware)**; the app "ready", both buds in the case | **BL-20**, `PAIR-003`, `BATT-004` | E2 07:02:46.158, 07:02:47.053 "Settings stream subscribed (channel 19)" |
| 07:03:00.36 | 29 | the Buds close DLCI 0x02 (the Right bud out of the case: runtime info 07:03:04 Left charging, Right not) | BL-19 (a re-home), `CASE-005` | H2 641; E2 07:03:00.362 |
| 07:03:01.87–05.20 | 31–32 | AFTER_LOSS re-open 1.50 s later, S12 on **ch 21**; **a stream packet `4:{13:1}` at 07:03:02.647 (717) — before this session's subscription** (the ch-19 subscription of 07:02:47 still reports, now on ch 21) → E2 "from the settings stream (**read**)" (the first mode ever shown in this process); subscription 790, `4:13` 790 → 794 `4:{13:1}` "(read)"; "Probably worn (checked 07:03:03)" | BL-19, `PAIR-003` | E2 07:03:02.652, 07:03:04.511, 07:03:04.692 |
| 07:03:07.14 | 36 | the same mode again (Off, one packet, ch 21) — "(read)" (the kept cause); the Left bud out of the case at 07:03:14 | `CASE-004` | H2 839; Battery (i) s44 "not charging (07:03:14)" |
| 07:03:10–57 | 39–86 | Battery (i); the ANC tab (Off) and its (i) "OFF (updated 07:03:07)", no cause line, the stream sentence; Sound; Controls; Settings: **no Play card**; Debug "Unidentified frames (17)"; Info "App: 1.3.0, build ff27ee1 (2026-10-14)", firmware 07:03:02, channel 21, serials 07:03:04 | **BL-14**, `FW-003` | film |
| 07:04:12.26 | 101 | **Disconnect** tap | BL-22 | E2 07:04:12.264; H2 1017 |
| 07:04:15.35–17.43 | 104–107 | **Connect**: S13 ch 21 — subscription 1131, `4:13` 1132 → 1137 `4:{13:1}` (the same as before: "(read)", no cause line) | BL-22 (done differently: no hold while disconnected), `PAIR-003`, `BATT-004` | E2 07:04:15.346, 07:04:16.641 |
| 07:04:32.0–33.9 | 121–122 | **Refresh battery**: `SubscribeRuntimeInfo` again (1205) and a fresh claim; "Probably worn (checked 07:04:32)" | — | E2 07:04:32.023 "Runtime info re-requested on Refresh" |
| 07:04:49.7–51.3 | 139–144 | **tap Noise cancellation** on the ANC tab: claim at attempt 1 (no Play); `Set` 1372 `08` 07:04:49.772 → ACK +25 ms → stream 1381/1384 +60/+85 ms → "(set_by_app)" ×2; on screen at 07:04:49.8; (i) "ACTIVE (updated 07:04:49)", **no cause line** | **BL-17**, `ANC-002` | E2 07:04:49.699, 07:04:49.836/.861 |
| 07:04:57.4–59.1 | 145–149 | tap Adaptive: `Set` 1470 `40` → ACK +77 → stream +81/+121; (i) no cause line | BL-17, `ANC-003` | E2 07:04:57.607/.646 |
| 07:05:04.8–06.4 | 152–156 | tap Transparency: `Set` 1558 `80` → ACK +50 → stream +55/+93; (i) no cause line | BL-17, `ANC-004` | E2 07:05:04.927/.966 |
| 07:05:13.6–15.3 | 162–168 | tap Off: `Set` 1650 `20` → ACK +82 → stream +94/+125; (i) no cause line | BL-17, `ANC-001` | E2 07:05:13.842/.868 |
| 07:05:24.11–07:06:38.28 | 172–247 | **12 press-and-holds on the ANC tab** (Adaptive, Transparency, NC, Off ×3), two packets each (1726/1729 … 2014/2017); **12 of 12 on screen within 0.1 s** (07:05:24.1 under a volume panel, 28.7, 35.3, 41.5, 47.0, 51.6, 56.2, 07:06:03.2, 13.1 under a volume panel, 22.1, 31.3, 38.3); the (i) five times: "Changed by the Buds at 07:06:03 / 07:06:13 / 07:06:22 / 07:06:31 / 07:06:38: …"; no claim, no Play | **BL-16** (12 instead of 10), `TOUCH-007` | E2 24 lines "(changed_by_buds)" 07:05:24.113 … 07:06:38.320 |
| 07:06:44–49 | 253–259 | Sound: Upper treble 6.0 → 1.7; `WriteSetting 4:{16:…}` 2018 → 2021 OK; mirror 2020 (1.69) | — | H2 |
| 07:06:56.25 | 265–267 | Controls: **In-ear detection off** (left off): `4:{2:0}` 2081 → 2088 OK; mirror 2087; (i) "changed 07:06:56" | `INEAR-001` | E2 07:06:56.363 "Setting 2 written (channel 21)" |
| 07:07:15–19 | 284–288 | the Find tab and its (i): "Each action briefly claims the Buds' Message Stream channel. …" | — | film |
| 07:07:25.8–27.6 | 293–295 | **Ring Left**: claim #9, `04 01 00 01 02` → ACK `ff 01 00 03 04 01 00` and the Buds' `04 01 00 01 02`; "Ringing: Left earbud — tap Stop to end it." | `FIND-001` | E2 07:07:25.925 … 07:07:27.559 |
| 07:07:32.3–34.0 | 301 | **Stop**: claim #10, `04 01 00 01 00` → ACK; the ringing line gone | `FIND-001` | E2 07:07:32.344 … 07:07:33.969 |
| 07:07:38–44 | 307–313 | Debug → Export: "Debug log saved (462 lines)." | BL-end2 | film |
| 07:07:48–57 | 317–326 | Debug, Info (serials) | — | film |
| 07:08:00–16 | 329–345 | App info → View logs → Save (A2); the log view has no "Show system log" in this user | BL-end2 | film |
| 07:08:19–20 | 348 | the notification "Connected — ANC: OFF"; the film ends | — | film |

### Part 2 — after the recording

| Time | What | Evidence |
|---|---|---|
| 07:08:21.6 | AVRCP "Playing" | H2 2633 |
| 07:08:33.44 | the ACL ends (`0x13`) — the session lost while the app was in the background (no export after 07:07:33) | H2 2666 |
| 07:10:20.4 | a new ACL; the Buds open RFCOMM (DLCI 0x00/0x08), no OpenControl session | H2 2678, 2877, 2884 |

## The maintainer's steps, as run (mapped to BL-1 … BL-25)

| Step | Done? | How (evidence) |
|---|---|---|
| P0 | partly | the package lists of both users with exit statuses (shell log); not on film: Play's *Nearby devices* state and version — read by `ai-sessions/0092` at 07:53 instead (CAP-076-adb-checks-0092.log) |
| P1 | yes, differently | the 1.2.2 Settings card and Info on film (s59–73); both `dumpsys` saved; **the install on film** (s92–93); Debug mode was already on; the APK pulled by `ai-sessions/0092` (07:52) — SHA-256 = the tested build's |
| P2 | yes | Bluetooth off (s8) and on (s44) on film; H1L → H1 |
| P3 (camera) | **no** | no camera films; the holds are the maintainer's statement plus the wire (stream packets without an app `Set`, no AVRCP) |
| P4 | part 1 yes; part 2 screen only | film 2 has no audio stream |
| P5 | yes | minute flips in both films |
| P6 | part 1 yes | the System log saved at s975; user 10 offers no system log |
| P7 | yes | "Modi: Niet storen is actief" (s955) |
| P8 | differently | the buds came out of the case during the run (CASE-004/005), not worn before BL-4 |
| P9 | yes | the app opened first on film (s17) |
| P10 | no | no second phone |
| BL-1 | yes, differently | the update on film; the LINK_BACK re-open at the 1.3.0 start (S1), the seventeen requests |
| BL-2 | yes | Info and Settings of 1.3.0 on film (s100–112) |
| BL-3 | yes | Disconnect 06:39:41, Connect 06:39:44 (S2) |
| BL-4 | differently | 13 changes on the Connection tab 06:40:47–06:41:51, mixed with wear changes (AVRCP), during podcast playback; that tab shows no mode |
| BL-5 | yes (17 holds) | 17 of 17 within 0.1 s |
| BL-6 | yes | the (i) at 06:44:22 with "Changed by the Buds at 06:44:11 …" |
| BL-7 | yes | four taps (Adaptive, Transparency, NC ×2), each collided at attempt 1, each succeeded at attempt 2 |
| BL-8 | yes (9 tile taps) | the tile added on film, 9 taps |
| BL-9 | yes (several) | the Buds' `DISC`s at 06:39:53, 06:41:31, 06:42:53, 06:42:57, 06:47:30, 06:47:44 and their re-opens; three wear-driven switches |
| BL-10 | **no** | no Bluetooth off/on with 1.3.0 in part 1 (the one on film was under 1.2.2, before the session) |
| BL-11 | yes | 13 taps with Play present, in 12 claims: 8 claims (9 taps) refused at attempt 1 and done at attempt 2, 2 done at attempt 1 and cut in their linger, 1 cut before the `Get`'s answer (toast), 1 cut after it (silent) |
| BL-12 | yes (12 holds) | 12 of 12 within 0.1 s |
| BL-13 | no | no second phone |
| BL-end1 | yes | export, app logcat, system log |
| BL-14 | yes | S10/S11/S12; Info, Settings |
| BL-15 | **no** | no holds on the Connection tab in part 2 |
| BL-16 | yes (12 holds) | 12 of 12 within 0.1 s |
| BL-17 | yes (4 taps) | each at attempt 1 |
| BL-18 | **no** | no tile in part 2 |
| BL-19 | yes, differently | the Buds' `DISC` 07:03:00 and the re-open (a bud out of the case, not out of an ear) |
| BL-20 | yes | Bluetooth off/on 07:02:39/44, the re-open subscribes again (S11) |
| BL-21 | **no** | |
| BL-22 | differently | Disconnect / Connect 07:04:12/15 without holds in between — the read equal, no line expected |
| BL-23 | in part 1 | both buds into the case 06:42:11 (`4:{13:0}`), the ACL drop, the Connect with "no mode settable" |
| BL-24 | **no** | |
| BL-25 | no | no second phone |
| BL-end2 | yes | export, app logcat; no system log in user 10 |

## Test-IDs (`AGENTS.md` §13 step 7; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`)

| Test-ID | Where in the timeline | Result |
|---|---|---|
| `PAIR-003` | S1–S9 (part 1), S10–S13 (part 2): Connects, LINK_BACK and AFTER_LOSS re-opens | every session opened; the subscription and the read in every full session (12 of 12) |
| `BATT-004` | every snapshot claim's `03 03` burst (e.g. H1 7128/7132); the runtime-info stream per session | L/R 100 %, Case 44 %/43 % on screen |
| `FW-003` | Info, part 1 s101–107; part 2 s81–86, s321 | `release_5.203` ×3, three serials |
| `ANC-001`…`ANC-004` | the 15 `Set`s (part 1 11, part 2 4) | all four modes; 15 ACKed, 0 NAK |
| `TOUCH-007` | part 1: 13 changes on the Connection tab, 17 + 12 holds on the ANC tab; part 2: 12 holds | 29 + 12 of 29 + 12 on screen within 0.1 s |
| `CASE-004` / `CASE-005` | 06:39:53 (the Left out), 06:40:10 (the Right out), 06:42:46–51 (out again); 07:03:00 (the Right out), 07:03:14 (the Left out) — from the runtime-info charging flags, not on film | the Buds' `DISC`s and re-opens |
| `INEAR-002` / `INEAR-003` | the Buds' AVRCP PLAY/PAUSE 06:40:46–06:41:41 | which ear: not identifiable |
| `INEAR-001` | 06:50:22 off / 06:50:55 on; 07:06:56 off | written, acknowledged, mirrored on the stream |
| `FIND-001` | 07:07:25 Ring Left, 07:07:32 Stop | ACKed |
| `PAIR-003` BL-10 (Bluetooth off/on in part 1), `TOUCH-007` BL-15/BL-21/BL-24 | — | **expected but not observed** (not run) |

## Analysis checklist

- [x] Every filter scoped to the Buds' ACL handle; DLCIs by content; every negative with its command, exit status and positive control (CAP-076-FINDINGS.md).
- [x] P0/P1: Play listed in user 0 only; the update with `firstInstallTime` unchanged; the Info hash `ff27ee1`; the pulled APK's SHA-256 = B1's.
- [x] The two new requests byte-identical, once per MAESTRO session: 12 subscriptions and 12 `ReadSetting 4:13` in 12 full 1.3.0 sessions; the 13th (S6)
      was closed by the Buds before them; after `SubscribeRuntimeInfo`, before `GetHardwareInfo` (FINDINGS §2).
- [x] The counts for the verdict (FINDINGS §10).
- [x] BL-7 / BL-17: the own tap classed as the app's change; the stream 55–246 ms after the `Set`.
- [x] BL-9 / BL-19 / BL-23: the Buds' `DISC` → re-open → read in every case; one stream packet before a subscription (07:03:02.647); value 0 never shown as a mode.
- [x] BL-10 / BL-20: BL-20 yes (S11 subscribed again); BL-10 not run.
- [x] BL-11: 13 taps with Play present; nothing re-claimed by itself.
- [ ] BL-13 / BL-25: not run.
- [x] The system log: crashes, ANRs, StrictMode — FINDINGS §0.
- [x] One row per step; Test-IDs with "expected but not observed".
- [x] Registers, the ADR-065 Update and the verdict: Phase D of `ai-sessions/0092` (with the maintainer).

## Appendix — the planned procedure (the skeleton of `ai-sessions/0091`, 2026-10-14, as written)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0089` for 1.2.2 on 2026-10-13; **rewritten by `ai-sessions/0091` for 1.3.0 on
2026-10-14** — 1.2.2 was released without this run, `TODO.md` §1). The hardware run of **1.3.0** (versionCode 10300; `DECISIONS.md` ADR-064 and ADR-065): at every
MAESTRO session (Connect and every ADR-044 re-open) the app sends one `SubscribeToSettingsChanges` and one `ReadSetting 4:13` after `SubscribeRuntimeInfo`, so
the Buds push every change of the noise-control mode — a press-and-hold on a bud, their own switch, the app's own `Set` — as a `4:{13:n}` packet on the app's
own channel (DLCI 0x02), **whatever tab is shown and without a claim of the shared channel DLCI 0x04**; every other setting the app reads follows the same
stream. The hold of 1.2.0 (ADR-061), the re-claim with the setting "Noise control with Google Play services" (ADR-062) and the Play-services line with the
`<queries>` entry (ADR-063) are gone. **Two new requests on the wire, nothing else new**: the official app's bytes (`CAP-051` 1177/1178, `CAP-056` 1451/1450).
**Two parts, both GrapheneOS users, the extended tier** (`RELEASING.md` §11a): **part 1** the Owner user (user 0) with Play services — where 1.2.x showed a hold
about half of the time at best; **part 2** the secondary user (user 10) without Play. The maintainer films; a later CAPTURE session analyses it and gives the
release verdict (`RELEASING.md` C3/C4). After the run: rename this folder to the screen recordings' first/last phone times and the recordings to
CAP-076-recording1.mp4 / CAP-076-recording2.mp4; the camera films to CAP-076-camera1.mp4 / CAP-076-camera2.mp4.

**Expected (from the official app's captures — `CAP-051-FINDINGS.md` §2–§3, `CAP-056-FINDINGS.md` §4, `CAP-038-FINDINGS.md` §3; `ai-sessions/0091` RESULT
§A.1):** the stream packet for a hold follows the Buds' `Notify` by 1–144 ms (`CAP-051` 1707 → 1717, 1754 → 1755, 1801 → 1802), also while no client holds the
Message Stream (`CAP-038` 2170, 2205); so **every hold shows on screen within a second, on any tab, in both users**, with "Changed by the Buds at HH:MM:SS" in
the noise-control (i). The app's own tap: `Set` → ACK → `Notify` → the stream packet 269 ms after the Set (`CAP-051` 1527 → 1534) — shown as the app's change.
A wear change the Buds answer with a `DISC` of DLCI 0x02 (12 of 77 in the captures, `DESKRESEARCH_FINDINGS.md` 2026-10-14): the ADR-044 re-open ≈ 1.5 s later
subscribes again and reads 13 — the mode on screen may be a few seconds old in between.

**Not in this run** (say so if asked): `ReadSetting 4:8/9/10` (the maintainer's choice in chat 2026-10-14, "Not part of this run"); `WriteSetting 4:{13:n}`
(`TODO.md` §4 E-01, needs its own approval); the case-sound experiment.

### Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-076` |
| Group(s) | BL (new) |
| Date | TBD |
| Phone | Pixel 9a, GrapheneOS (Android 17) — **part 1: the Owner user (user 0) with Google Play services** (`CAP-074`/`CAP-075` set 2's user); **part 2: the secondary user (user 10) without Google Play** (`CAP-073`/`CAP-075` set 1's user) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.3.0** (versionCode 10300), the release APK of `scripts/release.sh 1.3.0` (B1), kept as `~/opencontrol-1.3.0-tested` (B3) — **Info tab on film in each user**: "App: 1.3.0, build <B1 hash> (<date>)", no "-dirty"; APK SHA-256 and certificate SHA-256 from the script's output; **the installed APK pulled** (P1) |
| Previous version in each user | 1.2.2 build `bfd7891` (installed 2026-10-14 with Obtainium in the Owner user; the secondary user's version on film) — **the stored "Noise control with Google Play services" choice of 1.2.1/1.2.2 is ignored by 1.3.0** (ADR-065 item 5: never read, never migrated); the card is gone |
| Google Play services (part 1) | present and active (`com.google.android.gms`, uid 10205 in `CAP-074`/`CAP-075`); **its *Nearby devices* permission state and its version on film** (P0 — `TODO.md` §3's standing item, not done in `CAP-075`) |
| Official Pixel Buds app | not used on this phone; **optionally on a second phone** for BL-13 / BL-25 (the EQ or a switch changed there — the stream packet reaches OpenControl) — say if no second phone was available |
| Buds | Pixel Buds Pro 2, firmware `release_5.203` expected (any other ⇒ Safe Mode: say so, stop) — both buds **worn** for every hold step (Settable `e8`; `00` means no bud worn, ADR-049) |
| Video files | TBD — the screen recording **with sound** per part (say each action and each hold aloud, as in `CAP-075`) **and a camera on the ears and the phone** for the hold steps (the hold on the bud and the screen in one frame; no address overlay — `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3) |
| Log files | TBD — the HCI snoop logs, the app's debug export per part (Debug mode on), the app logcat, the system log with the GrapheneOS log viewer at the end of each part, the P0/P1 outputs |

### Preparation

| # | Check | Done |
|---|---|---|
| P0 | **In each user**: `adb shell am get-current-user`; `adb shell pm list packages --user <id> \| grep -i -E "gms\|vending"; echo "exit=$?"` (part 1: Play listed, exit 0; part 2: nothing, exit 1); positive control `grep opencontrol` (exit 0). **Part 1 on film:** Settings → Apps → Google Play services → Permissions → *Nearby devices* (allowed / not allowed) and the app's version line. Save the outputs **with the exit statuses** | ☐ |
| P1 | In the installed 1.2.2 (`bfd7891`): Debug mode **on** (on film); gear → Settings: the card "Noise control with Google Play services" and its selected option on film (what 1.3.0 will ignore). `adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds \| grep -E "firstInstallTime\|lastUpdateTime\|versionCode"` → save. `adb install -r ~/opencontrol-1.3.0-tested/opencontrol-pixelbudspro2-1.3.0.apk` (one APK serves both users) — **no uninstall**; the same `dumpsys` → save. After the run: `adb shell pm path --user <id> io.github.tedsluis.opencontrolpixelbuds` → `adb pull` → `sha256sum` against `~/opencontrol-1.3.0-tested` | ☐ |
| P2 | HCI snoop log on (set in the Owner; it stays on across the user switch) — **Bluetooth off and on on film** after enabling it | ☐ |
| P3 | The camera on a stand: the ears and the phone screen in one frame for BL-5 … BL-8 and BL-18 … BL-21; the phone's clock with seconds visible, or the status bar at a minute change at the start and the end | ☐ |
| P4 | Android's **screen recorder with sound** on for each part | ☐ |
| P5 | The status bar across a minute change at the start and the end of each recording | ☐ |
| P6 | **The system log at the end of each part** (the GrapheneOS log viewer, as `CAP-075`'s L) — crashes, ANRs, `StrictMode`; in part 1 Play's `RfcommEventStreamMedium` lines around the taps of BL-10/BL-11. `adb bugreportz` sends signal 3 to every Java process — not an app fault (`CAP-071-FINDINGS.md` §0) | ☐ |
| P7 | **Do Not Disturb on** | ☐ |
| P8 | Buds charged, **both worn** before BL-4; Android shows them connected before the app is opened | ☐ |
| P9 | **No app use before the film** — not opened between P1's install and BL-1 | ☐ |
| P10 | Optional: a second phone with the official Pixel Buds app paired to the same Buds (multipoint) for BL-13 / BL-25 — say on film if there is none | ☐ |

**Rhythm:** one action, then 5–10 s; say what you do ("left hold", "right hold", "tap", "tile", "bud out", "Bluetooth off"). Export the debug log at the end
of each part. Serials: never unredacted in any note (first 4 + last 2 characters).

### Steps

DLCIs are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05). Every MAESTRO request starts `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25` on
**channel 19** or `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25` on **channel 21** (Info → "Control channel"). **Reference frames** — the official app's bytes,
re-derived by `ai-sessions/0091` with `tshark -r <log> -Y 'frame.number==N' -T fields -E separator='|' -e frame.number -e frame.time -e frame.p2p_dir
-e bthci_acl.chandle -e btrfcomm.dlci -e data.data` (exit 0) after scoping to the Buds' ACL handle, decoded with `python3 scripts/pwrpc_decode.py <log>`
(a payload holding several HDLC frames split on `7e`, each CRC-32 verified):

| Request / event | Reference (whole pw_hdlc frame incl. CRC) |
|---|---|
| The Connect: `SABM` DLCI 0x02, then `ReadSetting 4:16`, 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29, `SubscribeRuntimeInfo`, **`SubscribeToSettingsChanges`, `ReadSetting 4:13`**, `GetHardwareInfo` — **fifteen MAESTRO requests** | `CAP-075` H2 1193 … 1302 (channel 19) / H1 762 … 841 (channel 21) for the thirteen of 1.2.x (`CAP-075-FINDINGS.md` §2), then the two new ones below, then `GetHardwareInfo` (`7e003b0310131dea71de7d5e25e3a5ec28ff1ebbb77e` on 19, `7e004b0310151dea71de7d5e25e3a5ec28f96761b57e` on 21) |
| **`SubscribeToSettingsChanges` REQUEST** (method `0x2821adf5`, empty payload) | channel 21: `CAP-051` 1177 — `7e004b0310151dea71de7d5e25f5ad2128019754b07e`; channel 19: `CAP-056` 1451 / `CAP-051` 2229 — `7e003b0310131dea71de7d5e25f5ad212807ee8eb27e`. Find them: `tshark -r CAP-076-btsnoop_hci1.log -Y 'btrfcomm.dlci==2 \|\| btrfcomm.dlci==3' -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data \| grep -i "25f5ad2128019754b0\\\|25f5ad212807ee8eb2"` — **one per MAESTRO session, direction 0 (sent)** |
| **`ReadSetting 4:13` REQUEST** | channel 21: `CAP-051` 1178 / `CAP-036` 1517 — `7e004b0310151dea71de7d5e2551aed0ae2a02200d9e82abac7e`; channel 19: `CAP-056` 1450 / `CAP-021` 1069 — `7e003b0310131dea71de7d5e2551aed0ae2a02200d24c239477e`. Find: `… \| grep -i "2a02200d"` — one per MAESTRO session, right after the subscription |
| **Its RESPONSE `4:{13:n}`** (type 1; the first read's value after every Connect/re-open) | channel 19: `7e80a3032a04220268 0n 080110131dea71de7d5e2551aed0ae` + CRC — n = 0 `…c6b9d8857e` (`CAP-051` 2247, after the Buds' own `DISC`), 1 `…2e62233c7e` (`CAP-021` 1089), 2 `…57085e2d7e` (`CAP-056` 1468), 3 `…bfd3a5947e` (`CAP-027` 923); channel 21: n = 0 `7e00a5032a0422026800080110151dea71de7d5e2551aed0ae651b08147e` (`CAP-045` 2033), 4 `…067d5e749e7e` (`CAP-051` 1225). 1 = Off, 2 = Noise cancellation, 3 = Transparency, 4 = Adaptive, **0 = no mode settable** (no bud worn) |
| **The stream packet `4:{13:n}`** (type 7 `SERVER_STREAM`, direction 1, the Buds → the phone) — **one per hold, per own tap and per wear-driven switch** | channel 19: `7e80a3032a04220268 0n 080710131dea71de7d5e25f5ad2128` + CRC — n = 1 `…117bf5557e` (`CAP-021` 1842), 2 `…681188447e` (`CAP-038` 2205, `CAP-056` 2324/4028), 3 `…80ca73fd7e` (`CAP-038` 2170), 4 `…9ac572667e` (`CAP-056` 2259/4025); channel 21: 0 `7e00a5032a0422026800080710151dea71de7d5e25f5ad21285a02de7d5d7e` (`CAP-045` 2277), 2 `…cbb358d57e` (`CAP-051` 1717), 3 `…2368a36c7e` (1802/1534), 4 `…3967a2f77e` (1755). Find: `… \| grep -i "0807101[35]1dea71de7d5e25f5ad2128"`; count them against the holds made |
| A stream packet for another setting (BL-13 / BL-25, the official app on a second phone) | field 16 (the EQ): `CAP-015` 2116 `7e80a3032a1e221c8201190d…080710131dea71de7d5e25f5ad2128…`; field 2: `CAP-056` 2175 `7e80a3032a0422021001080710131dea71de7d5e25f5ad2128475780f97e`; field 12: `CAP-056` 1696; field 29: `CAP-069` 2502 — the value on screen with the packet's time ("read HH:MM:SS" in the card's (i)) |
| The app's own tap (unchanged, ADR-032) | `SABM` DLCI 0x04 (or 0x05), `08 11 00 00`, `Notify`, `08 12 00 14 01 e8 e8 <mode> …`, ACK `ff 01 00 06 08 12 01 e8 e8 <mode>`, then the Buds' `Notify` and **the stream packet of that mode on DLCI 0x02** (`CAP-051` 1527 → 1529 → 1530/1533 → 1534: 269 ms); the phone's `DISC` of DLCI 0x04 ≈ 1.5 s after the answer (`MESSAGE_STREAM_LINGER_MS`) |
| Play's Fast Pair claim (part 1) | `SABM` DLCI 0x04, first message `03 08 00 02 01 25` (`CAP-075-FINDINGS.md` §1) — its presence is expected and changes nothing for the stream |
| The Buds' own `DISC` on a wear change (BL-9 / BL-22) | `DISC` DLCI 0x02 from the Buds (`CAP-051` 1996, 5 ms after the Notify 1993) → the app's loss line → ADR-044's re-open ≈ 1.5 s later: `SABM` DLCI 0x02, the fifteen requests, the subscription and the read again (`CAP-051` 2229/2231 → 2247) |

Anything else from the app on the wire (a request not in this table or in `CAP-075` §2/§3, a second `SubscribeToSettingsChanges` or `ReadSetting 4:13` within one
session, a `WriteSetting 4:{13:n}`, anything on DLCI 0x08/0x0a, a `SABM` on DLCI 0x04 that is not a tap, the tile, Refresh, a pull or the Connect's snapshot) is
a finding.

#### Part 1 — the Owner user (user 0) **with** Google Play services (screen recording with sound + the camera; `APP_TESTPLAN.md` Y1–Y13; `PAIR-003`, `BATT-004`, `FW-003`, `ANC-001`…`ANC-004`, `TOUCH-007`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BL-1 | P0–P10 done, user 0, both buds worn | open the app | the session opens by itself (ADR-044); "Probably worn"; Left/Right %, the Case; the mode | DLCI 0x02 `SABM`; the **fifteen** requests in order — the subscription and `ReadSetting 4:13` after `SubscribeRuntimeInfo`, `GetHardwareInfo` last — answered; the snapshot claim (one `SABM` DLCI 0x04, `08 11` → `Notify`, `03 03` burst, released ≈ 1.5 s later) | `PAIR-003`, `BATT-004` | a crash; a request missing, doubled or out of order; Safe Mode |
| BL-2 | ready | gear → **Info**: 3 s on the build line, the firmware, the serials; **Settings**: 3 s on the whole tab | "App: 1.3.0, build <B1 hash>", no "-dirty"; `release_5.203` ×3; Settings shows **Dark mode, Case sounds, Use different Buds — no "Noise control with Google Play services" card, no "Google Play services: …" line** | nothing | `FW-003` | "-dirty"; another hash; the card or the line still there |
| BL-3 | back on Connection | **Disconnect**, wait 5 s, **Connect** | ready | the phone's `DISC` of DLCI 0x02; the fifteen requests again (the subscription and the read once more — one per session); the snapshot claim | `PAIR-003` | the subscription missing after the Connect |
| BL-4 | ready, stay on the **Connection** tab, both buds worn, the camera on the ears | **press and hold a bud 10 times, ≈ 6 s apart** (alternate left/right); say each; **touch nothing on the phone**; count aloud the mode changes you see on the Connection tab's noise-control line (if any) — then open the **ANC** tab once and read the mode | the mode in the app follows **every** hold within a second (the tab need not be open: the notification's mode line and, on opening ANC, the current mode) | per hold: **one stream packet `4:{13:n}` on DLCI 0x02** (direction 1), no `Notify` (no claim), **no `SABM` on DLCI 0x04 from the app**; Play's own claims may come and go | `TOUCH-007`, `ANC-001`…`004` | a hold without a stream packet; an app `SABM` on DLCI 0x04 during the holds; a mode shown that differs from the last packet's |
| **BL-5** | the **ANC** tab open, both buds worn, the camera on the ears | **press and hold a bud 10 times, ≈ 6 s apart**; say each; count aloud the holds you see change on screen | **10 of 10** holds change the mode on screen within a second; the (i) after one of them: **"Changed by the Buds at HH:MM:SS: the Buds reported a new mode — a press-and-hold on a bud, or a change they made themselves."**; the (i)'s last sentence "The Buds report every change of the mode on the app's own channel. …" | one stream packet per hold; **no app `SABM` on DLCI 0x04** (the ANC tab claims nothing by itself since 1.3.0) | `TOUCH-007`, `ANC-001`…`004` | a hold not shown; an app `SABM`; a hint or "Take it back again" anywhere |
| BL-6 | the ANC tab | open the ANC **(i)** right after a hold, read it, close | the mode with its time; the "Changed by the Buds at …" line; "Connection: Ready"; the last sentence with "that part is a hypothesis" | nothing | `TOUCH-007` | the old hold sentence; the channel-taken line |
| BL-7 | the ANC tab | **tap a mode** (one tap); wait 5 s; open the (i) | the tapped mode at once; the (i) without a "Changed by the Buds" line (the app's own change) | one `SABM` DLCI 0x04 + `08 11` + `08 12` + ACK; the Buds' `Notify`; **the stream packet of the tapped mode on DLCI 0x02 within ≈ 0.3 s of the `Set`**; the phone's `DISC` ≈ 1.5 s after the ACK; if Play is on the channel at that moment: attempt 1 refused, attempt 2 connects (`CAP-075` §5) — or the tap fails with its message: tap again, say so | `ANC-001`…`004` | the stream packet of the own Set shown as "Changed by the Buds"; a `DISC` later than 2 s after the ACK |
| BL-8 | the ANC tab | pull **Quick Settings** down; tap the **ANC tile** once; close; look at the app | the tile steps one mode; the app follows at once | as BL-7: one claim for the tile's `Set`; the stream packet; the `DISC` ≈ 1.5 s later | `ANC-001`…`004` | a second `SABM`; the app not following |
| **BL-9** | the ANC tab, **both buds worn**, the camera on the ears | **take one bud out** (say which); wait 10 s; **put it back**; wait 10 s; read the mode after each | if the Buds close the session (the loss line "… the Buds closed …", ready again ≈ 1.5 s later by itself): after the re-open the mode read (`ReadSetting 4:13`'s answer) — the (i) says **"Changed since the app last saw it (read at HH:MM:SS): …"** if it differs from the mode before, nothing if it is the same; if the Buds keep the session: the stream packet of their own switch, "Changed by the Buds" (the stored mode per wear state, 🟡 `PROTOCOL.md` §6) | either the Buds' `DISC` of DLCI 0x02 → the re-open's `SABM`, the fifteen requests, **the subscription and the read again** (the first read's value after the re-open), or a stream packet `4:{13:n}` without a `DISC` — say which happened | `PAIR-003`, `TOUCH-007` | a re-open without the subscription; a changed read without the line; the app sending anything on reaction to the packet |
| BL-10 | ready | **Bluetooth off** (Quick Settings); wait 5 s; **Bluetooth on**; wait 15 s | "Bluetooth was switched off" as the loss cause; ready again by itself once Android reconnects the Buds | the loss; the re-open's `SABM` DLCI 0x02, the fifteen requests — **the subscription again** (M1 on hardware) | `PAIR-003` | no subscription after the re-open |
| BL-11 | ready, the ANC tab | **tap a mode** while Play's claim is likely open (any moment — Play holds the channel most of the time in this user); if the tap fails ("… could not claim …"), wait 5 s and tap again | the mode after at most two taps; nothing else; **no hint, no "Take it back again"** | one claim per tap; a refused attempt 1 with Play's `DISC` is the `CAP-075` §5 collision — write down how many taps failed; **the app never re-claims by itself** | `ANC-001`…`004` | an app `SABM` not caused by a tap/tile/Refresh/pull/Connect |
| BL-12 | the ANC tab | hold a bud **5 more times** ≈ 6 s apart | 5 of 5 shown | 5 stream packets; no app `SABM` | `TOUCH-007` | a hold not shown |
| BL-13 (optional, P10) | the Sound tab | on the **second phone**, in the official Pixel Buds app: move one EQ slider, then switch **In-ear detection** off and on (Controls); watch OpenControl | the equalizer follows within a second ("read HH:MM:SS" in its (i), not "changed by this app"); the switch follows | the stream packets for field 16 and field 2 on OpenControl's DLCI 0x02 (the official app writes on its own session) | — | a value not following; a write from OpenControl in reaction |
| BL-end1 | — | Debug → **Export**; the system log; the status bar across a minute change; stop the recordings; **switch to user 10** | "Debug log saved (N lines)." | — | — | an empty file |

#### Part 2 — the secondary user (user 10) **without** Google Play (screen recording with sound + the camera; `APP_TESTPLAN.md` Y14–Y22)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BL-14 | user 10, P0 (no Play), both buds worn, the recordings on | open the app; **Info** 3 s; **Settings** 3 s | the session opens by itself; "App: 1.3.0, build <B1 hash>"; no Play card, no Play line | the Connect sequence with the **fifteen** requests (the subscription and the read); the snapshot claim released ≈ 1.5 s after its answer | `PAIR-003`, `FW-003`, `BATT-004` | Safe Mode; "-dirty"; the card |
| BL-15 | ready, the **Connection** tab | **hold a bud 10 times** ≈ 6 s apart; say each; then open ANC | every hold followed (the mode line / the notification; ANC shows the last) | 10 stream packets; **no app `SABM`** on DLCI 0x04; 0 Play claims (positive control: part 1's count) | `TOUCH-007`, `ANC-001`…`004` | a hold without a packet; an app `SABM` |
| **BL-16** | the **ANC** tab, both worn | **hold a bud 10 times** ≈ 6 s apart | 10 of 10 shown within a second; "Changed by the Buds at …" in the (i) | 10 stream packets; no app `SABM`; **no `DISC` of DLCI 0x04 while the tab is shown — because there is no claim to cut** | `TOUCH-007`, `ANC-001`…`004` | a hold not shown; an app `SABM` |
| BL-17 | the ANC tab | **tap a mode**; wait 5 s; the (i) | the mode; no "Changed by the Buds" line | one claim: `08 11`, `08 12`, ACK, the `Notify`, the stream packet ≈ 0.3 s after the Set, the `DISC` ≈ 1.5 s after the ACK | `ANC-001`…`004` | the own Set shown as the Buds' change |
| BL-18 | the ANC tab | the **ANC tile** once; close; the app | the tile steps one mode; the app follows | as BL-17 | `ANC-001`…`004` | — |
| BL-19 | the ANC tab, both worn | **one bud out**; wait 10 s; **back in**; wait 10 s | as BL-9 (the re-open's read with "Changed since the app last saw it" if it differs, or the Buds' own switch as "Changed by the Buds") | as BL-9 | `PAIR-003`, `TOUCH-007` | as BL-9 |
| BL-20 | ready | **Bluetooth off**; 5 s; **on**; wait 15 s | the loss named; ready again by itself | the re-open's fifteen requests — the subscription again | `PAIR-003` | no subscription after the re-open |
| BL-21 | the Controls tab | hold a bud 5 times ≈ 6 s apart; then ANC | the last mode on ANC; (i) "Changed by the Buds" | 5 packets, no app `SABM` | `TOUCH-007` | — |
| BL-22 | ready | **Disconnect**; wait 5 s; hold a bud twice (no session); **Connect**; the ANC tab, the (i) | after the Connect the mode read; the (i) **"Changed since the app last saw it (read at HH:MM:SS): a press-and-hold on a bud or the Buds' own change, made while this app was not connected."** | the phone's `DISC`; nothing while disconnected; the Connect's fifteen requests; the read's answer with the new mode | `PAIR-003`, `TOUCH-007` | the line missing after a differing read; the line after a read of the same mode |
| BL-23 | the ANC tab | both buds **into the case**; wait 15 s; **out into the ears**; wait 15 s | the session ends with its cause (or the Buds report "no mode settable": the mode kept, "The Buds don't allow changing noise control right now …"); ready again by itself; the mode read | the loss (or a stream packet `4:{13:0}` — `CAP-045` 2277 — and no loss); the re-open's requests; the read's answer (0 while in the case, 1–4 once worn) | `PAIR-003` | a value 0 shown as a mode |
| BL-24 (optional) | the ANC tab | **Home**; wait 10 s; hold a bud twice; return | on return the holds' result shown (the stream arrived while hidden — the session stays open in the foreground service) | 2 packets while hidden; no `SABM` | `TOUCH-007` | — |
| BL-25 (optional, P10) | the Sound tab | the second phone's official app: an EQ slider; a switch | OpenControl follows | the stream packets for 16 / the switch's field | — | — |
| BL-end2 | — | Debug → **Export**; the system log; the status bar across a minute change; stop the recordings; P1's APK pull | "Debug log saved (N lines)." | — | — | — |

### Don'ts

- Do not uninstall the installed 1.2.2 and do not clear the app's data (P1 — the run is an update; the ignored 1.2.x choice must still be in the file).
- Do not open the app before the film (P9).
- In BL-4, BL-5, BL-12, BL-15, BL-16 and BL-21 touch nothing on the phone while holding the buds; say each hold; keep the ears on camera.
- Do not tap two things within 5 s of each other.
- Do not write a serial number unredacted anywhere in this folder (first 4 + last 2 characters); no Wi-Fi name, phone number, account name or e-mail in any
  committed text (the system log is committed as an extract, as `CAP-075`); the camera films carry no address overlay.

### After the run

Into this folder: the screen recordings and the camera films, the system logs (as extracts — the full logs and the originals to
`~/opencontrol-capture-originals/CAP-076/`), every debug export, the app logcats if saved, the HCI snoop logs, the P0/P1 outputs with their exit statuses, the
pulled APK's `sha256sum`, `dist/1.3.0/`'s `.sha256` and the script's certificate line; then `sha256sum *` into CAP-076-sha256sum.txt. Commit on the release
branch after the build commit (`RELEASING.md` C3) — never on `main`.

### Analysis checklist

- [ ] Every filter scoped to the Buds' ACL handle (the Connection Complete for the Buds' address with that handle shown); DLCIs by content; a negative with its
      command, its exit status and a positive control (`AGENTS.md` §13 step 8).
- [ ] P0 (part 1: Play listed, the *Nearby devices* permission state and Play's version on film; part 2: no Play, exit 1 with the positive control), P1 (the
      update: `firstInstallTime` unchanged, `lastUpdateTime` later, `versionCode=10300`); the Info frame's hash = B1's; the pulled APK's SHA-256 = B1's.
- [ ] **The two new requests, byte-identical** (the table above), **once per MAESTRO session**: count the `SubscribeToSettingsChanges` REQUESTs and the
      `ReadSetting 4:13` REQUESTs against the `SABM`s of DLCI 0x02 (Connects + re-opens) in each log — equal; their position after `SubscribeRuntimeInfo` and
      before `GetHardwareInfo`.
- [ ] **The counts for the verdict:** (1) OpenControl's `SABM`s on DLCI 0x04/0x05 (owner by the first phone message, `08 11` = the app, `03 08 00 02 01 25` =
      Play — `CAP-075-FINDINGS.md` §1's method) **vs the taps, tile taps, pulls and Connects/re-opens made** — equal (no claim the user did not cause);
      (2) **holds shown vs holds made** per step (BL-4, 5, 12, 15, 16, 21 — expected all); (3) **stream packets `4:{13:n}` vs `Notify`s** — one packet per
      hold, per own tap and per wear-driven switch, with the `Notify`s only inside the app's or Play's claims; (4) **the first read's value after every
      Connect/re-open** (the RESPONSE after each `ReadSetting 4:13`), its (i) line when it differs from the mode shown before.
- [ ] **BL-7 / BL-17:** the own tap's stream packet classed as the app's change (no "Changed by the Buds" line in the film); its delay after the `Set`.
- [ ] **BL-9 / BL-19 / BL-23:** which of the two happened (the Buds' `DISC` → re-open → read, or a stream packet without a `DISC`); the mode before and after;
      the (i) line; a value 0 never shown as a mode (the "not allowed" line instead).
- [ ] **BL-10 / BL-20:** the subscription after the Bluetooth-off re-open (M1 on hardware).
- [ ] **BL-11:** the taps that collided with Play (`CAP-075` §5's refusal), how many, and that nothing re-claimed afterwards — the known issue of 1.3.0.
- [ ] **BL-13 / BL-25 if run:** the official app's writes on its own session, the stream packets on OpenControl's, the values on screen with the packet's time.
- [ ] The system logs: crashes, ANRs, `StrictMode`; the `bugreportz` SIGQUITs discounted.
- [ ] One row per step — done / not done / done differently; `APP_TESTPLAN.md` section Y and the Summary; traceability (`AGENTS.md` §13 step 7): `PAIR-003`
      (BL-1, BL-3, BL-9, BL-10, BL-14, BL-19, BL-20, BL-22, BL-23), `BATT-004` (BL-1, BL-14), `FW-003` (BL-2, BL-14), `ANC-001`…`ANC-004` (BL-4, BL-5, BL-7,
      BL-8, BL-11, BL-15 … BL-18), `TOUCH-007` (BL-4 … BL-6, BL-9, BL-12, BL-15, BL-16, BL-19, BL-21, BL-22, BL-24) — each referenced in the timeline or flagged
      "expected but not observed".
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `id_registry.csv`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the Group column: BL), `APP_TESTPLAN.md`, `TODO.md`
      §1/§2/§3; write the findings file (CAP-076-FINDINGS); an ADR-065 Update with the numbers; `PROTOCOL.md` §6's wear-change item if BL-9/BL-19/BL-23 settle
      it (a promotion needs the maintainer's approval, `AGENTS.md` §6); the release verdict is the analysing session's, with the maintainer (`RELEASING.md` C4).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-076-2026-10-15_06-35-26_07-08-20-Group_BL/CAP-076-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-076-2026-10-15_06-35-26_07-08-20-Group_BL/CAP-076-EVENT-NOTES
