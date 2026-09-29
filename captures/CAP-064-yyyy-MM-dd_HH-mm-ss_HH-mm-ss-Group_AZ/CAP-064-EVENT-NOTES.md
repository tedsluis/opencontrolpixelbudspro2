# Event Notes: OpenControl for Pixel Buds on Pixel 9a (GrapheneOS) — Group AZ, hardware re-test of the `ai-sessions/0054` build (`CAP-064`)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0054`, 2026-09-28). After the run: fill in every blank, rename this folder from the
placeholder `CAP-064-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AZ` to the film's first/last overlay times, and analyse it as `CAP-063` was (`ai-sessions/0053`).

**Purpose:** the hardware re-test of the five improvements built in `ai-sessions/0054` from `CAP-063`'s findings — **I-1** a tap on a disabled ANC mode
checks again with a normal claim; **I-2** a session loss that happened while the app was in the background is worded from Android's link when you return;
**I-3** the balance snaps to "Centre" within ±3; **I-4** right after Connect the previous connection's per-bud lines are marked "last seen" / "last
connection"; **I-5** the note under "Press and hold" — plus **AY-3** (skipped in `CAP-063`): one bud **visibly** in an ear, the other **visibly** on the table,
the test of "Settable `0x00` = no bud worn" (`DECISIONS.md` ADR-024 Update, 🟡) — and the `APP_TESTPLAN.md` steps `CAP-063` skipped (F7, K1–K5, L3, A5, B4,
(E)). **Added by `ai-sessions/0056`:** the re-test of that session's build — the "Modes for press and hold (both buds)" list (`qhr` field 12, ADR-046) and the
"In-ear detection" switch (field 2, ADR-047) in OpenControl, with a long-press cycle on film and the in-ear-detection-off checks (section VII, AZ-1 … AZ-6). **Added by `ai-sessions/0057`** (the maintainer's choice
"CAP-064 section VIII", 2026-09-29): the Material 3 overhaul of that session — top-bar Debug and back, the (i) details, pull to refresh / reconnect on every tab,
and the one new use of a known request: the settings re-read on a pull (section VIII, AZ-9 … AZ-14).

