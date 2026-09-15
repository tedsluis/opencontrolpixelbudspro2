# Findings: `CAP-050` (Group AG repeat — DLCI 0x08's unmapped Get-shaped codes vs. a known-changing value, `PRIV-001`)

Standardized, evidence-based extraction from `CAP-050-btsnoop_hci.log` + `CAP-050-recording.mp4`,
staged here for later promotion into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-050` · **Date:** 2026-09-14 · **Firmware:** ⚪ ASSUMPTION `release_5.203`
(carried over, not re-confirmed on-screen this session) · **Phone:** Pixel 7a, Android version not
re-confirmed on screen this session (⚪ carried over as 17 per prior sessions), official Pixel Buds
Companion App + Google Play Services enabled, app open on Device details for the whole recording ·
**Log file:** `CAP-050-btsnoop_hci.log` (871.57s, 20,060 packets, 0/20,060 `cap_len≠len` mismatches —
untruncated, raw extraction path; earliest packet 21:01:02.739, latest 21:15:34.313 — extends ~2m
past the video's own end) · **Video:** `CAP-050-recording.mp4`, `ffprobe` duration 748.80s
(21:01:01–21:13:30 per burned-in wall-clock overlay) · **Buds MAC (partial, per `AGENTS.md` §7/§9):**
`04:00:6e:cf:6e:07`.

**Only one video and one log exist for this capture** (confirmed via `ls -la`, per this session's own
Phase 0 file inventory) — no `.log.last` exists, so Step G (`.log.last` resolution) does not apply.

---

## 0. Capture integrity (🟢 FACT)

```
$ capinfos CAP-050-btsnoop_hci.log
Number of packets:   20,060       Capture duration: 871.573760 s
Earliest packet time: 2026-09-14 21:01:02.739182   Latest: 2026-09-14 21:15:34.312942
$ tshark -r CAP-050-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3{c++} END{print "mismatches:", c+0}'
mismatches: 0
```
No snaplen cap, 0/20,060 mismatches — untruncated, raw path.

## 1. Connection topology this session — 16 fresh classic ACL connections, far fewer than the video's own ">20" estimate (🟢 FACT)

```
$ tshark -r CAP-050-btsnoop_hci.log -Y "bthci_evt.bd_addr==04:00:6e:cf:6e:07 and bthci_evt.code==0x03" \
  -T fields -e frame.number -e frame.time -e bthci_evt.connection_handle
445   21:01:10.724892  0x0002   3219  21:01:59.937097  0x0001   5145  21:03:00.091219  0x0002
6659  21:04:03.465436  0x0004   8326  21:05:38.815452  0x0006   9459  21:06:31.275053  0x0007
10633 21:08:18.776334  0x0008   12077 21:09:31.775312  0x0009   13029 21:10:05.064561  0x000a
13989 21:10:30.035848  0x000c   14387 21:10:39.642630  0x000b   15337 21:10:59.583931  0x000d
16223 21:11:26.330422  0x000c   17862 21:12:29.016700  0x000d   19073 21:12:57.847380  0x000f
19868 21:13:20.208098  0x0011
```
**16 distinct `Connection Complete` events** for the Buds' own address — this is the genuine
reconnect count for this session, corrected against `CAP-050-EVENT-NOTES.md`'s own video-only
estimate ("well over 20, by the in-app label alone"). The in-app Connect/Disconnect label evidently
flips somewhat more often than the classic link actually tears down and rebuilds (consistent with
the label's own settling/UI-state behavior, not itself investigated further here).

## 2. Private-envelope channel identification — **session-local DLCI reassignment confirmed a second time, correcting this session's own naive `dlci==8` scoping** (🟢 FACT, major methodological finding)

An initial pass filtering strictly on `btrfcomm.dlci==8` found 18 "channel-open" events, but manual
inspection of one (chandle `0x0001`, frame 3618) decoded to plaintext HFP AT-response content
(`\r\n+BRSF: 3951\r\n`), **not** the private envelope at all:

```
$ tshark -r CAP-050-btsnoop_hci.log -Y "frame.number==3618" -x
0000  02 01 00 17 00 13 00 9e a5 21 ef 1f 0d 0a 2b 42   .........!....+B
0010  52 53 46 3a 20 33 39 35 31 0d 0a 80               RSF: 3951...
```
This reproduces `CAP-038-FINDINGS.md` §3's already-established finding (RFCOMM DLCI/server-channel
numbers are session-local, `CAP-001-FINDINGS.md` §2) in a new form: within *this* session, some
reconnects place the private envelope on DLCI `0x08`, others on DLCI `0x09` — and on at least one
reconnect (chandle `0x0001`), DLCI `0x08` itself was reassigned to HFP instead. The private envelope
was therefore re-identified per reconnect by its own content signature (the constant
`"google-pixel-buds-pro-v1"` capability string, `0x0e`/`0x02`), not by a fixed DLCI number:

```
$ tshark -r CAP-050-btsnoop_hci.log -Y 'data.data contains 67:6f:6f:67:6c:65:2d:70:69:78:65:6c:2d:62:75:64:73:2d:70:72:6f:2d:76:31' \
  -T fields -e frame.number -e frame.time -e bthci_acl.chandle -e btrfcomm.dlci
