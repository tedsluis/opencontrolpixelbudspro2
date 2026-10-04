# Findings: `CAP-068` (Group BD — the release-signed 1.0.1 APK in a GrapheneOS secondary user without Google Play: the hardware test of the `ai-sessions/0069` hotfix)

Standardized, evidence-based extraction from the four HCI snoop logs, the two films (`CAP-068-recording1.mp4`, `CAP-068-recording2.mp4`), the five debug
exports and the one app logcat, per `ai-sessions/0070`. There is no system log. The timeline these findings refer to is `CAP-068-EVENT-NOTES.md`.

- 🟢 **FACT** — directly observed in this capture (frame number, log line or film time given).
- 🟡 **HYPOTHESIS** — plausible reading, not yet independently confirmed; the settling experiment is named.
- ⚪ **ASSUMPTION** — not tested here.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-068` · **Date:** 2026-10-04, film overlay 07:22:57–07:40:46 and 07:40:52–07:54:10 (phone = overlay − 0…0.5 s) · **Firmware:** 🟢
`release_5.203` (15 of 15 announcements, 45 entries; Info tab on film) · **Phone:** Pixel 9a, GrapheneOS, Android 17 `CP3A.260905.009`, **secondary user 10
(`full.secondary`), no Google Play** · **App under test:** OpenControl for Pixel Buds Pro 2 **1.0.1, build `e1fc886`** (release-signed, versionCode 10001) ·
**HCI logs:** "Z" = `CAP-068-btsnoop_hci.log.last` (1,413 packets, 07:11:25.133–07:23:02.020, before the film), "A" = `CAP-068-btsnoop_hci2.log.last` (2,314,
07:23:07.514–07:34:58.304), "B" = `CAP-068-btsnoop_hci2.log` (3,563, 07:35:44.243–07:56:06.298); `CAP-068-btsnoop_hci.log` is a byte-for-byte prefix of A
(2,219 packets) and is not counted · **Exports:** E1…E5, each a prefix of the next; E5 = `…-075355.txt` · **Buds:** `04:00:6e:cf:6e:07`, classic handle
`0x000b` for all six ACLs; no LE link.

Commands used throughout (rule 4a; all on handle `0x000b`, shown matching the Connection Complete events A160, B159, B1092, B2844): the control-frame
inventory `tshark -r <log> -Y "bthci_acl.chandle==0x000b && btrfcomm && (btrfcomm.frame_type==0x2f || btrfcomm.frame_type==0x63 || btrfcomm.frame_type==0x43
|| btrfcomm.frame_type==0x0f)" -T fields -e frame.number -e frame.time_epoch -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type`; the HCI link events
`-Y "bthci_evt.code==0x03 || bthci_evt.code==0x05 || bthci_evt.code==0x04 || bthci_cmd.opcode==0x0405 || (bthci_evt.code==0x3e &&
bthci_evt.le_meta_subevent!=0x0d && bthci_evt.le_meta_subevent!=0x02)"`; MAESTRO with `python3 scripts/pwrpc_decode.py --handle 0x000b <log>` (exit 0) plus a
scratch CRC-32 check per pw_hdlc frame on DLCI 2/3 per direction; the Message Stream with a scratch message-level parser over `tshark … -Y
"bthci_acl.chandle==0x000b && btrfcomm && (btrfcomm.dlci==4||btrfcomm.dlci==5)" -e frame.number -e frame.time_epoch -e frame.p2p_dir -e btrfcomm.frame_type -e
btrfcomm.len -e data.data` (`[Group][Code][Len:2 BE][Value]` per direction, a claim = a phone `SABM`), cross-checked with `python3
scripts/message_stream_tally.py -v` (`CAP-068-btsnoop_hci2.log.last {'get': 19, 'notify_00': 4, 'notify_e8': 36, 'set': 11, 'ack': 11}`, `CAP-068-btsnoop_hci2.log
{'get': 12, 'notify_e8': 11, 'notify_00': 3, 'set': 1, 'ack': 1}`). Scripts and their outputs are in the session scratchpad (`ai-sessions/0070` RESULT).

## 0. Capture integrity, build identity, logs (🟢 FACT unless marked)

