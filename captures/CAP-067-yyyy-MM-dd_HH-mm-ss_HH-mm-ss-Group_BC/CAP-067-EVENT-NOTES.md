# Event Notes: OpenControl for Pixel Buds on Pixel 9a (GrapheneOS) — Group BC, the first hardware run of the `ai-sessions/0064` build (`CAP-067`)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0064`, 2026-10-01; scope and order are the maintainer's choice in chat 2026-10-01,
`AskUserQuestion` "F-5 CAP-067": *"As listed, destructive last (Recommended)"*). **Adapted 2026-10-02 (`ai-sessions/0066`, the maintainer's changes before
the run):** the balance steps `[‹]`/`[›]` are gone (section II is the slider again) and Info has no "Licence on GitHub" link (P7: two links). After the run: rename this folder from the placeholder
`CAP-067-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BC` to the film's first/last overlay times and analyse it as `CAP-066` was (`ai-sessions/0063`).

**Purpose:**

- **A.0/P7 — the Info tab of the `0064`/`0066` build** (F-6, DECISIONS.md ADR-050 and its 2026-10-02 Update): the build line, the licence line "GNU Affero General
  Public License v3.0 or later (AGPL-3.0-or-later)", **Read the licence** (the bundled text, offline), and the two links (README, issues) — each opened once **on film** (the maintainer approved links at the
  `0064` checkpoint, "F-6 links": *"Links + bundled licence (Recommended)"*).
- **I — F-1, the tab across a configuration change.** `CAP-066` K4r: rotating reset **Sound → Connection** (`CAP-066-FINDINGS.md` §6). The `0064` build skips the
  pager ↔ back-stack sync until the restored back stack is known (`OpenControlNavHost.kt`, test `TabRestoreTest`).
- **II — the balance slider (BB-10 again).** The `0064` steps were removed in `ai-sessions/0066` (the maintainer's choice); the slider writes once per release.
  `CAP-066` BB-10: 32 drags, never Right 4 (`17:7`). Here: drag to Right 4 (count the drags) — the value before `CAP-064`.
- **III — BB-12, the open half of lead L-1** (`PROTOCOL.md` §2.2a, 2026-10-01 `0063` Update 🟡): with both buds worn on channel **19**, take the **Left** out —
  predicted: a Buds `DISC` of MAESTRO with the ACL up, then announcement **21**.
- **IV — BB-15, watch only:** an answer cut off by the claim's close (`ai-sessions/0062` F-3) — never seen on hardware yet.
- **V — K4d's rest:** dark mode **Off**, and **System** with Android's own dark switch (a configuration change, so also an F-1 check).
- **VI — L3:** an export with Debug mode **off** (no hex lines).
- **VII — F-3 and F-4 after a Bluetooth off/on:** the export names the loss "Bluetooth was switched off on this phone" (final, no "(provisional …)") and logs
  "Bluetooth adapter: ON -> TURNING_OFF"; logcat's StrictMode lines after the off/on (F-4: the app closes every profile proxy it obtained; 🟡 the framework's
  `BluetoothLeAudio` may still warn — `CAP-066-FINDINGS.md` §8).
- **VIII — K5:** GrapheneOS's Bluetooth auto-off set to a short value on film, then restored (`CAP-066`: it was disabled, `delayMillis: 0`).
- **IX — destructive last:** A5 (*Nearby devices* denied), (E) the Buds forgotten, B4 (Pair a device twice quickly), Z1 (re-pairing, `PAIR-001`).

**Facts the maintainer gave (chat 2026-10-01):** no narration on the films; Play services' *Nearby devices* permission is **allowed**; the build is read from the
Info tab on film.

---

### A.0. Which phone, which app, which build

| Item | Value |
|---|---|
| Phone | **Pixel 9a, GrapheneOS** |
| App under test | **OpenControl for Pixel Buds**, debug APK of the `ai-sessions/0064` commit or later — read from the **Info tab on film** (P7: "App: 0.1.0-dev, build <hash>[-dirty] (<commit date>)") |
| Official Pixel Buds app | **Not used.** Pixel 7a: Bluetooth off |
| Play services *Nearby devices* | allowed |
| Buds | Pixel Buds Pro 2, firmware `release_5.203` |

