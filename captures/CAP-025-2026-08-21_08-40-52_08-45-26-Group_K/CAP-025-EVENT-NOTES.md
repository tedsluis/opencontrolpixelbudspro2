# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group K, Find My Buds (`CAP-025`)

**Status:** ✅ Captured and analyzed. **Correction to this capture's original task framing:** a
`CAP-025-recording.mp4` (273.7s) **does exist** for this session, contrary to this batch's initial
assumption that this capture had no video — verified directly (`ffprobe`) before writing this file.
The analysis below is therefore video-confirmed like every other capture in this batch, not
log-derived-only. Video reviewed via tiled contact sheets (full pass) and targeted single-frame
extraction, cross-correlated against the wire log per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group K):** the last remaining fully unattributed app
command. `PROTOCOL.md` §4.4 has a concrete, testable hypothesis: Fast Pair Message Stream Action
group (`0x04`), Ring code (`0x01`), worked ACK example `0xFF 0x01 0x00 0x02 0x04 0x01`.

**Major structural finding, changes how this Group's 4 Test-IDs map onto the UI:** the official
app splits "Find My Buds" across **two distinct screens** with two distinct mechanisms:
- **Device details → Find device** — a simple screen with only two buttons, **"Ring Left"** and
  **"Ring Right"** (no Case, no "both" option here). This is what `FIND-001`/`FIND-002` map to.
