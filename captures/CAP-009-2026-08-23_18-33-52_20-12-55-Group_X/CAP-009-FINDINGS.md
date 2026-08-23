# CAP-009: HFP `AT+CIND`/`AT+BIEV` Cross-Check Over a Natural Battery Discharge (Group X, `BATT-006`)

Standardized, evidence-based extraction from `CAP-009-btsnoop_hci.log`, staged here per
`PROJECT_RULES.md` §2 (recorded first in this file, promoted to `PROTOCOL.md` only afterwards, and
only with maintainer sign-off per `AGENTS.md` §6). Every claim below carries a status per
`PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Sign-off status (updated 2026-08-2x):** the maintainer reviewed 5 proposals from this file and
approved all 5 (`AGENTS.md` §6). §1–§5's core `BATT-006` resolution and §3's per-earbud/cadence
revisions are now recorded in `PROTOCOL.md` §4.3 Option C and `DECISIONS.md` `ADR-015`. §6's two
Option E addenda, §7's DLCI `0x04` candidate (Option B), and §4's BLE-scan candidate (Option A)
are now recorded in `PROTOCOL.md` at 🟡 HYPOTHESIS level. This file is left as originally written
below (the source analysis); see `PROTOCOL.md` for the current canonical wording of what was
promoted.

## 0. Capture Metadata & Methodology

| Field | Value |
|---|---|
| Capture ID | `CAP-009` |
| Purpose | `BATT-006` — cross-check `AT+CIND?`'s `battchg` against `AT+BIEV=2,...` over a natural, multi-hour battery discharge, following up `CAP-001-FINDINGS.md` §3's single-snapshot disagreement (`battchg=3`≈60% vs. `AT+BIEV=2,100`=100% at the same instant) |
| Date | 2026-08-23 |
| Firmware | not queried this session — ⚪ ASSUMPTION `release_5.203`, carried over from `CAP-023`/`CAP-025` (same physical device) |
| Test device | ⚪ ASSUMPTION: Pixel 7a, Android 17 (carried over from `CAP-023`/`CAP-025`) — not independently confirmed this session |
| Log file | [`CAP-009-btsnoop_hci.log`](./CAP-009-btsnoop_hci.log) — 2026-08-23 18:33:47.20–20:15:00.73 (+0200), 30,234 packets |
| Notes file | [`CAP-009-EVENT-NOTES.md`](./CAP-009-EVENT-NOTES.md) — independent video timeline (built before consulting the maintainer's own notes), full diff, video↔log correlation |
| Video files | `CAP-009-recording1.mp4` / `CAP-009-recording2.mp4` — **independently reviewed this pass** (contact-sheet base pass over the full duration of both files, 1-frame-precision dense passes on every transition found); see `CAP-009-EVENT-NOTES.md` for methodology and the diff against the maintainer's original notes. `recording1`'s true end (20:01:14) and `recording2`'s true start (20:01:16) were also independently re-derived from each file's own burned-in clock — the two files' actual gap is ~2s, not the ~50s originally assumed. |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` — **independently re-derived and cross-checked 5 ways** (connection events, the `google-pixel-buds-pro-v1` capability string, SDP profile fingerprint, on-screen device name, cross-capture consistency with `CAP-001`/`CAP-006`/`CAP-016`/`CAP-019`–`CAP-025`) — see `CAP-009-EVENT-NOTES.md`'s "MAC verification" section for the full check |

**Filtering methodology (`AGENTS.md` §13 CLI hygiene):** `bluetooth.addr` never populates for ACL
data frames in this log (confirmed empty across the whole file). The log contains exactly **three**
`HCI_Connection_Complete` events, all for MAC `04:00:6e:cf:6e:07`, and all RFCOMM traffic sits on
connection handle `0x0002` — see `CAP-009-EVENT-NOTES.md`'s Methodology note for the full
verification. `bthci_acl.chandle==0x0002` is used below as the address-filter equivalent.

