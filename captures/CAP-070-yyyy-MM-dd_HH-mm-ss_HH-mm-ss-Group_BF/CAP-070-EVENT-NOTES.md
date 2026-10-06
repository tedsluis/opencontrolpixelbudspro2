# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, a secondary user without Google Play) — Group BF, the 1.1.0 release APK (`CAP-070`)

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

## Log Metadata

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
| Log files | TBD — `CAP-070-btsnoop_hci.log` and `.log.last` (Bluetooth is toggled in P2 and VI), the app's exports, logcat, the two `uiautomator` dumps, P0/P1 outputs |

## Preparation

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

## Steps

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

### I. The update and the Connect read (`APP_TESTPLAN.md` T1, T2, S6; `PAIR-003`, `BATT-004`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BF-1 | P0–P7 done; screen recording on; Bluetooth on | open the app (it connects by itself, ADR-044; else **Connect**); go straight to **Controls** | for about the first second the unread switches show "—", then the Buds' values (S6, screen recording); no crash after the update | DLCI 0x02 open; the announcement; `ReadSetting 4:16`, then **twelve** `ReadSetting` requests in the order 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29, each answered by a `RESPONSE` `4:{N:…}`; one `SubscribeRuntimeInfo` | `PAIR-003`, `BATT-004` | a request missing or out of order; any request for 13, 21, 23 … 39; a request on DLCI 0x08/0x0a |
| BF-2 | ready | gear → **Info** (hold 3 s on the build line and on "Control channel") | "App: 1.1.0, build <hash> (<date>)", no "-dirty"; "Control channel: 19" or "21" — note it | nothing sent while the menu is open | — | another version, or "-dirty" |
| BF-3 | ready | **Controls**: read each card; open the (i) of Head gestures, Multipoint, Case sounds. **Sound**: the Volume EQ switch under the presets; the Equalizer (i) | order of cards: Touch controls, Press and hold, Head gestures, In-ear detection, Multipoint, Case sounds (Earbuds replaced, Other alerts); labels only, no note; each (i) "<Label>: read HH:MM:SS"; Equalizer (i) ends "Volume EQ: read HH:MM:SS"; note every value | the answers of BF-1 decide the values: `11:v`, `29:1`=off/`29:2`=on, `27:v`, `28:v`, `15:v` | `MULTI-001`, `HEAD-001`, `CASE-001`, `CASE-002`, `AUDIO-002` | a switch whose position differs from its read answer |

### II. The five switches on the current channel, both buds worn (`APP_TESTPLAN.md` T3–T8)

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

### III. Channel 19 forms (`APP_TESTPLAN.md` T9; `MULTI-001`, `HEAD-001`, `AUDIO-003`, `CASE-004`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BF-13 | both buds into the case (lid open), wait until "ready" again | take **only the Left** bud out, into the left ear (head in view); **within about 2 s of "The app's channel was closed …"**, before "ready", tap **Multipoint** | the tap: "The setting was not changed: The app's channel is being reopened — try again in a moment." (`APP_TESTPLAN.md` C12), the switch unchanged; then ready by itself; Info: **"Control channel: 19"** | Buds `DISC` 0x02 → app `SABM` ≈ 1.5 s later → announcement with `10 13` (channel 19); **no** `WriteSetting` for the early tap (nothing queued) | `CASE-004`, `INEAR-002`, `PAIR-003` | a write sent later by itself; channel 21 announced (then do V first and come back) |
| BF-14 | on 19 | Multipoint **off**, then **on** | each after its OK | `…2a04220258009d8f9dc47e`, then `…2a04220258010bbf9ab37e` (derived, ch 19) → `RESPONSE` ch 19 — **first capture** | `MULTI-001` | another frame, or an error status / no answer |
| BF-15 | on 19 | Use head gestures **off**, then **on** | each after its OK | `…2a052203e80101fcd6da847e`, then `…2a052203e801024687d31d7e` (derived) → `RESPONSE` — **first capture** | `HEAD-001` | as BF-14 |
| BF-16 | on 19 | Sound: drag the balance to **Right 4**, release (as many drags as needed — say how many) | "Right 4 · changed …" in the (i) | `…2a052203880107e9b86e257e` (`4:{17:7}`, derived) → `RESPONSE` — `TODO.md` §2 | `AUDIO-003` | — |

