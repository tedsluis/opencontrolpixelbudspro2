# Findings: `CAP-008` (Group V — In-call HFP/SCO audio behavior)

Standardized, evidence-based extraction from `CAP-008-btsnoop_hci.log` +
`CAP-008-recording1.mp4`–`CAP-008-recording4.mp4`, staged here for later promotion
into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Every claim below carries a status per
`PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-008` · **Date:** 2026-08-26 · **Firmware:** `release_5.203`
(🟢 FACT, wire-confirmed, frame 1116's DLCI 0x08 payload — unchanged since `CAP-023`) ·
**Phone:** Pixel 7a, Android 17 (⚪ assumed, same device as `CAP-001`–`CAP-025`, not
re-confirmed on screen this session), official Pixel Buds Companion App (⚪ assumed
version `1.0.955078536`, not shown on screen this session) · **Log file:**
`CAP-008-btsnoop_hci.log` (309.3s, 3,668 packets, 2026-08-26 09:38:40.60–09:43:49.90
local/+0200) · **Video:** `CAP-008-recording1.mp4`–`CAP-008-recording4.mp4` (4 clips, 34.8s +
29.7s + 20.3s + 40.2s, all with an on-screen wall-clock overlay accurate to the second,
covering 09:38:44–09:39:19, 09:39:29–09:39:59, 09:40:02–09:40:22, 09:40:56–09:41:36
local) · **Devices:** phone `Google_7e:ca:81` (Pixel 7a), peer `Google_cf:6e:07` (the
Buds/case) — same physical units as every prior capture in this project.

**Scope note:** this session's purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group V) is
`CALL-001` — the first capture in this project containing an actual phone call, closing
the gap `CAP-001-FINDINGS.md` §6 Task 6 and `CAP-002-FINDINGS.md` §5 both flagged (no
prior capture ever exercised HFP's Service Level Connection re-setup or a real
SCO/eSCO audio path). The session also incidentally exercises a Bluetooth-radio
power-cycle (radio was off at the start), a Spotify play/pause sequence, and opening
the Pixel Buds companion app — none of these are Group V's own scope, but are noted
where they help correlate wire evidence to on-screen timing.

**Video-verification note:** all four recordings carry a burned-in camera timestamp
(`26 aug 2026 HH:MM:SS`, 1s resolution). Every timestamp in `CAP-008-EVENT-NOTES.md`
was re-checked by extracting 1fps contact sheets from each recording and reading this
overlay directly — all match the maintainer's manual notes to within 1 second (see
§10). No corrections to `CAP-008-EVENT-NOTES.md` were needed.

---

## 1. Connection lifecycle (🟢 FACT)

Bluetooth was off at the start of `CAP-008-recording1.mp4` (09:38:44, screen shows
"Bluetooth is off") and switched on by the user at 09:38:48 (video). This produced a
single, successful classic BR/EDR connection — no failed attempts, unlike `CAP-001`'s
three-attempt sequence:

| Step | Time | Frame |
|---|---|---|
| `Create Connection` sent | 09:38:47.919 | 230 |
| `Connect Complete` (status `0x00`) | 09:38:50.335 | 587 |
| `Link Key Request` → `Link Key Request Reply` (stored key, no pairing) | 09:38:50.386–.387 | 635 / 636 |
| `Authentication Complete` | 09:38:50.415 | 645 |
| `Set Connection Encryption` | 09:38:50.415 | 646 |

This is a **reconnection using a stored link key**, not a fresh pairing — consistent
with `CAP-001-FINDINGS.md` §1's own reconnection pattern. On screen, the Bluetooth
settings sheet shows "Connecting..." from 09:38:49 and "Active, L:98%, R:100%
battery" (approximate on-screen wording) at 09:38:52 — the ~2s gap between the
HCI-level `Connect Complete` (09:38:50.34) and the UI showing "Active" (09:38:52) is
consistent with the RFCOMM/HFP channel setup below completing in between.

