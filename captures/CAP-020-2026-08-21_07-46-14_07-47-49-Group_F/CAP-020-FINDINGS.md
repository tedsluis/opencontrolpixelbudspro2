# CAP-020: Touch Controls & Head Gestures Toggles (Group F)

Standardized, evidence-based extraction from `CAP-020-btsnoop_hci.log` + `CAP-020-recording.mp4`,
staged here per `PROJECT_RULES.md` §2 (recorded first in this file, promoted to `PROTOCOL.md` only
afterwards). Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

## 1. Capture Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-020` |
| Purpose | Main run-through Group F — attribute the wire commands for the top-level "Use touch controls" and "Use head gestures" on/off toggles |
| Date | 2026-08-21 |
| Firmware | not queried this session — ⚪ ASSUMPTION `release_5.203`, consistent with every prior capture on this Buds unit, not re-confirmed on-the-wire or on-screen here |
| Test device | Pixel 7a, Android 17 (⚪ ASSUMPTION, not re-confirmed on screen this session — same physical device used throughout this project), official Pixel Buds Companion App (version not visible on screen) |
| Log file | [`CAP-020-btsnoop_hci.log`](./CAP-020-btsnoop_hci.log) — 375.9s, 2026-08-21 07:44:34.048–07:50:49.945 (+0200) |
| Notes file | [`CAP-020-EVENT-NOTES.md`](./CAP-020-EVENT-NOTES.md) — full event timeline with tap-level video↔log correlation |
| Video file | [`CAP-020-recording.mp4`](./CAP-020-recording.mp4) — 95.2s, 07:46:14–07:47:49 local time (video's own overlay) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` — same physical device as `CAP-016`/`CAP-017` |

## 2. Methodology & Filtering

Per `AGENTS.md` §13's CLI hygiene rule, confirmed a single classic ACL peer before any
protocol-specific filtering:

```
$ tshark -r CAP-020-btsnoop_hci.log -T fields -e bthci_evt.bd_addr -e bthci_acl.dst.bd_addr \
    -e bthci_acl.src.bd_addr | tr '\t' '\n' | grep -v '^$' | sort -u
04:00:6e:cf:6e:07   # classic peer (Buds) — the only address RFCOMM/btrfcomm frames belong to
41:c8:23:6b:3a:5e / 46:7d:ab:98:74:c4 / 4f:f1:ab:85:cc:ec / 57:e4:a6:2b:31:2c / 5b:f9:1d:48:8c:0f /
77:fc:1c:7b:aa:d1   # unrelated BLE-layer random addresses (background scan noise), not RFCOMM
e8:d5:2b:7e:ca:81   # phone's own controller address
```

Video reviewed frame-by-frame (1fps tiled contact sheets, full pass; sub-second single-frame
extraction around both toggles) against its own burned-in wall-clock overlay; anchor calibrated
directly (`t=0s → 07:46:14`, `t=44s → 07:46:58`, `t=93s → 07:47:47` — a consistent 1:1 linear
mapping, confirmed at three points, no drift). RFCOMM DLCI 0x02 traffic isolated with:

```
$ tshark -r CAP-020-btsnoop_hci.log -Y "btrfcomm.len>0 and frame.time_epoch>1787291195 and \
    frame.time_epoch<1787291230" -T fields -e frame.number -e frame.time -e btrfcomm.dlci \
    -e btrfcomm.direction -e data.data
```

## 3. Analysis: `TOUCH-001` ("Use touch controls" OFF→ON)

Video shows the toggle in its OFF (unchecked) state through video t=29s, a finger tap on the
toggle at t=30s, and the toggle confirmed ON (checked, purple) by t=32s — wall clock **07:46:44**.

One `Sent`-direction DLCI 0x02 frame lands exactly in this window, frame **1741**, `07:46:44.850477`:

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

raw = bytes.fromhex("7e004b0310151dea71de7d5e251d9a8c9e2a0422022001c5a08a3c7e")
un = unescape_hdlc(raw[1:-1])
body, trailer = un[:-4], un[-4:]
assert struct.pack('<I', binascii.crc32(body) & 0xffffffff) == trailer   # CRC-32 verified
addr, i = leb128(body, 0); ctrl = body[i]; i += 1
payload = body[i:]
print(hex(addr), hex(ctrl), payload.hex())
# -> 0x0 0x4b 0310151dea71de7e251d9a8c9e2a0422022001
```

Walking the payload as standard protobuf wire-format tags from offset 13 (offsets 0–12 are a
constant, session-independent prefix — see §5):

```
[off 13] field=5 wt=LD  len=4  span=15..19
  [off 15] field=4 wt=LD  len=2  span=17..19
    [off 17] field=4 wt=varint value=1
```

Immediately followed by the Buds' echo/ACK burst on the same DLCI: frames 1749 (07:46:45.106282,
`Rcvd`, echoes the same `field 5{field 4{...}}` shape back with the request's own prefix appended)
and 1753 (07:46:45.142917, `Rcvd`, the standard connection-serial/firmware echo also seen at every
prior channel-(re)open in this capture, per `PROTOCOL.md` §2.2a). DLCI 0x04 and DLCI 0x0c carried
no data in this window.

**Status:** 🟡 **HYPOTHESIS** — frame 1741 is the `TOUCH-001` command, on tight (<1s) timing
correlation with the video-observed tap and toggle-state change, with a verified CRC-32 and a
mechanically-decoded protobuf tag structure. Not 🟢 FACT: no official Fast Pair extension documents
this Group/field numbering (checked against the extension pages already consulted for §4.1/§4.4 of
`PROTOCOL.md` — none cover touch controls), and this is a single capture/single sample.

## 4. Analysis: `HEAD-001` ("Use head gestures" OFF→ON)

Video shows a finger tap on the "Use head gestures" toggle at t=46s (07:47:00), an "Optimize head
gestures" one-time explainer dialog appearing t=47–49s, and the toggle confirmed ON by t=50s
(07:47:04).

One `Sent`-direction DLCI 0x02 frame lands at the *tap* moment, frame **1935**, `07:47:00.005060`
— i.e. **before** the explainer dialog even renders, confirming the wire write fires on the tap
itself, not on the dialog's later dismissal:

```
raw = "7e004b0310151dea71de7d5e251d9a8c9e2a052203e801020641623b7e"
# -> addr=0x0 ctrl=0x4b payload=0310151dea71de7e251d9a8c9e2a052203e80102
[off 13] field=5 wt=LD  len=5  span=15..20
  [off 15] field=4 wt=LD  len=3  span=17..20
    [off 17] field=29 wt=varint value=2
```

Payload offsets 0–12 (`03 10 15 1d ea 71 de 7e 25 1d 9a 8c 9e`) are **byte-identical** to
`TOUCH-001`'s frame 1741 above, and offsets 3–12 also match `CAP-005-FINDINGS.md` §5a's own
payload prefix from a different session five days earlier — the "request/response correlation ID"
region that section already identified is now confirmed stable **across sessions**, not only
within one, which that section left open.

ACK/Rcvd echo: frames 1939 (07:47:00.441100) and 1942 (07:47:00.444725), same shape as `TOUCH-001`'s
echo above.

**Status:** 🟡 **HYPOTHESIS**, same basis and same caveats as `TOUCH-001` above.

## 5. Cross-command structural comparison (both actions, this capture)

| | `TOUCH-001` (frame 1741) | `HEAD-001` (frame 1935) | `CAP-005`'s EQ (`CAP-005-FINDINGS.md` §5a, frame 1245) |
|---|---|---|---|
| Outer wrapper | `field 5{ field 4{ ... } }` | `field 5{ field 4{ ... } }` | `field 5{ field 4{ ... } }` |
| Inner field # | `4` | `29` | `16`/`18` (2-byte LEB128 tag) |
| Inner wire type | varint | varint | length-delimited (nested submessage) |
| Inner value | `1` | `2` | 25-byte 5×`float32` quintet |

**🟡 HYPOTHESIS (new this session):** the `field 5{ field 4{ ... } }` outer wrapper on DLCI 0x02
is a **general-purpose `libmaestro` settings-write envelope**, not EQ-specific as `PROTOCOL.md` §6
(the 2026-08-17 deskresearch item) left it — that item's own negative result was scoped to the
*inner* `field 16`/`field 18` shape specifically ("zero matches... under any outer field number" for
that exact envelope), which this capture doesn't contradict: `TOUCH-001`/`HEAD-001` use a
**different** inner field (a single varint, not a nested submessage). What's new here is that the
*outer* wrapper recurs unchanged across three structurally different settings (EQ, touch controls,
head gestures) captured on three different days — evidence the outer nesting is shared
infrastructure, while each setting supplies its own inner field number/value. **Not confirmed:**
what `field 4` vs. `field 29` represent (a per-setting/per-message-type ID?), or whether `1`/`2`
are enable-flags specific to each setting or share a common enum — no second on/off cycle was
captured this session (both toggles went OFF→ON only), and no official spec covers this. Flagged as
open (§6 below), not guessed further.

**Checked and ruled out — DLCI 0x08 Group `0x04` Code `0x16`:** this code's value (`08 01`/`08 02`)
changes near the `HEAD-001` frame (frame 1940, 07:47:00.442) but the same code also fires during
the initial connection handshake (frame 922, 07:46:20.194) and again 35s after `HEAD-001` with no
video-visible action nearby (frame 2045, 07:47:35.546), alternating `02→01→02` — matching the
already-documented irregular-interval alternator family (`PROTOCOL.md` §6 Resolved item, Code
`0x12`), not something caused by either toggle. Recorded so a future session doesn't mis-attribute
this coincidence.

## 6. Conclusions & Next Steps

- `TOUCH-001` and `HEAD-001` both isolate cleanly to a single `Sent` frame on DLCI 0x02, within
  <1s of the video-confirmed action, with a verified CRC-32 and a consistent structural envelope —
  🟡 HYPOTHESIS level, matching this project's usual bar for a first single-capture attribution.
- New structural finding: the DLCI 0x02 outer `field 5{ field 4{...} }` wrapper is shared across at
  least 3 different settings (EQ, touch controls, head gestures) — worth promoting to `PROTOCOL.md`
  §2.2a as a named, general-purpose envelope shape, distinct from any one setting's inner content.
- **Recommended next step:** a repeat capture toggling each setting back OFF (this session only
  exercised OFF→ON for both) would (a) confirm whether `field 4`/`field 29`'s value flips to `0`
  for "off" (supporting a simple enable-flag reading) or whether an entirely different field
  appears, and (b) give a second independent sample before this crosses to 🟢 FACT.

## 7. Open Questions

- 🔴 What do inner field numbers `4` (touch controls) and `29` (head gestures) represent inside the
  DLCI 0x02 `field 5{ field 4{...} }` wrapper — a per-setting message-type ID, a field-within-a-
  larger-schema position, or something else? Not derivable from this single-direction (OFF→ON
  only) capture. → copied to `PROTOCOL.md` §6.
- 🔴 Does the same `field 5{ field 4{...} }` wrapper generalize to *every* `libmaestro` setting
  (§4.5's whole remaining list), or only to some? → copied to `PROTOCOL.md` §6.
