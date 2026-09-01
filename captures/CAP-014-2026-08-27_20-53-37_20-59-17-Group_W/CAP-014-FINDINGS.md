# Findings: `CAP-014` (Group W repeat — GATT handle↔UUID mapping follow-up, snaplen fix)

Standardized, evidence-based extraction from `CAP-014-btsnoop_hci.log` + `CAP-014-recording.mp4` +
`CAP-014-nrf-connection.log`, staged here for later promotion directly into `PROTOCOL.md` per
`PROJECT_RULES.md` §2. Modeled on `CAP-001-FINDINGS.md` / `CAP-017-FINDINGS.md`. Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-014` · **Date:** 2026-08-27 · **Firmware:** `release_5.203` (L/R/Case,
confirmed on-screen 20:59:07 in the official Pixel Buds app's "Firmware update" page) · **Phone:**
Pixel 7a — identified via `Read BD ADDR` (frame 58) = `e8:d5:2b:7e:ca:81`, the exact same physical
phone used in `CAP-001`–`CAP-012` (Android version ⚪ ASSUMED 17, not re-confirmed on-screen this
session). **GATT client app:** nRF Connect for Mobile (Nordic Semiconductor), same tool as
`CAP-017`. **Log file:** `CAP-014-btsnoop_hci.log` (947.6s, 4,663 packets, 2026-08-27
20:45:57.27–21:01:44.90 local/+0200). **Video:** `CAP-014-recording.mp4` (340.2s, 20:53:37–20:59:17
local, on-screen wall-clock overlay). **Devices:** phone `e8:d5:2b:7e:ca:81` (Pixel 7a), peer
`04:00:6E:CF:6E:07` ("Pixel Buds Pro 2 van Ted") — the same physical Buds/case used throughout this
project.

---

## 0. Snaplen fix confirmed (🟢 FACT) — the stated reason for this recapture is resolved

```
$ capinfos CAP-014-btsnoop_hci.log
...
Packet size limit:   file hdr: (not set)
Number of packets:   4,663
...
Interface #0 info: Capture length = 262144

$ tshark -r CAP-014-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'
mismatches: 0
```

`CAP-017-FINDINGS.md` §2/§4b/§4c established that its ~15-byte-per-frame ACL snaplen made
discovery-response UUID bytes unrecoverable from the wire and was the specific, named reason this
recapture (`CAP-014`) was requested. **Confirmed here: `CAP-014-btsnoop_hci.log` is not truncated**
— no snaplen limit is set on the capture interface, and `frame.cap_len == frame.len` holds for all
4,663 frames. Whatever raw discovery-response bytes exist on the wire in this session are fully
recoverable. (Whether any such bytes actually exist for the target handle cluster is a separate
question — see §4.)

## 1. Procedure check: this session does not execute either of Group W's own candidate methods (🟢 FACT)

Checked directly, the same way `CAP-010-FINDINGS.md` §1 checked this for its own session:

- **Neither Option (a) nor Option (b) of `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Group W was used.**
  No `adb shell pm clear com.android.bluetooth` is visible or implied anywhere in the video or the
  nRF Connect debug log, and the phone used is the **same Pixel 7a** as every prior capture
  (`e8:d5:2b:7e:ca:81`, §above) — not the project's Pixel 9a (Option b requires a phone that has
  *never* connected to this Buds unit before; this one has connected dozens of times).
- **No "Forget" tap is visible in the recorded window.** The video starts at 20:53:37 already on
  nRF Connect's scanner screen; the Buds appear in the BLE scan list as `NOT BONDED` (frame at
  20:54:39, `f2_62.jpg`) before the session's own `device.createBond()` call. Whether the classic
  bond was removed *before* the video started (i.e., off-camera, prior to 20:53:37) is **not
  determinable from this capture's artifacts** — the video has no earlier footage, and the wire log
  (which starts earlier, 20:45:57) shows no `HCI Delete Stored Link Key`/bond-removal event for
  this peer before the one issued by `device.createBond()` itself at 20:55:11.353538 (frame 2364,
  see §2). **🔴 OPEN QUESTION, not glossed over:** whether the Buds were already unbonded from this
  phone before this session began — same open-ended gap `AGENTS.md` §0.1's guardrail flagged;
  unlike `CAP-013`/`CAP-031`'s equivalent question (Group A, a different test), this one is not
  resolvable after the fact because no pre-session artifact exists for `CAP-014`.
