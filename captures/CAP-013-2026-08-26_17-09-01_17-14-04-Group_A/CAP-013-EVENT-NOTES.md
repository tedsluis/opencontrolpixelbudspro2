# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group A repeat, logging started before any prior association (`CAP-013`)

**Status:** Reviewed against `CAP-013-recording.mp4` (1s-resolution `ffmpeg` screenshots around every
uncertain moment, using the video's own burned-in wall-clock overlay, bottom-right) and cross-checked
against `CAP-013-btsnooz_hci.log` via `tshark`. Corrects and extends the original draft (which had no
Test-IDs and no wire evidence filled in). See `CAP-013-FINDINGS.md` in this same folder for the
standardized, evidence-graded protocol findings extracted from this correlation — this file is the
*event timeline*, `CAP-013-FINDINGS.md` is *what it means for the protocol*.

**⚠️ Procedure deviation (flagged, not silently corrected — see `CAP-013-FINDINGS.md` §0 for the full
consequence for scope):** the maintainer's advance note for this session was that "the capture starts
only after the 'Forget' action, not before every association/action as `CAP-013` was intended." Verified
directly against both the video and the log rather than accepted as given, per this session's guardrail
4 — and the actual gap is both **differently shaped** and **larger** than that note describes:

1. **No "Forget" tap appears anywhere in this video.** The action actually performed at 17:09:24 is
   **Settings → System → Reset options → Reset Bluetooth & Wi-Fi** — a full reset of *all* Bluetooth
   and Wi-Fi settings phone-wide (screenshot-confirmed, see Event Timeline), not a single-device
   "Forget" from a device's own settings page. This is a broader clearing action than "Forget," not a
   narrower or equivalent one — but it is a *different* action than the one named in the advance note,
   worth recording precisely rather than silently treating the two as interchangeable.
2. **The log does not start at the reset, or even close to it.** `CAP-013-btsnooz_hci.log`'s earliest
   packet is 2026-08-26 17:11:45.799616 (frame 1, `capinfos`-confirmed) — **2 minutes 21 seconds**
   after the 17:09:24 reset, and also after Bluetooth was re-enabled (17:10:43), the case was opened
   (17:10:53), the case's physical pair button was pressed (17:11:14), "Pair new device" was tapped
   (17:11:25), "Pixel Buds Pro 2 van Ted" appeared in the discovery list (17:11:27), and the device was
   tapped in that list (~17:11:31–45). **None of that discovery/inquiry activity is in the log at all.**
   The log's first frame lands almost exactly on the moment the phone begins actively connecting (a
   passive BLE advertising report at 17:11:45.799, immediately followed by `Delete Stored Link Key` at
   17:11:46.737) — i.e. logging effectively started only once the user had already selected the device
   and the phone-side connect sequence was already underway.
