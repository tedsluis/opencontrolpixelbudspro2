# Findings: `CAP-002` (Group A fresh-pairing capture)

Standardized, evidence-based extraction from `btsnoop_hci.log` + `recording.mp4`, staged here
for later promotion into `PROTOCOL_NOTES.md` / `PROTOCOL.md` per `PROJECT_RULES.md` §2. Modeled
on `captures/2026-08-09_08-51-00_08-52-20-Group_Z/FINDINGS.md` (`CAP-001`). Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-002` · **Date:** 2026-08-09 · **Phone:** Pixel 7a (official app) ·
**Log file:** `btsnoop_hci.log` — a long-running, non-restarted snoop log spanning
08:50:32–17:10:58 (~8h20m, 50,468 packets total, shared with `CAP-001`); this capture's actual
window is the ~150s slice **17:04:35–17:07:05** (1,877 packets after slicing with
`editcap -A/-B`). **Video:** `recording.mp4` (114.2s, 17:04:53–17:06:46 local, on-screen
wall-clock overlay). **Devices:** phone `Google_7e:ca:81` (Pixel 7a, `E8:D5:2B:7E:CA:81`,
BD_ADDR partially visible on-screen in this capture — same phone as `CAP-001`), peer
`Google_cf:6e:07` (`04:00:6E:CF:6E:07`, the Buds/case — confirmed the **same physical device** as
`CAP-001`, both from the on-screen "Device's Bluetooth address" and the classic-link BD_ADDR in
the log).

**Scope note:** unlike `CAP-001` (a reconnection reusing a stored link key), this is a **genuine
first-time pairing** — the app deletes any stored link key before connecting and a full Secure
Simple Pairing (SSP) exchange occurs. It also captures the complete first-run flow through the
Pixel Buds app's own CompanionDeviceManager (CDM) permission screen and its "Device details"
screen loading for the first time. See `EVENT-NOTES.md` in this folder for the full event-by-event
correlation this document is built on.

---

## 1. Pairing/bonding lifecycle (🟢 FACT)

Unlike `CAP-001`'s flaky 3-attempt reconnect, this pairing succeeds on the **first** attempt:

| Step | Time | Frame(s) | Detail |
|---|---|---|---|
| Delete stored link key | 17:05:26.717 | 653 | Confirms this is a deliberate fresh-pairing flow |
| Create Connection | 17:05:26.724 | 655 | |
| Connect Complete | 17:05:27.146 (status `0x00`) | 658 | Succeeds immediately, no Page Timeout |
| Link Key Request → **Negative Reply** | 17:05:27.168–27.172 | 679, 681 | Confirms no prior bonding material existed |
| IO Capability Request/Reply/Response | 17:05:27.173–27.183 | 685, 687, 697 | Secure Simple Pairing (SSP) negotiation begins |
| Simple Pairing Complete | 17:05:33.608 | 723 | ~6.4s after IO Capability exchange — matches the on-screen "Pair with..." dialog being shown to the user during this gap |
| Link Key Notification (new key stored) | 17:05:33.622 | 724 | |
| Authentication Complete → Set Connection Encryption → Encryption Change | 17:05:33.622–33.721 | 725–734 | |

The ~6.4s gap between IO Capability Response (log) and Simple Pairing Complete (log) is exactly
where the video shows the user reading the "Pair with Pixel Buds Pro 2 van Ted?" dialog, toggling
"Also allow access to contacts and call history" ON, then tapping **Pair** at 17:05:33 — i.e. SSP
here does **not** require a passkey/PIN comparison step visible to the user; the dialog is purely
a permission confirmation, and the cryptographic exchange itself completes silently in that
window. No passkey digits were ever displayed on screen.

## 2. RFCOMM channel topology (🟢 FACT / 🟡 HYPOTHESIS per channel)

Channel numbers are the RFCOMM server channel; all channels opened within one L2CAP/multiplexer
session over PSM 0x0003, starting immediately after encryption completes.

| Channel | DLCI | Opened (frame) | Content observed | Status |
|---|---|---|---|---|
| 0 | 0x00 | 908 | Multiplexer control (PN negotiation) | 🟢 FACT |
| 6 | 0x0c | 930, labeled **"Hands-Free"** directly by Wireshark's SDP-driven heuristic | 937 | **No plaintext AT-command byte stream was found on this channel in this capture** — differs from `CAP-001`, where a full HFP AT handshake (`AT+BRSF`, `AT+CIND`, `AT+BIEV=2,100`, etc.) was clearly visible. See §5 open question. | 🔴 OPEN QUESTION |
| 4 | 0x08 | 933 | Short frames (4–69 bytes) in the initial 17:05:34.5–34.8 burst only; not decoded in this pass | 🔴 OPEN QUESTION |
| 5 | 0x0a | 978 | No data-carrying frames observed | 🔴 OPEN QUESTION (same as `CAP-001`) |
| 1 | 0x02 | 1125 | `0x7e`-delimited HDLC-style frames, same shape as `CAP-001`'s channel-1 traffic | 🟡 HYPOTHESIS — still unconfirmed byte-for-byte, but the structural match to `CAP-001` across two independent sessions (one reconnect, one fresh pair) is itself evidence this is a stable, real protocol, not noise |
| 2 | 0x04 | 1251, closed 17:05:42 (frame 1486), reopened 17:05:47 (frame 1541) | **Fast Pair Message Stream, Device Information group — spec-verified, see §3** | 🟢 FACT (framing + group + two field codes spec-verified 2026-08-10; upgraded from `CAP-001`'s 🔴) |

**Correction to `CAP-001`'s write-up:** `CAP-001`'s `FINDINGS.md` §2 speculated channel 2/DLCI
0x04's `0x7e`-delimited content might be AVRCP, based on an SDP record for AVRCP existing in that
session. In *this* capture, the `0x7e`-delimited content instead appears on **channel 1**, while
**channel 2** carries a clearly different, TLV-structured payload (§3). Channel *numbers* are
evidently **not stable identifiers across sessions** — RFCOMM server channel numbers are
negotiated per-connection, not fixed per profile. Future findings should key off payload
*content/structure*, not channel number, and any reference to "channel N" should always be
paired with the DLCI and a content description, not treated as a persistent label.

## 3. Fast Pair Message Stream — Device Information group (🟢 FACT, spec-verified 2026-08-10)

Channel 2 / DLCI 0x04 carries repeated frames of the exact byte shape
`[Group: 1B] [Code: 1B] [Length: 2B big-endian] [Length bytes of value]`. Example (frame 1267,
17:05:36.516, 47 bytes total):

```
03 0a 00 08 <8 bytes, changes every occurrence — see below>
03 01 00 03 da 2d b1
03 02 00 06 <6 bytes, changes every occurrence>
03 09 00 0a "Revision 6" (10 ASCII bytes)
07 10 00 00
```

**Spec verification performed 2026-08-10** by fetching Google's actual Fast Pair specification
pages directly (not a search-summary), specifically
`developers.google.com/nearby/fast-pair/specifications/extensions/messagestream` and
`.../extensions/deviceinformation`:

- **Overall framing — 🟢 FACT.** The Message Stream spec page, fetched directly, states the wire
  format verbatim as: *"Octet 0: uint8 Message group, Octet 1: uint8 Message code, Octet 2-3:
  uint16 Additional data length, Octet 4-n: Additional data"*, big-endian — an exact match to
  every frame observed on this channel in both `CAP-001` and `CAP-002`. `PROTOCOL.md` §2.1
  Hypothesis A can be promoted from HYPOTHESIS to **FACT for the framing itself** (not yet for
  every field's meaning — see below).
- **Group `0x03` = "Device Information" — 🟢 FACT.** Confirmed as the documented group ID on the
  `deviceinformation` spec page.
- **Code `0x01` = "Model ID", value `da 2d b1` — 🟢 FACT.** The spec page gives a worked example
  of exactly this shape — `0x03 0x01 0x00 0x03 <3-byte Model ID>` — and states the Model ID field
  is 3 bytes. Our observed value is 3 bytes (`da 2d b1`) and stays **constant** across all
  observed occurrences (frames 1267 and 1554, 11s apart) — consistent with a fixed, registered
  Fast Pair Model ID for this device, not a rotating field. This resolves the "not identified"
  open question from the previous version of this document.
- **Code `0x02` = "BLE address updated", value 6 bytes — 🟢 FACT.** The spec page's worked
  example is `0x03 0x02 0x00 0x06 <6-byte MAC>`, matching exactly. Our observed value **changes**
  between occurrences (`640ccd9a6ae3` at 17:05:36.516 vs a different 6-byte value at 17:05:47.657)
  — consistent with a rotating BLE (private/resolvable) address, exactly what a field named
  "BLE address updated" would be expected to do. This resolves the "6-byte rotating field" open
  question.
- **Code `0x09` = "Firmware version" — 🟢 FACT for field identity; value itself still 🔴 open.**
  The spec confirms code `0x09` carries *"the provider's firmware version as a string in utf-8
  encoding"*. Our observed value is the literal ASCII string `"Revision 6"`, reproduced
  identically in both `CAP-001` and `CAP-002`. The framing/field-identity match is now a FACT; the
  content itself is still odd — `"Revision 6"` doesn't look like `PROTOCOL.md` §0.1's confirmed
  firmware baseline (`release_5.203`), so it may be a protocol/schema revision rather than the
  buds' actual firmware build. **Open question, unchanged:** cross-check against the app's own
  displayed firmware version (visible under "More settings" in the app, not captured in this
  session) in a future capture.
- **Code `0x0a` (8-byte rotating value) — still 🔴 OPEN QUESTION, now spec-confirmed unassigned.**
  The fetched `deviceinformation` page's code table runs `0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
  0x07 (deprecated), 0x08, 0x09, 0x0B` — **`0x0A` is not listed at all**. This isn't a gap in our
  research; the code is genuinely absent from the documented table we retrieved. Either it's an
  undocumented/reserved code, or our retrieved page excerpt is incomplete. The value itself
  changes every occurrence, like Code `0x02`, so it's plausibly also address/session-related —
  speculative, not claimed as fact.
- **Group `0x07`, Code `0x41`, string `"in-use"` (frame 1578, 17:05:47.818) — upgraded from 🔴 to
  🟡, still not FACT.** A targeted search of Google's Fast Pair GATT/Key-based-Pairing
  specification surfaced the exact phrase **"Indicate in-use Account Key"** as a real, named Fast
  Pair procedure — but on the *GATT* characteristic-level spec page, not the *Message Stream*
  page. The English-phrase match is a meaningful coincidence worth recording, but this capture
  cannot confirm whether Message Stream group `0x07` is actually the same concept restated over
  this transport, or an unrelated use of the same word. Kept at HYPOTHESIS, not promoted further.

**Sources consulted (2026-08-10):**
- [Message Stream](https://developers.google.com/nearby/fast-pair/specifications/extensions/messagestream) — Google for Developers
- [Device information](https://developers.google.com/nearby/fast-pair/specifications/extensions/deviceinformation) — Google for Developers
- Fast Pair GATT/Key-based Pairing procedure spec (for the "Indicate in-use Account Key" phrase match)

> **Correction (2026-08-11), source: `CAP-004`'s `FINDINGS.md` §4.** This section treated the
> channel-2/DLCI-0x04 TLV content above as one finding, implicitly alongside the *separate*
> channel-4/DLCI-0x08 content documented in `CAP-001`'s `FINDINGS.md` §2 (`google-pixel-buds-
> pro-v1`, `Europe/Amsterdam`, protobuf-tag-framed, not the `[Group][Code][Length][Value]` TLV
> shape described here). `CAP-004` (Group S — Google Play Services disabled, Pixel Buds app
> uninstalled) shows these two are **not the same mechanism, and do not share the same
> dependency on Google Play Services**:
>
> - The *channel-2/DLCI-0x04* content described in this section — Group `0x03` Code `0x01`
>   (Model ID `da 2d b1`), Code `0x02` (BLE address updated), Code `0x09` (`"Revision 6"`) — is
>   **absent** in `CAP-004`: channel 2 is never even opened when GMS is disabled. This is
>   evidence the mechanism documented in *this* section is **GMS/Nearby-driven**, not
>   Buds-initiated as originally left open in §7 item 4 of `CAP-001`'s `FINDINGS.md`.
> - The *channel-4/DLCI-0x08* content from `CAP-001` reappears in `CAP-004` **unchanged**, GMS
>   disabled or not — that content is Buds-initiated, independent of GMS.
>
> **Broader lesson, applicable beyond this specific finding:** what this section originally
> treated as a single "device info exchange" is actually **two independent mechanisms**, on two
> different channels, with two different framings (protobuf-tag vs. Message-Stream TLV), and —
> now shown — two different real-world dependencies (GMS-driven vs. Buds-native). A shared
> theme ("device info sent around connection time") is not evidence of a shared mechanism;
> each channel/DLCI's content needs its own independent verification, per the same
> content-over-channel-number discipline already established for RFCOMM channel numbers
> (`CAP-001` `FINDINGS.md` §2's reusable note). Per `PROJECT_RULES.md` §3, this note supersedes
> the implicit "one finding" framing above without deleting it. See `CAP-004`'s `FINDINGS.md`
> §4 for the full byte-level evidence and `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `GFPS-001` row for
> the test-catalog-level status (🟡, mixed outcome).

