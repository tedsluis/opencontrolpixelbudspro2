# Findings: `CAP-073` (Group BI — the rebuilt 1.2.0 release APK, build `5b4d5db`, installed over the 1.2.0 build `ec6d163` in a GrapheneOS secondary user without Google Play: the re-test of the ADR-061 hold and the finer balance slider)

Standardized, evidence-based extraction from the screen recording (no audio track), the two HCI snoop logs, the debug export, the app logcat, the shell log and
the system log, per `ai-sessions/0085`. The timeline these findings refer to is `CAP-073-EVENT-NOTES.md` (prefixes B, A, E, L, X, S as defined there; all times
phone time; S cited as film seconds `t`, phone = 08:23:35.34 + t). **The maintainer did not follow the skeleton** ("I only tested the changed functionality",
chat 2026-10-10); what was not run is listed in §8 and §9.

- 🟢 **FACT** — directly observed in this capture (frame number, log line, film time or `file:line` given).
- 🟡 **HYPOTHESIS** — plausible reading, not yet independently confirmed; the settling experiment is named.
- ⚪ **ASSUMPTION** — not tested here.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-073` · **Date:** 2026-10-10, phone 08:23:35.3–08:33:24.6 · **Firmware:** 🟢 `release_5.203` (11 of 11 announcements; Info tab S t=10, t=217)
· **Phone:** Pixel 9a, GrapheneOS, Android 17 `CP3A.261005.005` (= `CAP-072`), **secondary user 10, no Google Play** · **App under test:** OpenControl **1.2.0,
build `5b4d5db`** (release-signed, versionCode 10200), installed over **1.2.0, build `ec6d163`** · **HCI logs:** B `CAP-073-btsnoop_hci.log` (7,064 packets,
08:22:51.771–08:35:07.913, the run), A `CAP-073-btsnoop_hci.log.last` (28,116 packets, 2026-10-09 18:32:14.701 – 2026-10-10 08:22:48.796, before the run) · **Buds:**
`…:07`, classic handle `0x000b` in every ACL; no LE link to the Buds.

Commands used throughout (rule 4a; all on handle `0x000b`, shown matching the Connection Complete events B 159, 3973, 4483, 4953, 5589, 6069, 6581 —
`tshark -r <log> -Y "bthci_evt.code==0x03 || bthci_evt.code==0x05 || bthci_evt.code==0x2c || (bthci_evt.code==0x3e && (bthci_evt.le_meta_subevent==0x01 ||
bthci_evt.le_meta_subevent==0x0a))" -T fields -e frame.number -e frame.time -e bthci_evt.code -e bthci_evt.le_meta_subevent -e bthci_evt.bd_addr -e
bthci_evt.connection_handle -e bthci_evt.status -e bthci_evt.reason`, exit 0): the RFCOMM inventory `tshark -r <log> -Y "bthci_acl.chandle==0x000b && btrfcomm"
-T fields -e frame.number -e frame.time_epoch -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type -e btrfcomm.len -e data.data` fed to a scratch parser
(`ai-sessions/0085` RESULT: per DLCI and direction a byte stream; pw_hdlc split on `0x7e`, unescaped, `zlib.crc32` over address + control + payload against the
trailing 4 bytes little-endian, the RpcPacket read with `scripts/pwrpc_decode.py`'s own `parse`/`describe`; the Message Stream as `[Group][Code][Len:2 BE][Value]`).
**CRC-32 OK on all 829 pw_hdlc frames** (B 381, A 448; 0 bad, 0 short). The decoder's positive control is `CAP-072-FINDINGS.md`'s (`CAP-036` 1415/1423).

## 0. Build, install, user, logs and privacy (🟢 FACT unless marked)

- **The installed APK is the B1 build, byte for byte:** pulled after the run (2026-10-10, the maintainer attached the phone): `adb shell pm path --user 10
  io.github.tedsluis.opencontrolpixelbuds` → one `base.apk` (exit 0; the same path for `--user 0`), `adb pull`, `sha256sum` → `ac04415eabf4369a49e9f88230aa83fc858a7e3ea0223d725c14a43ac5a67220`
  = `~/opencontrol-1.2.0-tested/opencontrol-pixelbudspro2-1.2.0.apk` = B1 (`ai-sessions/0084` RESULT, "After the session — the build"; certificate
  `a7530f5c…d8dcb` there). On film: **"App: 1.2.0, build 5b4d5db (2026-10-10)"**, no "-dirty" (S t=217–225); before the update **"App: 1.2.0, build ec6d163
  (2026-10-09)"** (S t=10–19). dex2oat: `app-version-name:1.2.0,app-version-code:10200`, `compilation-reason=install` (system log 08:26:56). This closes `CAP-072`'s
  open question for this build ("whether the installed APK equals `dist/1.2.0`").
- **The update path:** `adb` install at 08:26:55 (`abb_exec:package`); `installer_clear_app_data_caller … 39` twice (X 147–148, the code cache only); dex2oat
  7.9 s (X 154); `Force stopping … user=10: killDueToPackageUpdate`, PID 15696 killed (X 164–167); `PACKAGE_REPLACED replacing: true` for uid 10354 (user 0) and
  1010354 (user 10) (X 242–243); PID 17391 `top-activity` at 08:27:06.619 (X 260). The dying process's socket: the phone's `DISC` of DLCI 2 at 08:27:05.399
  (B 3025). Android showed "Updaten…" (S t=210–211). `lastUpdateTime` 2026-10-09 18:33:54 → 2026-10-10 08:27:05 (shell log).
- **The settings survived the update:** Dark mode **On** (S t=9 before, t=215 after); Debug mode **on** (S t=20 before, t=227 after; E line 9 is a hex line);
  `Permissions (start): … NOT_REQUESTED -> GRANTED` (E 1, logged at every process start); no permission dialog or Android picker on film; the session opened by
  itself 0.15 s after the start (E 4, LINK_BACK; ADR-044).
- **The 2026-10-09 18:33:54 install:** `dumpsys package` (2026-10-10, phone attached) prints `firstInstallTime=2026-10-09 18:33:54` under **User 0** and
  `17:24:41` under User 10 — `ec6d163` was installed into the **Owner user** after `CAP-072` ended; one APK serves both users. A (the Owner's day) shows that app
  in use at 18:35, 19:29, 06:43–06:44, 07:07–07:08 and 07:13 (each session with the thirteen requests and `GetHardwareInfo`; EQ and Volume-EQ writes at 07:07–07:08,
  A 21952–22180; `wm_set_resumed_activity: [0,…]` at 07:07:08). Not the run.
- **No Google Play in user 10:** P0 lists only `app.grapheneos.gmscompat.config`, `…lib`, `gmscompat`, `exit=0` (as `CAP-072-FINDINGS.md` §0). Not made:
  `am get-current-user` and the P0 positive control; the user is in X 2 (`am_switch_user: 10`, 08:22:47.871).
- **No network from the app:** no `INTERNET` permission (B1's `aapt2 dump badging`, `ai-sessions/0084`; the pulled APK is the same file).
- **Processes:** 15696 (`ec6d163`, user 10) 08:22:47.959 – 08:27:05.363; **17391 (`5b4d5db`) 08:27:06.619 to the end — no crash, no ANR, no force-stop.**
  `grep -c` over the system log's run window (08:22:40–08:34:07, 65,453 lines) → `FATAL` 0, `ANR in` 0, `am_crash` 0, `am_anr` 0, `Decoder fault` 0, `Unexpected
  error` 0 (exit 1 each); positive control `grep -c "Settings read"` → 11 (= the 11 sessions). `StrictMode` 195 lines, all from three `android.process.acore`
  processes (18024, 18905, 15838) — none from 15696/17391.
- **Warnings from the app's process:** `W System : A resource failed to call close.` ×9 (17391, FinalizerDaemon tid 17402), after session losses — the pattern of
  `CAP-066`/`CAP-071`/`CAP-072` (🟡 framework objects, `ARCHITECTURE.md` §12). `am_wtf … Background started FGS: Allowed [callingPackage: io.github.tedsluis…]` ×12
  at every Connect and re-open (e.g. 08:27:07.246, 08:27:42.460, 08:29:48.241) — Android notes and allows the foreground-service start (as `CAP-072`).
- **The `SIGQUIT`s are the bugreport:** `bugreportz -v` / `-p` at 08:33:47 (X 1264–1265); "reacting to signal 3" at 08:34:01–07 for `system_server`,
  `networklocation` and `camera.services` only; the system log ends 08:34:07.
- **Logs:** E (681 lines, 08:27:07.086–08:32:20.679; "Debug log exported (681 lines)" at 08:32:54.338, L) holds only the `5b4d5db` process; the `ec6d163` process's
  66 lines are in the system log (X). L's 260 `OpenControlBuds` lines from 06:31:38.986 UTC equal E's lines 08:31:38.986–08:32:20.679 except a 1-ms rounding on 14
  (scratch `diff`). B and A are consecutive (A ends 08:22:48.796, B starts 08:22:51.771); A ends with no Buds ACL open (the last ended 07:43:06, A 26567).
- **Privacy:** the film is committed **blurred** where the Wi-Fi name shows (Quick Settings, film 571.4–575 s and 588–589.4 s): `ffmpeg -i <film> -filter_complex
  "[0:v]split[b][c];[c]crop=1080:820:0:0,boxblur=30:6[d];[b][d]overlay=0:0:enable='between(t,569.5,575.5)+between(t,586.5,589.4)'[v]" -map "[v]" -c:v libx264 -preset
  veryfast -crf 23` (no audio stream to copy). The serials stay readable on the Info tab (ADR-010 scope, as `CAP-072`). The system log is committed as the extract
  `CAP-073-logcat-extract.txt`: `awk '$1=="10-10" && $2>="08:22:40.000" && $2<="08:34:10.000" && ($3=="15696"||$3=="17391"||/opencontrolpixelbuds/||
  /com\.android\.bluetooth[\/,:]/||/dumpstate/||/bugreportz/||/am_switch_user|uc_switch_user|ssm_user/||/installer_clear_app_data_caller/||/dex2oat64: dex2oat took/||
  /ArtService: Dexopt result/)' CAP-073-logcat-all.txt | grep -v -i -F -f <patterns: the account e-mail, the SSID, "OpenControlBuds: DLCI">` → 1,360 lines; the
  account e-mail, the SSID, the first two serials' hex and any `xx:xx:xx:xx:xx:xx` address → 0 each in the extract (exit 1), while the full log has SSID 122, serial
  hex 11 + 11 and the Buds' address 477. **The originals** (film `04f55af4…a308b`, full system log `8ad4046e…57f18`, SHA-256) are kept in
  `~/opencontrol-capture-originals/CAP-073/` (`cmp` identical); `CAP-073-sha256sum.txt` lists the committed files. As in `CAP-072`, the export and L carry the
  `GetHardwareInfo` answer inside the Debug-mode hex dump (ADR-058 item 5 and its 2026-10-10 Update); no serial and no MAC as text (`grep -c -i -E
  '([0-9a-f]{2}:){5}[0-9a-f]{2}'` → 0 in E and L; L prints `XX:XX:XX:XX:6E:07`).

