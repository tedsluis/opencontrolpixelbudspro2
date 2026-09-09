# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AF (repeat), unexplained disconnect/reconnect cycling (`CAP-049`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-049-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AF` to the actual session
date/start-time/end-time, e.g. `CAP-049-2026-09-15_08-30-00_08-40-00-Group_AF`.

**Purpose (repeat of `CAP-039`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AF):** `CAP-039-FINDINGS.md`
§6 found the classic ACL connection disconnected and reconnected 5 times across a single ~6-minute
session with no clearly camera-visible trigger for most of them (4 of 5 locally terminated, reason
`0x16`) — genuinely open what caused the repeated cycling.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-049`                     |
|      Group(s)    |                   AF (repeat)                      |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, official Pixel Buds Companion App + GMS enabled) |
| Video file       |    TBD — **continuous** phone-screen recording throughout, not just at action moments |
| Log file         |             TBD — `CAP-049-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AF)

1. Reproduce `CAP-039`'s own general setup (Settable-toggles Set-vs-Get comparison at a fixed dock
   state, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AF) but this time with the phone screen recorded
   continuously throughout (not just at action moments) so any background system event (a
   Bluetooth-settings toggle, a notification, a screen-off/on cycle) is video-visible.
2. If the cycling reproduces, note the phone's own state (screen on/off, any visible notification,
   any app in foreground) at each disconnect/reconnect moment.
3. If it does *not* reproduce, that is itself a useful negative result — record it as such rather
   than treating the session as failed.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Session start, continuous phone-screen recording begins | User | `OBS-006` | Conn. state: TBD |
| TBD | ANC mode tap (Set) | User (App) | `OBS-006` | TBD |
| TBD | Forced reconnect via Bluetooth toggle (Get) | User (Hardware) | `OBS-006` | TBD |
| TBD | (any disconnect/reconnect event — add a row per occurrence, noting phone state: screen on/off, notification visible, foreground app) | — | `OBS-006` | TBD |
| TBD | Session end | — | `OBS-006` | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AF)

- [ ] Correlate each `Disconnection Complete`/reconnect pair against the continuous phone-screen
      video to identify (or rule out) a phone-side trigger.
- [ ] For each disconnect, record the phone's own state (screen on/off, any visible notification,
      any app in foreground) at that exact moment.
- [ ] If the cycling does not reproduce at all, record that explicitly as a useful negative result,
      not a failed session.
- [ ] Decode both the Set-triggered and Get-triggered Notify frames' `Settable-toggles` byte and
      compare directly (this Group's original `OBS-006` question), per `CAP-039-EVENT-NOTES.md`'s
      Decode/Analysis checklist.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `OBS-006` is clearly referenced above.
- [ ] Write `CAP-049-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `OBS-006` row's Evidence column with a pointer
      once promoted into `PROTOCOL.md`.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-049-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AF/CAP-049-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-049-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AF/CAP-049-EVENT-NOTES
