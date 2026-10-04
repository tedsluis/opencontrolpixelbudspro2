# Findings: `CAP-041` (Group AH — DLCI 0x02 connect-time RPC burst vs. non-default settings, `OBS-007`)

> **Status as of 2026-10-03** (`ai-sessions/0069`; replaces the 2026-09-30 banner): §3, §6, §7 and §8 were rewritten in place — the phone's **requests** in the connect burst do not vary with the settings, but the Buds' **answers** in the same burst carry every setting (the burst is the official app's `ReadSetting` sweep, `PROTOCOL.md` §6 2026-09-24). §4's "2-field sub-message" is `SubscribeRuntimeInfo` entry 6.1 = Case % (Option F, ADR-043). §1, §2 and §5 are the analysis as written.

Standardized, evidence-based extraction from `CAP-041-btsnoop_hci.log` + `CAP-041-recording.mp4`,
staged here for later promotion into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-041` · **Date:** 2026-09-06 · **Firmware:** 🟢 FACT `release_5.203` (confirmed
on-screen, 17:11:32) · **Phone:** Pixel 7a, Android version ⚪ not recorded in this session (this file said "14"; the same phone is recorded as 17 in the captures before and after — unreconciled, `ai-sessions/0069` `A68-CAP-23`), official Pixel Buds Companion App, Google
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

## 3. Settings in the burst: none in the phone's requests, all of them in the Buds' answers (🟢 FACT)

§2 compared the **phone → Buds** payload lengths only, and found them the same in every window: the requests are a fixed sweep and carry no setting value. The
**Buds → phone** frames of the same burst are the answers, and they differ with the settings state. Decoded with `python3 scripts/pwrpc_decode.py
CAP-041-btsnoop_hci.log` (pw_hdlc + pw_rpc, CRC-32 checked; 96 `ReadSetting` requests = 32 per window):

| `ReadSetting` answer | Window A | Window B | Window C |
|---|---|---|---|
| field 4 (touch controls) | 799 `4:{4:0}` | 1938 `4:{4:0}` | 3211 `4:{4:0}` |
| field 7 (press and hold) | 805 Left 5 / Right 5 | 1944 same | 3227 Left **6** / Right 5 |
| field 15 (Volume EQ) | 868 `1` | 2007 `1` | 3239 **`0`** |
| field 16 (active EQ) | 871 `[6.00 × 5]` | 2010 `[6.00 × 5]` | 3242 **`[-6.00 × 5]`** |
| field 17 (balance, raw) | 874 `200` | 2013 `200` | 3245 **`199`** |
| field 19 (mono) | 880 `1` | 2019 `1` | 3252 **`0`** |
| field 29 | 907 `2` | 2047 `2` | 3279 **`1`** |

(`CAP-036`'s all-defaults baseline: field 16 = `[0.1, 0, 0.3, 0.2, 0.2]`, frame 1525.) So the connect burst **is** a settings read-back — by the official app
asking, not by the Buds volunteering. (Rewritten in place 2026-10-03, `ai-sessions/0069`, A68-CAP-18: this section and §8 used to conclude "no settings read-back
of any kind" from the request side alone.)

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

- 🟢 This session produced 3 wire-confirmed reconnects (not the 4 originally claimed), each with a distinct, logged settings state (§1).
- 🟢 The phone's side of the DLCI 0x02 connect burst (44–46 frames, a run of 26-byte `ReadSetting` requests) is the same in all three windows and in `CAP-036`'s
  baseline (§2, §8): the official app sends a fixed sweep.
- 🟢 The Buds' side of the burst answers that sweep with the current value of every setting (§3) — `OBS-007`'s question "does the connect burst carry the settings
  state?" is answered **yes** (in the answers), consistent with `DECISIONS.md` ADR-034/036.
- 🟢 The one request that differs per window is `SetWallclock` carrying the phone's clock (§8).
- 🟢 The recurring sub-message with the constant value 79 is `SubscribeRuntimeInfo` entry 6.1, the Case % (frame 782; `PROTOCOL.md` §4.3 Option F, ADR-043).

## 7. Open questions

- 🔴 Which claimed Bluetooth-toggle action (of the original 4) did not produce a distinct ACL connection — not resolved by the video frames pulled (§1).

## 8. Content-level diff of the phone's requests in the connect-time burst across 4 sessions (🟢 FACT)

**Method**: for each window (this capture's A/B/C, plus `CAP-036`'s baseline), extracted every
Sent-direction DLCI 0x02 payload in the burst window already isolated in §2, concatenated the raw
bytes across frames (RFCOMM I-frames can pack multiple HDLC sub-frames), split on unescaped `0x7e`
flags, HDLC-unescaped each sub-frame (`0x7d <X>` → `X XOR 0x20`), and verified each one's trailing
4-byte CRC-32 (IEEE 802.3/zlib) against the unescaped body — the same verification method as
`PROTOCOL.md` §2.2a's own original framing proof.

```
$ tshark -r CAP-041-btsnoop_hci.log \
  -Y "bthci_acl.chandle==0x0002 and btrfcomm.dlci==2 and btrfcomm.len>0 and frame.p2p_dir==0 and frame.time_relative>=3.7 and frame.time_relative<=9.5" \
  -T fields -e frame.number -e data.data
# (repeated per window with each window's own chandle/time bounds from §2; CAP-036's baseline
# pulled from CAP-036-btsnoop_hci.log, chandle 0x0005, 45.2-48.4s)
```

**Result**: all 184 extracted sub-frames (46 per window × 4 windows) pass CRC-32 verification —
**zero corrupted/misaligned extractions.** Comparing decoded (unescaped, CRC-stripped) sub-frame
bodies:

- **Tail run (sub-frames 16–45 of 46, the dominant run of 26-byte frames already characterized in
  §2–§3): byte-for-byte identical across all 4 sessions**, with no exception — confirmed by direct
  string equality of the decoded hex bodies at every one of these 30 positions.
- **Header run (sub-frames 0–15): each session contains the exact same *set* of 16 distinct
  sub-frame values as every other session** (verified via set symmetric-difference: 0 for the tail,
  and for the header, exactly one differing pair per session — see below) — **the only difference is
  transmission order**, not content. This matches this capture's own §2 finding that "the only
  positional differences are in the early header frames' exact order/count," now confirmed at the
  full-content level, not just the coarse length level.
- **The one genuine content difference, present in every session, all four values distinct**:
  ```
  Window A:      00 4b 03 10 15 1d ea 71 de 7e 25 4e ed 3b 67 2a 07 08 a7 83 9b ba 87 34
  Window B:      00 4b 03 10 15 1d ea 71 de 7e 25 4e ed 3b 67 2a 07 08 e4 8c a0 ba 87 34
  Window C:      00 4b 03 10 15 1d ea 71 de 7e 25 4e ed 3b 67 2a 07 08 84 bc a8 ba 87 34
  CAP-036:       00 4b 03 10 15 1d ea 71 de 7e 25 4e ed 3b 67 2a 07 08 cb fe d0 d5 86 34
  ```
  Bytes 0–17 (`00 4b 03 10 15 1d ea 71 de 7e 25 4e ed 3b 67 2a 07 08`) are constant across all four —
  matching the cross-session-stable correlation-ID-prefix shape already documented in `PROTOCOL.md`
  §4.5's shared preamble (`03 10 XX 1d ea 71 de 7e 25...`). Bytes 18–20 differ in every session
  (`a7 83 9b` / `e4 8c a0` / `84 bc a8` / `cb fe d0`); bytes 21–23 are shared by Windows A/B/C
  (`ba 87 34`) but differ for `CAP-036` (`d5 86 34`, 2 of 3 bytes different).
- **🟢 FACT: the varying sub-frame is `maestro_pw.Maestro/SetWallclock` with the phone's wall clock in milliseconds.** Method id `4e ed 3b 67` = `0x673bed4e` =
  `h65599("SetWallclock")`; the payload `08 a7 83 9b ba 87 34` is field 1 = varint 1788707520935 → 2026-09-06 15:12:00.935 UTC, and the frame that carries it (783)
  was captured at 17:12:00.936 +02:00 — the same instant. Windows B and C: 1912 `1:1788707604068`, 3148 `1:1788707741188`; each answered by an empty `RESPONSE`
  (789, 1920, 3154). (`python3 scripts/pwrpc_decode.py CAP-041-btsnoop_hci.log | grep SetWallclock`; rewritten 2026-10-03 — it was a 🟡 "timestamp or nonce".)

**Conclusion**: the **requests** of the connect burst are invariant apart from the wall clock; the settings are in the **answers** (§3). This section's diff is of
the request side only and must not be read as "the burst carries no settings read-back".

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-041-2026-09-06_17-10-39_17-17-48-Group_AH/CAP-041-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-041-2026-09-06_17-10-39_17-17-48-Group_AH/CAP-041-FINDINGS
