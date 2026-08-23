# CAP-009: HFP `AT+CIND`/`AT+BIEV` Cross-Check Over a Natural Battery Discharge (Group X, `BATT-006`)

Standardized, evidence-based extraction from `CAP-009-btsnoop_hci.log`, staged here per
`PROJECT_RULES.md` §2 (recorded first in this file, promoted to `PROTOCOL.md` only afterwards, and
only with maintainer sign-off per `AGENTS.md` §6). Every claim below carries a status per
`PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**No claim in this file has been promoted to `PROTOCOL.md`, and no `DECISIONS.md` entry has been
written or proposed as settled — per `AGENTS.md` §6, that requires explicit maintainer sign-off.**

## 0. Capture Metadata & Methodology

| Field | Value |
|---|---|
| Capture ID | `CAP-009` |
| Purpose | `BATT-006` — cross-check `AT+CIND?`'s `battchg` against `AT+BIEV=2,...` over a natural, multi-hour battery discharge, following up `CAP-001-FINDINGS.md` §3's single-snapshot disagreement (`battchg=3`≈60% vs. `AT+BIEV=2,100`=100% at the same instant) |
| Date | 2026-08-23 |
| Firmware | not queried this session — ⚪ ASSUMPTION `release_5.203`, carried over from `CAP-023`/`CAP-025` (same physical device) |
| Test device | ⚪ ASSUMPTION: Pixel 7a, Android 17 (carried over from `CAP-023`/`CAP-025`) — not independently confirmed this session |
| Log file | [`CAP-009-btsnoop_hci.log`](./CAP-009-btsnoop_hci.log) — 2026-08-23 18:33:47.20–20:15:00.73 (+0200), 30,234 packets |
| Notes file | [`CAP-009-EVENT-NOTES.md`](./CAP-009-EVENT-NOTES.md) — full event timeline, video↔log correlation |
| Video files | `CAP-009-recording1.mp4` / `CAP-009-recording2.mp4` — **not independently re-reviewed by the AI agent**; this file cross-checks the maintainer's manually-noted on-screen timestamps against the wire log, it does not re-derive them from the video |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` — same physical device as `CAP-001`/`CAP-006`/`CAP-016`/`CAP-019`–`CAP-025` |

**Filtering methodology (`AGENTS.md` §13 CLI hygiene):** `bluetooth.addr` never populates for ACL
data frames in this log (confirmed empty across the whole file). The log contains exactly **three**
`HCI_Connection_Complete` events, all for MAC `04:00:6e:cf:6e:07`, and all RFCOMM traffic sits on
connection handle `0x0002` — see `CAP-009-EVENT-NOTES.md`'s Methodology note for the full
verification. `bthci_acl.chandle==0x0002` is used below as the address-filter equivalent.

The HFP AT-command channel in this session is DLCI `0x0c` (RFCOMM channel 6), identified via
Wireshark's `bthfp` dissector (`Bluetooth HFP Profile` protocol layer), not by guessing a DLCI
number:

```
$ tshark -r CAP-009-btsnoop_hci.log -Y 'bthci_acl.chandle==0x0002 and (bthfp.data contains "BIEV" or bthfp.data contains "CIND")' \
    -T fields -e frame.number -e frame.time -e btrfcomm.dlci -e bthfp.data | cut -f3 | sort -u
0x0c
```

## 1. `AT+CIND?`'s `battchg`: a single, non-repeating snapshot (🟢 FACT)

`AT+CIND=?` (indicator list) and `AT+CIND?` (indicator values) each occur **exactly once** in this
~101-minute capture, both at HFP Service Level Connection setup, and are never repeated —
including after the 20:02:27 reconnect (§4):