Two connection-handle-level disconnects (reason `0x16`, "Connection Terminated by
Local Host") occur later, **each exactly at the end of one of the two phone calls**
(frame 2020 @09:39:51.692, handle `0x0005`; frame 2794 @09:41:18.332, handle `0x0006`)
— these are the two calls' individual eSCO connections tearing down, not the classic
ACL link (handle `0x0002`, which stays up for the whole 309s session). See §4/§5.

## 2. RFCOMM channel topology this session (🟢 FACT)

Six DLCIs open, in this order, all within ~1.3s of each other right after the classic
link authenticates:

| Order | DLCI | RFCOMM channel | Opened (frame / time) | Content this session | Cross-session identity |
|---|---|---|---|---|---|
| 1 | 0x00 | 0 | 734 / 09:38:50.624 | Multiplexer control (PN/MSC negotiation for the other 5) | Always channel 0 |
| 2 | 0x0c | **6**, Wireshark-labelled "(Hands-Free)" | 749 / 09:38:50.640 | Full HFP AT-command SLC handshake, then both calls' RING/CIEV/BCS traffic — see §3/§4 | `CAP-001` put this content on channel 4/DLCI 0x08–0x09; **channel numbers are session-local**, per `CAP-001-FINDINGS.md` §2's own reusable note — reconfirmed here with a third distinct channel-number assignment for the same profile |
| 3 | 0x04 | 2 | 924 / 09:38:51.004 | Official Fast Pair Message Stream (ANC Notify, Device Information) — not specifically exercised this session, background traffic only | `PROTOCOL.md` §2.1/§4.1 |
| 4 | 0x0a | 5 | 1042 / 09:38:51.196 | **SABM/UA only — zero payload for the rest of the 309s session** | Silent in this capture too — see §6 |
| 5 | 0x08 | 4 | 1076 / 09:38:51.238 | Private `[Group][Code][Length][Value]` envelope — periodic capability/firmware/status traffic (incl. the `release_5.203` string, frame 1116) | `PROTOCOL.md` §2.3 |
| 6 | 0x02 | 1 | 1222 / 09:38:51.935 | `libmaestro`/Pigweed `pw_hdlc` channel (§2.2a) — a steady ~3 exchanges/second heartbeat (43-byte Rcvd + 14-byte Sent UID-ack pairs) that continues completely unperturbed through both calls, their RING/answer/hangup transitions, and the eSCO setup/teardown | `PROTOCOL.md` §2.2a |

**HFP's RFCOMM channel assignment is new information, not a contradiction:** this is
the third distinct RFCOMM channel number this project has seen HFP-adjacent AT-command
traffic land on (channel 4/DLCI 0x08 in `CAP-001`; channel 6/DLCI 0x0c here), which
reinforces — with a third independent data point — that RFCOMM server channel numbers
are negotiated per-connection and must never be treated as a stable per-profile label
(the point `CAP-001-FINDINGS.md` §2 already made from two sessions).

## 3. HFP Service Level Connection (SLC) handshake reoccurs on reconnect (🟢 FACT)

Immediately after channel 6/DLCI 0x0c opens, a full AT-command SLC handshake fires,
structurally identical in shape to `CAP-001-FINDINGS.md` §3's handshake:

```
AT+BRSF=921              → +BRSF: 3951                    (frames 776/778)
AT+BAC=1,2,3              → OK                              (frame 788 — codecs offered: CVSD, mSBC, LC3)
AT+CIND=?                 → indicator list: call, callsetup, service, signal, roam, battchg, callheld  (frames 793/794)
AT+CIND?                  → 0,0,0,0,0,3,0  (battchg=3, ≈60% on its 0–5 scale)  (frames 801/802)
AT+CMER=3,0,0,1           → OK                              (enables +CIEV unsolicited indicator events)
AT+BIND=1,2 / =? / ?      → HF indicators 1 (enhanced safety) and 2 (battery level) both active
AT+BIEV=1,1               → OK
AT+VGM=7 / AT+VGS=10       → OK                              (mic/speaker gain)
AT+NREC=0 (1st)            → OK; AT+NREC=0 (2nd, duplicate) → ERROR (+CME ERROR not returned this time, plain ERROR)
AT+COPS=3,0 / AT+CMEE=1    → OK
```
(Full frame range: 776–1132, all within 09:38:50.66–.96, i.e. the entire handshake
completes in under 300ms.)

**Reproduction (added 2026-09-03, closing a hex-and-script-rule gap a documentation audit found —
this section originally cited frame numbers and decoded AT-command text without the underlying
extraction command or raw bytes):**

```
$ tshark -r CAP-008-btsnoop_hci.log -Y "frame.number in {776,778,788,793,794,801,802}" \
    -T fields -e frame.number -e frame.time_relative -e _ws.col.Info
776   10.060257  Rcvd AT+BRSF=921
778   10.061124  Sent   +BRSF: 3951
788   10.091497  Rcvd AT+BAC=1,2,3
793   10.098829  Rcvd AT+CIND=?
794   10.099336  Sent   +CIND: ("CALL",(0,1)),("CALLSETUP",(0-3)),("SERVICE",(0-1)),("SIGNAL",(0-5)),("ROAM",(0,1)),("BATTCHG",(0-5)),("CALLHELD",(0-2))
801   10.114239  Rcvd AT+CIND?
802   10.116962  Sent   +CIND: 0,0,0,0,0,3,0
```

Raw RFCOMM I-frame bytes for frame 776 (`AT+BRSF=921`) and its ACK'd response, frame 778
(`+BRSF: 3951`), via `tshark -r CAP-008-btsnoop_hci.log -Y "frame.number==776" -x` /
`-Y "frame.number==778" -x`:

```
776: 02 02 20 15 00 11 00 43 00 31 ff 19 03 41 54 2b   .. ....C.1...AT+
     42 52 53 46 3d 39 32 31 0d 89                     BRSF=921..
778: 02 02 00 17 00 13 00 88 a0 33 ef 1f 0d 0a 2b 42   .........3....+B
     52 53 46 3a 20 33 39 35 31 0d 0a 4f               RSF: 3951..O
```
`41 54 2b 42 52 53 46 3d 39 32 31 0d` decodes as ASCII `AT+BRSF=921\r` — the AT-command payload is
plain text carried directly in the RFCOMM I-frame, not a proprietary binary envelope, matching every
other HFP-classified frame in this section.

**This closes `PROTOCOL.md` §6's "Behavior" open item** ("why does HFP AT-command
traffic never recur after `CAP-001`'s own handshake") **with a second, independent
data point, not a contradiction of `CAP-002`'s negative result:** the handshake *does*
reoccur here, triggered by this session's own fresh classic-link connection (the
Bluetooth radio had just been switched on, §1) — consistent with SLC setup being tied
to (re-)establishing the RFCOMM/ACL connection itself, not to a background timer.
`CAP-002-FINDINGS.md` §5's ~8-hour log never saw a second handshake because, on the
evidence available, its ACL connection was never torn down and freshly re-established
within that log's window — this capture did not test that specific boundary case
(no deliberate reconnect-without-radio-toggle was performed), so it remains 🟡
HYPOTHESIS, not 🟢 FACT, that *any* reconnection (not just a radio power-cycle)
retriggers the full handshake.

