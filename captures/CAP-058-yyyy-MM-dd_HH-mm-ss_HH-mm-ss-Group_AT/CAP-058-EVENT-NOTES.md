# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AT (new), `SDP-001` 3rd attempt with a process-liveness check (`CAP-058`)

**Status:** 🔲 **Not yet captured — skeleton only.** Created 2026-09-24 (`ai-sessions/0045`, closing 0044 finding D-8: `CAP-058` was
registered `planned` on 2026-09-18 by `ai-sessions/0031` but, unlike `CAP-052`–`CAP-057`, had no placeholder folder). Fill in every
`TBD` below after recording, per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and `PROJECT_RULES.md`
rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from the placeholder
`CAP-058-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AT` to the actual session date/start-time/end-time.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AT, maintainer-approved 2026-09-18, `ai-sessions/0031`):** two prior attempts at
`SDP-001` (`CAP-033`, `CAP-044`) were each capped at 🟡 HYPOTHESIS by a procedural gap (`CAP-033`: Forget before Force-stop; `CAP-044`:
no confirmed process-dead window before the "Pair" tap). This attempt adds an explicit on-device process-liveness check immediately
before the "Pair" tap (`CAP-044-FINDINGS.md` §5).

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-058`                     |
|      Group(s)    |                     AT (new)                       |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, official Pixel Buds Companion App force-stopped for the first half) |
| Video file       |    TBD — must show the process-liveness check and the "Pair" tap |
| Log file         |             TBD — `CAP-058-btsnoop_hci.log` (raw path, not `btsnooz.py`) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AT)

1. **[`SDP-001`]** Force-stop the companion app, then Forget the device — in that order.
2. Immediately before tapping "Pair", run the process-liveness check (e.g. `adb shell dumpsys activity processes | grep -i pixelbuds`)
   on camera or in a parallel shell capture; it must show the companion app process absent up to and including the SDP browse.
3. Re-pair through Bluetooth settings only.
4. **Second half:** open the companion app normally and let its own `fetchUuidsWithSdp()` re-fetch run.
5. **[`SDP-002`]** only if a firmware update happens to be pending (opportunistic).

## Event timeline

| Wall clock | Action | Test-ID | Wire evidence |
|---|---|---|---|
| TBD | Force-stop companion app | `SDP-001` | — |
| TBD | Forget the Buds | `SDP-001` | TBD |
| TBD | Process-liveness check (app absent) | `SDP-001` | — |
| TBD | "Pair" tap → SDP browse | `SDP-001` | TBD |
| TBD | Open the companion app → re-fetch | `SDP-001` | TBD |

## Decode / analysis checklist

- Pre-filter by the Buds' address (`AGENTS.md` §13), then `btsdp`: does the "default internal rfcomm socket" UUID
  (`3a046f6d-24d2-7655-6534-0d7ecb759709`, either byte order) appear during the confirmed process-dead window?
- Three-way outcome as in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA; nothing is promoted without maintainer sign-off (`AGENTS.md` §6).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-058-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AT/CAP-058-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-058-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AT/CAP-058-EVENT-NOTES
