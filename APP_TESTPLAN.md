# APP_TESTPLAN.md — Functional test plan for the OpenControl for Pixel Buds app

**Purpose:** one run through **every function of the app** on real hardware (Pixel 9a / GrapheneOS + Pixel Buds Pro 2), with a place to
record the result of each test. Written 2026-09-25 against the app as committed in `7498cbc` (`ai-sessions/0046`). This is a *user-level*
functional test of this project's own app; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` is the separate catalogue of Buds/official-app behaviours, and the
"Expected on the wire" column below only names what to look for in the HCI log afterwards (`ai-sessions/0046` RESULT §9 has the exact frames).

**How to use:** do the tests in order (later tests assume the earlier state). Fill in **Result** (✅ / ❌ / ⚠️ / — not run) and **Notes** (the
time, what you saw). A ❌ needs the time and a screenshot or the film time — that is what makes it traceable afterwards.

---

## 0. Preparation (do this before test 1)

| # | Check | Done |
|---|---|---|
| P1 | Build and install the debug APK of the commit under test; write the commit hash here: `________` | ☐ |
| P2 | Settings → System → Developer options → **Bluetooth HCI snoop log: Enabled**; then switch Bluetooth **off and on on film** (the log only starts after a toggle) | ☐ |
| P3 | Camera films the phone screen **and** the case/buds; the phone's clock with seconds is visible (or film the status bar at a minute change at the start and the end) | ☐ |
| P4 | Write down Play services' *Nearby devices* permission (Settings → Apps → Google Play services → Permissions): `allowed / denied` | ☐ |
| P5 | Buds charged, in the case, lid closed; the official Pixel Buds app is **not** open | ☐ |
| P6 | Keep a small events file: time + what you did, for every step | ☐ |
| P7 | For the pairing tests (section B): the Buds are **forgotten** in Android's Bluetooth settings first. If you want to keep them paired, skip B and start at C | ☐ |

---

## A. Start, permissions and Bluetooth state

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| A1 | Bluetooth **off**. Open the app. | "Bluetooth is disabled." and **Enable Bluetooth** | — | | |
| A2 | Tap **Enable Bluetooth**. | Android's own "turn on Bluetooth" prompt (not a Play-services dialog); after allowing, the app leaves the "disabled" state | — | | |
| A3 | First start (or after clearing app data): the permission prompts. Allow *Nearby devices*. | The prompt appears; afterwards no "Bluetooth permission needed" | — | | |
| A4 | Allow notifications when asked (or tap **Allow notifications**). | The notifications hint disappears | — | | |
| A5 | *(optional)* Deny *Nearby devices* once, then reopen the app. | "Bluetooth permission needed" / "You denied the permission." with **Allow**; after "don't ask again": **Open app settings** | — | | |
| A6 | Settings → Apps → OpenControl → Permissions. | Only *Nearby devices* and *Notifications*; no location, no network permission | — | | |

## B. Pairing (only if the Buds were forgotten, P7)

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| B1 | Buds not paired, open the app. | "No Pixel Buds Pro 2 paired yet." + **Pair a device** | — | | |
| B2 | Open the case lid; tap **Pair a device**. | "Waiting for you to pick your Pixel Buds in the system dialog"; Android's picker lists **only** Pixel Buds | — | | |
| B3 | Pick the Buds, tap **Toestaan/Allow**. | "Pairing: keep the Buds close …", then "Paired" / "Connected to this phone (Android)" — no error | LE pairing, then the classic connection | | |
| B4 | Tap **Pair a device** twice quickly (or while the picker is open). | No second picker; no crash | — | | |

## C. Connection and Android's own state

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| C1 | Buds paired, taken out of the case (or case open). Look at the Connection tab before tapping anything. | "Connected to this phone (Android)" and "App control: not open yet — tap Connect …" | — (the app opens nothing by itself) | | |
| C2 | Tap **Connect**. | "App control: connecting…" → "App control: ready"; a **Firmware: release_5.203** line; **no** "Safe Mode — read-only" card; a notification "OpenControl for Pixel Buds" | DLCI 0x02 opened; the Buds announce their channel; the app reads the EQ and requests runtime info | | |
| C3 | Tap **Disconnect**. | "App control: not open yet …"; the notification disappears | the app closes DLCI 0x02 | | |
| C4 | Tap **Connect** again. | ready again within a few seconds | as C2 | | |
| C5 | Tap **Connect** twice quickly. | one session, no error | one DLCI 0x02 open | | |
| C6 | Disconnect the Buds in **Android's** Bluetooth panel (tap the Buds' row). | "Android no longer shows the Buds connected …" with **Connect**; no crash | ACL disconnect | | |
| C7 | Reconnect in Android's panel, then **Connect** in the app. | ready again | | | |
| C8 | Put both buds in the case and close the lid. | The session ends with a clear message ("Paired — not connected to this phone …"); no crash | ACL disconnect | | |
| C9 | Take them out again, open the app, **Connect**. | ready again | | | |
| C10 | Swipe left/right between the tabs, and use the bottom bar. | Both change the tab; Back does not jump to Debug | — | | |

