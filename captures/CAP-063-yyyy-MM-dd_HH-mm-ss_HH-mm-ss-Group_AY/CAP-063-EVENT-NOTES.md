# Event Notes: OpenControl for Pixel Buds on Pixel 9a (GrapheneOS) — Group AY, hardware re-test of the `ai-sessions/0048` and `ai-sessions/0052` builds (`CAP-063`)

**Status:** ⚪ **Skeleton — not yet captured.** Written 2026-09-26 (after `ai-sessions/0052`), so that the maintainer can run the whole session from
this file alone. Fill in the empty cells while (or right after) capturing; the analysis session fills in the evidence columns and writes
`CAP-063-FINDINGS.md`. After the capture, rename this folder to the film's own start/end time
(`CAP-063-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AY`, e.g. `CAP-063-2026-09-28_06-40-00_07-25-00-Group_AY`).

**Purpose.** One session on the **Pixel 9a (GrapheneOS)** with **OpenControl** (not the official app) that answers the open hardware questions of
`ai-sessions/0048` §9 (AY-0 … AY-12), `ai-sessions/0051` §19 (double Refresh, re-subscription while docked) and `ai-sessions/0052` (the new settings
reads and writes, the Refresh fix, EQ audibility), plus the `APP_TESTPLAN.md` steps never run so far (A5, B4, C5, F5–F7, H5, J4, K1–K5, L3, and
`0045` (E)/(F)). Nothing in the app since `ai-sessions/0048` is hardware-verified; this session is the first test of it.

**Not in this session:** the ANC-mode-list (field 12) Left/Right and bit-order question (`0051` F-6). It needs the **official Pixel Buds app**, so the
**Pixel 7a**; it is Group AR, planned as `CAP-056` (skeleton folder `captures/CAP-056-…-Group_AR/`), with the additions of §8 below. Run it as a
**separate** capture (own film, own HCI log), preferably the same day, never at the same time as this one.

---

## 0. Which phone, which app, which build

| Item | Value |
|---|---|
| Phone | **Pixel 9a, GrapheneOS** (Android 17; record the build number from Settings → About phone: `__________`) |
| App under test | **OpenControl for Pixel Buds**, debug APK built from this repository |
| Build (P1) | **The commit that completes `ai-sessions/0052`** (the settings UI: tab "Sound" with balance/mono/conversation detection, tab "Controls" with touch controls, press-and-hold, in-ear detection). Commit hash: `__________` (`git log -1 --format=%H`). ⚠️ At the time this skeleton was written that UI was **not yet built** (`0052` RESULT, Progress). With an older build (e.g. `0048`) run only the steps **not** marked **[0052]** and write that down here. |
| Official Pixel Buds app | **Not used** in this session. The Pixel 9a has none; do not open it on any other phone during the session |
| Pixel 7a | **Bluetooth off** for the whole session (the Buds support multipoint: a 7a connection would add a second phone's traffic to the Buds' behaviour) |
| Other Bluetooth devices near the 9a | Watch, car, speaker: Bluetooth off or out of range. If something else connects anyway, write it down (`CAP-004` had Fitbit traffic in its log) |
| Buds | Pixel Buds Pro 2, firmware `release_5.203` (the app shows it; a different version means Safe Mode — stop and report, §2 P10) |

---

