# Findings: `CAP-072` (Group BH — the release-signed 1.2.0 APK installed over 1.1.1 in a GrapheneOS secondary user without Google Play: the release run of 1.2.0, film 1)

Standardized, evidence-based extraction from the four HCI snoop logs, the camera film (no sound), the screen recording (with sound), the four debug exports,
the app logcat, the shell log and the system log, per `ai-sessions/0083`. **Film 2 (BH-25, BH-26) was not made** — moved to the run of the rebuilt 1.2.0 (the maintainer, chat 2026-10-10). The timeline these findings refer to is
`CAP-072-EVENT-NOTES.md` (prefixes A, B, Z, E1–E4, L, X, S, C as defined there; all times phone time).

- 🟢 **FACT** — directly observed in this capture (frame number, log line, film time or `file:line` given).
- 🟡 **HYPOTHESIS** — plausible reading, not yet independently confirmed; the settling experiment is named.
- ⚪ **ASSUMPTION** — not tested here.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-072` · **Date:** 2026-10-09, phone 17:27:26–18:23:42 (camera overlay 17:27:26–18:23:39) · **Firmware:** 🟢 `release_5.203` (31 of 31
announcements, 93 entries; Info tab on S) · **Phone:** Pixel 9a, GrapheneOS, Android 17 `CP3A.261005.005` (newer than `CAP-071`'s `CP3A.260905.009`), **secondary
user 10, no Google Play** · **App under test:** OpenControl **1.2.0, build `ec6d163`** (release-signed, versionCode 10200), installed over **1.1.1, build
`86a6fb3`** · **HCI logs:** A `CAP-072-btsnoop_hci2.log.last` (21,861 packets, 15:40:29.398–18:18:33.356), B `CAP-072-btsnoop_hci2.log` (1,158, 18:18:45.641–
18:23:12.322), Z `CAP-072-btsnoop_hci1.log.last` (36,083, 2026-10-08 21:47:46–15:40:25, before the run); `CAP-072-btsnoop_hci1.log` = the first 1,244,736 bytes of
A, not counted · **Buds:** `04:00:6e:…:07`, classic handle `0x000b` in all ACLs; no LE link to the Buds.

Commands used throughout (rule 4a; all on handle `0x000b`, shown matching the Connection Complete events A 5491, 9506, 13104, 16151, 20735 and B 159 —
`tshark -r <log> -Y "bthci_evt.code==0x03 || bthci_evt.code==0x05 || bthci_evt.code==0x3e" -T fields -e frame.number -e frame.time -e bthci_evt.code -e
bthci_evt.bd_addr -e bthci_evt.connection_handle -e bthci_evt.status -e bthci_evt.reason`, exit 0): the RFCOMM inventory `tshark -r <log> -Y
"bthci_acl.chandle==0x000b && btrfcomm" -T fields -e frame.number -e frame.time_epoch -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type -e btrfcomm.len -e
data.data` fed to a scratch parser (pw_hdlc split on `0x7e`, unescape, `zlib.crc32` over address + control + payload against the trailing 4 bytes little-endian, per
DLCI and direction, the RpcPacket read with `scripts/pwrpc_decode.py`'s own `parse`/`describe`; the Message Stream as `[Group][Code][Len:2 BE][Value]`). The decoder's
positive control: `python3 -I scripts/pwrpc_decode.py <CAP-036 log>` decodes `CAP-036` 1415 `REQUEST … GetHardwareInfo` and 1423 `RESPONSE … 7:{1:… 2:… 3:…}`
(exit 0). **CRC-32 OK on all 1,517 pw_hdlc frames** (Z 275, A 1,138, B 104; 0 `crc=BAD`, 0 unparsed). Scripts and outputs are in the session scratchpad
(`ai-sessions/0083` RESULT).

## 0. Build, install, user, logs, films and privacy (🟢 FACT unless marked)

- **Build on film:** before the update the Info tab reads **"App: 1.1.1, build 86a6fb3 (2026-10-08)"** (S 17:28:0x), after it **"App: 1.2.0, build ec6d163
  (2026-10-09)"** (S 17:32:2x and 18:11:2x), no "-dirty". L line 5 `package: io.github.tedsluis.opencontrolpixelbuds:10200, targetSdk 34`. `ec6d163` is the tip of
  `origin/main` and `origin/release/1.2.0` at this session's start, and `git diff --stat ec6d163..origin/release/1.2.0 -- android` prints nothing (exit 0).
- **The APK:** `dist/1.2.0/` (read only): `sha256sum -c` → `OK`; SHA-256 `a67adad1214daf163981ca7b2b72aa28fee45e7f344a7669345ea6a3b7ca44c8`; `apksigner verify
  --print-certs` (build-tools 36.0.0) → `a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb`. 🔴 **That the installed APK is this file is not proved**:
  the phone was not attached during the analysis, so no `pm path` + `adb pull` was made (`CAP-071` did). `aapt2 dump badging` against `~/opencontrol-1.1.1-tested`: the
  same five `uses-permission` lines (no `INTERNET`), `targetSdkVersion:'34'`, `compileSdkVersion='37'`, the same two `uses-library-not-required`; `aapt2 dump xmltree`:
  5 `activity`/`service`/`receiver`/`provider` elements in both. **B3 is not done:** `~/opencontrol-1.2.0-tested` does not exist.
- **The update path (U1/V1):** 🟢 `cmd package uninstall` of the app at 17:24:06 (X 817; `deletePackageX` for `user=-1`, X 818; PID 6994 killed, X 825) — the app
  then disappears from user 10 **and** user 0; 1.1.1 is newly installed into user 10 at 17:24:40–52 (X 1019, dex2oat `app-version-name:1.1.1`; `PACKAGE_ADDED
  replacing: false … uid 1010354`), started at 17:25:16 (PID 11717, X 1275); **1.2.0 is installed over it** at 17:29:12–25: `installer_clear_app_data_caller …
  39` twice (X 3257/3258 — 39 = `FLAG_STORAGE_DE|CE|EXTERNAL|FLAG_CLEAR_CODE_CACHE_ONLY`, the code cache only, as `CAP-071-FINDINGS.md` §0 traced in AOSP
  `IInstalld.aidl`/`InstallPackageHelper.java`), `Killing 11717 … killDueToPackageUpdate` (X 3295), `am_proc_start [10,12137,1010354,…,top-activity]` 17:29:25.119
  (X 3362). The shell log: `firstInstallTime` user 10 unchanged (17:24:41), `lastUpdateTime` 17:24:41 → 17:29:24, `versionCode` 10101 → 10200; the second
  `firstInstallTime=1970-01-01` is user 0, where the app is no longer installed. **So the update itself kept the data of a 1.1.1 that had lived five minutes.**
- **What was installed before 17:24:** 🟢 the uid-10353 app sent `GetHardwareInfo` (Z 11373 08:43:02, Z 20860 13:25:51, Z 26365 15:04:53, A 5667 17:19:38) — 1.1.1
  never does (`CAP-071`: 0) — and wrote `4:{28:0}`, `{27:0}`, `{28:1}`, `{27:1}` at 13:26:24–29 (Z 21478–21569). The maintainer's statement (chat 2026-10-10): it was
  the **`dist/1.2.0` release APK**. That explains the uninstall: 1.1.1 (10101) cannot be installed over 10200. 🟡 Settings made in that earlier install did not
  carry over (the uninstall deleted the data); none is needed for this run.
- **The settings survived the update (U1/V1):** 🟢 Dark mode **On** — set in the fresh 1.1.1 on film (S 17:28:2x); 1.2.0 started dark (S 17:29:25) and Settings showed
  "On" (S 17:32). 🟢 Debug mode — switched on in 1.1.1 on film (S 17:28:0x); on in 1.2.0 from its first second (E4 line 9 is a hex line) and on the switch (S
  17:33). 🟢 Permissions and the CDM association kept: E4 line 1 `Permissions (start): Bluetooth NOT_REQUESTED -> GRANTED, notifications NOT_REQUESTED -> GRANTED` is
  logged at every process start (`MainActivity.kt` `refreshPermissions("start")`), no permission dialog or Android picker appeared on S between 17:29 and 18:23,
  and the session opened by itself 1 s after the start (E4 4 "Automatic re-open … LINK_BACK"; ADR-044).
- **No Google Play in user 10:** P0 lists only `app.grapheneos.gmscompat.config`, `…lib`, `gmscompat`, `exit=0` — GrapheneOS's own compatibility-layer packages
  (`CAP-071-FINDINGS.md` §0 cites grapheneos.org/usage). The positive control and `am get-current-user` are not in the shell log; the commands ran (system log adbd
  lines 17:26:53/54).
- **No network from the app:** 🟢 no `INTERNET` permission (badging); the app's uid 1010354 appears in the system log only with Bluetooth socket and process lines.
- **Processes:** 6994 (uid 10353, user 10, a tile `bound-service` since 16:53:36) killed by the uninstall 17:24:06; 11717 (1.1.1) 17:25:16–17:29:24; **12137 (1.2.0)
  17:29:25 to the end — no crash, no ANR, no force-stop** (BH-25 is film 2). 🟢 No user-0 process this time (the app is not installed in user 0 after 17:24).
  `grep -c -i -E "FATAL|ANR in|am_crash|Decoder fault|Unexpected error|StrictMode"` over E4, L and X → 0 each (exit 1; positive control `grep -c "Settings read"`
  → 30 in E4).
- **The `SIGQUIT`s are bugreports:** 🟢 12137 "Signal Catcher … reacting to signal 3" at 18:16:33.038 and 18:21:56.577, each followed by "Wrote stack traces to
  tombstoned" (system log; L 648 is the second, in UTC) — during bugreport 1 (18:16:0x–18:18:18) and bugreport 2 (18:21:31–18:23:36), the way the HCI logs were
  pulled (`CAP-071-FINDINGS.md` §0).
- **Warnings from the app's process:** `W System : A resource failed to call close.` ×34 (12137, FinalizerDaemon tid 12147), after session losses — the same pattern
  as 1.1.0/1.1.1 (`CAP-071-FINDINGS.md` §0, 🟡 framework objects). `ActivityManager: Background started FGS: Allowed [callingPackage: io.github.tedsluis…]` with
  an `am_wtf` at each automatic re-open from LINK_BACK (X 8623/8628, 9241, 17779, 23980) — Android notes, and allows, the foreground-service start; 🔴 whether it was
  logged in `CAP-071` too (not checked).
- **Films and audio:** the camera film's audio stream is empty (`codec_name=unknown`, 0 channels; `ffmpeg -map 0:a -f null -` → "Decoding requested, but no decoder
  found for: none"); the screen recording's AAC track is the phone's microphone: the maintainer's speech (140 segments, not transcribed — no offline speech-to-text
  on this machine), the internet radio (from 17:54:59), the Buds' Find ring (18:12:28–18:13:09, the positive control) and the ringtone (18:02:25–31).
- **Privacy:** the films are committed **blurred** (the maintainer's choice, chat 2026-10-10): the screen recording full-frame at 1,625–1,656 s (browser history) and
  2,097–2,256 s (the call screen), the top 820 px at 1,324–1,362, 1,371–1,377, 2,286–2,296, 3,058–3,084, 3,372–3,377 s (Quick Settings with the SSID); the camera
  film's phone area (x 130–590, y 400–1,280) at the same ranges. Command (screen): `ffmpeg -i <screen> -filter_complex "[0:v]boxblur=40:6:enable='<full>'[a];
  [a]split[b][c];[c]crop=1080:820:0:0,boxblur=30:6[d];[b][d]overlay=0:0:enable='<qs>'[v]" -map "[v]" -map 0:a -c:v libx264 -preset veryfast -crf 23 -c:a copy`;
  camera: the same with `crop=460:880:130:400,boxblur=25:6` and overlay at 130:400, `-an` (the empty audio stream is dropped). The serials stay readable on the Info
  tab; the system log is committed as the extract `CAP-072-logcat-extract.txt`: `awk '$1=="10-09" && $2>="17:20:00" && $2<="18:25:00" && ($3=="6994"||
  $3=="11717"||$3=="12137"||$3=="27925"||$3=="22111"||/opencontrolpixelbuds/||/com\.android\.bluetooth[\/,:]/||/dumpstate/||/Telecom.*setCallState/||
  /ImsPhoneCallTracker.*updatePhoneState/||/bugreportz/)' CAP-072-logcat-all.txt | grep -v -i -E "<account e-mail>|@gmail|<SSID>|OpenControlBuds: DLCI" >
  CAP-072-logcat-extract.txt` → 41,178 lines; the same four patterns plus the first serial's hex dump (the 10-byte prefix of its 14 characters, written here only as `<serial-hex>`) → 0 in the extract (exit 1), while the full log
  has e-mail 4, SSID 100, hex 29. Telecom masks the caller (`handle=tel:************`). **The originals** (camera `9d72f8a8…c43c`, screen `95350562…479f`, full system log `4a42613d…5fc25`, SHA-256) are kept off the repository
  (`~/opencontrol-capture-originals/CAP-072/`, copied and checksum-verified 2026-10-10); `CAP-072-sha256sum.txt` lists the committed files.
