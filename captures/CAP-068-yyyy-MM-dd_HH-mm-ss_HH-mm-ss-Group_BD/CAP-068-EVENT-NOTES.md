# Event Notes: OpenControl for Pixel Buds Pro 2 on Pixel 9a (GrapheneOS, a secondary user without Google Play) — Group BD, the 1.0.1 release APK (`CAP-068`)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0069`, 2026-10-03; scope is the maintainer's choice in chat 2026-10-03,
`AskUserQuestion` "Hotfix 1.0.1": *"1.0.1 with all of 0069 (Recommended)"* and "Leads": *"CAP-068 = release build; CAP-069 = official app
(Recommended)"*). After the run: rename this folder from the placeholder `CAP-068-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BD` to the film's first/last
overlay times and analyse it as `CAP-067` was (`ai-sessions/0067`).

**Purpose.** Only what the existing logs and films cannot answer (`ai-sessions/0069` RESULT, ledger):

- **I — an ANC `Set` by the release build** (`A68-GOV-01`). `PROJECT.md`'s Definition of done 2 is ticked on the maintainer's own test; no
  capture holds an `08 12` from a release-signed build (`CAP-067`: 0 × `08 12`, positive control `CAP-066`). Here: all four modes from the tab,
  then the Quick Settings tile, buds worn, on film.
- **II — a tap on the current mode** (`ANC-006`, lead L68-7): does OpenControl send a `Set`, and what do the Buds answer?
- **III — Bluetooth off/on with an export after each** (`CAP-067` BC-10 had no export afterwards — F-3 unverified).
- **IV — the 1.0.1 changes on hardware** (`ai-sessions/0069` Phases 6–7): values from an earlier connection are marked, not shown as current;
  no silent choice between two paired Buds and the "Use different Buds" action; the reworded messages; "—" for a setting that was not read; the
  EQ preset "Flat"; the version line.
