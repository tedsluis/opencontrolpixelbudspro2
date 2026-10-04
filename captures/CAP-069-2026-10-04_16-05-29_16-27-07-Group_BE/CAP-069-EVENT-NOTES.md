# Event Notes: Pixel Buds Pro 2 with the official app 1.0.990706425 on the Pixel 7a — Group BE, head gestures, Multipoint, assistant hold, a tap on the current ANC mode, EQ Default, wear states, Find (`CAP-069`)

**Status:** 🟢 **Film pass (every 2 s, transitions at 4 fps) + full HCI log analysis complete** (`ai-sessions/0071_CAPTURE_PROMPT_2026_10_04.md`). See
`CAP-069-FINDINGS.md` for the analysis. The procedure as planned (the skeleton written by `ai-sessions/0069`) is kept unchanged as Appendix A; the
timeline below is what the film and the log show, not the plan.

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-069` |
| Group(s) | BE |
| Date | 2026-10-04 |
| Firmware version | `release_5.203` — on film (More settings → Firmware update, Left/Right/Case, overlay 16:07:44–48) and in all four `GetSoftwareInfo` announcements (frames 907, 6336, 7089, 8139) |
| Test device | Pixel 7a. On film: Settings → System update "Android version 17", "Android security update: September 5, 2026" (overlay 16:05:30); the build number is not shown |
| Official app | Google Pixel Buds **1.0.990706425**, "Updated on Sep 30, 2026" — Play Store → app details, overlay 16:26:54–16:27:06 |
| Play services | present (the Message Stream claims start `03 08 00 02 01 25`, frames 577, 6416, 6836, 8231). The *Nearby devices* permission state is not on film |
| OpenControl | not used; no OpenControl screen on film |
| Other devices | LE handle `0x0003` (16:05:59–16:23:06, reason `0x08`; again from 16:23:13): a device with a Heart Rate Service (`0x180d`, frames 855, 8658) — not the Buds; LE handle `0x0004` (16:05:59–16:06:04): not identified, not the Buds (no `0xfe2c`/`0x180f`/`0x180d` in its ATT traffic). Find Hub lists Pixel 7a, Pixel 9a, a Jabra and a Pebblebee tag (overlay 16:24:02) |
| Video file | `CAP-069-recording.mp4` — 1,297.98 s, H.264 1280×720 (rotation −90), 38,863 frames, ≈ 29.94 fps; burned-in camera overlay 16:05:29 … 16:27:07. The audio track holds **no samples** (its `stbl` has no sample description and no `stsz`) |
| Log file | `CAP-069-btsnoop_hci.log` — 10,582 packets, `H4 with linux header`, 16:05:56.025 – 16:30:06.677; starts with `Sent Reset` (frame 1) = the Bluetooth switch-on on film. No `.log.last` (the part before the switch-off was not pulled) |
| Clock | **phone = overlay + 0.43 s (± 0.03)**, measured at both ends (status-bar minute flips at film 30.10–30.13 s and 1289.11–1289.14 s against the overlay second flips). All "phone" times below are overlay + 0.43 s |
| Buds | classic address as in every earlier capture (ADR-010); handles `0x0002` (16:05:58.215 – 16:19:43.815, reason `0x13`), `0x0001` (16:20:02.334 – 16:21:50.129, `0x13`), `0x0002` (16:22:14.177 – end of log) |
| Wear mapping | the head is **not** in view (one glimpse of hair at overlay 16:21:00). Left/Right is taken from the case slot (camera's left slot = Left bud, right slot = Right — matched to the app's charging icons at 16:19:58–16:20:06 and the charging bits, frames 2217/2306) and from the Buds' own battery/runtime messages, never from a head side |

## Capture-integrity pre-flight

- `capinfos`: 10,582 packets, strict time order; `sha256` `45d0aa9f…fda1` (log), `c4f46782…6004` (film) — identical before and after the folder move.
- The log covers the whole film (log 16:05:56.0 – 16:30:06.7; film 16:05:29.4 – 16:27:07.4 phone time); the film's first 27 s (overlay 16:05:29–56) precede the
  log — before Bluetooth was switched on, nothing of the Buds is lost.
- `bluetooth.addr` is empty in this format; every filter is scoped by handle (`bthci_acl.chandle`) and, for handle `0x0002`, by frame range (it is reused).
- RFCOMM server channel ↔ DLCI per ACL (control-frame inventory, `tshark -Y "btrfcomm && btrfcomm.frame_type!=0xef && btrfcomm.frame_type!=0xff"`):
  ACL 1 (phone opened the multiplexer, 398): MAESTRO DLCI 2, Message Stream 4, GSND CONTROL 8, GSND AUDIO 0x0a, HFP 0x0c (phone → Buds channel 6).
  ACL 2 (phone, 6797): MAESTRO 2, MS 4, GSND CONTROL 8, GSND AUDIO 0x0a, HFP on DLCI **0x09** (the Buds opened the phone's channel 4, 6953 — AT text, 6973).
  ACL 3 (both sent `SABM` 0, the Buds' won, 7967/7982): MAESTRO **3**, MS **5**, GSND CONTROL **9**, GSND AUDIO **0x0b**, HFP on DLCI **0x08** (Buds-opened, 7994).
  **DLCI 0x08 is HFP on ACL 3, not GSND CONTROL** — counts below use the channel, not the DLCI number.

## Video review method (incl. privacy)

1. Whole film at a fixed 2-s interval: 109 contact sheets (`ffmpeg -i CAP-069-recording.mp4 -vf "fps=0.5,scale=360:640,drawtext=…,tile=3x2"`), each frame read
   (screen, case, hands, overlay).
2. Every tap and transition narrowed at 4 fps on a phone crop (`ffmpeg -ss <t> -t <n> -i … -vf "fps=4,crop=620:880:100:400,…,tile=6xN"`), the overlay second
   read in each frame; the clock strips at 30 fps.
3. Privacy (every frame checked): readable on film — the Wi-Fi SSID and a Wallet card's last four digits in Quick Settings (16:05:34–16:06:04), "20° in Utrecht",
   the first name in device names, launcher and Play Store search history (16:23:52–58, 16:26:28–30), and the Find Hub map with the Buds' last-seen location
   and date (16:24:02–16:25:30). No face, no messages, no e-mail address; the overlay carries date and time only. The maintainer chose **"Commit as is"** (chat
   2026-10-04, `AskUserQuestion` "Privacy").

## Event Timeline

Phone time = overlay + 0.43 s. Frames: `CAP-069-btsnoop_hci.log`. "MS" = Message Stream, "GC" = GSND CONTROL. Step IDs from Appendix A.

| Phone time | Action (film) | Actor | Step | Test-ID | Evidence (film overlay; frames) |
|---|---|---|---|---|---|
| 16:05:30 | Settings → System update: Android 17, security update 5 Sep 2026 | maintainer | P6 (part) | — | film t 0–2 s |
| 16:05:34–:42 | notification shade, Quick Settings, Connected devices: "Pixel Buds Pro 2 … Active, L 100 R 100" | maintainer | not in plan | — | t 4–12 s |
| 16:05:51 | Quick Settings: Do Not Disturb **On** | — | P5 | — | narrow `bt_toggle` |
| 16:05:53 | Bluetooth switched **off** (dialog "Bluetooth is off") | maintainer | P2 | `PAIR-003` | overlay 16:05:52.5–53 |
| 16:05:56.0 | Bluetooth switched **on** | maintainer | P2 | — | finger 16:05:56; log starts frame 1 `Sent Reset` 16:05:56.025 |
| 16:05:58.2 | Buds "Connecting…" → Active; phone-initiated ACL | Android | — | `PAIR-003` | `Create Connection` 178, `Connect Complete` 291 (`0x0002`); MS claim 577, `08 11` 604 → `Notify 01 e8 00 20` 617 (Off, Settable 00) |
| 16:05:59.7 | official app connect burst on MAESTRO (ch 21 and 19) | official app | — | `BATT-004` | `GetSoftwareInfo` 907 (ch 21, `release_5.203` × 3); reads 913…1348; `SubscribeRuntimeInfo` 933 (Case 68 %, both buds charging) |
| 16:06:08–16:06:20 | app Device details (L 100 C 68 R 100, ANC row greyed); Digital assistant row → Gemini intro → back; Controls and gestures: touch ON, Left/Right "Active noise control", head gestures **ON** | maintainer | P7 | — | t 38–50 s; read `29:2` 1226, `7:{1:5,2:5}` 1047 |
| 16:06:22–:36 | Touch controls; Customize left (NC ✓ Off ☐ Adaptive ✓ Transparency ✓) | maintainer | not in plan | — | t 52–64 s; read `12:{1:1 2:0 3:1 4:1}` 1153 |
| 16:06:40.4 | Customize **right**: tick **Off** | maintainer | not in plan | `HOLD-005` | finger 16:06:40; `WriteSetting 4:{12:{1:1 2:1 3:1 4:1}}` 1813 (16:06:40.931) → OK 1816, stream 1815 |
| 16:06:44 | Customize **left** shows Off ticked (no write) | — | not in plan | `HOLD-005` | t 74 s (one shared list, see FINDINGS §6) |
| 16:06:52 | Use head gestures screen opened, toggle ON, no tap | maintainer | not in plan | — | t 82–84 s |
| 16:06:58.6 | Device details → Sound | maintainer | — | — | new service `0xbf6c9399` subscribed 1894/1896 (ch 19 + 21), stream until `CLIENT_ERROR CANCELLED` 1943/1944 (16:07:09.5) |
| 16:07:02 | Sound: Conversation detection ON, Balance centre, Mono OFF | — | P7 | — | t 92–94 s |
| 16:07:08–:15 | Equalizer: preset **Balanced**, Volume EQ ON; preset menu | maintainer | P7 | — | read `16:{-3.5,0.5,1.0,-1.0,2.5}` 1100 |
| 16:07:17.5 | tap **Default** | maintainer | BE-17 (**done early**, from Balanced) | `EQP-001` | finger 16:07:17; `WriteSetting 4:{16:{0.0×5}}` 1990 (16:07:18.544) → OK 1995 |
| 16:07:24–16:08:04 | Sound → Device details → More settings (In-ear ON, Multipoint ON) → Multipoint (ON, no tap) → Firmware update (`release_5.203` ×3) → Device details (no "Find device" entry) | maintainer | P6, P7 | — | t 114–154 s |
| 16:08:10–:28 | Controls → Use head gestures, toggle ON (hold) | maintainer | BE-1 | `HEAD-001` | t 160–178 s |
| 16:08:32.6 | **Right** bud out of the case (camera's right slot) | maintainer | not in plan (pre-state of section I: both buds worn) | `CASE-005` | MS battery `e4 64 ff` 2217 (16:08:33.08); runtime 6.3 → not charging 2236; GC `04 05 … 05` 2255 (16:08:34.91); Notify `e8 e8 80` 2256 |
| 16:08:42–:46 | **Left** bud out of the case; both buds out of view (ears not on film) | maintainer | not in plan | `CASE-004`, `INEAR-002`/`003` (not filmed) | MS `64 64 ff` 2306 (16:08:44.00); GC `04 05 … 05` 2351 (16:08:46.37), `04 05 … 06` 2363 (16:08:48.48); Notify `e8 e8 40` 2366 |
| 16:09:18.4 | head gestures **OFF** | maintainer | BE-3 | `HEAD-001` | finger 16:09:17, OFF 16:09:18; `4:{29:1}` 2492 (16:09:18.409) → OK 2506; stream 2502; GC `04 16 … 02` 2503 |
| 16:09:28.9 | head gestures **ON** + "Optimize head gestures" dialog, Close | maintainer | BE-2 | `HEAD-001` | finger 16:09:28; `4:{29:2}` 2564 (16:09:29.409) → OK 2568; GC `04 16 … 01` 2567 |
| 16:10:17.2 | **OFF** | maintainer | BE-5 (as BE-3) | `HEAD-001` | `4:{29:1}` 2737 (16:10:17.740) → OK 2744; GC `… 02` 2741 |
| 16:10:41.9 | **ON** + dialog | maintainer | BE-4 (as BE-2) | `HEAD-001` | `4:{29:2}` 2831 (16:10:42.847) → OK 2837; GC `… 01` 2836 |
| 16:11:01.2 | **OFF** | maintainer | repeated (3rd OFF) | `HEAD-001` | `4:{29:1}` 2923 (16:11:01.921) → OK 2934; GC `… 02` 2933 |
| 16:11:17.9 | **ON** + dialog (ends as P7: ON) | maintainer | repeated (3rd ON) | `HEAD-001` | `4:{29:2}` 3025 (16:11:18.982) → OK 3033; GC `… 01` 3032 |
| 16:11:38 | Device details: ANC row active, **Adaptive** | — | — | — | t 368 s |
| 16:12:05.9 | Multipoint **OFF** | maintainer | BE-6 | `MULTI-001` | finger 16:12:05, OFF 16:12:06; `4:{11:0}` 3161 (16:12:06.338) → OK 3170; MS `07 11 … 98 00` 3166 |
| 16:12:14.4 | Multipoint **ON** | maintainer | BE-7 | `MULTI-001` | `4:{11:1}` 3212 (16:12:14.919) → OK 3219; `07 11 … b8 00` 3215 |
| 16:12:20.9 | **OFF** | maintainer | BE-8 (1) | `MULTI-001` | `4:{11:0}` 3245 → OK 3254; `… 98 00` 3248 |
| 16:12:27.0 | **ON** (ends as P7: ON) | maintainer | BE-8 (2) | `MULTI-001` | `4:{11:1}` 3275 → OK 3285; `… b8 00` 3278 |
| 16:13:15.9 | Customize right → **Digital assistant** tab; dialog "Allow your digital assistant to read your notifications…" → Skip (16:13:22) | maintainer | BE-9 | `HOLD-004` | `4:{7:{2:{4:{1:6}}}}` 3370 (16:13:16.605) → OK 3374; stream `7:{1:5,2:6}` 3372; GC `04 14 … 03` 3373 |
| 16:13:32.97 | (not on film: bud and head out of view) press-and-hold #1 | maintainer (inferred) | BE-10 | `HOLD-004` | GC `01 09 00 03 0a 01 03` 3403, first GSND AUDIO frame 3404 (+2.3 ms); film: mic pill 16:13:34.4, Gemini overlay "Vraag het Gemini" 16:13:34.9 |
| 16:13:34.5–16:13:42.3 | Gemini overlay; audio stream | Buds / phone | BE-10 | — | GC `… 05` 3417, `… 03` 3423, `… 01` 3456, `… 21` 3463; phone `08 11 00 00` 3470; 258 GSND AUDIO frames 3404–3995; phone `08 06 00 04 08 00 10 01` 3992 (16:13:42.20); AVRCP volume 48 → 32 % (16:13:34.62) → 48 % (16:13:42.31) |
| 16:13:59.37 | (not on film) press-and-hold #2; mic active 16:14:00.4 | maintainer (inferred) | BE-11 | `HOLD-004` | GC `… 03` 4084, audio 4085 (+1.7 ms); 221 frames until 4579 (16:14:07.70); `08 06` 4576 |
| 16:14:07.7–16:14:51.2 | nothing touched (Fit heads-up 16:14:46–50) | — | BE-12 (43.5 s) | `HOLD-003` | no `01 09` and no GSND AUDIO payload between 4579 and 4806 (FINDINGS §3) |
| 16:14:51.21 | (not on film) press-and-hold #3; mic active 16:14:54.4 | maintainer (inferred) | **not in plan** (extra hold) | `HOLD-004` | GC `… 03` 4806 → audio 4807–4823 (7 frames), `… 05` 4822, `… 03` 4827 (16:14:53.65) → audio 4829–5406 (256 frames), `08 06` 5401 (16:15:02.15) |
| 16:15:09.4 | Customize right → **Active noise control** tab | maintainer | BE-13 | `HOLD-004` | `4:{7:{2:{4:{1:5}}}}` 5482 (16:15:10.161) → OK 5486; GC `04 14 … 01` 5485 |
| 16:15:26.9 | Device details: **Adaptive → Noise cancellation** | maintainer | BE-14 | `ANC-002` | finger 16:15:26; `08 12 … 08` 5519 (16:15:27.85) → ACK 5523 → Notify `e8 e8 08` 5524; stream `13:2` 5528 |
| 16:16:10.9 | **NC → Adaptive** | maintainer | BE-16 (pre-step) | `ANC-003` | `08 12 … 40` 5600 (16:16:12.01) → ACK 5606 → Notify 5607; `13:4` 5611 |
| 16:16:16.4 | tap on **Adaptive** (already selected) | maintainer | BE-15 | `ANC-006` | finger on Adaptive 16:16:16 (2 frames); **no `08 12`** before 5644 |
| 16:16:33 | **Adaptive → Transparency** | maintainer | not in plan (repeat of the pattern) | `ANC-004` | `08 12 … 80` 5644 (16:16:34.48) → ACK 5651 → Notify 5652; `13:3` 5656 |
| 16:16:42.4 | tap on **Transparency** (selected) | maintainer | BE-16 | `ANC-006` | finger 16:16:42 (4 frames); no `08 12` before 5692 |
| 16:16:50.4 | **Transparency → Off** | maintainer | not in plan | `ANC-001` | `08 12 … 20` 5692 (16:16:51.88) → ACK 5696 → Notify 5697; `13:1` 5701 |
| 16:16:58.4 | tap on **Off** (selected) | maintainer | repeated (3rd current-mode tap) | `ANC-006` | finger 16:16:58 (2 frames); no `08 12` for the rest of the log |
| 16:17:10–:26 | Sound → Equalizer (Default) | maintainer | — | — | t 700–716 s |
| 16:17:29.4 | preset **Heavy bass** | maintainer | BE-17 pre-step | `EQP-001` | `4:{16:{5.0,3.0,0,0,0}}` 5783 (16:17:30.51) |
| 16:17:35.9 | preset **Default** | maintainer | BE-17 | `EQP-001` | `4:{16:{0.0×5}}` 5803 (16:17:36.77) |
| 16:18:34.9 | **Heavy bass** | maintainer | repeated | `EQP-001` | 5942 (16:18:35.47) |
| 16:18:43.4 | **Default** | maintainer | repeated | `EQP-001` | 5965 (16:18:44.02) |
| 16:18:52.9–:58.4 | five sliders dragged (Upper treble, Treble, Mid, Bass, Low bass), "Save" enabled | maintainer | **not in plan** | `EQS-004` | 6006, 6018, 6026, 6033, 6039 (`4:{16:…}`, one band each) |
| 16:18:59.4 | **Save**; preset label "Last saved", toast "EQ saved" | maintainer | not in plan | `EQS-004` | `4:{18:{-4.9,4.3,-4.4,3.5,-4.2}}` 6050 (16:19:00.58) → OK 6053 |
| 16:19:07.4 | **Default** | maintainer | repeated | `EQP-001` | 6066 (16:19:07.80) |
| 16:19:16 | **Last saved** (BE-18 planned "Balanced": **done differently**) | maintainer | BE-18 | `EQP-001` | `4:{16:{-4.9,4.3,-4.4,3.5,-4.2}}` 6120 (16:19:16.46) |
| 16:19:35–:36 | (not on film) a bud taken out of an ear | maintainer | not in plan | `INEAR-004` | GC `04 05 … 04` 6153 (16:19:36.354); Notify `e8 e8 80` 6156; **Buds `DISC` MAESTRO and MS** 6157/6158 (16:19:36.358); phone `DISC` GC/AUDIO 6174/6175 |
| 16:19:37.4–:38 | **Right** bud seated (camera's right slot, LED on) | maintainer | BE-26 (out of order) | — | film 16:19:37–38; phone re-opens GC 6204, AUDIO 6232, MAESTRO 6314 (16:19:39.99), MS 6407; runtime 6352: Right charging, Case 67 % |
| 16:19:40.35 | — | Buds | — | — | GC `01 09 00 04 0a 02 0b 06` 6323, `04 05 … 03` 6326, `04 16 … 02` 6329; announcement ch **19** 6336 |
| ≈ 16:19:42.4 | **Left** bud seated — both in the case, lid open | maintainer | BE-19 pre-state / BE-27 | — | MS (re-opened, Play services) `64 e4 ff` 6425, Notify `e8 00 20` 6434; **ACL drop** reason `0x13` 6468 (16:19:43.815); app shows "Connect" |
| 16:19:58–16:20:00 | **Left** bud out of the case, held in the hand | maintainer | BE-20 | `CASE-004` | Buds-initiated reconnect: `Connect Request` 6580 → `Connect Complete` 6584 (16:20:02.334, `0x0001`); GC `04 05 … 03` 6875; announcement ch 19 7089 |
| 16:20:20–:23 | (hand covers camera) Left bud into an ear | maintainer | BE-21 | `INEAR-002` | GC `04 05 … 04` 7309 (16:20:23.19); Notify `e8 e8 80` 7317 (app: Off → Transparency without a tap); `04 16 … 01` 7320; stream `13:3` 7327 |
| 16:20:34.4–:35 | **Right** bud out of the case, held | maintainer | BE-22 | `CASE-005` | film 16:20:34–35; runtime 7358 (16:20:35.99) still Right **charging**; GC `04 05 … 04` 7385 (16:20:37.81) |
| 16:20:43–:46 | (hand covers camera) Right bud into an ear | maintainer | BE-23 | `INEAR-003` | MS `64 64 ff` 7404 (16:20:46.06: Right no longer charging); GC `04 05 … 06` 7415 (16:20:46.32); Notify `e8 e8 20` 7416 (app: Transparency → Off); `13:1` 7419 |
| 16:21:00–:06 | Right bud out of the ear (hair in view), laid on the table (camera's right of the case) | maintainer | BE-24 | `INEAR-004` | GC `04 05 … 04` 7453 (16:21:02.28), `01 09 00 04 0a 02 0b 06` 7455; Notify `e8 e8 80` 7458; `13:3` 7459 |
| 16:21:20–:24 | Left bud out of the ear, laid on the table | maintainer | BE-25 | `INEAR-004` | GC `01 09 … 0b 06` 7514, `04 05 … 03` 7517 (16:21:22.93), `04 16 … 02` 7521; Notify **`e8 00 20`** 7520; `13:0` 7530 |
| 16:21:36–:38 | Right bud into the case | maintainer | BE-26 | — | MS `64 e4 ff` 7590 (16:21:38.51); runtime 7594 (Right charging, Case 67); GC `04 05 … 03` 7615 |
| 16:21:48–:50 | Left bud into the case — both in, lid open | maintainer | BE-27 | — | ACL drop `0x13` 7658 (16:21:50.129); app "Connect" at 16:21:50 |
| 16:22:12–:16 | both buds out of the case at once, laid on the table | maintainer | BE-28 pre-state | — | Buds-initiated reconnect 7759 → 7763 (16:22:14.177); multiplexer by the Buds; announcement ch 21 8139 (DLCI 3); MS (Play services) 8231; Notify `e8 00 20` 8260 |
| 16:22:30–16:23:48 | searches the app for Find: More settings (whole list), Controls, Hearing wellness, Audio switch, Case sounds — **no Find device** anywhere | maintainer | BE-28 **could not be run as written** | `FIND-001` | t 1020–1098 s |
| 16:23:50–16:24:00 | Home screen → app search "fi…" → **Find Hub** | maintainer | not in plan | — | t 1100–1110 s |
| 16:24:02–:28 | Find Hub: device list (two "Pixel Buds Pro 2" entries, "Nearby"); first entry "Device not seen for 7 days"; second: Case / Left / Right, "Last seen Sep 20 around 13:56/13:57", map | maintainer | not in plan | — | t 1112–1138 s |
| 16:24:30.4 | **Play sound — Left** → "Connecting…" + "If you have another device linked with your Google Account, it may try to play sound on Pixel Buds Pro 2." → **"Can't play sound"** (16:24:54) | maintainer / Find Hub | BE-28 (done differently) | `FIND-001` | **no** `04 01` on the MS in the whole log; only LE scan enables and advertising reports in the window (FINDINGS §7) |
| 16:25:06.4 | **Play sound — Right** → "Connecting…" → "Can't play sound" (16:25:30) | maintainer / Find Hub | not in plan | `FIND-001` | as above; MS only `03 03 00 03 64 64 ff` ×3 (9840–9847, 16:25:16.9) |
| — | ring stopped on the bud | — | BE-29 **skipped** (nothing rang) | `FIND-005` | not observable |
| 16:25:32–16:26:20 | back to the Pixel Buds app; More settings, Device details | maintainer | not in plan | — | t 1202–1250 s |
| 16:26:22–16:27:07 | Play Store → Google Pixel Buds → details: **Version 1.0.990706425**, Updated Sep 30 2026 | maintainer | P6 (app version, at the end) | — | t 1252–1296 s |

## Step mapping (skeleton → what happened)

| Step | Result |
|---|---|
| P1 Pixel 9a Bluetooth off | **not identifiable** (the Pixel 9a is not on film; the log shows one classic device only) |
| P2 Bluetooth off/on on film | **done** (16:05:53 off, 16:05:56 on; log frame 1) |
| P3 phone, case and head on film | **done differently** — the head is never in view; ears not filmed |
| P4 minute change at start and end | **done** (16:05→16:06, 16:26→16:27) |
| P5 Do Not Disturb | **done** (Quick Settings tile "On", 16:05:51) |
| P6 About phone + firmware row | **done differently** — System update (Android 17) instead of About phone (no build number); firmware row 16:07:44–48; app version at the end (16:26:54–16:27:06) |
| P7 starting values | **done** — head gestures ON, Multipoint ON, Left/Right ANC, EQ Balanced, ANC Off (row greyed, buds in case) |
| BE-1 | **done** (16:08:10–16:09:16; the buds were taken out of the case during the hold, not before it) |
| BE-2 / BE-3 / BE-4 / BE-5 | **done** in the order BE-3, BE-2, BE-5, BE-4 (the toggle started ON, as the skeleton foresaw), plus **one more OFF/ON pair (repeated)** |
| BE-6 / BE-7 / BE-8 | **done** (OFF, ON, OFF, ON) |
| BE-9 | **done** |
| BE-10 / BE-11 | **done differently** — the holds are not on film (bud out of view); identified on the wire and by the assistant overlay |
| BE-12 | **done** (43.5 s, no touch on film), then **an extra (third) hold** (not in plan) |
| BE-13 | **done** |
| BE-14 | **done** (Adaptive → Noise cancellation) |
| BE-15 | **done** (tap on Adaptive after NC → Adaptive) |
| BE-16 | **done** (Transparency), and **repeated** (Off) |
| BE-17 | **done early** (16:07:17, from Balanced) and **repeated** four more times (16:17:36, 16:18:43, 16:19:07; from Heavy bass, Heavy bass, Last saved) |
| BE-18 | **done differently** — "Last saved" (a new custom curve saved at 16:18:59) instead of Balanced; the run ends with the EQ **not** restored |
| BE-19 | **done differently** — both buds docked at 16:19:42 dropped the link (expected, ADR-016); the "connected, both in the case" state did not exist |
| BE-20 … BE-27 | **done** in order (BE-26/27 also once out of order at 16:19:37–42, before BE-19) |
| BE-28 | **could not be run as written** — the app has no Find device; done instead in Find Hub (Left, then Right): "Can't play sound" both times |
| BE-29 | **skipped** (nothing rang) |
| Not in plan | field-12 "Off" ticked (16:06:40, not restored); Gemini intro screen; Sound/Hearing wellness/Audio switch/Case sounds screens; five EQ slider drags + Save; launcher/Find Hub/Play Store |

## Analysis checklist

- [x] Every filter scoped by handle (and frame range for the reused `0x0002`); DLCIs identified by content.
- [x] Clock measured at both ends.
- [x] I: six `4:{29:n}` with the tap on film and the `04 16` value after each (FINDINGS §1). Proposal to the maintainer only.
- [x] II: four `4:{11:n}` and the `07 11` flags byte after each; the SASS page sentence quoted (FINDINGS §2).
- [x] III: per hold the first GC and GSND AUDIO frame and the gap; BE-12 negative with command, exit status and positive control (FINDINGS §3).
- [x] IV: `08 12` count in the current-mode windows against the positive controls (FINDINGS §4).
- [x] V: the five floats of every Default write (FINDINGS §5).
- [x] VI: the wear table (FINDINGS §8).
- [x] VII: `04 01` / `ff 01` by direction in the whole log (FINDINGS §7).
- [x] Traceability: `HEAD-001`, `MULTI-001`, `HOLD-003`, `HOLD-004`, `ANC-001`…`ANC-004`, `ANC-006`, `EQP-001`, `INEAR-002`…`INEAR-004`, `CASE-004`, `CASE-005`,
      `FIND-001`, `FIND-005`, `PAIR-003`, `BATT-004` — each in the timeline; `FIND-005` not observable (nothing rang).

## Appendix A — the procedure as planned (the skeleton as committed in `0378076`, file `CAP-069-EVENT-NOTES.md` in the placeholder folder `captures/CAP-069-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BE/`; text unchanged, heading levels lowered by two)

### Event Notes: Pixel Buds Pro 2 with the official app on the Pixel 7a — Group BE, head gestures, Multipoint, assistant hold, EQ Default, wear states (`CAP-069`)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0069`, 2026-10-03; scope is the maintainer's choice in chat 2026-10-03,
`AskUserQuestion` "Leads": *"CAP-068 = release build; CAP-069 = official app (Recommended)"*, and "Features": *"Yes to both, after CAP-069
(Recommended)"*). After the run: rename this folder from the placeholder `CAP-069-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BE` to the film's first/last
overlay times and analyse it as `CAP-067` was (`ai-sessions/0067`).

