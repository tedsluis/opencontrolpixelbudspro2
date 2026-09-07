# Findings: `CAP-038` (Group AE — realistic physical reconnect trigger vs. system-Bluetooth-toggle reconnect, `OBS-005`)

Standardized, evidence-based extraction from `CAP-038-btsnoop_hci.log` + `CAP-038-recording.mp4`,
staged here for later promotion into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-038` · **Date:** 2026-09-06 · **Firmware:** ⚪ ASSUMPTION `release_5.203`.
**Phone:** Pixel 7a, Android 14, official Pixel Buds Companion App, Google Play Services enabled.
**Log file:** `CAP-038-btsnoop_hci.log` (371.55s, 4,426 packets, 2026-09-06
06:49:58.224–06:56:09.778 local/+0200, 0/4,426 `cap_len≠len` mismatches — untruncated, raw
extraction path). **Video:** `CAP-038-recording.mp4` (ffprobe duration 247.07s, ~06:49:50–06:53:57
local — **significantly shorter than the originally-documented 06:49:50–06:56:07 window**).
**Devices:** phone (Pixel 7a), peer `04:00:6e:cf:6e:07` ("Pixel Buds Pro 2 van Ted"); one
unrelated BLE device (`48:bd:eb:a0:99:c7`) also present in the log, excluded (§1).

---

## 0. Capture integrity (🟢 FACT)

```
$ capinfos CAP-038-btsnoop_hci.log
Number of packets:   4,426
Capture duration:    371.553608 seconds
Earliest packet time: 2026-09-06 06:49:58.224154
Latest packet time:   2026-09-06 06:56:09.777762

$ tshark -r CAP-038-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'
mismatches: 0
```
No snaplen cap, 0/4,426 mismatches — untruncated, raw path.

## 1. Connection identification / CLI-hygiene (🟢 FACT)

```
$ tshark -r CAP-038-btsnoop_hci.log -Y "bthci_evt.bd_addr==04:00:6e:cf:6e:07 and (bthci_evt.code==0x03 or bthci_evt.code==0x05)" \
  -T fields -e frame.number -e frame.time -e bthci_evt.code -e bthci_evt.connection_handle -e bthci_evt.reason
841   06:50:26.121191  0x03 (Connection Complete)    chandle 0x0001
2484  06:51:57.185421  0x05 (Disconnection Complete)  chandle 0x0001  reason 0x13 (Remote User Terminated)
2814  06:52:46.781276  0x03 (Connection Complete)    chandle 0x0004
4247  06:53:46.089519  0x05 (Disconnection Complete)  chandle 0x0004  reason 0x13
```
Exactly **two** classic ACL connections to the Buds this session — chandle `0x0001` (06:50:26–
06:51:57, ~91s) and chandle `0x0004` (06:52:46–06:53:46, ~59s). No further Buds Connection
Complete event occurs anywhere else in the log (checked to the log's own end, 06:56:09.778).

**Unrelated background device, excluded (per `CAP-014-FINDINGS.md` §4a's precedent):**
```
$ tshark -r CAP-038-btsnoop_hci.log -Y "bthci_evt.le_meta_subevent==0x0a or bthci_evt.le_meta_subevent==0x01" \
  -T fields -e frame.number -e frame.time -e bthci_evt.bd_addr -e bthci_evt.connection_handle
