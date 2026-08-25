# DESKRESEARCH_FINDINGS.md

Offline, script-based pattern analyses that correlate or re-examine **existing**
captures — no new Bluetooth capture session involved. This is distinct from a
`CAP-NNN-FINDINGS.md` file, which documents first-pass findings from one
specific capture: an entry here typically spans multiple `CAP-NNN` captures
(e.g. "check this byte pattern across all four existing logs") or re-applies a
new hypothesis to data already on disk.

Findings here are subject to the same rules as anywhere else:
`PROJECT_RULES.md` §1 (FACT/HYPOTHESIS/ASSUMPTION labeling, evidence
traceability) and §2 (findings are promoted directly into `PROTOCOL.md` once
confirmed — there is no intermediate buffer). A deskresearch finding is not a
substitute for a purpose-built capture/experiment where one is warranted — see
`PROJECT_RULES.md` §4 on hypothesis tests; a deskresearch correlation against
existing data is weaker evidence than a fresh, purpose-built capture and
should be labeled accordingly (see e.g. `PROTOCOL.md` §4.1's "Verified with
experiment" note).

Status legend (consistent with `PROTOCOL.md` §0):

- 🟢 **FACT** — observed and repeatedly confirmed.
- 🟡 **HYPOTHESIS** — observed or plausible, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not yet tested, assumed based on comparable/official
  protocols or an older Pixel Buds generation.
- 🔴 **OPEN QUESTION** — genuinely unresolved: no specific hypothesis or working
  assumption exists yet, only an identified gap (e.g. this document's own
  2026-08-17 entry flags the `0x18`/`0x1a` inner-field-2 correlation this way).

---

## Template per entry

```
### <Date> — <short title>

- **Trigger:** why this analysis was run (e.g. an open question in `PROTOCOL.md` §6).
- **Method:** the exact command(s) used (per `PROJECT_RULES.md`'s hex/script
  rule — every decoding of a burst/packet includes the specific
  terminal/python command AND the raw hex bytes it operated on).
- **Captures examined:** which `CAP-NNN` log(s), by ID.
- **Result:** what was found, with status label.
- **Promoted to:** the `PROTOCOL.md` section this was written into, once
  promoted (leave blank until promotion happens).
```

---

## Entries

### 2026-08-17 — DLCI 0x02 cross-capture structural pass: EQ-envelope generalization check, and a new address-instability finding

- **Trigger:** `CAP-005-FINDINGS.md` §6's explicit open item ("Whether DLCI 0x02's field-16/18
  pair is EQ-specific or a general-purpose `libmaestro` 'apply/save' pair also used by ANC/other
  settings — needs a differently isolated capture (e.g. ANC-only) to check"), raised during a
  broader cross-capture consistency review of every capture session's findings/event-notes against
  `PROTOCOL.md`/`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`. `CAP-006` (a clean, single-tap-per-window repeat
  of all four ANC modes) already exists and satisfies exactly the "differently isolated ANC-only
  capture" the open item called for — no new capture was needed to check this.

- **Method:** applied `CAP-005-FINDINGS.md` §5a's exact decode pipeline (HDLC unescape, LEB128
  address/control parse, CRC-32/IEEE-802.3/zlib verify, then the three nested-length
  self-consistency assertions `payload[14]==len(payload)-15`, `payload[16]==len(payload)-17`,
  `payload[19]==len(payload)-20` that identify the EQ envelope's specific shape) against **every**
  DLCI 0x02 Sent-direction payload in `CAP-001`, `CAP-002`, `CAP-003`, `CAP-006`, `CAP-007`, and the
  11:42 `CAP-010` session (`CAP-004` and the 18:30 `CAP-017` session never open DLCI 0x02 at all —
  confirmed via `tshark -r <log> -Y "btrfcomm.dlci==0x02 and btrfcomm.len>0"`, 0 rows for both).

  Extraction per log:
  ```
  tshark -r <CAP-NNN-btsnoop_hci.log> -Y "btrfcomm.dlci==0x02 and btrfcomm.len>0" \
    -T fields -E separator='|' -e frame.number -e frame.time_epoch -e frame.p2p_dir \
    -e btrfcomm.len -e data.data
  ```
  **Correction made mid-pass, load-bearing for the result:** a first version of the decode script
  assumed one HDLC frame (`0x7e ... 0x7e`) per RFCOMM payload, which produced 68 spurious CRC
  failures. Root cause: RFCOMM payloads on this DLCI routinely pack multiple complete HDLC
  sub-frames back-to-back (e.g. `CAP-001` frame 1361 contains 7 sub-frames in one 312-byte RFCOMM
  I-frame) — exactly the "split each RFCOMM payload on the 0x7e flag byte" method
  `CAP-001-FINDINGS.md` §2 already documents, which the first version of this pass had not
  followed. Splitting on `0x7e` first (`raw.split(b'\x7e')`, discarding empty pieces) before
  decoding each piece independently brought CRC mismatches to **0 of 1,231** sub-frames across all
  7 logs — itself a re-confirmation, at larger scale, of `PROTOCOL.md` §2.2a's CRC-32 finding.

- **Captures examined:** `CAP-001`, `CAP-002`, `CAP-003`, `CAP-006`, `CAP-007`, `CAP-010` (11:42
  session). `CAP-004` and `CAP-017` (18:30 session) confirmed to never open DLCI 0x02.

- **Result — Part 1, the triggering question (🟡 HYPOTHESIS, strengthened by a clean negative):**
  the exact structural shape identified in `CAP-005` (all three nested-length checks passing)
  matches **only `CAP-005`'s own three already-known frames (1245, 1321, 1338) — zero matches in
  any other capture**, including `CAP-006`'s four cleanly isolated single ANC taps (checked at
  every payload length bucket present in each capture's Sent traffic: `CAP-006`'s Sent payloads
  are exclusively 13/17/21/22 bytes, never 45). This is the best available negative-result test for
  this question — a purpose-built, isolated ANC repeat, already on disk — and it comes back clean:
  **no ANC tap, in any capture, produces this DLCI-0x02 envelope shape, under any outer field
  number.** Not promoted to 🟢 FACT (still only one capture has ever produced this shape at all,
  so cross-capture *positive* replication for EQ itself is still needed independently of this
  question), but the specific "is it shared with ANC" question is answered: no evidence it is.