## D. Safe Mode and firmware (after the `ai-sessions/0046` fix)

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| D1 | After C2/C4: look at the Connection tab. | "Firmware: release_5.203"; **no** Safe Mode card | the Buds' `GetSoftwareInfo` on DLCI 0x02 | | |
| D2 | Do any write (ANC tap, F1). | It is **sent** — no "Safe Mode: nothing was sent" message | an ANC `Set` (`08 12 …`) on DLCI 0x04 | | |

## E. Battery

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| E1 | Buds in the case, lid open, **Connect**. | Left and Right with a % and "(charging)", each with "(updated HH:MM:SS)" | DLCI 0x04: three `03 03 …` frames | | |
| E2 | Same moment: the **Case** line. | A Case % (compare with Android's Bluetooth panel / the Buds' LED) **or** "The Buds haven't reported the Case level on this connection." — never a made-up value | DLCI 0x02 `SubscribeRuntimeInfo` stream; **no app activity on DLCI 0x08** | | |
| E3 | Take the **Right** bud out; tap **Refresh battery**. | Right loses "(charging)", Left keeps it; the times update | DLCI 0x04 claim, battery `e4 64 ff`-style | | |
| E4 | Take the **Left** bud out too; **Refresh battery**. | Neither shows "(charging)" | | | |
| E5 | Put both back; **Refresh battery**. | Both "(charging)" again | | | |
| E6 | Wait a few minutes with the app open (buds in the case, lid open). | The Case line updates by itself if the Buds send a new value — the app does **not** poll | only Buds-initiated stream packets | | |
| E7 | Check there is **no** dock sentence ("…seem to be in the case") anywhere. | No such sentence (removed in `ai-sessions/0046`) | — | | |

## F. Noise control (ANC screen)

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| F1 | Buds **in your ears**, Connected. ANC tab: tap **TRANSPARENT**. | "ANC mode: TRANSPARENT (updated …)"; you **hear** the surroundings | `08 12 … 80 …` → ACK `ff 01 …` / `08 13` | | |
| F2 | Tap **ADAPTIVE**. | mode ADAPTIVE | `… 40 …` | | |
| F3 | Tap **OFF**. | mode OFF; noise cancelling audibly off | `… 20 …` | | |
| F4 | Tap **ACTIVE**. | mode ACTIVE; noise cancelling audibly on | `… 08 …` | | |
| F5 | Change the mode with a **press-and-hold on a bud**, then tap **Refresh**. | The screen shows the bud's new mode | `08 11` → `08 13` | | |
| F6 | Tap two modes very quickly after each other. | The last one wins, no error or hang | two `Set`s, in order | | |
| F7 | Tap a mode and switch to another app immediately; come back after 5 s. | The mode was still applied; no stuck "claiming" state | the channel is released (`DISC`) | | |
| F8 | With the buds **in the case** (lid open, Connected): tap a mode. | Either applied, or a clear message ("The Buds refused the command (…)" / "didn't respond in time") — never a wrong mode shown as done | ACK / NAK `ff 02 …` | | |

## G. ANC Quick Settings tile

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| G1 | ANC tab: tap **Add ANC Quick Settings tile**. | A message: "added", "already in Quick Settings …" or "not added" | — | | |
| G2 | Open Quick Settings fully and find the **ANC** tile (swipe through the pages, or edit). | The tile "ANC" is there; its subtitle shows the current mode when connected, "Open the app" when not | — | | |
| G3 | Connected: tap the tile repeatedly. | Cycles ACTIVE → TRANSPARENT → ADAPTIVE → OFF → ACTIVE; audible each time; the ANC tab agrees | one `Set` per tap | | |
| G4 | **Not** connected: tap the tile. | The tile says "Open the app"; tapping opens the app; it does **not** connect by itself | nothing | | |

