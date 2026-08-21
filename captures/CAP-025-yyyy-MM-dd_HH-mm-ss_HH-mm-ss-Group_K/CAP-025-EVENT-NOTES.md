# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group K, Find My Buds (`CAP-025`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-025-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_K` to the actual session
date/start-time/end-time, e.g. `CAP-025-2026-09-01_10-00-00_10-05-00-Group_K`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group K):** main run-through group, never yet
captured — the last remaining fully unattributed command in the app's action list (ANC's channel
is already independently confirmed, so this is no longer the top framing-verification priority,
but the Find My Buds/Ring command itself is still unattributed). `PROTOCOL.md` §4.4 has a
concrete, testable hypothesis ready to check: Fast Pair Message Stream Action group (`0x04`),
Ring code (`0x01`), spec worked example `0xFF 0x01 0x00 0x02 0x04 0x01` (ACK).

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-025`                     |
|      Group(s)    |                         K                          |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    | TBD (Pixel 7a, Android version, official app version) |
| Video file       |               TBD — `CAP-025-recording.mp4`        |
| Log file         |             TBD — `CAP-025-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group K)

38. **Play sound on Left earbud** [`FIND-001`]. Wait. Note time.
39. **Play sound on Right earbud** [`FIND-002`]. Wait. Note time.
40. **Play sound on Case** [`FIND-003`]. Wait. Note time.
41. **Play sound on both earbuds simultaneously** [`FIND-004`]. Wait. Note time.

Usual rhythm: wait ~5s → note the exact time → perform the action → wait ~5–10s → move to the
next action. **Before treating the framing hypothesis as confirmed from #38 alone: cross-check
against 2–3 semantically different commands** — e.g. an ANC mode change or an EQ write, if
convenient to also exercise this session — one matching frame is a HYPOTHESIS, not a FACT.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Play sound on Left earbud | User (App) | `FIND-001` | TBD |
| TBD | Play sound on Right earbud | User (App) | `FIND-002` | TBD |
| TBD | Play sound on Case | User (App) | `FIND-003` | TBD |
| TBD | Play sound on both earbuds simultaneously | User (App) | `FIND-004` | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group K / §5)

- [ ] Check every frame against `PROTOCOL.md` §4.4's exact hypothesis: Message Stream Action
      group (`0x04`), Ring code (`0x01`), expecting an ACK `0xFF 0x01 0x00 0x02 0x04 0x01`.
- [ ] Check whether Left/Right/Case are distinguished via a payload field, separate codes, or
      separate DLCIs entirely.
- [ ] Cross-check structural elements (magic/group byte(s), length-field semantics,
      channel/message-ID position, checksum presence) against 2–3 other already-confirmed
      commands (e.g. ANC's `0x12` Set frame) before promoting past HYPOTHESIS.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `FIND-001`–`FIND-004` are clearly referenced above.
- [ ] Write `CAP-025-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] If confirmed, promote `PROTOCOL.md` §4.4 from 🟡 HYPOTHESIS to 🟢 FACT (maintainer sign-off
      required per `AGENTS.md` §6 — an agent must not do this promotion unilaterally).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.
