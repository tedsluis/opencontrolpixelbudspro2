# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AR (new), `HOLD-005` ANC-rotation-checklist Left/Right split, genuine re-run (`CAP-056`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-056-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AR` to the actual session
date/start-time/end-time, e.g. `CAP-056-2026-09-15_08-30-00_08-40-00-Group_AR`.

**⚠️ Read this before starting, per `CAP-045`'s own gap:** `CAP-045` (Group AJ) was intended to run
this exact procedure but never actually opened the rotation-checklist screen — the maintainer
performed physical press-and-hold ANC cycling instead, which produces *no* rotation-checklist write
at all (`CAP-045-FINDINGS.md` §2/§4). **This session only counts as valid if the checklist screen
itself is clearly visible on camera before any toggle is made** — see step 1 below.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AR, added 2026-09-13,
`ai-sessions/0017_MAINTENANCE_RESULT_2026_09_13.md` Phase 4):** `PROTOCOL.md` §4.5.3's ANC-mode
rotation checklist (`qhr` field 12, confirmed field-number identity as `qht`) has 16 wire-observed
boolean flags (`HOLD-005`, `CAP-021`) but no Left/Right-distinguishing field for this specific
write — unlike `HOLD-001`–`HOLD-004`. `CAP-045` did not answer this; this Group is a fresh attempt,
**not** a `CAP-045` v2 (Group AJ's own procedure remains valid and unchanged).

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-056`                     |
|      Group(s)    |                     AR (new)                       |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, official Pixel Buds Companion App) |
| Video file       |    TBD — **must show the ANC-mode rotation checklist screen itself** (a 4-item checkbox list: Noise cancellation / Off / Adaptive / Transparency) on camera for at least one full toggle, per the anti-repeat safeguard above |
| Log file         |             TBD — `CAP-056-btsnoop_hci.log` |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AR)

1. **Anti-repeat safeguard (mandatory):** before toggling anything, video-confirm the actual
   on-screen destination is "Device details → Controls and gestures → [the ANC-mode rotation
   checklist screen]" — the checkbox list must be clearly visible on-camera for **at least one full
   toggle** before this session counts as having run the procedure at all. If the checklist screen is
   not visible on camera at this point, **stop and restart** — do not proceed and hope the wire data
   disambiguates it after the fact, as happened in `CAP-045`.
2. With the checklist screen confirmed open for the **Left** earbud specifically, toggle each of the
   4 checklist items **one at a time**, with a clear pause (≥10s) and a distinct video-visible action
   between each toggle.
3. Navigate to the **Right** earbud's own rotation checklist (video-confirm the screen again, per
   step 1's safeguard) and repeat step 2.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Session start, raw-path HCI snoop logging confirmed | User | — | Conn. state: TBD |
| TBD | ANC-rotation-checklist screen opened for **Left** earbud, video-confirmed (anti-repeat safeguard) | User (App) | `HOLD-005` | TBD |
| TBD | Left: toggle item 1 (Noise cancellation), pause ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Left: toggle item 2 (Off), pause ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Left: toggle item 3 (Adaptive), pause ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Left: toggle item 4 (Transparency), pause ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | ANC-rotation-checklist screen opened for **Right** earbud, video-confirmed | User (App) | `HOLD-005` | TBD |
| TBD | Right: toggle item 1 (Noise cancellation), pause ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Right: toggle item 2 (Off), pause ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Right: toggle item 3 (Adaptive), pause ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Right: toggle item 4 (Transparency), pause ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Session end | — | — | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AR)

- [ ] Confirm each toggle produces a `field5(len12){field4(len10){field12(len8){field1..4=0|1}}}`
      write on DLCI 0x02 (`qhr` field 12 / `qht`), per `PROTOCOL.md` §4.5.3.
- [ ] Do the Left-earbud toggles and Right-earbud toggles produce distinguishable wire patterns (a
      different inner field position, a different correlation-ID pattern), or is Left/Right only
      inferable from timing/video correlation (as `CAP-021`/`CAP-045` already found for the
      structurally related press-and-hold-cycle Notify)?
- [ ] Record the result plainly either way — this directly closes `PROTOCOL.md` §6's open item on
      this question.

## Next steps after filling this in

- [ ] Cross-reference this session's own findings against `PROTOCOL.md` §4.5.3/§6 and
      `CAP-021-FINDINGS.md`/`CAP-045-FINDINGS.md` (`AGENTS.md` §13's traceability check).
- [ ] Write `CAP-056-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `HOLD-005` row with the result.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-056-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AR/CAP-056-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-056-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AR/CAP-056-EVENT-NOTES
