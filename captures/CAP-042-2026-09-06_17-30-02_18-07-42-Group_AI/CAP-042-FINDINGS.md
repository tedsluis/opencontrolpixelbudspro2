# Findings: `CAP-042` (Group AI — long pure-idle bracket for the periodic DLCI 0x02/0x04/0x08/HFP push cadence, `OBS-002`)

Standardized, evidence-based extraction from `CAP-042-btsnoop_hci.log` + `CAP-042-recording.mp4`,
staged here for later promotion into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Status legend:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-042` · **Date:** 2026-09-06 · **Firmware:** ⚪ ASSUMPTION `release_5.203`
(carried over, not re-checked on screen this session — screen stayed off). **Phone:** Pixel 7a,
Android ⚪ ASSUMPTION (carried from `CAP-036`, not visible this session). **App:** official Pixel
Buds Companion App, connected but **backgrounded** (not kept open — this is the deliberate
difference from `CAP-036`'s open-app baseline). **Google Play Services:** ⚪ ASSUMPTION enabled
(carried forward, not re-verified). **Buds MAC (partial):** `04:00:6e:cf:6e:07`.

---

## 0. Capture integrity — and a correction to this task's own framing (🟢 FACT)

```
$ capinfos CAP-042-btsnoop_hci.log
Number of packets:   8,560
Earliest packet time: 2026-09-06 17:30:02.870980
Latest packet time:   2026-09-06 18:07:41.745756
Capture duration:    2258.874776 seconds   (~37m38.9s)

$ tshark -r CAP-042-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'
mismatches: 0
```
Untruncated, no snaplen cap.

**Correction to the task brief's stated premise — the `.log.last` file is NOT part of this
session.** The task description asserted "the 33-minute session rotated mid-way, and both files
together cover the full session... confirmed, not just suspected." This does not hold up:

```
$ capinfos CAP-042-btsnoop_hci.log.last
Earliest packet time: 2026-09-06 17:11:56.963543
Latest packet time:   2026-09-06 17:30:00.907421

$ tshark -r CAP-041-*/CAP-041-btsnoop_hci.log -Y "bthci_evt.bd_addr==04:00:6e:cf:6e:07 and bthci_evt.code==0x03" \
  -T fields -e frame.number -e frame.time -e bthci_evt.connection_handle
279  2026-09-06T17:11:59.210239+0200  0x0002
1414 2026-09-06T17:13:22.493626+0200  0x0004
2642 2026-09-06T17:15:39.443412+0200  0x0005

$ tshark -r CAP-042-btsnoop_hci.log.last -Y "bthci_evt.bd_addr==04:00:6e:cf:6e:07 and bthci_evt.code==0x03" \
  -T fields -e frame.number -e frame.time -e bthci_evt.connection_handle
279  2026-09-06T17:11:59.210239+0200  0x0002
1414 2026-09-06T17:13:22.493626+0200  0x0004
2642 2026-09-06T17:15:39.443412+0200  0x0005
```
`CAP-042-btsnoop_hci.log.last` (17:11:56.964–17:30:00.907, 4,829 packets) reproduces **CAP-041's
own three Connection Complete events** at identical frame numbers, timestamps, and chandles, then
simply continues ~10.5 minutes further (to 17:30:00.907) — covering the idle gap between the
`CAP-041` and `CAP-042` sessions. This is leftover content from the phone's continuously-running
on-device snoop buffer, extracted a second time when `CAP-042`'s own bugreport was pulled — not an
intra-session rotation of `CAP-042`'s own data. `CAP-042`'s own main log starts completely cleanly
at frame 1 (17:30:02.871), with the Buds' own fresh Connection Complete for **this** session at
frame 277 (17:30:04.780):
```
$ tshark -r CAP-042-btsnoop_hci.log -Y "bthci_evt.bd_addr==04:00:6e:cf:6e:07 and bthci_evt.code==0x03" \
  -T fields -e frame.number -e frame.time -e bthci_evt.connection_handle
