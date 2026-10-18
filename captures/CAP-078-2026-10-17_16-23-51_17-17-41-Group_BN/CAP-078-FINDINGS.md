# Findings: `CAP-078` (Group BN — round 1: the experiment build 1.3.1-exp.1, five requests no capture had held, in the secondary user without Google Play and in the Owner user with it)

**Analysed by `ai-sessions/0097` (2026-10-17).** Companion: `CAP-078-EVENT-NOTES.md` (the timeline, the steps as run, the Test-IDs). Labels per
`PROJECT_RULES.md` §1: 🟢 FACT (a frame, a log line or a film second), 🟡 HYPOTHESIS, ⚪ ASSUMPTION, ❓ OPEN QUESTION. Names: **H** = CAP-078-btsnoop_hci3.log.last
(user 10, 16:24:08.942 – 17:07:38.710; CAP-078-btsnoop_hci1.log and …hci2.log are byte prefixes of it), **H3** = CAP-078-btsnoop_hci3.log (user 0, 17:07:42.395 –
17:19:32.604), **E1/E2/E3** the exports `-164310`/`-165715`/`-171718`, **A1/A2/A3** the app logcats `-8bfbd9598ce5`/`-4b3358e819e1`/`-02c37baa0fc4` (UTC),
**L** the full logcat (local time; CAP-078-logcat-extract.txt carries every quoted line), **Y** the system log (UTC; CAP-078-system-log-extract.txt), **s1/s2** a
second of recording 1/2 (phone time = 16:23:51.0 + s1 and 17:09:50.9 + s2, ± 0.1 s). Every `tshark` filter runs on the Buds' ACL handle `0x000b` (Connection
Complete for the Buds' address `…:cf:6e:07`: H 170, H3 192 and 2229). Counts: `scripts/pwhdlc_crc_table.py` (every pw_hdlc frame, CRC-32), `scripts/message_stream_claims.py`
(the DLCI 0x04 claims), `scripts/pwrpc_decode.py`; the ring tone and the settings-stream pairing by the session's scratch scripts (proposed for `scripts/`, §14).

**In short (🟢 unless marked).** The installed experiment APK is the build of `ai-sessions/0096` (SHA-256 `4e5facfa…`, pulled from the phone). The
maintainer ran parts 2 and 3; **part 1 (the release app) was not run** — its press-and-hold and dialog steps were done in the experiment app instead, which
carries the same 1.3.1 code. The five experiments:
- **A — `WriteSetting 4:{13:n}` switches the noise-control mode with no Message Stream claim.** 22 writes (18 in user 10, 4 in user 0), byte for byte the
  reference frames: 18 `RESPONSE OK`, each followed within 0.1 s by the Buds' own settings-stream push of exactly the value written; **4 `FAILED_PRECONDITION`**
  with no push — all four while no bud was worn (the stream had just pushed 13 = 0, "no mode settable"). In-ear detection off did not stop it (4 of 4 OK). In
  user 0, Google Play services held the Message Stream at the time and **received the Buds' ANC `Notify` for every write** — the Buds tell the Message Stream
  holder of a change made through setting 13. Whether the change was audible is the maintainer's to say (no transcript).
- **B — the ring status.** For each Ring (4 in user 10, 4 in user 0) the Buds ACKed (`ff 01 00 03 04 01 00`) and sent their own ringing status
  `04 01 00 01 0x`; when the ringing stopped they sent `04 01 00 01 00` — to whichever client then held the Message Stream. In user 10 the app held the claim,
  logged "the Buds reported 00 (stopped)" 4 of 4 and (B2) sent the phone's ACK `ff 01 00 02 04 01` 4 of 4; the Buds never repeated a status, ACKed or not. In
  user 0 Play services cut the app's claim 4.4–4.8 s after each ring command (the app's own connect attempt had cut Play's first) and Play received and
  **ACKed** the stop status itself; the app logged "no stop report within 30 s" 3 of 3. This is the specification's "Syncing ringing status back to Seekers".
- **C — Ring both `04 01 00 01 03` is ACKed and the Buds report status `03`** (user 10 1 of 1, user 0 1 of 2 — the second, sent while ringing, got the ACK only);
  a tone of 19 s followed in user 10. 🟡 That both buds rang is the status byte and the specification's meaning of `0x03`, not a measurement (one microphone).
- **D — `SubscribeToOobeActions` is answered**: two stream packets at once, `08 0a` and `08 0c` (= `qhp` 10 `LEFT_ON_HEAD`, 12 `RIGHT_ON_HEAD`), then 15.7 s later
  the server ended the call with status 14 (`UNAVAILABLE`); nothing for the bud-out/in events that followed. Run once (user 10).
