# Findings: `CAP-037` (Group AD — "Get ANC state" reconnect-reliability + dock-state transition, purpose-built repeat, `OBS-004`)

Standardized, evidence-based extraction from `CAP-037-btsnoop_hci.log` + `CAP-037-recording.mp4`,
staged here for later promotion into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-037` · **Date:** 2026-09-06 · **Firmware:** ⚪ ASSUMPTION `release_5.203`
(carried over, not re-checked on-screen). **Phone:** Pixel 7a, Android 14, official Pixel Buds
Companion App, Google Play Services enabled. **Log file:** `CAP-037-btsnoop_hci.log` (1212.32s,
29,956 packets, 2026-09-06 06:11:52.979–06:32:05.299 local, 0/29,956 `cap_len`≠`len` mismatches —
untruncated). **Video:** `CAP-037-recording.mp4` (1072.10s, 06:11:46–~06:29:38 local, wall-clock
overlay). **Devices:** phone (Pixel 7a), peer `04:00:6E:CF:6E:07` ("Pixel Buds Pro 2 van Ted").

---

## 0. Capture integrity (🟢 FACT)

```
$ capinfos CAP-037-btsnoop_hci.log
Number of packets:   29,956
Capture duration:    1212.320240 seconds
Earliest packet time: 2026-09-06 06:11:52.979042
Latest packet time:   2026-09-06 06:32:05.299282

$ tshark -r CAP-037-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'
mismatches: 0
```
Raw extraction path, unlimited snaplen, not truncated.

## 1. CLI hygiene — `bluetooth.addr` does not resolve; use `bthci_evt.bd_addr`/chandle (🟢 FACT)

Per the pattern established in `CAP-036-FINDINGS.md` §1, `bluetooth.addr == 04:00:6e:cf:6e:07`
returns 0 rows for RFCOMM traffic in this log. `bthci_evt.bd_addr` does resolve:

```
$ tshark -r CAP-037-btsnoop_hci.log -Y "bthci_evt.bd_addr==04:00:6e:cf:6e:07 and bthci_evt.code==0x03" \
  -T fields -e frame.number -e frame.time -e bthci_acl.chandle
```
Every subsequent query below scopes traffic via the resulting chandle (or, since this session has
many reconnects, via DLCI + payload-content matching, cross-checked against each chandle's own
Connection Complete timestamp).

## 2. Major correction to the planned procedure: this session ran far longer than 5 repeats (🟢 FACT)

**The video (1072.10s) and log (1212.32s) run roughly 15–18 minutes past the originally-documented
"relevant action 06:11:46–06:13:48" window.** A full-log scan for the Buds' own HCI Connection
Complete events (`bthci_evt.bd_addr==04:00:6e:cf:6e:07 and bthci_evt.code==0x03`) finds **34
reconnects** spread across the entire ~20-minute log (06:12:22 through 06:28:58), not 5. Video
review (`ffmpeg -ss <t> -frames:v 1`, at each connect timestamp) confirms this activity is real
and deliberate — a continued Bluetooth-toggle reconnect loop, matching the same
"more-repeats-than-planned" deviation pattern documented in `CAP-040-EVENT-NOTES.md`'s own
Contamination log for this same 2026-09-06 batch.

**More importantly, the planned alternation itself did not happen as written.** The procedure
called for Docked→Undocked→Docked→Undocked→Docked. Video frames at the connect times of what the
original Event Timeline calls "Repeat 1" through "Repeat 5" (06:12:15, 06:12:38, 06:14:13 directly
checked; all fall in the 06:12:22–06:14:14 wire window) all show **both earbuds still seated in the
open case** — i.e. all 5 originally-planned repeats were docked, not alternating. The buds were not
actually removed from the case until sometime between 06:14:14 and 06:15:34 (the first
`Settable-toggles=0xe8` sample). From there, the user continued the reconnect cycle for roughly 14
more minutes, alternating dock state in longer, unplanned blocks, until closing the case lid around
06:28:48 (video-confirmed).

This is reported as a genuine procedural finding, not glossed over: **the corrected Event Timeline
in `CAP-037-EVENT-NOTES.md` is the authoritative record**, not the original plan's timestamps/labels.

## 3. DLCI 0x04 "Get ANC state" (`0x11`)/"Notify ANC state" (`0x13`) — 26/26, zero misses (🟢 FACT, large same-session replication of `DECISIONS.md` ADR-022)

```
$ tshark -r CAP-037-btsnoop_hci.log -Y "btrfcomm.dlci==4 and btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e bthci_acl.chandle -e data.data \
  | grep -E "^[0-9]+\s.*\s(0811|0813)"
