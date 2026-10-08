# Findings: `CAP-071` (Group BG — the release-signed 1.1.1 APK installed over 1.1.0 in a GrapheneOS secondary user without Google Play: the regression run of the toolchain release)

Standardized, evidence-based extraction from the four HCI snoop logs, the camera film with sound (`CAP-071-recording.mp4`), the two debug exports, the app
logcat and the system log, per `ai-sessions/0080`. There is no screen recording, no second film and no `uiautomator` dump. The timeline these findings refer to
is `CAP-071-EVENT-NOTES.md` (prefixes "Z", "A", "B", E1/E2, L, X as defined there; phone time = film overlay + 1.25 s).

- 🟢 **FACT** — directly observed in this capture (frame number, log line, film time or `file:line` given).
- 🟡 **HYPOTHESIS** — plausible reading, not yet independently confirmed; the settling experiment is named.
- ⚪ **ASSUMPTION** — not tested here.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-071` · **Date:** 2026-10-08, film overlay 17:51:29–18:27:28 (phone = overlay + 1.25 s ± 0.1 s) · **Firmware:** 🟢 `release_5.203` (11 of 11
announcements, 33 entries; Info tab on film) · **Phone:** Pixel 9a, GrapheneOS, Android 17 `CP3A.260905.009`, **secondary user 10 (`full.secondary`), no Google
Play** · **App under test:** OpenControl for Pixel Buds Pro 2 **1.1.1, build `86a6fb3`** (release-signed, versionCode 10101), installed over **1.1.0, build
`0323849`** · **HCI logs:** Z `CAP-071-btsnoop_hci1.log.last` (854 packets, 17:37:32.758–17:51:34.615, before the film), A `CAP-071-btsnoop_hci2.log.last` (6,996,
17:51:39.365–18:26:23.881), B `CAP-071-btsnoop_hci2.log` (760, 18:26:32.311–18:28:44.260); `CAP-071-btsnoop_hci1.log` = the first 319,218 bytes of A, not counted ·
**Buds:** `04:00:6e:…:07`, classic handle `0x000b` in all 7 ACLs; no LE link to the Buds.

Commands used throughout (rule 4a; all on handle `0x000b`, shown matching the Connection Complete events Z187, A171, A5025, A5626, A6419, B159 — `tshark -r <log>
-Y "bthci_evt.code==0x03 || bthci_evt.code==0x05 || bthci_cmd.opcode==0x0405 || bthci_evt.code==0x04" -T fields -e frame.number -e frame.time -e bthci_evt.bd_addr
-e bthci_cmd.bd_addr -e bthci_evt.connection_handle -e bthci_evt.status -e bthci_evt.reason`, exit 0): the RFCOMM inventory `tshark -r <log> -Y
"bthci_acl.chandle==0x000b && btrfcomm" -T fields -e frame.number -e frame.time_epoch -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type -e btrfcomm.len -e
data.data` fed to a scratch parser (pw_hdlc split on `0x7e`, unescape, `zlib.crc32` over address + control + payload against the trailing 4 bytes little-endian,
per DLCI and direction; the Message Stream as `[Group][Code][Len:2 BE][Value]`); MAESTRO also with `python3 -I scripts/pwrpc_decode.py --handle 0x000b <log>` (exit
0: Z 32, A 424, B 29 packets). Completeness: every frame with an RFCOMM payload on DLCI 2 and 4 carries `data.data` (Z 32/22, A 424/236, B 29/7 — the same counts with
and without `data.data` in the filter). Scripts and outputs are in the session scratchpad (`ai-sessions/0080` RESULT).

## 0. Build, install, user, logs, film and privacy (🟢 FACT unless marked)

- **Build on film:** before the update the Info tab reads **"App: 1.1.0, build 0323849 (2026-10-06)"** (film 17:52:08–20, zoom), after it **"App: 1.1.1, build 86a6fb3
  (2026-10-08)"** (17:58:00–30 and five more times), no "-dirty" either time. L line 5: `package: io.github.tedsluis.opencontrolpixelbuds:10101, targetSdk 34`.
  `86a6fb3` was the tip of `release/1.1.1` when the APK was built; at this session's start the tip is `63a56a9` (this session's prompt, documentation only) and
  `git diff --stat 86a6fb3..origin/release/1.1.1 -- android` prints nothing (exit 0).
- **The tested APK is the built one:** `dist/1.1.1/` (read only): `sha256sum -c` → `OK`; SHA-256 `062f35b3b2d177ed7dbccd336742dffce59b194dccf6c4febcb7dc6def98f1d0`;
  `apksigner verify --print-certs` → `a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb`. With the maintainer's consent (chat 2026-10-08) the installed
  file was read back: `adb shell pm path --user 10 io.github.tedsluis.opencontrolpixelbuds` → `/data/app/~~x35dMFU8KMfd9jKKk9FqlQ==/…/base.apk` (the path of the
  maintainer's post-update `dumpsys`), `adb pull` → SHA-256 **`062f35b3…f1d0`**, equal. The kept copy `~/opencontrol-1.1.1-tested` (B3) exists and equals `dist/1.1.1/`
  file for file (`sha256sum` of both directories identical). `aapt2 dump badging` against `~/opencontrol-1.1.0-tested`: the same five `uses-permission` lines (no
  `INTERNET`), `targetSdkVersion:'34'`; 1.1.1 adds `uses-library-not-required:'androidx.window.extensions'` and `…sidecar'`, which `ai-sessions/0078` recorded.
- **No Google Play in user 10:** the maintainer's P0 (`pm list packages --user 10 | grep -i -E "gms|vending"; echo "exit=$?"`) lists only
  `app.grapheneos.gmscompat.config`, `…gmscompat.lib`, `…gmscompat`, exit 0 — GrapheneOS's own compatibility-layer packages (`CAP-070-FINDINGS.md` §0: their
  manifests in `GrapheneOS/platform_packages_apps_GmsCompat`; grapheneos.org/usage: *"GrapheneOS has a compatibility layer providing the option to install and use the
  official releases of Google Play in the standard app sandbox."*). No `com.google.android.gms` / `com.android.vending` line; the positive control `grep opencontrol`
  matched (exit 0). The wire agrees (§1). User 0 (the Owner) does have Play (`android.vending` in the full system log).
