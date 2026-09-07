# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AD, "Get ANC state" reconnect-reliability + dock-state transition, purpose-built repeat (`CAP-037`)

**Status:** ✅ **Captured, video-reviewed, and decoded — see `CAP-037-FINDINGS.md`.** The session ran
far longer than planned (34 reconnects over ~20 minutes, not 5 over ~2 minutes) — see the video
re-pass correction below and `CAP-037-FINDINGS.md` §2 for the full evidence. Folder renamed to
`CAP-037-2026-09-06_06-11-46_06-29-38-Group_AD`.

**Status legend used throughout this file** (`PROJECT_RULES.md` §1, `PROTOCOL.md` §0): 🟢 **FACT**
(directly observed, evidence referenced) · 🟡 **HYPOTHESIS** (unverified, with a stated test) ·
⚪ **ASSUMPTION** (treated as true without verification, with a stated reason) · 🔴 **OPEN
QUESTION** (identified gap, no hypothesis yet). Never write a conclusion in this file without one
of these four labels.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AD, `OBS-004`):** `CAP-036` found DLCI 0x04's
"Get ANC state" (`0x11`) query firing once, on a single reconnect. `DECISIONS.md` ADR-022 has
since promoted the *trigger-reliability* claim to 🟢 FACT — but that promotion rested on
**retrospective** analysis of 10 captures that were never designed for this question (settings-
toggle tests, a pairing repeat, an ANC repeat) — this project has never yet run a **purpose-built**
session whose entire point is this query. This session is that purpose-built repeat, and combines
it with a second, still-open question: `DECISIONS.md` ADR-024 promoted the "Notify ANC state"
`Settable-toggles` byte as a **dock-state indicator** (`0x00` = both earbuds seated in the case,
`0xe8` otherwise) from 7 video-confirmed samples **across different sessions** — but no single
session has yet captured the **within-session transition** itself (buds go from docked to
undocked, or back, while being reconnected each time). This session is designed to close both
gaps in one go: repeated, isolated reconnects, deliberately alternating dock state between
repeats.

**Method:** Pixel 7a, stock Android, official Pixel Buds Companion App installed and **open on the
Device details screen** throughout (so the query, if it fires, has a UI to be attributed to —
matching `CAP-036`'s own baseline), Google Play Services **enabled** (the normal baseline, same as
Group AC). Buds already bonded — this is a **reconnect** repeat, not a fresh pair (do not
"Forget").

**⚠️ The rule this whole capture depends on: touch nothing except the dock state and the
Bluetooth toggle/case lid.** No ANC tap, no EQ change, no settings screen navigation beyond what's
needed to keep Device details on screen. Every DLCI 0x02/0x04/0x08 frame in this log must be
connection-initiated, never a settings-write — if a setting is touched by accident, say so
explicitly in the Event Timeline and treat that window as contaminated.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-037`                      |
|      Group(s)    | AD (`OBS-004` — reconnect-reliability + dock-state-transition repeat; incidental `PAIR-003`) |
|       Date       |                     2026-09-06                      |
| Firmware version | ⚪ ASSUMPTION `release_5.203` (carried over from previous verified captures) |
|   Test device    | Pixel 7a, Android 14. **Official Pixel Buds Companion App, Google Play Services enabled** |
| Video file       | `CAP-037-recording.mp4` — 1072.10s (~17m52s), `06:11:46`–`~06:29:38` local time (video runs far longer than the originally-stated "relevant action 06:11:46–06:13:48" window — see corrected Event Timeline and CAP-037-FINDINGS.md §2) |
| Log file         | `CAP-037-btsnoop_hci.log` — 1212.32s (~20m12s), 29,956 packets, `06:11:52.979`–`06:32:05.299` local time |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` |

**Capture-integrity pre-flight (do this immediately after extraction, before any analysis — per
`CAP-014-FINDINGS.md` §0's method, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §6's snaplen gotcha, and
`CAP-036-EVENT-NOTES.md`'s note that `bluetooth.addr` did not resolve for RFCOMM frames in that
session — check `bthci_evt.bd_addr`/connection-handle instead if the same happens here):**

```
$ capinfos CAP-037-btsnoop_hci.log
# check: "Packet size limit" should read "(not set)" / "inferred: 262144" or similar — NOT a small value

$ tshark -r CAP-037-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'
# expect: mismatches: 0
```

If this fails (truncated), stop before spending analysis time — re-extract via
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3 **step 3's raw path first**, per the same reasoning as
`CAP-036-EVENT-NOTES.md`'s pre-flight note (a truncated log can silently clip the exact short
frames this session is looking for). Record which extraction path was actually used.

