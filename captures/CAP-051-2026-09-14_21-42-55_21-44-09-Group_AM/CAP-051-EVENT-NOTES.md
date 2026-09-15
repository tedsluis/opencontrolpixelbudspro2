# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AM (new), `qhr` field 13 ANC-parallel-path wire confirmation (`CAP-051`)

**Status:** 🟢 **Video pass + full log analysis complete** (`ai-sessions/0021_CAPTURE_PROMPT_2026_09_15.md`).
See `CAP-051-FINDINGS.md` for the full wire-level analysis, hex+command evidence, and conclusions —
headline result: all four ANC-mode actions confirmed against DLCI 0x04's already-FACT path
(in-app tap = genuine `Set`+ACK+`Notify`; all three physical gestures = `Notify`-only, no `Set`/`Get`,
confirming `CAP-038-FINDINGS.md` §5's mechanism), and a clean, confirmed negative for a `qhr` field-13
write on DLCI 0x02 for all four actions.

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
| Log file         |             `CAP-051-btsnoop_hci.log` (209.66s, 2,457 packets, 0/2,457 `cap_len≠len` mismatches — **confirmed raw path, untruncated**, `CAP-051-FINDINGS.md` §0) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            `04:00:6e:cf:6e:07`             |

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
| 21:42:55 | Video start. Phone screen shows Bluetooth settings, Bluetooth **off**. | User | — | no wire action yet (session start) |
| ~21:43:01–21:43:06 | "Use Bluetooth" toggle switched on; Bluetooth quick-settings panel shows "Pixel Buds Pro 2 van Ted — Connecting…" | User | — | Wire: `Connection Complete` chandle `0x0002`, frame 786, `21:43:07.657` — `CAP-051-FINDINGS.md` §1 |
| ~21:43:16 | Device details screen open (`Pixel Buds Pro 2 van Ted`, Active). On-screen: Left 100%, Case 54%, Right 100%. Active noise control row visible, **"Adaptive" already selected** — this is the screen's resting state when it was opened, not an action performed in this session. | User (App) | — | Wire: initial `Notify ANC state`, frame 1093, `21:43:11.971`, Current=`0x40`=Adaptive — matches on-screen state — `CAP-051-FINDINGS.md` §2 |
| 21:43:26 (tap ~21:43:26, confirmed on-screen by 21:43:27) | **In-app ANC-mode tap** (`QuickActionsFragment` toggle group, Active noise control row): user's finger taps "Transparency". Mode changes on-screen from "Adaptive" to "Transparency". Single isolated tap — no other in-app tap observed anywhere else in the video. | User (App) | `ANC`-family | Wire: `Set ANC state` frame 1527, `21:43:28.107`, new_mode=`0x80`=Transparency; ACK frame 1529; `Notify` frame 1530 confirms — genuine Set+ACK+Notify sequence, the official DLCI 0x04 path. **No DLCI 0x02 write found at this moment (confirmed absence)** — `CAP-051-FINDINGS.md` §2/§3 |
| 21:43:38–~21:43:41/42 (contact visible continuously in this window; mode-change confirmed on-screen by 21:43:42) | **Physical press-and-hold gesture #1**: finger visibly pressed against the earbud (right side of frame; Left/Right identity not independently confirmed from video, see Procedure note above) for ~3–4s. Mode changes on-screen from "Transparency" to "Noise cancellation". | User (Hardware) | `TOUCH-007` | Wire: `Notify ANC state` frame 1707, `21:43:43.336`, Current=`0x08`=Noise cancellation — **no preceding `Set`(`0x12`) or `Get`(`0x11`) anywhere in the log**, matching `CAP-038-FINDINGS.md` §5's "Notify without Set" pattern. **No DLCI 0x02 write found (confirmed absence)** — `CAP-051-FINDINGS.md` §2/§3 |
| ~21:43:51/52–~21:43:54 (contact visible continuously in this window; mode-change confirmed on-screen by 21:43:55) | **Physical press-and-hold gesture #2**: same earbud, finger pressed again for ~3s. Mode changes on-screen from "Noise cancellation" to "Adaptive". Gap since gesture #1's release (~21:43:42) to this gesture's contact start (~21:43:51/52) is **~9–10s — at or just under** the planned ≥10s isolation window. | User (Hardware) | `TOUCH-007` | Wire: `Notify ANC state` frame 1754, `21:43:56.002`, Current=`0x40`=Adaptive — again no `Set`/`Get`. **No DLCI 0x02 write found (confirmed absence)** — `CAP-051-FINDINGS.md` §2/§3 |
| ~21:44:02–~21:44:03 (contact visible continuously in this window; mode-change confirmed on-screen by 21:44:04) | **Physical press-and-hold gesture #3**: same earbud, finger pressed a third time for ~2s. Mode changes on-screen from "Adaptive" to "Transparency". Gap since gesture #2's release (~21:43:55) to this gesture's contact start (~21:44:02) is **~7s — clearly under** the planned ≥10s isolation window. | User (Hardware) | `TOUCH-007` | Wire: `Notify ANC state` frame 1801, `21:44:05.102`, Current=`0x80`=Transparency — again no `Set`/`Get`. **No DLCI 0x02 write found (confirmed absence)** — `CAP-051-FINDINGS.md` §2/§3 |
| 21:44:04–21:44:09 | No further action observed. Device details screen remains open, ANC mode remains "Transparency" through video end. Session/video end at 21:44:09. | — | — | Wire: only the already-documented ~10s periodic DLCI 0x02/0x04/0x08 push recurs (e.g. frame 1796, `21:44:03.087`) — `CAP-051-FINDINGS.md` §3 |

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

- [x] For the in-app tap (~21:43:26–27): check whether a `field5{field4{field13=N}}` write appears on
      DLCI 0x02 at that moment. — **Confirmed absence.** `CAP-051-FINDINGS.md` §3.
- [x] For physical gesture #1 (~21:43:38–42): check whether the same `field5{field4{field13=N}}`
      write appears on DLCI 0x02 at that moment. — **Confirmed absence.** `CAP-051-FINDINGS.md` §3.
- [x] For physical gesture #2 (~21:43:51/52–55): same check. — **Confirmed absence** (the reduced
      isolation gap is immaterial to a negative result — there is no hit to mis-attribute).
      `CAP-051-FINDINGS.md` §3.
