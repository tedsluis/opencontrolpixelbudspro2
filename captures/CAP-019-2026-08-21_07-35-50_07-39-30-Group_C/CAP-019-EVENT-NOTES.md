# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group C, Conversation Detection & Multipoint (`CAP-019`)

**Status:** ✅ Captured and analyzed. Video reviewed frame-by-frame against its burned-in
wall-clock overlay (full pass via tiled contact sheets, sub-second re-checks around both actions),
cross-correlated against the RFCOMM log per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group C):** main run-through group, never previously
captured — attributes the wire commands for toggling Conversation Detection and Multipoint.
**Correction against the skeleton's assumed location:** Conversation Detection is **not** a
top-level "Device details" item — it lives inside **Device details → Sound → Audio intelligence**.
Multipoint lives inside **Device details → More settings**. Both are distinct sub-screens, not
adjacent controls.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-019`                     |
|      Group(s)    |                         C                          |
|       Date       |                     2026-08-21                     |
| Firmware version |    not queried this session (⚪ ASSUMPTION `release_5.203`, consistent with every other capture on this Buds unit — not re-confirmed on-the-wire or on-screen here) |
|   Test device    | Pixel 7a, Android 17 (⚪ ASSUMPTION — not re-confirmed on screen this session), official Pixel Buds Companion App (version not visible on screen) |
| Video file       |            `CAP-019-recording.mp4` (220.5s, 07:35:50–07:39:30 local time, from the video's own burned-in overlay) |
| Log file         |             `CAP-019-btsnoop_hci.log` (07:34:41.723–07:41:23.329, a longer, non-restarted window bracketing the video) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` (classic peer — same address documented in `CAP-016`/`CAP-020`) |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group C)

6. **Toggle 'Conversation Detection' on/off** [`CONV-001`]. Wait. Note time.
7. **Toggle 'Multipoint' on/off** [`MULTI-001`]. Wait. Note time.

**Actual session shape (video-confirmed):** t=0–~28s is Bluetooth reconnect + app navigation
(quick-settings → connect → open app → briefly detour into system audio volume, not part of either
Test-ID) → "Device details". Both settings were found **OFF** at the start and switched **ON**
once each (a single isolated OFF→ON transition per Test-ID, not a second opposite-direction repeat).

## Event Timeline

| Time (local) | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 07:35:50 | Video start | — | — | Video first frame |
| 07:35:5x–07:36:2x | Bluetooth reconnect; app opened; brief detour into system "Sound" (media volume) screen, then "Device details" → "Sound" (the Buds' own sound screen, distinct from the system one) | User (App) | — | Connection/handshake burst on DLCI 0x02/0x04/0x08, matches `CAP-004-FINDINGS.md` §5a's known one-time capability shape |
| **07:36:41** (tap; confirmed ON on-screen 07:36:42) | **Toggle 'Conversation detection' OFF→ON**, under Device details → Sound → Audio intelligence | User (App) | `CONV-001` | **Frame 1808** (DLCI 0x02, `Sent`/ctrl `0x4b`, 07:36:40.238522) — see §Decode. Rcvd echo: frames 1812 (07:36:40.322594), 1813 (07:36:40.323574) |
| 07:36:5x–07:37:5x | Navigate back to Device details → "More settings" | User (App) | — | No RFCOMM data (navigation only) |
| **~07:38:01.6** (wire write; on-screen toggle transition visible at t≈132s/07:38:02, confirmed ON by 07:38:03) | **Toggle 'Multipoint' OFF→ON**, under Device details → More settings | User (App) | `MULTI-001` | **Frame 2293** (DLCI 0x02, `Sent`/ctrl `0x4b`, 07:38:01.609130) — see §Decode. Rcvd echo: frame 2295 (07:38:01.640548). **Also triggers a Fast Pair Message Stream SASS burst** on DLCI 0x04 (Group `0x07`, frames 2296–2319, 07:38:01.641–07:38:02.047) — see §Decode. |
| 07:38:06 | Routine periodic DLCI 0x08 Group `0x04` Code `0x12`-family alternation + one-time-capability-shaped DLCI 0x08 traffic recurs (frames 2326–2332) | Buds (Auto) | — | Not attributable to either Test-ID — same already-documented periodic/channel-reopen pattern as `CAP-020-FINDINGS.md` §5 |
| 07:39:30 | Video end | — | — | Video last frame |

## Decode (DLCI 0x02, `libmaestro` Pigweed `pw_hdlc` channel, `PROTOCOL.md` §2.2a)

Both `Sent`-direction (phone→Buds, ctrl `0x4b`) frames verified with the standard CRC-32 method
(`PROTOCOL.md` §2.2a) and walked as protobuf wire-format tags from payload offset 13 (offsets 0–12
are the same constant, cross-session-stable prefix documented in `CAP-020-FINDINGS.md` §3/§4):

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
    "CONV-001": "7e004b0310151dea71de7d5e251d9a8c9e2a052203b00101b46fc4c07e",   # frame 1808
    "MULTI-001": "7e004b0310151dea71de7d5e251d9a8c9e2a04220258013b536cdb7e",  # frame 2293
}
for name, hx in frames.items():
    raw = bytes.fromhex(hx)
    un = unescape_hdlc(raw[1:-1])
    body, trailer = un[:-4], un[-4:]
    assert struct.pack('<I', binascii.crc32(body) & 0xffffffff) == trailer   # CRC-32 verified, both
    addr, i = leb128(body, 0); ctrl = body[i]; i += 1
    payload = body[i:]
    print(name, hex(addr), hex(ctrl), payload.hex())
