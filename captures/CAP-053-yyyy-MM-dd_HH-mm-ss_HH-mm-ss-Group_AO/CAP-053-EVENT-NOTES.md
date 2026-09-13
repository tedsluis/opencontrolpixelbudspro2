# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AO (new), EQ outer field 16-vs-18: drag-and-release without ever tapping Save (`CAP-053`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-053-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AO` to the actual session
date/start-time/end-time, e.g. `CAP-053-2026-09-15_08-30-00_08-40-00-Group_AO`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AO, added 2026-09-13,
`ai-sessions/0017_MAINTENANCE_RESULT_2026_09_13.md` Phase 1):** `PROTOCOL.md` §4.2's "Outer field 16
vs. 18" item is a genuine, unreconciled tension: `CAP-015`'s wire timing reads as "field 18 fires on
slider-release" (no video-visible `Save` tap before any of 15 field-18 frames), but a full call-graph
trace of `fyd.d`/`fyd.e` (`REVERSE_ENGINEERING.md`'s `qjw` entry) found field 18 (`fyd.d`) reachable
through exactly **two** code paths — a dedicated `key_eq_save_button`/`title_eq_save_button` click
handler (self-describing log `"On click save EQ button"`), and, newly found in session 0017, a
**navigate-away-from-the-EQ-screen** path (`hod.java:36`, self-describing log `"Navigate away, save
EQ"`, gated on an unsaved-changes-shaped flag) — but **no** slider-release code path to field 18
anywhere in the decompiled source. This capture isolates all three candidate triggers from each
other for the first time.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-053`                     |
|      Group(s)    |                     AO (new)                       |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, official Pixel Buds Companion App; full DLCI 0x02 traffic retained, raw-path extraction) |
| Video file       |    TBD — must clearly show, for each slider action: whether the `Save` button is touched, and whether the screen changes |
| Log file         |             TBD — `CAP-053-btsnoop_hci.log` (raw path, not `btsnooz.py`) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AO)

1. Open the EQ screen (Device details → Sound → Equalizer → custom sliders).
2. Drag **slider 1** to a new position, release, and **wait ≥10s** with the `Save` button never
   tapped and **without navigating away from the EQ screen** (stay on this exact screen the whole
   time) — video must clearly show both: the `Save` button not being touched, and the screen not
   changing.
3. Repeat step 2 for **slider 2** (a second, independent release-only sample, still without leaving
   the screen or tapping Save).
4. As a clearly separated second half of the same session: drag **slider 3**, release, wait ≥10s (no
   Save tap, no navigation, replicating steps 2–3's isolation once more), then **deliberately tap the
   `Save` button** and video-confirm the tap.
5. As a third, separated part: drag **slider 4**, release, wait ≥3s, then **navigate away from the EQ
   screen** (e.g. press back to Device details) without ever tapping `Save` — video must show the
   screen change clearly.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Session start, raw-path HCI snoop logging confirmed | User | — | Conn. state: TBD |
| TBD | Open EQ screen | User (App) | — | TBD |
| TBD | Slider 1: drag + release, no Save, no navigation, ≥10s wait | User (App) | `EQS-*` | TBD |
| TBD | Slider 2: drag + release, no Save, no navigation, ≥10s wait | User (App) | `EQS-*` | TBD |
| TBD | Slider 3: drag + release, no Save, no navigation, ≥10s wait | User (App) | `EQS-*` | TBD |
| TBD | Slider 3's write: `Save` button tapped, video-confirmed | User (App) | `EQP-008` | TBD |
| TBD | Slider 4: drag + release, ≥3s wait | User (App) | `EQS-*` | TBD |
| TBD | Navigate away from EQ screen (no Save tap), video-confirmed | User (App) | — | TBD |
| TBD | Session end | — | — | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AO)

- [ ] For sliders 1/2 (release-only, no Save, no navigation): does a `field5{field4{field18=...}}`
      write appear on DLCI 0x02 at any point? Expected: no, per the `hju`/`hod` code trace.
- [ ] For slider 3's Save tap: confirm a field-18 write fires at that exact moment (the known,
      already-confirmed trigger).
- [ ] For slider 4's navigate-away: does a field-18 write fire at the moment of navigation, matching
      the newly-found `hod.java` "Navigate away, save EQ" trigger?
- [ ] Record the result plainly for each of the three conditions — a clean 3-way contrast (or a
      surprising positive on the release-only condition) both directly close `PROTOCOL.md` §4.2's
      own open item.

## Next steps after filling this in

- [ ] Cross-reference this session's own findings against `PROTOCOL.md` §4.2 and
      `REVERSE_ENGINEERING.md`'s `qjw` entry (`AGENTS.md` §13's traceability check).
- [ ] Write `CAP-053-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-053-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AO/CAP-053-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-053-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AO/CAP-053-EVENT-NOTES