## 4. No RFCOMM traffic during app setup — resolved: it moves to BLE/GATT (🟢 FACT)

All RFCOMM data-carrying frames in this entire ~150s capture window fall inside two short
bursts: **17:05:34.56–17:05:38.92** (the post-pairing SDP/profile-probing burst) and
**17:05:47.62–17:05:47.90** (channel 2's brief reopen). **Nothing at all** was found on RFCOMM
between 17:05:48 and the end of the sliced window (17:07:05).

**Update 2026-08-10 — explanation #2 from the previous version of this document is confirmed
correct:** the app-setup activity has a clear local Bluetooth footprint, it is simply on the
**secondary BLE/GATT transport** (`ARCHITECTURE.md` §1), not RFCOMM. Analyzing the 48 ATT packets
in this same window shows:

- **A fresh BLE (LE) connection is established at 17:05:40.830** (`LE Enhanced Connection
  Complete`, frame 1390) — separate from, and later than, the classic BR/EDR pairing (which
  completed at 17:05:33.7). MTU exchange (517 bytes) and a Device Name / Database Hash read
  follow immediately (frames 1411–1427). The **Database Hash** read (not a full
  Read-By-Group-Type service discovery) indicates Android is checking a *cached* GATT database
  against this device rather than re-discovering services from scratch — consistent with the
  phone having already seen this device's GATT layout during Fast-Pair BLE scanning before
  pairing even began.
- **A direct, plaintext GATT characteristic read returns `"Revision 6"` again — 🟢 FACT (frame
  1441, handle `0x0f2a`, Read Response, value `5265766973696f6e2036` = ASCII `"Revision 6"`).**
  This is the **same string** found via the RFCOMM Message Stream in §3, now confirmed via a
  completely independent transport and encoding (a raw GATT string read, no TLV framing needed).
  Two independent transports agreeing on the same value is strong corroboration that
  `"Revision 6"` is a real, stable property of this device (still not confirmed *which* real-world
  property — see §3's open question on whether it's firmware or a protocol/schema revision).
  Handle `0x0f2a` could not be resolved to a UUID in this capture (no service-discovery response
  visible — see the handle-mapping caveat below), but its behavior (plain UTF-8 string, no
  encryption) is consistent with a standard **Device Information Service (`0x180A`)**
  characteristic such as Firmware Revision String (`0x2A26`) or Software Revision String
  (`0x2A28`) — 🟡 HYPOTHESIS, not confirmed by UUID in this pass.
- **Four bursts of encrypted-looking GATT writes/notifications on handles `0x0c04`, `0x0c05`,
  `0x0c0a`, `0x0c0c`, `0x0c0d`, `0x0c13`, `0x0c14` (16–40 byte opaque values) at 17:05:41.2–41.9,
  17:05:42.7–42.9, 17:05:53.27–53.39, and 17:06:03.79–04.0 — 🟡 HYPOTHESIS.** Each burst follows
  the same shape: a 2-byte write to a `+1`-offset handle (`0x0c05` after `0x0c04`, value `01 00`,
  the standard CCCD "enable notifications" value) immediately followed by a write of an
  opaque 16-or-32-byte value to the base handle, then (in three of the four bursts) a
  Handle Value Notification of a similarly opaque value coming back. This structural
  pattern — CCCD-enable, encrypted request write, encrypted response notification — matches the
  general *shape* of Fast Pair's Key-based Pairing / Account Key GATT procedure as described in
  Google's spec, but this pass did not decrypt or byte-match the values against the spec, so it
  is **not** claimed as confirmed identification, only a structural resemblance.
- **Timing correlation with the video (🟢 FACT):** the 3rd burst (17:05:53.27–53.39) lands within
  the same second the app showed **"Pixel Buds Pro 2 van Ted connected"** (17:05:53 in
  `EVENT-NOTES.md`). The 4th burst (17:06:03.79–04.0) lands within the same second as the **Save**
  tap (17:06:04). This is a tight enough correlation to treat as causal, not coincidental,
  pending further verification. The Fast Pair "Save device" / GFPS step (explanation #3 from the
  previous version of this section) is therefore **not** purely cloud-side as speculated — at
  least some of it involves a local BLE/GATT exchange with the device itself, likely alongside
  whatever cloud/account-linking call also occurs.
- **UUID/handle mapping limitation:** no `Read By Group Type` (primary service discovery) response
  appears anywhere in this session's ATT traffic, so none of the above handles could be resolved
  to their actual 128-bit or 16-bit UUIDs from this capture alone — see §8 (new) for why, and why
  a same-device discovery capture is now the top recommended next step.

Explanation #4 (CDM permission flow being a pure OS/app-framework interaction with no Bluetooth
traffic) remains unconfirmed either way — none of the four ATT bursts fall inside the
17:06:11–17:06:31 CDM-dialog window specifically.

