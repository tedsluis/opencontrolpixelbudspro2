# Findings: `CAP-012` (Group S repeat — clean, system-settings-only procedure, no BLE tool)

Standardized, evidence-based extraction from `CAP-012-btsnooz_hci.log` + `CAP-012-recording.mp4`,
staged here for later promotion directly into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Modeled on
`captures/CAP-001-2026-08-09_08-51-00_08-52-20-Group_Z/CAP-001-FINDINGS.md`. Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-012` · **Date:** 2026-08-26 · **Firmware:** ⚪ ASSUMPTION —
`release_5.203` carried over from `PROTOCOL.md` §0.1's UI-baseline; **not independently
re-confirmed on-the-wire this session** (see §1 — DLCI 0x08 opens and carries the usual
`Group 0x03 Code 0x02` message shape, but its value bytes are truncated away before the
`"release_5.203"` string would appear) and not shown in any on-screen UI this session (no
Pixel Buds app, and the system Bluetooth "Device details" page shows no firmware field, matching
`CAP-004-FINDINGS.md` §7's identical observation). **Phone:** Pixel 7a, Android ⚪ 17 (assumed,
same physical phone as `CAP-001`–`CAP-011`; not shown on screen this session). **Google Play
Services disabled, Pixel Buds app uninstalled; paired via system Bluetooth settings only — no
BLE tool (nRF Connect or otherwise) used at any point**, confirmed both from the video (only the
stock Android Bluetooth-settings UI ever appears on screen) and from the wire log (§2 — zero LE
connection-complete events of any kind). **Log file:** `CAP-012-btsnooz_hci.log` (542.2s,
1,436 packets, 2026-08-26 15:25:52.11–15:34:54.33 local/+0200 — a `btsnooz`-extracted log, **not**
the raw untruncated `btsnoop_hci.log` path; see §1's capture-integrity finding, which materially
limits §4/§5 below). **Video:** `CAP-012-recording.mp4` (128.8s, 15:30:19–15:32:28 local,
on-screen wall-clock overlay). **Devices:** phone `Google_7e:ca:81`
(`E8:D5:2B:7E:CA:81` per the on-screen "Phone's Bluetooth address" field, frame captured at
15:30:57), peer `Google_cf:6e:07` (`04:00:6E:CF:6E:07`, the Buds/case) — **independently
re-verified for this session** (guardrail requirement, not carried over from `CAP-004`): every
`Create Connection`/`Connect Complete` event in this log names this exact address (frames
459/463/1001/1003, `tshark -r CAP-012-btsnooz_hci.log -Y "bthci_cmd.opcode==0x0405 or bthci_evt.code==0x03" -T fields -e frame.number -e bthci_cmd.bd_addr -e bthci_evt.bd_addr`).

**Stated goal of this session** (per `CAP-012-EVENT-NOTES.md`'s header and
`CAP-004-FINDINGS.md` §8 item 4 / `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Group-S-repeatability note):
isolate whether `CAP-004`'s Cross-Transport-Key-Derivation (CTKD) bonding result was an artifact
of nRF Connect's early BLE connection, or a genuine effect of the GMS-disabled/no-app condition
itself, by repeating Group S's procedure exactly (system Bluetooth settings only, no BLE tool at
any point). **§2 gives a clean answer.** The `GFPS-001` result itself (Fast Pair Message Stream
content present/absent) was not expected to change and — subject to the truncation caveat in
§1 — does not appear to (§4).

---

## 1. Capture-integrity finding: this log is severely ACL-truncated (`btsnooz`, ~15-byte snaplen) — 🟢 FACT, materially limits §4/§5

**This must be read before §4/§5 below**, per the same "the wire capture cannot answer its own
question" pattern already documented for BLE in `CAP-017-FINDINGS.md` §2 — this capture shows the
**classic BR/EDR** side of the identical phenomenon.