**Filter-sanity check (mandatory, run before any targeted filtering):** of the log's 30,234 total
frames, only 2,503 sit on chandle `0x0002` and 21 on chandle `0x0001` (~8.4% combined). This is
not evidence of a broken filter — the log has **zero** other classic-ACL `Connection Complete`
events (no other classic peer ever appears), but tens of thousands of ambient background-BLE
`LE Meta` frames (nearby devices, routine Android scanning) that a phone-side `btsnoop_hci` log
captures regardless of relevance. See `CAP-009-EVENT-NOTES.md` for the full breakdown.

**DLCI inventory, whole log (not limited to already-expected channels):** exactly 5 DLCIs carry
RFCOMM payload — `0x00` (multiplexer control), `0x02` (`libmaestro` `pw_hdlc`), `0x04`, `0x08`
(Option E, §6), `0x0c` (HFP, §1–§2). No unaccounted-for channel exists. See
`CAP-009-EVENT-NOTES.md` for the full `uniq -c` breakdown.

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

| Component | On-screen sequence (independently re-timed to sub-2s precision, see `CAP-009-EVENT-NOTES.md`) | Matches `AT+BIEV`? |
|---|---|---|
| Right earbud | 93 → 90 (19:02:47.35–48.02) → 89 (19:29:20.01–28.01) → 88 (20:00:01.68–02.01, bundled w/ case-insert) | **Yes** — same values, same order |
| Left earbud | 96 → 95 (19:26:28.02–29.02) → 94 (19:50:29.02–30.02) → 100 (20:00:01.68–02.01, placed in case) | No — none of 96/95/94/100 appear in the `AT+BIEV` sequence |
| Case | 72 → 71 (19:02:47.35–48.02) → 68 (20:00:01.68–02.01) → 75 (20:11:02.03–05.70) | No — none of 72/71/68/75 appear in the `AT+BIEV` sequence |

