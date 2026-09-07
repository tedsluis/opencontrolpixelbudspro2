# Findings: `CAP-041` (Group AH — DLCI 0x02 connect-time RPC burst vs. non-default settings, `OBS-007`)

Standardized, evidence-based extraction from `CAP-041-btsnoop_hci.log` + `CAP-041-recording.mp4`,
staged here for later promotion into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-041` · **Date:** 2026-09-06 · **Firmware:** 🟢 FACT `release_5.203` (confirmed
on-screen, 17:11:32) · **Phone:** Pixel 7a, Android 14, official Pixel Buds Companion App, Google
Play Services enabled (same baseline as `CAP-036`). **Log file:** `CAP-041-btsnoop_hci.log` (4,003
packets, 0/4,003 `cap_len`≠`len` mismatches, 2026-09-06 17:11:56.964–17:19:38.251 local, 461.29s).
**Video:** `CAP-041-recording.mp4` (421.23s, wall-clock overlay confirmed starting 17:10:39).
**Buds MAC (partial):** `04:00:6e:cf:6e:07` — same physical device as every other capture in this
project.

---

## 0. Capture integrity (🟢 FACT)

```
$ capinfos CAP-041-btsnoop_hci.log
Number of packets:   4,003
Capture duration:    461.287128 seconds
Earliest packet time: 2026-09-06 17:11:56.963543
Latest packet time:   2026-09-06 17:19:38.250671

$ tshark -r CAP-041-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'
mismatches: 0
```
Untruncated, raw-path extraction (no `-btsnooz` suffix, no snaplen cap). `ffprobe` confirms the
video is 421.23s; a frame extracted at `t=0` shows the burnt-in overlay reading `6 sep 2026
17:10:39`, matching the Log Metadata's claimed start.

## 1. Video re-pass: the claimed 4-window procedure maps onto 3 wire-confirmed reconnects (🟢 FACT for the wire count; 🔴 open for the exact 4th-window mapping)

**Method:** `ffmpeg -ss <t> -frames:v 1` extraction at t=0,46,60,63,66,69,72,75,78,80,85,90,95,100s
and further spot checks, read against the burnt-in overlay, cross-referenced against
`bthci_evt.bd_addr==04:00:6e:cf:6e:07` Connection/Disconnection Complete events:

```
$ tshark -r CAP-041-btsnoop_hci.log -Y "bthci_evt.bd_addr==04:00:6e:cf:6e:07" \
  -T fields -e frame.number -e frame.time -e bthci_evt.code -e bthci_evt.connection_handle
279   2026-09-06T17:11:59.210239+0200  0x03 (Connection Complete)     0x0002
1414  2026-09-06T17:13:22.493626+0200  0x03 (Connection Complete)     0x0004
2642  2026-09-06T17:15:39.443412+0200  0x03 (Connection Complete)     0x0005

$ tshark -r CAP-041-btsnoop_hci.log -Y "bthci_evt.code==0x05" \
  -T fields -e frame.number -e frame.time -e bthci_evt.connection_handle -e bthci_evt.reason
