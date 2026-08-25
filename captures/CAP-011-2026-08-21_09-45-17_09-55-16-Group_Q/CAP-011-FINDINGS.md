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

## 7. Added 2026-08-23 — DLCI 0x08 per-earbud battery push decoded (maintainer-requested re-analysis)

**Trigger:** the maintainer observed, from the recording, that both earbuds' on-screen battery
percentage dropped by 1% "around 09:45:47" and asked for (a) the exact timestamp from a fresh
frame-by-frame video pass, and (b) a wire analysis around that moment for previously-undecoded
packets.

### 7a. Precise timestamp — corrects the original estimate

**Method:** `ffmpeg` frame extraction (cropped to the battery-percentage row, `drawtext` overlay of
relative playback time, tiled into contact-sheet grids) at successively finer granularity — 2s,
then 0.5s, then per-frame (~0.033s, 30fps) — narrowing from a 1-minute window down to the exact
frame pair where the display changes, cross-checked against the video's own burned-in wall-clock
overlay at a known point.

```
$ ffmpeg -ss <offset> -t <dur> -i CAP-011-recording.mp4 \
    -vf "crop=440:75:150:685,drawtext=text='%{pts\:hms}':x=5:y=5:fontsize=16:fontcolor=yellow:box=1:boxcolor=black@0.6,select='not(mod(n\,N))',tile=RxC" \
    -frames:v 1 -vsync 0 out.png
```

**Result — 🟢 FACT, `[VERIFIED-LOCAL]` 2026-08-23:** the original "~09:45:47" estimate was the
moment the "Device details" screen *first opened* (already showing 93/89/88%, confirmed by a probe
frame at video t=30.000s reading the burned-in clock "09:45:47"), not a change. The actual
percentage change — **Left 93%→92%, Right 88%→87%, Case unchanged at 89%** — happens at
**video t=428.78–428.81s, i.e. local time 09:52:25.78–09:52:25.81** (last frame showing 93/88 at
relative offset 0.479s within a clip started at `-ss 428.3`; first frame showing 92/87 at relative
0.512s within the same clip — one frame apart at 30fps). A probe frame at video t=428.8s
independently confirms the burned-in clock reads "09:52:26" with 92/89/87 already on screen.

### 7b. Wire correlation — DLCI 0x08 `Group 0x0e Code 0x01` decodes to a per-earbud battery triple

**Method:** extracted every non-empty DLCI 0x08 payload across the *entire* log (not just the
video window) and grouped by the envelope's Group byte:

```
$ tshark -r CAP-011-btsnoop_hci.log -Y "btrfcomm.dlci==0x08 and btrfcomm.len>0" \
    -T fields -e frame.number -e frame.time -e data.data
```

then parsed each `[Group:1][Code:1][Length:2BE][Value]` envelope's `Value` as nested protobuf
(field = tag>>3, wiretype = tag&7), per `PROTOCOL.md` §2.1's envelope shape reused on this private
DLCI. **`Group 0x0e Code 0x01` (35-byte value) occurs exactly 4 times in this ~17.5-minute log**,
always immediately preceded (1–2ms) by `Group 0x0e Code 0x02` (26 bytes) — the already-documented
`"google-pixel-buds-pro-v1"` capability string (`AGENTS.md` §15's hardcoded-string exception;
first seen `CAP-001`). Full 4 occurrences, decoded:

```
frame 1156  09:45:26.289  0e0100230a210a03616c6c121a0a06085d100118010a060858100118020a04085c180318012001
frame 4222  09:49:28.326  0e0100230a210a03616c6c121a0a06085c100118010a060858100118020a04085c180318012001
frame 6103  09:52:24.925  0e0100230a210a03616c6c121a0a06085c100118010a060857100118020a04085c180318012001
frame 11632 10:00:45.105  0e0100230a210a03616c6c121a0a06085c100118010a060856100118020a04085c180318012001
```

Decode tree (identical shape all 4 times): `field1="all"` (ASCII, 3 bytes) + `field2` = a nested
"battery status" message containing **3 repeated entries** (`field1` each, itself a 3-field
sub-message) plus 2 trailing scalar fields (`field3=1, field4=1` at the battery-status level,
constant in all 4 samples):

