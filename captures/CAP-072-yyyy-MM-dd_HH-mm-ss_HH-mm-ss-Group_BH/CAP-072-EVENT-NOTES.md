# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, the secondary user without Google Play) — Group BH, the 1.2.0 release APK (`CAP-072`)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0082`, 2026-10-09). Scope chosen by the maintainer in chat 2026-10-09 (the
checkpoint of `ai-sessions/0082`): the **1.2.0 release run** — the three new things to see (the "Changed by the Buds" line in the noise-control (i), the
component serial numbers on the Info tab per `DECISIONS.md` ADR-058, the "probably worn" line on the battery card per ADR-059), the two moved switches
(Case sounds on gear → Settings, Conversation detection on Controls), every write once byte for byte, and — for the third time — the open film items of
`TODO.md` §2 (lead L-1 with the head in view, C12, S12, T11, H5, S6, the case-sound and Volume-EQ observations). **Installed over 1.1.1** (P1 records both
`dumpsys` times). The maintainer films; a later CAPTURE session analyses it and gives the release verdict. After the run: rename this folder from the
placeholder to the film's first/last overlay times, and the film to CAP-072-recording.mp4.

**Purpose.** 1.2.0 adds **one** request per connection (`GetHardwareInfo`, ADR-058) and nothing else on the wire; everything else is derived from frames
the app already received, or a control in a new place sending the same bytes. This run checks on the phone:

- **I — the update and the start.** 1.2.0 over 1.1.1 in user 10 (the second update test); the stored choices read back; the Connect-time traffic now
  **thirteen** MAESTRO requests plus the EQ read and the subscription — the twelve settings reads, `SubscribeRuntimeInfo`, then `GetHardwareInfo` last — each
  equal to its reference; "—" in the first second (S6); the Info tab's three serial lines (ADR-058; the serials are **not** written into these notes or the
  findings unredacted: first 4 + last 2 characters only).
- **II — the worn line and "Changed by the Buds", head in view.** ADR-059's six readings against the ears on film (`INEAR-006`), including the ≈ 28 s case of
  `CAP-064` (buds straight from the case to the table, watched for 60 s); lead L-1 (`INEAR-005`) in the same sequence; a press-and-hold on a bud with the
  app open → the (i) line; a tap in the app, a pull, the tile → no line.
- **III — every write once, byte for byte** (the codec is unchanged; the moved switches send what they sent from their old place): the two case sounds from
  gear → Settings with the sound observation said aloud, conversation detection from Controls, and the rest as in `CAP-071`; the read-back after a reconnect
  and the serial lines again.
