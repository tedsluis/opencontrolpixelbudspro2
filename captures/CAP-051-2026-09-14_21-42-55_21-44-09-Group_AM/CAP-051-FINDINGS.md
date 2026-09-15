# Findings: `CAP-051` (Group AM — `qhr` field 13 ANC-parallel-path wire confirmation)

Standardized, evidence-based extraction from `CAP-051-btsnoop_hci.log` + `CAP-051-recording.mp4`,
staged here for later promotion into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-051` · **Date:** 2026-09-14 · **Firmware:** ⚪ ASSUMPTION `release_5.203`
(carried over, not re-confirmed on-screen this session) · **Phone:** Pixel 7a, official Pixel Buds
Companion App · **Log file:** `CAP-051-btsnoop_hci.log` (209.66s, 2,457 packets, 0/2,457
`cap_len≠len` mismatches — untruncated, raw path; earliest packet 21:42:48.042, latest 21:46:17.706 —
extends both before and after the video's own 21:42:55–21:44:09 window) · **Video:**
`CAP-051-recording.mp4`, `ffprobe` duration 73.758s · **Buds MAC (partial, per `AGENTS.md` §7/§9):**
`04:00:6e:cf:6e:07`.

**Only one video and one log exist for this capture** (confirmed via `ls -la`, per this session's own
Phase 0 file inventory) — no `.log.last` exists, so Step G does not apply.

---

## 0. Capture integrity (🟢 FACT)

```
$ capinfos CAP-051-btsnoop_hci.log
Number of packets:   2,457       Capture duration: 209.664588 s
Earliest packet time: 2026-09-14 21:42:48.041587   Latest: 2026-09-14 21:46:17.706175
$ tshark -r CAP-051-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3{c++} END{print "mismatches:", c+0}'
mismatches: 0
```
No snaplen cap, 0/2,457 mismatches — untruncated, raw path. **This directly confirms
`CAP-051-EVENT-NOTES.md`'s own Log Metadata cell, which the video-only pass explicitly could not
verify** ("whether raw-path/non-`btsnooz.py` logging was actually used is a log-file property this
video-only session cannot confirm").

## 1. Connection topology and channel identification (🟢 FACT)

```
$ tshark -r CAP-051-btsnoop_hci.log -Y "bthci_evt.bd_addr==04:00:6e:cf:6e:07 and bthci_evt.code==0x03" \
  -T fields -e frame.number -e frame.time -e bthci_evt.connection_handle
786   2026-09-14T21:43:07.656912  0x0002
```
Exactly **one** classic ACL connection this whole session (chandle `0x0002`), matching the single
Bluetooth-toggle-on action shown on video (~21:43:01–06). No reconnect occurs at any point —
unlike `CAP-050`, there is no session-local DLCI-reassignment risk to check here (only one connection
instance exists), but the channel identities were still verified by content, not assumed from a fixed
DLCI number, per this project's own established practice (`CAP-001-FINDINGS.md` §2):

- **DLCI `0x02` = `libmaestro`'s Pigweed `pw_hdlc` channel** — confirmed via the `0x7E` HDLC flag byte
  framing every payload (e.g. frame 1163: `7e004b0310151d05d8d5...7e`), per `PROTOCOL.md` §2.2a.
- **DLCI `0x04` = the official Fast Pair Message Stream** — confirmed via Device Information content
  (frame 1070: `...4a0309000a5265766973696f6e2036...` = Group `0x03` Code `0x09`, `"Revision 6"`) and
  the already-FACT ANC Get/Notify shape (frame 1072: `08 11 00 00`), per `PROTOCOL.md` §2.1/§4.1.

## 2. The four target actions, cross-checked against DLCI 0x04's already-confirmed ANC path (🟢 FACT)

```
$ tshark -r CAP-051-btsnoop_hci.log -Y "btrfcomm.dlci==4 and btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data
```

| Frame | Wire time | Dir | Raw hex | Decoded |
|---|---|---|---|---|
| 1093 | 21:43:11.971 | Rcvd | `08 13 00 04 01 e8 e8 40` | Notify, Current=`0x40`=**Adaptive** (pre-existing state at connect, matches `CAP-051-EVENT-NOTES.md`'s "Adaptive already selected") |
| **1527** | **21:43:28.107** | **Sent** | `08 12 00 14 01 e8 e8 80 <16 reserved bytes>` | **Set ANC state, new_mode=`0x80`=Transparency** |
| 1529 | 21:43:28.219 | Rcvd | `ff 01 00 06 08 12 01 e8 e8 80` | ACK for the Set above |
| 1530 | 21:43:28.299 | Rcvd | `08 13 00 04 01 e8 e8 80` | Notify, Current=`0x80`=Transparency |
| **1707** | **21:43:43.336** | Rcvd | `08 13 00 04 01 e8 e8 08` | Notify, Current=`0x08`=**Noise cancellation** — **no `08 12` Set and no `08 11` Get anywhere nearby** |
| **1754** | **21:43:56.002** | Rcvd | `08 13 00 04 01 e8 e8 40` | Notify, Current=`0x40`=**Adaptive** — again no Set/Get nearby |
| **1801** | **21:44:05.102** | Rcvd | `08 13 00 04 01 e8 e8 80` | Notify, Current=`0x80`=**Transparency** — again no Set/Get nearby |

```
$ tshark -r CAP-051-btsnoop_hci.log -Y "btrfcomm.dlci==4 and data.data contains 08:12" \
  -T fields -e frame.number