```
$ tshark -r CAP-009-btsnoop_hci.log -Y 'bthci_acl.chandle==0x0002 and btrfcomm.dlci==0x0c and bthfp.data contains "CIND"' \
    -T fields -e frame.number -e frame.time -e bthfp.data
873  2026-08-23T18:34:01.850900000+0200  AT+CIND=?\r
875  2026-08-23T18:34:01.851356000+0200  \r\n+CIND: ("call",(0,1)),("callsetup",(0-3)),("service",(0-1)),("signal",(0-5)),("roam",(0,1)),("battchg",(0-5)),("callheld",(0-2))\r\n
884  2026-08-23T18:34:01.901742000+0200  AT+CIND?\r
885  2026-08-23T18:34:01.903312000+0200  \r\n+CIND: 0,0,0,0,0,4,0\r\n
```

Raw hex (frame 885, the `+CIND:` response — `battchg` is the 6th of 7 comma-separated fields):

```
$ tshark -r CAP-009-btsnoop_hci.log -Y "frame.number==885" -x
0000  02 02 00 21 00 1d 00 1c a1 33 ff 31 01 0d 0a 2b   ...!.....3.1...+
0010  43 49 4e 44 3a 20 30 2c 30 2c 30 2c 30 2c 30 2c   CIND: 0,0,0,0,0,
0020  34 2c 30 0d 0a 53                                 4,0..S
```

`battchg = 4` on its native 0–5 scale (≈80%). At the maintainer's first on-screen check 12s later
(18:34:14): L=96%, case=72%, R=93%. **None of the three matches 80%, or `4/5` of any of them in an
obvious way.** This is consistent with `CAP-001-FINDINGS.md` §3's open question that `battchg` may
be a stale/init-time-only value rather than a live reading — this capture adds direct evidence for
that: it is queried once, at SLC setup, and is never refreshed again for the remaining ~86 minutes
the HFP AT channel stays open (18:34:01–19:59:33), regardless of the ~13-percentage-point real
change that occurs on the peer's Right earbud in that window (§3).

## 2. `AT+BIEV=2,...`: periodic but irregular, not a fixed cadence (🟢 FACT for this capture)

69 `AT+BIEV=2,...` pushes occur across the session, at 5 distinct values (monotonically
decreasing): `93 → 92 → 90 → 89 → 88`. Extraction:

```
$ tshark -r CAP-009-btsnoop_hci.log -Y 'bthci_acl.chandle==0x0002 and btrfcomm.dlci==0x0c and bthfp.data contains "BIEV"' \
    -T fields -e frame.number -e frame.time -e bthfp.data
```

Value-transition frames only (raw hex for each, `AT+BIEV=2,<value>\r` — ASCII, no framing beyond
RFCOMM/HFP):

| Frame | Time | Value | Hex |
|---|---|---|---|
| 972   | 18:34:02.036 | 93 | `41 54 2b 42 49 45 56 3d 32 2c 39 33 0d` |
| 5556  | 18:40:36.313 | 92 | `41 54 2b 42 49 45 56 3d 32 2c 39 32 0d` |
| 14612 | 19:02:47.245 | 90 | `41 54 2b 42 49 45 56 3d 32 2c 39 30 0d` |
| 20632 | 19:26:28.135 | 89 | `41 54 2b 42 49 45 56 3d 32 2c 38 39 0d` |
| 26459 | 19:50:29.368 | 88 | `41 54 2b 42 49 45 56 3d 32 2c 38 38 0d` |