| Timestamp | Entry idx=1 (`field3`) | Entry idx=2 (`field3`) | Entry idx=3 (`field3`, no `field2`/flag) |
|---|---|---|---|
| 09:45:26.289 | value=**93**, flag=1 | value=**88**, flag=1 | value=92 |
| 09:49:28.326 | value=**92**, flag=1 | value=**88**, flag=1 | value=92 |
| 09:52:24.925 | value=**92**, flag=1 | value=**87**, flag=1 | value=92 |
| 10:00:45.105 | value=**92**, flag=1 | value=**86**, flag=1 | value=92 |

**Entries idx=1 and idx=2 match the on-screen Left/Right percentages exactly at every sampled
point** — including the 09:45:26 sample (93/88, matching the screen's first-opened values,
video-confirmed) and the 09:52:24→09:52:25.8 transition (92/87, this session's own finding, wire
event **~0.86s before** the UI visibly updates — plausible app-side render latency, not
independently measured further). Entry idx=2 (Right) also **keeps declining** across the two
off-camera recurrences (88→87→86), consistent with ongoing natural battery drain, not a one-off
coincidence.

**Independent cross-check — `Group 0x04 Code 0x03`, also on DLCI 0x08, fires at the same 4
moments** (within ~60–100ms of each `Group 0x0e` pair):

```
frame 1140  09:45:26.274  10051858   -> field2=5, field3=88
frame 4231  09:49:28.343  10051858   -> field2=5, field3=88
frame 6115  09:52:24.985  10051857   -> field2=5, field3=87
frame 11640 10:00:45.113  10051856   -> field2=5, field3=86
```

`field2` is constant (`5`) in all four; `field3` matches entry idx=2 (Right) byte-for-byte every
time — an independent, single-value confirmation of the same Right-earbud reading via a different
Code. A one-time `Group 0x04 Code 0x05` message (`0805`, frame 1142, fires once only, during the
initial connection-settle burst) plausibly subscribes/registers "index 5," though this is not
further decoded.

### 7c. Cross-capture confirmation, added 2026-08-23 — index=1/2/3 = Left/Right/Case, in 2 more independent sessions

