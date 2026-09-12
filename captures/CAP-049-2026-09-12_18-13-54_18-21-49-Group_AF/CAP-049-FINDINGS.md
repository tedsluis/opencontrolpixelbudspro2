# CAP-049: Unexplained disconnect/reconnect cycling, repeat of `CAP-039` (Group AF, `OBS-006`)

Standardized, evidence-based extraction from `CAP-049-btsnoop_hci-combined.log` (see §0) +
`CAP-049-recording.mp4`, staged here per `PROJECT_RULES.md` §2. Every claim below carries a status
per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

## 1. Capture Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-049` |
| Purpose | Group AF (repeat of `CAP-039`) — reproduce, or rule out, the unexplained disconnect/reconnect cycling `CAP-039-FINDINGS.md` §6 found (5 cycles, no clear trigger), this time with continuous phone-screen recording throughout to catch any background system event |
| Date | 2026-09-12 |
| Firmware | ⚪ ASSUMPTION `release_5.203` (carried over) |
| Test device | Pixel 7a, Android 17, official Pixel Buds Companion App `1.0.955078536`, Google Play services active |
| Log file | Main: [`CAP-049-btsnoop_hci.log`](./CAP-049-btsnoop_hci.log) (2,526 packets, `18:14:58.996–18:24:15.745`). Rotated tail: [`CAP-049-btsnoop_hci.log.last`](./CAP-049-btsnoop_hci.log.last) (1,866 packets, `18:13:55.218–18:14:56.767`). Combined, header-safe merge: [`CAP-049-btsnoop_hci-combined.log`](./CAP-049-btsnoop_hci-combined.log) (4,392 packets, `18:13:55.218–18:24:15.745`, 620.53s) — see §0. |
| Video file | [`CAP-049-recording.mp4`](./CAP-049-recording.mp4) (renamed from `CAP-049-recordings.mp4`, `git mv`) — 495.45s (`ffprobe`), overlay `18:13:54`–`18:21:49` |
| Notes file | [`CAP-049-EVENT-NOTES.md`](./CAP-049-EVENT-NOTES.md) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:...:07` |

## 0. `.log.last` integrity determination — genuine same-session rotation (🟢 FACT)

**Opposite determination from `CAP-048`.** Both files share an identical 16-byte btsnoop global
header:
```
$ xxd -l 16 CAP-049-btsnoop_hci.log.last
00000000: 6274 736e 6f6f 7000 0000 0001 0000 03ea  btsnoop.........
$ xxd -l 16 CAP-049-btsnoop_hci.log
00000000: 6274 736e 6f6f 7000 0000 0001 0000 03ea  btsnoop.........
```

```
$ capinfos CAP-049-btsnoop_hci.log.last | grep -E "Earliest|Latest"
Earliest: 2026-09-12 18:13:55.523844   Latest: 2026-09-12 18:14:56.767281
$ tshark -r CAP-049-btsnoop_hci.log.last -T fields -e frame.number -e frame.time -e _ws.col.Info | head -1
1  2026-09-12T18:13:55.218251+0200  Sent Reset
$ tshark -r CAP-049-btsnoop_hci.log.last -Y "bthci_evt.code==0x05" -T fields -e frame.number -e frame.time -e bthci_evt.reason
1844  18:14:56.403279  0x16
1864  18:14:56.665557  0x16
$ tshark -r CAP-049-btsnoop_hci.log -T fields -e frame.number -e frame.time -e _ws.col.Info | head -1
1  2026-09-12T18:14:58.996263+0200  Sent Reset
```

`.log.last` **begins with its own fresh `Sent Reset`** at `18:13:55.22` — matching this video's own
start (`18:13:54`) and its first documented step ("Bluetooth is OFF" → toggled on). It **ends** with
two locally-terminated disconnects at `18:14:56.4`/`.67` — matching this session's own
draft-documented step "user opens Bluetooth quick panel and toggles Bluetooth OFF" (~`18:14:48`).
The main log then begins, 2.2s later, with its **own** fresh `Sent Reset` (`18:14:58.996`) — matching
the draft's very next step, "Bluetooth toggled ON" (forced reconnect). **This is a genuine
intra-session on-device log rotation**, not leftover content from an untracked prior period (unlike
`CAP-048`'s own `.log.last`, which predated its own session's video start entirely) — the physical
event that caused the rotation (the deliberate Bluetooth-off/on cycle) is itself part of this
session's own documented procedure.

**Combining method** (per this task's Guardrails — a raw `cat` would corrupt the result, since
btsnoop's own global header must appear exactly once):
```python
HEADER_LEN = 16
with open('CAP-049-btsnoop_hci.log.last', 'rb') as f: first = f.read()
with open('CAP-049-btsnoop_hci.log', 'rb') as f: second = f.read()
assert first[:HEADER_LEN] == second[:HEADER_LEN]
combined = first + second[HEADER_LEN:]  # keep first file's header, strip second file's
```
Verified: `capinfos` on the result reports exactly `1,866 + 2,526 = 4,392` packets, span
`18:13:55.218–18:24:15.745` (620.53s), 0/4,392 `cap_len≠len` mismatches. **The combined file is used
as this session's sole evidence below.**

## 2. Central finding — the cycling does NOT reproduce (🟢 FACT, clean negative)

```
$ tshark -r CAP-049-btsnoop_hci-combined.log -Y "bthci_evt.bd_addr==04:00:6e:cf:6e:07 and bthci_evt.code==0x03" \
    -T fields -e frame.number -e frame.time -e bthci_evt.connection_handle
