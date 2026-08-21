# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group I, Firmware & device info (`CAP-023`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-023-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_I` to the actual session
date/start-time/end-time, e.g. `CAP-023-2026-09-01_09-40-00_09-45-00-Group_I`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group I):** main run-through group, never yet
captured — attributes the wire commands for the firmware/device-info screen. **Directly
relevant to `PROTOCOL.md` §0.1's still-open "wire-baseline firmware version" question** — this
is the one capture design that also records the app's own on-screen firmware-version display at
the same moment as the wire capture, per `PROTOCOL.md` §6's Framing open item.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-023`                     |
|      Group(s)    |                         I                          |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    | TBD (Pixel 7a, Android version, official app version) |
| Video file       |               TBD — `CAP-023-recording.mp4`        |
| Log file         |             TBD — `CAP-023-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group I)

31. **Tap the 'Firmware up to date' check** (manual) [`FW-001`]. Wait. Note time.
32. **Open 'More settings'** to view firmware version per component [`FW-002`] — may trigger a
    status query. Wait. Note time. **Record the exact on-screen firmware version string here.**
33. **View serial numbers per component** (same screen) [`FW-003`]. Wait. Note time.
34. **View connection status** ("Earbud status: Connected") [`FW-004`]. Wait. Note time.

Usual rhythm: wait ~5s → note the exact time → perform the action → wait ~5–10s → move to the
next action.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Tap 'Firmware up to date' check | User (App) | `FW-001` | TBD |
| TBD | Open 'More settings' — **on-screen firmware version string:** TBD | User (App, view) | `FW-002` | TBD |
| TBD | View serial numbers per component | User (App, view) | `FW-003` | TBD |
| TBD | View connection status | User (App, view) | `FW-004` | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [ ] Identify which DLCI/channel carries any status query triggered by opening 'More settings'.
- [ ] **Cross-check the on-screen firmware version string (recorded above) against every
      on-the-wire version-like string documented in `PROTOCOL.md` §0.1** (`"release_5.203"`,
      `"Revision 6"`, `"cape2_sm"`, `"500m"`–`"500p"`) — this is the specific check that resolves
      which (if any) of those is what the app itself calls "the firmware version".
- [ ] Compare structure against `PROTOCOL.md` §2's envelope hypotheses per the usual process.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `FW-001`–`FW-004` are clearly referenced above.
- [ ] Write `CAP-023-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] If the wire-baseline-firmware-version question is resolved, update `PROTOCOL.md` §0.1 and
      close the matching §6 open item (non-destructively, per `PROJECT_RULES.md` rule 9a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.
