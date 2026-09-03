# CAP-022: Audio & Volume Settings (Group H)

Standardized, evidence-based extraction from `CAP-022-btsnoop_hci.log` + `CAP-022-recording.mp4`,
staged here per `PROJECT_RULES.md` §2. Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

## 1. Capture Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-022` |
| Purpose | Main run-through Group H — attribute the wire commands for Mono audio, Volume EQ, and Volume balance |
| Date | 2026-08-21 |
| Firmware | not queried this session — ⚪ ASSUMPTION `release_5.203` |
| Test device | Pixel 7a, Android 17 (⚪ ASSUMPTION), official Pixel Buds Companion App (version not visible on screen) |
| Log file | [`CAP-022-btsnoop_hci.log`](./CAP-022-btsnoop_hci.log) — 263.2s, 2026-08-21 08:15:05.529–08:19:28.725 (+0200) |
| Notes file | [`CAP-022-EVENT-NOTES.md`](./CAP-022-EVENT-NOTES.md) |
| Video file | [`CAP-022-recording.mp4`](./CAP-022-recording.mp4) — 123.1s, 08:15:24–08:17:27 local time |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` — same physical device as `CAP-016`/`CAP-019`–`CAP-021` |

## 2. Methodology & Filtering

Every DLCI 0x02 payload was decoded and searched for the `field5{field4{...}}` settings-write
shape (`CAP-020-FINDINGS.md` §5), the same method used for `CAP-021`:

```
$ tshark -r CAP-022-btsnoop_hci.log -Y "btrfcomm.dlci==0x02 and btrfcomm.len>0" \
    -T fields -e frame.number -e frame.time -e data.data
```

45 matches total; a 34-frame sequentially-incrementing burst (`0x2001`→`0x2026`) at session start
(08:15:28.79–08:15:30.62) matches the same connection-time state-sync pattern documented in
`CAP-021-FINDINGS.md` (not attributed to any Test-ID). The remaining 11 matches are all
attributable to this Group's 3 Test-IDs.

## 3. Analysis: `AUDIO-001` (Mono audio)

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
    "ON  (frame 1621)": "7e003b0310131dea71de7d5e251d9a8c9e2a052203980101acbe2bd07e",
    "OFF (frame 1823)": "7e003b0310131dea71de7d5e251d9a8c9e2a0522039801003a8e2ca77e",
}
for name, hx in frames.items():
    raw = bytes.fromhex(hx)
    un = unescape_hdlc(raw[1:-1])
    body, trailer = un[:-4], un[-4:]
    assert struct.pack('<I', binascii.crc32(body) & 0xffffffff) == trailer   # CRC-32 verified
    addr, i = leb128(body, 0); ctrl = body[i]; i += 1
    print(name, "->", body[i:].hex())
```

Output: `ON -> ...2a052203980101` / `OFF -> ...2a0522039801 00`, decoding (offset 13) to
`field5(len5){ field4(len3){ field19=1|0 } }`.

Video: toggle tapped ON at t≈33s (08:15:57), tapped OFF at t≈41s (08:16:05) — both within a few
seconds of frames 1621 (`08:15:53.965`) and 1823 (`08:16:04.605`) respectively.

**Status:** 🟢 **FACT** — `field 19` = Mono audio, promoted 2026-09-03 (`DECISIONS.md` ADR-019
Update, maintainer-approved), based on this wire evidence (2 samples, ON and OFF, both within the
expected timing window) plus, independently, the app's own code (write site `fyo.java:278-298`,
read side logging `"received mono setting value"`, `fxb.java` case 19 — see
`REVERSE_ENGINEERING.md`'s `qhr` entry).

## 4. Analysis: `AUDIO-002` (Volume EQ)

Located at the bottom of **Device details → Sound → Equalizer**, not on the Sound page itself.

```
frame 1871 (08:16:15.825): field5(len4){ field4(len2){ field15=0 } }   # ON->OFF
frame 1895 (08:16:24.090): field5(len4){ field4(len2){ field15=1 } }   # OFF->ON
```

Both CRC-32 verified. Video: toggle already ON at t=49s (08:16:13), OFF by t=53s (08:16:17,
matching frame 1871), ON again by the time the user leaves the Equalizer page (matching frame 1895
at 08:16:24).

**Status:** 🟡 **HYPOTHESIS** — `field 15` = Volume EQ, 2 samples (both directions), clean timing
match.

## 5. Analysis: `AUDIO-003` (Volume balance)

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

def zigzag_decode(n):
    return (n >> 1) ^ -(n & 1)

frames = {
    1922: "7e003b0310131dea71de7d5e251d9a8c9e2a0622048801c701bcfac4347e",
    1944: "7e003b0310131dea71de7d5e251d9a8c9e2a05220388017bfe85dd7c7e",
    2019: "7e003b0310131dea71de7d5e251d9a8c9e2a052203880131702dd4ea7e",
    2039: "7e003b0310131dea71de7d5e251d9a8c9e2a05220388011e291005417e",
    2056: "7e003b0310131dea71de7d5e251d9a8c9e2a06220488019601a99664977e",
    2073: "7e003b0310131dea71de7d5e251d9a8c9e2a0622048801c80173e65cb37e",
    2099: "7e003b0310131dea71de7d5e251d9a8c9e2a05220388010a54c4df5b7e",
}
for fn, hx in frames.items():
    raw = bytes.fromhex(hx)
    un = unescape_hdlc(raw[1:-1])
    body, trailer = un[:-4], un[-4:]
    assert struct.pack('<I', binascii.crc32(body) & 0xffffffff) == trailer   # CRC-32 verified
    addr, i = leb128(body, 0); ctrl = body[i]; i += 1
    payload = body[i:]
    # field17's wire tag is 0x88 0x01 (field 17, wiretype 0=varint); the raw varint follows it
    tag17, j = leb128(payload, payload.index(b'\x88\x01'))
    raw_varint, j = leb128(payload, j)
    print(fn, "raw_varint=", raw_varint, "zigzag=", zigzag_decode(raw_varint))