**Purpose.** Everything here needs the **official** Pixel Buds app, so it is kept apart from `CAP-068` (the OpenControl release build). Each item
is a lead of `ai-sessions/0068` that the existing logs and films could not settle (`DESKRESEARCH_FINDINGS.md`, entry of 2026-10-03):

- **I — Head gestures off/on (lead L68-4, `PROTOCOL.md` §4.5.4, 🟡 `qhr` field 29: 1 = off, 2 = on).** One write in each direction exists
  (`CAP-020` frames 1935 and 2038); the OFF tap was not identified on film. Needed for promotion: both directions, twice, each tap on film.
- **II — Multipoint off/on (lead L68-3, §4.5.2, 🟡 field 11: 0 = off, 1 = on; SASS flag bit 🟡).** One OFF write exists (`CAP-019` 2482).
- **III — One assistant press-and-hold (lead L68-1, 🟡).** `CAP-021`'s four DLCI 0x0a waves start with a Buds message `01 09 00 03 0a 01 03` on
  DLCI 0x08; the bud was out of frame, so the trigger is not on film.
- **IV — A tap on the ANC mode that is already selected (lead L68-7, 🟡: the official app sends no `Set`).**
- **V — The official app's EQ preset "Default" (`A68-APP-16`).** OpenControl 1.0.1 has a "Flat" preset (0.0 × 5); what the official app writes
  for its own default is not captured.
