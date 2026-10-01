# Findings: `CAP-064` (Group AZ — hardware re-test of the `ai-sessions/0054`, `0056` and `0059` builds; the "no automatic connect when the case is opened" observation answered)

Standardized, evidence-based extraction from `CAP-064-btsnoop_hci.log`, `CAP-064-recording.mp4` (video and audio),
`CAP-064-opencontrol-debug-20261001-103023.txt` (the app's debug export), `CAP-064-OpenControl-for-Pixel-Buds-log-49ab12ce3f7f.txt` (app logcat) and
`CAP-064-System-log-11c30e3704a6.txt`, per `ai-sessions/0060`. The timeline these findings refer to is `CAP-064-EVENT-NOTES.md`.

- 🟢 **FACT** — directly observed in this capture (frame number, log line or film time given).
- 🟡 **HYPOTHESIS** — plausible reading, not yet independently confirmed; the settling experiment is named.
- ⚪ **ASSUMPTION** — not tested here.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-064` · **Date:** 2026-10-01, film overlay 10:04:14–10:29:28 (phone 10:04:14.6–10:29:28.6) · **Firmware:** 🟢 `release_5.203`
(14 of 14 DLCI 0x02 announcements, e.g. frame 1215) · **Phone:** Pixel 9a, GrapheneOS, Android 17 `CP3A.260905.009`, Google Play services and the
Google app present · **App under test:** OpenControl, ≥ the `ai-sessions/0057` build with the `0056` writes; installed 16 min after `b65085a` (§0) ·
**HCI log:** 11,390 packets, raw, 0 `cap_len≠len` · **Buds:** `04:00:6e:cf:6e:07`, classic handle `0x000b` for every ACL of the run. LE handle `0x0041`
(`c8:cc:a8:e7:48:93`, frame 284, 🟡 the "Charge 6" speaker) is excluded. `bluetooth.addr` is empty with this `H4 with linux header` encapsulation, so every
command pre-filters by handle.

Commands used throughout (rule 4a):
`tshark -r CAP-064-btsnoop_hci.log -Y "bthci_acl.chandle==0x000b && btrfcomm && (btrfcomm.frame_type==0x2f || btrfcomm.frame_type==0x43 ||
btrfcomm.frame_type==0x63 || btrfcomm.frame_type==0x0f)" -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e
btrfcomm.frame_type` (every `SABM`/`DISC`/`UA`/`DM`, 321 frames); HCI connection events with `-Y "bthci_evt.code==0x03 || bthci_evt.code==0x04 ||
bthci_evt.code==0x05 || bthci_cmd.opcode==0x0405"`; the MAESTRO channel with a scratch decoder that reuses `scripts/pwrpc_decode.py`'s `parse`/`describe`
but reads **DLCI 2 or 3** on handle `0x000b`, buffers **per direction** and checks the CRC-32 of every frame (`scripts/pwrpc_decode.py` alone reads DLCI 2
only and would miss the sessions after 10:17:32, when the Buds opened the RFCOMM multiplexer and MAESTRO became DLCI 0x03); the Message Stream with a
message-level `[Group][Code][Length]` parse of `-Y "bthci_acl.chandle==0x000b && (btrfcomm.dlci==4 || btrfcomm.dlci==5)" -e data.data` per direction;
AVRCP with `-Y "bthci_acl.chandle==0x000b && (btavctp or btavrcp)"` (exit 0, 63 frames — the positive control for every AVRCP negative below); HFP from
the raw RFCOMM payloads (`frame contains "AT+"`, 149 frames; the HFP dissector decodes none of them on DLCI 0x08/0x09). `p2p_dir` 0 = phone→Buds.
**Clocks:** phone = film overlay + 0.4 s (±0.2 s; the status-bar minute flips 0.6 s into overlay second :59 at both 10:04→10:05, t ≈ 45.4 s, and 10:28→10:29,
t ≈ 1485.5 s — no drift); HCI and the export are phone local time; logcat and system log (UTC) = local − 2 h 00 min 00.000 s.

---

## 0. Capture integrity, build identity, audio, logs (🟢 FACT unless marked)

- `capinfos`: 11,390 packets, "Packet size limit: (not set)", 10:04:23.294471–10:31:21.841209; `cap_len≠len` 0; one out-of-order pair. The film (1514.80 s)
  covers phone time 10:04:14.6–10:29:28.6: the HCI log starts 9 s after the film's first frame and ends 1 min 53 s after its last.
- **Build.** The film shows the `ai-sessions/0057` UI (top-bar bug icon → Debug, five tabs Connection/ANC/Sound/Controls/Find, (i) detail dialogs); the wire
  carries the `0056` reads `ReadSetting 4:12` (14 of 14 Connects, e.g. frame 1269 → answer) and writes of fields 12 and 2 (frames 9684, 9922). The system log
  shows `installer_clear_app_data_caller … package=io.github.tedsluis.opencontrolpixelbuds` at 06:53:21.369 UTC (= 08:53:21 local, line 8044) and the
  process start at 06:53:29 (line 8142) — 16 min after `b65085a` (`git log -1 --format=%ad b65085a` → 08:37:32 +0200), the last commit under `android/`
  (`git log --oneline -- android` on `d79aba4`). No log string or wire behaviour that exists only from `b65085a` on occurred in this run (no write timed out,
  no undecodable answer, no rotation), so **"the latest build" is the maintainer's statement, consistent with the evidence but not proven by it** (🟡).
  Contrary to the skeleton's P2 ("do not clear the app's data"), the data were cleared at installation.