### A.1. Preparation

| # | Check | Done |
|---|---|---|
| P1 | Debug APK installed **without** clearing the app's data | ☐ |
| P2 | Bluetooth HCI snoop log on; switch Bluetooth off and on **on film** | ☐ |
| P3 | Camera films the phone, the case and **your head** (ears visible for every wear step); head on the right of the frame = Left bud (as `CAP-064`) | ☐ |
| P4 | Do Not Disturb on; GrapheneOS Bluetooth auto-off: note its current value (section VIII restores it) | ☐ |
| P5 | Status bar on film across a minute change at the start and at the end (the clock offset) | ☐ |
| P6 | Quick Settings: the ANC tile large (as `CAP-066` BB-13), the Bluetooth tile reachable | ☐ |
| P7 | After the first "ready": gear → **Info** — hold 3 s (build line, firmware lines, "Control channel: N"); then **Read the licence** — scroll once, hold 3 s, **Close**; then **README on GitHub**, **Report an issue on GitHub** — each opens the browser on film; back to the app after each | ☐ |

**Rhythm:** one action, then wait 5–10 s (longer where a step says so). Something unexpected: stop, wait 10 s, continue.

### A.2. Steps

DLCI numbers are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05). Read the announced channel from the export line "Maestro channel announced by
the Buds: N".

#### P7 expected (F-6)

| What | Expected on screen | Expected on the wire / in the logs | Refuted if |
|---|---|---|---|
| Info tab | "Licence", "GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)", "Read the licence" (no "Licence on GitHub"); "Project", "README on GitHub", "Report an issue on GitHub", "Links open in your browser; this app itself has no internet access."; then "The Buds" with the firmware lines | nothing from the app on RFCOMM while the menu is open | a link is missing or the menu sends anything |
| Read the licence | a dialog "Licence" with the text starting "GNU AFFERO GENERAL PUBLIC LICENSE / Version 3, 19 November 2007", scrollable; **Close** | — (bundled, offline) | the text does not show, or a browser opens |
| Each link | the browser opens `…/blob/main/README.md`, `…/issues` (github.com/tedsluis/opencontrolpixelbudspro2) | system log: an `ACTION_VIEW` start of the browser (`START u0 {act=android.intent.action.VIEW dat=https://github.com/…}`); the app's merged manifest has no `INTERNET` | another address opens, or no app opens without the message "No app on this phone can open web links. The address is …" |

#### I. F-1 — the tab across a configuration change

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BC-1 | ready, **Connection** tab | Rotate to landscape, wait 3 s, rotate back | Connection stays selected and shown | nothing (UI only); logcat `wm_on_create` (activity relaunch) per rotation | — |
| BC-2 | ready | Repeat BC-1 on **ANC**, **Sound**, **Controls**, **Find** | the same tab stays selected and shown, each time | nothing | the screen switches to Connection (the `CAP-066` K4r defect) |
| BC-3r | ready | Gear → **Info**; rotate and back; then the top bar's back arrow | still Settings → Info after each rotation; back returns to the tab the menu was opened from | nothing | the menu closes or another tab shows |

#### II. The balance slider — BB-10 again (`qhr` field 17, ADR-045 unchanged; the `0064` steps removed in `ai-sessions/0066`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BC-3 | ready, **Sound** tab, balance read as **Centre** (`CAP-066` ended at `17:0`) | Drag the slider toward **R** and release, aiming at **Right 4**; repeat until the label reads Right 4 (count the drags; say nothing — the film shows the label) | the label after each OK | exactly one `WriteSetting 4:{17:n}` per release (zigzag), none during a drag, each → empty `RESPONSE` OK; the target **`17:7`**: on channel 21 = `CAP-064` 6671 `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 05 22 03 88 01 07 a9 7d 5e df 03 7e`; on channel 19 (derived, not captured) `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 05 22 03 88 01 07 e9 b8 6e 25 7e` | a write per drag frame, or the label changes before the OK |
| BC-5 | after BC-3 | Drag to near the centre and release | a release within ±3 of the centre writes `17:0` ("Centre") | one write | as BC-3 |