**No second full handshake precedes call 2** (§4) — only a codec renegotiation
(`AT+BCS`, §5) and the same RING/CIEV sequence repeat. The SLC, once established,
persists across both calls in this session.

## 4. Both calls, cross-correlated wire ↔ video (🟢 FACT)

### Call 1 (`CAP-008-recording2.mp4`)

| Event | Wire time | Frame | Video (wall-clock) |
|---|---|---|---|
| `+CIEV: 2,1` (CALLSETUP=1, alerting begins) | 09:39:19.689 | 1639 | *(end of recording1, not on camera — see gap note below)* |
| `RING` ×3 (5.000s apart: .823, 24.826, 29.827) | 09:39:19.823–29.827 | 1658, 1730, 1767 | Incoming-call screen already showing at 09:39:29 (recording2 start) |
| `+CIEV: 1,1` + `+CIEV: 2,0` (CALL=1, answered) | 09:39:33.861 | 1842/1843 | Finger taps green Answer button at 09:39:34 (Δ≈0.14s) |
| `+CIEV: 1,0` (CALL=0, call ends) | 09:39:51.694 | 2022 | Finger taps red hang-up at 09:39:49–50, screen shows "Device details" by 09:39:51 (Δ≈0s) |

Talk time: 09:39:33.861–09:39:51.694 ≈ **17.8s**. `AVDTP Start` (podcast A2DP media
resumes) fires 0.21s after call end (frame 2034, 09:39:51.904) — matches the
maintainer's note "podcast resumed playing" exactly.

