# Findings: `CAP-067` (Group BC — the release-signed 1.0.0 APK in a GrapheneOS secondary user without Google Play: the Definition-of-done run and the 1.0.0 release test)

Standardized, evidence-based extraction from `CAP-067-btsnoop_hci.log.last` and `CAP-067-btsnoop_hci.log` (before / after the Bluetooth off/on at 08:12:23),
`CAP-067-recording.mp4` (video only — the audio track holds no samples), the two debug exports and the one app logcat, per `ai-sessions/0067`. There is no
system log. The timeline these findings refer to is `CAP-067-EVENT-NOTES.md`.

- 🟢 **FACT** — directly observed in this capture (frame number, log line or film time given).
- 🟡 **HYPOTHESIS** — plausible reading, not yet independently confirmed; the settling experiment is named.
- ⚪ **ASSUMPTION** — not tested here.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-067` · **Date:** 2026-10-03, film overlay 07:57:35–08:18:29 (phone 07:57:33.9–08:18:27.9) · **Firmware:** 🟢 `release_5.203` (7 of 7
announcements, Info tab on film) · **Phone:** Pixel 9a, GrapheneOS, Android 17 `CP3A.260905.009`, **secondary user (`full.secondary`), no Google Play** ·
**App under test:** OpenControl for Pixel Buds Pro 2 **1.0.0, build `8d8af4b`** (release-signed, versionCode 10000) · **HCI logs:** "A" = `.log.last` (1,563
packets, 07:57:42.429–08:12:24.089), "B" = `.log` (1,810 packets, 08:12:38.178–08:20:05.132), 0 `cap_len≠len` · **Buds:** `04:00:6e:cf:6e:07`, classic handle
`0x000b` for every ACL, the only handle in both files; no LE link. `bluetooth.addr` is empty with this `H4 with linux header` encapsulation, so every command
pre-filters by handle.

Commands used throughout (rule 4a), each run on both files: the control-frame inventory `tshark -r <log> -Y "bthci_acl.chandle==0x000b && btrfcomm &&
(btrfcomm.frame_type==0x2f || btrfcomm.frame_type==0x43 || btrfcomm.frame_type==0x63 || btrfcomm.frame_type==0x0f)" -T fields -e frame.number -e frame.time -e
frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type`; HCI connection and pairing events with `-Y "bthci_evt.code==0x03 || bthci_evt.code==0x04 ||
bthci_evt.code==0x05 || bthci_cmd.opcode==0x0405 || bthci_cmd.opcode==0x0406 || bthci_evt.code==0x31 || bthci_evt.code==0x33 || bthci_evt.code==0x36 ||
bthci_evt.code==0x18 || bthci_cmd.opcode==0x0c03"`; MAESTRO and the Message Stream with a scratch decoder (`tshark … --disable-protocol bthfp
--disable-protocol bthsp -Y "bthci_acl.chandle==0x000b && btrfcomm" -e frame.number -e frame.time_epoch -e frame.p2p_dir -e btrfcomm.dlci -e
btrfcomm.frame_type -e data.data`, reusing `scripts/pwrpc_decode.py`'s `parse`/`describe`/`unescape`; pw_hdlc on **DLCI 2 or 3** per direction with a CRC-32 check
of every frame; the Message Stream on DLCI 4/5 at message level `[Group][Code][Length BE]`; every buffer reset at a control frame of its DLCI) — the same method
as `CAP-065`/`CAP-066`. A claim is Play services' when its first phone message is `03 08 00 02 01 25`, the app's when it is `08 11` or `08 12`. AVRCP with
`-Y "btavctp or btavrcp"` (exit 0; 9 + 15 frames). `p2p_dir` 0 = phone→Buds. **Clocks:** phone = film overlay − 1.1 s (±0.2 s, minute flips at both ends); HCI and
the exports are phone local time; the logcat (UTC) = local − 2 h 00 min 00.000 s.

---

## 0. Capture integrity, build identity, logs (🟢 FACT unless marked)

- **Two HCI logs.** A = 07:57:42.429–08:12:24.089 (frame 1 = HCI Reset of the P2 Bluetooth-on; ends with the phone's `Disconnect` A1537 and `Disconnection
  Complete` reason `0x16` A1561 08:12:23.979); B = 08:12:38.178–08:20:05.132 (frame 1 = HCI Reset). The 14.1-s gap is the BC-10 Bluetooth-off (film: off
  08:12:22.5, on 08:12:36); the Buds' ACL ended before it, so no Buds traffic is lost at the boundary. A: 0 out-of-order packets, B: 1 (frame 1431). Both files
  are complete for RFCOMM (181 pw_hdlc frames, all CRC-32 valid, 0 unparsed); A2DP media is not logged (0 media frames — also 0 in `CAP-066`'s logs:
  `tshark -Y "bta2dp || btavdtp.media"` → no output on both), so the small size (155 KB vs `CAP-066`'s 568 KB) comes from fewer events (6 Message Stream
  claims vs 49, no Play services, 3 ACLs vs 6), not from truncation.
- **Build — "the latest build, 1.0.0, see Settings → Info" (the maintainer's statement): 🟢 confirmed.** The Info tab on film (07:58:25–31; full-resolution
  crop t = 52 s) reads **"App: 1.0.0, build 8d8af4b (2026-10-02)"** — no "-dirty" — and the ADR-051 line "Works with Google Pixel Buds Pro 2. Not affiliated with
  or endorsed by Google. Pixel Buds is a trademark of Google LLC." The logcat header names `io.github.tedsluis.opencontrolpixelbuds:10000` (versionCode 10000 =
  1.0.0). `dist/1.0.0/opencontrol-pixelbudspro2-1.0.0.apk` (read only, not rebuilt): `sha256sum` → `107d49b609a3509364f92e2911931e9ff51ae1dd62f96405026c27464f3467a2`
  (= its `.sha256` file and the prompt); `apksigner verify --print-certs` → certificate SHA-256 `a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb`;
  `aapt2 dump badging` → `versionCode='10000' versionName='1.0.0'`, label "OpenControl for Pixel Buds Pro 2", permissions `BLUETOOTH_CONNECT`,
  `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_CONNECTED_DEVICE` (and the AndroidX `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`) — **no
  `INTERNET`**. Strings that exist only in this build are on film: the launcher label "OpenControl" (07:58:00–11), the notification title "OpenControl for Pixel
  Buds Pro 2" (08:10:21 …), the licence line and "Read the licence". ⚪ That the installed APK is byte-identical to the `dist` file is not proven (it was not pulled
  from the phone; no system log with an install line).
- **"Without Google Play services" (the maintainer's statement): 🟢 confirmed for this run.** Logcat header `userType: full.secondary`; the app drawer on film lists
  no Play Store (07:58:00); and the wire shows **no** Play-services Message Stream claim (§2). P0's package list was not saved (no file) — the drawer and the
  wire stand in for it.
- **Debug exports.** E1 (`…-081146.txt`, 341 lines, 07:12:34.868–08:11:28.901, PID 12287) was saved on film at 08:11:46–54 **with Debug mode off** (switched off
  08:11:44, on again 08:11:58) — the L3 export. E2 (`…-082306.txt`, 125 lines, 08:15:42.399–08:23:01.435, PID 20326) was saved after the film (logcat "Debug log
  exported (125 lines)" 06:23:08.412 UTC) with Debug mode on (hex lines in the logcat at 06:23:10.861). **The content cannot tell the two apart:** hex lines are
  gated at the moment a frame arrives (`BleLogger.kt:73–74`, `logHexDump` returns when Debug mode is off), earlier lines stay in the ring buffer, and the always-on
  `pw_rpc …` summary (`BudsRepositoryImpl.kt:468`) shows that no inbound frame arrived between the switch-off and the save in either file (E1: last inbound
  08:11:02.851, A1356; next Buds MAESTRO frame A1524 at 08:12:23.756, after the save). L3's "no hex line after the switch-off" is therefore **vacuous** here:
  🟢 no hex line, but also no frame that could have produced one.
- **No export covers 08:11:29–08:15:42** (process 12287 after E1: the Bluetooth off/on, BC-11s, BC-12, BC-R and A5); the maintainer confirmed in chat that no
  other export exists. The logcat's **main** buffer starts at 06:20:31.104 UTC (08:20:31), so it does not cover that stretch either. Film and HCI do.
- **App logcat.** One file (671 lines): events from 05:03:17.975 UTC, system from 05:58:11.483, main from 06:20:31.104 to 06:23:20.880. No crash, ANR, tombstone,
  `FATAL` or exception line (`grep -n "crash\|ANR\|tombstone\|FATAL\|Exception"` → only the header's "crash" buffer name; positive control: `grep -c
  wm_on_create_called` → 23). Two `W System: A resource failed to call close.` lines (06:23:02.643/.644, PID 20326) are CloseGuard's default report — StrictMode is
  off in a release build (`OpenControlApplication.kt:63`); 🟡 the same framework objects as `CAP-066-FINDINGS.md` §8 (no stack is printed without StrictMode).
- **No system log** (not available in the user without Play services). What it would have shown and what covers it instead: P7's `ACTION_VIEW` starts → film only
  (Vanadium opened github.com twice); BC-10/BC-12 `BluetoothAutoOff`/`STATE_CHANGED` → HCI Reset/`Disconnect` and film; the auto-off alarm → not tested (BC-12
  skipped); force-stop / `am_kill` → film (no force-stop was made, §7) and the logcat's PID change; install lines → none (⚪ above).
- **The "force-stop" the maintainer mentions** was the revocation of *Nearby devices* (A5): on film (3 fps) "Gedwongen stoppen" is visible on App-info but not
  tapped; "Niet toestaan" is selected at 08:15:34.2 and the phone sends `DISC` on MAESTRO 0.3 s later (B904 08:15:34.491), the ACL staying up; the old PID's last
  line is 06:15:23.679 UTC, the new PID 20326 starts 06:15:42.357. Android kills an app when a runtime permission is revoked; 🟡 not shown by a system log here.
- **"Permissions (start)" every ≈ 10 s at 07:12–07:13 and 08:01–08:03** (prompt §2): each is an activity relaunch by rotation — the logcat's
  `wm_on_stop_called … Reason=handleRelaunchActivity` precedes every one (6 before the film, 12 at BC-1/BC-2/BC-3r, film 08:01:10–08:03:07). The 08:10:44 and 08:11:28
  re-creations (`performDestroy` + `performCreate` without a relaunch reason, while the activity was stopped) are Android's dark-theme switches.
- **Audio.** The audio track has no samples (its `stbl` is 8 bytes, no `stsd`/`stsz`); `ffmpeg -map 0:0 -f null -` cannot decode it. Nothing about sound is
  evidenced.

## 1. ACLs and automatic connect

| # | Phone time | Physical / user action | HCI | App |
|---|---|---|---|---|
| A | 07:58:40.124 | **Connect tap** with the **lid closed** | phone `Create Connection` A157 → `Connection Complete` **status `0x04` (page timeout)** A160 07:58:45.266 | `ChannelUnavailable` after 5168 ms (E1 47–48) |
| B | 07:58:49.643 | **Retry tap** (lid open since 07:58:44) | phone page A161 → ACL A163 07:58:50.385 (`0x000b`) | ready 07:58:50.580 |
| — | 08:12:23.858 | Bluetooth off | phone `Disconnect` A1537 → `0x16` A1561 | (no log) |
| C | 08:12:39.269 | Bluetooth on | phone page B153 → ACL B159 08:12:40.749 (Android's own reconnect, before any app frame) | MAESTRO `SABM` B372 08:12:43.517 (no log; 🟡 LINK_BACK) |
| — | 08:16:12.364 | (E) forget | phone `Disconnect` B1145 → `0x16` B1147; `Delete Stored Link Key` B1151 | E2 49–52 |
| D | 08:17:01.713 | Z1 re-pairing | phone page B1251 → ACL B1253; SSP (§8) | LINK_BACK 08:17:04.156 |

- 🟢 Every ACL of the run was started by the **phone** (`-Y "bthci_evt.code==0x04"` — Connection Request from the Buds → 0 in both files, exit 0; positive
  control: `CAP-066` `.log.last` A2804). Opening the lid did not start one: the lid opened at 07:58:44 (film) and no Buds page followed in the 5.6 s before the
  Retry tap (A160 → A161), and the lid reopened at 08:16:43 with the bond already removed — a sixth/seventh "no automatic connect on lid-open" sample
  (`CAP-064` §1 … `CAP-066`; the buds stayed in the case each time).
- 🟢 The Connect tap with the lid closed is a new, **extra** observation: the page timed out and the card blamed another app — see §9 item 2.

## 2. Message Stream claims — the Definition-of-done negative and BC-7

```
$ for f in CAP-067…/CAP-067-btsnoop_hci.log.last CAP-067…/CAP-067-btsnoop_hci.log CAP-066…/CAP-066-btsnoop_hci.log.last CAP-066…/CAP-066-btsnoop_hci.log; do
    n=$(tshark -r "$f" -Y "bthci_acl.chandle==0x000b && (btrfcomm.dlci==4 || btrfcomm.dlci==5) && frame.p2p_dir==0 && btrfcomm.len>0" -T fields -e data.data)
    echo "$f exit=$? phone msgs=$(…) 030800020125: $(… | grep -c '^030800020125') 08110000: $(… | grep -c '^08110000')"; done