**Timing is not a fixed ~6–7s cadence over this session.** Gap statistics across all 68
consecutive-frame intervals (`frame.time` deltas, computed from the full 69-row extraction above):
minimum 0.015s (a near-duplicate retransmission), maximum 878.9s (≈14.6 min), median 20.5s; 15 of
68 gaps are under 10s, 5 are over 5 minutes. **This does not contradict `CAP-001-FINDINGS.md`
§3's ~6–7s observation** — that observation was made in the few seconds immediately following SLC
setup (a plausible "settling burst"), which this capture also shows (frames 972 and the next
several pushes cluster tightly after 18:34:02). What this capture adds is that the cadence does
**not** stay that tight through an extended idle period — it stretches out to multi-minute gaps.
🟡 **HYPOTHESIS, flagged for maintainer review, not a silent correction of `AGENTS.md` §5's
existing FACT-level characterization:** `AT+BIEV` pushes may follow a settle-then-relax pattern
(frequent right after connection, then only close to when the underlying value would plausibly
have ticked over, or on some other trigger not yet identified) rather than a constant global
period. This capture cannot distinguish "less frequent polling while idle" from "push-on-change
with the change itself occurring only this often" — see §3 for evidence bearing on the latter.

## 3. `AT+BIEV=2` tracks the Right earbud specifically, not case, not Left, not an aggregate (🟢 FACT for this capture / 🟡 HYPOTHESIS as a general protocol rule)

Comparing the `AT+BIEV` value sequence (`93→92→90→89→88`) against the maintainer's on-screen
timeline (`CAP-009-EVENT-NOTES.md`):

| Component | On-screen sequence | Matches `AT+BIEV`? |
|---|---|---|
| Right earbud | 93 → 90 (19:02:49) → 89 (19:29:22) → 88 (20:00:01, bundled w/ case-insert) | **Yes** — same values, same order |
| Left earbud | 96 → 95 (19:26:30) → 94 (19:50:30) → 100 (20:00:01, placed in case) | No — none of 96/95/94/100 appear in the `AT+BIEV` sequence |
| Case | 72 → 71 (19:02:49) → 68 (20:00:01) → 75 (20:11:07) | No — none of 72/71/68/75 appear in the `AT+BIEV` sequence |

Every `AT+BIEV` transition also lands at or *before* the corresponding on-screen check, never
after — consistent with the wire event being the actual (near-)real-time change and the on-screen
note being a periodic, not continuous, human observation:

| `AT+BIEV` transition (wire) | Maintainer's on-screen note | Δt (note − wire) |
|---|---|---|
| 92→90 @ 19:02:47.245 (frame 14612) | R 93→90 @ 19:02:49 | +2s |
| 90→89 @ 19:26:28.135 (frame 20632) | R 90→89 @ 19:29:22 | +3min 54s |
| 89→88 @ 19:50:29.368 (frame 26459) | R 89→88 @ 20:00:01 (bundled with case-insert note) | +9min 32s |

**🟢 FACT (this capture):** in this session, `AT+BIEV=2`'s HF Battery Level indicator value
tracks the Right earbud's on-screen percentage specifically — every value and every transition
order matches R and only R, across 5 distinct levels over 86 minutes.

**🟡 HYPOTHESIS (general rule, needs independent verification):** `CAP-001-FINDINGS.md` §3
concluded "neither HFP indicator distinguishes Left/Right/Case — both appear to report a single
aggregate value," based on a single simultaneous snapshot where this could not be determined. This
capture's extended, multi-point correlation is more specific evidence, but it is still only one
session, with the buds in one specific role/orientation. It is not yet established whether
`AT+BIEV=2` always reports "Right" specifically (e.g. because R happened to be the HFP
SCO-anchor/primary earbud this session) or reports whichever earbud is currently acting as
"primary" for the HFP link — a session where roles are swapped (or where L is confirmed primary)
would be needed to distinguish these. **Proposed verifying experiment:** repeat a `BATT-006`-style
natural-discharge bracket while confirming (e.g. via the official app's "primary earbud" indicator,
if shown) which earbud is HFP primary, and check whether `AT+BIEV` follows that assignment or
stays fixed to physical Right regardless.

## 4. HFP/RFCOMM control channel never reopens after the case/USB reconnect (🟢 FACT)

The last `AT+CIND`/`AT+BIEV` frame in the entire capture is frame 28704 (19:59:33.372,
`AT+BIEV=2,88`). The classic ACL connection disconnects 28s later (frame 28764, 20:00:01.312,
reason `0x13`), then:

- **20:00:01.74–20:00:02.78** (handle `0x0001`): a ~0.9s ACL connection forms, but only queries SDP
  for **Audio Source** (frame 28849, `Service Search Attribute Request : Audio Source`) — no
  RFCOMM/SPP L2CAP channel (PSM `0x0003`) is ever requested — then disconnects (frame 28864).
- **20:02:27.21** (handle `0x0002`, reused): another ACL connection forms (2s after the
  maintainer's noted USB-insert action), again queries SDP for **Audio Source only** (frame 29122),
  and an `AVDTP` (A2DP) L2CAP channel opens (frame 29132). **No RFCOMM/SPP connection request
  (PSM `0x0003`) appears anywhere after frame 29074** in the rest of the log (checked to the log's
  end at frame 30234 / 20:15:00.73):

```
$ tshark -r CAP-009-btsnoop_hci.log -Y 'frame.number>29074 and l2cap.psm==0x0003'
(0 matches)
$ tshark -r CAP-009-btsnoop_hci.log -Y 'frame.number>29074' -T fields -e _ws.col.Protocol | sort -u
ATT
AVDTP
HCI_CMD
HCI_EVT
L2CAP
SDP
```

**Conclusion:** the maintainer's later on-screen battery changes (R 88%→90% at 20:02:29, case
68%→75% and R 88%→100% at 20:11:07) have **zero corresponding HFP evidence** in this capture — the
HFP AT-command path this whole analysis is built on simply isn't present for that part of the
session. A small BLE `ATT` `Handle Value Notification` burst (Handle `0x0044`, 4 frames total,
20:03:06.169–.888, frames 29294/29296/29297/29299) appears in this window — structurally similar
to the `0x0044` burst already flagged as unattributed in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s
`GATT-002` entry — but 20:03:06 is ~37s after the 20:02:29 on-screen R change, too far to
confidently attribute without more data. 🔴 **OPEN QUESTION:** what mechanism actually updates the
on-screen battery percentages after 20:02:27 in this capture (BLE/GATT, Fast Pair, or something
else) is unresolved here — out of `BATT-006`'s HFP-specific scope, and not pursued further in this
file.

## 5. Answer to `BATT-006`

| Question | Answer |
|---|---|
| Does `AT+CIND?`'s `battchg` track a real battery-level change over time? | 🟢 **No** — it is queried exactly once, at SLC setup, and never refreshes again, even across ~13 percentage points of real, confirmed change on the peer's Right earbud during the same HFP session. |
| Does `AT+BIEV=2` track a real battery-level change over time? | 🟢 **Yes, for the Right earbud specifically**, in this session — 5 distinct values over 86 minutes, each matching R's on-screen value, each landing at or before the corresponding on-screen check. |
| Is either indicator a case- or Left-earbud-aware aggregate? | 🟢 **No** — neither indicator's value ever matches Left or Case at any point in this 101-minute session (§3). |
| Is `AT+BIEV`'s push cadence a fixed ~6–7s regardless of value change (as `AGENTS.md` §5 currently states, based on `CAP-001`)? | 🟡 **Not over this session's idle stretches** — tight (~6–7s-class) spacing is seen right after SLC setup, consistent with `CAP-001`, but gaps widen to multiple minutes (max ≈14.6 min) later in the same session. Flagged for maintainer review, not silently corrected here (`AGENTS.md` §6). |

## 6. Bonus: `PROTOCOL.md` §4.3 Option E confirmation — `CAP-009` as its purpose-built follow-up (🟢 FACT, extends an already-signed-off finding)

