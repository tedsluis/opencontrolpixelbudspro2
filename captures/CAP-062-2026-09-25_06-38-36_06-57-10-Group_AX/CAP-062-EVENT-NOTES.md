# Event Notes: OpenControl for Pixel Buds on Pixel 9a (GrapheneOS) — Group AX, `APP_TESTPLAN.md` run of the `ai-sessions/0046` build (`CAP-062`)

**Status:** 🟢 **Video pass + full log analysis complete** (`ai-sessions/0047`). See `CAP-062-FINDINGS.md` for the wire-level analysis, the
commands and raw bytes, and the answers to the maintainer's five observations.

**Purpose (maintainer, chat 2026-09-25):** one run through `APP_TESTPLAN.md` (A1 … L3) with the `ai-sessions/0046` build, which is also the
hardware re-test of `ai-sessions/0046` RESULT §9 (R1–R8). The maintainer reported afterwards: (1) Find My Buds does not work with the buds in the
case; (2) the app keeps losing its session while Android stays connected; (3) with the buds in the case, ANC/EQ changes show "The Buds refused the
command (not allowed in the current state)"; (4) after taking buds out, the app often does not show which bud is in and which is out; (5) when the
buds go back in the case the app disconnects and does not show which bud is in the case and its battery.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-062`                     |
|      Group(s)    |                        AX                          |
|       Date       |                    2026-09-25                      |
| Firmware version | 🟢 `release_5.203` — in every DLCI 0x02 announcement (e.g. HCI frame 2768; debug export line 24) and shown on screen ("Firmware: release_5.203", film t = 150 s) |
|   Test device    | Pixel 9a, GrapheneOS, Android 17 `CP2A.260805.005` (logcat header `google/tegu/tegu:17/CP2A.260805.005/2026091901`). App: OpenControl for Pixel Buds — the `ai-sessions/0046` code (`7498cbc`): log wording that exists only from that commit on is present ("Runtime info requested (channel 19)", export line 30; `SubscribeRuntimeInfo` REQUEST frame 2777), no app activity on DLCI 0x08, no "Safe Mode: write refused" line. The commit hash itself was not written down (P1). Google Play services and the Google app present. Play services' *Nearby devices* permission state: **not recorded** (P4) |
| Video file       | `CAP-062-recording.mp4`: 1115.19 s (`ffprobe`), H.264 1280×720 rotation −90 (portrait), 33,311 decoded frames (29.87 fps average), AAC audio. Burned-in overlay `Sep 25, 2026 HH:MM:SS`, timestamp only: first frame 06:38:36, last frame 06:57:10 |
| Log file         | `CAP-062-btsnoop_hci.log` — `capinfos`: 11,456 packets, "Packet size limit: (not set)" (raw path), 2026-09-25 06:38:41.863250–07:00:15.793623 (1293.93 s). 0 `cap_len≠len`. One 0.28 ms out-of-order pair (frame 4907) |
| App debug export | `CAP-062-debug-export.log` (731 lines, 06:38:50.494–06:56:44.568, phone local time; Debug mode on from ≈ 06:39:03, so it carries the raw frames) |
| App logcat       | `CAP-062-OpenControl-for-Pixel-Buds-log-091e23cb54d0.txt` (256 lines; lifecycle events from 04:28:31 UTC; the `OpenControlBuds` main-buffer lines only from 04:59:24 UTC, after the session) |
| System log       | `CAP-062-System-log-8bfd96877cca.txt` (71,598 lines; the session window 04:38–04:57 UTC is covered) |
| Buds (partial, `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` (same unit as every capture). Classic ACL handle `0x000b` (reused by every reconnect), LE handle `0x0042`. **Pre-filter (`AGENTS.md` §13.1):** `bluetooth.addr` is empty with this `H4 with linux header` encapsulation, so every command filters by handle — `bthci_acl.chandle==0x000b` / `==0x0042`, mapped to this address by HCI Connection Complete frame 1179 and LE Enhanced Connection Complete frame 1068. LE handle `0x0041` is another device (`c8:cc:a8:e7:48:93`, frame 264) and is excluded |
| Clock offsets    | **Film ↔ phone, measured at start and end** (10 fps crops of the status bar against the overlay): the phone's minute flips 06:39→06:40 at t = 83.1 s, 0.3 s after the overlay shows 06:39:59 (t = 82.8 s); 06:56→06:57 at t = 1103.1 s, again 0.3 s into overlay 06:56:59. So **overlay = 06:38:36.2 + t** and **phone clock = overlay + 0.7 s (±0.1 s), no drift**. **Logcat / system log (UTC) = export − 2 h 00 min 00.0 s** (e.g. `wm_on_activity_result_called` 04:39:39.614 = export line 10 "picker dismissed" 06:39:39.610). HCI timestamps are the phone's local clock |

## Capture-integrity pre-flight

```
$ capinfos CAP-062-btsnoop_hci.log
File encapsulation:  Bluetooth H4 with linux header
Packet size limit:   file hdr: (not set)
Number of packets:   11 k   (Interface #0: Number of packets = 11456)
Earliest packet time: 2026-09-25 06:38:41.863250
Latest packet time:   2026-09-25 07:00:15.793623

$ tshark -r CAP-062-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len -e frame.time_epoch \
  | awk '$2!=$3{c++} {if(NR>1 && $4<p) o++; p=$4} END{print "mismatches:",c+0,"out-of-order:",o+0}'
mismatches: 0 out-of-order: 1

$ sha256sum *   (before and after the move into this folder — identical)
249a9713…6b8ebf  CAP-062-btsnoop_hci.log          f6e300f7…81b3dd  CAP-062-debug-export.log
8cbcbb79…232207  CAP-062-OpenControl-…-091e23cb54d0.txt   1d9a2c49…6cc7c  CAP-062-recording.mp4
8168fefe…062888  CAP-062-System-log-8bfd96877cca.txt
```

## Video review method

- **Every frame, for privacy:** all 33,311 decoded frames were scanned (`ffmpeg -vf crop=720:200:0:1080 -f rawvideo -pix_fmt gray`, numpy
  bright-pixel counts per region): the timestamp line holds ≥ 1,451 overlay pixels in every frame; the band above it and the bottom-left corner
  are bright in nine short windows only (screen glare or a finger — each viewed). **No address overlay.** **Personal data:** the notification
  shade with legible third-party names and messages at t ≈ 189.3–190.9, 592.3–593.3, 636.4–637.2, 689–690, 929.9–950.3, 966.5–967.3,
  969.3–978.3, 1000–1009 and 1015–1022 s; a heads-up message from a named contact at t ≈ 1036–1040 s; a third-party video title in the Quick
  Settings media card whenever Quick Settings is open. The maintainer chose to keep the film **unblurred** (chat 2026-09-25, `ai-sessions/0047`).
- **Content:** a 1 fps pass over all 1,115 s (every frame on contact sheets), then 2–10 fps around every transition that a claim depends on (the
  minute rollovers, the notification-shade windows, the re-dock at t = 272–294 s), and single full-resolution frames for screen texts
  (t = 238.5, 256, 404, 884.5, 892 s). The **audio track** was used for Find My Buds: RMS per 0.25 s over t = 820–930 s.
- **Which slot is which bud** (fixed by the wire, not assumed): with the case as filmed, the **upper slot holds the Right bud** — removing it at
  t ≈ 465 s turns the Right charging bit off (`e4 64`, HCI frame 7058) — and the lower slot the Left bud.
- Every screen text quoted below was read on the frame of the given time. "phone" times = film overlay + 0.7 s.

## Event Timeline

Times are phone local time (HCI / debug export). "Claim" = the app's on-demand DLCI 0x04 open (ADR-032). Frame numbers are HCI frames of
`CAP-062-btsnoop_hci.log`; "export N" is a line of `CAP-062-debug-export.log`. Test IDs: `APP_TESTPLAN.md` (A1 … L3); registry Test-IDs in brackets.

| Time (phone) | Action / event | Initiator | Test ID | Evidence (video / HCI / logs) |
|---|---|---|---|---|
| 06:38:36–06:38:41 | Recording starts in Quick Settings, Bluetooth dialog "Bluetooth staat uit"; the maintainer switches **Bluetooth on** there (not via the app). Case closed on the table. | User (Android) | A1/A2 (done differently) | film t=0–5; HCI log starts 06:38:41.863; system log 04:38:42.137 `BluetoothAutoOff … STATE_CHANGED` |
| 06:38:47–06:39:00 | Home screen; OpenControl opened (splash); *Nearby devices* prompt → **Toestaan**, notifications prompt → **Toestaan**. | User | A3, A4 | film t=11–23; export 1–4 (both GRANTED 06:39:00.007); system log 04:38:50.210 `START … MainActivity` |
| 06:39:00–06:39:13 | "No Pixel Buds Pro 2 paired yet" + **Pair a device**; Debug tab, **Debug mode on**. | User | B1, L1 | film t=23–37 |
| 06:39:14.5 | Tap **Pair a device** with the **case lid closed**: CDM dialog "Zoeken naar een apparaat" finds nothing; lid opened at ≈ 06:39:27 (both buds seated, LED on); GmsCompat heads-up "Google Play services needs to sho…" (Fast Pair half sheet blocked). Picker dismissed → "Pairing failed: the system dialog reported 'canceled'. Try again." | User / OS / Play services | B2 (first attempt) | film t=37–63; export 9–11; system log 04:39:14.533 `CDM … mNamePattern=(?i).*Pixel\s*Buds.*`, 04:39:36.815 `Background activity launch blocked … fastpair.HalfSheetActivity` |
| 06:39:43.9–06:39:48.1 | Second **Pair a device**: CDM "Toestaan dat de app OpenControl … toegang heeft tot de Pixel Buds Pro 2 van Ted?" → **Toestaan**; bond BONDING→BONDED; LE link (1068, handle 0x0042) with SMP pairing, then classic Create Connection (1141) → Connection Complete (1179, 0x000b). Screen "Connected to this phone (Android) — App control: not open yet — tap Connect". | User / OS | B2, B3 [`PAIR-001`] | film t=66–71; export 12–19; HCI 1068–1179 |
| 06:39:48.2–06:39:48.8 | OS/other clients open their channels: HFP DLCI 0x0c (1503), **Play services DLCI 0x04** (1543; `03 08`, `07 10`, `06 01`), **DLCI 0x08/0x0a** (1670/1707; the 0x08 opener sends `05 0c … 0e 04 …` and gets the Case push `… 08 3c 10 01 18 03 …` = 60 %, frame 1766). | OS / Play services / Google app | — | HCI as cited (`d08` table in FINDINGS §6) |
| 06:40:12.8 | The phone closes the LE link (HCI Disconnect 2355, reason `0x13`; 59 Fast Pair Key-based Pairing writes before it, each answered "Error 0x81"). | OS / Play services | — | HCI 2355/2357 |
| 06:40:32.3–06:40:39 | **Both buds taken out** of the case and laid beside it (Right at ≈ 06:40:32, Left at ≈ 06:40:38). Buds `DISC` on Play services' 0x04 (2452); battery `64 e4` then `64 64`. | User (hardware) | [`CASE-004`/`005`] | film t=115–123; HCI 2452, 2627, 2691 |
| 06:41:04.8 | Tap **Connect** (buds on the table): DLCI 0x02 (2751), announcement ch 19 (2768), `ReadSetting 4:16` → `[-2, 0, 2, 3, 5]` (2775), `SubscribeRuntimeInfo` (2777) → stream without entry 6.1 (2782). Snapshot claim: attempt 1 fails and closes Play services' 0x04 (phone `DISC` 2784), attempt 2 (2805): `64 64 ff`, `Notify 01 e8 00 20`. Screen: "App control: ready", "Firmware: release_5.203", **no Safe Mode card**, Left 100 %, Right 100 %, "Case: Battery unavailable — The Buds haven't reported the Case level on this connection." Foreground service starts. | User (app) | C2, D1 (R1) | film t=148–152; export 20–41; system log 04:41:05.940 `am_foreground_service_start` |
| 06:41:18.8 | Tap **Disconnect** → "App control: not open yet"; FGS stops. | User (app) | C3 | film t=160–162; export 42–43; HCI 2851; system log 04:41:18.811 |
| 06:41:26.1 | Tap **Connect** → ready; L/R 100 % (06:41:28). | User (app) | C4 | film t=168–170; export 44–64 |
| 06:41:46.9–06:41:47.8 | Notification shade (privacy note), Quick Settings. | User (Android) | — | film t=189–191 |
| 06:41:49–06:41:52.9 | Bluetooth dialog: tap the Buds' row "Actief" → Android disconnects: phone `DISC` HFP 0x0c (3036), ACL `Disconnection Complete` reason `0x13` (3065, 06:41:52.873); app "Session lost: channel 0x02 closed …" (06:41:52.899); screen "Paired — not connected to this phone … Android no longer shows the Buds connected". | User (Android) | C6 | film t=192–202; export 65–68; HCI 3036, 3065 |
| 06:42:00.9 | Tap **Connect** in the app (Android not yet connected): the app's socket open creates the ACL (Create Connection 3123, 7 ms after "Connecting", Complete 3126); ready; "Android doesn't show the Buds as connected (yet)", then "Connected to this phone (Android)" (06:42:06.8). | User (app) | C7 | film t=204–210; export 69–95 |
| ≈ 06:42:26–06:42:34 | The **Right** bud is seated (runtime-info stream 3760 at 06:42:28.851: entry 6.1 = 60 appears, `3:{… 2:2}`, `7:{1:1}`; screen "Case: 60% (updated 06:42:29)"), then the **Left** bud (≈ 06:42:31–34). | User (hardware) | E2 [`CASE-006`] | film t=229–237; HCI 3760, 3770; export 96–99 |
| 06:42:35.1 | **ACL `Disconnection Complete` `0x13`** (3814) ≈ 1–3 s after the second bud was seated, lid still open (ADR-016). App: "Session lost …" — the screen says "**The Maestro channel (equalizer) was closed while Android still shows the Buds connected** …" (film t=238.5), although the ACL was already gone (Android link `NOT_CONNECTED` 06:42:35.244, 116 ms after the loss line); the text changes to "Android no longer shows the Buds connected" later (t=256). | Buds | C8 (partly) | film t=238–256; export 100–103; HCI 3814 |
| 06:42:37.9 | Lid **closed** (both buds in, LED on). | User (hardware) | C8 | film t=241 |
| 06:42:46–06:42:53 | Lid opened, a bud taken out: **Buds page the phone** (Connect Request event 3951) → Connection Complete 3955; the Buds open HFP (0x08 SABM 4183 — DLCI numbers flip on a Buds-initiated connection) and the Google app DLCI 0x09 (4196). | User (hardware) / Buds | [`CASE-004`] | film t=249–257; HCI 3951–4299 |
| 06:43:02.5 | Tap **Connect** (lid open, one bud out): MAESTRO on **DLCI 0x03** (4463), Message Stream on **DLCI 0x05** (4502) — session-local numbering; battery `64 e4` then `64 64`. | User (app) | C9 | film t=265–266; export 105–130 |
| 06:43:11–06:43:18 | Lid closed with the case **empty** (both buds beside it); **Refresh battery** (claim 4608): `64 64`. | User | E4-like | film t=275–278; export 131–138 |
| ≈ 06:43:21–06:43:28 | Lid opened, both buds seated (first ≈ 06:43:24.5, second ≈ 06:43:26.5–27.5, 2 fps); stream 06:43:25.435 (6.1 = 60 %; screen "Case: 60%"); **ACL `0x13`** at 06:43:28.156 (4667) — ADR-016 again. | User (hardware) / Buds | [`CASE-006`] | film t=284–291 (2 fps sheet); HCI 4652, 4667; export 139–146 |
| 06:43:30.8 | Tap **Connect** with both buds docked, lid open: ready; `e4 e4` (both charging), stream `6:{1:{1:60 …} 2:{… 2:2} 3:{… 2:2}} 7:{1:1 2:1}`; screen "Left 100% (charging), Right 100% (charging), Case: 60%". | User (app) | **E1, E2** (R5) | film t=293–295; export 147–170; HCI 4834, 4845 |
| 06:43:35.7 | **Refresh battery** → same (06:43:37). | User (app) | E1 | film t=299; export 171–178 |
| ≈ 06:43:38–06:43:40.7 | One bud taken out → **Buds `DISC` on DLCI 0x02** (5313, ACL up); the phone also drops and reopens the Google app's 0x08/0x0a (5334/5335 → 5360/5387). App "Session lost … not a user disconnect". | User (hardware) / Buds | — | film t=302–307; export 179–181; HCI 5313 |
| 06:43:45.8 | Tap **Connect**: ready (channel 21), `64 64`, Case unavailable. | User (app) | — | film t=309–312; export 182–202 |
| 06:44:04–06:44:08 | Tab tour: ANC "OFF (updated 06:43:48)", EQ `[-2, 0, 2, 3, 5]` (= Clarity), Find, Debug "Unidentified frames (46)"; swipe/bottom bar. | User | C10, H1 | film t=328–335 |
| 06:45:18.9 | ANC tab, tap **TRANSPARENT** (buds **lying on the table**, not worn): claim 5978, `08 12 00 14 01 e8 e8 80 …` (5989) → **`ff 02 00 03 02 08 12`** (5998) + `Notify 01 e8 00 20` (6000). Screen: "**The Buds refused the command (not allowed in the current state).**" ANC stays "OFF". | User (app) / Buds | F1 (not worn), D2 (R1/R2) | film t=402–404 (full res t=404: case open and empty, both buds beside it); export 203–212 |
| 06:45:25–06:45:55 | Four more ANC taps (ADAPTIVE, ACTIVE, TRANSPARENT, ACTIVE after two `08 11` refreshes) → NAK `0x02` each (6050, 6107, 6155, 6243); the refusal text also appears in the Connection card. | User (app) / Buds | F2–F4 (not worn), F5-like refresh | film t=409–438; export 213–255 |
| ≈ 06:46:02–06:46:07.5 | **Both buds put in the case** (lid open): Buds `DISC` 0x02 (6258, 06:46:04.708) then ACL `0x13` (6277, 06:46:07.483). ANC tab "Not connected to the Buds. Controls are disabled". | User (hardware) / Buds | E5-like, C8-like | film t=446–451; export 256–259 |
| 06:46:09.6 | Tap **Connect** (app creates the ACL, 6288/6290): ready, `e4 e4`, Case 60 % (06:46:12). | User (app) | E1/E2 | film t=452–455; export 260–283 |
| ≈ 06:46:21.5 | **Right** bud (upper slot) removed → Buds `DISC` 0x02 (6848, 06:46:22.753). | User (hardware) / Buds | E3 [`CASE-005`] | film t=465–466; export 284–286 |
| 06:46:24.8 / 06:46:28 | **Connect**, then **Refresh battery**: `e4 64` = "Left 100% (charging), Right 100%"; stream `2:{… 2:2} 3:{… 2:1}`, `7:{1:0 2:1}`; Case 60 %. | User (app) | E3 (R6: charging follows the bud) | film t=467–471; export 287–315; HCI 7033, 7058 |
| ≈ 06:46:33–06:46:40 | **Left** bud removed; stream 7118 (06:46:35.578) drops 6.1; **Refresh battery** → `64 64`, Case "unavailable". | User | E4 | film t=477–482; export 316–327 |
| ≈ 06:46:44–06:46:50.2 | Both buds back in the case → Buds `DISC` 0x02 (7192) → ACL `0x13` (7225); **Android** reconnects the ACL at once (Create Connection 7226, 12 ms later; the app was not connecting). | User (hardware) / Buds / OS | E5 | film t=488–495; export 328–331 |
| 06:46:52.9 / 06:46:56.6 | **Connect**, **Refresh battery**: `e4 e4`, Case 60 % (06:46:55, 06:46:57). | User (app) | E5 | film t=496–500; export 332–365 |
| 06:47:25–06:48:20 | Idle on the Connection tab, buds docked, lid open: no stream packet arrives, the Case line keeps "(updated 06:46:57)"; the app sends nothing. | — | E6 | film t=528–583; HCI: no DLCI 0x02/0x04 frame from the app |
| 06:48:23.1 | **Add ANC Quick Settings tile** → snackbar "The ANC tile is already in Quick Settings — open Quick Settings fully and swipe …" (result 1). | User (app) | G1 (R7) | film t=586–590; export 366 |
| 06:48:29–06:48:30 | Notification shade (privacy note), Quick Settings with the **ANC** tile. | User (Android) | G2 | film t=592–594 |
| 06:48:33–06:48:50.4 | **Tile taps** ×4 with the buds **docked** ("OFF → ACTIVE"): each `08 12 … 08` → NAK `0x02` (8344, 8448, 8456, 8505). | User (tile) / Buds | G3 (not worn) | film t=596–612; export 367–403; system log 04:48:33.109 … `AncTileService` |
| ≈ 06:48:51–06:48:56 | **Both buds taken out** of the case (then not visible on film: worn) → Buds `DISC` 0x02 (8514, 06:48:52.599), ACL stays. | User (hardware) / Buds | — | film t=614–619; export 404–406 |
| 06:49:06.7 | Tile tapped while the session is closed → the app opens (new `MainActivity`, logcat 04:49:08.067); it does **not** connect by itself. | User (tile) | G4 | film t=629–631; logcat 04:49:08.067 |
| 06:49:09.8 | Tap **Connect** → ready (ch 21); `Notify 01 e8 e8 80` (**Settable `e8`**, TRANSPARENT). | User (app) | C9-like | film t=633–635; export 412–437; HCI 8640 |
| 06:49:12.9 | Notification shade partly open (privacy note). | User | J1 | film t=636 |
| 06:49:16.8–06:49:35.6 | **Tile taps** ×5 with the buds **worn**: ADAPTIVE, OFF, ACTIVE, TRANSPARENT, ADAPTIVE → each ACK `ff 01 00 06 08 12 01 e8 e8 <mode>` + `Notify … e8 e8 <mode>` (8705, 8755, 8810, 8864, 8929). | User (tile) / Buds | G3 (R2) | film t=640–658; export 440–498 |
| 06:50:05–06:50:08 | Notification shade, home screen, back to the app: ANC tab "ADAPTIVE (updated 06:49:36)". | User | G3, J2 | film t=689–695 |
| 06:50:37–06:51:09.7 | EQ tab: **Clarity** tapped (write `[-2,0,2,3,5]`, 9061), **Treble** dragged to 6 (9161), **Upper treble** to 6 (9190), **Treble** −6 (9200), **Mid** −6 (9210), **Bass** −5.96 (9213), **Low bass** −5.8 (9216) — seven `WriteSetting`s, each answered by an empty `RESPONSE`, status OK; one write per release. | User (app) / Buds | H2 (Clarity only), H3, H4 (R3) | film t=705–754; export 509–526; HCI as cited |
| 06:51:40.4 / 06:51:43.1 | **Disconnect**, **Connect** → `ReadSetting` returns `[-5.8, -5.96, -6, -6, 6]` (9314); the EQ tab shows it ("EQ updated 06:51:44"). | User (app) | H6 | film t=782–790; export 527–549 |
| ≈ 06:52:02–06:52:05.2 | Buds taken **out of the ears** and laid on the table → Buds `DISC` 0x02 (9362). EQ tab "Not connected to the Buds. Controls are disabled". | User (hardware) / Buds | — | film t=808–811; export 550–552 |
| 06:52:19.7 | **Connect** → ready; `Notify … e8 00 20` (not worn). | User (app) | — | film t=822–826; export 553–575 |
| 06:52:33.9 | **Ring Left**: `04 01 00 01 02` (9541) → `ff 01 00 03 04 01 00` (9552) + the Buds echo `04 01 00 01 02` (9553); "Ringing: Left earbud — tap Stop to end it." | User (app) / Buds | I1 [`FIND-002`] (R4) | film t=837–839; export 576–584 |
| 06:52:46.7 | **Stop**: `04 01 00 01 00` ×2 (9607, 9617), ACKs; the loud ring bursts on the audio track stop at t≈852 s. | User (app) | I2 | film t=850; audio RMS; export 585–594 |
| 06:52:52.7 / 06:53:07.7 | **Ring Right** (`… 01`, 9660) → ACK; **Stop** (`… 00`, 9723) → ACK. | User (app) | I3 [`FIND-001`] | film t=856–871; export 595–612 |
| 06:53:11.7 | **Ring Left** (9772) → ACK; "Ringing: Left earbud". | User (app) | I4 | film t=875; export 613–621 |
| 06:53:20.4 | **Disconnect** while ringing: the Find tab shows "Not connected to the Buds. Controls are disabled" — **not** the expected "A ring was started on the Left earbud — reconnect and tap Stop to end it."; the ring **keeps sounding** (3 s bursts on the audio track t ≈ 888.5–913 s). | User (app) | I4 (**fails**) | film t=884.5, 892 (full res); audio RMS; export 622–623 |
| 06:53:42.6 / 06:53:47.5 | **Connect**, then **Stop** → `04 01 00 01 00` (9942) → ACK; the audio bursts stop (t ≈ 913.2 s = 06:53:50.1). | User (app) | I4 | film t=906–911; export 624–653; audio |
| 06:54:06–06:54:29 | Notification shade scrolled for ≈ 20 s (privacy note): the OpenControl notification reads "Connected — ANC: OFF". | User (Android) | J1 | film t=930–950 |
| 06:54:35.7 | ANC **ADAPTIVE** (buds on the table) → NAK `0x02` (10127); "The Buds refused the command …". | User (app) / Buds | F2 (not worn), F8-like | film t=959–961; export 656–665 |
| 06:54:43–06:54:54 | Quick Settings + notification shade (privacy note). | User (Android) | J1 | film t=967–977 |
| ≈ 06:55:02–06:55:04.3 | Buds picked up and put **in the ears** → Buds `DISC` 0x02 (10163). | User (hardware) / Buds | — | film t=984–991; export 668–670 |
| 06:55:07.7 | **Connect** → ready; `Notify … e8 e8 40` (worn). | User (app) | — | film t=991–995; export 671–691 |
| 06:55:14–06:55:24 | Quick Settings + notification shade: "Connected — ANC: ADAPTIVE". | User (Android) | J2 | film t=998–1009 |
| 06:55:29.8 | ANC **ACTIVE** → `08 12 … 08` (10337) → ACK (10348) + `Notify … e8 e8 08`; "ANC mode: ACTIVE (updated 06:55:30)". | User (app) / Buds | F4 (R2) | film t=1012–1014; export 692–701 |
| 06:55:31–06:55:39 | Quick Settings + notification shade: "Connected — ANC: ACTIVE". | User (Android) | J2 | film t=1015–1022 |
| 06:55:53–06:55:57 | Heads-up message notification over the app (privacy note). | — | — | film t=1036–1040 |
| 06:56:21–06:56:26.4 | Bluetooth dialog: tap the Buds' row → Android disconnects: phone `DISC` HFP 0x09 (10503), ACL `0x13` (10529, 06:56:26.375); app "Session lost". | User (Android) | C6 (repeat) | film t=1064–1067; export 706–709 |
| 06:56:29–06:56:34 | Bluetooth dialog: tap the Buds' row again ("Verbinding maken…") → Android reconnects (Create Connection 10536, Complete 10539 06:56:32.261); HFP/0x08/0x0a reopen (10720–10757). | User (Android) | C7 | film t=1072–1076; export 710 |
| 06:56:41.3 | **Connect** → ready; `e8 e8 08`. | User (app) | C7 | film t=1084–1086; export 711–731 |
| 06:57:00–06:57:11 | Tab tour: ANC "ACTIVE (updated 06:56:43)", EQ, Find, Debug "Unidentified frames (115)". Recording ends. The debug export (731 lines) was taken off film. | User | L2 (export exists) | film t=1104–1115 |
| 06:57:55.25 | After the film: Android stops the Google app's `BistoRealService` (04:57:55.173 UTC); 79 ms later the phone closes DLCI 0x08/0x0a (11308/11309). A bug report is started at 06:57:59. | OS / Google app | — | system log; HCI |

## Analysis checklist

- [x] Correlate video, HCI log, app debug export, app logcat and system log — done, `CAP-062-FINDINGS.md`.
- [x] Every Buds packet classified (handles 0x000b/0x0042): RFCOMM 1,141 (DLCI 0x00/0x02/0x03/0x04/0x05/0x08/0x09/0x0a/0x0b/0x0c, every data frame
      decoded per DLCI: `pwrpc_decode.py` for DLCI 0x02/0x03, a per-open `[Group][Code][Len]` reassembly for 0x04/0x05/0x08/0x09), SDP 258,
      AVDTP 222, HFP 102 (`AT+BIEV` ×14), AVRCP 42, HID 21, ATT 444 + SMP 13 on the LE handle (Fast Pair Key-based Pairing writes, ADR-008),
      L2CAP signalling. Audio profiles are classified, not decoded field by field (`PROJECT.md` non-goals).
- [x] **Traceability (`AGENTS.md` §13.7)** — `APP_TESTPLAN.md`: A1/A2 done differently (Bluetooth switched on in Quick Settings before opening the
      app), A3 ✓, A4 ✓, **A5 not run**, **A6 not on film**; B1 ✓, B2 ✓ (first attempt with the lid closed, cancelled), B3 ✓, **B4 not run**;
      C1–C4 ✓, **C5 not run**, C6/C7 ✓ (twice), C8 partly (session ended by the ACL drop when the second bud was seated, lid still open; stale
      wording), C9 ✓, C10 ✓ (bottom bar; swipe not distinguishable on film); D1/D2 ✓; E1–E7 ✓; F1–F4 done with the buds **not worn** (NAK) and
      once worn (F4 ACK), **F5 not run** as written (no press-and-hold), **F6/F7 not run**, F8 ✓; G1–G4 ✓; H1 ✓, H2 partly (Clarity only),
      H3/H4 ✓, **H5 not run**, H6 ✓; I1–I3 ✓, I4 ✗; J1/J2 ✓, J3 ✓ (system log), **J4 not run**; **K1–K5 not run**; L1 ✓, L2 ✓ (off film),
      **L3 not run**. Registry Test-IDs: `PAIR-001` ✓, `PAIR-003` ✓ (×14 app sessions), `CASE-004`/`CASE-005`/`CASE-006` ✓, `BATT-004` ✓,
      `ANC-001`–`ANC-004` ✓ (ACK worn, NAK not worn), `FIND-001`/`FIND-002` ✓, `EQS-001`/`EQP-*` ✓ (Clarity only).
- [x] `ai-sessions/0046` §9: R1 ✓, R2 ✓, R3 ✓ (audibility not established), R4 ✓ (ringing heard until Stop), R5 ✓, R6 ✓, R7 ✓, R8 (E)/(F)
      **expected but not observed** (not run).

## Next steps

- [x] `CAP-062-FINDINGS.md` — done.
- [x] Folder named with the film's own start/end (`CAP-062-2026-09-25_06-38-36_06-57-10-Group_AX`), Capture Index row, Group AX, `id_registry.csv`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-062-2026-09-25_06-38-36_06-57-10-Group_AX/CAP-062-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-062-2026-09-25_06-38-36_06-57-10-Group_AX/CAP-062-EVENT-NOTES
