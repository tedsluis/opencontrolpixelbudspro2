# Findings: `CAP-027` (Group N — touch gestures)

Standardized, evidence-based extraction from `CAP-027-btsnoop_hci.log` + `CAP-027-recording.mp4`,
staged here for later promotion into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-027` · **Date:** 2026-08-30 · **Firmware:** `release_5.203` (🟢 confirmed
on-wire, DLCI 0x08 private envelope, 6 occurrences in this log) · **Phone:** Pixel 7a (⚪
ASSUMPTION, not screen-confirmed this session) · **Log file:** `CAP-027-btsnoop_hci.log` (3,474
packets, 2026-08-30 15:44:22.07–15:51:47.04 local/+0200 — wider than the video) · **Video:**
`CAP-027-recording.mp4` (233.77s, 15:45:14–15:49:07 local, on-screen wall-clock overlay) · **Buds
MAC (partial):** `04:00:6e:cf:6e:07` — same physical device as `CAP-021`/`CAP-033`.

**Scope note:** Group N covers `TOUCH-002`–`TOUCH-007` (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` line
165–170). Spotify was playing throughout (started 15:45:30), which turned out to be essential —
see §3.

---

## 1. Session overview

Bluetooth was enabled and the Buds connected at the start of the video (15:45:18–15:45:20, no
frame-level detail sought here — out of this Group's scope). Spotify playback started at
15:45:30 (AVRCP `PlaybackStatusChanged → Playing`, frame 1194 @15:45:28.474, offset 1.53s — see
§3's timing-offset note). The full validated event timeline (video actions ↔ wire frames) is in
`CAP-027-EVENT-NOTES.md`.

**Timing-offset note (applies to every correlation in this file):** across all 15 timestamped
actions in this session that have an unambiguous single wire match, the video-overlay timestamp
runs a consistent **1.20–2.31s ahead of** the wire event it corresponds to (one outlier at 4.15s,
`TOUCH-003` @15:45:59). This is consistent with encoding/registration lag on the video side, not a
clock fault, and matches the same-direction offset independently observed in `CAP-033` (this
project's other 2026-08-30 session). All attributions below are **timing correlations** (nearest
wire event after the noted action, within this offset band) — no video frame in this capture shows
a finger actually touching a bud (the camera is aimed at the phone screen throughout), so none of
this Group's attributions has the kind of direct visual confirmation `CAP-001`'s ANC taps had.

## 2. Channel inventory (🟢 FACT)

Full-log DLCI inventory (`tshark -r CAP-027-btsnoop_hci.log -Y "btrfcomm.len>0" -T fields -e
btrfcomm.dlci -e frame.p2p_dir | sort | uniq -c`):

| DLCI | Frames (Sent+Rcvd) | Already documented as | Relevant to Group N? |
|---|---|---|---|
| `0x02` | 462+49 = 511 | `libmaestro`/Pigweed `pw_hdlc` (`PROTOCOL.md` §2.2a) | **No** — see §3.3 |
| `0x04` | 44+10 = 54 | Official Fast Pair Message Stream (`PROTOCOL.md` §2.1/§4.1) | **Yes** — carries `TOUCH-007`, §4 |
| `0x08` | 57+22 = 79 | Private `[Group][Code][Len][Value]` envelope (`PROTOCOL.md` §2.3) | No gesture-correlated content found |
| `0x0c` | 30+20 = 50 | HFP AT-command channel (`PROTOCOL.md` §4.3 Option C; channel identity cross-confirmed this same day in `CAP-033-FINDINGS.md` §5's SDP dump as RFCOMM channel 6, "Handsfree" service class) | No — standard SLC/battery traffic, unrelated to touch |
| `0x00` | 15+15 = 30 | RFCOMM multiplexer control channel (standard) | No |

**Not an RFCOMM DLCI at all:** the touch/swipe semantics for `TOUCH-002`–`TOUCH-006` (§3) live on
**AVRCP**, which runs over its own L2CAP PSM, independent of the RFCOMM DLCIs above. This is the
single most important structural finding of this capture — see §3.

## 3. `TOUCH-002`–`TOUCH-006`: carried entirely over AVRCP, not RFCOMM (🟢 FACT)

`tshark -r CAP-027-btsnoop_hci.log -Y "btavctp or btavrcp"` shows every tap/swipe action in this
session's timeline maps 1:1 onto a standard AVRCP `Pass Through` command or
`RegisterNotification(VolumeChanged)` event — Wireshark's own AVRCP dissector decodes these
natively (spec-compliant profile, not a proprietary format), so no hand-rolled frame parser is
needed for this section; the byte-for-byte match is nonetheless verifiable directly from the
capture with the command below.

### 3.1 Mapping table

| Test-ID | Gesture | AVRCP event | Frame(s) (representative) | Semantics match `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` line |
|---|---|---|---|---|
| `TOUCH-002` | Tap once | `Pass Through: PAUSE` or `PLAY` (Pushed), toggling on current play state | 1580, 2139 (PAUSE); 2251, 2464, 2642 (PLAY) | 165: "Play/pause" ✅ |
| `TOUCH-003` | Double-tap | `Pass Through: FORWARD` (Pushed) | 1909, 2313 | 166: "Next track" ✅ |
| `TOUCH-004` | Triple-tap | `Pass Through: BACKWARD` (Pushed) | 1980, 2066, 2396, 2575, 2746 | 167: "Previous track" ✅ |
| `TOUCH-005` | Swipe forward | `RegisterNotification: VolumeChanged` (increase) | 2822 (59→65%), 2999 (59→65%) | 168: "Raise volume" ✅ |
| `TOUCH-006` | Swipe backward | `RegisterNotification: VolumeChanged` (decrease) | 2873 (65→59%), 3032 (65→59%) | 169: "Lower volume" ✅ |

Every occurrence, its exact frame number, and its video timestamp/offset is listed in
`CAP-027-EVENT-NOTES.md`'s Event Timeline — not repeated here to avoid duplicating a table across
two files.

**Raw hex, one representative frame per gesture type** (`tshark -r CAP-027-btsnoop_hci.log -Y
"frame.number==<N>" -x`, HCI ACL layer, AVCTP/AVRCP payload starts after the L2CAP header):

```
Frame 1580 (TOUCH-002, PAUSE Pushed):
0000  02 02 20 0c 00 08 00 4d 00 10 11 0e 00 48 7c 46
0010  00
  -> AVRCP Pass Through opcode 0x7c, operation ID 0x46 = PAUSE (state_flag byte 0x00 = Pushed)

Frame 1909 (TOUCH-003, FORWARD Pushed):
0000  02 02 20 0c 00 08 00 4d 00 80 11 0e 00 48 7c 4b
0010  00
  -> operation ID 0x4b = FORWARD

Frame 1980 (TOUCH-004, BACKWARD Pushed):
0000  02 02 20 0c 00 08 00 4d 00 a0 11 0e 00 48 7c 4c
0010  00
  -> operation ID 0x4c = BACKWARD

Frame 2822 (TOUCH-005, VolumeChanged 65%):
0000  02 02 20 13 00 0f 00 4d 00 02 11 0e 0d 48 00 00
0010  19 58 31 00 00 02 0d 53
  -> Vendor-dependent PDU, EVENT_VOLUME_CHANGED (0x0d), volume byte 0x53 = 83/127 ≈ 65% (matches
     tshark's own decoded "Volume: 65%" — cross-checked, not re-derived independently)
```

Operation IDs `0x46`/`0x4b`/`0x4c` match the standard AV/C Panel subunit Pass Through op-code
table (`0x44`=PLAY, `0x46`=PAUSE, `0x4b`=FORWARD, `0x4c`=BACKWARD) — this is a spec-defined,
non-proprietary encoding, so no further zero-creativity byte-level derivation is needed beyond
confirming the dissector's reading against the raw hex above.

### 3.2 The "unintentional" swipe at 15:45:48 (🟢 FACT for the traffic; 🔴 open for its cause)

`CAP-027-EVENT-NOTES.md` logs a swipe on the right bud at 15:45:48 that the tester noted as
unintentional. It **did** produce AVRCP traffic — frame 1794 @15:45:46.515, `Rcvd Pass Through:
Control - PLAY (Pushed)`, i.e. a **tap-shaped** command (toggling from the Paused state left by
the preceding `TOUCH-002` at 15:45:39), not the `VolumeChanged` shape a genuine swipe produces
(§3.1). Two readings are equally consistent with this one data point, and this capture cannot
distinguish them:

- the touch sensor received the contact and classified it as a tap rather than a swipe (a
  misclassification on the device side), or
- the tester's own label ("swipe") described the intended gesture, not what the hardware actually
  registered, and a shorter/off-axis contact was interpreted as a plain tap.

What **is** established either way: the mechanism does not silently ignore an off-target/errant
touch — some contact on the bud reliably produces *a* Pass Through command, matching this
project's existing evidence-based reverse-engineering goal of preferring "produced an
observable, if ambiguous, event" over "produced a guessed non-event."

### 3.3 DLCI 0x02 traffic in this session — not gesture-correlated (🟡 HYPOTHESIS)

DLCI 0x02 carries far more traffic in this session (511 frames) than is typical for a short
capture, but it does **not** correlate with any Group N action. The bulk (398 of 511 payload
frames) are exactly 30 bytes and recur at a steady **~1.0–1.1s cadence** throughout the entire
session (e.g. frames at 15:45:29.02, 30.04, 31.05, 32.07, 33.07 — 1.01–1.02s apart), continuing
through gaps between gestures and past the video's own end (last DLCI 0x02 frame in this log:
15:51:38.47, ~2.5 minutes after the video stopped). This is consistent with a continuous,
gesture-independent periodic exchange on the already-documented `libmaestro`/pigweed channel
(`PROTOCOL.md` §2.2a), not a new finding about the channel's framing or identity — flagged here
only so a future reader doesn't mistake this session's unusually dense DLCI 0x02 traffic for a
touch-gesture signal. **Not decoded further in this file** — doing so is out of Group N's scope
(no Test-ID this Group covers predicts DLCI 0x02 content), and `PROTOCOL.md` §2.2a's own open
question about this channel's Sent-direction payload semantics is unaffected either way by this
observation.

## 4. `TOUCH-007` (press-and-hold): carried on DLCI 0x04, the *official* Fast Pair Message Stream — not `libmaestro` (🟢 FACT for the frame content; 🟡 HYPOTHESIS for the gesture attribution)

Unlike `TOUCH-002`–`TOUCH-006`, `TOUCH-007`'s two occurrences (right bud, then left bud — both
un-timestamped `TBD`/`TDB` in the original procedure notes) show up as RFCOMM traffic, on **DLCI
0x04** — the same officially-documented Fast Pair Message Stream channel already confirmed 🟢 FACT
for ANC control in `PROTOCOL.md` §4.1, **not** DLCI 0x02 (`libmaestro`/pigweed).

### 4.1 Configuration context (⚪ ASSUMPTION carried forward, independently corroborated below)

Per `CAP-021-FINDINGS.md` §3/§8 (2026-08-21, 9 days before this session), the last-recorded
per-earbud press-and-hold assignment was **both earbuds → "Active noise control"** (Left last set
by frame 4315 @08:03:49.667; Right last set by frame 4976 @08:04:09.920 — both later than each
earbud's respective "Digital assistant" assignment). No capture between `CAP-021` and `CAP-027`
re-queries or re-sets this configuration, so carrying it forward to this session is an ⚪
ASSUMPTION, not a re-confirmed FACT — but §4.2 below independently corroborates it from this
session's own wire data.

### 4.2 The two `TOUCH-007` frames (🟢 FACT for content; frame numbers as evidence)

Locating them: with `TOUCH-006` (right) confirmed at frame 2873 (15:48:17-ish) and `TOUCH-005`
(left) confirmed at frame 2999 (15:48:39-ish), the only unexplained DLCI 0x04 traffic in the
intervening gap — and in the following gap after `TOUCH-006` (left) at frame 3032 through the end
of video at 15:49:07 — is:

```
$ tshark -r CAP-027-btsnoop_hci.log -Y "btrfcomm.dlci==0x04 and btrfcomm.len>0" \
    -T fields -e frame.number -e frame.time -e data.data
...
2930  2026-08-30T15:48:24.898696+0200  0813000401e8e808
3056  2026-08-30T15:48:46.657653+0200  0813000401e8e840
3091  2026-08-30T15:48:52.342094+0200  0813000401e8e880
```

All three match `PROTOCOL.md` §4.1's already-documented **"Notify ANC state" (`0x13`)** shape
exactly: `[Group:1=0x08][Code:1=0x13][Len:2BE=0x0004][Version:1=0x01][UI toggles:1=0xe8][Settable
toggles:1=0xe8][Current state:1=XX]`, using the same one-hot bit mapping already established
there (`0x08`=bit3=ANC/Active, `0x40`=bit6=Adaptive, `0x80`=bit7=Transparent/Aware):

| Frame | Time | `new_mode` byte | Decoded ANC mode | Gap this falls in |
|---|---|---|---|---|
| 2930 | 15:48:24.898 | `0x08` | ANC / Active Noise Cancelling | Right-bud `TOUCH-007` (between `TOUCH-006` right @15:48:17 and `TOUCH-005` left @15:48:39) |
| 3056 | 15:48:46.658 | `0x40` | Adaptive | Left-bud `TOUCH-007` (between `TOUCH-006` left @15:48:44 and end of video @15:49:07) |
| 3091 | 15:48:52.342 | `0x80` | Transparent / Aware | Same gap as 3056, 5.684s later |

This is a **Notify** frame (Provider→Seeker, no ACK expected per `PROTOCOL.md` §4.1's table),
consistent with the Buds spontaneously reporting an ANC-mode change the press-and-hold gesture
itself triggered on-device — not a `Set` (`0x12`) command from the phone. That the mode observed
is ANC/Active, Adaptive, and Transparent (never "Off") across all three frames is consistent with
— and independently corroborates — §4.1's ⚪ ASSUMPTION that both earbuds were still configured
for ANC-mode cycling as of this session, carried forward from `CAP-021`.

### 4.3 Attribution and timestamps (🟡 HYPOTHESIS; one open sub-question)

- **Right bud `TOUCH-007`: frame 2930, 15:48:24.898** (log time). No independent video-visible
  moment exists to cross-check against (the phone screen shows the unchanged Spotify player
  throughout this gap — the physical gesture happens off-camera, on the bud itself). This is the
  **only** candidate frame in the entire right-bud gap, which makes the attribution reasonably
  strong despite the lack of visual confirmation.
- **Left bud `TOUCH-007`: frames 3056 (15:48:46.658) and 3091 (15:48:52.342).** 🔴 **Not resolved
  which reading is correct:** the procedure notes record only **one** press-and-hold action for
  the left bud, but the wire shows **two** distinct ANC-mode Notify frames, 5.684s apart, each
  advancing to a different mode (Adaptive, then Transparent). Two explanations are equally
  consistent with this capture's evidence:
  - a single held gesture that advanced the (undocumented) rotation cycle by two steps while held,
    or
  - two separate press-and-hold actions performed close together, with only the first noted.

  This capture cannot distinguish these — no video frame shows the physical gesture, and no other
  session-log signal (e.g. a distinguishable "hold begins"/"hold ends" marker) is present in either
  frame. Recorded as 🔴 OPEN QUESTION rather than guessed. A future capture that visually confirms
  the exact duration of a single press-and-hold, correlated against how many Notify steps it
  produces, would resolve this.

## 5. Other DLCIs checked, no gesture-correlated finding

- **DLCI 0x08** (79 frames): matches the already-documented `[Group][Code][Length][Value]`
  capability/status-ping shape (`CAP-004-FINDINGS.md` §5a) — e.g. frame 799 `05 0c 00 00`, frame
  809 `05 0a 00 0d 0a 07 37 31 33 66 38 35 35 10 40 18 00`. No frame in this DLCI's traffic falls
  in a gap unexplained by §3/§4 above, so no further decoding was attempted here (out of Group
  N's scope).
- **DLCI 0x0c** (50 frames): standard HFP AT-command SLC setup and `AT+BIEV`/`AT+CIND` battery
  traffic (e.g. frame 467 `AT+BRSF=921`, frame 483 `AT+CIND=?`) — matches `PROTOCOL.md` §4.3
  Option C exactly; unrelated to touch gestures. (This channel's RFCOMM server-channel identity —
  channel 6, SDP service class "Handsfree" 0x111E — was independently confirmed the same day via
  `CAP-033`'s SDP browse; see `CAP-033-FINDINGS.md` §5.)

## 6. Open questions

- 🔴 Left-bud `TOUCH-007`: does one held press-and-hold gesture advance the ANC rotation by more
  than one step, or did two gestures happen close together with only one noted? (§4.3) — copied to
  `PROTOCOL.md` §6.
- 🔴 What causes an "unintentional" swipe to register as a tap-shaped Pass Through command instead
  of either a volume-shaped one or no command at all? (§3.2) — not pursued further here, flagged
  as a UX/hardware-classification question rather than a protocol-framing one.
- 🔴 `TOUCH-007`'s ANC-mode rotation order (Active → Adaptive → Transparent observed here, in that
  order) is not itself confirmed as a fixed cycle — `CAP-021-FINDINGS.md` §4/§7 already left the
  rotation-list order/Left-Right attribution open; this session adds one more short, partial
  observation of the sequence without resolving it.

## 7. Recommended next steps

- A capture that keeps the camera aimed at the bud/ear (not just the phone screen) during Group N
  actions would let every `TOUCH-00x` attribution in this file move from timing-correlation to
  visual confirmation, matching the stronger evidentiary bar `CAP-001`'s ANC taps met.
- A deliberately short vs. deliberately long press-and-hold, captured separately and timed on
  video, would resolve §4.3's open question about single-hold-multi-step vs. repeated-press.
- `PROTOCOL.md` §4.1 currently documents `TOUCH-007`'s DLCI-0x04/Group-0x08 Notify shape as a
  general mechanism (via `CAP-001`'s app-driven ANC taps); this capture's finding that a
  **hardware** gesture (press-and-hold) also produces the identical Notify shape is worth folding
  into that section's evidence list — proposed here, not committed, per `AGENTS.md` §6.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-027-2026-08-30_15-45-14_15-49-07-Group_N/CAP-027-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-027-2026-08-30_15-45-14_15-49-07-Group_N/CAP-027-FINDINGS