- **HCI files:** A = `CAP-072-btsnoop_hci1.log` + 134 packets (`cmp -n 1244736` exit 0). Boundaries: the user switch 0 → 10 (Z ends 15:40:25.818, A starts
  15:40:29.398) and BH-22's Bluetooth off/on (A ends 18:18:33.356, B starts 18:18:45.641); no Buds ACL spans either gap. 18:23:12–18:23:42 is in no HCI log.

## 1. ACLs, the Message Stream and the Definition-of-done negative

| ACL (A/B) | Start | End | Note |
|---|---|---|---|
| A1 | A 5491 17:19:37.835 (page) | A 9492 17:38:29.167 `0x13` | the release APK's session 17:19, 1.1.1 17:25, 1.2.0 from 17:29; both buds into the case |
| A2 | A 9506 17:38:49.328 (the Buds) | A 13098 17:51:03.248 `0x13` | both into the case |
| A3 | A 13104 17:51:08.688 (the Buds) | A 16141 17:59:23.707 `0x13` | both into the case (BH-14) |
| A4 | A 16151 17:59:28.676 (the Buds) | A 20717 18:13:15.936 `0x13` | both into the case (BH-20) |
| A5 | A 20735 18:13:34.912 (the Buds) | A 21856 18:18:32.926 `0x16` | Bluetooth off |
| B1 | B 159 18:18:47.324 | open at B's end | after Bluetooth on |

