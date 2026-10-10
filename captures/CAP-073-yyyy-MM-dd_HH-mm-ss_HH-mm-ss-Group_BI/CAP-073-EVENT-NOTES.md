# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, the secondary user without Google Play) — Group BI, the rebuilt 1.2.0 (`CAP-073`)

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

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-073` |
| Group(s) | BI (new) |
| Date | TBD |
| Phone | Pixel 9a, GrapheneOS (Android 17, `CP3A.261005.005` in `CAP-072`) — the secondary user without Google Play (user 10 in `CAP-072`; P0) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.2.0** rebuilt (versionCode 10200), the release APK of `scripts/release.sh` kept as `RELEASING.md` B3 says — **Info tab on film**: "App: 1.2.0, build <the B1 hash> (<date>)", no "-dirty"; APK and certificate SHA-256 from the script's output |
| Previous version in this user | 1.2.0 build `ec6d163` (installed 2026-10-09 for `CAP-072`) — the update is from it (same versionCode: an equal code installs over the tested one, `RELEASING.md` rules) |
| Official Pixel Buds app | not used (not in this user) |
| Buds | Pixel Buds Pro 2, firmware `release_5.203` expected (any other ⇒ Safe Mode: say so, stop part III's writes) |
| Video files | TBD — the screen recording (with sound) for the whole run; the camera (with sound) for part II only |
| Log files | TBD — the HCI snoop logs, the app's debug exports, the app logcat, the system log (P6), the three `uiautomator` dumps (BI-14), the P0/P1 outputs |

## Preparation

What `CAP-072` missed is marked ★.

| # | Check | Done |
|---|---|---|
| P0 | In the test user: `adb shell am get-current-user`; `adb shell pm list packages --user <id> \| grep -i -E "gms\|vending"; echo "exit=$?"`; positive control `grep opencontrol` (exit 0) — save the outputs **with the exit statuses** | ☐ |
| P1 | In the installed 1.2.0 (`ec6d163`): Dark mode **On**, Debug mode **on** (on film). `adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds \| grep -E "firstInstallTime\|lastUpdateTime\|versionCode"` → save. `adb install --user <id> -r dist/1.2.0/opencontrol-pixelbudspro2-1.2.0.apk` — **no uninstall**; the same `dumpsys` → save | ☐ |
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

## Steps

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

### I. The minimal release run (screen recording only; `APP_TESTPLAN.md` W1–W3; `PAIR-003`, `BATT-004`, `FW-003`, `ANC-001`…`ANC-004`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BI-1 | P0–P9 done; lid open; the Connection tab will open first | open the app | dark (kept); no crash; the session opens by itself; "Both buds in the case" | DLCI 0x02 open, the announcement, the thirteen requests in order, one `GetHardwareInfo` last, answered; **no** DLCI 0x04 claim held (the Connection tab is shown: the snapshot claim is released ≈ 1.5 s after its answer) | `PAIR-003`, `BATT-004` | a crash; light theme; a request missing or out of order; DLCI 0x04 still open 3 s after the snapshot |
| BI-2 | ready | gear → **Info**: hold 3 s on the build line, the firmware, the serials | "App: 1.2.0, build <B1 hash> (<date>)", no "-dirty"; `release_5.203` ×3; Case / Right bud / Left bud | nothing | `FW-003` | "-dirty"; another hash than B1's; no serials |
| BI-3 | ready, Connection tab | **Disconnect**, **Connect** | ready | the thirteen requests again; one `GetHardwareInfo` | `PAIR-003` | — |
| BI-4 | both buds in the ears | Quick Settings **Bluetooth off**; 10 s; **on** | "Bluetooth is disabled.", then ready by itself | "Bluetooth adapter: ON -> TURNING_OFF" and "Session loss cause: Bluetooth was switched off on this phone" in the export; after on, the automatic re-open and the thirteen requests | `PAIR-003` | no re-open |
| BI-5 | ready | Debug → **Export** | "Debug log saved (N lines)." | — | — | an empty file |

### II. The hold (ADR-061) — **camera on, buds and head in view**; `APP_TESTPLAN.md` W4–W9; `TOUCH-007`, `ANC-001`…`ANC-004`, `CASE-004`, `CASE-005`

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

### III. What `CAP-072` did not run (screen recording; `APP_TESTPLAN.md` W10–W12, T11, C12, S12, H5, T7, V8; `AUDIO-002`, `AUDIO-003`, `INEAR-006`, `INEAR-004`)

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

## Don'ts

- Do not uninstall the installed 1.2.0 and do not clear the app's data (P1 — the run is an update).
- Do not open the app before the film (P9); do not open the Owner user during the run.
- In part II, do not tap anything during the 30 s of BI-6 or between a hold and its (i) check.
- Do not tap two things within 5 s of each other, except in BI-15 (the early tap).
- Do not write a serial number unredacted anywhere in this folder (first 4 + last 2 characters).

## After the run

Into this folder: the screen recording, the camera film (part II), the system log, every debug export, the app logcat if saved, the HCI snoop logs, the three
`uiautomator` dumps, the P0/P1 outputs, `dist/1.2.0/`'s `.sha256` and the script's certificate line; then `sha256sum *` into a file. Before committing:
blur phone numbers, Wi-Fi names, addresses and other device names. Commit on the release branch after the build commit (`RELEASING.md` C3) — never on `main`.

## Analysis checklist

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
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-073-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BI/CAP-073-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-073-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BI/CAP-073-EVENT-NOTES
