# Findings: `CAP-001` (Group Z pipeline-validation capture)

Standardized, evidence-based extraction from `CAP-001-btsnoop_hci.log` + `CAP-001-recording.mp4`, staged here
for later promotion directly into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Every
claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-001` · **Date:** 2026-08-09 · **Firmware:** `release_5.203` ·
**Phone:** Pixel 7a, Android 17 (official app v1.0.955078536) ·
**Log file:** `CAP-001-btsnoop_hci.log` (233.9s, 2,663 packets, 2026-08-09 08:50:32.67–08:54:26.57
local/+0200) · **Video:** `CAP-001-recording.mp4` (83.4s, 08:50:57–08:52:20 local, on-screen wall-clock
overlay) · **Devices:** phone `Google_7e:ca:81` (Pixel 7a, BD_ADDR partially redacted per
`AGENTS.md` §9), peer `Google_cf:6e:07` (the Buds/case, BD_ADDR partially redacted).

**Scope note:** this session was run as the Group Z pipeline-validation capture
(`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1) but in practice also exercised Group A (pairing
baseline), Group B (all four ANC modes, several times over), and Group M (case/in-ear
transitions) — see `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index update. Actions were not
isolated with clean pauses (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4's "one action per capture
window" rule) — six ANC-mode changes happened within ~30 seconds alongside other activity. This
materially limits how confidently individual RFCOMM frames can be attributed to a specific ANC
click (see §5 below).

---

## 1. Connection lifecycle (🟢 FACT)

Classic BR/EDR (RFCOMM) connection required **three** `Create Connection` attempts, not one:

| # | Sent | Result | Frame(s) |
|---|---|---|---|
| 1 | 08:51:01.981 | **Page Timeout** (status `0x04`) — buds not yet reachable | 732 → 737 |
| 2 | 08:51:07.325 | Succeeded (status `0x00`), then torn down ~0.24s later | 738 → 775 (connect), 831 (disconnect) |
| 3 | 08:51:09.565 | Succeeded (status `0x00`), persists for the rest of the session | 832 → 855 |

A BLE (LE) link to the same peer was already established earlier and independently, at
08:50:36.27 (frame 290, `LE Enhanced Connection Complete`) — well before the case was opened on
camera (~08:51:08) and before any classic-link attempt. This is consistent with the phone having
an existing GATT-level association with a previously-bonded device that predates this session's
visible "Forget" action (see §4 below — the app's own `Forget` button was tapped at 08:51:02–03
per video, yet a BLE link already existed 26s *before* that).

After connection 3 succeeds: `Link Key Request` → `Link Key Request Reply` → `Authentication
Complete` → `Set Connection Encryption` → `Encryption Change` all complete by 08:51:12.208
(frames 911–917), using a **stored link key** (no PIN/passkey exchange visible) — i.e. this was
a reconnection to a device the phone still held bonding material for, not a from-scratch pairing,
regardless of the on-screen "Forget" tap (see open question in §6).

## 2. RFCOMM channel topology (🟢 FACT / 🟡 HYPOTHESIS per channel)

Channel numbers below are the RFCOMM **server channel** number (not the DLCI); PSM 0x0003
throughout, single L2CAP connection carrying the whole multiplexer session.

| Channel | DLCI (phone→buds / buds→phone) | Opened (frame) | Content observed | Status |
|---|---|---|---|---|
| 0 | 0x00 | 984 (SABM) | RFCOMM multiplexer control (PN negotiation for all other channels) | 🟢 FACT |
| 1 | 0x02 | 1334 (phone-init) | Repeating ~21–57 byte frames, each starting and ending with `0x7e` (HDLC-style flag byte), two alternating frame-header variants (`80a3`/`00a5` after the flag) | 🟡 HYPOTHESIS — structure suggests AVRCP (SDP confirms an AVRCP service exists, frames 1164–1231), but not confirmed byte-for-byte against the AVRCP spec here |
| 2 | 0x04 | 990 (phone-init) | Short frames (6–24 bytes), several containing an `e8e8XX` byte pattern where `XX` varies between samples; also flow-control-shaped frames (`ff01...`, `08 13...`) | 🟢 FACT (2026-08-12) — this is Fast Pair's official "Hearable Controls" extension (Message Group `0x08`), Get/Set/Notify ANC state; `XX` is a one-hot ANC-mode bitmask, confirmed byte-for-byte against the spec and against this capture's own tap timeline (see §5's "Full resolution" and `PROTOCOL.md` §4.1) |
| 4 | 0x08 (phone-init) / 0x09 (buds-init, frame 1217) | 1035 / 1217 | **Two distinct payload types multiplexed under the same channel number**: (a) on 0x08 — periodic (~6–7s) frames containing the ASCII string `google-pixel-buds-pro-v1` and a separate protobuf-shaped blob containing ASCII `all`; one early frame (1673, 08:51:32.79) contains ASCII `Europe/Amsterdam`; (b) on 0x09 — plain-ASCII HFP AT commands, see §3 | 🟢 FACT (channel exists, is dual-directional, carries this content) |
| 5 | 0x0a | 1068 | No data-carrying frames observed in this capture, only PN/SABM/DISC control traffic | 🔴 OPEN QUESTION — channel opened and closed repeatedly but never carried a payload here |

> **Correction (2026-08-10), source: `CAP-002`'s `CAP-002-FINDINGS.md` §2/§3.** The 🟡 HYPOTHESIS above
> for channel 1/DLCI 0x02 — that its `0x7e`-delimited content "suggests AVRCP" — is **not
> supported** by `CAP-002` (a separate, fresh-pairing session against the same physical device).
> In `CAP-002`, the same-shaped `0x7e`-delimited traffic reappeared on channel 1 again (so that
> part replicates), but the *channel-2* traffic in that session was independently identified —
> and spec-verified against Google's actual Fast Pair Message Stream / Device Information
> documentation — as the **Fast Pair Message Stream, Device Information group (`0x03`)**, not
> AVRCP. This document's own §2 already correctly flagged channel 2/DLCI 0x04 here (this
> session) as unresolved (🔴 OPEN QUESTION, not attributed to AVRCP) — the AVRCP speculation was
> specifically about *channel 1*, which remains genuinely unresolved as of this correction; only
> the *channel-2* content has since been identified, in the other capture. Per
> `PROJECT_RULES.md` §3, this note supersedes the *channel 1 = possibly AVRCP* framing above
> without deleting it, following the same non-destructive correction pattern `DECISIONS.md` uses
> for superseded ADRs. See `CAP-002`'s `CAP-002-FINDINGS.md` §3 for the full spec-verified writeup, and
> the reusable methodological note directly below.
>
> **Reusable note for all future capture analysis — RFCOMM channel numbers are not stable
> per-profile labels.** RFCOMM server channel numbers are negotiated per-connection, not fixed
> per Bluetooth profile. `CAP-001` and `CAP-002-CAP-002` — two independent sessions against the same
> physical device — assigned *different* channel numbers to structurally similar traffic (this
> capture put HFP-adjacent Device-Information-group traffic on what was locally numbered
> "channel 2"; `CAP-002` also happened to land it on "channel 2", but `CAP-002`'s HFP itself came
> up on "channel 6", not "channel 4" as here). Any future finding should be keyed off **payload
> content/structure** (byte shape, known strings, spec-matched framing) and the **DLCI**, not the
> channel number alone — a channel number is only ever valid as a label *within the one session*
> it was observed in.

> **Follow-up (2026-08-12), deskresearch task — channel 1/DLCI 0x02's `0x7e`-delimited content
> characterized further; still not AVRCP, and not a match to any other known encapsulation
> either.** Extracted and split every RFCOMM payload on this DLCI (this capture and `CAP-002`'s,
> which — since it's the same shared buffer — includes this capture's own frames plus many more
> hours) on the `0x7e` delimiter byte. Every one of the resulting sub-frames starts with a
> consistent 3-byte micro-header: **byte 0 ∈ {`0x00`, `0x80`}, byte 1 ∈ {`0x3b`, `0x4b`, `0xa5`,
> `0xa3`}, byte 2 = `0x03` always** (854 sub-frames checked across both files' full logs, zero
> exceptions to byte 2 being `0x03`). `{00,3b}`/`{00,4b}` only ever appear in "Sent" (phone→Buds)
> frames; `{00,a5}`/`{80,a3}` only ever appear in "Rcvd" (Buds→phone) frames — direction-correlated,
> not random. Two payload shapes follow this header:
> - **Sent** frames: header + a 1-byte length (observed `0x10`=16) + exactly that many opaque
>   bytes, e.g. frame 1348: `00 4b 03 10 <16 opaque bytes>` — structurally identical to a
>   length-prefixed AES-128 block.
> - **Rcvd** frames: header + a rich, cleanly-decodable **protobuf** structure — e.g. frame 1346
>   contains three repeated 27-byte sub-blocks, each holding a 10-digit device serial number
>   (`"1779298694"`) paired with the firmware string `"release_5.203"` (`PROTOCOL.md` §0.1's
>   confirmed baseline) — i.e. this channel independently carries the same firmware string found
>   elsewhere on DLCI 0x08 (`CAP-002` `CAP-002-FINDINGS.md` §2a), on a *third*, structurally distinct
>   channel/framing.
>
> **Byte-length hypothesis ruled out (negative result):** interpreting byte 1 as an RFCOMM-style
> EA-extended length byte (`byte1 >> 1`) does **not** match the actual remaining payload length in
> any sampled frame — byte 1 is a message-type/opcode-like value, not a length field. **CRC/FCS
> hypothesis inconclusive, not confirmed:** the two trailing bytes before each closing `0x7e` do
> not match CRC-16/X.25 or CRC-16/CCITT-FALSE computed over the preceding bytes (checked for 3
> sample frames) — either a different CRC variant is in use, or (more likely, since the payload
> itself looks like opaque/encrypted data) there is no separate trailer at all and these are simply
> the value's own final bytes.
>
> **Answering this task directly:** this is **not** a byte-for-byte match to any standard, known
> Bluetooth L2CAP/RFCOMM sub-encapsulation — no such profile uses a bare `0x7e` flag byte with this
> exact 3-byte micro-header, and it is structurally distinct from both the official Fast Pair
> Message Stream framing (§2.0/§2.1, which has no `0x7e` flags or fixed 0x03 byte at all) and from
> DLCI 0x08's private `[Group][Code][Length]` envelope characterized elsewhere in this file and in
> `CAP-002`/`CAP-004`. It **is** consistent with a proprietary, HDLC/PPP-*style* framing
> (flag-delimited, no explicit length field, relying on the flag byte for frame boundaries) whose
> fixed third byte (`0x03`) sits in the exact position — and has the exact value — PPP's own
> Control field convention uses (RFC 1662, "Unnumbered Information"), though the preceding
> address-like byte pair does not follow PPP's fixed `0xFF`-address convention (it varies
> systematically by message kind instead) — so this reads as **a proprietary protocol borrowing the
> HDLC/PPP flag-and-control-byte idea, not a standards-compliant PPP or L2CAP/RFCOMM sub-channel**.
> Given its opaque 16-byte "Sent" payloads and rich protobuf "Rcvd" payloads carrying device
> identity/firmware data, this channel is a plausible candidate for part of `libmaestro` itself or
> a related proprietary companion-device channel — not confirmed, and out of scope to resolve
> further in this pass.

> **Upgrade (2026-08-12), deskresearch task — the "not confirmed" verdict above is superseded: this
> channel's framing IS now definitively identified, as Pigweed `pw_hdlc`.** `qzed/pbpctrl`'s own
> project notes (`docs/Notes.md`, consulted per `AGENTS.md` §12/`DECISIONS.md` ADR-003 — protocol
> knowledge only) state verbatim: *"The protocol is implemented using the pigweed RPC library...
> the RPC messages are wrapped in High-Level Data Link Control (HDLC) U-frames."* Testing this
> empirically against every RFCOMM payload on this DLCI in `CAP-001`, `CAP-002`, and `CAP-003`
> (`CAP-004` never opens it) — HDLC-unescaping each `0x7E`-delimited sub-frame (`0x7D <X>` → `X XOR
> 0x20`, standard byte-stuffing) and computing a CRC-32 (IEEE 802.3/zlib polynomial, little-endian)
> over everything but the trailing 4 bytes — gives a **640/640 (100%) match** against those
> trailing 4 bytes, with zero exceptions:
> ```
> CAP-001 frame 1348, raw (between flags): 004b0310151dea71de7d5e25e3a5ec28f96761b5
> unescaped (7d 5e -> 7e):                  004b0310151dea71de7e25e3a5ec28f96761b5
> crc32(unescaped[:-4]) little-endian     = f96761b5  == trailing 4 bytes  MATCH
> ```
> This resolves the byte-length hypothesis (ruled out above, correctly) and the CRC/FCS hypothesis
> (left "inconclusive" above) in one step: byte 1 (`0x3b`/`0x4b`/`0xa5`/etc.) is not a length byte
> at all — decoding byte 0 as an HDLC-standard LEB128 varint **Address** field (per Pigweed's own
> pw_hdlc spec) shows it terminates at 1 byte (`0x00`) for most frames, with the *next* byte being
> a single-byte **Control** field (`0x3b`/`0x4b` for phone→Buds, `0xa5` for Buds→phone) — and a
> second, 3-byte LEB128 address (`0xD180` = 53632, Buds→phone only, control `0x08`/`0x2a`) appears
> in a minority of frames, plausibly a second multiplexed pw_rpc channel. **The trailing bytes are
> a genuine CRC-32 FCS, not a raw data tail or an unconfirmed algorithm.** This is now 🟢 FACT for
> the framing mechanism (see `PROTOCOL.md` §2.2a for the full field-by-field table and
> reproduction script) — replacing this bullet's "not a byte-for-byte match to any standard...
> encapsulation" conclusion, which was correct about ruling out AVRCP/PPP/L2CAP but incorrect that
> no standard mechanism at all was in play. Whether this specific channel *is* `libmaestro`
> specifically (vs. some other Pigweed-RPC Google service) remains 🟡 HYPOTHESIS (strong) — no
> Maestro-specific command content (an ANC/EQ write) has been decoded from it yet; see
> `PROTOCOL.md` §2.2a/§2.3 for the full reasoning and what's still needed to close that gap.

**Protobuf framing evidence (🟢 FACT):** frame 1673's payload (channel 4, DLCI 0x08) is
`09 03 00 00 03 01 00 1b 08 9f 03 10 de af a9 aa 0e 1a 10` + `"Europe/Amsterdam"` (16 ASCII
bytes). The byte pair `1a 10` immediately preceding the 16-character string decodes as a
protobuf tag (field 3, wire type 2 = length-delimited) + length prefix `0x10` = 16, which exactly
matches the string length. This is concrete evidence that **at least this frame is
protobuf-encoded**, consistent with `PROTOCOL.md` §3's `.proto` schema hypothesis, though this
specific message (looks like a timezone-sync/device-info field) is not one of the
already-hypothesized schemas (`maestro_pw`/`anc_settings`/`eq_settings`/`hardware_status`) —
flagged as a new, unidentified message type.

## 3. HFP (Hands-Free Profile) AT-command handshake (🟢 FACT)

Channel 4 / DLCI 0x09 carries a full, literal-ASCII HFP AT-command handshake starting
08:51:13.959 (frame 1236), immediately after the RFCOMM channels stabilize:

```
AT+BRSF=921          → +BRSF: 3951                (supported-features exchange)
AT+BAC=1,2,3         → OK                          (available codecs)
AT+CIND=?            → indicator list: call, callsetup, service, signal, roam, battchg, callheld
AT+CIND?              → 0,0,0,0,0,3,0               (battchg = 3, i.e. 3/5 on the CIND scale)
AT+CMER=3,0,0,1       → OK                          (enable indicator event reporting)
AT+BIND=1,2 / AT+BIND=? / AT+BIND? → HF indicators 1 (enhanced safety) and 2 (battery level) both active
AT+BIEV=1,1            → OK
AT+VGM=7 / AT+VGS=8    → OK                          (mic/speaker gain)
AT+NREC=0              → +CME ERROR: 4 (not supported)
AT+COPS=3,0 / AT+CMEE=1 → OK
AT+BIEV=2,100           → OK   ← repeats at 08:51:14.106, 20.070, 20.136, 34.392, 34.860,
                                  41.410, 52.148(x2) — roughly every 6–7s