#### III. BB-12 — lead L-1, the Left out with both worn on 19

| Step | Pre-state | Action | Expected | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BC-6a | ready, both worn | If the export's last announcement is **21**: take the **Right** out onto the table, wait 20 s, put it back in, wait 20 s (`CAP-066`: ⇒ `DISC` + 19) | the app re-opens by itself | Buds `DISC` of MAESTRO with the ACL up, then announcement 19 | — (a precondition) |
| BC-6 | both worn, announcement **19** | Take the **Left** bud out of the ear onto the table (on film); wait 20 s | the app re-opens by itself if the Buds close the channel | 🟡 predicts: a Buds `DISC` of MAESTRO with the ACL up, then announcement **21** [`INEAR-004`] | the session stays on 19, or comes back on 19 |

#### IV. BB-15 — the cut-off watch (cannot be provoked)

| Step | Pre-state | Action | Expected on screen | Expected on the wire |
|---|---|---|---|---|
| BC-7 | any ANC tap or Refresh while Play services re-opens its claim | — | "The answer was cut off — another app took the Buds' channel. Tap Refresh to see the current mode."; after a cut-off `08 12` the mode dimmed and the (i) dot; **never** "The Buds didn't respond in time." for it | the app's `08 12`/`08 11`, then a phone `DISC` on the Message Stream DLCI before the Buds' ACK/`Notify` (as `CAP-065` 2640 → 2649 → 2651) |

#### V. K4d — dark mode Off; System with Android's own switch

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BC-8 | ready, Android **light**, the **Sound** tab | Gear → Settings → **Dark mode: Off**; back; gear → **System**; back; Quick Settings → Android's **dark theme on**, wait 5 s, **off** | Off = light; System follows Android's switch at once; **the Sound tab stays** through Android's switch (a configuration change — F-1) | nothing | the scheme does not follow, or the tab resets |

#### VI. L3 — an export with Debug mode off

| Step | Pre-state | Action | Expected | Expected in the file | Refuted if |
|---|---|---|---|---|---|
| BC-9 | Debug mode on (as usual) | Gear → **Debug** → **Debug mode off** → **Export debug log** (a second file); then Debug mode back on | the save dialog; "Debug log saved (N lines)." | no `DLCI 0x..:` hex lines after the switch-off line; state lines still there | a hex line after the switch-off |

#### VII. F-3 / F-4 — Bluetooth off and on (K1/K2 again)

| Step | Pre-state | Action | Expected on screen | Expected in the logs | Refuted if |
|---|---|---|---|---|---|
| BC-10 | ready, the app **on screen** | Quick Settings: **Bluetooth off**; wait 10 s | "Bluetooth is disabled." + Enable Bluetooth; no crash | export: "Bluetooth adapter: ON -> TURNING_OFF", "… -> OFF", "Session lost: channel 0x02 closed …", **"Session loss cause: Bluetooth was switched off on this phone"** — without "(provisional …)" — and no later "undetermined" line; system log `BluetoothAutoOff … STATE=13`, `STATE=10` | the cause is "undetermined" or provisional, or no adapter line |
| BC-11 | after BC-10 | Bluetooth on; wait for the automatic re-open | ready | export "Bluetooth adapter: OFF -> TURNING_ON", "-> ON", "Automatic re-open of the session (ADR-044, trigger: LINK_BACK)" [`PAIR-003`] | no re-open while the app is visible and Android reports the Buds connected |
| BC-11s | after BC-11 | Leave the app (home), wait 10 s, come back | — | logcat: "Android link observer stopped" then "… started"; `StrictMode … LeakedClosableViolation` lines: note each object and creation site — 🟡 a `BluetoothLeAudio` one may still appear (framework `CloseGuard`, `CAP-066-FINDINGS.md` §8); an app-created proxy left unclosed is not visible directly | — (information) |

