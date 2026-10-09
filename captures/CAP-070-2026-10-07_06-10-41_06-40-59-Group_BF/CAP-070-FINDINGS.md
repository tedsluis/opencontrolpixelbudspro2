# Findings: `CAP-070` (Group BF — the release-signed 1.1.0 APK in a GrapheneOS secondary user without Google Play: the hardware test of the five switches of `ai-sessions/0074`)

> **Status as of 2026-10-08** (`ai-sessions/0081`; read this first — the body below is the analysis as written): §0/§12's two `SIGQUIT`s are the computer's `adb bugreportz` (dumpstate sends signal 3 to every Java process — `CAP-071-FINDINGS.md` §0, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` "Note for every capture"), 🟡 as a mechanism shown in `CAP-071`; §11 item 1 was done in `ai-sessions/0076` (real `CAP-070` frames as fixtures); §11 item 3 was done in `CAP-068-FINDINGS.md` §0; §11 item 2 and §12's T11, C12, S12 and BF-18 are in `TODO.md` §2.

Standardized, evidence-based extraction from the four HCI snoop logs, the camera film (`CAP-070-recording.mp4`), the four debug exports and the two app
logcats, per `ai-sessions/0075`. There is no system log, no screen recording and no `uiautomator` dump. The timeline these findings refer to is
`CAP-070-EVENT-NOTES.md` (prefixes "Z", "A", "B", E1–E4, L1–L2 as defined there).

- 🟢 **FACT** — directly observed in this capture (frame number, log line or film time given).
- 🟡 **HYPOTHESIS** — plausible reading, not yet independently confirmed; the settling experiment is named.
- ⚪ **ASSUMPTION** — not tested here.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-070` · **Date:** 2026-10-07, film overlay 06:10:41–06:40:59 (phone = overlay + 0.5 s ± 0.25 s) · **Firmware:** 🟢 `release_5.203` (22 of 22
announcements, 66 entries; Info tab on film) · **Phone:** Pixel 9a, GrapheneOS, Android 17 `CP3A.260905.009`, **secondary user 10 (`full.secondary`), no Google
Play** · **App under test:** OpenControl for Pixel Buds Pro 2 **1.1.0, build `0323849`** (release-signed, versionCode 10100) · **HCI logs:** Z
`CAP-070-btsnoop_hci1.log.last` (4,664 packets, 05:44:02.521–06:10:46.252, the unfilmed rehearsal), A `CAP-070-btsnoop_hci2.log.last` (5,285,
06:10:52.690–06:37:28.403), B `CAP-070-btsnoop_hci2.log` (1,430, 06:39:36.576–06:42:06.705); `CAP-070-btsnoop_hci1.log` = the first 222,764 bytes of A, not
counted · **Buds:** `04:00:6e:…:07`, classic handle `0x000b` in all 19 ACLs; no LE link to the Buds.

Commands used throughout (rule 4a; all on handle `0x000b`, shown matching the Connection Complete events Z161, A159, B159 — `tshark -r <log> -Y
"bthci_evt.code==0x03 || bthci_evt.code==0x05 || bthci_cmd.opcode==0x0405" -T fields -e frame.number -e frame.time -e bthci_evt.bd_addr -e bthci_cmd.bd_addr -e
bthci_evt.connection_handle -e bthci_evt.reason`, exit 0): the RFCOMM inventory `tshark -r <log> -Y "bthci_acl.chandle==0x000b && btrfcomm" -T fields -e
frame.number -e frame.time_epoch -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type -e btrfcomm.len -e data.data` fed to a scratch parser (pw_hdlc split on
`0x7e`, unescape, `zlib.crc32` over address + control + payload vs the trailing 4 bytes little-endian, per DLCI and direction; the Message Stream as
`[Group][Code][Len:2 BE][Value]`); MAESTRO with `python3 scripts/pwrpc_decode.py --handle 0x000b <log>` (exit 0: Z 318, A 540, B 93 packets). Scripts and outputs
are in the session scratchpad (`ai-sessions/0075` RESULT).

## 0. Build, install, user, logs (🟢 FACT unless marked)

- **Build:** the Info tab reads "App: 1.1.0, build 0323849 (2026-10-06)" without "-dirty" (film 06:30:54; also 06:12:28–54); both logcats: `package:
  io.github.tedsluis.opencontrolpixelbuds:10100`. `0323849` is the tip of `origin/feature/0074-settings-switches` and pull request #7's head (`gh pr view 7` →
  `headRefOid 0323849e…`, `OPEN`); `git diff 0323849..origin/feature/0074-settings-switches -- android` is empty — the tested build is `RELEASING.md` B1's build
  commit. `dist/1.1.0/opencontrol-pixelbudspro2-1.1.0.apk` (read only): SHA-256 `0bc82f75…438abc13b` (= its `.sha256`), `apksigner verify --print-certs` → signer
  SHA-256 `a7530f5c…c79d8dcb`, `aapt2 dump badging` → `versionCode='10100' versionName='1.1.0'`, its dex holds `0323849` and `2026-10-06` (`strings`). ⚪ that
  the installed file is that APK (no install log).
