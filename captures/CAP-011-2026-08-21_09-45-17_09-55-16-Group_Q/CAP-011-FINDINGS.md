# CAP-011: Passive BLE Scan for Battery Notification (Group Q #18)

Standardized, evidence-based extraction from `CAP-011-btsnoop_hci.log` + `CAP-011-recording.mp4`,
staged here per `PROJECT_RULES.md` §2. Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

## 1. Capture Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-011` |
| Purpose | Group Q item #18 — passive BLE scan for the Fast Pair Battery Notification advertisement (`PROTOCOL.md` §4.3 Option A), independent of RFCOMM |
| Date | 2026-08-21 |
| Firmware | not queried this session — ⚪ ASSUMPTION `release_5.203` |
| Test device | Pixel 7a, Android 17 (⚪ ASSUMPTION), official Pixel Buds Companion App open for most of the session (see §2 deviation note) |
| Log file | [`CAP-011-btsnoop_hci.log`](./CAP-011-btsnoop_hci.log) — 1,055.2s, 2026-08-21 09:45:01.437–10:02:36.612 (+0200) |
| Notes file | [`CAP-011-EVENT-NOTES.md`](./CAP-011-EVENT-NOTES.md) |
| Video file | [`CAP-011-recording.mp4`](./CAP-011-recording.mp4) — 599.9s, 09:45:17–09:55:16 local time |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | Classic peer not confirmed this session; BLE side uses 5 rotating private addresses, see §3 |

## 2. Methodology & procedure deviation

**This is not an RFCOMM/app-action capture** — analyzed with HCI LE Meta Event filters
(`bthci_evt.le_meta_subevent`), not `btrfcomm`, per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.3/§5's
Group Q guidance.

**⚠️ Procedure deviation (flagged per this project's own precedent, e.g. `CAP-004`/`CAP-010`):**
the intended procedure is a passive scan with **no active RFCOMM connection**, case closed and
idle. This session instead shows the case open and empty at video start, Bluetooth turned on and
the official app connected (classic RFCOMM + GATT) within the first ~30s, and the app's "Device
details" screen left open for the rest of the ~10-minute video. The log confirms sustained
`btrfcomm` (426 frames) and `btatt`/GATT (4,155 frames) traffic throughout — this is an
active-connection idle-observation session, not a connection-free passive scan. See
`CAP-011-EVENT-NOTES.md` for the full timeline.

```
$ tshark -r CAP-011-btsnoop_hci.log -q -z io,phs
```
(Protocol hierarchy confirms `btrfcomm`/`btatt` traffic present throughout — reproduced in
`CAP-011-EVENT-NOTES.md`.)

## 3. Analysis: Fast Pair Service (`0xFE2C`) BLE advertisement presence

```
$ tshark -r CAP-011-btsnoop_hci.log -Y "bthci_evt.le_meta_subevent==0x0d and btcommon.eir_ad.entry.uuid_16==0xfe2c" \
    -T fields -e frame.number -e frame.time -e bthci_evt.bd_addr
```

**634 matching frames** across 5 distinct, rotating BLE addresses, all at strong/close-range RSSI
(-25 to -39 dBm):

| Address | Frames | RSSI |
|---|---|---|
| `4e:85:de:53:ae:75` | 458 | -29..-31 dBm |
| `5a:80:89:3d:f0:43` | 91 | -30..-39 dBm |
| `36:79:a6:bf:b5:03` | 50 | -27..-31 dBm |
| `4d:8f:10:28:d5:bd` | 29 | -27..-29 dBm |
| `45:e7:6a:2c:4c:a0` | 6 | -25..-27 dBm |

**Status:** 🟢 **FACT** — Fast Pair Service BLE advertisements are present throughout the
observation window (first three-way-taxonomy outcome: local condition held, traffic observed).
**Not confirmed:** whether all 5 addresses belong to this project's own Buds/Case unit (consistent
close RSSI is supporting, not conclusive, evidence) vs. unrelated nearby Fast-Pair devices.

## 4. Analysis: Battery Notification structural match (`PROTOCOL.md` §4.3 Option A)

Sample service-data payloads (bytes following the 2-byte `0xFE2C` UUID):

```
frame 1769 (09:45:49.588): 10 50 46 06 85 28 ac 21 d6 f2 46 64 ea 5c 21 1a 51 f5 04 de
frame 1772 (09:45:50.356): 10 50 46 06 85 28 ac 21 d6 f2 46 64 ea 5c 21 1a 51 f5 04 de  (identical)
frame 131  (09:45:02.534): 10 52 50 a6 78 8a 03 21 1c 74 29 38 db 08 2f b2 38 12 ee
```

`PROTOCOL.md` §4.3 Option A's documented layout: `[Flags:1=0x00][Account Key Data:var]
[Battery length&type:1, expects 0x33 show / 0x34 hide][L:1][R:1][Case:1]`.

**Checked mechanically:** no sampled payload begins with `0x00`; no byte at any offset equals
`0x33` or `0x34`. There is no candidate position for the documented Length&Type marker.

**Status:** 🔴 **OPEN QUESTION / INCONCLUSIVE** — per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5's explicit
instruction not to force-fit a non-matching frame. Two plausible, un-isolated explanations: (a) the
active-connection procedure deviation (§2) is itself a confound this project's guidance doesn't yet
cover, or (b) these payloads are a different Fast Pair sub-frame (a plain Account Key
Filter/rotating-salt advertisement) rather than the Battery Notification extension specifically.
Not guessed at further.

## 5. Conclusions & Next Steps

- Fast Pair BLE advertisement traffic (`0xFE2C`) is confirmed present (🟢 FACT), but this
  capture's procedure deviation (active connection throughout) and the payload's structural
  non-match against the documented Battery Notification layout mean `BATT-002`/`BATT-003` remain
  **inconclusive**, not resolved — recorded honestly as such rather than force-closed.
- **Recommended next step:** a genuine repeat — Bluetooth scanning only, case closed, **no app
  open, no active connection** — is still needed. If the same non-matching payload shape recurs
  under a clean connection-free scan, that would be strong evidence this is simply a different
  Fast Pair advertisement sub-type than the Battery Notification extension; if a `0x33`/`0x34`
  marker appears once the connection confound is removed, that would instead point at the
  active-connection state suppressing or altering the Battery Notification's normal broadcast.

## 6. Open Questions

- 🔴 Why do the sampled `0xFE2C` service-data payloads not match `PROTOCOL.md` §4.3 Option A's
  documented Battery Notification byte layout? → copied to `PROTOCOL.md` §6.
- 🔴 Does an active classic RFCOMM connection suppress or alter the Buds' Battery Notification BLE
  advertisement? Not previously an open question in this project — newly raised here. → copied to
  `PROTOCOL.md` §6.
- 🔴 Do all 5 rotating BLE addresses observed this session belong to the same physical Buds/Case
  unit? Not confirmed (RSSI proximity is supporting evidence only). → copied to `PROTOCOL.md` §6.