The file is named `CAP-012-btsnooz_hci.log` (not `-btsnoop_hci.log` like every other capture in
this project) and `capinfos` confirms it: `File type: Symbian OS btsnoop`,
`Packet size limit: inferred: 15 bytes - 126 bytes (range)`, average packet size 28 bytes over
1,436 packets in a 542s session — far short of what a full, untruncated snoop log of this
duration would contain. Per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3, this is the signature of the
**§3-step-4 fallback path** (`btsnooz.py` decoding an `adb bugreport`'s embedded, already
device-truncated snoop buffer) rather than the **§3-step-3 preferred path** (pulling a raw,
untruncated `btsnoop_hci.log` directly from the bugreport's `FS/` tree) that `CAP-001`–`CAP-011`
used.

**Directly verified, not inferred:** every `HCI_ACL` (data-carrying) packet in this log is capped
at a small captured length regardless of its true, declared length — e.g. frame 550 declares
`frame.len=684` but has `frame.cap_len=15`; frame 657 (an HFP AT-command frame) declares
`frame.len=147`, captured `15`. Command:

```
tshark -r CAP-012-btsnooz_hci.log -T fields -e frame.number -e _ws.col.Protocol -e frame.len -e frame.cap_len | \
  awk -F'\t' '$2=="HCI_ACL" && $3!=$4 {print}'
```
Sample output (`frame.number|Protocol|frame.len|frame.cap_len`):
```
534	HCI_ACL	59	15
535	HCI_ACL	19	15
550	HCI_ACL	684	15
551	HCI_ACL	209	15
```
By contrast, `HCI_CMD`/`HCI_EVT` packets (including every pairing-related command/event used in
§2) are **not** subject to this truncation — `frame.len == frame.cap_len` for all of them, which
is why §2's classic-link analysis below is unaffected and stays 🟢 FACT-grade, while §4/§5's
payload-content analysis is not.

**Consequence, stated plainly:** a byte-string search for `CAP-002`'s TLV content (Model ID,
`"Revision 6"`, `"in-use"`) or `CAP-001`'s DLCI-0x08 content (`"google-pixel-buds-pro-v1"`,
`"release_5.203"`) returning **zero matches** in this log is **expected under truncation alone**,
regardless of whether that content was actually sent — it is **not** the same kind of clean
negative result `CAP-004-FINDINGS.md` §4a reported (that capture used a full, untruncated log).
Verified directly:
```
tshark -r CAP-012-btsnooz_hci.log -Y 'data.data contains 52:65:76:69:73:69:6f:6e' | wc -l   # "Revision" → 0
tshark -r CAP-012-btsnooz_hci.log -Y 'data.data contains da:2d:b1'                | wc -l   # Model ID  → 0
tshark -r CAP-012-btsnooz_hci.log -Y 'data.data contains 69:6e:2d:75:73:65'       | wc -l   # "in-use"  → 0
tshark -r CAP-012-btsnooz_hci.log -Y 'data.data contains "google-pixel-buds-pro-v1"' | wc -l  # → 0
tshark -r CAP-012-btsnooz_hci.log -Y 'data.data contains "release_5.203"'         | wc -l   # → 0
```
All five return `0`. **Zero-Creativity rule applied: none of these absences is reported as a
resolved GFPS-001 result below** — each is reported as *inconclusive due to truncation*, exactly
as `CAP-017-FINDINGS.md` §2 did for its BLE handle/UUID search, not force-fit into "present" or
"absent."

**Recommendation (see §9):** a re-capture of this exact procedure using `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
§3 step 3's raw-log path (or confirming a working, larger snaplen before extracting via step 4) is
needed before `GFPS-001`'s payload-content question can be answered as cleanly for this
no-BLE-tool procedure as `CAP-004` answered it for the nRF-Connect procedure.

## 2. Classic bonding mechanism: confirmed classic Secure Simple Pairing, NOT Cross-Transport Key Derivation — 🟢 FACT, resolves the `CAP-004` confound

This is this session's core deliverable. Two independent classic-link sequences occur (a fresh
pairing, then a manual disconnect/reconnect — see §6); **neither shows any SMP/LE pairing
exchange, and no LE connection to the Buds is ever established at any point in this session.**

**Verification that no BLE link to the Buds ever forms (the specific confound this capture is
testing):**
```
tshark -r CAP-012-btsnooz_hci.log -Y "bthci_evt.code==0x3e" -T fields -e frame.number -e bthci_evt.le_meta_subevent | \
  sort -u -k2
