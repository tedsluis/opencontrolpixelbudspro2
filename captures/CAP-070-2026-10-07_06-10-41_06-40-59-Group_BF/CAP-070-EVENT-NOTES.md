# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, a secondary user without Google Play) — Group BF, the 1.1.0 release APK (`CAP-070`)

**Status:** ✅ **Captured 2026-10-07 and analyzed 2026-10-07** (`ai-sessions/0075`). One camera film (06:10:41–06:40:59 overlay), four HCI snoop logs, four
app debug exports, two app logcats; no system log, no screen recording, no `uiautomator` dumps. Folder renamed from the placeholder
`CAP-070-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BF`, the film → `CAP-070-recording.mp4`, file modes 644 (maintainer-approved in chat 2026-10-07). The
analysis is `CAP-070-FINDINGS.md`. **The run differs from the plan:** the five switches were first switched off with both buds **in the case** (not worn);
the "on" pass ran on channel **19**; BF-13's early tap, *Read EQ again* (H5), the rotation (S12), the head in view (P3) and the screen recording (P4) were
not done; an **unfilmed rehearsal** (05:44–05:59) precedes the film and is in the logs. The planned procedure (the committed skeleton) is kept unchanged as
the appendix.

**Frame prefixes used below:** "Z" = `CAP-070-btsnoop_hci1.log.last` (before the film), "A" = `CAP-070-btsnoop_hci2.log.last` (film part 1), "B" =
`CAP-070-btsnoop_hci2.log` (after Bluetooth on). `CAP-070-btsnoop_hci1.log` is a byte-for-byte prefix of A and is not counted. Exports: E1 `…-063244.txt`,
E2 `…-063339.txt`, E3 `…-063647.txt`, E4 `…-063952.txt`; logcats: L1 `…-log-e106d62edae5.txt`, L2 `…-log-807f70280a2a.txt`.

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-070` |
| Group(s) | BF |
| Date | 2026-10-07 |
| Phone | Pixel 9a, GrapheneOS, Android 17 — logcat header `osVersion: google/tegu/tegu:17/CP3A.260905.009/2026100201:user/release-keys`, `userType: full.secondary` (L1/L2 lines 2–3) |
| User and Google Play | User **10** (maintainer's P0 output: `adb shell am get-current-user` → `10`). `pm list packages --user 10 \| grep -i -E "gms\|vending"` listed only `app.grapheneos.gmscompat.config`, `app.grapheneos.gmscompat.lib`, `app.grapheneos.gmscompat` — GrapheneOS's own compatibility layer (the manifests of `github.com/GrapheneOS/platform_packages_apps_GmsCompat`, branch `17`, declare `package="app.grapheneos.gmscompat"` and `"app.grapheneos.gmscompat.lib"`, the config holder `namespace = "app.grapheneos.gmscompat.config"`), not Google Play: no `com.google.android.gms`, no `com.android.vending`. Exit status not recorded. The wire agrees (no Play-services claim, `CAP-070-FINDINGS.md` §1) |
| Install (P1) | Maintainer's output: `versionCode=10100`, `lastUpdateTime=2026-10-07 05:26:38`, `firstInstallTime=2026-10-04 15:22:42`, `firstInstallTime=2026-10-07 05:26:38`. AOSP `Settings.dumpPackageLPr` prints one `User <id>:` block per user in user-id order, each with its own `firstInstallTime` → the second line is user 10's: **1.1.0 was a first install in user 10, not an update over 1.0.1** (🟡, `CAP-070-FINDINGS.md` §0) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.1.0, build `0323849` (2026-10-06)**, no "-dirty" — Info tab on film 06:30:54 (also 06:12:28–54, 06:24:14, 06:28:38–06:29:44); logcat `package: io.github.tedsluis.opencontrolpixelbuds:10100` |
| Official Pixel Buds app | Not used (not installed in this user) |
| Buds | Pixel Buds Pro 2, `04:00:6e:…:07`, firmware **`release_5.203`** for Case, Left and Right (Info tab 06:30:54; 22 announcements, all entries `release_5.203`) |
| Other devices | Bluetooth list shows "Charge 6" (connected at 06:10:42) and "Niro" (saved); an LE link to another device (handle `0x0040`, Z3077 05:55:30) ended at Bluetooth off (Z4623). No other device on the Buds' handle `0x000b` |
| Video file | `CAP-070-recording.mp4` — 1,817.27 s, 1280×720 H.264 ≈ 29.85 fps; overlay 06:10:41 (first frame) – 06:40:59 (last frame); `creation_time` 04:40:59Z = the film's **end**. The sound track has no sample description (`stsd` absent in its `trak`; `ffprobe` `codec_name=unknown`, `sample_rate=0`) — **no audio** |
| Screen recording | none (P4 not done) |
| HCI logs | Z 4,664 packets 05:44:02.521–06:10:46.252; `CAP-070-btsnoop_hci1.log` 5,013 packets 06:10:52.690–06:36:08.491 (= the first 222,764 bytes of A); A 5,285 packets 06:10:52.690–06:37:28.403; B 1,430 packets 06:39:36.576–06:42:06.705. Encapsulation `bluetooth-h4-linux` |
| Exports | E1 958 lines (06:09:28.868–06:32:11.411, saved 06:32:49), E2 963 (…06:32:49.210, saved 06:33:41), E3 974 (…06:35:41.621, saved 06:36:48), E4 56 (06:37:57.663–06:39:43.230, saved 06:39:55; a new process) — E1 ⊂ E2 ⊂ E3 line for line; the toasts on film read 958, 963, 974, 56 |
| Logcats | L1 583 lines (saved ≈ 06:37:06 via Android's app info → log viewer, film 06:36:56–06:37:10), L2 617 lines (saved ≈ 06:40:34–56, film). Clock = phone − 2 h (L1 400 `04:32:11.377` = E1 `06:32:11.377`, the same frame) |
| Clock offsets | Film overlay = pts + 06:10:41.75; **phone = overlay + 0.5 s ± 0.25 s** at the start (status bar 06:12→06:13 between overlay 06:12:59.25 and 06:12:59.75) and at the end (06:39→06:40 between 06:39:59.25 and 06:39:59.75), 4-fps strips. Exports = HCI clock (E1 105 logs A755's `RESPONSE` bytes at 06:14:09.962, A755 is at 06:14:09.959 — 3 ms) |
| Wear and head side | The head was **never in view** (P3 not done). Case slots: **upper slot = Left**, lower = Right — film 06:29:50 (only the upper slot occupied, Connection tab "Left 100 % ⚡, Right 100 %") and runtime info A4251 06:29:34 (`6.2` field 2 = 2, Left charging). Which bud was out of the case is from the slots and Option F's per-bud field 2; whether a bud was **in an ear** is not on film |
| Buds MAC (partial, ADR-010) | `04:00:6e:…:07`, classic handle `0x000b` in every ACL (Z161…, A159, A1049, A1531, A2029, A2558, A3140, A4285, B159, B831) |

## Capture-integrity pre-flight

- **HCI prefix:** `python3 -c "A=open('CAP-070-btsnoop_hci1.log','rb').read();B=open('CAP-070-btsnoop_hci2.log.last','rb').read();print(len(A),len(B),B[:len(A)]==A)"` →
  `222764 237265 True`; A adds packets 5,014–5,285 (06:36:08.6–06:37:28.4). Boundaries: Z ends 06:10:46.252 after the LE link's `0x16` (Z4623, P2's Bluetooth off
  06:10:44 on film), A starts 06:10:52.690 (on at 06:10:50); A ends 06:37:28.403 (BF-24's Bluetooth off, A5283 ACL `0x16` 06:37:28.299), B starts 06:39:36.576
  (BF-25's on, film 06:39:34). No Buds packet lies between files; none is counted twice.
- **Exports:** E1 = E2 lines 1–958, E2 = E3 lines 1–963 (Python line comparison, True/True). Each export holds the ring buffer up to the tap on *Export* (E1's last
  line 06:32:11.411, the tap at 06:32:42); the lines after it are written later. E4 starts with a new process's `Permissions (start)` line 06:37:57.663.
  Debug mode was on throughout (film 06:11:46, `Debug mode (verbose hex-dump logging)` on; hex lines in every export).
- **Processes (L1/L2):** PID 23551 05:26:45.772–05:26:52.063 (`wm_on_create` → `wm_on_destroy`, 7 s after the install); PID 23764 05:44:14.008–06:09:27.053 (the
  unfilmed rehearsal; its last line `wm_on_top_resumed_lost`); PID 28098 06:09:28.851–06:37:28.408 (the filmed run; last line `onBluetoothServiceDown`); PID 525
  created 06:37:57.540 (`Zygote: Process 525 created`) when the icon was tapped (film 06:37:56). How 28098 ended is not logged (no system log); the skeleton's
  force-stop is the maintainer's step (BF-24). No `FATAL`, `ANR`, crash-buffer, "Decoder fault" or "Unexpected" line. Two `Signal Catcher … reacting to signal 3` /
  `Wrote stack traces to tombstoned` pairs: L1 508–510 (06:34:48.315, PID 28098, Debug tab idle on film) and L2 597–599 (06:40:31.955, PID 525, 2 s before the
  log viewer opened on film) — the process continued in both cases (E3 runtime info 06:34:51.699; L2 603–617).
- **Absent:** system log; screen recording (P4); the two `uiautomator` dumps (BF-24); P0's exit status.

## Video review method (and privacy)

The whole film at a fixed 2-s interval (101 contact sheets, 3×3, film time burned in), then every transition narrowed at 1–10 fps on a phone-region crop with
the overlay (`ffmpeg … crop=880:470:0:90,transpose=2` + `crop=300:36:980:684`). Privacy (every sheet): no street-address overlay (the overlay is date and
time only), no face or head; visible are the Wi-Fi SSID in Quick Settings (06:10:57–06:11:00, 06:36:54–56, 06:39:32, 06:40:38), Bluetooth device names
"Charge 6" and "Niro" (06:10:42–56, 06:37:22–26, 06:39:38), carrier "KPN", a public radio web page (06:18:16–28). Maintainer's decision in chat 2026-10-07:
*"Commit as is (Recommended)"*.

## Event Timeline

Times: phone clock (log times exact; film-only events = overlay + 0.5 s, rounded to the second). "Step" = the skeleton's ID (appendix); T/S/H/C = `APP_TESTPLAN.md`.

| Phone time | Action / event | Actor | Step | Test-plan | Registry Test-ID | Evidence |
|---|---|---|---|---|---|---|
| 05:26:38 | 1.1.0 installed in user 10 (first install there) | maintainer | P1 | T1 | — | maintainer's `dumpsys` output (Log Metadata) |
| 05:26:45–05:26:52 | app opened and closed (PID 23551) | maintainer | — (not in plan) | — | — | L1 10–22 |
| 05:44:14–06:09:27 | **unfilmed rehearsal** (PID 23764): 8 MAESTRO sessions (ch 21, 21, 19, 19, 19, 19, 19, 21), the five switches off on ch 21 (Z771, Z779, Z789, Z797, Z805) and on on ch 19 (Z2018, Z2048, Z2051, Z2054, Z2057), Volume EQ off/on ch 19 (Z3510, Z3526) — every write ACKed; no export covers it | maintainer | — (not in plan) | — | — | Z; L1 23–244 (lifecycle only) |
| 06:09:28.85 | new process 28098 (E1 line 1 `Permissions (start)`) | maintainer | — | — | — | L1 246–251; E1 1 |
| 06:10:42 | **film starts**: Bluetooth dialog in Quick Settings | maintainer | P5 | — | — | film |
| 06:10:44 / 06:10:50 | Bluetooth **off**, then **on** | maintainer | P2 | — | — | film; Z4623 `0x16` 06:10:45.94; A starts 06:10:52.690 |
| 06:10:59–06:11:00 | status bar 06:10 → 06:11 | — | P5 | — | — | film |
| 06:11:04 | case lid opened (both buds inside) | maintainer | P7 | — | — | film |
| 06:11:10 | app icon tapped; Connection tab "Paired — not connected to this phone … tap Connect" (no automatic open: Android link `NOT_CONNECTED`) | maintainer | BF-1 | T1 | — | film; E1 6–7 |
| 06:11:24–06:11:27.50 | **Connect** → page A157, ACL A159, DLCI 2 `SABM` A236 → announcement A260 ch **21** → `ReadSetting` 16, then 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29 (A296…A331, 31–60 ms each) → `SubscribeRuntimeInfo` A332 | maintainer / app | BF-1 | T1, O8 | `PAIR-003`, `BATT-004` | A157–A334; E1 8–50 |
| 06:11:27–28 | screen: ready, "Android doesn't show the Buds as connected (yet)", Left 100 % ⚡, Case "Battery unavailable", then Case 89 % (A334 6.1 = 89) | app | BF-1 | T1 | `BATT-004` | film 06:11:27–29 |
| 06:11:30 | Controls tab; values already shown (no "—" caught) | maintainer | BF-1 | S6 | — | film |
| 06:11:42–06:11:52 | gear → Settings → **Debug**: Debug mode **on**, "Unidentified frames (5)" | maintainer | P6 | — | — | film |
| 06:12:07.05 | **Disconnect** (phone `DISC` A614) | maintainer | — (not in plan) | — | — | A614; E1 57 |
| 06:12:17 / 06:12:18.12 | **Connect** → ch 21 (A655), twelve reads A670–A732 (37–60 ms) | maintainer / app | — (repeat of BF-1) | T1, O8, S6 | `PAIR-003`, `BATT-004` | A640–A735; E1 59–101 |
| 06:12:18.5 | Controls: "Use touch controls" already shown on (10-fps strip) — no "—" in the first second | — | BF-1 | S6 | — | film; A702 (field 4) 06:12:18.658 |
| 06:12:28–06:12:54 | gear → **Info**: "App: 1.1.0, build 0323849 (2026-10-06)", firmware `release_5.203` ×3, "Control channel: 21" | maintainer | BF-2 | T1 | — | film |
| 06:12:58–06:13:56 | Controls: the (i) of Touch controls ("read 06:12:18"), Press and hold, **Head gestures** ("Use head gestures: read 06:12:19"), In-ear detection, **Multipoint** ("Multipoint: read 06:12:18"), **Case sounds** ("Earbuds replaced: read 06:12:19 / Other alerts: read 06:12:19"); card order Touch controls · Press and hold · Head gestures · In-ear detection · Multipoint · Case sounds; all five switches **on** (= A708 `11:1`, A732 `29:2`, A726 `27:1`, A729 `28:1`, A714 `15:1`) | maintainer | BF-3 | T2 | `MULTI-001`, `HEAD-001`, `CASE-001`, `CASE-002` | film; A708–A732 |
| 06:14:09.90 | **Multipoint off** (buds in the case) → A752 `4:{11:0}` ch 21 → A755 empty `RESPONSE` | maintainer / app | BF-4 (done differently: not worn) | T3 | `MULTI-001` | A752/A755; E1 105–107; film 06:14:10 |
| 06:14:17.75 | **Use head gestures off** → A762 `4:{29:1}` → A765 | maintainer / app | BF-5 (differently) | T4 | `HEAD-001` | A762/A765; E1 110 |
| 06:14:33.37 | **Earbuds replaced off** → A770 `4:{28:0}` → A773 | maintainer / app | BF-6 (differently) | T5 | `CASE-001` | A770/A773; E1 113 |
| 06:14:40.35 | **Other alerts off** → A777 `4:{27:0}` → A780 | maintainer / app | BF-6 (differently) | T6 | `CASE-002` | A777/A780; E1 116 |
| 06:14:46–51 | Sound tab; **Volume EQ off** → A785 `4:{15:0}` → A788 | maintainer / app | BF-7 (differently) | T7 | `AUDIO-002` | A785/A788; E1 119 |
| 06:14:57.79 / 06:15:02.94 | **Disconnect** (A790) then **Connect** → ch 21 (A824): reads answer `11:0` A880, `15:0` A886, `27:0` A898, `28:0` A901, `29:1` A904 | maintainer / app | BF-8 | T8 | `PAIR-003`, `MULTI-001`, `HEAD-001`, `CASE-001`, `CASE-002`, `AUDIO-002` | A790–A905; E1 120–164 |
| 06:15:16 → 06:15:18.23 | **Left** (upper slot) out of the case onto the table → Buds `DISC` DLCI 2 (A913), ACL up | maintainer / Buds | BF-9 (differently: Left out of the case, not of an ear) | — | `CASE-004`, `INEAR-002` | film; A913; E1 168–172 |
| 06:15:19.73–06:15:19.86 | automatic re-open (AFTER_LOSS) → announcement **19** (A961) — only the Left out | app / Buds | BF-9 | — | `CASE-004`, `PAIR-003` | A945–A1038; E1 173–217 |
| 06:15:22 → 06:15:25.10 | Left back into the case → ACL dropped `0x13` (A1044); "Automatic re-open skipped: the session was lost 5296 ms after an automatic re-open" (ADR-044 chain guard). Observation "did the case sound" — not recordable (no audio, nothing said) | maintainer / Buds / app | BF-9 | — | `CASE-001` (observation: none) | A1044; E1 221–228 |
| 06:15:36–06:15:41.52 | Left out again → Buds open the ACL (A1049, no phone page) and the RFCOMM multiplexer (A1266 Buds `SABM` DLCI 0); LINK_BACK re-open (first attempt fails, A1252 collision; second A1383 DLCI **3**) → ch 19 (A1396); Buds-opened DLCI 0x08 = HFP (A1272, `AT+BRSF` A1293) | Buds / app | — (not in plan) | — | `CASE-004` | A1049–A1480; E1 229–275 |
| 06:15:54–06:15:58.63 | Left back into the case → ACL `0x13` (A1526) | maintainer | — (not in plan) | — | — | film; A1526 |
| 06:16:11 / 06:16:11.96 | Sound tab, **pull to refresh** (spinner on film) → page A1529 → ch **19** with both buds in the case (A1632); the Volume EQ switch reads off | maintainer / app | — (not in plan) | — | `PAIR-003` | film 06:16:09–12; A1529–A1705; E1 288–330 |
| 06:16:18.94 | **Volume EQ on** → A1953 `4:{15:1}` **ch 19** → A1955 | maintainer / app | BF-10 (differently: ch 19, buds in the case) | T3–T7 | `AUDIO-002` | A1953/A1955; E1 339 |
| 06:16:25.40 / 28.44 / 29.40 / 31.90 | Controls: **Multipoint on** A1989 `4:{11:1}`, **Earbuds replaced on** A1992 `4:{28:1}`, **Other alerts on** A1995 `4:{27:1}`, **Use head gestures on** A1998 `4:{29:2}` — each → empty `RESPONSE` (A1991, A1994, A1997, A2000) | maintainer / app | BF-10 (differently) | T3–T6 | `MULTI-001`, `CASE-001`, `CASE-002`, `HEAD-001` | A1989–A2000; E1 344–353 |
| 06:16:44 → 06:16:52.71 | Left out of the case onto the table (A2003 6.2 field 2 = 1) and back → ACL `0x13` (A2022) | maintainer / Buds | BF-11 (differently: case, not ear) | — | `CASE-001` (observation: none) | film; A2003–A2022 |
| 06:16:58 → 06:16:59.43 | Left out again → Buds ACL A2029 → LINK_BACK re-open (attempt 1 fails, A2241/A2274 collision; attempt 2) → ch 19 (A2297); the bud leaves the table out of view (06:17:07–10) | maintainer / app | — (not in plan) | — | `CASE-004` | A2029–A2383; E1 368–415 |
| 06:17:22 → 06:17:27.80 | the Left back into the case → ACL `0x13` (A2553) | maintainer | — | — | — | film; A2553 |
| 06:17:42 / 06:17:43.03 | Sound, **pull to refresh** → ch 19 (A2665), both in the case; **Volume EQ row shows "—"** before its read (film 06:17:43), then the switch | maintainer / app | BF-1's S6 check (on Sound) | S6 | `PAIR-003` | film 06:17:41–44; A2665–A2731 |
| 06:17:56 / 06:18:04 | **Left**, then **Right** out of the case (A3008 6.2 = 1; A3019 6.1 absent, both field 2 = 1); no `DISC`; case empty | maintainer | (prep of BF-12) | — | `INEAR-002`/`003` (not identifiable: head not in view) | film; A3008, A3019 |
| 06:18:16–06:18:30 | Home → browser, a radio stream started; back to the app (Recents) | maintainer | BF-12 prep | — | — | film |
| 06:18:37.54 / 06:18:52.59 | **Volume EQ off** A3072 `4:{15:0}` → A3074; **on** A3075 `4:{15:1}` → A3077 (ch 19) — audio playing; no observation recordable | maintainer / app | BF-12 | T7 | `AUDIO-002` (observation: none) | A3072–A3077; E1 510–513 |
| 06:19:04 | notification shade: media paused | maintainer | — (not in plan) | — | — | film |
| 06:19:26–06:19:35.88 | Right, then Left into the case (A3120 6.3 = 2) → ACL `0x13` (A3135) | maintainer | BF-13 pre-state | — | — | film; A3120–A3135 |
| 06:20:12–06:20:20 | Controls: "Not connected to the Buds. Controls are disabled — open the Connection tab to connect." | — | BF-13 | — | — | film |
| 06:20:22 → 06:20:25.06 | **only the Left** out of the case → Buds ACL A3140 → LINK_BACK re-open (attempt 2) → announcement **19** (A3402) | maintainer / app | BF-13 | C12 | `CASE-004`, `PAIR-003` | film; A3140–A3488; E3 529–583 |
| 06:20:24.5–06:20:25.9 | Controls: **"—" in place of every switch** (Use head gestures, In-ear detection, Multipoint, Earbuds replaced, Other alerts) until the reads land, then the switches | app | BF-1/BF-24 (S6) | S6, T11 | — | film 5-fps strip 06:20:24–26 |
| 06:20:30.50 | **Use head gestures off** → A3614 `4:{29:1}` ch 19 = `…2a052203e80101fcd6da847e` (**the derived frame**) → A3616 — the tap came after "ready" (film 06:20:29–31); **no early tap during the re-open**, no "being reopened" text | maintainer / app | BF-13 (differently) / BF-15 | C12 (not tested), T9 | `HEAD-001` | A3614/A3616; E3 584–586 |
| 06:21:20.25 / 06:21:24.25 | **Multipoint off** A3668 `4:{11:0}` ch 19 = `…2a04220258009d8f9dc47e` (derived) → A3671; **on** A3679 `4:{11:1}` = `…2a04220258010bbf9ab37e` → A3681 | maintainer / app | BF-14 | T9 | `MULTI-001` | A3668–A3681; E3 595–600 |
| 06:21:46.22 / 06:21:48.49 | **Use head gestures on** A3685 `4:{29:2}` = `…2a052203e801024687d31d7e` → A3688; **off** A3690 `4:{29:1}` → A3692 | maintainer / app | BF-15 | T9 | `HEAD-001` | A3685–A3692; E3 603–606 |
| 06:22:10–06:23:17.69 | Sound: **balance** dragged, 14 writes (A3707…A3770), values `17:` 13, 0, 31, 11, 9, 0, **7**, 0, 12, 20, 30, 8, 0, **7**; ends "Right 4" (film 06:23:17) — the channel-19 `4:{17:7}` = `…2a052203880107e9b86e257e` (derived) first at A3747 06:22:58.19 | maintainer / app | BF-16 | — | `AUDIO-003` | A3707–A3772; E3 611–652; film |
| 06:23:44 | the **Right** out of the case (lower slot) → both out; **no `DISC`**, stays on 19 (Info 06:24:14 "Control channel: 19") | maintainer | BF-17 | — | `CASE-005`, `INEAR-003` (wear not identifiable) | film; A3788/A3797; no Buds `DISC` (A control inventory) |
| 06:24:12–06:25:34 | Info opened repeatedly (ch 19); 06:25:04 Right into the case (A3823 6.3 = 2) and 06:25:12 out again (A3839) — no `DISC` | maintainer | — (not in plan) | — | `CASE-004`/`005` | film; A3823, A3839 |
| 06:25:38–06:25:40 | Home and back (link observer stopped/started) | maintainer | — | — | — | E3 672–675 |
| 06:25:51.18 / 06:26:02.36 / 06:26:17.07 | **Multipoint off** A3896 (derived ch-19 bytes), **on** A3902, **Use head gestures on** A3910 (derived) — each → `RESPONSE`; Info between taps (ch 19) | maintainer / app | BF-14/15 (repeated) | T9 | `MULTI-001`, `HEAD-001` | A3896–A3913; E3 678–684 |
| 06:26:39.40–06:27:48.16 | balance dragged again, 39 writes (A3924…A4058), last `17:7` (A4058, film "Right 4") | maintainer / app | BF-16 (repeated) | — | `AUDIO-003` | A3924–A4060; E3 689–805 |
| 06:28:10–06:28:44 | the Right into the case (A4079 6.3 = 2) and out again (A4098) — no `DISC` | maintainer | — (not in plan) | — | `CASE-005` | film; A4079, A4098 |
| 06:29:08–06:29:10.00 | **a bud taken off (out of view), held at the top of the frame — the Left** (it goes into the upper slot at 06:29:32, A4251 6.2 = 2) → **Buds `DISC` DLCI 2 (A4115) with the ACL up** | maintainer / Buds | BF-18 (wear not shown) | — | `INEAR-005`, `INEAR-004` | film; A4115/A4116; E3 818–822 |
| 06:29:11.52–06:29:12.36 | AFTER_LOSS re-open → announcement **21** (A4153 `… 10 15 1d ea 71 de 7d 5e 25 44 fa 99 71 38 ff ff ff ff 0f …`); Info "Control channel: 21" (film 06:29:12–16); ANC `Notify` A4208 Settable `e8` | app / Buds | BF-18 | — | `INEAR-005` | A4138–A4233; E3 823–866 |
| 06:29:32 | the Left into the case (upper slot) | maintainer | — | — | — | film; A4251 |
| 06:29:50 | Connection tab: Left 100 % ⚡, Case 87 %, Right 100 % | — | — | — | `BATT-004` | film; A4251 |
| 06:29:58 → 06:29:59.28 | the Right into the case → ACL `0x13` (A4280) | maintainer | BF-19 pre | — | — | film; A4280 |
| 06:30:01.95 | **Connect** → ch **21** (A4383), both docked | maintainer / app | BF-19 (differently) | T10 | `PAIR-003` | A4283–A4458; E3 888–930 |
| 06:30:18 | **only the Right** out of the case (A4734 6.3 = 1) — no `DISC`, stays on 21 (Info 06:30:24–28) | maintainer | BF-19 | — | `CASE-005` | film; A4734 |
| 06:30:45.43 / 06:30:57.07 | **Volume EQ off** A4770 `4:{15:0}` ch 21 → A4772; **on** A4800 `4:{15:1}` = `…2a04220278019977e84e7e` (**the derived frame**, first capture) → A4802 | maintainer / app | BF-20 | T10 | `AUDIO-002` | A4770–A4802; E3 945–948 |
| 06:31:42.62 / 06:31:45.57 | Sound: Upper treble dragged — `WriteSetting 4:{16:{… 5:-4.26}}` A4829 → A4831, then `5:3.72` A4832 → A4834; *Read EQ again* **not tapped** | maintainer / app | BF-21 (differently) | H5 (not done) | `EQS-001` | A4829–A4834; L1 394–397 |
| 06:31:54–06:32:31 | **swipe** through the tabs (Connection, ANC, Controls, Find, back to Controls, Sound, ANC, Connection) — each swipe changed the tab | maintainer | BF-22 | C10 | — | film (camera only) |
| 06:32:24 | ANC tab: "Off", "The Buds don't allow changing noise control right now (usually because no bud is in an ear) …" — the last claim's Settable (A4416 `00`, 06:30:02, both docked) | — | — (not in plan) | — | — | film; A4416 |
| 06:32:40–06:32:49 | Debug → **Export** → Android "save" dialog → "Debug log saved (958 lines)" (E1); no rotation | maintainer | BF-23 (differently) | S12 (not done) | — | film; E1/E2 963 |
| 06:33:38–06:33:42 | **Export** again → "Debug log saved (963 lines)" (E2); no rotation | maintainer | BF-23 (repeated) | — | — | film; E3 967 |
| 06:34:48.3 | `SIGQUIT` → "Wrote stack traces to tombstoned" (PID 28098); nothing on film (Debug tab idle) | — | — (not in plan) | — | — | L1 508–510 |
| 06:36:46–06:36:48 | **Export** → "Debug log saved (974 lines)" (E3) | maintainer | BF-24 | — | — | film; L1 556 |
| 06:36:52–06:37:10 | Quick Settings; Android Settings → Apps → OpenControl app info → log viewer (L1 saved) | maintainer | — (not in plan) | — | — | film |
| 06:37:26 → 06:37:28.00 | Bluetooth dialog: **Bluetooth off** → phone `DISC` HFP (A5223) and MAESTRO (A5275), ACL `0x16` (A5283); "Session loss cause: Bluetooth was switched off on this phone" | maintainer / app | BF-24 | — | — | film; A5223–A5283; L2 363–396 |
| 06:37:28.41–06:37:57.54 | process 28098 ends (force-stop per the skeleton; not on film, not logged) | maintainer | BF-24 | — | — | L2 396 → 398 |
| 06:37:56 / 06:37:57.54 | app icon → new process **525**; "Bluetooth is disabled." + Enable Bluetooth | maintainer / app | BF-24 | T11 | — | film; L2 398–457; E4 1–2 |
| 06:38:02–06:38:36 | **Controls**: red note; "—" for Use touch controls and Use head gestures (scrolled view) | maintainer | BF-24 | T11, M1/N1 | — | film 06:38:04–06 |
| 06:39:02–06:39:28 | **Sound**: the five EQ bands without values (sliders centred, "—") | maintainer | BF-24 | T11 | — | film 06:39:04–10 |
| — | `uiautomator dump` ×2 — **not done** (no dumps in the folder) | — | BF-24 | T11 | — | absent |
| 06:39:34 → 06:39:36.95 | Bluetooth dialog: **Bluetooth on** ("Pixel Buds Pro 2 … Verbinding maken…") | maintainer | BF-25 | — | — | film; L2 470–476 |
| 06:39:39.91–06:39:42.94 | LINK_BACK re-open → ch **21** (B416), twelve reads (B430–B525, 77–273 ms), EQ `5:3.72` (B428 — the 06:31:45 write persisted), runtime info B526 | app | BF-25 | O8 | `PAIR-003`, `BATT-004`, `EQS-001` | B153–B531; E4 6–55; L2 477–537 |
| 06:39:44–46 | Sound: EQ values back (Upper treble 3.7), Volume EQ on | — | BF-25 | — | — | film |
| 06:39:48–06:39:56 | Debug → **Export** → "Debug log saved (56 lines)" (E4) | maintainer | BF-end | — | — | film; L2 586 |
| 06:39:59–06:40:00 | status bar 06:39 → 06:40 | — | BF-end | — | — | film |
| 06:40:32 | `SIGQUIT` (PID 525) → stack traces; 06:40:34–56 app info → log viewer (L2 saved) | — / maintainer | — (not in plan) | — | — | L2 597–599; film |
| 06:40:59 | **film ends** | — | BF-end | — | — | film |
| 06:41:04–06:42:06 | after the film, no log but B: ACL `0x13` (B819); phone page 06:41:47 (B829) → app session ch 21 (B935, twelve reads); Buds `DISC` 06:42:03.20 (B1295) → app re-open ch 19 (B1347) 1.7 s later | maintainer / app / Buds | — (not in plan) | — | — | B819–B1430 |

## Step mapping (skeleton → what happened)

| Step | Result | Where |
|---|---|---|
| P0 | done (maintainer's output; exit status not recorded) | Log Metadata |
| P1 | done — the output shows a **first install in user 10** (🟡), not an update | Log Metadata |
| P2 | done | 06:10:44–50 |
| P3 | **not done** — the head never in view | — |
| P4 | **not done** — no screen recording | — |
| P5 | done (start 06:10:59 / 06:12:59; end 06:39:59) | — |
| P6 | done (Debug mode on); no audio track ready in advance — a radio stream was started at 06:18 | 06:11:46 |
| P7 | done (lid opened on film) | 06:11:04 |
| BF-1 | done differently (Connect tapped — no automatic open with the lid closed; Connection tab, not Controls, first); twelve reads ✓; repeated 06:12:18 | |
| BF-2 | done | 06:12:28 |
| BF-3 | done (Controls (i) ×6; Equalizer (i) not opened) | 06:12:58–06:14:46 |
| BF-4 … BF-7 | done differently (buds in the case, not worn) | 06:14:09–51 |
| BF-8 | done | 06:14:57–06:15:03 |
| BF-9 | done differently (Left out of the case and back; no ear; no observation) | 06:15:16–25 |
| BF-10 | done differently (channel 19, buds in the case; order 15, 11, 28, 27, 29) | 06:16:18–31 |
| BF-11 | done differently (as BF-9; no observation) | 06:16:44–52 |
| BF-12 | done (audio playing; no observation recordable) | 06:18:37–52 |
| BF-13 | done differently (only the Left out → 19 ✓; the tap came after "ready" — C12 not tested) | 06:20:22–30 |
| BF-14 | done; repeated 06:25:51 / 06:26:02 | 06:21:20–24 |
| BF-15 | done (plus 06:20:30 and 06:26:17) | 06:21:46–48 |
| BF-16 | done (14 + 39 writes); repeated | 06:22:10–06:27:48 |
| BF-17 | done (Right out, Left already out; no `DISC`, stays 19); wear not shown | 06:23:44 |
| BF-18 | done (Left taken off with both out of the case on 19 → `DISC` + 21); **wear and head not shown** | 06:29:08–12 |
| BF-19 | done differently (Connect with both docked gave 21; then only the Right out, no new announcement) | 06:30:01–18 |
| BF-20 | done | 06:30:45–57 |
| BF-21 | done differently (band dragged; *Read EQ again* not tapped; read back at BF-25) | 06:31:42–45 |
| BF-22 | done (camera only) | 06:31:54–06:32:31 |
| BF-23 | done differently (two exports, **no rotation** — S12 not done) | 06:32:40, 06:33:38 |
| BF-24 | done partly (export, Bluetooth off, new process, "—" on Controls and Sound on film); **no `uiautomator` dumps**; the force-stop not visible | 06:36:46–06:39:28 |
| BF-25 | done (automatic re-open, twelve reads) | 06:39:34–42 |
| BF-end | done (export, minute flip, film stopped) | 06:39:48–06:40:59 |

## Traceability (`AGENTS.md` §13 step 7)

| Test-ID | Rows | Status |
|---|---|---|
| `MULTI-001` | BF-3, BF-4, BF-8, BF-10, BF-14 (+ repeats) | observed |
| `HEAD-001` | BF-3, BF-5, BF-8, BF-10, BF-13/15 (+ repeats) | observed |
| `CASE-001` | BF-3, BF-6, BF-8, BF-10; BF-9/BF-11 observations | writes observed; **the case-sound observation not recorded** (no audio, nothing said) |
| `CASE-002` | BF-3, BF-6, BF-8, BF-10 | observed |
| `AUDIO-002` | BF-7, BF-8, BF-10, BF-12, BF-20 | writes observed; **the audibility observation not recorded** |
| `AUDIO-003` | BF-16 | observed |
| `INEAR-005` | BF-18 | action observed, **wear not shown** (head out of view) |
| `INEAR-002` / `003` / `004` | BF-9, BF-13, BF-17, BF-18 | case removals observed; **ear insertions not identifiable** |
| `CASE-004` / `005` | BF-9, BF-13, BF-17, BF-19 and the extra Right in/out at 06:25 and 06:28 | observed |
| `EQS-001` | BF-21, BF-25 | write + read-back on reconnect observed; H5's *Read EQ again* not done |
| `PAIR-003` | BF-1, BF-8, BF-13, BF-25 (+ the re-opens) | observed |
| `BATT-004` | every connect | observed |

## Analysis checklist

- [x] Every filter scoped to handle `0x000b` (Connection Complete for `04:00:6e:…:07` in each file); DLCIs identified by content (DLCI 0x08 in A's second ACL is HFP).
- [x] P0/P1 interpreted; Info frame "1.1.0, build 0323849" against `git log`; APK and certificate SHA-256 read from `dist/1.1.0/` (read only).
- [x] Zero Play-services claims (command, exit status, positive control `CAP-066`).
- [x] I: twelve reads per connection, in order, latencies; S6 on film (the camera, no screen recording).
- [x] II/III: every write against the table — all byte-identical, all ACKed; the three derived forms on the wire.
- [x] BF-13's early tap — not done (recorded).
- [x] IV: BF-18 — Buds `DISC`, announcement 21; bud identified by slot + runtime info; wear not shown.
- [x] VI: swipe yes; S12 no; H5 no; the `uiautomator` dumps absent.
- [x] One row per step; Summary of `APP_TESTPLAN.md` section T updated.

---

## Appendix — the planned procedure (skeleton by `ai-sessions/0074`, unchanged except one file name unquoted for the link check)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0074`, 2026-10-06; scope is the maintainer's in chat 2026-10-06: one build
session for the five switches, the screen-reader text and the open film items of `TODO.md` §2; the maintainer films this run, a later session analyses
it and gives the release verdict). After the run: rename this folder from the placeholder `CAP-070-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BF` to the film's
first/last overlay times and analyse it as `CAP-068` was (`ai-sessions/0070`).

