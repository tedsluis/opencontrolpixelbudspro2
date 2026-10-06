# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AO, EQ outer field 16 vs 18 (`CAP-053`)

**Status:** ✅ **Captured 2026-10-05 and analysed 2026-10-05** (`ai-sessions/0072`). Film and both HCI logs reviewed in full; the analysis is in
`CAP-053-FINDINGS.md`. **Central result:** field 18 was written **once**, 0.2–0.5 s after the filmed **Save** tap (frame 1838); the two release-only
windows (12.8 s and 18.7 s) and the leave-the-screen condition wrote **no** field 18. The "navigate away" step was done differently from the plan: the
maintainer left the EQ screen with the **Home** gesture and came back through **Recents**, not with Back. The planned skeleton is kept unchanged as
Appendix A (with the maintainer's app-version block).

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
| Capture ID | `CAP-053` |
| Group(s) | AO |
| Date | 2026-10-05 |
| Phone | Pixel 7a, Android 17 — the build is not on this film; the same phone's Quick Settings in `CAP-054` (same evening) reads `17 (CP3A.260905.009)` |
| Official app | Pixel Buds app **1.0.955078536** (versionCode 10253511) — the maintainer's `dumpsys package` block (Appendix A); the version is not shown on film |
| Play services | present: the Play-services claim marker `03 08 00 02 01 25` opens DLCI 0x04 (frame 702, ADR-032); its *Nearby devices* permission is not on film |
| Firmware | `release_5.203` ×3 (Case, Left, Right) in the Buds' unsolicited `GetSoftwareInfo`, frame 870 (17:47:05.488); not shown on film |
| Buds state | both buds out of the case: "Battery updated" `03 03 00 03 64 64 ff` (frame 709: Left 100, Right 100, not charging), runtime info without a Case entry (frame 883), ANC `Notify` `01 e8 e8 40` (frame 725: Adaptive, Settable `e8`); Case 55 % on screen and in GSND CONTROL `0e 01` (frame 817, short form). The case cavities look empty on film |
| Other devices | LE link `72:0a:e4:99:8d:56` (handle 0x0003, 17:47:27.003, frame 1179) — not the Buds, not followed; LE link `56:30:04:c9:fd:b5` (handle 0x0004, 17:49:53.924–17:50:01.276, frames 2146/2458) — the Buds' own LE address of this session (Device Information `03 02`, frame 704), after the film |
| Video file | `CAP-053-recording.mp4` — 184.54 s, 5,502 frames, portrait; overlay (camera clock) 17:46:34 → 17:49:39 |
| Clock offset | phone = film time + **17:46:32.5 ± 0.13 s**: status-bar minute 17:46→17:47 between film 27.37 and 27.60 s, 17:48→17:49 between 147.37 and 147.60 s (4 fps). The camera overlay runs ≈ 1.5–2 s ahead of the phone and ticks irregularly (±0.5 s) |
| Log files | `CAP-053-btsnoop_hci.log` — 2,924 packets, 17:46:25.846 → 17:53:48.054 (starts with its own `Reset`); `CAP-053-btsnoop_hci.log.last` — 3,081 packets, 17:39:52.118 → 17:46:23.518 (before the film; kept, maintainer 2026-10-05) |
| Audio track | AAC, near-silent (mean −68.1 dB, max −39.8 dB, `ffmpeg … volumedetect`); no peak above −38 dB — not used |
| Buds MAC (partial, ADR-010) | `04:00:6e:…:07` |

## Capture-integrity pre-flight

- The film covers the whole Group AO procedure (Bluetooth off at the start → EQ actions → back in the EQ screen at the end); `.log` covers the whole
  film. `.log.last` ends 9 s before the film starts.
- **Why the log is split:** `.log.last` ends with the phone's local disconnects (frames 3077–3079, 17:46:22.540–23.415, reason `0x16`) — Bluetooth
  switched off before the film. `.log` starts 2.3 s later with a `Reset` (frame 1, 17:46:25.846) followed by LE scanning only (`LE Set Extended Scan
  Enable` 101–194, 78 extended advertising reports up to frame 270) while the film shows "Bluetooth is off"; classic page/inquiry scan is enabled only
  at 17:47:02.221 (`Write Scan Enable`, frame 263), 0.3 s after the filmed Bluetooth-on tap. 🟢 for this capture: the log was restarted when Bluetooth
  went **off** (the stack's LE-only restart), not when it went on.
- Pre-filter: the Buds' classic link is handle **0x0002** (`Connect Complete` frame 396, `04:00:6e:cf:6e:07`); every RFCOMM count below is scoped to it.
  `bluetooth.addr` is empty on this log format; the handle filter matches the 6 EQ writes (positive control).

## Video review method (incl. privacy)

1. 2-s contact sheets of the whole film (92 tiles), read in full by a sub-review; every transition then narrowed by the main session at 4 fps
   (`ffmpeg -ss <t> -t <d> -i CAP-053-recording.mp4 -vf "crop=…,drawtext=%{pts},fps=4,scale=…,tile=8x…"`), and the Mid slider region every 2 s
   from 97.5 to 121 s. The sub-review's "Mid → centre at 106–108 s" was wrong: the drag is at 96.6–97.6 s (below).
2. Clock: the status-bar minute flips at both ends (4 fps), see Log Metadata.
3. Privacy (maintainer's decision 2026-10-05: commit as recorded): device name "Pixel Buds Pro 2 van Ted", carrier, the launcher home screen
   (≈ 156–162 s); no notification text, e-mail, Wi-Fi name, address or serial; no face.

## Event Timeline

Phone time = film time + 17:46:32.5. "Wire" frames are `CAP-053-btsnoop_hci.log` unless marked "last".

| Phone time | Film (s) | Action | Actor | Group step | Test-ID | Evidence |
|---|---|---|---|---|---|---|
| 17:39:52.70 – 17:46:22.54 | — (before the film) | An earlier connection: phone `Create Connection` (last 143), ACL 0x0002 (last 147), MAESTRO `SABM` 17:40:48.806 (last 512), the official app's connect reads incl. `ReadSetting` 16 = `[−1.0, 0.0, 4.0, 2.0, 0.0]` (Vocal boost) and 18 = `[−4.9, 4.3, −4.4, 3.5, −4.2]` (last 647/653), **no** `WriteSetting`; ends with Bluetooth off (last 3077–3079) | User / app | not in the plan (preparation) | — | `.log.last`; `python3 scripts/pwrpc_decode.py CAP-053-btsnoop_hci.log.last` |
| 17:46:25.846 | — | `.log` starts: `Reset`, LE-only scanning | System | — | — | frames 1–270 |
| 17:46:32.5 | 0.0 | Film starts: Quick Settings → Bluetooth dialog, "Bluetooth is off"; case open on the phone | — | pre | — | sheet tiles 1.97–27.97 s |
| 17:47:01.9 (tap 29.1–29.6 s) | 29.4 | **Bluetooth on** (switch ON at 29.60 s) | User | pre | `PAIR-003` | `Write Scan Enable` 263 (17:47:02.221), `Create Connection` 271 (17:47:02.249) |
| 17:47:02.1 → 17:47:05.0 | 29.6 → 32.5 | Row "…van Ted · Connecting…" → "Active. L: 100%, R: 100%" (row highlighted between 32.37 and 32.60 s) | System | pre | `PAIR-003` | ACL 0x0002 `Connect Complete` 396 (17:47:04.073); RFCOMM `SABM`s 0x00 482, 0x0c 516, 0x04 690, 0x0a 747, 0x08 777, **0x02 859 (17:47:05.460)** |
| 17:47:04.52 – 17:47:05.83 | — | Connect burst: Play-services claim of DLCI 0x04 (`03 08 00 02 01 25`, 702), Model ID `da 2d b1` (704), battery 100/100 (709), `08 11` → `08 13 … e8 e8 40` Adaptive (720/725), SASS exchange (744–840); GSND CONTROL burst (787–941) | System / Play services | — | — | `msgs.py` listing, FINDINGS §5 |
| 17:47:05.488 → 17:47:07.6 | — | MAESTRO: announcement channel 19 (870), the official app's sweep: `ReadSetting` 13, 1–5, 7, 11–13, 15–19, 21–32, 34–38 (877–1077), 16 = Vocal boost (1015), 18 = `[−4.9, 4.3, −4.4, 3.5, −4.2]` (1021) | App (official) | — | — | FINDINGS §2 |
| 17:47:09.4 (finger 36.1–36.87 s) | 36.9 | Gear of the Buds' row → **Device details** at 37.11 s (Left 100 %, Case 55 %, Right 100 %, Adaptive) | User | step 1 (path) | — | no wire frame (screen open) |
| 17:47:16.7 (finger 43.87–44.10 s) | 44.2 | Sound row → Sound page at 44.37 s | User | step 1 (path) | — | none |
| 17:47:18.5 (finger 45.6–45.87 s) | 46.0 | Equalizer row → **EQ screen** at 46.10 s: preset "Vocal boost", **Save disabled**, Volume EQ on | User | step 1 | — | none (no `ReadSetting` on screen open) |
| 17:47:35.1 – 17:47:36.1 | 62.6 – 63.62 | **Slider 1 = Upper treble** dragged right and released (hand gone at 63.85 s); **Save becomes enabled** | User | step 2 | `EQS-001` (not named by the Group) | `WriteSetting 4:{16:[−1.0, 0.0, 4.0, 2.0, **5.0**]}` **1577 (17:47:35.998)** → mirror 1583, OK 1584 |
| 17:47:36.3 – 17:47:49.1 | 63.85 – 76.6 | Release-only window 1: **12.8 s**, no hand on screen, Save not touched, screen unchanged | — | step 2 (wait ≥ 10 s) | — | **no field-18 write** (FINDINGS §1) |
| 17:47:49.1 – 17:47:50.3 | 76.6 – 77.85 | **Slider 2 = Treble** dragged left and released (gone at 78.12 s) | User | step 3 | `EQS-002` (not named by the Group) | `4:{16:[…, 4: **−4.8**, …]}` **1639 (17:47:50.268)** → mirror 1643, OK 1644 |
| 17:47:50.6 – 17:48:09.1 | 78.12 – 96.6 | Release-only window 2: **18.5 s**, no hand, Save not touched | — | step 3 | — | **no field-18 write**; Buds' periodic push 17:47:51.18–51.20 (1651–1662) |
| 17:48:09.1 – 17:48:10.0 | 96.6 – 97.59 | **Slider 3 = Mid** dragged to the centre (thumb at the centre at 97.59 s), released (gone 97.86 s) | User | step 4 (first half) | `EQS-003` | `4:{16:[…, 3: **0.3**, …]}` **1727 (17:48:10.180)** → mirror 1733, OK 1734 |
| 17:48:10.5 | 98.09 | **The UI shows the Mid thumb back at ≈ 0.82** (where it was before), no hand | App UI | — | — | film only |
| 17:48:20.0 – 17:48:20.3 | 107.59 – 107.86 | **The Mid thumb jumps to the centre by itself** — no hand, no wire frame between 17:48:11.224 (1755) and 17:48:29.824 (1799) | App UI | not in the plan | — | film; wire silence (FINDINGS §4) |
| 17:48:28.8 – 17:48:29.8 | 116.36 – 117.36 | **Mid dragged again**, back to ≈ 0.82, released (gone 117.60 s) | User | not in the plan (repeat of step 4's drag) | `EQS-003` | `4:{16:[…, 3: **4.2**, …]}` **1799 (17:48:29.824)** → mirror 1804, OK 1805 |
| 17:48:30.1 – 17:48:40.3 | 117.60 – 127.86 | Wait **10.2 s**, no hand, Save not touched | — | step 4 (wait) | — | **no field-18 write** |
| 17:48:40.3 – 17:48:40.6 | 127.86 – 128.10 | **Save tapped** (finger on Save at 127.86 s); toast **"EQ saved"**, preset label **"Last saved"**, Save disabled at 128.36 s | User | step 4 (Save) | `EQP-008` | `WriteSetting 4:{**18**:[−1.0, 0.0, 4.2, −4.8, 5.0]}` **1838 (17:48:40.823)** → mirror 1844, OK 1845 — 0.2–0.5 s after the tap |
| 17:48:41.23 | — | Buds' periodic push (GSND `0e 02`/`0e 01`, battery ×3, `AT+BIEV=2,100`) | Buds | — | — | 1848–1862 |
| 17:48:51.3 – 17:48:52.3 | 138.87 – 139.87 | **Slider 4 = Bass** dragged right, released (gone 140.10 s); Save enabled again | User | step 5 (drag) | `EQS-004` | `4:{16:[−1.0, **4.3**, 4.2, −4.8, 5.0]}` **1914 (17:48:52.515)** → mirror 1916, OK 1917 |
| 17:48:52.6 – 17:49:08.1 | 140.10 – 155.60 | Wait **15.5 s** (plan: ≥ 3 s), Save not touched | — | step 5 (wait) | — | **no field-18 write** |
| 17:49:08.1 – 17:49:08.3 | 155.60 – 155.87 | **Leaves the EQ screen with the Home gesture** (launcher home screen at 155.87 s) — not Back | User | step 5 **done differently** | — | **no write** (none after 1914 in the whole log) |
| 17:49:16.0 – 17:49:16.3 | 163.59 – 163.86 | Recents opened (card "Device deta…" showing the EQ) | User | not in the plan | — | none |
| 17:49:23.0 – 17:49:23.3 | 170.59 – 170.86 | Recents card tapped → **back in the EQ screen**: "Last saved", Save enabled, Bass still changed | User | not in the plan | — | none |
| 17:49:37.0 | 184.5 | Film ends on the EQ screen | — | — | — | — |
| 17:49:21.53, 17:49:51.57, 17:50:01.65 | — | Buds' periodic pushes; runtime info frames 2023, 2120, 2474 (no Case entry) | Buds | — | — | — |
| 17:49:53.924 – 17:50:01.276 | — | LE link to the Buds' LE address (handle 0x0004), local disconnect `0x16` | System | not in the plan | — | 2146, 2458 |
| → 17:53:48.054 | — | Log ends; ACL 0x0002 still up; **no `WriteSetting` after 1914** | — | — | — | `pwrpc_decode.py` |

## Step mapping (Group AO)

| Step | What the plan asked | What was done | Status |
|---|---|---|---|
| 1 | Open the EQ screen | Device details → Sound → Equalizer (46.1 s) | done |
| 2 | Slider 1: drag, release, ≥ 10 s, no Save, no navigation | Upper treble; 12.8 s; Save untouched; screen unchanged | done |
| 3 | Slider 2: same | Treble; 18.5 s | done |
| 4 | Slider 3: drag, release, ≥ 10 s, then Save | Mid dragged (17:48:09–10), the UI moved the thumb by itself 10 s later, Mid dragged **again** (17:48:29), 10.2 s wait, **Save** | done, with a **repeated** drag |
| 5 | Slider 4: drag, ≥ 3 s, navigate away (Back) without Save | Bass; 15.5 s; left with **Home**, then Recents → back into the EQ | **done differently** (Home, not Back) |
| — | (not in the plan) | the earlier connection in `.log.last`; the Recents return; film ends with an unsaved Bass change on screen | extra |

Test-IDs named by the Group: `EQS-004` (Bass slider) — exercised (slider 4); `EQP-008` (Save) — exercised (the Save tap). Also exercised, not named:
`EQS-001` (Upper treble), `EQS-002` (Treble) and `EQS-003` (Mid) — only their field-16 writes, nothing new. `PAIR-003` (reconnect) at the start.

## Analysis checklist (Group AO)

- [x] Sliders 1/2, release-only: no field-18 write in 12.8 s and 18.5 s (FINDINGS §1).
- [x] Slider 3 + Save: one field-18 write 0.2–0.5 s after the filmed tap (frame 1838).
- [~] Slider 4 + navigate away: Home/Recents wrote nothing; a Back navigation was **not** done, so the `hod` "Navigate away" path is still untested
  (FINDINGS §1, §3).
- [x] Result recorded per condition (FINDINGS §1).

## Next steps

- [x] `CAP-053-FINDINGS.md` written (`ai-sessions/0072`).
- [x] Capture Index row and `id_registry.csv` → analysed; folder renamed (2026-10-05).
- [ ] A Back-navigation sample (drag, wait, Back without Save) — `TODO.md` §3 (proposed in FINDINGS §8).

## Appendix A — the skeleton as planned (unchanged; headings one level down)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-053-2026-10-05_17-46-34_17-49-39-Group_AO` to the actual session
date/start-time/end-time, e.g. `CAP-053-2026-09-15_08-30-00_08-40-00-Group_AO`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AO, added 2026-09-13,
`ai-sessions/0017_MAINTENANCE_RESULT_2026_09_13.md` Phase 1):** `PROTOCOL.md` §4.2's "Outer field 16
vs. 18" item is a genuine, unreconciled tension: `CAP-015`'s wire timing reads as "field 18 fires on
slider-release" (no video-visible `Save` tap before any of 15 field-18 frames), but a full call-graph
trace of `fyd.d`/`fyd.e` (`REVERSE_ENGINEERING.md`'s `qjw` entry) found field 18 (`fyd.d`) reachable
through exactly **two** code paths — a dedicated `key_eq_save_button`/`title_eq_save_button` click
handler (self-describing log `"On click save EQ button"`), and, newly found in session 0017, a
**navigate-away-from-the-EQ-screen** path (`hod.java:36`, self-describing log `"Navigate away, save
EQ"`, gated on an unsaved-changes-shaped flag) — but **no** slider-release code path to field 18
anywhere in the decompiled source. This capture isolates all three candidate triggers from each
other for the first time.

### Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-053`                     |
|      Group(s)    |                     AO (new)                       |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, official Pixel Buds Companion App; full DLCI 0x02 traffic retained, raw-path extraction) |
| Video file       |    TBD — must clearly show, for each slider action: whether the `Save` button is touched, and whether the screen changes |
| Log file         |             TBD — `CAP-053-btsnoop_hci.log` (raw path, not `btsnooz.py`) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

Google pixel buds app version:
```bash
$ adb shell dumpsys package com.google.android.apps.wearables.maestro.companion   | grep -E 'versionName|versionCode|codePath|pkgFlags'
    codePath=/data/app/~~FPmr7afKNYZ6UI4YvR8lqw==/com.google.android.apps.wearables.maestro.companion-nNHllpUQ6Q2Fg1Z9gjMWOw==
    versionCode=10253511 minSdk=32 targetSdk=36
    versionName=1.0.955078536
    pkgFlags=[ HAS_CODE ALLOW_CLEAR_USER_DATA ALLOW_BACKUP KILL_AFTER_RESTORE ]
```

### Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AO)

1. Open the EQ screen (Device details → Sound → Equalizer → custom sliders).
2. Drag **slider 1** to a new position, release, and **wait ≥10s** with the `Save` button never
   tapped and **without navigating away from the EQ screen** (stay on this exact screen the whole
   time) — video must clearly show both: the `Save` button not being touched, and the screen not
   changing.
3. Repeat step 2 for **slider 2** (a second, independent release-only sample, still without leaving
   the screen or tapping Save).
4. As a clearly separated second half of the same session: drag **slider 3**, release, wait ≥10s (no
   Save tap, no navigation, replicating steps 2–3's isolation once more), then **deliberately tap the
   `Save` button** and video-confirm the tap.
5. As a third, separated part: drag **slider 4**, release, wait ≥3s, then **navigate away from the EQ
   screen** (e.g. press back to Device details) without ever tapping `Save` — video must show the
   screen change clearly.

### Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Session start, raw-path HCI snoop logging confirmed | User | — | Conn. state: TBD |
| TBD | Open EQ screen | User (App) | — | TBD |
| TBD | Slider 1: drag + release, no Save, no navigation, ≥10s wait | User (App) | `EQS-*` | TBD |
| TBD | Slider 2: drag + release, no Save, no navigation, ≥10s wait | User (App) | `EQS-*` | TBD |
| TBD | Slider 3: drag + release, no Save, no navigation, ≥10s wait | User (App) | `EQS-*` | TBD |
| TBD | Slider 3's write: `Save` button tapped, video-confirmed | User (App) | `EQP-008` | TBD |
| TBD | Slider 4: drag + release, ≥3s wait | User (App) | `EQS-*` | TBD |
| TBD | Navigate away from EQ screen (no Save tap), video-confirmed | User (App) | — | TBD |
| TBD | Session end | — | — | TBD |

### Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AO)

- [ ] For sliders 1/2 (release-only, no Save, no navigation): does a `field5{field4{field18=...}}`
      write appear on DLCI 0x02 at any point? Expected: no, per the `hju`/`hod` code trace.
- [ ] For slider 3's Save tap: confirm a field-18 write fires at that exact moment (the known,
      already-confirmed trigger).
- [ ] For slider 4's navigate-away: does a field-18 write fire at the moment of navigation, matching
      the newly-found `hod.java` "Navigate away, save EQ" trigger?
- [ ] Record the result plainly for each of the three conditions — a clean 3-way contrast (or a
      surprising positive on the release-only condition) both directly close `PROTOCOL.md` §4.2's
      own open item.

### Next steps after filling this in

- [ ] Cross-reference this session's own findings against `PROTOCOL.md` §4.2 and
      `REVERSE_ENGINEERING.md`'s `qjw` entry (`AGENTS.md` §13's traceability check).
- [ ] Write `CAP-053-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-053-2026-10-05_17-46-34_17-49-39-Group_AO/CAP-053-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-053-2026-10-05_17-46-34_17-49-39-Group_AO/CAP-053-EVENT-NOTES