- **VI — Wear and placement states against DLCI 0x08 Code `0x05` (lead L68-5, 🔴 the meaning of values 1, 3, 4, 5, 6).**
- **VII — Ring stopped on the bud, with Play services present (lead L68-2, 🟡 the status-sync reading of §4.4).** The run without Play services
  is `CAP-068`.

**Not in this run:** anything OpenControl does (→ `CAP-068`); the battery advertisement on case-open (→ `CAP-054`, Group AP, redesigned).

#### Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-069` |
| Group(s) | BE (new) |
| Date | TBD |
| Firmware version | TBD — read it from the official app's "Firmware" row on film |
| Test device | Pixel 7a, the official Pixel Buds app, Google Play services enabled. Android version: read it on film from Settings → About phone (the version of this phone is unreconciled in `CAP-036`…`041`, `A68-CAP-23`) |
| OpenControl | Not used. Pixel 9a: Bluetooth **off** for the whole run |
| Video file | TBD — camera film showing the phone, the case and **your head** (both ears for sections III, VI, VII) |
| Log file | TBD — `CAP-069-btsnoop_hci.log` (and `.log.last` if Bluetooth was toggled) |
| Buds address | not written down; refer to the Buds by connection handle (`AGENTS.md` §7/§9) |

#### Preparation

| # | Check | Done |
|---|---|---|
| P1 | Pixel 9a Bluetooth off (one phone only — Multipoint must not find a second source during section I, III–VII) | ☐ |
| P2 | Bluetooth HCI snoop log on (developer options); switch Bluetooth off and on **on film** so the log starts clean | ☐ |
| P3 | Camera films the phone, the case and your head; head on the right of the frame = Left bud (as `CAP-064`). No narration | ☐ |
| P4 | Status bar on film across a minute change at the start and at the end (the clock offset is measured, not assumed) | ☐ |
| P5 | Do Not Disturb on | ☐ |
| P6 | Settings → About phone on film (Android version, build); official app → Device details → the firmware row on film | ☐ |
| P7 | Note the starting values on film: Head gestures (on/off), Multipoint (on/off), press-and-hold Left/Right, the EQ preset, the ANC mode | ☐ |

