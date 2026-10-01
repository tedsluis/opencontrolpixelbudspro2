# Event Notes: OpenControl for Pixel Buds on Pixel 9a (GrapheneOS) — Group BB, the `CAP-064` leftovers (`CAP-066`)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0060`, 2026-10-01; extended by `0061`; **adapted to the `ai-sessions/0062` build**,
the maintainer's outline in chat 2026-10-01, "CAP-066": *"Apply as outlined (Recommended)"*). After the run: rename this folder from the placeholder
`CAP-066-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BB` to the film's first/last overlay times and analyse it as `CAP-064` was (`ai-sessions/0060`).

**Purpose (the maintainer's choice in chat 2026-10-01, `ai-sessions/0060` checkpoint "To CAP-065" — `CAP-065` had already been run, so the leftovers go here):**

- **I — the real I-1 path (the disabled-tap re-check of `ai-sessions/0054`).** In `CAP-064` it was never entered: the app's last `Notify` read `e8`, so taps and
  the tile sent a `Set` directly (ACKed with the buds on the table, frame 2698; NAKed by the tile, 3440). This section first makes the app *see* `00`, then taps.
- **II — when does the Settable byte read `00`?** `CAP-064-FINDINGS.md` §3: with in-ear detection on it read `e8` for ≈ 28 s with both buds on the table
  straight from the case (2299–2759), and `00` after buds were taken out of the ears (3299, 3443). 🟡 hypothesis: `00` follows a removal from the ear or a dock,
  not "not worn" as such.
- **III — the ANC modes never tapped in OpenControl** (`ANC-002` Noise cancellation, `ANC-004` Transparency) and the I-4 "last connection" words in the battery (i)
  dialog within 2 s of a Connect (`CAP-064-FINDINGS.md` §2 I-4a).
- **IV — restore the balance** to its value before `CAP-064` (Right 4 = `17:7`; `CAP-064` ended at Centre `17:0`; `CAP-065` read `17:0` throughout).
- **V — lead L-1, the hosting bud** (added by `ai-sessions/0061`, maintainer-approved in chat 2026-10-01, "CAP-066": *"Apply as proposed (Recommended)"*).
  `CAP-065` (`CAP-065-FINDINGS.md` §4): one bud out ⇒ Left 19 / Right 21 (7/7, 🟢); once the channel changed 21 → 19 inside one ACL after a Buds-side `DISC`
  with both buds worn (🟡 "the channel names the bud that hosts the link"). This section takes the hosting bud out while the other stays worn.
- **VI — the ANC tile's subtitle (redo of `CAP-065` BA-2/BA-3).** In `CAP-065` the tile was in Android's compact form (icon only) — no subtitle on any frame —
  and the app was swiped away, not force-stopped, so A58-APP-01 (a fresh process before the first ANC report) was not exercised.
- **VII — the `CAP-065` robustness steps that were not done** (K4 rotation, K1/K2, K3, L3, K5, then destructive A5, (E), B4, Z1) — the maintainer's choice
  in chat 2026-10-01 ("Leftovers": *"Append to CAP-066, destructive last (Recommended)"*).
- **VIII — the `ai-sessions/0062` build** (added by `0062`): every ANC tap — the ANC tab's and the tile's — sends `08 11` first and the `08 12` only if that
  claim's `Notify` reads Settable non-zero (F-1); the new wording "The Buds don't allow changing noise control right now (usually because no bud is in an ear).
  Tapping a mode checks again first." and the tile subtitle "Not allowed now" (F-2); an answer cut off by the claim's close is "The answer was cut off — another
  app took the Buds' channel. Tap Refresh to see the current mode." with the mode dimmed and the (i) dot (F-3, watch only); a **gear** in place of the bug icon
  opens **Settings** with the tabs Settings (dark mode System / On / Off), Debug (unchanged) and Info (app version and build, firmware of Case / Left bud / Right
  bud, control channel) (F-4/F-5/F-6); the Connection card's Disconnect label in the card's own text colour (F-7).

**Changed by `ai-sessions/0061` (the same chat):** BB-6 and BB-7 dropped (`CAP-065` answered both: after wearing ⇒ `00`, e.g. frames 3641/7091; one docked
and one loose ⇒ `00`, frame 5707); BB-8 shrunk to the in-app Transparency and Off taps (Noise cancellation was tapped in the app at `CAP-065` F7, frame 10790;
all four modes went through the tile, 2567…3045).

**Facts the maintainer gave (chat 2026-10-01):** the maintainer does not speak on the films (no spoken hash or state); Play services' *Nearby devices* permission
is **allowed** (used for casting to a Chromecast). The build is identified from the logs; if the next FEATURE session adds the app build number to an Info
screen (its plan), film that screen once at the start. **It does since `ai-sessions/0062`:** gear → Info (A.0, P7).

---

## A.0. Which phone, which app, which build

| Item | Value |
|---|---|
| Phone | **Pixel 9a, GrapheneOS** |
| App under test | **OpenControl for Pixel Buds**, debug APK of the latest commit under `android/` (the `ai-sessions/0062` build or later) — the build is read from the **Info tab on film** (P7: "App: 0.1.0-dev, build <hash>[-dirty] (<commit date>)"), no hash to write down |
| Official Pixel Buds app | **Not used.** Pixel 7a: Bluetooth off |
| Play services *Nearby devices* | allowed (maintainer, 2026-10-01) |
| Buds | Pixel Buds Pro 2, firmware `release_5.203` |

## A.1. Preparation

| # | Check | Done |
|---|---|---|
| P1 | Debug APK installed **without** clearing the app's data | ☐ |
| P2 | Bluetooth HCI snoop log on; switch Bluetooth off and on **on film** | ☐ |
| P3 | Camera films the phone, the case and **your head** (ears visible for every wear step); the head-orientation rule of `CAP-064` applies (head on the right of the frame = Left bud) | ☐ |
| P4 | Do Not Disturb on; open the notification shade only if a step asks | ☐ |
| P5 | Music you know, playable offline | ☐ |
| P6 | Keep the status bar on film across a minute change at the start and at the end (the clock offset) | ☐ |
| P7 | After the first "ready": top bar **gear** → **Info** tab — hold it on film 3 s (app version and build, "Firmware (from the Buds' announcement, HH:MM:SS)", Case / Left bud / Right bud, "Control channel: N"); back | ☐ |

**Rhythm:** one action, then wait 5–10 s (longer where a step says so). Something unexpected: stop, wait 10 s, continue.

## A.2. Steps

DLCI numbers are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05 — whichever side opened the RFCOMM multiplexer).

