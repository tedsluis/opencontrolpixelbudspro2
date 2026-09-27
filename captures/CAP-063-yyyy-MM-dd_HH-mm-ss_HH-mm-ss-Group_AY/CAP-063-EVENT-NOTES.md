# Event Notes: OpenControl for Pixel Buds on Pixel 9a (GrapheneOS) — Group AY, hardware re-test of the `ai-sessions/0048` and `ai-sessions/0052` builds (`CAP-063`)

**Status:** 🟡 **Video pass done (1 fps over the whole film, zoomed single frames for every screen text quoted) — correlation with the HCI log
and the other logs not yet done.** Timeline written 2026-09-27 from the film only; the HCI/log correlation, the frame numbers and
`CAP-063-FINDINGS.md` are the work of `ai-sessions/0053`. The procedure the maintainer followed (the skeleton written before the capture) is kept
unchanged as **Appendix A**; this part records what was **actually** done — the maintainer skipped some steps and repeated others.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-063`                     |
|      Group(s)    |                        AY                          |
|       Date       |                    2026-09-27                      |
| Firmware version | `release_5.203` — shown on screen ("Firmware: release_5.203", film 15:58:15); wire check pending |
|   Test device    | Pixel 9a, GrapheneOS, Android 17 `CP2A.260805.005` (logcat/system log header `google/tegu/tegu:17/CP2A.260805.005/2026091901`). Bluetooth list also shows a "Charge 6" speaker (connected) and a "Niro" (saved) — another device's traffic may be in the HCI log |
| Build under test | the `ai-sessions/0052` build (commit hash not written down, skeleton P1): its log wording is present — "Settings read (channel 21)" (debug export line 28, 15:58:15.392), "Runtime info re-requested on Refresh (channel 19)" (line 206), "Setting 17 written (channel 21)" (line 357); the UI has the tabs Sound and Controls on film |
| Play services *Nearby devices* (P4) | not recorded |
| Video file       | `CAP-063-recording.mp4`: 1650.15 s, H.264 1280×720 rotation −90 (portrait), 49,253 frames (≈ 29.85 fps). **The audio track is empty** (`Audio: none, 0 channels`, handler "SoundHandle", no decodable codec) — what the maintainer heard (rings, EQ, balance, mono, conversation detection) is **not** on the film. Burned-in overlay `Sep 27, 2026 HH:MM:SS`, timestamp only: first frame 15:57:34, last 16:25:03 |
| Log file         | `CAP-063-btsnoop_hci.log` — `capinfos`: 11,615 packets, "Packet size limit: (not set)" (raw path), 2026-09-27 15:57:40.614–16:26:58.325 |
| Earlier log      | `CAP-063-btsnoop_hci.log.last` — 961 packets, 15:56:10.981–15:57:16.332: **before** the film (ends 18 s before its first frame); the Bluetooth off/on at the start of the session rotated the snoop log. Kept as evidence of the pre-session state |
| App debug export | `CAP-063-debug-export.log`: 746 lines, 15:57:55.706–16:18:29.012; **cut at exactly 65,536 bytes, mid-line** (the last line ends `… 7e 80 a3 03 2a 1e 18 00 32`) — the share-sheet hand-off of the old export truncated it; nothing after 16:18:29 (the last 6.5 minutes of the session) is in it. Fixed in the app 2026-09-27 (save as a file, 20,000-line buffer) |
| App logcat       | `CAP-063-OpenControl-for-Pixel-Buds-log-a5f9783708f6.txt` (UTC; offset to be measured) |
| System log       | `CAP-063-System-log-6cf0a8a3bd50.txt` (≈ 14 MB) |
| Clock offsets    | **Provisional (to be measured in `0053`):** phone clock ≈ film overlay + 1 s — the Disconnect tap seen at overlay 16:00:08 is logged 16:00:10.163 (export line 43), the Connect tap at 16:00:15 → 16:00:16.592 (line 45), the loss message on screen at 16:02:05 → "Session lost" 16:02:06.167 (line 125). The minute changes at the start/end (skeleton S0/Z2) were not filmed. Times below are **film overlay** times |
| Buds             | `04:00:6e:cf:6e:07` (same unit as every capture; also visible in Android's device-details screen at 16:17:10–13). Which slot is which bud, from the app's own charging lines: the **upper** slot holds the **Right** bud (16:15:47–52: upper bud out → "Right … not charging") — confirm from the wire |

## Capture-integrity pre-flight (partial)

```
$ capinfos CAP-063-btsnoop_hci.log         → 11,615 packets, limit (not set), 15:57:40.614170 – 16:26:58.325151
$ capinfos CAP-063-btsnoop_hci.log.last    → 961 packets, 15:56:10.981016 – 15:57:16.331589
$ ffprobe CAP-063-recording.mp4            → 1650.152 s; stream 0 audio "none, 0 channels"; stream 1 h264 1280x720, 49,253 frames
$ wc -c CAP-063-debug-export.log           → 65536 (last line cut)
$ sha256sum (prefixes)  1e86f191… btsnoop_hci.log   946bb87b… .log.last   0afcebce… debug-export   533fa333… logcat
                        8f9e1d65… recording.mp4     e22c8948… System-log
