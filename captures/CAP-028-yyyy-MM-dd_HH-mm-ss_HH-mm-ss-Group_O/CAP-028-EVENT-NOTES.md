# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group O, Head gestures (`CAP-028`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-028-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_O` to the actual session
date/start-time/end-time, e.g. `CAP-028-2026-09-01_10-30-00_10-34-00-Group_O`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group O):** main run-through group, never yet
captured. **Requires 'Head gestures' enabled first (Group F, `CAP-020`)** — do not run this
session before `CAP-020` has toggled Head gestures on. Head gestures are a Pixel Buds Pro
2-exclusive feature, officially confirmed present (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` `HEAD-001`);
this session tests whether the gestures themselves (Nod/Shake) generate any wire-visible traffic,
or are purely on-device given they only act on an already-active call/notification context.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-028`                     |
|      Group(s)    |                         O                          |
|       Date       |                        TBD                         |
| Firmware version | TBD (must be ≥4.467 for Head gestures to exist at all, per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §6 — see the Preparation note below) |
|   Test device    | TBD (Pixel 7a, Android version, official app version) |
| Video file       |               TBD — `CAP-028-recording.mp4`        |
| Log file         |             TBD — `CAP-028-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

**Preparation (required before starting):**
- Confirm 'Head gestures' is enabled on screen (Device details → Controls and gestures → Head
  gestures) before starting this Group's actions — record how/when this was confirmed. **If it was
  not already enabled in a separate prior session (`CAP-020`), enable it now as this session's own
  first logged step** (see Procedure step 0 below) rather than assuming it's already on.
- Firmware note (documentation only, not currently a blocker): `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §6
  states Head Gestures require firmware ≥4.467, a version scheme distinct from this project's own
  confirmed wire-baseline `"release_5.203"` — `PROTOCOL.md` §0.1's `XC-03` note leaves the numeric
  relationship between the two schemes unreconciled, but also confirms this isn't currently
  blocking anything: Head gestures are already known working on this project's own test device
  since `CAP-020`. Record the firmware version shown on screen for this session regardless, per
  `PROJECT_RULES.md` rule 11's reproducibility requirement.
- Confirm HCI snoop logging is already enabled and running, and the Buds are connected, before
  either the Head-gestures-enable step (if needed) or the Nod/Shake actions begin.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group O — requires Head gestures enabled)

0. **(Only if not already done in a separate session):** enable 'Head gestures' now, as this
   session's own first logged step (Device details → Controls and gestures → Head gestures
   toggle on). Note the exact time. If already confirmed enabled from a prior session, skip this
   step and just record the confirmation per the Preparation note above.
13. **Nod** [`HEAD-002`] (simulating answering a call, or a text reply if 'Spoken notifications'
    is on). Wait. Note time.
14. **Shake** [`HEAD-003`] (simulating rejecting a call/dismissing a text reply). Wait. Note time.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | HCI snoop logging confirmed enabled and running, Buds connected | User | — | TBD |
| TBD | (if needed) Head gestures toggled on as this session's own first step | User (App) | — | TBD |
| TBD | Head gestures confirmed enabled on screen | — | — | TBD |
| TBD | Nod | User (Hardware) | `HEAD-002` | TBD |
| TBD | Shake | User (Hardware) | `HEAD-003` | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [ ] Check whether the gesture generates any RFCOMM/GATT traffic at all, or is purely on-device
      (e.g. only relevant if a call/notification is actually active — note whether that condition
      was met during this capture, since the app's own description ties these gestures to an
      active call/notification context, not a standalone toggle).
- [ ] If traffic is found, identify which DLCI/channel carries it.
- [ ] Compare structure against `PROTOCOL.md` §2's envelope hypotheses per the usual process.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `HEAD-002`/`HEAD-003` are clearly referenced above.
- [ ] Write `CAP-028-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-028-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_O/CAP-028-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-028-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_O/CAP-028-EVENT-NOTES