691   18:14:05.361236  0x0002
736   18:14:09.395217  0x0005
2150  18:15:01.966566  0x0002   <- the one deliberate, draft-documented reconnect
```

Only **one** genuine reconnect to the Buds' classic address occurs across the entire 620.53s combined
log, beyond the initial connect settling (two chandles within the first few seconds of the session).
After the `18:15:01.97` reconnect:

```
$ tshark -r CAP-049-btsnoop_hci-combined.log -Y 'bthci_acl.chandle==0x0002' -T fields -e frame.number -e frame.time | tail -3
4374  18:24:00.988040
4375  18:24:00.990208
4376  18:24:00.990801
$ tshark -r CAP-049-btsnoop_hci-combined.log -Y 'bthci_acl.chandle==0x0002 and btrfcomm.len>0' -T fields -e frame.number -e frame.time -e btrfcomm.dlci | tail -3
4363  18:24:00.968389  0x04
4370  18:24:00.981973  0x08
4375  18:24:00.990208  0x02
```

**The classic connection (chandle `0x0002`) is still carrying live RFCOMM traffic on multiple DLCIs
at `18:24:00.99`** — 8m59s after the deliberate reconnect, and right up to near the log's own end
(`18:24:15.75`). **No further `Disconnection Complete` for this chandle occurs anywhere in the log.**
This directly and cleanly answers this Group's own question: **`CAP-039`'s 5-cycle disconnect/
reconnect pattern did not reproduce in this repeat**, under a continuous-phone-screen-recording
condition. Per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AF's own procedure note ("If it does *not*
reproduce, that is itself a useful negative result"), this is recorded as such, not treated as a
failed session.

**One unrelated disconnect, ruled out:**
```
3510  18:17:43.675  Rcvd Disconnect Complete, chandle 0x0003, reason 0x08 (Connection Timeout)
```
Chandle `0x0003` never appears in the Buds' own classic Connection Complete events — this is a
separate, unrelated (background) BLE link timing out, not the Buds' own connection. Not investigated
further.

## 3. `OBS-006` — Set vs. Get `Settable-toggles` comparison, consistent (🟢 FACT, sparser same-session confirmation of ADR-024)

```
$ tshark -r CAP-049-btsnoop_hci-combined.log -Y "btrfcomm.dlci==4 and btrfcomm.len>0" -T fields \
    -e frame.number -e frame.time -e frame.p2p_dir -e data.data | grep -E "\s08(11|12|13)"
