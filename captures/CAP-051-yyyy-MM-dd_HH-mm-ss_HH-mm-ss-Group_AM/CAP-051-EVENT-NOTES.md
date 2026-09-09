# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AM (new), `qhr` field 13 ANC-parallel-path wire confirmation (`CAP-051`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-051-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AM` to the actual session
date/start-time/end-time, e.g. `CAP-051-2026-09-15_08-30-00_08-40-00-Group_AM`.

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
flagged here as a follow-up, not silently assigned one. The existing `ANC-*` Test-IDs cover the
UI-level ANC mode change itself; this session's own question (which channel(s) carry the write) is
distinct from those.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-051`                     |
|      Group(s)    |                     AM (new)                       |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, official Pixel Buds Companion App; full DLCI 0x02 traffic retained, raw-path extraction) |
| Video file       |    TBD — must clearly show the in-app tap and, separately, the physical press-and-hold gesture, each in isolation |
| Log file         |             TBD — `CAP-051-btsnoop_hci.log` (raw path, not `btsnooz.py`) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AM)

1. With full DLCI 0x02 traffic retained (raw-path extraction, not `btsnooz.py`), perform an
   isolated ANC-mode change via the **in-app** `QuickActionsFragment` toggle group (a single tap,
   pause ≥10s before the next action, matching `CAP-006`'s own isolated-tap discipline).
2. Separately, perform an isolated ANC-mode change via a **physical press-and-hold gesture** on one
   earbud (again, single gesture, pause ≥10s).

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Session start, raw-path HCI snoop logging confirmed | User | `ANC`-family | Conn. state: TBD |
| TBD | In-app ANC-mode tap (`QuickActionsFragment` toggle group), single isolated tap. Pause ≥10s | User (App) | `ANC`-family | TBD |
| TBD | Physical press-and-hold ANC-mode gesture, single isolated gesture. Pause ≥10s | User (Hardware) | `TOUCH-007` | TBD |
| TBD | Session end | — | — | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AM)

- [ ] For the in-app tap: check whether a `field5{field4{field13=N}}` write appears on DLCI 0x02 at
      that moment.
- [ ] For the physical gesture: check whether the same `field5{field4{field13=N}}` write appears on
      DLCI 0x02 at that moment.
- [ ] For each action, note whether it's time-correlated with the tap/gesture, mirroring `CAP-006`'s
      DLCI-0x04 confirmation methodology.
- [ ] A positive match for either or both actions would be strong evidence DLCI 0x02 *also* carries
      ANC state in parallel to DLCI 0x04's already-confirmed path — record the result plainly
      either way (a positive match, or a confirmed absence).

## Next steps after filling this in

- [ ] Cross-reference this session's own findings against `PROTOCOL.md` §4.1 (DLCI 0x04's confirmed
      ANC path) and `REVERSE_ENGINEERING.md`'s `qhr` field-13 entry (`AGENTS.md` §13's traceability
      check).
- [ ] Write `CAP-051-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Flag, as a follow-up outside this prompt's own scope, whether a dedicated Test-ID for this
      question should be added to `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (see the Purpose section
      above).
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-051-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AM/CAP-051-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-051-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AM/CAP-051-EVENT-NOTES