- **No Google Play in user 10:** the maintainer's P0 lists only `app.grapheneos.gmscompat`, `…gmscompat.lib`, `…gmscompat.config`. These are GrapheneOS's own
  packages: `github.com/GrapheneOS/platform_packages_apps_GmsCompat` (branch `17`) `AndroidManifest.xml` → `package="app.grapheneos.gmscompat"`,
  `lib/AndroidManifest.xml` → `package="app.grapheneos.gmscompat.lib"`, `config-holder/app/build.gradle.kts` → `namespace = "app.grapheneos.gmscompat.config"`;
  grapheneos.org/usage (fetched 2026-10-07): *"GrapheneOS has a compatibility layer providing the option to install and use the official releases of Google Play
  in the standard app sandbox."* and *"Since the Google Play apps are simply regular apps on GrapheneOS, you install them within a specific user or work profile
  and they're only available within that profile."* No `com.google.android.gms` / `com.android.vending` line (exit status not recorded). The wire agrees (§1).
- **P1 — first install in user 10, not an update (🟡 HYPOTHESIS, strong):** the output has one `lastUpdateTime` (package level, `2026-10-07 05:26:38`) and two
  `firstInstallTime` lines (`2026-10-04 15:22:42`, `2026-10-07 05:26:38`). AOSP `services/core/java/com/android/server/pm/Settings.java` (main, fetched 2026-10-07):
  `pw.print(prefix); pw.print("  lastUpdateTime=");` once per package, then `for (UserInfo user : users) { … pw.print("  User "); pw.print(user.id); … pw.print("
  firstInstallTime="); …}` — one line per user; the list comes from `UserManagerService.getUsersInternal`, which walks `mUsers` (a `SparseArray`, ascending id). So
  the first line is user 0's and the second **user 10's**: in user 10 the package was first installed at the same second as 1.1.0 — 1.0.1 was not installed
  there any more (or never was) when 1.1.0 came in. `CAP-068-FINDINGS.md` §0's 🟡 "installed fresh" for 1.0.1 is the same pattern. ⚪ GrapheneOS keeps AOSP's
  dump code. Settling: `adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds | grep -A12 "User 10:"`.
- **"Permissions (start): … NOT_REQUESTED -> GRANTED" says nothing about a fresh install (🟢, code):** `MainActivity.kt:162–164` initialises the state to
  `NOT_REQUESTED` for both, and `refreshPermissions("start")` (`:325`, logging at `:237–245`) logs any change — so every process start with both permissions
  already granted logs exactly this line (E1 1, E4 1). `CAP-068-FINDINGS.md` §0 used it as evidence for a fresh install; that half of its argument does not
  hold (its other half, 0 CDM associations removed, does). Proposal for the next rewrite of that finding, no change made here.
- **HCI files:** A = `CAP-070-btsnoop_hci1.log` + 272 packets (`python3 -c "A=open('CAP-070-btsnoop_hci1.log','rb').read();B=open('CAP-070-btsnoop_hci2.log.last','rb').read();
  print(len(A),len(B),B[:len(A)]==A)"` → `222764 237265 True`). Boundaries: P2's Bluetooth off/on (film 06:10:44/06:10:50; Z ends 06:10:46.252, A starts
  06:10:52.690) and BF-24/25 (A5283 ACL `0x16` 06:37:28.299, A ends 06:37:28.403; B starts 06:39:36.576, film on at 06:39:34). No Buds traffic between files.
- **Exports:** E1 (958 lines) ⊂ E2 (963) ⊂ E3 (974), line by line; E4 (56) is a new process. The toasts on film: 958, 963, 974, 56. Each export ends at the
  ring buffer's state when *Export* was tapped (E1's last line 06:32:11.411; tap 06:32:42).
- **Processes:** 23551 (05:26:45–05:26:52, 7 s after the install), 23764 (05:44:14–06:09:27, the unfilmed rehearsal), 28098 (06:09:28.851–06:37:28.408, the filmed
  run), 525 (from 06:37:57.540, `Zygote: Process 525 created`, after the icon tap at 06:37:56). How 28098 ended is not logged (no system log) — the force-stop is
  the maintainer's step. 🟢 No `FATAL`, `ANR`, crash-buffer, "Decoder fault", "Unexpected", "Late WriteSetting answer dropped", "Maestro request held" or "Safe Mode"
  line (`grep -c -i -E "<pattern>"` over E3, E4, L1, L2 → 0 each; positive control: the same command finds "Settings read" 11 times in E3).
- **The two `SIGQUIT`s:** L1 508–510 (06:34:48.315, PID 28098) and L2 597–599 (06:40:31.955, PID 525) — "Signal Catcher … reacting to signal 3" / "Wrote stack
  traces to tombstoned", ART's response to a stack-dump request, not a crash: both processes continued (E3 runtime info 06:34:51.699; L2 to 06:40:35). 🔴 What sent
  them: nothing visible on film at 06:34:48 (Debug tab idle); the second came 2 s before the system log viewer showed the app's log (film 06:40:34). 🟡 a tool on
  the maintainer's side (an `adb` dump or the log viewer). Settling: the system log of the next run.
