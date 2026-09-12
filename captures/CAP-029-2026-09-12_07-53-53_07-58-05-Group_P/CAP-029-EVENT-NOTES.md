# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group P, Voice & case button (`CAP-029`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-029-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_P` to the actual session
date/start-time/end-time, e.g. `CAP-029-2026-09-01_10-40-00_10-50-00-Group_P`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group P):** main run-through group, never yet
captured. Requires 'Conversation Detection' enabled first (Group C, `CAP-019`). Item 15
(`CONV-002`) tests whether the voice-triggered detection event itself (pause media, switch to
Transparency) produces a wire command; item 17 (`CASE-008`) targets a genuinely open protocol
question (`PROTOCOL.md` §6) — whether a shorter/different case-button press triggers pairing mode
without a full factory reset, since no officially documented duration exists for that.

> ⚠️ **Item 16 (`CASE-007`) is a confirmed full factory reset, not just pairing mode** — it also
> resets the Find My Device link on the Pro 2. It is **optional and not a prerequisite** for
> anything else in this guide (Group A's lightweight baseline is sufficient on its own). Do this
> deliberately, last, and only once ready to re-pair from scratch. See
> `WORKSTATION_PREPARATIONS.md`'s Disaster Recovery section before running it. If you do trigger
> it, capture the subsequent re-pair as its own isolated session right afterward — that session
> is `PAIR-002`'s genuine home (see `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s definition; do **not**
> tag an ordinary forget-and-re-pair as `PAIR-002` — that was a confirmed mislabeling incident
> fixed on 2026-08-20, see `CHANGELOG.md`).
>
> **Decide before capturing:** run items 15 and 17 only in this session (skip the destructive
> item 16), or run all three including the factory reset. Record the decision here: TBD.
>
> **Decision guidance (not a decision made on the maintainer's behalf):** items 15/17 alone are
> non-destructive and sufficient to close this session's own open questions (`CONV-002` wire
> traffic, `CASE-008`'s shorter-press behavior). Item 16 is worth including in *this specific*
> session only if a genuine, isolated `PAIR-002` (from-true-factory-state pairing) baseline is
> independently wanted — it is not required to answer items 15/17's own questions, and every other
> Group in this guide already works fine against Group A's lightweight forget-and-re-pair baseline.
> If in doubt, prefer running 15/17 only and scheduling the factory reset as its own deliberate,
> separately-planned session later.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-029`                     |
|      Group(s)    |                         P                          |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    | TBD (Pixel 7a, Android version, official app version) |
| Video file       |               TBD — `CAP-029-recording.mp4`        |
| Log file         |             TBD — `CAP-029-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

**Preparation (required before starting):**
- Confirm 'Conversation Detection' is enabled on screen (Device details → Sound → Audio
  intelligence → Conversation detection) before starting item 15. **If it was not already enabled
  in a separate prior session (`CAP-019`), enable it now as this session's own first logged step**
  (see Procedure step 0 below) rather than assuming it's already on.
- Decide the item-16 factory-reset question (see the "Decision guidance" note above) *before*
  starting, and record the decision in this file.
- Confirm HCI snoop logging is already enabled and running, and the Buds are connected, before any
  timed action begins.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group P)

0. **(Only if not already done in a separate session):** enable 'Conversation Detection' now, as
   this session's own first logged step. Note the exact time. If already confirmed enabled from a
   prior session, skip this step and just record the confirmation per the Preparation note above.
15. **Start speaking** with Conversation Detection on [`CONV-002`], to trigger the detection
    event. Wait. Note time.
16. **(Optional, destructive — see warning/decision-guidance above) Hold the case button for 30
    seconds** [`CASE-007`] (case open, buds inside, plugged into power). If run, capture the
    subsequent re-pair as its own isolated session immediately afterward [`PAIR-002`].
17. **(Open question, see `PROTOCOL.md` §6) Try a shorter/different press** [`CASE-008`] on the
    case button to see if it triggers pairing mode without a full reset. No officially confirmed
    duration exists — treat any local finding as `[VERIFIED-LOCAL]` material for
    `CAP-029-FINDINGS.md`.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 07:53:55 | Start of video. Bluetooth is off. | User | — | — |
| 07:54:01 | Bluetooth turned on, "Pixel Buds Pro 2 van Ted" connects. | User (Phone) | — | — |
| 07:54:08 | Spotify is opened, music starts playing ("Tears Dry On Their Own"). | User (App) | — | — |
| 07:54:13 | User starts speaking (thumb visible on screen). | User (Hardware) | `CONV-002` | Testing Conversation Detection. |
| 07:54:55 | User forgets the Pixel Buds Pro 2 in Bluetooth settings. | User (Phone) | — | Preparation for factory reset. |
| 07:55:03 | User starts holding the case button (buds in case, case open). | User (Hardware) | `CASE-007` | — |
| 07:55:33 | User releases the case button after ~30 seconds. | User (Hardware) | `CASE-007` | Factory reset triggered. |
| 07:56:01 | Reset Pixel Buds Pro 2 appear in the pairing list. | Hardware | `PAIR-002` | — |
| 07:56:11 | User initiates pairing process. | User (Phone) | `PAIR-002` | — |
| 07:57:10 | Setup complete, device is connected. | System | `PAIR-002` | — |
| 07:57:56 | User forgets the device again in Bluetooth settings. | User (Phone) | — | — |
| 07:58:04 | End of video. | User | — | — |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [ ] `CONV-002`: check whether the Conversation Detection trigger (pause media, switch to
      Transparency) produces a wire-visible command, and if so on which DLCI/channel.
- [ ] `CASE-007` (if run): confirm this reproduces the previously-documented factory-reset
      behavior (`CAP-001`/`CAP-002`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group P #16) — a third
      confirming data point, not expected to change the existing finding.
- [ ] `CASE-008`: record the exact press duration tried and whether pairing mode triggered — this
      resolves a genuinely open question (`PROTOCOL.md` §6), don't guess if inconclusive.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `CONV-002`/`CASE-007`/`CASE-008`/`PAIR-002` (as applicable)
      are clearly referenced above.
- [ ] Write `CAP-029-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-029-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_P/CAP-029-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-029-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_P/CAP-029-EVENT-NOTES