1338  2026-09-06T17:13:13.685476+0200  0x0002  0x16 (locally terminated)
2569  2026-09-06T17:15:29.826818+0200  0x0004  0x13 (remote user terminated)
```

Only **3** Connection Complete events and **2** Disconnection Complete events exist for the Buds in
this entire log — meaning exactly 3 distinct ACL sessions ("Windows A/B/C" below), not the 4
claimed in the original procedure notes ("Window 1"–"Window 4"). Video evidence for each:

- **Window A** (chandle `0x0002`, connect frame 279, `17:11:59.210`): frame at `t=72s` (`17:11:51`)
  shows a finger on the "Use Bluetooth" toggle in the quick-settings Bluetooth panel, with the Buds
  tile already showing a cached "Active. L:100%, R:100%" label; frame at `t=80s` (`17:11:59`) shows
  the same panel with the Buds tile confirming "Active" — matching the wire's Connection Complete
  almost exactly (within video-frame-sampling tolerance). The log itself starts at `17:11:56.96`,
  only ~3s before this connect, so the corresponding disconnect (the toggle-OFF half of this cycle)
  predates the log and isn't captured.
- **Window B** (chandle `0x0004`, connect frame 1414, `17:13:22.494`, preceding disconnect frame
  1338, `17:13:13.685`): frame at `t=95s` (`17:12:14`) shows a Device-details view (via a
  screenshot-markup overlay — "Screenshot"/"Select" pills visible at the bottom, i.e. the user was
  reviewing a screenshot, not the live app) with a "+ Connect" button and a fully greyed-out ANC
  row — consistent with a disconnected state somewhere in this window, though this specific frame's
  exact wall-clock relationship to the wire's own disconnect (`17:13:13.685`, ~1 minute earlier)
  was not pinned down further this pass.
- **Window C** (chandle `0x0005`, connect frame 2642, `17:15:39.443`, preceding disconnect frame
  2569, `17:15:29.827`): wire event confirmed; not independently re-verified frame-by-frame beyond
  that.

**Correction:** the original Event Timeline's four claimed toggle-off/toggle-on/connect timestamps
(`17:11:13`/`17:11:20`/`17:11:25` for "Window 1", `17:12:04`/`17:12:15`/`17:12:20` for "Window 2",
`17:15:16`/`17:15:20`/`17:15:25` for "Window 3", `17:16:55`/`17:17:00`/`17:17:05` for "Window 4")
run 14–62s **ahead of** (earlier than) their nearest wire event, a larger error than
`CAP-036-FINDINGS.md` §2's own 5–30s corrections. 🔴 **Open question, not resolved this pass:**
which single claimed action failed to produce a distinct new ACL connection. The most likely
explanation — that the claimed "Window 2" and "Window 3" toggles both landed inside what the wire
shows as one continuous Window-B connection (`17:13:22`–`17:15:29`) — is plausible but not
confirmed with the video frames pulled this pass. This does not affect §2–§4 below, which anchor
their analysis to the 3 wire-confirmed windows and their independently-logged settings states, not
to the claimed window count.

## 2. DLCI 0x02 connect-time burst: extraction and length-sequence comparison against `CAP-036`'s baseline (🟢 FACT for the extraction and comparison; scoped negative for OBS-007)

**Method**, per `CAP-036-FINDINGS.md` §4 and `DESKRESEARCH_FINDINGS.md`'s 2026-08-17 entry (split
each RFCOMM I-frame payload on the `0x7e` HDLC flag byte before decoding multiple packed
sub-frames):

```
$ tshark -r CAP-041-btsnoop_hci.log \
  -Y "bthci_acl.chandle==<chandle> and btrfcomm.dlci==2 and btrfcomm.len>0 and frame.p2p_dir==0" \
  -T fields -e frame.number -e frame.time_relative -e btrfcomm.len
```

DLCI 0x02 `SABM`/`UA` (channel-open) times per window: Window A rel. 3.79s (chandle `0x0002`),
Window B rel. 86.99s (chandle `0x0004`), Window C rel. 224.06s (chandle `0x0005`). Sent-direction,
non-empty `btrfcomm.len` sequence for the first ~5.5s after each channel opens:

```
Window A (45 frames): 42,21,21,22,22,26,21,21,22,21,22,31,26,29,26,26,26,26,27,26,26,26,26,26,
                       26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,27,26,26,26,21
Window B (44 frames): 42,22,26,22,22,21,21,31,29,26,26,21,21,43,26,26,26,27,26,26,26,26,26,26,
                       26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,27,26,26,26,21
Window C (46 frames): 21,21,22,21,22,26,29,22,21,21,31,21,26,21,22,26,26,26,26,27,26,26,26,26,
                       26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,27,26,26,26,21