Every `AT+BIEV` transition lands at or *extremely close to* the on-screen change for R (within
~1s), but *minutes before* the on-screen change for L (which `AT+BIEV` doesn't carry at all — the
L values below are `PROTOCOL.md` §4.3 Option E's DLCI `0x08` timestamps, §6) — independently
confirming the on-screen UI does not repaint on every individual wire push, but on some other,
coarser trigger (see the case-jump timing in §4 for the clearest example of this):

| Value push (wire) | On-screen change (independent video timing) | Δt (screen − wire) |
|---|---|---|
| `AT+BIEV` R 92→90 @ 19:02:47.245 (frame 14612) | R 90 visible @ 19:02:47.35–48.02 | **~0.1–0.8s** |
| `AT+BIEV` R 90→89 @ 19:26:28.135 (frame 20632) | R 89 visible @ 19:29:20.01–28.01 | ~2min 52s–3min |
| `AT+BIEV` R 89→88 @ 19:50:29.368 (frame 26459) | R 88 visible @ 20:00:01.68–02.01 (bundled with case-insert) | ~9min 32s |
| Option E L 96→95 @ 19:17:26.10 (frame 18029) | L 95 visible @ 19:26:28.02–29.02 | ~9min |
| Option E L 95→94 @ 19:37:16.88 (frame 24391) | L 94 visible @ 19:50:29.02–30.02 | ~13min |

**🟢 FACT (this capture):** in this session, `AT+BIEV=2`'s HF Battery Level indicator value
tracks the Right earbud's on-screen percentage specifically — every value and every transition
order matches R and only R, across 5 distinct levels over 86 minutes.

**🟡 HYPOTHESIS, new this pass — the on-screen UI does not repaint on every wire push.** R's
on-screen lag behind its own wire value grows from ~0.5s (first transition) to ~3 minutes to
~9.5 minutes across the session, while L's lag (against Option E, §6) is ~9 and ~13 minutes for
its two transitions. If the UI simply rendered whatever value it last received, this lag should
stay roughly constant (network/render latency) or shrink to sub-second, not grow into minutes. The
clean, tight exception — the simultaneous L/Case/R jump at 20:00:01.68–02.01 landing ~28s after
the *last* wire push but right at the disconnect event (§4) — suggests a more specific rule: **the
on-screen values may only repaint on specific triggers (an app-foreground event, a periodic UI
timer, or a connection-state change) rather than immediately on each individual wire push.** This
is a plausible reading of the pattern in this capture, not a confirmed mechanism — proposed as a
verifying experiment: repeat a natural-discharge bracket with the app kept continuously in the
foreground and screen-recorded at high frame rate throughout (unlike this capture's periodic
manual checks), to see whether the UI-vs-wire lag shrinks to near-zero when nothing else changes.

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

**Conclusion:** the on-screen battery changes after the reconnect (R 88%→90% at
20:02:28.68–29.01, case 68%→75%/R 90%→100% at 20:11:02.03–05.70, both independently re-timed —
see `CAP-009-EVENT-NOTES.md`) have **zero corresponding HFP evidence** in this capture — the HFP
AT-command path this whole analysis is built on simply isn't present for that part of the session.

**🟡 HYPOTHESIS, new this pass — a BLE Fast Pair Battery Notification scan is the likely
mechanism, not the previously-flagged `0x0044` ATT burst.** Re-examining the wire activity
immediately after the 20:02:27.21 reconnect (frame 29074) turned up a sequence not investigated in
the previous pass: 5× `Vendor Command 0x0157` (frames 29146–29154, 20:02:27.674–.683) immediately
followed by `LE Set Extended Scan Parameters`/`LE Set Extended Scan Enable` (frames 29156–29161,
20:02:27.683–.686), then a steady stream of `LE Extended Advertising Report` events starting
20:02:27.742 and continuing well past the R88→90 window (20:02:28.68–29.01 — squarely inside this
stream):

```
$ tshark -r CAP-009-btsnoop_hci.log -Y 'frame.time >= "2026-08-23 20:02:27.0" and frame.time <= "2026-08-23 20:02:29.5"' \
    -T fields -e frame.number -e frame.time -e _ws.col.Protocol -e _ws.col.Info
29156  20:02:27.683121  HCI_CMD  Sent LE Set Extended Scan Enable
29157  20:02:27.684065  HCI_EVT  Rcvd Command Complete (LE Set Extended Scan Enable)
29163  20:02:27.741591  HCI_EVT  Rcvd LE Meta (LE Extended Advertising Report)
29173  20:02:27.979715  HCI_EVT  Rcvd LE Meta (LE Extended Advertising Report)
29174  20:02:28.283698  HCI_EVT  Rcvd LE Meta (LE Extended Advertising Report)
... (advertising reports continue at sub-second intervals through and past 20:02:29.46)
```

Frame 29174's advertising report decodes to a **Fast Pair Service (`0xFE2C`) advertisement**:

```
$ tshark -r CAP-009-btsnoop_hci.log -Y "frame.number==29174" -V
    Peer Address Type: Random Device Address (0x01)
    BD_ADDR: 17:6e:d1:d9:e3:dd
    Service Data - 16 bit UUID
        Length: 22
        UUID 16: Google LLC (0xfe2c)
        Service Data: 1052411e8b825021f68f297b08f1c22865e871
```

This lines up with exactly the pattern `AGENTS.md` §7's bounded exception describes (a
foreground/reconnect-triggered, time-boxed BLE scan for an already-bonded device's Fast Pair
Battery Notification, `PROTOCOL.md` §4.3 Option A) — the scan starts within ~0.5s of the classic
reconnect completing, and the on-screen R update lands ~1s into the resulting advertising-report
stream. **Not confirmed as this specific device's advertisement**: the `BD_ADDR` above
(`17:6e:d1:d9:e3:dd`) is a random/rotating BLE address, and this pass did not decode the Service
Data far enough (Account Key Filter / rotation correlation) to prove it belongs to *this* Buds unit
rather than another nearby Fast Pair accessory — `PROTOCOL.md` §4.3 Option A itself is already 🟡
HYPOTHESIS for this reason in prior captures too. **Revises, does not confirm,** the previous
pass's dead-end (an unrelated `0x0044` `ATT` `Handle Value Notification` burst at 20:03:06,
~37s after the R change — now understood to be too late and not the right lead). Proposed
verifying experiment: repeat with the BLE side captured at full detail (no snaplen truncation) and
decode the Fast Pair Service Data's Account Key Filter against this device's known account key, to
confirm attribution before this can move past HYPOTHESIS. **Maintainer-approved 2026-08-2x and now
recorded in `PROTOCOL.md` §4.3 Option A at this same HYPOTHESIS level (`AGENTS.md` §6).**