- **Audio.** AAC stereo, decodes (`ffmpeg -i CAP-064-recording.mp4 -vn -ac 1 -ar 16000 …`). Per-second RMS: median −69.1 dBFS, 90th percentile −60.2,
  maximum −39.8 dBFS (t ≈ 311 s); 83 seconds rise > 12 dB above the median, all short broadband transients (handling, taps). The full spectrogram shows no
  sustained harmonic speech pattern; a faint harmonic pattern at t ≈ 474–480 s (10:12:08–14 phone) is unidentified. 🟢 the maintainer's statement "I did not
  speak" is consistent with the track; nothing heard inside the buds is on it — every audibility statement below is the maintainer's, not the film's.
- **Debug export.** 1,131 lines (1,130 newlines), 08:53:29.939–10:30:19.222; the first line is the process start, so the 20,000-line ring buffer did not
  wrap and the whole film window is inside it. Debug mode was on (film 10:04:42): every inbound frame of the window is hex-logged. No `MalformedFrame`,
  no unidentified-frame, Safe Mode, quarantine or undecodable-answer line (`grep -i 'malformed\|unidentif\|safe mode\|quarantin\|undecodable'` → 0 lines);
  the 49 `error|exception` hits are the `IOException` texts of socket opens and losses.
- **App logcat, last line "Wrote stack traces to tombstoned" (08:29:56.385 UTC).** Not an ANR: the system log shows a bug report — `dumpstate` takes the
  bug-report lock at 08:29:28.758 UTC (line 51721), dumps one process after another through `libdebuggerd_client`, and "started dumping process 21818"
  (= OpenControl) at 08:29:55.995 (line 57512); the app's Signal Catcher "reacting to signal 3" (57515) and "Wrote stack traces to tombstoned" (57520) follow,
  "done dumping process 21818" at 08:29:56.385 (57521). No `am_anr`/`am_crash` for the package (the only `am_anr` of the log is `me.proton.android.drive`
  at 07:11:11 UTC, before the session).
- **System log.** It carries no lines of the Bluetooth process itself (no `bt_stack`/`btif` tags in 61,308 lines), only system_server consumers (Telecom,
  `BluetoothAutoOff`, ActivityManager). 🟢 GrapheneOS's Bluetooth auto-off played no part (`BluetoothAutoOff … shouldScheduleAlarm: false` at every
  connection-state change, e.g. 08:05:30.670 UTC). Each foreground-service start of the app is accompanied by an `am_wtf … Background started FGS: Allowed
  [callingPackage: io.github.tedsluis.opencontrolpixelbuds; … uidState: TOP …]` (e.g. 08:05:31.026, 08:17:24.114 UTC) — a system diagnostic, not a failure
  (the start was allowed); 🔴 why the system logs it at WTF level for a TOP app.

## 1. The maintainer's observation — "the app does not connect by itself when the case is opened"

