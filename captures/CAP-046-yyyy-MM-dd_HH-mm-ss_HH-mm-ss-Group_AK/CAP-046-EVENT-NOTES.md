# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AK (new), Volume balance (`field 17`) scale/direction (`CAP-046`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-046-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AK` to the actual session
date/start-time/end-time, e.g. `CAP-046-2026-09-15_08-30-00_08-40-00-Group_AK`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AK, added 2026-09-09):** `PROTOCOL.md` §4.5.7
confirms `qhr` field 17 = Volume balance (full identity, 🟢 FACT), but its numeric scale/range
beyond the 7 samples in one continuous drag (`CAP-022`) and which direction (Left/Right)
corresponds to negative vs. positive zigzag-decoded values remain 🔴 open — a single continuous
drag at 1fps video-sampling resolution wasn't enough to resolve this.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-046`                     |
|      Group(s)    |                     AK (new)                       |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, official Pixel Buds Companion App) |
| Video file       |    TBD — must clearly show the Balance slider's on-screen position/label at each isolated sample |
| Log file         |             TBD — `CAP-046-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AK)

1. Open Device details → Sound → the Balance slider.
2. Drag the slider to its **full Left extreme**, release, and hold for ≥3s before any further
   action (an isolated, discrete sample, not a continuous drag) — video-confirm the slider's own
   on-screen position/label at this extreme.
3. Return the slider to center, pause ≥5s, then drag to its **full Right extreme**, release, hold
   ≥3s, video-confirm.
4. Repeat steps 2–3 at least once more for a second independent sample of each extreme.
5. Optionally, sample 1–2 clearly-labeled intermediate positions (e.g. "25% Left", "25% Right" if
   the UI shows a numeric/percentage label) the same isolated way.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Balance slider screen opened | User (App) | `AUDIO-003` | Conn. state: TBD |
| TBD | Sample 1: full Left extreme, hold ≥3s, video-confirmed label | User (App) | `AUDIO-003` | TBD |
| TBD | Slider returned to center. Pause ≥5s | User (App) | `AUDIO-003` | TBD |
| TBD | Sample 1: full Right extreme, hold ≥3s, video-confirmed label | User (App) | `AUDIO-003` | TBD |
| TBD | Sample 2: full Left extreme, hold ≥3s, video-confirmed label | User (App) | `AUDIO-003` | TBD |
| TBD | Sample 2: full Right extreme, hold ≥3s, video-confirmed label | User (App) | `AUDIO-003` | TBD |
| TBD | (optional) intermediate position sample(s), video-confirmed label | User (App) | `AUDIO-003` | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AK)

- [ ] Zigzag-decode (`(n>>1) ^ -(n&1)`) each isolated sample's `field 17` value.
- [ ] Match each decoded value against the video-confirmed slider position/label at that exact
      moment.
- [ ] Determine the field's numeric scale/range (does it saturate at the extremes sampled, or
      extend further?) and which direction (Left/Right) maps to negative vs. positive.
- [ ] Record the result plainly either way — this directly closes `PROTOCOL.md` §6's open item on
      this field's scale/direction.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `AUDIO-003` is clearly referenced above.
- [ ] Write `CAP-046-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `AUDIO-003` row's Evidence column with a pointer
      once promoted into `PROTOCOL.md`.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-046-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AK/CAP-046-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-046-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AK/CAP-046-EVENT-NOTES