CAP-036 baseline, chandle 0x0005, rel. 45.2-48.4s (45 frames):
                      21,21,22,22,26,22,21,21,43,21,21,31,26,29,26,26,26,26,27,26,26,26,26,26,
                      26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,26,27,26,26,26,21
```

(Note: the leading `42` values in Windows A/B are a bundling artifact — two 21-byte HDLC sub-frames
packed into one 42-byte RFCOMM I-frame payload, per the multi-subframe-per-payload packing already
documented; not a genuinely new frame type.)

**Result:** all four sequences (this session's 3 windows + `CAP-036`'s baseline) share the same
dominant signature — a run of 25–31 consecutive **26-byte** frames (closed by one 27-byte and one
21-byte frame) preceded by a short, variably-ordered set of small header frames drawn from the same
small set of lengths (`21, 22, 26, 29, 31, 42/43`). Frame counts are within 2 of each other (44–46)
across all four. The only positional differences are in the early header frames' exact order/count
(e.g. `CAP-036` and Window B each have one 43/42-byte-adjacent frame that Window A/C's sequences
don't reproduce at the same position) — none of these differences are large enough, or shaped like
a known settings payload (see §3), to read as settings-content.

**Settings states compared** (from the Event Timeline, all four genuinely different, none matching
`CAP-036`'s all-defaults baseline):
- `CAP-036` (baseline): every setting at default (EQ centered, touch controls on).
- Window A: touch controls fully off, one non-default EQ config, mono audio on.
- Window B: + both earbuds' touch customization off, a different EQ config.
- Window C: + both earbuds' touch customization set to Digital assistant/Adaptive, a third EQ
  config, Usage & diagnostics off.

## 3. Settings-shaped candidate check (🟢 FACT, clean negative at the length level)

Checked every payload length in all four compared sequences (§2) against the two known
settings-value shapes already documented in this project: a 5×`float32` EQ-band quintet
(`PROTOCOL.md` §4.2, roughly 20–24 bytes at the relevant nesting level once the outer
`field5{field4{...}}` wrapper and correlation-ID prefix are subtracted) and a single
boolean/enum-shaped short frame for a toggle like touch-controls-off. **No payload length appears
in one of the four sequences and not the others** that would be a plausible candidate for either
shape — every length present in any one sequence also appears in at least one other, including
`CAP-036`'s all-defaults baseline. Per `AGENTS.md` §13.6's zero-creativity rule, no settings-content
reading is offered for any of these lengths.

## 4. Bonus/incidental finding: the DLCI 0x02 periodic push's constant field matches on-screen Case% (🟡 HYPOTHESIS, strengthens but does not resolve `CAP-036-FINDINGS.md` §12.6's open question)

Within each window's connect-time burst (not a separate later push — this occurs ~4s after
channel-open, still inside the burst window itself), a recurring sub-message structurally identical
to `CAP-036-FINDINGS.md` §12.6's already-flagged, unresolved "2-field, non-1/2/3-indexed" pattern
appears:

```
$ tshark -r CAP-041-btsnoop_hci.log -Y "frame.number==782" -T fields -e btrfcomm.dlci -e frame.p2p_dir -e data.data
0x02  1  [...]2a2510ce829bba8734180032120a04084f10011204086410021a04086410023a0608011001180008[...]7e
```

HDLC-unescaped, structurally: field5 (`2a`, len `0x25`=37) → field6 (`32`, len `0x12`=18) → three
4-byte sub-messages `08 <v> 10 <i>`: `[0x4f(79), 1]`, `[0x64(100), 2]`, `[0x64(100), 2]`. Sampled
across 8 occurrences spanning all 3 windows (frames 782/1005/1072/1198/1272/1920/3154, and one more
in Window C's own repeat), **the first field's value is a constant `0x4f` (79) every single time,
in every window, for the entire ~7m41s session** — matching the on-screen Case battery percentage
(79%, confirmed via the video frame at `t=0`, `17:10:39`: "Left: 100%, Case: 79%, Right: 100%").

**What this does and does not show:** because the value never changes across this session (no
battery-level or dock-state change occurred to test the correlation), this is *consistent with*,
but does **not confirm**, the field tracking Case battery — it could equally be any other
session-constant value that happens to equal 79. This neither resolves nor contradicts
`CAP-036-FINDINGS.md` §12.6's own open question (that session's sample was uninformative because
every battery value was 100%, making a coincidental match to *some* field impossible to rule out
there either) — recorded as a second, still-inconclusive data point for that open question, not a
resolution. A future capture bracketing an actual mid-session Case-percentage *change* against this
specific field would be the natural next step.

## 5. Test-ID traceability (`AGENTS.md` §13)

- **`OBS-007`** (primary): exercised across 3 wire-confirmed reconnect windows, each with an
  independently-logged, genuinely non-default and mutually-different settings state. Result: see
  Conclusions below.
- **`PAIR-003`** (incidental, reconnect to an already-bonded device): exercised 3 times (Windows
  A/B/C), each a stored-link-key reconnect, no fresh pairing/SSP exchange observed on any of the 3
  chandles.

## 6. Conclusions

**Confirmed by this session's own evidence, at the strength the evidence supports:**
- This session produced 3 genuine wire-confirmed reconnects (not the 4 originally claimed), each
  with a distinct, logged settings state (§1).
- The DLCI 0x02 connect-time burst's **length/shape signature** is essentially invariant across
  these 3 genuinely different settings states and matches `CAP-036`'s own default-settings baseline
  — same frame count (44–46), same dominant 26-byte-frame-run signature (§2–§3). 🟢 **FACT for this
  session's own observation** (a raw length-sequence comparison, fully reproducible from the
  commands above).
- A recurring sub-message inside that burst holds a constant value (79) matching this session's
  on-screen Case battery percentage throughout — 🟡 **HYPOTHESIS**, unconfirmed (no change occurred
  to test it), strengthens but does not resolve `CAP-036-FINDINGS.md` §12.6's existing open item.

**Proposed, awaiting maintainer sign-off (per `AGENTS.md` §6/§15 — nothing below is committed as
FACT and no `DECISIONS.md` ADR is drafted here):**
- **Outcome classification for `OBS-007`: (b), a scoped clean negative.** At the structural
  (frame-count/length-sequence) level, this session finds no evidence that DLCI 0x02's connect-time
  RPC burst restructures itself based on EQ/touch-controls settings state. This is **not** a claim
  that the burst's byte *content* is settings-independent — only its length signature was compared
  this pass; a full HDLC-unescape+CRC-verify byte-for-byte content diff across all 4 compared bursts
  was outside this pass's time/token budget. Propose recording this scoped negative in `PROTOCOL.md`
  §6 pending a follow-up content-level diff, rather than treating the question as fully closed.
- Recommend as a low-cost next step: complete the byte-for-byte content diff of the ~45-frame burst
  across Windows A/B/C and `CAP-036`'s baseline (the length-sequence work above already isolates
  which frames to compare) to close the gap between "same shape" and "same content."

## 7. Open questions

- 🔴 Which claimed BT-toggle action (of the original 4) did not produce a distinct ACL connection —
  not resolved by the video frames pulled this pass (§1).
- 🔴 Whether the DLCI 0x02 periodic push's 2-field sub-message (§4) actually tracks Case battery, or
  merely coincides with it, remains open — needs a capture bracketing an actual Case% change.
- 🔴 Whether the connect-time burst's payload *content* (not just length) varies with settings state
  is untested (§6) — the natural, low-cost follow-up to this session's own length-level negative.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-041-2026-09-06_17-10-39_17-17-48-Group_AH/CAP-041-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-041-2026-09-06_17-10-39_17-17-48-Group_AH/CAP-041-FINDINGS
