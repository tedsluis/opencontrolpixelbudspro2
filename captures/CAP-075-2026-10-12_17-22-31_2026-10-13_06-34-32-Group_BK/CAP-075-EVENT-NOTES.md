# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, the Owner user **with** Google Play services and the secondary user without) — Group BK, 1.2.1: the "Noise-control channel" switch (`CAP-075`)

**Status:** ✅ **captured 2026-10-12 (set 1, the secondary user without Google Play, 17:22–17:31) and 2026-10-13 (set 2, the Owner user with Google Play,
06:18–06:36); analyzed 2026-10-13 by `ai-sessions/0088`** (`CAP-075-FINDINGS.md`). The run was made in the **reverse order** of the skeleton (part 2 first, on
the evening of 2026-10-12; part 1 the next morning) and as **two sets of files**, each with its own screen recording, its own pair of HCI logs (the log the
Bluetooth stack rotated at the Bluetooth off/on of each set, `.log.last`, and the live one), its own debug export and its own app logcat; set 2 also has the
system log (two files) and an earlier, unfilmed 1.2.1 session of the Owner user on the evening of 2026-10-12 inside its rotated HCI log. The skeleton's
procedure, with its reference frames, is kept below as the appendix; the steps as they were actually run are mapped to BK-1 … BK-21 in "The maintainer's
steps, as run". **The skeleton's "After the run" was done after the analysis, per Checkpoint 0 of `ai-sessions/0088` (chat 2026-10-13):** the folder renamed to the
first frame of film 1 and the last frame of film 2, the recordings renamed CAP-075-recording1.mp4 / CAP-075-recording2.mp4 and **committed as is** (the
maintainer's choice: no blur of the Wi-Fi name, the serials or the e-mail fragment, audio kept), the eight mode-750 files set to 644, the system log committed as
the redacted extract CAP-075-logcat-extract.txt (the two full files local only), the exports committed as is (their serial hex lines, as `CAP-073`/`CAP-074`),
CAP-075-sha256sum.txt over the committed data files; no APK pull (⚪ stays), no P0/P1 shell outputs. The release verdict (`RELEASING.md` C4) is proposed in `CAP-075-FINDINGS.md` §11 and is the
maintainer's to give.

**In one paragraph.** Without Play (set 1) the switch changes nothing, as designed: no other client ever touched the channel, every one of the 41 press-and-holds
made while the noise-control tab was shown arrived on this app's own claim and was on screen within a second, in both switch positions, and no hint was ever
shown. With Play (set 2) the behaviour of `CAP-074` is back: Play's Fast Pair takes the channel 5.3–5.8 s after it lost it, and its retry after a collision comes
2.2–2.8 s later — the same timers as `CAP-074`, over 13 and 14 measurements. **"Show a hint"** did what ADR-062 says (the hint with the cut's time, no re-claim)
and showed none of the 9 holds made while it was selected as they happened. **"Take it back"** ran three chains of exactly three re-claims (3 000 / 6 000 /
12 000 ms in the export), every re-claim colliding with Play's retry (attempt 1 refused by the stack, attempt 2 connecting ≈ 0.9 s later and killing Play's
socket), every re-claimed hold cut again 5.3–5.8 s after Play's loss; of the 31 holds made in set 2's film, 8 were on screen within a second (5 on the app's own
claim, 3 by a re-claim's own read), 2 only after the maintainer tapped, and 16 never. The stack path `CAP-074-FINDINGS.md` §8/§10 left 🟡 is on the system log
for 15 of 15 cuts in the log's reach.

**Not in this run** (as the skeleton allowed): BK-11 … BK-13 (a tab change, Home or the switch *within 2 s of a cut* — the one "re-claim cancelled" of the run
is before set 2's film, at 06:19:04), BK-19 … BK-21 (the tile, Home/return, a loss with the tab shown), BK-end2's switch back to "Show a hint" in set 2 (the
Owner user's stored choice is "Take it back" since 06:32:26), P0/P1's shell outputs, the second system log of part 1 (one `bugreportz` at 06:35:41 and the
GrapheneOS log viewer's export are the two system-log files), the APK pull, the camera (none planned). Play's *Nearby devices* permission state and Play's
version are **not** on film (P0's "on film" item not done).

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-075` |
| Group(s) | BK (new) |
| Date | **set 1:** 2026-10-12 17:22:31.85–17:28:42.7 on film (the files to 17:31:58); **set 2:** 2026-10-13 06:29:04.15–06:34:32.0 on film (the app process from 06:18:58, the files to 06:36) — all times the phone's local time (CEST, UTC+2); the app logcats and the System log are in UTC |
| Phone | Pixel 9a, GrapheneOS, Android 17 (`CP3A.261005.005` in `CAP-074`; not re-read here). **Set 1: the secondary user** (the app logcat's header `userType: full.secondary`; the 1.2.0 process of 17:22 and the 1.2.1 process of 17:25 both `package … :10201`). **Set 2: the Owner user 0** (`am_proc_start: [0,17838,10354,io.github.tedsluis.opencontrolpixelbuds,…]` at 06:18:58.248 in CAP-075-logcat-all.txt and the same line, in UTC, in the System log; the app logcat's header has no `userType` line) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.2.1** (versionCode 10201) — **Info tab on film in both users: "App: 1.2.1, build 31ee183 (2026-10-12)"**, no "-dirty" (film 1 s187–189 = 17:25:38.9–40.9; film 2 s23–36 = 06:29:27–40). Installed over 1.2.0 `5b4d5db` during set 1's film (Android's "Updaten…" overlay film 1 s166–168 = 17:25:17.9–19.9; the 1.2.0 process 11917 lost the foreground 17:25:17.843, the 1.2.1 process 14361 was created 17:25:20.128 — app logcat 1, UTC 15:25:17.843 / 15:25:20.128). The installed APK's hash: **not pulled** (⚪, Checkpoint 0) |
| Previous version in each user | 1.2.0 build `5b4d5db` — on film in set 1 (s0–4, "App: 1.2.0, build 5b4d5db (2026-10-10)", Settings without the "Noise-control channel" card, s7–10); the update kept the app's data (Debug mode and Dark mode as before; `firstInstallTime` not read: P1 not run) |
| Google Play services | **set 2: present and active** — uid 10205, process 7277, 19 Message Stream claims of its own in the live HCI log and 33 `cleanup_rfc_slot … app_uid: 10205` lines in the system log; its notification "Google Play-services needs to sho…" on film 2 s3–5. **Set 1: absent** — 0 frames with Play's first message `03 08 00 02 01 25` in both set-1 HCI logs (positive control: 22 in `CAP-074-btsnoop_hci.log`, 6 + 18 in set 2's two logs), 0 "closed, not by this app" lines in export 1. *Nearby devices* permission and Play's version: **not on film** |
| Official Pixel Buds app | not used |
| Buds | Pixel Buds Pro 2, firmware **`release_5.203` ×3** (film 1 s16–22 and s187; film 2 s24–36; `GetSoftwareInfo` answers hci1.log 7xx / hci2.log 1190); the three serials on film (not written here). **Worn** for every hold of set 1 from 17:26:04 (Settable byte `e8` from claim #5; `00` at 17:22:45, 17:23:10, 17:23:23, 17:25:20 and 17:25:24 — the buds were in the case until after 17:25:24, "Not worn (checked 17:25:24)" film 1 s209) and for every hold of set 2's film (`e8` from 06:28:55; `00` at 06:19:01 — in the case) |
| Video files | **set 1:** CAP-075-recording1.mp4 (Android's name carried the stamp 20261012-172842 = the end) — 370.84 s, 4 203 frames at 11.33 fps (variable), 1080×2424, **with sound** (AAC); **set 2:** CAP-075-recording2.mp4 (Android's stamp 20261013-063431 = the end; its epoch stamp 1791865743.758 = the start 06:29:03.758) — 327.89 s, **with sound**. No camera. Both **committed as is** (Checkpoint 0) |
| Log files | **set 1:** CAP-075-btsnoop_hci1.log.last (1 110 packets, 17:22:1x–17:23:04 — rotated at the Bluetooth off of 17:23:04), CAP-075-btsnoop_hci1.log (1 832 packets, 17:23:08–17:28:47), the export CAP-075-opencontrol-debug-20261012-173158.txt (228 lines, process 14361 only — the 1.2.1 process), the app logcat CAP-075-OpenControl-for-Pixel-Buds-Pro-2-log-97b3b88fc408.txt (272 lines, UTC, both processes); **set 2:** CAP-075-btsnoop_hci2.log.last (21 824 packets, **2026-10-12 18:13:13 – 2026-10-13 06:29:14** — rotated at the Bluetooth off of 06:29:13; it holds the Owner's unfilmed evening session and the 06:19 session), CAP-075-btsnoop_hci2.log (7 291 packets, 06:29:17 – 06:36), the export CAP-075-opencontrol-debug-20261013-063533.txt (512 lines), the app logcat CAP-075-OpenControl-for-Pixel-Buds-Pro-2-log-b6d40f91b2ca.txt, **the system log twice, kept local only** (both in ~/opencontrol-capture-originals/CAP-075/): the `bugreportz` file (83 255 lines, UTC — the main buffer only from 04:34:02 UTC) and the GrapheneOS log viewer's full log (135 471 lines, local time — the main buffer from 06:26:54.556, line 71 989, which covers every cut of the film); **committed as the extract CAP-075-logcat-extract.txt** (20 711 lines: the lines of the app 17838, the Bluetooth process 21872/22714 and Play 7277 on 2026-10-13 06:18:00–06:36:30, the Wi-Fi name and the account name dropped, every MAC-shaped string reduced to its first and last octet; `grep -c` for the Wi-Fi name, the account and an e-mail pattern → 0, exit 1 each; it keeps all 31 `already at opened state`, 79 `cleanup_rfc_slot` and 33 `Retry times remaining` lines). CAP-075-sha256sum.txt: the eleven committed data files |
| Buds' identity in the logs | the same address in all four HCI logs (first two octets `04:00`, last `07`); ACL handle `0x000b` in every ACL of the run |

## Capture-integrity pre-flight

- **Folder as delivered** (before any rename or mode change): 13 files; modes 750 on the two exports, the two app logcats, the two recordings, the System log and
  the logcat-all file, 644 on the four HCI logs and the skeleton; the sizes and SHA-256 sums match the prompt's §2 listing exactly (re-derived with `ls -l` and
  `sha256sum`).
- **Four HCI logs, two per set, each pair split by a Bluetooth off/on on film:** set 1 — film 1 s32–38: the Bluetooth dialog's "Bluetooth staat uit", the ACL to
  the Buds ends with `0x16` (Connection Terminated by Local Host, the phone's own off) at 17:23:04.162 (hci1.log.last frame 1097; the LE link 1096) and the
  phone pages the Buds again at 17:23:08.795 (hci1.log 145, `Create Connection`; the ACL 151 at 17:23:09.631). Set 2 — film 2 s9–18: `0x16` at 06:29:13.490
  (hci2.log.last 21818; `BluetoothAdapterState: TURNING_OFF` 06:29:12.990 in the logcat-all file); the phone pages again at 06:29:19.081 (hci2.log 163; the ACL
  469 at 06:29:21.136). **Every ACL of the run is paged by the phone** (`Create Connection` 0x0405 to the Buds' address: hci1.log.last 499, hci1.log 145,
  hci2.log.last 250, 2454 and 18117, hci2.log 163), never by the Buds.
- **Link ends:** set 1 — the Buds close DLCI 0x02 (hci1.log 1805, 17:28:47.096, `DISC` Buds → phone), send `AT+BIEV=2,100` on the HFP channel (1807) and end
  the ACL with `0x13` (Remote User Terminated Connection) at 17:28:47.531 (1814), 4.9 s after the app went to the background (export 1: "hold ended: the app is
  not visible" 17:28:43.653; "channel 0x02 lost (read return -1)" 17:28:47.101). Set 2 — the Buds close DLCI 0x02 and 0x04 at 06:28:49.008/.009 (hci2.log.last
  21490/21491, both `DISC` Buds → phone; the phone's `UA` 21492/21493), send `AT+BIEV=2,100` (21495), and the ACL **stays up** until the Bluetooth off; the app's
  session is lost (export 2 06:28:49.051) while the app is in the background. Both losses are the Buds' own closes (🟢 FACT for the direction), their trigger is
  an OPEN QUESTION (`CAP-075-FINDINGS.md` §7, §12).
- **Scoping:** every `tshark` filter below is on the Buds' ACL handle (`bthci_acl.chandle==0x000b`), shown matching a known frame (hci2.log 2298 = claim #4's
  `SABM`, `btrfcomm.dlci==4 && btrfcomm.frame_type==0x2f`). The address filter `bluetooth.addr` matches nothing on these logs (as `AGENTS.md` §13 step 1 warns).
- **pw_hdlc CRC-32 on DLCI 0x02 (the app's own channel):** 32 / 77 / 75 / 134 frames OK in the four logs, 0 bad, 0 short (scripts/pwrpc_decode.py, `--handle
  0x000b`); every `pw_rpc` status `OK`; 0 `DM` frames in all four logs (`btrfcomm.frame_type==0x0f`, exit 0; positive control: `0x2f` finds every `SABM`).
- **Clocks:** film 1 = phone − 17:22:31.85 ± 0.05 s (the status-bar minute flips at film 28.1, 88.1, 148.1, 208.1, 268.1 and 328.1 s, read from 10-fps strips, no
  drift over 5 minutes); film 2 = phone − 06:29:04.15 ± 0.05 s (flips at 55.8, 115.8, 175.8, 235.8, 295.8 s). Export, app logcat (UTC + 2 h), the logcat-all file
  and the HCI logs agree within 50 ms (e.g. claim #4's `SABM` 06:30:14.565 on the wire, "connected (attempt 2/3)" 06:30:14.639 in the export).
- **Processes:** set 1 — app 11917 (1.2.0, 17:22:00–17:25:17) and 14361 (1.2.1, from 17:25:20); set 2 — app 17838 only (06:18:58–06:35:52), Bluetooth 21872
  (until the off) and 22714 (from 06:29:17.570), Play 7277 (uid 10205), the Google app `com.google.android.googlequicksearchbox` uid 10302 (DLCI 0x08/0x0a, below).
  `am_crash` 0, `am_anr` 0 in both system-log files for the app; one `FATAL` at 2026-10-12 18:54:36 (process 26475, not this app); the `SIGQUIT` 06:35:41 is
  `bugreportz` (every Java process; `CAP-071-FINDINGS.md` §0).

## Video review method (and privacy)

Both films were viewed as 1-fps contact sheets (every second of film 1's 371 s and film 2's 328 s on 40 + 31 sheets, 2×2 per sheet at full resolution for the
app's text) plus 10-fps strips of the status bar around every minute change; a second is "proved unchanged" when the two sheets around it show the same screen
and the strip shows no transition. The audio tracks were down-mixed to mono WAV and the voiced segments tabulated by RMS against silence (film 1: mean −38.8 dB,
10 segments; film 2: −33.0 dB, 35 segments) — **the words are not intelligible to the analyst**; the press-and-holds are the maintainer's statement plus the
wire's unprovoked `Notify` frames (as `CAP-073`/`CAP-074`), and the cue segments fall 0.1–2.3 s after a `Notify` in the cases tabulated in
`CAP-075-FINDINGS.md` §4. **Privacy on film (Checkpoint 0):** film 1 shows the Wi-Fi network's name in the notification shade / Quick Settings (s23–27, s40,
s369–370) and the three serials on the Info tab (s16–22, s62–149, s187–189); film 2 shows the serials (s24–36) and, in the shade at s326–327, a Spotify media
card and a notification with the first characters of the maintainer's e-mail address; both audio tracks carry the maintainer's voice — **the maintainer chose to commit both films as is** (Checkpoint 0, "Commit as is"). The two
system-log files carry the Wi-Fi name (14 / 265 lines), MAC-shaped strings (89 / 534) and the Gmail account's name (7 / 8 lines); 0 e-mail addresses in the
exports and the app logcats (`grep -c '@'` on the four files: 0, exit 1; positive control: the logcat-all file, exit 0).

## Event Timeline

Times are the phone's local time; "film 1 sN" = film 1 second N (phone = N + 17:22:31.85), "film 2 sN" likewise (+ 06:29:04.15). "E1"/"E2" = the exports,
"L" = CAP-075-logcat-all.txt, "X" = CAP-075-System-log-571517c9248e.txt, "H1L"/"H1"/"H2L"/"H2" = the four HCI logs (hci1.log.last, hci1.log, hci2.log.last,
hci2.log). Claim numbers (#n) are `CAP-075-FINDINGS.md` §1's inventories (one per log). Every `Notify` is `08 13 00 04 01 <settable> <?> <mode>`; the modes are
`20` Off, `40` Adaptive, `80` Transparency, `08` Noise cancellation.

### Set 1 — 2026-10-12, the secondary user without Google Play (film 1: 17:22:31.85–17:28:42.7)

- **17:22:00.46** the 1.2.0 process 11917 starts (app logcat 1, UTC 15:22:00.463). The film starts 17:22:31.85 on the **Info** tab of **1.2.0**: "App: 1.2.0,
  build 5b4d5db (2026-10-10)", "The Buds: Not connected yet" (s0–4); Debug mode on, "Unidentified frames (0)" (s5–6); Settings **without** the Noise-control
  channel card (s7–10: Dark mode, Case sounds "—").
- **17:22:44** Connection tab "Paired — not connected to this phone … App control: not open yet — tap Connect" (s11–12), **Connect** tapped (s13 "connecting…"):
  the phone pages the Buds (H1L 499 `Create Connection` 17:22:44.672; the ACL 501 at 17:22:45.301, handle `0x000b`), the session opens on **channel 21**
  (announcement 17:22:45 on film s16–22), the snapshot claim #1 (H1L `SABM` 617 17:22:45.743, `08 11 00 00` 629, `Notify` 639 `…01 e8 00 20` — Off, Settable
  `00`: both buds in the case; the phone's `DISC` 841 17:22:47.292, 1.55 s later — the Connection tab is shown). s14: "Android doesn't show the Buds as connected
  (yet) · App control: ready", Left 100 % ⚡ / Case unavailable / Right 100 % ⚡, "Both buds in the case". `PAIR-003`, `BATT-004`.
- 17:22:47–54 Settings (Case sounds both on, s15); **Info** s16–22: firmware `release_5.203` ×3, "Control channel 21", the three serials with their labels
  (17:22:46). `FW-003` (1.2.0).
- **17:22:55–17:23:03** the notification shade (s23–27: the Wi-Fi name; "Pixel Buds Pro 2 va 100 %"; the app's notification "OpenControl … Connected — ANC:
  OFF"), full Quick Settings (s28–30), the Bluetooth dialog "Pixel Buds Pro 2 va · Actief · 100 %", "Niro · Opgeslagen" (s31).
- **17:23:04.162 Bluetooth off** (s32–36 "Bluetooth staat uit"; H1L 1096 LE, 1097 ACL `0x16`); **17:23:08–10 on** (s37–38 "Verbinding maken…"; H1 145/151).
  H1L ends; H1 begins. **Not** a BK step — the maintainer's own Bluetooth cycle, as in set 2.
- **17:23:10** the app back via Recents (s41–42): the ADR-044 re-open on **channel 21** (Info: announcement 17:23:10), the snapshot claim #1 of H1 (`SABM` 460
  17:23:10.055 → `Notify` 484 `…00 20` → `DISC` 546 17:23:11.594, 1.54 s). Connection s43: "Connected to this phone (Android) · App control: ready", L 100 % ⚡ /
  Case 74 % / R 100 % ⚡, "Both buds in the case"; the Battery (i) s46 (Case 74 % at 17:23:10). AVRCP `Stopped` 17:23:11.6 (the only AVRCP status change of set 1).
- **17:23:22.9–25.9** the **ANC tab of 1.2.0** (s51: "The Buds don't allow changing noise control right now…", Off — Settable `00`): claim #2 (H1 615 `SABM`
  17:23:23.003 → `Notify` 637 `…00 20`), the tab left to Sound (s54, 17:23:25.9) → `DISC` 640 17:23:27.076 (the hold's 1.5-s release). Sound s54 (EQ, Volume EQ
  on), Controls s57 (touch on, both holds = Noise control, four modes ticked), Settings s59–61 (no card), **Info s62–149** (17:23:34–17:25:01, unchanged apart
  from the recorder badge — the maintainer runs `adb install -r` from the workstation meanwhile), Controls s151–152, Connection s153–159.
- **17:25:17.9–19.9 the update to 1.2.1** (Android's "Updaten…" overlay, s166–168); the app restarts: s168 "connecting…" (dimmed), **17:25:20.283** E1
  "LINK_BACK" re-open on **channel 21**; the thirteen requests H1 762–837 (`ReadSetting` 4:16, 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29), `SubscribeRuntimeInfo`
  840, `GetHardwareInfo` 841 (answered 844 with the three serials), **byte-identical to set 2's 06:19:01 sequence (H2L 18253–18326) and to `CAP-073`'s channel-21
  bytes** (`CAP-075-FINDINGS.md` §2); the snapshot claim #3 (`SABM` 782 17:25:20.575 → `Notify` 803 `…00 20` → `DISC` 845 17:25:22.121). s169 ready, Case 74 %.
  `PAIR-003`, `BATT-004`, `FW-003`.
- **17:25:24.6–28.4 H1 (hold 1, "Show a hint" — the default after the update):** the ANC tab (s173–176, still "The Buds don't allow…", Off, Settable `00`): claim
  #4 (`SABM` 868 17:25:24.563, `Notify` 892 `…00 20`), E1 "hold started: the noise-control tab is on screen (ADR-061)" 17:25:24.617; the tab left (Sound s177)
  → "hold ended: the noise-control tab was left" 17:25:28.390 → `DISC` 954 17:25:29.897 (1.51 s). No bud was worn: no press-and-hold possible.
- 17:25:29–17:26:03 Sound, Controls (s178–179), **Settings s181: the "Noise-control channel" card, "Show a hint, do not take it back" selected** (the default on
  a fresh update — BK-15 ✓), Debug on / Unidentified (13) (s185), **Info s187–189: "App: 1.2.1, build 31ee183 (2026-10-12)"**, announcement 17:25:20, channel 21,
  serials 17:25:21; Settings s192–207; Controls s208; Connection s209 (17:25:40.9): L 100 % / Case 73 % / R 100 % without ⚡, **"Not worn (checked 17:25:24)"** —
  the buds are out of the case; **17:26:03.76 "Refresh battery"** (s212: "Probably worn (checked 17:26:04)"): claim #5 (`SABM` 1111 17:26:03.977, `Notify` 1129
  `…e8 e8 80` — **Transparency, Settable `e8`: both buds worn** from here; `DISC` 1159 17:26:05.612).
- **17:26:06.8–19.5 H2 (hold 2, hint only; 4 press-and-holds):** the ANC tab s214–215 (Transparency): claim #6 (`SABM` 1192 17:26:06.611, `Notify` 1212 `…80`),
  "hold started" 17:26:06.795; the unprovoked `Notify` 1242 `08` 17:26:11.349 (NC on screen s220, ≤ 1 s), 1245 `80` 13.613 (s222), 1246 `08` 15.480 (s224), 1249
  `80` 17.640 (s226); the volume panel overlay s219 (a volume press on the phone or a bud — no AVRCP volume frame: not resolved); the tab left (Controls s228–229)
  → "hold ended: the noise-control tab was left" 17:26:19.507 → `DISC` 1268 17:26:21.012. Cue bursts in the audio 17:26:12.28, 12.99, 16.43 (0.9–1.0 s after
  the `Notify`). `TOUCH-007`.
- **17:26:24.5–56.4 H3 (hold 3, hint only; 9 holds):** Connection s231 ("Probably worn (checked 17:26:17)"), the ANC tab s232: claim #7 (`SABM` 1289
  17:26:24.270, `Notify` 1308 `…08`), "hold started" 17:26:24.501; the holds 1323 `20` 26.375 (Off, s235), 1350 `40` 30.592 (s239), 1362 `80` 36.195 (s244),
  1365 `08` 38.991 (s247), 1366 `20` 42.107 (s250), 1369 `40` 45.550 (s254), 1370 `80` 50.354 (s259), 1373 `08` 53.278 (s261), 1374 `80` 55.484 (s264) — **9 of
  9 on screen within a second**; the tab left (Settings s265, 17:26:56.9) → "hold ended … left" 17:26:56.389 → `DISC` 1389 17:26:57.894. `TOUCH-007`, `ANC-001`…`004`
  (every mode shown from a `Notify`).
- **≈ 17:26:58 the switch to "Take it back while the noise-control tab is open (may fight that app)"** (Settings s267, 17:26:58.9 — the first time the row is
  selected; s265 still showed "Show a hint"). BK-17's first half.
- **17:27:04.9–38.99 H4 (hold 4, "Take it back"; 12 holds):** the ANC tab s273 (NC): claim #8 (`SABM` 1442 17:27:04.775, `Notify` 1462 `…08`), "hold started"
  17:27:04.920; the holds 1489 `20` 09.195 (s277), 1492 `40` 12.676 (s281), 1505 `80` 15.767 (s284), 1507 `08` 18.598 (s287), 1510 `80` 20.659 (s289), 1512 `08`
  22.590 (s291), 1515 `80` 23.690 (s292), 1528 `08` 26.075 (s294), 1529 `80` 27.634 (s296), 1532 `08` 29.674 (s298–299), 1533 `80` 31.617 (s300), 1548 `08` 37.196
  (s305) — **12 of 12 on screen within a second; no `DISC` for 35.7 s; no re-claim, no hint** (nothing closed the claim). **The ANC (i) open s302–305
  (17:27:33.9–36.9):** "ANC mode: TRANSPARENT (updated 17:27:31)", **"Changed by the Buds at 17:27:31 (a press-and-hold on a bud, or the Buds' own change)."**,
  "Connection: Ready", the ADR-062-corrected closing text ("… loses it to this app and may take it back about 5 seconds later; what the app does then is set on
  the Settings tab under Noise-control channel."), **no hint line**; at s305 the dialog re-rendered live to "ANC mode: ACTIVE (updated 17:27:37)" / "Changed by
  the Buds at 17:27:37" (`Notify` 1548). The tab left (Settings s307) → "hold ended … left" 17:27:38.991 → `DISC` 1551 17:27:40.496. `TOUCH-007`, `ANC-001`…`004`.
- **≈ 17:27:39.5 the switch back to "Show a hint"** (s308, 17:27:39.9). **17:27:42.4–17:28:16.4 H5 (hold 5, hint only; 12 holds):** the ANC tab s310: claim #9
  (`SABM` 1572 17:27:42.237, `Notify` 1592 `…08`), the holds 1595 `20` 43.640 (s312), 1621 `40` 46.572 (s315), 1622 `80` 49.267 (s317), 1625 `08` 52.237 (s320),
  1626 `80` 54.273 (s322), 1629 `08` 55.187 (s323), 1630 `20` 58.915 (s327), 1633 `40` 17:28:01.557 (s330), 1634 `80` 04.523 (s333), 1648 `08` 07.783 (s336),
  1651 `20` 11.217 (s339), 1652 `40` 14.332 (s343) — 12 of 12 shown; the tab left (Settings s345) → "hold ended … left" 17:28:16.352 → `DISC` 1670 17:28:17.857.
- **≈ 17:28:19 the third switch, to "Take it back"** (s348, 17:28:19.9; it stays there to the end of set 1 — BK-end2's "back to Show a hint" **not done** in set 1
  either; the secondary user's stored choice is "Take it back"). **17:28:22.7–43.7 H6 (hold 6, take it back; 4 holds):** the ANC tab s350 (Adaptive): claim #10
  (`SABM` 1724 17:28:22.537, `Notify` 1742 `…40`), the holds 1747 `80` 24.530 (s353), 1774 `08` 27.571 (s356), 1775 `20` 30.947 (s359), 1778 `40` 34.289 (s362)
  — 4 of 4 shown; **the notification shade s369–370 (17:28:40.9–41.9)**: the Wi-Fi name, "OpenControl … Connected — ANC: ADAPTIVE"; **the film ends 17:28:42.7**.
- **After the recording:** the app is no longer visible → "hold ended: the app is not visible" 17:28:43.653 → `DISC` 1802 17:28:45.159 (1.51 s; the last app
  frame of set 1). **17:28:47.096 the Buds close DLCI 0x02** (1805) → E1 "RFCOMM channel 0x02 lost (read return -1)" 17:28:47.101 (the last inbound frame on
  0x04 had been 4 376 ms earlier); `AT+BIEV=2,100` 1807; the ACL ends `0x13` 17:28:47.531 (1814). The app logcat ends 17:29:58; E1's last line 17:31:53.825
  ("Session loss cause: the loss happened while the app was not visible; on return Android's link was down") and the export is written 17:31:58 (the file name).
  **The trigger of the Buds' close is not on film** (the recording had stopped) — OPEN QUESTION (`CAP-075-FINDINGS.md` §12).

### Between the sets — the Owner user's unfilmed evening session (2026-10-12 18:13–18:14, H2L frames 250–2 4xx; no export, no film)

The rotated HCI log of set 2 begins at 18:13:13 on 2026-10-12 with an Owner-user session of 1.2.1 that has no film and no export (the export of set 2 covers
process 17838 only, from 06:18:58): two ACLs (18:13:17.3 → 18:14:11 `0x13`, 18:14:11 → 18:14:12 `0x13`) with **9 claims, alternating app / Play**: app #1 18:13:17.451
(snapshot, 1.56 s), app #2 19.396 (0.10 s), Play #3 21.660–30.979, app #4 31.621–36.254 (4.63 s), Play #5 38.657–50.117 (holds `40` 39.700, `80` 41.434, `08`
44.805 on Play's claim), app #6 50.739–55.597 (4.86 s; holds `20` 53.258, `40` 54.873 on the app's claim), Play #7 58.082–58.818, app #8 59.472–18:14:04.043
(4.57 s; `08` 02.595), Play #9 06.586–10.441 (`00 20` 09.414: the buds back in the case), then the Buds end the ACL. It shows the same ≈ 4.6–4.9-s app claims
and the Play alternation as set 2's film and is **not** analysed further (no screen, no export) — listed so no frame of the delivered logs is unaccounted for.

### Set 2 — 2026-10-13, the Owner user with Google Play (the app from 06:18:58; film 2: 06:29:04.15–06:34:32.0)

- **Before the recording.** 06:18:58.248 the app process 17838 starts in user 0 (L/X `am_proc_start`); E2 06:18:58.764 "start", 06:18:59.018 the Android link
  observer: NOT_CONNECTED. **06:19:00.638 Connect** (no "automatic re-open" line: a tap — not on film): the phone pages the Buds (H2L 18117 06:19:00.677; the ACL
  18119 06:19:00.784), **channel 21**, the thirteen requests + `GetHardwareInfo` (H2L 18253–18326, byte-identical to set 1's 17:25:20 sequence), the snapshot
  claim #10 of H2L (`SABM` 18257 06:19:01.110, attempt 1 OK — the channel was free; `Notify` 18288 `…e8 00 20`: Off, **in the case**). **06:19:02.593 "hold
  started"** (the ANC tab was opened within 1.5 s: the snapshot claim became the hold — BK-4's path) → **06:19:03.666 the phone's `DISC`** (18576; E2 03.674
  "closed (IOException: bt socket closed, read return: -1)" + 03.675 "hold ended: the channel was closed, not by this app") → **06:19:04.522 "Message Stream
  re-claim cancelled (ADR-062)"**: the switch was already on "Take it back" (set before the film — consistent with film 2 s41–52 showing it selected without a
  change on film) and the tab was left or the app hidden within 0.85 s of the cut (which of the two, the line does not say; the film had not started). This is
  the run's **only** exercise of the cancel path (BK-11/12/13's family). The stack's lines of this cut are **not** in either system-log file (L's main buffer
  starts 06:26:54; X's 04:34:02 UTC). Play's claim #11 (`SABM` 18720 06:19:05.727, 2.06 s after the cut) **holds the channel 583 s** (to 06:28:49.009); the app
  stays open in the background on the Connection tab (E2: the runtime-info stream packets 06:20:09 … 06:28:40, the Case 71 % → 69 %; the link observer stopped
  06:24:10 / started 06:26:54 / stopped 06:26:58 — the app hidden and shown).
- **06:28:49.008/.009 the Buds close DLCI 0x02 and 0x04** (H2L 21490/21491): E2 "channel 0x02 lost" 06:28:49.051 (the last inbound 8 496 ms earlier), L
  `cleanup_rfc_slot … scn: 1, app_uid: 10354` + `scn: 2, app_uid: 10205` at 06:28:49.021/.025 (both clients' sockets), Play's "Esdc … connected=false" /
  "disable ANC when event stream is temporarily disconnected"; `AT+BIEV=2,100` 21495. **Play's claim #12** `SABM` 21544 06:28:54.632 (**5.62 s** after its loss —
  the same timer as the cuts below), `Notify` 21568 `…e8 e8 40` (**Adaptive, both buds worn** — the buds came out of the case between 06:19 and 06:28:55);
  `DISC` 21797 06:29:13.401 at the Bluetooth off.
- **Film 2 starts 06:29:04.15** on the Owner's launcher (s0); the shade s3–5 (the Bluetooth tile; "Google Play-services needs to sho… · 2 d"; "Verbinding met
  Always-on VPN…"); the Bluetooth dialog s6–7 ("Pixel Buds Pro 2 va · Actief · Batterijniveau 100 %", "Niro"); **06:29:13 Bluetooth off** (s9–14; H2L 21817 LE,
  21818 ACL `0x16`; L TURNING_OFF 06:29:12.990); **on** s15–18 ("Verbinding maken…"; H2 163 `Create Connection` 06:29:19.081, the ACL 469 06:29:21.136; the
  Bluetooth process restarts as 22714 at 06:29:17.570). Quick Settings s19–20, the launcher s21.
- **06:29:24.0–24.2 two other clients of the new ACL:** Play's claim #1 of H2 (`SABM` 858 06:29:24.044, `03 08 00 02 01 25` 910, `08 11 00 00` 957, `Notify`
  969 `…40`, its ACKs) and **the Google app (uid 10302, `com.google.android.googlequicksearchbox`) on DLCI 0x0a and 0x08** (H2 `SABM` 884 06:29:24.056 and 966
  06:29:24.158; L `on_cli_rfc_connect … scn: 5, app_uid: 10302` 06:29:24.078 and `scn: 4` 06:29:24.216; 4 + 59 frames, the `[Group][Code][Length][Value]`
  envelope of `PROTOCOL.md` §2.3's third channel, closed 06:30:43.47/.49 — `cleanup_rfc_slot … scn: 4/5, app_uid: 10302`). **Not this app**, and not Play: the
  DLCI 0x08 traffic the skeleton asked to flag belongs to the Google app in the Owner user (🟢 FACT for the uid; its content is not analysed here —
  `CAP-075-FINDINGS.md` §12).
- **06:29:27.2 the app via Recents** (s23: Info with the old announcement 06:19:01 / channel 21): E2 06:29:27.642 "Session loss cause: the loss happened while
  the app was not visible; on return Android showed the Buds connected", **06:29:27.645 the LINK_BACK re-open on channel 19** (s24: announcement 06:29:28,
  "Serial numbers: Not read from the Buds yet"; s26 the serials at 06:29:30); the thirteen requests H2 1193–1293 + `SubscribeRuntimeInfo` 1301 +
  `GetHardwareInfo` 1302, **byte-identical to `CAP-074-FINDINGS.md` §2's channel-19 bytes** (B74 15758 … 15846); **the snapshot claim: attempt 1 fails after
  433 ms** (E2 06:29:28.836; L 06:29:28.826 `RFCOMM_CreateConnectionWithSecurity: already at opened state 2 … scn=2` — Play's #1 was on the channel; Play's `DISC`
  1213 06:29:28.829 **by the phone**: the collision killed Play's socket), **attempt 2 connects** (claim #2 `SABM` 1283 06:29:29.910; E2 "connected (attempt
  2/3)" 06:29:30.011; `Notify` 1317 `…e8 e8 40` Adaptive; released `DISC` 1330 06:29:31.982 — the Info tab is shown). Play's claim #3 `SABM` 1366 06:29:34.491
  (**5.66 s** after its loss) holds the channel 39 s. `PAIR-003`, `BATT-004`, `FW-003` (s24–36: "App: 1.2.1, build 31ee183 (2026-10-12)", `release_5.203` ×3, the
  serials).
- 06:29:41–06:30:12 Debug on / "Unidentified frames (11)" (s37–39); **Settings s41–52 (06:29:45–56): "Take it back while the noise-control tab is open" already
  selected** (no change on film — the Owner's stored choice from before 06:19); Controls s54–56 (touch on, both holds Noise control, four ticked); Connection s59
  (06:30:03: "Connected to this phone (Android) · ready", L 100 % / Case 69 % / R 100 %, "Probably worn (checked 06:29:30)"); the Battery (i) s64–65 (Case 69 %
  "last seen 06:28:40"); Connection s68.
- **06:30:13.2 the ANC tab (s69, Adaptive) — "Take it back", chain 1 (BK-9's path):** the tab's claim: **attempt 1 fails after 450 ms** (E2 06:30:13.733; L
  "already at opened state" 06:30:13.733; Play's #3 killed: `DISC` 2273 06:30:13.734), attempt 2 = **claim #4** (`SABM` 2298 06:30:14.565, `Notify` 2319 `…40`
  06:30:14.979), "hold started" 06:30:14.984. The ANC (i) s73–79 (06:30:17–23): "ANC mode: ADAPTIVE (updated 06:30:14)", "Connection: Ready", the ADR-062 text,
  **no hint line** (correct: nothing had been taken yet in this visit), **no "Changed by the Buds" line** (the mode came from the claim's own read).
  **The cut:** the phone's `DISC` 2369 **06:30:19.281** (E2 06:30:19.321 "closed … read return: -1" + 06:30:19.322 "hold ended: the channel was closed, not by this
  app"; **L 110190–110204: `bt_port_api … RFCOMM_CreateConnectionWithSecurity: already at opened state 2, RFC_state=4, MCB_state=5 … scn=2` 06:30:19.280 →
  `cleanup_rfc_slot … scn: 2, app_uid: 10205` 06:30:19.280 → `NearbyDiscovery: RfcommEventStreamMedium … Failed to read from socket. Retry times remaining 5`
  06:30:19.283 → `cleanup_rfc_slot … scn: 2, app_uid: 10354` 06:30:19.319** — Play's recovery connect, refused by the stack as "already at opened", which then
  closes both sockets: `CAP-074-FINDINGS.md` §8 (a)'s 🟡 path, observed) — **5.547 s after Play's loss**, 4.716 s after the app's `SABM`.
  → Play's retry #5 `SABM` 2401 **06:30:21.799 (2.518 s after the cut)** → **E2 06:30:22.330 "Message Stream re-claim 1/3 after a foreign close, 3000 ms later
  (ADR-062)"** → attempt 1 fails after 537 ms (Play's `DISC` 2476 06:30:22.867 by the phone) → attempt 2 = **claim #6** (`SABM` 2502 06:30:23.760, `Notify` 2524
  `…40`), "hold started" 06:30:24.119 → the cut `DISC` 2570 06:30:28.509 (5.642 s after Play's loss) → Play #7 2606 06:30:31.078 (2.569 s) → **"re-claim 2/3 …
  6000 ms"** 06:30:34.580 → fails 416 ms (Play's `DISC` 2694 34.996) → **claim #8** `SABM` 2716 06:30:35.717, `Notify` `…40` → the cut 2770 06:30:40.535 (5.539 s)
  → Play #9 2805 06:30:43.055 (2.520 s) — **on Play's claim:** its `Get` answered `80` (2832: a hold in the gap 06:30:40.5–43.1, Transparency), then `08` 2878
  45.007, `80` 2891 47.619, `08` 2907 50.006 — **none on screen** (Adaptive s80–s109, 06:30:24–53); Play's "onReceiveEventStreamResponse" 06:30:45.012 / 47.625
  / 50.012 in L. → **"re-claim 3/3 … 12000 ms"** 06:30:52.718 → fails 461 ms (Play's `DISC` 2938 53.181) → **claim #10** `SABM` 2960 06:30:54.026, `Notify` 2980
  `…80` 06:30:54.437 (**changed** — Transparency on screen s110–111, ≤ 1 s), **the unprovoked `Notify` 3006 `08` 06:30:58.178 → NC on screen s114 (≤ 1 s)** →
  the cut 3030 06:30:58.742 (5.561 s) → **E2 06:30:58.971 "Message Stream re-claim stopped after 3 re-claims — a tap, a pull or re-entering the noise-control
  tab claims again (ADR-062)"** → **the hint line on film s115 (06:30:59.2): "The noise-control channel was taken by another app at 06:30:58 (for example Google
  Play services' Fast Pair); the app took it back 3 times and stopped. A change made on a bud is not shown until you tap a mode, Refresh, or pull down."**
  → Play #11 3071 06:31:01.330 (2.588 s) holds 23.6 s: its `Get` answered `80` (3103 — a hold in the gap, Transparency), then `08` 3155 06:31:05.986, `20` 3158
  06:31:06.239 — **none shown** (NC s116–129). Cue bursts 06:30:59.21 (1.03 s after `08`). `TOUCH-007`, `ANC-001`…`004`.
- **06:31:16–21 Settings (s132), "Show a hint, do not take it back" selected at ≈ 06:31:20.5** (s137) — BK-13's switch, but not within 2 s of a cut (no re-claim
  was pending: the chain had stopped). **06:31:24.2 the ANC tab** (s140: the hint line of 06:30:58 still shown + NC): the re-entry's claim — attempt 1 fails
  after 777 ms (E2 06:31:24.949; Play's #11 `DISC` 3273 06:31:24.948), **claim #12** `SABM` 3297 06:31:25.871, `Notify` 3321 `…20` (Off — the hold of 06:31:06,
  read now); **the tab left at once** (Connection s141, 06:31:25: "Probably worn (checked 06:30:58)" → s142 "(checked 06:31:26)") — no "hold started" line
  (the tab was left before the claim's answer); **06:31:26.627 Disconnect** (s143 "not open yet — tap Connect"): the app's `DISC` 3325 06:31:26.633 and of
  DLCI 0x02. **06:31:28.158 Connect** (s144 "connecting…"; channel 19; the thirteen requests): **the snapshot claim connects at attempt 1** (claim #13 `SABM`
  3392 06:31:29.508 — the channel was free: Play's socket had died at 06:31:24.948 and its recovery timer was running; `Notify` 3423 `…20`; s145 ready, s146
  "Probably worn (checked 06:31:29)") → **cut by Play's recovery 06:31:30.508** (`DISC` 3463; E2 "closed not by this app" 06:31:30.576; **5.560 s after Play's
  loss** — the Connection tab was shown, so no hold, no hint, no re-claim: the snapshot claim was simply gone; BK-14's path with the tab not shown). → Play's
  retry collides with the next app claim: **06:31:31.2 the ANC tab** (s147, Off) → claim #14 `SABM` 3514 06:31:32.006 (attempt 1 OK: the channel was free),
  `Notify` 3538 `…20`, "hold started" 06:31:32.671 → **the cut 06:31:33.021** (`DISC` 3558 — Play's retry, **2.513 s after 30.508**; L 06:31:33.023 "Failed to
  read from socket. **Retry times remaining 4**" — the only decrement of Play's counter in the run: its second loss within 2.5 s) → **E2 06:31:33.096 "hold ended
  … not by this app" + 06:31:33.097 "Noise-control channel taken by another client: the hint is shown, no re-claim (ADR-062, hint only)"** → **the hint on film
  s149 (06:31:33.2): "The noise-control channel was taken by another app at 06:31:33 (for example Google Play services' Fast Pair). A change made on a bud is not
  shown until you tap a mode, Refresh, or pull down."** (no "took it back" clause — BK-5's hint ✓). **The hold lived 0.42 s** (hint only gives no control over
  the phase of Play's timers). Play #15 `SABM` 3618 **06:31:37.580 (4.559 s after the cut — the one retry outside 2.2–2.8 s, after the decrement)** holds 19.2 s:
  its `Get` answered `40` (3646: a hold in the gap 33.0–37.6), then `80` 3688 39.801, `08` 3711 43.527, `80` 3731 45.847, `08` 3801 51.482 — **none shown** (Off
  s150–159; the volume panel s151). Cue 06:31:33.72. BK-5/BK-6's family, `TOUCH-007`.
- **06:31:50–55 Disconnect / Connect (BK-14, hint only):** Connection s166 ("Probably worn (checked 06:31:32)"), **Disconnect 06:31:51.824** (s168–169; the app's
  `DISC` of 0x02), **Connect 06:31:55.535** (s171 "connecting…"; channel 19; the thirteen requests): the snapshot claim — attempt 1 fails 408 ms (Play's #15
  killed: `DISC` 3879 06:31:56.779), attempt 2 = claim #16 `SABM` 3924 06:31:57.639, `Notify` 3964 `…08` (NC — the hold of 06:31:51.48, read now), released
  `DISC` 3992 06:31:59.647 (the Connection tab: s172 ready, s174 "Probably worn (checked 06:31:58)"). **The hint was gone at the Connect** (s171–176) ✓.
  **06:32:00.2 the ANC tab** (s176, NC): claim #17 `SABM` 4025 06:32:00.579 (attempt 1 OK — Play's recovery timer running), `Notify` 4046 `…08`, "hold started"
  06:32:01.072 → **the cut 06:32:02.199** (`DISC` 4083 — Play's recovery, **5.420 s after its loss** at 56.779) → "hint only" 06:32:02.276 → **the hint s178
  (06:32:02.2): "… taken by another app at 06:32:02 …"** (the hold lived 1.2 s). Play #18 `SABM` 4122 06:32:04.651 (2.452 s) holds 27.5 s: its `Get` answered
  `08` (unchanged), then `20` 4181 05.495, `40` 4203 08.816, `80` 4265 19.143, `08` 4289 22.074 — **none shown** (NC s179–199; the volume panel s179). Cue
  segments 06:32:06.1 … `PAIR-003`, `TOUCH-007`.
- **06:32:24–27 Settings (s200), "Take it back" selected at ≈ 06:32:26.5** (s202–203) — **the last switch of the run; never set back** (BK-end2 not done:
  the Owner user's stored choice is "Take it back"). **06:32:31 the ANC tab** (s207: the 06:32:02 hint still shown + NC): the re-entry's claim — attempt 1 fails
  981 ms (E2 06:32:32.136; Play's #18 `DISC` 4374 06:32:32.135), claim #19 `SABM` 4408 06:32:33.157, `Notify` 4434 `…08`; **the tab left** (Connection s209,
  06:32:33.2; s210 "checked 06:32:33") → released `DISC` 4446 06:32:35.246 (no hold line: the tab was left before the answer); **06:32:35.405 Disconnect**
  (s211). Play's recovery #20 `SABM` 4489 06:32:37.707 (5.572 s after 32.135).
- **06:32:38.747 Connect (s214; BK-3/BK-4's path under "Take it back"):** the thirteen requests; the snapshot claim — attempt 1 fails 501 ms (Play's #20 killed:
  `DISC` 4600 06:32:39.714), attempt 2 = **claim #21** `SABM` 4639 06:32:40.490, `Notify` 4673 `…08` 06:32:41.002 (s215 ready, s217 "Probably worn (checked
  06:32:41)"); **the ANC tab within 1.5 s (s218, 06:32:42.2) → "hold started" 06:32:41.966 on the snapshot claim** (BK-4 ✓: no second `SABM`) → **the cut 4738
  06:32:45.240** (5.526 s after Play's loss) → **chain 2:** Play #22 4768 47.774 (2.534 s) → "re-claim 1/3 … 3000 ms" 06:32:48.332 → fails 792 ms (Play's `DISC`
  4842 49.124) → **claim #23** `SABM` 4865 06:32:50.090, `Notify` 4890 `…20` 50.775 (Off — a hold in the gap 45.2–47.8, seen by Play's `Get` 4792 first) **and
  the unprovoked 4893 `40` 06:32:50.814 → Adaptive on screen s227 (≤ 1 s; the `20` was never drawn — overdrawn after 39 ms)** → the cut 4924 54.728 (5.604 s) →
  Play #24 4957 57.545 (2.817 s): `Get` → `40` 4984 (unchanged), **`80` 4997 06:32:58.192 — a hold on Play's claim, not shown** (Adaptive s229–236) →
  "re-claim 2/3 … 6000 ms" 06:33:00.865 → fails 473 ms (Play's `DISC` 5053 01.337) → **claim #25** `SABM` 5080 06:33:02.235, `Notify` 5102 `…08` 02.775 (**NC —
  changed in the gap 01.3–02.2; on screen s239, ≤ 1 s**) → the cut 5147 07.158 (**5.821 s**, the longest) → Play #26 5188 09.818 (2.660 s): `Get` → `20` 5218
  (a hold in the gap 07.2–09.8), **`40` 5266 14.693 (a hold on Play's claim)** — neither shown (NC s249) → "re-claim 3/3 … 12000 ms" 06:33:19.267 → fails 519 ms
  (Play's `DISC` 5314 19.786) → **claim #27** `SABM` 5339 06:33:20.569, `Notify` 5361 `…80` 21.189 (**Transparency — changed in the gap 19.8–20.6; on screen
  s257, ≤ 1 s**; a duplicate `80` 5364 21.254) → the cut 5398 25.350 (5.564 s) → **"stopped after 3 re-claims" 06:33:25.419 → the hint s261 (06:33:25.2):
  "… taken by another app at 06:33:25 …; the app took it back 3 times and stopped. …"** → Play #28 5433 27.910 (2.560 s): `Get` → `80` (unchanged; no hold
  while it held). **The ANC (i) s265–269 (06:33:29–33):** "ANC mode: TRANSPARENT (updated 06:33:21)", **the hint line**, "Connection: Ready", the ADR-062 text;
  **no "Changed by the Buds" line** (the mode came from the re-claim's own read, not from an unprovoked `Notify` — the (i) labels it as read; see
  `CAP-075-FINDINGS.md` §4 on what that hides). `PAIR-003`, `TOUCH-007`, `ANC-001`…`004`.
- **06:33:37.2 "Refresh" tapped (s272; BK-7/BK-10's path):** the claim — attempt 1 fails 1 053 ms (E2 06:33:37.597; Play's #28 `DISC` 5544 37.595), **claim
  #29** `SABM` 5571 06:33:38.601, `Notify` 5591 `…80` 39.084 (unchanged), "hold started" 39.101 → **the hint gone (s275, 06:33:39.2)** ✓ → **the unprovoked
  5606 `08` 06:33:42.323 → NC on screen s278 (≤ 1 s)** → the cut 5625 43.272 (5.677 s) → **chain 3** (a new chain after the user's claim — BK-10 ✓): Play #30
  5666 45.853 (2.581 s): `Get` → `20` 5692 (Off — a hold in the gap 43.3–45.9) → "re-claim 1/3 … 3000 ms" 06:33:46.454 → fails 699 ms (Play's `DISC` 5737 47.154)
  → **claim #31** `SABM` 5762 06:33:48.118, `Notify` 5782 `…20` 48.550 (**Off on screen s284 — the gap hold, shown 2.7–5.3 s after it happened, by the re-claim's
  read**) → the cut 5818 52.672 (5.518 s) → Play #32 5856 55.344 (2.672 s): `Get` → `40` 5880 (Adaptive — a hold in the gap 52.7–55.3) → "re-claim 2/3 … 6000 ms"
  06:33:58.749 → fails 468 ms (Play's `DISC` 5934 59.218) → **claim #33** `SABM` 5957 06:34:00.132, `Notify` 5980 `…40` 00.797 (the gap hold) **and the
  unprovoked 5983 `80` 06:34:00.862 → Transparency on screen s297 (≤ 1 s; the `40` overdrawn after 65 ms)** → the cut 6020 04.680 (5.462 s) → Play #34 6056
  07.143 (2.463 s): `Get` → `80` (unchanged), **`08` 6166 06:34:12.656 — a hold on Play's claim, not shown** (Transparency s299–309); **06:34:11.67 AVRCP
  `Playing`** (Spotify, a podcast — on the Buds; AVDTP start 06:34:11.84; the maintainer's playback for the last holds; the shade s326 shows the card) →
  "re-claim 3/3 … 12000 ms" 06:34:16.741 → fails 261 ms (Play's `DISC` 6211 17.001) → **claim #35** `SABM` 6235 06:34:17.730, `Notify` 6257 `…08` 17.958 (the
  hold of 12.66, read 5.3 s late) **and the unprovoked 6260 `20` 06:34:18.265 → Off on screen s314 (≤ 1 s)** → the cut 6309 22.328 (5.327 s) → **"stopped after
  3 re-claims" 06:34:22.379 → the hint s318 (06:34:22.2): "… taken by another app at 06:34:22 …; the app took it back 3 times and stopped. …"** → Play #36
  `SABM` 6341 06:34:24.554 (2.226 s) holds 64 s (no hold while it held: only ACKs 6473, 6516). Cue segments 06:34:12.2–25.1 (long, during the playback).
  `TOUCH-007`, `ANC-001`…`004`.
- **06:34:30–31 the notification shade (s326–327):** Spotify "A good night's sleep" playing to "Pixel Buds Pro…" with the account's e-mail fragment; the
  recorder's "Stoppen" tapped; **the film ends 06:34:32.0**.
- **After the recording:** the link observer stopped 06:34:33.023 (the app hidden); AVDTP suspend 06:34:49.19, AVRCP `Paused` 06:34:46.2; **06:35:28.168** the app
  shown again (observer started): the ANC tab's claim — attempt 1 fails 777 ms (Play's #36 `DISC` 6746 06:35:28.929), **claim #37** `SABM` 6772 06:35:29.663,
  `Notify` 6792 `…20`, "hold started" 06:35:30.188 → "hold ended: the noise-control tab was left" 06:35:30.225 → released `DISC` 6805 06:35:31.731 (E2's last
  line); Play #38 `SABM` 6833 06:35:34.601 — **open at the end of the log**; "Debug log exported (513 lines)" 06:35:38 (app logcat 2, UTC 04:35:38); `bugreportz`
  06:35:41 (the System log; the SIGQUIT in the app logcat); the HCI log ends 06:36.

## The maintainer's steps, as run (mapped to the skeleton's BK-1 … BK-21)

| Step | Done? | As run (evidence above) |
|---|---|---|
| P0 | ✗ shell / ✓ by the logs | no `am get-current-user` / `pm list packages` output saved; set 1's user is `full.secondary` (app logcat 1's header), set 2's is user 0 (`am_proc_start … [0,17838,10354,…]`); Play present in set 2 (uid 10205, 19 claims), absent in set 1 (0 Play markers). Play's *Nearby devices* permission and version: **not on film** |
| P1 | partly | the update over 1.2.0 on film 1 (17:25:17.9–19.9, no uninstall: the app's data kept); no `dumpsys` saved; **no APK pull** (Checkpoint 0) |
| P2–P5, P7–P9 | ✓ (P7 not verifiable) | HCI snoop on across both sets (the rotation at each Bluetooth off/on); screen recordings with sound; the status bar across minute changes in both films; buds charged (100 %/100 %); **P9 not kept in set 2** (the app was opened at 06:18:58, ten minutes before the film) and in set 1 the 1.2.0 app was open before the update by design |
| P6 | ✓ (set 2) | `bugreportz` at 06:35:41 (the main buffer from 04:34:02 UTC — too late for the cuts) **and** the GrapheneOS log viewer's export whose main buffer starts 06:26:54 and covers **all 15 cuts of the film** (the stack lines are present for 15 of 15) |
| BK-1 (Play: open the app) | ✓ differently | the session of 06:19:00 (a Connect tap; the thirteen requests; the snapshot claim at attempt 1 — Play was not on the channel yet) and the re-open of 06:29:27 (attempt 1 fails 433 ms, attempt 2 connects) |
| BK-2 (Play: Info, Settings card) | ✓ | film 2 s24–36 Info ("App: 1.2.1, build 31ee183 (2026-10-12)", `release_5.203` ×3, the serials); the card on s41–52 — **"Take it back" already selected** (not the fresh default: set before the film) |
| BK-3 (Disconnect / Connect) | ✓ ×3 | 06:31:26.6/06:31:28.2, 06:31:51.8/06:31:55.5, 06:32:35.4/06:32:38.7 |
| BK-4 (the ANC tab within 1.5 s of ready) | ✓ ×2 | 06:19:02.593 and 06:32:41.966: "hold started" on the snapshot claim, no second `SABM` |
| BK-5 (hint only: holds every ≈ 5 s for 30 s) | ✓ differently | hint only was selected 06:31:20.5–06:32:26.5; the holds in that window (9) fell on Play's claims #15/#18 or in gaps; the app's own claims #14 and #17 lived 0.42 s and 1.2 s; the hint shown at 06:31:33 and 06:32:02 with the cut's time; no re-claim (0 "re-claim" lines between 06:30:59 and 06:32:48); P6 done by the log viewer |
| BK-6 (the (i) with the hint; a tap clears it) | partly | the (i) with the hint line and the corrected sentence: s265–269 (under "Take it back", after a stopped chain); **no mode tap in the run** (0 `Set` frames); the hint cleared by **Refresh** (06:33:37) and by a **Connect** (06:31:55) |
| BK-7 (pull-down clears the hint) | ✓ by the Refresh button | 06:33:37.2 (s272) — a tap on "Refresh", not a pull-down gesture |
| BK-8 (switch to "Take it back") | ✓ | 06:32:26.5 (s202–203); on return a new claim (#19, 06:32:33) |
| BK-9 (take it back: holds for 60 s) | ✓ ×3 chains | chains at 06:30:14–59, 06:32:42–06:33:25, 06:33:38–06:34:22 — exactly three re-claims each at 3 000 / 6 000 / 12 000 ms, the stop and the hint with "3 times" each time |
| BK-10 (a tap restarts the chain) | ✓ by Refresh | 06:33:37.2 → chain 3 |
| BK-11 / BK-12 / BK-13 (a tab change / Home / the switch within 2 s of a cut) | ✗ | only the pre-film "re-claim cancelled" of 06:19:04.522 (the tab left or the app hidden 0.85 s after a cut; not on film). The switches on film were made with no re-claim pending |
| BK-14 (hint only: Disconnect / Connect on the ANC tab) | ✓ differently | 06:31:51/55 with the **Connection** tab shown (the snapshot released after 2.0 s, not held); the hint gone at the Connect ✓; the ANC tab at 06:32:00 → the hold cut 1.2 s later → the hint |
| BK-end1 | ✓ | the export 06:35:33 (513 lines), `bugreportz` 06:35:41, the status bar across minutes; the switch to the other user was the evening before (set 1 came first) |
| BK-15 (no Play: open the app; Info; the card) | ✓ | film 1 s168–189 (the LINK_BACK re-open after the update; Info; the card with "Show a hint" — the default on the update) |
| BK-16 (hint only: the ANC tab, holds for 30 s) | ✓ ×3 | H2 (4 holds), H3 (9), H5 (12): every hold shown, **no `DISC` while the tab was shown**, no hint, 0 Play claims |
| BK-17 ("Take it back"; holds for 30 s) | ✓ ×2 | the switch 17:26:58; H4 (12 holds, 34 s, no `DISC`), H6 (4 holds, 21 s) — no re-claim line, no extra `SABM`, no hint |
| BK-18 (leave the tab) | ✓ ×5 | every hold ended "the noise-control tab was left" → the phone's `DISC` 1.50–1.51 s later (H1 … H5); H6 "the app is not visible" → 1.51 s |
| BK-19 / BK-20 / BK-21 (optional) | ✗ | not run in either set |
| BK-end2 | partly | the export 17:31:58 (228 lines); **the switch not set back** to "Show a hint" (set 1 ends on "Take it back" since 17:28:19); no APK pull |

## Test-IDs (`AGENTS.md` §13 step 7; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`)

- `PAIR-003` — set 1: the Connect 17:22:44, the ADR-044 re-open 17:23:10, the re-open after the update 17:25:20 (three sessions, each with the thirteen requests
  and one `GetHardwareInfo` answered); set 2: the Connect 06:19:00, the LINK_BACK re-open 06:29:27, the Disconnect/Connect pairs 06:31:26/28, 06:31:51/55,
  06:32:35/38 (five sessions, the same thirteen requests, byte-identical to `CAP-073`/`CAP-074`).
- `BATT-004` — "Battery updated" (`03 03 00 03 …`) on every claim's open in both sets (E1 lines 51–52 at 17:25:20.5; E2 55–56 at 06:19:03.6); the Case from the
  runtime-info stream (74 → 73 % set 1; 71 → 69 % set 2).
- `FW-003` — the Info tab on film in both users: `release_5.203` ×3, the serials with their labels, the 1.2.1 build line (film 1 s187–189; film 2 s24–36); the 1.2.0
  build line before the update (film 1 s0–4).
- `ANC-001`…`ANC-004` — **0 mode taps in the whole run** (0 `Set` frames, `data.data[0:2]==08:12` on DLCI 0x04 phone → Buds in all four logs, exit 0; positive
  control: 5 in `CAP-074-btsnoop_hci.log`); every mode shown from a `Notify` in both sets (set 1: all four modes repeatedly; set 2: Off, Adaptive, Transparency, NC).
  The `Set` → ACK path is **not exercised** by this run (it was by `CAP-074`).
- `TOUCH-007` — set 1: **41 mode changes the app did not request, 41 on the app's own claim, 41 on screen within a second**; set 2 (the film): **31 mode changes,
  8 on screen within a second (5 unprovoked `Notify` frames on the app's claims, 3 read by a re-claim's own `Get`), 2 shown 2.7–5.3 s late by a re-claim, 3 drawn
  for < 0.3 s or overdrawn, 2 only after a user action, 16 never** (16 `Notify` frames on Play's claims, 7 changes in the gaps seen only by Play's `Get`). The
  press-and-holds are the maintainer's statement (no camera; the cue segments 0.1–2.3 s after a `Notify`).
- **Expected but not observed:** `AUDIO-002` (no Volume EQ toggle), `CASE-004`/`CASE-005` (the buds' case moves are not on film: the Settable `00` → `e8` between
  17:25:24 and 17:26:04, and between 06:19:01 and 06:28:55, happened off screen); BK-11 … BK-13, BK-19 … BK-21.

## Analysis checklist

- [x] Every filter scoped to the Buds' handle `0x000b` (shown matching hci2.log 2298); the Play marker's positive control (`CAP-074` 22); DLCIs by content.
- [x] P0 by the logs (set 1 `full.secondary` + 0 Play; set 2 user 0 + Play uid 10205) — the shell outputs and the permission/version film **not** made; P1 the
      update on film, **no APK pull** (⚪ — Checkpoint 0); the Info frame's hash = B1's `31ee183` in both users.
- [x] The claim inventory of DLCI 0x04 with both clients (`CAP-075-FINDINGS.md` §1): 1 + 10 (set 1, all the app's), 12 + 38 (set 2: 6 + 19 the app's, 3 + 19 Play's),
      every `SABM`/`DISC` with its time and closer, every `Notify` attributed (§4).
- [x] Part 1 hint only: the cuts at Play's recovery **5.33–5.82 s after Play's loss** (not a fixed time after the app's `SABM` — 0.42 s and 1.2 s for #14 and #17);
      no app `SABM` after a cut without a tap, re-entry or Connect; the hint line with the cut's time (06:31:33, 06:32:02); cleared by the Connect and by Refresh;
      the (i)'s corrected sentence; the export's "hint is shown, no re-claim" lines ×2.
- [x] Part 1 take it back: three chains, re-claims at **3 000 / 6 000 / 12 000 ms** in the export (the wire's `SABM` ≈ 1.4–1.9 s later: attempt 1 refused, attempt
      2 ≈ 0.9 s after the refusal), each held 4.55–4.92 s, the fourth cut without a re-claim, the "stopped" line and the hint with "3 times" ×3; the new chain after
      Refresh; **Play's retry 2.23–2.82 s after each cut** (4.56 s once, after its counter decrement); BK-11 … BK-13 not run.
- [x] The system log: the stack path of `CAP-074-FINDINGS.md` §8 (a) on 15 of 15 cuts in the logcat-all file's reach (`already at opened state` → `cleanup_rfc_slot`
      uid 10205 → `RfcommEventStreamMedium … Failed to read from socket` → `cleanup_rfc_slot` uid 10354), and the app's own attempt-1 refusals as the same
      `already at opened state` line; Play's Hearable Controls lines at each loss and re-open.
- [x] Part 2: 0 Play claims (positive control set 2's 19 + 19); one claim per tab entry; no `DISC` while the tab was shown (H2 … H6, 14–36 s each); every hold
      shown; no re-claim, no hint, in both switch positions.
- [x] Every second of both films viewed or proved unchanged; both audio tracks tabulated; both exports and both app logcats read line by line; both system-log files
      swept with stated, positive-controlled filters (`CAP-075-FINDINGS.md` §0); `am_crash`/`am_anr` 0; the `bugreportz` SIGQUIT discounted.
- [x] Test-ID traceability above; "expected but not observed" stated.
- [x] Folder rename, file modes, the checksum file, the recordings' names (committed as is), the system-log extract — done per Checkpoint 0 of
      `ai-sessions/0088` (the maintainer's answers verbatim in `ai-sessions/0088_CAPTURE_RESULT_2026_10_12.md`); the APK pull not made (⚪).
- [x] `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `id_registry.csv`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `APP_TESTPLAN.md` §X, `TODO.md`, `ARCHITECTURE.md`; the ADR-062
      Update, the `PROTOCOL.md` §4.1 note and the release verdict ("no heavy issue: release 1.2.1 as built") — approved by the maintainer in chat 2026-10-13
      (`ai-sessions/0088`, Phase D) and written.

---

## Appendix — the planned procedure (the skeleton of `ai-sessions/0087`, 2026-10-12, as written; the reference frames of `CAP-074`/`CAP-073` are still valid)

**Not in this run** (say so if asked): the hold's other untested branches of `TODO.md` §2 (the tile on the tab, Home/return, a loss with the tab shown) — welcome
if time allows (BK-19 … BK-21, optional), not required for the verdict; the case-sound experiment; the wear sequences.

### Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-075` |
| Group(s) | BK (new) |
| Date | TBD |
| Phone | Pixel 9a, GrapheneOS (Android 17, `CP3A.261005.005` in `CAP-074`) — **part 1: the Owner user (user 0) with Google Play services** (`CAP-074`'s user); **part 2: the secondary user (user 10) without Google Play** (`CAP-073`'s user) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.2.1** (versionCode 10201), the release APK of `scripts/release.sh 1.2.1` (B1, 2026-10-12 17:17 — **build commit `31ee183`**, the merge commit of PR #39 on `main`), kept as `~/opencontrol-1.2.1-tested` (B3) — **Info tab on film in each user**: "App: 1.2.1, build 31ee183 (2026-10-12)", no "-dirty"; APK SHA-256 `a60a3cc9b2a80e9dc5fecb8810c9cb8e7cbb20fa9eb7a2a0ce663b89e964963e`, certificate SHA-256 `a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb` (= `README.md` / `SECURITY.md`; checked by `ai-sessions/0087` after the build); **the installed APK pulled** (P1) |
| Previous version in each user | 1.2.0 build `5b4d5db` (installed 2026-10-10; one APK serves both users, `CAP-073-FINDINGS.md` §0) — the update is from it (`adb install -r`, no uninstall) |
| Google Play services (part 1) | present and active (`com.google.android.gms`, uid 10205 in `CAP-074`); **its *Nearby devices* permission state on film** (P0 — `TODO.md` §3's standing item, never recorded so far) and its version (Settings → Apps → Google Play services, on film) |
| Official Pixel Buds app | not used (not installed in either user in `CAP-073`/`CAP-074`) |
| Buds | Pixel Buds Pro 2, firmware `release_5.203` expected (any other ⇒ Safe Mode: say so, stop) — both buds **worn** for every hold step (the Settable byte `e8`; a `00` means no bud worn and the taps would be refused, ADR-049) |
| Video files | TBD — the screen recording **with sound** for the whole run (the recorder's audio on: the maintainer says what they do, as in `CAP-074`); no camera needed (the holds are the maintainer's statement + the wire, as `CAP-073`/`CAP-074`) |
| Log files | TBD — the HCI snoop logs, the app's debug exports (one per part, Debug mode on), the app logcat, **the system log pulled within one minute of the first cut of part 1** (P6 — `CAP-074-FINDINGS.md` §10's first open question: the Bluetooth process's `RFCOMM_CreateConnectionWithSecurity: already at opened state` / `cleanup_rfc_slot` / `RfcommEventStreamMedium` lines are kept only ≈ 1–3 minutes), the P0/P1 outputs |

### Preparation

| # | Check | Done |
|---|---|---|
| P0 | **In each user**: `adb shell am get-current-user`; `adb shell pm list packages --user <id> \| grep -i -E "gms\|vending"; echo "exit=$?"` (part 1: Play listed, exit 0; part 2: nothing, exit 1); positive control `grep opencontrol` (exit 0). **Part 1 on film:** Settings → Apps → Google Play services → Permissions → *Nearby devices* (allowed / not allowed) and the app's version line. Save the outputs **with the exit statuses** | ☐ |
| P1 | In the installed 1.2.0 (`5b4d5db`): Dark mode and Debug mode as you like them, **Debug mode on** (on film). `adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds \| grep -E "firstInstallTime\|lastUpdateTime\|versionCode"` → save. `adb install -r ~/opencontrol-1.2.1-tested/opencontrol-pixelbudspro2-1.2.1.apk` (one APK serves both users) — **no uninstall**; the same `dumpsys` → save. After the run: `adb shell pm path --user <id> io.github.tedsluis.opencontrolpixelbuds` → `adb pull` → `sha256sum` against `~/opencontrol-1.2.1-tested` (`CAP-074` §0's ⚪: the installed APK's hash) | ☐ |
| P2 | HCI snoop log on (set in the Owner; it stays on across the user switch) | ☐ |
| P3 | No camera (both parts are screen recording + wire); the buds and the phone within reach | ☐ |
| P4 | Android's **screen recorder with sound** on for each part (one recording per user, or one across the switch) | ☐ |
| P5 | The status bar across a minute change at the start and at the end of each recording | ☐ |
| P6 | **The system log of part 1 within one minute of the first cut** (BK-5): `adb bugreportz` or the GrapheneOS log viewer right after BK-5's 30 s — the stack lines of the cut are retained only briefly (`CAP-074`: the `main` buffer reached back 75 s). A second system log at the end of the run. Each `adb bugreportz` sends signal 3 to every Java process — not an app fault (`CAP-071-FINDINGS.md` §0) | ☐ |
| P7 | **Do Not Disturb on** | ☐ |
| P8 | Buds charged, **both worn** before BK-4; Android shows them connected before the app is opened | ☐ |
| P9 | **No app use before the film** — not opened between P1's install and BK-1 | ☐ |

**Rhythm:** one action, then 5–10 s; say what you do ("left hold", "right hold", "tap", "switch"). Export the debug log at the end of each part. Serials: never
unredacted in any note (first 4 + last 2 characters).

### Steps

DLCIs are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05). Every MAESTRO request starts `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25` on
**channel 19** or `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25` on **channel 21** (Info → "Control channel"). **Reference frames** — "B74" = `CAP-074-btsnoop_hci.log`
(the Owner user with Play, 1.2.0 `5b4d5db`, handle `0x000b`), "B73" = `CAP-073-btsnoop_hci.log` (user 10 without Play, the same build), re-derived by
`ai-sessions/0087` with `tshark -r <log> -Y "frame.number==N" -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type
-e data.data` (exit 0):

| Request / event | Reference (channel 19 unless said) |
|---|---|
| The Connect: `SABM` DLCI 0x02, then `ReadSetting 4:16`, 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29, `SubscribeRuntimeInfo`, `GetHardwareInfo` | B74 15741 (`SABM`, 06:42:29.897), 15758 `7e003b0310131dea71de7d5e2551aed0ae2a022010fdae3f247e` (4:16), 15770 … 15832 (the twelve settings, ascending), 15844 `…90821ee6602d65a97e` (runtime info), 15846 `7e003b0310131dea71de7d5e25e3a5ec28ff1ebbb77e` (`GetHardwareInfo`) — byte-identical to `CAP-073` B73 3304–3376 (`CAP-074-FINDINGS.md` §2); channel 21 as `CAP-073-EVENT-NOTES.md`'s table |
| The snapshot claim after a Connect (user 0): attempt 1 fails, attempt 2 connects | B74 14450 (Play's claim killed, `DISC`), 14500 `SABM` 06:40:51.247, 14525 `Notify` `0813000401e8e840` (ADAPTIVE, `e8`) |
| A tap on the held channel (no second `SABM`) | B74 14552 `0813000401e8e840` (the `Get` answered) → 14553 `0812001401e8e880` + 16 × `00` (`Set` TRANSPARENT) → 14555 `ff010006081201e8e880` (ACK) → 14556 `0813000401e8e880` |
| **The cut** (Play's recovery; the stack closes the app's port) | the phone's `DISC` of the app's DLCI 0x04 **without** a "released" line in the export — B74 14577 06:40:55.922, **4.68 s** after `SABM` 14500 (5.22 s after Play's loss 14450); export: "RFCOMM on-demand channel 0x04 closed (IOException: bt socket closed, read return: -1)" + "Message Stream hold ended: the channel was closed, not by this app" |
| Play's retry after a cut | Play's `SABM` with `03 08 00 02 01 25` as its first message, 2.2–2.6 s after the cut — B74 14624 06:40:58.217 (2.29 s after 14577) |
| The shown hold (the unprovoked `Notify` inside the app's claim) | B74 15853 `0813000401e8e840` (the snapshot's answer) → **15860 `0813000401e8e880`** (06:42:33.204, no `08 11`/`08 12` before it) → shown TRANSPARENT (film t=138) → the cut 15909 (06:42:35.842) |
| A hold that reaches Play (not the app) | an unprovoked `Notify` on Play's claim — B74 15986 … 16004 (between Play's `SABM` 15931 and its `DISC` 16078); nothing on screen |
| **The re-claim (mode "Take it back", 1.2.1)** | a new app `SABM` + `08 11 00 00` ≈ **3.0 s** after the cut (then ≈ 6 s, ≈ 12 s after the next cuts), attempt 1 failing (Play on the channel) and attempt 2 connecting ≈ 0.4 s later; export: "Message Stream re-claim N/3 after a foreign close, D ms later (ADR-062)" then "hold started"; after the third: "Message Stream re-claim stopped after 3 re-claims — …" and the hint on the card. **No reference capture exists — this run makes it** |
| The hold in the user without Play (part 2) | B73 3484 `SABM` 08:27:59.763 → 3492 `08110000` → 3503 `0813000401e8e808`; the unprovoked 3531 `…40`, 3543 `…80`, 3546 `…08`, 3547 `…40` (four holds, each shown); **no `DISC` for 30.97 s**; the phone's `DISC` 3654 08:28:30.890, 1.509 s after leaving the tab |

Anything else from the app on the wire (a request not in this table or in `CAP-074` §2/§3, a second `GetHardwareInfo` in a session, anything on DLCI
0x08/0x0a, a claim while the tab is **not** shown that is not a tap/pull/Connect) is a finding.

#### Part 1 — the Owner user (user 0) **with** Google Play services (screen recording with sound; `APP_TESTPLAN.md` X1–X9; `PAIR-003`, `BATT-004`, `FW-003`, `ANC-001`…`ANC-004`, `TOUCH-007`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BK-1 | P0–P9 done, user 0, both buds worn | open the app | the session opens by itself (ADR-044); "Probably worn"; Left/Right %, the Case | DLCI 0x02 `SABM`, the announcement, the thirteen requests in order, `GetHardwareInfo` last, answered; the snapshot claim: attempt 1 fails ≈ 100–360 ms (Play on the channel), attempt 2 connects; `03 03` burst; `08 11` → `Notify`; the claim released ≈ 1.5 s after its answer **or** cut by Play ≈ 5 s after the `SABM` (the Connection tab is shown — either is fine here) | `PAIR-003`, `BATT-004` | a crash; a request missing or out of order; Safe Mode |
| BK-2 | ready | gear → **Info**: hold 3 s on the build line, the firmware, the serials; **Settings**: the "Noise-control channel" card — hold 3 s on it, open its (i), read it, close | "App: 1.2.1, build 31ee183 (2026-10-12)", no "-dirty"; `release_5.203` ×3; Case / Right bud / Left bud; the card between Dark mode and Case sounds, "Show a hint, do not take it back" selected; the (i)'s four lines | nothing | `FW-003` | "-dirty"; another hash; the card missing or the other row selected on a fresh update |
| BK-3 | back on Connection | **Disconnect**, wait 5 s, **Connect** | ready | the phone's `DISC` of DLCI 0x02; the thirteen requests again; the snapshot claim (attempt 1 fails, attempt 2 connects) | `PAIR-003` | — |
| BK-4 | ready, the **Connection** tab | open the **ANC** tab at once (within 1.5 s of "ready", so the snapshot claim becomes the hold) — or, if later, a new claim | the mode; (i) not needed yet | the hold on the snapshot claim (no new `SABM`) or one new `SABM` + `08 11`; export "Message Stream hold started: the noise-control tab is on screen (ADR-061)" | `ANC-001`…`004` | no hold line |
| **BK-5** | the hold open, mode **hint only** | **press and hold a bud every ≈ 5 s for 30 s** (6 holds); say each; touch nothing on the phone; **then P6 at once** (the system log within a minute of the first cut) | the **first** hold (if inside the ≈ 5 s) changes the mode on screen; **then the hint line appears below the mode buttons:** "The noise-control channel was taken by another app at HH:MM:SS (for example Google Play services' Fast Pair). A change made on a bud is not shown until you tap a mode, Refresh, or pull down." — the later holds change the mode in the ears, **not** on screen; **no new claim** by the app for the rest of the 30 s | the cut ≈ 4.7–5.1 s after the app's `SABM` (the phone's `DISC`, no "released" line; export "closed … read return: -1" + "hold ended: the channel was closed, not by this app" + **"Noise-control channel taken by another client: the hint is shown, no re-claim (ADR-062, hint only)"**); Play's `SABM` 2.2–2.6 s later; every later `Notify` on Play's claim; **no app `SABM` until BK-6** | `TOUCH-007` | an app `SABM` after the cut without a tap (a re-claim in hint-only mode — the M1 defect); no hint line; the hint without a time |
| BK-6 | the hint shown | open the ANC **(i)**, read it, close; then **tap a mode** | the (i) has the hint line and ends with the corrected sentence ("… loses it to this app and may take it back about 5 seconds later; what the app does then is set on the Settings tab under Noise-control channel."); after the tap: the mode after its ACK, **the hint line gone**; ≈ 5 s later the hint is back (the new claim is cut again) | the tap's claim: attempt 1 fails (Play on the channel), attempt 2 connects, `08 11` → `Notify` → `Set` → ACK; held; the cut ≈ 5 s later; the hint-only line in the export again | `ANC-001`…`004` | the hint not cleared by the tap; the old (i) sentence "cannot use it meanwhile" |
| BK-7 | the hint shown | **pull down** on the ANC tab (Refresh) | the hint gone, the mode read; ≈ 5 s later the hint is back | a new claim with `08 11` → `Notify`; the cut | — | the pull not clearing the hint |
| BK-8 | the ANC tab | gear → Settings → **"Take it back while the noise-control tab is open (may fight that app)"**; back to the **ANC** tab | the row selected; back on the tab a new claim (the menu covered the tab: the hold ended, a re-entry) | the `DISC` ≈ 1.5 s after the gear opened (the tab left), a new `SABM` + `08 11` on return | — | the choice not kept (a re-open of the menu shows "hint only") |
| **BK-9** | the hold open, mode **take it back**, both buds worn | **press and hold a bud every ≈ 4 s for 60 s** (≈ 15 holds); say each; touch nothing; count aloud which ones change the mode on screen | the mode changes on screen for the holds that fall in the app's windows (≈ 5 s of every ≈ 8.5 s during the chain: expect ≈ 6 of 10 shown, the others not); **after the third re-claim's cut** (≈ 37 s after the first cut) **the hint line with "; the app took it back 3 times and stopped"**, and no further change on screen for the rest of the 60 s | the cut ≈ 5 s after the hold's `SABM`; **the re-claim `SABM` + `08 11` ≈ 3.0 s after the cut** (attempt 1 fails: Play's retry had taken the channel; attempt 2 connects ≈ 0.4 s later); the cut again ≈ 5.2–5.7 s after Play lost its socket; the re-claim ≈ 6 s after that cut; the cut; the re-claim ≈ 12 s after; the cut; **then no app `SABM`**; export: "Message Stream re-claim 1/3 after a foreign close, 3000 ms later (ADR-062)", "… 2/3 … 6000 ms", "… 3/3 … 12000 ms", "Message Stream re-claim stopped after 3 re-claims — …"; Play's `SABM` 2.2–2.6 s after each cut (its retry) | `TOUCH-007`, `ANC-001`…`004` | a re-claim sooner than 2.8 s or later than 3.3 s after the cut (the first step); a fourth re-claim; the hint missing or without "3 times"; a re-claim while the tab is not shown |
| BK-10 | the hint shown (after the cap) | **tap a mode** → hold a bud twice ≈ 4 s apart → wait 10 s | the hint gone; the holds inside the app's windows shown; the chain restarted (a re-claim ≈ 3 s after the next cut) | the tap's claim; the cut; **the re-claim at ≈ 3 s again** (a new chain after the tap) | `ANC-001`…`004`, `TOUCH-007` | no re-claim after the tap's claim was cut (the counter not reset) |
| BK-11 | the hold or a re-claim pending | **tap the Sound tab within 2 s of a cut** (while the 3-s delay runs); wait 10 s; back to **ANC** | Sound; on return a new claim | after the tab change: **no `SABM` 3 s after the cut** ("Message Stream re-claim cancelled (ADR-062)" in the export), the phone's `DISC` if a claim was open; on return a new `SABM` + `08 11` | — | a re-claim while Sound is shown |
| BK-12 | the ANC tab, the hold or the chain running | **Home** within 2 s of a cut; wait 10 s; return to the app | the home screen; on return the ANC tab with a new claim | no `SABM` while hidden ("re-claim cancelled"); the `DISC` ≈ 1.5 s after Home if a claim was open; on return a new claim | — | a `SABM` while the app is hidden |
| BK-13 | the chain running (within 2 s of a cut) | gear → Settings → **"Show a hint, do not take it back"**; back to ANC | the row selected; on return the hint behaviour of BK-5 (the first hold shown, then the hint, no re-claim) | "re-claim cancelled" at the switch; no `SABM` 3 s after that cut; the return's claim; its cut; no re-claim | — | a re-claim after the switch to hint only |
| BK-14 | the ANC tab, hint only | **Disconnect**; wait 5 s; **Connect**; stay on the ANC tab | not connected, then ready; the hint gone at Connect | the thirteen requests; the snapshot claim becomes the hold; its cut ≈ 5 s later; the hint | `PAIR-003` | the hint surviving the Connect |
| BK-end1 | — | Debug → **Export**; a second system log; the status bar across a minute change; stop the recording; **switch to user 10** | "Debug log saved (N lines)." | — | — | an empty file |

#### Part 2 — the secondary user (user 10) **without** Google Play (screen recording; `APP_TESTPLAN.md` X10–X12; the `CAP-073` §4 regression)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BK-15 | user 10, P0 (no Play), both buds worn, the recording on | open the app; **Info** 3 s; Settings: the "Noise-control channel" card (hint only, the default) | the session opens by itself; "App: 1.2.1, build 31ee183 (2026-10-12)"; the card as in BK-2 | the Connect sequence (`CAP-073`'s bytes); the snapshot claim released ≈ 1.5 s after its answer (the Connection tab shown) | `PAIR-003`, `FW-003`, `BATT-004` | Safe Mode; "-dirty" |
| **BK-16** | ready, hint only | open the **ANC** tab; **press and hold a bud every ≈ 5 s for 30 s**; touch nothing | **every** hold changes the mode on screen (`CAP-073` §4: four of four); **no hint line** | one `SABM` + `08 11`; **no `DISC` for the 30 s**; the unprovoked `Notify` of each hold on the app's claim; nothing sent by the app; 0 Play-services claims (`frame contains 03:08:00:02:01:25` → 0 in this part) | `TOUCH-007`, `ANC-001`…`004` | a `DISC` while the tab is shown; a hold not shown; the hint line |
| BK-17 | the hold open | gear → Settings → **"Take it back …"**; back to **ANC**; **press and hold a bud every ≈ 5 s for 30 s** | every hold shown; **no hint, no re-claim** (nothing took the channel) | the `DISC` ≈ 1.5 s after the gear; one new `SABM` + `08 11` on return; **no further `SABM`, no `DISC`** for the 30 s; no "re-claim" line in the export | `TOUCH-007` | any re-claim line or extra `SABM`; a `DISC` while the tab is shown |
| BK-18 | the hold open, take it back | tap the **Controls** tab; wait 5 s | Controls | the phone's `DISC` ≈ 1.5 s after the tab change ("hold ended: the noise-control tab was left"); nothing else | — | no `DISC`, or one before 1.3 s |
| BK-19 (optional, `TODO.md` §2) | back on ANC, the hold open | pull **Quick Settings** down; tap the **ANC tile** once; close | the tile steps one mode; the app follows | `08 11` → `Notify` → `Set` → ACK on the held channel, no `SABM`, no `DISC` | `ANC-001`…`004` | a second `SABM` |
| BK-20 (optional) | the hold open | **Home**; wait 5 s; return | the home screen; ANC again | the `DISC` ≈ 1.5 s after Home ("… the app is not visible"); a new `SABM` + `08 11` on return | — | no `DISC` after Home |
| BK-21 (optional) | the ANC tab shown, both worn | both buds **into the case**; wait 10 s; **out into the ears** | the session ends with its cause; ready again by itself | the loss; the ADR-044 re-open, its snapshot claim **kept** as the hold (no `DISC` while the tab is shown) | `PAIR-003` | the snapshot released while the tab is shown |
| BK-end2 | — | gear → Settings → back to **"Show a hint …"** (the default, as installed); Debug → **Export**; the status bar across a minute change; stop the recording; P1's APK pull | "Debug log saved (N lines)." | — | — | — |

### Don'ts

- Do not uninstall the installed 1.2.0 and do not clear the app's data (P1 — the run is an update; the choice of BK-8 must survive BK-end1's user switch only
  as a stored setting of each user — each user has its own DataStore file).
- Do not open the app before the film (P9).
- In BK-5, BK-9, BK-16 and BK-17 touch nothing on the phone while holding the buds; say each hold.
- Do not tap two things within 5 s of each other, except where a step says "within 2 s of a cut" (BK-11, BK-12, BK-13).
- Do not write a serial number unredacted anywhere in this folder (first 4 + last 2 characters); no Wi-Fi name, phone number, account name or e-mail in any
  committed text (the system log is committed as an extract, as `CAP-074`).

### After the run

Into this folder: the screen recording(s), the two system logs of part 1 and the one of part 2 (as extracts — the full logs and the originals to
`~/opencontrol-capture-originals/CAP-075/`), every debug export, the app logcat if saved, the HCI snoop logs, the P0/P1 outputs with their exit statuses, the
pulled APK's `sha256sum`, `dist/1.2.1/`'s `.sha256` and the script's certificate line; then `sha256sum *` into CAP-075-sha256sum.txt. Before committing: blur
Wi-Fi names, addresses and other device names on film. Commit on the release branch after the build commit (`RELEASING.md` C3) — never on `main`.
---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-075-2026-10-12_17-22-31_2026-10-13_06-34-32-Group_BK/CAP-075-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-075-2026-10-12_17-22-31_2026-10-13_06-34-32-Group_BK/CAP-075-EVENT-NOTES