- **The update path (U1):** 🟢 1.1.0 was **newly installed into user 10 at 17:40:13**, 13 minutes before the update: the install session's `PACKAGE_ADDED` is
  `replacing: false` for uid 1010353 (user 10) and `replacing: true` for uid 10353 (user 0) (full system log 17:40:14.155/.208); the maintainer's P1 shows
  `firstInstallTime=2026-10-08 17:40:13` for user 10. 🟢 The update itself kept user 10's data: the same `ceDataInode=343627 deDataInode=468510` before and after,
  `adb shell dumpsys package …` today: `versionCode=10101`, `lastUpdateTime=2026-10-08 17:53:27`, `firstInstallTime` user 0 `2026-10-07 17:22:38`, user 10
  `2026-10-08 17:40:13`. 🟢 The update's `installer_clear_app_data_caller … 39` (17:53:02.146/.180) clears **only the code cache**: 39 = `0x27` = `FLAG_STORAGE_DE`
  (0x1) | `FLAG_STORAGE_CE` (0x2) | `FLAG_STORAGE_EXTERNAL` (0x4) | `FLAG_CLEAR_CODE_CACHE_ONLY` (0x20) — AOSP `frameworks/native/cmds/installd/binder/android/os/
  IInstalld.aidl` lines 150–156 (`const int FLAG_CLEAR_CODE_CACHE_ONLY = 0x20;`), and `frameworks/base/services/core/java/com/android/server/pm/InstallPackageHelper.java`
  (main, fetched 2026-10-08): `if (request.isClearCodeCache()) { mAppDataHelper.clearAppDataLIF(ps.getPkg(), UserHandle.USER_ALL, FLAG_STORAGE_DE | FLAG_STORAGE_CE |
  FLAG_STORAGE_EXTERNAL | Installer.FLAG_CLEAR_CODE_CACHE_ONLY); }` (two lines = users 0 and 10). The same event at 14:31:33 and 17:40:06 belongs to the earlier installs.
  **So U1 is an update over a 1.1.0 that had lived 13 minutes in user 10**, not over `CAP-070`'s copy; the settings it carried were set in that 1.1.0.
- **The settings survived the update (U1):** 🟢 Dark mode — set to On in 1.1.0 on film (17:52:24); 1.1.1 started dark (17:53:30) and Settings showed "On"
  (17:57:45). 🟢 Debug mode — on in 1.1.1 from its first frame (E2 14, the first hex line) and on the switch at the first view (17:57:51), with no tap; its default
  is off (`DebugSettingsStore.kt:43`, `prefs[DEBUG_MODE_KEY] ?: false`) and user 10's data dates from 17:40, so it was switched on in 1.1.0 — 🟡 that this happened
  off film (17:41–17:51, not filmed). 🟢 Permissions and the CDM association kept: 1.1.1's first line `Permissions (start): Bluetooth NOT_REQUESTED -> GRANTED,
  notifications NOT_REQUESTED -> GRANTED` (E2 1) is logged at every process start with both granted (`MainActivity.kt:326` `refreshPermissions("start")`;
  `CAP-070-FINDINGS.md` §0), no permission dialog appeared on film, and the bonded Buds were used without pairing.
- **Install events (full system log, local):** dex2oat `app-version-name:1.1.1,app-version-code:10101` 17:53:02.692; `Killing 26327:io.github.tedsluis.opencontrolpixelbuds
  /u10a353 … killDueToPackageUpdate` 17:53:12.422 (X3148 `am_kill`); `Killing 26493 … u0a353 … installPackageLI (force-kill)` 17:53:27.339 (X3165); `installation
  completed` 17:53:27.552. The new process 28680 started by itself at 17:53:29.171 (`top-activity`, X3240) after Android's "Updaten…" screen (film 17:53:11–28).
- **Processes:** 26327 (1.1.0, user 10) 17:41:42–17:53:12; **26493 (user 0, `bound-service … AncTileService`) 17:43:33–17:53:27** — the app also lives in the
  Owner, where its tile is in Quick Settings; 28680 (1.1.1, user 10) from 17:53:29 to the end. 🟢 The five tile taps of BG-12 were answered by user 10's process: E2
  235–283 "ANC tile tapped (shown: …)" in 28680's ring buffer and the claims on the Buds' link (§1). No crash, ANR or "Decoder fault" (`grep -c -i -E "FATAL|ANR in|
  am_crash|Decoder fault|Unexpected error"` over E2, L and X → 0 each; positive control `grep -c "Settings read"` → 12 in E2).
- **The `SIGQUIT`s are bugreports (answers `CAP-070`'s open question for this run):** 🟢 the computer ran `adb bugreportz` at 18:23:51.070 and 18:27:24.546 (adbd lines,
  full log); dumpstate (`bugreport-tegu-CP3A.260905.009-2026-10-08-18-23-51`) then asked every Java process for its stacks — X40275 the Bluetooth process and X40363
  `28680 … "Signal Catcher"]: reacting to signal 3` 18:24:30.299 → X40366 `Wrote stack traces to tombstoned` 18:24:30.506; again X47501/X47505 18:27:56.8/57.3. The app
  continued. These bugreports are how the HCI logs were pulled. 🟡 `CAP-070`'s two `SIGQUIT`s (06:34:48, 06:40:31) had the same cause — that run had no system log;
  settling: none needed for the app (benign), noted in `CAP-070` by a pointer only if the maintainer wants it.