- **The connecting app is nRF Connect (a third-party GATT client), not the official Pixel Buds
  app or system Bluetooth Settings** — matching `CAP-017`'s tool choice, not Group W's originally
  specified `pm clear`/Pixel-9a methods. This reproduces the *same* deviation pattern
  `CAP-010-FINDINGS.md` §1 already documented for its own session (a different deviation, same
  underlying issue: the folder's Group-W label does not guarantee Group W's own defined procedure
  was actually followed) — worth naming explicitly so a future session doesn't assume "Group W
  folder" implies "Group W method."

**Consequence:** this session's negative result for the handle↔UUID mapping (§4) cannot be
attributed to `pm clear`/Pixel-9a "not working" — neither was tried. It reproduces via a *third*,
more specific mechanism, identified precisely in §3/§4 below (GATT cache reuse on an
already-bonded phone), not a failure of Group W's own candidate methods.

## 2. ADR-008 compliance check: Accessory Non-Owner Service was NOT read or written (🟢 FACT — corrects the event notes' stale Procedure step 4)

`CAP-014-EVENT-NOTES.md`'s own "Procedure" section (step 4, copied from the session's *planned*
template before capture) instructed: *"tap into 'Accessory Non-Owner Service' and 'Unknown
Service' (`109b862f-…`) specifically, to read their characteristics/handles."* Tapping into the
Accessory Non-Owner Service would conflict with `DECISIONS.md` **ADR-008** (`PROJECT.md`'s
non-goals: investigating the Accessory Non-Owner Service is out of scope).

**Checked exhaustively against `CAP-014-nrf-connection.log` (174 lines, full file):**

```
$ grep -in "8e0c0001\|1519\|non-owner" CAP-014-nrf-connection.log
83:Accessory Non-Owner Service (15190001-12f4-c226-88ed-2ac5579f2a85)
84:- Accessory Non-Owner Characteristic [I W WNR] (8e0c0001-1d68-fb92-bf61-48377421680e)
```

Both hits are the passive service-discovery inventory printed once at 20:54:44.493 (the
`gatt.discoverServices()` callback dump) — **neither line is a `Reading characteristic
8e0c0001-…`/`gatt.readCharacteristic(...)` call**, unlike the 15 other characteristics the app
*did* read (§4c below; every actual read in this log is logged as `V ... Reading characteristic
<uuid>` immediately followed by `D ... gatt.readCharacteristic(<uuid>)`). Cross-checked against the
wire log too: no ATT operation targets the Accessory Non-Owner Service's handle at any point in
this session (§4's full ATT transcript for chandle `0x0003` contains no request correlating to
this UUID). The video (`f2_220.jpg`/`f2_247.jpg`, 20:57:17/20:57:44) shows the finger scrolling
*past* "Accessory Non-Owner Service" in the list without pausing or tapping — its characteristic
row never renders expanded on screen, unlike "Unknown Service" a few rows below it (§3).

**Conclusion: ADR-008 was not violated.** The service's UUID/name appears only in the unavoidable,
already-accepted inventory context (`CAP-004-FINDINGS.md` §6's precedent — naming a service in a
full discovery dump is not "investigating" it). **Correction applied:** `CAP-014-EVENT-NOTES.md`'s
Procedure step 4 is corrected below (§ of that file) to record this sub-step as *deliberately not
executed, per ADR-008*, rather than left standing as an open action item a future session might
carry out literally.

## 3. On-screen characteristic-level detail: "Unknown Service" is visible in full, Accessory Non-Owner is not (🟢 FACT)

Unlike `CAP-017`'s nRF Connect build (`CAP-017-FINDINGS.md` §4b: "flat, unexpanded list... no
tap-to-expand ever happened"), **this session's nRF Connect build renders every characteristic's
`Properties:`/`Descriptors:` inline in the scrollable `CLIENT` tab, with no tap required** — video
frames `f2_180.jpg` (20:56:37) through `f2_247.jpg` (20:57:44) show this directly, e.g.:

- 20:56:37 — Broadcast Audio Scan Service/Audio Stream Control Service/etc. characteristics shown
  with full `Properties:`/`Descriptors:`/UUID detail, unprompted.
- 20:57:44 — **Battery Service (`0x180F`) → Battery Level (`0x2A19`), `Properties: NOTIFY, READ`**
  — visually confirms `PROTOCOL.md` §4.3 Option D's service+characteristic pairing a second time
  (independent of `CAP-017`), still showing no *value*, consistent with §4c below (never read this
  session either).
- 20:57:44 — **Unknown Service (`109b862f-…`) → all 3 characteristics** (`8584cbb5-…` READ/WRITE,
  `b4eb9919-…` READ, `e66dd173-…` NOTIFY/READ with CCCD `0x2902`) rendered in full — this is passive
  list rendering, not a triggered read (§4c confirms no `readCharacteristic` call for any of the
  three).
- 20:57:17 — **Accessory Non-Owner Service** appears as a bare service header only (`PRIMARY
  SERVICE`, UUID) — its one characteristic does **not** render underneath it in this frame, unlike
  every other service around it. Not investigated further per §2/ADR-008.

**Structure vs. content, kept explicit per this task's Step 4 instruction:** for "Unknown Service"
and "Battery Service," this session establishes **structure** (UUID, characteristic UUIDs,
properties, CCCD presence) to the same level `CAP-017-FINDINGS.md` §4c already had from its own
later text-log export — it does **not** establish **content** (an actual read value) for either,
because no read was ever issued (§4c). Do not read this section as "these characteristics were
read this session" — they were only ever *listed*.

## 4. GATT traffic on the Buds' own connection (chandle `0x0003`): why the handle↔UUID mapping is still unresolved — root cause identified (🟢 FACT)

**Connection identification:**

```
$ tshark -r CAP-014-btsnoop_hci.log -Y "bthci_evt.bd_addr==04:00:6e:cf:6e:07 and bthci_evt.le_meta_subevent==0x0a" \
  -T fields -e frame.number -e frame.time_relative -e bthci_evt.connection_handle
2232  527.131977  0x0003
```

Frame 2232 (`LE Enhanced Connection Complete`) at 527.13s = 20:54:44.40 — matches
`CAP-014-nrf-connection.log`'s `20:54:44.459 [Callback] Connection state changed... CONNECTED`.
This connection (chandle `0x0003`) is active through the end of the log (no disconnect event for
it appears anywhere in the file).

**4a. CLI hygiene / cross-device contamination check (🟢 FACT — required before trusting any
handle-name resolution below):** three *other* LE connections exist in this same capture, to a
**different** peer (`67:6d:32:c1:30:07`, resolvable/random address, not the Buds), on chandles
`0x0002`/`0x0006`/`0x0009`:

```
$ tshark -r CAP-014-btsnoop_hci.log -Y "bthci_evt.le_meta_subevent==0x0a or bthci_evt.le_meta_subevent==0x01" \
  -T fields -e frame.number -e frame.time_relative -e bthci_evt.bd_addr -e bthci_evt.connection_handle
500   493.359368  67:6d:32:c1:30:07  0x0002
2232  527.131977  04:00:6e:cf:6e:07  0x0003   <- the Buds
4006  888.041644  67:6d:32:c1:30:07  0x0006
4483  923.032669  67:6d:32:c1:30:07  0x0007
4531  937.326383  67:6d:32:c1:30:07  0x0008
4570  944.806799  67:6d:32:c1:30:07  0x0009
```

All three `Read By Group Type` (opcode `0x10`/`0x11`) discovery bursts in this entire file (frames
532–573, 4046–4086, 4616–4662 — full 15-service-style walks including a `Heart Rate` service) sit
on chandles `0x0002`/`0x0006`/`0x0009`, i.e. **belong to the other device, not the Buds** —
confirmed via `bthci_acl.chandle` on each frame. **Zero** `Read By Group Type` frames exist on
chandle `0x0003` anywhere in the file. This matters because Wireshark's ATT-attribute name cache in
this dissector is not connection-scoped (demonstrated concretely in §4b below) — a naive
`btatt.opcode==0x10` search without first isolating the Buds' chandle would have misattributed the
other device's service list to the Buds, exactly the failure mode `AGENTS.md` §13's CLI-hygiene
rule warns about (framed there for classic RFCOMM `btrfcomm.dlci`; the same discipline applies to
LE `bthci_acl.chandle`).