CAP-067-btsnoop_hci.log.last: tshark exit=0  phone msgs=3   first-msg 03 08 00 02 01 25: 0   08 11 00 00: 3
CAP-067-btsnoop_hci.log:      tshark exit=0  phone msgs=3   first-msg 03 08 00 02 01 25: 0   08 11 00 00: 3
CAP-066-btsnoop_hci.log.last: tshark exit=0  phone msgs=223 first-msg 03 08 00 02 01 25: 22  08 11 00 00: 43     (positive control)
CAP-066-btsnoop_hci.log:      tshark exit=0  phone msgs=34  first-msg 03 08 00 02 01 25: 4   08 11 00 00: 6      (positive control)
```

| Claim (`SABM`) | Time | Trigger | Phone → Buds | Buds' answer |
|---|---|---|---|---|
| A275 (DLCI 4) | 07:58:50.633 | Connect (Retry) | `08 11 00 00` (A285) | Device Info `03 0a`, `03 01 da 2d b1`, `03 02`, `03 09 "Revision 6"`, `07 10`, `07 34`; battery `03 03 00 03 e4 e4 ff` ×3; `Notify 08 13 00 04 01 e8 00 20` (A299) |
| A1001 | 08:06:09.343 | AFTER_LOSS (BC-6a) | `08 11` (A1012) | battery `64 64 ff` ×3; `Notify 01 e8 e8 80` (A1027) |
| A1142 | 08:07:33.961 | AFTER_LOSS (BC-6) | `08 11` (A1153) | `64 64 ff` ×3; `Notify 01 e8 e8 80` (A1178) |
| B432 | 08:12:44.212 | re-open after Bluetooth-on | `08 11` (B459) | `03 0b` (25 B, FHN), `64 64 ff` ×3; `Notify 01 e8 e8 08` (B474) |
| B1000 | 08:15:54.543 | LINK_BACK (new process, A5) | `08 11` (B1011) | `03 0b`, `64 64 ff` ×3; `Notify 01 e8 e8 08` (B1025) |
| B1655 (DLCI 5) | 08:17:04.348 | LINK_BACK (after Z1) | `08 11` (B1666) | `e4 e4 ff` ×3; `Notify 01 e8 00 20` (B1678) |

- 🟢 **All 6 Message Stream claims are the app's** (each first phone message `08 11 00 00`); **0** start with `03 08 00 02 01 25` (exit 0 on both files; the same
  filter finds 22 + 4 Play-services claims in `CAP-066`). No other phone-side message was sent on DLCI 4/5 (phone messages = 6 = the six `08 11`). No other
  RFCOMM client opened anything on the Buds: the only DLCIs are 0x02/0x03 (the app), 0x04/0x05 (the app), 0x09/0x0c/0x08 (HFP: AT commands only, §8) — no DLCI
  0x08 "GSND CONTROL" or 0x0a "GSND AUDIO" from the phone (in B, DLCI 0x08 is HFP: the Buds opened the multiplexer, so channel numbers flip).
- 🟢 **BC-7 (F-3 watch):** no claim was closed between a request and its answer — every `Notify` came 17–283 ms after its `08 11` and each `DISC` came 1.5–1.6 s
  after the last answer (the linger: A406, A1047, A1197, B568, B1047, B1711). No "cut off" text on film. F-3 (`AnswerCutOff`) is therefore **not exercised** (as
  expected without another claimant).
- 🟡 HYPOTHESIS (`ai-sessions/0066` RESULT §C.1) "the Owner's sandboxed Play services cannot use Bluetooth while the secondary user is in the foreground":
  **consistent** — 0 Play-services claims over 22 min of foreground use; not proven (the Owner's Play services were not observed directly).
- 🟢 **No ANC `Set` (`08 12`) was sent in the whole run** (0 in both files) — the maintainer tapped no ANC mode and no tile. The ANC mode nevertheless changed:
  Off (`20`, A299) → Transparent (`80`, A1027, A1178, after wear changes) → **Active** (`08`, B474, B1025) → Off (`20`, B1678, docked). The Transparent → Active step
  happened between 08:07:34 and 08:12:44 with no app frame and no film tap; the notification and the tile showed "ANC: Active" at 08:13:24–34. 🟡 A wear-driven
  mode rule of the Buds (as `CAP-066` §3) or a press-and-hold on a bud off film — not settled.

## 3. The Settable byte (ADR-049)

All 6 `Notify ANC state` frames against the film's wear state: `00` with both buds docked (A299 07:58:50, B1678 08:17:04 — film: case with both buds); `e8` with at
least one bud worn (A1027 08:06:09 Left worn, Right on the table; A1178 08:07:34 one worn, one on the table; B474, B1025 both worn, case empty on film). 🟢 0
counter-examples to "`00` ⇒ no bud worn"; ADR-049 item 3 unchanged (🟡). The ANC tab showed the "not allowed" note at 08:01:31 (after `00`) and no note at
08:08:46 (after `e8`) — 🟢 the app follows the byte.

## 4. Lead L-1 — the hosting bud (`PROTOCOL.md` §2.2a, `0063` Update 🟡; BC-6a / BC-6)

Every `GetSoftwareInfo` `RESPONSE` with `call_id` 4294967295 (raw A253 `7e 00 a5 03 2a 64 22 57 0a 1b 0a 0a 31 37 37 39 32 39 38 36 39 34 12 0d 72 65 6c 65 61 73 65 5f
35 2e 32 30 33 … 08 01 10 15 1d ea 71 de 7d 5e 25 44 fa 99 71 38 ff ff ff ff 0f e8 a9 58 66 7e` = `10 15` = channel 21; A982 `… 10 13 …` = 19; all CRC OK):

| # | Announcement | Ch | Same ACL? | Just before (film) | Fits "names the hosting bud" |
|---|---|---|---|---|---|
| 1 | A253 07:58:50.597 | 21 | new (Retry page) | both buds docked | — |
| 2 | A982 08:06:09.126 | **19** | yes — Buds `DISC` 0x02 A945 08:06:06.805 | **Right out of the ear** (head on frame-left), Left worn, session was on **21** | ✓ **21 → 19** (BC-6a, 3rd sample) |
| 3 | A1123 08:07:33.681 | **21** | yes — Buds `DISC` 0x02 A1085 08:07:31.481 | **a bud** out of an ear (head **not** in view), both had been worn on **19** | ✓ if it was the Left; **bud not identifiable** |
| 4 | B399 08:12:43.820 | 21 | new (after Bluetooth-on) | both worn (off film) | — |
| 5 | B980 08:15:54.465 | 21 | yes (re-open by the new process) | both worn, unchanged | ✓ (unchanged) |
| 6 | B1628 08:17:04.300 (DLCI 0x03) | 21 | new (after Z1) | both docked | — |

Command: the control-frame inventory (Buds-side `DISC` on 0x02/0x03: A945, A1085 — 2 in the run) plus the decoder's announcement list.

- 🟢 FACT (this capture): with both buds worn on 21, taking the **Right** out made the Buds close MAESTRO with the ACL up and announce **19** (#2) — a third sample
  after `CAP-066`'s 2.
- 🟢 FACT: with both worn on **19**, taking **one** bud out made the Buds close MAESTRO and announce **21** (#3) — the first "19 → 21" on a removal. Which bud was
  out is **not identifiable** from the film (head out of view; the bud's shape on the table did not settle it), and nothing on the wire names it (both buds read
  "not charging" before and after). 🟡 The prediction "Left out on 19 ⇒ `DISC` + 21" is therefore **consistent but not confirmed**. Settling experiment: both worn
  on 19, take the Left out with the head in view (head on the right of the frame).
- 🟢 ADR-034 item 3 is unaffected: only 19 and 21 occurred, with the tabulated addresses (`00 3b`/`80 a3`, `00 4b`/`00 a5`).

## 5. MAESTRO (ADR-034/036/043/045) — reads, writes, runtime info; BC-3/BC-5/BC-R

- 🟢 All pw_rpc packets CRC-32 valid, 0 unparsed: A — 3 announcements, 24 `ReadSetting` + 24 `RESPONSE` OK, 3 `SubscribeRuntimeInfo` + 24 `SERVER_STREAM`, **18
  `WriteSetting` + 18 `RESPONSE` OK**; B — 3 announcements, 24 + 24 reads, 3 subscriptions + 11 stream packets, **1 + 1 writes**. 0 `CLIENT_ERROR`/`SERVER_ERROR`, no
  other method. Values read every time: EQ `[-1.00, 1.51, 4.00, 2.00, 0.00]` (`4:{16:{1:f32(-1.00) 2:f32(1.51) 3:f32(4.00) 4:f32(2.00) 5:f32(0.00)}}`), 2:1, 4:1,
  7 = Noise control ×2 (`7:{1:{4:{1:5}} 2:{4:{1:5}}}`), 12 = `{1:1 2:0 3:1 4:1}`, **17:0** (A/B before BC-R) then **17:7** (B1035, B1701), 19:0, 22:1. The Sound and
  Controls screens on film show the same values.
- 🟢 **BC-3 reached Right 4 on the 17th drag.** The 17 writes (A818 … A876, 08:03:52.663–08:04:43.079), zigzag-decoded: −8, −7, 0, 0, −5, 0, 0, −12, 0, 0, −13,
  −13, +7, +27, +52, 0, **−4** (Right 8, Right 7, Centre ×2, Right 5, Centre ×2, Right 12, Centre ×2, Right 13 ×2, Left 7, Left 27, Left 52, Centre, **Right 4**); each
  answered `RESPONSE` OK 29–369 ms later and logged "Setting 17 written (channel 21)" (E1 164 … 212); exactly one write per release (17 writes, 17 labels on film,
  no write during a drag). Raw A876 `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 05 22 03 88 01 07 a9 7d 5e df 03 7e` = `WriteSetting 4:{17:7}` on
  channel 21, **byte-identical to `CAP-064` frame 6671**. **BC-5:** A882 `4:{17:0}` → OK, label "Centre". **BC-R:** B834 08:15:00.639 the same bytes as A876 → OK
  B837 (514 ms) → "Right 4"; read back as 17:7 by both later sessions. 🔴 The channel-19 frame of `17:7` derived in the skeleton was **not** captured (every write
  was on channel 21).
- 🟡 6 of 17 drags landed on "Centre" — the ±3 snap (skeleton BC-5) catches releases near the middle; the slider alone stays imprecise (`CAP-066` §5), though Right
  4 was reached this time.
- 🟢 **Runtime info:** entry 6.1 (Case) present while a bud was charging — 95 % (A324 07:58:51), 93 % (A771 08:02:02 …, B1710 08:17:04); the Right's 6.3.2 turned
  `1` at A889 08:05:12.666 and the Left's 6.2.2 at A906 08:05:32.340 (6.1 absent from then on), both `2` again at B1710 — matching the film's undock/redock order
  (0 contradictions). The app showed the Case 93 % between 08:05:32 and 08:17 as the dated last-seen value (film 08:08:44 …: lighter "93%"), and "Battery
  unavailable — Not reported yet" for the Case at 07:58:50 before the first stream packet (A324 07:58:51.080) — ADR-043 Update as designed.

## 6. The 1.0.0 UI on hardware (F-1, F-6, ADR-050/051, K4d)

- 🟢 **F-1 — the tab survives a configuration change: holds, 13 of 13.** Rotation keeps Connection (2 relaunches), ANC (2), Sound (2), Controls (2), Find (2) and the
  Settings menu with its tab (2: Settings → Info across 08:02:59/08:03:07); Android's dark-theme switch keeps Connection (08:10:44) and the **Settings menu**
  (08:11:28.9, 4-fps narrowing). Back from the menu returned to the tab it was opened from 3 of 3 (Find 08:03:24.4, Connection 08:09:52, Connection 08:12:46).
  "Refuted if" (a tab other than Connection comes back as Connection) — **no literal hit**. Process death (A5) restored the **Sound** tab in the new process
  (08:15:42) — an extra positive.
- 🟢 **F-6 / ADR-050 — Info, licence, links:** the Info text on film as designed (Log Metadata); **Read the licence** opened the dialog "Licence" with "GNU AFFERO
  GENERAL PUBLIC LICENSE / Version 3, 19 November 2007" and closed (07:59:26–32), no browser; **README on GitHub** and **Report an issue on GitHub** each opened
  Vanadium on github.com/tedsluis/opencontrolpixelbudspro2 (README.md; the Issues page) on the tap (07:59:36, 07:59:56). No app RFCOMM frame while the menu or the
  browser was open (only Buds-pushed stream packets A534, A771, A800). ⚪ The `ACTION_VIEW` intent itself is not logged (no system log).
- 🟢 **ADR-051:** launcher "OpenControl" (07:58:00), app title "OpenControl", notification title "OpenControl for Pixel Buds Pro 2", the Info notice — all on film.
- 🟢 **K4d / BC-8:** Off → light at once (08:09:37), System → follows Android (light 08:09:48, dark 08:10:44 and 08:11:02, light 08:11:28), On → dark at once (08:09:25,
  08:10:54). Android's switch was made in the Settings app (Display → Dark theme), not in Quick Settings (no dark-theme tile in the test user).
- 🟢 **P6/P8:** the ANC tile in the test user's Quick Settings showed "ANC / Transparent" (08:10:22) and "ANC / Active" (08:13:34) — the last known mode; the tile was
  not tapped.

## 7. Session ends and re-opens (ADR-044; F-3; A5; (E))

| # | End (phone) | Ended by | ACL | Cause logged (last line) | Re-open |
|---|---|---|---|---|---|
| 1 | 07:58:45.279 | connect failed — page timeout (lid closed at the tap) | none | `Failed(ChannelUnavailable)` (E1 47–48); card blames another app (§9 item 2) | user Retry 07:58:49.6 |
| 2 | 08:06:06.805 | **Buds `DISC`** A945 (BC-6a) | up | "the Buds closed the channel **(provisional …)**" (E1 232) | AFTER_LOSS 08:06:08.340 (1.5 s) → ch 19 |
| 3 | 08:07:31.481 | **Buds `DISC`** A1085 (BC-6) | up | "the Buds closed the channel (provisional …)" (E1 280) | AFTER_LOSS 08:07:32.994 → ch 21 |
| 4 | 08:12:23.888 | **Bluetooth off** (phone `DISC` A1548) | off | **not logged in any file** | MAESTRO `SABM` B372 08:12:43.517, 2.8 s after the ACL (🟡 LINK_BACK; no log) |
| 5 | 08:15:34.491 | **permission revoked** — process killed (phone `DISC` B904) | up | — (process gone) | new process; prompt; granted 08:15:54.132 → LINK_BACK 08:15:54.174 |
| 6 | 08:16:12.364 | **(E) forget** (phone `Disconnect`, link key deleted) | down | "undetermined (no reading of Android's link close to the loss) **(provisional …)**" (E2 51) — stays the last line | — (not bonded) → after Z1: LINK_BACK 08:17:04.156 |

- 🟢 ADR-044 held in every case seen in a log: one attempt per event, 1.5 s after a Buds-side close, on link-back after the permission and after the re-pairing; every
  re-open succeeded on attempt 1/3; no loop.
- 🔴 **F-3 (BC-10) not verifiable:** the expected "Bluetooth adapter: ON -> TURNING_OFF", "-> OFF" and "Session loss cause: Bluetooth was switched off on this
  phone" lines of process 12287 were never exported (§0). The wire shows the off/on and the app's re-open 2.8 s after Android's ACL (B159 → B372); the app was
  STARTED under the shade (no `wm_on_stop` until 06:13:01.996 UTC), so the F-3 path was eligible. F-3 stays **not hardware-verified**.
- 🟢 **A5:** after the revocation the new process found `BLUETOOTH_CONNECT` not granted and — by design (`DeviceStatus.kt:42–47`: a fresh process cannot tell "never
  asked" from "denied", `MainActivity.kt:376` asks on resume when `NOT_REQUESTED`) — showed Android's prompt at once instead of "Bluetooth permission needed / You
  denied the permission" (the skeleton's expectation). Granting it re-opened the session within 42 ms (E2 4 → 10). Not a defect; the skeleton's expectation does not
  hold when the permission is revoked in Settings (the process dies).
- 🟢 **(E):** forgetting the Buds removed the link key (`Delete Stored Link Key` B1151), closed the ACL (`0x16`) and ended the foreground service (no notification on film
  08:16:44); the app showed "No Pixel Buds Pro 2 paired yet." + Pair a device. 🟡 As in `ai-sessions/0062` T-1's known limit, the provisional "undetermined" line stayed
  the last cause line (the app was not visible at the loss; the first reading on return was `UNKNOWN`, not `NOT_CONNECTED`).

## 8. Pairing (Z1, `PAIR-001`), HFP, AVRCP, LE

- 🟢 **B4 not demonstrated:** one press on "Pair a device" on film (4 fps, 08:16:49.9–50.4), one "Pairing: association requested (CDM picker)" in E2 (55) and no
  "Pairing: request ignored — an association request is already open" (`BudsCompanionPairing.kt:135`) — so `PairingFailure.AlreadyInProgress` was not exercised.
- 🟢 **Z1:** CDM picker (OS LE scan B1172–B1245, 7.1 s) → "Toestaan" → association created 08:17:01.671 → `createBond()` → `Delete Stored Link Key` B1249, phone
  page B1251, ACL B1253 → `IO Capability Request Reply` (phone Display Yes/No, MITM required — B1294) / `IO Capability Response` (Buds **No Input No Output**,
  "Numeric Comparison, Automatic Accept Allowed" — B1299) → `User Confirmation Request` B1300 → reply B1301 (72 ms, automatic) → `Simple Pairing Complete` status
  `0x00` B1303 → `Link Key Notification` key type **`0x04` Unauthenticated Combination Key, P-192** (B1304) → BONDED 08:17:05.283 (E2 98). Classic **SSP**, no CTKD (no
  LE link to the Buds) — `PROTOCOL.md` §5.1 / ADR-030 path 1. No Account Linking analysis (ADR-008).
- 🟢 After the re-pairing the **Buds** opened the RFCOMM multiplexer (B1466) and HFP (B1482); the app's MAESTRO was DLCI 0x03, its claim DLCI 0x05 — handled correctly.
- 🟢 HFP: 3 SLCs (A DLCI 0x09, B 0x0c, B 0x08), 33 × `AT+BIEV` (21 + 12), `+CIEV: 4,x` signal changes. 🟡 The Buds' stray bytes after `AT+NREC=0` recur (`CAP-066`
  §9): A550 `41542b4e5245433d30 ea71de7d5e2551aed0ae 2a02200c 08b2acdb7e 860d` — the tail of the app's `ReadSetting 4:12` on channel 21 (A310); B354 contains
  `release_5.203` text; B1617 `41542b4e5245433d30 081d0d`; each answered `ERROR`. No effect on the app.
- 🟢 AVRCP (9 + 15 frames, `-Y "btavctp or btavrcp"`, exit 0): `GetCapabilities`, `PlaybackStatusChanged` Paused/Stopped, `VolumeChanged` 48 %. AVDTP short
  Start/Suspend pairs (3–12 s) — 🟡 system sounds of the phone; no music played.

## 9. Defects, observations and the release classification

| # | Finding | Evidence | Class |
|---|---|---|---|
| 1 | **No ANC change was made** — Definition-of-done criterion 2's "change the ANC/Transparency mode" is not shown by this run (battery is) | §2: 0 × `08 12` | **DoD not proven by this run** (`RELEASING.md`: "DoD failed" ⇒ heavy) — a test gap, not an app fault |
| 2 | The connect-failure card always says "Another app on this phone — for example Google Play services' Fast Pair — may already be using it", also when the cause is the Buds being unreachable (page timeout, lid closed) and in a user without Play services | `ConnectionScreen.kt:424–426`; A160 `0x04`; film 07:58:46 | **minor** (wording; Retry worked) |
| 3 | F-3 not verifiable (no export of the Bluetooth-off) | §7 | test gap |
| 4 | L-1: the bud of BC-6 not identifiable | §4 | test gap |
| 5 | B4 (double tap) and BC-12 (auto-off) not exercised; channel-19 `17:7` not captured | §8, §5 | test gap |
| 6 | (E): "undetermined (provisional)" stays the last loss line when the app is not visible | §7 | minor (known limit, `ai-sessions/0062` T-1) |
| 7 | The ANC mode changed Transparent → Active without an app command | §2 | observation (🟡 Buds behaviour) |

No crash, ANR, wrong or unacknowledged write (19 of 19 writes OK), Safe-Mode failure (writes passed the gate on `release_5.203`/`da 2d b1`), NAK, pw_rpc error or
session lost without recovery — 🟢 **no heavy app defect** in this run.

## 10. Protocol correlation (per channel)

| Channel / protocol | What the app did | What the Buds answered | Goes well | Goes wrong / open |
|---|---|---|---|---|
| **MAESTRO pw_rpc** (DLCI 0x02/0x03, ch 19/21) | 6 session opens (1 tap, 5 automatic), 48 reads, 6 subscriptions, 19 balance writes | 48 + 19 `RESPONSE` OK, 35 stream packets | 0 errors; `17:7` reached and restored | channel-19 `17:7` not captured |
| **Message Stream** (DLCI 0x04/0x05) | 6 claims, each `08 11`; no `Set` | Device Info, battery, `Notify` | 0 Play-services claims; first attempt succeeds every time (no collision) | no ANC change made |
| **HFP / AVRCP** | — | `AT+BIEV`; Paused/Stopped | — | the Buds' stray `AT+NREC` bytes |
| **Pairing** | CDM + `createBond` | SSP Just Works, P-192 | re-pair and automatic re-open in 3 s | B4 not exercised |
| **Android's link** | mirrors it; ADR-044 | — | re-opens after loss, permission, re-pair | F-3 lines not captured |
| **UI** | rotation, dark mode, menu, Info, links | — | F-1 13/13, F-6, ADR-051 on film | wording of finding 2 |

## 11. Improvements (proposals only — nothing under `android/` was changed)

1. **Definition of done, criterion 2 (re-test, no code):** a short follow-up capture in the same user without Play — wear a bud, tap two ANC modes on the ANC tab
   and once on the tile; expected per tap: one claim `08 11` → `Notify` Settable `e8` → `08 12 00 14 01 e8 e8 <mode> 00…` → `ff 01 00 06 08 12 …` ACK; 0 ×
   `03 08 00 02 01 25`. Add a Bluetooth off/on with the app on the Connection tab and **an export right after it** (F-3), and the Left out on 19 with the head in view
   (L-1).
2. **Connect-failure wording (finding 2):** `ConnectionScreen.kt` `BudsError.ChannelUnavailable` text — name "another app" only when the failure was a fast
   collision; for a slow failure (≥ 2 s, the case `RfcommBudsTransport` already distinguishes for its retry, `ARCHITECTURE.md` §6.0b) say "The Buds didn't answer
   — open the case or put a bud in your ear, then tap Retry." Guardrails: UI text only, the detail line stays; no new state, no wire change. Test: a `ConnectionScreen`
   text test per detail (fast vs slow `ChannelUnavailable`), with the real detail string of E1 47. Hardware re-test: Connect with the lid closed → the new text.
3. **APP_TESTPLAN A5 expectation:** revoking *Nearby devices* in Settings kills the process; the app then asks again at once (by design). Record that the "You denied
   the permission" screen appears only after a denial **in the prompt**.
4. **Testplan hygiene:** export the debug log **before** any step that can end the process (A5, a force-stop), and after every Bluetooth off/on.

## 12. Open questions

- 🔴 Which bud did BC-6 take out (L-1 "19 → 21 when the Left is taken out")?
- 🔴 F-3 on hardware (the Bluetooth-off loss line) — no log of it in this run.
- 🔴 Why the ANC mode moved Transparent → Active between 08:07:34 and 08:12:44 without an app command.
- 🔴 The channel-19 bytes of `WriteSetting 4:{17:7}`.
- 🔴 B4 (`AlreadyInProgress`) and K5/BC-12 (auto-off in a secondary user) — not exercised.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-067-2026-10-03_07-57-35_08-18-29-Group_BC/CAP-067-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-067-2026-10-03_07-57-35_08-18-29-Group_BC/CAP-067-FINDINGS