**Purpose.** What the unit tests cannot show:

- **I — the update and the Connect read.** 1.1.0 installed **over 1.0.1** (an update — `TODO.md` §2: `CAP-068`'s logs pointed to a fresh install);
  the Connect read now asks 12 settings (2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29 — ADR-036/046/052…055).
- **II — the five switches on hardware** (ADR-052 … ADR-055; `MULTI-001`, `HEAD-001`, `CASE-001`, `CASE-002`, `AUDIO-002`): OFF → the request → the
  empty `RESPONSE` OK; a reconnect reads the value back; ON → the same. Case sounds: where the Buds and the case are, and — an observation — whether the
  case still sounds when a bud is put back with "Earbuds replaced" off. Volume EQ and head gestures: what you hear or see, as an observation.
- **III — the three request forms no capture holds yet** (`ai-sessions/0074` §A.1): `4:{11:v}` and `4:{29:v}` on **channel 19**, `4:{15:1}` on
  **channel 21**. The app builds them with the same codec; until this run the unit tests check them only structurally (`Settings074`, labelled).
  The channel is the Buds' choice: with only the Left bud out of the case they announce 19, with only the Right out 21 (🟢 `PROTOCOL.md` §2.2a, Update
  of 2026-10-01, `CAP-065` 7 of 7). The steps use that; whichever channel occurs is recorded (Settings → Info, "Control channel").
