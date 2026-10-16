# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, the secondary user without Google Play services) — Group BM, 1.3.1: the four `CAP-076` / `ai-sessions/0093` fixes, the screen reader and the single mode label (`CAP-077`)

**Status:** ✅ **captured 2026-10-16** (one screen recording, 06:56:14.8 – 07:10:38.4, no audio stream) **and analyzed 2026-10-16 by `ai-sessions/0095`**
(CAP-077-FINDINGS.md). The film starts on **1.3.0 build `ff27ee1`** and shows the update to the release-signed **1.3.1 build `7aa524b`** (versionCode 10301,
`adb install` at 06:57:57, Android's "Updaten…" on screen 06:58:05–06), all in the **secondary user (user 10) without Google Play**. The maintainer's statement:
"I did not keep to the test plan." — the steps as run are mapped to BM-1 … BM-9 below ("The steps, as run"); the skeleton of `ai-sessions/0094` is kept as
written as the appendix. **Only this file and CAP-077-FINDINGS.md are in the repository:** every data file of this folder (the two HCI logs, the export, the app logcat, the logcat extract, the shell log, the checks log, the hashes, the recording) is kept local in the working folder and in ~/opencontrol-capture-originals/CAP-077/ — Git LFS refused every upload on 2026-10-16 (the account's LFS budget is exceeded and cannot be raised; the maintainer, chat 2026-10-16: "Alleen .md committen, data lokaal"); they are to be committed when LFS accepts uploads again (`TODO.md` §6). **After the run (Checkpoint 0 of `ai-sessions/0095`, chat 2026-10-16):** the originals copied to ~/opencontrol-capture-originals/CAP-077/;
the three mode-740 files set to 644; CAP-077-sha256sums.txt holds the hashes of the files as received; the full logcat kept local and committed as the
redacted extract CAP-077-logcat-extract.txt; the film **kept local, not committed** (the repository's Git LFS budget was exceeded at the push of 2026-10-16 and cannot be raised — the maintainer, chat 2026-10-16; the original is in ~/opencontrol-capture-originals/CAP-077/, SHA-256 `0c6e0318…`); the read-only phone checks of 07:42 and 07:56 saved as CAP-077-adb-checks-0095.log;
no "System log" file was made (the maintainer, Checkpoint 0 — the full logcat's system and main buffers cover the run); the folder renamed to the film's first
and last phone times and the recording to CAP-077-recording1.mp4.

Labels per `PROJECT_RULES.md` §1: 🟢 FACT, 🟡 HYPOTHESIS, ⚪ ASSUMPTION, ❓ OPEN QUESTION. Frame numbers are per log: **HL** = CAP-077-btsnoop_hci.log.last,
**H** = CAP-077-btsnoop_hci.log; **E** = the debug export (local time); **A** = the app logcat (UTC); **L** = the full logcat (local time; the committed extract
carries every quoted line); **s** = a film second (phone time = 06:56:14.82 + s). Every `tshark` filter was run on the Buds' ACL handle `0x000b` (the
Connection Completes with the Buds' address and that handle: HL 686; H 151 and 1678 — `bluetooth.addr` matches 0 frames in both logs, `bthci_acl.chandle==0x000b`
matches 286 / 1,815, exit 0 each). Modes: Message Stream byte `20` Off, `08` Noise cancellation, `80` Transparency, `40` Adaptive; setting 13: 1 Off, 2 Noise
cancellation, 3 Transparency, 4 Adaptive, 0 no mode settable. "Claim #n" = the n-th phone `SABM` on DLCI 0x04 in H (`scripts/message_stream_claims.py`); "S0 … S5"
= the MAESTRO sessions (S0 in HL, S1–S5 in H).

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-077` |
| Group(s) | BM |
| Date | 2026-10-16, film 06:56:14.8 – 07:10:38.4 (local, CEST) |
| Phone | Pixel 9a "tegu", GrapheneOS, Android 17 — A header `osVersion: google/tegu/tegu:17/CP3A.261005.005/2026100601:user/release-keys` |
| User | **the secondary user (user 10) without Google Play**: A header `userType: full.secondary`; L `am_switch_user: 10` 06:47:59.689; the app's processes run as uid 1010354 (L `am_proc_start: [10,25520,1010354,…]` 06:47:59.739, `[10,28176,1010354,…]` 06:58:07.419); the shell log's `pm list packages --user 10 \| grep -i -E "gms\|vending"` lists only GrapheneOS's three `app.grapheneos.gmscompat*` packages (exit 0 because they match "gms"); this session's check `grep -E "com.google.android.gms$\|com.android.vending$"` → nothing for user 10 (exit 1), both packages for user 0 (exit 0, positive control); **0 Play markers** (`03 08 00 02 01 25`, phone-sent) in HL and H (positive control: `CAP-076-btsnoop_hci1.log` → 18). Google Play services runs in user 0 meanwhile (L PID 2189, `NearbyDiscovery`/`NearbyFastPair` lines) — 0 of its lines in the window mention RFCOMM, the event stream or hearable controls |
| App under test | **1.3.1 (10301), build `7aa524b`** — on film "App: 1.3.1, build 7aa524b (2026-10-16)", no "-dirty" (s127–133, s779–783); the installed APK pulled by this session at 07:42 (`pm path --user 10` → `base.apk`, `adb pull`) has SHA-256 `9be3a89c94d3db7236416a244d2fea51e0524c6e5806d2e648faf4ca0611ee0a` = ~/opencontrol-1.3.1-tested (B1's); `lastUpdateTime=2026-10-16 06:58:05` unchanged since the run (CAP-077-adb-checks-0095.log) |
| Previous version | **1.3.0 (10300), build `ff27ee1`** — on film "App: 1.3.0, build ff27ee1 (2026-10-14)" (s49–57); the shell log 06:54:22 `versionCode=10300`, `lastUpdateTime=2026-10-15 09:11:11` (🟡 the published 1.3.0 installed with Obtainium after its release — `RELEASING.md` §13's 1.3.0 row, "D4 done 2026-10-15"; no log buffer of this capture reaches 2026-10-15 09:11: L's system buffer starts 10-16 06:13:08, events 06:16:35, main 06:55:55); after the update 06:58:37 `versionCode=10301`, `lastUpdateTime=2026-10-16 06:58:05`, both `firstInstallTime` lines unchanged (an update, no reinstall) |
| Official Pixel Buds app | not used |
| Buds | Pixel Buds Pro 2, `release_5.203` ×3 (the `GetSoftwareInfo` announcements, e.g. H 1027; Info on film) — no Safe Mode |
| Video file | CAP-077-recording1.mp4 (**kept local, not committed** (the repository's Git LFS budget was exceeded at the push of 2026-10-16 and cannot be raised — the maintainer, chat 2026-10-16; the original is in ~/opencontrol-capture-originals/CAP-077/, SHA-256 `0c6e0318…`)) — 863.54 s, 1080 × 2424, **video only, no audio stream** (`ffprobe -show_entries stream=codec_type` → `video`), variable frame rate (10,747 frames, `r_frame_rate=1/1`), `creation_time` 05:10:38Z = the film's end; Android's screen recorder ("Schermopname", "Show taps" on: every touch draws a dot) |
| Log files | HL (1,368 packets), H (7,368), E (706 lines), A (701 lines), L (197,535 lines, kept local; CAP-077-logcat-extract.txt 22,441 lines committed), the adb shell log (20 lines), this session's checks log |

## Capture-integrity pre-flight

| File | Measured (2026-10-16) | Covers |
|---|---|---|
| HL | 1,368 packets, 06:48:05.214 – 06:56:37.570 (`capinfos -c -a -e`) | before the film and its first 23 s: Buds ACL 686 06:56:27.263 → Disconnection Complete 1350 06:56:37.199 (reason `0x16`, the Bluetooth off on film s22); an LE link `0x0040` 06:49:37.889 – 06:56:37.429 (1353, `0x16`) to a GATT peripheral with the Heart Rate service (`0x180d`) and private 128-bit services — **not the Buds** (their only handle is `0x000b`) |
| H | 7,368 packets, 06:56:41.715 – 07:12:00.360 | from the Bluetooth on (s27): ACL 151 06:56:42.680 → 1589 07:00:17.763 (`0x13`, ended by the Buds); ACL 1678 07:00:35.590 (after the phone's `Create Connection` 1675, the app's Connect) → open at the log's end |
| E | 706 lines (`wc -l`; the toast said 707), 06:58:07.853 – 07:11:14.281, one process (PID 28176, 1.3.1) | the 1.3.1 process from its first line: 182 event lines, 191 pw_rpc summaries, 333 Debug-mode hex lines of received bytes (178 DLCI 0x02, 155 DLCI 0x04) |
| A | 701 lines, UTC 04:48:01 – 05:11:24 | PID 25520 (1.3.0, 21 lines) and 28176 (1.3.1, 459); the app's own tag only from 05:09:43 (24 lines — the log buffer the viewer read); the header's `package: …:10301` is the version at export time |
| L | 197,535 lines; crash/kernel buffers from 10-08, system from 10-16 06:13:08, events 06:16:35, main 06:55:55; ends 10-16 07:10:32 | the whole run except its last 6 s and the exports |
| Shell log | 06:53:39 – 06:58:37 | P0 (user 10's package list — `am get-current-user`'s answer went to the terminal, not into the file) and P1's two `dumpsys` (before and after the update) |
| System log | **not made** (the maintainer, Checkpoint 0) | — |

The boundary HL/H: HL ends 06:56:37.570 with the Bluetooth off, H starts 06:56:41.715 with the Bluetooth on (the snoop log restarts with the stack). pw_hdlc
frames on DLCI 0x02 of handle `0x000b`, reassembled per (DLCI, direction), split on `7e`, CRC-32 checked (`python3 scripts/pwhdlc_crc_table.py <log> 0x000b`;
positive control: `CAP-051` 1177's bytes pass, the same bytes with one bit flipped fail): **HL 35, H 320 — 0 CRC failures**, the same counts as `python3
scripts/pwrpc_decode.py --handle 0x000b <log> | wc -l`.

## Review method and privacy

- **Film:** decoded at 1 fps in three ranges (864 frames, scaled to 360 px); every second compared with the one before (pixel |Δ| > 25 below the status bar):
  **294 changed seconds — all viewed** on labelled contact sheets of five; the other 569 are unchanged (max |Δ| ≤ 25). Where a claim depends on it, the
  native frames were read with their own timestamps (`ffmpeg -fps_mode passthrough` + `showinfo`): the clock, the six tile taps, the mode buttons over every hold
  (the patch method of `scripts/film_mode_buttons.py` on native frames), the "Pair a device" tap.
- **Clock:** the first native frame with the new minute is at s 45.179, 105.187, 165.186, 225.188, 405.190, 585.223, 825.175 (the clock's redraw is itself a frame)
  ⇒ **phone time = 06:56:14.82 + s, ± 0.05 s, no drift**; the film covers 06:56:14.8 – 07:10:38.4. Cross-checks: the first tile tap's touch dot 07:01:51.865 vs
  E "ANC tile tapped" 07:01:51.969; the eight holds' buttons 6–20 ms from their stream packets (FINDINGS §4).
- **Audio:** none — the recording has no audio stream; the maintainer: "I did not speak during the video." Hold timing comes from the wire and the screen only.
- **Privacy:** the film shows the Wi-Fi network's name in Quick Settings (s16–21, s333–334, s365, s862–863) and the three serial numbers on Info (s49–57, s127–133,
  s779–783) — allowed on film, never written here (first 4 + last 2: Case 5707…51, Right 5708…09, Left 5707…47). E prints the Buds' address as `XX:XX:XX:XX:6E:07`;
  **E's Debug-mode hex lines include the five `GetHardwareInfo` answers, i.e. the serials** (as in `CAP-073`…`CAP-076`, committed as is subject to the
  maintainer's answer, `ai-sessions/0095` Phase D); the extract has the address (5 lines) and the six hex answers redacted.

## Event Timeline

One row per event: phone time · film second · what happened · step / Test-ID · evidence.

### Before the recording

| Time | What | Step / Test-ID | Evidence |
|---|---|---|---|
| 06:47:59 | the phone switched to user 10; 1.3.0 started there as the tile's bound service (PID 25520); Bluetooth's process moved to user 10 (PID 25783) | — | L `am_switch_user: 10` 06:47:59.689; `am_proc_start: [10,25520,1010354,…,bound-service,{…AncTileService}]` 06:47:59.739; `[10,25783,1001002,com.android.bluetooth,…]` 06:48:03.003 |
| 06:49:37 | an LE device (Heart Rate service) connected — not the Buds | — | HL 197 LE Enhanced Connection Complete, handle `0x0040`, random address |
| 06:53:39–06:54:22 | P0 (user 10's package list) and P1's first `dumpsys` (10300, `lastUpdateTime=2026-10-15 09:11:11`) | P0, P1 | the shell log |
| 06:56:12.958 | 1.3.0's MainActivity created (the app opened 2 s before the film) | — | A `wm_on_create_called` 04:56:12.985 UTC; L "Permissions (start)" 06:56:12.958 |

### The recording (06:56:14.8 – 07:10:38.4)

| Time | s | What happened | Step / Test-ID | Evidence |
|---|---|---|---|---|
| 06:56:15–25 | 0–10 | 1.3.0, Connection tab: "Paired — not connected to this phone", [Connect] | — | film |
| 06:56:25–28 | 11–14 | **Connect** (1.3.0): "connecting…" → ready; L/R 100 % ⚡, Case 100 %; "Worn: unknown — in-ear detection is off" | `PAIR-003` | L "Disconnected -> Connecting" 06:56:25.893; HL 686 ACL; S0 `SABM` 763, 17 requests 796–873 (ch 21; `SubscribeToSettingsChanges` 867, `ReadSetting 4:13` 868 → `4:{13:0}` 872 "no mode settable"); snapshot claim HL 795 → `Notify` 819 `e8 00 20` (Settable 00) |
| 06:56:29 | — | AVRCP: phone reports PlayStatus Paused → Stopped | — | HL 974, 1122 |
| 06:56:30–35 | 16–21 | shade: notification "Connected — ANC: OFF" (1.3.0's label); Quick Settings; the Bluetooth panel: the Buds "Actief, 100 %", a second saved headset | — | film (Wi-Fi name visible) |
| 06:56:36–40 | 22–26 | **Bluetooth off** | — | L "Bluetooth adapter: ON -> TURNING_OFF" 06:56:36.879; HL 1335 phone `DISC` DLCI 0x02 06:56:37.103, 1350 ACL `0x16` 06:56:37.199; L "Session loss cause: Bluetooth was switched off on this phone"; L `am_proc_died: [10,25783,com.android.bluetooth,…]` 06:56:37.886 |
| 06:56:41–43 | 27–29 | **Bluetooth on**; the Buds "Verbinding maken…" → "Actief" | — | L `am_proc_start: [10,27920,…,com.android.bluetooth,…]` 06:56:40.981; H starts 06:56:41.715; H 151 ACL 06:56:42.680 |
| 06:56:42.9–44.8 | 28–30 | 1.3.0 **re-opens by itself** (LINK_BACK): S1, 17 requests (ch 21), `4:{13:0}`; snapshot claim #1 (`Notify` `e8 00 20`) | (ADR-044) | L "Automatic re-open … LINK_BACK" 06:56:42.935; H `SABM` 399, requests 440–531, subscription 525, `4:13` 526 → 530; claim #1 464–585 |
| 06:56:47–49 | 33–35 | Connection tab; the battery (i): L/R 100 % charging in the case (06:56:43), Case 100 %; firmware `release_5.203` | `BATT-004` | film; H 530 runtime info `6:{1:{1:100 2:1} 2:{… 2:2} 3:{… 2:2}}` |
| 06:57:02–16 | 48–62 | gear → Settings (1.3.0: Dark mode, Case sounds, Use different Buds — no dialog in 1.3.0); **Info "App: 1.3.0, build ff27ee1 (2026-10-14)"**, serials; Debug: **Debug mode on** (already), "Unidentified frames (10)" | P1, `FW-003` | film s48–62 |
| 06:57:20–21 | 66–67 | ANC tab (1.3.0): "The Buds don't allow changing noise control right now …", Off selected | — | film; the reads' `4:{13:0}` |
| 06:57:23 | 69 | Sound: EQ 1.7 / 2.0 / 4.0 / 0.0 / −1.0 (top to bottom), presets "HEAVY BASS" … (1.3.0), Volume EQ on | — | film; H 451 `4:{16:{… 5:1.69}}` |
| 06:57:25–29 | 71–75 | Controls: modes "Off" unticked → **ticked** (1.3.0) | `HOLD-005` | H 860 `WriteSetting 4:{12:{1:1 2:1 3:1 4:1}}` 06:57:29.446 → stream 862, `RESPONSE` 863; L "Setting 12 written (channel 21)" 06:57:29.491 (the reads had `4:{12:{1:1 2:0 3:1 4:1}}`, H 502) |
| 06:57:31–33 | 77–79 | Find tab (no tap), Connection tab | — | film |
| 06:57:34–06:58:04 | 80–110 | screen unchanged (the laptop's `adb install`) | P1 | L `adbd service requested 'abb_exec:package` 06:57:57.501, `package install` (abb) 06:57:57.501; `installer_clear_app_data_caller` 06:57:58.471 |
| 06:58:05–06 | 111–112 | **the update to 1.3.1** — Android's "Updaten…" | P1 | L `am_kill: [10,25520,…,stop … due to killDueToPackageUpdate,…]` 06:58:05.898; H 984 phone `DISC` DLCI 0x02 06:58:05.955; the shell log 06:58:37 `versionCode=10301` |
| 06:58:07–09 | 113–115 | **1.3.1 starts (PID 28176) and re-opens by itself** (LINK_BACK): S2, 17 requests (ch 21), `4:{13:0}` "no mode settable"; snapshot claim #2 (`e8 00 20`); battery unavailable → L/R 100 % ⚡ → Case 100 % | BM-1, `PAIR-003` | L `am_proc_start: [10,28176,…,top-activity,…]` 06:58:07.419; E 06:58:07.853 first line, "Automatic re-open … LINK_BACK" 06:58:08.007; H `SABM` 1010, requests 1029–1114 (subscription 1108, `4:13` 1109 → 1113), claim #2 1045–1118 |
| 06:58:19–27 | 125–133 | Settings (1.3.1); **Info "App: 1.3.1, build 7aa524b (2026-10-16)"**, no "-dirty"; firmware (06:58:08), channel 21, serials (06:58:09) | BM-1, `FW-003` | film |
| 06:58:55–06:59:55 | 161–221 | Connection tab, Case 98 %; screen unchanged 06:58:57 – 06:59:55 | — | film; H 1204 runtime info Case 98 (06:58:32) |
| 07:00:02–04 | 228–230 | **Use different Buds → the dialog** "Use different Buds? Forgets which Buds this app controls (the Bluetooth pairing in Android stays). You then pick the Buds again with Pair a device." [Cancel] [Forget these Buds] → **Forget these Buds** (no Cancel on film) | BM-8 (A93-APP-08, A93-APP-01) | film; E "Session ended by the user's Disconnect tap" 07:00:05.202, "Pairing: use different Buds — removed 0 association(s) of this app; the next Pair a device opens the picker" 07:00:05.206; H 1504 phone `DISC` DLCI 0x02 07:00:05.206 |
| 07:00:09 | 235 | Connection: **"No Pixel Buds Pro 2 paired yet." [Pair a device]** — no Connect button; nothing connects by name | BM-8 (A93-APP-01) | film; H: no `SABM` on DLCI 0x02 from 1504 (07:00:05.206) to 1755 (07:00:35.744) |
| 07:00:08–13 | — | A2DP streams stop (the phone's `Suspend`s) | — | H 1514, 1554 |
| 07:00:17.763 | 243 | **the Buds end the ACL** (`0x13`) — 0.3 s before the tap on Pair a device (the status bar's Bluetooth-device icon is gone at 07:00:17.868) | — | H 1588 phone `+CIEV: 4,2` 07:00:17.713, 1589 Disconnection Complete `0x13`; native frames 07:00:17.735 / 17.868 / 18.085 (the touch dot) |
| 07:00:18–19 | 243–245 | **Pair a device** → "Waiting for you to pick your Pixel Buds in the system dialog…" → Android's CDM dialog for one device: "Toestaan dat de app OpenControl for Pixel Buds Pro 2 toegang heeft tot de Pixel Buds Pro 2 va?" [Toestaan] [Niet toestaan] | BM-9 | E "Pairing: association requested (CDM picker)" 07:00:18.280; L `CDM_AssociationRequestsProcessor: processNewAssociationRequest() request=AssociationRequest { singleDevice = true, deviceFilters = [BluetoothDeviceFilter{mNamePattern=(?i).*Pixel\s*Buds.*…` 07:00:18.283; `CDM_CompanionDeviceDiscoveryService: onDeviceFound() (BT) XX:XX:XX:XX:6E:07 'Pixel Buds Pro 2 va' - New device.` 07:00:19.235 |
| 07:00:24 | 246–249 | **Toestaan** → association created; "Paired — Android's connection state isn't known (yet)" [Connect] → "Paired — not connected to this phone" (s251) | BM-9 | E "association created" 07:00:24.052, "device resolved from the association's own device", "already bonded — no createBond()" 07:00:24.061; L `CDM_AssociationStore: Adding new association=[Association{mId=51, mUserId=10, …` 07:00:24.043; the checks log: mId=51 user 10 approved 07:00:24 |
| 07:00:34–37 | 260–263 | **Connect** (touch dot s260) → ready; L/R 100 % ⚡, Case 98 % | BM-9, `PAIR-003` | E "Disconnected -> Connecting" 07:00:35.352; H 1675 `Create Connection` 07:00:35.356, 1678 ACL; S3 `SABM` 1755, 17 requests 1784–1865 (ch 21; subscription 1859, `4:13` 1860 → `4:{13:0}` 1864); claim #3 1789–2070 |
| 07:00:37.67 | — | after claim #3's `DISC`, the Buds send three battery frames on the closing channel; the phone answers each with `DM` | — | H 2070 `DISC`, 2080/2083/2086 `03 03 00 03 e4 e4 ff`, 2081/2084/2087 phone `DM`, 2089 the Buds' `UA` |
| 07:00:43.37 | 269 | **the Buds close MAESTRO** (the Left bud leaves the case): "The app's channel was closed while Android still shows the Buds connected …", "IOException: bt socket closed, read return: -1" | `CASE-004` | H 2135 Buds `DISC` DLCI 0x02 07:00:43.366; E "Session lost: channel 0x02 closed … 5640 ms earlier" 07:00:43.373, cause "the Buds closed the channel" 07:00:43.415; H 2310 runtime info 07:00:47.454: Left not charging |
| 07:00:44.9–47.7 | 270–273 | **AFTER_LOSS re-open** 1.507 s later: S4 on **channel 19**, 17 requests; `4:{13:1}` Off ("from the mode read (read)"); snapshot claim #4 `Notify` `e8 e8 20`; L 100 % (no ⚡) then both without ⚡ | (ADR-044), `CASE-005` | E "Automatic re-open … AFTER_LOSS" 07:00:44.877; H `SABM` 2201, requests 2221–2319 (subscription 2306, `4:13` 2307 → 2317); claim #4 2246–2323 (`Notify` 2279); H 2326 runtime info 07:00:48.128: both not charging, no Case entry |
| 07:00:48.664 | 274 | the Buds switch the mode to **Adaptive** by themselves (both buds out of the case; in-ear detection off) — one stream packet, channel 19 | — | H 2339 `4:{13:4}`; E "Noise-control mode from the settings stream (changed_by_buds)" 07:00:48.670; the Connection tab is on screen (no mode shown there) |
| 07:00:55 | 281 | battery (i): L/R 100 % (07:00:46) not charging (07:00:48); "Case: 98 % — last seen 07:00:47 (no bud charging in the case)" | `BATT-004` | film |
| 07:01:01–13 | 287–299 | Controls: all four modes ticked; **In-ear detection on** (the whole row highlights — one toggleable row) | `INEAR-001` | H 2478 `WriteSetting 4:{2:1}` 07:01:07.023 → stream 2484, `RESPONSE` 2485; E "Setting 2 written (channel 19)" 07:01:07.157 |
| 07:01:16–20 | 302–306 | Sound (1.3.1): presets "Heavy bass", "Light bass", "Balanced", "Vocal boost", "Clarity", "Flat"; Balance Centre, Mono off | BM-2 (A93-APP-08) | film |
| 07:01:21–22 | 307–308 | ANC tab (1.3.1): "Noise cancellation", "Off", **"Adaptive" ✓**, "Transparency" — no "not allowed" line | BM-2 | film |
| 07:01:23–31 | 309–317 | Connection: "Probably worn (checked 07:00:48)"; battery (i) | `INEAR-006` | film |
| 07:01:47 | 333 | shade: the notification **"Connected — noise control: Adaptive"** | BM-2 (A93-APP-08) | film |
| 07:01:48–07:02:18 | 334–363 | Quick Settings page 2: the tile **"ANC / Adaptive"**; **six tile taps**, each changing the subtitle 1.1–1.3 s after the touch (0.35 s for tap 2): Adaptive → Off → Noise cancellation → Transparency → Adaptive → Off → Noise cancellation; "Off" drawn as the inactive tile | BM-5 (A93-APP-03), BM-2, `ANC-001`…`ANC-004` | E "ANC tile tapped (shown: ADAPTIVE)" 07:01:51.969, "(shown: OFF)" 07:01:54.680, "(shown: ACTIVE)" 07:01:58.938, "(shown: TRANSPARENT)" 07:02:04.669, "(shown: ADAPTIVE)" 07:02:09.608, "(shown: OFF)" 07:02:15.532; H claims #5 2798 (taps 1 and 2: `Notify` 2821 → `Set` 2823 `…20` → ACK 2826; `Notify` 2841 → `Set` 2842 `…08` → ACK 2844), #6 2886 (`Set` 2910 `80`, ACK 2913), #7 2956 (2982 `40`, 2984), #8 3025 (3052 `20`, 3054), #9 3092 (3119 `08`, 3122); stream pairs ch 19/21 2832/2833 … 3127/3128, all "(set_by_app)" |
| 07:02:19 | 365 | shade: "Connected — noise control: Noise cancellation" | BM-2 | film |
| 07:02:20–23 | 366–369 | Connection: "Probably worn (checked 07:02:16)" | `INEAR-006` | film |
| 07:02:26–48 | 372–394 | ANC tab: **five taps** Off, Transparency, Adaptive, Noise cancellation, Adaptive — each shown 0.2–0.4 s after its `Set`; the (i) "ANC mode: Transparency (updated 07:02:30)", "Connection: connected" and "ANC mode: Adaptive (updated 07:02:47)" | `ANC-001`…`ANC-004`, BM-2 | H claims #10 3263 (`Set` 3287 `20`, ACK 3290), #11 3330 (3355 `80`, 3357), #12 3501 (3526 `40`, 3529), #13 3567 (3594 `08`, 3597), #14 3673 (3697 `40`, 3700); stream pairs 3295/3298 … 3705/3706 "(set_by_app)"; buttons on native frames 07:02:27.742 OFF, 30.391 TRA, 38.840 ADA, 42.288 NC, 47.334 ADA |
| 07:02:49–07:03:17 | 395–422 | the ANC tab on screen, unchanged | — | film |
| 07:03:18–07:03:58 | 423–463 | **eight press-and-holds** (the ANC tab on screen): Transparency, Noise cancellation, Off, Adaptive, Transparency, Noise cancellation, Off, Adaptive — **each shown 6–20 ms from the Buds' push** (two of them 6–8 ms before it, within the clock's ±0.05 s) | BM-2/BM-4 context, `TOUCH-007` | H stream pairs 3857/3858 07:03:18.090, 3882/3886 22.512, 3900/3901 26.901, 3921/3924 31.363, 3964/3967 41.130, 4006/4007 47.639, 4037/4038 52.641, 4057/4058 58.203 (each ch 19 then ch 21); E "(changed_by_buds)" then "(unchanged)" for each pair; buttons on native frames 07:03:18.101 TRA, 22.532 NC, 26.895 OFF, 31.377 ADA, 41.122 TRA, 47.654 NC, 52.652 OFF, 58.217 ADA; no claim open (claims #14 ended 07:02:48.833, #15 starts 07:04:37.155); no A2DP stream (`Suspend` 3784 07:02:59.529 → `Start` 4075 07:04:00.852) |
| 07:03:49.39 | 454 | the Buds send AVRCP **PLAY** (pushed / released) — a bud gesture; no playback follows | — | H 4013, 4016 |
| 07:04:00 | 466 | the ANC (i): "ANC mode: Adaptive (updated 07:03:58)", **"Changed by the Buds at 07:03:58: the Buds reported a new mode — a press-and-hold on a bud, or a change they made themselves."** | `TOUCH-007` | film |
| 07:04:17–19 | 483–485 | Sound: **Upper treble dragged to 5,9** | `EQS-001` | H 4201 `WriteSetting 4:{16:{… 5:5.87}}` 07:04:19.727 → stream 4205, `RESPONSE` 4206 (E pw_rpc lines only; no "written" line for the EQ) |
| 07:04:26 | 492 | **Volume EQ off** (whole row highlights) | `AUDIO-002` | H 4252 `4:{15:0}` → 4260 / 4262 / 4265 (three stream packets), `RESPONSE` 4262; E "Setting 15 written" 07:04:26.795 |
| 07:04:28 | 494 | **Mono audio on** | `AUDIO-001` | H 4280 `4:{19:1}` → 4282, 4283; E 07:04:28.682 |
| 07:04:33–34 | 499–500 | **Disconnect** → "App control: not open yet — tap Connect" | BM-4 (done differently), `PAIR-003` | E "Session ended by the user's Disconnect tap" 07:04:34.444; H 4330 phone `DISC` DLCI 0x02 |
| 07:04:35–38 | 501–504 | **Connect** ≈ 2 s later, no hold in between: S5 (ch 19), 17 requests; `4:{13:4}` = the shown Adaptive ("from the mode read (read)"); snapshot claim #15 `Notify` `e8 e8 40`; "Probably worn (checked 07:04:37)" | BM-4 (done differently), `PAIR-003` | E 07:04:36.709 "Connecting", 07:04:37.955 "(read)"; H `SABM` 4359, requests 4377–4461 (subscription 4452, `4:13` 4453 → 4459); claim #15 4396–4465 (`Notify` 4422) |
| 07:04:38–52 | 504–518 | Sound: read back Volume EQ off, Mono on, Upper treble 5,9 (the EQ (i) "EQ updated: 07:04:37"); **Volume EQ on**, **Mono off**; (i) "Mono audio: changed 07:04:49" | `AUDIO-001`, `AUDIO-002` | H 4384 `4:{16:{… 5:5.87}}`, 4432 `4:{15:0}`, 4438 `4:{19:1}`; 4520 `4:{15:1}` → `RESPONSE` 4529; 4547 `4:{19:0}` → 4550 |
| 07:05:01–15 | 527–541 | Controls; the (i) of Touch controls ("read 07:04:37") and Press and hold (three "read 07:04:37" lines) | — | film |
| 07:05:16–47 | 542–573 | screen unchanged | — | film |
| 07:05:50–07:06:08 | 576–594 | **Use touch controls off, then on** (whole row highlights); (i) "changed 07:05:51" / "changed 07:06:02" | `TOUCH-001` | H 4845 `4:{4:0}` → 4853; 4977 `4:{4:1}` → 4980 |
| 07:06:14–29 | 600–615 | **mode "Off" unticked, then ticked** again (whole row highlights); (i) "changed 07:06:14" / "changed 07:06:23" | `HOLD-005` | H 5047 `4:{12:{1:1 2:0 3:1 4:1}}` → 5051; 5118 `4:{12:{1:1 2:1 3:1 4:1}}` → 5121 |
| 07:06:35–43 | 621–629 | **Head gestures off, then on** | `HEAD-001` | H 5195 `4:{29:1}` → 5198; 5246 `4:{29:2}` → 5249 |
| 07:06:51–57 | 637–643 | **In-ear detection off, then on** | `INEAR-001` | H 5309 `4:{2:0}` → 5312; 5352 `4:{2:1}` → 5359 |
| 07:07:08–20 | 654–665 | **Conversation detection off, then on** | `CONV-001` | H 5449 `4:{22:0}` → 5452; 5521 `4:{22:1}` → 5524 |
| 07:07:26–36 | 672–682 | **Multipoint off, then on** | `MULTI-001` | H 5542 `4:{11:0}` → 5545; 5600 `4:{11:1}` → 5603 |
| 07:07:45–08:12 | 691–716 | Find: **Ring Left** ("Ringing: Left earbud — tap Stop to end it."), **Stop**, **Ring Right**, **Stop** | `FIND-001`, `FIND-002` | H claims #16 5687 (Ring L, ACK, the Buds' `04 01 00 01 02`), #17 5804 (Stop `…00`, then Ring R `…01` in the same claim), #18 5931 (Stop); E hex 07:07:48.140–07:08:11.323 |
| 07:08:13–28 | 719–734 | the Find (i) | — | film |
| 07:08:30–07:09:05 | 736–770 | Settings: **Earbuds replaced off, on; Other alerts off, on**; the (i) "changed 07:08:40 / 07:08:49 / 07:09:03" | `CASE-001`, `CASE-002` | H 6146 `4:{28:0}`, 6167 `4:{28:1}`, 6219 `4:{27:0}`, 6271 `4:{27:1}`, each `RESPONSE` |
| 07:09:10 | 776 | Debug (1.3.1): Debug mode on; "Unidentified frames (101)" incl. "DLCI 0x04 Group=0x03 Code=0x0b 29 bytes" | — | film; E hex `03 0b 00 19 …` (17 claims from 07:00:46) |
| 07:09:13–17 | 779–783 | Info: "App: 1.3.1, build 7aa524b (2026-10-16)"; firmware (07:04:37); **control channel 19**; serials (07:04:38) | `FW-003` | film |
| 07:09:20–30 | 786–796 | Connection; battery (i) "Case: 98 % — last seen 07:00:47 (last connection)"; **pull to refresh** | `BATT-004` | E "Runtime info re-requested on Refresh (channel 19)" 07:09:31.252; H 6481; claim #19 6503 (`Notify` 6523 `e8 e8 40`) |
| 07:09:31–40 | 797–806 | "Probably worn (checked 07:09:32)"; (i) L/R updated 07:09:32 | `INEAR-006` | film |
| 07:09:41–49 | 807–815 | ANC tab, **pull to refresh**; the (i) "ANC mode: Adaptive (updated 07:09:43)" | — | claim #20 6622 (`Notify` 6641 `e8 e8 40`) |
| 07:09:50–07:10:35 | 816–861 | the ANC tab, unchanged | — | film |
| 07:10:36–38 | 862–863 | shade: "Connected — noise control: Adaptive"; the recording stopped | — | film |

### After the recording

| Time | What | Evidence |
|---|---|---|
| 07:10:39 | AVRCP "Playing" for 6 s (07:10:39.864 → Stopped 07:10:45.502) — the only playback state of the whole H log | H 6955, 6982 |
| 07:10:39–07:11:24 | MainActivity stopped and restarted three times (the export's save dialog, the app logcat) — no re-creation in the run (one `wm_on_create_called` for PID 28176, 04:58:07.860 UTC) | A `wm_on_stop_called` / `wm_on_restart_called`; E "Android link observer stopped/started" 07:10:39 … 07:11:14 |
| 07:10:55 | the app process received SIGQUIT ("reacting to signal 3", "Wrote stack traces to tombstoned") — no crash; the sender is not in any log of this capture (L ends 07:10:32) | A 05:10:55.196 UTC |
| 07:11:17–20 | the debug export ("Debug log exported (707 lines)"); the app logcat at ≈ 07:11:24 | A 05:11:20.675 UTC |
| 07:42, 07:56 | this session's read-only phone checks (user 10 current, the APK hash, versionCode, the packages, the CDM associations) | CAP-077-adb-checks-0095.log |

## The steps, as run (mapped to BM-1 … BM-9)

| Step | Planned | As run | Result |
|---|---|---|---|
| BM-1 | update over 1.3.0; Info; Settings | done: `adb install` 06:57:57 → "Updaten…" 06:58:05; Info "1.3.1, build 7aa524b" (s127); Settings | ✅ as expected (FINDINGS §0, §2) |
| BM-2 | the labels | done: buttons, the (i) "ANC mode: …" / "Connection: connected", the notification "noise control: …", the tile's subtitle, "Heavy bass" — one name per mode everywhere | ✅ (FINDINGS §5.6) |
| BM-3 | TalkBack | **not done** — no TalkBack focus frame anywhere on film; the merged rows are seen only as one ripple per row | expected but not observed (FINDINGS §5.5) |
| BM-4 | Disconnect, two holds while disconnected, Connect, the (i) | **done differently**: Disconnect 07:04:34 → Connect 07:04:36 with no hold in between; the eight holds came 07:03:18–58, connected | the "changed since" fix **not exercised** (FINDINGS §5.2) |
| BM-5 | ten tile taps within ≈ 2 s of Ready | **done differently**: six tile taps 07:01:51–07:02:15, 66 s after the last session's Ready (07:00:45.457) | 6 of 6 answered by a `Set` and its ACK (FINDINGS §5.3) |
| BM-6 | a hold within ≈ 1 s of Connect | **not done** — no stream packet between any `ReadSetting` request and its answer | not exercised (FINDINGS §5.4) |
| BM-7 | Use different Buds → Cancel | **not done** — the dialog was answered with Forget at once | expected but not observed |
| BM-8 | Forget; rotate; Connect | **done, without the rotation and without a Connect tap** (the screen offers only *Pair a device*) | ✅ no connection by name (FINDINGS §5.1) |
| BM-9 | Pair a device → the same Buds | done: the CDM dialog → Toestaan → association mId 51 → Connect | ✅ |
| BM-end | export, app logcat, APK pull | done (07:11:17, ≈ 07:11:24); the APK pulled by this session at 07:42 | ✅ |

Done beyond the plan: the Bluetooth off/on with 1.3.0 (06:56:36–43); a mode-list write in 1.3.0 (06:57:29); five ANC-tab taps; eight holds; an EQ band, Volume
EQ, Mono, Touch controls, the mode list, Head gestures, In-ear detection, Conversation detection, Multipoint and both Case sounds, each off and on again
(21 writes, all acknowledged); Ring Left/Right and Stop; a battery Refresh and an ANC Refresh by pull.

## Test-IDs (`AGENTS.md` §13 step 7; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`)

| Test-ID | Planned (skeleton) | In this run |
|---|---|---|
| `PAIR-003` | BM-1, BM-4, BM-6, BM-8, BM-9 | ✅ the Connects 06:56:25, 07:00:35, 07:04:36; the re-opens 06:56:43, 06:58:08, 07:00:45 |
| `FW-003` | BM-1 | ✅ Info with the serials (s49–57, s127–133, s779–783); one `GetHardwareInfo` per session |
| `ANC-001`…`ANC-004` | BM-4, BM-5 | ✅ six tile taps and five tab taps (all four modes) |
| `TOUCH-007` | BM-4, BM-6 | ✅ eight holds 07:03:18–58 (none while disconnected, none during a Connect) |
| `PAIR-001` | — | ⚠️ not a bonding handshake: the Buds stayed bonded ("already bonded — no createBond()"); only a new CDM association — expected but not observed |
| `CASE-004` / `CASE-005` | — | ✅ (inferred, not on film) Left out of the case ≈ 07:00:43 (the Buds' `DISC`, H 2310), Right ≈ 07:00:48 (H 2326) |
| `INEAR-002` / `INEAR-003` | — | ❓ not observable (no camera; in-ear detection off until 07:01:07) |
| `INEAR-006` | — | ⚠️ the line shown ("Probably worn (checked …)") but no ears on film — expected but not observed as a test |
| `INEAR-001`, `TOUCH-001`, `HEAD-001`, `HOLD-005`, `CONV-001`, `MULTI-001`, `AUDIO-001`, `AUDIO-002`, `CASE-001`, `CASE-002`, `EQS-001`, `FIND-001`, `FIND-002`, `BATT-004` | — | ✅ each written and acknowledged, or read, as listed in the timeline |

## Appendix — the planned procedure (the skeleton of `ai-sessions/0094`, 2026-10-15, as written)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0094`, 2026-10-15). The hardware run of **1.3.1** (versionCode 10301), the fix release
the maintainer chose in chat 2026-10-15 ("1.3.1 met APP-01…06"): "Use different Buds" now acts on the pairing the repository connects with (A93-APP-01) and
asks first (A93-APP-08); a re-open's "Changed since the app last saw it" is kept when the same session's `ReadSetting 4:13` answers the same mode (A93-APP-02,
`CAP-076` §6.2); a tap's `Set` no longer takes the `Get`'s own `Notify` as its answer (A93-APP-03, `CAP-076` §5.3); a settings-stream push no longer ends a
pending read (A93-APP-04); the switch rows and EQ sliders are read with their names by TalkBack (A93-APP-07); one name for each mode on the buttons, the (i), the
tile and the notification (A93-APP-08). **Nothing new on the wire:** the app's requests are the 1.3.0 ones (the `transport.send` call sites are unchanged,
`ai-sessions/0094` RESULT). **One part, ≤ 15 minutes on film with sound**, in the **secondary user (user 10) without Google Play** (`ai-sessions/0093` §7 D4 — no
collision with Play on DLCI 0x04). The maintainer films; a later CAPTURE session analyses it and gives the release verdict (`RELEASING.md` C3/C4). After the run:
rename this folder to the screen recording's first/last phone times and the recording to CAP-077-recording1.mp4.

**Not in this run** (say so if asked): the user with Google Play (the per-tap collision is unchanged — `CAP-076` §5); a second pair of Buds (A93-APP-01 is
tested with the same pair chosen again in Android's picker); the case-sound experiment; the wear sequences.

### Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-077` |
| Group(s) | BM (new) |
| Date | TBD |
| Phone | Pixel 9a, GrapheneOS (Android 17) — **the secondary user (user 10) without Google Play** (`CAP-076` part 2's user) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.3.1** (versionCode 10301), the release APK of `scripts/release.sh 1.3.1` (B1), kept as `~/opencontrol-1.3.1-tested` (B3) — **Info tab on film**: "App: 1.3.1, build <B1 hash> (<date>)", no "-dirty" |
| Previous version | 1.3.0 build `ff27ee1` (installed 2026-10-15, `CAP-076`) |
| Official Pixel Buds app | not used |
| Buds | Pixel Buds Pro 2, firmware `release_5.203` expected (any other ⇒ Safe Mode: say so, stop) — both buds **worn** for the hold and tap steps |
| Video files | TBD — the screen recording **with sound** (say each action aloud); no camera |
| Log files | TBD — the HCI snoop log, the app's debug export (Debug mode on), the app logcat, the P0/P1 outputs |

### Preparation

| # | Check | Done |
|---|---|---|
| P0 | `adb shell am get-current-user` (10); `adb shell pm list packages --user 10 \| grep -i -E "gms\|vending"; echo "exit=$?"` (nothing, exit 1); positive control `grep opencontrol` (exit 0) | ☐ |
| P1 | In the installed 1.3.0: Debug mode **on** (on film). `adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds \| grep -E "firstInstallTime\|lastUpdateTime\|versionCode"` → save. `adb install -r` the tested 1.3.1 APK; the same `dumpsys` again (versionCode 10301) | ☐ |
| P2 | HCI snoop log on | ☐ |
| P3 | Android's **screen recorder with sound** on; the status bar across a minute change at the start and the end | ☐ |
| P4 | **Do Not Disturb on**; TalkBack **off** at the start (BM-3 turns it on and off again) | ☐ |
| P5 | Buds charged, both worn; Android shows them connected before the app is opened; **no app use before the film** | ☐ |

**Rhythm:** one action, then 5–10 s; say what you do. Export the debug log at the end. Serials: never unredacted in any note (first 4 + last 2 characters).

### Steps

DLCIs are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05). Every MAESTRO request starts `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25` on
**channel 19** or `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25` on **channel 21** (Info → "Control channel"). **Reference frames** — "H1" =
`CAP-076-btsnoop_hci1.log` (1.3.0 `ff27ee1`, handle `0x000b`), re-derived by `ai-sessions/0094` with `tshark -r CAP-076-btsnoop_hci1.log -Y "frame.number==N"
-T fields -e frame.number -e frame.time -e bthci_acl.chandle -e btrfcomm.dlci -e data.data` (exit 0):

| Request / event | Reference |
|---|---|
| The Connect's seventeen MAESTRO requests, with `SubscribeToSettingsChanges` and `ReadSetting 4:13` after `SubscribeRuntimeInfo` | `CAP-076` §2 (H1 4129 → 4130 on channel 21, byte-identical to `CAP-051` 1177/1178); unchanged in 1.3.1 |
| **The snapshot `Get`'s answer, then the equal `4:13` answer** (A93-APP-02) | H1 **4094** 06:41:33.418 `0813000401e8e808` (DLCI 0x04, Noise cancellation) → **4135** 06:41:34.165 `7e00a5032a0422026802080110151dea71de7d5e2551aed0aef4aa8ebc7e` (`4:{13:2}`, +747 ms) — in 1.3.0 the export said "Noise-control mode from the mode read (read)"; in 1.3.1 the (i) keeps "Changed since the app last saw it" and the export says "(unchanged)" |
| **A tile tap right after the `Get`'s answer** (A93-APP-03) | `CAP-076` §5.3 (06:46:08.96: no `Set` on the wire, nothing said) — in 1.3.1 every tap ends in a `Set` (`08 12 …`) with its ACK, or in a failure message |

Anything else from the app on the wire (a request not in this table or in `CAP-076` §2, a claim of DLCI 0x04 that is not a tap, the tile, Refresh, a pull or a
Connect's snapshot, a `WriteSetting 4:{13:n}`) is a finding.

| Step | Action (say it aloud) | Expected on screen | Expected on the wire | Test-ID | Result |
|---|---|---|---|---|---|
| BM-1 | P1's update; open the app; Info; gear → Settings | "App: 1.3.1, build …"; Settings = Dark mode, Case sounds, Use different Buds | the seventeen requests | `PAIR-003`, `FW-003` | |
| BM-2 | The labels: the ANC tab and its (i); pull the notification shade; the tile; the Sound tab's presets | the (i) "ANC mode: Noise cancellation …" (not "ACTIVE"), "Connection: connected"; the notification "Connected — noise control: …"; the tile's subtitle the same word; the presets "Heavy bass", not "HEAVY BASS" | — | | |
| BM-3 | TalkBack on: Settings → Case sounds (touch each switch row); Controls → a switch row; Sound → the Bass slider; the bottom tabs; TalkBack off | each row read with its name and state ("Earbuds replaced, switch, on"); the slider "Bass, … dB"; each tab named once | — | | |
| BM-4 | Disconnect; two press-and-holds while disconnected (the mode changes); Connect; the ANC (i) | after the Connect: **"Changed since the app last saw it (read at HH:MM:SS) …"** stays (A93-APP-02) | the snapshot `Get`'s `Notify` (new mode), then the `4:13` answer with the same mode, as H1 4094 → 4135; the export "(unchanged)" for the second | `PAIR-003`, `TOUCH-007`, `ANC-001`…`ANC-004` | |
| BM-5 | Right after a Connect (within ≈ 2 s of "Ready"): ten taps on the tile, ≈ 3 s apart | every tap changes the mode or says why not — **none silent** (A93-APP-03) | per tap: `08 11`, its `Notify`, then `08 12 …` and the ACK (or the failure) | `ANC-001`…`ANC-004` | |
| BM-6 | A press-and-hold during a Connect (tap Connect, hold within ≈ 1 s) | the held mode shown | a `4:{13:n}` stream packet before the `4:13` answer is possible; the export's "Noise-control mode read (channel N)" follows the RESPONSE, not the push (A93-APP-04) | `TOUCH-007`, `PAIR-003` | |
| BM-7 | Settings → **Use different Buds** → **Cancel** | the dialog closes; nothing changes | — | | |
| BM-8 | **Use different Buds** → **Forget these Buds**; rotate the phone; Connect | not connected to the Buds by their name (A93-APP-01): the app asks to pair (*Pair a device*) | **no `SABM`** on DLCI 0x02 after the Forget until BM-9 | `PAIR-003` | |
| BM-9 | *Pair a device* → choose the same Buds in Android's picker | connected; the seventeen requests | the Connect's requests | `PAIR-003` | |
| BM-end | Export the debug log; the app logcat; the APK pull | "Debug log saved (N lines)." | — | | |

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-077-2026-10-16_06-56-14_07-10-38-Group_BM/CAP-077-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-077-2026-10-16_06-56-14_07-10-38-Group_BM/CAP-077-EVENT-NOTES