```
Every `LE Meta` event in this log is subevent `0x0d` (**LE Extended Advertising Report** — a
passive scan result, 213 occurrences) or `0x02`/other advertising-report codes; **zero** occurrences
of subevent `0x01` (LE Connection Complete) or `0x0a` (LE Enhanced Connection Complete) appear
anywhere in the full 542s log, not just the video window. There is simply no BLE connection to
correlate against — the phone only ever passively hears the Buds' advertisements while performing
its own classic inquiry/pairing.

**Sequence 1 — fresh classic pairing (frames 457–526, 15:31:05.955–15:31:19.008):**

| Step | Time | Frame(s) | Detail |
|---|---|---|---|
| `Delete Stored Link Key` | 15:31:05.955 | 457–458 | Same intent as `CAP-002`/`CAP-003`/`CAP-004`'s fresh-bond flow |
| `Create Connection` (bd_addr `04:00:6e:cf:6e:07`) | 15:31:05.958 | 459 | |
| `Connect Complete` (status `0x00`) | 15:31:06.410 | 463 | |
| `Authentication Requested` → `Link Key Request` → **`Link Key Request Negative Reply`** | 15:31:06.439–.446 | 479–484 | No stored key available — consistent with the just-deleted key above |
| `IO Capability Request` → `IO Capability Request Reply` (io_capability=`0x01`, DisplayYesNo) → `IO Capability Response` (Buds report io_capability=`0x03`, NoInputNoOutput) | 15:31:06.448–.466 | 485–495 | **Classic SSP negotiation — this step alone already rules out CTKD**, which requires an SMP `Pairing Request` over an LE link instead of this classic IO-capability exchange |
| `Simple Pairing Complete` | 15:31:18.872 | 517 | ~12.4s after the IO Capability exchange, matching the on-screen "Pair with Pixel Buds Pro 2 van Ted?" confirmation dialog being shown (video: appears 15:31:09, tapped 15:31:20 — see §7) rather than a passkey step; no passkey digits are ever shown, same "silent SSP behind a confirmation dialog" shape as `CAP-002-FINDINGS.md` §1 |
| `Link Key Notification` (new key stored) → `Authentication Complete` | 15:31:18.881 | 518–519 | |
| `Set Connection Encryption` → `Encryption Change [v2]` | 15:31:18.900–15:31:19.008 | 521–526 | |

Command used to extract this table:
```
tshark -r CAP-012-btsnooz_hci.log -Y "bthci_cmd.opcode in {0x0405,0x040b,0x041b,0x041d,0x0413} or \
  bthci_evt.code in {0x03,0x04,0x05,0x06,0x08,0x0e,0x0f,0x18,0x22,0x2b,0x36}" \
  -T fields -e frame.number -e frame.time -e _ws.col.Info
