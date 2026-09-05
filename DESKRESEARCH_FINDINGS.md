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

### 2026-08-28 — Extraction-path (`btsnoop` vs. `btsnooz`) truncation pattern across all captures

- **Trigger:** the 2026-08-28 project-wide audit's `XC-01` finding — the extraction-path pattern
  (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3's own inline PROPOSAL note, first raised from `CAP-032`'s
  single comparison against three prior sessions) is exactly the kind of "check this byte pattern
  across all existing logs" correlation this document exists for, but had never been consolidated
  here — only as scattered per-capture notes and one inline blockquote.

- **Method:** for every `CAP-NNN`'s extracted log, checked (a) the filename suffix
  (`-btsnoop_hci.log` = raw path, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3 step 3; `-btsnooz_hci.log` =
  `btsnooz.py`-from-bugreport fallback, §3 step 4) and (b) whether `frame.cap_len == frame.len` for
  every frame:
  ```
  capinfos <CAP-NNN-btsnoop(z)_hci.log>
  tshark -r <CAP-NNN-btsnoop(z)_hci.log> -T fields -e frame.number -e frame.cap_len -e frame.len \
    | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'
  ```

- **Captures examined:** every capture whose own `CAP-NNN-FINDINGS.md` already documented an extraction
  path or truncation result — `CAP-012`, `CAP-013`, `CAP-017`, `CAP-031` (all `btsnooz`-extracted),
  `CAP-032` (raw-extracted). (The 2026-08-28 project-wide audit's own re-verification pass additionally
  confirmed all other real captures — `CAP-001`–`CAP-011` excl. `012`/`013`, `CAP-014`–`CAP-016`,
  `CAP-019`–`CAP-025` — are untruncated, either raw-extracted or from a freshly-restarted log; not
  repeated here since none of those used the `btsnooz` fallback.)

- **Result (🟡 HYPOTHESIS — one data point per session, not a controlled test):**

  | Capture | Extraction path | `capinfos` inferred cap | Mismatched frames |
  |---|---|---|---|
  | `CAP-012` | `btsnooz` fallback | 15–126 bytes (range) | 254 / 1,436 |
  | `CAP-013` | `btsnooz` fallback | 15 bytes (flat) | 320 / 1,747 |
  | `CAP-017` | `btsnooz` fallback | ~15 bytes | 268 / 1,747 |
  | `CAP-031` | `btsnooz` fallback | 15–126 bytes (range) | 259 / 1,747 |
  | `CAP-032` | raw `btsnoop_hci.log` | none (uncapped) | 0 / 2,455 |

  4 of 4 `btsnooz`-extracted sessions came out severely ACL-truncated; the 1 raw-extracted session
  came out fully untruncated. This is consistent enough across 5 independent sessions to treat as
  a reliable *practical* rule — **always check `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3 step 3 (the raw
  file) first and prefer it whenever present** — but it remains 🟡 HYPOTHESIS, not 🟢 FACT: no
  single session has been extracted both ways for a direct controlled comparison, so this is 5
  data points agreeing, not an isolated causal test.

- **Promoted to:** `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3's existing PROPOSAL note (trimmed to point
  here, 2026-08-28), `TODO.md`'s "Known technical debt" section (2026-08-28).

### 2026-09-04 — Bonus battery/firmware cross-check across `CAP-004`/`CAP-007`/`CAP-016`/`CAP-033`

- **Trigger:** following `CAP-036-FINDINGS.md` §12's incidental battery/firmware bonus pass (a
  by-product of that session's own `OBS-004` analysis), the maintainer asked whether other
  existing captures were worth a similar secondary look. No new capture — this is a pattern check
  across logs already on disk, exactly this document's purpose.

- **Selection rationale:** of the 30+ existing sessions, most already have exhaustive
  battery/firmware treatment (`CAP-001`, `CAP-002`, `CAP-009`, `CAP-011`, `CAP-032`, `CAP-035`) or
  are severely ACL-truncated (`CAP-012`, `CAP-013`, `CAP-017`, `CAP-031` — short frames like these
  are exactly what truncation destroys). Four were picked for offering a genuinely different
  condition, each pre-verified untruncated (`capinfos` + `frame.cap_len==frame.len`, 0 mismatches
  in every case): `CAP-004` (GMS disabled **and** app uninstalled — the strongest independence
  test on disk), `CAP-033` (app force-stopped throughout, GMS untouched — a different
  independence axis), `CAP-016` (already flags an ANC-Notify byte question directly relevant to
  `CAP-036`'s own open item), `CAP-007` (a ~6-minute session with several multi-minute idle
  brackets — more natural-idle cadence data).

