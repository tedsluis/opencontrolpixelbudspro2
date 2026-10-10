# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, the secondary user without Google Play) — Group BH, the 1.2.0 release APK (`CAP-072`)

**Status:** ✅ **Captured 2026-10-09 and analyzed 2026-10-09/10** (`ai-sessions/0083`) — film 1 only; **film 2 (BH-25, BH-26) not made**: the maintainer folded it into the hardware run of the rebuilt 1.2.0 (verdict "fix first", `DECISIONS.md` ADR-061; chat 2026-10-10). One camera film without
sound (overlay 17:27:26–18:23:39), one Android screen recording with sound (phone 17:27:26.3–18:23:42.4), four HCI snoop logs, four app debug exports, one app
logcat, the P0/P1 shell log and a filtered extract of the system log. Folder renamed from the placeholder `CAP-072-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BH` to the
camera film's first/last **overlay** times; file modes set to 644; the films are committed with the caller's number, the Wi-Fi name and the browser history
blurred; of the system log only `CAP-072-logcat-extract.txt` is committed (the maintainer's choices in chat 2026-10-09/10, `ai-sessions/0083` RESULT). The
analysis is `CAP-072-FINDINGS.md`.

**The run differs from the plan:** 1.1.1 was **freshly installed at 17:24** after the app (the `dist/1.2.0` release APK, the maintainer's statement) had been
uninstalled for all users; the run itself starts with 1.1.1 on screen and updates it to 1.2.0 at 17:29. Steps were done in another order and partly
differently: the worn line was read at many more moments than BH-5 … BH-13 name (the buds went in and out of the case 20 times), the head was in view only once
(17:37:09–13), the press-and-hold of BH-11 is not on either film (the maintainer's statement), the case-sound test (BH-14) was done with the switch on gear →
Settings while taking buds from the ears into the case (17:55:50–18:02:28), an **incoming call was answered** (18:02:25–18:04:59), the ANC tile was first added
to user 10's Quick Settings, music came from an internet radio in the browser, the balance slider needed ≈ 30 writes to land on "Right 4", only **Ring Left**
and **Ring Right** with their Stops were done, the rotation of S12 happened before the save dialog, not during it; BH-23 (the early tap) is not identifiable;
BH-25/BH-26 are film 2. The planned procedure (the committed skeleton) is kept unchanged as the appendix.

**Prefixes used below:** "A" = `CAP-072-btsnoop_hci2.log.last` (15:40:29–18:18:33, the run up to the Bluetooth off), "B" = `CAP-072-btsnoop_hci2.log`
(18:18:45–18:23:12), "Z" = `CAP-072-btsnoop_hci1.log.last` (2026-10-08 21:47 – 2026-10-09 15:40, before the run). `CAP-072-btsnoop_hci1.log` is a byte-for-byte
prefix of A and is not counted. Exports: E1 `…-181532.txt`, E2 `…-181901.txt`, E3 `…-182109.txt`, E4 `…-182335.txt` (each a byte prefix of the next; line numbers
below are E4's, which holds them all). L = the app logcat `CAP-072-OpenControl-for-Pixel-Buds-Pro-2-log-c0b102db82fc.txt` (UTC). X = `CAP-072-logcat-extract.txt`
(line numbers of the extract). "S" = the screen recording (its own clock in the status bar is the phone's); "C" = the camera film (overlay time). All times
below are **phone time** unless marked.

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-072` |
| Group(s) | BH |
| Date | 2026-10-09, phone 17:27:26–18:23:42 (film 1); film 2 not made |
| Phone | Pixel 9a, GrapheneOS `google/tegu/tegu:17/CP3A.261005.005/2026100601:user/release-keys` (L line 2) — **newer than `CAP-071`'s `CP3A.260905.009`**; secondary user 10, `userType: full.secondary` (L line 3); "dev options enabled" |
| Google Play in user 10 | none: P0 (below) lists only `app.grapheneos.gmscompat*`, `exit=0`; no `com.google.android.gms` / `com.android.vending`; 0 Play-services claims on the wire in A, B and `…hci1.log` (FINDINGS §1) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.2.0, build `ec6d163` (2026-10-09)**, versionCode 10200 — Info tab on S 17:32:2x (s297) and 18:11:2x (s2653), no "-dirty"; L line 5 `package: io.github.tedsluis.opencontrolpixelbuds:10200, targetSdk 34`; `dist/1.2.0` APK SHA-256 `a67adad1…44c8`, certificate `a7530f5c…d8dcb` |
| Version before the update | **1.1.1, build `86a6fb3` (2026-10-08)**, Info tab on S 17:28:0x (s40, s55–59); installed 17:24:41 into user 10 (uid 1010354) after `cmd package uninstall` at 17:24:06 (X 817–826) |
| Version before 17:24 | the app with uid 10353 (installed for `CAP-071`) was a build that sends `GetHardwareInfo` (Z 11373, 20860, 26365; A 5667) — the `dist/1.2.0` release APK (maintainer, chat 2026-10-10) |
| Official Pixel Buds app | not in this user |
| Buds | Pixel Buds Pro 2, `04:00:6e:…:07` (classic handle `0x000b` in every ACL), firmware `release_5.203` ×3 on the Info tab and in 31 of 31 announcements; serials (redacted) Case `5707…51`, Right bud `5708…09`, Left bud `5707…47` |
| Other devices | an LE device `c8:cc:…` (handles `0x0040`/`0x0041`), "Charge 6" (connected) and "Niro" (saved) in Android's Bluetooth dialog (S 18:18); none touched by the app |
| Video files | `CAP-072-recording.mp4` — camera, 1280×720 (rotation −90 → portrait), 29.87 fps, 3,373.3 s, **audio stream `codec_name=unknown`, 0 channels** (empty; `ffmpeg -map 0:a` → "Decoding requested, but no decoder found for: none"); `CAP-072-screen-20261009-182342-1791559646223.mp4` — screen recording 1080×2424, 59.94 fps, 3,376.1 s, **AAC 44.1 kHz mono** (the phone's microphone) |
| Log files | four HCI logs, four exports, the app logcat, the shell log, the system-log extract (the full log stays local) |
| Clock offsets (measured) | **S:** phone = 17:27:26.25 + recording time (the Disconnect tap: E4 line 53 17:31:08.853 = S t 222.6 s, 10-fps frames; end 18:23:42.35 = the file name's 18:23:42). **C:** phone = 17:27:25.9 + camera time = overlay − 0.1 s ± 0.5 s (the same tap at C t 223.0 s, 10 fps; and the recorder pill flipping 02:00 → 02:01 at C t 120.4 s); the end overlay 18:23:39 ↔ phone 18:23:39.2. **Exports ↔ HCI:** E4 "Maestro channel announced by the Buds: 21" 17:29:26.359 vs the announcement A 7513 17:29:26.353 (+6 ms). **L ↔ local:** L is UTC (L line 648 `16:21:56` = X `18:21:56.757` "Wrote stack traces"). **System log** = local time (same log clock as L + 2 h) |
| Wear mapping | the camera shows the case slots, not the head (the head was in view only at C 17:37:09–13); frame-left slot = **Left** bud (runtime info 6.2 field 2 = 1 at A 8619 17:35:57 when the frame-left bud left the case); ear removal is marked on the wire by the Buds' AVRCP PAUSE (in-ear detection on) |

### The maintainer's P0/P1 outputs (CAP-072-adb-shell.log, copied unchanged)

```
Fri Oct  9 05:26:37 PM CEST 2026
    versionCode=10101 minSdk=34 targetSdk=34
    lastUpdateTime=2026-10-09 17:24:41
      firstInstallTime=1970-01-01 01:00:00
      firstInstallTime=2026-10-09 17:24:41
Fri Oct  9 05:26:53 PM CEST 2026
package:app.grapheneos.gmscompat.config
package:app.grapheneos.gmscompat.lib
package:app.grapheneos.gmscompat
exit=0
Fri Oct  9 05:29:54 PM CEST 2026
    versionCode=10200 minSdk=34 targetSdk=34
    lastUpdateTime=2026-10-09 17:29:24
      firstInstallTime=1970-01-01 01:00:00
      firstInstallTime=2026-10-09 17:24:41
```

Reading: `firstInstallTime` of user 10 is unchanged across the update (17:24:41) and `lastUpdateTime` moves to 17:29:24 with `versionCode=10200` — the update
of U1/V1. The `1970-01-01` line is user 0, where the app is no longer installed after the 17:24 uninstall (the new install at 17:24:40–52 was for user 10 only:
`PACKAGE_ADDED replacing: false … uid 1010354`, full system log 17:24:52.313). **Not in the file:** the positive control of P0 (`grep opencontrol`, exit 0)
and the output of `am get-current-user`; the adbd lines of the full system log show both commands ran (17:26:53 `am get-current-user`, 17:26:54 `pm list
packages --user 10`). The user id 10 is also in every `am_proc_start` of the app (`[10,12137,1010354,…]`, X 3362).

## Capture-integrity pre-flight

| File | Packets / lines | Range (phone) | Check |
|---|---|---|---|
| Z `CAP-072-btsnoop_hci1.log.last` | 36,083 | 2026-10-08 21:47:46.610 – 15:40:25.818 | before the run; ends at the user switch 0 → 10 (`am_switch_user` 15:40:24; Bluetooth restarted for user 10: `com.android.bluetooth` started 15:40:25 in user 10) |
| `CAP-072-btsnoop_hci1.log` | 21,727 | 15:40:29.398 – 18:17:59.310 | **byte-for-byte prefix of A** (`cmp -n 1244736` exit 0); pulled by bugreport 1 (18:16:0x) — not counted |
| A `CAP-072-btsnoop_hci2.log.last` | 21,861 | 15:40:29.398 – 18:18:33.356 | the run up to BH-22's Bluetooth off (ACL `0x16` A 21856 18:18:32.926) |
| B `CAP-072-btsnoop_hci2.log` | 1,158 | 18:18:45.641 – 18:23:12.322 | from Bluetooth on to bugreport 2; **18:23:12–18:23:42 no HCI log** (the screen recording and E4 cover it) |
| E1 … E4 | 2,121 / 2,197 / 2,335 / 2,346 lines | 17:29:26.093 – 18:15:24.702 / 18:18:49.377 / 18:21:06.785 / 18:23:05.380 | one process (12137); E(n) is a byte prefix of E(n+1); Debug mode on throughout (1,004 `DLCI 0x` lines in E4) |
| L app logcat | 722 lines | UTC 15:25:16.837 – 16:23:46.806 | PIDs 11717 (1.1.1, events only) and 12137 (1.2.0); no `FATAL`, `ANR in`, "Decoder fault", "Unexpected error", `StrictMode`, "Late WriteSetting", "request held" (each `grep -c` → 0, exit 1; positive control `grep -c "Settings read"` → 30) |
| System log (local, not committed) | 326,217 lines | 10-08 21:46:59 – 10-09 18:24:23.574 | `logcat -b all` started 17:26:16.231; the extract X (41,178 lines) is committed |
| Shell log | 15 lines | 17:26:37 – 17:29:54 | above |
| Films | C 3,373.3 s, S 3,376.1 s | C 17:27:25.9 – 18:23:39.2; S 17:27:26.3 – 18:23:42.4 | no gap; C's audio stream empty |

**The Bluetooth off/on boundary (BH-22):** A ends 18:18:33.356 (after the phone's `DISC` of DLCI 3, A 21841 18:18:32.832, and ACL `0x16` A 21856); B starts
18:18:45.641 (Bluetooth `OFF → TURNING_ON` 18:18:45.967, E4 2142). No Buds ACL spans the gap; nothing is lost or counted twice. **No P2 Bluetooth off/on was
done on film** — A runs without a rotation from 15:40:29 to 18:18:33.

**Film 2 (not made):** BH-25 (export, Bluetooth off, the force-stop, three `uiautomator` dumps), BH-26 (Bluetooth on, export) — none of their files is in the
folder (checked at the session start and again before the checkpoint); the maintainer moved them to the run of the rebuilt 1.2.0 (chat 2026-10-10).

## Video review method (and privacy)

- **Screen recording:** decoded at 1 frame/s (3,376 frames, 540 px wide); each second compared with the previous one (pixel difference below the status bar);
  every second with any change (746 of 3,375) viewed on labelled 4-frame sheets; static stretches are the same screen. Transitions narrowed with 10-fps frames
  where a claim depends on them (the Disconnect tap). Every text on screen read from these frames.
- **Camera film:** frames every 2 s (1,687) viewed as full frames for 17:27–17:37 and as case close-ups for the whole film; 5-fps strips ±3 s around every
  bud-into-case moment. The camera films the case and the phone; the head is in view only at 17:37:09–13.
- **Audio:** the camera's track is empty. The screen recording's mono track: per-second level and spectral centroid; spectrograms of every minute with sound;
  narrow-band spectrograms (1.25–1.85 kHz) ±3–4 s around every bud-into-case moment with the camera strip aligned above them. **No speech-to-text tool is
  available offline on this machine** (no whisper/vosk; checked); speech is therefore located, not transcribed: speech-like sound (voiced harmonics < 1 kHz,
  formants) in 140 segments, e.g. 17:35:58–18:01:24 between almost every case event, 17:45:28–17:46:31, 17:48:01–17:48:11, 17:50:54–17:51:23. The content is
  asked from the maintainer (RESULT, checkpoint). Music (the internet radio) plays 17:54:59 onwards with pauses; the Buds' **Find ring** (S 18:12:28–18:12:45
  and 18:12:50–18:13:09: harmonic tones ≈ 1.2–4.6 kHz, growing louder) is the positive control that the microphone hears the Buds at table distance; the phone's ringtone at
  18:02:25–31.
- **Privacy:** the screen recording showed the caller's full phone number (18:02:25–18:04:59), the Wi-Fi SSID (a street name and number) in Quick Settings
  (17:49:3x–17:50:0x, 17:50:3x, 18:05:4x, 18:18:0x–18:18:3x, 18:23:4x), the browser's search history (17:54:2x–17:54:5x) and other Bluetooth device names
  (18:18); the camera film shows the phone screen including the call screen, and the maintainer's hair and glasses briefly. **The committed films are blurred**
  at those ranges (commands in FINDINGS §0); the serials on the Info tab stay readable (component serials, the maintainer's choice of ADR-010 scope; never
  written unredacted in any text). No address overlay on the camera film. The call audio: the maintainer's voice only (the call ran on the Buds over eSCO).

## Event Timeline

Columns: phone time · what happened (actor) · step / V / test-plan / Test-ID · evidence. "said" = speech on the audio track (not transcribed).

| Phone time | Event | Step · V · IDs | Evidence |
|---|---|---|---|
| 08:43–15:04 (before) | the app (uid 10353) runs 7 sessions with the thirteen requests incl. `GetHardwareInfo`; 13:26:24–29 writes `28:0`, `27:0`, `28:1`, `27:1` (the release APK tried before the run) | not in the plan (P9) | Z 11373 … 26365, Z 21478–21569 |
| 15:40:24 | user switch 0 → 10; Bluetooth restarts; Z ends | — | system log `am_switch_user` |
| 17:19:38 | a Connect of the uid-10353 app (12 reads, subscription, `GetHardwareInfo`) | P9 deviation | A 5589–5667 |
| 17:24:06 | `cmd package uninstall` of the app (all users); PID 6994 killed | P1 done differently | X 817–826 |
| 17:24:40–52 | 1.1.1 installed into user 10 (uid 1010354, dex2oat `app-version-name:1.1.1`) | P1 | X 1019–1022 |
| 17:25:16–17:25:34 | 1.1.1 started (PID 11717), one session (12 reads, 2 subscriptions, no `GetHardwareInfo`) | P9 deviation | X 1275; A 6827–6908 |
| 17:26:16 | `adb logcat -b all` started | P6 ★ done | adbd line |
| 17:26:37 / 17:26:53 | P1 `dumpsys` (before) / P0 | P0, P1 | shell log |
| 17:27:26 | **both films start**; app 1.1.1 on the Connection tab, light theme, ready; both buds in the case, lid open | P3, P4 ★, P8 ★ | S s0; C 17:27:26 |
| 17:28:0x | gear → Settings (Dark mode System); **Info: "App: 1.1.1, build 86a6fb3 (2026-10-08)"**, control channel 21; Debug: **Debug mode switched on** | P1 (on film) | S s37–s59 |
| 17:28:2x | Settings: **Dark mode → On** (the app turns dark) | P1 | S s60–s62 |
| 17:29:12–25 | **1.2.0 installed over 1.1.1** (Android's "Updaten…" on film); process 11717 killed `killDueToPackageUpdate`; 12137 starts `top-activity` | P1, BH-1 · V1 · U1 | X 3257–3362; S s119; C 17:29:26 |
| 17:29:26 | 1.2.0 opens by itself, **dark**; session ch 21: announcement, `ReadSetting` 16 + twelve, `SubscribeRuntimeInfo`, **`GetHardwareInfo`** → `RESPONSE` field 7 (37 ms) | BH-1 · V1, V2, V16 · `PAIR-003`, `BATT-004`, `FW-003` | E4 4–52; A 7499–7596 |
| 17:29:27 | Connection: Case 57 %, **"Both buds in the case"** | BH-3 · V4 · `INEAR-006` | S s120–s121 |
| 17:29:54 | P1 `dumpsys` (after) | P1 | shell log |
| 17:31:08 | **Disconnect** tap; Controls while disconnected: Conversation detection card after In-ear detection, **no Case sounds card** | not in the plan; BH-1's Controls check · V3 | E4 53; S s222–s236 |
| 17:31:28 | **Connect** tap; Controls shows "—" for touch and head gestures for < 1 s, then the values (S6 seen) | S6 · V2 | E4 55–99; S s239–s247 |
| 17:31:5x–17:33:5x | gear → Settings: Dark mode **On** (kept), **Case sounds card** between Dark mode and "Use different Buds", its (i) "Earbuds replaced: read 17:31:29 / Other alerts: read 17:31:29 / These settings live on the case and are read when the app connects."; Debug: **Debug mode on** (kept); **Info: "App: 1.2.0, build ec6d163 (2026-10-09)"**, firmware `release_5.203` ×3, control channel 21, **"Serial numbers (from the Buds, 17:31:29): Case 5707…51, Right bud 5708…09, Left bud 5707…47"** and the note "Labelled by position …" | BH-2 · V1, V3, V16 · U1–U3 · `FW-003` | S s260–s329 |
| 17:33:3x | Connection (i): Left/Right/Case with times, "Both buds in the case", the full Worn explanation incl. the `CAP-064` sentence | BH-3 · V4 | S s368–s391 |
| 17:34:0x–17:34:2x | Dark mode **System** → app light at once → Off → **On** | BH-4 done differently (ends On) · U8 | S s397–s424 |
| 17:35:51–55 | **Left** taken out of the case (C), to the ear (head not in view); Buds `DISC` 17:35:53.075 → re-open ch **19** (only the Left out) | BH-5 · V5 · `INEAR-005` | A 8491, 8522–8616; C 17:35:51 |
| 17:35:56 | Worn **"Not worn (checked 17:35:56)"** — `Notify` Settable `00` (the bud just out, in the hand) | BH-5 · `INEAR-006` | A 8590; S s544 |
| 17:36:31 | pull on Connection → **"Probably worn (checked 17:36:31)"**, `Notify` `e8` mode TRN (the Buds changed OFF → TRN themselves on wearing) | BH-5 | A 8743–8773; S s545–546 |
| 17:37:03–13 | **Right** out of the case, **head in view** (C 17:37:09–13) — into an ear; Buds `DISC` 17:37:12.203 → re-open ch **21**; "—" then "Probably worn (17:37:15)" | BH-6 · `INEAR-006` | A 8934–9031; S s586–589; C |
| 17:37:26 | pull → "Probably worn (checked 17:37:26)" | BH-6 | A 9103; S s600–601 |
| 17:37:3x–17:38:0x | Settings and Info (serials again at 17:37:16 on ch 21) | BH-6/7 · V16 | S s607–s638 |
| 17:38:19–23 | **Right** into the case (from the ear); Buds `DISC` 17:38:19.078 → re-open ch **19** (only the Left out); "Probably worn (17:38:22)" (Left worn); a short tone on the audio at the seating | not in the plan; `INEAR-006`, `CASE-001` | A 9334–9418; S s653–657; C |
| 17:38:29 | **Left** into the case → both in; ACL `0x13` 17:38:29.17 ("Android no longer shows the Buds connected …", S s663) | BH-20 (early) · `CASE-004` | A 9492; E4 322–329 |
| 17:38:49–17:39:01 | Left out (17:38:49), ACL back by the Buds (A 9506), re-open ch 19 "**Not worn (checked 17:38:50)**" (`00`, Left in the hand); Right out 17:39:01; Buds `DISC` 17:39:05 → ch 21 "Probably worn (17:39:08)" | BH-20 · `CASE-005`, `INEAR-006` | A 9506–10132; E4 330–443; S s683–702 |
| 17:39:31, 17:40:31 | pulls → "Probably worn" (both worn, no music) | BH-6/7 | A 10266, 10531; S s723–785 |
| 17:40:49–17:41:09 | both buds laid **on the table** (C); Buds `DISC` 17:41:06.482 → re-open ch 19, `DISC` again 17:41:09.690 (1.25 s later: re-open skipped, ADR-044 guard) | BH-8 · `INEAR-004` | A 10627–10728; E4 473–515 |
| 17:41:28 | Connect tap → "**Not worn (checked 17:41:30)**" (`00`) | BH-8 · V7 · `INEAR-006` | A 10777–10842; S s842–844 |
| 17:41:57–17:43:26 | ANC tab: **seven mode taps refused**, "The Buds don't allow changing noise control right now …"; each claim `Get` → `Notify` `00` → no `Set`; the (i) "checked HH:MM:SS" follows each | BH-8 (×7) · V7 · `ANC-004` | E4 565–631; A 10980 … 11705; S s853–s968 |
| 17:44:03–13 | both buds from the table into the ears (C: hair at 17:44:13) | BH-10 | C |
| 17:44:32 | ANC tap **Noise cancellation** → `Get` (TRN, `e8`) → **`Set` 08** → ACK → `Notify` NC; "Probably worn (17:44:32)" | BH-10 · V8 · `ANC-001` | A 11915–11949; S s1025–1026 |
| 17:44:34–17:48:46 | **no Message Stream claim open** (DISC A 11947 … SABM A 12490); ANC tab on screen, (i) opened 4× (always "ACTIVE (updated 17:44:32)", no "Changed by the Buds"); AVRCP volume changes from the Buds 17:45:27, 17:45:55, 17:46:11, 17:46:30 and a PLAY 17:46:22 (gestures on a bud); speech 17:45:28–17:46:31, 17:48:01–11 — **the maintainer's press-and-holds** (the maintainer's statement; not visible on either film) | BH-11 done differently · V10 · `ANC-001`…`004` | A 11947, 12490, 12027–12132; S s1082–s1229 |
| 17:48:46 | ANC tap **Off** → `Get` (NC) → `Set` 20 → ACK; (i) "OFF (updated 17:48:46)" | BH-12 · V11 | A 12490–12524; S s1280–1284 |
| 17:49:12 | pull on the ANC tab → `Get` → `Notify` OFF; (i) "OFF (updated 17:49:12)" | BH-12 · V11 | A 12666; S s1305–1313 |
| 17:49:2x–17:50:0x | Quick Settings: edit tiles, **the ANC tile added** to user 10 (not in QS before) | not in the plan · J-tile | S s1326–s1352 |
| 17:50:03 / 17:50:23 | **tile** taps: OFF → Active (`Set` 08), Active → Transparent (`Set` 80); the app's (i) "updated 17:50:04 / 17:50:24", no line; notification "Connected — ANC: …" follows | BH-12 · V11 · `ANC-001`…`003` | E4 684–706; A 12845–13014; S s1354–1385 |
| 17:50:55–17:51:03 | **Right** then **Left** into the case → ACL `0x13` 17:51:03.25 (both in); short tone at the Right's seating | BH-13 (first half) · `CASE-001` | A 13098; C 17:50:57–17:51:03 |
| 17:51:08–10 | Android's link back (the Buds' ACL A 13104) → automatic re-open ch 21: `Get` → `Notify` OFF (`00`) → **unprovoked `Notify` TRN (`e8`) 63 ms later** while both buds are taken out of the case → the (i) shows **"Changed by the Buds at 17:51:10 (a press-and-hold on a bud, or the Buds' own change)."** | BH-11's line (seen once) · V10 | A 13484–13519; S s1424–1425; C 17:51:07–17 |
| 17:51:13–19 | both buds laid **on the table** (C) | BH-13 · `INEAR-006` | C 17:51:13–19 |
| 17:51:18 | Buds `DISC` → re-open skipped (8.9 s after the last, ADR-044) | BH-13 | A 13613; E4 776–781 |
| 17:51:40 / 17:52:00 | Connect tap, then a pull on the ANC tab → **"Probably worn (checked 17:52:00)"** — `Notify` `e8` TRN at 17:51:42.2 and 17:52:00.9 with **both buds on the table since 17:51:19** (23 s and 42 s) | BH-13 · V12 · `INEAR-006` | A 13721–13856; S s1454–1497; C |
| 17:54:1x–17:54:5x | Home; browser; internet radio started (music) | P7 ★ (late) | S s1612–s1662 |
| 17:55:09–17 | both buds from the table into the ears; AVRCP PAUSE/PLAY from the Buds (17:55:10.9 / 17:55:13.4 / 17:55:15.6) | BH-14 setup | C; A AVRCP |
| 17:55:15 | Buds `DISC` while the app is not visible (cause "while not visible") → re-open on return 17:55:26 (LINK_BACK) | K1/K2 | E4 853–908 |
| 17:55:50 | Settings: **Earbuds replaced OFF** → `4:{28:0}` → OK | BH-14 · V13 · `CASE-001` | A 14583/14585; E4 911; S s1704 |
| 17:56:05–09 | **Left** out of the ear (PAUSE 17:56:04.98) → Buds `DISC` 17:56:05.370 → into the case 17:56:08 → re-open **ch 21** | BH-14, BH-7-like (L-1) · `INEAR-005`, `CASE-001` | A 14616, 14638; C |
| 17:56:15–17 | Left out of the case, into the ear → Buds `DISC` 17:56:17.640 → ch 19 | BH-14 | A 14794, 14827 |
| 17:56:31–41 | **Right** out of the ear (PAUSE 17:56:31.69), into the case 17:56:36, out 17:56:41 | BH-14 | C; A |
| 17:56:50 | **Earbuds replaced ON** → `4:{28:1}` → OK | BH-14 · V13 | A 15020/15026; E4 1049; S s1764 |
| 17:57:00–05 | **Left** out of the ear → Buds `DISC` 17:57:00.616 → into the case 17:57:04 → re-open **ch 21**; Left out 17:57:13, in again 17:57:20, out 17:57:23 | BH-14 | A 15067, 15090; C |
| 17:57:27–29 | Buds `DISC` (Left back in the ear) → ch 19 | BH-14 | A 15269, 15291 |
| 17:57:37–41 | **Right** into the case 17:57:38 (a short tone on the audio at seating), out 17:57:41 | BH-14 | C; S audio |
| 17:58:06–08 | Left out of the ear → `DISC` 17:58:06.155 → into the case 17:58:08 → **ch 21**; (i) "Earbuds replaced: read 17:58:10" | BH-14 | A 15457, 15489; S s1847 |
| 17:58:21–27 | Left out → `DISC` 17:58:24.755 → ch 19 | BH-14 | A 15690, 15714 |
| 17:58:45–53 | **Right** into the case 17:58:46 (short tone), out 17:58:52 | BH-14 | C; audio |
| 17:59:02 | **Earbuds replaced OFF** → `4:{28:0}` → OK | BH-14 (repeated) | A 15945/15952; E4 1308; S s1896 |
| 17:59:07–09 | **Left** out of the ear → `DISC` 17:59:06.992 → into the case → **ch 21** | BH-14 | A 15987, 16015 |
| 17:59:17–23 | **Right** into the case (both in) → ACL `0x13` 17:59:23.71; Right out 17:59:23, Left out 17:59:29 → ACL back, re-open ch 21, `DISC` 17:59:32.662 (skipped, 2.9 s) | BH-14, BH-20 | A 16141–16643; E4 1370–1444 |
| 17:59:5x | tap on the dimmed Earbuds-replaced switch (disabled; nothing sent); (i) "From the last connection …" | not in the plan · ADR-057 | S s1938–1967 |
| 18:00:06–07 / 18:00:16 | Left into the case 18:00:06; **Connect** tap 18:00:16.97 → ch 21 "Probably worn (18:00:17)" (Right worn) | BH-14 | A 16864; S s1969–1971 |
| 18:00:23–18:01:23 | Left out 18:00:24 (`DISC` 18:00:29.59 → ch 19), Left in 18:00:44, out 18:00:50 (`DISC` 18:00:41.48, skipped); Right in 18:01:02, out 18:01:22 | BH-14 | A 17040–17171; C |
| 18:01:47 | Connect tap → ch 19, "Probably worn (18:01:48)" | BH-14 | A 17373; S s2061–2062 |
| 18:01:53 | **Earbuds replaced ON** → `4:{28:1}` → OK | BH-14 · V13 | A 17479/17481 |
| 18:02:03 / 18:02:17 | **Other alerts OFF / ON** → `4:{27:0}` / `{27:1}` → OK | BH-14 · V13 · `CASE-002` | A 17519–17611 |
| 18:02:09–21 | the lid closed and opened ≈ 3 times with no bud inside | not in the plan | C |
| 18:02:25 | **incoming call** (RINGING), answered 18:02:31 (`ANSWERED → ACTIVE`), audio on the Buds over eSCO (handle `0x0006`, Synchronous Connection Complete A 17689 18:02:26.903); Right bud into the case 18:02:28 and out 18:02:37 during the ring | not in the plan · P7 ★ failed | X 27123–27377; A 17689 |
| 18:02:31–18:04:59 | the call (2 min 28 s); the app's session (ch 19 since 18:01:48) stays open, no claim, no write; ends `DISCONNECTED` 18:04:59.03; eSCO disconnect `0x16` 18:05:01.552 | — | A 18634; system log 18:04:59 |
| 18:05:10 / 18:05:17 | **Other alerts OFF / ON** again → OK; (i) "Earbuds replaced: changed 18:01:53 / Other alerts: changed 18:05:10" | BH-14 (repeated) | A 18708–18763; S s2261–2271 |
| 18:05:4x | music restarted in the browser | — | S s2294–2299 |
| 18:06:08 / 18:06:20 | Controls: **Conversation detection OFF / ON** → `4:{22:0}` / `{22:1}` → OK; AVRCP PAUSE/PLAY 18:06:22–47 | BH-15 · V14 · `CONV-001` | A 18921–18947; S s2321–2334 |
| 18:06:35 | ANC tap **Noise cancellation** (from TRN) → `Set` 08 → ACK (during music) | BH-10/16 · `ANC-001` | A 19008–19043; S s2348–2349 |
| 18:07:04 | Sound: preset **Balanced** → `4:{16:[-3.5,0.5,1.0,-1.0,2.5]}` → OK | BH-16 · V15 · `EQP-001` | A 19113/19115; S s2378 |
| 18:07:14 / 18:07:21 | Upper treble dragged: writes `…4.66` and `…6.0` → OK; (i) "EQ updated: 18:07:21" | BH-16 · `EQS-001` | A 19133–19181; S s2387–2401 |
| 18:07:49–18:08:57 | **balance**: 30 writes (`17:11, 0, 18, 0, 0, 15, 0, 0, 15, 0, 0, 0, 25, 105, 15, 0, 9, 0, 18, 18, 20, 0, 26, 20, 9, 24, 21, 0, 13, 0` — zigzag), labels on screen incl. "Right 6", "Left 9", "Right 53", "Right 8"; ends **Centre** (`17:0`) | BH-16 done differently · `AUDIO-003` | A 19287–19510; E4 1672–1765; S s2416–s2483 |
| 18:09:03 / 18:09:06 | **Mono ON / OFF** → OK | BH-16 · `AUDIO-001` | A 19520, 19529 |
| — | **Volume EQ** off/on and **Read EQ again** (H5) — not done | BH-16 partly skipped | no `4:{15:…}` write, no `ReadSetting 4:16` between connects |
| 18:09:24 / 18:09:29 | **Use touch controls OFF / ON** → OK | BH-17 · `HOLD-005` | A 19573, 19583 |
| 18:09:33 | mode list: **Adaptive ticked** → `4:{12:{1:1 2:0 3:1 4:1}}` → OK | BH-17 done differently | A 19595 |
| 18:09:45–18:10:01 | press and hold: **Left** Digital assistant → Noise control, **Right** Digital assistant → Noise control → OK ×4 | BH-17 (both buds) · `HOLD-005` | A 19622–19659 |
| 18:10:11 / 18:10:13 | mode list: Off box tapped twice (`4:{12:…}` ×2) → ends `{1:1 2:0 3:1 4:1}` (Off unticked, Adaptive ticked) | BH-17 | A 19682, 19690 |
| 18:10:27 / 18:10:32 | **head gestures OFF / ON** → `29:1` / `29:2` → OK | BH-17 · `HEAD-001` | A 19716, 19726 |
| 18:10:52 / 18:10:58 | **Multipoint OFF / ON** → OK | BH-17 · `MULTI-001` | A 19764, 19779 |
| 18:11:18 / 18:11:19 | **Disconnect, Connect** → ch 19; every value read back; Info: build `ec6d163`, firmware 18:11:20, **serials 18:11:20 identical** | BH-18 · V16, V17 · `PAIR-003`, `FW-003` | A 19816–19942; S s2631–s2659 |
| 18:12:04–11 | both buds from the ears onto the table; Buds `DISC` 18:12:04.334 → ch 21 (Info "Serial numbers: Not read from the Buds yet" for < 1 s, then 18:12:07) → `DISC` 18:12:09.737 (skipped) | BH-19 setup · V16 | A 20019–20160; S s2680–2691 |
| 18:12:22 | re-open (a tap) ch 19, `Notify` `00` | BH-19 | A 20217–20348 |
| 18:12:26 / 18:12:45 / 18:12:46 / 18:13:09 | **Ring Left** → ACK; **Stop** → ACK; **Ring Right** → ACK; **Stop** → ACK; ring on the audio both times | BH-19 · `FIND-001`, `FIND-002` | A 20379–20683; S s2699–2744 |
| 18:13:13–17 | both buds from the table into the case → ACL `0x13` 18:13:15.94 | BH-20 · C8 · `CASE-004` | A 20717; C |
| 18:13:31–37 | both taken out onto the table; the Buds' ACL back 18:13:34.9 → re-open ch 21 "**Not worn (checked 18:13:37)**" (`00`) → `DISC` 18:13:38.134 (skipped, 1.95 s) | BH-20 · C9 · `CASE-005` | A 20735–21181; S s2769–2772 |
| 18:13:44–18:15:20 | **Home** for 1 min 36 s (link observer stopped/started) → re-open on return ch 19 "Not worn (18:15:21)" | BH-21 · J4, K1, K2 | E4 2067–2119; S s2777–s2875 |
| 18:15:3x | **Export 1** (2,121 lines) → E1 | BH-22 export | E4 2125; S s2886–2891 |
| 18:16:0x–18:18:18 | bugreport 1 (pulls `…hci1.log`); SIGQUIT to 12137 at 18:16:33 | P6 note | X; L |
| 18:18:32 | **Bluetooth off** (Quick Settings): "Bluetooth adapter: ON -> TURNING_OFF", "Session loss cause: Bluetooth was switched off on this phone" | BH-22 · V18 · `PAIR-003` | E4 2135–2141; A 21841–21859; S s3061–3080 |
| 18:18:45 | **Bluetooth on** → the Buds' link 18:18:47.3 → automatic re-open ch 19, thirteen requests, serials 18:18:48 | BH-22 | E4 2142–2197; B 159–546 |
| 18:19:01 | **Export 2** (2,197 lines) → E2 | BH-end1 export (early) | E4 2201; S s3082–3098 |
| 18:19:35–41 | both buds from the table to the ears → Buds `DISC` 18:19:37.17 → ch 21: `Get` → `Notify` TRN, **unprovoked `Notify` NC 18:19:41.644**, Buds `DISC` 3 ms later (skipped) | not in the plan | B 703–829 |
| 18:20:03 | Connect tap → ch 19 | — | B 864 |
| 18:20:09 / 18:20:13 | **In-ear detection OFF / ON** (Controls; buds worn; no pull, the Worn line not read) | BH-9 done differently · `INEAR-004` | B 980, 983; S s3161–3169 |
| 18:20:44–18:20:49 | phone **rotated** (landscape) on the Debug tab and back (two Activity re-creations) | S12 part · K4 | E4 2322–2331; S s3198 |
| 18:21:10 | **Export 3** (2,335 lines) → E3 (portrait, no rotation during the save dialog) | BH-24 done differently · S12 | E4 2340; S s3203–3226 |
| 18:21:31–18:23:36 | bugreport 2 (pulls B); SIGQUIT 18:21:56 | P6 note | L 648 |
| 18:23:3x | **Export 4** (2,346 lines) → E4; notification "Connected — ANC: ACTIVE" | BH-end1 | S s3370–3375 |
| 18:23:39 / 18:23:42 | camera film ends / screen recording ends | BH-end1 | C, S |

## Step mapping

| Step | Result | Note |
|---|---|---|
| P0 | done | positive control and `am get-current-user` output missing from the file; the commands ran (system log) |
| P1 | done differently | 1.1.1 freshly reinstalled at 17:24 after an uninstall of the release APK; Dark mode On and Debug mode on set in the fresh 1.1.1 **on film** |
| P2 | skipped | no Bluetooth off/on at the start (A has no rotation 15:40:29 → 18:18:33) |
| P3 | done differently | camera films case and phone, not the head; its audio is empty |
| P4 ★ | done | the screen recording |
| P5 | done | clocks measured from shared events (above) |
| P6 ★ | done | started 17:26:16, before the install |
| P7 ★ | partly | music from 17:54 (internet radio); **Do Not Disturb not on** (the call came through; Quick Settings "Modi" tile inactive on S 17:49) |
| P8 ★ | done | lid open, Buds connected before 1.2.0 started |
| P9 ★ | not kept | the release APK used today before the run (Z, A 5589) and 1.1.1 opened at 17:25 before the film |
| BH-1 | done differently | session by itself, thirteen requests; Controls not opened within the first second (S6 caught at 17:31:28 instead) |
| BH-2 | done | build `ec6d163`, serials, Case sounds card and its (i), Debug on, Dark On |
| BH-3 | done | "Both buds in the case" |
| BH-4 | done differently | System → light at once, then Off, then **On** (not left on System) |
| BH-5 | done | only the Left out ⇒ ch 19; "Not worn" (bud in hand) then "Probably worn" after a pull |
| BH-6 | done | Right in, head in view, "Probably worn" |
| BH-7 | done differently (repeated ×4 in BH-14) | the Left out of the ear with both worn on 19 ⇒ Buds `DISC` + 21 at 17:56:05, 17:57:00, 17:58:06, 17:59:07 (head not in view; the ear removal by the Buds' PAUSE) |
| BH-8 | done (repeated ×7) | "Not worn"; seven refused ANC taps |
| BH-9 | done differently | in-ear detection off/on at 18:20 with buds worn; the Worn line not read |
| BH-10 | done differently | one ANC tap at 17:44:32 (Noise cancellation) and one at 18:06:35; the four-mode tap sequence not done |
| BH-11 | done differently | press-and-holds (the maintainer's statement) at 17:44:34–17:48:46 with no claim open; the (i) never changed; the line appeared once at 17:51:10 for a change by the Buds on taking the buds out of the case |
| BH-12 | done | tap (17:48:46), pull (17:49:12), tile ×2 (17:50:03/23): no line |
| BH-13 | done | both from the case to the table; "Probably worn" at 23 s and 42 s |
| BH-14 | done (extended) | Earbuds replaced OFF/ON/OFF/ON with 13 bud-into-case moments; Other alerts OFF/ON twice |
| BH-15 | done | conversation detection off/on; nothing said on film about it (speech present, not transcribed) |
| BH-16 | partly | Balanced, Upper treble, balance (≈ 30 writes), mono; **Volume EQ and Read EQ again (H5) not done**; balance ended Centre |
| BH-17 | done differently | touch, holds on both buds, mode list ends **with Adaptive** (not "without Off" only), head gestures, Multipoint |
| BH-18 | done | read-back identical; serials identical |
| BH-19 | done | Ring Left, Stop, Ring Right, Stop |
| BH-20 | done (×3) | 17:38:29, 17:51:03, 18:13:15 both into the case; re-open by itself when Android's link came back |
| BH-21 | done | Home 1 min 36 s (not 2 min) |
| BH-22 | done | export before off, off/on, re-open by itself |
| BH-end1 | done differently | exports at 18:19:01 and 18:23:3x; the minute change at the end on S (18:23) |
| BH-23 | not identifiable | no write was refused with "being reopened"; no tap during a re-open is on film |
| BH-24 | done differently | rotation at 18:20:44–49 before the export, not during the save dialog |
| BH-25, BH-26 | **not made** | folded into the rebuilt 1.2.0's run (the maintainer, chat 2026-10-10) |

**Not in the plan:** the pre-run use of the release APK (Z/A); the Disconnect/Connect at 17:31; the many extra case/ear moves (20 bud-into-case moments); the
ANC tile added to QS; the internet radio; the lid closed/opened at 18:02:09–21; the incoming call; the tap on a dimmed switch (17:59:5x); the second Other-alerts
pair (18:05); the unprovoked `Notify` at 18:19:41 when the buds were put in.

**Test-IDs (`AGENTS.md` §13.7):** `PAIR-003` (17:29:26, 18:11:19, 17:38:49, 18:13:34, 18:18:47), `BATT-004` (17:29:27), `FW-003` (17:31:5x, 18:11:2x),
`INEAR-006` (25 readings, FINDINGS §4), `INEAR-005` (17:35:55, 17:38:21, 17:38:50, 17:56:07 … 18:00:17), `INEAR-004` (17:41:06–17:43:26, 18:20:09/13),
`ANC-001` (17:44:32, 18:06:35), `ANC-002`/`ANC-003` (17:48:46, 17:50:03/23 via tap and tile), `ANC-004` (17:41:57–17:43:26), `CASE-001` (17:55:50–18:01:53),
`CASE-002` (18:02:03/17, 18:05:10/17), `CONV-001` (18:06:08/20), `EQP-001` (18:07:04), `EQS-001` (18:07:14/21), `AUDIO-001` (18:09:03/06), `AUDIO-002`
(**expected but not observed** — no Volume EQ write), `AUDIO-003` (18:07:49–18:08:57), `HOLD-005` (18:09:24–18:10:14), `HEAD-001` (18:10:27/32), `MULTI-001`
(18:10:52/58), `CASE-004` (17:38:29, 17:51:03, 17:59:23, 18:13:15), `CASE-005` (17:38:49, 17:51:08, 17:59:29, 18:13:34), `FIND-001`/`FIND-002` (18:12:26–18:13:10).
APP_TESTPLAN V1–V18 are mapped in FINDINGS §10; V19 (C12, S12, T11) is partly done (S12 differently); T11 moves to the rebuilt 1.2.0's run.

## Analysis checklist

- [x] Every filter scoped to handle `0x000b` (Connection Complete A 5491, 9506, 13104, 16151, 20735, B 159 for `04:00:6e:…:07`).
- [x] P0/P1, the update, the build on film, the APK checksums (FINDINGS §0).
- [x] The release APK against 1.1.1's (`aapt2 dump badging` / `xmltree`): the same five `uses-permission` lines (no `INTERNET`), `targetSdkVersion:'34'`, `compileSdkVersion='37'`, the same two `uses-library-not-required`, 5 components in both (FINDINGS §0).
- [x] Zero Play-services claims (FINDINGS §1).
- [x] I–IV per FINDINGS §2–§7; V: S12 differently, C12 not identifiable, T11 moved to the rebuilt 1.2.0's run.
- [x] The system log: no crash, ANR or `StrictMode`; two `bugreportz` SIGQUITs.
- [x] Step mapping and traceability (above).

## Appendix — the planned procedure (the skeleton as committed by `ai-sessions/0082`, unchanged)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0082`, 2026-10-09). Scope chosen by the maintainer in chat 2026-10-09 (the
checkpoint of `ai-sessions/0082`): the **1.2.0 release run** — the three new things to see (the "Changed by the Buds" line in the noise-control (i), the
component serial numbers on the Info tab per `DECISIONS.md` ADR-058, the "probably worn" line on the battery card per ADR-059), the two moved switches
(Case sounds on gear → Settings, Conversation detection on Controls), every write once byte for byte, and — for the third time — the open film items of
`TODO.md` §2 (lead L-1 with the head in view, C12, S12, T11, H5, S6, the case-sound and Volume-EQ observations). **Installed over 1.1.1** (P1 records both
`dumpsys` times). The maintainer films; a later CAPTURE session analyses it and gives the release verdict. After the run: rename this folder from the
placeholder to the film's first/last overlay times, and the film to CAP-072-recording.mp4.

**Purpose.** 1.2.0 adds **one** request per connection (`GetHardwareInfo`, ADR-058) and nothing else on the wire; everything else is derived from frames
the app already received, or a control in a new place sending the same bytes. This run checks on the phone:

- **I — the update and the start.** 1.2.0 over 1.1.1 in user 10 (the second update test); the stored choices read back; the Connect-time traffic now
  **thirteen** MAESTRO requests plus the EQ read and the subscription — the twelve settings reads, `SubscribeRuntimeInfo`, then `GetHardwareInfo` last — each
  equal to its reference; "—" in the first second (S6); the Info tab's three serial lines (ADR-058; the serials are **not** written into these notes or the
  findings unredacted: first 4 + last 2 characters only).
- **II — the worn line and "Changed by the Buds", head in view.** ADR-059's six readings against the ears on film (`INEAR-006`), including the ≈ 28 s case of
  `CAP-064` (buds straight from the case to the table, watched for 60 s); lead L-1 (`INEAR-005`) in the same sequence; a press-and-hold on a bud with the
  app open → the (i) line; a tap in the app, a pull, the tile → no line.
- **III — every write once, byte for byte** (the codec is unchanged; the moved switches send what they sent from their old place): the two case sounds from
  gear → Settings with the sound observation said aloud, conversation detection from Controls, and the rest as in `CAP-071`; the read-back after a reconnect
  and the serial lines again.
- **IV — robustness:** the case (ADR-044), Home for two minutes, Bluetooth off/on.
- **V — the open film items of `TODO.md` §2** (film 2): C12 (a tap during a re-open), S12 (an export across a rotation), T11 (the screen-reader text from
  three `uiautomator` dumps — the Settings tab's two switches are new).

The run is two films in this folder: **film 1** = P0–P9 and sections I–IV (≈ 35 min), **film 2** = section V (≈ 10 min). If time is short, film 2 can be
another day — the build stays the same; say so in the notes. (`CAP-071` had no film 2; `ai-sessions/0082` RESULT §I asks the maintainer whether to split.)

#### Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-072` |
| Group(s) | BH (new) |
| Date | TBD |
| Phone | Pixel 9a, GrapheneOS (Android 17 in `CAP-071`) — the secondary user without Google Play (user 10 in `CAP-071`; P0) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.2.0** (versionCode 10200), the release APK built by `scripts/release.sh` and kept as `RELEASING.md` B3 says — on the **Info tab on film**: "App: 1.2.0, build <hash> (<date>)", no "-dirty"; APK and certificate SHA-256 from the script's output |
| Previous version in this user | 1.1.1 (`86a6fb3`), installed 2026-10-08 for `CAP-071` — the update is from it |
| Official Pixel Buds app | Not used (not in this user). Pixel 7a: Bluetooth off |
| Buds | Pixel Buds Pro 2; firmware from the Info tab on film (`release_5.203` expected; any other ⇒ Safe Mode — the serial read still runs (ADR-058 item 2), say so and continue; the writes are blocked, so stop section III) |
| Video files | TBD — film 1 and film 2, **with sound** (P3) |
| Screen recording | TBD — Android's screen recorder in the test user, film 1 and film 2 (P4) |
| Log files | TBD — CAP-072-btsnoop_hci.log and .log.last (Bluetooth is toggled in P2, IV and V), the app's debug exports, the app logcat, the **system log** (P6), the three `uiautomator` dumps (BH-23), the P0/P1 outputs |

#### Preparation

What `CAP-071` missed is marked ★ — please check those twice.

| # | Check | Done |
|---|---|---|
| P0 | In the test user: `adb shell am get-current-user`; `adb shell pm list packages --user <id> \| grep -i -E "gms\|vending"; echo "exit=$?"`; positive control: the same with `grep opencontrol` (exit 0). Save the outputs **with the exit statuses** into this folder | ☐ |
| P1 | **Before the update, in 1.1.1** (on film): gear → **Settings → Dark mode On**; **Debug → Debug mode on**; Info shows "App: 1.1.1, build 86a6fb3". Then `adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds \| grep -E "firstInstallTime\|lastUpdateTime\|versionCode"` → save. **Install 1.2.0 over it**: `adb install --user <id> -r dist/1.2.0/opencontrol-pixelbudspro2-1.2.0.apk` — no uninstall, no "clear data". The same `dumpsys` again → save (**both times recorded**: `firstInstallTime` unchanged, `lastUpdateTime` later, `versionCode=10200`) | ☐ |
| P2 | Bluetooth HCI snoop log on (set in the Owner, then switch to the test user); switch Bluetooth **off and on on film** | ☐ |
| P3 | Camera films the phone, the case **and your head with both ears** at every wear step (head on the right of the frame = Left bud); **the camera records sound** — play back 3 s before starting | ☐ |
| P4 | Android's **screen recorder** on (Quick Settings) ★ (`CAP-071` had none — S6, C12 and the (i) texts need it) | ☐ |
| P5 | Status bar on film across a minute change at the start and at the end of each film | ☐ |
| P6 | **System log**: on the computer, before P2, `adb logcat -b all -v threadtime > CAP-072-logcat-all.txt` (stop it with Ctrl-C after the last export). Note: every `adb bugreportz` (the way the HCI logs are pulled) makes dumpstate send signal 3 to every Java process — "Signal Catcher … reacting to signal 3" once per pull is not an app fault (`CAP-071-FINDINGS.md` §0) | ☐ |
| P7 | A short music track ready at **low volume** for the sound observations (BH-12, BH-14, BH-15); **Do Not Disturb on** ★ (an incoming call put a third party's number on `CAP-071`'s film) | ☐ |
| P8 | Buds charged, both in the case, **lid open** ★ (`CAP-071` started with the lid closed: two failed Connects); Android shows them connected before the app is opened (the app then opens its session by itself, ADR-044) | ☐ |
| P9 | **No rehearsal before the film** ★ (`CAP-071` had an off-film Connect) — the app is not opened between P1's install and BH-1 | ☐ |

**Rhythm:** one action, then wait 5–10 s; **say** what you do at each wear change and at each observation (the film has sound). Something unexpected: stop,
wait 10 s, say it, continue. **Export the debug log before any step that ends the process and after every Bluetooth off/on.** The worn line and the
"Changed by the Buds" line follow the Buds' **last `Notify`** — after every wear change in section II, **pull down on the Connection tab** (one `08 11` claim)
so the app asks the Buds again; it never asks by itself. Before committing: check the films for a street-address overlay, Wi-Fi names and other device
names (`CAP-070` privacy note), and **redact the serial numbers** on any screenshot or in any text (first 4 + last 2 characters).

#### Steps

DLCIs are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05). Every MAESTRO request starts `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25` on
**channel 21** or `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25` on **channel 19** (Info → "Control channel"). **Reference frames** — the 1.1.1 build's own requests
in `CAP-071` unless named; the new request's references are the official app's (`ai-sessions/0082` RESULT §A.2/§D):

| Request | Channel 21 | Channel 19 |
|---|---|---|
| `ReadSetting 4:16`, then 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29 | `CAP-071` A (the 1.1.1 run, byte-identical to `CAP-070` A271, A296 … A329) | `CAP-070` A966, A979 … A1035 |
| `SubscribeRuntimeInfo` | `CAP-070` A332 `…90821ee66654bfab7e` | `CAP-070` A1038 `…90821ee6602d65a97e` |
| **`GetHardwareInfo` (new, ADR-058; last of the Connect-time requests)** | `CAP-036` 1415 `7e004b0310151dea71de7d5e25e3a5ec28f96761b57e` | `CAP-024` 801 `7e003b0310131dea71de7d5e25e3a5ec28ff1ebbb77e` |
| its answer (`CAP-036` 1423 / `CAP-024` 832): `RESPONSE` with `7:{1:<14 chars> 2:<14 chars> 3:<14 chars>}` | the three strings in the order Case, Right, Left | the same |
| Conversation detection `4:{22:0}` / `{22:1}` (now from Controls) | `CAP-019` 1720 `…2a052203b00100225fc3b77e` / 1808 `…2a052203b00101b46fc4c07e` | `CAP-071`'s 1.1.1 request on 19 if it ran there, else the codec's output (unit tests) |
| Earbuds replaced `4:{28:0}` / `{28:1}` (now from gear → Settings) | `CAP-070` A770 `…2a052203e0010092717fdb7e` / `CAP-058` 5643 `…2a052203e00101044178ac7e` | `CAP-024` 1988 `…2a052203e00100d2b7cefd7e` / `CAP-070` A1992 `…2a052203e001014487c98a7e` |
| Other alerts `4:{27:0}` / `{27:1}` (now from gear → Settings) | `CAP-070` A777 `…2a052203d80100bac507f17e` / `CAP-058` 5697 `…2a052203d801012cf500867e` | `CAP-024` 2053 `…2a052203d80100fa03b6d77e` / `CAP-070` A1995 `…2a052203d801016c33b1a07e` |
| Multipoint `4:{11:0}` / `{11:1}` | `CAP-070` A752 / `CAP-069` 3212 | `CAP-070` A3668 / A3679 |
| Head gestures `4:{29:1}` / `{29:2}` | `CAP-070` A762 / `CAP-069` 2564 | `CAP-070` A3614 / A3685 |
| Volume EQ `4:{15:0}` / `{15:1}` | `CAP-070` A785 / A4800 | `CAP-070` A3072 / A1953 |
| Balance Right 4 `4:{17:7}` | `CAP-064` 6671 | `CAP-070` A3747 |
| EQ preset Balanced `4:{16:[-3.5, 0.5, 1.0, -1.0, 2.5]}` | `CAP-059` 2188 | no captured frame — the codec's output (unit tests) |
| Mode list without Off `4:{12:{1:1 2:0 3:1 4:0}}` | `CAP-041` 2198 | no captured frame — the codec's output (unit tests) |
| ANC `Get` (each claim: a tap, a pull on Connection/ANC/Find, the tile) | `08 11 00 00` | |
| ANC `Notify` (the Buds' answer, and **unprovoked** after a press-and-hold) | `08 13 00 04 01 e8 e8 xx` — `xx` = `08` Active, `40` Adaptive, `80` Transparent, `20` Off; Settable `00` instead of the second `e8` when no bud is worn (`CAP-045` 612/1583/1755/1849) | |
| ANC `Set` Transparent / Off / Active / Adaptive | `CAP-068-btsnoop_hci.log` 1039 `08 12 00 14 01 e8 e8 80` + 16 × `00`; 1090 `…20…`; 1146 `…08…`; 1266 `…40…` | |
| Ring Left / Stop / Right | `CAP-068-btsnoop_hci2.log` 2142 `04 01 00 01 02`, 2416 `04 01 00 01 00`; `CAP-062-btsnoop_hci.log` 9660 `04 01 00 01 01` | |

A request with no row (touch controls, press and hold, in-ear detection, mono, a slider value) is compared with the app's unit-test fixture for the same
request (`android/data/src/test`). **Any difference is the finding.** Nothing may appear on DLCI 0x08/0x0a and no MAESTRO request other than those listed
(the wire discipline of `ai-sessions/0082` §5).

##### I. The update and the start (film 1; `APP_TESTPLAN.md` V1–V4, U1–U3, S6; `PAIR-003`, `BATT-004`, `FW-003`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BH-1 | P0–P9 done, 1.2.0 just installed over 1.1.1; lid open; screen recording on | open the app; within the first second go to **Controls** | **dark** (the 1.1.1 choice kept); no crash; the session opens by itself; for ≈ 1 s the unread switches show "—", then the Buds' values (S6, screen recording); Controls shows **Conversation detection** as its own card after In-ear detection and **no** Case sounds card | DLCI 0x02 open; the announcement; `ReadSetting 4:16`, then twelve reads in the order of the table, `SubscribeRuntimeInfo`, then **one `GetHardwareInfo`** = its reference for the announced channel, answered with `RESPONSE` field 7; no retry; nothing else | `PAIR-003`, `BATT-004`, `FW-003` | a crash; light theme; a read missing or out of order; a second `GetHardwareInfo`; any other request |
| BH-2 | ready | gear → **Settings**: Dark mode shows **On**; the **Case sounds** card (Earbuds replaced, Other alerts with their values) between Dark mode and "Use different Buds"; open its (i); then **Debug** (Debug mode **on**); then **Info**: hold 3 s on the build line, on "Control channel" and on the **Serial numbers** block | the two 1.1.1 choices kept; the (i) ends with "These settings live on the case and are read when the app connects."; "App: 1.2.0, build <hash> (<date>)", no "-dirty"; firmware `release_5.203` ×3; channel 19 or 21 — say it; **"Serial numbers (from the Buds, HH:MM:SS):" then "Case: …", "Right bud: …", "Left bud: …"** and the note "Labelled by position …" | nothing sent | `FW-003` | either choice lost; "-dirty"; "Serial numbers: Not read from the Buds yet" or "not read — …" while the answer is in the log; a label order other than Case, Right bud, Left bud |
| BH-3 | ready, both buds in the case, lid open | **Connection** tab: read the battery card's **Worn** line; open the (i) | **"Both buds in the case"** (both charging flags from the runtime-info stream); the (i) has the "Worn: …" explanation ending with the `CAP-064` sentence | the runtime-info `6.2`/`6.3` with field 2 = 2 for both buds already received | `INEAR-006` | "Probably worn" with both buds in the case |
| BH-4 | ready | gear → Settings → Dark mode **System** (leave it there) | the app follows Android's theme at once | — | — | — |

##### II. The worn line and "Changed by the Buds", head in view (film 1; `APP_TESTPLAN.md` V5–V12, F1–F4, G3, N11; `INEAR-006`, `INEAR-005`, `INEAR-004`, `ANC-001`…`ANC-004`)

Head and both ears in view at every step; say every wear change. After each wear change **pull down on the Connection tab** before reading the Worn
line. Note the Worn line's "(checked HH:MM:SS)" each time — it must move with each pull.

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BH-5 | both buds in the case | take **only the Left** out, into the left ear; wait 10 s; Info → "Control channel" (say it); pull on Connection; read Worn | the session re-opens by itself if the channel moved; **"Probably worn (checked …)"** | the announcement (`10 13` ⇒ 19 expected with only the Left out, `PROTOCOL.md` §2.2a); the thirteen requests again on the new channel incl. one `GetHardwareInfo`; the pull: `08 11` → `Notify` with Settable `e8` | `INEAR-005`, `INEAR-006` | "Not worn" with a bud in an ear |
| BH-6 | Left worn, channel 19 | the **Right** into the right ear; wait 10 s; Info; pull on Connection; read Worn | "Probably worn (checked …)" with a later time | `Notify` Settable `e8` | `INEAR-006` | — |
| BH-7 | both worn on 19 (if the channel is already 21, say so, put both back in the case 10 s, retry BH-5 once) | take the **Left** out of the ear and **hold it in view**; say "Left out"; wait 10 s; Info; pull on Connection | the session closes and re-opens by itself; Info shows the new channel; Worn stays "Probably worn" (the Right is in) | 🟡 lead L-1 (`PROTOCOL.md` §2.2a Updates of 2026-10-01/03/07): Buds `DISC` of MAESTRO with the ACL up, then an announcement `10 15` (21), the thirteen requests | `INEAR-005`, `INEAR-004`, `INEAR-006` | no `DISC`, or `DISC` followed by 19 again |
| BH-8 | Right worn, Left on the table | take the **Right** out, both buds **on the table**, in-ear detection on; wait 10 s; pull on Connection; read Worn; then **ANC tab: tap Transparent** | **"Not worn (checked …)"**; the ANC tap is refused with the 1.0.x not-allowed text, the mode unchanged | `Notify` with Settable **`00`** (`CAP-045` 1849's form); the tap: `08 11` → `Notify` `00` → **no `Set`** | `INEAR-006`, `INEAR-004`, `ANC-004` | "Probably worn" with both buds on the table beyond the first ≈ 30 s (see BH-11), or a `Set` sent |
| BH-9 | both on the table | Controls → **In-ear detection off**; pull on Connection; read Worn; **In-ear detection on** again; pull; read | after the OK: **"Worn: unknown — in-ear detection is off"**; after the second OK: "Not worn (checked …)" again | `4:{2:0}` → OK; `08 11` → `Notify` (Settable `e8` expected with in-ear off, `CAP-064` 10394 — the byte says nothing); `4:{2:1}` → OK; `08 11` → `Notify` `00` | `INEAR-006`, `INEAR-004` | "Probably worn" shown while in-ear detection is off |
| BH-10 | both on the table, in-ear on | both buds **into the ears**; wait 10 s; pull on Connection; **ANC tab: tap Transparent, Adaptive, Off, Active**, 8 s apart; after each, open the ANC (i) | "Probably worn (checked …)"; each mode after its ACK; the (i) says "Noise control: set HH:MM:SS …" and has **no** "Changed by the Buds" line | per tap `08 11` → `Notify` `e8` → the `Set` = the reference → ACK `ff 01` | `ANC-004`, `ANC-003`, `ANC-001`, `ANC-002`, `INEAR-006` | the line shown after an app tap; a `Set` that differs |
| BH-11 | both worn, ANC on Active, the ANC tab on screen with its (i) **closed** | **press and hold the Left bud** (its noise-control cycle); say it; wait 5 s; open the ANC (i); close; press and hold again; (i) again | the mode changes **without a tap**; the (i) shows **"Changed by the Buds at HH:MM:SS (a press-and-hold on a bud, or the Buds' own change)."** with the time of the `Notify`; the second time the line's time moves | an **unprovoked** `08 13 00 04 01 e8 e8 xx` from the Buds — no `08 11` before it; **nothing sent by the app** | `ANC-001`…`ANC-004` | no mode change on screen (the Buds' `Notify` not applied); the line missing; the app sending anything in reaction |
| BH-12 | the line shown | **tap** the shown mode's neighbour in the app; (i); then **pull down on the ANC tab**; (i); then the **ANC tile** once; (i) | after the tap the line is **gone** ("set HH:MM:SS"); after the pull no line ("read HH:MM:SS"); after the tile no line | the tap: `08 11` → `Notify` → `Set` → ACK; the pull: `08 11` → `Notify`; the tile: a claim as the tap | `ANC-001`…`ANC-004` | the line still shown after the app's own tap, pull or tile |
| BH-13 | both worn | both buds **straight from the ears into the case** (lid open), then **straight out onto the table**, say "on the table" and start counting; pull on Connection at ≈ 5, 15, 30, 45 and 60 s; read Worn each time | the `CAP-064` case: **"Probably worn"** may show for the first ≈ 30 s, then **"Not worn"**; or "Both buds in the case" first if the charging flags arrived; say what you see with the count | `Notify` per pull: `e8` then `00` (or `00` at once); the runtime-info `6.2`/`6.3` field 2 = 2 then 0 | `INEAR-006` | "Probably worn" still at 60 s (the 🟡 of ADR-049 item 3 is then weaker — write it down as a result, not a fault of the app) |

##### III. Every write once, buds worn (film 1; `APP_TESTPLAN.md` V13–V17, U10–U14, H2–H6, M2–M6, N2–N12, I1–I3, T3–T8; `CASE-001`, `CASE-002`, `CONV-001`)

Both buds **in your ears** unless a step says otherwise; music at low volume for BH-14/BH-15. Every write: the control moves only after the Buds' OK.

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BH-14 | worn, music | gear → **Settings → Case sounds**: **Earbuds replaced off** → take the **Left** out of the ear into the case and out again → **say whether the case sounded** → **on** → the same → say; **Other alerts** off, then on; open the card's (i) each time | each switch after its OK; the (i) "Earbuds replaced: changed HH:MM:SS", "Other alerts: changed …", the case note | `4:{28:0}` / `{28:1}`, `4:{27:0}` / `{27:1}` — each = its reference (the move changed nothing) | `CASE-001`, `CASE-002` | a request that differs; a switch on Controls |
| BH-15 | worn, music | **Controls → Conversation detection** off, then on; with it on **speak for 5 s** (ANC on Active); say what the Buds do | each after its OK; the card's subtitle "Switch from noise cancellation to transparency when you talk" | `4:{22:0}` = `CAP-019` 1720, `4:{22:1}` = 1808 (on 21) | `CONV-001` | a request that differs (field 19 or 22 swapped: the mutation M9 case); the row still on Sound |
| BH-16 | worn, music | **Sound**: preset **Balanced**; drag **Upper treble** up, release; **Read EQ again** (H5) ★; balance **Right 4**, then **Centre** ★; **Mono audio** on/off; **Volume EQ** off/on — say what you hear at each | each after its OK; *Read EQ again* shows the value just written; no conversation-detection row on Sound | Balanced = `CAP-059` 2188 on 21; one `WriteSetting 4:{16:…}` on release; `ReadSetting 4:16` = the write; `17:7`, `17:0`; `19:1`, `19:0`; `15:0`, `15:1` | `EQP-001`, `EQS-001`, `AUDIO-003`, `AUDIO-001`, `AUDIO-002` | a request that differs; a read that differs from its write |
| BH-17 | worn | **Controls**: **Use touch controls** off/on; press and hold **Left: Digital assistant**, then **Noise control**; mode list: **untick Off** (restores the Buds); **Use head gestures** off/on; **Multipoint** off/on | each after its OK | `4:{4:0}`/`{4:1}`; `4:{7:{1:{4:{1:6}}}}`/`{1:5}`; `4:{12:{1:1 2:0 3:1 4:0}}` (= `CAP-041` 2198 on 21); `29:1`/`29:2`; `11:0`/`11:1` — each = its reference | `HOLD-005`, `HEAD-001`, `MULTI-001` | a request that differs; the mode list keeps Off |
| BH-18 | worn | **Disconnect**, then **Connect**; open Sound, Controls, gear → Settings and Info | every value just written read back ("read …"): Balanced, Centre, mono off, conversation on, Volume EQ on, touch on, Left Noise control, mode list without Off, in-ear on, head gestures on, Multipoint on, both case sounds on; Info: the **same three serials** with a new time; the Worn line "—" for a moment, then "Probably worn" | the thirteen requests + `4:16`; each answer = the last write; one `GetHardwareInfo` with the same answer bytes as BH-1's | `PAIR-003`, `FW-003` | a read that differs from the last write; different serials; two `GetHardwareInfo` |
| BH-19 | both buds out of the ears, on the table | Find: **Ring Left**, **Stop**, **Ring Right**, **Stop** | the notice and the ring follow each tap | `04 01 00 01 02` → ACK; `… 00`; `… 01`; `… 00` | `FIND-001`, `FIND-002` | a ring that does not stop |

##### IV. Robustness (film 1; `APP_TESTPLAN.md` C8, C9, J4, K1, K2, R6)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BH-20 | ready | both buds into the case, lid open; wait 10 s; take both out again (do not tap Connect) | the session closes with the 1.0.1 cause text; then ready **by itself**; the Worn line "—" during the gap, then a reading after the re-open's `Notify`; the serials shown again | Buds `DISC` 0x02 (or the ACL drops); the app's `SABM` 0x02 ≈ 1.5 s after the link is back (ADR-044); the thirteen requests | `CASE-004`, `CASE-005`, `PAIR-003` | no automatic re-open while the app is on screen |
| BH-21 | ready | **Home**; wait 2 minutes; return to the app | still ready, or a clear message and a re-open; no crash | — | — | a crash |
| BH-22 | ready | **Export** the debug log; Quick Settings **Bluetooth off**; wait 10 s; **Bluetooth on** | "Bluetooth is disabled.", then ready again by itself; Info's serials "Not read …" while off, read again after | export lines "Bluetooth adapter: ON -> TURNING_OFF", "Session loss cause: Bluetooth was switched off on this phone"; **no serial number and no MAC in the export** (grep it afterwards); after on, the automatic re-open and the thirteen requests | `PAIR-003` | a crash; no re-open; a serial in the export |
| BH-end1 | — | **Export** the debug log; status bar across a minute change; stop film 1 and the screen recording | "Debug log saved (N lines)." | — | — | — |

##### V. The open film items of `TODO.md` §2 (film 2; `APP_TESTPLAN.md` C12, S12, T11)

Start film 2 and the screen recording; keep the system log running (P6). (Lead L-1 / `INEAR-005` is BH-5…BH-7 above.)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BH-23 | ready, both worn, Controls tab | put the **Right** bud into the case; **as soon as** the card shows that the app's channel was closed (≈ 0.5–1.5 s), **tap Multipoint** — before "ready" | "The setting was not changed: The app's channel is being reopened — try again in a moment."; the switch unchanged; after "ready" nothing is sent by itself | Buds `DISC` 0x02 → app `SABM` ≈ 1.5 s later; **no** `WriteSetting` for that tap | `CASE-004` | a write sent later by itself (if the tap comes after "ready", say so — C12 stays open) |
| BH-24 | ready | Debug → **Export debug log**; **rotate the phone while Android's "save as" dialog is open**; save (S12) | "Debug log saved (N lines)."; the file has content | — | — | an empty file, no toast |
| BH-25 | ready | **Export** again; Quick Settings **Bluetooth off**; on the computer `adb shell am force-stop io.github.tedsluis.opencontrolpixelbuds` (say it); open the app; **Controls** → `adb shell uiautomator dump /sdcard/CAP-072-controls.xml`; **Sound** (scroll to the Balance card) → `… /sdcard/CAP-072-sound.xml`; **gear → Settings** → `… /sdcard/CAP-072-settings.xml`; `adb pull` all three into this folder (T11) | "Bluetooth is disabled."; Controls: "—" in place of every switch (now seven, conversation detection included); Sound: the five EQ bands, Volume EQ, balance and mono audio "—"; Settings: the two Case sounds switches "—"; the battery card's Worn line "—" | — | — | in a dump, a node with `text="—"` whose `content-desc` is not `Not read from the Buds yet` |
| BH-26 | — | **Bluetooth on** → the app re-opens the session by itself (or **Connect**); **Export**; stop the system log (P6); status bar across a minute change; stop film 2 and the screen recording | ready; the values back; the serials back | the thirteen requests | `PAIR-003` | — |

#### Don'ts

- Do not uninstall 1.1.1 and do not clear the app's data (P1 — the run is an update).
- Do not open the app before the film runs (P9); do not open the Owner user during a film.
- Do not tap two things within 5 s of each other, except in BH-23 (the early tap).
- Do not read the Worn line without a pull first after a wear change — the app asks the Buds only on a claim; a stale "(checked …)" time is not a finding.
- Do not leave the buds in the case when a step says "worn" — say it if a step is done differently.
- Do not write a serial number unredacted anywhere in this folder or the findings (first 4 + last 2 characters; the fixtures use `5707XXXXXXXX51`).

#### After the run

Into this folder: both films, both screen recordings, the system log (CAP-072-logcat-all.txt), every debug export (`adb pull` from the test user's
storage), the app logcat if saved, both btsnoop_hci.log files, the three `uiautomator` dumps, the P0/P1 outputs (before and after the update), and
`dist/1.2.0/`'s `.sha256` and the script's printed certificate line. Then `sha256sum *` into a file. Commit on the release branch `release/1.2.0`, after the
build commit (`RELEASING.md` C3) — never on `main` (it takes no direct push).

#### Analysis checklist

- [ ] Scope every filter to the Buds' ACL handle (show the Connection Complete for the Buds' address with that handle); DLCIs by content (`AGENTS.md` §13);
      for a negative, the command, its exit status and a positive control (step 8).
- [ ] P0 (exit statuses, positive control), P1: user id; no `com.google.android.gms` / `com.android.vending`; **the update**: `firstInstallTime` unchanged
      and `lastUpdateTime` later, `versionCode=10200`; the Info frame "1.2.0, build <hash>" against `git log` and the B1 hash; APK and certificate SHA-256
      against `dist/1.2.0/` and `RELEASING.md`.
- [ ] **The release APK against 1.1.1's:** `aapt2 dump badging` — permissions identical (no `INTERNET`), `targetSdkVersion:'34'`, `sdkVersion:'34'`,
      `compileSdkVersion='37'`; `aapt2 dump xmltree` — no new `<activity>`, `<service>`, `<receiver>`, `<provider>`.
- [ ] Zero Play-services claims on the Message Stream (`frame contains 03:08:00:02:01:25`): command, exit status, positive control `CAP-066`.
- [ ] I: dark mode and Debug mode kept across the update; per session the **thirteen** requests in order with their answers (`python3 -I
      scripts/pwrpc_decode.py --handle <handle> <log>`) — `GetHardwareInfo` **once** per session, **last**, byte-identical to its reference, its answer's
      field 7 strings compared (redacted) with the Info tab on film in the order Case, Right bud, Left bud; S6 on the screen recording; **no**
      `GetHardwareInfo` outside Connect/re-open (`CAP-071` had none at all — positive control for the filter: the official app's in `CAP-036`).
- [ ] II: per pull the `08 11` → `Notify` and its Settable byte against the Worn line on the screen recording and the ears on film — one row per BH-5…BH-13
      reading (**the `INEAR-006` table**: time, ears, Settable byte, in-ear field 2, charging flags, Worn text); BH-11: the unprovoked `08 13` with no `08 11`
      in the 2 s before it and nothing from the app after it, the (i) line's time = the `Notify`'s; BH-12: the line gone after the app's own claim; BH-13:
      the ≈ 28 s timing (Settable `e8` → `00`) — a **proposal** for ADR-049 item 3 / ADR-059, no status change by the analysing session alone; BH-7: the
      `DISC` direction and the announced channel with the head side on film (L-1, `PROTOCOL.md` §2.2a — a proposal).
- [ ] III: **every request against the reference table** (byte for byte, per channel; the others against the unit-test fixtures) — the two case sounds from
      their new place and conversation detection from Controls first; every `RESPONSE`/ACK; the read-back of BH-18 and its serials; the sound observations
      quoted as said (BH-14, BH-15, BH-16).
- [ ] IV: each session end with its logged cause; the automatic re-opens; Bluetooth off/on; **`grep -c` over every export for each serial's first 4 + last 2
      characters and for the Buds' MAC = 0**, with a positive control (the export's own "Control channel" line).
- [ ] V: BH-23 — no `WriteSetting` after the early tap, the text on the screen recording; BH-24's file; **the three dumps** (count `text="—"` nodes and their
      `content-desc` per tab; Controls now seven, Settings two).
- [ ] The system log: crashes, ANRs, `StrictMode` (none expected in a release build); the `bugreportz` SIGQUITs discounted (P6 note).
- [ ] One row per step — done / not done / done differently; `APP_TESTPLAN.md` section V and the Summary updated; traceability (`AGENTS.md` §13 step 7):
      `PAIR-003` (BH-1, BH-18, BH-20, BH-22, BH-26), `BATT-004` (BH-1), `FW-003` (BH-1, BH-2, BH-18), `INEAR-006` (BH-3, BH-5…BH-10, BH-13), `INEAR-005`
      (BH-5, BH-7), `INEAR-004` (BH-7, BH-8, BH-9), `ANC-001`…`ANC-004` (BH-8, BH-10…BH-12), `CASE-001`, `CASE-002` (BH-14), `CONV-001` (BH-15),
      `EQP-001`, `EQS-001`, `AUDIO-001`…`AUDIO-003` (BH-16), `HOLD-005`, `HEAD-001`, `MULTI-001` (BH-17), `CASE-004` (BH-20, BH-23), `CASE-005` (BH-20),
      `FIND-001`, `FIND-002` (BH-19) — each referenced in the timeline or flagged "expected but not observed".
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `id_registry.csv`, `APP_TESTPLAN.md`, `TODO.md` §2/§4/§5 (the restored Buds, the items done, the fixture
      swap of `HardwareInfoFixtures`), ADR-058/ADR-059 Updates (proposals), and write `CAP-072-FINDINGS.md`; the release verdict for 1.2.0 is the analysing
      session's, with the maintainer.


---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-072-2026-10-09_17-27-26_18-23-39-Group_BH/CAP-072-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-072-2026-10-09_17-27-26_18-23-39-Group_BH/CAP-072-EVENT-NOTES