- **E — `ReadSetting 4:8`, `4:9`, `4:10`: 15 of 15 answered with status 2 (`UNKNOWN`) and the value 0** (`4:{8:0}`, `4:{9:0}`, `4:{10:0}`), in every condition.

The 1.3.1 behaviour seen on the way: the kept "changed since" cause (A93-APP-02) **verified twice** (modes changed by holds while disconnected, the (i) said
"Changed since the app last saw it" after the next Connect); "Use different Buds" Cancel twice with a rotation in between (A93-APP-08's dialog survives the
rotation; no session closed); "removed 0 association(s)" correct twice (a new package); TalkBack (A93-APP-07) and a push during a read (A93-APP-04) not
exercised. 12 session losses, every one the Buds' own `DISC` on DLCI 0x02 (a bud in or out), re-opened after 1.51 s except twice by the chain guard.
**Unexpected:** the A pushes are classed "changed_by_buds" by the 1.3.1 cause rule (the app does not know its own `WriteSetting` as a `Set`, §3.4); the Bluetooth
process was killed for a main-thread deadlock 2 min before the run (§12); D's packets were shown as "Unidentified frames" on DLCI 0x02 (§6).

## 0. Build, install, users, logs, privacy, health

- **Build.** On film "App: 1.3.1-exp.1, build 61a59b9-dirty (2026-10-16)" (s1 117–122; s2 7, 109). The APK on the phone (`adb shell pm path --user 10
  io.github.tedsluis.opencontrolpixelbuds.experiment` and `--user 0` → the same single `base.apk`, exit 0; `adb pull`, exit 0) has SHA-256
  `4e5facfa3b6178f518750578f3e1ccc5de169103fd619120ac21740eceadd422` = `ai-sessions/0096`'s (CAP-078-adb-checks-0097.log, 2026-10-17 18:03). 🟢 The tested APK is the
  0096 build. **"-dirty":** `git diff 0292e59 HEAD -- android/` holds only the later test fix; 15 of the 17 string literals of `0292e59`'s `ExperimentRunner.kt`
  are in the APK's 17 DEX files verbatim (`unzip -p … 'classes*.dex' | strings`), the other two are a KDoc comment (not compiled) and "setting 13 takes 1…4"
  (found up to the non-ASCII "…"). 🟡 The committed `0292e59` holds the tested experiment code (no rebuild, Checkpoint 0).
- **Install.** `dumpsys package …experiment`: `versionName=1.3.1-exp.1`, `versionCode=10301`, `User 10 … installed=true`, `firstInstallTime=2026-10-16 10:30:10`;
  `User 0 … installed=true`, `firstInstallTime=2026-10-16 10:30:27`. The release app 1.3.1 is installed in both users, `lastUpdateTime=2026-10-16 14:56:46`
  (the 10-16 shell log's 08:56:17 was earlier — something updated it again that afternoon; the 10-16 `PackageManager` lines are no longer in L's ring buffer:
  `awk '$1=="10-16"' … | grep -c PackageManager` → 0, while 15,309 lines of 10-16 exist). ❓ What installed the release app at 14:56:46.
- **Users.** User 10 (`userType: full.secondary`, A1/A2 headers): `am_switch_user: 10` at 16:21:38.942 (L); 0 Play markers on DLCI 0x04 in H (`frame contains
  03:08:00:02:01:25`, sent, → 0; positive control `CAP-076-btsnoop_hci1.log` → 18). User 0: `am_switch_user id=0` at 15:07:37.928 UTC (Y); the profile picker
  on film names the current user "Zonder Google Play Services" (s1 2021, user 10); H3 15 Play markers; the shell log of 2026-10-16 lists `com.google.android.gms`
  and `com.android.vending` for `--user 0` only.
- **Processes (L, Y; `am_proc_start`/`am_kill`).** The experiment app ran as `next-top-activity`: user 10 PID 11684 (16:19:53), 13365 (16:22:50), **13701**
  (16:24:16.028 – force-stopped 16:44:53.741: E1, A1), **18853** (16:45:08.606: E2, A2); user 0 **25531** (17:09:34.346: E3, A3). **The release app ran only as
  `bound-service` for `…app.AncTileService`** (its Quick Settings tile — six starts in user 10, four in user 0) and logged **0** lines with the app's tag
  (`awk '$3==PID && /OpenControlBuds/'` → 0 for each; positive control PIDs 13701/18853 → 729). No two RFCOMM sessions of the Buds overlap on DLCI 0x02 or 0x04 in
  H or H3 (§1). 🟢 Part 1 (BN-1 … BN-4 with the release app) was not run; no second app held a channel.
