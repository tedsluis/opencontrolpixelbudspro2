# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group P, Voice & case button (`CAP-029`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-029-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_P` to the actual session
date/start-time/end-time, e.g. `CAP-029-2026-09-01_10-40-00_10-50-00-Group_P`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group P):** main run-through group, never yet
captured. Requires 'Conversation Detection' enabled first (Group C, `CAP-019`).

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

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group P)

15. **Start speaking** with Conversation Detection on [`CONV-002`], to trigger the detection
    event. Wait. Note time.
16. **(Optional, destructive — see warning above) Hold the case button for 30 seconds**
    [`CASE-007`] (case open, buds inside, plugged into power). If run, capture the subsequent
    re-pair as its own isolated session immediately afterward [`PAIR-002`].
17. **(Open question, see `PROTOCOL.md` §6) Try a shorter/different press** [`CASE-008`] on the
    case button to see if it triggers pairing mode without a full reset. No officially confirmed
    duration exists — treat any local finding as `[VERIFIED-LOCAL]` material for
    `CAP-029-FINDINGS.md`.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Start speaking (Conversation Detection on) | User (Hardware) | `CONV-002` | TBD |
| TBD | (If run) Hold case button 30s — factory reset | User (Hardware) | `CASE-007` | TBD |
| TBD | (If item 16 run) Re-pair from factory-reset state, own isolated window | User (Hardware) | `PAIR-002` | TBD |
| TBD | Shorter/different case-button press attempt | User (Hardware) | `CASE-008` | TBD |

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
