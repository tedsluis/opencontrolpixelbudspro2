# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group Q (repeat), Battery Notification BLE scan, connection-free (`CAP-043`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-043-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_Q` to the actual session
date/start-time/end-time, e.g. `CAP-043-2026-09-15_08-30-00_08-32-00-Group_Q`.

**Purpose (repeat of `CAP-011`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q item #18):**
`PROTOCOL.md` §4.3 Option A's Fast Pair Battery Notification mechanism is still 🟡 HYPOTHESIS for
the Buds Pro 2 specifically — `CAP-011` (2026-08-21) found the `0xFE2C` Fast Pair Service BLE
advertisement present, but the sampled payloads did not structurally match the documented byte
layout (no `0x33`/`0x34` Length&Type marker at any offset), and the session had a procedure
deviation: an active classic RFCOMM+GATT connection was present throughout (the official app was
left open on "Device details"), which this repeat must avoid. This session closes
`CAP-011-FINDINGS.md` §4's own open item either way (a structural match, or a second confirmed
non-match worth re-examining the hypothesis itself).

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-043`                     |
|      Group(s)    |                    Q (repeat)                      |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    | TBD (either phone; official Pixel Buds Companion App force-stopped and no other app holding an active classic Bluetooth connection or GATT session throughout) |
| Video file       |          TBD — optional (nothing to visually confirm beyond "buds/case were not touched, no app opened") |
| Log file         |             TBD — `CAP-043-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

**Isolation check (required — this is the whole point of the repeat):** confirm and record
explicitly that the official Pixel Buds Companion App was force-stopped (not just backgrounded)
and that no other app held an active classic Bluetooth connection or GATT session to the Buds at
any point during the capture — the specific procedure deviation `CAP-011` had.

## Procedure (repeat of `CAP-011`, per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q item #18)

1. Ensure the official Pixel Buds Companion App is **not running** (force-stop it) and no other
   app holds an active classic Bluetooth connection or GATT session to the Buds for the entire
   capture. Confirm via Settings → Apps.
2. With the Buds already bonded (do not re-pair), start HCI snoop logging, then passively wait
   (case closed and idle, or worn but with no in-app interaction) for at least 60–90 seconds —
   long enough to observe multiple Battery Notification advertisement cycles per `PROTOCOL.md`
   §4.3 Option A.
3. Confirm via `bthci_evt`/`android.bluetooth` logs (or simply by not touching the phone) that no
   classic RFCOMM connection was ever established during the window.
4. Stop logging; extract per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3, preferring the raw
   `btsnoop_hci.log` path over the lossy `btsnooz.py`-from-bugreport fallback (per `TODO.md`'s own
   "Known technical debt" note on extraction-path truncation).

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Official app force-stopped, confirmed via Settings → Apps | User | `BATT-002`, `BATT-003` | Conn. state: TBD |
| TBD | HCI snoop logging started (Buds already bonded, not re-paired) | User | `BATT-002`, `BATT-003` | TBD |
| TBD | Passive observation window start (case closed/idle, or worn, no in-app interaction) | — | `BATT-002`, `BATT-003` | TBD |
| TBD | (any event of interest during window) | Buds/Case (Auto) | `BATT-002`, `BATT-003` | TBD |
| TBD | Passive observation window end (≥60–90s) | — | `BATT-002`, `BATT-003` | TBD |
| TBD | Confirmed no classic RFCOMM connection was ever established during the window | — | `BATT-002`, `BATT-003` | TBD |
| TBD | Logging stopped, extracted via raw path | — | — | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q item #18 / `CAP-011-FINDINGS.md` §4)

- [ ] Confirm the isolation held: zero classic RFCOMM connection anywhere in the log.
- [ ] Filter for `0xFE2C` service-data BLE advertisements (`btle`, not `btrfcomm`).
- [ ] Check every sampled payload against `PROTOCOL.md` §4.3 Option A's documented byte layout
      (Flags / Account Key Data / Battery-level-length-&-type byte [`0x33` show / `0x34` hide] /
      three battery-percentage octets).
- [ ] Record the result explicitly either way: a structural match promotes Option A toward FACT; a
      second confirmed non-match (this being the second connection-free-or-not attempt) is itself a
      reportable finding worth re-examining the hypothesis, not a failed session.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `BATT-002`/`BATT-003` are clearly referenced above.
- [ ] Write `CAP-043-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `BATT-002`/`BATT-003` rows' Evidence column with a
      pointer once promoted into `PROTOCOL.md`.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-043-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_Q/CAP-043-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-043-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_Q/CAP-043-EVENT-NOTES
