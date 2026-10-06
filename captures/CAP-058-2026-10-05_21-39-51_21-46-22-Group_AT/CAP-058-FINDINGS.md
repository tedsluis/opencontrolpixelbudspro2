# CAP-058: `SDP-001`, 3rd attempt with a process-liveness check (Group AT), and fourteen extra tests

Evidence: `CAP-058-recording.mp4` (391.01 s), `CAP-058-btsnoop_hci.log` (from the filmed Bluetooth-on), `CAP-058-btsnoop_hci.log.last` (18:02:45 →
21:40:04: `CAP-054`'s log, the rest of its third connection, and two Forget + Fast Pair pairings shortly before the film),
`CAP-058-adb-shell-dumpsys-activity-processes.txt` (six `dumpsys` runs, laptop clock). Timeline: `CAP-058-EVENT-NOTES.md`. Analysed in
`ai-sessions/0072` (2026-10-05). Labels per `PROJECT_RULES.md` §1: 🟢 FACT, 🟡 HYPOTHESIS, ⚪ ASSUMPTION, 🔴 OPEN QUESTION.

## 0. Capture metadata

| Field | Value |
|---|---|
| Purpose | Group AT: does the "default internal rfcomm socket" UUID `3a046f6d-24d2-7655-6534-0d7ecb759709` (`gbm.java`/`fzd.java`, ADR-018) appear in the SDP response when the pairing happens with the companion app **not running**? |
| Date | 2026-10-05, film 21:39:51.3–21:46:22.3 phone time |
| Phone / app | Pixel 7a, Android 17; official app 1.0.955078536 |
| Firmware | `release_5.203` (2834, 4295); "Up to date" on film |
| Log path | raw `btsnoop_hci.log` + `.log.last` |
| Clock | phone = film + 21:39:51.3 ± 0.13 s; laptop (`dumpsys` `date`) ↔ phone **not measured** |

## 1. The process state at the SDP browses (the procedure question)

**What is on record** (`CAP-058-adb-shell-dumpsys-activity-processes.txt`, each run `date ; adb shell dumpsys activity processes | grep -i
com.google.android.apps.wearables.maestro.companion`, laptop clock):

| Run (laptop) | Result | Detail |
|---|---|---|
| 21:40:22 | present, **PID 6219** — the positive control for the filter | `MaestroCompanionDeviceService` bound by `android`; `Proc # 24: prcp … (service)`; process age ≈ 3 m 39 s (its SettingsProvider connection "+3m38s582ms") |
| 21:40:35 | **no output — absent** | — |
| 21:40:51 | present, **new PID 7893** | `MaestroSliceProvider` used by `system/1000` ("+5s860ms") and Play services (`2796`, "+5s899ms"); `MaestroDeviceSettingsProviderService` bound by `com.android.settings`; `MaestroCompanionDeviceService` by `android`; state `fg … (provider)`; age ≈ 9 s ("+8s974ms") |
| 21:41:00, 21:41:21, 21:42:14 | present, PID 7893 | `prcp` (service), then `cch-empty` |

🟢 The force stop (film 21:40:32.1–32.4) killed the process, and **a new one ran ≈ 7–10 s later** (≈ 21:40:42 laptop clock) — **before** the Forget
(21:40:57.841, frame 1422) and before both filmed SDP browses (21:41:19.342 and 21:42:07.957). On the wire the app itself shows it ran: it searched SDP for
its "pigweed" UUID and opened MAESTRO after each pairing (2813 → `SABM` 2823 at 21:41:23.288; 4274 → 4284 at 21:42:09.951) — the app's own channel
(ADR-018).

**What started it** — 🔴 not on record: the revised procedure's `logcat -b events` file (`am_proc_start` carries the start *type* and *component*,
AOSP `EventLogTags.logtags` line 25: `30014 am_proc_start (User|1|5),(PID|1|5),(UID|1|5),(Process Name|3),(Type|3),(Component|3)`) was not made. 🟡
HYPOTHESIS: opening **Connected devices / Device details** in Settings started it — the film shows Recents → Connected devices at 21:40:39.3–43.3 and
Device details at 21:40:45–47 (phone clock), and the 21:40:51 snapshot lists exactly Settings' bind of `MaestroDeviceSettingsProviderService` and the
system's and Play services' use of `MaestroSliceProvider`, which date the start at ≈ 21:40:42 (laptop clock). External check (AOSP `main`, fetched
2026-10-05 with `curl …?format=TEXT | base64 -d`): `ApplicationInfo.FLAG_STOPPED`'s comment says *"Applications should avoid launching activities,
binding to or starting services, or otherwise causing a stopped application to run unless initiated by the user. … An app can also return to the stopped
state by a "force stop"."* — so the stopped state does not block a bind; and `ActiveServices.bindServiceLocked` calls `bringUpServiceLocked` for a
`BIND_AUTO_CREATE` bind, which starts the process (`startProcessLocked(… HostingRecord.HOSTING_TYPE_SERVICE …)`), reading `isStopped()` only to log it
(`ActiveServices.java` ≈ lines 4313–4361 and 5726–5910 of that revision). 🟡 for this phone's build (the device may differ from `main`).

