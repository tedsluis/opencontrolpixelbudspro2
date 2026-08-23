# CAP-024: In-ear Detection & Case Sounds (Group J)

Standardized, evidence-based extraction from `CAP-024-btsnoop_hci.log` + `CAP-024-recording.mp4`,
staged here per `PROJECT_RULES.md` §2. Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

## 1. Capture Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-024` |
| Purpose | Main run-through Group J — attribute the wire commands for In-ear detection and the two Case sounds settings |
| Date | 2026-08-21 |
| Firmware | not queried this session — ⚪ ASSUMPTION `release_5.203` (confirmed on-screen the same day, `CAP-023-FINDINGS.md` §3) |
| Test device | Pixel 7a, Android 17 (⚪ ASSUMPTION), official Pixel Buds Companion App (version not visible on screen) |
| Log file | [`CAP-024-btsnoop_hci.log`](./CAP-024-btsnoop_hci.log) — 261.6s, 2026-08-21 08:30:57.018–08:35:18.662 (+0200) |
| Notes file | [`CAP-024-EVENT-NOTES.md`](./CAP-024-EVENT-NOTES.md) |
| Video file | [`CAP-024-recording.mp4`](./CAP-024-recording.mp4) — 116.7s, 08:31:27–08:33:24 local time |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` — same physical device as `CAP-016`/`CAP-019`–`CAP-023` |

## 2. Methodology & Filtering

Same method as the rest of this batch (`CAP-020-FINDINGS.md` §2, `CAP-021-FINDINGS.md` §2): every
DLCI 0x02 payload decoded and searched for the `field5{field4{...}}` shape, matches cross-checked
against video timing.

```
$ tshark -r CAP-024-btsnoop_hci.log -Y "btrfcomm.dlci==0x02 and btrfcomm.len>0" \
    -T fields -e frame.number -e frame.time -e data.data
```

## 3. Analysis: `INEAR-001` (In-ear detection)

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
    "OFF (frame 1850)": "7e003b0310131dea71de7d5e251d9a8c9e2a0422021000904a3dfc7e",
    "ON  (frame 1912)": "7e003b0310131dea71de7d5e251d9a8c9e2a0422021001067a3a8b7e",
}
for name, hx in frames.items():
    raw = bytes.fromhex(hx)
    un = unescape_hdlc(raw[1:-1])
    body, trailer = un[:-4], un[-4:]
    assert struct.pack('<I', binascii.crc32(body) & 0xffffffff) == trailer   # CRC-32 verified
    addr, i = leb128(body, 0); ctrl = body[i]; i += 1
    print(name, "->", body[i:].hex())
```

Decodes to `field5(len4){ field4(len2){ field2=0|1 } }`. Video: In-ear detection ON at session
start (persisted from a prior session), tapped OFF (t≈44–52s, matching frame 1850 at
`08:32:10.980`), tapped back ON (t≈58s, matching frame 1912 at `08:32:23.091`).

**Status:** 🟡 **HYPOTHESIS** — `field 2` = In-ear detection, clean 2-sample ON/OFF match.

## 4. Analysis: `CASE-001` ("Bud return", labeled "Earbuds replaced" in the app's own settings list)

```
frame 1988 (08:32:38.084): field5(len5){ field4(len3){ field28=0 } }
frame 2023 (08:32:50.500): field5(len5){ field4(len3){ field28=1 } }
```

Both CRC-32 verified. Video: "Bud return" toggle already OFF when the Case sounds screen opens
(t=72s/08:32:39, ~1s after frame 1988), tapped ON at t≈82s (matching frame 2023 at 08:32:50.500).

**Status:** 🟡 **HYPOTHESIS** — `field 28` = "Bud return"/`CASE-001`. The `OFF` sample (frame 1988)
is not cleanly disambiguated between "a genuine tap re-affirming the already-off state" and "the
screen syncing its display to the already-cached value on open" — flagged, not asserted either
way. The `ON` sample is unambiguous.

## 5. Analysis: `CASE-002` ("Other alerts", labeled "Other notifications")

```
frame 2053 (08:33:02.060): field5(len5){ field4(len3){ field27=0 } }
frame 2084 (08:33:12.485): field5(len5){ field4(len3){ field27=1 } }
```

Both CRC-32 verified. Video: "Other alerts" ON at screen-open, tapped OFF at t≈94–97s (matching
frame 2053), tapped back ON at t≈102–105s (matching frame 2084) — both samples cleanly
video-correlated, no ambiguity.

**Status:** 🟡 **HYPOTHESIS** — `field 27` = "Other alerts"/`CASE-002`, clean 2-sample match.

## 6. Cross-command structural comparison

Extends the running table (`CAP-022-FINDINGS.md` §6):

| Setting | Inner field # | Capture |
|---|---|---|
| In-ear detection | 2 | `CAP-024` |
| Case sounds: "Bud return" (`CASE-001`) | 28 | `CAP-024` |
| Case sounds: "Other alerts" (`CASE-002`) | 27 | `CAP-024` |

No case-specific vs. bud-specific channel/address distinction was found — case sounds use the
same DLCI 0x02 envelope as every bud-targeted setting in this batch, just their own field numbers.
Now 9 distinct settings across `CAP-019`–`CAP-024`, each with its own distinct inner field number
inside the shared `field5{field4{...}}` wrapper — the general-purpose-envelope finding
(`CAP-020-FINDINGS.md` §5) held with no counter-examples across this whole batch and was
**promoted to 🟢 FACT 2026-08-23** (`DECISIONS.md` ADR-013) for the outer wrapper shape; the
individual field numbers documented in this capture (`field2`=In-ear detection, `field27`/`field28`=
Case sounds) remain their own, separately-labeled 🟡 HYPOTHESIS.

## 7. Conclusions & Next Steps

- All 3 Test-IDs isolate cleanly to DLCI 0x02 writes with video-timing correlation; `INEAR-001`
  and `CASE-002` have unambiguous 2-sample (both-direction) evidence, `CASE-001` has one
  unambiguous and one weakly-correlated sample.
- **Recommended next step:** none specific to this capture — the pattern is now well-established
  across 6 captures in this batch.

## 8. Open Questions

- 🔴 Whether frame 1988 (`CASE-001` OFF) reflects a genuine tap or a screen-open state sync — not
  resolved by this capture's video resolution. → copied to `PROTOCOL.md` §6.