- **Method:** same pipeline as `CAP-036-FINDINGS.md` §12 — `bthci_acl.chandle`-scoped DLCI census,
  `_ws.col.Info`-based HFP AT-command extraction (some sessions' `data.data` field returns hex,
  others return the dissected ASCII text directly depending on the dissector chain — checked per
  session rather than assumed), and `data.data` hex extraction for DLCI 0x08/0x04 content.

#### Result 1 — `CAP-004`: Option C (HFP battery) confirmed independent of GMS *and* the app

```
$ tshark -r CAP-004-btsnoop_hci.log -Y "btrfcomm.len>0" -T fields -e frame.number -e frame.time \
    -e btrfcomm.dlci -e _ws.col.Info | grep -iE "BIEV|CIND"
2124  ...  0x0c  Rcvd AT+CIND=? ...
2146  ...  0x0c  Sent   +CIND: 0,0,0,0,0,3,0
2203  ...  0x0c  Rcvd AT+BIEV=1,1
2253  ...  0x0c  Rcvd AT+BIEV=2,100
2600  ...  0x0c  Rcvd AT+BIEV=2,100
2641  ...  0x0c  Rcvd AT+BIEV=2,100
```
`AT+BIEV=2,100` (HF Indicator #2, Battery Level) fires normally in this session, whose entire
point (Group S, `GFPS-001`) was Google Play Services **disabled** and the official app
**uninstalled** — the strongest GMS/app-independence condition of any capture on disk. DLCI 0x08's
battery triple also reproduces unchanged:
```
$ tshark -r CAP-004-btsnoop_hci.log -Y "btrfcomm.dlci==8 and btrfcomm.len>0" -T fields \
    -e frame.number -e data.data | grep "^0e01"
2315  0e0100230a210a03616c6c121a0a060864100118010a060864100118020a060824100118032001
```
Decodes to `[100,1,1]` (Left) / `[100,1,2]` (Right) / Case `0x24`=36 in the short 2-field form —
matching `AT+BIEV`'s Right=100 independently. **Result: Option C (HFP battery) and Option E (DLCI
0x08 battery triple) are both confirmed working with GMS disabled *and* the app uninstalled** —
🟢 FACT for this session, extending `CAP-035-FINDINGS.md`'s GMS-independence result (which only
checked DLCI 0x08/0x0a/0x06/0x12, not HFP) to Option C specifically, on a different session/date
than `CAP-035`'s own GrapheneOS test. As with `CAP-035`, DLCI 0x02/0x04 both stay absent in this
session (already documented in `CAP-004-FINDINGS.md`) — unaffected, not re-litigated here.

#### Result 2 — `CAP-033`: same independence result for app-force-stopped, plus a clean DLCI 0x02 usage/registration contrast