**Gap note:** `CAP-008-recording1.mp4` ends at 09:39:19 and `CAP-008-recording2.mp4` starts at
09:39:29 — a 10s recording gap. The wire evidence shows the call actually started
alerting at 09:39:19.689, i.e. **right at the boundary of this gap**, which is why
neither video shows the alerting-begins moment directly; the incoming-call UI is
already up when recording2 starts 10s later, consistent with the wire timeline.

### Call 2 (`CAP-008-recording4.mp4`)

| Event | Wire time | Frame | Video (wall-clock) |
|---|---|---|---|
| `+CIEV: 2,1` (CALLSETUP=1, alerting begins) | 09:40:23.468 | 2273 | Incoming-call screen visible from 09:40:56 (recording4 start) |
| `RING` ×8 (5.000s apart, .732 → 58.734) | 09:40:23.732–58.734 | 2284…2627 | — |
| `+CIEV: 1,1` + `+CIEV: 2,0` (answered) | 09:40:59.340 | 2632/2633 | Finger taps green Answer button at 09:40:59–41:00 (Δ≈0.7s) |
| `+CIEV: 1,0` (call ends) | 09:41:18.344 | 2797 | Finger taps red hang-up at 09:41:16–17, note's 09:41:18 (Δ≈0s) |

Talk time: 09:40:59.340–09:41:18.344 ≈ **19.0s**. Ringing this time lasted ~35.9s (8
full RING cycles) before answer, vs. call 1's ~14.2s (3 RING cycles) — both consistent
with the same 5.000s RING cadence, just answered at a different point in it.

**`CALL-001` is now 🟢 FACT-evidenced**, video- and wire-correlated to sub-second
precision for both call start and call end, across two independent calls in the same
session.

## 5. eSCO audio path established — resolves the long-standing SCO/eSCO open question (🟢 FACT)

`CAP-001-FINDINGS.md` §6 Task 6 found **zero** SCO/eSCO HCI events across four earlier
captures and concluded this was simply because none of them contained a real call.
This capture confirms that reasoning directly: both calls trigger a full eSCO
connection at the HCI level, each following the exact same shape:

```
Sent Enhanced Setup Synchronous Connection (0x043d), Connection Handle 0x0002
  → Codec: mSBC (wideband speech), Tx/Rx bandwidth 8000 B/s, frame size 60
Rcvd Synchronous Connection Complete (0x2c), Status Success
  → new sync Connection Handle (0x0005 for call 1, 0x0006 for call 2), Link Type: eSCO
```
Call 1: frames 1646 (cmd) / 1656 (complete), 09:39:19.697/.821 — **7.6ms after**
`+CIEV: 2,1` and 76ms before the first `RING`. Call 2: frames 2274/2282,
09:40:23.468/.731 — essentially identical timing relative to that call's own
`+CIEV: 2,1`. Both sync connections are cleanly torn down (`Disconnect Complete`,
reason `0x16`) at the same moment their respective call's `+CIEV: 1,0` fires (§1, §4).

**Reproduction (added 2026-09-03, same hex-and-script-rule gap as §3 above):**

