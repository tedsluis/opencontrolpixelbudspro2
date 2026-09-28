# Event Notes: OpenControl for Pixel Buds on Pixel 9a (GrapheneOS) — Group AY, hardware re-test of the `ai-sessions/0048` and `ai-sessions/0052` builds (`CAP-063`)

**Status:** 🟢 **Video pass + full log analysis complete** (`ai-sessions/0053`). See `CAP-063-FINDINGS.md` for the wire-level analysis, the commands
and raw bytes, and the answers to the maintainer's twelve observations. The procedure the maintainer followed (the skeleton written before the
capture) is kept unchanged as **Appendix A**; the timeline below records what was **actually** done — the maintainer skipped some steps and
repeated others. The first video-only draft of this timeline (2026-09-27) was re-checked row by row; its corrections are listed in
`ai-sessions/0053_CAPTURE_RESULT_2026_09_27.md` §3.

**Purpose (maintainer, chat 2026-09-26/27):** the hardware re-test of `ai-sessions/0048` §9 (AY-0 … AY-12), `ai-sessions/0051` §19 (AY-13, AY-14)
and `ai-sessions/0052` §9 (settings reads/writes, Refresh on a fresh claim, re-subscription). The maintainer reported afterwards twelve observations
and three wishes (the prompt `ai-sessions/0053` §1.4): Find, EQ, balance/mono, conversation detection, connect by itself, battery, in/out of the case,
connect/disconnect, ANC (app and long press), touch controls, press-and-hold/Digital assistant — wishes: show the lid and in-ear state, choose the
ANC modes the hold cycles through, make in-ear detection writable.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-063`                     |
|      Group(s)    |                        AY                          |
|       Date       |                    2026-09-27                      |
| Firmware version | 🟢 `release_5.203` — in every DLCI 0x02 announcement (e.g. HCI frame 1261; debug export line 17) and on screen ("Firmware: release_5.203", film overlay 15:58:15) |
|   Test device    | Pixel 9a, GrapheneOS, Android 17 `CP2A.260805.005` (logcat header `google/tegu/tegu:17/CP2A.260805.005/2026091901`). App: OpenControl for Pixel Buds — the `ai-sessions/0052` build (`94e4fb1` or later): wording that exists only from that build on — "Settings read (channel 21)" (export line 28), "Runtime info re-requested on Refresh (channel 19)" (line 206), "Setting 17 written (channel 21)" (line 357), "Message Stream still claimed from an earlier action: released for a fresh claim" (line 568) — and six `ReadSetting`s after every EQ read (15 of 15 Connects). The commit hash was not written down (P1). The app's data had been cleared (first-start permission prompts on film). Google Play services and the Google app present. Play services' *Nearby devices* permission: **not recorded** (P4). Other Bluetooth devices: a "Charge 6" speaker (connected, Android's list) and a "Niro" (saved); the only other device in the HCI log is an LE device `c8:cc:a8:e7:48:93` (handle `0x0040`) |
| Video file       | `CAP-063-recording.mp4`: 1650.15 s, H.264 1280×720 rotation −90 (portrait), 49,253 video packets (≈ 29.85 fps). **Audio track empty** (stream 0 "Audio: none, 0 channels"; `ffmpeg -i CAP-063-recording.mp4 -map 0:a -f null -` → "no decoder found for: none") — every sound is the maintainer's observation. Burned-in overlay `Sep 27, 2026 HH:MM:SS`, timestamp only: **first frame 15:57:33**, last frame 16:25:03 |
| Log file         | `CAP-063-btsnoop_hci.log` — `capinfos`: 11,615 packets, "Packet size limit: (not set)" (raw path), 2026-09-27 15:57:40.614170–16:26:58.325151; 0 `cap_len≠len`; one out-of-order pair |
| Earlier log      | `CAP-063-btsnoop_hci.log.last` — 961 packets, 15:56:10.981–15:57:16.332: an HCI Reset and the other LE device only (no Buds traffic); it ends 18 s before the film's first frame (the Bluetooth off/on rotated the log). Context only |
| App debug export | `CAP-063-debug-export.log`: 746 lines, 15:57:55.706–16:18:29.012, Debug mode on at ≈ 15:58:23 (film), first hex line 15:58:31.308 (line 33) — the connect-time frames of 15:58:15 are not hex-logged; **cut at exactly 65,536 bytes, mid-line** by the old share-sheet export (fixed in `800555d`); the 1,000-line buffer did not wrap. 16:18:29–16:23:50 is covered by the HCI log only |
| App logcat       | `CAP-063-OpenControl-for-Pixel-Buds-log-a5f9783708f6.txt` (453 lines; lifecycle events from 13:55:01 UTC; `OpenControlBuds` lines only 14:23:50.493–14:25:38.403 UTC) |
| System log       | `CAP-063-System-log-6cf0a8a3bd50.txt` (129,188 lines; the session window 13:57:30–14:27:10 UTC is covered) |
| Clock offsets    | **Film ↔ phone, measured at start-half and end** (5 fps crops of the status bar against the overlay): the phone's minute flips 16:05→16:06 at t = 504.8 s, 0.6 s into overlay second 16:05:58 (which begins at t ≈ 504.1 s); 16:23→16:24 at t = 1584.8 s, again 0.6 s into overlay :58. So **phone clock = overlay + 1.4 s (±0.2 s), no drift**; overlay = 15:57:33 + ≈ t. (The skeleton's S0/Z2 minute changes were not filmed; the draft's "≈ +1 s" is superseded.) **Logcat / system log (UTC) = phone − 2 h 00 min 00.0 s** (logcat 14:24:21.283 "Session ended by the user's Disconnect tap" ↔ HCI phone `DISC` DLCI 0x02 frame 10981 at 16:24:21.290). HCI timestamps and the debug export are phone local time |
| Buds (partial, `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` (same unit as every capture). Classic ACL handle `0x000b` (HCI Connection Complete frame 810, reused by every reconnect), LE handle `0x0042` (LE Connection Complete frame 692, closed by the phone 15:58:39.857, frame 2180). **Pre-filter:** `bluetooth.addr` is empty with this encapsulation — every command filters `bthci_acl.chandle==0x000b`. **Slots:** the **upper** slot holds the **Right** bud (stream frame 7059, 16:15:51.551: 6.3.2 → 1 as the upper bud leaves; 7439, 16:16:10.762: → 2 as it is re-seated) |

## Capture-integrity pre-flight

```
$ capinfos CAP-063-btsnoop_hci.log
File encapsulation:  Bluetooth H4 with linux header
Packet size limit:   file hdr: (not set)
Number of packets:   11 k   (Interface #0: Number of packets = 11615)
Earliest packet time: 2026-09-27 15:57:40.614170
Latest packet time:   2026-09-27 16:26:58.325151

$ tshark -r CAP-063-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len -e frame.time_epoch \
  | awk '$2!=$3{c++} {if(NR>1 && $4<p) o++; p=$4} END{print "mismatches:",c+0,"out-of-order:",o+0}'
mismatches: 0 out-of-order: 1

$ sha256sum *   (identical before and after the move into this folder, `sha256sum -c`)
11e86f19…ffb39  CAP-063-btsnoop_hci.log            d946bb87…b0d55  CAP-063-btsnoop_hci.log.last
60afcebc…ff770  CAP-063-debug-export.log           c533fa33…51fa   CAP-063-OpenControl-for-Pixel-Buds-log-a5f9783708f6.txt
f8f9e1d6…a185   CAP-063-recording.mp4              4e22c894…394c   CAP-063-System-log-6cf0a8a3bd50.txt
$ wc -c CAP-063-debug-export.log  →  65536
```

## Video review method

- **Every frame, for privacy and screen state:** all 49,253 frames were decoded (`ffmpeg -fps_mode passthrough -vf crop=400:840:100:380,scale=50:105
  -pix_fmt gray`) and classified: frames not matching the app's own screen layout (bright page + bottom navigation bar) were grouped into 130
  segments ≥ 0.3 s and sampled every 1.5 s plus each segment's middle — 523 frames on 18 contact sheets, all viewed. **Personal data found:** the
  notification shade with legible third-party names and messages (WhatsApp group and contact names, LinkedIn, Volkskrant) and the maintainer's own
  e-mail address in a "Health" notification at overlay 15:57:43–44 (t ≈ 10.2–10.7 s), 16:06:24–39 (t ≈ 531–546 s), 16:12:55–57 (t ≈ 923–924 s),
  16:16:47 (t ≈ 1154 s, blurred) and 16:23:10–35 (t ≈ 1537–1562 s, partly behind the Bluetooth dialog); a LinkedIn notification behind the Bluetooth
  dialog at t = 0–9 s; the Spotify media card in Quick Settings; Android's device-details screen with the Buds' Bluetooth address at overlay
  16:17:09–12 (covered by ADR-010). **No camera address overlay.** The maintainer chose to keep the film **unblurred** (chat 2026-09-27,
  `ai-sessions/0053`; it was already committed and pushed in `ceaf05a`).
- **Content:** the draft's 1 fps pass over the whole film (69 contact sheets) was re-checked; transitions a claim depends on were narrowed with 1–5 fps
  crops (the clock flips, 16:02:03–16:03:33 case slot, 16:05:20–27 ANC tap, 16:09:18–29 conversation detection, 16:15:50–16:16:32 dock sequence,
  first/last frame).
- The film shows the phone and the case only: **when the buds are in the ears or in a hand off frame, the film cannot tell** — such states are marked
  "off film". The audio track is empty.

## Event Timeline

Times are **phone local time** (HCI / debug export); film-only events are overlay + 1.4 s, to ±1 s. Frame numbers are HCI frames of
`CAP-063-btsnoop_hci.log` (handle `0x000b` unless stated); "export N" is line N of `CAP-063-debug-export.log`. "Step" = the skeleton step
(Appendix A); "var." = done differently, "rep." = repeated. Test IDs: `APP_TESTPLAN.md`; registry Test-IDs in brackets.

| Time (phone) | Action / event | Actor | Step / test | Evidence |
|---|---|---|---|---|
| 15:57:34–15:57:38 | Film starts in Quick Settings, dialog "Bluetooth staat uit"; Bluetooth switched **on** there | User (Android) | S1 var., A1/A2 var. | film 15:57:33–37; HCI log starts 15:57:40.614; `.last` ends 15:57:16.332 |
| 15:57:45 | Notification shade | User | — | film (privacy) |
| 15:57:47–55 | OpenControl opened: *Nearby devices* → Toestaan; notifications → Toestaan | User | A3, A4 | film; export 1–3 (observer 15:57:55.7) |
| 15:57:55–57 | "Bluetooth permission needed", then "No Pixel Buds Pro 2 paired yet" + **Pair a device** — the Buds had been forgotten | App | B1 | film; the `.last` log has no Buds traffic |
| 15:57:59–15:58:06 | Case lid opened (both buds seated, LED) | User | [`CASE-003`] | film |
| 15:58:07 | Heads-up "Google Play services needs to sh…" (GmsCompat) | OS | — | film |
| 15:58:09–15:58:14 | **Pair a device** → CDM "Zoeken naar een apparaat" → "Toestaan … Pixel Buds Pro 2 van Ted" → Toestaan; bond; LE link 692, classic Create Connection 763 → Connection Complete 810 (`0x000b`) | User / OS | B2, B3 [`PAIR-001`] | export 4–10; HCI 692, 763, 810 |
| 15:58:14.3 | HFP (DLCI 0x0c) set up by the phone: `AT+BRSF` … `AT+BIEV=2,100` | OS | — | 1019 `SABM` 0x0c, 1051–1186 |
| 15:58:14.54–15:58:15.4 | **Opened by itself:** link CONNECTED → "Automatic re-open (ADR-044, trigger: LINK_BACK)" → app `SABM` DLCI 0x02 (1226) → announcement ch 21 (1261) → `ReadSetting 4:16` (1267 → 1279 `[-2.83,-5.89,2,3,5]`) → `4:2 4:4 4:7 4:17 4:19 4:22` (1293…1339: 1, 1, L/R 5, 7 = Right 4, 1, 1) → `SubscribeRuntimeInfo` (1340 → 1342: Case 37, L/R charging) | App | AY-0 var. (buds in the open case, right after pairing) [`PAIR-003`] | export 11–21; film "ready" 15:58:15 |
| 15:58:15.0–15:58:17.4 | Snapshot claim: attempt 1 collides with Play services' fresh DLCI 0x04 open (1275; phone `DISC` 1320), attempt 2 (1369): `08 11` 1379 → `03 03 00 03 e4 e4 ff` ×3, `Notify 01 e8 00 20` (1389); released 1548 | App / Play services | D1 | export 19, 22, 23 |
| 15:58:16 | Connection: "Firmware: release_5.203", no Safe Mode card; Left/Right "100% (updated 15:58:15) — charging in the case", "Case: 37% (updated 15:58:15)" | App | D1, E1, E2 [`BATT-004`] | film 15:58:15 |
| 15:58:16.6–16.7 | The Google app opens DLCI 0x0a/0x08 (1441/1470); Play services re-opens DLCI 0x04 (1708: `03 08`, `07 10`, `06 01`) and holds it until 16:00:16.8 | Google app / Play services | — | HCI; system log 13:58:30.336 UTC "Waited long enough for … BistoRealService" |
| 15:58:23 | Debug tab: **Debug mode on** | User | S3, L1 | film 15:58:21–22 |
| 15:58:39.9 | The phone closes the LE link (Disconnect 2178 → 2180, `0x16`) after 58 Fast Pair Key-based Pairing writes, each "Error 0x81" | Play services | — | HCI (out of scope, ADR-008) |
| 15:58:46–47 | ANC tab: "ANC mode: OFF (updated 15:58:15)", buttons disabled, "ANC can only be changed while you wear the Buds. Tap Refresh to check again." | App | F8 | film |
| 15:58:48–16:00 | Sound tab: "EQ updated 15:58:15", 5.0 / 3.0 / 2.0 / −5.9 / −2.8; presets in two rows, no label cut off; Balance "Right 4 · read 15:58:15", Mono **on**, Conversation detection **on** | App | B2, H0, H1, M1 | film; = the reads above |
| 15:59:25.6 | **LIGHT BASS** with both buds docked → `WriteSetting 4:{16:[-5,-1.5,0,0,0]}` (2374) → `RESPONSE` OK (2380); "EQ updated 15:59:26" | User / App | AY-2 (early) [`EQP-003`] | film 15:59:24–26 |
| 15:59:30–31 | Controls: "Use touch controls" on, Left/Right "Noise control", "In-ear detection (setting): on — read-only …", all "read 15:58:15" | App | B2, N1 | film |
| 15:59:35–53 | Tab tour by **swiping** (Find, Debug, Find, Controls, Sound, ANC, Connection) | User | B3 / C10 | film |
| 15:59:30.8 / 39.8 / 49.7 | Stream pushes while docked (2404, 2462, 2515; Case 37 → 36), each with `03 03` on Play services' DLCI 0x04 and `AT+BIEV=2,100` | Buds | E6 | film 15:59:52 "Case: 36% (updated 15:59:49)" |
| 16:00:00.36 | Android stops the Google app's `BistoRealService` ("app idle", 13:59:59.985 UTC); the phone closes DLCI 0x08/0x0a 373 ms later (2572/2573); closed until 16:15:39 | OS / Google app | — | system log; HCI |
| 16:00:10.16 | **Disconnect** → "App control: not open yet" | User | C3 | export 43; phone `DISC` 0x02 2628; film 16:00:08–09 |
| 16:00:16.59–16:00:18.9 | **Connect** (one tap) → one app `SABM` 0x02 (2670); reads; stream 2723; claim attempt 1 closes Play services' DLCI 0x04 (2699), attempt 2 (2742): `e4 e4`, `Notify 00 20` | User / App | C4, C5 var. (one tap, one `SABM`) | export 45–79; film 16:00:15–16 |
| 16:00:29.94 | **Disconnect** | User | AY-9 | export 80; 2837 |
| 16:00:31–16:00:53 | Upper (**Right**) bud taken out (≈ 16:00:31–34), then the lower (**Left**) (≈ 16:00:51–53); lid open, case empty; buds off film; `AT+BIEV` 2849/2940; the app **stays** "not open yet" | User | AY-9 ✓ [`CASE-005`, `CASE-004`] | film; no app `SABM` 0x02 until 2980 |
| 16:01:02.42 | **Connect** → ready (2980); claim 3022: battery `64 64` (3046), `Notify e8 e8 08` (3050); stream 3059 without 6.1 → "Left/Right 100% (updated 16:01:03) — not charging (out of the case) (16:01:04)", "Case: 36% — last seen 16:00:17 …" — for ≈ 2 s before that the previous session's "charging in the case (16:00:17)" lines stay | User / App | C4, E4 | export 82–124; film 16:01:00–05 |
| 16:01:15 | ANC tab "ACTIVE (updated 16:01:03)", buttons enabled (buds off film — worn) | App | — | film |
| 16:01:17–34 | Tab tour (Debug "Unidentified frames (16)") | User | B3 rep. | film |
| 16:01:03–16:02:09 | ANC mode `08` → `40` **without an app `Set`** (3050 → 3376) | Buds (🟡 press-and-hold) | (F5-like, not identifiable) | FINDINGS §3 |
| 16:02:06.15 | Buds `DISC` DLCI 0x02 (3285) as a bud comes from the hand/ear onto the **lower** slot (film: resting on the slot 16:02:06–16:02:16) — screen "The Maestro channel (equalizer) was closed …" for < 1 s, then "The Buds closed the app's channel …" | Buds / App | AY-7 var. | export 125–128; film t = 271 s |
| 16:02:07.68–16:02:09.5 | **Re-open by itself** (AFTER_LOSS, 1.5 s): `SABM` 3309, ch 19, reads, stream 3386 (**no bud charging**), claim 3348: `64 64`, `Notify e8 e8 40` | App | ADR-044 (a) ✓ | export 129–164 |
| 16:02:10–26 | Screen: both buds "not charging (out of the case) (16:02:09)", Case "last seen 16:00:17" while a bud lies on the lower slot — **the Buds' own report** (stream 3386, battery 3369) | App | E3 — app correct, bud not charging | FINDINGS §4 |
| 16:02:27.15 | **Disconnect** | User | AY-9 rep. | export 164; 3443 |
| 16:02:17–16:02:31 | The bud taken off the slot and laid back on it (film 16:02:16 off, 16:02:29 on); app stays closed | User | AY-9 rep. ✓ | film; no app `SABM` |
| 16:02:53–58 | Recents, home screen, back to the app: still "not open yet" | User | AY-9 ✓ (also on resume) | export 166–169; film |
| 16:03:04.18 | **Connect** → ready (3610); stream 3687 no bud charging; claim: `64 64`, `Notify e8 e8 40` (3678) | User | C4 rep. | export 170–205 |
| 16:03:16–18 | The bud taken off the slot and laid **left of the case** (on film until ≈ 16:04:01); the other off film | User | — | film |
| 16:03:34.36 | Stream push (3809) with `AT+BIEV` (3810) | Buds | E6 | film "(16:03:34)" |
| 16:03:47.03 | **Refresh battery** → fresh claim `SABM` 3889: burst `64 64` (3905), `Notify e8 e8 40` (3909, one bud on the table, the other off film); **one** `SubscribeRuntimeInfo` (3863) → answered 3870 (+263 ms) | User / App | AY-13a [`BATT-004`] | export 206–217; film 16:03:44–48 |
| 16:04:00.28 | Buds `DISC` 0x02 (3927) as the table bud is picked up (to an ear, off film) → re-open 1.5 s later (3949) | Buds / App | AY-5 var. ✓ | export 218–256; film 16:03:50–16:04:02 |
| 16:04:16.34 | **Refresh battery** → 4091/4107, re-subscription 4066 → 4078 | User | AY-13a rep. | export 257 |
| 16:04:22–26 | Both buds off film (in the ears); stream + `AT+BIEV` 4121/4122, 4128/4129 | User / Buds | — | film |
| 16:04:31.71 | **Refresh battery** → 4164/4180, `Notify e8 e8 08` (4184); re-subscription 4143 → 4152 | User | AY-13a rep. | export 272 |
| 16:04:42.50 / 16:04:43.87 | ANC tab: **ADAPTIVE** → `Set 40` (4233) → ACK 4241, `Notify` 4244; **OFF** 1.37 s later → `Set 20` (4249) → ACK 4251 | User / App | F6 var. ✓ [`ANC-003`, `ANC-001`] | film 16:04:39–44 |
| 16:04:54.95 / 16:05:01.39 | AVRCP PLAY (4284) / PAUSE (4329) from the Buds | Buds (tap or in-ear) | — | HCI |
| 16:05:26–27.97 | ANC tab: **ACTIVE tapped** (film overlay 16:05:24–25; not a Refresh) → claim 4485, `Set 08` (4497) → ACK 4508 → "ANC mode: ACTIVE (updated 16:05:28)" | User / App | F4 [`ANC-002`] (the draft's "F5 Refresh" corrected) | film t = 468–474 s; no `08 11` in the claim |
| 16:05:47 | Android volume panel; AVRCP VolumeChanged 33 % (4586) | User | — | film |
| 16:06:05–08 | Both buds taken **out of the ears** and laid on the table → Buds `DISC` 0x02 (4684, 16:06:08.05) | User / Buds | AY-5 var. [`INEAR-004`] | export 311; film 16:06:04–07 |
| 16:06:09.59–16:06:12.8 | Re-open by itself (4707); claim: `64 64`, **`Notify 01 e8 00 20`** (4774) → "ANC mode: OFF (updated 16:06:11)", buttons disabled + "ANC can only be changed while you wear the Buds." | App | AY-4 ✓, F8 ✓ | export 315–349; film 16:06:08–12 |
| 16:06:21–22 | Tap on the disabled ADAPTIVE: nothing | User | AY-4 ✓ | film; no DLCI 0x04 `SABM` |
| 16:06:26–40 | Quick Settings (shade 16:06:27), **ANC tile** → toast "ANC can only be changed while you wear the Buds." | User / App | AY-4 ✓, G3 | export 350 (16:06:30.286 "… nothing sent"); film (privacy) |
| 16:06:40–42 | Notification shade; back to the app | User | — | film (privacy) |
| 16:06:48 | Both buds picked up (then into the ears, off film) — **no** Buds `DISC` follows | User | D0 [`INEAR-002/003`] | film; next Buds `DISC` only 16:15 |
| 16:06:58–16:07:13 | ANC still "OFF (16:06:11)" and **disabled** although worn (no claim since); Sound tab: EQ Light bass (read 16:06:10), Balance Right 4, Mono on, CD on | App | — | FINDINGS §3 defect |
| 16:07:16.05 | **Balance → L**: `4:{17:200}` (5090) → OK (5092) → "Left 100 · changed 16:07:16" | User / App | D1, M2 [`AUDIO-003`] | export 357 |
| 16:07:22.20 | **Balance → R**: `4:{17:199}` (5095) → OK | User | D2, M3 | export 360 |
| 16:07:32.45 | **Balance ≈ halfway left**: `4:{17:104}` = Left 52 (5129) → OK | User | D3, M3 | export 363 |
| 16:07:43–16:08:00 | Twelve short touches near the centre: `17:21, 19, 7, 1, 1, 3, 22, 14, 11, 6, 7, 1` (5180 … 5245), all OK — "Centre" never reached | User | D4 rep. (✗ "Centre") | export 366–399 |
| 16:08:06.90 | **Mono off**: `4:{19:0}` (5261) → OK | User | D6 (D5 skipped: already on) [`AUDIO-001`] | export 404 |
| 16:08:20.45 / 16:08:30.21 | **Conversation detection off** (5280) / **on** (5294), each OK | User | D7, M5 [`CONV-001`] | export 407, 410 |
| 16:08:50–58 | Home → "Luisteren" → Spotify, **play** (AVRCP PlaybackStatus Playing 5319) → back to Sound | User | P10 var. (music, not a stereo test file) | film; export 411–414 |
| 16:09:00–16:09:11 | Balance again: `17:199` (Right 100), `192` (Left 96), `72` (Left 36), `21` (Right 11), `7` (Right 4) — all OK | User | D1–D4 rep. | export 417–429 |
| 16:09:12.36 / 16:09:14.91 | **Mono on** (5371) / **off** (5375), OK | User | D5, D6 rep. | export 432/435 |
| 16:09:17.88 / 16:09:20.82 | **Conversation detection off** (5381) / **on** (5386), OK | User | D7 rep. | export 438/441 |
| 16:09:22.95 / 16:09:30.36 | AVRCP **PAUSE** (5389) / **PLAY** (5433) from the Buds while the maintainer talks (off film; no hand near the phone on film) | Buds (🟡 conversation detection) | D7 / M5 [`CONV-002`] | FINDINGS §7 |
| 16:09:41.03 | **Use touch controls off**: `4:{4:0}` (5471) → OK | User | D8, N2 [`TOUCH-001`] | export 446 |
| 16:09:41–16:09:59 | Bud taps (off film): **no** AVRCP pass-through from the Buds | User / Buds | D8 ✓ [`TOUCH-002`] | HCI (none between 5437 and 5496) |
| 16:09:58.99 | **Use touch controls on**: `4:{4:1}` (5490) → OK; taps → PAUSE 16:10:11.23 (5496), PLAY 16:10:15.13 (5532) | User / Buds | D9, N2 | export 451 |
| 16:10:36.36 | **Left: Digital assistant**: `7{1:{4:{1:6}}}` (5654) → OK | User | D10, N3 [`HOLD-002`] | export 454 |
| 16:10:48.45 / 16:10:55.28 | **Left: Noise control** (5684) / **Digital assistant** (5690) | User | D11, D10 rep. [`HOLD-001`] | export 457/460 |
| 16:11:23.97 / 16:11:34.76 | **Right: Digital assistant** (5711) / **Noise control** (5722) | User | D12, N4 [`HOLD-004`, `HOLD-003`] | export 463/466 |
| 16:11:36.17 | AVRCP VolumeChanged 26 % from the Buds (a volume gesture), volume panel on film | Buds | — | 5725; system log 14:11:36.251 UTC |
| 16:11:44.18 | AVRCP PAUSE (5733) | Buds | — | HCI |
| 16:11:49–16:12:10 | Hold chips: L Noise control (5770), R Assistant (5781), L Assistant (5786), L Noise control (5796), R Noise control (5803) — ends **both Noise control**; press-and-holds off film; no `AT+BVRA`, no GSND channel open (FINDINGS §7) | User | D10–D12 rep. | export 469–481 |
| 16:12:30.17 / 16:12:32.33 | **Disconnect**, **Connect** → ready (5855); reads 5874…5929 = the last writes; claim `Notify e8 e8 08` (5923) | User | D13 ✓, M6, N5 [`PAIR-003`] | export 482–522; film 16:12:37–45 "read 16:12:3x" |
| 16:12:58–16:13:08 | Notification shade (privacy), Recents → Spotify → back | User | — | film; export 523–526 |
| 16:13:17.86 | **HEAVY BASS**: `4:{16:[5,3,0,0,0]}` (6009) → OK | User | H2 partly [`EQP-002`] | film 16:13:16–18 |
| 16:13:24.35 | **Upper treble** released at +5.23 (6017) → OK | User | H3 var. [`EQS-001`] | film |
| 16:13:31.92 | **Low bass** released at −5.71 (6026) → OK | User | H4 var. [`EQS-005`] | film |
| 16:13:47–59 | Scroll; "Read EQ again" not shown (no error) | User | H5 not run | film |
| 16:14:06.85 | **Refresh battery** → claim 6079 (burst 6094), re-subscription 6057 → 6064 | User | AY-13b part 1 | export 535 |
| 16:14:09.92 | **Refresh battery** again (3.07 s later, after the linger) → 6128/6143, 6107 → 6114 | User | AY-13b var. | export 546 |
| 16:14:49.92 / 16:14:50.72 | **Refresh battery twice, 0.79 s apart** → claim 6216 (burst 6232), "still claimed … released for a fresh claim", `DISC` 6240, fresh `SABM` 6263 (burst 6281); two re-subscriptions 6195/6239, answered 6202/6249 | User / App | **AY-13b ✓** | export 557–579 |
| 16:15:08.60 | AVRCP PAUSE (6301) as the buds come out of the ears | Buds | [`INEAR-004`] | HCI |
| 16:15:08–16:15:21 | Case picked up; **Left** seated first (stream 6338, 16:15:14.895: 6.2.2 = 2, Case 36), then Right; screen dims/off | User | AY-7 var. (both at once) [`CASE-006`] | film 16:15:07–19 |
| 16:15:21.995 | ACL `Disconnection Complete` `0x13` (6378) — both seated (ADR-016); the app, not visible (observer stopped 16:15:21.408), shows "The Maestro channel (equalizer) was closed. Tap Connect to reconnect." (loss cause undetermined) | Buds / App | AY-6 var. | export 586–593; film 16:15:24; FINDINGS §6 #8 |
| 16:15:25–33 | Android does **not** reconnect by itself (no Create Connection) | OS | — | HCI |
| 16:15:33.85 | **Connect** (Android not connected): the app's socket creates the ACL (Create Connection 6490 → 6493) → ready (6573); reads; stream 6658 both charging, Case 36; `Notify 00 20` (6635); Android "Connected" 16:15:38.86 | User / App | C7-like, E5 | export 594–628; film 16:15:33–39 |
| 16:15:38.65 | The Buds open HFP on DLCI 0x09 (6769, Buds-initiated); the Google app re-opens DLCI 0x0a/0x08 (6919/6947) | Buds / Google app | — | system log 14:15:37.389 UTC Bisto started |
| 16:15:48–53 | Upper (**Right**) bud out → stream 7059 (16:15:51.551): 6.3.2 = 1, 7.1 = 0 → "Right … not charging (16:15:52)", Left charging | User / App | E3 ✓, AY-7 [`CASE-005`] | film 16:15:47–52 |
| 16:15:54–56 | Lower (**Left**) out → stream 7112 (16:15:54.748) without 6.1 → both not charging, "Case: 36% — last seen 16:15:52 …" | User / App | E4 ✓ [`CASE-004`] | film 16:15:53–55 |
| 16:15:58–16:16:05 | Phone locked (screen off); buds off film | User | AY-10 var. | film; export 641 (observer stopped 16:16:04.440) |
| 16:16:05.56 | Buds `DISC` 0x02 (7175) while the app is not visible; the phone also closes DLCI 0x08/0x0a (7187/7188) | Buds | AY-10 ✓ (accidental) | export 644 |
| 16:16:06.21–16:16:10 | On return: re-open (LINK_BACK) — one `SABM` 7350; stream 7433 both out; claim `Notify e8 e8 40` (7420; buds off film) | App | ADR-044 (c) ✓ | export 647–690; film 16:16:05–07 |
| 16:16:09.5–10.8 | **Right** re-seated (upper) → stream 7439: 6.3.2 = 2, Case 36 → "Right … charging in the case (16:16:11)" | User / App | AY-7 ✓ | film 16:16:08–11 |
| 16:16:25.6 / 16:16:30.5 | Stream 7513 (Case 36), 7555 (Case **34**), Left still in the hand → screen "Left … not charging (16:16:30)", "Case: 34% (updated 16:16:30)" | Buds / App | — (the draft's "Left charging 16:16:30" corrected) | film t = 1133–1138 s |
| 16:16:33–34 | **Left** seated (lower) | User | AY-7 [`CASE-006`] | film 16:16:31–32 |
| 16:16:35.89 | ACL `0x13` (7566) — second bud seated; "Android no longer shows the Buds connected …" | Buds / App | AY-6 ✓ | export 694–699 |
| 16:16:49–54 | Notification shade (privacy); QS Bluetooth: "Pixel Buds Pro 2 van Ted — Opgeslagen" | User | — | film |
| 16:17:02.69 | Buds row tapped → Create Connection 7611 → 7613 → "Actief. Batterijniveau 100%" | User (Android) | AY-8b var. | film 16:17:01–06 |
| 16:17:05.83 | **Re-open by itself** (LINK_BACK, 7810); stream 8082 both charging, Case 34; `Notify 00 20` (8032) | App | ADR-044 (b) ✓ | export 700–735 |
| 16:17:10–14 | Android's device-details screen (shows the Bluetooth address) | User | — | film (ADR-010) |
| 16:17:18 / 28, 16:18:18 / 29 | Stream pushes while docked and idle, each with `AT+BIEV` (8165/8169, 8200/8201, 8306/8307, 8380/8381) | Buds | E6 ✓ | film (times re-stamped) |
| 16:17:21–16:18:40 | Idle, both docked, lid open (≈ 93 s, not 2 min) | User | AY-14 var. | film |
| 16:18:40.33 | **Refresh battery** → re-subscription 8417 → **answered** 8426 (+194 ms, Case 34) → "Case: 34% (updated 16:18:40)"; claim 8439: `e4 e4`, `Notify 00 20` | User / App | **AY-14 ✓ (answered)**, E9 | film 16:18:38–40 |
| 16:18:48.22 | Find: **Ring Left**, both docked → `04 01 00 01 02` (8518) → ACK `ff 01 00 03 04 01 00` (8520) + echo (8521); "Ringing: Left earbud …" | User / App | **AY-1** [`FIND-001`] | film 16:18:45–47 |
| 16:19:09.49 | **Stop** → `04 01 00 01 00` (8598) → ACK (8600) | User | AY-1 | film 16:19:07–08 |
| 16:19:25.86 | **BALANCED** while docked → `4:{16:[-3.5,0.5,1,-1,2.5]}` (8633) → OK | User | **AY-2 ✓** [`EQP-004`] | film 16:19:14–24 |
| 16:19:36.09 / 16:19:37.80 | **Mono on** (8662) / **off** (8668) while docked → OK | User | **AY-2b ✓** | film 16:19:34–37 |
| 16:19:46–48 | **Lid closed** (both docked) → ACL `0x13` (8687, 16:19:48.16) | User / Buds | C8 ✓ [`CASE-006`] | film 16:19:44–46 |
| 16:20:06 | "Paired — not connected … Android no longer shows the Buds connected …" | App | C8 ✓ | film |
| 16:20:14–15 | **Lid opened** (both docked): nothing on any channel; Buds' LE adverts resume (FINDINGS §8) | User | [`CASE-003`] | film 16:20:12–13 |
| 16:20:24–26 | Right, then Left taken out → the Buds page the phone (Connect Request 8754, 16:20:25.84) → ACL (8758) → app `SABM` 9035 (16:20:27.495) by itself; streams 9116/9247; `Notify 00 20` (9094, buds in the hand) | User / Buds / App | C9 ✓, E4 ✓ [`CASE-005`, `CASE-004`] | film 16:20:22–28 |
| 16:20:29–16:21:02 | Buds on the table, then picked up (one stays left of the case from ≈ 16:21:01) | User | — | film |
| 16:21:22 | **Home** (app in the background) | User | AY-10 / J4 | film |
| 16:21:49.0 | Bisto stopped (14:21:48.847 UTC) → the phone closes DLCI 0x08/0x0a (9617/9618) | OS | — | system log; HCI |
| 16:21:48–58 | Screen off, unlock, Recents → OpenControl: still **ready** | User / App | AY-10 var. (no loss while in the background) | film |
| 16:22:03–16:23:00 | **Home** again, ≈ 57 s (screen off 16:22:28–35) | User | J4 var. (≈ 1 min) | film |
| 16:23:00–02 | Back: still ready | App | J4 ✓ | film |
| 16:23:10–14 | Notification shade (privacy); QS Bluetooth: "Pixel Buds Pro 2 … Actief" tapped → **disconnect in Android**: phone `DISC` HFP DLCI 0x09 (10001, 16:23:14.29) → ACL `0x13` (10030, 16:23:15.72) → "Opgeslagen" | User (Android) | AY-8 [`PAIR-003`] | film 16:23:09–13 |
| 16:23:35–40 | Buds row tapped again → Create Connection 10075 → ACL 10077 → app **ready by itself** (10247, 16:23:37.62); `Notify e8 e8 40` (10379) | User / App | AY-8b ✓ | film 16:23:34–39 |
| 16:23:47–50 | Both buds laid on the table → Buds `DISC` (10644, 16:23:50.48) → re-open 1.5 s later (10776) | User / Buds / App | AY-5 var. ✓ | logcat 14:23:50.493–14:23:52.019 UTC; film 16:23:46–52 |
| 16:24:14.19 | Find: **Ring Left** (buds on the table) → 10951 → ACK 10959 + echo 10962 | User / App | AY-11 part 1 [`FIND-001`] | film 16:24:12–13 |
| 16:24:21.28 | **Disconnect** (10981) | User | AY-11 part 2 | logcat 14:24:21.283 UTC |
| 16:24:32–36 | Find tab: "Not connected …" + "A ring was started on the Left earbud — reconnect and tap Stop to end it." | App | AY-11 ✓, I4 ✓ | film 16:24:31–35 |
| 16:24:38.68 | **Connect** → ready (11043); claim 11087 `Notify 00 20` | User | AY-11 part 3 | logcat 14:24:38.680 UTC |
| 16:24:41 | Find: "A ring was started on the Left earbud before the app reconnected — it may still be ringing. Tap Stop to end it." | App | AY-11 ✓ | film 16:24:40 |
| 16:24:44.13 | **Stop** → `04 01 00 01 00` (11167) → ACK (11178) + echo; the notice disappears | User / App | AY-11 part 4 | film 16:24:42–43 |
| 16:24:58.35 | Bisto stopped (14:24:58.323 UTC) → DLCI 0x08/0x0a closed (11223/11224) | OS | — | system log; HCI |
| 16:25:03–04 | Debug tab: "Unidentified frames (163)"; film ends | User | — | film |
| 16:25:38 (logcat) … 16:26:58 | App stopped (observer stopped 14:25:38.385 UTC); ACL `0x13` 11543 at 16:26:33.94; HCI log ends; the export was saved off film (file dated 16:40) | — | L2 (off film) | logcat; HCI |

## Step mapping (skeleton Appendix A → what happened)

| Step | Result | Evidence |
|---|---|---|
| S0 minute change, spoken hash | **skipped** | film |
| S1/S2 Bluetooth off → app "disabled" → Enable | **done differently** (Bluetooth on in Quick Settings first) | film 15:57:33–37 |
| S3 Debug mode on | done | film 15:58:21 |
| (not planned) pairing from the app B1–B3 | done | export 4–10 |
| AY-0 | done differently (right after pairing, buds docked) ✓ | export 11–14 |
| B1, B2, B3 | done ✓ | film 15:58:15–15:59:52 |
| C5 | done differently (one tap) — one `SABM` ✓ | 2670 |
| AY-9 | done, repeated ✓ | 2837→2980, 3443→3610 |
| C4 | done, repeated ✓ | 2980, 3610 |
| AY-3a/b/c | **skipped** | — |
| F6 | done differently (1.37 s apart) ✓ | 4233/4249 |
| F7 | **skipped** | — |
| F5 | **not identifiable** (three mode changes without an app `Set`, gestures off film; the 16:05:26 action was an ACTIVE tap) | FINDINGS §3 |
| AY-4 | done ✓ (buttons + tile) | 4774; export 350 |
| D0 | done | film 16:06:47 |
| D1–D4 | done, repeated; D4 ✗ "Centre" never shown | 5090 … 5368 |
| D5, D6 | D5 skipped once (already on), then done; repeated ✓ | 5261, 5371, 5375 |
| D7 | done, repeated ✓; speaking → AVRCP PAUSE/PLAY (🟡) | 5280/5294/5381/5386; 5389/5433 |
| D8, D9 | done ✓ | 5471, 5490 |
| D10–D12 | done, repeated ✓ (writes); the phone's reaction to an assistant hold: nothing on the wire | 5654 … 5803 |
| D13 | done ✓ | 5874–5929 |
| H2 | partly (Heavy bass; Light bass and Balanced at other times) | 6009, 2374, 8633 |
| H3, H4 | partly (+5.23, −5.71; no ±6 pair) | 6017, 6026 |
| H5 | not run (button not shown) | film |
| AY-13a | done, repeated (5×) ✓ | export 206, 257, 272, 535, 546 |
| AY-13b | done ✓ (16:14:49.9/50.7, 0.79 s) | export 557–568 |
| AY-7 | done differently (both at once, 16:15:08–21), then per bud (16:15:48–16:16:34) ✓ | stream 6338 … 7555 |
| AY-6 | done ✓ (16:15:22, 16:16:35) | 6378, 7566 |
| AY-14 | done differently (≈ 93 s idle) ✓ answered | 8417 → 8426 |
| AY-1, AY-2, AY-2b | done ✓ | 8518, 8633, 8662/8668 |
| C8 | done ✓ | 8687 |
| C9 | done ✓ | 8754 → 9035 |
| AY-5 | done differently (3×: from the table, out of the ears, onto the table) ✓ | 3927, 4684, 10644 |
| AY-10 | done differently: the planned background loss did not happen at 16:21; an accidental one at 16:16:05 during a screen lock ✓ | 7175 → export 650 |
| J4 | done differently (≈ 1 min) ✓ | film |
| AY-8, AY-8b | done ✓ | 10001/10030, 10075 → 10247 |
| AY-11 | done ✓ | 10951 … 11178 |
| K4, K1, K2, K3, K5 | **skipped** | — |
| L2 | done off film (file dated 16:40) | the export file |
| L3 | **skipped / not identifiable** | — |
| A5, (E), B4 | **skipped** | — |
| Z1 restore | **not done**: at the end Balance Right 4 ✓ (original), Mono **off** (was on), Conversation detection on ✓, EQ Balanced (was 5/3/2/−5.9/−2.8), touch and hold as original | 16:24:40 reads (logcat/HCI 11043 …) |
| Z2 minute change | **skipped** | film |

**Traceability (`AGENTS.md` §13.7):** every `APP_TESTPLAN.md` test the skeleton names — A5, B4, C5, F5–F7, H5, J4, K1–K5, L3, (E), (F) — and every
`0048` §9 AY-0 … AY-12 and `0051` §19 AY-13/AY-14 step appears above with a result or an explicit "skipped" / "not identifiable". Registry Test-IDs
exercised: `PAIR-001`, `PAIR-003`, `CASE-003`–`CASE-006`, `BATT-004`, `ANC-001`–`ANC-003` (`ANC-004` Transparency not tapped), `FIND-001`, `EQP-002`–
`EQP-004`, `EQS-001`, `EQS-005`, `CONV-001`, `CONV-002` (🟡), `TOUCH-001`, `TOUCH-002` (🟡, off film), `HOLD-001`–`HOLD-004` (writes), `AUDIO-001`,
`AUDIO-003`, `INEAR-004` (🟡). Not exercised: `FIND-002`, `EQP-005`/`006`, `EQS-002`–`004`, `AUDIO-002`, `HOLD-005`, `INEAR-001`.

## Analysis checklist

- [x] Integrity pre-flight, hashes, audio check.
- [x] Pre-filter by handle `0x000b` / `0x0042`; the other LE device `0x0040` excluded.
- [x] Clock offsets measured at two minute flips (phone = overlay + 1.4 s); logcat/system log − 2 h.
- [x] DLCI 0x02 decoded in full (`pwrpc_decode.py`); every request byte-compared with `SettingsFixtures.kt`.
- [x] DLCI 0x04 per-claim table (29 app claims, every one with a `03 03` burst); AY-13b two claims.
- [x] "Refuted if" list applied (FINDINGS §1): nothing refuted.
- [x] Session-end table (FINDINGS §6); protocol correlation (FINDINGS §10).
- [x] Registry Test-IDs referenced above; the Capture Index row, Group AY and `id_registry.csv` are updated by `ai-sessions/0053`.

---

## Appendix A — the procedure as planned (the skeleton, written 2026-09-26 before the capture; unchanged)

### A.0. Which phone, which app, which build

| Item | Value |
|---|---|
| Phone | **Pixel 9a, GrapheneOS** (Android 17; record the build number from Settings → About phone: `__________`) |
| App under test | **OpenControl for Pixel Buds**, debug APK built from this repository |
| Build (P1) | **The commit that completes `ai-sessions/0052`** (the settings UI: tab "Sound" with balance/mono/conversation detection, tab "Controls" with touch controls, press-and-hold, in-ear detection). Commit hash: `__________` (`git log -1 --format=%H`). ⚠️ At the time this skeleton was written that UI was **not yet built** (`0052` RESULT, Progress). With an older build (e.g. `0048`) run only the steps **not** marked **[0052]** and write that down here. |
| Official Pixel Buds app | **Not used** in this session. The Pixel 9a has none; do not open it on any other phone during the session |
| Pixel 7a | **Bluetooth off** for the whole session (the Buds support multipoint: a 7a connection would add a second phone's traffic to the Buds' behaviour) |
| Other Bluetooth devices near the 9a | Watch, car, speaker: Bluetooth off or out of range. If something else connects anyway, write it down (`CAP-004` had Fitbit traffic in its log) |
| Buds | Pixel Buds Pro 2, firmware `release_5.203` (the app shows it; a different version means Safe Mode — stop and report, §2 P10) |

---

### A.1. Log Metadata (fill in)

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-063`                     |
|      Group(s)    |                        AY                          |
|       Date       |                                                     |
| Firmware version | (from the Connection tab "Firmware: …")             |
|   Test device    | Pixel 9a, GrapheneOS, build `…`; OpenControl commit `…`; Google Play services present: yes/no; Play services *Nearby devices*: allowed/denied (P4) |
| Video file       | `CAP-063-recording.mp4` — first/last overlay time: … / … |
| Log file         | `CAP-063-btsnoop_hci.log` — raw path used (`FS/data/log/bt/…` or `FS/data/misc/bluetooth/logs/…`) or `btsnooz.py`: … |
| App debug export | `CAP-063-debug-export.log` (Debug mode on from …) |
| App logcat       | `CAP-063-OpenControl-for-Pixel-Buds-log-<id>.txt` |
| System log       | `CAP-063-System-log-<id>.txt` |
| Events file      | CAP-063-events.txt (your own time + action notes, P6) — not provided for this run |
| Clock offsets    | Film ↔ phone: minute change filmed at start (`__:__`) and end (`__:__`) — measured in the analysis |

---

### A.2. Preparation — the day before / before filming

Tick each box. Nothing here is optional unless it says so.

| # | Check | Done |
|---|---|---|
| P1 | Build the debug APK of the commit named in §0 (`cd android && ./gradlew assembleDebug`) and install it (`adb install -r app/build/outputs/apk/debug/app-debug.apk`). Write the commit hash in §0 **and** say it aloud on film at the start | ☐ |
| P2 | Do **not** clear the app's data (the permissions stay granted; A5 below revokes one on purpose, at the end) — unless you want to repeat the first-start flow; then write that down | ☐ |
| P3 | Settings → System → Developer options: **Enable Bluetooth HCI snoop log = Enabled**, **USB debugging = on**. Then **reboot the phone** (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §2 step 5) — the log starts after a Bluetooth restart | ☐ |
| P4 | Settings → Apps → Google Play services → Permissions → *Nearby devices*: write down **allowed / denied** and **leave it as it is** for the session | ☐ |
| P5 | Buds and case charged (> 50 %). Buds in the case, lid closed, at the start | ☐ |
| P6 | Events file ready (a notes app on another device, or paper): for **every** step write the phone time (HH:MM:SS) and what you did — the step IDs below make this short ("AY-5 14:02:11 Right out") | ☐ |
| P7 | **Camera** (on a tripod) films the phone screen **and** the case/buds on the table in one frame, sharp enough to read the screen text; the camera's own burned-in overlay shows date and time. **Switch off any location/street-address overlay on the camera** (an earlier film carried one) | ☐ |
| P8 | Camera microphone on: you will **say aloud** what you hear (EQ, balance, mono, ANC, ring). Quiet room; no music from other sources | ☐ |
| P9 | Phone: **Do Not Disturb on** (earlier films showed private notifications in the shade); only open the notification shade when a step asks for it | ☐ |
| P10 | A **stereo test file** stored **on the phone** (e.g. a left/right channel test and a piece of music you know well), playable offline in any audio player — needed for H, the balance and mono steps | ☐ |
| P11 | GrapheneOS Settings → Security → *Bluetooth auto-off timeout*: write down the current value `______` (K5 uses it; set it to the shortest value only for K5, restore afterwards) | ☐ |
| P12 | Know the Buds' touch gestures: **tap** = play/pause, **press-and-hold** = the action set in the app (Noise control cycles the ANC modes). Know which side you wear which bud (L/R printed inside the bud) | ☐ |
| P13 | Laptop with `adb` and `platform-tools` next to you for §6 (the bug report takes minutes — start it within a minute of the last action) | ☐ |
| P14 | Read this whole file once before starting | ☐ |

**Rhythm for every step** (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4): wait ~5 s → note the time → do **one** action → wait 5–10 s (longer where a step
says so) → next step. If something unexpected happens, **stop, say it aloud, write the time**, then continue with the next step; do not repeat a step
without writing "repeat" in the events file.

**Record the original values (needed to restore the Buds at the end, step Z1):** after step B2, copy what the app shows:

| Setting | Original value (as read at Connect) |
|---|---|
| EQ (five sliders) / preset | |
| Balance | |
| Mono audio | |
| Conversation detection | |
| Use touch controls | |
| Press and hold — Left / Right | |
| In-ear detection (setting) | |
| ANC mode | |

---

### A.3. Start of the film

| Step | Action | Expected on screen | Time (phone) | Result / what you saw |
|---|---|---|---|---|
| S0 | Start the camera. Film the phone's status bar across a **minute change** (≥ 10 s before and after). Say aloud: date, "CAP-063, Group AY", the commit hash, Play services *Nearby devices* state | — | | |
| S1 | Quick Settings: **Bluetooth off**. Open OpenControl | "Bluetooth is disabled." + **Enable Bluetooth** (A1) | | |
| S2 | Tap **Enable Bluetooth** | Android's own "turn on Bluetooth" prompt; allow → the app leaves the "disabled" state (A2) | | |
| S3 | Debug tab: **Debug mode on** | "Unidentified frames (n)" appears (L1) | | |

---

### A.4. Event Timeline (the test steps)

Legend: **[0052]** = only with the build of §0 P1 (skip with an older build). "Expected on the wire" is for the analysis — you do not need to check it
while filming. HCI bracket abbreviations: `SABM`/`DISC` = RFCOMM channel open/close; DLCI 0x02 = the app's session (MAESTRO), 0x04 = Message Stream.

### A. Connect by itself, what the app reads

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| AY-0 | Buds in the case, lid closed; app on screen | Take both buds out and **put them in your ears**. Do **not** tap Connect | "Connected to this phone (Android)", then **by itself** "App control: connecting…" → "ready" (ADR-044) | app `SABM` 0x02 → Buds `GetSoftwareInfo` → `ReadSetting 4:16`, then **[0052]** `ReadSetting 4:2, 4:4, 4:7, 4:17, 4:19, 4:22` → `SubscribeRuntimeInfo` → DLCI 0x04 snapshot claim (`08 11` → `08 13 … e8 …`) | | |
| B1 | ready | Connection tab: read it aloud | "Firmware: release_5.203"; **no** Safe Mode card; Left/Right % with "(updated HH:MM:SS)" (D1) | — | | |
| B2 | ready | **[0052]** Open tab **Sound**, then tab **Controls**; read every value aloud and fill in the "original values" table (§2) | Each setting shows a value with "read HH:MM:SS", or "Not read from the Buds yet"; Controls shows "In-ear detection (setting): on/off — read-only; not whether a bud is worn". The EQ presets are **two rows** (3 + 2) and no label is cut off | the six `ReadSetting` answers above | | |
| B3 | ready | Tab tour with the **bottom bar**, then by **swiping** (C10); count the tabs aloud | Tabs **Connection, ANC, Sound, Controls, Find, Debug** (with [0052]); Back does not jump to Debug | — | | |

### B. Connect / Disconnect edge cases

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| C5 | ready | **Disconnect**, wait 5 s, then tap **Connect twice quickly** | One session, no error (C3 then C5) | exactly **one** app `SABM` 0x02 | | |
| AY-9 | ready | Tap **Disconnect**. Take **one** bud out of your ear and put it back; press Home and return to the app | Stays "App control: not open yet" — **no** automatic re-open after a Disconnect tap | **no** app `SABM` 0x02 until the next Connect tap | | |
| C4 | not open (after AY-9) | Tap **Connect** | ready | app `SABM` 0x02 | | |

### C. ANC while worn / not worn

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| AY-3a | both buds worn, ready | Take the **Left** bud out and lay it on the table (Right stays in your ear). Wait 10 s. ANC tab → **Refresh** | Mode buttons **enabled** or "ANC can only be changed while you wear the Buds." — say which | `08 11` → `08 13 00 04 01 e8 <settable> <mode>` — the test of "Settable `0x00` = not worn" (ADR-024 Update, 🟡) | | |
| AY-3b | Left on the table, Right worn | Swap: Left **in**, Right **out**. Wait 10 s. **Refresh** | as AY-3a — say which | as AY-3a | | |
| AY-3c | one worn | Right back **in** (both worn). **Refresh** | buttons enabled | Settable `e8` | | |
| F6 | both worn | Tap **ADAPTIVE** and immediately **OFF** | The last one (OFF) wins; no error or hang; say what you hear | two `08 12`, in order, each answered | | |
| F7 | both worn | Tap **ACTIVE**, press Home **immediately**; come back after 5 s | ANC ACTIVE applied (audible), no stuck state | `08 12 … 08` → ACK; later `DISC` 0x04 (release) | | |
| F5 | both worn, app on the ANC tab | **Press and hold** the Right bud once (with press-and-hold = Noise control it switches the ANC mode — you hear it); then tap **Refresh** | After Refresh the screen shows the bud's new mode | `08 11` → `08 13 … <new mode>` | | |
| AY-4 | both worn | Take both buds out, lay them on the table. ANC tab: tap a mode. Then pull down Quick Settings and tap the **ANC tile** once | Buttons disabled + "ANC can only be changed while you wear the Buds."; tile subtitle "Only while worn" + the same message | **no** `08 12` and no DLCI 0x04 `SABM` for the tap | | |

### D. Settings writes [0052] (buds **in your ears**, music or the test file playing, say aloud what you hear)

Put both buds back in your ears first (step D0: time `______`). The app may re-open by itself after the Buds close the channel — wait for "ready".

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / what you hear |
|---|---|---|---|---|---|---|
| D1 | stereo test file playing | Tab **Sound** → drag **Balance** fully to **L** and release | "Left 100 · changed HH:MM:SS"; sound only (or mostly) in the **left** ear | one `WriteSetting 4:{17:200}` (+100 = Left, zigzag) → empty `RESPONSE` | | |
| D2 | | Drag Balance fully to **R**, release | "Right 100 · changed …"; sound in the **right** ear | `4:{17:199}` → `RESPONSE` | | |
| D3 | | Drag Balance to about **halfway left**, release; say the number shown | "Left NN"; left louder, right still audible | `4:{17:<2·NN>}` | | |
| D4 | | Drag Balance back to the **centre** (the number shown should read "Centre"), release | "Centre" | `4:{17:0}` (or a small value) | | |
| D5 | | **Mono audio on** (the left/right test: both ears now play both channels) | switch on, "changed …" | `4:{19:1}` → `RESPONSE` | | |
| D6 | | **Mono audio off** | switch off; the channels separate again | `4:{19:0}` | | |
| D7 | ANC ACTIVE, music playing | **Conversation detection**: tap it **off**, wait 10 s, tap it **on** (from whatever the original value was, end with it on); then **speak** a sentence for ~5 s | Switch follows each tap with "changed …"; while speaking with it on: the Buds switch to transparency / pause (say what happens) | `4:{22:0}` / `4:{22:1}` → `RESPONSE` each | | |
| D8 | music playing | Tab **Controls** → **Use touch controls off**; then **tap** a bud once | switch off; the tap does **not** pause the music | `4:{4:0}` → `RESPONSE`; no DLCI traffic for the tap | | |
| D9 | | **Use touch controls on**; tap a bud once | switch on; the tap pauses/plays | `4:{4:1}` | | |
| D10 | | **Press and hold — Left: Digital assistant**; then press-and-hold the **Left** bud | value "Digital assistant · changed …"; say what the phone does (GrapheneOS may have no assistant) | `4:{7:{1:{4:{1:6}}}}` → `RESPONSE` | | |
| D11 | | **Press and hold — Left: Noise control**; press-and-hold the Left bud | ANC mode changes (audible) | `4:{7:{1:{4:{1:5}}}}` | | |
| D12 | | Same for **Right**: Digital assistant, hold Right, then Noise control, hold Right | as D10/D11 for Right | `7{2:…6}`, `7{2:…5}` | | |
| D13 | | **Disconnect**, **Connect**; open Sound and Controls | Every value just written is **read back** ("read HH:MM:SS") — balance, mono, conversation detection, touch controls, both hold actions | the six `ReadSetting` answers = the last writes | | |

### E. Equalizer audibility (buds in your ears, the music you know playing)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / what you hear |
|---|---|---|---|---|---|---|
| H2 | ready, Sound tab | Tap **HEAVY BASS**, wait 10 s; **LIGHT BASS**; **BALANCED**; **VOCAL BOOST**; **CLARITY** — say after each what changed | sliders move to the preset | one `WriteSetting 4:{16:…}` per tap → `RESPONSE` | | |
| H3 | | Drag **Upper treble** to +6, release; then to −6 | slider stays; audible? | one write per release | | |
| H4 | | Drag **Low bass** to +6, then −6 | audible? | as H3 | | |
| H5 | | Tap **Read EQ again** (only visible after an error; if it is not there, write "not shown") | the values just written | `ReadSetting 4:16` | | |

### F. Refresh battery

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| AY-13a | ready, buds worn, **no** action for ≥ 10 s | Connection tab: **Refresh battery** once | Left/Right "(updated HH:MM:SS)" = now; under the button "The Buds report the Case level only while a bud is in the case." **[0052]** | app `SABM` 0x04 → `03 03 00 03 …` burst → `08 11`/`08 13`; **[0052]** one `SubscribeRuntimeInfo` on DLCI 0x02 | | |
| AY-13b | right after AY-13a | Tap **Refresh battery twice within 1 s** | Both times new "(updated …)" times — or "No new battery reading from the Buds — try again." — never an old time presented as new | **[0052]** two `SABM`s on DLCI 0x04 (the lingering claim released in between: `DISC` then `SABM`), each followed by `03 03` | | |

### G. Buds in the case (lid open)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| AY-7 | both worn, ready, app on the Connection tab | Take the **Right** bud out of your ear and put it **in the case** (lid open); wait 20 s; then the **Left** | Right "— charging in the case (HH:MM:SS)", then both; Case "NN% (updated …)"; the session may close and re-open by itself | stream packets like `CAP-062` 3760/4845; Buds `DISC` 0x02 → app `SABM` ≈ 1.5 s later | | |
| AY-6 | both in the case, lid **open** | Wait. Say when Android's Bluetooth panel / the app shows the Buds disconnected and reconnected | a re-open, then (ACL drop) "Android no longer shows the Buds connected …" or "The Buds closed the app's channel (…)"; when Android reconnects on its own: ready again by itself, both "charging in the case", Case current | Buds `DISC` 0x02; ACL `0x13`; Android Create Connection; **one** app `SABM` 0x02 per event; no 2nd loss-triggered re-open within 10 s | | |
| AY-14 | both docked, lid open, ready | Do **nothing for 2 minutes** (watch the Case line time). Then tap **Refresh battery** | The Case time does not change by itself while nothing changes; after Refresh: the Case updates (if the Buds answer the re-subscription) or stays with its old time — both are valid; say which | **[0052]** app `REQUEST SubscribeRuntimeInfo` → a Buds `SERVER_STREAM` within 1 s, or none (ADR-043 Update, 🔴 untested) | | |
| AY-1 | both docked, lid open, ready | Find tab: **Ring Left**; listen 10 s; **Stop** | Does a **docked** bud ring? Say it; the notice follows the ACK | `04 01 00 01 02` → ACK `ff 01 00 03 04 01 00` or NAK `ff 02 …`; then `04 01 00 01 00` | | |
| AY-2 | same | Sound tab: tap **BALANCED** | accepted or an error shown (say which) | `WriteSetting` → `RESPONSE` OK or `status ≠ OK` | | |
| AY-2b | same | **[0052]** Sound tab: **Mono audio** on, then off | accepted or error | `4:{19:1}`, `4:{19:0}` | | |
| C8 | same | **Close the lid**; wait 20 s | "Paired — not connected to this phone"; the loss text never says "likely another app" | ACL disconnect; no app `SABM` while the link is down | | |

### H. Taking buds out, re-open rules

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| C9 | lid closed, app on screen | Open the lid, take **both** buds out (lay them on the table); do **not** tap Connect | ready again **by itself** once Android shows the Buds connected; Case "NN% — last seen HH:MM:SS (no bud charging in the case)" (E4) | one app `SABM` 0x02 per event; a stream packet without entry 6.1 | | |
| AY-5 | both buds **worn** (put them in first), ready, app on screen | Take **one** bud out of your ear | "The Buds closed the app's channel (…)", then ready again by itself | Buds `DISC` 0x02 → app `SABM` 0x02 ≈ 1.5 s later (one) | | |
| AY-10 | ready | Press **Home**; take a bud out (the Buds close the session); wait **30 s**; return to the app | nothing while in the background; on return: re-opens **once** | **no** app `SABM` while not visible; one after resume | | |
| J4 | ready | Press Home, wait **2 minutes**, reopen | still connected, or a clear message and a re-open; no crash | — | | |
| AY-8 | ready | Quick Settings → Bluetooth → tap the Buds' row to **disconnect in Android** | "Android no longer shows the Buds connected …"; **no** automatic re-open until Android reconnects | phone `DISC` HFP → ACL `0x13`; no app `SABM` while the link is down | | |
| AY-8b | after AY-8 | Tap the Buds' row again (Android reconnects); keep the app on screen | ready again by itself (link back) | Android Create Connection → one app `SABM` 0x02 | | |

### I. Find My Buds (buds on the table, out of the case)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| AY-11 | ready | **Ring Left**; **Disconnect**; wait 10 s; **Connect**; **Stop** | After Disconnect: "A ring was started on the Left earbud — reconnect and tap Stop to end it."; after Connect: "… before the app reconnected — it may still be ringing. Tap Stop to end it."; after Stop: gone, the ring stops (say when) | `04 01 00 01 02` → ACK; later `04 01 00 01 00` → ACK | | |

### J. Robustness and the remaining APP_TESTPLAN steps

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| K4 | ready | Rotate the phone; switch dark mode on and off (Quick Settings) | nothing lost, no crash | — | | |
| K1 | ready | Quick Settings: **Bluetooth off** | "Bluetooth is disabled." + Enable Bluetooth; no crash | — | | |
| K2 | after K1 | Bluetooth on; **Connect** if it does not connect by itself | ready again | app `SABM` 0x02 | | |
| K3 | ready | Walk with the phone to another room (out of range) for 30 s, come back | a clear "lost" message, then ready again (by itself or after Connect) | ACL loss; re-open | | |
| L2 | — | Debug tab: **Export debug log** → save to Files (this is X1's first export) | the log opens; connection lines with times; hex lines; no full Buds address | — | | |
| L3 | — | **Debug mode off**, export again (save as a second file) | no hex lines | — | | |
| K5 | — | *(optional, last)* Set the Bluetooth auto-off timeout to the shortest value (P11), lock the phone and wait for it; unlock, open the app; afterwards restore the timeout | "Bluetooth is disabled." (normal), no crash | — | | |

### K. Optional, destructive steps (only at the very end, only if you want to re-pair afterwards)

| Step | Action | Expected on screen | Time | Result / notes |
|---|---|---|---|---|
| A5 | Settings → Apps → OpenControl → Permissions → *Nearby devices*: **Don't allow**; open the app | "Bluetooth permission needed" / "You denied the permission." + **Allow** (after "don't ask again": **Open app settings**); allow again afterwards | | |
| (E) / B1 | Forget the Buds in Android's Bluetooth settings; open the app | "No Pixel Buds Pro 2 paired yet." + **Pair a device** | | |
| B4 | Open the lid; tap **Pair a device twice quickly** | no second picker, no crash; then pair normally (B2/B3) | | |

### Z. Restore

| Step | Action | Time | Done |
|---|---|---|---|
| Z1 | **[0052]** Set every setting back to the original value of §2 (balance, mono, conversation detection, touch controls, both hold actions, EQ) | | ☐ |
| Z2 | Stop the film after filming the status bar across a **minute change** again | | ☐ |

---

### A.5. What you must not do

- Do not use the official Pixel Buds app, or the Pixel 7a's Bluetooth, during this session.
- Do not tap Connect in steps that say "do not tap" (AY-0, AY-7, AY-6, C9, AY-5, AY-8b): those test the automatic re-open.
- Do not do two actions in one step; do not skip the 5–10 s pauses.
- Do not open the notification shade unless a step asks (privacy; DND is on).
- Do not change field 12 (the ANC-mode checklist) anywhere — it is not in this session (Group AR, `CAP-056`, Pixel 7a).

---

### A.6. After the run — within 1 minute of the last action

| # | Collect | Done |
|---|---|---|
| X1 | The two debug exports (L2 with Debug mode on, L3 without) — if L2/L3 were done earlier, export once more now with Debug mode on → `CAP-063-debug-export.log` | ☐ |
| X2 | `adb bugreport cap063` on the laptop; unzip; take the raw snoop log from `FS/data/log/bt/btsnoop_hci.log` or `FS/data/misc/bluetooth/logs/btsnoop_hci.log` (else `btsnooz.py`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3) → `CAP-063-btsnoop_hci.log`; write which path was used in §1 | ☐ |
| X3 | App logcat and the system log (GrapheneOS log viewer) → `CAP-063-OpenControl-for-Pixel-Buds-log-<id>.txt`, `CAP-063-System-log-<id>.txt` | ☐ |
| X4 | Film → `CAP-063-recording.mp4`; write its first and last overlay time in §1 | ☐ |
| X5 | Put everything with this file and your events file in this folder; rename the folder (see the top); `sha256sum *` into the analysis notes | ☐ |
| X6 | *(optional)* Switch the HCI snoop log off again; Pixel 7a Bluetooth may be switched on again | ☐ |

---

### A.7. Analysis checklist (for the analysis session)

- [ ] Integrity pre-flight (`capinfos`, `cap_len≠len`, out-of-order, `sha256sum`) as in `CAP-062-EVENT-NOTES.md`.
- [ ] Pre-filter by the Buds' classic ACL handle (and LE handle) — `bluetooth.addr` is empty with the `H4 with linux header` encapsulation
      (`AGENTS.md` §13.1; `CAP-062` used `bthci_acl.chandle==0x000b`). Map the handle to `04:00:6e:cf:6e:07` via HCI Connection Complete.
- [ ] Film ↔ phone clock offset from the two filmed minute changes; every "Time" above in phone time.
- [ ] DLCI 0x02: `python3 scripts/pwrpc_decode.py CAP-063-btsnoop_hci.log` — every `ReadSetting`/`WriteSetting`/`SubscribeRuntimeInfo` with its answer;
      compare the app's writes byte for byte with the `SettingsFixtures.kt` frames (channel-dependent header).
- [ ] DLCI 0x04: per-open reassembly (`opens.py` of `ai-sessions/0051` §4): a `03 03` burst on every open; two opens for AY-13b.
- [ ] Refuted if (`0048` §9 + `0052`): any app `SABM` 0x02 while the app is not visible or after a Disconnect tap before a Connect tap; more than one
      app `SABM` 0x02 per event; an `08 12` while the last `Notify` read Settable `00`; a Case value shown without its time; a loss text saying
      "another app"; a settings value changed on screen without the Buds' `RESPONSE`; any `WriteSetting`/`ReadSetting` naming field 12; any app
      frame on DLCI 0x08; a second `SubscribeRuntimeInfo` per Refresh.
- [ ] Traceability (`AGENTS.md` §13.7): every step ID above has a timeline row or an explicit "not run"; `APP_TESTPLAN.md` A5, B4, C5, F5–F7, H5, J4,
      K1–K5, L3 and `0045` (E)/(F); `0048` §9 AY-0 … AY-12; `0051` §19 AY-13/AY-14.
- [ ] Registry Test-IDs to reference: `PAIR-003`, `CASE-004`–`CASE-006`, `BATT-004`, `ANC-001`–`ANC-004`, `FIND-001`/`FIND-002`, `EQS-001`, `EQP-*`,
      `CONV-001`, `TOUCH-001`, `HOLD-001`–`HOLD-004`, `AUDIO-001`–`AUDIO-003` (balance/mono — check the IDs in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`).
- [ ] Write `CAP-063-FINDINGS.md`; add the Capture Index row and Group AY to `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (maintainer's approval), update
      `id_registry.csv`'s `CAP-063` row from *planned* to *analyzed*.

### A.8. For the separate Pixel 7a capture (Group AR, `CAP-056`) — additions from `ai-sessions/0051` F-6

Run it on the **Pixel 7a with the official Pixel Buds app** (Pixel 9a Bluetooth **off**), following Group AR in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` and
its anti-repeat safeguard (the checklist screen visible on film before the first toggle), with these steps added:

1. On **"Customize left"**'s ANC-mode checklist: untick **only Adaptive**; wait ≥ 10 s.
2. Open **"Customize right"** on film: is Adaptive unticked there too? (one shared list, 🟡)
3. Re-tick Adaptive (≥ 10 s), then untick **only Transparency** (≥ 10 s), then re-tick it.
4. One `WriteSetting 4:{12:{…}}` per tap is expected; which bit clears for Adaptive vs Transparency settles the `qht` order (code: 3 = Transparency,
   4 = Adaptive; on-screen reading: the reverse) — `PROTOCOL.md` §4.5.3 2026-09-26 Update.


---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-063-2026-09-27_15-57-33_16-25-03-Group_AY/CAP-063-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-063-2026-09-27_15-57-33_16-25-03-Group_AY/CAP-063-EVENT-NOTES
