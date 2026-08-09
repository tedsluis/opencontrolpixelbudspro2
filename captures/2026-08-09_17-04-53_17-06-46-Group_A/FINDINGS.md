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
| 2 | 0x04 | 1251, closed 17:05:42 (frame 1486), reopened 17:05:47 (frame 1541) | **Fast Pair Message Stream framing, now positively identified — see §3** | 🟡 HYPOTHESIS (upgraded from `CAP-001`'s 🔴, see §3) |

**Correction to `CAP-001`'s write-up:** `CAP-001`'s `FINDINGS.md` §2 speculated channel 2/DLCI
0x04's `0x7e`-delimited content might be AVRCP, based on an SDP record for AVRCP existing in that
session. In *this* capture, the `0x7e`-delimited content instead appears on **channel 1**, while
**channel 2** carries a clearly different, TLV-structured payload (§3). Channel *numbers* are
evidently **not stable identifiers across sessions** — RFCOMM server channel numbers are
negotiated per-connection, not fixed per profile. Future findings should key off payload
*content/structure*, not channel number, and any reference to "channel N" should always be
paired with the DLCI and a content description, not treated as a persistent label.

## 3. Fast Pair Message Stream — Device Information group, confirmed (🟡 HYPOTHESIS, evidence-backed)

Channel 2 / DLCI 0x04 carries repeated frames of the exact byte shape
`[Group: 1B] [Code: 1B] [Length: 2B big-endian] [Length bytes of value]` — which is precisely the
**officially documented Fast Pair Message Stream framing** already recorded as Hypothesis A in
`PROTOCOL.md` §2.1. Example (frame 1267, 17:05:36.516, 47 bytes total):

```
03 0a 00 08 <8 bytes, changes every occurrence — see below>
03 01 00 03 da 2d b1
03 02 00 06 <6 bytes, changes every occurrence>
03 09 00 0a "Revision 6" (10 ASCII bytes)
07 10 00 00
```

- **Group `0x03`, Code `0x09`, value `"Revision 6"` (🟡 HYPOTHESIS, moderately strong):** a
  targeted web search of Google's own Fast Pair Message Stream / Device Information spec
  confirms **code `0x09` is documented as the "Firmware version" property**, expected to carry a
  string value. Here it carries the literal ASCII string `"Revision 6"`. This is a strong
  structural match to the official spec (right code, right wire shape, right general
  category — a version-like string), but "Revision 6" is unlikely to be a real Buds firmware
  version (`PROTOCOL.md` §0.1 records `release_5.203` as the confirmed baseline) — it may be a
  *protocol/schema* revision number for the Message Stream implementation itself, or a
  companion-app-internal revision, not the firmware string shown in the app's "More settings"
  screen. Needs a capture that also shows the app's own displayed firmware version at the same
  moment to cross-check.
- **This exact structure was already present in `CAP-001`** (a *reconnect*, not a fresh pair),
  at essentially the same relative position in the connection sequence, including the literal
  string `"Revision 6"` — reproducible across two independent sessions on two different pairing
  types. Per `PROJECT_RULES.md` §1, repeated independent observation is what justifies raising
  confidence, though a byte-for-byte spec citation (rather than a web-search summary) would be
  needed before promoting this to 🟢 FACT.
- **The 8-byte value under Code `0x0a` and the 6-byte value under Code `0x02` change on every
  occurrence** (confirmed: two occurrences 11s apart within this same session, frames 1267 and
  1554, both differ in these fields while Code `0x01`'s 3-byte value `da 2d b1` and Code `0x09`'s
  `"Revision 6"` stay constant). This is consistent with a nonce, session identifier, or
  challenge/response field — flagged as an open question, not attributed further here.
- **Code `0x01`'s value `da 2d b1`** does not match Google's registered Bluetooth SIG company ID
  bytes in an obvious way and was not identified in this pass — open question.
- **A second, distinct sub-payload on the same channel** (frame 1578, 17:05:47.818, `Group 0x07
  Code 0x41`) contains the literal ASCII string `"in-use"` — plausibly related to Fast Pair's
  account-key-in-use concept from the official spec, but not confirmed against spec text in this
  pass.

## 4. No RFCOMM traffic during app setup / CDM permission / Device-details load (🔴 OPEN QUESTION)

All RFCOMM data-carrying frames in this entire ~150s capture window fall inside two short
bursts: **17:05:34.56–17:05:38.92** (the post-pairing SDP/profile-probing burst) and
**17:05:47.62–17:05:47.90** (channel 2's brief reopen). **Nothing at all** was found between
17:05:48 and the end of the sliced window (17:07:05) — confirmed by also checking a wider
17:06:46–17:09:00 tail slice of the full log (190 packets, zero RFCOMM frames) and by searching
the entire 17:05:00–17:15:00 window for any `AT+BIEV`-shaped bytes (none found).

This means the entire visible app flow after ~17:05:48 — the Fast Pair "Save device" dialog, the
Pixel Buds app's own "Set up"/"Allow a connection"/CompanionDeviceManager permission dialog, and
the "Device details" screen populating with Sound/Hearing wellness/ANC/EQ options and three
audio-routing toggles — happens **without any new local Bluetooth (RFCOMM) traffic** in this
capture. Plausible explanations, none confirmed here:

1. The Device Information already exchanged in §3 (name/revision/etc.) is enough for the initial
   UI to render without a live query.
2. Any further per-feature state (ANC mode, EQ, battery) is read over the **secondary BLE/GATT
   transport** (`ARCHITECTURE.md` §1), which this pass did not extract (only RFCOMM was
   analyzed — see Recommended next steps).
3. The Fast Pair "Save device"/GFPS account-linking step is genuinely cloud/GMS-side and has no
   local Bluetooth footprint, which would explain the silence specifically during 17:05:51–17:06:04.
4. The CDM permission flow (17:06:11–17:06:31) is a pure Android OS/app-framework interaction and
   was never expected to produce Bluetooth traffic.

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

## 7. Recommended next steps

1. Extract and analyze the **BLE/GATT (ATT) traffic** from this same window (48 ATT packets were
   present per the protocol-count summary but not examined in this pass) — likely where any live
   query behind the "Device details" screen actually happens, per §4's open question.
2. A capture spanning further past the visible "Device details" screen (this one ends at
   17:06:46 with the log confirming zero RFCOMM activity through 17:09:00) to see whether HFP
   AT-command SLC setup (§5) or any `libmaestro`-shaped RFCOMM traffic eventually occurs once the
   user actually interacts with an ANC/EQ control from this fresh-paired state.
3. Byte-for-byte verification of the Fast Pair Message Stream Device Information group/code
   values (§3) against the actual published spec document (not just a web-search summary), and
   an attempt to identify the `da 2d b1` constant and the two rotating fields.
4. Revisit `CAP-001`'s `FINDINGS.md` §2 in light of §2's correction here — its channel
   1/AVRCP-vs-channel-2 attribution should be re-checked against this capture's cleaner picture
   once a maintainer decision is made on whether/how to amend already-published capture findings.
5. Restart Bluetooth (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §2 step 5) before the next capture — the
   shared, non-restarted 8+ hour snoop log made this session's analysis start with an unnecessary
   slicing step (see `EVENT-NOTES.md`'s process note).