## 1. Log Metadata (fill in)

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-063`                     |
|      Group(s)    |                        AY                          |
|       Date       |                                                     |
| Firmware version | (from the Connection tab "Firmware: …")             |
|   Test device    | Pixel 9a, GrapheneOS, build `…`; OpenControl commit `…`; Google Play services present: yes/no; Play services *Nearby devices*: allowed/denied (P4) |
| Video file       | `CAP-063-recording.mp4` — first/last overlay time: … / … |
| Log file         | `CAP-063-btsnoop_hci.log` — raw path used (`FS/data/log/bt/…` or `FS/data/misc/bluetooth/logs/…`) or `btsnooz.py`: … |
| App debug export | `CAP-063-debug-export.log` (Debug mode on from …) |
| App logcat       | `CAP-063-OpenControl-for-Pixel-Buds-log-<id>.txt` |
| System log       | `CAP-063-System-log-<id>.txt` |
| Events file      | `CAP-063-events.txt` (your own time + action notes, P6) |
| Clock offsets    | Film ↔ phone: minute change filmed at start (`__:__`) and end (`__:__`) — measured in the analysis |

---

## 2. Preparation — the day before / before filming

Tick each box. Nothing here is optional unless it says so.

| # | Check | Done |
|---|---|---|
| P1 | Build the debug APK of the commit named in §0 (`cd android && ./gradlew assembleDebug`) and install it (`adb install -r app/build/outputs/apk/debug/app-debug.apk`). Write the commit hash in §0 **and** say it aloud on film at the start | ☐ |
| P2 | Do **not** clear the app's data (the permissions stay granted; A5 below revokes one on purpose, at the end) — unless you want to repeat the first-start flow; then write that down | ☐ |
| P3 | Settings → System → Developer options: **Enable Bluetooth HCI snoop log = Enabled**, **USB debugging = on**. Then **reboot the phone** (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §2 step 5) — the log starts after a Bluetooth restart | ☐ |
| P4 | Settings → Apps → Google Play services → Permissions → *Nearby devices*: write down **allowed / denied** and **leave it as it is** for the session | ☐ |
| P5 | Buds and case charged (> 50 %). Buds in the case, lid closed, at the start | ☐ |
| P6 | Events file ready (a notes app on another device, or paper): for **every** step write the phone time (HH:MM:SS) and what you did — the step IDs below make this short ("AY-5 14:02:11 Right out") | ☐ |
| P7 | **Camera** (on a tripod) films the phone screen **and** the case/buds on the table in one frame, sharp enough to read the screen text; the camera's own burned-in overlay shows date and time. **Switch off any location/street-address overlay on the camera** (an earlier film carried one) | ☐ |
| P8 | Camera microphone on: you will **say aloud** what you hear (EQ, balance, mono, ANC, ring). Quiet room; no music from other sources | ☐ |
| P9 | Phone: **Do Not Disturb on** (earlier films showed private notifications in the shade); only open the notification shade when a step asks for it | ☐ |
| P10 | A **stereo test file** stored **on the phone** (e.g. a left/right channel test and a piece of music you know well), playable offline in any audio player — needed for H, the balance and mono steps | ☐ |
| P11 | GrapheneOS Settings → Security → *Bluetooth auto-off timeout*: write down the current value `______` (K5 uses it; set it to the shortest value only for K5, restore afterwards) | ☐ |
| P12 | Know the Buds' touch gestures: **tap** = play/pause, **press-and-hold** = the action set in the app (Noise control cycles the ANC modes). Know which side you wear which bud (L/R printed inside the bud) | ☐ |
| P13 | Laptop with `adb` and `platform-tools` next to you for §6 (the bug report takes minutes — start it within a minute of the last action) | ☐ |
| P14 | Read this whole file once before starting | ☐ |

**Rhythm for every step** (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4): wait ~5 s → note the time → do **one** action → wait 5–10 s (longer where a step
says so) → next step. If something unexpected happens, **stop, say it aloud, write the time**, then continue with the next step; do not repeat a step
without writing "repeat" in the events file.

**Record the original values (needed to restore the Buds at the end, step Z1):** after step B2, copy what the app shows:

| Setting | Original value (as read at Connect) |
|---|---|
| EQ (five sliders) / preset | |
| Balance | |
| Mono audio | |
| Conversation detection | |
| Use touch controls | |
| Press and hold — Left / Right | |
| In-ear detection (setting) | |
| ANC mode | |

---

## 3. Start of the film

| Step | Action | Expected on screen | Time (phone) | Result / what you saw |
|---|---|---|---|---|
| S0 | Start the camera. Film the phone's status bar across a **minute change** (≥ 10 s before and after). Say aloud: date, "CAP-063, Group AY", the commit hash, Play services *Nearby devices* state | — | | |
| S1 | Quick Settings: **Bluetooth off**. Open OpenControl | "Bluetooth is disabled." + **Enable Bluetooth** (A1) | | |
| S2 | Tap **Enable Bluetooth** | Android's own "turn on Bluetooth" prompt; allow → the app leaves the "disabled" state (A2) | | |
| S3 | Debug tab: **Debug mode on** | "Unidentified frames (n)" appears (L1) | | |

---

## 4. Event Timeline (the test steps)

Legend: **[0052]** = only with the build of §0 P1 (skip with an older build). "Expected on the wire" is for the analysis — you do not need to check it
while filming. HCI bracket abbreviations: `SABM`/`DISC` = RFCOMM channel open/close; DLCI 0x02 = the app's session (MAESTRO), 0x04 = Message Stream.

### A. Connect by itself, what the app reads

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| AY-0 | Buds in the case, lid closed; app on screen | Take both buds out and **put them in your ears**. Do **not** tap Connect | "Connected to this phone (Android)", then **by itself** "App control: connecting…" → "ready" (ADR-044) | app `SABM` 0x02 → Buds `GetSoftwareInfo` → `ReadSetting 4:16`, then **[0052]** `ReadSetting 4:2, 4:4, 4:7, 4:17, 4:19, 4:22` → `SubscribeRuntimeInfo` → DLCI 0x04 snapshot claim (`08 11` → `08 13 … e8 …`) | | |
| B1 | ready | Connection tab: read it aloud | "Firmware: release_5.203"; **no** Safe Mode card; Left/Right % with "(updated HH:MM:SS)" (D1) | — | | |
| B2 | ready | **[0052]** Open tab **Sound**, then tab **Controls**; read every value aloud and fill in the "original values" table (§2) | Each setting shows a value with "read HH:MM:SS", or "Not read from the Buds yet"; Controls shows "In-ear detection (setting): on/off — read-only; not whether a bud is worn". The EQ presets are **two rows** (3 + 2) and no label is cut off | the six `ReadSetting` answers above | | |
| B3 | ready | Tab tour with the **bottom bar**, then by **swiping** (C10); count the tabs aloud | Tabs **Connection, ANC, Sound, Controls, Find, Debug** (with [0052]); Back does not jump to Debug | — | | |

### B. Connect / Disconnect edge cases

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| C5 | ready | **Disconnect**, wait 5 s, then tap **Connect twice quickly** | One session, no error (C3 then C5) | exactly **one** app `SABM` 0x02 | | |
| AY-9 | ready | Tap **Disconnect**. Take **one** bud out of your ear and put it back; press Home and return to the app | Stays "App control: not open yet" — **no** automatic re-open after a Disconnect tap | **no** app `SABM` 0x02 until the next Connect tap | | |
| C4 | not open (after AY-9) | Tap **Connect** | ready | app `SABM` 0x02 | | |

### C. ANC while worn / not worn

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| AY-3a | both buds worn, ready | Take the **Left** bud out and lay it on the table (Right stays in your ear). Wait 10 s. ANC tab → **Refresh** | Mode buttons **enabled** or "ANC can only be changed while you wear the Buds." — say which | `08 11` → `08 13 00 04 01 e8 <settable> <mode>` — the test of "Settable `0x00` = not worn" (ADR-024 Update, 🟡) | | |
| AY-3b | Left on the table, Right worn | Swap: Left **in**, Right **out**. Wait 10 s. **Refresh** | as AY-3a — say which | as AY-3a | | |
| AY-3c | one worn | Right back **in** (both worn). **Refresh** | buttons enabled | Settable `e8` | | |
| F6 | both worn | Tap **ADAPTIVE** and immediately **OFF** | The last one (OFF) wins; no error or hang; say what you hear | two `08 12`, in order, each answered | | |
| F7 | both worn | Tap **ACTIVE**, press Home **immediately**; come back after 5 s | ANC ACTIVE applied (audible), no stuck state | `08 12 … 08` → ACK; later `DISC` 0x04 (release) | | |
| F5 | both worn, app on the ANC tab | **Press and hold** the Right bud once (with press-and-hold = Noise control it switches the ANC mode — you hear it); then tap **Refresh** | After Refresh the screen shows the bud's new mode | `08 11` → `08 13 … <new mode>` | | |
| AY-4 | both worn | Take both buds out, lay them on the table. ANC tab: tap a mode. Then pull down Quick Settings and tap the **ANC tile** once | Buttons disabled + "ANC can only be changed while you wear the Buds."; tile subtitle "Only while worn" + the same message | **no** `08 12` and no DLCI 0x04 `SABM` for the tap | | |

### D. Settings writes [0052] (buds **in your ears**, music or the test file playing, say aloud what you hear)

Put both buds back in your ears first (step D0: time `______`). The app may re-open by itself after the Buds close the channel — wait for "ready".

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / what you hear |
|---|---|---|---|---|---|---|
| D1 | stereo test file playing | Tab **Sound** → drag **Balance** fully to **L** and release | "Left 100 · changed HH:MM:SS"; sound only (or mostly) in the **left** ear | one `WriteSetting 4:{17:200}` (+100 = Left, zigzag) → empty `RESPONSE` | | |
| D2 | | Drag Balance fully to **R**, release | "Right 100 · changed …"; sound in the **right** ear | `4:{17:199}` → `RESPONSE` | | |
| D3 | | Drag Balance to about **halfway left**, release; say the number shown | "Left NN"; left louder, right still audible | `4:{17:<2·NN>}` | | |
| D4 | | Drag Balance back to the **centre** (the number shown should read "Centre"), release | "Centre" | `4:{17:0}` (or a small value) | | |
| D5 | | **Mono audio on** (the left/right test: both ears now play both channels) | switch on, "changed …" | `4:{19:1}` → `RESPONSE` | | |
| D6 | | **Mono audio off** | switch off; the channels separate again | `4:{19:0}` | | |
| D7 | ANC ACTIVE, music playing | **Conversation detection**: tap it **off**, wait 10 s, tap it **on** (from whatever the original value was, end with it on); then **speak** a sentence for ~5 s | Switch follows each tap with "changed …"; while speaking with it on: the Buds switch to transparency / pause (say what happens) | `4:{22:0}` / `4:{22:1}` → `RESPONSE` each | | |
| D8 | music playing | Tab **Controls** → **Use touch controls off**; then **tap** a bud once | switch off; the tap does **not** pause the music | `4:{4:0}` → `RESPONSE`; no DLCI traffic for the tap | | |
| D9 | | **Use touch controls on**; tap a bud once | switch on; the tap pauses/plays | `4:{4:1}` | | |
| D10 | | **Press and hold — Left: Digital assistant**; then press-and-hold the **Left** bud | value "Digital assistant · changed …"; say what the phone does (GrapheneOS may have no assistant) | `4:{7:{1:{4:{1:6}}}}` → `RESPONSE` | | |
| D11 | | **Press and hold — Left: Noise control**; press-and-hold the Left bud | ANC mode changes (audible) | `4:{7:{1:{4:{1:5}}}}` | | |
| D12 | | Same for **Right**: Digital assistant, hold Right, then Noise control, hold Right | as D10/D11 for Right | `7{2:…6}`, `7{2:…5}` | | |
| D13 | | **Disconnect**, **Connect**; open Sound and Controls | Every value just written is **read back** ("read HH:MM:SS") — balance, mono, conversation detection, touch controls, both hold actions | the six `ReadSetting` answers = the last writes | | |

### E. Equalizer audibility (buds in your ears, the music you know playing)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / what you hear |
|---|---|---|---|---|---|---|
| H2 | ready, Sound tab | Tap **HEAVY BASS**, wait 10 s; **LIGHT BASS**; **BALANCED**; **VOCAL BOOST**; **CLARITY** — say after each what changed | sliders move to the preset | one `WriteSetting 4:{16:…}` per tap → `RESPONSE` | | |
| H3 | | Drag **Upper treble** to +6, release; then to −6 | slider stays; audible? | one write per release | | |
| H4 | | Drag **Low bass** to +6, then −6 | audible? | as H3 | | |
| H5 | | Tap **Read EQ again** (only visible after an error; if it is not there, write "not shown") | the values just written | `ReadSetting 4:16` | | |

### F. Refresh battery

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| AY-13a | ready, buds worn, **no** action for ≥ 10 s | Connection tab: **Refresh battery** once | Left/Right "(updated HH:MM:SS)" = now; under the button "The Buds report the Case level only while a bud is in the case." **[0052]** | app `SABM` 0x04 → `03 03 00 03 …` burst → `08 11`/`08 13`; **[0052]** one `SubscribeRuntimeInfo` on DLCI 0x02 | | |
| AY-13b | right after AY-13a | Tap **Refresh battery twice within 1 s** | Both times new "(updated …)" times — or "No new battery reading from the Buds — try again." — never an old time presented as new | **[0052]** two `SABM`s on DLCI 0x04 (the lingering claim released in between: `DISC` then `SABM`), each followed by `03 03` | | |

### G. Buds in the case (lid open)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| AY-7 | both worn, ready, app on the Connection tab | Take the **Right** bud out of your ear and put it **in the case** (lid open); wait 20 s; then the **Left** | Right "— charging in the case (HH:MM:SS)", then both; Case "NN% (updated …)"; the session may close and re-open by itself | stream packets like `CAP-062` 3760/4845; Buds `DISC` 0x02 → app `SABM` ≈ 1.5 s later | | |
| AY-6 | both in the case, lid **open** | Wait. Say when Android's Bluetooth panel / the app shows the Buds disconnected and reconnected | a re-open, then (ACL drop) "Android no longer shows the Buds connected …" or "The Buds closed the app's channel (…)"; when Android reconnects on its own: ready again by itself, both "charging in the case", Case current | Buds `DISC` 0x02; ACL `0x13`; Android Create Connection; **one** app `SABM` 0x02 per event; no 2nd loss-triggered re-open within 10 s | | |
| AY-14 | both docked, lid open, ready | Do **nothing for 2 minutes** (watch the Case line time). Then tap **Refresh battery** | The Case time does not change by itself while nothing changes; after Refresh: the Case updates (if the Buds answer the re-subscription) or stays with its old time — both are valid; say which | **[0052]** app `REQUEST SubscribeRuntimeInfo` → a Buds `SERVER_STREAM` within 1 s, or none (ADR-043 Update, 🔴 untested) | | |
| AY-1 | both docked, lid open, ready | Find tab: **Ring Left**; listen 10 s; **Stop** | Does a **docked** bud ring? Say it; the notice follows the ACK | `04 01 00 01 02` → ACK `ff 01 00 03 04 01 00` or NAK `ff 02 …`; then `04 01 00 01 00` | | |
| AY-2 | same | Sound tab: tap **BALANCED** | accepted or an error shown (say which) | `WriteSetting` → `RESPONSE` OK or `status ≠ OK` | | |
| AY-2b | same | **[0052]** Sound tab: **Mono audio** on, then off | accepted or error | `4:{19:1}`, `4:{19:0}` | | |
| C8 | same | **Close the lid**; wait 20 s | "Paired — not connected to this phone"; the loss text never says "likely another app" | ACL disconnect; no app `SABM` while the link is down | | |

### H. Taking buds out, re-open rules

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| C9 | lid closed, app on screen | Open the lid, take **both** buds out (lay them on the table); do **not** tap Connect | ready again **by itself** once Android shows the Buds connected; Case "NN% — last seen HH:MM:SS (no bud charging in the case)" (E4) | one app `SABM` 0x02 per event; a stream packet without entry 6.1 | | |
| AY-5 | both buds **worn** (put them in first), ready, app on screen | Take **one** bud out of your ear | "The Buds closed the app's channel (…)", then ready again by itself | Buds `DISC` 0x02 → app `SABM` 0x02 ≈ 1.5 s later (one) | | |
| AY-10 | ready | Press **Home**; take a bud out (the Buds close the session); wait **30 s**; return to the app | nothing while in the background; on return: re-opens **once** | **no** app `SABM` while not visible; one after resume | | |
| J4 | ready | Press Home, wait **2 minutes**, reopen | still connected, or a clear message and a re-open; no crash | — | | |
| AY-8 | ready | Quick Settings → Bluetooth → tap the Buds' row to **disconnect in Android** | "Android no longer shows the Buds connected …"; **no** automatic re-open until Android reconnects | phone `DISC` HFP → ACL `0x13`; no app `SABM` while the link is down | | |
| AY-8b | after AY-8 | Tap the Buds' row again (Android reconnects); keep the app on screen | ready again by itself (link back) | Android Create Connection → one app `SABM` 0x02 | | |

### I. Find My Buds (buds on the table, out of the case)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| AY-11 | ready | **Ring Left**; **Disconnect**; wait 10 s; **Connect**; **Stop** | After Disconnect: "A ring was started on the Left earbud — reconnect and tap Stop to end it."; after Connect: "… before the app reconnected — it may still be ringing. Tap Stop to end it."; after Stop: gone, the ring stops (say when) | `04 01 00 01 02` → ACK; later `04 01 00 01 00` → ACK | | |

### J. Robustness and the remaining APP_TESTPLAN steps

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| K4 | ready | Rotate the phone; switch dark mode on and off (Quick Settings) | nothing lost, no crash | — | | |
| K1 | ready | Quick Settings: **Bluetooth off** | "Bluetooth is disabled." + Enable Bluetooth; no crash | — | | |
| K2 | after K1 | Bluetooth on; **Connect** if it does not connect by itself | ready again | app `SABM` 0x02 | | |
| K3 | ready | Walk with the phone to another room (out of range) for 30 s, come back | a clear "lost" message, then ready again (by itself or after Connect) | ACL loss; re-open | | |
| L2 | — | Debug tab: **Export debug log** → save to Files (this is X1's first export) | the log opens; connection lines with times; hex lines; no full Buds address | — | | |
| L3 | — | **Debug mode off**, export again (save as a second file) | no hex lines | — | | |
| K5 | — | *(optional, last)* Set the Bluetooth auto-off timeout to the shortest value (P11), lock the phone and wait for it; unlock, open the app; afterwards restore the timeout | "Bluetooth is disabled." (normal), no crash | — | | |

### K. Optional, destructive steps (only at the very end, only if you want to re-pair afterwards)

| Step | Action | Expected on screen | Time | Result / notes |
|---|---|---|---|---|
| A5 | Settings → Apps → OpenControl → Permissions → *Nearby devices*: **Don't allow**; open the app | "Bluetooth permission needed" / "You denied the permission." + **Allow** (after "don't ask again": **Open app settings**); allow again afterwards | | |
| (E) / B1 | Forget the Buds in Android's Bluetooth settings; open the app | "No Pixel Buds Pro 2 paired yet." + **Pair a device** | | |
| B4 | Open the lid; tap **Pair a device twice quickly** | no second picker, no crash; then pair normally (B2/B3) | | |

### Z. Restore

| Step | Action | Time | Done |
|---|---|---|---|
| Z1 | **[0052]** Set every setting back to the original value of §2 (balance, mono, conversation detection, touch controls, both hold actions, EQ) | | ☐ |
| Z2 | Stop the film after filming the status bar across a **minute change** again | | ☐ |

---

## 5. What you must not do

- Do not use the official Pixel Buds app, or the Pixel 7a's Bluetooth, during this session.
- Do not tap Connect in steps that say "do not tap" (AY-0, AY-7, AY-6, C9, AY-5, AY-8b): those test the automatic re-open.
- Do not do two actions in one step; do not skip the 5–10 s pauses.
- Do not open the notification shade unless a step asks (privacy; DND is on).
- Do not change field 12 (the ANC-mode checklist) anywhere — it is not in this session (Group AR, `CAP-056`, Pixel 7a).

---

## 6. After the run — within 1 minute of the last action

| # | Collect | Done |
|---|---|---|
| X1 | The two debug exports (L2 with Debug mode on, L3 without) — if L2/L3 were done earlier, export once more now with Debug mode on → `CAP-063-debug-export.log` | ☐ |
| X2 | `adb bugreport cap063` on the laptop; unzip; take the raw snoop log from `FS/data/log/bt/btsnoop_hci.log` or `FS/data/misc/bluetooth/logs/btsnoop_hci.log` (else `btsnooz.py`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3) → `CAP-063-btsnoop_hci.log`; write which path was used in §1 | ☐ |
| X3 | App logcat and the system log (GrapheneOS log viewer) → `CAP-063-OpenControl-for-Pixel-Buds-log-<id>.txt`, `CAP-063-System-log-<id>.txt` | ☐ |
| X4 | Film → `CAP-063-recording.mp4`; write its first and last overlay time in §1 | ☐ |
| X5 | Put everything with this file and your events file in this folder; rename the folder (see the top); `sha256sum *` into the analysis notes | ☐ |
| X6 | *(optional)* Switch the HCI snoop log off again; Pixel 7a Bluetooth may be switched on again | ☐ |

---

## 7. Analysis checklist (for the analysis session)

- [ ] Integrity pre-flight (`capinfos`, `cap_len≠len`, out-of-order, `sha256sum`) as in `CAP-062-EVENT-NOTES.md`.
- [ ] Pre-filter by the Buds' classic ACL handle (and LE handle) — `bluetooth.addr` is empty with the `H4 with linux header` encapsulation
      (`AGENTS.md` §13.1; `CAP-062` used `bthci_acl.chandle==0x000b`). Map the handle to `04:00:6e:cf:6e:07` via HCI Connection Complete.
- [ ] Film ↔ phone clock offset from the two filmed minute changes; every "Time" above in phone time.
- [ ] DLCI 0x02: `python3 scripts/pwrpc_decode.py CAP-063-btsnoop_hci.log` — every `ReadSetting`/`WriteSetting`/`SubscribeRuntimeInfo` with its answer;
      compare the app's writes byte for byte with the `SettingsFixtures.kt` frames (channel-dependent header).
- [ ] DLCI 0x04: per-open reassembly (`opens.py` of `ai-sessions/0051` §4): a `03 03` burst on every open; two opens for AY-13b.
- [ ] Refuted if (`0048` §9 + `0052`): any app `SABM` 0x02 while the app is not visible or after a Disconnect tap before a Connect tap; more than one
      app `SABM` 0x02 per event; an `08 12` while the last `Notify` read Settable `00`; a Case value shown without its time; a loss text saying
      "another app"; a settings value changed on screen without the Buds' `RESPONSE`; any `WriteSetting`/`ReadSetting` naming field 12; any app
      frame on DLCI 0x08; a second `SubscribeRuntimeInfo` per Refresh.
- [ ] Traceability (`AGENTS.md` §13.7): every step ID above has a timeline row or an explicit "not run"; `APP_TESTPLAN.md` A5, B4, C5, F5–F7, H5, J4,
      K1–K5, L3 and `0045` (E)/(F); `0048` §9 AY-0 … AY-12; `0051` §19 AY-13/AY-14.
- [ ] Registry Test-IDs to reference: `PAIR-003`, `CASE-004`–`CASE-006`, `BATT-004`, `ANC-001`–`ANC-004`, `FIND-001`/`FIND-002`, `EQS-001`, `EQP-*`,
      `CONV-001`, `TOUCH-001`, `HOLD-001`–`HOLD-004`, `AUDIO-001`–`AUDIO-003` (balance/mono — check the IDs in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`).
