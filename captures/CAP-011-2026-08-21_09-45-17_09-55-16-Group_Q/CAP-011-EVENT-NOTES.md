# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group Q #18, Passive BLE scan for Battery Notification (`CAP-011`)

**Status:** ✅ Captured and analyzed. **Not an RFCOMM/app-action capture** — analyzed with the
`btle`-equivalent HCI LE Meta Event filters (this capture's controller reports BLE advertisements
as **LE Extended Advertising Reports**, `bthci_evt.le_meta_subevent==0x0d`, not the legacy
`btle`/`0x02` Advertising Report form `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 describes — see §Decode),
not `btrfcomm`. Observation start/end boundaries logged explicitly per §4.3's Group Q convention.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q, item #18):** capture the Fast Pair "Battery
Notification" BLE advertisement (`PROTOCOL.md` §4.3 Option A) independently of any active RFCOMM
connection, to confirm the officially specified 3-byte L/R/Case payload byte-for-byte.

**⚠️ Procedure deviation, flagged explicitly (per this project's own precedent for such
deviations, e.g. `CAP-004`'s Group S deviation, `CAP-010`'s Group W deviation):** the intended
procedure is a passive scan **"without any active RFCOMM connection"** with the case closed and
idle. **This session does not match that setup.** The video shows the case **open and empty**
(both buds sitting beside it, not stored inside) with Bluetooth **off** at t=0 (09:45:17); within
the first ~30s Bluetooth is turned on, the official app is opened, and the Buds connect via classic
RFCOMM — the app's "Device details" screen (showing Left/Right/Case battery % and "Active" state)
remains open for effectively the entire ~10-minute video. The wire log confirms an active classic
connection throughout (`btrfcomm`: 426 frames, `btatt`/GATT: 4,155 frames — this is **not** a
connection-free window). This capture is therefore better characterized as **an idle-observation
session with an active connection** (closer to `BATT-003`'s "Buds update battery status while
worn/connected" framing) than a clean `BATT-002` "case closed, no connection" scan — recorded here
so this isn't silently treated as if the intended isolation held.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-011`                     |
|      Group(s)    |                    Q (item #18)                    |
|       Date       |                     2026-08-21                     |
| Firmware version |    not queried this session (⚪ ASSUMPTION `release_5.203`) |
|   Test device    | Pixel 7a, Android 17 (⚪ ASSUMPTION), official Pixel Buds Companion App (version not visible on screen) |
| Video file       |          `CAP-011-recording.mp4` (599.9s, 09:45:17–09:55:16 local time) |
| Log file         |             `CAP-011-btsnoop_hci.log` (09:45:01.437–10:02:36.612, brackets the video) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | Classic peer not confirmed this session (no `bthci_evt.code==0x03` Connection Complete checked); BLE side uses rotating private addresses — see §Decode |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q #18)

1. **Passive BLE scan while the case is closed and idle** [`BATT-002`/`BATT-003`] — **not achieved
   as specified**, see the procedure-deviation note above.

## Event Timeline

| Time (local) | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| **09:45:17** | **Observation window start.** Video first frame: Bluetooth off, case open and empty, buds sitting beside it | — | `BATT-002`, `BATT-003` | Log itself starts 16s earlier (09:45:01), already showing BLE advertisement traffic (see §Decode) — i.e. before the video's own start, while the case was presumably in the same open/idle state off-camera |
| ~09:45:20–09:45:47 | Bluetooth turned on; official app opened; Buds connect via classic RFCOMM; "Device details" screen opened (Left 93%, Case 89%, Right 88%, "Active") | User (App) | — | Classic connection + GATT discovery traffic begins in the log around this window (not isolated further, since this is not this Group's actual target traffic) |
| 09:45:47–09:52:25 | "Device details" screen left open and idle, no further taps; percentages remain unchanged (Left 93%, Case 89%, Right 88%) | User (idle) | `BATT-002`, `BATT-003` | Fast Pair Service (UUID `0xFE2C`) BLE advertisements continue throughout — see §Decode |
| **09:52:25.8** (±1 video frame, ~0.03s) | **Correction, added 2026-08-23 — re-examined on request.** On-screen battery display changes: **Left 93%→92%, Right 88%→87%** (Case unchanged at 89%). Originally logged as "around 09:45:47," which was actually the moment the screen first opened at its *initial* values, not a change — corrected here after re-extracting video frames at sub-second granularity (`ffmpeg`, frame-by-frame contact sheets) and cross-checking against the burned-in wall-clock overlay (frame at video t=428.8s reads "09:52:26" with 92/89/87 already showing). Last frame showing 93/88: video-relative t=428.779s; first frame showing 92/87: t=428.812s. See `CAP-011-FINDINGS.md` §7 for the full wire correlation | User (Hardware, passive — battery drain) | `BATT-002`, `BATT-003` | See `CAP-011-FINDINGS.md` §7 — a DLCI 0x08 private-envelope message (`Group 0x0e Code 0x01`, frame 6103, 09:52:24.925) decodes to a 3-entry structure whose first two entries (92, index 1) and (87, index 2) match these exact new values, ~0.86s before the UI updates; a second message (`Group 0x04 Code 0x03`, frame 6115) independently confirms the Right value (87) |
| 09:52:25.8–09:55:16 | "Device details" screen remains open, no further taps; percentages stay 92/89/87 for the rest of the video | User (idle) | `BATT-002`, `BATT-003` | — |
| **09:55:16** | **Video ends** (log continues 16 more minutes, unreviewed on camera) | — | — | — |
| *(off-camera, log only)* 09:57:… / 10:00:45 | Not video-observable, but the wire shows the same `Group 0x0e`/`Group 0x04 Code 0x03` pattern recurring twice more after the video ends, with Right declining further (87%→86% by 10:00:45) | Buds (Auto) | `BATT-002`, `BATT-003` | See `CAP-011-FINDINGS.md` §7 |

## Decode — outcome per the mandatory three-way taxonomy (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.3 Group Q)

**Step 1 — filter mechanics.** This log's controller reports BLE advertisements as **LE Extended
Advertising Reports** (`bthci_evt.le_meta_subevent == 0x0d`), not the legacy Advertising Report
subevent (`0x02`) that populates Wireshark's separate `btle` protocol tree in older-style captures
— `tshark -Y "btle"` matches **zero** frames in this log even though 1,439 LE Meta subevent-`0x0d`
frames exist. This is a mechanical filter-form difference, not an absence of BLE traffic — noted
explicitly since `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5's filter table only documents the `btle` form.

```
$ tshark -r CAP-011-btsnoop_hci.log -Y "bthci_evt.le_meta_subevent==0x0d and btcommon.eir_ad.entry.uuid_16==0xfe2c" \
    -T fields -e frame.number -e frame.time -e bthci_evt.bd_addr
```

**634 matching frames**, across **5 distinct (rotating) BLE addresses**, all at strong RSSI
(-25 to -39 dBm, consistent with very close proximity — the case/buds sit directly beside the
phone throughout this session):

| Address | Frame count | RSSI range |
|---|---|---|
| `4e:85:de:53:ae:75` | 458 | -29 to -31 dBm |
| `5a:80:89:3d:f0:43` | 91 | -30 to -39 dBm |
| `36:79:a6:bf:b5:03` | 50 | -27 to -31 dBm |
| `4d:8f:10:28:d5:bd` | 29 | -27 to -29 dBm |
| `45:e7:6a:2c:4c:a0` | 6 | -25 to -27 dBm |

**🟢 FACT: Fast Pair Service (`0xFE2C`) BLE advertisements are present throughout this observation
window** — traffic outcome #1 of the three-way taxonomy ("local behavior confirmed + Bluetooth
traffic observed"), at least for the general Fast Pair service presence.

**Step 2 — check against `PROTOCOL.md` §4.3 Option A's documented Battery Notification byte
layout.** Sample service-data payloads (the bytes after the 2-byte `0xFE2C` UUID):

```
10 50 46 06 85 28 ac 21 d6 f2 46 64 ea 5c 21 1a 51 f5 04 de   (frame 1769, 09:45:49.588)
10 50 46 06 85 28 ac 21 d6 f2 46 64 ea 5c 21 1a 51 f5 04 de   (frame 1772, 09:45:50.356 — identical)
10 52 50 a6 78 8a 03 21 1c 74 29 38 db 08 2f b2 38 12 ee      (frame 131,  09:45:02.534)
```

The spec table (`PROTOCOL.md` §4.3 Option A) expects: `[Flags:1=0x00][Account Key Data:variable]
[Battery length&type:1, expected 0x33 (show) or 0x34 (hide)][Left:1][Right:1][Case:1]`. **Checked
mechanically, not force-fit:** none of the sampled 19–20-byte payloads begins with `0x00`, and no
byte at any offset equals `0x33`/`0x34` — there is no candidate position for the documented
Length&Type marker in any sampled frame.

**🔴 OPEN QUESTION / INCONCLUSIVE (the taxonomy's 3rd outcome), not forced into a match:** this
capture cannot confirm the Battery Notification sub-structure specifically, for two compounding,
un-isolated reasons — (a) the procedure deviation above means an active classic connection was
present throughout, a confound `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s own guidance doesn't cover, and
(b) the observed `0xFE2C` service-data payloads structurally resemble a **plain Account Key
Filter/rotating-salt advertisement** (the routine Fast Pair discoverable-adjacent broadcast every
paired device sends) rather than the Battery Notification extension specifically — genuinely
unresolved which, not guessed at further (`AGENTS.md` §13).

**Not resolved:** whether the 5 rotating addresses all belong to this project's own Buds/Case unit
(consistent close-range RSSI across all 5 supports this, but does not confirm it — no account-key
or GATT cross-reference was attempted this session) or include unrelated nearby Fast-Pair-capable
devices.

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q #18 / §5)

- [x] Filter for BLE advertising reports (not `btrfcomm`) — used the LE Extended Advertising
      Report form (`0x0d`), since legacy `btle`/`0x02` matched nothing in this log.
- [x] Compare the advertisement payload against `PROTOCOL.md` §4.3 Option A's structure — **does
      not match**, recorded as inconclusive rather than force-fit.
- [ ] Confirm visibility duration (≥8s when shown) — not attempted; the structural mismatch above
      makes a "shown"/"hidden" classification premature.
- [ ] Mark `[VERIFIED-LOCAL]` in `PROTOCOL.md` §4.3 Option A — **not done**, per the inconclusive
      result.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise — `BATT-002`/`BATT-003`
      both referenced above, with the procedure deviation explicitly noted for both.
- [x] Write `CAP-011-FINDINGS.md` per `PROJECT_RULES.md` §2.
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index.
- [x] Rename this capture's folder to the actual session date/start-time/end-time.
- [ ] **Recommended follow-up:** a genuine repeat of this Group's procedure — Bluetooth scanning
      only, case closed, **no app open, no active RFCOMM connection** — is still needed to cleanly
      test the Battery Notification hypothesis without this session's confounds.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-011-2026-08-21_09-45-17_09-55-16-Group_Q/CAP-011-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-011-2026-08-21_09-45-17_09-55-16-Group_Q/CAP-011-EVENT-NOTES
