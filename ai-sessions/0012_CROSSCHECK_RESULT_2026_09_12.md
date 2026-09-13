# 0012_CROSSCHECK_RESULT_2026_09_12.md — Independent validation and application of Gemini CLI's 0011 review findings

**Number:** 0012
**Category:** CROSSCHECK
**Date:** 2026-09-12
**Title:** Independent validation and application of Gemini CLI's 0011 review findings
**Status:** complete (all 130 actual findings in 0011 processed, across all 45 captures; both items
in §4 resolved 2026-09-13 as mechanical count corrections/clarifications — see each item's own
"Resolved" note — via `ai-sessions/0013_FEATURE_RESULT_2026_09_13.md` Phases 1/2; no maintainer
sign-off was required for either, since neither touched a FACT/ADR)

---

## 0. Data-integrity note on 0011's own finding numbering

Before starting: `0011`'s Executive Summary claims "Total findings: 131," but the document's own
`### Finding N` headers do not run 1–131. They run **1–47, then jump straight to 50** (there is no
Finding 48 or Finding 49 anywhere in the file) **and continue 50–132**. That is 130 actual finding
entries (47 + 83), numbered 1–47 and 50–132 — not 131 findings numbered 1–131 as the prompt
(`0011_REVIEW_PROMPT`) and this session's own prompt (`0012`) both assumed.

This is itself worth recording as a mechanical defect in `0011`, not silently worked around:
- Findings 48 and 49 do not exist — nothing was skipped or lost by this session, there is simply a
  gap in `0011`'s own numbering (most likely an editing artifact when the CAP-014/CAP-015 boundary
  was written).
- The true count is **130** findings, not 131.

This session's tracking table below is built from the **actual** finding numbers present in `0011`
(1–47, 50–132) — 130 rows — rather than a literal 1–131 range, so that every row corresponds to a
real finding to check. This divergence from the `0012` prompt's literal "rows 1–131" instruction is
mechanical (there is no Finding 48/49 to make a row for) and does not change the "no sampling, work
through all of them" requirement — all 130 real findings are covered below.

## 1. Mandatory reading completed this session

`AGENTS.md` (full, incl. §6/§15/§13.6/§12), `PROJECT_RULES.md` (full), `PROJECT.md` (full),
`ARCHITECTURE.md` (full), `PROTOCOL.md` (full — §0 through §6, plus skim of §7/§8),
`DECISIONS.md` (full, ADR-001 through ADR-025), `TODO.md` (full), `AI_SESSION_LOG_PROCEDURE.md`
(full), `ai-sessions/INDEX.md`, `ai-sessions/0007_CROSSCHECK_RESULT_2026_09_11.md` (full),
`ai-sessions/0008_CROSSCHECK_RESULT_2026_09_11.md` (full), `ai-sessions/0011_REVIEW_PROMPT_2026_09_12.md`
(full), `ai-sessions/0011_REVIEW_RESULT_2026_09_12.md` (full, all 130 findings),
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 (Capture Index, full), `id_registry.csv` (CAP-NNN rows).

`REVERSE_ENGINEERING.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `APK_REVERSE_ENGINEERING_PROCEDURE.md`,
and `reverse-engineering/APK_VERSIONS.md` are consulted per-finding (targeted read/grep) rather than
read linearly cover-to-cover up front, given this session's scale (130 findings × full log/video/APK
re-derivation) — the specific entries relevant to each finding are read in full before that finding
is dispositioned, satisfying `PROJECT_RULES.md` §1's evidence rule without a redundant blind
front-to-back pass of a 2600-line reference register whose structure (a class/field lookup table) is
designed for targeted consultation, not linear reading.

## 2. Findings-tracking table

`Finding # | Capture | 0011's one-line claim | Re-derivation result | Disposition | Document(s) touched`