277  2026-09-06T17:30:04.779890+0200  0x0002
```
**Conclusion: use ONLY the main, unmerged `CAP-042-btsnoop_hci.log` as this session's evidence.**
The genuine idle-bracket duration is **~37m39s** (17:30:02.871–18:07:41.746), not the ~55m45s
combined-file span and not the "33-minute" figure in the task's own framing — both of those numbers
are artifacts of treating `.log.last` as part of this session, which the evidence above rules out.
This is the same contamination pattern independently found and documented for `CAP-040` (its
`.log.last` overlaps with `CAP-039`'s own session) — not a one-off.

**Chandle/DLCI census, main log, whole session:**
```
$ tshark -r CAP-042-btsnoop_hci.log -T fields -e bthci_acl.chandle | sort -u | grep -v '^$'
0x0002 0x0003 0x0006 0x0009 0x000b 0x000f 0x0010 0x0012 0x0013 0x0014 0x0015
```
Only `chandle 0x0002` belongs to the Buds (one single, unbroken ACL connection for the entire
37m39s session — no reconnect, matching Group AI's design). Every other chandle belongs to a
**different** device, `68:44:30:d6:fb:72`, connecting/disconnecting repeatedly throughout the
session (13 separate Connection Complete events) — structurally the same kind of unrelated
background traffic already excluded in `CAP-004-FINDINGS.md` §1 (Fitbit) and `CAP-036-FINDINGS.md`
§1 (a second BLE device) — out of scope, excluded from everything below.
```
$ tshark -r CAP-042-btsnoop_hci.log -Y "bthci_acl.chandle==0x0002 and btrfcomm" -T fields -e btrfcomm.dlci | sort | uniq -c
     32 0x00
    112 0x02
     36 0x04
     44 0x08
      2 0x0a
     45 0x0c
```

## 1. Dock-state sanity check (🟢 FACT for this session's reading)

```
$ tshark -r CAP-042-btsnoop_hci.log -Y "bthci_acl.chandle==0x0002 and btrfcomm.dlci==4 and btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time_relative -e frame.p2p_dir -e data.data
598  2.345  Sent  08110000
602  2.484  Rcvd  0813000401e8e808
```
`Settable-toggles=0xe8` → **undocked**, per `DECISIONS.md` ADR-024. Video-confirmed (`ffmpeg -ss 5`
on `CAP-042-recording.mp4`, video-overlay time 17:29:53): both earbuds visibly resting loose beside
the open, empty case — matches the "undocked" reading exactly, and the fixed dock state the
Group AI procedure required (held constant, not touched again for the rest of the session — a
second spot-check at video-overlay time 18:04:48, ~35 minutes in, shows the identical undisturbed
scene: screen off, buds still loose beside the case). `Current state=0x08` = ANC/Active.

## 2. Full-session occurrence census, DLCI 0x02/0x04/0x08 + HFP `AT+BIEV` (🟢 FACT for the raw counts)

**The central, surprising result of this session: after the initial connect-settling burst, the
periodic push recurs only TWICE more in the entire 37m39s session — at ~16m14s and ~35m1s in —
not at the ~10–20s cadence `CAP-036`'s own ~7-minute session showed.**

```
$ tshark -r CAP-042-btsnoop_hci.log -Y "bthci_acl.chandle==0x0002 and btrfcomm.dlci==8 and btrfcomm.len>0 and frame.p2p_dir==1" \
  -T fields -e frame.number -e frame.time_relative -e data.data