- **Film and audio:** the sound track has no sample description (the `trak` with handler `soun` has no `stsd`; `ffprobe` → `codec_name=unknown`,
  `sample_rate=0`) — **no audio**. The BF-9/BF-11/BF-12 observations ("did the case sound", "what you hear") are therefore not recorded, and nothing is claimed about
  them (ADR-054/055).

## 1. ACLs, the Message Stream and the Definition-of-done negative

| ACL | Start | End | Note |
|---|---|---|---|
| Z1–Z8 | Z159 05:44:40 … Z3617 05:58:08 | Z4078 05:59:36 `0x13` | the rehearsal (8 MAESTRO sessions) |
| A1 | A157 phone page 06:11:26.924 (Connect) → A159 | A1044 06:15:25.096 `0x13` | Left back into the case |
| A2 | **A1049 06:15:41.516, Buds-initiated** (no `Create Connection`) | A1526 06:15:58.630 `0x13` | the Buds opened the multiplexer (A1266) and HFP on DLCI 0x08 (A1272, `AT+BRSF` A1293) |
| A3 | A1529 page 06:16:11.964 (pull) → A1531 | A2022 06:16:52.709 `0x13` | |
| A4 | A2029 06:16:59.089, Buds-initiated | A2553 06:17:27.803 `0x13` | |
| A5 | A2556 page 06:17:43.045 (pull) → A2558 | A3135 06:19:35.885 `0x13` | |
| A6 | A3140 06:20:24.745, Buds-initiated | A4280 06:29:59.280 `0x13` | BF-13 … BF-18 |
| A7 | A4283 page 06:30:01.958 (Connect) → A4285 | A5283 06:37:28.299 `0x16` | Bluetooth off |
| B1 | B153 page 06:39:37.214 (Bluetooth on) → B159 | B819 06:41:04.562 `0x13` | after the film from 06:41:00 |
| B2 | B829 page 06:41:47.171 → B831 | open at B's end | after the film |

- 🟢 **No Play-services claim:** `tshark -r <log> -Y 'btrfcomm && frame contains 03:08:00:02:01:25' | wc -l` → **0** in Z, `CAP-070-btsnoop_hci1.log`, A and B (exit 0 each);
  positive control `CAP-066-btsnoop_hci.log.last` → 22, `CAP-066-btsnoop_hci.log` → 4 (exit 0). **22 Message Stream claims, all the app's** (Z 8, A 11 — one of them
  on DLCI 5, A1430 —, B 3): every phone `SABM` on DLCI 4/5 is followed by the app's `08 11 00 00` (22 of 22) and matches an export "RFCOMM channel 0x04 connected" line;
  the app sent no ANC `Set` and no Ring in this run. No phone frame on DLCI 0x0a/0x0b (`-Y 'bthci_acl.chandle==0x000b && (btrfcomm.dlci==10 || btrfcomm.dlci==11)'`
  → 0 in Z, A, B; positive control `CAP-066-btsnoop_hci.log.last` as in `CAP-068`). DLCI 0x08 appears only in ACL A2, opened **by the Buds**, carrying HFP
  (`AT+BRSF=921`, `+CIND: ("call",…`; A1293, A1309) — DLCIs are session-local; the app sent nothing there (its frames are only on DLCI 2/3/4/5).
- 🟢 Every claim starts with the Buds' Device Information burst (`03 0a`, `03 01 00 03 da 2d b1`, `03 02`, `03 09 … Revision 6`, `07 10 00 00`, `07 34`,
  `03 03` ×3), as in `CAP-068`; the Debug tab counted them as unidentified (5 → 61 → 6 after the new process).
- 🟢 ANC `Notify` Settable (23 `Notify`s in 22 claims): `00` in 19 (every one with both buds docked, or one docked and one on the table/in the hand), `e8` in 4 — A2365
  06:17:00.53 (Left out ≈ 1 s), A4208 06:29:13 (both out of the case, the Left just taken off), B493 06:39:41 (Right out), B1393 06:42:05 (after the film). Consistent
  with ADR-049 item 3 (🟡 `00` ⇒ no bud worn); no counter-example; wear itself is not on film.

## 2. The Connect read — twelve settings (BF-1, BF-8, BF-25; T1, T8, O8)

🟢 **22 of 22 sessions** (Z 8, A 11, B 3): the Buds' unsolicited `GetSoftwareInfo` → `ReadSetting 4:16` → `ReadSetting` **2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28,
29** in that order, each answered by an OK `RESPONSE 4:{N:…}` → one `SubscribeRuntimeInfo` (e.g. A655 → A733). One exception in the order count: Z's first session
read the twelve twice (Z298–Z333, Z649–Z707: a pull 7 min later on the same channel). **No read of any other field, no request on DLCI 0x08/0x0a**
(`grep -h "dir=Sent" <pwrpc output> | grep -v -E "ReadSetting|WriteSetting|SubscribeRuntimeInfo"` → nothing; the phone sent 409 pw_rpc requests in Z/A/B, all of these three methods).

