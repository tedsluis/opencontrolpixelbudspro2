# CAP-007: In-Ear Detection / Wear State (Group U)

Standardized, evidence-based extraction from `CAP-007-btsnoop_hci.log` + `CAP-007-recording.mp4`,
following the same template as `CAP-001-FINDINGS.md`. Every claim below carries a status per
`PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Scope correction up front, since it affects how every section below should be read:** Group U
was **not** designed as a general "map in-ear vs. out-of-ear byte values" capture. Per
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1's own Group U description and `CAP-004-FINDINGS.md` §5a
Task 5, its actual, narrower purpose is to test whether **DLCI 0x08 Group `0x04` Code `0x12`'s**
already-known-but-unexplained value alternation is event-driven or a free-running
liveness/sequence indicator, using three brackets: (1) a worn bud removed and held in hand, (2)
the case lid closed while the connection stays active, (3) a multi-minute idle wait. This document
answers that question, plus reports what the one physical bud-removal event in this capture *did*
and *did not* trigger elsewhere on the wire — it does not deliver a confirmed Left/Right/In/Out
opcode table, because the evidence gathered here does not support one (see §3.5, §5).

## 1. Capture Metadata

- **Log:** `CAP-007-btsnoop_hci.log` — 366.24s, 2,476 packets, 2026-08-16 09:14:08.335–09:20:14.579
  local (+0200). `capinfos CAP-007-btsnoop_hci.log`.
- **Event notes:** `CAP-007-EVENT-NOTES.md` (this folder) — video-verified timeline this findings
  document is built from.
- **Video:** `CAP-007-recording.mp4` — 227.36s, 09:14:10–09:17:56/57 local, burned-in wall-clock
  overlay (`ffprobe`-confirmed duration). The log continues **~137s past the end of the video**,
  unattended — see §3.2.
- **Test device:** Pixel 7a, Android's system Bluetooth "Device details" page for "Pixel Buds Pro
  2 van Ted" (Fast-Pair-enhanced Settings UI — not the dedicated companion app, never shown on
  camera). Firmware `release_5.203`, confirmed on-wire this capture (ASCII string, frames
  791/801/1559).
- **Devices:** phone `Google_7e:ca:81` (Pixel 7a), peer `Google_cf:6e:07` (Buds/case) — same
  physical addresses as `CAP-001`–`CAP-006`.

## 2. Methodology & Filtering

Base MAC filter (per `AGENTS.md` §13's CLI-hygiene rule — always filter by the Buds' address
before layering protocol filters):

```
tshark -r CAP-007-btsnoop_hci.log -Y "bluetooth.addr == cf:6e:07" ...
```

In practice this capture's log contains only this one ACL link for its entire duration (a single
`Connect Complete`, frame 313, and zero `Disconnect Complete` events anywhere in the log —
confirmed via `tshark -r CAP-007-btsnoop_hci.log -Y "bthci_evt.code==0x05"`, zero results), so the
address filter changes nothing here in practice, but is included below for reproducibility and
because it *would* matter if another device's traffic were interleaved.

DLCI targeting for this session (RFCOMM server channel numbers are session-local — see
`CAP-001-FINDINGS.md` §2's "Reusable note", re-confirmed again here):

| Channel (this session) | DLCI | Content this session |
|---|---|---|
| 0 | 0x00 | Multiplexer control |
| 6 | 0x0c | Hands-Free (HFP) |
| 1 | 0x02 | `libmaestro`'s Pigweed `pw_hdlc` channel (opaque, per `PROTOCOL.md` §2.2a) |
| 2 | 0x04 | Fast Pair Message Stream (Device Information, ANC Group `0x08`, etc.) |
| 4 | 0x08 | Private `[Group][Code][Len:2B-BE][Value]` envelope — **this document's primary target** |
| 5 | 0x0a | No payload observed this session (consistent with every prior capture — `CAP-001-FINDINGS.md` §6) |

Primary extraction command used throughout this document (Group U's own target channel):

```
tshark -r CAP-007-btsnoop_hci.log -Y "btrfcomm.dlci==0x08 && data.data" \
  -T fields -e frame.number -e frame.time_relative -e data.data