**Rhythm:** one action, then wait **10 s** with hands off (longer where a step says so). Something unexpected: stop, wait 10 s, continue.
Before committing: the camera film may carry a street-address overlay — check and mask.

#### Steps

DLCI numbers are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05, GSND CONTROL 0x08 or 0x09, GSND AUDIO 0x0a or 0x0b); the
expected bytes below are written for the even numbers.

##### I. Head gestures (`HEAD-001`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BE-1 | both buds worn, Controls → Head gestures screen open | hold 5 s on the current state | the toggle as noted in P7 | the connect-time `ReadSetting` of field 29 (1 or 2) | — |
| BE-2 | toggle **off** | tap the toggle **on** | on | `WriteSetting 4:{29:2}` → OK; settings stream `4:{29:2}`; DLCI 0x08 `04 16 00 02 08 01` | the write carries another value, or no write follows the tap |
| BE-3 | on | tap **off** | off | `WriteSetting 4:{29:1}` → OK; DLCI 0x08 `04 16 00 02 08 02` | as BE-2 |
| BE-4 | off | repeat BE-2 | on | as BE-2 | a second sample disagrees with the first |
| BE-5 | on | repeat BE-3; then leave the toggle as P7 noted | off | as BE-3 | as BE-4 |

If the toggle was **on** at the start, run BE-3 first, then BE-2, BE-5, BE-4.