3. **The log is also severely ACL-truncated**, the same issue `CAP-012-FINDINGS.md` §1 documented for
   that session: `capinfos` reports an inferred 15-byte packet size limit, and every RFCOMM data frame
   checked (e.g. frame 661, `frame.len`=53 vs. `frame.cap_len`=15) confirms it — this is a `btsnooz`
   fallback extraction, not a raw untruncated log (matching this folder's `CAP-013-btsnooz_hci.log`
   filename, unlike other captures' `-btsnoop_hci.log` naming). This does not affect HCI-event-level
   sequencing (pairing/connection state machine, RFCOMM channel-open/close), which is unaffected by
   application-payload truncation, but it does limit how much of any given RFCOMM payload's *content*
   (DLCI 0x08 Group/Code/Length/Value, DLCI 0x02 HDLC frames) can be read past the first ~15 bytes.

**Consequence for scope (detailed in `CAP-013-FINDINGS.md` §0):** the primary question this capture was
meant to answer — whether a BLE link/valid link key already existed *before* the clearing action
(`CAP-001-FINDINGS.md` §6) — **cannot** be answered from this capture, because the clearing action
itself, and everything between it and the device-selection tap, falls in the un-logged 2m21s gap. The
secondary question (does the subsequent re-pairing show a fresh SSP handshake or key reuse,
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `PAIR-004`) **is** answerable from what was captured, since the
actual classic-link bonding sequence (`Delete Stored Link Key` onward) happens 1s into the log window.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-013`                     |
|      Group(s)    |                    A (repeat)                      |
|       Date       |                     2026-08-26                     |
| Firmware version | release_5.203 (screen-confirmed at 17:13:27, see timeline) |
|   Test device    | Pixel 7a, Android 17 (⚪ assumed — build number not screen-confirmed this session), system Bluetooth settings + Pixel Buds companion app for setup steps only |
| Video file       | `CAP-013-recording.mp4` — 304.4s, 17:09:01–17:14:04 (wall clock, +0200, burned-in overlay) |
| Log file         | `CAP-013-btsnooz_hci.log` — 336.7s, 17:11:45.799616–17:17:22.478814 (wall clock, +0200), 1,747 packets. **`btsnooz`-fallback extraction, ~15-byte inferred capture length per packet — see deviation note above and `CAP-013-FINDINGS.md` §1.** |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:...:6e:07` — independently re-confirmed on the wire this session (not assumed from a prior capture), same address as `CAP-001`–`CAP-012` |

## Event Timeline

Timestamps are the video's own on-screen wall-clock overlay (bottom-right, seconds-resolution); the log
uses the same wall clock (+0200), directly comparable. Screenshots pulled via
`ffmpeg -y -ss <offset> -i CAP-013-recording.mp4 -frames:v 1 <out>.png` at 1s precision around every
timestamp below.

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 17:09:01 | Start of video recording. Screen shows Quick Settings panel, Bluetooth tile visible (not yet touched) | User (App) | — | — (before log start) |
| 17:09:24 | **Settings → System → Reset options → "Reset Bluetooth & Wi-Fi"** confirmation dialog shown (screenshot-confirmed: "This will reset all Wi-Fi & Bluetooth settings. You can't undo this action.") — **not** a single-device "Forget," see deviation note above | User (App) | — | — (before log start) |
| 17:10:43 | Bluetooth quick-settings panel shown, toggle **off** ("Bluetooth is off") — confirms the reset above took effect | User (App) | — | — (before log start) |
| 17:10:53 | Bluetooth toggle turned **on**; case shown open (buds visible, case LED lit amber/orange); "Pair new device" option visible in the Bluetooth panel | User (App/Hardware) | — | — (before log start) |
| 17:11:14 | Buds held in hand near case (consistent with pressing the case's physical pair button), Bluetooth panel with "Pair new device" still shown | User (Hardware) | — | — (before log start) |
| 17:11:25 | "Pair new device" tapped | User (App) | — | — (before log start) |
| 17:11:26–27 | Device-discovery screen shown: phone's own address listed, "Pixel Buds Pro 2 van Ted" appears in "Available devices" | App (Auto) | — | — (before log start) |
| 17:11:31 | Device tapped in the list; screenshot at 17:11:45 (below) shows the device already in a "Connecting..." sub-state, consistent with the tap having occurred a few seconds before that screenshot | User (App) | — | — (before log start) |
| **17:11:45.799616** | **First frame of `CAP-013-btsnooz_hci.log`** — a passive `Rcvd LE Extended Advertising Report`. Screenshot at this exact second shows the "Pair new device" list with "Pixel Buds Pro 2" already showing "Connecting..." | App (Auto) | `PAIR-001` | Frame 1; screenshot `f164.png` |
| 17:11:46.737 | `Sent Delete Stored Link Key` for `04:00:6e:cf:6e:07`, immediately followed by `Sent Create Connection` to the same address | Phone (Auto) | `PAIR-001`/`PAIR-004` | Frames 117/119, hex `01 12 0c 07 07 6e cf 6e 00 04 00` (Delete Stored Link Key, addr reversed = `04:00:6e:cf:6e:07`) — `tshark -r CAP-013-btsnooz_hci.log -Y "frame.number==117" -x` |
| 17:11:47.281 | `Rcvd Connect Complete` (status `0x00`) | Buds (Auto) | `PAIR-001` | Frame 133 |
| 17:11:47.301–306 | `Rcvd Link Key Request` → `Sent Link Key Request **Negative** Reply` → `Rcvd IO Capability Request` → `Sent IO Capability Request Reply` | Both (Auto) | `PAIR-001`/`PAIR-004` | Frames 149–153 — see `CAP-013-FINDINGS.md` §2 for the full fresh-pairing-path match against `PROTOCOL.md` §5.1 |
| 17:11:47.415 | `Rcvd IO Capability Response` | Buds (Auto) | `PAIR-001` | Frame 169 |
| 17:11:48.005–199 | `Rcvd User Confirmation Request` → `Sent User Confirmation Request Reply` (silent, no on-screen passkey shown — same pattern as `CAP-002`/`CAP-003`) | Both (Auto) | `PAIR-001` | Frames 219/227 |
| 17:11:48.525 | `Rcvd Simple Pairing Complete` | Buds (Auto) | `PAIR-001` | Frame 229 |
| 17:11:48.561 | `Rcvd Link Key Notification` (new key stored) → `Rcvd Authentication Complete` | Buds (Auto) | `PAIR-001`/`PAIR-004` | Frames 230/231 |
| 17:11:48.588–843 | `Sent Set Connection Encryption` → `Rcvd Encryption Change` — classic link fully encrypted | Both (Auto) | `PAIR-001` | Frames 247/270 |
| 17:11:50.113–217 | RFCOMM multiplexer + channels open: DLCI `0x00` (control), `0x0c` (channel 6, HFP AT — `AT`/CRLF bytes observed, truncated), `0x08` (channel 4, private envelope), `0x0a` (channel 5, opens but silent, consistent with `CAP-001-FINDINGS.md` §6) | Both (Auto) | `PAIR-001`, incidental `BATT-004` | Frames 539–605 — see `CAP-013-FINDINGS.md` §3 |
| 17:11:51.780 | DLCI `0x04` (Fast Pair Message Stream) opens; Device Information (Group `0x03`) and ANC Get (Group `0x08` Code `0x11`) traffic observed shortly after | Both (Auto) | incidental `BATT-004`, `ANC`-related Get | Frames 785–813 — hex for frame 804: `ef 09 08 11 00` (RFCOMM UIH payload `08 11 00` = Group `0x08` Code `0x11` Length `0x00`) |
| ~17:11:52.463 | A **second**, independent BLE link (`LE Enhanced Connection Complete`) forms — to a **different, random/resolvable** address (`43:8a:82:03:4b:f2`), not the Buds' public classic address — after the classic link and RFCOMM channels are already up | Auto | — | Frame 851; not attributable to the Buds with the evidence in this capture alone — see `CAP-013-FINDINGS.md` §6 |
| 17:11:53 | "Ready to use" pop-up appears on screen | App (Auto) | — | — |
| 17:12:09 | "Start using the device" tapped — Pixel Buds Pro 2 shown as paired | User (App) | — | — |
| 17:12:10 | "Set up device" pop-up appears (hands off to the Pixel Buds companion app) | App (Auto) | `APP-001` | — |
| 17:12:31 | "Set up" tapped; "Allow a connection to your Pixel Buds" permission screen appears | User (App) | `APP-001` | — |
| 17:12:45 | "Continue" tapped; "Allow your Pixel Buds to find..." permission screen appears | User (App) | `APP-002` | — |
| 17:12:51.981–985 | **DLCI `0x02`** (`libmaestro` candidate, Pigweed `pw_hdlc`) opens — over a minute *after* the other four channels, not during the initial multiplexer burst | Both (Auto) | — | Frames 1170/1172 — see `CAP-013-FINDINGS.md` §5 (🟡 single-sample timing correlation with the app-permission flow, not promoted) |
| 17:12:52 | "Allow" tapped on the find-my-device permission screen; a further "allow the Pixel Buds app to access..." permission screen appears | User (App) | — | — |
| 17:13:02 | "Allow" tapped | User (App) | — | — |
| 17:13:27 | Device details screen shows firmware **`release_5.203`** | App (Auto) | — | Consistent with `ADR-012`'s wire-baseline firmware string; not independently re-verified on the wire this session (truncation limits DLCI 0x08 Group `0x03` Code `0x02` payload recovery here, see `CAP-013-FINDINGS.md` §4) |
| 17:13:47 | Device details panel fully shown: Right 100%, Case 13%, Left 100%, ANC controls (Transparency/Adaptive/Off/Noise cancellation), Forget/Disconnect | App (Auto) | incidental `BATT-004` | Screenshot `f_firmware.png`; DLCI `0x08` Group `0x0e` Code `0x01`/`0x02` battery-shaped traffic observed near this window (frames 1423–1425) but Length/Value bytes are truncated, so the 100/100/13 values cannot be cross-checked against the wire this session |
| 17:14:04 | End of video recording | — | — | — |

## Corrections vs. the original draft of this file

- The original draft's timeline had no Test-IDs and no wire-evidence column filled in (all `-`); both
  are filled in above per `AGENTS.md` §13's traceability check.
- The original draft's skeleton assumed the session's clearing action would be a "Forget" tap; the
  actual on-screen action is "Reset Bluetooth & Wi-Fi" from the system Reset-options menu — corrected
  above and in the deviation note, not silently treated as equivalent.
- Two duplicate rows in the original draft ("17:09:24 | user performed a 'reset bluetooth & wifi'")
  are merged into the single confirmed action above.
- Added the log-start-vs-video-timeline gap analysis (deviation note point 2) and the truncation finding
  (point 3), neither of which was in the original draft.

## Traceability check (`AGENTS.md` §13 point 7, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s Group-A Test-IDs)

Group A's associated Test-IDs are `PAIR-001` (primary) and incidental `BATT-004`; the Group-A-repeat note
additionally targets `PAIR-004`. All three are referenced in the timeline above. `APP-001`/`APP-002`
(Pixel Buds app onboarding permission taps) are incidentally exercised here too and referenced above,
though they are not Group A's own assigned Test-IDs.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-013-2026-08-26_17-09-01_17-14-04-Group_A/CAP-013-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-013-2026-08-26_17-09-01_17-14-04-Group_A/CAP-013-EVENT-NOTES
