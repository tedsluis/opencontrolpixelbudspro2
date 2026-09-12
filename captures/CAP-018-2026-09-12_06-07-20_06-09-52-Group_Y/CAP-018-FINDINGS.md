# CAP-018: BLE-only isolation of the `0x0044` notification burst (Group Y, `GATT-002`)

Standardized, evidence-based extraction from `CAP-018-btsnoop_hci.log` + `CAP-018-recording.mp4`,
staged here per `PROJECT_RULES.md` §2. Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

## 1. Capture Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-018` |
| Purpose | Group Y — isolate whether `CAP-016-FINDINGS.md` §11's `0x0044` BLE notification burst is triggered by BLE link establishment alone, with the Buds/case completely untouched |
| Date | 2026-09-12 |
| Firmware | ⚪ ASSUMPTION `release_5.203` (carried over, not re-checked on-screen or on-the-wire this session) |
| Test device | Pixel 7a, Android 17, official Pixel Buds Companion App `1.0.955078536`, Google Play services active |
| Log file | [`CAP-018-btsnoop_hci.log`](./CAP-018-btsnoop_hci.log) — 2,309 packets, 409.75s, `2026-09-12 06:06:05.722–06:12:55.471`. `capinfos`/`cap_len==len` confirms 0/2,309 mismatches — untruncated. |
| Video file | [`CAP-018-recording.mp4`](./CAP-018-recording.mp4) — 152.16s (`ffprobe`), burned-in overlay `06:07:20`–`06:09:52`, 1280×720@30fps |
| Notes file | [`CAP-018-EVENT-NOTES.md`](./CAP-018-EVENT-NOTES.md) — corrected Event Timeline |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:...:07` (classic) |

## 2. Methodology

Per `AGENTS.md` §13's CLI-hygiene rule, every Bluetooth address appearing in the log was enumerated
before any protocol-specific filtering:

```
$ tshark -r CAP-018-btsnoop_hci.log -T fields -e bthci_evt.bd_addr -e bthci_acl.dst.bd_addr \
    -e bthci_acl.src.bd_addr | tr '\t' '\n' | grep -v '^$' | sort -u
04:00:6e:cf:6e:07   # Buds, classic address (same physical unit as every prior capture)
e8:d5:2b:7e:ca:81   # phone's own controller address
3b:35:92:1f:8e:10 / 40:a8:ef:16:bb:35 / 47:db:37:51:d3:41 / 50:bc:f8:b8:fc:fc /
53:92:39:78:6f:f2 / 5e:c7:63:1e:ee:c4 / 60:3b:b0:00:41:45 / 60:82:1f:83:55:4c
    # unrelated background BLE addresses