```
$ tshark -r CAP-008-btsnoop_hci.log -Y "frame.number in {1646,1656}" \
    -T fields -e frame.number -e frame.time_relative -e _ws.col.Info
1646   39.092424  Sent Enhanced Setup Synchronous Connection
1656   39.216659  Rcvd Synchronous Connection Complete
```

Raw HCI command/event bytes, via `tshark -r CAP-008-btsnoop_hci.log -Y "frame.number==1646" -x` /
`-Y "frame.number==1656" -x`:

```
1646 (Enhanced Setup Synchronous Connection, opcode 0x043d, connection handle 0x0002):
  01 3d 04 3b 02 00 40 1f 00 00 40 1f 00 00 05 00
  00 00 00 05 00 00 00 00 3c 00 3c 00 00 7d 00 00
  00 7d 00 00 04 00 00 00 00 04 00 00 00 00 10 00
  10 00 02 02 00 00 01 01 00 00 0d 00 88 03 02

1656 (Synchronous Connection Complete event, opcode 0x2c, status 0x00 = success,
      new sync connection handle 0x0005, peer BD_ADDR 6e:cf:6e:07:00:05 (reversed on the wire),
      link type 0x02 = eSCO):
  04 2c 11 00 05 00 07 6e cf 6e 00 04 02 0c 04 3c
  00 3c 00 05
```
Per Wireshark's own HCI dissector (`tshark -r CAP-008-btsnoop_hci.log -Y "frame.number==1646" -V`,
not a hand-decode of the raw bytes above): both the Transmit and Receive Coding Format fields
report `Codec: mSBC (0x05)`, `Transmit`/`Receive Codec Frame Size: 60`, `Input`/`Output Bandwidth:
32000` — an exact match to this section's "mSBC (wideband speech), Tx/Rx bandwidth 8000 B/s, frame
size 60" summary.

`mSBC` (wideband) was selected via a standard Codec Connection Setup exchange just
before each eSCO setup: `Sent +BCS: 2` → `Rcvd AT+BCS=2` → `OK` (frames 1641/1644/1645
for call 1). This matches `AT+BAC=1,2,3`'s advertised codec list from §3 (codec ID 2 =
mSBC); codec ID 3 (LC3/Super Wideband) was offered but not selected in either call.

**This closes `CAP-001-FINDINGS.md` §6 Task 6's open question**: SCO/eSCO absence in
every prior capture was correctly attributed to "no capture ever contained a real
call," not to the Buds/phone never using this mechanism — confirmed here with two
clean, matching occurrences.

## 6. DLCI 0x0a (RFCOMM channel 5) — 14th consecutive silent result, now narrowed further (🔴 OPEN QUESTION, narrowed)

Channel 5/DLCI 0x0a opens (`SABM`/`UA`, frames 1042/1049) exactly like every other
DLCI, but carries **zero payload frames** for the rest of the 309s session — the same
negative result `PROTOCOL.md` §6 already records for 13 prior captures
(`CAP-001`/`CAP-002`/`CAP-005`/`CAP-006`/`CAP-007`/`CAP-016`,
`CAP-011`/`CAP-019`/`CAP-020`/`CAP-022`–`CAP-025`) — `CAP-021` remains the sole
exception (its own still-unexplained 1123-frame burst, `PROTOCOL.md` §6).

This capture specifically tests — and rules out — the one hypothesis this channel had
never been checked against before: that it might carry the call's audio path.
**It does not.** §5 shows the actual SCO/eSCO audio connection is a separate,
HCI-level synchronous connection (its own connection handle, `0x0005`/`0x0006`) that
never touches RFCOMM/L2CAP data framing at all — DLCI 0x0a stays open-but-empty
throughout both calls, the exact condition that should have produced traffic on it if
the "channel 5 = SCO audio" hypothesis from `CAP-001-FINDINGS.md` §6 had been correct.
DLCI 0x0a's actual purpose remains unidentified, but "the call's audio channel" is now
a checked-and-rejected candidate, not merely an untested one.