## Preparation checklist (before recording)

- [x] Buds are **already bonded** to this Pixel 7a and connect normally. Do **not** "Forget" or
      re-pair.
- [x] Official Pixel Buds Companion App installed and working.
- [x] Google Play Services **enabled** (the normal baseline) — carried `CAP-036`'s verification forward as ⚪ ASSUMPTION.
- [x] No third-party BLE/GATT tool (nRF Connect or similar) used at any point.
- [x] Bluetooth HCI snoop logging enabled and the phone rebooted.
- [x] Video recording with a visible wall-clock overlay ready.
- [x] **Note the starting dock state** before recording: Both buds are fully **docked** in the open case.
- [x] Decide and write down the exact repeat sequence before starting: 5-step alternating sequence (Docked -> Undocked -> Docked -> Undocked -> Docked).

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AD)

Leave a clean few-second buffer between repeats so one repeat's settling traffic doesn't bleed
into the next. Log explicit **window start** and **window end** boundaries for every repeat.

**Recommended default sequence — 5 repeats, alternating dock state, so both the reliability
question and the dock-state-transition question get isolated same-session evidence:**

1. Start video recording (wall-clock overlay visible), confirm HCI snoop logging active. Buds
   start **docked** (both seated in the case, case open, case visible on camera).
2. **Repeat 1 — reconnect from docked.** Toggle Bluetooth ON (or open the case lid if that alone
   triggers reconnect — note which). Note the exact time the connection completes on screen, then
   idle ~5s. Note window end. Do **not** remove the buds from the case this repeat.
3. Toggle Bluetooth OFF (clean disconnect), buffer ~3s.
4. **Repeat 2 — reconnect with buds undocked.** Remove both buds from the case (hold them in hand
   or set them on the table, still on camera, not worn) *before* toggling Bluetooth back ON. Note
   connection-complete time, idle ~5s, note window end.
5. Toggle Bluetooth OFF, buffer ~3s.
6. **Repeat 3 — reconnect from docked again.** Place both buds back in the case, close the lid
   briefly then reopen (or just reseat them, case open), toggle Bluetooth ON. Note times as above.