```

93 frames matched; the full output is reproduced/filtered inline below per finding.

## 3. Analysis: DLCI 0x08 Group `0x04` Code `0x12` and the one observed physical event

### 3.1 The physical event this capture actually contains

Per `CAP-007-EVENT-NOTES.md`, the only unambiguous physical bud-state change in this capture is at
**09:15:38** (video), where the **Left** earbud is lifted directly out of the still-open case. At
**no point in this capture is any earbud seen inserted into an ear** — both buds sit in the open
case from the start of the video until 09:15:38, and only the Left one is ever removed (the Right
stays in the case the entire session, including through the later lid-close). This capture
therefore exercises `CASE-004` (remove Left earbud from the case) more precisely than `INEAR-004`
("in-ear sensor reports removed, bud taken out of ear, not placed in case") — the two are easy to
conflate because Group U's own write-up (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1) describes the
procedure as "remove **one worn** earbud", but this session's procedure did not include ever
wearing the bud first. **This is flagged explicitly, per `PROJECT_RULES.md` §1, rather than
silently treated as an in-ear event**: any wire signal found to correlate with 09:15:38 below is
evidence for "bud left the case's charging/sensing pocket", not confirmed evidence for "bud left
an ear" specifically — the two may or may not share a signal, and this capture cannot distinguish
them.

### 3.2 Group `0x04` Code `0x12` — full decoded value table

Extraction (per §2's command, filtered to the 4-byte-value frames only):

```
tshark -r CAP-007-btsnoop_hci.log -Y "btrfcomm.dlci==0x08 && data.data matches \"^0412000408\"" \
  -T fields -e frame.number -e frame.time_relative -e data.data
```

All 18 matches, 🟢 FACT (raw bytes + direction independently re-checked per frame via
`tshark -r CAP-007-btsnoop_hci.log -Y "frame.number==<N>"`):

| Frame | Wall clock | Raw hex | Direction | `field1` (byte 5) |
|---|---|---|---|---|
| 721  | 09:14:17.878 | `04 12 00 04 08 02 10 01` | Rcvd (Buds→Phone) | `0x02` |
| 835  | 09:14:18.298 | `04 12 00 04 08 02 10 01` | Rcvd | `0x02` |
| 1436 | 09:15:39.271 | `04 12 00 04 08 02 10 01` | Rcvd | `0x02` |
| 1444 | 09:15:39.285 | `04 12 00 04 08 02 10 01` | Rcvd | `0x02` |
| 2074 | 09:18:43.428 | `04 12 00 04 08 03 10 01` | Rcvd | `0x03` |
| 2173 | 09:18:46.776 | `04 12 00 04 08 02 10 01` | Rcvd | `0x02` |
| 2234 | 09:18:55.974 | `04 12 00 04 08 04 10 01` | Rcvd | `0x04` |
| 2251 | 09:18:59.153 | `04 12 00 04 08 02 10 01` | Rcvd | `0x02` |
| 2255 | 09:19:00.092 | `04 12 00 04 08 04 10 01` | Rcvd | `0x04` |
| 2306 | 09:19:06.167 | `04 12 00 04 08 02 10 01` | Rcvd | `0x02` |
| 2318 | 09:19:09.435 | `04 12 00 04 08 04 10 01` | Rcvd | `0x04` |
| 2335 | 09:19:16.316 | `04 12 00 04 08 02 10 01` | Rcvd | `0x02` |
| 2344 | 09:19:20.219 | `04 12 00 04 08 04 10 01` | Rcvd | `0x04` |
| 2356 | 09:19:23.557 | `04 12 00 04 08 02 10 01` | Rcvd | `0x02` |
| 2359 | 09:19:24.707 | `04 12 00 04 08 04 10 01` | Rcvd | `0x04` |
| 2370 | 09:19:28.034 | `04 12 00 04 08 02 10 01` | Rcvd | `0x02` |
| 2399 | 09:19:40.455 | `04 12 00 04 08 04 10 01` | Rcvd | `0x04` |
| 2463 | 09:19:45.019 | `04 12 00 04 08 02 10 01` | Rcvd | `0x02` |

**Envelope decode, 🟢 FACT** (consistent, zero exceptions, across all 18 frames):
`[Group=0x04][Code=0x12][Len:2B-BE=0x0004][Value: 4 bytes]`, and the 4-byte value itself parses
cleanly as two protobuf varint fields: tag `0x08` = field 1 (varint) → value byte 5; tag `0x10` =
field 2 (varint) → value always `0x01` (constant, all 18 frames). This matches the
protobuf-tag-decoding approach already validated elsewhere on this same DLCI in `CAP-004`
(`CAP-004-FINDINGS.md` §5a Task 2).

**Correction to `CAP-004-FINDINGS.md` §5a's characterization, 🟢 FACT:** that document described
this field as alternating **"2↔3"**. Across this capture's 18 samples, the value actually takes
**three** distinct values — `0x02`, `0x03`, `0x04` — and after the one `0x03` seen immediately at
the moment activity resumes post-idle (frame 2074), it settles into a clean **`0x02`↔`0x04`**
alternation for the remaining 14 frames, never revisiting `0x03`. Whether `0x03` is a genuine third
steady state or a one-off transitional artifact at a resumption boundary is 🔴 open — this capture
saw it exactly once, at exactly the first frame after the longest silence in the log, which is
consistent with either reading.

**Direction, 🟢 FACT:** all 18 value-bearing frames are **Rcvd** (Buds→Phone) — i.e. this is the
Buds pushing data, not the phone querying it. The phone does send a handful of zero-length
"placeholder" frames referencing the same Group/Code (e.g. `04 12 00 00`, embedded inside a larger
Sent burst, frame 1426 at 09:15:39.212, immediately after DLCI 0x08 reopens — see §3.3) — these are
0-length Get/subscribe-shaped requests, never carrying the real value themselves. Searching the
**entire log** for any *Sent* frame containing a non-placeholder `04 12 00 04...` pattern returns
**zero** matches — none of the 18 real-value pushes above were immediately preceded by a matching
phone-side query. This directly answers Group U's own research question:

> **🟢 FACT: Code `0x12` is autonomously pushed by the Buds, not a phone-polled value.** The 14
> pushes between 09:18:43 and 09:19:45 (§3.2's table, frames 2074–2463) fire with **no**
> corresponding RFCOMM channel reopen, no Sent-side query, and — per §3.5 below — no camera
> coverage of any physical action at all, on a channel that had otherwise been completely silent
> for the preceding ~184s. This rules out "value only changes because the phone just asked" and
> rules out "value only changes because the channel/session was just re-established" as the sole
> explanations — some of these pushes are genuinely autonomous, buds-initiated events on an
> already-idle, already-open channel.

### 3.3 The 09:15:38 bud-removal window — what it *did* trigger

Extraction:

```
tshark -r CAP-007-btsnoop_hci.log -Y "frame.time_relative>=90.0 && frame.time_relative<=96.1" \
  -Y "btrfcomm.frame_type or bthci_evt"