```

**Sequence 2 — manual disconnect + reconnect, same session (frames 962–1044, see §6 for what
triggers this) — reuses the just-established stored key, no SSP at all:**

| Step | Time | Frame(s) | Detail |
|---|---|---|---|
| `Create Connection` | 15:32:07.802 | 1001 | |
| `Connect Complete` (status `0x00`) | 15:32:08.630 | 1003 | |
| `Authentication Requested` → `Link Key Request` → **`Link Key Request Reply`** (not Negative) | 15:32:08.642–.652 | 1017–1021 | Stored key from Sequence 1 reused directly — no IO Capability/SSP exchange at all, same shape as `CAP-001`'s reconnect path (`PROTOCOL.md` §5.1) |
| `Authentication Complete` → `Set Connection Encryption` → `Encryption Change [v2]` | 15:32:08.669–.698 | 1029–1044 | |

**Conclusion, answering this session's own hypothesis test directly:** with no BLE tool used and
no pre-existing BLE link to the Buds at any point, classic bonding follows the **same classic SSP
path as `CAP-002`/`CAP-003`** (`PROTOCOL.md` §5.1's "fresh pairing" state machine) — **not**
`CAP-004`'s Cross-Transport Key Derivation path. This directly confirms the hypothesis
`CAP-004-FINDINGS.md` §8 item 4 raised: **CTKD was an artifact of nRF Connect's early BLE
connection specifically, not a genuine effect of GMS being disabled or the Pixel Buds app being
absent.** Three independent sessions (`CAP-002`, `CAP-003`, `CAP-012`) now show classic SSP under
three different conditions (official app; nRF Connect BLE tool but classic-only pairing path;
GMS-disabled/no-app, no BLE tool at all) and exactly one session (`CAP-004`) shows CTKD, under the
one condition that involved an early BLE connection — a clean, direct causal isolation, not merely
a repeated negative.

## 3. RFCOMM channel topology this session — 🟢 FACT

Both connection sequences open the identical set of DLCIs: **0x00** (multiplexer control),
**0x08** (private envelope, §5), **0x0a** (silent — channel-control traffic only, matching every
prior capture's characterization), **0x0c** (HFP AT-command channel, §6). **DLCI 0x02
(`libmaestro`'s Pigweed `pw_hdlc` channel) and DLCI 0x04 (the official Fast Pair Message Stream)
are never opened in either sequence** — the same "channels 1/2 never opened" pattern
`CAP-004-FINDINGS.md` §3 first reported under GMS-disabled/no-app conditions, now reproduced a
second time under the same conditions without a BLE tool in the mix:
```
tshark -r CAP-012-btsnooz_hci.log -Y "btrfcomm.dlci" -T fields -e btrfcomm.dlci | sort -u
```
Output: `0x00`, `0x08`, `0x0a`, `0x0c` (no `0x02`/`0x04` in either occurrence).

## 4. `GFPS-001` outcome — inconclusive for payload content, consistent with `CAP-004` for channel topology (🟡, not the clean result `CAP-004` reached)

- **Channel-level result (not affected by truncation, since DLCI identity is header information,
  always within the captured prefix): DLCI 0x04 never opens** — same as `CAP-004`. This is
  independently consistent with `CAP-004-FINDINGS.md` §4a's "channel-2/DLCI-0x04 TLV content is
  GMS-and/or-app-dependent" finding, now reproduced without the nRF-Connect confound clouding the
  bonding-mechanism side of that session. **Still not isolated between "GMS-dependent" and
  "app-dependent"** — this session changed both variables together too, same confound
  `CAP-004-FINDINGS.md` §4a already flagged as unresolved; `CAP-012` does not add a new data point
  on that specific sub-question.
- **Payload-content result: inconclusive, not "absent"** — per §1, every byte string this
  project's `GFPS-001` procedure checks for (`CAP-002` §3's TLV content, `CAP-001`'s DLCI-0x08
  content) returns zero matches, but this log's ACL truncation makes that the expected outcome
  regardless of ground truth. This is **not** a second confirming data point for `CAP-004-FINDINGS.md`
  §4b's "DLCI-0x08 content survives GMS-disabled conditions unchanged" finding — it can neither
  confirm nor contradict it here.

## 5. DLCI 0x08 private envelope: header structure reproduces, content unrecoverable — 🟡/🔴 (bounded by §1)

Even though the *value* bytes of most DLCI 0x08 messages are truncated away, the leading
`[Group][Code]` header bytes usually survive (they're the first 2 bytes of the RFCOMM payload,
within the ~15-byte captured prefix), letting the **structure** — which is what `CAP-004-FINDINGS.md`
§5a already promoted to 🟢 FACT ("groups `0x03`/`0x04`/`0x05`/`0x09`/`0x0e` are genuine,
self-contained, standalone Message-Stream-shaped groups") — be checked again, independent of
whether any individual value can be read.

Extraction command:
```
tshark -r CAP-012-btsnooz_hci.log -Y "btrfcomm.dlci==0x08" -T fields -E separator='|' \
  -e frame.number -e frame.time_epoch -e _ws.col.Info -e frame.len -e frame.cap_len -e data.data
