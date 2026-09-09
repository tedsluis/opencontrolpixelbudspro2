# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AD (repeat), dock-state anomaly with an open ACL (`CAP-048`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-048-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AD` to the actual session
date/start-time/end-time, e.g. `CAP-048-2026-09-15_08-30-00_08-50-00-Group_AD`.

**Purpose (repeat of `CAP-037`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AD):** `CAP-037-FINDINGS.md`'s
own open anomaly: on one of 26 same-session `DECISIONS.md` ADR-022 replications, a second "Notify
ANC state" frame appeared 18 seconds after the first, with no new `08 11` Get frame in between, and
its `Settable-toggles` value flipped `0xe8`→`0x00` — genuinely open whether this reflects a real
dock-state change while the ACL connection stayed open (in tension with ADR-016's "ACL disconnects
the instant both buds are re-docked" finding) or a spontaneous, unprompted Notify unrelated to dock
state.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-048`                     |
|      Group(s)    |                   AD (repeat)                      |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, official Pixel Buds Companion App + GMS enabled) |
| Video file       |    TBD — must be a **continuous, timestamped video specifically of the Buds/case's own physical dock state**, not just the phone screen |
| Log file         |             TBD — `CAP-048-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AD)

1. Reproduce `CAP-037`'s own procedure (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AD): multiple
   isolated reconnects, alternating docked/undocked states, at least 20+ minutes total.
2. This time, keep a continuous, timestamped video specifically of the Buds/case's own physical
   dock state throughout (not just the phone screen) — the anomaly needs sub-second dock-state
   correlation that `CAP-037`'s own pass didn't have.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Session start, continuous dock-state video recording begins | User | `OBS-004` | Conn. state: TBD |
| TBD | Reconnect #1 (docked/undocked, record which) | User (Hardware) | `OBS-004` | TBD |
| TBD | Reconnect #2 | User (Hardware) | `OBS-004` | TBD |
| TBD | (continue for 20+ minutes, alternating docked/undocked — add rows as needed) | User (Hardware) | `OBS-004` | TBD |
| TBD | Session end | — | `OBS-004` | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AD)

- [ ] For each reconnect, confirm `08 11 00 00`/`08 13` fires and record its `Settable-toggles` byte
      against the known dock state.
- [ ] Specifically check for any `Settable-toggles` flip with no preceding `08 11` Get and no ACL
      disconnect nearby, matching `CAP-037-FINDINGS.md`'s own anomaly shape.
- [ ] If such a flip occurs, check the physical dock-state video at that exact wire timestamp — was
      a bud actually moved in/out of the case at that moment (reconciling with `DECISIONS.md`
      ADR-016), or not (a genuine open anomaly, now with video evidence either way)?
- [ ] A miss on any repeat is a counter-example to ADR-022; a `Settable-toggles` value that doesn't
      match dock state on any repeat is a counter-example to ADR-024 — either must be reported
      plainly, not reconciled away.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `OBS-004` is clearly referenced above.
- [ ] Write `CAP-048-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `OBS-004` row's Evidence column with a pointer
      once promoted into `PROTOCOL.md`.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-048-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AD/CAP-048-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-048-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AD/CAP-048-EVENT-NOTES
