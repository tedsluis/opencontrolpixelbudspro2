# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, the secondary user without Google Play and the Owner user with it) — Group BO, round 2: release candidate 1.4.0 and the Dosimeter experiment (`CAP-079`)

**Status:** 🟢 **captured 2026-10-18 07:07:31 – 08:42:33 and analyzed 2026-10-18 by `ai-sessions/0099`** (findings: CAP-079-FINDINGS.md). The skeleton of
`ai-sessions/0098` is kept below as the appendix ("the planned procedure"). All four parts ran; several steps were done differently or not at all (the step
mapping below) — the maintainer: "I did not always keep to the test plan." Storage per `DECISIONS.md` ADR-066: the nine films, three full logcats and two
system logs are kept in ~/opencontrol-capture-originals/CAP-079/ (hashes in CAP-079-sha256sums.txt); the small files and two extracts are committed.

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-079` |
| Group(s) | BO |
| Date | 2026-10-18, 07:07:31 – 08:42:33 (local, CEST) — user 10 07:01:07.8 – 07:36:34.2 and 08:00:17.9 – 08:30:44.7, user 0 07:36:34.2 – 08:00:17.9 and from 08:30:44.7 (`am_switch_user`, full logcats) |
| Phone | Pixel 9a, GrapheneOS `CP3A.261005.005` (Android 17; the app logcats' `osVersion`) — **user 10 without Google Play** (parts 1 and 3), **Owner user 0 with Google Play** (parts 2 and 4) |
| Apps | **OpenControl 1.4.0** (`io.github.tedsluis.opencontrolpixelbuds`, versionCode 10400, release key; Info tab "1.4.0, build e2b1c06 (2026-10-17)" on film) and **1.4.0-exp.1** (`….experiment`, debug key; Info "1.4.0-exp.1, build e2b1c06"). The APKs on the phone (`pm path --user 0/10`, one path per package, `adb pull` 2026-10-18) hash to `2f75ad86…511172` and `5280e7b3…071ab11e` = the two APKs built at `e2b1c06` (CAP-079-adb-checks-0099.log). Installed 06:41:56 / 06:42:10 (release) and 06:42:21 / 06:42:28 (experiment), each for users 0 and 10 (full logcat `installation completed` + `ShortcutService … userId=0/10`) |
| Buds | Pixel Buds Pro 2, `release_5.203` (Case, Left, Right — the Buds' announcement; Info tab on film 07:08:41) |
| Video files (local only) | Camera: **c070731** (`recording_20261018_070731`, the `moov` box missing — video recovered from a copy, 3,468 frames, no audio; the camera's burned-in clock 07:07:33 – ≈ 07:09:31), **c070945** (1,111.3 s, 07:09:45.7 – 07:28:17), **c074156** (53.0 s, 07:41:56 – 07:42:49), **c074254** (396.0 s, 07:42:54 – 07:49:30), **c080913** (788.8 s, 08:09:13.2 – 08:22:22; audio track without a sample description, not decodable). Screen: **s072815** (1,220.4 s, **07:07:55.2 + t**, no audio), **s074935** (488.4 s, **07:41:26.8 + t**), **s082214** (782.2 s, **08:09:12.7 + t**), **s084232** (520.8 s, **08:33:52.1 + t**). Part 4 has no camera film. |
| Log files | **H1** CAP-079-btsnoop_hci1.log (part 1, 07:01:12.962 – 07:30:21.781, 6,112 packets); **L1** hci1.log.last (2026-10-17 17:07:42 – 07:01:08, 37,707; `CAP-078`'s CAP-078-btsnoop_hci3.log is its byte prefix — the rest is the evening's Buds traffic to 21:12:57 and another device's LE link overnight); **L2** hci2.log.last (07:36:38.8 – 07:41:36.5, 1,071, user 0 before the Bluetooth off/on, no ACL); **H2** hci3.log.last (part 2, 07:41:38.771 – 08:00:20.097, 7,618; hci2.log is its byte prefix); **H3** hci4.log.last (part 3, 08:00:22.816 – 08:30:45.384, 5,126; hci3.log is its byte prefix); **H4** hci4.log (part 4, 08:30:49.652 – 08:45:00.517, 8,455). Exports **E1** `-072742` (1,302 lines, 07:02:59 – 07:27:28), **E2** `-074943` (448, 07:42:05 – 07:49:39), **E3** `-082157` (2,174, 08:04:14 – 08:21:57), **E4** `-084224` (2,948, 08:31:18 – 08:42:24); app logcats (UTC) **A1** `-e6d5930222ee` (release, user 10), **A2** `-6e1f3e6952b9` (release, user 0), **A3** `-aa5039cbef3d` (experiment, user 10), **A4** `-44863b57e090` (experiment, user 0); full logcats **L-1/2/3** (local time, to 07:28:35 / 07:50:26 / 08:46:23 — local only) → CAP-079-logcat-extract.txt; system logs **Y2** `-180018fac7f6` (UTC, to 05:50:15) and **Y4** `-fbd720889bab` (to 06:42:56) → CAP-079-system-log-extract.txt; CAP-079-adb-shell.log (06:26 – 06:46, the install); CAP-079-adb-checks-0099.log |

## Capture-integrity pre-flight

- 🟢 Nesting: `cmp -n <size of the first> <first> <second>` exit 0 for hci2.log ⊂ hci3.log.last, hci3.log ⊂ hci4.log.last and `CAP-078`'s hci3.log ⊂ hci1.log.last;
  exit 1 for hci1.log vs hci2.log.last and hci2.log.last vs hci2.log. Analysing L1 (from frame 9,435), H1, L2, H2, H3 and H4 covers every packet of all eight files.
- 🟢 The Buds' ACL handle `0x000b` in every part (Connection Complete for `XX:XX:XX:cf:6e:07`): H1 184 07:03:20.176 (three page timeouts 07:01:18 – 07:01:28
  before), H1 5122 07:26:57.682 (after a connection timeout 0x08 at 07:26:49.781); H2 1589 07:43:05.841 → 7606 08:00:18.640 (0x16, the user switch); H3 151
  08:00:28.759 → 692 08:04:28.086 (0x13), 714 08:04:37.068 → 5045 08:30:20.062 (0x13); H4 217 08:30:52.092 → 1415 08:31:37.354 (0x13), 1463 08:31:40.411 → open.
  LE handles 0x0040/0x0041 belong to other devices (counted, not analysed). H1 ends 07:30:21.8 although user 10 ran to 07:36:34 (the snoop log was pulled
  while running; 07:30:22 – 07:36:38 is in no HCI file); L2's gap to H2 is the Bluetooth off/on on film s074935 9–11 (07:41:36 – 07:41:38).
- 🟢 Every pw_hdlc frame CRC-32 checked: H1 716, H2 226, H3 890, H4 1,214 (L1 314 incl. the prefix), 0 bad (`scripts/pwhdlc_crc_table.py <log> 0x000b`).
- 🟢 Clocks: the screen films by status-bar minute flips at 10 fps (s072815 t = 4.8, 64.9, 1205.4; s074935 33.2, 453.2; s082214 47.3, 767.5; s084232 7.9,
  487.9) — ± 0.1 s; the camera films carry their own clock in the picture, equal to the name/`creation_time` within 1 s and to the screen events within ≈ 1 s
  (battery (i) opened at camera 07:10:39, screen 07:10:40); A and Y are UTC (export − 2 h exactly: E2 07:49:07.835 = A2 05:49:07.835), E and L local.
- 🟡 The camera film c070731 had no `moov` box ("moov atom not found"). Its H.264 was recovered from a copy by walking the length-prefixed NAL units of `mdat`
  with the parameter sets of c070945 (the positive control on c070945's first 96,461,054 bytes recovered 3,458 of the 3,459 frames that start there, with no
  decoder error); frame times are the camera's burned-in clock, not a frame index (the film is variable-rate). Its audio could not be recovered.

## Review method and privacy

All nine films at 1 fps (camera 640 px, screen 540 px); every second whose picture changed (> 0.03 % of the pixels below the status bar by > 40 levels) viewed
on labelled contact sheets — screen 213 + 112 + 190 + 131 changed seconds, camera 105 + 737 + 52 + 343 + 504 — the unchanged seconds are pixel-identical below
the threshold; minute flips at 10 fps. Audio of six tracks (16 kHz mono): every segment ≥ −40 dBFS per 100 ms tabulated, the ring tone (3.08–3.20 kHz) with
`scripts/ring_tone_windows.py`. The maintainer did not speak (the maintainer's statement); no speech is attributed. **Ear rule (the maintainer's statement):** head on the
left of the frame = the right ear, head on the right = the left ear — used only where the head is in view. The exports by template and every event line, the
app logcats by tag/PID, the full logcats and system logs by stated filters over 10-18 06:41 – 08:46. The films show the Wi-Fi name, notifications (one with
an account address, s074935 486), a web search and media titles, and the serial numbers on the Info tab: not transcribed; MACs masked in the extracts.

## Event Timeline

Phone time (local) · what happened · step / Test-ID · evidence (film second or camera clock; H/L/E/A/Y = frame or log line).

**Before the recording.** 07:00:28 the release app's tile process in user 0 (PID 6626), force-stopped 07:00:51 from Settings; 07:01:07.813 switch to user 10;
07:01:10 Bluetooth process (user 10); H1 from 07:01:12.96; page timeouts 07:01:18 – 07:01:28; 07:02:59.8 the release app opened (E1 1): permissions;
07:03:05 "Use different Buds" → 1 association removed; 07:03:15 – 17 Android's pairing dialog → association, already bonded; 07:03:20.176 ACL and session 1
(channel 21, buds in the case — mode read 13 = 0 "no mode settable"; one app process in user 10, PID 7046).

| Phone time | Event | Step / Test-ID | Evidence |
|---|---|---|---|
| 07:07:31 – 07:07:55 | Camera and screen recordings start; both buds seated in the open case; Connected, L/R 100 % charging, Case 77 %, "Both buds in the case" | P3, BO-1 (done earlier, 07:03:20, buds in the case) | c070731; s072815 0 |
| 07:08:02 – 07:08:21 | Both buds out of the case to the ears (head below the frame); pushes 13 = 3 (07:08:08.849), 13 = 4 (07:08:23.306) shown as "Probably worn (checked 07:08:08)", Transparency, then Adaptive | `INEAR-002`/`003`, `INEAR-006` | c070731 31–50; H1 742, 769; E1 07:08:08.865 "(read)", 07:08:23.314 "(changed_by_buds)" |
| 07:08:41 – 07:08:51 | Info "1.4.0, build e2b1c06 (2026-10-17)", firmware `release_5.203`, control channel 21; Debug mode on, Unidentified frames (0) | P1 | s072815 37–50 |
| 07:08:58.934 | The Buds close MAESTRO (no bud event on film); re-open 1.5 s later on channel 19 | — | H1 971, 997; E1 "Automatic re-open … AFTER_LOSS" 07:09:00.453 |
| 07:09:08 | Battery (i): "Left/Right %: read from the Buds when the app last reached their Message Stream …" — the 1.3.x text | (P6), defect | s072815 73 |
| 07:09:17, 07:10:37, 07:10:53 | **Refresh battery** ×3: one `SubscribeRuntimeInfo` each, a packet 75–86 ms later; **no DLCI 0x04** | **BO-2, `BATT-004`** | H1 1134 → 1137, 1336 → 1341, 1437 → 1439; s072815 165, 181 |
| 07:11:17 – 07:11:42 | **ANC tab, both worn: Off, NC, Transparency, Adaptive** — `WriteSetting 4:{13:1…4}` on ch 19, each RESPONSE OK and pushed back on 19 (same frame) and 21; no DLCI 0x04 | **BO-3, `ANC-001` … `ANC-004`, `ANC-007`** | H1 1837 … 1985; E1 "written (channel 19)" ×4; c070945 07:11:17 – 40 |
| 07:12:14 – 07:12:21 | One bud out of the ear and laid on the table (Left — by elimination, see 07:13:16); the Buds close MAESTRO, re-open announces **21** | `INEAR-004`, BO-4 | H1 2093, 2123; c070945 07:12:10 – 21 |
| 07:12:33 – 07:13:04 | **Right worn only:** NC, Adaptive, Off, Transparency on ch 21 — OK ×4 | **BO-4** | H1 2183 … 2365 |
| 07:13:16 – 07:13:21 | **Head on the left of the frame: the Right bud out of the ear** (ear rule) and laid on the table; Buds close MAESTRO; re-open on 19; read 13 = 0 | `INEAR-004` | c070945 07:13:16 – 21; H1 2402, 2435, 2484 |
| 07:13:36 | **No bud worn:** tap NC → `RESPONSE` **FAILED_PRECONDITION**; red banner "The Buds don't allow changing …"; 07:13:41 Refresh → 13 = 0 | **BO-5, `ANC-007`** | H1 2495 → 2501; E1 07:13:36.636; s072815 342 |
| 07:14:14 | **In-ear detection off with both buds on the table** → push 13 = 4 (mode settable again) | **BO-6 (done differently: no bud worn)**, `INEAR-001` | H1 2736 → 2742; c070945 07:14:13 – 15 |
| 07:14:17 – 07:14:23 | ANC tab shows Adaptive but still the red "don't allow" banner until a Refresh (07:14:22) | defect (stale `ancError`) | s072815 382 – 388 |
| 07:14:34 – 07:14:52 | NC, Off, Transparency, Adaptive with no bud worn and in-ear detection off — OK ×4 | BO-6 | H1 2850 … 2986 |
| 07:15:14 – 07:15:18 | In-ear detection on (13 = 0), off again 0.2 s later (13 = 4), on (13 = 0) — three writes | BO-6 | H1 3118, 3136, 3143 |
| 07:15:58 – 07:16:19 | **Tile ×3 with no bud worn:** "Not allowed now"; each tap written → FAILED_PRECONDITION, toast "The Buds don't allow changing …" | **BO-7 (done differently)** | H1 3219, 3228, 3243; E1 "ANC tile tapped (shown: ADAPTIVE, the Buds last allowed no change)" ×3; s072815 482 – 508 |
| 07:16:27 – 07:16:51 | **Right bud into the ear** (head on the left of the frame), session re-opened on **21**; **Left into the ear**, re-opened on **19** | `INEAR-003`, `INEAR-002` | c070945 07:16:27 – 41; H1 3253, 3286, 3343, 3374 |
| 07:16:51 – 07:17:06 | **Tile ×4 worn:** Off → NC → Transparency → Adaptive, each OK; no "unavailable" frame between states | **BO-7, P2** | H1 3431 … 3464; s072815 535 – 551 |
| 07:17:59 | **Controls: Off unticked** — `4:{12:{1:1 2:0 3:1 4:1}}` OK; ANC tab: Off disabled, "Only the modes ticked under Controls → …" | **BO-8** | H1 3528 → 3536; s072815 605, 614 |
| 07:18:52 – 07:19:08 | **Tile ×4 with Off unticked:** NC, Transparency, Adaptive, NC — never `13:1` | **BO-8, `ANC-007`** | H1 3653 … 3685; s072815 658 – 674 |
| 07:19:27 – 07:20:36 | **Press-and-holds:** head on the left of the frame (the Right bud) to 07:20:11, then the arm across (the other bud); 13 pushes 4, 3, 2, 4 … 4 on both channels (never 1), each "Changed by the Buds" | **BO-9, `TOUCH-007`** | H1 3700 … 3789; E1 "(changed_by_buds)" ×13; c070945 07:19:27 – 07:20:36 |
| 07:20:58 – 07:21:12 | Both buds out of the ears onto the table; pushes 13 = 3 (07:21:01, ch 19), 13 = 0 (07:21:10) | `INEAR-004` | c070945; H1 3800, 3804 |
| 07:21:18 – 07:21:43 | **Ring Left** (claim 07:21:19.190): ACK + status 02, ACKed by the app; **the bud picked up and touched 07:21:38 – 07:21:48**; tone 07:21:27.0 – 07:21:43.3; **no stop status before the hold ended** ("no stop report within 20 s" 07:21:39.403), released 07:21:40.915; screen "Ringing: Left earbud" — no "Stopped on the bud" | **BO-10 (done; expectation not met)**, `FIND-001`, `FIND-005` | H1 3837 … 3906; E1; c070945 07:21:38 – 50; tone camera 07:21:27 – 43 |
| 07:22:02 – 07:22:29 | **Ring Right** (claim 07:22:04.651): status 01 ACKed; **the bud touched 07:22:12 – 07:22:26 instead of Stop** → Notify `e8 e8 80` and the Buds close DLCI 0x04 and MAESTRO 07:22:17.688 (session loss; re-open on 21), again 07:22:28.969 (re-open skipped by the 10-s guard) | **BO-11 (done differently)**, `FIND-002` | H1 3942 … 4011, 4104; E1 07:22:17 – 07:22:29; tone 07:22:09.4 – 07:22:25.4 |
| 07:22:46 | Re-open on 19 (app resumed), 13 = 0 | — | H1 4171 … 4229 |
| 07:22:54 – 07:23:18 | **Ring both** (claim 07:22:56.465): ACK `ff 01 00 03 04 01 00` + status **03**, ACKed; tone 07:23:01.2 – 07:23:14.5; hold ended at 20 s (07:23:16.622) **0.35 s before the app's Stop** 07:23:16.971 → ACK + status 00 → ACKed; no "Stopped on the bud" ✓ | **BO-12, BO-11's Stop path, `FIND-004`** | H1 4285 … 4388; E1 |
| 07:23:51 – 07:25:06 | **Ring Left, nothing touched:** status 02 ACKed; hold ended "no stop report within 20 s" 07:24:12.142, released 07:24:13.649; **the tone ran 07:23:56.7 – 07:24:55.0 (≈ 63 s after the Ring)**; screen "Ringing: Left earbud" to the Disconnect | **BO-13 (expectation not met)**, `FIND-005` | H1 4523 … 4578; c070945; audio camera 07:23:56.7 – 07:24:55.0 |
| 07:25:14 | Disconnect | `PAIR-003` | H1 4656; E1 |
| 07:25:41 – 07:25:51 | Right bud (head on the left of the frame) and Left bud back into the ears | `INEAR-003`/`002` | c070945 |
| 07:26:20 – 07:26:38 | **Connect / Disconnect ×4 with holds:** in the session of 07:26:20 the push 13 = 3 sits in frame 4806 **before** the `ReadSetting 4:13` RESPONSE of that frame — the read ends normally ("Noise-control mode read"), the mode shown is Transparency; a push 13 = 2 at 07:26:26.172 arrives in the next session **before its own subscription** | **BO-15, `TOUCH-007`** (A93-APP-04) | H1 4802 → 4806, 4877 – 4888; E1 07:26:21.665 – 07:26:38 |
| 07:26:49 – 07:26:59 | Disconnect tap; the ACL drops with reason 0x08 (connection timeout) 15 ms later; "Paired — not connected"; Connect 07:26:52, ACL 07:26:57.682, ready 07:26:58 | `PAIR-003` | H1 5113, 5115, 5122; s072815 1135 – 1145 |
| 07:27:18 – 07:27:24 | **Disconnect / Connect quickly:** SABM 07:27:19.885, Disconnect 07:27:20.474 — logged "(was Ready)"; **no "Connect finished after a Disconnect tap"** | **BO-16 (attempted; the race not hit)** | H1 5573 – 5611; E1 07:27:19.029, 07:27:20.472 |
| 07:27:38 – 07:28:06 | Debug: **Unidentified frames (29)** — DLCI 0x04 Group 0x03 / 0x07 frames from the ring claims; Export "Debug log saved (1303 lines)."; View logs → A1 | **BO-17** | s072815 1184 – 1210 |
| 07:28:15.6 / 07:28:17 | Screen / camera recordings end (part 1) | — | — |
| 07:36:22.596 | Force stop of the release app (Settings, user 10) | BO-17 | L `am_kill` 7046 |
| 07:36:34.212 | **Switch to user 0** | Part 2 | L `am_switch_user: 0` |
| 07:41:26 – 07:41:42 | Screen recording (user 0) starts; Bluetooth off and on (Quick Settings) | — | s074935 0 – 15; L `killBluetoothProcess` 07:41:36.775 |
| 07:42:04 – 07:43:08 | Release app (user 0); Info "1.4.0, build e2b1c06"; "Use different Buds" → forget; the case held and closed/opened; Pair → association; Connect 07:43:05 (buds in the case), Play claims DLCI 0x04 at 07:43:08 | BO-18 | s074935 37 – 101; c074156; c074254 0 – 8; H2 1589, 2001 |
| 07:43:44 | Refresh battery — a packet 49 ms later, no claim | BO-18, `BATT-004` | H2 2674 → 2699 |
| 07:44:04 – 07:44:06 | A Refresh with both buds in the case → 13 = 0, "don't allow" banner | BO-18 | H2 2973; E2 07:44:05.615 |
| 07:44:15 – 07:44:30 | Both buds out of the case into the ears; stays "don't allow" banner with NC shown (07:44:20) | `INEAR-002`/`003`, defect (stale `ancError`) | c074254; s074935 173 |
| 07:44:56 – 07:45:01 | Session lost (Buds), re-open on **19** | — | H2 3347, 3451 |
| 07:45:10 – 07:45:14 | Controls: Adaptive unticked (Off was still unticked from part 1 — the list is the Buds'), then Off ticked, Adaptive ticked — 3 field-12 writes OK | (BO-8's "tick Off again") | H2 3629, 3638, 3645 |
| 07:45:29, 07:45:39 | **ANC tab: Adaptive, Transparency** — OK, no app claim on DLCI 0x04; Play receives the `Notify` for each | **BO-18, `ANC-007`** | H2 3756, 3844; claims #2 (Play) notify e8e840, e8e880 |
| 07:45:52 – 07:46:31 | **Press-and-holds:** head on the left of the frame (the Right bud) 5 holds, then the arm across (the other bud) 5 holds — 10 pushes on both channels, shown "Changed by the Buds" | **BO-18, `TOUCH-007`** | H2 3986 … 4162; c074254 07:45:52 – 07:46:30 |
| 07:47:13 – 07:47:22 | Both buds out onto the table; push 13 = 2 (ch 19) then 13 = 0 | `INEAR-004` | c074254; H2 4472, 4499 |
| 07:47:25 – 07:48:02 | **Ring Left:** attempt 1 refused (Play holds DLCI 0x04), attempt 2 0.66 s later; status 02 ACKed; **Play cuts the claim 4.77 s after the SABM** ("hold ended — the claim was closed by another client"), no re-claim; the bud touched 07:47:55 – 08:01; **the stop status 07:48:00.492 goes to Play, which ACKs it**; screen keeps "Ringing: Left earbud" | **BO-19 (done differently: touch, no Stop)**, `FIND-001`, `FIND-005` | H2 4566 … 4788; E2; c074254; tone 07:47:33 – 07:47:58 |
| 07:48:04 – 07:48:28 | **Ring Right** (attempt 2), cut 4.81 s; the bud touched 07:48:19 – 26 → stop status to Play (07:48:24.599, ACKed by Play); Buds close MAESTRO 07:48:25.259 and again 07:48:28.526 | `FIND-002`, `FIND-005` | H2 4860 … 5134 |
| 07:48:39 – 07:49:09 | **Ring both** (attempt 2), status 03, cut 4.75 s; **Stop** 07:49:04 on film → new claim (attempt 1 refused, attempt 2 kills Play's socket) 07:49:07.688, status 00 ACKed by the app, released 07:49:09.338; Play back 07:49:12.784 | `FIND-004`, `FIND-002` | H2 5367 … 5722; Y2 "already at opened state" 05:48:40.701, 05:48:46.122, 05:49:07.094 UTC |
| 07:49:30 / 07:49:35 | Camera / screen recordings end; export E2 07:49:39 – 43 (not on film) | BO-20 | E2 last line 07:49:39.663 |
| 08:00:11.576 | Force stop of the release app (user 0) | BO-20 | L `am_kill` 14309 |
| 08:00:17.895 | **Switch to user 10** | Part 3 | L |
| 08:00:28 – 08:04:37 | ACL (user 10); release app force-stopped 08:00:29.950; the experiment app opened 08:04:13.8: LINK_BACK re-open, Use different Buds, pair, Connect 08:04:37 (ch 21) | BO-21 prep | H3 151, 593, 791; E3 1 – 30 |
| 08:09:13 – 08:10:25 | Recordings start; both buds out of the case into the ears (camera 08:10:05 – 08:10:25); pushes 13 = 2, 13 = 1 | `INEAR-002`/`003` | c080913; H3 1400, 1418 |
| 08:10:31 – 08:10:40 | Info "1.4.0-exp.1, build e2b1c06"; Debug: the card "Experiments (round 2, not a release)" | BO-21 | s082214 78 – 87 |
| 08:11:00 – 08:11:03 | Buds close MAESTRO; re-open on 19 | — | H3 1607, 1647 |
| 08:11:53 – 08:12:20 | **A with all modes ticked:** 13 = 4, 2, 3, 1 — RESPONSE OK ×4, each pushed | BO-21 (done differently: first with all ticked), `ANC-007` | H3 2213 … 2359; E3 |
| 08:12:26 – 08:12:57 | **Adaptive unticked, then A → Adaptive: RESPONSE OK and the Buds switch to Adaptive** (push 13 = 4 on both channels); A Adaptive again 08:12:57 OK | **BO-21, `ANC-007` (ADR-067 Note)** | H3 2365, 2426 → 2433/2434, 2488 → 2491 |
| 08:13:08 – 08:13:13 | **F1 SetWallclock** `1:1792303991632` → RESPONSE OK 78 ms later; the app shows "**Experiment F1 failed: Timeout**" and Unidentified frames (1) | **BO-22, `WELL-003`** | H3 2559 → 2563; E3 08:13:13.153; s082214 235 – 239 |
| 08:13:22 → | Every runtime-info packet now carries field 2 = the phone's time (absent before) | `WELL-003` | H3 2596 … |
| 08:13:24 – 08:13:43 | **F3 Daily 19, 21** — day index 475; entry 475: 724,528.31 (ch 19) / 49,913,470,976 (ch 21) | BO-23 (after F1, not before), `WELL-001` | H3 2638 → 2644, 2690 → 2692 |
| 08:13:58 – 08:17:11 | **F2 Live 19, 21** (×6) in silence: 3–10 packets/min per channel, values 0–6.3 | BO-24 (silence) | H3; E3 per-minute 13, 11, 6 |
| 08:16:53 – 08:17:11 | Speech-like sound on the screen film's microphone (not on the camera, source not on film); live values unchanged | BO-24 ("speech" — not confirmed) | s082214 audio |
| 08:17:16 – 08:17:42 | Browser: an online radio stream started 08:17:41 (heard in the buds) | BO-24 (music) | s082214 483 – 509 |
| 08:17:42 – 08:22:07 | **Music at four media-volume settings** (panels 08:18:05 – 10, 08:18:27 – 33, 08:19:17 – 22 up, 08:19:45 – 51 down); F2 ×10; live packets 60/min per channel; field 2 ≈ 1.5 × 10⁶ → 2 × 10⁴ → 4 × 10⁸ → 4 × 10⁹ → 1.6 × 10⁶; **no sound-level meter in frame** | **BO-24 (done differently)**, `WELL-002` | H3 live packets; E3 "120 live-stream packets" ×4; c080913 |
| 08:20:06 – 08:20:14 | **F3 19, 21 after** — entry 475: 2,233,065,216 / 52,201,500,672; older days unchanged | BO-25, `WELL-001`/`003` | H3 4120 → 4122, 4150 → 4152 |
| 08:20:43 – 08:21:33 | **F4: 19 on, 19 off — RESPONSE OK** (push field 21 on ch 19, "field 21 not shown"); **21 on, 21 off — FAILED_PRECONDITION** | **BO-26** | H3 4278 … 4499; s082214 690 – 740 |
| 08:21:57 – 08:22:22 | Export "Debug log saved (2175 lines)."; radio stopped 08:22:07; recordings end | BO-27 | s082214 764 – 781 |
| 08:30:36.047 | Force stop of the experiment app (user 10) | BO-27 | L `am_kill` 23914 |
| 08:30:44.705 | **Switch to user 0** | Part 4 | L |
| 08:31:18 – 08:31:42 | Experiment app (user 0): LINK_BACK re-open; **live-dB packets on ch 21 and 19 from 08:31:19.235 — 0.48 s after the SABM, with no `SubscribeToLiveDb` in this session or ACL**; Use different Buds, pair, Connect 08:31:40 (ch 19) | BO-28, unexpected | H4 1008, 1038, 1099; E4 1 – 16 |
| 08:33:52 – 08:35:53 | Screen recording (no camera); Adaptive still unticked (ANC tab, the current mode greyed) → ticked again 08:34:50; two Disconnect/Connect; a music app started 08:35:49 | BO-21's "tick again", BO-28 | s084232 0 – 121; H4 |
| 08:36:14 – 08:36:19 | **F1** again → OK on the wire, "failed: Timeout" in the app | `WELL-003` | H4 4591 → 4593; E4 |
| 08:36:25 – 08:41:40 | F2 ×13 (ch 19 ×7, ch 21 ×6), F3 ×4 with music (60 packets/min per channel); a volume raise 08:39:24 – 27 | **BO-28 (done more broadly)**, `WELL-001`/`002` | H4; E4 "120 live-stream packets" ×5 |
| 08:41:44 – 08:42:10 | F4: 19 on/off OK, 21 on/off FAILED_PRECONDITION | `WELL-002` (field 21) | E4 |
| 08:42:19 – 08:42:33 | Music paused; Export "Debug log saved (2949 lines)."; recording ends | BO-end | s084232 507 – 520 |

**After the recording.** 08:43:06 the Buds close MAESTRO and DLCI 0x04 (Play's claim); 08:43:11 Play claims again; H4 ends 08:45:00.5; the full logcat runs to
08:46:23; 08:44:00 – 08:44:02 three StrictMode CloseGuard reports in the experiment app (framework objects).

## Step mapping (the skeleton, appendix below)

| Step | Done? | How |
|---|---|---|
| P0 | Done differently | the shell log's `grep -i -E "gms|vending"`: user 10 → GrapheneOS's three `app.grapheneos.gmscompat*` packages only (not Play), user 0 → those plus `com.android.vending` and `com.google.android.gms` |
| P1 | Done (hashes by the read-only check of `ai-sessions/0099`) | installs in both users (full logcat); EQ not touched (no "EQ written") |
| P2 | Done | four logs |
| P3 | Done, with gaps | part 1's first camera film truncated; part 1's screen film has no sound; part 3's camera audio unreadable; part 4 no camera |
| P4 | Not observable | TalkBack never on |
| P5 | Done differently | in-ear detection on; Off was unticked at 07:17:59 and stayed so until 07:45:13 |
| P6 | Done | force stops 07:00:51, 07:36:22, 08:00:11, 08:00:29, 08:30:36; the release app's tile process ran beside the experiment app (no second RFCOMM connection on the wire) |
| P7 | **Not done** | no sound-level meter in any film |
| BO-1 | Done differently | Connect with both buds in the case (07:03:20); no DLCI 0x04 at any of the 14 + 5 Connects/re-opens of the release app |
| BO-2 | Done | 3 + 1 Refresh battery, packets 49–86 ms later, no claim |
| BO-3 | Done | 4 of 4 OK |
| BO-4 | Done | Right worn only, 4 of 4 OK |
| BO-5 | Done | status 9 |
| BO-6 | Done differently | in-ear off with no bud worn: 4 of 4 OK |
| BO-7 | Done differently | 3 taps not worn (status 9 ×3), then 4 worn (OK ×4) |
| BO-8 | Done | no `13:1` while Off was unticked (4 tile taps); Off ticked again only in part 2 |
| BO-9 | Done | 13 holds in part 1, 10 in part 2 |
| BO-10 | Done; expectation not met | the touch came 19 s after the Ring; no stop status before the 20-s hold ended |
| BO-11 | Done differently | the Right bud touched instead of Stop (session loss); the app's Stop exercised in BO-12 and part 2 |
| BO-12 | Done | status 03, Stop → status 00 without "Stopped on the bud" |
| BO-13 | Done; expectation not met | the ring ran ≈ 63 s, no stop status within 20 s |
| BO-14 | **Not done** | TalkBack never on |
| BO-15 | Done | the push before the read's RESPONSE (H1 4806) |
| BO-16 | Done differently | Disconnect taps came after Ready; the race (A68-APP-13 (a)) not hit |
| BO-17 | Done | export 1303 lines; force stop 07:36:22 |
| BO-18 | Done differently | no tile tap in user 0; Connect with buds in the case |
| BO-19 | Done differently | touched instead of 10 s + Stop; Ring Right and both added |
| BO-20 | Done | export 07:49:39 – 43, force stop 08:00:11 |
| BO-21 | Done differently | A ×4 with all ticked first; A Adaptive with Adaptive unticked ×2; ticked again in part 4 |
| BO-22 | Done; app result wrong | F1 OK on the wire, "Timeout" in the app; no meter/clock in frame |
| BO-23 | Done differently | after F1, not before |
| BO-24 | Done differently | no meter; silence and music at four volume settings; no speech level |
| BO-25 | Done | after the music |
| BO-26 | Done | 19 OK, 21 FAILED_PRECONDITION |
| BO-27 | Done | export 08:21:57; force stop 08:30:36 |
| BO-28 | Done more broadly | F1, F2 ×13, F3 ×4, F4 ×4 with music; no camera, no meter |

## Test-ID traceability (`AGENTS.md` §13 step 7)

| Test-ID | Observed? | Where |
|---|---|---|
| `ANC-001` … `ANC-004` | Yes — each mode written and confirmed (both users) | BO-3, BO-4, BO-6, BO-7, BO-18 |
| `ANC-007` | Yes — the release path itself and experiment A (incl. a mode unticked) | BO-3 … BO-8, BO-18, BO-21 |
| `INEAR-001` | Yes | 07:14:14, 07:15:14 – 17 |
| `INEAR-002` / `INEAR-003` (not in the skeleton's list) | Yes — the ear rule for the Right at 07:16:27 and 07:25:41 | timeline |
| `INEAR-004` | Yes | 07:12:14, 07:13:16, 07:20:58, 07:47:13 |
| `INEAR-006` (not in the list) | Seen on screen ("Probably worn …", "Not worn …") | s072815 14, 1039 |
| `BATT-004` | Yes — runtime info at every Connect, 4 Refreshes | BO-1, BO-2, BO-18 |
| `FIND-001`, `FIND-002` | Yes | BO-10, BO-11, BO-19 |
| `FIND-004` | Yes (status 03 ×2) | BO-12, part 2 |
| `FIND-005` | Partly — the touch stop reached Play (user 0, 2 of 2); in user 10 no stop status arrived while the app held the channel (2 rings ended past the 20-s hold) | BO-10, BO-13, BO-19 |
| `TOUCH-007` | Yes | BO-9, BO-15, BO-18 |
| `PAIR-003` | Yes | 07:25 – 07:27, every Connect |
| `WELL-001` | Yes | F3 ×8 |
| `WELL-002` | **Expected but not observed as specified** — no meter; the live value was observed against the phone's volume steps instead | BO-24, BO-28 |
| `WELL-003` | Partly — `SetWallclock` answered OK; no day entries were taken **before** F1 | BO-22, BO-23 |

## Appendix — the planned procedure (skeleton of `ai-sessions/0098`, unchanged)

### Preparation

| # | Check | Done |
|---|---|---|
| P0 | `adb shell am get-current-user`; `adb shell pm list packages --user 10 \| grep -E "com.google.android.gms$\|com.android.vending$"; echo "exit=$?"` (nothing, exit 1) and the same for `--user 0` (both, exit 0) | ☐ |
| P1 | **1.4.0** (the release APK of this branch, versionCode 10400) installed over 1.3.1 in **both users**; the **experiment APK 1.4.0-exp.1** (package `….experiment`) in both users; `sha256sum` of both against the RESULT; `dumpsys package <package> \| grep -E "versionName\|lastUpdateTime"` on film or in the shell log | ☐ |
| P2 | HCI snoop log on; a fresh log per part (four parts) | ☐ |
| P3 | Screen recording **with sound** + the camera on the phone and the buds; the status bar across a minute change at the start and the end of each part | ☐ |
| P4 | Do Not Disturb on; TalkBack off at the start (BO-14 turns it on and off) | ☐ |
| P5 | Buds charged; in-ear detection **on** at the start; all four press-and-hold modes ticked (Controls) | ☐ |
| P6 | **One app at a time:** force-stop the other OpenControl package before opening one (`adb shell am force-stop --user N <package>`) | ☐ |
| P7 | Part 3–4 only: a **sound-level meter** (a stand-alone meter preferred; a phone app on a second phone otherwise) in frame next to the buds, its display readable on the camera; a speaker for speech and music at fixed volumes | ☐ |

**Rhythm:** one action, then 5–10 s; say what you do and what you hear. **Never ring with a bud in an ear.**

### Reference frames (built by the app's codecs; every one is pinned by a test with the capture frame it equals)

| Request | Channel 19 (request address `00 3b`) | Channel 21 (`00 4b`) | Real bytes |
|---|---|---|---|
| `WriteSetting 4:{13:1}` Off | `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 04 22 02 68 01 f8 89 dc 6c 7e` | `7e 00 4b 03 10 15 … 22 02 68 01 c8 65 2a 04 7e` | `CAP-078` H 5921 / H 7277 |
| `4:{13:2}` Noise cancellation | — | `… 22 02 68 02 72 34 23 9d 7e` | `CAP-078` H 6491 (`SettingsCodecTest`, `Cap078Fixtures.kt`) |
| `4:{13:3}` Transparency | — | `… 22 02 68 03 e4 04 24 ea 7e` | `CAP-078` H 6540 |
| `4:{13:4}` Adaptive | — | `… 22 02 68 04 47 91 40 74 7e` | `CAP-078` H 7298 |
| The Buds' answers | — | `RESPONSE` OK `7e 00 a5 03 08 01 10 15 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 03 6d 4e d8 7e`; `FAILED_PRECONDITION` (status 9) with no bud worn | `CAP-078` H 6495 / H 6777 |
| Refresh: `ReadSetting 4:13` | `7e 00 3b … 2a 02 20 0d 24 c2 39 47 7e` | `7e 00 4b … 2a 02 20 0d 9e 82 ab ac 7e` | `CAP-056` 1450 / `CAP-051` 1178 |
| Refresh battery: `SubscribeRuntimeInfo` | `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 90 82 1e e6 60 2d 65 a9 7e` | (the channel-21 form) | `CAP-062` 2777 |
| Ring Left / Right / both (DLCI 0x04) | `04 01 00 01 02` / `04 01 00 01 01` / `04 01 00 01 03` | — | `CAP-078` H 7772 / `CAP-025` 2040 / `CAP-078` H 8203 |
| The Buds' ring status, the phone's ACK of it | Buds `04 01 00 01 xx` (`00` = stopped); phone `ff 01 00 02 04 01` | — | `CAP-078` H 7775, 7816 / H 7970 (`RingCodecTest`) |
| Experiment F1 `SetWallclock` | RPC `10 13 1d ea 71 de 7e 25 4e ed 3b 67 2a 07 08 <ms varint>` | `10 15 …` | `CAP-041` 783 (`ExperimentFramesTest`) |
| Experiment F2 `SubscribeToLiveDb` | `7e 00 3b 03 10 13 1d 05 d8 d5 73 25 e2 b6 93 4d e4 61 0c 6a 7e` | `7e 00 4b 03 10 15 1d 05 d8 d5 73 25 e2 b6 93 4d e2 18 d6 68 7e` | `CAP-027` 1212 / 1214 |
| Experiment F3 `FetchDailySummaries` | `7e 00 3b 03 10 13 1d 05 d8 d5 73 25 ce 72 b7 73 f3 b3 c7 26 7e` | `7e 00 4b 03 10 15 1d 05 d8 d5 73 25 ce 72 b7 73 f5 ca 1d 24 7e` | `CAP-058` 5736 |
| Experiment F4 `WriteSetting 4:{21:b}` | `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 05 22 03 a8 01 0b …` — **never on a wire** | `… 2a 05 22 03 a8 01 00 ea ad f6 a5 7e` (off) / `… a8 01 01 7c 9d f1 d2 7e` (on) | `CAP-058` 5747 / 5753 (channel 21 only) |

**Log lines to look for** (the app's debug export): "Noise-control mode written (channel N): <mode>", "Noise-control mode not written: the Buds allow no
change (FAILED_PRECONDITION — no bud worn?)", "Noise-control request failed: …", "Ring status from the Buds: ringing left/right/both | stopped", "Ring status
acknowledged (ff 01 00 02 04 01)", "Ring: claim held until the Buds report the stop (at most 20 s)", "Ring: hold ended — the Buds reported the stop | the claim
was closed by another client | no stop report within 20 s", "Connect finished after a Disconnect tap: its channels are closed again", "EQ written (channel N)";
experiment build: "Experiment A/F1/F2/F3/F4: sending …", "Experiment F: <type> ch=N <method> <status> <fields>", "Experiment F: N live-stream packets in the
last S s".

### Steps

| Step | Action (say it aloud) | Expected on screen | Expected on the wire | Test-ID | "Refuted if" |
|---|---|---|---|---|---|
| **Part 1 — user 10 (no Google Play), OpenControl 1.4.0** | | | | | |
| BO-1 | Connect (both buds worn) | Connected; Left, Right and Case % on the battery card; "Noise control: <mode>" on the Connection tab | the MAESTRO session (DLCI 0x02) with `SubscribeRuntimeInfo` (6.2/6.3/6.1); **no SABM on DLCI 0x04** | `PAIR-003`, `BATT-004` | refuted if DLCI 0x04 is opened at Connect, or L/R stay empty with runtime info carrying 6.2/6.3 |
| BO-2 | Battery card → Refresh battery | the values (and their time) renewed | one `SubscribeRuntimeInfo` and its stream packet; nothing on DLCI 0x04 | `BATT-004` | refuted if DLCI 0x04 is opened |
| BO-3 | ANC tab, both worn: Off, Noise cancellation, Transparency, Adaptive, 8 s apart; listen | the mode follows; the Connection tab's line follows | per tap `WriteSetting 4:{13:n}` (the table above), the push `4:{13:n}` and `RESPONSE` OK; no DLCI 0x04 | `ANC-001` … `ANC-004`, `ANC-007` | refuted if a tap opens DLCI 0x04 or the mode does not change with status OK |
| BO-4 | One bud worn (Left out, kept out of the case): two modes | as BO-3 | as BO-3 | `ANC-007`, `INEAR-004` | — |
| BO-5 | No bud worn (both on the table): tap Noise cancellation | "The Buds don't allow changing noise control right now …"; the mode stays | the write, then `RESPONSE` status 9 (`FAILED_PRECONDITION`) | `ANC-007` | refuted if the app shows the new mode |
| BO-6 | In-ear detection off (Controls), both worn: two modes; in-ear detection on again | as BO-3 | as BO-3 | `ANC-007`, `INEAR-001` | — |
| BO-7 | Quick Settings tile: three taps, 5 s apart | the tile steps through the ticked modes; no "unavailable" flash after a change (P2) | one `WriteSetting 4:{13:n}` per tap | `ANC-007` | refuted if the tile flashes "unavailable" between two states |
| BO-8 | Controls: untick Off for press and hold; ANC tab; the tile twice; tick Off again | Off disabled with "Only the modes ticked under Controls → …"; the tile skips Off | the field-12 write; no write of `13:1` | `ANC-007` (ADR-067 Note) | refuted if `4:{13:1}` is sent while Off is unticked |
| BO-9 | Hold a bud (press and hold) twice | the mode follows ("Changed by the Buds") | the push `4:{13:n}` on both channels | `TOUCH-007` | — |
| BO-10 | **Buds out of the ears**, near the camera: Find → Ring Left; when it rings, **touch the Left bud** | "Ringing: Left earbud …", then "Stopped on the bud at HH:MM:SS" | Ring `04 01 00 01 02`, the Buds' ACK, status `…02` → ACK `ff 01 00 02 04 01`; status `…00` → ACK; the claim released after "hold ended" + the linger | `FIND-001`, `FIND-005` | refuted if a status is not ACKed while DLCI 0x04 is open, or "Stopped on the bud" is missing after the status `00` |
| BO-11 | Ring Right; tap **Stop** in the app after ≈ 5 s | the notice ends; **no** "Stopped on the bud" line | Stop `04 01 00 01 00`, the Buds' status `00` (ACKed) | `FIND-002` | refuted if "Stopped on the bud" appears after the app's own Stop |
| BO-12 | Ring both; listen to both; Stop | "Ringing: Both earbuds …" | `04 01 00 01 03`, ACK, status `03` | `FIND-004` | refuted if only one bud rings or a NAK comes |
| BO-13 | Ring Left and **let it run** (touch nothing) | after the Buds' own stop (14.8–17.1 s in `CAP-078`) "Stopped on the bud at …" | the status `00` without a Stop from the phone; the hold ends at the latest at 20 s | `FIND-005` | — |
| BO-14 | TalkBack on: Settings → Case sounds (each switch row), Controls → a switch row, Sound → the Bass slider, the bottom tabs, Find's buttons; TalkBack off | each row read with its name and state; "Bass, … dB"; each tab once | — | — (A93-APP-07) | refuted if a switch is read without its label |
| BO-15 | Disconnect; tap Connect and **press and hold a bud at once** (within ≈ 1 s); repeat until the mode changes during the Connect | the held mode shown after the Connect | a `4:{13:n}` push between `ReadSetting 4:13` and its answer (if timed right); the read ends with its `RESPONSE` | `TOUCH-007` (A93-APP-04) | refuted if the read fails while its `RESPONSE` came |
| BO-16 | Disconnect; tap Connect and **Disconnect at once** (A68-APP-13) | "Disconnected"; not connected a moment later | the channels the Connect opened are closed again (`DISC`) | `PAIR-003` | refuted if the app shows Connected after the Disconnect tap |
| BO-17 | Export the debug log; force-stop the app | "Debug log saved (N lines)." | — | — | — |
| **Part 2 — the Owner user 0 (with Google Play), OpenControl 1.4.0** | | | | | |
| BO-18 | Connect; the battery card; ANC tab: two modes; the tile once; two press-and-holds | as BO-1/BO-3/BO-9; holds shown with Play holding DLCI 0x04 | no app claim on DLCI 0x04; Play's own claim untouched | `ANC-007`, `TOUCH-007`, `BATT-004` | refuted if the app opens DLCI 0x04 for a mode change |
| BO-19 | Buds out of the ears: Ring Left; touch nothing for 10 s; Stop | "Ringing: Left earbud …"; no "Stopped on the bud" the app did not hear | Ring, the hold, Play's cut (≈ 5.3 s after its loss, `CAP-075`), "hold ended — the claim was closed by another client"; Stop on a new claim | `FIND-001`, `FIND-005` | refuted if the app re-claims by itself after the cut |
| BO-20 | Export; force-stop | — | — | — | — |
| **Part 3 — user 10, the experiment app 1.4.0-exp.1** (force-stop the release app first; gear → Debug) | | | | | |
| BO-21 | Connect; the card "Experiments (round 2, not a release)"; Controls: untick Adaptive; card **A → Adaptive** (both worn); tick Adaptive again | "Experiment A: RESPONSE …" | `WriteSetting 4:{13:4}` while field 12 has Adaptive unticked; the Buds' answer | `ANC-007` (ADR-067 Note) | — (the answer is the result) |
| BO-22 | **F1 Clock** once; the phone's clock and the meter in frame | "Experiment F1: RESPONSE OK" (or an error); the log line "the phone's clock N ms" | `SetWallclock` with N, its `RESPONSE` | `WELL-003` | — |
| BO-23 | **F3 Daily 19**, **F3 Daily 21** (before) | "Experiment F3: sent …" | the `FetchDailySummaries` answers; the log's "Experiment F: RESPONSE … FetchDailySummaries …" | `WELL-001`, `WELL-003` | — |
| BO-24 | **F2 Live 19**, **F2 Live 21**; buds worn; one minute each, read the meter aloud every ≈ 10 s: **silence**, **speech** (normal voice at ≈ 1 m), **music at a low volume**, **music at a high volume** | "Experiment F2: sent …" | `SERVER_STREAM` packets of `SubscribeToLiveDb` (field 2); "N live-stream packets in the last S s" once a minute | `WELL-002` | refuted (for the live value) if the packets do not change with the level |
| BO-25 | **F3 Daily 19**, **F3 Daily 21** (after) | as BO-23 | the answers — a change in the current day entry? | `WELL-001`, `WELL-003` | — |
| BO-26 | **F4 21 on**, wait 10 s, **21 off** (channel 21); then **19 on**, **19 off** (channel 19, never captured) | "Experiment F4: RESPONSE …" | the writes and their answers; any setting push of field 21 | `WELL-002` | — |
| BO-27 | Export; force-stop | — | — | — | — |
| **Part 4 — the Owner user 0, the experiment app** | | | | | |
| BO-28 | Connect; F2 Live 21 for one minute (music at one level, the meter in frame); F3 Daily 21 once | as BO-24/BO-23 — with Google Play present (🟡 iv of draft ADR-060) | as BO-24 | `WELL-002` | — |
| BO-end | Export; the app logcats; `adb logcat -b all`; the four HCI logs; stop the films | — | — | — | — |

### Run card (one page for the maintainer)

1. P0–P7 (two users, two apps, HCI on, film + camera with sound; the meter for parts 3–4).
2. **User 10, 1.4.0:** Connect → Refresh battery → ANC four modes (both worn) → one bud → no bud → in-ear detection off → tile ×3 → untick Off, tab + tile, tick
   again → two holds → (buds out of ears) Ring Left + touch → Ring Right + Stop → Ring both + Stop → Ring Left, let it run → TalkBack → Connect + hold at once
   (repeat) → Connect + Disconnect at once → export.
3. **User 0, 1.4.0:** Connect, battery, two modes, tile, two holds → Ring Left, wait 10 s, Stop → export.
4. **User 10, experiment app:** untick Adaptive, A Adaptive, tick again → F1 → F3 19/21 → F2 19 and 21 with four levels of one minute (read the meter aloud) → F3
   19/21 → F4 21 on/off, 19 on/off → export.
5. **User 0, experiment app:** F2 21 one minute + F3 21 → export.
6. Pull everything; keep the films and the full logcats local (ADR-066).

---

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-079-2026-10-18_07-07-31_08-42-33-Group_BO/CAP-079-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-079-2026-10-18_07-07-31_08-42-33-Group_BO/CAP-079-EVENT-NOTES