| Session (announcement) | Ch | 11 | 15 | 27 | 28 | 29 | Latency per read (excl. 16) |
|---|---|---|---|---|---|---|---|
| A260 06:11:27.541 | 21 | 1 | 1 | 1 | 1 | 2 | 31–60 ms |
| A655 06:12:18.523 | 21 | 1 | 1 | 1 | 1 | 2 | 37–60 ms |
| **A824 06:15:03.064 (BF-8)** | 21 | **0** | **0** | **0** | **0** | **1** | 35–62 ms |
| A961 06:15:19.856 | 19 | 0 | 0 | 0 | 0 | 1 | 43–60 ms |
| A1396 06:15:42.443 (DLCI 3) | 19 | 0 | 0 | 0 | 0 | 1 | 27–67 ms |
| A1632 06:16:12.462 | 19 | 0 | 0 | 0 | 0 | 1 | 26–56 ms |
| A2297 06:17:00.065 | 19 | 1 | 1 | 1 | 1 | 2 | 19–65 ms |
| A2665 06:17:43.549 | 19 | 1 | 1 | 1 | 1 | 2 | 37–68 ms |
| A3402 06:20:25.726 | 19 | 1 | 1 | 1 | 1 | 2 | 22–65 ms |
| A4153 06:29:12.361 | 21 | 1 | 1 | 1 | 1 | 2 | 55–234 ms |
| A4383 06:30:02.932 | 21 | 1 | 1 | 1 | 1 | 2 | 27–70 ms |
| **B416 06:39:40.934 (BF-25)** | 21 | 1 | 1 | 1 | 1 | 2 | 77–273 ms |
| B935 / B1347 (after the film) | 21 / 19 | 1 | 1 | 1 | 1 | 2 | 25–83 ms |

Command: `python3 scripts/pwrpc_decode.py --handle 0x000b <log>` and a scratch pairing of each `REQUEST ReadSetting` with the next `RESPONSE ReadSetting`.
Raw example A880 `7e 00 a5 03 2a 04 22 02 58 00 08 01 10 15 1d ea 71 de 7d 5e 25 51 ae d0 ae …` = `4:{11:0}`.

- 🟢 **BF-8: the Buds stored all five** — the reads after the off-writes return `11:0`, `15:0`, `27:0`, `28:0`, `29:1` (A880, A886, A898, A901, A904); after
  the on-writes `11:1`, `15:1`, `27:1`, `28:1`, `29:2` (A2297 onward). Field 29 is 1/2, never 0 (ADR-052).
- 🟢 **Latency:** 8–273 ms per read across the 22 sessions; the slowest pass (B416, after Bluetooth on, 77–273 ms) took 1.87 s from the announcement to the
  subscription — inside `ai-sessions/0074` §A.4's estimate (≈ 0.7 s typical, ≈ 2.9 s at the slowest observed sample × 12); no read reached
  `SETTING_READ_TIMEOUT_MS` (2,000 ms).
- 🟢 Other read values every session: 2 = 1, 4 = 1, 7 = `{1:{4:{1:5}} 2:{4:{1:5}}}`, 12 = `{1:1 2:1 3:1 4:0}`, 19 = 0, 22 = 1; 17 = 0 until the balance
  drags, then 7 (A4153 onward); 16 = `[-1, 0, 4, 2, 0]` until 06:31:45, then `5:3.72` (B428).
- **S6 ("—" in the first second):** 🟢 seen on film at 06:17:43 (Sound, the Volume EQ row "—" before its read, pull on channel 19) and 06:20:24.5–25.9 (Controls,
  "—" in place of every switch until the reads landed, A3453–A3487); not caught at 06:11:27 / 06:12:18 (the first Controls frame already showed values, 10-fps strip).

## 3. The five switches — every write (BF-4 … BF-20; T3 … T10; ADR-052 … ADR-055)

🟢 **89 `WriteSetting` requests in Z/A** (Z 12, A 77) — **every one byte-identical to the skeleton's expected frame where the table has one, every one answered by the
empty `RESPONSE`** (`7e00a503080110151dea71de7d5e251d9a8c9e036d4ed87e` on 21, `7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e` on 19), none with an error
status, none unanswered. Command: the scratch comparison of each request's reassembled pw_hdlc frame with the table (the skeleton's "Steps" section) and the next
`RESPONSE WriteSetting`; CRC-32 OK on all 951 pw_hdlc frames (Z 318, A 540, B 93).

