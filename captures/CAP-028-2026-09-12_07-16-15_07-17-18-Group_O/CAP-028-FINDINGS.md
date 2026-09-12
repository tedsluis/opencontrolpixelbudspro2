# CAP-028: Head gestures (Group O, `HEAD-002`, `HEAD-003`)

Standardized, evidence-based extraction from `CAP-028-btsnoop_hci.log` + `CAP-028-recording.mp4`,
staged here per `PROJECT_RULES.md` §2. Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

## 1. Capture Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-028` |
| Purpose | Group O — physical head-gesture actions (Nod/Shake); requires Head gestures already enabled (`CAP-020`, Group F) |
| Date | 2026-09-12 |
| Firmware | ⚪ ASSUMPTION `release_5.203` (carried over) |
| Test device | Pixel 7a, Android 17, official Pixel Buds Companion App `1.0.955078536`, Google Play services active |
| Log file | [`CAP-028-btsnoop_hci.log`](./CAP-028-btsnoop_hci.log) — 2,187 packets, 227.71s, `2026-09-12 07:16:14.148–07:20:01.855`. 0/2,187 `cap_len≠len` mismatches — untruncated. |
| Video file | [`CAP-028-recording.mp4`](./CAP-028-recording.mp4) — 62.68s (`ffprobe`), overlay `07:16:15`–`07:17:18`. Camera points at the phone screen, not the user's head/ears. |
| Notes file | [`CAP-028-EVENT-NOTES.md`](./CAP-028-EVENT-NOTES.md) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:...:07` |

## 2. Precondition check — Head gestures already enabled (🟢 FACT, from `CAP-020`)

`CAP-020-FINDINGS.md` §4/§6 confirmed the "Use head gestures" toggle was left ON at the end of that
session (2026-08-21), specifically as this Group's own precondition. This session does not re-toggle
it (no `field5{field4{field29=...}}` write appears anywhere in this log's DLCI 0x02 traffic — checked,
see §4). The wire's own connect-time "Notify ANC state" reads `Settable-toggles=0xe8` (undocked, per
`DECISIONS.md` ADR-024) at `06:16:21.742` (frame 765) — confirming the Buds were **not** seated in the
case at connect time, consistent with being worn for this Group's physical-gesture test. **The video
itself cannot independently confirm this** — the camera frames the phone/case on a table, and whether
the earbuds are visibly seated in the case's two charging cavities is genuinely ambiguous at this
lighting/resolution (checked directly, brightened crop at `t=45` — inconclusive either way). The
wire's own dock-state indicator is treated as the authoritative source here, consistent with this
project's existing preference for wire evidence over ambiguous visual inspection.

## 3. A second, unrelated LE connection carries substantial ATT traffic overlapping the gesture window (🟢 FACT, matches `CAP-018`'s already-documented pattern)

```
$ tshark -r CAP-028-btsnoop_hci.log -Y "bthci_evt.le_meta_subevent==0x01 or bthci_evt.le_meta_subevent==0x0a" \
    -T fields -e frame.number -e frame.time -e bthci_evt.bd_addr -e bthci_evt.connection_handle
736  2026-09-12T07:16:21.689359+0200  7d:0a:16:e6:10:68  0x0003
```
A second LE connection forms 0.78s after the classic `Connect Complete` (frame 241, `07:16:20.912`),
to an address distinct from the Buds' own classic address, on chandle `0x0003` (the Buds' classic
connection is chandle `0x0002`). Its own GATT discovery walk:

```
$ tshark -r CAP-028-btsnoop_hci.log -Y "bthci_acl.chandle==0x0003 and btatt.opcode==0x11" -T fields \
    -e frame.number -e _ws.col.Info
822  Rcvd Read By Group Type Response, Attribute List Length: 3, GAP, GATT, Device Information
837  Rcvd Read By Group Type Response, Attribute List Length: 1, Unknown
...
880  Rcvd Read By Group Type Response, Attribute List Length: 1, Heart Rate
```
**A standard Heart Rate service (`0x180D`) is present** — the same signature `CAP-018-FINDINGS.md`
§3 already attributed to an unrelated nearby device, not the Buds. This connection carries 62
`Handle Value Notification`s (handle `0x0044` — the same handle `CAP-016-FINDINGS.md` §11 flagged)
and 40 `Write Command`s (handle `0x0042`) between `07:16:25.29` and `07:17:02.87` —
**overlapping this session's own claimed head-gesture testing window (`07:16:29`–`07:17:15`, per the
Event Timeline)**. Given the structural match to `CAP-018`'s already-established finding (Heart Rate
service present, no Fast Pair Service, no `0x0c0X` handle cluster — checked, absent here too), this
traffic is attributed to the same category of unrelated background device, **not** the Buds — a
coincidental timing overlap with the gesture window, not evidence of a Buds-side head-tracking signal.
Payload content was not decoded further (high-entropy-looking bytes, no recognizable tag structure) —
out of scope for an unrelated device's own traffic.

**What this does NOT establish:** whether this is literally the *same* physical unrelated device as
`CAP-018`'s (BLE random/resolvable addresses are not expected to repeat across sessions, so address
identity can't be checked) — only that the same *category* of signature (Heart Rate service, no Buds
markers) recurs a second time, now a third instance counting `CAP-032-FINDINGS.md` §5's original
report.

## 4. The Buds' own connection carries zero traffic during the claimed gesture window (🟢 FACT, clean negative)

```
$ tshark -r CAP-028-btsnoop_hci.log -Y '(btrfcomm.dlci==2 or btrfcomm.dlci==4 or btrfcomm.dlci==8) \
    and btrfcomm.len>0 and frame.time>="2026-09-12 07:16:25" and frame.time<="2026-09-12 07:17:15"' \
    -T fields -e frame.number
