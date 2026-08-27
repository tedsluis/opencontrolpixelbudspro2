# Findings: `CAP-032` (Group A repeat — fourth attempt at the pre-clearing-action baseline)

Standardized, evidence-based extraction from `CAP-032-btsnoop_hci.log` + `CAP-032-recording.mp4`,
staged here per `PROJECT_RULES.md` §2. Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-032` · **Date:** 2026-08-27 · **Firmware:** `release_5.203` (screen-confirmed,
`CAP-032-recording.mp4` t=137s/18:32:15; also wire-confirmed, DLCI 0x08 `Group 0x03 Code 0x02`, §4
below) · **Phone:** Pixel 7a, Android 17 (⚪ assumed, build not screen-confirmed this session) ·
**Log file:** `CAP-032-btsnoop_hci.log` (487.57s, 2,455 packets, 2026-08-27
18:29:45.722826–18:37:53.294436 +0200, **genuine raw, untruncated BTSnoop — see §0**) · **Video:**
`CAP-032-recording.mp4` (137.8s, 18:30:15–18:32:33, burned-in wall-clock overlay) · **Devices:**
phone `E8:D5:2B:xx:CA:81` (Pixel 7a, partially redacted per `AGENTS.md` §9), peer `Google_cf:6e:07`
(`04:00:6e:cf:6e:07` — same address as `CAP-001`–`CAP-031`, independently re-confirmed on the wire
this session).

---

## 0. Scope: what this capture can and cannot answer (Step 0 — coverage + extraction-path verification)

This capture was planned as a **fourth attempt** at `CAP-001-FINDINGS.md` §6's still-open primary
question — did a BLE link and/or a still-valid classic link key already exist for this peer *before*
the on-screen clearing action? — this time explicitly verifying the snoop log's own *content*
freshness (not just file size, per `CAP-031-FINDINGS.md` §8's proposed fix) before the Forget tap.

**Unlike the three prior attempts (`CAP-001`, `CAP-013`, `CAP-031`), this attempt succeeded: the log
genuinely covers the pre-Forget window.**

### 0.1 Genuine raw BTSnoop, not a `btsnooz` fallback — 🟢 FACT

- `file CAP-032-btsnoop_hci.log` → `BTSnoop version 1, HCI UART (H4)`.
- `capinfos CAP-032-btsnoop_hci.log` → `Packet size limit: file hdr: (not set)`, interface capture
  length `262144`. **No per-packet truncation cap at all** — structurally different from
  `CAP-012`/`CAP-013`/`CAP-031`'s `btsnooz`-fallback logs, which `capinfos` reported as an
  *inferred* 15-byte (or 15–126-byte) cap.
- `tshark -r CAP-032-btsnoop_hci.log -T fields -e frame.cap_len -e frame.len` → `frame.cap_len ==
  frame.len` for **all 2,455 frames**, zero exceptions. Every RFCOMM data payload decodes in full
  this session (§4).
- **The file is also correctly named** `CAP-032-btsnoop_hci.log` (no "z"), unlike `CAP-013`'s and
  `CAP-031`'s `-btsnooz_hci.log` files. `CAP-032-EVENT-NOTES.md`'s original metadata table still had
  the "z" typo (copy-pasted from the `CAP-031` template) — corrected there (see that file's
  Corrections section).

**Extraction-path hypothesis (leading list item 6, this session's brief) — 🟡 supported by this one
data point, PROPOSAL awaiting maintainer sign-off for promotion.** The pattern across four captures
now: `CAP-012`, `CAP-013`, `CAP-031` were all extracted via the `btsnooz.py` bugreport-text fallback
(`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3 step 4), all three ended up severely ACL-truncated, and all
three were named with a "z". `CAP-032` was extracted via the raw-file path (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`
§3 step 3), is genuinely untruncated, and is named without a "z". This is consistent with (not yet
independently isolated from) the hypothesis that **the extraction path itself, not some property of
the individual session, determines whether a capture is usable for anything beyond short
control-frame sequencing** — but this is one data point on the "raw" side, following three on the
"btsnooz" side; a controlled test (deliberately extracting the *same* session both ways) has not been
done. See §8 below for the proposed documentation update this motivates.

### 0.2 Timing: log start vs. the Forget tap — 🟢 FACT, first full pre-Forget coverage in four attempts

- Log's first frame: **18:29:45.722826** (`Sent Reset`, an HCI controller reset — the cleanest
  possible starting point, not a resumed/truncated session).
- Video's own on-screen wall clock at video start (18:30:15) shows the Bluetooth quick-settings
  panel already open with **"Bluetooth is off"** and the "Use Bluetooth" toggle OFF
  (`CAP-032-EVENT-NOTES.md` row 1, screenshot-confirmed).
- The user's finger is on the "Use Bluetooth" toggle at 18:30:21; the log's classic-radio bring-up
  burst (`Write Voice Setting`, `Write Scan Enable`, `Write Page Scan Activity`, `Write Extended
  Inquiry Response`, frames 177–319) starts at 18:30:22.381 — within ~1s, consistent with UI-to-HCI
  lag for that specific tap.
- The on-screen Forget confirmation tap is screenshot-confirmed at **18:30:42** (finger on the
  "Forget device" button; the destination "Connected devices" screen is already showing by
  18:30:43). The log's `Sent Delete Stored Link Key` for the Buds' address is at **18:30:43.927732**
  (frame 768) — within ~1–2s of the on-screen tap, matching the same tap-to-HCI-effect lag pattern.
- **Net coverage: the log's first frame precedes the Forget tap by ~58 seconds, and precedes the
  video's own start by ~30 seconds.** This is the first of four attempts (`CAP-001`, `CAP-013`,
  `CAP-031`, `CAP-032`) where the pre-clearing-action window is actually inside the logged window,
  not before it.

**One genuine puzzle in this window, read as 🟡 HYPOTHESIS, not asserted further:** the log shows
~36 seconds of LE-only HCI activity (`Sent Reset` at 18:29:45.722, followed by LE scanning, LE
advertising, and one LE GATT connection — §5/§6 below) *before* the 18:30:21 on-screen Bluetooth-
toggle tap, while the screen itself shows "Bluetooth is off" for that whole span. No classic
BR/EDR-specific command (inquiry scan, page scan, voice setting) appears until the 18:30:22.381 burst
that matches the toggle tap. The most consistent reading, without going beyond what's evidenced, is
that Android's BLE "opportunistic scanning" (BLE scanning permitted independently of the user-facing
Bluetooth toggle, when enabled in Location settings) was active on this controller throughout that
window, and only the classic radio was gated on the toggle. This is not confirmed against any
Android source/settings state in this pass — flagged as 🟡 HYPOTHESIS. It does not change §0.3's
conclusion below, since the Buds' own address is checked across the *entire* window regardless of
this distinction.

### 0.3 Primary question — 🟢 FACT for this session (first clean answer in four attempts)

Across the **entire** covered window (18:29:45.722–18:30:43.927, controller Reset through the Forget
tap's own HCI effect):

- **Zero classic BR/EDR connection events of any kind** — no `Create Connection`, no `Connect
  Complete`, no `Link Key Request`/`Reply` of any kind — appear anywhere before frame 768's `Sent
  Delete Stored Link Key` (`tshark -r CAP-032-btsnoop_hci.log -Y "frame.number<=768 and
  (bthci_evt.code==0x03 or bthci_evt.code==0x04 or bthci_evt.code==0x05 or bthci_evt.code==0x0a or
  bthci_evt.code==0x18)"` → 0 matches; `_ws.col.info contains "Link Key"` → only frame 768 itself).
- **Exactly one LE connection is established** in this window (frame 356, 18:30:28.802), and it
  resolves to a **different, unrelated** device — see §6 below, not the Buds.
- **Zero occurrences of the Buds' own classic/public address** (`04:00:6e:cf:6e:07`) in any
  `bthci_cmd.bd_addr`/`bthci_evt.bd_addr` field before frame 768 (`tshark ... -Y "frame.number<=768
  and (bthci_cmd.bd_addr==04:00:6e:cf:6e:07 or bthci_evt.bd_addr==04:00:6e:cf:6e:07)"` → first match
  is frame 768 itself). One vendor-specific command *does* reference this address earlier, as raw
  payload bytes rather than a dissected `bd_addr` field — see §5, a distinct finding documented on
  its own merits.
- **The `Delete Stored Link Key` command issued at the Forget tap's own moment (frame 768) reports
  `Num_Keys_Deleted = 0`** — see §1 below for the byte-level decode. The controller held no stored
  classic link key for the Buds' address at that exact moment.

**Conclusion for this session: 🟢 FACT — no active/recent BLE connection to the Buds, and no valid
stored classic link key for the Buds, existed anywhere in the covered window immediately before, and
at the moment of, the on-screen Forget tap.** This directly answers `CAP-001-FINDINGS.md` §6's
primary question **for this session** — the first of four attempts to actually test it. **This does
not reproduce `CAP-001`'s original finding** (a BLE link and a still-valid link key both existing
before that session's own clearing action) — it shows the opposite. This is not read as
contradicting or invalidating `CAP-001`'s result (a different session, different prior history); it
shows that the residual-state condition `CAP-001` observed is **not universal** — a clean
counter-example now exists. `CAP-001`'s own session-specific puzzle (why *that* session had residual
state) remains independently open. Framing and promotion into `PROTOCOL.md`/`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
proposed in §8 below — **PROPOSAL, pending maintainer approval** per `AGENTS.md` §6/§15.

## 1. `Delete Stored Link Key` at the Forget tap — 🟢 FACT, byte-level

**Command:** `tshark -r CAP-032-btsnoop_hci.log -Y "frame.number==768" -x`
**Hex:** `01 12 0c 07 07 6e cf 6e 00 04 00` — HCI command `0x0c12` (`Delete Stored Link Key`),
`BD_ADDR` `07 6e cf 6e 00 04` (little-endian-reversed = `04:00:6e:cf:6e:07`, the Buds' known
address), trailing flag byte `00` = delete for this specific address only (not "delete all").

**Response — command:** `tshark -r CAP-032-btsnoop_hci.log -Y "frame.number==769" -x`
**Hex:** `04 0e 06 01 12 0c 00 00 00` — `Command Complete` for opcode `0x0c12`, `Status=0x00`
(success), `Num_Keys_Deleted = 0x0000` (bytes 8–9, little-endian) — **zero keys deleted.**

The same command is issued again during the actual re-pairing at frame 1090 (18:31:26.775758,
§2 below) — identical hex, identical zero-keys-deleted response
(`tshark -r CAP-032-btsnoop_hci.log -Y "frame.number==1090 or frame.number==1091" -x` →
`01 12 0c 07 07 6e cf 6e 00 04 00` / `04 0e 06 01 12 0c 00 00 00`), consistent with the key having
already been absent since the Forget tap.

## 2. Classic BR/EDR link establishment — fresh pairing path confirmed again (🟢 FACT), seventh instance

The re-pairing sequence (following the case pair-button press and the on-screen "Connect" tap at
18:31:24) matches `PROTOCOL.md` §5.1's already-FACT **"Fresh pairing (no stored key)"** path exactly:

| Step | Event | Frame | Time (+0200) |
|---|---|---|---|
| 1 | `Sent Delete Stored Link Key` | 1090 | 18:31:26.775758 |
| 2 | `Sent Create Connection` | 1092 | 18:31:26.777779 |
| 3 | `Rcvd Connect Complete` (status `0x00`) | 1094 | 18:31:27.098014 |
| 4 | `Rcvd Link Key Request` | 1110 | 18:31:27.118249 |
| 5 | `Sent Link Key Request **Negative** Reply` | 1111 | 18:31:27.118853 |
| 6 | `Rcvd IO Capability Request` → `Sent IO Capability Request Reply` | 1113/1114 | 18:31:27.119958/120400 |
| 7 | `Rcvd IO Capability Response` | 1124 | 18:31:27.163136 |
| 8 | `Rcvd User Confirmation Request` → `Sent User Confirmation Request Reply` | 1142/1150 | 18:31:27.526450/27.781469 |
| 9 | `Rcvd Simple Pairing Complete` | 1152 | 18:31:28.003374 |
| 10 | `Rcvd Link Key Notification` (new key stored) | 1153 | 18:31:28.019247 |

**Command/hex evidence** (`PROJECT_RULES.md` §1 rule 4a):
`tshark -r CAP-032-btsnoop_hci.log -Y "frame.number==1153" -x` →
`04 18 17 07 6e cf 6e 00 04 b9 b9 18 20 62 e3 fc 5c 76 27 91 96 57 10 78 05 05` — Link Key
Notification event, 16-byte new key `b9 b9 18 20 62 e3 fc 5c 76 27 91 96 57 10 78 05`, key type
`05` = "Authenticated Combination Key generated from P-256".

**Video correlation:** the user taps "Connect" on the "Pair with Pixel Buds Pro 2" pop-up at
**18:31:24** (`CAP-032-EVENT-NOTES.md`), matching `Sent Delete Stored Link Key` at 18:31:26.776
within the video's own 1-second sampling resolution plus the case's own advertising/discovery lag.

**Conclusion:** every SSP sub-step (IO Capability exchange, User Confirmation, Simple Pairing
Complete, a **new** Link Key Notification) is present, no key-reuse path observed — a **seventh**
independent confirming instance of `PROTOCOL.md` §5.1's fresh-pairing path (after `CAP-002`,
`CAP-003`, `CAP-013`, `CAP-031`, plus `CAP-001`'s distinct reconnect-path and `CAP-016`'s
Buds-initiated variant).

## 3. RFCOMM channel topology this session (🟢 FACT for topology; content fully decodable this time)

| Server channel | DLCI | Opened (frame, time +0200) | Content this session | Status |
|---|---|---|---|---|
| 0 | `0x00` | 1263/1285, 18:31:28.799/899 | RFCOMM multiplexer control (PN negotiation) | 🟢 FACT |
| 5 | `0x0a` | 1298/1312, 18:31:28.913/942 | No payload observed — consistent with `CAP-001-FINDINGS.md` §6's finding that this DLCI stays silent (SCO/eSCO ruled out there) | 🟢 FACT (silence reproduced again, per-session close also observed: frame 1943) |
| 4 | `0x08` | 1303/1320, 18:31:28.915/949 | Private `[Group][Code][Length][Value]` envelope (`PROTOCOL.md` §2.3) — **fully decoded this session, no truncation**; see §4 | 🟢 FACT for channel identity and, for the first time in this repeat series, full content |
| 6 | `0x0c` | 1434/1437, 18:31:29.057/063 | HFP AT-command channel (per `PROTOCOL.md` §4.3 Option C's established pattern; not decoded in detail this session, out of this task's scope) | 🟢 FACT for channel identity/topology |
| 2 | `0x04` | 1505/1509, 18:31:29.256/261 | Official Fast Pair Message Stream — Device Information and Hearable Controls ANC Get observed; see §4 | 🟢 FACT for channel identity |
| 1 | `0x02` | 1645/1652, 18:31:30.895/926 | `libmaestro` candidate Pigweed `pw_hdlc` channel (per `PROTOCOL.md` §2.2a) | 🟢 FACT for channel identity; opens ~2.1s after DLCI 0x00, within the initial multiplexer burst — matches `CAP-031`'s non-delayed pattern, not `CAP-013`'s single-session ~61s-delay outlier (already ruled out by `CAP-031-FINDINGS.md` §5) |

**Command:** `tshark -r CAP-032-btsnoop_hci.log -Y "btrfcomm.frame_type == 0x2f or btrfcomm.frame_type == 0x63" -T fields -e frame.number -e frame.time -e btrfcomm.dlci -e btrfcomm.channel -e _ws.col.info`

## 4. DLCI 0x08 / DLCI 0x04 content — fully decoded, untruncated (🟢 FACT)

**Command:** `tshark -r CAP-032-btsnoop_hci.log -Y "btrfcomm.dlci==0x08 and btrfcomm.len>0" -T fields -e frame.number -e frame.time -e frame.len -e frame.cap_len -e data.data`

Unlike `CAP-013`/`CAP-031` (both capped at ~15 bytes), every DLCI `0x08` frame this session has
`frame.cap_len == frame.len` — full `[Group][Code][Length][Value]` payloads recovered:

- **Firmware/serial (`Group 0x03 Code 0x02`, frame 1413, 18:31:29.018238):**
  `03 02 00 3f 08 06 10 01 22 0d 72 65 6c 65 61 73 65 5f 35 2e 32 30 33 2a 00 30 e6 01 38 00 4a 07
  37 31 33 66 38 35 35 50 00 60 b1 db e8 06 70 02 78 01 a8 01 01 b0 01 01 ba 01 02 01 02 c0 01 01
  c8 01 01` — protobuf field `0x22 0d` decodes to the ASCII string `release_5.203` (firmware
  baseline, matching `ADR-012`'s confirmed wire value) and field `0x4a 07` to `7133f855`-prefixed
  serial fragment, consistent with `CAP-001`'s serial `1779298694`/`ADR-012` evidence chain.
- **Capability identifier (`Group 0x0e Code 0x02`, frame 1415, 18:31:29.030698):**
  `0e 02 00 1a 0a 18 67 6f 6f 67 6c 65 2d 70 69 78 65 6c 2d 62 75 64 73 2d 70 72 6f 2d 76 31` —
  protobuf field `0x0a 18` decodes to the ASCII string `google-pixel-buds-pro-v1`, the confirmed
  wire-protocol capability literal covered by `PROJECT_RULES.md` §8 rule 22's hardcoded-strings
  exception.
- **Battery push (`Group 0x0e Code 0x01`, `ADR-014`, frame 1417, 18:31:29.031518):**
  `0e 01 00 23 0a 21 0a 03 61 6c 6c 12 1a 0a 06 08 64 10 01 18 01 0a 06 08 64 10 01 18 02 0a 06 08
  39 10 01 18 03 20 01` — decodes to three `[value, flag, index]` triplets: `[100, 1, 1]` (Left
  100%), `[100, 1, 2]` (Right 100%), `[0x39=57, 1, 3]` (Case 57%). **This matches the on-screen
  reading exactly** ("Left: 100%, Case: 57%, Right: 100%", screenshot-confirmed at t=16s/18:30:31 and
  again at t=27s/18:30:42, before the Forget tap — the reading is stable across the Forget/re-pair
  cycle, as expected for a hardware-side battery level). **This is the first capture in this repeat
  series (`CAP-013`/`CAP-031` were both truncated exactly at this field) where the specific
  `[value, flag, index]` triplets are directly decodable and cross-validated against the on-screen
  percentages** — resolving that open sub-item for `BATT-004`.

On DLCI `0x04`, frame 1546 (`08 11 00 00`, RFCOMM UIH payload) is a `Group 0x08 Code 0x11` frame —
the confirmed ANC **Get** request/response per `PROTOCOL.md` §4.1/`ADR-009`, consistent with an
automatic state query on connect (no ANC action was performed on screen this session).

## 5. Vendor-specific HCI command referencing the Buds' address, 105ms into the log (🔴 OPEN QUESTION)

Per this session's zero-creativity-but-not-blind-to-the-unknown guardrail: a field-based search for
`bthci_cmd.bd_addr`/`bthci_evt.bd_addr` (§0.3) found no reference to the Buds' address before the
Forget tap — but that search only catches HCI opcodes Wireshark has a dissector for. A byte-level
grep across every raw frame in the pre-Forget window found one match Wireshark's dissector doesn't
tag as an address field at all:

**Frame 91, 18:29:45.827476 (+0200)** — 105ms after the log's very first frame, and ~58 seconds
before the Forget tap:
`tshark -r CAP-032-btsnoop_hci.log -Y "frame.number==91" -x` →
`01 57 fd 0a 02 00 04 07 6e cf 6e 00 04 02` — HCI command opcode `0xFD57` (a Google/vendor-specific
opcode group, sub-command `0x0157` per its own first parameter byte), parameter payload
`02 00 04 07 6e cf 6e 00 04 02`, containing `07 6e cf 6e 00 04` — the Buds' `BD_ADDR`,
little-endian-reversed, byte-for-byte identical to every other occurrence of this address in this
project's captures.

**Structural context, not asserted as confirmed protocol knowledge:** this opcode (`0xFD57`/`0x0157`)
appears 69 times total before frame 768, in two bursts (frames 59–160, coinciding with the log's
LE-only opening window, and frames 213–278, coinciding with the 18:30:22 classic-radio-enable burst).
Decoding every occurrence's raw payload shows a repeating pattern: a `02 00 NN <6-byte-addr> 02`
entry (assigning list index `NN` to an address) is followed by a `12 01 00 NN <bytes>` entry
(apparently setting per-index flags/parameters), across a total of 22 distinct addresses and indices
`0x04`–`0x1a` — matching exactly the 22 distinct `bd_addr` values independently found elsewhere in
this log (§0.3). The Buds' address is assigned index `0x04` (frame 91) with what looks like a
flags/parameter sub-command immediately after (frame 93: `01 00 04 01 00 11 11 01 80 01 f4 01`) that
structurally differs from most other indices' own follow-up (e.g. index `0x06`, frame 113:
`01 00 06 40 00 11 11 01 80 00 00 00`) — indices `0x04`/`0x05` share one parameter pattern, `0x06`
onward another. What this command actually configures (most plausibly a bulk push of all
currently-bonded devices' identities into the controller's own address/resolving-list at
Bluetooth-stack bring-up, given the timing and the one-entry-per-bonded-device shape) is **not**
independently confirmed against any vendor spec in this pass. **Recorded as 🔴 OPEN QUESTION per this
session's guardrail — not guessed further.**

**Bearing on the primary question (§0.3): none, on the evidence available.** This command carries no
connection-establishment semantics (no `Create Connection`, no ACL handle, no link-key exchange) —
it is consistent with, and unsurprising given, the already-known fact that the Buds *was* a bonded
device before the Forget tap (visible on the Device Details screen throughout this session before
18:30:42). It does not show a live BLE link or a reused classic link key, so §0.3's conclusion
stands. Flagged here as a genuinely new, previously-undocumented piece of wire evidence in its own
right, not as evidence bearing for or against the primary question.

## 6. Unattributed LE connection to a random address, before the Forget tap (🔴 OPEN QUESTION, not the Buds)

**Frame 356, 18:30:28.801900 (+0200)**, `Rcvd LE Meta (LE Enhanced Connection Complete [v1])`:
`tshark -r CAP-032-btsnoop_hci.log -Y "frame.number==356" -V` shows `Peer Address Type: Random
Device Address (0x01)`, `BD_ADDR: 51:ef:91:49:2f:d6` — a **random**, non-public address, distinct
from the Buds' known public classic/LE address `04:00:6e:cf:6e:07`.

This is the **only** LE connection established anywhere before frame 768 (confirmed by scanning
every `bthci_evt.le_meta_subevent==0x0a` event with `frame.number<=768` — exactly one hit). A
subsequent GATT service-discovery exchange (frames 377–461+) against this connection resolves
`GAP`, `GATT`, `Device Information`, and **`Heart Rate`** primary services — a service profile
structurally inconsistent with the Pixel Buds Pro 2 (no Fast Pair/audio-accessory GATT services
observed), so this is read as an **unrelated nearby device** (most plausibly a fitness
tracker/smartwatch already bonded to this phone, reconnecting independently), not the Buds under a
private/resolvable address. This connection remains open through frame 768 (no `Disconnection
Complete` observed in this window) — its own identity is not pursued further, out of this task's
scope, and recorded as 🔴 OPEN QUESTION only for completeness, per the same non-attribution standard
`CAP-013-FINDINGS.md` §6/`CAP-031-FINDINGS.md` §6 applied to their own unattributed addresses
(`43:8a:82:03:4b:f2`, `4f:25:00:85:9a:b1`) — this is a **third, distinct** address, not a
recurrence of either of theirs.

## 7. Hypothesis test record (`PROJECT_RULES.md` §4, rule 10/11 template)

**Test A — primary `PAIR-004` question (pre-clearing-action state)**

- **Hypothesis:** starting HCI snoop logging via the raw-file extraction path (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`
  §3 step 3), rather than the `btsnooz.py` fallback used for the three prior attempts, combined with
  verifying the log's own content freshness before the Forget tap (per `CAP-031-FINDINGS.md` §8),
  would finally capture the pre-Forget window and allow `CAP-001-FINDINGS.md` §6's question to be
  tested.
