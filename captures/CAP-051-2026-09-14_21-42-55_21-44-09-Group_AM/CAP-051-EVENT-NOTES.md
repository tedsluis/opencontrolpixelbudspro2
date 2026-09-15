# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AM (new), `qhr` field 13 ANC-parallel-path wire confirmation (`CAP-051`)

**Status:** 🟡 **Video-verified, log analysis pending.** This file's Event Timeline and Log Metadata
reflect a full, non-sampled review of `CAP-051-recording.mp4` (`ai-sessions/0020_CAPTURE_PROMPT_2026_09_14.md`).
**`.log` analysis and `CAP-051-FINDINGS.md` are explicitly out of scope for that video-only pass and
remain a separate, future task** — every "Wire evidence / Notes" cell below is `TBD` pending that
session, and every Analysis checklist item stays unchecked. Do not infer any DLCI 0x02 correlation
result from this file alone.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AM, added 2026-09-09):**
`REVERSE_ENGINEERING.md`'s `qhr` entry fully traces field 13 (ANC state, DLCI 0x02) to exactly two
code-side callers — an in-app `QuickActionsFragment` toggle-group tap, and a physical
press-and-hold gesture (`gvi`/`gvj`) — but neither has ever been wire-confirmed: no capture has yet
correlated a `qhr`-field-13 write on DLCI 0x02 with an itself otherwise-unexplained DLCI-0x04
Notify (the specific pattern `CAP-038-FINDINGS.md` §5 observed twice with no preceding Get/Set on
DLCI 0x04).