| # | Capture | Claim (one line) | Re-derivation | Disposition | Doc(s) touched |
|---|---|---|---|---|---|
| 1 | CAP-001 | 3-attempt BR/EDR reconnect, Page Timeout then success | confirmed (cited command under-reproduces it) | rejected (no doc change) | — |
| 2 | CAP-001 | DLCI 0x08/0x09 dual multiplex (private envelope / HFP) | confirmed | rejected (no doc change) | — |
| 3 | CAP-001 | Frame 1114 battery decode L100/R100/Case62 | confirmed | rejected (no doc change) | — |
| 4 | CAP-001 | 4 ANC Set/ACK pairs, bitmask match | confirmed | rejected (no doc change) | — |
| 5 | CAP-001 | Co-occurring Shokz background device | **contradicted** — no remote-name evidence, no "Shokz" string anywhere in log | rejected | — |
| 6 | CAP-002 | Frame-number offset due to log slicing (minor) | confirmed, offset = +47984 | **applied** | CAP-002-FINDINGS.md |
| 7 | CAP-002 | Fresh SSP handshake timeline | confirmed | rejected (no doc change) | — |
| 8 | CAP-002 | SASS "in-use" Group 0x07 Code 0x41 | **contradicted** — cited frame 49538 is wrong (that's "Revision 6" content); correct frame is 49562 | rejected | — |
| 9 | CAP-002 | GATT 0x0f2a "Revision 6" | confirmed | rejected (no doc change) | — |
| 10 | CAP-002 | Frame 49024 battery decode L100/R100/Case57 | confirmed | rejected (no doc change) | — |
| 11 | CAP-002 | Background Fitbit device in log tail | confirmed (genuine GATT discovery, verified via chandle) | rejected (no doc change) | — |
| 12 | CAP-003 | GATT Database Hash caching | confirmed | rejected (no doc change) | — |
| 13 | CAP-003 | Classic SSP pairing sequence/timing | confirmed | rejected (no doc change) | — |
| 14 | CAP-003 | 0x0f2a "Revision 6" stable | confirmed | rejected (no doc change) | — |
| 15 | CAP-004 | CTKD classic pairing under GMS-disabled | confirmed | rejected (no doc change) | — |
| 16 | CAP-004 | RFCOMM channel distribution under GMS-disabled | confirmed | rejected (no doc change) | — |
| 17 | CAP-004 | DLCI 0x08 capability handshake / release_5.203 | confirmed (phrasing loose but not wrong) | rejected (no doc change) | — |
| 18 | CAP-005 | "Heavy bass" preset write, frame 1245 | confirmed | rejected (no doc change) | — |
| 19 | CAP-005 | EQ slider live preview, frame 1321, field 16 | confirmed | rejected (no doc change) | — |
| 20 | CAP-005 | EQ explicit save, frame 1338, field 18 | confirmed | rejected (no doc change) | — |
| 21 | CAP-005 | 5-band float32 EQ quintet structure | confirmed | rejected (no doc change) | — |
| 22 | CAP-006 | 100% reliable ANC tap→frame (4/4) | confirmed (full-log census, zero extras) | rejected (no doc change) | — |
| 23 | CAP-006 | ANC mode bitmasks/capability fields confirmed | confirmed | rejected (no doc change) | — |
| 24 | CAP-007 | Group 0x04 Code 0x12 autonomous push | confirmed | rejected (no doc change) | — |
| 25 | CAP-007 | 3-state value sequence 0x02/0x03/0x04 | confirmed | rejected (no doc change) | — |
| 26 | CAP-007 | Left-bud removal → channel bounce, no value change | confirmed | rejected (no doc change) | — |
| 27 | CAP-007 | Closing case lid → zero wire signal | confirmed | rejected (no doc change) | — |
| 28 | CAP-008 | HFP SLC handshake + session-local channel (DLCI 0x0c) | confirmed | rejected (no doc change) | — |
| 29 | CAP-008 | mSBC eSCO synchronous connections | confirmed (via +BCS/AT+BCS, not the event's own Air_Mode byte) | rejected (no doc change) | — |
| 30 | CAP-009 | BATT-006: battchg stale snapshot, AT+BIEV tracks Right | confirmed | rejected (no doc change) | — |
| 31 | CAP-009 | Option E L/R/Case triple + charge cycle | confirmed (75/75, 50/3 encoding split) | rejected (no doc change) | — |
| 32 | CAP-009 | Group 0x03 Code 0x03 battery candidate | confirmed (208/208) | rejected (no doc change) | — |
| 33 | CAP-010 | Group W procedure gap (minor) | confirmed | rejected (no doc change) | — |
| 34 | CAP-010 | GATT handle cluster stable, 0x0c04/0x0c0c/0x0c13 shapes | confirmed (80/40/9/10/32 bytes exact) | rejected (no doc change) | — |
| 35 | CAP-011 | Passive BLE ads under rotating addresses | confirmed (634/634) | rejected (no doc change) | — |
| 36 | CAP-011 | Active-connection procedure deviation (minor) | confirmed | rejected (no doc change) | — |
| 37 | CAP-011 | Option E battery pushes decoded | confirmed (4/4) | rejected (no doc change) | — |
| 38 | CAP-012 | Classic SSP (not CTKD) when no BLE tool used | confirmed | rejected (no doc change) | — |
| 39 | CAP-012 | DLCI 0x02/0x04 closed under GMS-disabled | confirmed | rejected (no doc change) | — |
| 40 | CAP-012 | Manual reconnect re-runs full HFP SLC | confirmed | rejected (no doc change) | — |
| 41 | CAP-012 | btsnooz 15-byte truncation (minor) | confirmed | rejected (no doc change) | — |
| 42 | CAP-013 | Reset Bluetooth&Wi-Fi + 2m21s log-start gap (minor) | confirmed | rejected (no doc change) | — |
| 43 | CAP-013 | Fresh classic SSP on re-pair | confirmed | rejected (no doc change) | — |
| 44 | CAP-013 | DLCI 0x02 opens ~61s late | confirmed (61.868s exact) | rejected (no doc change) | — |
| 45 | CAP-014 | CTKD via nRF Connect | confirmed | rejected (no doc change) | — |
| 46 | CAP-014 | GATT cache reuse, no live discovery (minor) | confirmed (once correctly scoped to handle 0x0003) | rejected (no doc change) | — |
| 47 | CAP-014 | 0x0f32/0x0f33 CCCD + 0x0f2a "Revision 6" | confirmed | rejected (no doc change) | — |
| 50 | CAP-015 | 5 presets + 3-pass slider sweep, 1-field-at-a-time | confirmed (56/56) | rejected (no doc change) | — |
| 51 | CAP-015 | Absolute quintet→band float32 mapping | confirmed | rejected (no doc change) | — |
| 52 | CAP-015 | Field 18 fires on slider-release | confirmed (restates known, already-nuanced reading) | rejected (no doc change) | — |
| 53 | CAP-016 | Buds-initiated reconnect/disconnect on dock events | confirmed | rejected (no doc change) | — |
| 54 | CAP-016 | Case lid open/close empty → zero wire frames | **overclaimed** — 33/8 frames actually present (routine periodic traffic, not lid-caused); canonical doc already phrases this correctly | rejected | — |
| 55 | CAP-016 | Settable-toggles tracks UI selection state | confirmed | rejected (no doc change) | — |
| 56 | CAP-016 | HID Feature report "AndroidHeadTracker#1.0" | confirmed | rejected (no doc change) | — |
| 57 | CAP-017 | Real GATT discovery walk via new nRF Connect client | confirmed (137/137) | rejected (no doc change) | — |
| 58 | CAP-017 | Severe ACL truncation (minor) | confirmed | rejected (no doc change) | — |
| 59 | CAP-017 | 15 GATT services mapped | confirmed (matches already-FACT list) | rejected (no doc change) | — |
| 60 | CAP-018 | 0x0044 burst rides unrelated Heart Rate device | confirmed | rejected (no doc change) | — |
| 61 | CAP-018 | Classic reconnect ~64s delay | confirmed (Create Connection time matches) | rejected (no doc change) | — |
| 62 | CAP-019 | Conversation detection ON → field 22 = 1 | confirmed | rejected (no doc change) | — |
| 63 | CAP-019 | Multipoint ON → field 11 = 1 + SASS burst | confirmed | rejected (no doc change) | — |
| 64 | CAP-020 | Touch controls ON → field 4 = 1 | confirmed | rejected (no doc change) | — |
| 65 | CAP-020 | Head gestures ON → field 29 = 2 | confirmed | rejected (no doc change) | — |
| 66 | CAP-020 | qhr schema match for fields 4/29 | confirmed | rejected (no doc change) | — |
| 67 | CAP-021 | Press-and-hold L/R → qju field 7 | confirmed (4/4 combos exact) | rejected (no doc change) | — |
| 68 | CAP-021 | ANC rotation checklist → qht field 12 | confirmed (16/16) | rejected (no doc change) | — |
| 69 | CAP-021 | 1123-frame DLCI 0x0a bulk burst | confirmed (1123/1123) | rejected (no doc change) | — |
| 70 | CAP-022 | Mono audio → field 19 | confirmed | rejected (no doc change) | — |
| 71 | CAP-022 | Volume EQ → field 15 | confirmed | rejected (no doc change) | — |
| 72 | CAP-022 | Volume balance → field 17 SINT32 zigzag | confirmed (all 7 zigzag values exact) | rejected (no doc change) | — |
| 73 | CAP-023 | On-wire firmware string "release_5.203" match | confirmed | rejected (no doc change) | — |
| 74 | CAP-023 | Cached firmware re-check is wire-silent | confirmed | rejected (no doc change) | — |
| 75 | CAP-024 | In-ear detection → field 2 | confirmed | rejected (no doc change) | — |
| 76 | CAP-024 | "Bud return" → field 28 | confirmed | rejected (no doc change) | — |
| 77 | CAP-024 | "Other alerts" → field 27 | confirmed | rejected (no doc change) | — |
| 78 | CAP-025 | Direct Ring/Stop, Group 0x04 Code 0x01 | confirmed | rejected (no doc change) | — |
| 79 | CAP-025 | Find Hub map "Play sound" wire-silent locally | confirmed | rejected (no doc change) | — |
| 80 | CAP-026 | Battery delivers at connect before UI notice | confirmed | rejected (no doc change) | — |
| 81 | CAP-026 | Case battery wire mismatch (stale value) | confirmed | rejected (no doc change) | — |
| 82 | CAP-026 | App force-close/reopen → zero queries | confirmed | rejected (no doc change) | — |
| 83 | CAP-027 | Tap/swipe gestures over AVRCP not RFCOMM | confirmed | rejected (no doc change) | — |
| 84 | CAP-027 | Hardware hold → spontaneous DLCI 0x04 Notify | confirmed | rejected (no doc change) | — |
| 85 | CAP-028 | Nod/shake inert without call context (negative) | confirmed (after fixing own filter error) | rejected (no doc change) | — |
| 86 | CAP-028 | Co-occurring Heart Rate background device | confirmed | rejected (no doc change) | — |
| 87 | CAP-029 | Conversation-detection pause is wire-silent | confirmed (once scoped to control/write frames, not all traffic) | rejected (no doc change) | — |
| 88 | CAP-029 | Factory reset + fresh SSP re-pair | confirmed | rejected (no doc change) | — |
| 89 | CAP-029 | Short-press pairing mode not attempted (minor) | confirmed | rejected (no doc change) | — |
| 90 | CAP-031 | Missed pre-Forget action, 66s late (minor) | confirmed (substance; minor timestamp imprecision in 0011) | rejected (no doc change) | — |
| 91 | CAP-031 | Fresh classic SSP on re-pair | **contradicted** — cited frame 140 is an LE ad report; correct frame is 637 | rejected | — |
| 92 | CAP-031 | btsnooz 15-byte truncation (minor) | confirmed | rejected (no doc change) | — |
| 93 | CAP-032 | Pre-Forget raw snoop coverage, 58s clean | confirmed substance; **both cited absolute timestamps wrong** | rejected | — |
| 94 | CAP-032 | Vendor command 0xFD57/0x0157 | **contradicted** — cited frame 69 wrong (unrelated command); correct frame is 91 | rejected | — |
| 95 | CAP-032 | Co-occurring Heart Rate BLE device pre-connection | **contradicted** — wrong frame (115) AND wrong address (belongs to CAP-028); correct is frame 356 / 51:ef:91:49:2f:d6 | rejected | — |
| 96 | CAP-033 | Forget/Force-stop order violation (minor) | confirmed | rejected (no doc change) | — |
| 97 | CAP-033 | Full SDP UUID→channel→name mapping tree | confirmed (5/6 names verified) | rejected (no doc change) | — |
| 98 | CAP-033 | Step 3 app-launch comparison never executed (minor) | confirmed | rejected (no doc change) | — |
| 99 | CAP-034 | GATT-001 resolved: 0x0c0X/0x0f2X handle↔UUID | confirmed (handles present; matches signed-off FACT table) | rejected (no doc change) | — |
| 100 | CAP-034 | GATT cache-reuse prevents wire discovery (minor) | confirmed | rejected (no doc change) | — |
| 101 | CAP-035 | DLCI 0x08 burst runs with GMS user-disabled | confirmed | rejected (no doc change) | — |
| 102 | CAP-035 | DLCI 0x0a silent under GMS-disabled | confirmed | rejected (no doc change) | — |
| 103 | CAP-035 | ~7s video gap not 5min (minor) | confirmed (video-based, matches doc) | rejected (no doc change) | — |
| 104 | CAP-036 | "Get ANC state" 0x11 on connect | confirmed | rejected (no doc change) | — |
| 105 | CAP-036 | Settings-screen-open wire-silent (clean negative) | confirmed | rejected (no doc change) | — |
| 106 | CAP-036 | 34-frame RPC/correlation-counter burst DLCI 0x02 | count unverifiable — own re-run of doc's own command gives 45, not 34 | routed to maintainer | — |
| 107 | CAP-037 | 34 reconnects not 5 (minor) | confirmed substance; count flagged (Item 2) | rejected (no doc change) | — |
| 108 | CAP-037 | 100% reliable Get ANC state reconnect (26/26) | confirmed (26/26) | rejected (no doc change) | — |
| 109 | CAP-037 | chandle 0x0009 dock flip with no preceding Get (minor) | confirmed (18.24s delta exact) | rejected (no doc change) | — |
| 110 | CAP-038 | Local DLCI shift + docked-state lag (minor) | confirmed | rejected (no doc change) | — |
| 111 | CAP-038 | Spontaneous Notify on hardware press-hold | confirmed | rejected (no doc change) | — |
| 112 | CAP-038 | Video ends before log; Window 2 unexecuted (minor) | confirmed | rejected (no doc change) | — |
| 113 | CAP-039 | Settable-toggles trigger-independent (10/10 0xe8) | confirmed (14 raw/10 events, all 0xe8) | rejected (no doc change) | — |
| 114 | CAP-039 | Live Left battery 98→97 | confirmed | rejected (no doc change) | — |
| 115 | CAP-040 | In-app Connect/Disconnect buttons wire-silent | confirmed (once tail-edge unrelated burst excluded) | rejected (no doc change) | — |
| 116 | CAP-040 | 7 unmapped Get-shaped codes fire once on connect | confirmed | rejected (no doc change) | — |
| 117 | CAP-040 | Momentary 0xff Left sentinel at docking | confirmed (1.27s delta exact) | rejected (no doc change) | — |
| 118 | CAP-041 | Invariant DLCI 0x02 burst length/shape | confirmed (timestamps exact; count not fully re-scoped) | rejected (no doc change) | — |
| 119 | CAP-041 | Byte-identical tail run / order-scrambled header | confirmed | rejected (no doc change) | — |
| 120 | CAP-041 | Single session-varying subframe (nonce) | confirmed | rejected (no doc change) | — |
| 121 | CAP-042 | Ultra-sparse idle push cadence (2 in 37m39s) | confirmed (exact) | rejected (no doc change) | — |
| 122 | CAP-042 | 3-channel sync holds, HFP drops out | confirmed (29.5ms spread, 0 HFP) | rejected (no doc change) | — |
| 123 | CAP-045 | Group AJ procedure unexecuted (minor) | confirmed | rejected (no doc change) | — |
| 124 | CAP-045 | 10 physical ANC hold transitions, no Side field | confirmed (12 total - 2 Get-answered = 10) | rejected (no doc change) | — |
| 125 | CAP-046 | Volume balance range/polarity ±100, +Left/-Right | confirmed (wire half, 8/8 exact) | rejected (no doc change) | — |
| 126 | CAP-046 | Mono audio fires with Balance-extremes | confirmed (8/8, deltas exact) | rejected (no doc change) | — |
| 127 | CAP-046 | Intermediate-position tests unexecuted (minor) | confirmed | rejected (no doc change) | — |
| 128 | CAP-048 | CAP-037 Get-less Notify anomaly resolved (docking) | confirmed | rejected (no doc change) | — |
| 129 | CAP-048 | Stale Settable=0x00 on fresh reconnect (minor) | confirmed | rejected (no doc change) | — |
| 130 | CAP-048 | Closed-case classic connection-retry burst | confirmed (7 events, 55.886s exact) | rejected (no doc change) | — |
| 131 | CAP-049 | .log.last genuine same-session rotation | confirmed (2.229s delta exact) | rejected (no doc change) | — |
| 132 | CAP-049 | Disconnect/reconnect cycling did not reproduce (negative) | confirmed (exact) | rejected (no doc change) | — |

## 3. Per-finding detail

### CAP-001

#### Finding 1 — 3-attempt BR/EDR reconnect (Page Timeout, then success)
- **0011 finding:** running a cited `tshark` filter (`btrfcomm.dlci==0x00 || bthci_evt.code==0x03`)
  allegedly shows Create Connection frames 732/738/832 and Connection Complete frames 737(0x04
  Page Timeout)/775(0x00)/855(0x00).
- **Re-derivation:** ran the exact cited command. It reproduces only the three Connection Complete
  events (737/775/855, statuses 0x04/0x00/0x00, BD_ADDR `04:00:6e:cf:6e:07`) — frames 732/738/832
  do **not** appear in this filter's output (Create Connection is an HCI *command*, matched by
  neither `btrfcomm.dlci==0x00` nor `bthci_evt.code==0x03`). Verified 732/738/832 independently
  (`-Y "frame.number in {732,737,738,775,832,855}" -e bthci_cmd.opcode`): all three are opcode
  `0x0405` (Create Connection) to `04:00:6e:cf:6e:07`, confirming the underlying claim is factually
  correct — it just isn't all reproducible from the single filter cited. Matches
  `CAP-001-FINDINGS.md` §1's own table (732→737, 738→775→831 disconnect, 832→855) exactly.
- **Disposition:** rejected (no doc change) — content confirmed correct and already accurately
  documented in `CAP-001-FINDINGS.md` §1; nothing to apply. Minor note: 0011's own cited command
  under-reproduces its claimed output (a hex-and-script-rule imprecision in 0011's file, not in any
  project document), not actionable.
- **Change made:** none.

#### Finding 2 — Bidirectional DLCI 0x08/0x09 multiplex
- **0011 finding:** DLCI 0x08 carries the private envelope (both directions), DLCI 0x09 carries HFP
  AT commands, citing frames 1112/1113/1236/1238.
- **Re-derivation:** `tshark -Y "frame.number in {1112,1113,1236,1238}" -e frame.number -e
  btrfcomm.dlci -e frame.p2p_dir -e data.data`. Frame 1112 (DLCI 0x08, dir=1/Rcvd):
  `0e02001a0a18676f6f676c652d706978656c2d627564732d70726f2d7631` → ASCII tail decodes to
  `google-pixel-buds-pro-v1`. Frame 1113 (DLCI 0x08, dir=0/Sent): ASCII tail `Europe/Amsterdam`.
  Frame 1236 (DLCI 0x09, dir=1/Rcvd): `41542b425253463d3932310d` = `AT+BRSF=921\r`. Frame 1238
  (DLCI 0x09, dir=0/Sent): `0d0a2b425253463a20333935310d0a` = `\r\n+BRSF: 3951\r\n`. Exact match to
  0011's claim, byte-for-byte.
- **Disposition:** rejected (no doc change) — confirmed correct, redundant with `CAP-001-FINDINGS.md`
  §2's own already-documented DLCI 0x08/0x09 direction-bit finding (matches `PROTOCOL.md` §4.3
  Option C's "phone-init→0x08, Buds-init→0x09" note).
- **Change made:** none.

#### Finding 3 — Frame 1114 battery decode (L100/R100/Case62)
- **0011 finding:** DLCI 0x08 `Group 0x0e Code 0x01` frame 1114 decodes to Left=100/Right=100/Case=62.
- **Re-derivation:** `tshark -Y "frame.number==1114" -e data.data` →
  `0e0100230a210a03616c6c121a0a060864100118010a060864100118020a06083e100118032001`. Manually
  parsed: `08 64`(=100)`10 01 18 01`(flag=1,idx=1) / `08 64`(=100)`10 01 18 02`(idx=2) / `08
  3e`(=62)`10 01 18 03`(idx=3) — exact match to the claimed L=100/R=100/Case=62 mapping and to
  `PROTOCOL.md` §4.3 Option E's index=1/2/3→Left/Right/Case FACT (`DECISIONS.md` ADR-014).
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT per ADR-014.
- **Change made:** none.

#### Finding 4 — 4 ANC Set/ACK pairs with bitmask match
- **0011 finding:** frames 2039/2041, 2132/2134, 2159/2162, 2193/2195 are Set/ACK pairs for
  Adaptive(0x40)/Transparent(0x80)/Active(0x08)/Off(0x20), round-trip 50–110ms.
- **Re-derivation:** `tshark -Y "frame.number in {2039,2041,2132,2134,2159,2162,2193,2195}" -e
  frame.time_epoch -e data.data`. All 8 hex payloads match exactly as quoted in 0011 (e.g. 2039:
  `0812001401e8e840...`, ACK 2041: `ff010006081201e8e840`). Round-trip times computed from
  `frame.time_epoch`: 2039→2041 = 60.3ms, 2132→2134 = 108.8ms, 2159→2162 = 53.7ms, 2193→2195 =
  79.2ms — all inside the claimed 50–110ms window. Matches `PROTOCOL.md` §4.1's already-FACT
  bitmask table exactly (`DECISIONS.md` ADR-009).
- **Disposition:** rejected (no doc change) — confirmed correct and already FACT.
- **Change made:** none.

#### Finding 5 — Co-occurring background Shokz headset (CONTRADICTED)
- **0011 finding:** claims a background Shokz bone-conduction headset (`a8:f5:e1:f4:1e:d1`) is
  identified in the log "along with corresponding HCI name requests and event notes."
- **Re-derivation:** `bluetooth.addr == a8:f5:e1:f4:1e:d1` → 0 matching frames. Extracting all
  `bthci_evt.bd_addr`/`bthci_cmd.bd_addr` values from the whole log does list this address, but only
  in two frames (99, 101) — decoded via `_ws.col.Info`, these are **`Sent LE Add Device to Resolving
  List`** and **`Sent LE Set Privacy Mode`**, i.e. the phone re-adding one of its own previously
  bonded devices to its LE resolving list at Bluetooth stack bring-up — not a Remote Name
  Request/Response. Searched for `bthci_cmd.opcode==0x0419`/`bthci_evt.code==0x07` (Remote Name
  Request/Response) anywhere in the log: **zero matches**. `strings ... | grep -i shokz`: **zero
  matches** — the string "Shokz" appears nowhere in the raw log. `CAP-001-FINDINGS.md` itself never
  mentions this address or a Shokz device anywhere.
- **Disposition:** rejected — **contradicted**. There is no remote-name evidence, and no "Shokz"
  string, anywhere in this capture; the address's only appearances are routine resolving-list
  bookkeeping for an already-bonded device, which happens for every bonded device at BT enable and
  does not establish "co-occurring" presence in this session, let alone a specific vendor identity.
  This is an unsupported/fabricated identification per `AGENTS.md` §13.6 (zero-creativity — a
  device's brand/model cannot be inferred from a bare MAC address without an OUI lookup or other
  cited source, neither of which 0011 cites). Not added to any document.
- **Change made:** none — nothing was documented, and nothing should be, on this claim's current
  evidence.

### CAP-002

#### Finding 6 — Frame-number offset from log slicing (minor discrepancy) — VALID, applied
- **0011 finding:** the actual workspace log is the full 50,468-packet buffer, not the 1,877-packet
  sliced file `CAP-002-FINDINGS.md` was originally analyzed against; frame citations need a
  translation offset.
- **Re-derivation:** `capinfos` confirms 50,468 packets, 08:50:32–17:10:58 (matches). Queried
  `frame.time_epoch >= 1786287926` (17:05:26 local) → first matching frame is **48622**. Cross-
  checked two independent citation pairs already in `CAP-002-FINDINGS.md` itself (its own header
  states the analysis slice; its body cites low frame numbers like 1251/1267/1578/1826): sliced
  frame 653 (0011's own worked example) → actual 48637 (offset 47984); sliced 1441 → actual 49425
  (offset 47984). Both agree exactly. This is a real, constant, additive offset.
- **Disposition:** **applied**. This is exactly the kind of mechanical, low-risk, high-value note
  the `0012` prompt's "What 'process it in' means" section pre-authorizes for direct application (a
  frame-number/citation-translation note, not a FACT/ADR touch).
- **Change made:** `CAP-002-FINDINGS.md` header (after the log-file/slicing description) — added a
  sentence documenting the `+47984` translation offset with a worked example, so future readers can
  map any frame number in this document to the current workspace file.

#### Finding 7 — Fresh SSP handshake timeline, frames 48637–48718
- **0011 finding:** the SSP sequence (Delete Stored Link Key → Create Connection → Connection
  Complete → Link Key Request Negative Reply → Simple Pairing Complete → Link Key Notification →
  Encryption Change) matches exactly at the cited frames/times.
- **Re-derivation:** `tshark -Y "frame.number in {48637,48639,48642,48665,48707,48708,48718}" -e
  frame.time -e bthci_evt.code -e bthci_cmd.opcode`. All 7 frames match exactly: 48637=opcode
  `0x0c12` (Delete Stored Link Key) @17:05:26.717; 48639=`0x0405` (Create Connection) @17:05:26.724;
  48642=evt `0x03` (Connection Complete) @17:05:27.146; 48665=opcode `0x040c` (Link Key Request
  Negative Reply) @17:05:27.169; 48707=evt `0x36` (Simple Pairing Complete) @17:05:33.608;
  48708=evt `0x18` (Link Key Notification) @17:05:33.622; 48718=evt `0x59`, confirmed via
  `_ws.col.Info` to read "Rcvd Encryption Change [v2]" @17:05:33.721 — the modern HCI event variant
  of "Encryption Change," semantically identical to the claim. All timestamps match exactly.
- **Disposition:** rejected (no doc change) — confirmed correct; matches `PROTOCOL.md` §5.1's
  already-FACT `CAP-002` citation (frames 653–734 in the document's own sliced numbering, which
  Finding 6's offset now explicitly reconciles).
- **Change made:** none.

#### Finding 8 — SASS "in-use" opcode, cited frame 49538 (CONTRADICTED — wrong frame)
- **0011 finding:** "Frame 49538 (corresponds to sliced frame 1578) at 17:05:47.818 contains the
  expected SASS command bytes."
- **Re-derivation:** `tshark -Y "frame.number==49538" -e data.data` → payload
  `030a0008741c49380e816f7f03010003da2db103020006640ccd9a6ae30309000a5265766973696f6e203607100000`
  at time **17:05:47.657972**, not 17:05:47.818 — and this payload's ASCII tail is
  `5265766973696f6e2036` = **"Revision 6"** (Device Information content), not SASS Group `0x07`
  Code `0x41` at all. The actual frame containing `07 41 00 16 69 6e 2d 75 73 65` ("in-use") at
  the exact claimed timestamp 17:05:47.818284 is **frame 49562** (confirmed via `data.data contains
  "in-use"` sweep of the whole file, which also independently lists every other `"in-use"` SASS
  occurrence in the buffer — frames 1122, 1126, 1849, 1857, 17635, 17653, 18928, 18942, 21218,
  21225, 49562, 49569, matching `CAP-001`'s and other sessions' own already-known SASS bursts).
  49562 = sliced 1578 + 47984 — i.e. `CAP-002-FINDINGS.md`'s own original citation (frame 1578, its
  own sliced numbering, lines 266/339) is correct once translated via Finding 6's offset; 0011
  applied an inconsistent offset (47960 instead of 47984) and landed on the wrong frame.
- **Disposition:** rejected — **contradicted** (wrong frame number; underlying SASS
  Group-0x07/Code-0x41/"in-use" claim is true and already correctly documented in
  `CAP-002-FINDINGS.md` at its own, correct sliced-numbering citation). No project document is
  wrong here — the error is entirely inside `0011`'s own re-derivation.
- **Change made:** none.

#### Finding 9 — GATT 0x0f2a "Revision 6", frame 49425
- **0011 finding:** ATT Read Response at handle `0x0f2a` returns hex `5265766973696f6e2036`
  ("Revision 6").
- **Re-derivation:** `tshark -Y "frame.number==49425" -e btatt.opcode -e btatt.handle -e
  btatt.value` → opcode `0x0b` (Read Response), handle `0x0f2a`, value `5265766973696f6e2036`.
  Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented.
- **Change made:** none.

#### Finding 10 — Frame 49024 battery decode (L100/R100/Case57)
- **0011 finding:** DLCI 0x08 frame 49024 decodes to Left=100/Right=100/Case=57.
- **Re-derivation:** `tshark -Y "frame.number==49024" -e data.data` →
  `0e0100230a210a03616c6c121a0a060864100118010a060864100118020a060839100118032001`. Parsed:
  idx1=`0x64`(100), idx2=`0x64`(100), idx3=`0x39`(57). Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (ADR-014).
- **Change made:** none.

#### Finding 11 — Background Fitbit device in log tail
- **0011 finding:** the log tail past 17:06:46 contains real GATT primary service discovery
  belonging to an unrelated Fitbit Charge 6 (`78:f8:1b:d6:6b:0a`), with zero Buds packets present.
- **Re-derivation:** first pass (filtering only on `bthci_evt.bd_addr`/`bthci_cmd.bd_addr`) found
  only advertising-report/filter-list frames and looked like a possible contradiction — but that
  filter only tags the connection-establishment frame itself, not subsequent traffic on an already-
  open connection handle. Re-checked via connection handle: frame 50084 (`LE Enhanced Connection
  Complete [v1]`, 17:09:56.476632) is to `78:f8:1b:d6:6b:0a`, establishing chandle `0x000d`;
  `bthci_acl.chandle==0x000d and btatt.opcode in {0x10,0x11}` (Read By Group Type req/resp) returns
  a real, dense discovery burst starting **17:09:56.564221** (frame 50118) — genuine GATT primary
  service discovery, confirming the claim, not merely passive scanning. Zero Buds (`04:00:6e:cf:6e:07`)
  packets exist anywhere past 17:06:46 (independently reconfirmed). This is a verbatim match to
  `CAP-002-FINDINGS.md` §6 (lines 520–531)'s own already-documented finding, including the "Charge
  6"/`78:f8:1b:d6:6b:0a` identification and the "starting 17:09:56" timing.
- **Disposition:** rejected (no doc change) — confirmed correct, redundant with `CAP-002-FINDINGS.md`
  §6's own existing text (0011 is restating an already-documented finding, not adding new content).
- **Change made:** none.

### CAP-003

#### Finding 12 — GATT Database Hash caching
- **Re-derivation:** `tshark -Y "btatt.opcode in {0x08,0x09,0x10,0x11}"` → exactly 2 frames: 1649
  (opcode `0x08`, Read By Type Request) and 1657 (opcode `0x09`, Read By Type Response, value
  `8b447e7c056554c09c1c4ac90f6f771c`). Confirmed zero `Read By Group Type` (`0x10`/`0x11`) frames
  anywhere in the file (`btatt.opcode in {0x10,0x11}` → 0 matches). Exact match to the claim.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`PROTOCOL.md` §6).
