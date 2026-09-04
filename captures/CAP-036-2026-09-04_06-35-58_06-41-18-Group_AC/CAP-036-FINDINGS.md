# Findings: `CAP-036` (Group AC — settings-state read-back on (re)connect and on settings-screen open, `OBS-004`)

**✅ Maintainer sign-off obtained 2026-09-04, per `AGENTS.md` §6/§15.** The "Get ANC state" (`0x11`)
opcode's *identity* (Group/Code values, direction, zero-length structure, real-observed-on-wire
status) is promoted to 🟢 FACT in `PROTOCOL.md` §4.1, recorded in `DECISIONS.md` ADR-021. The
broader *trigger-reliability* claim ("fires on every reconnect") and the settings-screen-open
clean-negative result remained 🟡 HYPOTHESIS at the time of this session — the maintainer
explicitly declined to promote those from a single sample. §10 lists every downstream update
applied this session (`PROTOCOL.md` §4.1/§6, `DECISIONS.md` ADR-021, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`,
Capture Index, `id_registry.csv`).

**Update, same day (`DESKRESEARCH_FINDINGS.md`'s two-round bonus cross-check):** the
trigger-reliability claim has since been independently replicated 17 times across 10 capture
files (`CAP-006`, `CAP-010`, `CAP-016`, `CAP-019`–`CAP-025`, this session) with zero misses against
a precisely-scoped condition, and **promoted to 🟢 FACT, `DECISIONS.md` ADR-022** — see that ADR
and `DESKRESEARCH_FINDINGS.md`'s 2026-09-04 "round 2" entry for the full replication evidence. The
settings-screen-open negative result (this session's own primary finding) is unaffected and
remains 🟡 HYPOTHESIS.

Standardized, evidence-based extraction from `CAP-036-btsnoop_hci.log` +
`CAP-036-recording.mp4`, staged here for later promotion into `PROTOCOL.md` per `PROJECT_RULES.md`
§2. Modeled on `CAP-001-FINDINGS.md`/`CAP-035-FINDINGS.md`. Every claim below carries a status per
`PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-036` · **Date:** 2026-09-04 · **Firmware:** ⚪ ASSUMPTION `release_5.203`
(carried over, not explicitly re-checked on-screen this session). **Phone:** Pixel 7a, Android 14,
official Pixel Buds Companion App (`1.0.955078535`), Google Play Services enabled (the normal
baseline — see `CAP-036-EVENT-NOTES.md`'s preparation checklist for the `dumpsys` verification,
carried in as-is per this session's brief). **Method:** official app only, no third-party
BLE/GATT tool. **Log file:** `CAP-036-btsnoop_hci.log` (454.462092s, 2,492 packets, 2026-09-04
06:35:47.353558–06:43:21.815650 local/+0200). **Video:** `CAP-036-recording.mp4` (317.95s,
06:35:58–06:41:16 local, wall-clock overlay burned in). **Devices:** phone (Pixel 7a), peer
`04:00:6E:CF:6E:07` ("Pixel Buds Pro 2 van Ted") — the same physical Buds/case used throughout
this project.

---

## 0. Capture integrity: unlimited snaplen, zero truncation, raw extraction path (🟢 FACT)

```
$ capinfos CAP-036-btsnoop_hci.log
Number of packets:   2,492
Packet size limit:   file hdr: (not set)
Capture duration:    454.462092 seconds
Earliest packet time: 2026-09-04 06:35:47.353558
Latest packet time:   2026-09-04 06:43:21.815650
Interface #0 info: Capture length = 262144

$ tshark -r CAP-036-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'
mismatches: 0
```
No snaplen limit; 0/2,492 mismatches. Not truncated. File name suffix (`-btsnoop_hci.log`, not
`-btsnooz_hci.log`) and the absence of any snaplen cap together confirm the raw step-3 extraction
path (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3) was used, not the `btsnooz.py` step-4 fallback —
consistent with `DESKRESEARCH_FINDINGS.md`'s 2026-08-28 cross-capture pass, and disproportionately
important for this specific session (a truncated log could manufacture a false negative by
clipping exactly the short query frames this capture looks for).

## 1. Connection identification / CLI-hygiene correction (🟢 FACT)

**`bluetooth.addr == 04:00:6e:cf:6e:07`, the filter `AGENTS.md` §13 and this capture's own
preparation checklist specify, returns zero matches for RFCOMM traffic in this log:**

```
$ tshark -r CAP-036-btsnoop_hci.log -Y "bluetooth.addr == 04:00:6e:cf:6e:07" -T fields -e frame.number
(0 rows)
```

Wireshark's `bluetooth.addr` aggregate field is not populated for ACL/RFCOMM frames in this
particular btsnoop capture — only for the HCI event layer. The working substitute:

```
$ tshark -r CAP-036-btsnoop_hci.log -T fields -e bthci_evt.bd_addr 2>/dev/null | grep -v "^$" | sort -u
04:00:6e:cf:6e:07
44:d6:94:50:f0:4e
4c:11:0e:25:c9:90
66:7a:1b:07:1a:ca
74:84:d3:e9:98:0f
77:95:23:6a:6e:75
e8:d5:2b:7e:ca:81

$ tshark -r CAP-036-btsnoop_hci.log -Y "bthci_evt.code == 0x03" -T fields -e frame.number -e frame.time -e bthci_evt.bd_addr -e bthci_evt.connection_handle
906  2026-09-04T06:36:30.962265000+0200  04:00:6e:cf:6e:07  0x0005
```

`bthci_evt.bd_addr == 04:00:6e:cf:6e:07` matches (Connection Complete event, frame 906), which
identifies **`bthci_acl.chandle == 0x0005`** as the Buds' connection handle for the rest of this
session. Used as the pre-filter in every command below, in place of `bluetooth.addr`.

**Unrelated background device present, per the `CAP-004-FINDINGS.md` §1 Fitbit-contamination
precedent — structurally isolated, does not affect this session's evidence:**

```
$ tshark -r CAP-036-btsnoop_hci.log -T fields -e bthci_acl.chandle | sort -u
0x0003
0x0005
```

`chandle 0x0003` carries plain `ATT` (BLE GATT) service-discovery traffic — `Find By Type Value`,
`Read By Type`, `Read By Group Type` against `GAP`/`GATT`/`Device Information` — from
06:36:03.42 to 06:36:21.90 (frames 432–865), ending **before** the Buds' own reconnect even
completes (frame 906, 06:36:30.96). No Connection Complete event for `chandle 0x0003` appears in
this log (the underlying LE connection predates the capture), and its identity was not further
pursued (out of scope — it never touches DLCI 0x02/0x04/0x08). This traffic never overlaps any of
this session's windows and is excluded from all findings below.

## 2. Video re-pass: corrected Event Timeline (🟢 FACT)

**Method:** `ffmpeg -vf fps=1` extracted one PNG per second across the full 317.95s recording
(318 frames), read against the burnt-in wall-clock overlay; several transitions were additionally
checked at finer granularity by extracting adjacent seconds. `CAP-036-EVENT-NOTES.md`'s Event
Timeline carries the full corrected table; this section documents the corrections themselves and
the evidence for each.

**Corrections made (placeholder time → verified time, with the video evidence):**

| Placeholder said | Verified via video | Delta | Evidence |
|---|---|---|---|
| Window 2 end `06:38:00` | Back-arrow tap at `~06:37:53`, Device details rendered by `06:37:58` | ~5–7s early | EQ screen still shows untouched sliders at `06:37:53` (finger on back arrow); Device details visible at `06:37:58` |
| Window 3 start `06:38:46` | "Touch controls" **leaf** screen (the actual touch-controls screen) opened at `~06:38:28`, not `06:38:46` | ~18s early | `06:38:05`: "Controls and gestures" (parent) screen open, idle; `06:38:23`: finger tapping "Use touch controls" chevron; `06:38:28`: "Touch controls" leaf screen fully rendered |
| Window 3 end `06:39:15` | Back-navigation out of "Touch controls" at `~06:38:44`–`46` (`06:38:46` is exactly when the *placeholder's own guessed Window-3-start value* actually shows the user already back on "Controls and gestures") | ~29–31s early | `06:38:43`: still on "Touch controls", idle; `06:38:46`: already back on "Controls and gestures" |
| Window 5 start `06:39:53` | Multipoint screen opened at `~06:39:54` | ~1s, within normal 1fps-sampling tolerance — confirmed, not corrected | `06:39:43`: still on "More settings" list; `06:39:54`: "Multipoint" screen rendered |
| Window 5 end `06:41:16` | Confirmed exact — video's own last frame | 0s | Last extracted frame (`t=317s`) shows `06:41:16`, still on Multipoint, untouched |

**What this means for the analysis below:** the *actual* touch-controls idle observation ran in
two separate, both-clean sub-windows — "Controls and gestures" (parent, `06:38:05`–`~06:38:23`,
~18s idle) and "Touch controls" (leaf, `06:38:28`–`~06:38:44`, ~16s idle) — rather than one window
starting at `06:38:46` as originally guessed. Both sub-windows are unaffected by this correction
(nothing was toggled in either), so the DLCI 2/4/8 decode in §§3–5 covers both.

**On-screen values captured this pass** (satisfying `CAP-036-EVENT-NOTES.md` line ~329's
requirement, previously blank):
- ANC mode: **Off** (the "Off" tile alone shows a tinted/selected background at `06:37:04`,
  cropped and confirmed pixel-for-pixel against the other three tiles' flat grey).
- EQ: **"Last saved" preset**, all five bands (Upper treble/Treble/Mid/Bass/Low bass) centered at
  0, **Volume EQ = ON** (`06:37:28`).
- Touch/gesture config: **Use touch controls = ON, Left press-and-hold = Active noise control,
  Right press-and-hold = Active noise control, Use head gestures = ON** (`06:38:05`, `06:38:28`).
- **In-ear detection = ON, Multipoint = ON** (visible on the "More settings" list at `06:39:16`,
  before either sub-screen was individually opened).

## 3. DLCI 0x04 — "Get ANC state" (`0x11`) query found: the documented-but-never-observed opcode (🟢 FACT for opcode identity, `DECISIONS.md` ADR-021; 🟡 HYPOTHESIS for trigger reliability)

```
$ tshark -r CAP-036-btsnoop_hci.log -Y "bthci_acl.chandle==0x0005 and btrfcomm.dlci==4" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.len -e data.data
```

Relevant rows (direction: `0`=Sent, `1`=Rcvd):

```
1109  06:36:31.301896  Sent  0    SABM (DLCI 0x04 opens)
1125  06:36:31.324643  Rcvd  0    UA
1169  06:36:31.358590  Sent  4    08110000
1182  06:36:31.369254  Rcvd  8    0813000401e80020
```

**Decode:**
- Frame 1169, `08 11 00 00`: `Group=0x08, Code=0x11, Len(2BE)=0x0000` — exactly `PROTOCOL.md`
  §4.1's documented **"Get ANC state" (Seeker → Provider, no payload, no ACK)**. This opcode has
  never before been observed on the wire in any capture to date (`CAP-001-FINDINGS.md` §5; the
  Group AC purpose statement in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`).