- **Logs.** Six HCI files: hci1.log ⊂ hci2.log ⊂ hci3.log.last (`cmp -n <size>`, exit 0); hci1.log.last = hci2.log.last (same SHA-256 `37a4b7a8…`, 220 packets
  16:21:42.522 – 16:24:05.623, 0 ACL packets — Bluetooth was off: film s1 14–17 "Bluetooth staat uit"). H covers 16:24:08.942 – 17:07:38.710; H3 starts 17:07:42.395
  after the user switch (the Bluetooth process restarted: `am_proc_died` user 10 PID 13619 15:07:40.001 UTC, `am_proc_start` user 0 PID 23705 15:07:40.484, Y).
  L ends 16:59:06 — part 3 is only in Y. Clocks: A and Y in UTC, E and L in local time (+2 h; the same `am_proc_start` PID 11426 at 16:19:41.132 / 14:19:41.132).
- **Films.** Recording 1: 2,077.08 s, 1080 × 2424, AAC mono 44.1 kHz; covers 16:23:51.0 – 16:58:28.1 (status-bar minute flips at s1 69.0, 189.0, 609.0, 1209.0,
  1989.0, 10 fps; `creation_time` 16:59:15 is 47 s after the end). Recording 2: 470.78 s; 17:09:50.9 – 17:17:41.7 (flips s2 9.1, 249.1, 429.1). Cross-check: s1 1162
  toast "Debug log saved (664 lines)" (16:43:13) – A1 14:43:12.832 "Debug log exported (664 lines)". Both kept local (ADR-066), hashes in CAP-078-sha256sums.txt.
- **Privacy.** On film: the Wi-Fi name in Quick Settings, the serial numbers on the Info tab, a private message notification (s2 469) — not transcribed. The
  extracts have every MAC masked to `XX:XX:XX:…` and 0 e-mail addresses, 0 Wi-Fi names, 0 serial numbers (`grep -c`); serials in text: Case `5707…51`, Right
  `5708…09`, Left `5707…47`.
- **Health.** 0 `FATAL EXCEPTION`/`ANR in`/`am_crash`/`am_anr` lines naming either package in L (10-17 16:19–16:59) or Y (15:07– UTC) (`grep -E … | grep -c
  opencontrolpixelbuds` → 0; positive control: the same pattern over L, any package →
  other packages: 8). E1–E3: 0 "Safe Mode", 0 "malformed" (`grep -c -i`).

## 1. ACLs, MAESTRO sessions and the Message Stream claims

