# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group P, Voice & case button (`CAP-029`)

**Status:** ✅ **Captured and analyzed 2026-09-12.** Full video (251.55s) and full log (4,944
packets, untruncated) reviewed — see `CAP-029-FINDINGS.md`. **Major correction to the draft:** the
draft's final line ("07:57:56 user forgets the device again") did **not** happen — both video (the
device remains shown "Active"/paired at the very last frame) and wire (no `Delete Stored Link Key`,
no ACL disconnect of the Buds' classic connection, anywhere after `07:56:34`) confirm the device stays
connected through the end of the recording. `CASE-008` (a shorter/different case-button press) was
**not attempted** this session — no second case-button interaction is visible anywhere after the
30s factory-reset hold completes.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group P):** main run-through group, never yet
captured. Requires 'Conversation Detection' enabled first (Group C, `CAP-019`). Item 15
(`CONV-002`) tests whether the voice-triggered detection event itself (pause media, switch to
Transparency) produces a wire command; item 17 (`CASE-008`) targets a genuinely open protocol
question (`PROTOCOL.md` §6) — whether a shorter/different case-button press triggers pairing mode
without a full factory reset, since no officially documented duration exists for that.

> ⚠️ **Item 16 (`CASE-007`) is a confirmed full factory reset, not just pairing mode** — it also
> resets the Find My Device link on the Pro 2. It is **optional and not a prerequisite** for
> anything else in this guide (Group A's lightweight baseline is sufficient on its own). Do this
> deliberately, last, and only once ready to re-pair from scratch. See
> `WORKSTATION_PREPARATIONS.md`'s Disaster Recovery section before running it. If you do trigger
> it, capture the subsequent re-pair as its own isolated session right afterward — that session
> is `PAIR-002`'s genuine home (see `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s definition; do **not**
> tag an ordinary forget-and-re-pair as `PAIR-002` — that was a confirmed mislabeling incident
> fixed on 2026-08-20, see `CHANGELOG.md`).
>
> **Decide before capturing:** run items 15 and 17 only in this session (skip the destructive
> item 16), or run all three including the factory reset. Record the decision here: TBD.
>
> **Decision guidance (not a decision made on the maintainer's behalf):** items 15/17 alone are
> non-destructive and sufficient to close this session's own open questions (`CONV-002` wire
> traffic, `CASE-008`'s shorter-press behavior). Item 16 is worth including in *this specific*
> session only if a genuine, isolated `PAIR-002` (from-true-factory-state pairing) baseline is
> independently wanted — it is not required to answer items 15/17's own questions, and every other
> Group in this guide already works fine against Group A's lightweight forget-and-re-pair baseline.
> If in doubt, prefer running 15/17 only and scheduling the factory reset as its own deliberate,
> separately-planned session later.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-029`                     |
|      Group(s)    |                         P                           |
|       Date       |                     2026-09-12                     |
| Firmware version | ⚪ ASSUMPTION `release_5.203` (carried over; re-confirmed identical peer post-reset, same physical unit) |
|   Test device    | Pixel 7a, Android 17, Pixel Buds Companion App `1.0.955078536`, Google Play services active |
| Video file       | `CAP-029-recording.mp4` — 251.55s, overlay `07:53:53`–`07:58:05` |
| Log file         | `CAP-029-btsnoop_hci.log` — 4,944 packets, 385.64s, `07:53:51.908`–`08:00:17.547`, untruncated |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |         `04:00:6e:...:07`         |
| **Decision on item 16 (`CASE-007`)** | **Run** — the maintainer performed the 30s case-button hold (factory reset), per the video/wire evidence below. |

**Preparation (required before starting):**
- Confirm 'Conversation Detection' is enabled on screen (Device details → Sound → Audio
  intelligence → Conversation detection) before starting item 15. **If it was not already enabled
  in a separate prior session (`CAP-019`), enable it now as this session's own first logged step**
  (see Procedure step 0 below) rather than assuming it's already on.
- Decide the item-16 factory-reset question (see the "Decision guidance" note above) *before*
  starting, and record the decision in this file.
- Confirm HCI snoop logging is already enabled and running, and the Buds are connected, before any
  timed action begins.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group P)

0. **(Only if not already done in a separate session):** enable 'Conversation Detection' now, as
   this session's own first logged step. Note the exact time. If already confirmed enabled from a
   prior session, skip this step and just record the confirmation per the Preparation note above.
15. **Start speaking** with Conversation Detection on [`CONV-002`], to trigger the detection
    event. Wait. Note time.
16. **(Optional, destructive — see warning/decision-guidance above) Hold the case button for 30
    seconds** [`CASE-007`] (case open, buds inside, plugged into power). If run, capture the
    subsequent re-pair as its own isolated session immediately afterward [`PAIR-002`].
17. **(Open question, see `PROTOCOL.md` §6) Try a shorter/different press** [`CASE-008`] on the
    case button to see if it triggers pairing mode without a full reset. No officially confirmed
    duration exists — treat any local finding as `[VERIFIED-LOCAL]` material for
    `CAP-029-FINDINGS.md`.

## Event Timeline (corrected against full video review + full log analysis, 2026-09-12)

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 07:53:53 | Video starts. Bluetooth off. | User | — | Video `t=0`. |
| ~07:53:58 | Bluetooth toggled on | User (Hardware) | — | (not individually re-checked at sub-second resolution). |
| 07:54:02.767 | Classic `Connect Complete` (stored key, no SSP) | System | — | Log frame 370. |
| 07:54:03.253–.269 | DLCI 0x04 `Get`(`08110000`)/`Notify`(`Settable=0xe8`=undocked,`Current=0x40`=Adaptive) | System (auto) | — | Log frames 756/777 — this is the **only** Group `0x08` (ANC) traffic anywhere before the reset (checked, see Findings §2). |
| ~07:54:08–14 | Spotify opened, "Amy Winehouse Best Of" playing | User (App) | — | Video `t=20`, still playing. |
| ~07:54:14–16 | **Media pauses on screen** (play/pause icon flips from pause to play) | User (Hardware) → Buds (Auto)? | `CONV-002` | Video `t=20`→`t=22`: still playing at overlay `07:54:14`, already paused at `07:54:16`. **No DLCI 0x02/0x04/0x08 traffic of any kind accompanies this transition** (checked `07:54:00`–`07:54:30` in full) — see Findings §2 for the significance. |
| ~07:54:55 | Buds "forgotten" in Bluetooth settings (in preparation for the reset) | User (Phone) | — | Video-inferred from the draft's own note; ACL disconnect (reason `0x16`, locally-terminated) at `07:54:54.532` (log frame 2034) matches closely. |
| ~07:55:03–07:55:33 | Case button held for ~30s (case open, buds inside) | User (Hardware) | `CASE-007` | Video `t=68` (button being held, `07:55:02`) through `t=98` (case closed/lifted, `07:55:32`) — a full factory reset, per the app's already-confirmed behavior. |
| 07:55:13.695 | First `Delete Stored Link Key` (during the hold) | System | — | Log frame 2091. |
| 07:55:44.765–07:56:34.348 | Several reconnect/disconnect cycles as the reset settles (4 total `Delete Stored Link Key` events: `07:55:13`, `07:56:12`, `07:56:19`, `07:56:34`) | System (auto) | — | Log frames 2091/2497/2860/3316 — see Findings §3. |
| ~07:56:01–07:56:09 | Reset "Pixel Buds Pro 2" reappears in "Pair new device" list | System/User | `PAIR-002` | Video `t=135` (`07:56:09`), user's finger tapping the entry. |
| 07:56:34.035–07:56:34.653 | **Fresh classic SSP handshake**: `Link Key Request Negative Reply`→`IO Capability Request/Response`→`User Confirmation Request` | System | `PAIR-002` | Log frames 3316–3370 — genuine fresh pairing, not a key-reuse reconnect. |
| ~07:56:45–07:57:09 | First-run onboarding: SDP browse, RFCOMM/HID/AVDTP connects, permission grants (Phone calls/Media audio/Input device) | System/User | `APP-001`/`APP-002` (incidental) | Video `t=195` (`07:57:09`). |
| 07:56:49.53–~07:57:01.59 | A separate LE connection (chandle `0x0009`) performs GATT service discovery then disconnects — routine, not a Buds-classic-link drop | System (auto) | — | Log frames 4038–4482. |
| ~07:57:09–07:58:05 | Device stays connected/"Active" on the "Device details" screen; **no further case-button interaction, no `Forget` tap visible** | — | — | Video spot-checked at `t=235,240,243,246,249,251` — unchanged "Active" state throughout, right to the last frame. **`CASE-008` was not attempted this session.** |
| 07:58:05 | Video ends (251.55s) | System | — | Video `t=251`. |
| (log continues to `08:00:17.547`) | No further Buds-classic-ACL disconnect anywhere in the remaining log | — | — | Confirms the device is still connected when the log itself ends, well past video end — **the draft's claimed `07:57:56` second "Forget" did not happen.** |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [x] `CONV-002`: check whether the Conversation Detection trigger (pause media, switch to
      Transparency) produces a wire-visible command, and if so on which DLCI/channel. → **Clean
      negative**: media visibly pauses, but zero DLCI 0x02/0x04/0x08 traffic of any kind accompanies
      it, and no ANC mode change occurs anywhere in the pre-reset session.
- [x] `CASE-007` (if run): confirm this reproduces the previously-documented factory-reset
      behavior. → Confirmed — matches the established pattern (bond removed, multiple
      `Delete Stored Link Key`/reconnect cycles, followed by a fresh device advertisement).
- [ ] `CASE-008`: record the exact press duration tried and whether pairing mode triggered. →
      **Not attempted this session** — no second case-button interaction occurs anywhere after the
      reset; this open question remains untested.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `CONV-002`/`CASE-007`/`PAIR-002` are clearly referenced above
      (`CASE-008` explicitly not exercised, noted as such).
- [x] Write `CAP-029-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [x] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-029-2026-09-12_07-53-53_07-58-05-Group_P/CAP-029-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-029-2026-09-12_07-53-53_07-58-05-Group_P/CAP-029-EVENT-NOTES