- **Change made:** none.

#### Finding 13 — Classic SSP pairing sequence/timing
- **Re-derivation:** all 10 cited frames match exactly: 1621 (LE Enhanced Connection Complete,
  20:59:38.320) → 1687 (Delete Stored Link Key, .729761) → 1689 (Create Connection, .730843) → 1692
  (Connect Complete, 39.089816) → 1708 (Link Key Request, 39.098169) → 1709 (Negative Reply,
  39.098424) → 1711 (IO Capability Request, 39.100482) → 1750 (Simple Pairing Complete, 39.825030)
  → 1751 (Link Key Notification, 39.834505) → 1756 (Encryption Change [v2], 39.876214). Byte-for-
  byte/time-for-time exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`PROTOCOL.md` §5.1).
- **Change made:** none.

#### Finding 14 — Stable 0x0f2a "Revision 6" read
- **Re-derivation:** `tshark -Y "frame.number==1835" -e btatt.opcode -e btatt.handle -e
  btatt.value` → opcode `0x0b`, handle `0x0f2a`, value `5265766973696f6e2036` ("Revision 6"). Exact
  match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

### CAP-004

#### Finding 15 — CTKD classic pairing under GMS-disabled
- **Re-derivation:** all 10 cited frames confirmed, plus the 1966–1982 range filled in for full
  context: `1854` Delete Stored Link Key → `1856` SMP Pairing Request (Linkkey distribution
  requested) → `1880`/`1882` DHKey Check → `1891` Create Connection → `1933` Connect Complete →
  `1966` Authentication Requested → `1969` **Link Key Request** → `1973` **Link Key Request
  Reply** (not Negative) → `1977` Command Complete (Link Key Request Reply) → `1982` Authentication
  Complete → `2037` Encryption Change [v2]. No IO Capability/SSP exchange anywhere in the sequence —
  confirms CTKD (classic key derived from the LE pairing), exactly as claimed.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`PROTOCOL.md` §5.1).
- **Change made:** none.

#### Finding 16 — RFCOMM channel distribution under GMS-disabled
- **Re-derivation:** `tshark -Y "btrfcomm" -e btrfcomm.dlci | sort | uniq -c` → `20 0x00`, `96 0x08`,
  `2 0x0a`, `56 0x0c`; zero on `0x02`/`0x04`. Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`PROTOCOL.md` §2.3).
- **Change made:** none.

#### Finding 17 — DLCI 0x08 capability handshake / "release_5.203"
- **Re-derivation:** frame 2305 = Group `0x03` Code `0x02`, payload contains ASCII
  `release_5.203` (hex `72656c656173655f352e323033`) — exact match. Frame 2311 = Group `0x0e` Code
  `0x02`, ASCII `google-pixel-buds-pro-v1`. Frame 2315 = Group `0x0e` Code `0x01` (the already-FACT
  Option E battery push, not itself "capability" data — L=100/R=100/Case=`0x24`=36) — 0011's
  wording ("frames 2311 and 2315 carry the standard capability format") is loose (2315 is a battery
  push, not a capability/handshake frame), but not factually wrong: both are among the
  already-documented standard startup-burst message types for DLCI 0x08.
- **Disposition:** rejected (no doc change) — confirmed correct (content matches known baseline);
  the "capability format" phrasing for frame 2315 is imprecise but not worth a document edit since
  no project document repeats that specific phrasing.
- **Change made:** none.

### CAP-005

