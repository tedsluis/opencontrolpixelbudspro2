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
| Video file       | `CAP-046-recording.mp4` (17:02:59 - 17:05:49)      |
| Log file         |             `CAP-046-btsnoop_hci.log`              |
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
| 17:02:59 | Video starts. Buds are out of the case and in ears. Bluetooth is off. | User | — | — |
| 17:03:01 | User enables Bluetooth via toggle. | User | — | — |
| 17:03:04 | "Pixel Buds Pro 2 van Ted" connecting. | System | — | — |
| 17:03:14 | Connection active. Battery: L:100%, C:96%, R:100% | System | — | — |
| 17:03:34 | User opens Pixel Buds app ("Device details") and navigates to the 'Sound' menu. | User (App) | `AUDIO-003` | — |
| 17:03:59 | Sample 1: Slider dragged to full Left extreme (-100). | User (App) | `AUDIO-003` | Held for ≥ 3s |
| 17:04:03 | Slider returned to center. | User (App) | `AUDIO-003` | Paused for ≥ 5s |
| 17:04:12 | Sample 1: Slider dragged to full Right extreme (100). | User (App) | `AUDIO-003` | Held for ≥ 3s |
| 17:04:17 | Slider returned to center. | User (App) | `AUDIO-003` | Paused for ≥ 5s |
| 17:04:30 | Sample 2: Slider dragged to full Left extreme (-100). | User (App) | `AUDIO-003` | Held for ≥ 3s |
| 17:04:35 | Slider returned to center. | User (App) | `AUDIO-003` | Paused for ≥ 5s |
| 17:04:48 | Sample 2: Slider dragged to full Right extreme (100). | User (App) | `AUDIO-003` | Held for ≥ 3s |
| 17:04:53 | Slider returned to center. | User (App) | `AUDIO-003` | Paused for ≥ 5s |
| 17:05:03 | Sample 3: Slider dragged to intermediate position Left (-93). | User (App) | `AUDIO-003` | Held for ≥ 3s |
| 17:05:07 | Slider returned to center. | User (App) | `AUDIO-003` | Paused for ≥ 5s |
| 17:05:20 | Sample 3: Slider dragged to intermediate position Right (96). | User (App) | `AUDIO-003` | Held for ≥ 3s |
| 17:05:24 | Slider returned to center. | User (App) | `AUDIO-003` | Paused for ≥ 5s |
| 17:05:39 | Sample 4: Slider dragged to intermediate position Left (-31). | User (App) | `AUDIO-003` | Held for ≥ 3s |
| 17:05:43 | Slider returned to center. | User (App) | `AUDIO-003` | Paused for ≥ 5s |
| 17:05:49 | Video ends. | System | — | — |

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
