# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AT, `SDP-001` 3rd attempt with a process-liveness check (`CAP-058`)

**Status:** ✅ **Captured 2026-10-05 and analysed 2026-10-05** (`ai-sessions/0072`). Film, both HCI logs and the `dumpsys` file reviewed in full;
analysis in `CAP-058-FINDINGS.md`. **Central result:** the companion app's process was **killed by the force stop and running again ≈ 10 s later**,
before the Forget and before both filmed SDP browses (outcome (a) of the revised Group AT is not met; (b) only in part — the restart is on record, its
cause is not). The "default internal rfcomm socket" UUID `3a046f6d-…` appears in **none** of the four full SDP browses (two filmed, two before the
film) nor anywhere else in the logs. The procedure followed was a mix of the committed and the revised Group AT text (below). Fourteen extra tests
follow the pairing (X1–X14). The planned skeleton is kept unchanged as Appendix A.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
| Capture ID | `CAP-058` |
| Group(s) | AT |
| Date | 2026-10-05 |
| Phone | Pixel 7a, Android 17 (the Quick Settings build string at film ≈ 22 s is too small to read; `CAP-054` shows `CP3A.260905.009` the same evening) |
| Official app | 1.0.955078536 (maintainer's statement; not shown on film — the app's About page shows no version) |
| Play services | present (Fast Pair half-sheets on film; Play-services claim `03 08 00 02 01 25` on DLCI 0x04, e.g. frame 5454) |
| Firmware | `release_5.203` (announcements 2834, 4295); film: More settings → "Firmware update · Up to date" |
| Buds | L 100 / C 49 → 48 / R 100 (film); both buds in the open case during the pairing (the case LED, the charging bit `e4` in battery frames, e.g. 5460) |
| Other devices | LE links to `7c:cc:c7:81:ef:bf` (0x0002, 21:40:07.943) and `71:cd:95:b0:2b:b5` (in `.log.last`, 19:53–21:40:04) — not the Buds; four TVs listed in "Pair new device" (film ≈ 134 s) |
| Video file | `CAP-058-recording.mp4` — 391.01 s, 11,695 frames; overlay 21:39:51 → 21:46:22 |
| Clock offset | phone = film + **21:39:51.3 ± 0.13 s** (minute flips between film 8.60/8.87 s and 368.59/368.86 s); the overlay agrees with the phone to ≈ ±0.5 s. **Laptop clock** (the `date` lines of the `dumpsys` file) against the phone: **not measured** — the terminal is not on film |
| Log files | `CAP-058-btsnoop_hci.log` — 6,093 packets, 21:40:06.636 → 21:49:13.292; `CAP-058-btsnoop_hci.log.last` — 13,250 packets, 18:02:45.507 → 21:40:04.504 (its first 4,807 packets are `CAP-054-btsnoop_hci.log`; kept, maintainer 2026-10-05) |
| Other file | `CAP-058-adb-shell-dumpsys-activity-processes.txt` — six runs of `date ; adb shell dumpsys activity processes \| grep -i com.google.android.apps.wearables.maestro.companion` (laptop clock) |
| Not present | CAP-058-proc-events.txt (not made) (`logcat -b events`), `pidof` outputs, a system log, a bugreport — the revised procedure's process log was not made |
| Audio track | AAC, near-silent (mean −70.2 dB, max −35.9 dB at 220.3–220.6 s) — not used |
| Buds MAC (partial, ADR-010) | `04:00:6e:…:07` |

## Capture-integrity pre-flight

- `.log.last` ends 21:40:04.504 with the Bluetooth-off sequence (last disconnect 21:40:04.400, an LE link) — the filmed Bluetooth-off tap at
  21:40:03.9–04.1. `.log` starts 21:40:06.636 with a `Reset` (LE-only restart, as `CAP-053`); the filmed Bluetooth-on tap is at 21:40:07.1–07.6.
  Together the two logs cover the film without a gap of more than 2.1 s, in which the film shows the Bluetooth dialog only.
