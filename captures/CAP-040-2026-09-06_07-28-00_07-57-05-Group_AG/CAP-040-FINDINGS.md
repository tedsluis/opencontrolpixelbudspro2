# Findings: `CAP-040` (Group AG — DLCI 0x08's unmapped Get-shaped codes vs. a known-changing value, `PRIV-001`)

Standardized, evidence-based extraction from `CAP-040-btsnoop_hci.log` + `CAP-040-recrding.mp4`,
staged here for later promotion into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-040` · **Date:** 2026-09-06 · **Firmware:** ⚪ ASSUMPTION `release_5.203`
(carried over) · **Phone:** Pixel 7a, Android 14, official Pixel Buds Companion App, Google Play
Services enabled · **Log file:** `CAP-040-btsnoop_hci.log` (main log — see §0 for why this is the
only file used) · **Video:** `CAP-040-recrding.mp4` (note: filename typo in the original recording,
"recrding," left as-is — 1745.23s, ~07:28:00–07:57:05 local, wall-clock overlay) · **Buds MAC
(partial):** `04:00:6e:cf:6e:07`.

---

## 0. Capture integrity — untruncated, and a major correction to this task's own Guardrail 5 assumption (🟢 FACT)

```
$ capinfos CAP-040-btsnoop_hci.log
Number of packets:   8,539       Capture duration: 1831.987236 s
Earliest packet time: 2026-09-06 07:28:07.138550   Latest: 2026-09-06 07:58:39.125786
$ tshark -r CAP-040-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3{c++} END{print "mismatches:", c+0}'
mismatches: 0
```

No truncation, raw extraction path. **`CAP-040-btsnoop_hci.log.last` is not the earlier half of
this session.** It spans 07:07:28.040–07:28:04.684 — its earliest packet timestamp
(`07:07:28.040028`) is byte-for-byte identical to `CAP-039-btsnoop_hci.log`'s own earliest packet,
and its Buds (`04:00:6e:cf:6e:07`) Connection-Complete chandles/timestamps
(`0x0002@07:07:34.091`, `0x0005@07:07:35.111`, `0x0006@07:08:25.794`, `0x0007@07:09:01.366`,
`0x0008@07:09:38.914`, `0x0009@07:10:24.477`, `0x000a@07:11:04.170`) match CAP-039's own log
exactly:

```
$ tshark -r CAP-040-btsnoop_hci.log.last -Y "bthci_evt.bd_addr==04:00:6e:cf:6e:07 and bthci_evt.code==0x03" \
  -T fields -e frame.number -e frame.time
653   2026-09-06T07:07:34.090967000+0200
683   2026-09-06T07:07:35.110759000+0200
1711  2026-09-06T07:08:25.794177000+0200
2523  2026-09-06T07:09:01.366166000+0200
3369  2026-09-06T07:09:38.914402000+0200
4406  2026-09-06T07:10:24.476988000+0200
5260  2026-09-06T07:11:04.170356000+0200
```

`.log.last` is leftover on-device buffer content that happens to contain **CAP-039's own session**
plus the ~14-minute idle gap before CAP-040 started — not part of Group AG's own procedure. This
task's Guardrail 5 asked to "confirm whether CAP-040 actually rotated... before assuming it needs
the same treatment" as CAP-042; this is that confirmation, and the answer is **no, not in the way
assumed** — there is no genuine intra-session rotation here to merge back together. CAP-040's own
main log starts cleanly and independently at `07:28:07.139` (frame 1), with the Buds' own fresh
Connection Complete for *this* session at frame 646, `07:28:12.077`:

```
$ tshark -r CAP-040-btsnoop_hci.log -Y "bthci_evt.bd_addr==04:00:6e:cf:6e:07 and bthci_evt.code==0x03" \
  -T fields -e frame.number -e frame.time -e bthci_evt.connection_handle
646   2026-09-06T07:28:12.076671000+0200   0x0002
```

**Every finding below uses only this main, unmerged log.** `CAP-040-btsnoop_hci-merged.log` was
created per this task's Guardrail 5 instruction but is explicitly **not** used as evidence —
treating it as one continuous CAP-040 session would misattribute CAP-039's own traffic to CAP-040.

## 1. Connection topology this session — the App's own "Connect"/"Disconnect" buttons produce zero wire signal (🟢 FACT, major correction to this session's own procedure notes)

```
$ tshark -r CAP-040-btsnoop_hci.log -T fields -e bthci_acl.chandle | sort -u
0x0002
0x0003
```

Only two chandles the whole session; `0x0003` is unrelated background BLE traffic (not pursued,
same pattern as `CAP-036-FINDINGS.md` §1's Fitbit-precedent). The Buds' own chandle (`0x0002`) is
established once at `07:28:12.077` and **never disconnects until `07:55:41.465`** (`bthci_evt.code
== 0x05`, reason `0x13`) — the only `Disconnection Complete` event in the entire 30-minute session:

```
$ tshark -r CAP-040-btsnoop_hci.log -Y "bthci_evt.code==0x05" -T fields \
  -e frame.number -e frame.time -e bthci_evt.connection_handle -e bthci_evt.reason