- **ACLs (handle `0x000b`).** H: 170 16:25:12.652 → 10039 17:07:38.487, reason `0x16` (local host — the user switch); an LE connection `0x0040` to another device
  (`…:16:a2:43`, 8778 17:04:53 → 10023, `0x16`) is not the Buds. H3: 192 17:07:45.656 → 1408 17:09:23.436, reason **`0x13`** (remote user terminated — before the
  app's process started at 17:09:34; ❓ cause), 2229 17:10:44.868 → open at the log's end.
- **MAESTRO sessions (DLCI 0x02 `SABM` → `DISC`; `tshark -Y "bthci_acl.chandle==0x000b && btrfcomm.dlci==0x02 && btrfcomm.frame_type!=0xef"`).** User 10: 12 —
  16:25:12.8 – 16:33:17.9 (phone, Disconnect tap), 16:33:21.6 – 16:34:13.8 (phone), 16:35:10.3 – 16:35:36.5 (phone), 16:36:02.8 – 16:36:53.1 (**Buds**),
  16:36:55.1 – 16:37:15.4 (Buds), 16:37:17.4 – 16:37:26.5 (Buds), 16:37:37.5 – 16:44:53.9 (phone, force stop), 16:45:10.2 – 16:48:14.5 (Buds), 16:48:16.5 –
  16:53:47.9 (Buds), 16:53:49.8 – 16:54:06.9 (Buds), 16:54:08.9 – 16:56:43.6 (Buds), 16:56:45.7 – 17:06:38.4 (phone). User 0: 6 (17:10:44.9 …; 5 ended by the
  Buds). Per session one `SubscribeToSettingsChanges`, one `ReadSetting 4:13`, one `GetHardwareInfo` (user 10: 12/12/12, `SubscribeRuntimeInfo` 13 — one extra on
  Refresh, E1 "Runtime info re-requested on Refresh"; user 0: 6/6/6/6).
- **Message Stream claims** (`scripts/message_stream_claims.py`): user 10 **22**, all the app's (Connect snapshots, Refresh, the ring experiments); user 0 **29**,
  15 Play services' (marker `03 08 00 02 01 25`) and 14 the app's. In user 0 every app claim that met a Play claim followed the known contention
  (`ARCHITECTURE.md` §6.0b, ADR-032): the app's connect attempt failed ("RFCOMM channel 0x04 connect failed after 111 … 559 ms (attempt 1/3)", E3 9 times) and
  cut Play's claim at the same millisecond (e.g. E3 17:14:19.097 – H3 Play `DISC` 17:14:19.098), and the retry won ("connected (attempt 2/3)"). An app claim
  that stayed open (the four rings) was closed from the phone side 5.24–5.45 s after Play's loss (E3 "on-demand channel 0x04 closed (IOException …)"), and
  Play re-claimed 2.22–2.57 s after that — `CAP-075`'s timers (5.3–5.8 s / 2.1–2.8 s) to within 0.1 s (§4.2).

## 2. The Connect sequence

Every session sent the 1.3.1 requests of `CAP-077` §2 (`SubscribeRuntimeInfo`, `ReadSetting` 4:16, …, `SubscribeToSettingsChanges`, `ReadSetting 4:13`,
`GetHardwareInfo`); the pairs counted above are one per session in 18 of 18 sessions. 🟢 Outside the experiment source sets the build changed two main files
(`git diff --stat 61a59b9 0292e59 -- android/data/src/main android/app/src/main`: `BudsRepositoryImpl.kt` 28 lines — the "(experiment seam)" visibility —
and `MainActivity.kt` 3 lines — the card's slot).

**Every other write.** 16:26:57 – 16:31:56 the maintainer toggled every setting and back (H 899 … 3018): 24 `WriteSetting`s — fields 28, 27 (case sounds), 4
(touch controls), 12 (hold modes), 29 (head gestures), 2 (in-ear detection), 22 (conversation), 11 (multipoint), 16 (an EQ preset), 15 (Volume EQ), 17 (balance,
three), 19 (mono) — each `RESPONSE OK` and pushed back on the settings stream (one or two packets); the (i) lines on film said "changed hh:mm:ss" for each
(s1 192 … 481). 🟢 No write that the user did not make; settings 3 and 6 never written.

## 3. Experiment A — `WriteSetting 4:{13:n}` (`ANC-007`)

### 3.1 The frames
Every send equals the reference table of the skeleton byte for byte; e.g. H 5921 16:46:20.072 sent (unescaped) `00 3b 03 10 13 1d ea 71 de 7e 25 1d 9a 8c 9e 2a 04 22
02 68 01 f8 89 dc 6c` (channel 19, `4:{13:1}`), answered by 5924 `80 a3 03 08 01 10 13 1d ea 71 de 7e 25 1d 9a 8c 9e 4c 05 e6 d9` (RESPONSE, status 0) and pushed by
5923 `80 a3 03 2a 04 22 02 68 01 08 07 10 13 …` (SERVER_STREAM `4:{13:1}`). A `FAILED_PRECONDITION`: 6775 → 6777 `00 a5 03 08 01 10 15 … 30 09 …` (field 6 = 9).
Command: `python3 scripts/pwhdlc_crc_table.py <log> 0x000b | awk -F'\t' '$8 ~ /WriteSetting/'`.

### 3.2 Every send (H, user 10 — 18; H3, user 0 — 4)

| Phone time | n | Ch | Status | Settings-stream push after it | Condition (wire / film) |
|---|---|---|---|---|---|
| 16:46:20.072 / 22.868 / 25.434 / 28.021 | 1 2 3 4 | 19 | OK × 4 | 13 = n within 0.15 s (2 on ch 19 + 1 on ch 21; n = 1: 1) | session after the LINK_BACK re-open; mode settable (E2 16:45:11 "(read)") |
| 16:47:14.393 / 23.282 / 30.658 / 38.114 | 1 2 3 4 | 19 | OK × 4 | 13 = n, three packets each | — |
| 16:48:37.994 / 45.700 | 2 3 | 21 | OK × 2 | 13 = n, three packets each | after the Buds' `DISC` 16:48:14.5 (a bud moved — BN-9's one bud?) |
| 16:49:37.050 / 16:50:07.829 / 15.496 / 23.395 | 2 3 4 1 | 21 | **FAILED_PRECONDITION × 4** | **none** | push 13 = 0 at 16:49:24.909 (E2 "no mode settable"); film s1 1625 "Not worn (checked 16:49:24)" |
| 16:52:09.828 / 14.950 / 21.474 / 27.182 | 1 2 3 4 | 21 | OK × 4 | 13 = n, three packets each | in-ear detection off: `4:{2:0}` 16:51:01.473 … `4:{2:1}` 16:52:31.321 |
| 17:12:57.38 / 17:13:09.29 / 19.4 / 28.4 (H3) | 1 2 3 4 | 19 | OK × 4 | 13 = n, three packets each | Play holds DLCI 0x04 (claim 4748, 17:12:46.418 – 17:14:19.098) |

🟢 **22 sends, 18 OK, 4 FAILED_PRECONDITION; every OK pushed back as 13 = n; no push after a FAILED_PRECONDITION.** 🟢 **No DLCI 0x04 frame of any client during
the user-10 sends** (claims at 16:45:10 and 16:48:16–18 only; §1) — the switch needs no Message Stream.

### 3.3 The Message Stream holder learns the change
🟢 In H3 Play's claim received the Buds' `Notify` `08 13 00 04 01 e8 e8 xx` after each send: 17:12:57.512 `20`, 17:13:09.401 `08`, 17:13:19.502 `80`, 17:13:28.437
`40` — the Off / Noise cancellation / Transparency / Adaptive bits of `PROTOCOL.md` §4.1 for n = 1 … 4. 🟡 In user 10 nobody held DLCI 0x04, so no `Notify` was
sent (consistent with ADR-064's observation that the Buds report on the holder's channel only).

### 3.4 What the app made of it
E2/E3 classed every push after an own `WriteSetting` as **"(changed_by_buds)"** (e.g. E2 16:46:22.989), the two duplicates as "(unchanged)": the 1.3.1 cause rule
only knows its own Message Stream `Set`s (ARCHITECTURE §3.1). 🟡 If the app switches through setting 13 (round 2), its own write must count as `set_by_app`
(proposal P1, §14). The Debug tab does not show the mode; the notification shade (s1 2012: "Connected — noise control: Transparency") and the ANC tab showed the
pushed mode.

### 3.5 Open
❓ Whether every OK write was **audible** (the maintainer's speech is timed, not transcribed). ❓ The meaning of `FAILED_PRECONDITION` beyond "no bud worn" (one
condition only). ❓ Why a write on channel 19 is pushed twice on 19 and once on 21 (also `CAP-077` §9.1 — two subscriptions on the writing channel?).

## 4. Experiment B — the ring status and the phone's ACK (`FIND-001`, `FIND-002`, `FIND-005`)

### 4.1 User 10 (H; the app held every claim)

| Experiment | Ring sent | Buds' ACK | Buds' status | Phone's ACK of it | Tone (audio, bin/tone.py) | Stop status | Phone's ACK | Claim end |
|---|---|---|---|---|---|---|---|---|
| B1 Left | 7772 16:54:31.177 `04 01 00 01 02` | 7774 `ff 01 00 03 04 01 00` | 7775 `04 01 00 01 02` | — (B1) | 16:54:37.5 – 46.7 | 7816 16:54:46.193 `04 01 00 01 00` | — | 16:54:47.719 (phone) |
| B1 Right | 7870 16:55:05.635 `…01` | 7874 | 7877 `…01` | — | 16:55:11.5 – 21.6 | 7907 16:55:20.884 | — | 16:55:22.410 |
| B2 Left | 7965 16:55:37.222 `…02` | 7967 | 7968 `…02` | 7970 16:55:37.289 `ff 01 00 02 04 01` | 16:55:43.5 – 53.4 | 8013 16:55:54.299 | 8014 .315 | 16:55:55.831 |
| B2 Right | 8100 16:56:06.227 `…01` | 8103 | 8104 `…01` | 8106 .256 | 16:56:12.1 – 19.4 | 8145 16:56:20.986 | 8147 16:56:21.000 | 16:56:22.516 |

🟢 Each ring: the Buds' ACK carries one byte more than the specification's 2-byte form (`… 04 01 00` — `PROTOCOL.md` §4.4), then the Buds' own status equal to the
command; at the end a status `00`. 🟢 **The Buds sent each status once** — B1 (no phone ACK) and B2 (ACKed within 7–16 ms) look the same on the wire. 🟡 The stops
came 14.8–17.1 s after the command; whether the maintainer's touch or a Buds time-out ended them is not on the wire (the camera did not film the buds; no speech at
those seconds). 🟢 The tone (≈ 3.14 kHz, 1.1–1.5-s pulses ≈ 3 s apart) began 5.9–6.3 s after the command; its last pulse lay within 1.6 s of the stop status.

