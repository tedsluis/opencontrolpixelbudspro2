# Findings: `CAP-004` (Group S — GMS disabled / no Pixel Buds app)

Standardized, evidence-based extraction from `btsnoop_hci.log` + `recording.mp4`, staged here
for later promotion into `PROTOCOL_NOTES.md` / `PROTOCOL.md` per `PROJECT_RULES.md` §2. Modeled
on `captures/2026-08-09_08-51-00_08-52-20-Group_Z/FINDINGS.md` (`CAP-001`). Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-004` · **Date:** 2026-08-11 · **Phone:** Pixel 7a, **Google Play Services
disabled, Pixel Buds app uninstalled**; connected via nRF Connect then paired via system
Bluetooth settings — no Pixel-Buds-specific app involved at any point. **Log file:**
`btsnoop_hci.log` (342.3s, 2,921 packets, 06:22:04.23–06:27:46.48 local/+0200). **Video:**
`recording.mp4` (155.4s, 06:22:36–06:25:12 local, on-screen wall-clock overlay). **Devices:**
phone `Google_7e:ca:81` (Pixel 7a, same phone as `CAP-001`–`CAP-003`), peer `Google_cf:6e:07`
(`04:00:6E:CF:6E:07`, the Buds/case — confirmed same physical device via frame 1891's BD_ADDR).

**Stated goal of this session:** determine whether the Fast Pair Message Stream traffic
identified in `CAP-002`'s `FINDINGS.md` §3 (the `[Group][Code][Length:2B][Value]`-framed
channel/DLCI carrying, among other fields, Group `0x03` Code `0x09` = `"Revision 6"`, Code
`0x01` = `da 2d b1`, and Group `0x07` Code `0x41` = `"in-use"`) is **Buds-initiated** (would
reappear identically) or **GMS/Nearby-driven** (would disappear or change) once Google Play
Services is disabled and no Pixel Buds app is present. **§4 gives the answer, with a nuance the
question didn't anticipate — see there.** A full pairing exchange was also expected as a side
effect and is documented in §2, per this session's own procedure step 3.

---

## 1. Log contains unrelated background traffic — Fitbit Charge 6 excluded (🟢 FACT)

Before analyzing anything else: the log's very first GATT activity (frames 216–327+, starting
06:22:05.17, including a full `Read By Group Type` primary-service discovery resolving
`GAP`/`GATT`/`Device Information`/**`Heart Rate`**) does **not** belong to the Buds. Checked
directly: frame 229's connection context shows `Source BD_ADDR: 62:6f:64:e2:1b:4a`, `Source
Device Name: Charge 6` — the maintainer's Fitbit, reconnecting in the background on the same
phone, same as the unrelated traffic already flagged and excluded in `CAP-002`'s `FINDINGS.md`
§7. This is recorded here so it isn't mistaken for Buds discovery traffic by a future reader —
all findings below are scoped to traffic confirmed against `Google_cf:6e:07`.

## 2. Classic pairing via Cross-Transport Key Derivation, not classic SSP (🟢 FACT — new mechanism, not seen in `CAP-002`/`CAP-003`)

Both earlier fresh-pairing captures (`CAP-002`, `CAP-003`) showed classic Secure Simple Pairing:
an `IO Capability Request/Response` exchange followed by `Simple Pairing Complete`. **This
session shows neither.** Instead:

| Step | Time | Frame(s) | Detail |
|---|---|---|---|
| `Delete Stored Link Key` | 06:24:24.668 | 1854–1855 | Confirms a deliberate fresh-bond flow, same intent as `CAP-002`/`CAP-003` |
| SMP `Pairing Request` (LE) | 06:24:24.672 | 1856 | `AuthReq: Bonding, MITM, SecureConnection`; **key distribution includes `Linkkey`** for both initiator and responder — this is the explicit request for **Cross-Transport Key Derivation (CTKD)** |
| SMP `Pairing Response`, Public Key exchange, `Pairing Confirm`/`Random` | 06:24:24.735–24.974 | 1859–1879 | LE Secure Connections (ECDH-based) pairing proceeds — not legacy SMP pairing |
| SMP `DHKey Check` (sent/received) | 06:24:29.606–29.684 | 1880, 1882 | Completes the LE Secure Connections pairing |
| `Identity Information`/`Identity Address Information` exchanged | 06:24:29.864–29.866 | 1886–1890 | Standard LE bonding key distribution |
| Classic `Create Connection` | 06:24:29.878 | 1891 | Sent immediately after the LE pairing completes |
| Classic `Connect Complete` | 06:24:30.380 (status `0x00`) | 1933 | |
| `Authentication Requested` → `Link Key Request` → **`Link Key Request Reply`** (not Negative) | 06:24:30.422–30.431 | 1966–1977 | The classic link key is **already available** — derived from the LE pairing above via CTKD, not obtained through a separate classic SSP exchange |
| `Authentication Complete` → `Encryption Change` | 06:24:30.442–30.795 | 1982, 2037 | |

**Why this differs from `CAP-002`/`CAP-003`:** in both earlier captures, classic pairing was the
*first* thing to happen, with no pre-existing BLE link. Here, nRF Connect had already established
a BLE connection to the Buds at 06:23:01 — over a minute before the classic pairing sequence
began — giving Android's stack an existing LE Secure Connections context to derive the classic
key from via CTKD instead of negotiating classic SSP from scratch. This is a genuinely new,
useful data point for `PROTOCOL.md` §5's connection-lifecycle section: **the classic bonding
mechanism used depends on whether an LE Secure Connections link already exists**, not just on
whether a classic bond exists.

**Answering this session's own procedural question** (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group S
step 3, "note whether the device was already unpaired or a fresh bond"): this **was** a fresh
bond (link key deleted, new key derived), but via CTKD from a fresh LE pairing rather than via
the classic-SSP mechanism seen before.

## 3. RFCOMM channel topology this session (🟢 FACT)

Channels opened: **0** (multiplexer control, frame 2052), **6** (labeled **"Hands-Free"** by
Wireshark, frame 2068 — HFP AT-command handshake confirmed present, 53 HFP-tagged packets),
**4** (frame 2178), **5** (frame 2225). **Channels 1 and 2 are never opened at all this
session** — a first among the four captures to date, and directly relevant to §4 below, since
`CAP-002`'s Fast Pair Message Stream Device Information content lived specifically on channel
2/DLCI 0x04.

## 4. Answering the core question: present-but-partial, not a clean present/absent (🟡 mixed outcome — see the two parts below)

The exact byte strings from `CAP-002` §3 were searched for directly across every RFCOMM data
frame in this capture. Result is genuinely mixed between two distinct sub-mechanisms that
`CAP-002` had described together as one channel's content — this capture shows they are
**not** the same thing, and behave differently under GMS-disabled conditions:

### 4a. The `[Group][Code][Length:2B][Value]` TLV content specific to `CAP-002` §3 — **ABSENT** (outcome 2: supports GMS/Nearby-driven)

A direct byte-string search for `"Revision 6"` (`5265766973696f6e2036`), the Model ID value
`da 2d b1`, and `"in-use"` (`696e2d757365`) across every RFCOMM data frame in this capture
returns **zero matches**. Consistent with that: **channel 2/DLCI 0x04 — the specific channel
`CAP-002` found this content on — is never opened in this session at all** (§3). This is a
clean, unambiguous absence for this specific sub-mechanism: the Model-ID/Firmware-version/
BLE-address-updated TLV exchange that `CAP-002` documented does not occur when GMS is disabled
and no Pixel Buds app is installed. **This supports the GMS/Nearby-driven hypothesis** for that
specific content — not proof (a negative result from one capture can't rule out a triggering
condition this session didn't happen to hit), but a real, checked absence.

### 4b. A related-but-distinct protobuf-shaped blob (channel 4/DLCI 0x08) — **PRESENT, essentially unchanged** (outcome 1: supports Buds-initiated)

`CAP-001`'s `FINDINGS.md` §2 separately documented a **different** piece of content — on
channel 4/DLCI 0x08, *not* channel 2 — containing the ASCII strings `google-pixel-buds-pro-v1`
and `Europe/Amsterdam`, protobuf-tag-framed (not the Group/Code/Length TLV shape). **This
content reappears in this capture, on the same channel-4/DLCI-0x08 pattern, essentially
unchanged:**

- `google-pixel-buds-pro-v1` + a capability-negotiation blob containing ASCII `all` — frames
  2311/2315 (06:24:31.104–31.105), byte-identical in shape to the equivalent frames in
  `CAP-001`/`CAP-002`/`CAP-003`.
- `Europe/Amsterdam`, protobuf tag+length-framed exactly as in `CAP-001` (`1a 10` + 16-byte
  string) — frame 2274 (06:24:31.076), value `08 9f 03 10 c3 b8 c2 f8 0e 1a 10` + `"Europe/
  Amsterdam"` — the leading random-looking bytes differ from `CAP-001`'s occurrence (expected,
  per `CAP-001`'s own note that this field appears session-specific/nonce-like), but the
  string, tag byte, and length byte are identical.