**Method:** since the DLCI 0x08 `[Group:Length:2BE][Value]` envelope is the same shape used
throughout this DLCI (§2.2a's sibling private envelope, `PROTOCOL.md` §2.3), the same decode
pipeline from §7b was re-run against every other real capture with DLCI 0x08 traffic (18 sessions
checked: `CAP-001`–`CAP-007`, `CAP-010`, `CAP-015`–`CAP-017`, `CAP-019`–`CAP-025`), each time
picking a `Group 0x0e Code 0x01` frame near a moment where that session's own `CAP-NNN-EVENT-NOTES.md`
independently records an on-screen Left/Case/Right triple:

```
CAP-001 frame 1114 (08:51:12.657715+0200) — on-screen ref (CAP-001-EVENT-NOTES.md, 08:51:11
  notification banner): "Left 100% Case 62% Right 100%"
  0e0100230a210a03616c6c121a0a060864100118010a060864100118020a06083e100118032001
  -> entry idx=1: value=100 flag=1   entry idx=2: value=100 flag=1   entry idx=3: value=62 flag=1

CAP-002 frame 49024 (2026-08-09, 17:05:34.6xx, per CAP-002-FINDINGS.md §2a) — on-screen ref
  (CAP-002-EVENT-NOTES.md, 17:05:53 notification banner): "Left 100% Case 57% Right 100%"
  0e0100230a210a03616c6c121a0a060864100118010a060864100118020a060839100118032001
  -> entry idx=1: value=100 flag=1   entry idx=2: value=100 flag=1   entry idx=3: value=57 flag=1
```

**Result: exact 3-for-3 match in both independent sessions** — entry idx=1=Left, idx=2=Right,
**and idx=3=Case**, all three simultaneously, against a video/UI-confirmed on-screen triple, in
captures recorded 2026-08-09 (`CAP-001`, `CAP-002`) and 2026-08-21 (`CAP-011`) — 12 days apart,
different sessions, different physical battery states each time. This clears
`PROJECT_RULES.md` §1's "repeated confirmation across multiple captures/experiments" FACT bar for
the **idx=1/2/3 → Left/Right/Case mapping specifically** — stronger evidence than this session's
own §7b alone (which only had 4 same-session recurrences), though still narrower than the full
message's every field (see below).

**This revises §7b's idx=3 conclusion, not contradicts it:** idx=3 **is** Case — but in `CAP-011`
specifically, it reads a **stale, non-matching** value (92, vs. the on-screen 89% throughout this
session), unlike `CAP-001`/`CAP-002` where it matched live. A plausible (not confirmed) explanation
given this session's own procedure-deviation note (§2): the case sat open and empty, buds beside
it, for the whole video — if the case's own battery reporting requires the case to be
closed/holding a bud to refresh, `CAP-011`'s idx=3 could be carrying a last-known value from
before this session's log even starts, while the on-screen 89% comes from a separately-cached
source. Not verified further — flagged as the concrete next question, not guessed at beyond this.

**🟢 FACT, promoted 2026-08-23** (maintainer sign-off obtained per `AGENTS.md` §6, `DECISIONS.md`
ADR-014): DLCI 0x08's `Group 0x0e Code 0x01` battery-status message's 3 repeated entries carry the
Left (index 1), Right (index 2), and Case (index 3) battery percentages, each with a `flag` field
(`field2`) whose meaning is unconfirmed (observed as `1` whenever a value is fresh; absent when
`CAP-011`'s idx=3 carries its stale reading — consistent with, not proof of, a "valid/fresh" bit).
Pushed at irregular intervals (`CAP-011`: 4:02, 2:56, 8:21 apart — not a fixed cadence like HFP's
~6–7s), independently of and in addition to the already-🟢-FACT HFP `AT+BIEV` mechanism
(`PROTOCOL.md` §4.3 Option C). This is a **refinement of an already-known message shape**, not a
newly-discovered packet type: `CAP-002-FINDINGS.md` §2a (2026-08-12) already documented this exact
structure (`field1="all"` + "nested field2 x3 (varint triples)") but did not attempt a semantic
reading at the time — this session's contribution, extended by this cross-capture check, is
decoding what the three numbers mean.

**Not resolved, explicitly not guessed at (`AGENTS.md` §13):**
- **Why `CAP-011`'s idx=3 (Case) reads stale/non-matching** while `CAP-001`/`CAP-002`'s read live —
  see the plausible-but-unconfirmed reading above (case open/empty vs. closed/holding a bud).
- **What the `flag` field (`field2`) represents.** Consistently `1` when a value is fresh across
  `CAP-001`/`CAP-002`/`CAP-011`'s idx=1/2, and specifically *absent* on `CAP-011`'s stale idx=3 —
  plausibly a "fresh/valid" bit, not confirmed as such.
- **What triggers this burst.** The session has near-continuous BLE connect/disconnect churn
  throughout (dozens of `LE Enhanced Connection Complete`/`Disconnection Complete` pairs per
  minute, `bthci_evt.code`/`le_meta_subevent` — routine background scanning activity, not unique to
  this moment), so a nearby BLE reconnect does **not** explain why this specific burst is rare
  (4 times in 17.5 minutes) — that hypothesis was checked and does not hold up against the data.
  The actual trigger (a coarse timer? a threshold-crossing event? something else?) is unresolved.
- **A weaker, unparsed observation:** the DLCI 0x02 HDLC `Rcvd` frame immediately following this
  burst (frame 6116, address `0xD180`, CRC-verified per the established §2.2a pipeline) contains
  the exact byte substrings `08 5c 10 01` and `08 57 10 01` (i.e. the same value=92/flag=1 and
  value=87/flag=1 pairs) somewhere within its payload, but this payload's overall structure was
  **not** cleanly parsed to a top-level field path in this pass (the address `0xD180` channel's
  established decode, per `CAP-001-FINDINGS.md` §2, is for a *different* content shape — device
  serial + firmware string — and reconciling the two wasn't attempted here). Recorded as a raw
  byte-pattern observation only, not a claim about DLCI 0x02's structure.

**Recorded in `PROTOCOL.md` §4.3** as Option E, promoted to 🟢 FACT 2026-08-23 (`DECISIONS.md`
ADR-014) for the index→Left/Right/Case mapping, per the maintainer sign-off obtained under
`AGENTS.md` §6.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-011-2026-08-21_09-45-17_09-55-16-Group_Q/CAP-011-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-011-2026-08-21_09-45-17_09-55-16-Group_Q/CAP-011-FINDINGS