**4b. What actually happened on chandle `0x0003` (🟢 FACT):**

```
$ tshark -r CAP-014-btsnoop_hci.log -Y "bthci_acl.chandle==0x0003 and btatt" \
  -T fields -e frame.number -e frame.time_relative -e btatt.opcode -e _ws.col.Info
```

74 ATT frames total. In order:

1. **MTU exchange** (517 bytes) — frames 2252/2256.
2. **`Read By Type Request`, Database Hash UUID `0x2B2A`, handles `0x0001..0xffff`** (frame 2257) →
   Response (frame 2262): `8b447e7c056554c09c1c4ac90f6f771c` — **byte-for-byte identical** to
   `CAP-014-nrf-connection.log`'s own later `Reading characteristic 00002b2a...` at 20:56:03.316
   (`value: (0x) 8B-44-7E-7C-05-65-54-C0-9C-1C-4A-C9-0F-6F-77-1C`), confirming handle `0x0007` =
   Database Hash for this session (Read By Type's own request/response pair ties the UUID to the
   handle directly — not a guess).
3. A **partial, genuinely live discovery walk — but only of the GATT service itself (handles
   `0x0001`–`0x0009`)**: `Read By Type Request, Characteristic, Handles 0x0001..0x0009` (2465) →
   4-item response naming Service Changed/Client Supported Features/Database Hash/Server Supported
   Features (2467) → boundary probe `0x0009..0x0009` → `Attribute Not Found` (2485, confirms the
   service ends at 0x0009) → `Find Information Request, Handles 0x0003..0x0009` (2489) → Response
   (2531) resolving descriptor handles `0x0003`–`0x0009` individually.
4. **No `Read By Group Type` (service discovery) or `Read By Type`/`Find Information` walk ever
   targets any handle above `0x0009`.** Instead, the very next ATT operations on this connection
   (frames 2583 onward, all *after* pairing completes at 20:55:25, §5) are **direct `Read
   Request`/`Write Request` operations against handles already known by number** — `0x0f32`,
   `0x0f33`, `0x0f2a`, `0x0c04`, `0x0c05`, `0x0c0a`, `0x0c0c`, `0x0c0d`, `0x0c13`, `0x0c14` — with
   **no preceding declaration response for any of them anywhere in this file.**

**Root cause, stated as 🟡 HYPOTHESIS (same characterization already used for this exact pattern in
`CAP-002-FINDINGS.md` §4, now reproduced independently):** the phone's Android Bluetooth stack
recognized this already-bonded device (same phone/Buds pair as every prior capture, §1) via the
Database Hash check (step 2) and served its **cached** GATT database for the higher handle range
instead of re-walking it on the wire — it only re-verified the GATT service itself (steps 2–3),
which is the one service whose contents can change without a hash-invalidation elsewhere (`Service
Changed` lives there). This is not a truncation problem (§0 already ruled that out) and not
Group W's untried cache-busting methods failing (§1 — neither was tried) — it is a **third,
independently identified cause**: an already-cached client on an already-bonded phone simply never
asks the peer to re-declare handles it already has cached, so those declaration bytes are never
transmitted on the wire this session, and no amount of snaplen fixing can recover bytes that were
never sent.

**4c. Content actually read this session (🟢 FACT, hex + command per `PROJECT_RULES.md` §1 rule
4a):**