- **The app's own warnings:** L597/L598/L600 (18:26:43.242) and L671 (18:27:41.058) `W System : A resource failed to call close.` from the FinalizerDaemon thread
  (tid 28689), after the Bluetooth off/on; the same in 1.1.0 at 17:51:08.514 (X547–X550, followed by `V BluetoothSocket: close() XX:XX:XX:XX:6E:07: Already closed`).
  🟡 Framework objects, not an app leak — the pattern `CAP-066-FINDINGS.md` §8 traced on a debug build (a `BluetoothSocket` whose `read()` hit end-of-stream and a
  `BluetoothLeAudio` proxy's `CloseGuard`); a release build logs no stack (`ARCHITECTURE.md` §12), so the objects are not identified here. Present before and after
  the update: not a toolchain change.
- **No network from the app:** 🟢 the APK has no `INTERNET` permission (badging above); in the full system log 17:40–18:29 every line with the app's uid 1010353 is a
  Bluetooth socket line (`BluetoothSocketManagerBinder: connectSocket: … uuid=25e97ff7-… from uid/pid=1010353/28680` for MAESTRO, `uuid=df21fe2c-…` for the Message
  Stream); `grep 1010353 | grep -c -i network` → 0 (positive control: 18 network lines with other uids). The two GitHub pages opened in the browser (film 18:05:48,
  18:06:02).
- **Film and audio:** a camera film (1280×720, portrait) with an **AAC 44.1-kHz stereo** track; overlay 17:51:29–18:27:28; phone = overlay + 1.25 s at both ends
  (8-fps status-bar strips). The audio (16-kHz mono, per-second level and spectrograms) holds room and handling noise (mean −72 dBFS), the phone's ringtone
  (18:13:35–37), short voice-like bursts during the call (18:13:40–18:14:10, up to −29 dBFS; 🟡 a voice, not listened to), and the Buds' **Find ring** — a periodic
  multi-tone chime (≈ 1.6, 2.0, 2.7, 3.5, 4.2, 5.1, 6.1 kHz) growing louder, Left 18:19:27.6–41, Right 18:19:59–18:20:13, each ending before its Stop on the wire
  (18:19:42.05, 18:20:12.72). **No music** is audible (it played in the Buds) and **no case chime** at any of the five bud-into-case moments (18:15:35, 18:16:16,
  18:16:54 with "Earbuds replaced" off; 18:17:46, 18:18:05 with it on) — only broadband handling noise. Nothing was said about the steps.
- **Privacy:** an incoming phone call was answered on film (18:13:37–18:14:13): the caller's full number is on the call screen and the call is on the audio track.
  The maintainer's decision in chat 2026-10-08: commit the film as is; of the system log only the extract (it holds no e-mail address, group id, Wi-Fi name or phone
  number: `grep -c -i -E "ted\.sluis|@g\.us|<caller's digits>"` → 0, while the full log has the caller's number once and the account address 36 times). The number is in
  no document. Extract command (local time, run in the capture folder on the full log):
  `awk '$1=="10-08" && $2>="17:35:00" && $2<="18:29:00" && ($3=="984"||$3=="25526"||$3=="28198"||$3=="3964"||$3=="26327"||$3=="28680"||$3=="26493"||/opencontrolpixelbuds/||
  /com\.android\.bluetooth[\/,:]/)' CAP-071-logcat-all.txt > CAP-071-logcat-extract.txt` → 47,628 lines, SHA-256 `2b568464…a2f2`.
- **HCI files:** A = `CAP-071-btsnoop_hci1.log` + 102 packets (`B[:len(A)]==A` → `True`, `cmp -n 319218` exit 0). Boundaries: P2's Bluetooth off/on (Z ends
  17:51:34.615, A starts 17:51:39.365) and BG-19's (A ends 18:26:23.881 after ACL `0x16` A6990, B starts 18:26:32.311); no Buds ACL spans a gap. Z starts 17:37:32 with
  Bluetooth on in user 10 (Bluetooth process 25526 started 17:37:30.985, X).

## 1. ACLs, the Message Stream and the Definition-of-done negative