- Frame 1182, `08 13 00 04 01 e8 00 20`, arriving **~10.7ms** later: `Group=0x08, Code=0x13,
  Len=0x0004, Version=0x01, UI toggles=0xe8, Settable toggles=0x00, Current state=0x20` — the
  documented **"Notify ANC state"** layout. `Current state = 0x20` = bit 5 (spec's `Bit 2`) =
  **Off**, per `PROTOCOL.md` §4.1's bit-mapping table.
- **On-screen cross-check:** the Device details screen at `06:37:04` (§2) shows the "Off" ANC tile
  selected — matching this decode exactly, ~33 seconds after the wire-level state was read.
- **Timing/trigger:** the query fires at `06:36:31.359`, **34ms** after DLCI 0x04's own `SABM`/`UA`
  handshake completes (frames 1109/1125), and **~9 seconds before** the UI's own "Active"
  confirmation renders (`~06:36:40`–`41`, §2) — i.e. this is the very first thing the app does on
  this channel after the reconnect, well before any user-visible confirmation. It never recurs:
  a full-log search finds exactly one `08 11` frame and one `08 13` frame in the entire session:

```
$ grep -c "0811" cap036_rfcomm_full.tsv   # 1 occurrence (frame 1169)
$ grep -c "0813" cap036_rfcomm_full.tsv   # 1 occurrence (frame 1182)
$ grep -c "0812" cap036_rfcomm_full.tsv   # 0 occurrences (no Set ANC state anywhere — consistent with "nothing touched")
```

**Conclusion — stated at the strength the evidence actually supports:** the official app **does**
issue the spec-documented "Get ANC state" query, **on reconnection**, immediately after the Fast
Pair Message Stream channel (DLCI 0x04) opens — not merely a theoretical, spec-only opcode.
**✅ Maintainer sign-off obtained 2026-09-04:** the opcode's *identity* (Group/Code, direction,
structure, real-observed status) is promoted to 🟢 FACT in `PROTOCOL.md` §4.1 (`DECISIONS.md`
ADR-021). This remains a single sample from one session, so the *broader* claim — that this query
reliably fires on **every** reconnect — stays 🟡 **HYPOTHESIS pending replication**, per the
maintainer's explicit, narrower approval (see §10).