| Handle | Op | Value (hex) | Decoded | Cross-check |
|---|---|---|---|---|
| `0x0007` | Read Resp (2262) | `8b447e7c056554c09c1c4ac90f6f771c` | Database Hash | matches nRF log 20:56:03.316 |
| `0x0f32` | Read Resp (2615) | `64` | 1 byte, `100` decimal | **matches `CAP-017-FINDINGS.md` §6's `0x0f32 = 0x64`** exactly — see §6 below |
| `0x0f33` | Write Req (2616) | `0100` | CCCD-shaped enable (handle = `0x0f32`+1) | new this session vs. `CAP-002`/`CAP-003`/`CAP-010` |
| `0x0f2a` | Read Resp (2973) | `5265766973696f6e2036` | ASCII `"Revision 6"` | **byte-for-byte identical to `CAP-002-FINDINGS.md` §4** (frame 1441 there) — 3rd session now confirming this exact string on this exact handle |
| `0x0c04` | Write Req (2954) | `c916abcc85ee00fa1e6a9e5b448bc59686c8e978ff46d6a1a7616772aedf081f6effaf6d88bd9f99f90e2480f91c21941362b035170cd6804f3d7ef8377e2b44a140da5d8c447e71fa9aab6d3f5a0cae` | 80 bytes, opaque | reproduces the `0x0c04` write-then-notify shape from `CAP-002-FINDINGS.md` §4 |
| `0x0c04` | Notify (2961) | `7dac8090ac7052714274f2ab09627cbd` | 17 bytes, opaque | — |
| `0x0c0c` | Notify (2962) | `b76223856d8393eca0eb175f757eba157a3f9121fa0dd1684c19021d39fdcfb3e4999cbb7a11a427` | 41 bytes, opaque | reproduces `CAP-010-FINDINGS.md` §3's `0x0c0c` 40B-notify characterization (off-by-one is this frame's own byte count, not re-verified further) |
| `0x0c0a` | Write Req (2974) | `4acd5fa227209cc4a23a717f114e5cc1` | 17 bytes, opaque | — |
| `0x0c13` | Read Resp (2979) | `016fccf028840654ca` | 9 bytes, leading `0x01` | matches `CAP-010-FINDINGS.md` §3's `0x0c13` 9-byte-Read + leading-`0x01` characterization exactly |
| `0x0c13` | Write Req (2983) | `0108c7d7f39b2ed2689c` | 10 bytes, leading `0x01` | matches `CAP-010`'s 10-byte-Write characterization |
| `0x0c13` | Notify (2985) | `011e341ac0d5ca1d17360158d58a1dbaa85ecbebcbccdcf99d3a65e7c71f9400` | 32 bytes, leading `0x01` | matches `CAP-010`'s 32-byte-Notify characterization |
| `0x0c14` | Write Req (2980/2987) | `0100` | CCCD enable/disable, 2 bytes | matches `CAP-010`'s `0x0c14` characterization |

Extraction command pattern used for each row:
```
tshark -r CAP-014-btsnoop_hci.log -Y "frame.number == <N>" \
  -T fields -e frame.number -e btatt.handle -e btatt.opcode -e btatt.value
```

**Two handles not previously documented on the `0x0c04`/`0x0c05`/`0x0c0a` write pattern also
recur** (`0x0c05`, `0x0c0d`, both `0100` CCCD-enable writes) — same shape as `CAP-002`/`CAP-003`,
not shown as a separate row above for brevity.

**One byte-level anomaly, flagged not explained (🔴 OPEN QUESTION, zero-creativity — no
interpretation offered):** frame 3353, a `Read Response` on handle `0x0034` (never declared via
any discovery response on this or any other connection in this file), returns
`656c20427564732050726f20322076616e20546564` = ASCII `"el Buds Pro 2 van Ted"` — a 21-byte
substring of the device name `"Pixel Buds Pro 2 van Ted"` missing its first 3 characters (`"Pix"`),
returned via a plain `Read Request` (opcode `0x0a`), not a `Read Blob Request` (which would
normally explain an offset read). Wireshark's own dissector labels this handle's declaration
`abbafd00…`/`abbafd01…`/`0x2803`(!) with an `[Expert Info (Warning/Protocol): Bad Data]` flag —
this is the **same cross-connection name-cache pollution** identified in §4a (those UUIDs belong to
the *other* device's connection, not the Buds'), so that label is disregarded per the zero-creativity
rule. The raw bytes above are reported as-is; no theory for the missing 3 bytes or handle `0x0034`'s
real identity is offered.

## 5. Classic bonding mechanism this session: Cross-Transport Key Derivation, not classic SSP (🟢 FACT — 2nd confirming instance)