925   18:14:10.186  0  08110000                    <- Get (initial connect)
946   18:14:10.247  1  0813000401e8e808            <- Notify, Settable=0xe8, Current=0x08 (NC)
1567  18:14:24.543  0  0812001401e8e840...          <- Set (user taps Adaptive)
1574  18:14:24.985  1  0813000401e8e840            <- Notify, Settable=0xe8, Current=0x40 (Adaptive)
1576  18:14:24.987  1  0813000401e8e840            <- (duplicate Notify, same content)
2596  18:15:03.032  0  08110000                    <- Get (forced reconnect)
2610  18:15:03.052  1  0813000401e8e840            <- Notify, Settable=0xe8, Current=0x40 (unchanged)
```

The Set-triggered Notify (`18:14:24.99`) and the Get-triggered Notify (`18:15:03.05`) both read
`Settable-toggles=0xe8` (undocked) — video-confirmed (buds worn/out of the case throughout, per the
draft's own opening step and consistent screen state through the session). This is a same-session,
trigger-independent confirmation of `DECISIONS.md` ADR-024, the same kind of result `CAP-039-FINDINGS.md`
§3 already established with 10 samples — this capture adds a smaller (2-sample), independent
same-session confirmation, no counter-example.

## 4. Cross-checks

- Video confirms the ANC Set (`t=30`, `18:14:24`, "Device details," Adaptive selected) and the
  reconnect (`t=68`, `18:15:02`, "Active") to within the timestamps above.
- **Correction to the draft**: from roughly `18:15:15` onward, the phone screen shows the **system
  Bluetooth quick-settings sheet** (device row: "Active. L:100%, R:100% batt…"), not the Pixel Buds
  app's own "Device details" screen as the draft's later rows claim — spot-checked at `t=300`
  (`18:18:54`) and consistent at every other checked point through the video's end. This does not
  affect any wire finding (no DLCI 0x02/0x04/0x08 traffic occurs during this stretch beyond the
  events already accounted for above).
- No contradiction found with any existing 🟢 FACT.

## 5. Test-ID traceability

- **`OBS-006`**: exercised — Set-vs-Get comparison confirmed (§3); the disconnect-cycling
  reproduction attempt is a clean negative (§2).

## 6. Conclusions & proposed downstream updates

**Recorded as this session's own factual result (no sign-off needed, purely descriptive):**
- `.log.last` genuinely rotated mid-session; a safe combined log was produced and used.
- The cycling behavior did not reproduce — the classic connection stayed open and stable for the
  entire ~9-minute session after the one deliberate reconnect.
- The Set-vs-Get `Settable-toggles` comparison is consistent, no counter-example.

**Proposed (⏳ awaiting maintainer sign-off, per `AGENTS.md` §6/§15):**
1. `PROTOCOL.md` §6's open item on `CAP-039`'s cycling (added 2026-09-06) — add this capture's clean
   negative result: the cycling did not reproduce with continuous phone-screen recording, so its
   original trigger remains unidentified and was not caught here either (a negative result does not
   itself explain `CAP-039`'s own occurrence).
2. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `OBS-006` row — Evidence column pointer to this file.
3. `CAP-049-btsnoop_hci-combined.log` — proposed to be kept alongside the two source files as the
   session's primary analysis artifact, per this task's own Step G instruction (not overwriting
   either original).

## 7. Open Questions

- 🔴 `CAP-039`'s original disconnect/reconnect cycling trigger remains unidentified — this repeat's
  own clean negative doesn't explain it, only confirms it isn't a reliably-reproducible phenomenon
  under this same general procedure.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-049-2026-09-12_18-13-54_18-21-49-Group_AF/CAP-049-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-049-2026-09-12_18-13-54_18-21-49-Group_AF/CAP-049-FINDINGS