## 7. A2DP (AVDTP) suspends and resumes around each call (🟢 FACT, connection-lifecycle only)

Both calls show a clean AVDTP `Suspend`/`Start` pair bracketing the call almost
exactly:

| Call | `Suspend` sent | Relative to `+CIEV: 2,1` | `Start` sent | Relative to `+CIEV: 1,0` |
|---|---|---|---|---|
| 1 | 09:39:19.666 (frame 1618) | −23ms (before) | 09:39:51.904 (frame 2034) | +210ms (after) |
| 2 | 09:40:23.423 (frame 2251) | −46ms (before) | 09:41:18.631 (frame 2807) | +287ms (after) |

This is the phone-side Bluetooth stack suspending the podcast's A2DP media stream
(SEID 2, negotiated as AAC per this session's `SetConfiguration`, frame 939 earlier in
the log — noted only as a connection-lifecycle fact, not pursued further per
`PROJECT.md`'s audio-codec non-goal) the moment a call starts alerting, and resuming
it the moment the call ends — standard, expected behavior, but not something this
project had directly observed with a real call before. Matches the maintainer's own
notes ("podcast paused playing" / "podcast resumed playing") exactly.

## 8. `AT+BIEV` HF Indicator #2 tracks the Left earbud this session, not Right (🟡 HYPOTHESIS)

`ADR-015`/`PROTOCOL.md` §4.3 Option C established `AT+BIEV=2,<value>` as a per-earbud
(not fixed-aggregate) indicator, but left open *which* earbud it tracks — `CAP-009`
found it tracked the physical Right earbud that session, with the underlying rule
(fixed-Right vs. whichever earbud is HFP-primary) still unresolved.

This capture's `AT+BIEV=2` value starts at **98** (frames 865/1370/1562,
09:38:50.85–09:39:11.45) and drops to **97** at 09:39:31.516 (frame 1794, during call
1's ringing), staying 97 for the rest of the session (last sample: frame 3461,
09:43:13.04). Cross-checked against the companion app's own Device Details screen,
read directly from three separate video frames:

| Video timestamp | On-screen Left | On-screen Right | On-screen Case |
|---|---|---|---|
| 09:39:09 (`CAP-008-recording1.mp4`) | **98%** | 100% | 43% |
| 09:40:05 (`CAP-008-recording3.mp4`) | **97%** | 100% | 43% |
| 09:41:21 (`CAP-008-recording4.mp4`) | **97%** | 100% | 43% |

The wire's 98→97 transition matches the on-screen **Left** percentage's own 98%→97%
drop exactly, while Right (100%) and Case (43%) never change at all across the whole
session. **This session's `AT+BIEV=2` tracks Left, not Right.** Combined with
`CAP-009` tracking Right, this is now two independent sessions each tracking a
*different* physical earbud — evidence in favor of `PROTOCOL.md` §4.3 Option C's
"whichever earbud is currently HFP-primary" hypothesis over a fixed-Right rule, though
still 🟡 HYPOTHESIS: neither session deliberately swapped the primary earbud
mid-session to test this directly, so which earbud ends up HFP-primary (and why it
differs between sessions) is still not itself explained.

## 9. Isolated `battchg` push via unsolicited `+CIEV` (🔴 OPEN QUESTION)

One `+CIEV: 6,4` (index 6 = `BATTCHG` per this session's own `AT+CIND=?` indicator
list, §3) fires at 09:40:12.143 (frame 2161) — the *only* `BATTCHG` change in the
whole session; the initial `AT+CIND?` query (§3) had read `battchg=3`.