- The log runs to 21:49:13, 2 m 51 s after the film; the last Buds frame is 21:46:19.589 (5866); ACL 0x000d is not closed in the log.
- Handles of the Buds: `.log.last` 0x0001/0x0004/0x0005 (`CAP-054`), LE 0x000d + ACL 0x000f (21:36), LE 0x0010 + ACL 0x0012 (21:37); `.log` LE 0x0003 +
  ACL 0x0005 (21:41:17), LE 0x0006, ACLs 0x0008/0x000a/0x000c (failed), ACL 0x000d (21:42:05), LE 0x000e/0x000f (21:42:10, 21:42:30). The filters
  below are scoped by these handles; `bluetooth.addr` is empty on this format.

## Which Group AT procedure was followed

Neither text exactly. **Committed** (2026-09-24): force-stop, Forget, liveness check, re-pair through Bluetooth settings, then open the app. **Revised**
(uncommitted, 2026-10-05): `pidof` with a positive control, `logcat -b events` to a file, `adb shell am force-stop`, a `pidof` after each step, pairing
mode via the case button, Settings → Pair new device, ≈ 30 s wait, then open the app. **Done:** the force stop through Settings → App info (not `adb`), a
liveness check with `dumpsys activity processes | grep -i com.google.android.apps.wearables.maestro.companion` — the **corrected package name** of the
revised text, six runs, the first (process present) serving as the positive control — but **no** events log and **no** `pidof`; the Forget; the case
button (pairing mode); the **Fast Pair half-sheet's Connect** (the revised text says not to use it), then Settings → Pair new device; the app was never
opened from the launcher — its screens appeared by themselves (Fast Pair → "Set up device" → the app's permission screen and CompanionDeviceManager's
dialog).

## Event Timeline

Phone time = film + 21:39:51.3. "last" = `CAP-058-btsnoop_hci.log.last`, other frames `CAP-058-btsnoop_hci.log`. Laptop-clock rows are marked *(laptop)*.