(0 rows)
```

From the end of the connect-time settling burst (`07:16:25.49`, frame 1448) through `07:17:15.55`
(frame 1834, the next routine periodic push — see §5), **zero** DLCI 0x02/0x04/0x08 frames appear on
the Buds' own chandle (`0x0002`). This window fully contains the claimed head-gesture testing period
(`07:16:29`–`07:17:15`, per the corrected Event Timeline). Also checked and ruled out: no AVRCP/AVCTP
traffic anywhere in this window (`avctp or avrcp` filter, 0 rows) and no HCI-level SCO/eSCO connection
(call) anywhere in the entire log — consistent with there being no active call/notification context,
which `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s own `HEAD-002`/`HEAD-003` description ties Nod/Shake's
actual function to ("Answers a call"/"Rejects a call... or dismisses a text reply").

**Directly answers this Group's own analysis question**: the gesture(s), whether or not physically
performed, produced no wire-visible RFCOMM/GATT signal on the Buds' own connection — consistent with
(not contradicting) the reading that Nod/Shake are functionally inert without an active
call/notification for them to act on, rather than evidence the gestures weren't performed at all.
This capture cannot distinguish "no call was active, so nothing happened" from "the gesture itself was
never physically performed" — both produce the identical, silent wire signature.

## 5. Cross-checks

- A routine periodic DLCI 0x02/0x04/0x08 cross-channel push (`CAP-036-FINDINGS.md` §12.5/§4.3 Option
  B pattern) fires at `07:17:15.55` (frames 1834–1847) and again at `07:18:02.17` and `07:19:35.78` —
  matching the already-documented irregular-interval autonomous push, unrelated to any gesture (its
  timing coincides with, but does not follow from, the end of the video's own coverage).
- A brief HID-Control/HID-Interrupt L2CAP connect-then-immediate-disconnect (frames 1492–1516,
  `07:16:27.28–.48`) occurs on the Buds' own classic ACL (chandle `0x0002`) — matches
  `CAP-002-FINDINGS.md`/`ARCHITECTURE.md` §1's already-documented, still-unconfirmed HID hypothesis;
  not decoded further, not attributed to the gesture window (it precedes it).
- No contradiction found with any existing 🟢 FACT.

## 6. Test-ID traceability

- **`HEAD-002`/`HEAD-003`**: attempted (per the draft's own procedure note), but not camera-visible.
  Wire evidence is a clean negative on the Buds' own channels — see §4. Inconclusive whether this
  reflects "no call/notification was active" (expected-silent) or "gestures weren't actually
  triggered" — this capture cannot distinguish the two.

## 7. Conclusions & proposed downstream updates

**Recorded as this session's own factual result (no sign-off needed, purely descriptive):**
- Zero DLCI 0x02/0x04/0x08 traffic on the Buds' own connection during the entire claimed gesture
  window; no AVRCP traffic; no SCO/eSCO call anywhere in the log.
- A second, unrelated LE connection (Heart Rate service signature, matching `CAP-018`) coincidentally
  overlaps the same window with substantial ATT traffic — not Buds-attributable.

**Proposed (⏳ awaiting maintainer sign-off, per `AGENTS.md` §6/§15):**
1. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `HEAD-002`/`HEAD-003` rows — record this inconclusive result
   (no active call/notification context, so a functionally-inert gesture is the expected explanation,
   but not confirmed over a genuinely triggered-and-silent gesture) — a repeat with an actual
   incoming call/notification active would be needed for a conclusive test.
2. `PROTOCOL.md` §6 — this is the third session (after `CAP-032`, `CAP-018`) to show the same
   unrelated-nearby-device (Heart Rate service) BLE-connection signature; worth a cross-reference note
   alongside `CAP-018`'s own proposed update.

## 8. Open Questions

- 🔴 Do Nod/Shake ever produce wire-visible traffic when an actual call or notification is active?
  Not tested by this capture (no such context existed).
- 🔴 Was the unrelated Heart-Rate-service device in `CAP-018`/`CAP-028` (and `CAP-032`'s original
  report) the same physical device across sessions? Not determinable from BLE address alone.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-028-2026-09-12_07-16-15_07-17-18-Group_O/CAP-028-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-028-2026-09-12_07-16-15_07-17-18-Group_O/CAP-028-FINDINGS