7784  2026-09-06T07:55:41.465485000+0200  0x0002  0x13
```

**DLCI 0x08 (RFCOMM channel 4) opens exactly once** (`SABM`→`UA`, frames 958/962, `07:28:12.805`–
`.810`) and **never bounces (`DISC`) for the rest of the session**:

```
$ tshark -r CAP-040-btsnoop_hci.log -Y "bthci_acl.chandle==0x0002" -T fields -e frame.time -e _ws.col.Info \
  | grep "Channel=4 " | grep -E "SABM|DISC"
2026-09-06T07:28:12.805007000+0200  Sent SABM Channel=4 (UUID128: Unknown)
(no further Channel=4 SABM/DISC anywhere else in the log)
```

**DLCI 0x04 (channel 2) does bounce, but only 10 times total across the whole session** — once at
session start (`07:28:12.700`), then **nine more times clustered entirely within the last ~70
seconds** (`07:54:38`–`07:55:39`), immediately before the one real disconnect:

```
$ tshark -r CAP-040-btsnoop_hci.log -Y "bthci_acl.chandle==0x0002" -T fields -e frame.time -e _ws.col.Info \
  | grep "Sent SABM Channel=2 "
2026-09-06T07:28:12.699925000+0200
2026-09-06T07:54:38.575484000+0200
2026-09-06T07:55:09.139456000+0200
2026-09-06T07:55:14.537102000+0200
2026-09-06T07:55:19.869730000+0200
2026-09-06T07:55:25.351504000+0200
2026-09-06T07:55:30.882529000+0200
2026-09-06T07:55:33.720526000+0200
2026-09-06T07:55:36.216520000+0200
2026-09-06T07:55:39.920964000+0200
```

**Direct check of a documented "Tapped 'Disconnect' in App" → "Tapped 'Connect' in App" pair
(`07:29:46`/`07:29:50`) finds zero frames of any kind on the Buds' chandle in that window:**

```
$ tshark -r CAP-040-btsnoop_hci.log -Y "bthci_acl.chandle==0x0002 and frame.time >= \"2026-09-06 07:29:44\" and frame.time <= \"2026-09-06 07:29:53\"" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e _ws.col.Info
(0 rows)
```

**Conclusion:** the official app's own in-app "Connect"/"Disconnect" buttons, as tapped in this
session, do not tear down or rebuild the RFCOMM connection or any DLCI channel — the classic ACL
link and DLCI 0x08 stay open continuously from `07:28:12` through `07:55:41`. Whatever the
"Disconnect"/"Connect" button pair actually does (a local UI-state toggle, or a change scoped to a
higher layer this project hasn't identified), it is not visible at the RFCOMM/HCI level in this
session. **This invalidates the ~15-repeat bracketing this session's own procedure intended**: the
documented Event Timeline's ~30 "Tapped Disconnect/Connect in App" and "Case closed/opened (auto)"
rows between `07:28:44` and `07:52:09` correspond to **zero** distinguishable wire events for the
vast majority of them — see `CAP-040-EVENT-NOTES.md`'s corrected Contamination log for the full
detail. The one place in this session with genuine, wire-confirmed channel/connection activity is
the `07:54:28`–`07:55:41` cluster addressed in §2/§4 below, which falls *after* this session's own
documented end time (`07:52:10`) and inside the video's own previously-unreviewed tail.

## 2. Video re-pass — the previously-unreviewed tail (07:52:10–07:57:05) contains the session's only genuine dock/disconnect event (🟢 FACT)

The video (`CAP-040-recrding.mp4`) is 1745.23s long — the Event Timeline's documented window
(`07:28:00`–`07:52:10`, 1450s) covers only the first ~83% of it. Per this task's "no sampling, full
analysis" rule, the tail was reviewed:

| Video offset | Wall clock | On-screen state |
|---|---|---|
| 1468s | `07:52:28` | "Device details," **Disconnect** button visible (connected), Left 95% / Case 85% / Right 100%, both earbuds visibly loose beside the case (undocked) |
| 1580s | `07:56:20` | "Device details," **Connect** button visible (disconnected), Left **100%** / Case **84%** / Right 100%, both earbuds visibly seated in the case (docked) |
| 1712s | `07:57:00` | Same screen, still disconnected/docked, values unchanged |

This confirms: (1) wire and video timestamps agree closely (no drift correction needed for this
capture, unlike `CAP-037`–`CAP-039`); (2) the session's only real disconnect (`07:55:41.465`, §1)
is reflected on screen from `07:56:20` onward as expected; (3) the Left-earbud percentage jumped
from its slowly-discharging `95` to `100` exactly across this real docking event — see §4.

**Correction to this session's own Preparation checklist note** ("these values remain exactly the
same throughout the entire 24-minute video"): true for the documented 24-minute (1450s) window, but
not for the video as a whole — Left and Case both change in the final, previously-unreviewed ~5
minutes.

## 3. `PRIV-001`'s central question: the 7 flagged unmapped codes fire exactly once (🟢 FACT for the observation; inconclusive for the correlation test)

```
$ tshark -r CAP-040-btsnoop_hci.log -Y "bthci_acl.chandle==0x0002 and btrfcomm.dlci==8 and btrfcomm.len>0" \
  -T fields -E separator='|' -e frame.number -e frame.time -e frame.p2p_dir -e data.data
