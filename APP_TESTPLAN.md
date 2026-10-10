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
Android's dark switch (K4, O13); a Bluetooth-off loss is named (K1); **section R** (Info's licence and links, the Bluetooth-off line, the profile proxies).
**Updated 2026-10-02 for `ai-sessions/0066`:** the balance steps are gone again (M3, R5 back to the slider) and Info has no "Licence on GitHub" link (R1, R3).
**Updated 2026-10-06 for the `ai-sessions/0074` build (1.1.0):** five new switches — Multipoint, Use head gestures, Case sounds "Earbuds replaced" and "Other
alerts" on Controls, Volume EQ on Sound (**section T**); a switch that was not read shows "—" in place of the switch (M1, N1); twelve settings reads at Connect
and on a pull (O8); A5 corrected (`CAP-067` §7). **Updated 2026-10-08 for the 1.1.1 build** (`ai-sessions/0079`: the app of 1.1.0 rebuilt with the
toolchain of `ai-sessions/0078` — no new function; **section U**, a regression pass over what the new toolchain could change). **Updated 2026-10-09 for the
1.2.0 build** (`ai-sessions/0082`, `DECISIONS.md` ADR-058/ADR-059): "Changed by the Buds" in the ANC (i); the serial numbers on gear → Info; the "probably worn"
line on the battery card; the **Case sounds** switches moved from Controls to gear → **Settings** (T5, T6 note; N1); **Conversation detection** moved from Sound to
**Controls** (M5 → N12; M1); thirteen MAESTRO requests at Connect (one `GetHardwareInfo` after the subscription; O8); **section V**. **Updated 2026-10-10 for
the rebuilt 1.2.0** (`ai-sessions/0084`, `DECISIONS.md` ADR-061): the Message Stream claim is held while the noise-control tab is on screen (F-rows: a tap there
reuses the held channel; V10 can pass); the balance slider shows the finger's value while dragging and is finer near the centre — the ±3 snap of M3 is gone;
**section W**.
This is a *user-level*
functional test of this project's own app; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` is the separate catalogue of Buds/official-app behaviours, and the
"Expected on the wire" column below only names what to look for in the HCI log afterwards (`ai-sessions/0046` RESULT §9 has the exact frames).

**How to use:** do the tests in order (later tests assume the earlier state). Fill in **Result** (✅ / ❌ / ⚠️ / — not run) and **Notes** (the
time, what you saw). A ❌ needs the time and a screenshot or the film time — that is what makes it traceable afterwards.