- 🟢 **No Play-services claim during the run:** `tshark -r <log> -Y 'btrfcomm && frame contains 03:08:00:02:01:25' | wc -l` → **0** in A, `…hci1.log` and B (exit 0
  each); positive control `CAP-066-btsnoop_hci.log.last` → 22 (exit 0). **Z holds 9** — Z is the Owner user's day before 15:40 (user 0 has Play), not the run.
- 🟢 **52 Message Stream claims in the run (A 49 from 17:29:26, B 3; A has 3 more before the update), all the app's:** each phone `SABM` on DLCI 4 or 5 is followed by the app's `08 11 00 00` (49) or a
  Ring/Stop `04 01 00 01 xx` (3: A 20379, 20510, 20648) as its first message; released ≈ 1.5 s after the last answer (`DISC` by the phone, e.g. A 11947
  17:44:34.090), except two cut by the Buds' `DISC` (A 21181 18:13:38.136, B 827 18:19:41.647).
- 🟢 **No other app on the Buds' RFCOMM:** DLCIs on `0x000b` in A/B are 0, 2/3 (MAESTRO), 4/5 (Message Stream), 8/9 (HFP, opened by the Buds: `SABM` B>P A 5779,
  9877, 13327, 16538, 20958; `AT+BRSF=921`) and 12 in B (HFP, opened by the phone); nothing on DLCI 0x0a/0x0b. Z has DLCI 8/10 opened by the phone in the morning
  (Owner user, the Google app) — not the run.
- 🟢 **ANC `Notify` Settable** (the third value byte, `01 e8 [S] mode`): `00` in 18 claims (both buds in the case, in the hand or on the table — §4; at 17:51:10 followed by `e8` 63 ms later), `e8` in the other 31 claims with a `Notify` (43 `e8` messages).

## 2. The Connect read — thirteen requests (BH-1, BH-18, BH-20, BH-22; V1, V2, V16, V17; ADR-058)

🟢 **31 sessions** (A 28 from 17:29:26, B 3; plus the release APK's at 17:19 and 1.1.1's at 17:25): the Buds' unsolicited `GetSoftwareInfo` → `ReadSetting 4:16` →
`ReadSetting` **2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29** → one `SubscribeRuntimeInfo` → **one `GetHardwareInfo`**, in that order, in **30 of 30 complete
sessions**; the session of 17:41:08 (A 10667) ended after the eighth read (the Buds' `DISC` A 10728, 1.3 s after its start), so it has neither. Every read and every
`GetHardwareInfo` has its OK `RESPONSE`; no packet carries a status other than OK. Command: the scratch session pairing over the `RPC` lines above.

- 🟢 **`GetHardwareInfo` = its reference, byte for byte:** on channel 21 `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 e3 a5 ec 28 f9 67 61 b5 7e` (e.g. A 7593 17:29:27.052)
  = `CAP-036` 1415; on channel 19 `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 e3 a5 ec 28 ff 1e bb b7 7e` (e.g. A 8616) = `CAP-024` 801. **30 requests, 29 answers, no
  retry, never a second one in a session, none outside Connect/re-open**: the request of 18:13:38.030 (A 21175) was cut by the Buds' `DISC` 104 ms later (A 21179) and
  not retried; the next session read the serials (18:15:21). `grep -c "GetHardwareInfo status=OK"` → 29 in E4 (= the 29 answers); the release APK's earlier
  requests in Z / A 5667 are not this process. Latency request → answer **35–173 ms** over the 29 answers (e.g. A 7593 → 7596 17:29:27.052 → .087). It is the last request, so it delays nothing the screen shows: "Settings read" precedes it in every session (E4).
- 🟢 **The answer:** `RESPONSE … 1:6 2:17 5:9 6:7 7:{1:0x35373037…3531 2:0x35373038…3039 3:0x35373037…3437} 8:{…}` — field 7's three
  length-14 ASCII strings `5707…51`, `5708…09`, `5707…47` (redacted per ADR-058), **identical in all 30 answers** and identical to `CAP-036` 1423 / `CAP-024` 832.
  On the Info tab in the order **Case `5707…51`, Right bud `5708…09`, Left bud `5707…47`** (S 17:32:2x–17:33:0x) with "(from the Buds, 17:31:29)" = A 7863's time
  17:31:29.668 (the answer of the 17:31:28 Connect), and again "(18:11:20)" after BH-18 (A 19934 → answer 18:11:20.98). While a re-open waited for the answer the Info
  tab said **"Serial numbers: Not read from the Buds yet"** (S s2680, 18:12:06 — between the announcement A 20066 and the answer at 18:12:07.02).
- 🟢 **S6 (the first second):** after the Connect tap at 17:31:28.46 Controls showed "—" for Use touch controls and Use head gestures (S s242, 17:31:28.x), the values
  one second later (s243) — the reads 4 and 29 answered at 17:31:28.9 and 17:31:29.6. The same "—" during every re-open on Settings (s508–509) and Controls (s3132).
- 🟢 **BH-18 read-back** (A 19856 18:11:20 …): `16:[-3.5,0.5,1.0,-1.0,6.0]`, `2:1`, `4:1`, `7:{1:{4:{1:5}} 2:{4:{1:5}}}`, `11:1`, `12:{1:1 2:0 3:1 4:1}`, `15:1`, `17:0`,
  `19:0`, `22:1`, `27:1`, `28:1`, `29:2` — each the last write (§3); the screens showed them (S s2645–2659).

## 3. Every write — byte for byte

🟢 **60 `WriteSetting` requests, all answered by the empty `RESPONSE`** (`7e 80 a3 03 08 01 10 13 1d ea 71 de 7d 5e 25 1d 9a 8c 9e …` on channel 19), none with an error
status, none unanswered; **5 ANC `Set`s → 5 ACKs, 0 NAK; 4 Ring/Stop → 4 ACKs.** All writes were on **channel 19** (the sessions that took writes were all on 19).
Each E4 "Setting N written" line follows the export's own `pw_rpc RESPONSE … WriteSetting` line by 0–11 ms (57 of 57; e.g. A 14585 17:55:50.093 → E4 911 17:55:50.101). Requests against the references (the bytes
after `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 1d 9a 8c 9e`):