| Write | Channel | Frames (request → `RESPONSE`) | Bytes vs table | Film |
|---|---|---|---|---|
| `4:{11:0}` Multipoint off | 21 | A752 → A755 (06:14:09.90 → .96); Z771 | = `CAP-069` 3161 (real) | switch off 06:14:10 |
| `4:{29:1}` head gestures off | 21 | A762 → A765; Z779 | = `CAP-069` 2492 | off 06:14:18 |
| `4:{28:0}` / `4:{27:0}` | 21 | A770 → A773, A777 → A780; Z789, Z797 | = `CAP-058` 5623 / 5680 | 06:14:34 / 06:14:40 |
| `4:{15:0}` Volume EQ off | 21 | A785 → A788, **A4770 → A4772**; Z805 | = `CAP-041` 2461 | 06:14:52; 06:30:46 |
| `4:{15:1}` Volume EQ on | **21** | **A4800 → A4802 (06:30:57.07 → .16)** | **`…2a04220278019977e84e7e` = the derived frame — first capture** | on 06:30:58 |
| `4:{15:1}` / `4:{15:0}` | 19 | A1953, A3075 / A3072; Z2018, Z3526 / Z3510 | = `CAP-022` 1895 / 1871 | 06:16:18, 06:18:52 / 06:18:38 |
| `4:{11:1}` Multipoint on | **19** | A1989, A3679, A3902; **Z2051 (05:53:47, first)** | **`…2a04220258010bbf9ab37e` = derived** | 06:16:26, 06:21:24, 06:26:02 |
| `4:{11:0}` Multipoint off | **19** | **A3668 → A3671 (06:21:20.25)**, A3896 | **`…2a04220258009d8f9dc47e` = derived — first capture** | 06:21:20, 06:25:51 |
| `4:{29:2}` head gestures on | **19** | A1998, A3685, A3910; **Z2048 (05:53:45, first)** | **`…2a052203e801024687d31d7e` = derived** | 06:16:32, 06:21:46, 06:26:17 |
| `4:{29:1}` head gestures off | **19** | **A3614 → A3616 (06:20:30.50)**, A3690 | **`…2a052203e80101fcd6da847e` = derived — first capture** | 06:20:31, 06:21:49 |
| `4:{28:1}` / `4:{27:1}` | 19 | A1992, A1995; Z2054, Z2057 | = `CAP-024` 2023 / 2084 | 06:16:28 / 06:16:29 |
| `4:{17:7}` balance Right 4 | **19** | **A3747 (06:22:58.19, first)**, A3770, A4019, A4058 | **`…2a052203880107e9b86e257e` = derived** | "Right 4" 06:23:17, 06:27:48 |
| `4:{17:v}` other drag values | 19 | 49 more (A3707 … A4055): v = 0, 8, 9, 11, 12, 13, 14, 15, 16, 17, 19, 20, 30, 31, 50 | no table row; each OK | drags 06:22–06:27 |
| `4:{16:{…5:-4.26}}` / `5:3.72` | 21 | A4829 → A4831, A4832 → A4834 | EQ (ADR-020/034) | Upper treble 3.7, 06:31:44 |

- 🟢 **The three request forms no capture held (lead 4) are on the wire, byte-identical to the derived frames, and answered OK:** fields 11 and 29 on channel 19
  (four frames: A3668, Z2051/A3679, A3614, Z2048/A3685) and `4:{15:1}` on channel 21 (A4800); also the channel-19 balance `4:{17:7}` (A3747). Raw A4800
  (scratch parser, CRC OK): `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 04 22 02 78 01 99 77 e8 4e 7e`; A4802 `7e 00 a5 03 08 01 10 15 1d ea 71 de 7d 5e
  25 1d 9a 8c 9e 03 6d 4e d8 7e`. These frames can now replace the labelled structural fixtures (`Settings074`) and ADR-055's `// TODO(verify)` — an `android/`
  change, **proposed for a follow-up session** (§9), not made here.
- 🟢 **Only after the answer:** each export's "Setting N written" line comes ≤ 10 ms after its `RESPONSE`'s HCI time (e.g. A755 06:14:09.959 → E1 107 06:14:09.964; A4802 06:30:57.160 → E3 948 06:30:57.165); the
  switches on film moved after the tap within the 2-s/1-s sampling. 🔴 The 30–490 ms between request and answer is below the film's resolution here (no screen
  recording); "moved only after the `RESPONSE`" is the code's rule (`writeSetting`, `ai-sessions/0074` §C–G) and its unit test, not shown frame-by-frame on film.
- 🟢 **No mirrored push, no SASS:** the app does not subscribe to settings changes, and no `SERVER_STREAM SubscribeToSettingsChanges` occurs (0 in Z/A/B). No
  SASS `07 11 00 04 01 02 98/b8 00` after any Multipoint write — no Message Stream claim was open then (the app releases DLCI 4 ≈ 1.6 s after each Connect, e.g.
  A736; the Multipoint writes came ≥ 1 min later). ADR-053's SASS byte is not testable without a held claim (expected; ⚪ that the Buds would send it).
- 🟢 **Head gestures: nothing on DLCI 0x08 from the phone** in ACL A1/A3/A6/A7 (no DLCI 0x08 at all in those ACLs); no dialog in the app. GSND Code `0x16` cannot
  occur (no Google app channel in this user).