### I. The real I-1 path (ANC tab and tile)

| Step | Pre-state | Action | Expected on screen | Expected on the wire |
|---|---|---|---|---|
| BB-1 | ready, both buds **worn** (on film), ANC tab | Take **both** buds out of the ears, lay them on the table **in view**; wait 20 s; tap **Refresh** | "The Buds don't allow changing noise control right now (usually because no bud is in an ear). Tapping a mode checks again first." appears; buttons stay enabled | `08 11` → `08 13 00 04 01 e8 00 …` (Settable `00`) |
| BB-2 | as BB-1, note shown | Tap **ADAPTIVE** (buds still on the table) | no mode change; the line "The Buds don't allow changing noise control right now (usually because no bud is in an ear)."; the (i)'s checked time moves | **one** claim: `08 11` → `08 13 … 00 …` → **no** `08 12` |
| BB-3 | as BB-2 | Quick Settings → **ANC tile** once (subtitle "Not allowed now" if the tile is large, BB-13) | no mode change; toast "The Buds don't allow changing noise control right now (usually because no bud is in an ear)." | one claim: `08 11` → `08 13 … 00 …` → no `08 12` |
| BB-4 | as BB-3 | Put both buds in the ears **on film**; wait 10 s; tap **ADAPTIVE** | Adaptive | one claim: `08 11` → `08 13 … e8 …` → `08 12 … 40` → ACK |
| BB-4t | both worn, mode Adaptive (BB-4), the tile large | Quick Settings → **ANC tile** once | the tile shows the mode **after the one the Buds just reported** (Adaptive → Off in the tile's cycle Noise cancellation → Transparent → Adaptive → Off) | one claim: `08 11` → `08 13 … e8 <mode>` → `08 12` with the next mode after **that** `<mode>` → ACK |

**Refuted if** (section I and every other ANC tap of the run, `ai-sessions/0062` F-1): an `08 12` without an `08 11` earlier **in the same claim**, or an `08 12`
after that claim's `Notify` read Settable `00`, or a tap that claims twice.

### II. When does Settable read `00`? (ears and buds in view; in-ear detection **on**)

| Step | Pre-state | Action | Expected | Expected on the wire |
|---|---|---|---|---|
| BB-5 | both buds in the case, lid open | Take both buds out and lay them **straight on the table** (do not wear them). Tap ANC **Refresh** at ≈ 10 s, 30 s, 60 s, 90 s, 120 s | say nothing; just tap | one `08 11` → `08 13 … <Settable> …` per Refresh — the result is the Settable value over time |
| ~~BB-6~~ | — | *dropped by `ai-sessions/0061`*: answered by `CAP-065` (after wearing ⇒ `00`, frames 3641, 7091, 5465) | — | — |
| ~~BB-7~~ | — | *dropped by `ai-sessions/0061`*: answered by `CAP-065` BA-6 (one docked, one loose ⇒ `00`, frame 5707) | — | — |

### III. ANC modes and the I-4 words

| Step | Pre-state | Action | Expected on screen | Expected on the wire |
|---|---|---|---|---|
| BB-8 | both worn, ANC tab | Tap **Transparency** (in the app, not the tile), wait 5 s, tap **Off** | each mode after the Buds' answer | per tap one claim: `08 11` → `08 13 … e8 …` → `08 12 … 80` → ACK; then `08 11` → `08 13 … e8 …` → `08 12 … 20` → ACK [`ANC-004`, `ANC-001`] (Noise cancellation: `CAP-065` F7) |
| BB-9 | ready, both buds in the case, lid open, Connection tab | **Disconnect**; take both buds out onto the table; **Connect**, and **immediately** tap the battery card's (i) | the (i) dialog shows the previous connection's lines marked "last connection" until the new report arrives (≈ 2 s), then "not charging (out of the case)" | app `SABM` MAESTRO; claim with `03 03 00 03 64 64 ff` |

### V. Lead L-1 — the hosting bud (`PROTOCOL.md` §2.2a 2026-10-01 🟡)

Read the channel from the debug export line "Maestro channel announced by the Buds: N" after each step.

| Step | Pre-state | Action | Expected | Expected on the wire |
|---|---|---|---|---|
| BB-12 | ready, both worn; the export's last announcement is **19** | Take the **Left** bud out of the ear onto the table (on film); wait 20 s | the app re-opens by itself if the Buds close the channel | 🟡 predicts: a Buds `DISC` of MAESTRO with the ACL up, then an announcement **21** (refuted if the session stays on 19 or comes back on 19) |
| BB-12m | ready, both worn, announcement **21** (e.g. after BB-12 and the Left back in) | Take the **Right** out onto the table; wait 20 s | as BB-12 | 🟡 predicts a Buds `DISC`, then **19** |

### VI. The ANC tile's subtitle (redo of `CAP-065` BA-2/BA-3, A58-APP-01)

| Step | Pre-state | Action | Expected on screen | Expected on the wire |
|---|---|---|---|---|
| BB-13 | before the run | Quick Settings → edit (pencil) → make the **ANC tile large** (two columns), so its subtitle shows | — | — |
| BB-14 | ready, both worn | Settings → Apps → OpenControl → **Force stop**; reopen; as soon as "ready" shows, pull down Quick Settings (within 3 s); read the tile | a mode or "Tap to switch", **never "Open the app"** while the app shows ready | app `SABM` MAESTRO; the snapshot claim's `Notify` may come later |

### VIII. The settings menu, dark mode, the cut-off watch (`ai-sessions/0062`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire |
|---|---|---|---|---|
| BB-16 | ready, on the **Sound** tab | Tap the **gear**; tap **Debug**, then **Info**, then **Settings**; then the top bar's back arrow | the title "Settings", three tabs; Debug = the Debug screen as before (Debug-mode switch, Export debug log, Unidentified frames); back returns to **Sound** | nothing from the app (opening the menu sends nothing) |
| K4d | ready, Android in **light** mode | Gear → Settings → **Dark mode: On**; back to the Connection tab (film the card ≈ 3 s, the **Disconnect** label close up); gear → **Off**; gear → **System**; then Android's own dark theme on and off in Quick Settings | On = dark at once, Off = light at once, System follows Android's switch; no restart, no crash; Disconnect legible on the card in the dark scheme (the `CAP-065` 11:23:50 frame measured ≈ 1.2:1) | — |
| BB-15 | *(watch only — cannot be provoked)* any ANC tap or Refresh while Play services re-opens its claim | — | if a claim is closed after `08 12`/`08 11` and before the answer: "The answer was cut off — another app took the Buds' channel. Tap Refresh to see the current mode."; after a cut-off `08 12` the mode buttons dimmed and the (i) dot, its first line "Not confirmed: the answer to the change at HH:MM:SS was cut off — the Buds may have switched. Tap Refresh."; **never** "The Buds didn't respond in time." for it; cleared by the next `Notify` | the app's `08 12`/`08 11`, then a phone `DISC` on the Message Stream DLCI before the Buds' ACK/`Notify` (as `CAP-065` 2640 → 2649 → 2651) |

### IV. Restore

| Step | Action | Done |
|---|---|---|
| BB-10 | Sound tab: drag the balance to **Right 4** (release) | `WriteSetting 4:{17:7}` → OK ☐ |
| BB-11 | *(after section VII)* Status bar across a minute change; stop the film | ☐ |

### VII. Robustness steps not done in `CAP-065` (from its section IV) — destructive steps last, after BB-10

| Step | Pre-state | Action | Expected on screen | Expected on the wire |
|---|---|---|---|---|
| K4r | ready | Rotate the phone to landscape and back | nothing lost, no crash | — |
| K1 | ready | Quick Settings: **Bluetooth off** | "Bluetooth is disabled." + Enable Bluetooth; no crash | — (the HCI log may stop) |
| K2 | after K1 | Bluetooth on; Connect if it does not connect by itself | ready | app `SABM` MAESTRO |
| K3 | ready, both worn | Walk out of range (another room, buds in the ears) for 30 s; come back | a clear loss text, then ready again | ACL `Disconnection Complete` reason `0x08` (timeout), not `0x13`; a re-open |
| L3 | — | Gear → **Debug** tab → **Debug mode off** → Export debug log (a second file) | no hex lines in it | — |
| K5 | — | *(optional)* GrapheneOS Bluetooth auto-off to the shortest; lock; wait; unlock; open the app; restore the setting | "Bluetooth is disabled." (normal), no crash | system log `BluetoothAutoOff` |
| A5 | — | *(destructive)* Settings → Apps → OpenControl → Permissions → *Nearby devices*: **Don't allow**; open the app; allow again | "Bluetooth permission needed" / "You denied the permission." + Allow | — |
| (E) | — | *(destructive)* Forget the Buds in Android's Bluetooth settings; open the app | "No Pixel Buds Pro 2 paired yet." + Pair a device | bond removal |
| B4 | after (E) | Open the lid; tap **Pair a device twice quickly** | one picker, no crash | — |
| Z1 | after B4 | Pair in the picker; Connect | ready | CDM association, bonding (SSP or CTKD, `PROTOCOL.md` §5.1) |

## A.3. After the run

Gear (Settings) → **Debug** tab → **Export debug log**; `adb bugreport`; the raw `btsnoop_hci.log`; app logcat and system log (GrapheneOS log viewer); the film.
All into this folder, then `sha256sum *`.

## A.4. Analysis checklist

- [ ] Pre-filter by the Buds' classic handle; DLCIs by content.
- [ ] P7: the Info tab frame — build hash against `git log`, firmware lines and the channel against the export's "Maestro channel announced" line.
- [ ] BB-1 … BB-4, BB-4t and **every** ANC tap of the run: each claim's `08 11`/`08 13`/`08 12` sequence against the "expected" column; **refuted if** an `08 12`
      has no `08 11` before it in the same claim, an `08 12` is sent after a `Notify` read `00` in the same claim, or a tap claims twice; BB-4t's `08 12` mode
      against the cycle applied to **that claim's** `Notify` mode.
- [ ] BB-5: a table Settable × time × physical state (film) — `CAP-064` read `e8` ≈ 28 s here, `CAP-065` `00` every time.
- [ ] BB-8: three ACKs, modes `08`, `80`, `20`.
- [ ] BB-9: the (i) dialog frames of the first 3 s against the claim/stream times.
- [ ] BB-12/BB-12m: each announcement's channel against which bud was taken out (film) and any Buds-side `DISC` with the ACL up.
- [ ] BB-14: the tile's subtitle frames against the export's `ConnectionState` lines.
- [ ] BB-15: any app claim closed by a phone `DISC` between the app's request and the Buds' answer — the screen text and the dimmed mode on film; the export's
      "answer cut off, not retried" line; no second `08 12` in that claim.
- [ ] BB-16 / K4d: no app frame while the menu is open; the scheme change per choice (frames); the Disconnect label's contrast on the dark card (pixel sample, as
      `ai-sessions/0062` RESULT §J).
- [ ] VII: `APP_TESTPLAN.md` sections K, L, A, B, E; K3's ACL reason code; Z1's bonding events.
- [ ] Registry Test-IDs: [`ANC-001`], [`ANC-002`], [`ANC-003`], [`ANC-004`], [`INEAR-002`]–[`INEAR-004`], [`CASE-004`]–[`CASE-006`], [`BATT-004`],
      [`AUDIO-003`], [`PAIR-003`], [`PAIR-001`] (Z1).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-066-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BB/CAP-066-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-066-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BB/CAP-066-EVENT-NOTES