- [x] For physical gesture #3 (~21:44:02–04): same check. — **Confirmed absence**, same reasoning.
      `CAP-051-FINDINGS.md` §3.
- [x] For each action, note whether it's time-correlated with the tap/gesture, mirroring `CAP-006`'s
      DLCI-0x04 confirmation methodology. — done; all four DLCI 0x04 events individually
      frame/time-correlated to the video, `CAP-051-FINDINGS.md` §2.
- [x] A positive match for any action would be strong evidence DLCI 0x02 *also* carries ANC state in
      parallel to DLCI 0x04's already-confirmed path — record the result plainly either way. —
      **Recorded: a clean, confirmed absence for all four actions**, not a positive match.
      `CAP-051-FINDINGS.md` §3/§6.

## Next steps after filling this in

- [x] Cross-reference this session's own findings against `PROTOCOL.md` §4.1 (DLCI 0x04's confirmed
      ANC path) and `REVERSE_ENGINEERING.md`'s `qhr` field-13 entry (`AGENTS.md` §13's traceability
      check). — done, `CAP-051-FINDINGS.md` §2/§3/§6.
- [x] Write `CAP-051-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a). — done.
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path. —
      done.
- [x] Flag, as a follow-up outside this prompt's own scope, whether a dedicated Test-ID for this
      question should be added to `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (see the Purpose section
      above). — a candidate ID (`ANC-005`) is proposed in `CAP-051-FINDINGS.md` §5 item 4, awaiting
      maintainer sign-off — not unilaterally added.
- [x] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time — done, via `git mv`, to
      `CAP-051-2026-09-14_21-42-55_21-44-09-Group_AM` (video overlay start/end times).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-051-2026-09-14_21-42-55_21-44-09-Group_AM/CAP-051-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-051-2026-09-14_21-42-55_21-44-09-Group_AM/CAP-051-EVENT-NOTES