- **Result — Part 2, an unplanned finding from broadening the same pass (🟡 HYPOTHESIS, new):**
  classifying *every* Sent-direction DLCI-0x02 payload's HDLC Address field (not just the
  EQ-shaped ones) surfaced two addresses never documented before — `0x1e80` (7808) and `0x2680`
  (9856), both Sent-direction (phone→Buds), always control byte `0x03`, always appearing together
  within milliseconds of each other. A matching Rcvd-direction (Buds→phone) address, `0xe980`
  (59776), answers them.

  These three addresses are **absent from `CAP-001`, `CAP-002`, `CAP-003`, `CAP-006`, and the
  11:42 `CAP-010` session entirely** (all five show only the two already-documented addresses,
  `0x0000` and `0xD180`) and appear **only in `CAP-005` and `CAP-007`**:

  - `CAP-005`: one burst, frames 2278–2387, **15:06:11.760–15:06:14.774** — well past the video
    (ends 15:03:45) and past the two documented EQ actions (15:03:12/15:03:22/15:03:27), in the
    log's unattended post-session tail. Not present anywhere near the session's own initial
    connection-setup burst (~15:02:39–42).
  - `CAP-007`: three bursts — frames 788–989 (**09:14:18.1–09:14:21.2**, immediately after DLCI
    0x02 first opens per `CAP-007-EVENT-NOTES.md`'s own timeline), frames 1489–1604
    (**09:15:41.994–09:15:45.052**, i.e. within the exact ~1s window of `CAP-007-FINDINGS.md` §3.3's
    already-documented 09:15:38 bud-removal RFCOMM channel bounce), and frames 2057–2241
    (**09:18:43.392–09:18:56.004**, i.e. within `CAP-007-FINDINGS.md` §3.2's already-documented
    idle-silence-ends-and-DLCI-0x08-resumes moment at 09:18:43). **All three bursts land inside a
    connection-(re)open/channel-bounce window this project has already independently established
    from other channels' evidence** — this is a new data point *for* that existing
    channel-(re)initialization-triggers-a-generic-burst hypothesis (`CAP-007-FINDINGS.md` §3.3),
    now showing DLCI 0x02 also participates in it, not just DLCI 0x04/0x08.

  **Content, not just timing, ties this to already-documented material.** The `0xe980` Rcvd
  responses decode (same protobuf-tag method as `CAP-001-FINDINGS.md` §2) to the **same "device
  serial `1779298694` + firmware `release_5.203`, repeated 3×" content already documented on the
  `0x0000`→`0xD180` pair since `CAP-001`** — e.g. `CAP-007` frame 792:
  ```
  6b10bbb2afcb803422570a1b0a0a31373739323938363934120d72656c656173655f352e323033...
  (repeats the same 0x0a1b0a0a<serial>120d<firmware> sub-block three times)
  ```
  This is the same content, on a *different* address pair, in a session (`CAP-007`) that never
  used the official Companion App at all — only Android's own system Bluetooth "Device details"
  page. The correlation-ID/`call_id` fragments already characterized in `CAP-005-FINDINGS.md` §5b
  (e.g. `1d 05 d8 d5 73 25 ce 72 b7 73`) also reappear verbatim across *both* the old and new
  address pairs within the same session, confirming these are not coincidentally similar bytes —
  the Buds are echoing the same correlation scheme regardless of which address pair carried the
  request.

  One further, unresolved structural detail: in every observed occurrence, the request on address
  `0x1e80` carries an inner field-2 value of `0x18` (24) and the request on `0x2680` carries `0x1a`
  (26) — a 1:1 correlation between which HDLC address is used and which inner value accompanies
  it, with the rest of the payload (the fixed32 fields and the call-id tail) otherwise identical
  between the two. **Not decoded further** — could be two parallel/redundant pw_rpc calls, two
  distinct pw_rpc service method numbers that both happen to return device-info-shaped data, or
  something else; flagged rather than guessed at, per `AGENTS.md` §13's zero-creativity rule.

  **Reading, stated at the appropriate confidence level:** DLCI 0x02's HDLC Address field is not a
  small, fixed set of protocol-level constants (as `PROTOCOL.md` §2.2a's "two multiplexed pw_rpc
  channels" framing, based on only `0x0000`/`0xD180`, implied) — it looks instead like a
  **per-connection/per-reconnect-negotiated pw_rpc client/channel handle**, re-issued on
  reconnect/channel-bounce events, that happens to carry the same "device info" request each time
  regardless of which specific address value gets assigned. This extends
  `CAP-001-FINDINGS.md` §2's existing "RFCOMM server channel numbers are session-local, not
  profile-fixed" methodological note one layer deeper, into the addressing *within* DLCI 0x02
  itself — a genuinely new implication for `CodecRouter`/`FrameDecoder` design
  (`ARCHITECTURE.md` §5): DLCI 0x02's `FrameDecoder` cannot hardcode `0x0000`/`0xD180` as the only
  valid addresses and must not assume a fixed small address set persists across reconnects.

- **Promoted to:** `PROTOCOL.md` §6 (Commands & schemas) — both results added as dated open-item
  updates, 2026-08-17.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/DESKRESEARCH_FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/DESKRESEARCH_FINDINGS