- **IV — robustness:** the case (ADR-044), Home for two minutes, Bluetooth off/on.
- **V — the open film items of `TODO.md` §2** (film 2): C12 (a tap during a re-open), S12 (an export across a rotation), T11 (the screen-reader text from
  three `uiautomator` dumps — the Settings tab's two switches are new).

The run is two films in this folder: **film 1** = P0–P9 and sections I–IV (≈ 35 min), **film 2** = section V (≈ 10 min). If time is short, film 2 can be
another day — the build stays the same; say so in the notes. (`CAP-071` had no film 2; `ai-sessions/0082` RESULT §I asks the maintainer whether to split.)

### Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-072` |
| Group(s) | BH (new) |
| Date | TBD |
| Phone | Pixel 9a, GrapheneOS (Android 17 in `CAP-071`) — the secondary user without Google Play (user 10 in `CAP-071`; P0) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.2.0** (versionCode 10200), the release APK built by `scripts/release.sh` and kept as `RELEASING.md` B3 says — on the **Info tab on film**: "App: 1.2.0, build <hash> (<date>)", no "-dirty"; APK and certificate SHA-256 from the script's output |
| Previous version in this user | 1.1.1 (`86a6fb3`), installed 2026-10-08 for `CAP-071` — the update is from it |
| Official Pixel Buds app | Not used (not in this user). Pixel 7a: Bluetooth off |
| Buds | Pixel Buds Pro 2; firmware from the Info tab on film (`release_5.203` expected; any other ⇒ Safe Mode — the serial read still runs (ADR-058 item 2), say so and continue; the writes are blocked, so stop section III) |
| Video files | TBD — film 1 and film 2, **with sound** (P3) |
| Screen recording | TBD — Android's screen recorder in the test user, film 1 and film 2 (P4) |
| Log files | TBD — CAP-072-btsnoop_hci.log and .log.last (Bluetooth is toggled in P2, IV and V), the app's debug exports, the app logcat, the **system log** (P6), the three `uiautomator` dumps (BH-23), the P0/P1 outputs |

### Preparation

What `CAP-071` missed is marked ★ — please check those twice.

| # | Check | Done |
|---|---|---|
| P0 | In the test user: `adb shell am get-current-user`; `adb shell pm list packages --user <id> \| grep -i -E "gms\|vending"; echo "exit=$?"`; positive control: the same with `grep opencontrol` (exit 0). Save the outputs **with the exit statuses** into this folder | ☐ |
| P1 | **Before the update, in 1.1.1** (on film): gear → **Settings → Dark mode On**; **Debug → Debug mode on**; Info shows "App: 1.1.1, build 86a6fb3". Then `adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds \| grep -E "firstInstallTime\|lastUpdateTime\|versionCode"` → save. **Install 1.2.0 over it**: `adb install --user <id> -r dist/1.2.0/opencontrol-pixelbudspro2-1.2.0.apk` — no uninstall, no "clear data". The same `dumpsys` again → save (**both times recorded**: `firstInstallTime` unchanged, `lastUpdateTime` later, `versionCode=10200`) | ☐ |
| P2 | Bluetooth HCI snoop log on (set in the Owner, then switch to the test user); switch Bluetooth **off and on on film** | ☐ |
| P3 | Camera films the phone, the case **and your head with both ears** at every wear step (head on the right of the frame = Left bud); **the camera records sound** — play back 3 s before starting | ☐ |
| P4 | Android's **screen recorder** on (Quick Settings) ★ (`CAP-071` had none — S6, C12 and the (i) texts need it) | ☐ |
| P5 | Status bar on film across a minute change at the start and at the end of each film | ☐ |
| P6 | **System log**: on the computer, before P2, `adb logcat -b all -v threadtime > CAP-072-logcat-all.txt` (stop it with Ctrl-C after the last export). Note: every `adb bugreportz` (the way the HCI logs are pulled) makes dumpstate send signal 3 to every Java process — "Signal Catcher … reacting to signal 3" once per pull is not an app fault (`CAP-071-FINDINGS.md` §0) | ☐ |
| P7 | A short music track ready at **low volume** for the sound observations (BH-12, BH-14, BH-15); **Do Not Disturb on** ★ (an incoming call put a third party's number on `CAP-071`'s film) | ☐ |
| P8 | Buds charged, both in the case, **lid open** ★ (`CAP-071` started with the lid closed: two failed Connects); Android shows them connected before the app is opened (the app then opens its session by itself, ADR-044) | ☐ |
| P9 | **No rehearsal before the film** ★ (`CAP-071` had an off-film Connect) — the app is not opened between P1's install and BH-1 | ☐ |

**Rhythm:** one action, then wait 5–10 s; **say** what you do at each wear change and at each observation (the film has sound). Something unexpected: stop,
wait 10 s, say it, continue. **Export the debug log before any step that ends the process and after every Bluetooth off/on.** The worn line and the
"Changed by the Buds" line follow the Buds' **last `Notify`** — after every wear change in section II, **pull down on the Connection tab** (one `08 11` claim)
so the app asks the Buds again; it never asks by itself. Before committing: check the films for a street-address overlay, Wi-Fi names and other device
names (`CAP-070` privacy note), and **redact the serial numbers** on any screenshot or in any text (first 4 + last 2 characters).

### Steps

DLCIs are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05). Every MAESTRO request starts `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25` on
**channel 21** or `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25` on **channel 19** (Info → "Control channel"). **Reference frames** — the 1.1.1 build's own requests
in `CAP-071` unless named; the new request's references are the official app's (`ai-sessions/0082` RESULT §A.2/§D):