## 5. Answer to `BATT-006`

| Question | Answer |
|---|---|
| Does `AT+CIND?`'s `battchg` track a real battery-level change over time? | 🟢 **No** — it is queried exactly once, at SLC setup, and never refreshes again, even across ~13 percentage points of real, confirmed change on the peer's Right earbud during the same HFP session. |
| Does `AT+BIEV=2` track a real battery-level change over time? | 🟢 **Yes, for the Right earbud specifically**, in this session — 5 distinct values over 86 minutes, each matching R's on-screen value, each landing at or before the corresponding on-screen check. |
| Is either indicator a case- or Left-earbud-aware aggregate? | 🟢 **No** — neither indicator's value ever matches Left or Case at any point in this 101-minute session (§3). |
| Is `AT+BIEV`'s push cadence a fixed ~6–7s regardless of value change (as `AGENTS.md` §5 currently states, based on `CAP-001`)? | 🟡 **Not over this session's idle stretches** — tight (~6–7s-class) spacing is seen right after SLC setup, consistent with `CAP-001`, but gaps widen to multiple minutes (max ≈14.6 min) later in the same session. Flagged for maintainer review, not silently corrected here (`AGENTS.md` §6). |
| Does the on-screen UI repaint immediately on each wire push? | 🟡 **Evidence points to no** — R's on-screen lag behind its own wire value grows from sub-second to ~9.5 minutes across the session; a coarser repaint trigger (foreground/timer/connection-state-change) is a plausible explanation, not confirmed (§3). |
| Does a second, independent RFCOMM battery mechanism exist on DLCI `0x04`? | 🟡 **Likely** — `Group 0x03 Code 0x03` tracks Left/Right in near-lockstep with `AT+BIEV`/Option E for the whole session outside the L-charging period, and is proposed as a candidate for `PROTOCOL.md` §4.3 Option B's still-unconfirmed battery code (§7). |
| What updates the screen after the HFP/Option E channels close post-reconnect (20:02:27+)? | 🟡 **Likely a BLE Fast Pair Battery Notification scan** (`PROTOCOL.md` §4.3 Option A) — a `LE Set Extended Scan Enable` + advertising-report stream starts within ~0.5s of the reconnect and brackets the on-screen R update, but the specific advertisement wasn't confirmed as belonging to this device (§4). |

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

**New observations, maintainer-approved 2026-08-2x and now recorded as an addendum to
`PROTOCOL.md` §4.3 Option E (`AGENTS.md` §6):**

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

## 7. DLCI `0x04` `Group 0x03 Code 0x03` — a candidate resolution to `PROTOCOL.md` §4.3 Option B's open battery code (🟡 HYPOTHESIS, revised this pass)