### 4.2 User 0 (H3; Play services re-claimed)
B1 Left 5814 17:14:19.916, B1 Right 6626 17:14:58.831, B2 Left 6989 17:15:30.478 (+ phone ACK 6997), B2 Right 7367 17:16:02.032 (+ ACK 7381): each ACKed and echoed
as in 4.1. 🟢 Each app claim was closed from the phone side 4.45–4.86 s after it opened — 5.24–5.45 s after Play had lost its own claim to the app's connect — and Play
re-claimed 2.22–2.57 s later (claims 5814 → 17:14:24.522, 6605 → 17:15:03.447, 6973 → 17:15:35.124, 7351 → 17:16:06.111; E3 "on-demand channel 0x04 closed
(IOException: bt socket closed …)" each time). 🟢 **Play's claim then received the stop status and ACKed it**: 6833 17:15:16.378 `04 01 00 01 00` → 6836 `ff 01 00 02 04 01`;
7204 17:15:44.888 → 7206; 7605 17:16:19.049 → 7606. For B1 Left no stop status was seen by anyone (its tone ended ≈ 17:14:42.3; Play's claims 5949 and 6200
span 17:14:27.1–38.6 and 17:14:44.1–50.4). E3: "no stop report within
30 s — claim released" 3 times, "the Buds reported 00" never. Tone: 17:14:35.9 – 42.3, 17:15:08.1 – 15.2, 17:15:39.7 – 44.0, 17:16:11.3 – 18.5.

### 4.3 Reading
🟢 `FIND-005` ("Syncing ringing status back to Seekers", Device Action page): the Buds report the start and the stop of ringing, unprompted, to the client holding the
Message Stream, and Play services ACKs the report. 🟡 An unACKed report is not repeated (4 of 4 B1 in user 10). Stop by the app's own `Stop` (user 0 7731
17:16:29.785, 8287 17:17:04.593) → the ACK, and once the status `00` (8292).

