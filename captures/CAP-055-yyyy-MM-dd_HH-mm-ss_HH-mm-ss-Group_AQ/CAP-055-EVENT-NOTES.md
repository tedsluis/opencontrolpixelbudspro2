# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AQ (new), Head gestures (Nod/Shake) with an active call/notification (`CAP-055`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-055-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AQ` to the actual session
date/start-time/end-time, e.g. `CAP-055-2026-09-15_08-30-00_08-40-00-Group_AQ`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AQ, added 2026-09-13,
`ai-sessions/0017_MAINTENANCE_RESULT_2026_09_13.md` Phase 3):** `CAP-028` (Group O) found zero
wire-visible traffic on the Buds' own connection during a claimed Nod/Shake gesture window — but
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s own description ties Nod/Shake's actual function to an active
call ("answers a call") or notification ("dismisses a text reply"), and no call/notification was
active during `CAP-028`'s own window, so the clean-negative result cannot distinguish "functionally
inert, as expected" from "gesture not actually triggered." This Group fixes that specific gap.
Session 0017's APK cross-reference additionally confirmed `HeadGesture`'s result enum (`qin`) is
genuinely 3-valued (raw `{0,1,2}`) but carries no name for any value — this capture is the only way
to determine which raw value is Nod and which is Shake, if the enum is ever observed on the wire.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-055`                     |
|      Group(s)    |                     AQ (new)                       |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, official Pixel Buds Companion App; "Use head gestures" toggled ON, re-confirmed on camera) |
| Video file       |    TBD — must frame **both** the phone screen (call/notification state) **and** the user's head/ears (the gesture itself) |
| Log file         |             TBD — `CAP-055-btsnoop_hci.log` |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AQ)

1. Confirm "Use head gestures" is toggled ON (Device details → Controls and gestures) before starting
   — re-confirm on camera (do not rely on a carried-over assumption from `CAP-020`).
2. With the Buds connected and worn, **trigger an actual incoming phone call** (e.g. call the test
   phone from a second phone). While the call is ringing, perform a **Nod** gesture — camera angled to
   capture both the phone screen (call being answered) and the head/ears (the gesture itself).
3. During the call, end it normally; then trigger a second incoming call and perform a **Shake**
   gesture to reject it — same dual camera framing.
4. Separately, trigger a text-message notification (with 'Spoken notifications'/dictation-reply
   context if feasible) and perform a Nod (reply via dictation) or Shake (dismiss) gesture, camera
   angled the same dual way.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Session start, "Use head gestures" confirmed ON on camera | User | — | Conn. state: TBD |
| TBD | Second phone calls test phone — incoming call rings | User (Phone) | `CALL-001` | TBD |
| TBD | Nod gesture performed (answer call), camera on head+screen | User (Hardware) | `HEAD-002` | TBD |
| TBD | Call ends normally | User | — | TBD |
| TBD | Second incoming call rings | User (Phone) | `CALL-001` | TBD |
| TBD | Shake gesture performed (reject call), camera on head+screen | User (Hardware) | `HEAD-003` | TBD |
| TBD | Text-message notification triggered | User (Phone) | — | TBD |
| TBD | Nod or Shake gesture performed in response, camera on head+screen | User (Hardware) | `HEAD-002`/`HEAD-003` | TBD |
| TBD | Session end | — | — | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AQ)

- [ ] For each gesture, check for a wire-visible signal correlated with it: SCO/eSCO setup/teardown
      change, AVRCP command, or a DLCI 0x02/0x04/0x08 write (per `PROTOCOL.md` §4.1's ANC-Notify
      pattern as a template for what a hardware-gesture-triggered push might look like).
- [ ] If a DLCI 0x02 `HeadGesture`-shaped RPC is found, check it against `REVERSE_ENGINEERING.md`'s
      `qin` register (updated by session 0017 — the response enum is 3-valued, raw `{0,1,2}`) and
      record, per `AGENTS.md` §13.6, which raw value corresponds to Nod and which to Shake — this is
      new evidence, not a guess, since both the gesture and (if found) the wire value are now
      camera/log-confirmed together for the first time.
- [ ] Record a clean negative plainly if no signal is found even with an active call/notification —
      this would be a stronger result than `CAP-028`'s own inconclusive one (it would rule out the
      "no call was active" confound entirely).

## Next steps after filling this in

- [ ] Cross-reference this session's own findings against `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s
      `HEAD-002`/`HEAD-003` rows and `PROTOCOL.md` §6 (`AGENTS.md` §13's traceability check).
- [ ] Write `CAP-055-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-055-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AQ/CAP-055-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-055-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AQ/CAP-055-EVENT-NOTES