### IV. Lead L-1: the Left out with both worn on 19 (`INEAR-005`, new)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BF-17 | on 19, Left worn | take the **Right** bud out of the case into the right ear (head in view); wait 10 s; Info: note the channel | both worn; the session may re-open | note whether the Buds `DISC` and which channel they announce (`CAP-066`: a Right insertion with the Left not worn gave 21) | `CASE-005`, `INEAR-003` | — (records the state for BF-18) |
| BF-18 | **both worn, channel 19** (if BF-17 left 21: put the Right back in the case, take it out again, retry once; else note "not reached") | take the **Left** bud out of the ear, hold it in view (head in view) | the session closes and re-opens by itself | 🟡 predicted (`PROTOCOL.md` §2.2a, Updates of 2026-10-01 / 2026-10-03): Buds `DISC` of MAESTRO with the ACL up, then an announcement with `10 15` (**21**) | `INEAR-005`, `INEAR-004` | no `DISC`, or a `DISC` followed by 19 again |

### V. The channel-21 form of Volume EQ on (`APP_TESTPLAN.md` T10; `AUDIO-002`, `CASE-005`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BF-19 | — | both buds into the case; take **only the Right** out, into the right ear (head in view); wait until ready; Info | "Control channel: 21" | announcement `10 15` (channel 21) | `CASE-005`, `INEAR-003` | 19 announced (then record it and repeat once) |
| BF-20 | on 21 | Sound: Volume EQ **off**, then **on** | each after its OK | `…2a04220278000f47ef397e` (= `CAP-041` 2461) → `RESPONSE`; then `…2a04220278019977e84e7e` (derived) → `RESPONSE` — **first capture of the channel-21 "on" frame** (ADR-055; its bytes become the unit-test fixture and the `// TODO(verify)` in `SettingsCodec` goes) | `AUDIO-002` | the "on" frame differs from the derived one, or an error status / no answer |

### VI. The film items of `TODO.md` §2 and the screen-reader text (`APP_TESTPLAN.md` H5, C10, S12, T11)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BF-21 | ready | Sound: drag one EQ band, release; then **Read EQ again** (H5) | the value just written; "EQ updated: HH:MM:SS" moves | `WriteSetting 4:{16:…}` → OK; `ReadSetting 4:16` → the same five floats | `EQS-001` | the read differs from the write |
| BF-22 | ready | **swipe** left through all five tabs and back (C10/O2), slowly | the tab follows each swipe; the bottom bar agrees | — | — | a swipe that does not change the tab (screen recording) |
| BF-23 | ready | Debug tab → **Export debug log**; **rotate the phone while Android's "save as" dialog is open**; save (S12) | "Debug log saved (N lines)."; the file has content | — | — | an empty file or no toast |
| BF-24 | ready | **Export** again (before the process ends); Quick Settings: **Bluetooth off**; `adb shell am force-stop io.github.tedsluis.opencontrolpixelbuds`; open the app; **Controls** tab → `adb shell uiautomator dump /sdcard/CAP-070-controls.xml`; **Sound** tab (scroll to the Balance card) → `adb shell uiautomator dump /sdcard/CAP-070-sound.xml`; `adb pull` both into this folder | "Bluetooth is disabled."; Controls: six "—" (no switches); Sound: five EQ bands, Volume EQ, balance, mono audio and conversation detection show "—" | — | — | in a dump, a node with `text="—"` whose `content-desc` is not `Not read from the Buds yet` |
| BF-25 | — | **Bluetooth on** → the app re-opens the session by itself (or **Connect**) | ready; the values back | the twelve reads | `PAIR-003` | — |
| BF-end | — | export the debug log; status bar across a minute change; stop the film and the screen recording | — | — | — | — |

## Don'ts

- Do not clear the app's data and do not uninstall 1.0.1 first (P1 — the run is an update).
- Do not open the Owner user during the run.
- Do not tap two things within 5 s of each other, except in BF-13 (the early tap).
- Do not use the official app to change a setting during the run (it is not in this user).

## After the run

The exports (`adb pull` from the test user's storage), both `btsnoop_hci.log` files, the app's logcat, the two `uiautomator` dumps, P0's and P1's
outputs, the camera film and the screen recording. All into this folder, then `sha256sum *`.

## Analysis checklist

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
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-070-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BF/CAP-070-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-070-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BF/CAP-070-EVENT-NOTES