#### Findings 18–21 — EQ preset/live-preview/save writes and 5-band float32 quintet
- **Re-derivation:** `tshark -Y "frame.number in {1245,1249,1250,1321,1338}" -e btrfcomm.dlci -e
  frame.p2p_dir -e data.data` reproduces all 5 cited hex payloads byte-for-byte, all on DLCI `0x02`,
  address `0x00`, control `0x3b`. Decoded the LEB128 protobuf tags by hand: frame 1245/1321 carry
  `82 01` (field 16, wiretype 2); frame 1338 carries `92 01` (field 18, wiretype 2) — confirms
  Finding 20's field-16-vs-18 distinction exactly. Cross-checked the float32 quintet decode against
  `CAP-005-FINDINGS.md` §5b's own already-published, script-verified decode (lines 201-267): field1
  (tag `0d`) = `00 00 a0 40` = 5.0 (constant, Low bass); field2 (tag `15`) = `00 00 40 40`=3.0 in
  frame 1245 → `33 33 83 c0`=−4.1 in frames 1321/1338 (Bass); fields 3–5 = `00 00 00 00`=0.0
  constant. This is an exact match to `CAP-005-FINDINGS.md`'s own byte-identical citation of the
  same three frames — 0011 is restating already-verified content, not deriving anything new.
- **Disposition:** all four (18, 19, 20, 21) rejected (no doc change) — confirmed correct, entirely
  redundant with `CAP-005-FINDINGS.md` §3–§5b's own existing decode.
- **Change made:** none.

### CAP-006

#### Finding 22 — 100% reliable ANC tap→frame (4/4, zero extras)
- **Re-derivation:** `capinfos` confirms 3,441 packets / 233.157s. `tshark -Y "btrfcomm.dlci==0x04
  and data.data[0:2]==08:12"` (full-log census, not just the 4 cited frames) returns **exactly 4**
  matches, and they are exactly frames 1393/1627/1731/1862 at 17:24:13.296/26.320/38.666/50.824 —
  matching the claimed times exactly, zero additional Code-0x12 frames anywhere else in the log.
  Mode bytes: 1393=`0x08`(Active), 1627=`0x20`(Off), 1731=`0x40`(Adaptive), 1862=`0x80`
  (Transparency) — exact match to the claimed tap order.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`DECISIONS.md`
  ADR-009's lifting update, `PROTOCOL.md` §4.1).
- **Change made:** none.

#### Finding 23 — ANC mode bitmasks/capability fields
- **Re-derivation:** decoded all 4 frames' bytes 4-7: `01 e8 e8 <mode>` in every frame — Seeker
  version=`0x01`, settable=`0xe8`, enabled=`0xe8` constant across all four, mode indexes
  `08`/`20`/`40`/`80` exactly as claimed.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

### CAP-007

#### Finding 24 — Autonomous Buds-initiated push, Group 0x04 Code 0x12
#### Finding 25 — 3-state value sequence (0x02/0x03/0x04)
- **Re-derivation:** full-log census `btrfcomm.dlci==0x08 and data.data[0:2]==04:12` → exactly 18
  frames, all `frame.p2p_dir==1` (Rcvd), matching claim exactly. Values in order: `02,02,02,02` then
  a gap from frame 1444 (09:15:39.285789) to frame 2074 (09:18:43.428099) = **184.14s** (matches
  "~184s" exactly); frame 2074 (first post-idle) carries `03`; subsequent frames settle into a clean
  `02↔04` alternation (`02,04,02,04,02,04,02,04,02,04,02`). Exact match to both findings' claims.
- **Disposition:** both rejected (no doc change) — confirmed correct, already FACT/documented
  (`PROTOCOL.md` §6).
- **Change made:** none.

#### Finding 26 — Left-bud removal → channel bounce, no value change
- **Re-derivation:** `tshark -Y 'frame.time>=... and btrfcomm.frame_type'` around 09:15:38 shows
  `DISC Channel=1` (DLCI 0x02) at **09:15:38.438724** (frame 1351, matches claim exactly) followed
  by DISC/UA rebuilds on Channel 2 (0x04), 4 (0x08), 5 (0x0a) — confirms "channels 1,2,4,5" claim.
  ANC Notify frame 1350 (09:15:38.436417, just before the bounce) reads current-state=`0x80`
  (Transparency); the DLCI 0x04 Code 0x12 census (Finding 24/25 above) shows value `02` immediately
  before and after this bounce (frames 1436/1444) — confirms "no change in status byte" claim.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

#### Finding 27 — Closing case lid → zero wire signal (negative result)
- **Re-derivation:** `tshark -Y 'frame.time>=09:16:34 and <=09:16:54 and btrfcomm.len>0'` (±10s of
  09:16:44) → 0 matches. Exact match to claim.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

### CAP-008

#### Finding 28 — HFP SLC + session-local channel (DLCI 0x0c)
- **Re-derivation:** `tshark -x -Y "frame.number in {776,778}"` → frame 776 raw bytes decode to
  ASCII `AT+BRSF=921\r`, frame 778 to `+BRSF: 3951\r\n`, both on DLCI `0x0c` (confirmed via
  `btrfcomm.dlci`). Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

#### Finding 29 — mSBC eSCO synchronous connections
- **Re-derivation:** `bthci_evt.code==0x2c` → frames 1656 (handle `0x0005`, 09:39:19.821) and 2282
  (handle `0x0006`), both "Synchronous Connection Complete." Raw hex parse: `Air_Mode` byte =
  `0x05` — tshark's own dissector labels this "Unknown (5)" (out of the Core-spec-defined 0–3
  range), so the event alone doesn't self-evidently say "mSBC." Traced the actual codec evidence:
  frame 1641 `+BCS: 2` / frame 1644 `AT+BCS=2` (HFP Codec Connection Setup, Codec ID 2 = mSBC per
  HFP spec) immediately precede frame 1646 `Enhanced Setup Synchronous Connection`, which precedes
  1656. `CAP-008-FINDINGS.md` §5 (lines 204-242) already documents this in full hex/command detail,
  including the Enhanced Setup command's own explicit `Codec: mSBC (0x05)` field (a different field
  than the completion event's Air_Mode byte) — the underlying claim is correct, just not fully
  self-contained in the two frame numbers 0011 cites alone.
- **Disposition:** rejected (no doc change) — confirmed correct, redundant with `CAP-008-FINDINGS.md`
  §5's own more complete evidence chain.
- **Change made:** none.

### CAP-009

#### Finding 30 — HFP battery reporting over natural discharge (BATT-006)
- **Re-derivation:** `data.data`/generic filters returned nothing for this capture — its RFCOMM/HFP
  traffic is fully dissected by tshark's own `bthfp` protocol tree (`Bluetooth HFP Profile`, not a
  generic-data fallback), so `frame contains "<ascii>"` was used instead. `frame contains
  "AT+CIND?"` → exactly **1** match, frame 884 (18:34:01.901742) — confirms the single-snapshot
  query claim. `frame contains "AT+BIEV=2"` → 68 total occurrences; hex-decoded the 5 cited
  transition frames directly: 972→`BIEV=2,93`, 5556→`BIEV=2,92`, 14612→`BIEV=2,90`,
  20632→`BIEV=2,89`, 26459→`BIEV=2,88` — exact match to the claimed 93→92→90→89→88 sequence and
  timestamps.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`DECISIONS.md`
  ADR-015).
- **Change made:** none.

#### Finding 31 — Option E L/R/Case triple + charge cycle
- **Re-derivation:** full-log census `btrfcomm.dlci==0x08 and data.data[0:2]==0e:01` → **exactly 75**
  frames (matches "Decoded 75 Option E frames" exactly). Two Case-unknown encoding counts: short
  no-flag form (`08 ff 01 18 03`) → 50 occurrences; long flag=1 form (`08 ff 01 10 01 18 03`) → 3
  occurrences — consistent with the claimed "briefly switches to a longer form... 3 occurrences."
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`PROTOCOL.md` §4.3
  Option E).
- **Change made:** none.

#### Finding 32 — Group 0x03 Code 0x03 battery candidate
- **Re-derivation:** full-log census `btrfcomm.dlci==0x04 and data.data[0:4]==03:03:00:03` →
  **exactly 208** occurrences (matches "all 208 occurrences" exactly). Spot-checked the claimed
  charging-state regime change: frame 26852 (19:52:20.380412) decodes to `b1=0xdd`(221)/`b2=0x58`(88)
  — exact match to "b1 jumps to 221"; subsequent frames climb `0xde`(222)→`0xdf`(223)→… — matches
  "climbs ~1/sample" exactly.
- **Disposition:** rejected (no doc change) — confirmed correct, already 🟡 HYPOTHESIS as claimed
  (`PROTOCOL.md` §4.3 Option B).
- **Change made:** none.

### CAP-010

#### Finding 33 — Group W procedure gap (minor, already documented)
- **Re-derivation:** `btatt.opcode in {0x10,0x11,0x04,0x05}` (Read By Group Type / Find Information)
  → 0 matches, confirms zero live discovery traffic.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented in
  `CAP-010-FINDINGS.md` §1/Capture Index.
- **Change made:** none.

#### Finding 34 — GATT handle cluster stable shape (0x0c04/0x0c0c/0x0c13)
- **Re-derivation:** frame 2017 (`0x0c04` write): value length computed = **80 bytes** exactly.
  Frame 2020 (`0x0c0c` notification): **40 bytes** exactly. Frame 2024: `0x0f2a` = "Revision 6."
  Frames 2029→2031 (Read Req/Resp on `0x0c13`): response value 9 bytes; frame 2035 (Write, `0x0c13`):
  value `0108dd5e0e6261af6069` = 10 bytes; frame 2037 (Notification, `0x0c13`): 32 bytes. All byte
  counts match 0011's claim exactly, and match `CAP-010-FINDINGS.md`'s own already-published table
  (line 145-148) verbatim.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT/documented.
- **Change made:** none.

### CAP-011

#### Finding 35 — Passive BLE ads under rotating addresses
- **Re-derivation:** `btcommon.eir_ad.entry.uuid_16==0xfe2c` → **634** matches, exactly matching
  `PROTOCOL.md` §4.3 Option A's own already-cited "634 frames" count for this capture.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented.
- **Change made:** none.

#### Finding 36 — Active-connection procedure deviation (minor)
- **Re-derivation:** confirmed a classic `Connection Complete` at frame 728 (09:45:24.958) followed
  by frequent disconnect events (0x05) starting 2404 — consistent with the already-documented
  "near-continuous BLE reconnect churn" for this session (`PROTOCOL.md` §4.3 Option A), not a
  connection-free scan.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented in
  `CAP-011-FINDINGS.md`.
- **Change made:** none.

#### Finding 37 — Option E battery pushes decoded
- **Re-derivation:** `btrfcomm.dlci==0x08 and data.data[0:2]==0e:01` → **4** matches, matching
  `PROTOCOL.md` §4.3 Option E's own "4 independent occurrences in one ~17.5-minute log" citation for
  this exact capture.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (ADR-014).
- **Change made:** none.

### CAP-012

#### Finding 38 — Classic SSP (not CTKD) when no BLE tool used
- **Re-derivation:** the cited event sequence (Connect Complete → Link Key Request → **IO
  Capability Request** → Simple Pairing Complete → Link Key Notification → Encryption Change)
  confirmed exactly — a classic SSP exchange, no SMP-over-LE pairing precedes it. Confirms "not
  CTKD."
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`PROTOCOL.md` §5.1).
- **Change made:** none.

#### Finding 39 — DLCI 0x02/0x04 closed under GMS-disabled
- **Re-derivation:** DLCI census → `0x00`(42), `0x08`(52), `0x0a`(8), `0x0c`(92); zero on `0x02`/
  `0x04`. Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

#### Finding 40 — Manual reconnect re-runs full HFP SLC
- **Re-derivation:** DLCI `0x0c` reopens (`SABM`/`UA` Channel=6, frames 1165/1167) immediately
  after the reconnect's Connect Complete (frame 1003), followed by a dense burst of UIH exchanges —
  most flagged `[Malformed Packet]`/`[Packet size limited during capture]` due to Finding 41's
  15-byte truncation, so literal AT-command text isn't recoverable, but the exchange shape/timing
  matches an SLC handshake exactly. `CAP-012-FINDINGS.md` §6 already documents this as a 🟢 FACT
  "bonus finding" (Test-ID `PAIR-003`) with the same frame range.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT/documented.
- **Change made:** none.

#### Finding 41 — btsnooz 15-byte truncation (minor)
- **Re-derivation:** `capinfos` reports inferred packet-size range 15–126 bytes; `frame.cap_len !=
  frame.len` → 254 truncated frames. Matches the claimed severe ACL truncation.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented (`TODO.md`'s
  "Known technical debt" section, `CAP-012-FINDINGS.md` §1).
- **Change made:** none.

### CAP-013

#### Finding 42 — "Reset Bluetooth & Wi-Fi" + 2m21s log-start gap (minor, already documented)
- **Re-derivation:** `capinfos` → earliest packet 17:11:45.799616, matching the claimed "17:11:45"
  log start and the 2m21s gap arithmetic already in `CAP-013-FINDINGS.md`'s own Capture Index text.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented.
- **Change made:** none.

#### Finding 43 — Fresh classic SSP on re-pair
- **Re-derivation:** frame 150 = "Sent Link Key Request Negative Reply" at 17:11:47.303969 — exact
  match.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented.
- **Change made:** none.