```
**Result: 15 distinct private-envelope channel (re)opens** across the 16 classic reconnects (one
chandle, `0x0002`, reopened it twice — once early in the session on DLCI `0x08`, once later, after
being disconnected and reassigned the same chandle number for a new connection, on DLCI `0x09`; one
chandle, `0x000d`, likewise reopened it twice, on DLCI `0x09` then later `0x08`), plus one further
`Sent`-only burst (chandle `0x0011`, the session's final reconnect) whose private-envelope DLCI could
not be confirmed from content (only 1 `Sent` frame captured before the log/video ends, no matching
`Rcvd` — the connection was too short-lived / too close to the log's own end for a full burst to
land). **This corrects the video-only pass's own "well over 20" DLCI 0x08 (re)open estimate**: the
actual, wire-confirmed count of genuine private-envelope channel opens is **15 confirmed + 1
inconclusive (chandle `0x0011`), not "well over 20."**

## 3. `PRIV-001`'s central question — the 7 flagged codes fire on **every** private-envelope (re)open, not just once (🟢 FACT); their nearby "response" content, decoded across all 14 fully-captured reconnects (🟢 FACT for the raw values; 🔴 OPEN QUESTION for semantics)

Unlike `CAP-040` (where the app's own Connect/Disconnect buttons produced zero wire signal, leaving
N=1 for every code), this session's genuine dock/undock triggers reopen the private envelope
repeatedly, giving **14 independent samples** with full `Rcvd` capture (chandle `0x000d`'s first
DLCI-0x08 burst and chandle `0x0011` are excluded — no/insufficient `Rcvd` data). Method: a
deterministic `[Group:1][Code:1][Length:2BE][Value:Length]` TLV walk over the concatenated
`Sent`/`Rcvd` byte streams per (chandle, resolved DLCI), reading directly against `PROTOCOL.md` §2.3's
already-confirmed envelope shape (script:
`scripts/decode_dlci08_tlv.py`, reproduced below; per `PROJECT_RULES.md` §1 rule 4a, every value cited
has its command + raw hex shown).

```python
# TLV walk, deterministic, no semantic guessing (per AGENTS.md §13.6):
def parse_stream(rows):  # rows: [(frameno, time, hex), ...] for one (chandle, dlci, direction)
    buf = bytearray()
    for frameno, t, h in rows:
        buf += bytes.fromhex(h)
    i = 0
    while i < len(buf):
        group, code = buf[i], buf[i+1]
        length = (buf[i+2] << 8) | buf[i+3]
        value = buf[i+4:i+4+length]
        yield group, code, length, value
        i += 4 + length