- **Build:** the Info tab on film reads "App: 1.0.1, build e1fc886 (2026-10-04)" without "-dirty" (film 1 07:23:45–51, film 2 07:42:33–07:43:14); the logcat
  header reads `package: io.github.tedsluis.opencontrolpixelbuds:10001`. `e1fc886` is the tip of `origin/maintenance/0069` (`git log -1`), so `git diff
  e1fc886..origin/maintenance/0069 -- android` is empty — the tested build is the release candidate of `RELEASING.md` B1. `dist/1.0.1/…apk`: SHA-256
  `f9dce033…63b15e0f` (= its `.sha256`), certificate SHA-256 `a7530f5c…c79d8dcb` (`apksigner verify --print-certs`), `versionCode 10001`, its dex holds
  `e1fc886`/`2026-10-04` (`strings`). ⚪ that the installed file is that APK (no install log).
- **Install over 1.0.0 (P1):** 🟡 HYPOTHESIS that the app was **not** updated in place but installed fresh (or its data and the association removed): the first
  start asked for both runtime permissions (E5 7–8 `NOT_REQUESTED -> GRANTED`) and "Use different Buds" removed **0** CDM associations (E5 537), whereas
  `CAP-067` ended with an association (its E2 08:17:01.671). Both survive an update. Settling: the maintainer's memory of the install, or
  `adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds | grep -E "firstInstallTime|lastUpdateTime"` for user 10.
- **No Google Play in the test user:** P0 (maintainer's output, exit status not recorded) lists only `app.grapheneos.gmscompat`, `…gmscompat.lib`,
  `…gmscompat.config` — packages in GrapheneOS's own `app.grapheneos` namespace that match "gms" by name. grapheneos.org/usage ("Sandboxed Google Play", fetched
  2026-10-04): *"GrapheneOS has a compatibility layer providing the option to install and use the official releases of Google Play in the standard app sandbox."* and
  *"Since the Google Play apps are simply regular apps on GrapheneOS, you install them within a specific user or work profile and they're only available within that
  profile."* 🟡 that these three packages are that layer (the page names no package); either way they are not Google Play: no `com.google.android.gms`, no
  `com.android.vending` line. The wire agrees (§1).
- **HCI files:** `CAP-068-btsnoop_hci.log` (99,452 bytes) equals the first 99,452 bytes of `CAP-068-btsnoop_hci2.log.last` (106,821) — `python3 -c
  "A=open('CAP-068-btsnoop_hci.log','rb').read();B=open('CAP-068-btsnoop_hci2.log.last','rb').read();print(B[:len(A)]==A)"` → `True`. Two Bluetooth off/on
  cycles make the boundaries: P2 (film 1 07:23:01–06; Z ends 07:23:02.020, A starts 07:23:07.514) and BD-9/11 (A2312 ACL `0x16` 07:34:58.165, A ends
  07:34:58.304; adapter ON 07:35:45.635 E5 441, B starts 07:35:44.243). No Buds traffic falls between files and none is counted twice.
- **Exports:** E1 (435 lines) ⊂ E2 (481) ⊂ E3 (862) ⊂ E4 (869) ⊂ E5 (942), compared line by line; the toasts on film give the same five counts. One process.
  Debug mode was off until 07:23:54 and from 07:49:28 to 07:50:30 (film); E5 holds no hex line before 07:24:11.875 and none from 07:49:10.066 to 07:50:49.875.