##### II. Multipoint (`MULTI-001`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BE-6 | Multipoint screen open | tap the toggle to the **other** state | changed | `WriteSetting 4:{11:1}` (on) or `4:{11:0}` (off) → OK; within 1 s a Message Stream `07 11 00 04 01 02 xx 00` | no write, or another field |
| BE-7 | — | tap it back | changed back | the other value; `07 11 …` with one bit of `xx` changed (🟡 `b8` on / `98` off) | the `07 11` byte does not change with the toggle |
| BE-8 | — | repeat BE-6 and BE-7 once | — | as above | the second pair disagrees |

##### III. Assistant press-and-hold (`HOLD-004`, `HOLD-003`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BE-9 | both worn | Controls: set **Right** press-and-hold to **Digital assistant** | "Digital assistant" | `WriteSetting` as `CAP-021` frame 3619 | — |
| BE-10 | Right ear in view | press and hold the **Right** bud for about 3 s, say nothing, release; wait 15 s | the assistant's overlay, if any | DLCI 0x08 Buds `01 09 00 03 0a 01 03`, then DLCI 0x0a payload frames, a phone `08 11 00 00` on DLCI 0x08, and about 8 s later a phone `08 06 00 04 08 00 10 01` | the hold on film is not followed within 1 s by `01 09 00 03 0a 01 03`, or the wave appears without a hold |
| BE-11 | — | repeat BE-10 once | — | as BE-10 | — |
| BE-12 | — | wait 30 s without touching anything | — | **no** `01 09 00 03 0a 01 xx` and no DLCI 0x0a payload | a wave appears with nothing done |
| BE-13 | — | set Right press-and-hold back to **Active noise control** | — | `WriteSetting` as `CAP-021` frame 4976 | — |

