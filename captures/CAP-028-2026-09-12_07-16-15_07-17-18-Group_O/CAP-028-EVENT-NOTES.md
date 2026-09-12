# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group O, Head gestures (`CAP-028`)

**Status:** ✅ **Captured and analyzed 2026-09-12.** Full video (62.68s) and full log (2,187 packets,
untruncated) reviewed — see `CAP-028-FINDINGS.md`. The video's own camera is pointed at the phone
screen, not the user's head/ears — the physical Nod/Shake gestures are genuinely not visible on
camera (matching the draft's own "outside of video" note), so this session's evidence is wire-only
for the gesture window itself; the surrounding connect/app-navigation sequence is video-confirmed.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group O):** main run-through group, never yet
captured. **Requires 'Head gestures' enabled first (Group F, `CAP-020`)** — do not run this
session before `CAP-020` has toggled Head gestures on. Head gestures are a Pixel Buds Pro
2-exclusive feature, officially confirmed present (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` `HEAD-001`);
this session tests whether the gestures themselves (Nod/Shake) generate any wire-visible traffic,
or are purely on-device given they only act on an already-active call/notification context.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-028`                     |
|      Group(s)    |                         O                          |
|       Date       |                     2026-09-12                     |
| Firmware version | ⚪ ASSUMPTION `release_5.203` (carried over; already known working for Head gestures since `CAP-020`, per `PROTOCOL.md` §0.1's `XC-03` note — not re-checked on-screen this session) |
|   Test device    | Pixel 7a, Android 17, Pixel Buds Companion App `1.0.955078536`, Google Play services active |
| Video file       | `CAP-028-recording.mp4` — 62.68s, overlay `07:16:15`–`07:17:18`. **Camera points at the phone screen, not the user's head — the physical Nod/Shake gestures are not visible on camera.** |
| Log file         | `CAP-028-btsnoop_hci.log` — 2,187 packets, 227.71s, `07:16:14.148`–`07:20:01.855`, untruncated |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |         `04:00:6e:...:07`         |

**Preparation (required before starting):**
- Confirm 'Head gestures' is enabled on screen (Device details → Controls and gestures → Head
  gestures) before starting this Group's actions — record how/when this was confirmed. **If it was
  not already enabled in a separate prior session (`CAP-020`), enable it now as this session's own
  first logged step** (see Procedure step 0 below) rather than assuming it's already on.
- Firmware note (documentation only, not currently a blocker): `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §6
  states Head Gestures require firmware ≥4.467, a version scheme distinct from this project's own
  confirmed wire-baseline `"release_5.203"` — `PROTOCOL.md` §0.1's `XC-03` note leaves the numeric
  relationship between the two schemes unreconciled, but also confirms this isn't currently
  blocking anything: Head gestures are already known working on this project's own test device
  since `CAP-020`. Record the firmware version shown on screen for this session regardless, per
  `PROJECT_RULES.md` rule 11's reproducibility requirement.
- Confirm HCI snoop logging is already enabled and running, and the Buds are connected, before
  either the Head-gestures-enable step (if needed) or the Nod/Shake actions begin.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group O — requires Head gestures enabled)

0. **(Only if not already done in a separate session):** enable 'Head gestures' now, as this
   session's own first logged step (Device details → Controls and gestures → Head gestures
   toggle on). Note the exact time. If already confirmed enabled from a prior session, skip this
   step and just record the confirmation per the Preparation note above.
13. **Nod** [`HEAD-002`] (simulating answering a call, or a text reply if 'Spoken notifications'
    is on). Wait. Note time.
14. **Shake** [`HEAD-003`] (simulating rejecting a call/dismissing a text reply). Wait. Note time.

## Event Timeline (corrected against full video review + full log analysis, 2026-09-12)

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 07:16:15 | Video starts; Bluetooth Settings sheet open, "Bluetooth is off." | User | — | Video `t=0`. |
| ~07:16:19–20 | Bluetooth toggled ON | User (Hardware) | — | (not individually re-checked at 1s resolution this pass; draft's `07:16:20` accepted, consistent with the log's own timing below). |
| 07:16:20.912 | Classic `Connect Complete` (stored key, no SSP) | System | — | Log frame 241. |
| 07:16:21.719–.742 | DLCI 0x04 `Get`(`08110000`)/`Notify`(`Settable=0xe8`=**undocked**, `Current=0x40`=Adaptive) | System (auto) | — | Log frames 759/765 — confirms the Buds were **not** in the case at connect time (consistent with being worn for this Group's own test, though this could not be independently confirmed on video — see Findings §2). |
| 07:16:21.689 | **A second, distinct LE connection forms** (chandle `0x0003`, address `7d:0a:16:e6:10:68` — not the Buds' classic address) | Peer device (Auto) | — | Log frame 736 — see Findings §3; this connection's own GATT discovery finds a standard Heart Rate service, matching `CAP-018`'s already-documented unrelated-device pattern. |
| ~07:16:27–29 | User navigates to home screen, reopens the Pixel Buds app ("Device details") | User | — | Video `t=12`–`t=14` (app screen shows "Device details," ANC "Adaptive" selected, `L:100% C:93% R:100%`). |
| 07:16:25.29–07:17:02.87 | The second LE connection's own ATT traffic (`Write Command`→handle `0x0042`, `Handle Value Notification`←handle `0x0044`, the same handle `CAP-016`/`CAP-018` flagged) — dense, recurring | Peer device (Auto) | — | 62 Notify + 40 Write frames, chandle `0x0003` only — **not** the Buds' own classic connection (chandle `0x0002`), which carries zero ATT traffic throughout this session. |
| 07:16:29–07:17:15 | **Head-gesture testing window (physical Nod/Shake, per the draft) — not camera-visible; app screen stays on "Device details," unchanged** | User (Hardware) | `HEAD-002`, `HEAD-003` | Video spot-checked at `t=30,38,42,46,50,54,58` — identical static screen throughout, buds/case position ambiguous on video (see Findings §2). **Zero DLCI 0x02/0x04/0x08 traffic on the Buds' own chandle anywhere in this entire window** (log query `07:16:25`→`07:17:15` returns 0 rows) — see Findings §4. |
| 07:17:15.550 | The periodic DLCI 0x02/0x04/0x08 cross-channel push fires (routine, not gesture-related — matches the already-documented `CAP-036`/`CAP-042` pattern) | System (auto) | — | Log frames 1834–1847. |
| 07:17:18 | Video ends (62.68s) | System | — | Video `t=62`. |
| 07:17:15–07:20:02 (log tail, beyond video) | Log continues past video end; two further periodic pushes (07:18:02, 07:19:35), no other Buds-attributable RFCOMM traffic | System (auto) | — | Log frames 1925–2120. |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [x] Check whether the gesture generates any RFCOMM/GATT traffic at all, or is purely on-device
      (e.g. only relevant if a call/notification is actually active — note whether that condition
      was met during this capture). → **No active call/notification is visible anywhere in the
      video or log** (no SCO/eSCO connection, no incoming-call/notification UI) — the app screen
      stays on the static "Device details" view throughout. Zero DLCI 0x02/0x04/0x08 traffic on the
      Buds' own chandle during the claimed gesture window — a clean negative, consistent with the
      app's own documented behavior (Nod/Shake only act on an active call/notification, which this
      session never had).
- [x] If traffic is found, identify which DLCI/channel carries it. → N/A on the Buds' own channels;
      a separate, unrelated-device LE connection (chandle `0x0003`) carries substantial ATT traffic
      overlapping this window, but is not attributable to the Buds (see `CAP-028-FINDINGS.md` §3).
- [x] Compare structure against `PROTOCOL.md` §2's envelope hypotheses per the usual process. → N/A,
      no Buds-attributable traffic to compare.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `HEAD-002`/`HEAD-003` are clearly referenced above.
- [x] Write `CAP-028-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [x] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-028-2026-09-12_07-16-15_07-17-18-Group_O/CAP-028-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-028-2026-09-12_07-16-15_07-17-18-Group_O/CAP-028-EVENT-NOTES