| Request | Channel 21 | Channel 19 |
|---|---|---|
| `ReadSetting 4:16`, then 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29 | `CAP-071` A (the 1.1.1 run, byte-identical to `CAP-070` A271, A296 … A329) | `CAP-070` A966, A979 … A1035 |
| `SubscribeRuntimeInfo` | `CAP-070` A332 `…90821ee66654bfab7e` | `CAP-070` A1038 `…90821ee6602d65a97e` |
| **`GetHardwareInfo` (new, ADR-058; last of the Connect-time requests)** | `CAP-036` 1415 `7e004b0310151dea71de7d5e25e3a5ec28f96761b57e` | `CAP-024` 801 `7e003b0310131dea71de7d5e25e3a5ec28ff1ebbb77e` |
| its answer (`CAP-036` 1423 / `CAP-024` 832): `RESPONSE` with `7:{1:<14 chars> 2:<14 chars> 3:<14 chars>}` | the three strings in the order Case, Right, Left | the same |
| Conversation detection `4:{22:0}` / `{22:1}` (now from Controls) | `CAP-019` 1720 `…2a052203b00100225fc3b77e` / 1808 `…2a052203b00101b46fc4c07e` | `CAP-071`'s 1.1.1 request on 19 if it ran there, else the codec's output (unit tests) |
| Earbuds replaced `4:{28:0}` / `{28:1}` (now from gear → Settings) | `CAP-070` A770 `…2a052203e0010092717fdb7e` / `CAP-058` 5643 `…2a052203e00101044178ac7e` | `CAP-024` 1988 `…2a052203e00100d2b7cefd7e` / `CAP-070` A1992 `…2a052203e001014487c98a7e` |
| Other alerts `4:{27:0}` / `{27:1}` (now from gear → Settings) | `CAP-070` A777 `…2a052203d80100bac507f17e` / `CAP-058` 5697 `…2a052203d801012cf500867e` | `CAP-024` 2053 `…2a052203d80100fa03b6d77e` / `CAP-070` A1995 `…2a052203d801016c33b1a07e` |
| Multipoint `4:{11:0}` / `{11:1}` | `CAP-070` A752 / `CAP-069` 3212 | `CAP-070` A3668 / A3679 |
| Head gestures `4:{29:1}` / `{29:2}` | `CAP-070` A762 / `CAP-069` 2564 | `CAP-070` A3614 / A3685 |
| Volume EQ `4:{15:0}` / `{15:1}` | `CAP-070` A785 / A4800 | `CAP-070` A3072 / A1953 |
| Balance Right 4 `4:{17:7}` | `CAP-064` 6671 | `CAP-070` A3747 |
| EQ preset Balanced `4:{16:[-3.5, 0.5, 1.0, -1.0, 2.5]}` | `CAP-059` 2188 | no captured frame — the codec's output (unit tests) |
| Mode list without Off `4:{12:{1:1 2:0 3:1 4:0}}` | `CAP-041` 2198 | no captured frame — the codec's output (unit tests) |
| ANC `Get` (each claim: a tap, a pull on Connection/ANC/Find, the tile) | `08 11 00 00` | |
| ANC `Notify` (the Buds' answer, and **unprovoked** after a press-and-hold) | `08 13 00 04 01 e8 e8 xx` — `xx` = `08` Active, `40` Adaptive, `80` Transparent, `20` Off; Settable `00` instead of the second `e8` when no bud is worn (`CAP-045` 612/1583/1755/1849) | |
| ANC `Set` Transparent / Off / Active / Adaptive | `CAP-068-btsnoop_hci.log` 1039 `08 12 00 14 01 e8 e8 80` + 16 × `00`; 1090 `…20…`; 1146 `…08…`; 1266 `…40…` | |
| Ring Left / Stop / Right | `CAP-068-btsnoop_hci2.log` 2142 `04 01 00 01 02`, 2416 `04 01 00 01 00`; `CAP-062-btsnoop_hci.log` 9660 `04 01 00 01 01` | |

A request with no row (touch controls, press and hold, in-ear detection, mono, a slider value) is compared with the app's unit-test fixture for the same
request (`android/data/src/test`). **Any difference is the finding.** Nothing may appear on DLCI 0x08/0x0a and no MAESTRO request other than those listed
(the wire discipline of `ai-sessions/0082` §5).

#### I. The update and the start (film 1; `APP_TESTPLAN.md` V1–V4, U1–U3, S6; `PAIR-003`, `BATT-004`, `FW-003`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BH-1 | P0–P9 done, 1.2.0 just installed over 1.1.1; lid open; screen recording on | open the app; within the first second go to **Controls** | **dark** (the 1.1.1 choice kept); no crash; the session opens by itself; for ≈ 1 s the unread switches show "—", then the Buds' values (S6, screen recording); Controls shows **Conversation detection** as its own card after In-ear detection and **no** Case sounds card | DLCI 0x02 open; the announcement; `ReadSetting 4:16`, then twelve reads in the order of the table, `SubscribeRuntimeInfo`, then **one `GetHardwareInfo`** = its reference for the announced channel, answered with `RESPONSE` field 7; no retry; nothing else | `PAIR-003`, `BATT-004`, `FW-003` | a crash; light theme; a read missing or out of order; a second `GetHardwareInfo`; any other request |
| BH-2 | ready | gear → **Settings**: Dark mode shows **On**; the **Case sounds** card (Earbuds replaced, Other alerts with their values) between Dark mode and "Use different Buds"; open its (i); then **Debug** (Debug mode **on**); then **Info**: hold 3 s on the build line, on "Control channel" and on the **Serial numbers** block | the two 1.1.1 choices kept; the (i) ends with "These settings live on the case and are read when the app connects."; "App: 1.2.0, build <hash> (<date>)", no "-dirty"; firmware `release_5.203` ×3; channel 19 or 21 — say it; **"Serial numbers (from the Buds, HH:MM:SS):" then "Case: …", "Right bud: …", "Left bud: …"** and the note "Labelled by position …" | nothing sent | `FW-003` | either choice lost; "-dirty"; "Serial numbers: Not read from the Buds yet" or "not read — …" while the answer is in the log; a label order other than Case, Right bud, Left bud |
| BH-3 | ready, both buds in the case, lid open | **Connection** tab: read the battery card's **Worn** line; open the (i) | **"Both buds in the case"** (both charging flags from the runtime-info stream); the (i) has the "Worn: …" explanation ending with the `CAP-064` sentence | the runtime-info `6.2`/`6.3` with field 2 = 2 for both buds already received | `INEAR-006` | "Probably worn" with both buds in the case |
| BH-4 | ready | gear → Settings → Dark mode **System** (leave it there) | the app follows Android's theme at once | — | — | — |

#### II. The worn line and "Changed by the Buds", head in view (film 1; `APP_TESTPLAN.md` V5–V12, F1–F4, G3, N11; `INEAR-006`, `INEAR-005`, `INEAR-004`, `ANC-001`…`ANC-004`)

Head and both ears in view at every step; say every wear change. After each wear change **pull down on the Connection tab** before reading the Worn
line. Note the Worn line's "(checked HH:MM:SS)" each time — it must move with each pull.

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BH-5 | both buds in the case | take **only the Left** out, into the left ear; wait 10 s; Info → "Control channel" (say it); pull on Connection; read Worn | the session re-opens by itself if the channel moved; **"Probably worn (checked …)"** | the announcement (`10 13` ⇒ 19 expected with only the Left out, `PROTOCOL.md` §2.2a); the thirteen requests again on the new channel incl. one `GetHardwareInfo`; the pull: `08 11` → `Notify` with Settable `e8` | `INEAR-005`, `INEAR-006` | "Not worn" with a bud in an ear |
| BH-6 | Left worn, channel 19 | the **Right** into the right ear; wait 10 s; Info; pull on Connection; read Worn | "Probably worn (checked …)" with a later time | `Notify` Settable `e8` | `INEAR-006` | — |
| BH-7 | both worn on 19 (if the channel is already 21, say so, put both back in the case 10 s, retry BH-5 once) | take the **Left** out of the ear and **hold it in view**; say "Left out"; wait 10 s; Info; pull on Connection | the session closes and re-opens by itself; Info shows the new channel; Worn stays "Probably worn" (the Right is in) | 🟡 lead L-1 (`PROTOCOL.md` §2.2a Updates of 2026-10-01/03/07): Buds `DISC` of MAESTRO with the ACL up, then an announcement `10 15` (21), the thirteen requests | `INEAR-005`, `INEAR-004`, `INEAR-006` | no `DISC`, or `DISC` followed by 19 again |
| BH-8 | Right worn, Left on the table | take the **Right** out, both buds **on the table**, in-ear detection on; wait 10 s; pull on Connection; read Worn; then **ANC tab: tap Transparent** | **"Not worn (checked …)"**; the ANC tap is refused with the 1.0.x not-allowed text, the mode unchanged | `Notify` with Settable **`00`** (`CAP-045` 1849's form); the tap: `08 11` → `Notify` `00` → **no `Set`** | `INEAR-006`, `INEAR-004`, `ANC-004` | "Probably worn" with both buds on the table beyond the first ≈ 30 s (see BH-11), or a `Set` sent |
| BH-9 | both on the table | Controls → **In-ear detection off**; pull on Connection; read Worn; **In-ear detection on** again; pull; read | after the OK: **"Worn: unknown — in-ear detection is off"**; after the second OK: "Not worn (checked …)" again | `4:{2:0}` → OK; `08 11` → `Notify` (Settable `e8` expected with in-ear off, `CAP-064` 10394 — the byte says nothing); `4:{2:1}` → OK; `08 11` → `Notify` `00` | `INEAR-006`, `INEAR-004` | "Probably worn" shown while in-ear detection is off |
| BH-10 | both on the table, in-ear on | both buds **into the ears**; wait 10 s; pull on Connection; **ANC tab: tap Transparent, Adaptive, Off, Active**, 8 s apart; after each, open the ANC (i) | "Probably worn (checked …)"; each mode after its ACK; the (i) says "Noise control: set HH:MM:SS …" and has **no** "Changed by the Buds" line | per tap `08 11` → `Notify` `e8` → the `Set` = the reference → ACK `ff 01` | `ANC-004`, `ANC-003`, `ANC-001`, `ANC-002`, `INEAR-006` | the line shown after an app tap; a `Set` that differs |
| BH-11 | both worn, ANC on Active, the ANC tab on screen with its (i) **closed** | **press and hold the Left bud** (its noise-control cycle); say it; wait 5 s; open the ANC (i); close; press and hold again; (i) again | the mode changes **without a tap**; the (i) shows **"Changed by the Buds at HH:MM:SS (a press-and-hold on a bud, or the Buds' own change)."** with the time of the `Notify`; the second time the line's time moves | an **unprovoked** `08 13 00 04 01 e8 e8 xx` from the Buds — no `08 11` before it; **nothing sent by the app** | `ANC-001`…`ANC-004` | no mode change on screen (the Buds' `Notify` not applied); the line missing; the app sending anything in reaction |
| BH-12 | the line shown | **tap** the shown mode's neighbour in the app; (i); then **pull down on the ANC tab**; (i); then the **ANC tile** once; (i) | after the tap the line is **gone** ("set HH:MM:SS"); after the pull no line ("read HH:MM:SS"); after the tile no line | the tap: `08 11` → `Notify` → `Set` → ACK; the pull: `08 11` → `Notify`; the tile: a claim as the tap | `ANC-001`…`ANC-004` | the line still shown after the app's own tap, pull or tile |
| BH-13 | both worn | both buds **straight from the ears into the case** (lid open), then **straight out onto the table**, say "on the table" and start counting; pull on Connection at ≈ 5, 15, 30, 45 and 60 s; read Worn each time | the `CAP-064` case: **"Probably worn"** may show for the first ≈ 30 s, then **"Not worn"**; or "Both buds in the case" first if the charging flags arrived; say what you see with the count | `Notify` per pull: `e8` then `00` (or `00` at once); the runtime-info `6.2`/`6.3` field 2 = 2 then 0 | `INEAR-006` | "Probably worn" still at 60 s (the 🟡 of ADR-049 item 3 is then weaker — write it down as a result, not a fault of the app) |

#### III. Every write once, buds worn (film 1; `APP_TESTPLAN.md` V13–V17, U10–U14, H2–H6, M2–M6, N2–N12, I1–I3, T3–T8; `CASE-001`, `CASE-002`, `CONV-001`)

Both buds **in your ears** unless a step says otherwise; music at low volume for BH-14/BH-15. Every write: the control moves only after the Buds' OK.

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BH-14 | worn, music | gear → **Settings → Case sounds**: **Earbuds replaced off** → take the **Left** out of the ear into the case and out again → **say whether the case sounded** → **on** → the same → say; **Other alerts** off, then on; open the card's (i) each time | each switch after its OK; the (i) "Earbuds replaced: changed HH:MM:SS", "Other alerts: changed …", the case note | `4:{28:0}` / `{28:1}`, `4:{27:0}` / `{27:1}` — each = its reference (the move changed nothing) | `CASE-001`, `CASE-002` | a request that differs; a switch on Controls |
| BH-15 | worn, music | **Controls → Conversation detection** off, then on; with it on **speak for 5 s** (ANC on Active); say what the Buds do | each after its OK; the card's subtitle "Switch from noise cancellation to transparency when you talk" | `4:{22:0}` = `CAP-019` 1720, `4:{22:1}` = 1808 (on 21) | `CONV-001` | a request that differs (field 19 or 22 swapped: the mutation M9 case); the row still on Sound |
| BH-16 | worn, music | **Sound**: preset **Balanced**; drag **Upper treble** up, release; **Read EQ again** (H5) ★; balance **Right 4**, then **Centre** ★; **Mono audio** on/off; **Volume EQ** off/on — say what you hear at each | each after its OK; *Read EQ again* shows the value just written; no conversation-detection row on Sound | Balanced = `CAP-059` 2188 on 21; one `WriteSetting 4:{16:…}` on release; `ReadSetting 4:16` = the write; `17:7`, `17:0`; `19:1`, `19:0`; `15:0`, `15:1` | `EQP-001`, `EQS-001`, `AUDIO-003`, `AUDIO-001`, `AUDIO-002` | a request that differs; a read that differs from its write |
| BH-17 | worn | **Controls**: **Use touch controls** off/on; press and hold **Left: Digital assistant**, then **Noise control**; mode list: **untick Off** (restores the Buds); **Use head gestures** off/on; **Multipoint** off/on | each after its OK | `4:{4:0}`/`{4:1}`; `4:{7:{1:{4:{1:6}}}}`/`{1:5}`; `4:{12:{1:1 2:0 3:1 4:0}}` (= `CAP-041` 2198 on 21); `29:1`/`29:2`; `11:0`/`11:1` — each = its reference | `HOLD-005`, `HEAD-001`, `MULTI-001` | a request that differs; the mode list keeps Off |
| BH-18 | worn | **Disconnect**, then **Connect**; open Sound, Controls, gear → Settings and Info | every value just written read back ("read …"): Balanced, Centre, mono off, conversation on, Volume EQ on, touch on, Left Noise control, mode list without Off, in-ear on, head gestures on, Multipoint on, both case sounds on; Info: the **same three serials** with a new time; the Worn line "—" for a moment, then "Probably worn" | the thirteen requests + `4:16`; each answer = the last write; one `GetHardwareInfo` with the same answer bytes as BH-1's | `PAIR-003`, `FW-003` | a read that differs from the last write; different serials; two `GetHardwareInfo` |
| BH-19 | both buds out of the ears, on the table | Find: **Ring Left**, **Stop**, **Ring Right**, **Stop** | the notice and the ring follow each tap | `04 01 00 01 02` → ACK; `… 00`; `… 01`; `… 00` | `FIND-001`, `FIND-002` | a ring that does not stop |

#### IV. Robustness (film 1; `APP_TESTPLAN.md` C8, C9, J4, K1, K2, R6)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BH-20 | ready | both buds into the case, lid open; wait 10 s; take both out again (do not tap Connect) | the session closes with the 1.0.1 cause text; then ready **by itself**; the Worn line "—" during the gap, then a reading after the re-open's `Notify`; the serials shown again | Buds `DISC` 0x02 (or the ACL drops); the app's `SABM` 0x02 ≈ 1.5 s after the link is back (ADR-044); the thirteen requests | `CASE-004`, `CASE-005`, `PAIR-003` | no automatic re-open while the app is on screen |
| BH-21 | ready | **Home**; wait 2 minutes; return to the app | still ready, or a clear message and a re-open; no crash | — | — | a crash |
| BH-22 | ready | **Export** the debug log; Quick Settings **Bluetooth off**; wait 10 s; **Bluetooth on** | "Bluetooth is disabled.", then ready again by itself; Info's serials "Not read …" while off, read again after | export lines "Bluetooth adapter: ON -> TURNING_OFF", "Session loss cause: Bluetooth was switched off on this phone"; **no serial number and no MAC in the export** (grep it afterwards); after on, the automatic re-open and the thirteen requests | `PAIR-003` | a crash; no re-open; a serial in the export |
| BH-end1 | — | **Export** the debug log; status bar across a minute change; stop film 1 and the screen recording | "Debug log saved (N lines)." | — | — | — |

#### V. The open film items of `TODO.md` §2 (film 2; `APP_TESTPLAN.md` C12, S12, T11)

Start film 2 and the screen recording; keep the system log running (P6). (Lead L-1 / `INEAR-005` is BH-5…BH-7 above.)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BH-23 | ready, both worn, Controls tab | put the **Right** bud into the case; **as soon as** the card shows that the app's channel was closed (≈ 0.5–1.5 s), **tap Multipoint** — before "ready" | "The setting was not changed: The app's channel is being reopened — try again in a moment."; the switch unchanged; after "ready" nothing is sent by itself | Buds `DISC` 0x02 → app `SABM` ≈ 1.5 s later; **no** `WriteSetting` for that tap | `CASE-004` | a write sent later by itself (if the tap comes after "ready", say so — C12 stays open) |
| BH-24 | ready | Debug → **Export debug log**; **rotate the phone while Android's "save as" dialog is open**; save (S12) | "Debug log saved (N lines)."; the file has content | — | — | an empty file, no toast |
| BH-25 | ready | **Export** again; Quick Settings **Bluetooth off**; on the computer `adb shell am force-stop io.github.tedsluis.opencontrolpixelbuds` (say it); open the app; **Controls** → `adb shell uiautomator dump /sdcard/CAP-072-controls.xml`; **Sound** (scroll to the Balance card) → `… /sdcard/CAP-072-sound.xml`; **gear → Settings** → `… /sdcard/CAP-072-settings.xml`; `adb pull` all three into this folder (T11) | "Bluetooth is disabled."; Controls: "—" in place of every switch (now seven, conversation detection included); Sound: the five EQ bands, Volume EQ, balance and mono audio "—"; Settings: the two Case sounds switches "—"; the battery card's Worn line "—" | — | — | in a dump, a node with `text="—"` whose `content-desc` is not `Not read from the Buds yet` |
| BH-26 | — | **Bluetooth on** → the app re-opens the session by itself (or **Connect**); **Export**; stop the system log (P6); status bar across a minute change; stop film 2 and the screen recording | ready; the values back; the serials back | the thirteen requests | `PAIR-003` | — |

### Don'ts

- Do not uninstall 1.1.1 and do not clear the app's data (P1 — the run is an update).
- Do not open the app before the film runs (P9); do not open the Owner user during a film.
- Do not tap two things within 5 s of each other, except in BH-23 (the early tap).
- Do not read the Worn line without a pull first after a wear change — the app asks the Buds only on a claim; a stale "(checked …)" time is not a finding.
- Do not leave the buds in the case when a step says "worn" — say it if a step is done differently.
- Do not write a serial number unredacted anywhere in this folder or the findings (first 4 + last 2 characters; the fixtures use `5707XXXXXXXX51`).

### After the run

Into this folder: both films, both screen recordings, the system log (CAP-072-logcat-all.txt), every debug export (`adb pull` from the test user's
storage), the app logcat if saved, both btsnoop_hci.log files, the three `uiautomator` dumps, the P0/P1 outputs (before and after the update), and
`dist/1.2.0/`'s `.sha256` and the script's printed certificate line. Then `sha256sum *` into a file. Commit on the release branch `release/1.2.0`, after the
build commit (`RELEASING.md` C3) — never on `main` (it takes no direct push).

### Analysis checklist

- [ ] Scope every filter to the Buds' ACL handle (show the Connection Complete for the Buds' address with that handle); DLCIs by content (`AGENTS.md` §13);
      for a negative, the command, its exit status and a positive control (step 8).
