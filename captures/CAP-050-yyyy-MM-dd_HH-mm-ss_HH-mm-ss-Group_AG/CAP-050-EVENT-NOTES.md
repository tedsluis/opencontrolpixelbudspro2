# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AG (repeat), `CAP-040` with a genuine trigger (`CAP-050`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-050-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AG` to the actual session
date/start-time/end-time, e.g. `CAP-050-2026-09-15_08-30-00_08-50-00-Group_AG`.

**Purpose (repeat of `CAP-040`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AG):** `CAP-040-FINDINGS.md`
§3's own correlation attempt for DLCI 0x08's 7 unmapped zero-length `[Group][Code][00 00]`-shaped
frames failed because the session used the app's own in-app "Connect"/"Disconnect" buttons as the
bracketing trigger — but `CAP-040-FINDINGS.md` §1 separately found those buttons produce **zero
wire-visible signal** at all, invalidating the intended correlation (DLCI 0x08 opened exactly once,
so all 7 codes had only one sample each).

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-050`                     |
|      Group(s)    |                   AG (repeat)                      |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, official Pixel Buds Companion App + GMS enabled, app open on Device details for on-screen battery/case values) |
| Video file       |    TBD — must show on-screen Left/Right/Case values at each reconnect |
| Log file         |             TBD — `CAP-050-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

**Trigger check (required — this is the whole point of the repeat):** confirm and record that the
trigger used actually reopens DLCI 0x08 (the OS-level Bluetooth toggle, or physical case/bud
docking/undocking) — explicitly **not** the app's own in-app Connect/Disconnect buttons, which
`CAP-040` found produce zero wire signal.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AG)

1. Use a trigger confirmed to actually reopen DLCI 0x08 — either the OS-level Bluetooth toggle
   (Settings → Bluetooth off/on, per `CAP-037`'s own procedure) or physical case/bud
   docking/undocking — **not** the app's own Connect/Disconnect buttons.
2. Repeat this trigger ~10–15 times, each cycle bracketing one moment where a plausibly-relevant
   value might change (e.g. dock state, or whatever else can be identified as independently
   variable and video-observable at each DLCI-0x08-reopen moment).

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Session start, on-screen L/R/Case values noted | User | `PRIV-001` | Conn. state: TBD |
| TBD | Reconnect #1 (Bluetooth toggle or dock/undock — record which), bracketed value: TBD | User (Hardware) | `PRIV-001` | TBD |
| TBD | Reconnect #2 | User (Hardware) | `PRIV-001` | TBD |
| TBD | (continue for 10–15 reconnects — add rows as needed) | User (Hardware) | `PRIV-001` | TBD |
| TBD | Session end | — | `PRIV-001` | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AG)

- [ ] For each unmapped code (`05 0c`, `04 02`, `04 04`, `04 11`, `04 13`, `04 15`, `0e 04`), find
      its `Sent` Get frame and the immediately-following `Rcvd` response(s) on the same Group.
- [ ] Decode any numeric fields and check whether any field tracks the bracketed value across all
      reconnects.
- [ ] A field that stays constant is not a match; only report a semantic reading for a field that
      visibly tracks the known value repeat after repeat.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `PRIV-001` is clearly referenced above.
- [ ] Write `CAP-050-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `PRIV-001` row's Evidence column with a pointer
      once promoted into `PROTOCOL.md`.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-050-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AG/CAP-050-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-050-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AG/CAP-050-EVENT-NOTES