## 4. The channel choice and lead L-1 (BF-13, BF-17 … BF-19; `INEAR-005`)

Announcements (`GetSoftwareInfo` `call_id 0xFFFFFFFF`, `10 13` = 19 / `10 15` = 21) against the case slots (upper = Left, lower = Right, EVENT-NOTES) and Option F's
per-bud field 2 (2 = charging):

| Announcement | Ch | State (film + runtime info) | Fits §2.2a 🟢 "only Left out ⇒ 19, only Right out ⇒ 21"? |
|---|---|---|---|
| A961 06:15:19.86, A1396, A2297 | 19 | **only the Left out** (film 06:15:16, 06:15:38, 06:16:58; A1040, A2383 6.2 = 1, 6.3 = 2) | ✓ (3 more samples) |
| A3402 06:20:25.73 (BF-13) | 19 | **only the Left out** (film 06:20:22; A3488 6.2 = 1, 6.3 = 2) | ✓ |
| A260, A655, A824, A4383 | 21 | both docked (runtime info with both field 2 = 2, e.g. A334) | — (not covered) |
| A1632, A2665 | 19 | both docked (A1707, A2731 both 2) | — (not covered) |
| A4153 06:29:12.36 (BF-18) | 21 | **both out of the case, the Left just taken off** (§4 below) | — (L-1) |
| B416 06:39:40.93 | 21 | **only the Right out** (film 06:30:18; B528 6.2 = 2, 6.3 = 1) | ✓ |

- 🟢 **4 + 1 new samples of the 🟢 rule** (Left only ⇒ 19 ×4, Right only ⇒ 21 ×1), no counter-example. With **both docked** the Buds announced 21 four times and
  19 twice — the rule does not cover that state, and this capture shows it is not fixed.
- 🟢 **BF-18 — the Left taken off with both buds out of the case, on 19 ⇒ Buds `DISC` + 21.** Session A3402 (ch 19) ran from 06:20:25; the Right left the case at
  06:23:44 (A3788) and both stayed out (A4098 06:28:42 `6:{2:{1:100 2:1} 3:{1:100 2:1}}`, no 6.1). At 06:29:08–10 a bud was taken off out of view and held at the top
  of the frame; it went into the **upper (Left)** slot at 06:29:32 (A4251 6.2 = 2). The Buds closed MAESTRO with the ACL up — A4115 Buds `DISC` DLCI 2 06:29:10.004
  (A4116 phone `UA`) — and the app's AFTER_LOSS re-open got announcement **21** (A4153 `…10 15 1d ea 71 de 7d 5e 25 44 fa 99 71 38 ff ff ff ff 0f e8 a9 58 66 7e`;
  Info "Control channel: 21" film 06:29:12–16). Its claim's Settable read `e8` (A4208). This is the predicted 19 → 21 (`PROTOCOL.md` §2.2a, Updates of 2026-10-01 and
  2026-10-03), with the bud identified — but **whether both buds were in the ears is not shown** (head out of view; Settable `e8` says at least one worn, 🟡
  ADR-049). Command: `tshark -r CAP-070-btsnoop_hci2.log.last -Y "bthci_acl.chandle==0x000b && btrfcomm.frame_type==0x43 && btrfcomm.dlci==2 && frame.p2p_dir==1"` →
  A913, A4115 (exit 0) and the scratch decoder for A4153.