- [ ] Write `CAP-063-FINDINGS.md`; add the Capture Index row and Group AY to `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (maintainer's approval), update
      `id_registry.csv`'s `CAP-063` row from *planned* to *analyzed*.

## 8. For the separate Pixel 7a capture (Group AR, `CAP-056`) — additions from `ai-sessions/0051` F-6

Run it on the **Pixel 7a with the official Pixel Buds app** (Pixel 9a Bluetooth **off**), following Group AR in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` and
its anti-repeat safeguard (the checklist screen visible on film before the first toggle), with these steps added:

1. On **"Customize left"**'s ANC-mode checklist: untick **only Adaptive**; wait ≥ 10 s.
2. Open **"Customize right"** on film: is Adaptive unticked there too? (one shared list, 🟡)
3. Re-tick Adaptive (≥ 10 s), then untick **only Transparency** (≥ 10 s), then re-tick it.
4. One `WriteSetting 4:{12:{…}}` per tap is expected; which bit clears for Adaptive vs Transparency settles the `qht` order (code: 3 = Transparency,
   4 = Adaptive; on-screen reading: the reverse) — `PROTOCOL.md` §4.5.3 2026-09-26 Update.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-063-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AY/CAP-063-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-063-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AY/CAP-063-EVENT-NOTES