```
18 total DLCI 0x08 Rcvd payload frames in the whole session (vs. `CAP-036`'s much denser count in
1/5th the time). Grouped by cluster:

| Cluster | Frames | Session time (`frame.time_relative`) | Content |
|---|---|---|---|
| Connect burst | 688–751 | 2.680–4.346s | Full connect-time content: capability blob, firmware string, Option E battery triple (`0e0100210a1f...`, frame 709, decodes `[value,flag,index]`≈Left/Right/Case all reading high, consistent with a fresh, just-connected read), Device Info Group 0x03 fields, `Group 0x04 Code 0x12` alternating-value pings (frames 721/724/729/751, values cycling per `DECISIONS.md` ADR-016 finding 7) |
| Push #1 | 4522, 4606 | 974.084s, 977.350s | Both `Group 0x04 Code 0x12` (`0412000408031001` / `0412000408021001` — the alternating-value ping, NOT a fresh Option E battery-triple push) |
| Push #2 | 7569, 7671 | 2101.036s, 2104.301s | Same shape as Push #1 (`0412...0803...` / `0412...0802...`) |

**No fresh Option E battery-triple (`0e01`) push occurs anywhere after the initial connect burst** —
only the `Group 0x04 Code 0x12` alternating ping recurs, twice per cluster, ~3.27s apart within each
cluster (974.084→977.350s; 2101.036→2104.301s).

```
$ tshark -r CAP-042-btsnoop_hci.log -Y "bthci_acl.chandle==0x0002 and btrfcomm.dlci==4 and btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time_relative -e frame.p2p_dir -e data.data
```
27 total DLCI 0x04 Rcvd+Sent frames. Beyond the connect burst (frames 571–631, includes the
`08 11`/`08 13` ANC Get/Notify from §1, Fast Pair Device-Info Group 0x03 fields, and `Group 0x07`
SASS traffic), only 4 more frames occur: `Group 0x07 Code 0x34` at 974.086s/977.378s (Push #1) and
2101.036s/2104.154s (Push #2) — same two-cluster pattern as DLCI 0x08.

```
$ tshark -r CAP-042-btsnoop_hci.log -Y "bthci_acl.chandle==0x0002 and btrfcomm.dlci==2 and btrfcomm.len>0 and frame.p2p_dir==1" \
  -T fields -e frame.number -e frame.time_relative
```
52 total DLCI 0x02 Rcvd frames: the connect-settling burst (36 frames, 3.403–6.526s, matching
`CAP-036-FINDINGS.md` §4's already-documented shape) plus **8 frames in Push #1** (974.056–977.146s)
and **6 frames in Push #2** (2101.353–2103.412s) — the same connect-settling-burst-shaped content
recurring, not a new frame type.

```
$ tshark -r CAP-042-btsnoop_hci.log -Y "bthci_acl.chandle==0x0002 and btrfcomm.dlci==12" -T fields \
  -e frame.number -e frame.time_relative -e _ws.col.Info | grep -i BIEV
