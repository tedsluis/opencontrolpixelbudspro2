# Findings: `CAP-047` (Group AL — `CAP-021`'s DLCI 0x0a burst trigger, Trigger candidate 3 only)

Standardized, evidence-based extraction from `CAP-047-btsnoop_hci.log` (Recording 1),
`CAP-047-btsnoop_hci-2.log` (Recording 2), `CAP-047-btsnoop_hci.log.last` (resolved below as
out-of-scope), `CAP-047-recording.mp4`, and `CAP-047-recording-2.mp4`, staged here for later
promotion into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Every claim below carries a status per
`PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-047` · **Date:** 2026-09-14 · **Firmware:** `release_5.203` (confirmed by the
maintainer at session-commissioning time, per `ai-sessions/0022_CAPTURE_PROMPT_2026_09_15.md`
Context) · **Phone:** Pixel 7a, official Pixel Buds Companion App `1.0.955078536`, Android 17,
Google Play services active · **Buds MAC (partial, per `AGENTS.md` §7/§9):** `04:00:6e:cf:6e:07`.

---

## 0. Capture integrity and `.log.last` resolution

```
$ ls -la captures/CAP-047-2026-09-14_05-51-38_06-35-30-Group_AL/
CAP-047-btsnoop_hci-2.log        262364 bytes  mtime 06:38
CAP-047-btsnoop_hci.log          182785 bytes  mtime 06:13
CAP-047-btsnoop_hci.log.last      88274 bytes  mtime 05:51
CAP-047-recording.mp4         940897318 bytes  mtime 06:11  (ffprobe: 1203.45s)
CAP-047-recording-2.mp4       115345214 bytes  mtime 06:35  (ffprobe: 149.591s)
```

**Untruncated, both in-scope logs (🟢 FACT):**
```
$ capinfos CAP-047-btsnoop_hci.log | grep -E "Number of packets|Capture duration|Earliest|Latest"
Number of packets:   3,250       Capture duration: 1318.878132 seconds
Earliest: 2026-09-14 05:51:30.681771   Latest: 2026-09-14 06:13:29.559903
$ capinfos CAP-047-btsnoop_hci-2.log | grep -E "Number of packets|Capture duration|Earliest|Latest"
Number of packets:   4,716       Capture duration: 275.881191 seconds
Earliest: 2026-09-14 06:32:57.378679   Latest: 2026-09-14 06:37:33.259870
$ tshark -r CAP-047-btsnoop_hci.log   -T fields -e frame.number -e frame.cap_len -e frame.len | awk '$2!=$3{c++} END{print c+0}'
0
$ tshark -r CAP-047-btsnoop_hci-2.log -T fields -e frame.number -e frame.cap_len -e frame.len | awk '$2!=$3{c++} END{print c+0}'
0
```
Raw-path extraction, 0/3,250 and 0/4,716 `cap_len≠len` mismatches — no snaplen truncation in either
in-scope log.

**`.log.last` — leftover pre-session buffer, NOT in-scope evidence for this session (🟢 FACT),
mirroring the `CAP-040`/`CAP-042` precedent (`CAP-040-FINDINGS.md` §0), not the `CAP-049` one.**
```
$ capinfos CAP-047-btsnoop_hci.log.last | grep -E "Number of packets|Earliest|Latest"
Number of packets:   1,749
Earliest: 2026-09-14 05:49:51.676297   Latest: 2026-09-14 05:51:28.877915
$ tshark -r CAP-047-btsnoop_hci.log.last -T fields -e frame.number -e frame.time -e _ws.col.Info | head -1
1  2026-09-14T05:49:51.676297+0200  Sent Reset
$ tshark -r CAP-047-btsnoop_hci.log.last -T fields -e frame.number -e frame.time -e _ws.col.Info | tail -1
1749  2026-09-14T05:51:28.877915+0200  Rcvd Command Complete (Vendor Command 0x015E ...)
$ tshark -r CAP-047-btsnoop_hci.log.last -Y "bthci_evt.bd_addr==04:00:6e:cf:6e:07 and bthci_evt.code==0x03" -T fields -e frame.number -e frame.time -e bthci_evt.connection_handle
393  2026-09-14T05:49:56.449623+0200  0x0002
$ tshark -r CAP-047-btsnoop_hci.log.last -Y "bthci_evt.code==0x05" -T fields -e frame.number -e frame.time -e bthci_evt.connection_handle -e bthci_evt.reason
1413  2026-09-14T05:50:05.465904+0200  0x0004  0x16
1745  2026-09-14T05:51:28.768435+0200  0x0003  0x16
1746  2026-09-14T05:51:28.775597+0200  0x0002  0x16
$ tshark -r CAP-047-btsnoop_hci.log -T fields -e frame.number -e frame.time -e _ws.col.Info | head -1
1  2026-09-14T05:51:30.681771+0200  Sent Reset
$ tshark -r CAP-047-btsnoop_hci.log -Y "bthci_evt.bd_addr==04:00:6e:cf:6e:07 and bthci_evt.code==0x03" -T fields -e frame.number -e frame.time -e bthci_evt.connection_handle
611  2026-09-14T05:51:39.912667+0200  0x0002
```
`.log.last` is a **self-contained** connect→disconnect cycle: it opens with its own fresh `Sent
Reset` (frame 1, 05:49:51.676) — not a continuation of any earlier log on disk — includes its own
Buds Connection Complete (frame 393, 05:49:56.45, chandle `0x0002`) and ends with that same
chandle's own locally-terminated Disconnect Complete (frame 1746, 05:51:28.776, reason `0x16`),
97.2s after its own start. The **main log begins independently** with its own fresh `Sent Reset`
(frame 1, 05:51:30.682) — only 1.8s after `.log.last`'s last frame — and its own fresh Buds
Connection Complete (frame 611, 05:51:39.91) lands 11s after Recording 1's own documented start
(05:51:38, both Buds resting on the table, case closed, phone screen black/standby).

