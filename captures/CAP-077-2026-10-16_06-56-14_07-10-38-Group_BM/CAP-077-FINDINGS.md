# Findings: `CAP-077` (Group BM — 1.3.1, build `7aa524b`, the fixes of `ai-sessions/0094` in the secondary user without Google Play services, installed over 1.3.0 `ff27ee1` on film, measured against `CAP-076`)

**Analysed by `ai-sessions/0095` (2026-10-16).** Companion: `CAP-077-EVENT-NOTES.md` (the timeline, the steps as run, the Test-IDs). Labels per
`PROJECT_RULES.md` §1: 🟢 FACT (a frame, a log line or a film second), 🟡 HYPOTHESIS, ⚪ ASSUMPTION, ❓ OPEN QUESTION. Names as in the event notes: HL / H the
HCI logs, E the export, A the app logcat (UTC), L the full logcat (local time; CAP-077-logcat-extract.txt carries every quoted line), s a film second (phone
time = 06:56:14.82 + s). Every `tshark` filter was run on the Buds' ACL handle (`bthci_acl.chandle==0x000b`; the Connection Completes for the Buds' address:
HL 686, H 151 / 1678 — `bluetooth.addr` matches nothing in either log, exit 0); every negative carries its command, its exit status and a positive control
(`AGENTS.md` §13 step 8). Counts come from the committed scripts `scripts/pwhdlc_crc_table.py`, `scripts/message_stream_claims.py`, `scripts/pwrpc_decode.py`
and the patch method of `scripts/film_mode_buttons.py` (run on native frames with their own timestamps).