| Phone time | Film (s) | Action | Actor | Group step | Test-ID | Evidence |
|---|---|---|---|---|---|---|
| 18:10:10 – 18:18:00.803 | — | `CAP-054`'s third Buds-initiated connection ends (0x13) | Buds | (CAP-054) | — | last 3951 → 5829 |
| 19:53:01 – 21:40:04 | — | LE links to `71:cd:95:b0:2b:b5` (not the Buds) | other device | — | — | last 6542–13248 |
| 21:31:03.366 | — | `am_proc_start … maestro.companion … bound-service … MaestroCompanionDeviceService` (PID 5056) — **context only**: the chat that wrote the prompt read it on the phone; not a file of this capture | System | — | — | prompt `0072` §2 |
| 21:34:40.246 | — | **Forget #0** (`Delete Stored Link Key`) — not on film | User | not in the plan (preparation) | `PAIR-004` | last 9436 |
| 21:36:28.236 – 21:36:42.545 | — | **Fast Pair pairing A**: LE link to the Buds' public address (0x000d), key-based pairing (`0x0c04` 10169, passkey `0x0c07` 10326), SSP Buds IO DisplayYesNo (10253), confirmation reply 10330, link key type **0x05** (10333), account key write `0x0c0a` (10496); **full SDP browse** 21:36:30.274–21:36:34.167 (10403–10996); RFCOMM 0x0c, 0x0a, 0x08, 0x04 — **no MAESTRO**; local disconnect 0x16 21:36:42.541 and **Forget #1** 21:36:42.545 (11052) | User / Play services | not in the plan | `PAIR-001`, `SDP-001` | last 10091–11052 |
| *(laptop)* ≈ 21:36:43 | — | PID **6219** started (its SettingsProvider connection is "+3m38s582ms" old at the 21:40:22 run) | System | — | — | `dumpsys` run 1 |
| 21:37:06.596 – 21:37:25.348 | — | **Fast Pair pairing B**: LE 0x0010, KBP 11904, SSP DisplayYesNo 11947, key **0x05** 11982, account key 12045; **full SDP browse** 21:37:08.293–21:37:11.737; RFCOMM 0x0a, 0x08, 0x0c, 0x04; **MAESTRO `SABM` 21:37:10.627** (12545) after the pigweed search 12529; ACL ends 0x13 | User / Play services / app | not in the plan | `PAIR-001`, `SDP-001` | last 11862–12921 |
| 21:39:51.3 | 0 | Film starts: Bluetooth dialog, "…van Ted · L: 100%, C: 49%, R: 100%" (bonded, not connected); case closed | — | — | — | — |
| 21:40:03.9 – 21:40:04.1 | 12.6 – 12.87 | **Bluetooth off** | User | not in the plan | — | `.log.last` ends 21:40:04.504 |
| 21:40:06.636 | — | `.log` starts (LE-only restart) | System | — | — | frame 1 |
| 21:40:07.1 – 21:40:07.6 | 15.87 – 16.37 | **Bluetooth on** | User | prep | — | — |
| 21:40:15 – 21:40:31 | 23.97 – 40 | Recents → **App info · Pixel Buds** → Force stop → dialog | User | 1 | `SDP-001` | — |
| *(laptop)* 21:40:22 | — | `dumpsys` run 1: **process present, PID 6219** (`MaestroCompanionDeviceService` bound by `android`) — the positive control | Maintainer | 2 (check) | `SDP-001` | `.txt` lines 1–23 |
| 21:40:32.1 – 21:40:32.4 | 40.87 – 41.11 | **Force stop → OK** (Force stop greyed) | User | 1 | `SDP-001` | no Buds link exists (none since 21:37:25) |
| *(laptop)* 21:40:35 | — | `dumpsys` run 2: **no output — process absent** | Maintainer | 2 | `SDP-001` | `.txt` lines 24–25 |
| 21:40:39.3 – 21:40:43.3 | 47.98 – 51.97 | Recents → **Connected devices** ("Saved devices · …van Ted · L 100 C 49 R 100") | User | not in the plan | — | — |
| *(laptop)* ≈ 21:40:42 | — | **PID 7893 starts** (SettingsProvider connection "+8s974ms" at the 21:40:51 run; `MaestroSliceProvider` used by `system` "+5s860ms" and Play services "+5s899ms") | System | — | `SDP-001` | `dumpsys` run 3 |
| 21:40:45 – 21:40:47 | 53.98 – 55.98 | Gear → **Device details** (Left 100 %, Case 49 %, Right 100 %; Forget / Connect; More controls: **Find device · "See location info or ring device"**, Controls and gestures, Sound, Hearing wellness, Audio switch — checked on the frame at 58 s) | User | not in the plan | — | — |
| *(laptop)* 21:40:51 | — | `dumpsys` run 3: **present, new PID 7893**, process state `fg … (provider)`; bound: `MaestroDeviceSettingsProviderService` by `com.android.settings`, `MaestroCompanionDeviceService` by `android` | Maintainer | 2 | `SDP-001` | `.txt` lines 26–58 |
| 21:40:53.6 – 21:40:53.9 | 62.34 – 62.61 | **Forget** tapped → "Forget device?" | User | 1 (Forget) | `PAIR-004` | — |
| 21:40:57.9 – 21:40:58.1 | 66.61 – 66.84 | **"Forget device" confirmed**; the Buds' row disappears | User | 1 | `PAIR-004` | `Delete Stored Link Key` **21:40:57.841** (1422) |
| *(laptop)* 21:41:00 | — | `dumpsys` run 4: present, PID 7893 (`prcp`, service) | Maintainer | 2 | `SDP-001` | `.txt` |
| 21:41:10.7 | 79.4 | **Lid opened** | User | 3 | — | — |
| 21:41:11.4 – 21:41:15.9 | 80.1 – 84.6 | Hand on the back of the case (button); **white LED** from 80.35 s — pairing mode | User | 3 | `CASE-008` | from 21:41:14.543 the Buds' **public** address advertises Fast Pair *discoverable* data `00 37 da2db1 18 02` (frame 2081; 85 reports): version `00`, `0x37` = model ID (3 bytes) `da 2d b1`, `0x18` = capability map (1 byte) `02` |
| 21:41:14.9 – 21:41:15.1 | 83.61 – 83.85 | Connected devices shows "Pixel Buds Pro 2 · Tap to pair. Earbuds will be tied to …"; **Fast Pair half-sheet** ("…will appear on devices linked with <e-mail>") | Play services | not in the plan | — | — |
| 21:41:17.6 – 21:41:17.9 | 86.35 – 86.61 | **Connect** on the half-sheet → "Connecting…" | User | **done differently** (Fast Pair, not Settings) | `PAIR-001` | LE 0x0003 21:41:17.790 (2116); KBP 2156–2170; `Delete Stored Link Key` + `Create Connection` 2171/2173 |
| 21:41:18.236 – 21:41:19.211 | — | Classic pairing: Buds IO **DisplayYesNo** (2204), confirmation request 2223 → phone reply **2232 (21:41:18.992, no dialog on film)**, Simple Pairing Complete 2234, link key **0x05** 2235; account key write `0x0c0a` 2334 (21:41:19.864) | Play services | 3 | `PAIR-001` | — |
| 21:41:19.342 – 21:41:23.169 | — | **Full SDP browse #1** (L2CAP browse response 2272: 8 services, 6 custom UUIDs, **no `3a046f6d`**), then targeted searches GSND AUDIO/CONTROL, GFPS, and the **pigweed UUID** (2813, 21:41:23.044) | Android / app | 3 | **`SDP-001`** | `sdp` listing; byte search (FINDINGS §2) |
| *(laptop)* 21:41:21 | — | `dumpsys` run 5: present, PID 7893 (`cch-empty`) | Maintainer | 2 | `SDP-001` | `.txt` |
| 21:41:20.9 | 89.61 | "**Ready to use** · Pixel Buds Pro 2 is connected to someone else's account…" (Ask owner / Remove previous owner / Start using) | Play services | not in the plan | — | out of scope (ADR-008) |
| 21:41:19.96 – 21:41:22.21 | — | RFCOMM `SABM` 0x0a, 0x08, Buds' 0x09 (HFP), 0x04 | System | — | — | 2424–2747 |
| 21:41:23.288 | — | **MAESTRO `SABM`** (2823) → announcement channel 21 (2834) → the app's requests (2837–2944) | App | — | — | the app process ran (ADR-018) |
| 21:41:24.223 / 21:41:25.421 | — | LE 0x0003 disconnect 0x16; **ACL 0x0005 disconnect 0x13** (2948) | Buds / system | — | — | — |
| 21:41:27.4 – 21:41:31.1 | 96.12 – 99.85 | "Ask owner to share device" → "Ask owner to share" → **Done** | User | not in the plan | — | — |
| 21:41:31.4 – 21:41:33.1 | 100.12 – 101.85 | Half-sheet **"Set up device"** → **Set up** | User | not in the plan | — | — |
| 21:41:33.4 | 102.12 | The **Pixel Buds app**'s screen "Allow a connection to your Pixel Buds" (Continue) | App | 4 (**done differently**: opened by Fast Pair "Set up") | `SDP-001` | — |
| 21:41:34.9 – 21:41:36.4 | 103.6 – 105.12 | Continue → **CompanionDeviceManager dialog** "Allow the app Pixel Buds to access Pixel Buds Pro 2 van Ted?"; the fingertip is between *Allow* and *Don't allow* (105.12 s) — **not identifiable** | User | 4 | — | — |
| 21:41:37.1 | 105.85 | Device details (Case 48 %) + a second Fast Pair half-sheet | Play services | — | — | — |
| 21:41:38.4 – 21:41:38.6 | 107.12 – 107.37 | **Connect** again → Connecting… | User | not in the plan | `PAIR-001` | LE 0x0006 21:41:38.562, KBP 3161–3173, LE disconnect 0x16 21:41:38.982 |
| 21:41:40.088 – 21:41:45.469 | — | Three classic attempts (0x0008, 0x000a, 0x000c): each `User Confirmation Request` answered with **Negative Reply** by the phone (3341, 3410, 3478), disconnect 0x16 | Play services | — | — | — |
| 21:41:45.6 | 114.35 | **"Couldn't connect · Try manually pairing…"**; its right button at 115.85–116.35 s | Play services / User | — | — | — |
| 21:42:03.4 – 21:42:03.6 | 132.12 – 132.35 | Connected devices → **Pair new device** (lists the Buds and four TVs) | User | 3 (revised: Settings → Pair new device) | `PAIR-001` | Extended Inquiry Result 3724 (21:42:04.247) |
| 21:42:05.1 – 21:42:05.6 | 133.85 – 134.35 | Taps "Pixel Buds Pro 2 van Ted" | User | 3 | `PAIR-001` | `Delete Stored Link Key` + `Create Connection` 3744/3746 (21:42:05.231) → ACL 0x000d 3749 |
| 21:42:05.517 – 21:42:05.824 | — | SSP: Buds IO **NoInputNoOutput** (3789), confirmation request 3797 | System | 3 | `PAIR-001` | — |
| 21:42:06.4 – 21:42:07.6 | 135.12 – 136.36 | Dialog "Pair with Pixel Buds Pro 2 van Ted?" → **Pair** | User | 3 | `PAIR-001` | **User Confirmation Request Reply 21:42:07.643** (3801), link key **0x04** (3804) |
| 21:42:07.957 – 21:42:10.468 | — | **Full SDP browse #2** (3818–4388; browse response 3840: same 8 services, **no `3a046f6d`**), pigweed search 4274 (21:42:09.895) | Android / app | 3 | **`SDP-001`** | FINDINGS §2 |
| 21:42:08.4 | 137.12 | Connected devices: "Media devices · …van Ted · Active. 100% battery." | — | 3 | — | RFCOMM 0x0c 4004, 0x04 4008, 0x08 4083, 0x0a 4146; A2DP AAC (3904) |
| 21:42:09.951 | — | **MAESTRO `SABM`** (4284) → announcement ch 21 (4295), the app's first requests (4298–4400) | App | — | — | — |
| *(laptop)* 21:42:14 | — | `dumpsys` run 6: present, PID 7893 | Maintainer | — | `SDP-001` | `.txt` |
| 21:42:11 – 21:42:31 | 140 – 160 | Device details (no app rows yet); nothing touched | — | — | — | the app's `ReadSetting` sweep 21:42:26.480–28.422 (4447–4542), no filmed trigger |
| 21:42:30.218 – 21:42:36.206 | — | LE 0x000f to the Buds' LE address: KBP, account key write `0x0c0a` 21:42:31.444 (4635) | Play services | — | — | — |
| 21:42:32.6 – 21:42:34.6 | 161.36 – 163.36 | Half-sheet "Save device to <e-mail>" → **Save** (right button) | User | not in the plan | — | — |
| 21:42:35.6 – 21:42:36.8 | 164.36 – 165.59 | Sheet "Set up device" → **Set up** → "Starting setup…" → the app's "Allow a connection" | User | not in the plan | — | — |
| 21:42:39.1 – 21:42:39.6 | 167.86 – 168.36 | **CDM dialog** again; the fingertip between *Allow* and *Don't allow* — **not identifiable** | User | not in the plan | — | — |
| 21:42:40.6 | 169.36 | Device details now with the app's rows (Sound, Hearing wellness, More settings, Audio switch) | App | — | — | — |
| 21:42:53 | ≈ 182 | Device details (connected, "Active"): Digital assistant, Controls and gestures, Sound, Hearing wellness, More settings, Audio switch — **no Find device** (frames at 182 s and 366 s); before the Forget (not connected, 58 s) the list had Find device and no Digital assistant / More settings | App | not in the plan | `FIND-001` (observation) | film only |

