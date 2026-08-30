# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group N, Touch gestures (`CAP-027`)

**Status:** ✅ **Captured and analyzed 2026-08-30.** See `CAP-027-FINDINGS.md` for the full
evidence-based writeup; this file records the validated event timeline the findings are built on.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group N):** main run-through group — physical touch
gestures on the bud hardware itself (distinct from Group F's app-side on/off toggles).
**`TOUCH-007`'s behavior depends on the per-earbud press-and-hold configuration set in Group G
(`CAP-021`)** — per `CAP-021-FINDINGS.md` §3/§8, the last-recorded per-earbud assignment as of that
session (2026-08-21) was **both earbuds → "Active noise control" (ANC-mode cycling)**, not Digital
assistant (Left last set by frame 4315 @ 08:03:49.667; Right last set by frame 4976 @
08:04:09.920). This session's own wire evidence (see `CAP-027-FINDINGS.md` §4) independently shows
ANC-mode `Notify` frames on both `TOUCH-007` presses, consistent with that carried-forward
configuration.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-027`                     |
|      Group(s)    |                         N                          |
|       Date       |                     2026-08-30                     |
| Firmware version | `release_5.203` — 🟢 confirmed on-wire (DLCI 0x08 private envelope, 6 occurrences; e.g. frame 923 area) |
|   Test device    | Pixel 7a (⚪ ASSUMPTION, same device as prior sessions — not screen-confirmed this session), official Pixel Buds Companion App (Spotify used as the music source for AVRCP correlation) |
| Video file       |          `CAP-027-recording.mp4` — 233.77s, 15:45:14–15:49:07 local time |
| Log file         |     `CAP-027-btsnoop_hci.log` — 15:44:22.07–15:51:47.04 (+0200), wider than the video window |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |         `04:00:6e:cf:6e:07` — same physical device as `CAP-021`/`CAP-033`             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group N — physical actions on the bud, either
phone)

7. **Tap once** on a bud [`TOUCH-002`]. Wait. Note time.
8. **Double-tap** on a bud [`TOUCH-003`]. Wait. Note time.
9. **Triple-tap** on a bud [`TOUCH-004`]. Wait. Note time.
10. **Swipe forward** on a bud (volume up) [`TOUCH-005`]. Wait. Note time.
11. **Swipe backward** on a bud (volume down) [`TOUCH-006`]. Wait. Note time.
12. **Press and hold** on a bud [`TOUCH-007`]. Wait. Note time. **Record which per-earbud
    press-and-hold mode was active** (from Group G, `CAP-021`).

## Event Timeline

All wire-evidence timestamps below are `btsnoop_hci.log` frame times (local, +0200). Per-action
video-overlay timestamps in the leftmost column run a consistent **~1.5–2.3s ahead of** the
matching wire event across every action in this session (video shows the action slightly later
than the wire event that caused it — encoding/registration lag, not a clock fault); this offset is
noted once here rather than repeated per row. No frame of any kind is directly visible confirming
finger-on-bud contact for any Group N action (camera is aimed at the phone screen throughout,
never at the ear/bud) — every attribution below is a **timing correlation** (nearest wire event
following the noted action, within the established offset), not a visual gesture confirmation.

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 15:45:14 | start of video
| 15:45:18 | user enables bluetooth
| 15:45:20 | Pixel buds pro 2 connected
| 15:45:30 | spotify app: user selects play, music starts playing 
| 15:45:39 | Tap once on right bud | User (Hardware) | `TOUCH-002` | AVRCP frame 1580 @15:45:36.892 `Rcvd Pass Through: Control - PAUSE (Pushed)` (offset 2.11s) |
| 15:45:48 | swipe on right bud (unintentionaly) | User (Hardware) | — | AVRCP frame 1794 @15:45:46.515 `Rcvd Pass Through: Control - PLAY (Pushed)` (offset 1.49s) — see `CAP-027-FINDINGS.md` §3.2: the "unintentional" contact still produced a Pass Through command, but a **tap-shaped** one (PLAY), not the swipe/volume shape |
| 15:45:59 | Double-tap on right bud | User (Hardware) | `TOUCH-003` | AVRCP frame 1909 @15:45:55.855 `Rcvd Pass Through: Control - FORWARD (Pushed)` (offset 4.15s) |
| 15:46:09 | Triple-tap on right bud | User (Hardware) | `TOUCH-004` | AVRCP frame 1980 @15:46:07.368 `Rcvd Pass Through: Control - BACKWARD (Pushed)` (offset 1.63s) |
| 15:46:24 | Triple-tap on right bud | User (Hardware) | `TOUCH-004` | AVRCP frame 2066 @15:46:22.800 `Rcvd Pass Through: Control - BACKWARD (Pushed)` (offset 1.20s) |
| 15:46:35 | Tap once on left bud | User (Hardware) | `TOUCH-002` | AVRCP frame 2139 @15:46:32.692 `Rcvd Pass Through: Control - PAUSE (Pushed)` (offset 2.31s) |
| 15:46:44 | Tap once on left bud | User (Hardware) | `TOUCH-002` | AVRCP frame 2251 @15:46:42.245 `Rcvd Pass Through: Control - PLAY (Pushed)` (offset 1.75s) |
| 15:46:51 | Double-tap on left bud | User (Hardware) | `TOUCH-003` | AVRCP frame 2313 @15:46:49.512 `Rcvd Pass Through: Control - FORWARD (Pushed)` (offset 1.49s) |
| 15:47:07 | Triple-tap + tap once on left bud | User (Hardware) | `TOUCH-004` | AVRCP frame 2396 @15:47:05.244 `BACKWARD (Pushed)` (triple-tap, offset 1.76s) **+** frame 2407 @15:47:05.647 `PAUSE (Pushed)` 0.40s later (the "+ tap once") |
| 15:47:14 | Tap once on left bud | User (Hardware) | `TOUCH-002` | AVRCP frame 2464 @15:47:12.027 `Rcvd Pass Through: Control - PLAY (Pushed)` (offset 1.97s) |
| 15:47:32 | Triple-tap + tap once on left bud | User (Hardware) | `TOUCH-004` | AVRCP frame 2575 @15:47:30.259 `BACKWARD (Pushed)` (triple-tap, offset 1.74s) **+** frame 2587 @15:47:30.613 `PAUSE (Pushed)` 0.35s later (the "+ tap once") |
| 15:47:36 | Tap once on left bud | User (Hardware) | `TOUCH-002` | AVRCP frame 2642 @15:47:34.596 `Rcvd Pass Through: Control - PLAY (Pushed)` (offset 1.40s) |
| 15:47:54 | Triple-tap on left bud | User (Hardware) | `TOUCH-004` | AVRCP frame 2746 @15:47:52.180 `Rcvd Pass Through: Control - BACKWARD (Pushed)` (offset 1.82s) — **no** accompanying tap-once this time, matching this row's own label (unlike the two `07`/`32` rows above) |
| 15:48:07 | Swipe forward on right bud (volume up)| User (Hardware) | `TOUCH-005` | AVRCP frame 2822 @15:48:05.136 `Rcvd Vendor dependent: Changed - RegisterNotification - VolumeChanged - Volume: 65%` (up from the running 59% baseline; offset 1.86s) |
| 15:48:17 | Swipe backward on right bud (volume down)| User (Hardware) | `TOUCH-006` | AVRCP frame 2873 @15:48:15.097 `Volume: 59%` (down from 65%; offset 1.90s) |
| 15:48:24.898 (log) — no separate video-visible moment (screen unchanged) | Press and hold on right bud (mode: **Active noise control**, carried over from `CAP-021`) | User (Hardware) | `TOUCH-007` | DLCI 0x04 frame 2930 `08 13 00 04 01 e8 e8 08` — "Notify ANC state" (Group `0x08` Code `0x13`), `new_mode=0x08` = ANC/Active. Falls in the only unexplained gap between `TOUCH-006` (15:48:17) and the next confirmed AVRCP event (15:48:36.74/`TOUCH-005` left) — see `CAP-027-FINDINGS.md` §4 |
| 15:48:39 | Swipe forward on left bud (volume up) | User (Hardware) | `TOUCH-005` | AVRCP frame 2999 @15:48:36.744 `Volume: 65%` (offset 2.26s) |
| 15:48:44 | Swipe backward on left bud (volume down) | User (Hardware) | `TOUCH-006` | AVRCP frame 3032 @15:48:42.444 `Volume: 59%` (offset 1.56s) |
| ~15:48:48–15:48:54 (log 15:48:46.658 / 15:48:52.342) — no separate video-visible moment (screen unchanged) | Press and hold on left bud (mode: **Active noise control**, carried over from `CAP-021`) | User (Hardware) | `TOUCH-007` | DLCI 0x04 frame 3056 @15:48:46.658 `08 13 00 04 01 e8 e8 40` (`new_mode=0x40`=Adaptive) **and** frame 3091 @15:48:52.342 `08 13 00 04 01 e8 e8 80` (`new_mode=0x80`=Transparent/Aware), 5.684s apart. 🔴 **Not resolved which**: this could be one held gesture that advanced the rotation two steps, or two repeated press-and-hold actions — the procedure notes only recorded one action for this earbud, and the wire data alone cannot distinguish the two readings. See `CAP-027-FINDINGS.md` §4 |
| 15:49:07 | end of video

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [x] These are hardware-initiated gestures — check whether they generate any RFCOMM/GATT traffic
      at all, or are purely on-device (media-key-style local reactions with no wire signal). →
      **All six Test-IDs produce wire traffic**, but `TOUCH-002`–`TOUCH-006` do **not** appear on
      any RFCOMM DLCI at all — they ride the standard **AVRCP** profile over its own L2CAP channel
      (Pass Through commands / VolumeChanged notifications), not `libmaestro`/Fast-Pair-MS/the
      private envelope. `TOUCH-007` is the exception (see below).
- [x] If traffic is found, identify which DLCI/channel carries it. → `TOUCH-002`–`TOUCH-006`: AVRCP
      (L2CAP, not an RFCOMM DLCI). `TOUCH-007`: RFCOMM **DLCI 0x04** (the official Fast Pair
      Message Stream, already 🟢 FACT for ANC per `PROTOCOL.md` §4.1) — **not** DLCI 0x02
      (`libmaestro`/pigweed).
- [x] Compare structure against `PROTOCOL.md` §2's envelope hypotheses per the usual process. →
      `TOUCH-007`'s frames match §4.1's already-documented "Notify ANC state" (`0x13`) shape
      exactly, byte-for-byte. See `CAP-027-FINDINGS.md` §4.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `TOUCH-002`–`TOUCH-007` are clearly referenced above. All six
      present above.
- [x] Write `CAP-027-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time. **Not done in this pass** — the placeholder folder
      name already embeds the correct actual date/start/end (`2026-08-30_15-45-14_15-49-07`), so
      no rename is needed; only the `-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-` skeleton pattern from the
      template was ever a placeholder, and this folder does not use it.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-027-2026-08-30_15-45-14_15-49-07-Group_N/CAP-027-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-027-2026-08-30_15-45-14_15-49-07-Group_N/CAP-027-EVENT-NOTES
