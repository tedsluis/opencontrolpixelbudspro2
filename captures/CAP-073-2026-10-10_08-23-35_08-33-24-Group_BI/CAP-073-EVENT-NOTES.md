# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, the secondary user without Google Play) — Group BI, the rebuilt 1.2.0 (`CAP-073`)

**Status:** ✅ **Captured 2026-10-10 and analyzed 2026-10-10** (`ai-sessions/0085`). One Android screen recording **without an audio track** (phone
08:23:35.3–08:33:24.6), two HCI snoop logs, one app debug export, the app logcat, the P0/P1 shell log and a filtered extract of the system log. **No camera
film** (the skeleton's part II asked for one). Folder renamed from the placeholder `CAP-073-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BI` to the screen recording's
first/last phone times; `CAP-0731-btsnoop_hci.log.last` (a stray "1") renamed to `CAP-073-btsnoop_hci.log.last`; the film renamed to `CAP-073-screen.mp4`
and committed with the Wi-Fi name blurred; file modes set to 644; of the system log only `CAP-073-logcat-extract.txt` is committed (the maintainer's choices in
chat 2026-10-10, `ai-sessions/0085` RESULT, Checkpoint 0). The analysis is `CAP-073-FINDINGS.md`.

**The run differs from the plan — the maintainer's statement (chat 2026-10-10): "I did not follow the test plan. I only tested the changed functionality."**
What was done, in order: the old build `ec6d163` on screen (Info, Debug, Settings); the **update to `5b4d5db`** over it at 08:27:05 (no uninstall); Info and Debug
again; the buds taken out of the case; the **noise-control tab for 30 s with four press-and-holds and four mode taps** (the hold of ADR-061); the **balance
slider** (four releases); Controls and Find looked at; Disconnect/Connect; buds in and out of the case one at a time with **Earbuds replaced** switched off and
on (a case-sound check, not in this plan); one export; the app logcat saved from Android's App info → "View logs". Not done: the camera part (BI-6 … BI-13 with
the head in view), the Bluetooth off/on (BI-4), the tile on the noise-control tab (BI-10), leaving the app while the tab is shown (BI-12), the re-open with the
tab shown (BI-13), the force-stop and the `uiautomator` dumps (BI-14), the early Multipoint tap (BI-15), the rotation during the save dialog (BI-16), *Read EQ
again* (BI-17), Volume EQ (BI-18), In-ear detection off/on (BI-19). The planned procedure (the committed skeleton) is kept unchanged as the appendix.

**Prefixes used below:** "B" = `CAP-073-btsnoop_hci.log` (2026-10-10 08:22:51.771–08:35:07.913, 7,064 packets — the run), "A" = `CAP-073-btsnoop_hci.log.last`
(2026-10-09 18:32:14.701 – 2026-10-10 08:22:48.796, 28,116 packets — before the run; it ends 2.98 s before B starts, the two are consecutive, not a prefix).
E = the debug export `CAP-073-opencontrol-debug-20261010-083250.txt` (line numbers; only the `5b4d5db` process, 08:27:07.086–08:32:20.679). L = the app logcat
`CAP-073-OpenControl-for-Pixel-Buds-Pro-2-log-774c13a575c8.txt` (UTC; its 260 `OpenControlBuds` lines from 06:31:38.986 equal E's lines for 08:31:38.986–08:32:20.679
except a 1-ms rounding on 14). X = `CAP-073-logcat-extract.txt` (line numbers). S = the screen recording, cited as film seconds `t` (the recorder's own timer
reads t + 1); **phone time = 08:23:35.34 + t** (± 0.05 s: the status-bar minute changed to 08:28 between film 264.650 and 264.667; 08:24, 08:27 and 08:31 agree within
0.36 s, the variable frame rate). All times below are phone time.

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-073` |
| Group(s) | BI |
| Date | 2026-10-10, phone 08:23:35–08:33:24 (screen recording) |
| Phone | Pixel 9a, GrapheneOS `google/tegu/tegu:17/CP3A.261005.005/2026100601:user/release-keys` (L line 2, = `CAP-072`); secondary user 10, `userType: full.secondary` (L line 3; X 2 `am_switch_user: 10` at 08:22:47.871) |
| Google Play in user 10 | none: P0 (below) lists only `app.grapheneos.gmscompat*`, `exit=0`; 0 Play-services claims in B (FINDINGS §1; 14 in A — the Owner user before the switch) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.2.0, build `5b4d5db` (2026-10-10)**, versionCode 10200 — Info tab S t=217–225, no "-dirty"; **the installed APK pulled after the run** (`adb shell pm path --user 10` → `adb pull`, 2026-10-10): SHA-256 `ac04415eabf4369a49e9f88230aa83fc858a7e3ea0223d725c14a43ac5a67220` = `~/opencontrol-1.2.0-tested` (B1); dex2oat `app-version-name:1.2.0,app-version-code:10200`, `compilation-reason=install` (system log 08:26:56) |
| Version before the update | **1.2.0, build `ec6d163` (2026-10-09)** — Info tab S t=10–19; process 15696 (user 10) from 08:22:47.959 (X 4, the tile service) |
| The update | `adb` install 08:26:55 (`abb_exec:package`), `installer_clear_app_data_caller … 39` ×2 (X 147–148, the code cache only — `CAP-071-FINDINGS.md` §0), dex2oat 7.9 s (X 154), `killDueToPackageUpdate` user 10 (X 164–167; 15696 killed), `PACKAGE_REPLACED` for uid 10354 (user 0) and 1010354 (user 10) (X 242–243), process 17391 `top-activity` 08:27:06.619 (X 260); S t=210–211 "Updaten…" |
| Official Pixel Buds app | not in this user |
| Buds | Pixel Buds Pro 2, classic handle `0x000b` in every ACL (`…:07`), firmware `release_5.203` ×3 in 11 of 11 announcements and on the Info tab; serials (redacted) Case `5707…51`, Right bud `5708…09`, Left bud `5707…47` |
| Other devices | an LE device advertising the name "Charge 6" (handle `0x0040`, 08:23:51–08:26:06, ATT only); not touched by the app |
| Audio | **no audio track** (`ffprobe` lists one video stream); the maintainer did not speak (the maintainer's statement) |
| Video files | `CAP-073-screen.mp4` (589.4 s, 1080 × 2424, variable frame rate ≈ 7.8 fps; committed blurred, see below) |
| Log files | B, A, E, L, X, `CAP-073-adb-shell.log`, `CAP-073-sha256sum.txt` |

### The maintainer's P0/P1 outputs (`CAP-073-adb-shell.log`, copied unchanged)

```
Sat Oct 10 08:24:10 AM CEST 2026
    versionCode=10200 minSdk=34 targetSdk=34
    lastUpdateTime=2026-10-09 18:33:54
      firstInstallTime=2026-10-09 18:33:54
      firstInstallTime=2026-10-09 17:24:41