### The extra tests (after the pairing, not in the plan)

| # | Phone time | Film (s) | Action on film | Wire (handle 0x000d) | Test-ID |
|---|---|---|---|---|---|
| X1 | 21:42:57.4 / 21:43:08.0 | 186.2 / 196.8 | Device details: **Phone calls** off, then on | HFP DLCI 0x0c `DISC` **4877 (21:42:57.458)**; on: SDP HF query 4960, `SABM` 0x0c **4971 (21:43:08.082)**, SLC | none |
| X2 | 21:43:18.0 / 21:43:26.4 | 206.8 / 215.1 | **Media audio** off, then on | AVDTP `Close` **5091 (21:43:18.078)**, A2DP + AVCTP L2CAP closed, and the phone's **`DISC` of DLCI 0x08 and 0x0a** 5110/5111 (21:43:18.706/.709); on: AVDTP Discover/SetConfiguration (AAC)/Open **5155–5175 (21:43:26.469–.508)**, `SABM` 0x08 5207, 0x0a 5232, AVCTP 5301 | none |
| X3 | 21:43:34.1 / 21:43:42.7 | 222.8 / 231.4 | **Input device** off, then on | HID-Interrupt/Control L2CAP disconnect **5347/5350 (21:43:34.124)**; on: HID-Control/Interrupt connect **5362/5373 (21:43:42.727)** | none |
| X4 | 21:43:51.3 – 21:43:53.3 | ≈ 240 – 242 | **Audio switch** page opened | nothing sent (no RFCOMM data and no L2CAP signalling from the phone on 0x000d between 21:43:43.106 and 21:43:57.167) | `SWITCH-001` |
| X5 | 21:43:57.1 / 21:44:06.9 | 245.9 / 255.7 | **Use audio switch** off, then on | off: Message Stream `07 42 …` + `07 11 00 14 01 01 **00 00** …` (5411/5412, 21:43:57.167), `DISC` 0x04 5413, re-open and `07 11 … 00 00` again (5466, 5476, 5483); on: `07 42` + `07 11 … **80 00**` (5505/5506, 21:44:06.968), `DISC` 5507, re-open, `07 11 … 80 00` (5562 …), `07 21`/`07 41 "in-use"`/`07 40`/`07 42` | `SWITCH-001` |
| X6 | 21:44:17.3 – 21:44:19.3 | ≈ 266 – 268 | **More settings** (Firmware "Up to date", Multipoint on, In-ear detection on, Case sounds, About, Usage & diagnostics off) | nothing sent | — |
| X7 | 21:44:21.3 – 21:44:23.3 | ≈ 270 – 272 | **Case sounds** (Bud return on, Other alerts on) | nothing sent | — |
| X8 | 21:44:25.8 / 21:44:33.9 | 274.5 / 282.7 | **Bud return** off, then on | `WriteSetting 4:{28:0}` **5623**, OK 5628; `4:{28:1}` **5643**, OK 5648 | `CASE-001` |
| X9 | 21:44:43.0 / 21:44:52.8 | 291.7 / 301.6 | **Other alerts** off, then on | `4:{27:0}` **5680**, OK 5683; `4:{27:1}` **5697**, OK 5702 | `CASE-002` |
| X10 | 21:44:57.3 – 21:45:11.3 | ≈ 306 – 320 | Back to More settings, scrolled (Tips & support, Notifications, Send feedback, Safety…) | nothing sent | — |
| X11 | 21:45:15.0 – 21:45:15.5 | 323.7 – 324.2 | **Hearing wellness** (Current level OK 0 dB; exposure OK; 24 h 0 %, 7 days 2 %; Volume level notifications on) | `Dosimeter.FetchDailySummaries` on channels 19 and 21, **5736 (21:45:14.872)** | `WELL-001` |
| X12 | 21:45:18.5 / 21:45:18.8 / 21:45:24.6 / 21:45:34.2 | 327.3 / 327.6 / 333.4 / 343.0 | **Volume level notifications**: off and straight back on (switch OFF at 327.72 s, ON at 328.22 s), then off, then on | `WriteSetting 4:{21:0}` **5747**, `4:{21:1}` **5753**, `4:{21:0}` **5767**, `4:{21:1}` **5790**, each OK and mirrored | `WELL-001` |
| X13 | 21:45:41 – 21:45:59 | ≈ 350 – 368 | Back to Device details (Left 100, Case 48, Right 100) | nothing sent | — |
| X14 | 21:46:01 – 21:46:22 | ≈ 370 – 391 | More settings → **About**: Earbuds status Connected, Model Pixel Buds Pro 2, serial numbers Left …3147, Right …3209, Case …0251; film ends | nothing sent (the serials come from `GetHardwareInfo` 4301 → 4328 at connect) | — |