- **IV — lead L-1** (`TODO.md` §2, `INEAR-005`): both buds worn on channel 19, take the **Left** out of the ear with the head in view.
- **V — the open film items of `TODO.md` §2** that fit a release build in this user: H5 with *Read EQ again*, S6 (the first second after "ready", on
  Android's screen recording), S12 (an export across a rotation), the swipe between tabs, the channel-19 balance frame `17:7`, P1's install times.
- **VI — the screen-reader text** (`TODO.md` §5 "Accessibility"; the maintainer's text in chat 2026-10-06: *"Not read from the Buds yet"*): every "—"
  carries that content description — checked from the accessibility tree (`uiautomator dump`), because no screen reader is assumed to be installed in
  the user without Google Play (⚪ — if one is, film it reading a "—" as well).
- **Not in this run** (`ai-sessions/0074` §A.5): S9 (needs two Pixel Buds), B4 (needs the Buds forgotten and re-paired), K5/BC-12 (the auto-off setting
  exists only in the Owner user), F-4 (needs a debug build), a second phone.

If the run exceeds one sitting (about 35 minutes), stop after section III and run IV–VI as a second film in the same folder.

### Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-070` |
| Group(s) | BF (new) |
| Date | TBD |
| Phone | Pixel 9a, GrapheneOS — the secondary user without Google Play used for `CAP-067`/`CAP-068`; note its user id (P0) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.1.0** (versionCode 10100), the release APK signed with the maintainer's key (`scripts/release.sh`, `RELEASING.md`) — read from the **Info tab on film**: "App: 1.1.0, build <hash> (<date>)", no "-dirty"; APK and certificate SHA-256 from the script's output |
| Official Pixel Buds app | Not used. Pixel 7a: Bluetooth off |
| Buds | Pixel Buds Pro 2; firmware read from the Info tab on film |
| Video file | TBD — camera film: the phone, the case and **your head** (both ears) at every wear change |
| Screen recording | TBD — Android's own screen recorder in the test user, started before BF-1 (S6 and the swipe need the screen at full frame rate) |
| Log files | TBD — CAP-070-btsnoop_hci.log and .log.last (Bluetooth is toggled in P2 and VI), the app's exports, logcat, the two `uiautomator` dumps, P0/P1 outputs |

### Preparation

| # | Check | Done |
|---|---|---|
| P0 | In the test user: `adb shell am get-current-user`; `adb shell pm list packages --user <id> \| grep -i -E "gms\|vending"` — note the output **and the exit status**; positive control: the same with `grep opencontrol` (exit 0). Save both outputs into this folder (`CAP-068` P0 had no exit status) | ☐ |
| P1 | The **1.1.0 release** APK installed **over 1.0.1** in the test user (same key, no uninstall, do not clear data). Then `adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds \| grep -E "firstInstallTime\|lastUpdateTime\|versionCode"` — save the output: an update shows `lastUpdateTime` later than `firstInstallTime` and `versionCode=10100` (`TODO.md` §2) | ☐ |
| P2 | Bluetooth HCI snoop log on (set in the Owner, then switch to the test user); switch Bluetooth off and on **on film** | ☐ |
| P3 | Camera films the phone, the case and your head; **both ears in view at every wear step** (`CAP-068`: the head was in view twice); head on the right of the frame = Left bud | ☐ |
| P4 | Android's screen recorder on (Quick Settings), recording the phone's screen | ☐ |
| P5 | Status bar on film across a minute change at the start and at the end | ☐ |
| P6 | Debug tab: **Debug mode on** (hex lines in the exports); a short audio track ready at **low volume** for VI's Volume EQ observation | ☐ |
| P7 | Buds charged, both in the case, lid open; the Buds' earlier settings are whatever `CAP-069`/`CAP-068` left (`TODO.md` §4: the EQ on a custom curve, Off ticked in the mode list) — the run reads them first | ☐ |

**Rhythm:** one action, then wait 5–10 s. Something unexpected: stop, wait 10 s, continue. **Export the debug log before any step that ends the
process and after every Bluetooth off/on.** No narration needed except where a step says "say". Before committing: check the films for a street-address
overlay and for Wi-Fi/device names (`CAP-068`'s privacy note).

### Steps

DLCI numbers are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05); the bytes are written for the even numbers. Every `WriteSetting`
starts `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 1d 9a 8c 9e` on **channel 21** (request address `00 4b`) or `7e 00 3b 03 10 13 …` on **channel 19**
(`00 3b`); its answer is the empty `RESPONSE` `7e00a503080110151dea71de7d5e251d9a8c9e036d4ed87e` (ch 21) / `7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e`
(ch 19). The full expected frames (real = a capture's bytes, derived = computed, not yet seen on the wire):

| Write | Channel 21 | Channel 19 |
|---|---|---|
| Multipoint off `4:{11:0}` | `…2a0422025800ad636bac7e` (real, `CAP-069` 3161) | `…2a04220258009d8f9dc47e` (**derived**) |
| Multipoint on `4:{11:1}` | `…2a04220258013b536cdb7e` (real, 3212) | `…2a04220258010bbf9ab37e` (**derived**) |
| Head gestures off `4:{29:1}` | `…2a052203e80101bc106ba27e` (real, 2492) | `…2a052203e80101fcd6da847e` (**derived**) |
| Head gestures on `4:{29:2}` | `…2a052203e801020641623b7e` (real, 2564) | `…2a052203e801024687d31d7e` (**derived**) |
| Earbuds replaced off / on `4:{28:0\|1}` | `…2a052203e0010092717fdb7e` / `…2a052203e00101044178ac7e` (real, `CAP-058` 5623/5643) | `…2a052203e00100d2b7cefd7e` / `…2a052203e001014487c98a7e` (real, `CAP-024` 1988/2023) |
| Other alerts off / on `4:{27:0\|1}` | `…2a052203d80100bac507f17e` / `…2a052203d801012cf500867e` (real, `CAP-058` 5680/5697) | `…2a052203d80100fa03b6d77e` / `…2a052203d801016c33b1a07e` (real, `CAP-024` 2053/2084) |
| Volume EQ off `4:{15:0}` | `…2a04220278000f47ef397e` (real, `CAP-041` 2461) | `…2a04220278003fab19517e` (real, `CAP-022` 1871) |
| Volume EQ on `4:{15:1}` | `…2a04220278019977e84e7e` (**derived**) | `…2a0422027801a99b1e267e` (real, `CAP-022` 1895) |
| Balance Right 4 `4:{17:7}` | `…2a052203880107a97d5edf037e` (real, `CAP-064` 6671) | `…2a052203880107e9b86e257e` (**derived**) |

The derived frames are what the app's unit tests expect (`SettingsCodecTest`, `Settings074.VEQ_ON_CH21_DERIVED`); each was also computed independently
(`python3`: `zlib.crc32` over address, control and `RpcPacket`, `ai-sessions/0074` §I). A frame on the wire that differs from its row is the finding.

#### I. The update and the Connect read (`APP_TESTPLAN.md` T1, T2, S6; `PAIR-003`, `BATT-004`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BF-1 | P0–P7 done; screen recording on; Bluetooth on | open the app (it connects by itself, ADR-044; else **Connect**); go straight to **Controls** | for about the first second the unread switches show "—", then the Buds' values (S6, screen recording); no crash after the update | DLCI 0x02 open; the announcement; `ReadSetting 4:16`, then **twelve** `ReadSetting` requests in the order 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29, each answered by a `RESPONSE` `4:{N:…}`; one `SubscribeRuntimeInfo` | `PAIR-003`, `BATT-004` | a request missing or out of order; any request for 13, 21, 23 … 39; a request on DLCI 0x08/0x0a |
| BF-2 | ready | gear → **Info** (hold 3 s on the build line and on "Control channel") | "App: 1.1.0, build <hash> (<date>)", no "-dirty"; "Control channel: 19" or "21" — note it | nothing sent while the menu is open | — | another version, or "-dirty" |
| BF-3 | ready | **Controls**: read each card; open the (i) of Head gestures, Multipoint, Case sounds. **Sound**: the Volume EQ switch under the presets; the Equalizer (i) | order of cards: Touch controls, Press and hold, Head gestures, In-ear detection, Multipoint, Case sounds (Earbuds replaced, Other alerts); labels only, no note; each (i) "<Label>: read HH:MM:SS"; Equalizer (i) ends "Volume EQ: read HH:MM:SS"; note every value | the answers of BF-1 decide the values: `11:v`, `29:1`=off/`29:2`=on, `27:v`, `28:v`, `15:v` | `MULTI-001`, `HEAD-001`, `CASE-001`, `CASE-002`, `AUDIO-002` | a switch whose position differs from its read answer |

#### II. The five switches on the current channel, both buds worn (`APP_TESTPLAN.md` T3–T8)

Both buds **in your ears** (head in view), the case open on the table next to the phone. Use the column of the channel noted in BF-2.

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BF-4 | ready, Multipoint on (else turn it on first and note it) | Controls: **Multipoint** off | the switch moves only after the Buds' OK; (i) "Multipoint: changed HH:MM:SS" | `4:{11:0}` (table) → the empty `RESPONSE`; the mirrored settings push is **not** expected (the app does not subscribe); SASS `07 11 00 04 01 02 98 00` on DLCI 0x04 **only if** a Message Stream claim is open at that moment — note whether it came (ADR-053) | `MULTI-001` | no write, a second write, or the switch moving before the `RESPONSE` |
| BF-5 | — | **Use head gestures** off | as BF-4; no dialog (the official "Optimize head gestures" is not reproduced) | `4:{29:1}` → `RESPONSE`; **nothing** on DLCI 0x08 (GSND CONTROL) from the phone | `HEAD-001` | `4:{29:0}`, or any phone frame on DLCI 0x08 |
| BF-6 | — | Case sounds: **Earbuds replaced** off, then **Other alerts** off | each after its OK; (i) lines "Earbuds replaced: changed …", "Other alerts: changed …" | `4:{28:0}` → `RESPONSE`; `4:{27:0}` → `RESPONSE` | `CASE-001`, `CASE-002` | 27 and 28 swapped, or one write for both |
| BF-7 | — | Sound: **Volume EQ** off | after its OK; Equalizer (i) "Volume EQ: changed …" | `4:{15:0}` → `RESPONSE` | `AUDIO-002` | — |
| BF-8 | all five off | **Disconnect**, then **Connect** | after ready all five show off with "read HH:MM:SS" (the Buds stored them) | the twelve reads; answers `11:0`, `29:1`, `27:0`, `28:0`, `15:0` | `PAIR-003` + the five | a read answer that differs from the last write |
| BF-9 | Earbuds replaced **off**, buds worn | take the **Left** bud out and put it back into the case (lid open), then take it out and back into the ear; **say** whether the case made a sound | the session may close and re-open by itself (a bud in/out of the case) | Buds `DISC` 0x02 and the app's `SABM` ≈ 1.5 s later (ADR-044); no write | `CASE-001` (observation), `CASE-004`, `INEAR-002` | — (an observation: whether "Earbuds replaced" off silences the case) |
| BF-10 | — | turn all five **on**: Multipoint, Use head gestures, Earbuds replaced, Other alerts, Volume EQ | each after its OK, "changed …" | `4:{11:1}`, `4:{29:2}`, `4:{28:1}`, `4:{27:1}`, `4:{15:1}` → each `RESPONSE`. **If this session is on channel 21, `4:{15:1}` is the first capture of that frame** — note the time | the five | any value written other than the table's |
| BF-11 | Earbuds replaced **on** | as BF-9: Left bud into the case and out again; **say** whether the case sounded | as BF-9 | as BF-9 | `CASE-001` (observation) | — |
| BF-12 | Volume EQ on, audio playing at low volume | Volume EQ off, listen 10 s, on, listen 10 s; **say** what you hear | the switch follows each OK | `4:{15:0}`, `4:{15:1}` | `AUDIO-002` (observation) | — (audibility is an observation, not a claim of the app) |

#### III. Channel 19 forms (`APP_TESTPLAN.md` T9; `MULTI-001`, `HEAD-001`, `AUDIO-003`, `CASE-004`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BF-13 | both buds into the case (lid open), wait until "ready" again | take **only the Left** bud out, into the left ear (head in view); **within about 2 s of "The app's channel was closed …"**, before "ready", tap **Multipoint** | the tap: "The setting was not changed: The app's channel is being reopened — try again in a moment." (`APP_TESTPLAN.md` C12), the switch unchanged; then ready by itself; Info: **"Control channel: 19"** | Buds `DISC` 0x02 → app `SABM` ≈ 1.5 s later → announcement with `10 13` (channel 19); **no** `WriteSetting` for the early tap (nothing queued) | `CASE-004`, `INEAR-002`, `PAIR-003` | a write sent later by itself; channel 21 announced (then do V first and come back) |
| BF-14 | on 19 | Multipoint **off**, then **on** | each after its OK | `…2a04220258009d8f9dc47e`, then `…2a04220258010bbf9ab37e` (derived, ch 19) → `RESPONSE` ch 19 — **first capture** | `MULTI-001` | another frame, or an error status / no answer |
| BF-15 | on 19 | Use head gestures **off**, then **on** | each after its OK | `…2a052203e80101fcd6da847e`, then `…2a052203e801024687d31d7e` (derived) → `RESPONSE` — **first capture** | `HEAD-001` | as BF-14 |
| BF-16 | on 19 | Sound: drag the balance to **Right 4**, release (as many drags as needed — say how many) | "Right 4 · changed …" in the (i) | `…2a052203880107e9b86e257e` (`4:{17:7}`, derived) → `RESPONSE` — `TODO.md` §2 | `AUDIO-003` | — |

#### IV. Lead L-1: the Left out with both worn on 19 (`INEAR-005`, new)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BF-17 | on 19, Left worn | take the **Right** bud out of the case into the right ear (head in view); wait 10 s; Info: note the channel | both worn; the session may re-open | note whether the Buds `DISC` and which channel they announce (`CAP-066`: a Right insertion with the Left not worn gave 21) | `CASE-005`, `INEAR-003` | — (records the state for BF-18) |
| BF-18 | **both worn, channel 19** (if BF-17 left 21: put the Right back in the case, take it out again, retry once; else note "not reached") | take the **Left** bud out of the ear, hold it in view (head in view) | the session closes and re-opens by itself | 🟡 predicted (`PROTOCOL.md` §2.2a, Updates of 2026-10-01 / 2026-10-03): Buds `DISC` of MAESTRO with the ACL up, then an announcement with `10 15` (**21**) | `INEAR-005`, `INEAR-004` | no `DISC`, or a `DISC` followed by 19 again |

#### V. The channel-21 form of Volume EQ on (`APP_TESTPLAN.md` T10; `AUDIO-002`, `CASE-005`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BF-19 | — | both buds into the case; take **only the Right** out, into the right ear (head in view); wait until ready; Info | "Control channel: 21" | announcement `10 15` (channel 21) | `CASE-005`, `INEAR-003` | 19 announced (then record it and repeat once) |
| BF-20 | on 21 | Sound: Volume EQ **off**, then **on** | each after its OK | `…2a04220278000f47ef397e` (= `CAP-041` 2461) → `RESPONSE`; then `…2a04220278019977e84e7e` (derived) → `RESPONSE` — **first capture of the channel-21 "on" frame** (ADR-055; its bytes become the unit-test fixture and the `// TODO(verify)` in `SettingsCodec` goes) | `AUDIO-002` | the "on" frame differs from the derived one, or an error status / no answer |

#### VI. The film items of `TODO.md` §2 and the screen-reader text (`APP_TESTPLAN.md` H5, C10, S12, T11)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BF-21 | ready | Sound: drag one EQ band, release; then **Read EQ again** (H5) | the value just written; "EQ updated: HH:MM:SS" moves | `WriteSetting 4:{16:…}` → OK; `ReadSetting 4:16` → the same five floats | `EQS-001` | the read differs from the write |
| BF-22 | ready | **swipe** left through all five tabs and back (C10/O2), slowly | the tab follows each swipe; the bottom bar agrees | — | — | a swipe that does not change the tab (screen recording) |
| BF-23 | ready | Debug tab → **Export debug log**; **rotate the phone while Android's "save as" dialog is open**; save (S12) | "Debug log saved (N lines)."; the file has content | — | — | an empty file or no toast |
| BF-24 | ready | **Export** again (before the process ends); Quick Settings: **Bluetooth off**; `adb shell am force-stop io.github.tedsluis.opencontrolpixelbuds`; open the app; **Controls** tab → `adb shell uiautomator dump /sdcard/CAP-070-controls.xml`; **Sound** tab (scroll to the Balance card) → `adb shell uiautomator dump /sdcard/CAP-070-sound.xml`; `adb pull` both into this folder | "Bluetooth is disabled."; Controls: six "—" (no switches); Sound: five EQ bands, Volume EQ, balance, mono audio and conversation detection show "—" | — | — | in a dump, a node with `text="—"` whose `content-desc` is not `Not read from the Buds yet` |
| BF-25 | — | **Bluetooth on** → the app re-opens the session by itself (or **Connect**) | ready; the values back | the twelve reads | `PAIR-003` | — |
| BF-end | — | export the debug log; status bar across a minute change; stop the film and the screen recording | — | — | — | — |

### Don'ts

- Do not clear the app's data and do not uninstall 1.0.1 first (P1 — the run is an update).
- Do not open the Owner user during the run.
- Do not tap two things within 5 s of each other, except in BF-13 (the early tap).
- Do not use the official app to change a setting during the run (it is not in this user).

### After the run

The exports (`adb pull` from the test user's storage), both `btsnoop_hci.log` files, the app's logcat, the two `uiautomator` dumps, P0's and P1's
outputs, the camera film and the screen recording. All into this folder, then `sha256sum *`.

### Analysis checklist

- [ ] Scope every filter to the Buds' ACL handle (`bluetooth.addr` is empty on this encapsulation — show the Connection Complete for the Buds' address
      with that handle); identify the DLCIs by content (`AGENTS.md` §13).
- [ ] P0 (exit status + positive control) and P1: user id, no `com.google.android.gms`/`com.android.vending`; **update over 1.0.1**: `lastUpdateTime` >
      `firstInstallTime`, `versionCode=10100`; the Info frame "1.1.0" and the build hash against `git log`; APK and certificate SHA-256 against the script.
- [ ] Zero Play-services claims on the Message Stream (`03 08 00 02 01 25`): command, exit status, positive control `CAP-066`.
- [ ] I: the Connect read — the twelve `ReadSetting` requests in order, each with its answer, per connection (`python3 scripts/pwrpc_decode.py <log>`); the
      latency per read against `ai-sessions/0074` §A.4 (21–241 ms there); S6 on the screen recording (frame time of the first "—" and of the values).
- [ ] II: per write — the request bytes against the table, the `RESPONSE`, the switch on film moving after it; the read-back of BF-8; the SASS byte of
      BF-4 if a claim was open; nothing from the phone on DLCI 0x08 in BF-5; the observations of BF-9, BF-11, BF-12 quoted as said.
- [ ] III: the channel of each announcement; **the three derived frames against the wire** (BF-14, BF-15, BF-20) — if equal, they become real fixtures
      (`Settings074`) and the labelled tests and the `// TODO(verify)` of ADR-055 go; if not, the difference is a finding for the maintainer.
- [ ] III: BF-13's early tap — no `WriteSetting` after it; the text on film.
- [ ] IV: BF-18 — `DISC` direction and the announced channel; the head side on film; the lead L-1 result for `PROTOCOL.md` §2.2a (a proposal to the
      maintainer, no status change by the analysing session alone).
- [ ] VI: H5's read equals its write; the swipe; S12's file; **the two `uiautomator` dumps**: every `text="—"` node has `content-desc="Not read from the
      Buds yet"` (count them per tab).
- [ ] One row per step — done / not done / done differently — and `APP_TESTPLAN.md`'s Summary (section T) updated.
- [ ] Traceability (`AGENTS.md` §13 step 7): `MULTI-001` (BF-3, BF-4, BF-10, BF-14), `HEAD-001` (BF-3, BF-5, BF-10, BF-15), `CASE-001` (BF-6, BF-9,
      BF-10, BF-11), `CASE-002` (BF-6, BF-10), `AUDIO-002` (BF-7, BF-10, BF-12, BF-20), `AUDIO-003` (BF-16), `INEAR-005` (BF-18), `INEAR-002`/`003`/`004`
      (BF-9, BF-13, BF-17, BF-18, BF-19), `CASE-004`/`005` (BF-9, BF-13, BF-17, BF-19), `EQS-001` (BF-21), `PAIR-003` (BF-1, BF-8, BF-13, BF-25),
      `BATT-004` (every connect) — each referenced in the timeline or flagged "expected but not observed".
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `id_registry.csv`, `APP_TESTPLAN.md`, `TODO.md` §2, and write `CAP-070-FINDINGS.md`; the release
      verdict for 1.1.0 is the analysing session's, with the maintainer.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-070-2026-10-07_06-10-41_06-40-59-Group_BF/CAP-070-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-070-2026-10-07_06-10-41_06-40-59-Group_BF/CAP-070-EVENT-NOTES
