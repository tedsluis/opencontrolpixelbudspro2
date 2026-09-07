# Findings: `CAP-039` (Group AF — `Settable-toggles` byte: Set-tap vs. reconnect-Get, fixed dock state, `OBS-006`)

Standardized, evidence-based extraction from `CAP-039-btsnoop_hci.log` + `CAP-039-recording.mp4`,
staged here for later promotion into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-039` · **Date:** 2026-09-06 · **Firmware:** ⚪ ASSUMPTION `release_5.203`
(carried over, not re-confirmed on-screen this session). **Phone:** Pixel 7a, Android 14, official
Pixel Buds Companion App, Google Play Services enabled. **Log file:** `CAP-039-btsnoop_hci.log`
(372.04s, 6,184 packets, 2026-09-06 07:07:28.040–07:13:40.082 local/+0200, 0/6,184
`cap_len≠len` mismatches — untruncated, raw extraction path; first frame is a full HCI `Reset`,
confirming Bluetooth was freshly re-enabled at session start). **Video:** `CAP-039-recording.mp4`
(ffprobe duration 265.62s, ~07:07:17–07:11:43 local — **shorter than the originally-documented
07:07:17–07:12:20 window by ~37s**; corrected below). **Devices:** phone (Pixel 7a), peer
`04:00:6e:cf:6e:07` ("Pixel Buds Pro 2 van Ted").

---

## 0. Capture integrity (🟢 FACT)

```
$ capinfos CAP-039-btsnoop_hci.log
Number of packets:   6,184
Capture duration:    372.041640 seconds
Earliest packet time: 2026-09-06 07:07:28.040028
Latest packet time:   2026-09-06 07:13:40.081668
Packet size limit:   file hdr: (not set)

$ tshark -r CAP-039-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'
mismatches: 0
```
No snaplen cap, 0/6,184 mismatches — untruncated, raw `btsnoop_hci.log` path (filename suffix
confirms, matching `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3 step 3).

## 1. Connection identification / CLI-hygiene (🟢 FACT)

`bluetooth.addr` does not reliably resolve RFCOMM frames in this project's captures (established
`CAP-036-FINDINGS.md` §1). Using `bthci_evt.bd_addr` instead:

```
$ tshark -r CAP-039-btsnoop_hci.log -Y "bthci_evt.bd_addr==04:00:6e:cf:6e:07 and bthci_evt.code==0x03" \
  -T fields -e frame.number -e frame.time -e bthci_evt.connection_handle
653   2026-09-06T07:07:34.090967+0200  0x0002   (BLE)
683   2026-09-06T07:07:35.110759+0200  0x0005   (classic ACL)
1939  2026-09-06T07:08:29.641790+0200  0x0006
2766  2026-09-06T07:09:04.052094+0200  0x0007
3586  2026-09-06T07:09:42.452317+0200  0x0008
4648  2026-09-06T07:10:25.024896+0200  0x0009
5462  2026-09-06T07:11:04.636795+0200  0x000a
```
Seven Connection Complete events for the Buds' address: one initial BLE+classic pair (`0x0002`/
`0x0005`), then **five further classic reconnects** on chandles `0x0006`–`0x000a`. Disconnection
Complete events (`bthci_evt.code==0x05`) show reason `0x16` ("Terminated by Local Host") for
`0x0005`/`0x0006`/`0x0007`/`0x0008`, and reason `0x13` ("Remote User Terminated") for `0x0009` —
i.e. 4 of 5 disconnects were phone-initiated, consistent with repeated app/OS-level
disconnect-reconnect action (the same locally-initiated pattern `CAP-040`'s own procedure used
deliberately), not a single clean Bluetooth toggle as this Group's procedure described.

## 2. Video re-pass: the documented "one Set, one Get" timeline does not match the video or wire (🟢 FACT — major correction)

**Method:** targeted `ffmpeg -ss <t> -frames:v 1` extraction at video-relative offsets matching
each wire event and the originally-claimed timestamps, per `CAP-036-FINDINGS.md` §2's precedent.

