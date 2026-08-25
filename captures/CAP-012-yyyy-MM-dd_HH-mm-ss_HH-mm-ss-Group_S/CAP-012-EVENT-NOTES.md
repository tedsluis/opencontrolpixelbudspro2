# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group S repeat, clean GMS-disabled/no-app procedure (`CAP-012`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-012-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_S` to the actual session
date/start-time/end-time, e.g. `CAP-012-2026-09-01_10-00-00_10-10-00-Group_S`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group S, repeat):** repeat of Group S following its
**original system-settings-only procedure exactly (no nRF Connect at any point)**, to isolate
whether `CAP-004`'s Cross-Transport Key Derivation bonding result was an artifact of nRF
Connect's early BLE connection, or a genuine effect of GMS being disabled
(`CAP-004-FINDINGS.md` §8 item 4). The core `GFPS-001` result itself is **not** expected to
change — only the §2-equivalent CTKD-vs-classic-SSP bonding-mechanism finding.

**Setup (must be validated before capturing, per Group S's own procedure):** with the Pixel Buds
app uninstalled and Google Play Services disabled (Settings → Apps → see all apps → Google Play
Services → Disable; `adb shell pm disable-user --user 0 com.google.android.gms` is the scriptable
equivalent), confirm pairing via system Bluetooth settings still succeeds and that the Fast Pair
"Connect" half-sheet does **not** appear.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-012`                     |
|      Group(s)    |                    S (repeat)                      |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |            TBD (Pixel 7a, GMS disabled, Pixel Buds app uninstalled — no BLE tool used at any point) |
| Video file       |               TBD — `CAP-012-recording.mp4`        |
| Log file         |             TBD — `CAP-012-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

**Deviation check (required):** confirm and record explicitly whether this session, unlike
`CAP-004`, avoided nRF Connect or any other BLE tool entirely — this is the whole point of the
repeat, so any deviation must be noted here, not silently glossed over.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group S)

1. Confirm the Pixel Buds app is uninstalled and Google Play Services is disabled, per the setup
   note above.
2. Work through §2 (enable HCI snoop, restart Bluetooth/reboot) as usual.
3. **Pair via system Bluetooth settings** [`GFPS-001`] — no app, no BLE tool. Note whether the
   device was already unpaired or whether this capture also includes a fresh bonding handshake.
4. **Isolate the whole pair-and-settle sequence as one action window**: note the exact connect-tap
   time and when the connection visibly settles.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Setup confirmed (GMS disabled, app uninstalled, no BLE tool) | — | — | TBD |
| TBD | HCI snoop enabled, Bluetooth restarted | — | — | TBD |
| TBD | Pair via system Bluetooth settings (tap device in list) | User (Hardware) | `GFPS-001`, `PAIR-001` | TBD |
| TBD | Connection visibly settles | App (Auto) | — | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group S)

- [ ] Check whether a channel/DLCI carrying the same `[Group][Code][Length][Value]` framing as
      `CAP-002` §3 appears at all.
- [ ] If present, check whether the same fields (e.g. Code `0x09`'s value) match `CAP-002`'s.
      **Do not assume an outcome before analyzing** — either result is a real, useful finding.
- [ ] Classic bonding mechanism: does this session use CTKD (as `CAP-004` did) or classic SSP (as
      `CAP-002`/`CAP-003` did)? This is the specific confound this repeat is meant to isolate.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `GFPS-001`/`PAIR-001` are clearly referenced above.
- [ ] Write `CAP-012-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-012-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_S/CAP-012-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-012-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_S/CAP-012-EVENT-NOTES