- 🟢 **No `DISC` on the Right's five changes with the Left out** (06:23:44 out, 06:25:05 in, 06:25:12 out, 06:28:12 in, 06:28:42 out; A3788, A3823, A3839, A4079, A4098)
  — session A3402 stayed on 19. Together with BF-18: on 19 the Buds closed MAESTRO on the Left's change only, the mirror of `CAP-066` on 21 (closed on the Right's
  changes only). 🟡 **HYPOTHESIS (strengthened):** the channel names the bud hosting the link, and the Buds re-home the link when that bud leaves. Settling: the
  same run with the head in view (P3), or a wear indicator on the wire (none without the Google app's DLCI 0x08).

## 5. A write during a re-open (BF-13, C12) and the screen-reader text (BF-24, T11)

- 🔴 **C12 not tested:** the tap came after "ready" (re-open ready 06:20:25.72, tap at 06:20:29–31 on film, write A3614 06:20:30.50 OK). No "The app's channel is being
  reopened" text appeared, and no write was sent other than for the visible taps (the 77 writes in A ↔ the film's taps and drags; none in the 06:20:22–25 window).
- 🟢 **"—" on film:** after the force-stop with Bluetooth off, Controls showed "—" for "Use touch controls" and "Use head gestures" (film 06:38:04–06, the rest scrolled
  out of view) and Sound showed the five EQ bands without values (06:39:04–10); during the 06:20:24 re-open every switch showed "—" (§2). 🔴 **The screen-reader
  description** ("Not read from the Buds yet") cannot be seen on film and **no `uiautomator` dump exists** — T11 is not settled; the unit tests (`ControlsScreenTest`,
  `EqScreenTest`, mutation M9 of `ai-sessions/0074`) remain the only evidence.

## 6. Session ends, re-opens and processes (ADR-044)

| # | Time | Cause on the wire | App log | Re-open |
|---|---|---|---|---|
| 1 | 06:12:07.051 | phone `DISC` (A614) | Disconnect tap | Connect 06:12:18 |
| 2 | 06:14:57.789 | phone `DISC` (A790) | Disconnect (BF-8) | Connect 06:15:02.9 |
| 3 | 06:15:18.226 | Buds `DISC` DLCI 2 (A913), ACL up — Left out of the case | "the Buds closed the channel (provisional …)" (E1 168–172) | AFTER_LOSS 06:15:19.73 → 19 |
| 4 | 06:15:25.096 | ACL `0x13` (A1044) — Left back, both docked | "Automatic re-open skipped: … 5296 ms after an automatic re-open" (E1 224); "Android's link went down" | LINK_BACK 06:15:41.8 (Buds' ACL) |
| 5 | 06:15:58.630 | ACL `0x13` (A1526) | link down | pull 06:16:11.96 |
| 6 | 06:16:52.709 | ACL `0x13` (A2022) | link down | LINK_BACK 06:16:59.4 |
| 7 | 06:17:27.803 | ACL `0x13` (A2553) | link down | pull 06:17:43.0 |
| 8 | 06:19:35.885 | ACL `0x13` (A3135) — both into the case | link down | LINK_BACK 06:20:25.1 |
| 9 | 06:29:10.004 | Buds `DISC` (A4115), ACL up — the Left taken off | provisional → Buds closed | AFTER_LOSS 06:29:11.52 → 21 |
| 10 | 06:29:59.280 | ACL `0x13` (A4280) — both docked | link down | Connect 06:30:01.95 |
| 11 | 06:37:28.199 | phone `DISC` (A5275), ACL `0x16` | "Session loss cause: Bluetooth was switched off on this phone" (L2 394) — final | LINK_BACK 06:39:39.9 (new process) |
| — | 06:37:28.4–57.5 | process 28098 ends (force-stop per the skeleton) | — | process 525 at the icon tap |

- 🟢 Every loss was classified and recovered: Buds-side closes with the ACL up re-opened after 1.5 s (#3, #9); one loss within 10 s of an automatic re-open was
  not re-opened (#4, ADR-044's chain guard, `SessionReopener.kt:101–104`, `CHAIN_GUARD_MS = 10_000L` `:137`) — the next Buds ACL re-opened it; every ACL loss came
  back by LINK_BACK when the Buds reconnected, or by a pull/Connect while both stayed docked. No session was lost without a message.
- 🟢 **The first LINK_BACK attempt fails on a multiplexer collision** (#4, #6, #8, #11 is clean): E3 232/371/532 "RFCOMM channel 0x02 connect failed after 84–106
  ms (attempt 1/3)", and on the wire the phone's `SABM` DLCI 0 crossed the Buds' own (A1252/A1266) or was repeated (A2241/A2274, A3351/A3385); attempt 2 succeeded
  400 ms later each time (`RfcommBudsTransport` retry, `DEFAULT_RETRY_DELAY_MS = 400L`, `RfcommBudsTransport.kt:386`). As designed (`ai-sessions/0039`).
- 🟢 **Bluetooth off/on (BF-24/25):** final cause line, no "provisional"; the new process re-opened by itself on Bluetooth on (L2 478, trigger `LINK_BACK`) and read
  the twelve settings.

## 7. Battery and case

- 🟢 Film 06:11:27–28: Left 100 % ⚡, Right 100 % ⚡, Case "Battery unavailable … Not reported yet" then **89 %** — A286 `03 03 00 03 e4 e4 ff` (both 100, charging)
  06:11:27.582; A334 runtime info 6.1 = 89 06:11:28.308. Film 06:29:50: Left 100 % ⚡, Case 87 %, Right 100 % — A4251 06:29:34 `6:{1:{1:87 …} 2:{1:100 2:2} 3:{1:100
  2:1}}`. Case 90 → 89 (Z2533 05:54:37) → 87 (A3656 06:21:11) → 86 (B813 06:41:01). Every shown value equals the last wire value before it.
- 🟢 6.1 (Case) present exactly when at least one bud reports field 2 = 2 (Option F): absent in A3019, A3788, A3797, A3839, A4098, B1278 (both buds out), present
  otherwise. One packet against the rule's 🟡 "charging = in the case": A3788 06:23:42.97 `6:{2:{1:100 2:1} 3:{1:100 2:2}} 7:{1:0 2:0 3:0}` — Right field 2 = 2 but
  no 6.1 and 7.1 = 0, 1 s before the Right left the case on film (06:23:44); the next packet (A3797) has both 1. 🔴 a transitional packet; one sample.

## 8. What differs in a secondary user without Play; HFP, AVRCP, LE

- 🟢 Nothing in the app's behaviour depends on it: same reads, writes, ACKs and re-opens as in the Owner-user captures; the Message Stream was never contended
  (no Play services), so the app's claim always succeeded on attempt 1 (22 of 22). The ANC tab showed the last claim's Settable (`00`, A4416) on a later visit
  (film 06:32:24) — the known "tap a mode checks again first" design.
- 🟢 HFP: `AT+BIEV` pushes (`frame contains "AT+BIEV"`: Z 45, A 70, B 10); AVRCP frames (`btavctp or btavrcp`: Z 53, A 66, B 15) — the radio stream at 06:18.
  LE: one LE link to another device (Z3077 `c8:cc:…`, ended Z4623 `0x16`); none to the Buds.

## 9. Defects, observations and the release classification

| # | What | Class | Evidence |
|---|---|---|---|
| 1 | The screen-reader text of "—" is unverified on a phone (no dump) | **open test item**, not a defect shown | §5 |
| 2 | C12 (a tap during a re-open) not exercised | open test item | §5 |
| 3 | Case-sound and Volume-EQ audibility not observable (no audio track) | open test item (observations, not app claims) | §0 |
| 4 | The first LINK_BACK attempt collides with the Buds' own multiplexer `SABM` (4 times), the retry succeeds | observation — as designed | §6 |
| 5 | One loss within 10 s of a re-open is not re-opened; the Buds' next ACL re-opened it | minor — ADR-044 as designed (already a 1.0.x known behaviour) | §6 #4 |
| 6 | Two `SIGQUIT` stack dumps, cause unknown | 🔴 not an app defect shown (the process continued) | §0 |
| 7 | P1: 1.1.0 was a first install in user 10, not the planned update over 1.0.1 | test-procedure gap; the update path stays untested on hardware | §0 |

**No heavy defect:** no crash or ANR in the app's buffers; 89 of 89 writes (all five switches, both channels, the balance, the EQ) were answered by the empty OK
`RESPONSE` and their values read back on the next connections; every write matched its expected bytes; no request outside the user's actions and the
documented Connect read; nothing from the app on DLCI 0x08/0x0a; no Safe Mode on `release_5.203`; every session end recovered.

## 10. Protocol correlation (per channel)

- **MAESTRO (DLCI 2/3):** as ADR-034/036/043/045/052–055; 951 of 951 pw_hdlc frames CRC-OK; every `RESPONSE` status OK; no `CLIENT_ERROR`/`SERVER_ERROR`
  (`grep -h "status=" <pwrpc output> | grep -v -c "status=OK"` → 0; positive control `grep -c "status=OK"` → 318/540/93). The three channel forms not seen before
  are now real bytes (§3). Field 17: `sint32` zigzag (`17:7` = −4 = "Right 4", `17:50` = +25).
- **Message Stream (DLCI 4/5):** only the app's `Get`; Settable as ADR-049 (§1).
- **DLCI 0x08:** HFP when the Buds open the multiplexer (A2); no GSND.
- **§2.2a channel:** §4.

## 11. Improvements (proposals only — nothing under `android/` was changed)

1. **Replace the labelled structural fixtures with real bytes (lead 4).** `SettingsFixtures.kt` `Settings074`: the channel-19 Multipoint off/on (A3668, A3679),
   head gestures off/on (A3614, A3685) and the channel-21 Volume EQ on (A4800, answer A4802), plus the channel-19 balance `17:7` (A3747) — each with frame number,
   time and command in the comment (`AGENTS.md` §11); remove the "labelled" markers in `SettingsCodecTest.kt:282` and the `// TODO(verify)` on
   `SettingsCodec.FIELD_VOLUME_EQ` (`SettingFrame.kt:82`, ADR-055), and update that KDoc's placeholder path to this folder. Test: the existing byte-for-byte codec
   tests, now on real fixtures. Hardware re-test: none needed (this capture is the re-test). A follow-up FEATURE session, after the release (or before it, if the
   maintainer wants the tests in 1.1.0 — it does not change the shipped code).
2. **Test procedure for the next app run:** P4 screen recording for S6/C12; the two `uiautomator` dumps (T11); a rotation during the save dialog (S12); *Read EQ
   again* (H5); head in view for L-1; P1 with `dumpsys … | grep -A12 "User 10:"`; the update path tested by installing over a kept older version in the same user.
3. **`CAP-068-FINDINGS.md` §0:** drop the "Permissions (start)" line as evidence of a fresh install (§0) — a findings rewrite, proposed.

## 12. Open questions

- 🔴 The screen-reader description on a phone (T11) — the dumps.
- 🔴 C12 on hardware.
- 🔴 What sent the two `SIGQUIT`s.
- 🟡 BF-18 with both buds **worn** (head in view) — the L-1 settling experiment.
- 🟡 P1: first install in user 10 (settle with the `User 10:` block).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-070-2026-10-07_06-10-41_06-40-59-Group_BF/CAP-070-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-070-2026-10-07_06-10-41_06-40-59-Group_BF/CAP-070-FINDINGS