##### IV. A tap on the current ANC mode (`ANC-006`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BE-14 | both worn, Device details, a mode highlighted | tap **another** mode (positive control) | that mode highlighted | `08 12 00 14 …` → ACK → Notify | — |
| BE-15 | — | tap the **highlighted** mode once; wait 10 s | unchanged | 🟡 **no** `08 12` (command, exit status and BE-14 as the positive control) | a `08 12` follows the tap — then note its answer (ACK or NAK and its reason) |
| BE-16 | — | repeat BE-15 with a second mode selected first | — | as BE-15 | — |

##### V. EQ "Default" (`EQP-001`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BE-17 | Sound → Equalizer, any non-default preset selected (tap one first; note its name) | tap **Default** | the sliders as the app draws them — hold 3 s on film | one `WriteSetting 4:{16:{…}}`: **record the five values** (prediction 🟡: 0.0 × 5) | the app writes nothing, or writes another field |
| BE-18 | — | select the P7 preset again | — | its write | — |

##### VI. Wear and placement states (`INEAR-002`, `INEAR-003`, `INEAR-004`, `CASE-004`, `CASE-005`)

Each step is one movement of one bud, with the bud and the ear (or the case slot) in view, then 10 s hands off.

| Step | Action | Expected on the wire | Refuted if |
|---|---|---|---|
| BE-19 | start: both buds in the case, lid open, connected | note the current `04 05 00 02 08 xx` (🟡 3) and the Settable byte of the last Notify | — |
| BE-20 | take the **Left** bud out and hold it in the hand (not in the ear) | a new `04 05` value or none — record | — |
| BE-21 | put the Left bud in the ear | record `04 05`; a Notify with Settable `e8` | — |
| BE-22 | take the **Right** bud out, hold it | record | — |
| BE-23 | put the Right bud in the ear | record (🟡 6 with both worn) | — |
| BE-24 | take the Right bud out of the ear, lay it on the table | record | — |
| BE-25 | take the Left bud out of the ear, lay it on the table | record; a Notify with Settable `00` | — |
| BE-26 | put the Right bud in the case | record; a battery update with the Right charging bit | — |
| BE-27 | put the Left bud in the case | record (🟡 3); the link may drop (ADR-016) | — |

