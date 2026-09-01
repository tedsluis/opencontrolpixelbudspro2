# Findings: `CAP-034` (Group W, 4th attempt — GATT `0x0c0X`/`0x0f2X` handle↔UUID mapping RESOLVED)

**✅ Maintainer sign-off obtained 2026-09-01, per `AGENTS.md` §6/§15.** All four promotions proposed
in §9 (`PROTOCOL.md` §6/§4.3 Option D, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `GATT-001` row,
`CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Capture Index + Group W section, and the cross-reference notes in
`CAP-010`/`CAP-017`/`CAP-014-FINDINGS.md`) have been applied.

Standardized, evidence-based extraction from `CAP-034-btsnoop_hci.log` + `CAP-034-recording.mp4`,
staged here for later promotion into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Modeled on
`CAP-001-FINDINGS.md`. This is the **fourth** capture attempting to resolve the `0x0c0X`/`0x0f2X`
GATT handle↔UUID mapping question open since `CAP-002` (`CAP-010` → `CAP-017` → `CAP-014` →
`CAP-034`) — see §1 for the lineage. Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-034` · **Date:** 2026-09-01 · **Firmware:** ⚪ ASSUMPTION `release_5.203`
(carried over — no official Pixel Buds app used this session, DLCI 0x08/RFCOMM never opens, §2).
**Phone:** Pixel 9a, GrapheneOS (a phone not otherwise used in this project's Group W attempts).
**GATT client app:** nRF Connect for Mobile (Nordic Semiconductor); official Pixel Buds Companion
App **not installed**. **Log file:** `CAP-034-btsnoop_hci.log` (485.4s, 3,717 packets,
2026-09-01 06:45:35.44–06:53:40.82 local/+0200). **Video:** `CAP-034-recording.mp4` (374.5s,
06:46:31–06:52:45 local, on-screen wall-clock overlay). **Devices:** phone (Pixel 9a), peer
`04:00:6E:CF:6E:07` ("Pixel Buds Pro 2 van Ted") — the same physical Buds/case used throughout this
project, and (per the folder's own preparation checklist) not previously connected to this specific
phone.

---

## 0. Capture integrity: unlimited snaplen, zero truncation (🟢 FACT)

```
$ capinfos CAP-034-btsnoop_hci.log
...
Number of packets:   3,717
Packet size limit:   file hdr: (not set)
Interface #0 info: Capture length = 262144
...

$ tshark -r CAP-034-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'
mismatches: 0

$ tshark -r CAP-034-btsnoop_hci.log -T fields -e frame.len | sort -n | tail -1
684
```

No snaplen limit is set; `frame.cap_len == frame.len` holds for all 3,717 frames (0 mismatches);
the largest frame is 684 bytes, comfortably inside any reasonable capture buffer. **`CAP-017`'s
truncation blocker (§2 there) does not recur here** — this is the second capture to confirm a
working, unlimited snaplen (`CAP-014-FINDINGS.md` §0 was the first), and the first to combine it
with a genuine cache-miss (§1, §3).

## 1. Lineage: why `CAP-010`/`CAP-017`/`CAP-014` fell short, and why this one doesn't (🟢 FACT)

| Capture | What worked | What blocked the mapping |
|---|---|---|
| `CAP-010` | — | Did not actually execute Group W's own method (ran bond-removal-only on the *same*, already-used Pixel 7a); zero discovery traffic at all — Android's GATT cache (keyed to phone+peer, independent of the classic bond) served the cluster with no wire declaration (`CAP-010-FINDINGS.md` §1–§2). |
| `CAP-017` | First-ever live discovery (fresh GATT-client-app cache miss); full 15-service UUID list recovered from video | Wire log severely ACL-truncated (~15B snaplen) — discovery *response* bytes (handle ranges, 128-bit UUIDs) never survived capture; no characteristic-level drill-down happened on screen either (`CAP-017-FINDINGS.md` §2, §4b, §4c). |
| `CAP-014` | Snaplen fixed (confirmed 0/4,663 truncated) | Same phone + already-bonded, already-cached nRF Connect client → Android served the `0x0c0X`/`0x0f2X` cluster from its cache again; only the GATT service itself (handles `0x0001`–`0x0009`) was genuinely re-declared live (`CAP-014-FINDINGS.md` §0, §4). |
| **`CAP-034`** | **Both at once**: unlimited snaplen (§0) **and** a genuine full-database cache miss (a phone never before connected to this Buds unit + a fresh GATT client) | — |

**What made the cache miss genuine this time, checked directly (🟢 FACT):** the primary discovery
burst (§3 below) issues a full `Read By Group Type Request, Handles: 0x0001..0xffff` (frame 3272)
and walks every primary service in the database from scratch, including the GATT service itself
(unlike `CAP-014`, where only the GATT service was re-declared) — this is the first Group-W-labeled
session where the *entire* attribute table, not just a fragment of it, is genuinely re-declared on
the wire. This is exactly the combination `CAP-014-FINDINGS.md` §8 identified as still-untried after
three prior attempts.

## 2. Connection identification / CLI hygiene (🟢 FACT)

Per `AGENTS.md` §13's CLI-hygiene rule and `CAP-014-FINDINGS.md` §4a's worked example (a prior
capture's log contained another device's LE connection that had to be excluded first):

```
$ tshark -r CAP-034-btsnoop_hci.log -Y "bthci_evt.le_meta_subevent==0x0a or bthci_evt.le_meta_subevent==0x01" \
  -T fields -e frame.number -e frame.time -e bthci_evt.bd_addr -e bthci_evt.connection_handle
3247  2026-09-01T06:47:42.073317000+0200  04:00:6e:cf:6e:07  0x0040

$ tshark -r CAP-034-btsnoop_hci.log -Y "btatt" -T fields -e bthci_acl.chandle | sort | uniq -c
    194 0x0040
```

**Unlike `CAP-014`, this log contains exactly one LE connection, and it is the Buds' own** — all 194
ATT frames in the file sit on chandle `0x0040`, address `04:00:6e:cf:6e:07`. (The log does contain a
large number of unrelated `LE Extended Advertising Report` frames from dozens of nearby BLE devices,
expected background RF noise from a passive scan window — none of them form a connection, so none
require exclusion.) No cross-device contamination check is needed beyond this.

**RFCOMM/`libmaestro` — not applicable this session (🟢 FACT):**
```
$ tshark -r CAP-034-btsnoop_hci.log -Y "btrfcomm" | wc -l
0
```
This session used nRF Connect only (no official app, no RFCOMM/SPP client) — DLCI 0x08's private
handshake (the usual firmware-string source) never opens, which is why firmware version stays an
⚪ ASSUMPTION above, not a re-confirmed FACT.

## 3. Session structure: one continuous LE link, two evidentiary windows (🟢 FACT)

**Guardrail check, per this session's own task instructions — primary vs. secondary evidence for the
mapping question, determined from the wire itself, not assumed:**

```
$ tshark -r CAP-034-btsnoop_hci.log -Y "bthci_evt.code == 0x05" \
  -T fields -e frame.number -e frame.time -e bthci_evt.connection_handle -e bthci_evt.reason
3684  2026-09-01T06:51:13.438965+0200  0x000b  0x13   <- classic BR/EDR link only
3717  2026-09-01T06:53:40.815261+0200  0x0040  0x13   <- LE link, post-video teardown
```

**The LE connection (`0x0040`) never disconnects between the first connect (06:47:42.073) and the
post-video teardown (06:53:40.815) — there is no second LE connection anywhere in this log.** The
"disconnect/reconnect" visible on screen at 06:51:13/06:51:22 (`f_291.1.jpg`, nRF Connect's UI shows
`DISCONNECTED` and offers `CONNECT` again) is the **classic** BR/EDR link (chandle `0x000b`)
disconnecting after bonding completes — a normal pattern where the classic ACL exists only to carry
bonding/SDP and closes once that's done. nRF Connect's own connection-state UI evidently tracks the
classic `BluetoothDevice` ACL state, not the LE GATT link, causing it to display "disconnected" and
offer a "reconnect" tap even though the underlying LE link (and its already-resolved GATT database)
never went away.

**Consequence — this directly settles the guardrail question, from the log itself:**

- **06:47:42.147–06:47:45.490 (before bonding, first and only genuine discovery walk) is the
  primary, and in this capture the *only*, source for the handle↔UUID mapping** — see §4.
- **06:51:22.158 onward (the "reconnect" tap) is not a second discovery pass at all.** Its entire
  wire-level effect is a single Database Hash re-check:
  ```
  $ tshark -r CAP-034-btsnoop_hci.log -Y "frame.number==3690 or frame.number==3691" \
    -T fields -e frame.number -e frame.time -e btatt.opcode -e _ws.col.Info
  3690  2026-09-01T06:51:22.158235+0200  0x08  Sent Read By Type Request, Database Hash, Handles: 0x0001..0xffff
  3691  2026-09-01T06:51:22.212250+0200  0x09  Rcvd Read By Type Response, Attribute List Length: 1
  ```
  This is the standard GATT-caching hash check (`CAP-003-FINDINGS.md` §1) — it confirms the client
  still trusts its already-resolved database (the one just declared 20s earlier in the same LE
  session) and explicitly does **not** re-walk it. **This capture therefore has no second,
  independent confirmation of the structure — only one discovery pass exists in the whole log.**
  This is a stronger, more precise version of the guardrail's prediction: not "treat window 2 as
  secondary," but "window 2 contributes no structural evidence of its own at all," confirmed
  directly rather than assumed.
- The planned "drill into Unknown Service" step (06:51:37–06:51:40, video `f_306.jpg`/`f_309.jpg`)
  is a **passive scroll** through characteristics already known from the 06:47:42 burst — the only
  ATT frame in that entire window is an unrelated Service-Changed indication acknowledgement:
  ```
  $ tshark -r CAP-034-btsnoop_hci.log -Y "frame.time>=\"2026-09-01 06:51:22\" and frame.time<=\"2026-09-01 06:51:41\" and bthci_acl.chandle==0x0040 and btatt" \
    -T fields -e frame.number -e frame.time -e btatt.opcode -e btatt.handle -e _ws.col.Info
  3690  06:51:22.158235  0x08          Sent Read By Type Request, Database Hash, Handles: 0x0001..0xffff
  3691  06:51:22.212250  0x09  0x0007  Rcvd Read By Type Response, Attribute List Length: 1
  3695  06:51:40.646186  0x1e  0x0003  Rcvd Handle Value Confirmation, Handle: 0x0003 (Unknown)
  ```
  No Read Request ever targets any of the Unknown Service's three characteristic value handles
  (`0x0f39`/`0x0f3b`/`0x0f3d`) in this window. The video's "content" for this step is exactly what
  nRF Connect already rendered from the burst's own declarations (§4.6) — not new wire data.

## 4. THE RESOLUTION: full 15-service handle map, from the primary discovery burst (🟢 FACT)

**Method:** the entire 06:47:42.147–45.490 burst (frames 3264–3469) was extracted and every
`Read By Group Type`/`Read By Type`/`Find Information` response decoded byte-for-byte:

```
tshark -r CAP-034-btsnoop_hci.log -Y "bthci_acl.chandle==0x0040 and btatt" \
  -T fields -e frame.number -e frame.time -e btatt.opcode -e btatt.handle -e _ws.col.Info
```

### 4.1 Primary service list (Read By Group Type, `0x0001..0xffff`)

```
$ tshark -r CAP-034-btsnoop_hci.log -Y "frame.number==3273" -x
0000  02 40 20 42 00 3e 00 04 00 11 06 01 00 09 00 01
0010  18 32 00 36 00 00 18 e0 00 e6 00 4f 18 00 01 16
0020  01 4e 18 00 02 12 02 50 18 50 02 61 02 44 18 00
0030  03 07 03 4d 18 00 0b 00 0b 53 18 10 0b 12 0b 55
0040  18 00 0c 14 0c 2c fe
```
(ATT `Read By Group Type Response`, opcode `0x11`: each 4-byte group is `[start handle LE]
[end handle LE] [UUID16 LE]`.) Frame 3273 (response 1 of 3, request in frame 3272) decodes to:

| # | Start | End | UUID | Name |
|---|---|---|---|---|
| 1 | `0x0001` | `0x0009` | `0x1801` | GATT |
| 2 | `0x0032` | `0x0036` | `0x1800` | GAP |
| 3 | `0x00e0` | `0x00e6` | `0x184F` | Broadcast Audio Scan Service |
| 4 | `0x0100` | `0x0116` | `0x184E` | Audio Stream Control Service |
| 5 | `0x0200` | `0x0212` | `0x1850` | Published Audio Capabilities Service |
| 6 | `0x0250` | `0x0261` | `0x1844` | Volume Control |
| 7 | `0x0300` | `0x0307` | `0x184D` | Microphone Control |
| 8 | `0x0b00` | `0x0b00` | `0x1853` | Common Audio Service |
| 9 | `0x0b10` | `0x0b12` | `0x1855` | Telephony and Media Audio Service |
| 10 | **`0x0c00`** | **`0x0c14`** | **`0xFE2C`** | **Google LLC = Google Fast Pair Service** |

Continued walk (frames 3274→3276, 3277→3279, 3280→3281; each raw hex verified the same way):

| # | Start | End | UUID | Name |
|---|---|---|---|---|
| 11 | `0x0c15` | `0x0c18` | `15190001-12f4-c226-88ed-2ac5579f2a85` | Accessory Non-Owner Service (nRF's name — out of scope per ADR-008, §5) |
| 12 | `0x0f20` | **`0x0f2a`** | `0x180A` | **Device Information** |
| 13 | `0x0f30` | `0x0f33` | `0x180F` | Battery Service |
| 14 | `0x0f37` | `0x0f3e` | `109b862f-50e3-45cc-8ea1-ac62de4846d1` | "Unknown Service" (nRF's own label — no bundled name) |

Handle `0x400`–`0x040f` (Audio Input Control, `0x1843`) is a **secondary** service included by
Microphone Control (confirmed via the `Read By Type Request, Include` sub-walk, frame 3374); walk
terminates cleanly with `Attribute Not Found, Handle: 0x0f3f` (frame 3284) — no service exists above
`0x0f3e`. **This is the same 15-service list `CAP-017-FINDINGS.md` §3 recovered from video only** —
now independently confirmed from raw wire bytes, with every handle range attached for the first
time.

**This single response (frame 3273) already directly answers the open question by itself:**
`0x0c00`–`0x0c14` **is** the Google Fast Pair Service (`0xFE2C`), and `0x0f20`–`0x0f2a` **is** the
Device Information Service (`0x180A`) with **`0x0f2a` as its own end-group handle** — exactly the
two headline items this session's task brief flagged from an informal preview, now confirmed with
full hex-and-script evidence rather than a spot check.

### 4.2 Fast Pair Service (`0xFE2C`, handles `0x0c00`–`0x0c14`) — full characteristic breakdown

```
$ tshark -r CAP-034-btsnoop_hci.log -Y "frame.number==3412" -x
0000  02 40 20 6f 00 6b 00 04 00 09 15 01 0c 02 02 0c
0010  ea 0b 10 32 de 01 b0 8e 14 48 66 83 33 12 2c fe
0020  03 0c 18 04 0c ea 0b 10 32 de 01 b0 8e 14 48 66
0030  83 34 12 2c fe 06 0c 18 07 0c ea 0b 10 32 de 01
0040  b0 8e 14 48 66 83 35 12 2c fe 09 0c 08 0a 0c ea
0050  0b 10 32 de 01 b0 8e 14 48 66 83 36 12 2c fe 0b
0060  0c 18 0c 0c ea 0b 10 32 de 01 b0 8e 14 48 66 83
0070  37 12 2c fe
```
(`Read By Type Response`, opcode `0x09`: each 21-byte group is `[decl handle LE][properties]
[value handle LE][UUID128 LE]`; UUID128 wire bytes are little-endian — reversed below to standard
big-endian notation.) Plus frames 3415, 3417 (`0x0c0e..0x0c13` continuation) and the CCCD
`Find Information Response`s (frames 3422, 3425, 3428, 3430):

| Decl | Value | CCCD | Properties | UUID (reversed from wire) | Name (Fast Pair spec, verified below) |
|---|---|---|---|---|---|
| `0x0c01` | `0x0c02` | — | Read | `fe2c1233-8366-4814-8eb0-01de32100bea` | **Model ID** |
| `0x0c03` | **`0x0c04`** | `0x0c05` | Notify, Write | `fe2c1234-8366-4814-8eb0-01de32100bea` | **Key-based Pairing** |
| `0x0c06` | `0x0c07` | `0x0c08` | Notify, Write | `fe2c1235-8366-4814-8eb0-01de32100bea` | **Passkey** |
| `0x0c09` | **`0x0c0a`** | — | Write | `fe2c1236-8366-4814-8eb0-01de32100bea` | **Account Key** |
| `0x0c0b` | **`0x0c0c`** | `0x0c0d` | Notify, Write | `fe2c1237-8366-4814-8eb0-01de32100bea` | **Additional Data** |
| `0x0c0e` | `0x0c0f` | — | Read | `0x2A26` (standard 16-bit) | Firmware Revision String (a **second**, standard-UUID copy of this characteristic, distinct from `0x0f2a`'s copy under Device Information) |
| `0x0c10` | `0x0c11` | — | Read | `fe2c1239-8366-4814-8eb0-01de32100bea` | **Message Stream PSM Characteristic** |
| `0x0c12` | **`0x0c13`** | `0x0c14` | Notify, Write, Read | `fe2c1238-8366-4814-8eb0-01de32100bea` | not found in any spec page checked — see §8 |

**Names verified against the official spec, fetched live (not from training-data recall) — quoted
verbatim:**
- `developers.google.com/nearby/fast-pair/specifications/characteristics`: confirms Model ID
  (`FE2C1233…`, Read), Key-based Pairing (`FE2C1234…`, Write+notify), Passkey (`FE2C1235…`,
  Write+notify), Account Key (`FE2C1236…`, Write), Additional Data (`FE2C1237…`, Write+notify), and
  Firmware Revision under Device Information Service as standard `0x2A26`.
- `developers.google.com/nearby/fast-pair/specifications/bledevice#message_stream_PSM`: confirms
  "Message Stream PSM Characteristic", UUID `FE2C1239-8366-4814-8EB0-01DE32100BEA`, "allows the
  Seeker to read the PSM value, and then establish secure L2CAP connection by the PSM value."
- `developers.google.com/nearby/fast-pair/specifications/extensions/personalizedname`: confirms
  Personalized Name reuses the Additional Data characteristic (`FE2C1237…`), not a new UUID.
- `FE2C1238-8366-4814-8EB0-01DE32100BEA` (handle `0x0c13`) does **not** appear on any of the three
  pages checked above — genuinely unnamed by this pass, not guessed at (§8).

**This directly, finally resolves the byte-shape hypotheses `CAP-010-FINDINGS.md` §3 and
`CAP-014-FINDINGS.md` §4c could only characterize structurally:**
- The 80-byte first write / 16-byte subsequent write-notify shape on `0x0c04` (`CAP-002`, `CAP-003`,
  `CAP-010`, `CAP-014`) is the **Key-based Pairing** characteristic — exactly the FORM already
  hypothesized since `CAP-003-FINDINGS.md` §4, now a confirmed UUID/name match, not just a
  form/shape match.
- `0x0c0a`'s single opaque write (`CAP-010`, `CAP-014`) is **Account Key**.
- `0x0c0c`'s 40/41-byte notify (`CAP-010` §3, `CAP-014` §4c) is **Additional Data**.
- `0x0c13`/`0x0c14`'s distinct 9/10/32-byte shapes with a leading `0x01` byte, already flagged in
  `CAP-010-FINDINGS.md` §3 as "possibly a structurally distinct characteristic from the Key-based
  Pairing pair" — **confirmed structurally distinct**: it is UUID `FE2C1238…`, not part of the
  Key-based-Pairing/Passkey/Account-Key/Additional-Data quartet at all, still unnamed (§8).

### 4.3 Video cross-check — independent confirmation from nRF Connect's own UUID database (🟢 FACT)

At `t=209s` (06:50:00 on-screen), the video (`f_209.jpg`) shows nRF Connect's `CLIENT` tab rendering
this exact service inline, using its own bundled UUID name database — matching the raw-hex decode
above verbatim, byte-for-byte, on every UUID and property:

> **Google Fast Pair Service**, UUID: `0xFE2C`, PRIMARY SERVICE
> **Model ID**, UUID: `fe2c1233-8366-4814-8eb0-01de32100bea`, Properties: READ
> **Key-based Pairing**, UUID: `fe2c1234-8366-4814-8eb0-01de32100bea`, Properties: NOTIFY, WRITE,
> Descriptors: Client Characteristic Configuration UUID: `0x2902`
> **Passkey**, UUID: `fe2c1235-8366-4814-8eb0-01de32100bea`, Properties: NOTIFY, WRITE…
> **Account Key**, UUID: `fe2c1236-8366-4814-8eb0-01de32100bea`, Properties: WRITE
> **Additional Data**, UUID: `fe2c1237-8366-4814-8eb0-01de32100bea`, Properties: NOTIFY, WRITE…
> **Firmware Revision String**, UUID: `0x2A26`…

This is independent confirmation via a second decoding path (nRF Connect's own client-side UUID
resolution, not this session's `tshark`/manual parse) — the wire-hex decode and the on-screen
UI-rendered decode agree on every field checked.

### 4.4 Device Information Service (`0x180A`, handles `0x0f20`–`0x0f2a`) — resolves `0x0f28`/`0x0f2a`

```
$ tshark -r CAP-034-btsnoop_hci.log -Y "frame.number==3446" -x
0000  02 40 20 29 00 25 00 04 00 09 07 21 0f 02 22 0f
0010  50 2a 23 0f 02 24 0f 29 2a 25 0f 02 26 0f 24 2a
0020  27 0f 02 28 0f 25 2a 29 0f 02 2a 0f 26 2a
```
(`Read By Type Response`, opcode `0x09`, 7-byte groups: `[decl LE][properties][value LE]
[UUID16 LE]`.) Decodes to 5 characteristics:

| Decl | Value | UUID16 | Name |
|---|---|---|---|
| `0x0f21` | `0x0f22` | `0x2A50` | PnP ID |
| `0x0f23` | `0x0f24` | `0x2A29` | Manufacturer Name String |
| `0x0f25` | `0x0f26` | `0x2A24` | Model Number String |
| `0x0f27` | **`0x0f28`** | `0x2A25` | **Serial Number String** |
| `0x0f29` | **`0x0f2a`** | `0x2A26` | **Firmware Revision String** |

**Resolves both long-standing values cleanly, in context:** `0x0f28`'s value `0x31` (ASCII `"1"`,
first observed `CAP-002`, reproduced `CAP-003`/`CAP-010`/`CAP-017`) is the standard **Serial Number
String**'s content, and `0x0f2a`'s `"Revision 6"` (four confirming captures before this one) is the
standard **Firmware Revision String**'s content — both entirely ordinary Device Information Service
semantics, not a Fast-Pair-proprietary value as some earlier framing left open.

### 4.5 Battery Service (`0x180F`, handles `0x0f30`–`0x0f33`) — resolves `0x0f32`/`0x0f33`

```
$ tshark -r CAP-034-btsnoop_hci.log -Y "frame.number==3453" -x
0000  02 40 20 0d 00 09 00 04 00 09 07 31 0f 12 32 0f
0010  19 2a
```
Decodes to one characteristic: decl `0x0f31`, value **`0x0f32`**, properties `0x12` (Notify+Read),
UUID `0x2A19` = **Battery Level**. Its CCCD is `0x0f33` (confirmed via `Find Information Response`,
frame 3458, same 16-bit-UUID pattern as §4.2's CCCDs).

**Resolves `CAP-017-FINDINGS.md` §4/§6 and `CAP-014-FINDINGS.md` §6's open question** ("what does
`0x0f32` (value `0x64`) represent, and why only via nRF Connect") **cleanly**: `0x0f32` is the
standard GATT **Battery Level** characteristic (`0x2A19`), and its previously-observed value `0x64`
= 100 decimal = 100% is an entirely ordinary battery-percentage reading — not a proprietary field,
and not something that would appear when the official app is used instead (the official app reads
battery via the Fast Pair/HFP mechanisms documented in `PROTOCOL.md` §4.3, not this BLE
characteristic, hence why it only ever showed up in nRF-Connect-driven captures).

### 4.6 "Unknown Service" (`109b862f-…`, handles `0x0f37`–`0x0f3e`) — full characteristic map, corrects a `CAP-017` hypothesis

```
$ tshark -r CAP-034-btsnoop_hci.log -Y "frame.number==3464" -x
0000  02 40 20 45 00 41 00 04 00 09 15 38 0f 0a 39 0f
0010  67 b0 58 09 3e 58 9d ab a3 45 58 2d b5 cb 84 85
0020  3a 0f 02 3b 0f fd 96 51 52 c2 fe dd a9 a2 46 10
0030  a9 19 99 eb b4 3c 0f 12 3d 0f ae 38 80 af 62 01
0040  16 ae 5a 4f ae b2 73 d1 6d e6                    
```

| Decl | Value | Properties | UUID (reversed) |
|---|---|---|---|
| `0x0f38` | `0x0f39` | Write, Read | `8584cbb5-2d58-45a3-ab9d-583e0958b067` |
| `0x0f3a` | `0x0f3b` | Read | `b4eb9919-a910-46a2-a9dd-fec2525196fd` |
| `0x0f3c` | `0x0f3d` | Notify, Read | `e66dd173-b2ae-4f5a-ae16-0162af8038ae` |

Byte-for-byte identical to `CAP-017-FINDINGS.md` §4c's later text-log-export list — confirming this
service is stable across sessions (as already established) — **but this resolves, in the corrective
direction, `CAP-017-FINDINGS.md` §6's own open hypothesis**: *"whether `109b862f-…` is the container
for the `0x0c0X` cluster already characterized by byte-shape"* — **no.** This service occupies
handles `0x0f37`–`0x0f3e`, entirely separate from the Fast Pair Service's `0x0c00`–`0x0c14` (§4.2).
The `0x0c0X` cluster's real container is the Google Fast Pair Service, confirmed directly (§4.1–4.2)
— "Unknown Service" remains genuinely unidentified as to *its own* purpose (still no public UUID
match found for `109b862f-50e3-45cc-8ea1-ac62de4846d1` itself), but it is definitively **not** the
answer to the `0x0c0X` question.

### 4.7 Accessory Non-Owner Service (handles `0x0c15`–`0x0c18`) — structurally confirmed, not investigated

```
$ tshark -r CAP-034-btsnoop_hci.log -Y "frame.number==3435" -x
0000  02 40 20 1b 00 17 00 04 00 09 15 16 0c 2c 17 0c
0010  0e 68 21 74 37 48 61 bf 92 fb 68 1d 01 00 0c 8e
```
Decl `0x0c16`, value `0x0c17`, properties `0x2c` (Indicate+Write+WriteNoResponse), UUID
`8e0c0001-1d68-fb92-bf61-48377421680e` — matches `CAP-014-FINDINGS.md` §3's "Accessory Non-Owner
Characteristic" exactly. CCCD at `0x0c18` (frame 3440). See §5 — this is unavoidable discovery-walk
inventory, not an investigation, per ADR-008's already-accepted precedent.

## 5. ADR-008 compliance check: Accessory Non-Owner Service was NOT actively read or written (🟢 FACT)

Checked exhaustively across the full session for any Read Request (`0x0a`) or Write Request (`0x12`)
against handles `0x0c15`–`0x0c18`:

```
$ grep -E "0c15|0c16|0c17|0c18" cap034_att_full.tsv
3274  Sent Read By Group Type Request, Primary Service, Handles: 0x0c15..0xffff
3276  Rcvd Read By Group Type Response, Attribute List Length: 1, Unknown
3431  Sent Read By Type Request, Include, Handles: 0x0c15..0x0c18
3433  Rcvd Error Response - Attribute Not Found, Handle: 0x0c15
3434  Sent Read By Type Request, Characteristic, Handles: 0x0c15..0x0c18
3435  Rcvd Read By Type Response, Attribute List Length: 1, Unknown
3436  Sent Read By Type Request, Characteristic, Handles: 0x0c17..0x0c18
3438  Rcvd Error Response - Attribute Not Found, Handle: 0x0c17
3439  Sent Find Information Request, Handles: 0x0c18..0x0c18
3440  Rcvd Find Information Response, Handle: 0x0c18 (Client Characteristic Configuration)
```

Every one of these is a *declaration*-level sub-procedure (`Read By Group Type`/`Read By Type
Include`/`Read By Type Characteristic`/`Find Information`) — the same unavoidable, blanket
`discoverServices()` walk that also declares every other service in the database (§4.1–§4.7). **No
opcode `0x0a` (Read Request) or `0x12` (Write Request) ever targets handle `0x0c17` (the
characteristic's value) or `0x0c18` (its CCCD) anywhere in this log.** On screen (`f_306.jpg`,
06:51:37), "Accessory Non-Owner Service" is visible at the very top edge of the scrolled list,
partially cut off, consistent with the finger scrolling past it rather than tapping in — matching
`CAP-014-FINDINGS.md` §2/§3's precedent exactly. This session's own event notes (step 7) explicitly
instructed skipping it, and the log confirms that instruction was honored. **Conclusion: ADR-008 was
not violated.**

## 6. Video/event-notes validation — corrects two claims in the planned timeline (🟢 FACT)

Per this session's task instructions, every timestamp in `CAP-034-EVENT-NOTES.md`'s timeline was
cross-checked against the video (`ffmpeg` frame extraction) and the wire log (`tshark`); frame
numbers have been filled in there. Sync was confirmed at the first cross-checkable anchor: the video
overlay reads `06:47:42` (`f_70.9.jpg`) at the same moment the wire log's `LE Extended Create
Connection` command fires (frame 3241, 06:47:42.050644) — a ~0.08s app-processing delay, consistent
throughout.

### 6a. "Read individual characteristics 06:48:13.798–06:50:00" — video shows a passive scroll, not reads

The planned timeline's claim that this window reads "Client Supported Features `2b29`, Fast Pair
Model ID `fe2c1233…`, etc." is **not supported by the wire log** — only 5 standard GATT/GAP reads
succeed in that window (§ Event Notes timeline), none touching Fast Pair. Video frames `f_102.8.jpg`
and `f_150.jpg` show the `CLIENT` tab's per-characteristic `Properties:`/`Descriptors:` rendering
**inline**, unprompted, for every service scrolled past — the same nRF Connect build behavior
`CAP-014-FINDINGS.md` §3 already documented ("renders every characteristic's Properties/Descriptors
inline… with no tap required"). The apparent "individual reads" in the plan were this passive
rendering, not user-triggered `Read` taps.

### 6b. Genuine attempted reads exist — but fail locally, before reaching the wire (🟢 FACT, new observation)

The video's `DEBUG` log tab (`f_274.1.jpg`, visible while scrolled to 06:49:43–06:51:05) shows real
`gatt.readCharacteristic()`/`gatt.setCharacteristicNotification()` calls attempted on the Fast Pair
characteristics, **before** bonding:

```
06:49:56.741  Reading characteristic fe2c1233-8366-4814-8eb0-01de32100bea
06:49:56.741  gatt.readCharacteristic(fe2c1233-8366-4814-8eb0-01de32100bea)
06:49:56.742  Exception occurred (Reading characteristic failed)
06:49:59.627  Reading descriptor 00002902-0000-1000-8000-00805f9b34fb
06:49:59.627  gatt.readDescriptor(00002902-0000-1000-8000-00805f9b34fb)
06:49:59.627  Exception occurred (Reading descriptor failed)
06:50:00.857  Enabling notifications for fe2c1235-8366-4814-8eb0-01de32100bea
06:50:00.857  gatt.setCharacteristicNotification(fe2c1235-8366-4814-8eb0-01de32100bea, true)
06:50:00.860  gatt.writeDescriptor(00002902-0000-1000-8000-00805f9b34fb, value=0x0100)
```

Cross-checked against the wire — **zero** matching frames exist:
```
$ tshark -r CAP-034-btsnoop_hci.log -Y "bthci_acl.chandle==0x0040 and frame.time>=\"2026-09-01 06:49:55\" and frame.time<=\"2026-09-01 06:50:02\""
(no output)
```
Every attempt throws its exception **~1ms** after the call — far too fast for a real BLE round trip
(the discovery burst's own request/response pairs took 30–65ms each, §4). 🟡 HYPOTHESIS, not
confirmed: this is consistent with a local Android-API-level rejection (e.g. firing a new GATT
operation before a prior one's callback returns, a well-known `BluetoothGatt` pitfall) rather than
any wire-level ATT error or encryption requirement — the official Fast Pair spec's own
characteristics table (§4.2) lists these as "Encrypted: No," so an encryption-gate explanation is
not supported by the cited spec text either. Reported as an open, unresolved observation (§8), not a
guessed mechanism.

### 6c. The "reconnect" / "drill into Unknown Service" steps — already covered in §3

`f_291.1.jpg` (06:51:22) and `f_306.jpg`/`f_309.jpg` (06:51:37–40) are the source frames for §3's
finding that no second LE connection or additional discovery ever occurs — see §3 for the full wire
cross-check.

## 7. Test-ID traceability (`AGENTS.md` §13 requirement)

- **`GATT-001`** (this session's primary goal): **resolved** for the `0x0c0X`/`0x0f2X` cluster and,
  incidentally, for the entire 15-service primary database — see §4, §9.
- **`PAIR-001`** (bonding, incidental): exercised in full — §3, LE SMP pairing with CTKD (frames
  3492–3527), classic BR/EDR bond completing 06:51:10.332. **Note, not previously called out this
  precisely:** the on-screen dialog ("Koppelen met Pixel Buds Pro 2 van Ted?") is a generic system
  pairing prompt over an LE Security Manager (SMP) exchange here, not classic SSP — same
  CTKD-when-a-BLE-tool-connects-first pattern already established by `CAP-004`/`CAP-014`
  (`CAP-012-FINDINGS.md` §2's explanation), reproduced a third time.
- **`PAIR-003`** (disconnect/reconnect, optional): attempted but, per §3, the "reconnect" only
  affects the classic link — the LE link this project cares about for `GATT-001` never disconnects,
  so this Test-ID's own goal (a genuine GATT-level reconnect cycle) is not actually exercised this
  session; carried forward as still open for a future session if wanted.
- **`BATT-003`** (idle spontaneous-traffic window): exercised (06:51:40–06:52:45) — zero traffic
  observed, consistent with a short (~1 minute) idle window on an LE-only connection with no active
  battery-push mechanism engaged (this session never uses HFP/Fast-Pair-Message-Stream, the usual
  battery-push channels per `PROTOCOL.md` §4.3).

## 8. Open questions remaining after this session

- 🔴 `FE2C1238-8366-4814-8EB0-01DE32100BEA` (handle `0x0c12`/`0x0c13`, Notify+Write+Read) has no
  confirmed official name. Checked live against the Fast Pair base characteristics spec, the Message
  Stream extension (`bledevice#message_stream_PSM`), and the Personalized Name extension — none
  document this UUID. Its byte-shape characterization from `CAP-010`/`CAP-014` (leading `0x01` byte,
  9/10/32-byte payloads) still stands unconnected to any spec name. A further spec page (e.g. a
  Beacon Actions or other extension not yet checked) or an APK static-analysis pass
  (`REVERSE_ENGINEERING.md`) could resolve this; not attempted further here per this session's scope
  (BLE/GATT capture analysis, not APK analysis).
- 🔴 Why nRF Connect's pre-bond Fast Pair reads/subscribes fail locally with a ~1ms exception and
  zero wire traffic (§6b) — flagged as a HYPOTHESIS (local API-misuse pattern), not confirmed.
- 🔴 "Unknown Service" (`109b862f-50e3-45cc-8ea1-ac62de4846d1`)'s own purpose remains unidentified —
  this session corrects what it is *not* (§4.6) but does not identify what it *is*.
- 🔴 Carried forward, unaffected: the `libmaestro`/ANC-EQ control channel identity (DLCI 0x02/0x08)
  — this session is BLE/GATT-only and touches neither (§2).

## 9. Conclusions & downstream updates (✅ maintainer sign-off obtained 2026-09-01, applied)

**The `0x0c0X`/`0x0f2X` handle↔UUID mapping question, open since `CAP-002`, is resolved by this
capture (🟢 FACT, §4), on the strength of the primary 06:47:42.147–45.490 discovery burst alone —
independently corroborated by the video's own nRF-Connect-rendered UUID names (§4.3).** Stated
plainly, not hedged beyond what the data supports: `0x0c00`–`0x0c14` is the Google Fast Pair Service
(`0xFE2C`) with all 5 spec-defined characteristics (Model ID, Key-based Pairing, Passkey, Account
Key, Additional Data) plus the Message Stream PSM characteristic and one still-unnamed
`FE2C1238…` characteristic; `0x0f20`–`0x0f2a` is the standard Device Information Service; `0x0f30`–
`0x0f33` is the standard Battery Service. The reconnect window (§3) contributes no independent
second confirmation — this capture's resolution rests on one discovery pass, not two, and that pass
is judged sufficient because it is a full, genuine `0x0001..0xffff` walk with an untruncated log and
independent video corroboration.

**Maintainer sign-off obtained 2026-09-01 — all four applied:**

1. **`PROTOCOL.md` §6**'s `0x0c0X`/`0x0f2X` open item — closed, citing this capture (§4.1–§4.7) as
   🟢 FACT, with the full handle/UUID/name table added in place of the "🔴 OPEN QUESTION" framing.
   `PROTOCOL.md` §4.3 Option D (Battery Service) also updated with the resolved handle range.
2. **`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`**'s `GATT-001` row — marked resolved, citing this file in
   place of the prior "still 🔴 OPEN" `CAP-014` note.
3. **`CAPTURE_BLUETOOTH_HCI_SNOOP.md`**'s Capture Index — a `CAP-034` row added (Group W, 4th
   attempt, method = `pm clear com.android.bluetooth` on a phone never before connected to this
   Buds unit + nRF Connect, both `GATT-001` blockers combined for the first time) and Group W's own
   section updated with a ✅ RESOLVED banner.
4. **Closing cross-references** added to `CAP-010-FINDINGS.md` §8, `CAP-017-FINDINGS.md` §6, and
   `CAP-014-FINDINGS.md` §9 — each now points forward to this resolution where they previously
   listed the mapping as still open.
5. **`DECISIONS.md`** — no new ADR is proposed; this is a protocol-fact resolution (governed by
   `PROTOCOL.md`'s own FACT/HYPOTHESIS process), not an architecture or scope decision.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-034-2026-09-01_06-46-31_06-52-45-Group_W/CAP-034-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-034-2026-09-01_06-46-31_06-52-45-Group_W/CAP-034-FINDINGS