```

Video reviewed via dense `ffmpeg -ss <t> -frames:v 1` extraction against its own burned-in overlay:
every 2s across `t=0–65` (the connection-forming window), 1s resolution across `t=1–14` (to pin the
Bluetooth-toggle tap) and `t=55–64`, then every 5–10s across the rest of the video to its end
(`t=70–151`). No frame shows the buds/case moved from their initial resting position on top of the
phone at any point.

## 3. Central finding — the burst appears, but on a connection attributable to an unrelated device, not the Buds (🟢 FACT for this session)

**Step 1 — isolation confirmed.** Full video review (152.16s, every reviewed frame) shows the
buds/case sitting untouched on top of the phone from `t=0` to the video's end — same framing/shadow
throughout, no hand ever approaches them. Bluetooth was toggled on at video overlay `~06:07:23`
(frames `t=2`→`t=4`), confirmed to within ~1s of wall-clock by the log's own classic-controller
bring-up sequence (`Write Extended Inquiry Response`/`Write Scan Enable`, frames 328–363,
`06:07:23.926–06:07:24.023`) — i.e. video-overlay time and log wall-clock time agree closely in this
capture, consistent with every other capture in this project that has checked this.

**Step 2 — the classic (RFCOMM) reconnect is slow this session, but clean.** The phone does not
actually send `Create Connection` to the Buds' classic address until `06:08:27.773` (frame 761) —
**~64s after** the Bluetooth toggle — during which only background `LE Extended Advertising Report`s
appear (no BLE connection, no page attempts). `Connect Complete` (status `0x00`) follows at
`06:08:28.615` (frame 763), a single-attempt, stored-key reconnect (`Link Key Request`→`Reply`, no
SSP) — matching `PROTOCOL.md` §5.1's already-FACT reconnect path. The device row visible on screen
from `t=6` onward showing "L:100%, C:95%, R:100%" *before* this real connection completes is Android's
own cached/last-known display, not a live read — it only turns purple/"Active" once the classic link
is actually up (video `t=90`, `06:08:50`, well after the wire's own `06:08:28.6` completion).
**Bonus, incidental finding**: this project's own captures have generally shown the classic reconnect
completing within a few seconds of the Bluetooth toggle; a ~64s gap between toggle and the phone
actually initiating `Create Connection` is new to this project's own record — 🔴 **OPEN QUESTION**,
not investigated further here (out of this session's own scope).

**Step 3 — DLCI 0x04's connect-time Get/Notify fires exactly as expected (🟢 FACT, further ADR-021/022 replication).**

```
$ tshark -r CAP-018-btsnoop_hci.log -Y "btrfcomm.dlci==4 and btrfcomm.len>0" -T fields \
    -e frame.number -e frame.time -e frame.p2p_dir -e data.data | grep -E "0811|0813"
1024  2026-09-12T06:08:29.171402+0200  0  08110000
1041  2026-09-12T06:08:29.206434+0200  1  0813000401e80020
```
`Get` at frame 1024, `Notify` at frame 1041 (35ms later), `Settable-toggles=0x00` (docked — matches
the buds video-confirmed sitting in the open case throughout, per `DECISIONS.md` ADR-024),
`Current=0x20`=Off. A further, unremarkable same-session confirmation of ADR-021/ADR-022.

**Step 4 — a *second*, unrelated LE connection forms 20s after the video ends, and the `0x0044` burst rides on it, not on the Buds' own link.**

```
$ tshark -r CAP-018-btsnoop_hci.log -Y "bthci_evt.le_meta_subevent==0x0a" -T fields \
    -e frame.number -e frame.time -e bthci_evt.bd_addr -e bthci_evt.connection_handle
