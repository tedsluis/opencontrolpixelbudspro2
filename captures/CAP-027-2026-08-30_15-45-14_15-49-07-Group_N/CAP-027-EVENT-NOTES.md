# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group N, Touch gestures (`CAP-027`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-027-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_N` to the actual session
date/start-time/end-time, e.g. `CAP-027-2026-09-01_10-20-00_10-26-00-Group_N`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group N):** main run-through group, never yet
captured — physical touch gestures on the bud hardware itself (distinct from Group F's app-side
on/off toggles). **`TOUCH-007`'s behavior depends on the per-earbud press-and-hold configuration
set in Group G (`CAP-021`)** — note which mode (Toggle ANC vs. Digital assistant) was active for
whichever earbud is tested here.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-027`                     |
|      Group(s)    |                         N                          |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    | TBD (Pixel 7a, Android version, official app version) |
| Video file       |               TBD — `CAP-027-recording.mp4`        |
| Log file         |             TBD — `CAP-027-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group N — physical actions on the bud, either
phone)

7. **Tap once** on a bud [`TOUCH-002`]. Wait. Note time.
8. **Double-tap** on a bud [`TOUCH-003`]. Wait. Note time.
9. **Triple-tap** on a bud [`TOUCH-004`]. Wait. Note time.
10. **Swipe forward** on a bud (volume up) [`TOUCH-005`]. Wait. Note time.
11. **Swipe backward** on a bud (volume down) [`TOUCH-006`]. Wait. Note time.
12. **Press and hold** on a bud [`TOUCH-007`]. Wait. Note time. **Record which per-earbud
    press-and-hold mode was active** (from Group G, `CAP-021`).

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 15:45:14 | start of video
| 15:45:18 | user enables bluetooth
| 15:45:20 | Pixel buds pro 2 connected
| 15:45:30 | spotify app: user selects play, music starts playing 
| 15:45:39 | Tap once on right bud | User (Hardware) | `TOUCH-002` | TBD |
| 15:45:48 | swipe on right bud (unintentionaly)  
| 15:45:59 | Double-tap on right bud | User (Hardware) | `TOUCH-003` | TBD |
| 15:46:09 | Triple-tap on right bud | User (Hardware) | `TOUCH-004` | TBD |
| 15:46:24 | Triple-tap on right bud | User (Hardware) | `TOUCH-004` | TBD |
| 15:46:35 | Tap once on left bud | User (Hardware) | `TOUCH-002` | TBD |
| 15:46:44 | Tap once on left bud | User (Hardware) | `TOUCH-002` | TBD |
| 15:46:51 | Double-tap on left bud | User (Hardware) | `TOUCH-003` | TBD |
| 15:47:07 | Triple-tap + tap once on left bud | User (Hardware) | `TOUCH-004` | TBD |
| 15:47:14 | Tap once on left bud | User (Hardware) | `TOUCH-002` | TBD |
| 15:47:32 | Triple-tap + tap once on left bud | User (Hardware) | `TOUCH-004` | TBD |
| 15:47:36 | Tap once on left bud | User (Hardware) | `TOUCH-002` | TBD |
| 15:47:54 | Triple-tap on left bud | User (Hardware) | `TOUCH-004` | TBD |
| 15:48:07 | Swipe forward on right bud (volume up)| User (Hardware) | `TOUCH-005` | TBD |
| 15:48:17 | Swipe backward on right bud (volume down)| User (Hardware) | `TOUCH-006` | TBD |
| TDB | Press and hold on right bud (mode: TBD) | User (Hardware) | `TOUCH-007` | TBD |
| 15:48:39 | Swipe forward on left bud (volume up) | User (Hardware) | `TOUCH-005` | TBD |
| 15:48:44 | Swipe backward on left bud (volume down) | User (Hardware) | `TOUCH-006` | TBD |
| TBD | Press and hold on left bud (mode: TBD) | User (Hardware) | `TOUCH-007` | TBD |
| 15:49:07 | end of video

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [ ] These are hardware-initiated gestures — check whether they generate any RFCOMM/GATT traffic
      at all, or are purely on-device (media-key-style local reactions with no wire signal).
- [ ] If traffic is found, identify which DLCI/channel carries it.
- [ ] Compare structure against `PROTOCOL.md` §2's envelope hypotheses per the usual process.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `TOUCH-002`–`TOUCH-007` are clearly referenced above.
- [ ] Write `CAP-027-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-027-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_N/CAP-027-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-027-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_N/CAP-027-EVENT-NOTES