**In short (🟢 unless marked).** The tested APK is the build of B1 (SHA-256 checked by `adb pull`). 1.3.1 sent the same requests as 1.3.0, byte for byte, in
every session (6 of 6, two of them 1.3.0's): one `SubscribeToSettingsChanges` and one `ReadSetting 4:13` per MAESTRO session, after `SubscribeRuntimeInfo`, before
`GetHardwareInfo`. **Every press-and-hold showed on screen with the Buds' push — 8 of 8, 6–20 ms from the packet** (the noise-control tab was on screen for all
eight); the (i) said "Changed by the Buds at 07:03:58". All 39 noise-control stream packets reached the export with the right cause (9 Buds' changes, 22 own
`Set`s, 8 "unchanged" — 1.3.1's new word). Every own tap ended in a `Set` and its ACK: **6 of 6 tile taps** (A93-APP-03 — no silent tap) and 5 of 5 tab taps.
**"Use different Buds"** asked first (the new dialog), forgot the Buds and connected to nothing by name until Android's CDM dialog created an association
(A93-APP-01, verified); "removed 0 association(s)" is correct — user 10 had none since 2026-10-04. One name per mode on the buttons, the (i), the tile and the
notification; "Heavy bass" (A93-APP-08). **Not exercised:** TalkBack (A93-APP-07 — only the one-ripple-per-row is on film), the kept "changed since" cause
(A93-APP-02 — no mode change while disconnected), a push during a read (A93-APP-04), Cancel in the new dialog. 22 setting writes by 1.3.1 (and one by 1.3.0), all
acknowledged and read back where re-read. No crash, no ANR, no wrong write, no Safe Mode; the one session loss was the Buds' own close when a bud left the case,
re-opened 1.507 s later. **Unexpected (none harmful):** for five settings (4, 11, 15, 27, 28) each write was echoed by **three** stream packets, for six others by
one (§9.1); and — against the maintainer's "during playback" — **no media played**: AVRCP reported Stopped throughout and the A2DP stream was suspended
during all eight holds (§8).

## 0. Build, install, user, logs, privacy, health

- **Build.** On film "App: 1.3.0, build ff27ee1 (2026-10-14)" before the update (s49–57) and "App: 1.3.1, build 7aa524b (2026-10-16)", no "-dirty", after it
  (s127–133, s779–783). The installed APK, pulled by this session at 07:42 (`adb shell pm path --user 10 io.github.tedsluis.opencontrolpixelbuds` → one
  `base.apk`, exit 0; `adb pull`, exit 0), has SHA-256 `9be3a89c94d3db7236416a244d2fea51e0524c6e5806d2e648faf4ca0611ee0a` = ~/opencontrol-1.3.1-tested's
  (`versionCode=10301`, `lastUpdateTime=2026-10-16 06:58:05` — no install after the run; CAP-077-adb-checks-0095.log). 🟢 The tested APK is the build of B1.
- **Install.** The update was made from the laptop: L `adbd service requested 'abb_exec:package` and abb's `package install` 06:57:57.501, the installer
  06:57:58.471, `am_kill: [10,25520,…,stop io.github.tedsluis.opencontrolpixelbuds due to killDueToPackageUpdate,…]` 06:58:05.898, Android's "Updaten…" on film
  s111–112 (06:58:05–06), the new process `am_proc_start: [10,28176,1010354,…,top-activity,…]` 06:58:07.419. The shell log: `versionCode=10300` at 06:54:22,
  `10301` at 06:58:37, both `firstInstallTime` lines unchanged — an update, no reinstall; the Debug-mode setting survived it (on in 1.3.0 at s59, on in 1.3.1 at
  s776). The previous install, `lastUpdateTime=2026-10-15 09:11:11`, is not in any log buffer of this capture (L: system from 10-16 06:13:08, events 06:16:35,
  main 06:55:55); 🟡 it is the published 1.3.0 installed with Obtainium after its release (`RELEASING.md` §13: published 2026-10-15T07:09:03Z = 09:09 local, "D4
  done … installed with Obtainium") — the Info tab shows its build `ff27ee1` (s49).
- **The user.** 🟢 user 10, a secondary user: A `userType: full.secondary`; L `am_switch_user: 10` 06:47:59.689; both app processes uid 1010354. 🟢 no Google Play in
  user 10: `adb shell pm list packages --user 10 | grep -E "com.google.android.gms$|com.android.vending$"` → nothing, exit 1; the same for user 0 → both, exit 0
  (positive control). The maintainer's P0 `grep -i -E "gms|vending"` printed GrapheneOS's three `app.grapheneos.gmscompat*` packages with exit 0 — a match on the
  substring "gms", not a positive for Play. **0 Play markers** on the wire: `tshark -r <log> -Y 'frame.p2p_dir==0 && frame contains 03:08:00:02:01:25'` → 0
  in HL and in H (exit 0; positive control `CAP-076-btsnoop_hci1.log` → 18). Play services runs in user 0 meanwhile (L PID 2189: 11,582 lines in the run's window,
  `NearbyShared`/`NearbyDiscovery`/`NearbyFastPair`); 0 of them mention RFCOMM, the event stream or hearable controls (`grep -i -c -E
  'rfcomm|eventstream|event stream|hearable'` → 0). 🟢 The maintainer's statement holds.
- **Logs.** HL 06:48:05–06:56:37 and H 06:56:41–07:12:00 (the snoop log restarts with the Bluetooth on); E from 1.3.1's first line (06:58:07.853) to 07:11:14;
  A (the app logcat) and L (`adb logcat -b all`, ending 07:10:32). **The "System log" the maintainer named was not made** (Checkpoint 0: "Niet gemaakt"); L's
  system and main buffers cover the whole film.
- **The data files are not in the repository** (only the two notes are; Git LFS refused every upload on 2026-10-16 — EVENT-NOTES, header; `TODO.md` §6). The film: CAP-077-recording1.mp4 is **kept local, not committed** (the repository's Git LFS budget was exceeded at the push of 2026-10-16 and cannot be raised — the maintainer, chat 2026-10-16; the original is in ~/opencontrol-capture-originals/CAP-077/, SHA-256 `0c6e0318…`); every film second cited here can be checked against that original.
- **Privacy.** The film shows the Wi-Fi network's name and the serial numbers (allowed on film; not written in any text here). The full logcat is committed only as
  CAP-077-logcat-extract.txt (window 06:47:00–07:12:30; every line of the app's, Bluetooth's and the CDM dialog's PIDs and every line naming them; the update's
  installer lines; the Buds' full address redacted in 5 lines and the six Debug-mode `GetHardwareInfo` hex answers redacted — the command is in its header).
  E's Debug-mode hex lines carry the five `GetHardwareInfo` answers of 1.3.1 (the serials), as the exports of `CAP-073`…`CAP-076` did.
- **Health.** A: `FATAL`, `ANR in`, `StrictMode` → 0 (positive control: the tag `OpenControlBuds` → 24); L in the window: no `FATAL EXCEPTION` and no `ANR in` naming
  the app (the four `FATAL EXCEPTION` lines of L are `in.sunilpaulmathew.izzyondroid`, 10-12 … 10-15). One **SIGQUIT** to the app at 07:10:55 (A "reacting to
  signal 3", "Wrote stack traces to tombstoned"), after the film and before the exports; its sender is in no log of this capture (L ends 07:10:32) — ❓, as
  `CAP-071`'s (the computer's `adb bugreportz` there). No crash followed: the same PID 28176 exported the log at 07:11:20.
- **The Activity.** One `wm_on_create_called` for PID 28176 (04:58:07.860 UTC); E's second "Android link observer started" at 07:11:14 is a restart after a stop
  (A `wm_on_restart_called` 05:11:14.265 UTC — the export's save dialog), not a re-creation. No rotation on film.

## 1. ACLs, sessions and the Message Stream claims

| ACL | From | To (reason) | Sessions (MAESTRO `SABM` → end) |
|---|---|---|---|
| HL 686 06:56:27.263 | 1.3.0's Connect tap | HL 1350 06:56:37.199 (`0x16`, the Bluetooth off) | S0 763 → phone `DISC` 1335 06:56:37.103 |
| H 151 06:56:42.680 | the Bluetooth on | H 1589 07:00:17.763 (`0x13`, the Buds) | S1 399 (1.3.0, LINK_BACK) → phone `DISC` 984 06:58:05.955 (the update); S2 1010 (1.3.1, LINK_BACK) → phone `DISC` 1504 07:00:05.206 (Forget) |
| H 1678 07:00:35.590 | the phone's `Create Connection` 1675 (the Connect tap, E 07:00:35.352) | open | S3 1755 → **Buds' `DISC` 2135 07:00:43.366**; S4 2201 (AFTER_LOSS) → phone `DISC` 4330 07:04:34.446 (Disconnect); S5 4359 → open |

- 🟢 The ACL's end at 07:00:17.763 was the Buds' (reason `0x13`), 12.6 s after the Forget and **0.3 s before** the tap on *Pair a device* (the status bar's
  Bluetooth-device icon is gone in the native frame of 07:00:17.868, the touch dot appears at 07:00:18.085); the phone had suspended its last A2DP stream at
  07:00:13.442 (H 1567) and sent HFP `+CIEV: 4,2` at 07:00:17.713 (H 1588). 🟡 The Buds, both in the case (charging, H 2090), ended an idle link with no app
  session; nothing in this run ties it to the app. The CDM dialog's discovery started after it (L `startDiscovery()` 07:00:18.870).
- **Claims of DLCI 0x04** (`python3 scripts/message_stream_claims.py <log> 0x000b`): HL 1, H 20 — **all the app's**, each ended by the phone's `DISC` (the release),
  none by another client; 0 Play claims. H: the snapshots of S1–S5 (#1–#4, #15), the six tile taps in five claims (#5 carries taps 1 and 2 — tap 2 came 1.7 s
  after tap 1's ACK, inside the 1.5-s linger), the five tab taps (#10–#14), Ring Left / Stop + Ring Right / Stop (#16–#18), the battery pull (#19), the ANC pull
  (#20). Durations 1.54–3.79 s (#5 with its two taps; the others ≤ 2.67 s) — `MESSAGE_STREAM_LINGER_MS` 1.5 s after the last answer. 🟢 No claim that is not a
  tap, the tile, a pull or a Connect snapshot; nothing held, nothing re-claimed (E: 0 lines with "re-claim" or "hold" — `grep -c -i 're-claim\|hold'` → 0;
  positive control: `CAP-075`'s export 2, as `CAP-076` §1).
- **Phone `DM` on DLCI 0x04** (6): after claim #3's `DISC` (07:00:37.505) the Buds sent three `03 03 00 03 e4 e4 ff` battery frames before their `UA`, the phone
  answered each with `DM` (H 2080–2089); the same after #9's `DISC` (07:02:18.207, H 3138–3144, `64 64 ff`). 🟢 The stack's answer to data on a channel it is
  closing; the app had already released the claim; harmless.
- **Other DLCIs:** 0x00 (multiplexer), 0x02, 0x04, 0x09 / 0x0c (HFP, opened by the Buds) — `tshark -r <log> -Y 'bthci_acl.chandle==0x000b && btrfcomm.len>0' -T fields
  -e btrfcomm.dlci | sort | uniq -c`. **0 frames on DLCI 0x08 or 0x0a** in HL and H (`-Y 'btrfcomm.dlci==0x08 || btrfcomm.dlci==0x0a'` → 0, exit 0; positive control
  `CAP-029-btsnoop_hci.log` → 109, e.g. frame 1592 `01 09 00 05 0a 03 0b 06 13`).

## 2. The Connect sequence — byte for byte, once per MAESTRO session (`PAIR-003`, `FW-003`, `BATT-004`)

Every session — S0 and S1 (1.3.0), S2 … S5 (1.3.1) — sent the same seventeen requests in the same order: `ReadSetting 4:16`, `4:2`, `4:4`, `4:7`, `4:11`, `4:12`,
`4:15`, `4:17`, `4:19`, `4:22`, `4:27`, `4:28`, `4:29`, `SubscribeRuntimeInfo`, **`SubscribeToSettingsChanges`, `ReadSetting 4:13`**, `GetHardwareInfo` (HL 796–873;
H 440–531, 1029–1114, 1784–1865, 2221–2319, 4377–4461). S0–S3 on channel 21, S4–S5 on channel 19 (the Buds' announcement: HL 790, H 430, 1027, 1777 ch 21; H 2219,
4375 ch 19). **Byte for byte:** each of the 34 distinct request frames (17 per channel) occurs in `CAP-076`'s H1/H2 (`pwhdlc_crc_table.py` on both, raw hex compared);
e.g. `SubscribeToSettingsChanges` ch 21 `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 f5 ad 21 28 01 97 54 b0 7e` (H 1859 = `CAP-051` 1177), ch 19 `7e 00 3b 03 10 13 1d ea 71 de
7d 5e 25 f5 ad 21 28 07 ee 8e b2 7e` (H 2306 = `CAP-056` 1451). One `GetHardwareInfo` per session, each answered (HL 1, H 5); E: four "Settings stream subscribed"
(S2–S5 — E starts after S1). The answers of `ReadSetting 4:13`: S0 `4:{13:0}`, S1 0, S2 0, S3 0 (buds in the case — "no mode settable"), **S4 `4:{13:1}`** (Off),
**S5 `4:{13:4}`** (Adaptive). Every `ReadSetting` answered (84 of 84), every answer in 12–233 ms (HL 14–129 ms). 🟢 Nothing new on the wire, as `ai-sessions/0094` said.

## 3. Every write — byte for byte

23 `WriteSetting` requests, each answered by an empty `RESPONSE` (no status = OK, 23 of 23) and each mirrored on the settings stream: 1.3.0 `4:{12:{1:1 2:1 3:1 4:1}}`
(H 860, ch 21, 06:57:29 — the mode list had `2:0`, H 502); 1.3.1 on ch 19: `4:{2:1}` (2478), `4:{16:{-1.00, 0.00, 4.00, 2.00, 5.87}}` (4201, the Upper-treble drag),
`4:{15:0}` / `4:{15:1}`, `4:{19:1}` / `4:{19:0}`, `4:{4:0}` / `4:{4:1}`, `4:{12:{1:1 2:0 3:1 4:1}}` / `{…2:1…}`, `4:{29:1}` / `4:{29:2}`, `4:{2:0}` / `4:{2:1}`,
`4:{22:0}` / `4:{22:1}`, `4:{11:0}` / `4:{11:1}`, `4:{28:0}` / `4:{28:1}`, `4:{27:0}` / `4:{27:1}` (EVENT-NOTES for the frames). Each followed a tap on film within
0.1–0.5 s and none came without one. The values written before S5 were read back by S5 (`4:{16:{… 5.87}}` 4384, `4:{15:0}` 4432, `4:{19:1}` 4438, `4:{2:1}` 4390).
**0 `WriteSetting 4:{13:n}`**, 0 writes of fields 3 or 6 (`awk` over the tables' REQUEST rows → none). 🟢 No wrong write. The Buds end the run as they began it, except
**in-ear detection on** (off since `CAP-076` 07:06:56) and the EQ's Upper treble 5.87 (was 1.69).

## 4. The holds — every stream packet against the screen (`TOUCH-007`; ADR-064/065)

### 4.1 Method

A hold = a `SERVER_STREAM` `4:{13:n}` with no app `Set` within 2 s before it and no claim open. The screen: the four mode buttons read on the film's **native frames**
with their own timestamps (`ffmpeg -ss 365 -i <film> -t 120 -fps_mode passthrough -vf "crop=1080:400:0:540,pad=1080:1000:0:540,showinfo"`; the 40 × 40 patches and the
threshold of `scripts/film_mode_buttons.py`). The clock: phone = 06:56:14.82 + s ± 0.05 s (EVENT-NOTES, "Review method").

### 4.2 Every hold (🟢)

| # | Stream (H, ch 19 / ch 21) | Value | Button filled (native frame) | Δ | Export |
|---|---|---|---|---|---|
| 1 | 3857 / 3858, 07:03:18.090 | 3 Transparency | 07:03:18.101 TRA | +11 ms | 07:03:18.095 "(changed_by_buds)", 18.097 "(unchanged)" |
| 2 | 3882 / 3886, 07:03:22.512 | 2 Noise cancellation | 07:03:22.532 NC | +20 ms | 22.518 / 22.581 |
| 3 | 3900 / 3901, 07:03:26.901 | 1 Off | 07:03:26.895 OFF | −6 ms | 26.907 / 26.908 |
| 4 | 3921 / 3924, 07:03:31.363 | 4 Adaptive | 07:03:31.377 ADA | +14 ms | 31.370 / 31.435 |
| 5 | 3964 / 3967, 07:03:41.130 | 3 Transparency | 07:03:41.122 TRA | −8 ms | 41.134 / 41.181 |
| 6 | 4006 / 4007, 07:03:47.639 | 2 Noise cancellation | 07:03:47.654 NC | +15 ms | 47.644 / 47.705 |
| 7 | 4037 / 4038, 07:03:52.641 | 1 Off | 07:03:52.652 OFF | +11 ms | 52.645 / 52.711 |
| 8 | 4057 / 4058, 07:03:58.203 | 4 Adaptive | 07:03:58.217 ADA | +14 ms | 58.208 / 58.275 |

**8 of 8 holds shown**, each within the clock's uncertainty of the push; the order T → NC → Off → A twice is the four-mode list (`4:{12:{1:1 2:1 3:1 4:1}}`). The (i)
on film s466: "Changed by the Buds at 07:03:58: the Buds reported a new mode — a press-and-hold on a bud, or a change they made themselves." No claim was open
(#14 ended 07:02:48.833, #15 began 07:04:37.155) — the mode came on the app's own channel only. ⚪ Every hold made produced a packet: there is no camera and no audio,
so a hold the Buds did not act on would leave no trace; one bud gesture without a mode change is on the wire — AVRCP **PLAY** from the Buds at 07:03:49.39 (H 4013/4016),
between holds 6 and 7 (❓ a short tap). One more push came without a hold: 07:00:48.664 `4:{13:4}` (H 2339, ch 19 only), 0.5 s after the second bud left the case,
with in-ear detection off — the Buds' own switch (🟡, the `PROTOCOL.md` §6 item "a stored mode per wear state"); E "(changed_by_buds)"; the Connection tab was on screen.

**The maintainer's claim** "When I did a long press on a bud, the ANC mode in the app changed too" 🟢 holds for all eight holds. "The ANC modes during playback were
good" — see §8: there was no playback.

### 4.3 Two packets per change (🟢 observation, 🟡 cause)

With both buds out of the case every mode change came twice, channel 19 then 21, 1–69 ms apart (19 changes after 07:00:48: 8 holds + 11 own `Set`s), as in `CAP-076` §4.3;
the single packet at 07:00:48.664 came when the Right bud had just left the case. E logs the second packet of a hold "(unchanged)" and the second of an own `Set`
"(set_by_app)" (still within `ANC_SET_WINDOW_MS`). 🟡 One packet per bud core (`PROTOCOL.md` §4.1 Note of 2026-10-15).

### 4.4 The cause shown and logged (🟢)

E's 39 "Noise-control mode from the settings stream (…)" lines = H's 39 `4:{13:n}` packets, one to one in order: **9 "(changed_by_buds)"** (07:00:48.670 and the first
packet of each hold), **22 "(set_by_app)"** (both packets of the 11 own `Set`s), **8 "(unchanged)"** (the second packet of each hold); plus 4 "from the mode read: no mode
settable" (S2, S3; L: S0, S1) and 2 "from the mode read (read)" (S4 07:00:47.549, S5 07:04:37.955). Each matches the code: `causeOfStream` (an own `Set` of that mode
within 2 s → `SET_BY_APP`; a differing value → `CHANGED_BY_BUDS`; the same → `null`, logged "unchanged" since 1.3.1) and `causeOfReadAnswer` (`BudsRepositoryImpl.kt`).

## 5. The 1.3.1 fixes

### 5.1 A93-APP-01 — "Use different Buds" — 🟢 verified on hardware

The dialog on film (s229–230); "Forget these Buds" at 07:00:04; E "Session ended by the user's Disconnect tap" 07:00:05.202 and "Pairing: use different Buds — removed
0 association(s) of this app; the next Pair a device opens the picker" 07:00:05.206; H 1504 phone `DISC` DLCI 0x02. Then the Connection tab showed **"No Pixel Buds
Pro 2 paired yet." with *Pair a device* only** (s235) — **no `SABM` on DLCI 0x02 from 07:00:05.206 (1504) until 07:00:35.744 (1755)**, after the new association
(`tshark -r H -Y 'bthci_acl.chandle==0x000b && btrfcomm.dlci==0x02 && btrfcomm.frame_type==0x2f'` → 399, 1010, 1755, 2201, 4359; positive control: those five are the
sessions). In 1.3.0 the same tap left the repository's pairing instance able to connect by name (`ai-sessions/0093` A93-APP-01); 1.3.1 did not. *Pair a device*
opened Android's CDM dialog for one device (L `AssociationRequest { singleDevice = true, deviceFilters = [BluetoothDeviceFilter{mNamePattern=(?i).*Pixel\s*Buds.*…`
07:00:18.283), "Toestaan" created association mId 51 in user 10 at 07:00:24 (L `CDM_AssociationStore`; E "association created", "already bonded — no
createBond()"), and the Connect at 07:00:34 opened S3.

**"Removed 0 association(s)" is right** (🟢): this session's `adb shell dumpsys companiondevice` (read-only, 07:56) lists user 10's earlier association of the app,
mId 48, as removed on **2026-10-04 15:22:08 ("pkg-data-cleared")** — so from then until 07:00:24 today user 10 had none, and 1.3.0/1.3.1 had chosen the Buds by the
name fallback (ADR-056: "without one, a single bonded device whose name contains 'Pixel Buds'"; the second bonded headset's name does not contain it). AOSP's
`CompanionDeviceManager.java` documents `getMyAssociations()` as returning "a list of associations that have been previously associated with the current app" (§15) —
with none in user 10 there was nothing to remove. Not on film: **Cancel** (BM-7) and a rotation (BM-8).

### 5.2 A93-APP-02 — the kept "changed since" — not exercised

The cause "changed since the app last saw it" needs a mode that differs from the shown one at a Connect or re-open. S2, S3 read 0 (no mode); S4 read Off with the shown
mode Off (from S2/S3's snapshot `Notify` `e8 00 20`, mode byte `20`); S5 read Adaptive = shown Adaptive (the Disconnect → Connect lasted 2.3 s, no hold between —
EVENT-NOTES BM-4). Every read answer was a reading (`causeOfReadAnswer` → `READ`); the fixed branch (the other read source of the same `readRound` keeping
`CHANGED_SINCE_LAST_READ`) was never reached. 🟢 Not exercised — neither verified nor refuted; the unit test with `CAP-076` H1 4094 → 4135 stays the only evidence.

### 5.3 A93-APP-03 — no silent tile tap — 🟢 verified for the normal path; the cut-off path not exercised

Six tile taps (E "ANC tile tapped (shown: …)" 07:01:51.969 … 07:02:15.532); each: `Get` → `Notify` (the Buds' mode) → `Set` 3–6 ms later → ACK 48–146 ms → the
stream pair 65–261 ms after the `Set` (§6); the tile's subtitle changed 1.13–1.34 s after the touch (0.35 s for tap 2, on the open claim). **6 of 6 ended in a `Set`
and its ACK; none silent.** The `CAP-076` defect needed a claim closed between the `Get`'s answer and the `Set` — a foreign close (Play) that cannot happen in user 10;
none occurred (every claim ended by the app's own `DISC`). The tile flashed its "unavailable" look for one or two frames (≤ 50 ms) right after three updates
(07:01:53.25, 07:02:10.81, 07:02:16.77 — native frames): 🟡 the tile's state being redrawn; minor, cosmetic.

### 5.4 A93-APP-04 — a push during a read — not exercised

No `SubscribeToSettingsChanges` packet arrived between any `ReadSetting` (or EQ read) request and its answer in S2–S5 (`awk` over H's table: the stream packets are
at 07:00:48.664 and later, the reads end ≤ 07:00:47.545 and 07:04:37.950); the two pulls (07:09:31 battery, 07:09:43 ANC) got no stream packet either. 🟢 Not exercised.

### 5.5 A93-APP-07 — TalkBack — not exercised

No TalkBack on film (no focus frame, no spoken-feedback overlay in 294 viewed seconds). What is on film: every switch tap highlights the **whole row** (label and switch,
e.g. s292, s492, s576, s637, s654, s745), the ripple of one toggleable element (`Modifier.toggleable`, A93-APP-07) — 🟢 for the merged touch target, ❓ for what a screen
reader says.

### 5.6 A93-APP-08 — the labels, "(unchanged)", the dialog — 🟢 verified

The buttons "Noise cancellation", "Off", "Adaptive", "Transparency" (s307); the (i) "ANC mode: Transparency (updated 07:02:30)" and "Connection: connected" (s379); the
notification "Connected — noise control: Adaptive" (s333; 1.3.0's was "Connected — ANC: OFF", s16); the tile "ANC / Noise cancellation" … (s334–363); the presets
"Heavy bass" … (s302; 1.3.0 "HEAVY BASS", s69); the dialog with Cancel and Forget these Buds (s229); the log word "(unchanged)" (§4.4). The tile title stays "ANC".

## 6. The own taps (`ANC-001`…`ANC-004`, ADR-032, ADR-065 item 4)

| Tap | Claim | `Notify` → `Set` → ACK → stream (H, time 07:0…) | Set → stream |
|---|---|---|---|
| tile 1 (→ Off) | #5 | 2821 01:53.079 → 2823 .086 → 2826 .200 → 2832 .344 | 258 ms |
| tile 2 (→ NC) | #5 | 2841 01:54.807 → 2842 .813 → 2844 .938 → 2849 55.010 | 197 ms |
| tile 3 (→ T) | #6 | 2909 01:59.871 → 2910 .876 → 2913 .977 → 2918 02:00.085 | 209 ms |
| tile 4 (→ A) | #7 | 2981 02:05.626 → 2982 .629 → 2984 .729 → 2991 .890 | 261 ms |
| tile 5 (→ Off) | #8 | 3051 02:10.609 → 3052 .615 → 3054 .761 → 3059 .865 | 250 ms |
| tile 6 (→ NC) | #9 | 3117 02:16.580 → 3119 .586 → 3122 .695 → 3127 .831 | 245 ms |
| tab Off | #10 | 3284 02:27.680 → 3287 .686 → 3290 .734 → 3295 .751 | 65 ms |
| tab T | #11 | 3354 02:30.289 → 3355 .294 → 3357 .379 → 3362 .398 | 104 ms |
| tab A | #12 | 3524 02:38.769 → 3526 .773 → 3529 .841 → 3533 .849 | 76 ms |
| tab NC | #13 | 3593 02:42.232 → 3594 .235 → 3597 .298 → 3602 .331 | 96 ms |
| tab A | #14 | 3696 02:47.254 → 3697 .258 → 3700 .322 → 3705 .347 | 89 ms |

11 `Set`s, 11 ACKs, 0 NAKs; each `Set` was sent only after its claim's `Notify` read Settable `e8` (`ai-sessions/0062` F-1). 20 phone `SABM`s on DLCI 0x04 for 11 `Set`s:
the snapshots (5), the tile (5 claims for 6 taps), the tab (5), Ring/Stop (3), the pulls (2). 🟢 Every tap answered and classed as the app's.

## 7. The sessions, the loss and the re-opens (ADR-044)

- **Loss (1):** S3 ended by the Buds' `DISC` (H 2135, 07:00:43.366) — the Left bud had left the case (runtime info 07:00:47.454: Left not charging; film s272 Left
  without ⚡); E "Session lost: channel 0x02 closed (IOException: bt socket closed, read return: -1); last inbound frame … 5640 ms earlier", cause "the Buds closed the
  channel" (07:00:43.415). **Re-open 1.507 s later** (E 07:00:44.877 AFTER_LOSS; `LOSS_REOPEN_DELAY_MS` 1,500), `SABM` 2201 at 07:00:45.388, on channel 19, the full
  sequence with the subscription and `4:13` (M1). No second loss within `CHAIN_GUARD_MS`.
- **LINK_BACK re-opens (2):** 1.3.0 after the Bluetooth on (06:56:42.935, S1); 1.3.1 at its start (06:58:08.007, S2) — 🟢 a just-updated app re-opened by itself on its
  first resume with the Buds connected.
- **User ends (2) and the Bluetooth off (1):** Forget (07:00:05), Disconnect (07:04:34); the Bluetooth off (06:56:37, 1.3.0: "Bluetooth was switched off on this phone").
- **Value 0** ("no mode settable") was never shown as a mode (S0–S3; the ANC tab of 1.3.0 showed "The Buds don't allow changing noise control right now", s66).

## 8. Playback

🟢 **No media played during the film.** AVRCP: the phone's `PlaybackStatusChanged` reads Paused/Stopped at every registration (HL 974, 1122; H 571, 1957, 2117/2120) and
first "Playing" at **07:10:39.864** (H 6955), after the film, for 6 s. A2DP: the phone started and suspended the stream 61 times in H (`btavdtp.signal_id==0x07`
Start / `0x09` Suspend, phone-sent commands, 61 each), short streams that follow the touches (🟡 the phone's UI sounds); **the stream was suspended from 07:02:59.529 (H 3784) to 07:04:00.852
(H 4075) — through all eight holds**. The maintainer's "the ANC modes during playback were good" is therefore not supported by the wire for this run; the holds and
their display are verified without playback (❓ whether the maintainer meant an earlier listening session).

## 9. Unexpected findings (the wire and the logs)

1. **Three stream packets per write for five settings** (🟢 observation, 🔴 cause). A write of field 4, 11, 15, 27 or 28 was echoed three times: ch 19 before the
   `RESPONSE`, ch 19 again inside the same RFCOMM frame as the `RESPONSE` (two HDLC frames), ch 21 (e.g. `4:{15:0}` H 4260, 4262, 4265); a write of 2, 12, 16, 19, 22 or 29
   once (ch 19). 10 writes × 3 + 13 × 1 = 43 = the non-13 stream packets of H (82 − 39). Harmless for the app (the last value wins; no log line). Not in the official
   app's captures as such (❓ — a check over `CAP-069`/`CAP-070` would settle whether it depends on the client or the field).
2. **No playback** (§8) — contrary to the maintainer's statement.
3. **The Buds' own mode switch on leaving the case with in-ear detection off** (07:00:48.664, §4.2) — 🟡 another sample for `PROTOCOL.md` §6 ("a stored mode per wear
   state"); with in-ear detection off it was not expected to act on wear.
4. **The Buds' battery burst on a closing claim** answered by `DM` (§1) — benign.
5. **Device Information `03 0b`** (length 25, "Current FHN ephemeral identifier" per `PROTOCOL.md` §6) on every claim from 07:00:46 on — not new (it is in `CAP-071` …
   `CAP-076`); it fills the Debug tab's "Unidentified frames" (101 at s776).
6. **The ACL end at 07:00:17.763** by the Buds, idle in the case after the Forget (§1) — not the app's.
7. **An LE link to another device** (HL `0x0040`, Heart Rate service) — not the Buds; 0 LE frames to the Buds (their only handle is `0x000b`; H has no LE ACL at all:
   `tshark -r H -Y 'bthci_acl' -T fields -e bthci_acl.chandle | sort -u` → `0x000b`).
8. Nothing unknown on DLCI 0x02: every frame CRC-good, every packet of `maestro_pw.Maestro`, no unnamed service, no unknown method (the tables' method column);
   E: no "UnidentifiedFrame" on DLCI 0x02, no failure line (`grep -c -i 'error\|fail\|timeout'` over E's event lines → 0).
9. The SIGQUIT at 07:10:55 (§0) — ❓ its sender.

## 10. `CAP-076` (1.3.0, part 2 — the same user) vs `CAP-077` (1.3.1)

| Aspect | `CAP-076` part 2 (1.3.0) | `CAP-077` (1.3.1) |
|---|---|---|
| Requests per session | 17, byte-identical to the official app's | the same 17, byte-identical to `CAP-076`'s (6 of 6 sessions) |
| Holds shown | 12 of 12, ≤ 0.1 s | 8 of 8, 6–20 ms |
| Own taps answered | 4 of 4 (+ Ring/Stop) | 11 of 11 (6 tile, 5 tab) + Ring/Stop ×2 |
| Silent tile tap | 1 (part 1, with Play) | 0 of 6 (no Play — the trigger cannot occur) |
| "Changed since" after a re-open | overwritten (12 of 12 sessions) | not exercised |
| Same-mode push logged | with the old cause | "(unchanged)" (8) |
| Use different Buds | no dialog; the repository could still connect by name (A93-APP-01, found in code) | dialog; no connection by name; a new CDM association |
| Labels | "ANC: OFF", "HEAVY BASS" | one name per mode; "Heavy bass" |
| Writes | in-ear detection off, balance, EQ | 23, all acknowledged |
| Session losses | the Buds' closes, re-opened | one, re-opened in 1.507 s |
| Crash / ANR / Safe Mode | none | none |

## 11. The counts for the verdict

- App `SABM`s on DLCI 0x04: 21 (HL 1, H 20) — 5 Connect/re-open snapshots of 1.3.1 (+ 2 of 1.3.0), 15 user actions; 0 unexplained.
- Holds shown vs made: 8 of 8 (⚪ every hold made produced a packet, §4.2).
- Stream `4:{13:n}` packets vs E lines: 39 = 39 (9 / 22 / 8).
- The first read after every Connect/re-open: S0–S3 "no mode settable", S4 Off, S5 Adaptive — each equal to what the Buds had last reported; none shown wrongly.
- Writes: 23 sent, 23 acknowledged, 0 unasked.

## 12. The skeleton's expectations, applied literally per step run

BM-1 ✅ ("App: 1.3.1, build 7aa524b", the seventeen requests). BM-2 ✅. BM-3 not run. BM-4 run differently — the expectation ("changed since" stays) cannot be applied:
no change while disconnected. BM-5 run differently (six taps, 66 s after Ready) — "none silent" ✅ for the six. BM-6 not run. BM-7 not run. BM-8 ✅ for "not connected by
name" and "no `SABM` on DLCI 0x02 after the Forget until BM-9"; the rotation not run. BM-9 ✅ (the seventeen requests in S3). BM-end ✅.

## 13. Verdict table (`RELEASING.md` C4 — the analysing session's proposal; the verdict is the maintainer's)

| Item | Evidence | Class |
|---|---|---|
| Crash, ANR, wrong write, Safe Mode, lost session not recovered | none (§0, §3, §7) | — |
| A93-APP-01 Use different Buds | verified (§5.1); Cancel not run | OK |
| A93-APP-02 kept "changed since" | not exercised (§5.2) | open — unit test only |
| A93-APP-03 no silent tile tap | 6 of 6 answered (§5.3); the cut-off path needs Play | OK for user 10 |
| A93-APP-04 push during a read | not exercised (§5.4) | open — unit tests only |
| A93-APP-07 TalkBack | not exercised; merged rows seen (§5.5) | open — Robolectric tests only |
| A93-APP-08 labels, dialog, "unchanged" | verified (§5.6) | OK |
| Holds, taps, settings, Find, battery, update path | verified (§4, §6, §3, EVENT-NOTES) | OK |
| Three stream packets per write (5 fields), the tile's grey flash | §9.1, §5.3 | not an app defect / cosmetic |

**The analysing session's view:** no heavy issue; no minor defect found in the code exercised. Three of the six fixes were not exercised on hardware (02, 04, 07) and
rest on their unit tests. Two ways forward for the maintainer: (a) release as built, with the three not-exercised fixes stated in the release record; (b) a short
re-run of BM-3, BM-4 (two holds while disconnected), BM-6 and BM-7 first (≈ 5 minutes, the same APK).

## 14. Open questions and proposals (proposals — no code in this session; `AGENTS.md` §6)

1. ❓ Three stream packets per write for fields 4, 11, 15, 27, 28 vs one for 2, 12, 16, 19, 22, 29 (§9.1) — a check over the official app's captures
   (`python3 scripts/pwrpc_decode.py <log> | grep SubscribeToSettingsChanges` around its writes) before any protocol note.
2. 🟡 The Buds switch the mode when a bud leaves the case even with in-ear detection off (§4.2) — to the `PROTOCOL.md` §6 item with this sample; a filmed test.
3. ❓ The SIGQUIT at 07:10:55 — which tool sent it (compare with the maintainer's commands after the film).
4. Proposal P1 (XS): log the EQ write like the other writes ("EQ written (channel N)") — today only the pw_rpc summary shows it (§3).
5. Proposal P2 (XS–S): the tile's one-frame "unavailable" look after an update (§5.3) — check `AncTileService`'s state while the mode is re-read.
6. Re-run items for the next app run (`RELEASING.md` §11a): BM-3 (TalkBack), BM-4 (holds while disconnected), BM-6 (a hold during a Connect), BM-7 (Cancel), a rotation
   with the dialog open.

## 15. External validation (fetched 2026-10-16)

- **Pigweed pw_rpc** `pw_rpc/internal/packet.proto` (raw.githubusercontent.com/google/pigweed/main), verbatim: "REQUEST = 0;" ("The client invokes an RPC. Always the
  first packet."), "RESPONSE = 1;" ("The RPC has finished."), "SERVER_STREAM = 7;" ("A message in a server stream.") — as `scripts/pwrpc_decode.py` decodes them.
- **`CompanionDeviceManager`** (android.googlesource.com, frameworks/base main, `CompanionDeviceManager.java`), verbatim: `getMyAssociations()` "@return a list of
  associations that have been previously associated with the current app"; the class text: "Applications can also use {@link #disassociate(int)} to remove an
  association". The developer.android.com reference page could not be read to the method entries (the fetch returned its navigation only) — not quoted.
- **Bluetooth controller error codes `0x13` / `0x16`:** the Bluetooth SIG Core 5.4 page (controller-error-codes.html) returned only its navigation to the fetch; the
  names were **not read verbatim from the specification**. The names used here are Wireshark's decode of the events (`tshark -r H -Y 'frame.number==1589' -V` →
  "Reason: Remote User Terminated Connection (0x13)"; HL 1350 → "Reason: Connection Terminated by Local Host (0x16)") — ⚪ taken as the specification's names.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-077-2026-10-16_06-56-14_07-10-38-Group_BM/CAP-077-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-077-2026-10-16_06-56-14_07-10-38-Group_BM/CAP-077-FINDINGS