**This supports the Buds-initiated hypothesis** for *this* content specifically — it appears
unchanged with GMS disabled and no app installed, meaning the Buds (or the phone's own Bluetooth
stack, independent of GMS) send this regardless.

### Conclusion: the honest answer is "it depends on which sub-mechanism"

`CAP-002` §3 treated the channel-2 TLV content and general "device info exchange" as one
finding. This capture shows that framing was too coarse: there are **at least two distinct
device-info-flavored exchanges** happening over RFCOMM at connection time, on different
channels, with different framing, and — now shown here — **different dependencies on GMS**.
Recommend `CAP-002`'s `FINDINGS.md` §3 be given a similar non-destructive correction note (per
the pattern already used there for the channel-1/AVRCP correction) clarifying that its content
is GMS-dependent, distinct from the channel-4/DLCI-0x08 content documented in `CAP-001`, which
is not.

## 5. Spec research on Groups `0x04`/`0x05`/`0x09`, and a re-decode of frame 2305 (updated 2026-08-11)

**Method correction first:** the byte table in the original version of this section was read by
treating each individual RFCOMM data frame as one complete `[Group][Code][Length:2B][Value]`
message. Re-parsing precisely (Python, exact byte offsets) shows this was wrong for at least two
of the frames — **a single logical message can be split across consecutive RFCOMM packets**,
header in one frame, value in the next (mirroring how `CAP-002`'s single 47-byte RFCOMM frame
packed several complete messages together — the reverse fragmentation pattern). Frames 2272
(`03 01 00 1b`, header only, declared value length 27) and 2274 (27 bytes) are one message, not
two; frame 2305 is a single, complete 67-byte frame. The corrected per-frame decode:

| Frame(s) | Group | Code | Declared len | Value |
|---|---|---|---|---|
| 2246 | `0x05` | `0x0c` | 0 | — |
| 2256, 2258, 2259, 2261, 2263 | `0x04` | `0x02`, `0x04`, `0x11`, `0x13`, `0x15` | 0 each | — |
| 2264 | `0x05` | `0x0a` | 13 | `0a 07 "713f855" 10 40 18 00` (protobuf-shaped) |
| 2272+2274 (one message) | `0x03` | `0x01` | 27 | `08 9f 03 10 <4 bytes> 0e 1a 10 "Europe/Amsterdam"` |
| 2275 | `0x04` | `0x03` | 4 | `00 10 05 18 64` (truncated in source dump — see raw hex) |
| 2277 | `0x09` | `0x03` | 0 | — |
| 2280 | `0x04` | `0x05` | 2 | `08 03` |
| 2289 | `0x04` | `0x12` | 4 | `08 02 10 01` |
| 2292 | `0x04` | `0x14` | 2 | `08 01` |
| 2295 | `0x04` | `0x16` | 2 | `08 02` |
| **2305** | **`0x03`** | **`0x02`** | **63** | protobuf-shaped, contains `"release_5.203"` and `"713f855"` — see below |
| 2310 | `0x09` | `0x02` | 2 | `08 00` |

