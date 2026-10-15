# Findings: `CAP-076` (Group BL — 1.3.0, build `ff27ee1`, the settings stream on the app's own channel (ADR-064/065) in both GrapheneOS users: the Owner user with Google Play services and the secondary user without, measured against `CAP-075`)

**Analysed by `ai-sessions/0092` (2026-10-15).** Companion: `CAP-076-EVENT-NOTES.md` (the timeline, the steps as run, the Test-IDs). Labels per
`PROJECT_RULES.md` §1: 🟢 FACT (a frame, a log line or a film second), 🟡 HYPOTHESIS, ⚪ ASSUMPTION, ❓ OPEN QUESTION. Frame and log names as in the event
notes: H1L / H1 / H2L / H2 the HCI logs, E1 / E2 the exports, A1 / A2 the app logcats (UTC), L the full logcat (local time; CAP-076-logcat-extract.txt carries
the quoted lines), s a film second. Every `tshark` filter was run on the Buds' ACL handle (`bthci_acl.chandle==0x000b`; the Connection Completes for the
Buds' address: H1 672 / 4680, H2L 311 / 605, H2 151 / 2678 — `bthci_evt.bd_addr == 04:00:6e:cf:6e:07` matches them, `bluetooth.addr` matches nothing, exit 0
both); every negative carries its command, its exit status and a positive control (`AGENTS.md` §13 step 8). The counts come from four scripts committed with this
analysis (`PROJECT_RULES.md` rule 4a): `scripts/pwhdlc_crc_table.py`, `scripts/message_stream_claims.py`, `scripts/stream_events.py`,
`scripts/film_mode_buttons.py` (their usage in each file's header).

**In short (🟢 unless marked).** 1.3.0 did what ADR-064/065 say, in both users. Every MAESTRO session that got through its reads sent **one**
`SubscribeToSettingsChanges` and **one** `ReadSetting 4:13`, byte-identical to the official app's, after `SubscribeRuntimeInfo` and before `GetHardwareInfo`
(12 of 12 sessions; a 13th was closed by the Buds after 6 reads, before them). **Every press-and-hold made on the noise-control tab showed on screen within
0.1 s of the Buds' push — 29 of 29 in the user with Google Play services, 12 of 12 without — while Play's Fast Pair held the shared channel (DLCI 0x04) and
received the `Notify` frames**; the app sent nothing to get them. The maintainer's statement "when I did a long press on a bud, the ANC mode in the app
changed too" holds for every hold the film shows. Every own tap (15 `Set`s) was answered and classed as the app's own change. Nothing was held or re-claimed:
the app's 33 claims of DLCI 0x04 are 14 snapshots of Connects/re-opens and 19 user actions (13 noise-control taps in 12 claims of part 1; Refresh, 4 taps, Ring
and Stop of part 2). New on the wire: **every change arrived twice, on channel 19 and 21**, when both buds were out of the case (§4.3). Three defects of 1.3.0
are found — none heavy: a re-open's "changed since the app last saw it" cause is overwritten by the equal `4:13` answer 0.2–0.7 s later (§6.2), a tile tap cut
right after its `Get`'s answer changed nothing and said nothing (§5.3), and a same-mode push is logged with the old cause (§4.4). The per-tap collision with
Play (1.3.0's known issue) is measured: 8 of 12 tap claims refused at attempt 1, 4 cut by Play's connect (§5.2). No crash, no wrong write, no Safe Mode; the
sessions lost were all ended by the Buds or by the ACL (§6).

## 0. Build, install, users, logs, privacy, health

- **Build.** "App: 1.3.0, build ff27ee1 (2026-10-14)", no "-dirty", on film in both users (film 1 s101–107; film 2 s81–86, s321). The installed APK, pulled
  by this session at 07:52 (`adb shell pm path --user 0 …` and `--user 10` → the same `base.apk`, exit 0 each; `adb pull`), has SHA-256
  `03272c6f954316d76c3ef6d1d020abd8bdb97755d7f8e165c84d3f16a9c6a96c` = `~/opencontrol-1.3.0-tested/opencontrol-pixelbudspro2-1.3.0.apk.sha256` (B1's);
  `lastUpdateTime=2026-10-15 06:36:57` unchanged since the run (CAP-076-adb-checks-0092.log). 🟢 The tested APK is the build of B1.
- **Install.** 1.2.2 `bfd7891` at the start of film 1 (s62–73) → the update on film (s92–93 "Updaten…"; L `PackageManager: … installation completed`
  06:36:58.015) → 1.3.0 (PID 11800, L `am_proc_start` 06:36:59.954). The update killed **both** users' processes (L `am_kill … killDueToPackageUpdate`:
  `[10,4104,…]` 06:36:57.930, `[0,10952,…]` 06:36:57.941). The shell log: `versionCode=10202` (06:23:50) → `10300` (06:38:17), the two `firstInstallTime`
  lines unchanged (an update, no reinstall). The 1.2.2 Settings card and its choice ("This phone has Google Play services", "Google Play services: found on
  this phone", s59) are gone in 1.3.0 (s100, film 2 s73): 🟢 ADR-065 item 5 on hardware (the stored `hold_reclaim_mode` is not shown anywhere).
- **Users (Checkpoint 0, the maintainer: "Yes, as read").** Part 1 = the Owner (user 0) with Google Play services: A1 has no `userType:` line; L
  `am_proc_start: [0,11800,10354,…]`; `pm list packages --user 0` lists `com.google.android.gms` (exit 0); `dumpsys package com.google.android.gms` (07:53):
  `versionName=26.32.34 (260400-968093310)`, `User 0: installed=true`, `BLUETOOTH_SCAN`/`_CONNECT`/`_ADVERTISE` `granted=true` (the *Nearby devices* group;
  ⚪ the same an hour earlier); 18 Play claims in H1 (`frame contains 03:08:00:02:01:25` → 18, exit 0). Part 2 = the secondary user (user 10) without Play:
  A2 line 3 `userType: full.secondary` (source.android.com, *Multiple users*, as quoted in `CAP-075-FINDINGS.md` §0); `pm list packages --user 10 | grep -i -E
  "gms|vending"` exit 0 **because it matched `app.grapheneos.gmscompat*`** — `com.google.android.gms` is not in the list, and `dumpsys` says `User 10: …
  installed=false`; the same Play-marker filter → 0 in H2L and H2 (exit 0; positive control H1 18).
- **Logs.** Sizes, modes and SHA-256 as in the prompt's §2 (re-derived: all 14 files equal; the prompt's "empty `test` file" did not exist). H1 has **10,617**
  packets (06:36:11.035–06:54:29.229), not "10,0xx". H1L holds no Buds frame (0 on handle `0x000b`, 0 RFCOMM; positive control H1 3,670 / 2,556) — it ends with
  the Bluetooth off of film 1 s8. The exports end without a newline: 1,062 and 462 lines (the toasts' counts; `wc -l` gives 1,061 / 461) and **do carry the
  Debug-mode hex lines** (E1 444 `DLCI 0x0N:` lines, E2 201) — the prompt's "no raw hex lines" is not so. **Film 2 has no audio stream** (`ffprobe
  -show_entries stream=codec_type` → `video` only, exit 0; film 1 → `video`, `audio`).
- **Health (🟢).** A1/A2: `FATAL EXCEPTION` 0, `ANR in` 0, `StrictMode` 0 (`grep -c`, exit 1 each; positive control `grep -c OpenControlBuds` → 278 / 94, exit
  0). L over part 1: `am_crash`/`am_anr` for the app 0 (positive control `am_proc_start` for the app 3, exit 0). A1 carries **seven** `W System: A resource failed
  to call close.` lines (the finalizer thread, 06:47:10 ×5 and 06:47:44 ×2, next to `BluetoothSocket: close() … Already closed`) — 🟡 CloseGuard reports for
  sockets of the refused attempt-1 connects that were never `close()`d before collection; A2 0. pw_hdlc: 658 frames, 0 CRC-32 failures (§2).
- **Privacy (Checkpoint 0).** Films committed as is (audio kept): film 2 shows the Wi-Fi network's name (s0–4, s15, s327–328), both films the serials
  (film 1 s62–73, s101–107; film 2 s81–86, s321–323). The exports' hex lines carry the serials (the `GetHardwareInfo` answers) — committed as is (as
  `CAP-073`…`CAP-075`); the extract has them redacted (9 lines). The extract (14,121 lines: part 1 06:35–06:53, PIDs 10952/11800/6149/11130, Play's
  Fast Pair lines, every line naming the app or `com.android.bluetooth`): 0 e-mail, 0 Wi-Fi name, 0 MAC other than `00:…`/`ff:…` (`grep -c`, exit 1 each;
  positive control: `grep -c 'xx:xx:xx:xx:6e:07'` > 0).

## 1. ACLs, the Message Stream claims of both clients, Play's timers

**Method** (as `CAP-075-FINDINGS.md` §1): a claim = a phone `SABM` on DLCI 0x04 (never 0x05 in this run) to its `DISC`/`DM` or the ACL's end; the owner is
the first phone message (`08 11 00 00` = the app; `03 08 00 02 01 25` = Play; `04 01 …` = the app's Ring/Stop). Command: `python3 scripts/message_stream_claims.py <log> 0x000b` ( `tshark -r <log> -Y
"bthci_acl.chandle==0x000b && (btrfcomm.dlci==4 || btrfcomm.dlci==5)" -T fields -e frame.number -e frame.time_epoch -e frame.p2p_dir -e btrfcomm.dlci -e
btrfcomm.frame_type -e data.data`, exit 0), messages split as `[Group][Code][Len:2][Value]` per direction.

| Log | ACLs (handle `0x000b`) | Claims | App | Play | `Notify` frames |
|---|---|---|---|---|---|
| H1 | 672 06:36:19.895 → 4558 06:42:23.192 (`0x13`); 4680 06:42:46.059 → open at the end | 40 | 22 (10 snapshots incl. S0 of 1.2.2, 12 taps' claims) | 18 | 110: 42 on the app's claims, 66 on Play's, 2 after a claim's `DISC` |
| H2L | 311 07:01:07.655 → 489 07:01:08.972 (`0x13`); 605 07:01:13.785 → 1294 07:02:39.649 (`0x16`) | 1 | 1 (snapshot) | 0 | 1 |
| H2 | 151 07:02:45.890 → 2666 07:08:33.441 (`0x13`); 2678 07:10:20.388 → open | 10 | 10 (3 snapshots, Refresh, 4 taps, Ring, Stop) | 0 | 16 |

**Play's timers (🟢, Play 26.32.34, 2026-10-15, part 1)** — from L's `cleanup_rfc_slot … scn: 2, app_uid: 10205` (Play's loss) to Play's next connect (its
`SABM` on the wire or its own `already at opened state`): **recovery 5.13–5.97 s** (17 values: 5.16, 5.13, 5.60, 5.26, 5.77, 5.51, 5.59, 5.53, 5.67, 5.48,
5.75, 5.97, 5.63, 5.28, 5.49, 5.52, and 5.60 after the Buds' own `DISC` 06:39:53), **retry 2.37–2.50 s** after its connect was refused (06:45:58.170 →
06:46:00.559; 06:46:09.879 → 06:46:12.375; 06:46:26.112 → 06:46:28.479), and **4.49 s once** after two refusals within 2.4 s (06:46:28.479 → 06:46:32.965,
"Retry times remaining 4" at 06:46:28.483; all 20 other lines say 5). `CAP-075`: 5.33–5.82 / 2.06–2.82 / 4.56 once — 🟢 the same mechanism; the recovery
range is a little wider here (5.13 and 5.97 are new extremes).

## 2. The Connect sequence — byte for byte, once per MAESTRO session (`PAIR-003`, `BATT-004`, `FW-003`)

Command: `python3 scripts/pwhdlc_crc_table.py <log> 0x000b` (the RFCOMM payloads of DLCI 2/3 per direction reassembled, split on `7e`, unescaped, `zlib.crc32` over address + control + data against
the little-endian trailer, the RpcPacket parsed with `scripts/pwrpc_decode.py`'s own functions) — **H1 466 frames, H2L 39, H2 153, 0 CRC failures**; positive
control: `CAP-051` 1177's bytes pass, the same bytes with one bit flipped fail; the counts equal `python3 scripts/pwrpc_decode.py --handle 0x000b <log> | wc -l`
(466 / 39 / 153).

| Session | Opened by | Channel | Requests | `SubscribeToSettingsChanges` → `ReadSetting 4:13` → answer |
|---|---|---|---|---|
| S0 (1.2.2) 06:36:20 | 1.2.2's Connect | 21 | 15 (13 reads, SRI, GHI) | — (1.2.2) |
| S1 06:37:00 | LINK_BACK | 21 | 17 | 1745 → 1746 → 1750 `4:{13:0}` |
| S2 06:39:44 | Connect | 21 | 17 | 2838 → 2839 → 2843 `4:{13:0}` |
| S3 06:39:56 | AFTER_LOSS | 19 | 17 | 3120 → 3120 → 3124 `4:{13:1}` |
| S4 06:41:33 | AFTER_LOSS | 21 | 17 | 4129 → 4130 → 4135 `4:{13:2}` |
| S5 06:42:46 | Connect | 21 | 17 | 4858 → 4859 → 4863 `4:{13:0}` |
| S6 06:42:56 | AFTER_LOSS | 19 | **6** (4:16 … 4:12; the Buds' `DISC` 5616 at 06:42:57.106) | — |
| S7 06:43:05 | Connect | 21 | 17 | 5995 → 5996 → 6003 `4:{13:2}` |
| S8 06:47:32 | AFTER_LOSS | 19 | 17 | 8959 → 8960 → 8968 `4:{13:1}` |
| S9 06:47:46 | AFTER_LOSS | 21 | 17 | 9213 → 9214 → 9220 `4:{13:1}` |
| S10 07:01:14 | LINK_BACK | 19 | 17 | H2L 848 → 849 → 853 `4:{13:0}` |
| S11 07:02:46 | LINK_BACK (after Bluetooth on) | 19 | 17 | H2 540 → 541 → 546 `4:{13:0}` |
| S12 07:03:02 | AFTER_LOSS | 21 | 17 | 790 → 790 → 794 `4:{13:1}` |
| S13 07:04:15 | Connect | 21 | 17 | 1131 → 1132 → 1137 `4:{13:1}` |

- 🟢 **The two new requests, once per MAESTRO session:** 12 subscriptions and 12 `ReadSetting 4:13` in the 12 full 1.3.0 sessions, none in S0 (1.2.2) and
  S6 (closed before them), no second one in any session; each right after `SubscribeRuntimeInfo` and before `GetHardwareInfo`. **Byte-identical** to the
  references: channel 21 `7e004b0310151dea71de7d5e25f5ad2128019754b07e` (= `CAP-051` 1177) and `7e004b0310151dea71de7d5e2551aed0ae2a02200d9e82abac7e`
  (= `CAP-051` 1178); channel 19 `7e003b0310131dea71de7d5e25f5ad212807ee8eb27e` (= `CAP-056` 1451) and `7e003b0310131dea71de7d5e2551aed0ae2a02200d24c239477e`
  (= `CAP-056` 1450). Command: `awk -F'\t' '$3==0 && $8 ~ /SubscribeToSettingsChanges/'` and `'$9=="4:13"'` over its tables.
- 🟢 **The other fifteen** (4:16, 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29, `SubscribeRuntimeInfo`, `GetHardwareInfo`): one byte form per field and channel
  across all 14 sessions (e.g. 4:16 ch 21 `7e004b0310151dea71de7d5e2551aed0ae2a02201047eeadcf7e`, ch 19 `…2a022010fdae3f247e`; 4:2 ch 21 `…0220020f9f143c7e`)
  — equal to `CAP-075-FINDINGS.md` §2's 1.2.1 bytes. **A full 1.3.0 session sends seventeen MAESTRO requests**; the skeleton's "fifteen" is 1.2.x's count
  (`CAP-075` §2) and miscounts the list it gives.
- 🟢 The answers: the reads of `4:13` gave 0 (no mode settable) at S1, S2, S5, S10, S11 — each with both buds in the case (runtime info: both charging, e.g.
  H1 4863's packet; film 1 s441, film 2 s19 "Both buds in the case") — 1 at S3, S8, S9, S12, S13 and 2 at S4, S7. Raw: H1 1750 (2nd HDLC frame)
  `7e00a5032a0422026800080110151dea71de7d5e2551aed0ae651b08147e` (= `CAP-045` 2033), H1 3124 `7e80a3032a0422026801080110131dea71de7d5e2551aed0ae2e62233c7e`
  (= `CAP-021` 1089). Every `GetHardwareInfo` answered (the same three strings), every status OK.
- The snapshot claim (ADR-065 Q7) after every session; in part 1 three of the nine 1.3.0 snapshots were refused at attempt 1 (S1, S2, S7: Play on the
  channel) and connected at attempt 2 ≈ 0.6–0.9 s later.

## 3. Every write — byte for byte

- DLCI 0x02 (🟢): part 1 `WriteSetting 4:{16:{-1, 0, 4, 2, 6}}` 9854 (06:50:04.010) → empty RESPONSE OK 9860, `4:{2:0}` 9925 → 9934, `4:{2:1}` 10037 →
  10042; part 2 `4:{16:{-1, 0, 4, 2, 1.69}}` 2018 → 2021, `4:{2:0}` 2081 → 2088 (raw 2081 `7e004b0310151dea71de7d5e251d9a8c9e2a0422021000a0a6cb947e`, the
  same bytes as H1 9925). Each mirrored on the settings stream (H1 9856, 9932, 10040; H2 2020, 2087) and applied once (`keepsOwnMark`: E1 06:50:22.351 "Setting 2
  written", the (i) "changed 06:50:22"). **Nothing else written:** **0 `WriteSetting 4:{13:n}`** — `tshark -r <log> -Y "bthci_acl.chandle==0x000b &&
  frame.p2p_dir==0 && frame contains 1d:9a:8c:9e:2a && frame contains 22:02:68"` → 0 in H1 and H2 (exit 0); positive controls: the method part
  `frame contains 1d:9a:8c:9e:2a` on sent frames → 3 / 2 (the writes above), `frame contains 22:02:68` on received frames → 117 / 37.
- DLCI 0x04 (🟢): 15 `Set` frames (H1 11, H2 4: `frame.p2p_dir==0 && frame contains 08:12:00:14`, exit 0), Ring Left `04 01 00 01 02` (H2 claim #9) and Stop
  `04 01 00 01 00` (#10) — each answered (§5). Nothing on DLCI 0x08/0x0a from the app: those channels were the Google app's (uid 10302, L `cleanup_rfc_slot …
  scn: 4/5, app_uid: 10302`), as in `CAP-075`.
- **The Buds' state after the run (🟢):** EQ `[−1, 0, 4, 2, 1.69]` (Low bass … Upper treble), **in-ear detection off** (H2 2081 at 07:06:56, not switched on
  again), the other settings as read (`TODO.md` §1, restore if wanted).

## 4. The holds — every stream packet against the screen (`TOUCH-007`; ADR-064 item 3, ADR-065 items 3–4)

### 4.1 Method

The field-13 `SERVER_STREAM` packets from `scripts/pwhdlc_crc_table.py` (H1 117, H2L 0, H2 37); `scripts/stream_events.py` merges the two packets of one change (the same value within 0.4 s) and
marks the app's own `Set`s (§5). The screen: `scripts/film_mode_buttons.py` reads the four mode buttons of the noise-control card at 10 fps (the mean grey value of a patch inside each
button at full resolution; filled < 170) over film 1 s460–530, s560–610, s792–852 and film 2 s132–252, and prints each change of the selected button with its
phone time. "Shown within 0.1 s" = the first 10-fps frame with the packet's mode is ≤ 0.1 s after the packet (± 0.1 s for the clock offset).

### 4.2 Per part (🟢)

| Part | Where | Changes the app did not request | Their stream packets | On screen |
|---|---|---|---|---|
| 1 (Play) | the Connection tab, 06:40:02–06:41:51 | 14 (06:40:02 Adaptive on ch 19 only; 13 from 06:40:47, pairs) | 27 | **not visible** — the Connection tab shows no mode; E1 logs each ("changed_by_buds"), the battery card's "checked HH:MM:SS" follows each push (ADR-065 Q5: the stream feeds the availability) |
| 1 (Play) | the ANC tab, 06:43:14–06:44:11 | 17 holds | 34 | **17 of 17 within 0.1 s** (06:43:14.8 … 06:44:11.2, EVENT-NOTES) |
| 1 (Play) | the ANC tab, 06:48:44–06:49:33 | 12 holds | 24 | **12 of 12 within 0.1 s** (one under a volume panel) |
| 1 (Play) | the Connection tab, 06:42:11 | Off, then 0 (both buds into the case) | 2 | "Not worn (checked 06:42:11)"; 0 never shown as a mode (the ANC tab at 06:42:48: "The Buds don't allow changing noise control right now …", Off kept) |
| 2 (no Play) | the ANC tab, 07:05:24–07:06:38 | 12 holds | 24 | **12 of 12 within 0.1 s** (two under a volume panel) |

- **The holds on the ANC tab: 29 of 29 in the user with Play, 12 of 12 without — 🟢 FACT for this run.** In part 1 the shared channel was Play's the whole time
  (claims #16 06:43:11–06:44:35 and #40 from 06:47:50, carrying the `Notify` of every hold: 6107 … 6548, 9488 … 9716); the app had no claim open during any of
  the 29 and sent nothing (no app `SABM` between 06:43:08.76 and 06:44:35.96, and between 06:47:49.51 and the end of the log). In `CAP-075` set 2 (1.2.1, the
  same user) 8 of 31 were shown within a second (§9).
- **Which of these were press-and-holds** (🟡 for each, as in `CAP-073`…`CAP-075`): no camera; the 41 changes on the ANC tab came with no AVRCP from the Buds,
  no audio and no app `Set`, in the Buds' list order (Noise cancellation → Off → Adaptive → Transparency, all four ticked: `4:{12:{1:1 2:1 3:1 4:1}}` at every
  Connect), 2–7 s apart — the maintainer's holds (his statement) is the reading; a wear change is excluded for these by the absence of the Buds' PLAY/PAUSE that
  in-ear detection sent for every bud in/out in part 1 (06:40:46–06:41:41, §8).
- **The Connection-tab stretch 06:40:47–06:41:51 is mixed** (🟢 for the coincidences): 3 of its 13 changes fall 2–7 ms from the Buds' AVRCP PAUSE/PLAY
  (H1 3600 ↔ 3603, 3624 ↔ 3626, 3972 ↔ 3974) — wear-driven switches (🟡 `PROTOCOL.md` §6, "the stored mode per wear state"); the other 10 have no AVRCP within
  0.7 s; ten short voiced audio segments follow ten changes by 0.6–1.4 s (§0 of the event notes). The maintainer's statement for this stretch is not
  separable into holds and wear changes from the evidence; it is not counted for the verdict's "shown" (the tab cannot show it).
- **The maintainer's "the ANC modes during playback were good":** the only playback of the run is part 1 06:40:51–06:41:44 (AVRCP "Playing" 3553, AVDTP
  START 3569 … SUSPEND 4316, the podcast of the Quick Settings card) — the Connection-tab stretch; the mode changes there reached the app (E1) but not the
  screen. The 41 holds on the ANC tab were made **without** playback (AVRCP "Paused"/"Stopped", no AVDTP START from 06:41:44.8 to 06:52:15 in H1; in H2 the
  START/SUSPEND pairs of 3–5 s around taps are the phone's system sounds with AVRCP "Stopped" until 07:08:21).

### 4.3 Two stream packets per change — channel 19 and 21 (🟢 for the observation, 🟡 for the cause)

- **Every change of the mode with both buds out of the case arrived twice**, once with `channel_id` 21 (address `00 a5`) and once with 19 (`80 a3`), 1–200 ms
  apart, the same value: **68 of 68** such changes (part 1: 13 on the Connection tab + 17 + 12 holds + 10 own `Set`s = 52; part 2: 12 holds + 4 own = 16), e.g. H1 6108 `7e00a5032a0422026801080710151dea71de7d5e25f5ad2128b2d925c47e` (06:43:14.732, ch 21) and 6117
  `7e80a3032a0422026801080710131dea71de7d5e25f5ad2128117bf5557e` (06:43:14.840, ch 19 = `CAP-021` 1842); H2 1726 / 1729 (ch 21 = `CAP-051` 1755, ch 19 =
  `CAP-056` 2259). Command: `awk '$3=="SERVER_STREAM" && $5 ~ /13:/'` over the table of `scripts/pwhdlc_crc_table.py`, merged by `scripts/stream_events.py`.
- **A single packet came on the out bud's channel when one bud was in the case**: H1 3233 (06:40:02.267, ch 19 — runtime info 06:39:57.99: Left out, Right
  charging), H1 4492 / 4502 (06:42:11, ch 21 — the buds going into the case), H2 717 and 839 (07:03:02.647 / 07:03:07.137, ch 21 — runtime info 07:03:04:
  Left charging, Right out); and **two single packets of the same mode with both out** (H1 8739 06:47:07.108, 8774 06:47:18.657, ch 21).
- 🟡 **HYPOTHESIS H1:** each bud out of the case pushes its own packet, with its core's channel (`fux.java`: 19 = LEFT_BT_CORE, 21 = RIGHT_BT_CORE,
  `PROTOCOL.md` §2.2a) — fits all 68 changes and the three one-bud cases; the two same-mode pushes of 06:47 are not explained. **Against it:** the official
  app's captures show one packet per change (`python3 scripts/pwrpc_decode.py captures/CAP-051*/CAP-051-btsnoop_hci.log | grep 'SERVER_STREAM.*SubscribeToSettingsChanges'`
  → ch 21 only, 4 packets; `CAP-056` → 4 on ch 19, 1 on ch 21, none paired; exit 0) although both buds were worn there (`PROTOCOL.md` §6, 2026-10-14: `CAP-056`
  2322 "both worn"). 🟡 **H2:** a subscription of an earlier session of the same ACL survives the Buds' `DISC` and is served on the other core's channel —
  refuted for 06:48:44 (four earlier subscriptions in that ACL, two packets) and 07:03:07 (two subscriptions, one packet). ❓ Settling experiment: one Connect on a
  fresh ACL (lid closed and opened), both buds worn, five holds — packets per hold with one subscription; then the same with the official app on the same ACL.
- **Effect on the app (🟢):** none visible — the second packet is "the same mode again" (`causeOfStream` → `null`); E1/E2 log it as a second "from the settings
  stream (<cause>)" line (the prompt's "86 + 22" lines are 43 + 11 changes; E2's 24 + 8 are 12 + 4). The export's paired `set_by_app` lines (e.g. 07:04:49.836 /
  .861) are these two packets, not two answers to one `Set`.

### 4.4 The cause shown and logged (🟢)

- The ANC (i) after a hold: "Changed by the Buds at HH:MM:SS: the Buds reported a new mode — a press-and-hold on a bud, or a change they made themselves." —
  film 1 s536 (06:44:11), film 2 s216, s224, s233, s242, s249 (07:06:03 … 07:06:38): 🟢 ADR-065 item 4 / Q10 on hardware.
- **A same-mode push logs the kept cause** (`BudsRepositoryImpl.kt:419`, `(cause ?: _ancModeCause.value)`): H1 8739 / 8774 (Off again, 39 s and 50 s after the
  tile's Off) → E1 06:47:07.121 / 06:47:18.664 "from the settings stream (**set_by_app**)"; H2 839 → E2 07:03:07.144 "(read)". The shown cause is right (the
  (i) at s705/s716 has no cause line); only the log line reads as if the packet were the app's change (§13 P4).

## 5. The own taps (`ANC-001`…`ANC-004`, ADR-032, ADR-065 item 4)

### 5.1 Set → ACK → stream (🟢)

| Part | `Set` (frame, mode) | ACK | the Buds' `Notify` | the stream (first, second) | E1/E2 cause |
|---|---|---|---|---|---|
| 1 | 6724 `40` 06:44:37.456 | 6726 +91 ms | 6727 +92 | 6731 +177, 6732 +246 | set_by_app ×2 |
| 1 | 6956 `80` 06:44:50.476 | +104 | +169 | +237, +238 | set_by_app ×2 |
| 1 | 7137 `08` 06:45:00.538 | +131 | +133 | +154, +155 | set_by_app ×2 |
| 1 | 7151 `08` (the second tap, same mode) | +75 | +74 | none (no change) | — |
| 1 | 7498 `80` (tile) | +83 | +84 | +160, +201 | set_by_app ×2 |
| 1 | 7665 `40` (tile) | +100 | +144 | +246, +248 | set_by_app ×2 |
| 1 | 7930 `20` (tile) | +95 | +96 | +198, +199 | set_by_app ×2 |
| 1 | 8208 `08` (tile) | +129 | +130 | +205, +242 | set_by_app ×2 |
| 1 | 8377 `80` (tile) | +127 | +128 | +233, +300 | set_by_app ×2 |
| 1 | 8445 `40` (tile) | +100 | +101 | +176, +218 | set_by_app ×2 |
| 1 | 8527 `20` (tile) | +129 | +131 | +195, +260 | set_by_app ×2 |
| 2 | 1372 `08` 07:04:49.772 | +25 | +26 | +60, +85 | set_by_app ×2 |
| 2 | 1470 `40` | +77 | +78 | +81, +121 | set_by_app ×2 |
| 2 | 1558 `80` | +50 | +52 | +55, +93 | set_by_app ×2 |
| 2 | 1650 `20` | +82 | +85 | +94, +125 | set_by_app ×2 |

Raw (H1 6724 → 6726 → 6727): `0812001401e8e840` + 16 × `00` → `ff010006081201e8e840` → `0813000401e8e840`. 🟢 **15 `Set`s, 15 ACKed, 0 NAK**; the stream
packet 55–246 ms after the `Set` (`CAP-051`: 269 ms) — inside `ANC_SET_WINDOW_MS` (2 000 ms) with margin; **every own change classed as the app's**
(E1/E2 "set_by_app"; the (i) without a cause line on film: film 1 s553, s566, s576; film 2 s140, s148, s156, s166). On screen at the ACK (film 1
06:44:50.6, 06:45:00.7; film 2 07:04:49.8, 57.6, 07:05:04.9, 13.8 — each the ACK's time + ≤ 0.1 s).

### 5.2 The collisions with Play's claim — 1.3.0's known issue, measured (🟢)

13 taps with Play present (4 on the ANC tab, 9 on the tile) in 12 claims:

| Outcome | Claims (taps) | Evidence |
|---|---|---|
| attempt 1 refused by the stack ("already at opened state": Play on the channel), attempt 2 connected 0.87–1.11 s later, the `Set` answered | 8 (9) — #17, #19, #21 (2 taps), #23, #25, #28, #31, #33 | E1 "connect failed after 326 … 1071 ms (attempt 1/3)" ×8; L `already at opened state` 06:44:35.961, 06:44:49.138, 06:44:59.106, 06:45:45.626, 06:45:52.286, 06:46:03.767, 06:46:14.285, 06:46:20.634 |
| attempt 1 connected, the `Set` answered, the claim cut in its linger by Play's connect | 2 — #34, #35 | E1 06:46:26.250, 06:46:28.549 "… closed"; L `already at opened state` 06:46:26.112, 06:46:28.479 (Play's) |
| attempt 1 connected, cut before the `Get`'s answer: "answer cut off", a toast | 1 — #26 | E1 06:45:58.383; film 1 s632–634 |
| attempt 1 connected, cut **right after** the `Get`'s answer: no `Set`, no line, no toast | 1 — #29 | §5.3 |

The stack path of every cut (4 of 4) is `CAP-075-FINDINGS.md` §5's: Play's `connect()` refused (`bt_port_api … RFCOMM_CreateConnectionWithSecurity:
already at opened state 2, … scn=2, is_server=false, … dlci=4`), `cleanup_rfc_slot … app_uid: 10205` (Play) and 70–135 ms later `… app_uid: 10354` (the
app), `NearbyDiscovery: RfcommEventStreamMedium … Failed to read from socket. Retry times remaining 5`, the app's `read return: -1` (L 06:45:58.170 …
06:45:58.254; the extract). **Nothing re-claimed by itself** (0 "re-claim"/"hold" lines in E1 and E2 — `grep -ciE 're-claim|hold'` → 0, exit 1; positive
control `CAP-075`'s export 2 → 31, exit 0). The tile's next tap after a failure was the user's (06:46:03.4, 06:46:13.9).

### 5.3 The silent tile tap (🟢 for the wire and the logs, 🟡 for the code path)

06:46:08.959 "ANC tile tapped (shown: OFF)" → claim #29 connected at attempt 1 (06:46:09.527) → `Get` 8016 (06:46:09.741) → the Buds' `Notify` 8029 `e8 e8 20`
06:46:09.878 → Play's connect refused at 06:46:09.879 → the stack's `DISC` of DLCI 0x04 (8030, phone → Buds) at **06:46:09.880** → the app's "closed" line
06:46:09.952. On the wire no `Set`; in E1 no failure line ("answer cut off", "Safe Mode", "rejected") and on film no toast (s640–646 pixel-identical,
although the tile's code shows a toast for any failure, `AncTileService.kt` lines 137–140) — the tile stayed "Off" and the user tapped again 4.9 s later.
🟡 **HYPOTHESIS (code reading, `BudsRepositoryImpl.kt`):** the `Notify` that answers the `Get` is also emitted to `_ancOutcomes` (line 708, after line 702's
`_ancNotifies`); if the `Set`'s wait (`sendAndAwaitOnClaim(_ancOutcomes, …)`, line 973) subscribes before line 708 runs, it takes that `Notify` as its
`AncOutcome.Notified` and returns success (line 993) without an ACK; the `Set` itself was written after the stack had dropped the port. Settling test (a
FEATURE session): a `FakeBudsTransport` test that emits the `Get`'s `Notify` with a delay between the two emissions and asserts that the tap then fails; and a
`Set` wait that ignores a `Notify` received before the `Set` was sent.

## 6. The Buds' closes and the re-opens (ADR-044; M1 on hardware)

### 6.1 Every session end (🟢)

| Time | Session | Ended by | Re-open | Evidence |
|---|---|---|---|---|
| 06:36:57.99 | S0 (1.2.2) | the update (the phone's `DISC`) | the 1.3.0 start: LINK_BACK S1 06:37:00.656 | H1 1586 |
| 06:39:41.77 | S1 | the user's Disconnect | Connect S2 06:39:44.75 | H1 2691; E1 |
| 06:39:53.97 | S2 (ch 21) | **the Buds' `DISC`** (the Left out of the case) | AFTER_LOSS 1.504 s after the loss line → S3 ch 19 | H1 2985 |
| 06:41:31.28 | S3 (ch 19) | **the Buds' `DISC`** (a bud into an ear: AVRCP PLAY +0.31 s) | AFTER_LOSS 1.508 s → S4 ch 21 | H1 3994 |
| 06:42:23.19 | S4 | the ACL's end (`0x13`; both buds in the case) | the user's Connect 06:42:45.70 → S5 ch 21 | H1 4558 |
| 06:42:53.38 | S5 (ch 21) | **the Buds' `DISC`** | AFTER_LOSS 1.503 s → S6 ch 19 | H1 5357 |
| 06:42:57.11 | S6 (ch 19) | **the Buds' `DISC`** + `DM` (928 ms after the re-open) | **skipped** ("the session was lost 928 ms after an automatic re-open", `CHAIN_GUARD_MS`); the user's Connect 7.8 s later → S7 ch 21 | H1 5616/5621; E1 06:42:57.113 |
| 06:47:30.43 | S7 (ch 21) | **the Buds' `DISC`** (DLCI 0x02 and Play's 0x04) | AFTER_LOSS 1.504 s → S8 ch 19 | H1 8827/8828 |
| 06:47:44.72 | S8 (ch 19) | **the Buds' `DISC`** (0x02 and Play's 0x04) | AFTER_LOSS 1.504 s → S9 ch 21 | H1 9077/9080 |
| 07:02:39.43 | S10 | Bluetooth off | LINK_BACK S11 0.001 s after Android's link (07:02:46.159) | H2L 1276; §7 |
| 07:03:00.36 | S11 (ch 19) | **the Buds' `DISC`** (the Right out of the case) | AFTER_LOSS 1.503 s → S12 ch 21 | H2 641 |
| 07:04:12.27 | S12 | the user's Disconnect | Connect S13 07:04:15.35 | H2 1017 |
| 07:08:33.44 | S13 | the ACL's end (`0x13`), after the film | — (the app in the background) | H2 2666 |

- 🟢 **M1 on hardware:** every re-open that got through its reads subscribed again (S3, S4, S8, S9, S12 AFTER_LOSS; S1, S10, S11 LINK_BACK; §2). The seven
  `AFTER_LOSS` delays are 1.503–1.508 s (`LOSS_REOPEN_DELAY_MS` = 1 500 ms); the one skip is `CHAIN_GUARD_MS` working as ADR-044 says ("a loss within 10 s of
  an automatic re-open schedules none"); afterwards nothing re-opened the session until the user's Connect (Android's link stayed connected, so no LINK_BACK
  event came — as designed). The re-homes follow `PROTOCOL.md` §2.2a's L-1 (🟢 there): the Left alone out → 19 (S3), the Right alone out → 21 (S12), changes
  with both out → the other channel (S4, S8, S9).
- 🟢 The Buds' closes that `CAP-075` saw only while the app was hidden happened here with the app on screen. None came within 5 ms of a change (the
  12-of-77 `DISC` case of `PROTOCOL.md` §4.1; the nearest: 3972 at 06:41:29.127, 2.15 s before the `DISC` 3994), so no push was lost to a `DISC` here; the
  two reads that differed (S4, S7) followed a `DISC` without a push (S4) and the ACL's end with both buds in the case (S7) — §6.2.

### 6.2 The first read after each re-open — and a cause that is overwritten (🟢 wire, 🟢 code)

- The value read after every Connect/re-open (§2's table) equals the last pushed mode, except where nothing was pushed: S4 read 2 (Noise cancellation) after the
  last push 3 (3972, Transparency) and the Buds' `DISC`; S7 read 2 after the last push 0/1 (4492/4502) and the ACL's end. **In both, the mode had changed while
  the app had no session — the case ADR-065 item 4 / Q4 calls "Changed since the app last saw it".**
- **What the app did:** the snapshot claim's `Get` answered first (S4: `Notify` 4094 `e8 e8 08`, 06:41:33.418; S7: 5991, 06:43:07.250) → `causeOfNotify` →
  `causeOfReadAnswer` → `CHANGED_SINCE_LAST_READ`; then the `ReadSetting 4:13` answer with the **same** mode (4135 at 06:41:34.165, +747 ms; 6003 at
  06:43:07.440, +190 ms) → `causeOfReadAnswer` → `READ` — it **replaced** the cause (`BudsRepositoryImpl.kt:385–386`: `if (… mode != _ancMode.value)
  CHANGED_SINCE_LAST_READ else READ`). E1 logs only the second: 06:41:34.170 and 06:43:07.444 "Noise-control mode from the mode read (**read**)". Neither (i)
  was opened before the next push changed the cause, so the film neither shows nor refutes the line; the two exports have **0** "changed_since" lines
  (`grep -c changed_since`, exit 1; positive control `grep -c changed_by_buds` → 86 / 24, exit 0).
- 🟢 FACT (wire order + code): **the snapshot claim's `Notify` came before the `4:13` answer in 12 of 12 sessions** (by 107–1,459 ms: e.g. 1736 → 1750
  +107 ms, 3090 → 3124 +1,075 ms, 8929 → 8968 +1,459 ms; H2L 810 → 853, H2 485 → 546, 760 → 794, 1099 → 1137), **so a mode that changed while the app was not
  connected is shown as "read", not as "changed since"** — the line stays only when the snapshot claim fails (Play refusing both attempts, or a cut).
  ADR-065's (i) line 2 is therefore not reachable in the normal order. Severity: minor (an (i) line). Fix (§13 P1): a read answer equal to the shown mode keeps
  the cause (`null`, as `causeOfStream`'s same-mode rule).

### 6.3 "No mode settable" (M2 on hardware, 🟢)

Six `4:{13:0}` (five read answers, one push: H1 4502) — each with both buds in the case; the app logged "… : no mode settable" and never showed 0 as a mode
(the ANC tab at 06:42:48 kept Off with "The Buds don't allow changing noise control right now (usually because no bud is in an ear) …"; film 1 s442); the next
1–4 (the snapshot's `Notify` or the read) allowed changes again (film 1 s462: the ANC tab without the line).

## 7. Bluetooth off/on in part 2 (BL-20, 🟢)

07:02:39.124 "Bluetooth adapter: ON -> TURNING_OFF" (film 2 s8) → H2L 1294 Disconnection Complete `0x16` 07:02:39.649 → E2 07:02:39.442 "Session loss cause:
Bluetooth was switched off on this phone" (final, not provisional — `ai-sessions/0064` F-3) → 07:02:44.681 ON → H2 151 ACL 07:02:45.890 → Android's link
CONNECTED 07:02:46.158 → LINK_BACK 07:02:46.159 → S11 with its subscription (540) and read (541 → 546 `4:{13:0}`). Part 1 had no Bluetooth off/on with 1.3.0
(BL-10 not run; the one on film 1 was before the 1.2.2 session).

## 8. Playback

Part 1: AVRCP "Playing" 06:40:51.002 (H1 3553) after the Buds' PLAY 06:40:46.806; the Buds' PAUSE/PLAY at 06:40:53.995, 55.970, 06:41:10.617, 14.434, 19.327,
25.043, 29.130, 31.589, 41.315 (in-ear detection, on until 06:50:22); AVDTP START/SUSPEND 3569/3769, 3804/3881, 3926/4316 (06:40:51.4 … 06:41:44.8); after the
film AVRCP "Playing" 06:52:14.9. Part 2: AVRCP "Stopped" until 07:08:21.6 ("Playing", after the film); 37 AVDTP START/SUSPEND pairs of 3–8 s from 07:02:46
(the phone's system sounds; no media session). Command: `tshark -r <log> -Y "bthci_acl.chandle==0x000b && btavrcp" -T fields -e frame.number -e frame.time -e
_ws.col.Info` (exit 0; positive control `CAP-075-btsnoop_hci2.log` → 22 AVRCP frames incl. "PlayStatus: Paused"); AVDTP: `… && (btavdtp.signal_id==0x07 ||
btavdtp.signal_id==0x09)` (exit 0). The volume panels on film (film 1 s370, s447–449, s701, s828; film 2 s172–173, s221–222) have no AVRCP volume frame
(🟡 phone keys, as `CAP-075` §6).

## 9. `CAP-075` (1.2.1, the same two users) vs `CAP-076` (1.3.0)

| Aspect | `CAP-075` set 1 (no Play) | `CAP-075` set 2 (Play) | `CAP-076` part 2 (no Play) | `CAP-076` part 1 (Play) |
|---|---|---|---|---|
| Holds on the noise-control tab | 41 | 31 (9 "hint", 19 "take it back", 3 stopped) | 12 | 29 |
| On screen within a second | **41 of 41** (the held claim) | **8 of 31** (2 late, 3 overdrawn, 2 after a user action, 16 never) | **12 of 12** (≤ 0.1 s) | **29 of 29** (≤ 0.1 s) |
| What carried them to the app | the app's held `Notify` claim | the app's claim when it had one; Play's otherwise | the settings stream (DLCI 0x02) | the settings stream — Play held DLCI 0x04 and got every `Notify` |
| The app's claims of DLCI 0x04 | 10 (holds of 14–36 s) | 19 (holds, re-claims, snapshots) | 11 (snapshots, Refresh, taps, Ring, Stop; ≤ 1.7 s each) | 22 (10 snapshots, 12 tap claims) — **0 holds, 0 re-claims** |
| Play's claims | 0 | 19 + 5 | 0 | 18 |
| Play's timers | — | recovery 5.33–5.82 s, retry 2.06–2.82 s | — | 5.13–5.97 s, 2.37–2.50 s (§1) |
| Taps | 0 `Set` | 0 `Set` | 4 `Set`, 4 ACKed at attempt 1 | 11 `Set`, 11 ACKed; 8 claims refused at attempt 1, 4 claims cut (§5.2) |
| "First hold only" pattern (`CAP-074`) | — | gone with the re-claim, ≈ 43 % coverage | gone | **gone** — the channel no longer matters for the holds |
| Session losses | 1 (the Buds, app hidden) | 1 (the Buds, app hidden) | 1 the Buds + 1 Bluetooth off + 1 ACL (after the film) | 6 the Buds' `DISC` + 1 ACL, all re-opened by ADR-044 or the user |

**The answer per user (🟢 for this run):** without Play nothing changed for the holds (all shown, as `CAP-073`/`CAP-075` set 1) and the taps work at attempt
1; with Play the holds are now shown as well as without Play — the 1.2.x limit of `CAP-074`/`CAP-075` is gone — while each tap still meets Play's claim on
the shared channel (8 of 12 claims at attempt 2, 2 of 13 taps lost: one with a message, one silently).

## 10. The counts for the verdict (the skeleton's analysis checklist)

1. **The app's `SABM`s on DLCI 0x04 vs the user's actions (🟢):** part 1 22 = 10 snapshots (S0 … S9; S0 is 1.2.2's) + 12 claims for 13 taps (4 on the ANC tab,
   9 on the tile — E1's 9 "ANC tile tapped" lines; film 1 s553–576 and s618–662); part 2 11 = 4 snapshots (S10 … S13) + Refresh (07:04:32) + 4 taps + Ring + Stop.
   **No claim the user did not cause.**
2. **Holds shown vs made (🟢):** ANC tab part 1 29 / 29, part 2 12 / 12; the Connection-tab stretch (part 1, 14 changes) not visible by design.
3. **Stream packets vs `Notify`s (🟢):** part 1: 109 field-13 `SERVER_STREAM` packets = 52 changes × 2 (both buds out) + 5 single packets (06:40:02,
   06:42:11 ×2, 06:47:07, 06:47:18) — with the 8 `4:13` answers the 117 of §3's filter; the 110 `Notify` frames: 42 on the app's claims (answers to its `Get`s and
   `Set`s), 66 on Play's (every hold reached Play), 2 outside a claim's `SABM`–`DISC` window (`scripts/message_stream_claims.py`). Part 2: 34 packets = 16 × 2 + 2 single (07:03:02,
   07:03:07) — with H2's 3 `4:13` answers the 37 of §3; 16 `Notify`, all on the app's claims. **One push (or one pair) per hold, per own change and per
   wear-driven switch; no hold without a push.**
4. **The first read after every Connect/re-open (🟢):** §2's table; the "changed since" line never shown — §6.2.

## 11. The skeleton's "Refuted if", per step run

| Step | Refuted if … | Outcome |
|---|---|---|
| BL-1 | a crash; a request missing, doubled or out of order; Safe Mode | **not refuted** (17 requests in order in every full session) |
| BL-2 | "-dirty"; another hash; the card or the line still there | **not refuted** |
| BL-3 | the subscription missing after the Connect | **not refuted** (S2 2838) |
| BL-4 | a hold without a stream packet; an app `SABM` on DLCI 0x04 during the holds; a mode shown that differs from the last packet's | **not refuted** for the wire (each change pushed; no app `SABM` 06:40:02–06:41:31 except the re-open's snapshot #9 at 06:41:33); the mode not shown on that tab |
| BL-5 | a hold not shown; an app `SABM`; a hint or "Take it back again" anywhere | **not refuted** (17 / 17; none) |
| BL-6 | the old hold sentence; the channel-taken line | **not refuted** |
| BL-7 | the stream packet of the own Set shown as "Changed by the Buds"; a `DISC` later than 2 s after the ACK | **not refuted** (the phone's `DISC` 1.507–1.516 s after the ACK in the 12 taps whose claim was not cut — e.g. 6726 06:44:37.547 → 6737 06:44:39.060 = 1.513 s; #34 and #35 were cut by Play 1.12 and 0.48 s after their ACK) |
| BL-8 | a second `SABM`; the app not following | **not refuted** for the 7 tile taps that got their `Set` through; the silent one (§5.3) did not follow — it changed nothing on the Buds either |
| BL-9 | a re-open without the subscription; a changed read without the line; the app sending anything in reaction to the packet | **refuted in part:** "a changed read without the line" — S4 and S7 (§6.2); the other two conditions not refuted |
| BL-11 | an app `SABM` not caused by a tap/tile/Refresh/pull/Connect | **not refuted** |
| BL-12 | a hold not shown | **not refuted** (12 / 12) |
| BL-14 | Safe Mode; "-dirty"; the card | **not refuted** |
| BL-16 | a hold not shown; an app `SABM` | **not refuted** |
| BL-17 | the own Set shown as the Buds' change | **not refuted** |
| BL-19 | as BL-9 | **not refuted** (S12's read equalled the pushed mode) |
| BL-20 | no subscription after the re-open | **not refuted** (S11 540) |
| BL-22 | the line missing after a differing read; the line after a read of the same mode | **not refuted** for the second (S13 equal, no line); the first not exercised (no hold while disconnected) |
| BL-23 | a value 0 shown as a mode | **not refuted** |
| BL-10, 13, 15, 18, 21, 24, 25 | — | not run |

## 12. Verdict table (`RELEASING.md` C4 — the analysing session's proposal; the verdict is the maintainer's)

| C4 category | Found? | Evidence |
|---|---|---|
| Crash | no | §0 (FATAL/ANR/StrictMode 0; 14 sessions) |
| Wrong write | no | §3 (the writes are the user's EQ/in-ear changes and 15 `Set`s, each answered; 0 `WriteSetting 4:13`; nothing on 0x08/0x0a) |
| Safe Mode on the verified firmware | no | `release_5.203` ×3, every write allowed |
| Lost session | **not by the app** | §6: 7 Buds-side closes and 2 ACL ends, every one re-opened by ADR-044 or the user's Connect; the skip by `CHAIN_GUARD_MS` left the session closed until the user tapped Connect (7.8 s) — as designed |
| ADR-064/065's main claim: every hold shown, any tab, both users, no claim | **verified** for the noise-control tab: 29/29 with Play, 12/12 without | §4 |
| The two requests once per session; the re-open subscribes again (M1); 0 never a mode (M2); the own Set as the app's (M3) | **verified** | §2, §6, §5.1 |
| Minor issues | (a) "Changed since the app last saw it" overwritten by the equal `4:13` answer (§6.2); (b) a tile tap cut after the `Get`'s answer sends nothing and says nothing (§5.3, 1 of 13); (c) a same-mode push logged with the kept cause (§4.4); (d) the per-tap collision with Play — 1.3.0's known issue, now measured (§5.2) | |
| Not verified by this run | a hold on the Connection, Sound or Controls tab **on screen** (BL-15/BL-21 not run; part 1's Connection-tab changes reached the app but that tab shows no mode); Home with holds (BL-24); Bluetooth off/on in the Play user (BL-10); another app's EQ/switch change (BL-13/BL-25); the ears (no camera) | |

**The analysing session's view:** no heavy issue. The release goal of 1.3.0 is met in both users with the evidence of this run; (a)–(c) are minor and
fixable in a later version; (a) and (b) deserve a Known-issues line or a quick fix release (the maintainer's choice — Phase D). The decision is the
maintainer's (`RELEASING.md` C4).

## 13. Open questions and proposals (proposals — no code in this session; `AGENTS.md` §6)

| # | Proposal | Evidence | Design (sketch) | Cost |
|---|---|---|---|---|
| **P1** | **Keep "changed since" across the equal second read** | §6.2 (S4, S7) | `causeOfReadAnswer`: an answer equal to the shown mode returns `null` (keeps the cause), as `causeOfStream` does; a test with `CAP-076` H1 4094 → 4135 | small |
| **P2** | **A tile tap whose claim closes before its `Set` reports a failure** | §5.3 | the `Set`'s wait ignores a `Notify`/outcome received before the `Set` was written (a sequence number or a timestamp check), so a cut is `AnswerCutOff`/a send failure → the toast; a test emitting the `Get`'s `Notify` with a delay | small–medium |
| P3 | Log a same-mode push as "unchanged" | §4.4 | `applyAncState`: log the packet's own classification (`unchanged`) instead of the kept cause | trivial |
| P4 | Settle the two packets per change | §4.3 | the experiment of §4.3 (one Connect per fresh ACL; the official app on the same ACL) | a capture |
| P5 | Shorter attempt-2 latency (`CAP-075` §9 P3, still open) | §5.2: attempt 2 0.87–1.11 s after the refusal, 8 of 12 tap claims | unchanged from `TODO.md` §5 | small |
| P6 | Show the mode on the Connection tab (or the notification's line) | §4.2: 14 changes there reached the app but no screen | UI only | small |

**Open questions:** ❓ the two same-mode pushes of 06:47:07/18 (single, both buds out of the case — a wear change of one bud with in-ear detection on?); ❓ the
seven CloseGuard lines (§0) — which sockets; ❓ `PROTOCOL.md` §6's "stored mode per wear state" — this run has three wear-driven switches coinciding with the
Buds' PAUSE/PLAY (§4.2) but no film of the ears; **it does not settle the item** (no promotion proposed); ❓ why the official app's captures show one packet per
change (§4.3).

**`PROTOCOL.md` note (proposal, for the maintainer):** §4.1 — "OpenControl 1.3.0 subscribes once per MAESTRO session (`CAP-076`, 12 of 12 sessions, byte-
identical to `CAP-051` 1177/1178 and `CAP-056` 1451/1450); the Buds pushed every reported change of setting 13 to it, also while Google Play services held the
Message Stream (41 of 41 holds shown); with both buds out of the case each change came twice, with channel 21 and 19 (🟡 one per bud core)."

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-076-2026-10-15_06-35-26_07-08-20-Group_BL/CAP-076-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-076-2026-10-15_06-35-26_07-08-20-Group_BL/CAP-076-FINDINGS