```

Output: `CONV-001 0x0 0x4b 0310151dea71de7e251d9a8c9e2a052203b00101` /
`MULTI-001 0x0 0x4b 0310151dea71de7e251d9a8c9e2a0422025801`.

Walking from offset 13 (mechanical tag decode, `field=tag>>3`, `wiretype=tag&7`):

```
CONV-001:  [off 13] field=5 wt=LD len=5  span=15..20
             [off 15] field=4 wt=LD len=3  span=17..20
               [off 17] field=22 wt=varint value=1
MULTI-001: [off 13] field=5 wt=LD len=4  span=15..19
             [off 15] field=4 wt=LD len=2  span=17..19
               [off 17] field=11 wt=varint value=1
```

Both match the same `field 5{ field 4{ <inner field>=<value> } }` outer wrapper first identified
in `CAP-020-FINDINGS.md` §5 for touch controls/head gestures — a fourth and fifth data point for
that wrapper being general-purpose, not feature-specific. Running field-number table so far (all
🟡 HYPOTHESIS, one capture each):

| Setting | Inner field # | Value | Capture |
|---|---|---|---|
| Touch controls ON | 4 | 1 | `CAP-020` |
| Head gestures ON | 29 | 2 | `CAP-020` |
| Conversation detection ON | 22 | 1 | `CAP-019` |
| Multipoint ON | 11 | 1 | `CAP-019` |

**New this capture — Multipoint additionally triggers a Fast Pair Message Stream burst on DLCI
0x04, Group `0x07` (SASS, per `PROTOCOL.md` §2.3's table):** frames 2296–2319 (07:38:01.641–
07:38:02.047), immediately following the DLCI 0x02 write above. Sample frames:
`07 11 00 04 01 02 b8 00` (Code `0x11`), `07 21 00 00` (Code `0x21`),
`07 41 00 16 69 6e 2d 75 73 65 ...` (Code `0x41`, len `0x16`=22, value starts with ASCII
`69 6e 2d 75 73 65` = `"in-use"`), `07 40 00 11 ...` / `07 42 00 11 ...` (Codes `0x40`/`0x42`, both
len 17, opaque), `07 34 00 0c 01 ...` (Code `0x34`, len 12, opaque, recurs 3× across this burst
and again at 07:38:06 outside any action window — likely a periodic/keepalive SASS code, not
Multipoint-specific), ACKs `ff 01 00 03 07 40 00` / `ff 01 00 03 07 42 00` /
`ff 01 00 08 07 41 69 6e 2d 75 73 65`. **This directly confirms
`CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s own Group C hint** ("Multipoint may trigger an SDP/connection
update, not just an RFCOMM command") — Multipoint activates SASS (Smart Audio Source Switching)
Message Stream traffic, previously only structurally identified (`PROTOCOL.md` §2.3) with no
content correlated to any specific user action.

**Not decoded further, flagged rather than guessed:** the remaining Group `0x07` codes' non-ASCII
payloads (`0x11`, `0x21`, `0x40`, `0x42`, `0x34`) — no official Fast Pair SASS extension page was
consulted this session to check field-by-field meaning; the `"in-use"` string (Code `0x41`) is the
only immediately human-readable content.

DLCI 0x04's `08 13 00 04 01 e8 e8 80` (frame 2277, 07:38:00.916, ~0.7s before the Multipoint write)
is the already-documented periodic/on-change "Notify ANC state" report (`PROTOCOL.md` §4.1, Code
`0x13`) — coincidental timing, **not** attributed to `MULTI-001`.

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [x] Identify which DLCI/channel carries each toggle's command frame. → Both on DLCI 0x02
      (`libmaestro` Pigweed `pw_hdlc`); Multipoint additionally triggers DLCI 0x04 Group `0x07`
      (SASS) traffic.
- [x] Note per `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s own text: Multipoint may trigger an
      SDP/connection update, not just an RFCOMM command — **confirmed**, see §Decode above.
- [x] Compare structure against `PROTOCOL.md` §2's envelope hypotheses per the usual process. →
      DLCI 0x02: matches the CRC-32/HDLC envelope exactly; DLCI 0x04: matches the official Message
      Stream `[Group][Code][Len][Value]` shape exactly.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise — `CONV-001`/`MULTI-001`
      both referenced above.
- [x] Write `CAP-019-FINDINGS.md` per `PROJECT_RULES.md` §2.
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index.
- [x] Rename this capture's folder to the actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-019-2026-08-21_07-35-50_07-39-30-Group_C/CAP-019-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-019-2026-08-21_07-35-50_07-39-30-Group_C/CAP-019-EVENT-NOTES