The reading of Code `0x05` is refuted as a placement state if the same movement gives different values in BE-20…BE-27 and in a repeat, or if the
value changes with no movement on film.

##### VII. Ring stopped on the bud (`FIND-005`, `FIND-001`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BE-28 | both buds out of the ears, on the table, connected | Find device → ring **Left** | ringing | phone `04 01 00 01 02`; Buds ACK `ff 01 00 03 04 01 00`; Buds `04 01 00 01 02`; phone ACK `ff 01 00 02 04 01` | — |
| BE-29 | ringing | stop it **on the bud** (touch it / put it in the ear) — on film; wait 10 s | the app shows the ringing as stopped | 🟡 Buds `04 01 00 01 00` with **no** phone command before it, then the phone's ACK `ff 01 00 02 04 01` | the ringing stops and no Buds `04 01` follows, or a phone `04 01 00 01 00` precedes it |

#### Don'ts

- Do not open OpenControl; do not switch the Pixel 9a's Bluetooth on.
- Do not change two settings within 10 s of each other.
- Do not speak during BE-10/BE-11 (the DLCI 0x0a payload is audio; nothing of it is to be decoded or kept beyond frame counts).

#### After the run

The raw `btsnoop_hci.log` (both, if Bluetooth was toggled) via `adb bugreport`; the film. All into this folder, then `sha256sum *`. Restore the
settings noted in P7.