- **Logcat:** one PID (16124), `wm_on_create_called` 05:11:42.204 UTC = E5's first line 07:11:42.250; no `FATAL`, ANR, crash-buffer, `tombstoned`, StrictMode,
  "Decoder fault" or "Unexpected error" line (`grep -n -i -E "FATAL|ANR|crash|tombstone|exception|StrictMode|Decoder fault|Unexpected"` → only line 6, the
  buffer list, and line 303, the failed connect's `IOException`). The main buffer starts 05:50:49.140.
- **Force-stop:** in no file. 🟢 The logcat ends 05:54:17.956 UTC after `wm_on_stop`; HCI log B shows the MAESTRO session still open at 07:55:59 (B3556 runtime
  info, B3559 the phone's credit) with no `DISC` up to 07:56:06.298 — so every file was saved before the force-stop, which is the maintainer's statement only.
- **Films and audio:** both near-silent (median −80 dB); the Find tone is audible film 2 07:48:44.8–07:49:03.5 and 07:50:48–07:51:12.4 (3-s pulses).

## 1. ACLs, the Message Stream and the Definition-of-done negative

| ACL | Start | End | Note |
|---|---|---|---|
| 1 | Z153 phone `Create Connection` 07:11:25.835 → Z159 | Z1108 07:20:58.391 `0x13` | before the film |
| 2 | A158 phone page 07:23:32.823 (the app's Connect) → A160 | A2312 07:34:58.165 `0x16` | Bluetooth off (BD-9) |
| 3 | B155 phone page 07:35:45.696 (Bluetooth on) → B159 | B933 07:40:26.648 `0x13` | buds docked during the pairing |
| 4 | B1090 phone page 07:41:19.844 (Connect after the pairing) → B1092 | B2836 07:52:15.153 `0x13` | buds docked, lid closed (BD-29 pre) |
| — | B2837 phone page 07:53:00.258 | B2840 07:53:05.400 **status `0x04`** (page timeout) | BD-29, no RFCOMM |
| 5 | B2842 phone page 07:53:42.474 (Retry) → B2844 | open at B's end | BD-30 |

- 🟢 **No Play-services claim:** `tshark -r <log> -Y 'btrfcomm && frame contains 03:08:00:02:01:25' | wc -l` → **0** in Z, A, `CAP-068-btsnoop_hci.log` and B, `tshark` exit 0 each;
  positive control `CAP-066-btsnoop_hci.log.last` → 22, `CAP-066-btsnoop_hci.log` → 4 (exit 0). **35 Message Stream claims, all the app's** (A 19, B 16): every
  phone `SABM` on DLCI 4 is followed by the app's `08 11 00 00` (31) or `04 01 00 01 xx` (4) and matches an E5 "RFCOMM channel 0x04 connected" line. No other
  client opened DLCI 4, and nobody opened DLCI 8/9/0x0a/0x0b (`-Y 'btrfcomm.dlci==8 || btrfcomm.dlci==10 || btrfcomm.dlci==11'` → 0 in Z, A, B; positive
  control `CAP-066-btsnoop_hci.log.last` → 234).
- 🟢 Every claim starts with the Buds' Device Information burst (session nonce `03 0a`, Model ID `03 01 00 03 da 2d b1`, BLE address `03 02`, `03 09 … Revision
  6`, `07 10 00 00`, `07 34 …`, `03 0b` and three `03 03` battery frames), e.g. A284–A290 (raw A284 `03 0a 00 08 1a 62 bd d1 7e c3 97 d6 03 01 00 03 da 2d b1 03 02
  00 06 79 11 43 32 e0 0f 03 09 00 0a 52 65 76 69 73 69 6f 6e 20 36 07 10 00 00`). The Debug tab lists these as unidentified (film: 5 → 200 by the end).
- 🟢 At the release of claim 6 the phone answered three Buds battery frames that crossed its `DISC` with `DM` (A1154 `DISC` 07:26:17.844, A1155/58/59 Buds
  `03 03 00 03 64 64 ff`, A1157/60/61 phone `DM`, A1164 `UA`) — the stack's handling, no app effect.

## 2. ANC by the release build — Definition of done 2 with frames (BD-1 … BD-8, BD-25)

**12 `Set`s, 12 ACKs, 0 NAK; 31 `Get`s, each answered by a `Notify`** (A + B, message-level parse above; 54 `Notify` frames: 47 × Settable `e8`, 7 × `00`).

| Film (phone) | Action | `Get` → `Notify` | `Set` | ACK → `Notify` | Screen |
|---|---|---|---|---|---|
| 07:25:50.5 | tab Transparency (BD-1) | A1029 → A1037 `08 13 00 04 01 e8 e8 08` | A1039 `08 12 00 14 01 e8 e8 80` + 16 × `00` | A1042 `ff 01 00 06 08 12 01 e8 e8 80` → A1043 `… 80` | ✓ Transparency 07:25:51 |
| 07:25:59.9 | tab Off (BD-3) | A1080 → A1088 `… 80` | A1090 `… 20` | A1093 → A1094 | ✓ Off |
| 07:26:15.8 | tab Noise cancellation (BD-4) | A1136 → A1144 `… 20` | A1146 `… 08` | A1149 → A1150 | ✓ |
| 07:26:37.1 | tile (BD-5) | A1205 → A1213 `… 08` | A1215 `… 80` | A1218 → A1219 | tile "Transparent" |
| 07:26:44.7 | tile | A1256 → A1264 `… 80` | A1266 `… 40` | A1269 → A1270 | "Adaptive" |
| 07:26:51.4 | tile | A1312 → A1321 `… 40` | A1323 `… 20` | A1326 → A1327 | "Off" |
| 07:26:57.8 | tile | A1364 → A1373 `… 20` | A1375 `… 08` | A1378 → A1379 | "Active" |
| 07:27:06.5 | tile (5th) | A1416 → A1424 `… 08` | A1426 `… 80` | A1429 → A1430 | "Transparent" |
| 07:29:30.4 | tab Noise cancellation (BD-7′) | A1901 → A1912 `… 80` | A1913 `… 08` | A1915 → A1916 | ✓ |
| 07:29:40.4 | tab Off (BD-8) | A1956 → A1965 `… 08` | A1967 `… 20` | A1970 → A1971 | ✓ |
| 07:29:46.0 | **tab Off again — the current mode** | A2022 → A2033 **`… 20`** | **A2034 `… 20`** | **A2036 `ff 01 00 06 08 12 01 e8 e8 20`** → A2037 `… 20` | unchanged |
| 07:49:42 | tab Transparency (BD-25) | B2619 → B2627 `… 08` | B2629 `… 80` | B2632 → B2633 | ✓ |

- 🟢 **Every `Set` followed this claim's `Get` and a non-zero Settable**, and every one was ACKed with the requested mode and confirmed by a `Notify` — the 1.0.1
  release build changes ANC without Google Play services, from the tab and from the tile, on film (Definition of done 2; `ANC-001`…`ANC-004`; `ANC-003` only from
  the tile, Adaptive was not tapped on the tab). The tile always stepped from the claim's fresh `Notify` (Transparent → Adaptive → Off → Active → Transparent).
- 🟢 **BD-6 (not worn):** both buds on the table (film 07:27:37) → tile claim 12 `Notify 01 e8 00 20` (A1508) and tab claim 13 `… 00 20` (A1616) → **no `Set`**
  (E5 245, 258 "ANC Set not sent: this claim's Notify reports no switchable mode (Settable 0x00)"); the tile toast (S11) and the tab's note on film.
- 🟢 **`ANC-006` — a tap on the current mode:** 1.0.1 sends the `Set` (it does not compare with the mode shown) and the Buds **ACK** it with the unchanged mode and
  send a `Notify` (A2034 → A2036 → A2037). No NAK `0x04` ("redundant device action"). One sample; the official app's behaviour is `CAP-069` IV.
- 🟢 **Settable vs wear** (ADR-049; no head-side mapping was possible, §5): the seven `00` came with both buds docked (A293, B1228, B1689, B2977), one docked and
  one in the hand (A848, right after the Right came out) and both on the table (A1508, A1616); every `e8` came after at least one bud was worn or out of the
  case and handled. Consistent with ADR-049 item 3 (🟡 "`00` ⇒ no bud worn"); no counter-example.

## 3. MAESTRO (ADR-034/036/043/045) — reads, writes, runtime info

- 🟢 341 of 341 pw_hdlc frames pass CRC-32 (A 132, B 209); every packet is `maestro_pw.Maestro`; status OK on every `RESPONSE`; no `CLIENT_ERROR`/`SERVER_ERROR`.
- 🟢 15 sessions, each: the Buds' unsolicited `GetSoftwareInfo` (`call_id 0xFFFFFFFF`, 3 × `release_5.203`) → the app's `ReadSetting` 4:16, 2, 4, 7, 12, 17, 19, 22
  → `SubscribeRuntimeInfo` (e.g. A261 → A318). Channels announced: 19, 21, 19, 21, 19, 19 (A) and 19, 19, 21, 21, 19, 21, 21, 19, 21 (B); the app used the matching
  address every time (`00 3b` / `00 4b`).
- 🟢 **EQ writes (BD-15/16/21), 6 of 6 OK:** B732 `4:{16:{0,0,0,0,0}}` (FLAT, BD-15) → B735 OK; B740 `[5,3,0,0,0]` (Heavy bass) → B743; B745 Flat → B747; B748
  `[−1,0,4,2,0]` (Vocal) → B750; B751 Flat → B753; B1733 `4:{16:{0,0,0,0,3.37}}` (Upper treble dragged) → B1735. Raw B732 (pwrpc_decode: `REQUEST ch=19
  method=WriteSetting | 4:{16:{1:f32(0.00) 2:f32(0.00) 3:f32(0.00) 4:f32(0.00) 5:f32(0.00)}}`) — the payload of `CAP-015` frame 2111 on channel 19. 🟢 The next
  connection read `4:{16:{… 5:f32(3.37)}}` (B1948) — the write persisted (H5's purpose, not via *Read EQ again*). The UI shows "3,4" (one decimal).
- 🟢 **Settings read at every connect:** 2 = 1, 4 = 1, 7 = `{1:{4:{1:5}} 2:{4:{1:5}}}`, 12 = `{1:1 2:0 3:1 4:1}`, 17 = 7 (zigzag → −4 = "Right 4", film 2 07:44:01),
  19 = 0, 22 = 1 (e.g. B1957–B1996) — matching the screen.
- 🟢 **Runtime info:** Case 80 → 79 → 78 → 77 % (A320 … B3556); 6.1 present in 25 of 59 packets, exactly those where a bud reports charging (field 2 = 2; Option F); the app's Case line
  "last seen 07:25:14 (last connection)" (film 1 07:37:59) = the last packet with 6.1 before that Connect, A860 07:25:14.792. Top-level field 2 (wall clock)
  absent in all 59 packets (no `SetWallclock` from the app).

## 4. Bluetooth off/on and the exports (BD-9 … BD-12; F-3, `PAIR-003`)

- 🟢 E5 429 "Bluetooth adapter: ON -> TURNING_OFF" 07:34:57.441, 431 "-> OFF" 07:34:58.067; the phone closed HFP (A2243 `DISC` DLCI 9) and MAESTRO (A2303 `DISC`
  DLCI 2, 07:34:58.121) itself; E5 433–434 "Session lost …" then **"Session loss cause: Bluetooth was switched off on this phone"** — final, no "provisional"
  (F-3 verified on hardware). Screen: "Bluetooth is disabled." + Enable Bluetooth, then "Bluetooth was switched off on this phone, which closed the app's
  channel. Tap Connect to reconnect." (07:35:45).
- 🟢 E1 (saved 07:35:23) contains lines 429–435; E2 (07:36:04) also 440–485: adapter `OFF -> TURNING_ON -> ON`, `NOT_CONNECTED -> CONNECTED` and **"Automatic
  re-open of the session (ADR-044, trigger: LINK_BACK)"** 07:35:48.018 → ready on channel 19 (B415) **without a Connect tap**.
- S12 (rotation while the save dialog is open) was **not done** — no rotation on film.

## 5. Session ends, re-opens and wear (ADR-044)

| # | Time | Cause on the wire | App log / screen | Re-open |
|---|---|---|---|---|
| 1 | 07:25:11.294 | Buds `DISC` DLCI 2 (A749), ACL up — **Right** bud out of the case (film 07:25:09; A860 6.3 field 2 = 1, 6.2 = 2) | "the Buds closed the channel"; card on film | AFTER_LOSS 07:25:12.8 → ch **21** (A797) |
| 2 | 07:25:34.474 | Buds `DISC` (A883) — the **Left** out (film 07:25:27) | same | AFTER_LOSS → ch **19** (A919) |
| 3 | 07:28:38.960 | Buds `DISC` (A1635) — first bud into an ear (bud not identifiable) | same | AFTER_LOSS → ch 21 (A1673) |
| 4 | 07:28:50.960 | Buds `DISC` (A1744) — second bud into an ear | **"Automatic re-open skipped: the session was lost 9939 ms after an automatic re-open"** (E5 309) | none (ADR-044's guard); the maintainer pulled to refresh 07:29:22 → ch 19 |
| 5 | 07:30:31.918 | phone `DISC` (A2045) | Disconnect tap | Connect tap 07:31:02 |
| 6 | 07:34:58.121 | phone `DISC`, ACL `0x16` | Bluetooth off (§4) | LINK_BACK 07:35:48 |
| 7 | 07:37:00.950 | phone `DISC` (B609) | Disconnect | Connect 07:37:40 |
| 8 | 07:39:36.360 | phone `DISC` (B756) | Use different Buds | pairing, Connect 07:41:19 |
| 9 | 07:43:20.843 | phone `DISC` (B1613) | Disconnect (BD-20) | Connect 07:43:23.9 (one `SABM` B1634) |
| 10 | 07:45:32.443 | Buds `DISC` DLCI 2 **and** DLCI 4 (B1896/B1897) during the Refresh claim | "the Buds closed the channel"; "Still connecting…" | AFTER_LOSS → ch 19 (B1936) |
| 11 | 07:48:31.650 | Buds `DISC` (B2164) while ringing | same; ring notice "reconnect and tap Stop" | AFTER_LOSS → ch 21 (B2203) |
| 12 | 07:48:42.374 | phone `DISC` (B2273) | Disconnect (I4) | Connect 07:48:54.6 |
| 13 | 07:49:06.633 | Buds `DISC` (B2434) | same | AFTER_LOSS → ch 19 (B2471) |
| 14 | 07:52:05.893 | phone `DISC` (B2823) | Disconnect (BD-29 pre) | — |
| 15 | 07:53:05.400 | page timeout `0x04` | "Couldn't open the app's channel …" | Retry 07:53:42 |

- 🟢 Every Buds-side close (7) came with the ACL up and was re-opened within 1.5–2.0 s except #4 (the 10-s chain guard, as designed); every user/phone close
  recovered on the next tap; no session was lost without a message. Criterion 3 ("stable across multiple connect/disconnect cycles") holds over 15 ends.
- 🟢 **Lead L-1 (the hosting bud):** with both docked on 19, the **Right** out ⇒ `DISC` + **21**; then the Left out ⇒ `DISC` + **19** (#1, #2). Fits the 🟡 reading of
  `PROTOCOL.md` §2.2a ("the channel names the bud hosting the link") but does not test the open case (the Left out with both **worn** on 19). No status change.
- 🟡 #11 and #13: no action on film; the ring tone became audible 13 s after the Ring (07:48:44.8) — consistent with a bud being taken out of an ear while ringing
  (#11) and put back (#13). Head not in view; not settled.
- 🟢 **J4 (BD-23):** app paused 07:45:47.552 → resumed 07:47:59.548 (logcat); MAESTRO stayed open (runtime info B2051, B2059, B2065 received), no `DISC`; on
  return "Noise cancellation", no message.

## 6. Find My Buds — every `04 01` / `ff 01` frame, and the Ring stopped on the bud (BD-24, BD-26 … BD-28; `FIND-001`, `FIND-005`)

| Frame (B) | Time | Direction | Bytes | Meaning |
|---|---|---|---|---|
| 2142 | 07:48:16.927 | phone → Buds | `04 01 00 01 02` | Ring Left |
| 2150 | 07:48:17.052 | Buds → phone | `ff 01 00 03 04 01 00` | ACK |
| 2153 | 07:48:17.132 | Buds → phone | `04 01 00 01 02` | the Buds' own ring status |
| 2416 | 07:49:02.559 | phone → Buds | `04 01 00 01 00` | Stop (after Disconnect/Connect, I4) |
| 2427 | 07:49:02.648 | Buds → phone | `ff 01 00 03 04 01 00` | ACK |
| 2428 | 07:49:02.649 | Buds → phone | `04 01 00 01 00` | status: stopped |
| 2721 | 07:50:49.882 | phone → Buds | `04 01 00 01 02` | Ring Left (BD-26) |
| 2730 | 07:50:50.052 | Buds → phone | `ff 01 00 03 04 01 00` | ACK |
| 2733 | 07:50:50.120 | Buds → phone | `04 01 00 01 02` | status: ringing |
| 2799 | 07:51:49.153 | phone → Buds | `04 01 00 01 00` | Stop (BD-28) |
| 2810 | 07:51:49.351 | Buds → phone | `ff 01 00 03 04 01 00` | ACK — **no** status message follows |

Command: the message-level parser of the header on B (claims 8, 11, 14, 15), confirmed with `tshark -r CAP-068-btsnoop_hci2.log -Y "bthci_acl.chandle==0x000b
&& btrfcomm.dlci==4 && btrfcomm.len>0 && (frame contains 04:01:00:01 || frame contains ff:01:00:03:04:01)" -T fields -e frame.number -e frame.p2p_dir -e data.data`.

- 🟢 The directions of `PROTOCOL.md` §4.4's 2026-10-03 Correction hold again (ACK `ff 01 00 03 04 01 00` and the Buds' own `04 01 00 01 xx`); **no phone ACK**
  `ff 01 00 02 04 01` anywhere (no Play services, and the app sends none) — 4 of 4 commands.
- 🟢 **BD-27 gives no wire evidence for `FIND-005`:** the app released DLCI 4 1.5 s after the Ring (B2736 07:50:51.562) and nobody held it while the bud was touched
  (film 07:51:12–14; the tone stopped between 07:51:12.4 and 07:51:14.2). Between B2738 and B2788 only HFP and one MAESTRO runtime-info push were on the link —
  the Buds had no open Message Stream on which to send a status. When the app's Stop arrived 35 s later (B2799) the Buds ACKed it **without** a status message —
  like `CAP-025`'s Stop while not ringing (2202 → 2204). 🟡 HYPOTHESIS: the Buds had already stopped ringing (the tone stopped; the second Stop got no status).
  🔴 Whether the Buds push `04 01 00 01 00` after a stop on the bud is **still open**; it needs a Seeker that holds the Message Stream through the stop (the
  official app with Play services, `CAP-069` VII).
- 🟢 **What the app showed:** "Ringing: Left earbud — tap Stop to end it." stayed from 07:50:50 to the Stop tap at 07:51:47 (film), 33 s after the ring stopped —
  1.0.1 reads nothing from the status message and had none to read. After a Buds-side loss during a ring the I4 texts appear: "A ring was started on the Left
  earbud — reconnect and tap Stop to end it." (07:48:32) and "… before the app reconnected — it may still be ringing. Tap Stop to end it." (07:48:34, 07:48:59).
  The I4 text "after Disconnect" was shown after the Buds' close, not after the Disconnect tap (the Find tab was not on screen then).

## 7. The 1.0.1 changes on hardware (`APP_TESTPLAN.md` section S)

| S | Result | Evidence |
|---|---|---|
| S1 | ✅ partly: after Disconnect the ANC mode stays shown, dimmed, (i) with the dot; the tile reads **"ANC / Open the app"**; the (i) text "— from the last connection" not opened | film 1 07:30:43–45, 07:37:05–09 |
| S2 | ✅ partly: Sound (EQ, presets) and Controls dimmed and disabled, (i) with the dot; the (i) text not opened | film 1 07:37:11–21 |
| S3 | not identifiable (the ANC tab at 1 fps showed the mode current at 07:37:45; the transition not caught) | film 1 |
| S4 | ✅ "Case: 80% — last seen 07:25:14 (last connection)" in the battery (i), not "(no bud charging in the case)" | film 1 07:37:59–07:38:05 |
| S5 | ✅ FLAT → `4:{16:{0 ×5}}` → OK; five sliders "0,0" | B732/B735; film 1 07:38:19 |
| S6 | not identifiable (BD-18 not filmed in the first second) | — |
| S7 | ✅ the text exactly as specified; the tap closed the session; "No Pixel Buds Pro 2 paired yet." + Pair a device; E5 537 "removed 0 association(s)" (no association existed — §0) | film 1 07:39:25–51; E5 535–538 |
| S8 | ✅ the picker opened (no silent reuse); the third attempt offered the bonded Buds; association created, no bonding | E5 539–547; film 1/2 |
| S9 | not testable (one pair of Buds) | — |
| S10 | ✅ page timeout `0x04` (B2840), no RFCOMM, the new text without naming another app, ≈ 6 s after the tap | B2837–B2840; E5 901–903; film 2 07:53:06 |
| S11 | ✅ the tile toast with the reason (Settable `00`) | film 1 07:27:53–55; A1508 |
| S12 | not done | — |
| S13 | ✅ "App: 1.0.1, build e1fc886 (2026-10-04)", no "-dirty" | film 1 07:23:45; film 2 07:42:33 |

- 🟢 No "—" was caught on film (S6), no "Unexpected error", no "Decoder fault" line, no reworded message wrong: the session-closed card, the Bluetooth-off cause,
  the connect failure and the not-allowed note read as the literals pinned in `UserMessageTest` and `LossWordingTest`.

## 8. Pairing after "Use different Buds", HFP, AVRCP, LE

- 🟢 Picker 1 (07:39:53.954) and 2 (07:40:30.018): Android ran a BR/EDR inquiry (B770–B864, B942–B1063) — one Extended Inquiry Result, a TV — and listed nothing;
  both cancelled (`user_rejected`, E5 540, 542). The Buds were connected (ACL 3 up) during picker 1 and docked in the open case during picker 2 (ACL dropped
  B933 07:40:26.648 when both were docked). Picker 3 (07:40:58.58) showed the consent dialog for the bonded "Pixel Buds Pro 2 va…" at once (film 2 07:41:00)
  and cancelled its inquiry (B1082 `Inquiry Cancel` 07:40:59.069); "association created", "already bonded — no createBond()". 🔴 Why the first two pickers
  offered nothing and the third the bonded device directly (CDM's own logic; no system log). No new bonding: the link key was reused (B1148–B1150).
- 🟢 HFP: two Service Level Connections (A DLCI 0x0c, B DLCI 0x09 after the pairing); `AT+BIEV=2,NN` pushes (22 in A, 34 in B; `frame contains "AT+BIEV"`).
- 🟢 AVRCP: one Play pass-through from the Buds at 07:40:24.277 (B915), during the case handling (film 07:40:25 volume panel) — a bud gesture, not an app action.
- 🟢 LE: advertising reports only during the CDM scans (206 between 07:39:30 and 07:41:21 in B); no LE connection.

## 9. Defects, observations and the release classification

| # | What | Class | Evidence |
|---|---|---|---|
| 1 | A stop on the bud leaves "Ringing: Left earbud — tap Stop to end it." on screen until Stop is tapped | **minor** — the known limit of 1.0.1 (the app reads no ring status, the maintainer's choice "Nothing new on the wire; test first"); harmless (Stop works) | §6 |
| 2 | A tap on the current ANC mode sends a `Set`; the Buds ACK it | observation, not a defect | §2 |
| 3 | After two wear changes within 10 s the second loss is not re-opened; the user must tap Connect or pull | minor — ADR-044's guard as designed; the screen says "Not connected …" | §5 #4 |
| 4 | P1 "update over 1.0.0" not supported by the logs | not an app defect (a test-procedure question) | §0 |
| 5 | The CDM picker offered nothing twice before offering the bonded Buds | 🔴 Android's picker; not an app defect shown | §8 |

**No heavy defect:** no crash or ANR in the app's buffers, every write acknowledged (12 ANC `Set`, 6 EQ `WriteSetting`), Safe Mode let the writes pass on
`release_5.203`, every session end recovered, no unexpected request on any channel, nothing on DLCI 0x08/0x0a.

## 10. Protocol correlation (per channel)

- **Message Stream (DLCI 4):** ANC `Get`/`Set`/`Notify`/ACK as `PROTOCOL.md` §4.1; Settable byte as ADR-049; Ring as §4.4 (directions); Device Information burst per
  claim. Nothing new except `ANC-006`'s ACK of a `Set` for the current mode.
- **MAESTRO (DLCI 2):** as ADR-034/036/043/045; runtime-info field 2 absent; 6.1 present only while a bud is in the case (25 packets with 6.1, each with a bud charging; 34 without, none with a bud charging).
- **DLCI 0x08/0x0a:** never opened (no Google app service in this user; positive control `CAP-066`). The L68-5 table (`04 05`/`04 03`) gets no samples here.
- **HFP / AVRCP / LE:** as §8.
- **Android's link state:** every loss classified with Android's link reading (E5 "Android still showed the Buds connected right after the loss — the Buds
  closed the channel (provisional …)"); the Bluetooth-off loss final.

## 11. Improvements (proposals only — nothing under `android/` was changed)

1. **Ring status (defect 1):** the app could clear its notice when a claim of its own receives the Buds' `04 01 00 01 00` — but this capture shows the claim is
   closed before a stop on the bud, so reading the message alone would not help; the notice could say "… — tap Stop to end it, or touch the bud" instead. A
   change of wording only, no new send; test: `RingNoticeTextTest` literal; hardware: none needed. Any acknowledgement or a held claim would be a new send /
   a new ADR (draft not proposed — the evidence for the status message on a stop at the bud is still missing).
2. **`ANC-006` (observation 2):** keep sending the `Set` (ACKed, harmless, the shown mode may be stale). Alternative: skip when the claim's `Notify` already
   reports the requested mode — saves one frame; a unit test with A2033/A2034 as fixtures. Decision after `CAP-069` IV (the official app).
3. **Test procedure:** P1 should record `dumpsys package` (first install / last update time) in the test user; BD-18/S6 needs a screen recording of the phone,
   not the camera; S12 must be filmed with the rotation.

## 12. Open questions

- 🔴 Does the Buds' ring-status message follow a stop on the bud? (`FIND-005`, `CAP-069` VII with a held Message Stream.)
- 🔴 Why the CDM picker listed nothing twice (§8).
- 🟡 Was the 1.0.1 APK installed fresh (§0)?
- 🟡 Losses #11/#13 = a bud out of / into an ear during the ring (§5).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-068-2026-10-04_07-22-57_07-54-10-Group_BD/CAP-068-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-068-2026-10-04_07-22-57_07-54-10-Group_BD/CAP-068-FINDINGS