1527
```
**Exactly one `08 12` "Set ANC state" frame exists in the entire log — frame 1527, the in-app tap.**

**Mapping to `CAP-051-EVENT-NOTES.md`'s Event Timeline, four-for-four, in the right sequence, each
within ~1–2s of the video-observed action (well inside this project's own established video/wire
tolerance, `PROTOCOL.md` §4.1):**

| Action (per `CAP-051-EVENT-NOTES.md`) | Video timestamp | Wire frame/time | Mode transition |
|---|---|---|---|
| In-app tap | tap ~21:43:26, confirmed by 21:43:27 | frame 1527, 21:43:28.107 (`Set`) | Adaptive→**Transparency** |
| Physical gesture #1 | contact 21:43:38–42, confirmed by 21:43:42 | frame 1707, 21:43:43.336 (`Notify` only) | Transparency→**Noise cancellation** |
| Physical gesture #2 | contact ~21:43:51/52–54, confirmed by 21:43:55 | frame 1754, 21:43:56.002 (`Notify` only) | Noise cancellation→**Adaptive** |
| Physical gesture #3 | contact ~21:44:02–03, confirmed by 21:44:04 | frame 1801, 21:44:05.102 (`Notify` only) | Adaptive→**Transparency** |

**This is exactly `CAP-038-FINDINGS.md` §5's already-documented "Notify without Set" pattern,
reproduced here for the first time with a positively-identified, video-confirmed trigger**: the
in-app tap produces a genuine `Set`(`0x12`)+ACK+`Notify` sequence on DLCI 0x04 (the confirmed,
official path, `PROTOCOL.md` §4.1), while all three physical press-and-hold gestures produce only a
spontaneous `Notify`(`0x13`) with no preceding `Set` or `Get` anywhere in the log — matching
`CAP-027-FINDINGS.md` §4's established mechanism (the Buds decide the mode change on-device and only
report it out). **This resolves `CAP-038-FINDINGS.md` §5's own open item as a confirmed positive**:
its "plausibly a hardware press-and-hold gesture" reading (offered without video evidence, since that
session's camera was aimed at the phone screen) is now directly confirmed by a session that *does*
have video of the physical gesture.

## 3. Central `PRIV`/Group AM question: does either action write `qhr` field 13 on DLCI 0x02? — **clean, confirmed negative for all four actions** (🟢 FACT)

```
$ tshark -r CAP-051-btsnoop_hci.log -Y "btrfcomm.dlci==2 and frame.time >= \"2026-09-14 21:43:20\" and frame.time <= \"2026-09-14 21:44:10\"" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.len -e data.data
```
This window covers all four target actions (tap at `21:43:28.107` through gesture #3's Notify at
`21:44:05.102`) with margin on both sides. **30 frames total, but every `Sent`-direction (`p2p_dir==0`)
frame in this window has `btrfcomm.len==0`** — i.e. every phone→Buds frame here is an empty RFCOMM
acknowledgement, not a data-carrying UIH frame:

```
1520  21:43:23.250  Sent  len=0
1543  21:43:28.945  Sent  len=0
1566  21:43:33.094  Sent  len=0
1718  21:43:43.481  Sent  len=0
1724  21:43:44.026  Sent  len=0
1761  21:43:56.073  Sent  len=0
1768  21:43:58.069  Sent  len=0
1797  21:44:03.090  Sent  len=0
1811  21:44:06.076  Sent  len=0
```
**The only non-empty traffic in this entire window is `Rcvd`-direction** (Buds→phone) — the
already-documented periodic push (`PROTOCOL.md` §4.3 Option B's cross-channel-sync entry,
`CAP-036-FINDINGS.md` §12.5), e.g. frame 1565 (`21:43:33.092`):
```
7e00a5032a1f10e2f3c88b8a341800320c1204086410011a04086410013a06080010001800080710151dea71de7d5e2590821ee64863cfb07e
```
— recurring every ~10s (`1517`/`1565`/`1711`/`1796` at `21:43:23`/`33`/`43`/`44:03`), not correlated
with any of the four actions' own timing, consistent with `CAP-036-FINDINGS.md` §12.5's existing
"periodic, not event-triggered" characterization.

**No `field5{field4{field13=N}}` write, or any other `Sent`-direction payload of any kind, appears on
DLCI 0x02 at or near any of the four actions.** This holds for:
- the in-app tap (`21:43:28.107`) — **confirmed absence**;
- physical gesture #1 (`21:43:43.336`) — **confirmed absence**;
- physical gesture #2 (`21:43:56.002`) — **confirmed absence**;
- physical gesture #3 (`21:44:05.102`) — **confirmed absence**.

**The isolation-window caveat `CAP-051-EVENT-NOTES.md` flagged (gestures #2/#3 falling short of the
planned ≥10s gap) does not weaken this result**: a confirmed absence does not depend on attributing a
positive hit to one specific gesture among several closely-spaced ones — there is no hit to attribute,
in any of the windows, closely-spaced or not.

**What this does and does not establish, stated precisely per `AGENTS.md` §13.6:** this is a clean
negative for *this session*, on *this app version* (`1.0.955078536`), for *both* code-side triggers
`REVERSE_ENGINEERING.md`'s `qhr` entry names (`QuickActionsFragment`'s in-app tap and the physical
press-and-hold `gvi`/`gvj` path). It does **not** contradict that static-analysis finding — a
compiled, reachable code path existing is a fact about the app's binary, independent of whether this
one session happened to exercise it. Two candidate readings, neither confirmed by this session alone,
per `AGENTS.md` §13.6's zero-creativity rule (no forced resolution offered):
- the in-app tap in this specific app version routes through DLCI 0x04's official path only (the
  `08 12` Set observed directly, §2), and `fye.a(qhs)`'s DLCI-0x02 `qhr` field-13 write path is either
  dead code in this build, or reachable from a different UI element than the one exercised here;
- the physical press-and-hold gesture, as directly confirmed by `CAP-027-FINDINGS.md` §4 and this
  session's own §2, drives the mode change through firmware/on-device logic that reports out via DLCI
  0x04's Notify alone — with no accompanying phone-side DLCI 0x02 write in either direction, contrary
  to the `gvi`/`gvj` static-analysis path's own premise that this specific app-side callback fires on
  a `HOLD` gesture event.

## 4. Test-ID traceability (`AGENTS.md` §13)

- **`ANC`-family / `TOUCH-007`**: all four transitions individually confirmed and time-correlated
  against DLCI 0x04's already-FACT ANC path (§2) — the in-app tap is the first video-confirmed,
  wire-correlated `Set` frame for a `QuickActionsFragment` tap specifically (as opposed to the
  Device-details-screen ANC row used by `CAP-001`/`CAP-006`); `TOUCH-007` gains a third confirming
  session (after `CAP-027`) with, for the first time, both physical-gesture video *and* a positively
  matched "Notify without Set" wire signature in the same session.
- **This Group's own primary question (no assigned Test-ID, per `CAP-051-EVENT-NOTES.md`'s own
  Purpose note)**: fully exercised — see §3.

## 5. Conclusions & proposed downstream updates — awaiting maintainer sign-off for proposed items

**Recorded as this session's own factual result (no sign-off needed, per `AGENTS.md` §15 — direct
observations, not FACT promotions to `PROTOCOL.md`):**
- The log is confirmed raw-path/untruncated (§0), settling `CAP-051-EVENT-NOTES.md`'s own
  unconfirmed cell.
- All four ANC-mode actions confirmed byte-for-byte against DLCI 0x04's already-FACT path, in
  sequence, with sub-2s video/wire correlation (§2).
- The physical-gesture "Notify without Set" pattern is directly, video-confirmedly reproduced (§2),
  resolving `CAP-038-FINDINGS.md` §5's own open item as a confirmed positive.
- **Clean, confirmed negative for `qhr` field 13 on DLCI 0x02, for all four actions** (§3) — no
  candidate write found for either the in-app tap or any of the three physical gestures.

**Proposed only, NOT committed — awaiting explicit maintainer sign-off per `AGENTS.md` §6/§15:**
1. `PROTOCOL.md` §6 — update `CAP-038-FINDINGS.md` §5's open item (two Get-less/Set-less ANC Notify
   frames) to record that a dedicated, video-confirmed repeat (this capture) directly reproduces and
   confirms the "physical press-and-hold gesture" explanation that item could previously only offer
   as unconfirmed 🟡 HYPOTHESIS.
2. `PROTOCOL.md` §6 — add a new item recording this session's own clean negative for a DLCI 0x02
   `qhr` field-13 write, for both the in-app tap and physical press-and-hold gestures, alongside the
   existing `REVERSE_ENGINEERING.md` `qhr`-entry code-level finding (a real, compiled write path
   exists in the app's decompiled source; this session did not observe it fire).
3. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` — `TOUCH-007`'s row gains a second confirming session
   (`CAP-051`, in addition to `CAP-027`), now with genuine physical-gesture video.