1589  2026-09-12T06:10:10.007996+0200  40:a8:ef:16:bb:35  0x0004
```
This is the **only** `LE Enhanced Connection Complete` event anywhere in the entire 409.75s log —
address `40:a8:ef:16:bb:35`, chandle `0x0004`, **not** the Buds' own classic address
(`04:00:6e:cf:6e:07`, chandle `0x0003`). It lands at `06:10:10.008` — **~18s after this video's own
end** (`06:09:52`) — so this connection and everything that follows it is not video-covered; the
isolation claim (buds/case untouched) is established for the video's own 152.16s window, not for
this specific later moment, though nothing in the wire log between video-end and this connection
(§5 below) suggests a physical action either.

```
$ tshark -r CAP-018-btsnoop_hci.log -Y 'bthci_acl.chandle==0x0004 and btatt.opcode==0x11' -x
# frame 1639, 06:10:10.198782:
0000  02 04 20 1a 00 16 00 04 00 11 14 40 00 45 00 e8   .. ........@.E..
0010  bf 6c cf 17 8b 32 b8 4c 48 6a e5 00 ff ba ab      .l...2.LHj.....
```
Decoded: ATT opcode `0x11` (Read By Group Type Response), group length `0x14`=20B, **start handle
`0x0040`, end handle `0x0045`**, 128-bit UUID (wire bytes reversed:
`abbaff00-e56a-484c-b8328b17cf6cbfe8`) — an unnamed/vendor UUID, same "Unknown Service" outcome
`CAP-016-FINDINGS.md` §11 itself reported for this handle range. **This service's handle range
(`0x0040`–`0x0045`) contains handle `0x0044`** — the exact handle the burst occurs on. The same
discovery walk on chandle `0x0004` also finds GAP, GATT, Device Information, a second unnamed 128-bit
service (`0x0050`–`0x0054`), and, critically, a **standard, SIG-assigned Heart Rate service
(`0x180D`)**:

```
1663  2026-09-12T06:10:10.319016+0200  1  Rcvd Read By Group Type Response, Attribute List Length: 1, Heart Rate
```

**None of the Buds' own already-known GATT structure appears on this connection** — no Google Fast
Pair Service (`0xFE2C`), no `0x0c00`–`0x0c14`/`0x0f2X`/`0x0f3X` handle cluster (`CAP-034-FINDINGS.md`
§4, `PROTOCOL.md` §4.3 Option D). A Heart Rate service is not part of the Buds' own confirmed profile
anywhere in this project's records. This structurally matches `CAP-032-FINDINGS.md` §5's own
already-documented finding of "one LE connection resolving to an unrelated random-address device
exposing a Heart Rate GATT service, not the Buds" — the same category of background/incidental
nearby-device noise (a fitness tracker or similar), not the same literal address (BLE random/resolvable
addresses are not expected to repeat across sessions).

**Confirms the classic connection carries zero ATT traffic, and only two connection handles exist in
this entire log:**

```
$ tshark -r CAP-018-btsnoop_hci.log -Y 'bthci_acl.chandle==0x0003 and btatt' -T fields -e frame.number | wc -l
0
$ tshark -r CAP-018-btsnoop_hci.log -T fields -e bthci_acl.chandle | sort -u | grep -v '^$'
0x0003
0x0004
```

**The `0x0044` burst itself:**

```
$ tshark -r CAP-018-btsnoop_hci.log -Y "btatt.handle==0x0044 and btatt.opcode==0x1b" -T fields \
    -e frame.number -e frame.time -e bthci_acl.chandle
1929  2026-09-12T06:10:12.474202+0200  0x0004
1933  2026-09-12T06:10:12.556688+0200  0x0004
1934  2026-09-12T06:10:12.558115+0200  0x0004
...   (23 total, all chandle 0x0004)
```
23 frames, matching `CAP-016-FINDINGS.md` §11's original count exactly. Frame 1934's payload
(`...ff115755a9fe0003a9fe...`) contains the same recurring `a9 fe` (`0xfea9`) byte-pair marker that
capture reported.

## 4. What this resolves, and what it does not

**Resolves, for this capture (🟢 FACT):** the `0x0044` burst is **not** carried on the Buds' own
classic ACL connection (chandle `0x0003`, zero ATT traffic) and is **not** tied to the Buds' own BLE
identity in any way this session can confirm — it rides a wholly separate LE connection, to a
different address, whose GATT profile structurally matches an unrelated nearby device (Heart Rate
service present; no Fast Pair Service, no Buds-known handle cluster). This directly answers `GATT-002`'s
original question ("is the burst triggered by BLE link establishment alone, independent of any
bud/case action?") with a **different, sharper answer than either of the two outcomes the Group Y
procedure anticipated**: it isn't that the Buds' own BLE link independently triggers the burst
regardless of physical action — in this capture, the burst isn't attributable to the Buds' link at
all.

**Does not resolve:** whether `CAP-016`'s *original* burst (same session that also had genuine bud
removal/insertion activity nearby) was itself from the Buds or from a similarly-unrelated device —
this capture cannot retroactively re-examine `CAP-016`'s own log, and no cross-capture check of
`CAP-016-btsnoop_hci.log` was performed here (out of this task's per-phase scope). Also not resolved:
what device `40:a8:ef:16:bb:35` actually is (no Device Name characteristic read was observed in the
frame range checked), or why the classic reconnect took ~64s this session.

## 5. Cross-checks — nothing in the wire log between video-end and the LE connection

The video's own 152.16s coverage ends at `06:09:52`; the unrelated LE connection forms at `06:10:10`,
18s later. Checked this gap directly for any Buds-side signal that might indicate a bud/case action
happened off-camera in that window:

```
$ tshark -r CAP-018-btsnoop_hci.log -Y 'bthci_acl.chandle==0x0003 and frame.time>="2026-09-12 06:09:52" and frame.time<="2026-09-12 06:10:10"' \
    -T fields -e frame.number -e _ws.col.Info
