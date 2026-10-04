# Event Notes: OpenControl for Pixel Buds on Pixel 9a (GrapheneOS) — Group BA, robustness, UI and the `ai-sessions/0059` fixes (`CAP-065`)

**Status:** 🟢 **Video pass + full log analysis complete** (`ai-sessions/0061`). See `CAP-065-FINDINGS.md` for the wire-level analysis, the commands and
the raw bytes. The procedure the maintainer followed (the skeleton written by `ai-sessions/0059`) is kept unchanged as **Appendix A**; the timeline below
records what was **actually** done — the maintainer filmed the ears, did not speak, changed the order of some steps, repeated some and did not do the
robustness steps after K4 (K1–K3, K5, L2/L3 on film, A5, (E), B4, Z1).

**Purpose:** the hardware run of the `ai-sessions/0059` app fixes (the ANC tile right after Connect, the EQ sliders disabled until read), the Settable byte
with the ears in view (`DECISIONS.md` ADR-049, 🟡), lead L-1 (which pw_rpc channel the Buds announce when only the Left or only the Right bud is out,
`PROTOCOL.md` §2.2a), the `APP_TESTPLAN.md` robustness steps and the `ai-sessions/0057` pull-to-refresh checks.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-065`                     |
|      Group(s)    |                        BA                          |
|       Date       |                    2026-10-01                      |
| Firmware version | 🟢 `release_5.203` — in all 10 DLCI 0x02/0x03 announcements (e.g. HCI frame 884, debug export line 18) and on screen (battery (i) "Firmware: release_5.203", film 11:13:24–28) |
|   Test device    | Pixel 9a, GrapheneOS, Android 17 `CP3A.260905.009` (logcat header `google/tegu/tegu:17/CP3A.260905.009/2026092501`; Quick Settings build line on film 11:10:00). App: OpenControl `io.github.tedsluis.opencontrolpixelbuds:1`, targetSdk 34. **Build: the same installation as `CAP-064`** — the system log shows that app process PID 21818 (the `CAP-064` process, FINDINGS §0 there) ran until `am_kill … Reason=remove task` at 09:00:00.760 UTC (= 11:00:00.760, line 27374) and a new process PID 8072 started at 09:00:16.110 UTC (line 28757); no installer, data-clear or package line for the app between 07:44 UTC (the start of the events buffer) and the end of the log (`grep -c installer_clear_app_data` → 0). What that build is, is the `CAP-064` finding: at least the `0057` UI with the `0056` writes, installed 16 min after `b65085a`; no log carries a commit hash (FINDINGS §0). The maintainer did not speak on the film (chat 2026-10-01). Google Play services present (its Fast Pair holds DLCI 0x04/0x05 between the app's claims — 26 of the 51 Message Stream claims are Play services', 25 the app's (= the export's 25 "RFCOMM channel 0x04 connected" lines)). Play services *Nearby devices*: **allowed** — the maintainer's statement (chat 2026-10-01), not on film; consistent with Play services opening the Message Stream (system log `NearbyDiscovery: RfcommEventStreamMedium`, 09:27:10.453 UTC) |
| Other devices    | An LE device `c8:cc:a8:e7:48:93` (handle `0x0041`, LE Connection Complete frame 290, 11:07:37.907; dropped with reason `0x08` at 11:28:05.08, frame 12282, back at 11:28:09.57, frame 12323) — 🟢 the Fitbit "Charge 6" that Android's Bluetooth panel shows "Verbonden" on film 11:07:38: the system log's CDM presence events for association 37 of `com.fitbit.FitbitMobile` match it within 0.1 s (connected 09:07:37.987 UTC, disconnected 09:28:05.235, connected 09:28:10.081 — the three LE events above, − 2 h); excluded. (`CAP-064` had called it a speaker, 🟡.) The Bluetooth panel also lists "Niro" (saved, not connected). No other classic link |
| Video file       | `CAP-065-recording.mp4`: 1252.98 s, H.264 1280×720 rotation −90 (portrait), 37,393 video frames (r_frame_rate 179/6 ≈ 29.83 fps), **AAC stereo 44.1 kHz, 1252.61 s** (decodes); `creation_time` 2026-10-01T09:28:23Z. Burned-in overlay `Oct 1, 2026 HH:MM:SS`: **first frame 11:07:30, last frame 11:28:23** (full-resolution crops) |
| Audio            | Quiet (per-second RMS median −71.7 dBFS, 90th percentile −59.1, maximum −37.9 at t ≈ 705 s ≈ 11:19:15, the lid opening); 134 seconds > 12 dB above the median, broadband handling transients; no speech (the maintainer did not speak); a faint stable-pitch tonal pattern at t ≈ 1030–1120 s (≈ 11:24:40–11:26:10) is unidentified. Nothing heard inside the buds is on the track |
| Log file         | `CAP-065-btsnoop_hci.log` — `capinfos`: 12,771 packets, "Packet size limit: (not set)", 11:07:36.017203–11:30:07.973343; 0 `cap_len≠len`; 4 out-of-order pairs. The film covers phone time 11:07:30.5–11:28:23.5: the HCI log starts 5.5 s after the film's first frame (S1's Bluetooth-on) and runs 1 min 44 s past its last |
| App debug export | `CAP-065-opencontrol-debug-20261001-112907.txt` (the app's own file name; the skeleton planned CAP-065-debug-export.log twice, L2/L3): **one** export, 739 lines (738 newlines), 11:00:19.035–11:29:04.055, saved 11:29:07 (after the film; not on film). The first line is the new process's start (11:00:19, PID 8072), so the 20,000-line ring buffer did not wrap and the film window is inside it. Debug mode was on (film 11:11:50, 11:27:48): every inbound frame is hex-logged (outbound frames are not) |
| App logcat       | `CAP-065-OpenControl-for-Pixel-Buds-log-14015f5bc461.txt` (606 lines; buffers events/system/main interleaved; 08:04:34.908–09:28:30.331 UTC; `OpenControlBuds` lines 09:27:02–09:27:38 UTC only). No crash, ANR or tombstone line (the logcat ends at 09:28:30 UTC, before the bug report's process dumps); three `W System: A resource failed to call close.` (FINDINGS §0) |
| System log       | `CAP-065-System-log-a52da8511ec6.txt` (64,665 lines; 09-30 05:32 … 10-01 09:28:54.997 UTC; events buffer from 07:44:19 UTC). **Unlike `CAP-064`, it carries lines of the Bluetooth process** (PID 8499, uid 1002) — but only for 09:07 (its start) and 09:23–09:28 UTC |
| Events file      | none; no `CAP-065-btsnoop_hci.log.last` |
| Clock offsets    | **Film ↔ phone, measured at both ends** (5 fps crops of the status bar against the overlay): the minute flips 11:07→11:08 and 11:27→11:28 both between the 3rd and 4th 0.2-s frame of overlay second :59 ⇒ **phone = overlay + 0.5 s (±0.2 s), no drift**. **Logcat / system log (UTC) = phone − 2 h 00 min 00.000 s** (export 11:27:04.669 "RFCOMM channel 0x04 connect failed after 631 ms" = logcat 09:27:04.669, the same line; system log `on_cl_rfc_init: INIT unsuccessful` 09:27:04.665). HCI and the export are phone local time |
| Buds (partial, `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` (ADR-010). Classic handle `0x000b` for all seven ACLs of the run, **each created by the Buds' Connection Request** (frames 596, 1528, 3689, 4698, 7346, 8809, 9848) — the phone paged nobody. **Pre-filter:** `bluetooth.addr` is empty with this encapsulation; every command filters `bthci_acl.chandle==0x000b`. **DLCIs are session-local:** in ACLs where the phone opened the RFCOMM multiplexer MAESTRO = 0x02 and the Message Stream = 0x04; where the Buds opened it (their `SABM` DLCI 0x00: frames 1758, 4907, 7607, 10074) MAESTRO = 0x03, Message Stream = 0x05 — identified by content |
| Wear mapping     | The maintainer's rule (chat 2026-10-01): **head on the right of the frame = Left bud, head on the left = Right bud.** Slots: the bud in the **left-hand slot of the frame** is the Left bud. Cross-checked against the runtime-info stream's per-bud charging field (6.2.2 Left / 6.3.2 Right) at 11 dock changes (frames 1089, 3611, 4162, 4518, 5596, 6174, 6240, 8256, 9350, 10405, 10646): **0 contradictions** (FINDINGS §4) |

## Capture-integrity pre-flight

```
$ capinfos CAP-065-btsnoop_hci.log
File encapsulation:  Bluetooth H4 with linux header
Packet size limit:   file hdr: (not set)
Number of packets:   12 k   (Interface #0: Number of packets = 12771)
Earliest packet time: 2026-10-01 11:07:36.017203
Latest packet time:   2026-10-01 11:30:07.973343

$ tshark -r CAP-065-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len -e frame.time_epoch \
  | awk '$2!=$3{c++} {if(NR>1 && $4<p) o++; p=$4} END{print "packets:",NR," mismatches:",c+0," out-of-order:",o+0}'
packets: 12771  mismatches: 0  out-of-order: 4

$ ffprobe … CAP-065-recording.mp4   → audio aac 44100 Hz 2 ch 1252.608 s (53,945 frames); video h264 1280x720, r_frame_rate 179/6,
                                       37,393 frames, 1252.980 s, side data rotation −90; creation_time 2026-10-01T09:28:23Z

$ sha256sum *   (identical before and after the folder rename, `sha256sum -c`, 6/6 OK, 2026-10-01)
737a6414…3ec8  CAP-065-btsnoop_hci.log                         d7a6ccf9…f272  CAP-065-opencontrol-debug-20261001-112907.txt
30eac19d…b718  CAP-065-OpenControl-for-Pixel-Buds-log-…txt     3ee8de78…312b  CAP-065-recording.mp4
a4b83d6d…3721  CAP-065-System-log-a52da8511ec6.txt
```

File modes: the four files the phone produced were `-rwxr-----` (the HCI log already `644`); set to `644` on the maintainer's approval (chat 2026-10-01,
"Migration"). `git check-attr filter` → `lfs` for all five capture files.

## Video and audio review method

- **Every second:** the whole film was extracted at 1 fps (1,253 frames, `ffmpeg -vf fps=1,scale=405:720`) and viewed at 2 s spacing on 79 contact
  sheets (4 × 2); an `ffmpeg` scene-change pass (`select='gt(scene,0.05)'`, 71 changes) found no change outside the windows viewed. Transitions a claim depends
  on were narrowed: the status-bar minute flips (5 fps), the Quick Settings tile colour (2 fps, numerically: red − green of the tile area; grey = Off, red = a
  mode, `AncTile.kt:44`), the Sound tab across the 11:08:59 re-open (every frame, t = 88.4–90.2 s), the first and last overlay (full resolution). App
  actions with a log line (taps on the tile, Connect, Disconnect, pulls, Refresh) are timed from the export, not from the film.
- **Audio:** decoded to 16 kHz mono; per-second RMS over the full 1252 s and a full-length spectrogram (`ffmpeg … showspectrumpic`), zoomed at 270–340 s
  and 1000–1160 s. Used for nothing below.
- **Privacy (ADR-037):** no camera address overlay. The notification shade is legible at 11:09:56 and 11:22:52–11:23:01 (WhatsApp group and contact names
  with message snippets, LinkedIn with third persons' names, a bank's "€ 9,00 afgeschreven" notice, Play Integrity, USB debugging, Volkskrant); the
  Settings search for "darkmode"/"donker" at 11:23:08–36. The maintainer's own head, hair, glasses and ears are on film by design. **The maintainer chose to
  keep the film unblurred** (chat 2026-10-01, `ai-sessions/0061`, "Keep unblurred (Recommended)").
- **Wear:** each wear row names the side of the frame the head was on, or the slot the bud came from, and the bud that follows from the mapping.

## Event Timeline

Times are **phone local time** (HCI / debug export); film-only events are overlay + 0.5 s, to ±1 s (2-s sheets). Frame numbers are HCI frames of
`CAP-065-btsnoop_hci.log` (handle `0x000b`); "export N" is line N of the debug export. "Step" = the skeleton step (Appendix A); "var." = done differently,
"rep." = repeated. Registry Test-IDs in brackets. "PS" = a Message Stream claim by Google Play services (its first message is `03 08 00 02 01 25`); the
app's claims start with `08 11` or `08 12` (FINDINGS §3).

| Time (phone) | Action / event | Actor | Step / test | Evidence |
|---|---|---|---|---|
| 11:00:00.8 → 11:00:16 | The `CAP-064` app process (PID 21818) killed by "remove task"; new process PID 8072; activity created 11:00:19 (before the film) | User / OS | (before S0) | system log 27374, 27393, 28757; logcat `wm_on_create_called` 09:00:19.088 UTC; export 1 |
| 11:07:27.8 | Bluetooth switched **off** (no auto-off alarm was scheduled: `BluetoothAutoOff … alarm already scheduled: false`) — before the film | User | S1 | system log 31674–31705; export 8 (link `UNKNOWN` 11:07:28.275) |
| 11:07:30.5–11:07:34 | Film starts in Quick Settings: dialog "Bluetooth staat uit"; Bluetooth switched **on** (≈ 11:07:33) | User | S1 | film; Bluetooth process restarted (system log 31912, 09:07:34.188 UTC); HCI log starts 11:07:36.017 |
| 11:07:38–11:08:26 | Bluetooth list: "Charge 6 Verbonden", "Pixel Buds Pro 2 va Opgeslagen", "Niro Opgeslagen"; case closed above the phone; OpenControl stays resumed underneath (no `wm_on_*` between 09:00:31 and 09:10:41 UTC) | OS | — | film; LE Connection Complete 290; logcat |
| 11:07:59.5 | Status bar 11:07 → 11:08 (overlay :59 + 0.5 s) | — | S0 var. (nothing spoken) | 5 fps crops |
| 11:08:20.5 | **Lid opened, both buds docked**; no HCI event for the Buds until 11:08:27.10 | User | BA-1 var. | film; HCI (no event 290 → 596) |
| 11:08:22–28 | Left-slot bud taken out (Right stays docked); head on the **right** of the frame ⇒ **Left** into the ear (≈ 11:08:28–30) | User | BA-1 [`CASE-005`, `INEAR-002`] | film; stream 1089 6.3.2 = 2 (Right charging), 6.2.2 = 1 |
| 11:08:27.10 | **Buds' Connection Request** (596) → ACL (600) | Buds | — | HCI |
| 11:08:27.8–11:08:28.8 | **Opened by itself:** link `CONNECTED` → "Automatic re-open (ADR-044, trigger: LINK_BACK)" → app `SABM` 0x02 (863) → announcement **ch 19** (884) → reads 16, 2, 4, 7, 12, 17, 19, 22 (923…1049) → `SubscribeRuntimeInfo` (1066; Case 89, 1089); snapshot claim `08 11` (973) → `Notify 01 e8 00 20` (990, Left in the hand) | App | BA-1 [`PAIR-003`, `EQS-001`] | export 11–58 |
| 11:08:31.45 | Play services' claim: `Notify 01 e8 e8 40` — the Left bud is in the ear | Buds | [`INEAR-002`] | 1352 |
| 11:08:44–49 | Shade closed: Sound tab, EQ −1/0/4/2/0 (Low bass … Upper treble), sliders enabled, presets enabled (the EQ was read at 11:08:28.2, before the shade closed — the "disabled until read" moment is not on film here) | App | BA-1 | film |
| 11:08:51–53 | Left bud from the ear back into the left slot (both docked, lid open) → `Notify 01 e8 00 20` (PS, 1485) → ACL `0x13` (1498, 11:08:53.557) → session lost (export 59–64: "undetermined" → "the Buds closed the channel" → "Android's link … went down") | User / Buds | (not planned) [`CASE-006`] | film 11:08:52–56 "Not connected to the Buds. Controls are disabled …", sliders and presets grey |
| 11:08:58–59 | Left-slot bud out again, head on the **right** ⇒ **Left** into the ear → Connection Request (1528) → ACL (1532) | User / Buds | BA-1 rep. | film |
| 11:08:59.36–11:08:59.62 | Re-open (LINK_BACK) → Buds open the multiplexer (1758) → MAESTRO on **DLCI 0x03** (1837) → announcement **ch 19** (1883, 11:08:59.571) → ready 11:08:59.549 → `ReadSetting 4:16` (1904) → answer (1929, 11:08:59.609) | App | **BA-1 ✓** [`EQS-001`] | export 65–73; **film (every frame, t = 88.4–90.2 s): "Still connecting… Controls are disabled …" → the EQ card with its (i) marked not current and the note "Reading the Buds' current EQ… The sliders are off until it arrives; a preset can be chosen now." (`EqScreen.kt:217`) for ≈ 5 frames → enabled sliders**; the hand covers most slider tracks |
| 11:09:01.5–11:09:02.7 | App's snapshot claim: `Notify 01 e8 00 20` (2134) then `01 e8 e8 40` (2144) | Buds | [`INEAR-002`] | export 93–102 |
| 11:09:06–10 | Right-slot bud out; head on the **left** ⇒ **Right** into the ear; case empty, lid open; both worn | User | BA-1 [`CASE-005`, `INEAR-003`] | film; stream 2314 (none charging, 11:09:08.408) |
| 11:09:26–30 | EQ (i): "Equalizer — EQ updated: 11:08:59" | User / App | (i) | film; nothing on the wire from the app |
| 11:09:32–44 | "Balance and audio": Balance Centre, Mono off, Conversation detection on; (i) "read 11:09:00" ×3 | User / App | — | film |
| 11:09:56.5 | Notification shade (privacy) → Quick Settings page 2: the **compact** ANC tile (icon only, no label or subtitle at this size) **red** | User | **BA-2 var.**: 57 s after ready, not within 3 s; the subtitle is not visible on any frame | film |
| 11:10:00.185 | **Tile tap** "ADAPTIVE → OFF" → claim (attempt 1 fails 420 ms, 2nd at 2553) → `Set 01 e8 e8 20` (2567, **no `Get`**) → ACK (2579) → `Notify … 20` (2580); tile grey from ≈ 11:10:02 | User / App | BA-2 [`ANC-001`] | export 114–124 |
| 11:10:05.526 | **Tile tap** "OFF → ACTIVE" → claim 2613 → `Set … 08` (2640, 11:10:06.345) → the phone side sends `DISC` (2649, 11:10:06.494) and three `DM` — **the Buds' ACK (2651, 11:10:06.530) and `Notify … 08` (2654) arrive after the close**; the app logs "on-demand channel 0x04 closed" (export 132) and keeps OFF; tile stays grey | User / App / OS | BA-2 [`ANC-002`] | export 125–132; FINDINGS §3 |
| 11:10:08.9–09.7 | PS claim (2685): `Notify … 08` (2723) — the Buds are in Noise cancellation | Buds | — | HCI |
| 11:10:09.092 | **Tile tap** "OFF → ACTIVE" again → claim 2790 → `Set … 08` (2806) → ACK (2814) → `Notify … 08`; tile red from ≈ 11:10:11 | User / App | BA-2 rep. [`ANC-002`] | export 133–144 |
| 11:10:14.307 | **Tile tap** "ACTIVE → TRANSPARENT" → `Set … 80` (2869) → ACK (2879) → `Notify … 80` | User / App | BA-2 rep. [`ANC-004`] | export 145–155 |
| 11:10:20.024 | **Tile tap** "TRANSPARENT → ADAPTIVE" → `Set … 40` (3045) → ACK (3052) | User / App | BA-2 rep. [`ANC-003`] | export 156–167 |
| 11:10:38–42 | Quick Settings closed; Recents; the OpenControl card swiped away (activity destroyed 09:10:42.050 UTC; the process and the session stay) | User | **BA-3 var.** (no force-stop) | film; logcat `wm_on_destroy_called`; export 170–171 (observer stopped) |
| 11:10:46–48 | OpenControl reopened from the home screen: activity created 11:10:48.37; Connection tab "Connected to this phone (Android) ✓ App control: ready", L 100 % / Case 89 % / R 100 % | User / App | BA-3 var. | export 172–174; film |
| 11:11:08–24 | Quick Settings page 2: tile red | User | BA-3 | film |
| 11:11:24.433 | **Tile tap** "ADAPTIVE → OFF" → `Set … 20` (3337, no `Get`) → ACK (3346); tile grey from ≈ 11:11:26 | User / App | BA-3 [`ANC-001`] | export 177–188; film |
| 11:11:46–11:12:30 | Back to the app; Debug (top-bar icon): **Debug mode on** (already), "Unidentified frames (49)"; back | User | S2 (already on) | film |
| 11:12:34–38 | Right bud from the ear into the right slot ("Right 100 % ⚡") | User | (not planned) [`CASE-006`] | film; stream 3611 6.3.2 = 2 |
| 11:12:42–46 | Left bud from the ear into the left slot (both docked, lid open) → `Notify 01 e8 00 20` (PS, 3641, 11:12:43.04) → ACL `0x13` (3646, 11:12:45.965); card: "Connected to this phone (Android) / App control: not open yet … IOException: bt socket closed, read return: -1", then "Paired — not connected …" | User / Buds / App | BA-5 pre-state | export 197–203; film |
| 11:12:52–54 | **Right**-slot bud out onto the table right of the case (Left docked) → Connection Request (3689) → re-open (LINK_BACK; attempt 1 failed 84 ms) → announcement **ch 21** (4074) → ready; snapshot `Notify 01 e8 00 20` (4120); "Left 100 % ⚡" | User / Buds / App | **BA-11 by accident** (only the Right out) [`CASE-005`, `PAIR-003`] | export 204–245; stream 4162 6.2.2 = 2 |
| 11:12:56–58 | **Left**-slot bud out onto the table left of the case; **both on the table**, case open and empty | User | **BA-5** [`CASE-005`] | film; stream 4321 none charging |
| 11:13:20–28 | Battery (i): "Left: 100% (updated 11:12:56) — not charging (out of the case) (11:13:07) … Case: 89% — last seen 11:12:56 (no bud charging in the case) / The Buds report the Case level only while a bud is in the case. / Firmware: release_5.203" | User / App | — [`BATT-004`] | film |
| 11:13:30–34 | **Left** bud into the left slot; Right stays on the table | User | **BA-6** (BA-5 not yet refreshed) [`CASE-006`] | film; stream 4518 6.2.2 = 2 |
| 11:13:46–52 | Battery (i): "Left: 100% (updated 11:12:56) — charging in the case (11:13:34) / Right: … not charging (out of the case) (11:13:34) / Case: 89% (updated 11:13:34)" | User / App | — | film |
| 11:13:56–11:14:06 | ANC tab: "ANC can only be changed while you wear the Buds. Tapping a mode checks again first.", Off | App | BA-6 | film |
| 11:14:12–14 | Right bud into the right slot (both docked) → ACL `0x13` (4653, 11:14:14.830); "Not connected to the Buds …" | User / Buds | (not planned) [`CASE-006`] | export 261–266 |
| 11:14:20–22 | Right bud out onto the table, then the Left onto the table → Connection Request (4698) → re-open, announcement **ch 21** (4962) → snapshot `Notify 01 e8 00 20` (5280); the ANC note again | User / App | **BA-5 rep.** [`CASE-005`] | export 267–296; film |
| 11:14:31.6 | **ANC Refresh, both buds on the table** → claim 5443 → `Get` (5453) → **`Notify 01 e8 00 20`** (5465, 11:14:33.437) | User / App | **BA-5 ✓** [`ANC-001`] | export 306–314; film (finger on Refresh 11:14:30) |
| 11:14:42–46 | **Left** bud into the left slot; Right stays on the table | User | **BA-6 rep.** | film; stream 5596 6.2.2 = 2 |
| 11:14:58.4 | **Refresh** → `Get` (5694) → **`Notify 01 e8 00 20`** (5707, 11:15:00.485); (i) "ANC mode: OFF (updated 11:15:00) / ANC can only be changed while you wear the Buds (checked 11:15:00)…" | User / App | **BA-6 ✓** | export 319–329; film 11:15:04–10 |
| 11:15:30–34 | **Lid closed** with the Left bud inside, the Right on the table | User | **BA-7** | film; stream 5874 (11:15:34.649): `6:{3:{1:100 2:1}} 7:{1:0 3:0}` — **only the Right entry**, no Case, no Left |
| 11:15:51.3 | **Refresh** (lid closed) → `Get` (6007) → battery **`03 03 00 03 ff 64 ff`** (6015 — Left unknown) → **`Notify 01 e8 00 20`** (6019, 11:15:53.518); (i) "(checked 11:15:53)" | User / App | **BA-7 ✓** | export 332–340; film 11:15:50–12:02 |
| 11:16:14–16 | Lid opened (Left docked) | User | BA-7 → BA-8 | film; stream 6174 (11:16:19.21) 6.2.2 = 2, Case 89 again |
| 11:16:18–22 | Right bud from the table, head on the **left** ⇒ **Right** into the ear → PS `Notify 01 e8 e8 40` (6224, 11:16:23.618) | User / Buds | BA-8 [`INEAR-003`] | film |
| 11:16:26–30 | Left bud out of the slot, head on the **right** ⇒ **Left** into the ear → PS `Notify 01 e8 e8 20` (6257, 11:16:31.526); both worn | User / Buds | BA-8 [`INEAR-002`, `CASE-005`] | film; stream 6240 none charging |
| 11:16:40.2 | **Refresh, both worn** → `Get` (6322) → **`Notify 01 e8 e8 20`** (6334, 11:16:41.787) → the note disappears; (i) "ANC mode: OFF (updated 11:16:41) / Connection: Ready …" | User / App | **BA-8 ✓** | export 351–359; film 11:16:38–50 |
| 11:16:52–54 | Lid closed (empty case) | User | — | film |
| 11:17:00.6 | **Refresh** again → `Notify 01 e8 e8 20` (6532); (i) "updated 11:17:02" | User / App | BA-8 rep. | export 360–368; film 11:17:06 |
| 11:17:46.13 | **Buds `DISC` DLCI 0x03 and 0x05** (6720/6721) with the ACL up, both buds worn, no visible action → "Not connected …" on film 11:17:46 → re-open AFTER_LOSS 11:17:47.650 → announcement **ch 19** (6762) — the session channel changed 21 → 19 inside one ACL | Buds / App | (not planned) | export 373–411; film |
| 11:18:30.4 | **Disconnect** (both worn) | User | **BA-9** | export 415 (11:18:31.908); film 11:18:30 |
| 11:18:32–48 | Lid opened; a bud from an ear into the right slot (11:18:38–40), the other into the left slot (11:18:42–46); lid closed (11:18:46–48) → PS `Notify … 40` (7011) and `… 00 20` (7091) → ACL `0x13` (7094, 11:18:46.496); "Paired — not connected …" | User / Buds | BA-9 ✓ [`CASE-006`] | export 417; film |
| 11:18:48–11:19:15 | Lid closed, both docked, 27 s | User | BA-9 | film |
| 11:19:16–20 | Lid opened; **only the Left**-slot bud out, onto the table; Right docked | User | **BA-10** [`CASE-005`] | film; stream 8256 6.3.2 = 2 |
| 11:19:23.0–23.9 | Connection Request (7346) → ACL (7350) → link `CONNECTED`; **no re-open** (the Disconnect tap switched ADR-044 off): "App control: not open yet — tap Connect" | Buds / App | ADR-044 item 2 ✓ | export 418; film 11:19:24 |
| 11:19:52.25 | **Connect** → ready → announcement **ch 19** (8190); snapshot `Notify 01 e8 00 20` (8269) | User / App | **BA-10 ✓** [`PAIR-003`] | export 419–455 |
| 11:19:58.4 | **Disconnect** (Left still out) | User | BA-11 var. | export 456 (11:20:00.934) |
| 11:20:14.71 | **Connect** again (Left still out) → announcement **ch 19** (8509) | User / App | **BA-10 rep.** | export 458–495 |
| 11:20:18–22 | Left bud back into the left slot (both docked) → ACL `0x13` (8613, 11:20:20.896); lid closed → "Paired — not connected …" | User / Buds | BA-11 [`CASE-006`] | export 496–501; film |
| 11:20:22–50 | Lid closed, both docked, 28 s | User | BA-11 | film |
| 11:20:50–56 | Lid opened; **only the Right**-slot bud out onto the table; Left docked | User | **BA-11** [`CASE-005`] | film; stream 9350 6.2.2 = 2 |
| 11:20:57.9–58.9 | Connection Request (8809) → **re-open by itself** (LINK_BACK 11:20:58.666; enabled again by the 11:20:14 Connect tap) → announcement **ch 21** (9134) | Buds / App | **BA-11 ✓** (automatic, no Connect tap) | export 502–545 |
| 11:21:08–14 | Right bud back into the right slot → ACL `0x13` (9683, 11:21:13.195); lid closed | User / Buds | BA-11 → (repeat) | export 547–551; film |
| 11:21:28–34 | Lid opened; **only the Left**-slot bud out → Connection Request (9848) → re-open (LINK_BACK) → announcement **ch 19** (10123) | User / Buds / App | **BA-10 rep. (3rd)** | export 552–571; stream 10405 6.3.2 = 2 |
| 11:21:35.0–35.7 | Snapshot claim (10276): `Get` (10321), battery `64 e4` (10341), then the phone side `DISC` (10344, 11:21:35.235); the Buds' `Notify 01 e8 00 20` (10356) comes after the close; the app: "on-demand channel 0x04 closed" → **"The Buds didn't respond in time."** on the Connection and ANC tabs (film 11:21:36–11:22:10) | App / OS | (not planned) | export 568–575; film |
| 11:21:46–50 | Left bud from the table, head on the **right** ⇒ **Left** into the ear → PS `Notify 01 e8 e8 40` (10620) | User / Buds | F7 pre-state [`INEAR-002`] | film |
| 11:21:54–58 | Right bud out of the slot, hair on the **left** ⇒ **Right** into the ear → PS `Notify … e8 e8 20` (10676); both worn | User / Buds | F7 pre-state [`CASE-005`, `INEAR-003`] | film; stream 10646 none charging |
| 11:22:16.1 | **F7: "Noise cancellation" tapped** (ANC tab, availability unknown after the failed snapshot) → claim (attempt 1 fails 667 ms) → `Set 01 e8 e8 08` (10790, **no `Get`**) → ACK (10799, 11:22:18.003) → `Notify … 08`; **Home** at ≈ 11:22:17 (activity stopped 11:22:19.18); back via Recents 11:22:29.65 → "Noise cancellation" selected, the error line gone | User / App | **F7 ✓** [`ANC-002`] | export 584–606; logcat `wm_on_paused` 09:22:18.144, `wm_on_restart` 09:22:29.646; film 11:22:14–30 |
| 11:22:52–11:23:04 | Notification shade (privacy); Quick Settings; page 2 | User | — | film |
| 11:23:06–36 | Settings app: search "b", "dark", "darkmode" (no results), "donk" → "Donker thema" | User | K4 | film |
| 11:23:41–46 | **Dark theme on** → OpenControl recreated (activity destroyed/created 11:23:45.49/.53, no crash) | User / App | **K4 (dark mode) ✓** | logcat; export 614–616; film |
| 11:23:46–11:24:16 | All five tabs in dark mode: Connection (pink card, low-contrast "Disconnect"), ANC, Sound (EQ, presets), Controls, Find | User / App | K4 ✓ | film |
| 11:24:18–26 | Dark theme off → recreated (11:24:25.43/.45) → light | User / App | K4 ✓ | logcat; export 619–623 |
| — | **Rotation**: not done (portrait on every frame; no configuration change other than the two theme changes in the logcat) | — | **K4 (rotation) skipped** | film; logcat |
| 11:24:26–11:26:26 | Connection tab, unchanged for 2 min; no visible action; no ACL or RFCOMM change (the faint tonal audio pattern 11:24:40–11:26:10) | — | (K3 not done: no ACL loss, FINDINGS §6) | film; HCI |
| 11:26:29.6 | **Pull down on Controls** → `ReadSetting` **2, 4, 7, 12, 17, 19, 22** (11476 … 11497), each answered before the next, nothing else | User / App | **BA-12 ✓** [`INEAR-001`, `HOLD-001`] | export "Settings read" 11:26:31.797; film spinner 11:26:30 |
| 11:26:49.9 | **Pull down on Sound** → `ReadSetting 4:16` first (11536), then the same seven (11541 … 11561) | User / App | **BA-13 ✓** [`EQS-001`, `AUDIO-003`] | export 11:26:50.216–51.159; film 11:26:48–50 |
| 11:27:04.04 | **Pull down on Connection** → "Runtime info re-requested on Refresh" → `SubscribeRuntimeInfo` (11591) → `SERVER_STREAM` (11601); claim (attempt 1 collides) → `03 03 … 64 64 ff` → `Get` → `Notify … 08` | User / App | **BA-14 ✓** [`BATT-004`] | export 11:27:04.036–07.412; film 11:27:04 |
| 11:27:09.26 | **Pull down on Find** → `SubscribeRuntimeInfo` (11674) → stream (11684); claim 11699 → `Notify … 08`; **closed early at 11:27:10.45 by Play services' colliding connect** (system log 09:27:10.450 UTC) | User / App / OS | **BA-14 ✓** | export 11:27:09.255–10.558; system log |
| 11:27:16.7 | **Pull down on ANC** → claim (attempt 1 collides) → `Get` → `Notify … 08` (11894) | User / App | **BA-15 ✓** | export 11:27:17.474–20.376; film 11:27:16 |
| 11:27:28.98 | **Disconnect** (Connection tab) | User | **BA-16** | export 11:27:28.979; logcat 09:27:28.981 socket close channel=1 |
| 11:27:32–35.2 | Controls tab "Not connected …" → **pull** → "Still connecting…" → Connect sequence once: `SABM` 0x03, announcement **ch 19** (12062), the eight reads, `SubscribeRuntimeInfo` (12119), snapshot claim (12116, attempt 1 collides) → `Notify … 08` | User / App | **BA-16 ✓** [`PAIR-003`] | export 11:27:35.168–38.926; film 11:27:32–36 |
| 11:27:48–56 | Top bar → **Debug** ("Unidentified frames (149)", Export button) → back | User | **BA-17 ✓** (Debug; no (i) tapped in this step) | film; nothing from the app on the wire 11:27:39–11:28:23 |
| 11:27:59.5 | Status bar 11:27 → 11:28 | — | Z2 (minute change) | 5 fps crops |
| 11:28:23.5 | **Film ends** (Controls tab, session ready) | — | — | film |
| 11:28:29–30 | App stopped (`wm_on_stop_called` 09:28:30.323 UTC); logcat collected after this | User | — | logcat |
| 11:29:04–07 | App resumed; **debug export saved** (Debug mode on) — the only export | User | L2 var. / X1 | export last line; file name |

## Step mapping (skeleton Appendix A → what happened)

| Step | Result | Evidence |
|---|---|---|
| P1 hash aloud / written | **not done**: the maintainer does not speak on films; A.0 blank. Build: the `CAP-064` installation (same process until 11:00:00, no install line) | Log Metadata |
| P2 do not clear app data | **done** — no data clear or install in the log (events buffer from 07:44 UTC) | system log |
| P4 *Nearby devices* | not on film; **allowed** per the maintainer (chat 2026-10-01) | — |
| P6 events file | **not provided** | — |
| P11 auto-off timeout | not on film; the system log logs `delayMillis: 0` at both Bluetooth-on evaluations (09:07:36/37 UTC) and Bluetooth never went off by itself | system log 31943, 32021 |
| S0 minute change + spoken items | **done differently**: status bar on film across 11:07→11:08 and 11:27→11:28; nothing spoken | 5 fps crops |
| S1 force-stop, Bluetooth off → on | **done differently**: no force-stop (the process had been restarted by a task removal at 11:00:00); Bluetooth off at 11:07:27.8 (before the film), on at ≈ 11:07:33 on film | system log; film |
| S2 Debug mode on | **done** (already on), "Unidentified frames (49)" | film 11:11:50 |
| BA-1 sliders until EQ read | **done, repeated**: 1st open the EQ was read before the screen was seen; at the 11:08:59 re-open the "Reading the Buds' current EQ… The sliders are off until it arrives; a preset can be chosen now." note is on film between "Still connecting…" and the enabled sliders (slider tracks partly hidden by the hand) | film t = 88.4–90.2 s; 1904/1929 |
| BA-2 tile within 3 s of ready | **done differently**: 57 s after ready; the tile is the compact form — **no subtitle visible on any frame**; five taps, four ACKed (`ANC-001`, `ANC-002`, `ANC-004`, `ANC-003`), one ACKed by the Buds but lost to the app (11:10:05.5) | FINDINGS §2 |
| BA-3 force-stop, reopen, tile at once | **done differently**: the task was swiped away (activity destroyed, the process and session stayed — not a force-stop); tile tapped 36 s after reopening → ACK | logcat; export 172–188 |
| BA-4 watch-only lines | **done**: neither line occurred (`grep -c` 0, positive control in FINDINGS §2) | FINDINGS §2 |
| BA-5 both on the table, Refresh | **done** (2nd time; the 1st time, 11:12:56, without a Refresh): `00` | 5465 |
| BA-6 Left docked, Right on the table, Refresh | **done, repeated** (11:13:30 without Refresh, 11:14:42 with): `00` | 5707 |
| BA-7 lid closed, Right outside, Refresh | **done**: `00`; Left battery `ff` (unknown) while the lid was closed | 6015, 6019 |
| BA-8 both worn, Refresh | **done, repeated**: `e8`, the note disappears | 6334, 6532 |
| BA-9 Disconnect, both in the case, lid closed | **done** (link dropped 14.6 s after the Disconnect tap, 3 s after the lid closed) | 7094 |
| BA-10 only Left out, Connect | **done, repeated ×3** (11:19:52 Connect, 11:20:14 Connect, 11:21:34 automatic): ch 19 every time | 8190, 8509, 10123 |
| BA-11 only Right out, Connect | **done differently**: after the 11:20:14 Connect ADR-044 was on again, so the session opened **by itself** (11:20:58); also by accident at 11:12:54: ch 21 both times | 9134, 4074 |
| F7 ANC tap then Home | **done** (Noise cancellation, `Set … 08` → ACK; Home ≈ 1 s later; state kept on return) | 10790–10803 |
| K4 rotation | **skipped** (never rotated) | film; logcat |
| K4 dark mode on/off | **done**: two activity recreations, no crash, all five tabs viewed in dark | logcat 09:23:45, 09:24:25 |
| K1 / K2 Bluetooth off / on | **skipped** during the session (Bluetooth was only toggled at S1, before the app was used) | HCI; film |
| K3 out of range | **not identifiable / not done**: no ACL loss other than the six `0x13` drops on docking (FINDINGS §6); the film shows the phone untouched 11:24:26–11:26:26 with no Bluetooth change | HCI |
| L2 export (Debug on) | **done after the film** (11:29:07), off film | file name |
| L3 export with Debug off | **not done** (one export only) | — |
| K5 auto-off | **skipped** | system log (no auto-off) |
| A5 / (E) / B4 | **skipped** (no permission change, no unpairing, no pairing picker on film or in the logs: no bond-state or CDM line for the Buds) | system log; HCI (no pairing events) |
| BA-12 pull on Controls | **done** ✓ | 11476–11499 |
| BA-13 pull on Sound | **done** ✓ | 11536–11563 |
| BA-14 pull on Connection and Find | **done** ✓ (Find's claim closed early by Play services) | 11591…, 11674… |
| BA-15 pull on ANC | **done** ✓ | 11873–11894 |
| BA-16 Disconnect, pull on Controls | **done** ✓ | export 697–724 |
| BA-17 Debug, back, (i)s | **done** for Debug (11:27:48–56, nothing on the wire); (i)s were tapped earlier in the run (11:09:26, 11:09:38, 11:13:24, 11:13:46, 11:15:04, 11:15:58, 11:16:46, 11:17:06), none put anything on the wire | HCI |
| Z1 restore | **not needed / not done**: nothing was changed except the ANC mode (Noise cancellation at the end, Off at the start); no write in the whole run | MAESTRO: 0 `WriteSetting` |
| Z2 minute change | **done** (11:27→11:28 on film) | 5 fps crops |
| X1–X5 | export ✓ (one, 11:29:07), bug report ✓ (`dumpstate` takes the bug-report lock at 11:28:21.136, system log, 184 `dumpstate` lines 11:28), HCI ✓, logcat ✓, system log ✓, film ✓; no events file | system log |

**Traceability (`AGENTS.md` §13.7):** every skeleton step (S0–S2, BA-1 … BA-17, F7, K1–K5, L2/L3, A5, (E), B4, Z1/Z2) appears above with a result. Registry
Test-IDs the skeleton names: exercised — `ANC-001` (tile 11:10:00, 11:11:24; BA-5 Refresh), `ANC-002` (tile 11:10:09, F7), `ANC-003` (tile 11:10:20),
`ANC-004` (tile 11:10:14), `PAIR-003` (every automatic re-open and the Connect taps 11:19:52, 11:20:14, BA-16), `CASE-004` (lid closed with a bud inside,
BA-7, BA-9), `CASE-005` (every bud out of the case), `CASE-006` (every bud into the case), `BATT-004` (every claim's `03 03` burst; BA-14), `EQS-001`
(every Connect's `ReadSetting 4:16`; BA-13), `INEAR-001` (field 2 read at every Connect and in BA-12/13; no write), `HOLD-001` (field 7 read, Noise control
both buds; no write), `AUDIO-003` (field 17 read `0` = Centre; no write). **Not exercised:** `PAIR-001` (B4/Z1 skipped — no pairing), `EQP-002` (no preset
tapped), `AUDIO-001` (no music played: AVRCP "Stopped" in all seven ACLs).

## Analysis checklist

- [x] Integrity pre-flight, hashes, audio check (present, no speech).
- [x] Pre-filter by handle `0x000b`; the LE device `0x0041` excluded; session-local DLCIs identified by content.
- [x] Clock offsets measured at two minute flips (phone = overlay + 0.5 s); logcat/system log − 2 h.
- [x] MAESTRO decoded in full (266 pw_rpc packets, all CRC-valid); Message Stream message-level parse of all 51 claims (app and Play services).
- [x] Every `Notify` against the film's wear state (FINDINGS §3); every announcement against which bud was out (FINDINGS §4).
- [x] "Refuted if" lists applied literally (FINDINGS §2).
- [x] Session-end and lid-open tables (FINDINGS §1, §6).
- [x] Registry Test-IDs referenced above; the Capture Index row, Group BA and `id_registry.csv` are updated by `ai-sessions/0061`.

---

## Appendix A — the procedure as planned (the skeleton, written 2026-09-30 by `ai-sessions/0059`; unchanged except for heading levels and that the planned file names CAP-065-debug-export.log and CAP-065-events.txt, never produced, are no longer code-formatted)

### A.0. Which phone, which app, which build

| Item | Value |
|---|---|
| Phone | **Pixel 9a, GrapheneOS** (Settings → About phone → build number: `__________`) |
| App under test | **OpenControl for Pixel Buds**, debug APK of the commit that completes `ai-sessions/0059`. Commit hash: `__________` (`git log -1 --format=%H`) — **also say it aloud on film** (P1) |
| Official Pixel Buds app | **Not used.** Pixel 7a: **Bluetooth off** for the whole session |
| Other Bluetooth devices | Watch, car, speaker: off or out of range; write down anything that connects anyway |
| Buds | Pixel Buds Pro 2, firmware `release_5.203` (anything else is Safe Mode — stop and report) |

### A.1. Log Metadata (fill in)

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-065`                     |
|      Group(s)    |                        BA                          |
|       Date       |                                                     |
| Firmware version | (from the Connection tab "Firmware: …")             |
|   Test device    | Pixel 9a, GrapheneOS, build `…`; OpenControl commit `…`; Google Play services present: yes/no; Play services *Nearby devices*: allowed/denied (P4) |
| Video file       | `CAP-065-recording.mp4` — first/last overlay time: … / … |
| Log file         | `CAP-065-btsnoop_hci.log` — raw path used (`FS/data/log/bt/…` or `FS/data/misc/bluetooth/logs/…`) or `btsnooz.py`: … |
| App debug export | CAP-065-debug-export.log (Android's "save as" dialog — the whole log; Debug mode on from …) |
| App logcat       | `CAP-065-OpenControl-for-Pixel-Buds-log-<id>.txt` |
| System log       | `CAP-065-System-log-<id>.txt` |
| Events file      | CAP-065-events.txt (your own time + action notes, P6) |
| Clock offsets    | Film ↔ phone: minute change filmed at start (`__:__`) **and** end (`__:__`) — measured in the analysis |

### A.2. Preparation — before filming

The lessons of `CAP-063`/`CAP-064` are marked **(CAP-063)**.

| # | Check | Done |
|---|---|---|
| P1 | Build the debug APK of the commit in A.0 (`cd android && ./gradlew assembleDebug`), install it (`adb install -r app/build/outputs/apk/debug/app-debug.apk`). Write the hash in A.0 **and say it aloud on film at the start** **(CAP-063: not recorded)** | ☐ |
| P2 | Do **not** clear the app's data before the run (A5/(E)/B4 at the end revoke and re-pair on purpose) | ☐ |
| P3 | Developer options: **Bluetooth HCI snoop log = Enabled**, **USB debugging = on**; switch Bluetooth **off and on on film** (the log starts after a toggle) | ☐ |
| P4 | Google Play services → Permissions → *Nearby devices*: write **allowed / denied**, **say it aloud on film**, leave it **(CAP-063: not recorded)** | ☐ |
| P5 | Buds and case charged (> 50 %); buds in the case, lid closed, at the start | ☐ |
| P6 | **Events file** ready (notes app on another device, or paper): phone time (HH:MM:SS) + step ID for every action **(CAP-063: none)** | ☐ |
| P7 | **Camera** on a tripod films the phone screen **and** the case/buds **and your head** in one frame; for II, the ears and the table must be visible — turn your head to the camera when a bud goes in or out **(CAP-063: "worn" never on film)**. No location/address overlay | ☐ |
| P8 | Camera microphone on; **say aloud** what you hear. The phone's own audio is not on the film **(CAP-063)** | ☐ |
| P9 | **Do Not Disturb on**; open the notification shade only when a step asks **(CAP-063: names and an e-mail address were readable in the shade)** | ☐ |
| P10 | Music you know, playable offline | ☐ |
| P11 | GrapheneOS Settings → Security → *Bluetooth auto-off timeout*: write the current value `______` (K5) | ☐ |
| P12 | Laptop with `adb` next to you for A.6 (bug report within a minute of the last action) | ☐ |
| P13 | Read this whole file once before starting | ☐ |

**Rhythm for every step:** wait ~5 s → note the time → **one** action → wait 5–10 s → next step. Something unexpected: **stop, say it aloud, write the time**.

### A.3. Start of the film

| Step | Action | Expected on screen | Time (phone) | Result |
|---|---|---|---|---|
| S0 | Film the status bar across a **minute change** (≥ 10 s before and after). Say aloud: date, "CAP-065, Group BA", the commit hash, Play services *Nearby devices* state | — | | |
| S1 | Force-stop OpenControl (Settings → Apps → Force stop). Quick Settings: Bluetooth **off**, then **on** (starts the HCI log). Do **not** open the app yet | — | | |
| S2 | Open OpenControl; top bar **bug icon → Debug**: **Debug mode on**; back | "Unidentified frames (n)" appears | | |

### A.4. Steps

"Expected on the wire" is for the analysis. DLCI 0x02 = the app's session (MAESTRO), 0x04 = the Message Stream (claimed on demand).

#### I. The `ai-sessions/0059` app fixes

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| BA-1 | buds in the case, lid closed, app on the Sound tab | Open the lid, take both buds out, put them **in your ears** (on film). Watch the Sound tab from the moment the session opens until the EQ shows "read HH:MM:SS" | the five EQ sliders are **disabled** (greyed) until the EQ is read, then enabled at the Buds' values; the presets are enabled throughout (A58-APP-02) | app `SABM` 0x02 → the Buds' `GetSoftwareInfo` → `ReadSetting 4:16` → `RESPONSE 4:{16:…}` | | |
| BA-2 | ready, buds worn | Pull down Quick Settings **within 3 s of "ready"**; read the ANC tile aloud; tap it once | subtitle shows a mode or "Tap to switch" — **never "Open the app"** while the app shows ready (A58-APP-01); the tap switches the mode | one DLCI 0x04 claim: `08 11` → `08 13 … e8 …` → `08 12 …` → ACK | | |
| BA-3 | ready | Force-stop the app; reopen; Connect if it does not connect by itself; **immediately** pull down Quick Settings | as BA-2: the tile follows the session at once, even before the snapshot's ANC report | — | | |
| BA-4 | ready | *(watch only, nothing to provoke)* After the run, search the debug export for "answered with a value this app cannot read" and "late WriteSetting answer" | — | if either line exists: the `ReadSetting`/`WriteSetting` frame and its answer in the HCI log | | |

#### II. The Settable byte with the ears visible (ADR-049)

Pre-state for the section: ready, ANC tab. For each step say aloud where each bud is. **Ears and table in view.**

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| BA-5 | both worn | Take both buds out and lay them on the table **in view**, case open and empty; wait 10 s (the session may re-open by itself — wait for "ready"); ANC tab → **Refresh** | say which: the "ANC can only be changed while you wear the Buds…" line appears or not | `08 11` → `08 13 00 04 01 e8 <Settable> <mode>` — 🟡 predicts `00`; `CAP-048` 11939 read `e8` in this situation | | |
| BA-6 | both on the table | Put the **Left** bud into its slot in the open case (Right stays on the table, not worn); wait 10 s; **Refresh** | as BA-5 — say which | as BA-5 — 🟡 predicts `00`; `CAP-045` 612 read `e8` in this situation | | |
| BA-7 | Left docked, Right on the table | Close the lid **only if** the Right bud is outside it (it is); wait 10 s; open the lid; **Refresh** | as BA-5 | as BA-5 | | |
| BA-8 | — | Put both buds in your ears (on film); **Refresh** | no "can only be changed…" line | Settable `e8` | | |

#### III. Lead L-1 — which channel do the Buds announce?

Read the channel from the debug export line "Maestro channel announced by the Buds: N" (and the HCI log, `scripts/pwrpc_decode.py`: the first `GetSoftwareInfo`
`RESPONSE` with `call_id 0xFFFFFFFF`).

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| BA-9 | ready | **Disconnect**. Put **both** buds in the case, close the lid, wait 15 s (the link drops) | "Paired — not connected …" | ACL `Disconnection Complete` | | |
| BA-10 | both docked, lid closed | Open the lid, take **only the Left** bud out (Right stays docked); **Connect** | ready | the announcement's `channel_id`: 🟡 predicts **19** (or 24) — Left Bluetooth core | | |
| BA-11 | Left out, Right docked | **Disconnect**; Left back in, lid closed, 15 s; open, take **only the Right** out; **Connect** | ready | 🟡 predicts **21** (or 26) — Right Bluetooth core. A different pairing refutes "the channel names the hosting bud" | | |

#### IV. Robustness steps moved from `CAP-064` VI (`APP_TESTPLAN.md`) — destructive steps last

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| F7 | buds worn, ANC tab | Tap **ACTIVE**, press Home **immediately**; come back after 5 s | ACTIVE applied (audible), no stuck state | `08 12 … 08` → ACK; later `DISC` 0x04 | | |
| K4 | ready | Rotate the phone; dark mode on and off | nothing lost, no crash | — | | |
| K1 | ready | Quick Settings: **Bluetooth off** | "Bluetooth is disabled." + Enable Bluetooth; no crash | — | | |
| K2 | after K1 | Bluetooth on; Connect if it does not connect by itself | ready | app `SABM` 0x02 | | |
| K3 | ready | Walk out of range (another room) for 30 s, come back | a clear loss text, then ready again | ACL loss; re-open | | |
| L2 | — | Bug icon → Debug: **Export debug log** → save as CAP-065-debug-export.log | "Debug log saved (N lines)." | — | | |
| L3 | — | **Debug mode off**, export again (a second file) | no hex lines | — | | |
| K5 | — | *(optional, last before K-destructive)* auto-off timeout to the shortest (P11), lock, wait, unlock, open the app; restore the timeout | "Bluetooth is disabled." (normal), no crash | — | | |
| A5 | — | *(destructive, end)* Settings → Apps → OpenControl → Permissions → *Nearby devices*: **Don't allow**; open the app | "Bluetooth permission needed" / "You denied the permission." + Allow; allow again afterwards | — | | |
| (E) | — | *(destructive, end)* Forget the Buds in Android's Bluetooth settings; open the app | "No Pixel Buds Pro 2 paired yet." + Pair a device | — | | |
| B4 | after (E) | Open the lid; tap **Pair a device twice quickly** | no second picker, no crash; then pair normally | — | | |

#### V. Material 3 overhaul, pull to refresh and the settings re-read (moved from `CAP-064` VIII, `ai-sessions/0057`)

Run section V **before** the destructive steps A5, (E), B4 of section IV (do IV in order up to K5, then V, then A5, (E), B4).

Pre-state: ready, both buds worn, music playing. Wait ≥ 5 s between pulls and **say each pull aloud** with the tab name. The detailed screen checks are
`APP_TESTPLAN.md` section O; this section is what the **HCI log** must show.

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| BA-12 | Controls tab | **Pull down** once | spinner, then the (i) "read HH:MM:SS" times move | DLCI 0x02 exactly **seven** `ReadSetting` requests `4:2, 4:4, 4:7, 4:12, 4:17, 4:19, 4:22` in that order, each answered before the next (≤ 2 s), **nothing else** from the app (no write, no `SubscribeRuntimeInfo`, no DLCI 0x04 claim) | | |
| BA-13 | Sound tab | **Pull down** once | spinner; EQ and settings times in the (i) move | `ReadSetting 4:16` first, then the same seven reads, in order, nothing else | | |
| BA-14 | Connection tab, then Find tab | **Pull down** on each | new battery times or "No new battery reading…" | per pull: one DLCI 0x04 claim (`SABM` → `03 03 …` → `08 11`/`08 13`) released ≈ 1.5 s later, and one `SubscribeRuntimeInfo` on DLCI 0x02 — as a *Refresh battery* tap (E8) | | |
| BA-15 | ANC tab | **Pull down** | the (i) "updated" time moves | one DLCI 0x04 claim with `08 11` → `08 13`, as the Refresh button | | |
| BA-16 | — | **Disconnect**, then pull on Controls | the app connects | `SABM` DLCI 0x02, then the normal Connect sequence (EQ read, the seven reads, subscription, the snapshot claim) — once | | |
| BA-17 | ready | Bug icon → Debug → system back; tap a few (i)s | Debug full screen, back to the tab; dialogs open/close | **nothing** on any channel | | |

**Refuted if (section VIII):** a pull sends anything other than its tab's action; a read is repeated in the same pass (a retry) or out of order; a pull
sends anything while the app is still connecting; opening Debug or an (i) puts anything on the wire.

#### Z. Restore

| Step | Action | Time | Done |
|---|---|---|---|
| Z1 | After B4: pair again, Connect; allow *Nearby devices* again (A5); ANC as you like it | | ☐ |
| Z2 | Film the status bar across a **minute change** again, then stop the film | | ☐ |

### A.5. What you must not do

- Do not use the official Pixel Buds app, or the Pixel 7a's Bluetooth, during this session.
- Do not tap Connect where a step does not ask for it (BA-1, BA-5): those test the automatic behaviour.
- Do not do two actions in one step; do not skip the pauses.
- Do not open the notification shade unless a step asks (DND is on) — the Quick Settings steps (BA-2, BA-3, K1, K5) are the only ones.
- Do not run A5, (E) or B4 before section V is done.

### A.6. After the run — within 1 minute of the last action

| # | Collect | Done |
|---|---|---|
| X1 | Bug icon → Debug → **Export debug log** (Debug mode on) → save as CAP-065-debug-export.log (L2 made one earlier; export again now) | ☐ |
| X2 | `adb bugreport cap065`; the raw snoop log from `FS/data/log/bt/btsnoop_hci.log` or `FS/data/misc/bluetooth/logs/btsnoop_hci.log` (else `btsnooz.py`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3) → `CAP-065-btsnoop_hci.log`; write the path in A.1 | ☐ |
| X3 | App logcat and system log (GrapheneOS log viewer) | ☐ |
| X4 | Film → `CAP-065-recording.mp4`; its first/last overlay time in A.1 | ☐ |
| X5 | Everything, with this file and the events file, into this folder; rename the folder; `sha256sum *` | ☐ |

### A.7. Analysis checklist

- [ ] Integrity pre-flight (`capinfos`, `cap_len≠len`, out-of-order, `sha256sum`); audio track present or not.
- [ ] Pre-filter by the Buds' classic ACL handle (map it to the Buds' address via HCI Connection Complete) — `bluetooth.addr` is empty with this
      encapsulation (`AGENTS.md` §13.1).
- [ ] Film ↔ phone offset from the two filmed minute changes; logcat/system log UTC offset.
- [ ] A negative needs a positive control (`AGENTS.md` §13 step 8): every "0 frames" with its command, exit status and a filter that matches a known frame.
- [ ] I: BA-1 film frames of the Sound tab against the `ReadSetting 4:16` answer time; BA-2/BA-3 tile subtitle against `connectionState` in the export.
- [ ] II: the Settable byte of each `Notify` against the film (which bud is where, ears visible) — a counter-example either way is the result (ADR-049 🟡).
- [ ] III: `python3 scripts/pwrpc_decode.py CAP-065-btsnoop_hci.log | grep GetSoftwareInfo` — the `ch=` of each announcement against which bud was out.
- [ ] IV/V: as `CAP-064`'s old sections — `APP_TESTPLAN.md` sections K, L, A, B, E and O.
- [ ] Traceability (`AGENTS.md` §13 step 7): every step has a timeline row or an explicit "skipped". Test-IDs: `ANC-001`–`ANC-004` (BA-2/3, BA-5–8, F7),
      `PAIR-001` (B4, Z1), `PAIR-003` (BA-9–11, K2), `CASE-004`–`CASE-006` (BA-6, BA-9–11), `BATT-004` (BA-14), `EQS-001`/`EQP-002`
      (BA-1, BA-13), `AUDIO-001`/`AUDIO-003`, `HOLD-001`, `INEAR-001` (BA-12 reads).

**Refuted if:**
- the ANC tile shows "Open the app" while the app shows the session ready (BA-2/BA-3);
- an EQ slider is enabled before the EQ is read, or a slider write leaves while the EQ is unread (BA-1);
- a `Notify` reads Settable `0x00` while a bud is visibly in an ear, or `0xe8` while no bud is in an ear (BA-5 … BA-8) — the latter refutes ADR-049's 🟡;
- the announced channel does not follow the bud that is out (BA-10/BA-11) — refutes L-1's "names the hosting bud" (the address derivation itself stays as tabulated);
- any app frame on DLCI 0x08; any app `SABM` 0x02 while the app is not visible; a pull sends anything other than its tab's action (V);
- a settings value changes on screen without the Buds' `RESPONSE`.


---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-065-2026-10-01_11-07-30_11-28-23-Group_BA/CAP-065-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-065-2026-10-01_11-07-30_11-28-23-Group_BA/CAP-065-EVENT-NOTES
