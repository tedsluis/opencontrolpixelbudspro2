# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, the secondary user without Google Play and the Owner user with it) — Group BN, round 1: the experiment build 1.3.1-exp.1 (five untested requests) (`CAP-078`)

**Status:** 🟢 **captured 2026-10-17 16:23:51 – 17:17:41 and analyzed 2026-10-17 by `ai-sessions/0097`** (findings: CAP-078-FINDINGS.md). The skeleton of
`ai-sessions/0096` is kept below as the appendix ("the planned procedure"). The maintainer ran parts 2 and 3 with the experiment build; **part 1 with the release
app was not run** — BN-2 and BN-4 were done in the experiment app, which carries the same 1.3.1 code. Storage per `DECISIONS.md` ADR-066: the two films, the full
logcat and the system log are kept in ~/opencontrol-capture-originals/CAP-078/ (hashes in CAP-078-sha256sums.txt); the small files and two extracts are committed.

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-078` |
| Group(s) | BN |
| Date | 2026-10-17, 16:23:51 – 17:17:41 (local, CEST) — user 10 16:19–17:07, user 0 17:07–17:19 |
| Phone | Pixel 9a, GrapheneOS `CP3A.261005.005` (Android 17) — **user 10 without Google Play** (parts 1–2), **Owner user 0 with Google Play** (part 3) |
| Apps | **Experiment build 1.3.1-exp.1** (`io.github.tedsluis.opencontrolpixelbuds.experiment`, versionCode 10301, build "61a59b9-dirty"; the APK on the phone = SHA-256 `4e5facfa…eadd422`, CAP-078-adb-checks-0097.log), installed in user 10 2026-10-16 10:30:10 and user 0 10:30:27. The **release 1.3.1** is installed beside it in both users; only its Quick Settings tile service ran (never opened) |
| Buds | Pixel Buds Pro 2, `release_5.203` (case, left, right — the Buds' announcement 16:25:12 and 17:10:45; film s1 117, s2 109) |
| Video files | CAP-078-recording1.mp4 (2,077.08 s; phone time = **16:23:51.0 + s1**, ± 0.1 s; audio AAC mono) and CAP-078-recording2.mp4 (470.78 s; **17:09:50.9 + s2**); no camera film of the buds — local only |
| Log files | **H** CAP-078-btsnoop_hci3.log.last (user 10, 16:24:08.942 – 17:07:38.710, 10,057 packets; hci1.log and hci2.log are byte prefixes of it), **H3** CAP-078-btsnoop_hci3.log (user 0, 17:07:42.395 – 17:19:32.604, 9,434), hci1.log.last = hci2.log.last (16:21:42 – 16:24:05, 220 packets, Bluetooth off — no ACL); exports **E1** `-164310` (663 lines, 16:24:16 – 16:40:17), **E2** `-165715` (688, 16:45:09 – 16:56:58), **E3** `-171718` (598, 17:09:35 – 17:17:06); app logcats **A1** `-8bfbd9598ce5` (778, UTC 14:19:54 – 14:43:23), **A2** `-4b3358e819e1` (1,853, 14:19:54 – 14:57:28, a superset start of A1), **A3** `-02c37baa0fc4` (430, 15:09:35 – 15:18:04, user 0); full logcat (158,238 lines, to 16:59:06 — local only) → CAP-078-logcat-extract.txt; system log (78,700 lines, UTC, to 15:18:24) → CAP-078-system-log-extract.txt; CAP-078-adb-shell.log (the install, 2026-10-16 10:20 – 10:31); CAP-078-adb-checks-0097.log |

## Capture-integrity pre-flight

- 🟢 The six HCI files: `cmp -n <size> hci1.log hci2.log` and `hci2.log hci3.log.last` exit 0 (prefixes); hci1.log.last and hci2.log.last have the same SHA-256
  (`37a4b7a8…`). Analysing H, H3 and the 220-packet `.last` covers every packet of all six.
- 🟢 The Buds' ACL handle `0x000b` (Connection Complete for `…:cf:6e:07`: H 170 16:25:12.652, H3 192 17:07:45.656 and 2229 17:10:44.868). H ends at the user switch
  (`0x16`, 17:07:38.487); H3 starts 4 s later with a new Bluetooth process (Y `am_proc_start` PID 23705 15:07:40.484 UTC).
- 🟢 Every pw_hdlc frame CRC-32 checked: 667 + 277, 0 bad (`scripts/pwhdlc_crc_table.py <log> 0x000b`).
- 🟢 Clocks: films by status-bar minute flips (s1 69.0, 189.0, 609.0, 1209.0, 1989.0; s2 9.1, 249.1, 429.1); A and Y in UTC, E and L local (+2 h).

## Review method and privacy

Both films at 1 fps (405 px) with every changed second viewed on contact sheets (film 1: 441 changed of 2,076; film 2: 107 of 470; a change = > 0.03 % of the
pixels below the status bar moved by > 40 levels) and transitions at 10 fps; both audio tracks tabulated (segments ≥ −40 dB, the 3.14-kHz ring tone per 100 ms);
the speech was timed, not transcribed (Checkpoint 0). Every export line by its template (84 templates over 1,949 lines), the phone logs by PID and keyword
filters over the run. The films show the Wi-Fi name, the serial numbers on the Info tab and one private message notification (s2 469): not transcribed; serials
in text first 4 + last 2; MACs masked in the extracts.

## Event Timeline

Phone time (local) · what happened · step / Test-ID · evidence (s1/s2 = film seconds; H/H3 = frame numbers; E/A/L/Y = log lines).

**Before the recording (L).** 16:17:15 experiment app started in user 0 (PID 10158); 16:19:34.489 `am_wtf` "killBluetoothProcess: Deadlock on main thread"
(Bluetooth restarted, PID 10886, user 10); 16:19:53.700 experiment app user 10 (PID 11684); 16:20:57 → 16:21:38.942 a brief switch to user 0 and back;
16:22:11 / 16:23:16 experiment app force-stopped twice (from Settings, PID 11423); 16:21:42 – 16:24:05 the first snoop log (no ACL).

| Phone time | Event | Step / Test-ID | Evidence |
|---|---|---|---|
| 16:23:51 | Recording 1 starts (Quick Settings, "Scherm en audio opnemen") | P3 | s1 0 |
| 16:24:05 – 16:24:08 | Bluetooth off and on again (Quick Settings) | — | s1 14–20; L `am_proc_died` 12970 16:24:05.888, `am_proc_start` 13619 16:24:08.226; H starts 16:24:08.942 |
| 16:24:16 – 16:24:24 | Experiment app opened (PID 13701): Nearby-devices and notification prompts allowed | P7 | s1 25–29; L 16:24:16.028; E1 1–4 |
| 16:24:29 – 16:24:39 | Settings → "Use different Buds?" → Forget: "removed 0 association(s)" (a new package — correct) | P7, A93-APP-01 | s1 38–48; E1 9–10 |
| 16:24:55 – 16:25:03 | Pair a device → Android's dialog → association created, already bonded | P7, `PAIR-003` | s1 64–66; E1 12–15 |
| 16:25:12 – 16:25:14 | Connect: ACL, MAESTRO session 1 (17 requests), Message Stream snapshot; ready; L/R 100 % charging, case 90 % | BN-5, `PAIR-003` | H 170, 247; s1 81–87 |
| 16:25:57 – 16:26:16 | Debug mode on: the experiment card appears; Info tab "1.3.1-exp.1, build 61a59b9-dirty", firmware `release_5.203` | BN-5 | s1 106–122 |
| 16:26:57 – 16:31:56 | Every setting toggled and back: case sounds (fields 28, 27), touch controls (4), the hold modes (12), head gestures (29), in-ear detection (2), conversation detection (22), multipoint (11), an EQ preset (16, 16:30:26), Volume EQ (15), balance (17: Right 47, then Right 1), mono (19) — 24 writes, each `RESPONSE OK` and pushed back | (1.3.1 regression) | H 899 … 3021 (§3 of the FINDINGS: every `WriteSetting`); s1 186–486 |
| 16:32:14 – 16:32:44 | ANC tab with both buds in the case: four Refresh taps, "don't allow changing" — no `Set` sent (Settable `0x00`) | `ANC-004` | E1 194, 203, 220; H 3113 … 3409; s1 491–536 |
| 16:33:05, 16:33:21 | Refresh battery; Disconnect (16:33:17.9) and Connect | — | H 3539, 3633, 3676; s1 552–573 |
| 16:34:00 – 16:34:05 | Right bud out of the case, then worn: pushes 13 = 1, 13 = 4 | `INEAR-002` | H 3880, 3893; s1 605–610 "Probably worn (checked 16:34:00)" |
| 16:34:13 – 16:35:10 | Disconnect; **press-and-holds while disconnected** (speech 16:34:18 – 16:35:06); Connect: snapshot `Notify` `e8 e8 20` (Off, was Adaptive); the (i): "Changed since the app last saw it (read at 16:35:11)" | **BN-2 (done differently)**, `TOUCH-007`, A93-APP-02 | H 3904, 4059; E1 361 "(unchanged)"; s1 623–693 |
| 16:35:36 – 16:36:03 | Disconnect; holds while disconnected; Connect: `Notify` `e8 e8 80` (Transparency); the (i) "Changed since … (read at 16:36:03)" | BN-2, A93-APP-02 | H 4197, 4328; E1 419; s1 705–737 |
| 16:36:37 – 16:37:38 | Buds' pushes 1, 3, 0, 1, 3, 0 and **three session losses** (the Buds' `DISC` 16:36:53.1, 16:37:15.4, 16:37:26.5 — buds in/out; speech 16:37:05 – 16:37:31); re-opens 1.51 s later, the third skipped by the chain guard; Connect 16:37:37 | `INEAR-002`/`003` | H 4495 … 4826; E1 429 … 571; s1 782–838 |
| 16:37:46 – 16:38:22 | Settings → "Use different Buds?" → rotation with the dialog open → Cancel; again in landscape → Cancel; four Activity re-creations; no session closed | **BN-4 (done in the experiment app)**, A93-APP-08 | s1 842–862; E1 629–648; H no DLCI 0x02 `DISC` until 16:44:53 |
| 16:38:45 – 16:38:46 | Pushes 13 = 2, 13 = 1 (both channels) — a hold? | `TOUCH-007` | H 5148 … 5155 |
| 16:39:14 | A "Off" dialog → Cancel (no send) | BN-7 | s1 923–926; E1 0 "Experiment" lines |
| 16:40:17 – 16:41:20 | "USB debugging not allowed" (secondary user), shade | — | s1 978–1046 |
| 16:43:10 – 16:43:23 | Export E1 ("664 lines"); App info → View logs → A1 saved | BN-16 (partial) | s1 1159–1189; A1 14:43:12.832 |
| 16:44:53 | Force stop of the experiment app (Settings) | P6 | s1 1258–1263; L `am_kill` 13701 16:44:53.741; H 5631 `DISC` |
| 16:45:08 – 16:45:12 | Reopened (PID 18853): LINK_BACK re-open, session 8, mode read (read) | BN-5 | E2 1–51; H 5731 |
| 16:46:20 – 16:46:28 | **A: 13 = 1, 2, 3, 4 (channel 19) — RESPONSE OK × 4, pushed back** | **BN-7, `ANC-007`** | H 5921 … 5946; E2; s1 1346–1357 |
| 16:46:50 – 16:47:03 | **E: Read 8, 9, 10 — status UNKNOWN, value 0** | **BN-6, `ANC-008`** | H 5992 … 6069; s1 1377–1390 |
| 16:47:14 – 16:47:38 | A: 1, 2, 3, 4 again — OK × 4 | BN-7 | H 6135 … 6268; s1 1401–1425 |
| 16:47:48 – 16:47:57 | E again — UNKNOWN × 3 | **BN-8** | H 6303 … 6311 |
| 16:48:14 – 16:48:18 | Session loss (Buds' `DISC`), re-open 1.515 s later | `INEAR-002` | H 6340; E2 174–178 |
| 16:48:37 – 16:49:14 | A: 2, 3 (channel 21) — OK; E × 3 — UNKNOWN | **BN-9** 🟡 (one bud) | H 6491 … 6720; s1 1485–1519 |
| 16:49:24 | Push 13 = 0 — no bud worn ("Not worn (checked 16:49:24)") | BN-10, `INEAR-004` | H 6756; E2 273; s1 1625 |
| 16:49:37 – 16:50:44 | **A: 2, 3, 4, 1 — FAILED_PRECONDITION × 4, no push**; E × 3 — UNKNOWN | **BN-10, `ANC-007`** | H 6775 … 7053; s1 1544–1612 |
| 16:51:01 | In-ear detection off (`4:{2:0}`) | BN-11, `INEAR-001` | H 7117; s1 1630–1631 |
| 16:51:33 – 16:51:36 | Pushes 3, 2 (a hold?) | `TOUCH-007` | H 7172 … 7177 |
| 16:52:09 – 16:52:31 | A: 1, 2, 3, 4 — OK × 4 (in-ear detection off); in-ear detection on again (`4:{2:1}` 16:52:31.321) | **BN-11** | H 7277 … 7308; s1 1697–1720 |
| 16:53:15 – 16:53:55 | **D: SubscribeToOobeActions → stream `08 0a`, `08 0c`, then status 14 at 16:53:31.5**; speech 16:53:31 – 16:53:54 (buds out/in); pushes 3, 4, 4; a session loss 16:53:47.9 | **BN-12, `INEAR-007`** | H 7388 … 7571; s1 1762–1818 |
| 16:54:06 – 16:54:11 | Session loss and re-open | `INEAR-002` | H 7583, 7604 |
| 16:54:31 – 16:54:47 | **B1 Left: ACK + status 02, tone 16:54:37 – 16:54:46, stop status 00 (16:54:46.193)** | **BN-13, `FIND-001`, `FIND-005`** | H 7752 … 7817; E2; s1 1837–1855 |
| 16:55:05 – 16:55:22 | **B1 Right: status 01, stop 00 (16:55:20.884)** | BN-13, `FIND-002` | H 7854 … 7908 |
| 16:55:37 – 16:55:55 | **B2 Left + ACK: status 02 ACKed, stop 00 ACKed** | **BN-14, `FIND-005`** | H 7945 … 8016; s1 1904–1906 |
| 16:56:06 – 16:56:22 | **B2 Right + ACK: status 01 ACKed, stop 00 ACKed** | BN-14 | H 8081 … 8151; s1 1931–1935 |
| 16:56:37 – 16:57:05 | **C: Ring both — ACK + status 03; tone 16:56:46.7 – 16:57:05.9**; session loss 16:56:43.6 and re-open | **BN-15, `FIND-004`** | H 8188 … 8216, 8240; s1 1964–1975 |
| 16:56:58 | Push 13 = 0 (buds out) | — | H 8372 |
| 16:57:15 – 16:57:58 | Export E2 ("689 lines"); A2 saved; profile picker opened and closed | BN-16 | s1 2006–2036 |
| 16:58:28 | Recording 1 ends | — | s1 2076 |
| 17:06:38 | Session 12 ended from the phone (the app left) | — | H 9366 |
| 17:07:37.928 | **Switch to user 0** | Part 3 | Y `am_switch_user id=0` |
| 17:07:45 – 17:09:23 | Buds' ACL in user 0; Play services claims the Message Stream (17:07:48); ACL ends `0x13` at 17:09:23 | — | H3 192, 432, 1408 |
| 17:09:34 – 17:10:46 | Experiment app (PID 25531); recording 2 starts 17:09:50.9; Info tab; Debug mode on; Use different Buds → Forget ("removed 0"); Play's heads-up notification; Pair a device → association; Connect 17:10:44 | P7, BN-17 | E3 1–69; s2 0–56; H3 2229 |
| 17:10:47 – 17:12:41 | Play and the app take turns on DLCI 0x04; three session losses (17:11:59, 17:12:14, 17:12:25 — the last not re-opened, chain guard); Connect 17:12:40 | — | H3 claims; E3 103–254; s2 129–171 |
| 17:12:57 – 17:13:28 | **A: 1, 2, 3, 4 — OK × 4; Play's claim receives the `Notify` for each** | **BN-17, `ANC-007`** | H3 4891 … 5277; s2 182–218 |
| 17:13:45 – 17:14:04 | E × 3 — UNKNOWN | `ANC-008` | H3 5418 … 5665; s2 232–247 |
| 17:14:19 – 17:16:19 | **B1 Left, B1 Right, B2 Left, B2 Right**: each ACK + status; Play cuts each app claim ≈ 4.6 s later and receives and ACKs three stop statuses; the app logs "no stop report within 30 s" | **BN-18, `FIND-005`** | H3 5814 … 7606; E3; s2 265–376 |
| 17:16:29 | Stop (ACK) | BN-18 | H3 7731; s2 397 |
| 17:16:34, 17:16:55 | **C twice: ACK + status 03; then ACK only**; tone 17:16:43 – 17:17:05 | **BN-19, `FIND-004`** | H3 7792, 8074; s2 401–423 |
| 17:17:04 | Stop: ACK + status 00 | BN-19 | H3 8287, 8292 |
| 17:17:18 – 17:17:41 | Export E3 ("599 lines"); A3 saved; recording 2 ends | BN-end | s2 448–470 |

**After the recording.** 17:18:24 UTC+2 the system log was taken; H3 runs to 17:19:32 (Play holds DLCI 0x04).

## Step mapping (the skeleton, appendix below)

| Step | Result |
|---|---|
| BN-1 (TalkBack, release app) | **not done** — TalkBack never on film |
| BN-2 (holds while disconnected) | **done differently** (in the experiment app), twice — A93-APP-02 verified |
| BN-3 (hold within 1 s of Connect) | **not exercised** — 0 of 18 reads had a push inside |
| BN-4 (Use different Buds → Cancel, rotate) | **done in the experiment app** — Cancel × 2, rotation with the dialog open, no session closed |
| BN-5 … BN-16 | done (BN-9 🟡 "one bud" from the wire only) |
| BN-17 … BN-19 | done (BN-18 four rings, BN-19 C twice and Stop twice) |
| BN-end | done (three exports, three app logcats, full logcat, system log, HCI logs) |

## Test-ID traceability (`AGENTS.md` §13 step 7)

| Test-ID | Exercised | Where |
|---|---|---|
| `PAIR-003` | yes | 16:24:55 – 16:25:14; 17:10:31 – 17:10:46 |
| `TOUCH-007` | yes (holds while disconnected; pushes not caused by the app) | 16:34:13 – 16:36:03; §8 of the FINDINGS |
| `ANC-007` | yes — 22 sends | 16:46:20 – 16:52:27; 17:12:57 – 17:13:28 |
| `ANC-008` | yes — 15 reads | 16:46:50 – 16:50:44; 17:13:45 – 17:14:04 |
| `INEAR-001` | yes (in-ear detection off and on) | 16:51:01, 16:52:31 |
| `INEAR-002`/`003`/`004` | yes, from the wire only (no camera) | the session losses; 16:49:24 "Not worn" |
| `INEAR-007` | yes | 16:53:15 – 16:53:31 |
| `FIND-001`, `FIND-002` | yes | 16:54:31, 16:55:05; 17:14:19, 17:14:58 |
| `FIND-004` | yes | 16:56:37; 17:16:34, 17:16:55 |
| `FIND-005` | yes | every ring (user 10: the app; user 0: Play services) |
| A93-APP-07 (TalkBack) | **expected but not observed** | BN-1 not run |
| A93-APP-04 (a push during a read) | **expected but not observed** | BN-3 not exercised |

## Appendix — the planned procedure (skeleton of `ai-sessions/0096`, unchanged)

### Preparation

| # | Check | Done |
|---|---|---|
| P0 | `adb shell am get-current-user`; `adb shell pm list packages --user 10 \| grep -E "com.google.android.gms$\|com.android.vending$"; echo "exit=$?"` (nothing, exit 1) and the same for `--user 0` (both, exit 0) | ☐ |
| P1 | The experiment APK installed in **both users** (`adb install --user 10 …`, `--user 0 …`), `sha256sum` against the RESULT's value; `dumpsys package io.github.tedsluis.opencontrolpixelbuds.experiment \| grep -E "versionName\|lastUpdateTime"`. The release app stays at 1.3.1 | ☐ |
| P2 | HCI snoop log on (a fresh log per user part) | ☐ |
| P3 | Screen recording **with sound** + the camera; the status bar across a minute change at the start and the end of each part | ☐ |
| P4 | Do Not Disturb on; TalkBack off at the start (part 1 turns it on and off) | ☐ |
| P5 | Buds charged; in-ear detection **on** at the start (Controls); all four press-and-hold modes ticked | ☐ |
| P6 | **One app at a time:** before opening an app, force-stop the other (`adb shell am force-stop --user N <package>` or App info → Force stop) | ☐ |
| P7 | The experiment app's first start: allow Bluetooth and notifications; *Pair a device* → choose the Buds in Android's dialog (its own association); Debug mode **on** (gear → Debug) | ☐ |

**Rhythm:** one action, then 5–10 s; say what you do and what you hear. Never ring with a bud in an ear (C, B).

### Reference frames (built by the app's codecs; `ExperimentFramesTest` pins them — structural until this run puts them on the wire)

| Experiment | Channel 19 (request address `00 3b`) | Channel 21 (`00 4b`) | Real-byte control |
|---|---|---|---|
| A `WriteSetting 4:{13:1}` Off | `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 04 22 02 68 01 f8 89 dc 6c 7e` | `7e 00 4b 03 10 15 … 2a 04 22 02 68 01 c8 65 2a 04 7e` | the same envelope gives `CAP-077` H 2478 `WriteSetting 4:{2:1}` byte for byte |
| A `4:{13:2}` Noise cancellation | `… 68 02 42 d8 d5 f5 7e` | `… 68 02 72 34 23 9d 7e` | |
| A `4:{13:3}` Transparency | `… 68 03 d4 e8 d2 82 7e` | `… 68 03 e4 04 24 ea 7e` | |
| A `4:{13:4}` Adaptive | `… 68 04 77 7d 5d b6 1c 7e` | `… 68 04 47 91 40 74 7e` | |
| E `ReadSetting 4:8` / `4:9` / `4:10` | `… 51 ae d0 ae 2a 02 20 08 ab 36 53 37 7e` / `… 20 09 3d 06 54 40 7e` / `… 20 0a 87 57 5d d9 7e` | `… 20 08 11 76 c1 dc 7e` / `… 20 09 87 46 c6 ab 7e` / `… 20 0a 3d 17 cf 32 7e` | `CAP-077` `ReadSetting 4:2` ch 19/21 |
| D `SubscribeToOobeActions` (`0x57f3becb`), empty | `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 cb be f3 57 8c b7 ae cf 7e` | `… 25 cb be f3 57 8a ce 74 cd 7e` | `CAP-077` `SubscribeRuntimeInfo` |
| B Ring Left / Right (DLCI 0x04) | `04 01 00 01 02` / `04 01 00 01 01` | — | `CAP-025` 2131 / 2040 |
| B2 the phone's ACK of a Buds status | `ff 01 00 02 04 01` | — | `CAP-025` 2048 (the official app) |
| C Ring both | `04 01 00 01 03` | — | none (spec value) |

Every experiment line in the export starts "Experiment A/B1/B2/C/D/E: …" and names the bytes it sent; the Buds' answers are in the Debug-mode hex lines and the HCI log.

### Steps

| Step | Action (say it aloud) | Expected on screen | Expected on the wire | Test-ID | Result / "Refuted if" |
|---|---|---|---|---|---|
| **Part 1 — user 10, the release app 1.3.1** (the steps `CAP-077` did not run) | | | | | |
| BN-1 | TalkBack on: Settings → Case sounds (touch each switch row), Controls → a switch row, Sound → the Bass slider, the bottom tabs; TalkBack off | each row read with its name and state; "Bass, … dB"; each tab once | — | — (A93-APP-07) | refuted if a switch is read without its label |
| BN-2 | Disconnect; **two press-and-holds while disconnected** (the mode changes, heard); Connect; ANC (i) | **"Changed since the app last saw it (read at …)"** stays after the Connect | the snapshot `Get`'s `Notify` (new mode) and the equal `4:13` answer | `TOUCH-007`, `PAIR-003` (A93-APP-02) | refuted if the (i) shows no "changed since" line |
| BN-3 | Tap Connect and **hold a bud within ≈ 1 s** | the held mode shown | a `4:{13:n}` stream packet between a `ReadSetting` request and its answer (if timed right); the read ends with its RESPONSE | `TOUCH-007` (A93-APP-04) | refuted if a read fails while its RESPONSE came |
| BN-4 | Use different Buds → **Cancel**; rotate the phone with the dialog open; Cancel again | nothing changes; still connected | no `DISC` on DLCI 0x02 | (A93-APP-08) | refuted if Cancel forgets the Buds |
| **Part 2 — user 10, the experiment app** (force-stop the release app first) | | | | | |
| BN-5 | Connect; gear → Debug: the card "Experiments (round 1, not a release)" | the card with A, E, D, B, C | the 1.3.1 Connect sequence (17 requests) | `PAIR-003` | — |
| BN-6 | **E**: Read 8, Read 9, Read 10 (both buds worn) | "Experiment E: sent …" | three `ReadSetting` + their answers (a value or an error status) | `ANC-008` | — (the answer is the result) |
| BN-7 | **A**, both buds worn: Off, Noise canc., Transparency, Adaptive, 8 s apart; listen | the mode follows (through the stream) and "Experiment A: RESPONSE OK" | per tap: the `WriteSetting`, its `RESPONSE`, `4:{13:n}` stream packet(s); a Message Stream `Notify`? | `ANC-007` | refuted (as a way to switch) if the status is not OK or the mode does not change |
| BN-8 | E again (Read 8/9/10) after BN-7 | — | the answers | `ANC-008` | — |
| BN-9 | **A with one bud worn** (take the Left out, keep it out of the case): two modes; then E | — | as BN-7 | `ANC-007`, `ANC-008`, `INEAR-004` | — |
| BN-10 | **A with no bud worn** (both on the table — Settable `00`): Noise canc. | — | the `RESPONSE` (OK? an error?) and whether anything changes | `ANC-007` | — |
| BN-11 | **A with in-ear detection off** (Controls), both worn: two modes; in-ear detection on again | — | as BN-7 | `ANC-007`, `INEAR-001` | — |
| BN-12 | **D**: Subscribe; then, on camera, Left out of the ear, in again; Right out, in again (5 s apart) | "Experiment D: sent" | `SubscribeToOobeActions` and any `SERVER_STREAM` for it (`qhp` 10–13?) | `INEAR-007`, `INEAR-002`/`003` | not refuted by silence alone: say whether any packet came |
| BN-13 | **B1** (buds out of the ears, near the camera/microphone): "B1 Left"; when it rings, **touch the Left bud to stop it** | "Experiment B1: the Buds reported 00 (stopped)" or "no stop report in 30 s" | Ring `…02`, the Buds' ACK and status `…02`, then a status `…00` when touched? | `FIND-005`, `FIND-001` | refuted if no `04 01 00 01 00` comes while the claim is held |
| BN-14 | **B2** the same with "B2 Right + ACK"; touch the Right bud | as BN-13 | each Buds status answered by `ff 01 00 02 04 01` | `FIND-005`, `FIND-002` | — |
| BN-15 | **C** (both buds out of the ears): "Ring both" — listen to both; then Stop | "Experiment C: ACK" or "NAK reason …" | `04 01 00 01 03`, the ACK/NAK; Stop `…00` | `FIND-004` | refuted if NAK or only one bud rings |
| BN-16 | Export the debug log; force-stop the experiment app | "Debug log saved (N lines)." | — | — | — |
| **Part 3 — the Owner user 0 with Google Play** (the experiment app; force-stop the release app there too) | | | | | |
| BN-17 | Connect; **A** once per mode (both worn) | as BN-7 | who gets the Message Stream `Notify` — Play (`03 08 00 02 01 25` claims) or none | `ANC-007` | — |
| BN-18 | **B1** Left; touch to stop | — | how long the held claim lives before Play's recovery cuts it (`CAP-074`: ≈ 5.3 s) | `FIND-005` | — |
| BN-19 | **C** once | — | as BN-15 | `FIND-004` | — |
| BN-end | Export both apps' debug logs; the app logcats; `adb logcat -b all`; the HCI logs; stop the films | — | — | — | — |

### Run card (one page for the maintainer)

1. P0–P7 (two users, two apps, Debug mode, HCI on, film + camera with sound).
2. Part 1 with the **release** app: TalkBack (BN-1), two holds while disconnected → Connect → (i) (BN-2), hold right after Connect (BN-3), Use different Buds → Cancel, rotate, Cancel (BN-4).
3. Force-stop the release app; open the **experiment** app; gear → Debug.
4. E (8, 9, 10) → A (four modes) → E → A one bud worn → A no bud worn → A in-ear detection off → D (each bud out/in on camera) → B1 Left (touch to stop) → B2 Right (touch to stop) → C (buds out of ears) → export.
5. Owner user: experiment app: A per mode, B1, C; export.
6. Pull everything (HCI logs, exports, logcats, `adb logcat -b all`); keep the films and the full logcats local (ADR-066).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-078-2026-10-17_16-23-51_17-17-41-Group_BN/CAP-078-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-078-2026-10-17_16-23-51_17-17-41-Group_BN/CAP-078-EVENT-NOTES