(no RFCOMM data frames — only background BLE advertising reports and periodic Number-of-Completed-Packets in this window)
```
No DLCI activity of any kind on the Buds' own chandle in this gap — consistent with (not proof of) the
buds/case remaining untouched through the unvideoed 18s tail, matching the pattern of every reviewed
video frame.

## 6. External validation

`0x180D` (Heart Rate Service) and the standard GAP/GATT/Device Information services are Bluetooth
SIG-assigned identifiers, resolved directly by Wireshark's own bundled BT SIG assigned-numbers
database (the same mechanism already relied on for `CAP-034-FINDINGS.md`'s GATT profile work) — no
further external fetch was needed or performed for this specific identification.

## 7. Test-ID traceability

- **`GATT-002`** (primary): exercised as planned (Bluetooth enabled from an already-bonded, untouched
  state) — see §3 for the result, which answers the question with a different, more specific finding
  than either originally-anticipated outcome.

## 8. Conclusions & proposed downstream updates — proposals only, awaiting maintainer sign-off

**Recorded as this session's own factual result (no sign-off needed, purely descriptive):**
- Full video review confirms zero bud/case contact for the entire 152.16s recording.
- The `0x0044` burst (23 frames, chandle `0x0004`) rides a connection this session's own GATT
  discovery evidence (Heart Rate service present, no Fast Pair Service, no Buds-known handle range)
  attributes to an unrelated nearby BLE device, not the Buds — the Buds' own classic connection
  (chandle `0x0003`) carries zero ATT traffic throughout.
- The classic reconnect took ~64s from Bluetooth-toggle to `Create Connection` this session — slower
  than this project's typical reconnect timing, not investigated further.

**Proposed (⏳ awaiting maintainer sign-off, per `AGENTS.md` §6/§15 — these are proposals, not
promotions):**
1. `PROTOCOL.md` §6's existing `0x0044`/`0xfea9` open item (added 2026-08-18 from `CAP-016-FINDINGS.md`
   §11) should be updated with this capture's finding: in this session, the burst is attributable to
   an unrelated device, not the Buds — sharpening, not closing, the open question (whether *any*
   occurrence of this burst across this project's captures was ever genuinely Buds-originated is now
   itself open).
2. A new 🔴 open question for `PROTOCOL.md` §6: what causes the classic reconnect to take ~64s in this
   session, when other captures typically complete within a few seconds of the Bluetooth toggle?
3. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `GATT-002` row — Evidence column pointer to this file.
4. `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 — `CAP-018` row status update (this file's own Step L).

## 9. Open Questions

- 🔴 Was `CAP-016`'s *original* `0x0044` burst genuinely Buds-originated, or could it too have been
  an unrelated nearby device coincidentally connecting around the same time as the bud
  removal/insertion that session's own procedure involved? Not checked by this capture.
- 🔴 What is `40:a8:ef:16:bb:35`'s actual identity (device name was not observed in the checked frame
  range)?
- 🔴 Why did the classic reconnect take ~64s from Bluetooth-toggle to `Create Connection` this
  session?

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-018-2026-09-12_06-07-20_06-09-52-Group_Y/CAP-018-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-018-2026-09-12_06-07-20_06-09-52-Group_Y/CAP-018-FINDINGS