## 5. HFP channel opened but no AT-command traffic observed (🔴 OPEN QUESTION)

SDP resolves and RFCOMM channel 6 is explicitly opened and labeled **"Hands-Free"** by
Wireshark's own heuristic (frame 930/937), yet no `AT+`-prefixed ASCII bytes were found anywhere
on any DLCI in this capture (checked via a raw hex search for the `AT` byte pattern across the
whole sliced window — the one match was incidental, inside an unrelated channel-1 binary blob,
not a real AT command). This directly contrasts with `CAP-001`, where a complete, unambiguous HFP
AT handshake (`AT+BRSF`, `AT+CIND`, `AT+BIND`, `AT+BIEV=2,100`, ...) was captured on channel 4.

Possible explanations, not resolved here: the Service Level Connection (SLC) AT handshake may
happen fractionally before or after this capture's slice boundaries; it may require the phone to
actually initiate a call/audio-routing test to trigger; or first-time pairing may defer full HFP
SLC setup until first use rather than performing it immediately (unlike `CAP-001`'s reconnect,
where HFP was evidently already in active use). Worth checking directly with a wider time slice
in a future pass.

## 6. Other observations

- **CompanionDeviceManager usage confirmed on-screen (🟢 FACT, video evidence):** the system
  dialog "Allow the app Pixel Buds to access Pixel Buds Pro 2 van Ted?" (17:06:25–31) is the
  standard Android CDM association-consent prompt. This directly corroborates
  `DECISIONS.md` ADR-005's assumption that the official app uses CDM for its association step,
  though this is app-side UI evidence, not Bluetooth-log evidence of the CDM API call itself.
