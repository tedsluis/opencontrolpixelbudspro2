# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group Q items #19–20, Loud Noise Protection & Adaptive Audio observation (`CAP-030`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-030-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_Q` to the actual session
date/start-time/end-time, e.g. `CAP-030-2026-09-01_11-00-00_11-10-00-Group_Q`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q, items #19–20 specifically):** item #18
(passive BLE scan for the Battery Notification) is already planned separately as `CAP-011`. This
session covers the two remaining Group Q items: attempting to observe Loud Noise Protection and
Adaptive Audio engaging. These are waiting periods to catch spontaneous hardware-initiated
traffic — nothing to tap, just capture while the condition holds. Log explicit **observation
start** and **observation end** boundaries, not just a single timestamp.

**Three-way outcome guidance (required — "nothing found" is not a single outcome, per
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q):**
- **Local behavior confirmed + Bluetooth traffic observed** → record the frame(s) as
  `[VERIFIED-LOCAL]`.
- **Local behavior confirmed, but no Bluetooth traffic despite a clean observation window** →
  itself a positive finding (evidence *for* a purely on-device implementation) — record as such,
  don't leave the row blank.
- **Inconclusive** (unsure the local trigger actually fired, or the window was contaminated) →
  note as 🔴 unconfirmed and retry with a clearer trigger before drawing any conclusion.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-030`                     |
|      Group(s)    |                   Q (items #19–20)                 |
|       Date       |                        TBD                         |
| Firmware version |                        TBD (must be ≥4.467 for `LOUD-001`/`ADAPT-002` to exist at all) |
|   Test device    | TBD (Pixel 7a, Android version, official app version) |
| Video file       |               TBD — `CAP-030-recording.mp4`        |
| Log file         |             TBD — `CAP-030-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q, items 19–20)

19. **Trigger a loud, sudden sound near the buds while worn** [`LOUD-001`] (e.g. clap sharply
    nearby) to attempt to observe Loud Noise Protection engaging. Confirm the local effect
    (e.g. audible volume dip) actually occurred before concluding anything about the Bluetooth
    traffic. Does not cover impulse sounds (gunshots, fireworks) per Google.
20. **Move between distinctly different acoustic environments while worn** [`ADAPT-002`] (e.g.
    quiet room → street) to attempt to observe Adaptive Audio adjusting. Confirm the local effect
    first, same guidance as #19.

## Event Timeline

| Time | Action | Initiator | Test-ID | Local effect confirmed? | Wire evidence / Notes |
|---|---|---|---|---|---|
| TBD | Observation window 1 start (worn, ready for a loud sound) | — | `LOUD-001` | — | TBD |
| TBD | Loud/sudden sound triggered | User (Hardware) | `LOUD-001` | TBD | TBD |
| TBD | Observation window 1 end | — | `LOUD-001` | — | TBD |
| TBD | Observation window 2 start (worn, in first acoustic environment) | — | `ADAPT-002` | — | TBD |
| TBD | Moved to distinctly different acoustic environment | User (Hardware) | `ADAPT-002` | TBD | TBD |
| TBD | Observation window 2 end | — | `ADAPT-002` | — | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q)

- [ ] For each of `LOUD-001`/`ADAPT-002`, classify the outcome using the three-way guidance above
      — do not record a plain "no traffic found" without that classification.
- [ ] If traffic is found, identify which DLCI/channel carries it.
- [ ] Update `PROTOCOL.md` §4.5/§6 with whichever outcome is confirmed (currently listed there as
      "likely on-device DSP only, no wire-visible command expected" — a HYPOTHESIS this capture
      can confirm or refute either way).

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `LOUD-001`/`ADAPT-002` are clearly referenced above.
- [ ] Write `CAP-030-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-030-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_Q/CAP-030-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-030-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_Q/CAP-030-EVENT-NOTES