## 1. ACLs, the Message Stream claims and the Definition-of-done negative

| ACL (B) | Start | End | Note |
|---|---|---|---|
| 1 | B 159 08:23:01.706 (**the phone pages**: Create Connection B 157, 17 ms after the app's "Connecting") | B 3968 08:30:30.238 `0x13` | sessions 1–4; the Right bud into the case at 08:30:23 |
| 2 | B 3973 08:30:32.393 (the Buds: Connection Request B 3969) | B 4478 08:30:41.300 `0x13` | session 5 |
| 3 | B 4483 08:30:45.973 (the Buds) | B 4950 08:30:56.298 `0x13` | session 6 |
| 4 | B 4953 08:31:17.294 (**the phone pages**, B 4951, 2 ms after the Connect) | B 5584 08:31:46.896 `0x13` | sessions 7, 8; both buds in the case at 08:31:18 |
| 5 | B 5589 08:31:51.313 (the Buds) | B 6064 08:31:55.364 `0x13` | session 9 |
| 6 | B 6069 08:32:03.610 (the Buds) | B 6576 08:32:13.208 `0x13` | session 10 |
| 7 | B 6581 08:32:16.554 (the Buds) | B 7038 08:32:20.603 `0x13` | session 11 |

- 🟢 **No Play-services claim during the run:** `tshark -r <log> -Y 'btrfcomm && frame contains 03:08:00:02:01:25' | wc -l` → **0** in B (exit 0); positive
  control `CAP-066-btsnoop_hci.log.last` → 22 (exit 0). **A holds 14** — the Owner user before the switch (Play services present there), not the run. The phone
  sent no SASS (`07 xx`) message in B (inventory: only `08 11` and `08 12` from the phone on DLCI 4/5).
- 🟢 **12 Message Stream claims in B, all the app's:** each phone `SABM` on DLCI 4 or 5 (B 270, 3078, 3318, 3484, 3823, 4262, 4853, 5064, 5522, 5878, 6441, 6840)
  is followed by `08 11 00 00` as the app's first message. 11 are Connect/re-open **snapshot claims**, released by the phone's `DISC` **1.504–1.513 s** after their
  answering `Notify` (e.g. B 3100 08:27:07.417 → `DISC` B 3143 08:27:08.928); **one is the ADR-061 hold** (B 3484 08:27:59.763 → `DISC` B 3654 08:28:30.890, §4).
- 🟢 **No other app on the Buds' RFCOMM:** DLCIs on `0x000b` in B are 0, 2/3 (MAESTRO), 4/5 (Message Stream) and 8/9 (opened by the Buds: `SABM` B>P, HFP as in
  `CAP-072`); nothing on DLCI 0x0a/0x0b; no phone `SABM` other than on 0, 2, 3, 4, 5.