#### VIII. K5 — GrapheneOS Bluetooth auto-off

| Step | Pre-state | Action | Expected on screen | Expected in the logs |
|---|---|---|---|---|
| BC-12 | buds in the case, lid closed (no connection) | Settings → Security & privacy → Bluetooth auto-off: the **shortest** value (on film); lock the phone; wait past that time; unlock; open the app; then **restore** the value of P4 (on film) | "Bluetooth is disabled." (normal), no crash | system log `BluetoothAutoOff` "scheduled alarm" and the adapter `STATE=13`/`STATE=10`; the app's export (if open then) "Bluetooth adapter: … -> OFF" |

#### Restore

| Step | Action | Done |
|---|---|---|
| BC-R | Sound tab: drag to **Right 4** (`17:7` → OK) — the balance before `CAP-064` (if BC-3 ended there, nothing to do) | ☐ |

#### IX. Destructive steps — last

| Step | Pre-state | Action | Expected on screen | Expected on the wire |
|---|---|---|---|---|
| A5 | — | Settings → Apps → OpenControl → Permissions → *Nearby devices*: **Don't allow**; open the app; allow again | "Bluetooth permission needed" / "You denied the permission." + Allow | — |
| (E) | — | Forget the Buds in Android's Bluetooth settings; open the app | "No Pixel Buds Pro 2 paired yet." + Pair a device | bond removal |
| B4 | after (E) | Open the lid; tap **Pair a device twice quickly** | one picker, no crash | — |
| Z1 | after B4 | Pair in the picker; Connect | ready | CDM association, bonding (SSP or CTKD, `PROTOCOL.md` §5.1) [`PAIR-001`] |
| BC-end | — | Status bar across a minute change; stop the film | — | — |

### A.3. After the run

Gear → **Debug** tab → **Export debug log**; `adb bugreport`; the raw `btsnoop_hci.log` (both, if Bluetooth was toggled); app logcat and system log (GrapheneOS log
viewer); the film. All into this folder, then `sha256sum *`.

### A.4. Analysis checklist

- [ ] Pre-filter by the Buds' classic handle; DLCIs by content.
- [ ] P7: the Info frames — build hash against `git log`; the licence line; the dialog's first line; each link's browser frame and its `ACTION_VIEW` system-log line;
      no app RFCOMM frame while the menu was open.
- [ ] BC-1 … BC-3r, BC-8: per rotation / dark switch, the tab before and after (film) against the logcat `wm_on_create` times — **refuted if** any tab other than
      Connection comes back as Connection.
- [ ] BC-3, BC-5, BC-R: every `WriteSetting 4:{17:n}` (zigzag) with its `RESPONSE`, one per release; the number of drags to reach `17:7`; the label frames after each OK; the
      channel-19 bytes of `17:7` against the derived frame above (a first capture of it).
- [ ] BC-6: the announcement before and after against which bud was taken out (film), and any Buds-side `DISC` with the ACL up — settles BB-12 (L-1).
- [ ] BC-7: any app claim closed between the request and the answer.
- [ ] BC-9: no hex line after the Debug-mode-off line.
- [ ] BC-10/BC-11: the export's adapter and loss-cause lines against the system log's `STATE_CHANGED` times; "provisional" must not appear on the Bluetooth-off line.
- [ ] BC-11s: every StrictMode violation with its object and creation site.
- [ ] BC-12: the auto-off alarm lines and the app's adapter lines.
- [ ] IX: `APP_TESTPLAN.md` sections A, B, E; Z1's bonding events.
- [ ] Registry Test-IDs: [`AUDIO-003`] (BC-3, BC-5, BC-R), [`INEAR-002`]–[`INEAR-004`] (BC-6a/BC-6), [`PAIR-003`] (BC-11), [`BATT-004`] (every connect), [`PAIR-001`]
      (Z1).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-067-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BC/CAP-067-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-067-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BC/CAP-067-EVENT-NOTES