## 5. Experiment C — Ring both (`FIND-004`)
🟢 User 10: 8203 16:56:37.848 `04 01 00 01 03` → 8210 `ff 01 00 03 04 01 00` → 8211 16:56:37.904 `04 01 00 01 03`; claim released 16:56:39.374 (1.5 s, as built: C
holds no claim); the tone ran 16:56:46.7 – 16:57:05.9 (19.2 s, longer than any single ring), no stop status (no holder). User 0: 7792 17:16:34.046 → ACK 7796 +
status 7797 `…03`; 8074 17:16:55.029 (sent while ringing) → ACK 8078 only. 🟢 The Buds accept a **local** Ring both and report it as `03`; the Device Action page
defines `0x03` as "Ring both left and right" (fetched 2026-10-17). 🟡 Both buds sounding is not separately measured. ⚠ This contradicts the reasoning of ADR-027
("no local mechanism") — a decision for the maintainer (§14).

## 6. Experiment D — `SubscribeToOobeActions` (`INEAR-007`)
🟢 H 7388 16:53:15.779 REQUEST ch 21 `00 4b 03 10 15 1d ea 71 de 7e 25 cb be f3 57 8a ce 74 cd` (= the reference; method `0x57f3becb`, empty payload) → 7390
16:53:15.827 SERVER_STREAM payload `08 0a`, 7391 .828 `08 0c`, then **7429 16:53:31.515 RESPONSE status 14 (`UNAVAILABLE`)**, empty. 🟡 10/12 = `qhp`
`LEFT_ON_HEAD`/`RIGHT_ON_HEAD` (`REVERSE_ENGINEERING.md`, `qhp.java:8–24`) — the state at subscribe time, both buds worn. ❓ Why the server ended the call 15.7 s
later (speech starts at 16:53:31.1 — the maintainer taking the left bud out?); no event reached the app for the outs/ins that followed (positive control: the
settings stream pushed 13 = 3 at 16:53:31.580 and 13 = 4 at 16:53:40.181 on the same channel). The app's codec listed the three packets as "Unidentified frames —
DLCI 0x02 Group=? 18/18/16 bytes" (film s1 1797).

