# Findings: `CAP-016` (2026-08-18 re-run, Group U — case/bud-removal hardware events)

Standardized, evidence-based extraction from `CAP-016-btsnoop_hci.log` + `CAP-016-recording.mp4`
(the `06-31-31` folder), following the same template as `CAP-001-FINDINGS.md`. Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-016` (this is the **2026-08-18 06:31:31 session** — do not confuse with the
older, separate `CAP-007-2026-08-16_09-14-10_09-17-57-Group_U/` capture, which has its own
independent findings document) · **Date:** 2026-08-18 · **Firmware:** `release_5.203` (confirmed
on-wire, frames 1544/1584/1590) · **Phone:** Pixel 7a, Android 17 (build `CP2A.260705.006`),
system Bluetooth "Device details" page · **Log file:** `CAP-016-btsnoop_hci.log` (785.31s, 3,404
packets, 2026-08-18 06:23:12.4636–06:36:17.7734 local/+0200) · **Video:**
`CAP-016-recording.mp4` (147.39s, 06:31:31–06:33:58 local, on-screen wall-clock overlay) ·
**Devices:** phone `Google_7e:ca:81`, peer/Buds classic `Google_cf:6e:07` (handle `0x0001`) —
same physical devices as `CAP-001`–`CAP-007`(old). See `CAP-016-EVENT-NOTES.md` in this folder for
the full video-to-log correlated timeline this document is built from.

**Scope note:** unlike `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Group U write-up (a narrow DLCI 0x08
liveness-bracket procedure), this session's on-camera content is a general hardware-event
sequence: Bluetooth-on → both-buds-removed-from-case → case-close/reopen (empty) →
both-buds-docked → disconnect → case-close. It exercises `CASE-004`/`CASE-005` and `CASE-006`
cleanly but **does not** exercise `INEAR-002`/`INEAR-003`/`INEAR-004` — no earbud is ever shown
inserted into or removed from an ear on camera (see `CAP-016-EVENT-NOTES.md`'s header note).

---

## 1. Connection lifecycle: Buds-initiated reconnect on bud removal (🟢 FACT)

Unlike `CAP-001`'s three phone-initiated `Create Connection` attempts (`CAP-001-FINDINGS.md` §1),
this session's classic BR/EDR connection is established in a **single, Buds-initiated** page:

```
tshark -r CAP-016-btsnoop_hci.log -Y "frame.time_relative>=529.5 && frame.time_relative<=531.5" \
  -T fields -e frame.number -e frame.time -e _ws.col.Info
```

| Event | Frame | Time |
|---|---|---|
| `Rcvd Connect Request` (Buds page the phone) | 1213 | 06:32:02.531 |
| `Sent Accept Connection Request` | 1214 | 06:32:02.545 |
| `Rcvd Connect Complete` (handle `0x0001`, `04:00:6e:cf:6e:07`, status `0x00`) | 1217 | 06:32:02.749 |

This lands **within 0.5s of the on-camera earbud removal** (hand lifts the first earbud out of the
case ≈06:32:00–02, per `CAP-016-EVENT-NOTES.md`) — the Buds themselves initiate the classic-profile
reconnection once removed from the case, rather than the phone polling/paging them. `Link Key
Request`/`Authentication`/`Set Connection Encryption` were **not** observed as a separate visible
step in this window — a full HCI event dump (frames 1213–1260) shows `Read Remote Version
Information`, `Read Remote Supported Features`, `Write Link Policy Settings`, `Read Clock Offset`,
`Change Connection Packet Type`, `Write Link Supervision Timeout` immediately following `Connect
Complete`, consistent with a reconnection to an already-bonded device (link-layer encryption
already active from a prior session) rather than a fresh pairing — 🟡 HYPOTHESIS, not independently
re-verified against the HCI encryption-state bit in this pass.

**Disconnection is equally clean and immediate, 🟢 FACT:**

```
tshark -r CAP-016-btsnoop_hci.log -Y "bthci_evt.code==0x05" \
  -T fields -e frame.number -e frame.time -e bthci_evt.connection_handle -e bthci_evt.reason
# → 3235  2026-08-18T06:33:45.152102+0200  0x0001  0x13
```

Frame 3235, **06:33:45.152**, handle `0x0001`, reason `0x13` = **"Remote User Terminated
Connection"** — i.e. the *Buds* end the link, not the phone. This is the **only** `Disconnection
Complete` event anywhere in the entire 785s log. It lands 0.9–1.9s after the second (last) earbud
is placed back into the case (≈06:33:44–45 per video) — the classic link drops the instant the case
judges "both buds docked," with no RFCOMM-level teardown or other warning frame preceding it beyond
the routine ~6–7s periodic heartbeat (frame 3230/3231, `03 03 00 03 e4 64 ff`, 06:33:45.072).

**Reproduction:**
```
tshark -r CAP-016-btsnoop_hci.log -Y "bthci_acl.chandle" -T fields -e bthci_acl.chandle | sort -u
# → 0x0001, 0x0002 (only two connection handles in the entire log)
```

## 2. A second, distinct BLE link appears when Bluetooth is turned on (🟡 HYPOTHESIS)

```
tshark -r CAP-016-btsnoop_hci.log -Y "bthci_evt.le_meta_subevent==0x0a" \
  -T fields -e frame.number -e frame.time -e bthci_evt.bd_addr -e bthci_evt.connection_handle
# → 691  2026-08-18T06:31:40.982971+0200  4f:25:00:85:9a:b1  0x0002
```

This `LE Enhanced Connection Complete` (frame 691, **06:31:40.983**) lands within 1s of the video's
own Quick-Settings/notification-shade battery display appearing ("Left 100% Case 100% Right 100%",
≈06:31:40) — **before** the classic connection exists (that only forms at 06:32:02.749, §1). The BD_ADDR
(`4f:25:00:85:9a:b1`) does **not** match the classic peer's BD_ADDR (`04:00:6e:cf:6e:07`) at all —
consistent with a resolvable/random BLE address (common for Fast-Pair-capable peripherals
advertising battery state independently of the classic bond), but this capture does **not**
independently confirm the two addresses belong to the same physical Buds unit beyond the
temporal coincidence. 🔴 **Not resolved this pass:** a GATT/Fast-Pair-advertisement content check
(reading the actual battery-service payload on handle `0x0002`) would be needed to promote this
past HYPOTHESIS.

## 3. RFCOMM channel bounce at 06:33:16 has no identified physical trigger (🔴 OPEN QUESTION, re-confirms `CAP-007`(old)'s "autonomous" finding)

`CAP-007`(old)'s findings (§3.3) found a full DLCI 0x02/0x04/0x08/0x0a channel teardown-and-rebuild
coincident with a bud-removal action, and hypothesized it was *caused* by that removal (an
antenna/role-handover side effect). This session lets that hypothesis be tested against a second
data point: **a structurally identical channel bounce occurs here too, but with no bud-removal (or
any other camera-visible action) anywhere near it:**

```
tshark -r CAP-016-btsnoop_hci.log -Y "btrfcomm && frame.time_relative>=596.0 && frame.time_relative<=610.5 && (btrfcomm.frame_type==0x2f or btrfcomm.frame_type==0x63 or btrfcomm.frame_type==0x43)" \
  -T fields -e frame.number -e frame.time_relative -e btrfcomm.dlci -e btrfcomm.frame_type
```

🟢 FACT (frame numbers independently re-checked): `DISC` (`0x43`) on DLCI `0x02`/`0x04`/`0x08`/`0x0a`
between 06:33:15.941–06:33:17.985 (frames 2770, 2773, 2784, 2785), followed by `SABM`(`0x2f`)/`UA`
(`0x63`) reopening each in turn, complete by **06:33:19.615** (frame 2906, DLCI `0x02` last). The
underlying ACL link (handle `0x0001`) is undisturbed throughout — no `Connect`/`Disconnect Complete`
anywhere near this window (confirmed: the log's only such events are 06:32:02.749 and 06:33:45.152,
both accounted for elsewhere).

**At this exact moment in the video:** both buds have already been out of the case for ~56s
(removed at 06:32:19–20), the case sits open and empty on the stand, and no hand is in frame near
the case or phone. **This rules out "bud physically leaving/entering the case" as the sole trigger
for this class of channel bounce** — `CAP-007`(old)'s "plausibly an antenna/role-handover effect of
removing an earbud" hypothesis is not falsified for *that* capture's own bounce (which *was*
coincident with a removal), but this capture shows the same bounce shape can also fire with no
case/bud interaction whatsoever, strengthening the alternative "autonomous, buds-internal-condition"
reading `CAP-007`(old) §5 already favored for the related DLCI 0x08 Code `0x12` liveness value.

**What the bounce carries, 🟢 FACT:** the ANC-state Notify (DLCI 0x04, Group `0x08` Code `0x13`) is
re-announced mid-bounce, but with an inconsistent value across the three samples surrounding it:

```
tshark -r CAP-016-btsnoop_hci.log -Y "btrfcomm.dlci==0x04 && btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time -e data.data | grep '^0813\|\t0813'
```

| Frame | Time | Raw hex | Settable-toggles byte | Current-state byte |
|---|---|---|---|---|
| 2768 | 06:33:15.940 | `08 13 00 04 01 e8 e8 80` | `0xe8` | `0x80` (Transparency) |
| 3012 | 06:33:22.359 | `08 13 00 04 01 e8 e8 80` | `0xe8` | `0x80` (Transparency) |
| 3054 | 06:33:23.456 | `08 13 00 04 01 e8 00 20` | `0x00` | `0x20` (Off) |

The value flips from "Transparency, fully settable" back to "Off, settable-toggles=0x00" *after*
two re-announcements of the unchanged Transparency state — i.e. it does not simply revert once and
stay reverted. 🔴 **Not explained by this capture**: no physical action, ANC tap, or bud
insertion/removal is visible in the video at 06:33:23. Video evidence (`CAP-016-EVENT-NOTES.md`)
shows the app's ANC row itself loses its highlighted selection at ≈06:33:16, i.e. at the *start* of
the bounce, and — per the video — never regains a highlighted selection for the remainder of the
capture (buds are re-docked at 06:33:44–45 and the connection drops nine seconds after frame 3054),
so this cannot be checked further against the UI in this capture.

## 4. Settable-toggles byte (`0x00` vs `0xe8`) tracks the app's "no mode selected" UI state, not just current ANC state (🟡 HYPOTHESIS, new — refines `CAP-001-FINDINGS.md` §5's ANC Notify field table)

`CAP-001-FINDINGS.md` §5 documents the `08 13` Notify shape as `[Group][Code][Len=4][Version:1][UI
toggles:1][Settable toggles:1][Current state:1]` per the official Fast Pair Hearable Controls spec,
but every sample in `CAP-001` carried the same `0xe8` settable-toggles byte, so that field's role was
never exercised. This capture is the first to observe it change value:

```
tshark -r CAP-016-btsnoop_hci.log -Y "btrfcomm.dlci==0x04 && btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time -e data.data | grep '0813'
```

All 8 ANC Notify frames this session, in order:

| Frame | Time | Settable-toggles | Current-state | Context |
|---|---|---|---|---|
| 1521 | 06:32:03.567 | `0x00` | `0x20` (Off) | Right after classic connect, both buds still docked at this instant (first removed ~06:32:00–02, connect completes 06:32:02.749, this notify is part of the immediate post-connect RFCOMM-channel-open burst) |
| 2128 | 06:32:18.631 | `0xe8` | `0x80` (Transparency) | ~0s before the *second* bud is lifted from the case (≈06:32:19–20) |
| 2220 | 06:32:23.195 | `0xe8` | `0x80` | Idle, both buds out |
| 2245 | 06:32:25.013 | `0xe8` | `0x80` | Idle |
| 2273 | 06:32:27.265 | `0xe8` | `0x80` | Idle |
| 2768 | 06:33:15.940 | `0xe8` | `0x80` | Channel-bounce start (§3) |
| 3012 | 06:33:22.359 | `0xe8` | `0x80` | Channel-bounce tail |
| 3054 | 06:33:23.456 | `0x00` | `0x20` | Channel-bounce tail, ~1.1s after frame 3012 |

**Correlation, 🟡 HYPOTHESIS:** every `settable=0x00` sample (1521, 3054) corresponds to a video
window where the app's ANC row shows **no highlighted mode** (06:32:04–19 and, per §3, from ≈06:33:16
onward); every `settable=0xe8` sample corresponds to a window where **Transparency is highlighted**
(06:32:20–06:33:16ish). This is consistent with `settable-toggles=0x00` meaning "the Buds have not
yet reported which ANC modes are currently selectable" (e.g. because not enough of the accessory is
"active" — both buds still partly in the case, or a channel that just bounced and hasn't re-synced
capability data yet) and the app rendering that as "no mode selectable" rather than defaulting to
showing the last-known `current-state` value. **Not confirmed against the official spec text in this
pass** — `CAP-001-FINDINGS.md` §5 cites the spec's bit-layout for the *current-state* byte
(`0x80`=Transparent, `0x40`=Adaptive, `0x20`=Off, `0x08`=ANC) but does not document a defined
meaning for a `0x00` *settable-toggles* value specifically; this capture's own 8-sample pattern is
the only evidence offered here.

## 5. Case-lid open/close, while both buds are elsewhere, produces zero wire signal (🟢 FACT — replicates `CAP-007`(old) §3.4's negative result on a cleaner, isolated action)

Two separate lid actions this session, both with **both buds already removed** (case empty) and the
connection already `Active`:

```
tshark -r CAP-016-btsnoop_hci.log -Y "btrfcomm.len>0 && frame.time_relative>=570.0 && frame.time_relative<=594.0" \
  -T fields -e frame.number -e frame.time_relative -e btrfcomm.dlci -e data.data
```

| Action | Video time | Wire result |
|---|---|---|
| Case lid **closed** (empty) | 06:32:44–51 | Only routine periodic traffic in the surrounding window (device-info re-announce `google-pixel-buds-pro-v1`, `AT+BIEV`-shaped HFP heartbeat frames at their ordinary ~6–7s cadence) — **no frame timed to the lid closing specifically** |
| Case lid **reopened** (empty) | 06:33:02–04 | Same — no dedicated frame |

This is a second, independent confirmation of `CAP-007`(old) §3.4's finding ("closing the case lid,
while a bud remains outside the case and the connection stays active, produces no observable RFCOMM
wire signal") — here extended to **both directions** (close *and* reopen) and to the case containing
**zero** buds rather than one. **Combined conclusion, 🟢 FACT across two independent captures:** the
case lid's open/closed state is not, by itself, wire-visible on any RFCOMM channel captured here —
whatever senses the lid position (if anything does, independent of bud presence) does not appear to
report it to the phone over DLCI `0x02`/`0x04`/`0x08`/`0x0a` while no bud is docked.

## 6. Docking a bud produces no distinct "docked" wire event either (🔴 OPEN QUESTION, new)

```
tshark -r CAP-016-btsnoop_hci.log -Y "frame.time_relative>=624.0 && frame.time_relative<=630.0" \
  -T fields -e frame.number -e frame.time_relative -e _ws.col.Info
```

In the ±3s window around the **first** bud being placed back into the case (≈06:33:38–40), the log
shows only unrelated `LE Extended Advertising Report` frames (background BLE scan noise from other
nearby devices, not the Buds) and one `Mode Change` baseband event at **06:33:41.680**. No RFCOMM
data frame, no ANC re-notify, no DLCI 0x08 Code `0x12` push appears in this window. The `Mode
Change` event's timing is suggestively close but — per `CAP-001-FINDINGS.md` §3.5's identical
caveat about the same event type — there is no documented mechanism connecting an HCI-level
active/sniff-mode transition to app-visible case-docking state, so this is 🔴 **not attributable**,
consistent with (not a new finding beyond) the prior capture's own treatment of this event type.

The **second** bud's docking (≈06:33:44–45) is likewise wire-silent except for the routine periodic
heartbeat immediately before the disconnect (§1) — no distinct "bud N docked" signal, only the
eventual `Disconnection Complete` once *both* are back.

## 7. DLCI 0x08 Group `0x04` Code `0x12` liveness value — re-confirms `CAP-007`(old) §3.2, no new resolution (🟢 FACT for behavior, 🔴 still open for meaning)

Opportunistic re-check, same envelope/extraction as `CAP-007`(old) §3.2:

```
tshark -r CAP-016-btsnoop_hci.log -Y "btrfcomm.dlci==0x08 && btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time -e data.data | grep '^0412000[48]'
```

8 pushes this session (frames 1536, 2148, 2396, 2479, 2673, 2739, 2862, 2871), value cycling only
`0x02`/`0x03` (no `0x04` observed this time, vs. `CAP-007`(old)'s `0x02`/`0x03`/`0x04`) — all
Rcvd-only (Buds→phone), same as before. Frame 1536 (06:32:03.595, part of the post-connect
channel-open burst) and frames 2862/2871 (06:33:18.536/18.683, inside the §3 channel bounce) both
fire in step with channel-(re)opens, same as `CAP-007`(old)'s characterization; frames 2396, 2479,
2673, 2739 fire during otherwise-idle stretches with no channel churn, same "neither purely reactive
nor purely free-running" behavior already established. **No new evidence on what the value itself
encodes** — still 🔴 open, per `CAP-007`(old) §6.

## 8. Architectural impact

- **Does not open a new channel or unblock `FrameEncoder`/`FrameDecoder` for any feature.** Per
  `AGENTS.md` §6, all content here is on the three already-documented RFCOMM sub-protocols
  (`PROTOCOL.md` §2.3).
- **§4's settable-toggles observation is a refinement candidate for `PROTOCOL.md`'s ANC Notify
  field table** (`PROTOCOL.md` §4.1, sourced from `CAP-001-FINDINGS.md` §5) — flagged as a proposal
  per `AGENTS.md` §6, **not** committed as FACT: promoting "settable-toggles=0x00 means no mode is
  currently selectable" from this capture's 8-sample pattern to `PROTOCOL.md` needs either the
  official spec text for that specific byte or a dedicated single-variable repeat (bud fully
  docked vs. fully out, nothing else changing) before it clears `PROJECT_RULES.md` §1's promotion
  bar. Awaiting maintainer sign-off before any `PROTOCOL.md` edit.
- **§1's Buds-initiated-reconnect finding and §5's case-lid-silence finding are both stable,
  cross-capture-replicated FACTs** (the latter now confirmed in two independent sessions) and are
  safe reference points for future capture design — a case-lid sensor's state, if it exists at all,
  is not expected to produce RFCOMM traffic on its own in future captures either.

## 9. Open questions

- 🔴 What actually triggers the class of RFCOMM multiplexer channel bounce seen in both this
  capture (06:33:16, no camera-visible cause) and `CAP-007`(old) (09:15:38, coincident with a bud
  removal)? Confirmed **not** solely tied to bud removal (§3); still no positive mechanism
  identified.
- 🔴 Why does the ANC Notify's settable-toggles byte revert from `0xe8` to `0x00` a second time at
  06:33:23.456 (frame 3054), after already having re-announced `0xe8`/Transparency twice during the
  same bounce (frames 2768, 3012)? Not resolved by any video-visible action in this capture.
- 🔴 Does `4f:25:00:85:9a:b1` (the BLE link opened at 06:31:40.983, §2) actually belong to the same
  physical Buds unit as classic peer `04:00:6e:cf:6e:07`? Time-coincident but not content-verified
  in this pass — a GATT-level read of that handle's advertised service data would settle it.
- 🔴 What is the black, non-Pixel-Buds earbud/case visible in frame from ≈06:33:16–45
  (`CAP-016-EVENT-NOTES.md` §timeline)? Confirmed to never establish any Bluetooth session in this
  log — purely a camera-frame question, not a protocol one, but flagged so a future viewer of the
  raw video is not misled into thinking it is a second Buds unit under test.

## 10. Bluetooth HID traffic: `AndroidHeadTracker` accessory Feature report (🟡 HYPOTHESIS)

`ARCHITECTURE.md` §1/§15 has an open item since 2026-08-14: HID-Control/HID-Interrupt L2CAP
channels are opened every session (first observed `CAP-002-FINDINGS.md` §6), but no HID report
content had ever been captured or decoded. This session's HID-Control traffic (PSM `0x0011`,
classic ACL handle `0x0001`) does carry decodable content:

```
tshark -r CAP-016-btsnoop_hci.log -Y "btl2cap.psm==0x0011" -T fields -e frame.number -e frame.time_relative -e frame.p2p_dir
```
→ 5 frames total this session: 1943 (`Connection Request`, opens CID `0x004d`), 1980 (`Rcvd
GET_REPORT`, Report Type Feature, Report Id `0x01`), 1983 (`Sent`, `DATA`/Feature response to
`0x01`), 1984 (`Rcvd GET_REPORT`, Report Type Feature, Report Id `0x02`), 1991 (`Sent`, `DATA`/
Feature response to `0x02`).

**🟢 FACT — the byte-decode of frame 1991 itself** (direct, reproducible hex read, not an
inference):

```
tshark -r CAP-016-btsnoop_hci.log -Y "frame.number==1991" -x
```
```
0000  02 01 20 2d 00 29 00 4d 00 a3 02 23 41 6e 64 72   .. -.).M...#Andr
0010  6f 69 64 48 65 61 64 54 72 61 63 6b 65 72 23 31   oidHeadTracker#1
0020  2e 30 00 00 00 00 00 00 00 00 42 54 04 00 6e cf   .0........BT..n.
0030  6e 07                                             n.
```
After the HCI ACL (`02 01 20 2d 00`) and L2CAP (`29 00 4d 00`) headers, the HID payload is `a3 02`
(Transaction Type `DATA`, Report Type `Feature`, Report Id `0x02`) followed by 45 bytes that decode
byte-for-byte as: ASCII `#AndroidHeadTracker#1.0` (24 bytes) + **exactly 8** `0x00` zero-padding
bytes + ASCII `BT` (2 bytes) + `04 00 6e cf 6e 07` — the Buds' own classic BD_ADDR, in the same byte
order the `bthci_acl` dissector reports it elsewhere in this log (`[Source BD_ADDR: Google_cf:6e:07
(04:00:6e:cf:6e:07)]`). Wireshark's own HID dissector misparses the leading bytes of this frame as
generic mouse-report fields (`Button 6/Right/Left`, `X/Y Displacement`) — an artifact of the
dissector defaulting to a HID mouse template for an unrecognized Report Id, not a property of the
actual payload; the raw hex above is what was actually decoded, not the dissector's mislabeled
field names.

