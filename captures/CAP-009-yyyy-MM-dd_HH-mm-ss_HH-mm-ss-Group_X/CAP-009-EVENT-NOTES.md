# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group X, Battery-level discrepancy bracket (`CAP-009`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-009-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_X` to the actual session
date/start-time/end-time, e.g. `CAP-009-2026-09-01_08-00-00_18-00-00-Group_X`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group X):** `CAP-001-FINDINGS.md` §3 found
`AT+CIND?`'s `battchg=3` (≈60%) and `AT+BIEV=2,100` (100%) disagreeing at the same moment —
unresolved whether either indicator actually tracks a real battery-level change over time.
**Can be combined with `CAP-008`'s (Group V) session** if a phone call happens to occur naturally
within this window — no need to force it, just note it if it happens.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-009`                     |
|      Group(s)    |                         X                          |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    | TBD (Pixel 7a/9a, Android version, official app version if used) |
| Video file       |          TBD — n/a for this Group (long-duration, no continuous recording expected — see note below) |
| Log file         |             TBD — `CAP-009-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

**Note on video:** this is a multi-hour passive-observation session — a continuous screen
recording is not expected/required the way it is for a short, action-isolated capture. Note the
on-screen battery percentage manually at each periodic check instead (see timeline below).

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group X)

1. Start logging well before an expected, natural battery-level decline (e.g. at the start of a
   normal day of use), keeping the connection active/idle rather than disconnecting between
   checks.
2. Periodically (e.g. every 15–30 minutes) note the wall-clock time — no action needed, both
   indicators are expected to update on their own per their respective triggers.
3. End the session after a natural, visible battery-percentage drop has occurred on screen.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Session start, on-screen battery % noted | — | `BATT-006` | TBD |
| TBD | Periodic check #1, on-screen battery % noted | — | `BATT-006` | TBD |
| TBD | Periodic check #2, on-screen battery % noted | — | `BATT-006` | TBD |
| ... | (add rows as needed, one per periodic check) | — | `BATT-006` | TBD |
| TBD | Session end — visible battery-percentage drop confirmed on screen | — | `BATT-006` | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group X)

- [ ] Extract every `AT+CIND?` (`battchg`) value across the session with its timestamp.
- [ ] Extract every `AT+BIEV=2,...` value across the session with its timestamp.
- [ ] Compare both trends against the on-screen battery percentage noted at each periodic check
      above.
- [ ] Determine whether either indicator (or neither) tracks the real level change accurately.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `BATT-006` is clearly referenced above, not silently missing.
- [ ] Write `CAP-009-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.