468  2.264  Rcvd AT+BIEV=1,1
522  2.343  Rcvd AT+BIEV=2,100
```
**Exactly 2 `AT+BIEV` occurrences in the whole session — both inside the initial connect-settling
burst. `AT+BIEV` does NOT recur at all during Push #1 or Push #2.** DLCI 0x0c's own full-session
frame count is only 45 (mostly consumed by SLC setup) — consistent with this being a genuine
absence, not a filtering artifact (re-checked with a plain `contains "BIEV"` grep, same 2 results).

## 3. Cross-channel timing — near-lockstep holds *within* each push cluster, but HFP drops out entirely (🟡 HYPOTHESIS, single session)

| Event | DLCI 0x02 | DLCI 0x04 | DLCI 0x08 | HFP `AT+BIEV` |
|---|---|---|---|---|
| Connect burst | 3.403–6.526s | 2.596–6.53s (approx, spans SLC+Info) | 2.680–4.346s | 2.264s, 2.343s |
| Push #1 | 974.056–977.146s | 974.086s, 977.378s | 974.084s, 977.350s | **absent** |
| Push #2 | 2101.353–2103.412s | 2101.036s, 2104.154s | 2101.036s, 2104.301s | **absent** |

Within Push #1 and Push #2, DLCI 0x02/0x04/0x08 still fire within tens of milliseconds to a few
hundred ms of each other (e.g. Push #1's first sub-event: DLCI 0x02 at 974.056s, DLCI 0x04 at
974.086s, DLCI 0x08 at 974.084s — all within 30ms) — **the cross-channel synchronization
`CAP-036-FINDINGS.md` §12.5 documented between DLCI 0x02/0x04/0x08 continues to hold at this much
longer timescale.** But **HFP's `AT+BIEV` — which `CAP-036` §12.5 found joining this same
near-lockstep group within 7–18ms, extending the sync to 4 mechanisms — does not participate in
either Push #1 or Push #2 here.** 🟡 **HYPOTHESIS (new, single session, only 2 data points):**
either HFP's own push is genuinely absent during backgrounded/settled idle (contradicting
`CAP-036`'s 3.5–7-minute-old, still-settling sample), or its trigger condition (a real battery-value
change, per `DECISIONS.md` ADR-015's "push-on-change" reading) simply didn't occur during this
window while the other three channels' pushes are driven by something else entirely (see §5).

## 4. Gap-distribution comparison against `CAP-009-FINDINGS.md` §2's HFP-only model (🟡 HYPOTHESIS, revises the model's applicability)

`CAP-009`'s 101-minute HFP-only session: min 0.015s, max 878.9s (≈14.6 min), median 20.5s, settling
burst then irregular.

This session's actual gap structure (post-settling-burst, all channels combined):

| Gap | Duration |
|---|---|
| Connect-burst end (6.53s) → Push #1 start (974.06s) | **967.5s (16m7.5s)** |
| Push #1 end (977.38s) → Push #2 start (2101.04s) | **1123.7s (18m43.7s)** |
| Push #2 end (2104.30s) → session end (2258.87s) | 154.6s (session simply ends here, not a confirmed further gap) |

**Both measured gaps (16m7.5s, 18m43.7s) exceed `CAP-009`'s own previously-documented maximum gap
of ~14.6 minutes.** With only 2 gaps to measure, this cannot establish a new ceiling with any
confidence (🟡 HYPOTHESIS, n=2) — but it is a genuine, citable data point that the push (at least
for DLCI 0x02/0x04/0x08 specifically, in a backgrounded-app/settled-idle condition) can go
substantially longer than 14.6 minutes without firing. This does **not** contradict `ADR-015`'s
"not a fixed cadence" finding — it extends it.

## 5. Candidate explanation, flagged not claimed: app-foreground state, not connection-supervision (🔴 OPEN QUESTION)

`CAP-036-FINDINGS.md` §12.5 speculated the near-lockstep push's shared trigger is "plausibly
Buds-side, e.g. a periodic connection-supervision tick." That session ran with the official app
**open on Device details for the entire session**. This session deliberately ran with the app
**backgrounded** (Group AI's own procedure) — and shows a dramatically sparser cadence (2
occurrences in 37m39s vs. `CAP-036`'s several occurrences in ~7 minutes) **and** HFP's own
`AT+BIEV` dropping out of the sync entirely. This is consistent with — but not proven by, one
session — the push being **app-foreground-driven** (a periodic UI-refresh poll the app issues only
while its own screen is active) rather than a Buds-autonomous or link-supervision-level mechanism.
**Not claimed as resolved:** a session that toggles the app between foreground and background
mid-recording, with everything else held constant, would be needed to actually test this — flagged
as a recommended next capture, per `AGENTS.md` §13.6's zero-creativity rule (the data here is
consistent with the hypothesis, it does not establish it).

## 6. Cross-check against `CAP-027-FINDINGS.md`'s cross-sync-caveat (🟡 HYPOTHESIS)

`DESKRESEARCH_FINDINGS.md`'s 2026-09-04 round-2 entry (Result 2, `CAP-027`) found DLCI 0x08 firing
3 times with no HFP counterpart **during active A2DP streaming** — proposed as "streaming
specifically breaks the sync." **This session is fully idle (no streaming) and still shows HFP
dropping out of the cross-channel sync** (§3) — meaning the "sync breaks down" phenomenon is
**not** specific to active streaming after all; it also occurs at idle, at least for HFP's
participation. This narrows/revises `CAP-027`'s "streaming-specific" framing: the more general
reading supported by both sessions combined is that DLCI 0x02/0x04/0x08's own mutual sync is more
robust than HFP's participation in it, under multiple different conditions (streaming, idle
backgrounded) — HFP is the piece of the 4-mechanism sync that most easily decouples, not the group
as a whole.

## 7. Test-ID traceability

- **`OBS-002`** (this session's sole purpose): exercised for the full ~37m39s idle window. Result:
  cadence is far sparser than `CAP-036`'s short-session sample suggested (2 post-settling
  occurrences, ~16–19 minute gaps, not ~10–20s), and the previously-observed 4-channel
  near-lockstep sync (§12.5 there) narrows to a 3-channel (DLCI 0x02/0x04/0x08) sync once HFP drops
  out — see §3–§6.

## 8. Conclusions — confirmed this session vs. proposed, awaiting sign-off

**Confirmed by this session's own evidence (raw observation, not requiring promotion):**
- The `.log.last`/CAP-041-overlap correction (§0) — a factual, mechanically-verified finding, not
  an interpretive claim.
- The raw occurrence counts and timestamps for DLCI 0x02/0x04/0x08/HFP across the whole session
  (§2) — directly read off the wire.
- DLCI 0x02/0x04/0x08's mutual near-lockstep timing continuing to hold within each push cluster at
  this longer timescale (§3, first half) — a direct observation.

**Proposed, awaiting maintainer sign-off (do NOT treat as settled):**
- 🟡 That the push cadence is fundamentally sparser/slower than `CAP-036`'s sample suggested, and
  that HFP specifically drops out of the sync while DLCI 0x02/0x04/0x08 continue (§3–§4) — strong
  within this session, but n=2 for the gap measurement and single-session for the HFP-dropout
  observation.
- 🔴 The app-foreground-vs-backgrounded candidate explanation (§5) — flagged, not tested.
- 🟡 The revision to `CAP-027`'s "streaming-specific" sync-breakdown framing (§6).

Per `AGENTS.md` §6/§15, none of the above is committed to `PROTOCOL.md` as FACT and no
`DECISIONS.md` ADR is drafted here — these are proposals for maintainer review, to be applied
centrally alongside the other five captures in this batch.

## 9. Open Questions

- 🔴 Does the periodic DLCI 0x02/0x04/0x08 push cadence depend on the companion app being in the
  foreground (open on screen) vs. backgrounded? This session's sparse, HFP-absent cadence is
  consistent with a foreground-driven mechanism but does not establish it (§5).
- 🔴 What triggers the `Group 0x04 Code 0x12` alternating-value ping specifically (the only DLCI
  0x08 content that recurs in this session, twice per cluster, ~3.27s apart) when nothing else
  changes? `DECISIONS.md` ADR-016 finding 7 already characterizes this as "event-driven and
  autonomous" but not further — this session adds two more autonomous firings with no channel
  churn nearby, consistent with, not resolving, that existing open question.
- 🔴 Why does HFP's `AT+BIEV` fail to recur even once in 37m39s of a session where DLCI
  0x02/0x04/0x08 fire twice? (battery genuinely never changed vs. HFP's push mechanism being
  distinct from the other three's, per §3/§6)

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-042-2026-09-06_17-30-02_18-07-42-Group_AI/CAP-042-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-042-2026-09-06_17-30-02_18-07-42-Group_AI/CAP-042-FINDINGS