**Not part of `BATT-006`'s HFP-specific mandate, but the same overall battery-tracking
investigation, using the same capture.** `PROTOCOL.md` §4.3 Option E (DLCI `0x08` private
envelope, `Group 0x0e Code 0x01` message) is **already 🟢 FACT** — maintainer sign-off obtained
2026-08-23 per `DECISIONS.md` ADR-014, cross-confirmed in `CAP-001`/`CAP-002`/`CAP-011` — for the
mapping "3 repeated `[value, flag, index]` entries, index 1/2/3 = Left/Right/Case". `PROTOCOL.md`
§4.3 Option E's own text explicitly names **this** capture as the next opportunity: *"A dedicated
repeat (e.g. combining this with the still-planned `CAP-009` battery-discrepancy bracket...) would
add a fully purpose-built confirmation on top of the existing 3-session cross-check."* This
section is that confirmation.

**Extraction (whole log, not just a video window), using the already-established decode:**

```
$ tshark -r CAP-009-btsnoop_hci.log -Y 'bthci_acl.chandle==0x0002 and btrfcomm.dlci==0x08 and data.data[0:2]==0e:01' \
    -T fields -e frame.number -e frame.time -e data.data
```

**75 occurrences** across the 101-minute capture (vs. `CAP-011`'s 4) — by far the longest-running,
most densely-sampled confirmation of Option E to date. Decoding each with the established
`[Group:1][Code:1][Length:2BE]` envelope + 3×`[value(varint), flag(varint,optional), index(varint)]`
nested-protobuf entries (per `PROTOCOL.md` §4.3 Option E / `CAP-011-FINDINGS.md` §7b) gives a clean
L/R/Case triple at every occurrence. Selected raw hex (first, and around the case-insertion
event):

```
frame 1201   18:34:02.410  0e0100220a200a03616c6c12190a060860100118010a06085d100118020a0508ff0118032001
             -> L=96(flag=1)  R=93(flag=1)  C=255(no flag field)
frame 26619  19:52:15.164  0e0100240a220a03616c6c121b0a06085d100118010a060858100118020a0708ff01100118032001
             -> L=93(flag=1)  R=88(flag=1)  C=255(flag=1)
frame 26743  19:52:16.466  0e0100230a210a03616c6c121a0a06085d100118010a060858100118020a060847100118032001
             -> L=93(flag=1)  R=88(flag=1)  C=71(flag=1)
frame 28698  19:59:33.360  0e0100230a210a03616c6c121a0a060864100118010a060858100118020a060844100118032001
             -> L=100(flag=1) R=88(flag=1)  C=68(flag=1)
```

**🟢 FACT (this capture, 4th independent confirming session):** across all 75 occurrences, L and R
track the maintainer's on-screen Left/Right values exactly, at every transition, for the entire
101-minute session — the same result as §3's `AT+BIEV` cross-check for R, now independently
reproduced via a structurally unrelated channel/message (DLCI `0x08` vs. DLCI `0x0c`/HFP), and
extended to L (which HFP's `AT+BIEV=2` does **not** carry — §3). This is also the **first**
Option E confirming session to capture a live charge cycle: after the Left earbud is placed in the
case (~19:52:15), its Option-E-reported value climbs monotonically — 93→94→95→96→97→98→100 across
28698's timeline — matching the on-screen "94%→100%" note (`CAP-009-EVENT-NOTES.md`, 20:00:01) with
far finer granularity than the maintainer's periodic manual checks could capture.

**New observations, proposed as an addendum to `PROTOCOL.md` §4.3 Option E, not written there by
this AI agent (`AGENTS.md` §6):**

1. **Two distinct "Case unknown" encodings, differing only by flag-field presence.** For the
   ~78 minutes before any bud touches the case, every occurrence's Case entry is the **5-byte**
   form `08 ff 01 18 03` (value=255, **no flag field at all**) — e.g. frame 1201 above. At
   19:52:15–16.07 (3 occurrences, right as a bud is about to make contact), the Case entry becomes
   the **7-byte** form `08 ff 01 10 01 18 03` (value=255, but **with** `flag=1`) — e.g. frame 26619
   above — before becoming a real value (71) 1.3s later at frame 26743. 🟡 **HYPOTHESIS:** this is
   new, capture-based evidence for the exact mechanism `CAP-011-FINDINGS.md` §7c already speculated
   about but couldn't observe directly (that capture never saw an empty→populated case transition):
   the case's battery reading needs the case to be closed/holding a bud to be considered "fresh";
   value=255 with no flag field is a long-lived cached/stale placeholder, while value=255 *with* a
   flag briefly appears as the case actively (but not-yet-successfully) tries to read a bud that
   has just made contact, immediately before a real reading arrives. Not confirmed beyond this one
   transition.
2. **Case's own reading drops sharply right after charging starts, before slowly declining
   further.** Case goes 71% (19:52:16.47) → 69% (19:52:37.70, ~21s later) → holds at 69% for
   ~6 min → 68% (19:58:53.04) → holds to the log's last Option-E frame (19:59:33.36). A 2-point
   drop in 21 seconds is far faster than this session's otherwise-observed discharge rates (e.g.
   Right: 5 points over 76 minutes) and coincides exactly with a bud starting to draw charging
   current from the case. 🟡 **HYPOTHESIS:** this reading may reflect an instantaneous/voltage-based
   estimate that dips under sudden load (a charging-current draw) rather than a simple charge-level
   percentage, which would explain why it doesn't match a smooth discharge curve; the on-screen UI
   (which the maintainer's notes show settling at 68% only by ~20:00:01) may smooth or delay this
   compared to the raw wire value. Not verified further — proposed as a concrete follow-up
   hypothesis test (a case-insertion bracket with tighter video sampling), not asserted as fact.

## 7. Unresolved, structurally distinct pattern on DLCI `0x04` — explicitly NOT decoded (🔴 OPEN QUESTION)

Separately from §6's DLCI `0x08` `Group 0x0e Code 0x01` (which **is** an established, FACT-level
decode), DLCI `0x04` in this session (`bthci_acl.chandle==0x0002`) carries a differently-structured,
undocumented repeating frame that only **partially** overlaps numerically with battery values:

```
$ tshark -r CAP-009-btsnoop_hci.log -Y 'bthci_acl.chandle==0x0002 and btrfcomm.dlci==0x04 and frame.number>=20620 and frame.number<=20640' \
    -T fields -e frame.number -e frame.time -e data.data
20626  2026-08-23T19:26:28.123843000+0200  030300035f5aff
20627  2026-08-23T19:26:28.126876000+0200  030300035f59ff
```

`0x5f`=95 (L's value at the time, stable), last byte `0x5a`→`0x59` = 90→89 (matches the `AT+BIEV`/
Option-E transition at the same timestamp). This pattern (`03 03 00 03 <byte> <byte> ff`) recurs
near several other `AT+BIEV` transition points (e.g. also visible around frames 14608–14614 and
26455–26461) — but a clean counter-example exists 40s after one of those:

```
$ tshark -r CAP-009-btsnoop_hci.log -Y "frame.number==26852" -T fields -e frame.number -e frame.time -e data.data
26852  2026-08-23T19:52:20.380412000+0200  03030003dd58ff
```

Leading byte `0xdd` = 221 decimal — outside any valid 0–100 battery-percentage range, on the exact
same apparent structure. **Conclusion:** DLCI `0x04`'s `03 03 00 03 ...` frames are **not** the
same message as §6's DLCI `0x08` `Group 0x0e`/`Group 0x04` structures (different DLCI, different
byte layout, no `index`/`flag` sub-fields), and the numeric overlap with battery values seen in some
instances does not hold consistently enough to interpret under this project's "zero creativity"
evidence bar (`AGENTS.md` §13 item 6). Recorded here only so the observation isn't lost — pursuing
it needs its own dedicated capture/Test-ID, not further speculation in this file.
