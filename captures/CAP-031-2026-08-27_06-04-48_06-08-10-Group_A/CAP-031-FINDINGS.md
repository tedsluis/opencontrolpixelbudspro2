# Findings: `CAP-031` (Group A repeat — third attempt at the pre-clearing-action baseline)

Standardized, evidence-based extraction from `CAP-031-btsnooz_hci.log` + `CAP-031-recording.mp4`,
staged here per `PROJECT_RULES.md` §2. Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-031` · **Date:** 2026-08-27 · **Firmware:** `release_5.203` (screen-confirmed,
`CAP-031-recording.mp4` 06:05:15 and 06:07:58) · **Phone:** Pixel 7a, Android 17 (⚪ assumed, build
not screen-confirmed this session) · **Log file:** `CAP-031-btsnooz_hci.log` (227.98s, 1,747
packets, 2026-08-27 06:06:37.159791–06:10:25.141561 +0200, **`btsnooz`-format file — inferred
capture length 15–126 bytes/frame, same truncation issue as `CAP-012`/`CAP-013`**) · **Video:**
`CAP-031-recording.mp4` (202.4s, 06:04:48–06:08:10, burned-in wall-clock overlay) · **Devices:**
phone `E8:D5:2B:xx:CA:81` (Pixel 7a, partially redacted per `AGENTS.md` §9), peer `Google_cf:6e:07`
(`04:00:6e:cf:6e:07` — same address as `CAP-001`–`CAP-013`, independently re-confirmed on the wire
this session, both as the classic BD_ADDR and, notably, as the **public** address used for this
session's LE advertising — see §6).

---

## 0. Scope: what this capture can and cannot answer (Step 0 — timing-fix verification)

This capture was explicitly planned (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Group-A-repeat note,
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `PAIR-004` row) as a third attempt to resolve
`CAP-001-FINDINGS.md` §6's still-open primary question — did a BLE link and/or a still-valid
classic link key already exist for this peer *before* the on-screen clearing action? — this time
with a live file-size-polling check during recording, specifically to avoid `CAP-013`'s "log
started too late" failure. **That live check did not succeed: the log again starts after the
clearing action, not before it.** Verified independently against both the video and the wire log,
not assumed from the maintainer's setup intent:

- **The clearing action performed was a genuine, narrow, per-device "Forget"** — unlike `CAP-013`'s
  broader "Reset Bluetooth & Wi-Fi". Confirmed on-screen: the "Device details" page for "Pixel Buds
  Pro 2 van Ted" (`CAP-031-recording.mp4` t=35s/06:05:23) shows the standard per-device trash-can
  "Forget" button; tapping it (t=37s/06:05:27) raises a "Forget device? / Your phone will no longer
  be paired with Pixel Buds Pro 2 van Ted" confirmation dialog with "Cancel"/"Forget device" buttons
  (t=39s/06:05:29); the user taps "Forget device" at **06:05:31** (t=43s, finger visible on the
  button); by t=45s/06:05:33 the "Connected devices" screen shows an empty "Saved devices" list.
  This is exactly `PAIR-004`'s intended scenario (narrow per-device Forget), not `CAP-013`'s
  confound.
- **`CAP-031-btsnooz_hci.log`'s first frame is 06:06:37.159791** (`capinfos`/`tshark`, both
  system and log timezone confirmed `+0200` CEST, matching the video overlay's own convention) —
  **66 seconds after** the 06:05:31 Forget tap. The intervening, entirely unlogged window also
  contains: the case being opened (06:06:01), the case pair button being pressed (06:06:02), the
  user checking "Saved devices" (06:06:14, confirmed empty), the first "Pair new device" tap
  (06:06:20), and the phone's own Bluetooth address being displayed (06:06:21). A screenshot at
  the log's own first-frame second (t=109s/**06:06:37**) shows the phone already sitting on the
  "Pair new device" → "Available devices" screen (list showing `[TV] Samsung 5 Series`,
  `vuart:ktunnel`, `DoorLocker` — no Buds yet), i.e. the log picks up **mid-way through the first
  scan attempt**, not at or before the Forget tap.

**Primary question (`CAP-001-FINDINGS.md` §6 / `PROTOCOL.md` §6's "Re-raised 2026-08-26" entry /
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `PAIR-004`) — 🔴 remains OPEN QUESTION, NOT answered by this
capture either.** Whether a BLE link or valid link key existed *before* the Forget tap cannot be
determined here: the Forget tap itself, and the 66s of case-open/pair-button/first-scan-attempt
activity between it and this log's first frame, fall entirely in this capture's un-logged gap. Per
this project's guardrail on negative conclusions requiring a covered time window, no conclusion
(positive or negative) is drawn about that window from this capture.

**Secondary question (`PAIR-004`'s "does the subsequent re-pairing show a fresh SSP/link-key
handshake, or reuse of an old key?") — 🟢 answerable, and answered again, a sixth confirming
instance.** The classic bonding sequence runs cleanly within this capture's covered window — see
§2 below.

This is the **second** consecutive attempt (after `CAP-013`) where the live/procedural fix intended
to capture the pre-clearing-action window did not work — see §8 for a proposed root-cause note and
a fourth-attempt method.

## 1. Log-truncation limitation (🟢 FACT) — reproduces a third time

`capinfos CAP-031-btsnooz_hci.log` reports an inferred packet size limit of 15–126 bytes (`btsnooz`
format). Confirmed directly against individual frames: e.g. frame 961 (DLCI `0x08` traffic) shows
`frame.len`=77 but `frame.cap_len`=15
(`tshark -r CAP-031-btsnooz_hci.log -Y "frame.number==961" -T fields -e frame.cap_len -e frame.len`).
The maximum `frame.cap_len` seen anywhere in this log is 126 bytes, and every RFCOMM data frame
sampled below is capped at 15. This is the same limitation `CAP-012-FINDINGS.md` §1 and
`CAP-013-FINDINGS.md` §1 documented (this folder's file is also named
`CAP-031-btsnooz_hci.log`, not `-btsnoop_hci.log`, despite the standard `btsnoop` magic bytes in
its header — `file(1)` reports "BTSnoop version 1, HCI UART (H4)" but `capinfos`'s "inferred"
length range shows the same real-world per-packet truncation as the two prior sessions).
**Consequence:** HCI-event-level sequencing (pairing state machine, RFCOMM channel-open/close via
SABM/UA, which DLCI opened when) is unaffected — those are short control frames within the 15-byte
cap — but any RFCOMM data payload longer than ~15 bytes (DLCI `0x08`'s Group/Code/Length/Value
envelope beyond the first ~2–3 bytes, DLCI `0x02`'s HDLC frame bodies, DLCI `0x04`'s longer Message
Stream payloads) is truncated and cannot be decoded past that point in this capture either.

## 2. Classic BR/EDR link establishment — fresh pairing path confirmed again (🟢 FACT)

The full sequence matches `PROTOCOL.md` §5.1's already-FACT **"Fresh pairing (no stored key)"**
path exactly, frame-for-frame in kind, within this capture's covered window (06:06:37 onward —
i.e. this describes the *second* pairing attempt this session, the one that succeeded; see §6 for
the LE discovery activity immediately preceding it):

| Step | Event | Frame | Time (+0200) |
|---|---|---|---|
| 1 | `Sent Delete Stored Link Key` (target `04:00:6e:cf:6e:07`) | 598 | 06:07:15.111235 |
| 2 | `Sent Create Connection` | 600 | 06:07:15.113151 |
| 3 | `Rcvd Connect Complete` (status `0x00`) | 615 | 06:07:15.420861 |
| 4 | `Rcvd Link Key Request` | 635 | 06:07:15.473339 |
| 5 | `Sent Link Key Request **Negative** Reply` | 637 | 06:07:15.473817 |
| 6 | `Rcvd IO Capability Request` → `Sent IO Capability Request Reply` | 639/640 | 06:07:15.476743/477230 |
| 7 | `Rcvd IO Capability Response` | 648 | 06:07:15.583876 |
| 8 | `Rcvd User Confirmation Request` → `Sent User Confirmation Request Reply` | 678/686 | 06:07:15.984302/16.170377 |
| 9 | `Rcvd Simple Pairing Complete` | 688 | 06:07:16.419983 |
| 10 | `Rcvd Link Key Notification` (new key stored) | 689 | 06:07:16.451414 |

**Command/hex evidence** (`PROJECT_RULES.md` §1 rule 4a):
`tshark -r CAP-031-btsnooz_hci.log -Y "frame.number==598" -x` →
`0000  01 12 0c 07 07 6e cf 6e 00 04 00` (HCI command `0x0c12` Delete Stored Link Key, `BD_ADDR`
field `07 6e cf 6e 00 04` little-endian-reversed = `04:00:6e:cf:6e:07`, matching the Buds' known
address).
`tshark -r CAP-031-btsnooz_hci.log -Y "frame.number==600" -x` →
`0000  01 05 04 0d 07 6e cf 6e 00 04 18 cc 01 00 88 f8 01` (HCI command `0x0405` Create Connection,
same `BD_ADDR`).
`tshark -r CAP-031-btsnooz_hci.log -Y "frame.number==637" -x` →
`0000  01 0c 04 06 07 6e cf 6e 00 04` (HCI command `0x040c` Link Key Request Negative Reply).
`tshark -r CAP-031-btsnooz_hci.log -Y "frame.number==689" -x` →
`0000  04 18 17 07 6e cf 6e 00 04 22 85 59 a4 33 b1 eb 58 18 79 f4 a1 3f bc 4a 0a 05` (Link Key
Notification event, 16-byte new key `22 85 59 a4 33 b1 eb 58 18 79 f4 a1 3f bc 4a 0a`, key type
`05` = "Authenticated Combination Key generated from P-256").

**Video correlation:** the user taps "Connect" on the "Pair with Pixel Buds Pro 2" pop-up at
**06:07:15** per `CAP-031-EVENT-NOTES.md`'s timeline — matching `Sent Delete Stored Link Key` at
06:07:15.111 to within the video's own 1-second sampling resolution.

**Conclusion, no `PAIR-004` ambiguity:** `Sent Link Key Request Negative Reply` at step 5 is the
discriminating event, exactly as in `CAP-002`/`CAP-003`/`CAP-013`. Every SSP sub-step (IO
Capability exchange, User Confirmation, Simple Pairing Complete, a **new** Link Key Notification)
is present. **This re-pairing used a completely fresh classic SSP handshake, not a reused link
key** — a sixth independent confirming instance of `PROTOCOL.md` §5.1's fresh-pairing path. As with
`CAP-013`, this speaks only to the bonding state active immediately before *this capture's own*
pairing attempt — it does not, and cannot, speak to what existed before the Forget tap itself (§0).

## 3. RFCOMM channel topology this session (🟢 FACT for topology; per-channel content status below)

Session-local RFCOMM server-channel numbers mapped to their DLCIs, opened via `SABM`/`UA` starting
~1.1s after the classic link's `Encryption Change`:

| Server channel | DLCI | Opened (frame, time +0200) | Content this session | Status |
|---|---|---|---|---|
| 0 | `0x00` | 812/821, 06:07:17.561/602 | RFCOMM multiplexer control (PN negotiation) | 🟢 FACT |
| 6 | `0x0c` | 831/839, 06:07:17.636/675 | HFP AT-command channel (per `PROTOCOL.md` §4.3 Option C's established pattern; not decoded in detail this session, out of this task's scope) | 🟢 FACT for channel identity/topology |
| 5 | `0x0a` | 836/850, 06:07:17.670/702 | No payload observed — consistent with `CAP-001-FINDINGS.md` §6's finding that this DLCI stays silent (SCO/eSCO ruled out there) | 🟢 FACT (silence reproduced again) |
| 4 | `0x08` | 925/940, 06:07:17.877/898 | Private `[Group][Code][Length][Value]` envelope (`PROTOCOL.md` §2.3) — Groups `0x03`,`0x04`,`0x05`,`0x09`,`0x0e`,`0x12`,`0x14`,`0x16` observed by first bytes; see §4 | 🟢 FACT for channel identity/topology; 🔴 most payload content truncated (§1) |
| 1 | `0x02` | **1122/1124, 06:07:19.202/213** | `libmaestro` candidate Pigweed `pw_hdlc` channel (per `PROTOCOL.md` §2.2a) | 🟢 FACT for channel identity; see §5 — opens in the **initial burst this time**, ~1.6s after channel 0, not delayed |
| 2 | `0x04` | 1343/1346, 06:07:24.342/415 | Official Fast Pair Message Stream — Device Information and Hearable Controls ANC Get (`0x08 0x11`) observed; see §4 | 🟢 FACT for channel identity |

**Command:** `tshark -r CAP-031-btsnooz_hci.log -Y "btrfcomm.frame_type == 0x2f or btrfcomm.frame_type == 0x63" -T fields -e frame.number -e frame.time -e btrfcomm.dlci -e btrfcomm.channel -e _ws.col.info`

## 4. DLCI 0x08 / DLCI 0x04 content — Group/Code visible, Value truncated (🟢 FACT for presence, 🔴 OPEN for full values)

**Command:** `tshark -r CAP-031-btsnooz_hci.log -Y "btrfcomm.dlci==0x08 and btrfcomm.len>0" -T fields -e frame.number -e frame.time -e frame.len -e frame.cap_len -e data.data`

Representative hex (first bytes only, per §1's truncation):

```
961  06:07:17.9257  len=77  data=05 0c
988  06:07:17.9621  len=80  data=03 02 00
990  06:07:17.9634  len=19  data=09 02 00
992  06:07:17.9647  len=43  data=0e 02 00
994  06:07:17.9724  len=52  data=0e 01 00
```

`Group 0x0e Code 0x01`/`0x02` traffic (per `ADR-014`, the confirmed per-earbud+case battery push,
`PROTOCOL.md` §4.3 Option E) is **present at connection time** (frames 992/994), consistent with
prior captures. **The Length and Value bytes are truncated in every one of these frames**
(`frame.cap_len`=15 throughout), so the specific `[value, flag, index]` triplets cannot be decoded
this session — presence is 🟢 FACT, specific values are 🔴 OPEN, exactly the same limitation as
`CAP-012`/`CAP-013`.

On DLCI `0x04`, frame 1359 (`ef 09 08 11 00`-shaped RFCOMM UIH payload `08 11 00`) is a
`Group 0x08 Code 0x11` frame — the confirmed ANC **Get** request/response per `PROTOCOL.md`
§4.1/`ADR-009`. Consistent with an automatic state query on connect, not evidence of a
user-initiated ANC change (no ANC action was performed on screen this session).

## 5. DLCI 0x02 timing — `CAP-013`'s "~61s delay" hypothesis tested and NOT reproduced (🟢 FACT, negative result)

`CAP-013-FINDINGS.md` §5 raised a single-sample 🟡 HYPOTHESIS: that DLCI `0x02` (the `libmaestro`
candidate channel) might open only once the companion app reaches a specific point in its
permission-granting setup flow, rather than unconditionally during the initial RFCOMM multiplexer
burst. This capture is a direct, higher-precision test of that hypothesis (the plan's Step 3
explicitly asked for it): **DLCI `0x02` opens at 06:07:19.202 (frames 1122/1124) — only ~1.64s
after DLCI `0x00`'s own `SABM` at 06:07:17.561, and *before* DLCI `0x04` (06:07:24.342)**. This is
squarely within the initial multiplexer-open burst (all 5 DLCIs opened within ~6.85s of each
other), matching `CAP-001`/`CAP-002`/`CAP-004`/`CAP-006`'s pattern, not `CAP-013`'s outlier. The
first app-permission "Allow" tap this session doesn't occur until 06:07:41 (per
`CAP-031-EVENT-NOTES.md`), over 20 seconds after DLCI `0x02` already opened — there is no timing
coincidence to explain here at all.

**Conclusion: 🟢 FACT — `CAP-013`'s ~61s-delay observation does not reproduce.** It was a
single-session anomaly, not a companion-app-permission-gated behavior; `CAP-013-FINDINGS.md` §5's
hypothesis is not promoted and should be read as ruled out by this second data point, pending
maintainer sign-off on the wording (§7 below).

## 6. Second/unattributed BLE link — `CAP-013`'s open question tested and NOT reproduced (🟢 FACT, negative result)

`CAP-013-FINDINGS.md` §6 found a second, independent `LE Enhanced Connection Complete` to an
unattributed random address (`43:8a:82:03:4b:f2`) shortly after the classic link was already up,
raising an open question (also referenced in `PROTOCOL.md` §6). This capture's full log was
checked for the same pattern:

- **Zero occurrences of `43:8a:82:03:4b:f2`** anywhere in this log
  (`tshark -r CAP-031-btsnooz_hci.log -Y "bthci_evt.bd_addr == 43:8a:82:03:4b:f2 or bthci_cmd.bd_addr == 43:8a:82:03:4b:f2"` → 0 matches).
- **Exactly one `LE Enhanced Connection Complete` event in the entire log** (frame 529,
  06:07:14.645649), and it resolves to the Buds' own address:
  `tshark -r CAP-031-btsnooz_hci.log -Y "frame.number==529" -V` shows `Peer Address Type: Public
  Device Address (0x00)`, `BD_ADDR: Google_cf:6e:07 (04:00:6e:cf:6e:07)` — the same public address
  as the classic link, not a random/resolvable one.
- **Bycatch, not previously documented this precisely:** the Buds advertise via LE using this same
  **public** BD_ADDR during the discovery/pairing window. `Rcvd LE Meta (LE Extended Advertising
  Report)` frames from `04:00:6e:cf:6e:07` start at 06:07:09.590795 (frame 423) — right after the
  case pair-button was pressed a second time at 06:07:07 — and continue until the LE connection at
  06:07:14.645649 (frame 529), ~0.47s before the classic `Sent Delete Stored Link Key` at
  06:07:15.111. Raw hex for frame 423: `04 3e 2f 0d 01 01 00 00 07 6e cf 6e 00 04 01 02 03 7f e0 00
  00 00 00 00 00 00 00 00 15 02 0a f6 02 01 02 0a 16 2c fe 00 37 da 2d b1 18 02 03 16 53 18`
  (`tshark -r CAP-031-btsnooz_hci.log -Y "frame.number==423" -x`); byte 8–13 (`07 6e cf 6e 00 04`,
  little-endian-reversed) = `04:00:6e:cf:6e:07`, `Peer Address Type: Public Device Address (0x00)`.

**Conclusion: 🟢 FACT — `CAP-013`'s unattributed-second-BLE-link pattern does not reproduce.** This
session shows a single, cleanly-attributed LE link on the Buds' own public address, not a second
mystery random address. This is one clean negative data point against a *recurring* pattern, but
does not itself resolve `CAP-013`'s or `CAP-016`'s original open question about their own
still-unattributed addresses (`43:8a:82:03:4b:f2`, `4f:25:00:85:9a:b1`) — those remain 🔴 OPEN, this
capture simply didn't reproduce the phenomenon a third time.

## 7. Hypothesis test record (`PROJECT_RULES.md` §4, rule 10/11 template)

**Test A — primary `PAIR-004` question (pre-clearing-action state)**

- **Hypothesis:** starting HCI snoop logging with a live, in-recording file-size-polling check
  (performed specifically to avoid `CAP-013`'s failure) would ensure the log's first frame precedes
  the on-screen Forget tap, allowing the pre-existing BLE-link/link-key question
  (`CAP-001-FINDINGS.md` §6) to finally be tested.
- **Setup:** Pixel 7a, Android 17, firmware `release_5.203`, official Pixel Buds companion app
  (version not screen-confirmed). Live snoop-log file-size polling was performed during recording,
  before the Forget tap, per this task's own preparation step. Capture method: `btsnooz`-format
  file, same truncation profile as `CAP-012`/`CAP-013` (§1).
- **Expected outcome:** log's first frame timestamp ≤ the video's own Forget-tap timestamp
  (06:05:31).
- **Actual outcome:** log's first frame is 06:06:37.159791 — 66 seconds *after* the Forget tap, and
  after the case-open/pair-button/first-scan-attempt sequence too (§0).
- **Conclusion:** 🔴 **Failed / inconclusive for the primary question**, recorded per
  `PROJECT_RULES.md` rule 12 as evidence in its own right. The primary question
  (`CAP-001-FINDINGS.md` §6) remains 🔴 OPEN, untested a third time. See §8 for what may have gone
  wrong and a proposed fourth-attempt method.

**Test B — secondary `PAIR-004` question (fresh SSP vs. reused key after Forget)**

- **Hypothesis:** the classic-link re-pairing following this session's Forget tap shows a fresh
  SSP/link-key handshake, matching `PROTOCOL.md` §5.1's "no stored key" path.
- **Setup:** same as Test A.
- **Expected outcome:** `Delete Stored Link Key` → `Create Connection` → `Connect Complete` →
  `Link Key Request` → **Negative** Reply → IO Capability exchange → Simple Pairing Complete →
  **new** Link Key Notification.
- **Actual outcome:** exact match — see §2's table (frames 598–689, 06:07:15.111–16.451).
- **Conclusion:** 🟢 **CONFIRMED**, a sixth independent confirming instance of `PROTOCOL.md`
  §5.1's fresh-pairing path.

**Test C — `CAP-013` §5's DLCI 0x02 timing hypothesis**

- **Hypothesis:** DLCI `0x02` opens later than the other RFCOMM channels, plausibly gated on an
  app-permission step.
- **Setup:** same as Test A; DLCI open times compared across all 5 channels.
- **Expected outcome (if hypothesis held):** DLCI `0x02`'s `SABM` tens of seconds after DLCI
  `0x00`'s, coinciding with an on-screen "Allow" tap.
- **Actual outcome:** DLCI `0x02` opens 1.64s after DLCI `0x00`, within the initial multiplexer
  burst, ~20s before the first "Allow" tap (§5).
- **Conclusion:** 🔴→ruled out. `CAP-013`'s observation does not reproduce; not promoted.

**Test D — `CAP-013` §6's second-BLE-link pattern**

- **Hypothesis:** a second, unattributed BLE link (random/resolvable address) recurs around
  connection time, as seen in `CAP-013` and (a different address) `CAP-016`.
- **Setup:** same as Test A; full-log scan for any `LE Enhanced Connection Complete` beyond the
  main Buds link.
- **Expected outcome (if hypothesis held):** a second `LE Enhanced Connection Complete` to an
  address other than `04:00:6e:cf:6e:07`.
- **Actual outcome:** exactly one LE connection this session, to the Buds' own public address
  (§6).
- **Conclusion:** does not reproduce this session — one clean negative data point, original
  `CAP-013`/`CAP-016` open questions about their own addresses remain 🔴 OPEN independently.

## 8. Other open questions raised by this capture / proposed next steps

- **Why did the live file-size-polling check not catch the Forget action this time either?**
  Two non-exclusive candidate explanations, neither confirmed: (a) the polling check itself only
  confirmed the log *file* was growing, not that its *content* had reached the current wall-clock
  moment — if the underlying `btsnoop`/`btsnooz` capture pipe buffers or lags, a growing file size
  does not guarantee the newest frames are current; (b) the polling check may have been performed
  correctly but *before* logging was actually (re-)started, or logging may have briefly stopped and
  restarted around the Bluetooth-enable step at 06:04:55 (not verified against Android's own HCI
  snoop log rotation/restart behavior in this pass). **A fourth attempt should verify snoop-log
  *content* freshness directly** (e.g. tail the log file's own last-frame timestamp against a
  live wall clock immediately before performing the Forget tap) rather than relying on file size
  alone.
- `43:8a:82:03:4b:f2` (`CAP-013`) and `4f:25:00:85:9a:b1` (`CAP-016`) remain unattributed — this
  capture's clean single-LE-link result doesn't resolve either, only shows the phenomenon isn't
  universal.
- DLCI `0x0c` (HFP) content was not decoded in detail this session — out of this task's scope, but
  available in the log for a future battery-tracking pass if needed.

> **Update (2026-08-27), PROPOSAL — pending maintainer approval:** the fourth attempt proposed
> above, `CAP-032`, succeeded — extracted via the raw BTSnoop file path (§1's proposed root-cause
> guess, "extraction path vs. session-specific," is now supported: `CAP-032`'s raw-path log is
> genuinely untruncated). Its log's first frame lands ~58s *before* the on-screen Forget tap,
> finally covering the pre-clearing-action window. For that session: no BLE link and no valid
> classic link key existed for the Buds before the Forget tap — the opposite of `CAP-001`'s original
> finding, read as a clean counter-example rather than a resolution of `CAP-001`'s own puzzle. See
> `CAP-032-FINDINGS.md` §0 for the full account and `CAP-001-FINDINGS.md` §6/`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s
> `PAIR-004` row for the updated cross-reference chain. This capture's own root-cause guess in §8
> above (live-recording file-size-polling not guaranteeing content freshness) is superseded by the
> simpler, better-supported explanation that this capture used the `btsnooz.py` fallback extraction
> path at all, independent of the polling check's own correctness — `CAP-032-FINDINGS.md` §0.1/§7
> Test C.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-031-2026-08-27_06-04-48_06-08-10-Group_A/CAP-031-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-031-2026-08-27_06-04-48_06-08-10-Group_A/CAP-031-FINDINGS
