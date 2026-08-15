# Findings: `CAP-002` (Group A fresh-pairing capture)

Standardized, evidence-based extraction from `CAP-002-btsnoop_hci.log` + `CAP-002-recording.mp4`, staged here
for later promotion directly into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Modeled
on `captures/CAP-001-2026-08-09_08-51-00_08-52-20-Group_Z/CAP-001-FINDINGS.md` (`CAP-001`). Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-002` · **Date:** 2026-08-09 · **Phone:** Pixel 7a (official app) ·
**Log file:** `CAP-002-btsnoop_hci.log` — a long-running, non-restarted snoop log spanning
08:50:32–17:10:58 (~8h20m, 50,468 packets total, shared with `CAP-001`); this capture's actual
window is the ~150s slice **17:04:35–17:07:05** (1,877 packets after slicing with
`editcap -A/-B`). **Video:** `CAP-002-recording.mp4` (114.2s, 17:04:53–17:06:46 local, on-screen
wall-clock overlay). **Devices:** phone `Google_7e:ca:81` (Pixel 7a, `E8:D5:2B:7E:CA:81`,
BD_ADDR partially visible on-screen in this capture — same phone as `CAP-001`), peer
`Google_cf:6e:07` (`04:00:6E:CF:6E:07`, the Buds/case — confirmed the **same physical device** as
`CAP-001`, both from the on-screen "Device's Bluetooth address" and the classic-link BD_ADDR in
the log).

**Scope note:** unlike `CAP-001` (a reconnection reusing a stored link key), this is a **genuine
first-time pairing** — the app deletes any stored link key before connecting and a full Secure
Simple Pairing (SSP) exchange occurs. It also captures the complete first-run flow through the
Pixel Buds app's own CompanionDeviceManager (CDM) permission screen and its "Device details"
screen loading for the first time. See `CAP-002-EVENT-NOTES.md` in this folder for the full event-by-event
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
| 1 | 0x02 | 1125 | `0x7e`-delimited HDLC-style frames, same shape as `CAP-001`'s channel-1 traffic | 🟢 FACT (2026-08-12) — confirmed as Pigweed `pw_hdlc` framing (CRC-32 FCS verified 100% match, including this session's own 371 frames); see `CAP-001-FINDINGS.md` §2 "Upgrade (2026-08-12)" and `PROTOCOL.md` §2.2a for the full evidence |
| 2 | 0x04 | 1251, closed 17:05:42 (frame 1486), reopened 17:05:47 (frame 1541) | **Fast Pair Message Stream, Device Information group — spec-verified, see §3** | 🟢 FACT (framing + group + two field codes spec-verified 2026-08-10; upgraded from `CAP-001`'s 🔴) |

**Correction to `CAP-001`'s write-up:** `CAP-001`'s `CAP-001-FINDINGS.md` §2 speculated channel 2/DLCI
0x04's `0x7e`-delimited content might be AVRCP, based on an SDP record for AVRCP existing in that
session. In *this* capture, the `0x7e`-delimited content instead appears on **channel 1**, while
**channel 2** carries a clearly different, TLV-structured payload (§3). Channel *numbers* are
evidently **not stable identifiers across sessions** — RFCOMM server channel numbers are
negotiated per-connection, not fixed per profile. Future findings should key off payload
*content/structure*, not channel number, and any reference to "channel N" should always be
paired with the DLCI and a content description, not treated as a persistent label.

## 2a. 2026-08-12 follow-up: channel-4/DLCI-0x08 burst fully decoded (resolves §2's 🔴 OPEN QUESTION row)

§2's table above marks Channel 4/DLCI 0x08 🔴 OPEN QUESTION: "Short frames (4–69 bytes) in the
initial 17:05:34.5–34.8 burst only; not decoded in this pass." A deskresearch pass (Python,
`tshark -T fields`/`data.data`, stream-reassembling `[Group][Code][Len:2B-BE][Value]` parser — see
`CAP-004` `CAP-004-FINDINGS.md` §5a for the reproducible script and the same method applied there) decodes
this burst completely. It uses the identical private Group/Code/Length envelope already documented
on this same DLCI in `CAP-001` `CAP-001-FINDINGS.md` §2 (the `google-pixel-buds-pro-v1`/`Europe/Amsterdam`
content) — **this session's burst is markedly richer** (39 messages vs. `CAP-001`'s and `CAP-004`'s
~20–30), because this is a genuine first-time pairing with a longer capability-negotiation
exchange. Full burst, 17:05:34.509–34.601 (frames 48917–49030 in this file's own numbering):

```
frame 48981  050c0000                                                              Group=0x05 Code=0x0c Len=0
frame 48983  04020000                                                              Group=0x04 Code=0x02 Len=0
frame 48985  04040000                                                              Group=0x04 Code=0x04 Len=0
frame 48986  04110000                                                              Group=0x04 Code=0x11 Len=0
frame 48988  04130000                                                              Group=0x04 Code=0x13 Len=0
frame 48991  04150000                                                              Group=0x04 Code=0x15 Len=0
frame 48993  0e040000                                                              Group=0x0e Code=0x04 Len=0
frame 48999  050a000d0a073731336638353510401800                                    Group=0x05 Code=0x0a Len=13  pb: field1="713f855" field2=64 field3=0
frame 49001  0301001b089f0310e298bbb80e1a104575726f70652f416d7374657264616d09030000
             Group=0x03 Code=0x01 Len=27  pb: field1=415 field2=3876506722 field3="Europe/Amsterdam"
             + Group=0x09 Code=0x03 Len=0   (both packed in one 35-byte write, unlike CAP-004's fragmented instance)
frame 49002  0403000410051864                                                      Group=0x04 Code=0x03 Len=4   pb: field2=5 field3=100
frame 49004  040500020803                                                          Group=0x04 Code=0x05 Len=2   pb: field1=3
frame 49007  0412000408021001                                                      Group=0x04 Code=0x12 Len=4   pb: field1=2 field2=1
frame 49009  041400020801                                                          Group=0x04 Code=0x14 Len=2   pb: field1=1
frame 49014  041600020802                                                          Group=0x04 Code=0x16 Len=2   pb: field1=2
frame 49020  0e02001a0a18676f6f676c652d706978656c2d627564732d70726f2d7631         Group=0x0e Code=0x02 Len=26  pb: field1="google-pixel-buds-pro-v1"
frame 49024  0e0100230a210a03616c6c121a0a060864100118010a060864100118020a060839100118032001
             Group=0x0e Code=0x01 Len=35  pb: field1="all" + nested field2 x3 (varint triples)
frame 49028  0302003f08061001220d72656c656173655f352e3230332a0030e60138004a0737313366383535500060b1dbe80670027801a80101b00101ba01020102c00101c80101
             Group=0x03 Code=0x02 Len=63  pb — byte-for-byte identical to CAP-004 frame 2305
             (field4="release_5.203", field9="713f855", see CAP-004 CAP-004-FINDINGS.md §5a Task 2)
frame 49030  090200020800                                                          Group=0x09 Code=0x02 Len=2   pb: field1=0
```

Immediately followed, still within the same burst window (frames 49052–49089), by content **not
seen in `CAP-001` or `CAP-004`'s captured windows at all** — new groups `0x01` and `0x02`:

```
frame 49066  0107000e08807d101818b009200228003001                                 Group=0x01 Code=0x07 Len=14  pb: field1=16000 field2=24 field3=1200 field4=2 field5=0 field6=1
frame 49081  020500020800                                                          Group=0x02 Code=0x05 Len=2   pb: field1=0
frame 49083  020400170801180020002a0d72656c656173655f352e3230333803               Group=0x02 Code=0x04 Len=23  pb: field1=1 field3=0 field4=0 field5="release_5.203" field7=3
frame 49084  020500020801                                                          Group=0x02 Code=0x05 Len=2   pb: field1=1
frame 49086  020400410801180020012a0d72656c656173655f352e323033380442093637373536323631374a0863617065325f736d5a133530306d0a3530306e0a3530306f0a35303070
             Group=0x02 Code=0x04 Len=65  pb: field1=1 field3=0 field4=1 field5="release_5.203" field7=4
             field8="677562617" field9="cape2_sm" field11="500m\n500n\n500o\n500p" (raw bytes, contains \n)
frame 49087  020b00080801120435303070                                             Group=0x02 Code=0x0b Len=8   pb: field1=1 field2="500p"
frame 49089  02060004080b1001                                                     Group=0x02 Code=0x06 Len=4   pb: field1=11 field2=1
```

Group `0x02` Code `0x04`'s value is the richest single field found on this DLCI across all four
captures: it repeats `"release_5.203"` (`PROTOCOL.md` §0.1's confirmed firmware baseline) a third
time (alongside `CAP-004`'s frame 2305 and this same burst's frame 49028), plus what look like
internal hardware/board codenames (`"cape2_sm"`) and config-variant identifiers (`"500m"`,
`"500n"`, `"500o"`, `"500p"`, newline-separated) — plausibly per-preset or per-profile identifiers,
not yet mapped to a user-visible feature.

**Answering this task directly:** yes to protobuf tags (every group's value on this DLCI decodes
as well-formed protobuf), no to Magic Bytes in the `PROTOCOL.md` §2.2 Hypothesis-B sense (no fixed
sync byte precedes each message — the envelope is `[Group:1][Code:1][Len:2BE][Value]`, matching
Hypothesis A's *shape*, just on a private/vendor Group namespace rather than the official one), and
yes to "one-time setup handshake" — every group/code pair in this table occurs **only within the
first ~1 second after DLCI 0x08 opens**, in all four captures, with the sole exception of Group
`0x04` Code `0x12`, which recurs every few seconds for the rest of the session (see `CAP-004`
`CAP-004-FINDINGS.md` §5a) as an apparent periodic status ping. §2's table row above is updated in status
from 🔴 OPEN QUESTION to 🟢 FACT for "this is a decodable, cross-capture-consistent private
Group/Code/Length envelope, not raw undecoded noise" — the *identity/purpose* of the private
protocol itself (is this `libmaestro`'s own setup handshake, or a lower-level Nearby/CDM companion
negotiation independent of Fast Pair?) remains 🔴 open, unchanged.

> **Task 2 (2026-08-12): spec research on Groups `0x01`/`0x02` — negative result, and why that's
> the expected outcome, not a research gap.** Targeted lookups against every Fast Pair Message
> Stream extension page found so far — Device Information (`0x03`), Device Action (`0x04`), Change
> Capability (`0x06`), SASS (`0x07`), Hearable Controls (`0x08`), Acknowledgement (`0xFF`),
> Personalized Name (uses a different, GATT-based framing, no Message Stream group at all) — find
> **no page documenting a standalone Message Group `0x01` or `0x02`.** Unlike Code `0x0a` (§3's
> Task 4/6 addenda), which sits on DLCI 0x04, the *official* Message Stream channel, Groups `0x01`
> and `0x02` here sit on **DLCI 0x08** — the private, non-Fast-Pair-spec envelope already
> established (§2a above, `CAP-001-FINDINGS.md` §2, `CAP-004-FINDINGS.md` §5a) to reuse Group
> numbers `0x03`/`0x04`/`0x05`/`0x09`/`0x0e` for content unrelated to their official DLCI-0x04
> meanings. A negative search result for `0x01`/`0x02` is therefore the **expected**, consistent
> outcome, not a gap — there is no reason to expect DLCI 0x08's private numbering to intersect
> Google's official group IDs at all, and it doesn't. Left as 🔴 OPEN QUESTION for identity (same as
> Groups `0x05`/`0x09`, see `CAP-004-FINDINGS.md` §5a/Task 3 below), explicitly **not** attempted to
> resolve via generic "Fast Pair spec" search, per this task's own instruction to search narrowly.
>
> **Numeric-value comparison against audio-codec parameters, as requested — one plausible,
> unconfirmed match, rest inconclusive.** Group `0x01` Code `0x07`'s value (`field1=16000, field2=24,
> field3=1200, field4=2, field5=0, field6=1`, frame 49066): `16000` is a **recognizable, common
> audio sample rate** (16 kHz — "wideband"/"super-wideband" speech, used by e.g. the Bluetooth HFP
> mSBC codec and several LE Audio profiles) — a plausible, not confirmed, reading for `field1`.
> `24` does not cleanly match a common bit-depth (16/24/32-bit PCM would usually appear as `16` or
> `24` for depth specifically, so this is *possible* as bit-depth, but equally could be a channel
> count, gain, or unrelated index — no way to disambiguate from one value alone). `1200` does not
> match any standard Bluetooth audio bitrate/latency convention checked (typical SBC/AAC bitrates
> run in the tens-to-hundreds-of-kbps range, not 1200; 1200 ms would be an unusually long latency
> figure for an audio pipeline) — no plausible unit identified. **Group `0x02` Code `0x04`'s
> numeric fields** (`field1∈{0,1}`, `field7∈{3,4}`) are small enough to be simple flags/counters,
> not codec parameters. **Conclusion, stated as a negative result per this task's own instruction:**
> beyond the single plausible 16000 Hz sample-rate reading, no confirmed or even strongly
> suggestive codec-capability-negotiation match was found — the surrounding fields (hardware
> codename `"cape2_sm"`, build ID `"677562617"`/`"713f855"`, config variants `"500m"`–`"500p"`,
> firmware string `"release_5.203"`) point more toward a general **device/build-identity dump**
> than an audio-codec negotiation specifically, which better fits this whole burst's already-
> established character as a one-time capability/setup handshake (§2a) than a per-call codec
> negotiation would.
>
> **Out of scope, added 2026-08-15 (`PROJECT.md` non-goals):** this line of investigation is
> closed, not just inconclusive — even a confirmed codec-parameter reading would not be actionable
> for this app (audio/codec handling stays with the OS/Bluetooth stack, `ARCHITECTURE.md` §6).
> Recorded here so this specific hypothesis isn't re-opened from scratch in a future session.

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

> **Correction (2026-08-11), source: `CAP-004`'s `CAP-004-FINDINGS.md` §4.** This section treated the
> channel-2/DLCI-0x04 TLV content above as one finding, implicitly alongside the *separate*
> channel-4/DLCI-0x08 content documented in `CAP-001`'s `CAP-001-FINDINGS.md` §2 (`google-pixel-buds-
> pro-v1`, `Europe/Amsterdam`, protobuf-tag-framed, not the `[Group][Code][Length][Value]` TLV
> shape described here). `CAP-004` (Group S — Google Play Services disabled, Pixel Buds app
> uninstalled) shows these two are **not the same mechanism, and do not share the same
> dependency on Google Play Services**:
>
> - The *channel-2/DLCI-0x04* content described in this section — Group `0x03` Code `0x01`
>   (Model ID `da 2d b1`), Code `0x02` (BLE address updated), Code `0x09` (`"Revision 6"`) — is
>   **absent** in `CAP-004`: channel 2 is never even opened when GMS is disabled. This is
>   evidence the mechanism documented in *this* section is **GMS/Nearby-driven**, not
>   Buds-initiated as originally left open in §7 item 4 of `CAP-001`'s `CAP-001-FINDINGS.md`.
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
> (`CAP-001` `CAP-001-FINDINGS.md` §2's reusable note). Per `PROJECT_RULES.md` §3, this note supersedes
> the implicit "one finding" framing above without deleting it. See `CAP-004`'s `CAP-004-FINDINGS.md`
> §4 for the full byte-level evidence and `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `GFPS-001` row for
> the test-catalog-level status (🟡, mixed outcome).

> **Follow-up (2026-08-12), source: `CAP-004`'s `CAP-004-FINDINGS.md` §5a.** The correction directly above
> already separates this section's DLCI-0x04 content from `CAP-001`'s DLCI-0x08 content. `CAP-004`'s
> 2026-08-12 addendum sharpens this further: `CAP-004` frame 2305 and this file's own frame 49028
> (§2a below) are byte-for-byte identical 67-byte messages, both on DLCI 0x08, both reading
> `Group 0x03 Code 0x02` — the exact same Group/Code label this section uses for its own
> spec-verified DLCI-0x04 content. A pure-protobuf reading of that header's first byte is provably
> invalid (protobuf field number 0 does not exist), which rules out "coincidence" and confirms DLCI
> 0x08 runs its **own private Group/Code/Length envelope**, reusing this section's Group `0x03`
> numbering for an unrelated 63-byte protobuf value (containing `"release_5.203"`) that has nothing
> to do with this section's Code `0x02` = "BLE address updated" (6-byte MAC). **This closes the open
> question directly above ("is `libmaestro`... a custom Message Group ID... or a separate channel")
> for this specific pair of channels: DLCI 0x04 and DLCI 0x08 are confirmed as two independent
> Group/Code namespaces that happen to share numeric labels, not one shared namespace split across
> two channels.** See `CAP-004` `CAP-004-FINDINGS.md` §5a (Task 2) for the full byte-level argument, and §2a
> below for this file's own copy of the byte-identical frame.
>
> **Task 3 (2026-08-12): Group `0x07` Code `0x41` = `"in-use"` — upgraded from 🟡 HYPOTHESIS to 🟢
> FACT.** §3's bullet above (line ~205) found the phrase "Indicate in-use Account Key" on a
> *GATT-level* Fast Pair spec page (phrase-match only, kept at 🟡). `CAP-004` `CAP-004-FINDINGS.md` §5
> subsequently found Google's [SASS (Smart Audio Source Switching)](https://developers.google.com/nearby/fast-pair/specifications/extensions/sass)
> extension page, which states explicitly that it uses **Message Group `0x07`** and gives a code
> table including **`0x41` = "Indicate in use account key."** — an exact group **and** code match,
> over the *same* transport (Message Stream) this section is about, not a different one. Per this
> project's own promotion rule (spec byte-match ⇒ 🟢 FACT), this bullet is promoted: **Group `0x07`
> Code `0x41`, value `"in-use"` = SASS extension's "Indicate in use Account Key" message — 🟢
> FACT**, evidenced by frame 1578 (17:05:47.818) and the SASS spec page cited above. §3's original
> phrase-match text is left in place, non-destructively, for the record — only its status marker
> and this addendum's conclusion change.
>
> **Task 1 (2026-08-12): fragmentation/byte-offset check for this section's content — negative,
> i.e. NOT fragmented, checked across all 7 occurrences of the Group-0x03 burst found in this
> file's full, non-restarted 8h20m log** (frames 1004, 1826, 17610, 18895, 21195, 49251, 49538 —
> the first two predate this session, from `CAP-001`'s own capture window, since this log is the
> same shared buffer; the rest span the whole day). Every occurrence is one complete 47-byte RFCOMM
> UIH frame carrying all five TLV messages (`03 0a 00 08`+8B, `03 01 00 03`+3B, `03 02 00 06`+6B,
> `03 09 00 0a`+`"Revision 6"`, `07 10 00 00`) packed together — byte offsets sum exactly to 47 in
> every case, and `bthci_acl.pb_flag`/`continuation_to`/`reassembled_in` confirm no L2CAP/ACL-level
> segmentation on any of them (`tshark -r CAP-002-btsnoop_hci.log -Y "frame.number==<N>" -e bthci_acl.pb_flag -e bthci_acl.continuation_to -e bthci_acl.reassembled_in`).
> Reported per this project's "negative results are findings too" rule: fragmentation risk for this
> specific burst is ruled out, not merely unconfirmed.
>
> **Task 6 (2026-08-12): Code `0x0a`'s 8-byte value across all captures — no counter, timer, or
> MAC-derived pattern found; appears high-entropy/pseudorandom.** Extracted from the same 7
> occurrences above, plus `CAP-003`'s one occurrence (frame 2285, `2ef138bad86fbc63`):
> ```
> frame 1004   d426b434a22f9ea1    (co-occurring addr field: 77962c96681c)
> frame 1826   fbd84d69f8d3d47c    (addr:                    530cb4c8063d)
> frame 17610  2a45a6b7c0ab6a82    (addr:                    5551274fae59)
> frame 18895  7e816fa6243cdfe7    (addr:                    5551274fae59  -- SAME addr as 17610, DIFFERENT 0x0a value)
> frame 21195  e75e4b5e50484cd0    (addr:                    49c09f25ab9b)
> frame 49251  ebb47218d48ba6c6    (addr:                    640ccd9a6ae3)
> frame 49538  741c49380e816f7f    (addr:                    640ccd9a6ae3  -- SAME addr as 49251, DIFFERENT 0x0a value)
> CAP-003 frame 2285  2ef138bad86fbc63
> ```
> Two negative results, both load-bearing: (1) **not a simple incrementing/modulo-256 counter** —
> no byte position increments monotonically across occurrences; (2) **not simply derived from the
> co-occurring BLE address (Code `0x02`)** — twice in this data the address stays identical between
> two consecutive occurrences (17610↔18895, 49251↔49538) while the Code `0x0a` value still changes
> completely both times, ruling out any static function of the address alone. Consistent with a
> per-message nonce or salt; actual purpose remains 🔴 OPEN QUESTION — Google's own spec table for
> this Message Group (fetched 2026-08-10, above) does not list code `0x0a` at all.
>
> **Task 4 (2026-08-12): searched every other known Fast Pair extension page for code `0x0a` under
> a different group — negative result, documented explicitly rather than left unchecked.** Checked:
> [Find Hub Network / FMDN](https://developers.google.com/nearby/fast-pair/specifications/extensions/fmdn)
> (code `0x0A` exists there, but as a "Ranging Capability Request/Response" **Data ID** in the
> unrelated Precision Finding operation table — a different framing entirely from the Message
> Stream `[Group][Code][Length][Value]` structure this section is about, so not a match, despite
> the numeric coincidence); [SASS](https://developers.google.com/nearby/fast-pair/specifications/extensions/sass)
> (full 14-entry code table checked, `0x0a` absent); [Device Action](https://developers.google.com/nearby/fast-pair/specifications/extensions/deviceaction)
> and [Hearable Controls](https://developers.google.com/nearby/fast-pair/specifications/extensions/hearablecontrols)
> (codes documented there — `0x01`/`0x11`-`0x16` and `0x11`-`0x13` respectively — do not include
> `0x0a` either). **No group-independent or shared "code `0x0a`" convention was found anywhere.**
> Combined with the Device Information page's own code table deliberately running `...0x08, 0x09,
> 0x0B` (skipping `0x0A` outright, not merely omitting it from an incomplete excerpt, per the
> original 2026-08-10 research), this strengthens rather than resolves the open question: `0x0A`
> looks like a genuinely reserved or deliberately-unpublished code specifically within the Device
> Information group, not a documented cross-group convention — consistent with, but not proof of,
> it being an intentionally undocumented/private field Google chose not to publish. Left at 🔴 OPEN
> QUESTION, now on a more thoroughly checked basis.

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
  `CAP-002-EVENT-NOTES.md`). The 4th burst (17:06:03.79–04.0) lands within the same second as the **Save**
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

> **Update (2026-08-12) — the "wider time slice" check above is now done, and settles this
> question with a clean negative, not a slice-boundary artifact.** This file's underlying
> `CAP-002-btsnoop_hci.log` is not actually a pre-sliced ~150s file — it is the full, shared, non-restarted
> ~8h20m buffer (50,468 packets, 08:50:32–17:10:58) already documented in this file's own header
> and in `CAP-002`'s Capture Index row. Searching the **entire** file for the ASCII byte pattern
> `AT+` (`tshark -r CAP-002-btsnoop_hci.log -Y 'btrfcomm.len > 0 and data.data contains "AT+"'`) returns
> exactly **23 matches, all falling within 08:51:13.958–08:51:34.860** — i.e. all 23 belong to
> `CAP-001`'s own, already-documented HFP handshake (its `CAP-001-FINDINGS.md` §3), captured on DLCI 0x09
> in this same shared buffer. **Zero `AT+` matches exist anywhere else in the full 8+ hour log** —
> not during this session's own pairing/setup window (17:04–17:07), not during any of the other
> reconnect events visible in this buffer (e.g. 11:32, 12:05 — see §2a above), and not in the ~7
> hours of otherwise-idle time between them. This rules out the "happened just outside the slice
> boundary" explanation directly (there was no slice — the whole day was searched) and weakens the
> "requires a call/audio test to trigger" and "first-time-pairing defers SLC setup" explanations,
> since neither predicts *zero* AT traffic for the rest of the day once the phone had reconnected
> and presumably used the Buds normally afterward. Left as 🔴 OPEN QUESTION still (this capture
> alone cannot prove HFP SLC setup never happens under any condition), but the evidence now points
> toward "this specific phone/Buds pairing did not perform an HFP AT handshake again after
> `CAP-001`'s reconnect, for the rest of the day this buffer covers" rather than a capture-window
> artifact.

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
4. ~~Revisit `CAP-001`'s `CAP-001-FINDINGS.md` §2~~ — **done**, see `CAP-001`'s `CAP-001-FINDINGS.md`
   correction note dated 2026-08-10, added per the same pattern `DECISIONS.md` uses for superseding
   ADRs (the original text is kept, not silently rewritten).
5. Restart Bluetooth (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §2 step 5) before the next capture — the
   shared, non-restarted 8+ hour snoop log made this session's analysis start with an unnecessary
   slicing step, and item §7 shows it also accumulates unrelated devices' traffic.
6. Attempt to decrypt or otherwise identify the four GATT write/notify bursts from §4 against
   Fast Pair's Key-based Pairing / Account Key procedure spec, now that item 1 above would supply
   the missing UUIDs needed to even attempt that.

## 9. Promotion readiness — what's ready for `PROTOCOL.md`

Per `PROJECT_RULES.md` §2, new protocol knowledge is recorded first here, in this capture's
`CAP-NNN-FINDINGS.md`, before being promoted directly to `PROTOCOL.md`. Assessment of what in
this document already clears that bar:

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
- **Group `0x07` Code `0x41` = `"in-use"`** — spec-confirmed 2026-08-12 against Google's SASS
  extension page (exact group **and** code match) — §3 addendum. Moved out of "not ready yet."
- **DLCI 0x08's private Group/Code/Length envelope is a genuine, decodable, cross-capture-stable
  TLV framing** (2026-08-12, §2a) — resolves this file's own §2 🔴 OPEN QUESTION row for channel
  4/DLCI 0x08. The private protocol's *identity/purpose* stays open (see below), but "is this
  decodable structure or noise" is now settled.
- The Group-0x03 Device Information TLV burst documented in §3 does **not** fragment across RFCOMM
  packets, confirmed across all 7 occurrences found in this file's full 8h20m log (2026-08-12,
  §3 addendum) — a negative result worth recording per `PROJECT_RULES.md` rule 12.
- **Channel 1/DLCI 0x02's framing is Pigweed `pw_hdlc` (CRC-32 FCS verified, 100% match)** —
  §2's table updated 2026-08-12; see `PROTOCOL.md` §2.2a and `CAP-001-FINDINGS.md` §2 for the
  full evidence. Promotes `PROTOCOL.md` §2.2's framing placeholder to FACT for this channel.

**Not ready yet (needs more evidence before promotion):**
- Code `0x0a`'s meaning (§3) — genuinely undocumented in the spec page retrieved; the 2026-08-12
  addendum rules out two candidate explanations (simple counter; simple function of the
  co-occurring BLE address) but does not resolve the actual meaning — still needs a more complete
  spec source or its own targeted investigation.
- The `0x0c0X` GATT handle cluster's identity — needs the UUID-resolving capture from §8 item 1
  before any claim beyond "structurally resembles"; **the structural resemblance itself was
  substantially strengthened 2026-08-12** — see `CAP-003` `CAP-003-FINDINGS.md` §4's own 2026-08-12
  addendum for exact byte-length validation against the official Key-based Pairing/Passkey spec
  (16-byte AES blocks, CCCD-gated flow, cross-confirmed in `CAP-002` and `CAP-003`).
- Any HFP AT-command behavior claim specific to fresh pairing (§5) — **the "wider time slice"
  check is now done (2026-08-12, §5 addendum): zero `AT+` traffic anywhere in the full 8h20m log
  outside `CAP-001`'s already-documented handshake.** This is a clean negative for "AT traffic
  recurs later," but still does not fully explain *why* — kept 🔴 pending a dedicated HFP-focused
  capture.
- DLCI 0x08's private Group/Code/Length envelope's *purpose/identity* (§2a) — decodability is now
  a 🟢 FACT, but whether it's `libmaestro`'s own handshake or an unrelated companion-device
  negotiation remains 🔴 open.
- The `libmaestro`/ANC-EQ control channel identity — **still completely unaddressed by either
  `CAP-001` or `CAP-002`**; both captures only cover pairing/setup, not actual ANC/EQ commands
  under clean isolation (see `CAP-001`'s own §5 and §7 for that separate, still-open thread).
