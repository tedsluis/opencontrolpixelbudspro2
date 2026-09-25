# Event Notes: OpenControl for Pixel Buds on Pixel 9a (GrapheneOS) — Group AW, first hardware run of the `ai-sessions/0045` build (`CAP-061`)

**Status:** 🟢 **Video pass + full log analysis complete** (`ai-sessions/0046`). See `CAP-061-FINDINGS.md` for the wire-level analysis,
the commands and raw bytes, and the root cause of every symptom this timeline shows.

**Purpose (maintainer, chat 2026-09-24):** first hardware run of the `ai-sessions/0045` build, exercising `ai-sessions/0045` RESULT §9's
re-test items (A)–(H): Case battery with the `0e 04` request, ANC answers, Safe Mode, the dock line, pairing, Find My Buds, EQ and the ANC
tile. The maintainer reported afterwards: no Case level, ANC does not change, "Both earbuds seem to be in the case" while one was out (with
one bud charging and the other not), a Safe Mode message, EQ does not work, Find My Buds does not work.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-061`                     |
|      Group(s)    |                        AW                          |
|       Date       |                    2026-09-24                      |
| Firmware version | 🟢 `release_5.203` — on the wire in every DLCI 0x02 announcement (e.g. HCI frame 1508) and every DLCI 0x08 `03 02`/`02 04` (e.g. frame 1311); the film never shows a firmware screen |
|   Test device    | Pixel 9a, GrapheneOS, Android 17 `CP2A.260805.005` (logcat header `google/tegu/tegu:17/CP2A.260805.005/2026091901`). App: OpenControl for Pixel Buds, the `ai-sessions/0045` code — `android/` is identical from commit `5ade05e` to `964fa91` (`git diff --stat 5ade05e HEAD -- android` is empty) and log wording that exists only from `5ade05e` on is present (`Safe Mode: write refused`, the `0e 04 00 00` send). The capture (17:25–17:31) predates the commit time (17:52): the APK was built from that working tree. Google Play services and the Google app (`com.google.android.googlequicksearchbox`) present. Play services' *Nearby devices* permission state: **not recorded** |
| Video file       | `CAP-061-recording.mp4`: 375.99 s (`ffprobe`), H.264 1280×720 with rotation −90 (portrait 720×1280), 29.83 fps (179/6), AAC audio track present (no narration relevant to the analysis was used). Burned-in overlay `Sep 24, 2026 HH:MM:SS`, timestamp only: first frame 17:24:59, last frame 17:31:14 |
| Log file         | `CAP-061-btsnoop_hci.log` — `capinfos`: 6,369 packets, "Packet size limit: (not set)" (raw path), 2026-09-24 17:25:03.610669–17:34:33.853806 (570.24 s). 0 `cap_len≠len`. One 1 µs out-of-order pair (frame 2334) |
| App debug export | `CAP-061-debug-export.log` (386 lines, 17:25:13.609–17:42:47.658, phone local time) |
| App logcat       | `CAP-061-OpenControl-for-Pixel-Buds-log-7a465b5a9d5e.txt` (139 lines; its main buffer holds the app's own `OpenControlBuds` lines only from 15:33:08 UTC, after the session) |
| System log       | `CAP-061-System-log-2c0390537392.txt` (76,811 lines, 09-20 18:56 → 09-24 15:36 UTC; `com.android.bluetooth`'s own lines in the session are dropped by `liblog`, e.g. `dropped=28` at 15:25:04.339) |
| Buds (partial, `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` (same unit as every capture). Classic ACL handle `0x000b`, LE handle `0x0042`. **Pre-filter note (`AGENTS.md` §13.1):** with this `H4 with linux header` encapsulation `bluetooth.addr` is empty for every packet, so the pre-filter is by connection handle: `bthci_acl.chandle==0x000b` / `==0x0042` (both mapped to this address by HCI Connection Complete frame 715 and LE Enhanced Connection Complete frame 584). LE handle `0x0041` is another device (`c8:cc:a8:e7:48:93`, frame 277) and is excluded |
| Clock offsets    | **Film ↔ phone, measured at start and end:** the overlay second 17:26:00 begins at video t = 60.85 s and the phone's status-bar minute flips 17:25→17:26 at t = 60.9–61.0 s; at the end the overlay second 17:31:00 begins at t = 360.85–360.9 s and the status bar flips 17:30→17:31 at t = 360.9–361.0 s. So film overlay = 17:24:59.15 + t, and the phone clock is 0.0–0.2 s behind the overlay, no drift. **Logcat / system log ↔ debug export:** −2 h 00 min 00.000 s (UTC vs local; e.g. "Android link observer stopped" logcat 15:33:08.165 = export line 379 17:33:08.165). HCI timestamps are the phone's local clock (capinfos, CEST) |

## Capture-integrity pre-flight

```
$ capinfos CAP-061-btsnoop_hci.log
Packet size limit:   file hdr: (not set)
Number of packets:   6,369
Earliest packet time: 2026-09-24 17:25:03.610669
Latest packet time:   2026-09-24 17:34:33.853806

