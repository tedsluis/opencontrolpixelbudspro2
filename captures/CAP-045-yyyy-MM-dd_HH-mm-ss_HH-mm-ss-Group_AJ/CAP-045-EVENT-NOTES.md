# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AJ (new), `HOLD-005` Left/Right ANC-rotation-checklist split (`CAP-045`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-045-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AJ` to the actual session
date/start-time/end-time, e.g. `CAP-045-2026-09-15_08-30-00_08-40-00-Group_AJ`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AJ, added 2026-09-09):** `PROTOCOL.md` §4.5.3's
ANC-mode rotation checklist (`qhr` field 12, confirmed field-number identity as `qht`) has 16
wire-observed boolean flags (`HOLD-005`, `CAP-021`) but no Left/Right-distinguishing field for this
specific write — unlike `HOLD-001`–`HOLD-004`, it's unknown which frames belong to which earbud's
own rotation list.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-045`                     |
|      Group(s)    |                     AJ (new)                       |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, official Pixel Buds Companion App) |
| Video file       |    TBD — must clearly show Left/Right selection and each individual checklist toggle |
| Log file         |             TBD — `CAP-045-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AJ)

1. Open Device details → Controls and gestures → the ANC-mode rotation checklist for the **Left**
   earbud specifically (the UI is confirmed to expose this per-earbud, per `PROTOCOL.md` §4.5.3's
   own UI description).
2. Toggle each of the 4 checklist items (Noise cancellation / Off / Adaptive / Transparency) for
   the Left earbud **one at a time**, with a clear pause (≥10s) and a distinct video-visible action
   between each toggle, so each write can be isolated to one specific checklist item.
3. Repeat step 2 for the **Right** earbud's own rotation checklist, again one item at a time.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Left earbud rotation checklist opened | User (App) | `HOLD-005` | Conn. state: TBD |
| TBD | Left: toggle 'Noise cancellation'. Wait ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Left: toggle 'Off'. Wait ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Left: toggle 'Adaptive'. Wait ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Left: toggle 'Transparency'. Wait ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Right earbud rotation checklist opened | User (App) | `HOLD-005` | TBD |
| TBD | Right: toggle 'Noise cancellation'. Wait ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Right: toggle 'Off'. Wait ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Right: toggle 'Adaptive'. Wait ≥10s | User (App) | `HOLD-005` | TBD |
| TBD | Right: toggle 'Transparency'. Wait ≥10s | User (App) | `HOLD-005` | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AJ)

- [ ] Isolate each of the 8 toggles (4 Left + 4 Right) to its own DLCI 0x02 `Sent` frame, per the
      usual `field5{field4{...}}` envelope (`PROTOCOL.md` §4.5).
- [ ] Do the Left-earbud toggles and Right-earbud toggles produce a distinguishable wire pattern —
      a different inner field position, a different correlation-ID pattern, or a confirmed
      video-to-frame 1:1 timing correlation good enough to assign each frame to a side by
      elimination?
- [ ] Record the result plainly either way — this directly closes `PROTOCOL.md` §6's open item on
      this question (a confirmed structural Left/Right split, or a confirmed absence of one that
      instead relies purely on timing correlation).

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `HOLD-005` is clearly referenced above.
- [ ] Write `CAP-045-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `HOLD-005` row's Evidence column with a pointer
      once promoted into `PROTOCOL.md`.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-045-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AJ/CAP-045-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-045-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AJ/CAP-045-EVENT-NOTES