```
2364  554.085374  Sent Delete Stored Link Key
2365  554.089218  Sent Pairing Request: AuthReq: Bonding, MITM, SecureConnection | Initiator/Responder Key(s): LTK, IRK, CSRK, Linkkey
2379  554.155230  Sent Pairing Public Key
2381  554.323102  Rcvd Pairing Public Key
2414  567.269347  Sent Pairing DHKey Check
2417  567.315335  Sent LE Enable Encryption
2419  567.493724  Rcvd Encryption Change [v2]
2425  567.520513  Sent Create Connection            <- classic BR/EDR, initiated right after LE bonding
2484  567.971144  Sent Authentication Requested
2487  567.973811  Rcvd Link Key Request
2488  567.974846  Sent Link Key Request Reply         <- key already available, not negotiated fresh
2500  567.984062  Rcvd Authentication Complete
```

This is the SMP-over-LE pairing flow (Public Key/Confirm/Random/DHKey-Check, key distribution
including `Linkkey`) **followed immediately by a classic BR/EDR connection that authenticates via
`Link Key Request Reply` with no intervening classic SSP exchange** (no `IO Capability
Request`/`Simple Pairing Complete` anywhere in this window) — the exact pattern
`CAP-004-FINDINGS.md` §2 first documented and named **Cross-Transport Key Derivation (CTKD)**, and
which `CAP-012-FINDINGS.md` §2 confirmed does **not** happen when only system Bluetooth Settings is
used (no BLE tool). `CAP-014` uses nRF Connect (a BLE tool) to initiate bonding, and reproduces
CTKD — this is the **2nd** independently confirmed CTKD instance (`CAP-004`, now `CAP-014`),
consistent with `CAP-012`'s already-established explanation ("CTKD when a BLE tool connects first,
classic SSP when only system settings is used") rather than new evidence toward a different
mechanism. No promotion proposed here — `CAP-012-FINDINGS.md` §2 already carries this as 🟢 FACT;
this section only records that `CAP-014` is a further reproducing data point, worth noting for
future sessions choosing a bonding method.

## 6. Cross-capture reconciliation: `CAP-017`'s two open questions about `0x0f32`/`0x0f2a` (🟢 FACT, narrows both)

`CAP-017-FINDINGS.md` §6 left two open questions this capture directly informs:

- *"What `0x0f32` (value `0x64`) represents, and why it and its CCCD `0x0f33` appear only in this
  session, never in any capture driven by the official app."* — **`CAP-014` reproduces the exact
  same handle, same CCCD pairing, and the identical value `0x64`, 11 days later, in an
  independent session.** This rules out a one-off artifact of `CAP-017`'s specific session; the
  "never in any official-app-driven capture" half of the observation still stands (both sessions
  that show it use nRF Connect, not the official app) — 🟡 HYPOTHESIS, strengthened: `0x0f32`/
  `0x0f33` may be specific to non-official-app GATT clients (e.g. a characteristic the official app
  never happens to read, not one only nRF Connect can see), still not resolved to a UUID or
  semantic meaning by either session.
- *"Whether `0x0f2a` ('Revision 6') is genuinely absent from nRF Connect's own read pattern, or
  simply wasn't triggered because no characteristic-level read happened on screen [in `CAP-017`]."*
  — **Resolved: nRF Connect does read `0x0f2a` when given the chance.** `CAP-014`'s session (a
  reused, cached connection to an already-bonded device, §4) issued a direct `Read Request` against
  `0x0f2a` and got `"Revision 6"` back (§4c) — `CAP-017`'s absence was a property of *that*
  session (no characteristic-level read ever happened on screen there, per its own §4b/§4c), not a
  general property of the nRF Connect tool.

## 7. Test-ID traceability (`AGENTS.md` §13 / §0.1 requirement)

- **`GATT-001`** (this session's primary goal, per `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`): partially
  advanced, not resolved. §0 closes the snaplen half of the previously-identified blocker; §4
  identifies a *new*, more specific blocker (cached-client discovery skip) that a fixed snaplen
  alone cannot fix. See §8 for what would actually close it.
- **`PAIR-001`** (incidental, per this session's own instructions): exercised and evidenced in full
  — §5's CTKD sequence (`device.createBond()` at 20:55:11.345 through `Device bonded` at
  20:55:25.378) is a complete, byte-level-documented pairing lifecycle, on nRF Connect's own
  initiative rather than system Settings.

## 8. Conclusions & next steps

- **Snaplen: fixed, confirmed (🟢 FACT, §0).** This specific blocker from `CAP-017` is closed.
- **Handle↔UUID mapping (`0x0c0X`/`0x0f2X` cluster): still 🔴 OPEN**, but the *reason* has changed
  from "log truncated" (`CAP-017`) to "declaration bytes were never sent on the wire this session,
  because the phone already had them cached from dozens of prior sessions with this same physical
  Buds unit" (§4, this capture). **Fixing the snaplen was necessary but not sufficient** — the
  session also needed a genuine cache-miss, which neither of Group W's own candidate methods
  (untried, §1) nor this session's actual method (reused phone + reused/cached GATT client
  connection) provided.