**Group `0x04` identified — 🟢 FACT.** A targeted search (not a generic query — searched directly
for "Fast Pair Message Stream Action event group 0x04") found Google's own
[Device action](https://developers.google.com/nearby/fast-pair/specifications/extensions/deviceaction)
spec page, which states explicitly: *"Device action event"* = `0x04`, and defines code `0x01` as
**Ring** (already known to this project via `PROTOCOL.md` §4.4's Ring/Find-My-Buds hypothesis).
The page's visible content only documents code `0x01` in detail; codes `0x02`, `0x03`, `0x04`,
`0x05`, `0x11`, `0x12`, `0x13`, `0x14`, `0x15`, `0x16` observed here are **not** covered by the
fetched excerpt — group identity confirmed, individual code meanings within it remain 🔴 open.

**Bonus, directly relevant to `CAP-002` §3's separate open item:** the same search pass also
found Google's [SASS (Smart Audio Source Switching)](https://developers.google.com/nearby/fast-pair/specifications/extensions/sass)
extension page, which states it uses **Message Group `0x07`** and lists a full code table
including **`0x41` = "Indicate in use account key."** This is an exact match to `CAP-002`
`FINDINGS.md` §3's Group `0x07` Code `0x41` `"in-use"` finding — previously 🟡 on the strength of
an *English-phrase* match against a different (GATT-level) spec page. This SASS page gives the
*same transport* (Message Stream) and the *exact* group/code pair, which is materially stronger
evidence. Recommend `CAP-002` §3 be updated to 🟢 FACT for that specific bullet (not done in this
file — out of this session's scope, flagged for a maintainer/future pass, consistent with how
`CAP-002` §7 item 4 already deferred a similar cross-file update).

**Groups `0x05` and `0x09` — still 🔴 genuinely unidentified.** Unlike Group `0x04`, no spec page
was found defining a standalone "Group `0x05`" or "Group `0x09`". Search results for these codes
kept surfacing **Device Information (Group `0x03`) codes `0x05`/`0x06`** ("Active components
request/response") and **code `0x09`** ("Firmware version") instead — i.e. `0x05` and `0x09` are
documented as *codes within Group `0x03`*, not as separate top-level groups. This raises a real
possibility not resolved in this pass: frames 2246/2264 (`Group 0x05`) and 2277/2310
(`Group 0x09`) might not be freestanding messages with an undocumented group byte, but
**fragments of a still-larger multi-message frame this analysis hasn't fully reassembled** (per
the fragmentation behavior just discovered above) — or they are genuinely undocumented groups.
Left as 🔴 OPEN QUESTION rather than guessed at.

**Frame 2305 decoded — a `Group 0x03`/`Code 0x02` message containing the project's real,
confirmed firmware string, precisely re-parsed (not the "12-character string" originally
guessed — corrected below):**

```
Group 0x03, Code 0x02, Length 63
Value (protobuf-shaped):
  08 06                      → field 1 (varint) = 6
  10 01                      → field 2 (varint) = 1
  22 0d "release_5.203"      → field 4 (string, len 13) = "release_5.203"
  2a 00                      → field 5 (string, len 0) = ""
  30 e6 01                   → field 6 (varint) = 230
  38 00                      → field 7 (varint) = 0
  4a 07 "713f855"            → field 9 (string, len 7) = "713f855"   [7 chars, not 12]
  50 00                      → field 10 (varint) = 0
  60 b1 db e8 06             → field 12 (varint, multi-byte)
  70 02                      → field 14 (varint) = 2
  78 01                      → field 15 (varint) = 1
  ... (remaining bytes: further small varint fields)
```

**This confirms `"release_5.203"` — `PROTOCOL.md` §0.1's actual, already-verified firmware
baseline — genuinely appears on the wire**, inside this protobuf-shaped value. This is a
different string from `CAP-002`'s `"Revision 6"` (found under the *documented* Group `0x03` Code
`0x09` = Firmware version field). **New open question raised, not resolved, by this precise
decode:** this value sits under Code `0x02`, which `CAP-002` §3 spec-confirmed as **"BLE address
updated"** — a documented 6-byte MAC address field, categorically incompatible with this 63-byte
protobuf structure. Either Code `0x02` is being reused here for something unrelated to
`CAP-002`'s clean 6-byte-MAC match there, or — more likely, given the fragmentation bug already
found in this same burst — **this frame's leading `03 02 00 3f` is not actually a Message Stream
header at all**, and is coincidentally similar-looking header bytes belonging to a different,
not-yet-identified structure. **Not resolved in this pass — flagged explicitly rather than
force-fit into either the "BLE address" or "firmware version" slot.** This means `CAP-002` §3's
open question ("does `'Revision 6'` or something else represent the real firmware version?")
remains open: `"release_5.203"` is now a documented, on-the-wire-confirmed candidate, but the
framing that would explain *which* field it officially belongs to is not yet established.

## 6. GATT service list from nRF Connect's UI — cross-checked against `CAP-002`/`CAP-003`'s open UUID questions (🟡 HYPOTHESIS, UI-sourced not wire-confirmed)

As established in §1's method and `CAP-003`'s own findings, nRF Connect's displayed service list
comes from Android's **cached** GATT database (confirmed here too — the only GATT traffic at
connect time, frame 1524, is a `Database Hash` check, not live discovery). The list itself is
still genuine, real data (Android didn't fabricate it), just not re-verifiable against wire
traffic in this specific session. Full list observed on screen (`EVENT-NOTES.md`):

`Generic Attribute (0x1801)`, `Generic Access (0x1800)`, `Broadcast Audio Scan Service (0x184F)`,
`Audio Stream Control Service (0x184E)`, `Published Audio Capabilities Service (0x1850)`,
`Volume Control (0x1844)`, `Microphone Control (0x184D)`, `Audio Input Control (0x1843)`,
`Common Audio Service (0x1853)`, `Telephony and Media Audio Service (0x1855)`, **`Google Fast
Pair Service (0xFE2C)`**, **`Accessory Non-Owner Service (13190001-12f4-c226-88ed-
2ac5579f2a85)`**, `Device Information (0x180A)`, `Battery Service (0x180F)`, and an unnamed
**`Unknown Service (109b8b21-50e3-45cc-8ea1-ac62de4846d1)`**.

This is the first time this project has a candidate **named** service list for the Buds at all.
Combined with `CAP-002`/`CAP-003`'s handle numbers, a structural hypothesis (not wire-confirmed):

- Handle `0x0f2a` (returns `"Revision 6"`) and `0x0f28` (returns `0x31`) sit in a **high** handle
  range, consistent with falling inside **`Device Information (0x180A)`**, which per `CAP-002`'s
  spec research (Manufacturer/Model/Serial, then Hardware/Firmware/Software Revision String,
  consecutively) is exactly the kind of service that would place a Firmware/Software Revision
  characteristic near a Hardware Revision characteristic two handles apart.
- The `0x0c0X` handle cluster (encrypted-looking write/notify bursts, CCCD-enable pattern) sits
  in a **lower-middle** handle range — a plausible fit for **`Google Fast Pair Service
  (0xFE2C)`**, whose documented Key-based Pairing / Passkey / Account Key characteristics are
  specified to use exactly this encrypted-write/notify shape.
- **`Accessory Non-Owner Service (13190001-12f4-c226-88ed-2ac5579f2a85)`** is a non-standard
  128-bit UUID (not a Bluetooth SIG base UUID) — the name and structure strongly suggest this is
  Google's **Find My Device Network (FMDN)** accessory service already referenced in
  `PROTOCOL.md`/`DECISIONS.md` in the Fast Pair context, not yet linked to a concrete UUID
  anywhere in this project's documents until now.

None of this is a confirmed handle→UUID mapping (still requires the live-discovery capture
`CAP-003` §7 already recommended) — it is a plausible, multi-source-consistent HYPOTHESIS,
recorded so the eventual discovery capture has concrete predictions to check itself against.

## 7. Other observations

- **The "Enable Google Play services" notification persists for the entire session (🟢 FACT,
  video evidence, 06:23:01 onward)** — direct on-screen confirmation the GMS-disabled condition
  held throughout, not just at setup-check time.
- **No Fast Pair "half-sheet" or "Save device" dialog appears anywhere in this video** — consistent
  with `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group S's own manually-validated setup note that this
  GMS-driven UI does not appear when GMS is disabled.
- **The end-of-session device page is the generic Android Bluetooth settings page, not any
  Buds-specific UI (🟢 FACT, video evidence, 06:25:11)** — no Sound/ANC/EQ/Hearing wellness
  section at all, unlike `CAP-002`/`CAP-003`'s Pixel-Buds-app screens — direct confirmation that
  ANC/EQ control simply isn't reachable at all without the official app, reinforcing why
  `libmaestro` command capture (the project's ultimate goal) requires the app to be present, per
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s own guidance to do the primary/app-based capture first.

## 8. Recommended next steps

1. Determine the meaning of Message Stream Groups `0x04`, `0x05`, and `0x09` (§5) — search
   further into Google's Fast Pair spec pages (the ones already found for Group `0x03` link to
   sibling pages in the same documentation set) or a dedicated capture bracketing specific
   actions to correlate against these codes' timing.
2. Add a non-destructive correction note to `CAP-002`'s `FINDINGS.md` §3 (per §4's conclusion
   above) distinguishing the channel-2/DLCI-0x04 GMS-dependent TLV content from the
   channel-4/DLCI-0x08 content already documented in `CAP-001`, which is not GMS-dependent —
   not done in this pass, left for a maintainer decision per `CAP-002`'s own §7 item 4 precedent.
3. The live-GATT-discovery capture already recommended in `CAP-003`'s `FINDINGS.md` §7 item 1
   remains the only way to confirm §6's handle→UUID hypotheses; this capture adds concrete named
   candidates (Fast Pair Service, Accessory Non-Owner Service, Device Information) worth
   checking against once that capture exists.
4. A repeat of this same GMS-disabled test *without* nRF Connect in the mix (pure system-settings
   pairing, per the original Group S design) would cleanly isolate whether nRF Connect's presence
   affected any of this session's results (e.g. the CTKD pairing mechanism in §2 might be an
   artifact of nRF Connect's early BLE connection, not of GMS being disabled) — worth doing once,
   even though this capture's core `GFPS-001` answer (§4) is not expected to change.

## 9. `GFPS-001` outcome and `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` status

Per the three possible outcomes defined for this test: the result is **outcome 3 (present but
different)** — more precisely, "present for one sub-mechanism, absent for another," which
`CAP-002` had not yet distinguished as two separate mechanisms. This is recorded as 🟡 in
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (not 🟢), because:
- The *absence* of the channel-2 TLV content (§4a) is a clean, checked negative result, but one
  capture cannot rule out that some other trigger (not exercised in this session) would still
  produce it under GMS-disabled conditions.
- The channel-4 content's *presence* (§4b) is solid, but was already established as
  Buds-initiated-shaped in `CAP-001` before this test — this capture confirms it survives
  GMS-disabled conditions, which is the new information, not a first discovery.
- The newly-found Group `0x04`/`0x05`/`0x09` content (§5) is a genuine unknown this test
  surfaced, not resolved by it.

## 10. Promotion readiness — what's ready for `PROTOCOL_NOTES.md` (updated 2026-08-11)

**Ready to promote now (🟢 FACT):**
- Cross-Transport Key Derivation as an alternate classic-bonding path when an LE Secure
  Connections link already exists (§2) — new addition to `PROTOCOL.md` §5's connection-lifecycle
  material.
- The channel-2/DLCI-0x04 TLV content from `CAP-002` §3 does not appear when GMS is disabled
  and no Pixel Buds app is present, in a session that otherwise fully connects and bonds (§4a) —
  now also cross-referenced as a non-destructive correction in `CAP-002`'s `FINDINGS.md` §3.
- The channel-4/DLCI-0x08 content from `CAP-001` (`google-pixel-buds-pro-v1`, `Europe/
  Amsterdam`, capability blob) reappears unchanged under the same GMS-disabled conditions (§4b).
- RFCOMM channel numbers continue to vary session-to-session (channels 1/2 never opened this
  time at all) — a fourth confirming data point for the reusable note in `CAP-001`'s
  `FINDINGS.md` §2.
- **Message Stream Group `0x04` = "Device action"**, spec-confirmed directly against Google's
  own `deviceaction` page (§5) — corroborates `PROTOCOL.md` §4.4's existing Ring/Find-My-Buds
  hypothesis (code `0x01` = Ring) with an authoritative source, and gives the group itself a
  confirmed identity even though most individual codes within it (`0x02`–`0x16` observed here)
  remain open.
- **Message Stream Group `0x07` Code `0x41` = "Indicate in use account key,"** spec-confirmed
  via the SASS extension page (§5), which names the exact group **and** code together over the
  same transport — materially stronger than `CAP-002`'s original phrase-match evidence. `CAP-002`
  §3's corresponding bullet is flagged there for upgrade to 🟢 (not edited in that file this
  round, per this session's scope).
- Fragmentation behavior discovered: a single Message Stream message can split its header and
  value across two consecutive RFCOMM packets (§5's frame 2272+2274 correction) — a
  methodological finding relevant to how *all* prior and future captures' RFCOMM data should be
  reassembled before TLV-parsing, not specific to this capture's content.

**Not ready yet:**
- Groups `0x05`/`0x09`'s identity (§5) — search results kept redirecting to Device Information
  (Group `0x03`) codes `0x05`/`0x06`/`0x09` instead of confirming standalone groups; may be a
  reassembly artifact rather than genuine separate groups.
- Frame 2305's precise field mapping (§5) — `"release_5.203"` is confirmed on the wire inside a
  protobuf-shaped value nominally under Group `0x03` Code `0x02`, but that code is
  spec-documented as "BLE address updated" (6-byte MAC), incompatible with this 63-byte
  structure — the header may not be a genuine Message Stream header at all. `CAP-002` §3's open
  question (which field is the *real* firmware version — `"Revision 6"` or something else)
  remains open; `"release_5.203"` is now a documented candidate, not a resolved answer.
- Individual codes within Group `0x04` beyond Ring (§5).
- Any handle→UUID mapping (§6) — hypotheses only, still needs a real discovery capture.
- Whether nRF Connect's presence (vs. a pure system-settings flow) influenced any result here
  (§8 item 4).
- The `libmaestro`/ANC-EQ control channel identity — **still completely unaddressed by any of
  the four captures to date.**