4. **New Test-ID proposal** (per `CAP-051-EVENT-NOTES.md`'s own flagged gap — no existing Test-ID
   covers this specific DLCI-0x02/DLCI-0x04 correlation question): a candidate ID such as `ANC-005`
   ("DLCI 0x02 `qhr` field-13 parallel-write check for an ANC-mode change") could be added to
   `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, linked to Group AM, with this file as its first (negative)
   evidence — proposed, not added unilaterally.

None of the above reaches `DECISIONS.md`'s promotion bar (a clean negative does not promote anything
to 🟢 FACT in the sense of a new command/opcode finding) — nothing is proposed there.

## 6. Open questions

- 🔴 Why does the in-app `QuickActionsFragment` tap in this session's app version
  (`1.0.955078536`) appear to route exclusively through DLCI 0x04's official path, with no
  accompanying DLCI 0x02 `qhr` field-13 write, despite `REVERSE_ENGINEERING.md`'s static-analysis
  finding that this exact UI element's `OnClickListener` calls `fye.a(qhs)` (§3)? Not resolved by
  this capture — a genuinely open tension between static code analysis and wire behavior, not
  reconciled here.
- 🔴 Same question for the physical press-and-hold gesture's `gvi`/`gvj` code path (§3).
- 🔴 Left/Right identity of the physical earbud used for all three gestures remains unconfirmed from
  video alone (carried over from `CAP-051-EVENT-NOTES.md`'s own Procedure note — not investigated
  further this session, as it does not bear on the DLCI 0x02/0x04 correlation question).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-051-2026-09-14_21-42-55_21-44-09-Group_AM/CAP-051-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-051-2026-09-14_21-42-55_21-44-09-Group_AM/CAP-051-FINDINGS
