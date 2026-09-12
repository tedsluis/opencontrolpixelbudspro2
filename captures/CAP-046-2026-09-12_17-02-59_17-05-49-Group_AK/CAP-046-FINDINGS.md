# CAP-046: Volume balance (`field 17`) scale/direction (Group AK, `AUDIO-003`)

Standardized, evidence-based extraction from `CAP-046-btsnoop_hci.log` + `CAP-046-recording.mp4`,
staged here per `PROJECT_RULES.md` §2. Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

## 1. Capture Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-046` |
| Purpose | Group AK — resolve `qhr` field 17 (Volume balance)'s numeric scale/range and which direction (Left/Right) corresponds to negative vs. positive zigzag-decoded values, using isolated discrete extreme-position samples |
| Date | 2026-09-12 |
| Firmware | ⚪ ASSUMPTION `release_5.203` (carried over) |
| Test device | Pixel 7a, Android 17, official Pixel Buds Companion App `1.0.955078536`, Google Play services active |
| Log file | [`CAP-046-btsnoop_hci.log`](./CAP-046-btsnoop_hci.log) — 2,655 packets, 307.74s, `2026-09-12 17:03:01.185–17:08:08.925`. 0/2,655 `cap_len≠len` mismatches — untruncated. |
| Video file | [`CAP-046-recording.mp4`](./CAP-046-recording.mp4) — 170.14s (`ffprobe`), overlay `17:02:59`–`17:05:49` |
| Notes file | [`CAP-046-EVENT-NOTES.md`](./CAP-046-EVENT-NOTES.md) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:...:07` |

## 2. Central finding — polarity resolved, opposite of the draft's own assumption (🟢 FACT, 3× video-confirmed)

**Method:** every DLCI 0x02 `Sent` frame (61 total) was HDLC-unescaped, CRC-32-verified, and searched
for the `qhr` field-17 tag (`0x88 0x01`, per `PROTOCOL.md` §4.5.7):

```python
def unescape_hdlc(data):
    out = bytearray(); i = 0
    while i < len(data):
        b = data[i]
        if b == 0x7d:
            i += 1; out.append(data[i] ^ 0x20)
        else:
            out.append(b)
        i += 1
    return bytes(out)

def zigzag(n): return (n >> 1) ^ -(n & 1)
# ... walk field5{field4{field17=N}}} per PROTOCOL.md §4.5.7, verify CRC-32 first
```

Cross-checked with a raw-hex `grep -c "8801"` across every DLCI 0x02 Sent frame — exactly 8 matches,
identical to the structured decode. **8 `field17` writes total, zigzag-decoded:**

| Frame | Wire time | Raw varint | Zigzag value | Video-confirmed slider position |
|---|---|---|---|---|
| 1181 | 17:03:42.117 | 199 | **-100** | **Right** extreme (`w_43.1.png`) |
| 1686 | 17:04:01.984 | 4 | 2 | center |
| 1746 | 17:04:15.383 | 200 | **+100** | **Left** extreme (`w_76.4.png`) |
| 1786 | 17:04:38.261 | 2 | 1 | center |
| 1834 | 17:04:51.320 | 199 | **-100** | **Right** extreme again (`w_112.3.png`) |
| 1873 | 17:05:09.216 | 0 | 0 | center |
| 1908 | 17:05:22.959 | 200 | **+100** | **Left** extreme again (`w_144.0.png`) |
| 1957 | 17:05:46.275 | 2 | 1 | center |

**Result, 3/3 extreme-position video confirmations, zero counter-examples:**
- `field17 = -100` (negative extreme) → slider at the **Right** extreme.
- `field17 = +100` (positive extreme) → slider at the **Left** extreme.
- Center-return values cluster near zero (`0`, `1`, `1`, `2`) — consistent with a true center of `0`,
  small nonzero readings reflecting the drag gesture not landing exactly on-pixel-center before
  release, the same pattern already documented for the ±6.0 EQ clamp's own near-extreme values
  (`CAP-015-FINDINGS.md` §4).

**This is the opposite polarity from the original working assumption** (`CAP-022-FINDINGS.md` §5 and
this capture's own draft notes both implicitly/explicitly labeled the first-sampled negative value as
"Left") — this session's isolated, video-confirmed extreme samples directly overturn that assumption:
**positive = Left, negative = Right.** The numeric range is confirmed as exactly **±100** (a clean
percentage-like scale), matching both sampled extremes with no over/undershoot in either direction.

## 3. Bonus finding — `field19` (Mono audio) fires in lockstep with every Balance-extreme write (🟡 HYPOTHESIS, new, 8/8 samples)

```
$ grep "9801" <(tshark -r CAP-046-btsnoop_hci.log -Y 'btrfcomm.dlci==2 and btrfcomm.len>0 and frame.p2p_dir==0' \
    -T fields -e frame.number -e frame.time -e data.data)