- 🟢 **ANC `Notify` Settable** (`01 e8 [S] mode`): `00` with mode OFF in 8 snapshot claims (08:23:02, 08:27:07, 08:30:33, 08:31:17, 08:31:44, 08:31:52, 08:32:05,
  08:32:17 — buds in the case or one just taken out), `e8` in the others; the four ANC `Set`s were sent only after an `e8` `Notify` of the same claim.
- 🟢 **After the last release** (B 7002 08:32:18.873) the Buds sent three more "Battery updated" frames on DLCI 4 before their `UA` (B 7003, 7006, 7013); the
  phone's stack answered each with `DM` (B 7004, 7008, 7014), then the `UA` came (B 7016). 🟡 The stack's answer to frames on a DLCI it is closing (the TS 07.10
  "disconnected mode" response; no source found that names this case, `ai-sessions/0085` search); harmless — the app had released the channel.
- 🟢 **Another LE device** ("Charge 6", handle `0x0040`, 08:23:51–08:26:06, ATT only) — excluded by the handle filter.

## 2. The Connect read — thirteen requests (BI-1, BI-3; W1–W3; ADR-058)

🟢 **11 sessions** (1 by `ec6d163`, 10 by `5b4d5db`): the Buds' unsolicited `GetSoftwareInfo` → `ReadSetting` **16, 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29** →
one `SubscribeRuntimeInfo` → **one `GetHardwareInfo`**, in that order, **in 11 of 11 sessions**; every request answered `status=OK` (E "Settings read" ×10 + X ×1).
Channels: 21 in sessions 1, 2, 5, 8, 10; 19 in 3, 4, 6, 7, 9, 11. Command: the scratch session listing over the inventory (`ai-sessions/0085` RESULT).