**Tiers (since 2026-10-10, `ai-sessions/0084`; the maintainer's choice in chat 2026-10-10, `ai-sessions/0083` "Both tiers + helper + card"):** a release run
executes only the rows its skeleton names — the minimal tier of `RELEASING.md` §11a plus the extended steps of the new behaviour; the full plan is for a new
phone or a large change.

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
| A5 | *(optional)* Deny *Nearby devices* **in the app's own prompt** (A3) once, then reopen the app. *(Corrected 2026-10-06, `CAP-067` §7: revoking it later in Settings → Apps ends the app's process — Android's behaviour — so this screen follows only a denial in the prompt.)* | "Bluetooth permission needed" / "You denied the permission." with **Allow**; after "don't ask again": **Open app settings** | — | | |
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
| C8 | Put both buds in the case, lid open, then close the lid. | Within seconds the Buds close the session; the card names the cause: "The app's channel was closed while Android still shows the Buds connected — the Buds do this when a bud goes in or out of the case or an ear. …" (wording since 1.0.1) or "Android no longer shows the Buds connected …" — **never** "likely another app"; with the lid open the app may re-open by itself (a bud docked: charging in the case); after the lid is closed: "Paired — not connected to this phone"; no crash | Buds `DISC` 0x02 and/or ACL disconnect; at most one app `SABM` 0x02 per event | | |
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
| E3 | Take the **Right** bud out (app on screen). Do **not** tap anything, then tap **Refresh battery**. | Before the tap already: Right "— not charging (HH:MM:SS)" (1.0.1: no longer "(out of the case)"), Left "— charging in the case"; the session may be closed and re-opened by itself (≈ 1.5 s); after Refresh the % times update | a stream packet with 6.3 field 2 = 1; then the DLCI 0x04 claim, battery `e4 64 ff`-style | | |
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
| H0 | Look at the presets. | Two rows: `[HEAVY BASS] [LIGHT BASS] [BALANCED]` / `[VOCAL BOOST] [CLARITY] [FLAT]` (Flat since 1.0.1), equal widths, no label cut off | — | | |
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
| M1 | Right after Connect: read Balance and Mono audio (until 1.1.1 also Conversation detection — on Controls since 1.2.0, N12). | Each shows the Buds' value with "read HH:MM:SS" (or "Not read from the Buds yet"); since 1.1.0 an unread switch shows "—" in place of the switch | `ReadSetting 4:17`, `4:19` (+ `4:22` for N12) + answers | | |
| M2 | Drag **Balance** fully to **L**, release. | "Left 100 · changed …"; sound in the left ear | `WriteSetting 4:{17:200}` → empty `RESPONSE` | | |
| M3 | Balance fully to **R**; then about halfway left; then back near the centre and release. | "Right 100", "Left NN", "Centre" — since the rebuilt 1.2.0 (`ai-sessions/0084`) the label shows the finger's value while dragging and the middle third holds −10 … +10 in steps of 1, so "Centre" is reached by releasing at it (the ±3 snap of `ai-sessions/0054` I-3 is gone); Left/Right 4 stays 4 | `4:{17:199}`, `4:{17:2·NN}`, `4:{17:0}` | | |
| M4 | **Mono audio** on, then off. | switch follows after the Buds' OK; both ears play both channels while on | `4:{19:1}`, `4:{19:0}` | | |
| M5 | ~~**Conversation detection** off, then on~~ — **moved to Controls in 1.2.0: see N12** (the row stays for the runs before 1.2.0). | — | — | | |
| M6 | Disconnect, Connect, open Sound. | The last written values are read back ("read …") | the `ReadSetting` answers = the last writes | | |

## N. Controls (tab "Controls" — `ai-sessions/0052`, DECISIONS.md ADR-045)

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| N1 | Right after Connect: open Controls. | "Use touch controls" with its value and time; "Press and hold" Left / Right: Noise control or Digital assistant; while a bud is on Noise control "Modes for press and hold (both buds)" with four boxes (Noise cancellation / Off / Adaptive / Transparency) and "read HH:MM:SS"; the "In-ear detection" switch with "Pauses audio when you take a bud out and resumes it when you put it back.", the note "With it off, audio does not pause …" and its time. A box whose value was not read is greyed; since 1.1.0 a switch that was not read shows "—" in place of the switch; **since 1.2.0** a "Conversation detection" card after In-ear detection (subtitle "Switch from noise cancellation to transparency when you talk") and **no** Case sounds card (gear → Settings, V) | `ReadSetting 4:4`, `4:7`, `4:12`, `4:2`, `4:22` + answers | | |
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
| N12 | **Conversation detection** off, then on; with it on, speak for 5 s (ANC on). (On Sound as M5 until 1.1.1; on Controls since 1.2.0.) | switch follows after each OK; say what the Buds do while you speak | `4:{22:0}` = `CAP-019` 1720, `4:{22:1}` = 1808 (channel 21) | | |

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
| O8 | Pull down on **Connection**, **Find**, **ANC**, **Sound**, **Controls** (Ready), one at a time, ≥ 5 s apart; write down each time. | A spinner while it runs, gone when done; Connection/Find: new battery times in the (i) or the "No new battery reading" line; ANC: the (i) time moves; Sound/Controls: the "read HH:MM:SS" times in the (i) move | Connection/Find: one DLCI 0x04 claim with `03 03` (+ one `SubscribeRuntimeInfo`); ANC: one claim `08 11` → `08 13`; Sound: `ReadSetting 4:16`, then **exactly** `4:2, 4:4, 4:7, 4:11, 4:12, 4:15, 4:17, 4:19, 4:22, 4:27, 4:28, 4:29` in that order (twelve since 1.1.0; seven before); Controls: exactly those twelve reads, **nothing else** | | |
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
| Q1 | Ready: gear → **Info**. | "App: <version>, build <short hash>[-dirty] (<commit date>)" — 1.0.1 for the hotfix build (= `git log -1 --format='%h %cs'` of the build); "Firmware (from the Buds' announcement, HH:MM:SS):", "Case: release_5.203", "Left bud: release_5.203", "Right bud: release_5.203", "Control channel: 19" (or 21); after Disconnect: "Not connected yet — the Buds report their firmware when the app connects." | nothing from the app while the menu is open | | |
| Q2 | Gear → **Settings**: Dark mode **On**, then **Off**, then **System**; with System, switch Android's own dark theme on and off. | On: dark at once; Off: light at once; System follows Android — no restart, no crash; the choice is kept after the app is closed and reopened | — | | |
| Q3 | Q2 **On** (dark), Connection tab, session ready. | The card's **Disconnect** is as legible as the card's other text (the `CAP-065` dark frame measured ≈ 1.2:1 before the fix) | — | | |
| Q4 | Both worn, mode Adaptive; Quick Settings → ANC tile once (large tile). | Off (the next after the Buds' **reported** mode, Adaptive) — even if the app showed another mode before | one claim: `08 11` → `08 13 … e8 40` → `08 12 … 20` → ACK | | |
| Q5 | *(watch only)* An ANC tap or Refresh while Play services takes the channel back. | "The channel was closed before the Buds' answer arrived (possibly by another app using it). Tap Refresh to see the current mode." (wording since 1.0.1); after a cut-off change the mode buttons dimmed + the (i) dot ("Not confirmed: …"), cleared by the next Refresh; never "The Buds didn't respond in time." for it | the app's request, then a phone `DISC` before the Buds' answer | | |
| Q6 | Gear → **Debug** tab. | The Debug screen as before (Debug-mode switch, Export debug log, Unidentified frames) | — | | |

## R. The `ai-sessions/0064` build (tab on rotation, balance steps, Bluetooth-off loss, profile proxies, Info links)

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| R1 | Gear → **Info**. | Under "Licence": "GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)", **Read the licence** (no "Licence on GitHub" since `ai-sessions/0066`); under "Project": **README on GitHub**, **Report an issue on GitHub**, "Links open in your browser; this app itself has no internet access."; then "The Buds" | nothing from the app | | |
| R2 | **Read the licence**; scroll; **Close**. | A dialog "Licence" with the full text ("GNU AFFERO GENERAL PUBLIC LICENSE / Version 3, 19 November 2007 …"), also with no network; Close closes it | — | | |
| R3 | Tap each of the two links (back to the app after each). | The browser opens `…/blob/main/README.md`, `…/issues` on github.com/tedsluis/opencontrolpixelbudspro2. With no browser installed: "No app on this phone can open web links. The address is …" | the system log shows the browser's `VIEW` start; the app holds no `INTERNET` permission (A6) | | |
| R4 | Each tab (and Settings → Info): rotate to landscape and back. | The same tab stays selected and shown each time (`CAP-066` K4r) | — | | |
| R5 | Sound: drag the balance toward **R**, release; try to land on **Right 4**. *(The `0064` steps were removed in `ai-sessions/0066`.)* | the Buds' value after their OK; say how many drags Right 4 took | one `WriteSetting 4:{17:n}` per release, none during the drag; `17:7` = Right 4 | | |
| R6 | App on screen: Bluetooth off; wait 10 s; Bluetooth on. | "Bluetooth is disabled.", then ready again by itself | export: "Bluetooth adapter: ON -> TURNING_OFF", "… -> OFF", "Session loss cause: Bluetooth was switched off on this phone" (final), later "… -> TURNING_ON", "… -> ON" and the automatic re-open | | |
| R7 | *(debug build)* After R6, leave the app and come back; read logcat `StrictMode`. | — | note every `LeakedClosableViolation` with its object; 🟡 a `BluetoothLeAudio` one may remain (framework `CloseGuard`, `CAP-066-FINDINGS.md` §8) — the app closes every proxy it obtained (`ai-sessions/0064` F-4) | | |

## S. The 1.0.1 build (`ai-sessions/0069`: values from the last connection, device choice, wording, Flat, "—")

In `CAP-068` as BD-13 … BD-19 and BD-9/BD-29.

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| S1 | Ready, buds worn, an ANC mode shown. Tap **Disconnect**. Open the ANC tab. | The last mode is still shown, **dimmed**, the card's (i) has the dot; in the (i): "ANC mode: <MODE> — from the last connection (updated HH:MM:SS)". The Quick Settings tile says "Open the app" | the session closes; nothing else is sent | | |
| S2 | After S1: Sound and Controls tabs. | Sliders, switches and chips keep their last values, **dimmed** and disabled; each card's (i) has the dot and starts with "From the last connection — the app is not connected to the Buds now." | — | | |
| S3 | **Connect**; watch the ANC tab for 3 s. | Until this connection's first answer the old mode stays dimmed with "— from the last connection"; then the mode is shown as current (no dot). Sound/Controls show "not read" until their reads answer (as before) | `08 11` → Notify; the `ReadSetting` sweep | | |
| S4 | Both buds in the case: Disconnect, take both out, Connect; the battery card's (i) within 3 s. | "Case: NN% — last seen HH:MM:SS (last connection)" — not "(no bud charging in the case)" — until the Buds report the Case again | — | | |
| S5 | Sound → Equalizer: tap **FLAT**. | All five sliders at 0.0 after the Buds' OK | `WriteSetting 4:{16:{0.0 × 5}}` → OK (the bytes of `CAP-015` frame 2111 on the announced channel) | | |
| S6 | Right after a Connect, before the EQ and the balance are read (or with the reads failing). | Each EQ band shows "—" instead of a number; Balance shows "—" instead of "Centre"; sliders disabled | — | | Film the first second after "ready" |
| S7 | Gear → **Settings**: the block "Use different Buds". Tap the button. | Text: "Forgets which Buds this app controls (the Bluetooth pairing in Android stays). You then pick the Buds again with Pair a device." After the tap: the session closes; the Connection tab shows **Pair a device** | Disconnect only; export: "Pairing: use different Buds — removed N association(s) of this app…" | | The Buds stay paired in Android's Bluetooth settings |
| S8 | After S7: **Pair a device**; pick the Buds in Android's dialog. | Android's picker opens (no silent reuse); after the choice the app is paired with the chosen Buds | CDM association; no new bonding if already bonded | | |
| S9 | *(only with two Pixel Buds devices paired in Android and none chosen in the app, e.g. after S7)* Open the Connection tab. | "More than one Pixel Buds device is paired with this phone. Tap Pair a device to choose the one to control." + **Pair a device** — the app connects to neither by itself | — | | "not testable" with one pair |
| S10 | Disconnect; both buds in the **closed** case (wait until Android shows them not connected); tap **Connect**. | Within about 15 s: "Couldn't open the app's channel to the Buds. Possible causes: the Buds are out of reach or in the closed case. Open the case and try again." — **no** mention of another app or Play services | a page attempt ending in a page timeout; no RFCOMM | | The published known issue of 1.0.0 |
| S11 | Quick Settings: tap the ANC tile while a change cannot be made (buds not worn; or Safe Mode). | A toast with the reason for **every** failure (not worn: "The Buds don't allow changing noise control right now …"; a timeout: "The Buds didn't respond in time.") | `08 11` → Notify; no `Set` when not allowed | | 1.0.0 was silent except for two errors |
| S12 | Start an export (Debug → Export debug log) and **rotate the phone while Android's "save as" dialog is open**; then save. | "Debug log saved (N lines)." — the file has content | — | | 1.0.0 lost the text on rotation |
| S13 | Gear → **Info**. | "App: 1.0.1, build <hash> (<date>)", no "-dirty" | — | | |

## T. The 1.1.0 build (`ai-sessions/0074`: five switches, "—" for an unread switch, the screen-reader text)

In `CAP-070` as BF-1 … BF-25 (`captures/CAP-070-…-Group_BF/CAP-070-EVENT-NOTES.md`, which has the exact bytes per channel). Each switch changes only after the
Buds' empty `RESPONSE` OK; a refused or unanswered write shows "The setting was not changed: <reason>" and the old value stays. No note or subtitle under the
new switches (the maintainer's choice, "alleen labels").

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| T1 | Right after the update over 1.0.1: open the app, let it connect; open **Controls** within the first second (screen recording). | The unread switches show "—" for about a second, then the Buds' values; no crash | `ReadSetting 4:16`, then twelve `ReadSetting` (2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29), each answered | ⚠️ partly | CAP-070: twelve reads in 22/22 sessions; no crash; a first install in user 10, not an update; "—" not caught at the first Connect (camera, no screen recording) |
| T2 | Read the Controls tab and Sound's Equalizer card. | Controls: Touch controls · Press and hold · **Head gestures** · In-ear detection · **Multipoint** · **Case sounds** (Earbuds replaced, Other alerts); Sound: **Volume EQ** below the presets; each (i) "<Label>: read HH:MM:SS" | — | ✅ | CAP-070 06:12:58–06:14:46 (order and (i) lines as specified; Equalizer (i) not opened) |
| T3 | **Multipoint** off, then on. | Each after the Buds' OK, "Multipoint: changed HH:MM:SS" | `4:{11:0}`, `4:{11:1}` → empty `RESPONSE` | ✅ | Whether a second device stays connected is not claimed; CAP-070 A752/A1989 (ch 21 off, ch 19 on), all ACKed |
| T4 | **Use head gestures** off, then on. | Each after the OK; no dialog | `4:{29:1}` (off), `4:{29:2}` (on) → `RESPONSE`; nothing on DLCI 0x08 | ✅ | CAP-070 A762/A1998; nothing on DLCI 0x08 |
| T5 | **Earbuds replaced** off; put a bud back into the case; then on, and again. Say whether the case sounded each time. (On Controls until 1.1.1; **on gear → Settings since 1.2.0**, V13.) | Each after the OK | `4:{28:0}`, `4:{28:1}` | ⚠️ partly | An observation, not a claim of the app; writes ✅ (A770, A1992); the case-sound observation not recorded (no audio) |
| T6 | **Other alerts** off, then on. (On gear → Settings since 1.2.0, V13.) | Each after the OK | `4:{27:0}`, `4:{27:1}` | ✅ | CAP-070 A777, A1995 |
| T7 | Sound: **Volume EQ** off, then on; listen at low volume and say what you hear. | Each after the OK; Equalizer (i) "Volume EQ: changed …" | `4:{15:0}`, `4:{15:1}` | ⚠️ partly | Audibility is an observation; writes ✅ (A785, A1953, A3072, A3075); audibility not recorded (no audio) |
| T8 | With T3–T7 left off: Disconnect, Connect. | All five read back as off, "read HH:MM:SS" | the twelve reads; the answers equal the last writes | ✅ | CAP-070 A824–A904: `11:0`, `15:0`, `27:0`, `28:0`, `29:1` |
| T9 | Only the **Left** bud out of the case (Info: "Control channel: 19"): Multipoint and Use head gestures off, then on. | As T3/T4 | the channel-19 forms — first on the wire in `CAP-070` (BF-14/15) | ✅ | CAP-070 A3668/A3679, A3614/A3685 — the derived frames, ACKed |
| T10 | Only the **Right** bud out (Info: "Control channel: 21"): Volume EQ off, then on. | As T7 | `4:{15:1}` on channel 21 — first on the wire in `CAP-070` (BF-20) | ✅ | CAP-070 A4770/A4800 — `15:1` on ch 21 = the derived frame, ACKed |
| T11 | Bluetooth off; force-stop the app; open it; Controls, then Sound. Check what a screen reader gets for each "—" (`adb shell uiautomator dump`, or a screen reader if one is installed). | Controls: six "—" in place of the switches; Sound: the EQ bands, Volume EQ, balance, mono audio, conversation detection "—"; each "—" has the description **"Not read from the Buds yet"** | — | ⚠️ partly | "—" on film (Controls 06:38:04, Sound 06:39:04); no `uiautomator` dump — the description is not checked |

## U. The 1.1.1 build (the toolchain upgrade of `ai-sessions/0078`)

In `CAP-071` as BG-1 … BG-24 (`captures/CAP-071-2026-10-08_17-51-29_18-27-28-Group_BG/CAP-071-EVENT-NOTES.md`, which has the reference bytes per request). Nothing
here is new behaviour: each row checks that the rebuilt app does what 1.1.0 did. The robustness rows (C8, C9, J4, K1, K2, R6) and the open items (C12, S6, S12,
T11, H5) are the earlier IDs, run again in `CAP-071`.

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| U1 | In 1.1.0: Settings → Dark mode **On**, Debug → Debug mode **on**; install 1.1.1 **over** it (no uninstall); open the app. | Dark at once; Debug mode still on; no crash | — | ✅ | `dumpsys`: `lastUpdateTime` later than `firstInstallTime`; CAP-071: dark at once (film 17:53:30), Debug mode on at the first view (17:57:51, no tap; set in 1.1.0 off film), no crash; data kept (`ceDataInode` equal), `lastUpdateTime=2026-10-08 17:53:27` > user 10's `firstInstallTime` 17:40:13 (1.1.0 was added to user 10 13 min before) |
| U2 | Gear → **Info**. | "App: 1.1.1, build <hash> (<date>)", no "-dirty" | — | ✅ | CAP-071: "App: 1.1.1, build 86a6fb3 (2026-10-08)" (film 17:58:00–30); the installed APK's SHA-256 = `dist/1.1.1` |
| U3 | Right after the start: **Controls** within the first second (screen recording). | "—" for ≈ 1 s, then the values (S6) | the twelve reads, each = `CAP-070`'s request | ⚠️ | CAP-071: the twelve reads in 11/11 sessions, byte-identical; "—" not caught in the first second (no screen recording; the value was there 0.2 s after ready); "—" seen while not connected and during re-opens |
| U4 | Tap each bottom tab; swipe through all and back. | The same screens as 1.1.0 (`CAP-070`'s film); the bar follows the swipe | — | ✅ | CAP-071 17:58:53–17:59:59 |
| U5 | Pull down on each tab. | A spinner that ends; the (i) times move | as O8 | ✅ | CAP-071 18:00:06–18:00:34 (Find, Controls, Sound, ANC, Connection) |
| U6 | Open several (i) dialogs; close with Close and with back. | They open and close | — | ✅ | CAP-071 18:00:47–18:02:07 (nine dialogs) |
| U7 | Gear: the tab row; ← and system back. | Settings · Debug · Info; the selected label in the accent colour with a **full-width** underline, as 1.1.0 (`TabRow` kept with its deprecation suppressed); back returns to the tab you came from | — | ✅ | CAP-071 18:02:33–18:03:13 (full-width underline) |
| U8 | Rotate on Sound and on Info; Android's dark theme on/off with Dark mode = System. | The tab stays; the theme follows without a restart | — | ✅ | CAP-071 18:03:30–18:05:09 |
| U9 | Info → **Read the licence**; both links. | The full licence; the browser opens README and issues | nothing from the app | ✅ | CAP-071 18:05:27–18:06:08 |
| U10 | ANC tab: the four modes; the tile once; the notification. | Each mode after its ACK; the tile steps from the reported mode; the notification shows the mode | per tap `08 11` then the `Set` = `CAP-068`'s request | ✅ | CAP-071: 4 modes from the tab + 5 tile taps, 9 `Set` → 9 ACK during playback, `08 11` first each time; notification "Connected — ANC: …" |
| U11 | Sound: preset **Balanced**; a band, *Read EQ again*; balance Right 4 and Centre; mono; conversation detection; Volume EQ — say what you hear. | Each after its OK | each request = its reference (`CAP-059` 2188 for Balanced on 21) | ⚠️ | CAP-071: Balanced = `CAP-059` 2188, Upper treble +6.0, Centre, mono, conversation, Volume EQ — all byte-identical and OK; *Read EQ again* (H5) and "Right 4" not done; nothing said |
| U12 | Controls: touch controls, press and hold, the mode list without Off, in-ear detection, head gestures, Multipoint, both case sounds — say whether the case sounds with "Earbuds replaced" off. | Each after its OK | each request = its reference (`CAP-041` 2198 for the mode list on 21) | ⚠️ | CAP-071: every request byte-identical and OK (incl. the mode list = `CAP-041` 2198); the case sound not said; no chime on the audio either way |
| U13 | Disconnect, Connect. | Every value of U11–U12 read back | the reads answer the last writes | ✅ | CAP-071 18:18:40–43: every value read back |
| U14 | Find: Ring Left, Stop, Ring Right, Stop. | The ring follows each tap | `04 01 00 01 02` / `00` / `01` / `00` → ACK | ✅ | CAP-071 18:19:24–18:20:13: ACKed; the ring on the audio track |

## V. The 1.2.0 build (`ai-sessions/0082`: "Changed by the Buds", the serial numbers, the worn line, the two moves)

In `CAP-072` as BH-1 … BH-26 (`captures/CAP-072-2026-10-09_17-27-26_18-23-39-Group_BH/CAP-072-EVENT-NOTES.md`, which has the reference bytes per request and
the wear sequence with the head in view). One new request per Connect (`GetHardwareInfo`, ADR-058); the worn line (ADR-059) and the cause line are derived
from frames the app already received; the moved switches send the same bytes as before. The robustness rows (C8, C9, J4, K1, K2, R6) and the open items
(C12, S6, S12, T11, H5) are the earlier IDs, run again. Serial numbers are device identifiers: **redact them** in every note (first 4 + last 2 characters).

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| V1 | In 1.1.1: Settings → Dark mode **On**, Debug → Debug mode **on**; `dumpsys package … \| grep -E "firstInstallTime\|lastUpdateTime"`; install 1.2.0 **over** it; the same `dumpsys`; open the app (lid open). | Dark at once; Debug mode on; no crash; `firstInstallTime` unchanged, `lastUpdateTime` later | the twelve reads, `SubscribeRuntimeInfo`, then **one** `GetHardwareInfo` = `CAP-036` 1415 (21) / `CAP-024` 801 (19), answered | | BH-1 |
| V2 | Gear → **Info**: the build line, the firmware, **Serial numbers**. | "App: 1.2.0, build <hash> (<date>)"; "Serial numbers (from the Buds, HH:MM:SS):" then "Case: …", "Right bud: …", "Left bud: …" and the note "Labelled by position …" | nothing new (the answer of V1) | | BH-2; the three strings = the answer's field 7 in order |
| V3 | Gear → **Settings**: the Case sounds card between Dark mode and Use different Buds; its (i). | Both switches with their values; the (i) "Earbuds replaced: read HH:MM:SS", "Other alerts: read …", "These settings live on the case and are read when the app connects." | — | | BH-2 |
| V4 | Both buds in the case, lid open: the battery card's **Worn** line and its (i). | "Both buds in the case"; the (i) explanation ("Worn: from the Buds' last noise-control report. 'Probably worn' = … 'Not worn' = … With in-ear detection off the report says nothing about wearing.") | the runtime-info charging flags already received | | BH-3 |
| V5 | Only the Left in the ear; pull on Connection. | "Probably worn (checked HH:MM:SS)" | `08 11` → `Notify` Settable `e8` | | BH-5 (`INEAR-006`) |
| V6 | Both in; pull. Then the Left out with both worn on 19 (head in view); pull. | "Probably worn" with a later time; the session re-opens by itself | `Notify` `e8`; lead L-1: Buds `DISC` + announcement 21 | | BH-6, BH-7 (`INEAR-005`) |
| V7 | Both buds on the table, in-ear detection on; pull; then an ANC tap. | "Not worn (checked …)"; the tap refused with the not-allowed text | `Notify` Settable `00`; no `Set` | | BH-8 |
| V8 | In-ear detection **off**; pull. Then **on**; pull. | "Worn: unknown — in-ear detection is off"; then "Not worn (checked …)" | `4:{2:0}` → OK; `Notify` (`e8` expected, says nothing); `4:{2:1}` → OK; `Notify` `00` | | BH-9 |
| V9 | Both in the ears; pull; the four ANC modes from the tab; the (i) after each. | "Probably worn"; each mode after its ACK; **no** "Changed by the Buds" line in the (i) | per tap `08 11` → `Notify` → `Set` → ACK | | BH-10 |
| V10 | **Press and hold a bud** (its noise-control cycle) with the ANC tab open; the (i). Twice. | The mode changes without a tap; the (i) shows "Changed by the Buds at HH:MM:SS (a press-and-hold on a bud, or the Buds' own change)."; the time moves the second time | an unprovoked `08 13 00 04 01 e8 e8 xx` — no `08 11` before it; nothing sent by the app | | BH-11 |
| V11 | Then a tap in the app; a pull on the ANC tab; the tile — the (i) after each. | The line is gone after each ("set …" / "read …") | each: a claim `08 11` → `Notify` (→ `Set` → ACK for the tap and the tile) | | BH-12 |
| V12 | Both buds straight from the ears into the case and out onto the table; pull at ≈ 5, 15, 30, 45, 60 s. | "Probably worn" for at most ≈ 30 s (`CAP-064`), then "Not worn" — or "Both buds in the case" first; say what you see | `Notify` `e8` → `00`; the charging flags 2 → 0 | | BH-13; a result, not a fault, if "Probably worn" lasts longer — report it |
| V13 | Gear → Settings → **Case sounds**: Earbuds replaced off (a bud into the case and out; say whether the case sounded), on (the same); Other alerts off, on; the (i). | Each after its OK; "changed HH:MM:SS" | `4:{28:0}`, `4:{28:1}`, `4:{27:0}`, `4:{27:1}` = `CAP-070`/`CAP-071`'s requests | | BH-14 (T5, T6) |
| V14 | Controls → **Conversation detection** off, on; speak 5 s. | Each after its OK; the subtitle kept | `4:{22:0}` = `CAP-019` 1720, `4:{22:1}` = 1808 | | BH-15 (N12) |
| V15 | Sound: Balanced, a slider, *Read EQ again*, Right 4, Centre, mono, Volume EQ; Controls: touch, press and hold, the mode list without Off, head gestures, Multipoint — say what you hear. | Each after its OK; no conversation-detection row on Sound | each request = its reference (`CAP-072-EVENT-NOTES.md`'s table) | | BH-16, BH-17 (H5 ★, the "Right 4" write ★) |
| V16 | Disconnect, Connect; Sound, Controls, Settings, Info. | Every value read back; the **same three serials** with a new time; the Worn line "—" then "Probably worn" | the thirteen requests; one `GetHardwareInfo`, the same answer bytes | | BH-18 |
| V17 | Find: Ring Left, Stop, Ring Right, Stop. | The ring follows each tap | `04 01 00 01 02` / `00` / `01` / `00` → ACK | | BH-19 |
| V18 | The case (lid open), Home 2 min, Export + Bluetooth off/on; grep every export for a serial (first 4 + last 2) and the MAC. | The 1.0.1 cause texts; re-open by itself; "Bluetooth is disabled."; **0 hits** in the exports (positive control: the "Control channel" line) | ADR-044's `SABM` ≈ 1.5 s after the link; the thirteen requests after each re-open | | BH-20…BH-22 (C8, C9, J4, K1) |
| V19 | Film 2: C12 (the early tap), S12 (the export across a rotation), T11 (three `uiautomator` dumps: Controls, Sound, **Settings**). | "The setting was not changed: …"; "Debug log saved (N lines)."; every "—" node's `content-desc` = `Not read from the Buds yet` (Controls seven, Sound eight, Settings two) | no `WriteSetting` after the early tap | | BH-23…BH-26 |

## W. The rebuilt 1.2.0 (`ai-sessions/0084`: the Message Stream held on the noise-control tab — ADR-061 — and the finer balance slider)

In `CAP-073` as BI-1 … BI-20 (`captures/CAP-073-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BI/CAP-073-EVENT-NOTES.md`, which has the reference bytes per request). Nothing
new on the wire: ADR-061 changes only **when** the existing claim (`SABM` DLCI 0x04, `08 11`, the phone's `DISC`) ends. The open items of `CAP-072` (T11, C12,
S12, H5, T7, V8) are the earlier IDs, run again.

| ID | Steps | Expected on screen | Expected on the wire | Result | Notes |
|---|---|---|---|---|---|
| W1 | Update over the `ec6d163` 1.2.0 (Dark mode and Debug mode set); open the app; Info. | Dark at once; Debug mode on; "App: 1.2.0, build <B1 hash>", no "-dirty"; firmware ×3; the serials | the thirteen requests, one `GetHardwareInfo` = `CAP-072` A 7593 (21) / A 8616 (19) | | BI-1, BI-2 |
| W2 | Disconnect, Connect; Bluetooth off/on; Export. | ready; "Bluetooth is disabled.", then ready by itself; "Debug log saved (N lines)." | the thirteen requests after each open | | BI-3 … BI-5 |
| W3 | (the fixture-independent check) The requests of W1/W2 against `CAP-072`'s frames. | — | byte-identical to `CAP-072` (the unit tests' fixtures since `ai-sessions/0084` are `CAP-072`'s own frames) | | analysis |
| W4 | Both buds worn: open the **ANC** tab, wait 30 s. | the mode; the (i) ends "While this tab is open the app keeps the Buds' noise-control channel open, …" | one `SABM` DLCI 0x04 + `08 11`; **no `DISC`** for 30 s | | BI-6 (ADR-061) |
| W5 | **Press and hold** the Left bud, then the Right bud; the (i) after each. | the mode changes without a tap; "Changed by the Buds at HH:MM:SS (a press-and-hold on a bud, or the Buds' own change)."; the time moves | an unprovoked `08 13 00 04 01 e8 e8 xx` per hold, nothing from the app | | BI-7, BI-8 (`TOUCH-007`) |
| W6 | On the ANC tab: a mode tap; then the tile from Quick Settings. | each mode after its ACK; no "Changed by the Buds" line | `08 11` → `Notify` → `Set` → ACK on the held channel — **no second `SABM`**, no `DISC` | | BI-9, BI-10 |
| W7 | Leave the ANC tab (Controls); back; Home; return. | — | the phone's `DISC` ≈ 1.5 s after leaving the tab and after Home; a new `SABM` + `08 11` on entering and on return | | BI-11, BI-12 |
| W8 | With the ANC tab shown: both buds into the case, then out into the ears. | the session ends with its cause, then ready by itself | ADR-044's re-open and its snapshot claim **kept** (no `DISC` while the tab is shown) | | BI-13 |
| W9 | T11: Bluetooth off, `am force-stop`, three `uiautomator` dumps (Controls, Sound, gear → Settings). | every "—" has `content-desc` "Not read from the Buds yet" (Controls 7, Sound 8, Settings 2) | — | | BI-14 |
| W10 | C12: a Multipoint tap during the re-open after the Right bud went into the case; S12: Export with a rotation during the save dialog; H5: *Read EQ again*. | "The app's channel is being reopened …", nothing written; "Debug log saved (N lines)."; the EQ time moves | no `4:{11:…}`; one `ReadSetting 4:16` | | BI-15 … BI-17 |
| W11 | Volume EQ off/on (say what you hear); in-ear detection off → pull → on → pull. | each after its OK; "Worn: unknown — in-ear detection is off", then "Probably worn (checked …)"; the battery (i) says "… 40 seconds or more …", no capture id | `4:{15:0}`/`{15:1}`; `4:{2:0}`/`{2:1}` = the references | | BI-18, BI-19 |
| W12 | Balance: drag slowly to **"Right 4 — release to set"**, release; then to "Centre". | the label follows the finger with "— release to set"; after release the Buds' value; one write per release | `4:{17:7}` (`CAP-070` 3747 on 19, `CAP-064` 6671 on 21), then `4:{17:0}`; nothing during a drag | | BI-20 (`AUDIO-003`) |

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
| S 1.0.1 build | 13 | | | | |
| T 1.1.0 build | 11 | 7 | 0 | 4 | 0 |
| U 1.1.1 build | 14 | 11 | 0 | 3 | 0 |
| V 1.2.0 build | 19 | 13 | 1 | 4 | 1 |
| W 1.2.0 rebuilt | 12 | | | | |

**Run `CAP-072` (2026-10-09, 1.2.0 `ec6d163`, the user without Play; `ai-sessions/0083`, `CAP-072-FINDINGS.md`):** section V — ✅ V1–V7, V11, V13, V14, V16,
V17, V18; ❌ **V10** — the press-and-hold change is never shown: the app holds no Message Stream claim between actions (ADR-032), so the step **cannot pass as
written**; verdict "fix first", ADR-061 (hold the claim while the noise-control tab is on screen); ⚠️ V8 (in-ear off/on written, the Worn line not read), V9 (two
taps, not the four modes), V12 ("Probably worn" at 42 s on the table — the documented limit), V15 (Volume EQ, *Read EQ again* and "Right 4" not done); not run
V19 (film 2 moved to the rebuild's run; S12 done differently: the rotation before the save dialog). Other steps — ✅ S6 on the screen recording (17:31:28), U1–U3
(the update kept Dark mode and Debug mode), C8/C9 (×4), K1/K2, J4.

**Run `CAP-070` (2026-10-07, 1.1.0 `0323849`, the user without Play; `ai-sessions/0075`, `CAP-070-FINDINGS.md`):** section T — ✅ T2, T3, T4, T6, T8, T9, T10;
⚠️ T1 (no "—" caught at the first Connect; a first install, not an update), T5 and T7 (writes ✅, observations not recorded — no audio), T11 (no dump). Other
steps — ✅ C10 (the swipe, camera only), O8 (twelve reads), M1/N1 ("—" on film); not run H5 (*Read EQ again* not tapped; the EQ read back after Bluetooth
on), C12 (the tap came after "ready"), S6 on a screen recording, S12 (no rotation).

**Run `CAP-068` (2026-10-04, 1.0.1 `e1fc886`, the user without Play; `ai-sessions/0070`, `CAP-068-FINDINGS.md`):** section S — ✅ S1 (partly: the tile "Open the app",
the mode dimmed; the (i) text not opened), S2 (partly, same), S4, S5, S7, S8, S10, S11, S13; not identifiable S3, S6; not run S9 (one pair), S12 (no rotation).
Never-run steps — ✅ C5 (one `SABM`; a second tap not identifiable on film), F5 (two Refresh taps; the press-and-hold not on film), J4 (2 min 12 s), I4 (the
"after Disconnect" text appeared after a Buds-side loss, not after the tap), L3 (no hex line after the switch, E4), K1 (F-3: "Bluetooth was switched off on
this phone", final); ⚠️ H5 done differently (*Read EQ again* not tapped; the value was read back at the next Connect).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/APP_TESTPLAN.md - https://tedsluis.github.io/opencontrolpixelbudspro2/APP_TESTPLAN
