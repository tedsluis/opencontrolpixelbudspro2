# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AP (new), Battery Notification: bracket a single-bud insertion/removal, connection-free (`CAP-054`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-054-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AP` to the actual session
date/start-time/end-time, e.g. `CAP-054-2026-09-15_08-30-00_08-40-00-Group_AP`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AP, added 2026-09-13,
`ai-sessions/0017_MAINTENANCE_RESULT_2026_09_13.md` Phase 2):** `CAP-043` (Group Q repeat)
established, under rigorously clean connection-free isolation, that the Buds' idle/case-closed
`0xFE2C` BLE advertisement does not structurally match `PROTOCOL.md` §4.3 Option A's documented
Battery Notification layout — but only tested the idle/case-closed condition. The official Fast Pair
spec itself describes the Battery Notification extension as "**optional** when a single bud is
inserted/removed" — a materially different trigger condition, not yet bracketed by any capture to
date.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-054`                     |
|      Group(s)    |                     AP (new)                       |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, official Pixel Buds Companion App **force-stopped** for the entire session, matching `CAP-043`'s methodology) |
| Video file       |    TBD — must show the system Bluetooth settings panel and the exact bud removal/re-insertion moments |
| Log file         |             TBD — `CAP-054-btsnoop_hci.log` |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD (expected: not observed, per `CAP-043`'s own isolation — no classic connection this session)             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AP)

1. Force-stop the official Pixel Buds app (as `CAP-043` did) and confirm, before starting the log,
   that no classic RFCOMM connection to the Buds is active (system Bluetooth settings showing
   "not connected," or the Buds already disconnected).
2. Start HCI snoop logging and a screen/phone-camera recording of the system Bluetooth settings
   panel (matching `CAP-043`'s own methodology).
3. With the case closed and the phone otherwise idle, **remove exactly one earbud from the case**
   (video-confirm the exact removal moment), then wait ≥15s without touching anything else.
4. **Re-insert that same earbud** into the case (video-confirm), wait ≥15s again.
5. Repeat steps 3–4 once more for the **other** earbud, as an independent second sample.
6. Throughout, avoid opening the official app or making any classic RFCOMM connection — per
   `AGENTS.md` §7's bounded scanning exception (filtered to the bonded device, foreground-triggered,
   time-boxed).

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Session start, app confirmed force-stopped, no active connection | User | — | Conn. state: TBD |
| TBD | Earbud 1 (Left/Right — specify) removed from case, video-confirmed | User (Hardware) | `CASE-004`/`CASE-005` | TBD |
| TBD | Wait ≥15s, nothing touched | — | — | TBD |
| TBD | Earbud 1 re-inserted into case, video-confirmed | User (Hardware) | — | TBD |
| TBD | Wait ≥15s, nothing touched | — | — | TBD |
| TBD | Earbud 2 (the other one) removed from case, video-confirmed | User (Hardware) | `CASE-004`/`CASE-005` | TBD |
| TBD | Wait ≥15s, nothing touched | — | — | TBD |
| TBD | Earbud 2 re-inserted into case, video-confirmed | User (Hardware) | — | TBD |
| TBD | Wait ≥15s, nothing touched | — | — | TBD |
| TBD | Session end | — | — | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AP)

- [ ] Isolation check (per `CAP-043`'s own method): confirm zero classic connection, zero RFCOMM,
      zero SDP to the Buds' known classic address anywhere in this log.
- [ ] For each of the 4 bracketed events (2 removals + 2 insertions), does a `0xFE2C` service-data
      advertisement carrying the documented Battery Notification layout
      (`[Flags=0x00][Account Key Data][0x33/0x34 marker][L][R][Case]`) appear within a few seconds?
- [ ] If still a clean negative across all 4 events, record that plainly — this strengthens the case
      for reframing `PROTOCOL.md` §4.3 Option A's status, per `CAP-043-FINDINGS.md` §7's own
      recommendation (a maintainer decision, not to be made unilaterally here).
- [ ] If a positive match is found, decode the payload per the documented layout and cross-reference
      against `AGENTS.md` §13.6's zero-creativity rule.

## Next steps after filling this in

- [ ] Cross-reference this session's own findings against `PROTOCOL.md` §4.3 Option A and
      `CAP-043-FINDINGS.md` (`AGENTS.md` §13's traceability check).
- [ ] Write `CAP-054-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-054-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AP/CAP-054-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-054-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AP/CAP-054-EVENT-NOTES