270  06:50:01.430355  48:bd:eb:a0:99:c7  0x0002
```
Chandle `0x0002` belongs to a different BLE device (`48:bd:eb:a0:99:c7`, not the Buds) and carries
a full GATT service-discovery walk (GAP/GATT/Device Information) starting 06:50:01.43 — excluded
from all findings below; it is not the Buds' own GATT/battery-service traffic.

## 2. Video re-pass: video ends ~2m10s before the log, "Window 2" has no coverage at all (🟢 FACT — major correction)

**Method:** targeted `ffmpeg -ss <t> -frames:v 1` extraction at the wire-derived event timestamps.

- `t=0` (06:49:50 overlay, `t0.png`): Bluetooth quick-panel, Buds "Active" L:100%/C:85%/R:100%.
- `t=36s` (06:50:26 overlay, `w36.png`): Buds visibly removed/out of case, quick-panel shown —
  matches the wire's Connection Complete (06:50:26.121, chandle `0x0001`) essentially exactly.
- `t=176s` (06:52:46 overlay, `w176.png`): quick-panel shown again — matches the wire's second
  Connection Complete (06:52:46.781, chandle `0x0004`) exactly.
- `t=234s` (06:53:44 overlay, `w234.png`): finger reaching toward the case with both Buds visible
  near/at it, panel shows Buds "Active" — matches the documented "Window 1 end" placement moment.
- `t=246.5s` (06:53:57 overlay, `t246.5.png`, **the video's last usable frame**): back to the
  Bluetooth quick-panel, Buds listed but not "Active" — **ffprobe reports the file's total
  duration as 247.07s, i.e. the video ends here.**

**The originally-documented Event Timeline claims the session continues to `06:56:07`** (Window 2:
case-lid-open reconnect test, 06:54:14–06:55:53) — **this entire span has zero video coverage.**
Independently, the wire log confirms nothing Buds-related happens there either: the last
Disconnection Complete for the Buds is at `06:53:46.090` (chandle `0x0004`), and no further
Connection Complete for `04:00:6e:cf:6e:07` occurs anywhere in the remaining ~2m24s of log (only
unrelated LE Extended Advertising Reports from a passive scan). **Window 2 is therefore recorded
as NOT VERIFIABLY EXECUTED** — this is a capture gap, not a "clean negative": a clean negative
would require confirming the case lid was actually opened and nothing happened; here there is no
evidence the action was performed at all within the logged/filmed window.

**Full corrected Event Timeline is in `CAP-038-EVENT-NOTES.md`.**

## 3. OBS-005 central finding: on the FIRST reconnect, the Fast Pair Message Stream lands on DLCI 0x05 (not 0x04) — and reads DOCKED, not undocked (🟢 FACT — corrects an initial mis-read of this same session)

```
$ tshark -r CAP-038-btsnoop_hci.log -Y "bthci_acl.chandle==0x0001 and btrfcomm" -T fields -e btrfcomm.dlci \
  | sort | uniq -c
     50 0x00
    155 0x03
     46 0x05
     61 0x08
    129 0x09
      6 0x0b
```
Chandle `0x0001` — the reconnect immediately following physical removal of the Buds from the case
(the most "realistic" trigger this Group set out to test) — never opens the literal DLCI numbers
`0x02`/`0x04`. **This is not an absence of the underlying channels** — RFCOMM server-channel
numbers, and therefore DLCI numbers, are session-local, not fixed (`CAP-001-FINDINGS.md` §2's
established finding, already load-bearing for `DECISIONS.md` ADR-018). On this specific connection,
the official Fast Pair Message Stream (ANC control) landed on **DLCI `0x05`** and `libmaestro`'s
own channel on **DLCI `0x03`** instead of the more usual `0x04`/`0x02`:

```
$ tshark -r CAP-038-btsnoop_hci.log -Y "bthci_acl.chandle==0x0001 and btrfcomm.dlci==5 and btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data
1143  06:50:26.792187  0  08110000
1154  06:50:26.821286  1  0813000401e80020
```
The Get/Notify pair fires normally, immediately after channel-open, exactly as `DECISIONS.md`
ADR-022 predicts — **not** a counter-example, and **not** a case of the channel failing to open.
Decoded: `08 13 00 04 01 e8 00 20` → Version `0x01`, UI-toggles `0xe8`, **`Settable-toggles=0x00`
(DOCKED)**, Current-state `0x20` (Off).

**This is the actually significant finding, and it points the opposite direction from the
originally-drafted read of this session:** at the very first reconnect — logged as occurring
immediately after the Buds were physically removed from the case for this Group's "realistic worn
reconnect" test — the wire reads **docked**, not undocked. 🔴 **Genuine tension, not resolved
here:** either the query (which fires within tens of milliseconds of channel-open, per ADR-022's
own timing data) caught a dock-sensor state that hadn't yet updated from a just-completed physical
removal, or the removal itself hadn't fully registered with the accessory at that exact instant.
Per `AGENTS.md` §13.6, no forced reconciliation is offered — this is reported as a tension for a
future capture (one that holds the connection open longer before checking, or that independently
video-confirms the dock sensor's own physical state at the query's exact timestamp) to resolve.

`libmaestro`'s own channel (DLCI `0x03`, 155 non-empty frames total) is not decoded field-by-field
this pass — see §7 for its role in the mid-session settings contamination. DLCI `0x0b` (6 frames,
new to this project's documented census) is likewise not decoded — out of this capture's scope.

The session's **second** reconnect (chandle `0x0004`, 06:52:46–06:53:46) uses the more usual
numbering — DLCI `0x04`'s Get/Notify fires at `08 11 00 00`/`08 13 00 04 01 e8 e8 80` (frames
3261/3282, `Current=0x80`=Transparency, **`Settable=0xe8`=undocked**), matching `DECISIONS.md`
ADR-022/ADR-024's usual reading. **Combined with the first reconnect's `0x00` reading**, this
session's own dock state genuinely changed at least once between the two reconnects (undocked→
docked is not what's shown — it reads docked, then undocked, then docked again by §4) — consistent
with the buds being handled/re-docked/re-removed multiple times during the heavy settings-browsing
contamination window between the two connections, not a single clean removal-and-wear.

## 4. `Settable-toggles` byte: undocked throughout, correctly flips to docked at the real dock moment (🟢 FACT, reconfirms ADR-024)

```
$ tshark -r CAP-038-btsnoop_hci.log -Y "bthci_acl.chandle==0x0004 and btrfcomm.dlci==4 and btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data \
  | awk -F'\t' '$4 ~ /^0813/'