| ACL | Start | End | Note |
|---|---|---|---|
| Z1 | pages Z153/Z161/Z165 time out (`0x04`, 17:37:38–48); page Z185 17:42:02.820 → Z187 | Z673 17:42:16.942 `0x13` | **1.1.0 off film**: one session ch 21 (Z264–Z605) |
| A0 | pages A160 (17:57:00.747) and A164 (17:57:11.675) time out `0x04` — the lid closed | — | two Connects of 1.1.1 (E2 4–9) |
| A1 | page A169 17:57:30.183 (Retry) → A171 | A5022 18:20:28.891 `0x13` | both buds into the case; sessions A248 … A4547 |
| A2 | page A5023 18:20:34.008 (Connect tap) → A5025 | A5620 18:20:58.885 `0x13` | buds docked → out → back |
| A3 | **A5622 18:21:08.658, Buds' Connection Request** → A5626 | A6416 18:24:59.825 `0x13` | both buds out (table) |
| A4 | page A6417 18:25:00.244 → A6419 (**the app's connect retry**, below) | A6990 18:26:23.773 `0x16` | Bluetooth off |
| B1 | page B153 18:26:33.062 → B159 | open at B's end | after Bluetooth on |

- 🟢 **No Play-services claim:** `tshark -r <log> -Y 'btrfcomm && frame contains 03:08:00:02:01:25'` → **0** frames in Z, `CAP-071-btsnoop_hci1.log`, A and B (exit 0
  each); positive control `CAP-066-btsnoop_hci.log.last` → 22 (exit 0). **27 Message Stream claims, all the app's** (Z 1, A 25, B 1): every phone `SABM` on DLCI 4 is
  followed by the app's `08 11 00 00` (23 of 27) or a Ring `04 01 00 01 xx` (4 of 27: A4705, A4791, A4872, A4967) as its first message; the 26 claims of 1.1.1
  match E2's 26 "RFCOMM channel 0x04 connected" lines (`grep -c`). The MAESTRO and Message Stream sockets are the app's: `BluetoothSocketManagerBinder: connectSocket … from uid/pid=1010353/28680` (full
  log, e.g. 17:57:30.181 uuid `25e97ff7-…`, 17:57:30.773 uuid `df21fe2c-…`).
- 🟢 **No other app on the Buds' RFCOMM:** the DLCIs on handle `0x000b` are 0, 2, 4 and 9 in Z and A, and 0, 2, 4 and 12 in B; `-Y 'bthci_acl.chandle==0x000b &&
  (btrfcomm.dlci==8 || btrfcomm.dlci==10 || btrfcomm.dlci==11)'` → 0 in A (exit 0; positive control `CAP-070-btsnoop_hci2.log.last` → 43). DLCI 9 is **HFP opened
  by the Buds** (`SABM` B>P Z505, A456 …; `AT+BRSF=921` in Z); DLCI 12 in B is HFP opened by the phone after Bluetooth on (B259, tshark: "Channel=6 (Hands-Free)").
  The app sent nothing there.
- 🟢 Every claim starts with the Buds' Device Information burst (`03 0a`, `03 01 00 03 da 2d b1`, `03 02`, `03 09 … Revision 6`, `07 10`, `07 34`, `03 03` ×3); from
  18:08:13 also `03 0b` (25 bytes, the Find Hub EID, `PROTOCOL.md` §6) — counted in the Debug tab as unidentified (5 → 20 → 136 → 146 on film).
- 🟢 **ANC `Notify` Settable** (`python3 -I scripts/message_stream_tally.py` → `CAP-071-btsnoop_hci2.log.last {'get': 21, 'notify_00': 8, 'notify_e8': 31, 'set': 9,
  'ack': 9}`; the script scans every capture, its TOTAL counts `CAP-071-btsnoop_hci1.log` again): `00` in 8 claims of A (A282, A997, A1156, A1204, A5136, A6533: both buds
  docked; A5547, A5915: both just out of the case, in the hand or on the table) and in Z/B; `e8` in every claim from 18:08:13 to 18:18:44 (at least one bud out of the case, worn per the
  film's hand-to-head moves). No counter-example to ADR-049 item 3 (🟡 `00` ⇒ no bud worn).

## 2. The Connect read — twelve settings (BG-1, BG-15, BG-19; T1, U3, U13)

🟢 **11 of 11 sessions** (Z 1, A 9, B 1): the Buds' unsolicited `GetSoftwareInfo` → `ReadSetting 4:16` → `ReadSetting` **2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29** in
that order, each answered by an OK `RESPONSE 4:{N:…}` → one `SubscribeRuntimeInfo`. The phone sent 205 pw_rpc requests in Z/A/B — `ReadSetting` 168,
`SubscribeRuntimeInfo` 13, `WriteSetting` 24 — and nothing else (no `GetSoftwareInfo` request, no other service); every read and write has its `RESPONSE`
(168 + 24), every subscription its `SERVER_STREAM`, and no packet carries a status other than OK (`grep -h "status=" <pwrpc output> | grep -v -c "status=OK"` → 0). CRC-32 OK on all 485 pw_hdlc frames (Z 32, A 424, B 29; 0 `crc=BAD`, 0 unparsed).

| Session (announcement) | Ch | State | 12 | 17 | 28 | 29 | Latency per read | Subscribe |
|---|---|---|---|---|---|---|---|---|
| Z294 17:42:03.808 (1.1.0, off film) | 21 | both docked | `{1:1 2:1 3:1 4:0}` | 7 | 1 | 2 | 21–73 ms | Z377 |
| **A261 17:57:30.791** (1.1.1, first) | 21 | both docked | `{1:1 2:1 3:1 4:0}` | 7 | 1 | 2 | 12–88 ms | A344 |
| A3885 18:16:15.218 | 19 | Right docked, Left out | **`{1:1 2:0 3:1 4:0}`** | **0** | **0** | 2 | 28–84 ms | A3977 |
| A4105 18:16:47.734 | 21 | both out | `{1:1 2:0 3:1 4:0}` | 0 | 0 | 2 | 23–85 ms | A4180 |
| A4390 18:18:04.593 | 19 | Right docked, Left out | `{1:1 2:0 3:1 4:0}` | 0 | **1** | 2 | 22–115 ms | A4484 |
| **A4563 18:18:43.165 (BG-15)** | 19 | both out | `{1:1 2:0 3:1 4:0}` | 0 | 1 | 2 | 24–87 ms | A4640 |
| A5127 18:20:34.265 | 19 | both docked | same | 0 | 1 | 2 | 17–129 ms | A5198 |
| A5524 18:20:51.186 | 21 | both out | same | 0 | 1 | 2 | 57–136 ms | A5602 |
| A5890 18:21:09.893 | 21 | both out | same | 0 | 1 | 2 | 8–74 ms | A5987 |
| A6514 18:25:03.983 | 21 | both docked | same | 0 | 1 | 2 | 12–64 ms | A6592 |
| B450 18:26:33.607 (after Bluetooth on) | 21 | both docked | same | 0 | 1 | 2 | 40–63 ms | B530 |

Plus the two pulls of BG-5 on A261's session: Controls 18:00:12.47 (the twelve, no 16; A1035–A1094) and Sound 18:00:18.73 (16 + the twelve; A1095–A1133).
Command: `python3 -I scripts/pwrpc_decode.py --handle 0x000b <log>` and a scratch pairing of each `REQUEST ReadSetting` with the next `RESPONSE`.

- 🟢 **BG-15 read-back:** the reads after Disconnect/Connect (A4563 …) answer every last write — `16:[-3.5, 0.5, 1.0, -1.0, 6.0]`, `17:0`, `19:0`, `22:1`, `15:1`, `4:1`,
  `7:{1:{4:{1:5}} 2:{4:{1:5}}}`, `12:{1:1 2:0 3:1 4:0}`, `2:1`, `29:2`, `11:1`, `27:1`, `28:1`; the Sound and Controls tabs showed them (film 18:18:44–18:19:00).
  🟢 The Buds kept them across the later ACL drops and the Bluetooth off/on (B450's reads equal).
- 🟢 **The first ready (U3/S6):** "App control: ready" on film at phone 17:57:30.76 = E2 13 `17:57:30.765 Discovering -> Ready`; the Controls tab already showed "Use
  touch controls" on at 17:57:30.96 (5-fps strip) — field 4's answer A313 came at 17:57:30.958, so a "—" could not be caught by the camera. 🟢 "—" is on film in
  place of every switch and EQ band while not connected (17:56:25–17:56:44) and during the automatic re-opens (18:16:14, 18:16:46, 18:18:02). Without a screen
  recording "about a second" is not measured.
- 🟢 **Latency:** 8–136 ms per read across the 11 sessions; the slowest pass (A5524) took 1.59 s from announcement to subscription; no read reached
  `SETTING_READ_TIMEOUT_MS` (2,000 ms). `CAP-070`: 8–273 ms.

## 3. Every write (BG-11 … BG-16; U10 … U14) — byte for byte against the references

🟢 **24 `WriteSetting` requests, all on channel 21, each answered by the empty `RESPONSE`** (`7e 00 a5 03 08 01 10 15 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 03 6d 4e d8 7e`,
E2 e.g. 300), none with an error status, none unanswered; **9 ANC `Set`s → 9 ACKs, 0 NAK; 4 Ring/Stop → 4 ACKs**. Every request is byte-identical to its reference
(the skeleton's table, `ai-sessions/0079` RESULT §A.3) or, where the table has no row, to the same request in an earlier OpenControl capture:

| Write | Frames (request → answer) | Bytes after `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 1d 9a 8c 9e` | Reference | Equal |
|---|---|---|---|---|
| EQ preset **Balanced** `4:{16:[-3.5,0.5,1.0,-1.0,2.5]}` | A3000 → A3002 (18:10:18.372) | `2a1e221c8201190d000060c0150000003f1d0000803f25000080bf2d0000204008f7fa577e` | `CAP-059` 2188 | ✓ |
| Upper treble `5:6.0` | A3009 → A3011 | `…2d0000c040e40484097e` | EQ codec (ADR-020); CRC OK | — (a slider value) |
| Balance Centre `4:{17:0}` | A3013 → A3015 | `2a0522038801000aebbb9d7e` | `CAP-064` 6712, `CAP-067` | ✓ |
| Mono on / off `19:1` / `19:0` | A3022 / A3025 | `2a052203980101ec789af67e` / `…980100 7a489d817e` | `CAP-063` 5371 / 5261 | ✓ |
| Conversation off / on `22:0` / `22:1` | A3028 / A3031 | `2a052203b00100225fc3b77e` / `…b00101b46fc4c07e` | `SettingsFixtures.kt` (`CAP-019`) | ✓ |
| Volume EQ off / on `15:0` / `15:1` | A3034 / A3043 | `2a04220278000f47ef397e` / `…78019977e84e7e` | `CAP-070` A785 / A4800 | ✓ |
| Touch off / on `4:0` / `4:1` | A3046 / A3051 | `2a042202200053908d4b7e` / `…2001c5a08a3c7e` | `CAP-020` 1995 (fixture) | ✓ |
| Left hold assistant / noise control `7:{1:{4:{1:6}}}` / `{1:5}` | A3054 / A3057 | `2a0a22083a060a04220208062c50cfbc7e` / `…08059601c6257e` | `CAP-063` 5654 / 5684 | ✓ |
| Mode list without Off `12:{1:1 2:0 3:1 4:0}` | A3060 → A3062 | `2a0c220a62080801100018012000fc57b66a7e` | `CAP-041` 2198 | ✓ |
| In-ear off / on `2:0` / `2:1` | A3063 / A3066 | `2a0422021000a0a6cb947e` / `…10013696cce37e` | `Cap056Fixtures.kt` (`CAP-056`) | ✓ |
| Head gestures off / on `29:1` / `29:2` | A3075 / A3078 | `2a052203e80101bc106ba27e` / `…e801020641623b7e` | `CAP-070` A762 / `CAP-069` 2564 | ✓ |
| Multipoint off / on `11:0` / `11:1` | A3227 / A3232 | `2a0422025800ad636bac7e` / `…58013b536cdb7e` | `CAP-070` A752 / `CAP-069` 3212 | ✓ |
| Earbuds replaced off / on `28:0` / `28:1` | A3288 / A4289 | `2a052203e0010092717fdb7e` / `…e00101044178ac7e` | `CAP-070` A770 / `CAP-058` 5643 | ✓ |
| Other alerts off / on `27:0` / `27:1` | A4278 / A4283 | `2a052203d80100bac507f17e` / `…d801012cf500867e` | `CAP-070` A777 / `CAP-058` 5697 | ✓ |
| ANC `Set` T / A / Off / Active (tab) | A2560 / A2617 / A2668 / A2720 | `08 12 00 14 01 e8 e8 80` (`40`, `20`, `08`) + 16 × `00` | `CAP-068` 1039/1266/1090/1146 | ✓ |
| ANC `Set` via the tile ×5 | A2780, A2831, A2883, A2934, A2984 | `…80`, `…40`, `…20`, `…08`, `…80` | the same | ✓ |
| Ring Left / Stop / Right / Stop | A4705 / A4791 / A4872 / A4967 | `04 01 00 01 02` / `00` / `01` / `00` | `CAP-068` 2142/2416, `CAP-062` 9660 | ✓ |

Commands: the scratch parser above (raw frames with CRC check) and `tshark -r <earlier log> -Y "frame contains <bytes>" -T fields -e frame.number` for the
earlier-capture matches (e.g. `frame contains 2a:05:22:03:98:01:01:ec:78:9a:f6` → `CAP-063` 5371).

- 🟢 **Only after the answer:** each E2 "Setting N written" line comes 3–5 ms after its `RESPONSE` (e.g. A3015 18:11:21.626 → E2 309 18:11:21.637; A4291 18:17:39.778 →
  E2 529 18:17:39.781); each ANC mode on film changed after its ACK (Transparency shown at phone 18:08:13.3, ACK A2563 18:08:13.202). Each ANC claim sent `08 11` first
  and its `Set` only after a `Notify` with Settable `e8` (`ARCHITECTURE.md` §3.1).
- 🟢 **The Buds are restored** (`TODO.md` §4): EQ Balanced was written (A3000), then the Upper treble moved to +6.0 (A3009) — so the active EQ is Balanced with Upper
  treble 6.0, not Balanced (B492 `4:{16:[-3.5, 0.5, 1.0, -1.0, 6.00]}`); the press-and-hold mode list is without Off (B508). 🔴 Whether the maintainer wants the band at
  2.5 — a one-tap preset after the release.
- 🟢 **ANC during playback:** A2DP streamed from A2462 (`START` 18:07:26.406) to A3091 (`SUSPEND` 18:13:34.786) — all 9 `Set`s (18:08:13–18:09:52) and the BG-13
  writes happened while music played; each was ACKed and followed by the Buds' `Notify` of the new mode. That the modes *sounded* right is the maintainer's statement
  (chat 2026-10-08), not on the audio track.
- 🟢 **Find:** each Ring/Stop ACKed `ff 01 00 03 04 01 00` and echoed by the Buds (`04 01 00 01 xx`, A4710 …); the ring is on the audio track (§0).
- 🔴 **Not established:** whether a case chime sounds with "Earbuds replaced" on or off (no chime recorded either way, nothing said), and what Volume EQ, mono or
  Multipoint do to the sound (known issues, unchanged).

## 4. The channel choice and lead L-1 (`INEAR-005` not run)

| Announcement | Ch | State (film slots + runtime info 6.2 Left / 6.3 Right field 2) | `PROTOCOL.md` §2.2a 🟢 rule |
|---|---|---|---|
| Z294, A261, A6514, B450 | 21 | both docked | not covered |
| A5127 | 19 | both docked | not covered |
| **A3885 18:16:15.22** | 19 | **only the Left out** (Right seated 18:16:13, A3981 6.3 = 2) | ✓ |
| **A4390 18:18:04.59** | 19 | **only the Left out** (Right seated 18:18:02, A4486 6.3 = 2) | ✓ |
| A4105 18:16:47.73 | 21 | both out (the Right just back from the case to the head) | — |
| A4563, A5524, A5890 | 19, 21, 21 | both out | — |

- 🟢 **Two more samples of "only the Left out ⇒ 19"**; no counter-example. With both docked the Buds announced 21 four times and 19 once — not fixed, as in `CAP-070`.
- 🟢 **Session on 21:** the Right into the case ⇒ Buds `DISC` with the ACL up (A3840 18:16:13.30, A4345 18:18:02.72) and re-home to 19; the Left's three trips into the
  case and out (18:15:35–43, 18:16:51–59, 18:17:46–53) gave **no** `DISC`. 🟢 **Session on 19:** the Right out of the case (18:16:25, 18:18:08) gave no `DISC`, but at
  18:16:45.88 (A4060) the Buds closed MAESTRO while the Right was being taken to the head (film 18:16:30–44, head out of view) and the re-open announced **21**.
  🟡 HYPOTHESIS (consistent with `PROTOCOL.md` §2.2a's "the channel names the bud hosting the link"): the Buds re-home the link to the Right when it becomes worn again;
  `CAP-066` saw "into an ear with the Left not worn ⇒ 21" once. Settling: BG-20 (the Left out with both worn on 19, head in view) and the Right into an ear with the
  Left worn on 19, head in view.
- 🔴 **Lead L-1 itself (BG-20) was not run** — no film 2. Status unchanged.

## 5. The screens (BG-2 … BG-10; U2, U4 … U9, C10, O2, O3, K4) and the open film items

- 🟢 Every tab by tap and by swipe (17:58:53–17:59:59): each swipe changed the tab and the bottom bar followed (C10, O2). Five pulls (18:00:06–34), each running its
  tab's refresh once (§2, §1). Nine (i) dialogs opened and closed with Close and with back (18:00:47–18:02:07); their times match the wire ("EQ updated: 18:00:18",
  "Balance: Right 4 · read 18:00:19", "Left: 100 % (updated 18:00:34) — charging in the case (18:00:36)").
- 🟢 **Settings menu (U7, O3):** tabs Settings · Debug · Info, the selected label with a **full-width** underline under its tab (zoom 18:02:36) — as 1.1.0 (`TabRow`
  kept, `ai-sessions/0078`); **←** and system back both returned to Controls (18:03:06, 18:03:13).
- 🟢 **Rotation (K4, U8):** Sound stayed Sound (18:03:30–39), Info stayed Info (18:03:57–18:04:07); E2 126/131/136/141 show four `Permissions (start)` lines in the same
  process — Activity re-creations, the configuration changes (`wm_on_create_called` in L).
- 🟢 **Dark mode (U1, U8, BG-3, BG-9):** On kept across the update; with Dark mode **System**, Android's dark theme off turned the app light at once without a restart
  (18:05:05–09; E2 153, the same PID); the first Android toggle (18:04:44) was made while the app's setting was still On.
- 🟢 **Licence and links (U9, BG-10):** the full licence text (scrolled to the warranty and liability sections), Close; "README on GitHub" and "Report an issue on
  GitHub" opened the browser at `github.com/tedsluis/opencontrolpixelbudspro2` (README; issues, not signed in); the app only logged its link observer
  stopping and starting (E2 158–165).
- 🔴 **C12 (BG-21), S12 (BG-22), T11 (BG-23's dumps):** not done — no film 2, no screen recording, no `uiautomator` dump. The screen-reader description of "—" stays
  verified by the unit tests only. Stay open in `TODO.md` §2.
- 🟢 **H5 not done:** *Read EQ again* was not tapped (no `ReadSetting 4:16` between A3011 18:10:29 and A3892 18:16:15). The EQ was read back at the next Connect
  instead (A3896, A4576 …: `[-3.5, 0.5, 1.0, -1.0, 6.0]`).

## 6. Session ends, re-opens and processes (BG-17 … BG-19; C8, C9, K1, K2)

| # | Time | Cause on the wire | App log (E2) | Re-open |
|---|---|---|---|---|
| — | 17:57:05.9 / 17:57:16.8 | page timeout `0x04` (A163, A167) — lid closed | "connect failed after 5270 / 5150 ms (attempt 1/3)", `Failed(ChannelUnavailable)` (5–9) | Retry 17:57:30.17 (a tap) |
| 1 | 18:16:13.30 | Buds `DISC` DLCI 2 (A3840), ACL up — the Right into the case | "the Buds closed the channel (provisional …)" (392–396) | AFTER_LOSS 18:16:14.84 (+1.51 s) → 19 |
| 2 | 18:16:45.89 | Buds `DISC` (A4060) — the Right to the head | the same (453–457) | AFTER_LOSS 18:16:47.40 (+1.50 s) → 21 |
| 3 | 18:18:02.73 | Buds `DISC` (A4345) — the Right into the case | the same (540–544) | AFTER_LOSS 18:18:04.24 (+1.50 s) → 19 |
| 4 | 18:18:40.47 | phone `DISC` (A4526) | "Session ended by the user's Disconnect tap" (604) | Connect tap 18:18:42.83 → 19 |
| 5 | 18:20:28.91 | ACL `0x13` (A5022) — both buds into the case | "Android's link to the Buds went down around the loss" (704), final | none (link `NOT_CONNECTED`); Connect tap 18:20:34.00 → 19 |
| 6 | 18:20:48.97 | Buds `DISC` (A5473) — a bud out of the case | provisional → Buds closed (754–758) | AFTER_LOSS 18:20:50.48 (+1.50 s) → 21 |
| 7 | 18:20:55.91 | Buds `DISC` (A5608), then ACL `0x13` (A5620 18:20:58.885) | "Automatic re-open skipped: the session was lost 4963 ms after an automatic re-open" (809) | LINK_BACK 18:21:09.07 (the Buds' ACL A5622) → 21 |
| 8 | 18:24:57.75 | Buds `DISC` (A6391), then ACL `0x13` (A6416 18:24:59.825) — both into the case | provisional → Buds closed (890–894) | AFTER_LOSS 18:24:59.25: attempt 1 fails after 580 ms, **attempt 2 pages the docked Buds** (A6417 18:25:00.244 = 18:24:59.836 + 400 ms) → ready 18:25:03.95 → 21 |
| 9 | 18:26:23.56 | phone `DISC` (A6969), ACL `0x16` (A6990) | "Session loss cause: Bluetooth was switched off on this phone" (956), final | LINK_BACK 18:26:33.50 → 21 |

- 🟢 Every loss was classified and recovered; the re-open delay was 1.500–1.508 s (`SessionReopener.kt:134` `LOSS_REOPEN_DELAY_MS = 1_500L`); the chain guard held
  once (#7, `SessionReopener.kt:102–104`, `CHAIN_GUARD_MS = 10_000L` `:137`); the first LINK_BACK attempt after the Buds' own ACL collided on the multiplexer (A5838
  then A5871 `SABM` DLCI 0, "connect failed after 95 ms") and the retry 400 ms later succeeded — as in `CAP-070` (`RfcommBudsTransport.kt:386`).
- 🟢 **#8 explains a phone page after a drop:** the retry of the app's own AFTER_LOSS attempt (a fast failure, < 2 s, so retried after `DEFAULT_RETRY_DELAY_MS`) is
  the phone's `Create Connection` 0.42 s after the ACL drop; the docked Buds (lid open) accepted it and the session came back with both docked (C9 "ready again by
  itself"). 🟡 `CAP-064` 8399 and `CAP-066` B2438 (pages 0.1 s after a drop, "🔴 why" in `PROTOCOL.md` §5) may be the same mechanism — settling: those runs' exports
  around the drops.
- 🟢 **#5 is the documented limit, not a defect:** with both buds docked the Buds dropped the ACL; Android's link stayed down, so the app had no event to re-open on
  (`ARCHITECTURE.md` §6.0b; `TODO.md` §5 "Auto-connect on lid-open: not possible"). The maintainer tapped Connect.
- 🟢 **Home for 2 min 8 s (BG-18, K1/K2):** link observer stopped 18:21:20.244 and started 18:23:25.959 (E2 873–880); the session stayed ready (no loss in between).
- 🟢 **Bluetooth off/on (BG-19):** "Bluetooth adapter: ON -> TURNING_OFF" (E2 951) 0.41 s before the loss, cause final; on: "Automatic re-open … LINK_BACK" (E2 963) and
  the twelve reads. "Bluetooth is disabled." is not on film (Android's Bluetooth dialog covered the app).
- 🟢 **Processes:** 28680 lived from 17:53:29 to after the film; no force-stop (BG-23 not done); no new process.

## 7. Battery and case

- 🟢 Every value shown equals the last wire value before it: film 17:58:54 Left 100 % ⚡, Case 72 %, Right 100 % ⚡ (A298 `03 03 00 03 e4 e4 ff`, A780 17:58:46 6.1 = 72);
  18:07:01 Right 100 % without ⚡ (A2330 6.3 field 2 = 1); 18:07:13 Case 72 % dimmed with the (i) dot (no 6.1 since A2377 — "last seen"); 18:18:42 Case 71 % (6.1 = 71 from
  A3355 18:15:36.86); 18:20:35 Left/Right ⚡, Case 71 (A5201). Case 73 → 72 (A780) → 71 (A3355).
- 🟢 6.1 (Case) present exactly when at least one bud reports field 2 = 2 in 75 of 77 stream packets (Z/A/B); the two others are transitional packets 3 ms and
  0.07 s before the next one (A3992 18:16:25.365 → A3995; A4202 18:16:57.166 → A4210), as `CAP-070` A3788. Scratch check over every `SERVER_STREAM` packet.

## 8. Secondary user, user 0, and the other profiles

- 🟢 Nothing in the app's behaviour depends on the user: the reads, writes, ACKs and re-opens equal `CAP-070` and the Owner-user captures; the Message Stream was never
  contended (no Play services in user 10), so every claim succeeded on attempt 1 (27 of 27).
- 🟢 **The app in user 0** is a separate process (26493, `u0a353`) started for its Quick Settings tile when the Owner's SystemUI bound it (17:43:33); it made no Buds
  connection (no MAESTRO `SABM` by another uid) and was killed by the update. The tile tapped in BG-12 was user 10's (`sysui_multi_action … AncTileService`, X21198 …;
  E2 235–283). The ANC tile had first to be added to user 10's Quick Settings (film 18:08:56–18:09:22).
- 🟢 HFP on DLCI 9 (opened by the Buds) and DLCI 12 (opened by the phone after Bluetooth on); the phone call routed to the Buds over eSCO (A3114 18:13:35.236 →
  A3163 18:14:16.470 `0x16`); AVDTP start/suspend pairs (56 `START`s, many short — system sounds) and the streaming span above. LE: one link to `c8:cc:…` (A3361
  18:15:38 → A6984 18:26:23 `0x16`); none to the Buds.

## 9. 1.1.0 → 1.1.1: did the toolchain change anything? (lead 2)

| Aspect | 1.1.0 (`CAP-070`, and Z here) | 1.1.1 (this run) | Same? |
|---|---|---|---|
| Connect read | 16, then 2 … 29 in order; one `SubscribeRuntimeInfo`; no other request | identical, 11 of 11 sessions (Z = 1.1.0 here, A/B = 1.1.1) | ✓ |
| Request bytes | the reference table | every request byte-identical (§3) | ✓ |
| `GetSoftwareInfo` | never requested | never requested | ✓ |
| Message Stream claim | `08 11` first; Ring claims `04 01` first; released ≈ 1.5 s after the last answer | the same (27 claims, 1.545–1.93 s from `SABM` to `DISC`) | ✓ |
| ADR-044 timing | re-open +1.5 s, chain guard 10 s | +1.500–1.508 s; one skip at 4,963 ms | ✓ |
| Write rule | applied on the empty `RESPONSE`/ACK only | the same (§3) | ✓ |
| Screens | `CAP-070`'s film | same cards, texts and order; tab row full-width underline; rotation, dark switch, licence, links as before | ✓ |
| Warnings | "A resource failed to call close" (framework) | the same, in 1.1.0 and 1.1.1 (§0) | ✓ |
| Crashes / ANRs | none | none | ✓ |

🟢 **No behaviour change attributable to the toolchain was found.** The only differences are in the APK's manifest metadata (`compileSdkVersion='37'`, the two
`uses-library-not-required` lines), recorded by `ai-sessions/0078`.

## 10. Re-test verdicts and the release classification

**The skeleton's "Refuted if", applied literally:** BG-1 — no crash, not light, no read missing, out of order or different, no other request → **not refuted**
(the session did not open by itself: the lid was closed, P8; after the lid opened it opened on Retry). BG-2 — neither choice lost, "1.1.1, build 86a6fb3", no
"-dirty" → not refuted. BG-4 … BG-10 — not refuted. BG-11/BG-12 — no differing `Set`, no NAK, `Get` first every time → not refuted. BG-13/BG-14 — no differing request,
reads equal the writes → not refuted. BG-15 — not refuted. BG-16 — the ring stopped each time → not refuted. BG-17 — "no automatic re-open while the app is on screen":
the first time (#5) none came because Android's link was down (by design); the second time (#8) it re-opened by itself → not refuted as a defect. BG-18, BG-19 — no
crash, re-open → not refuted. BG-20 … BG-24 — **not run**.

| # | What | Class | Evidence |
|---|---|---|---|
| 1 | Film 2 not made: lead L-1 (BG-20), C12 (BG-21), S12 (BG-22), T11 (BG-23's dumps), the force-stop (BG-23) | open test items — none is a 1.1.1 change | §4, §5 |
| 2 | No screen recording: S6/U3 ("—" for ≈ 1 s) not measured | open test item | §2 |
| 3 | H5 (*Read EQ again*) not done; the "Right 4" balance write not done | open test items | §5, §3 |
| 4 | Case chime, Volume EQ, mono, Multipoint: what is heard not established (no chime recorded, nothing said) | known issue, unchanged | §3, §0 |
| 5 | The screen-reader text of "—" unverified on a phone | known issue, unchanged | §5 |
| 6 | With both buds docked and the ACL down, no automatic re-open (#5) | documented limit (`TODO.md` §5) | §6 |
| 7 | "A resource failed to call close." ×4 in the app's logcat | observation — framework objects, the same in 1.1.0 (🟡) | §0 |
| 8 | The two `SIGQUIT`s | the computer's `bugreportz`, not the app | §0 |
| 9 | P1: 1.1.0 newly installed in user 10 thirteen minutes before the update and used off film (P9) | test-procedure deviation; the update path itself is shown (data kept, settings kept) | §0 |

**No heavy defect:** no crash or ANR; 24 of 24 setting/EQ writes, 9 of 9 ANC `Set`s and 4 of 4 Ring/Stop answered OK/ACKed, byte-identical to their references, and
the settings read back after a reconnect; no write applied before its answer; no request outside the user's actions and the documented Connect read; no Safe Mode on
`release_5.203`; every session end recovered; no setting lost by the update; nothing from the app on DLCI 0x08/0x0a; no behaviour change from the toolchain.
**Verdict (the maintainer, chat 2026-10-08, `AskUserQuestion` "Verdict", option *"Release 86a6fb3 as 1.1.1 (Recommended)"*; "Known issues": *"Keep unchanged
(Recommended)"*):** release the tested build `86a6fb3` as 1.1.1 with the known issues of `CHANGELOG.md` `[1.1.1]` unchanged; the skipped film-2 items stay in
`TODO.md` §2 — they test 1.1.0 behaviour that 1.1.1 does not change.

## 11. Protocol correlation (per channel)

- **MAESTRO (DLCI 2):** as ADR-034/036/043/045 … 055; 485 of 485 pw_hdlc frames CRC-OK; every `RESPONSE` status OK; no `CLIENT_ERROR`/`SERVER_ERROR`. Field 17
  `sint32` zigzag (`17:7` = −4 "Right 4", `17:0` Centre); field 29 1/2 (never 0); field 12 four booleans.
- **Message Stream (DLCI 4):** only the app's claims; ANC Get/Set/Notify and Ring as `PROTOCOL.md` §4.1/§4.4; Settable as ADR-049 (§1); `03 0b` present.
- **DLCI 9 / 12:** HFP (opened by the Buds / by the phone); no GSND channel in this user.
- **§2.2a channel:** §4.

## 12. Improvements (proposals only — nothing under `android/` was changed)

No app defect was found, so no code change is proposed. For the next app run:

1. **Test procedure:** film 2 (BG-20 … BG-24) as written, with Android's screen recording on (S6, C12) and the head in view; keep the lid **open** before the update
   (P8) and no app use before the film (P9); say each observation aloud; leave out incoming calls (Do Not Disturb) — the call put a third party's number on film.
2. **`CAPTURE_BLUETOOTH_HCI_SNOOP.md`:** note that `adb bugreportz` sends signal 3 to every Java process (the "Wrote stack traces to tombstoned" lines), so a capture's
   app logcat shows a `SIGQUIT` per pull — the explanation for `CAP-070` §0 as well (🟡 there).
3. **`TODO.md` §4:** the active EQ is Balanced with Upper treble +6.0 — one tap on Balanced restores the preset (a maintainer step, no capture needed).

## 13. Open questions

- 🔴 Lead L-1 with both buds worn (BG-20), C12, S12, T11 — not run.
- 🟡 Inserting the Right into an ear re-homes a channel-19 session to 21 (18:16:45, one sample, head out of view) — `PROTOCOL.md` §2.2a note of 2026-10-08.
- 🟡 The phone pages after a drop in `CAP-064`/`CAP-066` were the app's connect retry (as #8 here) — `PROTOCOL.md` §5 note of 2026-10-08.
- 🔴 What the case sounds with "Earbuds replaced" off/on — the audio track recorded no chime either way.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-071-2026-10-08_17-51-29_18-27-28-Group_BG/CAP-071-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-071-2026-10-08_17-51-29_18-27-28-Group_BG/CAP-071-FINDINGS