#### Analysis checklist

- [ ] Scope every filter to the Buds' ACL handle first; identify the DLCIs by content (`AGENTS.md` §13).
- [ ] The film's clock against the phone's: measured from P4, both ends.
- [ ] I: every `WriteSetting 4:{29:n}` with its time against the tap on film; the `04 16` value after each. **Proposal to the maintainer only** —
      promotion of field 29 needs their answer in chat (`AGENTS.md` §6).
- [ ] II: every `4:{11:n}` and the `07 11` flags byte after each; the bit against the Fast Pair SASS page's flag table (quote the sentence).
- [ ] III: per hold on film, the first DLCI 0x08 and DLCI 0x0a frame after it and the gap; frame counts only for the audio channel. BE-12 as the
      negative window, with the command, its exit status and BE-10 as the positive control.
- [ ] IV: `08 12` count in the BE-15/BE-16 windows against BE-14.
- [ ] V: the five floats of BE-17's write; compare with OpenControl's "Flat".
- [ ] VI: a table step → `04 05` value → Settable byte → battery charging bits; state which values repeat.
- [ ] VII: direction and order of every `04 01` and `ff 01` frame in BE-28/BE-29.
- [ ] Traceability (`AGENTS.md` §13 step 7): `HEAD-001` (BE-1…5), `MULTI-001` (BE-6…8), `HOLD-004`/`HOLD-003` (BE-9, BE-13), `ANC-006`
      (BE-15/16), `ANC-001`…`004` (BE-14, whichever mode), `EQP-001` (BE-17), `INEAR-002`/`003`/`004` and `CASE-004`/`005` (BE-19…27),
      `FIND-001`/`FIND-005` (BE-28/29), `PAIR-003` and `BATT-004` (every connect) — each referenced in the timeline or flagged.
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `id_registry.csv`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` rows, and write `CAP-069-FINDINGS.md`.


---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-069-2026-10-04_16-05-29_16-27-07-Group_BE/CAP-069-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-069-2026-10-04_16-05-29_16-27-07-Group_BE/CAP-069-EVENT-NOTES