**🟡 HYPOTHESIS (single-occurrence, not cross-validated) — the interpretation:** this Feature
report identifies the Buds as exposing an `AndroidHeadTracker` accessory (protocol version `1.0`)
over classic HID, tagged `BT` (vs. a hypothetical `BLE` variant) and self-identifying by BD_ADDR —
consistent with a spatial-audio head-tracking capability, narrowing `ARCHITECTURE.md` §1/§15's open
"is the HID surface architecturally relevant, and if so to what" question toward "yes, and it looks
like head-tracking," specifically. **Not promoted to `ARCHITECTURE.md`** (`AGENTS.md` §6 — this is a
single sample from one capture, no independent replication, and no functional behavior — e.g. what
triggers a `SET_REPORT`, whether any Buds firmware feature is gated on this — has been observed to
confirm the capability is actually *used*, only that it is *advertised*).

**🔴 Report Id `0x01`'s response — flagged as not decoded, not guessed:**

```
tshark -r CAP-016-btsnoop_hci.log -Y "frame.number==1983" -x
```
```
0000  02 01 20 07 00 03 00 4d 00 a3 00 00               .. ....M....
```
Only 3 bytes follow the L2CAP header: `a3 00 00` (`DATA`/Feature, then two `0x00` bytes) — far too
short to carry any string content comparable to Report Id `0x02`'s response, and with no Report Id
byte distinguishable from padding. This is reported exactly as observed (a short/near-empty
response) — **no content is inferred or guessed for what Report Id `0x01` represents.**

