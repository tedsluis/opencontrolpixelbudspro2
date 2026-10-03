# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, a secondary user without Google Play) — Group BC, the 1.0.0 release APK (`CAP-067`)

**Status:** 🟢 **Video pass + full log analysis complete** (`ai-sessions/0067`). See `CAP-067-FINDINGS.md` for the wire-level analysis, the commands and
the raw bytes. The procedure the maintainer followed (the skeleton written by `ai-sessions/0064`, adapted by `0066` and `0065`) is kept as **Appendix A**
(text unchanged, heading levels lowered by one); the timeline below records what was **actually** done — the maintainer filmed the ears, did not speak, changed
the order of some steps, did some differently (A5 before (E), Android's dark switch via the Settings app, BC-6 with the head out of view) and did not do BC-12
(the auto-off setting was not found in the test user), B4 (one effective tap) or any ANC tap. **No force-stop was made:** the process ended when *Nearby
devices* was revoked (A5, film 08:15:34).

**Purpose:** the release test of the release-signed **1.0.0** APK (`scripts/release.sh`, commit `8d8af4b`) in a GrapheneOS **secondary user without Google
Play** — the evidence for `PROJECT.md`'s Definition of done 1–3 — plus F-1 (tab across a configuration change), the balance slider to Right 4, lead L-1, the
Info tab's licence and links, dark mode Off/System, an export with Debug mode off, F-3 (Bluetooth off), K5 and the destructive steps A5, (E), B4, Z1.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-067`                     |
|      Group(s)    |                        BC                          |
|       Date       |                    2026-10-03                      |
| Firmware version | 🟢 `release_5.203` — all 7 MAESTRO announcements of the HCI logs (e.g. `.log.last` frame 253) and the Info tab on film ("Case / Left bud / Right bud: release_5.203", 07:59:04–10, 08:09:30, 08:10:50) |
|   Test device    | Pixel 9a, GrapheneOS, Android 17 `CP3A.260905.009` (logcat header `google/tegu/tegu:17/CP3A.260905.009/2026092501:user/release-keys`), **`userType: full.secondary`** (logcat header) — a secondary user. **No Google Play in that user:** the app drawer on film (07:58:00) lists App Store, Auditor, Bellen, Berichten, Bestanden, Camera, Contacten, Galerij, Info, Instellingen, Klok, OpenControl, PDF Viewer, Rekenmachine, Vanadium — no Play Store; the HCI logs carry **no** Play-services Message Stream claim (FINDINGS §2: 0 of 6 claims; positive control `CAP-066` 26). P0's `pm list packages` output was not saved (no file) |
|   App build      | **OpenControl for Pixel Buds Pro 2 1.0.0, build `8d8af4b` (2026-10-02), not "-dirty"** — the Info tab on film (07:58:25–31, full-resolution crop t = 52 s: "App: 1.0.0, build 8d8af4b (2026-10-02)") and the logcat header `package: io.github.tedsluis.opencontrolpixelbuds:10000, targetSdk 34` (versionCode 10000 = 1.0.0). `8d8af4b` = "docs(capture): adapt the CAP-067 skeleton …" (2026-10-02 08:33:08 +0200). `dist/1.0.0/opencontrol-pixelbudspro2-1.0.0.apk` (read only): SHA-256 `107d49b6…f3467a2`, signing certificate SHA-256 `a7530f5c…79d8dcb`, `versionCode='10000' versionName='1.0.0'`, label "OpenControl for Pixel Buds Pro 2", no `INTERNET` permission (`apksigner verify --print-certs`, `aapt2 dump badging`) — equal to the values in the prompt. That the phone ran **this** file is consistent (version code, hash, label, notification title) but not proven (the APK was not pulled from the phone). Launcher label "OpenControl" on film (07:58:00–11); notification title "OpenControl for Pixel Buds Pro 2" (08:10:21, 08:12:15, 08:13:33, 08:15:21) |
| Other devices    | Bluetooth list on film: "Charge 6" and "Niro" (saved, not connected). Classic handle `0x000b` = the Buds, the only handle in both files. LE: only advertising reports during the CDM picker's scan (08:16:51.5–08:16:58.6, 18 addresses incl. the Fitbit `c8:cc:a8:e7:48:93`), no LE link |
| Video file       | `CAP-067-recording.mp4`: 1,254.32 s, 967,913,555 bytes, H.264 1280×720, rotation −90 (portrait), 37,431 frames, r_frame_rate 179/6; `creation_time` 2026-10-03T06:18:29Z (= 08:18:29 local = the **end**). Burned-in overlay `Oct 3, 2026 HH:MM:SS`: **first frame 07:57:35, last frame 08:18:29** |
| Audio            | **None.** The audio track (`hdlr soun`) has an empty sample table (`stbl` of 8 bytes, no `stsd`/`stsz`; the only `stsd` in `moov` belongs to the video track); `ffmpeg -i … -map 0:0 -f null -` → "Decoding requested, but no decoder found for: none". Nothing about sound is evidenced by the film |
| HCI logs         | **Two files, because Bluetooth was switched off and on during the film (BC-10/BC-11):** `CAP-067-btsnoop_hci.log.last` — 1,563 packets, 07:57:42.429039–08:12:24.088836, the log **before** the 08:12:23 Bluetooth-off (frame 1 = HCI Reset of the P2 Bluetooth-on; ends with the phone's `Disconnect` A1537 08:12:23.858 → `Disconnection Complete` reason `0x16` A1561 08:12:23.979); `CAP-067-btsnoop_hci.log` — 1,810 packets, 08:12:38.177513–08:20:05.131746, **after** it (frame 1 = HCI Reset). Both "Bluetooth H4 with linux header", no size limit, **0** `cap_len≠len`; 0 / 1 out-of-order packets (`.log` frame 1431). The 14.1-s gap 08:12:24.1–08:12:38.2 is the Bluetooth-off period (film: off 08:12:22–24, on 08:12:36) — the Buds' ACL was closed before it (A1561), so no Buds traffic falls into it. RFCOMM payloads complete: every pw_hdlc frame CRC-32 valid (114 + 67); A2DP media is not logged (0 media frames — the same in `CAP-066`), which explains part of the smaller size; the rest is fewer events (6 Message Stream claims vs 49, no Play services). Film covers 07:57:33.9–08:18:27.9 phone time; the HCI starts 8.5 s after the film and ends 1 min 37 s after it |
| App debug exports | **Two** (the app's names): `CAP-067-opencontrol-debug-20261003-081146.txt` ("E1") — 341 lines (340 newlines), 07:12:34.868–08:11:28.901, process PID 12287 (started ≈ 07:03 local, logcat `service_manager_slow` 05:03:17.975 UTC); saved on film 08:11:46–54 ("Debug log saved (341 lines)." 08:11:54) **with Debug mode OFF** (switched off on film 08:11:44, back on 08:11:58) — this is L3/BC-9. `CAP-067-opencontrol-debug-20261003-082306.txt` ("E2") — 125 lines (logcat "Debug log exported (125 lines)" 06:23:08.412 UTC), 08:15:42.399–08:23:01.435, the new process PID 20326; saved **after the film** (08:23:06–08), Debug mode **on** at 08:23:10 (hex lines in the logcat right after). Neither export can show Debug mode by content: hex lines are gated at append (`BleLogger.kt:73–74`), earlier lines stay in the ring buffer, and in neither file did an inbound frame arrive between the switch-off and the save (E1: last inbound 08:11:02.851, the always-on `pw_rpc` summary line would show any later frame — `BudsRepositoryImpl.kt:468`). **Not covered by any export:** 08:11:29–08:15:42 — the Bluetooth off/on (BC-10/BC-11), BC-11s, BC-12, BC-R and A5 of process 12287 (it was never exported again; the maintainer confirmed in chat 2026-10-03 that no other export exists) — covered by the film and the HCI log only |
| App logcat       | `CAP-067-OpenControl-for-Pixel-Buds-Pro-2-log-0d4af245fcc4.txt` — 671 lines, one file, saved after the film. Buffers: **events** from 05:03:17.975 UTC (activity lifecycle `wm_*`), **system** from 05:58:11.483, **main** (the app's own lines) **only from 06:20:31.104** to 06:23:20.880. PIDs 12287 (396 lines, last 06:15:23.679) and 20326 (165 lines, first 06:15:42.357). Only one logcat exists (the maintainer's "app log saved an extra time" is not present). No crash, ANR, tombstone or exception line; 2 × `W System: A resource failed to call close.` (06:23:02.643/.644, PID 20326, thread 20336 — CloseGuard's default report; StrictMode is off in a release build, `OpenControlApplication.kt:63`) |
| System log       | **None** — not available in the user without Play services (the maintainer, chat 2026-10-03). Checks that needed it (P7 `ACTION_VIEW`, BC-10/BC-12 `BluetoothAutoOff`/`STATE_CHANGED`, force-stop/`am_kill` lines, install lines) are covered by the film and the HCI log or stay unverified (FINDINGS §0) |
| Events file / P0 output | none / none |
| Clock offsets    | **Film ↔ phone, measured at both ends** (5-fps crops of the status bar against the overlay, session scratchpad): 07:57→07:58 on the 2nd 0.2-s frame of overlay 07:58:01, 08:17→08:18 on the 1st frame of overlay 08:18:01 ⇒ **phone = overlay − 1.1 s (±0.2 s), no drift**. Cross-check: export save dialog closed on film at overlay 08:11:55 ≈ logcat `wm_on_activity_result_called` 06:11:53.891 UTC. **Logcat (UTC) = phone − 2 h 00 min 00.000 s** (E2 08:20:31.104 `DLCI 0x02: 7e 00 a5 …` = logcat 06:20:31.104, the same line). HCI and the exports are phone local time |
| Buds (partial, `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` (ADR-010; also on film in Android's device details, 08:16:11). Classic handle `0x000b` for all three ACLs. **Pre-filter:** `bluetooth.addr` is empty with this encapsulation; every command filters `bthci_acl.chandle==0x000b`. **DLCIs:** where the phone opened the RFCOMM multiplexer MAESTRO = 0x02, Message Stream = 0x04; after the re-pairing the **Buds** opened it (`.log` B1466) and MAESTRO = 0x03, Message Stream = 0x05 |
| Wear mapping     | The maintainer's rule (chat 2026-10-03): **head on the right of the frame = Left bud, head on the left = Right bud**; the bud from the **right-hand slot** of the case is the Right bud (as `CAP-066`). Cross-check against the runtime-info per-bud charging field (6.2.2 Left / 6.3.2 Right) at the two undock events: the right-hand-slot bud left first (film 08:05:12) and 6.3.2 turned `1` first (`.last` A889 08:05:12.666), the left-hand-slot bud second (film 08:05:24–30) and 6.2.2 turned `1` (A906 08:05:32.340); both re-docked before 08:16:37 and both charging again at B1710 08:17:04.763 — **0 contradictions**. BC-6a: head entered from frame-**left** (= Right bud) at 08:06:04. BC-6: the head was **not** in view — which bud was taken out is not identifiable from the film |

## Capture-integrity pre-flight

```
$ capinfos CAP-067-btsnoop_hci.log.last          $ capinfos CAP-067-btsnoop_hci.log
File encapsulation:  Bluetooth H4 with linux header      (same)
Packet size limit:   file hdr: (not set)                 (same)
Number of packets:   1,563                               1,810
Earliest packet time: 2026-10-03 07:57:42.429039         2026-10-03 08:12:38.177513
Latest packet time:   2026-10-03 08:12:24.088836         2026-10-03 08:20:05.131746
Strict time order:   True                                False

$ tshark -r <log> -T fields -e frame.number -e frame.cap_len -e frame.len -e frame.time_epoch \
  | awk '$2!=$3{c++} {if(NR>1 && $4<p) o++; p=$4} END{print "packets:",NR," mismatches:",c+0," out-of-order:",o+0}'
.log.last → packets: 1563  mismatches: 0  out-of-order: 0          .log → packets: 1810  mismatches: 0  out-of-order: 1 (frame 1431)

$ ffprobe … CAP-067-recording.mp4 → stream 0 audio codec_name=unknown tag [0][0][0][0] (empty sample table); stream 1 h264 1280x720, 179/6,
                                    37,431 frames, 1254.32 s, rotation −90; creation_time 2026-10-03T06:18:29Z; size 967,913,555

$ sha256sum *   (identical before and after the folder rename, `sha256sum -c`, 7/7 OK, 2026-10-03)
db1b094c…ea133b  CAP-067-btsnoop_hci.log                   5924fe0b…26d320  CAP-067-btsnoop_hci.log.last
336103ba…c0f029  …debug-20261003-081146.txt                2da681b4…64d8   …debug-20261003-082306.txt
a97a965e…ac01e2  …log-0d4af245fcc4.txt                     8cd6231b…9448   CAP-067-recording.mp4
```

File modes: the two HCI logs were `-rw-r--r--`, the other four `-rwxr-----`; set to `644` on the maintainer's approval (chat 2026-10-03, "Migration":
*"Approve as proposed (Recommended)"*). `git check-attr filter` → `lfs` for all six capture files. The phone's file names are kept.

## Video review method

- **Every second:** the whole film was extracted at 1 fps (1,254 frames, `ffmpeg -vf fps=1,scale=360:640`) and viewed at 2-s spacing on 79 contact sheets
  (4 × 2, the burned-in overlay on each frame). Transitions a claim depends on were narrowed at 1.5–5 fps: the two status-bar minute flips (5 fps), the back
  press after BC-3r (08:03:23–29, 4 fps), the bud order 08:05:09–37, BC-6a 08:06:03–13, BC-6 08:07:25–37 (3 fps), the bud's return 08:08:13–23, the return after
  Android's dark switch 08:11:27–33 (4 fps), A5 08:15:27–37 (3 fps), (E) 08:16:09–15, B4 08:16:48–54 (4 fps), the Info build line (full resolution, t = 52 s).
  App actions with a log line or a wire frame are timed from that line or frame, not from the film.
- **Audio:** none recorded (see Log Metadata).
- **Privacy (ADR-037):** no camera address overlay; no speech; no face but the maintainer's own hair/glasses/ear (by design). Legible personal data: the Wi-Fi
  SSID "Bachstraat20" (address-like) in Quick Settings at 07:57:35–53, 08:10:21–31, 08:12:15–19, 08:13:33–35, 08:15:21–23, 08:16:07; the Buds' device name
  "Pixel Buds Pro 2 van Ted" in the CDM dialog (08:16:59–08:17:01); the Buds' MAC in Android's device details (08:16:11, ADR-010); notifications only "USB-
  foutopsporing verbonden", charging and the app's own; the save-as dialog showed an empty Downloads folder. **The maintainer chose to keep the film unblurred**
  (chat 2026-10-03, "Privacy": *"Keep unblurred (Recommended)"*).
- **Wear:** each wear row names the side of the frame the head was on, or the slot of the case the bud came from.

## Event Timeline

Times are **phone local time**: film-only events are overlay − 1.1 s, to ±1 s (2-s sheets) unless narrowed; logged events use the log time. "A N" / "B N" =
frame N of `.log.last` / `.log` (handle `0x000b`); "E1 N" / "E2 N" = line N of the export; "LC" = the logcat (UTC + 2 h). "Step" = the skeleton step (Appendix A);
"var." = done differently, "rep." = repeated, "extra" = not in the plan. Registry Test-IDs in brackets.

| Time (phone) | Action / event | Actor | Step / test | Evidence |
|---|---|---|---|---|
| 07:03–07:20 | Before the film: process 12287 starts (≈ 07:03); 07:12:34 activity created; **six relaunches 07:12:41–07:13:31** (`handleRelaunchActivity`, each with a "Permissions (start)" line) — a rotation test before the film; the app stopped 07:18:31, 07:19:54, 07:20:23 | User / App | (before the film) | LC events `wm_*` 05:12:34–05:20:23; E1 1–43 |
| 07:57:34 | **Film starts** in Quick Settings, Bluetooth dialog "Bluetooth staat uit"; closed case on top of the phone | — | P2 | film |
| 07:57:40 | **Bluetooth on**; device list: Charge 6, Niro, Pixel Buds Pro 2 (saved); `.last` starts with the HCI Reset (A1 07:57:42.429) | User / OS | P2 ✓ | film; HCI |
| 07:57:56–07:58:10 | Home; app drawer — launcher label **"OpenControl"**, no Play Store; tap OpenControl | User | P8 ✓ (label), P0 (film only) | film |
| 07:58:11.3 | App resumes (activity restart): "Paired — not connected to this phone", Connect | App | — | LC 05:58:11.344; E1 44–45 |
| 07:58:14–20 | Gear → Settings (Dark mode: System); Debug tab: Debug mode **off** → switched **on** | User | (prep, extra) | film |
| 07:58:24–31 | **Info tab:** "App: 1.0.0, build 8d8af4b (2026-10-02)" (no "-dirty"), "Works with Google Pixel Buds Pro 2. Not affiliated with or endorsed by Google. Pixel Buds is a trademark of Google LLC.", Licence "GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)", Read the licence, README on GitHub, Report an issue on GitHub, "Links open in your browser; this app itself has no internet access.", The Buds: "Not connected yet …" | App | P7 (var.: before the first "ready") | film (full-res crop t = 52 s) |
| 07:58:32 | Back → Connection | User | — | film |
| 07:58:38–40 | **Connect tap** with the **lid closed** → "connecting…"; the phone pages the Buds (A157 07:58:40.124) | User / OS | extra | E1 46; HCI |
| 07:58:44 | Lid opened (hand) | User | — | film |
| 07:58:45.3 | Page timeout (A160 status `0x04`); E1 "RFCOMM channel 0x02 connect failed after 5168 ms … read failed, socket might closed or timeout"; card: **"Couldn't open the Maestro channel (equalizer). Another app on this phone — for example Google Play services' Fast Pair — may already be using it. Wait a few seconds, then try again."** + Retry | App | — | E1 47–48; A160; film 07:58:46–48 |
| 07:58:49.6 | **Retry tap** → phone page A161 → ACL A163 07:58:50.385 → MAESTRO `SABM` A241 → ready 07:58:50.58; announcement **ch 21** (A253); EQ, settings read; claim A275 `08 11` → `Notify 01 e8 00 20` (A299); battery `e4 e4 ff` (L/R 100 % charging) | User / App | [`BATT-004`] | E1 49–84; A161–A324 |
| 07:58:50 | Card "Android doesn't show the Buds as connected (yet)" + ready; L 100 ⚡, Case "Battery unavailable", R 100 ⚡ → 07:58:52 "Connected to this phone (Android)", **Case 95 %** (runtime info A324 `6:{1:{1:95 …}}` 07:58:51.080) | App | — | film 07:58:50–52; E1 85 |
| 07:58:56–07:59:10 | Gear → Debug: "Unidentified frames (5)" (DLCI 0x04 `03/0a`, `03/02`, `03/09`, `07/10`, `07/34`); **Info**: "Firmware (from the Buds' announcement, 07:58:50)", Case / Left bud / Right bud `release_5.203`, **"Control channel: 21"** | User / App | P7 ✓ (firmware lines) | film; A253 |
| 07:59:22–07:59:32 | Info → **Read the licence** → dialog "Licence", "GNU AFFERO GENERAL PUBLIC LICENSE / Version 3, 19 November 2007" → **Close** | User | P7 ✓ | film 07:59:26–32 |
| 07:59:36–07:59:50 | **README on GitHub** → Vanadium opens github.com/tedsluis/opencontrolpixelbudspro2 README ("OpenControl for Pixel Buds Pro 2"); Vanadium's notification prompts; back to the app via recents | User / OS | P7 ✓ (link 1) | film; E1 88–91 (observer stopped/started 07:59:38/07:59:51); LC `wm_on_stop` 05:59:38.187 |
| 07:59:56–08:00:06 | **Report an issue on GitHub** → Vanadium: the repository's Issues (0 open, 0 closed); back | User / OS | P7 ✓ (link 2) | film; E1 92–95; LC 05:59:57.528 / 06:00:06.327 |
| 07:58:52–08:03:07 | No app RFCOMM frame while the menu or the browser was open (only the runtime stream A534, A771, A800 pushed by the Buds) | — | P7 ✓ | decoder (FINDINGS §5) |
| 08:00:12 | Top-bar back → Connection; idle (buds docked, lid open) | User | — | film |
| 08:01:10–08:01:24 | **Rotate on Connection** → landscape, Connection kept; back to portrait, Connection kept | User / App | BC-1 ✓ | film; LC relaunch 06:01:11.304, 06:01:23.876 |
| 08:01:31 | ANC tab: note "The Buds don't allow changing noise control right now (usually because no bud is in an ear). Tapping a mode checks again first.", **Off** selected | User / App | — | film |
| 08:01:35–08:01:45 | Rotate on **ANC** → ANC kept, both ways | User / App | BC-2 ✓ | LC 06:01:36.241, 06:01:44.755 |
| 08:01:47–08:02:04 | Rotate on **Sound** → Sound kept (scrolled to Balance in landscape), both ways | User / App | BC-2 ✓ | LC 06:01:54.172, 06:02:03.958 |
| 08:02:07–08:02:24 | Rotate on **Controls** → Controls kept, both ways (touch controls on; press and hold Noise control ×2; modes NC ✓ Off ☐ Adaptive ✓ Transparency ✓ — equal to the read `4:{12:{1:1 2:0 3:1 4:1}}`) | User / App | BC-2 ✓ | LC 06:02:12.598, 06:02:23.156; A312 |
| 08:02:26–08:02:38 | Rotate on **Find** → Find kept, both ways (no ring tapped) | User / App | BC-2 ✓ | LC 06:02:31.317, 06:02:37.436 |
| 08:02:50–08:03:24 | From Find: gear → Settings; rotate → Settings tab kept (landscape); Debug and Info tabs in landscape; rotate back → Info kept; Settings tab | User / App | BC-3r ✓ | LC 06:02:59.202, 06:03:07.478; film |
| 08:03:24.4 | Top-bar back → **Find** (the tab the menu was opened from) | User / App | BC-3r ✓ | film (4 fps, 08:03:25.5 overlay) |
| 08:03:26 | Tap **Sound**; scroll to "Balance and audio" (Centre) | User | BC-3 | film |
| 08:03:52.7 – 08:04:43.1 | **17 balance drags**, one `WriteSetting 4:{17:n}` each, all `RESPONSE` OK: Right 8, Right 7, Centre, Centre, Right 5, Centre, Centre, Right 12, Centre, Centre, Right 13, Right 13, Left 7, Left 27, Left 52, Centre, **Right 4** (A876 08:04:43.079, `17:7`); label after each OK on film ("Right 8" 08:03:52, "Right 7" 08:04:04, "Right 5" 08:04:12, "Right 12"/"13" 08:04:26–32, "Left 27" 08:04:34, "Left 52" 08:04:36–38, "Right 4" 08:04:44–58) | User / App | BC-3 ✓ (17th drag) [`AUDIO-003`] | A818–A878; E1 162–212 |
| 08:05:00.0 | Drag to the centre → `17:0` (A882 08:05:01.136) → "Centre" | User / App | BC-5 ✓ [`AUDIO-003`] | A882/A885; E1 213–215; film |
| 08:05:12 | The **right-hand-slot** bud out of the case; head enters from frame-**left** → **Right** bud into the Right ear | User | (BC-6 prep) [`INEAR-002`] | film; A889 08:05:12.666 6.3.2 = 1 |
| 08:05:24–08:05:31 | The **left-hand-slot** bud out (head out of view) → Left bud into the Left ear; case empty | User | (BC-6 prep) | film; A906 08:05:32.340 (no 6.1, 6.2.2 = 1) |
| 08:05:36–08:06:02 | Both worn (off frame), Sound tab; session on **21** | — | — | film |
| 08:06:04–08:06:10 | **Head on frame-left → the Right bud out of the ear** (both had been worn on 21), laid right of the case | User | **BC-6a ✓** [`INEAR-003`] | film (2 fps) |
| 08:06:06.805 | **Buds `DISC` MAESTRO** (A945) with the ACL up → E1 "Session lost … Buds closed the channel (provisional …)" → **AFTER_LOSS** re-open 08:06:08.340 → ready 08:06:08.906 → announcement **ch 19** (A982); claim A1001: `Notify 01 e8 e8 80` (Transparent), battery `64 64 ff` | Buds / App | BC-6a ✓ (21 → 19) | A945–A1049; E1 228–269; film (Sound dimmed 08:06:08) |
| 08:06:55–08:06:58 | The Right bud picked up from the table and put back into the Right ear (head on frame-left) | User | BC-6a ✓ | film |
| 08:07:29–08:07:34 | **A bud taken out of an ear** (head **not** in view; the hand enters from the bottom-left) and laid left of the case | User | **BC-6 (bud not identifiable)** [`INEAR-004`] | film (3 fps) |
| 08:07:31.481 | **Buds `DISC` MAESTRO** (A1085), ACL up → AFTER_LOSS re-open 08:07:32.994 → announcement **ch 21** (A1123); claim A1142: `Notify … e8 e8 80` | Buds / App | BC-6 (19 → 21) | A1085–A1199; E1 276–317 |
| 08:08:14–08:08:20 | That bud picked up from the table (out of frame towards the user) | User | (extra) | film |
| 08:08:42–08:08:58 | Tab tour: Connection (L 100, Case 93, R 100), **ANC: Transparency selected, no note**, Sound, Controls, Find, Connection | User | extra | film |
| 08:09:20–08:09:32 | Gear → **Dark mode On** → dark at once; Debug (15 unidentified frames), Info ("announcement 08:07:33", channel 21) in dark | User / App | (K4d On, extra) | film |
| 08:09:36–08:09:52 | **Dark mode Off** → light at once; Debug, Info; **System** → light (Android light); back → Connection | User / App | BC-8 ✓ (Off, System) | film |
| 08:10:20–08:10:30 | Quick Settings: notification **"OpenControl for Pixel Buds Pro 2 · 2 min / Connected — ANC: TRANSPARENT"**; tile **"ANC / Transparent"**; no dark-theme tile | User | P8 ✓, P6 ✓ | film |
| 08:10:30–08:10:40 | Settings app → Scherm → **"Donker thema" on** (Android dark) | User | BC-8 var. (Settings app, not Quick Settings) | film; E1 328–329 (observer stopped 08:10:31.998) |
| 08:10:42–44 | Recents → OpenControl: **Connection** (the tab it was left on), **dark** (System follows Android) | User / App | BC-8 ✓ | LC 06:10:44.236 destroy / .249 create; E1 332–334 |
| 08:10:46–08:11:02 | Gear → Settings (System, dark); Info; **On**; **Off** (light); Debug; Settings; **System** → dark (follows Android) | User / App | BC-8 ✓ (rep., extra) | film |
| 08:11:20–08:11:26 | Recents → Settings app → **"Donker thema" off** | User | BC-8 | film; LC 06:11:22.382 stop |
| 08:11:28.9 | Recents → OpenControl: **Settings menu (Settings tab) restored**, light | User / App | BC-8 ✓ (F-1 for the menu) | LC 06:11:28.851 destroy / .857 create; film 4 fps 08:11:28.9 |
| 08:11:29.9 | Top-bar back → Connection | User | — | film |
| 08:11:38–08:11:44 | Gear → Debug: Debug mode **on → off** | User | BC-9 | film |
| 08:11:46–08:11:54 | **Export debug log** → save-as (Downloads, empty) → OPSLAAN → "Debug log saved (341 lines)." = **E1** | User / App | BC-9 ✓ (L3) | film; E1 (341 lines, ends 08:11:28.901); LC 06:11:46.037–06:11:53.891 |
| 08:11:58 | Debug mode back **on** | User | BC-9 ✓ | film |
| 08:12:12–08:12:22 | Quick Settings pulled down **over the app's Debug tab** (the app stays STARTED — no `wm_on_stop`); notification "… · 4 min / Connected — ANC: TRANSPARENT"; Bluetooth dialog "Pixel Buds Pro 2 va… Actief, Batterijniveau 100%" | User | BC-10 var. (app not on the Connection tab, under the shade) | film; LC 06:12:14.094 focus false |
| 08:12:22.5 | **Bluetooth off**: phone `DISC` HFP (A1494 08:12:23.703) and MAESTRO (A1548 08:12:23.888), `Disconnect` A1537 → reason `0x16` (A1561 08:12:23.979); `.last` ends 08:12:24.089 | User / OS | **BC-10** [K1] | HCI; film 08:12:24 "Bluetooth staat uit" |
| 08:12:24–08:12:36 | Bluetooth off (14.1-s HCI gap). **The app's adapter and loss-cause lines are in no file** (no export of process 12287 after 08:11:46; logcat main from 08:20:31) | — | BC-10 (log check not possible) | — |
| 08:12:36 | **Bluetooth on** — `.log` starts (B1 08:12:38.178 Reset); the phone pages the Buds (B153 08:12:39.269) → ACL B159; HFP B263; **app MAESTRO `SABM` B372 08:12:43.517** (ready, announcement ch 21 B399); claim B432: `Notify 01 e8 e8 08` (**Active**), battery `64 64 ff` | User / OS / App | **BC-11 ✓ (wire)** [`PAIR-003`] | HCI; film 08:12:42 "Verbinding maken" |
| 08:12:44–46 | Back to the app (Debug tab, "Unidentified frames (21)") → top-bar back → Connection, ready | User / App | BC-11 ✓ | film |
| 08:13:00–08:13:14 | Home (10 s) → OpenControl: Connection, ready | User / App | BC-11s ✓ (StrictMode not testable on release) | LC 06:13:01.996 stop, 06:13:14.366 restart |
| 08:13:24–08:13:34 | Quick Settings: notification **"Connected — ANC: ACTIVE"**, tile **"ANC / Active"** — the mode changed Transparent → Active **without any app `Set`** (B474 `08`) | OS / Buds | extra | film; B474 |
| 08:13:36–08:14:20 | Settings app → **Beveiliging en privacy** → Privacyopties → back; no Bluetooth auto-off setting found ("Meer beveiliging en privacy" not opened) | User | **BC-12 skipped** (not found in the test user) | film; LC 06:13:36.487 stop, 06:14:22.044 restart |
| 08:14:22 | Back in the app (Connection) | User | — | film |
| 08:14:42–08:15:02 | Sound → drag balance → `17:7` (B834 08:15:00.639 → OK B837 08:15:01.153, 514 ms) → "Right 4" | User / App | **BC-R ✓** [`AUDIO-003`] | B834/B837; film 08:15:02 |
| 08:15:20–08:15:28 | Quick Settings → gear → Settings → Apps → OpenControl → App-info ("Gedwongen stoppen" visible, **not tapped**) → Rechten | User | A5 | film (3 fps) |
| 08:15:32–08:15:34.2 | Rechten → "Apparaten in de buurt" → **"Niet toestaan"** | User | **A5** | film (3 fps) |
| 08:15:34.491 | **Process killed by the revocation:** phone `DISC` MAESTRO (B904; ACL stays up); last PID 12287 line 06:15:23.679 | OS | A5 ("the force-stop") | HCI; LC |
| 08:15:42.4 | OpenControl reopened → **new process 20326**: "Permissions (start): Bluetooth NOT_REQUESTED -> NOT_REQUESTED", "showing the system prompt" → system dialog "Toestaan dat OpenControl for Pixel Buds Pro 2 apparaten in de buurt vindt …?" over the **Sound** tab (dimmed) | App / OS | A5 var. (no "You denied" screen) | E2 1–3; LC 06:15:42.357/.412; film 08:15:42–52 |
| 08:15:54.1 | **"Toestaan"** → granted → link CONNECTED → **LINK_BACK re-open** 08:15:54.174 → ch 21 (B980); claim B1000 `Notify … e8 e8 08`; balance read **17:7** ("Right 4", film 08:15:56) | User / App | A5 ✓ | E2 4–46; B965–B1049 |
| 08:16:02–08:16:12 | Home → Quick Settings → Connected devices → Pixel Buds Pro 2 → device details (address shown) → **forget** | User | **(E)** | film |
| 08:16:12.36 | Phone `Disconnect` → reason `0x16` (B1147); `Delete Stored Link Key` B1151 08:16:12.545; E2 "Session lost …", cause **"undetermined (no reading …) (provisional …)"** — the last cause line | OS / App | (E) ✓ | HCI; E2 49–52 |
| 08:16:14–08:16:44 | Device list without the Buds; home; lid closed and reopened (buds docked); Quick Settings: **no OpenControl notification** | User / OS | — | film |
| 08:16:46.8 | OpenControl: briefly Sound, then **"No Pixel Buds Pro 2 paired yet."** + Pair a device | App | (E) ✓ | E2 53–54; film 08:16:48 |
| 08:16:49.9–08:16:50.4 | **Pair a device** — one press on film; one "association requested" in E2 (08:16:50.982), no "request ignored" | User | **B4 var.** (no double tap effective) | film 4 fps; E2 55 |
| 08:16:51–08:16:58 | CDM "Zoeken naar een apparaat" (LE scan B1172–B1245); the case's pairing button pressed | User / OS | Z1 | film; HCI |
| 08:16:58–08:17:00 | CDM "Toestaan dat de app … toegang heeft tot de Pixel Buds Pro 2 van Ted?" → **Toestaan** | User | Z1 | film; E2 56 08:17:01.671 association created |
| 08:17:01.7 | createBond → phone page B1251 → ACL B1253 → **SSP numeric comparison, auto-accept** (Buds NoInputNoOutput), Simple Pairing Complete B1303, link key type `0x04` (B1304) → BONDED 08:17:05.283 | OS | **Z1 ✓** [`PAIR-001`] | E2 57–59, 98; HCI |
| 08:17:04.0 | The **Buds** open the RFCOMM multiplexer (B1466) and HFP (B1482); link CONNECTED → **LINK_BACK** re-open 08:17:04.156 → MAESTRO **DLCI 0x03** (B1601), ch 21; claim DLCI 0x05 `Notify 01 e8 00 20`; battery `e4 e4 ff`; Case 93 % | Buds / App | Z1 ✓ [`BATT-004`] | E2 60–99; B1466–B1713 |
| 08:17:02–06 | "Pairing… keep the Buds close to the phone." → "Connected to this phone (Android)", ready, L 100 ⚡, Case 93 %, R 100 ⚡ | App | Z1 ✓ | film |
| 08:17:06–08:18:28 | Idle on Connection (buds docked, lid open); **film ends 08:18:27.9** | — | BC-end (minute flip ✓) | film |
| 08:18:28–08:20:05 | After the film: runtime stream B1756–B1783 only; `.log` ends 08:20:05.132 | Buds | — | HCI |
| 08:20:31–08:23:20 | After the film and the HCI: three runtime packets (E2 112–117); the app stopped/restarted 08:22:02/08:22:24/08:22:33/08:23:01; export E2 saved 08:23:06–08 (Debug mode on at 08:23:10); logcat ends 06:23:20.880 UTC | User / App | (export, logs) | E2 112–125; LC main |

## Step mapping (skeleton Appendix A → what happened)

| Step | Result | Where |
|---|---|---|
| P0 | **not done as written** — no package list saved; the app drawer on film shows no Play Store; the logcat header says `full.secondary` | film 07:58:00; LC header |
| P1 | done — 1.0.0 / 8d8af4b on film; CDM association existed before the film (no picker at 07:58) | film; E1 |
| P2 | done — Bluetooth on at the film's start | film 07:57:40; A1 |
| P3 | done differently — ears in view at BC-6a and the undocking, **not** at BC-6 | film |
| P4 | not identifiable (DND / auto-off value not on film) | — |
| P5 | done (minute flips at both ends) | film, 5 fps |
| P6 | done — ANC tile in the test user's Quick Settings | film 08:10:22, 08:13:34 |
| P7 | done, **before** the first ready (Info, licence, both links) and repeated after it (firmware lines) | film 07:58:24–08:00:12 |
| P8 | done — launcher "OpenControl", notification title | film |
| BC-1 | done | 08:01:10–24 |
| BC-2 | done (ANC, Sound, Controls, Find) | 08:01:31–08:02:38 |
| BC-3r | done | 08:02:50–08:03:24 |
| BC-3 | done — Right 4 on the 17th drag | A876 |
| BC-5 | done | A882 |
| BC-6a | done (Right out on 21 → 19), head on frame-left | A945/A982 |
| BC-6 | done differently — a bud out on 19 → `DISC` + 21; **which bud is not identifiable** | A1085/A1123 |
| BC-7 | done (watch) — no cut-off text on film, no other claimant | FINDINGS §2 |
| BC-8 | done differently — app Off/System **and** On; Android's dark theme via the Settings app (twice: on 08:10:39, off 08:11:24) | film |
| BC-9 | done — E1 with Debug mode off | film 08:11:38–58 |
| BC-10 | done differently — Bluetooth off from the shade over the Debug tab; **no log of the app's lines** | HCI A1494–A1561 |
| BC-11 | done — automatic re-open on the wire | B372 |
| BC-11s | done (home 10 s) — StrictMode check not testable on the release build | LC |
| BC-12 | **skipped** — the setting was not found in the test user | film 08:13:36–08:14:20 |
| BC-R | done | B834 |
| A5 | done differently — order before (E); the app showed the system prompt at once | 08:15:20–08:15:54 |
| (E) | done | B1145–B1151 |
| B4 | **not demonstrated** — one effective tap | E2 55 |
| Z1 | done — SSP, automatic re-open | B1251–B1713 |
| BC-end | done | film end |
| Extra | the Connect tap with the lid closed (07:58:39); a tab tour (08:08:42); dark mode On (08:09:24, 08:10:52); the ANC mode change by the Buds (Transparent → Active) | timeline |

Registry Test-IDs: [`AUDIO-003`] BC-3/BC-5/BC-R ✓; [`INEAR-002`] 08:05:12 ✓; [`INEAR-003`] BC-6a ✓; [`INEAR-004`] BC-6 (bud not identifiable); [`PAIR-003`]
BC-11 ✓ (wire only), also after A5 and Z1; [`BATT-004`] every connect ✓; [`PAIR-001`] Z1 ✓.

## Analysis checklist

- [x] Pre-filter by the Buds' classic handle; DLCIs by content — FINDINGS header.
- [x] P0/A.0 — user type and package absence from the logcat header and the film; APK identity from `dist/1.0.0` (read only).
- [x] X / BC-7 — 6 claims, all the app's; 0 × `03 08 00 02 01 25` (positive control `CAP-066`) — FINDINGS §2.
- [x] P7/P8 — FINDINGS §6 (no system log: `ACTION_VIEW` only on film).
- [x] BC-1 … BC-3r, BC-8 — FINDINGS §6.
- [x] BC-3, BC-5, BC-R — FINDINGS §5 (channel 19 bytes of `17:7` not captured: every write was on 21).
- [x] BC-6 — FINDINGS §4.
- [x] BC-9 — FINDINGS §0 (not discriminating by content).
- [x] BC-10/BC-11 — FINDINGS §7 (app lines missing).
- [x] BC-11s, BC-12 — not testable / skipped.
- [x] IX — FINDINGS §7, §8.

---

## Appendix A — the planned procedure (skeleton as committed before the run, `8d8af4b`; text unchanged, heading levels lowered by one)

### Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, a profile without Google Play) — Group BC, the 1.0.0 release APK (`CAP-067`)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0064`, 2026-10-01; scope and order are the maintainer's choice in chat 2026-10-01,
`AskUserQuestion` "F-5 CAP-067": *"As listed, destructive last (Recommended)"*). **Adapted 2026-10-02 (`ai-sessions/0066`, the maintainer's changes before
the run):** the balance steps `[‹]`/`[›]` are gone (section II is the slider again) and Info has no "Licence on GitHub" link (P7: two links). **Adapted again
2026-10-02 (`ai-sessions/0065` follow-up, the maintainer's request in chat):** the run uses the **release-signed 1.0.0 APK** (`scripts/release.sh`, checkpoint
answer (a)) in a **GrapheneOS secondary user without Google Play** (answer (f) — the evidence for `PROJECT.md`'s Definition of done 1–3); new P0, P1/P7/P8
and A.0 rows, BC-7 is now a negative check, BC-11s cannot be shown on a release build, BC-12 and IX note what is per user and what is device-wide.
After the run: rename this folder from the placeholder
`CAP-067-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BC` to the film's first/last overlay times and analyse it as `CAP-066` was (`ai-sessions/0063`).

**Purpose:**

- **A.0/P7 — the Info tab of the `0064`/`0066` build** (F-6, DECISIONS.md ADR-050 and its 2026-10-02 Update): the build line, the licence line "GNU Affero General
  Public License v3.0 or later (AGPL-3.0-or-later)", **Read the licence** (the bundled text, offline), and the two links (README, issues) — each opened once **on film** (the maintainer approved links at the
  `0064` checkpoint, "F-6 links": *"Links + bundled licence (Recommended)"*).
- **I — F-1, the tab across a configuration change.** `CAP-066` K4r: rotating reset **Sound → Connection** (`CAP-066-FINDINGS.md` §6). The `0064` build skips the
  pager ↔ back-stack sync until the restored back stack is known (`OpenControlNavHost.kt`, test `TabRestoreTest`).
- **II — the balance slider (BB-10 again).** The `0064` steps were removed in `ai-sessions/0066` (the maintainer's choice); the slider writes once per release.
  `CAP-066` BB-10: 32 drags, never Right 4 (`17:7`). Here: drag to Right 4 (count the drags) — the value before `CAP-064`.
- **III — BB-12, the open half of lead L-1** (`PROTOCOL.md` §2.2a, 2026-10-01 `0063` Update 🟡): with both buds worn on channel **19**, take the **Left** out —
  predicted: a Buds `DISC` of MAESTRO with the ACL up, then announcement **21**.
- **IV — BB-15, watch only:** an answer cut off by the claim's close (`ai-sessions/0062` F-3) — never seen on hardware yet. **Without Play services in this
  user, no other app should claim the Message Stream at all** — so here BC-7 is mainly the negative check of the Definition of done (below).
- **V — K4d's rest:** dark mode **Off**, and **System** with Android's own dark switch (a configuration change, so also an F-1 check).
- **VI — L3:** an export with Debug mode **off** (no hex lines).
- **VII — F-3 and F-4 after a Bluetooth off/on:** the export names the loss "Bluetooth was switched off on this phone" (final, no "(provisional …)") and logs
  "Bluetooth adapter: ON -> TURNING_OFF"; logcat's StrictMode lines after the off/on (F-4: the app closes every profile proxy it obtained; 🟡 the framework's
  `BluetoothLeAudio` may still warn — `CAP-066-FINDINGS.md` §8). **On the release APK StrictMode is off** (`OpenControlApplication.kt:63`, `BuildConfig.DEBUG`
  only), so F-4 is not observable in this run — no StrictMode line is no evidence either way.
- **VIII — K5:** GrapheneOS's Bluetooth auto-off set to a short value on film, then restored (`CAP-066`: it was disabled, `delayMillis: 0`).
- **IX — destructive last:** A5 (*Nearby devices* denied), (E) the Buds forgotten, B4 (Pair a device twice quickly), Z1 (re-pairing, `PAIR-001`).
- **X — the Definition of done without Google Play services** (`PROJECT.md`, `ai-sessions/0065` §A.4; the maintainer's choice (f)): connect (1), battery and ANC
  (2), several connect/disconnect cycles (3 — BC-6/BC-10/BC-11/Z1 give them), all in a user where Google Play is not installed, with **no** Play-services claim
  on the Message Stream in the HCI log. 🟡 HYPOTHESIS (`ai-sessions/0066` RESULT §C.1): the Owner's sandboxed Play services cannot use Bluetooth while this
  user is in the foreground — the HCI log decides.

**Facts the maintainer gave (chat 2026-10-01):** no narration on the films; Play services' *Nearby devices* permission is **allowed** (in the **Owner**; the test
user has no Play services); the build is read from the Info tab on film. **Chat 2026-10-02:** a new build was tried in a GrapheneOS profile without Google Play
services before the run — "it all seems to work".

---

#### A.0. Which phone, which app, which build

| Item | Value |
|---|---|
| Phone | **Pixel 9a, GrapheneOS** — the run is in a **secondary user without Google Play**; note its user id (P0) |
| App under test | **OpenControl for Pixel Buds Pro 2 1.0.0**, the **release APK signed with the maintainer's key** from `dist/1.0.0/` (`scripts/release.sh`, `RELEASING.md` §5) — read from the **Info tab on film** (P7: "App: 1.0.0, build <hash> (<commit date>)", **no** "-dirty"); its SHA-256 and certificate SHA-256 noted from the script's output |
| Official Pixel Buds app | **Not used.** Pixel 7a: Bluetooth off |
| Google Play in the test user | **not installed** (P0); in the Owner: sandboxed Play present, *Nearby devices* allowed (unchanged) |
| Buds | Pixel Buds Pro 2, firmware `release_5.203` |

#### A.1. Preparation

| # | Check | Done |
|---|---|---|
| P0 | In the test user: `adb shell am get-current-user` (the user id); `adb shell pm list packages --user <id> \| grep -i -E "gms\|vending"` — note the output **and the exit status** (1 = none); positive control: the same with `grep opencontrol` (exit 0). Save both outputs into this folder | ☐ |
| P1 | The **release** APK installed in the test user. An app package is device-wide: if the debug build (debug key) is still installed in **any** user, the release APK is refused (different signing key) — first `adb uninstall io.github.tedsluis.opencontrolpixelbuds` (removes it from **all** users, with their app data and the app's pairing association). Then, in the test user: notifications allowed, **Pair a device** once (the companion association is per user; the Buds' bond is device-wide) | ☐ |
| P2 | Bluetooth HCI snoop log on — a device-wide developer option, set it **in the Owner** (developer options are usually not available in a secondary user), then switch to the test user; switch Bluetooth off and on **on film** | ☐ |
| P3 | Camera films the phone, the case and **your head** (ears visible for every wear step); head on the right of the frame = Left bud (as `CAP-064`) | ☐ |
| P4 | Do Not Disturb on (in the test user); GrapheneOS Bluetooth auto-off: note its current value (section VIII restores it) — if the setting is not shown in the test user, note that and read it in the Owner | ☐ |
| P5 | Status bar on film across a minute change at the start and at the end (the clock offset) | ☐ |
| P6 | Quick Settings **of the test user** (tiles are per user): the ANC tile added and large (as `CAP-066` BB-13), the Bluetooth tile reachable | ☐ |
| P7 | After the first "ready": gear → **Info** — hold 3 s (build line, firmware lines, "Control channel: N"); then **Read the licence** — scroll once, hold 3 s, **Close**; then **README on GitHub**, **Report an issue on GitHub** — each opens the browser on film; back to the app after each | ☐ |
| P8 | The launcher shows **"OpenControl"** under the icon; the connection notification's title reads "OpenControl for Pixel Buds Pro 2" (ADR-051) — on film once | ☐ |

**Rhythm:** one action, then wait 5–10 s (longer where a step says so). Something unexpected: stop, wait 10 s, continue.

#### A.2. Steps

DLCI numbers are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05). Read the announced channel from the export line "Maestro channel announced by
the Buds: N".

#### P7 expected (F-6)

| What | Expected on screen | Expected on the wire / in the logs | Refuted if |
|---|---|---|---|
| Info tab | "App: 1.0.0, build <hash> (<date>)" without "-dirty"; "Works with Google Pixel Buds Pro 2. Not affiliated with or endorsed by Google. Pixel Buds is a trademark of Google LLC." (ADR-051); "Licence", "GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)", "Read the licence" (no "Licence on GitHub"); "Project", "README on GitHub", "Report an issue on GitHub", "Links open in your browser; this app itself has no internet access."; then "The Buds" with the firmware lines | nothing from the app on RFCOMM while the menu is open | a link is missing or the menu sends anything |
| Read the licence | a dialog "Licence" with the text starting "GNU AFFERO GENERAL PUBLIC LICENSE / Version 3, 19 November 2007", scrollable; **Close** | — (bundled, offline) | the text does not show, or a browser opens |
| Each link | the browser opens `…/blob/main/README.md`, `…/issues` (github.com/tedsluis/opencontrolpixelbudspro2) | system log: an `ACTION_VIEW` start of the browser (`START u0 {act=android.intent.action.VIEW dat=https://github.com/…}`); the app's merged manifest has no `INTERNET` | another address opens, or no app opens without the message "No app on this phone can open web links. The address is …" |

##### I. F-1 — the tab across a configuration change

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BC-1 | ready, **Connection** tab | Rotate to landscape, wait 3 s, rotate back | Connection stays selected and shown | nothing (UI only); logcat `wm_on_create` (activity relaunch) per rotation | — |
| BC-2 | ready | Repeat BC-1 on **ANC**, **Sound**, **Controls**, **Find** | the same tab stays selected and shown, each time | nothing | the screen switches to Connection (the `CAP-066` K4r defect) |
| BC-3r | ready | Gear → **Info**; rotate and back; then the top bar's back arrow | still Settings → Info after each rotation; back returns to the tab the menu was opened from | nothing | the menu closes or another tab shows |

##### II. The balance slider — BB-10 again (`qhr` field 17, ADR-045 unchanged; the `0064` steps removed in `ai-sessions/0066`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BC-3 | ready, **Sound** tab, balance read as **Centre** (`CAP-066` ended at `17:0`) | Drag the slider toward **R** and release, aiming at **Right 4**; repeat until the label reads Right 4 (count the drags; say nothing — the film shows the label) | the label after each OK | exactly one `WriteSetting 4:{17:n}` per release (zigzag), none during a drag, each → empty `RESPONSE` OK; the target **`17:7`**: on channel 21 = `CAP-064` 6671 `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 05 22 03 88 01 07 a9 7d 5e df 03 7e`; on channel 19 (derived, not captured) `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 05 22 03 88 01 07 e9 b8 6e 25 7e` | a write per drag frame, or the label changes before the OK |
| BC-5 | after BC-3 | Drag to near the centre and release | a release within ±3 of the centre writes `17:0` ("Centre") | one write | as BC-3 |

##### III. BB-12 — lead L-1, the Left out with both worn on 19

| Step | Pre-state | Action | Expected | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BC-6a | ready, both worn | If the export's last announcement is **21**: take the **Right** out onto the table, wait 20 s, put it back in, wait 20 s (`CAP-066`: ⇒ `DISC` + 19) | the app re-opens by itself | Buds `DISC` of MAESTRO with the ACL up, then announcement 19 | — (a precondition) |
| BC-6 | both worn, announcement **19** | Take the **Left** bud out of the ear onto the table (on film); wait 20 s | the app re-opens by itself if the Buds close the channel | 🟡 predicts: a Buds `DISC` of MAESTRO with the ACL up, then announcement **21** [`INEAR-004`] | the session stays on 19, or comes back on 19 |

##### IV. BB-15 — the cut-off watch (cannot be provoked) — and the negative check for the Definition of done

| Step | Pre-state | Action | Expected on screen | Expected on the wire |
|---|---|---|---|---|
| BC-7 | the whole run | — (watch only) | **expected here: never** "The answer was cut off — another app took the Buds' channel …" — no other app should claim the channel in a user without Play services. If it does show: note the time on film; it refutes "no Play services claim" for this run | **expected: no** Message Stream claim whose first phone message is `03 08 00 02 01 25` (the Play-services marker, `CAP-066-FINDINGS.md`); every claim's first message is the app's `08 11`/`08 12` (or its battery/Find sequence). A cut-off would be: the app's `08 12`/`08 11`, then a phone `DISC` before the Buds' ACK/`Notify` (as `CAP-065` 2640 → 2649 → 2651) |

##### V. K4d — dark mode Off; System with Android's own switch

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BC-8 | ready, Android **light**, the **Sound** tab | Gear → Settings → **Dark mode: Off**; back; gear → **System**; back; Quick Settings → Android's **dark theme on**, wait 5 s, **off** | Off = light; System follows Android's switch at once; **the Sound tab stays** through Android's switch (a configuration change — F-1) | nothing | the scheme does not follow, or the tab resets |

##### VI. L3 — an export with Debug mode off

| Step | Pre-state | Action | Expected | Expected in the file | Refuted if |
|---|---|---|---|---|---|
| BC-9 | Debug mode on (as usual) | Gear → **Debug** → **Debug mode off** → **Export debug log** (a second file); then Debug mode back on | the save dialog; "Debug log saved (N lines)." | no `DLCI 0x..:` hex lines after the switch-off line; state lines still there | a hex line after the switch-off |

##### VII. F-3 / F-4 — Bluetooth off and on (K1/K2 again)

| Step | Pre-state | Action | Expected on screen | Expected in the logs | Refuted if |
|---|---|---|---|---|---|
| BC-10 | ready, the app **on screen** | Quick Settings: **Bluetooth off**; wait 10 s | "Bluetooth is disabled." + Enable Bluetooth; no crash | export: "Bluetooth adapter: ON -> TURNING_OFF", "… -> OFF", "Session lost: channel 0x02 closed …", **"Session loss cause: Bluetooth was switched off on this phone"** — without "(provisional …)" — and no later "undetermined" line; system log `BluetoothAutoOff … STATE=13`, `STATE=10` | the cause is "undetermined" or provisional, or no adapter line |
| BC-11 | after BC-10 | Bluetooth on; wait for the automatic re-open | ready | export "Bluetooth adapter: OFF -> TURNING_ON", "-> ON", "Automatic re-open of the session (ADR-044, trigger: LINK_BACK)" [`PAIR-003`] | no re-open while the app is visible and Android reports the Buds connected |
| BC-11s | after BC-11 | Leave the app (home), wait 10 s, come back | — | logcat: "Android link observer stopped" then "… started". **Release APK: no StrictMode** (debug builds only) — the `LeakedClosableViolation` check of F-4 is **not testable** in this run; record it as such, not as "no violation" | — (information) |

##### VIII. K5 — GrapheneOS Bluetooth auto-off

| Step | Pre-state | Action | Expected on screen | Expected in the logs |
|---|---|---|---|---|
| BC-12 | buds in the case, lid closed (no connection) | Settings → Security & privacy → Bluetooth auto-off: the **shortest** value (on film; if the setting is not available in the test user: skip BC-12 and note it — do not switch users in the middle of the run); lock the phone; wait past that time; unlock; open the app; then **restore** the value of P4 (on film) | "Bluetooth is disabled." (normal), no crash | system log `BluetoothAutoOff` "scheduled alarm" and the adapter `STATE=13`/`STATE=10`; the app's export (if open then) "Bluetooth adapter: … -> OFF" |

#### Restore

| Step | Action | Done |
|---|---|---|
| BC-R | Sound tab: drag to **Right 4** (`17:7` → OK) — the balance before `CAP-064` (if BC-3 ended there, nothing to do) | ☐ |

##### IX. Destructive steps — last

| Step | Pre-state | Action | Expected on screen | Expected on the wire |
|---|---|---|---|---|
| A5 | — | Settings → Apps → OpenControl → Permissions → *Nearby devices*: **Don't allow**; open the app; allow again (the permission is per user) | "Bluetooth permission needed" / "You denied the permission." + Allow | — |
| (E) | — | Forget the Buds in Android's Bluetooth settings (in the test user); open the app. **Note:** the bond is device-wide — forgetting here also unpairs them for the Owner (and its Play services); Z1 pairs them again device-wide | "No Pixel Buds Pro 2 paired yet." + Pair a device | bond removal |
| B4 | after (E) | Open the lid; tap **Pair a device twice quickly** | one picker, no crash | — |
| Z1 | after B4 | Pair in the picker; Connect | ready | CDM association, bonding (SSP or CTKD, `PROTOCOL.md` §5.1) [`PAIR-001`] |
| BC-end | — | Status bar across a minute change; stop the film | — | — |

#### A.3. After the run

Gear → **Debug** tab → **Export debug log** — the file lands in the **test user's** storage (e.g. `/storage/emulated/<id>/…`): `adb pull` from that path or share
it; `adb bugreport` (covers all users); the raw `btsnoop_hci.log` (both, if Bluetooth was toggled); app logcat and system log (GrapheneOS log viewer); P0's two
outputs; the film. All into this folder, then `sha256sum *`. Before committing: the camera films may carry a street-address overlay — check and mask.

#### A.4. Analysis checklist

- [ ] Pre-filter by the Buds' classic handle; DLCIs by content.
- [ ] P0/A.0: the test user's id and package list (no `com.google.android.gms`, no `com.android.vending`; exit status and positive control); the APK's SHA-256 and
      certificate SHA-256 against `scripts/release.sh`'s output.
- [ ] X / BC-7: every Message Stream claim and its first phone message — **zero** `03 08 00 02 01 25`; the command, its exit status and a positive control (the same
      filter matching a Play-services claim in `CAP-066`). Only then may the Definition of done 1–3 be ticked (`PROJECT.md`, with this capture as evidence).
- [ ] P7/P8: the Info frames — "1.0.0", build hash without "-dirty" against `git log`, the ADR-051 notice; launcher label and notification title; the licence line; the dialog's first line; each link's browser frame and its `ACTION_VIEW` system-log line;
      no app RFCOMM frame while the menu was open.
- [ ] BC-1 … BC-3r, BC-8: per rotation / dark switch, the tab before and after (film) against the logcat `wm_on_create` times — **refuted if** any tab other than
      Connection comes back as Connection.
- [ ] BC-3, BC-5, BC-R: every `WriteSetting 4:{17:n}` (zigzag) with its `RESPONSE`, one per release; the number of drags to reach `17:7`; the label frames after each OK; the
      channel-19 bytes of `17:7` against the derived frame above (a first capture of it).
- [ ] BC-6: the announcement before and after against which bud was taken out (film), and any Buds-side `DISC` with the ACL up — settles BB-12 (L-1).
- [ ] BC-7: any app claim closed between the request and the answer (expected: none).
- [ ] BC-9: no hex line after the Debug-mode-off line.
- [ ] BC-10/BC-11: the export's adapter and loss-cause lines against the system log's `STATE_CHANGED` times; "provisional" must not appear on the Bluetooth-off line.
- [ ] BC-11s: "not testable on the release APK" (StrictMode is debug-only) — F-4 stays open for a later debug run.
- [ ] BC-12: the auto-off alarm lines and the app's adapter lines.
- [ ] IX: `APP_TESTPLAN.md` sections A, B, E; Z1's bonding events.
- [ ] Registry Test-IDs: [`AUDIO-003`] (BC-3, BC-5, BC-R), [`INEAR-002`]–[`INEAR-004`] (BC-6a/BC-6), [`PAIR-003`] (BC-11), [`BATT-004`] (every connect), [`PAIR-001`]
      (Z1).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-067-2026-10-03_07-57-35_08-18-29-Group_BC/CAP-067-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-067-2026-10-03_07-57-35_08-18-29-Group_BC/CAP-067-EVENT-NOTES