| Write | Frames (request → answer) | Bytes | Reference | Equal |
|---|---|---|---|---|
| Earbuds replaced off / on `28:0` / `28:1` | A 14583 → 14585, 15945; A 15020 → 15026, 17479 | `2a052203e00100d2b7cefd7e` / `…e001014487c98a7e` | `CAP-024` 1988 / `CAP-070` A1992 | ✓ |
| Other alerts off / on `27:0` / `27:1` | A 17519, 18708 / A 17608, 18761 | `2a052203d80100fa03b6d77e` / `…d801016c33b1a07e` | `CAP-024` 2053 / `CAP-070` A1995 | ✓ |
| Conversation off / on `22:0` / `22:1` | A 18921 / 18945 | channel-19 form | the codec's (no captured ch-19 frame in the reference table; `CAP-071` A3028/3031 were ch 21) — **first ch-19 capture** | — |
| EQ Balanced | A 19113 → 19115 | `4:{16:[-3.5,0.5,1.0,-1.0,2.5]}` on 19 | no ch-19 reference — first capture | — |
| Upper treble 4.66 / 6.0 | A 19133, 19178 | slider values | EQ codec | — |
| Balance ×30 (`17:11` … `17:0`) | A 19287 … 19510 | zigzag sint32 (11 = −6 "Right 6", 18 = +9 "Left 9", 105 = −53 "Right 53", 7 never sent) | `CAP-070` A3747 (`17:7`) not reached | — |
| Mono on / off | A 19520 / 19529 | `…980101…` / `…980100…` | `CAP-063` 5371 / 5261 (ch 19) | ✓ |
| Touch off / on | A 19573 / 19583 | `4:{4:0}` / `{4:1}` | `CAP-020` 1995 (fixture) | ✓ |
| Mode list `{1:1 2:0 3:1 4:1}` (Adaptive ticked) | A 19595, 19682, 19690 | `4:{12:{…}}` | codec | — |
| Left / Right hold assistant ↔ noise control | A 19622, 19639, 19643, 19659 | `4:{7:{1|2:{4:{1:6|5}}}}` | `CAP-063` 5654 / 5684 (Left) | ✓ (Left) |
| Head gestures off / on `29:1` / `29:2` | A 19716 / 19726 | `…e80101…` / `…e80102…` | `CAP-070` A3614 / A3685 | ✓ |
| Multipoint off / on | A 19764 / 19779 | `4:{11:0}` / `{11:1}` | `CAP-070` A3668 / A3679 | ✓ |
| In-ear off / on | B 980 / 983 | `4:{2:0}` / `{2:1}` | `CAP-056` 2173 / 4048 | ✓ |
| ANC `Set` NC / Off / NC (tile) / TRN (tile) / NC | A 11939, 12514, 12870, 13001, 19032 | `08 12 00 14 01 e8 e8 08` (`20`, `08`, `80`, `08`) + 16 × `00` | `CAP-068` 1146/1090/1039 | ✓ |
| Ring Left / Stop / Right / Stop | A 20393, 20524, 20540, 20666 | `04 01 00 01 02` / `00` / `01` / `00` | `CAP-068` 2142/2416, `CAP-062` 9660 | ✓ |

Command for an "Equal" check: `tshark -r <reference log> -Y "frame contains <bytes>" -T fields -e frame.number` (e.g. `frame contains 2a:05:22:03:e0:01:00:d2:b7:ce:fd`
→ `CAP-024` 1988). **Not written in this run:** Volume EQ (`15`), and the skeleton's "Right 4" (`17:7`) was never sent; the channel-21 forms were not exercised.

- 🟢 **Only after the answer:** each switch moved after its `RESPONSE` (e.g. Earbuds replaced off on S at 17:55:50.x, `RESPONSE` A 14585 17:55:50.093); each ANC mode
  after its ACK; each tap's claim sent `Get` first and its `Set` only after a `Notify` with Settable `e8`; 7 taps on Settable `00` sent no `Set` (§4).
- 🟢 **The Buds' end state** (B 954 18:20:06): EQ Balanced with Upper treble **6.0** (not plain Balanced), balance Centre, mono off, conversation detection on,
  Volume EQ on, touch on, both holds Noise control, the mode list **Noise cancellation + Transparency + Adaptive** (Off unticked — `TODO.md` §4 asked for "without Off"
  = `{1:1 2:0 3:1 4:0}`; Adaptive is now on as well), in-ear on, head gestures on, Multipoint on, both case sounds on.
- 🟢 **ANC during playback:** music played (AVDTP streaming, the internet radio) from 17:55:0x; the writes of 18:05–18:11 and the `Set` of 18:06:35 (ACK A 19034) ran
  while it played. That the modes *sounded* right is the maintainer's statement; the audio track (the phone's microphone) cannot hear inside the Buds.

## 4. The worn line (`INEAR-006`, ADR-059) and the channel choice (lead L-1, `INEAR-005`)