## 11. Handle `0x0044` notification burst containing an `0xfea9` marker (🔴 OPEN QUESTION)

```
tshark -r CAP-016-btsnoop_hci.log -Y "btatt.handle==0x0044 and btatt.opcode==0x1b" -T fields -e frame.number -e frame.time -e bthci_acl.chandle
```
73 `Handle Value Notification` frames on ATT handle `0x0044`, all on connection handle `0x0002`
(the BLE link opened at 06:31:40.983, §2) — frames 1032–2082, spanning **06:31:43.468–06:32:12.689**
only (verified: no frame on this handle appears outside that ~29s window anywhere else in the
785s log). 23 of the 73 contain the byte sequence `a9 fe` (`0xfea9` little-endian) somewhere in
their payload:

```
tshark -r CAP-016-btsnoop_hci.log -Y "btatt.handle==0x0044 and btatt.opcode==0x1b and btatt.value contains a9:fe" -T fields -e frame.number
```

**Not decoded further here** — the payloads are long, multi-segment, and do not obviously match
this project's already-documented envelope shapes (RFCOMM `libmaestro`/Fast-Pair/private-envelope
framing, `PROTOCOL.md` §2.3), so no structural claim is made about them beyond their existence,
handle, and the recurring `0xfea9` byte pair.

**What this burst correlates with, checked against both candidate events in this session:** the
burst's own timing (06:31:43–06:32:12) sits immediately after the BLE connection completes
(06:31:40.983, §2) and entirely **before** the classic connection even forms (06:32:02.749, §1) —
i.e. it overlaps the BLE-link startup window, not the classic RFCOMM channel-open burst. It does
**not** overlap the RFCOMM multiplexer channel bounce at 06:33:15.94–06:33:19.615 (§3) — that event
starts roughly **63 seconds after** this burst's last frame (2082, 06:32:12.689), with no handle-
`0x0044` traffic anywhere in between. So of the two events this project might guess this burst is
tied to, the timing evidence supports only the BLE-connect correlation — the channel-bounce
correlation is **not supported by this capture's timestamps** and is not asserted. **Still 🔴 open:**
what handle `0x0044` is (no `Read By Group Type`/`Find Information` response resolving its UUID
was captured for it this session — the earlier discovery burst, §2, targets a different handle
range), and what the recurring `0xfea9` marker specifically encodes.