```
(Full frame list: 1236–1310, 1558–1574, 1949–1969, 2027–2028, 2245–2246, 2268–2269.)

**Battery finding (🟢 FACT):** `AT+BIEV=2,100` reports HF Indicator #2 (Battery Level, per the
Bluetooth HFP spec's assigned-number registry) = **100**, matching the app's displayed Left/Right
= 100% at the time. This is the standard HFP "Bluetooth HF Indicators" mechanism —
`PROTOCOL.md` §4.3 Option C — now confirmed **actually active** for this device/session, not
just an assumed fallback.

**Discrepancy worth flagging (🔴 OPEN QUESTION):** the older `AT+CIND?` `battchg` indicator
reported `3` (on its native 0–5 scale, ≈60%) at the very start of the handshake (08:51:13.996),
while `AT+BIEV=2,100` reports 100% throughout. Both are standard, simultaneously-active HFP
mechanisms on this device, but they disagree — worth a dedicated follow-up capture bracketing a
real battery-level change to see which one (if either) tracks it accurately, and whether
`battchg` is a stale/init-time-only value.

Neither HFP indicator distinguishes Left/Right/Case — both appear to report a single aggregate
value. The BLE Fast Pair Battery Notification advertisement (`PROTOCOL.md` §4.3 Option A, which
*does* have separate L/R/Case fields) was not captured in this RFCOMM-focused pass — no
`btle`-filtered advertising data was extracted from this log in this session.

## 4. Video-observed app behavior, correlated to the log (🟢 FACT)

| Video (wall-clock) | On-screen event | Correlated log evidence |
|---|---|---|
| 08:51:02–03 | Tap **Forget** | No log-visible effect before 08:51:12 (see §6 — a BLE link and cached link key both still existed afterward) |
| 08:51:06–11 | Case lid opened, buds visible with case LED lit | 1st `Create Connection` at 08:51:01.98 fails with Page Timeout (frame 733/737) — consistent with the case/buds not yet reachable until the lid was fully open |
| 08:51:14–15 | App briefly shows **"Problem connecting. Turn device off & back on"** | Coincides with the successful-then-immediately-torn-down 2nd connection (frames 775 connect / 831 disconnect, 08:51:09.33–09.56) |
| 08:51:12–15 | (not directly visible on screen) | RFCOMM channels 0, 2, 4, 5 opened (frames 984–1070); SDP queries for Audio Sink/AVRCP/HFP (frames 902–1231); HFP AT handshake (frames 1236–1310) |
| 08:51:32 | Tap **Transparency** (1st) | See §5 — no confidently attributable command frame identified |
| 08:51:39 | Tap **Off** | See §5 |
| 08:51:43 | Tap **Adaptive** | See §5 |
| 08:51:49 | Tap **Transparency** (2nd) | See §5 |
| 08:51:54 | Tap **Noise Cancellation** | See §5 |
| 08:52:00 | Tap **Off** (2nd) | See §5 |
| 08:52:08–13 | Buds placed back in case, lid closed | `Disconnect Complete` at 08:52:08.343 (frame 2302) |

## 5. ANC command attribution attempt — inconclusive (🔴 OPEN QUESTION)

For each of the six ANC-mode taps above, the RFCOMM traffic in the surrounding ±3s window was
inspected on every open channel. Findings:

- Channel 4/DLCI 0x08 shows a recurring ~24-byte frame near several (not all) of the taps, with
  a varying byte immediately after a fixed `e8e8` prefix (e.g. `...e8e840`, `...e8e880`,
  `...e8e808`, `...e8e820`). This looked initially promising as a mode-ID byte, **but** the "Off"
  action (tapped twice, at 08:51:39 and 08:52:00) produced two *different* trailing bytes (`40`
  and `20`) rather than a repeatable value for the same mode — this contradicts a simple
  "byte = ANC mode" hypothesis. It is more likely a rolling counter, timestamp fragment, or
  flow-control sequence value unrelated to ANC state. **Not promoted past 🔴.**
- The same window also contains the periodic HFP `AT+BIEV=2,100` battery report and the periodic
  `google-pixel-buds-pro-v1`/capability-negotiation frames on channel 4/DLCI 0x08 — both fire on
  their own ~6–7s cadence regardless of user action, at intervals close enough to the ANC-tap
  spacing (5–7s, per the test procedure's own rhythm) that a naive "nearest frame in time" match
  is unreliable.
- Channel 1/DLCI 0x02's `0x7e`-delimited frames also cluster near several taps but with the same
  ambiguity — their cadence looks driven by connection/AVRCP housekeeping, not clearly by the
  ANC button.

**Conclusion:** this capture does not yield a confident FACT or even a testable HYPOTHESIS for
the ANC-mode command opcode/channel. This is a direct consequence of the scope note in the
header — six mode changes were bundled into one continuous, unpaused session together with
pairing and case/bud housekeeping, which is exactly the isolation failure
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4 warns against. **Recommendation:** a dedicated Group B
capture with genuinely isolated actions (wait ~10s of silence, single ANC tap, wait ~10s of
silence, repeat) is needed before promoting any ANC-opcode claim to `PROTOCOL.md`.

> **Resolution (2026-08-12), deskresearch task — the `e8e8XX` open question closes to a 🟢 FACT
> characterization, but NOT as an ANC opcode.** First, a channel-label correction: this bullet
> says "Channel 4/DLCI 0x08" for the `e8e8` pattern, but §2's own table above (and this capture's
> raw frames) place it on **Channel 2/DLCI 0x04** — §2's table was correct, this bullet's channel
> label was not; flagged here rather than silently fixed. Second, and substantively: `CAP-002`'s
> `CAP-002-btsnoop_hci.log` is the same shared, non-restarted buffer as this capture's (see that file's own
> header), so it contains many more hours of the same traffic. Filtering the *whole* shared log for
> the exact byte pattern `e8 e8` (`tshark -r CAP-001-btsnoop_hci.log -Y 'btrfcomm.len > 0 and data.data contains "e8:e8"'`)
> returns 26 frames spanning 08:51:29–08:52:02 (this session's own window, same frames this bullet
> already found) — no further occurrences later in the ~8h20m buffer, so the exchange itself is
> tied to this session's activity window, not a background heartbeat that runs all day. Precisely
> decoded, every frame in this exchange fits the confirmed `[Group][Code][Length:2B-BE][Value]`
> Message Stream envelope (`PROTOCOL.md` §2.1), on a **previously undocumented Group `0x08`**:
> ```
> Rcvd  08 13 00 04  01 e8 e8 XX                                   Group=0x08 Code=0x13 Len=4  (periodic notify)
> Sent  08 12 00 14  01 e8 e8 XX <16 opaque bytes>                 Group=0x08 Code=0x12 Len=20  (phone's write, echoing the same XX)
> Rcvd  ff 01 00 06  08 12 01 e8 e8 XX                             Group=0xff Code=0x01 Len=6   (ACK, echoing "08 12 01 e8e8XX" back)
> ```
> The `ff 01 00 06 08 12 01 e8 e8 XX` line is a **textbook match to `PROTOCOL.md` §2.1's own
> documented ACK shape** (`0xFF 0x01 <len> <acked group/code/data>`), applied here to a new group
> (`0x08`) this project had not previously identified — i.e. this *is* a real, working Message
> Stream request/notify/ACK cycle, not noise. Across all 26 frames, `XX` takes only **four distinct
> values: `0x08`, `0x20`, `0x40`, `0x80`** — each a **single set bit** (bit 3, 5, 6, 7 respectively).
> This is a clean, structural finding that resolves the specific hypotheses this bullet raised:
> **🟢 FACT — not a modulo-256 (or any) incrementing counter** (a counter would not stay confined to
> four one-hot values over 26 samples); **not confirmed as a raw timer value either** (a
> free-running timer would not cluster into exactly four one-hot bit positions). The evidence best
> fits **a one-hot state/mode flag byte** signaling one of (at least) four mutually-exclusive
> states via Group `0x08` — plausible candidates include a connection-quality/RSSI tier, a
> charge-state flag, or an "active component" indicator, **none confirmed**; the *specific*
> semantic meaning of the bitmask remains 🔴 OPEN QUESTION, narrower and better-defined than the
> original "ANC-mode byte?" framing this bullet started from. This capture's own six ANC taps
> happened not to correlate with distinct `e8e8XX` values in any consistent way, which is now
> understood as expected — this bitmask answers a different question than ANC state entirely.

> **Full resolution (2026-08-12), deskresearch task — this is not just "ANC-relevant," it is the
> confirmed ANC set/get/notify command channel, byte-for-byte, spec- and capture-verified.** First
> pass at this correction (superseded below) found `qzed/pbpctrl`'s community notes naming "group 8,
> code 19" as an ANC-status event — that pointed at the right Group/Code pair but wasn't itself
> conclusive. Following up directly against **Google's own official specification** —
> [Hearable Controls extension](https://developers.google.com/nearby/fast-pair/specifications/extensions/hearablecontrols),
> `[OFFICIAL-SPEC]` — resolves this completely:
>
> - Message Group `0x08` = "Hearable control", with three documented codes: `0x11` Get ANC state,
>   `0x12` **Set ANC state** (Seeker→Provider, MAC+ACK required), `0x13` Notify ANC state
>   (Provider→Seeker) — this bullet's `08 13 00 04 01 e8 e8 XX` is exactly a "Notify ANC state"
>   frame (`[Group][Code][Len=4][Version:1][UI toggles:1][Settable toggles:1][Current state:1]`).
> - The spec's own bit layout for the ANC-mode flag bytes (`Bit 0`=Transparent, `Bit 1`=Adaptive,
>   `Bit 2`=Off, `Bit 3`=Reserved, `Bit 4`=ANC, spec's own MSB-first numbering — standard bit7..bit3)
>   maps this bullet's four observed one-hot values **exactly, with zero leftover or ambiguous
>   value**: `0x80`=Transparent/Aware, `0x40`=Adaptive, `0x20`=Off, `0x08`=ANC/Active Noise
>   Cancelling — precisely the four ANC UI states this project already knew existed
>   (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §1).
> - **The phone's own outbound command exists too, and was hiding in plain sight:** four `08 12 00
>   14 01 e8 e8 XX <16 reserved bytes>` frames ("Set ANC state") appear in this exact capture —
>   frames 2039, 2132, 2159, 2193 — each immediately followed by the documented ACK
>   (`ff 01 00 06 08 12 01 e8 e8 XX`, frames 2041/2134/2162/2195). Decoding `new_mode` (byte 7) for
>   each and comparing against this file's own §4 tap timeline:
>   ```
>   frame 2039 @08:51:41.77  new_mode=0x40 (Adaptive)              nearest tap: "Adaptive" @08:51:43
>   frame 2132 @08:51:48.14  new_mode=0x80 (Transparent/Aware)      nearest tap: "Transparency (2nd)" @08:51:49
>   frame 2159 @08:51:53.39  new_mode=0x08 (ANC/Noise Cancelling)   nearest tap: "Noise Cancellation" @08:51:54
>   frame 2193 @08:51:59.20  new_mode=0x20 (Off)                    nearest tap: "Off (2nd)" @08:52:00
>   ```
>   **4/4 match their nearest tap by content, in the correct sequence, each within ~1–1.5s** — well
>   inside this capture's own already-documented ±1s video-sampling uncertainty. This is not a
>   spec-shape resemblance; it is a positive identification with an internal cross-check.
> - **Not resolved:** the capture's *first two* taps (Transparency @08:51:32, Off @08:51:39) have no
>   matching `0x12` frame anywhere in the log — plausibly UI-state realization rather than genuine
>   commands (the ANC row was still greyed out until shortly before this, per §4's tap table), not
>   confirmed either way.
>
> **Status: 🟢 FACT** — clears `PROJECT_RULES.md` §1's promotion bar twice over (official spec
> byte-match, plus an internal content+timing cross-check this capture itself supplies). This
> single-handedly resolves this bullet's original "ANC-mode byte?" question, this section's
> "inconclusive" verdict, and `PROTOCOL.md` §4.1's long-standing 🔴 unconfirmed status — see
> `PROTOCOL.md` §4.1 for the full write-up now promoted there, and §2.3's updated three-channel
> table for how this fits alongside DLCI 0x02 (`libmaestro`/Pigweed HDLC, still unresolved for its
> own command content) and DLCI 0x08 (the separate private envelope, also still unresolved).
>
> **Risk flag, added 2026-08-15 (`DECISIONS.md` ADR-009):** the 🟢 FACT status above covers the
> *framing/opcode identification* — what a `0x12` frame means when one appears. It does **not**
> mean every ANC tap is confirmed to reliably produce one: **2 of this capture's 6 physical ANC
> taps (Transparency @08:51:32, Off @08:51:39) have no matching `0x12` frame anywhere in the
> log** (see "Not resolved" above). That's a 1/3 miss rate in the only capture that exists for
> this command so far. The leading explanation (first-tap UI-state realization while the row was
> still greyed out, not a genuine user-initiated set) is plausible but **not confirmed** — the
> alternative, that real user taps can silently fail to produce a wire command under some
> condition not yet understood, would be a functional risk for the app being built, not just a
> documentation gap. This is why `FrameEncoder` implementation for this command is explicitly
> blocked on `CAP-006` (a clean, single-tap-per-window repeat) rather than proceeding on this
> capture's evidence alone — see ADR-009.

## 6. Other open questions raised by this capture

- Why did a BLE link and a still-valid link key both exist *before* the on-screen "Forget" tap
  (08:51:02–03) and *before* the case was reopened? Either the "Forget" tap didn't fully clear
  bonding state, or the visible BLE link (08:50:36) belongs to a different logical association
  than the classic-link bonding that was reused at 08:51:12. Needs a cleaner capture that starts
  before any prior state exists.

  > **Update (2026-08-26):** `CAP-013` attempted exactly the "cleaner capture that starts before
  > any prior state exists" called for above, but did not achieve it — HCI snoop logging in that
  > session only began 2m21s *after* its own clearing action ("Reset Bluetooth & Wi-Fi", not a
  > single-device "Forget") and after the subsequent case-open/pair-button/device-selection
  > sequence too, leaving the question above still fully 🔴 OPEN (`CAP-013-FINDINGS.md` §0,
  > `PROTOCOL.md` §6's "Behavior" section). `CAP-013` did resolve a related but distinct secondary
  > question (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `PAIR-004`) — the re-pairing that followed its
  > own clearing action used a fresh SSP handshake, not a reused key, for whatever bonding state
  > was active when its logging window began (`CAP-013-FINDINGS.md` §2/§7). A further repeat, with
  > logging verified to start before the clearing action itself, is still needed to answer this
  > section's original question — proposed as a new capture (next free ID `CAP-031`, not yet
  > assigned) in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Group-A-repeat note.
- Channel 1 (DLCI 0x02) and channel 2 (DLCI 0x04)'s exact protocol identity is still unconfirmed
  — candidates are AVRCP and/or A2DP signaling (both present in SDP), but this was not verified
  byte-for-byte against either spec in this pass.
- Channel 5 (DLCI 0x0a) opened/closed repeatedly but never carried a payload in this session —
  worth checking whether it's HFP's audio channel (SCO/eSCO, which wouldn't appear as RFCOMM
  data) or simply unused here.

  > **Task 6 (2026-08-12): checked at the HCI level across all four captures — SCO/eSCO
  > hypothesis ruled out, clean negative result.** Filtered every `bthci_evt` event code and
  > every `bthci_cmd` opcode in `CAP-001`, `CAP-002`, `CAP-003`, and `CAP-004`'s full logs for
  > Synchronous Connection Complete (`0x2C`), Synchronous Connection Changed (`0x2D`), Setup
  > Synchronous Connection (`0x0428`), and Enhanced Setup Synchronous Connection (`0x043D`) — the
  > HCI-level events/commands a SCO/eSCO link would necessarily produce, and which would **not**
  > appear as RFCOMM data (explaining channel 5's silence, if this hypothesis held). **Zero
  > matches in all four captures** — confirmed by listing every distinct `bthci_evt.code` value
  > actually present in each log (17–30 distinct codes per capture, none `0x2C`/`0x2D`) and every
  > `bthci_cmd.opcode` (none `0x0428`/`0x043D`). **No SCO/eSCO connection is ever established in
  > any of these four sessions, full stop** — this isn't a timing-correlation question (channel 5
  > opening lining up with a SCO event) because there is no SCO event to correlate against at all.
  > This rules out the SCO/eSCO hypothesis definitively rather than leaving it "worth checking."
  > Channel 5/DLCI 0x0a's silence remains 🔴 OPEN QUESTION — genuinely unused in every capture to
  > date, not explained by an out-of-band audio path. (Unsurprising in hindsight: none of these
  > four sessions ever place or receive a live phone call, the only scenario that would trigger
  > HFP's SCO/eSCO audio path — a future capture bracketing an actual call would be a more
  > direct test than re-checking these four idle-audio sessions again.)

## 7. Recommended next steps

1. A properly isolated Group B capture (single ANC action per window, per
   `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4) to re-attempt ANC opcode attribution.
2. A passive BLE scan (Group Q #18) to capture the Fast Pair Battery Notification advertisement
   independently, to cross-check against the two conflicting HFP battery indicators found here.
3. Correlate channel 1/2 traffic against the AVRCP/A2DP specs directly (byte-level) before
   spending more capture time on them. ~~Partially done 2026-08-12~~ — see the "Follow-up
   (2026-08-12)" note under §2's table: channel 1/DLCI 0x02 is now characterized in detail
   (HDLC/PPP-*style* flag-delimited framing, not AVRCP, not standard PPP either) but its exact
   protocol identity is still unconfirmed; channel 2/DLCI 0x04's `e8e8XX` traffic is now resolved
   to a Group `0x08` Message-Stream request/notify/ACK cycle with a one-hot state byte (§5's
   "Resolution (2026-08-12)" note) — neither is AVRCP/A2DP, so this item's original premise (they
   might be AVRCP/A2DP signaling) is superseded rather than completed as originally framed.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-001-2026-08-09_08-51-00_08-52-20-Group_Z/CAP-001-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-001-2026-08-09_08-51-00_08-52-20-Group_Z/CAP-001-FINDINGS