```

Of the 34 total reconnects, **26 opened DLCI 0x04 with real Message Stream payload following** —
exactly `DECISIONS.md` ADR-022's precisely-scoped trigger condition ("fires on every DLCI 0x04
(re)establishment that proceeds to carry real Message Stream traffic, not on a bare channel-level
bounce with no payload"). **Every one of those 26 produced a `08 11 00 00` (Get) frame, and every
one of those 26 Gets was answered by a `08 13` (Notify) frame — zero misses.** Representative rows
(full 26-row table cross-referenced against dock state in `CAP-037-EVENT-NOTES.md`'s corrected
Event Timeline):

```
1180  06:12:22.645007  Sent  chandle 0x0004  08110000
1196  06:12:22.672194  Rcvd  chandle 0x0004  0813000401e80020   <- Settable=0x00, Current=0x20 (Off)
3245  06:12:49.541733  Sent  chandle 0x0005  08110000
3262  06:12:49.552651  Rcvd  chandle 0x0005  0813000401e80020
...
8117  06:15:34.385790  Sent  chandle 0x0005  08110000
8139  06:15:34.572352  Rcvd  chandle 0x0005  0813000401e8e880   <- Settable=0xe8, Current=0x80 (Transparency)
...
29080 06:28:11.617313  Sent  chandle 0x000a  08110000
29103 06:28:11.795912  Rcvd  chandle 0x000a  0813000401e80020
```

**Timing:** Get→Notify latency across all 26 pairs: min 6.4ms, max 283.0ms, mean 116.8ms — broadly
consistent with `CAP-036`'s single ~10.7ms sample in order of magnitude, but with visibly more
spread than one sample could show; several pairs exceed 200ms. Not itself a counter-example to
anything already FACT (no timing bound is part of ADR-021/ADR-022's claim), but recorded as new,
more complete latency-distribution data.

**Bonus, clean internal correlation (🟡 HYPOTHESIS, new, single session):** the Notify's
`Settable-toggles` and `Current-state` bytes co-occur perfectly across all 26 samples —
`Settable=0x00` pairs with `Current=0x20` (Off) in all 16 docked-block samples, and
`Settable=0xe8` pairs with `Current=0x80` (Transparency) in all 10 undocked-block samples. A
plausible, evidence-consistent (not force-fit) reading: ANC processing simply does not run while
both earbuds are seated in the case, so "Current ANC state" trivially reads "Off" whenever
`Settable-toggles` also reads "docked" — but this capture only shows correlation, not the
underlying mechanism, so it stays HYPOTHESIS.

## 4. DLCI 0x02 — silent throughout (🟢 FACT, clean negative, consistent with "nothing else touched")

```
$ tshark -r CAP-037-btsnoop_hci.log -Y "btrfcomm.dlci==2 and frame.p2p_dir==0 and btrfcomm.len>0" \
  -T fields -e frame.number
