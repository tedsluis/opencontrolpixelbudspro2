# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group A repeat, logging started before any prior association (`CAP-013`)

**Status:** 🔲 **Not yet captured — skeleton only, optional/lower priority.** Fill in every `TBD`
below after recording, per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to
update), and `PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this
folder from the placeholder `CAP-013-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_A` to the actual session
date/start-time/end-time, e.g. `CAP-013-2026-09-01_07-00-00_07-10-00-Group_A`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group A repeat, optional, added 2026-08-14):**
`CAP-001-FINDINGS.md` §6 found a BLE link and a still-valid link key both existing *before* the
on-screen "Forget" tap and before the case was reopened — unresolved whether "Forget" fully
clears prior bonding/BLE-association state. A repeat of Group A that starts HCI snoop logging
**before** any association with the device exists at all (e.g. immediately after a phone
restart, before ever opening the case or any Buds app) would isolate this. **Optional, lower
priority than `CAP-008`/`CAP-009`/`CAP-011`/`CAP-012`/`CAP-014` above.**

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-013`                     |
|      Group(s)    |                    A (repeat)                      |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    | TBD (Pixel 7a — must be freshly restarted before any Bluetooth/Buds-app activity this session) |
| Video file       |               TBD — `CAP-013-recording.mp4`        |
| Log file         |             TBD — `CAP-013-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

**Pre-condition (required, this is the whole point of the repeat):** the phone must be restarted,
and HCI snoop logging enabled/Bluetooth restarted, **before** the case is ever opened or any Buds
app is ever opened this session — confirm and record explicitly that no prior association exists
at capture start.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group A, with the "before any association"
pre-condition above)

1. **Pairing / bonding baseline** [`PAIR-001`] — capture as its own isolated session. If the Buds
   are already paired, "forget" the device on the phone side first (Bluetooth settings → the
   paired device → Forget), then re-pair through Bluetooth settings. Expect a burst, not a single
   frame — pairing is one user action triggering an automatic multi-step exchange (inquiry,
   authentication, link-key exchange, SDP, profile connect).

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|----------|---|---|---|---|
| 17:09:01 | start of video CAP-012-recording.mp4. | - | - | - |
| 17:09:24 | user performed a "reset bluetooth & wifi" (reset options). | - | - | - |
| 17:09:24 | user performed a "reset bluetooth & wifi" (reset options). | - | - | - |
| 17:10:43 | user enabled bluetooth. | - | - | - |
| 17:10:53 | user opened buds case. | - | - | - |
| 17:11:14 | user presses the pair button on the buds case.. | - | - | - |
| 17:11:25 | user selects 'pair new device'. | - | - | - |
| 17:11:26 | pair new device: phone's bluetooth address E8:D5::2B:7E:CA:81 is displayed. | - | - | - |
| 17:11:27 | "pixel Buds Pro 2 van Ted" shows up in 'Available devices' list. | - | - | - |
| 17:11:31 | Pop-up appears: "pixel Buds Pro 2". | - | - | - |
| 17:11:45 | user selects "connect" from pop-up "Pair with Pixel Buds Pro 2". | - | - | - |
| 17:11:53 | "ready to use" pop-up appears. | - | - | - |
| 17:12:09 | user selects "start using the device", Pixel buds pro 2 are paired. | - | - | - |
| 17:12:10 | "set up device" pop-up appears. | - | - | - |
| 17:12:31 | user selects "set up", "allow a connection to your pixel buds" appears. | - | - | - |
| 17:12:45 | user selects continue. "Allow your pixel to find" pop-up  appears. | - | - | - |
| 17:12:52 | user selects allow, "allow the pixel buds app to access the Pixel Buds pro 2 Bud van Ted" pop-up appears. | - | - | - |
| 17:13:02 | user selects allow.. | - | - | - |
| 17:13:27 | firmware release_5.203 was show. | - | - | - |
| 17:14:04 | end of video CAP-012-recording.mp4. | - | - | - |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group A repeat note)

- [ ] Confirm no BLE link or valid link key exists for this device anywhere in the log **before**
      the first on-screen pairing/"Forget" action this session (the specific gap `CAP-001` found).
- [ ] If a prior BLE association is found even with logging starting this early, that would mean
      "Forget" (or a phone restart) does not fully clear it — a stronger result than `CAP-001`'s.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `PAIR-001`/`PAIR-004` are clearly referenced above.
- [ ] Write `CAP-013-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-013-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_A/CAP-013-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-013-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_A/CAP-013-EVENT-NOTES
