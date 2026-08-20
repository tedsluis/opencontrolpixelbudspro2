# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group Q #18, Passive BLE scan for Battery Notification (`CAP-011`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-011-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_Q` to the actual session
date/start-time/end-time, e.g. `CAP-011-2026-09-01_09-00-00_09-05-00-Group_Q`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q, item #18):** capture the Fast Pair "Battery
Notification" BLE advertisement (`PROTOCOL.md` §4.3 Option A) independently of any active RFCOMM
connection, to confirm the officially specified 3-byte L/R/Case payload byte-for-byte against the
Buds Pro 2's actual advertisement. **This is a one-off, manual reverse-engineering capture, not a
template for the app** — the production app's own BLE scanning stays governed separately, and
more narrowly, by the bounded exception in `AGENTS.md` §7 / `DECISIONS.md` ADR-006. **Do not
combine this session with RFCOMM framing analysis.**

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-011`                     |
|      Group(s)    |                    Q (item #18)                    |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |         TBD (either phone — Pixel 7a or 9a)        |
| Video file       |          TBD — optional for this Group (passive scan, nothing to visually confirm beyond the case being closed/idle) |
| Log file         |             TBD — `CAP-011-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q #18)

1. **Passive BLE scan while the case is closed and idle** [`BATT-002`/`BATT-003`] — this does not
   require the buds to be connected to the capturing phone at all; any nearby scan should do, per
   the spec. Note the observation **start** and **end** time explicitly (same boundary-logging
   discipline as Group L/Q's other observation windows), not just a single timestamp.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Observation window start (case closed, idle) | — | `BATT-002`, `BATT-003` | TBD |
| TBD | Battery Notification advertisement observed (if any) | Buds/Case (Auto) | `BATT-002`, `BATT-003` | TBD |
| TBD | Observation window end | — | `BATT-002`, `BATT-003` | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q #18 / §5)

- [ ] Filter for `btle` advertising reports (not `btrfcomm` — this is BLE Link Layer traffic, a
      different structure entirely from the RFCOMM envelope hypotheses).
- [ ] Compare the advertisement payload against `PROTOCOL.md` §4.3 Option A's structure: Flags
      octet, Account Key Data, battery-level-length/type byte, then the 3 battery bytes
      (L/R/Case, each `0bSVVVVVVV`).
- [ ] Confirm visibility duration (≥8s when shown, auto-hidden after 20s or via explicit "hide").
- [ ] Mark the result `[VERIFIED-LOCAL]` in `PROTOCOL.md` §4.3 Option A once confirmed.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `BATT-002`/`BATT-003` are clearly referenced above.
- [ ] Write `CAP-011-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.
