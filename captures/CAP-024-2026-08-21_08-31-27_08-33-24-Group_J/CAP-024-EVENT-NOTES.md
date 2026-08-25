# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group J, In-ear detection & case sounds (`CAP-024`)

**Status:** ✅ Captured and analyzed. Video reviewed via tiled contact sheets (full pass) and
targeted single-frame extraction; wire log searched for the DLCI 0x02 `field5{field4{...}}`
settings-write shape across the whole session, cross-checked against video timing.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group J):** attribute the wire commands for In-ear
detection and the two case-sound settings.

**Location note:** In-ear detection lives on **Device details → More settings** (top item under
"Device settings", alongside Multipoint — confirming `CAP-019`'s Multipoint change persisted
across sessions, both still showing ON at this session's start). Case sounds is a separate
sub-screen (**More settings → Case sounds**) with two toggles labeled **"Bud return"** (=
`CASE-001`, "Earbuds replaced" in the skeleton's wording) and **"Other alerts"** (= `CASE-002`,
"Other notifications").

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-024`                     |
|      Group(s)    |                         J                          |
|       Date       |                     2026-08-21                     |
| Firmware version |    not queried this session (⚪ ASSUMPTION `release_5.203`, confirmed on-screen the same day in `CAP-023`) |
|   Test device    | Pixel 7a, Android 17 (⚪ ASSUMPTION), official Pixel Buds Companion App (version not visible on screen) |
| Video file       |            `CAP-024-recording.mp4` (116.7s, 08:31:27–08:33:24 local time) |
| Log file         |             `CAP-024-btsnoop_hci.log` (08:30:57.018–08:35:18.662) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` — same physical device as `CAP-016`/`CAP-019`–`CAP-023` |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group J)

35. **Toggle 'In-ear detection' on/off** [`INEAR-001`].
36. **Toggle case sound 'Earbuds replaced' on/off** [`CASE-001`].
37. **Toggle case sound 'Other notifications' on/off** [`CASE-002`].

## Event Timeline

| Time (local) | Action | Initiator | Test-ID | Wire evidence |
|---|---|---|---|---|
| 08:31:27 | Video start | — | — | Video first frame |
| 08:31:3x–08:32:0x | Bluetooth reconnect; navigate Device details → More settings | User (App) | — | Connection handshake burst |
| **08:32:10.980** (video: In-ear detection ON at t=36s, OFF by t=52s) | **In-ear detection ON→OFF** | User (App) | `INEAR-001` | Frame 1850 |
| **08:32:23.091** (video: ON again by t=58s) | **In-ear detection OFF→ON** | User (App) | `INEAR-001` | Frame 1912 |
| — | Navigate More settings → Case sounds (tap "Case sounds") | User (App) | — | No RFCOMM write (navigation) |
| **08:32:38.084** (video: "Bud return" shown OFF at t=72s, shortly after this screen opens — see caveat below) | **"Bud return" state = OFF** | User (App) / possibly screen-open sync | `CASE-001` | Frame 1988 — see caveat in §Decode |
| **08:32:50.500** (video: finger taps "Bud return" toggle ≈t=82s) | **"Bud return" OFF→ON** | User (App) | `CASE-001` | Frame 2023 |
| **08:33:02.060** (video: finger taps "Other alerts" toggle ≈t=94–97s) | **"Other alerts" ON→OFF** | User (App) | `CASE-002` | Frame 2053 |
| **08:33:12.485** (video: finger taps "Other alerts" toggle again ≈t=102–105s) | **"Other alerts" OFF→ON** | User (App) | `CASE-002` | Frame 2084 |
| 08:33:24 | Video end | — | — | Video last frame |

## Decode (DLCI 0x02, `libmaestro` Pigweed `pw_hdlc` channel, `PROTOCOL.md` §2.2a)

All 6 frames CRC-32 verified (`CAP-020-FINDINGS.md` §3's method), decoded from payload offset 13:

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
    "INEAR OFF (1850)": "7e003b0310131dea71de7d5e251d9a8c9e2a0422021000904a3dfc7e",
    "INEAR ON  (1912)": "7e003b0310131dea71de7d5e251d9a8c9e2a0422021001067a3a8b7e",
    "CASE1 OFF (1988)": "7e003b0310131dea71de7d5e251d9a8c9e2a052203e00100d2b7cefd7e",
    "CASE1 ON  (2023)": "7e003b0310131dea71de7d5e251d9a8c9e2a052203e001014487c98a7e",
    "CASE2 OFF (2053)": "7e003b0310131dea71de7d5e251d9a8c9e2a052203d80100fa03b6d77e",
    "CASE2 ON  (2084)": "7e003b0310131dea71de7d5e251d9a8c9e2a052203d801016c33b1a07e",
}
for name, hx in frames.items():
    raw = bytes.fromhex(hx)
    un = unescape_hdlc(raw[1:-1])
    body, trailer = un[:-4], un[-4:]
    assert struct.pack('<I', binascii.crc32(body) & 0xffffffff) == trailer   # CRC-32 verified, all 6
    addr, i = leb128(body, 0); ctrl = body[i]; i += 1
    print(name, "->", body[i:].hex())
```

Decoded (all `field5{field4{...}}`):

```
INEAR OFF/ON: field2 = 0 / 1
CASE1  OFF/ON: field28 = 0 / 1
CASE2  OFF/ON: field27 = 0 / 1
```

**🟡 HYPOTHESIS:** `field 2` = In-ear detection, `field 28` = "Bud return" (`CASE-001`), `field 27`
= "Other alerts" (`CASE-002`) — each with a clean ON/OFF pair.

**Caveat on `CASE1 OFF` (frame 1988, 08:32:38.084):** this write lands almost exactly when the
"Case sounds" screen opens (video shows the screen fully loaded, toggle already OFF, at t=72s /
08:32:39 — 1s later), rather than at a clearly video-visible tap. It is **not disambiguated from
video alone** whether this is a genuine user tap that happened to re-affirm the already-OFF state,
or the screen populating itself with the current (already-OFF) value on open. The **second**
frame for this Test-ID (`CASE1 ON`, 08:32:50.500) unambiguously matches a video-visible tap. Noted
here rather than silently treated as two equally-confident samples.

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [x] Identify which DLCI/channel carries each toggle's command frame. → DLCI 0x02 for all three.
- [x] Check whether case-sound writes target a case-specific field distinct from bud-targeted
      writes. → No distinguishing marker found; both case-sound settings use the same
      `field5{field4{...}}` envelope as every other setting in this batch, just different inner
      field numbers (27/28) — no separate "case" vs. "bud" channel or address observed.
- [x] Compare structure against `PROTOCOL.md` §2's envelope hypotheses. → Matches exactly.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise — `INEAR-001`/`CASE-001`/
      `CASE-002` all referenced above.
- [x] Write `CAP-024-FINDINGS.md` per `PROJECT_RULES.md` §2.
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index.
- [x] Rename this capture's folder to the actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-024-2026-08-21_08-31-27_08-33-24-Group_J/CAP-024-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-024-2026-08-21_08-31-27_08-33-24-Group_J/CAP-024-EVENT-NOTES
