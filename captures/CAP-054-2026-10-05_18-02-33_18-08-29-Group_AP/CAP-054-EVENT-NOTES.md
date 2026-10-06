# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AP, the `0xFE2C` advertisement on case-open, connection-free (`CAP-054`)

**Status:** ✅ **Captured 2026-10-05 and analysed 2026-10-05** (`ai-sessions/0072`). Film and the HCI log reviewed in full (the log is also the first
4,807 packets of `CAP-058-btsnoop_hci.log.last`, which continues to the end of this capture's third connection). Analysis: `CAP-054-FINDINGS.md`.
**Central result:** in 610 `0xFE2C` advertising reports no field of the Battery Notification layout (`0x33`/`0x34` after the salt) appears — not in the
closed-case baseline, not after either lid opening, not with a bud out — while Android showed "Left 100% Case 53% Right 100%" in a heads-up ≈ 1.5 s
after each lid opening without any connection. The run was **not connection-free** for the bud steps: each bud taken out started a Buds-initiated
classic connection. The planned skeleton is kept unchanged as Appendix A.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
| Capture ID | `CAP-054` |
| Group(s) | AP |
| Date | 2026-10-05 |
| Phone | Pixel 7a, Android 17, build **`17 (CP3A.260905.009)`** (Quick Settings footer on film ≈ 8 s) |
| Official app | 1.0.955078536 (versionCode 10253511; maintainer's `dumpsys` block, Appendix A); **force-stopped on film** (OK at film 27.7 s); the Play Store page at the end shows the store version **1.0.990706425** with an **Update** button (so the installed one is older) |
| Play services | present (Play-services claim `03 08 00 02 01 25` on each connection's Message Stream, frames 2028, 3119, 4464) |
| Firmware | `release_5.203` (announcement frames 2295, 3029, 4550) |
| Buds | Left/Right 100 %, Case 54 → 53 % (Settings), both in the case at the start |
| Other devices | LE link `63:2c:d8:77:cb:fa` (handle 0x0002, 18:02:48.571, `.log.last` frame 260) — not the Buds |
| Video file | `CAP-054-recording.mp4` — 356.02 s, 10,654 frames; overlay 18:02:33 → 18:08:29 |
| Clock offset | phone = film + **18:02:31.4 ± 0.13 s** (minute flips between film 28.60/28.86 s and 328.35/328.62 s, 4 fps); overlay ≈ 2 s ahead of the phone |
| Log file | `CAP-054-btsnoop_hci.log` — 4,807 packets, 18:02:45.507 → 18:10:34.930; byte-identical to the first 4,807 packets of `CAP-058-btsnoop_hci.log.last` (`cmp -n 268989` equal; md5 of time/len/data equal) |
| Audio track | AAC, mostly silent (mean −63.4 dB); transients at film 152.25 s (−1 dB, the lid closing), 182.83, 245.74, 306.47 s (−26 dB, a bud seated) — used for timing only |
| Buds MAC (partial, ADR-010) | `04:00:6e:…:07` (classic); LE addresses rotate (FINDINGS §2) |

## Capture-integrity pre-flight

- The log starts 18:02:45.507 with a `Reset` (13.7 s after the film starts at 18:02:31.4) and LE scanning on (`LE Set Extended Scan Enable` frames
  101–107); the film shows Bluetooth **off** until the tap at 18:02:47.9–48.2 — the same LE-only restart as in `CAP-053`. Nothing in the film's first
  14 s needs the log (App info screen, notification shade, Quick Settings).
- **The phone's controller was scanning throughout** (scan enables at 18:02:45, :48, :54, 18:04:20, 18:04:50, 18:05:37, 18:06:07, 18:06:37, 18:06:43,
  18:07:39 — `tshark -Y "bthci_cmd.opcode==0x2042"`), and 610 `0xFE2C` reports were delivered — the positive control for every "no field" result.
- Connections to the Buds: **three** Buds-initiated ACLs (`Connect Request` 1773, 2639, 3947) — the run was connection-free only for the lid steps.

## Video review method (incl. privacy)

2-s contact sheets read in full by a sub-review; every lid, bud and screen transition narrowed by the main session at 4 fps (the case region and the
Settings text). Privacy (maintainer's decision 2026-10-05: commit as recorded): the notification shade at film ≈ 5–6 s shows a bank notification, a
LinkedIn notification naming a third person, a partial e-mail address; Quick Settings ≈ 5–22 s show the Wi-Fi name and Wallet digits; the end shows the
home screen and the Play Store (search history, another device of the account). No face.

## Event Timeline

Phone time = film + 18:02:31.4. Frames are `CAP-054-btsnoop_hci.log` (= `CAP-058-btsnoop_hci.log.last` for frames ≤ 4,807).

| Phone time | Film (s) | Action | Actor | Group step | Test-ID | Evidence |
|---|---|---|---|---|---|---|
| 18:02:31.4 | 0 | Film starts on **App info – Pixel Buds** (Force stop enabled); lid closed, both buds inside | — | 1 | — | — |
| 18:02:35 – 18:02:53 | 3.5 – 21.5 | Notification shade, then Quick Settings; Bluetooth dialog "Bluetooth is off" | User | not in the plan | — | film only |
| 18:02:45.507 | — | Log starts (LE-only stack, scanning) | System | — | — | frames 1–107 |
| 18:02:47.9 – 18:02:48.2 | 16.59 – 16.86 | **Bluetooth on**; row "Pixel Buds Pro 2 van Ted · L: 100%, C: 54%, R: 100%" (not connected) | User | 1 | — | first `0xFE2C` report 18:02:47.764 (frame 129) |
| 18:02:58.9 – 18:02:59.2 | 27.59 – 27.86 | **Force stop → OK** (Force stop greyed at 29.99 s) | User | 1 | — | none on the wire |
| 18:03:05 – 18:03:13 | 31.99 – 41.99 | Recents → **Connected devices**: "Saved devices · …van Ted · L: 100%, C: 54%, R: 100%" (not connected) | User | 1/2 | — | — |
| 18:02:47.8 – 18:04:13.5 | 16 – 102 | **Baseline, lid closed** (≈ 92 s from Bluetooth on to the opening) | — | 3 | `BATT-002` | only the closed-case advertiser `36:91:c4:7d:54:e4`: 25 reports, flags/filter type 2 (hide UI), salt, then byte **`0x2a`** (129–907) |
| 18:04:16.511 | — | That advertiser's payload changes (new salt) | Buds | — | — | frame 917 |
| 18:04:16.7 – 18:04:17.0 | 105.35 – 105.62 | **Settings: "C: 54%" → "C: 53%"** with the lid still closed | Android | not in the plan | `BATT-002` | no connection; nearest Buds frame 917 (0.2–0.5 s before) |
| 18:04:18.2 – 18:04:19.5 | 106.85 – 108.12 | **Lid opened (#1)**; case LED lit | User | 4 | `BATT-007`, `CASE-003` | — |
| 18:04:19.985 | — | Closed-case advertiser switches to the **`0x29`** form | Buds | 4 | `BATT-007` | frame 923 (55 reports to 18:04:29.257) |
| 18:04:20.418 | — | Phone restarts its LE scan | System | — | — | 933–957 |
| 18:04:21.0 – 18:04:21.2 | 109.62 – 109.85 | **Heads-up "Pixel Buds Pro 2 van Ted · Left 100% Case 53% Right 100%"**, headphone icon in the status bar | Android | 4 | `BATT-007` | **no connection** (no ACL/LE link to the Buds until 18:06:18) |
| 18:04:21.512 → 18:05:03.919 | — | A second Buds advertiser `59:c2:44:e0:c7:4e`: filter type **0 (show UI)**, salt, Random Resolvable Data `0x46` + 4 bytes; **no battery field** | Buds | 4 | `BATT-007` | 78 reports, frames 967–1331 |
| 18:04:22 – 18:05:03 | 110 – 152 | Lid open ≈ 44 s, nothing touched; panel stays "Saved devices … L 100 C 53 R 100" | — | 4 | `BATT-007` | — |
| 18:05:03.5 – 18:05:04.0 | 152.12 – 152.62 | **Lid closed (#1)** (audio click 152.25 s); headphone icon gone | User | 4 | `CASE-006` (lid part) | closed-case advertiser back to `0x2a` from 18:05:05.320 (1336) |
| 18:05:04 – 18:05:36.5 | 152.6 – 185 | Lid closed ≈ 33 s | — | 4 (wait 30 s) | — | 36:91 `0x2a` ×19+ |
| 18:05:36.5 – 18:05:37.0 | 185.10 – 185.62 | **Lid opened (#2)** | User | 4 (repeat) | `BATT-007`, `CASE-003` | 36:91 → `0x29` at 18:05:37.278 (1414); scan restart 18:05:37.655 |
| 18:05:37.949 → 18:06:15.226 | — | Show-UI advertiser `6e:e9:9a:a6:b7:26` (type 0, salt, RRD `0x46`), no battery field | Buds | 4 | `BATT-007` | 77 reports, 1451–1768 |
| 18:05:38.2 – 18:05:38.5 | 186.85 – 187.12 | **Heads-up "Left 100% Case 53% Right 100%"** again | Android | 4 | `BATT-007` | no connection |
| — | — | **The lid is not closed a second time** (stays open to the end) | — | 4 (second close) | — | **skipped** |
| 18:06:13 – 18:06:15 | ≈ 220 – 224 | Case picked up and turned to face the camera | User | not in the plan | — | — |
| 18:06:16.0 – 18:06:16.7 | 224.6 – 225.36 | **Bud out: the left slot as seen — the Left bud** (wire: Right still charging) | User | 5 | `CASE-004`, `BATT-002` | show-UI advertiser `43:ec:60:de:d5:d8` from 18:06:15.871 (1769) with an extra `0x1a` field after the RRD |
| 18:06:18.004 | — | **Buds-initiated classic connection** (`Connect Request` 1773 → ACL 0x0001 1777) | Buds | 5 | `PAIR-003` | film: "Problem connecting. Turn device off & back on" (225.99–227.99 s), then "Active. L: 100%, R: 100%" |
| 18:06:18.80 – 18:06:22.20 | — | Message Stream (DLCI 0x04): battery `03 03 00 03 64 e4 ff` (Left 100 not charging, **Right charging 100**) 2054; `08 13 … e8 00 20` (Off, Settable `00`) 2092; MAESTRO `SABM` 2282 (18:06:22.065) → the official app's sweep | Buds / app | 5 | `BATT-001`, `BATT-004` | `pwrpc_decode.py`: announcement ch 19 (2295), runtime info Case **53**, Left not charging, Right charging (2308) |
| 18:06:36.0 – 18:06:37.2 | 244.6 – 245.86 | **Left bud back in** (audio 245.74 s) | User | 5 | — | ACL 0x0001 `Disconnect Complete` **0x13** 18:06:37.242 (2523) |
| 18:07:03.0 – 18:07:03.7 | 271.6 – 272.37 | **Bud out: the right slot — the Right bud** | User | 6 | `CASE-005`, `BATT-002` | show-UI advertiser `75:77:c6:2d:85:83` from 18:07:03.226 |
| 18:07:04.081 | — | Buds-initiated ACL 0x0004 (2639/2643); the Buds open the multiplexer (`SABM` DLCI 0 2864); MAESTRO on **DLCI 0x03** (3003), Message Stream on 0x05 (3092) | Buds | 6 | `PAIR-003` | battery `03 03 00 03 e4 64 ff` (**Left charging**, Right 100) 3133; announcement ch **21** (3029) |
| 18:07:36.5 – 18:07:38.5 | 305.1 – 307.1 | **Right bud back in** (audio 306.47 s) | User | 6 | — | `Disconnect Complete` 0x13 18:07:38.850 (3479) |
| 18:08:03 – 18:08:27 | 332 – 356 | Home → Play Store → "pixel buds app" → app page: Version 1.0.990706425, Update offered | User | not in the plan | — | none |
| 18:08:27.4 | 356.0 | Film ends (lid open, both buds in the case) | — | — | — | — |
| 18:10:10.329 → 18:18:00.803 | — | **A third Buds-initiated ACL** (0x0005), after the film: both buds out (runtime info without a Case entry, 4573), Notify Transparent → Adaptive → NC (4503, 4650, 4759); ends 18:18:00.803 (0x13) — `CAP-058-btsnoop_hci.log.last` 5829 | Buds | not in the plan | — | not on film |

## Step mapping (Group AP)

| Step | Plan | Done | Status |
|---|---|---|---|
| 1 | Bluetooth on, Buds bonded not connected, app force-stopped, panel on film | Bluetooth turned on, force stop on film, Connected devices "Saved devices" (not connected) | done (order: Bluetooth on before the force stop) |
| 2 | Start log and film | film first, log 13.7 s later (LE-only restart) | done |
| 3 | Lid closed ≥ 60 s | ≈ 92 s after Bluetooth on (≈ 80 s after the force stop) | done |
| 4 | Lid open ≥ 30 s, close, wait 30 s; repeat once | open 44 s, closed 33 s, open again — **never closed again** | done once; the repeat's close **skipped** |
| 5 | One bud out ≥ 15 s, back, ≥ 15 s | Left out 20 s, back 26 s | done (the phone connected — see FINDINGS §3) |
| 6 | The other bud | Right out 34 s, back | done (connected) |
| 7 | No app scan | the official app was force-stopped; it ran again by 18:06:22 (MAESTRO opened) | done for scanning; see FINDINGS §4 |
| — | (not in the plan) | notification shade, Play Store at the end, Settings "C 54 → 53" with the lid closed, the third ACL after the film | extra |

Test-IDs named by the Group: `BATT-007` — exercised twice (lid openings); `BATT-002` — exercised (baseline and bud steps, no battery field);
`BATT-003` ("update while worn") — **not exercised** (no bud was worn). Also exercised: `CASE-003`, `CASE-004`, `CASE-005`, `PAIR-003`, `BATT-001`, `BATT-004`.

## Analysis checklist (Group AP)

- [x] Isolation check: no classic ACL during the baseline and both lid steps; **three** Buds-initiated ACLs at the bud steps and after the film
  (FINDINGS §3).
- [x] Both lid openings: no `0x3`/`0x4` field in any `0xFE2C` report (command and positive control in FINDINGS §1).
- [x] The 4 bud events: no battery field either.
- [x] Clean negative recorded; proposal for `PROTOCOL.md` §4.3 Option A in FINDINGS §7.

## Next steps

- [x] `CAP-054-FINDINGS.md` written.
- [x] Capture Index, registry → analysed; folder renamed.

## Appendix A — the skeleton as planned (unchanged; headings one level down)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-054-2026-10-05_18-02-33_18-08-29-Group_AP` to the actual session
date/start-time/end-time, e.g. `CAP-054-2026-09-15_08-30-00_08-40-00-Group_AP`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AP; redesigned 2026-10-03, `ai-sessions/0069`, lead L68-6):** the Fast Pair
`batterynotification` page names its own use case — show the notification "when the case has opened", hide it when the buds are removed or the
case is closed (`PROTOCOL.md` §4.3 Option A, Correction of 2026-10-03). No capture covers "case just opened, both buds inside, phone not
connected": `CAP-043` tested only the closed, idle case. This run opens the lid twice with the phone not connected, then brackets one bud
out/in as the earlier design did. Until 2026-10-03 this skeleton tested a single-bud insertion/removal only, on a sentence that is not on the
spec page (`A68-PROT-06`). Test-IDs: `BATT-007` (lid open), `BATT-002`, `BATT-003` (bud out/in).

### Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-054`                     |
|      Group(s)    |                     AP (new)                       |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, official Pixel Buds Companion App **force-stopped** for the entire session, matching `CAP-043`'s methodology) |
| Video file       |    TBD — must show the system Bluetooth settings panel and the exact bud removal/re-insertion moments |
| Log file         |             TBD — `CAP-054-btsnoop_hci.log` |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD (expected: not observed, per `CAP-043`'s own isolation — no classic connection this session)             |

Google pixel buds app version:
```bash
$ adb shell dumpsys package com.google.android.apps.wearables.maestro.companion   | grep -E 'versionName|versionCode|codePath|pkgFlags'
    codePath=/data/app/~~FPmr7afKNYZ6UI4YvR8lqw==/com.google.android.apps.wearables.maestro.companion-nNHllpUQ6Q2Fg1Z9gjMWOw==
    versionCode=10253511 minSdk=32 targetSdk=36
    versionName=1.0.955078536
    pkgFlags=[ HAS_CODE ALLOW_CLEAR_USER_DATA ALLOW_BACKUP KILL_AFTER_RESTORE ]
```

### Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AP)

1. Phone: Bluetooth on, the Buds bonded but **not connected** — every other phone that knows the Buds has Bluetooth off; the official app
   force-stopped (as `CAP-043`). Film the system Bluetooth panel showing the Buds as not connected.
2. Start HCI snoop logging and the camera film (the phone's Bluetooth panel **and the case** in frame; the status bar across a minute change at
   the start and the end).
3. Both buds in the case, lid **closed**, ≥ 60 s (baseline).
4. **Open the lid** on film; touch nothing for ≥ 30 s; close it; wait 30 s. Repeat once. If the phone connects by itself, note the time.
5. Lid open: take **one** bud out on film, wait ≥ 15 s; put it back, wait ≥ 15 s.
6. Repeat step 5 with the other bud.
7. No app scans in this run — the log is the system's own (`AGENTS.md` §7).

### Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Session start, app confirmed force-stopped, no active connection | User | — | Conn. state: TBD |
| TBD | Lid closed, both buds inside, ≥ 60 s (baseline) | — | `BATT-002` | TBD |
| TBD | Lid **opened**, video-confirmed; ≥ 30 s untouched | User (Hardware) | `BATT-007`, `CASE-003` | TBD |
| TBD | Lid closed; 30 s | User (Hardware) | — | TBD |
| TBD | Lid opened a second time; ≥ 30 s untouched | User (Hardware) | `BATT-007`, `CASE-003` | TBD |
| TBD | Earbud 1 (Left/Right — specify) removed from case, video-confirmed | User (Hardware) | `CASE-004`/`CASE-005` | TBD |
| TBD | Wait ≥15s, nothing touched | — | — | TBD |
| TBD | Earbud 1 re-inserted into case, video-confirmed | User (Hardware) | — | TBD |
| TBD | Wait ≥15s, nothing touched | — | — | TBD |
| TBD | Earbud 2 (the other one) removed from case, video-confirmed | User (Hardware) | `CASE-004`/`CASE-005` | TBD |
| TBD | Wait ≥15s, nothing touched | — | — | TBD |
| TBD | Earbud 2 re-inserted into case, video-confirmed | User (Hardware) | — | TBD |
| TBD | Wait ≥15s, nothing touched | — | — | TBD |
| TBD | Session end | — | — | TBD |

### Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AP)

- [ ] Isolation check (per `CAP-043`'s own method): confirm zero classic connection, zero RFCOMM,
      zero SDP to the Buds' known classic address anywhere in this log.
- [ ] For each of the 2 lid openings (`BATT-007`): the `0xFE2C` service-data advertisements in the 30 s after the lid opens — any field
      with type `0x3`/`0x4` and three value bytes? Command, exit status, and a positive control (the same filter on `CAP-043`).
- [ ] For each of the 4 bracketed events (2 removals + 2 insertions), does a `0xFE2C` service-data
      advertisement carrying the documented Battery Notification layout
      (`[Flags=0x00][Account Key Data][0x33/0x34 marker][L][R][Case]`) appear within a few seconds?
- [ ] If still a clean negative across the 2 lid openings and all 4 bud events, record that plainly — this strengthens the case
      for reframing `PROTOCOL.md` §4.3 Option A's status, per `CAP-043-FINDINGS.md` §7's own
      recommendation (a maintainer decision, not to be made unilaterally here).
- [ ] If a positive match is found, decode the payload per the documented layout and cross-reference
      against `AGENTS.md` §13.6's zero-creativity rule.

### Next steps after filling this in

- [ ] Cross-reference this session's own findings against `PROTOCOL.md` §4.3 Option A and
      `CAP-043-FINDINGS.md` (`AGENTS.md` §13's traceability check).
- [ ] Write `CAP-054-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-054-2026-10-05_18-02-33_18-08-29-Group_AP/CAP-054-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-054-2026-10-05_18-02-33_18-08-29-Group_AP/CAP-054-EVENT-NOTES