**Verdict: qualified — 🟢 for what the wire shows, 🟢 for the code path.** The app **did** open its session by itself every time Android's link to the Buds
came up while the app was on screen (4 of 4, below), as ADR-044 1(b) specifies. **Opening the lid with both buds in the case does not bring Android's link up:**
neither the Buds nor the phone start a Bluetooth connection until a bud leaves the case. With no link there is no event for ADR-044 to react to, and
ADR-044 deliberately waits for Android's link — so the app stays "not open yet" and the user has to tap Connect, on every tab alike (the re-open is
tab-independent, see the code path). This is not a regression of `0054`–`0059`; `CAP-063` saw the same (its §8: "Opening it produced nothing until a bud was
taken out"). `CAP-063`'s observation 5 ("as soon as a bud is taken out of the case, the app connects by itself") is confirmed again here.

### 1a. Every moment the Buds' link came up, or the case was opened, while the app was visible

| # | Film (phone time) | Tab | App visible | Physical action | HCI | Android link (export) | App `SABM` MAESTRO | Re-open line | Connect tap |
|---|---|---|---|---|---|---|---|---|---|
| A | 10:04:50 | Connection | yes (resumed 10:04:34.9) | lid **closed**, both docked | phone Create Connection 673 → Page Timeout `0x04` 699 (10:04:56.067) | `NOT_CONNECTED` | none (socket failed) | — | **yes**, 10:04:50.915 (export 218) → "Couldn't open …" |
| B | 10:05:10–16 | Connection | yes | **lid opened**, both docked | **nothing** (no event between 699 and 903) | `NOT_CONNECTED` | — | — | no |
| C | 10:05:28–31 | Connection | yes | both buds taken out | Buds' **Connection Request** 903 (10:05:30.332) → 907 | `CONNECTED` 10:05:31.007 | 1198 (10:05:31.670) | LINK_BACK 10:05:31.011 (export 222) | no — **automatic** ✓ |
| D | 10:06:12–16 | ANC | yes | both out again (lid open) | Connection Request 1868 → 1872 | `CONNECTED` 10:06:13.822 | 2239 | LINK_BACK 10:06:13.829 (281) | no — automatic ✓ |
| E | 10:13:30–33 | Connection | yes (resumed 10:12:57.4) | right-slot bud out (lid open since 10:05) | Connection Request 5765 (10:13:32.204) → 5769 | `CONNECTED` 10:13:32.834 | 6129 | LINK_BACK 10:13:32.840 (579) | no — automatic ✓ |
| F | 10:15:56–10:16:10 | Sound → Connection | yes | both seated, lid open | ACL `0x13` 7147 (10:15:55.888); then **nothing** for 15.5 s | `NOT_CONNECTED` (refresh, 10:15:56.038) | — | "re-open skipped: … 8237 ms after an automatic re-open" (755) — moot, the link was down | **yes**, 10:16:11.421 → **the phone pages the docked Buds** (Create Connection 7201) → ACL 10:16:12.380 (7204) → ready |
| G | 10:17:27–32 | Connection | yes | both seated, lid open | ACL `0x13` 8398 (10:17:27.084) → **phone Create Connection 3.4 ms later** (8399) → ACL 8414 (10:17:29.496) | `CONNECTED` 10:17:32.495 (profiles [1]) | 8741 (DLCI 0x03) | LINK_BACK 10:17:32.498 (855) | no — automatic ✓ |
| — | 10:12:40.8 (I-2b) | — | **no** (stopped 10:12:35.5) | one bud out | Connection Request 5069 | (no readings while hidden) | none ✓ | — | — |

Command: `tshark -r CAP-064-btsnoop_hci.log -Y "bthci_evt.code==0x03 || bthci_evt.code==0x04 || bthci_evt.code==0x05 || bthci_cmd.opcode==0x0405"
-T fields -e frame.number -e frame.time -e frame.p2p_dir -e bthci_evt.code -e bthci_cmd.opcode -e bthci_evt.status -e bthci_evt.connection_handle
-e bthci_evt.reason` → exactly the rows above (plus the LE device's frame 284).

- 🟢 **Positive control (`AGENTS.md` §13 step 8):** C, D, E and G — four automatic re-opens by LINK_BACK, each 6–8 ms after the reading turned `CONNECTED`;
  plus 8 AFTER_LOSS re-opens (§6). The thing that differs between C/D/E/G and B/F: in C/D/E the **Buds** paged the phone the moment a bud left the case
  (HCI Connection Request event), in G **Android** re-paged the Buds 3.4 ms after an ACL drop; in B and F nobody paged.
- 🟢 **Docked Buds accept a page when the lid is open** (F: Create Connection → Connection Complete in 0.96 s) and **not when it is closed** (A: Page Timeout
  after 5.14 s). So the app *could* connect docked Buds in an open case by itself — but only by starting a connection without a sign that the case was
  opened (see §1c).
- 🔴 Why Android re-paged at once in G but not in F (both: both buds seated, lid open, app visible). The system log has no Bluetooth-process lines to tell. In
  `CAP-062` Android re-created the ACL after an ADR-016 drop, in `CAP-063` (#8, #10) it did not — now both in one capture. Difference here: in G the app had
  just opened DLCI 0x04 (8373 `SABM` 10:17:25.275) and lost its session to a Buds `DISC` 2.1 s before the drop; in F the drop came 8 s after a re-open.

### 1b. Why no re-open was attempted in B and F — the code path

`SessionReopener.attempt` (`android/data/src/main/kotlin/io/github/tedsluis/opencontrolpixelbuds/data/SessionReopener.kt:113–125`) returns at once unless
`enabled && visible && link == AndroidLink.CONNECTED && !sessionOpenOrOpening()` (line 114). `link` is set only from `onAndroidLink` (lines 92–96), which
`BudsRepositoryImpl.onAndroidLink` forwards (`BudsRepositoryImpl.kt:324–331`) from `MainActivity`'s collector of `OsConnectionObserver.observe`
(`MainActivity.kt:312–320`, `repeatOnLifecycle(STARTED)` — independent of the tab shown). The observer derives `CONNECTED` only from
`BluetoothProfile.getConnectedDevices()` of the A2DP/HEADSET/LE_AUDIO proxies, re-read on ACL/profile/bond broadcasts and refresh events
(`OsConnectionObserver.kt:82–117`). In B and F no ACL existed, so no profile was connected, every reading was `NOT_CONNECTED` (export 217, 757), and
**the condition `link == CONNECTED` at `SessionReopener.kt:114` was false**; RESUME (`onVisible(true)`, line 88) is subject to the same condition. No
other guard was involved: `enabled` was true (no Disconnect tap before B or F), the app was visible, the session was `Failed`/`Disconnected`. 🟢 (code read
in full; the readings are in the export.)

- **Tests.** `SessionReopenerTest` and `BudsRepositoryImplTest` ("no re-open when Android's link is down after the delay … then one when it comes back
  (I-1 b)", line 1651) script a `CONNECTED` reading; they pass, and the hardware never breaks them. What the hardware breaks is the **design assumption of
  ADR-044 1(b)** — that opening the case makes Android's link come back. It does not for docked buds; the test suite cannot catch that because there is no
  event to script.
- **Android documentation (fetched 2026-10-01).** `BluetoothDevice.ACTION_ACL_CONNECTED` (AOSP `framework/java/android/bluetooth/BluetoothDevice.java`,
  `refs/heads/main`): *"Broadcast Action: Indicates a low level (ACL) connection has been established with a remote device. … ACL connections are managed
  automatically by the Android Bluetooth stack."*, `@RequiresPermission(BLUETOOTH_CONNECT)` — the app already listens to it (`OsConnectionObserver.kt:109`)
  with that permission; there is no public broadcast for a case lid. `BluetoothSocket.connect()` (same module, `BluetoothSocket.java`): *"Attempt to connect to
  a remote device. This method will block until a connection is made or the connection fails."* — on the wire this is the phone's HCI Create Connection
  (673, 7201) when no ACL exists; it needs no permission beyond the `BLUETOOTH_CONNECT` the app holds (the `BLUETOOTH_PRIVILEGED` sentence there applies
  only to an offloaded data path). No hidden API is involved in any option of §9.

### 1c. Root cause, labelled

- 🟢 **Cause:** with both buds seated, opening the lid starts no Bluetooth connection (B: 18 s of no HCI event; `CAP-063` §8 the same) — the Buds page the
  phone only when a bud leaves the case (C/D/E). ADR-044 re-opens only when Android reports the link (`SessionReopener.kt:114`), so there is nothing to
  react to. The lid itself is visible on no channel the app uses (`CAP-063-FINDINGS.md` §8: only the Buds' LE advertisements change, which this app may not
  scan for, ADR-006).
- 🟢 **Not the cause:** a tab-dependent path, a preceding Disconnect tap, a `PENDING` reading, the chain guard (F's "skipped" line is moot — the link was
  down), or a `0059` guard.
- 🟡 **The maintainer's "every tab" experience** is consistent with this: the same `link == CONNECTED` condition applies on every tab. Only the Connection
  tab was filmed at B/F; the other tabs are covered by the code reading, not by film.

## 2. Re-test verdicts (skeleton steps; "Refuted if" applied literally)

| Step | Verdict | Evidence |
|---|---|---|
| I-0 ready by itself | 🟢 **confirmed** once a bud was out (C, D); the note "ANC can only be changed while you wear the Buds. Tapping a mode checks again first." with enabled buttons (film 10:05:52, after `Notify 01 e8 00 20`, 1257) | export 222–258 |
| I-1a tap on a disabled mode | ⚪ **not exercised as designed**: the precondition failed — the re-open snapshot read **`01 e8 e8 20`** (2299, 10:06:15.005) with both buds out of the case and on the table, so the buttons were enabled; the ADAPTIVE tap sent `08 12 00 14 01 e8 e8 40 00…00` (2687) and the Buds **ACKed** it (2698 `ff 01 00 06 08 12 01 e8 e8 40`) → `Notify 01 e8 e8 40` (2699) — see §3 | export 331–341; film 10:06:16–44 both on the table |
| I-1b worn: `Get` and `Set` in one claim | 🟢 **confirmed, the "unless" branch**: one claim, `Set` without `Get` (the app's last `Notify` read `e8`) → ACK (2977 → 2988) | film 10:07:26.6–27.4 overlay (finger on ADAPTIVE) |
| I-1c tile while not worn | ❗ **literal criterion met** — "an `08 12` in a claim whose `Notify` read Settable `00`": the tile's claim sent `Set 20` (3433, no `Get`, because the app's last `Notify`, 3200 at 10:07:47.36, read `e8`), the Buds NAKed it (3440 `ff 02 00 03 02 08 12`, reason 0x02) and then sent `Notify 01 e8 00 20` (3443). No toast (the tile toasts only `AncNotAllowed`, `AncTileService.kt:135`). The app behaved as built (I-1 re-checks only when the *known* availability is `NOT_ALLOWED`, `BudsRepositoryImpl.kt:693`); the gap is that the known value was 18 s old — Play services' claim had already read `00` at 10:07:54.689 (3299), which the app cannot see | export 404–417 |
| I-1d tile while worn | 🟢 **confirmed** (switches to the next mode, no toast): `Set 20` (3835) → ACK (3843); no `Get` (as I-1b) | export 462–473 |
| AY-3a Left on the table, Right worn | 🟢 **`e8`** (4103 `08 13 00 04 01 e8 e8 20`, 10:09:49.393); no note | film 10:09:28–47, head right ⇒ Left out |
| AY-3b Left worn, Right on the table | 🟢 **`e8`** (4533 `… 01 e8 e8 40`, 10:10:41.488) | film 10:10:06–40 |
| AY-3c both worn | 🟢 `e8` (4720 `… 01 e8 e8 20`) | film 10:10:48–58 |
| I-2a loss while away, link down on return | 🟢 **confirmed** — exact sentence on film 10:12:14; export 573 "Session loss cause: the loss happened while the app was not visible; on return Android's link was down"; ACL `0x13` 4978 while the app was stopped (export 559 10:11:30.858 → 571 10:12:13.718); no app `SABM` 0x02 in between | §6 #6 |
| I-2b one bud out and back while away | 🟢 no app `SABM` while away ✓; **done differently**: the session had already ended (I-2a), so the bud's removal created a fresh ACL (5069) that dropped again (5675) before the return | HCI |
| I-3a–d balance Centre | 🟢 **confirmed**: 26 `WriteSetting 4:{17:n}`, 26 `RESPONSE` OK; the five releases the screen showed as "Centre" wrote `17:0` (6712, 6744, 6765, 6776, 6854); no write carries a value within ±1…3 (zigzag-decoded: 43, −9, −9, −7, −7, **−4**, 7, 0, −13, −9, −7, 0, −10, 0, −6, 0, −11, −9, −8, 12, −5, 10, 9, 11, 5, 0); "Right 4" (6671 `17:7`) not snapped. The next connection read `4:{17:0}` (7044) | §5 |
| I-4a stale per-bud lines at Connect | 🟢 **the stale values were replaced within ≈ 1.5 s** (ready 10:16:43.139 → burst 10:16:44.784 → film "100 %" without ⚡ at ≈ 10:16:45); ⚪ the words "last seen / last connection" are **not on the main surface** — since `ai-sessions/0057` D-7 they live in the (i) dialog, which was opened only at 10:17:13 (and then reads "Case: 92% — last seen 10:16:14 (no bud charging in the case)"); whether the stale "100 %⚡" was drawn dimmed is not identifiable at the film's resolution | film t = 747–751 s |
| I-4b | 🟢 done differently: charging lines and Case 92 % with their times after the automatic re-open (stream 8882, both charging) | export 836–891 |
| I-5 note | 🟢 **confirmed**, text byte-for-byte the skeleton's | film 10:18:04–52 (full-res) |
| AZ-1 list read at Connect | 🟢 `ReadSetting 4:12` in 14 of 14 Connects, between `4:7` and `4:17`, answer `4:{12:{1:1 2:1 3:1 4:1}}`; the list shows all four ticked | §5 |
| AZ-2 untick Adaptive | 🟢 one write, byte-identical to `CAP-041` 2192 (channel 21), OK at 10:20:29.571; the box unticked on screen ≈ 0.0–0.2 s **after** the OK (5 fps) | 9684/9688 |
| AZ-3 long presses with Adaptive unticked | 🟢 **confirmed: never `40`** — six presses, `Notify` modes `80 → 08 → 20 → 80 → 08 → 20` (9737, 9750, 9762, 9776, 9785, 9793). Done differently: the ANC tab was not opened and no Refresh was made; the `Notify`s arrived on Play services' claim, not the app's | §3 |
| AZ-4 untick a second mode | 🟢 done differently (Noise cancellation instead of Off): one write `4:{12:{1:0 2:1 3:1 4:0}}` OK; the last two boxes greyed with "At least two modes must stay selected"; taps on them put **nothing** on the wire (no request between 9849 and 9899) | |
| AZ-5 restore | 🟢 two writes OK, the last all four | 9899–9910 |
| AZ-6a in-ear detection off | 🟢 `4:{2:0}` byte-identical to `CAP-056` 2849 (ch 21) → OK; SASS `07 11 00 04 01 02 b0 00` 0.8 ms after the OK (9927) | |
| AZ-6b a bud out with it off | 🟢 **no pause** (2nd attempt, music playing since 10:23:38.86): no AVRCP `PlaybackStatusChanged` between 10047 (Playing) and 10494 (the user's pause in the shade, 10:25:02.43); the 1st attempt (10:22:44–10:23:00) had no playback and cannot show it. 🟢 The Buds still close the session on wear changes with it off: `DISC` 10102 (10:24:15.569, ≈ 13 s after re-insertion) and 10309 (10:24:50.181, both out) | AVRCP |
| AZ-6c Settable with it off and no bud worn | 🟢 **`e8`** — settles `CAP-056-FINDINGS.md` §4's 🔴: 10394 (10:24:52.251) and the Refresh's 10600 (10:25:16.549) both `08 13 00 04 01 e8 e8 20`, both buds on the table on film 10:24:50–10:26:30; Play services' 10466/10653 the same | §3 |
| AZ-6d on again | 🟢 `4:{2:1}` = `CAP-056` 3627 → OK; SASS `… b8 00` (10822) | |
| AZ-7 read-back | 🟢 `4:{2:1}`, `4:{12:{1:1 2:1 3:1 4:1}}` (10959, 10972) = the last writes | |
| AZ-8 list hides/reappears | 🟢 four `4:{7:…}` writes OK; the list hidden while both are Digital assistant (film 10:28:24) and back with Left = Noise control (10:28:34); no field-12 write | 11126–11170 |
| Refuted-if list (A.7) | **one literal hit (I-1c, above)**; none of the others: no app `SABM` MAESTRO while not visible or after a Disconnect tap before Connect (10:16:26.6 → 10:16:42.6, 10:27:44.9 → 10:27:47.0); one per event; no app frame on DLCI 0x08/0x0a (no channel open line in the export, ADR-043); no field-12/field-2 request outside section VII's taps and the Connect reads; every settings value changed on screen after its `RESPONSE`; I-2a wording correct; Centre only for 0 | |

## 3. ANC and the Settable byte (ADR-049, AY-3, AZ-6c)

Every `Notify ANC state` of the run (app's and Play services' claims; message-level parse), against the film's wear state (head-orientation mapping):

| Settable | Frames | Film |
|---|---|---|
| `00` | 1257, 1456 (10:05:31–34); 3299 (10:07:54.689); 3443, 3505 (10:08:14–19); 4972 (10:11:57.7); **5591** (10:12:44.95); 6190 (10:13:33.56); 7138 (10:15:53.27); 7338, 7648, 8031 (10:16:12–40); 8179, 8238 (10:16:44–49); 8877, 9158 (10:17:33–36) | no bud worn: in the hand right out of the case, on the table, in the case; **5591: one bud on the table, the other docked** (the `CAP-065` "docked/loose" variant, by accident) |
| `e8`, one or both worn | 2759→ worn from 10:07:06 (2847), 2989, 3052, 3101, 3200, 3262, 3567, 3659, 3729, 3765, 3846/3847, 3903, **4103 (AY-3a)**, 4157, 4213, 4223, 4259, 4358, 4425, **4533 (AY-3b)**, 4593, 4637, **4720 (AY-3c)**, 4784, 4904, 6233, 6422, 6524, 6936, 7038, 7105, 9423, 9490, 9555, 9737–9793, 10190, 10258, 11022, 11072 | at least one bud in an ear on film |
| **`e8`, no bud worn, in-ear detection ON** | **2299** (10:06:15.005), **2542** (10:06:17.511), **2699 + 2702** (10:06:38.44/.48, after the app's ACKed `Set`), **2759** (10:06:43.159) | both buds out of the case 10:06:12–16, **both on the table 10:06:16–10:06:44** (film, untouched) |
| **`e8`, no bud worn, in-ear detection OFF** | **10394, 10466, 10600, 10653** (10:24:52–10:25:21) | both on the table |

- 🟢 FACT (this capture): **one worn bud is enough for `e8`** — AY-3a (Left out) and AY-3b (Right out), ears on film, both orientations.
- 🟢 FACT (this capture): **with in-ear detection off, the byte reads `e8` with no bud worn** (4 `Notify`s, ≈ 30 s on the table). The `CAP-056` §4 🔴 is answered.
- 🟢 FACT (this capture): **with in-ear detection on, the byte read `e8` for ≈ 28 s with both buds on the table** (2299–2759) **and the Buds ACKed a `Set`
  in that state** (2687 → 2698). This is a counter-example to ADR-049's 🟡 "`0x00` ⇔ no bud worn" in its "no bud worn ⇒ `00`" direction. In the other
  direction there is none here: every `00` came with no bud worn (incl. 5591 one docked/one loose).
- 🟡 HYPOTHESIS: the Buds set `00` on a removal *from the ear* or on docking, not merely on "not in an ear" — the `e8` period followed buds lifted from the
  case to the table without being worn (10:06:12–16), while every `00` on the table came after a removal from an ear (10:07:50–56 → 3299) or straight out
  of the case after a long dock (10:05:31, 1257 — which contradicts a pure "since the last case exit" rule). Settling experiment: buds from the case straight to
  the table, `Get` every 10 s for 2 min; then worn and taken out onto the table, the same.
- 🟢 The ADR-049 FACT rows hold: the one `Set` after a `00` reading the app could not see was NAKed with reason `0x02` (3440); all three `Set`s sent after an `e8`
  reading were ACKed (2698, 2988, 3843).
- 🟢 **AZ-3 — the Buds follow the list:** with Adaptive unticked (10:20:29.571) six long presses gave `80, 08, 20, 80, 08, 20` — never `40`; the order is
  Transparency → Noise cancellation → Off. Two AVRCP `VolumeChanged` (9746 40 %, 9789 33 %) fall 0.6 s before the 2nd and 6th `Notify` — 🟡 the hand
  touched the bud's swipe area during those presses (the volume panel is on film 10:20:56, 10:21:22).
- 🟢 Mode changes without an app `Set` on wear changes (in-ear on): 4213 `40` (10:10:07.5, Left re-inserted), 4223 `20` (10:10:11.3), 4259 `40`
  (10:10:20.8, Right out), 6233 `40` (10:13:34.4, Right in). 🟡 the Buds' own wear-driven mode report (as `CAP-056` §4: `40` on removal), not a user action.

## 4. Battery, case and the per-bud mapping (lead 5)

- 🟢 Every Left/Right value the app showed matched the claim's `03 03 00 03 <L> <R> ff` (`e4 64` = Left charging 100 / Right 100 at 10:05:31; `64 64` out;
  `e4 e4` both charging; `64 e4` at 10:15:48 Right charging) and was shown with its receive time in (i) (film 10:17:13: "Left: 100% (updated 10:16:44) — not
  charging (out of the case) (10:16:44)"). Case 93 → 92 % from entry 6.1 (1282 `6:{1:{1:93 …}}`, 4954 `1:92` 10:11:52.216), "last seen 10:16:14" while no bud
  charged (stream 8168 without 6.1). 🟢 No percentage without a time.
- 🟢 **Head-orientation mapping vs the wire — 0 contradictions in 5 checks:** Right seated in the right/upper slot → 6.3.2 = 2 (4929, 10:11:51.29; 7054,
  10:15:49.21); the right-slot bud out → 6.3.2 = 1 while Left still charges (6228 10:13:34.08; 9437 10:19:33.82); then both out (6517). Each time the hand of
  the head-on-the-left side handled the right-slot bud.
- 🟢 I-4a: see §2.

## 5. Settings (DLCI MAESTRO, ADR-036/045/046/047)

- 🟢 384 pw_rpc packets, **all CRC-32 valid, 0 unparsed**: 14 `GetSoftwareInfo` announcements (ch 21 ×8, ch 19 ×6), 112 `ReadSetting` requests + 112
  answers OK (14 × fields 16, 2, 4, 7, 12, 17, 19, 22), 14 `SubscribeRuntimeInfo` + 60 `SERVER_STREAM`, 36 `WriteSetting` + 36 empty `RESPONSE`s OK. No
  `CLIENT_ERROR`/`SERVER_ERROR`, no `SubscribeToSettingsChanges`, no other method.
- 🟢 Byte comparison (every phone→Buds frame against the hex fixtures in `android/data/src/test/kotlin/…/codec/*Fixtures.kt`, exact string match):
  `READ_*_REQ` ×48 (ch 21), `READ_12_REQ_CH21_1514` ×8, `READ_12_REQ_CH19` ×6, `SUBSCRIBE_RUNTIME_INFO_2777` ×6 (ch 19), `WRITE_ADAPTIVE_OFF_CH21_2192` ×2,
  `WRITE_INEAR_OFF_CH21_2849`, `WRITE_INEAR_ON_CH21_3627`; the rest (ch 19 reads, balance, `{1:0 2:1 3:1 4:0}`, all-four on ch 21, field 7) have no fixture
  for their channel/value and decode to the intended payload with a valid CRC.
- 🟢 Persistence: balance `17:7` read in sessions 1–6, `17:0` from 10:15:48 (after the last write `17:0` at 10:15:23.88); `2:0` read at 10:24:17/10:24:52 after the
  OFF write, `2:1` at 10:27:47 after the ON write; field 12 all four at every read after AZ-5.
- Raw example (frame 9922, 10:22:31.813, in-ear detection off, ch 21): `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 04 22 02 10 00 a0 a6 cb 94 7e`
  = `WRITE_INEAR_OFF_CH21_2849`; answer 9926 `RESPONSE` OK; SASS 9927 `07 11 00 04 01 02 b0 00`.

## 6. Session ends and re-opens (lead 4)

| # | End (phone) | Ended by | ACL | Film | Cause shown / logged | Re-open |
|---|---|---|---|---|---|---|
| 1 | 10:06:04.595 | ACL `0x13` 1808 (both into the case) | down | both seated | "undetermined" → "the Buds closed the channel" → "Android's link … went down" (export 275/277/279, within 204 ms) | LINK_BACK 10:06:13.829 when the buds came out |
| 2 | 10:07:44.337 | Buds `DISC` 3103 | up | Right out of the ear | Buds closed | AFTER_LOSS 10:07:45.862 |
| 3 | 10:08:35.030 | Buds `DISC` 3568 | up | Right into the ear | Buds closed | AFTER_LOSS 10:08:36.561 |
| 4 | 10:10:20.852 | Buds `DISC` 4261 | up | Right out of the ear | Buds closed | AFTER_LOSS 10:10:22.374 |
| 5 | (none) | — | — | Left in/out (AY-3a/b), both in (I-1b) | — | the Buds did **not** close the session on 4 of the wear changes (10:06:44–10:07:06, 10:09:28, 10:10:06, 10:10:48) |
| 6 | 10:11:59.946 | ACL `0x13` 4978 (both seated), app not visible | down | app in the background | "loss while not visible; on return link down" ✓ (573) | none (link down); none while hidden ✓ |
| 7 | 10:15:45.562 | Buds `DISC` 6938 | up | Right seated | Buds closed | AFTER_LOSS 10:15:47.081 |
| 8 | 10:15:55.900 | ACL `0x13` 7147 | down | Left seated | "link went down" (758) | skipped (chain guard, 755); user Connect 10:16:11.421 |
| 9 | 10:16:26.618 | Disconnect tap | up | docked | — | user Connect 10:16:42.643 (none in between ✓) |
| 10 | 10:17:22.570 | Buds `DISC` 8294 | up | Right seated | Buds closed | AFTER_LOSS 10:17:24.092 |
| 11 | 10:17:25.412 | Buds `DISC` 8378 (0.77 s after #10's re-open) | up → down 10:17:27.08 | Left seated | Buds closed | skipped (chain guard, 850) → LINK_BACK 10:17:32.498 after Android's own reconnect |
| 12 | 10:19:30.193 | Buds `DISC` 9317 | up | Right out of the slot into the ear | Buds closed | AFTER_LOSS 10:19:31.713 |
| 13 | 10:24:15.644 | Buds `DISC` 10102 | up | both worn, ≈ 13 s after the Left re-insertion (in-ear off) | Buds closed | AFTER_LOSS 10:24:17.198 |
| 14 | 10:24:50.188 | Buds `DISC` 10309 | up | both out onto the table (in-ear off) | Buds closed | AFTER_LOSS 10:24:51.709 |
| 15 | 10:27:44.886 | Disconnect tap (AZ-7) | up | worn | — | user Connect 10:27:47.036 |

- 🟢 ADR-044 held in every case: one attempt per event, only while visible, none after a Disconnect tap, none while the link was down, the 10 s chain guard
  stopped a second re-open twice (#8, #11) — and in #11 it was right to: the link dropped 1.7 s later.
- 🟢 The session stayed open after the film (stream packet 11324 at 10:30:40.15, app stopped at 10:29:43): the foreground service keeps it, as designed.
- 🟢 Cosmetic (log only): at #1 (and at 08:56:22, export 152–156) the cause is logged three times within ≈ 0.2 s — "undetermined", then "the Buds closed the
  channel" (a `CONNECTED` reading 126 ms after the loss), then "Android's link … went down" (the `NOT_CONNECTED` reading 165 ms after). The final cause is
  right; the middle line is a premature verdict (`classifySessionLoss` takes the first reading after the loss when no `NOT_CONNECTED` lies within −2…+1 s yet).

## 7. HFP, AVRCP, GSND, LE

- 🟢 HFP: 60 × `AT+BIEV=2,100` (41 on DLCI 0x09, 19 on 0x08 after the multiplexer changed side), the SLC sequence 5 times; **0 `AT+BVRA`**
  (`frame contains "BVRA"` → 0; positive control: `frame contains "AT+"` → 149). Not an app source (ADR-040).
- 🟢 AVRCP (63 frames): playback Stopped/Paused until the Buds' pass-through **PLAY** (10026, 10:23:31.33, 🟡 a bud tap off the visible area), Paused
  10:23:36.06, Playing 10:23:38.86, the user's Paused 10:25:02.43; `VolumeChanged` 40 % / 33 % (§3). No PAUSE on any wear change with in-ear detection off.
- 🟢 DLCI 0x08/0x0a (GSND): opened and closed by the phone side only around the Google app (e.g. 1556/1587 10:05:36, 5353/5367 10:12:43); the app's export
  has no line for them. DLCI 0x04/0x05 outside the app's claims is Play services' (its `08 11` and the SASS `07 41 "in-use"`, `07 40`, `07 42` ACKs, e.g.
  1446–1485).
- 🟢 LE: only `c8:cc:a8:e7:48:93` (handle `0x0041`); no LE link to the Buds in this log.

## 8. Protocol correlation (per channel)

| Channel / protocol | What the app does | What the Buds answer | Goes well | Goes wrong / to improve |
|---|---|---|---|---|
| **MAESTRO pw_rpc** (DLCI 0x02 or 0x03, ch 19/21) | 15 session opens (3 by a Connect tap, 12 automatic; the re-open of 10:17:24 was lost before its announcement, so 14 announced): reads 16, 2, 4, 7, 12, 17, 19, 22, `SubscribeRuntimeInfo`; 36 writes (17 ×26, 12 ×4, 2 ×2, 7 ×4) | 162 answers OK, 60 stream packets | 0 errors, persisted values read back | the Buds close it on most wear/dock changes (9 of 14 ends; 3 by an ACL drop, 2 by Disconnect taps) |
| **Message Stream** (DLCI 0x04/0x05) | 23 claims in the film window (snapshot, Refresh, ANC taps, tile; 11 of them needed a second attempt after a collision with Play services) | Device Info, battery burst, `Notify`, ACK ×3, NAK ×1 | AY-3, AZ-3, AZ-6c answered on the wire | a tile `Set` on a stale `e8` was NAKed (I-1c); Settable `e8` with no bud worn (§3) |
| **SASS** (Group `0x07`, on Play services' claims) | nothing | capability `b0 00`/`b8 00` follows field 2 (9927, 10822) | confirms AZ-6a/d | — |
| **HFP / AVRCP** | nothing | `AT+BIEV`, PLAY/PAUSE, volume | no pause with in-ear detection off | — |
| **Android link** | mirrors it; ADR-044 re-open | — | 4 LINK_BACK + 8 AFTER_LOSS re-opens | **no event when the lid opens with docked buds** (§1) |

## 9. Improvements (proposals only — nothing under `android/` was changed)

**The maintainer's decisions (chat 2026-10-01, `ai-sessions/0060`):** item 1 — *"Nothing now"* (ADR-044 unchanged; the finding is recorded); items 2 and 3 — built by
the next FEATURE session; item 4 — recorded as the ADR-049 Update; item 5 — cosmetic, optional in the same session.

1. **Auto-connect when the case is opened (§1).** The lid is not observable, so no change can react to "lid opened" as such. Options (details and the drafted
   ADR text in `ai-sessions/0060_CAPTURE_RESULT_2026_10_01.md` §E):
   (a) **wording only** — the not-connected card says what connects by itself: "Take a bud out of the case and the app connects by itself — with both buds in
   the case, tap Connect." No ADR, no behaviour change.
   (b) **one connect attempt on resume when Android's link is down** (the app pages the Buds once; docked buds in an open case answer in < 1 s, F; a closed
   case or out of range fails after ≈ 5 s, A, shown quietly). Needs an ADR amending ADR-044's condition "Android reports the Buds connected". Still no timer,
   no loop, foreground only, no new permission; it helps only when the app is opened (or returned to) after the lid — not when the lid is opened while the app
   is already on screen.
   (c) **background CDM device presence** — stays the unbuilt proposal of ADR-044 (c); it would not see a lid either.
   Unit test for (b): `SessionReopenerTest` — visible, link `NOT_CONNECTED`, enabled → exactly one `RESUME_PAGE` attempt; a failure is not retried; no
   attempt after a Disconnect tap. Hardware step: app open on Connection, lid closed → open the lid (buds docked) → expect no attempt (the lid is not an event);
   Home → return → expect one phone Create Connection and a ready session within ≈ 1 s.
2. **I-1c: `Get` before every ANC `Set`** — one claim, `08 11` first, `Set` only if that claim's `Notify` reads non-zero. Removes the NAK of 3440 (the app's `e8`
   was 18 s old). Cost ≈ 10–300 ms per tap (2299 answered 16 ms after its `Get`; 4103 289 ms). Fixture: `CAP-064` 3433/3440/3443 (stale `e8` → `Set` →
   NAK). Hardware: buds on the table after wearing, tile tap → `08 11` → `08 13 … 00 …` → no `08 12`.
3. **ANC wording (§3):** "ANC can only be changed while you wear the Buds" is not what the byte means — it read `e8` with no bud worn (in-ear on, 28 s; in-ear
   off, always). Proposed: "The Buds don't allow changing noise control right now (usually because no bud is in an ear). Tapping a mode checks again first."
4. **ADR-049 (draft Update, for the maintainer):** record AY-3 (one worn ⇒ `e8`, 2/2), AZ-6c (in-ear off ⇒ `e8` with none worn) and the in-ear-on counter-example
   2299–2759; restate the 🟡 as "`00` ⇒ no bud worn (0 counter-examples)"; the converse is refuted as a rule.
5. **Loss-cause log (§6 #1):** log a cause only once it is final (after the +1 s window), or mark the earlier one "provisional". Cosmetic.

## 10. Open questions

- 🔴 Why Android re-paged the Buds 3.4 ms after the ACL drop at 10:17:27 (G) but not at 10:15:55 (F) — same physical state (§1a).
- 🔴/🟡 When the Buds report Settable `00` with in-ear detection on (§3 hypothesis and its experiment).
- 🔴 Why the Buds closed the session 13 s after a re-insertion with in-ear detection off (#13) but not on four other wear changes (#5).
- 🔴 Whether the stale "100 %⚡" at I-4a was drawn dimmed (film resolution) — a screenshot test (`:ui` Robolectric) settles it without hardware.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-064-2026-10-01_10-04-14_10-29-28-Group_AZ/CAP-064-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-064-2026-10-01_10-04-14_10-29-28-Group_AZ/CAP-064-FINDINGS