Applying the `CAP-040`/`CAP-042`/`CAP-049` precedent's own diagnostic (`CAP-049-FINDINGS.md` §0):
what determines whether `.log.last` is genuine intra-session rotation is not the size of the time
gap, but whether the *physical action that caused the boundary* is itself part of this session's own
documented procedure/video. Here it is not — `.log.last`'s own connect/disconnect cycle, and the
`Sent Reset` that opens the main log 1.8s later, both complete **before** Recording 1's own video
starts (05:51:38); nothing in `CAP-047-EVENT-NOTES.md`'s Event Timeline documents any Bluetooth
toggle or reconnect action before that point. This reads as the maintainer's own pre-session
logging-setup/verification activity (enabling HCI snoop logging per `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
§2, confirming it captures a Buds connect/disconnect, then starting the actual recorded session) —
**not** part of Group AL's own procedure. **`CAP-047-btsnoop_hci.log.last` is excluded from this
session's evidence base**; only `CAP-047-btsnoop_hci.log` (Recording 1) and
`CAP-047-btsnoop_hci-2.log` (Recording 2) are used below.

## 1. Video re-check — no corrected (matching-slot) docking in either recording (🟢 FACT)

**Context:** the maintainer recalled that, in Recording 2, after the buds were placed in the case
the wrong way round, they were immediately placed correctly. The existing
`CAP-047-EVENT-NOTES.md` (video-only pass, `ai-sessions/0018`) stated the opposite — no corrected
docking ever occurred. This session re-checked Recording 2's `~06:33:41`–`06:35:30` window at a
substantially higher density than the original pass.

**Method:** `ffmpeg -ss <t> -i CAP-047-recording-2.mp4 -vf fps=1 ...` across the full disputed
window (110 frames, 1 per second, `06:33:37`–`06:35:26` by the video's own burned-in wall-clock
overlay — a few seconds either side of the nominal `06:33:41`–`06:35:30` bound due to `-ss` seek
rounding, fully covering it), plus a denser `fps=4` pass (104 frames, 1 every 0.25s) across the
single busiest sub-window (`~06:34:47`–`06:35:13`, where swap attempt #3's two placements and two
removals are packed within ~25s). Every extracted frame carries the video's own burned-in wall-clock
overlay, used directly as ground truth (no independent time arithmetic trusted). Case-slot occupancy
was checked visually (an occupied slot shows a light gray/white bud body; an empty slot is a plain
black cavity — visually unambiguous once zoomed, confirmed by directly comparing a known
one-bud frame against a known two-bud frame, see below) and cross-validated against the phone
screen's own per-earbud charging-bolt indicator (Device details' Left/Case/Right circles) —
observed to correlate with the docked bud, at 1s and 0.25s granularity, throughout the whole
window.

**Result:** no frame anywhere in the re-checked window shows **both** Left and Right displaying the
charging-bolt icon simultaneously — the signature a genuine matching-slot docking would be expected
to produce (both electrical contacts aligned, both buds able to charge at once). Only ever one
icon (or neither) shows a bolt at a time, exactly matching the already-documented swapped-slot
sequence (case closed → swap attempt #2 at `06:33:41` → disconnect `06:33:57` → both removed
`06:34:13`–`15` → reconnect `06:34:16` → lid closed `06:34:37` → swap attempt #3 at `06:34:41`/`51`
→ disconnect `06:34:51` → bottom bud removed `06:34:59` → reconnect `06:35:00`/`01` → remaining bud
removed `06:35:12` → recording ends `06:35:30`, both slots empty). No case-visual frame at any
timestamp shows a bud configuration inconsistent with this already-documented timeline.

**`CAP-047-EVENT-NOTES.md`'s existing claim — "Proper (matching) slot corrected docking was never
performed during either recording" — is CONFIRMED, not contradicted, by this denser re-check.** The
maintainer's recollection of a corrected docking in Recording 2 is not supported by the video
evidence at either density tested. `CAP-047-EVENT-NOTES.md`'s Event Timeline, Analysis checklist,
and Status line have been updated accordingly (see that file's `06:35:30` row and Trigger-3 note).

**Bonus observation, flagged not asserted (🔴 OPEN QUESTION):** the per-earbud charging-icon
correlation described above shows a difference between the two swap attempts — during swap
attempt #2 (`06:33:41`–`57`), only the **Right** icon shows a charging bolt (window
`~06:33:53`–`56`, never Left); during swap attempt #3 (`06:34:41`–`58`), only the **Left** icon
shows a charging bolt (window `~06:34:42`–`58`, never Right). If slot identity maps consistently to
which icon lights up, this would imply the two swap attempts placed the *opposite* physical bud in
the top slot from each other — but swap attempt #2's own case-visual frames show the case being
actively lifted/tilted/handled by hand at the moment the Right bolt appears (`f_017.png`,
`06:33:53`), unlike swap attempt #3's static, table-resting framing, so a slot-identity inference
from this single frame is not reliable. Not resolved here — reported as an honest observation per
`AGENTS.md` §13.6, not force-fit into a specific bud-identity claim. Cross-referencing this against
the wire's own per-earbud data (§5 below) does not resolve it either, since DLCI 0x08's Option E
battery-triple mechanism (`PROTOCOL.md` §4.3) never fires in either in-scope log (checked, see §5).

## 2. Connection lifecycle and per-connection DLCI role mapping (🟢 FACT)

**Recording 1 (`CAP-047-btsnoop_hci.log`) — exactly one classic connection, never disconnects:**
```
$ tshark -r CAP-047-btsnoop_hci.log -Y "bthci_evt.code==0x03 or bthci_evt.code==0x05" \
  -T fields -e frame.number -e frame.time -e bthci_evt.code -e bthci_evt.connection_handle -e bthci_evt.reason -e bthci_evt.bd_addr
611  2026-09-14T05:51:39.912667+0200  0x03  0x0002    04:00:6e:cf:6e:07
```
One Connection Complete (frame 611, 05:51:39.91, chandle `0x0002`), **zero** Disconnection Complete
events anywhere in the remaining 21m50s of log — the classic link stays up continuously through the
entire recording, including the swapped-slot docking event at `06:10:21` (§4 below). DLCI roles this
session (confirmed by content signature, per `AGENTS.md` §13's CLI-hygiene rule):
`0x00`=mux, `0x02`=`libmaestro`, `0x04`=Fast Pair Message Stream, `0x08`=private envelope,
`0x09`=HFP, `0x0a`=silent (`CAP-021`'s burst channel).

**Recording 2 (`CAP-047-btsnoop_hci-2.log`) — three separate classic connections:**
```
$ tshark -r CAP-047-btsnoop_hci-2.log -Y "bthci_evt.code==0x03 or bthci_evt.code==0x05" \
  -T fields -e frame.number -e frame.time -e bthci_evt.code -e bthci_evt.connection_handle -e bthci_evt.reason -e bthci_evt.bd_addr
734   2026-09-14T06:33:08.633725+0200  0x03  0x0005            04:00:6e:cf:6e:07
2033  2026-09-14T06:33:59.193728+0200  0x05  0x0005  0x13
2130  2026-09-14T06:34:15.016601+0200  0x03  0x0001            04:00:6e:cf:6e:07
3443  2026-09-14T06:34:50.917076+0200  0x05  0x0001  0x13
3525  2026-09-14T06:35:00.437689+0200  0x03  0x0002            04:00:6e:cf:6e:07
```
Connection 1 (chandle `0x0005`, `06:33:08`–`59`) covers swap attempt #2 and its disconnect.
Connection 2 (chandle `0x0001`, `06:34:15`–`50`) covers the reconnect after full removal, lid-close,
and swap attempt #3's placements through its disconnect. Connection 3 (chandle `0x0002`,
`06:35:00`–end) covers the final reconnect and both remaining removals. Each connection gets its own
session-local DLCI set (`CAP-001-FINDINGS.md` §2, `DECISIONS.md` ADR-018); roles identified by
content signature (not by raw DLCI number, per `AGENTS.md` §13):

| Role | Connection 1 (`0x0005`) | Connection 2/3 (`0x0001`/`0x0002`) |
|---|---|---|
| Mux control | `0x00` | `0x00` |
| `libmaestro` (HDLC-framed, `"release_5.203"` strings) | `0x02` | `0x03` |
| Fast Pair Message Stream (`08 11`/`08 13` etc.) | `0x04` | `0x05` |
| HFP (`AT+` commands) | `0x09` | `0x08` |
| Private envelope (`05 0c`/`04 02`/... zero-length codes) | — (not opened, see below) | `0x09` |
| **Silent / `CAP-021`'s burst channel** | **`0x0a`** | **`0x0b`** |

Confirmed by payload-count-per-DLCI, e.g. for Connection 2:
```
$ tshark -r CAP-047-btsnoop_hci-2.log -Y "bthci_acl.chandle==0x0001 and btrfcomm.len>0" -T fields -e btrfcomm.dlci | sort | uniq -c
     60 0x00
    140 0x03
     53 0x05
     48 0x08
     53 0x09
```
`0x0b` (opened per the SABM table in §3) is absent from this list — zero payload, confirming its
role as the silent channel for this connection, mirroring `0x0a`'s role in Connection 1 and in every
other capture on file (`PROTOCOL.md` §6). Connection 1's own private-envelope channel (`0x08`role
in the table above) is a genuine role, not an omission — it was checked and does carry content
(the connect-time zero-length-code burst, `PROTOCOL.md` §6's `PRIV-001` item); it is left out of
this write-up's frame citations only because it is not this session's own subject.

## 3. Group AL hypothesis test — Trigger candidate 3 (charge-state change) (🟢 FACT, negative result)

Per `PROJECT_RULES.md` §4's fixed template.

- **Hypothesis:** `CAP-021`'s 1123-frame DLCI 0x0a payload burst (`CAP-021-FINDINGS.md` §4a,
  `PROTOCOL.md` §6) is triggered by a charge-state change — docking one or both Buds into the case
  (charging begins) or removing them (charging stops).
- **Setup:** `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AL, Trigger candidate 3 only (candidates 1 and 2
  — app backgrounded/foregrounded, a scheduled idle sync window — were not attempted this session,
  per `CAP-047-EVENT-NOTES.md`'s own Method-choice line). This session incidentally exercised **six**
  distinct charge-state transitions across the two recordings, all bracketed on video and correlated
  against the wire log: (1) Recording 1's swapped-slot dock at `06:10:21` (both seated); (2) Recording
  1's undock at `06:10:46`–`50` (both removed); (3) Recording 2 swap #2's dock at `06:33:41`–`51`
  (both seated); (4) Recording 2's undock at `06:34:13`–`15` (both removed); (5) Recording 2 swap
  #3's dock at `06:34:41`–`51` (both seated, in two separate placements); (6) Recording 2's
  progressive undock at `06:34:59` (one removed) and `06:35:12` (the other removed). This is a
  substantially richer bracket set than the Group AL skeleton's own minimum ask (a single dock or
  undock transition).
- **Expected outcome, if the hypothesis holds:** a DLCI 0x0a-role payload burst (any size, not just
  1123 frames) appears at or shortly after one or more of the six transitions above.
- **Actual outcome — clean, complete negative, zero payload frames on the silent channel across
  either entire in-scope log, at any charge-state transition tested:**
  ```
  $ tshark -r CAP-047-btsnoop_hci.log -Y "btrfcomm.dlci==0x0a and btrfcomm.len>0" -T fields -e frame.number | wc -l
  0
  $ tshark -r CAP-047-btsnoop_hci-2.log -Y "btrfcomm.dlci==0x0a and btrfcomm.len>0" -T fields -e frame.number | wc -l
  0
  $ tshark -r CAP-047-btsnoop_hci-2.log -Y "btrfcomm.dlci==0x0b and btrfcomm.len>0" -T fields -e frame.number | wc -l
  0
  ```
  This is not a spot-check around the dock moments only — it is the complete DLCI 0x0a/0x0b payload
  count across each log's **entire** duration (Recording 1: 1318.88s; Recording 2: 275.88s), per the
  maintainer's own "no sampling" instruction. Every frame the silent channel carries in either log is
  a channel-control frame (`SABM`/`UA`/`DISC`), listed in full:
  ```
  $ tshark -r CAP-047-btsnoop_hci.log -Y "btrfcomm.dlci==0x0a" -T fields -e frame.number -e frame.time -e btrfcomm.frame_type -e frame.p2p_dir
  1235  2026-09-14T05:51:46.226670+0200  0x2f  0
  1241  2026-09-14T05:51:46.304451+0200  0x63  1
  $ tshark -r CAP-047-btsnoop_hci-2.log -Y "btrfcomm.dlci==0x0a" -T fields -e frame.number -e frame.time -e btrfcomm.frame_type -e frame.p2p_dir
  905   2026-09-14T06:33:12.096505+0200  0x2f  0
  920   2026-09-14T06:33:12.224862+0200  0x63  1
  1857  2026-09-14T06:33:55.494977+0200  0x43  0
  1862  2026-09-14T06:33:55.556142+0200  0x63  1
  1889  2026-09-14T06:33:56.549805+0200  0x2f  0
  1896  2026-09-14T06:33:56.755678+0200  0x63  1
  $ tshark -r CAP-047-btsnoop_hci-2.log -Y "btrfcomm.dlci==0x0b" -T fields -e frame.number -e frame.time -e btrfcomm.frame_type -e frame.p2p_dir
  2365  2026-09-14T06:34:15.617978+0200  0x2f  0
  2377  2026-09-14T06:34:15.643459+0200  0x63  1
  2492  2026-09-14T06:34:17.576015+0200  0x43  0
  2499  2026-09-14T06:34:17.640734+0200  0x63  1
  2582  2026-09-14T06:34:18.660359+0200  0x2f  0
  2591  2026-09-14T06:34:18.755113+0200  0x63  1
  3104  2026-09-14T06:34:41.951686+0200  0x43  0
  3111  2026-09-14T06:34:42.058717+0200  0x63  1
  3145  2026-09-14T06:34:43.146748+0200  0x2f  0
  3151  2026-09-14T06:34:43.272384+0200  0x63  1
  3830  2026-09-14T06:35:01.046306+0200  0x2f  0
  3841  2026-09-14T06:35:01.059527+0200  0x63  1
  ```
  (`0x2f`=SABM, `0x63`=UA, `0x43`=DISC.) Notably, the silent channel itself undergoes **RFCOMM
  channel-bounces** (a `DISC`→`UA`→fresh `SABM`→`UA` cycle, the same still-unexplained phenomenon
  `PROTOCOL.md` §6/`CAP-016-FINDINGS.md` §3/§9 already documents) at `06:33:55`–`56` — inside
  Connection 1, ~2s before that connection's own ACL disconnect — and twice more inside Connection 2,
  at `06:34:17`–`18` and `06:34:41`–`43` (the latter landing almost exactly at swap attempt #3's own
  lid-reopen/first-placement moment, `06:34:41`). **Even these channel-bounce reopens carry no
  payload** — the burst is not merely absent between dock events, it is absent even when the channel
  itself is freshly re-established at a moment adjacent to a charge-state change.
- **Conclusion:** Trigger candidate 3 (charge-state change) does **not** reproduce `CAP-021`'s DLCI
  0x0a burst, across six independently bracketed charge-state transitions in a single session
  (Recording 1: 2 transitions; Recording 2: 4 transitions across 3 separate connections) — a clean,
  complete negative, not an inconclusive or failed result. This raises the "1 of N sessions checked"
  denominator from `CAP-021-FINDINGS.md`/`PROTOCOL.md` §6's existing count. Combined with the
  fifteen-plus prior sessions already checked (`CAP-001`/`CAP-002`/`CAP-005`–`CAP-008`/`CAP-011`/
  `CAP-016`/`CAP-019`/`CAP-020`/`CAP-022`–`CAP-025`, per `CAP-021-FINDINGS.md` §4a and
  `CAP-008-FINDINGS.md` §6) and the still-open Trigger candidates 1/2 (not attempted this session —
  see §8), the burst remains attributable to exactly **one** capture (`CAP-021`) out of what is now
  at least **twenty** independent sessions checked for DLCI 0x0a payload. Trigger candidate 3 is
  **ruled out** as an explanation for `CAP-021`'s burst; Trigger candidates 1 and 2 remain untested,
  open follow-up work (not silently dropped — see §8/§9).

## 4. Bonus finding A — Video 1's swapped-slot dock never disconnects; Video 2's do (🟢 FACT, unreconciled with `DECISIONS.md` ADR-016)

Context's own bonus lead #1: "Video 1's swapped-slot dock at `06:10:21` — the phone screen kept
showing 'connected' (no disconnect), unlike video 2's swapped-slot dock at `06:33:57`, which did
show a disconnect." Confirmed on the wire as a **genuine behavioral difference, not an on-screen-label
artifact:**

- Recording 1: **zero** Disconnection Complete events anywhere in the log (§2 above) — the single
  classic connection (chandle `0x0002`, frame 611) stays up continuously from `05:51:39.91` through
  the log's own end at `06:13:29.56`, spanning the entire `06:10:15`–`06:10:50` swapped-dock/undock
  cycle.
- Recording 2: **two** genuine ACL Disconnection Complete events, reason `0x13` (remote user
  terminated — i.e. Buds-initiated, matching `DECISIONS.md` ADR-016's own "Disconnect-on-redock"
  mechanism), at `06:33:59.19` (frame 2033, ~2s after swap #2's both-docked moment) and
  `06:34:50.92` (frame 3443, essentially simultaneous with swap #3's second placement at `06:34:51`).

`DECISIONS.md` ADR-016 (`PROTOCOL.md` §5.1/§7) documents disconnect-on-redock as firing "the
instant the *second* bud is placed in the case" — with no qualification for slot correctness, since
its own evidence base never included a swapped-slot seating. This session's own dock-state-byte
evidence (§5 below) confirms the physical-presence sensor reads "both docked" identically in **all
three** swapped-dock instances (`06:10:20.78`, and both of Recording 2's) — yet only two of the
three produce a disconnect. **🔴 OPEN QUESTION, not resolved here:** why does Recording 1's
swapped dock (`06:10:21`) not disconnect, when both Recording 2 swaps do, given the dock-sensor
itself reports the identical "both docked" state in all three? No video-visible or wire-visible
difference between the three docking actions themselves was found that would explain this
(all three are direct physical placements, not the CAP-016-style "both removed then case closed"
sequence ADR-016's own evidence was originally built from) — reported plainly, not force-fit into a
guessed mechanism, per `AGENTS.md` §13.6.

## 5. Bonus finding B — `DECISIONS.md` ADR-024's dock-state byte during a swapped-slot seating (🟢 FACT for the primary question; new counter-examples, 🔴 OPEN QUESTION)

Context's own bonus lead #2 asked whether ADR-024's `Settable-toggles` dock-state byte (DLCI 0x04)
reads "both docked" during a swapped-slot seating — a case its own evidence base never tested.

**Primary result — 🟢 FACT: yes, it reads `0x00` ("both docked") during a swapped-slot seating,
exactly as it does for a correctly-slotted one.** The dock-state sensor mechanism does not
distinguish which physical slot each bud occupies, only physical presence:
```
$ tshark -r CAP-047-btsnoop_hci.log -Y "btrfcomm.dlci==0x04 and btrfcomm.len>0" -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data | grep 0813
1048  2026-09-14T05:51:44.201013+0200  1  0813000401e8e880
2803  2026-09-14T06:10:20.777168+0200  1  0813000401e80020
```
Frame 2803, `08 13 00 04 01 e8 00 20` — Group `0x08` Code `0x13` (Notify), Version `0x01`, UI
toggles `0xe8`, **Settable-toggles `0x00`**, Current state `0x20` (Off) — lands at `06:10:20.78`,
essentially simultaneous with the video's own documented `06:10:21` "both buds are inside the case
in swapped configuration" moment. (Frame 1048, at Recording 1's own start with both Buds resting
outside, correctly reads `0xe8`.)

**Secondary result — two new, unreconciled counter-examples to ADR-024 in Recording 2, additional to
the two `CAP-048-FINDINGS.md` §5 already documents, of the same general character (a fresh
channel-(re)establishment's Get/Notify returning a stale value):**
```
$ tshark -r CAP-047-btsnoop_hci-2.log -Y "btrfcomm.dlci==0x05 and btrfcomm.len>0" -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data | grep 0813
2673  2026-09-14T06:34:19.721882+0200  1  0813000401e8e880
3048  2026-09-14T06:34:39.508158+0200  1  0813000401e80020
3364  2026-09-14T06:34:46.790989+0200  1  0813000401e80020
3834  2026-09-14T06:35:01.048349+0200  1  0813000401e80020
3996  2026-09-14T06:35:02.195231+0200  1  0813000401e8e880
4339  2026-09-14T06:35:15.170599+0200  1  0813000401e8e840
```
- Frame 2673 (`06:34:19.72`, Settable=`0xe8`) — correct: both buds are outside (removed `06:34:13`–15).
- **Frame 3364 (`06:34:46.79`, Settable=`0x00`) — counter-example.** This Notify follows a DLCI
  0x05 channel-bounce reopen at `06:34:45.78`–`45.92` (SABM/UA, immediately preceding it). Per
  §1's video re-check, at `06:34:46` only **one** bud (top slot) is seated — the second placement
  does not happen until `06:34:51` — yet this reading claims both docked. Matches the
  "post-reopen settling" pattern `CAP-048-FINDINGS.md` §5 already proposed as 🟡 HYPOTHESIS
  (a fresh reconnect's own Get/Notify may return a value queried before firmware has settled on an
  already-changed physical state) — this instance follows a channel-level reopen, not a full ACL
  reconnect, extending that hypothesis's applicable trigger if it holds.
- **Frame 3834 (`06:35:01.05`, Settable=`0x00`) — counter-example, immediately self-corrected.** This
  is Connection 3's own connect-time Get/Notify (fresh ACL reconnect, chandle `0x0002`, at
  `06:35:00.44`). Per §1's video re-check, at `06:35:01` only **one** bud (top slot) is seated — the
  bottom-slot bud was removed at `06:34:59`, and the reconnect itself is triggered by that same
  removal (the Right bud, now outside, re-establishing its own connection) — yet this reading claims
  both docked. **Frame 3996, 1.15s later** (`06:35:02.20`, Settable=`0xe8`, a spontaneous re-Notify
  with no intervening Get) **reads correctly** — directly reproducing `CAP-048-FINDINGS.md` §5's own
  proposed settling mechanism with a second, independent instance in a different session.
- **Frame 3048 (`06:34:39.51`, Settable=`0x00`) — counter-example, mechanism unclear.** Unlike the
  two above, this Notify is **not** preceded by any channel reopen — DLCI 0x05 had been open
  continuously since `06:34:19` — nor by a visible `08 11` Get; it appears to be a spontaneous,
  Buds-initiated Notify. Per §1's video re-check, the case is empty at this time (lid closed on an
  empty case at `06:34:37`, first placement not until `06:34:41`) — yet this reading claims both
  docked. It lands 2.5s after the video's documented lid-close action, and is followed 0.4s later
  (`06:34:39.92`) by the DLCI 0x05 channel-bounce already noted in §3. 🔴 **OPEN QUESTION, not
  resolved:** whether this spontaneous Notify and the immediately-following channel bounce share a
  common cause, and what (if anything) about a lid-close-on-an-empty-case action would produce a
  stale "both docked" reading — this instance does not fit the "settling after a fresh
  (re)establishment" pattern the other two counter-examples do, since no (re)establishment precedes
  it. Reported as observed, not guessed at.
- Frame 4339 (`06:35:15.17`, Settable=`0xe8`) — correct: both buds removed by `06:35:12`.

None of this contradicts ADR-024's own core finding (dock-state-vs-not, confirmed correct in the
large majority of readings, including every genuinely-both-docked and genuinely-neither-docked
sample in this session) — it adds three more single-session counter-examples (one video-time-clean,
two with a plausible settling explanation, one without) to the same still-open reliability question
`CAP-048-FINDINGS.md` §5 already flagged, now reproduced independently a second time.

## 6. Cross-checks

- **Against `CAP-021-FINDINGS.md` §4a / `PROTOCOL.md` §6's DLCI 0x0a item:** this session's result
  (zero payload across six bracketed charge-state transitions, two full in-scope logs) is
  **consistent with** the existing characterization — the burst remains a one-off, not reproduced
  by this session's own trigger. No contradiction.
- **Against `CAP-008-FINDINGS.md` §5/§6 (SCO/eSCO ruled out as the burst's content):** unaffected —
  this session involved no phone call, and adds no new evidence either way on that already-closed
  question.
- **Against `DECISIONS.md` ADR-016 (disconnect-on-redock):** §4 above is a genuine, flagged tension
  — not contradicted outright (2 of 3 swapped docks do disconnect, matching ADR-016's own mechanism),
  but the one exception is unreconciled and worth the maintainer's attention.
- **Against `DECISIONS.md` ADR-024 (dock-state byte):** §5 above both **confirms** the core finding
  (swapped-slot seating still reads "both docked," directly answering the Context's own bonus
  question) and **extends** the still-open reliability caveat `CAP-048-FINDINGS.md` §5 already
  recorded, with two new instances of a similar character and one without an obvious explanation.
- **Against `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Group AL section:** all three documented trigger
  candidates are correctly reflected — this session ran only candidate 3, and the section already
  anticipated exactly this "up to 3 separate bracketed sub-sessions... the maintainer's choice at
  execution time" structure.
- **Against `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`:** re-confirmed no existing Test-ID covers this
  bracketed-trigger question (checked directly, full-file read, `ai-sessions/0022` Phase 0) — see §9
  for the still-standing proposal to add one.

## 7. External validation

No new opcode, tag-shape, or byte-level decode was produced this session (the burst did not
recur, so there is nothing new to validate against a public standard). `CAP-021-FINDINGS.md` §4a's
existing protobuf tag-shape observation (`0a d0 01` = field 1, length 208) is unaffected and not
re-validated here, since this session adds no further sample of it. The RFCOMM channel-bounce
mechanism referenced in §3/§4 remains, as before, an internally-observed, not-externally-documented
phenomenon (`PROTOCOL.md` §6) — no new external source was consulted this session, since nothing new
about its *mechanism* (as opposed to its recurrence) was found.

## 8. Test-ID traceability (`AGENTS.md` §13 item 7)

Group AL has no existing assigned Test-ID (confirmed §6/`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` above) —
this write-up does not imply one exists. Trigger candidates 1 (app backgrounded/foregrounded) and 2
(scheduled sync window) remain **untested, open follow-up work**, not silently dropped — see §9's
proposal.

## 9. Conclusions and proposed downstream updates — ⏳ awaiting maintainer sign-off for every proposed item

**Confirmed this session (no sign-off needed — negative results and direct observations, not FACT
promotions of new protocol semantics):**
- Trigger candidate 3 (charge-state change) does not reproduce `CAP-021`'s DLCI 0x0a burst, across
  six bracketed transitions, two full untruncated logs, zero sampling (§3).
- The maintainer's recalled corrected docking in Recording 2 is not supported by a dense
  frame-level + charging-icon re-check (§1); `CAP-047-EVENT-NOTES.md` updated accordingly.
- `.log.last` is leftover pre-session content, excluded from evidence (§0).
- ADR-024's dock-state byte reads "both docked" during a swapped-slot seating (§5) — a direct,
  video-time-correlated answer to a previously untested case; this is additional evidence for an
  *already-FACT* finding (the byte's presence/absence semantics), not a new promotion.

**Proposed for maintainer review (not committed to `PROTOCOL.md`/`DECISIONS.md` by this session, per
`AGENTS.md` §6):**
1. Add a dated update to `PROTOCOL.md` §6's DLCI 0x0a open item recording this session's negative
   result and the raised session count (§3).
2. Add a new `PROTOCOL.md` §6 (or a `DECISIONS.md` ADR-016 update) item recording §4's unreconciled
   Video-1-vs-Video-2 disconnect-behavior difference during a swapped-slot dock.
3. Add a dated update to `DECISIONS.md` ADR-024 recording §5's two new counter-example readings,
   alongside `CAP-048-FINDINGS.md` §5's existing ones. **Done 2026-09-18** — `DECISIONS.md` ADR-024
   gained a dated update recording all three of §5's counter-example frames (3364, 3834→3996, 3048),
   maintainer-approved in a chat session continuing `ai-sessions/0031`/`0032`.
4. Propose a new Test-ID (e.g. `CASE-009` or similar, area prefix `CASE`) in
   `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` for "swapped-slot (mismatched L/R) docking" as its own
   bracketed behavior, given this session establishes it as a distinct, reproducible scenario with
   its own (still partly unreconciled) wire behavior — currently tracked only informally via Group
   AL's own purpose statement.
5. Trigger candidates 1 (app backgrounded/foregrounded) and 2 (scheduled sync window) remain
   open, undesigned follow-up work for a future Group AL sub-session.

## 10. Open Questions

- Why does Recording 1's swapped-slot dock (`06:10:21`) never trigger a disconnect, when both of
  Recording 2's swapped-slot docks do, given the dock-sensor byte reads identically "both docked" in
  all three (§4)? No video- or wire-visible procedural difference found.
- What causes frame 3048's spontaneous "both docked" Notify (`06:34:39.51`) on an empty case, not
  preceded by any channel reopen (§5)? Possibly related to the immediately-following channel
  bounce (`06:34:39.92`), possibly not — not established.
- The bonus charging-icon asymmetry between swap #2 (Right bolt only) and swap #3 (Left bolt only)
  remains unresolved — not confidently attributable to slot identity given swap #2's own frames show
  the case being actively handled (§1).
- Trigger candidates 1 and 2 for the original Group AL question remain untested (§8/§9).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-047-2026-09-14_05-51-38_06-35-30-Group_AL/CAP-047-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-047-2026-09-14_05-51-38_06-35-30-Group_AL/CAP-047-FINDINGS