**Open discrepancy, not resolved here:** this Notify frame's "Settable toggles" byte reads `0x00`,
while every previously-documented **Set** ANC state frame (`CAP-001`, `PROTOCOL.md` §4.1) shows
`settable=0xe8` in the same byte position. Recorded as a new 🔴 open question (§11) rather than
silently reconciled or ignored. **Update (2026-09-04, `DESKRESEARCH_FINDINGS.md`'s bonus
cross-check):** `CAP-016-FINDINGS.md` §4 independently documents the exact same
`08 13 00 04 01 e8 00 20` byte pattern firing as the very first Notify right after a fresh RFCOMM
connection, with its own 🟡 HYPOTHESIS reading ("the Buds have not yet reported which ANC modes
are selectable, e.g. right after connect"). This session's own frame 1182 fits that same
connect-time pattern precisely — the "discrepancy" is likely an instance of `CAP-016`'s
already-observed behavior, not a new, separate anomaly, though still only 🟡 HYPOTHESIS at 2
sessions.

## 4. DLCI 0x02 (`libmaestro`) — no `Sent`-direction frame in any idle window (🟢 FACT, clean negative)

```
$ tshark -r CAP-036-btsnoop_hci.log -Y "bthci_acl.chandle==0x0005 and btrfcomm.dlci==2 and btrfcomm" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.len -e data.data
```

All 99 DLCI 0x02 frames in this session fall into exactly two clusters:

1. **Connection-settling burst, frames 1392–1591 (06:36:32.570–35.655), ~3.1s** — `SABM`/`UA`
   followed by a dense back-and-forth of small `Sent`/`Rcvd` payloads, including three ASCII
   `"release_5.203"` firmware-version strings (frame 1405) and many short request/response pairs
   sharing a partial constant prefix (`03 10 15 1d ea 71 de 7e 25...`, matching — up to that
   point — §4.5's documented request/response correlation-ID prefix `03 10 XX 1d ea 71 de 7e 25 1d
   9a 8c 9e`). **This burst is not decoded further this session**: it precedes Window 1's own
   stated idle span (it completes ~5 seconds before the "Active" UI confirmation, let alone the
   idle-observation period that follows it), and none of the payloads sampled visually matches
   §4.5's specific `field5{field4{fieldN=V}}` write-envelope shape on inspection (recorded as a new
   🔴 open question, §11, rather than force-decoded).
2. **Periodic `Rcvd`-only push, 06:39:51–06:42:31 (frames 2009, 2048, 2164, 2204, 2237, 2334,
   2360)** — content stable across samples (`7e00a5032a2510...0821ee6...7e`), always answered by an
   empty (`len=0`) `Sent` RFCOMM-level acknowledgement, never a new query-shaped payload.

```
$ tshark -r CAP-036-btsnoop_hci.log -Y "bthci_acl.chandle==0x0005 and btrfcomm.dlci==2 and frame.p2p_dir==0 and btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time
(34 rows, all with frame.time between 06:36:32.597 and 06:36:35.610 — none inside any idle window)
```

**Conclusion:** zero `Sent`-direction DLCI 0x02 frames occur inside Window 1's idle span
(`06:36:40`–`~06:37:03`), Window 2 (`06:37:28`–`~06:37:53`), either Window 3 sub-span
(`06:38:05`–`~06:38:23`, `06:38:28`–`~06:38:44`), or Window 5 (`06:39:54`–`06:41:16`). Clean
negative, per `PROJECT_RULES.md` §1 rule 4a's hex-and-script requirement — the command above is
what makes this "nothing found" independently verifiable.

## 5. DLCI 0x08 — known Option E push confirmed; connect-time "Get"-shaped frames flagged, not claimed (🟢 FACT for content match, 🔴 open question for the new observation)

```
$ tshark -r CAP-036-btsnoop_hci.log -Y "bthci_acl.chandle==0x0005 and btrfcomm.dlci==8" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.len -e data.data
```

**Connect-time burst (frames 1273–1372, 06:36:31.427–31.809):** capability blob
(`google-pixel-buds-pro-v1`, frame 1315), firmware string (`release_5.203`, frame 1327), and
per-earbud battery-like values (`04 03 00 04 10 05 18 64`, frame 1298) — byte-identical in shape
to `PROTOCOL.md` §4.3 Option E's documented content. This is a **known push**, not a new query
response, per the exact caution this Group's own procedure states.

**New observation, flagged but not over-claimed:** this same burst contains several `Sent` frames
matching the identical `[Group:1][Code:1][Len(2BE)=0000]` zero-length shape as DLCI 0x04's
confirmed "Get" pattern (§3): `05 0c 00 00`, `04 02 00 00`, `04 04 00 00`, `04 11 00 00`,
`04 13 00 00`, `04 15 00 00` (frames 1273–1282), and `0e 04 00 00` (frame 1286). **None of these
Group/Code pairs is mapped to any known setting** — `PROTOCOL.md` §6 already lists most of DLCI
0x08's Groups (`0x01`/`0x02`/`0x05`/`0x09`) as structurally confirmed but semantically
unresolved, and Group `0x04`/Group `0x0e` aren't in that list at all. Recorded as a new 🔴 open
question (§11) — this is *not* claimed as evidence of a settings-state query; it is only noted
because the shape happens to match.

**Idle-window check:**

```
$ tshark -r CAP-036-btsnoop_hci.log -Y "bthci_acl.chandle==0x0005 and btrfcomm.dlci==8 and frame.p2p_dir==0 and btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time
(all rows between 06:36:31.427 and 06:36:31.809 — none inside any idle window)
```

The same connect-time content (capability blob + firmware string + battery-like value) recurs
verbatim during Window 5 (frames 1999–2357, ~10s-ish cadence from `06:39:51` onward, matching
DLCI 0x02/0x04's periodic push documented in §4/§3) — always `Rcvd`, always immediately
acknowledged by an empty `Sent` frame, never a new query.

**Conclusion:** confirms Option E's content once more; zero new `Sent`-direction query frames in
any idle window. The zero-length "Get"-shaped frames found are a genuinely new, unresolved
observation (§11), structurally interesting but explicitly not claimed as a settings-query finding
for this Group's purposes.

## 6. Per-window attribution table

| Window | Screen | Span | DLCI 2/4/8 traffic | Attribution |
|---|---|---|---|---|
| 1 (connect burst, not the idle span itself) | — (reconnect handshake) | `06:36:31.30`–`35.66` | DLCI 4: `08 11`/`08 13` (§3); DLCI 8: Option E burst (§5); DLCI 2: RPC-shaped burst, undecoded (§4) | Reconnect-triggered — the query in §3 sits here, 34ms after DLCI 4 opens |
| 1 (idle) | Device details (background) | `06:36:40`–`~06:37:03` | none | Clean |
| 2 | Equalizer | `06:37:28`–`~06:37:53` | none | Clean |
| 3a | Controls and gestures (parent) | `06:38:05`–`~06:38:23` | none | Clean |
| 3b | Touch controls (leaf) | `06:38:28`–`~06:38:44` | none | Clean |
| — | More settings (list) | `06:39:16`–`~06:39:43` | none | Clean |
| 5 | Multipoint | `06:39:54`–`06:41:16` | DLCI 2/4/8 periodic push, starting `06:39:51` (3s **before** screen render) | Time-triggered, not screen-open-triggered |

## 7. Three-way outcome, split by trigger (per Group AC's stated-in-advance discipline)

- **Reconnection trigger — outcome (a), query frame found.** DLCI 0x04's "Get ANC state" (`0x11`)
  fires once, immediately after channel establishment, answered by "Notify ANC state" (`0x13`)
  ~10.7ms later, matching the on-screen ANC state. 🟡 HYPOTHESIS pending replication (§3).
- **Settings-screen-open trigger (EQ, Controls-and-gestures, Touch controls, More settings,
  Multipoint) — outcome (b), clean negative.** Zero query-shaped or write-shaped frames on DLCI
  0x02/0x04/0x08 across five distinct idle windows/sub-windows, all confirmed clean (no
  contamination) via the video re-pass. A citable negative result, per this Group's own explicit
  design intent (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AC purpose statement).
- **Not applicable:** outcome (c) (inconclusive) — the log is untruncated (§0), no window was
  contaminated (§2, contamination log), so neither sub-question needs a re-run.

## 8. Cross-reference to `CAP-024-FINDINGS.md` §4 (kept separate, per this Group's own instruction)

`PROTOCOL.md` §6 Behavior carries an open item from `CAP-024-FINDINGS.md` §4: does opening the
"Case sounds" screen itself trigger a state-sync **write** on DLCI 0x02, or does a write only
register on an explicit tap? This session's Windows 2/3a/3b/5 are the same experimental shape
(a settings screen opened with nothing touched) applied to different screens (EQ,
Controls-and-gestures, Touch controls, Multipoint) — and §4 above shows **zero `Sent`-direction
DLCI 0x02 frames in any of them**, which is directly relevant evidence for the *write*-on-open
question too, even though it does not itself settle it (this session's screens are not "Case
sounds," and the two questions — read-on-open vs. write-on-open — remain, per this Group's own
instruction, separate findings). Recorded here as a cross-reference only, not merged into §3–§7's
conclusions.

## 9. Test-ID traceability (`AGENTS.md` §13 requirement)

- **`OBS-004`** (this session's primary goal): exercised across all 5 windows/sub-windows. Result:
  split outcome per §7 — query found on reconnect (§3), clean negative on settings-screen-open
  (§4–§6).
- **`PAIR-003`** (reconnect to an already-bonded device, incidental): exercised cleanly — HCI
  `Create Connection` (frame 898, 06:36:28.61) → `Connection Complete` (frame 906, 06:36:30.96),
  no fresh pairing/bonding traffic (no `IO Capability`/SSP exchange observed on this chandle).

## 10. Conclusions & downstream updates — ⏳ awaiting maintainer sign-off for the proposed items

**Confirmed, at the strength the evidence actually supports — not overstated:**
- DLCI 0x04's "Get ANC state" (`0x11`) query — documented since `PROTOCOL.md` §4.1 was written,
  never before observed on the wire — **does occur**, once, on reconnection. Its *opcode identity*
  is now 🟢 FACT (`DECISIONS.md` ADR-021); whether it fires on *every* reconnect remains 🟡
  HYPOTHESIS, single sample (§3).
- No settings-state query or write of any kind occurs on DLCI 0x02/0x04/0x08 when a settings
  screen is opened with nothing touched, in this session's log (🟢 FACT for the raw observation,
  five clean windows, §4–§6) — the broader behavioral generalization ("the app never does this")
  stays 🟡 HYPOTHESIS pending a second replicating session (§7).

**✅ Maintainer sign-off obtained 2026-09-04, applied:**
1. `PROTOCOL.md` §4.1 — the "Get ANC state" (`0x11`) opcode's identity (Group/Code values,
   direction, zero-length structure, real-observed-on-wire status) promoted to 🟢 FACT. The
   trigger-reliability claim and the settings-screen-open negative result were explicitly **not**
   promoted — the maintainer approved only the narrower opcode-identity claim.
2. `DECISIONS.md` ADR-021 — written, recording this promotion and its explicit scope limits (see
   above).

**Applied this session, factual/administrative (no sign-off required):**
- `PROTOCOL.md` §6 — 3 new 🔴 open questions copied in, per §8's mandatory rule (this file's §3/§4/
  §5 discrepancies/observations).
- `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `OBS-004` row — Evidence column updated to a pointer
  (`CAP-036-FINDINGS.md`), no restated finding inline.
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Capture Index — `CAP-036` row updated from `planned` to
  `analyzed`, with Android/firmware/app-version columns and log path filled in.
- `id_registry.csv`'s `CAP-036` row updated from `planned` to `analyzed`.

## 11. Open questions after this session

- 🔴 DLCI 0x08's connect-time zero-length `[Group][Code][00 00]`-shaped `Sent` frames (`04 02`,
  `04 04`, `04 11`, `04 13`, `04 15`, `0e 04`) are structurally identical to DLCI 0x04's confirmed
  "Get" pattern, but none of these Group/Code pairs is mapped to any known setting (§5).
- 🔴 The dense RPC-shaped burst on DLCI 0x02 immediately after channel establishment (frames
  1404–1591) is not decoded beyond noting its partial match to §4.5's correlation-ID prefix —
  whether it carries any settings-state read-back via `libmaestro`'s own channel is unresolved (§4).
- 🔴 This session's "Notify ANC state" frame (`08 13 00 04 01 e8 00 20`) decodes "Settable
  toggles" as `0x00`, versus `0xe8` in every previously-documented "Set ANC state" frame in the
  same byte position — not reconciled (§3).
- 🔴 **Added in §12's bonus analysis:** DLCI 0x02's periodic push (§12.6) decodes structurally
  (verified HDLC/CRC-32) to a repeated triple pattern resembling, but not matching field-for-field,
  the confirmed Option E battery-triple shape — genuinely open, not claimed as battery content.
- 🔴 **Added in §12's bonus analysis:** the BLE address `44:d6:94:50:f0:4e`, identified via DLCI
  0x04's "BLE address updated" field as (very likely) the Buds' own rotating identity this
  session, broadcasts a stable Fast Pair (`0xFE2C`)/`0x1853` advertisement 407 times — but its
  payload does not match the documented Battery Notification layout (§4.3 Option A), consistent
  with `CAP-011`'s existing inconclusive finding, not force-fit into a positive result (§12.4).

## 12. Bonus/secondary analysis: battery & firmware (incidental to `OBS-004`, outside this Group's own scope)

**Scope note:** none of this section is evidence for or against `OBS-004`'s own question (§3–§7
above already cover that in full). This is a secondary pass over the *same* log, requested
separately, covering channels `OBS-004`'s own procedure never asked about (DLCI 0x0a/0x0c, BLE
advertising) and adding detail to channels it did touch only in passing (DLCI 0x04/0x08's
connect-time content). No new capture was needed — this is analysis of data already on disk.