```

99 total DLCI 0x08 payload frames across the whole session. All 7 flagged codes appear **exactly
once**, bundled together in a single `Sent` frame immediately after DLCI 0x08's one-and-only
`SABM`/`UA` (§1):

```
Frame 980, 07:28:12.857467, Sent:
050c0000 040200000 40400000 04110000 04130000 04150000 0e0400000301001b08a003109a8ac1a9071a104575726f70652f416d7374657264616d09030000
```

Decoded as consecutive `[Group:1][Code:1][Len:2BE=0000]` zero-length Gets: `05 0c 00 00`,
`04 02 00 00`, `04 04 00 00`, `04 11 00 00`, `04 13 00 00`, `04 15 00 00`, `0e 04 00 00` — matching
`CAP-036-FINDINGS.md` §5's shape exactly — followed in the same frame by the already-documented
`Group 0x03 Code 0x02` firmware string (`ADR-012`) and the `Europe/Amsterdam` capability blob
(`0x01 00 1b 08 a0 03 10 9a 8a c1 a9 07 1a 10 <"Europe/Amsterdam"> 09 03 00 00`).

**Because DLCI 0x08 never reopens for the rest of the session (§1), none of these 7 codes recur —
there is no second, third, ... occurrence to compare against a bracketed dock-state change.** The
intended correlation test (does any `Rcvd` response field track dock state across repeats) cannot
be run: N=1 for every one of the 7 codes. This is a clean, well-evidenced **negative result for
this specific capture's execution** (per `AGENTS.md` §13.6, no semantic reading is offered for any
of these 7 codes — there is nothing to correlate against), not a negative result for the underlying
question. A re-run using a trigger that genuinely reopens DLCI 0x08 (the OS Bluetooth toggle, per
`CAP-037`'s own procedure, or physical case-lid/bud-removal cycling) is needed before concluding
anything about what these codes mean.

**Full raw response inventory for completeness** (none of these are new claims — all match already
-documented content from `CAP-036-FINDINGS.md` §5/§12.2 and `PROTOCOL.md` §4.3 Option E):

```
$ awk -F'|' '{print substr($4,1,8)}' dlci08_all.tsv | sort | uniq -c | sort -rn
     26 04030004   <- Group 0x04 Code 0x03 (Option E cross-check, field3=Right%, PROTOCOL.md §4.3)
     22 0e02001a   <- Group 0x0e Code 0x02, constant "google-pixel-buds-pro-v1" capability string
     15 0e010021   <- Group 0x0e Code 0x01 (Option E battery triple, ADR-014) — see §4
      6 0e010023   <- same, longer variant (see §4)
      6 04050002
      3 09020002
      3 04160002
      2 04120004
      ...
      1 050c0000   <- (one of the 7 flagged codes, bundled with the other 6 in frame 980)