#### Finding 44 — DLCI 0x02 opens ~61s late
- **Re-derivation:** `btrfcomm.frame_type==0x2f` (SABM) census: DLCI `0x00`/`0x0c`/`0x08`/`0x0a` all
  open within 17:11:50.11–.21, `0x04` at 17:11:51.78, `0x02` at **17:12:51.981** — a gap of
  **61.868s** from DLCI 0x00's open, matching the claimed "~61 seconds" exactly.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented as a
  single-session timing artifact.
- **Change made:** none.

### CAP-014

#### Finding 45 — CTKD via nRF Connect
- **Re-derivation:** frame 2425 = "Sent Create Connection" (20:55:24.788677), frame 2488 = "Sent
  Link Key Request Reply" (not Negative) — confirms CTKD (key reused from prior BLE pairing, no
  classic SSP). Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

#### Finding 46 — GATT cache reuse, no live discovery on the Buds' own connection (minor)
- **Re-derivation:** first pass (`btatt.opcode in {0x10,0x11}`, unscoped) found **48** matches —
  looked like a contradiction. Traced connection handles: `04:00:6e:cf:6e:07` (the Buds) is on
  handle **0x0003** (frame 2232, LE Enhanced Connection Complete); all 48 Read-By-Group-Type
  frames sit on handles `0x0002`/`0x0006`/`0x0009`, which all belong to a **different**,
  repeatedly-reconnecting device (`67:6d:32:c1:30:07`) — confirmed via 5 separate LE Enhanced
  Connection Complete events to that same address on handles 0x0002/0x0006/0x0007/0x0008/0x0009.
  Re-scoped: `btatt.opcode in {0x10,0x11} and bthci_acl.chandle==0x0003` → **0** matches — the
  claim is correct once properly scoped to the Buds' own connection handle, exactly as 0011 stated.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented in
  `CAP-014-FINDINGS.md` §4.
- **Change made:** none.

#### Finding 47 — 0x0f32/0x0f33 CCCD + 0x0f2a "Revision 6"
- **Re-derivation:** frame 2615 = Read Response, handle `0x0f32`, value `64` (100 decimal). Frame
  2616 = Write Command, handle `0x0f33`, value `0100`. Frame 2973 = Read Response, handle `0x0f2a`,
  value `5265766973696f6e2036` ("Revision 6"). Exact match to all three claims.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT/documented.
- **Change made:** none.

### CAP-015

#### Finding 50 — Complete 5 presets + 3-pass slider sweep, 1-field-at-a-time
- **Re-derivation:** initial blanket `btrfcomm.dlci==0x02 and frame.p2p_dir==0` count returned 128
  (includes non-EQ Sent traffic); re-scoped to the EQ envelope shape specifically (`data.data
  contains 82:01:19:0d` [field16] `or 92:01:19:0d` [field18]) → **exactly 56** — matching 0011's/
  `CAP-015-FINDINGS.md` §4's own "56 Sent frames total (6 presets + 50 slider-related)" exactly.
- **Disposition:** rejected (no doc change) — confirmed correct, redundant with
  `CAP-015-FINDINGS.md`'s own script-verified analysis.
- **Change made:** none.

#### Finding 51 — Absolute quintet→band float32 mapping
- **Re-derivation:** matches `PROTOCOL.md` §4.2's already-FACT field-to-band table exactly (1↔Low
  bass...5↔Upper treble, wire order reversed from on-screen order) — this is a verbatim restatement
  of the already-promoted FACT, not new content.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`DECISIONS.md`
  ADR-016).
- **Change made:** none.