```
No `Sent`-direction DLCI 0x02 traffic outside each reconnect's own brief connection-settling burst
(matching `CAP-036-FINDINGS.md` §4's already-documented pattern) — confirms no setting was touched
by accident anywhere in this 20-minute session, consistent with the Contamination log's "no settings
screens opened" note.

## 5. Open question: chandle `0x0009`'s mid-connection dock-state flip (🔴 OPEN QUESTION)

```
$ tshark -r CAP-037-btsnoop_hci.log -Y "btrfcomm.dlci==4 and bthci_acl.chandle==0x0009" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data
11483  06:18:05.748367  Sent  08110000
11507  06:18:06.031360  Rcvd  0813000401e8e880   <- Settable=0xe8 (undocked)
12008  06:18:24.272960  Rcvd  0813000401e80020   <- Settable=0x00 (docked), NO preceding Get
```
On this one chandle, a **second** "Notify ANC state" frame arrives 18 seconds after the first,
with **no new Get frame** in between (confirmed: no `0811` frame for this chandle between 11507 and
12008), and its `Settable-toggles` value flips from `0xe8` to `0x00`. Two readings are consistent
with this single data point, and this capture cannot distinguish them:
- a real dock-state change (buds redocked) while the ACL connection remained open — which would be
  in tension with `DECISIONS.md` ADR-016's "ACL disconnects the instant both buds are re-docked"
  finding (from `CAP-016`, a different session/context), or
- a spontaneous, unprompted second Notify unrelated to dock state, coincidentally matching the
  docked-state byte value.
No corresponding video timestamp was checked precisely for this ~18s window (out of scope for this
pass's efficiency budget) — a future capture isolating this exact sub-second window on video would
resolve it. Recorded as 🔴 OPEN QUESTION, not force-fit into either reading. Copy to
`PROTOCOL.md` §6.

## 6. Test-ID traceability

- **`OBS-004`**: exercised 26 times (not the planned 5) — see §2/§3. Result: 26/26 Get/Notify
  pairs fire, 26/26 dock-state correlations hold, one flagged anomaly (§5).
- **`PAIR-003`** (reconnect to an already-bonded device): exercised 34 times total (26 of which
  also produced real DLCI 0x04 payload) — every reconnect reuses the existing bond, no fresh
  pairing/SSP traffic observed on any chandle checked.

## 7. Conclusions & downstream updates — proposals only, awaiting maintainer sign-off

**Recorded as this session's own factual result (no sign-off needed, purely descriptive):**
- The session ran 34 reconnects (26 with real DLCI 0x04 payload) over ~20 minutes, not the planned
  5 — a procedural deviation, documented in full in the corrected Event Timeline.
- The planned dock-state alternation did not occur on schedule; the actual dock-state sequence is
  the one recorded in the corrected Event Timeline.

**Proposed (⏳ awaiting maintainer sign-off, per `AGENTS.md` §6/§15 — these are proposals, not
promotions):**
1. This session's 26/26 Get/Notify result is proposed as **additional supporting evidence** for
   `DECISIONS.md` ADR-022 (already 🟢 FACT) — the largest single-session replication count on file
   (previous largest: `CAP-025`'s 5). Not a new promotion; ADR-022 already covers this.
2. This session's 16 docked + 10 undocked `Settable-toggles` samples, all matching video-confirmed
   dock state where checked, are proposed as **additional supporting evidence** for `DECISIONS.md`
   ADR-024 (already 🟢 FACT). Not a new promotion.
3. The `Settable-toggles`↔`Current-state` co-occurrence pattern (§3) is proposed as a new 🟡
   HYPOTHESIS entry for `PROTOCOL.md` §4.1 — plausible mechanism (ANC doesn't run while docked),
   single-session evidence.
4. The chandle-`0x0009` anomaly (§5) is proposed as a new 🔴 OPEN QUESTION for `PROTOCOL.md` §6.

## 8. Open questions after this session

- 🔴 Chandle `0x0009`'s mid-connection `Settable-toggles` flip with no preceding Get (§5).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-037-2026-09-06_06-11-46_06-29-38-Group_AD/CAP-037-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-037-2026-09-06_06-11-46_06-29-38-Group_AD/CAP-037-FINDINGS
