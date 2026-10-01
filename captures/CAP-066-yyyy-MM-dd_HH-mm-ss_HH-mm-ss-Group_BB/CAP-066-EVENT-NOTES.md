# Event Notes: OpenControl for Pixel Buds on Pixel 9a (GrapheneOS) — Group BB, the `CAP-064` leftovers (`CAP-066`)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0060`, 2026-10-01). After the run: rename this folder from the placeholder
`CAP-066-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BB` to the film's first/last overlay times and analyse it as `CAP-064` was (`ai-sessions/0060`).

**Purpose (the maintainer's choice in chat 2026-10-01, `ai-sessions/0060` checkpoint "To CAP-065" — `CAP-065` had already been run, so the leftovers go here):**

- **I — the real I-1 path (the disabled-tap re-check of `ai-sessions/0054`).** In `CAP-064` it was never entered: the app's last `Notify` read `e8`, so taps and
  the tile sent a `Set` directly (ACKed with the buds on the table, frame 2698; NAKed by the tile, 3440). This section first makes the app *see* `00`, then taps.
- **II — when does the Settable byte read `00`?** `CAP-064-FINDINGS.md` §3: with in-ear detection on it read `e8` for ≈ 28 s with both buds on the table
  straight from the case (2299–2759), and `00` after buds were taken out of the ears (3299, 3443). 🟡 hypothesis: `00` follows a removal from the ear or a dock,
  not "not worn" as such.
- **III — the ANC modes never tapped in OpenControl** (`ANC-002` Noise cancellation, `ANC-004` Transparency) and the I-4 "last connection" words in the battery (i)
  dialog within 2 s of a Connect (`CAP-064-FINDINGS.md` §2 I-4a).
- **IV — restore the balance** to its value before `CAP-064` (Right 4 = `17:7`; `CAP-064` ended at Centre `17:0`).

**Facts the maintainer gave (chat 2026-10-01):** the maintainer does not speak on the films (no spoken hash or state); Play services' *Nearby devices* permission
is **allowed** (used for casting to a Chromecast). The build is identified from the logs; if the next FEATURE session adds the app build number to an Info
screen (its plan), film that screen once at the start.

---

## A.0. Which phone, which app, which build

| Item | Value |
|---|---|
| Phone | **Pixel 9a, GrapheneOS** |
| App under test | **OpenControl for Pixel Buds**, debug APK of the latest commit under `android/` — write the hash here: `__________` (`git log -1 --format=%h -- android`); film the app's Info screen if it exists |
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

**Rhythm:** one action, then wait 5–10 s (longer where a step says so). Something unexpected: stop, wait 10 s, continue.

## A.2. Steps

DLCI numbers are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05 — whichever side opened the RFCOMM multiplexer).

### I. The real I-1 path (ANC tab and tile)

| Step | Pre-state | Action | Expected on screen | Expected on the wire |
|---|---|---|---|---|
| BB-1 | ready, both buds **worn** (on film), ANC tab | Take **both** buds out of the ears, lay them on the table **in view**; wait 20 s; tap **Refresh** | "ANC can only be changed …" (or the new wording) appears; buttons stay enabled | `08 11` → `08 13 00 04 01 e8 00 …` (Settable `00`) |
| BB-2 | as BB-1, note shown | Tap **ADAPTIVE** (buds still on the table) | no mode change; the checked time moves | **one** claim: `08 11` → `08 13 … 00 …` → **no** `08 12` |
| BB-3 | as BB-2 | Quick Settings → **ANC tile** once | no mode change; toast "ANC can only be changed while you wear the Buds." | one claim: `08 11` → `08 13 … 00 …` → no `08 12` |
| BB-4 | as BB-3 | Put both buds in the ears **on film**; wait 10 s; tap **ADAPTIVE** | Adaptive | one claim: `08 11` → `08 13 … e8 …` → `08 12 … 40` → ACK |

### II. When does Settable read `00`? (ears and buds in view; in-ear detection **on**)

| Step | Pre-state | Action | Expected | Expected on the wire |
|---|---|---|---|---|
| BB-5 | both buds in the case, lid open | Take both buds out and lay them **straight on the table** (do not wear them). Tap ANC **Refresh** at ≈ 10 s, 30 s, 60 s, 90 s, 120 s | say nothing; just tap | one `08 11` → `08 13 … <Settable> …` per Refresh — the result is the Settable value over time |
| BB-6 | both on the table | Put both buds in the ears for 20 s, then take both out onto the table; Refresh at ≈ 10 s, 30 s, 60 s | — | as BB-5 |
| BB-7 | both on the table | Put one bud in the case (lid open), leave the other on the table; Refresh at ≈ 10 s and 60 s | — | as BB-5 (the `CAP-064` frame 5591 situation, `00` there) |

### III. ANC modes and the I-4 words

| Step | Pre-state | Action | Expected on screen | Expected on the wire |
|---|---|---|---|---|
| BB-8 | both worn, ANC tab | Tap **Noise cancellation**, wait 5 s, tap **Transparency**, wait 5 s, tap **Off** | each mode after the Buds' answer | `08 12 … 08` → ACK; `08 12 … 80` → ACK; `08 12 … 20` → ACK [`ANC-002`, `ANC-004`, `ANC-001`] |
| BB-9 | ready, both buds in the case, lid open, Connection tab | **Disconnect**; take both buds out onto the table; **Connect**, and **immediately** tap the battery card's (i) | the (i) dialog shows the previous connection's lines marked "last connection" until the new report arrives (≈ 2 s), then "not charging (out of the case)" | app `SABM` MAESTRO; claim with `03 03 00 03 64 64 ff` |

### IV. Restore

| Step | Action | Done |
|---|---|---|
| BB-10 | Sound tab: drag the balance to **Right 4** (release) | `WriteSetting 4:{17:7}` → OK ☐ |
| BB-11 | Status bar across a minute change; stop the film | ☐ |

## A.3. After the run

Bug icon / settings → Debug → **Export debug log**; `adb bugreport`; the raw `btsnoop_hci.log`; app logcat and system log (GrapheneOS log viewer); the film.
All into this folder, then `sha256sum *`.

## A.4. Analysis checklist

- [ ] Pre-filter by the Buds' classic handle; DLCIs by content.
- [ ] BB-1 … BB-4: each claim's `08 11`/`08 13`/`08 12` sequence against the "expected" column; **refuted if** an `08 12` is sent after a `Notify` read `00` in
      the same claim, or a disabled tap claims twice.
- [ ] BB-5 … BB-7: a table Settable × time × physical state (film).
- [ ] BB-8: three ACKs, modes `08`, `80`, `20`.
- [ ] BB-9: the (i) dialog frames of the first 3 s against the claim/stream times.
- [ ] Registry Test-IDs: [`ANC-001`], [`ANC-002`], [`ANC-003`], [`ANC-004`], [`INEAR-002`]–[`INEAR-004`], [`CASE-004`]–[`CASE-006`], [`BATT-004`],
      [`AUDIO-003`], [`PAIR-003`].

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-066-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BB/CAP-066-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-066-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BB/CAP-066-EVENT-NOTES
