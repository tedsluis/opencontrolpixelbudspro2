# APP_TESTPLAN.md — Functional test plan for the OpenControl for Pixel Buds app

**Purpose:** one run through **every function of the app** on real hardware (Pixel 9a / GrapheneOS + Pixel Buds Pro 2), with a place to
record the result of each test. Written 2026-09-25 against the app as committed in `7498cbc` (`ai-sessions/0046`); **updated 2026-09-25 for the
`ai-sessions/0048` build** (automatic foreground re-open ADR-044: C1, C3, C8, C9, E3–E5, G4; ANC only while worn: F8, G3; loss wording: C8; per-bud
charging and last-seen Case: E2–E5; ring notice: I4); **updated 2026-09-26 for the `ai-sessions/0052` build** (the EQ tab is "Sound", a new
"Controls" tab, presets in two rows: H0; *Refresh battery* on a fresh claim: E8, E9; settings read and written: sections M and N); **updated 2026-09-28 for the `ai-sessions/0054` build** (a tap on a disabled ANC mode checks again: F8, G3; the loss wording after
returning to the app: C11; balance snaps to "Centre": M3; the previous connection's per-bud lines marked at Connect: E10; the Digital-assistant note: N3);
**updated 2026-09-28 for the `ai-sessions/0056` build** (the press-and-hold ANC-mode list, field 12: N1, N7–N9; the "In-ear detection" switch, field 2: N1, N10, N11;
a setting not read yet is disabled: N1; a tap during a re-open: C12); **updated 2026-09-29 for the `ai-sessions/0057` build** (Material 3 overhaul: top app
bar with a Debug action and five tabs, the (i) details dialogs, the graphical battery, pull to refresh / reconnect, the settings re-read, the app theme —
**section O**). **Since `0057` every time and state word the older rows expect "on screen" ("updated / read / changed HH:MM:SS", "last seen …",
"… last connection", "Not read from the Buds yet") is in that card's (i) dialog** — tap (i) to check it; the main surface shows the value, and a dot on the (i)
plus a dimmed value (battery) or a disabled control (settings) when it is not current. ~~Debug (L) is opened with the bug icon in the top bar, not a tab.~~
**Updated 2026-10-01 for the `ai-sessions/0062` build:** every ANC tap — the ANC tab's and the tile's — sends `08 11` first and the `08 12` only if that claim's
`Notify` allows it (F1–F8, G3, D2); the new not-allowed wording and the tile subtitle "Not allowed now" (F8, G3, O12); the **gear** in the top bar opens
**Settings** with the tabs Settings (dark mode), Debug (L) and Info (L1, O1, O3, O13, P1); **section Q** (the menu, Info, dark mode, the tile from the fresh
`Notify`, the cut-off watch, the Disconnect label's contrast). **Updated 2026-10-01 for the `ai-sessions/0064` build:** the tab survives a rotation or
Android's dark switch (K4, O13); the balance's `[‹]`/`[›]` steps (M3); a Bluetooth-off loss is named (K1); **section R** (Info's licence and links, the steps to
Right 4, the Bluetooth-off line, the profile proxies).
This is a *user-level*
functional test of this project's own app; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` is the separate catalogue of Buds/official-app behaviours, and the
"Expected on the wire" column below only names what to look for in the HCI log afterwards (`ai-sessions/0046` RESULT §9 has the exact frames).

**How to use:** do the tests in order (later tests assume the earlier state). Fill in **Result** (✅ / ❌ / ⚠️ / — not run) and **Notes** (the
time, what you saw). A ❌ needs the time and a screenshot or the film time — that is what makes it traceable afterwards.

---

## 0. Preparation (do this before test 1)

| # | Check | Done |
|---|---|---|
| P1 | Build and install the debug APK of the commit under test; **since `ai-sessions/0062`** read the build on the app's **Info** tab (gear → Info, "App: …, build <hash> (<date>)") on film — no hash to write down | ☐ |
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
| C1 | Buds paired, taken out of the case (or case open). Open the app (fresh start, no Disconnect tapped in this run). | "Connected to this phone (Android)", then **by itself** "App control: connecting…" → "ready" (ADR-044, `ai-sessions/0048`) | DLCI 0x02 opened by the app without a tap, only while the app is on screen | | |
| C2 | Tap **Connect**. | "App control: connecting…" → "App control: ready"; a **Firmware: release_5.203** line; **no** "Safe Mode — read-only" card; a notification "OpenControl for Pixel Buds" | DLCI 0x02 opened; the Buds announce their channel; the app reads the EQ and requests runtime info | | |
| C3 | Tap **Disconnect**. | "App control: not open yet …"; the notification disappears; it **stays** closed (no automatic re-open after a Disconnect tap, also not on resume or when Android reconnects) | the app closes DLCI 0x02; no app `SABM` on 0x02 afterwards until C4 | | |
| C4 | Tap **Connect** again. | ready again within a few seconds | as C2 | | |
| C5 | Tap **Connect** twice quickly. | one session, no error | one DLCI 0x02 open | | |
| C6 | Disconnect the Buds in **Android's** Bluetooth panel (tap the Buds' row). | "Android no longer shows the Buds connected …" with **Connect**; no crash | ACL disconnect | | |
| C7 | Reconnect in Android's panel, then **Connect** in the app. | ready again | | | |
| C8 | Put both buds in the case, lid open, then close the lid. | Within seconds the Buds close the session; the card names the cause: "The Buds closed the app's channel (…)" or "Android no longer shows the Buds connected …" — **never** "likely another app"; with the lid open the app may re-open by itself (a bud docked: charging in the case); after the lid is closed: "Paired — not connected to this phone"; no crash | Buds `DISC` 0x02 and/or ACL disconnect; at most one app `SABM` 0x02 per event | | |
| C9 | Take them out again (lid open), keep the app on screen; do **not** tap Connect. | ready again **by itself** once Android shows the Buds connected (one attempt; if it fails, a message and a Connect/Retry button) | one app `SABM` 0x02 per event (link back / Buds `DISC` + ≈ 1.5 s) | | |
| C10 | Swipe left/right between the tabs, and use the bottom bar. | Both change the tab; Back does not jump to Debug | — | | |
| C11 | Connected, buds out, lid open: press **Home**; put **both** buds into the case (the link drops while the app is away); wait 10 s; return to the app. | "Android no longer showed the Buds connected when you returned to the app — the connection ended while the app was in the background (…). Tap Connect to reconnect." — not the undetermined "The Maestro channel (equalizer) was closed" (`ai-sessions/0054` I-2) | ACL disconnect while the app is not visible; no app `SABM` 0x02 while away | | |
| C12 | Right after the Buds closed the app's channel (a bud out of an ear, app on screen), tap a setting on Controls within ~2 s, before "ready". | "The setting was not changed: The app's channel is being reopened — try again in a moment."; the switch unchanged; after "ready" nothing is sent by itself | no `WriteSetting` for that tap (nothing queued) | | |

## D. Safe Mode and firmware (after the `ai-sessions/0046` fix)

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| D1 | After C2/C4: look at the Connection tab. | "Firmware: release_5.203"; **no** Safe Mode card | the Buds' `GetSoftwareInfo` on DLCI 0x02 | | |
| D2 | Do any write (ANC tap, F1). | It is **sent** — no "Safe Mode: nothing was sent" message | `08 11` → `08 13 … e8 …`, then the ANC `Set` (`08 12 …`) on DLCI 0x04 | | |

## E. Battery

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| E1 | Buds in the case, lid open, app on screen (it connects by itself; else **Connect**). | Left and Right each "NN% (updated HH:MM:SS) — charging in the case (HH:MM:SS)" | DLCI 0x04: three `03 03 …` frames; DLCI 0x02 stream with 6.2/6.3 field 2 = 2 | | |
| E2 | Same moment: the **Case** line. | "Case: NN% (updated HH:MM:SS)" (compare with Android's Bluetooth panel / the Buds' LED) **or**, if never reported since the app started, "Not reported yet — the Buds send the Case level only while a bud is charging in the case." — never a made-up value | DLCI 0x02 `SubscribeRuntimeInfo` stream; **no app activity on DLCI 0x08** | | |
| E3 | Take the **Right** bud out (app on screen). Do **not** tap anything, then tap **Refresh battery**. | Before the tap already: Right "— not charging (out of the case) (HH:MM:SS)", Left "— charging in the case"; the session may be closed and re-opened by itself (≈ 1.5 s); after Refresh the % times update | a stream packet with 6.3 field 2 = 1; then the DLCI 0x04 claim, battery `e4 64 ff`-style | | |
| E4 | Take the **Left** bud out too. | Both "not charging"; Case "NN% — last seen HH:MM:SS (no bud charging in the case)" | a stream packet without entry 6.1 | | |
| E5 | Put both back (lid open). | Both "charging in the case", Case current again — by itself (automatic re-open if the Buds closed the session) | Buds `DISC` 0x02 → app `SABM` 0x02 ≈ 1.5 s later (unless the ACL dropped first) | | |
| E6 | Wait a few minutes with the app open (buds in the case, lid open). | The Case line updates by itself if the Buds send a new value — the app does **not** poll | only Buds-initiated stream packets | | |
| E7 | Check there is **no** dock sentence ("…seem to be in the case") anywhere. | No such sentence (removed in `ai-sessions/0046`) | — | | |
| E8 | Buds worn, nothing done for ≥ 10 s: **Refresh battery**, then **Refresh battery twice within 1 s**. | Each Refresh stamps new "(updated …)" times, or says "No new battery reading from the Buds — try again." — never an old time shown as new; below the button "The Buds report the Case level only while a bud is in the case." (`ai-sessions/0052`) | one `SABM` 0x04 per Refresh (a lingering claim is released first: `DISC` → `SABM`), each followed by `03 03 …`; one `SubscribeRuntimeInfo` on DLCI 0x02 per Refresh | | |
| E9 | Both buds in the case, lid open, idle 2 minutes, then **Refresh battery**. | The Case updates (the Buds answered the re-subscription) or keeps its old value and time — say which | app `SubscribeRuntimeInfo` → a `SERVER_STREAM` within 1 s, or none (ADR-043 Update, untested) | | |
| E10 | Both buds in the case (lines say "charging in the case"): **Disconnect**, take both out, **Connect**; watch the lines for 3 s. | First "Left: NN% — last seen HH:MM:SS — charging in the case (HH:MM:SS, last connection)" (the previous connection's values), then within ≈ 2 s the new "not charging (out of the case)" lines with new times (`ai-sessions/0054` I-4) | the new connection's stream packet and battery burst | | |

## F. Noise control (ANC screen)

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| F1 | Buds **in your ears**, Connected. ANC tab: tap **TRANSPARENT**. | "ANC mode: TRANSPARENT (updated …)"; you **hear** the surroundings | one claim: `08 11` → `08 13 … e8 …`, then `08 12 … 80 …` → ACK `ff 01 …` / `08 13` (`ai-sessions/0062` F-1: the `Get` first, every tap) | | |
| F2 | Tap **ADAPTIVE**. | mode ADAPTIVE | `… 40 …` | | |
| F3 | Tap **OFF**. | mode OFF; noise cancelling audibly off | `… 20 …` | | |
| F4 | Tap **ACTIVE**. | mode ACTIVE; noise cancelling audibly on | `… 08 …` | | |
| F5 | Change the mode with a **press-and-hold on a bud**, then tap **Refresh**. | The screen shows the bud's new mode | `08 11` → `08 13` | | |
| F6 | Tap two modes very quickly after each other. | The last one wins, no error or hang | two `Get` + `Set` pairs, in order | | |
| F7 | Tap a mode and switch to another app immediately; come back after 5 s. | The mode was still applied; no stuck "claiming" state | the channel is released (`DISC`) | | |
| F8 | With the buds **not in your ears** (in the case, or on the table), Connected: look at the ANC tab; tap a mode; then put the buds in your ears and tap a mode again. | "The Buds don't allow changing noise control right now (usually because no bud is in an ear). Tapping a mode checks again first." (the (i): "…in an ear; checked HH:MM:SS)…"); the mode buttons stay **enabled** (`ai-sessions/0054` I-1, `0062` F-2). Not worn: the tap changes nothing but the checked time, the line "The Buds don't allow changing noise control right now (usually because no bud is in an ear)."; worn: the tap switches the mode | not worn: one claim, `08 11` → `08 13 … 00 …`, **no** `08 12`; worn: one claim, `08 11` → `08 13 … e8 …` → `08 12` → ACK | | |

## G. ANC Quick Settings tile

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| G1 | ANC tab: tap **Add ANC Quick Settings tile**. | A message: "added", "already in Quick Settings …" or "not added" | — | | |
| G2 | Open Quick Settings fully and find the **ANC** tile (swipe through the pages, or edit). | The tile "ANC" is there; its subtitle shows the current mode when connected, "Open the app" when not | — | | |
| G3 | Connected, Buds **worn**: tap the tile repeatedly. Then take them out and tap once more. | Worn: cycles ACTIVE → TRANSPARENT → ADAPTIVE → OFF → ACTIVE from the mode **the Buds report in that tap's claim** (`ai-sessions/0062` F-1), audible; the ANC tab agrees. Not worn: subtitle "Not allowed now" (large tile); a tap checks first and toasts "The Buds don't allow changing noise control right now (usually because no bud is in an ear)." if they refuse; after putting them back in, a tap switches | per tap one claim: `08 11` → `08 13`, then `08 12` only if Settable ≠ `00` | | |
| G4 | **Not** connected: tap the tile. | The tile says "Open the app"; tapping opens the app; the app then connects by itself if Android shows the Buds connected (ADR-044) — the tile itself never connects | nothing from the tile | | |

## H. Equalizer (tab "Sound" since `ai-sessions/0052`)

Buds **in your ears** for H2–H4, and **say aloud** what you hear at each step (the film records it) — audibility has never been recorded so far
(`PROTOCOL.md` §4.2; `ai-sessions/0050` UX-01).

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| H0 | Look at the presets. | Two rows: `[HEAVY BASS] [LIGHT BASS] [BALANCED]` / `[VOCAL BOOST] [CLARITY]`, equal widths, no label cut off | — | | |
| H1 | Sound tab right after Connect. | The five sliders show the Buds' current EQ (compare with the official app if available); "EQ updated: HH:MM:SS" | `ReadSetting 4:16` + answer | | |
| H2 | Tap each preset: **Heavy bass, Light bass, Balanced, Vocal boost, Clarity**. | The sliders move to the preset; audible difference | one `WriteSetting` per tap, each answered `OK` | | |
| H3 | Drag **Upper treble** up and release. | The slider stays; audible | one `WriteSetting` (on release, not per pixel) | | |
| H4 | Drag each other band (Treble, Mid, Bass, Low bass) once. | as H3 | | | |
| H5 | Tap **Read EQ again**. | The sliders show the value just written | `ReadSetting` | | |
| H6 | Disconnect, Connect, open EQ. | The last written EQ is read back (it is stored in the Buds) | | | |

## M. Sound settings (tab "Sound", below the presets — `ai-sessions/0052`, DECISIONS.md ADR-045)

Buds **in your ears**, a stereo test file playing; say aloud what you hear. A value changes on screen only after the Buds accepted it ("changed
HH:MM:SS"); a refused or unanswered write shows "The setting was not changed: <reason>" and the old value stays.

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| M1 | Right after Connect: read Balance, Mono audio and Conversation detection. | Each shows the Buds' value with "read HH:MM:SS" (or "Not read from the Buds yet") | `ReadSetting 4:17`, `4:19`, `4:22` + answers | | |
| M2 | Drag **Balance** fully to **L**, release. | "Left 100 · changed …"; sound in the left ear | `WriteSetting 4:{17:200}` → empty `RESPONSE` | | |
| M3 | Balance fully to **R**; then about halfway left; then back near the centre and release. Then tap **`[›]`** four times from Centre (`ai-sessions/0064` F-2). | "Right 100", "Left NN", "Centre" — a release within ±3 of the middle snaps to "Centre" (`ai-sessions/0054` I-3); then Right 1, 2, 3, **Right 4** (a step is never snapped) | `4:{17:199}`, `4:{17:2·NN}`, `4:{17:0}`; then `4:{17:1}`, `4:{17:3}`, `4:{17:5}`, `4:{17:7}` — one write per tap | | |
| M4 | **Mono audio** on, then off. | switch follows after the Buds' OK; both ears play both channels while on | `4:{19:1}`, `4:{19:0}` | | |
| M5 | **Conversation detection** off, then on; with it on, speak for 5 s (ANC on). | switch follows; say what the Buds do while you speak | `4:{22:0}`, `4:{22:1}` | | |
| M6 | Disconnect, Connect, open Sound. | The last written values are read back ("read …") | the `ReadSetting` answers = the last writes | | |

## N. Controls (tab "Controls" — `ai-sessions/0052`, DECISIONS.md ADR-045)

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| N1 | Right after Connect: open Controls. | "Use touch controls" with its value and time; "Press and hold" Left / Right: Noise control or Digital assistant; while a bud is on Noise control "Modes for press and hold (both buds)" with four boxes (Noise cancellation / Off / Adaptive / Transparency) and "read HH:MM:SS"; the "In-ear detection" switch with "Pauses audio when you take a bud out and resumes it when you put it back.", the note "With it off, audio does not pause …" and its time. A switch/box whose value was not read is greyed | `ReadSetting 4:4`, `4:7`, `4:12`, `4:2` + answers | | |
| N2 | **Use touch controls** off; tap a bud (music playing). Then on; tap again. | Off: the tap does nothing; on: the tap pauses/plays | `4:{4:0}`, `4:{4:1}` | | |
| N3 | **Left: Digital assistant**, press-and-hold the Left bud; then **Left: Noise control**, hold again. | the chip follows after the Buds' OK; say what the phone/Buds do; under "Press and hold" the note "Digital assistant needs an assistant app on this phone that supports headphones (…). Without one, holding the bud may only play a tone." (`ai-sessions/0054` I-5) | `4:{7:{1:{4:{1:6}}}}`, `…{1:5}` | | |
| N4 | The same for **Right**. | as N3 | `4:{7:{2:{4:{1:6}}}}`, `…{1:5}` | | |
| N5 | Disconnect, Connect, open Controls. | the written values are read back | the `ReadSetting` answers | | |
| N6 | Safe Mode (only if a Safe Mode card shows): tap any setting. | "The setting was not changed: Safe Mode: nothing was sent …" | nothing sent | | |
| N7 | Untick **Adaptive** in the mode list; then long-press a bud three times (say what you hear). | Adaptive unticked after the Buds' OK ("changed …"); the presses cycle Noise cancellation → Off → Transparency only | `WriteSetting 4:{12:{1:1 2:1 3:1 4:0}}` → empty `RESPONSE` | | |
| N8 | Untick modes until two are left; try to untick one more. | the last two boxes greyed, "At least two modes must stay selected."; nothing changes | one write per allowed untick; **nothing** for the refused one | | |
| N9 | Set both buds to **Digital assistant**; then one back to **Noise control**. Tick the modes back afterwards. | the mode list disappears, then reappears | only the `4:{7:…}` writes, then the `4:{12:…}` ones | | |
| N10 | **In-ear detection** off; with music playing take a bud out and put it back. | switch off after the OK; the music does **not** pause | `WriteSetting 4:{2:0}` → OK | | |
| N11 | **In-ear detection** on; take a bud out and put it back. | switch on after the OK; the music pauses and resumes | `4:{2:1}` → OK | | |

## I. Find My Buds

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| I1 | Buds out of your ears, Find tab: **Ring Left**. | "Ringing: Left earbud — tap Stop to end it."; the **Left** bud rings and keeps ringing | `04 01 00 01 02` → ACK | | |
| I2 | Tap **Stop**. | The notice disappears; the ringing stops | `04 01 00 01 00` → ACK | | |
| I3 | **Ring Right**, then **Stop**. | The **Right** bud rings, then stops | `… 01` / `… 00` | | |
| I4 | Ring Left, then **Disconnect** before Stop; then **Connect** and **Stop**. | After Disconnect: "A ring was started on the Left earbud — reconnect and tap Stop to end it."; after Connect: "… before the app reconnected — it may still be ringing. Tap Stop to end it."; after Stop: the notice disappears and the ring stops | `04 01 00 01 02` → ACK; later `04 01 00 01 00` → ACK | | |

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
| K1 | Connected: switch **Bluetooth off** in Quick Settings. | "Bluetooth is disabled." with **Enable Bluetooth**; no crash; the debug export says "Session loss cause: Bluetooth was switched off on this phone" — no "(provisional …)" (`ai-sessions/0064` F-3) | | | |
| K2 | Switch Bluetooth on again, **Connect**. | ready again | | | |
| K3 | Connected: walk out of range (another room) and back. | A clear "lost" message, then **Connect** works again | | | |
| K4 | Rotate the phone / change to dark mode while connected (Android's switch, and the app's own Settings tab, Q2) — on each of the five tabs and on Settings → Info. | Nothing lost; no crash; **the same tab stays** (`CAP-066` reset Sound → Connection; fixed in `ai-sessions/0064` F-1) | | | |
| K5 | Leave the phone locked for the GrapheneOS Bluetooth auto-off time, unlock, open the app. | "Bluetooth is disabled." (normal), no crash | | | |

## L. Debug screen and export (the **Debug tab** of the settings menu since `ai-sessions/0062`)

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| L1 | Gear (top bar) → **Debug** tab: switch **Debug mode** on. *(Bug icon `0057`–`0061`; the gear and the menu since `ai-sessions/0062`.)* | "Unidentified frames (n)" list grows during use | — | | |
| L2 | Tap **Export debug log**, pick a folder and file name in Android's "save as" dialog. | "Debug log saved (N lines)."; the file holds the **whole** log (not cut at 64 KiB — `CAP-063`), connection lines with times; with Debug mode on also hex lines; **no** full Buds address | — | | |
| L3 | Debug mode off, export again. | No hex lines | — | | |

## O. Material 3 overhaul (`ai-sessions/0057`)

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| O1 | Start the app; look at the top and the bottom. | Top bar "OpenControl" with a **gear** on the right (since `ai-sessions/0062`; a bug icon before); bottom bar with **five** tabs Connection, ANC, Sound, Controls, Find (own icons); no Debug tab | — | | |
| O2 | Swipe left through all tabs and back; tap each tab. | The selected tab follows swipe and tap | — | | |
| O3 | Tap the gear; then the ← arrow; again the gear, then **system back**. | The settings menu fills the screen (title "Settings", ←, tabs Settings / Debug / Info, no bottom bar); ← and back return to the tab you came from | — | | |
| O4 | Ready, both buds in the case: Connection tab. | Status card in the theme's accent colour with a check icon and "App control: ready"; Battery card with three columns Left \| Case \| Right, icon, %, a bar, a bolt on a charging bud | — | | |
| O5 | Tap the Battery card's (i). | A dialog with the explanation, the full Left/Right/Case lines **with the same times as the debug log**, the Case note and "Firmware: …"; "Close" and back close it | — | | |
| O6 | Take both buds out (E4's state). | Case column dimmed, a dot on the Battery (i); TalkBack (if used) reads "Battery: Details — not current"; the (i) says "last seen HH:MM:SS" | stream packet without 6.1 | | |
| O7 | Make a part unavailable (e.g. right after the app starts, before any report). | "Battery unavailable" in that column and **no** bar | — | | |
| O8 | Pull down on **Connection**, **Find**, **ANC**, **Sound**, **Controls** (Ready), one at a time, ≥ 5 s apart; write down each time. | A spinner while it runs, gone when done; Connection/Find: new battery times in the (i) or the "No new battery reading" line; ANC: the (i) time moves; Sound/Controls: the "read HH:MM:SS" times in the (i) move | Connection/Find: one DLCI 0x04 claim with `03 03` (+ one `SubscribeRuntimeInfo`); ANC: one claim `08 11` → `08 13`; Sound: `ReadSetting 4:16`, then **exactly** `4:2, 4:4, 4:7, 4:12, 4:17, 4:19, 4:22` in that order; Controls: exactly those seven reads, **nothing else** | | |
| O9 | **Disconnect**; pull down on any tab. Then Bluetooth off and pull; then Bluetooth on. | After Disconnect: the app connects (as the Connect button); with Bluetooth off: Android's own "enable Bluetooth" prompt — never a new dialog, never nothing | `SABM` DLCI 0x02 after the first pull | | |
| O10 | While "App control: connecting…", pull. | The spinner ends at once; nothing else happens | nothing extra | | |
| O11 | Sound: drag an EQ band and the balance while the write is refused (e.g. in Safe Mode, or right after a Disconnect before the tap). | While the finger is down the knob follows it; after release it returns to the Buds' value and stays there; the error line says why | no write, or a write with no OK | | |
| O12 | ANC tab. | Four large buttons (2 × 2); the Buds' mode is the filled one with a check; with no bud worn the sentence "The Buds don't allow changing noise control right now (usually because no bud is in an ear). Tapping a mode checks again first." (its time is in the (i), `ai-sessions/0062`) | — | | |
| O13 | Android Settings → Display → Dark theme on, then off; change the wallpaper colours. | The app follows dark/light and the wallpaper colours; all text readable in both | — | | |

---

## P. The `ai-sessions/0059` fixes (in `CAP-065` section I as BA-1…4)

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| P1 | Buds in the case, app on the Sound tab; take them out and wear them; watch until the EQ shows "read HH:MM:SS". | Until then the five EQ sliders are **disabled** and the notice says the sliders are off until the EQ arrives; the presets can be tapped. After the read the sliders enable at the Buds' values (A58-APP-02) | `ReadSetting 4:16` answered | | |
| P2 | Within 3 s of "App control: ready", pull down Quick Settings; read the ANC tile. | A mode or "Tap to switch" — never "Open the app" while the app says ready (A58-APP-01) | — | | |
| P3 | Debug mode on (L1); use the app until "Unidentified frames (n)" shows n > 0; rotate the phone (auto-rotate on) or switch dark theme. | The list keeps its entries (it used to empty — A58-APP-08) | — | | If n stays 0, write "not testable" |
| P4 | Only during a pairing (section B): after picking the Buds in Android's dialog, rotate the phone before "Paired" shows. | Pairing still ends with "Paired" (or its own failure text), not stuck (A58-APP-08) | `Pairing:` lines continue in the debug log | | |
| P5 | Bluetooth on; force-stop the app and start it; watch the first second. | "Bluetooth is disabled" does **not** flash before the normal screen (A58-APP-08) | — | | Film at normal speed |
| P6 | *(watch only)* After the run, search the debug export for "answered with a value this app cannot read" and "Late WriteSetting answer dropped". | — | If either exists: note the time; the analysis looks up the frame (A58-APP-03/04) | | |

## Q. The `ai-sessions/0062` build (settings menu, Info, dark mode, ANC `Get` first, cut-off)

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| Q1 | Ready: gear → **Info**. | "App: 0.1.0-dev, build <short hash>[-dirty] (<commit date>)" (= `git log -1 --format='%h %cs'` of the build); "Firmware (from the Buds' announcement, HH:MM:SS):", "Case: release_5.203", "Left bud: release_5.203", "Right bud: release_5.203", "Control channel: 19" (or 21); after Disconnect: "Not connected yet — the Buds report their firmware when the app connects." | nothing from the app while the menu is open | | |
| Q2 | Gear → **Settings**: Dark mode **On**, then **Off**, then **System**; with System, switch Android's own dark theme on and off. | On: dark at once; Off: light at once; System follows Android — no restart, no crash; the choice is kept after the app is closed and reopened | — | | |
| Q3 | Q2 **On** (dark), Connection tab, session ready. | The card's **Disconnect** is as legible as the card's other text (the `CAP-065` dark frame measured ≈ 1.2:1 before the fix) | — | | |
| Q4 | Both worn, mode Adaptive; Quick Settings → ANC tile once (large tile). | Off (the next after the Buds' **reported** mode, Adaptive) — even if the app showed another mode before | one claim: `08 11` → `08 13 … e8 40` → `08 12 … 20` → ACK | | |
| Q5 | *(watch only)* An ANC tap or Refresh while Play services takes the channel back. | "The answer was cut off — another app took the Buds' channel. Tap Refresh to see the current mode."; after a cut-off change the mode buttons dimmed + the (i) dot ("Not confirmed: …"), cleared by the next Refresh; never "The Buds didn't respond in time." for it | the app's request, then a phone `DISC` before the Buds' answer | | |
| Q6 | Gear → **Debug** tab. | The Debug screen as before (Debug-mode switch, Export debug log, Unidentified frames) | — | | |

## R. The `ai-sessions/0064` build (tab on rotation, balance steps, Bluetooth-off loss, profile proxies, Info links)

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| R1 | Gear → **Info**. | Under "Licence": "GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)", **Read the licence**, **Licence on GitHub**; under "Project": **README on GitHub**, **Report an issue on GitHub**, "Links open in your browser; this app itself has no internet access."; then "The Buds" | nothing from the app | | |
| R2 | **Read the licence**; scroll; **Close**. | A dialog "Licence" with the full text ("GNU AFFERO GENERAL PUBLIC LICENSE / Version 3, 19 November 2007 …"), also with no network; Close closes it | — | | |
| R3 | Tap each of the three links (back to the app after each). | The browser opens `…/blob/main/LICENSE`, `…/blob/main/README.md`, `…/issues` on github.com/tedsluis/opencontrolpixelbudspro2. With no browser installed: "No app on this phone can open web links. The address is …" | the system log shows the browser's `VIEW` start; the app holds no `INTERNET` permission (A6) | | |
| R4 | Each tab (and Settings → Info): rotate to landscape and back. | The same tab stays selected and shown each time (`CAP-066` K4r) | — | | |
| R5 | Sound: balance at Centre; tap **`[›]`** four times, then **`[‹]`** four times. At Left 100 / Right 100 look at the button toward that end. | Right 1 … Right 4, then back to Centre — each after the Buds' OK; the button toward an end is greyed there; before the balance was read both are greyed | per tap one `WriteSetting 4:{17:n}` → OK; `17:7` = Right 4 | | |
| R6 | App on screen: Bluetooth off; wait 10 s; Bluetooth on. | "Bluetooth is disabled.", then ready again by itself | export: "Bluetooth adapter: ON -> TURNING_OFF", "… -> OFF", "Session loss cause: Bluetooth was switched off on this phone" (final), later "… -> TURNING_ON", "… -> ON" and the automatic re-open | | |
| R7 | *(debug build)* After R6, leave the app and come back; read logcat `StrictMode`. | — | note every `LeakedClosableViolation` with its object; 🟡 a `BluetoothLeAudio` one may remain (framework `CloseGuard`, `CAP-066-FINDINGS.md` §8) — the app closes every proxy it obtained (`ai-sessions/0064` F-4) | | |

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
| C Connection | 12 | | | | |
| D Safe Mode / firmware | 2 | | | | |
| E Battery | 10 | | | | |
| F ANC | 8 | | | | |
| G ANC tile | 4 | | | | |
| H EQ | 7 | | | | |
| I Find My Buds | 4 | | | | |
| J Notification | 4 | | | | |
| K Robustness | 5 | | | | |
| L Debug | 3 | | | | |
| M Sound settings | 6 | | | | |
| N Controls | 11 | | | | |
| O Material 3 overhaul | 13 | | | | |
| P 0059 fixes | 6 | | | | |
| Q 0062 build | 6 | | | | |
| R 0064 build | 7 | | | | |

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/APP_TESTPLAN.md - https://tedsluis.github.io/opencontrolpixelbudspro2/APP_TESTPLAN