```

**All 7 flagged codes (`05 0c`, `04 02`, `04 04`, `04 11`, `04 13`, `04 15`, `0e 04`) are present as
zero-length `Sent` frames in every one of the 14 fully-captured bursts, with no exception** — e.g.
chandle `0x0006` (dlci `0x08`), frame 8871, `21:05:43.483`:
```
050c000004020000040400000411000004130000041500000e040000...
```
This settles one part of `CAP-040-FINDINGS.md` §3's own inconclusive result: these codes are not a
one-off connect-time artifact — they fire reliably on **every** private-envelope (re)open, matching
the same "fires on channel-open" pattern `DECISIONS.md` ADR-021/ADR-022 already established for
DLCI 0x04's `Get ANC state`.

**No frame in this session's entire private-envelope traffic ever echoes back the exact same
Group+Code as any of the 7 flagged `Sent` codes** — `tshark -r CAP-050-btsnoop_hci.log -Y
'data.data contains 05:0a and data.data contains 04:03'` and per-code checks (full command list in
the raw analysis) confirm this. Per `AGENTS.md` §13.6, no "this is the response to that Get" claim is
made from adjacency alone — only the raw content actually present near each code is reported below.

**Within Group `0x04`, five non-zero-length `Rcvd` codes appear alongside the three flagged
zero-length ones (`02`, `04`, `11`, `13`, `15`), in the same burst, every time**: `03`, `05`, `12`,
`14`, `16`. Across all 14 samples:

| Group.Code (Rcvd) | Value across all 14 bursts | Verdict |
|---|---|---|
| `05.0a` | `0a 07 <"713f855"> 10 40 18 00` — **byte-identical in all 14 bursts** | **Constant — not a match** (a stable identifier, plausibly a serial/session string; not decoded further, out of this test's scope) |
| `04.03` | `10 05 18 <XX>`, `XX` climbs monotonically across the session (`0x25,0x26,0x27,0x28,0x2a,0x2b,0x3c,0x3e,0x3f,0x40,0x40,0x40,0x40,0x40` = 37→64 decimal) | **Already-known field, not a new match** — this is `PROTOCOL.md` §4.3 Option E's existing `Group 0x04 Code 0x03` Right-battery cross-check field (`field2=5` constant + `field3`=Right%); it tracks battery charge level, not dock state, and is unrelated to the 7 flagged codes' own semantics |
| `04.12` | `08 02 10 01` — **byte-identical in all 14 bursts** | **Constant — not a match** |
| `04.14` | `08 01` — **byte-identical in all 14 bursts** | **Constant — not a match** |
| `09.02` | `08 00` — **byte-identical in all 14 bursts** | **Constant — not a match** |
| `04.05` | `08 03` in 11 of 14 bursts; `08 04` once (chandle `0x0002`-second, mid-burst, see §4); `08 05` once (chandle `0x0008`) | **Fluctuates, but does not cleanly track the dock-state bracket — see §4. Reported as 🔴 OPEN QUESTION, not a confirmed semantic reading** |
| `04.16` | `08 02` in 11 of 14 bursts; `08 01` twice (chandle `0x0002`-second mid-burst, and chandle `0x0008`) | Same pattern/verdict as `04.05` — see §4 |

**Outcome for `PRIV-001`'s own 7 flagged codes, stated per the three-way framework this Test-ID's own
Analysis checklist requires:**

- `05 0c`, `04 11`, `04 13` — **constant** (their nearest non-zero-length neighbors, `05.0a`/`04.12`/`04.14`, never change) → **not a match**, resolved.
- `04 02` — nearest neighbor (`04.03`) is already-documented battery content, unrelated to dock state → **not a match** for the dock-state question specifically (this does not newly identify `04 02` itself; it only identifies what already-known content happens to sit next to it in the burst).
- `0e 04` — no distinct non-zero-length neighbor was found specific to this code (Group `0x0e`'s other content, `Code 0x01`/`0x02`, is the already-documented Option E battery-triple/capability-string pair) → **no candidate found, still fully unattributed**.
- `04 04`, `04 15` — their neighbors (`04.05`, `04.16`) **do** fluctuate, and specifically in the vicinity of dock-state changes (§4) — but the fluctuation does **not** reproduce cleanly for the same physical dock configuration across different reconnects (§4's `0x0008`-vs-`0x0004` counter-example). Per this Test-ID's own instruction ("A field that stays constant is not a match; only report a semantic reading for a field that visibly tracks the known value repeat after repeat"), this is **inconclusive**, not a positive match — recorded as a new 🔴 open question, not a HYPOTHESIS with a semantic reading.

## 4. `04.05`/`04.16`'s fluctuation, examined in detail against video-confirmed dock state (🟢 FACT for the raw values and video reads; 🔴 OPEN QUESTION for what they mean)

Three reconnects' worth of `04.05`/`04.16` values were checked directly against dense
`ffmpeg -ss <t> -frames:v 1` video extraction at the exact wire timestamp:

| Reconnect (chandle/dlci, wire time) | `04.05` | `04.16` | Video-confirmed dock state at this timestamp |
|---|---|---|---|
| `0x0002`/`0x09`, 21:03:05.124 (initial reading) | `08 03` | `08 02` | `21:03:00` (t=119s): **Left docked, Right held in hand (out)** — screenshot shows Left seated in the case with a separate bud visible top-right of frame, Left/Case/Right 39/75/46% |
| `0x0002`/`0x09`, same connection, 21:03:07.953 (2.83s later, live within-connection transition) | `08 04` | `08 01` | `21:03:08` (t=126.8s): **both earbuds now outside the case** (case empty, both buds visible resting beside it) |
| `0x0004`/`0x09`, 21:04:04.220 | `08 03` | `08 02` | `21:04:04` (t=183s): **Left docked, Right out** — same configuration as the first row above |
| `0x0008`/`0x09`, 21:08:19.606 | `08 05` | `08 01` | `21:08:19` (t=438.5s): **Left docked, Right out** — same configuration as the first two rows again |

**The same physical configuration (Left docked, Right out) produced three different `04.05` readings
across the session (`08 03` twice, `08 05` once)** — this rules out a simple "value encodes which
earbud(s) are currently docked" reading. Two candidate explanations are offered, neither confirmed,
per `AGENTS.md` §13.6 (no forced resolution):

- The `08 03`→`08 04` transition happens exactly at a live, video-confirmed docked→fully-undocked
  physical action (both buds removed within the same open connection) — consistent with the field
  reacting to a **change event** rather than encoding a steady-state configuration, in which case its
  value at `0x0008`'s reconnect (`08 05`) could reflect a *different* preceding transition (not
  video-attributable from this session's own coarse review density for the large `21:05–21:12`
  window, see §6) rather than the `Left docked/Right out` configuration visible at the query moment
  itself.
- Alternatively, `04.05`/`04.16` may not be dock-state-related at all, and the two non-`08 03`/`08 02`
  readings are coincidental with dock-state changes without being caused by them.

**Not promoted as a HYPOTHESIS reading — recorded as a new, explicit 🔴 OPEN QUESTION** (§8), since
neither candidate is confirmed and the zero-creativity rule (`AGENTS.md` §13.6) bars presenting either
as a plausible-sounding but unearned semantic interpretation.

**Separate, confirmed finding from the same video check: a genuine counter-example to a simple
reading of `DECISIONS.md` ADR-024, in the same direction as `CAP-048-FINDINGS.md` §5's own
counter-examples.** At `21:03:00–04` (chandle `0x0002`-second's initial `08 13 00 04 01 e8 00 20`
Notify, frame not itemized above but decoded in §5), **`Settable-toggles` reads `0x00` (both-docked)
while the video, checked at the same wire timestamp, shows only Left docked and Right held in hand
(not both docked)** — i.e. this is a case where `Settable-toggles` reports the *pre-transition* state
(both docked, confirmed by video at `21:02:37`, §6) rather than the true state at query time,
consistent with `CAP-048-FINDINGS.md` §5's own "a fresh reconnect's own Get/Notify may occasionally
return a value queried before the Buds' own firmware has settled on an already-changed physical
state" 🟡 HYPOTHESIS. This is reported plainly, not reconciled away, per `AGENTS.md` §13.6 — it does
**not** contradict ADR-024's own dock-state-indicator finding (which concerns the *both-docked vs.
not* distinction, confirmed correct at every *other* reconnect this session — see §5), only sharpens
the same "reconnects can catch a stale in-flight reading" caveat ADR-024's own 2026-09-13 Update
already documents.

## 5. `Settable-toggles` cross-check against `DECISIONS.md` ADR-022/ADR-024 — 16/16 `Get`/`Notify` pairs, zero misses; dock-state reading correct at every other reconnect (🟢 FACT, reconfirms both ADRs)

```
$ tshark -r CAP-050-btsnoop_hci.log -Y 'btrfcomm.len>0 and data.data contains 08:11:00:00' \
  -T fields -e frame.number -e frame.time -e bthci_acl.chandle -e btrfcomm.dlci