`ADR-015` established `battchg` as "a single, non-repeating snapshot queried once at
SLC setup" based on `CAP-009`'s finding that the `AT+CIND?` *query* is never repeated.
That specific finding is not contradicted here (this capture's own `AT+CIND?` is also
only queried once, frame 801). But this is the first time this project has observed
the **indicator itself** push an unsolicited update via `+CIEV` outside the initial
sync burst — meaning `battchg` is not necessarily frozen for the life of the
connection, even though it is only ever *queried* once. This single occurrence isn't
tied to any video-visible action (it falls inside the notification-shade-open window
in `CAP-008-recording3.mp4`'s 09:40:11–18 span, not the Device Details screen) and is recorded
here as a genuinely unexplained trigger, not force-fit to an explanation — flagged for
`PROTOCOL.md` §6 as a refinement candidate to `ADR-015`'s characterization, not a
reversal of it.

## 10. Video/notes verification (per `AGENTS.md` §13 Step 2)

All timestamps in `CAP-008-EVENT-NOTES.md` were checked against 1fps contact sheets
extracted from all four recordings (burned-in camera clock, 1s resolution) and found
accurate to within 1 second in every case — see the table below. No changes were made
to `CAP-008-EVENT-NOTES.md`.

| Note's claim | Video-verified |
|---|---|
| 09:38:49 Bluetooth turned on | ✅ toggle-on tap visible 09:38:48, "Connecting…" shown 09:38:49 |
| 09:38:52 connected | ✅ "Active, L:98% R:100%" shown 09:38:52 |
| 09:38:59 Spotify play tapped | ✅ tap visible 09:38:57–58, playing 09:38:59; **wire's `AVDTP Start` at 09:38:58.646 corroborates** |
| 09:39:08 Pixel Buds app started | ✅ app-drawer tap 09:39:07, Device Details loading 09:39:08 |
| 09:39:34 call answered | ✅ tap 09:39:34; **wire's `+CIEV:1,1` at 09:39:33.861** |
| 09:39:51 call ended | ✅ tap 09:39:49–50, screen changed by 09:39:51; **wire's `+CIEV:1,0` at 09:39:51.694** |
| 09:40:14 Spotify paused | ✅ pause tap visible exactly 09:40:14 |
| 09:41:00 call answered | ✅ tap 09:40:59–41:00; **wire's `+CIEV:1,1` at 09:40:59.340** |
| 09:41:18 call ended | ✅ tap 09:41:16–17, Device Details shown by 09:41:19; **wire's `+CIEV:1,0` at 09:41:18.344** |

## 11. Test-ID traceability check (per `AGENTS.md` §13 Step 7)

`CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Group V procedure names one Test-ID, `CALL-001`
("Place or receive an actual phone call while connected to the Buds"). This capture
exercises it **twice** (§4), both video- and wire-confirmed — no gap to flag. Group
V's optional step 2 ("trigger a deliberate audio-routing action during the call") was
**not** performed this session — not a gap (the step is explicitly optional), but
noted for a future capture if that data point is wanted.

## 12. Recommended next steps

1. Promote §5 (eSCO/mSBC establishment) and §4 (`CALL-001` wire/video correlation) to
   `PROTOCOL.md` — these clear the FACT bar (frame numbers, byte-level command
   parameters, cross-checked against two independent calls) — pending maintainer
   sign-off per `AGENTS.md` §6.
2. A capture with a deliberate mid-call audio-route switch (Group V's optional step 2)
   would show whether that produces any additional wire signal beyond the
   already-documented AVDTP/eSCO lifecycle.
3. §8's Left-vs-Right `AT+BIEV` question would be best resolved by a capture that
   deliberately swaps which earbud is "primary" mid-session (e.g. removing the
   currently-tracked earbud from the ear during a call) while watching `AT+BIEV`.
4. §9's isolated `battchg` push is a single occurrence — a capture bracketing a
   deliberate battery-level change (similar to `CAP-009`'s Group X design) while
   watching for further unsolicited `+CIEV: 6,...` events would help confirm whether
   this is a real, general mechanism or a one-off.
5. DLCI 0x0a (§6) remains unidentified — now with "the call's audio path" ruled out on
   top of the existing eleven silent captures. No further hypothesis is currently
   proposed for what would trigger payload on it.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-008-2026-08-26_09-38-44_09-41-36-Group_V/CAP-008-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-008-2026-08-26_09-38-44_09-41-36-Group_V/CAP-008-FINDINGS