**No existing Test-ID covers this specific DLCI-0x02/DLCI-0x04 correlation question** — a new
Test-ID would be a `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` addition, out of this skeleton-creation
prompt's own scope (see `ai-sessions/0005_MAINTENANCE_PROMPT_2026_09_09.md` Task 4); this is
flagged here as a follow-up, not silently assigned one, and stays flagged (not actioned) after this
video-only pass too, per `ai-sessions/0020_CAPTURE_PROMPT_2026_09_14.md`'s own scope boundary. The
existing `ANC-*` Test-IDs cover the UI-level ANC mode change itself; this session's own question
(which channel(s) carry the write) is distinct from those.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-051`                     |
|      Group(s)    |                     AM (new)                       |
|       Date       |                     2026-09-14                     |
| Firmware version |                   `release_5.203`                  |
|   Test device    | Pixel 7a, official Pixel Buds Companion App; full DLCI 0x02 traffic retained, raw-path extraction — **note:** whether raw-path/non-`btsnooz.py` logging was actually used is a log-file property this video-only session cannot confirm from the recording alone; that specific sub-claim is left open for the separate log-analysis session, not marked confirmed just because this cell (inherited from the skeleton) states it |
| Video file       | `CAP-051-recording.mp4`, duration 73.758489s (`ffprobe`), 1280×720 @ 30fps, wall-clock overlay 21:42:55 → 21:44:09 (2026-09-14). Watched in full via dense `ffmpeg -ss <t> -frames:v 1` extraction (1s spacing across the full duration, tightened to 0.5s around each action) — clearly shows the in-app tap and each physical press-and-hold gesture, each isolated by a visible gap with no contact/tap in between. |
| Log file         |             TBD — pending separate log-analysis session — `CAP-051-btsnoop_hci.log` (raw path, not `btsnooz.py`; not independently confirmable from this video-only pass, see Test device cell above) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure

**Planned procedure (skeleton, unchanged below):**

1. With full DLCI 0x02 traffic retained (raw-path extraction, not `btsnooz.py`), perform an
   isolated ANC-mode change via the **in-app** `QuickActionsFragment` toggle group (a single tap,
   pause ≥10s before the next action, matching `CAP-006`'s own isolated-tap discipline).
2. Separately, perform an isolated ANC-mode change via a **physical press-and-hold gesture** on one
   earbud (again, single gesture, pause ≥10s).

**What the video actually shows happened (this session's own verified account):** the session opens
with Bluetooth being turned on and the Buds connecting (not itself part of the planned two-action
procedure, but recorded here as a plain observation), then the Device details screen is opened
showing ANC already on "Adaptive" — this is the pre-existing state, not an action performed during
this capture. From there, the video shows exactly **one** in-app ANC-mode tap (`Adaptive` →
`Transparency`), followed by **three** separate physical press-and-hold gestures on the same visible
earbud (not two, as recalled beforehand — "2x lang ingedrukt"): `Transparency` → `Noise cancellation`,
then `Noise cancellation` → `Adaptive`, then `Adaptive` → `Transparency`. This is a genuine deviation
from the planned single-tap/single-gesture structure — see the Event Timeline below for exact
timestamps, and the spacing note beneath it for the ≥10s isolation gap, which two of the three
inter-gesture gaps fall short of.

**Left/Right identity — unconfirmed from video alone.** Every physical gesture is performed on the
same earbud, visible on the right-hand side of the video frame, with a bud consistently visible
in that ear throughout. The video never shows the other ear, the case, or any marking that would
independently establish which physical earbud (Left or Right) this is — the maintainer's account
(Context section of `ai-sessions/0020_CAPTURE_PROMPT_2026_09_14.md`, "right earbud, in the right
ear") is plausible and consistent with a single consistent earbud/ear appearing throughout, but is
not itself confirmed by anything visible in this recording.

## Event Timeline

| Time (wall clock, from video overlay) | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 21:42:55 | Video start. Phone screen shows Bluetooth settings, Bluetooth **off**. | User | — | TBD — pending separate log-analysis session (out of scope for this prompt) |
| ~21:43:01–21:43:06 | "Use Bluetooth" toggle switched on; Bluetooth quick-settings panel shows "Pixel Buds Pro 2 van Ted — Connecting…" | User | — | TBD — pending separate log-analysis session (out of scope for this prompt) |
| ~21:43:16 | Device details screen open (`Pixel Buds Pro 2 van Ted`, Active). On-screen: Left 100%, Case 54%, Right 100%. Active noise control row visible, **"Adaptive" already selected** — this is the screen's resting state when it was opened, not an action performed in this session. | User (App) | — | TBD — pending separate log-analysis session (out of scope for this prompt) |
| 21:43:26 (tap ~21:43:26, confirmed on-screen by 21:43:27) | **In-app ANC-mode tap** (`QuickActionsFragment` toggle group, Active noise control row): user's finger taps "Transparency". Mode changes on-screen from "Adaptive" to "Transparency". Single isolated tap — no other in-app tap observed anywhere else in the video. | User (App) | `ANC`-family | TBD — pending separate log-analysis session (out of scope for this prompt) |
| 21:43:38–~21:43:41/42 (contact visible continuously in this window; mode-change confirmed on-screen by 21:43:42) | **Physical press-and-hold gesture #1**: finger visibly pressed against the earbud (right side of frame; Left/Right identity not independently confirmed from video, see Procedure note above) for ~3–4s. Mode changes on-screen from "Transparency" to "Noise cancellation". | User (Hardware) | `TOUCH-007` | TBD — pending separate log-analysis session (out of scope for this prompt) |
| ~21:43:51/52–~21:43:54 (contact visible continuously in this window; mode-change confirmed on-screen by 21:43:55) | **Physical press-and-hold gesture #2**: same earbud, finger pressed again for ~3s. Mode changes on-screen from "Noise cancellation" to "Adaptive". Gap since gesture #1's release (~21:43:42) to this gesture's contact start (~21:43:51/52) is **~9–10s — at or just under** the planned ≥10s isolation window. | User (Hardware) | `TOUCH-007` | TBD — pending separate log-analysis session (out of scope for this prompt) |
| ~21:44:02–~21:44:03 (contact visible continuously in this window; mode-change confirmed on-screen by 21:44:04) | **Physical press-and-hold gesture #3**: same earbud, finger pressed a third time for ~2s. Mode changes on-screen from "Adaptive" to "Transparency". Gap since gesture #2's release (~21:43:55) to this gesture's contact start (~21:44:02) is **~7s — clearly under** the planned ≥10s isolation window. | User (Hardware) | `TOUCH-007` | TBD — pending separate log-analysis session (out of scope for this prompt) |
| 21:44:04–21:44:09 | No further action observed. Device details screen remains open, ANC mode remains "Transparency" through video end. Session/video end at 21:44:09. | — | — | TBD — pending separate log-analysis session (out of scope for this prompt) |

**Spacing summary (video-only observation, no wire conclusion drawn):** the gap between the in-app
tap (~21:43:27) and physical gesture #1's start (~21:43:38) is ~11s, meeting the planned ≥10s
isolation window. The gap between physical gesture #1's release (~21:43:42) and gesture #2's start
(~21:43:51/52) is ~9–10s, and the gap between gesture #2's release (~21:43:55) and gesture #3's
start (~21:44:02) is ~7s — both **shorter** than the planned ≥10s isolation window. Per
`ai-sessions/0020_CAPTURE_PROMPT_2026_09_14.md`'s Context section, this is flagged explicitly as
reducing the future log-analysis session's ability to cleanly attribute any single DLCI 0x02 write
to one specific gesture for these two pairs — it does not itself say anything about whether such a
write exists.

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AM)

- [ ] For the in-app tap (~21:43:26–27): check whether a `field5{field4{field13=N}}` write appears on
      DLCI 0x02 at that moment. *(Requires the separate, out-of-scope log-analysis session.)*
- [ ] For physical gesture #1 (~21:43:38–42): check whether the same `field5{field4{field13=N}}`
      write appears on DLCI 0x02 at that moment. *(Requires the separate, out-of-scope log-analysis
      session.)*
- [ ] For physical gesture #2 (~21:43:51/52–55): same check. Note the reduced (~9–10s) isolation gap
      from gesture #1 when interpreting the result. *(Requires the separate, out-of-scope
      log-analysis session.)*
- [ ] For physical gesture #3 (~21:44:02–04): same check. Note the reduced (~7s) isolation gap from
      gesture #2 when interpreting the result. *(Requires the separate, out-of-scope log-analysis
      session.)*
- [ ] For each action, note whether it's time-correlated with the tap/gesture, mirroring `CAP-006`'s
      DLCI-0x04 confirmation methodology. *(Requires the separate, out-of-scope log-analysis
      session.)*
- [ ] A positive match for any action would be strong evidence DLCI 0x02 *also* carries ANC state in
      parallel to DLCI 0x04's already-confirmed path — record the result plainly either way (a
      positive match, or a confirmed absence). *(Requires the separate, out-of-scope log-analysis
      session.)*

## Next steps after filling this in

- [ ] Cross-reference this session's own findings against `PROTOCOL.md` §4.1 (DLCI 0x04's confirmed
      ANC path) and `REVERSE_ENGINEERING.md`'s `qhr` field-13 entry (`AGENTS.md` §13's traceability
      check). *(Deferred to the log-analysis session.)*
- [ ] Write `CAP-051-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a). *(Deferred — explicitly out of
      scope for this video-only pass.)*
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
      *(Deferred — not touched by this video-only pass; status stays `planned`.)*
- [ ] Flag, as a follow-up outside this prompt's own scope, whether a dedicated Test-ID for this
      question should be added to `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (see the Purpose section
      above). *(Still flagged, not actioned, per this session's own scope boundary.)*
- [x] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time — done, via `git mv`, to
      `CAP-051-2026-09-14_21-42-55_21-44-09-Group_AM` (video overlay start/end times).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-051-2026-09-14_21-42-55_21-44-09-Group_AM/CAP-051-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-051-2026-09-14_21-42-55_21-44-09-Group_AM/CAP-051-EVENT-NOTES