- **Find device → "Most recent location"** — navigates to a **Find Hub / Find My Device map view**
  with three per-target icons (a combined earbuds icon, a case icon, an individual-bud icon) and
  its own "Play sound" flow, showing a **"Connecting…"** state and text *"If you have another
  device linked with your Google Account, it may try to play sound on Pixel Buds Pro 2"* — i.e.
  this path is Google-account/network-mediated (Find My Device Network), not a direct local
  command. This is what `FIND-003` (Case) and `FIND-004` (both) require.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-025`                     |
|      Group(s)    |                         K                          |
|       Date       |                     2026-08-21                     |
| Firmware version |    not queried this session (⚪ ASSUMPTION `release_5.203`) |
|   Test device    | Pixel 7a, Android 17 (⚪ ASSUMPTION), official Pixel Buds Companion App (version not visible on screen) |
| Video file       |            `CAP-025-recording.mp4` (273.7s, 08:40:52–08:45:26 local time) |
| Log file         |             `CAP-025-btsnoop_hci.log` (08:40:49.737–08:47:14.185, brackets the video) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | Not independently confirmed this session |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group K)

38. **Play sound on Left earbud** [`FIND-001`].
39. **Play sound on Right earbud** [`FIND-002`].
40. **Play sound on Case** [`FIND-003`].
41. **Play sound on both earbuds simultaneously** [`FIND-004`].

## Event Timeline

| Time (local) | Action | Initiator | Test-ID | Wire evidence |
|---|---|---|---|---|
| 08:40:52 | Video start | — | — | Video first frame |
| 08:40:5x–08:41:2x | Bluetooth reconnect; navigate Device details → Find device | User (App) | — | Connection handshake burst |
| **08:41:30.234** (video: tap "Ring Right" at t=38s; status → "Right earbud volume increasing…", button → "Mute Right") | **Play sound on Right earbud (start)** | User (App) | `FIND-002` | Frame 2040 (+retransmit 2045); ACKs 2044, 2048 — see §Decode |
| **08:41:41.243** (video: tap "Mute Right" ≈t=49s; status returns to "Connected") | Stop ringing Right (part of the `FIND-002` action's own lifecycle, not a separate Test-ID) | User (App) | `FIND-002` | Frame 2120 (+retransmit 2124); ACKs 2123, 2127 |
| **08:41:46.703** (video: tap "Ring Left" at t=54s; status → "Left earbud volume increasing…", button → "Mute Left") | **Play sound on Left earbud (start)** | User (App) | `FIND-001` | Frame 2131 (+retransmit 2134); ACKs 2133, 2137 |
| **08:41:58.882** (video: tap "Mute Left" at t=67s; status returns idle) | Stop ringing Left (part of `FIND-001`'s lifecycle) | User (App) | `FIND-001` | Frame 2180 (+retransmit 2183); ACKs 2182, 2186 |
| 08:42:02.900 | A 5th, asymmetric (single send + single ACK) instance of the same stop-shaped frame — see §Decode caveat | — | `FIND-001` (likely a residual retry) | Frame 2202; ACK 2204 |
| **08:42:03–08:42:11** (video: t=71s tap "Most recent location" → t≈75s Find Hub map loads) | Navigate to Find Hub / Find My Device map view | User (App) | — | No DLCI 0x04 Group `0x04` traffic accompanies this navigation |
| **~08:42:24–08:44:32** (video: "Connecting…"/"Stop sound" screen visible ≈t=95s/08:42:27 onward; map view with 3 per-target icons visible ≈t=150s/08:43:22 and ≈t=250s/08:45:02 — "Pixel Buds Pro 2 - Left" detail sheet with "Play sound"/"Share ownership") | **Attempted `FIND-003`(Case)/`FIND-004`(both) via Find Hub's own "Play sound" flow** | User (App) | `FIND-003`, `FIND-004` | **No `Group 0x04 Code 0x01` frame appears anywhere in this window** — instead, three full classic-RFCOMM reconnection bursts occur (08:43:42, 08:44:25, 08:45:04, ~40s apart, each matching the standard connection-open capability-handshake shape) — see §Decode |
| 08:45:26 | Video end | — | — | Video last frame |

## Decode (DLCI 0x04, official Fast Pair Message Stream, `PROTOCOL.md` §2.1)

### `FIND-001`/`FIND-002` — confirmed, video-correlated

```
Right, start (frame 2040, 08:41:30.234): 04 01 00 01 01   ACK: ff 01 00 03 04 01 00 / ff 01 00 02 04 01
Right, stop  (frame 2120, 08:41:41.243): 04 01 00 01 00   ACK: (same two-part ACK shape)
Left,  start (frame 2131, 08:41:46.703): 04 01 00 01 02   ACK: (same)
Left,  stop  (frame 2180, 08:41:58.882): 04 01 00 01 00   ACK: (same)
```

Decoded as `[Group:1][Code:1][Len:2BE][Value:1]` (`PROTOCOL.md` §2.1): `Group=0x04` (Action),
`Code=0x01` (Ring) — exact match to `PROTOCOL.md` §4.4's hypothesis, including the Group/Code pair
from the spec's own worked example. **`Value` byte, now resolved with direct video confirmation
(not previously possible without video):**

| Value | Meaning (🟢 video-confirmed this session) |
|---|---|
| `0x00` | Stop / mute (used for both Left and Right) |
| `0x01` | Start ringing the **Right** earbud |
| `0x02` | Start ringing the **Left** earbud |

Every `Sent` frame is immediately retransmitted once (identical bytes), and answered by **two**
distinct ACK shapes: `ff 01 00 02 04 01` (a byte-for-byte match to `PROTOCOL.md` §4.4's spec-quoted
worked example) and `ff 01 00 03 04 01 00` (one extra trailing `0x00` byte, not in the spec's
worked example — plausibly a status/result code, not confirmed further).

**Frame 2202 (08:42:02.900)** repeats the stop shape (`04 01 00 01 00`) once more, with only a
single ACK (`ff 01 00 03 04 01 00`, no second ACK, no retransmission) — video shows no further tap
in this exact window (already back on the idle "Find device" screen by t=70s), so this is most
plausibly a residual retry/retransmission tail of the `FIND-001` stop above rather than a 6th
distinct action — not asserted with certainty.

### `FIND-003`/`FIND-004` — attempted via a different mechanism, no local Ring command observed

The Find Hub "Play sound" flow (video-confirmed active from ≈08:42:27 through at least 08:45:02)
produces **zero** `Group 0x04 Code 0x01` frames anywhere in the log. Instead, three full
classic-connection reopen bursts occur during this exact window:

```
08:43:42.430–.706  full capability-handshake burst (Group 3/7/8, matches connection-open shape)
08:44:25.204–.327  same shape, ~43s later
08:45:04.556–.736  same shape, ~39s later
```

**🟡 HYPOTHESIS:** `FIND-003`(Case)/`FIND-004`(both) route through Google's Find My Device Network
(cloud/account-mediated, per the on-screen "may try to play sound on Pixel Buds Pro 2" copy,
consistent with `DECISIONS.md` ADR-008 scoping Fast Pair account-linked features as a distinct
area from this project's local-protocol focus) rather than the direct local Message-Stream Ring
command confirmed for `FIND-001`/`FIND-002` above. The repeated classic-connection bounces
correlate with, but are not proven to be caused by, the Find Hub flow's own connectivity checks.

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group K / §5)

- [x] Check every frame against `PROTOCOL.md` §4.4's exact hypothesis — Group/Code confirmed
      exactly; ACK shape mostly matches, with one extra byte in a second ACK variant.
- [x] Check whether Left/Right/Case are distinguished via a payload field — **yes for Left/Right**
      (`Value` byte, video-confirmed); **Case is not reached by this mechanism at all**.
- [x] Cross-check structural elements against 2–3 other already-confirmed commands — see
      `CAP-025-FINDINGS.md` §6 (compared against ANC's confirmed DLCI 0x04 command).

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise — `FIND-001`/`FIND-002`
      confirmed; `FIND-003`/`FIND-004` attempted but resolve to a different (network-mediated)
      mechanism, not a wire-visible local command — explicitly flagged, not silently missing.
- [x] Write `CAP-025-FINDINGS.md` per `PROJECT_RULES.md` §2.
- [ ] If confirmed, promote `PROTOCOL.md` §4.4 from 🟡 HYPOTHESIS to 🟢 FACT — **not done here**;
      maintainer sign-off required per `AGENTS.md` §6.
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index.
- [x] Rename this capture's folder to the actual session date/start-time/end-time (video-derived).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-025-2026-08-21_08-40-52_08-45-26-Group_K/CAP-025-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/#/captures/CAP-025-2026-08-21_08-40-52_08-45-26-Group_K/CAP-025-EVENT-NOTES