### 12.1 DLCI 0x0c (HFP) — Option C reconfirmed, one more cadence data point (🟢 FACT, reconfirms `ADR-015`)

```
$ tshark -r CAP-036-btsnoop_hci.log -Y "bthci_acl.chandle==0x0005 and btrfcomm.dlci==12" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.len -e _ws.col.Info
```

Standard HFP SLC setup (frames 1106–1272: `AT+BRSF`, `AT+BAC`, `AT+CIND=?`/`AT+CIND?`, `AT+CMER`,
`AT+BIND`), then:
- **`AT+CIND?`** (frame 1209) → `+CIND: 0,0,0,0,0,2,0` — `BATTCHG` (6th indicator, range `0-5` per
  the `AT+CIND=?` response, frame 1195) = `2`, a coarse snapshot. Matches `ADR-015`'s existing
  🟢 FACT characterization exactly (single non-repeating query-response at SLC setup, coarse
  0–5 scale, not a percentage) — reconfirmation, not a new finding.
- **`AT+BIEV=2,100`** (frame 1357, Rcvd) — HF Indicator #2 = Battery Level = **100**, matching the
  on-screen 100% throughout this session. Recurs at frames 2005, 2044, 2159, 2200, 2235, 2330,
  2356 — cadence: `199.7s, 10.1s, 70.1s, 10.1s, 9.6s, 50.4s, 9.6s` between occurrences. Consistent
  with `PROTOCOL.md` §4.3 Option C's existing "settling burst, then irregular — not a fixed
  cadence" characterization; adds one more session's data point, no change to that conclusion.