- [ ] P0 (exit statuses, positive control), P1: user id; no `com.google.android.gms` / `com.android.vending`; **the update**: `firstInstallTime` unchanged
      and `lastUpdateTime` later, `versionCode=10200`; the Info frame "1.2.0, build <hash>" against `git log` and the B1 hash; APK and certificate SHA-256
      against `dist/1.2.0/` and `RELEASING.md`.
- [ ] **The release APK against 1.1.1's:** `aapt2 dump badging` — permissions identical (no `INTERNET`), `targetSdkVersion:'34'`, `sdkVersion:'34'`,
      `compileSdkVersion='37'`; `aapt2 dump xmltree` — no new `<activity>`, `<service>`, `<receiver>`, `<provider>`.
- [ ] Zero Play-services claims on the Message Stream (`frame contains 03:08:00:02:01:25`): command, exit status, positive control `CAP-066`.
- [ ] I: dark mode and Debug mode kept across the update; per session the **thirteen** requests in order with their answers (`python3 -I
      scripts/pwrpc_decode.py --handle <handle> <log>`) — `GetHardwareInfo` **once** per session, **last**, byte-identical to its reference, its answer's
      field 7 strings compared (redacted) with the Info tab on film in the order Case, Right bud, Left bud; S6 on the screen recording; **no**
      `GetHardwareInfo` outside Connect/re-open (`CAP-071` had none at all — positive control for the filter: the official app's in `CAP-036`).
- [ ] II: per pull the `08 11` → `Notify` and its Settable byte against the Worn line on the screen recording and the ears on film — one row per BH-5…BH-13
      reading (**the `INEAR-006` table**: time, ears, Settable byte, in-ear field 2, charging flags, Worn text); BH-11: the unprovoked `08 13` with no `08 11`
      in the 2 s before it and nothing from the app after it, the (i) line's time = the `Notify`'s; BH-12: the line gone after the app's own claim; BH-13:
      the ≈ 28 s timing (Settable `e8` → `00`) — a **proposal** for ADR-049 item 3 / ADR-059, no status change by the analysing session alone; BH-7: the
      `DISC` direction and the announced channel with the head side on film (L-1, `PROTOCOL.md` §2.2a — a proposal).
- [ ] III: **every request against the reference table** (byte for byte, per channel; the others against the unit-test fixtures) — the two case sounds from
      their new place and conversation detection from Controls first; every `RESPONSE`/ACK; the read-back of BH-18 and its serials; the sound observations
      quoted as said (BH-14, BH-15, BH-16).
- [ ] IV: each session end with its logged cause; the automatic re-opens; Bluetooth off/on; **`grep -c` over every export for each serial's first 4 + last 2
      characters and for the Buds' MAC = 0**, with a positive control (the export's own "Control channel" line).
