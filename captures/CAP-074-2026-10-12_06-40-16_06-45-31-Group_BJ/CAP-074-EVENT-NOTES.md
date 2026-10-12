# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, the Owner user **with** Google Play services) — Group BJ, the released 1.2.0 (`CAP-074`)

**Status:** ✅ **Captured 2026-10-12 and analyzed 2026-10-12** (`ai-sessions/0086`). One Android screen recording **with an audio track** (phone
06:40:15.8–06:45:31.0), two HCI snoop logs, one app debug export, the app logcat (saved from Android's App info → "View logs") and the full system log.
**No camera film, no shell log, no APK pull** (the maintainer's ad-hoc run; there was no skeleton). Checkpoint 0 of `ai-sessions/0086` (the maintainer's
choices in chat 2026-10-12): Group **BJ**; the folder renamed from the placeholder CAP-074-2026-dd-mm_hh-mm-ss_hh-mm-ss-Group_BJ to the recording's first/last phone
times; every file `chmod 644`; CAP-074-sha256sum.txt written; the recording committed **as is, with its audio track**; of the system log only the extract
`CAP-074-logcat-extract.txt` is committed (the full log and the original recording in `~/opencontrol-capture-originals/CAP-074/`). The analysis is `CAP-074-FINDINGS.md`.

**What the maintainer did (chat 2026-10-12, translated):** in the GrapheneOS user **with** Google Play services, with the released 1.2.0 (build `5b4d5db`): a
Disconnect and a Connect, then several press-and-holds on a bud; "the ANC mode changed audibly every time, but the app's mode changed only after the first hold";
in the user without Play services (`CAP-073`) every hold changes the app's mode. **The maintainer's statements, each verified below:** the app is 1.2.0 build
`5b4d5db` (✅ Info tab on film, t=6); Pixel 9a, GrapheneOS Android 17, a user with Play services (✅ `CP3A.261005.005` in both logcat headers; `com.google.android.gms`
uid 10205 and `app.grapheneos.gmscompat` running; the app's processes run as `User=0`, the Owner); "during the video I spoke only to say when I had done a left or a
right press-and-hold" (the words cannot be heard by this analysis — 33 short voiced segments of 0.4–0.9 s are measured, each 0.1–1.5 s after a `Notify` on the wire,
no other speech or music energy); "switching with a press-and-hold during playback worked; the app followed only right after a Disconnect/Connect" (✅ for the
app's behaviour; ❌ **no playback during the film**: AVRCP reads Paused/Stopped from 06:30 to 06:45:35 and the audio track carries no music — the one "Playing"
is at 06:45:35–06:45:53, after the recording). The maintainer wrote "Group BI" in chat; **BI is `CAP-073`, this run is Group BJ** (confirmed at Checkpoint 0).

**Prefixes used below:** "B" = `CAP-074-btsnoop_hci.log` (2026-10-11 19:50:00.312 – 2026-10-12 06:52:09.044, 18,097 packets — the run and the 06:30–06:31 window
before it), "A" = `CAP-074-btsnoop_hci.log.last` (2026-10-11 09:20:50.730 – 19:50:00.311, 64,787 packets — the previous rotation, ends 1 ms before B starts; it
holds nothing of 2026-10-12). E = the debug export `CAP-074-opencontrol-debug-20261012-070305.txt` (line numbers; process 13677 only, 06:38:10.182–07:03:04.708
local). L = the app logcat `CAP-074-OpenControl-for-Pixel-Buds-Pro-2-log-5e00d0eabad2.txt` (**UTC**; 351 lines, 04:30:20.102–04:46:12.407; its `OpenControlBuds`
lines from 04:44:13.366 equal E's lines 06:44:13.366 → 07:03:04 up to a 1-ms rounding, as in `CAP-073`). X = the system log
CAP-074-System-log-3d4f8ff69fde.txt (**UTC**; 81,044 lines; `main` buffer only from 04:45:21.795 UTC, `events` and `system` further back). S = the screen
recording, cited as film seconds `t` (the recorder's own timer reads t + 1 or t + 2); **phone time = 06:40:15.8 + t (± 0.2 s)** — the status-bar minute changed at
film 44.11–44.32 (06:41:00), 103.99–104.34 (06:42:00), 163.99–164.33 (06:43:00) and 283.99–284.31 (06:45:00), all four within 0.1 s of each other. All times
below are phone time (+02:00) unless marked UTC.

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-074` |
| Group(s) | BJ (new) |
| Date | 2026-10-12, phone 06:40:15.8–06:45:31.0 (screen recording); the Buds' ACL of the run 06:31:12.317 → beyond the log's end 06:52:09 (B 10528, handle `0x000b`) |
| Phone | Pixel 9a, GrapheneOS `google/tegu/tegu:17/CP3A.261005.005/2026100601:user/release-keys` (L line 2, X line 2, = `CAP-072`/`CAP-073`); **the Owner user (user 0)** — `am_proc_start: [User=0,PID=13677,UID=10354,Process Name=io.github.tedsluis.opencontrolpixelbuds …]` (X 04:38:09.372 UTC); no user switch in X (0 `am_switch_user`/`uc_switch_user`); no `userType:` line in L's header (it had `full.secondary` for user 10 in `CAP-073`) |
| Google Play services | **present and active in this user:** `com.google.android.gms` uid 10205 (`am_proc_start … UID=10205`, X 10-11 19:53:29), 232 `NearbyDiscovery` + 129 `FastPair` lines (X, 04:45–04:46 UTC — the `main` buffer's reach), `app.grapheneos.gmscompat` notifications (X 04:39:21, S t=314 "GmsCompat"); **22 Play-services claims of DLCI 0x04 in B** (`frame contains 03:08:00:02:01:25`, positive control `CAP-066-btsnoop_hci.log.last` → 22) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.2.0, build `5b4d5db` (2026-10-10)**, versionCode 10200 — Info tab S t=6, no "-dirty"; `package: io.github.tedsluis.opencontrolpixelbuds:10200, targetSdk 34` (L line 4); **the installed APK's hash is not verified** (no pull, no shell log — the maintainer attaches the phone or not, Checkpoint 0); the build on film is the one published 2026-10-10 (`ai-sessions/0085`) |
| Official Pixel Buds app | not seen (no `maestro.companion` process in X on 10-12); DLCI 0x08 "GSND CONTROL" was opened by the phone at 06:30:24 (B 9763) — another client, 🟡 as in `CAP-059`–`CAP-061` |
| Buds | Pixel Buds Pro 2, `…:07`, classic handle `0x000b`; firmware `release_5.203` ×3 in 10 of 10 announcements (E) and on the Info tab (t=6); serials (redacted) Case `5707…51`, Right bud `5708…09`, Left bud `5707…47` (E's hex lines; **readable on the Info tab at t=6** — never written unredacted here) |
| Other devices | none on the Buds' handle; handle `0x0041` (an LE peer) disconnected 2026-10-11 20:48:29 (B 3575) — before the day of the run |
| Audio | **AAC track** (13,571 frames): the maintainer's cue words (33 segments in the run, −22 to −37 dB peak, 0.4–0.9 s each); no music |
| Video | `CAP-074-recording.mp4` (315.17 s, 1080 × 2424, 2,559 frames ≈ 8.1 fps variable; `creation_time` 2026-10-12T04:45:34Z = 3 s after the computed end — the file's finalisation) |
| Log files | B, A, E, L, X (committed as the extract `CAP-074-logcat-extract.txt`, 2,370 lines) and CAP-074-sha256sum.txt |

## Capture-integrity pre-flight

- **B is the run; A is the day before.** `capinfos -a -e`: A ends 2026-10-11 19:50:00.311, B starts 19:50:00.312 — a rotation, not a prefix (`cmp -n 1100280 A B`
  → "differ: byte 20", exit 1). A's last Buds ACL ended 2026-10-11 18:42:48 (A 58630, `0x13`); nothing of 2026-10-12 is in A. A does show the same pattern on
  2026-10-11 17:31–17:46 (Play claims of 772 s, 15 s, 66 s; app claims released after 1.6 s or cut after 4.6 s — `ai-sessions/0086` RESULT, ms_inventory_last.txt) — the
  Owner user's day before, not analysed further.
- **The run's ACL** is B 10528 (Connection Complete 06:31:12.317, after the phone's `Create Connection` B 10526 at 06:31:11.866); the earlier ACL B 9121 (06:30:18.228,
  the Buds' `Connection Request` B 9117) ended at B 10473 (06:31:07.373, reason `0x16` "terminated by local host"). No other ACL event on `0x000b` in B
  (`tshark -r B -Y "bthci_evt.code==0x03 || bthci_evt.code==0x05"`, exit 0: 3575, 9121, 10473, 10528).
- **Every filter below is scoped to `bthci_acl.chandle==0x000b`**, shown matching: `tshark -r B -Y "bthci_acl.chandle==0x000b && btrfcomm.dlci==4 && frame.number==14500"`
  → the app's `SABM` of 06:40:51.246 (exit 0).
- **The export is one process, in local time:** E 1 "Permissions (start) …" 06:38:10.182 = L `wm_on_create_called` 04:38:10.189 UTC (process 13677; the earlier process
  12456, 04:30:19–04:38:05 UTC, left no export — its lines are in X's `events` buffer only). E runs 18 minutes past the film (to 07:03:04).
- **The system log's reach** (X): `kernel` from 10-08, `system` from 10-11 19:36:11, `events` from 10-11 (line 25050), **`main` only from 10-12 04:45:21.795 UTC** (line 63393)
  — so the Bluetooth process's RFCOMM port lines of the collisions (the `RFCOMM_CreateConnectionWithSecurity: already at opened state` / `cleanup_rfc_slot` /
  `RfcommEventStreamMedium` lines `ai-sessions/0039` §2.4 and `CAP-065`/`CAP-066` quoted) are **not retained for this run**: 0 of each in X (`grep -c`, exit 1 each;
  positive control: `grep -c NearbyDiscovery` → 232). The `events` buffer carries the app's lifecycle and `am_wtf`; the `main` buffer carries the last 75 s only
  (Play services' `NearbyDiscovery`/`NearbyFastPair` lines for its claim of 06:45:21, see the timeline).
- **The clock offset** is derived twice: the status-bar minute changes (above) and E's "Disconnected -> Connecting" 06:40:50.304 = the Connect tap on film t=34
  (touch indicator on the Connect button, S t=34; 06:40:15.8 + 34.5 = 06:40:50.3 ✓).

## Video review method (and privacy)

- **Screen recording:** decoded at 1 frame/s (315 frames, 540 px wide, scratch `diffs.py`); each second compared with the previous one below the status bar (pixels
  with a difference > 40): **240 seconds unchanged** (only the recorder's timer), **75 changed** — every changed second and t=0 viewed on 13 labelled 3 × 2 sheets.
  Narrowed at the full frame rate for the minute changes (status-bar crops at 06:41, 06:42, 06:43, 06:45).
- **Audio:** `ffmpeg -vn` → mono 16 kHz; `silencedetect` (−35 dB, ≥ 0.3 s) → 34 voiced segments (the first is the recorder's start click at 0.0–0.58 s); RMS per
  100 ms (`astats`) confirms no sustained energy anywhere (peaks −22 … −37 dB in the segments, silence elsewhere). **Nothing said can be cited as words** — the
  segments' *timing* is measured; that they are the cue words "left/right press-and-hold" is the maintainer's statement.
- **Privacy on film:** no Wi-Fi name (the Quick Settings tiles at t=314 carry no SSID label), no phone number, no notification content beyond "GmsCompat • Play Integrity
  API usage WhatsApp used the…" and "USB-foutopsporing verbonden" (t=314); the home screen (t=0) shows app folders only; the **Info tab shows the serial numbers in full
  (t=6)** — allowed on film (ADR-010 scope, as `CAP-072`/`CAP-073`), never written unredacted in any text. The audio track carries the maintainer's voice (cue words only,
  by their statement). Whether the audio stays and whether anything is blurred: Checkpoint 0.
- **Privacy in the logs:** X carries the Wi-Fi SSID (165 lines), the Buds' address (47 lines; 106 address-shaped strings in all), no e-mail address, no phone number,
  no serial. E carries the `GetHardwareInfo` answer inside 10 Debug-mode hex lines (ADR-058 item 5 allows it), no address; L carries the same hex (3 lines) and
  Android's own `XX:XX:XX:XX:6E:07` (18 lines). The full system log stays off the repository; an extract as in `CAP-072`/`CAP-073` is proposed at Checkpoint 0.

## Event Timeline

Columns: phone time · what happened (actor) · step / Test-ID · evidence. "Claim" = the app's `SABM` on DLCI 0x04 answered by `UA`, its first message `08 11 00 00`;
"Play claim" = a `SABM` whose first phone message is `03 08 00 02 01 25` (Play services' platform-type message, `CAP-065`/`CAP-066`'s rule). "Cut" = the phone's
`DISC` of the **app's** claim without a release line in E, logged there as "RFCOMM on-demand channel 0x04 closed (IOException: bt socket closed, read return: −1) …
Message Stream hold ended: the channel was closed, not by this app". "Killed" = the phone's `DISC` of **Play's** claim at the moment the app's first connect attempt
fails ("connect failed after N ms (attempt 1/3)", E). **The Buds' `Notify` is listed with the claim it arrived on** — that client is the only one that received it.

### Before the recording — the earlier app process (06:30–06:38) and the app start (06:38–06:40)

| Phone time | Event | Step · IDs | Evidence |
|---|---|---|---|
| 06:30:18.1 | the Buds connect (Connection Request B 9117, ACL B 9121, handle `0x000b`); HFP on DLCI 0x09 (the Buds' `SABM` B 9394) | — | B 9117–9394 |
| 06:30:19.5 | app process 12456 started in **user 0**, MainActivity created 04:30:20.144 UTC | before the film | X `am_proc_start`; L 11 |
| 06:30:20.1–21.8 | **Play claim** B 9392 (`03 08 00 02 01 25`, then `08 11` → `Notify` `e8 e8 40` B 9447) — killed at 06:30:21.783 (B 9602) | — | B 9392–9602 |
| 06:30:22.9–25.1 | app claim B 9659 (`08 11` → `Notify 40` B 9715; `Notify 20` B 9799 — a `Set`? no `08 12` on this claim: 🟡 a mode change by the Buds), released 2.24 s | the Connect's snapshot (no export for this process) | B 9659–9803 |
| 06:30:23.8 / 24.3 | the phone opens DLCI 0x0a (GSND AUDIO, B 9733) and DLCI 0x08 (GSND CONTROL, B 9763: `05 0c`, `04 02`, `04 04`, `04 11`, `04 13` …, the `CAP-059`/`CAP-061` burst) — **not this app** (it opens only 0x02 and 0x04) | — | B 9733–9830 |
| 06:30:26.4–27.0 | app claim B 9885 (0.66 s) | — | B 9885–9926 |
| 06:30:29.3–31.8 | Play claim B 9947 (2.4 s; killed) | — | B 9947–10059 |
| 06:30:32.3–36.4 | app claim B 10080 (4.09 s) — `Notify 20` then `40` ×2 (B 10103–10110; a `Set 40`? the claim carries no `08 12`: 🟡 the mode list write below) — **cut** at 06:30:36.415 (B 10122) | — | B 10080–10122 |
| 06:30:3x | the first process writes `4:{12:{1:1 2:0 3:1 4:1}}` (Off unticked; B 10196 → OK 10198), balance `4:{17:1}` (B 10268 → 10271) and `4:{17:0}` (B 10292 → 10294) | before the film | `scripts/pwrpc_decode.py B` lines 32–38 |
| 06:30:37.2–06:31:06.4 | Play claim B 10143 (29.2 s; `Notify 40` B 10169) | — | B 10143–10451 |
| 06:31:07.4 | **ACL ended by the local host** (B 10473, reason `0x16`); the phone pages the Buds again 06:31:11.866 (B 10526), ACL B 10528 06:31:12.317 — the run's ACL | — | B 10473–10528 |
| 06:31:14.3–18.2 | app claim B 10655 (3.84 s; cut) | — | B 10655–10733 |
| 06:31:19.5–27.0 | Play claim B 10980 (7.6 s; killed) | — | B 10980–11309 |
| 06:31:27.7–32.6 | app claim B 11332 (4.94 s; `Notify 40`, then **unprovoked `80`** B 11387 at 06:31:30.659; cut) | — | B 11332–11411 |
| 06:31:35.1–44.1 | Play claim B 11439 (8.9 s; `Notify 08` B 11466, `40` B 11501) | — | B 11439–11592 |
| 06:31:44.6–49.6 | app claim B 11638 (5.00 s; `Notify 40`, unprovoked `80` B 11682 at 06:31:47.393; cut) | — | B 11638–11735 |
| 06:31:52.1–06:38:10.6 | **Play claim B 11768 held 378.6 s** — until the new app process's first attempt kills it | — | B 11768–12932 |
| 06:31:20–06:32:19 | process 12456 paused/stopped 04:31:20–21 UTC, restarted 04:31:25, focus changes to 04:32:19 | — | L 34–57 |
| 06:38:05.6 | process 12456 killed by Android (`am_kill … Reason=remove task`), destroyed 04:38:05.593 UTC — the maintainer swiped the app away (🟡 from the reason text) | — | X 04:38:05.632; L 68 |
| 06:38:09.4 | **process 13677 started (user 0)**; MainActivity created 04:38:10.189 UTC; E 1 "Permissions (start): … GRANTED" 06:38:10.182 | — | X `am_proc_start`; L 72; E 1 |
| 06:38:10.3 | Android link CONNECTED (profile 2 bound) → **automatic re-open (LINK_BACK)**; session 1 (ch 19): the announcement, EQ read, the twelve settings, runtime info, `GetHardwareInfo` (E 4–51) | `PAIR-003`, `BATT-004`, `FW-003` | E 4–51; B 12940–13030 |
| 06:38:10.633 | the snapshot claim's **attempt 1 fails after 56 ms** (E 15) = Play's 378-s claim **killed** (B 12932 `DISC` 06:38:10.631); attempt 2 connects 06:38:11.212 (B 12979 `SABM` :11.200) → Model ID, battery ×3, `Notify 40` (E 37–40); released 1.6 s later (E 52; B 13024 `DISC` 06:38:12.826) | ADR-032 | E 15, 34–52 |
| 06:38:15.9–23.9 | Play claim B 13047 (7.9 s; killed by the app's next attempt 1, E 53 "180 ms") | — | B 13047–13168 |
| 06:38:24.5–26.7 | app claim B 13191: **hold started 06:38:24.617 — ended 06:38:25.243 "the noise-control tab was left"** (a 0.6-s visit of the tab), released 06:38:26.747 | ADR-061 | E 54–63 |
| 06:38:27.7 | `WriteSetting 4:{16:[−3.5, 0.5, 1.0, −1.0, 2.5]}` → OK (an EQ change on the Sound tab, B 13222 → 13224) | — | E 64–65; `pwrpc_decode` line 138 |
| 06:38:29.2–36.9 | Play claim B 13251 (7.7 s; killed, E 68 "185 ms") | — | B 13251–13373 |
| 06:38:37.5–39.2 | app claim B 13397 (1.68 s, `Notify 40`, released) — no hold line: 🟡 the ANC tab's Refresh, or a visit shorter than the claim | — | E 69–76 |
| 06:38:41.2 | `WriteSetting 4:{12:{1:1 2:1 3:1 4:1}}` → OK, "Setting 12 written" — **all four modes ticked for press and hold** (B 13427 → 13429) | — | E 77–79; `pwrpc_decode` line 141 |
| 06:38:42.2–06:40:38.2 | **Play claim B 13450 held 116 s** (until the app's attempt 1 of 06:40:38) | — | B 13450–14072 |
| 06:39:18–19 | the app paused and stopped (04:39:18–19 UTC); "Android link observer stopped" 06:39:19.265 | — | L 84–87; E 80–81 |
| 06:40:16.7 | the app restarted/resumed (04:40:16.718 UTC); link observer started 06:40:16.727 | t=1 | L 88–95; E 82–83 |

### The recording (06:40:15.8–06:45:31.0)

| Phone time | Event | Step · IDs | Evidence |
|---|---|---|---|
| **06:40:15.8** | **screen recording starts** (t=0): the home screen, the "Luisteren" folder open with OpenControl | BJ-1 | S t=0 |
| 06:40:16.7 | OpenControl resumed: Connection tab, "App control: ready", Left 100 % · Case "Battery unavailable" · Right 100 %, "Probably worn (checked 06:38:37)" | BJ-1 | S t=1–2; L 90 |
| 06:40:19–26 | gear → Settings (Dark mode System, Case sounds both on), **Info: "App: 1.2.0, build 5b4d5db (2026-10-10)"**, firmware ×3 `release_5.203` (06:38:10), control channel 19, serials (06:38:11); Debug: Debug mode **on**, Unidentified frames (18) — all DLCI 0x04 Group 03/07 codes of the claims | BJ-2 · `FW-003` | S t=4, 6, 10 |
| 06:40:30 | Connection; 06:40:32 **Controls**: touch on, both holds Noise control, **the four modes all ticked** (the 06:38:41 write) | BJ-3 | S t=14, 16 |
| 06:40:38.2 | **ANC tab** (t=22): the hold's claim — attempt 1 fails 275 ms (E 84; Play's 116-s claim killed, B 14072), attempt 2 connects 06:40:38.884 (B 14093 `SABM` :38.838) → `Notify` **`e8 e8 40`** B 14112 → Adaptive shown; **"hold started" 06:40:38.936** | BJ-4 · ADR-061 | E 84–92; S t=22 |
| 06:40:40.4 | Connection tab (t=25): **hold ended "the noise-control tab was left"** 06:40:40.392; released 06:40:41.895 (B 14126 `DISC` :41.897, 1.50 s later) | BJ-5 | E 93–94 |
| 06:40:43.5 | Play re-claims (B 14152, 1.64 s after the release; `Notify 40` B 14173) | — | B 14152–14263 |
| 06:40:44.4 | **pull-to-refresh on Connection** (Refresh battery): runtime info re-requested (E 95), a fresh claim — attempt 1 fails 202 ms (Play's 1.1-s claim killed, B 14263), attempt 2 06:40:45.353 (B 14285) → battery ×3, `Notify 40`; released 06:40:46.971 (B 14314); "Probably worn (checked 06:40:45)" | BJ-6 · `BATT-004` | E 95–106; S t=30 |
| 06:40:47.6 | **Disconnect** tap (t=32): "Session ended by the user's Disconnect tap (was Ready)"; "App control: not open yet — tap Connect" | BJ-7 · `PAIR-003` | E 107–108; S t=32 |
| 06:40:49.9 | Play re-claims (B 14346, 2.96 s after the app's release) | — | B 14346 |
| **06:40:50.3** | **Connect** tap (t=34, touch on the button): session 2 (ch 19), the thirteen requests + `GetHardwareInfo` (E 109–155); the snapshot claim — attempt 1 fails 117 ms (Play's 0.8-s claim killed, B 14450 06:40:50.698), attempt 2 06:40:51.301 (B 14500 `SABM` :51.246) → `Notify 40` B 14525 | BJ-8 · `PAIR-003` | E 109–155; S t=34–35 |
| 06:40:51.9 | **ANC tab** (t=36, Adaptive): the lingering snapshot claim becomes **the hold** ("hold started" 06:40:51.921, no new `SABM`) | BJ-9 · ADR-061 | E 156; S t=36 |
| 06:40:54.6 | **tap Transparency** (t≈38.5): `08 11` B 14550 → `Notify 40` B 14552 → `Set 80` B 14553 → ACK B 14555 → `Notify 80` ×2 (B 14556–14557) — on the held channel, no `SABM`; Transparency shown | BJ-10 · `ANC-004` | E 157–160; S t=39 |
| **06:40:55.9** | **the app's claim is cut** (B 14577 `DISC`, 4.68 s after its `SABM`, 5.22 s after Play lost its socket): "closed (IOException: bt socket closed, read return: −1) … hold ended: the channel was closed, not by this app" | **the finding** | E 161–162; B 14577 |
| 06:40:58.2 | Play re-claims (B 14624, 2.29 s after the cut; `08 11` → `Notify 80` B 14674) | — | B 14624–14674 |
| 06:40:59.5 | **tap Off** (t≈43.5): a new claim — attempt 1 fails 363 ms (Play's 0.6-s claim killed, B 14710), attempt 2 06:40:59.556 (B 14732) → `08 11` → `Notify 80` → `Set 20` → ACK → `Notify 20` ×2; "hold started" 06:40:59.801; Off shown | BJ-10 · `ANC-001` | E 163–174; S t=44 |
| 06:41:04.4 | **cut** (B 14810, 4.92 s after the `SABM`, 5.54 s after Play's loss); Play's recovery attempt fails too (no Play `SABM` follows within 2 s — see FINDINGS §4) | — | E 176–177 |
| 06:41:06.2 | **tap Noise cancellation** (t≈50.5): claim B 14845 **connects on attempt 1** (the channel is free) → `08 11` → `Notify 20` → `Set 08` → ACK B 14877 → `Notify 08` ×2; hold started 06:41:06.430; NC shown | BJ-10 · `ANC-002` | E 178–188; S t=51 |
| 06:41:06.6 | **cut after 0.38 s** (B 14890; 2.18 s after Play's failed recovery of 06:41:04.385 — Play's retry) — the ACK had arrived 0.14 s earlier | — | E 189–190 |
| 06:41:11.0 | Play re-claims (B 14947, 4.44 s after the cut; `Notify 08` B 14974) | — | B 14947–15022 |
| 06:41:12.2 | **tap Adaptive** (t≈55.5, touch at t=55): attempt 1 fails 208 ms (Play's 0.5-s claim killed, B 15022), attempt 2 06:41:12.205 (B 15051) → `08 11` → `Notify 08` → `Set 40` → ACK → `Notify 40` ×2; hold started 06:41:12.441; Adaptive shown | BJ-10 · `ANC-003` | E 191–202; S t=55–57 |
| 06:41:17.0 | **cut** (B 15129, 4.82 s after the `SABM`, 5.49 s after Play's loss) | — | E 204–205 |
| 06:41:19.4–06:41:53.5 | **Play claim B 15152 (34.1 s)** — `08 11` → `Notify 40` B 15176 (Play's reading), then **six unprovoked `Notify`: `80` 06:41:21.569 (B 15204), `08` :31.897 (15231), `80` :34.121 (15235), `08` :38.096 (15242), `20` :40.393 (15247), `40` :46.319 (15256)** — the maintainer's press-and-holds (cue words at 06:41:22.5, :33.0, :41.4, :47.9 — four for six changes); **the app had no claim: the screen stays Adaptive** (t=57–94) | BJ-11 · `TOUCH-007` | B 15152–15334; S t=76–94; E (nothing between 205 and 206) |
| 06:41:31.9 / 06:42:00.1 / 06:43:42.4 | Android's media volume stepped 17 → 15 → 13 → 12 (`volume_changed`, X), each within 0.5 s of a `Notify` (B 15231, 15618, 16735); the volume panel on film t=76–79, 105, 207 — 🟡 a swipe on the bud together with the hold; not in scope | — | X 04:41:31.919, 04:42:00.118, 04:43:42.370; S t=76, 105, 207 |
| 06:41:50.8 | **Disconnect** (t=95) | BJ-12 · `PAIR-003` | E 206–207; S t=95 |
| 06:41:53.1 | **Connect** (t=97): session 3; snapshot attempt 1 fails 143 ms (**Play's 34-s claim killed**, B 15334 06:41:53.486), attempt 2 06:41:54.166 (B 15386) → `Notify 40`; released 06:41:55.775 (B 15423) — the Connection tab is shown (t=99, "checked 06:41:54") | BJ-12 | E 208–257; S t=97–99 |
| 06:41:57.7 | **ANC tab** (t=102): claim B 15500 **on attempt 1** (Play has not recovered yet) → `Notify 40`; hold started 06:41:57.867 | BJ-13 | E 258–265; S t=102 |
| 06:41:58.7 | **cut after 1.03 s** (B 15539; **5.25 s after Play lost its socket** at :53.486) | — | E 266–267 |
| 06:42:01.0–06:42:30.1 | **Play claim B 15592 (29.1 s)** — `08 11` → `Notify 80` B 15618 (Play's reading — already Transparency: a hold at the moment of Play's open, cue word 06:42:01.75); then **five unprovoked: `08` :05.970, `80` :08.760, `08` :13.071, `20` :15.060, `40` :22.541** (cue words :07.8, :15.7, :23.4); **screen stays Adaptive** (t=102–129) | BJ-13 · `TOUCH-007` | B 15592–15774; S t=105–129 |
| 06:42:26.3 | **Disconnect** (t=131); 06:42:29.7 **Connect** (t=134): session 4; snapshot attempt 1 fails 160 ms (Play's 29-s claim killed, B 15774 06:42:30.128), attempt 2 06:42:30.772 (B 15822) → `Notify 40` B 15853 | BJ-14 · `PAIR-003` | E 268–317 |
| 06:42:31.0 | **ANC tab** (t=135, Adaptive): the snapshot claim kept as **the hold** (06:42:31.044) | BJ-15 | E 318 |
| **06:42:33.2** | **unprovoked `Notify 80` B 15860 on the app's claim → Transparency shown (t=138) — the "first hold" after this Connect** (cue word 06:42:34.2) | BJ-15 · `TOUCH-007`, `ANC-004` | E 319; S t=138 |
| 06:42:35.8 | **cut** (B 15909, 5.13 s after the `SABM`, 5.71 s after Play's loss) | — | E 321–322 |
| 06:42:38.2–06:42:56.3 | **Play claim B 15931 (18.1 s)** — `Notify 08` B 15957 (Play's reading, already NC — a hold at the open, cue :38.9); unprovoked **`80` :40.236, `08` :42.490, `20` :44.528, `40` :48.686** (cues :45.6, :49.6); **screen stays Transparency** (t=138–156) | BJ-15 · `TOUCH-007` | B 15931–16078; S t=138–156 |
| 06:42:53.1 | **Disconnect** (t=157); 06:42:55.9 **Connect** (t=160): session 5; attempt 1 fails 95 ms (Play killed, B 16078), attempt 2 06:42:56.905 (B 16124) → `Notify 40` | BJ-16 · `PAIR-003` | E 323–372 |
| 06:42:58.0 | **ANC tab** (t=162): hold started; the Buds push "Battery updated" ×3 at 06:42:58.7 (E 374–376) and a runtime-info packet | BJ-17 | E 373–378 |
| **06:43:00.0** | **unprovoked `Notify 80` B 16176 → Transparency shown (t=164)** (cue 06:43:00.8) | BJ-17 · `TOUCH-007` | E 379; S t=164 |
| 06:43:01.5 | **cut** (B 16196, 4.66 s / 5.24 s) | — | E 380–381 |
| 06:43:04.1–06:43:23.6 | **Play claim B 16245 (19.5 s)** — `Notify 08` (reading, a hold at the open, cue :04.4); unprovoked **`80` :05.732, `08` :07.838, `20` :09.496, `40` :13.121** (cues :10.2, :14.2); **screen stays Transparency** | BJ-17 · `TOUCH-007` | B 16245–16437; S t=164–182 |
| 06:43:19.4 | **Disconnect** (t=184); 06:43:23.1 **Connect** (t=187): session 6; attempt 1 fails 117 ms (Play killed, B 16437), attempt 2 06:43:24.187 (B 16484) → `Notify 40`; **released** 06:43:25.777 (B 16524) — the Connection tab is shown (t=188–189) | BJ-18 · `PAIR-003` | E 382–432 |
| 06:43:27.3 | **ANC tab** (t=191): claim B 16578 on attempt 1 (channel free) → `Notify 40`; hold started 06:43:27.434 | BJ-19 | E 433–439 |
| 06:43:28.9 | **cut after 1.61 s** (B 16618; **5.29 s after Play's loss** at :23.587) | — | E 440–441 |
| 06:43:31.3–06:43:50.8 | **Play claim B 16665 (19.6 s)** — `Notify 40` (reading); unprovoked **`80` 06:43:42.822** (cue :44.3); **screen stays Adaptive** (t=191–210) | BJ-19 · `TOUCH-007` | B 16665–16818 |
| 06:43:47.1 | **Disconnect** (t=211); 06:43:50.4 **Connect**: session 7; attempt 1 fails 151 ms (Play killed, B 16818), attempt 2 06:43:51.418 (B 16867) → `Notify 80` B 16892 | BJ-20 · `PAIR-003` | E 442–492 |
| 06:43:51.7 | **ANC tab** (t=216, Transparency): the snapshot claim kept as the hold | BJ-21 | E 493; S t=216 |
| **06:43:53.7 / 06:43:55.9** | **two unprovoked `Notify` on the app's claim: `08` B 16907 → NC shown (t=218); `80` B 16937 → Transparency shown (t=220)** (cue 06:43:54.7 for the first) | BJ-21 · `TOUCH-007` | E 494–496; S t=218, 220 |
| 06:43:56.2 | **cut** (B 16952, 4.85 s / 5.40 s) | — | E 497–498 |
| 06:43:58.7–06:44:22.3 | **Play claim B 16977 (23.6 s)** — `Notify 08` ×2 (reading); unprovoked **`20` 06:44:00.992, `40` :05.207, `80` :11.027, `08` :15.903, `80` :18.088** (cues :02.0, :06.3, :12.0, :16.9); **screen stays Transparency** (t=220–242) | BJ-21 · `TOUCH-007` | B 16977–17142; S t=242 |
| 06:44:19.6 | **Disconnect** (t=244); 06:44:21.9 **Connect** (t=246): session 8; attempt 1 fails 101 ms (Play killed, B 17142), attempt 2 06:44:22.899 (B 17188) → `Notify 80` B 17211; **unprovoked `Notify 08` B 17231 at 06:44:23.761 inside the snapshot claim** (a hold during the Connect) | BJ-22 · `PAIR-003`, `TOUCH-007` | E 503–552 |
| 06:44:24.5 | **ANC tab** (t=249): **Noise cancellation shown** (B 17231's mode); hold started 06:44:24.476 | BJ-23 | E 553; S t=249 |
| **06:44:27.1** | **unprovoked `Notify 20` B 17235 → Off shown (t=251)** (cue :27.7) | BJ-23 · `TOUCH-007`, `ANC-001` | E 554; S t=251 |
| 06:44:27.5 | **cut** (B 17254, 4.66 s / 5.23 s) | — | E 555–556 |
| 06:44:30.1–06:44:53.0 | **Play claim B 17303 (22.9 s)** — `Notify 20` (reading); unprovoked **`40` :31.402, `80` :36.802, `08` :41.389, `80` :43.679, `08` :46.433, `20` :48.000** (cues :32.5, :37.6, :42.4, :48.9); **screen stays Off** (t=251–274) | BJ-23 · `TOUCH-007` | B 17303–17465; S t=274 |
| 06:44:51.1 | **Disconnect** (t=275); 06:44:52.6 **Connect**: session 9; attempt 1 fails 176 ms (Play killed, B 17465), attempt 2 06:44:53.651 (B 17513) → `Notify 20` | BJ-24 · `PAIR-003` | E 557–606 |
| 06:44:53.9 | **ANC tab** (t=278, Off): hold started | BJ-25 | E 607; S t=278 |
| **06:44:57.0** | **unprovoked `Notify 40` B 17548 → Adaptive shown (t=281)** (cue :57.8) | BJ-25 · `TOUCH-007`, `ANC-003` | E 608; S t=281 |
| 06:44:58.7 | **cut** (B 17594, 5.04 s / 5.64 s) | — | E 610–611 |
| 06:45:01.2–06:45:13.4 | **Play claim B 17618 (12.2 s)** — `Notify 40` (reading); unprovoked **`80` :03.002, `08` :06.488, `80` :08.851** (cues :03.7, :07.3); **screen stays Adaptive** | BJ-25 · `TOUCH-007` | B 17618–17759 |
| 06:45:11.6 | **Disconnect** (t=296); 06:45:12.9 **Connect** (t=297): session 10; attempt 1 fails 153 ms (Play killed, B 17759), attempt 2 06:45:14.052 (B 17807) → `Notify 80` B 17835; **unprovoked `08` B 17843 at 06:45:14.501** (in the snapshot claim) | BJ-26 · `PAIR-003`, `TOUCH-007` | E 612–663; S t=297–298 |
| 06:45:14.6 | **ANC tab** (t=299): NC shown; hold started | BJ-27 | E 664; S t=299 |
| **06:45:17.6** | **unprovoked `Notify 20` B 17848 → Off shown (t=302)** (cue :18.4) | BJ-27 · `TOUCH-007` | E 665; S t=302 |
| 06:45:18.9 | **cut** (B 17891, 4.88 s / 5.48 s) | — | E 667–668 |
| 06:45:21.3 | **Play claim B 17915** (to the log's end): `Notify 40` (reading) and unprovoked **`80` 06:45:26.634** (B 17977; cue :27.6) — **Play services logs it**: `HearableControlManager: onReceiveEventStreamResponse` 04:45:26.639 UTC and `ActiveNoiseCancellationModule: update dataStore … AncIndex` 04:45:26.668 (X, 5–34 ms after the frame) | BJ-27 · `TOUCH-007` | B 17915–17977; X 04:45:21.795–26.668 |
| 06:45:29.8 | Quick Settings pulled down (t=314): the recorder's notification "Scherm en audio opnemen • 05:15", the app's notification "Connected — ANC: OFF" | BJ-28 | S t=314 |
| **06:45:31.0** | **screen recording ends**; the app stopped 04:45:31.4–32.0 UTC; "Android link observer stopped" 06:45:32.015 | BJ-28 | L 321–343; E 669–670 |

### After the recording (06:45:31–07:03)

| Phone time | Event | Evidence |
|---|---|---|
| 06:45:35.1–53.6 | AVRCP "Playing" (B 18024 → Stopped B 18072) — media played for 18 s **after** the film; `SendMediaUpdate play_state=true` X 04:45:35.102 | B 18024, 18072 |
| 06:45:41 | `bugreportz -v` / `-p` (X 04:45:41.265/.449, `adbd`): the maintainer's pull; SIGQUITs 04:45:57–04:46:15 (37 processes, the app's at 04:46:12.407 = L's last line) — the bugreport's trace dump, not an app fault (`CAP-071-FINDINGS.md` §0) | X 68174–68177; L 350 |
| 06:46:29 / 06:46:44 | L saved (file mtime; via App info → View logs, by the maintainer's statement in the prompt); X saved | file times |
| 06:52:09 | **the HCI log ends** (B's last packet) — the 07:03 claim below is not on the wire | `capinfos` |
| 06:53:29, 06:57:04–24 | runtime-info packets on the session channel (E 673–680) — the session stayed open with the app in the background | E 673–680 |
| 07:03:01.5 | the app resumed (link observer started); the ANC tab: attempt 1 fails 231 ms, attempt 2 07:03:02.409 → `Notify 80`; hold started 07:03:02.473, **ended 07:03:03.206 "the noise-control tab was left"**, released 07:03:04.708; **the export saved at 07:03:05** (its name) | E 681–693 |

## The maintainer's steps, as run (derived from the film — there was no skeleton)

BJ-1 open the app (the recorder started first) · BJ-2 Settings → Info → Debug · BJ-3 Connection, Controls · BJ-4 ANC tab · BJ-5 Connection · BJ-6 pull-to-refresh on
Connection · BJ-7 Disconnect · BJ-8 Connect · BJ-9 ANC tab · BJ-10 tap Transparency, Off, Noise cancellation, Adaptive (06:40:54–06:41:12) · BJ-11 press-and-holds on
the buds with the app on the ANC tab (06:41:21–06:41:46; none shown) · BJ-12 Disconnect, Connect · BJ-13 ANC tab; holds (06:42:01–06:42:23; none shown) · BJ-14
Disconnect, Connect · BJ-15 ANC tab; holds (06:42:33 shown — then 06:42:38–06:42:49 not) · BJ-16 Disconnect, Connect · BJ-17 ANC tab; holds (06:43:00 shown — then
06:43:04–06:43:13 not) · BJ-18 Disconnect, Connect · BJ-19 ANC tab; hold (06:43:43 not shown) · BJ-20 Disconnect, Connect · BJ-21 ANC tab; holds (06:43:54 and :56 shown
— then 06:44:01–06:44:18 not) · BJ-22 Disconnect, Connect (a hold during the Connect, shown on entering the tab) · BJ-23 ANC tab; holds (06:44:27 shown — then
06:44:31–06:44:48 not) · BJ-24 Disconnect, Connect · BJ-25 ANC tab; holds (06:44:57 shown — then 06:45:03–06:45:09 not) · BJ-26 Disconnect, Connect (a hold during the
Connect) · BJ-27 ANC tab; holds (06:45:18 shown — then 06:45:27 not) · BJ-28 Quick Settings, the recorder stopped.

## Test-IDs (`AGENTS.md` §13 step 7; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`)

- `PAIR-003` — nine Disconnect/Connect pairs (06:40:47/50, 06:41:51/53, 06:42:26/30, 06:42:53/56, 06:43:19/23, 06:43:47/50, 06:44:20/22, 06:44:51/53, 06:45:12/13) and the
  automatic re-open at 06:38:10 (LINK_BACK); 10 sessions, each with the thirteen requests and one `GetHardwareInfo` answered (E "Settings read" ×10, "Hardware info
  read" ×10, 130 `ReadSetting` answers).
- `ANC-001`…`ANC-004` — four taps in the app (Transparency 06:40:54, Off :59, Noise cancellation 06:41:06, Adaptive :12), 4 `Set` → 4 ACK, 0 NAK; every mode also
  shown from a `Notify` (Off 06:44:27, 06:45:18; NC 06:43:54, 06:44:24, 06:45:14; Adaptive 06:44:57; Transparency 06:42:33, 06:43:00, 06:43:56).
- `TOUCH-007` — **47 mode changes the app did not request** in the run: 9 arrived on the app's claims and are on screen; 35 are unprovoked `Notify` frames on Play
  services' claims; 3 are Play's own `Get` answers that already carry a changed mode. The press-and-holds themselves are the maintainer's statement (no camera; the
  cue words are 0.1–1.5 s after a `Notify` in 33 of 33 cases).
- `BATT-004` — "Battery updated" ×3 on every claim's open (E 39, 58–59, 73–74, …; 100 %/100 %/`ff`).
- `FW-003` — the Info tab on film (t=6): firmware ×3, the three serials with their labels (06:38:11).
- **Expected but not observed:** `AUDIO-002` (no Volume EQ toggle); music playback during the holds (the maintainer's "during playback"): none on the wire or in the
  audio track during the film — AVRCP Playing only 06:45:35–06:45:53.

## Analysis checklist

- [x] Every filter scoped to the Buds' handle `0x000b` (B 10528), shown matching a known frame.
- [x] Build identity: the Info tab on film; the installed APK's hash **not** verified (no pull) — Checkpoint 0.
- [x] The claim inventory of DLCI 0x04 with both clients (FINDINGS §1), every `Notify` attributed (FINDINGS §4), the Play-services marker counted with its positive control.
- [x] Every second of the film viewed or proved unchanged; the audio's voiced segments tabulated.
- [x] The export and the app logcat read line by line; the system log swept in the run's and the pre-run's windows with stated, positive-controlled filters.
- [x] Test-ID traceability (above); "expected but not observed" stated.
- [x] Folder rename, file modes, the checksum file, the recording as is, the system-log extract — Checkpoint 0 of `ai-sessions/0086` (done); the APK pull not made (⚪, §0 of the FINDINGS).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-074-2026-10-12_06-40-16_06-45-31-Group_BJ/CAP-074-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-074-2026-10-12_06-40-16_06-45-31-Group_BJ/CAP-074-EVENT-NOTES
