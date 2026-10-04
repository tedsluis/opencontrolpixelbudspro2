# Event Notes: OpenControl for Pixel Buds on Pixel 9a (GrapheneOS) — Group BB, the first hardware run of the `ai-sessions/0062` build (`CAP-066`)

**Status:** 🟢 **Video pass + full log analysis complete** (`ai-sessions/0063`). See `CAP-066-FINDINGS.md` for the wire-level analysis, the commands and
the raw bytes. The procedure the maintainer followed (the skeleton written by `ai-sessions/0060`, extended by `0061`, adapted by `0062`) is kept unchanged as
**Appendix A**; the timeline below records what was **actually** done — the maintainer filmed the ears, did not speak, changed the order of some steps,
repeated some, and did not do BB-9 as designed, BB-12, L3, K5, A5, (E), B4 and Z1.

**Purpose:** the first hardware run of the `ai-sessions/0062` build (ANC `Get` before every `Set`, the new wording, the cut-off handling, the settings menu with
dark mode and Info, the Disconnect label's contrast, StrictMode), the Settable byte with buds straight from the case (ADR-049), lead L-1 (the hosting bud,
`PROTOCOL.md` §2.2a), the ANC tile's subtitle with the tile enlarged and a real force-stop, and the `CAP-065` robustness leftovers.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-066`                     |
|      Group(s)    |                        BB                          |
|       Date       |                    2026-10-01                      |
| Firmware version | 🟢 `release_5.203` — all 10 DLCI 0x02/0x03 announcements of the HCI logs (e.g. `.log.last` frame 820) and the Info tab on film ("Case / Left bud / Right bud: release_5.203", 16:13:32–36, 16:28:00–34) |
|   Test device    | Pixel 9a, GrapheneOS, Android 17 `CP3A.260905.009` (logcat header `google/tegu/tegu:17/CP3A.260905.009/2026092501`). App: OpenControl `io.github.tedsluis.opencontrolpixelbuds:1`, targetSdk 34, **build `043a09b` (2026-10-01), not "-dirty"** — the Info tab on film reads "App: 0.1.0-dev, build 043a09b (2026-10-01)" (16:13:32–36 light, 16:28:00–34, 16:29:34–38 dark). `043a09b` is `HEAD` of 2026-10-01 15:38:08 (docs: session 0062 RESULT); `git diff ad0c4ba 043a09b -- android` is empty, so the app code is `ad0c4ba`, the last commit under `android/`. Installed 13:42:56 UTC (system log `PackageManager: installation completed`, line 28166 of the before-force-stop log; `installer_clear_app_data_caller` line 28123), 4 min 56 s after `ad0c4ba`; the `installer_clear_app_data_caller … flags=39` line at the update is a **code-cache-only** clear (0x20 \| 0x4 \| 0x2 \| 0x1, caller `prepPerformDexoptIfNeeded`; AOSP `IInstalld.aidl`, FINDINGS §0) — the app's data were **not** cleared (Debug mode stayed on from the first line of the export). The maintainer did not speak on the film (chat 2026-10-01). Play services *Nearby devices*: **allowed** — the maintainer's statement; consistent with Play services holding the Message Stream between the app's claims (26 Play-services claims) |
| Other devices    | LE `c8:cc:a8:e7:48:93` (handle `0x0041`) = the Fitbit "Charge 6" (Android's Bluetooth dialog on film 16:32:28 "Charge 6 Verbonden"; CDM association 37 of `com.fitbit.FitbitMobile` in the system log); excluded. The same dialog lists "Niro" (saved). No other classic link |
| Video file       | `CAP-066-recording.mp4`: 1,312.44 s, 1,024,704,351 bytes, H.264 1280×720 rotation −90 (portrait), 39,194 frames, r_frame_rate 179/6; **AAC stereo 44.1 kHz, 1,312.07 s** (decodes); `creation_time` 2026-10-01T14:34:50Z (= the end). Burned-in overlay `Oct 1, 2026 HH:MM:SS`: **first frame 16:12:57, last frame 16:34:50** (full-resolution crops) |
| Audio            | Quiet: per-second RMS median −74.8 dBFS, 90th percentile −61.2, maximum −37.3 at t = 1198 s (16:32:55); 165 s more than 12 dB above the median, broadband handling transients; two runs of regular ≈ 0.5-s transients at t ≈ 1194–1214 s (16:32:51–16:33:11) and 1251–1265 s (16:33:48–16:34:02) — footsteps, timing K3's walk out of range and back; no speech pattern in the full spectrogram. Nothing heard inside the buds is on the track |
| HCI logs         | **Two files, because Bluetooth was switched off and on during the film (K1/K2):** `CAP-066-btsnoop_hci.log.last` — 8,086 packets, 16:13:05.082266–16:32:17.845273, the log **before** the 16:32 Bluetooth-off (it starts with the HCI Reset of the 16:13 Bluetooth-on, frame 1); `CAP-066-btsnoop_hci.log` — 2,587 packets, 16:32:23.725933–16:36:28.686106, the log **after** it (frame 1 = HCI Reset). Both "Bluetooth H4 with linux header", no size limit, 0 `cap_len≠len`; 0 / 2 out-of-order pairs. The 5.9-s gap is the Bluetooth-off period (system log: `STATE_OFF` 14:32:17.651 UTC, `STATE_TURNING_ON` 14:32:24.378); `.last` ends with the phone's `Disconnect` commands (8040/8042) and their `Disconnection Complete` reason `0x16` (8071/8072); `.log` starts with the controller reset — no Buds traffic is lost at the boundary. Film covers phone time 16:12:57.5–16:34:50.5: the HCI starts 7.6 s after the film's first frame (the P2 Bluetooth-on) and ends 1 min 38 s after its last |
| App debug exports | **Two** (the app's own names): `CAP-066-opencontrol-debug-20261001-162556.txt` — 769 lines, 15:43:01.669–16:25:46.906, saved 16:25:56 (film 16:25:54–16:26:02) **before** the force-stop; its first line is the process start of the 15:43 installation (PID 6546), so the ring buffer did not wrap. `CAP-066-opencontrol-debug-20261001-163412.txt` — 255 lines (254 newlines), 16:27:05.492–16:33:56.192, saved 16:34:12–16 (film; logcat "Debug log exported (255 lines)" 14:34:16.973 UTC) **after** the force-stop — its first line is the new process (PID 12310). No overlap (different processes). **Not covered by any export:** 16:25:47–16:27:05 (the first process's last 68 s and the restart) — covered by the first logcat (to 16:26:23.6), the after-force-stop system log (app lines to 16:26:30.8, `am_kill` 16:26:54.5) and the HCI log; and 16:33:56–16:34:50 (film end) — covered by the second logcat (app lines to 16:35:00.4) and the HCI log. **Both exports were made with Debug mode on** (hex lines in both; film 16:26:02, 16:34:10) |
| App logcats      | `CAP-066-OpenControl-for-Pixel-Buds-log-deb3e621dad8.txt` — 787 lines, **before** the force-stop (last line 14:26:23.637 UTC; PID 6546 only; main buffer from 14:21:18). `CAP-066-OpenControl-for-Pixel-Buds-log-efa74772f5c3.txt` — 697 lines, **after** (to 14:35:00.410 UTC): 261 lines of PID 6546 that are all also in the first logcat (`comm -12` = 261) plus 305 of PID 12310 (main buffer from 14:32:27.8). No crash, ANR or tombstone line in either; **5 StrictMode `LeakedClosableViolation`s** (FINDINGS §8) |
| System logs      | `CAP-066-System-log-d55db7f4e1c8.txt` — 65,360 lines, to 14:26:33.395 UTC, **before** the force-stop; `CAP-066-System-log-7f4bb1d5ea2c.txt` — 62,154 lines, to 14:35:12.805, **after**; 29,801 lines are in both. Per buffer: events from 12:43:48 / 13:11:22, system from 13:15:08 / 13:34:16, **main (incl. the Bluetooth process) only 14:21:18–14:26:33 and 14:32:27–14:35:12 UTC** — the Bluetooth process is not logged for 16:13–16:21 and 16:26:33–16:32:27 local. Crash buffer: one Vanadium WebView sandbox SIGABRT at 14:35:11 UTC (after the film, not the app) |
| Events file      | none |
| Clock offsets    | **Film ↔ phone, measured at both ends** (5-fps crops of the status bar against the overlay): the minute flips 16:13→16:14 and 16:33→16:34 both fall on the 3rd 0.2-s frame of overlay second :59 ⇒ **phone = overlay + 0.5 s (±0.2 s), no drift**. **Logcats and system logs (UTC) = phone − 2 h 00 min 00.000 s** (export 16:21:21.670 "RFCOMM channel 0x04 connect failed after 328 ms" = logcat 14:21:21.670, the same line; second export 16:32:29.440 = logcat 14:32:29.440; system log `on_cl_rfc_init: INIT unsuccessful` 14:21:21.665). HCI and the exports are phone local time |
| Buds (partial, `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` (ADR-010). Classic handle `0x000b` for all ACLs of both files. **Pre-filter:** `bluetooth.addr` is empty with this encapsulation; every command filters `bthci_acl.chandle==0x000b`. **DLCIs are session-local:** where the Buds opened the RFCOMM multiplexer (their `SABM` DLCI 0x00, `.last` 3029, `.log` 1671) MAESTRO = 0x03 and the Message Stream = 0x05; otherwise 0x02/0x04 |
| Wear mapping     | The maintainer's rule (chat 2026-10-01): **head on the right of the frame = Left bud, head on the left = Right bud**; the bud in the **left-hand slot (or left of the case) is the Left bud**. Cross-checked against the runtime-info stream's per-bud charging field (6.2.2 Left / 6.3.2 Right) at every dock change it reports: Right docked 16:16:55.28 (`.last` 2633) and 16:21:40.55 (5344), Right undocked 16:22:23.02 (5973) with the Left already out (5966) — **0 contradictions** (FINDINGS §4) |

## Capture-integrity pre-flight

```
$ capinfos CAP-066-btsnoop_hci.log.last          $ capinfos CAP-066-btsnoop_hci.log
File encapsulation:  Bluetooth H4 with linux header      (same)
Packet size limit:   file hdr: (not set)                 (same)
Number of packets:   8,086                               2,587
Earliest packet time: 2026-10-01 16:13:05.082266         2026-10-01 16:32:23.725933
Latest packet time:   2026-10-01 16:32:17.845273         2026-10-01 16:36:28.686106

$ tshark -r <log> -T fields -e frame.number -e frame.cap_len -e frame.len -e frame.time_epoch \
  | awk '$2!=$3{c++} {if(NR>1 && $4<p) o++; p=$4} END{print "packets:",NR," mismatches:",c+0," out-of-order:",o+0}'
.log.last → packets: 8086  mismatches: 0  out-of-order: 0          .log → packets: 2587  mismatches: 0  out-of-order: 2

$ ffprobe … CAP-066-recording.mp4 → audio aac 44100 Hz 2 ch 1312.07 s; video h264 1280x720, 179/6, 39,194 frames, 1312.44 s, rotation −90;
                                    creation_time 2026-10-01T14:34:50Z; size 1,024,704,351

$ sha256sum *   (identical before and after the folder rename, `sha256sum -c`, 10/10 OK, 2026-10-01)
5fc7f124…f586f  CAP-066-btsnoop_hci.log                    eb98883a…8072c  CAP-066-btsnoop_hci.log.last
c32e841f…b316   …debug-20261001-162556.txt                 725015cb…a471   …debug-20261001-163412.txt
073d69a3…8a3f   …log-deb3e621dad8.txt                      db19d6b5…835a   …log-efa74772f5c3.txt
5bc63aa4…4e44   CAP-066-recording.mp4                      7ac59796…fb24   CAP-066-System-log-7f4bb1d5ea2c.txt
be3dd84a…39dd   CAP-066-System-log-d55db7f4e1c8.txt
```

File modes: the two HCI logs were `-rw-r--r--`, the other seven `-rwxr-----`; set to `644` on the maintainer's approval (chat 2026-10-01, "Migration":
*"Approve as proposed (Recommended)"*). `git check-attr filter` → `lfs` for all nine capture files (`.log.last` matches `captures/**/*.log.last` in
`.gitattributes`). The phone's file names are kept; which file of each pair is before/after the force-stop is stated in the Log Metadata above.

## Video and audio review method

- **Every second:** the whole film was extracted at 1 fps (1,313 frames, `ffmpeg -vf fps=1,scale=405:720`) and viewed at 2-s spacing on 82 contact sheets
  (4 × 2, overlay time on each frame); an `ffmpeg` scene-change pass (`select='gt(scene,0.05)'` on a 320-px copy, 90 changes) found no change outside the
  windows viewed. Transitions a claim depends on were narrowed: the two status-bar minute flips (5 fps), the bud order at 16:14:08–24 and 16:22:15–23 (2 fps),
  the BB-2 tap 16:15:19–26 (2 fps), the back press after the dark-mode change 16:28:51–55 and 16:29:48–51 (4 fps), the rotation 16:31:36–49 (2 fps), the Info
  tab and the dark Connection card (full resolution, t = 35.5 s and 960.5 s). App actions with a log line (tile taps, `Set not sent`, re-opens) are timed from
  the export, not the film.
- **Audio:** decoded to 16 kHz mono (`ffmpeg -i CAP-066-recording.mp4 -vn -ac 1 -ar 16000`); per-second RMS over the whole 1,312 s and a full-length
  spectrogram (`showspectrumpic`), zoomed at 1180–1315 s. Used only to time K3 (footsteps).
- **Privacy (ADR-037):** no camera address overlay; no speech; no face but the maintainer's own head/hair/glasses/ears (by design). Legible personal data:
  the notification shade at 16:16:24, 16:25:02, 16:27:22 and 16:28:08–10 (WhatsApp group and contact names with snippets, LinkedIn, a bank's "€ 9,00
  afgeschreven" notice, Volkskrant, Play Integrity, USB debugging) and — new in this run — Android's save-as dialog (Downloads) at 16:26:00, 16:26:28–36 and
  16:34:14–16, listing file names that include a street address and a bank statement with an account-like number; the media card "Back To Black — Amy
  Winehouse" in Quick Settings. **The maintainer chose to keep the film unblurred** (chat 2026-10-01, `ai-sessions/0063`, "Privacy": *"Keep unblurred"*).
- **Wear:** each wear row names the side of the frame the head was on, or the slot / side of the case the bud came from, and the bud that follows.

## Event Timeline

Times are **phone local time** (HCI / debug export); film-only events are overlay + 0.5 s, to ±1 s (2-s sheets) unless narrowed. Frame numbers are HCI frames
of `CAP-066-btsnoop_hci.log.last` ("A") or `CAP-066-btsnoop_hci.log` ("B"), handle `0x000b`. "E1 N" / "E2 N" is line N of the before / after export. "Step" =
the skeleton step (Appendix A); "var." = done differently, "rep." = repeated. Registry Test-IDs in brackets. "PS" = a Message Stream claim of Google Play
services (its first message is `03 08 00 02 01 25`); the app's claims start with `08 11` (FINDINGS §2).

| Time (phone) | Action / event | Actor | Step / test | Evidence |
|---|---|---|---|---|
| 15:42:56 | Debug APK of `043a09b` installed over the old one (code-cache-only clear, no data clear); process PID 6546 starts 15:43:00.2, activity 15:43:01 | User / OS | P1 ✓ | system log 28123–28178, 28493 (before-force-stop log) |
| 15:43:02–15:59:51 | Before the film: three automatic re-opens (ch 21, 19, 21), a Disconnect and Connect tap at 15:45:06/09, four tile taps at 15:56:39–15:56:50 (Notify `80` → ACK `40`, `40` → `20`, `20` → `08`, `08` → `80` — each step from the claim's own `Notify`) | User / App | (before the film; no HCI) | E1 1–260 |
| 16:12:57.5 | **Film starts** in Quick Settings: the Bluetooth dialog, "Pixel Buds Pro 2 va… Actief, 76%"; both buds lie on the table left and right of the **open, empty case** (the lid stays open for the whole film) | — | — | film |
| 16:13:00.8–16:13:01.4 | **Bluetooth off** (system `STATE_TURNING_OFF` → `STATE_OFF`); the app: link `UNKNOWN`, session lost, cause "undetermined (provisional …)" — the last cause line | User | P2 | system log (BluetoothAutoOff intent lines 14:13:00.837, 14:13:01.361 UTC); E1 279–283 |
| 16:13:05.3–16:13:05.7 | **Bluetooth on**; HCI log `.last` starts with the HCI Reset (A1 16:13:05.082); the phone pages the Buds (A173 `Create Connection` 16:13:05.698) → ACL (A430 16:13:09.567) | User / OS | P2 ✓ | system log; HCI |
| 16:13:11.7–16:13:16.3 | **Opened by itself:** link `CONNECTED` → "Automatic re-open (ADR-044, trigger: LINK_BACK)" → MAESTRO `SABM` 0x02 (A765) → announcement **ch 21** (A820) → reads → `SubscribeRuntimeInfo` → snapshot claim: `08 11` (A1033) → `Notify 01 e8 00 20` (A1049, both buds on the table) | App | [`PAIR-003`] | E1 286–324 |
| 16:13:18–16:13:20 | Quick Settings: the ANC tile is already **large** — "ANC / Not allowed now" (BB-13 done before the film) | — | BB-13 (before), **BB-3 subtitle wording ✓** | film |
| 16:13:20–24 | OpenControl Connection tab: ready; Left 77 %, Case 85 %, Right 76 % (Case: the in-memory last-seen value of the 15:44 session) | App | — | film |
| 16:13:26–16:13:36 | Gear → **Settings** tab (Dark mode: System), **Debug** tab (Debug mode on, "Unidentified frames (48)"), **Info** tab: "App: 0.1.0-dev, build 043a09b (2026-10-01)", "Firmware (from the Buds' announcement, 16:13:13): Case / Left bud / Right bud: release_5.203", "Control channel: 21"; back | User / App | **P7 ✓**, BB-16 (part) | film t = 28–39 s (full res. t = 35.5 s); nothing from the app on the wire |
| 16:13:59.5 | Status bar 16:13 → 16:14 (overlay :59 + 0.5 s) | — | P6 ✓ | 5-fps crops |
| 16:14:09–13 | The bud **right of the case** picked up; head on the **left** of the frame ⇒ **Right** into the ear (≈ 16:14:12) → PS `Notify 01 e8 e8 80` (A1396, 16:14:14.64) | User / Buds | (not planned) [`INEAR-003`] | film 2 fps |
| 16:14:18–22 | The bud **left of the case** picked up; head on the **right** ⇒ **Left** into the ear (≈ 16:14:21); both worn; PS `Notify … e8 80` (A1427, 16:14:23.40); no session change (ch 21 kept) | User / Buds | BB-1 pre-state [`INEAR-002`] | film 2 fps |
| 16:14:30 | ANC tab: "The Buds don't allow changing noise control right now (usually because no bud is in an ear). Tapping a mode checks again first." — the app's last check is the 16:13:16 `00` (stale while worn, by design) | App | F-2 wording ✓ | film |
| 16:14:38–42 | Head on the **right** ⇒ **Left** out of the ear, laid left of the case → PS `Notify … e8 80` (A1489, 16:14:40.49, the Right still worn) | User | BB-1 | film |
| 16:14:46–50 | Head on the **left** ⇒ **Right** out, laid right of the case; **both on the table** → PS `Notify 01 e8 00 20` (A1523, 16:14:49.13) | User / Buds | BB-1 | film |
| 16:15:17.5 | **ANC Refresh** (finger 16:15:14) → app claim A1630: `08 11` (A1638) → **`Notify 01 e8 00 20`** (A1652, 16:15:17.92); the note stays, buttons enabled | User / App | **BB-1 ✓** [`ANC-001`] | film; HCI |
| 16:15:23.5 | **ADAPTIVE tapped** (buds on the table) → claim (attempt 1 failed 426 ms; A1778 16:15:25.22) → `08 11` (A1789) → **`Notify … e8 00 20`** (A1800) → "ANC Set not sent: this claim's Notify reports no switchable mode (Settable 0x00)" — **no `08 12`**; Off stays selected | User / App | **BB-2 ✓** [`ANC-003`] | film 2 fps (finger on Adaptive 16:15:23); E1 338–347 |
| 16:15:36–16:15:42 | Quick Settings: tile "ANC / Not allowed now"; **tile tapped** 16:15:38.719 ("shown: OFF, the Buds last allowed no change") → claim A1951 → `08 11` (A1965) → **`Notify … 00 20`** (A1972) → "Set not sent"; toast "The Buds don't allow changing noise control right now (usually because…" (16:15:42) | User / App | **BB-3 ✓** [`ANC-001`] | film; E1 348–358 |
| 16:15:56–16:16:10 | Right-of-case bud picked, hair on the **left** ⇒ **Right** into the ear (16:16:00) → PS `Notify … e8 80` (A2090); left-of-case bud, head on the **right** ⇒ **Left** in (16:16:06–10) → PS `Notify … e8 80` (A2125); both worn | User / Buds | BB-4 pre-state [`INEAR-002`, `INEAR-003`] | film |
| 16:16:16.5 | **ADAPTIVE tapped** (both worn; the note still shown from the last check) → claim A2177 → `08 11` (A2190) → `Notify 01 e8 e8 80` (A2201) → **`08 12 00 14 01 e8 e8 40`** (A2202) → **ACK** `ff 01 00 06 08 12 01 e8 e8 40` (A2204) → `Notify … 40`; Adaptive selected, note gone (16:16:20) | User / App | **BB-4 ✓** [`ANC-003`] | film; HCI |
| 16:16:24 | Notification shade (privacy) → Quick Settings: tile "ANC / Adaptive" (red) | User | — | film |
| 16:16:27.842 | **Tile tapped** ("shown: ADAPTIVE") → claim A2344 → `08 11` (A2358) → `Notify 01 e8 e8 40` (A2369) → **`08 12 … 20`** (A2370, the mode after Adaptive in the tile cycle) → ACK (A2372); tile "ANC / Off" (grey) from 16:16:30 | User / App | **BB-4t ✓** [`ANC-001`] | film; E1 373–385 |
| 16:16:46–48 | Hair on the **left** ⇒ **Right** out of the ear (Left still worn, session on **ch 21**) → PS `Notify … e8 80` (A2500, 16:16:49.879) → **Buds `DISC` MAESTRO 0x02** (A2502, 16:16:49.881) with the ACL up; tile "ANC / Open the app" (16:16:50, not ready) | User / Buds | **BB-12m ✓** (hosting bud) | film; HCI; E1 388–392 |
| 16:16:51.4–16:16:52.9 | Re-open **AFTER_LOSS** (1.51 s) → `SABM` 0x02 (A2531) → announcement **ch 19** (A2547, 16:16:52.207) → snapshot `Notify … e8 80` (A2595); tile "ANC / Off" then "ANC / Transparent" (red, 16:16:54–17:02) | App | **BB-12m: 21 → 19 ✓** | E1 393–429 |
| 16:16:54–55 | The Right bud into the **right** slot → stream `6.3.2 = 2` (A2633, 16:16:55.28, Right charging; Case 85) | User | (not planned) [`CASE-006`] | film; HCI |
| 16:16:58–17:02 | The Left bud from the ear into the **left** slot (both docked, lid open) → PS `Notify 01 e8 00 20` (A2738, 16:17:00.63) → **ACL `0x13`** (A2745, 16:17:03.124); "link went down" (final cause, E1 444); tile "Open the app" | User / Buds | [`CASE-006`] | film; HCI; E1 438–444 |
| 16:17:24–28 | **Right**-slot bud out onto the table (16:17:24), then the **Left**-slot bud (16:17:26): both straight from the case to the table → **Buds' Connection Request** (A2804, 16:17:24.961) → ACL → re-open LINK_BACK → `SABM` 0x03 (A3248) → announcement **ch 21** (A3267, Right out first) → snapshot `Notify 01 e8 00 20` (A3345, 16:17:28.58 — 2–4 s after leaving the case); the claim is closed by the phone 41 ms after the answer (A3349) | User / Buds / App | **BB-5 start** [`CASE-005`, `CASE-004`, `PAIR-003`] | film; HCI; E1 445–483 |
| 16:17:38–46 | Shade closed → home screen → "Luisteren" folder → OpenControl (activity stopped/resumed: observer stopped 16:17:39.73, started 16:17:45.55) | User | — | film; E1 484–487 |
| 16:17:49.9–16:20:17 | **BB-5 Refresh series**, buds untouched on the table (film 16:17:28–16:20:44): app claims A3554 (16:17:49.9), A3722 (16:18:03.9), A3916 (16:18:35.2), A4127 (16:19:12.0), A4364 (16:19:47.0), A4570 (16:20:15.7), each `08 11` → **`Notify 01 e8 00 20`** (A3574, 3742, 3937, 4148, 4384, 4595) ≈ 25, 39, 70, 107, 142, 171 s after leaving the case; the Play-services claims in between read `00` too (A3454, 3635, 3798, 3991, 4206, 4444, 4651) | User / App | **BB-5 ✓ (done, 6 Refreshes instead of 5)** [`ANC-001`] | film (finger/ripple 16:17:48, 16:18:02, 16:18:32, 16:19:10, 16:19:44); HCI |
| 16:20:46–58 | Right-of-case bud, hair on the **left** ⇒ **Right** into the ear (16:20:48) → PS `Notify … e8 80` (A4741); left-of-case bud ⇒ **Left** in (≈ 16:20:56) → PS `Notify … e8 20` (A4760, 16:20:59.62); both worn | User / Buds | BB-8 pre-state [`INEAR-002`, `INEAR-003`] | film |
| 16:21:12.5 | **Transparency tapped** (finger 16:21:10) → claim A4855 → `08 11` (A4867) → `Notify 01 e8 e8 20` (A4878) → **`08 12 … 80`** (A4879) → **ACK** (A4881) → `Notify … 80`; Transparency selected, note gone (16:21:14) | User / App | **BB-8 ✓** [`ANC-004`] | film; HCI |
| 16:21:21.5 | **Off tapped** (finger 16:21:20) → claim A5007 → `08 11` (A5020) → `Notify … e8 80` (A5028) → **`08 12 … 20`** (A5030) → **ACK** (A5033); Off selected (16:21:24) | User / App | **BB-8 ✓** [`ANC-001`] | film; HCI |
| 16:21:36–40 | A bud from the ear into the **right** slot (the Right; head not on film) with the Left still worn, session on **ch 21** (announcement A3267; corrected 2026-10-03) → PS `Notify … e8 80` (A5221) → **Buds `DISC` 0x03** (A5223, 16:21:37.023) → re-open AFTER_LOSS → announcement **ch 19** (A5268) → snapshot `Notify … e8 80` (A5333); stream `6.3.2 = 2` (A5344, Right charging) | User / Buds / App | (not planned) [`CASE-006`] | film ("Still connecting…" 16:21:38); E1 570–611 |
| 16:21:44–48 | The Left bud into the **left** slot (both docked, lid open) → PS `Notify … 00 20` (A5431) → **ACL `0x13`** (A5444, 16:21:48.561); "Automatic re-open skipped: the session was lost 9484 ms after an automatic re-open" (the 10-s guard), then "link went down" | User / Buds / App | [`CASE-006`] | E1 612–619; film 16:21:48 "Not connected to the Buds…" |
| 16:21:54–16:22:16 | Connection tab: "Paired — not connected to this phone / … App control: not open yet — tap Connect … / Android no longer shows the Buds connected to this phone — after a disconnect in Android's own Bluetooth settings, with both buds in the case, or out of range. Tap Connect to reconnect. / IOException: bt socket closed, read return: -1" + Connect (no tap) | App | BB-9 pre-state (var.: no Disconnect tap) | film |
| 16:22:18–24 | **Left**-slot bud out first (≈ 16:22:19, laid left of the case), the **Right**-slot bud ≈ 16:22:22 → Buds' Connection Request (A5530, 16:22:20.792, only the Left out) → ACL → re-open LINK_BACK → announcement **ch 19** (A5893) → snapshot `Notify … e8 00 20` (A5944, 16:22:22.39); battery `03 03 00 03 4d cd ff` (Right 77 % **charging**, still docked) then `4d 4d` (E1 637–661); stream A5966 (Right charging, Case 83) → A5973 (none); film 16:22:22 "L 77 % · Case 84 % · R 77 % ⚡", 16:22:24 Case 83 % | User / Buds / App | **BB-9 var.** (automatic re-open, no Connect tap) [`CASE-004`, `CASE-005`, `PAIR-003`, `BATT-004`] | film 2 fps; HCI; E1 620–674 |
| 16:22:28–50 | Battery (i): "Left: 77% (updated 16:22:23) — not charging (out of the case) (16:22:23) / Right: 77% (updated 16:22:23) — not charging (out of the case) (16:22:23) / Case: 83% — last seen 16:22:2x (no bud charging in the case) / The Buds report the Case level only while a bud is in the case. / Firmware: release_5.203" — opened ≈ 6 s after ready; no "last connection" marks visible (the new report had already arrived) | User / App | **BB-9 var.** (I-4 words not seen) | film |
| 16:22:50–52 | Right bud from the table, hair on the **left** ⇒ **Right** into the ear (Left still on the table), session on **ch 19** → PS `Notify … e8 80` (A6274) → **Buds `DISC` 0x02** (A6278, 16:22:53.869) → "The Buds closed the app's channel (this happens when a bud goes in or out of the case or an ear). Android still shows the Buds connected; the app reopens its channel by itself while it is on screen, or tap Connect." → re-open AFTER_LOSS → announcement **ch 21** (A6441) | User / Buds / App | (not planned) — L-1: a switch **19 → 21 on insertion** of the Right | film; E1 675–716 |
| 16:22:58–16:23:04 | The Left bud into the ear (hands cover the head) → PS `Notify … e8 20` (A6598, 16:23:03.53, both worn); no session change (ch 21) | User / Buds | [`INEAR-002`] | film |
| 16:23:22–24 | A bud laid **left** of the case (the Left out of the ear, the Right still worn), session on **ch 21** → PS `Notify … e8 80` (A6672); **no `DISC`**, ch 21 kept | User / Buds | L-1: the non-hosting bud out (no switch ✓) | film; HCI |
| 16:23:56–16:24:02 | The Left from the table, head on the **right** ⇒ **Left** into the ear → PS `Notify … e8 20` (A6779); no session change | User / Buds | [`INEAR-002`] | film |
| 16:24:10–14 | Glasses at the **left** edge ⇒ **Right** out of the ear (Left worn), session on **ch 21** → PS `Notify … e8 80` (A6800) → **Buds `DISC` 0x02** (A6803, 16:24:12.944) → "App control: connecting…" → re-open AFTER_LOSS → announcement **ch 19** (A6850); the Right laid right of the case | User / Buds / App | **BB-12m rep. ✓** (21 → 19) | film; E1 721–762 |
| 16:24:46–52 | The Right from the table, hair at the **left** edge ⇒ **Right** into the ear (Left worn), session on **ch 19** → PS `Notify … e8 20` (A7024); **no `DISC`**, ch 19 kept; both worn from here to the end of the film (the head is not on film after 16:25) | User / Buds | L-1: the non-hosting bud in (no switch ✓) | film; HCI |
| 16:25:02–16:25:30 | Notification shade (privacy) → Quick Settings tile "ANC / Transparent" (the Buds' own mode report `80`) → **"Tegels bewerken"**: the ANC tile ("ANC / OpenControl for Pixel Buds") resized/moved to large | User | **BB-13 ✓ (on film)** | film |
| 16:25:32–42 | Quick Settings: large tile "ANC / Transparent" (red) | — | — | film |
| 16:25:46–16:26:02 | OpenControl → gear → **Debug** tab ("Unidentified frames (158)") → **Export debug log** → Android's save-as dialog (Downloads; privacy) → saved; toast "Debug log saved (…)" | User / App | X1 (1st export, Debug on) | film; file `…-162556.txt` |
| 16:26:08–16:26:40 | Settings → Apps → OpenControl → App-info → **View logs** → the app's logcat saved ("OpenControl for Pixel Buds log deb3e621da…"), then the **System log** saved ("System log d55db7f4e1…") | User | X1 (before the force-stop) | film |
| 16:26:52–54 | App-info → **"Gedwongen stoppen" (Force stop)**: `ActivityManager: Force stopping … from pid 741 (com.android.settings)` 14:26:54.523 UTC, `am_kill … PID=6546` 14:26:54.529 → the phone `DISC`es MAESTRO 0x02 (A7219, 16:26:54.647; UA A7221) | User / OS | **BB-14 (force-stop) ✓** | film; after-force-stop system log 40206–40222; HCI |
| 16:27:02.8–16:27:08.9 | OpenControl reopened from the home screen: new process PID 12310 (`am_proc_start` 14:27:02.770); splash 16:27:04; "App control: connecting…" (16:27:06); re-open **LINK_BACK at app start** → `SABM` 0x02 (A7258) → ready 16:27:06.98 → announcement **ch 19** (A7274); "Battery unavailable" ×3, then L 77 % / R 76 % and "Case: Battery unavailable — Not reported yet — the Buds send the Case level only while a bud is charging in the case." (16:27:10); snapshot `Notify 01 e8 e8 20` (A7354, 16:27:08.91) | User / App | BB-14 ✓ [`PAIR-003`] | film; E2 1–41 |
| 16:27:16–20 | Quick Settings: tile **"ANC / Off"** (grey) — ≈ 9 s after ready (not within 3 s); never "Open the app" while ready | User / App | **BB-14 var.** (≈ 9 s, not ≤ 3 s) | film |
| 16:27:22 | Notification shade (privacy) | User | — | film |
| 16:27:24–44 | ANC tab (Off, no note); Sound tab: EQ −1 / 0 / 4 / 2 / 0 (Low bass … Upper treble) | User / App | — | film |
| 16:27:46–16:28:38 | **Gear** (from Sound) → Settings tab (Dark mode System) → Debug tab ("Unidentified frames (6)") → **Info** tab (build 043a09b, "Firmware (from the Buds' announcement, 16:27:07)", release_5.203 ×3, "Control channel: 19") → shade 16:28:08–10 (privacy) → Debug → **back arrow (16:28:38)** → **Sound** tab | User / App | **BB-16 ✓** | film; nothing from the app on the wire 16:27:46–16:28:38 (no app claim, no MAESTRO request) |
| 16:28:42–46 | Gear (from Sound) → Settings tab → **Dark mode: On** → dark at once | User / App | **K4d ✓ (On)** | film |
| 16:28:54.5–16:29:30 | **Back** → **Sound** (dark, 16:28:54.5), then a tap on Connection: the dark card with "Disconnect" in the card's own dark text (≈ 3.1:1 on film, the card's other text 2.8–3.1:1, FINDINGS §6) (16:28:56–29:00); ANC, Sound, Controls (press-and-hold Noise control both, 4 modes ticked, In-ear detection on), Sound (Balance Centre, Mono off, Conversation detection on), Find — all dark, readable | User / App | **K4d ✓**, F-7 ✓ | film 4 fps |
| 16:29:30–50 | Gear (from **Find**) → Settings (On) → Info (dark) → Debug (dark) → **back (16:29:50) → Find** (16:29:50.5), then a tap on ANC | User / App | **BB-16 rep. ✓** | film 4 fps |
| 16:29:52–16:30:32 | ANC tab (dark), Off, idle | — | — | film |
| 16:30:34–16:31:28 | Sound tab: **balance** dragged 32 times (labels after each release "Right 6", "Left 35", "Left 9", …, "Centre") → 32 × `WriteSetting 4:{17:…}`, each `RESPONSE` OK; **`17:7` (Right 4) never written**; the last write `4:{17:0}` (Centre, A7873, 16:31:27.68) | User / App | **BB-10 var. (not reached)** [`AUDIO-003`] | film; HCI A7723–7875; E2 52–147 |
| 16:31:42–16:31:49 | Phone picked up and **rotated to landscape**: the Sound tab still shown at 16:31:46.5, the **Connection** tab from 16:31:47.5 (activity relaunched 14:31:47.326 UTC; E2 148 "Permissions (start)") | User / App | **K4r var.** — the tab was **not** kept (Sound → Connection) | film 2 fps; logcat 372–374 |
| 16:32:04–12 | Phone laid down; portrait again 16:32:12 (activity relaunched 14:32:12.037 UTC), still Connection | User / App | K4r | film; logcat 387–389; E2 157 |
| 16:32:14–16:32:24 | Quick Settings: tile "ANC / Off" → **Bluetooth off** (16:32:16 tap; `STATE_TURNING_OFF` 14:32:16.820, `STATE_OFF` 17.651 UTC); the phone `Disconnect`s (A8040/8042, reason `0x16` A8071/8072); tile "ANC / Open the app"; **Bluetooth on** (tap 16:32:20; `TURNING_ON` 14:32:24.378, `ON` 24.960); HCI `.log` starts (B1 16:32:23.726) | User / OS | **K1 ✓, K2 ✓** — "Bluetooth is disabled." **not on film** (Quick Settings covered the app throughout) | film; system log; HCI |
| 16:32:17.0–17.6 | The app: link `UNKNOWN`, session lost, cause "undetermined (provisional …)" — the last cause line of this loss | App | K1 | E2 164–168 |
| 16:32:25–16:32:34 | The phone pages the Buds (B173) → ACL (B287) → link `CONNECTED` 16:32:29.44 → re-open LINK_BACK → `SABM` 0x02 (B700) → announcement **ch 19** (B742) → snapshot `Notify 01 e8 e8 20` (B936); Bluetooth dialog on film "Pixel Buds Pro 2 va… Verbinding maken…", "Charge 6 Verbonden", "Niro Opgeslagen" (16:32:28); Connection tab ready, L 76 % / R 76 %, Case unavailable (16:32:32) | OS / App | **K2 ✓** (re-opened by itself, no Connect tap) [`PAIR-003`] | film; HCI; E2 169–209 |
| 16:32:36 | Battery (i) (dark): "Left: 76% (updated 16:32:32) — not charging (out of the case) (16:32:32) / Right: 76% (updated 16:32:32) — not charging (out of the case) (16:32:32) / Case: Battery unavailable …" | User / App | [`BATT-004`] | film |
| 16:32:42–46 | Gear → Settings: **Dark mode: System** → light at once (Android is in light mode) | User / App | **K4d ✓ (System)** | film |
| 16:32:46–16:34:02 | The phone lies untouched on the Settings tab; footsteps away (audio 16:32:51–16:33:11) → **ACL `Disconnection Complete` reason `0x08`** (connection timeout, B1414, 16:33:28.700) → "link went down" (final cause); Bluetooth icon absent from the status bar ≈ 16:33:30–16:33:50; footsteps back (16:33:48–16:34:02) → **Buds' Connection Request** (B1445, 16:33:50.763) → re-open LINK_BACK → `SABM` 0x03 (B1865) → announcement **ch 19** (B1947) → snapshot `Notify … e8 e8 20` (B2068) | User / Buds / App | **K3 ✓** (`0x08`, not `0x13`) [`PAIR-003`] | film; audio; HCI; E2 210–255 |
| 16:33:59.5 | Status bar 16:33 → 16:34 | — | BB-11 (minute change) ✓ | 5-fps crops |
| 16:34:08–16:34:20 | Gear → **Debug** tab (Debug mode **still on**, "Unidentified frames (18)") → **Export debug log** → save-as (Downloads; privacy) → "Debug log saved (255 lines)" | User / App | **L3 var.** (the 2nd export, but Debug mode on — L3 not done) | film; logcat 14:34:16.973 |
| 16:34:20–16:34:50.5 | Debug tab, untouched; **film ends** 16:34:50.5 | — | BB-11 ✓ | film |
| 16:35:00–16:36:28 | After the film: app stopped (logcat 14:35:00.398 UTC); logcat and system log saved (16:35); HCI: PS `Notify … e8 80` (B2419) → Buds `DISC` 0x03 (B2422, 16:35:35.389) → ACL `0x13` (B2437, 16:35:36.945) → **the phone pages the Buds 0.1 s later** (B2438 `Create Connection` 16:35:37.044) → ACL (B2453) → `0x13` again (B2520, 16:35:39.353) — the buds docked (not on film) | User / OS | (after the film) | HCI; file times |

## Step mapping (skeleton Appendix A → what happened)

| Step | Result | Evidence |
|---|---|---|
| P1 install without clearing data | **done** — update install 15:42:56; the clear was code-cache-only (`flags=39`); Debug mode persisted | system log; E1 9 (hex from the first session) |
| P2 snoop on, Bluetooth off/on on film | **done** (16:13:00.8 off, 16:13:05.3 on) | system log; HCI A1 |
| P3 camera on phone, case, head | **done** (head with the mapping at every wear step 16:14–16:24:52; not on film after 16:25) | film |
| P4 Do Not Disturb | **not identifiable** — a ⊖ icon is in the status bar and the Quick Settings tile next to Bluetooth is on, but the tile's label is not legible; the shade was opened 4× (privacy) | film |
| P5 music | **not used** — AVRCP `PlaybackStatusChanged` "Paused" in every ACL, no AVDTP `Start` | HCI |
| P6 minute change start/end | **done** (16:13→14, 16:33→34) | 5-fps crops |
| P7 Info tab | **done** (16:13:32–36), repeated 16:28:00–34 and 16:29:34–38 | film |
| BB-1 both out, Refresh | **done** → `00` | A1652 |
| BB-2 Adaptive with `00` | **done** → one claim, `08 11` → `00`, no `08 12` | A1778–1807; E1 346 |
| BB-3 tile with `00` | **done** → one claim, no `08 12`, toast and subtitle "Not allowed now" | A1951–1981; E1 348–358 |
| BB-4 both worn, Adaptive | **done** → `08 11` → `e8` → `08 12 … 40` → ACK | A2177–2213 |
| BB-4t tile from the fresh `Notify` | **done** → `Notify … 40` → `08 12 … 20` (Adaptive → Off) → ACK | A2344–2378; E1 373 |
| BB-5 Settable over time | **done, 6 Refreshes** (≈ 25–171 s) → `00` every time | §Timeline; FINDINGS §3 |
| BB-8 Transparency, Off in the app | **done** → two claims, `08 11` first, ACK `80`, ACK `20` | A4855, A5007 |
| BB-9 Disconnect / Connect, (i) at once | **done differently**: no Disconnect or Connect tap in the whole run; the session re-opened by itself when a bud left the case (16:22:21); the (i) was opened ≈ 6 s after ready — the "last connection" words were not seen | E1 620–629; film 16:22:28 |
| BB-12 Left out with both worn on 19 | **not done** — on ch 19 with both worn (16:24:52 onwards) no bud was taken out on film | film; HCI (no `DISC` 16:24:52–16:32:17) |
| BB-12m Right out with both worn on 21 | **done, repeated** (16:16:46 → 19, 16:24:10 → 19), plus a variant (16:21:36, the Right from the ear into the case, on 19 → 19) and a new case (16:22:50, the Right **into** the ear with the Left on the table: 19 → 21) | FINDINGS §4 |
| BB-13 tile large | **done** (before the film, and again on film 16:25:06–30) | film |
| BB-14 force-stop, tile within 3 s | **done differently**: real force-stop ✓ (16:26:54); the tile read ≈ 9 s after ready: "ANC / Off" — never "Open the app" while ready | film; system log |
| BB-15 cut-off watch | **watched — no cut-off occurred**: 0 of 23 app claims had an answer after the claim's close; no "answer cut off" line | FINDINGS §2 |
| BB-16 gear, tabs, back | **done, repeated** (from Sound ×2, from Find): back returned to the tab it came from 3/3; nothing on the wire | film; HCI |
| K4d dark mode On / Off / System + Android's switch | **done differently**: On (16:28:44) and System (16:32:44) in the app, applied at once; **Off not chosen; Android's own dark switch not toggled** | film |
| BB-10 balance Right 4 | **done differently — not reached**: 32 writes, none `17:7`; ends at Centre `17:0` | A7723–7875 |
| K4r rotation | **done**: no crash; **the tab was reset Sound → Connection** by the activity relaunch | film; logcat |
| K1 / K2 Bluetooth off / on | **done** (16:32:16 / 16:32:20); "Bluetooth is disabled." not on film; re-opened by itself after on | film; HCI; E2 164–179 |
| K3 out of range | **done** → ACL reason **`0x08`**, re-open by itself on return | B1414, B1445; E2 210–226 |
| L3 export with Debug mode off | **not done** — the second export was made with Debug mode on | film 16:34:10 |
| K5 GrapheneOS auto-off | **not done** (setting not filmed); the system log shows the auto-off **disabled** (`delayMillis: 0` ×4, no alarm scheduled) | system log; FINDINGS §0 |
| A5 / (E) / B4 / Z1 | **not done** (no permission change, no unpairing, no picker, no bonding: 0 revoke / bond / CDM lines for the app or the Buds) | system log; HCI (no pairing events) |
| BB-11 minute change, stop | **done** (16:33→34; film ends 16:34:50) | film |
| A.3 after the run | exports ✓ (2), app logcat ✓ (2), system log ✓ (2), HCI ✓ (2), film ✓; **a bug report ran**: `dumpstate` takes the bug-report lock at 14:34:48.929 UTC (16:34:48.9, 2 s before the film ends; after-force-stop system log line 58014) — the Vanadium WebView SIGABRT at 14:35:11 is inside its `DumpForSigQuit` (crash buffer); the bug report itself is not in the folder | files; system log |

**Traceability (`AGENTS.md` §13.7):** every skeleton step (P1–P7, BB-1 … BB-5, BB-8 … BB-16, BB-4t, BB-12m, K4d, K4r, K1–K3, L3, K5, A5, (E), B4, Z1, A.3) appears
above with a result. Registry Test-IDs the skeleton names: exercised — `ANC-001` (BB-1, BB-3, BB-4t, BB-5, BB-8 Off), `ANC-003` (BB-2, BB-4), `ANC-004` (BB-8),
`INEAR-002`/`INEAR-003` (every bud into an ear, with the head side), `CASE-004` (the Left out of the case 16:17:26, 16:22:19), `CASE-005` (the Right out 16:17:24,
16:22:22), `CASE-006` (buds placed in the case 16:16:54, 16:16:58, 16:21:36, 16:21:44 — the lid stayed open),
`BATT-004` (every claim's `03 03` burst; the (i) at 16:22:28 and 16:32:36), `AUDIO-003` (BB-10, 32 writes), `PAIR-003` (every automatic re-open), `INEAR-004` (a bud out of an ear, not into the case: 16:14:38, 16:14:46, 16:16:46, 16:23:22, 16:24:10 → the
Buds' `Notify … e8 80` / `00 20` and, for the Right, a `DISC`). **Not
exercised:** `ANC-002` (Noise cancellation was not tapped in this run — only by the tile before the film, 15:56:43, no HCI), `PAIR-001`
(Z1 not done).

## Analysis checklist

- [x] Integrity pre-flight (both HCI logs, both of every log kind), hashes, audio (no speech), privacy (kept unblurred, the maintainer's choice).
- [x] Pre-filter by handle `0x000b`; LE `0x0041` (Fitbit) excluded; session-local DLCIs identified by content.
- [x] Clock offsets at two minute flips; logcat/system log − 2 h; the before/after-force-stop files mapped and their gaps covered.
- [x] P7: build `043a09b` = `ad0c4ba`'s app code, not "-dirty"; firmware lines and the control channel (21 at 16:13, 19 at 16:27) = the export's announcements.
- [x] Every ANC claim of the run (23 app, 26 Play services) against "Refuted if" — FINDINGS §2.
- [x] BB-5 Settable × time × physical state; every `Notify` against the film's wear state — FINDINGS §3.
- [x] Every announcement against which bud was out / worn, every Buds-side `DISC` with the ACL up — FINDINGS §4.
- [x] BB-14 tile frames; BB-15 cut-off watch with its command and a positive control; BB-16/K4d frames and the contrast measurement; StrictMode stacks.
- [x] VII: K1–K3 wire and logs; L3, K5, A5, (E), B4, Z1 not done (recorded).

---

## Appendix A — the planned procedure (skeleton as committed before the run, unchanged)


**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0060`, 2026-10-01; extended by `0061`; **adapted to the `ai-sessions/0062` build**,
the maintainer's outline in chat 2026-10-01, "CAP-066": *"Apply as outlined (Recommended)"*). After the run: rename this folder from the placeholder
`CAP-066-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BB` to the film's first/last overlay times and analyse it as `CAP-064` was (`ai-sessions/0060`).

**Purpose (the maintainer's choice in chat 2026-10-01, `ai-sessions/0060` checkpoint "To CAP-065" — `CAP-065` had already been run, so the leftovers go here):**

- **I — the real I-1 path (the disabled-tap re-check of `ai-sessions/0054`).** In `CAP-064` it was never entered: the app's last `Notify` read `e8`, so taps and
  the tile sent a `Set` directly (ACKed with the buds on the table, frame 2698; NAKed by the tile, 3440). This section first makes the app *see* `00`, then taps.
- **II — when does the Settable byte read `00`?** `CAP-064-FINDINGS.md` §3: with in-ear detection on it read `e8` for ≈ 28 s with both buds on the table
  straight from the case (2299–2759), and `00` after buds were taken out of the ears (3299, 3443). 🟡 hypothesis: `00` follows a removal from the ear or a dock,
  not "not worn" as such.
- **III — the ANC modes never tapped in OpenControl** (`ANC-002` Noise cancellation, `ANC-004` Transparency) and the I-4 "last connection" words in the battery (i)
  dialog within 2 s of a Connect (`CAP-064-FINDINGS.md` §2 I-4a).
- **IV — restore the balance** to its value before `CAP-064` (Right 4 = `17:7`; `CAP-064` ended at Centre `17:0`; `CAP-065` read `17:0` throughout).
- **V — lead L-1, the hosting bud** (added by `ai-sessions/0061`, maintainer-approved in chat 2026-10-01, "CAP-066": *"Apply as proposed (Recommended)"*).
  `CAP-065` (`CAP-065-FINDINGS.md` §4): one bud out ⇒ Left 19 / Right 21 (7/7, 🟢); once the channel changed 21 → 19 inside one ACL after a Buds-side `DISC`
  with both buds worn (🟡 "the channel names the bud that hosts the link"). This section takes the hosting bud out while the other stays worn.
- **VI — the ANC tile's subtitle (redo of `CAP-065` BA-2/BA-3).** In `CAP-065` the tile was in Android's compact form (icon only) — no subtitle on any frame —
  and the app was swiped away, not force-stopped, so A58-APP-01 (a fresh process before the first ANC report) was not exercised.
- **VII — the `CAP-065` robustness steps that were not done** (K4 rotation, K1/K2, K3, L3, K5, then destructive A5, (E), B4, Z1) — the maintainer's choice
  in chat 2026-10-01 ("Leftovers": *"Append to CAP-066, destructive last (Recommended)"*).
- **VIII — the `ai-sessions/0062` build** (added by `0062`): every ANC tap — the ANC tab's and the tile's — sends `08 11` first and the `08 12` only if that
  claim's `Notify` reads Settable non-zero (F-1); the new wording "The Buds don't allow changing noise control right now (usually because no bud is in an ear).
  Tapping a mode checks again first." and the tile subtitle "Not allowed now" (F-2); an answer cut off by the claim's close is "The answer was cut off — another
  app took the Buds' channel. Tap Refresh to see the current mode." with the mode dimmed and the (i) dot (F-3, watch only); a **gear** in place of the bug icon
  opens **Settings** with the tabs Settings (dark mode System / On / Off), Debug (unchanged) and Info (app version and build, firmware of Case / Left bud / Right
  bud, control channel) (F-4/F-5/F-6); the Connection card's Disconnect label in the card's own text colour (F-7).

**Changed by `ai-sessions/0061` (the same chat):** BB-6 and BB-7 dropped (`CAP-065` answered both: after wearing ⇒ `00`, e.g. frames 3641/7091; one docked
and one loose ⇒ `00`, frame 5707); BB-8 shrunk to the in-app Transparency and Off taps (Noise cancellation was tapped in the app at `CAP-065` F7, frame 10790;
all four modes went through the tile, 2567…3045).

**Facts the maintainer gave (chat 2026-10-01):** the maintainer does not speak on the films (no spoken hash or state); Play services' *Nearby devices* permission
is **allowed** (used for casting to a Chromecast). The build is identified from the logs; if the next FEATURE session adds the app build number to an Info
screen (its plan), film that screen once at the start. **It does since `ai-sessions/0062`:** gear → Info (A.0, P7).

---

### A.0. Which phone, which app, which build

| Item | Value |
|---|---|
| Phone | **Pixel 9a, GrapheneOS** |
| App under test | **OpenControl for Pixel Buds**, debug APK of the latest commit under `android/` (the `ai-sessions/0062` build or later) — the build is read from the **Info tab on film** (P7: "App: 0.1.0-dev, build <hash>[-dirty] (<commit date>)"), no hash to write down |
| Official Pixel Buds app | **Not used.** Pixel 7a: Bluetooth off |
| Play services *Nearby devices* | allowed (maintainer, 2026-10-01) |
| Buds | Pixel Buds Pro 2, firmware `release_5.203` |

### A.1. Preparation

| # | Check | Done |
|---|---|---|
| P1 | Debug APK installed **without** clearing the app's data | ☐ |
| P2 | Bluetooth HCI snoop log on; switch Bluetooth off and on **on film** | ☐ |
| P3 | Camera films the phone, the case and **your head** (ears visible for every wear step); the head-orientation rule of `CAP-064` applies (head on the right of the frame = Left bud) | ☐ |
| P4 | Do Not Disturb on; open the notification shade only if a step asks | ☐ |
| P5 | Music you know, playable offline | ☐ |
| P6 | Keep the status bar on film across a minute change at the start and at the end (the clock offset) | ☐ |
| P7 | After the first "ready": top bar **gear** → **Info** tab — hold it on film 3 s (app version and build, "Firmware (from the Buds' announcement, HH:MM:SS)", Case / Left bud / Right bud, "Control channel: N"); back | ☐ |

**Rhythm:** one action, then wait 5–10 s (longer where a step says so). Something unexpected: stop, wait 10 s, continue.

### A.2. Steps

DLCI numbers are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05 — whichever side opened the RFCOMM multiplexer).

### I. The real I-1 path (ANC tab and tile)

| Step | Pre-state | Action | Expected on screen | Expected on the wire |
|---|---|---|---|---|
| BB-1 | ready, both buds **worn** (on film), ANC tab | Take **both** buds out of the ears, lay them on the table **in view**; wait 20 s; tap **Refresh** | "The Buds don't allow changing noise control right now (usually because no bud is in an ear). Tapping a mode checks again first." appears; buttons stay enabled | `08 11` → `08 13 00 04 01 e8 00 …` (Settable `00`) |
| BB-2 | as BB-1, note shown | Tap **ADAPTIVE** (buds still on the table) | no mode change; the line "The Buds don't allow changing noise control right now (usually because no bud is in an ear)."; the (i)'s checked time moves | **one** claim: `08 11` → `08 13 … 00 …` → **no** `08 12` |
| BB-3 | as BB-2 | Quick Settings → **ANC tile** once (subtitle "Not allowed now" if the tile is large, BB-13) | no mode change; toast "The Buds don't allow changing noise control right now (usually because no bud is in an ear)." | one claim: `08 11` → `08 13 … 00 …` → no `08 12` |
| BB-4 | as BB-3 | Put both buds in the ears **on film**; wait 10 s; tap **ADAPTIVE** | Adaptive | one claim: `08 11` → `08 13 … e8 …` → `08 12 … 40` → ACK |
| BB-4t | both worn, mode Adaptive (BB-4), the tile large | Quick Settings → **ANC tile** once | the tile shows the mode **after the one the Buds just reported** (Adaptive → Off in the tile's cycle Noise cancellation → Transparent → Adaptive → Off) | one claim: `08 11` → `08 13 … e8 <mode>` → `08 12` with the next mode after **that** `<mode>` → ACK |

**Refuted if** (section I and every other ANC tap of the run, `ai-sessions/0062` F-1): an `08 12` without an `08 11` earlier **in the same claim**, or an `08 12`
after that claim's `Notify` read Settable `00`, or a tap that claims twice.

### II. When does Settable read `00`? (ears and buds in view; in-ear detection **on**)

| Step | Pre-state | Action | Expected | Expected on the wire |
|---|---|---|---|---|
| BB-5 | both buds in the case, lid open | Take both buds out and lay them **straight on the table** (do not wear them). Tap ANC **Refresh** at ≈ 10 s, 30 s, 60 s, 90 s, 120 s | say nothing; just tap | one `08 11` → `08 13 … <Settable> …` per Refresh — the result is the Settable value over time |
| ~~BB-6~~ | — | *dropped by `ai-sessions/0061`*: answered by `CAP-065` (after wearing ⇒ `00`, frames 3641, 7091, 5465) | — | — |
| ~~BB-7~~ | — | *dropped by `ai-sessions/0061`*: answered by `CAP-065` BA-6 (one docked, one loose ⇒ `00`, frame 5707) | — | — |

### III. ANC modes and the I-4 words

| Step | Pre-state | Action | Expected on screen | Expected on the wire |
|---|---|---|---|---|
| BB-8 | both worn, ANC tab | Tap **Transparency** (in the app, not the tile), wait 5 s, tap **Off** | each mode after the Buds' answer | per tap one claim: `08 11` → `08 13 … e8 …` → `08 12 … 80` → ACK; then `08 11` → `08 13 … e8 …` → `08 12 … 20` → ACK [`ANC-004`, `ANC-001`] (Noise cancellation: `CAP-065` F7) |
| BB-9 | ready, both buds in the case, lid open, Connection tab | **Disconnect**; take both buds out onto the table; **Connect**, and **immediately** tap the battery card's (i) | the (i) dialog shows the previous connection's lines marked "last connection" until the new report arrives (≈ 2 s), then "not charging (out of the case)" | app `SABM` MAESTRO; claim with `03 03 00 03 64 64 ff` |

### V. Lead L-1 — the hosting bud (`PROTOCOL.md` §2.2a 2026-10-01 🟡)

Read the channel from the debug export line "Maestro channel announced by the Buds: N" after each step.

| Step | Pre-state | Action | Expected | Expected on the wire |
|---|---|---|---|---|
| BB-12 | ready, both worn; the export's last announcement is **19** | Take the **Left** bud out of the ear onto the table (on film); wait 20 s | the app re-opens by itself if the Buds close the channel | 🟡 predicts: a Buds `DISC` of MAESTRO with the ACL up, then an announcement **21** (refuted if the session stays on 19 or comes back on 19) |
| BB-12m | ready, both worn, announcement **21** (e.g. after BB-12 and the Left back in) | Take the **Right** out onto the table; wait 20 s | as BB-12 | 🟡 predicts a Buds `DISC`, then **19** |

### VI. The ANC tile's subtitle (redo of `CAP-065` BA-2/BA-3, A58-APP-01)

| Step | Pre-state | Action | Expected on screen | Expected on the wire |
|---|---|---|---|---|
| BB-13 | before the run | Quick Settings → edit (pencil) → make the **ANC tile large** (two columns), so its subtitle shows | — | — |
| BB-14 | ready, both worn | Settings → Apps → OpenControl → **Force stop**; reopen; as soon as "ready" shows, pull down Quick Settings (within 3 s); read the tile | a mode or "Tap to switch", **never "Open the app"** while the app shows ready | app `SABM` MAESTRO; the snapshot claim's `Notify` may come later |

### VIII. The settings menu, dark mode, the cut-off watch (`ai-sessions/0062`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire |
|---|---|---|---|---|
| BB-16 | ready, on the **Sound** tab | Tap the **gear**; tap **Debug**, then **Info**, then **Settings**; then the top bar's back arrow | the title "Settings", three tabs; Debug = the Debug screen as before (Debug-mode switch, Export debug log, Unidentified frames); back returns to **Sound** | nothing from the app (opening the menu sends nothing) |
| K4d | ready, Android in **light** mode | Gear → Settings → **Dark mode: On**; back to the Connection tab (film the card ≈ 3 s, the **Disconnect** label close up); gear → **Off**; gear → **System**; then Android's own dark theme on and off in Quick Settings | On = dark at once, Off = light at once, System follows Android's switch; no restart, no crash; Disconnect legible on the card in the dark scheme (the `CAP-065` 11:23:50 frame measured ≈ 1.2:1) | — |
| BB-15 | *(watch only — cannot be provoked)* any ANC tap or Refresh while Play services re-opens its claim | — | if a claim is closed after `08 12`/`08 11` and before the answer: "The answer was cut off — another app took the Buds' channel. Tap Refresh to see the current mode."; after a cut-off `08 12` the mode buttons dimmed and the (i) dot, its first line "Not confirmed: the answer to the change at HH:MM:SS was cut off — the Buds may have switched. Tap Refresh."; **never** "The Buds didn't respond in time." for it; cleared by the next `Notify` | the app's `08 12`/`08 11`, then a phone `DISC` on the Message Stream DLCI before the Buds' ACK/`Notify` (as `CAP-065` 2640 → 2649 → 2651) |

### IV. Restore

| Step | Action | Done |
|---|---|---|
| BB-10 | Sound tab: drag the balance to **Right 4** (release) | `WriteSetting 4:{17:7}` → OK ☐ |
| BB-11 | *(after section VII)* Status bar across a minute change; stop the film | ☐ |

### VII. Robustness steps not done in `CAP-065` (from its section IV) — destructive steps last, after BB-10

| Step | Pre-state | Action | Expected on screen | Expected on the wire |
|---|---|---|---|---|
| K4r | ready | Rotate the phone to landscape and back | nothing lost, no crash | — |
| K1 | ready | Quick Settings: **Bluetooth off** | "Bluetooth is disabled." + Enable Bluetooth; no crash | — (the HCI log may stop) |
| K2 | after K1 | Bluetooth on; Connect if it does not connect by itself | ready | app `SABM` MAESTRO |
| K3 | ready, both worn | Walk out of range (another room, buds in the ears) for 30 s; come back | a clear loss text, then ready again | ACL `Disconnection Complete` reason `0x08` (timeout), not `0x13`; a re-open |
| L3 | — | Gear → **Debug** tab → **Debug mode off** → Export debug log (a second file) | no hex lines in it | — |
| K5 | — | *(optional)* GrapheneOS Bluetooth auto-off to the shortest; lock; wait; unlock; open the app; restore the setting | "Bluetooth is disabled." (normal), no crash | system log `BluetoothAutoOff` |
| A5 | — | *(destructive)* Settings → Apps → OpenControl → Permissions → *Nearby devices*: **Don't allow**; open the app; allow again | "Bluetooth permission needed" / "You denied the permission." + Allow | — |
| (E) | — | *(destructive)* Forget the Buds in Android's Bluetooth settings; open the app | "No Pixel Buds Pro 2 paired yet." + Pair a device | bond removal |
| B4 | after (E) | Open the lid; tap **Pair a device twice quickly** | one picker, no crash | — |
| Z1 | after B4 | Pair in the picker; Connect | ready | CDM association, bonding (SSP or CTKD, `PROTOCOL.md` §5.1) |

### A.3. After the run

Gear (Settings) → **Debug** tab → **Export debug log**; `adb bugreport`; the raw `btsnoop_hci.log`; app logcat and system log (GrapheneOS log viewer); the film.
All into this folder, then `sha256sum *`.

### A.4. Analysis checklist

- [ ] Pre-filter by the Buds' classic handle; DLCIs by content.
- [ ] P7: the Info tab frame — build hash against `git log`, firmware lines and the channel against the export's "Maestro channel announced" line.
- [ ] BB-1 … BB-4, BB-4t and **every** ANC tap of the run: each claim's `08 11`/`08 13`/`08 12` sequence against the "expected" column; **refuted if** an `08 12`
      has no `08 11` before it in the same claim, an `08 12` is sent after a `Notify` read `00` in the same claim, or a tap claims twice; BB-4t's `08 12` mode
      against the cycle applied to **that claim's** `Notify` mode.
- [ ] BB-5: a table Settable × time × physical state (film) — `CAP-064` read `e8` ≈ 28 s here, `CAP-065` `00` every time.
- [ ] BB-8: three ACKs, modes `08`, `80`, `20`.
- [ ] BB-9: the (i) dialog frames of the first 3 s against the claim/stream times.
- [ ] BB-12/BB-12m: each announcement's channel against which bud was taken out (film) and any Buds-side `DISC` with the ACL up.
- [ ] BB-14: the tile's subtitle frames against the export's `ConnectionState` lines.
- [ ] BB-15: any app claim closed by a phone `DISC` between the app's request and the Buds' answer — the screen text and the dimmed mode on film; the export's
      "answer cut off, not retried" line; no second `08 12` in that claim.
- [ ] BB-16 / K4d: no app frame while the menu is open; the scheme change per choice (frames); the Disconnect label's contrast on the dark card (pixel sample, as
      `ai-sessions/0062` RESULT §J).
- [ ] VII: `APP_TESTPLAN.md` sections K, L, A, B, E; K3's ACL reason code; Z1's bonding events.
- [ ] Registry Test-IDs: [`ANC-001`], [`ANC-002`], [`ANC-003`], [`ANC-004`], [`INEAR-002`]–[`INEAR-004`], [`CASE-004`]–[`CASE-006`], [`BATT-004`],
      [`AUDIO-003`], [`PAIR-003`], [`PAIR-001`] (Z1).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-066-2026-10-01_16-12-57_16-34-50-Group_BB/CAP-066-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-066-2026-10-01_16-12-57_16-34-50-Group_BB/CAP-066-EVENT-NOTES
