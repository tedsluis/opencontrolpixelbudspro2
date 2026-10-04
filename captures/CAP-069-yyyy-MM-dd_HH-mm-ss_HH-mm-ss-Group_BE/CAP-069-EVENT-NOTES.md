# Event Notes: Pixel Buds Pro 2 with the official app on the Pixel 7a — Group BE, head gestures, Multipoint, assistant hold, EQ Default, wear states (`CAP-069`)

**Status:** 🔲 **Not yet captured — skeleton only** (written by `ai-sessions/0069`, 2026-10-03; scope is the maintainer's choice in chat 2026-10-03,
`AskUserQuestion` "Leads": *"CAP-068 = release build; CAP-069 = official app (Recommended)"*, and "Features": *"Yes to both, after CAP-069
(Recommended)"*). After the run: rename this folder from the placeholder `CAP-069-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BE` to the film's first/last
overlay times and analyse it as `CAP-067` was (`ai-sessions/0067`).

**Purpose.** Everything here needs the **official** Pixel Buds app, so it is kept apart from `CAP-068` (the OpenControl release build). Each item
is a lead of `ai-sessions/0068` that the existing logs and films could not settle (`DESKRESEARCH_FINDINGS.md`, entry of 2026-10-03):

- **I — Head gestures off/on (lead L68-4, `PROTOCOL.md` §4.5.4, 🟡 `qhr` field 29: 1 = off, 2 = on).** One write in each direction exists
  (`CAP-020` frames 1935 and 2038); the OFF tap was not identified on film. Needed for promotion: both directions, twice, each tap on film.
- **II — Multipoint off/on (lead L68-3, §4.5.2, 🟡 field 11: 0 = off, 1 = on; SASS flag bit 🟡).** One OFF write exists (`CAP-019` 2482).
- **III — One assistant press-and-hold (lead L68-1, 🟡).** `CAP-021`'s four DLCI 0x0a waves start with a Buds message `01 09 00 03 0a 01 03` on
  DLCI 0x08; the bud was out of frame, so the trigger is not on film.
- **IV — A tap on the ANC mode that is already selected (lead L68-7, 🟡: the official app sends no `Set`).**
- **V — The official app's EQ preset "Default" (`A68-APP-16`).** OpenControl 1.0.1 has a "Flat" preset (0.0 × 5); what the official app writes
  for its own default is not captured.
- **VI — Wear and placement states against DLCI 0x08 Code `0x05` (lead L68-5, 🔴 the meaning of values 1, 3, 4, 5, 6).**
- **VII — Ring stopped on the bud, with Play services present (lead L68-2, 🟡 the status-sync reading of §4.4).** The run without Play services
  is `CAP-068`.

**Not in this run:** anything OpenControl does (→ `CAP-068`); the battery advertisement on case-open (→ `CAP-054`, Group AP, redesigned).

## Log Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-069` |
| Group(s) | BE (new) |
| Date | TBD |
| Firmware version | TBD — read it from the official app's "Firmware" row on film |
| Test device | Pixel 7a, the official Pixel Buds app, Google Play services enabled. Android version: read it on film from Settings → About phone (the version of this phone is unreconciled in `CAP-036`…`041`, `A68-CAP-23`) |
| OpenControl | Not used. Pixel 9a: Bluetooth **off** for the whole run |
| Video file | TBD — camera film showing the phone, the case and **your head** (both ears for sections III, VI, VII) |
| Log file | TBD — `CAP-069-btsnoop_hci.log` (and `.log.last` if Bluetooth was toggled) |
| Buds address | not written down; refer to the Buds by connection handle (`AGENTS.md` §7/§9) |

## Preparation

| # | Check | Done |
|---|---|---|
| P1 | Pixel 9a Bluetooth off (one phone only — Multipoint must not find a second source during section I, III–VII) | ☐ |
| P2 | Bluetooth HCI snoop log on (developer options); switch Bluetooth off and on **on film** so the log starts clean | ☐ |
| P3 | Camera films the phone, the case and your head; head on the right of the frame = Left bud (as `CAP-064`). No narration | ☐ |
| P4 | Status bar on film across a minute change at the start and at the end (the clock offset is measured, not assumed) | ☐ |
| P5 | Do Not Disturb on | ☐ |
| P6 | Settings → About phone on film (Android version, build); official app → Device details → the firmware row on film | ☐ |
| P7 | Note the starting values on film: Head gestures (on/off), Multipoint (on/off), press-and-hold Left/Right, the EQ preset, the ANC mode | ☐ |

**Rhythm:** one action, then wait **10 s** with hands off (longer where a step says so). Something unexpected: stop, wait 10 s, continue.
Before committing: the camera film may carry a street-address overlay — check and mask.

## Steps

DLCI numbers are session-local (MAESTRO 0x02 or 0x03, Message Stream 0x04 or 0x05, GSND CONTROL 0x08 or 0x09, GSND AUDIO 0x0a or 0x0b); the
expected bytes below are written for the even numbers.

### I. Head gestures (`HEAD-001`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BE-1 | both buds worn, Controls → Head gestures screen open | hold 5 s on the current state | the toggle as noted in P7 | the connect-time `ReadSetting` of field 29 (1 or 2) | — |
| BE-2 | toggle **off** | tap the toggle **on** | on | `WriteSetting 4:{29:2}` → OK; settings stream `4:{29:2}`; DLCI 0x08 `04 16 00 02 08 01` | the write carries another value, or no write follows the tap |
| BE-3 | on | tap **off** | off | `WriteSetting 4:{29:1}` → OK; DLCI 0x08 `04 16 00 02 08 02` | as BE-2 |
| BE-4 | off | repeat BE-2 | on | as BE-2 | a second sample disagrees with the first |
| BE-5 | on | repeat BE-3; then leave the toggle as P7 noted | off | as BE-3 | as BE-4 |

If the toggle was **on** at the start, run BE-3 first, then BE-2, BE-5, BE-4.

### II. Multipoint (`MULTI-001`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BE-6 | Multipoint screen open | tap the toggle to the **other** state | changed | `WriteSetting 4:{11:1}` (on) or `4:{11:0}` (off) → OK; within 1 s a Message Stream `07 11 00 04 01 02 xx 00` | no write, or another field |
| BE-7 | — | tap it back | changed back | the other value; `07 11 …` with one bit of `xx` changed (🟡 `b8` on / `98` off) | the `07 11` byte does not change with the toggle |
| BE-8 | — | repeat BE-6 and BE-7 once | — | as above | the second pair disagrees |

### III. Assistant press-and-hold (`HOLD-004`, `HOLD-003`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BE-9 | both worn | Controls: set **Right** press-and-hold to **Digital assistant** | "Digital assistant" | `WriteSetting` as `CAP-021` frame 3619 | — |
| BE-10 | Right ear in view | press and hold the **Right** bud for about 3 s, say nothing, release; wait 15 s | the assistant's overlay, if any | DLCI 0x08 Buds `01 09 00 03 0a 01 03`, then DLCI 0x0a payload frames, a phone `08 11 00 00` on DLCI 0x08, and about 8 s later a phone `08 06 00 04 08 00 10 01` | the hold on film is not followed within 1 s by `01 09 00 03 0a 01 03`, or the wave appears without a hold |
| BE-11 | — | repeat BE-10 once | — | as BE-10 | — |
| BE-12 | — | wait 30 s without touching anything | — | **no** `01 09 00 03 0a 01 xx` and no DLCI 0x0a payload | a wave appears with nothing done |
| BE-13 | — | set Right press-and-hold back to **Active noise control** | — | `WriteSetting` as `CAP-021` frame 4976 | — |

### IV. A tap on the current ANC mode (`ANC-006`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BE-14 | both worn, Device details, a mode highlighted | tap **another** mode (positive control) | that mode highlighted | `08 12 00 14 …` → ACK → Notify | — |
| BE-15 | — | tap the **highlighted** mode once; wait 10 s | unchanged | 🟡 **no** `08 12` (command, exit status and BE-14 as the positive control) | a `08 12` follows the tap — then note its answer (ACK or NAK and its reason) |
| BE-16 | — | repeat BE-15 with a second mode selected first | — | as BE-15 | — |

### V. EQ "Default" (`EQP-001`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BE-17 | Sound → Equalizer, any non-default preset selected (tap one first; note its name) | tap **Default** | the sliders as the app draws them — hold 3 s on film | one `WriteSetting 4:{16:{…}}`: **record the five values** (prediction 🟡: 0.0 × 5) | the app writes nothing, or writes another field |
| BE-18 | — | select the P7 preset again | — | its write | — |

### VI. Wear and placement states (`INEAR-002`, `INEAR-003`, `INEAR-004`, `CASE-004`, `CASE-005`)

Each step is one movement of one bud, with the bud and the ear (or the case slot) in view, then 10 s hands off.

| Step | Action | Expected on the wire | Refuted if |
|---|---|---|---|
| BE-19 | start: both buds in the case, lid open, connected | note the current `04 05 00 02 08 xx` (🟡 3) and the Settable byte of the last Notify | — |
| BE-20 | take the **Left** bud out and hold it in the hand (not in the ear) | a new `04 05` value or none — record | — |
| BE-21 | put the Left bud in the ear | record `04 05`; a Notify with Settable `e8` | — |
| BE-22 | take the **Right** bud out, hold it | record | — |
| BE-23 | put the Right bud in the ear | record (🟡 6 with both worn) | — |
| BE-24 | take the Right bud out of the ear, lay it on the table | record | — |
| BE-25 | take the Left bud out of the ear, lay it on the table | record; a Notify with Settable `00` | — |
| BE-26 | put the Right bud in the case | record; a battery update with the Right charging bit | — |
| BE-27 | put the Left bud in the case | record (🟡 3); the link may drop (ADR-016) | — |

The reading of Code `0x05` is refuted as a placement state if the same movement gives different values in BE-20…BE-27 and in a repeat, or if the
value changes with no movement on film.

### VII. Ring stopped on the bud (`FIND-005`, `FIND-001`)

| Step | Pre-state | Action | Expected on screen | Expected on the wire | Refuted if |
|---|---|---|---|---|---|
| BE-28 | both buds out of the ears, on the table, connected | Find device → ring **Left** | ringing | phone `04 01 00 01 02`; Buds ACK `ff 01 00 03 04 01 00`; Buds `04 01 00 01 02`; phone ACK `ff 01 00 02 04 01` | — |
| BE-29 | ringing | stop it **on the bud** (touch it / put it in the ear) — on film; wait 10 s | the app shows the ringing as stopped | 🟡 Buds `04 01 00 01 00` with **no** phone command before it, then the phone's ACK `ff 01 00 02 04 01` | the ringing stops and no Buds `04 01` follows, or a phone `04 01 00 01 00` precedes it |

## Don'ts

- Do not open OpenControl; do not switch the Pixel 9a's Bluetooth on.
- Do not change two settings within 10 s of each other.
- Do not speak during BE-10/BE-11 (the DLCI 0x0a payload is audio; nothing of it is to be decoded or kept beyond frame counts).

## After the run

The raw `btsnoop_hci.log` (both, if Bluetooth was toggled) via `adb bugreport`; the film. All into this folder, then `sha256sum *`. Restore the
settings noted in P7.

## Analysis checklist

- [ ] Scope every filter to the Buds' ACL handle first; identify the DLCIs by content (`AGENTS.md` §13).
- [ ] The film's clock against the phone's: measured from P4, both ends.
- [ ] I: every `WriteSetting 4:{29:n}` with its time against the tap on film; the `04 16` value after each. **Proposal to the maintainer only** —
      promotion of field 29 needs their answer in chat (`AGENTS.md` §6).
- [ ] II: every `4:{11:n}` and the `07 11` flags byte after each; the bit against the Fast Pair SASS page's flag table (quote the sentence).
- [ ] III: per hold on film, the first DLCI 0x08 and DLCI 0x0a frame after it and the gap; frame counts only for the audio channel. BE-12 as the
      negative window, with the command, its exit status and BE-10 as the positive control.
- [ ] IV: `08 12` count in the BE-15/BE-16 windows against BE-14.
- [ ] V: the five floats of BE-17's write; compare with OpenControl's "Flat".
- [ ] VI: a table step → `04 05` value → Settable byte → battery charging bits; state which values repeat.
- [ ] VII: direction and order of every `04 01` and `ff 01` frame in BE-28/BE-29.
- [ ] Traceability (`AGENTS.md` §13 step 7): `HEAD-001` (BE-1…5), `MULTI-001` (BE-6…8), `HOLD-004`/`HOLD-003` (BE-9, BE-13), `ANC-006`
      (BE-15/16), `ANC-001`…`004` (BE-14, whichever mode), `EQP-001` (BE-17), `INEAR-002`/`003`/`004` and `CASE-004`/`005` (BE-19…27),
      `FIND-001`/`FIND-005` (BE-28/29), `PAIR-003` and `BATT-004` (every connect) — each referenced in the timeline or flagged.
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9, `id_registry.csv`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` rows, and write `CAP-069-FINDINGS.md`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-069-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BE/CAP-069-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-069-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BE/CAP-069-EVENT-NOTES