Sat Oct 10 08:24:30 AM CEST 2026
package:app.grapheneos.gmscompat.config
package:app.grapheneos.gmscompat.lib
package:app.grapheneos.gmscompat
exit=0
Sat Oct 10 08:39:19 AM CEST 2026
    versionCode=10200 minSdk=34 targetSdk=34
    lastUpdateTime=2026-10-10 08:27:05
      firstInstallTime=2026-10-09 18:33:54
      firstInstallTime=2026-10-09 17:24:41
```

The two `firstInstallTime` lines are per user. A full `dumpsys package` taken by this session (2026-10-10, phone attached) prints them under "User 0" and
"User 10": **user 0 (the Owner) 2026-10-09 18:33:54, user 10 17:24:41.** So `ec6d163` was installed into the Owner user on 2026-10-09 at 18:33:54, after
`CAP-072` ended; one APK serves both users (`pm path` gives the same path for user 0 and 10). Not made: `am get-current-user` and the P0 positive control
`grep opencontrol` (the user switch to 10 is in X 2).

## Capture-integrity pre-flight

- **B and A are consecutive:** A ends 2026-10-10 08:22:48.796, B starts 08:22:51.771 (`capinfos -a -e`); the user switch 0 → 10 is at 08:22:46.7–47.9 (system log
  `uc_switch_user` / `am_switch_user`). No Buds ACL spans the gap: A's last Buds ACL ended at 07:43:06 (A 26567, `0x13`), B's first starts at 08:23:01.706 (B 159).
- **A is before the run:** OpenControl sessions on 2026-10-09 18:35, 19:29 and 2026-10-10 06:43–06:44, 07:07–07:08, 07:13, each with the thirteen requests and
  `GetHardwareInfo` (a 1.2.0 build), plus EQ and Volume EQ writes (07:07:40–07:08:44, A 21952–22180) and Earbuds-replaced writes (06:43–06:44); the system log
  shows the app resumed **in user 0** at 07:07:08 (`wm_set_resumed_activity: [0,…]`), process 30161 died 07:57:45. Play services (the Owner user) claimed the
  Message Stream 14 times in A. **Not the run;** it explains the Buds' starting state (EQ = −2, 0, 2, 3, 5).
- **The skeleton's P9 ("no app use before the film"):** the app was opened in user 10 at 08:22:59 and connected at 08:23:01 — 34 s before the film starts.
- **The ANC tab with the old build:** not opened on film (S t=0–27: Connection, Settings, Info, Debug); no Message Stream claim from 08:23:03.538 to the update.
  The comparison old ↔ new on the noise-control tab therefore comes from `CAP-072` (FINDINGS §7).

## Video review method (and privacy)

- **Screen recording:** decoded at 1 frame/s (589 frames, 540 px wide); each second compared with the previous one below the status bar (pixels with a
  difference > 40; scratch `diffs3.py`): 447 seconds unchanged (only the recorder's timer changed), 141 changed — every changed second and the first viewed on
  36 labelled 4-frame sheets. Narrowed at the full frame rate where a claim depends on the moment: the minute changes (the clock offset) and the tab change that
  ended the hold (film 293.899 finger on "Sound", 294.029 the Sound content).
- **No audio:** nothing said can be cited; sounds (case chimes, the Buds' mode tones) cannot be checked.
- **Privacy:** the Wi-Fi name (a street name and number) is visible in Quick Settings at film 571.4–575 s and 588–589.4 s; the committed film is **blurred** (top
  820 px, 569.5–575.5 s and 586.5–589.4 s; command in FINDINGS §0). The serial numbers on the Info tab stay readable (component serials, ADR-010 scope, as in
  `CAP-072`); never written unredacted in any text. No phone number, no notification content, no other device name on film.

## Event Timeline

Columns: phone time · what happened (actor) · step / W / Test-ID · evidence.

| Phone time | Event | Step · W · IDs | Evidence |
|---|---|---|---|
| 08:22:46.7–47.9 | user switch 0 → 10; the app's tile service bound in user 10 (process 15696, `ec6d163`) | — | X 2, X 4 |
| 08:22:59.2 | the app opened (MainActivity resumed in user 10); "Android link: PENDING -> NOT_CONNECTED" | P9 deviation | X 32; system log 15696 lines |
| 08:23:01.113 | `ConnectionState: Disconnected -> Connecting` with no "Automatic re-open" line (Android's link read NOT_CONNECTED, so ADR-044 could not fire) — 🟡 a Connect tap; the phone pages the Buds (Create Connection B 157, 17 ms later) | P9 deviation | system log 15696; B 157, 159 |
| 08:23:01.9–08:23:03.5 | session 1 (ch 21): announcement B 258, the thirteen requests, `GetHardwareInfo` B 333 → 336 (46 ms); snapshot claim `SABM` B 270 → `08 11` B 280 → `Notify` `e8 00 20` (B 291, OFF, no bud worn) → the phone's `DISC` B 450 at 08:23:03.530 (**1.513 s** after B 291) | `PAIR-003`, `BATT-004` (old build) | B 236–469 |
| 08:23:04.067 | the phone sets AVRCP absolute volume **0 %**; play status Stopped | — | B 575, 577 |
| **08:23:35.3** | **screen recording starts** (t=0): Connection tab, dark, "App control: ready", Left 100 % ⚡, Case 100 %, Right 100 % ⚡, "Both buds in the case" | P4 · `INEAR-006` | S t=0 |
| 08:23:44–08:24:01 | gear → Settings: Dark mode **On**, Case sounds both on; **Info: "App: 1.2.0, build ec6d163 (2026-10-09)"**, firmware ×3 `release_5.203` (08:23:01), control channel 21, serials (08:23:02) Case / Right bud / Left bud; Debug: **Debug mode on**, Unidentified frames (5); Settings; back to Connection | P1 (on film) · W1 (before) · `FW-003` | S t=9–26 |
| 08:24:02–08:27:05 | the Connection tab, unchanged on screen (182 s) | — | S t=27–209 (no body change) |
| 08:24:10 / 08:24:30 | P1 `dumpsys` (before) / P0 | P0, P1 | shell log |
| 08:26:55–08:27:05 | **`5b4d5db` installed over `ec6d163`** (adb); 15696 killed `killDueToPackageUpdate`; the phone's `DISC` of DLCI 2 at 08:27:05.399 (the dying process's socket) | P1 · W1 | X 147–243; B 3025 |
| 08:27:05.7–06.6 | Android's "Updaten…" (light) | W1 | S t=210–211 |
| 08:27:07.086 | new process 17391: "Permissions (start): … GRANTED"; Android link CONNECTED; **automatic re-open (LINK_BACK)**; session 2 (ch 21) | W1 · `PAIR-003` | E 1–51; X 260, 343 |
| 08:27:07.6 | Connection: Case "Battery unavailable — Not reported yet — the Buds send the Case level only while a bud is charging in the case." (for < 1 s), then 100 % | W1 | S t=212–213; E 49 (runtime info 08:27:08.204) |
| 08:27:07.381–08:27:08.928 | snapshot claim: `SABM` B 3078, `08 11` B 3086, `Notify` `e8 00 20` B 3100 (OFF), the phone's `DISC` B 3143 (**1.511 s**; E 52 "released") — the Connection tab is shown, no hold | BI-1 · W1 | B 3078–3145 |
| 08:27:10–08:27:30 | Settings: Dark mode **On** (kept), Case sounds on; **Info: "App: 1.2.0, build 5b4d5db (2026-10-10)"**, no "-dirty", firmware ×3 (08:27:07), channel 21, serials (08:27:08) Case / Right bud / Left bud; Debug: Debug mode **on** (kept) | BI-2 · W1 · `FW-003` | S t=215–229 |
| 08:27:28–08:27:33 | Connection; battery (i): "Left: 100% (updated 08:27:07) — charging in the case (08:27:26)", Right the same, Case 100 % (08:27:26), "Both buds in the case", the Worn text **"… they have reported it for 40 seconds or more with both buds on a table. …"** (no capture id) | BI-19's (i) text · W11 (part) | S t=232–235; E 53 |
| 08:27:40.931 | the Buds' `DISC` of DLCI 2 (the Left bud taken out — runtime info 08:27:45: 6.2 = 1); "Session loss cause: … the Buds closed the channel" | `CASE-004` | B 3257; E 57–61 |
| 08:27:42.445 | automatic re-open (AFTER_LOSS, **1.500 s** after "Ready -> Disconnected"); session 3 on **ch 19** (only the Left out) | `PAIR-003` | E 62–111; B 3283 |
| 08:27:43.536–08:27:45.519 | snapshot claim B 3318 … `Notify` `e8 e8 80` B 3344 (Settable `e8`, TRN), `DISC` B 3383 (**1.507 s**); screen "App control: not open yet … The app's channel was closed while Android still shows the Buds connected …" then "connecting…", then ready, **"Probably worn (checked 08:27:44)"** | — · `INEAR-006` | S t=246–250 |
| 08:27:57.6 | the Right bud out too (runtime info 6.3 = 1) | `CASE-005` | B 3451 |
| **08:27:59.4** | **the ANC tab opened** (t=264): Transparency (the 08:27:44 value) | **BI-6** · W4 | S t=264 |
| 08:27:59.763–.918 | **one claim**: `SABM` B 3484, `UA` B 3486, `08 11 00 00` B 3492, `Notify` **`e8 e8 08`** B 3503 (NC) → screen **Noise cancellation**; E 131 **"Message Stream hold started: the noise-control tab is on screen (ADR-061)"** at 08:27:59.920 | BI-6 · W4 · `ANC-002` | B 3484–3504; E 130–131; S t=265 |
| 08:28:05.455 | **unprovoked `Notify` `e8 e8 40`** (B 3531; the app's last `08 11` was 5.57 s earlier) → screen **Adaptive** without a touch | BI-7 · W5 · `TOUCH-007`, `ANC-003` | S t=270 |
| 08:28:08.072 | unprovoked `Notify` `e8 e8 80` (B 3543) → **Transparency** | BI-8 · W5 · `TOUCH-007`, `ANC-004` | S t=273 |
| 08:28:11.877 | unprovoked `Notify` `e8 e8 08` (B 3546) → **Noise cancellation** | W5 · `TOUCH-007` | S t=277 |
| 08:28:15.749 | unprovoked `Notify` `e8 e8 40` (B 3547) → **Adaptive** | W5 · `TOUCH-007` | S t=280 |
| 08:28:07.4 / 08:28:27.6 | the Buds push "Battery updated" `03 03 00 03 64 64 ff` ×3 on the held channel (no request) | — | B 3534–3538, 3629–3631 |
| 08:28:19.010–.197 | tap **Transparency**: `08 11` B 3550 → `Notify` `e8 e8 40` → `Set` `08 12 00 14 01 e8 e8 80 …` B 3557 → ACK `ff 01 00 06 08 12 01 e8 e8 80` B 3566 — **no `SABM`** | BI-9 · W6 · `ANC-004` | S t=284 |
| 08:28:20.810–.942 | tap **Off**: `08 11` B 3574 → `Set … 20` B 3577 → ACK B 3579 | BI-9 · W6 · `ANC-001` | S t=285–286 |
| 08:28:22.366–.520 | tap **Noise cancellation**: `08 11` B 3584 → `Set … 08` B 3587 → ACK B 3589 | BI-9 · W6 · `ANC-002` | S t=287 |
| 08:28:23.623–.734 | tap **Adaptive**: `08 11` B 3594 → `Set … 40` B 3597 → ACK B 3599 | BI-9 · W6 · `ANC-003` | S t=288 |
| **08:28:29.37** | tap **Sound** (finger on film 293.899, the Sound content 294.029); E 164 **"Message Stream hold ended: the noise-control tab was left"** at 08:28:29.381 | **BI-11** · W7 | S t=293.9–294.0 |
| 08:28:30.890 | **the phone's `DISC`** of DLCI 4 (B 3654), **1.509 s** after the hold ended; `UA` B 3656; E 166 "released" | BI-11 · W7 | B 3654–3656 |
| 08:28:33–08:28:50 | **Sound → Balance:** release 1 → `4:{17:9}` B 3679 → OK B 3681, "Right 5"; release 2 → `4:{17:27}` B 3682 → OK, "Right 14"; drag "Right 6 — release to set", "Right 3 — …", "Right 4 — …", release 3 → **`4:{17:7}`** B 3685 → OK B 3687, **"Right 4"**; drag "Right 2 — …", "Left 6 — …", "Left 43 — …", "Left 83 — …", "Left 77 — …", "Left 9 — …", "Left 3 — …", release 4 → `4:{17:8}` B 3691 → OK B 3694 (08:28:49.068), "Right 4" until the OK, then **"Left 4"** | **BI-20** · W12 · `AUDIO-003` | S t=298–314; E 167–178 |
| 08:28:54–08:29:00 | Controls (touch on, both holds Noise control, the mode list Noise cancellation + Adaptive + Transparency, Off unticked); Find | — | S t=319–321 |
| 08:29:46.094 | **Disconnect** tap (phone `DISC` DLCI 2 B 3761) | BI-3 · W2 · `PAIR-003` | E 183–184; S t=371 |
| 08:29:48.237 | **Connect** tap; session 4 (ch 19), thirteen requests, `GetHardwareInfo` B 3878 → 3882 (57 ms); snapshot claim B 3823 … `DISC` B 3883 (**1.508 s**); "Probably worn (checked 08:29:48)" | BI-3 · W2 · `PAIR-003` | E 185–231; S t=373 |
| 08:29:51–08:29:56 | battery (i): Left/Right "not charging (08:29:49)", Case "last seen 08:27:46 (last connection)", the Worn text | — | S t=376–380 |
| 08:30:23.5 | the Right bud into the case (6.3 = 2) — Right ⚡ on screen | — | B 3953; S t=408 |
| 08:30:30.238 | **ACL dropped by the Buds (`0x13`)**; "the Buds closed the channel" → "Android's link to the Buds went down around the loss"; screen "Android no longer shows the Buds connected …" | — | B 3968; E 238–244; S t=415 |
| 08:30:32.3–08:30:35.2 | the Buds' ACL back (B 3969 Connection Request, B 3973); LINK_BACK re-open, first attempt fails on the multiplexer, the second connects (ch **21**, Left in, Right out); snapshot `Notify` `e8 00 20` (OFF) then **unprovoked `e8 e8 80`** (TRN, B 4315, 334 ms later); "Not worn (checked 08:30:33)" → **"Probably worn (checked 08:30:34)"**; `DISC` B 4448 (1.507 s) | `PAIR-003`, `CASE-005`, `INEAR-006` | E 245–302; S t=417–419 |
| 08:30:41.3 / 08:30:56.3 | two more ACL drops (`0x13`, B 4478, 4950) as buds were swapped; one LINK_BACK re-open between (ch 19, B 4816); the 10-s chain guard skips two re-opens (E 306, 366) | `CASE-004`, `CASE-005` | E 303–370; S t=426–443 |
| 08:31:16.3 | 🟡 **a pull-to-refresh on Controls while not connected** (the content is drawn lower at film t=459, a touch at its middle; no "Automatic re-open" line) → Connect; the phone pages the Buds (B 4951), session 7 (ch 19), **both buds in the case** (runtime info 08:31:18) | not in the plan · `PAIR-003` | E 371–418; B 4951–4953; S t=455–463 |
| 08:31:20–08:31:37 | Controls read (touch, holds, mode list, head gestures, in-ear detection, conversation detection, Multipoint — all on/as before); gear → Settings | — | S t=463–481 |
| 08:31:38.903 | **Earbuds replaced off** → `4:{28:0}` (ch 19) B 5419 → OK B 5421; the switch moves after the OK | not in the plan (case-sound check) | E 422–424; S t=483–484 |
| 08:31:41.6–08:32:20.6 | buds in and out of the case one at a time: the Buds' `DISC` (08:31:41.598), re-open ch 21 (AFTER_LOSS 1.500 s), ACL drops 08:31:46.9, 08:31:55.4, 08:32:13.2, 08:32:20.6 (`0x13`), LINK_BACK re-opens ch 19 / 21 / 19; each re-open shows "—" for both case-sound switches until the reads answer; Earbuds replaced read `0` until the next write | not in the plan · `CASE-004`, `CASE-005`, `PAIR-003` | E 427–681; S t=486–525 |
| 08:32:08.320 | **Earbuds replaced on** → `4:{28:1}` (ch **21**) B 6510 → OK B 6516 | not in the plan | E 605–607; S t=513 |
| 08:32:20.6 | last session loss (ACL `0x13`, B 7038); the app stays not connected to the end | — | E 674–681 |
| 08:32:49.4–08:32:54.3 | Debug → **Export debug log** → Android's save dialog (Downloads) → "Debug log saved (681 lines)." | BI-5 / BI-end · W2 | S t=554–560; L "Debug log exported (681 lines)" 06:32:54.338 UTC |
| 08:33:06–08:33:22 | Quick Settings; Android Settings → Apps → OpenControl → App info → **View logs** → the log viewer → Save → `OpenControl for Pixel Buds Pro 2 log 774c13a575c8.txt` | X (the app logcat) | S t=571–586 |
| 08:33:24.6 | **screen recording ends** (Quick Settings, "Stoppen") | — | S t=589.4 |
| 08:33:47 | `bugreportz` (the HCI log pull); system log ends 08:34:07 | — | X 1264–1265 |
| 08:39:19 | P1 `dumpsys` (after) | P1 | shell log |

## Step mapping

| Step | Done? | Where | Note |
|---|---|---|---|
| P0 | done, partly | shell log | no `am get-current-user`, no positive control (the user is in X 2) |
| P1 | done | S t=9–20 (before), t=215–229 (after); shell log | Dark mode and Debug mode set before, kept after |
| P2, P6 | done | B; system log (full log kept off the repository) | — |
| P3 | **not done** | — | no camera film |
| P4 | done, **no sound** | S | the recorder's audio off |
| P5 | done differently | S | minute changes at film 24.3, 204.3, 264.65, 444.3 |
| P7 | not checked | — | no call came |
| P8 | done | S t=0 | both buds in the case at the start |
| P9 | **not done** | E/X 08:22:59–08:23:03 | the app opened and connected 34 s before the film |
| BI-1 | done (old build at 08:23:01; new build at 08:27:07 by LINK_BACK) | E 1–52; B 3078–3145 | thirteen requests, one `GetHardwareInfo`; the snapshot claim released 1.511 s after its answer |
| BI-2 | done | S t=215–225 | build `5b4d5db`, no "-dirty"; firmware ×3; serials Case / Right / Left |
| BI-3 | done | E 183–231 | Disconnect 08:29:46, Connect 08:29:48 |
| BI-4 | **not done** | — | no Bluetooth off/on |
| BI-5 | done once (at the end) | S t=555–560 | "Debug log saved (681 lines)." |
| BI-6 | done (without camera; both buds out of the case, worn per the maintainer — not filmed) | B 3484–3504; E 131 | one `SABM`, `08 11`, the hold logged; **no `DISC` for 30.5 s** (08:27:59.8–08:28:30.9) while the tab was shown; the ANC (i) not opened |
| BI-7, BI-8 | done (four holds, the bud not identifiable) | B 3531, 3543, 3546, 3547 | the mode changed on screen without a tap each time; the (i) line **not looked at** |
| BI-9 | done ×4 | B 3550–3602 | `08 11` → `Notify` → `Set` → ACK on the held channel, no `SABM`; the (i) not opened |
| BI-10 | **not done** | — | the tile not used |
| BI-11 | done (tab → Sound instead of Controls) | B 3654; E 164–166 | `DISC` 1.509 s after the hold ended |
| BI-12 | **not done** | — | no return to the ANC tab, no Home |
| BI-13 | **not done** with the tab shown | — | the many re-opens (08:30–08:32) happened on Connection, Controls and Settings |
| BI-14 … BI-19 | **not done** | — | (the battery (i) text of BI-19 seen at t=235/376) |
| BI-20 | done | B 3679–3694 | "Right 4" on the 3rd release, then "Left 4" (not "Centre") |
| BI-end | done | S t=555–589 | export, the app logcat via "View logs" |

**Not in the plan:** the app opened and connected before the film (P9); the Earbuds-replaced off/on with buds swapped in and out of the case (08:31:38–08:32:20,
the skeleton's "Not in this run"); a Connect by pull-to-refresh on Controls (08:31:16); the app logcat saved through Android's App info.

**Test-IDs (`AGENTS.md` §13.7):** `PAIR-003` (08:23:01, 08:27:07, 08:27:42, 08:29:48, 08:30:32, 08:31:16, 08:31:43, 08:31:51, 08:32:03, 08:32:16), `BATT-004`
(08:23:02, 08:27:07), `FW-003` (S t=10–19, 217–225), `ANC-001` Off (set 08:28:20), `ANC-002` NC (read 08:27:59, set 08:28:22), `ANC-003` Adaptive (08:28:05 by the Buds, set 08:28:23), `ANC-004` Transparency (08:28:08 by the Buds, set 08:28:19), `TOUCH-007` (08:28:05,
08:28:08, 08:28:11, 08:28:15 — four unprovoked `Notify`; the press-and-holds themselves are the maintainer's statement, not filmed), `CASE-004` Left out of the case (08:27:40, 08:30:47, 08:31:52,
08:32:17 — runtime info 6.2 = 1), `CASE-005` Right out (08:27:57, 08:30:33, 08:31:45, 08:32:06 — 6.3 = 1), `INEAR-006` (the Worn line at 08:23:35, 08:27:44, 08:29:48, 08:30:33/34), `AUDIO-003`
(08:28:34–08:28:49). **Expected but not observed:** `AUDIO-002` (BI-18, Volume EQ not run), `INEAR-004` (BI-19, in-ear detection not switched).

## Analysis checklist

- [x] Every filter scoped to the Buds' handle `0x000b` (Connection Complete B 159, 3973, 4483, 4953, 5589, 6069, 6581 for the Buds' address).
- [x] P0, P1, the Info frame's hash = B1's; **the installed APK = B1's file** (pulled, SHA-256 equal).
- [x] Zero Play-services claims in B (positive control `CAP-066`).
- [x] I: every session's thirteen requests (11 of 11), one `GetHardwareInfo` per session; every snapshot claim released 1.507–1.513 s after its answer.
- [x] II (ADR-061) as far as run: the hold's start, no `DISC` while the tab was shown, the `DISC` 1.509 s after leaving; four unprovoked `Notify`, nothing from
      the app after them; the taps without `SABM`. Not run: the tile, Home, the re-open with the tab shown, the (i) line.
- [x] III: BI-20 against the references; the others not run.
- [x] The system log: no crash, ANR or app `StrictMode`; the `bugreportz` SIGQUITs (08:34:01–07) hit other processes only.
- [x] Step mapping and traceability (above).

## Appendix — the planned procedure (the skeleton as committed by `ai-sessions/0084`, unchanged)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0084`, 2026-10-10). The re-test of **1.2.0 rebuilt as the same version**
(versionCode 10200) after `CAP-072`'s verdict "fix first" (the maintainer, chat 2026-10-10): `DECISIONS.md` **ADR-061** — the app holds its noise-control
channel (the Message Stream, DLCI 0x04) while the noise-control tab is on screen, so a press-and-hold on a bud shows as "Changed by the Buds" — plus a finer
balance slider and the film-2 items `CAP-072` did not run. **Kept short on purpose** (the maintainer's request in `ai-sessions/0083`: "find out how testing
can be simpler and shorter"): **≤ 20 minutes on film**, three parts — **I** the minimal release run (screen recording only), **II** the hold (the camera on
the buds and the head, for this part only), **III** the leftovers of `CAP-072`. Installed **over the `ec6d163` 1.2.0 now on the phone** (no uninstall). The
maintainer films; a later CAPTURE session analyses it and gives the release verdict. After the run: rename this folder to the film's first/last overlay
times and the films to CAP-073-recording.mp4 / CAP-073-screen.mp4.