## 7. Experiment E — `ReadSetting 4:8/9/10` (`ANC-008`)
🟢 15 of 15 answered with **status 2 (`UNKNOWN`)** and a value 0: e.g. H 5992 16:46:50.873 `… 2a 02 20 08 …` → 5994 `80 a3 03 2a 04 22 02 40 00 08 01 … 30 02 …`
(`4:{8:0}`); `4:9` → `22 02 48 00`, `4:10` → `22 02 50 00`. Conditions: both worn (16:46:50–16:47:03, 16:47:48–57), after a bud event (16:48:54–16:49:14), no
bud worn (16:50:34–44), user 0 (17:13:45–17:14:04) — identical. 🟡 The firmware `release_5.203` does not implement settings 8/9/10 (the status), the zero being a
default; ❓ the APK's meaning of 8/9/10 stays open (`PROTOCOL.md` §6).

## 8. The press-and-holds and the settings stream
Pushes of 13 not caused by an app write (H): 16:34:00.719 13 = 1, 16:34:05.432 13 = 4, 16:36:37.555 1, 16:36:49.815 3, 16:36:59.391 0, 16:37:05.004 1, 16:37:10.739
3, 16:37:21.495 0, 16:38:45.212 2 (×2 ch), 16:38:46.718 1 (×2), 16:51:33.492 3, 16:51:36.516 2, 16:53:31.580 3, 16:53:40.181 4, 16:53:55.166 4, 16:56:58.694 0;
H3: 17:12:06.509 4, 17:12:20.366 0, 17:14:48.792 4, 17:15:25.252 0. Every one reached the export within 25 ms with a cause (E1–E3 "(changed_by_buds)",
"(unchanged)", "no mode settable"). 🟡 Which were holds and which in-ear changes is not separable without a transcript; the values 0 coincide with "Not worn"
on film. **Holds while disconnected (BN-2, done in the experiment app):** the Connect snapshots' `Notify` 16:35:10.619 `e8 e8 20` (Off) after the last push 4
(Adaptive, 16:34:05) and 16:36:03.116 `e8 e8 80` (Transparency) after Off; the (i) on film said "Changed since the app last saw it (read at 16:35:11) …" (s1
692–693) and "(read at 16:36:03)" (s1 737) while E1 logged "(unchanged)" for the equal 4:13 reads — 🟢 **A93-APP-02 verified**: the read did not overwrite the
kept cause. **A push during a read (BN-3, A93-APP-04): not exercised** — 0 of 18 `ReadSetting 4:13` had a push between request and answer.

## 9. The release app
🟢 Not opened in the window (no `top-activity` start in L/Y); its tile service was bound by Quick Settings and logged nothing with the app's tag. Part 1 (BN-1
TalkBack, BN-2/BN-3 with 1.3.1-release, BN-4) not run with it. 🟢 BN-4 was run in the experiment app: "Use different Buds?" opened, rotated with the dialog open
(s1 846–857), Cancel twice (s1 849, 860); E1 shows four Activity re-creations (16:37:57.315, 16:38:01.414, 16:38:06.372, 16:38:12.299 "Permissions (start)")
and no DLCI 0x02 `DISC` between 16:37:37.5 and 16:44:53.9.