```

Extracted via `tshark -r CAP-022-btsnoop_hci.log -Y "frame.number in {1922,1944,2019,2039,2056,2073,2099}" -T fields -e frame.number -e data.data`, all 7 CRC-32 verified against the raw hex above. Script
output:

```
frame 1922 (08:16:34.740): raw_varint=199 zigzag=-100
frame 1944 (08:16:42.770): raw_varint=123 zigzag=-62
frame 2019 (08:16:51.533): raw_varint=49  zigzag=-25
frame 2039 (08:16:59.396): raw_varint=30  zigzag=15
frame 2056 (08:17:06.224): raw_varint=150 zigzag=75
frame 2073 (08:17:12.863): raw_varint=200 zigzag=100
frame 2099 (08:17:20.458): raw_varint=10  zigzag=5
```

**Correction (2026-09-03, `DECISIONS.md` ADR-019 Update, maintainer-approved):** `qhr`'s own schema
types field 17 as `SINT32` (`REVERSE_ENGINEERING.md` line 856), so these raw varints must be
zigzag-decoded (`(n>>1) ^ -(n&1)`), not read as plain unsigned values as this section originally
did — the `zigzag=` column in the script output above is the correct reading.

Video confirms this whole window (08:16:34–08:17:20) is a single continuous drag gesture on the
"Balance" slider, with no other action happening — centered at t=70s (08:16:34), toward the right
edge by t=78s (08:16:42), back toward center-right by t=86s (08:16:50).

**Status:** 🟢 **FACT** for the field-number identity and semantic name ("Volume balance") —
promoted 2026-09-03 (`DECISIONS.md` ADR-019 Update), based on this wire evidence plus,
independently, the app's own code (write site `fxf.java:82-133`, read side logging `"received last
saved volume balance setting value"`, `fxb.java` case 17 — see `REVERSE_ENGINEERING.md`'s `qhr`
entry). **Not confirmed:** the value's full scale/range beyond these 7 samples, or which direction
(L/R) increasing/decreasing values represent — 1fps video sampling only captured 3 checkpoints
against 7 wire values, insufficient to map specific values to specific slider positions; the
zigzag correction narrows but does not resolve this. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s note that
this setting is stored **locally on the earbuds** (persistent, works across devices) was **not**
tested this session — no disconnect/reconnect cycle was captured to confirm persistence.

## 6. Cross-command structural comparison

Extends the running table (`CAP-021-FINDINGS.md` §5):

| Setting | Inner field # | Value(s) observed | Capture |
|---|---|---|---|
| Touch controls ON | 4 | 1 | `CAP-020` |
| Head gestures ON | 29 | 2 | `CAP-020` |
| Conversation detection ON | 22 | 1 | `CAP-019` |
| Multipoint ON | 11 | 1 | `CAP-019` |
| Mono audio | 19 | 0 / 1 | `CAP-022` |
| Volume EQ | 15 | 0 / 1 | `CAP-022` |
| Volume balance | 17 | -100 to 100 (zigzag-decoded `SINT32`, continuous) | `CAP-022` |

All seven settings share the same two-level `field5{field4{...}}` outer wrapper (or the
per-earbud-extended `field5{field4{field7{...}}}` variant for `HOLD-001`–`HOLD-004`,
`CAP-021-FINDINGS.md` §3), each with its own distinct inner field number — now 7 confirmed
distinct field numbers across 7 different settings. The "general-purpose settings envelope"
finding first raised in `CAP-020-FINDINGS.md` §5 was **promoted to 🟢 FACT 2026-08-23**
(`DECISIONS.md` ADR-013) for the outer wrapper shape itself — this capture's 7 field numbers are
part of the cross-capture evidence base for that promotion. Field 19's ("Mono audio") and field
17's ("Volume balance") own meanings were independently **promoted to 🟢 FACT 2026-09-03**
(`DECISIONS.md` ADR-019 Update, per §3/§5 above); field 15's ("Volume EQ") own meaning stays 🟡
HYPOTHESIS, unaffected by this update.

## 7. Conclusions & Next Steps

- All 3 Test-IDs (`AUDIO-001`–`AUDIO-003`) isolate cleanly to DLCI 0x02 writes, each cross-checked
  against video timing, each with at least 2 samples (except `AUDIO-003`, which has 7 samples from
  one continuous gesture).
- **Recommended next step:** a capture that pauses at each Balance slider extreme (rather than a
  continuous drag) would let the value range/direction be read directly; a disconnect/reconnect
  after setting Balance would test the "persists on the earbud" claim.

## 8. Open Questions

- 🔴 `field 17`'s (Volume balance) numeric range and L/R direction — not resolved by a continuous
  drag alone. → copied to `PROTOCOL.md` §6.
- 🔴 Does Volume balance actually persist locally on the earbuds across a disconnect/reconnect, as
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §1 claims? Not tested this session. → copied to `PROTOCOL.md` §6.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-022-2026-08-21_08-15-24_08-17-27-Group_H/CAP-022-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-022-2026-08-21_08-15-24_08-17-27-Group_H/CAP-022-FINDINGS