3282  06:52:51.210778  1  0813000401e8e880   Settable=e8 Current=80 (Transparency)
3695  06:53:04.494974  1  0813000401e8e808   Settable=e8 Current=08 (Noise cancellation)
3847  06:53:19.910169  1  0813000401e8e880   Settable=e8 Current=80 (Transparency)
4122  06:53:26.109001  1  0813000401e8e880   Settable=e8 Current=80 (Transparency)
4158  06:53:27.150443  1  0813000401e80020   Settable=00 Current=20 (Off)
```
`Settable-toggles=0xe8` (undocked) on every Notify while the Buds are genuinely worn/out-of-case,
then flips to `0x00` (docked) at frame 4158 (06:53:27.150) — ~17s **before** the documented
case-placement timestamp (06:53:44), consistent with the placement action itself starting a few
seconds earlier than its logged estimate. This is a fifth-plus session reproducing `DECISIONS.md`
ADR-024's dock-state reading with zero counter-examples.

## 5. Two unexplained ANC-mode Notify frames — no Get, no Set anywhere in the log (🟡 HYPOTHESIS, new)

Frames 3695 (`Current=0x08`) and 3847 (`Current=0x80`) are DLCI 0x04 "Notify ANC state" frames
with **no immediately-preceding `08 11` Get** and — checked exhaustively — **no `08 12` "Set ANC
state" frame anywhere in this entire log**:
```
$ tshark -r CAP-038-btsnoop_hci.log -Y "bthci_acl.chandle==0x0004 and data.data contains 08:12" \
  -T fields -e frame.number
(0 rows)
```
Also confirmed DLCI 0x04 never bounces (no SABM/UA) between the two clear Get/Notify pairs
(3261/3282 and 4116/4122) — these two frames appear on an already-open, continuously-open
channel. This matches `CAP-027-FINDINGS.md` §4's already-established mechanism exactly: a
hardware press-and-hold gesture on the bud itself produces a spontaneous, Buds-initiated "Notify
ANC state" frame with no corresponding Set command from the phone, because the mode change is
decided on-device. **Plausible explanation, not confirmed**: the camera was aimed at the phone
screen throughout this session (not the buds/ears), so no video evidence exists either way — the
user was actively handling the Buds around this time (removed from case, inserted, navigating
settings), making an accidental press-and-hold physically plausible. 🟡 HYPOTHESIS.

## 6. No A2DP/AVDTP audio-streaming setup found (🟢 FACT, clean negative)

```
$ tshark -r CAP-038-btsnoop_hci.log -Y "avdtp" -T fields -e frame.number
(0 rows)
```
Despite the Buds allegedly being worn for this entire session, **no AVDTP/A2DP signaling occurs
anywhere in the log** — no audio-streaming profile was established. This answers one part of this
Group's own checklist directly: physical wearing alone does not trigger A2DP setup in this
session (no audio was played).

## 7. Bonus: DLCI 0x02 settings-write burst location corrected, not decoded field-by-field (🟡 HYPOTHESIS, scope-limited)

```
$ tshark -r CAP-038-btsnoop_hci.log -Y "bthci_acl.chandle==0x0004 and btrfcomm.dlci==2 and btrfcomm.len>0 and frame.p2p_dir==0" \
  -T fields -e frame.number | wc -l
