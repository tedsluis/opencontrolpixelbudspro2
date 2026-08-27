# Findings: `CAP-013` (Group A repeat — intended pre-clearing-action baseline)

Standardized, evidence-based extraction from `CAP-013-btsnooz_hci.log` + `CAP-013-recording.mp4`,
staged here per `PROJECT_RULES.md` §2. Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-013` · **Date:** 2026-08-26 · **Firmware:** `release_5.203` (screen-confirmed) ·
**Phone:** Pixel 7a, Android 17 (⚪ build not screen-confirmed this session) · **Log file:**
`CAP-013-btsnooz_hci.log` (336.7s, 1,747 packets, 2026-08-26 17:11:45.799616–17:17:22.478814 +0200,
**`btsnooz`-fallback extraction, ~15-byte inferred capture length per packet**) · **Video:**
`CAP-013-recording.mp4` (304.4s, 17:09:01–17:14:04, burned-in wall-clock overlay) · **Devices:** phone
`E8:D5:xx:xx:CA:81` (Pixel 7a, partially redacted per `AGENTS.md` §9), peer `Google_cf:6e:07`
(`04:00:6e:...:6e:07` — same address as `CAP-001`–`CAP-012`, independently re-confirmed on the wire
this session).

---

## 0. Scope: what this capture can and cannot answer

This capture was planned (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Group-A-repeat note,
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `PAIR-004` row) to resolve a specific gap:
`CAP-001-FINDINGS.md` §6 found a BLE link and a still-valid classic link key both existing *before*
the on-screen clearing action and before the case was even reopened — genuinely unresolved whether
that clearing action fully removes prior bonding/BLE-association state. The intended method was to
start HCI snoop logging **before any association exists at all** (e.g. immediately after a phone
restart) and watch the entire clearing-and-re-pair sequence from a known-clean starting point.

**That method was not what actually happened, and the gap is confirmed, verified independently against
both the video and the log (not taken on the maintainer's advance word alone) to be larger than
originally flagged** — see `CAP-013-EVENT-NOTES.md`'s deviation note for the full detail:

- The clearing action actually performed was **"Reset Bluetooth & Wi-Fi"** (System → Reset options),
  not a single-device "Forget" — a broader, phone-wide action, but still not logged.
- `CAP-013-btsnooz_hci.log`'s first frame is 17:11:45.799616 — **2 minutes 21 seconds after** the
  17:09:24 reset, and also after Bluetooth being re-enabled, the case being opened, the case's pair
  button being pressed, "Pair new device" being tapped, the device appearing in the discovery list, and
  the device being tapped in that list. None of that is in the log.

**Primary question (`CAP-001-FINDINGS.md` §6 / `PROTOCOL.md` §5.1's still-open cross-reference) —
🔴 remains OPEN QUESTION, NOT answered by this capture.** Whether a BLE link or valid link key existed
*before* the clearing action cannot be determined here: the clearing action itself, and the ~2m21s
of activity between it and the device-selection tap, fall entirely in this capture's un-logged gap. Any
absence of "prior state" in this log's visible window is not evidence of absence *before* that window —
per this session's guardrail on negative conclusions requiring a covered time window, no such
conclusion is drawn here.

**Secondary question (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `PAIR-004` — does the subsequent re-pairing
show a fresh SSP/link-key handshake, or reuse of an old key?) — 🟢 answerable, and answered, from what
was captured.** The classic bonding sequence (`Delete Stored Link Key` onward) happens 1 second into
the log window, fully captured — see §2 below. This is the one genuine, usable result of this session.

## 1. Log-truncation limitation (🟢 FACT)

`capinfos CAP-013-btsnooz_hci.log` reports an inferred packet size limit of 15 bytes. Confirmed
directly against individual frames, not just the tool's own inference: frame 661 (`0e01` battery-shaped
DLCI `0x08` traffic) shows `frame.len`=53 but `frame.cap_len`=15
(`tshark -r CAP-013-btsnooz_hci.log -Y "frame.number==661" -T fields -e frame.cap_len -e frame.len`);
frames 565/575/580 (RFCOMM control-channel UIH frames) show the same 15-byte cap against a 17-byte
original length. This is the same limitation `CAP-012-FINDINGS.md` §1 documented for that session's
`btsnooz`-fallback log (this folder's file is likewise named `CAP-013-btsnooz_hci.log`, not
`-btsnoop_hci.log`). **Consequence:** HCI-event-level sequencing (pairing state machine, RFCOMM
channel-open/close via SABM/UA, which DLCI opened when) is unaffected, since those are short control
frames within the 15-byte cap — but any RFCOMM data payload longer than ~15 bytes (DLCI `0x08`'s
Group/Code/Length/Value envelope beyond the first 2 bytes, DLCI `0x02`'s HDLC frame bodies, DLCI `0x04`'s
longer Message Stream payloads) is truncated and cannot be decoded past that point in this capture.

## 2. Classic BR/EDR link establishment — fresh pairing path confirmed again (🟢 FACT)

The full sequence matches `PROTOCOL.md` §5.1's already-FACT **"Fresh pairing (no stored key)"** path
exactly, frame-for-frame in kind (not merely "similar"):

| Step | Event | Frame | Time (+0200) |
|---|---|---|---|
| 1 | `Sent Delete Stored Link Key` (target `04:00:6e:cf:6e:07`) | 117 | 17:11:46.737043 |
| 2 | `Sent Create Connection` | 119 | 17:11:46.737892 |
| 3 | `Rcvd Connect Complete` (status `0x00`) | 133 | 17:11:47.281435 |
| 4 | `Rcvd Link Key Request` | 149 | 17:11:47.301364 |
| 5 | `Sent Link Key Request **Negative** Reply` | 150 | 17:11:47.303969 |
| 6 | `Rcvd IO Capability Request` → `Sent IO Capability Request Reply` | 152/153 | 17:11:47.306228/306989 |
| 7 | `Rcvd IO Capability Response` | 169 | 17:11:47.414681 |
| 8 | `Rcvd User Confirmation Request` → `Sent User Confirmation Request Reply` | 219/227 | 17:11:48.005302/48.199051 |
| 9 | `Rcvd Simple Pairing Complete` | 229 | 17:11:48.525367 |
| 10 | `Rcvd Link Key Notification` (new key stored) | 230 | 17:11:48.561467 |
| 11 | `Rcvd Authentication Complete` | 231 | 17:11:48.561776 |
| 12 | `Sent Set Connection Encryption` → `Rcvd Encryption Change` | 247/270 | 17:11:48.587762/48.843158 |

**Command/hex evidence** (`PROJECT_RULES.md` §1 rule 4a):
`tshark -r CAP-013-btsnooz_hci.log -Y "frame.number==117" -x` →
`0000  01 12 0c 07 07 6e cf 6e 00 04 00` (HCI command `0x0c12` Delete Stored Link Key, `BD_ADDR` field
`07 6e cf 6e 00 04` little-endian-reversed = `04:00:6e:cf:6e:07`, matching the Buds' known address).
`tshark -r CAP-013-btsnooz_hci.log -Y "frame.number==119" -x` →
`0000  01 05 04 0d 07 6e cf 6e 00 04 18 cc 01 00 54 8f 01` (HCI command `0x0405` Create Connection,
same `BD_ADDR`).

**Conclusion, no PAIR-004 ambiguity:** `Sent Link Key Request Negative Reply` at step 5 is the
discriminating event — a stored-key reuse (as `CAP-001-FINDINGS.md` §1's reconnect path showed) replies
to the Link Key Request with the *positive* `Link Key Request Reply` and skips IO Capability/SSP/User
Confirmation entirely. Here, every SSP sub-step (IO Capability exchange, User Confirmation, Simple
Pairing Complete, a **new** Link Key Notification) is present and observed. **This re-pairing used a
completely fresh classic SSP handshake, not a reused link key** — this is the answer to `PAIR-004`'s
question for whatever was in effect immediately before this capture's own window started. It does not,
and cannot, speak to what existed before the un-logged clearing action itself (§0 above).

## 3. RFCOMM channel topology this session (🟢 FACT for topology; per-channel content status below)

Session-local RFCOMM server-channel numbers (per `PROTOCOL.md` §2's note that these are session-local,
unlike GATT handles) mapped to their DLCIs, opened via `SABM`/`UA` starting ~1.3s after Encryption
Change:

| Server channel | DLCI | Opened (frame, time +0200) | Content this session | Status |
|---|---|---|---|---|
| 0 | `0x00` | 539/549, 17:11:50.113/118 | RFCOMM multiplexer control (PN negotiation) | 🟢 FACT |
| 6 | `0x0c` | 556/564, 17:11:50.124/152 | HFP AT-command traffic — ASCII `AT` (`0x41 0x54`) and `\r\n` (`0x0d 0x0a`) prefixes visible before truncation, e.g. frame 597 (`4154`) and frame 600 (`0d0a2b`, `\r\n+`, consistent with an unsolicited result code such as `+CIEV`/`+BIEV`) | 🟢 FACT that this is the HFP AT channel (per `PROTOCOL.md` §4.3 Option C's established pattern); full command/argument content not recoverable this session due to truncation (§1) |
| 4 | `0x08` | 559/574, 17:11:50.143/165 | Private `[Group][Code][Length][Value]` envelope (`PROTOCOL.md` §2.3) — Groups `0x04`,`0x03`,`0x09`,`0x0e` observed by their first 2 bytes; see §4 below | 🟢 FACT for channel identity/topology; 🔴 most payload content truncated |
| 5 | `0x0a` | 596/605, 17:11:50.209/217 | Opens, no payload observed — consistent with `CAP-001-FINDINGS.md` §6's finding that this DLCI stays silent (SCO/eSCO ruled out there) | 🟢 FACT (silence reproduced a 5th time) |
| 2 | `0x04` | 785/787, 17:11:51.780/785 | Official Fast Pair Message Stream — Device Information (Group `0x03`) and Hearable Controls ANC Get (Group `0x08` Code `0x11`) observed; see §4 | 🟢 FACT for channel identity |
| 1 | `0x02` | 1170/1172, 17:12:51.981/985 | `libmaestro` candidate Pigweed `pw_hdlc` channel — opens **~61s later** than the other four, not during the initial multiplexer burst; see §5 | 🟢 FACT for channel identity/framing (per `PROTOCOL.md` §2.2a); 🟡 timing observation below is new and single-sample |

**Command:** `tshark -r CAP-013-btsnooz_hci.log -Y "btrfcomm.frame_type == 0x2f or btrfcomm.frame_type == 0x63" -T fields -e frame.number -e frame.time -e btrfcomm.dlci -e btrfcomm.channel -e _ws.col.info`

## 4. DLCI 0x08 / DLCI 0x04 content — Group/Code visible, Value truncated (🟢 FACT for presence, 🔴 OPEN for full values)

**Command:** `tshark -r CAP-013-btsnooz_hci.log -Y "btrfcomm.dlci==0x08 and btrfcomm.len>0" -T fields -e frame.number -e frame.time_epoch -e frame.len -e data.data`

Representative hex (first bytes only, per §1's truncation):

```
622  17:11:50.2315  len=18  data=05 0c
635  17:11:50.2543  len=18  data=0e 04
658  17:11:50.2798  len=44  data=0e 02
661  17:11:50.2874  len=53  data=0e 01
735  17:11:51.3920  len=27  data=08 80 7d
1423 17:12:40.2590  len=43  data=0e 02 00
1425 17:12:40.2623  len=52  data=0e 01 00
```

`Group 0x0e Code 0x01`/`0x02` traffic (per `ADR-014`, the confirmed per-earbud+case battery push) is
**present** at connection time (frames 635/658/661) and again twice more later in the session (frames
1423/1425, 1450/1451, 1580/1582) — consistent with a battery push recurring around the 17:13:47
on-screen "Right 100% / Case 13% / Left 100%" reading. **The Length and Value bytes are truncated in
every one of these frames** (`frame.cap_len` = 15 throughout, per §1), so the specific
`[value, flag, index]` triplets cannot be decoded and cross-checked against the on-screen percentages
this session — recorded as 🔴 OPEN for the *specific values* this session, while the *presence* of this
message type at connection time is 🟢 FACT, consistent with prior captures.

On DLCI `0x04`, frame 804 (`ef 09 08 11 00`, RFCOMM UIH payload `08 11 00`) is a `Group 0x08 Code 0x11`
frame — the confirmed ANC **Get** request/response per `PROTOCOL.md` §4.1/`ADR-009`. This is consistent
with an automatic state query on connect, not evidence of a user-initiated ANC change (no ANC action was
performed on screen this session).

## 5. DLCI 0x02 (`libmaestro` candidate) opens ~61s after the other channels (🟡 HYPOTHESIS, single sample)

Unlike `CAP-001`–`CAP-004`/`CAP-006` (where DLCI `0x02` opens in the same initial multiplexer burst as
the other channels, `PROTOCOL.md` §2.2a), this session's DLCI `0x02` `SABM`/`UA` exchange (frames
1170/1172) happens at 17:12:51.981–985 — **~61 seconds after** the other four channels opened
(17:11:50.1–51.8). This timing lines up closely with the on-screen "Allow" tap on the Pixel Buds app's
"find your Pixel" permission screen at 17:12:52 (`CAP-013-EVENT-NOTES.md`'s timeline). **🟡 HYPOTHESIS
(single sample, not promoted):** DLCI `0x02` may be opened by the Pixel Buds companion app itself once
it reaches a specific point in its setup flow (e.g. once its own permissions are granted), rather than
being opened unconditionally as part of the RFCOMM multiplexer's initial session setup as seen in
earlier captures. This is a new, previously unrecorded observation — flagged here for a future capture
to test directly (e.g. repeat Group A and note precisely when each permission screen is dismissed
relative to DLCI 0x02's `SABM`), not treated as confirmed from one occurrence.

## 6. Second BLE link to an unrelated random address (🔴 OPEN QUESTION, not attributed)

At 17:11:52.463 (frame 851), a second, independent `LE Enhanced Connection Complete` is observed — to
`43:8a:82:03:4b:f2`, a **random/resolvable** address distinct from the Buds' public classic address
(`04:00:6e:cf:6e:07`). This happens *after* the classic link and all RFCOMM channels except DLCI `0x02`
are already open. **No evidence in this capture identifies this address as belonging to the Buds** (no
address-resolution event, no correlated content) — recorded as 🔴 OPEN QUESTION rather than assumed to
be a Buds-side private address, per this session's zero-creativity rule. Plausible candidates (Buds'
own GATT-side private address per Fast Pair's typical design; an unrelated nearby device) are not
distinguished by the evidence in hand.

## 7. Hypothesis test record (`PROJECT_RULES.md` §4, rule 10/11 template)

- **Hypothesis:** the classic-link re-pairing following this session's clearing action ("Reset
  Bluetooth & Wi-Fi") shows a fresh SSP/link-key handshake (matching `PROTOCOL.md` §5.1's "no stored
  key" path), not a reused link key — i.e. the clearing action worked, at least for whatever bonding
  state existed *immediately before this capture's logged window began*.
- **Setup:** Pixel 7a, Android 17, firmware `release_5.203`, official Pixel Buds companion app (version
  not screen-confirmed). HCI snoop logging enabled before the pairing tap but — per §0's deviation —
  **not** before the clearing action itself or the subsequent case-open/pair-button/discovery sequence.
  Capture method: `btsnooz`-fallback extraction (severely ACL-truncated, ~15-byte cap, §1).
- **Expected outcome:** `Delete Stored Link Key` → `Create Connection` → `Connect Complete` →
  `Link Key Request` → **Negative** Reply → IO Capability exchange → Simple Pairing Complete →
  **new** Link Key Notification → Authentication Complete → Set Connection Encryption → Encryption
  Change (per `PROTOCOL.md` §5.1's documented fresh-pairing template).
- **Actual outcome:** exact match, frame-for-frame in kind — see §2's table (frames 117–270,
  17:11:46.737–48.843). No stored-key-reuse path (`Link Key Request` → positive `Reply`, no SSP) was
  observed anywhere in this log.
- **Conclusion:** 🟢 **CONFIRMED** for the secondary (`PAIR-004`) question, scoped exactly as stated
  above — fresh SSP occurred for the bonding state active at the moment this capture's window began.
  🔴 **NOT ANSWERED** for the primary question (whether a BLE link/valid key existed *before* the
  17:09:24 clearing action) — that state is entirely outside this capture's logged window (§0). This is
  not a failed or inconclusive test of the primary hypothesis; it is a test that was never actually run
  against it, because the logging gap made the primary hypothesis untestable from this data, and that
  distinction is recorded here rather than blurred.

## 8. Other open questions raised by this capture

- DLCI `0x02`'s delayed opening (§5) — is this driven by the companion app's setup-flow state, or
  coincidental to this session? Needs a repeat with precise screen-to-SABM timing as the explicit goal.
- The second BLE link to a random address (§6) — is this the Buds' own GATT-side identity, or an
  unrelated device? Needs an explicit BLE-address-resolution check (e.g. via `Identity Address` in a
  Pairing/Bonding event, if one appears later in the log beyond this capture's own end, or a dedicated
  capture correlating this address against a known Buds BLE MAC from another tool).
- A genuine repeat of this capture's original intent (logging started **before** the clearing action,
  not just before the pairing tap) is still needed — see the proposal in
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Group-A-repeat note (updated alongside this findings file).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-013-2026-08-26_17-09-01_17-14-04-Group_A/CAP-013-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-013-2026-08-26_17-09-01_17-14-04-Group_A/CAP-013-FINDINGS