$ tshark -r CAP-061-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'
mismatches: 0

$ sha256sum *   (before and after the move into this folder — identical)
5f21dfc6…981c  CAP-061-btsnoop_hci.log          608a91c3…2606  CAP-061-debug-export.log
5f99b1ba…ee07  CAP-061-OpenControl-…-7a465b5a9d5e.txt   4400380…08b2  CAP-061-recording.mp4
02214bf7…e16b  CAP-061-System-log-2c0390537392.txt
```

## Video review method

- **Every frame, for privacy:** all 11,209 decoded frames were scanned (`ffmpeg … -f rawvideo | numpy`, near-white pixel counts per
  region): the timestamp line is present in every frame (≥ 1,103 overlay pixels), the band above it is empty except for scene content
  (the case LED, screen glare — viewed), the bottom-left is always empty. No address overlay anywhere. **Personal data found:** the
  notification shade is open twice with legible third-party names and chat-group names — t = 10.1–11.8 s (17:25:09.3–17:25:11.0) and
  t = 215.0–216.9 s (17:28:34.2–17:28:36.1). The maintainer chose to keep the file unblurred (chat 2026-09-24).
- **Content:** a 1 fps pass over all 376 s (every frame viewed on contact sheets), then 2–30 fps around every transition: the two
  notification-shade windows (10 fps), the minute rollovers (10 fps), the case during the re-dock at the end (2 fps and 30 fps).
- Every screen text quoted below was read on the frame of the given time.

## Event Timeline

Times are phone local time (debug export / HCI); film times are overlay seconds (= phone + 0.0–0.2 s). "App claim" = the app opened an
on-demand channel. Frame numbers are HCI frames in `CAP-061-btsnoop_hci.log`.

| Time (local) | Action / event | Initiator | Test-ID | Evidence (video / HCI / logs) |
|---|---|---|---|---|
| 17:24:59–17:25:04 | Recording starts: Quick Settings → Bluetooth dialog, "Bluetooth staat uit"; the case (closed) above the phone. The maintainer switches **Bluetooth on** (tap at ≈ 17:25:02). | User (Android) | — | film t=0–5; system log 15:25:02.371 `Start proc … com.android.bluetooth`; `BluetoothAutoOff` STATE_CHANGED 15:25:03.992/15:25:04.381 |
| 17:25:05–17:25:08 | Bluetooth list: bonded "Charge 6", "Nirvo" (not the Buds — they are not bonded yet). Tap "Klaar". | User | — | film t=6–9 |
| 17:25:09.3–17:25:11.0 | Notification shade open (third-party notifications, see privacy note). | User | — | film t=10.1–11.8 |
| 17:25:11–17:25:13 | Home screen, OpenControl launched (splash). | User | — | film t=12–14; system log 15:25:12.337 `START … opencontrolpixelbuds/.app.MainActivity`; export line 1 17:25:13.609 |
| 17:25:14–17:25:21 | "Nearby devices" prompt → **Toestaan** (≈ 17:25:16), notifications prompt → **Toestaan** (≈ 17:25:20). | User | — | film t=15–22; export lines 3–4 (both GRANTED 17:25:21.307) |
| 17:25:21–17:25:32 | "No Pixel Buds Pro 2 paired yet" + "Pair a device". Case **lid opened** at ≈ 17:25:26–17:25:28, both buds seated. | User (hardware) | `CASE-003` | film t=22–33 |
| 17:25:33 | Tap **Pair a device** → "Waiting for you to pick your Pixel Buds in the system dialog". | User (app) | `PAIR-001` | film t=34–35; export line 9 17:25:33.896; system log 15:25:33.905 `CDM_AssociationRequestsProcessor … mNamePattern=(?i).*Pixel\s*Buds.*` |
| 17:25:34.5 | Heads-up "Google Play services needs to sho…" (GmsCompat): Play services' Fast Pair half sheet was blocked from a background start. | Play services | — | film t=35–37; system log 15:25:34.453 `Background activity launch blocked … com.google.android.gms/.nearby.discovery.fastpair.HalfSheetActivity`, 15:25:34.503 `notification_enqueue … app.grapheneos.gmscompat … channel=bg_activity_start` |
| 17:25:35–17:25:38 | CDM dialog "Toestaan dat de app OpenControl for Pixel Buds toegang heeft tot de Pixel Buds Pro 2 van Ted?" → **Toestaan** (≈ 17:25:36); "Pairing: keep the Buds close"; bond BONDING→BONDED. LE link then classic link. | User / OS | `PAIR-001` | film t=36–39; export lines 10–15; HCI 584 LE connection (handle 0x0042) 17:25:36.838, 605–640 SMP pairing with `Linkkey` distribution (CTKD, ADR-030), 652 Create Connection 17:25:37.629, 715 Connection Complete 17:25:38.044 (0x000b) |
| 17:25:38.8–17:25:42.7 | OS profiles and the Google apps open their channels: HFP DLCI 0x0c (1013), **Play services DLCI 0x04** (1050, sends `03 08`/`07 10`/`06 01`), **DLCI 0x08 + 0x0a** (1226/1256; the 0x08 opener sends the `05 0c … 0e 04 … 03 01 … 02 0b` burst and receives the Case `0e 01` push, Case `0x44` = 68 %, frame 1319). | OS / Play services / Google app | `BATT-004` | HCI 1013–1381; export line 16 (Android link CONNECTED 17:25:39.021) |
| 17:25:38–17:25:43 | App shows "Paired — Android's connection state isn't known (yet)", then "Connected to this phone (Android) — App control: not open yet — tap Connect". | App | — | film t=39–44 |
| 17:25:44.9 | Tap **Connect**. DLCI 0x02 opens (1496/1498); the Buds announce channel 21 + firmware (1508); `ReadSetting 4:16` → EQ `[-5, -1.5, 0, 0, 0]` (1513/1525). App state Ready. Foreground service starts. | User (app) | `PAIR-003` (first app session) | film t=45; export lines 17–24; system log 15:25:45.007 `am_foreground_service_start … BudsForegroundService` |
| 17:25:45.56–17:25:47.09 | Connect snapshot: app's DLCI 0x04 attempt 1 fails and the stack closes **Play services'** 0x04 (phone `DISC` 1536); attempt 2 opens (1574), `08 11` → Model ID, battery `e4 e4 ff` (both charging), `Notify 01 e8 00 20` (1594). Then the Case claim: attempt 1 fails and the stack closes the **Google app's** 0x08 (phone `DISC` 1621); attempts 2 and retry open 0x08 (1658, 1693) and the **Buds send `DISC` 1.2 ms / 4 ms after the `UA`** (1662, 1699) → "Case battery not read: ChannelLost". | App / Buds | `BATT-004` | export lines 25–33; HCI as cited |
| 17:25:45–17:25:46 | Screen: "Both earbuds seem to be in the case — … (updated 17:25:46) — provisional, read right after the channel opened." Left 100 % (charging), Right 100 % (charging), Case: Battery unavailable. **Correct** (both buds seated on film). | App | — | film t=46–47 |
| 17:25:51 / 17:25:57 | Tap **Refresh battery** twice. Each: Play services' 0x04 closed by the app's failed attempt 1 (2013, 2447), app claim, `Notify 00 20`; Case: Google app's 0x08 closed (2116, 2528), the app's two opens closed by the Buds within 4–13 ms (2152/2182, 2565/2595) → ChannelLost. The Google app re-opens 0x08 1.3–2.3 s later each time (2298, 2643). | User (app) / Buds | `BATT-004` | film t=52, 58; export lines 34–51 |
| 17:26:00–17:26:10 | ANC tab: "Connection: Ready. ANC mode: OFF (updated 17:25:58)"; EQ tab: sliders `0, 0, 0, −1.5, −5.0` (= Light bass, matches the wire read). | User | — | film t=61–71 |
| 17:26:11–17:26:22 | Find tab; Debug tab: "Unidentified frames (15)"; **Debug mode switched on** (≈ 17:26:15). | User | — | film t=72–83 |
| 17:26:29 | Tap **Refresh battery**: app 0x04 claim (2874), `Notify 00 20` (2896); Case: 0x08 opens closed by the Buds (2943, 2969) → ChannelLost. | User (app) | `BATT-004` | film t=90; export lines 52–64 |
| 17:26:36–17:26:37 | **Right bud removed** from the case (film). Battery on Play services' held 0x04 becomes `e4 64 ff` (Left charging, Right not) at 17:26:37.501 (3146). | User (hardware) | `CASE-005` | film t=97–98; HCI 3146 |
| 17:26:38–17:26:59 | Screen still shows "Both earbuds seem to be in the case … (updated 17:26:31)" — the last reading, with its time, until the next claim. | App | — | film t=99–120 |
| 17:26:45.7 | Buds `Notify 01 e8 e8 40` on the Google/Play-services-held 0x04 (3197): Settable `e8` with Left in, Right out. | Buds | — | HCI 3197 |
| 17:26:51–17:26:52 | **Left bud removed**; case empty. Battery `64 64 ff` at 17:26:52.256 (3233). | User (hardware) | `CASE-004` | film t=112–113; HCI 3233 |
| 17:27:00 | Tap **Refresh battery**: Play services' 0x04 closed (3303), app claim (3326): Left 100 %, Right 100 % (not charging), `Notify e8 08` (3346) → "At least one earbud seems to be out of the case … (updated 17:27:02) — provisional". **Correct.** Case: ChannelLost again (3395, 3425). | User (app) | `BATT-004` | film t=121–125; export lines 65–79 |
| 17:27:06 | **Lid closed** with both buds out (LED visible). | User (hardware) | — | film t=127–128 |
| 17:27:08 | Tap **Refresh battery**: app 0x04 claim first attempt (3581; Play services no longer re-claims 0x04 after 3303), `Notify e8 08`; Case: ChannelLost (3656, 3687). | User (app) | `BATT-004` | export lines 80–93 |
| 17:27:14–17:27:21 | ANC tab, tap **Adaptive**, then **Off**: nothing sent; after a 3 s wait each: "Safe Mode — nothing was sent — the Buds' firmware or model isn't one this app was verified against (read-only)"; Connection tab: **Safe Mode — read-only** card "The Buds did not announce their firmware version on this connection … Detected: Fast Pair model da2db1". | User (app) | `ANC-003`, `ANC-001` (attempted, refused) | film t=135–143; export lines 94–108 (refusals 17:27:17.974, 17:27:20.994); no `08 12` in the whole log |
| 17:27:24.37 | Android stops the Google app's `BistoRealService` ("app idle"); 91 ms later the phone closes DLCI 0x08 and 0x0a (3908/3909); neither is reopened by anyone but the app. | OS / Google app | — | system log 15:27:24.371; HCI 3908, 3909 |
| 17:27:35–17:27:43 | ANC taps (**Active**, Refresh): refusals 17:27:38.581, 17:27:41.597; `08 11` → `Notify e8 08` (3994/3996). | User (app) | `ANC-002` (attempted, refused) | film t=156–164; export lines 109–118 |
| 17:27:44–17:27:50 | EQ: **Upper treble** dragged to 3.8 → "The last EQ request failed: Safe Mode: nothing was sent …" + "Read EQ again". Second EQ action refused 17:27:50.648. | User (app) | `EQS-001` (attempted, refused) | film t=165–171; export lines 119–122; no `WriteSetting` on DLCI 0x02 in the whole log |
| 17:27:51–17:28:13 | Find: **Ring Left**, **Ring Right** (twice), **Stop** — each refused ("Safe Mode: nothing was sent …" on the Find screen). | User (app) | `FIND-001`, `FIND-002` (attempted, refused) | film t=172–194; export lines 123–147 (refusals 17:27:56.609, 17:28:03.849, 17:28:14.714, 17:28:17.727); no `04 01` in the whole log |
| 17:28:16 | Debug tab: "Unidentified frames (62)". | User | — | film t=197 |
| 17:28:21 | Tap **Disconnect** (phone `DISC` 0x02, 4226). Foreground service stops. | User (app) | — | film t=202; export lines 148–149; system log 15:28:22.256 `am_foreground_service_stop` |
| 17:28:24.8 | Tap **Connect**: 0x02 (4255), announcement ch 21 (4271, logged in full with Debug mode on, export line 154), EQ read; snapshot claim of 0x04 first attempt (4289), `Notify e8 08`; **Case claim held** (4335): `0e 04 00 00` sent (4343), **no answer**; "Case battery not read: Timeout"; released 4352. | User (app) | `PAIR-003`, `BATT-004` | film t=205–207; export lines 150–170 |
| 17:28:28 / 17:28:30 | Refresh battery: 0x04 claim (4378), `Notify e8 08`; Case claim (4429), `0e 04 00 00` (4438), no answer → Timeout. Screen: "Case: Battery unavailable — The Buds didn't report the Case level (they may not send it while the case is closed or the buds are out)". | User (app) | `BATT-004` | film t=209–213; export lines 171–181 |
| 17:28:34–17:28:46 | Notification shade (privacy note). A GmsCompat group, age "2 m", reads "Google Play-services needs to show a scr…" and "Google Play-services has crashed Tap to …" (film t=215.3); the system log has no matching crash line or `notification_enqueue` in 15:25:34–15:28:34, so when/what crashed is open, Bluetooth dialog "Pixel Buds Pro 2 van Ted — Actief, Batterijniveau 100%", device details page (LE Audio off, "Invoerapparaat" on); back to the app. | User (Android) | — | film t=215–227; logcat/system log 15:28:38.857 app paused, 15:28:46.167 resumed; export lines 182–185 |
| 17:28:49–17:28:51 | Refresh battery: 0x04 claim (4559); Case claim (4605), `0e 04 00 00` (4614), no answer. | User (app) | `BATT-004` | export lines 186–203 |
| 17:28:53–17:29:21 | ANC taps (Adaptive, Off, Transparent…) and **ANC Refresh**: refusals 17:28:56.181, 17:28:59.198, 17:29:02.217, 17:29:11.866, 17:29:14.876, 17:29:17.893, 17:29:20.908; `08 11`→`Notify e8 08` at 4680/4682 and 4777/4781. **"Add ANC Quick Settings tile"** tapped: result `1` = `TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED` (nothing shown on screen). | User (app) | `ANC-001`–`ANC-004` (attempted, refused) | film t=234–262; export lines 202–222, 205 (17:29:01.640) |
| 17:29:23–17:29:39 | EQ: preset taps and an Upper-treble drag, each refused 3 s later (17:29:24.404 … 17:29:41.816, eight "Safe Mode" lines, five "EQ request failed: UnsupportedFirmware"). | User (app) | `EQP-*` (presets not disambiguated), `EQS-001` (attempted, refused) | film t=264–280; export lines 223–236 |
| 17:29:40–17:30:06 | Find: Ring Left/Right taps, refused (17:29:45.250, 17:29:56.747, 17:30:03.253, 17:30:06.266). Disconnect 17:29:48.4 (4889) / Connect 17:29:49.6 (4915; announcement 4931; EQ read; 0x04 snapshot 4948; Case claim 4993 + `0e 04` 5001, no answer). | User (app) | `FIND-001`, `FIND-002` (refused), `PAIR-003` | film t=281–307; export lines 237–284 |
| 17:30:16 / 17:30:26 / 17:30:29 / 17:30:38 | Refresh battery (0x04 5164, Case 5214 + `0e 04` 5222, no answer), ANC Refresh (5256), **Transparent** tap refused (17:30:32.847), Refresh battery (5372; Case 5419 + `0e 04` 5427, no answer). | User (app) | `BATT-004`, `ANC-004` (refused) | film t=317–340; export lines 285–322 |
| 17:30:32–17:30:48 | Lid opened; the maintainer picks up the buds and starts putting them back. At 17:30:48.339 the **Buds send `DISC` on DLCI 0x02** (5456); the app: "Session lost: channel 0x02 closed … last inbound frame … 8448 ms earlier; not a user disconnect"; the screen: "The Maestro channel (equalizer) was closed while Android still shows the Buds connected …". ACL stays up. | User (hardware) / Buds | — | film t=333–349; export lines 323–325 |
| ≈ 17:30:52.4–17:30:53 | **Right bud seated** (film 2 fps: t=352.5–353.0); case LED on; `AT+BIEV=2,100` 17:30:53.145 (5464). | User (hardware) | `CASE-006` (part) | film t=353–355; HCI 5464 |
| 17:30:56–17:30:58 | Tap **Connect** (with the Left bud still in the hand): 0x02 (5507), announcement **channel 19** (5524); snapshot claim 0x04 (5541): battery **`64 e4 ff`** (Left 100 % not charging, Right 100 % charging, 5557) and **`Notify 01 e8 00 20`** (5560, 17:30:58.806) → screen "**Both earbuds seem to be in the case** … (updated 17:30:58) — provisional", "Left: 100% (updated 17:30:58)", "Right: 100% (charging)". **Wrong:** the Left bud is in the hand beside the case at that instant (30 fps: in hand t=359.4–360.4, seated t≈360.45 = ≈ 17:30:59.4–59.6 phone time). Case claim 5585 + `0e 04` 5593. | User (app) / Buds | `PAIR-003` | film t=357–361; export lines 326–344 |
| 17:30:59.97 | **Left bud seated** → ACL `Disconnection Complete` reason `0x13` (5597), 0.4–0.6 s after the seating (ADR-016). App: session lost; Android link NOT_CONNECTED. | Buds | `CASE-006` | HCI 5597; export lines 345–348 |
| 17:31:05.5 | Tap **Connect** with both buds seated, lid open: Create Connection (5605) → Connection Complete (5608); 0x02 (5685), announcement ch 19 (5697); 0x04 snapshot (5719): battery `e4 e4 ff`, `Notify e8 00 20` → "Both earbuds seem to be in the case" — **correct**; Case claim (5766) + `0e 04` (5776), no answer → Timeout; 0x08 released (6026). HFP opened by the Buds on DLCI 0x09 (5887). | User (app) | `PAIR-003`, `BATT-004` | film t=367–369; export lines 349–370 |
| 17:31:09 | **Lid closed** → ACL `Disconnection Complete` `0x13` (6068, 17:31:09.588). App: "Paired — not connected to this phone … Android no longer shows the Buds connected". | User (hardware) | `CASE-006` | film t=370–375; HCI 6068; export lines 371–374 |
| 17:31:14 | Recording ends. The HCI log runs to 17:34:33 with no further Buds classic/LE traffic. | — | — | film end |

## Analysis checklist

- [x] Correlate video, HCI log, app debug export, app logcat and system log — done, `CAP-061-FINDINGS.md`.
- [x] Every Buds packet classified: RFCOMM 1,428 (DLCI 0x00/0x02/0x04/0x08/0x09/0x0a/0x0c, every frame decoded per DLCI), SDP 204,
      HFP 80, AVDTP 52, AVRCP 20, HID 4, L2CAP signalling/ATT 836 (LE handle 0x0042: SMP, GATT discovery, 56 Fast Pair Key-based
      Pairing writes), HCI ACL fragments 19. The audio profiles (AVDTP/AVRCP) are classified, not decoded field by field (codec
      detail is out of scope, `PROJECT.md` non-goals).
- [x] **Traceability (`AGENTS.md` §13.7)** — the Test-IDs Group AW is meant to exercise (`ai-sessions/0045` §9 A–H):
      `PAIR-001` ✓ (17:25:33–38), `PAIR-003` ✓ (17:25:44, 17:28:24, 17:29:49, 17:30:56, 17:31:05), `CASE-003` ✓, `CASE-004` ✓,
      `CASE-005` ✓, `CASE-006` ✓ (re-dock + lid close), `BATT-004` ✓ (Left/Right on every claim; Case never answered),
      `ANC-001`–`ANC-004` **attempted, nothing sent** (Safe Mode), `FIND-001`/`FIND-002` **attempted, nothing sent**, `EQS-001` and
      `EQP-*` **attempted, nothing sent**. Re-test items: (A) exercised, (B) not exercised (no Set was ever sent), (C) exercised,
      (D) exercised, (E) **expected but not observed** (the "No paired Pixel Buds" state appears only before pairing, the
      Forget-then-Connect path was not run), (F) **expected but not observed**, (G) exercised (service start/stop in the system log; the
      notification text is not legible on film), (H) tile exercised (already added), EQ audibility and Find ringing **not exercised**
      (Safe Mode), drop hunt: one Buds-side `DISC` on 0x02 (17:30:48).

## Next steps

- [x] `CAP-061-FINDINGS.md` — done.
- [x] `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index row and Group AW section; `id_registry.csv` row — done (`ai-sessions/0046`).
- [x] Folder named with the film's own start/end (`CAP-061-2026-09-24_17-24-59_17-31-14-Group_AW`).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-061-2026-09-24_17-24-59_17-31-14-Group_AW/CAP-061-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-061-2026-09-24_17-24-59_17-31-14-Group_AW/CAP-061-EVENT-NOTES