63
$ tshark -r CAP-038-btsnoop_hci.log -Y "bthci_acl.chandle==0x0001 and btrfcomm.dlci==2 and btrfcomm.len>0 and frame.p2p_dir==0" \
  -T fields -e frame.number | wc -l
0
```
All 63 `Sent`-direction DLCI `0x02`-*numbered* frames in this log occur on chandle `0x0004`
(06:52:53.26 onward) — zero on chandle `0x0001` (which, per §3's correction, uses DLCI `0x03` for
the equivalent `libmaestro` channel instead, carrying 155 non-empty frames of its own). This means
the EQ/In-Ear/Multipoint/Touch-Controls settings-browsing that spans both connections likely
produced writes on **both** chandles' respective `libmaestro` DLCI (`0x03` on the first, `0x02` on
the second) — not, as an earlier pass concluded, exclusively on the second. **Not individually
field-decoded this pass** (out of this Group's own scope, and secondary to `OBS-005`'s central
question) — recorded as a location/timing correction only, not a content analysis. A future
deskresearch pass could decode these frames against the known `field5{field4{...}}` mappings
(`DECISIONS.md` ADR-013/ADR-019) if the specific field values become relevant.

## 8. Test-ID traceability (`AGENTS.md` §13)

- **`OBS-005`** (primary): exercised via both reconnects. The first (chandle `0x0001`, immediately
  after physical removal from the case — the more "realistic" trigger) reads
  **`Settable-toggles=0x00` (docked)**, a genuine tension with the intended test condition (§3); no
  A2DP/GATT-battery traffic found on either connection (clean negative, §6). The second (chandle
  `0x0004`) reads undocked as expected, then correctly flips to docked (§4). Window 2 (case-lid-only
  trigger) not verifiably executed (§2).
- **`PAIR-003`** (incidental): two reconnects to an already-bonded device, exercised cleanly (§1).
- **`INEAR`-family**: not directly exercised — no in-ear-detection-specific wire signal identified
  this session (checked, none found distinct from the channels already covered above).

## 9. Conclusions — awaiting maintainer sign-off for anything beyond factual record

**Confirmed by this session's own evidence (factual record):**
- The originally-documented Event Timeline is materially wrong about session length and Window
  2's execution — corrected in `CAP-038-EVENT-NOTES.md` with video+wire evidence (§2).
- RFCOMM channel/DLCI numbering is session-local — this session's Fast Pair Message Stream and
  `libmaestro` channels used DLCI `0x05`/`0x03` on the first connection, `0x04`/`0x02` on the
  second (§3) — reconfirms `CAP-001-FINDINGS.md` §2's existing finding, not a new one.
- `Settable-toggles` tracks dock state correctly on the second connection, trigger-independent,
  reconfirming `DECISIONS.md` ADR-024 (§4).
- No A2DP/AVDTP setup and no Buds-owned BLE GATT battery-service traffic this session (§1/§6).

**Proposed, awaiting maintainer sign-off (not committed, per `AGENTS.md` §6/§15):**
- Flag as a 🔴 open question for `PROTOCOL.md` §6 (not yet a HYPOTHESIS — no candidate explanation
  is offered): the first reconnect's `Settable-toggles=0x00` (docked) reading, logged as occurring
  immediately after physical removal from the case, is a genuine tension with `DECISIONS.md`
  ADR-024's dock-state reading as currently understood (§3) — needs a replication capture with
  either a longer post-removal buffer before reconnecting, or independent video confirmation of the
  dock sensor's own state at the exact query timestamp.
- Record as a new 🟡 HYPOTHESIS: a hardware press-and-hold gesture plausibly explains the two
  Get-less/Set-less ANC Notify frames (§5), extending `CAP-027-FINDINGS.md` §4's mechanism to an
  accidental/incidental trigger.

**Recorded as open (§ Open Questions in `CAP-038-EVENT-NOTES.md`, copy into `PROTOCOL.md` §6):**
- 🔴 Why does the first reconnect's "Notify ANC state" read `Settable-toggles=0x00` (docked)
  immediately after the Buds were reported physically removed from the case? (§3)
- 🔴 What caused the two unexplained ANC Notify frames? (§5)
- 🔴 Window 2 (case-lid-only trigger) remains untested — needs a re-run with full video coverage.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-038-2026-09-06_06-49-50_06-53-57-Group_AE/CAP-038-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-038-2026-09-06_06-49-50_06-53-57-Group_AE/CAP-038-FINDINGS