- **What would actually close this, stated precisely:** a session combining **both** (a) the
  snaplen fix already validated here, **and** (b) a genuine GATT cache miss for the *entire*
  database, not just the GATT service — i.e. actually running Group W's own `pm clear
  com.android.bluetooth` (Option a) or the Pixel 9a (Option b), exactly as already recommended by
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Group W section and never yet tried. `CAP-017`'s cache-miss
  came from a *fresh app install*, which forced full discovery but with a truncated log; `CAP-014`
  had a working log but no cache miss. No capture to date has had both at once.
- **ADR-008: compliant.** §2 confirms the Accessory Non-Owner Service was never read/written this
  session; the stale "planned" instruction to tap into it has been corrected in
  `CAP-014-EVENT-NOTES.md` (§ "Procedure," step 4) to record this as a deliberate ADR-008-driven
  omission rather than an outstanding action item.
- **Two `CAP-017` open questions narrowed (§6)**, one of them effectively resolved (`0x0f2a`'s
  absence in `CAP-017` was session-specific, not tool-specific).
- **Recommended next capture:** repeat Group W's own procedure literally — `pm clear
  com.android.bluetooth` on the Pixel 7a (accepting the "re-pair every other device" cost noted in
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md`), *with* a confirmed-unlimited snaplen (§0's method reused to
  verify beforehand) — this is the first combination that hasn't been tried across `CAP-002`,
  `CAP-003`, `CAP-004`, `CAP-010`, `CAP-017`, and now `CAP-014`.

## 9. Open Questions

- 🔴 Whether the Buds were already unbonded from this phone before the video started (§1) —
  unresolvable from this capture's own artifacts.
- 🔴 The `0x0c0X`/`0x0f2X` handle↔UUID mapping itself — §8 states the precise next step. **Resolved
  2026-09-01 (`CAP-034`, maintainer sign-off obtained per `AGENTS.md` §6):** §8's recommended next
  capture — a fixed snaplen combined with one of Group W's own untried cache-busting methods — was
  run. `0x0c00`–`0x0c14` = Google Fast Pair Service (`0xFE2C`), `0x0f20`–`0x0f2a` = Device
  Information, `0x0f30`–`0x0f33` = Battery Service; every handle this file characterized by byte
  shape now has a spec-verified name. See
  `captures/CAP-034-2026-09-01_06-46-31_06-52-45-Group_W/CAP-034-FINDINGS.md` §4 and
  `PROTOCOL.md` §6.
- 🔴 Handle `0x0034`'s real identity and the "missing first 3 bytes" of its device-name-like read
  value (§4c) — reported as raw bytes, not explained. **Not resolved by `CAP-034`** — that capture's
  own handle `0x0034` belonged to a different, legitimately-declared GAP Device Name characteristic
  on the Buds' own attribute table, not this section's cross-connection-contaminated handle from a
  different peer device; the two are unrelated, still open on its own terms.
- 🔴 `0x0f32`/`0x0f33`'s semantic meaning (value `100`) — reproduces exactly across `CAP-017` and
  `CAP-014`, still no UUID or meaning (§6). **Resolved 2026-09-01 (`CAP-034`):** `0x0f32` is the
  standard Battery Level characteristic (`0x2A19`) within the Battery Service (`0x0f30`–`0x0f33`);
  `0x0f33` is its CCCD. The value `100` is an ordinary 100% battery reading, not a proprietary
  field. See `CAP-034-FINDINGS.md` §4.5.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-014-2026-08-27_20-53-37_20-59-17-Group_W/CAP-014-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-014-2026-08-27_20-53-37_20-59-17-Group_W/CAP-014-FINDINGS