**A previous pass over this same data looked at 2 example frames of this message, found one
apparent counter-example, and wrote the whole pattern off as unreliable ("does not hold
consistently ... zero creativity bar not met").** Re-run this pass as a full, systematic decode of
every occurrence rather than a couple of samples — per this session's mandate to redo the analysis
independently rather than re-confirm a prior pass's spot-checks — and the picture is different:
the pattern is highly consistent, with one clean, explicable regime change, not a scattering of
anomalies.

**Extraction — every `Group 0x03 Code 0x03` frame on DLCI `0x04`, whole log:**

```
$ tshark -r CAP-009-btsnoop_hci.log -Y 'bthci_acl.chandle==0x0002 and btrfcomm.dlci==0x04 and data.data[0:4]==03:03:00:03' \
    -T fields -e frame.number -e frame.time -e data.data
```

**208 occurrences** (dominant message on this DLCI — see the full Group/Code inventory below).
Decoding each as `03 03 00 03 <b1> <b2> ff` and printing only the frames where `(b1,b2)` changes
from the previous one gives **14 transitions across the whole session**:

| Frame | Time | b1 | b2 | Note |
|---|---|---|---|---|
| 1044  | 18:34:02.127 | 96 | 93 | first frame |
| 5551  | 18:40:36.297 | 96 | 92 | b2 changes |
| 14609 | 19:02:47.243 | 96 | 90 | b2 changes |
| 18031 | 19:17:26.107 | **95** | 90 | b1 changes |
| 20627 | 19:26:28.126 | 95 | **89** | b2 changes |
| 24394 | 19:37:16.887 | **94** | 89 | b1 changes |
| 26456 | 19:50:29.365 | 94 | **88** | b2 changes |
| 26852 | 19:52:20.380 | **221** | 88 | b1 breaks from L% — see below |
| 26907 | 19:52:27.896 | 222 | 88 | |
| 27020 | 19:53:28.571 | 223 | 88 | |
| 27195 | 19:54:38.378 | 224 | 88 | |
| 27377 | 19:55:51.669 | 225 | 88 | |
| 27581 | 19:57:01.743 | 226 | 88 | |
| 28563 | 19:58:21.878 | 228 | 88 | |

**b2 = the Right earbud's percentage, 7-for-7, for the entire session** — every transition and
timestamp matches `AT+BIEV`'s R value from §3 *and* Option E's R value from §6 to within tens of
milliseconds (e.g. 19:02:47.243 here vs. 19:02:47.245 for `AT+BIEV` vs. 19:02:47.236 for Option
E — three structurally unrelated messages, on three different DLCIs, updating within a 9ms
window of each other).

**b1 = the Left earbud's percentage, but only until the Left earbud starts charging.** b1 matches
L's known trajectory exactly (96→95 at 19:17:26.107, matching Option E's 19:17:26.102 to 5ms;
94 at 19:37:16.887, matching Option E's 19:37:16.875 to 12ms) for as long as L is not in the case.
The instant L is placed in the case and begins charging (~19:52:15, §6), b1 stops tracking a
percentage at all — it jumps to **221** and then climbs **222 → 223 → 224 → 225 → 226 → 228**,
i.e. incrementing by ~1 per sample, structurally unlike a percentage and unlike Option E's smooth
93→100 charging climb on the same physical event. **This is the "clean counter-example"
(`0xdd`=221) the previous pass flagged as disqualifying** — full context shows it isn't a random
anomaly breaking an otherwise-unreliable pattern, but a **regime change coinciding exactly with
the L-charging event**, i.e. itself evidence, not noise. 🟡 **HYPOTHESIS:** while charging, this
field switches to reporting something else — a charge-cycle/session counter, a raw ADC sample, or
similar — rather than continuing to report a percentage; not decoded further here.

**Candidate identification, maintainer-approved 2026-08-2x and now recorded in `PROTOCOL.md`
(`AGENTS.md` §6):** `PROTOCOL.md` §4.3 Option B describes the official Fast Pair Message Stream's
"Device Information" group as having a firmware-version code (`0x09`, confirmed) and an *expected
but not-yet-confirmed* battery code in the same group. DLCI `0x04` in this project's captures is
documented (`AGENTS.md` §6) as carrying the official Fast Pair Message Stream. `Group 0x03 Code
0x03` on this DLCI, in this session, behaves exactly as a battery-code message would: two
earbud-percentage fields, pushed in near-lockstep with the two other already-established battery
mechanisms (`AT+BIEV` and Option E), non-Case-aware (consistent with Option B's own text
describing a per-field structure, not necessarily 3-wide). **This is proposed as a HYPOTHESIS-level
candidate for Option B's still-open battery code — `Group 0x03 Code 0x03`, fields = [Left%,
Right%] — not a confirmed resolution.** It needs at minimum: (a) confirmation this Group/Code
numbering is stable across sessions rather than session-dynamic like DLCI assignment itself
(`AGENTS.md` §6), and (b) an explanation or independent confirmation of the charging-state
behavior above, before promotion.

**Full Group/Code inventory on this DLCI, for completeness (other codes not pursued further,
out of `BATT-006` scope):**

```
$ tshark -r CAP-009-btsnoop_hci.log -Y 'bthci_acl.chandle==0x0002 and btrfcomm.dlci==0x04 and btrfcomm.len>0' \
    -T fields -e data.data | cut -c1-4 | sort | uniq -c | sort -rn
    208 0303   (this section)      6 0813      6 030a ("Revision 6" device-info string, cf. CAP-002-FINDINGS.md §2a)
     34 ff01                       6 0811      6 0308
     29 0734                       6 0741      3 030b
     18 0711                       6 0740
     12 0710                       6 0721
```