- **`t=6s` (07:07:22 overlay):** phone shows the **system Bluetooth quick-panel** (not Device
  details), Buds already listed "Active, L:98% R:100%", user's finger near the Buds row. This is
  *before* the log even starts (first log frame `07:07:28.040`) — the originally-documented
  `07:07:23` ANC tap has **no corresponding wire frame at all**, because logging had not started
  yet at that timestamp (the log opens with a full `Sent Reset` HCI init sequence, consistent with
  the prep checklist's "phone rebooted" step).
- **`t=17s` (07:07:33 overlay):** "Pixel Buds Pro 2 van Ted — Connecting…" shown — matches the
  wire's Connection Complete at `07:07:34.091`/`35.111` almost exactly (≤1s offset).
- **`t=40s` (07:07:56 overlay):** Device details screen, "Noise cancellation" tile shows selected,
  user's finger is on the **third tile ("Adaptive")** — this is the real first ANC tap. It matches
  the wire's Set frame at `07:07:57.588` → resulting Notify Current state `0x40` = Adaptive
  (frame 1416). **The originally-documented "Off → Noise cancellation" claim is incorrect on two
  counts**: the mode was already Noise cancellation (not Off), and the tap moved it to Adaptive
  (not Noise cancellation).
- **`t=260s` (07:11:36 overlay):** Device details screen, "Noise cancellation" selected — matches
  the wire's last Notify (frame 5496, Current `0x08`) at `07:11:04`.
- **Video actually ends at ~265.6s (≈07:11:43)**, ~37s before the originally-documented
  `07:12:20` end time. The log itself continues to `07:13:40.082`, but nothing beyond idle BLE
  advertising reports occurs there (checked directly — see §0's tail).

**Corrected Event Timeline is in `CAP-039-EVENT-NOTES.md`** (full table, not duplicated here). In
summary: **6 reconnects** (1 initial + 5 more, chandles `0x0005`–`0x000a`) and **4 ANC Set taps**
alternating Noise cancellation ↔ Adaptive occurred across the session — not the single Set + single
Get the procedure called for. Per this task's brief, this is additional evidence to extract, not a
failed session: it turns the planned 1-Set/1-Get comparison into a 4-Set/6-Get, same-session,
constant-dock-state dataset.

## 3. Central question — OBS-006: `Settable-toggles` byte, Set vs. Get, 10 same-session samples (🟢 FACT for this session)

```
$ tshark -r CAP-039-btsnoop_hci.log -Y "btrfcomm.dlci==4 and btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e bthci_acl.chandle -e data.data \
  | awk -F'\t' '$5 ~ /^0812/ {print "SET   "$0} $5 ~ /^0813/ {print "NOTIFY "$0}'
```

| # | Frame | Time | Trigger | chandle | Raw hex (Notify) | Settable-toggles | Current state |
|---|---|---|---|---|---|---|---|
| Get 1 | 909 | 07:07:39.412 | reconnect (initial) | `0x0005` | `0813000401e8e808` | `0xe8` | `0x08` NC |
| Set 1 | 1416 | 07:07:57.896 | ANC tap (Adaptive) | `0x0005` | `0813000401e8e840` | `0xe8` | `0x40` Adaptive |
| Get 2 | 1967 | 07:08:29.979 | reconnect #2 | `0x0006` | `0813000401e8e840` | `0xe8` | `0x40` |
| Get 3 | 2821 | 07:09:04.459 | reconnect #3 | `0x0007` | `0813000401e8e840` | `0xe8` | `0x40` |
| Set 2 | 3197 | 07:09:16.795 | ANC tap (NC) | `0x0007` | `0813000401e8e808` | `0xe8` | `0x08` |
| Get 4 | 3624 | 07:09:42.873 | reconnect #4 | `0x0008` | `0813000401e8e808` | `0xe8` | `0x08` |
| Set 3 | 4161 | 07:09:57.399 | ANC tap (Adaptive) | `0x0008` | `0813000401e8e840` | `0xe8` | `0x40` |
| Get 5 | 4692 | 07:10:25.080 | reconnect #5 | `0x0009` | `0813000401e8e840` | `0xe8` | `0x40` |
| Set 4 | 5095 | 07:10:43.913 | ANC tap (NC) | `0x0009` | `0813000401e8e808` | `0xe8` | `0x08` |
| Get 6 | 5496 | 07:11:04.662 | reconnect #6 | `0x000a` | `0813000401e8e808` | `0xe8` | `0x08` |

**Result: all 10 occurrences (4 Set-triggered + 6 Get-triggered) read `Settable-toggles = 0xe8`,
with zero exceptions.** The Buds were undocked/worn for the entire session (never redocked,
confirmed by procedure and by this byte never reading `0x00`).

**Conclusion — directly answering OBS-006:** this is strong same-session evidence that
`DECISIONS.md` ADR-024's dock-state reading is **trigger-independent**: whether the Notify is
produced by a user-initiated `Set` (ANC tap) or an app/OS-initiated `Get` (reconnect), the byte
tracks dock state only, not which command triggered it — confirming ADR-024 with **10 samples in
one session** (vs. the originally-planned 2), all in agreement, zero counter-examples. This
strengthens (does not newly promote) ADR-024, which is already 🟢 FACT.

## 4. Bonus: DLCI 0x02 burst is RPC/correlation traffic, not a confirmed settings write (🔴 open, corrects an initial hypothesis)

A ~300-frame `Sent`-direction DLCI 0x02 burst occurs at `07:07:43.76`–`46.90` (frames 1203–1362,
representative), overlapping the mid-session settings-navigation window. Decoded (HDLC-unescape +
CRC-32/IEEE-802.3 verify, splitting each RFCOMM payload on `0x7e` first per
`DESKRESEARCH_FINDINGS.md`'s 2026-08-17 entry):

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
```

```
frame 1228 body: 004b0310151d5d6c251c25c533379a2a06080110001800
  -> after the constant correlation-ID prefix (03 10 15 1d ...): 2a 06 08 01 10 00 18 00
     = field5(len6){ field1=1, field2=0, field3=0 }  -- NOT the field5{field4{...}} settings
       envelope shape (`DECISIONS.md` ADR-013) — a different, flatter 3-field structure.
frame 1265 body: 004b0310151dea71de7e2551aed0ae2a022001
  -> tail: 2a 02 20 01 = field5(len2){ field4=1 }  -- a single incrementing varint, consistent
     with an RPC sequence/correlation counter (the burst's own trailing byte increments
     0x00,0x01,0x02,...,0x20 across ~32 frames in ~3 seconds), not a settings value.
```

**Corrected from an initial working guess:** the burst's rapid, quasi-monotonic value stream
initially looked EQ-slider-shaped (per `PROTOCOL.md` §4.2's live-drag pattern), but the decoded
content does not match the `field5{field4{...}}` write envelope (`ADR-013`) or any known
settings-field shape — it matches `CAP-036-FINDINGS.md` §4's own "connection-settling RPC burst,
undecoded" characterization instead. **Recorded as a clean negative for "this is an EQ write,"**
not forced into that reading, per `AGENTS.md` §13.6. Genuinely unresolved what this burst's exact
purpose is (🔴 open, same open question as `CAP-036`'s own §11/§4, not a new one).

## 5. Bonus: DLCI 0x04 `Group 0x03 Code 0x03` reproduces the existing battery-code candidate, live 98→97 transition (🟡 HYPOTHESIS, strengthens existing item)

```
$ tshark -r CAP-039-btsnoop_hci.log -Y "btrfcomm.dlci==4 and btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time -e data.data | awk -F'\t' '$3 ~ /^030300/'
1140  07:07:42.007  030300036264ff   (repeats identically through frame 5614 @07:11:04.817)
6146  07:13:26.595  030300036164ff   <- changes here
```
`03 03 00 03 <L> <R> ff` = `Group 0x03, Code 0x03, Len 0x0003, value [L, R, 0xff]`. `L=0x62`(98)/
`R=0x64`(100) match the on-screen Left 98%/Right 100% confirmed on video (`t6.png`, `t40.png`,
`t260.png`) throughout the session; at `07:13:26.595` (past the video's own coverage, which ends
~07:11:43) the value changes to `L=0x61`(97), i.e. a live 98→97 Left-earbud drop. This reproduces
`CAP-009-FINDINGS.md` §7's existing 🟡 HYPOTHESIS candidate for `PROTOCOL.md` §4.3 Option B's open
battery code (there identified as `Group 0x03 Code 0x03` on DLCI 0x04) with a second, independent
live-transition data point — not video-confirmed for the transition itself (past video end), but
consistent with the pre-transition values matching on-screen ground truth for the whole video's
duration. Strengthens, does not promote, the existing HYPOTHESIS.

## 6. Test-ID traceability (`AGENTS.md` §13)

- **`OBS-006`** (primary): exercised 10 times (4 Set-triggered, 6 Get-triggered Notify), all
  `Settable-toggles=0xe8`, constant dock state (undocked) — see §3.
- **`ANC`-family** (incidental): 4 Set taps exercised (Noise cancellation ↔ Adaptive), none to Off
  or Transparency this session.
- **`PAIR-003`** (incidental): 6 reconnects to an already-bonded device, 5 of them mid-session
  disconnect/reconnect cycles (4 locally-initiated, 1 remote-terminated) — see §1/§2.

## 7. Conclusions — awaiting maintainer sign-off for anything beyond factual record

**Confirmed by this session's own evidence (factual record, no promotion needed — restates
already-FACT `DECISIONS.md` ADR-024 with new same-session evidence):**
- `Settable-toggles=0xe8` on all 10 Set/Get Notify occurrences in a single session with dock state
  held constant (undocked) — supports ADR-024's dock-state reading as trigger-independent, per
  §3. No counter-example found; ADR-024 does not need revisiting.
- The originally-documented Event Timeline (single Set at `07:07:23` "Off→Noise cancellation",
  single Get at `07:08:16`) is factually incorrect on the mode, the trigger count, and (partially)
  the timestamps — corrected in `CAP-039-EVENT-NOTES.md` with video+wire evidence, per §2.

**Proposed, awaiting maintainer sign-off (not committed, per `AGENTS.md` §6/§15):**
- Fold this session's 10-sample confirmation into `PROTOCOL.md` §4.1's ADR-024 citation as
  additional same-session evidence (not a new ADR — the underlying finding is unchanged).
- `PROTOCOL.md` §4.3 Option B — add this session's `Group 0x03 Code 0x03` 98→97 transition as a
  second data point for the existing 🟡 HYPOTHESIS (§5), still not promotable to FACT (transition
  itself not video-confirmed).

**Recorded as open, not resolved (§6 items, copy into `PROTOCOL.md` §6):**
- 🔴 What triggers the 5 mid-session disconnect/reconnect cycles (reason `0x16`/`0x13`) with no
  clearly camera-visible cause for most of them?
- 🔴 The ~300-frame DLCI 0x02 burst's exact purpose remains undecoded (same open item as
  `CAP-036-FINDINGS.md` §4/§11, reconfirmed not-EQ here).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-039-2026-09-06_07-07-17_07-11-43-Group_AF/CAP-039-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-039-2026-09-06_07-07-17_07-11-43-Group_AF/CAP-039-FINDINGS