## H. Equalizer

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| H1 | EQ tab right after Connect. | The five sliders show the Buds' current EQ (compare with the official app if available); "EQ updated: HH:MM:SS" | `ReadSetting 4:16` + answer | | |
| H2 | Tap each preset: **Heavy bass, Light bass, Balanced, Vocal boost, Clarity**. | The sliders move to the preset; audible difference | one `WriteSetting` per tap, each answered `OK` | | |
| H3 | Drag **Upper treble** up and release. | The slider stays; audible | one `WriteSetting` (on release, not per pixel) | | |
| H4 | Drag each other band (Treble, Mid, Bass, Low bass) once. | as H3 | | | |
| H5 | Tap **Read EQ again**. | The sliders show the value just written | `ReadSetting` | | |
| H6 | Disconnect, Connect, open EQ. | The last written EQ is read back (it is stored in the Buds) | | | |

## I. Find My Buds

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| I1 | Buds out of your ears, Find tab: **Ring Left**. | "Ringing: Left earbud — tap Stop to end it."; the **Left** bud rings and keeps ringing | `04 01 00 01 02` → ACK | | |
| I2 | Tap **Stop**. | The notice disappears; the ringing stops | `04 01 00 01 00` → ACK | | |
| I3 | **Ring Right**, then **Stop**. | The **Right** bud rings, then stops | `… 01` / `… 00` | | |
| I4 | Ring Left, then **Disconnect** before Stop. | "A ring was started on the Left earbud — reconnect and tap Stop to end it." | | | |

## J. Notification / foreground service

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| J1 | Connect; pull down the notification shade. | One quiet notification "OpenControl for Pixel Buds" with the state (and ANC mode) | — | | |
| J2 | Change ANC; look again. | The notification text follows the mode | | | |
| J3 | Disconnect (or lose the session). | The notification disappears | | | |
| J4 | Connect, press Home, wait 2 minutes, reopen. | Still connected (or a clear message if the Buds closed it); no crash | | | |

## K. Robustness

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| K1 | Connected: switch **Bluetooth off** in Quick Settings. | "Bluetooth is disabled." with **Enable Bluetooth**; no crash | | | |
| K2 | Switch Bluetooth on again, **Connect**. | ready again | | | |
| K3 | Connected: walk out of range (another room) and back. | A clear "lost" message, then **Connect** works again | | | |
| K4 | Rotate the phone / change to dark mode while connected. | Nothing lost; no crash | | | |
| K5 | Leave the phone locked for the GrapheneOS Bluetooth auto-off time, unlock, open the app. | "Bluetooth is disabled." (normal), no crash | | | |

## L. Debug screen and export

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| L1 | Debug tab: switch **Debug mode** on. | "Unidentified frames (n)" list grows during use | — | | |
| L2 | Tap **Export debug log**, share it to yourself (e.g. Files). | The log opens; it has the connection lines with times; with Debug mode on also hex lines; **no** full Buds address | — | | |
| L3 | Debug mode off, export again. | No hex lines | — | | |

---

## After the run (within 1 minute of the last action)

| # | Collect | Done |
|---|---|---|
| X1 | Export the debug log (L2) | ☐ |
| X2 | Pull the HCI snoop log (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3) | ☐ |
| X3 | App logcat and the system log (GrapheneOS log viewer) | ☐ |
| X4 | Stop the film; note its first and last clock time | ☐ |
| X5 | Put everything, with this filled-in plan and the events file, in one folder for analysis (it becomes the next `CAP-NNN`) | ☐ |

## Summary

| Section | Tests | ✅ | ❌ | ⚠️ | not run |
|---|---|---|---|---|---|
| A Start / permissions / Bluetooth | 6 | | | | |
| B Pairing | 4 | | | | |
| C Connection | 10 | | | | |
| D Safe Mode / firmware | 2 | | | | |
| E Battery | 7 | | | | |
| F ANC | 8 | | | | |
| G ANC tile | 4 | | | | |
| H EQ | 6 | | | | |
| I Find My Buds | 4 | | | | |
| J Notification | 4 | | | | |
| K Robustness | 5 | | | | |
| L Debug | 3 | | | | |

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/APP_TESTPLAN.md - https://tedsluis.github.io/opencontrolpixelbudspro2/APP_TESTPLAN