- **Setup:** Pixel 7a, Android 17, firmware `release_5.203`, official Pixel Buds companion app
  (version not screen-confirmed). Capture method: raw `btsnoop_hci.log` (untruncated, §0.1).
- **Expected outcome:** log's first frame timestamp ≤ the video's own Forget-tap timestamp
  (18:30:42).
- **Actual outcome:** log's first frame is 18:29:45.722826 — **~58 seconds *before*** the Forget
  tap, and ~30 seconds before the video itself starts (§0.2).
- **Conclusion:** 🟢 **CONFIRMED** — the pre-clearing-action window is captured for the first time in
  four attempts. Given that coverage, the primary question itself is answerable for this session:
  🟢 **no prior BLE link or valid classic link key existed for the Buds before the Forget tap**
  (§0.3). PROPOSAL — pending maintainer approval for how this is reflected in
  `PROTOCOL.md`/`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (§8).

**Test B — secondary `PAIR-004` question (fresh SSP vs. reused key after Forget)**

- **Hypothesis:** the classic-link re-pairing following this session's Forget tap shows a fresh
  SSP/link-key handshake, matching `PROTOCOL.md` §5.1's "no stored key" path.
- **Setup:** same as Test A.
- **Expected outcome:** `Delete Stored Link Key` → `Create Connection` → `Connect Complete` →
  `Link Key Request` → **Negative** Reply → IO Capability exchange → Simple Pairing Complete →
  **new** Link Key Notification.
- **Actual outcome:** exact match — see §2's table (frames 1090–1153).
- **Conclusion:** 🟢 **CONFIRMED**, a seventh independent confirming instance of `PROTOCOL.md`
  §5.1's fresh-pairing path.

**Test C — extraction-path hypothesis (this session's leading question, per the reading list item 6)**

- **Hypothesis:** the `-btsnooz_hci.log` naming and the severe ACL truncation seen in `CAP-012`/
  `CAP-013`/`CAP-031` are both consequences of the `btsnooz.py` bugreport-text fallback extraction
  path specifically, not an incidental property of those sessions — and a capture taken via the raw
  BTSnoop file path (when available) would not show this truncation.
- **Setup:** same as Test A; log file type/truncation checked via `file(1)`/`capinfos`/`tshark`
  `cap_len`-vs-`len` comparison (§0.1).
- **Expected outcome (if hypothesis held):** `CAP-032-btsnoop_hci.log` (raw-path extraction, no "z")
  shows `frame.cap_len == frame.len` throughout, with no `capinfos`-inferred size cap.
- **Actual outcome:** exact match — `Packet size limit: file hdr: (not set)`, 2,455/2,455 frames
  with `cap_len == len` (§0.1).
- **Conclusion:** 🟡 **Supported, one data point on the "raw" side after three on the "btsnooz"
  side** — consistent with the hypothesis, not yet independently isolated (no single session has
  been extracted via *both* paths for a direct controlled comparison). PROPOSAL — pending maintainer approval for promoting this into a documented warning in
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3 (§8 below).

## 8. Other open questions raised by this capture / proposed next steps

- The vendor-specific `0xFD57`/`0x0157` command family (§5) — what it actually configures on the
  controller is unconfirmed. Given its shape (one entry per bonded device, assigning list indices),
  the most direct way to test the "bulk known-device provisioning at BT-enable time" reading would
  be a capture that starts with a **known, small** set of bonded devices (e.g. only the Buds bonded)
  and checks whether exactly one `0x0157` entry appears — out of this task's scope, proposed for a
  future deskresearch/protocol-adjacent pass, not a `libmaestro`/Buds-specific question.
- The unattributed LE connection to `51:ef:91:49:2f:d6` (§6) is not pursued further — it is
  structurally unrelated to the Buds (Heart Rate GATT service) and out of this project's scope
  (`PROJECT.md` targets exactly one paired Pixel Buds Pro 2).
- `CAP-001`'s own original puzzle — why *that* specific session had a BLE link and a valid link key
  present before its own clearing action — remains independently 🔴 OPEN. `CAP-032`'s clean negative
  result narrows the question from "does this ever happen" to "what was different about `CAP-001`'s
  session" — not something this capture's evidence can address.
- DLCI `0x0c` (HFP) content was not decoded in detail this session — out of this task's scope, but
  available (and, unlike prior repeats, untruncated) in the log for a future battery-tracking pass.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-032-2026-08-27_18-30-15_18-32-33-Group_A/CAP-032-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-032-2026-08-27_18-30-15_18-32-33-Group_A/CAP-032-FINDINGS