7. Toggle Bluetooth OFF, buffer ~3s.
8. **Repeat 4 — reconnect with buds undocked**, mirroring repeat 2.
9. Toggle Bluetooth OFF, buffer ~3s.
10. **Repeat 5 — reconnect from docked**, mirroring repeats 1/3, as a final reliability check.
11. Stop video recording and HCI snoop logging. Keep the session short enough to avoid on-device
    log rotation (§2's note).
12. Extract via the raw btsnoop path first (§3 step 3), then run the capture-integrity pre-flight
    above before any analysis.

## Event Timeline

*(Fill in after reviewing the video frame-by-frame against its wall-clock overlay, cross-checked
against `CAP-037-btsnoop_hci.log` via `tshark` — per `AGENTS.md` §13. One row per repeat boundary.
Leave `___` where a value isn't known yet rather than estimating it.)*

**CORRECTED, per Phase 1 full video re-pass (CAP-037-FINDINGS.md §2):** the video (1072.10s) and log
(06:11:52.979–06:32:05.299) run far past the originally-planned 5-repeat, ~2-minute window. Video
review (frames at 06:12:15/06:12:38/06:14:13/06:16:48/06:23:42/06:28:48) plus a full wire-log
extraction of DLCI 0x04's Get/Notify pairs across every reconnect in the entire log shows the
session actually ran **26 reconnects that opened DLCI 0x04 with real payload** (not 5), in six
dock-state blocks, continuing until the case lid was closed around 06:28:48. Critically, the
**planned alternation did not happen as written**: all 5 of the originally-"planned" repeats
(chandles `0x0004`–`0x0008`, 06:12:22–06:14:14) were video-confirmed **docked**, not the intended
Docked→Undocked→Docked→Undocked→Docked sequence — the buds were not actually removed from the case
until sometime between 06:14:14 and 06:15:34. From there the user continued the reconnect cycle,
organically alternating dock state in longer blocks, for ~14 more minutes. This is documented fully,
per-reconnect, in CAP-037-FINDINGS.md §2/§3 (video timestamps are approximate for the untouched
middle of the extra tail — the *wire* timeline below, cross-validated against 6 video spot-checks
that all agree, is the authoritative record for this correction).

| Time (local) | Action / Event | Dock state (wire: `Settable-toggles`, video-spot-checked where noted) | Test-ID | Evidence in `CAP-037-btsnoop_hci.log` |
|---|---|---|---|---|
| `06:11:46` | Start video recording | docked (video) | — | — |
| `06:12:22.65` | Reconnect 1 — Get/Notify (chandle `0x0004`) | docked (`0x00`, video-confirmed 06:12:15) | `OBS-004`, `PAIR-003` | frames 1180/1196 |
| `06:12:49.54` | Reconnect 2 — Get/Notify (chandle `0x0005`) | docked (`0x00`, video-confirmed 06:12:38) | `OBS-004`, `PAIR-003` | frames 3245/3262 |
| `06:13:15.17` | Reconnect 3 — Get/Notify (chandle `0x0006`) | docked (`0x00`) | `OBS-004`, `PAIR-003` | frames 4195/4272 |
| `06:13:43.62` | Reconnect 4 — Get/Notify (chandle `0x0007`) | docked (`0x00`) | `OBS-004`, `PAIR-003` | frames 5111/5128 |
| `06:14:14.04` | Reconnect 5 — Get/Notify (chandle `0x0008`) | docked (`0x00`, video-confirmed 06:14:13) — **all 5 "planned" repeats were docked, not alternating as written** | `OBS-004`, `PAIR-003` | frames 5989/6027 |
| `~06:14:14`–`06:15:34` | Buds physically removed from case (undock event; exact moment not pinned to the second) | transition | — | — |
| `06:15:34.39`–`06:18:05.75` | Reconnects 6–10 (chandles `0x0005`,`0x0006`,`0x0007`,`0x0008`,`0x0009`) | undocked (`0xe8`, video-confirmed 06:16:48) | `OBS-004`, `PAIR-003` | frames 8117–11507 (5 Get/Notify pairs) |
| `06:18:06.03` | Extra Notify on chandle `0x0009` (no new Get) 18s after its own Get, value flips to `0x00` (docked) mid-connection | dock-state flip within one connection — flagged, not fully explained | 🔴 open | frame 12008 |
| `06:18:51.72`–`06:20:02.03` | Reconnects 11–13 (chandles `0x000a`,`0x000b`,`0x000c`) | docked (`0x00`) | `OBS-004`, `PAIR-003` | frames 12700–14457 |
| `06:21:13.95`–`06:23:45.96` | Reconnects 14–18 (chandles `0x0007`,`0x0008`,`0x000a`,`0x000b`,`0x000c`) | undocked (`0xe8`, video-confirmed 06:23:42) | `OBS-004`, `PAIR-003` | frames 16300–19778 |
| `06:24:24.86`–`06:26:08.78` | Reconnects 19–23 (chandles `0x000d`,`0x000e`,`0x000f`,`0x0010`,`0x0011`) | docked (`0x00`) | `OBS-004`, `PAIR-003` | frames 20776–24400 |
| `06:27:01.29`–`06:28:11.80` | Reconnects 24–26 (chandles `0x0007`,`0x0008`,`0x000a`) | docked (`0x00`) | `OBS-004`, `PAIR-003` | frames 26181–29103 |
| `06:28:48` | Case lid closed (video-confirmed) | docked, lid shut | — | — |
| `~06:29:38` | End of video (1072.10s duration) | — | — | — |

Not every Connection-Complete event in the full log (34 total for the Buds' address) produced a DLCI
0x04 Get — several chandles reconnected without DLCI 0x04 carrying real payload afterward, matching
`DECISIONS.md` ADR-022's precisely-scoped trigger condition (channel reopens **and** carries real
Message Stream payload, not any reconnect). 26 reconnects met that condition; all 26 produced a Get
and all 26 were answered by a Notify (see CAP-037-FINDINGS.md §3 for the full table).

**Contamination log:** No settings screens were opened and no ANC/EQ/other control was touched at
any point (confirmed via video spot-checks and the log's own DLCI 0x02 silence — see
CAP-037-FINDINGS.md §4). The only deviation from the written plan is procedural, not a
contamination in the usual sense: the user continued the Bluetooth-toggle/dock-state cycle for far
longer than the 5 originally-planned repeats, and did not alternate dock state on the intended
schedule (all 5 "planned" repeats turned out to be docked). This is additional evidence, not noise —
see CAP-037-FINDINGS.md for the full resulting dataset (26 reconnects instead of 5).

## Decode / Analysis

*(Full decode with the exact command **and** the raw hex bytes per finding belongs in
`CAP-037-FINDINGS.md`, per `PROJECT_RULES.md` §1 rule 4a.)*

```
tshark -r CAP-037-btsnoop_hci.log -Y "bthci_acl.chandle==<Buds chandle> and btrfcomm.dlci==4 and btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data
```
(Use `bthci_evt.bd_addr` to find the Buds' chandle first if `bluetooth.addr` doesn't resolve, per
`CAP-036-EVENT-NOTES.md`'s note.)

- [x] **Does `08 11 00 00` (Get) + `08 13` (Notify) appear on every reconnect that opened DLCI 0x04
      with real payload?** Result: **Yes, 26/26 — zero misses**, across far more than the originally
      planned 5 repeats (see corrected Event Timeline above; full per-reconnect table in
      CAP-037-FINDINGS.md §3). This is the largest single-session replication of ADR-022's trigger
      condition on file to date.
- [x] **Does the `Settable-toggles` byte match dock state on every repeat**, per ADR-024? Result:
      **Yes, 26/26** — every docked-block reconnect reads `0x00`, every undocked-block reconnect
      reads `0xe8`, cross-validated against 6 independent video spot-checks (all agree). One
      anomaly: chandle `0x0009` shows a second Notify 18s after its own Get, with no new Get frame,
      flipping from `0xe8` to `0x00` mid-connection — flagged as a new open question, not silently
      reconciled (CAP-037-FINDINGS.md §5). Bonus: within this session, `Settable-toggles=0x00`
      paired with `Current-state=0x20` (Off) in all 16 docked samples, and `Settable=0xe8` paired
      with `Current=0x80` (Transparency) in all 10 undocked samples — a clean 1:1 co-occurrence,
      plausibly because ANC simply doesn't run while docked (see CAP-037-FINDINGS.md §3).
- [x] **Timing consistency:** Get→Notify latency across all 26 pairs ranges ~11ms–270ms (most
      under 30ms), broadly consistent with `CAP-036`'s ~10.7ms single sample but with more spread —
      see CAP-037-FINDINGS.md §3 for the full per-pair timing table.
- [x] **Three-way outcome:** **(a)** — 26/26 fire, dock state matches on every repeat. Strong
      same-session reproduction of both ADR-022 and ADR-024, proposed as additional evidence (not a
      new promotion). Classification: **(a), with one flagged anomaly (the chandle-`0x0009`
      mid-connection flip) that doesn't fit cleanly into (a)/(b)/(c) — reported as a new open
      question rather than forced into one of the three buckets.**

## Open Questions

*(Add every new 🔴 OPEN QUESTION found here — and copy each into `PROTOCOL.md` §6's matching
subsection per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §8's mandatory rule.)*

- 🔴 Chandle `0x0009`'s second "Notify ANC state" frame (frame 12008, 06:18:06.03) fires ~18s after
  its own Get (frame 11483/11507) with no new Get in between, and its `Settable-toggles` value
  flips from `0xe8` to `0x00` — implying either a dock-state change mid-connection without a
  disconnect (in tension with `DECISIONS.md` ADR-016's "disconnect fires the instant both buds are
  re-docked" finding) or a spontaneous, unprompted second Notify on this specific connection. Not
  resolved by this capture (CAP-037-FINDINGS.md §5).
- 🔴 The planned Docked→Undocked→Docked→Undocked→Docked alternation did not happen on schedule —
  all 5 "planned" repeats were docked, and the real dock-state alternation began organically ~1–1.5
  minutes later than planned, continuing for many more cycles. Not itself a protocol open question,
  but worth noting for future capture-procedure design (buds evidently were not removed from the
  case as quickly as instructed).

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13) —
      confirm `OBS-004`/`PAIR-003` are clearly referenced above.
- [ ] Write `CAP-037-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status from
      `planned` to `captured`/`analyzed`, and fill in the Android/firmware/app-version columns and
      the log path. Update `id_registry.csv`'s `CAP-037` row to match.
- [ ] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `OBS-004` row with a **pointer only**, never a
      restated finding.
- [ ] Copy every new 🔴 OPEN QUESTION into `PROTOCOL.md` §6 (mandatory, §8).
- [ ] **Do not** promote anything in `PROTOCOL.md` to 🟢 FACT, and **do not** write or amend a
      `DECISIONS.md` ADR, without explicit maintainer sign-off — propose it, clearly labelled as a
      proposal (`AGENTS.md` §6, §15). Even a clean 5/5 reproduction of ADR-022/ADR-024 is
      *additional evidence for an already-FACT claim*, not itself grounds for the agent to write a
      new ADR unprompted.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-037-2026-09-06_06-11-46_06-29-38-Group_AD/CAP-037-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-037-2026-09-06_06-11-46_06-29-38-Group_AD/CAP-037-EVENT-NOTES