- **`AT+BIEV=1,1`** (frame 1287) — HF Indicator #1 (Enhanced Safety) = 1, sent once, never repeats
  — consistent with it not being a battery-related indicator.

### 12.2 DLCI 0x08 — Option E and firmware content reconfirmed (🟢 FACT, reconfirms `ADR-012`/`ADR-014`); one completeness addendum

```
$ tshark -r CAP-036-btsnoop_hci.log -Y "bthci_acl.chandle==0x0005 and btrfcomm.dlci==8" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.len -e data.data
```

- **`Group 0x0e Code 0x01`** (frame 1317, Rcvd, `0e0100230a210a03616c6c121a0a060864100118010a060864100118020a060864100118032001`)
  decodes to three `[value=100, flag=1, index=N]` entries (`N`=1/2/3 → Left/Right/Case per
  `ADR-014`) — **100/100/100**, matching the on-screen "Left 100% Case 100% Right 100%" exactly.
  Recurs identically at frames 1999+/2036+/2153+/2194+/2227+/2321+/2348+ (same periodic cadence as
  §12.1's `AT+BIEV`, see §12.5). Reconfirms `ADR-014`'s existing FACT.
- **`Group 0x04 Code 0x03`** (frame 1298, Rcvd, `0403000410051864`) decodes to `field2=5,
  field3=100` — `field3=100` matches the on-screen **Right** earbud percentage, per this opcode's
  existing documented role (`PROTOCOL.md` §4.3 Option E: "an independent, single-value message...
  cross-confirms the Right value"). **Completeness addendum, not a new claim:** `field2`'s value
  is `5` in **every** session checked this pass (`CAP-001`, `CAP-002`, `CAP-011` — where it tracks
  88/87/86 in `field3` — and `CAP-036`) — a stable constant across 4+ independent sessions, not
  previously called out at this byte-level precision in `PROTOCOL.md`.
- **`Group 0x03 Code 0x02`** (firmware string, e.g. frame 1327 contains ASCII `"release_5.203"`)
  reconfirms `ADR-012`'s existing FACT — this is `CAP-036`'s own §0's independent evidence,
  restated here only for this section's completeness.
- **DLCI 0x0a ("GSND AUDIO")** — opens in lockstep with 0x08 (frames 1246/1264, `SABM`/`UA`) but
  carries **zero payload**, consistent with the established multi-capture baseline
  (`CAP-035-FINDINGS.md` §6 and its own citation list).

### 12.3 DLCI 0x04 — Device Information group reconfirmed; a documentation-gap fixed (🟢 FACT, already established since `CAP-002`, 2026-08-10 — not a new promotion)

```
$ tshark -r CAP-036-btsnoop_hci.log -Y "bthci_acl.chandle==0x0005 and btrfcomm.dlci==4" \
  -T fields -e frame.number -e data.data | grep "^1160"
1160  030a00081f4d16b60963f13d03010003da2db10302000644d69450f04e0309000a5265766973696f6e203607100000
```

Decodes to three Group `0x03` ("Device Information") messages back-to-back:
- `Code 0x0a`, 8-byte value `1f4d16b60963f13d` — still 🔴 unresolved/unassigned in the official
  spec's own code table (`CAP-002-FINDINGS.md` §3), rotates every session, consistent with prior
  observations.
- `Code 0x01` = **"Model ID"**, value `da2db1` — **identical** to every other session checked this
  pass (`CAP-001`, `CAP-002`, `CAP-010`) — a constant, registered Fast Pair Model ID, per
  `CAP-002-FINDINGS.md` §3's original 🟢 FACT (spec-worked-example match).
- `Code 0x02` = **"BLE address updated"**, value `44:d6:94:50:f0:4e` — a 6-byte value, spec-shape
  match (`0x03 0x02 0x00 0x06 <6-byte MAC>`), 🟢 FACT since `CAP-002-FINDINGS.md` §3 (official
  spec worked example). **Differs every session** (`77:96:2c:96:68:1c` in `CAP-001`/`CAP-002`,
  `53:0c:b4:c8:06:3d`/`75:51:27:4f:ae:59` elsewhere in those same logs, `51:70:22:b8:72:2f`/
  `5e:6a:14:ce:17:9f` in `CAP-010`, `44:d6:94:50:f0:4e` here) — consistent with a rotating
  private/resolvable BLE address, exactly as its own name states.
- `Code 0x09` = "Firmware version" = ASCII `"Revision 6"` — reconfirms the existing FACT
  (`CAP-002-FINDINGS.md` §3), unchanged from every prior session.

**Documentation-gap fixed, not a new promotion:** `Code 0x01`/`Code 0x02`'s 🟢 FACT status was
established in `CAP-002-FINDINGS.md` §3 on 2026-08-10 (direct fetch of Google's official
`deviceinformation` spec page, worked-example byte match) but was **never copied into
`PROTOCOL.md`** — a gap in the "promote from `CAP-NNN-FINDINGS.md` into `PROTOCOL.md`" pipeline
(`PROJECT_RULES.md` §2 rule 5). Fixed in `PROTOCOL.md` §0.1 this session (mechanical fix, citing
the pre-existing FACT — no new maintainer sign-off needed, since the underlying finding was
already approved at the time; see §13 below).

### 12.4 New: device-attribution advance for `PROTOCOL.md` §4.3 Option A — not a resolution (🟡 HYPOTHESIS, new)

`CAP-011-FINDINGS.md` §4 (and `PROTOCOL.md` §4.3 Option A) left the Fast Pair (`0xFE2C`) BLE
advertisement's device attribution **unconfirmed** — payloads didn't match the documented Battery
Notification layout, and no Account Key Filter decode was attempted to tie any observed rotating
address back to this specific Buds unit.

**This session's `Code 0x02` value (§12.3), `44:d6:94:50:f0:4e`, was cross-checked directly
against the log's own BLE advertising traffic:**

```
$ tshark -r CAP-036-btsnoop_hci.log -Y "bthci_evt.bd_addr==44:d6:94:50:f0:4e" \
  -T fields -e frame.number -e btcommon.eir_ad.entry.uuid_16 -e btcommon.eir_ad.entry.service_data
131  0xfe2c,0x1853  10504990880c0921905b46e21c52ce
(407 occurrences total, identical service-data payload throughout the ~7-minute session)
```

**This address broadcasts a Fast Pair Service (`0xFE2C`) + `0x1853` advertisement 407 times**, with
a **stable, unchanging** 15-byte service-data payload throughout the session. This is the first
time this project has tied a live-observed BLE advertising address directly back to a
classic-channel field describing the Buds themselves (rather than only a timing correlation, as
`CAP-009-FINDINGS.md` §4 previously recorded) — 🟡 **HYPOTHESIS**: `44:d6:94:50:f0:4e` is this
session's own Buds unit's rotating BLE identity, single-session evidence.

**Not resolved, explicitly:** the service-data payload's first byte is `0x10`, not the `0x00`
"Flags (reserved)" byte the documented Battery Notification layout requires at that position
(`PROTOCOL.md` §4.3 Option A's table) — structurally more consistent with an Account Key
Filter/rotating-salt frame, exactly `CAP-011-FINDINGS.md` §4's own prior reading. **This session
advances *device attribution*, not the *payload-layout* question** — the two are separate parts
of Option A's open item, and only one moved. Not force-fit into "Option A confirmed," per
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5's explicit instruction.

### 12.5 New: cross-channel timing synchronization extends to DLCI 0x02 (🟡 HYPOTHESIS, new)

`PROTOCOL.md` §4.3's changelog already records (`CAP-009`, 2026-08-23) that Option B (DLCI 0x04
`Group 0x03 Code 0x03`), Option C (HFP `AT+BIEV`), and Option E (DLCI 0x08) fire "in near-lockstep"
for the same underlying battery change. **This session's periodic push (already documented in
§4/§5 above as occurring on DLCI 0x02/0x04/0x08 simultaneously) shows the same synchronization
additionally includes DLCI 0x0c's `AT+BIEV`:**

| DLCI 0x08 push (§4/§5) | `AT+BIEV=2,100` (§12.1) | Δ |
|---|---|---|
| 06:39:51.205 | 06:39:51.217 | 12ms |
| 06:40:01.283 | 06:40:01.292 | 9ms |
| 06:41:11.368 | 06:41:11.375 | 7ms |
| 06:41:21.446 | 06:41:21.458 | 12ms |
| 06:41:31.071 | 06:41:31.089 | 18ms |
| 06:42:21.453 | 06:42:21.465 | 12ms |
| 06:42:31.044 | 06:42:31.053 | 9ms |

7 of 7 occurrences fire within 7–18ms of each other, across DLCI 0x02, 0x04, 0x08, **and** 0x0c
simultaneously — extends the already-known phenomenon from 3 mechanisms to (at least) 4,
suggesting a single shared underlying trigger (plausibly Buds-side, e.g. a periodic
connection-supervision tick) rather than four independently-timed push loops. 🟡 HYPOTHESIS, one
session — the *existence* of near-lockstep timing is already 🟢 FACT-adjacent per `CAP-009`, this
session only adds DLCI 0x02 to the set of channels observed moving together.

### 12.6 New: DLCI 0x02's periodic push, verified decode — structurally similar to, but not a match for, Option E's battery triple (🔴 OPEN QUESTION, not claimed)

```
$ tshark -r CAP-036-btsnoop_hci.log -Y "frame.number==2048" -T fields -e btrfcomm.dlci -e frame.p2p_dir -e data.data
0x02  1  7e00a5032a2510f3d8ddd58634180032120a04086410011204086410021a04086410023a06080110011800080710151dea71de7d5e2590821ee6175049487e
```

HDLC-unescaped, CRC-32 (IEEE 802.3/zlib) **verified valid** (`17504948` received = `17504948`
calculated, little-endian) — a genuine, well-formed `pw_hdlc` frame, addr=0, ctrl=`0xa5`. Payload
(54 bytes): `032a2510f3d8ddd58634180032120a04086410011204086410021a04086410023a06080110011800080710151dea71de7e2590821ee6`.
Field 5 (`2a`, len 37) contains a nested field 6 (`32`, len 18) with **three repeated 4-byte
sub-messages** — `0a 04 08 64 10 01`, `12 04 08 64 10 01`, `1a 04 08 64 10 02` — each decoding to
`[tag1=0x64(100), tag2=0x01 or 0x02]`.

**Explicitly not claimed as a match for Option E's battery-triple:** Option E's confirmed shape
(`PROTOCOL.md` §4.3 Option E) is `[value, flag, index]` — **3** fields per entry. This structure
has only **2** fields per entry (value + one trailing number), and that trailing number is `01,
01, 02` rather than a clean `1, 2, 3` index sequence. The `100` value repeating three times is
consistent with this all-100%-battery session, but with only two structurally-similar-but-different
fields per entry and no index sequence to check against Left/Right/Case, this is **not** decoded
as confirmed battery content — recorded as a new, distinct 🔴 open question (§11), per
`AGENTS.md` §13.6's zero-creativity rule: the bytes don't determine a reading beyond "structurally
similar," so no further interpretation is offered.

### 12.7 Conclusions

**Reconfirmed this session (no status change, additional replication data point):** Option C
(HFP `AT+BIEV`/`battchg`, `ADR-015`), Option E (DLCI 0x08 per-earbud+case battery, `ADR-014`),
firmware wire-baseline `"release_5.203"` (`ADR-012`), DLCI 0x04 Device Information Model ID/BLE
address/firmware fields (🟢 FACT since `CAP-002`, now mechanically copied into `PROTOCOL.md` §0.1
— see §13).

**New this session, at 🟡 HYPOTHESIS — not proposed for FACT:** the device-attribution advance for
Option A (§12.4), the extended cross-channel timing synchronization (§12.5), and the DLCI 0x02
periodic push's partial, non-matching structural resemblance to Option E (§12.6, left as 🔴 OPEN
QUESTION, not a HYPOTHESIS, per its own ambiguity).

## 13. Downstream updates from this bonus analysis

- `PROTOCOL.md` §0.1 — added `Code 0x01`/`Code 0x02`'s already-established FACT status (mechanical
  documentation-gap fix, citing `CAP-002-FINDINGS.md` §3 — no new sign-off needed).
- `PROTOCOL.md` §4.3 Option A — added the device-attribution HYPOTHESIS (§12.4), clearly separated
  from the still-open payload-layout question.
- `PROTOCOL.md` §4.3 (cross-reference note) — extended the existing near-lockstep observation to
  include DLCI 0x02 (§12.5).
- `PROTOCOL.md` §6 — 2 new 🔴 open questions added (§12.4's payload-layout non-match restated as
  unchanged, and §12.6's DLCI 0x02 structural question).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-036-2026-09-04_06-35-58_06-41-18-Group_AC/CAP-036-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-036-2026-09-04_06-35-58_06-41-18-Group_AC/CAP-036-FINDINGS