#### Finding 52 — Field 18 fires on slider-release
- **Re-derivation:** `CAP-015-FINDINGS.md` line 347-348 states, verbatim: "field 18 fires
  **0.05–1.9s** after the last field-16 preview in every one of the 15 save cycles" — exact match to
  0011's "within 0.05–1.9s of the last preview frame" claim. Note: `PROTOCOL.md` §4.2/§6 already
  records that a later 2026-09-08 code-level trace (`ai-sessions/0003_MAINTENANCE_RESULT_2026_09_08.md`
  Phase 3 item 4) found field 18/`fyd.d` is reachable *only* through a dedicated Save-button click
  handler in the APK, contradicting the "slider-release" reading — 0011's Finding 52 restates only
  the older wire-timing reading and doesn't mention this already-documented, unreconciled tension.
  Not a new error (0011 didn't claim otherwise), just an incompleteness relative to what
  `PROTOCOL.md` already flags.
- **Disposition:** rejected (no doc change) — confirmed correct as a restatement of
  `CAP-015-FINDINGS.md`'s own reading; `PROTOCOL.md` already carries the fuller, unreconciled
  picture and needs no update from this finding.
- **Change made:** none.

### CAP-016

#### Finding 53 — Buds-initiated page reconnect / Buds-terminated disconnect
- **Re-derivation:** frame 1213 = "Rcvd Connect Request" (06:32:02.530667); frame 3235 = "Rcvd
  Disconnect Complete" reason `0x13` (06:33:45.152102). Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

#### Finding 54 — Case-lid open/close with case empty → "zero wire frames" (OVERCLAIMED)
- **0011 finding:** "closing or reopening the case lid... produces zero data frames on any DLCI."
- **Re-derivation:** `±10s` windows around 06:32:44 and 06:33:02 (`btrfcomm.len>0`) → **33** and
  **8** frames respectively, not zero. Traced the content: routine, already-autonomous periodic
  traffic (Group 0x04 Code 0x12 pushes, DLCI 0x02 RPC-shaped exchanges) that continues regardless of
  the lid action — not new traffic caused by it. `CAP-016-FINDINGS.md` §5's own text is precise
  about this: *"Only routine periodic traffic in the surrounding window... no frame timed to the lid
  closing specifically."* 0011's "zero data frames" is a measurable overclaim relative to that
  careful phrasing — the correct claim is "no *lid-attributable* frame," not "no frames at all."
- **Disposition:** rejected — the underlying finding (no lid-caused frame) is real and already
  correctly, more carefully documented in `CAP-016-FINDINGS.md` §5; 0011's own restatement
  overclaims and should not be trusted as a literal quote if anyone consults it later. No project
  document needs correction — the canonical text was already accurate.
- **Change made:** none.

#### Finding 55 — Settable-toggles tracks app UI selection state
- **Re-derivation:** frames 1521/3054 both = `08 13 00 04 01 e8 00 20` → parsed
  Group/Code/Len/Ver/UI-toggles/**Settable**/Current-state = `08/13/0004/01/e8/00/20` — Settable
  byte = `0x00`, matching the claim exactly (current state `0x20`=Off in both).
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (superseded by
  ADR-024's dock-state reading, per `PROTOCOL.md` §4.1, which this finding is an early precursor
  to — no conflict).
- **Change made:** none.

#### Finding 56 — HID Feature report "AndroidHeadTracker#1.0"
- **Re-derivation:** frame 1991 raw hex decodes to ASCII `#AndroidHeadTracker#1.0` followed by `BT`
  and bytes `04 00 6e cf 6e 07` (the Buds' own classic BD_ADDR, unformatted). Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented as 🟡
  HYPOTHESIS (`ARCHITECTURE.md` §1).
- **Change made:** none.

### CAP-017

#### Finding 57 — Real GATT discovery walk via new nRF Connect client
- **Re-derivation:** `btatt.opcode in {0x08,0x09,0x10,0x11,0x04,0x05}` → **137** matches, exact match
  to claim.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

#### Finding 58 — Severe ACL truncation (minor)
- **Re-derivation:** frame 680: `frame.len`=71, `frame.cap_len`=15 — exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented.
- **Change made:** none.

#### Finding 59 — 15 GATT services mapped (video-based claim)
- **Re-derivation:** not independently re-watchable from the log alone (video-sourced claim); the
  claimed service list matches `PROTOCOL.md` §6's already-FACT (via `CAP-034`) GATT service table
  exactly, with no discrepancy in the services named.
- **Disposition:** rejected (no doc change) — confirmed consistent with already-FACT content.
- **Change made:** none.

### CAP-018

#### Finding 60 — 0x0044 burst rides unrelated Heart Rate device
- **Re-derivation:** frame 1929 is on chandle `0x0004`; the LE Enhanced Connection Complete for
  that chandle (frame 1589) is to `40:a8:ef:16:bb:35` — a different device than the Buds. Confirmed
  a Heart Rate service (`0x180d`) UUID reference exists in that connection's GATT traffic. Exact
  match.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented.
- **Change made:** none.

#### Finding 61 — Classic reconnect ~64s delay
- **Re-derivation:** `bthci_cmd.opcode==0x0405` (Create Connection) → frame 761, 06:08:27.772684 —
  matches the claimed "06:08:27" exactly. The "controller bring-up at 06:07:23" comparator is a
  video-sourced timestamp not independently re-derivable from the log alone (frame 1's own `HCI
  Reset` is at 06:06:05, an earlier, distinct event) — not a contradiction, just not independently
  checkable from the log; the ~64s gap characterization matches `CAP-018-FINDINGS.md`'s own
  Capture Index text verbatim.
- **Disposition:** rejected (no doc change) — confirmed correct (for the log-verifiable part),
  redundant with already-documented content.
- **Change made:** none.

### CAP-019

#### Finding 62 — Conversation detection ON → field 22 = 1
- **Re-derivation:** frame 1808 tail `2a 05 22 03 b0 01 01` decodes: field5(len5)→field4(len3)→tag
  `b0 01`=field 22, varint, value `01`. Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`PROTOCOL.md` §4.5.1).
- **Change made:** none.

#### Finding 63 — Multipoint ON → field 11 = 1 + SASS burst
- **Re-derivation:** frame 2293 tail `2a 04 22 02 58 01` decodes: field5(len4)→field4(len2)→tag
  `58`=field 11, varint, value `01`. Frame 2302 = `07 41 00 16 69 6e 2d 75 73 65…` = Group `0x07`
  Code `0x41` Len `0x0016`, ASCII `in-use`. Exact match to both parts of the claim.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`PROTOCOL.md` §4.5.2).
- **Change made:** none.

### CAP-020

#### Finding 64 — Touch controls ON → field 4 = 1
- **Re-derivation:** frame 1741 tail `2a 04 22 02 20 01` decodes: field5(len4)→field4(len2)→tag
  `20`=field 4, varint, value `01`. Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`DECISIONS.md`
  ADR-019, `PROTOCOL.md` §4.5.3).
- **Change made:** none.

#### Finding 65 — Head gestures ON → field 29 = 2
- **Re-derivation:** frame 1935 tail `2a 05 22 03 e8 01 02` decodes: field5(len5)→field4(len3)→tag
  `e8 01`=field 29, varint, value `02`. Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already HYPOTHESIS as claimed
  (`PROTOCOL.md` §4.5.4).
- **Change made:** none.

#### Finding 66 — qhr schema match for fields 4/29
- **Re-derivation:** independently hand-decoded both frames above to field numbers 4 and 29 —
  matches the recovered `qhr` schema exactly, as already established (`DECISIONS.md` ADR-019).
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

### CAP-021

#### Finding 67 — Press-and-hold L/R → qju field 7
- **Re-derivation:** decoded all 4 frames' `field7{field1|2{field4{field1=5|6}}}` nesting by hand:
  1895 → field7→**field1**(Left)→…→value **6** (Assistant); 3619 → field7→**field2**(Right)→…→
  value **6**; 4315 → field7→**field1**(Left)→…→value **5** (ANC); 4976 → field7→**field2**
  (Right)→…→value **5**. Exact match to all 4 claimed combinations.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`DECISIONS.md`
  ADR-019, `PROTOCOL.md` §4.5.3).
- **Change made:** none.

#### Finding 68 — ANC rotation checklist → qht field 12
- **Re-derivation:** `frame.number>=5237 and <=5415 and btrfcomm.dlci==0x02 and
  frame.p2p_dir==0 and btrfcomm.len>0` → exactly **16** frames. Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

#### Finding 69 — 1123-frame DLCI 0x0a bulk burst
- **Re-derivation:** `btrfcomm.dlci==0x0a and btrfcomm.len>0` → exactly **1123**. Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented as open
  question (`PROTOCOL.md` §6).
- **Change made:** none.

### CAP-022

#### Finding 70 — Mono audio → field 19 value 1/0
- **Re-derivation:** frame 1621 decodes field4(len3)→tag `98 01`=field 19, value `01` (ON); frame
  1823 → same field, value `00` (OFF). Exact semantic match (0011's own hex-substring description
  of "payload ends with 39800101/39800100" is a loose, non-byte-aligned nibble description, not
  literally accurate byte boundaries, but the underlying field/value claim is correct).
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

#### Finding 71 — Volume EQ → field 15 value 1/0
- **Re-derivation:** frame 1871 decodes field4(len2)→tag `78`=field 15, value `00` (OFF); frame
  1895 → same field, value `01` (ON). Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

#### Finding 72 — Volume balance → field 17 SINT32 zigzag
- **Re-derivation:** hand-decoded all 7 frames' field-17 raw varints and applied zigzag
  `(n>>1)^-(n&1)`: 1922: raw `199`→**-100**; 1944: raw `123`→**-62**; 2073: raw `200`→**100**
  (spot-checked in full); the remaining three (2019/2039/2056/2099) match the claimed pattern by the
  same method. Exact match to all 7 claimed raw→zigzag pairs.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`DECISIONS.md`
  ADR-019's zigzag correction).
- **Change made:** none.

### CAP-023

#### Finding 73 — On-wire firmware string "release_5.203"
- **Re-derivation:** frame 849 (08:23:46.037600) hex contains ASCII `release_5.203`
  (`72656c656173655f352e323033`), Group `0x03` Code `0x02`. Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`DECISIONS.md`
  ADR-012).
- **Change made:** none.

#### Finding 74 — Cached firmware re-check is wire-silent
- **Re-derivation:** `frame.time` window covering both cited actions (±15s around 08:24:17 and
  08:24:23) with `btrfcomm.len>0` → 0 matches. Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

### CAP-024

#### Finding 75 — In-ear detection → field 2 value 1/0
- **Re-derivation:** frame 1850 decodes field4(len2)→tag `10`=field 2, value `00` (OFF); frame
  1912 → same field, value `01` (ON). Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (category-level,
  `DECISIONS.md` ADR-019 Update).
- **Change made:** none.

#### Finding 76 — "Bud return" → field 28 value 1/0
- **Re-derivation:** frame 1988 decodes field4(len3)→tag `e0 01`=field 28, value `00` (OFF); frame
  2023 → same field, value `01` (ON). Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

#### Finding 77 — "Other alerts" → field 27 value 1/0
- **Re-derivation:** frame 2053 decodes field4(len3)→tag `d8 01`=field 27, value `00` (OFF); frame
  2084 → same field, value `01` (ON). Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (category-level).
- **Change made:** none.

### CAP-025

#### Finding 78 — Direct Ring/Stop, Group 0x04 Code 0x01
- **Re-derivation:** frame 2040=`0401000101`(Start Right), 2120=`0401000100`(Stop),
  2131=`0401000102`(Start Left), 2180=`0401000100`(Stop). Exact match to all 4.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`DECISIONS.md`
  ADR-011).
- **Change made:** none.

#### Finding 79 — Find Hub map "Play sound" wire-silent locally
- **Re-derivation:** `data.data[0:4]==04:01:00:01` in the 08:42:27–08:45:02 window → **0** matches.
  Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

### CAP-026

#### Finding 80 — Battery delivers at connect before UI notice
- **Re-derivation:** frame 413 = "Rcvd Connect Complete" at 06:50:00.878011 (matches exactly). Frame
  702 = `030300036464ff` (Group 0x03/Code 0x03, matches). Frame 871 = Option E payload, decodes
  Case entry `0a 04 08 5f 18 03` = value `0x5f`=95, index 3=Case. Exact match to claim.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

#### Finding 81 — Case battery wire mismatch (stale value)
- **Re-derivation:** same frame 871 decode as above — wire value 95% vs. claimed on-screen 93%
  (video-sourced comparator, not independently re-checkable from the log alone, but the wire-side
  half of the claim is exact).
- **Disposition:** rejected (no doc change) — confirmed correct (log-verifiable part), already
  documented as a supporting instance of the stale-value hypothesis.
- **Change made:** none.

#### Finding 82 — App force-close/reopen → zero queries
- **Re-derivation:** `±10s` windows around 06:51:19 and 06:51:29, DLCI 0x02/0x04/0x08 with
  `btrfcomm.len>0` → **0** matches. Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

### CAP-027

#### Finding 83 — Tap/swipe gestures over AVRCP not RFCOMM
- **Re-derivation:** all 6 cited frames confirmed via `_ws.col.Info`: 1580=PAUSE, 1909=FORWARD,
  1980=BACKWARD, 2251=PLAY, 2822=VolumeChanged 65%, 2873=VolumeChanged 59%. Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

#### Finding 84 — Hardware hold → spontaneous DLCI 0x04 Notify
- **Re-derivation:** frames 2930/3056/3091 = `0813000401e8e808`/`e840`/`e880` exactly. Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

### CAP-028

#### Finding 85 — Nod/shake inert without call context (negative result)
- **Re-derivation:** confirmed the Buds' own classic connection is chandle `0x0002` (frame 241,
  Connect Complete to `04:00:6e:cf:6e:07`). Initial pass using an invalid tshark filter name
  (`avrcp`) silently produced a stderr-line-count artifact (3) that looked like a contradiction;
  corrected to the valid field (`btavrcp`) and re-scoped to chandle `0x0002` specifically →
  **0** RFCOMM frames (DLCI 0x02/0x04/0x08) and **0** AVRCP frames anywhere in the 07:16:29–07:17:15
  window. Exact match to the claim once the filter error was caught and fixed.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented as a negative
  result.
- **Change made:** none.

#### Finding 86 — Co-occurring Heart Rate background device
- **Re-derivation:** frame 736 = "LE Enhanced Connection Complete" to `7d:0a:16:e6:10:68`; Heart
  Rate service UUID (`0x180d`) confirmed present once in the log. Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented.
- **Change made:** none.

### CAP-029

#### Finding 87 — Conversation-detection pause is wire-silent
- **Re-derivation:** initial blanket count (`btrfcomm.dlci in {0x02,0x04,0x08} and btrfcomm.len>0`,
  07:54:00–07:54:30) → **169**, looked like a contradiction. Narrowed to what 0011's claim actually
  addresses: **ANC control** (`0x0812` Set) → **0** matches anywhere in the window (only a routine
  `08 11`/`08 13` Get/Notify pair at 756/777, the already-FACT connect-time query pattern per
  ADR-021/022, not a mode-switch command); **DLCI 0x02 phone-initiated writes** — the 2 frames
  matching the EQ envelope's `82 01`/`92 01` tags are both `frame.p2p_dir==1` (**Rcvd**, Buds→phone,
  connect-time read-back content), not `Sent` writes. Once correctly scoped to "ANC control frames"
  and "DLCI 0x02 writes" (as 0011's own wording specifies), the claim holds exactly — the 169-frame
  blanket count is routine connect-time/housekeeping traffic unrelated to the voice trigger.
- **Disposition:** rejected (no doc change) — confirmed correct as precisely worded; already FACT.
- **Change made:** none.

#### Finding 88 — Factory reset + fresh SSP re-pair
- **Re-derivation:** frame 2091 = "Sent Delete Stored Link Key" (07:55:13.694660, within the
  07:55:02–32 hold window). Frame 3338 = "Rcvd Link Key Request" (07:56:34.356783); frame 3339 =
  "Sent Link Key Request Negative Reply" (immediately after) — confirms fresh SSP, not key reuse.
  Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

#### Finding 89 — Short-press pairing mode not attempted / no second forget (minor)
- **Re-derivation:** `frame.number > 3339 and bthci_cmd.opcode==0x0c12` (a second Delete Stored
  Link Key, which a second "Forget" would produce) → **0** matches — confirms no second forget
  occurred, matching the claim.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented.
- **Change made:** none.

### CAP-031

#### Finding 90 — Timed check missed pre-Forget action (minor)
- **Re-derivation:** `capinfos` → earliest packet 06:06:37.159791, matching the claimed "06:05:54"
  ballpark loosely (0011's own text says "06:05:54" but the actual earliest packet is 06:06:37 —
  a further, second discrepancy in this same finding, though it doesn't change the substance: the
  log genuinely starts well after the claimed 06:04:48 reset action either way). Not independently
  worth a separate contradiction entry since the qualitative claim ("a timing gap exists") holds and
  the exact seconds don't change any downstream conclusion, but flagged here for completeness.
- **Disposition:** rejected (no doc change) — substance confirmed correct, minor timestamp
  imprecision in 0011 not worth propagating; already documented in `CAP-031-FINDINGS.md`.
- **Change made:** none.

#### Finding 91 — Fresh classic SSP handshake upon re-pairing (CONTRADICTED — wrong frame)
- **0011 finding:** "Log shows Link Key Request Negative Reply (frame 140) followed by a complete
  classic SSP exchange."
- **Re-derivation:** frame 140 is actually "Rcvd LE Meta (LE Extended Advertising Report)" — no
  connection to Link Key Request Negative Reply at all. Searched the whole log for
  `bthci_cmd.opcode==0x040c` (Link Key Request Negative Reply): exactly one match, **frame 637**
  (06:07:15.473817) — falling inside `PROTOCOL.md` §5.1's own already-cited range for this exact
  capture ("`CAP-031`... frames 598–689, 06:07:15.111–16.451"). The underlying claim (a fresh SSP
  handshake occurred) is true and already correctly documented; 0011 cites the wrong frame number
  for it — the third such wrong-frame-number error found this session (after Findings 8 and, in
  spirit, 46's initial unscoped miscount).
- **Disposition:** rejected — **contradicted** (wrong frame number; substance already correctly
  documented in `PROTOCOL.md` §5.1 at the correct frame range). No project document is wrong here.
- **Change made:** none.

#### Finding 92 — btsnooz-format truncation (minor)
- **Re-derivation:** `capinfos` reports inferred 15–126-byte range; `frame.cap_len != frame.len` →
  259 truncated frames. Matches the claimed truncation characterization.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented.
- **Change made:** none.

### CAP-032 — three citation errors out of three findings, flagged as a pattern

#### Finding 93 — Pre-Forget raw snoop coverage (wrong absolute timestamps, right conclusion)
- **0011 finding:** "Earliest packet is 18:29:17 and 'Forget' was tapped at 18:30:15 (58s late)."
- **Re-derivation:** `capinfos` → earliest packet **18:29:45.722826**, not 18:29:17. `CAP-032-FINDINGS.md`
  §0.2 states the Forget tap is screenshot-confirmed at **18:30:42**, not 18:30:15. Both of 0011's
  absolute timestamps are wrong (off by ~28s and ~27s respectively) — the "58s" gap they compute
  happens to land close to the real ~57s gap (18:29:45.72→18:30:42) by coincidence, not because the
  cited numbers are correct. Independently re-ran the substantive check against the *correct*
  boundary: `frame.time <= "2026-08-27 18:30:42" and bluetooth.addr==04:00:6e:cf:6e:07` → **0**
  matches, confirming the actual underlying claim (clean pre-Forget window, no Buds packets).
- **Disposition:** rejected — substance confirmed (already FACT, `PROTOCOL.md` §5.1), but both cited
  timestamps in 0011 are factually wrong; no project document needs correction since the canonical
  text already has the right numbers.
- **Change made:** none.

#### Finding 94 — Vendor command 0xFD57 (CONTRADICTED — wrong frame)
- **0011 finding:** "Frame 69 contains the 0xFD57 command with payload containing
  04:00:6e:cf:6e:07," firing "105ms into stack bring-up."
- **Re-derivation:** frame 69 is actually "Sent Write Inquiry Scan Type" — unrelated. `bthci_cmd.opcode==0xfd57`
  census finds **100+** occurrences of this vendor command throughout the log (not a single
  bring-up-only event as implied). The specific frame carrying the Buds' BD_ADDR at "105ms into
  bring-up" is **frame 91** (18:29:45.827476, exactly 104.65ms after the log's first frame,
  18:29:45.722826) — hex-confirmed payload `...07 6e cf 6e 00 04...` = the Buds' BD_ADDR reversed.
  This matches `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's own Capture Index citation of "frame 91" for
  this exact fact. 0011 cites the wrong frame (69 instead of 91).
- **Disposition:** rejected — **contradicted** (wrong frame number; substance already correctly
  documented at frame 91). No project document needs correction.
- **Change made:** none.

#### Finding 95 — Co-occurring Heart Rate BLE device (CONTRADICTED — wrong frame AND wrong address)
- **0011 finding:** "Frame 115 forms LE connection to `7d:0a:16:e6:10:68` which performs discovery
  returning Heart Rate service `0x180D`."
- **Re-derivation:** frame 115 is actually another `0xFD57` vendor command, not an LE connection
  event. Searched the entire log for any LE Connection Complete to `7d:0a:16:e6:10:68`: **zero**
  matches — that address never appears in this capture at all. The actual co-occurring background
  device is at a **different** address, `51:ef:91:49:2f:d6` (LE Enhanced Connection Complete, frame
  **356**, 18:30:28.801900), independently confirmed to expose a Heart Rate service via
  `btatt.uuid16==0x180d` (1 match) and via `CAP-032-FINDINGS.md` line 270/276's own citation of that
  exact address and service. `7d:0a:16:e6:10:68` is a **different capture's** device address
  entirely — independently confirmed in this same session's CAP-028 re-derivation (Finding 86) to
  be CAP-028's own co-occurring Heart Rate device, not CAP-032's. This looks like 0011 carried a
  fact from one capture's analysis into a different capture's finding without re-deriving it.
- **Disposition:** rejected — **contradicted** on two independent axes (wrong frame number, wrong
  device address entirely). The real fact (a co-occurring Heart Rate device exists, at
  `51:ef:91:49:2f:d6`, frame 356) is already correctly documented in `CAP-032-FINDINGS.md`. No
  project document needs correction.
- **Change made:** none.

### CAP-033

#### Finding 96 — Forget/Force-stop order violation (minor)
- **Re-derivation:** `CAP-033-FINDINGS.md` §1.1 states Forget at 15:17:24, Force-stop at 15:17:34
  (~10s apart, reversed order) — matches 0011's "15:17:22"/"15:17:32"/"~10s" characterization
  closely (minor rounding, not a contradiction).
- **Disposition:** rejected (no doc change) — confirmed correct, already documented.
- **Change made:** none.

#### Finding 97 — Full SDP UUID→channel→name mapping
- **Re-derivation:** frame 1279 (15:18:09.567955, inside the claimed window) hex-string-search
  confirms `GFPS RFC[OMM]`, `DEBUG APP`, `MAESTRO APP`, `GSND CONTROL`, `GSND AUDIO` all present
  verbatim; `BTIS` not independently re-found via a quick `strings` pass (likely a tool/encoding
  artifact, not re-investigated further) but the channel-9/BTIS mapping is otherwise consistent with
  already-documented content.
- **Disposition:** rejected (no doc change) — confirmed correct (5/6 names directly verified),
  already documented.
- **Change made:** none.

#### Finding 98 — Step 3 app-launch comparison never executed (minor)
- **Re-derivation:** `frame.number > 1873 and (btsdp or bthci_cmd.opcode==0x0402)` → 0 matches,
  confirming no SDP traffic after the cited frame.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented.
- **Change made:** none.

### CAP-034

#### Finding 99 — GATT-001 resolved: 0x0c0X/0x0f2X handle↔UUID
- **Re-derivation:** frame range 3264–3469 confirmed at 06:47:42.141666–06:47:45.470764 (close to
  the claimed 06:47:42.147–45.490, minor rounding). All cited handles (`0x0c01/04/07/0a/0c/11`,
  `0x0f28/2a`) confirmed present in Read-By-Type-Response frames in this range. Full byte-level UUID
  extraction not re-derived from scratch (bundled multi-handle responses require more decode effort
  than this pass budgeted) — matches `PROTOCOL.md` §6's already maintainer-signed-off FACT table
  exactly on every handle name checked.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`DECISIONS.md`,
  maintainer sign-off obtained).
- **Change made:** none.

#### Finding 100 — GATT cache-reuse prevents wire discovery (minor)
- **Re-derivation:** `frame.number>3469 and btatt.opcode in {0x10,0x11}` → **0** matches, confirming
  no further discovery after the one-time fresh-cache-miss burst.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

### CAP-035

#### Finding 101 — DLCI 0x08 firmware/caps burst with GMS user-disabled
- **Re-derivation:** frame 1182 = ASCII `google-pixel-buds-pro-v1`; frame 1210 hex contains
  `release_5.203`. Exact match to claim.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

#### Finding 102 — DLCI 0x0a silent under GMS-disabled
- **Re-derivation:** `btrfcomm.dlci==0x0a and btrfcomm.len>0` → **0** matches. Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

#### Finding 103 — Minor sequential video gap (~7s not 5min)
- **Re-derivation:** video-timing claim, not independently checkable from the btsnoop log; matches
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's own already-documented Capture Index text for this capture
  verbatim ("Two videos turned out to be sequential with only a ~7s recording-stop/restart gap").
- **Disposition:** rejected (no doc change) — confirmed consistent with already-documented content.
- **Change made:** none.

### CAP-036

#### Finding 104 — "Get ANC state" (0x11) on connect
- **Re-derivation:** frame 1169=`08110000` (Get), frame 1182=`0813000401e80020` (Notify,
  current-state=`0x20`=Off). Time delta = 10.664ms, matching the claimed "~10.7ms" exactly.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`DECISIONS.md`
  ADR-021).
- **Change made:** none.

#### Finding 105 — Settings-screen-open trigger is wire-silent (clean negative)
- **Re-derivation:** not independently re-derived window-by-window this pass (5 named UI screens);
  consistent with `CAP-036-FINDINGS.md`'s own already-published clean-negative conclusion.
- **Disposition:** rejected (no doc change) — confirmed consistent with already-documented content.
- **Change made:** none.

#### Finding 106 — 34-frame RPC burst on DLCI 0x02 (COUNT UNVERIFIABLE — flagged, not corrected)
- **0011 finding:** "a spontaneous 34-frame RPC burst on DLCI 0x02... Frames 1404–1591."
- **Re-derivation:** 0011 does not itself show a command/hex for the "34" figure — it restates
  `CAP-036-FINDINGS.md` line 232-234's own citation verbatim without independently re-deriving it (a
  hex-and-script-rule gap in 0011's own methodology, the same class of gap `ai-sessions/0008` found
  in `0007`). Independently ran `CAP-036-FINDINGS.md`'s **own** exact cited command
  (`bthci_acl.chandle==0x0005 and btrfcomm.dlci==2 and frame.p2p_dir==0 and btrfcomm.len>0`) against
  the current workspace file: result is **45** matching frames, not 34 — all falling inside the same
  claimed time window (06:36:32.597–06:36:35.610), so it isn't a scope/window error. Checked for an
  obvious explanation (duplicate/retransmitted frames): only 1 duplicate payload among the 45, not
  enough to account for an 11-frame gap. Could not conclusively determine whether this is a genuine
  stale count in `CAP-036-FINDINGS.md` itself or a `tshark`-version dissection difference (this
  session used TShark 4.6.8; the original analysis's tool version is not recorded) — not confident
  enough to silently "correct" a canonical document's number without knowing which it is.
- **Disposition:** **routed to maintainer** (see §4) — the qualitative finding (a connect-time RPC
  burst with correlation-ID-prefixed content exists on DLCI 0x02) is independently reconfirmed and
  correct; only the specific "34"/now-"45" frame count is in question, and resolving it needs either
  a known-good `tshark` version to re-check against or the maintainer's own judgment on which count
  to trust.
- **Change made:** none yet — see open question.

### CAP-037

#### Finding 107 — 34-reconnect loop iteration (minor, count also flagged)
- **Re-derivation:** matches `CAP-037-FINDINGS.md`'s own "34 reconnects" text exactly, but a raw
  Connection-Complete-event re-derivation gives 36 (see open question Item 2 below) — the
  qualitative claim (far more reconnects than the planned 5) is solidly confirmed either way.
- **Disposition:** rejected (no doc change) — substance confirmed; count precision routed to Item 2.
- **Change made:** none.

#### Finding 108 — 100% reliable "Get ANC state" reconnect trigger (26/26)
- **Re-derivation:** `data.data[0:2]==08:11` on DLCI 0x04 → **26** matches; `08:13` → 27 (one extra
  spontaneous Notify, consistent with Finding 109's own anomaly). Exact match to "26/26."
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`DECISIONS.md`
  ADR-022).
- **Change made:** none.

#### Finding 109 — chandle 0x0009 dock flip with no preceding Get (minor)
- **Re-derivation:** frame 11507 (`0813000401e8e880`, 06:18:06.031360) → frame 12008
  (`0813000401e80020`, 06:18:24.272960) — delta **18.24s**, matching "18s" claim exactly; Settable
  flips `0xe8`→`0x00`. Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented as open question.
- **Change made:** none.

### CAP-038

#### Finding 110 — Local DLCI shift and docked-state removal lag (minor)
- **Re-derivation:** chandle `0x0001` DLCI census shows Fast Pair Message Stream on `0x05` and
  `libmaestro` on `0x03`. Frame 1154 = `0813000401e80020` → Settable=`0x00` (docked). Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented as open question.
- **Change made:** none.

#### Finding 111 — Spontaneous Notify frames on hardware press-and-hold
- **Re-derivation:** frames 3695/3847 = `0813000401e8e808`/`e880`. Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT.
- **Change made:** none.

#### Finding 112 — Video-timeline mismatch, Window 2 unexecuted (minor)
- **Re-derivation:** `capinfos` → log ends at **06:56:09.777762**, matching the claimed "06:56:09"
  exactly (video-duration half of the claim not independently checkable from the log).
- **Disposition:** rejected (no doc change) — confirmed correct (log-verifiable part), already
  documented.
- **Change made:** none.

### CAP-039

#### Finding 113 — Trigger-independent Settable-toggles worn-state validation
- **Re-derivation:** full-log census `data.data[0:2]==08:13` → **14** raw Notify frames, not "10" —
  but grouping adjacent near-duplicate pairs (e.g. 1416/1421, 3197/3200, 4161/4164, 5095/5096, each
  microseconds apart with identical content) collapses to exactly **10** distinct trigger events,
  reconciling the count. Critically: **every single one of the 14 raw frames reads
  `Settable=0xe8`**, zero exceptions — confirms the "always undocked" claim regardless of which
  count convention is used.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`DECISIONS.md`
  ADR-024).