**Not in this session (the maintainer's choice, chat 2026-09-28):** the "Digital assistant via GSND" check (`CAP-063-FINDINGS.md` §7) was not chosen. The official
app's W-12b steps were run in `CAP-056` (Group AR, Pixel 7a); since `ai-sessions/0056` OpenControl can write field 2 itself, so section VII repeats the key checks
**with OpenControl** on this Pixel 9a. The "open Customize right while a mode is unticked on the left" check needs the official app and is **not** part of
`CAP-064` (see the note after section VII).

---

## A.0. Which phone, which app, which build

| Item | Value |
|---|---|
| Phone | **Pixel 9a, GrapheneOS** (Settings → About phone → build number: `__________`) |
| App under test | **OpenControl for Pixel Buds**, debug APK of the commit that completes `ai-sessions/0057` (it contains the `0054` and `0056` builds). Commit hash: `__________` (`git log -1 --format=%H`) — **also say it aloud on film** (P1) |
| Official Pixel Buds app | **Not used.** Pixel 7a: **Bluetooth off** for the whole session (multipoint would add a second phone's traffic) |
| Other Bluetooth devices | Watch, car, speaker: off or out of range; write down anything that connects anyway (`CAP-063`: a "Charge 6" speaker was connected) |
| Buds | Pixel Buds Pro 2, firmware `release_5.203` (the app shows it; anything else is Safe Mode — stop and report) |

## A.1. Log Metadata (fill in)

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-064`                     |
|      Group(s)    |                        AZ                          |
|       Date       |                                                     |
| Firmware version | (from the Connection tab "Firmware: …")             |
|   Test device    | Pixel 9a, GrapheneOS, build `…`; OpenControl commit `…`; Google Play services present: yes/no; Play services *Nearby devices*: allowed/denied (P4) |
| Video file       | `CAP-064-recording.mp4` — first/last overlay time: … / … |
| Log file         | `CAP-064-btsnoop_hci.log` — raw path used (`FS/data/log/bt/…` or `FS/data/misc/bluetooth/logs/…`) or `btsnooz.py`: … |
| App debug export | `CAP-064-debug-export.log` (saved with Android's "save as" dialog — the whole log, not cut at 64 KiB; Debug mode on from …) |
| App logcat       | `CAP-064-OpenControl-for-Pixel-Buds-log-<id>.txt` |
| System log       | `CAP-064-System-log-<id>.txt` |
| Events file      | `CAP-064-events.txt` (your own time + action notes, P6) |
| Clock offsets    | Film ↔ phone: minute change filmed at start (`__:__`) **and** end (`__:__`) — measured in the analysis |

## A.2. Preparation — before filming

Tick each box. The lessons of `CAP-063` are marked **(CAP-063)**.

| # | Check | Done |
|---|---|---|
| P1 | Build the debug APK of the commit in A.0 (`cd android && ./gradlew assembleDebug`), install it (`adb install -r app/build/outputs/apk/debug/app-debug.apk`). Write the hash in A.0 **and say it aloud on film at the start** **(CAP-063: the hash was not recorded)** | ☐ |
| P2 | Do **not** clear the app's data (A5/B4/(E) at the end revoke or re-pair on purpose) | ☐ |
| P3 | Developer options: **Bluetooth HCI snoop log = Enabled**, **USB debugging = on**; then switch Bluetooth **off and on on film** (the log starts after a toggle) | ☐ |
| P4 | Settings → Apps → Google Play services → Permissions → *Nearby devices*: write **allowed / denied**, **say it aloud on film**, leave it as it is **(CAP-063: not recorded)** | ☐ |
| P5 | Buds and case charged (> 50 %); buds in the case, lid closed, at the start | ☐ |
| P6 | **Events file** ready (notes app on another device, or paper): the phone time (HH:MM:SS) and the step ID for every action, e.g. "I-1b 14:02:11 tap ADAPTIVE" **(CAP-063: none was provided)** | ☐ |
| P7 | **Camera** on a tripod films the phone screen **and** the case/buds **and your head** in one frame, sharp enough to read the screen. For the wear steps (I-1, AY-3) **your ears must be visible on film** — turn your head to the camera when you put a bud in or take it out **(CAP-063: "worn" could never be confirmed; every wear state was "off film")**. No location/address overlay on the camera | ☐ |
| P8 | Camera microphone on; **say aloud** what you hear (ANC switching, balance). The phone's own audio is not on the film **(CAP-063: the audio track was empty)** | ☐ |
| P9 | **Do Not Disturb on**; open the notification shade only when a step asks **(CAP-063: WhatsApp/LinkedIn names and your e-mail address were readable in the shade)** | ☐ |
| P10 | A stereo test file on the phone (left/right channel test) and music you know, playable offline | ☐ |
| P11 | GrapheneOS Settings → Security → *Bluetooth auto-off timeout*: write the current value `______` (K5) | ☐ |
| P12 | Know the gestures: tap = play/pause, press-and-hold = Noise control (the setting of `CAP-063`'s end) | ☐ |
| P13 | Laptop with `adb` next to you for A.6 (start the bug report within a minute of the last action) | ☐ |
| P14 | Read this whole file once before starting | ☐ |

**Rhythm for every step:** wait ~5 s → note the time → **one** action → wait 5–10 s (longer where a step says so) → next step. Something unexpected:
**stop, say it aloud, write the time**, then continue. A repeated step is written "repeat" in the events file.

## A.3. Start of the film

| Step | Action | Expected on screen | Time (phone) | Result |
|---|---|---|---|---|
| S0 | Film the phone's status bar across a **minute change** (≥ 10 s before and after). Say aloud: date, "CAP-064, Group AZ", the commit hash, Play services *Nearby devices* state | — | | |
| S1 | Quick Settings: Bluetooth **off**, then **on** (starts the HCI log). Open OpenControl | the app connects by itself once Android shows the Buds connected (after I-0) | | |
| S2 | Debug tab: **Debug mode on** | "Unidentified frames (n)" appears | | |

## A.4. Steps

"Expected on the wire" is for the analysis. DLCI 0x02 = the app's session (MAESTRO), 0x04 = the Message Stream (claimed on demand).

### I. ANC re-check on a disabled tap (I-1) and one bud worn (AY-3)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| I-0 | buds in the case, lid closed | Open the lid, take both buds out and **lay them on the table** (visible). Wait for "ready" | ready by itself (ADR-044); ANC tab: "ANC can only be changed while you wear the Buds (checked HH:MM:SS). Tapping a mode checks again first." — the mode buttons are **enabled** | app `SABM` 0x02 …; snapshot claim `08 11` → `08 13 00 04 01 e8 00 20` (Settable `00`) | | |
| I-1a | buds on the table, ANC tab | Tap **ADAPTIVE** (buds still on the table) | no mode change; the "(checked HH:MM:SS)" time moves to now; no error line | **one** app `SABM` 0x04 → `08 11 00 00` → `08 13 … e8 00 …` → **no** `08 12`; `DISC` 0x04 ≈ 1.5 s later | | |
| I-1b | same | Put **both** buds in your ears **on film** (turn to the camera); wait 10 s (the Buds may close the session and it re-opens by itself — wait for "ready"). ANC tab: tap **ADAPTIVE** | "ANC mode: ADAPTIVE (updated HH:MM:SS)"; the buttons stay enabled; you hear Adaptive | **one** claim: `SABM` 0x04 → `08 11` → `08 13 … e8 e8 …` → `08 12 00 14 01 e8 e8 40 …` → ACK `ff 01 00 06 08 12 01 e8 e8 40` — `Get` and `Set` **in the same claim** (unless the re-open's snapshot already read `e8`: then the `Set` comes without a `Get`, as before — say which) | | |
| I-1c | buds worn | Take both buds out, lay them on the table **on film**; wait 10 s; pull down Quick Settings, tap the **ANC tile** once | tile subtitle "Only while worn"; a toast "ANC can only be changed while you wear the Buds." | one claim: `08 11` → `08 13 … 00 …` → no `08 12` | | |
| I-1d | buds on the table | Put the buds in your ears **on film**, then tap the **ANC tile** once (do not open the app) | the mode switches (the one after the mode shown); no toast | one claim: `08 11` → `08 13 … e8 …` → `08 12 …` → ACK | | |
| AY-3a | both worn | Take the **Left** bud out **on film** and lay it on the table **in view**; the Right stays **visibly** in your ear. Wait 10 s. ANC tab → **Refresh** | say which: the "can only be changed…" line appears, or not | `08 11` → `08 13 00 04 01 e8 <Settable> <mode>` — **the test of "Settable `0x00` = no bud worn"** (ADR-024 Update, 🟡): `e8` expected if one worn bud is enough | | |
| AY-3b | Left on the table, Right worn | Swap **on film**: Left **in**, Right **out** onto the table. Wait 10 s. **Refresh** | as AY-3a — say which | as AY-3a | | |
| AY-3c | one worn | Right back **in** (both worn, on film). **Refresh** | no "can only be changed…" line | Settable `e8` | | |

### II. Loss wording after returning to the app (I-2)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| I-2a | ready, app on the Connection tab, lid **open**, buds out | Press **Home** (app in the background). Put **both** buds into the case (lid stays open) so the Buds drop the link. Wait **10 s**. Return to the app | "Paired — not connected …" and **"Android no longer showed the Buds connected when you returned to the app — the connection ended while the app was in the background (for example both buds in the case, or out of range). Tap Connect to reconnect."** | ACL `Disconnection Complete` `0x13` while the app is not visible; no app `SABM` 0x02 while it is away; the debug export: "Session loss cause: the loss happened while the app was not visible; on return Android's link was down" | | |
| I-2b | (optional) ready | Home; take **one** bud out of the case and put it back (the Buds close the app's channel, the link stays); wait 10 s; return | the session re-opens by itself right away; if the text shows for a moment: "The app's channel was closed while the app was in the background; Android showed the Buds connected when you returned. …" — say what you saw | Buds `DISC` 0x02 while away; one app `SABM` 0x02 after the return | | |

### III. Balance "Centre" (I-3)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| I-3a | ready, buds worn, stereo test file playing, Sound tab | Drag Balance to about **Left 20**, release | "Left NN · changed …" | `WriteSetting 4:{17:<2·NN>}` → empty `RESPONSE` | | |
| I-3b | | Drag the knob back **near the middle** (within a few steps of it), release | the knob jumps to the middle; **"Centre · changed HH:MM:SS"**; both ears equally loud | `WriteSetting 4:{17:0}` (= `CAP-046` 1873's payload on this channel) → empty `RESPONSE` | | |
| I-3c | | Drag to about **Right 4** (just outside the snap), release | "Right 4" (not snapped) | `4:{17:7}` | | |
| I-3d | | Back near the middle, release | "Centre" | `4:{17:0}` | | |

### IV. Stale per-bud lines at Connect (I-4)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| I-4a | ready, both buds **in the case** (lid open), Connection tab shows both "charging in the case (HH:MM:SS)" | Tap **Disconnect**. Take **both** buds out onto the table (on film). Tap **Connect**. **Film the battery lines for the first 3 s** | first: "Left: 100% — last seen HH:MM:SS — charging in the case (HH:MM:SS, last connection)" (the times of the previous connection); then, within ≈ 2 s: "Left: NN% (updated …) — not charging (out of the case) (…)" | app `SABM` 0x02; stream packet without 6.1; claim with `03 03 00 03 …` | | |
| I-4b | ready, both out | **Disconnect**; put both buds **in the case**; **Connect**; film the lines | the old "not charging" lines marked "last connection" until the stream says "charging in the case"; Case "last seen" until 6.1 arrives | stream packet with 6.1 | | |

### V. Digital-assistant note (I-5)

| Step | Pre-state | Action | Expected on screen | Time | Result |
|---|---|---|---|---|---|
| I-5 | ready | Controls tab: read aloud the line under "Press and hold" | "Digital assistant needs an assistant app on this phone that supports headphones (for example the Google app). Without one, holding the bud may only play a tone." | | |

### VI. Steps `CAP-063` skipped (`APP_TESTPLAN.md`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| F7 | buds worn, ANC tab | Tap **ACTIVE**, press Home **immediately**; come back after 5 s | ACTIVE applied (audible), no stuck state | `08 12 … 08` → ACK; later `DISC` 0x04 | | |
| K4 | ready | Rotate the phone; dark mode on and off | nothing lost, no crash | — | | |
| K1 | ready | Quick Settings: **Bluetooth off** | "Bluetooth is disabled." + Enable Bluetooth; no crash | — | | |
| K2 | after K1 | Bluetooth on; Connect if it does not connect by itself | ready | app `SABM` 0x02 | | |
| K3 | ready | Walk out of range (another room) for 30 s, come back | a clear loss text, then ready again | ACL loss; re-open | | |
| L2 | — | Debug: **Export debug log** → save as `CAP-064-debug-export.log` | "Debug log saved (N lines)." | — | | |
| L3 | — | **Debug mode off**, export again (a second file) | no hex lines | — | | |
| K5 | — | *(optional, last before K-destructive)* auto-off timeout to the shortest (P11), lock, wait, unlock, open the app; restore the timeout | "Bluetooth is disabled." (normal), no crash | — | | |
| A5 | — | *(destructive, end)* Settings → Apps → OpenControl → Permissions → *Nearby devices*: **Don't allow**; open the app | "Bluetooth permission needed" / "You denied the permission." + Allow; allow again afterwards | — | | |
| (E) | — | *(destructive, end)* Forget the Buds in Android's Bluetooth settings; open the app | "No Pixel Buds Pro 2 paired yet." + Pair a device | — | | |
| B4 | after (E) | Open the lid; tap **Pair a device twice quickly** | no second picker, no crash; then pair normally | — | | |

### VII. Press-and-hold ANC-mode list and in-ear detection in OpenControl (`ai-sessions/0056`, ADR-046/047)

Pre-state for the section: ready, both buds worn **on film**, music playing, Controls tab; Left **and** Right press and hold = **Noise control** (set it with the
chips first if needed — otherwise the list is hidden, by design). Long presses: hold a bud ~2 s **facing the camera**, then wait 5 s before the next one.

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| AZ-1 | right after Connect | Open Controls; read the list aloud | "Modes for press and hold (both buds)" with Noise cancellation / Off / Adaptive / Transparency, the Buds' ticks, "read HH:MM:SS" | in the Connect burst `ReadSetting 4:12` (between `4:7` and `4:17`) → `RESPONSE 4:{12:{…}}` | | |
| AZ-2 | all four ticked | Untick **Adaptive** | Adaptive unticked, "changed HH:MM:SS" (only after the Buds' OK) | **one** `WriteSetting 4:{12:{1:1 2:1 3:1 4:0}}` (on channel 19 byte-identical to `CAP-056` 1815) → empty `RESPONSE` OK | | |
| AZ-3 | Adaptive unticked, ANC tab open | **Three long presses** on one bud, on film; after **each** press say what you hear and tap **Refresh** on the ANC tab | the ANC mode line follows each press | one claim per Refresh: `08 11` → `08 13 … <mode>`; **expected modes only `08`, `20`, `80`** (NC, Off, Transparency), **never `40`** (Adaptive) | | |
| AZ-4 | Adaptive unticked | Untick **Off**, then try to untick **Noise cancellation** | after Off: two ticked, both greyed, "At least two modes must stay selected."; NC cannot be unticked | one write `4:{12:{1:1 2:0 3:1 4:0}}` → OK; **nothing** for the NC attempt | | |
| AZ-5 | — | Tick **Off** and **Adaptive** again (restore all four) | all four ticked, "changed …" | two writes → OK each; the last `4:{12:{1:1 2:1 3:1 4:1}}` (ch 19 = `CAP-056` 1725) | | |
| AZ-6a | in-ear detection **on** (the switch reads on) | Tap **In-ear detection** → off | switch off after the OK, "changed HH:MM:SS"; the note "With it off, audio does not pause …" is always visible | `WriteSetting 4:{2:0}` → OK; the Buds' SASS `07 11 00 04 01 02 b0 00` on DLCI 0x04 **if** that channel is open (a claim) | | |
| AZ-6b | off, music playing | Take the **Left** bud out **on film** (ear visible), wait 10 s, put it back | music keeps playing (say so) | **no** AVRCP `PlaybackStatusChanged` / PAUSE; possibly a Buds `DISC` of DLCI 0x02 and the app's re-open (ADR-044) | | |
| AZ-6c | off | Take **both** buds out, lay them on the table **on film**; wait 10 s; ANC tab → **Refresh** | say which: the "ANC can only be changed while you wear the Buds…" line appears or not | `08 11` → `08 13 01 e8 <Settable> …` — **settles 🔴 "Settable with in-ear detection off and no bud worn"** (`CAP-056-FINDINGS.md` §4) | | |
| AZ-6d | off, buds on the table | Put both buds back in **on film**; Controls → tap **In-ear detection** → on | switch on after the OK | `WriteSetting 4:{2:1}` → OK; SASS `… b8 00` if DLCI 0x04 is open | | |
| AZ-7 | — | Disconnect, Connect, open Controls | the list and the switch read back as last written ("read …") | `ReadSetting 4:12` / `4:2` answers = the last writes | | |
| AZ-8 | ready | Set **both** buds' press and hold to **Digital assistant**; then Left back to **Noise control** | the list disappears while neither bud is Noise control, and reappears | the two/one `4:{7:…}` writes → OK; no field-12 write | | |

**Refuted if (section VII):** a list or switch value changes on screen before the Buds' `RESPONSE` OK; more than one `WriteSetting` per tap, or any `CLIENT_ERROR` from
the app; a field-12 write with fewer than two `1`s, or one that is not all four booleans; the NC attempt of AZ-4 puts anything on the wire; a long press in AZ-3
reaches Adaptive (then the Buds do not follow the list — ADR-046 would need a new look); in AZ-6b the phone pauses with the switch off.

**Not `CAP-064` — a later Pixel 7a capture with the official app (outside this Pixel 9a/GrapheneOS scope):** open "Customize left", untick one mode (e.g.
Adaptive), go back, open **"Customize right"** on film — does it show Adaptive unticked (one shared list, the 🟡 of `PROTOCOL.md` §4.5.3) or ticked (two lists — then
ADR-046 is superseded)? Record it as its own capture ID when it is run.

### VIII. Material 3 overhaul, pull to refresh and the settings re-read (`ai-sessions/0057`)

Pre-state: ready, both buds worn, music playing. Wait ≥ 5 s between pulls and **say each pull aloud** with the tab name. The detailed screen checks are
`APP_TESTPLAN.md` section O; this section is what the **HCI log** must show.

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Time | Result / notes |
|---|---|---|---|---|---|---|
| AZ-9 | Controls tab | **Pull down** once | spinner, then the (i) "read HH:MM:SS" times move | DLCI 0x02 exactly **seven** `ReadSetting` requests `4:2, 4:4, 4:7, 4:12, 4:17, 4:19, 4:22` in that order, each answered before the next (≤ 2 s), **nothing else** from the app (no write, no `SubscribeRuntimeInfo`, no DLCI 0x04 claim) | | |
| AZ-10 | Sound tab | **Pull down** once | spinner; EQ and settings times in the (i) move | `ReadSetting 4:16` first, then the same seven reads, in order, nothing else | | |
| AZ-11 | Connection tab, then Find tab | **Pull down** on each | new battery times or "No new battery reading…" | per pull: one DLCI 0x04 claim (`SABM` → `03 03 …` → `08 11`/`08 13`) released ≈ 1.5 s later, and one `SubscribeRuntimeInfo` on DLCI 0x02 — as a *Refresh battery* tap (E8) | | |
| AZ-12 | ANC tab | **Pull down** | the (i) "updated" time moves | one DLCI 0x04 claim with `08 11` → `08 13`, as the Refresh button | | |
| AZ-13 | — | **Disconnect**, then pull on Controls | the app connects | `SABM` DLCI 0x02, then the normal Connect sequence (EQ read, the seven reads, subscription, the snapshot claim) — once | | |
| AZ-14 | ready | Bug icon → Debug → system back; tap a few (i)s | Debug full screen, back to the tab; dialogs open/close | **nothing** on any channel | | |

**Refuted if (section VIII):** a pull sends anything other than its tab's action; a read is repeated in the same pass (a retry) or out of order; a pull
sends anything while the app is still connecting; opening Debug or an (i) puts anything on the wire.

### Z. Restore

| Step | Action | Time | Done |
|---|---|---|---|
| Z1 | Balance back to the start value; in-ear detection and the ANC-mode list back to their start values (VII); ANC as you like it | | ☐ |
| Z2 | Film the status bar across a **minute change** again, then stop the film | | ☐ |

## A.5. What you must not do

- Do not use the official Pixel Buds app, or the Pixel 7a's Bluetooth, during this session.
- Do not tap Connect where a step does not ask for it (I-0, I-1b, I-2a/b): those test the automatic behaviour.
- Do not do two actions in one step; do not skip the pauses.
- Do not open the notification shade unless a step asks (DND is on).
- Change in-ear detection and the ANC-mode list only in section VII, and leave both as they were at the start (Z1).

## A.6. After the run — within 1 minute of the last action

| # | Collect | Done |
|---|---|---|
| X1 | The debug exports (L2 with Debug mode on, L3 without) — if L2 was done earlier, export once more now → `CAP-064-debug-export.log` | ☐ |
| X2 | `adb bugreport cap064`; the raw snoop log from `FS/data/log/bt/btsnoop_hci.log` or `FS/data/misc/bluetooth/logs/btsnoop_hci.log` (else `btsnooz.py`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3) → `CAP-064-btsnoop_hci.log`; write the path in A.1 | ☐ |
| X3 | App logcat and system log (GrapheneOS log viewer) | ☐ |
| X4 | Film → `CAP-064-recording.mp4`; its first/last overlay time in A.1 | ☐ |
| X5 | Everything, with this file and the events file, into this folder; rename the folder; `sha256sum *` | ☐ |

## A.7. Analysis checklist

- [ ] Integrity pre-flight (`capinfos`, `cap_len≠len`, out-of-order, `sha256sum`); audio track present or not.
- [ ] Pre-filter by the Buds' classic ACL handle (map it to `04:00:6e:cf:6e:07` via HCI Connection Complete) — `bluetooth.addr` is empty with this
      encapsulation (`AGENTS.md` §13.1).
- [ ] Film ↔ phone offset from the two filmed minute changes; logcat/system log UTC offset.
- [ ] DLCI 0x04 per claim: for I-1a/c exactly `08 11` → `08 13 … 00 …` and **no** `08 12`; for I-1b/d `08 11` → `08 13 … e8 …` → `08 12` → ACK in **one** claim.
- [ ] AY-3: the Settable byte of each `Notify` against the film (which bud is visibly in an ear) — a counter-example either way is the result.
- [ ] I-2: the export's loss and cause lines against the film (app in the background) and the HCI `Disconnection Complete`.
- [ ] I-3: `python3 scripts/pwrpc_decode.py CAP-064-btsnoop_hci.log | grep WriteSetting` — `4:{17:0}` after I-3b/d, answered OK.
- [ ] I-4: film frames of the first 3 s after each Connect against the stream/battery frame times.
- [ ] VII: `pwrpc_decode.py … | grep -E '4:\{?(12|2)[:\}]'` — each write one per tap, answered OK; AZ-3 `Notify` modes; AZ-6c Settable byte; AZ-6b no pause.
- [ ] Traceability (`AGENTS.md` §13.7): every step above has a timeline row or an explicit "skipped"; registry Test-IDs to reference: `ANC-001`–`ANC-004`,
      `INEAR-001`–`INEAR-004`, `HOLD-005`, `HOLD-001`–`HOLD-004` (AZ-8), `CASE-004`–`CASE-006`, `BATT-004`, `AUDIO-003`, `PAIR-003`.

**Refuted if** (the `ai-sessions/0054` build, plus the standing `0048`/`0052` criteria):
- an `08 12` in a claim whose `Notify` read Settable `00` (I-1), or a `Set` on a disabled tap **before** that claim's `08 13`;
- more than one DLCI 0x04 claim for one disabled tap, or a claim without a user tap (no automatic ANC re-check exists);
- after I-2a the screen says "The Maestro channel (equalizer) was closed" (undetermined) although the first link reading on return was "not connected", or
  it claims "The Buds closed the app's channel" for a loss read on return;
- a release within ±3 of the centre written as anything but `4:{17:0}`, or "Centre" shown for a value ≠ 0;
- right after Connect a previous connection's per-bud value shown without "last seen" / "last connection", or a new report still marked so;
- any app `SABM` 0x02 while the app is not visible or after a Disconnect tap before a Connect tap; more than one per event;
- any app frame on DLCI 0x08; a field-12 or field-2 request outside section VII's taps and the Connect reads; a settings value changed on screen without the Buds'
  `RESPONSE` (plus section VII's own criteria).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-064-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AZ/CAP-064-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-064-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AZ/CAP-064-EVENT-NOTES