- [ ] V: BH-23 — no `WriteSetting` after the early tap, the text on the screen recording; BH-24's file; **the three dumps** (count `text="—"` nodes and their
      `content-desc` per tab; Controls now seven, Settings two).
- [ ] The system log: crashes, ANRs, `StrictMode` (none expected in a release build); the `bugreportz` SIGQUITs discounted (P6 note).
- [ ] One row per step — done / not done / done differently; `APP_TESTPLAN.md` section V and the Summary updated; traceability (`AGENTS.md` §13 step 7):
      `PAIR-003` (BH-1, BH-18, BH-20, BH-22, BH-26), `BATT-004` (BH-1), `FW-003` (BH-1, BH-2, BH-18), `INEAR-006` (BH-3, BH-5…BH-10, BH-13), `INEAR-005`
      (BH-5, BH-7), `INEAR-004` (BH-7, BH-8, BH-9), `ANC-001`…`ANC-004` (BH-8, BH-10…BH-12), `CASE-001`, `CASE-002` (BH-14), `CONV-001` (BH-15),
      `EQP-001`, `EQS-001`, `AUDIO-001`…`AUDIO-003` (BH-16), `HOLD-005`, `HEAD-001`, `MULTI-001` (BH-17), `CASE-004` (BH-20, BH-23), `CASE-005` (BH-20),
      `FIND-001`, `FIND-002` (BH-19) — each referenced in the timeline or flagged "expected but not observed".
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `id_registry.csv`, `APP_TESTPLAN.md`, `TODO.md` §2/§4/§5 (the restored Buds, the items done, the fixture
      swap of `HardwareInfoFixtures`), ADR-058/ADR-059 Updates (proposals), and write `CAP-072-FINDINGS.md`; the release verdict for 1.2.0 is the analysing
      session's, with the maintainer.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-072-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BH/CAP-072-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-072-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BH/CAP-072-EVENT-NOTES
