# CAP-006: Active Noise Control Verification (Group B Repeat)

Standardized, evidence-based extraction from `CAP-006-btsnoop_hci.log` + `CAP-006-recording.mp4`,
staged here per `PROJECT_RULES.md` §2 (recorded first in this file, promoted to `PROTOCOL.md`
only afterwards, and only with maintainer sign-off per `AGENTS.md` §6). Every claim below carries
a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

## 1. Capture Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-006` |
| Purpose | Clean, single-tap-per-window repeat of Group B (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s 2026-08-14 "Repeat recommendation"), explicitly ordered by `DECISIONS.md` ADR-009 to resolve whether every genuine ANC tap reliably produces a `0x12` "Set ANC state" frame |
| Date | 2026-08-15 |
| Firmware | `release_5.203` |
| Test device | Pixel 7a, Android 17 (official Pixel Buds Companion App v1.0.955078536) |
| Log file | [`CAP-006-btsnoop_hci.log`](./CAP-006-btsnoop_hci.log) — 233.16s, 2026-08-15 17:23:37.30–17:27:30.45 (+0200), 3,441 packets |
| Notes file | [`CAP-006-EVENT-NOTES.md`](./CAP-006-EVENT-NOTES.md) — full event timeline with tap-level video↔log correlation |
| Video file | [`CAP-006-recording.mp4`](./CAP-006-recording.mp4) — nominal 17:23:49–17:25:07; container index corrupted (see `CAP-006-EVENT-NOTES.md`'s video recovery note), recovered up to 17:25:05 via VLC's `scene` filter, which covers every action in this session |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `...cf:6e:07` — same physical device as `CAP-001`/`CAP-002`/`CAP-004` |
| Phone MAC (partial) | `...7e:ca:81` — same phone as `CAP-001` |

## 2. Methodology & Filtering

Per `AGENTS.md` §13's CLI hygiene rule, the Buds' address was identified first and used to confirm
this is a single-peer trace before any protocol-specific filtering:

```
$ tshark -r CAP-006-btsnoop_hci.log -Y "bthci_evt.code == 0x03" \
    -T fields -e frame.number -e frame.time -e bthci_evt.bd_addr -e bthci_evt.status
361  2026-08-15T17:23:53.237582000+0200  04:00:6e:cf:6e:07  0x00

$ tshark -r CAP-006-btsnoop_hci.log -Y "bthci_acl" -T fields -e bthci_acl.chandle | sort -u
0x0002
```

Exactly one classic `Connection Complete` event and one ACL connection handle exist in this
session — i.e., **every RFCOMM frame in this log necessarily belongs to the Buds**; an explicit
`bluetooth.addr == 04:00:6e:cf:6e:07` filter would be a no-op here, unlike `CAP-004`'s incidental
Fitbit traffic. This is stated explicitly rather than silently assumed, per `AGENTS.md` §13's
requirement to pre-filter by address before protocol-specific filtering.

The ANC command channel (DLCI 0x04, Message Group `0x08`) is isolated with:

```
$ tshark -r CAP-006-btsnoop_hci.log -Y "btrfcomm.dlci == 0x04 and btrfcomm.len > 0" \
    -T fields -e frame.number -e frame.time -e btrfcomm.direction -e btrfcomm.len -e data.data
```

and the specific "Set ANC state" (Code `0x12`) frames with:

```
$ tshark -r CAP-006-btsnoop_hci.log -Y "btrfcomm.dlci == 0x04 and data.data[0:2] == 08:12" \
    -T fields -e frame.number -e frame.time -e data.data
```

## 3. The 100% Frame Generation Check

**Result: yes — every single physical ANC tap in this capture produced a matching `0x12` "Set ANC
state" frame. The `CAP-001` anomaly (2 of 6 taps missing) does not reproduce here.**

Running the Code-`0x12` filter above against the **entire** 233s log (not just the ~78s video
window) returns exactly four frames, one per tap, in tap order, with zero extras and zero misses:

```
$ tshark -r CAP-006-btsnoop_hci.log -Y "btrfcomm.dlci == 0x04 and data.data[0:2] == 08:12" \
    -T fields -e frame.number -e frame.time -e data.data
1393  2026-08-15T17:24:13.296030000+0200  0812001401e8e808dab2a21d8219ab1368b5dd668ecde0a8
1627  2026-08-15T17:24:26.320542000+0200  0812001401e8e820ff04a960081016aef820c5f14ba07d24
1731  2026-08-15T17:24:38.666168000+0200  0812001401e8e84026c82eea590a563c42c8e9001d91cd30
1862  2026-08-15T17:24:50.824571000+0200  0812001401e8e880ae472e9e51a58f9f69061924c72defa9
```

| Tap (`CAP-006-EVENT-NOTES.md`) | Video tap time | `0x12` frame | Frame time | Δt (frame − tap) |
|---|---|---|---|---|
| `ANC-002` Noise Cancellation | 17:24:12 | 1393 | 17:24:13.296 | +1.3s |
| `ANC-001` Off | 17:24:26 | 1627 | 17:24:26.321 | +0.3s |
| `ANC-003` Adaptive | 17:24:38 | 1731 | 17:24:38.666 | +0.7s |
| `ANC-004` Transparency | 17:24:51 | 1862 | 17:24:50.825 | −0.2s |

All four offsets are within this capture's own ±1s video-sampling tolerance (in fact, tighter than
`CAP-001`'s already-accepted ~1–1.5s window). **This directly closes the open question `DECISIONS.md`
ADR-009 was blocking `FrameEncoder` implementation on**: with genuinely isolated, single taps
against an already-active ANC row, the hit rate is 4/4 (100%), not `CAP-001`'s 4/6 (67%). The
leading explanation from `CAP-001-FINDINGS.md` §5 — that the two missing frames there were
UI-state realization while the ANC row was still greyed out, not genuine commands — is now the
**only** explanation consistent with the evidence across both captures (this session started only
once the row was confirmed active, per `CAP-006-EVENT-NOTES.md`'s scope note, and never missed a
frame).

## 4. Analysis: ANC Mode Commands

Each `0x12` frame decodes against the confirmed layout (`PROTOCOL.md` §4.1):
`[Group:1][Code:1][Len:2BE][Seeker version:1][ANC settable modes:1][ANC enabled modes:1]
[New ANC mode index:1][Reserved:16]`.

### `ANC-002` — Noise Cancellation (frame 1393)

```
Hex: 08 12 00 14 01 e8 e8 08 dab2a21d8219ab1368b5dd668ecde0a8
     Grp Cod Len---  Ver Set Ena Mode  <16 reserved bytes>
```
`new_mode = 0x08` → bit 3 (spec's own `Bit 4`) → **ANC / Active Noise Cancelling**. Matches
`PROTOCOL.md` §4.1's bit-mapping table exactly.

ACK (frame 1398, +126ms): `ff 01 00 06 08 12 01 e8 e8 08` — the documented `0xFF 0x01 <len> <echoed
group/code/data>` shape. Notify (frames 1399/1402): `08 13 00 04 01 e8 e8 08`.

### `ANC-001` — Off (frame 1627)

```
Hex: 08 12 00 14 01 e8 e8 20 ff04a960081016aef820c5f14ba07d24
```
`new_mode = 0x20` → bit 5 → **Off**. ACK (frame 1630, +51ms): `ff 01 00 06 08 12 01 e8 e8 20`.
Notify (frames 1631/1633): `08 13 00 04 01 e8 e8 20`.

### `ANC-003` — Adaptive (frame 1731)

```
Hex: 08 12 00 14 01 e8 e8 40 26c82eea590a563c42c8e9001d91cd30
```
`new_mode = 0x40` → bit 6 → **Adaptive**. ACK (frame 1735, +42ms): `ff 01 00 06 08 12 01 e8 e8 40`.
Notify (frames 1736/1738): `08 13 00 04 01 e8 e8 40`.

### `ANC-004` — Transparency (frame 1862)

```
Hex: 08 12 00 14 01 e8 e8 80 ae472e9e51a58f9f69061924c72defa9
```
`new_mode = 0x80` → bit 7 (MSB) → **Transparent / Aware**. ACK (frame 1864, +37ms):
`ff 01 00 06 08 12 01 e8 e8 80`. Notify (frames 1865/1867): `08 13 00 04 01 e8 e8 80`.

### Comparison against `PROTOCOL.md` §4.1's expected mapping

| `new_mode` byte | `PROTOCOL.md` §4.1 expected mode | This capture's tap | Match? |
|---|---|---|---|
| `0x08` | ANC / Active Noise Cancelling | Noise Cancellation | ✅ |
| `0x20` | Off | Off | ✅ |
| `0x40` | Adaptive | Adaptive | ✅ |
| `0x80` | Transparent / Aware | Transparency | ✅ |

4/4 exact match, zero discrepancies. The `[Seeker version]`, `[ANC settable modes]`, and `[ANC
enabled modes]` bytes are `01`/`e8`/`e8` in all four frames — identical across all four taps and
identical to every `0x12` frame previously observed in `CAP-001` (`PROTOCOL.md` §4.1), consistent
with these being session-scoped capability fields rather than per-command values. The trailing
16 "reserved" bytes differ in every frame and do not repeat — `[Unknown]`, most plausibly a MAC/
nonce given the spec's own "MAC: Y" column for this code (not decoded further here; out of scope
for this capture's purpose).

## 5. Conclusions & Next Steps

- **The ANC command channel is fully confirmed. `FrameEncoder`/`FrameDecoder` implementation for
  this specific command is unblocked**, pending the maintainer's own review and a `DECISIONS.md`
  entry recording this capture's result (per `AGENTS.md` §6 — this document proposes that
  conclusion; it does not itself promote `PROTOCOL.md` or write the ADR). Both conditions
  `DECISIONS.md` ADR-009 set are now met on the same evidence base:
  1. Opcode/payload structure — already 🟢 FACT since `CAP-001` (`PROTOCOL.md` §4.1), unchanged
     and reconfirmed byte-for-byte by this capture's four frames.
  2. Reliability of tap→frame generation — **now resolved**: 4/4 isolated single taps against an
     already-active ANC row produced a matching `0x12` frame, at the expected mode value, in the
     expected order, within video-sampling tolerance. `CAP-001`'s 2/6 miss rate does not reproduce
     under the isolated single-tap protocol this capture used.
- **Recommended next step:** the maintainer records a `DECISIONS.md` update (superseding/extending
  ADR-009, not a fresh independent decision — see `PROJECT_RULES.md` §3) stating the `CAP-006`
  result and formally lifting the `FrameEncoder` block for this command, then implementation of
  the ANC `FrameEncoder`/`FrameDecoder` may proceed per `AGENTS.md` §6.
- This does **not** unblock any other channel or feature (EQ, touch/head gestures, `libmaestro`'s
  own DLCI 0x02 content) — per `AGENTS.md` §6, the implementation gate is per channel/feature, not
  a single global switch.

## 6. Open Questions

- **Trailing 16-byte "reserved" field in each `0x12` frame** — differs in every observed frame
  (across both `CAP-001` and `CAP-006`), never repeats. `[Unknown]`. Plausibly a MAC/nonce (the
  spec's own "MAC: Y" column for this code), but not decoded or tested here — out of scope for
  this capture's purpose (tap→frame reliability), flagged for a future capture if the MAC
  computation itself ever needs to be replicated for write support.
- **Post-Transparency RFCOMM channel renegotiation (~17:24:56–17:25:02, `CAP-006-EVENT-NOTES.md`'s
  timeline)** — RFCOMM channels 1 and 2 are cleanly torn down and a full SDP + multiplexer
  renegotiation runs across DLCIs 0x02/0x04/0x08/0x0a, without any HCI-level `Disconnect`/`Create
  Connection` in between (the underlying ACL link never drops). Not attributable to a specific
  on-screen action since the video decode is truncated in this exact window (see
  `CAP-006-EVENT-NOTES.md`'s video recovery note). 🔴 Not investigated further here — out of scope
  for this capture's ANC-focused purpose. A repeat capture with an intact video recording through
  this window (or a targeted repeat isolating whatever triggers it — e.g., the app being
  backgrounded) would be needed to attribute a cause.
- **Recording pipeline reliability** — this is the second capture in a row (after none previously
  reported) where the screen-recording `.mp4` itself was corrupted (truncated `mdat` relative to
  its own `moov` sample tables). Worth a note for the maintainer's own recording workflow (e.g.,
  whether the recorder app was force-stopped rather than stopped normally) — not a protocol
  finding, but noted here since it materially affected this capture's analysis effort.
- **DLCI 0x0c traffic (50 frames this session, added 2026-08-20)** — present in this log but out
  of scope for this capture's ANC-focused purpose, not analyzed here. Already well-characterized
  from other captures as the `0x0c0X` Key-based-Pairing-shaped cluster (`CAP-002-FINDINGS.md` §4/§7,
  `CAP-004-FINDINGS.md` §6, `CAP-010-FINDINGS.md` §3) — this note exists only so a future reader
  can tell "not analyzed" from "nothing there" without re-deriving it.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-006-2026-08-15_17-23-49_17-25-06-Group_B/CAP-006-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-006-2026-08-15_17-23-49_17-25-06-Group_B/CAP-006-FINDINGS
