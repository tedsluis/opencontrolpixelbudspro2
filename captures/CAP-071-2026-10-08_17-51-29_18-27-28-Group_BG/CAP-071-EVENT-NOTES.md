# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, the secondary user without Google Play) — Group BG, the 1.1.1 release APK (`CAP-071`)

**Status:** ✅ **Captured 2026-10-08 and analyzed 2026-10-08** (`ai-sessions/0080`). One camera film with sound (17:51:29–18:27:28 overlay), four HCI snoop
logs, two app debug exports, one app logcat, and an extract of the system log; no screen recording, no second film, no `uiautomator` dumps. Folder renamed
from the placeholder `CAP-071-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BG` to the film's first/last **overlay** times; file modes set to 644; of the system log only
`CAP-071-logcat-extract.txt` is committed (the maintainer's choices in chat 2026-10-08). The analysis is `CAP-071-FINDINGS.md`.

**The run differs from the plan:** the film covers P0–P9 and sections I–IV only — **film 2 (BG-20 … BG-24: lead L-1, C12, S12, T11) was not made**. 1.1.0 was
installed into user 10 thirteen minutes before the update (17:40:13) and used once off film (a Connect at 17:42:03); the case lid was **closed** when 1.1.1
started, so BG-1 began with two failed Connect attempts; *Read EQ again* (H5) and the balance "Right 4" write were not done; Dark mode = System (BG-3) was set
after the rotation step; the ANC tile was first added to Quick Settings and then tapped five times; an **incoming phone call was answered on film**
(18:13:37–18:14:13); BG-17 happened twice (the first ended with a Connect tap, the second re-opened by itself). Nothing was said aloud. The planned procedure
(the committed skeleton) is kept unchanged as the appendix.

**Frame prefixes used below:** "Z" = `CAP-071-btsnoop_hci1.log.last` (17:37:32–17:51:34, before the film), "A" = `CAP-071-btsnoop_hci2.log.last` (17:51:39–18:26:23,
the film), "B" = `CAP-071-btsnoop_hci2.log` (18:26:32–18:28:44). `CAP-071-btsnoop_hci1.log` is a byte-for-byte prefix of A and is not counted. Exports: E1
`…-182336.txt`, E2 `…-182647.txt`; app logcat: L `CAP-071-OpenControl-for-Pixel-Buds-Pro-2-log-bed3be1a5dc7.txt`; system log: X = `CAP-071-logcat-extract.txt`
(line numbers of the extract). "Film" times are the burned-in overlay; **phone time = overlay + 1.25 s** (below).

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-071` |
| Group(s) | BG |
| Date | 2026-10-08 |
| Phone | Pixel 9a, GrapheneOS, Android 17 — L lines 2–3 `osVersion: google/tegu/tegu:17/CP3A.260905.009/2026100201:user/release-keys`, `userType: full.secondary`; the system log's bugreport name `bugreport-tegu-CP3A.260905.009-2026-10-08-18-23-51` (full log, local) |
| User and Google Play | User **10** (the maintainer's P0: `adb shell am get-current-user` → `10`). `pm list packages --user 10 \| grep -i -E "gms\|vending"` listed only `app.grapheneos.gmscompat.config`, `app.grapheneos.gmscompat.lib`, `app.grapheneos.gmscompat` — exit 0 because GrapheneOS's own compatibility-layer packages match the pattern (`CAP-070-FINDINGS.md` §0 has their manifests); no `com.google.android.gms`, no `com.android.vending`. Positive control `grep opencontrol` → `package:io.github.tedsluis.opencontrolpixelbuds`, exit 0. The wire agrees: no Play-services claim (`CAP-071-FINDINGS.md` §1). The **Owner (user 0)** does run Play: `android.vending` lines in the full system log, and the app is installed there too (process 26493, `u0a353`) |
| Install (P1) | 1.1.0 was **added to user 10 at 17:40:13** by an install session that also replaced the user-0 copy (full log: `PACKAGE_ADDED replacing: false … uid 1010353` and `replacing: true … uid 10353`, 17:40:14.155/.208) — so it was a first install in user 10, 13 minutes before the update. The update: `adb install --user 10 -r dist/1.1.1/opencontrol-pixelbudspro2-1.1.1.apk` → "Success" (adbd `abb_exec` 17:53:01.091/.112); dex2oat `app-version-name:1.1.1,app-version-code:10101` 17:53:02.692; `Killing 26327 … killDueToPackageUpdate` 17:53:12.422; `installation completed` 17:53:27.552 (full log, local; the extract keeps the `Killing`/`am_kill`/`am_proc_start` lines). `dumpsys … \| grep -A12 "User 10:"` before and after: `ceDataInode=343627 deDataInode=468510`, `firstInstallTime=2026-10-08 17:40:13` (both), the same data directory — the update kept user 10's data; `lastUpdateTime`/`versionCode` are outside the 12 lines `grep -A12` printed (the maintainer's output, below). The update's `installer_clear_app_data_caller` flags `39` = `0x27` = DE \| CE \| EXTERNAL \| `FLAG_CLEAR_CODE_CACHE_ONLY` — the code cache only (AOSP `IInstalld.aidl` and `InstallPackageHelper.java`, `CAP-071-FINDINGS.md` §0) |
| App under test | **1.1.0, build `0323849` (2026-10-06)** before the update — Info tab on film 17:52:08–20 (zoom: "App: 1.1.0, build 0323849 (2026-10-06)", no "-dirty", firmware from the announcement of 17:42:03); **1.1.1, build `86a6fb3` (2026-10-08)** after it — Info tab 17:58:00–30, 18:02:52, 18:03:52, 18:04:48, 18:05:18 ("App: 1.1.1, build 86a6fb3 (2026-10-08)", no "-dirty"); L header `package: io.github.tedsluis.opencontrolpixelbuds:10101, targetSdk 34` |
| Official Pixel Buds app | Not used (not in user 10) |
| Buds | Pixel Buds Pro 2, `04:00:6e:…:07`, firmware **`release_5.203`** for Case, Left and Right (Info tab; 11 of 11 announcements in Z/A/B, 33 of 33 entries) |
| Other devices | Bluetooth dialog shows "Charge 6" and "Niro" (saved, not connected); LE advertisements of other devices in Z (`49:44:…`, `58:54:…`); an LE link to `c8:cc:…` (handle `0x0040`, A3361 18:15:38 → A6984 18:26:23 `0x16`). No other device on the Buds' handle `0x000b` |
| Video file | `CAP-071-recording.mp4` — 2,158.67 s, H.264 1280×720 (displayed portrait) ≈ 29.86 fps, **AAC 44.1 kHz stereo**; overlay 17:51:29 (first frame) – 18:27:28 (last); `creation_time` 16:27:28Z = the film's **end**. A camera film (the phone, the case, hands; the head only at 18:06:56–58) |
| Screen recording | none (P4 not done) |
| HCI logs | Z 854 packets 17:37:32.758–17:51:34.615; `CAP-071-btsnoop_hci1.log` 6,894 packets 17:51:39.365–18:25:35.689 (= the first 319,218 bytes of A); A 6,996 packets 17:51:39.365–18:26:23.881; B 760 packets 18:26:32.311–18:28:44.260. Encapsulation `bluetooth-h4-linux`. Pulled by two `adb bugreportz` (18:23:51 and 18:27:24, full log) |
| Exports | E1 882 lines (17:53:29.631–18:23:28.787; toast "Debug log saved (882 lines)." film 18:23:40–42), E2 1,013 lines (…18:26:39.862; toast "… (1013 lines)." film 18:26:50–52). E1 = E2 lines 1–882 (Python comparison `True`). Both from process 28680 |
| App logcat | L, 671 lines, buffers `main,system,crash,events,kernel`, saved ≈ 18:27:37 (L669 `16:27:37.366 … Android link observer stopped`, after the film). Clock **UTC = phone − 2 h** exactly (L475 `16:26:23.148 … Bluetooth adapter: ON -> TURNING_OFF` = E2 951 `18:26:23.148`). PIDs 26327 (1.1.0) and 28680 (1.1.1) only |
| System log | `adb logcat -b all -v threadtime` started 17:55:53.737 (adbd line; after the update — the ring buffer still reaches back to 10-04 08:43); 221,518 lines, 33.5 MB, local time. **Committed: `CAP-071-logcat-extract.txt`** (47,628 lines): 17:35:00–18:29:00, the app's PIDs (26327, 26493, 28680), the Bluetooth process PIDs (984, 25526, 28198, 3964) and every line naming the app or `com.android.bluetooth`; command in `CAP-071-FINDINGS.md` §0. The full file holds personal data of the whole phone and stays local |
| Clock offsets | **Phone = overlay + 1.25 s (± 0.1 s)** at the start (status bar 17:51→17:52 at overlay 17:51:58.75, 8-fps strip) and at the end (18:26→18:27 at overlay 18:26:58.75); no drift; cross-check: "App control: ready" on film at overlay 17:57:29.51 = phone 17:57:30.76, E2 13 `17:57:30.765 … Discovering -> Ready`. Exports = HCI clock (E2 185 `18:08:12.967 RFCOMM channel 0x04 connected` / A2540 UA 18:08:12.960; E2 `03 0b …` 18:08:13.075 / A2549 18:08:13.070). L = phone − 2 h. X = phone (X40366 / E1 and L share the 18:24:30 SIGQUIT) |
| Wear and head side | The head is in view only at 18:06:56–58 (top-left, while the Right bud goes in); otherwise the ears are not on film (P3 not done). **Case slots: film-left = Left, film-right = Right** — the bud seated film-left at 18:15:34 is reported as the Left (A3297 18:15:35.54 `6.2` field 2 = 2), the one seated film-right at 18:16:16 as the Right (A3981 18:16:16.105 `6.3` = 2); the bud taken from film-right at 18:06:50 is the Right (A2330 18:06:51.66 `6.3` = 1). Whether a bud out of the case is in an ear is inferred (taken to the head, Settable `e8`), not seen |
| Buds MAC (partial, ADR-010) | `04:00:6e:…:07`, classic handle `0x000b` in every ACL (Z187, A171, A5025, A5626, A6419, B159) |
| Audio | Room noise (mean −72 dBFS); no music audible (it played in the Buds); the phone's ringtone 18:13:35–37 and short voice-like bursts during the call 18:13:40–18:14:10; the Buds' Find ring (Left 18:19:27.6–41, Right 18:19:59–18:20:13); **no case chime** at any of the five bud-into-case moments; nothing said about the steps. `CAP-071-FINDINGS.md` §0 |

### The maintainer's P0/P1 outputs (added to the skeleton after the run; copied unchanged)

Before the 1.1.1 install
```bash
$ adb shell am get-current-user; adb shell pm list packages --user 10 | grep -i -E "gms|vending"; echo "exit=$?"
10
package:app.grapheneos.gmscompat.config
package:app.grapheneos.gmscompat.lib
package:app.grapheneos.gmscompat
exit=0
$ adb shell pm list packages --user 10 | grep opencontrol; echo "exit=$?"
package:io.github.tedsluis.opencontrolpixelbuds
exit=0
$ adb shell am get-current-user
10
$ adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds | grep -A12 "User 10:"
    User 10: ceDataInode=343627 deDataInode=468510 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
      installReason=0
      dataDir=/data/user/10/io.github.tedsluis.opencontrolpixelbuds
      firstInstallTime=2026-10-08 17:40:13
      uninstallReason=0
      overlay paths:
        /product/overlay/NavigationBarMode3Button/NavigationBarMode3ButtonOverlay.apk
        /data/resource-cache/com.android.systemui-neutral-t3JX.frro
        /data/resource-cache/com.android.systemui-accent-gF4L.frro
        /data/resource-cache/com.android.systemui-dynamic-LLxR.frro
      legacy overlay paths:
        /product/overlay/NavigationBarMode3Button/NavigationBarMode3ButtonOverlay.apk
      runtime permissions:
--
    User 10:
      [com.google.android.iwlan,com.google.pixel.camera.services,com.android.dynsystem,android,com.android.localtransport,com.android.providers.settings,com.android.DeviceAsWebcam,com.android.inputdevices,com.android.server.telecom,com.android.keychain,com.android.settings,com.android.qns,com.android.location.fused]:
        io.github.tedsluis.opencontrolpixelbuds
      com.android.systemui:
        io.github.tedsluis.opencontrolpixelbuds
      [com.google.android.iwlan,com.google.pixel.camera.services,com.android.dynsystem,android,com.android.localtransport,com.android.providers.settings,com.android.DeviceAsWebcam,com.android.inputdevices,com.android.server.telecom,com.android.keychain,com.android.settings,com.android.qns,com.android.location.fused]:
        io.github.tedsluis.opencontrolpixelbuds
      com.android.inputmethod.latin:
        io.github.tedsluis.opencontrolpixelbuds
      com.android.launcher3:
        io.github.tedsluis.opencontrolpixelbuds
      android.ext.services:
        io.github.tedsluis.opencontrolpixelbuds
```
Install 1.1.1
```bash
$ adb install --user 10 -r dist/1.1.1/opencontrol-pixelbudspro2-1.1.1.apk
Performing Streamed Install
Success
✔ tedsluis@fedora ~/git/opencontrolpixelbudspro2 [release/1.1.1|✔] $ adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds | grep -A12 "User 10:"
    User 10: ceDataInode=343627 deDataInode=468510 pccCeDataInode=0 pccDeDataInode=0 installed=true hidden=false suspended=false appLockEnabled=false distractionFlags=0 stopped=false notLaunched=false enabled=0 instant=false virtual=false quarantined=false
      installReason=0
      dataDir=/data/user/10/io.github.tedsluis.opencontrolpixelbuds
      firstInstallTime=2026-10-08 17:40:13
      uninstallReason=0
      overlay paths:
        /product/overlay/NavigationBarMode3Button/NavigationBarMode3ButtonOverlay.apk
        /data/resource-cache/com.android.systemui-neutral-t3JX.frro
        /data/resource-cache/com.android.systemui-accent-gF4L.frro
        /data/resource-cache/com.android.systemui-dynamic-LLxR.frro
      legacy overlay paths:
        /product/overlay/NavigationBarMode3Button/NavigationBarMode3ButtonOverlay.apk
      runtime permissions:
--
    User 10:
      [com.google.android.iwlan,com.google.pixel.camera.services,com.android.dynsystem,android,com.android.localtransport,com.android.providers.settings,com.android.DeviceAsWebcam,com.android.inputdevices,com.android.server.telecom,com.android.keychain,com.android.settings,com.android.qns,com.android.location.fused]:
        io.github.tedsluis.opencontrolpixelbuds
      com.android.inputmethod.latin:
        io.github.tedsluis.opencontrolpixelbuds
  queryable via uses-library:

Dexopt state:
  [io.github.tedsluis.opencontrolpixelbuds]
    path: /data/app/~~x35dMFU8KMfd9jKKk9FqlQ==/io.github.tedsluis.opencontrolpixelbuds-zrKUBHHAVGvHLO2GpZeHHQ==/base.apk
      arm64: [status=speed] [reason=install] [primary-abi]
        [location is /data/app/~~x35dMFU8KMfd9jKKk9FqlQ==/io.github.tedsluis.opencontrolpixelbuds-zrKUBHHAVGvHLO2GpZeHHQ==/oat/arm64/base.odex]
```


## Capture-integrity pre-flight

- **HCI prefix:** `python3 -I -c "import sys;A=open(sys.argv[1],'rb').read();B=open(sys.argv[2],'rb').read();print(len(A),len(B),B[:len(A)]==A)" CAP-071-btsnoop_hci1.log
  CAP-071-btsnoop_hci2.log.last` → `319218 327010 True`; `cmp -n 319218 …` exit 0. A adds packets 6,895–6,996 (18:25:35.7–18:26:23.9). hci1.log was written by the
  first bugreport (18:23:51) while the snoop session was still running; hci2.log.last is the same session closed at the Bluetooth-off of 18:26:23.
- **Boundaries:** Z ends 17:51:34.615 (P2's Bluetooth off, film overlay 17:51:32–34) and A starts 17:51:39.365 (on; the Bluetooth process 28198 started 17:51:38.498,
  X), a 4.75-s gap with Bluetooth off; A ends 18:26:23.881 (BG-19's off: phone `DISC` A6919/A6969, ACL `0x16` A6990 18:26:23.773) and B starts 18:26:32.311 (on;
  process 3964 started 18:26:31.417, X). No Buds ACL exists across either gap (`bthci_evt.code==0x05` A6990 and the first Buds ACL of B at B159), so no Buds packet
  is lost or counted twice. Z itself begins 17:37:32.758, when Bluetooth was switched on in user 10 (process 25526 started 17:37:30.985).
- **Exports:** E1 = E2 lines 1–882; each export holds the ring buffer up to the tap on *Export* (E1's last line 18:23:28.787, tap ≈ 18:23:35; E2's last line
  18:26:39.862, tap ≈ 18:26:47). Both start with the new process's `Permissions (start)` line 17:53:29.631. **Debug mode on throughout**: hex lines from the first
  frame (E2 14 onward); the switch is on at the first view of the Debug tab (film 17:57:51) with no tap; the default is off (`DebugSettingsStore.kt:43`).
- **Processes (X, L):** 26327 = 1.1.0 in user 10 (`am_proc_start` X129 17:41:42.133, killed X3148 17:53:12.422 `killDueToPackageUpdate`); 26493 = the app in **user 0**
  for the tile (`bound-service … AncTileService`, X216 17:43:33.777; killed X3165 17:53:27.340 `installPackageLI (force-kill)`); 28680 = 1.1.1 in user 10 (X3240
  17:53:29.171, `top-activity`), alive to the end (`am_pss` 18:28:13.786). No force-stop of 28680 (BG-23 not done): `grep -c "Force stopping io.github.tedsluis
  .opencontrolpixelbuds" ` over the full log after 17:53:28 → 0 (the same pattern finds the 17:53:27.573 `pkg removed` line before it). No `FATAL`, `ANR in`,
  `am_crash`, "Decoder fault", "Unexpected error" (`grep -c` over E2, L and X → 0 each; positive control: `grep -c "Settings read"` → 12 in E2). Two pairs of "reacting to
  signal 3" / "Wrote stack traces to tombstoned" for 28680 (X40363/X40366 18:24:30, X47501/X47505 18:27:57) — from the two `bugreportz` runs (§0 of the FINDINGS).
- **Absent:** film 2 and BG-20 … BG-24; the screen recording (P4); the two `uiautomator` dumps (BG-23); spoken observations; `lastUpdateTime` in the P1 output.

## Video review method (and privacy)

The whole film at a fixed 2-s interval (108 contact sheets of 10 frames, film time burned in), every change narrowed at 1 s (2,159 frames) with a
frame-difference map to find every movement (quiet stretches 17:53:30–17:56:23 and 18:21:20–18:23:22 overlay confirmed by the map), zooms where a reading depends
on it (Info lines, tab row, toast, case slots), 5-fps and 8-fps strips for the first Connect and the clock flips. The audio as a 16-kHz mono track: per-second
level and spectrograms. **Privacy (every sheet and the audio):** no street-address overlay (date and time only); the maintainer's hair and glasses at 18:06:56–58;
Wi-Fi name and carrier in Quick Settings; Bluetooth names "Charge 6", "Niro"; public web pages (GitHub README and issues, a radio station's player); and **an
incoming phone call answered on film, 18:13:37–18:14:13 (phone): the caller's full number on the call screen and the call on the audio track**. The maintainer's
decision in chat 2026-10-08: *"Commit as is"* (the film) and *"Extract only (Recommended)"* (the system log). The number is not written into any document.

## Event Timeline

Times: phone clock (log times exact; film-only events = overlay + 1.25 s, rounded). "Step" = the skeleton's ID (appendix); U/C/O/S/T/H/K = `APP_TESTPLAN.md`.

| Phone time | Action / event | Actor | Step | Test-plan | Registry Test-ID | Evidence |
|---|---|---|---|---|---|---|
| 17:37:31 | Bluetooth on in user 10 (process 25526); pages to the Buds fail (`0x04` page timeout Z160, Z164, Z168) | maintainer / phone | — (not in plan) | — | — | Z153–Z168; X |
| 17:40:06–17:40:14 | **1.1.0 installed** — new in user 10 (`replacing: false`), replacing the user-0 copy | maintainer | P1 (differently: a fresh 1.1.0 in user 10, 13 min before the update) | U1 | — | full log 17:40:13.923 `installation completed`, 17:40:14.155/.208 |
| 17:41:42–17:42:21 | **1.1.0 opened, off film** (PID 26327, `wm_on_activity_result` 17:41:45.7 = a permission dialog); Connect → phone page Z185, ACL Z187, DLCI 2 `SABM` Z264, announcement Z294 ch **21**, reads 16, 2 … 29 (Z300–Z376), `SubscribeRuntimeInfo` Z377, claim Z293 (`08 11` Z308 → `Notify 01 e8 00 20` Z320); ACL `0x13` Z673 17:42:16.942 (Buds docked) | maintainer / app 1.1.0 | — (not in plan; P9 not kept) | — | `PAIR-003` | Z; L10–38; X133–212 |
| 17:43:33 | the app started in **user 0** for the tile (PID 26493, `u0a353`) | system | — | — | — | X216–217 |
| 17:51:08 | 1.1.0 back in front (`wm_on_restart`), before the film | maintainer | — (not in plan) | — | — | L39–46; X458–555 |
| 17:51:31 | **film starts** (overlay 17:51:29): Quick Settings' Bluetooth dialog; the case closed, both buds inside | — | P5 | — | — | film |
| 17:51:33 / 17:51:39 | Bluetooth **off**, then **on** | maintainer | P2 | — | — | film 17:51:32/38; Z ends 17:51:34.615; X634 "Bluetooth adapter: ON -> TURNING_OFF" (26327) 17:51:34.139; A starts 17:51:39.365 |
| 17:52:00 | status bar 17:51 → 17:52 | — | P5 | — | — | film (8-fps strip) |
| 17:52:03 | Recents → OpenControl **1.1.0**, light theme, Connection tab "Paired — not connected to this phone … tap Connect" | maintainer | P1 | U1 | — | film 17:52:02; L56–66 |
| 17:52:07–17:52:21 | gear → Settings (Dark mode System); **Info: "App: 1.1.0, build 0323849 (2026-10-06)"**, firmware from the announcement 17:42:03, Control channel 21 | maintainer | P1 | U1 | — | film zoom |
| 17:52:24–25 | **Dark mode → On** (the app turns dark). The Debug tab is not opened in 1.1.0 on film | maintainer | P1 (Debug-mode part not on film) | U1 | — | film |
| 17:53:01–17:53:28 | **1.1.1 installed over 1.1.0**: adb install; dex2oat `app-version-name:1.1.1,app-version-code:10101`; 26327 killed; the phone shows "Updaten…" (17:53:12–29); user-0 process 26493 killed; installation completed 17:53:27.552 | maintainer / system | P1 | U1 | — | full log (Log Metadata); X am_kill; film |
| 17:53:29.2–17:53:31 | **1.1.1 starts by itself** (PID 28680, `top-activity`): **dark** at once, Connection tab "Paired — not connected …", Connect (no session: Android `NOT_CONNECTED`, lid closed) | system / app | BG-1 | U1 | — | X3240; E2 1–3; film 17:53:30 |
| 17:54:17 | `dumpsys package …` after the update (the maintainer's P1 output) | maintainer | P1 | U1 | — | full log adbd 17:54:16.992 |
| 17:55:54 | system-log capture started (`logcat -b all`) | maintainer | P6 (after the update) | — | — | full log adbd 17:55:53.737 |
| 17:56:25–17:56:44 | Controls: "Not connected to the Buds. Controls are disabled …", **"—" for every switch**; Sound: the five EQ bands "—"; ANC: "ANC mode: unknown" | maintainer / app | — (not in plan) | S6/T11 (shown on screen, not dumped) | — | film zooms 17:56:26, 17:56:36 |
| 17:56:58–17:57:17 | **Connect** tapped with the lid closed → page timeout `0x04` (A160 → A163), "Couldn't open the app's channel …"; a second attempt 17:57:11.67 (A164 → A167) fails the same way; Controls shows "Still connecting…" and the red cause | maintainer / app | BG-1 (differently: P8 not kept) | C-section | `PAIR-003` | E2 4–9; film 17:56:56–17:57:20 |
| 17:57:23 | **case lid opened** (both buds inside) | maintainer | P8 (late) | — | — | film 17:57:22 |
| 17:57:28–17:57:32 | **Retry** → page A169, ACL A171, DLCI 2 `SABM` A248 → announcement A261 ch **21** → `ReadSetting 16`, then 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29 (A272–A343, 12–88 ms) → `SubscribeRuntimeInfo` A344; claim A282 (`08 11` A292, `Notify` A305 `01 e8 00 20`, `03 03 e4 e4 ff` ×3); "ready" at 17:57:30.76; the Controls tab already shows the read values at 17:57:30.96 (no "—" caught) | maintainer / app | BG-1 | U3 (not caught), T1 | `PAIR-003`, `BATT-004` | A169–A429; E2 10–34; film 5-fps strip |
| 17:57:45–17:57:58 | gear → Settings (Dark mode **On**); **Debug: Debug mode on** (no tap), "Unidentified frames (5)" | maintainer | BG-2 | U1 | — | film zoom 17:57:52 |
| 17:58:01–17:58:32 | **Info: "App: 1.1.1, build 86a6fb3 (2026-10-08)"**, no "-dirty"; firmware `release_5.203` ×3 (announcement 17:57:30); "Control channel: 21" | maintainer | BG-2 | U2 | — | film zoom 17:58:12 |
| 17:58:53–17:59:21 | ← to Connection: "Connected to this phone (Android) · App control: ready", **Left 100 % ⚡, Case 72 %, Right 100 % ⚡** (A298 `e4 e4`, A780 17:58:46 6.1 = 72); taps ANC ("The Buds don't allow changing noise control right now …", Off), Sound (3.7 / 2.0 / 4.0 / 0.0 / −1.0, Volume EQ on), Controls, Find | maintainer | BG-4 | U4 | `BATT-004` | film |
| 17:59:29–17:59:59 | **swipe** through the tabs, back and forth — each swipe changes the tab and the bottom bar follows | maintainer | BG-4 | U4, C10, O2 | — | film |
| 18:00:06.37 | **pull on Find** → `SubscribeRuntimeInfo` A976 + claim A997 (`08 11`, `03 03` ×3, `Notify 00`) | maintainer / app | BG-5 | U5 | `BATT-004` | E2 41–51 |
| 18:00:12.47 | **pull on Controls** → the twelve reads 2 … 29 (A1035–A1094) | maintainer / app | BG-5 | U5 | — | E2 76 |
| 18:00:18.73 | **pull on Sound** (spinner on film) → `ReadSetting 16` + the twelve reads (A1095–A1133) | maintainer / app | BG-5 | U5 | — | E2 79–104 |
| 18:00:27.65 | **pull on ANC** → claim A1156 (`08 11` → `Notify 01 e8 00 20`) | maintainer / app | BG-5 | U5 | — | E2 105–112 |
| 18:00:34.37 | **pull on Connection** → `SubscribeRuntimeInfo` A1183 + claim A1204 | maintainer / app | BG-5 | U5 | `BATT-004` | E2 113–123 |
| 18:00:47–18:02:07 | (i) dialogs: Battery ("Left: 100 % (updated 18:00:34) — charging in the case (18:00:36) … Case: 72 % (updated 18:00:36)"), Noise control, Equalizer, Balance and audio ("Balance: Right 4 · read 18:00:19"), Touch controls, Head gestures, In-ear detection, Multipoint, Case sounds — closed with Close and with back | maintainer | BG-6 (more dialogs than planned) | U6 | — | film |
| 18:02:33–18:03:13 | gear: tab row **Settings · Debug · Info**, the selected label with a **full-width** underline; Debug ("Unidentified frames (20)"); Info; **←** back to Controls; gear again, **system back** → Controls | maintainer | BG-7 | U7, O3 | — | film zoom 18:02:36 |
| 18:03:30–18:03:39 | **rotation** to landscape and back on **Sound** — the tab stays (E2 126 `Permissions (start)` 18:03:30.842 and 131 18:03:38.191 = two Activity re-creations, the same process) | maintainer | BG-8 | U8, K4 | — | film; E2 126–135; L |
| 18:03:50–18:04:07 | gear → Info; **rotation** on Info and back — Info stays (E2 136 18:03:58.757, 141 18:04:05.878) | maintainer | BG-8 | U8, K4 | — | film; E2 136–145 |
| 18:04:23–18:04:41 | Quick Settings, **"Tegels bewerken"**: a dark-theme tile added | maintainer | — (not in plan) | — | — | film |
| 18:04:44 | Android **dark theme on** via the tile (app already dark: Dark mode = On); notification shade "OpenControl for Pixel Buds Pro 2 · Connected — ANC: OFF" | maintainer | BG-9 (part, with Dark mode On) | U8 | — | film; E2 148 (re-creation 18:04:44.780) |
| 18:04:59–18:05:01 | Settings → **Dark mode "System (follows Android)"** | maintainer | BG-3 (late) | U8 | — | film |
| 18:05:05–18:05:09 | Android **dark theme off** → the app turns **light** at once, no restart (E2 153 re-creation 18:05:04.647, the same PID) | maintainer | BG-9 | U8 | — | film; E2 153–157 |
| 18:05:27–18:05:45 | Info → **Read the licence**: the full text, scrolled, Close | maintainer | BG-10 | U9 | — | film |
| 18:05:47–18:06:08 | **README on GitHub** → the browser (README page), back; **Report an issue on GitHub** → the issues page ("Sign in", no account), back. The app stops/starts its link observer only (E2 158–165); nothing else from the app | maintainer | BG-10 | U9 | — | film; E2 158–165 |
| 18:06:51 | the **Right** bud out of the case (A2330 18:06:51.66 6.3 = 1) and into the right ear (head in view at 18:06:57–59) | maintainer | BG-11 pre-state | — | `CASE-005` | film; A2330 |
| 18:07:07–18:07:09 | the **Left** out of the case (A2377 18:07:08.41 6.2 = 1, 6.1 gone), towards the head (out of view) | maintainer | BG-11 pre-state | — | `CASE-004` | film; A2377 |
| 18:07:19–18:07:39 | Home → the browser: a web-radio stream started (A2DP `START` A2462 18:07:26.406, streaming until A3091 18:13:34.786); back to the app | maintainer | P7 (differently: web radio) | — | — | film; A2391–A2462 |
| 18:08:13.14 | ANC tab: **Transparent** → claim A2538: `08 11` → `Notify 01 e8 e8 08` → `Set …80` A2560 → ACK A2563 → `Notify …80` | maintainer / app / Buds | BG-11 | U10 | `ANC-004` | E2 185–195; film 18:08:09–13 |
| 18:08:18.94 | **Adaptive** → `Set …40` A2617 → ACK A2620 | maintainer / app | BG-11 | U10 | `ANC-003` | E2 198–208 |
| 18:08:27.46 | **Off** → `Set …20` A2668 → ACK A2671 | maintainer / app | BG-11 | U10 | `ANC-001` | E2 209–219 |
| 18:08:37.86 | **Active** → `Set …08` A2720 → ACK A2722. Nothing said about the sound | maintainer / app | BG-11 | U10 | `ANC-002` | E2 220–230 |
| 18:08:49–18:09:22 | Quick Settings: the "Modi" tile hit by mistake (dialog closed); **"Tegels bewerken": the OpenControl ANC tile added** | maintainer | — (not in plan) | — | — | film |
| 18:09:26 – 18:09:53 | **ANC tile tapped five times**: ACTIVE→Transparent (A2780 → ACK A2783), →Adaptive (A2831/A2834), →Off (A2883/A2886), →Active (A2934/A2937), →Transparent (A2984/A2988); the tile text follows; each claim `08 11` first; E2 "ANC tile tapped (shown: …)" ×5; SystemUI `sysui_multi_action … AncTileService` (X21198 …) | maintainer / app (user 10) | BG-12 (×5) | U10 | `ANC-001` … `ANC-004` | E2 235–294; X; film |
| 18:10:05–18:10:09 | Home → OpenControl → Sound | maintainer | — | — | — | E2 297–300 |
| 18:10:18.37 | preset **BALANCED** → `WriteSetting 4:{16:[-3.5, 0.5, 1.0, -1.0, 2.5]}` A3000 → `RESPONSE` A3002; sliders 2.5 / −1.0 / 1.0 / 0.5 / −3.5 | maintainer / app | BG-13 | U11 | `EQP-001` | film; A3000 |
| 18:10:29.48 | **Upper treble** dragged to 6.0 → A3009 `5:6.00` → A3011 | maintainer / app | BG-13 | U11 | `EQS-001` | film; A3009 |
| 18:10:45–18:10:51 | (i) Equalizer "EQ updated: 18:10:29"; ***Read EQ again* not tapped** (no `ReadSetting 4:16` from A3011 to A3892) | maintainer | BG-13 (H5 not done) | H5, U11 | — | film; A |
| 18:11:19–18:11:23 | balance dragged to **Centre** → `4:{17:0}` A3013 → A3015 (it read "Right 4" = `17:7` before; **no "Right 4" write**) | maintainer / app | BG-13 (differently) | U11 | `AUDIO-003` | E2 309 |
| 18:11:30–18:12:01 | **Mono** on/off (A3022/A3025), **Conversation detection** off/on (A3028/A3031), **Volume EQ** off/on (A3034/A3043) — each `RESPONSE` OK; music playing; nothing said | maintainer / app | BG-13 | U11 | `AUDIO-001`, `CONV-001`, `AUDIO-002` | E2 314–331 |
| 18:12:14–18:12:44 | Controls: **touch** off/on (A3046/A3051); **Left: Digital assistant**, then **Noise control** (A3054/A3057); mode list **Off unticked** `4:{12:{1:1 2:0 3:1 4:0}}` A3060 → A3062 (NC and Transparency greyed, "At least two modes must stay selected.") | maintainer / app | BG-14 | U12 | `HOLD-005` | E2 334–346; film |
| 18:13:01–18:13:29 | **In-ear detection** off/on (A3063/A3066); **Use head gestures** off/on (A3075/A3078) | maintainer / app | BG-14 | U12 | `HEAD-001` | E2 349–360 |
| 18:13:35–18:14:16 | **incoming phone call**, answered; eSCO to the Buds A3114 18:13:35.236 → disconnected A3163 18:14:16.470 (`0x16`); A2DP suspended A3091; the app stops/starts its observer (E2 361–364) | third party / maintainer | — (not in plan; privacy) | — | — | A3091–A3163; film; audio |
| 18:14:19 | notification "OpenControl for Pixel Buds Pro 2 · Connected — ANC: TRANSPARENT" | — | (J) | — | — | film |
| 18:14:29–18:14:35 | **Multipoint** off/on (A3227 `…ad636bac` / A3232 `…3b536cdb`) | maintainer / app | BG-14 | U12 | `MULTI-001` | E2 367–370 |
| 18:14:53–18:15:03 | browser: the radio resumed (A2DP `START` A3266); back to the app | maintainer | — (not in plan) | — | — | film; A3266 |
| 18:15:14.25 | **Earbuds replaced off** → `4:{28:0}` A3288 → A3290 | maintainer / app | BG-14 | U12 | `CASE-001` | E2 379 |
| 18:15:35–18:15:43 | the **Left** into the case (A3297 6.2 = 2, Case 72) and out again (A3640); no `DISC` (session on 21); **no case chime on the audio**; nothing said | maintainer | BG-14 | U12 | `CASE-001`, `CASE-004` | film; A; audio |
| 18:16:13.30 | the **Right** into the case → **Buds `DISC` DLCI 2** (A3840), ACL up; Controls shows "—"; AFTER_LOSS re-open 18:16:14.84 → announcement A3885 ch **19** (only the Left out); A3981 6.3 = 2 | maintainer / Buds / app | BG-14 (Right instead of only the Left) | U12 | `CASE-001` | E2 392–446; film 18:16:12–17 |
| 18:16:25.37 | the Right out of the case (A3995), towards the head (out of view); no `DISC` (session on 19) | maintainer | — | — | `CASE-005` | A3995; film |
| 18:16:45.88 | **Buds `DISC` DLCI 2** (A4060) while the Right goes back to the ear (head out of view); AFTER_LOSS 18:16:47.40 → announcement A4105 ch **21** | Buds / app | — (not in plan) | — | `INEAR-*` (not identifiable) | E2 453–504 |
| 18:16:51–18:16:59 | the **Left** into the case and out again (A4186 6.2 = 2 → A4222 6.2 = 1); no `DISC` | maintainer | BG-14 (repeated) | U12 | `CASE-004` | A4186–A4225; film |
| 18:17:30–18:17:40 | **Other alerts** off/on (A4278/A4283); **Earbuds replaced on** (A4289) | maintainer / app | BG-14 | U12 | `CASE-002`, `CASE-001` | E2 523–529 |
| 18:17:46–18:17:53 | the **Left** into the case and out (A4300 → A4312), Earbuds replaced on — no chime on the audio | maintainer | BG-14 | U12 | `CASE-001`, `CASE-004` | A; audio |
| 18:18:02.72 | the **Right** into the case → **Buds `DISC`** (A4345) → AFTER_LOSS 18:18:04.24 → A4390 ch **19**; the Right out again 18:18:08.65 (A4490), no `DISC` | maintainer / Buds / app | BG-14 | U12 | `CASE-001`, `CASE-005` | E2 540–597; film |
| 18:18:40.48 | **Disconnect** (phone `DISC` A4526), 18:18:42.83 **Connect** → A4563 ch **19**: the twelve reads answer every last write (`2:1 4:1 7:{…5…5} 11:1 12:{1:1 2:0 3:1 4:0} 15:1 17:0 19:0 22:1 27:1 28:1 29:2`, 16 = Balanced + 6.0); Sound and Controls show them; Case 71 % | maintainer / app | BG-15 | U13 | `PAIR-003` | E2 604–651; A4547–A4642; film |
| 18:19:13–18:19:21 | both buds out of the ears onto the table | maintainer | BG-16 pre-state | — | `INEAR-004` (not identifiable) | film |
| 18:19:24.46 / 18:19:42.05 | **Ring Left** `04 01 00 01 02` A4705 → ACK A4709 → "Ringing: Left earbud — tap Stop to end it."; ring on the audio 18:19:27.6–41; **Stop** `… 00` A4791 → ACK A4798 | maintainer / app / Buds | BG-16 | U14 | `FIND-001` | E2 654–673; film; audio |
| 18:19:56.38 / 18:20:12.72 | **Ring Right** `… 01` A4872 → ACK A4882 (the bud held to the camera); ring 18:19:59–18:20:13; **Stop** A4967 → A4973 | maintainer / app / Buds | BG-16 | U14 | `FIND-002` | E2 674–693; audio |
| 18:20:25–18:20:29 | **both buds into the case** (lid open) → ACL `0x13` A5022 18:20:28.891; cause "Android's link to the Buds went down around the loss" (final); Find shows "Not connected to the Buds …"; **no automatic re-open** (Android `NOT_CONNECTED`) | maintainer / Buds / app | BG-17 | C8, U-K | `CASE-004`, `CASE-005` | E2 698–704; film |
| 18:20:31–18:20:36 | Connection: "Paired — not connected …", **Connect tapped** (E2 705, no "Automatic re-open" line) → page A5023 → A5127 ch **19** with both docked; "Android doesn't show the Buds as connected (yet) · App control: ready" | maintainer / app | BG-17 (differently: a tap) | C9 (not met here) | `PAIR-003` | E2 705–751; film |
| 18:20:48.97 | a bud out of the case → **Buds `DISC`** (A5473) → AFTER_LOSS → A5524 ch **21**; both buds out by 18:20:52 (A5604 both field 2 = 1) | maintainer / Buds / app | — (not in plan) | C9 | `CASE-005` | E2 754–805; film |
| 18:20:55.91 | buds back towards the case → **Buds `DISC`** (A5608): "Automatic re-open skipped: the session was lost 4963 ms after an automatic re-open" (ADR-044 chain guard); ACL `0x13` A5620 18:20:58.885 | maintainer / Buds / app | — | C9 | — | E2 806–812 |
| 18:21:08–18:21:11 | both buds out → the Buds' Connection Request A5622 → LINK_BACK re-open (attempt 1 fails after 95 ms, the multiplexer `SABM`s cross A5838/A5871; attempt 2 OK) → A5890 ch **21**; "connecting…", then ready **by itself** | maintainer / Buds / app | BG-17 (by itself, this time) | C9 | `CASE-004`, `CASE-005`, `PAIR-003` | E2 813–872; film |
| 18:21:18–18:23:26 | **Home** for 2 min 8 s (link observer stopped/started E2 873–880); back via Recents: **still ready** | maintainer | BG-18 | K1/K2 | — | film; E2 |
| 18:23:33–18:23:42 | Debug ("Unidentified frames (136)") → **Export** → Android's save dialog → "Debug log saved (882 lines)." = E1 | maintainer | BG-19 (export) | — | — | film; E2 883–889 |
| 18:23:51–≈18:25:35 | `adb bugreportz` on the computer (pulls hci1/hci1.last); dumpstate sends signal 3: "Wrote stack traces to tombstoned" for 28680 (X40366 18:24:30.506) — the app continued | maintainer / system | — (not in plan) | — | — | full log; X |
| 18:24:55–18:25:06 | **both buds into the case again** → Buds `DISC` A6391 18:24:57.73; AFTER_LOSS 18:24:59.25 attempt 1 fails (580 ms; ACL `0x13` A6416 18:24:59.82), attempt 2 pages the docked Buds (A6417 18:25:00.24 → ACL A6419) → A6514 ch **21**, ready **by itself** ("connecting…" → ready on film) | maintainer / Buds / app | BG-17 (repeated, by itself) | C8, C9 | `PAIR-003` | E2 890–944; film |
| 18:26:17–18:26:23 | notification shade ("Connected — ANC: OFF"); Bluetooth dialog: **Bluetooth off** → phone `DISC` HFP (A6919) and MAESTRO (A6969), ACL `0x16` A6990; "Session loss cause: Bluetooth was switched off on this phone" (final). "Bluetooth is disabled." not visible (the dialog covers the app) | maintainer / app | BG-19 | — | — | E2 951–957; film |
| 18:26:31–18:26:41 | **Bluetooth on** → LINK_BACK re-open by itself (B421 `SABM` 18:26:33.567 → B450 ch **21**, twelve reads B457–B529); the phone also opens HFP (DLCI 12, B259); back in the app: ready, Left 100 % ⚡, Case 71 %, Right 100 % ⚡ | maintainer / app | BG-19 | — | `PAIR-003` | E2 958–1009; B; film |
| 18:26:43–18:26:52 | Settings (Dark mode System) → Debug ("Unidentified frames (146)") → **Export** → "Debug log saved (1013 lines)." = E2 | maintainer | BG-end1 | — | — | film; L642 |
| 18:27:00 | status bar 18:26 → 18:27 | — | BG-end1 (P5) | — | — | film strip |
| 18:27:25–18:27:57 | the second `bugreportz` (pulls hci2/hci2.last); "Wrote stack traces to tombstoned" for 28680 (X47505) | maintainer / system | — | — | — | full log; X |
| 18:27:30 | **film ends** (overlay 18:27:28); then the app's log saved through Android's log viewer (L669 18:27:37) and the system log stopped (its last line 18:28:13.786) | maintainer | BG-end1 | — | — | film; L; full log |
| — | **BG-20 … BG-24 not done** (no film 2): lead L-1 with the head in view, C12, S12, T11's dumps | — | BG-20 … BG-24 | C12, S12, T11 | `INEAR-005`, `INEAR-004` | — |

## Step mapping

| Step | Result | Where |
|---|---|---|
| P0 | done (outputs below, exit statuses recorded) | before the film |
| P1 | done differently: 1.1.0 newly installed in user 10 at 17:40 and used off film; Dark mode On on film; Debug mode on not shown in 1.1.0 (on in 1.1.1 without a tap); Info 1.1.0 on film; `dumpsys` before/after without `lastUpdateTime`; installed from `dist/1.1.1/` | 17:40–17:54 |
| P2 | done | 17:51:33/39 |
| P3 | done differently: sound yes; the head in view once (18:06:57) | — |
| P4 | skipped | — |
| P5 | done (start and end) | 17:52:00, 18:27:00 |
| P6 | done differently: started 17:55:53, after the update (the buffer still covers it) | — |
| P7 | done differently: a web-radio stream | 18:07:26–18:16:27 |
| P8 | not kept: lid closed until 17:57:23 | — |
| P9 | not kept: 1.1.0 opened 17:41:42 (a session 17:42:03–16) and 17:51:08 before the film | — |
| BG-1 | done differently (lid closed: two failed Connects, then Retry); "—" in the first second not caught | 17:53:29–17:57:32 |
| BG-2 | done | 17:57:45–17:58:32 |
| BG-3 | done late (after BG-8) | 18:05:00 |
| BG-4 | done | 17:58:53–17:59:59 |
| BG-5 | done (five pulls; nothing said) | 18:00:06–18:00:34 |
| BG-6 | done (nine dialogs) | 18:00:47–18:02:07 |
| BG-7 | done | 18:02:33–18:03:13 |
| BG-8 | done | 18:03:30–18:04:07 |
| BG-9 | done (first with Dark mode On, then with System) | 18:04:44, 18:05:05 |
| BG-10 | done | 18:05:27–18:06:08 |
| BG-11 | done (during playback; nothing said) | 18:08:13–18:08:38 |
| BG-12 | done ×5 after adding the tile | 18:09:26–18:09:53 |
| BG-13 | done differently: no *Read EQ again* (H5), no "Right 4" write; nothing said | 18:10:18–18:12:01 |
| BG-14 | done (case-sound test with both buds and both states; nothing said; no chime recorded) | 18:12:14–18:18:09 |
| BG-15 | done | 18:18:40–18:18:59 |
| BG-16 | done | 18:19:24–18:20:13 |
| BG-17 | done ×2: first ended with a Connect tap, second re-opened by itself | 18:20:25–18:21:11, 18:24:55–18:25:06 |
| BG-18 | done | 18:21:18–18:23:26 |
| BG-19 | done (export 2.5 min before the Bluetooth off/on; "Bluetooth is disabled." not visible) | 18:23:33–18:26:41 |
| BG-end1 | done | 18:26:43–18:27:30 |
| BG-20 … BG-24 | skipped (no film 2) | — |
| Extra | 1.1.0 install and session off film; the dark-theme and ANC tiles added; "Modi" dialog; the phone call; the web radio; two channel moves at 18:16; the Left/Right case cycles; two `bugreportz` during the film | timeline |

**Test-IDs of the skeleton** (traceability, `AGENTS.md` §13 step 7): `PAIR-003` (17:42, 17:57, 18:18, 18:20, 18:21, 18:25, 18:26), `BATT-004` (17:57:30, 18:00:06,
18:00:34), `ANC-001` … `ANC-004` (18:08, 18:09), `EQP-001` (18:10:18 — the preset Balanced, not "Default"), `EQS-001` (18:10:29), `AUDIO-001`, `CONV-001`, `AUDIO-002`
(18:11–18:12), `AUDIO-003` (18:11:21), `HOLD-005` (18:12:43), `HEAD-001` (18:13), `MULTI-001` (18:14), `CASE-001` (18:15:14, 18:17:39 and the case cycles),
`CASE-002` (18:17:30/34), `CASE-004`, `CASE-005` (18:06:51, 18:07:08, 18:15–18:18, 18:20, 18:21), `FIND-001` (18:19:24), `FIND-002` (18:19:56) — each in the
timeline. **Expected but not observed:** `INEAR-005` (BG-20 not done); `INEAR-004` (buds taken out of the ears at 18:19:13–21 and 18:16 off camera — not
identifiable without the head in view or a wear indicator on the wire).

**`APP_TESTPLAN.md` IDs:** U1 partly (dark kept on film; Debug mode on, its 1.1.0 setting not on film; no crash; data dir kept), U2 ✓, U3 not caught (no
screen recording), U4–U10 ✓, U11 partly (no H5, no Right 4), U12 ✓, U13 ✓, U14 ✓; C8 ✓, C9 ✓ (18:21:09, 18:25:03; not at 18:20:28, where the link went down
with both buds docked), C10 ✓, C12 not done, O2 ✓, O3 ✓, S6 partly ("—" on screen while not connected and during the re-opens 18:16:14, 18:16:46, 18:18:02; not
in the first second after the first ready), S12 not done, T11 not done (no dump), K4 ✓, H5 not done.

## Analysis checklist

- [x] Every filter scoped to handle `0x000b` (Connection Complete Z187, A171, A5025, A5626, A6419, B159 for `04:00:6e:…:07`); DLCIs by content; negatives with
      command, exit status and positive control (`CAP-071-FINDINGS.md`).
- [x] P0/P1 interpreted (§0 of the FINDINGS); the Info frames against `git log` (`86a6fb3`) and `dist/1.1.1/` (checksums, certificate).
- [x] Zero Play-services claims: `tshark -r <log> -Y 'btrfcomm && frame contains 03:08:00:02:01:25'` → 0 in all four logs (exit 0 each); positive control
      `CAP-066-btsnoop_hci.log.last` → 22.
- [x] I: dark mode kept (film 17:53:30); Debug mode on (17:57:51); the reads per session (FINDINGS §2).
- [x] II: screens, tab row, back, rotation, dark switch, licence, links (FINDINGS §5).
- [x] III: every request against the reference table and earlier captures (FINDINGS §3); read-back of BG-15.
- [x] IV: session ends, re-opens, Bluetooth off/on (FINDINGS §6).
- [ ] V: not filmed — open.
- [x] The system log: no crash or ANR; the `SIGQUIT` sender identified (bugreport).
- [x] One row per step (above); `APP_TESTPLAN.md` U and the Summary updated; traceability done.

## Appendix — the planned procedure (the skeleton as committed by `ai-sessions/0079`, unchanged)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0079`, 2026-10-08). Scope chosen by the maintainer in chat 2026-10-08: a **broad
regression run** of the 1.1.1 release APK — the app of 1.1.0 rebuilt with the toolchain of `ai-sessions/0078` (Gradle 9.7.1, AGP 9.3.3 with built-in Kotlin,
Kotlin 2.4.20, Compose BOM 2026.09.00 / material3 1.4.0, AndroidX, Hilt 2.60.1; compileSdk 37, targetSdk and minSdk 34) — **installed over 1.1.0**, filmed
with sound, Android's screen recording and the system log, plus every open film item of `TODO.md` §2 (T11, H5, S12, S6, C12, lead L-1, the sound
observations). The maintainer films; a later CAPTURE session analyses it and gives the release verdict. After the run: rename this folder from the
placeholder to the film's first/last overlay times, and the film to CAP-071-recording.mp4.

**Purpose.** Nothing in the app's behaviour was meant to change (0078 RESULT: same tests, same mutations caught, same permissions and components). This run
checks that on the phone, where the new toolchain could show:

- **I — the update and the start.** 1.1.1 over 1.1.0 in user 10 (the first real update test: `CAP-070`'s 1.1.0 was a first install there, 🟡); the settings
  1.1.0 stored (dark mode, Debug mode) read back by DataStore 1.2.1; the Connect read of twelve settings; "—" in the first second (S6).
- **II — the screens** (Compose 1.7 → 1.12, material3 1.3 → 1.4): every tab by tap and by swipe, a pull on each, the (i) dialogs, the settings menu's look
  (`TabRow` kept, deprecated in 1.4.0), back from the menu, a rotation, Android's dark switch, the licence dialog (`LocalResources`) and the two links
  (`toUri()`).
- **III — every write once, byte for byte** (the codec is unchanged): ANC from the tab and the tile, an EQ preset and a slider, balance, mono, conversation
  detection, Volume EQ, touch controls, press and hold, the mode list, in-ear detection, head gestures, Multipoint, the two case sounds, Ring/Stop — each
  request equal to its reference (table below); the values read back after a reconnect. The preset **Balanced** and the mode list without **Off** also
  restore the Buds (`TODO.md` §4).
- **IV — robustness:** the case (the Buds close the session; the automatic re-open, ADR-044), Home for two minutes, Bluetooth off/on.
- **V — the open film items of `TODO.md` §2** (film 2): lead L-1 with the head in view (`INEAR-005`), C12 (a tap during a re-open), S12 (an export across a
  rotation), T11 (the screen-reader text from two `uiautomator` dumps).

The run is two films in this folder: **film 1** = P0–P9 and sections I–IV (≈ 30 min), **film 2** = section V (≈ 10 min). If time is short, film 2 can be
another day — the build stays the same; say so in the notes.

### Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-071` |
| Group(s) | BG (new) |
| Date | TBD |
| Phone | Pixel 9a, GrapheneOS (Android 17 in `CAP-070`) — the secondary user without Google Play (user 10 in `CAP-070`; P0) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.1.1** (versionCode 10101), the release APK built by `scripts/release.sh` and kept as `RELEASING.md` B3 says — on the **Info tab on film**: "App: 1.1.1, build <hash> (<date>)", no "-dirty"; APK and certificate SHA-256 from the script's output |
| Previous version in this user | 1.1.0 (`0323849`), installed 2026-10-07 for `CAP-070` — the update is from it |
| Official Pixel Buds app | Not used (not in this user). Pixel 7a: Bluetooth off |
| Buds | Pixel Buds Pro 2; firmware from the Info tab on film (`release_5.203` expected; any other ⇒ Safe Mode, stop and say so) |
| Video files | TBD — film 1 and film 2, **with sound** (P3) |
| Screen recording | TBD — Android's screen recorder in the test user, film 1 and film 2 (P4) |
| Log files | TBD — CAP-071-btsnoop_hci.log and .log.last (Bluetooth is toggled in P2, IV and V), the app's debug exports, the app logcat, the **system log** (P6), the two `uiautomator` dumps (BG-23), the P0/P1 outputs |

### Preparation

What `CAP-070` missed is marked ★ — please check those twice.

| # | Check | Done |
|---|---|---|
| P0 | In the test user: `adb shell am get-current-user`; `adb shell pm list packages --user <id> \| grep -i -E "gms\|vending"; echo "exit=$?"`; positive control: the same with `grep opencontrol` (exit 0). Save the outputs **with the exit statuses** into this folder ★ | ☐ |
| P1 | **Before the update, in 1.1.0** (on film): gear → **Settings → Dark mode On**; **Debug → Debug mode on**; Info shows "App: 1.1.0, build 0323849". Then `adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds \| grep -A12 "User <id>:"` → save. **Install 1.1.1 over it**: `adb install --user <id> -r dist/1.1.1/opencontrol-pixelbudspro2-1.1.1.apk` — no uninstall, no "clear data". The same `dumpsys` again → save ★ (`CAP-070` was a first install) | ☐ |
| P2 | Bluetooth HCI snoop log on (set in the Owner, then switch to the test user); switch Bluetooth **off and on on film** | ☐ |
| P3 | Camera films the phone, the case **and your head with both ears** at every wear step (head on the right of the frame = Left bud) ★; **the camera records sound** — play back 3 s before starting ★ (`CAP-070`'s film had no audio track) | ☐ |
| P4 | Android's **screen recorder** on (Quick Settings) ★ | ☐ |
| P5 | Status bar on film across a minute change at the start and at the end of each film | ☐ |
| P6 | **System log** ★: on the computer, before P2, `adb logcat -b all -v threadtime > CAP-071-logcat-all.txt` (stop it with Ctrl-C after the last export); it answers `CAP-070`'s open question — what sent the `SIGQUIT`s | ☐ |
| P7 | A short music track ready at **low volume** for the sound observations (BG-13, BG-14) | ☐ |
| P8 | Buds charged, both in the case, **lid open**; Android shows them connected before the app is opened (the app then opens its session by itself, ADR-044) | ☐ |
| P9 | **No rehearsal before the film** ★ (`CAP-070`'s logs hold an unfilmed one) — the app is not opened between P1's install and BG-1 | ☐ |

**Rhythm:** one action, then wait 5–10 s; **say** what you do at each wear change and at each observation (the film has sound now). Something unexpected:
stop, wait 10 s, say it, continue. **Export the debug log before any step that ends the process and after every Bluetooth off/on.** Before committing:
check the films for a street-address overlay, Wi-Fi names and other device names (`CAP-070` privacy note).

### Steps

DLCIs are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05). Every MAESTRO request starts `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25` on
**channel 21** or `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25` on **channel 19** (Info → "Control channel"). **Reference frames** — the 1.1.0 build's own
requests in `CAP-070` unless named (all re-derived in `ai-sessions/0079` RESULT §A.3, which lists every frame number):

| Request | Channel 21 | Channel 19 |
|---|---|---|
| `ReadSetting 4:16`, then 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29 | `CAP-070` A271, A296 … A329 | A966, A979 … A1035 |
| `SubscribeRuntimeInfo` | A332 `…90821ee66654bfab7e` | A1038 `…90821ee6602d65a97e` |
| Multipoint `4:{11:0}` / `{11:1}` | A752 `…2a0422025800ad636bac7e` / `CAP-069` 3212 `…2a04220258013b536cdb7e` | A3668 `…2a04220258009d8f9dc47e` / A3679 `…2a04220258010bbf9ab37e` |
| Head gestures `4:{29:1}` / `{29:2}` | A762 `…2a052203e80101bc106ba27e` / `CAP-069` 2564 `…2a052203e801020641623b7e` | A3614 `…2a052203e80101fcd6da847e` / A3685 `…2a052203e801024687d31d7e` |
| Earbuds replaced `4:{28:0}` / `{28:1}` | A770 `…2a052203e0010092717fdb7e` / `CAP-058` 5643 `…2a052203e00101044178ac7e` | `CAP-024` 1988 `…2a052203e00100d2b7cefd7e` / A1992 `…2a052203e001014487c98a7e` |
| Other alerts `4:{27:0}` / `{27:1}` | A777 `…2a052203d80100bac507f17e` / `CAP-058` 5697 `…2a052203d801012cf500867e` | `CAP-024` 2053 `…2a052203d80100fa03b6d77e` / A1995 `…2a052203d801016c33b1a07e` |
| Volume EQ `4:{15:0}` / `{15:1}` | A785 `…2a04220278000f47ef397e` / A4800 `…2a04220278019977e84e7e` | A3072 `…2a04220278003fab19517e` / A1953 `…2a0422027801a99b1e267e` |
| Balance Right 4 `4:{17:7}` | `CAP-064` 6671 `…2a052203880107a97d5edf037e` | A3747 `…2a052203880107e9b86e257e` |
| EQ preset Balanced `4:{16:[-3.5, 0.5, 1.0, -1.0, 2.5]}` | `CAP-059` 2188 `…2a1e221c8201190d000060c0150000003f1d0000803f25000080bf2d0000204008f7fa577e` | no captured frame — the app's codec output (unit tests) |
| Mode list without Off `4:{12:{1:1 2:0 3:1 4:0}}` | `CAP-041` 2198 `…2a0c220a62080801100018012000fc57b66a7e` | no captured frame — the app's codec output (unit tests) |
| ANC `Get` (each claim) | `08 11 00 00` (`CAP-070`, 11 of 11 claims) | |
| ANC `Set` Transparent / Off / Active / Adaptive | `CAP-068-btsnoop_hci.log` 1039 `08 12 00 14 01 e8 e8 80` + 16 × `00`; 1090 `…e8 e8 20…`; 1146 `…e8 e8 08…`; 1266 `…e8 e8 40…` | |
| Ring Left / Stop / Right | `CAP-068-btsnoop_hci2.log` 2142 `04 01 00 01 02`, 2416 `04 01 00 01 00`; `CAP-062-btsnoop_hci.log` 9660 `04 01 00 01 01` | |

A request with no row (the other settings writes: touch controls, press and hold, in-ear detection, mono, conversation detection, a slider value) is
compared with the app's unit-test fixture for the same request (`android/data/src/test`). **Any difference is the finding** — the codec did not change.

#### I. The update and the start (film 1; `APP_TESTPLAN.md` U1–U3, T1, S6; `PAIR-003`, `BATT-004`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BG-1 | P0–P9 done, 1.1.1 just installed over 1.1.0; screen recording on | open the app; within the first second go to **Controls** | **dark** (the 1.1.0 choice kept); no crash; the session opens by itself; for ≈ 1 s the unread switches show "—", then the Buds' values (S6, screen recording) | DLCI 0x02 open; the announcement; `ReadSetting 4:16`, then twelve reads in the order of the table, each answered; one `SubscribeRuntimeInfo` — each request equal to its reference | `PAIR-003`, `BATT-004` | a crash; light theme; a read missing, out of order or different; any other request |
| BG-2 | ready | gear → **Settings** (Dark mode shows **On**), **Debug** (Debug mode **on**), **Info** (hold 3 s on the build line and on "Control channel") | the two 1.1.0 choices kept; "App: 1.1.1, build <hash> (<date>)", no "-dirty"; firmware `release_5.203` ×3; channel 19 or 21 — say it | nothing sent | — | either choice lost; another version or "-dirty" |
| BG-3 | ready | gear → Settings → Dark mode **System** (leave it there) | the app follows Android's theme at once | — | — | — |

#### II. The screens (film 1; `APP_TESTPLAN.md` U4–U9, C10, O2–O8, O13, Q2, R1–R4, K4)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BG-4 | ready, Connection tab | tap each bottom tab in turn (Connection, ANC, Sound, Controls, Find); then **swipe** left through all and back | each tab as in `CAP-070`'s film (same cards, texts, order); the bottom bar follows each swipe | — | — | a missing or moved card, text cut off, a swipe that does not change the tab |
| BG-5 | ready | **pull down** on each tab, ≥ 5 s apart; say each time | a spinner while it runs, gone when done; Sound/Controls: the (i) "read" times move | Connection/Find: one DLCI 0x04 claim (`08 11`, the `03 03` burst); Sound/Controls: the twelve reads again; ANC: `08 11` → `08 13` | `BATT-004` | a spinner that never stops; a pull that sends nothing |
| BG-6 | ready | open the (i) on the Battery card, the ANC card, the Equalizer card and two Controls cards; close each with Close and once with back | a dialog with the times, Close and back close it | — | — | a dialog that does not open or close |
| BG-7 | ready | gear: look at the menu's tab row 3 s; tap Settings, Debug, Info; leave with the **←** arrow; open it again, leave with **system back** | tabs **Settings · Debug · Info**, the selected label in the accent colour with a **full-width** underline (as 1.1.0 — `TabRow` kept, `ai-sessions/0078`); ← and back return to the tab you came from | — | — | the underline only under the label, a grey selected label, another tab order, back leaving the app |
| BG-8 | ready, Sound tab | rotate to landscape and back; the same on Settings → Info | the same tab stays (`CAP-066` K4r fix); nothing lost | — | — | the tab resets |
| BG-9 | ready | Android's Quick Settings: **Dark theme** on, then off | the app follows each (Dark mode = System, BG-3) without a restart | — | — | the app does not follow, or restarts |
| BG-10 | ready | Settings → Info: **Read the licence**, scroll, Close; then **README on GitHub** and **Report an issue on GitHub** (back to the app after each) | the full licence text ("GNU AFFERO GENERAL PUBLIC LICENSE / Version 3, 19 November 2007 …"); the browser opens `…/blob/main/README.md` and `…/issues` | nothing from the app (the browser is another app) | — | an empty or cut dialog; a link that does nothing |

#### III. Every write once, buds worn, head in view (film 1; `APP_TESTPLAN.md` U10–U14, F1–F4, G3, H2–H6, M2–M6, N2–N11, I1–I3, T3–T8)

Both buds **in your ears** and the head in view unless a step says otherwise; music playing at low volume for BG-13/BG-14. Every write: the control moves
only after the Buds' OK ("changed HH:MM:SS" in the (i)).

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BG-11 | worn | ANC tab: **Transparent**, **Adaptive**, **Off**, **Active**, 8 s apart; say what you hear | each mode after its ACK; audible | per tap one claim: `08 11 00 00` → `Notify`, then the `Set` = the reference (`…80`, `…40`, `…20`, `…08`) → ACK `ff 01` | `ANC-004`, `ANC-003`, `ANC-001`, `ANC-002` | a `Set` that differs, a NAK while worn, no `Get` first |
| BG-12 | worn | Quick Settings: the **ANC tile** once; then pull the notification shade | the next mode after the Buds' reported one; the notification "OpenControl for Pixel Buds" shows it | one claim `08 11` → `Notify` → `Set` → ACK | `ANC-001`…`ANC-004` | the tile does nothing or opens the app while ready |
| BG-13 | worn, music | Sound: preset **Balanced** (restores the Buds, `TODO.md` §4); drag **Upper treble** up, release; **Read EQ again** (H5); balance to **Right 4**, then **Centre**; **Mono audio** on/off; **Conversation detection** off/on; **Volume EQ** off/on — say what you hear at each | each after its OK; *Read EQ again* shows the value just written; "Right 4", "Centre" | Balanced = `CAP-059` 2188 on 21 (on 19: the codec's output); one `WriteSetting 4:{16:…}` on release; `ReadSetting 4:16` = the write; `17:7`, `17:0`; `19:1`, `19:0`; `22:0`, `22:1`; `15:0`, `15:1` | `EQP-001`, `EQS-001`, `AUDIO-003`, `AUDIO-001`, `CONV-001`, `AUDIO-002` | a request that differs; a read that differs from its write |
| BG-14 | worn | Controls: **Use touch controls** off/on; press and hold **Left: Digital assistant**, then **Noise control**; mode list: **untick Off** (restores the Buds); **In-ear detection** off/on; **Use head gestures** off/on; **Multipoint** off/on; **Earbuds replaced off** → take the **Left** out of the ear into the case and out again → **say whether the case sounded** → **on** → the same → say (wait for "ready" after each bud change); **Other alerts** off/on | each after its OK | `4:{4:0}`/`{4:1}`; `4:{7:{1:{4:{1:6}}}}`/`{1:5}`; `4:{12:{1:1 2:0 3:1 4:0}}` (= `CAP-041` 2198 on 21); `2:0`/`2:1`; `29:1`/`29:2`; `11:0`/`11:1`; `28:0`/`28:1`; `27:0`/`27:1` — each = its reference | `HOLD-005`, `HEAD-001`, `MULTI-001`, `CASE-001`, `CASE-002`, `CASE-004` | a request that differs; the mode list keeps Off |
| BG-15 | worn | **Disconnect**, then **Connect**; open Sound and Controls | every value just written read back ("read …"): Balanced, Centre, mono off, conversation on, Volume EQ on, touch on, Left Noise control, mode list without Off, in-ear on, head gestures on, Multipoint on, both case sounds on | the twelve reads; each answer = the last write | `PAIR-003` | a read that differs from the last write |
| BG-16 | both buds out of the ears, on the table | Find: **Ring Left**, **Stop**, **Ring Right**, **Stop** | the notice and the ring follow each tap | `04 01 00 01 02` → ACK; `… 00`; `… 01`; `… 00` (the references) | `FIND-001`, `FIND-002` | a ring that does not stop |

#### IV. Robustness (film 1; `APP_TESTPLAN.md` C8, C9, J4, K1, K2, R6)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BG-17 | ready | both buds into the case, lid open; wait 10 s; take both out again (do not tap Connect) | the session closes with the 1.0.1 cause text; then ready **by itself** | Buds `DISC` 0x02 (or the ACL drops); the app's `SABM` 0x02 ≈ 1.5 s after the link is back (ADR-044) | `CASE-004`, `CASE-005`, `PAIR-003` | no automatic re-open while the app is on screen |
| BG-18 | ready | **Home**; wait 2 minutes; return to the app | still ready, or a clear message and a re-open; no crash | — | — | a crash |
| BG-19 | ready | **Export** the debug log; Quick Settings **Bluetooth off**; wait 10 s; **Bluetooth on** | "Bluetooth is disabled.", then ready again by itself | export lines "Bluetooth adapter: ON -> TURNING_OFF", "Session loss cause: Bluetooth was switched off on this phone"; after on, the automatic re-open and the twelve reads | `PAIR-003` | a crash; no re-open |
| BG-end1 | — | **Export** the debug log; status bar across a minute change; stop film 1 and the screen recording | "Debug log saved (N lines)." | — | — | — |

#### V. The open film items of `TODO.md` §2 (film 2; `APP_TESTPLAN.md` C12, S12, T11; `INEAR-005`)

Start film 2 and the screen recording; keep the system log running (P6).

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BG-20 | both buds in the case | take **only the Left** out, into the left ear (Info: "Control channel: 19"); then the **Right** into the right ear; wait 10 s (Info again); **with both worn on 19**, take the **Left** out of the ear and hold it in view — **head and both ears in view, say "Left out"** | the session closes and re-opens by itself; Info shows the new channel | 🟡 predicted (lead L-1, `PROTOCOL.md` §2.2a Updates of 2026-10-01/03/07): Buds `DISC` of MAESTRO with the ACL up, then an announcement `10 15` (21) | `INEAR-005`, `INEAR-004` | no `DISC`, or `DISC` followed by 19 again (if the Right's insertion already moved the channel to 21, say so and put it back in the case, retry once) |
| BG-21 | ready, both worn, Controls tab | put the **Right** bud into the case; **as soon as** the card shows that the app's channel was closed (≈ 0.5–1.5 s), **tap Multipoint** — before "ready" | "The setting was not changed: The app's channel is being reopened — try again in a moment."; the switch unchanged; after "ready" nothing is sent by itself | Buds `DISC` 0x02 → app `SABM` ≈ 1.5 s later; **no** `WriteSetting` for that tap | `CASE-004` | a write sent later by itself (if the tap comes after "ready", say so — C12 stays open) |
| BG-22 | ready | Debug → **Export debug log**; **rotate the phone while Android's "save as" dialog is open**; save (S12) | "Debug log saved (N lines)."; the file has content | — | — | an empty file, no toast |
| BG-23 | ready | **Export** again; Quick Settings **Bluetooth off**; on the computer `adb shell am force-stop io.github.tedsluis.opencontrolpixelbuds` (say it); open the app; **Controls** → `adb shell uiautomator dump /sdcard/CAP-071-controls.xml`; **Sound** (scroll to the Balance card) → `adb shell uiautomator dump /sdcard/CAP-071-sound.xml`; `adb pull` both into this folder (T11) | "Bluetooth is disabled."; Controls: "—" in place of every switch; Sound: the five EQ bands, Volume EQ, balance, mono audio and conversation detection "—" | — | — | in a dump, a node with `text="—"` whose `content-desc` is not `Not read from the Buds yet` |
| BG-24 | — | **Bluetooth on** → the app re-opens the session by itself (or **Connect**); **Export**; stop the system log (P6); status bar across a minute change; stop film 2 and the screen recording | ready; the values back | the twelve reads | `PAIR-003` | — |

### Don'ts

- Do not uninstall 1.1.0 and do not clear the app's data (P1 — the run is an update).
- Do not open the app before the film runs (P9); do not open the Owner user during a film.
- Do not tap two things within 5 s of each other, except in BG-21 (the early tap).
- Do not leave the buds in the case when a step says "worn" (`CAP-070` did BF-4…BF-7 with the buds in the case) — say it if a step is done differently.

### After the run

Into this folder: both films, both screen recordings, the system log (CAP-071-logcat-all.txt), every debug export (`adb pull` from the test user's
storage), the app logcat if saved, both btsnoop_hci.log files, the two `uiautomator` dumps, the P0/P1 outputs (before and after the update), and
`dist/1.1.1/`'s `.sha256` and the script's printed certificate line. Then `sha256sum *` into a file. Commit on the release branch `release/1.1.1`, after the
build commit (`RELEASING.md` C3) — never on `main` (it takes no direct push).

### Analysis checklist

- [ ] Scope every filter to the Buds' ACL handle (show the Connection Complete for the Buds' address with that handle); DLCIs by content (`AGENTS.md` §13);
      for a negative, the command, its exit status and a positive control (step 8).
- [ ] P0 (exit statuses, positive control), P1: user id; no `com.google.android.gms` / `com.android.vending`; **the update**: user `<id>`'s `firstInstallTime`
      unchanged from before and `lastUpdateTime` later, `versionCode=10101`; the Info frame "1.1.1, build <hash>" against `git log` and the B1 hash; APK and
      certificate SHA-256 against `dist/1.1.1/` and `RELEASING.md` (`a7530f5c…c79d8dcb`).
- [ ] **The release APK against 1.1.0's:** `aapt2 dump badging` of `dist/1.1.1/…apk` and `~/opencontrol-1.1.0-tested/…apk` — permissions identical (no
      `INTERNET`), `targetSdkVersion:'34'`, `sdkVersion:'34'`, `compileSdkVersion='37'`, the two `uses-library-not-required` (androidx.window); `aapt2 dump xmltree`
      — no new `<activity>`, `<service>`, `<receiver>`, `<provider>`.
- [ ] Zero Play-services claims on the Message Stream (`frame contains 03:08:00:02:01:25`): command, exit status, positive control `CAP-066`.
- [ ] I: dark mode and Debug mode kept across the update (film, BG-1/BG-2); the twelve reads per session in order with their answers (`python3 -I
      scripts/pwrpc_decode.py --handle <handle> <log>`); S6 on the screen recording (frame time of the first "—" and of the values).
- [ ] II: per step on film and the screen recording — same screens as `CAP-070`; the settings menu's tab row (full-width underline, accent label); back;
      rotation; dark switch; licence; links.
- [ ] III: **every request against the reference table** (byte for byte, per channel; the others against the unit-test fixtures); every `RESPONSE`/ACK;
      the read-back of BG-15; the sound observations quoted as said (BG-11, BG-13, BG-14).
- [ ] IV: each session end with its logged cause; the automatic re-opens; Bluetooth off/on.
- [ ] V: BG-20's `DISC` direction and announced channel, with the head side on film — the L-1 result is a **proposal** for `PROTOCOL.md` §2.2a, no status
      change by the analysing session alone; BG-21 — no `WriteSetting` after the early tap, the text on the screen recording; BG-22's file; **the two dumps**
      (count `text="—"` nodes and their `content-desc` per tab).
- [ ] The system log: crashes, ANRs, `StrictMode` (none expected in a release build), and **what sent any `SIGQUIT`** (`CAP-070`'s open question).
- [ ] One row per step — done / not done / done differently; `APP_TESTPLAN.md` section U and the Summary updated; traceability (`AGENTS.md` §13 step 7):
      `PAIR-003` (BG-1, BG-15, BG-17, BG-19, BG-24), `BATT-004` (BG-1, BG-5), `ANC-001`…`ANC-004` (BG-11, BG-12), `EQP-001`, `EQS-001`, `AUDIO-001`…`003`,
      `CONV-001` (BG-13), `HOLD-005`, `HEAD-001`, `MULTI-001`, `CASE-001`, `CASE-002`, `CASE-004` (BG-14, BG-17, BG-21), `CASE-005` (BG-17), `FIND-001`,
      `FIND-002` (BG-16), `INEAR-004`, `INEAR-005` (BG-20) — each referenced in the timeline or flagged "expected but not observed".
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `id_registry.csv`, `APP_TESTPLAN.md`, `TODO.md` §2/§4 (the restored Buds, the items done), and write
      `CAP-071-FINDINGS.md`; the release verdict for 1.1.1 is the analysing session's, with the maintainer.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-071-2026-10-08_17-51-29_18-27-28-Group_BG/CAP-071-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-071-2026-10-08_17-51-29_18-27-28-Group_BG/CAP-071-EVENT-NOTES
