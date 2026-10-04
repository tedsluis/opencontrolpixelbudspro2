# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, a secondary user without Google Play) — Group BD, the 1.0.1 release APK (`CAP-068`)

**Status:** 🟢 **Video pass + full log analysis complete** (`ai-sessions/0070`). See `CAP-068-FINDINGS.md` for the wire-level analysis, the commands and
the raw bytes. The planned procedure (the skeleton of `ai-sessions/0069`, as committed in `e1fc886`) is kept as **Appendix A** (text unchanged, heading levels
lowered by one; the maintainer's P0 output that was added to it after the run is under Log Metadata). The timeline below records what was **actually** done: the
maintainer did not speak, filmed the ears only at a few moments, did section IV's Disconnect before section III, skipped Adaptive on the tab, did BD-7 not as
written, BD-17's pairing with three picker attempts (the first film stopped by itself during the second), H5 through a reconnect instead of *Read EQ again*,
and two extra things (pull-to-refresh to reconnect after the re-open was skipped; the Sound/Controls/Find tabs while disconnected). **The force-stop is in no
file** (see Log Metadata, "Force-stop").

**Purpose:** the release test of the release-signed **1.0.1** APK (build `e1fc886`, versionCode 10001) in a GrapheneOS **secondary user without Google Play**:
an ANC `Set` from the tab and the tile (Definition of done 2 with frames), a tap on the current mode (`ANC-006`), Bluetooth off/on with an export after each
(F-3), the `ai-sessions/0069` changes (section S of `APP_TESTPLAN.md`), the never-run steps C5, H5, F5, J4, I4, L3, the Ring stopped on the bud (`FIND-005`) and
a connect that fails.

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-068` |
| Group(s) | BD |
| Date | 2026-10-04 |
| Firmware version | 🟢 `release_5.203` — all 15 MAESTRO announcements (A 6, B 9; each with 3 entries `release_5.203`) in the HCI logs (e.g. A261, B415; `scripts/pwrpc_decode.py`) and the Info tab on film (film 1 07:23:45–51 and film 2 07:42:33–07:43:14: "Case / Left bud / Right bud: release_5.203") |
| Test device | Pixel 9a, GrapheneOS, Android 17 `CP3A.260905.009` (logcat header `google/tegu/tegu:17/CP3A.260905.009/2026092501:user/release-keys`), **`userType: full.secondary`**, user id **10** (P0) |
| Google Play in the test user | **Absent.** P0, added by the maintainer after the run (exit status not recorded; no positive control recorded): `adb shell am get-current-user` → `10`; `adb shell pm list packages --user 10 \| grep -i -E "gms\|vending"` → `package:app.grapheneos.gmscompat.config`, `package:app.grapheneos.gmscompat.lib`, `package:app.grapheneos.gmscompat` — packages of GrapheneOS's own `app.grapheneos` namespace that match "gms" by name (🟡 its sandboxed-Play compatibility layer, grapheneos.org/usage; `CAP-068-FINDINGS.md` §0); **no** `com.google.android.gms` and **no** `com.android.vending` line. Wire: **0** Message Stream messages `03 08 00 02 01 25` (Play services' marker) in all four logs, positive control `CAP-066` 22 + 4 (`CAP-068-FINDINGS.md` §1). No DLCI 0x08/0x0a traffic either (Google app's GSND channels; positive control `CAP-066` 234) |
| App build | **OpenControl for Pixel Buds Pro 2 1.0.1, build `e1fc886` (2026-10-04), not "-dirty"** — Info tab on film 1 07:23:45–51 and film 2 07:42:33–07:43:14 ("App: 1.0.1, build e1fc886 (2026-10-04)"); logcat header `package: io.github.tedsluis.opencontrolpixelbuds:10001, targetSdk 34`. `e1fc886` is the tip of `origin/maintenance/0069` (PR #1, open). `dist/1.0.1/opencontrol-pixelbudspro2-1.0.1.apk`: SHA-256 `f9dce033d3a42d93892b90ebe7b77347385c93602bdec099f59d232963b15e0f` (re-computed = its `.sha256`), signer certificate SHA-256 `a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb` (`apksigner verify --print-certs`, build-tools 34.0.0), `versionCode='10001' versionName='1.0.1'` (`aapt2 dump badging`); the strings `e1fc886` and `2026-10-04` are in its dex. That this file is the APK on the phone is ⚪ (no install log; the on-screen build and versionCode match) |
| Install over 1.0.0 (P1) | **Not verifiable, and the evidence points the other way** (🟡): at the first start the app had to ask for *Nearby devices* and notifications (E5 lines 7–8 "Bluetooth NOT_REQUESTED -> GRANTED, notifications NOT_REQUESTED -> GRANTED"), and "Use different Buds" removed **0** associations (E5 537) although `CAP-067` ended with an association created (`CAP-067` E2 08:17:01.671). Runtime permissions and CDM associations survive an update; both are reset by an uninstall. No system log exists to settle it |
| Official Pixel Buds app | Not used |
| Other devices | Bluetooth list on film: "Charge 6", "Niro" (saved, not connected). HCI: classic handle `0x000b` = the Buds, the only classic connection in all four logs. The two CDM inquiries found one other device, a TV (B827, B1016, its name in the Extended Inquiry Result); LE advertising reports only during the CDM scans |
| Buds | `04:00:6e:cf:6e:07` (ADR-010). **Pre-filter:** `bluetooth.addr` is empty with this encapsulation; every command filters `bthci_acl.chandle==0x000b` (shown matching: Connection Complete A160, B159, B1092, B2844 with that handle and the Buds' address). **DLCIs:** the phone opened every multiplexer in this run (A231, B256, B1164, B2915 `SABM` DLCI 0) ⇒ MAESTRO DLCI 0x02, Message Stream 0x04, HFP 0x0c (Buds' AG side DLCI 0x09 for the second HFP) |
| Video files | `CAP-068-recording1.mp4`: 1,069.67 s, 834,251,747 bytes, H.264 1280×720 rotation −90 (portrait), ≈ 29.8 fps, AAC 44.1 kHz stereo; `creation_time` 2026-10-04T05:40:47Z = its **end**; overlay `Oct 4, 2026 HH:MM:SS` **07:22:57 (first frame) – 07:40:46 (last frame)**. `CAP-068-recording2.mp4`: 798.98 s, 623,542,033 bytes, same format; `creation_time` 05:54:11Z = its end; overlay **07:40:52 – 07:54:10**. **Gap between the films: 07:40:47–07:40:51 (≈ 5 s)**, during the second CDM picker attempt (the picker shows "Zoeken naar een apparaat" at both edges) |
| Audio | Both tracks are near-silent (median −80 dB RMS per second) apart from handling noise. Audible: the Find My Buds tone (3-s pulses) film 2 07:48:44.8–07:49:03.5 and 07:50:48–07:51:12.4. Used for timing only; what the maintainer heard in the ears is not on the track |
| HCI logs | **Four files, two Bluetooth off/on cycles (P2 at 07:23:01–06, BD-9/BD-11 at 07:34:56–07:35:44).** "Z" `CAP-068-btsnoop_hci.log.last`: 1,413 packets 07:11:25.132–07:23:02.020 (before the film; the P2 Bluetooth-off). "A" `CAP-068-btsnoop_hci2.log.last`: 2,314 packets 07:23:07.514–07:34:58.303 (P2's Bluetooth-on to BD-9's off). `CAP-068-btsnoop_hci.log`: 2,219 packets 07:23:07.514–07:33:48.745 — **a byte-for-byte prefix of A** (99,452 of A's 106,821 bytes identical; per-packet time/length/direction equal for all 2,219) = the same snoop session pulled earlier; **not counted separately**. "B" `CAP-068-btsnoop_hci2.log`: 3,563 packets 07:35:44.243–07:56:06.297 (BD-11's Bluetooth-on to 2 min after the film). Boundaries: Z→A 5.5 s and A→B 45.9 s, each a Bluetooth-off period (no Buds traffic possible) |
| App debug exports | **Five, one process, each later export contains the earlier one as its exact prefix** (line by line). E1 `…-073517.txt` 435 lines (434 newlines) 07:11:42.250–07:34:58.165, saved on film 07:35:16–23 (toast "Debug log saved (435 lines)."); E2 `…-073601.txt` 481 lines to 07:35:51.250, film 07:36:01–07 (481); E3 `…-074922.txt` 862 lines to 07:49:11.125, film 07:49:19–26 (862); E4 `…-074950.txt` 869 lines to 07:49:45.704, film 07:49:47–54 (869); E5 `…-075355.txt` 942 lines to 07:53:44.526, film 07:53:52–58 (942). Each export is the ring buffer at the tap; the gap between its last line and its save time is time without a log event. Debug mode (hex lines): off until film 07:23:54 (first hex line 07:24:11.875), off again 07:49:28–07:50:30 (film; no hex line between 07:49:10.066 and 07:50:49.875), on otherwise |
| App logcat | `CAP-068-OpenControl-for-Pixel-Buds-Pro-2-log-b098b04ac4a7.txt`, 425 lines, buffers main/system/crash/events; times UTC (= local − 2 h: E5 07:53:05.414 "connect failed" = logcat 05:53:05.413). **One PID (16124)** from 05:11:42.183 (`wm_on_create_called`) to 05:54:17.956; the **main** buffer (the app's own lines) only from 05:50:49.140. No `FATAL`, ANR, crash, `tombstoned` or StrictMode line; the only exception text is the `IOException` of the failed connect (line 303) |
| System log | **None** — not available in the user without Play services (the maintainer). Checks that needed it are covered as follows: adapter `STATE_CHANGED` → the app's own "Bluetooth adapter: …" lines (E5 429–441) and the HCI logs; `ForegroundServiceDidNotStartInTimeException` → only the logcat's crash buffer of PID 16124 (empty) — a system-side exception stays unverified; force-stop / `am_kill` → no evidence |
| Force-stop | **In no file.** The last logcat lines are `wm_on_paused` 05:54:16.836 / `wm_on_stop` 05:54:17.921 UTC (the app left the screen after the film); the HCI log B still shows the app's MAESTRO session alive at 07:55:59 (B3556–3560, a runtime-info push and the phone's RFCOMM credit) with no `DISC` up to its end 07:56:06.297. So all files were saved **before** the force-stop (the maintainer's statement), and the force-stop happened after 07:56:06 or was not captured at all; the films end 07:54:10 |
| Clock offsets | Film ↔ phone, 4-fps crops of the status bar against the overlay at both ends of each film: the minute flips 0–0.5 s after the overlay's second tick (film 1 07:23:00 and 07:40:00; film 2 07:41:00 and 07:54:00) ⇒ **phone = overlay − 0…0.5 s, no drift**. Logcat = local − 2 h (± 1 ms, above). Exports = phone local time. Times below are phone time; film times are the overlay |
| Wear mapping | The maintainer's rule: head on the **right** of the frame = **Left** bud, on the **left** = **Right** bud. **The head was in view only at film 1 07:27:19–21 (taking a bud out, hand obstructing the ear) and 07:36:41–45 (no bud action)** — no wear change can be assigned by head side. Buds out of the case are identified by the runtime-info per-bud charging field (6.2 Left / 6.3 Right, field 2: 2 = charging) and the app's ⚡ marks (`CAP-068-FINDINGS.md` §5) |
| Events file / P0 | none / P0 above (in the skeleton's working-tree version) |

## Capture-integrity pre-flight

```
$ capinfos -c -a -e -E -T CAP-068-btsnoop_hci.log.last CAP-068-btsnoop_hci.log CAP-068-btsnoop_hci2.log.last CAP-068-btsnoop_hci2.log
CAP-068-btsnoop_hci.log.last   bluetooth-h4-linux  1413  2026-10-04 07:11:25.132606  2026-10-04 07:23:02.020361
CAP-068-btsnoop_hci.log        bluetooth-h4-linux  2219  2026-10-04 07:23:07.514325  2026-10-04 07:33:48.745027
CAP-068-btsnoop_hci2.log.last  bluetooth-h4-linux  2314  2026-10-04 07:23:07.514325  2026-10-04 07:34:58.303896
CAP-068-btsnoop_hci2.log       bluetooth-h4-linux  3563  2026-10-04 07:35:44.243055  2026-10-04 07:56:06.297903
$ python3 -c "A=open('CAP-068-btsnoop_hci.log','rb').read(); B=open('CAP-068-btsnoop_hci2.log.last','rb').read(); print(len(A),len(B),B[:len(A)]==A)"
99452 106821 True
```

- Every ACL on handle `0x000b` begins with the **phone's** `Create Connection` (Z153, A158, B155, B1090, B2837, B2842) — Bluetooth-on (Z, A, B) or an app connect with no ACL (B1090 after the pairing, B2837 the failed connect, B2842 the Retry). Disconnections: Z1108 07:20:58.391 `0x13`, A2312 07:34:58.165 `0x16` (local host: Bluetooth off), B933 07:40:26.648 `0x13` (buds docked during the pairing), B2836 07:52:15.153 `0x13` (buds docked, lid closed); B2840 07:53:05.400 `Connection Complete` **status `0x04`** (page timeout).
- The pw_hdlc CRC-32 is correct in **341 of 341** MAESTRO frames (A 132, B 209; Z has none) — scratchpad `crc.py`, `CAP-068-FINDINGS.md` §2.
- Exports: E1 ⊂ E2 ⊂ E3 ⊂ E4 ⊂ E5 (each earlier file equals the first lines of the next, Python line comparison). Only E5 is cited below unless a line is missing from it.
- Gap between the films (07:40:47–07:40:51): the HCI log B shows the CDM scan's `Inquiry Complete` at 07:40:42.897 and nothing else on the classic link until the third picker request at 07:40:58.64; E5 has no line between 07:40:30.018 and 07:40:56.431. Nothing happened on the wire or in the app in the gap.

## Video review method

Both films were scanned completely at 1 fps (contact sheets of 2-s steps; scratchpad `sheets/`), every transition again at 1 s and, where a claim depends on
it, at 4 fps (`flip/`): the status-bar minute flips at both ends of each film, the Connect double tap (BD-20), the Refresh taps (BD-22), the Debug-mode switch
(BD-25/26), the ringing notice (BD-27/28) and the wear changes of film 1. Film 1 was recorded in portrait (rotation −90) close to the phone; film 2 starts on a
tripod (wide, 07:40:52–07:41:26) and continues close to the phone. Text was read from the 960-px frames; nothing below depends on a word that was not legible.
**Privacy (checked frame by frame, the maintainer's answer in chat 2026-10-04: commit as is):** the Wi-Fi network name is readable in Quick Settings (film 1
07:22:57–07:23:09, 07:24:04–07:24:43, 07:26:28–07:27:55, 07:34:51–07:35:01), the paired-device names "Charge 6" and "Niro", the carrier; the maintainer's head
(hair, glasses) at film 1 07:27:19–21 and 07:36:41–45; no notification content, no other face, no street-address overlay (the overlay is the date and time only).

## Event Timeline

Phone time. "A"/"B"/"Z" frame numbers as in Log Metadata; E5 line numbers; L = logcat line. Step = the skeleton (Appendix A); S = `APP_TESTPLAN.md` section S.

| Time | Action / event | Actor | Step | S | Test-ID | Evidence |
|---|---|---|---|---|---|---|
| 07:11:25–07:23:02 | Before the film: Bluetooth on (phone pages the Buds Z153 → ACL Z159), the app started 07:11:42 (permission prompt shown, app left at 07:11:46), opened again 07:21:37–45; ACL dropped 07:20:58 (Z1108 `0x13`). No app traffic (no MAESTRO frame in Z) | User / Android | (P2 pre) | — | — | Z; E5 1–5; L 12–35 |
| 07:22:57–07:23:01 | Film starts on the Bluetooth panel: Bluetooth on, "Pixel Buds Pro 2 va…" saved; case open, both buds docked | — | P5 | — | — | film 1 |
| 07:23:01–07:23:06 | **Bluetooth off and on** in the panel (Z ends 07:23:02.020, A starts 07:23:07.514) | User | P2 | — | `PAIR-003` | film 1 4 fps; Z/A edges |
| 07:23:13–07:23:28 | Home → OpenControl icon → Android's *Nearby devices* prompt → **Toestaan**; notifications prompt → **Toestaan** | User | (P1 consequence) | — | — | film 1; E5 7–8 "NOT_REQUESTED -> GRANTED" |
| 07:23:29–07:23:35 | "Paired — not connected to this phone …" → **Connect** (07:23:32.8) → ready; Battery 98 % ⚡ / Case 80 % / 98 % ⚡ (docked) | User | — | — | `BATT-004`, `PAIR-003` | E5 13–34; A158–A433; claim 1 A271: `08 11` → `03 03 00 03 e2 e2 ff`, Notify `01 e8 00 20`; RT A320 Case 80 |
| 07:23:40–07:23:51 | Gear → Settings ("Use different Buds" text) → **Info**: "App: 1.0.1, build e1fc886 (2026-10-04)", ADR-051 notice, firmware ×3 `release_5.203` (07:23:34), "Control channel: 19" | User | P7, BD-19 | S13 | — | film 1 |
| 07:23:52–55 | Debug tab: Debug mode **off**, "Unidentified frames (5)"; Debug mode switched **on** | User | P7 | — | — | film 1; first hex line E5 36 07:24:11.875 |
| 07:24:04–07:24:43 | Shade: notification "Connected — ANC: OFF"; Quick Settings edit: **ANC tile added and enlarged**; tile reads "ANC / Not allowed now" (docked) | User | P6 | — | — | film 1 |
| 07:24:44–49 | Home and back to the app | User | — | — | — | L 50–55 |
| 07:25:09–07:25:13 | **A bud out of the case** → Buds `DISC` MAESTRO (A749 07:25:11.294) → card "The app's channel was closed while Android still shows the Buds connected — the Buds do this when a bud goes in or out of the case or an ear …" → automatic re-open 07:25:12.8, announcement **21** (A797). Which bud: the **Right** (A860 runtime info 6.3 field 2 = 1, 6.2 = 2; film 07:25:19 "Left 100 % ⚡ … Right 100 %") | User → Buds → App | (BD-1 pre) | — | `CASE-005` | A749, A797, A860; E5 45–74; film 1 |
| 07:25:27–07:25:37 | **The other (Left) bud out** → Buds `DISC` (A883 07:25:34.474) → re-open, announcement **19** (A919) | User → Buds → App | (BD-1 pre) | — | `CASE-004` | A883, A919, A983 (no 6.1, 6.2/6.3 field 2 = 1); E5 93–120 |
| 07:25:41 | ANC tab: Noise cancellation ✓ (Notify A971 `01 e8 e8 08`) | User | — | — | — | film 1 |
| 07:25:50.5 | Tab **Transparency** → claim 4: Get → Notify `e8 e8 08` → `Set … 80` (A1039) → ACK `ff 01 00 06 08 12 01 e8 e8 80` (A1042) → Notify `… 80` | User | BD-1 | — | `ANC-002` | A1017–A1049; film 1 07:25:51 ✓ Transparency |
| — | **Adaptive not tapped on the tab** | — | BD-2 skipped | — | `ANC-003` (tile only) | film 1; no `Set … 40` before 07:26:45 |
| 07:25:59.9 | Tab **Off** → `Set … 20` (A1090) → ACK (A1093) | User | BD-3 | — | `ANC-001` | film 1 07:26:01 |
| 07:26:15.8 | Tab **Noise cancellation** → `Set … 08` (A1146) → ACK (A1149); at the release the phone sent three `DM` on DLCI 4 (A1157/60/61) while the Buds' battery frames crossed its `DISC` (A1154/55) | User | BD-4 | — | `ANC-004` | A1124–A1164; film 1 07:26:17 |
| 07:26:28–29 | Shade: "Connected — ANC: ACTIVE" | — | — | — | — | film 1 |
| 07:26:37.1 | **Tile** (shown ACTIVE) → Get → Notify `08` → `Set 80` (A1215) → ACK (A1218) → tile "Transparent" | User | BD-5 (1) | — | `ANC-002` | E5 171; A1193–A1225; film 1 07:26:39 |
| 07:26:44.7 | Tile → Notify `80` → `Set 40` (A1266) → ACK (A1269) → "Adaptive" | User | BD-5 (2) | — | `ANC-003` | E5 183; film 1 07:26:45 |
| 07:26:51.4 | Tile → Notify `40` → `Set 20` (A1323) → ACK → "Off" | User | BD-5 (3) | — | `ANC-001` | E5 195; film 1 07:26:53 |
| 07:26:57.8 | Tile → Notify `20` → `Set 08` (A1375) → ACK → "Active" | User | BD-5 (4) | — | `ANC-004` | E5 209; film 1 07:26:59 |
| 07:27:06.5 | Tile → Notify `08` → `Set 80` (A1426) → ACK → "Transparent" — **a fifth tile tap** (repeat) | User | BD-5 (5, repeated) | — | `ANC-002` | E5 221; film 1 07:27:07 |
| 07:27:19–07:27:37 | **Both buds out of the ears onto the table** (head in view 07:27:19–21, the ear covered by the hand; no head-side assignment). No Buds `DISC` on this change | User | BD-6 | — | `INEAR-004` | film 1; A (no `DISC` 07:27:08–07:28:38) |
| 07:27:51.1 | **Tile** → claim 12: Notify **`01 e8 00 20`** (A1508) → **no `Set`**; toast "The Buds don't allow changing noise control right now (usually because no b…"; tile "Not allowed now" | User | BD-6 | S11 | `INEAR-004` | E5 237, 245; A1486–A1511; film 1 07:27:53–55 |
| 07:27:58–07:28:05 | Home, app reopened; ANC tab shows **Off** ✓ (the claim's Notify) with the "don't allow" note | User | BD-6 | — | — | film 1; L 67–72 |
| 07:28:07–08 | Tab **Adaptive** → claim 13: Notify `00` (A1616) → no `Set`; mode stays Off | User | BD-6 | — | — | E5 258; A1596–A1621 |
| 07:28:33–07:28:41 | First bud into an ear (head not in view) → Buds `DISC` (A1635 07:28:38.960) → re-open, announcement **21** (A1673); claim 14 Notify `01 e8 e8 80` (A1719) — Transparent, the Buds' own mode | User → Buds → App | BD-7 pre | — | `INEAR-002`/`003` (bud not identifiable) | E5 265–303; film 1 07:28:39–43 |
| 07:28:45–07:28:51 | Second bud into an ear → Buds `DISC` (A1744 07:28:50.960) → **"Automatic re-open skipped: the session was lost 9939 ms after an automatic re-open"** (ADR-044's 10-s guard); ANC tab "Not connected to the Buds …" | User → Buds → App | — | — | — | E5 307–311; film 1 07:28:51–07:29:21 |
| 07:29:22–23 | **Pull-to-refresh on the ANC tab** (not in the plan) → Connect → ready on **19** (A1791) | User | extra | — | `PAIR-003` | film 1 4 fps 07:29:22.5; E5 312 |
| 07:29:30.4 | Tab **Noise cancellation** (shown Transparency) → Notify `80` → `Set 08` (A1913) → ACK | User | BD-7 done differently (the shown mode was Transparency, not Active) | — | `ANC-004` | A1887–A1922; film 1 07:29:31 |
| 07:29:40.4 | Tab **Off** → Notify `08` → `Set 20` (A1967) → ACK (A1970) | User | BD-8 (1st) | — | `ANC-001` | A1943–A1990; film 1 07:29:41 |
| 07:29:46.0 | Tab **Off again** (the current mode) → Notify **`01 e8 e8 20`** (A2033) → **`Set … 20` sent** (A2034) → **ACK** `ff 01 00 06 08 12 01 e8 e8 20` (A2036) → Notify `… 20` (A2037) | User | BD-8 (2nd) | — | **`ANC-006`** | A2009–A2042; screen unchanged |
| 07:30:22–07:30:31 | Connection tab: Left 100 % / Case 80 % (dimmed, dot) / Right 98 % | — | — | — | — | film 1 |
| 07:30:31.9 | **Disconnect** (section IV before section III) → card "not open yet"; notification gone; **tile "ANC / Open the app"**; ANC/Sound/Controls tabs **not** opened | User | BD-13 (1st, partly) | S1 (tile only) | `PAIR-003` | E5 387; A2045; film 1 07:30:33–55 |
| 07:31:02.7 | **Connect** → ready (A2100 ch 19); Left 98 / Right 98, Case 80 dimmed with dot | User | BD-14 (1st) | S3 (mode not filmed) | `PAIR-003`, `BATT-004` | E5 389; A2118–A2165; film 1 07:31:03–05 |
| 07:31:05–07:34:50 | Idle ≈ 3.7 min on the Connection tab | — | — | — | — | film 1 |
| 07:34:51–55 | Shade: "Connected — ANC: OFF · 2 m"; Bluetooth panel "Actief. Batterijniveau 98%" | — | — | — | — | film 1 |
| 07:34:56–57 | **Bluetooth off** (panel) → E5 429 "Bluetooth adapter: ON -> TURNING_OFF" 07:34:57.441; phone `DISC` HFP (A2243) and MAESTRO (A2303 07:34:58.121); ACL `0x16` (A2312); E5 434 **"Session loss cause: Bluetooth was switched off on this phone"** (no "provisional") | User | BD-9 | — | `PAIR-003` | film 1; E5 429–435; A2243–A2312 |
| 07:35:03–13 | App: "Bluetooth is disabled." + **Enable Bluetooth** | — | BD-9 | — | — | film 1 |
| 07:35:13–23 | Gear → Debug → **Export** → save → toast "Debug log saved (435 lines)." (E1); no rotation during the dialog | User | BD-10 | S12 not done | — | film 1; E5 438 |
| 07:35:37–07:35:49 | **Enable Bluetooth** → Android "OpenControl … wil bluetooth aanzetten" → **Toestaan** → adapter ON 07:35:45.635 (E5 441) → card "Bluetooth was switched off on this phone, which closed the app's channel. Tap Connect to reconnect." → **automatic re-open (ADR-044, LINK_BACK)** 07:35:48.0 → ready (B415 ch 19) | User → App | BD-11 | — | `PAIR-003`, `BATT-004` | film 1; E5 440–485; B155–B508 |
| 07:35:57–07:36:07 | Export again → "Debug log saved (481 lines)." (E2) | User | BD-12 | — | — | film 1; E5 486 |
| 07:37:00.9 | **Disconnect** again; then **ANC** (Off, dimmed, (i) dot, "Not connected to the Buds. Controls are disabled …"), **Sound** (EQ 0/2/4/0/−1 = Vocal, dimmed, dot; presets disabled), **Controls** (dimmed; press-and-hold, mode list), **Find** (buttons disabled) — the (i) dialogs not opened | User | BD-13 (2nd, done) | S1, S2 (texts in the (i) not read) | — | E5 487; film 1 07:37:01–31 |
| 07:37:40.5 | **Connect** → ready (B660 ch 19); battery card (i): "Left: 97% (updated 07:37:42) … Right: 98% (updated 07:37:42) … **Case: 80% — last seen 07:25:14 (last connection)** … The Buds report the Case level only while a bud is in the case. Firmware: release_5.203" | User | BD-14 (2nd) | S3 (mode transition not filmed), **S4 ✓** | `BATT-004` | E5 489; B644–B726; film 1 07:37:41–07:38:06 |
| 07:38:17 | Sound → **FLAT** → `WriteSetting 4:{16:{0.0 ×5}}` (B732) → `RESPONSE` OK (B735); sliders 0,0 | User | BD-15 | S5 | `EQS-001` | E5 526; film 1 07:38:19 |
| 07:38:50 / :54 / :58 / 07:39:00 | **Heavy bass** (B740 `[5,3,0,0,0]`) → **Flat** (B745) → **Vocal** (B748 `[−1,0,4,2,0]`, the preset active before) → **Flat** (B751); each OK | User | BD-16 (+ two extra presets) | — | `EQS-001` | E5 528–534; film 1 |
| 07:39:24–07:39:36 | Gear → Settings: "Use different Buds" → tap → session closes; E5 537 **"Pairing: use different Buds — removed 0 association(s) of this app; the next Pair a device opens the picker"** | User | BD-17 | S7 | — | E5 535–538; B756; film 1 |
| 07:39:48–07:39:55 | Sound tab (EQ 0,0 dimmed); Connection: **"No Pixel Buds Pro 2 paired yet."** + Pair a device → tap → "Waiting for you to pick your Pixel Buds in the system dialog…" | User | BD-17 | S7, S8 | — | film 1; E5 539 |
| 07:39:56–07:40:14 | CDM picker "Zoeken naar een apparaat": the phone's inquiry (B770–B864) finds only a TV; nothing listed → **Annuleren** → "Pairing failed: the system dialog reported "user_rejected". Try again." | User | BD-17 (1st attempt) | S8 | — | B770–B873; E5 540; film 1 |
| 07:40:15–07:40:27 | Case handled, **both buds docked** (07:40:27, LED); a bud gesture → AVRCP PLAY (B915) and the volume panel on screen (07:40:25) — not in the plan; ACL dropped `0x13` (B933 07:40:26.648) | User | extra | — | `CASE-006` | film 1; B914–B933 |
| 07:40:29–07:40:56 | **Pair a device** (2nd) → picker searching (inquiry B942–B1063, the TV only) — **film 1 ends 07:40:46, film 2 starts 07:40:52** — **Annuleren** → "user_rejected" | User | BD-17 (2nd attempt) | — | — | E5 541–542; films 1/2 |
| 07:40:58–07:41:04 | **Pair a device** (3rd) → Android's dialog "Toestaan dat de app OpenControl for Pixel Buds Pro 2 toegang heeft tot de Pixel Buds Pro 2 va…?" (no device list) → **Toestaan** → "association created", "already bonded — no createBond()" → "Paired — not connected to this phone" | User | BD-17 (3rd, done) | S8 | `PAIR-001` (association only, no bonding) | E5 543–547; film 2 07:41:00–06 |
| 07:41:19.8 | **Connect** → phone pages (B1090) → ACL (B1092) → MAESTRO (B1170) → ready, announcement **21** (B1192); "Android doesn't show the Buds as connected (yet)" then "Connected"; 98 ⚡ / 79 / 98 ⚡ | User | BD-17 end | S8 | `PAIR-003`, `BATT-004` | E5 548–572; film 2 07:41:20–22 |
| 07:42:13–07:42:30 | Sound tab: EQ 0,0 — **not filmed within the first second after ready** | — | BD-18 not identifiable | S6 not shown | — | film 2 |
| 07:42:33–07:43:14 | **Info**: "App: 1.0.1, build e1fc886 (2026-10-04)", firmware (07:41:20), "Control channel: 21" | User | BD-19 | S13 | — | film 2 |
| 07:43:20.8 | **Disconnect**; 07:43:22.5–07:43:23.9 finger on **Connect** (one long touch on film; a second tap not identifiable) → **one** "Disconnected -> Connecting" (07:43:23.877), **one** `SABM` DLCI 2 (B1634) → ready, no error | User | BD-20 | — | `PAIR-003` | E5 597–613; film 2 4 fps |
| 07:43:42–44 | Sound: **Upper treble** dragged → `WriteSetting 4:{16:{0,0,0,0,3.37}}` (B1733) → OK (B1735); slider "3,4"; (i) "EQ updated: 07:43:44" | User | BD-21 | — | `EQS-001` | E5 638; film 2 |
| 07:44:01 | Sound scrolled: Balance "Right 4", Mono off, Conversation detection on | — | — | — | — | film 2 |
| — | **Read EQ again not tapped** (no `ReadSetting` between B1735 and B1941); the next connection's read returned the written quintet `[0,0,0,0,3.37]` (B1948 07:45:34.938) | — | BD-21 done differently | — | — | B1941–B1948 |
| 07:44:40–07:44:48 | Buds taken out of the case (runtime info B1757–B1781: 6.1 Case disappears, 6.2/6.3 field 2 → 1); ANC tab "The Buds don't allow …", Off | User | BD-22 pre | — | `CASE-004`/`005` | film 2 07:44:55 |
| 07:45:15–16 | **Refresh** (4 fps 07:45:15) → claim 5: Notify **`01 e8 e8 80`** (B1835) → screen Transparency, note gone | User | BD-22 (Refresh 1) | — | — | B1815–B1840; film 2 |
| 07:45:30 | **Refresh** again → claim 6: Notify **`01 e8 e8 08`** (B1893) — the mode changed to Noise cancellation **without a phone `Set`**; then the Buds `DISC` DLCI 4 and DLCI 2 (B1896/B1897 07:45:32.44) → re-open (ch 19, B1936) | User / Buds | BD-22 (F5, done; the press-and-hold itself not on film) | — | — | B1873–B1999; E5 670–694; film 2 07:45:32–36 |
| 07:45:47.6–07:47:59.5 | **Home; app in the background 2 min 12 s**; session stays ready (runtime-info pushes B2051, B2059, B2065 received, no `DISC`); reopened → ANC "Noise cancellation", no message | User | BD-23 | — | — | L 206–212; E5 711–720; film 2 |
| 07:48:16.9 | Find → **Ring Left** → claim 8: `04 01 00 01 02` (B2142) → Buds ACK `ff 01 00 03 04 01 00` (B2150) → Buds' own `04 01 00 01 02` (B2153); no phone ACK of it; "Ringing: Left earbud — tap Stop to end it." | User | BD-24 | — | `FIND-001` | B2130–B2156; film 2 07:48:18 |
| 07:48:31.7 | Buds `DISC` MAESTRO (B2164) — no user action on film → "A ring was started on the Left earbud — reconnect and tap Stop to end it." → re-open (ch 21, B2203) → "… before the app reconnected — it may still be ringing. Tap Stop to end it." | Buds → App | BD-24 (extra loss) | — | — | E5 733–773; film 2 07:48:32–34 |
| 07:48:42.4 | **Disconnect** (Connection tab) | User | BD-24 | — | — | E5 774; B2273 |
| 07:48:44.8–07:49:03.5 | The ring tone is audible on the film's audio (3-s pulses) | Buds | BD-24 | — | — | film 2 audio |
| 07:48:54.6 | **Connect** → ready (ch 21, B2318); Find: "A ring was started on the Left earbud before the app reconnected — it may still be ringing. Tap Stop to end it." | User | BD-24 | — | — | E5 776; film 2 07:48:59 |
| 07:49:02.6 | **Stop** → `04 01 00 01 00` (B2416) → ACK `ff 01 00 03 04 01 00` (B2427) → Buds `04 01 00 01 00` (B2428); notice gone; tone ends after 07:49:03.5 | User | BD-24 | — | `FIND-001` | B2405–B2433; film 2 07:49:04 |
| 07:49:06.6 | Buds `DISC` MAESTRO (B2434) → "Still connecting…" → re-open (ch 19) | Buds → App | extra loss | — | — | E5 822–841 |
| 07:49:19–26 | Export (Debug mode on) → "Debug log saved (862 lines)." (E3) | User | (BD-25 pre) | — | — | film 2; E5 866 |
| 07:49:28 | **Debug mode off** | User | BD-25 | — | — | film 2 |
| 07:49:42–44 | ANC tab **Transparency** → Notify `08` → `Set 80` (B2629) → ACK (B2632) | User | BD-25 | — | `ANC-002` | B2605–B2639; film 2 |
| 07:49:47–54 | Export → "Debug log saved (869 lines)." (E4): **no hex line** after the switch (E4 = E3 + 7 state lines; the claim's 12 inbound messages are not dumped) | User | BD-25 | — | — | E4 vs E3; film 2 |
| 07:50:12–07:50:28 | Both buds out of the ears onto the table | User | BD-26 pre | — | — | film 2 |
| 07:50:29–30 | **Debug mode on** again | User | BD-26 pre | — | — | film 2 4 fps |
| 07:50:48 | Find → **Ring Left** → claim 14: `04 01 00 01 02` (B2721) → ACK (B2730) → Buds `04 01 00 01 02` (B2733); claim released 07:50:51.56 (B2736) | User | BD-26 | — | `FIND-001` | B2710–B2738; film 2 07:50:50 |
| 07:51:04–07:51:14 | A bud picked up from the table, a finger **touches** it in front of the camera; the tone stops between 07:51:12.4 and 07:51:14.2 (audio); **no frame on any channel** at that time (the Message Stream was closed; DLCI 2 only the runtime-info push B2744 07:51:11.375, HFP `+CIEV` B2753); the notice "Ringing: Left earbud — tap Stop to end it." **stays** (07:51:14–46) | User → Buds | BD-27 | — | `FIND-005` | film 2; B2736–B2766 |
| 07:51:47.5 | **Stop** → `04 01 00 01 00` (B2799) → ACK `ff 01 00 03 04 01 00` (B2810) — **no** Buds `04 01 00 01 00` this time; notice gone 07:51:49 | User | BD-28 | — | `FIND-001` | B2788–B2813; film 2 4 fps |
| 07:52:05.9 | **Disconnect**; buds put in the case, **lid closed** (07:52:10–16); ACL `0x13` (B2836 07:52:15.153); "Paired — not connected to this phone"; 07:52:50 the closed case carried out of the frame | User | BD-29 pre | — | — | E5 898–900; B2823–B2836; film 2 |
| 07:53:00.2 | **Connect** → phone `Create Connection` (B2837) → **`Connection Complete` status `0x04`** (page timeout, B2840 07:53:05.400), no RFCOMM → 07:53:05.4 "RFCOMM channel 0x02 connect failed after 5161 ms (attempt 1/3) …", "Failed(ChannelUnavailable)" → on screen (07:53:06): **"Couldn't open the app's channel to the Buds. Possible causes: the Buds are out of reach or in the closed case. Open the case and try again."** + `IOException: read failed, socket might closed or timeout, read ret: -1` + **Retry** | User | BD-29 | S10 | `PAIR-003` | E5 901–903; L 291–304; B2837–B2840; film 2 07:53:04–06 |
| 07:53:37–07:53:46 | Case brought back next to the phone; **Retry** (07:53:41) → page OK (B2842/B2844), ready (ch 21, B2943); 100 ⚡ / 78 / 100 ⚡ | User | BD-30 | — | `PAIR-003`, `BATT-004` | E5 904–942; film 2 |
| 07:53:52–58 | Debug tab (Debug mode on, "Unidentified frames (200)") → Export → "Debug log saved (942 lines)." (E5) | User | BD-end | — | — | film 2; L 398 |
| 07:54:00–10 | Status bar 07:53 → 07:54 on film; film 2 ends 07:54:10 | — | BD-end (P5) | — | — | film 2 4 fps |
| 07:54:16.8–17.9 | App paused and stopped (left the screen); logcat ends 05:54:17.956 UTC | User | (after the run) | — | — | L 410–425 |
| 07:55:19, 07:55:59 | The MAESTRO session is still open (runtime-info pushes B3532, B3556); HCI log B ends 07:56:06.297 | Buds | — | — | — | B |
| after 07:56:06 (?) | **Force-stop** — the maintainer's statement; not in any file | User | (not in the plan) | — | — | — |

## Step mapping (Appendix A → what happened)

| Step | Result | Where |
|---|---|---|
| P0 | done after the run (output in Log Metadata; exit status and positive control not recorded) | Log Metadata |
| P1 | done as 1.0.1 / `e1fc886` on film; "over 1.0.0, no uninstall" **not supported** by the logs (permissions asked again, 0 associations) | Log Metadata |
| P2 | done (Bluetooth off/on on film 07:23:01–06) | timeline |
| P3 | done for the phone and the case; **the head was in view only twice, never at a wear change** | Log Metadata, "Wear mapping" |
| P4 | not identifiable | — |
| P5 | done at both ends (minute flips 07:23 and 07:54 on film) | Log Metadata |
| P6 | done on film (07:24:11–43) | timeline |
| P7 | done, Info at 07:23:45 after the first ready; Debug mode on 07:23:54 (off before — first hex line 07:24:11) | timeline |
| BD-1 | done (07:25:50) | |
| BD-2 | **skipped** on the tab (Adaptive only from the tile, 07:26:44) | |
| BD-3 | done (07:26:00) | |
| BD-4 | done (07:26:16) | |
| BD-5 | done, **five** tile taps instead of four (07:26:37 … 07:27:06) | |
| BD-6 | done (both on the table 07:27:37; tile 07:27:51 and tab 07:28:08: no `Set`) | |
| BD-7 | **done differently**: Noise cancellation tapped while Transparency was shown (07:29:30) — not a tap on the current mode | |
| BD-8 | done (Off 07:29:40, Off again 07:29:46 → `Set` sent, ACKed) | |
| BD-9 | done (07:34:56) | |
| BD-10 | done (E1, 07:35:16–23) | |
| BD-11 | done (Enable Bluetooth; the app reconnected by itself, no Connect tap) | |
| BD-12 | done (E2) | |
| BD-13 | **repeated**: 07:30:31 (before section III; only the tile checked) and 07:37:00 (ANC, Sound, Controls and Find opened; the (i) texts not opened) | |
| BD-14 | **repeated**: 07:31:02 and 07:37:40 (the battery (i) read: S4 ✓); the ANC mark's disappearance not caught at 1 fps | |
| BD-15 | done (07:38:17) | |
| BD-16 | done, with Heavy bass and two more Flat taps (07:38:50 … 07:39:00) | |
| BD-17 | done; picker **three times** (two cancelled with nothing listed, the third offered the bonded Buds directly); the first film ended during the second attempt | |
| BD-18 | **not identifiable** (the Sound tab was not on screen within the first second after a ready) | |
| BD-19 | done twice (07:23:45, 07:42:33) | |
| BD-20 | done; a second tap not identifiable on film; one `SABM` | |
| BD-21 | **done differently**: a band written (07:43:44), *Read EQ again* not tapped; the value was read back at the next Connect (07:45:34) | |
| BD-22 | done (two Refresh taps; the bud's press-and-hold not on film) | |
| BD-23 | done (2 min 12 s) | |
| BD-24 | done; plus a Buds-side session loss during the ring (07:48:31) | |
| BD-25 | done (Debug off 07:49:28 → ANC tap → E4) | |
| BD-26 | done (07:50:48) | |
| BD-27 | done (bud touched 07:51:12–14); 15-s wait done (notice watched to 07:51:46) | |
| BD-28 | done (07:51:47) | |
| BD-29 | done (case closed and carried away; page timeout) | |
| BD-30 | done via **Retry** (07:53:41) | |
| BD-end | done (E5; minute flip on film) | |
| Not in the plan | pull-to-refresh to connect (07:29:22); the Find tab while disconnected (07:37:23); Heavy bass (07:38:50); the case and a bud gesture during the pairing (07:40:15–27, AVRCP PLAY); the force-stop (after the logs) | |

## Analysis checklist

- [x] Every filter scoped to handle `0x000b`; DLCIs identified by content (MAESTRO announcements on 0x02; `03 0a`/`03 01` on 0x04; AT commands on 0x0c/0x09).
- [x] P0 and Info: user 10, no `com.google.android.gms`/`com.android.vending` (P0 exit status not recorded — the wire negative carries it); 1.0.1 `e1fc886`; APK and certificate SHA-256.
- [x] Zero Play-services claims (0 in four logs; `CAP-066` 26).
- [x] I: 11 `Set`s on film with ACK and Notify (tab 4, tile 5, BD-25 1, BD-7′ 1); the Definition-of-done note — the maintainer's decision (`ai-sessions/0070` checkpoint).
- [x] II: a tap on the current mode sends a `Set` and the Buds ACK it (BD-8).
- [x] III: the two exports against the app's adapter lines and the HCI logs (no system log).
- [x] IV: BD-13/14 (S1 tile, S2 dimmed, S4 text), BD-15, BD-17, BD-19; BD-18 not identifiable.
- [x] V: one row per step (above; `APP_TESTPLAN.md` Summary updated).
- [x] VI: every `04 01`/`ff 01` frame by direction (`CAP-068-FINDINGS.md` §6).
- [x] VII: page timeout `0x04` and the text on film.
- [x] Traceability (`AGENTS.md` §13.7): `ANC-001` (BD-3, BD-5, BD-8), `ANC-002` (BD-1, BD-5, BD-25), `ANC-003` (tile only — tab skipped), `ANC-004` (BD-4, BD-5, BD-7′), `ANC-006` (BD-8), `PAIR-003` (BD-9…11, BD-13/14, BD-20, BD-29/30 and the losses), `BATT-004` (every connect), `EQS-001` (BD-15/16, BD-21), `FIND-001` (BD-24, BD-26, BD-28), `FIND-005` (BD-27 — no wire evidence, see FINDINGS §6), `INEAR-004` (BD-6) — each in the timeline.
- [x] `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `id_registry.csv`, `APP_TESTPLAN.md`, `CAP-068-FINDINGS.md`.

## Appendix A — the planned procedure (skeleton as committed in `e1fc886`; text unchanged, heading levels lowered by one)

## Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, a secondary user without Google Play) — Group BD, the 1.0.1 release APK (`CAP-068`)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0069`, 2026-10-03; scope is the maintainer's choice in chat 2026-10-03,
`AskUserQuestion` "Hotfix 1.0.1": *"1.0.1 with all of 0069 (Recommended)"* and "Leads": *"CAP-068 = release build; CAP-069 = official app
(Recommended)"*). After the run: rename this folder from the placeholder `CAP-068-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BD` to the film's first/last
overlay times and analyse it as `CAP-067` was (`ai-sessions/0067`).

**Purpose.** Only what the existing logs and films cannot answer (`ai-sessions/0069` RESULT, ledger):

- **I — an ANC `Set` by the release build** (`A68-GOV-01`). `PROJECT.md`'s Definition of done 2 is ticked on the maintainer's own test; no
  capture holds an `08 12` from a release-signed build (`CAP-067`: 0 × `08 12`, positive control `CAP-066`). Here: all four modes from the tab,
  then the Quick Settings tile, buds worn, on film.
- **II — a tap on the current mode** (`ANC-006`, lead L68-7): does OpenControl send a `Set`, and what do the Buds answer?
- **III — Bluetooth off/on with an export after each** (`CAP-067` BC-10 had no export afterwards — F-3 unverified).
- **IV — the 1.0.1 changes on hardware** (`ai-sessions/0069` Phases 6–7): values from an earlier connection are marked, not shown as current;
  no silent choice between two paired Buds and the "Use different Buds" action; the reworded messages; "—" for a setting that was not read; the
  EQ preset "Flat"; the version line.
- **V — `APP_TESTPLAN.md` steps never run on hardware** (`A68-SES-04`): C5, H5, F5, J4, I4, L3 with content, the K1 screen.
- **VI — Ring stopped on the bud, without Play services** (`FIND-005`, lead L68-2): the app sends nothing new on the wire for it (the
  maintainer's choice, "Ring status": *"Nothing new on the wire; test first (Recommended)"*) — this step records what the Buds send and what the
  app shows.
- **VII — the forced connect failure** (`A68-APP-10`): Connect with the Buds out of range.
- **Not in this run:** anything that needs the official app (→ `CAP-069`); the battery advertisement on case-open (→ `CAP-054`); K5 (the
  GrapheneOS auto-off setting is not available in the user without Play, `CAP-067`); the destructive steps A5/(E)/B4/Z1 (run in `CAP-067`).

If the run exceeds one sitting (about 25 minutes), stop after section IV and run V–VII as a second film in the same folder.

### Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-068` |
| Group(s) | BD (new) |
| Date | TBD |
| Phone | Pixel 9a, GrapheneOS — the secondary user without Google Play used for `CAP-067`; note its user id (P0) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.0.1** (versionCode 10001), the release APK signed with the maintainer's key (`scripts/release.sh`, `RELEASING.md`) — read from the **Info tab on film**: "App: 1.0.1, build <hash> (<date>)", no "-dirty"; APK SHA-256 and certificate SHA-256 from the script's output |
| Official Pixel Buds app | Not used. Pixel 7a: Bluetooth off |
| Buds | Pixel Buds Pro 2; firmware read from the Connection tab on film |
| Video file | TBD — camera film: the phone, the case and **your head** |
| Log files | TBD — `CAP-068-btsnoop_hci.log` and `.log.last` (Bluetooth is toggled in section III), the app's exports, logcat |

### Preparation

| # | Check | Done |
|---|---|---|
| P0 | In the test user: `adb shell am get-current-user`; `adb shell pm list packages --user <id> \| grep -i -E "gms\|vending"` — note the output **and the exit status** (1 = none); positive control: the same with `grep opencontrol` (exit 0). Save both outputs into this folder | ☐ |
| P1 | The **1.0.1 release** APK installed over 1.0.0 in the test user (same signing key — an update, no uninstall; note that Android accepted versionCode 10001 over 10000). Do **not** clear the app's data: section IV needs the stored association | ☐ |
| P2 | Bluetooth HCI snoop log on (set in the Owner, then switch to the test user); switch Bluetooth off and on **on film** | ☐ |
| P3 | Camera films the phone, the case and your head (ears visible for every wear step); head on the right of the frame = Left bud | ☐ |
| P4 | Do Not Disturb on | ☐ |
| P5 | Status bar on film across a minute change at the start and at the end (the clock offset is measured) | ☐ |
| P6 | Quick Settings of the test user: the ANC tile present and large | ☐ |
| P7 | Debug tab: **Debug mode on** (for the exports); after the first "ready": gear → **Info**, hold 3 s on the build line | ☐ |

**Rhythm:** one action, then wait 5–10 s. Something unexpected: stop, wait 10 s, continue. **Export the debug log before any step that ends the
process and after every Bluetooth off/on** (the `CAP-067` lesson). No narration. Before committing: check the film for a street-address overlay.

### Steps

DLCI numbers are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05); the bytes below are written for the even numbers.

#### I. ANC by the release build (`ANC-001`…`ANC-004`, `APP_TESTPLAN.md` F1–F4, G3)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BD-1 | both buds **worn**, ready, ANC tab | tap **TRANSPARENT** | "ANC mode: TRANSPARENT (updated …)" | claim of the Message Stream; `08 11 00 00` → Notify; `08 12 00 14 01 e8 e8 80 …` → ACK `ff 01 00 02 08 12` → Notify `08 13 00 04 01 e8 e8 80` | no `08 12`, or a NAK with both buds worn |
| BD-2 | — | tap **ADAPTIVE** | ADAPTIVE | `… 40 …` → ACK → Notify | as BD-1 |
| BD-3 | — | tap **OFF** | OFF | `… 20 …` | as BD-1 |
| BD-4 | — | tap **ACTIVE** | ACTIVE | `… 08 …` | as BD-1 |
| BD-5 | worn, Quick Settings open | tap the **ANC tile** four times, 5 s apart | the tile's label follows the mode | four `08 12` → ACK → Notify | a tap without `08 12` |
| BD-6 | — | take **both** buds out (on the table); tap the tile once; then the ANC tab | the tile and the tab say the Buds refuse a change while not worn | Notify with Settable `00`; if a `Set` is sent: NAK `ff 02 00 03 02 08 12` | an ACK with Settable `00` |

#### II. A tap on the current mode (`ANC-006`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BD-7 | both worn again, mode ACTIVE shown | tap **ACTIVE** | unchanged | record: a `Set` `… 08 …` or none; if sent, its answer (ACK, or NAK and its reason byte) | — (an observation; the result decides whether the app should skip the `Set`) |
| BD-8 | — | tap **OFF**, wait, tap **OFF** again | OFF | as BD-7 for the second tap | — |

#### III. Bluetooth off/on with exports (`PAIR-003`, `APP_TESTPLAN.md` K1/K2)

| Step | Pre-state | Action | Expected on screen | Expected on the wire / in the export | Refuted if |
|---|---|---|---|---|---|
| BD-9 | ready, Connection tab on screen | Quick Settings: Bluetooth **off**; back to the app | "Bluetooth is disabled." + **Enable Bluetooth**; Left/Right/Case lines no longer shown as current (section IV) | export line "Bluetooth adapter: ON -> TURNING_OFF"; the loss named "Bluetooth was switched off on this phone", without "(provisional …)" | a generic loss text, or "provisional" on that line |
| BD-10 | — | **Export debug log** now (Debug tab) | saved | the lines above are in the file | — |
| BD-11 | — | **Enable Bluetooth** → allow; wait; **Connect** if it does not connect by itself | ready | new announcement, `SubscribeRuntimeInfo`, battery | no ready within 30 s |
| BD-12 | — | **Export debug log** again | saved | "Bluetooth adapter: … -> ON" and the new session | — |

#### IV. The 1.0.1 changes (`ai-sessions/0069` Phases 6–7; `APP_TESTPLAN.md` section S — S1…S13 map to the steps below)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BD-13 | ready, buds worn, an ANC mode on screen (S1, S2) | tap **Disconnect**; open ANC, Sound, Controls | ANC: the last mode **dimmed**, (i) with the dot, in it "ANC mode: <MODE> — from the last connection (updated HH:MM:SS)"; Sound and Controls: values dimmed, each (i) starts "From the last connection — the app is not connected to the Buds now."; the tile says "Open the app" (the battery card is shown only while ready) | the session closes | a value of the closed session shown undimmed or without the (i) line |
| BD-14 | — (S3, S4) | tap **Connect**; ANC tab, then the battery card's (i) | until this connection's Notify the mode stays dimmed "— from the last connection", then current; a Case value not yet reported again reads "Case: NN% — last seen HH:MM:SS (last connection)" | announcement, battery, `08 11` → Notify | a mark stays after fresh data arrived, or the Case line says "(no bud charging in the case)" for a value from before this connection |
| BD-15 | ready (S5) | Sound tab: tap **FLAT** | the five sliders at 0.0 after the Buds' OK | one `WriteSetting 4:{16:{0.0 × 5}}` → OK — the payload of `CAP-015` frame 2111 | another value, or no write |
| BD-16 | — | tap the preset that was active before (note it) | — | its write | — |
| BD-17 | ready (S7, S8) | gear → **Settings**: read the "Use different Buds" text, tap the button; then Connection tab → **Pair a device** → pick the Buds | the session closes; the Connection tab shows **Pair a device** (with one pair of Buds bonded: "No Pixel Buds Pro 2 paired yet."); the tap opens Android's picker; after the choice: paired, Connect works | Disconnect; export line "Pairing: use different Buds — removed N association(s) of this app"; a new CDM association | the app reconnects to Buds without the picker having been shown |
| BD-18 | — (S6) | right after "ready": the Sound tab in the first second (film at normal speed) | each EQ band shows "—" until its read answers, Balance shows "—" instead of "Centre"; then the numbers | the `ReadSetting` answers of field 16 and 17 | "0.0" or "Centre" shown before the read answered |
| BD-19 | — | Info tab | "App: 1.0.1, build <hash> (<date>)" | — | another version, or "-dirty" |

Two paired Pixel Buds devices are needed to show the "no silent pick" sentence (S9: "More than one Pixel Buds device is paired with this phone. …"); with one pair this run shows only BD-17 — note S9 as not run. S11 (a tile toast for every failure) is part of BD-6; S12 (export across a rotation): rotate while the "save as" dialog of BD-10 is open.

#### V. Steps never run on hardware (`A68-SES-04`)

| Step | `APP_TESTPLAN.md` | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BD-20 | C5 | Disconnect; tap **Connect twice quickly** | one session, no error | one MAESTRO open (one `SABM`) | two opens, or an error text |
| BD-21 | H5 | Sound: drag a band, release; tap **Read EQ again** | the value just written | `WriteSetting` → OK; `ReadSetting` → the same five floats | the read differs |
| BD-22 | F5 | change the ANC mode with a **press-and-hold on a bud** (on film); tap **Refresh** | the bud's mode | a Notify from the Buds without a phone `Set`; `08 11` → Notify on Refresh | the screen keeps the old mode after Refresh |
| BD-23 | J4 | ready; press Home; wait **2 minutes**; reopen | still ready, or a clear message if the Buds closed the session | no app-side close in the two minutes | a silent loss (no message) |
| BD-24 | I4 | Find: **Ring Left**; **Disconnect** before Stop; **Connect**; **Stop** | after Disconnect the notice that a ring may still be sounding; after Stop it ends | `04 01 00 01 02` → ACK; after the reconnect `04 01 00 01 00` → ACK | the words differ from `APP_TESTPLAN.md` I4 |
| BD-25 | L3 | Debug mode **off**; do one ANC tap; export | — | the export has state lines and **no** hex line after the "Debug mode off" line | a hex line after it |

#### VI. Ring stopped on the bud (`FIND-005`, `FIND-001`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BD-26 | both buds on the table, ready, Debug mode on again | Find: **Ring Left** | "Ringing: Left earbud — tap Stop to end it." | `04 01 00 01 02`; Buds ACK `ff 01 00 03 04 01 00`; Buds' own `04 01 00 01 02` | — |
| BD-27 | ringing | stop it **on the bud** (touch it / pick it up, on film); do not tap Stop; wait 15 s | record what the notice does (1.0.1 does not read the Buds' status message — it may stay) | 🟡 Buds `04 01 00 01 00` with no phone command before it; the app sends **no** ACK `ff 01 00 02 04 01` (no Play services in this user) | a phone `04 01 00 01 00` precedes it |
| BD-28 | — | tap **Stop** | the notice disappears | `04 01 00 01 00` → ACK | — |

#### VII. A connect that fails (`A68-APP-10`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BD-29 | Disconnect tapped; both buds in the **closed** case, in another room (or the case closed for ≥ 60 s so the link is down) (S10) | tap **Connect** | within about 15 s: "Couldn't open the app's channel to the Buds. Possible causes: the Buds are out of reach or in the closed case. Open the case and try again." — no mention of another app; the notification may appear and disappear once | a page attempt ending in status `0x04` (page timeout), no RFCOMM | a spinner without end, "Unexpected error", a text naming another app, or a crash (system log: `ForegroundServiceDidNotStartInTimeException`) |
| BD-30 | — | open the case next to the phone; **Connect** | ready | — | — |
| BD-end | — | export the debug log; status bar across a minute change; stop the film | — | — | — |

### Don'ts

- Do not clear the app's data and do not uninstall 1.0.0 first (P1).
- Do not open the Owner user during the run (its Play services must stay in the background).
- Do not tap two things within 5 s of each other, except where a step says "quickly".

### After the run

The exports (test user's storage — `adb pull` or share), `adb bugreport` (covers all users), both `btsnoop_hci.log` files, app logcat and
system log, P0's two outputs, the film. All into this folder, then `sha256sum *`.

### Analysis checklist

- [ ] Scope every filter to the Buds' ACL handle; identify the DLCIs by content (`AGENTS.md` §13).
- [ ] P0 and the Info frame: user id, no `com.google.android.gms`/`com.android.vending` (exit status + positive control); "1.0.1", the build hash
      against `git log`, the APK and certificate SHA-256 against the script's output.
- [ ] Zero Play-services claims on the Message Stream (`03 08 00 02 01 25`): command, exit status, positive control in `CAP-066`.
- [ ] I: every `08 12` with its answer and the following Notify, against the tap on film. **Only then** may the note "maintainer-attested, not
      captured" on the Definition of done 2 (`PROJECT.md`, `RELEASING.md`) be replaced by this capture — the maintainer's decision.
- [ ] II: per tap on the current mode — `Set` sent or not, and the answer. Proposal for the app (skip the `Set`, or keep it) to the maintainer.
- [ ] III: the two exports against the system log's adapter `STATE_CHANGED` times.
- [ ] IV: film frames of BD-13/BD-14 (marks appear and disappear), BD-15's five floats, BD-17, BD-18, BD-19.
- [ ] V: one row per step — done / not done / done differently — and the `APP_TESTPLAN.md` Summary updated.
- [ ] VI: direction and order of every `04 01`/`ff 01` frame; what the app showed between BD-27 and BD-28.
- [ ] VII: the HCI status of the failed page and the app's text on film.
- [ ] Traceability (`AGENTS.md` §13 step 7): `ANC-001`…`004` (BD-1…5), `ANC-006` (BD-7/8), `PAIR-003` (BD-9…11, BD-13/14, BD-20, BD-29/30),
      `BATT-004` (every connect), `EQS-001` (BD-15, BD-21), `FIND-001`/`FIND-005` (BD-24, BD-26…28), `INEAR-004` (BD-6) — each referenced in
      the timeline or flagged.
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `id_registry.csv`, `APP_TESTPLAN.md`, and write `CAP-068-FINDINGS.md`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-068-2026-10-04_07-22-57_07-54-10-Group_BD/CAP-068-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-068-2026-10-04_07-22-57_07-54-10-Group_BD/CAP-068-EVENT-NOTES