- **"Input device" toggle exists in Device details (🟢 FACT, video evidence):** alongside "Phone
  calls" and "Media audio", the app exposes a third toggle, "Input device", all three on by
  default. Combined with the SDP-discovered **HID-Control and HID-Interrupt L2CAP channels**
  opened during the post-pairing burst (frames 837–869), this suggests the Buds/case expose a
  **standard Bluetooth HID service**, most plausibly for translating touch/head gestures (or at
  minimum media-key style actions) into generic HID reports rather than requiring every gesture
  to go through the proprietary RFCOMM channel. This is a new architectural finding not
  previously noted in `PROTOCOL.md` or `ARCHITECTURE.md` and may be relevant to
  `ARCHITECTURE.md` §1's transport list, which currently only names RFCOMM (primary) and GATT
  (secondary) — HID would be a third local transport surface to account for.
- **The permission-toggle interaction inside the OS pairing dialog** ("Also allow access to
  contacts and call history", switched ON at 17:05:30–32) is a detail with no corresponding log
  evidence in this RFCOMM-focused pass, but is relevant context for `AGENTS.md` §9 (privacy
  posture) if this project's own app ever needs to request the equivalent permission.

## 7. Extended log tail check, 17:06:46–17:10:58 (🟢 FACT — checked before planning a new capture)

Per the recommendation to check for already-useful traffic before spending a new capture session,
the untrimmed tail of the shared log (17:06:46, where the video ends, through 17:10:58, the end
of the whole 8h20m log) was checked directly, without slicing to this device.

- 637 packets total in that window (245 ATT, 41 L2CAP, remainder HCI). At first glance this
  looked promising — it includes a **full GATT primary service discovery** (`Read By Group Type`
  requests/responses) starting 17:09:56, which is exactly the kind of traffic missing from §4's
  analysis and would have resolved the handle→UUID mapping gap.
- **However, this traffic belongs to a different device.** The discovery response's `Source
  Device Name` field reads **"Charge 6"** (a Fitbit Charge 6) at BD_ADDR `78:f8:1b:d6:6b:0a` —
  confirmed by filtering the tail explicitly for the Buds' own address
  (`04:00:6e:cf:6e:07`), which returns **zero packets** anywhere in this 17:06:46–17:10:58 window.
  This is unrelated background Bluetooth activity from another of the maintainer's devices,
  accumulated in the same long-running, non-restarted snoop log (see the header's scope note) —
  it must **not** be used as Buds evidence, and is recorded here only to document that it was
  checked and correctly excluded, not silently missed.
- **Conclusion: no additional, usable Buds-specific traffic exists anywhere in this log past
  17:06:46.** A new, dedicated capture — with Bluetooth restarted first, per
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §2 step 5 — is genuinely necessary to get a real primary
  service discovery against the Buds themselves and resolve the §4 handle/UUID gap, rather than
  something recoverable from this session's existing log.

## 8. Recommended next steps

1. **Highest priority:** a capture that includes a **full GATT service discovery** against the
   Buds (not a cached-database-hash reconnect) — e.g. by having Android forget and re-discover
   the device, or by using a generic BLE scanner tool — to resolve handle `0x0f2a` and the
   `0x0c0X` handle cluster from §4 to real UUIDs. §7 confirms this cannot be recovered from
   existing logs and needs a fresh, targeted capture.
2. A capture spanning further past the visible "Device details" screen to see whether HFP
   AT-command SLC setup (§5) or any `libmaestro`-shaped RFCOMM traffic eventually occurs once the
   user actually interacts with an ANC/EQ control from a fresh-paired state.
3. ~~Byte-for-byte verification of the Fast Pair Message Stream Device Information group/code
   values against the actual published spec~~ — **done, see §3** (2026-08-10).
4. ~~Revisit `CAP-001`'s `FINDINGS.md` §2~~ — **done**, see `CAP-001`'s `FINDINGS.md` correction
   note dated 2026-08-10, added per the same pattern `DECISIONS.md` uses for superseding ADRs
   (the original text is kept, not silently rewritten).
5. Restart Bluetooth (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §2 step 5) before the next capture — the
   shared, non-restarted 8+ hour snoop log made this session's analysis start with an unnecessary
   slicing step, and item §7 shows it also accumulates unrelated devices' traffic.
6. Attempt to decrypt or otherwise identify the four GATT write/notify bursts from §4 against
   Fast Pair's Key-based Pairing / Account Key procedure spec, now that item 1 above would supply
   the missing UUIDs needed to even attempt that.

## 9. Promotion readiness — what's ready for `PROTOCOL_NOTES.md`

Per `PROJECT_RULES.md` §2, new protocol knowledge is recorded first in `PROTOCOL_NOTES.md`
before being promoted to `PROTOCOL.md`. Assessment of what in this document already clears that
bar:

**Ready to promote now (🟢 FACT, spec- and/or cross-capture-verified):**
- The Fast Pair Message Stream framing itself (`[Group][Code][Length:2B-BE][Data]`) — §3 —
  promotes `PROTOCOL.md` §2.1 Hypothesis A's *framing* from HYPOTHESIS to FACT (the framing, not
  yet every message's meaning).
- Group `0x03` = Device Information, Code `0x01` = Model ID (value `da 2d b1`, constant), Code
  `0x02` = BLE address updated (6-byte, rotating) — §3.
- Group `0x03` Code `0x09` = Firmware version *field identity* (still open what the value
  `"Revision 6"` actually represents) — §3.
- The pairing/bonding lifecycle sequence for a fresh pair (delete-key → create-connection →
  negative-link-key-reply → IO-capability/SSP → simple-pairing-complete → new-link-key) — §1.
- CompanionDeviceManager is used by the official app (video-confirmed) — §6, corroborating
  `DECISIONS.md` ADR-005.

**Not ready yet (needs more evidence before promotion):**
- Code `0x0a`'s meaning (§3) — genuinely undocumented in the spec page retrieved; needs either a
  more complete spec source or its own targeted investigation.
- The `0x0c0X` GATT handle cluster's identity and the Key-based-Pairing structural resemblance
  (§4) — needs the UUID-resolving capture from §8 item 1 before any claim beyond "structurally
  resembles".
- Group `0x07` Code `0x41` = `"in-use"` (§3) — phrase-match only, not a confirmed protocol-level
  identification.
- Any HFP AT-command behavior claim specific to fresh pairing (§5) — this capture shows *absence*
  of AT traffic, which is itself not strong enough evidence to assert HFP behaves differently on
  first pairing vs. reconnect; needs the wider-window capture from §8 item 2.
- The `libmaestro`/ANC-EQ control channel identity — **still completely unaddressed by either
  `CAP-001` or `CAP-002`**; both captures only cover pairing/setup, not actual ANC/EQ commands
  under clean isolation (see `CAP-001`'s own §5 and §7 for that separate, still-open thread).