- 🟢 **Every request is byte-identical to `CAP-072`'s own:** the 171 phone→Buds frames on DLCI 2/3 (`tshark -r CAP-073-btsnoop_hci.log -Y "bthci_acl.chandle==0x000b &&
  btrfcomm.dlci in {2,3} && frame.p2p_dir==0 && btrfcomm.len>0" -T fields -e frame.number -e data.data`, exit 0) are 36 distinct byte strings; each of the 30
  Connect-read strings (15 per channel) is found with `tshark -r <CAP-072 log> -Y "frame contains <bytes>"` in `CAP-072-btsnoop_hci2.log.last` (channel 19: A 8540 … 8616;
  channel 21: A 5595 … 5667), e.g. `GetHardwareInfo` ch 21 `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 e3 a5 ec 28 f9 67 61 b5 7e` (B 333) = `CAP-072` A 5667 = `CAP-036`
  1415, ch 19 `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 e3 a5 ec 28 ff 1e bb b7 7e` (B 3376) = `CAP-072` A 8616 = `CAP-024` 801. The six writes: §3.
- 🟢 **`GetHardwareInfo`: 11 requests, 11 answers, 45–166 ms**, never a second one in a session, none outside Connect/re-open; the answer's three length-14 strings
  `5707…51`, `5708…09`, `5707…47` in every answer (E's hex lines; redacted per ADR-058), field 8 `…93` on 21, `…94` on 19 (as `CAP-072`). Info tab: Case / Right bud /
  Left bud with "(from the Buds, 08:27:08)" = B 3142's time 08:27:08.202 (S t=217).
- 🟢 **The read values** (session 11, B 6803 …): `16:[-2.0, 0.0, 2.0, 3.0, 5.0]`, `2:1`, `4:1`, `7:{1:{4:{1:5}} 2:{4:{1:5}}}`, `11:1`, `12:{1:1 2:0 3:1 4:1}`, `15:1`,
  `17:8`, `19:0`, `22:1`, `27:1`, `28:1`, `29:2` (E 636–659); the screens show them (S t=294–298 EQ, t=319 and t=463–466 Controls).

## 3. Every write — byte for byte

🟢 **6 `WriteSetting` requests, all answered by the empty `RESPONSE` status OK**, 4 ANC `Set`s → 4 ACKs, 0 NAK. Bytes after the channel's common prefix
`7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 1d 9a 8c 9e` (19) / `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 1d 9a 8c 9e` (21):

| Write | Frames (request → answer) | Bytes | Reference | Equal |
|---|---|---|---|---|
| Balance `4:{17:9}` (Right 5) | B 3679 → 3681 | `2a052203880109ee95d6c27e` (19) | `CAP-072` A 19395 | ✓ |
| Balance `4:{17:27}` (Right 14) | B 3682 → 3684 | `2a05220388011ba6e46f317e` (19) | none captured — the codec's zigzag form, CRC OK | — |
| **Balance `4:{17:7}` (Right 4)** | B 3685 → 3687 | `2a052203880107e9b86e257e` (19) | **`CAP-070` A 3747** (the skeleton's reference) | ✓ |
| Balance `4:{17:8}` (Left 4) | B 3691 → 3694 | `2a05220388010878a5d1b57e` (19) | none captured — codec form, CRC OK | — |
| Earbuds replaced off `4:{28:0}` | B 5419 → 5421 | `2a052203e00100d2b7cefd7e` (19) | `CAP-072` A 14583, `CAP-024` 1988 | ✓ |
| Earbuds replaced on `4:{28:1}` | B 6510 → 6516 | `2a052203e00101044178ac7e` (**21**) | `CAP-058` 5643 (`SettingsFixtures.CS28_ON_CH21_5643`), `CAP-071` 4289 | ✓ |
| ANC `Set` TRN / Off / NC / Adaptive | B 3557, 3577, 3587, 3597 → ACK 3566, 3579, 3589, 3599 | `08 12 00 14 01 e8 e8 80` (`20`, `08`, `40`) + 16 × `00` | `CAP-072` A 11939 (`…08`), `CAP-068` 1146/1090/1039 | ✓ |

Command for an "Equal" check: `tshark -r <reference log> -Y "frame contains <bytes>" -T fields -e frame.number` (exit 0). The zigzag reading of field 17 (9 → −5
"Right 5", 27 → −14 "Right 14", 7 → −4 "Right 4", 8 → +4 "Left 4") matches the labels on screen after each OK (S t=299, 300, 304, 314) and `CAP-072` §3's table.

- 🟢 **Only after the answer:** each balance label and each switch changed after its `RESPONSE` (e.g. release 4: "Right 4" at S t=313 until the OK at 08:28:49.068,
  "Left 4" at t=314); each ANC mode after its ACK (§4); the case-sound switch after its OK (S t=484, t=513).
- 🟢 **Read back:** `17:8` in every later session (E 213 …); `28:0` in the sessions of 08:31:43 and 08:31:52 (E 470, 529), `28:1` from 08:32:17 (E 656).
- 🟢 **The Buds' end state** (session 11): EQ −2, 0, 2, 3, 5 (from the Owner user's 07:08:44 write, A 22180), **balance Left 4** (not Centre), mono off, conversation
  detection on, Volume EQ on, touch on, both holds Noise control, the mode list **Noise cancellation + Adaptive + Transparency** (Off unticked), in-ear on, head
  gestures on, Multipoint on, both case sounds on.

## 4. The ADR-061 hold and "Changed by the Buds" (BI-6 … BI-11; W4–W7; the maintainer's observation)

**The maintainer's statement:** "The ANC modes during playback were right! When I did a long press on a bud, the ANC modes in the app changed too!"

**What the evidence shows (🟢 unless marked):**
- **The hold opens with the tab:** the ANC tab is on screen from film 264 (08:27:59.4, the last value "Transparency" from 08:27:44); one claim — `SABM` B 3484
  08:27:59.763, `UA` B 3486, `08 11 00 00` B 3492, `Notify` `08 13 00 04 01 e8 e8 08` B 3503 08:27:59.918 (NC; screen "Noise cancellation" at t=265) — and E 131
  **"Message Stream hold started: the noise-control tab is on screen (ADR-061)"** at 08:27:59.920, 2 ms after the `Notify` (`releaseOrHold`,
  `BudsRepositoryImpl.kt:477–488`: the claim's `finally` keeps the channel while the tab is shown and the app visible).
- **No `DISC` while the tab is shown:** no RFCOMM control frame on DLCI 4/5 from B 3486 to B 3654 (inventory; `tshark -r B -Y "bthci_acl.chandle==0x000b && btrfcomm.dlci==4
  && btrfcomm.frame_type!=0xef && frame.number>=3480 && frame.number<=3660"` → B 3484 `0x2f`, 3486 `0x63`, 3654 `0x43`, 3656 `0x63` (exit 0) — the positive control is the claim's own `SABM`/`UA`). The channel stayed open
  **30.97 s** (08:27:59.763–08:28:30.890) — `ec6d163` released every claim 1.5 s after its answer (B 450; `CAP-072` A 11947).
- **Four unprovoked `Notify`** on the held channel: B 3531 08:28:05.455 `e8 e8 40` (Adaptive), B 3543 08:28:08.072 `e8 e8 80` (Transparency), B 3546 08:28:11.877
  `e8 e8 08` (NC), B 3547 08:28:15.749 `e8 e8 40` (Adaptive). **No `08 11` in the 2 s before any of them** (the app's last `08 11` before them is B 3492, 5.6 s
  earlier) and **nothing from the app after them** (the phone's only frames on DLCI 4 are zero-length UIH credit frames, `btrfcomm.len==0`, B 3532, 3544, 3548).
  The screen changed **without a touch** each time: Adaptive at S t=270, Transparency t=273, NC t=277, Adaptive t=280 (no touch indicator in those frames; the
  recorder shows touches elsewhere, e.g. t=285). The cycle NC → Adaptive → Transparency → NC matches the mode list read at Connect (`12:{1:1 2:0 3:1 4:1}`, Off
  unticked).
- **The cause:** `causeOfNotify` (`BudsRepositoryImpl.kt:358–364`) classes a `Notify` with no `Get` and no `Set` waiting and a mode other than the shown one as
  `CHANGED_BY_BUDS` — all four qualify. 🟡 The noise-control (i) — where the line "Changed by the Buds at HH:MM:SS (…)" is shown — **was not opened** during the hold
  (no (i) dialog on film from t=264 to t=294), so the line itself is not seen on film; settles: open the (i) after a hold.
- 🟡 **The four `Notify` are press-and-holds:** the maintainer's statement; the buds and the head are not on film (no camera). Consistent with it: no app request
  in the window, the order follows the press-and-hold mode list, and Google's Hearable Controls page says "If the user changes the setting via headset gesture or
  companion application … the Provider should also send notification to all connected Seekers"
  (`developers.google.com/nearby/fast-pair/specifications/extensions/hearablecontrols`, fetched 2026-10-10 by `ai-sessions/0085`). Which bud: not determinable.
- **Taps on the held channel:** four taps (Transparency 08:28:19.0, Off 20.8, NC 22.4, Adaptive 23.6), each `08 11` → `Notify` → `Set` → ACK (B 3550–3602) with
  **no `SABM`** (none between B 3484 and B 3654) — the screen's mode after each ACK (S t=284–288).
- **Other pushes on the held channel:** "Battery updated" `03 03 00 03 64 64 ff` ×3 at 08:28:07.4 and 08:28:27.6 (B 3534–3538, 3629–3631) and SASS `07 34` ×4 —
  received by the app (E 134–163) without a request.
- **The hold ends with the tab:** the finger on "Sound" at film 293.899, the Sound content at 294.029 (= 08:28:29.37); E 164 **"Message Stream hold ended: the
  noise-control tab was left"** at 08:28:29.381; the phone's **`DISC` B 3654 at 08:28:30.890 — 1.509 s later** (`MESSAGE_STREAM_LINGER_MS = 1_500L`,
  `BudsRepositoryImpl.kt:1657`); `UA` B 3656; E 166 "released". The skeleton's window (1.3–2.0 s) is met.
- **Not exercised in this run:** the tile on the held tab (BI-10), Home / app hidden (BI-12, `HoldEnd.APP_HIDDEN`), a session loss or the Buds closing the channel
  during the hold (`SESSION_LOST`, `CHANNEL_CLOSED`), a re-open while the tab is shown (BI-13), a second entry of the tab. These branches are covered by the unit
  tests of `ai-sessions/0084` (`BudsRepositoryImplTest`, 10 hold tests), not by hardware.
- **"Changed by the Buds" outside the hold:** the re-open of 08:30:32 read `e8 00 20` (OFF, B 4289) and 334 ms later, unasked, `e8 e8 80` (TRN, B 4315) — a wear
  change inside a snapshot claim (the Right bud just out of the case, runtime info 08:30:34); the Worn line moved from "Not worn (checked 08:30:33)" to "Probably worn
  (checked 08:30:34)" (S t=418–419). As in `CAP-072` A 13515/13519.
- **"During playback":** 🟢 **no media played during the run** by AVRCP: `PlaybackStatusChanged` Paused/Stopped in every registration (B 426, 577, 585, 4159, …, 6755,
  `tshark -Y btavrcp`, 50 frames) and the phone set absolute volume **0 %** at 08:23:04.067 (B 575); AVDTP streams were started and suspended briefly around
  actions (e.g. Start B 3448 08:27:57.592 → Suspend B 3508 08:28:03.067; Start B 3553 08:28:19.101 → Suspend B 3605 08:28:27.196 — `btavdtp.signal_id in {7,9}`,
  exit 0) and **no stream was open from 08:28:03.142 to 08:28:19.101**, the window of all four press-and-holds. 🟡 The maintainer heard the mode changes without
  music (noise cancellation and transparency are audible on their own) — or the remark refers to listening outside this recording. The modes **themselves** are on
  the wire and on screen as stated.

**Answer:** 🟢 ADR-061's main path works on hardware: entering the noise-control tab opens and **keeps** the Message Stream claim, the Buds' own mode changes reach
the app and the screen follows them without a tap, taps use the held channel, and leaving the tab releases it 1.509 s later. `CAP-072` §5's gap (a press-and-hold
never shown) is closed for the tab. Not seen on film: the (i) line's text, and the branches listed above.

## 5. The balance slider (BI-20; W12; `AUDIO-003`)

- 🟢 **The live label:** while dragging the label shows the finger's value with "— release to set": "Right 6 — release to set" (S t=301), "Right 3 — …" (t=302),
  "Right 4 — …" (t=303), "Right 2 — …" (t=306), "Left 6 — …" (t=307), "Left 43 — …" (t=308), "Left 83 — …" (t=309), "Left 77 — …" (t=310), "Left 9 — …" (t=311),
  "Left 3 — …" (t=312); after each release the Buds' value without the suffix ("Right 5", "Right 14", "Right 4", "Left 4").
- 🟢 **One write per release, none during a drag:** 4 releases → 4 `WriteSetting 4:{17:…}` (B 3679, 3682, 3685, 3691); no other write between 08:28:30 and 08:28:50
  (the inventory; the drags of t=301–303 and t=306–312 produced none).
- 🟢 **"Right 4" on the third release** (`4:{17:7}` = `CAP-070` A 3747 byte for byte) — `CAP-072` needed ≈ 30 writes; the skeleton's limit "more than 3 attempts"
  is not exceeded.
- 🟡 The last drag ended on "Left 3 — release to set" at S t=312 and wrote `17:8` (Left 4) at 08:28:48.806: the finger moved between the last 1-fps frame and the
  release (the label follows the finger, so the written value is the one at release). The skeleton's "then Centre" was not done; the Buds end on **Left 4**.
- 🟢 The fine centre: values −6 … +9 near the middle and −43/−83/−77 far out on screen are consistent with ±10 in steps of 1 in the middle third
  (`EqScreen.kt` `balanceFromPosition`); 🔴 the step size near the centre is not measured from the film (no drag across adjacent values at full frame rate).

## 6. Session ends, re-opens and processes (BI-3, BI-4; `PAIR-003`, `CASE-004`, `CASE-005`)

🟢 **11 session ends:** 1 the old process's kill at the update (08:27:05.399, phone `DISC`), 1 the Disconnect tap (08:29:46.094), 2 Buds-side `DISC`s with the ACL
up (08:27:40.931, 08:31:41.598; "the Buds closed the channel"), **7 ACL drops by the Buds** (`0x13`, 08:30:30.238 … 08:32:20.603; "Android's link to the Buds went
down around the loss", each first "provisional … the Buds closed the channel" and corrected within 0.2 s by the link reading — E 242–244, 308–310, …). Automatic
re-opens: AFTER_LOSS 1.500 s after "Ready -> Disconnected" (08:27:42.445, 08:31:43.103; `SessionReopener.kt` `LOSS_REOPEN_DELAY_MS`), LINK_BACK when Android's link
came back (08:27:07, 08:30:32, 08:30:46, 08:31:51, 08:32:03, 08:32:16 — four first attempts failed on the multiplexer after ≈ 100 ms and the 400-ms retry succeeded,
as `CAP-070`–`072`); the 10-s chain guard skipped 6 (E 306, 366, 485, 548, 611, 677; losses 3.1–8.7 s after a re-open). Connects by the user: 08:23:01
(old build, before the film), 08:29:48 (tap), 08:31:16 (🟡 a pull-to-refresh on Controls while not connected — `ARCHITECTURE.md` §2.4: "otherwise the action of the
Connection screen's own button"; the phone paged the Buds, B 4951). 🟢 No session stayed lost without the documented reason; the app ended not connected (last loss
08:32:20.603, nothing after).

- 🟢 **The channel follows the bud out of the case** (`PROTOCOL.md` §2.2a lead L-1): only the Left out ⇒ 19 (08:27:43, 08:30:47, 08:31:52, 08:32:17), only the Right
  out ⇒ 21 (08:30:33, 08:31:43, 08:32:05) — 7 more samples of both rules; both in the case: 21 (08:23:01, 08:27:07) and 19 (08:31:17), not fixed (as `CAP-072`) —
  runtime info 6.2 (Left) / 6.3 (Right) field 2.
- 🟡 **The ACL drops at single-bud swaps:** in `CAP-072` the Buds dropped the ACL only when both buds went into the case; here all 7 came while one bud was
  in the case and the other out (by the last runtime-info packet before each), during quick swaps (08:30:23–08:32:20). 🔴 Whether the Buds hand the link over between buds; settles: a run with one bud swapped
  at a time, 30 s apart, with the camera on the case.
- 🟢 Every re-open showed "—" for unread switches until the reads answered (S t=488, 497, 509, 522 for the case sounds; t=461 for Controls).
- **Not run:** Bluetooth off/on (BI-4), the force-stop and the dumps (BI-14), the early Multipoint tap (BI-15), the rotation during the save dialog (BI-16).

## 7. `ec6d163` → `5b4d5db` inside the run

| Aspect | `ec6d163` (08:22:59–08:27:05) | `5b4d5db` (08:27:07 →) | Same? |
|---|---|---|---|
| Connect read | the thirteen, one `GetHardwareInfo` (session 1) | the same (sessions 2–11) | ✓ byte-identical (§2) |
| Snapshot claim | released 1.513 s after its answer (B 450) | 1.504–1.511 s (10 claims) | ✓ |
| Noise-control tab | not opened on film; no claim between 08:23:03.5 and 08:27:05 | **held 31 s** while shown | the change of ADR-061 (`CAP-072` §5 is the old behaviour) |
| Settings, permissions, association | Dark mode On, Debug mode on | kept; no dialog | ✓ |
| Texts | — | the battery (i) "… for 40 seconds or more with both buds on a table …" (S t=235, 259, 376) — no capture id | ✓ the rebuild's text |
| Warnings | — | "A resource failed to call close" ×9, `am_wtf` FGS at each Connect | ✓ as before |

🟢 No behaviour change outside the hold and the slider was found. The (i) texts of the noise-control card (the ADR-061 sentence) were not opened on film.

## 8. The skeleton's "Refuted if", applied literally per step that was run

- **BI-1** (crash; light theme; a request missing or out of order; DLCI 0x04 open 3 s after the snapshot): not refuted — dark, 13 requests in order in 11/11
  sessions, every snapshot released ≤ 1.513 s after its answer.
- **BI-2** ("-dirty"; another hash; no serials): not refuted — `5b4d5db`, serials Case/Right/Left.
- **BI-3**: not refuted. **BI-4**: not run.
- **BI-5** (an empty file): not refuted — 681 lines, 66.75 kB (S t=584).
- **BI-6** (a `DISC` while the tab is shown): **not refuted** — none in 30.5 s.
- **BI-7/BI-8** (no `Notify`; the `Notify` but no line; the app sending anything in reaction): no `Notify` — not refuted (four); the app sending — not refuted;
  **"the line" — not checked** (the (i) not opened).
- **BI-9** (a second `SABM`; the line still shown): `SABM` — not refuted; the line — not checked.
- **BI-10**: not run.
- **BI-11** (no `DISC`, or one before 1.3 s): **not refuted** — 1.509 s.
- **BI-12, BI-13** (with the tab shown), **BI-14 … BI-19**: not run.
- **BI-20** (more than one write per release; a write during a drag; "Right 4" taking more than 3 attempts): **not refuted** — 4 releases, 4 writes, none during a
  drag, "Right 4" on the 3rd. The "then Centre" part: not done (Left 4).

## 9. Verdict table — the rebuilt 1.2.0 (`RELEASING.md` C4)

| # | What | Class | Evidence |
|---|---|---|---|
| 1 | The hold (ADR-061): opened on the tab, kept 31 s, four Buds-side changes shown without a tap, taps on the held channel, released 1.509 s after leaving | **works** (the main path) | §4 |
| 2 | The hold's other branches (tile, Home, loss, re-open with the tab shown) and the (i) line's text | **not hardware-tested** (unit-tested in `ai-sessions/0084`) | §4, §8 |
| 3 | Balance slider: live label, one write per release, "Right 4" in 3 | **works** | §5 |
| 4 | Every request byte-identical to `CAP-072`; 11/11 sessions complete; 6/6 writes and 4/4 `Set`s answered | **no regression** | §2, §3 |
| 5 | The installed APK = B1; update kept the settings; no crash, ANR or app `StrictMode` | **OK** | §0 |
| 6 | Not run: Bluetooth off/on, T11 (force-stop + dumps), C12, S12, H5, Volume EQ (T7), in-ear detection off (V8) | open test items — carried from `CAP-072` | EVENT-NOTES step mapping |
| 7 | "During playback": no media on the wire in the run | the maintainer's statement, not confirmed — not a defect | §4 |
| 8 | Buds end with balance Left 4 | `TODO.md` §4 restore | §3 |
| 9 | 7 ACL drops during single-bud swaps (the Buds' `0x13`) | observation (the Buds; every one recovered) | §6 |
| 10 | P9: the app opened and connected 34 s before the film; the Owner user used the app before the run | test-procedure deviation | §0 |

**No heavy defect:** no crash or ANR; every write answered and byte-identical where a reference exists; no write before its answer; no Safe Mode on `release_5.203`;
every session end explained and recovered; no setting lost by the update; no request outside the thirteen + the user's writes; nothing on DLCI 0x08/0x0a from the app.

**The analysing session's view (the decision is the maintainer's):** what ADR-061 was built for — a press-and-hold shown on the noise-control tab — is on the wire
and on screen; the untested branches are release-and-close paths whose failure mode is a channel released too late or early (the ADR-032 behaviour), not a wrong
write. Together with `CAP-072` (every 1.2.0 item and every write on hardware), this supports **OK — ready to merge and publish**, with the untested items kept as
open test items (`TODO.md`).

**Verdict (the maintainer, chat 2026-10-10, `AskUserQuestion` "Verdict", option *"OK — ready to release (Recommended)"*):** **OK — the rebuilt 1.2.0 (`5b4d5db`) is
ready to merge and publish** (`RELEASING.md` C4); the hold's untested branches become test items in `TODO.md`. The maintainer before the session: "Met de
resultaten van CAP-072 moet dit genoeg zijn voor een release" ("With the results of CAP-072 this should be enough for a release").

## 10. Open questions

- 🔴 The "Changed by the Buds" line's text after a press-and-hold (the (i) was not opened) — settles: open the noise-control (i) after a hold.
- 🔴 The hold's tile, Home, loss and re-open branches on hardware.
- 🔴 Why the Buds dropped the ACL at single-bud swaps here and not in `CAP-072` (§6).
- 🔴 What the case sounded like with Earbuds replaced off and on (no audio track; `CAP-072` §6's question stays open).
- 🟡 Which bud and which gesture produced the four `Notify` (no camera).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-073-2026-10-10_08-23-35_08-33-24-Group_BI/CAP-073-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-073-2026-10-10_08-23-35_08-33-24-Group_BI/CAP-073-FINDINGS
