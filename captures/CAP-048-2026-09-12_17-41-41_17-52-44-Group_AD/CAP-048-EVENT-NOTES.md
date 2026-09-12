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
| Video file       | `CAP-048-recording.mp4` (17:41:41 - 17:52:44)      |
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
| 17:41:41 | Session start. Both buds are in the open case. Bluetooth is OFF. | User | `OBS-004` | — |
| 17:41:43 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:41:48 | **Reconnect #1 (Docked)** | System | `OBS-004` | Active, L:100% R:100% Case:95% |
| 17:42:04 | User opens Pixel Buds app ("Device details"). | User (App) | — | — |
| 17:42:27 | Buds physically removed from case (undocked). | User (Hardware) | — | — |
| 17:42:37 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:42:41 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:42:46 | **Reconnect #2 (Undocked)** | System | `OBS-004` | — |
| 17:42:54 | Buds physically placed in case (docked). | User (Hardware) | — | — |
| 17:42:59 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:43:02 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:43:07 | **Reconnect #3 (Docked)** | System | `OBS-004` | — |
| 17:43:09 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:43:10 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:43:15 | **Reconnect #4 (Docked)** | System | `OBS-004` | **Deviation**: User did not undock buds before this reconnect. |
| 17:43:17 | Buds physically removed from case (undocked). | User (Hardware) | — | — |
| 17:43:20 | Bluetooth toggled ON (after being toggled OFF at 01:35). | User (Hardware) | `OBS-004` | — |
| 17:43:26 | **Reconnect #5 (Undocked)** | System | `OBS-004` | — |
| 17:43:29 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:43:31 | Buds physically placed in case (docked). | User (Hardware) | — | — |
| 17:43:33 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:43:39 | **Reconnect #6 (Docked)** | System | `OBS-004` | — |
| 17:43:43 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:43:45 | Buds physically removed from case (undocked). | User (Hardware) | — | — |
| 17:43:46 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:43:50 | **Reconnect #7 (Undocked)** | System | `OBS-004` | — |
| 17:44:08 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:44:09 | Buds physically placed in case (docked). | User (Hardware) | — | — |
| 17:44:15 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:44:19 | **Reconnect #8 (Docked)** | System | `OBS-004` | — |
| 17:44:40 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:44:41 | Buds physically removed from case (undocked). | User (Hardware) | — | — |
| 17:44:43 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:44:47 | **Reconnect #9 (Undocked)** | System | `OBS-004` | — |
| 17:45:13 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:45:14 | Buds physically placed in case (docked). | User (Hardware) | — | — |
| 17:45:17 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:45:22 | **Reconnect #10 (Docked)** | System | `OBS-004` | — |
| 17:45:38 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:45:39 | Buds physically removed from case (undocked). | User (Hardware) | — | — |
| 17:45:43 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:45:47 | **Reconnect #11 (Undocked)** | System | `OBS-004` | — |
| 17:46:15 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:46:16 | Buds physically placed in case (docked). | User (Hardware) | — | — |
| 17:46:19 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:46:23 | **Reconnect #12 (Docked)** | System | `OBS-004` | — |
| 17:46:35 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:46:35 | Buds physically removed from case (undocked). | User (Hardware) | — | — |
| 17:46:38 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:46:41 | **Reconnect #13 (Undocked)** | System | `OBS-004` | — |
| 17:46:44 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:46:45 | Buds physically placed in case (docked). | User (Hardware) | — | — |
| 17:46:49 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:46:53 | **Reconnect #14 (Docked)** | System | `OBS-004` | — |
| 17:47:12 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:47:13 | Buds physically removed from case (undocked). | User (Hardware) | — | — |
| 17:47:17 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:47:21 | **Reconnect #15 (Undocked)** | System | `OBS-004` | — |
| 17:47:37 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:47:38 | Buds physically placed in case (docked). | User (Hardware) | — | — |
| 17:47:42 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:47:47 | **Reconnect #16 (Docked)** | System | `OBS-004` | — |
| 17:48:10 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:48:10 | Buds physically removed from case (undocked). | User (Hardware) | — | — |
| 17:48:25 | Buds physically placed back in case (docked). | User (Hardware) | — | User fidgeted with buds while BT was off. |
| 17:48:42 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:48:47 | **Reconnect #17 (Docked)** | System | `OBS-004` | **Deviation**: User did not reconnect while undocked here. |
| 17:49:06 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:49:07 | Buds physically removed from case (undocked). | User (Hardware) | — | — |
| 17:49:11 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:49:15 | **Reconnect #18 (Undocked)** | System | `OBS-004` | — |
| 17:49:36 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:49:37 | Buds physically placed in case (docked). | User (Hardware) | — | — |
| 17:49:41 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:49:46 | **Reconnect #19 (Docked)** | System | `OBS-004` | — |
| 17:50:10 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:50:11 | Buds physically removed from case (undocked). | User (Hardware) | — | — |
| 17:50:14 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:50:18 | **Reconnect #20 (Undocked)** | System | `OBS-004` | — |
| 17:50:31 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:50:33 | Buds physically placed in case (docked). | User (Hardware) | — | — |
| 17:50:37 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:50:41 | **Reconnect #21 (Docked)** | System | `OBS-004` | — |
| 17:51:09 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:51:10 | Buds physically removed from case (undocked). | User (Hardware) | — | — |
| 17:51:14 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:51:19 | **Reconnect #22 (Undocked)** | System | `OBS-004` | — |
| 17:51:37 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:51:38 | Buds physically placed in case (docked). | User (Hardware) | — | — |
| 17:51:43 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:51:47 | **Reconnect #23 (Docked)** | System | `OBS-004` | — |
| 17:52:03 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:52:04 | Buds physically removed from case (undocked). | User (Hardware) | — | — |
| 17:52:08 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:52:12 | **Reconnect #24 (Undocked)** | System | `OBS-004` | — |
| 17:52:21 | Bluetooth toggled OFF. | User (Hardware) | `OBS-004` | — |
| 17:52:22 | Buds physically placed in case (docked). | User (Hardware) | — | — |
| 17:52:26 | Bluetooth toggled ON. | User (Hardware) | `OBS-004` | — |
| 17:52:30 | **Reconnect #25 (Docked)** | System | `OBS-004` | — |
| 17:52:44 | Session end | — | `OBS-004` | Video ends. |
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
