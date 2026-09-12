# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AK (new), Volume balance (`field 17`) scale/direction (`CAP-046`)

**Status:** ✅ **Captured and analyzed 2026-09-12.** Full video (170.14s) and full log (2,655
packets, untruncated) reviewed — see `CAP-046-FINDINGS.md`. **Major correction to the draft:** the
draft labeled its Left-extreme samples "-100" and Right-extreme samples "100" — the wire+video
evidence shows the **opposite polarity**: positive zigzag values are the **Left** extreme, negative
values are the **Right** extreme. Also, only **2** full extreme cycles actually produced wire writes
(not the 4 samples the draft describes) — the draft's "sample 3"/"sample 4" intermediate-position
actions (`-93`, `96`, `-31`) never happened on screen or on the wire; the slider simply stays at the
left extreme, untouched, for the rest of the video.

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
|       Date       |                     2026-09-12                     |
| Firmware version | ⚪ ASSUMPTION `release_5.203` (carried over) |
|   Test device    | Pixel 7a, Android 17, Pixel Buds Companion App `1.0.955078536`, Google Play services active |
| Video file       | `CAP-046-recording.mp4` — 170.14s, overlay `17:02:59`–`17:05:49` |
| Log file         | `CAP-046-btsnoop_hci.log` — 2,655 packets, 307.74s, `17:03:01.185`–`17:08:08.925`, untruncated |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |         `04:00:6e:...:07`         |

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

## Event Timeline (corrected against full video review + full log analysis, 2026-09-12)

**Method:** DLCI 0x02 `Sent` frames decoded (HDLC unescape + CRC-32 verify), searching every frame
for the `qhr` field-17 tag (`0x88 0x01`, per `PROTOCOL.md` §4.5.7) — 8 occurrences found, zero
missed by a broader raw-hex grep cross-check. Each wire timestamp was then matched against a video
frame extracted at that exact offset.

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 17:02:59 | Video starts. Buds worn. Bluetooth off. | User | — | — |
| ~17:03:01–14 | Bluetooth enabled, connects | User/System | — | — |
| ~17:03:34 | Navigates to Device details → Sound | User (App) | `AUDIO-003` | Video `t=35`. |
| **17:03:42.117** | **Slider dragged to full extreme — wire `field17=-100`** | User (App) | `AUDIO-003` | Log frame 1181. **Video at this exact timestamp (`w_43.1.png`) shows the handle at the full RIGHT extreme** — `field17=-100` = **Right**, not Left as the draft assumed. |
| 17:03:42.484 | `field19` (Mono audio) fires `=1` (ON), 0.37s after the balance write | System (auto?) | — | Log frame 1190 — see Findings §3, a new correlation. |
| **17:04:01.984** | Slider returned to center — wire `field17=2` (near-zero) | User (App) | `AUDIO-003` | Log frame 1686. |
| 17:04:02.163 | `field19` fires `=0` (OFF) | System (auto?) | — | Log frame 1696. |
| **17:04:15.383** | Slider dragged to full extreme — wire `field17=100` | User (App) | `AUDIO-003` | Log frame 1746. **Video (`w_76.4.png`) shows the handle at the full LEFT extreme** — `field17=100` = **Left**. |
| 17:04:15.680 | `field19` fires `=1` (ON) | System (auto?) | — | Log frame 1754. |
| **17:04:38.261** | Slider returned to center — wire `field17=1` | User (App) | `AUDIO-003` | Log frame 1786. |
| 17:04:38.712 | `field19` fires `=0` (OFF) | System (auto?) | — | Log frame 1806. |
| **17:04:51.320** | Slider dragged to full extreme (2nd cycle) — wire `field17=-100` | User (App) | `AUDIO-003` | Log frame 1834. **Video (`w_112.3.png`) confirms RIGHT extreme again** — 2nd confirmation. |
| 17:04:51.613 | `field19` fires `=1` (ON) | System (auto?) | — | Log frame 1843. |
| **17:05:09.216** | Slider returned to center — wire `field17=0` | User (App) | `AUDIO-003` | Log frame 1873. |
| 17:05:09.362 | `field19` fires `=0` (OFF) | System (auto?) | — | Log frame 1880. |
| **17:05:22.959** | Slider dragged to full extreme (2nd cycle) — wire `field17=100` | User (App) | `AUDIO-003` | Log frame 1908. **Video (`w_144.0.png`) confirms LEFT extreme again** — 3rd confirmation. |
| 17:05:23.290 | `field19` fires `=1` (ON) | System (auto?) | — | Log frame 1916. |
| ~17:05:39 | **No slider change** — handle remains at the Left extreme, unchanged | — | — | Video `t=160` (`f_160.png`) — the draft's claimed "Sample 4: intermediate -31" action **did not happen**; the slider is untouched here. |
| **17:05:46.275** | Slider returned to center — wire `field17=1` (final write of the session) | User (App) | `AUDIO-003` | Log frame 1957. |
| 17:05:46.335 | `field19` fires `=0` (OFF) | System (auto?) | — | Log frame 1965. |
| 17:05:49 | Video ends (170.14s) | System | — | — |

**Correction to the draft:** the draft describes 4 distinct samples (2 full-extreme cycles + 2
"intermediate-position" cycles at `-93`/`96`/`-31`). Only the **2 full-extreme cycles** actually
occurred and produced wire writes (8 `field17` writes total, confirmed by an exhaustive raw-hex
search for the field-17 tag across every DLCI 0x02 `Sent` frame in the log — no additional
occurrences exist). No intermediate-position drag appears anywhere in the video or the log.

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AK)

- [x] Zigzag-decode (`(n>>1) ^ -(n&1)`) each isolated sample's `field 17` value. → Done, 8 samples.
- [x] Match each decoded value against the video-confirmed slider position/label at that exact
      moment. → 3 extreme-position video confirmations (+ a 4th confirming the "no change" gap).
- [x] Determine the field's numeric scale/range and which direction maps to negative vs. positive. →
      **Range ±100. Positive = Left, negative = Right — opposite of the draft's own labeling.**
- [x] Record the result plainly either way. → Recorded, with the draft's polarity error corrected.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `AUDIO-003` is clearly referenced above.
- [x] Write `CAP-046-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [x] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `AUDIO-003` row's Evidence column with a pointer
      once promoted into `PROTOCOL.md`.
- [x] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-046-2026-09-12_17-02-59_17-05-49-Group_AK/CAP-046-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-046-2026-09-12_17-02-59_17-05-49-Group_AK/CAP-046-EVENT-NOTES