**Not in this run** (say so if asked): the case-sound experiment (`TODO.md` §2 — its own capture, the official app and a microphone at the case) and the wear
sequences of `CAP-072` §4 (no app change touches the worn line's logic; only its (i) text changed).

#### Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-073` |
| Group(s) | BI (new) |
| Date | TBD |
| Phone | Pixel 9a, GrapheneOS (Android 17, `CP3A.261005.005` in `CAP-072`) — the secondary user without Google Play (user 10 in `CAP-072`; P0) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.2.0** rebuilt (versionCode 10200), the release APK of `scripts/release.sh` (2026-10-10 08:05), **build commit `5b4d5db`** (the tip of `release/1.2.0-rebuild`), kept as `~/opencontrol-1.2.0-tested` (B3, byte-identical to `dist/1.2.0`) — **Info tab on film**: "App: 1.2.0, build 5b4d5db (<date>)", no "-dirty"; APK SHA-256 `ac04415eabf4369a49e9f88230aa83fc858a7e3ea0223d725c14a43ac5a67220`, certificate SHA-256 `a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb` (= `README.md` / `SECURITY.md`) |
| Previous version in this user | 1.2.0 build `ec6d163` (installed 2026-10-09 for `CAP-072`) — the update is from it (same versionCode: an equal code installs over the tested one, `RELEASING.md` rules) |
| Official Pixel Buds app | not used (not in this user) |
| Buds | Pixel Buds Pro 2, firmware `release_5.203` expected (any other ⇒ Safe Mode: say so, stop part III's writes) |
| Video files | TBD — the screen recording (with sound) for the whole run; the camera (with sound) for part II only |
| Log files | TBD — the HCI snoop logs, the app's debug exports, the app logcat, the system log (P6), the three `uiautomator` dumps (BI-14), the P0/P1 outputs |

#### Preparation

What `CAP-072` missed is marked ★.

| # | Check | Done |
|---|---|---|
| P0 | In the test user: `adb shell am get-current-user`; `adb shell pm list packages --user <id> \| grep -i -E "gms\|vending"; echo "exit=$?"`; positive control `grep opencontrol` (exit 0) — save the outputs **with the exit statuses** | ☐ |
| P1 | In the installed 1.2.0 (`ec6d163`): Dark mode **On**, Debug mode **on** (on film). `adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds \| grep -E "firstInstallTime\|lastUpdateTime\|versionCode"` → save. `adb install --user <id> -r ~/opencontrol-1.2.0-tested/opencontrol-pixelbudspro2-1.2.0.apk` (SHA-256 `ac04415e…a67220`) — **no uninstall**; the same `dumpsys` → save | ☐ |
| P2 | HCI snoop log on (set in the Owner, then switch to the test user) | ☐ |
| P3 | The camera (with sound) is needed for **part II only**: the buds, the case and your head with both ears in view | ☐ |
| P4 | Android's **screen recorder with sound** on for the whole run | ☐ |
| P5 | The status bar across a minute change at the start and at the end | ☐ |
| P6 | `adb logcat -b all -v threadtime > CAP-073-logcat-all.txt` on the computer before P2; stop it after the last export. Each `adb bugreportz` sends signal 3 to every Java process — not an app fault (`CAP-071-FINDINGS.md` §0) | ☐ |
| P7 ★ | **Do Not Disturb on** (`CAP-072` took a call on film) | ☐ |
| P8 ★ | Buds charged, both in the case, **lid open**; Android shows them connected before the app is opened | ☐ |
| P9 ★ | **No app use before the film** — not opened between P1's install and BI-1 | ☐ |

**Rhythm:** one action, then 5–10 s; say what you do. Export the debug log before Bluetooth off and before the force-stop. Serials: never unredacted in any
note (first 4 + last 2 characters).

#### Steps

DLCIs are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05). Every MAESTRO request starts `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25` on
**channel 21** or `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25` on **channel 19** (Info → "Control channel"). **Reference frames** — "A" =
`CAP-072-btsnoop_hci2.log.last`, "B" = `CAP-072-btsnoop_hci2.log` (OpenControl 1.2.0 `ec6d163`), `CAP-070` = `CAP-070-btsnoop_hci2.log.last` (1.1.0), all
re-derived by `ai-sessions/0084` with `tshark -r <log> -Y "frame.number==N" -T fields -e frame.number -e data.data` or `-Y 'frame contains <bytes>'`:

| Request | Channel 21 | Channel 19 |
|---|---|---|
| `ReadSetting 4:16`, then 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29 | A 7521 `7e004b0310151dea71de7d5e2551aed0ae2a02201047eeadcf7e`, then as `CAP-072-EVENT-NOTES.md`'s table | A 8540 `7e003b0310131dea71de7d5e2551aed0ae2a022010fdae3f247e`, then as that table |
| `SubscribeRuntimeInfo` | A 7592 `…90821ee66654bfab7e` | A 8615 `…90821ee6602d65a97e` |
| `GetHardwareInfo` (last of the thirteen) | A 7593 `7e004b0310151dea71de7d5e25e3a5ec28f96761b57e` | A 8616 `7e003b0310131dea71de7d5e25e3a5ec28ff1ebbb77e` |
| its answer | A 7596 (the three strings Case, Right, Left — redacted) | A 8620 |
| Volume EQ `4:{15:0}` / `{15:1}` | `CAP-070` 785 `…2a04220278000f47ef397e` / 4800 `…2a04220278019977e84e7e` | `CAP-070` 3072 `…2a04220278003fab19517e` / 1953 `…2a0422027801a99b1e267e` |
| Balance Right 4 `4:{17:7}` / Centre `4:{17:0}` | `CAP-064` 6671 `…2a052203880107a97d5edf037e` | `CAP-070` 3747 `…2a052203880107e9b86e257e` / `CAP-046` 1873 |
| In-ear detection `4:{2:0}` / `{2:1}` | `CAP-056` 2849 / 3627 | B 980 `…2a0422021000904a3dfc7e` / B 983 `…2a0422021001067a3a8b7e` |
| Multipoint (must **not** be sent in BI-15) | `CAP-069` 3161 / 3212 | `CAP-070` 3668 `…2a04220258009d8f9dc47e` / 3679 |
| ANC `Get` (every claim) | `08 11 00 00` (A 13499) | |
| ANC `Notify` | `08 13 00 04 01 e8 [S] [mode]` — answered (A 11938 `…e8e880`) or **unprovoked** (A 13519 `…e8e880`, B 825 `…e8e808`); mode `08` NC, `40` Adaptive, `80` Transparency, `20` Off; Settable `00` when no bud is worn | |
| ANC `Set` / ACK | A 11939 `0812001401e8e808` + 16 × `00` / A 11941 `ff010006081201e8e808` | |
| The release of a claim | the **phone's** `DISC` on DLCI 0x04 (`btrfcomm.frame_type == 0x43`, A 11947, 1.51 s after the last answer A 11944) and the Buds' `UA` (A 11949) | |

Anything else from the app on the wire (a second `GetHardwareInfo` in a session, a request not in this table or `CAP-072`'s, anything on DLCI 0x08/0x0a) is a
finding. Requests with no row here are compared with the unit-test fixtures (`android/data/src/test`).

##### I. The minimal release run (screen recording only; `APP_TESTPLAN.md` W1–W3; `PAIR-003`, `BATT-004`, `FW-003`, `ANC-001`…`ANC-004`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BI-1 | P0–P9 done; lid open; the Connection tab will open first | open the app | dark (kept); no crash; the session opens by itself; "Both buds in the case" | DLCI 0x02 open, the announcement, the thirteen requests in order, one `GetHardwareInfo` last, answered; **no** DLCI 0x04 claim held (the Connection tab is shown: the snapshot claim is released ≈ 1.5 s after its answer) | `PAIR-003`, `BATT-004` | a crash; light theme; a request missing or out of order; DLCI 0x04 still open 3 s after the snapshot |
| BI-2 | ready | gear → **Info**: hold 3 s on the build line, the firmware, the serials | "App: 1.2.0, build <B1 hash> (<date>)", no "-dirty"; `release_5.203` ×3; Case / Right bud / Left bud | nothing | `FW-003` | "-dirty"; another hash than B1's; no serials |
| BI-3 | ready, Connection tab | **Disconnect**, **Connect** | ready | the thirteen requests again; one `GetHardwareInfo` | `PAIR-003` | — |
| BI-4 | both buds in the ears | Quick Settings **Bluetooth off**; 10 s; **on** | "Bluetooth is disabled.", then ready by itself | "Bluetooth adapter: ON -> TURNING_OFF" and "Session loss cause: Bluetooth was switched off on this phone" in the export; after on, the automatic re-open and the thirteen requests | `PAIR-003` | no re-open |
| BI-5 | ready | Debug → **Export** | "Debug log saved (N lines)." | — | — | an empty file |

##### II. The hold (ADR-061) — **camera on, buds and head in view**; `APP_TESTPLAN.md` W4–W9; `TOUCH-007`, `ANC-001`…`ANC-004`, `CASE-004`, `CASE-005`

The press-and-hold cycles through the modes ticked on Controls ("Modes for press and hold"); after `CAP-072` that list is Noise cancellation, Transparency,
Adaptive (`TODO.md` §4) — say which mode you hear after each hold.

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BI-6 | both worn, Connection tab | open the **ANC** tab; wait **30 s**, touch nothing | the mode; the (i) ends "While this tab is open the app keeps the Buds' noise-control channel open, …" | one `SABM` DLCI 0x04 and `08 11 00 00` → `Notify`; **no `DISC`** on DLCI 0x04 for the 30 s; export later: "Message Stream hold started: the noise-control tab is on screen (ADR-061)" | `ANC-001`…`ANC-004` | a `DISC` while the tab is shown |
| BI-7 | the hold open, the ANC (i) **closed** | **press and hold the Left bud**; say the mode you hear; wait 5 s; open the ANC (i) | the mode changes **without a tap**; the (i): "Changed by the Buds at HH:MM:SS (a press-and-hold on a bud, or the Buds' own change)." | an **unprovoked** `08 13 00 04 01 e8 e8 xx` — no `08 11` in the 2 s before it, nothing sent by the app after it | `TOUCH-007` | no `Notify`; the `Notify` but no line; the app sending anything in reaction |
| BI-8 | the (i) open | close it; **press and hold the Right bud**; say the mode; open the (i) | the mode changes again; the line's time moves | another unprovoked `08 13` | `TOUCH-007` | the time not moving |
| BI-9 | the line shown | **tap** another mode in the app; open the (i) | the mode after its ACK; no "Changed by the Buds" line ("updated …") | `08 11` → `Notify` → `Set` = the reference → ACK — **no `SABM`** (the held channel) | `ANC-001`…`ANC-004` | a second `SABM`; the line still shown |
| BI-10 | the ANC tab shown | pull **Quick Settings** down over it; tap the **ANC tile** once; close Quick Settings | the tile steps one mode; the app follows | `08 11` → `Notify` → `Set` → ACK on the held channel, **no `SABM`**, **no `DISC`** | `ANC-001`…`ANC-004` | a `DISC` after the tile |
| BI-11 | the hold open | tap the **Controls** tab; wait 5 s | Controls | the **phone's `DISC`** on DLCI 0x04 **≈ 1.5 s** after the tab change (from the screen recording's clock; window 1.3–2.0 s); export: "Message Stream hold ended: the noise-control tab was left" | — | no `DISC`, or one before 1.3 s |
| BI-12 | Controls | back to **ANC** (a new claim); wait 5 s; **Home**; wait 5 s; return to the app | ANC; then the home screen; then ANC again | a new `SABM` + `08 11` on entering; the phone's `DISC` ≈ 1.5 s after Home ("… the app is not visible"); a new `SABM` + `08 11` on return | — | no claim on entering or return; no `DISC` after Home |
| BI-13 | ANC tab shown, both worn | both buds **into the case** (the session ends); wait 10 s; **both out into the ears**; wait 15 s, ANC tab still shown | the session ends with its cause; then ready again by itself | ACL `0x13` (or the Buds' `DISC`); "Message Stream hold ended: the session was lost"; after the Buds' link is back: the ADR-044 re-open, the thirteen requests and its snapshot claim (`SABM` + `08 11`) — **kept: no `DISC` on DLCI 0x04** while the tab is shown | `CASE-004`, `CASE-005`, `PAIR-003` | the snapshot claim released while the ANC tab is shown |

Camera off after BI-13.

##### III. What `CAP-072` did not run (screen recording; `APP_TESTPLAN.md` W10–W12, T11, C12, S12, H5, T7, V8; `AUDIO-002`, `AUDIO-003`, `INEAR-006`, `INEAR-004`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BI-14 | ready | **Export**; Quick Settings **Bluetooth off**; on the computer `adb shell am force-stop io.github.tedsluis.opencontrolpixelbuds` (say it); open the app; **Controls** → `adb shell uiautomator dump /sdcard/CAP-073-controls.xml`; **Sound** (scroll to Balance) → `… /sdcard/CAP-073-sound.xml`; **gear → Settings** → `… /sdcard/CAP-073-settings.xml`; `adb pull` all three (T11) | "Bluetooth is disabled."; "—" in place of every unread switch (Controls seven, Sound eight, Settings two) | — | — | a `text="—"` node whose `content-desc` is not `Not read from the Buds yet` |
| BI-15 | Bluetooth **on**, ready, both worn, Controls | put the **Right** bud into the case; **as soon as** the card shows the channel closed (≈ 0.5–1.5 s), **tap Multipoint** — before "ready" (C12) | "The setting was not changed: The app's channel is being reopened — try again in a moment."; the switch unchanged; nothing sent later by itself | the Buds' `DISC` → the app's `SABM` ≈ 1.5 s later; **no** `4:{11:…}` | `CASE-004` | a Multipoint write (if the tap came after "ready", say so — C12 stays open) |
| BI-16 | ready | Debug → **Export**; **rotate the phone while Android's save dialog is open**; save (S12) | "Debug log saved (N lines)."; the file has content | — | — | an empty file, no toast |
| BI-17 | ready, both worn | **Sound**: tap **Read EQ again** (H5) | the EQ (i) "EQ updated: HH:MM:SS" moves; the bands unchanged | one `ReadSetting 4:16` (the reference), its answer = the active EQ | — | no read, or a write |
| BI-18 | ready | **Volume EQ** off, then on — say what you hear at low volume (T7) | each after its OK | `4:{15:0}` / `{15:1}` = the reference for the channel | `AUDIO-002` | a request that differs |
| BI-19 | both worn | **Controls → In-ear detection off**; **Connection: pull down**; read the Worn line; **In-ear detection on**; pull; read (V8) | after off and the pull: **"Worn: unknown — in-ear detection is off"**; after on and the pull: "Probably worn (checked …)"; the battery (i) without "CAP-064" ("… for 40 seconds or more …") | `4:{2:0}` → OK; `08 11` → `Notify`; `4:{2:1}` → OK; `08 11` → `Notify` | `INEAR-006`, `INEAR-004` | "Probably worn" while in-ear detection is off |
| BI-20 | ready | **Sound → Balance**: drag slowly and watch the label; let go at **"Right 4 — release to set"**; then drag to **"Centre — release to set"** and let go | while dragging the label shows the finger's value with "— release to set"; after each release the Buds' value: "Right 4", then "Centre"; **one write per release** | `4:{17:7}` = the reference for the channel, then `4:{17:0}` — nothing during the drags | `AUDIO-003` | more than one write per release; a write during a drag; "Right 4" taking more than 3 attempts (a UX result, write it down) |
| BI-end | — | **Export**; stop the system log (P6); the status bar across a minute change; stop the screen recording | "Debug log saved (N lines)." | — | — | — |

#### Don'ts

- Do not uninstall the installed 1.2.0 and do not clear the app's data (P1 — the run is an update).
- Do not open the app before the film (P9); do not open the Owner user during the run.
- In part II, do not tap anything during the 30 s of BI-6 or between a hold and its (i) check.
- Do not tap two things within 5 s of each other, except in BI-15 (the early tap).
- Do not write a serial number unredacted anywhere in this folder (first 4 + last 2 characters).

#### After the run

Into this folder: the screen recording, the camera film (part II), the system log, every debug export, the app logcat if saved, the HCI snoop logs, the three
`uiautomator` dumps, the P0/P1 outputs, `dist/1.2.0/`'s `.sha256` and the script's certificate line; then `sha256sum *` into a file. Before committing:
blur phone numbers, Wi-Fi names, addresses and other device names. Commit on the release branch after the build commit (`RELEASING.md` C3) — never on `main`.

#### Analysis checklist

- [ ] Every filter scoped to the Buds' ACL handle (the Connection Complete for the Buds' address with that handle shown); DLCIs by content; a negative with
      its command, its exit status and a positive control (`AGENTS.md` §13 step 8).
- [ ] P0, P1 (the update: `firstInstallTime` unchanged, `lastUpdateTime` later, `versionCode=10200`); the Info frame's hash = B1's; APK and certificate SHA-256.
- [ ] Zero Play-services claims (`frame contains 03:08:00:02:01:25`, positive control `CAP-066`).
- [ ] I: per session the thirteen requests against the reference table; one `GetHardwareInfo` per session; the Connection tab's snapshot claim released.
- [ ] **II (ADR-061):** every DLCI 0x04 `SABM`/`DISC` with its time against the tab shown on the screen recording — the hold's start at BI-6, no `DISC` while
      the tab is shown, the `DISC` ≈ 1.5 s after leaving the tab (BI-11) and after Home (BI-12), the new claim on entering and on return; the unprovoked `08 13`
      of BI-7/BI-8 with no `08 11` in the 2 s before and nothing from the app after, the (i) line's time = the `Notify`'s; BI-9/BI-10 without `SABM`; BI-13's
      re-open snapshot kept; the export's hold lines with their reasons.
- [ ] III: the dumps (count `text="—"` per tab and their `content-desc`); BI-15 without a `WriteSetting`; BI-16's file; BI-17's read; BI-18 and BI-20 against the
      references (BI-20: count the writes per release, none during a drag); BI-19's Worn texts on the screen recording.
- [ ] The system log: crashes, ANRs, `StrictMode`; the `bugreportz` SIGQUITs discounted.
- [ ] One row per step — done / not done / done differently; `APP_TESTPLAN.md` section W and the Summary; traceability (`AGENTS.md` §13 step 7): `PAIR-003`
      (BI-1, BI-3, BI-4, BI-13), `BATT-004` (BI-1), `FW-003` (BI-2), `ANC-001`…`ANC-004` (BI-6, BI-9, BI-10), `TOUCH-007` (BI-7, BI-8), `CASE-004` (BI-13,
      BI-15), `CASE-005` (BI-13), `AUDIO-002` (BI-18), `AUDIO-003` (BI-20), `INEAR-006`, `INEAR-004` (BI-19) — each referenced in the timeline or flagged
      "expected but not observed".
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `id_registry.csv`, `APP_TESTPLAN.md`, `TODO.md` §2/§5; write the findings file (CAP-073-FINDINGS); the release verdict is the
      analysing session's, with the maintainer.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-073-2026-10-10_08-23-35_08-33-24-Group_BI/CAP-073-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-073-2026-10-10_08-23-35_08-33-24-Group_BI/CAP-073-EVENT-NOTES