- **V — `APP_TESTPLAN.md` steps never run on hardware** (`A68-SES-04`): C5, H5, F5, J4, I4, L3 with content, the K1 screen.
- **VI — Ring stopped on the bud, without Play services** (`FIND-005`, lead L68-2): the app sends nothing new on the wire for it (the
  maintainer's choice, "Ring status": *"Nothing new on the wire; test first (Recommended)"*) — this step records what the Buds send and what the
  app shows.
- **VII — the forced connect failure** (`A68-APP-10`): Connect with the Buds out of range.
- **Not in this run:** anything that needs the official app (→ `CAP-069`); the battery advertisement on case-open (→ `CAP-054`); K5 (the
  GrapheneOS auto-off setting is not available in the user without Play, `CAP-067`); the destructive steps A5/(E)/B4/Z1 (run in `CAP-067`).

If the run exceeds one sitting (about 25 minutes), stop after section IV and run V–VII as a second film in the same folder.

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-068` |
| Group(s) | BD (new) |
| Date | TBD |
| Phone | Pixel 9a, GrapheneOS — the secondary user without Google Play used for `CAP-067`; note its user id (P0) |
| App under test | OpenControl for Pixel Buds Pro 2 **1.0.1** (versionCode 10001), the release APK signed with the maintainer's key (`scripts/release.sh`, `RELEASING.md`) — read from the **Info tab on film**: "App: 1.0.1, build <hash> (<date>)", no "-dirty"; APK SHA-256 and certificate SHA-256 from the script's output |
| Official Pixel Buds app | Not used. Pixel 7a: Bluetooth off |
| Buds | Pixel Buds Pro 2; firmware read from the Connection tab on film |
| Video file | TBD — camera film: the phone, the case and **your head** |
| Log files | TBD — `CAP-068-btsnoop_hci.log` and `.log.last` (Bluetooth is toggled in section III), the app's exports, logcat |

## Preparation

| # | Check | Done |
|---|---|---|
| P0 | In the test user: `adb shell am get-current-user`; `adb shell pm list packages --user <id> \| grep -i -E "gms\|vending"` — note the output **and the exit status** (1 = none); positive control: the same with `grep opencontrol` (exit 0). Save both outputs into this folder | ☐ |
| P1 | The **1.0.1 release** APK installed over 1.0.0 in the test user (same signing key — an update, no uninstall; note that Android accepted versionCode 10001 over 10000). Do **not** clear the app's data: section IV needs the stored association | ☐ |
| P2 | Bluetooth HCI snoop log on (set in the Owner, then switch to the test user); switch Bluetooth off and on **on film** | ☐ |
| P3 | Camera films the phone, the case and your head (ears visible for every wear step); head on the right of the frame = Left bud | ☐ |
| P4 | Do Not Disturb on | ☐ |
| P5 | Status bar on film across a minute change at the start and at the end (the clock offset is measured) | ☐ |
| P6 | Quick Settings of the test user: the ANC tile present and large | ☐ |
| P7 | Debug tab: **Debug mode on** (for the exports); after the first "ready": gear → **Info**, hold 3 s on the build line | ☐ |

**Rhythm:** one action, then wait 5–10 s. Something unexpected: stop, wait 10 s, continue. **Export the debug log before any step that ends the
process and after every Bluetooth off/on** (the `CAP-067` lesson). No narration. Before committing: check the film for a street-address overlay.

## Steps

DLCI numbers are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05); the bytes below are written for the even numbers.

### I. ANC by the release build (`ANC-001`…`ANC-004`, `APP_TESTPLAN.md` F1–F4, G3)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BD-1 | both buds **worn**, ready, ANC tab | tap **TRANSPARENT** | "ANC mode: TRANSPARENT (updated …)" | claim of the Message Stream; `08 11 00 00` → Notify; `08 12 00 14 01 e8 e8 80 …` → ACK `ff 01 00 02 08 12` → Notify `08 13 00 04 01 e8 e8 80` | no `08 12`, or a NAK with both buds worn |
| BD-2 | — | tap **ADAPTIVE** | ADAPTIVE | `… 40 …` → ACK → Notify | as BD-1 |
| BD-3 | — | tap **OFF** | OFF | `… 20 …` | as BD-1 |
| BD-4 | — | tap **ACTIVE** | ACTIVE | `… 08 …` | as BD-1 |
| BD-5 | worn, Quick Settings open | tap the **ANC tile** four times, 5 s apart | the tile's label follows the mode | four `08 12` → ACK → Notify | a tap without `08 12` |
| BD-6 | — | take **both** buds out (on the table); tap the tile once; then the ANC tab | the tile and the tab say the Buds refuse a change while not worn | Notify with Settable `00`; if a `Set` is sent: NAK `ff 02 00 03 02 08 12` | an ACK with Settable `00` |

### II. A tap on the current mode (`ANC-006`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BD-7 | both worn again, mode ACTIVE shown | tap **ACTIVE** | unchanged | record: a `Set` `… 08 …` or none; if sent, its answer (ACK, or NAK and its reason byte) | — (an observation; the result decides whether the app should skip the `Set`) |
| BD-8 | — | tap **OFF**, wait, tap **OFF** again | OFF | as BD-7 for the second tap | — |

### III. Bluetooth off/on with exports (`PAIR-003`, `APP_TESTPLAN.md` K1/K2)

| Step | Pre-state | Action | Expected on screen | Expected on the wire / in the export | Refuted if |
|---|---|---|---|---|---|
| BD-9 | ready, Connection tab on screen | Quick Settings: Bluetooth **off**; back to the app | "Bluetooth is disabled." + **Enable Bluetooth**; Left/Right/Case lines no longer shown as current (section IV) | export line "Bluetooth adapter: ON -> TURNING_OFF"; the loss named "Bluetooth was switched off on this phone", without "(provisional …)" | a generic loss text, or "provisional" on that line |
| BD-10 | — | **Export debug log** now (Debug tab) | saved | the lines above are in the file | — |
| BD-11 | — | **Enable Bluetooth** → allow; wait; **Connect** if it does not connect by itself | ready | new announcement, `SubscribeRuntimeInfo`, battery | no ready within 30 s |
| BD-12 | — | **Export debug log** again | saved | "Bluetooth adapter: … -> ON" and the new session | — |

### IV. The 1.0.1 changes (`ai-sessions/0069` Phases 6–7; `APP_TESTPLAN.md` section S — S1…S13 map to the steps below)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BD-13 | ready, buds worn, an ANC mode on screen (S1, S2) | tap **Disconnect**; open ANC, Sound, Controls | ANC: the last mode **dimmed**, (i) with the dot, in it "ANC mode: <MODE> — from the last connection (updated HH:MM:SS)"; Sound and Controls: values dimmed, each (i) starts "From the last connection — the app is not connected to the Buds now."; the tile says "Open the app" (the battery card is shown only while ready) | the session closes | a value of the closed session shown undimmed or without the (i) line |
| BD-14 | — (S3, S4) | tap **Connect**; ANC tab, then the battery card's (i) | until this connection's Notify the mode stays dimmed "— from the last connection", then current; a Case value not yet reported again reads "Case: NN% — last seen HH:MM:SS (last connection)" | announcement, battery, `08 11` → Notify | a mark stays after fresh data arrived, or the Case line says "(no bud charging in the case)" for a value from before this connection |
| BD-15 | ready (S5) | Sound tab: tap **FLAT** | the five sliders at 0.0 after the Buds' OK | one `WriteSetting 4:{16:{0.0 × 5}}` → OK — the payload of `CAP-015` frame 2111 | another value, or no write |
| BD-16 | — | tap the preset that was active before (note it) | — | its write | — |
| BD-17 | ready (S7, S8) | gear → **Settings**: read the "Use different Buds" text, tap the button; then Connection tab → **Pair a device** → pick the Buds | the session closes; the Connection tab shows **Pair a device** (with one pair of Buds bonded: "No Pixel Buds Pro 2 paired yet."); the tap opens Android's picker; after the choice: paired, Connect works | Disconnect; export line "Pairing: use different Buds — removed N association(s) of this app"; a new CDM association | the app reconnects to Buds without the picker having been shown |
| BD-18 | — (S6) | right after "ready": the Sound tab in the first second (film at normal speed) | each EQ band shows "—" until its read answers, Balance shows "—" instead of "Centre"; then the numbers | the `ReadSetting` answers of field 16 and 17 | "0.0" or "Centre" shown before the read answered |
| BD-19 | — | Info tab | "App: 1.0.1, build <hash> (<date>)" | — | another version, or "-dirty" |

Two paired Pixel Buds devices are needed to show the "no silent pick" sentence (S9: "More than one Pixel Buds device is paired with this phone. …"); with one pair this run shows only BD-17 — note S9 as not run. S11 (a tile toast for every failure) is part of BD-6; S12 (export across a rotation): rotate while the "save as" dialog of BD-10 is open.

### V. Steps never run on hardware (`A68-SES-04`)

| Step | `APP_TESTPLAN.md` | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BD-20 | C5 | Disconnect; tap **Connect twice quickly** | one session, no error | one MAESTRO open (one `SABM`) | two opens, or an error text |
| BD-21 | H5 | Sound: drag a band, release; tap **Read EQ again** | the value just written | `WriteSetting` → OK; `ReadSetting` → the same five floats | the read differs |
| BD-22 | F5 | change the ANC mode with a **press-and-hold on a bud** (on film); tap **Refresh** | the bud's mode | a Notify from the Buds without a phone `Set`; `08 11` → Notify on Refresh | the screen keeps the old mode after Refresh |
| BD-23 | J4 | ready; press Home; wait **2 minutes**; reopen | still ready, or a clear message if the Buds closed the session | no app-side close in the two minutes | a silent loss (no message) |
| BD-24 | I4 | Find: **Ring Left**; **Disconnect** before Stop; **Connect**; **Stop** | after Disconnect the notice that a ring may still be sounding; after Stop it ends | `04 01 00 01 02` → ACK; after the reconnect `04 01 00 01 00` → ACK | the words differ from `APP_TESTPLAN.md` I4 |
| BD-25 | L3 | Debug mode **off**; do one ANC tap; export | — | the export has state lines and **no** hex line after the "Debug mode off" line | a hex line after it |

### VI. Ring stopped on the bud (`FIND-005`, `FIND-001`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BD-26 | both buds on the table, ready, Debug mode on again | Find: **Ring Left** | "Ringing: Left earbud — tap Stop to end it." | `04 01 00 01 02`; Buds ACK `ff 01 00 03 04 01 00`; Buds' own `04 01 00 01 02` | — |
| BD-27 | ringing | stop it **on the bud** (touch it / pick it up, on film); do not tap Stop; wait 15 s | record what the notice does (1.0.1 does not read the Buds' status message — it may stay) | 🟡 Buds `04 01 00 01 00` with no phone command before it; the app sends **no** ACK `ff 01 00 02 04 01` (no Play services in this user) | a phone `04 01 00 01 00` precedes it |
| BD-28 | — | tap **Stop** | the notice disappears | `04 01 00 01 00` → ACK | — |

### VII. A connect that fails (`A68-APP-10`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BD-29 | Disconnect tapped; both buds in the **closed** case, in another room (or the case closed for ≥ 60 s so the link is down) (S10) | tap **Connect** | within about 15 s: "Couldn't open the app's channel to the Buds. Possible causes: the Buds are out of reach or in the closed case. Open the case and try again." — no mention of another app; the notification may appear and disappear once | a page attempt ending in status `0x04` (page timeout), no RFCOMM | a spinner without end, "Unexpected error", a text naming another app, or a crash (system log: `ForegroundServiceDidNotStartInTimeException`) |
| BD-30 | — | open the case next to the phone; **Connect** | ready | — | — |
| BD-end | — | export the debug log; status bar across a minute change; stop the film | — | — | — |

## Don'ts

- Do not clear the app's data and do not uninstall 1.0.0 first (P1).
- Do not open the Owner user during the run (its Play services must stay in the background).
- Do not tap two things within 5 s of each other, except where a step says "quickly".

## After the run

The exports (test user's storage — `adb pull` or share), `adb bugreport` (covers all users), both `btsnoop_hci.log` files, app logcat and
system log, P0's two outputs, the film. All into this folder, then `sha256sum *`.

## Analysis checklist

- [ ] Scope every filter to the Buds' ACL handle; identify the DLCIs by content (`AGENTS.md` §13).
- [ ] P0 and the Info frame: user id, no `com.google.android.gms`/`com.android.vending` (exit status + positive control); "1.0.1", the build hash
      against `git log`, the APK and certificate SHA-256 against the script's output.
- [ ] Zero Play-services claims on the Message Stream (`03 08 00 02 01 25`): command, exit status, positive control in `CAP-066`.
- [ ] I: every `08 12` with its answer and the following Notify, against the tap on film. **Only then** may the note "maintainer-attested, not
      captured" on the Definition of done 2 (`PROJECT.md`, `RELEASING.md`) be replaced by this capture — the maintainer's decision.
- [ ] II: per tap on the current mode — `Set` sent or not, and the answer. Proposal for the app (skip the `Set`, or keep it) to the maintainer.
- [ ] III: the two exports against the system log's adapter `STATE_CHANGED` times.
- [ ] IV: film frames of BD-13/BD-14 (marks appear and disappear), BD-15's five floats, BD-17, BD-18, BD-19.
- [ ] V: one row per step — done / not done / done differently — and the `APP_TESTPLAN.md` Summary updated.
- [ ] VI: direction and order of every `04 01`/`ff 01` frame; what the app showed between BD-27 and BD-28.
- [ ] VII: the HCI status of the failed page and the app's text on film.
- [ ] Traceability (`AGENTS.md` §13 step 7): `ANC-001`…`004` (BD-1…5), `ANC-006` (BD-7/8), `PAIR-003` (BD-9…11, BD-13/14, BD-20, BD-29/30),
      `BATT-004` (every connect), `EQS-001` (BD-15, BD-21), `FIND-001`/`FIND-005` (BD-24, BD-26…28), `INEAR-004` (BD-6) — each referenced in
      the timeline or flagged.
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `id_registry.csv`, `APP_TESTPLAN.md`, and write `CAP-068-FINDINGS.md`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-068-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BD/CAP-068-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-068-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BD/CAP-068-EVENT-NOTES