1190  17:03:42.484157  980101   <- Mono=ON,  0.37s after field17=-100
1696  17:04:02.163007  980100   <- Mono=OFF, 0.18s after field17=2 (center)
1754  17:04:15.679781  980101   <- Mono=ON,  0.30s after field17=100
1806  17:04:38.712317  980100   <- Mono=OFF, 0.45s after field17=1 (center)
1843  17:04:51.612759  980101   <- Mono=ON,  0.29s after field17=-100
1880  17:05:09.362430  980100   <- Mono=OFF, 0.15s after field17=0 (center)
1916  17:05:23.290011  980101   <- Mono=ON,  0.33s after field17=100
1965  17:05:46.334776  980100   <- Mono=OFF, 0.06s after field17=1 (center)
```

**All 8 of 8 `field17` writes are immediately followed (0.06–0.45s later) by a `field19` write whose
value tracks whether Balance just reached an extreme (`=1`/ON) or returned near center (`=0`/OFF) —
zero exceptions.** Video confirms this directly: every extreme-position screenshot (`w_43.1.png`,
`w_76.4.png`, `w_112.3.png`, `w_144.0.png`) shows "Mono audio" toggled ON (purple, checked); the
center-position screenshot (`g_62_5.png`) shows it OFF. 🟡 **HYPOTHESIS**: dragging Volume Balance to
a full extreme causes the app/Buds to also enable Mono audio (plausibly because panning fully to one
side makes a stereo signal functionally mono in the remaining ear, and this is the app surfacing that
as an explicit setting) — a genuine causal mechanism, not confirmed beyond this tight, consistent
timing correlation (no direct code/spec source consulted this pass, per `AGENTS.md` §13.6's
zero-creativity rule — flagged as a correlation, not asserted as confirmed causation).

## 4. Correction to the draft: only 2 of the claimed 4 samples occurred (🟢 FACT)

The draft describes 4 samples: 2 full-extreme cycles plus 2 "intermediate-position" cycles at
`-93`/`+96` and `-31`. **Only the first 2 full-extreme cycles are real** — every `field17` write in
the entire session (8 total, exhaustively enumerated in §2) is either `±100` or a near-zero center
value; no intermediate value (`93`, `96`, `31`, or any other non-extreme/non-zero reading) appears
anywhere. Video directly confirms this for the claimed "Sample 4" window specifically: at `t=160`
(overlay `17:05:39`, the draft's claimed intermediate-Left-drag moment), the slider handle is still
resting at the full Left extreme, unchanged since the prior sample — no drag gesture is visible.
**This capture does not provide any intermediate-position data** — `PROTOCOL.md` §6's open item on
this question (does the field scale linearly at non-extreme positions?) remains open.

## 5. Test-ID traceability

- **`AUDIO-003`**: exercised — 2 clean extreme-position cycles (not the planned 4), sufficient to
  resolve the polarity/range question this Group exists to answer (see §2).

## 6. Conclusions & proposed downstream updates

**Recorded as this session's own factual result (no sign-off needed, purely descriptive):**
- 8 `field17` writes, zigzag-decoded, 3 extreme positions video-confirmed.
- `field19` (Mono audio) fires in exact lockstep with every Balance-extreme/center transition.
- Only 2 of the draft's claimed 4 samples actually occurred.

**Proposed (⏳ awaiting maintainer sign-off, per `AGENTS.md` §6/§15):**
1. `PROTOCOL.md` §4.5.7 — **correct the Left/Right polarity**: positive zigzag = Left, negative =
   Right (the field-number/identity FACT from `ADR-019` is unaffected; only the *direction* mapping,
   previously unresolved, is now proposed for promotion). Range confirmed `±100`.
2. `PROTOCOL.md` §6 — remove/narrow the "which direction is negative vs. positive" open item,
   replacing it with this session's resolved reading (pending sign-off).
3. `PROTOCOL.md` §4.5.5a / §6 — add the `field17`↔`field19` (Balance-extreme↔Mono-audio) timing
   correlation as a new 🟡 HYPOTHESIS, not yet promoted.
4. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `AUDIO-003` row — Evidence column pointer to this file.

## 7. Open Questions

- 🔴 Does `field17` scale linearly between center and the ±100 extremes? Not tested — this capture
  only sampled the two extremes and near-center, never an intermediate position.
- 🔴 Is the `field17`↔`field19` correlation (§3) a genuine app-side automatic behavior (dragging to
  an extreme causes Mono audio to engage), or some other shared trigger? Not confirmed beyond timing.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-046-2026-09-12_17-02-59_17-05-49-Group_AK/CAP-046-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-046-2026-09-12_17-02-59_17-05-49-Group_AK/CAP-046-FINDINGS