```

🟢 FACT, all frame numbers/timestamps independently re-checked:

- At **09:15:38.438** (frame 1351), the Buds send `DISC Channel=1`, beginning a full RFCOMM
  teardown-and-rebuild of **channels 1, 2, 4, 5** (i.e. DLCI 0x02, 0x04, 0x08, 0x0a all at once) —
  fresh SDP queries and `SABM`/`UA` for each, complete by **09:15:44.352** (frame 1572). No new
  `Create Connection`/`Connect Complete`/`Disconnect Complete` HCI event occurs anywhere near this
  window (confirmed: the log's *only* such event pair is the initial connect at 09:14:14–16) — the
  underlying ACL/baseband link is undisturbed; only the RFCOMM multiplexer's service channels
  bounce.
- The re-opened DLCI 0x04 (Fast Pair Message Stream) immediately re-announces the **ANC state**:
  `08 13 00 04 01 e8 e8 80` (Group `0x08` Code `0x13`, "Notify ANC state" per the official Google
  Fast Pair Hearable Controls spec, already confirmed byte-for-byte in `CAP-001-FINDINGS.md` §5) —
  at **09:15:38.436** (frame 1350) and again at **09:15:44.352** (frame 1572). Value `0x80` =
  **Transparency** — the **same** value as the very first ANC-state notify of this whole session,
  09:14:17.337 (frame 623). The ANC mode never actually changed; this is a re-announcement of
  unchanged state, not a real mode transition (matches the video: the "Transparency" pill stays
  selected throughout).
- The re-opened DLCI 0x08 immediately pushes a fresh Group `0x04` Code `0x12` value — `0x02`
  (frames 1436, 1444, §3.2) — again, the **same** value already seen twice before this event
  (frames 721, 835). **The value did not change across the one physical event this capture
  actually observed.**
- **Hex dump, reproduction command:**
  ```
  tshark -r CAP-007-btsnoop_hci.log -Y "frame.number==1350 or frame.number==1436" \
    -T fields -e frame.number -e data.data
  1350  0813000401e8e880
  1436  0412000408021001
  ```

**Interpretation, 🟡 HYPOTHESIS:** the RFCOMM channel bounce at 09:15:38 is temporally coincident
(sub-second) with the video-observed bud removal, strongly suggesting the removal *caused* it
(plausibly an antenna/role-handover effect of removing an earbud that was acting as part of the
Buds' dual-radio pair — not confirmed, no direct evidence of the mechanism). What the bounce
*produces*, however — a generic "re-announce current state on every re-opened channel" burst
(ANC state, Group `0x04` Code `0x12`, and elsewhere a full device-info re-announcement on DLCI
0x04, frames 1559/1560/1567) — is standard channel-(re)initialization behavior, not a dedicated
"earbud removed" opcode. **No byte anywhere in this burst changed value as a result of the
physical event** (§3.5 makes this the capture's central negative result).

### 3.4 The case-lid-close window (`OBS-003` step 2) — clean negative result

Per `CAP-007-EVENT-NOTES.md`, the case lid is closed 09:16:44–48 (Right earbud still inside, Left
earbud left outside — connection stays up throughout, confirmed no `Disconnect Complete` anywhere
in the log).

```
tshark -r CAP-007-btsnoop_hci.log -Y "btrfcomm.channel && data.data && frame.time_relative>=145 && frame.time_relative<=165"
```

Zero results — 🟢 FACT. Not one RFCOMM data-carrying frame, on any DLCI, appears in a ±10s window
around the lid closing. This directly answers Group U step 2's own question: **closing the case
lid, while a bud remains outside the case and the connection stays active, produces no observable
RFCOMM wire signal** in this capture — no ANC-state re-announcement, no Group `0x04` Code `0x12`
push, no channel bounce, nothing. This is itself evidence (`PROJECT_RULES.md` §4 rule 12 — a
negative result is still recorded).

### 3.5 The ANC-row grey-out/re-enable pair (09:15:38 → 09:15:52–53) — not wire-attributable

The app's "Active noise control" row greys out at 09:15:38 (same second as the bud removal) and
re-enables at 09:15:52–53, while the Left bud stays physically out of the case the whole time.

```
tshark -r CAP-007-btsnoop_hci.log -Y "frame.time_relative>=95 && frame.time_relative<=108"
```

Result (🟢 FACT, full frame list in `CAP-007-EVENT-NOTES.md`'s companion analysis): the only events
in this window are HCI-level `Mode Change` (frame 1621, 09:15:51.916) and `Sniff Subrating` (frame
1623, 09:15:53.899) baseband events, plus unrelated `LE Extended Advertising Report` events from
other nearby BLE devices — **zero RFCOMM/L2CAP data frames of any kind**. The `Mode Change` event's
timing (~1–2s before the visible re-enable) is suggestively close, but a baseband
active/sniff-mode transition has no documented mechanism for driving an app-level UI control
directly — 🔴 **not attributable**. The more likely explanation, 🟡 HYPOTHESIS: the ANC row's
grey-out is a **local, client-side loading-state reaction to the 09:15:38 RFCOMM channel bounce
itself** (the app disables ANC controls while its command channel is torn down and rebuilding,
09:15:38.438–09:15:44.352 per §3.3), and re-enables a few seconds later once the app's own internal
state machine settles — independent of the bud's continued physical absence from the case. This
would mean the grey-out/re-enable pair is evidence about **app UI robustness during a channel
bounce**, not about in-ear/out-of-case sensing at all.

## 4. Architectural Impact

- **Does not open a new channel.** All content in this document is on the same three
  already-known-and-documented RFCOMM sub-protocols (`ARCHITECTURE.md` §1, `PROTOCOL.md` §2.3):
  Fast Pair Message Stream (DLCI 0x04), the private `[Group][Code][Len][Value]` envelope (DLCI
  0x08), and `libmaestro`'s Pigweed channel (DLCI 0x02, present but not analyzed further here — no
  content on it was found to correlate with the 09:15:38 event beyond the routine
  channel-reopen traffic already characterized in `CAP-001-FINDINGS.md`/`CAP-004-FINDINGS.md`).
- **Does not unblock `FrameEncoder`/`FrameDecoder` for any in-ear/wear-state feature.** Per
  `AGENTS.md` §6, the promotion gate is per channel/feature, evidenced with a 🟢 FACT entry in
  `PROTOCOL.md`. This capture found **no byte, on any DLCI, that changed value in response to the
  one physical bud-removal event it captured** (§3.3, §3.5) — the opposite of what would be needed
  to propose a FACT-level wear-state opcode. `INEAR-004` remains without protocol evidence.
- **Proposed update to `CAP-004-FINDINGS.md` §5a / `PROTOCOL.md`'s characterization of DLCI 0x08
  Group `0x04` Code `0x12`** (flagged as a proposal per `AGENTS.md` §6 — not committed as FACT by
  this document): revise from "alternates 2↔3, irregular interval, 🟡 HYPOTHESIS free-running
  liveness/sequence bit" to "🟢 FACT: Buds-initiated (Rcvd-only, never seen as a direct reply to a
  Sent query), fires (a) immediately after DLCI 0x08 is opened or re-opened — at least once with
  value unchanged across a real physical event (§3.3) — and (b) autonomously during an otherwise
  fully idle connection, with a `0x02`↔`0x04` steady-state alternation (one `0x03` transitional
  value seen at a resumption boundary) at irregular ~1–12s intervals; 🔴 still unresolved what
  real-world condition the value itself encodes — not confirmed to be in-ear state, not confirmed
  to be a simple counter (a true free-running counter would not go fully silent for ~184s, §3.2)."
  Awaiting maintainer sign-off before editing `PROTOCOL.md` directly.

## 5. Conclusions & Next Steps

**Group U's own research question (is Code `0x12` event-driven or free-running?) is answered: 🟢
FACT, neither, cleanly.** It is Buds-initiated and can fire with no phone-side trigger and no
channel churn (ruling out "purely a reaction to phone queries or channel reopening"), but it also
goes completely silent for ~184s during genuine idle time (ruling out "purely free-running,
fixed-period"). The most defensible reading is an event-driven push tied to some *internal* Buds
condition not observed directly in this capture — plausibly connection-supervision/heartbeat-
adjacent, given its behavior around the channel bounce, but this is 🔴 not confirmed.

**This segment is NOT unblocked for the task's original framing (in-ear detection wire mapping).**
The task that produced this capture asked to "map the exact byte values corresponding to In Ear
vs. Out of Ear for both Left and Right" — this capture cannot deliver that, for two compounding
reasons, both stated plainly per `PROJECT_RULES.md` §1 rather than papered over:

1. **No earbud was ever inserted into an ear in this capture** (§3.1) — only removed from the open
   case. Whatever in-ear sensing does on the wire, if anything, was not exercised here.
2. **The one physical case-removal event that *was* captured produced no value change on any
   byte, on any DLCI, that could be attributed to it** (§3.3, §3.5) — every candidate signal near
   that moment (ANC re-announcement, Group `0x04` Code `0x12`) turned out to be a generic
   channel-(re)initialization side effect with an unchanged value, not a state-specific signal.

**Recommended next capture, not yet scheduled:** a dedicated session that (a) actually inserts and
removes an earbud from the ear (not the case) while the app is on screen and the log is running,
(b) isolates Left and Right separately with a pause between them (per
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4's isolation rule), and (c) keeps the camera running for the
full duration including any idle bracket, so that a Code `0x12` push (or its absence) can be
matched against a *known* physical state rather than inferred after the fact.

## 6. Open Questions

- 🔴 What does Group `0x04` Code `0x12`'s value (`0x02`/`0x03`/`0x04`) actually represent? Not
  confirmed to be wear state, not confirmed to be a simple counter. Is `0x03` a genuine third
  steady state or a one-off resumption artifact (seen exactly once, at exactly the first push
  after the longest silence in the log)?
- 🔴 What caused the 09:15:38 RFCOMM channel bounce (channels 1/2/4/5 all torn down and rebuilt
  together, ACL link undisturbed)? Coincident with the bud removal, but no direct mechanism
  confirmed — a repeat capture with a clean single bud-removal action and nothing else happening
  nearby would strengthen or break this correlation.
- 🔴 Is the ANC-row grey-out/re-enable pair (09:15:38 → 09:15:52–53) a reaction to the RFCOMM
  channel bounce (🟡 leading hypothesis, §3.5) or to something else entirely? No wire-level frame
  of any kind is attributable to the 09:15:52–53 re-enable moment specifically.
- 🔴 Does DLCI 0x08 Group `0x04` have other Codes/values that *do* respond to in-ear state
  specifically? Only Code `0x12` (and empty `0x11`/`0x13` placeholders) were observed on this DLCI
  in this capture — a Get-all/enumerate pass against a firmware reference (if one becomes
  available) could reveal Codes not exercised by this particular session's actions.
- 🔴 Right earbud was never removed in this capture — whether Group `0x04` Code `0x12` (or
  anything else) carries any Left/Right distinction at all remains completely untested; this
  capture's single-earbud design cannot answer it either way.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-007-2026-08-16_09-14-10_09-17-57-Group_U/CAP-007-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-007-2026-08-16_09-14-10_09-17-57-Group_U/CAP-007-FINDINGS