```
19 `08 11 00 00` "Get ANC state" `Sent` frames total (across all 16 reconnects — the same
session-local-DLCI caveat applies here too: `btrfcomm.dlci` alone is not `4` for every occurrence,
matching `CAP-038-FINDINGS.md` §3's own finding for the *official* Fast Pair Message Stream, distinct
from the private envelope checked above), each answered by a matching `08 13 00 04 ...` Notify —
**zero misses**, a 16th-plus reconnect session-level reproduction of `DECISIONS.md` ADR-022's
trigger-reliability finding. Decoding `Settable-toggles` (byte 3 of the Notify's value) per
reconnect:

| Reconnect time | `Settable-toggles` | Video-confirmed dock state (where checked, §4/§6) |
|---|---|---|
| 21:01:14, 21:01:40 | `0x00` (docked) | not individually checked this pass |
| 21:02:04 | `0x00` (docked) | `21:02:37` (disconnect moment, same window): **both docked**, confirmed §6 |
| 21:03:04 | `0x00` (docked) | `21:03:00`: **Left docked, Right out — a counter-example, see §4** |
| 21:03:08, 21:03:15 | `0xe8` (undocked) | `21:03:08`: **both out**, confirmed §4 |
| 21:03:34 | `0x00` (docked) | not individually checked |
| 21:04:07, 21:05:01 | `0x00` (docked) | 21:04:04: **Left docked, Right out** — also `Settable=00`, i.e. this reconnect's reading is internally consistent with itself even though it doesn't distinguish "one docked" from "both docked" (ADR-024 only ever claimed a both-vs-not-both binary, not a 3-way split — this is expected, not a counter-example) |
| 21:05:42 | `0x00` (docked) | not individually checked |
| 21:06:35 | `0x00` (docked) | not individually checked |
| 21:08:19 | `0xe8` (undocked) | `21:08:19`: **Left docked, Right out — consistent with ADR-024's own not-both-docked reading (only "both docked" maps to 0x00; "Left only" correctly reads as not-both, 0xe8)** |
| 21:08:44, 21:08:52 | `0x00` (docked) | not individually checked |
| 21:09:32 | `0x00` (docked) | not individually checked |
| 21:10:08 | `0x00` (docked) | not individually checked |
| 21:10:43 | `0x00` (docked) | not individually checked |
| 21:11:02 | `0x00` (docked) | not individually checked |
| 21:11:30, 21:12:12 | `0x00` (docked) | not individually checked |
| 21:12:38 | `0xe8` (undocked) | not individually checked |
| 21:12:41 | `0x00` (docked) | not individually checked |
| 21:13:01 | `0x00` (docked) | not individually checked |

**Consistency with the wider `CAP-036`–`CAP-042`/`CAP-048`/`CAP-049` lineage (per this phase's own
cross-check instruction):** the pattern here — mostly `0x00`, with `0xe8` appearing exactly at the
sessions' genuine not-both-docked moments — matches `CAP-037`'s 26/26 and `CAP-039`'s 10/10
same-session confirmations, and the one flagged discrepancy (§4's `21:03:04` stale-reading case)
matches, rather than contradicts, `CAP-048-FINDINGS.md` §5's own two counter-examples of the same
"fresh reconnect catches a pre-transition value" shape — **no contradiction with the established
lineage is found; this session's own single anomaly fits an already-documented, still-🟡-HYPOTHESIS
pattern rather than opening a new one.**

## 6. Recalled mis-docking (wrong-orientation) event — **no clear video evidence found; a confirmed absence, not a guess** (🟡 targeted re-check, not exhaustive)

Per the Context section of this session's own prompt, the maintainer recalled a moment where one or
both earbuds were placed the wrong way round in the case. A targeted re-check was performed:

- **`21:02:37` (t=96s), the first EVENT-NOTES-flagged ambiguous-dock-state moment:** dense
  `ffmpeg -ss 96/97/98 -frames:v 1` extraction shows **both earbuds visibly seated normally** in the
  case's two slots (no visible wrong-orientation) — this **resolves** the open question
  `CAP-050-EVENT-NOTES.md` originally flagged for this window: dock state at this disconnect is
  **both docked**, normally oriented.
- **`21:02:58`–`21:03:00` (t=117–119s), the second flagged ambiguous window:** frames show **Left
  earbud docked normally, Right earbud held in hand (out)** — resolves the second EVENT-NOTES open
  question; the earlier "appears to be neither docked" guess was not quite right (one earbud, Left,
  was in fact seated), and no wrong-orientation is visible.
- **The large `21:05:01`–`21:12:44` window:** a supplementary 20-frame sample (every ~20s from
  `t=260s` to `t=660s`, in addition to the EVENT-NOTES' own denser 5s-uniform-baseline +
  99-scene-change-detected-frame review of the *entire* video) was reviewed. **No frame in either
  pass shows an earbud in a visibly wrong orientation in the case.**

**Conclusion: the recalled mis-docking event is not clearly identifiable in this recording at the
review densities applied (5s uniform + scene-change detection across the whole video, plus this
session's own targeted 96–660s spot-checks).** Per this task's own explicit guidance, this is recorded
as a confirmed absence at this review density, not forced into a specific timestamp. It does not, on
its own, explain any of the already-resolved ambiguous-dock-state windows above (both of those are now
resolved by ordinary — not wrong-orientation — dock/undock actions).

## 7. Test-ID traceability (`AGENTS.md` §13)

- **`PRIV-001`** (primary): fully exercised — 14 of the 7 target codes' full context decoded across 14
  independent reconnects (vs. `CAP-040`'s N=1). Three-way outcome stated in §3: 5 of 7 codes resolve to
  "not a match" or "no candidate found"; 2 of 7 (`04 04`, `04 15`) remain inconclusive with a new,
  explicit open question (§4/§8), not silently left unmentioned.
- **`PAIR-003`** (incidental, reconnect to an already-bonded device): all 16 reconnects exercise this;
  none involved a fresh pairing handshake (no `Link Key Request`/SSP sequence checked this pass, out of
  scope for `PRIV-001`'s own question).
- **`BATT`-family** (incidental): §3/§4's `04.03` field (already-documented Option E content) provides
  incidental confirmation of continued battery tracking through the session; not separately analyzed.

## 8. Conclusions & proposed downstream updates — awaiting maintainer sign-off for proposed items

**Recorded as this session's own factual result (no sign-off needed, per `AGENTS.md` §15 — direct
observations, not FACT promotions to `PROTOCOL.md`):**
- 16 fresh classic ACL reconnects, 15 confirmed + 1 inconclusive private-envelope channel opens (§1/§2).
- Session-local DLCI reassignment for the private-envelope channel, confirmed a second time in a new
  form (DLCI `0x08` carrying HFP on one reconnect) (§2).
- The 7 flagged `PRIV-001` codes fire on every private-envelope (re)open, not a one-off (§3).
- Five of the 7 codes resolve to "not a match"/"no candidate" against the dock-state bracket; two
  remain genuinely inconclusive (§3/§4) — not silently dropped.
- Both of `CAP-050-EVENT-NOTES.md`'s own flagged ambiguous-dock-state windows are now resolved from
  wire+video correlation (§4/§6, both docked; Left-docked/Right-out respectively).
- A `Settable-toggles=0x00` reading that does not match the true physical state (Left-only docked, not
  both) at one reconnect (§4/§5) — consistent with, not contradicting, `CAP-048-FINDINGS.md` §5's
  already-documented stale-reading pattern.
- No clear video evidence of the recalled mis-docking event, at the review densities applied (§6).

**Proposed only, NOT committed — awaiting explicit maintainer sign-off per `AGENTS.md` §6/§15:**
1. `PROTOCOL.md` §6 — add a new 🔴 open question: `Group 0x04 Code 0x05`/`Code 0x16` (adjacent to the
   still-unmapped `PRIV-001` codes `04 04`/`04 15`) take a non-constant value that appears to relate to
   recent dock/undock activity but does not reproduce consistently for the same physical dock
   configuration across different reconnects (§4) — flagged as inconclusive, not a HYPOTHESIS.
2. `PROTOCOL.md` §6 — update the `PRIV-001` open item: 5 of the 7 originally-flagged codes are now a
   resolved "not a match"/"no candidate found" (constant values); 2 remain open per item 1 above; this
   supersedes `CAP-040-FINDINGS.md` §3's inconclusive-due-to-N=1 framing for this specific Test-ID.
3. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `PRIV-001` row — Evidence column pointer to this file; status
   updated to reflect the resolved/still-open split above.
4. `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 / `id_registry.csv` — `CAP-050` row status update (`planned` →
   `analyzed`).

None of the above reaches `DECISIONS.md`'s promotion bar (no 🟢 FACT semantic reading is proposed for
any previously-open code) — nothing is proposed there.

## 9. Open questions

- 🔴 What do `Group 0x04 Code 0x05`/`Code 0x16` actually encode? Fluctuates near dock-state changes but
  does not reproduce for the same physical configuration across reconnects (§4) — genuinely open, not a
  HYPOTHESIS.
- 🔴 `04 02`, `04 04`, `04 11`, `04 13`, `04 15`, `05 0c`, `0e 04` themselves (as opposed to their
  neighbors) remain entirely undecoded — this session establishes what's near them, not what they
  themselves mean or whether they carry any payload-bearing response at all.
- 🔴 The recalled mis-docking (wrong-orientation) event was not identified in this recording at the
  review densities applied (§6) — a confirmed absence, not resolved to a specific timestamp.
- 🔴 Chandle `0x0011`'s (final reconnect) private-envelope DLCI could not be confirmed — the log/video
  end before a full burst is captured (§2).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-050-2026-09-14_21-01-01_21-13-30-Group_AG/CAP-050-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-050-2026-09-14_21-01-01_21-13-30-Group_AG/CAP-050-FINDINGS