```

## 4. Bonus finding: DLCI 0x08's Left-earbud battery field reports the `0xff` sentinel at the exact moment of physical docking (🟡 HYPOTHESIS, new, single occurrence)

Decoding every `Group 0x0e Code 0x01` occurrence (`ADR-014`'s already-FACT `[value, flag, index]`
triple, `index=1`→Left/`2`→Right/`3`→Case) across the whole session:

```python
# 3-level protobuf decode: body -> field1(bytes) -> {field1="all", field2=entries_blob} ->
# entries_blob -> repeated field1(bytes, 6B) -> {field1=value, field2=flag, field3=index}
```

| Time | Frame | Left (idx=1) | Right (idx=2) | Case (idx=3) |
|---|---|---|---|---|
| 07:28:12.928 – 07:43:57.917 (13 occurrences) | 1029…3980 | `96`→`95` (stable at 95 from 07:28:17 on) | `100` | `85` |
| **07:54:28.342** | **6243** | **`255` (`0xff`, the documented "unknown" sentinel)** | `100` | `85` |
| 07:54:29.613 onward (8 occurrences) | 6270…7464 | `100` | `100` | `85`→`84` |

Raw hex for the transition frames:
```
$ grep -E "6243|6270" dlci08_all.tsv
6243|2026-09-06T07:54:28.342208000+0200|1|0e0100240a220a03616c6c121b0a0708ff01100118010a060864100118020a060855100118032001
6270|2026-09-06T07:54:29.613584000+0200|1|0e0100230a210a03616c6c121a0a060864100118010a060864100118020a060855100118032001
```
Frame 6243's Left entry is `0a07 08ff01 1001 1801` — a 7-byte sub-message (one byte longer than the
usual 6, because `0xff` zigzags to a 2-byte varint `ff 01`) decoding to `value=255, flag=1, idx=1`.
Frame 6270 (1.27s later) reads `value=100, flag=1, idx=1`.

**This single sentinel-then-100 transition falls exactly inside the video-confirmed real docking
event** (§2: undocked at `07:52:28`, docked+disconnected at `07:56:20`; the wire-confirmed
`SABM`/`DISC` churn and final disconnect span `07:54:28`–`07:55:41`, i.e. the same window). Per
`AGENTS.md` §13.6, this is reported as a correlation, not a proven mechanism: 🟡 **HYPOTHESIS** —
DLCI 0x08's Left-earbud battery field may report the `0xff`/255 "unknown" sentinel momentarily at
the point of physical case contact (sensor/contact-read gap), before settling to a value that looks
like a charging-state placeholder (`100`) rather than a fresh percentage reading. Single occurrence,
one session — not proposed for promotion.

## 5. Test-ID traceability (`AGENTS.md` §13)

- **`PRIV-001`** (primary): exercised, but the session's own procedure deviation (§1) limited the
  achievable evidence to N=1 per code — see §3's outcome classification (c), with the bonus lead in
  §4.
- **`PAIR-003`** (incidental, reconnect to an already-bonded device): the one genuine reconnect
  cluster (`07:54:38`–`07:55:39`) plausibly exercises this, but since it falls in the previously
  undocumented video tail, no clean single-action correlation is claimed here beyond what §2/§4
  already establish.
- **`BATT`-family** (incidental): §4's battery-field observation is incidental evidence for this
  family, not a dedicated `BATT-00x` test.

## 6. Conclusions & proposed downstream updates — ⏳ awaiting maintainer sign-off for every proposed item

**Recorded as this session's own factual result (no sign-off needed, per `AGENTS.md` §15 — these
are observations, not FACT promotions to `PROTOCOL.md`):**
- The `.log.last`/CAP-039 overlap (§0) and the App Connect/Disconnect buttons' zero-wire-signal
  behavior (§1) are direct, repeatable observations from this capture's own log.
- The video re-pass correction (§2) and the 7-flagged-codes single-occurrence finding (§3) are
  likewise direct observations.

**Proposed only, NOT committed — awaiting explicit maintainer sign-off per `AGENTS.md` §6/§15:**
1. `PROTOCOL.md` §6 — add a new 🔴 open question: do the app's in-app "Connect"/"Disconnect"
   buttons ever produce wire-visible traffic under any condition (this session: no, 0/~15 taps)?
2. `PROTOCOL.md` §6 — add a new 🟡 HYPOTHESIS candidate (§4): `Group 0x0e Code 0x01`'s Left-earbud
   field may emit the `0xff` sentinel at the instant of physical docking.
3. `PROTOCOL.md` §6 — the 7 originally-flagged DLCI 0x08 unmapped codes (`05 0c`/`04 02`/`04 04`/
   `04 11`/`04 13`/`04 15`/`0e 04`) remain fully unattributed; this capture does not close that
   question, and a note should be added that a repeat with a genuine RFCOMM-reopening trigger is
   still needed.
4. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `PRIV-001` row — Evidence column pointer to this file.
5. `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 / `id_registry.csv` — `CAP-040` row status update.

None of the above touches `DECISIONS.md` — no finding in this capture reaches this project's
promotion bar, and none is proposed to.

## 7. Open questions

- 🔴 The 7 flagged DLCI 0x08 unmapped codes remain entirely unattributed (§3) — needs a re-run with
  a trigger that genuinely reopens DLCI 0x08 (OS Bluetooth toggle or physical case cycling, not the
  app's own Connect/Disconnect buttons).
- 🔴 Does the app's in-app "Connect"/"Disconnect" button pair ever produce any wire-visible signal
  under any condition, or is it purely local/optimistic UI state (§1)?
- 🔴 What does the `0xff`→`100` transition on `Group 0x0e Code 0x01`'s Left field actually represent
  (§4) — single occurrence, not reconciled with any documented mechanism.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-040-2026-09-06_07-28-00_07-57-05-Group_AG/CAP-040-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-040-2026-09-06_07-28-00_07-57-05-Group_AG/CAP-040-FINDINGS