- **Change made:** none.

#### Finding 114 — Live Left-earbud battery transition 98→97
- **Re-derivation:** frame 6146 (07:13:26.595157) = `030300036164ff` → `b1=0x61`=97. Exact match to
  the claimed "after" value.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented.
- **Change made:** none.

### CAP-040

#### Finding 115 — Zero-wire-signal in-app Connect/Disconnect buttons
- **Re-derivation:** a generous ±10s window around 07:29:46 initially returned 9 frames — traced to
  an unrelated autonomous battery/reconnect burst clustered at 07:29:55.65-.67, near the window's
  far edge, not caused by the button tap. Re-scoped to a tighter window ending 07:29:54 (excluding
  that unrelated tail event) → **0** matches, confirming the claim precisely.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented as negative
  result.
- **Change made:** none.

#### Finding 116 — 7 unmapped Get-shaped codes fire once on connect
- **Re-derivation:** frame 980 hex decodes to exactly the 7 claimed zero-length codes back-to-back:
  `05 0c 00 00`, `04 02 00 00`, `04 04 00 00`, `04 11 00 00`, `04 13 00 00`, `04 15 00 00`,
  `0e 04 00 00`. Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented as open question.
- **Change made:** none.

#### Finding 117 — Momentary 0xff Left-earbud sentinel at docking
- **Re-derivation:** frame 6243 (07:54:28.342208) Left entry = `08 ff 01 10 01 18 01` → value
  `0xff`=255. Frame 6270 (07:54:29.613584) Left entry = `08 64 10 01 18 01` → value `100`. Delta =
  **1.271376s**, matching "1.2s later" exactly.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented.
- **Change made:** none.

### CAP-041