## 10. The sessions, losses and re-opens (ADR-044)
🟢 12 "Session lost: channel 0x02 closed … read return: -1" (E1 3, E2 4, E3 5), each 12–18 ms after a Buds' `DISC` on DLCI 0x02 (H 4526, 4661, 4794, 6340,
7441, 7583, 8240; H3 3598, 4100, 4363, 6062, 6330); re-open "AFTER_LOSS" 1.509–1.515 s later (`LOSS_REOPEN_DELAY_MS`), skipped twice by the chain guard ("lost 8986 ms / 9480 ms after an
automatic re-open", E1 16:37:26.487, E3 17:12:25.541). One LINK_BACK re-open (E2 16:45:10.006). Film s1 782, 805, 816; s2 129, 143, 155: "The app's channel was
closed while Android still shows the Buds connected …".

## 11. Playback and audio
🟢 No media: AVRCP `PlaybackStatus` Stopped (`0x00`) at every registration; AVDTP START/SUSPEND pairs every 3–7 s (H 160, H3 43 START commands) — short system
sounds, not music. Audio track: median −85.8 dB per 100 ms; speech-like segments (f0 ≈ 120–130 Hz) e.g. 16:33:58, 16:34:47 – 16:35:54, 16:37:05 – 16:37:31,
16:53:31 – 16:53:54, 17:12:04 – 17:12:32, 17:14:50, 17:15:24, 17:15:44 (scratch tables; words not transcribed, Checkpoint 0).

## 12. Unexpected findings
1. 🟢 L 16:19:34.489 `am_wtf … com.android.bluetooth … killBluetoothProcess: Deadlock on main thread` — before the run; the stack restarted (PID 10886).
2. 🟢 The A pushes classed "changed_by_buds" (§3.4).
3. 🟢 H3 1408 17:09:23.436 ACL ended with `0x13` 11 s before the app started in user 0 — ❓ who.
4. 🟢 D's three packets surfaced as Unidentified frames (§6) — the app's decoder has no name for `0x57f3becb` (E2 "method=Maestro/0x57f3becb").
5. 🟢 User 0 "Google Play-services has crashed" notifications (GmsCompat, "3 u" = ≈ 3 h earlier, s2 469) — not in the window.
6. 🟢 Nothing on DLCI 0x08/0x0a in user 10 (H: 0 frames; H3: 54 sent / 95 received on 0x08, 3 claims on 0x0a — Play services); no CRC failure (0 of 944); every
   DLCI 0x02 packet named by the decoder.

## 13. The skeleton's expectations, step by step
BN-1 not done · BN-2 done differently (experiment app; verified) · BN-3 not exercised · BN-4 done in the experiment app (verified) · BN-5 done (card at s1 1289) ·
BN-6 done (E, UNKNOWN) · BN-7 done (A OK, 2.5–8 s apart) · BN-8 done · BN-9 🟡 done (one bud — the wire shows a bud event at 16:48:14) · BN-10 done
(FAILED_PRECONDITION; "Refuted if the status is not OK" applies only to worn buds) · BN-11 done (OK with in-ear detection off) · BN-12 done (two events at subscribe,
none after; not refuted by silence) · BN-13 done (B1 L and R; stop reported) · BN-14 done (B2 L and R; each status ACKed) · BN-15 done (ACK, status 03; "refuted if
NAK" — not refuted) · BN-16 done (export 16:57:15) · BN-17 done (A × 4; Play got the Notify) · BN-18 done (B1 L/R and B2 L/R; Play cut the claim and ACKed the
stop) · BN-19 done (C × 2, Stop × 2) · BN-end done (three exports, three app logcats, L, Y, HCI logs).

## 14. Proposals and open questions (proposals — nothing promoted, `AGENTS.md` §6)
- **P-A (round 2, ANC):** switch the mode with `WriteSetting 4:{13:n}` on DLCI 0x02 — no Message Stream claim, no contention with Play services; treat its push
  as `set_by_app`; show `FAILED_PRECONDITION` as "no bud worn". A `PROTOCOL.md` §4.1 🟢 FACT promotion and an ADR are the maintainer's.
- **P-B (ring):** while the app holds the Message Stream, listen for `04 01 00 01 xx` and ACK it (the specification's rule); in the Play user the stop report goes to
  Play — show "ringing" only from the app's own claim.
- **P-C (Ring both):** a local Ring both exists — ADR-027 would need a superseding decision before the app offers it.
- **P-D/E:** `SubscribeToOobeActions` reports the on-head state at subscribe but ended with `UNAVAILABLE` — not usable as an in-ear source yet; settings 8/9/10
  unsupported by this firmware.
- **Scripts (rule 4a):** commit the ring-tone detector and the HDLC/claim helpers used here under `scripts/`.
- ❓ The audibility of the A writes; the 17:09:23 `0x13`; the release app's update at 2026-10-16 14:56:46; D's status 14.

## 15. External validation (fetched 2026-10-17; pages "Last updated 2025-08-12 UTC")
- developers.google.com/nearby/fast-pair/specifications/extensions/deviceaction: "0x03 (0b00000011): Ring both left and right"; "The second byte in additional
  data, if present, represents the timeout in seconds."; "Providers may want to notify a Seeker when it changes the ringing status, for example if a gesture
  causes the ringing to stop." / "Seeker's will listen for this message and provide an acknowledgement when it is received."
- …/extensions/acknowledgement: group `0xFF`, ACK `0x01`, NAK `0x02`, "within 1 second of receiving the message".
- pigweed.dev/api/cc/group__pw__status.html: `PW_STATUS_UNKNOWN = 2`, `PW_STATUS_FAILED_PRECONDITION = 9`, `PW_STATUS_UNAVAILABLE = 14`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-078-2026-10-17_16-23-51_17-17-41-Group_BN/CAP-078-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-078-2026-10-17_16-23-51_17-17-41-Group_BN/CAP-078-FINDINGS