**Every Worn reading on S against the wire and the case** (Settable = the claim's `Notify` byte 3; L/R = runtime-info 6.2/6.3 field 2, 2 = charging):

| Reading on S (checked) | Settable · mode | L / R | Buds (C) | ADR-059 rule | Right? |
|---|---|---|---|---|---|
| "Both buds in the case" (17:29:27, 17:33:5x) | `00` OFF (A 7554) | 2 / 2 | both in the case | both charging | ✓ |
| "Not worn" (17:35:56) | `00` OFF (A 8590) | 1 / 2 | Left just out, in the hand (C 17:35:51–55) | Settable `00` | ✓ |
| "Probably worn" (17:36:31, 17:37:15, 17:37:26) | `e8` TRN | 1 / 2, 1 / 1 | Left in the ear, then Right too (head C 17:37:09–13) | non-zero | ✓ |
| "Probably worn" (17:38:22) | `e8` TRN (A 9418) | 1 / 2 | Right just into the case, Left worn | non-zero | ✓ |
| "Not worn" (17:38:50) | `00` OFF (A 9817) | 1 / 2 | both were in the case; Left just taken out | Settable `00` | ✓ |
| "Probably worn" (17:39:08, 17:39:31, 17:40:31) | `e8` TRN | 1 / 1 | both out, worn (no film of the head) | non-zero | 🟡 ✓ |
| "Not worn" (17:41:30) and 7 refused taps 17:41:57–17:43:26 | `00` OFF | 1 / 1 | **both on the table** (C 17:41:09–17:44:01) | Settable `00` | ✓ |
| "Probably worn" (17:44:32 … 17:50:24) | `e8` | 1 / 1 | worn (C: taken from the table 17:44:03–13) | non-zero | 🟡 ✓ |
| — (17:51:10: `00` then `e8` 63 ms later) | `00` OFF → `e8` TRN (A 13515/13519) | 2 / 1 → 1 / 1 | both being taken out of the case (C 17:51:07–17) | — | — |
| **"Probably worn" (17:51:42, 17:52:00)** | **`e8` TRN** (A 13749, 13853) | 1 / 1 | **both on the table since 17:51:19** (C 17:51:19–17:55:09) — 23 s and **42 s** | non-zero | **✗ vs the buds; ✓ vs the rule** |
| "Probably worn" (17:55:27 … 18:06:35) | `e8` | varies | worn / one in the case | non-zero | ✓ |
| "Not worn" (18:13:37, 18:15:21, 18:18:47) | `00` OFF | 1 / 1 | both on the table (C 18:13:37–18:19:33) | Settable `00` | ✓ |
| "Worn: —" during every re-open | — | — | — | no `Notify` this connection | ✓ |
| "Worn: unknown — in-ear detection is off" | not looked at | | in-ear off 18:20:09–13 (buds worn) | | **not observed** |

- 🟢 **No "Not worn" with a bud in an ear and no "Probably worn" with in-ear detection off** (heavy-defect criteria) — every `00` came with no bud worn (18 claims).
- 🟢 **The `CAP-064` case recurred, longer:** with in-ear detection on and both buds straight from the case to the table (17:51:13–19), the Buds reported Settable
  `e8` at **23 s and 42 s** (17:51:42.240, 17:52:00.861), so the app said "Probably worn (checked 17:52:00)". After the next claims (18:12:22, 18:13:37) with the buds
  on the table it was `00`. Not a fault of the app: the (i) states this limit. A result for ADR-049 item 3 / ADR-059 (a **proposal**, RESULT checkpoint): the
  ≈ 28 s of `CAP-064` is at least 42 s here; `CAP-065`/`CAP-066` read `00` within 2.6 s. 🔴 What makes the difference (the music state? AVRCP showed no play then; the
  order the buds left the case?).
- 🟢 **The Buds switch to OFF when no bud is worn, and back when worn:** every `00` claim reports mode OFF; the first `e8` claim after wearing reports TRN or NC
  (17:35:56 OFF → 17:36:31 TRN; 18:12:06 TRN → 18:12:22 OFF on the table). The app shows these as "read" (correct per `AncModeCause`).
- **Lead L-1 and the channel choice** (`PROTOCOL.md` §2.2a):

| Announcement | Ch | State (C; runtime info; the Buds' AVRCP PAUSE) | Rule |
|---|---|---|---|
| A 8538 17:35:55, A 9371 17:38:21, A 9776 17:38:50 | 19 | **only the Left out** | 🟢 "only the Left out ⇒ 19" — 3 more samples |
| A 14653 17:56:07, 15109 17:57:02, 15505 17:58:08, 16033 17:59:09, 16423 17:59:29, 16879 18:00:17 | 21 | **only the Right out** (Left in the case, 6.2 = 2) | 🟢 "only the Right out ⇒ 21" (`CAP-065`) — 6 more samples |
| A 14616 17:56:05.370, 15067 17:57:00.616, 15457 17:58:06.155, 15987 17:59:06.992 (Buds `DISC`) | 19 → 21 | **both worn on 19, the Left taken out of the ear** (PAUSE 17:56:04.98 …), into the case 1–3 s later | 🟢 **lead L-1: the Buds close MAESTRO and re-announce 21 — 4 of 4** (head not in view; the ear removal from the PAUSE and C's bud entering the case) — promoted to 🟢 in `PROTOCOL.md` §2.2a (maintainer, chat 2026-10-10) |
| A 14794 17:56:17, 15269 17:57:27, 15690 17:58:24 (Buds `DISC`) | 21 → 19 | the Left back into an ear (Right worn) | 🟡 the Left becoming worn re-homes to 19 |
| others (both out / both docked) | 19 or 21 | — | not fixed |

## 5. "Changed by the Buds" and the press-and-hold observation (lead 5; BH-11; V10)

**The maintainer's observation:** a press-and-hold on a bud changed the ANC mode audibly, but the app never showed it.

**What the evidence shows (🟢 unless marked):**
- The press-and-holds are not on either film (the camera films the case; the screen shows the app). The ANC tab was on screen 17:44:3x–17:48:4x with its (i) opened
  4 times (S s1160, s1214, s1228, s1229 — always "ANC mode: ACTIVE (updated 17:44:32)", no "Changed by the Buds" line); speech at 17:45:28–17:46:31 and 17:48:01–11.
  🟡 The holds took place in this window (the maintainer's statement; the only stretch with the ANC tab up, worn buds and no tap).
- **No Message Stream claim was open from 17:44:34.090 (the app's `DISC`, A 11947) to 17:48:46.460 (`SABM`, A 12490).** In that window the Buds sent only
  runtime-info packets on DLCI 2 (A 11978 … 12426, nothing about ANC) and AVRCP: volume `CHANGED` 17:45:27, 17:45:55, 17:46:11, 17:46:30 and PLAY 17:46:22 (gestures on
  a bud). `tshark -r A -Y "bthci_acl.chandle==0x000b && btrfcomm.dlci in {4,5} && frame.time >= \"2026-10-09 17:44:35\" && frame.time <= \"2026-10-09 17:48:46\""` →
  0 frames (exit 0; the same filter over 17:44:30–17:44:35 matches the claim's frames A 11938–11949).
- The mode list at that time was **Noise cancellation + Transparency** (`4:{12:{1:1 2:0 3:1 4:0}}`, every read until 18:09), so a hold toggles NC ↔ TRN. The next
  claim (17:48:46, a tap on Off) read **NC** — the same as the app showed (A 12513). 🟡 An even number of holds, or the last hold back to NC.
- The (i) line **did** appear once: 17:51:10, when the automatic re-open's `Get` was answered `OFF` (`00`, A 13515) and the Buds, 63 ms later and unasked, reported
  `TRN` (`e8`, A 13519) as the buds were taken out of the case (C 17:51:07–17). A second unprovoked `Notify` (TRN → **NC**, B 825 18:19:41.644) came when the buds were
  put in (C 18:19:35–41), 3 ms before the Buds closed the session (B 826/827). Both are the Buds' own wear-related changes inside a claim window.
- **`BudsRepositoryImpl.kt`:** the claim is released `MESSAGE_STREAM_LINGER_MS = 1_500L` (`:1558`) after the action (`scheduleRelease`, `:1359–1368`); a claim is opened
  only by a tap, a pull, the tile or a Connect (`withMessageStream`, `:1315`; `launchInitialSnapshot`, `:1370`); `causeOfNotify` (`:356–362`) gives
  `CHANGED_BY_BUDS` only to a `Notify` with nothing waiting and a mode other than the shown one — which needs an open channel to arrive at all.

**The hypotheses, weighed:**
- (a) **The channel was closed — by design (ADR-032).** 🟢 for the window 17:44:34–17:48:46: no RFCOMM channel existed for a `Notify` to arrive on. ADR-032 item 5
  ("values from DLCI 0x04 are only as fresh as the last claim") is exactly this. In a user **without** Play services nobody else holds DLCI 0x04 between claims, so a
  hold's `Notify` has no recipient. **The feature can only show a change that happens within ≈ 1.5 s of an app action** (or during a Connect snapshot).
  `ai-sessions/0082` §A.1 designed and tested it against `CAP-045`'s `Notify`s, which were captured with the official app holding the channel; the skeleton's
  BH-11 expectation was not achievable on this phone — a **planning gap**, not a codec or Buds defect.
- (b) A `Notify` arrived and was misclassed — 🟢 refuted for this run: the two unprovoked `Notify`s that arrived were classed `CHANGED_BY_BUDS` (S s1425) or overtaken by
  the session's end.
- (c) The UI did not recompose — 🟢 refuted: the line appeared on S at 17:51:10.
- (d) The next claim showed the new mode as "read" — 🟢 the mechanism is right (e.g. 18:12:06 read TRN after NC), but in the hold window the next claim read the same mode.
- (e) The Buds send no `Notify` without a client — 🟡 cannot be told apart from (a) by this run (no hold fell inside an open window). `ADR-046`'s Update records six holds
  with `Notify`s on the wire in `CAP-064` (9737–9793), when a client (🟡 Play services in the Owner user) held the channel — consistent with (a).
- (f) The tile: 🟢 user 10's process answered both tile taps (E4 684, 696); a hold during a tile claim did not happen.

**Answer:** 🟢 the app did not show the press-and-holds because, as designed by ADR-032, it holds no Message Stream channel between actions, and in this user no other
client holds it either; the Buds' report of the change has nowhere to go. 1.2.0's "Changed by the Buds" works (17:51:10) but only for a change inside a claim. The
`CHANGELOG.md` / release-notes wording ("when a bud's press-and-hold or the Buds themselves changed the mode") promises more than the design can deliver. Options for
the maintainer (RESULT checkpoint, proposals only).

## 6. The case-sound observation (lead 7; BH-14; V13; `CASE-001`, `CASE-002`)

**The maintainer's observation:** with "Earbuds replaced" the case sometimes sounded when a bud went back although the switch was off, and sometimes not although it
was on; the maintainer's explanation: the connection between the phone and the case drops for a moment when a bud goes from the ear into the case.

**Every bud-into-case event** (C at 5 fps; the switch state = the last ACKed `4:{28:x}`; the tone = a short (≈ 0.1–0.15 s) two-tone at ≈ 1.4 and ≈ 1.7 kHz on the
screen recording's track at the bud's seating, read from narrow-band spectrograms 1.25–1.85 kHz ±3 s aligned with the camera — 🟡 a visual reading at the noise floor,
by one reviewer):

| # | Phone time | Bud, from | Earbuds replaced (since) | Session at that second | Tone at seating |
|---|---|---|---|---|---|
| E1 | 17:38:21 | Right, ear | on (read 17:38:23) | Buds `DISC` 17:38:19.08, re-open 19 | **yes** (+2 s, at the LED) |
| E2 | 17:38:28 | Left, ear (both in) | on | ch 19, ACL dropped 17:38:29.17 | faint |
| E3 | 17:50:57 | Right, ear | on | ch 21 open | faint |
| E4 | 17:51:02 | Left, ear (both in) | on | ACL dropped 17:51:03.25 | no |
| E5 | 17:56:08 | Left, ear | **off** (17:55:50, 18 s) | Buds `DISC` 17:56:05.37 → re-open 21 at 17:56:07 | faint? |
| E6 | 17:56:36 | Right, ear | off | ch 19 open | no |
| E7 | 17:57:04 | Left, ear | **on** (17:56:50, 14 s) | `DISC` 17:57:00.62 → 21 | no |
| E8 | 17:57:20 | Left, hand | on | ch 21 open | no |
| E9 | 17:57:38 | Right, ear | on | ch 19 open | **yes** (clear) |
| E10 | 17:58:08 | Left, ear | on | `DISC` 17:58:06.16 → 21 | no |
| E11 | 17:58:46 | Right, ear | on | ch 19 open | faint |
| E12 | 17:59:08 | Left, ear | **off** (17:59:02, 6 s) | `DISC` 17:59:06.99 → 21 | no |
| E13 | 17:59:18 | Right, ear (both in) | off | ch 21; ACL dropped 17:59:23.71 | no |
| E14 | 18:00:06 | Left, ear | off | ch 21 open | no |
| E15 | 18:00:44 | Left, ear | off | no session (skipped re-open) | no |
| E16 | 18:01:02 | Right, ear | off | none (Connect at 18:01:47) | no |
| E17 | 18:02:28 | Right, hand | **on** (18:01:53, 35 s) | ch 19 open; the phone ringing | not decidable (ringtone) |
| E18 | 18:13:14 | both, table | on | ch 21 open; ACL dropped 18:13:15.94 | faint |

- 🟢 **Every write was acknowledged long before the next entry** (6–35 s; `RESPONSE` A 14585, 15026, 15952, 17481), and **every later read returned the written value**
  — including the reads by the *other* bud right after a Left entry (17:56:08 `28:0` on ch 21, A 14744; 17:57:04 `28:1`, A 15189; 17:59:10 `28:0`, A 16113). So both
  buds hold the setting at once.
- **The hypotheses:**
  - (a) A link loss between the phone and the case: 🟢 there is no phone ↔ case link on the wire (the ACL is to the Buds; the case appears only through the buds'
    runtime info). What does drop at a Left entry is the MAESTRO session (the Buds' `DISC`, 4 of 4 — §4), not the ACL, and the written value survives it (the reads
    above). A dropped session cannot undo an ACKed write. **Refuted as stated**; the Buds' session drop is real but harmless to the setting.
  - (b) The write not acknowledged / sent on a closing channel: 🟢 refuted (all ACKed, 6–35 s before).
  - (c) Field 28 does not mean "a chime at every return": 🟡 **supported** — with the switch on, a tone was found at 2 of 2 clear Right entries (E1, E9) and faint at
    E3, E11, but at none of the five Left entries (E4, E7, E8, E10 and E2 faint); with it off, at none of 7 except a faint candidate at E5. 🟡 The chime may depend on
    which bud is replaced, on the lid, on both buds, or play from the bud rather than the case.
  - (d) Another sound (field 27, a charging or low-battery sound): 🔴 not distinguishable here; "Other alerts" was on throughout the entries.
  - (e) A stale switch on screen: 🟢 refuted — the switch on S matched every read (e.g. S s1722 "off" after the 17:56:08 re-open read `28:0`).
  - (f) Audibility: 🟢 the tone is at the microphone's noise floor; speech and the radio cover several entries (E2, E5, E17). The maintainer was closer than the phone's
    microphone; what was heard can differ from what was recorded.
- **Answer:** 🟡 the app wrote and the Buds stored "Earbuds replaced" correctly every time; the variable sound is the Buds'/case's own behaviour, possibly bud-specific
  (Right yes, Left no in this run). The maintainer's explanation (a phone ↔ case link loss) does not fit the wire. 🔴 What exactly the switch silences stays open.
- **Proposed experiment** (no skeleton in this session): the official app, Earbuds replaced on/off, each bud separately and both, from the ear and from the hand, lid
  open, 30 s between entries, music off, a phone microphone **at the case** (or a second recorder), three repetitions per state; then OpenControl the same. Settles (c),
  (d) and the bud dependence.

## 7. Session ends, re-opens and processes (BH-20 … BH-22; C8, C9, J4, K1, K2, R6; V18)

🟢 **28 session losses** (E4 "Session lost" ×28) plus 2 Disconnect taps (17:31:08, 18:11:18) and the Bluetooth off (18:18:32): 22 Buds-side `DISC`s with the ACL up
(cause "the Buds closed the channel"), 4 with the ACL dropped by the Buds (17:38:29, 17:51:03, 17:59:23, 18:13:15 — both buds into the case;
cause "Android's link to the Buds went down"), 1 while the app was hidden (17:55:15), 1 Bluetooth off (final at once). Every automatic re-open after a loss came **1.498–1.503 s after
"Ready -> Disconnected"** (AFTER_LOSS, 15 times, E4; `SessionReopener.kt` `LOSS_REOPEN_DELAY_MS = 1_500L`) or when Android's link came back (LINK_BACK, 8 times, the first attempt failing on
the multiplexer 4 times and the 400-ms retry succeeding — as `CAP-070`/`071`); the 10-s chain guard skipped 8 re-opens (losses 1.25–9.9 s after a re-open), after
which a Connect tap followed (17:41:28, 17:51:40, 18:00:16, 18:01:47, 18:12:22, 18:20:03). 🟢 No session stayed lost without the documented reason.

- 🟢 **BH-20 (both into the case and out):** the ACL dropped (`0x13`), "Paired — not connected to this phone / Open the case or put the Buds in your ears …" (S
  s665), the Buds' own ACL came back when the buds left the case (A 9506, 13104, 16151, 20735) and the app re-opened by itself 0.27–0.52 s later — **C9 "ready again by
  itself" ×4**.
- 🟢 **BH-21:** Home 18:13:44–18:15:20 (1 min 36 s), the session was lost at 18:13:38 just before; on return LINK_BACK re-opened it (E4 2069–2119).
- 🟢 **BH-22:** export E1 at 18:15:3x before the off; "Bluetooth adapter: ON -> TURNING_OFF" 18:18:32.433, cause "Bluetooth was switched off on this phone" (final);
  on 18:18:45.967 → the Buds' link 18:18:47.3 → automatic re-open, thirteen requests, serials 18:18:48 (E4 2135–2197). "Bluetooth is disabled." is not on S (Android's
  Bluetooth dialog covered the app). Info's "Not read …" while off: not looked at.
- **The exports:** E1 (18:15:3x, 2,121 lines) before the off; E2 (18:19:01) after the on; E3 (18:21:10) after the rotation; E4 (18:23:3x). 🟢 **No serial and no MAC
  as text** in any export or in L: `grep -c "<serial>"` for each of the three full strings and `grep -c -i "04:00:6e:cf:6e:07"` → 0 each (exit 1); positive control
  `grep -c "GetHardwareInfo status=OK"` → 26/27/29/29. 🟢 **But the `GetHardwareInfo` answer is in the Debug-mode hex dumps** (`grep -c "<the first serial as spaced hex>"`
  → 26/27/29/29): ADR-058 item 5 allows the raw hex dump in Debug mode only; the skeleton's BH-22 "Refuted if: a serial in the export" does not say "as text". The
  maintainer's answer (chat 2026-10-10, "Hex serials"): accepted per ADR-058 item 5 — BH-22's check means "as text"; ADR-058 Update of 2026-10-10.
- 🟢 Each export's last line is earlier than its save time by the time the user spent in Android's save dialog (E1: last line 18:15:24.7, saved 18:15:3x; its own
  "Debug log exported (2121 lines)" is in E2 at 18:15:36.690).

## 8. The incoming call (lead 9; not in the plan)

🟢 RINGING 18:02:25.507 (X 27124), `setCallState NEW → RINGING` 18:02:26.469 (X 27127), ANSWERED 18:02:31.435 (X 27300), ACTIVE 18:02:31.676, IDLE 18:04:59.003,
DISCONNECTED 18:04:59.029 — **2 min 28 s, answered on the phone's screen** (S s2102–2106). The audio went to the Buds: **eSCO** Synchronous Connection Complete A 17689
18:02:26.903 (handle `0x0006`, the Buds' address) → Disconnection Complete A 18634 18:05:01.552 (`0x16`). The app's session (ch 19 since 18:01:48) **stayed open
through the call**; no claim, write or loss during it; the next write 18:05:10 worked. Do Not Disturb was not on (P7 ★; the Modes tile inactive on S 17:49). The caller's
number is on S (blurred in the committed copy) and masked in the system log (`tel:************`); it is in no document.

## 9. Battery and case (lead 15)

🟢 The Case 57 % (17:29:27, runtime info 6.1 = 57, A 7596) → 56 % (from A 9482 17:38:24); the battery card showed each with its time; Left/Right 100 % with ⚡ while in
the case (6.2/6.3 = 2) and without when out; Case "last seen" (dimmed, with the dot) whenever no bud charged (S s577). 🟢 6.1 present exactly when at least one bud
reports field 2 = 2 in every runtime-info packet of the run (scratch check over all `SERVER_STREAM` packets of A/B).

## 10. Re-test verdicts, the five 1.2.0 items and the release classification

**The five 1.2.0 items:**
1. **"Changed by the Buds"** — 🟢 works as built (shown at 17:51:10, never after the app's own tap, pull or tile: BH-12 ✓); 🟢 **cannot show a press-and-hold made
   between claims** (§5) — partly.
2. **Serial numbers (ADR-058)** — 🟢 works: 30 requests byte-identical, once per session, last, 29 answered (one cut by the Buds' `DISC`, not retried); labels and order as specified; identical in every answer.
3. **"Probably worn" (ADR-059)** — 🟢 works as specified: every reading follows the rule; the only mismatch with reality is the Buds' own `e8` on a table (≥ 42 s),
   the documented limit.
4. **Case sounds on gear → Settings** — 🟢 works: card, (i), writes byte-identical, reads back; not on Controls.
5. **Conversation detection on Controls** — 🟢 works: card after In-ear detection, writes OK (first channel-19 capture), not on Sound.

**The skeleton's "Refuted if", applied literally:** BH-1 — no crash, not light, no read missing or out of order, no second `GetHardwareInfo`, no other request →
not refuted. BH-2 — not refuted (build `ec6d163`, no "-dirty", serials Case/Right/Left). BH-3 — not refuted. BH-5/6 — not refuted. BH-7 — not refuted (`DISC` then 21,
4 of 4). BH-8 — not refuted (no `Set` on `00`; "Probably worn" on the table only in BH-13's case). BH-9 — not checked (the Worn line not read). BH-10 — not refuted.
**BH-11 — refuted as written** ("no mode change on screen … the line missing"): the cause is the design (§5), not a dropped `Notify`. BH-12 — not refuted.
BH-13 — **"Probably worn" still at 42 s** (a result, not a fault). BH-14 — not refuted. BH-15 — not refuted. BH-16 — no differing request; Volume EQ and H5 not done.
BH-17 — "the mode list keeps Off": no (Off unticked), but Adaptive was added. BH-18/19/20/21 — not refuted. BH-22 — "a serial in the export": **not as text; as hex in
the Debug-mode dump**, accepted per ADR-058 item 5 (the maintainer, chat 2026-10-10). BH-23 — not identifiable. BH-24 — rotation not during the dialog. BH-25/26 — **not made** (moved to the rebuild's run).

| # | What | Class | Evidence |
|---|---|---|---|
| 1 | Press-and-hold changes are never shown: no claim open between actions (ADR-032); the feature and its wording promise more | **design limit / wording** — the maintainer's choice: release with reworded text, or fix first | §5 |
| 2 | Serial strings in the Debug-mode hex dump of the exports | by ADR-058 item 5 (Debug only); BH-22's wording | §7 |
| 3 | "Probably worn" with both buds on the table for ≥ 42 s | known issue (already in the (i) and the release notes), now longer | §4 |
| 4 | Case chime varies (Right yes, Left no in this run), the app's writes and reads correct | known issue (the app says what the switch sets) | §6 |
| 5 | Volume EQ, *Read EQ again* (H5), "Right 4", the Worn line with in-ear off, C12, T11 not done; S12 differently | open test items (the rebuilt 1.2.0's run) | EVENT-NOTES |
| 6 | Balance slider: ≈ 30 writes and labels up to "Right 53" to reach a small value | UX observation (each write correct and ACKed) | §3 |
| 7 | Buds end with Adaptive in the mode list and EQ Upper treble 6.0 | `TODO.md` §4 restore | §3 |
| 8 | The `SIGQUIT`s | the computer's `bugreportz` | §0 |
| 9 | The release APK used before the run; 1.1.1 reinstalled 5 min before the update | test-procedure deviation | §0 |

**No heavy defect:** no crash or ANR; 60 of 60 writes, 5 of 5 ANC `Set`s and 4 of 4 Ring/Stop answered OK/ACKed, each byte-identical to its reference where one exists;
no write applied before its answer; 30 `GetHardwareInfo`, never retried or outside Connect; no Safe Mode on `release_5.203`; every session end recovered or explained;
no setting lost by the update; no "Not worn" with a bud worn, no "Probably worn" with in-ear off; no "Changed by the Buds" after the app's own action; nothing from the
app on DLCI 0x08/0x0a.

**Verdict (the maintainer, chat 2026-10-10, `AskUserQuestion` "Verdict", option *"Fix first"*; "Hold result", option *"Fix first: hold 0x04 on ANC tab"*):**
**fix first** — 1.2.0 is rebuilt with `DECISIONS.md` ADR-061 (the Message Stream claim held while the noise-control tab is on screen) before release; nothing is
published; the rebuild gets a targeted hardware run that also takes this run's film-2 items (T11, the force-stop). Known issues of `CHANGELOG.md` `[1.2.0]` are
not changed now (the maintainer: "No CHANGELOG change now"; the FEATURE session updates it).

## 11. 1.1.1 → 1.2.0: did anything else change? (lead 12)

| Aspect | 1.1.1 (`CAP-071`, and A 6827 here) | 1.2.0 (this run) | Same? |
|---|---|---|---|
| Connect read | 16, then 2 … 29, one `SubscribeRuntimeInfo` | the same + one `GetHardwareInfo` last | ✓ + the planned request |
| Request bytes | the reference table | byte-identical where compared (§3) | ✓ |
| Message Stream claim | `08 11` first; released ≈ 1.5 s after | the same (52 claims) | ✓ |
| ADR-044 timing | +1.5 s, chain guard 10 s | +1.498–1.503 s, 8 skips | ✓ |
| Screens | `CAP-071`'s film | the same cards, plus the five 1.2.0 items | ✓ |
| Warnings | "A resource failed to call close" | the same; plus `am_wtf` "Background started FGS: Allowed" at LINK_BACK re-opens (🔴 not checked in `CAP-071`) | ✓ / 🔴 |

🟢 No behaviour change outside the five items was found. The OS update (`CP3A.260905.009` → `CP3A.261005.005`) shows no Bluetooth difference in this run; this run
cannot separate an OS effect from the app (both changed) for anything subtler.

## 12. Improvements (proposals only — nothing under `android/` was changed)

1. **Press-and-hold (§5):** `DECISIONS.md` ADR-061 (accepted 2026-10-10): hold DLCI 0x04 while the noise-control tab is visible. `BudsRepositoryImpl.withMessageStream`/`scheduleRelease` (`:1315`, `:1359`) gain a tab-scoped hold (opened on entering the tab, released 1.5 s after leaving it or the app); `AncScreen`/`MainActivity` report the tab's visibility. Unit test: the `CAP-045` 1583 unprovoked `Notify` fixture arriving during a hold with no `Get`/`Set` pending → `CHANGED_BY_BUDS`; a release after leaving the tab. Re-test: tab open, hold a bud → the (i) line; leave → the phone's `DISC` ≤ 1.5 s.
2. **Fixture swap (`TODO.md` §5):** `HardwareInfoFixtures` → this capture's real answer bytes, A 7596 (ch 21) and A 8620 (ch 19), serials X-ed out the same way.
3. **Balance slider:** a finer step or tap targets around the centre (UX; a FEATURE question).
4. **Test procedure:** the shorter run proposal (RESULT).

## 13. Open questions

- 🔴 Which sounds field 28 silences, and whether per bud (§6).
- 🔴 Why the Buds kept `e8` ≥ 42 s on the table here and < 3 s in `CAP-065`/`066` (§4).
- 🔴 Whether the installed APK equals `dist/1.2.0` (no pull).
- 🟡 Holds in 17:44:34–17:48:46 (the maintainer's statement; not filmed).
- Not made (moved to the rebuilt 1.2.0's run): BH-25 (force-stop, the three `uiautomator` dumps, T11), BH-26.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-072-2026-10-09_17-27-26_18-23-39-Group_BH/CAP-072-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-072-2026-10-09_17-27-26_18-23-39-Group_BH/CAP-072-FINDINGS