```
Still to do in `0053`: `cap_len ≠ len` and out-of-order checks, full hashes.

## Video review method

- 1 frame per second over the whole film (`ffmpeg -i CAP-063-recording.mp4 -vf "fps=1,scale=240:-2,tile=8x3"`, 69 contact sheets of 24 s), every
  sheet looked at; every screen text quoted below read on a zoomed single frame (`ffmpeg -ss <t> -i … -frames:v 1 -vf crop=…`, t = overlay −
  15:57:34). Transitions are to ≈ 1 s; the HCI log will narrow them.
- **Privacy — personal data on film:** the notification shade with legible third-party names and message previews (WhatsApp, LinkedIn, e-mail) at
  ≈ 15:57:43–44, 16:06:26, 16:06:39, 16:12:57–58, 16:16:48–49, 16:23:09–10; the Spotify media card ("Back To Black – Amy Winehouse") in Quick
  Settings at 16:06:28–38; Android's device-details screen showing the Buds' Bluetooth address at 16:17:10–13 (covered by ADR-010 for this
  project's own captures). **No address overlay** from the camera. To be decided by the maintainer before committing (as for `CAP-062`).
- The film shows the phone and the case only: **when the buds are in the ears or in a hand off-frame, the film cannot tell** — such states are
  inferred from the app's screen and marked so.

## Event Timeline (film overlay time; ≈ +1 s = phone time)

"Step" = the skeleton step (Appendix A) the action corresponds to; "rep." = a repetition; "var." = done differently. Evidence columns (HCI frames,
log lines) are filled in by `ai-sessions/0053`.

| Time (film) | Action / what the screen shows | Actor | Step | Notes |
|---|---|---|---|---|
| 15:57:34–37 | Film starts in Quick Settings, Bluetooth dialog "Bluetooth staat uit"; Bluetooth switched **on** (≈ 15:57:36) | User (Android) | S1 var. (Bluetooth on from Quick Settings, not from the app) | the S0 minute change and the spoken hash are not on film |
| 15:57:43–44 | Notification shade | User | — | privacy |
| 15:57:46–53 | OpenControl opened; *Nearby devices* → Toestaan; notifications → Toestaan | User | A3/A4 | the app's data had been cleared (permissions asked again) |
| 15:57:54–55 | "Bluetooth permission needed", then "No Pixel Buds Pro 2 paired yet" + **Pair a device** | App | B1 | the Buds were not bonded at the start (the Bluetooth list at 15:57:36 has no Pixel Buds) |
| 15:57:58–15:58:05 | Case lid opened (both buds seated, LED) | User | — | |
| 15:58:06 | Heads-up "Google Play services needs to sh…" (GmsCompat) | OS | — | as in `CAP-062` |
| 15:58:07–10 | **Pair a device** → CDM "Zoeken naar een apparaat" → "Toestaan … Pixel Buds Pro 2 van Ted" → Toestaan | User / OS | B2/B3 | |
| 15:58:11–14 | "Pairing… keep the Buds close" → "Connected to this phone (Android)" → **by itself** "connecting…" → "ready" (15:58:14) | App | AY-0 var. (buds in the case, lid open) | debug export 13–14: Connecting 15:58:14.568, DLCI 0x02 connected .941 |
| 15:58:15 | Connection: "Firmware: release_5.203", **no Safe Mode card**; Left/Right "100% (updated 15:58:15) — charging in the case (15:58:15)", "Case: 37% (updated 15:58:15)" | App | B1 (skeleton) / D1 | |
| 15:58:21–22 | Debug tab: **Debug mode on** | User | S3 | |
| 15:58:44–46 | ANC tab: "ANC mode: OFF (updated 15:58:15)", buttons disabled, "ANC can only be changed while you wear the Buds. Tap Refresh to check again." | App | F8 | buds in the case |
| 15:58:47–59 | Sound tab: "EQ updated: 15:58:15", Upper treble 5.0 / Treble 3.0 / Mid 2.0 / Bass −5.9 / Low bass −2.8; presets in two rows, no label cut off; Balance "Right 4 · read 15:58:15", Mono **on** (read 15:58:15), Conversation detection **on** (read 15:58:15) | App | B2, H0 | the original values of the skeleton's table (§2): Balance Right 4, Mono on, Conversation detection on |
| 15:59:24–26 | **LIGHT BASS** tapped with both buds docked → "EQ updated 15:59:26", 0 / 0 / 0 / −1.5 / −5.0 | User / App | AY-2 (early) | an EQ write while docked, accepted on screen |
| 15:59:29–30 | Controls: "Use touch controls" on, Left/Right "Noise control", "In-ear detection (setting): on — read-only …", all "read 15:58:15" | App | B2 | originals: touch on, hold L/R Noise control, in-ear on |
| 15:59:34–52 | Tab tour by **swiping** (Find, Debug, Find, Controls, Sound, ANC, Connection) | User | B3 (C10) | |
| 15:59:52 | Connection: charging (15:59:49), "Case: 36% (updated 15:59:49)" | App | E6 | a runtime-info push while docked, no user action |
| 16:00:08–09 | **Disconnect** → "App control: not open yet" | User | C5 part 1 | export 43: 16:00:10.163 |
| 16:00:15–16 | **Connect** → ready; L/R (updated 16:00:17), Case 36% (updated 16:00:17) | User | C5 part 2 (one tap, not two quickly — check the wire) | export 45–46 |
| 16:00:28–29 | **Disconnect** → not open yet | User | AY-9 | export 80 |
| 16:00:30–52 | Upper (Right) bud taken out (16:00:30–33), then the lower (Left) (16:00:50–52); lid open, case empty; the app **stays** "not open yet" | User | AY-9 ✓ (no re-open after a Disconnect tap) | buds off film afterwards |
| 16:00:59–16:01:02 | **Connect** → ready. For ≈ 2 s the old lines stay ("charging in the case (16:00:17)"), then "Left/Right 100% (updated 16:01:03) — not charging (out of the case) (16:01:04)", "Case: 36% — last seen 16:00:17 (no bud charging in the case)" | User / App | C4, E4 | the 2 s of stale "charging" after Connect is a finding to check |
| 16:01:14 | ANC tab: "ANC mode: ACTIVE (updated 16:01:03)", buttons **enabled** | App | — | buds out of the case (worn? not on film) |
| 16:01:16–33 | Tab tour (Sound, Controls, Find, Debug "Unidentified frames (16)", Find, Controls, Sound) | User | B3 rep. | |
| 16:02:02–05 | Connection; a bud put back in the **lower** slot (16:02:05–09); screen "not open yet — **The Buds closed the app's channel** (this happens when a bud goes in or out of the case or an ear). Android still shows the Buds connected; the app reopens its channel by itself …" + "IOException: bt socket closed, read return: -1" | Buds / App | AY-7 var. | export 125: Session lost 16:02:06.167 |
| 16:02:07 | **Ready again by itself** | App | ADR-044 (a) ✓ | export 130–131 (1.5 s later) |
| 16:02:09–25 | One bud seated (lower slot): the screen shows **both** buds "not charging (out of the case) (16:02:09)" and the Case "last seen 16:00:17" | App | E3 ✗? | ⚠ a bud is in the case on film but not "charging in the case" on screen — check the stream packets (0053) |
| 16:02:25–26 | **Disconnect** → not open yet | User | AY-9 rep. | export 164 |
| 16:02:28–36 | The seated bud taken out and re-seated (lower slot); app stays closed | User | AY-9 rep. ✓ | |
| 16:02:52–57 | Recents, home screen (folder "Luisteren"), back to the app: still "not open yet" | User | AY-9 ✓ (also not on resume) | |
| 16:03:02–04 | **Connect** → ready; L/R "not charging (out of the case) (16:03:06)", Case last seen 16:00:17 | User | C4 rep. | ⚠ a bud is still seated (lower slot) on film |
| 16:03:14–17 | The seated bud taken out and laid **left of the case** | User | — | |
| 16:03:34 | Charging lines re-stamped (16:03:34) | App | — | a push |
| 16:03:44–48 | **Refresh battery** → "updated 16:03:48" | User | AY-13a | export 206: "Runtime info re-requested on Refresh (channel 19)" |
| 16:03:50–16:04:02 | The bud on the table picked up/moved; "The Buds closed the app's channel …" (16:03:59) → ready **by itself** (16:04:01–02), updated 16:04:03 | Buds / App | AY-5 var. ✓ | export 218–224 |
| 16:04:11–17 | **Refresh battery** → updated 16:04:17 | User | AY-13a rep. | export 257 |
| 16:04:20–26 | Both buds off film (put in the ears); push 16:04:24; **Refresh battery** 16:04:25–26 → updated 16:04:32 | User | AY-13a rep. | export 272 |
| 16:04:37 | ANC tab: "ACTIVE (updated 16:04:32)", enabled | App | — | worn |
| 16:04:39–44 | **ADAPTIVE** → "ADAPTIVE (updated 16:04:42)"; **OFF** → "OFF (updated 16:04:44)" | User | F6 var. (≈ 2 s apart, not "immediately") | |
| 16:05:25–28 | ANC **Refresh** → "ANC mode: ACTIVE (updated 16:05:28)" — changed from OFF without an app tap | User / Buds | F5 ✓ (the press-and-hold on a bud itself is off film) | the maintainer's observation 9 |
| 16:05:46–48 | Android volume panel | User | — | |
| 16:06:04–07 | Both buds taken **out of the ears** and laid on the table; ANC tab "Not connected to the Buds …", "Connection: Disconnected" | User / Buds | AY-4 | export 311: Session lost 16:06:08.077 |
| 16:06:08–12 | "Still connecting…" → ready **by itself** → "ANC mode: OFF (updated 16:06:11)", buttons disabled + "ANC can only be changed while you wear the Buds." | App | AY-4 ✓ / F8 ✓ | |
| 16:06:20–21 | Tap on the disabled ADAPTIVE: nothing | User | AY-4 ✓ | |
| 16:06:24–38 | Quick Settings (shade visible 16:06:26), **ANC tile** tapped ≈ 16:06:28 → toast "ANC can only be changed while you wear the Buds." (16:06:29–38) | User / App | AY-4 tile ✓ | privacy; Spotify card |
| 16:06:39–41 | Notification shade; back to the app | User | — | privacy |
| 16:06:47 | Both buds picked up (then in the ears, off film) | User | D0 | |
| 16:06:57–16:07:12 | Connection "not charging (16:06:54)"; ANC still OFF (16:06:11) disabled; Sound tab: EQ = Light bass (read 16:06:10), Balance Right 4, Mono on, Conversation detection on (read 16:06:11) | App | — | ⚠ ANC stays disabled after the buds are back in the ears until a Refresh (no claim since) |
| 16:07:14–16 | **Balance → L** → "Left 100 · changed 16:07:16" | User | D1 | export 357 |
| 16:07:19–22 | **Balance → R** → "Right 100 · changed 16:07:22" | User | D2 | export 360 |
| 16:07:30–32 | **Balance ≈ halfway left** → "Left 52 · changed 16:07:32" | User | D3 | export 363 |
| 16:07:41–16:08:00 | Several drags toward the centre: "Right 4 · changed 16:07:49", …, "Right 1 · changed 16:08:00" — **exact centre (0) never reached** | User | D4 rep. (✗ "Centre") | ⚠ export 366–399: **12 writes of field 17 in 17 s** (e.g. 16:07:48.609, 49.718, 51.940, 52.333) — more writes than completed drags visible on film? check |
| 16:08:04–06 | **Mono off** → changed 16:08:06 | User | D6 (D5 skipped: mono was already on) | export 404 |
| 16:08:19–20 | **Conversation detection off** → changed 16:08:20 | User | D7 part 1 | export 407 |
| ≈ 16:08:29–30 | **Conversation detection on** → changed 16:08:30 | User | D7 part 2 | export 410 |
| 16:08:49–56 | Home → folder "Luisteren" → Spotify "Amy Winehouse Mix", **play** → back to OpenControl (Sound tab) | User | P10 var. (music, not a stereo test file) | music from here |
| 16:08:58–16:09:10 | Balance again: "Right 100 · changed 16:09:00", "Left 96 · 16:09:04", "Right 11 · 16:09:08", "Right 4 · 16:09:10" | User | D1–D4 rep. (with music) | export 417–429 |
| 16:09:11–14 | **Mono on** → changed 16:09:12; **Mono off** → changed 16:09:14 | User | D5, D6 rep. | export 432/435 |
| 16:09:16–20 | **Conversation detection off** → 16:09:17; **on** → 16:09:20 | User | D7 rep. | export 438/441 |
| 16:09:21–36 | Sound tab idle, music playing | User | D7 (talking — not on film, no audio) | observation 4 |
| 16:09:37–41 | Controls: **Use touch controls off** → changed 16:09:41 | User | D8 | export 446 |
| 16:09:41–57 | (bud taps off film) | User | D8 | observation 10 |
| ≈ 16:09:58–59 | **Use touch controls on** → changed 16:09:59 | User | D9 | export 451 |
| 16:10:34–36 | **Left: Digital assistant** → changed 16:10:36 | User | D10 | export 454 |
| 16:10:47–48 | **Left: Noise control** → changed 16:10:48 | User | D11 | export 457 |
| 16:10:54–55 | **Left: Digital assistant** → changed 16:10:55 | User | D10 rep. | export 460 |
| 16:11:22–24 | **Right: Digital assistant** → changed 16:11:24 | User | D12 part 1 | export 463 |
| 16:11:33–16:12:09 | Further press-and-hold chip taps, ending with **both Noise control** (export 466 16:11:34.847 and later lines — the exact sequence from the export/HCI) | User | D11/D12 rep. | 16:11:35–37 volume panel |
| 16:12:28–32 | **Disconnect**, **Connect** → ready | User | D13 part 1 | |
| 16:12:37–45 | Sound: Balance Right 4, Mono off, Conversation detection on; Controls: touch on, L/R Noise control, in-ear on — all "read 16:12:3x" | App | D13 ✓ (read back) | |
| 16:12:57–16:13:06 | Notification shade (privacy), Recents → Spotify → back | User | — | |
| 16:13:15–17 | **HEAVY BASS** → EQ updated 16:13:17 | User | H2 (one preset only) | |
| 16:13:21–24 | **Upper treble** dragged to +5.2 → updated 16:13:24 | User | H3 (not to +6, no −6) | |
| 16:13:28–32 | **Low bass** dragged to −5.7 → updated 16:13:32 | User | H4 (−6 only) | |
| 16:13:46–57 | Scroll; finger on the screen | User | H5 — "Read EQ again" not shown (no error) → not run | |
| 16:13:58 | Connection: L/R (updated 16:12:34) | App | — | |
| 16:14:04–07 | **Refresh battery** → updated 16:14:07 | User | AY-13b part 1 | |
| ≈ 16:14:08–10 | **Refresh battery** again → updated 16:14:10 | User | AY-13b var. (≈ 2–3 s apart, not < 1 s) | no "No new battery reading" on screen |
| 16:14:48–51 | **Refresh battery** → updated 16:14:51 | User | AY-13 rep. | debug export ends 16:18:29 — the rest only in HCI/logcat |
| 16:15:07–19 | Case picked up; **both buds put in the case** (both seated by ≈ 16:15:19); screen dims/off 16:15:14–23 | User | AY-7 var. (both at once) | |
| 16:15:24 | "Paired — not connected to this phone … The Maestro channel (equalizer) was closed. Tap Connect to reconnect." | App | AY-6 var. | ⚠ loss cause "undetermined" wording, while the ACL dropped (ADR-016) — check the link readings (screen was off) |
| 16:15:32–38 | **Connect** (Android not connected): connecting → ready (16:15:36), "Android doesn't show the Buds as connected (yet)" → "Connected to this phone (Android)" (16:15:38); L/R charging in the case (16:15:38), Case 36% (updated 16:15:37) | User / App | C7-like | the app's socket created the ACL, as in `CAP-062` |
| 16:15:47–52 | Upper (Right) bud taken out → "Right … not charging (out of the case) (16:15:52)", Left "charging in the case (16:15:51)" | User / App | E3 ✓ / AY-7 | slot ↔ bud established here |
| 16:15:53–55 | Lower (Left) bud taken out → both not charging; "Case: 36% — last seen 16:15:52 (no bud charging in the case)" | User / App | E4 ✓ | |
| 16:15:57–16:16:04 | Screen off (phone locked) | User | AY-10 var. | |
| 16:16:05–07 | Unlocked: app "connecting…" → ready **by itself** (resume) | App | ADR-044 (c) ✓ | |
| 16:16:08–11 | Right bud back in the upper slot → "Right … charging in the case (16:16:11)", "Case: 36% (updated 16:16:11)" | User / App | AY-7 ✓ | |
| 16:16:25–30 | Case 36% (16:16:25); Left bud seated (lower slot) → "Left … charging in the case (16:16:30)", Case 34% (16:16:30) | User / App | AY-7 ✓ | |
| 16:16:32–35 | Case handled; "not open yet — **Android no longer shows the Buds connected** to this phone — after a disconnect in Android's own Bluetooth settings, with both buds in the case, or out of range. Tap Connect to reconnect." | Buds / App | AY-6 ✓ (ACL drop, no re-open while the link is down) | |
| 16:16:48–53 | Notification shade (privacy); QS Bluetooth: "Pixel Buds Pro 2 van Ted — Opgeslagen" | User | — | |
| 16:17:01–06 | Buds row tapped → "Verbinding maken…" → "Actief. Batterijniveau 100%" (buds docked, lid open) | User (Android) | AY-6 / AY-8b var. | |
| 16:17:10–13 | Android's device-details screen (shows the Bluetooth address) | User | — | privacy (ADR-010) |
| 16:17:14 | App **ready by itself** (link back): L/R charging in the case (16:17:06), Case 34% (updated 16:17:06) | App | ADR-044 (b) ✓ | |
| 16:17:18 / 16:17:28 / 16:18:18 / 16:18:29 | Charging/Case re-stamped (pushes while docked and idle) | Buds | E6 ✓ | |
| 16:17:20–16:18:38 | Idle, both docked, lid open (≈ 80 s, not 2 min) | User | AY-14 var. | |
| 16:18:38–40 | **Refresh battery** → L/R and "Case: 34% (updated 16:18:40)" | User / App | AY-14 | the Case time moved at the Refresh — answered re-subscription? (HCI; the export ends 16:18:29) |
| 16:18:45–47 | Find tab: **Ring Left** with both buds **docked** (lid open) → "Ringing: Left earbud — tap Stop to end it." | User / App | AY-1 | ring audible? not on film (no audio) — maintainer's observation |
| 16:19:07–08 | **Stop** → the notice disappears | User | AY-1 | |
| 16:19:14–24 | Sound tab; **BALANCED** tapped while docked → Treble −1.0, Mid 1.0, Bass 0.5, Low bass −3.5 | User / App | AY-2 ✓ | |
| 16:19:34–37 | **Mono on** (changed 16:19:35), **off** (16:19:37) while docked | User / App | AY-2b ✓ | |
| 16:19:44–46 | **Lid closed** (both docked); 16:19:47 the Sound controls grey out | User | C8 | |
| 16:20:05 | Connection: "Paired — not connected … Android no longer shows the Buds connected …" | App | C8 ✓ | |
| 16:20:12–13 | **Lid opened** (both docked); app unchanged (Android not reconnected) through 16:20:21 | User | — | |
| 16:20:22–26 | Right, then Left bud taken out; app **connecting by itself** (16:20:25) → ready (16:20:26); L "charging (16:20:27)", R not → both "not charging (16:20:28)"; "Case: 33% — last seen 16:20:28" | User / App | C9 ✓, E4 ✓ | |
| 16:20:28–16:21:01 | Buds laid on the table, then picked up (one stays left of the case from 16:21:00; the other off film) | User | — | |
| 16:21:21 | **Home** (app in the background) | User | AY-10 / J4 | |
| 16:21:47–57 | Screen off, unlock, Recents → OpenControl: still **ready**, "not charging (16:21:56)" | User / App | AY-10 var. (no session loss happened while in the background) | |
| 16:22:02–59 | **Home** again, ≈ 57 s in the background (screen off 16:22:27–34) | User | J4 var. (≈ 1 min, not 2) | |
| 16:22:59–16:23:01 | Back via Recents: still **ready**; re-stamps 16:22:19 / 16:23:06 | App | J4 ✓ | |
| 16:23:09–13 | Notification shade (privacy); QS Bluetooth: tap "Pixel Buds Pro 2 … Actief" → **disconnect in Android** → "Opgeslagen" | User (Android) | AY-8 | the app is hidden behind the dialog for 20 s — its text during AY-8 is not on film |
| 16:23:34–39 | Buds row tapped again → "Verbinding maken…" → app **ready by itself** (16:23:38–39), updated 16:23:37 | User / App | AY-8b ✓ | |
| 16:23:46–52 | Both buds laid on the table; "The Buds closed the app's channel …" (16:23:49) → connecting → ready **by itself** (16:23:52), updated 16:23:53 | User / Buds / App | AY-5 var. ✓ | |
| 16:24:12–13 | Find: **Ring Left** → "Ringing: Left earbud — tap Stop to end it." (buds on the table) | User / App | AY-11 part 1 | |
| 16:24:19–20 | Connection: **Disconnect** → not open yet | User | AY-11 part 2 | |
| 16:24:31–35 | Find tab: "Not connected to the Buds …" + "**A ring was started on the Left earbud — reconnect and tap Stop to end it.**" | App | AY-11 ✓ (the `0048` I-6 fix, `CAP-062` I4 ✗ fixed) | |
| 16:24:37–38 | **Connect** → ready | User | AY-11 part 3 | |
| 16:24:40 | Find: "**A ring was started on the Left earbud before the app reconnected — it may still be ringing. Tap Stop to end it.**" | App | AY-11 ✓ | |
| 16:24:42–43 | **Stop** → the notice disappears | User | AY-11 part 4 | ring stop audible? not on film |
| 16:25:02–03 | Debug tab: "Unidentified frames (163)"; film ends | User | — | |

## Step mapping (skeleton Appendix A → what happened)

| Step | Result |
|---|---|
| S0 (minute change, spoken hash) | **skipped** |
| S1/S2 (Bluetooth off → app "disabled" → Enable) | **done differently** — Bluetooth switched on in Quick Settings before the app was opened; the app's "Bluetooth is disabled" screen not shown |
| S3 Debug mode on | done 15:58:21 |
| (not planned) B1–B3 pairing from the app | done 15:57:54–15:58:14 (the Buds had been forgotten; the app's data cleared) |
| AY-0 connect by itself | done differently (buds in the case, lid open, right after pairing) — ready by itself 15:58:14 |
| B1/B2/B3 (skeleton) | done 15:58:15–15:59:52 |
| C5 double Connect | done differently (one tap) — check the wire |
| AY-9, C4 | done, repeated (16:00:28 … 16:03:04) |
| AY-3a–c (one bud worn) | **skipped** |
| F6 | done differently (two taps ≈ 2 s apart) |
| F7 | **not identifiable** / skipped |
| F5 | done (16:05:25) |
| AY-4 | done (16:06:04–38), app and tile |
| D0 | done 16:06:47 |
| D1–D4 | done, repeated (16:07:14–16:08:00 and 16:08:58–16:09:10); "Centre" never reached |
| D5 | skipped the first time (mono already on); done 16:09:11 |
| D6, D7 | done, repeated |
| D8, D9 | done (16:09:40, 16:09:58) |
| D10–D12 | done, repeated (16:10:34–16:12:09) |
| D13 | done (16:12:28–45) |
| H2 | partly (Heavy bass only; Light bass/Balanced were tapped at other times: 15:59:25, 16:19:24) |
| H3/H4 | partly (Upper treble +5.2, Low bass −5.7; no ±6 pair) |
| H5 | not run ("Read EQ again" only appears after an error) |
| AY-13a | done, repeated (16:03:44, 16:04:11, 16:04:25, 16:14:04, 16:14:48) |
| AY-13b | done differently (≈ 2–3 s apart, not < 1 s) |
| AY-7 | done (16:15:07–16:16:30), plus variants (16:02:05, 16:02:36) |
| AY-6 | done (16:15:24, 16:16:35) |
| AY-14 | done differently (≈ 80 s idle, not 2 min) |
| AY-1, AY-2, AY-2b | done (16:18:46, 16:19:24, 16:19:35) |
| C8 | done (16:19:44) |
| C9 | done (16:20:25) |
| AY-5 | done differently (buds from the table / out of the ears: 16:03:59, 16:06:07, 16:23:49) |
| AY-10 | done differently (no session loss happened while in the background; resume re-open at 16:16:05 after a lock) |
| J4 | done differently (≈ 1 min) |
| AY-8, AY-8b | done (16:23:11–39); the app's text during AY-8 hidden by the dialog |
| AY-11 | done (16:24:12–43) ✓ |
| K4, K1, K2, K3, K5 | **skipped** |
| L2, L3 | export done **off film** (the file is dated 16:40); L3 not identifiable |
| A5, (E), B4 | **skipped** |
| Z1 restore | **not done** — at the end Balance Right 4 ✓ (original), Mono **off** (was on), Conversation detection on ✓, EQ Balanced (was 5/3/2/−5.9/−2.8), touch/hold as original |
| Z2 minute change | **skipped** |

## Open points for the analysis (`ai-sessions/0053`)

- Every row: HCI frames, debug-export lines (until 16:18:29), logcat/system-log lines, and the measured clock offset.
- ⚠ 16:02:09–16:03:34: a bud seated in the lower slot while the app shows both "not charging" — stream contents?
- ⚠ 16:01:02: ≈ 2 s of stale "charging in the case" lines after Connect.
- ⚠ 16:07:41–16:08:00: 12 balance writes in 17 s — one per completed drag, or more?
- ⚠ 16:15:24: the "was closed" (undetermined cause) wording for an ACL drop with the screen off.
- ⚠ 16:06:57–16:07:12: ANC disabled after the buds went back into the ears, until a claim.
- AY-14 / 16:18:40: was the re-sent `SubscribeRuntimeInfo` answered?
- AY-1: `04 01` while docked — ACK or NAK? (no audio on the film)
- The empty audio track: the maintainer's observations about sound (rings, EQ, balance, mono, conversation detection) stand as observations.

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
| Events file      | `CAP-063-events.txt` (your own time + action notes, P6) |
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
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-063-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AY/CAP-063-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-063-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AY/CAP-063-EVENT-NOTES