```
Header bytes recovered, Sequence 1 (frames 854–895, 15:31:19.850–.965 — 13 header pairs, in
order): `05 0c`, `05 0a`, `04 02`, `04 03`, `04 04`, `04 11`, `04 05`, `04 13`, `04 12`, `04 15`,
`04 14`, `04 16`, `0e 04`, `09 03`, `03 01`, `0e 02`, `0e 01`, `09 02` (plus several
zero-payload/header-only frames, e.g. 878/889/895, and one ambiguous 3-byte fragment at frame 882,
`08 a0 03`, not force-decoded — see below). Sequence 2 (frames 1307–1347, 15:32:09.048–.225)
reproduces the same Group/Code set in the same relative order: `05 0c`, `04 02`, `04 04`, `04 11`,
`04 13`, `04 15`, `0e 04`, `05 0a`, `09 03`, `04 03`, `04 05`, `04 12`, `04 14`, `04 16`, `0e 02`,
`0e 01`, `09 02`, **`03 02`** (not `03 01` this time — see below).

**Cross-checked against `CAP-004-FINDINGS.md` §5a's table — every Group/Code pair recovered here
already appears in that table** (Groups `0x03`/`0x04`/`0x05`/`0x09`/`0x0e`, the same Code sets
within each). 🟢 **FACT for structural reproduction**: this session's DLCI 0x08 handshake burst is
the same connection-setup handshake shape already documented across four prior captures, now a
fifth and sixth confirming occurrence (once per connection sequence this session).

**What is explicitly NOT claimed, per the Zero-Creativity rule:**
- **No value byte beyond the 2–4-byte header (and, for some frames, one length byte) is asserted.**
  E.g. `0e 01 00` (frame 887/1343) matches Group `0x0e` Code `0x01`'s known header
  (`DECISIONS.md` ADR-014's per-earbud+case battery push), but the captured `00` is only the
  *first* of the 2 declared length bytes (`frame.len` 52/52, `frame.cap_len` 15) — the actual
  length and all 3 battery-entry values are truncated away and are **not** reconstructed or
  assumed here.
- **`03 02 00` (frame 1347, Sequence 2) is structurally consistent with, but not confirmed as,**
  the `Group 0x03 Code 0x02` protobuf message `CAP-004-FINDINGS.md` §5a byte-for-byte matched
  across `CAP-002`/`CAP-004` (containing `"release_5.203"`) — only the 3-byte header survives
  (`frame.len=80`, `frame.cap_len=15`); the value is not recoverable, so this is **not** used as
  wire re-confirmation of the firmware string (see this file's header note on firmware version).
- **Frame 882's `08 a0 03`** does not cleanly match any previously-documented Group/Code pair in
  this position of the sequence (compare to `CAP-004-FINDINGS.md` §5a's table, which has no
  `Group 0x08` entry on this DLCI) and is not force-fit into one — flagged as 🔴 unresolved,
  possibly a truncated fragment of a different message's later bytes rather than a fresh header
  (per `CAP-004-FINDINGS.md` §5a's own finding that messages can split across RFCOMM frames), not
  guessed at further.
- **Sequence 1 shows `03 01` where Sequence 2 shows `03 02`** in the same structural position
  (both immediately after `09 03`/`09 02`-ish traffic). Given `CAP-004-FINDINGS.md` §5a documents
  *both* `Group 0x03 Code 0x01` (the `"Europe/Amsterdam"` message) and `Group 0x03 Code 0x02` (the
  `"release_5.203"` protobuf message) as real, independent messages on this DLCI, this is
  plausibly just "Sequence 1 happened to send the timezone message, Sequence 2 happened to send
  the firmware-info message" rather than a session-to-session inconsistency — **not confirmed**,
  since neither value is recoverable here to check which is which.

## 6. Bonus finding: manual disconnect + reconnect (Test-ID `PAIR-003`) retriggers the full HFP AT-command handshake — 🟢 FACT, advances a `PROTOCOL.md` §6 open question

**What triggers Sequence 2 (§2):** video review shows the user tapping directly on the
**"Pixel Buds Pro 2 van Ted — Active. 100% battery."** row (not the gear icon) in system
Bluetooth settings at 15:32:04, which Android's UI treats as a manual disconnect — the chip
changes to **"Saved"** by 15:32:05. The wire log corroborates this exactly: `Sent DISC Channel=6`
(15:32:03.203, frame 962) → `Sent DISC Channel=0` (15:32:03.589) → L2CAP
`Disconnection Request`/`Response` (15:32:03.652–.666) tears the ACL link down, phone-initiated,
with no preceding hardware event (buds stay out of the case throughout, per video). The user then
taps the same row again at 15:32:08 ("Connecting…" shown by 15:32:10), producing Sequence 2's
`Create Connection` at 15:32:07.802 (frame 1001).

This is a clean, on-screen-confirmed instance of `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `PAIR-003`
("Disconnect and reconnect to an already-bonded device") — previously captured only on the
Pixel 9a (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` row for `PAIR-003`: "no dedicated Pixel 7a scenario
yet"). **`CAP-012` supplies that missing Pixel 7a data point, incidentally, not by design.**

**Relevant to `PROTOCOL.md` §6's open HFP-recurrence question** (`CAP-008-FINDINGS.md` §3's
narrowing: "does a reconnect that does *not* involve a full radio power-cycle also retrigger the
HFP SLC handshake, or does `CAP-002`'s negative result specifically reflect an ACL connection that
was simply never torn down?"): **this session's Sequence 2 is exactly that untested case — a
disconnect/reconnect via the Settings UI, no Bluetooth radio toggle — and the full HFP AT-command
handshake reoccurs.** Verified via the same `AT`/`\r\nOK` round-trip shape on DLCI 0x0c
(`RFCOMM Channel=6`) in both sequences:
```
tshark -r CAP-012-btsnooz_hci.log -Y "btrfcomm.dlci==0x0c and btrfcomm.len>0" -T fields \
  -e frame.number -e frame.time_epoch -e data.data
```
Sequence 1: first `AT` byte pair (`41 54`) at frame 634 (15:31:19.4485), 34 AT-command exchanges
through frame 762 (15:31:19.6658). Sequence 2: first `AT` at frame 1185 (15:32:08.9185), same
shape resumes immediately after the reconnect's `SABM Channel=6`/`UA Channel=6` (frames
1165/1167). Content is truncated beyond `AT`/`\r\nOK` fragments (§1 — no `frame.len<=15` DLCI 0x0c
frame in either sequence carries a real AT command payload; the individual command text,
`AT+CIND?` values, and `AT+BIEV` battery figure are **not** recoverable), but the **shape and
timing of the handshake itself** — a full SLC re-establishment, not silence — is unambiguous from
the frame-count and `4154`/`0d0a4f` pattern alone.

**This narrows, but does not fully close, the open question**: a reconnect that does not power-cycle
the radio (this session's `PAIR-003` case) *does* retrigger the handshake, same as `CAP-008`'s
radio-power-cycle case — two independently-triggered reconnect types now both show recurrence,
none show `CAP-002`'s original silence. `CAP-002`'s own negative result increasingly looks like it
reflects "this session's ACL connection was simply never torn down," per `CAP-008-FINDINGS.md`
§3's own leading explanation, rather than a property of *which* reconnect mechanism is used — but
this is now two data points, not a definitive rule; see §9/§10 for the proposed `PROTOCOL.md`
§6 update, which is a **proposal only**, per `AGENTS.md` §6.

## 7. Video/event-timeline validation (`CAP-012-EVENT-NOTES.md`)

Reviewed `CAP-012-recording.mp4` frame-by-frame (1s resolution at every timeline entry, plus
1s-resolution brackets around 15:32:03–15:32:11, the previously-undocumented reconnect window) via
`ffmpeg -ss <hh:mm:ss> -i CAP-012-recording.mp4 -frames:v 1 <out>.png`, using the video's own
burned-in wall-clock overlay. All eleven timestamps already in the original draft
`CAP-012-EVENT-NOTES.md` are confirmed accurate to within 1s (e.g. 15:30:57's "phone's Bluetooth
address" field is legible on screen: `E8:D5:2B:7E:CA:81`, matching this file's header exactly).
One event was added and one gap was closed — see `CAP-012-EVENT-NOTES.md`'s own "Corrections vs.
the original draft" section for the itemized diff; the updated timeline (including the
15:32:03–15:32:10 disconnect/reconnect from §6) now lives directly in that file's Event Timeline
table, per this project's event-notes convention.

## 8. Test-ID traceability check (`AGENTS.md` §13 point 7)

Group S's assigned Test-ID is `GFPS-001` (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §0.3/§4.1) —
exercised and referenced in §4 above. Bonus Test-IDs actually exercised on camera, cross-checked
against `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`:

- **`PAIR-001`** (pairing/bonding handshake baseline) — exercised, §2 Sequence 1. Same
  "bond-removal-only, not a factory reset" framing `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group S step 3
  already documents for this Group.
- **`CASE-003`** (open the charging case lid) — exercised at 15:30:32 (`CAP-012-EVENT-NOTES.md`);
  no dedicated wire-level check performed here since this Test-ID's own definition
  (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` row) is about the BLE advertisement/Fast-Pair-popup trigger,
  already established elsewhere, and no Fast Pair popup is expected or seen under this session's
  GMS-disabled condition (consistent with `CAP-004-FINDINGS.md` §7).
- **`PAIR-003`** (disconnect/reconnect to an already-bonded device) — exercised incidentally, §6;
  not part of Group S's own written procedure, but the first Pixel-7a occurrence of this Test-ID
  (see §6 and §9).

**No gap to flag**: every Test-ID this Group's own procedure calls for (`GFPS-001`) is clearly
referenced in the timeline, and the two bonus Test-IDs found are both already-defined IDs, not new
untracked behavior.

## 9. Recommended next steps

1. **Re-capture this exact procedure with a working, non-truncated snoop log** (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`
   §3 step 3's raw-log path, or a confirmed-larger snaplen if step 4 must be used) — the single
   highest-value follow-up, since §1's truncation is the only reason this session cannot give
   `GFPS-001` as clean an answer as `CAP-004` did for the nRF-Connect variant.
2. Fold this capture's §6 finding (a non-power-cycle reconnect retriggers the full HFP handshake)
   into a dedicated look at whether `CAP-002`'s original silent session ever tore down its ACL
   link at all — `CAP-002`'s own ~8h20m shared log likely already contains the answer and would
   not require a new capture.
3. `PAIR-003` now has a Pixel 7a data point (§6) — update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s row
   accordingly (see §10).

## 10. Hypothesis test record (`PROJECT_RULES.md` rule 10/11 template)

- **Hypothesis:** `CAP-004`'s Cross-Transport Key Derivation (CTKD) bonding result is an artifact
  of nRF Connect's early BLE connection to the Buds, not a genuine effect of Google Play Services
  being disabled and the Pixel Buds app being uninstalled. Repeating Group S's procedure exactly
  (system Bluetooth settings only, no BLE tool at any point) should show classic Secure Simple
  Pairing (as in `CAP-002`/`CAP-003`) instead of CTKD.
- **Setup:** Pixel 7a, Android 17 (⚪ assumed, not re-confirmed on screen this session), Buds
  firmware ⚪ assumed `release_5.203` (not re-confirmed on the wire this session — see header and
  §5), Google Play Services disabled, Pixel Buds app uninstalled, no BLE tool used at any point.
  Capture method: `adb bugreport` → `btsnooz.py` (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3 step 4;
  **this session's log is more heavily ACL-truncated than prior captures — see §1**).
- **Expected outcome:** classic SSP (IO Capability exchange, `Simple Pairing Complete`), no SMP/LE
  pairing exchange, no pre-existing BLE link to the Buds.
- **Actual outcome:** confirmed exactly as expected — §2's Sequence 1 shows a full classic SSP
  exchange (`IO Capability Request/Response` → `Simple Pairing Complete` → `Link Key Notification`),
  zero SMP frames anywhere in the 542s log, and zero LE Connection Complete/LE Enhanced Connection
  Complete events of any kind (only passive LE Extended Advertising Reports, which do not
  constitute a connection).
- **Conclusion:** 🟢 **FACT — hypothesis confirmed.** `CAP-004`'s CTKD bonding mechanism was
  specifically caused by nRF Connect's early BLE connection existing before classic pairing began,
  not by the GMS-disabled/no-app condition. With that confound removed, classic bonding under
  GMS-disabled/no-app conditions follows the same classic-SSP state machine as every other fresh
  pairing this project has captured (`PROTOCOL.md` §5.1). This is a genuine resolution, not an
  inconclusive result — unlike §4/§5's payload-content questions, which §1's truncation finding
  leaves open. This session's failed/degraded aspect (truncation) is itself recorded here per
  `PROJECT_RULES.md` rule 12 ("a failed or inconclusive hypothesis test is still recorded"),
  scoped specifically to the payload-content sub-question, not to this hypothesis test's own
  (successful) bonding-mechanism question.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-012-2026-08-26_15-30-19_15-32-28-Group_S/CAP-012-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-012-2026-08-26_15-30-19_15-32-28-Group_S/CAP-012-FINDINGS
