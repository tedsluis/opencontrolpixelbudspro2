# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, the secondary user without Google Play) — Group BG, the 1.1.1 release APK (`CAP-071`)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0079`, 2026-10-08). Scope chosen by the maintainer in chat 2026-10-08: a **broad
regression run** of the 1.1.1 release APK — the app of 1.1.0 rebuilt with the toolchain of `ai-sessions/0078` (Gradle 9.7.1, AGP 9.3.3 with built-in Kotlin,
Kotlin 2.4.20, Compose BOM 2026.09.00 / material3 1.4.0, AndroidX, Hilt 2.60.1; compileSdk 37, targetSdk and minSdk 34) — **installed over 1.1.0**, filmed
with sound, Android's screen recording and the system log, plus every open film item of `TODO.md` §2 (T11, H5, S12, S6, C12, lead L-1, the sound
observations). The maintainer films; a later CAPTURE session analyses it and gives the release verdict. After the run: rename this folder from the
placeholder to the film's first/last overlay times, and the film to CAP-071-recording.mp4.

**Purpose.** Nothing in the app's behaviour was meant to change (0078 RESULT: same tests, same mutations caught, same permissions and components). This run
checks that on the phone, where the new toolchain could show:

- **I — the update and the start.** 1.1.1 over 1.1.0 in user 10 (the first real update test: `CAP-070`'s 1.1.0 was a first install there, 🟡); the settings
  1.1.0 stored (dark mode, Debug mode) read back by DataStore 1.2.1; the Connect read of twelve settings; "—" in the first second (S6).
- **II — the screens** (Compose 1.7 → 1.12, material3 1.3 → 1.4): every tab by tap and by swipe, a pull on each, the (i) dialogs, the settings menu's look
  (`TabRow` kept, deprecated in 1.4.0), back from the menu, a rotation, Android's dark switch, the licence dialog (`LocalResources`) and the two links
  (`toUri()`).
- **III — every write once, byte for byte** (the codec is unchanged): ANC from the tab and the tile, an EQ preset and a slider, balance, mono, conversation
  detection, Volume EQ, touch controls, press and hold, the mode list, in-ear detection, head gestures, Multipoint, the two case sounds, Ring/Stop — each
  request equal to its reference (table below); the values read back after a reconnect. The preset **Balanced** and the mode list without **Off** also
  restore the Buds (`TODO.md` §4).
- **IV — robustness:** the case (the Buds close the session; the automatic re-open, ADR-044), Home for two minutes, Bluetooth off/on.
- **V — the open film items of `TODO.md` §2** (film 2): lead L-1 with the head in view (`INEAR-005`), C12 (a tap during a re-open), S12 (an export across a
  rotation), T11 (the screen-reader text from two `uiautomator` dumps).

The run is two films in this folder: **film 1** = P0–P9 and sections I–IV (≈ 30 min), **film 2** = section V (≈ 10 min). If time is short, film 2 can be
another day — the build stays the same; say so in the notes.

### Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-071` |
| Group(s) | BG (new) |
| Date | TBD |
| Phone | Pixel 9a, GrapheneOS (Android 17 in `CAP-070`) — the secondary user without Google Play (user 10 in `CAP-070`; P0) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.1.1** (versionCode 10101), the release APK built by `scripts/release.sh` and kept as `RELEASING.md` B3 says — on the **Info tab on film**: "App: 1.1.1, build <hash> (<date>)", no "-dirty"; APK and certificate SHA-256 from the script's output |
| Previous version in this user | 1.1.0 (`0323849`), installed 2026-10-07 for `CAP-070` — the update is from it |
| Official Pixel Buds app | Not used (not in this user). Pixel 7a: Bluetooth off |
| Buds | Pixel Buds Pro 2; firmware from the Info tab on film (`release_5.203` expected; any other ⇒ Safe Mode, stop and say so) |
| Video files | TBD — film 1 and film 2, **with sound** (P3) |
| Screen recording | TBD — Android's screen recorder in the test user, film 1 and film 2 (P4) |
| Log files | TBD — CAP-071-btsnoop_hci.log and .log.last (Bluetooth is toggled in P2, IV and V), the app's debug exports, the app logcat, the **system log** (P6), the two `uiautomator` dumps (BG-23), the P0/P1 outputs |

### Preparation

What `CAP-070` missed is marked ★ — please check those twice.

| # | Check | Done |
|---|---|---|
| P0 | In the test user: `adb shell am get-current-user`; `adb shell pm list packages --user <id> \| grep -i -E "gms\|vending"; echo "exit=$?"`; positive control: the same with `grep opencontrol` (exit 0). Save the outputs **with the exit statuses** into this folder ★ | ☐ |
| P1 | **Before the update, in 1.1.0** (on film): gear → **Settings → Dark mode On**; **Debug → Debug mode on**; Info shows "App: 1.1.0, build 0323849". Then `adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds \| grep -A12 "User <id>:"` → save. **Install 1.1.1 over it**: `adb install --user <id> -r dist/1.1.1/opencontrol-pixelbudspro2-1.1.1.apk` — no uninstall, no "clear data". The same `dumpsys` again → save ★ (`CAP-070` was a first install) | ☐ |
| P2 | Bluetooth HCI snoop log on (set in the Owner, then switch to the test user); switch Bluetooth **off and on on film** | ☐ |
| P3 | Camera films the phone, the case **and your head with both ears** at every wear step (head on the right of the frame = Left bud) ★; **the camera records sound** — play back 3 s before starting ★ (`CAP-070`'s film had no audio track) | ☐ |
| P4 | Android's **screen recorder** on (Quick Settings) ★ | ☐ |
| P5 | Status bar on film across a minute change at the start and at the end of each film | ☐ |
| P6 | **System log** ★: on the computer, before P2, `adb logcat -b all -v threadtime > CAP-071-logcat-all.txt` (stop it with Ctrl-C after the last export); it answers `CAP-070`'s open question — what sent the `SIGQUIT`s | ☐ |
| P7 | A short music track ready at **low volume** for the sound observations (BG-13, BG-14) | ☐ |
| P8 | Buds charged, both in the case, **lid open**; Android shows them connected before the app is opened (the app then opens its session by itself, ADR-044) | ☐ |
| P9 | **No rehearsal before the film** ★ (`CAP-070`'s logs hold an unfilmed one) — the app is not opened between P1's install and BG-1 | ☐ |

**Rhythm:** one action, then wait 5–10 s; **say** what you do at each wear change and at each observation (the film has sound now). Something unexpected:
stop, wait 10 s, say it, continue. **Export the debug log before any step that ends the process and after every Bluetooth off/on.** Before committing:
check the films for a street-address overlay, Wi-Fi names and other device names (`CAP-070` privacy note).

### Steps

DLCIs are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05). Every MAESTRO request starts `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25` on
**channel 21** or `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25` on **channel 19** (Info → "Control channel"). **Reference frames** — the 1.1.0 build's own
requests in `CAP-070` unless named (all re-derived in `ai-sessions/0079` RESULT §A.3, which lists every frame number):

| Request | Channel 21 | Channel 19 |
|---|---|---|
| `ReadSetting 4:16`, then 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29 | `CAP-070` A271, A296 … A329 | A966, A979 … A1035 |
| `SubscribeRuntimeInfo` | A332 `…90821ee66654bfab7e` | A1038 `…90821ee6602d65a97e` |
| Multipoint `4:{11:0}` / `{11:1}` | A752 `…2a0422025800ad636bac7e` / `CAP-069` 3212 `…2a04220258013b536cdb7e` | A3668 `…2a04220258009d8f9dc47e` / A3679 `…2a04220258010bbf9ab37e` |
| Head gestures `4:{29:1}` / `{29:2}` | A762 `…2a052203e80101bc106ba27e` / `CAP-069` 2564 `…2a052203e801020641623b7e` | A3614 `…2a052203e80101fcd6da847e` / A3685 `…2a052203e801024687d31d7e` |
| Earbuds replaced `4:{28:0}` / `{28:1}` | A770 `…2a052203e0010092717fdb7e` / `CAP-058` 5643 `…2a052203e00101044178ac7e` | `CAP-024` 1988 `…2a052203e00100d2b7cefd7e` / A1992 `…2a052203e001014487c98a7e` |
| Other alerts `4:{27:0}` / `{27:1}` | A777 `…2a052203d80100bac507f17e` / `CAP-058` 5697 `…2a052203d801012cf500867e` | `CAP-024` 2053 `…2a052203d80100fa03b6d77e` / A1995 `…2a052203d801016c33b1a07e` |
| Volume EQ `4:{15:0}` / `{15:1}` | A785 `…2a04220278000f47ef397e` / A4800 `…2a04220278019977e84e7e` | A3072 `…2a04220278003fab19517e` / A1953 `…2a0422027801a99b1e267e` |
| Balance Right 4 `4:{17:7}` | `CAP-064` 6671 `…2a052203880107a97d5edf037e` | A3747 `…2a052203880107e9b86e257e` |
| EQ preset Balanced `4:{16:[-3.5, 0.5, 1.0, -1.0, 2.5]}` | `CAP-059` 2188 `…2a1e221c8201190d000060c0150000003f1d0000803f25000080bf2d0000204008f7fa577e` | no captured frame — the app's codec output (unit tests) |
| Mode list without Off `4:{12:{1:1 2:0 3:1 4:0}}` | `CAP-041` 2198 `…2a0c220a62080801100018012000fc57b66a7e` | no captured frame — the app's codec output (unit tests) |
| ANC `Get` (each claim) | `08 11 00 00` (`CAP-070`, 11 of 11 claims) | |
| ANC `Set` Transparent / Off / Active / Adaptive | `CAP-068-btsnoop_hci.log` 1039 `08 12 00 14 01 e8 e8 80` + 16 × `00`; 1090 `…e8 e8 20…`; 1146 `…e8 e8 08…`; 1266 `…e8 e8 40…` | |
| Ring Left / Stop / Right | `CAP-068-btsnoop_hci2.log` 2142 `04 01 00 01 02`, 2416 `04 01 00 01 00`; `CAP-062-btsnoop_hci.log` 9660 `04 01 00 01 01` | |

A request with no row (the other settings writes: touch controls, press and hold, in-ear detection, mono, conversation detection, a slider value) is
compared with the app's unit-test fixture for the same request (`android/data/src/test`). **Any difference is the finding** — the codec did not change.

#### I. The update and the start (film 1; `APP_TESTPLAN.md` U1–U3, T1, S6; `PAIR-003`, `BATT-004`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BG-1 | P0–P9 done, 1.1.1 just installed over 1.1.0; screen recording on | open the app; within the first second go to **Controls** | **dark** (the 1.1.0 choice kept); no crash; the session opens by itself; for ≈ 1 s the unread switches show "—", then the Buds' values (S6, screen recording) | DLCI 0x02 open; the announcement; `ReadSetting 4:16`, then twelve reads in the order of the table, each answered; one `SubscribeRuntimeInfo` — each request equal to its reference | `PAIR-003`, `BATT-004` | a crash; light theme; a read missing, out of order or different; any other request |
| BG-2 | ready | gear → **Settings** (Dark mode shows **On**), **Debug** (Debug mode **on**), **Info** (hold 3 s on the build line and on "Control channel") | the two 1.1.0 choices kept; "App: 1.1.1, build <hash> (<date>)", no "-dirty"; firmware `release_5.203` ×3; channel 19 or 21 — say it | nothing sent | — | either choice lost; another version or "-dirty" |
| BG-3 | ready | gear → Settings → Dark mode **System** (leave it there) | the app follows Android's theme at once | — | — | — |

#### II. The screens (film 1; `APP_TESTPLAN.md` U4–U9, C10, O2–O8, O13, Q2, R1–R4, K4)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BG-4 | ready, Connection tab | tap each bottom tab in turn (Connection, ANC, Sound, Controls, Find); then **swipe** left through all and back | each tab as in `CAP-070`'s film (same cards, texts, order); the bottom bar follows each swipe | — | — | a missing or moved card, text cut off, a swipe that does not change the tab |
| BG-5 | ready | **pull down** on each tab, ≥ 5 s apart; say each time | a spinner while it runs, gone when done; Sound/Controls: the (i) "read" times move | Connection/Find: one DLCI 0x04 claim (`08 11`, the `03 03` burst); Sound/Controls: the twelve reads again; ANC: `08 11` → `08 13` | `BATT-004` | a spinner that never stops; a pull that sends nothing |
| BG-6 | ready | open the (i) on the Battery card, the ANC card, the Equalizer card and two Controls cards; close each with Close and once with back | a dialog with the times, Close and back close it | — | — | a dialog that does not open or close |
| BG-7 | ready | gear: look at the menu's tab row 3 s; tap Settings, Debug, Info; leave with the **←** arrow; open it again, leave with **system back** | tabs **Settings · Debug · Info**, the selected label in the accent colour with a **full-width** underline (as 1.1.0 — `TabRow` kept, `ai-sessions/0078`); ← and back return to the tab you came from | — | — | the underline only under the label, a grey selected label, another tab order, back leaving the app |
| BG-8 | ready, Sound tab | rotate to landscape and back; the same on Settings → Info | the same tab stays (`CAP-066` K4r fix); nothing lost | — | — | the tab resets |
| BG-9 | ready | Android's Quick Settings: **Dark theme** on, then off | the app follows each (Dark mode = System, BG-3) without a restart | — | — | the app does not follow, or restarts |
| BG-10 | ready | Settings → Info: **Read the licence**, scroll, Close; then **README on GitHub** and **Report an issue on GitHub** (back to the app after each) | the full licence text ("GNU AFFERO GENERAL PUBLIC LICENSE / Version 3, 19 November 2007 …"); the browser opens `…/blob/main/README.md` and `…/issues` | nothing from the app (the browser is another app) | — | an empty or cut dialog; a link that does nothing |

#### III. Every write once, buds worn, head in view (film 1; `APP_TESTPLAN.md` U10–U14, F1–F4, G3, H2–H6, M2–M6, N2–N11, I1–I3, T3–T8)

Both buds **in your ears** and the head in view unless a step says otherwise; music playing at low volume for BG-13/BG-14. Every write: the control moves
only after the Buds' OK ("changed HH:MM:SS" in the (i)).

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BG-11 | worn | ANC tab: **Transparent**, **Adaptive**, **Off**, **Active**, 8 s apart; say what you hear | each mode after its ACK; audible | per tap one claim: `08 11 00 00` → `Notify`, then the `Set` = the reference (`…80`, `…40`, `…20`, `…08`) → ACK `ff 01` | `ANC-004`, `ANC-003`, `ANC-001`, `ANC-002` | a `Set` that differs, a NAK while worn, no `Get` first |
| BG-12 | worn | Quick Settings: the **ANC tile** once; then pull the notification shade | the next mode after the Buds' reported one; the notification "OpenControl for Pixel Buds" shows it | one claim `08 11` → `Notify` → `Set` → ACK | `ANC-001`…`ANC-004` | the tile does nothing or opens the app while ready |
| BG-13 | worn, music | Sound: preset **Balanced** (restores the Buds, `TODO.md` §4); drag **Upper treble** up, release; **Read EQ again** (H5); balance to **Right 4**, then **Centre**; **Mono audio** on/off; **Conversation detection** off/on; **Volume EQ** off/on — say what you hear at each | each after its OK; *Read EQ again* shows the value just written; "Right 4", "Centre" | Balanced = `CAP-059` 2188 on 21 (on 19: the codec's output); one `WriteSetting 4:{16:…}` on release; `ReadSetting 4:16` = the write; `17:7`, `17:0`; `19:1`, `19:0`; `22:0`, `22:1`; `15:0`, `15:1` | `EQP-001`, `EQS-001`, `AUDIO-003`, `AUDIO-001`, `CONV-001`, `AUDIO-002` | a request that differs; a read that differs from its write |
| BG-14 | worn | Controls: **Use touch controls** off/on; press and hold **Left: Digital assistant**, then **Noise control**; mode list: **untick Off** (restores the Buds); **In-ear detection** off/on; **Use head gestures** off/on; **Multipoint** off/on; **Earbuds replaced off** → take the **Left** out of the ear into the case and out again → **say whether the case sounded** → **on** → the same → say (wait for "ready" after each bud change); **Other alerts** off/on | each after its OK | `4:{4:0}`/`{4:1}`; `4:{7:{1:{4:{1:6}}}}`/`{1:5}`; `4:{12:{1:1 2:0 3:1 4:0}}` (= `CAP-041` 2198 on 21); `2:0`/`2:1`; `29:1`/`29:2`; `11:0`/`11:1`; `28:0`/`28:1`; `27:0`/`27:1` — each = its reference | `HOLD-005`, `HEAD-001`, `MULTI-001`, `CASE-001`, `CASE-002`, `CASE-004` | a request that differs; the mode list keeps Off |
| BG-15 | worn | **Disconnect**, then **Connect**; open Sound and Controls | every value just written read back ("read …"): Balanced, Centre, mono off, conversation on, Volume EQ on, touch on, Left Noise control, mode list without Off, in-ear on, head gestures on, Multipoint on, both case sounds on | the twelve reads; each answer = the last write | `PAIR-003` | a read that differs from the last write |
| BG-16 | both buds out of the ears, on the table | Find: **Ring Left**, **Stop**, **Ring Right**, **Stop** | the notice and the ring follow each tap | `04 01 00 01 02` → ACK; `… 00`; `… 01`; `… 00` (the references) | `FIND-001`, `FIND-002` | a ring that does not stop |

#### IV. Robustness (film 1; `APP_TESTPLAN.md` C8, C9, J4, K1, K2, R6)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BG-17 | ready | both buds into the case, lid open; wait 10 s; take both out again (do not tap Connect) | the session closes with the 1.0.1 cause text; then ready **by itself** | Buds `DISC` 0x02 (or the ACL drops); the app's `SABM` 0x02 ≈ 1.5 s after the link is back (ADR-044) | `CASE-004`, `CASE-005`, `PAIR-003` | no automatic re-open while the app is on screen |
| BG-18 | ready | **Home**; wait 2 minutes; return to the app | still ready, or a clear message and a re-open; no crash | — | — | a crash |
| BG-19 | ready | **Export** the debug log; Quick Settings **Bluetooth off**; wait 10 s; **Bluetooth on** | "Bluetooth is disabled.", then ready again by itself | export lines "Bluetooth adapter: ON -> TURNING_OFF", "Session loss cause: Bluetooth was switched off on this phone"; after on, the automatic re-open and the twelve reads | `PAIR-003` | a crash; no re-open |
| BG-end1 | — | **Export** the debug log; status bar across a minute change; stop film 1 and the screen recording | "Debug log saved (N lines)." | — | — | — |

#### V. The open film items of `TODO.md` §2 (film 2; `APP_TESTPLAN.md` C12, S12, T11; `INEAR-005`)

Start film 2 and the screen recording; keep the system log running (P6).

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Test-ID | Refuted if |
|---|---|---|---|---|---|---|
| BG-20 | both buds in the case | take **only the Left** out, into the left ear (Info: "Control channel: 19"); then the **Right** into the right ear; wait 10 s (Info again); **with both worn on 19**, take the **Left** out of the ear and hold it in view — **head and both ears in view, say "Left out"** | the session closes and re-opens by itself; Info shows the new channel | 🟡 predicted (lead L-1, `PROTOCOL.md` §2.2a Updates of 2026-10-01/03/07): Buds `DISC` of MAESTRO with the ACL up, then an announcement `10 15` (21) | `INEAR-005`, `INEAR-004` | no `DISC`, or `DISC` followed by 19 again (if the Right's insertion already moved the channel to 21, say so and put it back in the case, retry once) |
| BG-21 | ready, both worn, Controls tab | put the **Right** bud into the case; **as soon as** the card shows that the app's channel was closed (≈ 0.5–1.5 s), **tap Multipoint** — before "ready" | "The setting was not changed: The app's channel is being reopened — try again in a moment."; the switch unchanged; after "ready" nothing is sent by itself | Buds `DISC` 0x02 → app `SABM` ≈ 1.5 s later; **no** `WriteSetting` for that tap | `CASE-004` | a write sent later by itself (if the tap comes after "ready", say so — C12 stays open) |
| BG-22 | ready | Debug → **Export debug log**; **rotate the phone while Android's "save as" dialog is open**; save (S12) | "Debug log saved (N lines)."; the file has content | — | — | an empty file, no toast |
| BG-23 | ready | **Export** again; Quick Settings **Bluetooth off**; on the computer `adb shell am force-stop io.github.tedsluis.opencontrolpixelbuds` (say it); open the app; **Controls** → `adb shell uiautomator dump /sdcard/CAP-071-controls.xml`; **Sound** (scroll to the Balance card) → `adb shell uiautomator dump /sdcard/CAP-071-sound.xml`; `adb pull` both into this folder (T11) | "Bluetooth is disabled."; Controls: "—" in place of every switch; Sound: the five EQ bands, Volume EQ, balance, mono audio and conversation detection "—" | — | — | in a dump, a node with `text="—"` whose `content-desc` is not `Not read from the Buds yet` |
| BG-24 | — | **Bluetooth on** → the app re-opens the session by itself (or **Connect**); **Export**; stop the system log (P6); status bar across a minute change; stop film 2 and the screen recording | ready; the values back | the twelve reads | `PAIR-003` | — |

### Don'ts

- Do not uninstall 1.1.0 and do not clear the app's data (P1 — the run is an update).
- Do not open the app before the film runs (P9); do not open the Owner user during a film.
- Do not tap two things within 5 s of each other, except in BG-21 (the early tap).
- Do not leave the buds in the case when a step says "worn" (`CAP-070` did BF-4…BF-7 with the buds in the case) — say it if a step is done differently.

### After the run

Into this folder: both films, both screen recordings, the system log (CAP-071-logcat-all.txt), every debug export (`adb pull` from the test user's
storage), the app logcat if saved, both btsnoop_hci.log files, the two `uiautomator` dumps, the P0/P1 outputs (before and after the update), and
`dist/1.1.1/`'s `.sha256` and the script's printed certificate line. Then `sha256sum *` into a file. Commit on the release branch `release/1.1.1`, after the
build commit (`RELEASING.md` C3) — never on `main` (it takes no direct push).

### Analysis checklist

- [ ] Scope every filter to the Buds' ACL handle (show the Connection Complete for the Buds' address with that handle); DLCIs by content (`AGENTS.md` §13);
      for a negative, the command, its exit status and a positive control (step 8).
- [ ] P0 (exit statuses, positive control), P1: user id; no `com.google.android.gms` / `com.android.vending`; **the update**: user `<id>`'s `firstInstallTime`
      unchanged from before and `lastUpdateTime` later, `versionCode=10101`; the Info frame "1.1.1, build <hash>" against `git log` and the B1 hash; APK and
      certificate SHA-256 against `dist/1.1.1/` and `RELEASING.md` (`a7530f5c…c79d8dcb`).
- [ ] **The release APK against 1.1.0's:** `aapt2 dump badging` of `dist/1.1.1/…apk` and `~/opencontrol-1.1.0-tested/…apk` — permissions identical (no
      `INTERNET`), `targetSdkVersion:'34'`, `sdkVersion:'34'`, `compileSdkVersion='37'`, the two `uses-library-not-required` (androidx.window); `aapt2 dump xmltree`
      — no new `<activity>`, `<service>`, `<receiver>`, `<provider>`.
- [ ] Zero Play-services claims on the Message Stream (`frame contains 03:08:00:02:01:25`): command, exit status, positive control `CAP-066`.
- [ ] I: dark mode and Debug mode kept across the update (film, BG-1/BG-2); the twelve reads per session in order with their answers (`python3 -I
      scripts/pwrpc_decode.py --handle <handle> <log>`); S6 on the screen recording (frame time of the first "—" and of the values).
- [ ] II: per step on film and the screen recording — same screens as `CAP-070`; the settings menu's tab row (full-width underline, accent label); back;
      rotation; dark switch; licence; links.
- [ ] III: **every request against the reference table** (byte for byte, per channel; the others against the unit-test fixtures); every `RESPONSE`/ACK;
      the read-back of BG-15; the sound observations quoted as said (BG-11, BG-13, BG-14).
- [ ] IV: each session end with its logged cause; the automatic re-opens; Bluetooth off/on.
- [ ] V: BG-20's `DISC` direction and announced channel, with the head side on film — the L-1 result is a **proposal** for `PROTOCOL.md` §2.2a, no status
      change by the analysing session alone; BG-21 — no `WriteSetting` after the early tap, the text on the screen recording; BG-22's file; **the two dumps**
      (count `text="—"` nodes and their `content-desc` per tab).
- [ ] The system log: crashes, ANRs, `StrictMode` (none expected in a release build), and **what sent any `SIGQUIT`** (`CAP-070`'s open question).
- [ ] One row per step — done / not done / done differently; `APP_TESTPLAN.md` section U and the Summary updated; traceability (`AGENTS.md` §13 step 7):
      `PAIR-003` (BG-1, BG-15, BG-17, BG-19, BG-24), `BATT-004` (BG-1, BG-5), `ANC-001`…`ANC-004` (BG-11, BG-12), `EQP-001`, `EQS-001`, `AUDIO-001`…`003`,
      `CONV-001` (BG-13), `HOLD-005`, `HEAD-001`, `MULTI-001`, `CASE-001`, `CASE-002`, `CASE-004` (BG-14, BG-17, BG-21), `CASE-005` (BG-17), `FIND-001`,
      `FIND-002` (BG-16), `INEAR-004`, `INEAR-005` (BG-20) — each referenced in the timeline or flagged "expected but not observed".
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `id_registry.csv`, `APP_TESTPLAN.md`, `TODO.md` §2/§4 (the restored Buds, the items done), and write
      `CAP-071-FINDINGS.md`; the release verdict for 1.1.1 is the analysing session's, with the maintainer.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-071-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BG/CAP-071-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-071-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BG/CAP-071-EVENT-NOTES