```
$ tshark -r CAP-033-btsnoop_hci.log -Y "btrfcomm.len>0" -T fields -e frame.number -e frame.time \
    -e btrfcomm.dlci -e _ws.col.Info | grep -iE "BIEV=2"
1475  ...  0x0c  Rcvd AT+BIEV=2,100
2130  ...  0x0c  Rcvd AT+BIEV=2,100
2185  ...  0x0c  Rcvd AT+BIEV=2,100
2239  ...  0x0c  Rcvd AT+BIEV=2,100
2268  ...  0x0c  Rcvd AT+BIEV=2,100
2301  ...  0x0c  Rcvd AT+BIEV=2,100
```
`AT+BIEV=2,100` fires repeatedly with the official app **force-stopped throughout the entire
session** — the condition `CAP-033`'s own procedure was built around. DLCI 0x08's battery triple
and Device Information (Model ID `da2db1`) also reproduce unchanged (commands identical to Result
1's pattern, frames 1604/1637, not repeated here). **New, additional to `CAP-033-FINDINGS.md`'s
own SDP-level finding** (that "MAESTRO APP"/DLCI 0x02's UUID is still *advertised* via SDP with
the app closed): a full-session DLCI census —
```
$ tshark -r CAP-033-btsnoop_hci.log -Y "btrfcomm" -T fields -e btrfcomm.dlci | sort | uniq -c
     32 0x00
     77 0x04
     55 0x08
      2 0x0a
     55 0x0c
```
— shows **zero** DLCI 0x02 frames of any kind (not even `SABM`/`UA` control frames) in this
entire 346-second session. **Result:** SDP *registration* (the service exists, per
`CAP-033-FINDINGS.md`) and the RFCOMM *channel actually being opened* are different claims — this
session cleanly separates them: DLCI 0x02 is registered but never used without the app running,
while DLCI 0x04/0x08/0x0c (battery, firmware, capability content) all function normally. Consistent
with, and sharpens, `DECISIONS.md` ADR-018's existing "DLCI 0x02 is the companion app's own
internal channel" finding.

#### Result 3 — `CAP-016`: second independent session reproducing the Settable-toggles-at-connect pattern that `CAP-036` also found

```
$ tshark -r CAP-016-btsnoop_hci.log -Y "btrfcomm.dlci==4 and btrfcomm.len>0" -T fields \
    -e frame.number -e frame.time -e data.data | grep "^0813"
1521  06:32:03.567  0813000401e80020   <- Settable=0x00, right after classic connect
2128  06:32:18.631  0813000401e8e880   <- Settable=0xe8, ANC "Transparency" highlighted on screen
...
3054  06:33:23.456  0813000401e80020   <- Settable=0x00 again, after an unrelated channel bounce
```
`CAP-016-FINDINGS.md` §4 already documents this as a 🟡 HYPOTHESIS: `settable-toggles=0x00`
correlates with the app's ANC row showing **no highlighted mode**, specifically observed
immediately after connect (frame 1521, "both buds still docked at this instant... part of the
immediate post-connect RFCOMM-channel-open burst") and again after a mid-session channel bounce —
i.e. whenever the accessory's capability data hasn't (re-)synced yet, not a fixed property of the
current ANC mode. **`CAP-036`'s own frame 1182 (`08 13 00 04 01 e8 00 20`, Settable=`0x00`,
Current=`0x20`/Off) — flagged there as an unreconciled discrepancy against every other
session's `0xe8`, `CAP-036-FINDINGS.md` §3/§11 — is structurally identical to `CAP-016`'s own
connect-time sample**, both firing at the very first Notify after a fresh RFCOMM connection. This
is a **second independent session** showing `Settable=0x00` specifically at connect-time,
strengthening (not yet promoting) `CAP-016`'s original HYPOTHESIS — the "discrepancy" flagged in
`CAP-036-FINDINGS.md` is best read as an instance of this same already-observed pattern, not a
new, separate anomaly. Still 🟡 HYPOTHESIS (2 sessions, not maintainer-reviewed for promotion).

Also reconfirms, incidentally: Model ID `da2db1` constant across this session's own two
reconnects (frames 1507/2992); the "BLE address updated" field (Code `0x02`) differs **between
those same two reconnects within one continuous log** (`49:c6:b1:16:78:70` vs `5b:7c:59:39:46:83`)
— new evidence that the rotation is per-connection, not merely per-day/per-boot as the
cross-session comparison in `CAP-036-FINDINGS.md` §12.3 alone could show.

#### Result 4 — `CAP-007`: one more idle-cadence data point, plus a new Case-encoding sample

```
$ tshark -r CAP-007-btsnoop_hci.log -Y "btrfcomm.len>0" -T fields -e frame.number -e frame.time \
    -e btrfcomm.dlci -e _ws.col.Info | grep "BIEV=2"
556   09:14:17.216  0x0c  Rcvd AT+BIEV=2,100
1150  09:14:43.012  0x0c  Rcvd AT+BIEV=2,100   (Δ25.8s)
1291  09:15:12.148  0x0c  Rcvd AT+BIEV=2,100   (Δ29.1s)
1647  09:16:02.085  0x0c  Rcvd AT+BIEV=2,100   (Δ49.9s)
1697  09:16:23.211  0x0c  Rcvd AT+BIEV=2,100   (Δ21.1s)
1777  09:17:02.073  0x0c  Rcvd AT+BIEV=2,100   (Δ38.9s)
1880  09:17:52.504  0x0c  Rcvd AT+BIEV=2,100   (Δ50.4s)
```
Widening gaps (21–50s) over a ~4-minute idle stretch — another data point consistent with
`PROTOCOL.md` §4.3 Option C's existing "settling burst, then irregular, median ~20s" model
(`CAP-009`'s 101-minute session remains the primary evidence for that model; this adds a
shorter, independent confirmation).

DLCI 0x08's battery triple shows a Case entry in the **short, 2-field form** (no `flag` byte) —
`0a 04 08 2d 18 03` (value `0x2d`=45, index=3) — constant for the whole session:
```
$ tshark -r CAP-007-btsnoop_hci.log -Y "btrfcomm.dlci==8 and btrfcomm.len>0" -T fields \
    -e frame.number -e data.data | grep "^0e01" | head -1
733  0e0100230a210a03616c6c121a0a060864100118010a060864100118020a04082d180318012001
```
`PROTOCOL.md` §4.3 Option E's addendum documents this short form as carrying a `0xff` (255)
*unknown-value sentinel* specifically. **This sample does not match that specific sentinel value**
(`0x2d`=45 here, a plausible real percentage, not `0xff`) — recorded as a new, distinct data point
for the "two distinct Case wire encodings" open item, not force-fit into the existing sentinel
reading: the short form may simply be usable for any Case value under some condition not yet
identified, not exclusively for the unknown/255 placeholder.

- **Promoted to:** `PROTOCOL.md` §4.3 Option C (GMS/app-independence note, `CAP-004`+`CAP-033`);
  `PROTOCOL.md` §6's `CAP-016`/`CAP-036` Settable-toggles item (cross-session strengthening note);
  `PROTOCOL.md` §4.3 Option E's Case-encoding addendum (new non-sentinel sample, `CAP-007`).

### 2026-09-04 (round 2) — Bonus battery/firmware cross-check, six more captures: a major "Get ANC state" replication, and the Settable-toggles hypothesis sharpened

- **Trigger:** continuing the same maintainer-requested bonus-analysis line as the entry above.
  Six more captures selected for offering a still-untested angle: `CAP-035` (Option C on
  GrapheneOS specifically — the prior GrapheneOS session, `CAP-035` itself, never checked HFP),
  `CAP-027` (active A2DP/AVRCP media streaming — every prior sample was an idle session),
  `CAP-008` (a phone call — Device Info/cross-channel-sync not yet checked there),
  `CAP-019`–`CAP-025` (seven settings-toggle sessions, each with its own independent RFCOMM
  connection — never checked for `PROTOCOL.md` §4.1's "Get ANC state" opcode), `CAP-006` (a clean
  ANC repeat with 3 separate DLCI 0x04 channel (re)opens in one log), `CAP-010` (a fresh-pairing
  repeat). All 12 logs pre-verified untruncated.

#### Result 1 — `CAP-035`: Option C (HFP) confirmed on GrapheneOS specifically

```
$ tshark -r CAP-035-btsnoop_hci.log -Y "btrfcomm.len>0" -T fields -e frame.number -e frame.time \
    -e btrfcomm.dlci -e _ws.col.Info | grep -iE "BIEV=2"
1230  06:52:37.670  0x0c  Rcvd AT+BIEV=2,100   (fresh connect)
1309  06:53:35.936  0x0c  Rcvd AT+BIEV=2,100
1744  06:56:00.419  0x0c  Rcvd AT+BIEV=2,100   (reconnect)
```
`AT+BIEV=2,100` fires normally on **both** connection events in this session — GMS present but
`dumpsys`-verified disabled, no official app, no nRF Connect, on Pixel 9a/GrapheneOS
(`CAP-035-FINDINGS.md`'s own precondition). DLCI 0x08's battery triple (`100/100/100`) and Model
ID (`da2db1`) also reproduce unchanged. **Result: Option C is confirmed working on GrapheneOS
itself** — this project's actual target platform — not just on stock Android
(`CAP-004`/`CAP-033`, prior entry). Extends the GMS/app-independence result to a second OS.

#### Result 2 — `CAP-027`: the "near-lockstep" cross-channel sync does NOT always hold during active audio use

```
$ tshark -r CAP-027-btsnoop_hci.log -Y "btrfcomm.len>0" -T fields -e frame.number -e frame.time \
    -e btrfcomm.dlci -e _ws.col.Info | grep -iE "BIEV=2"
618   15:45:16.947  Rcvd AT+BIEV=2,100
1760  15:45:42.816  Rcvd AT+BIEV=2,100   (Δ25.9s)
3323  15:50:08.188  Rcvd AT+BIEV=2,100   (Δ4m25.4s — largest gap seen outside CAP-009's own idle session)
3438  15:51:28.341  Rcvd AT+BIEV=2,100   (Δ80.2s)
3457  15:51:38.499  Rcvd AT+BIEV=2,100   (Δ10.2s)

$ tshark -r CAP-027-btsnoop_hci.log -Y "btrfcomm.dlci==8 and btrfcomm.len>0" -T fields \
    -e frame.number -e frame.time -e data.data | grep "^0e01"
839   15:45:17.554  0e0100230a210a03616c6c121a0a060864100118010a060864100118020a040822180318012001
1750  15:45:42.711  (same content)
2935  15:48:24.913  (same content, no AT+BIEV counterpart within ±30s — checked directly, zero DLCI 0x0c frames 15:48:20–55)
3060  15:48:46.669  (variant, longer form)
3096  15:48:52.377  (same content, no AT+BIEV counterpart)
3319  15:50:08.179  0e0100230a210a03616c6c121a0a060864100118010a060864100118020a040822180318012001
3430  15:51:28.320  (same content)
3450  15:51:38.401  (same content)
```
DLCI 0x08's battery-triple push fires **3 extra times** (15:48:24/46/52) with **no accompanying
HFP `AT+BIEV` push nearby** — this session has AVRCP/A2DP traffic actively flowing throughout
(the touch-gesture test's own procedure, `TOUCH-002`–`TOUCH-006` riding AVRCP per
`CAP-027-FINDINGS.md`). 🟡 **HYPOTHESIS, new, single session:** the previously-documented
"near-lockstep" synchronization between Option C and Option E (`CAP-009`, extended to DLCI 0x02 in
`CAP-036-FINDINGS.md` §12.5) is **not universal** — it may specifically hold during idle/settled
sessions and break down (or simply run at a different, independent cadence) during active audio
streaming. Not confirmed as caused by the streaming itself — correlation only, one session.

#### Result 3 — `CAP-008`: Device Info + a cross-mechanism confirmation of the already-known Left 98%→97% transition

```
$ tshark -r CAP-008-btsnoop_hci.log -Y "btrfcomm.dlci==8 and btrfcomm.len>0" -T fields \
    -e frame.number -e frame.time -e data.data | grep "^0e01"
1111  09:38:51.281  0e0100230a210a03616c6c121a0a060862100118010a060864100118020a04082b180310012001
3451  09:43:13.003  0e0100230a210a03616c6c121a0a060861100118010a060864100118020a04082b180310012001
```
Decodes to Left `0x62`=98→`0x61`=97, Right constant `0x64`=100, Case constant `0x2b`=43 —
**independently reproduces**, via a completely different mechanism (DLCI 0x08 Option E), the same
98%→97% Left-earbud transition `CAP-008-FINDINGS.md` already established via HFP `AT+BIEV`. Both
mechanisms agree, cross-confirming each other within one session. HFP battery pushes continue at a
normal, undisrupted cadence through both SCO/eSCO call windows (09:39:19–?, 09:40:23–?) — the call
itself does not interrupt Option C.

#### Result 4 — `CAP-019`–`CAP-025`: **major replication of `PROTOCOL.md` §4.1's "Get ANC state" query — 12 occurrences, zero misses**

```
$ tshark -r <CAP-0NN>-btsnoop_hci.log -Y "btrfcomm.dlci==4 and btrfcomm.len>0" -T fields \
    -e frame.number -e frame.time -e frame.p2p_dir -e data.data | grep "^08110000\|^0813"
```
Every one of these 7 independently-logged sessions has its **own fresh HCI `Connection Complete`**
(`bthci_evt.code==0x03`, one per file) — and **every one shows `08 11 00 00` (Get) fired
immediately after DLCI 0x04 opens, answered ~10–60ms later by `08 13`**:

| Capture | `Get` occurrences | Settable-toggles byte(s) |
|---|---|---|
| `CAP-019` | 1 (07:35:58.54) | `0xe8` |
| `CAP-020` | 1 (07:46:19.93) | `0xe8` |
| `CAP-021` | 1 (07:59:41.90) | `0xe8` |
| `CAP-022` | 1 (08:15:27.98) | `0xe8` |
| `CAP-023` | 1 (08:23:45.85) | `0xe8` |
| `CAP-024` | 1 (08:31:36.58) | `0xe8` |
| `CAP-025` | 5 (08:40:58.04, 08:41:21.74, 08:43:42.55, 08:44:25.23, 08:45:04.58) | `0xe8` (all 5) |

`CAP-025`'s 5 occurrences are the standout: this single session shows the classic ACL connection
**never disconnects** (one `Connection Complete`, no `Disconnection Complete` for that handle) yet
DLCI 0x04 itself bounces (`DISC`→`SABM`→`UA`) several times — **the query fires again every time
the channel reopens with real payload following, even without a fresh ACL connection.** Several
DLCI 0x04 bounces in this same log (e.g. frames 1484, 1570, 1683) are bare `SABM`→`UA`→`DISC`
cycles carrying **zero payload** at all — these do **not** trigger a new `Get` — narrowing the
trigger precisely: **it fires on every DLCI 0x04 (re)establishment that proceeds to carry real
Message Stream traffic, not on a bare channel-level bounce with no payload, and not necessarily
tied to the underlying classic ACL link itself reconnecting.**

`CAP-006` (a clean, single-tap-per-window ANC repeat, previously never checked for this opcode)
adds **3 more occurrences in one log**, at 17:23:54.37 (`Settable=0xe8`, right after the session's
one and only classic `Connection Complete`), 17:25:02.03 (`Settable=0xe8`, after a DLCI-0x04-only
bounce mid-session, no ACL reconnect), and 17:26:55.06 (`Settable=0x00`, near the very end of the
session, again after a DLCI-0x04-only bounce). `CAP-010` (a fresh system-Settings forget-and-repair
repeat) adds **2 more**, both `Settable=0x00` (11:43:46.98, 11:47:04.15).

**Combined tally across this document's two entries + `CAP-036-FINDINGS.md` §3: 17 occurrences
across 10 independent capture files (`CAP-006` ×3, `CAP-010` ×2, `CAP-016` ×1 [prior entry],
`CAP-019`–`CAP-024` ×1 each, `CAP-025` ×5, `CAP-036` ×1), zero misses against the "opens with real
payload" criterion.** This is a materially stronger replication base than several of this
project's own existing FACT promotions required (e.g. `ADR-009`'s ANC-Set opcode: 4 samples in one
capture; `ADR-014`'s Option E: 4 independent sessions before FACT).

**Settable-toggles byte, sharpened not contradicted:** of the 17 occurrences, **12 show
`Settable=0xe8`** (`CAP-006`'s first, all 7 of `CAP-019`–`CAP-025`) and **5 show `Settable=0x00`**
(`CAP-016`, `CAP-036`, `CAP-006`'s last, both of `CAP-010`'s). Every `0x00` sample sits at a moment
plausibly close to the Buds being in or near the case (`CAP-016`: "both buds still docked";
`CAP-036`: buds sitting in the open case the whole session, never worn; `CAP-006`'s last: end of
an ANC-tap test session; `CAP-010`: a fresh-pairing repeat, buds freshly taken from the case to
re-pair). Every `0xe8` sample sits in a session where the buds are actively in use throughout
(`CAP-019`–`CAP-025`'s settings-toggle tests; `CAP-006`'s first, mid-ANC-test). **Revises
`CAP-036-FINDINGS.md` §3/§11 and the previous entry's own over-generalization** ("this is a
connect-time pattern") — it is not connect-time as such; 12 of 17 connect/reopen-time samples show
`0xe8`. The sharper reading: `Settable=0x00` correlates with the Buds being physically in/near the
case, independent of whether DLCI 0x04 just (re)opened.

- **Promoted to:** `PROTOCOL.md` §4.1 (`Get ANC state` trigger-reliability — **promoted to 🟢 FACT,
  maintainer sign-off obtained 2026-09-04, `DECISIONS.md` ADR-022**); `PROTOCOL.md` §6's
  Settable-toggles item (corrected reading, in/near-case not connect-time);
  `PROTOCOL.md` §4.3 (Option A GrapheneOS confirmation, Option E cross-mechanism note, cross-sync
  caveat).

### 2026-09-05 — Video verification of the Settable-toggles "in-case vs. worn" hypothesis

- **Trigger:** the maintainer asked to verify, before considering promotion, whether the
  in-case/worn correlation from the round-2 entry above actually holds on screen — the "worn"
  status for `CAP-019`–`CAP-025` had only been inferred from the session's procedure, not directly
  checked frame-by-frame.

- **Method:** `ffmpeg -ss <t> -frames:v 1` extraction (this project's standard method) at the
  video-relative offset matching each target wire timestamp, computed from each video's own
  wall-clock overlay at `t=0`. One `0x00` sample (`CAP-010`) and one `0xe8` sample each from
  `CAP-021` and `CAP-025` were checked (all three videos playable); `CAP-006`'s
  `CAP-006-recording.mp4` **could not be checked — the file fails to open in `ffmpeg`**
  (`stream 1, contradictionary STSC and STCO` / `error reading header`; `ffprobe` fails
  identically; no repair tool available in this environment). This is a genuine gap, not a
  negative result — flagged for the maintainer, not silently skipped.

- **Result — 3/3 checked samples confirm the hypothesis, no counter-examples:**

  | Capture | Settable byte | Wire time | Video time (offset from t=0) | On-screen state |
  |---|---|---|---|---|
  | `CAP-010` | `0x00` | 11:43:46.98 | 11:43:48 (+76s) | Both buds **seated in the case's charging slots**, LED lit — mid Fast-Pair "Save device to account" dialog |
  | `CAP-021` | `0xe8` | 07:59:41.90 | 07:59:42 (+6s) | Case open, **both slots empty** — cropped/zoomed to confirm, buds off-frame (presumably worn) |
  | `CAP-025` | `0xe8` | 08:40:58.04 | 08:40:58 (+6s) | Case open, **both slots empty**, both buds visible loose on the table beside the case — not docked |

  Combined with the two samples this hypothesis was originally built on (`CAP-016`'s own frame
  1521, "both buds still docked at this instant"; `CAP-036`, buds sitting in the open case the
  entire session, never worn) — **5 of 5 video-checked samples now confirm the pattern**: `Settable
  =0x00` when the Buds are physically in the case, `Settable=0xe8` when they are not, with zero
  counter-examples found. Note `CAP-025`'s specific state (buds loose beside the case, not
  necessarily worn) refines "worn" to more precisely "not docked" — the distinguishing factor
  is the case, not literally ear-insertion.

- **Promoted to:** `PROTOCOL.md` §4.1 — promoted to 🟢 FACT, maintainer sign-off obtained
  2026-09-05, `DECISIONS.md` ADR-024.

**Addendum, same day — `CAP-006`'s corrupted video replaced, 2 more samples confirmed:** the
maintainer re-pulled `CAP-006-recording.mp4` from the phone; the replacement (79.49s, 1280×720)
opens cleanly in `ffmpeg` (`captures/CAP-006-2026-08-15_17-23-49_17-25-06-Group_B/CAP-006-EVENT-NOTES.md`'s
video recovery note updated accordingly). This covers `CAP-006`'s first two `Settable` samples:

```
$ ffmpeg -ss 5.4 -i CAP-006-recording.mp4 -frames:v 1 t5.png   # wire time 17:23:54.37, Settable=0xe8
$ ffmpeg -ss 73  -i CAP-006-recording.mp4 -frames:v 1 t73.png  # wire time 17:25:02.03, Settable=0xe8
```
Both frames show the case open with **both slots empty** (the case is already empty at video
start, `t=0`/17:23:49, Bluetooth still off) — consistent with the pattern. **Now 7 of 7
video-checked samples confirm it, zero counter-examples.** The replacement file, like the
original, ends at ~17:25:08 — `CAP-006`'s own *third* sample (`Settable=0x00`, wire time
17:26:55.06, ~1m47s past the video's end) remains unverified, so the specific within-session
`0xe8`→`0x00` transition is still open, independent of the earlier file-corruption issue.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/DESKRESEARCH_FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/DESKRESEARCH_FINDINGS
