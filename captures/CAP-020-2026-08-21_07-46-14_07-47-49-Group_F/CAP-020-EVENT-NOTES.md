# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group F, Touch & head gesture toggles (`CAP-020`)

**Status:** ✅ Captured and analyzed. Video reviewed frame-by-frame against its burned-in
wall-clock overlay (full pass at 1s resolution via tiled contact sheets, sub-second re-checks
around both actions), cross-correlated against the RFCOMM log per `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
§5.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group F):** main run-through group — attributes the
wire commands for the top-level Touch controls / Head gestures on-off toggles. **Note:** Group O
(Head gestures physical actions, `CAP-028`) requires 'Head gestures' enabled — this session leaves
it ON at the end, satisfying that dependency.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-020`                     |
|      Group(s)    |                         F                          |
|       Date       |                     2026-08-21                     |
| Firmware version |    not queried this session (⚪ ASSUMPTION `release_5.203`, consistent with every other capture on this Buds unit to date — not re-confirmed on-the-wire or on-screen here) |
|   Test device    | Pixel 7a, Android 17 (⚪ ASSUMPTION — not re-confirmed on screen this session; same physical device used throughout this project), official Pixel Buds Companion App (version not visible on screen) |
| Video file       |            `CAP-020-recording.mp4` (95.2s, 07:46:14–07:47:49 local time, from the video's own burned-in overlay) |
| Log file         |             `CAP-020-btsnoop_hci.log` (07:44:34.048–07:50:49.945, a longer, non-restarted window bracketing the video) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` (classic peer — same address documented in `CAP-016-FINDINGS.md`) |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group F)

21. **Toggle 'Touch controls' fully on/off** [`TOUCH-001`]. Wait. Note time.
22. **Toggle 'Head gestures' fully on/off** [`HEAD-001`]. Wait. Note time.

**Actual session shape (video-confirmed):** t=0–24s is Bluetooth reconnect (Bluetooth was off at
video start) and app navigation (Bluetooth quick-settings → connect → open Pixel Buds app →
"Device details" → "Controls and gestures"), not itself part of either Test-ID. Both toggles were
found **already OFF** at the start of this session and were switched **ON** once each — a single
isolated action per toggle, matching the Group's "on/off" framing as one directional transition
captured this session (not a second, opposite-direction repeat).

## Event Timeline

| Time (local) | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 07:46:14 | Video start (Bluetooth off, quick-settings panel open) | — | — | Video first frame |
| 07:46:19–07:46:20 | Bluetooth turned on; Buds reconnect; RFCOMM DLCIs 0x02/0x04/0x08/0x0c (re)open | User / Auto | — | Frames 892–977 (DLCI 0x08 one-time capability handshake, matches `CAP-004-FINDINGS.md` §5a's known shape) |
| 07:46:2x–07:46:44 | Navigate: connect device row → open Pixel Buds app → "Device details" → "Controls and gestures" | User (App) | — | No RFCOMM data traffic (navigation only, no query) |
| **07:46:44** (video t=30s, tap seen mid-transition; confirmed ON by t=32s) | **Toggle 'Use touch controls' OFF→ON** | User (App) | `TOUCH-001` | **Frame 1741** (DLCI 0x02, Sent, 07:46:44.850477) — see §Decode below. ACK/Rcvd echo: frames 1749 (07:46:45.106282), 1750/1751 (DLCI 0x08, 07:46:45.140–.142, one-time capability strings — coincidental, part of a channel-reopen burst, not touch-controls-specific), 1753 (07:46:45.142917) |
| 07:47:00 (video t=46s tap; "Optimize head gestures" info dialog shown 07:47:01–07:47:03; final ON confirmed on-screen 07:47:04) | **Toggle 'Use head gestures' OFF→ON** | User (App) | `HEAD-001` | **Frame 1935** (DLCI 0x02, Sent, 07:47:00.005060) — see §Decode below. ACK/Rcvd echo: frames 1939 (07:47:00.441100), 1942 (07:47:00.444725). The wire write precedes the on-screen "Optimize head gestures" explainer dialog by ~1s and has no separate wire action tied to the dialog's dismissal — the dialog is client-side-only. |
| 07:46:56–07:47:49 | Screen remains on "Controls and gestures" with both toggles ON; one incidental, non-navigating tap near the "Left/Active noise control" row around video t=63–66s (07:47:17–20) produces no visible screen change | User (App, incidental) | — | No corresponding DLCI 0x02/0x04/0x08 data frame found in that window — consistent with a tap that didn't register as a state change |
| 07:47:49 | Video end | — | — | Video last frame |

## Decode (DLCI 0x02, `libmaestro` Pigweed `pw_hdlc` channel, `PROTOCOL.md` §2.2a)

Both actions produced a `Sent`-direction (phone→Buds) HDLC frame on DLCI 0x02, address `0x00`,
verified via the exact CRC-32 method established in `PROTOCOL.md` §2.2a / `CAP-005-FINDINGS.md` §5a:

```python
import struct, binascii

def unescape_hdlc(data):
    out = bytearray(); i = 0
    while i < len(data):
        b = data[i]
        if b == 0x7d:
            i += 1; out.append(data[i] ^ 0x20)
        else:
            out.append(b)
        i += 1
    return bytes(out)

def leb128(data, i=0):
    val = 0; shift = 0
    while True:
        b = data[i]; val |= (b & 0x7f) << shift; i += 1
        if not (b & 0x80): break
        shift += 7
    return val, i

frames = {
    1741: "7e004b0310151dea71de7d5e251d9a8c9e2a0422022001c5a08a3c7e",  # TOUCH-001
    1935: "7e004b0310151dea71de7d5e251d9a8c9e2a052203e801020641623b7e",  # HEAD-001
}
for fno, hx in frames.items():
    raw = bytes.fromhex(hx)
    un = unescape_hdlc(raw[1:-1])
    body, trailer = un[:-4], un[-4:]
    assert struct.pack('<I', binascii.crc32(body) & 0xffffffff) == trailer
    addr, i = leb128(body, 0); ctrl = body[i]; i += 1
    payload = body[i:]
    print(fno, hex(addr), hex(ctrl), payload.hex())
```

Output: `1741 0x0 0x4b 0310151dea71de7e251d9a8c9e2a0422022001` /
`1935 0x0 0x4b 0310151dea71de7e251d9a8c9e2a052203e80102`.

**CRC-32 verified for both frames (matches `PROTOCOL.md` §2.2a's confirmed algorithm).**

Payload offsets 0–12 (`03 10 15 1d ea 71 de 7e 25 1d 9a 8c 9e`) are byte-for-byte identical between
the two frames, and offsets 3–12 (`1d ea 71 de 7e 25 1d 9a 8c 9e`) also match `CAP-005-FINDINGS.md`
§5a's own payload prefix from a completely different session five days earlier — consistent with
that section's "request/response correlation ID (`call_id`)" reading of this region, now confirmed
stable *across sessions*, not just within one (not previously checked cross-session).

Walking the remainder as standard protobuf wire-format tags (`field=tag>>3`, `wiretype=tag&7`),
mechanically, from offset 13:

```
1741 (TOUCH-001): [off 13] field=5 wt=LD len=4  span=15..19
                     [off 15] field=4 wt=LD len=2  span=17..19
                       [off 17] field=4 wt=varint value=1
1935 (HEAD-001):  [off 13] field=5 wt=LD len=5  span=15..20
                     [off 15] field=4 wt=LD len=3  span=17..20
                       [off 17] field=29 wt=varint value=2
```

**🟡 HYPOTHESIS:** the outer `field 5 { field 4 { ... } }` wrapper is a **general-purpose
`libmaestro` settings-write envelope** on DLCI 0x02, not EQ-specific — `CAP-005-FINDINGS.md` §5a
found the *same* `field 5 { field 4 { ... } }` outer nesting for EQ writes, with EQ's own inner
content (a 2-byte outer tag `field 16`/`field 18`, §5a) differing from what's found here (`field 4`
= varint `1` for touch controls; `field 29` = varint `2` for head gestures). This is a new
structural data point, not previously documented — the shared outer wrapper is consistent with one
common settings-apply mechanism, while each individual setting has its own inner field
number/value inside it. **Not confirmed:** what `field 4`=1 vs. `field 29`=2 specifically encode
(a per-setting ID plus a fixed "enabled" value, or something else) — no official spec or second
independent capture confirms this yet; flagged as open rather than guessed further
(`AGENTS.md` §13's zero-creativity rule).

**Checked and ruled out as event-driven for either toggle specifically:** DLCI 0x08's Group `0x04`
Code `0x16` value (`08 01`/`08 02`, frames 922/1940/2045) also changes near the `HEAD-001` write
(frame 1940, 07:47:00.442) — but the *same* code already appears during the initial connection
handshake (frame 922, 07:46:20.194) and again 35s later with **no** video-visible action nearby
(frame 2045, 07:47:35.546), alternating `0x02`→`0x01`→`0x02`. This matches the already-documented
"irregular-interval alternator" family (`PROTOCOL.md` §6 Resolved item, Code `0x12`) rather than
being caused by the `HEAD-001` tap — **not attributed to either Test-ID**, noted here so a future
session doesn't re-discover this coincidence and mis-attribute it.

DLCI 0x04 (Fast Pair Message Stream) and DLCI 0x0c carried no data-bearing traffic in either
action's window — both toggles are confirmed **not** to ride the official Message Stream.

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [x] Identify which DLCI/channel carries each toggle's command frame. → DLCI 0x02 (`libmaestro`
      Pigweed `pw_hdlc`, `PROTOCOL.md` §2.2a) for both.
- [x] Compare structure against `PROTOCOL.md` §2's envelope hypotheses per the usual process. →
      Matches the DLCI 0x02 HDLC/CRC-32 envelope exactly (100% CRC match, both frames); inner
      content follows the same outer `field 5 { field 4 { ... } }` wrapper first seen in
      `CAP-005-FINDINGS.md` §5a for EQ.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — `TOUCH-001`/`HEAD-001` both referenced above.
- [x] Write `CAP-020-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index.
- [x] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-020-2026-08-21_07-46-14_07-47-49-Group_F/CAP-020-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/#/captures/CAP-020-2026-08-21_07-46-14_07-47-49-Group_F/CAP-020-EVENT-NOTES