## Step mapping (Group AT)

| Step | Committed text | Revised text | Done | Status |
|---|---|---|---|---|
| 1 | Force-stop, then Forget | `am force-stop` + `pidof`; Forget + `pidof` | Settings Force stop 21:40:32; Forget 21:40:58 (in that order) | done; `pidof` not used |
| 2 | Liveness check right before "Pair" | `pidof` before tapping the name; events log running | `dumpsys … \| grep` ×6 (21:40:22 present, :35 absent, :51 present again, 21:41:00, :21, 21:42:14 present); **no events log** | done differently |
| 3 | Re-pair through Bluetooth settings only | case button, Settings → Pair new device, no half-sheet | case button 21:41:11; **Fast Pair half-sheet Connect** 21:41:17.6 (pairing #1); then Settings → Pair new device 21:42:03–07 (pairing #2) | done, **twice**, the first **differently** |
| 4 | Open the app, let it re-fetch | open from the launcher, note the time | never opened from the launcher; its screens came through Fast Pair "Set up" (21:41:33, 21:42:36) | done differently |
| 5 | `SDP-002` if an update is pending | same | "Up to date" (X6) — no update | not applicable |
| — | — | — | two Forget + Fast Pair pairings before the film (21:34–21:37); Bluetooth off/on at the start; X1–X14 | extra |

Test-IDs: `SDP-001` — exercised (four browses; the process-dead condition not met); `SDP-002` — not applicable (no update pending); `PAIR-001`
(forget and re-pair) — exercised four times (two before the film, two on film); `PAIR-004` (Forget) — the Forget's `Delete Stored Link Key` is on the
wire (1422); `CASE-008` (pairing mode by the case button) — on film; `CASE-001`/`CASE-002` — X8/X9; `SWITCH-001`, `WELL-001` (candidates without a
scenario) — X4/X5, X11/X12.

## Analysis checklist (Group AT)

- [x] Every SDP transaction listed and searched for `3a046f6d-…` in both byte orders (FINDINGS §2) — 0 occurrences.
- [x] Process state at each browse: alive (FINDINGS §1).
- [x] Outcome (a)/(b) applied (FINDINGS §1): neither cleanly.
- [x] Extra tests recorded (FINDINGS §6).

## Appendix A — the skeleton as planned (unchanged; headings one level down)

**Status:** 🔲 **Not yet captured — skeleton only.** Created 2026-09-24 (`ai-sessions/0045`, closing 0044 finding D-8: `CAP-058` was
registered `planned` on 2026-09-18 by `ai-sessions/0031` but, unlike `CAP-052`–`CAP-057`, had no placeholder folder). Fill in every
`TBD` below after recording, per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and `PROJECT_RULES.md`
rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from the placeholder
`CAP-058-2026-10-05_21-39-51_21-46-22-Group_AT` to the actual session date/start-time/end-time.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AT, maintainer-approved 2026-09-18, `ai-sessions/0031`):** two prior attempts at
`SDP-001` (`CAP-033`, `CAP-044`) were each capped at 🟡 HYPOTHESIS by a procedural gap (`CAP-033`: Forget before Force-stop; `CAP-044`:
no confirmed process-dead window before the "Pair" tap). This attempt adds an explicit on-device process-liveness check immediately
before the "Pair" tap (`CAP-044-FINDINGS.md` §5).

### Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-058`                     |
|      Group(s)    |                     AT (new)                       |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, official Pixel Buds Companion App force-stopped for the first half) |
| Video file       |    TBD — must show the process-liveness check and the "Pair" tap |
| Log file         |             TBD — `CAP-058-btsnoop_hci.log` (raw path, not `btsnooz.py`) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

### Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AT)

1. **[`SDP-001`]** Force-stop the companion app, then Forget the device — in that order.
2. Immediately before tapping "Pair", run the process-liveness check (e.g. `adb shell dumpsys activity processes | grep -i pixelbuds`)
   on camera or in a parallel shell capture; it must show the companion app process absent up to and including the SDP browse.
3. Re-pair through Bluetooth settings only.
4. **Second half:** open the companion app normally and let its own `fetchUuidsWithSdp()` re-fetch run.
5. **[`SDP-002`]** only if a firmware update happens to be pending (opportunistic).

### Event timeline

| Wall clock | Action | Test-ID | Wire evidence |
|---|---|---|---|
| TBD | Force-stop companion app | `SDP-001` | — |
| TBD | Forget the Buds | `SDP-001` | TBD |
| TBD | Process-liveness check (app absent) | `SDP-001` | — |
| TBD | "Pair" tap → SDP browse | `SDP-001` | TBD |
| TBD | Open the companion app → re-fetch | `SDP-001` | TBD |

### Decode / analysis checklist

- Pre-filter by the Buds' address (`AGENTS.md` §13), then `btsdp`: does the "default internal rfcomm socket" UUID
  (`3a046f6d-24d2-7655-6534-0d7ecb759709`, either byte order) appear during the confirmed process-dead window?
- Three-way outcome as in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA; nothing is promoted without maintainer sign-off (`AGENTS.md` §6).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-058-2026-10-05_21-39-51_21-46-22-Group_AT/CAP-058-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-058-2026-10-05_21-39-51_21-46-22-Group_AT/CAP-058-EVENT-NOTES