**The revised Group AT's rule, applied literally:** outcome **(a)** — "no `am_proc_start` between the force stop and the SDP browse" — is **not met**:
the process was present again 16 s after the force stop. Outcome **(b)** — "a restart with its cause" — is met **in part**: the restart is on record
(two PIDs), its cause is not. 🟢 What can be concluded: through this path (force stop in Settings, then Settings' device pages, Forget, pairing) the
companion app does **not** stay dead; "app never running" was not reached in this run. With the `CAP-054` observation (the app ran again after its
force stop and opened MAESTRO at 18:06:22) and the context observation of the prompt (`am_proc_start … bound-service …
MaestroCompanionDeviceService` at 21:31:03, PID 5056 — the chat that wrote the prompt, not a file of this capture), 🟡 HYPOTHESIS (strengthened):
"app never running at the SDP browse" is not reachable through the Settings pairing flow, because Settings, CompanionDeviceManager and Play services
bind or query the app's components.

## 2. The `SDP-001` question: `3a046f6d-…` never appears (🟢, four browses)

**Search, both byte orders, every frame** (`tshark -r <log> -Y "frame contains <16 bytes, colon-separated>" | wc -l`; exit 0 each):

| Log | pigweed `25e97ff7…f7b5` fwd | reversed | default `3a046f6d…9709` fwd | reversed |
|---|---|---|---|---|
| `CAP-033-btsnoop_hci.log` (control from an earlier attempt) | 1 | 1 | 0 | 0 |
| `CAP-053-btsnoop_hci.log` / `.log.last` | 2 / 2 | 0 / 0 | 0 / 0 | 0 / 0 |
| `CAP-054-btsnoop_hci.log` | 7 | 0 | 0 | 0 |
| `CAP-058-btsnoop_hci.log` | 7 | 1 | 0 | 0 |
| `CAP-058-btsnoop_hci.log.last` | 12 | 2 | 0 | 0 |

The pigweed UUID is the positive control in every file (in SDP responses, in the phone's targeted search requests, and reversed in the Buds' Extended
Inquiry Result 3724 and last 9967/11742).

**The four full browses** (L2CAP-wide `Service Search Attribute Request`, both logs): 21:36:30.274 (last 10403–10479, pairing A), 21:37:08.293 (last
11998–12021, pairing B), **21:41:19.342** (2249–2272, filmed Fast Pair pairing), **21:42:07.957** (3818–3840, filmed Settings pairing). Each response
names the same eight records — *Audio Sink, Android Gamepad, BTIS, GSND CONTROL, GSND AUDIO, GFPS RFCOMM, DEBUG APP, MAESTRO APP* (`btsdp.service_name`),
RFCOMM channels 6, 9, 4, 5, 2, 3, 1 — and the same six custom 128-bit UUIDs (`btsdp.data_element.value.custom_uuid` of 2272):
`25e97ff7-24ce-4c4c-8951-f764a708f7b5` (MAESTRO APP), `25e97ff7-24ce-4c4c-8951-f764a708f7b4`, `81c2e72a-0591-443e-a1ff-05f988593351`,
`df21fe2c-2515-4fdb-8886-f12c4d67927c`, `e7ab2241-ca64-4a69-ac02-05f5c6fe2d62`, `f8d1fbe4-7966-4334-8024-ff96c9330e15` (GSND CONTROL). The phone's targeted
searches after each pairing ask for GSND AUDIO, GSND CONTROL, GFPS RFCOMM and the pigweed UUID — **never** for `3a046f6d-…` (0 frames in either
direction).

**What this run adds to the five earlier negatives** (`CAP-033`, `CAP-044` and the logs `REVERSE_ENGINEERING.md` lists): four more full browses with
the same record set; for the first time the process state at the browse is **measured** — alive (§1) — so the run does not test the
"app-never-running" branch; and the phone's own search requests show the app only ever looks for the pigweed UUID. 🟡 HYPOTHESIS (stronger): this
firmware does not publish `3a046f6d-…` at all; the "default internal rfcomm socket" branch of `gbm.java` is for another product or firmware.

## 3. The pairings (`PAIR-001`; ADR-008/025: described, not reverse-engineered)

| Pairing | LE (Fast Pair) | Classic SSP | Link key | Outcome |
|---|---|---|---|---|
| A, 21:36:28 (before the film) | key-based pairing `0x0c04`, passkey `0x0c07`, account key `0x0c0a` | Buds IO DisplayYesNo, phone confirms | type **0x05** (authenticated P-256) | SDP, RFCOMM 0x0c/0x0a/0x08/0x04, no MAESTRO; local disconnect, Forget at 21:36:42.545 |
| B, 21:37:06 (before the film) | as A | as A | 0x05 | SDP, MAESTRO opened 21:37:10.627; ended 0x13 21:37:25.348 |
| 1, 21:41:17.6 (film: half-sheet *Connect*) | KBP 2156–2170, account key 2334 | DisplayYesNo 2204, confirmation 2223 → reply **2232** (no dialog on film) | 0x05 (2235) | SDP, MAESTRO 2823; "Ready to use — connected to someone else's account"; ACL ended 0x13 21:41:25.421 |
| (failed), 21:41:38.4 (film: *Connect* again) | KBP 3161–3173, LE dropped 3211 | three attempts, each `User Confirmation Request Negative Reply` from the phone (3341, 3410, 3478) | — | "Couldn't connect" 21:41:45.6 |
| 2, 21:42:05.6 (film: Settings → Pair new device → *Pair*) | none before; KBP + account key `0x0c0a` on LE 0x000f afterwards (4627–4650, 21:42:30–32) | Buds IO **NoInputNoOutput** (3789), confirmation 3797 → reply **3801 (21:42:07.643)** = the filmed *Pair* tap (21:42:07.1–07.6) | type **0x04** (unauthenticated P-256) (3804) | SDP, MAESTRO 4284, connected to the end |

🟢 The Buds answer with a different IO capability in the two classic paths: DisplayYesNo after a Fast Pair key-based pairing, NoInputNoOutput for a
Settings pairing — so Android's link key is authenticated (0x05) after Fast Pair and unauthenticated (0x04) after the Settings pairing. `PROTOCOL.md`
§5.1's FACT (the fresh-pairing sequence) holds for all four; the IO capability split is new (🟢 one run each; 🟡 as a rule). In pairing mode the Buds'
**public** address advertises Fast Pair *discoverable* data `00 37 da2db1 18 02` (from 2081, 85 reports): version `00`, model ID `da 2d b1`, capability
map `02` (the provider page's `0bRRRRRRSO`: S = 1, LE Audio sharing supported; O = 0).

## 4. The official app after the downgrade

- 🟢 Connect sweep after pairing 2: early requests 21:42:10.0–10.6 (Dosimeter on 19/21, `GetHardwareInfo`, `SubscribeToSettingsChanges`,
  `SubscribeRuntimeInfo`, `SetWallclock`, `SetConfig 1:1 2:0 3:0`, `Multipoint.SubscribeToQuietModeStatus`), then the `ReadSetting` sweep of the same field
  set as `CAP-053` at **21:42:26.480–28.422** (4447–4542), 16 s later, with nothing filmed at that moment. No unknown service.
- 🟢 **Find device on film, but only while not connected:** Settings' Device details (whose rows the app supplies through its settings provider) showed
  *More controls: **Find device · "See location info or ring device"***, Controls and gestures, Sound, Hearing wellness, Audio switch at film 58 s (the Buds
  bonded, not connected, process 7893 just started); after the pairing, connected, the list is Digital assistant, Controls and gestures, Sound, Hearing
  wellness, More settings, Audio switch — **no Find device** (frames at 182 s and 366 s). 🟡 HYPOTHESIS: this app version offers Find device only
  while the Buds are not connected — a possible reason why the maintainer saw none (with the Buds connected); `TODO.md` §4's "removed by something outside
  the APK version" stays open. Test: open Device details once connected and once disconnected, on film, with each app version.
- 🟢 The app's own screens appeared only through Fast Pair: "Set up device" → **Set up** → "Allow a connection to your Pixel Buds" → CompanionDeviceManager's
  "Allow the app Pixel Buds to access Pixel Buds Pro 2 van Ted?" (twice, 21:41:35 and 21:42:39). Which button was tapped is **not identifiable** (the
  fingertip lies between *Allow* and *Don't allow*); after the second the app's rows appeared in Device details.

## 5. Other protocols

- **Message Stream:** per claim nonce, Model ID, BLE address `5f:c6:22:32:97:a6` (5457), "Revision 6", battery `e4 e4 ff` (both charging, in the case),
  `08 13 … e8 00 20` (Off, Settable `00`), SASS. **GSND CONTROL / AUDIO:** opened at each connection; closed and reopened by the phone with Media audio (X2).
  **HFP:** Buds-opened on 0x09 on ACL 0x0005, phone-opened 0x0c on 0x000d. **A2DP:** AVDTP SetConfiguration AAC (3904, 5167), no stream start.
- **Links:** LE 0x000e (21:42:10.045, 0x3e "connection failed to be established") and 0x000f (21:42:30.218–36.206) to the Buds' LE address; an LE
  `Connection Complete` with status 0x02 at 21:42:29.785 (4553).

## 6. The extra tests (the maintainer's statement: "extra tests at the end of the film")

| # | Action (film) | Wire | Finding |
|---|---|---|---|
| X1 | Phone calls off/on | HFP DLCI 0x0c `DISC` 4877 / `SABM` 4971 | 🟢 Android's per-device "Phone calls" switch closes and reopens HFP; nothing on MAESTRO |
| X2 | Media audio off/on | AVDTP Close 5091 + A2DP/AVCTP L2CAP closed + **DLCI 0x08/0x0a `DISC`** 5110/5111; reopen 5155–5175 + `SABM` 0x08/0x0a 5207/5232 | 🟢 the "Media audio" switch also closes and reopens **GSND CONTROL and GSND AUDIO** (0.6 s after AVDTP) — 🟡 those channels belong to the media/assistant path (cf. the Bisto lead, `CAP-061`) |
| X3 | Input device off/on | HID-Control/Interrupt L2CAP closed 5347/5350, reopened 5362/5373 | 🟢 the "Input device" switch is the HID profile (the head-tracker HID, `ARCHITECTURE.md` §1) |
| X4 | Audio switch page | nothing | — |
| X5 | Use audio switch off/on | SASS `07 11 00 14 01 01 00 00 …` (off) / `… 01 01 80 00 …` (on), each with `07 42`, a Message Stream `DISC` and a re-claim | 🟢 the phone's *Notify capability of Audio switch* carries flags `0x8000` with the switch on and `0x0000` with it off (SASS page: *"Bit 0 (octet 6, MSB): Audio switch state 1, if Audio switch state is on"*; the page says a Seeker's flags "should be ignored"); version `0x0101`; the Buds' own `07 11 … b8 00` is unchanged |
| X6, X7, X10, X13 | More settings, Case sounds, back, Device details | nothing | 🟢 screen opens send nothing (as `CAP-036`, `CAP-053`) |
| X8 | Bud return off/on | `WriteSetting 4:{28:0}` 5623, `4:{28:1}` 5643, OK | 🟢 as `CAP-024` (`PROTOCOL.md` §4.5.8) — a second filmed sample |
| X9 | Other alerts off/on | `4:{27:0}` 5680, `4:{27:1}` 5697, OK | 🟢 the label "Other alerts" writes field 27 (film + wire) — the 🟡 label of §4.5.8 gets a second filmed sample |
| X11 | Hearing wellness opened | `Dosimeter.FetchDailySummaries` on channels 19 and 21, 5736 (21:45:14.872), as the page opened (21:45:15.0–15.5) | 🟢 the Hearing wellness page reads the Dosimeter (`WELL-001`; display out of scope) |
| X12 | Volume level notifications: off+on quickly, off, on | `4:{21:0}` 5747, `4:{21:1}` 5753, `4:{21:0}` 5767, `4:{21:1}` 5790, each OK and mirrored | 🟢 **first wire sample of `qhr` field 21**: the switch "Volume level notifications" (Hearing wellness) writes field 21, 1 = on (connect read `4:{21:1}`); `REVERSE_ENGINEERING.md`'s `qhr` register had field 21 as an unnamed `HearingWellnessFragment` toggle (`hey.java:165–190`). Search of every capture for this write (`frame contains 1d:9a:8c:9e:2a:05:22:03:a8:01`): only these 4 frames |
| X14 | About: serial numbers | nothing (the values come from `GetHardwareInfo` at connect, 4301 → 4328) | — |

## 7. Proposals (awaiting the maintainer)

1. **`SDP-001` / ADR-018 / `REVERSE_ENGINEERING.md` `gbm`/`fzd`** — dated notes: a 3rd attempt with a measured process state; the process was alive at
   both browses (outcome (a) not met); `3a046f6d-…` absent from four more browses and never searched for by the phone; status of `SDP-001` unchanged
   (🟡); the "app never running" branch is 🟡 unreachable through Settings pairing.
2. **Group AT, a 4th attempt (procedure only, no skeleton now):** pair **without** Settings' device pages and without Fast Pair: disable the companion app
   (`adb shell pm disable-user` — reversible) or test with the app uninstalled, keep `logcat -b events` running, `pidof` with its positive control before
   the pairing, pair from Settings → Pair new device. If the maintainer prefers to stop: close `SDP-001` as "not reachable in practice; the UUID was never
   seen in the five earlier captures nor in these four browses". `TODO.md` §3.
3. **`PROTOCOL.md` §4.5** — a new subsection or a register row: `qhr` field 21 = "Volume level notifications" (Hearing wellness), 0 = off, 1 = on —
   🟢 FACT proposal on one filmed run (four writes, both directions); nothing is built.
4. **`PROTOCOL.md` §4.5.8** — field 27's "Other alerts" label: a second filmed sample (X9); promotion of the label to 🟢 is the maintainer's choice.
5. **`PROTOCOL.md` §5.1** — note: Buds IO capability DisplayYesNo after Fast Pair (key type 0x05) vs NoInputNoOutput in a Settings pairing (0x04).
6. **SASS (X5)** — `PROTOCOL.md` §6: the phone's `07 11` flags follow the Audio switch setting.
7. **Find device (§4)** — `TODO.md` §4's Ring-status line: Find device is shown by 1.0.955078536 when the Buds are not connected (🟡).

## 8. Consequences for OpenControl

None to build. OpenControl opens only MAESTRO (pigweed UUID) and the Message Stream; the absence of `3a046f6d-…` changes nothing. The pairing finding
(DisplayYesNo/0x05 via Fast Pair, NoInputNoOutput/0x04 via Settings or CDM) matters only if OpenControl ever checks the link-key strength — it does
not. Field 21 could be offered later (a read is covered by no ADR; a write would need one) — not proposed.

## 9. Open questions

- 🔴 What started PID 7893 (§1) — needs `logcat -b events`.
- 🔴 Whether `3a046f6d-…` is published by any firmware or only used by another product.
- 🔴 Which CDM button was tapped (twice not identifiable).
- 🔴 What "connected to someone else's account" refers to (Fast Pair account linking — out of scope, ADR-008).
- 🔴 Why the official app's `ReadSetting` sweep began 16 s after MAESTRO opened on ACL 0x000d.

## 10. Test-ID traceability

`SDP-001` — exercised (four browses; process alive). `SDP-002` — not applicable (no update). `PAIR-001` — four pairings. `PAIR-004` — Forget on the
wire (1422; earlier 9436, 11052 in `.log.last`). `CASE-008` — pairing mode by the case button on film. `CASE-001`, `CASE-002` — X8, X9. `SWITCH-001` —
X4/X5. `WELL-001` — X11/X12. `FIND-001` — observation only (§4).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-058-2026-10-05_21-39-51_21-46-22-Group_AT/CAP-058-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-058-2026-10-05_21-39-51_21-46-22-Group_AT/CAP-058-FINDINGS