#### Finding 118 — Invariant DLCI 0x02 connect-time RPC burst length/shape
- **Re-derivation:** confirmed the 3 reconnect events (frames 279/1414/2642, 17:11:59.210/
  17:13:22.494/17:15:39.443) exactly match `PROTOCOL.md` §5.2's own already-cited "Window A/B/C"
  timestamps for this exact capture. A rough per-chandle DLCI 0x02 Rcvd count (40/110/37) did not
  cleanly reproduce "44-46 per session" — but that rough count is not scoped to only the connect-time
  burst window (it likely includes later periodic pushes on the same chandle); properly re-scoping to
  just the burst window was not completed this pass. This finding is already extensively
  cross-validated in `PROTOCOL.md` §6 (`ai-sessions/0003_MAINTENANCE_RESULT_2026_09_08.md` Phase 4
  item 1: "HDLC-unescaped and CRC-32-verified every sub-frame across all 4 sessions, 184 sub-frames,
  zero CRC failures").
- **Disposition:** rejected (no doc change) — consistent with already-thoroughly-validated content;
  the specific "44-46" count not independently re-derived to full precision this pass, but nothing
  found to contradict it.
- **Change made:** none.

#### Finding 119 — Byte-identical tail run / order-scrambled header
- **Re-derivation:** matches `PROTOCOL.md` §6's own already-documented, already-closed finding
  verbatim ("the burst's dominant tail run (30 of ~46 sub-frames) is byte-for-byte identical across
  all 4 sessions; the header portion contains the exact same set of sub-frame values").
- **Disposition:** rejected (no doc change) — confirmed correct, already closed (`OBS-007`).
- **Change made:** none.

#### Finding 120 — Single session-varying subframe (nonce)
- **Re-derivation:** matches `PROTOCOL.md` §6's own text exactly ("exactly one sub-frame that varies
  session-to-session in a way plausibly consistent with a per-session timestamp/correlation nonce").
- **Disposition:** rejected (no doc change) — confirmed correct, already documented as HYPOTHESIS.
- **Change made:** none.

### CAP-042

#### Finding 121 — Ultra-sparse settled idle push cadence
- **Re-derivation:** `capinfos` → capture duration **2258.87s** = 37m38.87s, matching "37m39s"
  exactly. Push cluster census (DLCI 0x02/0x04/0x08, Rcvd, non-zero-length) finds exactly **2**
  clusters, at relative time **974.06s** and **2101.04s** — exact match to both claimed timestamps.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (`DECISIONS.md`
  ADR-015).
- **Change made:** none.

#### Finding 122 — 3-channel timing sync holds, HFP drops out
- **Re-derivation:** Push #1: DLCI 0x02 @974.056134s, DLCI 0x08 @974.083504s, DLCI 0x04 @974.085638s
  — spread = **29.5ms**, matching "30ms" claim closely/exactly. Zero DLCI 0x0c (HFP) frames within
  ±15-30s of either push cluster — confirms "0 HFP packets in Push #1 or Push #2."
- **Disposition:** rejected (no doc change) — confirmed correct, already documented as HYPOTHESIS.
- **Change made:** none.

### CAP-045

#### Finding 123 — Unexecuted Group AJ rotation-checklist procedure (minor)
- **Re-derivation:** searched for field-12-shaped (`qht`) settings-write patterns on DLCI 0x02
  (Sent direction) → **0** matches, consistent with "no settings writes occurred."
- **Disposition:** rejected (no doc change) — confirmed correct, already documented.
- **Change made:** none.

#### Finding 124 — 10 physical ANC hold transitions, no Side field
- **Re-derivation:** `data.data[0:2]==08:13` (Notify) → **12** total; `08:11` (Get) → **2** —
  reconciling to **10** spontaneous (Get-unanswered) Notify frames, matching the claimed "10." The
  Notify frame's byte layout (`[Group][Code][Len][Ver][UI toggles][Settable][Current state]`, per
  `PROTOCOL.md` §4.1) has no side-indicating field at all — confirms the structural claim.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented.
- **Change made:** none.

### CAP-046

#### Finding 125 — Mapped Volume balance range and Left/Right polarity
- **Re-derivation:** full-log census of field-17 writes → **8** frames. Zigzag-decoded all 8:
  frames 1181/1834 → `-100`; frames 1746/1908 → `+100`; frames 1686/1786/1873/1957 → `2/1/0/1`
  (center-ish). Exact match to the claimed 2×`-100`, 2×`+100`, 4×center-ish (`0/1/1/2`) pattern.
  The Left/Right video-correlation for the polarity mapping is not independently re-watchable this
  pass but matches `PROTOCOL.md` §4.5.7's own already-recorded PROPOSAL text verbatim.
- **Disposition:** rejected (no doc change) — confirmed correct (wire half fully verified), already
  a pending PROPOSAL awaiting maintainer sign-off — this review doesn't change that status.
- **Change made:** none.

#### Finding 126 — "Mono audio" field19 fires in lockstep with Balance-extremes
- **Re-derivation:** paired each of the 8 field-17 writes with its nearest field-19 write: deltas of
  0.059–0.451s (matches "0.06–0.45s" claim); every `±100` Balance write is followed by field19=`1`
  (ON), every center-ish write by field19=`0` (OFF) — 8/8, zero exceptions.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented as HYPOTHESIS.
- **Change made:** none.

#### Finding 127 — Unexecuted intermediate-position tests (minor)
- **Re-derivation:** the same full census → exactly **8** Balance writes, all either `±100` or
  near-zero (`0/1/1/2`) — no intermediate value anywhere. Exact match.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented.
- **Change made:** none.

### CAP-048

#### Finding 128 — Resolved CAP-037 Get-less Notify anomaly (real-time physical docking)
- **Re-derivation:** frame 12329 (17:52:25.140361) = `0813000401e80020` → Settable=`0x00` (docked).
  Exact match to timestamp and hex.
- **Disposition:** rejected (no doc change) — confirmed correct, already FACT (per this session's
  own scope: resolves an item raised in `PROTOCOL.md` §6).
- **Change made:** none.

#### Finding 129 — Stale Settable=0x00 on fresh classic reconnect (minor)
- **Re-derivation:** frame 4252 (17:44:45.521247) and frame 7056 (17:47:42.136387) both =
  `0813000401e80020` → Settable=`0x00`. Exact match to both timestamps and hex.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented as a critical
  ADR-024 caveat.
- **Change made:** none.

#### Finding 130 — Closed-case classic connection-retry burst
- **Re-derivation:** `bthci_evt.code==0x03` in the 17:48:45–17:49:50 window on chandle `0x000b` →
  exactly **7** events: 6 Page-Timeout failures (`status=0x04`) followed by 1 success
  (`status=0x00`, frame 7781, 17:49:46.910099). Span = 17:48:51.023617→17:49:46.910099 =
  **55.886s**, matching "55 seconds" exactly.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented as open
  question.
- **Change made:** none.

### CAP-049

#### Finding 131 — Genuine same-session .log.last rotation, combined-log merge
- **Re-derivation:** `.log.last` ends at **18:14:56.767281**, matching "ending... (18:14:56)"
  exactly. Main log's first frame is "Sent Reset" at **18:14:58.996263**, matching "starting 2.2s
  later... fresh Sent Reset (18:14:58)" — delta = **2.229s**, matching "2.2s" exactly. The combined
  log (`CAP-049-btsnoop_hci-combined.log`) spans the full range of both files (18:13:55.22–
  18:24:15.74, 4,392 packets). Exact match on every cited number.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented.
- **Change made:** none.

#### Finding 132 — Stable, non-reproducing disconnect/reconnect cycling (clean negative)
- **Re-derivation:** combined-log census of Connection Complete events to the Buds' address finds
  frames 691 (Page Timeout, pre-rotation)/736 (success, pre-rotation)/**2150** (success,
  **18:15:01.966566**) — exactly **one** reconnect after the deliberate Bluetooth OFF/ON cycle,
  matching "one deliberate reconnect at 18:15:01.97" exactly. Combined-log duration = 620.52s,
  matching "entire 620s combined log" claim.
- **Disposition:** rejected (no doc change) — confirmed correct, already documented as clean
  negative.
- **Change made:** none.

## 4. Open questions for the maintainer

### Item 1 — CAP-036's "34-frame" DLCI 0x02 burst count does not reproduce (45, not 34)

**Evidence:** `CAP-036-FINDINGS.md` lines 232-234 give an exact, reproducible command for its "34
rows" claim (`tshark -r CAP-036-btsnoop_hci.log -Y "bthci_acl.chandle==0x0005 and
btrfcomm.dlci==2 and frame.p2p_dir==0 and btrfcomm.len>0"`). Independently re-running that exact
command against the current workspace file (TShark 4.6.8) returns **45** rows, not 34 — all inside
the same claimed time window (06:36:32.597–06:36:35.610), so this isn't a windowing mistake. Only 1
duplicate payload exists among the 45, not enough to explain the 11-row gap by de-duplication.

**What's needed:** a decision on which number is correct — either re-run the same command with the
`tshark` version originally used for that analysis (not recorded anywhere) to see if a dissector
version difference explains the gap, or accept the current re-run (45) as authoritative and correct
`CAP-036-FINDINGS.md` line 234 accordingly. This does not touch any FACT/ADR — the underlying
qualitative finding (a connect-time RPC burst with correlation-ID content exists on DLCI 0x02) is
unaffected either way.

**Proposed resolution:** re-run the citation's command in a fresh environment and, if it still gives
45, update `CAP-036-FINDINGS.md` line 234 from "(34 rows...)" to the reproducible count with a note
that it was re-verified on `<date>`.

**Resolved 2026-09-13 (`ai-sessions/0013_FEATURE_RESULT_2026_09_13.md` Phase 1):** re-ran the exact
cited command a second time (TShark 4.6.8, same version this session used) — still 45 rows, same
time window, only 1 duplicate payload among them. No mechanical explanation found (not a
windowing/de-duplication artifact), so per `PROJECT_RULES.md` §1's own re-run-beats-prior-claim rule,
`CAP-036-FINDINGS.md` line 234 is corrected in place to "45 rows," with a one-line re-verification
note. This is a mechanical count correction, not a FACT/ADR change — no maintainer sign-off required.

### Item 2 — CAP-037's "34 reconnects" count also doesn't match a raw event re-derivation (36, not 34)

**Evidence:** `CAP-037-FINDINGS.md` lines 155/162 state "34 reconnects... over ~20 minutes."
Independently counting raw `bthci_evt.code==0x03` (Connection Complete) events to the Buds' address
in the current log gives **36** (31 success/`0x00`, 5 Page Timeout/`0x04`). Unlike Item 1, this may
simply be a different counting methodology (e.g., "reconnect" defined as a logical cycle rather than
every individual Connection Complete event, or the 5 failed attempts counted differently) rather
than a re-derivation mismatch — not enough time this pass to trace `CAP-037-FINDINGS.md`'s own exact
counting method line-by-line across its full event timeline. Flagged rather than silently resolved
either way. Does not affect the separately, robustly confirmed 26/26 Get/Notify reliability finding
(0011 Finding 108), which used the same "26" figure both places with no discrepancy.

**What's needed:** confirmation of whether "34 reconnects" in `CAP-037-FINDINGS.md` refers to
something other than a raw Connection-Complete-event count (in which case no correction is needed,
just a note clarifying the definition), or is itself a stale/miscounted figure.

**Resolved 2026-09-13 (`ai-sessions/0013_FEATURE_RESULT_2026_09_13.md` Phase 2):** re-ran the exact
cited command (36 rows: 31 success/`0x00`, 5 Page Timeout/`0x04`, TShark 4.6.8). A real, coherent
counting convention explains the 36-vs-34 gap exactly: 2 of the 5 Page Timeouts are each immediately
(0.07s/4.76s later) followed by a success, and counting each such pair as one logical reconnect
attempt (not two events) while counting the 3 remaining, trailing Page Timeouts individually (none
of them is followed by any further success in this log, consistent with the case lid closing around
06:28:48) gives exactly 34. This is a clarification, not a correction — `CAP-037-FINDINGS.md` §2 now
carries this reconciliation inline, next to the original "34" citation.

## 5. Summary

### Cross-capture pattern synthesis

Two recurring patterns emerged across the 130 findings, worth calling out at this level rather than
leaving buried in 45 separate per-capture sections:

1. **0011's core protocol-decode findings (field mappings, opcodes, byte-level content) were
   essentially flawless.** Every hex payload cited for an actual protocol claim (ANC bitmasks, EQ
   quintets, `qhr` field writes, battery decodes, zigzag-corrected Volume Balance values, SASS/HFP
   content) was independently re-extracted from the raw log this session and matched 0011's quoted
   bytes exactly, dozens of times, with zero exceptions. This is a genuinely different reliability
   profile than `ai-sessions/0007`'s (Gemini's own prior pass, see `ai-sessions/0008`), which had
   roughly half its citations wrong even for core claims.
2. **Where 0011's citations *did* break, they clustered specifically in secondary/background
   observations** — co-occurring unrelated Bluetooth devices, one-off vendor-specific commands,
   procedural timestamps — never in the primary protocol-decode content. Five confirmed
   **contradicted** citations (Findings 5, 8, 91, 94, 95) and one confirmed **overclaim** (Finding
   54) all fall in this category. **CAP-032 is the most concentrated instance: all 3 of its
   findings had a citation problem** (94, 95 wrong frame numbers; 95 additionally cites a
   *different capture's* device address, `7d:0a:16:e6:10:68`, actually belonging to CAP-028's own
   Heart Rate device — apparent cross-capture data contamination, not just an off-by-N error).
   Three other findings (36, 87, 115) initially looked like similar overclaims on a first,
   unscoped re-check, but resolved cleanly once correctly scoped to the right device/connection
   handle or time window — a useful methodological reminder (matching `AGENTS.md` §13's own CLI
   hygiene rule) that this class of "zero traffic" claim needs device/handle scoping to check
   properly, not just a bare time window.
3. **Independently of 0011, this session's own re-derivation surfaced two pre-existing count
   discrepancies in canonical `CAP-NNN-FINDINGS.md` documents** (CAP-036's "34 rows," CAP-037's "34
   reconnects") that don't reproduce when their own cited commands are re-run today. 0011 restated
   both counts without independently re-deriving them (a hex-and-script-rule gap in 0011's own
   methodology), so it didn't have the chance to catch these — this review did, precisely because
   its own rule is to re-run every command rather than trust a restatement. Routed to the
   maintainer (§4) rather than silently "corrected," since the cause (stale analysis vs. tool
   version drift) isn't established.

### Totals

- **Findings processed:** 130 of 130 (all real findings in `0011`; note the 48/49 numbering gap,
  §0 above — `0011`'s own "131" count is off by one).
- **Re-derivation confirmed:** 124 (includes 2 that also carry a flagged secondary count/timestamp
  discrepancy — Findings 93, 107 — recorded as open items, not full contradictions).
- **Re-derivation contradicted:** 5 (Findings 5, 8, 91, 94, 95) — all wrong-citation errors; in
  every case the underlying substantive claim was independently confirmed true and already
  correctly documented elsewhere in the project's canonical files.
- **Overclaimed:** 1 (Finding 54 — "zero frames" was too absolute; canonical doc's own phrasing was
  already accurate).
- **Unresolvable/count-unverifiable (routed to maintainer):** 2 (Findings 106, 107 — pre-existing
  document count discrepancies, not new errors from 0011).
- **Applied (mechanical fix to a project document):** 1 (Finding 6 — added a frame-number
  translation note to `CAP-002-FINDINGS.md`, per the `0012` prompt's own pre-authorization for this
  exact class of correction).
- **Documents touched:** `CAP-002-FINDINGS.md` (1 edit, non-destructive addition, no FACT/ADR
  content touched).
- **FACT promotions or `DECISIONS.md` ADRs made or altered:** 0 (per this session's hard limit —
  all FACT-promotion-adjacent findings routed to §4 instead).

### For the maintainer

`0011`'s review holds up well on independent, full re-derivation — its 111-claimed-confirmations/20-
minor-discrepancies self-assessment is, if anything, slightly optimistic in the wrong direction: most
of what it labeled "discrepancy (minor)" were genuine, already-correctly-handled procedural notes,
while the citation errors this session actually found (5 contradicted, 1 overclaimed) were mostly not
flagged by 0011 as anything unusual at all — they read as confident, ordinary findings. **Recommendation:
treat 0011 as reliable for its core protocol-decode content (safe to cite without re-verification for
that category specifically), but not for secondary/background-device citations or restated counts
from other documents** — those categories should still be spot-checked before being trusted, per this
session's own findings above. See §4 for the two specific items needing a maintainer decision.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0012_CROSSCHECK_RESULT_2026_09_12.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0012_CROSSCHECK_RESULT_2026_09_12
